package com.android.pplusaudit2.Report.CustomerRegionSummary;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.android.pplusaudit2.General;
import com.android.pplusaudit2.R;
import com.android.pplusaudit2.Report.CustomerSummary.CustomerStoreItem;
import com.android.pplusaudit2.Report.CustomerSummary.CustomerSubAdapter;

import java.util.ArrayList;

public class CustomerRegionSubReport extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_region_subreport_activity);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        overridePendingTransition(R.anim.slide_up, R.anim.hold);

        ArrayList<CustomerStoreItem> arrStoreSumlist = new ArrayList<>();
        arrStoreSumlist.addAll(General.selectedCustomer.customerStoreItems);

        ListView lvwCustRegionSub = (ListView) findViewById(R.id.lvwCustRegionSub);
        TextView tvwRegionName = (TextView) findViewById(R.id.tvwRegionName);
        TextView tvwAuditMonth = (TextView) findViewById(R.id.tvwAuditMonth);

        CustomerRegionSubAdapter customerSubAdapter = new CustomerRegionSubAdapter(this, arrStoreSumlist);
        lvwCustRegionSub.setAdapter(customerSubAdapter);

        tvwRegionName.setText(General.selectedCustomer.regionName);
        tvwAuditMonth.setText(General.selectedCustomer.auditName);
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
