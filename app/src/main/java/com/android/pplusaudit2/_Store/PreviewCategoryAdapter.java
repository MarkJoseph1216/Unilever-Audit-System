package com.android.pplusaudit2._Store;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.pplusaudit2.Database.SQLLibrary;
import com.android.pplusaudit2.Database.SQLiteDB;
import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.General;
import com.android.pplusaudit2.R;
import com.android.pplusaudit2.TCRLib;
import com.android.pplusaudit2._Category.Category;

import java.util.ArrayList;

/**
 * Created by ULTRABOOK on 2/18/2016.
 */
class PreviewCategoryAdapter extends BaseAdapter {

    private Context mContext;
    private SQLLibrary sqlLibrary;
    private TCRLib tcrLib;
    private ArrayList<Category> arrCategory;

    PreviewCategoryAdapter(Context ctx, ArrayList<Category> arrayList) {
        this.mContext = ctx;
        this.sqlLibrary = new SQLLibrary(ctx);
        this.tcrLib = new TCRLib(ctx);
        this.arrCategory = arrayList;
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(ctx, General.errlogFile));
    }

    private class ViewHolder {
        TextView tvwPreviewCateg;
        TextView tvwPreviewCategStatus;
        TableLayout tblPreviewGroup;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final ViewHolder holder;

        if(convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.store_preview_layout_row, parent, false);

            holder.tvwPreviewCateg = (TextView) convertView.findViewById(R.id.tvwPreviewCateg);
            holder.tvwPreviewCategStatus = (TextView) convertView.findViewById(R.id.tvwPreviewCategStatus);
            holder.tblPreviewGroup = (TableLayout) convertView.findViewById(R.id.tblPreviewGroup);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvwPreviewCateg.setText(arrCategory.get(position).activityName);
        if(TCRLib.arrPCategoryList.contains(arrCategory.get(position).webCategoryID)) {
            holder.tvwPreviewCateg.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
        }
        else holder.tvwPreviewCateg.setTextColor(mContext.getResources().getColor(R.color.flat_normal_text));

        holder.tvwPreviewCateg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TCRLib.arrPCategoryList.contains(arrCategory.get(position).webCategoryID)) {
                    holder.tvwPreviewCateg.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
                }
                else holder.tvwPreviewCateg.setTextColor(mContext.getResources().getColor(R.color.flat_normal_text));
            }
        });

        String status = "";
        switch (arrCategory.get(position).categoryScoreStatus) {
            case PASSED:
                status = General.SCORE_STATUS_PASSED;
                holder.tvwPreviewCategStatus.setTextColor(mContext.getResources().getColor(R.color.green));
                break;
            case FAILED:
                holder.tvwPreviewCategStatus.setTextColor(mContext.getResources().getColor(R.color.red));
                status = General.SCORE_STATUS_FAILED;
                break;
            default:
                break;
        }

        holder.tvwPreviewCategStatus.setText(status);

        // STORE CATEGORY GROUP
        Cursor cursStoreCategoryGroups = sqlLibrary.RawQuerySelect("SELECT tblstorecateggroup.id, tblgroup.groupdesc, " + SQLiteDB.TABLE_GROUP + "." + SQLiteDB.COLUMN_GROUP_groupid + ", " + SQLiteDB.TABLE_STORECATEGORYGROUP + "." + SQLiteDB.COLUMN_STORECATEGORYGROUP_final
                + "," + SQLiteDB.COLUMN_STORECATEGORYGROUP_status  + "," + SQLiteDB.TABLE_STORECATEGORYGROUP + "." + SQLiteDB.COLUMN_STORECATEGORYGROUP_exempt
                + "," + SQLiteDB.TABLE_STORECATEGORYGROUP + "." + SQLiteDB.COLUMN_STORECATEGORYGROUP_initial
                + " FROM " + SQLiteDB.TABLE_STORECATEGORYGROUP
                + " JOIN " + SQLiteDB.TABLE_GROUP + " ON " + SQLiteDB.TABLE_GROUP + "." + SQLiteDB.COLUMN_GROUP_id + " = " + SQLiteDB.TABLE_STORECATEGORYGROUP + "." + SQLiteDB.COLUMN_STORECATEGORYGROUP_groupid
                + " WHERE " + SQLiteDB.TABLE_STORECATEGORYGROUP + "." + SQLiteDB.COLUMN_STORECATEGORYGROUP_storecategid + " = " + arrCategory.get(position).categoryAndTempid
                + " ORDER BY " + SQLiteDB.COLUMN_GROUP_grouporder);
        cursStoreCategoryGroups.moveToFirst();

        holder.tblPreviewGroup.removeAllViews();

        if(cursStoreCategoryGroups.getCount() > 0) {

            while (!cursStoreCategoryGroups.isAfterLast()) {
                TableRow row = (TableRow) LayoutInflater.from(mContext).inflate(R.layout.store_preview_layout_subrow, null);

                TextView tvwGrouppreview = (TextView) row.findViewById(R.id.tvwGrouppreview);
                TextView tvwImgStatus = (TextView) row.findViewById(R.id.tvwImgStatus);
                TextView tvwScore = (TextView) row.findViewById(R.id.tvwScore);

                String groupDesc = cursStoreCategoryGroups.getString(cursStoreCategoryGroups.getColumnIndex(SQLiteDB.COLUMN_GROUP_groupdesc));
                int groupId = cursStoreCategoryGroups.getInt(cursStoreCategoryGroups.getColumnIndex(SQLiteDB.COLUMN_GROUP_groupid));
                String grpScoreno = cursStoreCategoryGroups.getString(cursStoreCategoryGroups.getColumnIndex(SQLiteDB.COLUMN_STORECATEGORYGROUP_final));
                int storeCategroupID = cursStoreCategoryGroups.getInt(cursStoreCategoryGroups.getColumnIndex(SQLiteDB.COLUMN_STORECATEGORYGROUP_id));

                if(!sqlLibrary.HasQuestionsPerGroup(storeCategroupID)) {
                    cursStoreCategoryGroups.moveToNext();
                    continue;
                }

                if(TCRLib.arrPGroupList.contains(groupId)) {
                    tvwGrouppreview.setTextColor(mContext.getResources().getColor(R.color.colorPrimary_pressed));
                }

                General.SCORE_STATUS grpScoreStatus = tcrLib.GetScoreStatus(grpScoreno);

                tvwGrouppreview.setText(groupDesc.toUpperCase());

                String groupStatus = "";
                switch (grpScoreStatus) {
                    case PASSED:
                        groupStatus = General.SCORE_STATUS_PASSED;
                        tvwImgStatus.setTextColor(mContext.getResources().getColor(R.color.green));
                        tvwScore.setTextColor(mContext.getResources().getColor(R.color.green));
                        break;
                    case FAILED:
                        tvwImgStatus.setTextColor(mContext.getResources().getColor(R.color.red));
                        tvwScore.setTextColor(mContext.getResources().getColor(R.color.red));
                        groupStatus = General.SCORE_STATUS_FAILED;
                        break;
                    default:
                        break;
                }

                // GET SCORE IF OSA
                tvwScore.setText("");
                Cursor cursOsalist = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_OSALIST, SQLiteDB.COLUMN_OSALIST_osakeygroupid + " = '" + groupId + "'");
                cursOsalist.moveToFirst();
                if(cursOsalist.getCount() > 0) {
                    int nCorrectOsaAns = sqlLibrary.GetCorrectAnswersComp(String.valueOf(storeCategroupID));
                    int nTotalQuestionOSA = sqlLibrary.GetTotalQuestionsComputation(String.valueOf(storeCategroupID));

                    if(nCorrectOsaAns > 0 || grpScoreno.trim().equals("1")) {
                        String strOsaScore = String.valueOf(nCorrectOsaAns) + " / " + String.valueOf(nTotalQuestionOSA);
                        tvwScore.setText(strOsaScore);
                    }
                }
                cursOsalist.close();

                // NPI
                Cursor cursNpi = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_NPI, SQLiteDB.COLUMN_NPI_keygroupid + " = '" + groupId + "'");
                cursNpi.moveToFirst();
                if (cursNpi.getCount() > 0) {

                    int nTotalCorrectNPI = sqlLibrary.GetCorrectAnswersComp(String.valueOf(storeCategroupID));
                    int nTotalQuestionsNPI = sqlLibrary.GetTotalQuestionsComputation(String.valueOf(storeCategroupID));

                    if(nTotalCorrectNPI > 0 || grpScoreno.trim().equals("1")) {
                        String strOsaScore = String.valueOf(nTotalCorrectNPI) + " / " + String.valueOf(nTotalQuestionsNPI);
                        tvwScore.setText(strOsaScore);
                    }
                }
                cursNpi.close();

                // PLANOGRAM
                Cursor cursPlanogram = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_PLANOGRAM, SQLiteDB.COLUMN_PLANOGRAM_keygroupid + " = '" + groupId + "'");
                cursPlanogram.moveToFirst();
                if (cursPlanogram.getCount() > 0) {

                    int nTotalCorrectPlano = sqlLibrary.GetCorrectAnswersComp(String.valueOf(storeCategroupID));
                    int nTotalQuestionsPlano = sqlLibrary.GetTotalQuestionsComputation(String.valueOf(storeCategroupID));

                    if(nTotalCorrectPlano > 0 || grpScoreno.trim().equals("1")) {
                        String strOsaScore = String.valueOf(nTotalCorrectPlano) + " / " + String.valueOf(nTotalQuestionsPlano);
                        tvwScore.setText(strOsaScore);
                    }
                }
                cursPlanogram.close();

                tvwImgStatus.setText(groupStatus);
                cursStoreCategoryGroups.moveToNext();
                holder.tblPreviewGroup.addView(row);
            }
        }

        cursStoreCategoryGroups.close();

        return convertView;
    }

    @Override
    public int getCount() {
        return arrCategory.size();
    }

    @Override
    public Category getItem(int position) {
        return arrCategory.get(position);
    }

    @Override
    public long getItemId(int position) {
        return arrCategory.get(position).activityID;
    }
}
