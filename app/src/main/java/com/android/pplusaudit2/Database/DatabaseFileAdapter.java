package com.android.pplusaudit2.Database;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.pplusaudit2.General;
import com.android.pplusaudit2.R;

import java.util.ArrayList;

/**
 * Created by Lloyd on 7/20/16.
 */

public class DatabaseFileAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<DatabaseFile> databaseFileArrayList;
    private Typeface menuFontIcon;

    public DatabaseFileAdapter(Context mContext, ArrayList<DatabaseFile> databaseFileArrayList) {
        this.mContext = mContext;
        this.databaseFileArrayList = databaseFileArrayList;
        this.menuFontIcon = Typeface.createFromAsset(mContext.getAssets(), General.typefacename);
    }

    private class ViewHolder {
        TextView tvwUpdated;
        TextView tvwName;
        TextView tvwCreated;
        TextView tvwDbID;
        TextView tvwDatabaseVersion;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final ViewHolder holder;

        if(convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.database_row_layout, parent, false);

            holder.tvwUpdated = (TextView) convertView.findViewById(R.id.tvwUpdatedAt);
            holder.tvwName = (TextView) convertView.findViewById(R.id.tvwName);
            holder.tvwCreated = (TextView) convertView.findViewById(R.id.tvwCreated);
            holder.tvwDbID = (TextView) convertView.findViewById(R.id.tvwId);
            holder.tvwDatabaseVersion = (TextView) convertView.findViewById(R.id.tvwDatabaseVersion);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvwUpdated.setTypeface(menuFontIcon);
        holder.tvwUpdated.setText("\uf00c  UPDATED AT: " + databaseFileArrayList.get(position).updatedAt.toUpperCase());
        holder.tvwName.setText("Name: " + databaseFileArrayList.get(position).fileName);
        holder.tvwCreated.setText("Imported At: " + databaseFileArrayList.get(position).createdAt);
        holder.tvwDbID.setText("ID: " + String.valueOf(databaseFileArrayList.get(position).ID));
        holder.tvwDatabaseVersion.setText("DB Version: " + String.valueOf(databaseFileArrayList.get(position).databaseVersion));

        return convertView;
    }

    @Override
    public int getCount() {
        return databaseFileArrayList.size();
    }

    @Override
    public DatabaseFile getItem(int position) {
        return databaseFileArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return databaseFileArrayList.get(position).ID;
    }
}
