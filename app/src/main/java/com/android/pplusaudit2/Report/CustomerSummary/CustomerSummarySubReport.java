package com.android.pplusaudit2.Report.CustomerSummary;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.General;
import com.android.pplusaudit2.R;

import java.util.ArrayList;

public class CustomerSummarySubReport extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_summary_subreport_activity);
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(this, General.errlogFile));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        overridePendingTransition(R.anim.slide_up, R.anim.hold);

        ArrayList<CustomerStoreItem> arrStoreSumlist = new ArrayList<>();
        arrStoreSumlist.addAll(General.selectedCustomer.customerStoreItems);

        ListView lvwCustSubReport = (ListView) findViewById(R.id.lvwCustSubReport);
        TextView tvwCustomerName = (TextView) findViewById(R.id.tvwCustomerName);

        CustomerSubAdapter customerSubAdapter = new CustomerSubAdapter(this, arrStoreSumlist);
        lvwCustSubReport.setAdapter(customerSubAdapter);
        tvwCustomerName.setText(General.selectedCustomer.customerName);
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
