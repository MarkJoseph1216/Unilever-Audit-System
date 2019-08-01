package com.android.pplusaudit2.Report.AuditSummary;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.General;
import com.android.pplusaudit2.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ReportAuditActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private UserSummary userSummary;
    private long selectedAuditID;
    private ListView lvwAuditSummary;
    private ArrayList<ReportField> arrReports;
    private ReportFieldAdapter reportAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_audit_activity);

        String title = "Audit Summary";
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(title);
        overridePendingTransition(R.anim.slide_up, R.anim.hold);

        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(this, General.errlogFile));

        final Spinner spnAudit = (Spinner) findViewById(R.id.spnAudit);
        Button btnProcess = (Button) findViewById(R.id.btnProcess);

        AuditAdapter dataAdapter = new AuditAdapter(ReportAuditActivity.this, android.R.layout.simple_dropdown_item_1line, General.arraylistAudits);
        spnAudit.setAdapter(dataAdapter);

        btnProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedAuditID = spnAudit.getSelectedItemId();
                new CheckInternet().execute();
            }
        });

        arrReports = new ArrayList<>();
        lvwAuditSummary = (ListView) findViewById(R.id.lvwAuditSummary);
        reportAdapter = new ReportFieldAdapter(ReportAuditActivity.this, arrReports);
        lvwAuditSummary.setAdapter(reportAdapter);
    }

    public class GetAuditSummaryReport extends AsyncTask<Void, Void, Boolean> {
        String error = "";
        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(ReportAuditActivity.this, "", "Processing report.");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;
            try {
                String response = "";

                URL url = new URL(General.URL_REPORT_USERSUMMARY + "/" + selectedAuditID + "/user/" + General.usercode);
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

                if (!response.trim().equals("")) {

                    if(response.trim().contains("No Report Available")) {
                        String msg = new JSONObject(response).getString("msg");
                        error = msg;
                        return result;
                    }

                    JSONObject jsonObject = new JSONObject(response);

                    String name = jsonObject.getString("name");
                    String month = jsonObject.getString("description");
                    int mappedStores = jsonObject.getInt("mapped_stores");
                    int storesVisited = jsonObject.getInt("store_visited");
                    int perfectStores = jsonObject.getInt("perfect_store_count");
                    double storeAchieve = jsonObject.getDouble("perfect_store_achivement");
                    int categoryDoors = jsonObject.getInt("category_doors");
                    double doorAchieve = jsonObject.getDouble("category_door_per");
                    userSummary = new UserSummary(name, month, mappedStores, storesVisited, perfectStores, storeAchieve, categoryDoors, doorAchieve);

                    result = true;
                }
            }
            catch (MalformedURLException ex) {
                Log.e("URL Error", ex.getMessage());
                error = ex.getMessage();
            }
            catch (JSONException ex) {
                Log.e("JSON Error", ex.getMessage());
                error = ex.getMessage();
            }
            catch (IOException ex) {
                Log.e("IO Error", ex.getMessage());
                error = "No data found.";
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            progressDialog.dismiss();
            arrReports.clear();
            reportAdapter.notifyDataSetChanged();
            if(!aBoolean) {
                Toast.makeText(ReportAuditActivity.this, error, Toast.LENGTH_LONG).show();
                return;
            }

            ArrayList<ReportField> arrayList = new ArrayList<>();
            arrayList.add(new ReportField(1, "Username", General.userFullName.toUpperCase()));
            arrayList.add(new ReportField(2, "Month", userSummary.month));
            arrayList.add(new ReportField(3, "Stores Mapped", String.valueOf(userSummary.storesMapped)));
            arrayList.add(new ReportField(4, "Stores Audited", String.valueOf(userSummary.storesAudited)));
            arrayList.add(new ReportField(5, "Perfect Stores", String.valueOf(userSummary.perfectStores)));
            arrayList.add(new ReportField(6, "% Achievement", userSummary.storeAchievement + " %"));
            arrayList.add(new ReportField(7, "Category Doors", String.valueOf(userSummary.categoryDoors)));
            arrayList.add(new ReportField(8, "% Achievement", String.valueOf(userSummary.categoryAchievements) + " %"));

            arrReports.addAll(arrayList);
            reportAdapter.notifyDataSetChanged();
        }
    }

    public class CheckInternet extends AsyncTask<Void, Void, Boolean> {
        String errmsg = "";
        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(ReportAuditActivity.this, "", "Checking internet connection.");
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
                Toast.makeText(ReportAuditActivity.this, errmsg, Toast.LENGTH_SHORT).show();
                return;
            }

            new GetAuditSummaryReport().execute();
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
        overridePendingTransition( R.anim.hold, R.anim.slide_down );
    }
}
