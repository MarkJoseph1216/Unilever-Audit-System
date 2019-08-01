package com.android.pplusaudit2.Report.AuditSummary;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.General;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ULTRABOOK on 5/10/2016.
 */
public class AuditAdapter extends ArrayAdapter<Audit> {
    Context mContext = null;
    ArrayList<Audit> arrAudits = new ArrayList<>();

    public AuditAdapter(Context context, int resource, ArrayList<Audit> objects) {
        super(context, resource, objects);
        this.mContext = context;
        arrAudits.addAll(objects);
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(context, General.errlogFile));
    }

    public AuditAdapter(Context context, int resource, List<Audit> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.arrAudits.addAll(objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView tvwLabel = new TextView(mContext);
        tvwLabel.setPadding(10, 10, 10, 10);
        tvwLabel.setTextColor(Color.BLACK);
        tvwLabel.setTextSize(14);
        tvwLabel.setText(arrAudits.get(position).auditDesc);

        return tvwLabel;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView label = new TextView(mContext);
        label.setTextColor(Color.BLACK);
        label.setPadding(10, 10, 10, 10);
        label.setTextSize(14);
        label.setText(arrAudits.get(position).auditDesc);

        return label;
    }

    public int getCount(){
        return arrAudits.size();
    }

    public Audit getItem(int position){
        return arrAudits.get(position);
    }

    public long getItemId(int position){
        return arrAudits.get(position).auditID;
    }
}