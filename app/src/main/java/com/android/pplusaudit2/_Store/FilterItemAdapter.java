package com.android.pplusaudit2._Store;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.pplusaudit2.R;

import java.util.ArrayList;

/**
 * Created by Lloyd on 7/20/16.
 */

public class FilterItemAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<FilterItem> itemArrayList;

    public FilterItemAdapter(Context mContext, ArrayList<FilterItem> itemArrayList) {
        this.mContext = mContext;
        this.itemArrayList = itemArrayList;
    }

    private class ViewHolder {
        TextView tvwFilterDesc;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final ViewHolder holder;

        if(convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.layout_filter_row, parent, false);
            holder.tvwFilterDesc = (TextView) convertView.findViewById(R.id.tvwFilterDesc);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvwFilterDesc.setText(itemArrayList.get(position).filterItemDesc);

        return convertView;
    }

    @Override
    public int getCount() {
        return itemArrayList.size();
    }

    @Override
    public FilterItem getItem(int position) {
        return itemArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return itemArrayList.get(position).filterItemID;
    }
}
