package com.android.pplusaudit2.PJP_Compliance;

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

import java.util.Collections;
import java.util.List;

/**
 * Created by ULTRABOOK on 6/7/2016.
 */
class PjpPreviewAdapter extends BaseAdapter {

    private Context mContext;
    private List<Compliance> complianceList = Collections.EMPTY_LIST;
    private Typeface menuFontIcon;

    PjpPreviewAdapter(Context mContext, List<Compliance> complianceList) {
        this.mContext = mContext;
        this.complianceList = complianceList;
        this.menuFontIcon = Typeface.createFromAsset(mContext.getAssets(), General.typefacename);
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(mContext, General.errlogFile));

    }

    private class ViewHolder {
        TextView tvwDatetime;
        TextView tvwLocation;
        TextView tvwUser;
        TextView tvwStatus;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final ViewHolder holder;

        if(convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.pjppreview_activity_layout_row, parent, false);

            holder.tvwDatetime = (TextView) convertView.findViewById(R.id.tvwPjpDatetime);
            holder.tvwUser = (TextView) convertView.findViewById(R.id.tvwPjpUsername);
            holder.tvwLocation = (TextView) convertView.findViewById(R.id.tvwPjpLocation);
            holder.tvwStatus = (TextView) convertView.findViewById(R.id.tvwPjpStatus);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        String strDatetime = complianceList.get(position).date + " " + complianceList.get(position).time;

        holder.tvwDatetime.setText(strDatetime);
        holder.tvwUser.setText(complianceList.get(position).username);
        holder.tvwLocation.setText(complianceList.get(position).address);
        holder.tvwStatus.setTypeface(menuFontIcon);

        if(complianceList.get(position).isPosted) {
            holder.tvwStatus.setText("\uF00C");
            holder.tvwStatus.setTextColor(mContext.getResources().getColor(R.color.green));
        }
        else
            holder.tvwStatus.setText("");



        return convertView;
    }



    @Override
    public int getCount() {
        return complianceList.size();
    }

    @Override
    public Compliance getItem(int position) {
        return complianceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return complianceList.get(position).complianceID;
    }
}
