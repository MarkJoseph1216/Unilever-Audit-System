package com.android.pplusaudit2.Report.CustomerRegionSummary;

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
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.ErrorLogs.ErrorLog;
import com.android.pplusaudit2.General;
import com.android.pplusaudit2.R;
import com.android.pplusaudit2.Report.AuditSummary.AuditAdapter;
import com.android.pplusaudit2.Report.CustomerSummary.Customer;
import com.android.pplusaudit2.Report.CustomerSummary.CustomerAdapter;
import com.android.pplusaudit2.Report.CustomerSummary.CustomerStoreItem;
import com.android.pplusaudit2.Report.CustomerSummary.CustomerSummaryReport;
import com.android.pplusaudit2.Report.CustomerSummary.CustomerSummarySubReport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class CustomerRegionReport extends AppCompatActivity {

    private ProgressDialog progressDialog;

    private ArrayList<Customer> arrCustomerItems;
    private ArrayList<Customer> arrCustomerLoader;
    private ArrayList<CustomerStoreItem> arrCustSummarySubItems;
    private long selectedAuditID;
    private CustomerRegionAdapter customersAdapter;

    private ErrorLog errorLog;
    private String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_region_report_activity);

        errorLog = new ErrorLog(General.errlogFile, this);
        TAG = this.getLocalClassName();
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(this, General.errlogFile));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        arrCustomerItems = new ArrayList<>();
        arrCustomerLoader = new ArrayList<>();
        arrCustSummarySubItems = new ArrayList<>();

        final Spinner spnAudit = (Spinner) findViewById(R.id.spnAudit);
        ListView lvwRegionCustomers = (ListView) findViewById(R.id.lvwRegionCustomers);
        Button btnProcess = (Button) findViewById(R.id.btnProcess);

        AuditAdapter dataAdapter = new AuditAdapter(CustomerRegionReport.this, android.R.layout.simple_dropdown_item_1line, General.arraylistAudits);
        spnAudit.setAdapter(dataAdapter);

        customersAdapter = new CustomerRegionAdapter(CustomerRegionReport.this, arrCustomerItems);
        lvwRegionCustomers.setAdapter(customersAdapter);

        btnProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedAuditID = spnAudit.getSelectedItemId();
                new CheckInternet().execute();
            }
        });

        lvwRegionCustomers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                General.selectedCustomer = arrCustomerItems.get(position);

                if(General.selectedCustomer.customerStoreItems.size() == 0) {
                    Toast.makeText(CustomerRegionReport.this, "No records found for this customer.", Toast.LENGTH_LONG).show();
                    return;
                }

                startActivity(new Intent(CustomerRegionReport.this, CustomerRegionSubReport.class));
            }
        });
    }


    private class CheckInternet extends AsyncTask<Void, Void, Boolean> {
        String errmsg = "";

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(CustomerRegionReport.this, "", "Checking internet connection.");
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
                Toast.makeText(CustomerRegionReport.this, errmsg, Toast.LENGTH_SHORT).show();
                return;
            }

            new FetchCustomerSummaryReport().execute();
        }
    }

    private class FetchCustomerSummaryReport extends AsyncTask<Void, Void, Boolean> {
        String errormsg = "";

        @Override
        protected void onPreExecute() {
            arrCustomerLoader.clear();
            progressDialog = ProgressDialog.show(CustomerRegionReport.this, "", "Fetching Customer Summary report. Please wait.");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;

            String response = "";

            try {

                URL url = new URL(General.URL_REPORT_CUSTOMER_REGION + "/" + selectedAuditID + "/user/" + General.usercode);
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
                    errormsg = new JSONObject(response).getString("msg");
                    return false;
                }

                if (!response.trim().equals("")) {

                    JSONArray dataArray = new JSONArray(response);

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject jsonObject = (JSONObject) dataArray.get(i);

                        Customer customer = new Customer(i + 1);
                        customer.customerCode = jsonObject.getString("customer_code");
                        customer.customerName = jsonObject.getString("customer");
                        customer.regionCode = jsonObject.getString("region_code");
                        customer.regionName = jsonObject.getString("region");
                        customer.channelCode = jsonObject.getString("channel_code");
                        customer.auditID = jsonObject.getInt("audit_id");
                        customer.mappedStores = jsonObject.getInt("mapped_stores");
                        customer.visitedStores = jsonObject.getInt("visited_stores");
                        customer.template = jsonObject.getString("audit_tempalte");
                        customer.auditName = jsonObject.getString("audit_group");
                        customer.osaAve = jsonObject.getDouble("osa_ave");
                        customer.osa = jsonObject.getDouble("osa");
                        customer.npiAve = jsonObject.getDouble("npi_ave");
                        customer.npi = jsonObject.getDouble("npi");
                        customer.planogram = jsonObject.getDouble("planogram");
                        customer.planogramAve = jsonObject.getDouble("planogram_ave");
                        customer.perfectStores = jsonObject.getInt("perfect_stores");
                        customer.perfectStoresAve = jsonObject.getDouble("ave_perfect_stores");
                        customer.psDoors = jsonObject.getDouble("ps_doors");

                        arrCustomerLoader.add(customer);
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
                Toast.makeText(CustomerRegionReport.this, errormsg, Toast.LENGTH_LONG).show();
                return;
            }

            new FetchCustomerSummarySubReport().execute();
        }
    }

    private class FetchCustomerSummarySubReport extends AsyncTask<Void, Void, Boolean> {

        String errormsg = "";

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(CustomerRegionReport.this, "", "Fetching Customer Summary report. Please wait.");
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            boolean result = false;
            String response = "";

            try {

                for (Customer customer : arrCustomerLoader) {

                    arrCustSummarySubItems.clear();

                    URL url = new URL(General.URL_REPORT_CUSTOMER_REGION
                            + "/" + customer.customerCode
                            + "/region/" + customer.regionCode
                            + "/template/" + customer.channelCode
                            + "/audit/" + selectedAuditID
                            + "/user/" + General.usercode);

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

                    if(response.trim().contains("No reports found.")) {
                        errormsg = new JSONObject(response).getString("msg");
                        return false;
                    }

                    if (!response.trim().equals("")) {
                        JSONArray dataArray = new JSONArray(response);

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject jsonObject = (JSONObject) dataArray.get(i);

                            int id = jsonObject.getInt("id");

                            CustomerStoreItem customerStoreItem = new CustomerStoreItem(id);
                            customerStoreItem.userID = jsonObject.getInt("id");
                            customerStoreItem.auditID = jsonObject.getInt("audit_id");
                            customerStoreItem.account = jsonObject.getString("account");
                            customerStoreItem.area = jsonObject.getString("area");
                            customerStoreItem.distributorCode = jsonObject.getString("distributor_code");
                            customerStoreItem.distributor = jsonObject.getString("distributor");
                            customerStoreItem.storeCode = jsonObject.getString("store_code");
                            customerStoreItem.storeName = jsonObject.getString("store_name");
                            customerStoreItem.channelCode = jsonObject.getString("channel_code");
                            customerStoreItem.perfectStore = jsonObject.getInt("perfect_store");
                            customerStoreItem.osa = jsonObject.getDouble("osa");
                            customerStoreItem.npi = jsonObject.getDouble("npi");
                            customerStoreItem.planogram = jsonObject.getDouble("planogram");
                            customerStoreItem.createdAt = jsonObject.getString("created_at");
                            customerStoreItem.updateAt = jsonObject.getString("updated_at");
                            customerStoreItem.perfectCategory = jsonObject.getInt("perfect_category");
                            customerStoreItem.totalCategory = jsonObject.getInt("total_category");
                            customerStoreItem.perfectPercentage = jsonObject.getInt("perfect_percentage");
                            customerStoreItem.customer = customer;

                            arrCustSummarySubItems.add(customerStoreItem);
                        }

                        customer.customerStoreItems.addAll(arrCustSummarySubItems);
                        result = true;
                    }
                    else errormsg = "Web response error.";
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
                Toast.makeText(CustomerRegionReport.this, errormsg, Toast.LENGTH_LONG).show();
            }

            arrCustomerItems.clear();
            arrCustomerItems.addAll(arrCustomerLoader);
            customersAdapter.notifyDataSetChanged();
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
