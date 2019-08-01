package com.android.pplusaudit2.Report.CustomerSummary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.General;
import com.android.pplusaudit2.R;
import com.android.pplusaudit2.Report.StoreSummary.ReportStoreAdapter;
import com.android.pplusaudit2.Report.StoreSummary.StoreItem;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Lloyd on 8/22/16.
 */

public class CustomerAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Customer> arrCustomers;

    public CustomerAdapter(Context mContext, ArrayList<Customer> arrCustomerItems) {
        this.mContext = mContext;
        this.arrCustomers = arrCustomerItems;
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(mContext, General.errlogFile));
    }

    private class ViewHolder {
        TextView tvwCustomerName;
        TextView tvwAuditName;
        TextView tvwPerfectStore;
        TextView tvwOSA;
        TextView tvwNPI;
        TextView tvwPlanogram;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.customer_summary_report_activity_row, parent, false);
            holder = new ViewHolder();

            holder.tvwCustomerName = (TextView) convertView.findViewById(R.id.tvwCustomerName);
            holder.tvwAuditName = (TextView) convertView.findViewById(R.id.tvwAuditName);
            holder.tvwPerfectStore = (TextView) convertView.findViewById(R.id.tvwPerfectStore);
            holder.tvwOSA = (TextView) convertView.findViewById(R.id.tvwOSA);
            holder.tvwNPI = (TextView) convertView.findViewById(R.id.tvwNPI);
            holder.tvwPlanogram = (TextView) convertView.findViewById(R.id.tvwPlanogram);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvwCustomerName.setText(arrCustomers.get(position).customerName);
        holder.tvwAuditName.setText(arrCustomers.get(position).auditName);
        holder.tvwPerfectStore.setText(String.valueOf(arrCustomers.get(position).perfectStores));
        holder.tvwOSA.setText(String.format(Locale.getDefault(), "%.2f", arrCustomers.get(position).osaAve));
        holder.tvwNPI.setText(String.format(Locale.getDefault(), "%.2f", arrCustomers.get(position).npiAve));
        holder.tvwPlanogram.setText(String.format(Locale.getDefault(), "%.2f", arrCustomers.get(position).planogramAve));

        return convertView;
    }

    @Override
    public int getCount() {
        return arrCustomers.size();
    }

    @Override
    public Customer getItem(int position) {
        return arrCustomers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return arrCustomers.get(position).customerID;
    }
}
