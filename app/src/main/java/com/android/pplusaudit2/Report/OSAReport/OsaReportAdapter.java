package com.android.pplusaudit2.Report.OSAReport;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.General;
import com.android.pplusaudit2.R;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Lloyd on 8/23/16.
 */

public class OsaReportAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<OsaItem> arrOsaItems;

    public OsaReportAdapter(Context mContext, ArrayList<OsaItem> arrayList) {
        this.mContext = mContext;
        this.arrOsaItems = arrayList;
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(mContext, General.errlogFile));
    }

    private class ViewHolder {
        TextView tvwCustomerName;
        TextView tvwSkuItem;
        TextView tvwStoreCount;
        TextView tvwAvailability;
        TextView tvwOsaValue;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.osa_report_activity_row, parent, false);
            holder = new ViewHolder();

            holder.tvwCustomerName = (TextView) convertView.findViewById(R.id.tvwCustomerName);
            holder.tvwSkuItem = (TextView) convertView.findViewById(R.id.tvwSkuItem);
            holder.tvwStoreCount = (TextView) convertView.findViewById(R.id.tvwStoreCount);
            holder.tvwAvailability = (TextView) convertView.findViewById(R.id.tvwAvailability);
            holder.tvwOsaValue = (TextView) convertView.findViewById(R.id.tvwOsaValue);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvwCustomerName.setText(arrOsaItems.get(position).customerName);
        holder.tvwSkuItem.setText(String.valueOf(arrOsaItems.get(position).prompt));
        holder.tvwStoreCount.setText(String.valueOf(arrOsaItems.get(position).storeCount));
        holder.tvwAvailability.setText(String.valueOf(arrOsaItems.get(position).availability));
        holder.tvwOsaValue.setText(String.format(Locale.getDefault(), "%.2f", arrOsaItems.get(position).osaPercent) + " %");

        return convertView;
    }

    @Override
    public int getCount() {
        return arrOsaItems.size();
    }

    @Override
    public OsaItem getItem(int position) {
        return arrOsaItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return arrOsaItems.get(position).osaItemID;
    }

}
