package com.android.pplusaudit2.Report.PjpFrequencyReport;

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
import android.widget.TextView;
import android.widget.Toast;

import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.ErrorLogs.ErrorLog;
import com.android.pplusaudit2.General;
import com.android.pplusaudit2.R;
import com.android.pplusaudit2.Report.AuditSummary.AuditAdapter;
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

public class PjpFrequencyActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private String TAG;
    private ErrorLog errorLog;
    private long selectedAuditID;

    private ArrayList<FrequencyItem> arrFrequencyItems;
    private ArrayList<FrequencyItem> arrFrequencyItemsLoader;
    private FrequencyItemAdapter frequencyReportAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pjp_frequency_activity);

        errorLog = new ErrorLog(General.errlogFile, this);
        TAG = this.getLocalClassName();
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(this, General.errlogFile));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        overridePendingTransition(R.anim.slide_up, R.anim.hold);

        arrFrequencyItems = new ArrayList<>();
        arrFrequencyItemsLoader = new ArrayList<>();

        final Spinner spnAudit = (Spinner) findViewById(R.id.spnAudit);
        TextView tvwUserName = (TextView) findViewById(R.id.tvwUserName);
        Button btnProcess = (Button) findViewById(R.id.btnProcess);
        ListView lvwPjpFrequency = (ListView) findViewById(R.id.lvwPjpFrequency);

        AuditAdapter dataAdapter = new AuditAdapter(PjpFrequencyActivity.this, android.R.layout.simple_dropdown_item_1line, General.arraylistAudits);
        spnAudit.setAdapter(dataAdapter);

        btnProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedAuditID = spnAudit.getSelectedItemId();
                new CheckInternet().execute();
            }
        });
        tvwUserName.setText(General.userFullName);

        frequencyReportAdapter = new FrequencyItemAdapter(this, arrFrequencyItems);
        lvwPjpFrequency.setAdapter(frequencyReportAdapter);

    }

    private class CheckInternet extends AsyncTask<Void, Void, Boolean> {
        String errmsg = "";

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(PjpFrequencyActivity.this, "", "Checking internet connection.");
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
                Toast.makeText(PjpFrequencyActivity.this, errmsg, Toast.LENGTH_SHORT).show();
                return;
            }

            new FetchOsaReportTask().execute();
        }
    }

    private class FetchOsaReportTask extends AsyncTask<Void, Void, Boolean> {

        private String errMsg;
        private String response;

        @Override
        protected void onPreExecute() {
            arrFrequencyItemsLoader.clear();
            progressDialog = ProgressDialog.show(PjpFrequencyActivity.this, "", "Fetching PJP Frequency Report. Please wait.", false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            boolean result = false;

            try {

                URL url = new URL(General.URL_REPORT_PJP_FREQUENCY_REPORT + "/" + selectedAuditID + "/user/" + General.usercode);
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
                response = stringBuilder.toString().toLowerCase();

                if (!response.contains("no report available")) {

                    JSONArray dataArray = new JSONArray(response);

                    if(dataArray.length() > 0) {

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject jsonObject = (JSONObject) dataArray.get(i);

                            FrequencyItem frequencyItem = new FrequencyItem(i + 1);
                            frequencyItem.frequency = jsonObject.getInt("frequency");
                            frequencyItem.storeName = jsonObject.getString("store_name");

                            arrFrequencyItemsLoader.add(frequencyItem);
                        }
                        result = true;
                    }
                    else {
                        errMsg = "No reports found.";
                    }
                }
                else {
                    errMsg = new JSONObject(response).getString("msg").trim().toLowerCase();
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
                Toast.makeText(PjpFrequencyActivity.this, errMsg, Toast.LENGTH_LONG).show();
            }

            arrFrequencyItems.clear();
            arrFrequencyItems.addAll(arrFrequencyItemsLoader);
            frequencyReportAdapter.notifyDataSetChanged();
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
