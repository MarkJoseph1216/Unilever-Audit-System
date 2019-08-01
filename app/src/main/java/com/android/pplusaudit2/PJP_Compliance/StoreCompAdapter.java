package com.android.pplusaudit2.PJP_Compliance;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.pplusaudit2.Database.SQLLibrary;
import com.android.pplusaudit2.Database.SQLiteDB;
import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.General;
import com.android.pplusaudit2.R;
import com.android.pplusaudit2._Store.Stores;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ULTRABOOK on 6/7/2016.
 */
class StoreCompAdapter extends BaseAdapter {

    private Context mContext;
    private SQLLibrary sql;
    private List<Stores> storesArrayList = Collections.EMPTY_LIST;
    private ArrayList<Compliance> arrCompliance;
    private Typeface menuFontIcon;

    StoreCompAdapter(Context mContext, List<Stores> storesArrayList, ArrayList<Compliance> aComp) {
        this.mContext = mContext;
        this.storesArrayList = storesArrayList;
        this.sql = new SQLLibrary(mContext);
        this.menuFontIcon = Typeface.createFromAsset(mContext.getAssets(), General.typefacename);
        this.arrCompliance = new ArrayList<>();
        this.arrCompliance.addAll(aComp);

        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(mContext, General.errlogFile));
    }

    private class PjpViewHolder {
        TextView tvwTitle;
        TextView tvwDetails;
        TextView tvwSubDetails;
        Button btnPreview;
        Button btnCheckin;
        RelativeLayout relMainPjp;
    }

        @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final PjpViewHolder holder;

            if(convertView == null) {
                holder = new PjpViewHolder();
                convertView = inflater.inflate(R.layout.pjp_activity_layout_row, parent, false);

                holder.tvwTitle = (TextView) convertView.findViewById(R.id.tvwPjpStoreName);
                holder.tvwDetails = (TextView) convertView.findViewById(R.id.tvwPjpDetails);
                holder.btnPreview = (Button) convertView.findViewById(R.id.btnPreviewPjp);
                holder.btnCheckin = (Button) convertView.findViewById(R.id.btnCheckInPjp);
                holder.relMainPjp = (RelativeLayout) convertView.findViewById(R.id.relMainPjp);
                holder.tvwSubDetails = (TextView) convertView.findViewById(R.id.tvwPjpSubDetails);

                convertView.setTag(holder);
            }
            else {
                holder = (PjpViewHolder) convertView.getTag();
            }
            holder.tvwTitle.setText(storesArrayList.get(position).storeName);

            String checkMsg = "";
            String strAddress = "";

            holder.tvwDetails.setTypeface(menuFontIcon);
            holder.tvwSubDetails.setTypeface(menuFontIcon);

            if(storesArrayList.get(position).isChecked) {
                checkMsg = "\uf00c" + " Checked in: " + storesArrayList.get(position).dateCheckedIn + " " + storesArrayList.get(position).timeChecked;
                strAddress = "\uf041" + "   Location: " + storesArrayList.get(position).addressChecked;
                holder.relMainPjp.setBackgroundColor(mContext.getResources().getColor(R.color.color_highlight));
            }
            else {
                holder.relMainPjp.setBackgroundColor(mContext.getResources().getColor(R.color.white));
            }

            holder.tvwDetails.setText(checkMsg);
            holder.tvwSubDetails.setText(strAddress);

            holder.btnCheckin.setTag(storesArrayList.get(position));
            holder.btnPreview.setTag(storesArrayList.get(position));

            holder.btnPreview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    General.selectedStore = (Stores) v.getTag();

                    Cursor cursChecked = sql.GetDataCursor(SQLiteDB.TABLE_PJPCOMP, SQLiteDB.COLUMN_PJPCOMP_storeid + " = '" + General.selectedStore.storeID + "'");
                    if(!cursChecked.moveToFirst()) {
                        Toast.makeText(mContext, "No records found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Intent intentPreview = new Intent(mContext, PjpPreviewActivity.class);
                    mContext.startActivity(intentPreview);
                }
            });

            return convertView;
    }



    @Override
    public int getCount() {
        return storesArrayList.size();
    }

    @Override
    public Stores getItem(int position) {
        return storesArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return storesArrayList.get(position).storeID;
    }
}
