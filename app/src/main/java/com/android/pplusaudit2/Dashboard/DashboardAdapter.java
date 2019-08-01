package com.android.pplusaudit2.Dashboard;

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

/**
 * Created by ULTRABOOK on 9/30/2015.
 */
class DashboardAdapter extends BaseAdapter {

    private Context mContext;
    private String[] menuValues;
    private Typeface menufontIcon;

    DashboardAdapter(Context context, String[] menuValues) {
        this.mContext = context;
        this.menuValues = menuValues;
        this.menufontIcon = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont.ttf");
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(context, General.errlogFile));
    }

    private class ViewHolder {
        //ImageView imgMenu;
        TextView tvwMenu;
        TextView tvwMenuDesc;
        TextView tvwImageMenu;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if(convertView == null) {

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            //convertView = inflater.inflate(R.layout.field_main_menu, parent, false);
            convertView = inflater.inflate(R.layout.dashboard_activity_layout_row_list, parent, false);
            holder = new ViewHolder();
            //holder.imgMenu = (ImageView) convertView.findViewById(R.id.imageViewMenu);
            holder.tvwMenu = (TextView) convertView.findViewById(R.id.tvwMenu);
            holder.tvwMenuDesc = (TextView) convertView.findViewById(R.id.tvwMenuDesc);
            holder.tvwImageMenu = (TextView) convertView.findViewById(R.id.tvwImagemenu);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        String strMenu = General.Menu[position].split(":")[0].toUpperCase();
        String strMenuDesc = General.Menu[position].split(":")[1];

        if(General.isAdminMode) {
            strMenu = General.Menu_admin[position].split(":")[0].toUpperCase();
            strMenuDesc = General.Menu_admin[position].split(":")[1];
        }

        //holder.imgMenu.setImageResource(menuValues[position]);
        holder.tvwMenu.setText(strMenu);
        holder.tvwMenuDesc.setText(strMenuDesc);

        holder.tvwImageMenu.setTypeface(menufontIcon);
        holder.tvwImageMenu.setText(menuValues[position]);

        return convertView;
    }

    @Override
    public int getCount() {
        return menuValues.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}
