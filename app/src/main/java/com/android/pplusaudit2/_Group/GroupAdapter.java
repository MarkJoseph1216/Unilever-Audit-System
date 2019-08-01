package com.android.pplusaudit2._Group;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.General;
import com.android.pplusaudit2.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ULTRABOOK on 9/22/2015.
 */
class GroupAdapter extends BaseAdapter {

    private Context mContext;
    private List<Group> lstGroup = null;
    private ArrayList<Group> arrGrouplist;
    private Typeface menufontIcon;

    GroupAdapter(Context ctx, ArrayList<Group> arrList)
    {
        this.mContext = ctx;
        lstGroup = arrList;
        arrGrouplist = new ArrayList<Group>();
        arrGrouplist.addAll(arrList);
        this.menufontIcon = Typeface.createFromAsset(mContext.getAssets(), General.typefacename);
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(ctx, General.errlogFile));
    }

    public class ViewHolder {
        TextView tvwGroup;
        TextView tvwGroupStatus;
        TextView tvwGroupScoreStatus;
        TextView tvwGroupIconStatus;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final ViewHolder holder;

        Toast.makeText(mContext, "TEST", Toast.LENGTH_SHORT).show();

        if(view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.group_activity_layout_row, parent, false);

            holder.tvwGroup = (TextView) view.findViewById(R.id.tvwGroup);
            holder.tvwGroupStatus = (TextView) view.findViewById(R.id.tvwGroupStatus);
            holder.tvwGroupScoreStatus = (TextView) view.findViewById(R.id.tvwGroupScoreStatus);
            holder.tvwGroupIconStatus = (TextView) view.findViewById(R.id.tvwIconStatusGroup);

            view.setTag(holder);
        }
        else {
            holder = (ViewHolder) view.getTag();
        }

        String groupStatus = "";
        try {
            groupStatus = lstGroup.get(position).groupQuestionsStatus.trim().toUpperCase();
        }
        catch (Exception ex) { }
        String groupScoreStatus = "";

        holder.tvwGroup.setText(String.valueOf(lstGroup.get(position).Groupdesc));
        holder.tvwGroupStatus.setText(groupStatus);
        holder.tvwGroupIconStatus.setTypeface(menufontIcon);

        if(groupStatus.equals(General.STATUS_PENDING)) {
            holder.tvwGroupStatus.setTextColor(view.getResources().getColor(R.color.red));
            holder.tvwGroupIconStatus.setTextColor(view.getResources().getColor(R.color.gray));
            holder.tvwGroupIconStatus.setText(General.ICON_STAR_PENDING);
        }
        else if (groupStatus.equals(General.STATUS_PARTIAL)) {
            holder.tvwGroupStatus.setTextColor(view.getResources().getColor(R.color.colorAccentDark));
            holder.tvwGroupIconStatus.setTextColor(view.getResources().getColor(R.color.yellow));
            holder.tvwGroupIconStatus.setText(General.ICON_STAR_PARTIAL);
        }
        else {
            holder.tvwGroupStatus.setTextColor(view.getResources().getColor(R.color.green));
            holder.tvwGroupIconStatus.setTextColor(view.getResources().getColor(R.color.green));
            holder.tvwGroupIconStatus.setText(General.ICON_STAR_COMPLETE);
        }

        switch (lstGroup.get(position).groupScoreStatus) {
            case PASSED:
                groupScoreStatus = General.ICON_PASSED + " " + General.SCORE_STATUS_PASSED;
                holder.tvwGroupScoreStatus.setTextColor(view.getResources().getColor(R.color.green));
                break;
            case FAILED:
                groupScoreStatus = General.ICON_FAILED + " " + General.SCORE_STATUS_FAILED;
                holder.tvwGroupScoreStatus.setTextColor(view.getResources().getColor(R.color.red));
                break;
            default:
                break;
        }

        holder.tvwGroupScoreStatus.setTypeface(menufontIcon);
        holder.tvwGroupScoreStatus.setText(groupScoreStatus);

        notifyDataSetChanged();
        holder.tvwGroup.setTag(lstGroup.get(position).audittempid_categid_grpid);
        return view;

    }

    @Override
    public int getCount() {
        return lstGroup.size();
    }

    @Override
    public Object getItem(int position) {
        return lstGroup.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
