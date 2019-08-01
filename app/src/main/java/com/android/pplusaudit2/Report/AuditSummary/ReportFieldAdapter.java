package com.android.pplusaudit2.Report.AuditSummary;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.pplusaudit2.R;

import java.util.ArrayList;

/**
 * Created by ULTRABOOK on 6/1/2016.
 */
public class ReportFieldAdapter extends BaseAdapter {
    Context mContext;
    ArrayList<ReportField> reportFieldArrayList;

    public ReportFieldAdapter(Context mContext, ArrayList<ReportField> reportFieldArrayList) {
        this.mContext = mContext;
        this.reportFieldArrayList = reportFieldArrayList;
    }

    public class ViewHolder {
        TextView tvwTitle;
        TextView tvwDesc;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.report_audit_activity_row, parent, false);
            holder = new ViewHolder();

            holder.tvwTitle = (TextView) convertView.findViewById(R.id.tvwRowItem);
            holder.tvwDesc = (TextView) convertView.findViewById(R.id.tvwRowValue);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvwTitle.setText(reportFieldArrayList.get(position).fieldTitle.toUpperCase());
        holder.tvwDesc.setText(reportFieldArrayList.get(position).fieldDesc);

        return convertView;
    }

    @Override
    public int getCount() {
        return reportFieldArrayList.size();
    }

    @Override
    public ReportField getItem(int position) {
        return reportFieldArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return reportFieldArrayList.get(position).fieldID;
    }
}
