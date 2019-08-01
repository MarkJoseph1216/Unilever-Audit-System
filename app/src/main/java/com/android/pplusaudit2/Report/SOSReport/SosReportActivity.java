package com.android.pplusaudit2.Report.SOSReport;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SosReportActivity extends AppCompatActivity {

    private ErrorLog errorLog;
    private String TAG;

    private ArrayList<SosItem> sosItemArrayList;
    private ArrayList<SosItem> sosItemArrayListLoader;
    private long selectedAuditID;
    private ProgressDialog progressDialog;
    private SosReportAdapter sosReportAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sos_report_activity);

        errorLog = new ErrorLog(General.errlogFile, this);
        TAG = this.getLocalClassName();
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(this, General.errlogFile));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sosItemArrayList = new ArrayList<>();
        sosItemArrayListLoader = new ArrayList<>();

        final Spinner spnAudit = (Spinner) findViewById(R.id.spnAudit);
        Button btnProcess = (Button) findViewById(R.id.btnProcess);

        AuditAdapter dataAdapter = new AuditAdapter(SosReportActivity.this, android.R.layout.simple_dropdown_item_1line, General.arraylistAudits);
        spnAudit.setAdapter(dataAdapter);

        btnProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedAuditID = spnAudit.getSelectedItemId();
                new CheckInternet().execute();
            }
        });

        ListView lvwSosReport = (ListView) findViewById(R.id.lvwSosReport);
        sosReportAdapter = new SosReportAdapter(SosReportActivity.this, sosItemArrayList);
        lvwSosReport.setAdapter(sosReportAdapter);
    }

    private class CheckInternet extends AsyncTask<Void, Void, Boolean> {
        String errmsg = "";

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(SosReportActivity.this, "", "Checking internet connection.");
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
                Toast.makeText(SosReportActivity.this, errmsg, Toast.LENGTH_SHORT).show();
                return;
            }

            new GetSosReportTask().execute();
        }
    }

    private class GetSosReportTask extends AsyncTask<Void, Void, Boolean> {
        String errormsg = "";
        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(SosReportActivity.this, "", "Processing Report.");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;

            String response = "";
            sosItemArrayList.clear();

            try {
                URL url = new URL(General.URL_REPORT_SOS + "/" + selectedAuditID + "/user/" + General.usercode);
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

                if(response.trim().contains("No Report Available")) {
                    String msg = new JSONObject(response).getString("msg");
                    errormsg = msg;
                    sosItemArrayList.clear();
                    return false;
                }

                if (!response.trim().equals("")) {

                    sosItemArrayListLoader.clear();

                    JSONArray dataArray = new JSONArray(response);

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject jsonObject = (JSONObject) dataArray.get(i);

                        String sos = jsonObject.getString("sos_measurement").trim().equals("") ? "0" : jsonObject.getString("sos_measurement").trim();
                        String target = jsonObject.getString("target").trim().equals("") ? "0" : jsonObject.getString("target").trim();

                        SosItem item = new SosItem(i+1);
                        item.auditID = jsonObject.getInt("audit_id");
                        item.desc = jsonObject.getString("description");
                        item.storeName = jsonObject.getString("store_name");
                        item.storeCode = jsonObject.getString("store_code");
                        item.category = jsonObject.getString("category");
                        item.psSosMeasurement = Double.valueOf(sos);
                        item.customerName = jsonObject.getString("customer");
                        item.auditTemplate = jsonObject.getString("template");
                        item.target = Double.valueOf(target);

                        sosItemArrayListLoader.add(item);
                    }

                    result = true;
                }
            }
            catch (IOException ex) {
                errormsg = "Error in fetching reports. Please check internet connection and try again.";
                String exErr = ex.getMessage() != null ? ex.getMessage() : errormsg;
                errorLog.appendLog(exErr, TAG);
            }
            catch (JSONException ex) {
                errormsg = "Error in web return response.";
                String exErr = ex.getMessage() != null ? ex.getMessage() : errormsg;
                errorLog.appendLog(exErr, TAG);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean bResult) {
            progressDialog.dismiss();
            if(!bResult) {
                Toast.makeText(SosReportActivity.this, errormsg, Toast.LENGTH_LONG).show();
            }

            sosItemArrayList.clear();
            sosItemArrayList.addAll(sosItemArrayListLoader);
            sosReportAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if(id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
