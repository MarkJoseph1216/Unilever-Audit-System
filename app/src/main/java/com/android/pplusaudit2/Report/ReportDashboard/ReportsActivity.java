package com.android.pplusaudit2.Report.ReportDashboard;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.ErrorLogs.ErrorLog;
import com.android.pplusaudit2.General;
import com.android.pplusaudit2.R;
import com.android.pplusaudit2.Report.AuditSummary.Audit;
import com.android.pplusaudit2.Report.AuditSummary.ReportAuditActivity;
import com.android.pplusaudit2.Report.CustomerRegionSummary.CustomerRegionReport;
import com.android.pplusaudit2.Report.CustomerSummary.CustomerSummaryReport;
import com.android.pplusaudit2.Report.CustomizedPlanoReport.CustomizedPlanoReportActivity;
import com.android.pplusaudit2.Report.NPIReport.NpiReportActivity;
import com.android.pplusaudit2.Report.OSAReport.OsaReportActivity;
import com.android.pplusaudit2.Report.PjpFrequencyReport.PjpFrequencyActivity;
import com.android.pplusaudit2.Report.SOSReport.SosReportActivity;
import com.android.pplusaudit2.Report.StoreSummary.ReportStoreActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ReportsActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private String TAG;
    private ErrorLog errorLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reports_activity);

        TAG = ReportsActivity.this.getLocalClassName();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        overridePendingTransition(R.anim.slide_in_left, R.anim.hold);
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(this, General.errlogFile));

        errorLog = new ErrorLog(General.errlogFile, this);

        final ArrayList<Reports> reportsArrayList = new ArrayList<>();
        reportsArrayList.add(new Reports(1, "USER AUDIT SUMMARY REPORT", "Audit summary report of user per audit month.", "\uf0c5"));
        reportsArrayList.add(new Reports(2, "STORE AUDIT SUMMARY REPORT", "Stores audit summary report per audit month.", "\uf0c5"));
        reportsArrayList.add(new Reports(3, "CUSTOMER SUMMARY REPORT", "Customer report per audit month.", "\uf0c5"));
        reportsArrayList.add(new Reports(4, "CUSTOMER REGION SUMMARY REPORT", "Customer regions report per audit month.", "\uf0c5"));
        reportsArrayList.add(new Reports(5, "OSA REPORT - PER SKU", "Report of SKU's On Shelf Availability.", "\uf0c5"));
        reportsArrayList.add(new Reports(6, "NPI REPORT - PER SKU", "Report of SKU's NPI.", "\uf0c5"));
        reportsArrayList.add(new Reports(7, "SOS REPORT", "SOS Report of user.", "\uf0c5"));
        reportsArrayList.add(new Reports(8, "CUSTOMIZED PLANOGRAM REPORT", "Customized planogram report.", "\uf0c5"));
        reportsArrayList.add(new Reports(9, "PJP FREQUENCY REPORT", "PJP frequency report of user.", "\uf0c5"));

        ListView lvwReports = (ListView) findViewById(R.id.lvwReports);
        lvwReports.setAdapter(new ReportsAdapter(this, reportsArrayList));

        lvwReports.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new CheckInternet(reportsArrayList.get(position).reportID).execute();
            }
        });
    }

    private class CheckInternet extends AsyncTask<Void, Void, Boolean> {
        String errmsg = "";
        int reportID;

        CheckInternet(int reportID) {
            this.reportID = reportID;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(ReportsActivity.this, "", "Checking internet connection.");
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
            else errmsg = "No internet connection.";

            return result;
        }

        @Override
        protected void onPostExecute(Boolean bResult) {
            progressDialog.dismiss();
            if(!bResult) {
                Toast.makeText(ReportsActivity.this, errmsg, Toast.LENGTH_SHORT).show();
                return;
            }

            new FetchReportData(reportID).execute();
        }
    }

    private class FetchReportData extends AsyncTask<Void, Void, Boolean> {
        String response = null;
        int reportID;
        private String errorMsg;

        FetchReportData(int reportID) {
            this.reportID = reportID;
            General.arraylistAudits = new ArrayList<>();
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(ReportsActivity.this, "", "Fetching report data.");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;

            try {

                URL url = new URL(General.URL_REPORT_AUDITS);
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
                if(GetAudits(response)) { // GET AUDIT DATES
                    result = true;
/*                    switch (reportID) {
                        case 1: // USER SUMMARY REPORT
                            result = GetUserSummaryReport();
                            break;
                        case 2: // STORE REPORT
                            result = GetStoreReport();
                            break;
                        default:
                            break;
                    }*/
                }
            }
            catch (MalformedURLException ex) {
                ex.printStackTrace();
                errorMsg = "Can't connect to web server. Please try again.";
                String errmsg = ex.getMessage() != null ? ex.getMessage() : errorMsg;
                errorLog.appendLog(errmsg, TAG);
            }
            catch (IOException ex) {
                ex.printStackTrace();
                errorMsg = "Slow or unstable internet connection. Please try again.";
                String errmsg = ex.getMessage() != null ? ex.getMessage() : errorMsg;
                errorLog.appendLog(errmsg, TAG);
            }
            catch (JSONException ex) {
                ex.printStackTrace();
                errorMsg = "Error in web response of server. Please try again.";
                String errmsg = ex.getMessage() != null ? ex.getMessage() : errorMsg;
                errorLog.appendLog(errmsg, TAG);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean bResult) {
            progressDialog.dismiss();
            if(!bResult) {
                Toast.makeText(ReportsActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = null;

            switch (reportID) {
                case 1: // USER SUMMARY REPORT
                    intent = new Intent(ReportsActivity.this, ReportAuditActivity.class);
                    break;
                case 2: // STORE SUMMARY REPORT
                    intent = new Intent(ReportsActivity.this, ReportStoreActivity.class);
                    break;
                case 3: // CUSTOMER SUMMARY REPORT
                    intent = new Intent(ReportsActivity.this, CustomerSummaryReport.class);
                    break;
                case 4: // CUSTOMER REGIONAL SUMMARY REPORT
                    intent = new Intent(ReportsActivity.this, CustomerRegionReport.class);
                    break;
                case 5: // PER SKU OSA REPORT
                    intent = new Intent(ReportsActivity.this, OsaReportActivity.class);
                    break;
                case 6: // PER SKU NPI REPORT
                    intent = new Intent(ReportsActivity.this, NpiReportActivity.class);
                    break;
                case 7: // SOS REPORT
                    intent = new Intent(ReportsActivity.this, SosReportActivity.class);
                    break;
                case 8: // CUSTOMIZED PLANOGRAM REPORT
                    intent = new Intent(ReportsActivity.this, CustomizedPlanoReportActivity.class);
                    break;
                case 9: // PJP FREQUENCY REPORT
                    intent = new Intent(ReportsActivity.this, PjpFrequencyActivity.class);
                    break;
                default:
                    break;
            }
            startActivity(intent);
        }
    }

    private boolean GetAudits(String response) throws JSONException {
        boolean res = false;
        if(response == null || response.trim().equals("")) {
            return false;
        }

        JSONArray dataArray = new JSONArray(response);
        if(dataArray.length() == 0) return false;

        General.arraylistAudits.clear();

        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject data = (JSONObject) dataArray.get(i);
            General.arraylistAudits.add(new Audit(data.getInt("audit_id"), data.getString("description")));
            res = true;
        }

        return res;
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
        overridePendingTransition(R.anim.hold, R.anim.slide_in_right);
    }
}
