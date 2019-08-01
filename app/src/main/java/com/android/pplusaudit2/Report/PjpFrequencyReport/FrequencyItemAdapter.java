package com.android.pplusaudit2.Report.PjpFrequencyReport;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.pplusaudit2.R;

import java.util.ArrayList;

/**
 * Created by Lloyd on 9/1/16.
 */

public class FrequencyItemAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<FrequencyItem> arrFrequencies = new ArrayList<>();

    public FrequencyItemAdapter(Context mContext, ArrayList<FrequencyItem> arrFrequencies) {
        this.mContext = mContext;
        this.arrFrequencies = arrFrequencies;
    }

    private class ViewHolder {
        TextView tvwStoreName;
        TextView tvwFrequency;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if(convertView == null) {

            holder = new ViewHolder();

            LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.pjp_frequency_activity_row, parent, false);

            holder.tvwStoreName = (TextView) convertView.findViewById(R.id.tvwStoreName);
            holder.tvwFrequency = (TextView) convertView.findViewById(R.id.tvwFrequency);

            convertView.setTag(holder);

        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvwStoreName.setText(arrFrequencies.get(position).storeName.toUpperCase());
        holder.tvwFrequency.setText(String.valueOf(arrFrequencies.get(position).frequency));

        return convertView;
    }

    @Override
    public FrequencyItem getItem(int position) {
        return arrFrequencies.get(position);
    }

    @Override
    public int getCount() {
        return arrFrequencies.size();
    }

    @Override
    public long getItemId(int position) {
        return arrFrequencies.get(position).frequencyID;
    }
}
