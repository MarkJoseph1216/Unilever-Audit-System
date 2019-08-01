package com.android.pplusaudit2.Report.CustomerRegionSummary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.General;
import com.android.pplusaudit2.R;
import com.android.pplusaudit2.Report.CustomerSummary.CustomerStoreItem;
import com.android.pplusaudit2.Report.CustomerSummary.CustomerSubAdapter;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Lloyd on 8/23/16.
 */

public class CustomerRegionSubAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<CustomerStoreItem> arrCustomerStores;

    public CustomerRegionSubAdapter(Context mContext, ArrayList<CustomerStoreItem> arrCustomerStoreItems) {
        this.mContext = mContext;
        this.arrCustomerStores = arrCustomerStoreItems;
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(mContext, General.errlogFile));
    }

    private class ViewHolder {
        TextView tvwStoreName;
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
            convertView = inflater.inflate(R.layout.customer_region_subreport_activity_row, parent, false);
            holder = new ViewHolder();

            holder.tvwStoreName = (TextView) convertView.findViewById(R.id.tvwStoreName);
            holder.tvwPerfectStore = (TextView) convertView.findViewById(R.id.tvwPerfectStore);
            holder.tvwOSA = (TextView) convertView.findViewById(R.id.tvwOSA);
            holder.tvwNPI = (TextView) convertView.findViewById(R.id.tvwNPI);
            holder.tvwPlanogram = (TextView) convertView.findViewById(R.id.tvwPlanogram);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvwStoreName.setText(arrCustomerStores.get(position).storeName);
        holder.tvwPerfectStore.setText(String.valueOf(arrCustomerStores.get(position).perfectStore));
        holder.tvwOSA.setText(String.format(Locale.getDefault(), "%.2f", arrCustomerStores.get(position).osa));
        holder.tvwNPI.setText(String.format(Locale.getDefault(), "%.2f", arrCustomerStores.get(position).npi));
        holder.tvwPlanogram.setText(String.format(Locale.getDefault(), "%.2f", arrCustomerStores.get(position).planogram));

        return convertView;
    }

    @Override
    public int getCount() {
        return arrCustomerStores.size();
    }

    @Override
    public CustomerStoreItem getItem(int position) {
        return arrCustomerStores.get(position);
    }

    @Override
    public long getItemId(int position) {
        return arrCustomerStores.get(position).CustomerStoreItemID;
    }

}
