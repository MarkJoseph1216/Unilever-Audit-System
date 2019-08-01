package com.android.pplusaudit2.Report.SOSReport;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.General;
import com.android.pplusaudit2.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Lloyd on 8/26/16.
 */

public class SosReportAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<SosItem> sosItemArrayList;

    public SosReportAdapter(Context mContext, ArrayList<SosItem> sosItemArrayList) {
        this.mContext = mContext;
        this.sosItemArrayList = sosItemArrayList;
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(mContext, General.errlogFile));
    }

    private class ViewHolder {
        TextView tvwStorename;
        TextView tvwCustomer;
        TextView tvwCategory;
        TextView tvwAuditTemplate;
        TextView tvwTarget;
        TextView tvwSOS;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.sos_report_activity_row, parent, false);
            holder = new ViewHolder();
            holder.tvwStorename = (TextView) convertView.findViewById(R.id.tvwStorename);
            holder.tvwCustomer = (TextView) convertView.findViewById(R.id.tvwCustomer);
            holder.tvwCategory = (TextView) convertView.findViewById(R.id.tvwCategory);
            holder.tvwAuditTemplate = (TextView) convertView.findViewById(R.id.tvwAuditTemplate);
            holder.tvwTarget = (TextView) convertView.findViewById(R.id.tvwTarget);
            holder.tvwSOS = (TextView) convertView.findViewById(R.id.tvwSOS);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvwStorename.setText(sosItemArrayList.get(position).storeName.toUpperCase());
        holder.tvwCustomer.setText(sosItemArrayList.get(position).customerName);
        holder.tvwAuditTemplate.setText(sosItemArrayList.get(position).auditTemplate);
        holder.tvwCategory.setText(sosItemArrayList.get(position).category.toUpperCase());
        holder.tvwTarget.setText(String.format(Locale.getDefault(), "%.2f", sosItemArrayList.get(position).target));
        holder.tvwSOS.setText(String.format(Locale.getDefault(), "%.2f", sosItemArrayList.get(position).psSosMeasurement));

        return convertView;
    }

    @Override
    public int getCount() {
        return sosItemArrayList.size();
    }

    @Override
    public SosItem getItem(int position) {
        return sosItemArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return sosItemArrayList.get(position).sosID;
    }
}
