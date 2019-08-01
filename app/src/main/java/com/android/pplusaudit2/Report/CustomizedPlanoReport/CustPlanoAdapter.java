package com.android.pplusaudit2.Report.CustomizedPlanoReport;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.pplusaudit2.R;
import com.android.pplusaudit2.Report.OSAReport.OsaItem;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Lloyd on 8/30/16.
 */

public class CustPlanoAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<OsaItem> osaItemArrayList;

    public CustPlanoAdapter(Context mContext, ArrayList<OsaItem> osaItemArrayList) {
        this.mContext = mContext;
        this.osaItemArrayList = osaItemArrayList;
    }

    private class ViewHolder {
        TextView tvwCustomerName;
        TextView tvwAuditTemplate;
        TextView tvwCategory;
        TextView tvwPrompt;
        TextView tvwStoreCount;
        TextView tvwStoreCompliance;
        TextView tvwComplianceRate;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if(convertView == null) {

            holder = new ViewHolder();

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.customized_plano_activity_row, parent, false);

            holder.tvwCustomerName = (TextView) convertView.findViewById(R.id.tvwCustomerName);
            holder.tvwAuditTemplate = (TextView) convertView.findViewById(R.id.tvwAuditTemplate);
            holder.tvwCategory = (TextView) convertView.findViewById(R.id.tvwCategory);
            holder.tvwPrompt = (TextView) convertView.findViewById(R.id.tvwPrompt);
            holder.tvwStoreCount = (TextView) convertView.findViewById(R.id.tvwStoreCount);
            holder.tvwStoreCompliance = (TextView) convertView.findViewById(R.id.tvwStoreCompliance);
            holder.tvwComplianceRate = (TextView) convertView.findViewById(R.id.tvwComplianceRate);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvwCustomerName.setText(osaItemArrayList.get(position).customerName.toUpperCase());
        holder.tvwAuditTemplate.setText(osaItemArrayList.get(position).template.toUpperCase());
        holder.tvwCategory.setText(osaItemArrayList.get(position).category.toUpperCase());
        holder.tvwPrompt.setText(osaItemArrayList.get(position).prompt.toUpperCase());
        holder.tvwStoreCount.setText(String.valueOf(osaItemArrayList.get(position).storeCount));
        holder.tvwStoreCompliance.setText(String.valueOf(osaItemArrayList.get(position).availability));
        holder.tvwComplianceRate.setText(String.format(Locale.getDefault(), "%.2f", osaItemArrayList.get(position).osaPercent));

        return convertView;
    }

    @Override
    public int getCount() {
        return osaItemArrayList.size();
    }

    @Override
    public OsaItem getItem(int position) {
        return osaItemArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return osaItemArrayList.get(position).osaItemID;
    }
}
