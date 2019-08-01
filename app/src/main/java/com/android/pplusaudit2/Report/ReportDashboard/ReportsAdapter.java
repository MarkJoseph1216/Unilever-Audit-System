package com.android.pplusaudit2.Report.ReportDashboard;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.General;
import com.android.pplusaudit2.R;

import java.util.ArrayList;

/**
 * Created by ULTRABOOK on 5/10/2016.
 */
public class ReportsAdapter extends BaseAdapter {

    Context mContext;
    ArrayList<Reports> arrReports;
    private Typeface menufontIcon;

    public ReportsAdapter(Context mContext, ArrayList<Reports> arrReports) {
        this.mContext = mContext;
        this.arrReports = arrReports;
        this.menufontIcon = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont.ttf");
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(mContext, General.errlogFile));
    }

    public class ViewHolder {
        TextView tvwReportName;
        TextView tvwReportDesc;
        TextView tvwReportIcon;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.report_item_rows, parent, false);
            holder = new ViewHolder();

            holder.tvwReportName = (TextView) convertView.findViewById(R.id.tvwReportName);
            holder.tvwReportDesc = (TextView) convertView.findViewById(R.id.tvwReportDesc);
            holder.tvwReportIcon = (TextView) convertView.findViewById(R.id.tvwReportIcon);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvwReportName.setText(arrReports.get(position).reportName.toUpperCase());
        holder.tvwReportDesc.setText(arrReports.get(position).reportDesc);
        holder.tvwReportIcon.setTypeface(menufontIcon);
        holder.tvwReportIcon.setText(arrReports.get(position).reportIcon);


        return convertView;
    }

    @Override
    public Reports getItem(int position) {
        return arrReports.get(position);
    }

    @Override
    public int getCount() {
        return arrReports.size();
    }

    @Override
    public long getItemId(int position) {
        return arrReports.get(position).reportID;
    }
}
