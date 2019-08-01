package com.android.pplusaudit2.Report.NPIReport;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.ErrorLogs.ErrorLog;
import com.android.pplusaudit2.General;
import com.android.pplusaudit2.R;
import com.android.pplusaudit2.Report.AuditSummary.AuditAdapter;
import com.android.pplusaudit2.Report.OSAReport.OsaItem;
import com.android.pplusaudit2.Report.OSAReport.OsaReportActivity;
import com.android.pplusaudit2.Report.OSAReport.OsaReportAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class NpiReportActivity extends OsaReportActivity {

    private ProgressDialog progressDialog;
    private String TAG;
    private ErrorLog errorLog;
    private long selectedAuditID;

    private ArrayList<OsaItem> arrNpiItems;
    private ArrayList<OsaItem> arrNpiItemsLoader;
    private OsaReportAdapter npiReportAdapter;
    private String urlNpi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.npi_report_activity);

        errorLog = new ErrorLog(General.errlogFile, this);
        TAG = this.getLocalClassName();
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(this, General.errlogFile));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        overridePendingTransition(R.anim.slide_up, R.anim.hold);

        arrNpiItems = new ArrayList<>();
        arrNpiItemsLoader = new ArrayList<>();

        final Spinner spnAudit = (Spinner) findViewById(R.id.spnAudit);
        Button btnProcess = (Button) findViewById(R.id.btnProcess);
        ListView lvwOsaReports = (ListView) findViewById(R.id.lvwOsaReports);

        AuditAdapter dataAdapter = new AuditAdapter(NpiReportActivity.this, android.R.layout.simple_dropdown_item_1line, General.arraylistAudits);
        spnAudit.setAdapter(dataAdapter);

        btnProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedAuditID = spnAudit.getSelectedItemId();
                urlNpi = General.URL_REPORT_NPI + "/" + selectedAuditID + "/user/" + General.usercode;
                new CheckInternet().execute();
            }
        });

        npiReportAdapter = new OsaReportAdapter(this, arrNpiItems);
        lvwOsaReports.setAdapter(npiReportAdapter);
    }

    private class CheckInternet extends AsyncTask<Void, Void, Boolean> {
        String errmsg = "";

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(NpiReportActivity.this, "", "Checking internet connection.");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;

            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if(activeNetwork != null) {
                if(activeNetwork.isFailover()) errmsg = "Internet connection fail over.";
                result = activeNetwork.isAvailable() || activeNetwork.isConnectedOrConnecting();
            }
            else errmsg = "Not connected to the internet.";

            return result;
        }

        @Override
        protected void onPostExecute(Boolean bResult) {
            progressDialog.dismiss();
            if(!bResult) {
                Toast.makeText(NpiReportActivity.this, errmsg, Toast.LENGTH_SHORT).show();
                return;
            }

            new FetchNpiReportTask().execute();
        }
    }

    private class FetchNpiReportTask extends AsyncTask<Void, Void, Boolean> {

        private String errMsg;
        private String response;

        @Override
        protected void onPreExecute() {
            arrNpiItemsLoader.clear();
            progressDialog = ProgressDialog.show(NpiReportActivity.this, "", "Fetching NPI Report per SKU. Please wait.", false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            boolean result = false;

            try {

                URL url = new URL(urlNpi);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                urlConnection.disconnect();
                response = stringBuilder.toString();

                if (response.trim().contains("No Report Available")) {
                    errMsg = new JSONObject(response).getString("msg");
                    return false;
                }

                if (!response.trim().equals("")) {

                    JSONArray dataArray = new JSONArray(response);

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject jsonObject = (JSONObject) dataArray.get(i);

                        OsaItem osaItem = new OsaItem(i+1);
                        osaItem.osaPercent = jsonObject.getDouble("osa_percent");
                        osaItem.template = jsonObject.getString("template");
                        osaItem.category = jsonObject.getString("category");
                        osaItem.description = jsonObject.getString("description");
                        osaItem.auditID = jsonObject.getInt("audit_id");
                        osaItem.userID = jsonObject.getInt("user_id");
                        osaItem.storeCount = jsonObject.getInt("store_count");
                        osaItem.prompt = jsonObject.getString("prompt");
                        osaItem.customerName = jsonObject.getString("customer");
                        osaItem.group = jsonObject.getString("group");
                        osaItem.channelCode = jsonObject.getString("channel_code");
                        osaItem.availability = jsonObject.getInt("availability");

                        arrNpiItemsLoader.add(osaItem);
                    }
                    result = true;
                }
            }
            catch (IOException | JSONException ex) {
                errMsg = "Error in fetching reports. Please check internet connection and try again.";
                String exErr = ex.getMessage() != null ? ex.getMessage() : errMsg;
                errorLog.appendLog(exErr, TAG);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean bResult) {
            progressDialog.dismiss();
            if(!bResult) {
                Toast.makeText(NpiReportActivity.this, errMsg, Toast.LENGTH_LONG).show();
                return;
            }

            arrNpiItems.clear();
            arrNpiItems.addAll(arrNpiItemsLoader);
            npiReportAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition( R.anim.hold, R.anim.slide_down );
    }
}
