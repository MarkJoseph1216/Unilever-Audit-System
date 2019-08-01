package com.android.pplusaudit2._Store;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.pplusaudit2.Database.SQLLibrary;
import com.android.pplusaudit2.Database.SQLiteDB;
import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.ErrorLogs.ErrorLog;
import com.android.pplusaudit2.General;
import com.android.pplusaudit2.PJP_Compliance.Compliance;
import com.android.pplusaudit2.R;
import com.android.pplusaudit2.TCRLib;
import com.android.pplusaudit2._Category.CategoryActivity;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by ULTRABOOK on 9/22/2015.
 */
public class StoreActivity extends AppCompatActivity {

    private ArrayList<Stores> arrStoreList = new ArrayList<Stores>();
    private ArrayList<Stores> lstStores = new ArrayList<Stores>();

    private ArrayList<FilterItem> arrFilterItems;

    private SQLLibrary sql;
    private SQLiteDB sqLiteDB;

    private PowerManager.WakeLock wlStayAwake;

    private ProgressDialog progressDL;
    private ListView lvwStore;
    private StoreAdapter adapter;
    private String TAG;
    private ErrorLog errorLog;
    private FilterItemAdapter adapterFilterItem;
    private String strSelectedFilterArea;
    private ArrayList<Compliance> arrCompliances;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.store_activity_layout);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(this, General.errlogFile));
        overridePendingTransition(R.anim.slide_in_left, R.anim.hold);

        errorLog = new ErrorLog(General.errlogFile, this);
        TAG = StoreActivity.this.getLocalClassName();
        arrFilterItems = new ArrayList<>();

        getSupportActionBar().setTitle("STORES");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        PowerManager powerman = (PowerManager) getSystemService(POWER_SERVICE);
        wlStayAwake = powerman.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "wakelocktag");
        wlStayAwake.acquire();
        sql = new SQLLibrary(this);
        sqLiteDB = new SQLiteDB(this);
        arrCompliances = new ArrayList<>();

        GetPerfectList();

        lvwStore = (ListView) findViewById(R.id.lvwStore);
    }

    @Override
    protected void onDestroy() {
        wlStayAwake.release();
        super.onDestroy();
    }

    private class LoadFilter extends AsyncTask<Void, Void, Boolean> {

        private String errMessage = "";
        private String strQuery = "";
        private String strFieldName = "";
        private String strFilterTitle = "";
        private int nFilterCode = 0;
        private ArrayList<FilterItem> arrayList = new ArrayList<>();

        LoadFilter(int filterCode, String strQuery, String strFieldName, String strOtherName) {
            this.strQuery = strQuery;
            this.strFieldName = strFieldName;
            this.nFilterCode = filterCode;
            this.strFilterTitle = strOtherName;
        }

        @Override
        protected void onPreExecute() {
            progressDL = ProgressDialog.show(StoreActivity.this, "", "Getting filters. Please wait.");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;

            try {

                Cursor cursFilterItems = sql.RawQuerySelect(this.strQuery);

                int ctr = 0;
                arrayList.clear();

                if(cursFilterItems.moveToFirst()) {
                    while (!cursFilterItems.isAfterLast()) {
                        String strDesc = cursFilterItems.getString(cursFilterItems.getColumnIndex(this.strFieldName)).trim().toUpperCase();

                        if(strDesc.equals("")) {
                            cursFilterItems.moveToNext();
                            continue;
                        }

                        ctr++;
                        arrayList.add(new FilterItem(ctr, strDesc));
                        cursFilterItems.moveToNext();
                    }
                }

                cursFilterItems.close();
                result = true;
            }
            catch (Exception ex) {
                errMessage = "Can't load filters. Please try again.";
                String exErr = ex.getMessage() != null ? ex.getMessage() : errMessage;
                errorLog.appendLog(exErr, TAG);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            progressDL.dismiss();
            if(!aBoolean) {
                General.messageBox(StoreActivity.this, "Filter", errMessage);
                return;
            }

            arrFilterItems.clear();
            arrFilterItems.addAll(arrayList);

            final Dialog filterDialog = new Dialog(StoreActivity.this);
            filterDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            filterDialog.setCancelable(true);
            filterDialog.setContentView(R.layout.layout_dialog_filter);

            ListView lvwFilters = (ListView) filterDialog.findViewById(R.id.lvwFilters);
            TextView tvwFilterTitle = (TextView) filterDialog.findViewById(R.id.tvwFilterTitle);
            Button btnBack = (Button) filterDialog.findViewById(R.id.btnFilterBack);

            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    filterDialog.dismiss();
                }
            });

            String promptMessage = "SELECT " + this.strFilterTitle.toUpperCase();
            if(arrFilterItems.size() == 0)
                promptMessage = "No filter found.";
            tvwFilterTitle.setText(promptMessage);

            adapterFilterItem = new FilterItemAdapter(StoreActivity.this, arrFilterItems);
            lvwFilters.setAdapter(adapterFilterItem);
            adapterFilterItem.notifyDataSetChanged();

            lvwFilters.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    filterDialog.dismiss();
                    if(nFilterCode == 1) {
                        strSelectedFilterArea = arrFilterItems.get(position).filterItemDesc;
                    }

                    new LoadStores(nFilterCode, arrFilterItems.get(position)).execute();
                }
            });

            filterDialog.show();
        }
    }

    // LOAD STORE SCORES
    private class LoadStores extends AsyncTask<Void, Void, Boolean> {

        private FilterItem filterItem;
        private ArrayList<Stores> arrPendings;
        private ArrayList<Stores> arrPosted;
        private String errmsg = "";

        private int filterCode = 0;

        LoadStores(int filterCode, FilterItem filterItem) {
            this.filterCode = filterCode;
            this.filterItem = filterItem;
        }

        LoadStores() {
            this.filterCode = 0;
        }

        @Override
        protected void onPreExecute() {
            arrPendings = new ArrayList<>();
            arrPosted = new ArrayList<>();
            arrStoreList.clear();
            progressDL = ProgressDialog.show(StoreActivity.this, "", "Loading stores. Please wait.");
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            boolean result = false;

            try {

                String strQuery = "SELECT * FROM " + SQLiteDB.TABLE_STORE + " ORDER BY " + SQLiteDB.COLUMN_STORE_status + " > 0 DESC";

                switch (filterCode) {
                    case 1: // filter by selected area
                        strQuery = "SELECT * FROM " + SQLiteDB.TABLE_STORE + " WHERE " + SQLiteDB.COLUMN_STORE_area + " = '" + filterItem.filterItemDesc + "' ORDER BY " + SQLiteDB.COLUMN_STORE_status + " > 0 DESC";
                        break;
                    case 2: // filter by selected area and remarks
                        strQuery = "SELECT * FROM " + SQLiteDB.TABLE_STORE + " WHERE " + SQLiteDB.COLUMN_STORE_remarks + " = '" + filterItem.filterItemDesc + "' AND " + SQLiteDB.COLUMN_STORE_area + " = '" + strSelectedFilterArea + "' ORDER BY " + SQLiteDB.COLUMN_STORE_status + " > 0 DESC";
                        break;
                    default:
                        break;
                }

                Cursor cursorStore = sql.RawQuerySelect(strQuery);

                int nstoreid;
                int templateid;
                String templatename;
                boolean isAudited = false;
                boolean isPosted = false;

                cursorStore.moveToFirst();
                while (!cursorStore.isAfterLast()) {
                    nstoreid = cursorStore.getInt(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_id));

                    String storeCode = cursorStore.getString(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_storecode));
                    String webStoreid = cursorStore.getString(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_storeid));

                    String storename = cursorStore.getString(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_name)).trim().replace("\"", "");
                    templateid = cursorStore.getInt(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_audittempid));
                    templatename = cursorStore.getString(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_templatename)).trim().replace("\"", "");
                    isAudited = cursorStore.getInt(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_status)) > 0;
                    int finalValue = cursorStore.getInt(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_final));
                    int initialValue = cursorStore.getInt(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_initial));
                    int nStatus = cursorStore.getInt(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_status));
                    isPosted = cursorStore.getInt(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_posted)) == 1;
                    int gMatrixId = cursorStore.getInt(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_gradematrixid));
                    int auditID = cursorStore.getInt(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_auditid));
                    String account = cursorStore.getString(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_account));
                    String customerCode = cursorStore.getString(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_customercode));
                    String customer = cursorStore.getString(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_customer));
                    String area = cursorStore.getString(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_area));
                    String regionCode = cursorStore.getString(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_regioncode));
                    String region = cursorStore.getString(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_region));
                    String distCode = cursorStore.getString(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_distributorcode));
                    String dist = cursorStore.getString(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_distributor));
                    String remarks = cursorStore.getString(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_remarks));
                    String startDate = cursorStore.getString(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_startdate));
                    String endDate = cursorStore.getString(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_enddate));
                    String templateCode = cursorStore.getString(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_templatecode));

                    double osa = cursorStore.getDouble(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_osa));
                    double npi = cursorStore.getDouble(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_npi));
                    double planogram = cursorStore.getDouble(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_planogram));
                    double perfectStore = cursorStore.getDouble(cursorStore.getColumnIndex(SQLiteDB.COLUMN_STORE_perfectstore));

                    Stores store = new Stores(nstoreid, storeCode, webStoreid, storename, templateid, templatename, finalValue, initialValue, isAudited, isPosted, gMatrixId);
                    store.auditID = auditID;
                    store.account = account;
                    store.customerCode = customerCode;
                    store.customer = customer;
                    store.area = area;
                    store.regionCode = regionCode;
                    store.region = region;
                    store.distributorCode = distCode;
                    store.distributor = dist;
                    store.remarks = remarks;
                    store.startDate = startDate;
                    store.endDate = endDate;
                    store.osa = osa;
                    store.npi = npi;
                    store.planogram = planogram;
                    store.perfectStore = perfectStore;
                    store.templateCode = templateCode;
                    store.status = nStatus;
                    store.templateDate = startDate + " - " + endDate;

                    if (isAudited && !isPosted)
                        arrPendings.add(store);
                    else if (isAudited && isPosted)
                        arrPosted.add(store);
                    else
                        arrStoreList.add(store);

                    cursorStore.moveToNext();
                }
                cursorStore.close();
                result = true;
            }
            catch (Exception ex) {
                errmsg = "Can't load stores.";
                String exErr = ex.getMessage() != null ? ex.getMessage() : errmsg;
                errorLog.appendLog(exErr, TAG);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean bResult) {
            if(!bResult) {
                progressDL.dismiss();
                Toast.makeText(StoreActivity.this, errmsg, Toast.LENGTH_SHORT).show();
                return;
            }

            lstStores.clear();
            lstStores.addAll(arrPendings);
            lstStores.addAll(arrPosted);
            lstStores.addAll(arrStoreList);

            adapter = new StoreAdapter(StoreActivity.this, lstStores);
            lvwStore.setAdapter(adapter);
            lvwStore.setSmoothScrollbarEnabled(true);
            adapter.notifyDataSetChanged();

            progressDL.dismiss();

            if(filterItem != null) {
                if (filterCode == 1) {
                    String strQuery = "SELECT " + SQLiteDB.COLUMN_STORE_remarks + " FROM " + SQLiteDB.TABLE_STORE + " WHERE " + SQLiteDB.COLUMN_STORE_area + " = '" + strSelectedFilterArea + "' GROUP BY " + SQLiteDB.COLUMN_STORE_remarks;
                    new LoadFilter(2, strQuery, SQLiteDB.COLUMN_STORE_remarks, "territory").execute();
                    return;
                }
            }

            int nUnposteds = arrPendings.size();
            if(nUnposteds > 0) {
                new AlertDialog.Builder(StoreActivity.this)
                        .setCancelable(false)
                        .setTitle("Audits")
                        .setMessage("You have " + String.valueOf(nUnposteds) + " unposted audits on stores. Please post it before the posting date expiration.")
                        .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();
            }
        }
    }

    // AUDIT BUTTON CLICK
    public void auditOnClickEvent(View v) {
        Stores stores = (Stores) v.getTag();
        General.selectedStore = stores;
        new AsyncGetStoreTemplate().execute();
    }

    // PREVIEW BUTTON CLICK
    public void previewOnClickEvent(View v) {

        Stores storeSelected = (Stores) v.getTag();

        if(storeSelected == null) {
            Toast.makeText(StoreActivity.this, "No selected Store.", Toast.LENGTH_LONG).show();
            return;
        }

        General.selectedStore = storeSelected;

        Intent intentPreview = new Intent(StoreActivity.this, StorePreviewActivity.class);
        startActivity(intentPreview);
    }

    private void GetPerfectList() {
        Cursor cursPCategory = sql.GetDataCursor(SQLiteDB.TABLE_PERFECT_CATEGORY);
        Cursor cursPGroup = sql.GetDataCursor(SQLiteDB.TABLE_PERFECT_GROUP);

        TCRLib.arrPCategoryList.clear();
        TCRLib.arrPGroupList.clear();

        if(cursPCategory.moveToFirst()) {
            while (!cursPCategory.isAfterLast()) {
                int categoryID = cursPCategory.getInt(cursPCategory.getColumnIndex(SQLiteDB.COLUMN_PCATEGORY_categoryid));
                TCRLib.arrPCategoryList.add(categoryID);
                cursPCategory.moveToNext();
            }
        }

        if(cursPGroup.moveToFirst()) {
            while (!cursPGroup.isAfterLast()) {
                int groupID = cursPGroup.getInt(cursPGroup.getColumnIndex(SQLiteDB.COLUMN_PGROUP_groupid));
                TCRLib.arrPGroupList.add(groupID);
                cursPGroup.moveToNext();
            }
        }

        cursPCategory.close();
        cursPGroup.close();
    }

    // TASK: LOAD STORE TEMPLATE
    private class AsyncGetStoreTemplate extends AsyncTask<Void, Void, Boolean> {

        private String errMsg;

        @Override
        protected void onPreExecute() {
            progressDL = ProgressDialog.show(StoreActivity.this, "", "Loading store template. Please wait", true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            Boolean result = false;

            try {

                // STORE QUESTIONS
                Cursor curs = sql.GetDataCursor(SQLiteDB.TABLE_STORECATEGORY, SQLiteDB.COLUMN_STORECATEGORY_storeid + " = '" + General.selectedStore.storeID + "'");
                curs.moveToFirst();
                if (curs.getCount() > 0) {
                    return true;
                } else {
                    General.arrBrandSelected = new ArrayList<String>();
                    General.arrBrandSelected.clear();

                    // GET SECONDRY KEYLIST
                    Cursor cursKeylist = sql.GetDataCursor(SQLiteDB.TABLE_SECONDARYKEYLIST);
                    cursKeylist.moveToFirst();
                    General.arrSecondaryKeylist = new ArrayList<String>();
                    while (!cursKeylist.isAfterLast()) {
                        General.arrSecondaryKeylist.add(cursKeylist.getString(cursKeylist.getColumnIndex(SQLiteDB.COLUMN_SECONDARYKEYLIST_keygroupid)));
                        cursKeylist.moveToNext();
                    }

                    SQLiteDatabase dbaseStoreQ = sqLiteDB.getWritableDatabase();

                    // GET STORE CATEGORIES
                    Cursor cursCategoryOfStore = sql.GetDataCursor(SQLiteDB.TABLE_CATEGORY, SQLiteDB.COLUMN_CATEGORY_audittempid + " = '" + General.selectedStore.auditTemplateId + "'");
                    cursCategoryOfStore.moveToFirst();

                    String[] afieldsStoreCategories = {
                            SQLiteDB.COLUMN_STORECATEGORY_storeid,
                            SQLiteDB.COLUMN_STORECATEGORY_categoryid,
                            SQLiteDB.COLUMN_STORECATEGORY_initial,
                            SQLiteDB.COLUMN_STORECATEGORY_exempt,
                            SQLiteDB.COLUMN_STORECATEGORY_final,
                            SQLiteDB.COLUMN_STORECATEGORY_status
                    };

                    String sqlinsertStoreCategories = sql.createInsertBulkQuery(SQLiteDB.TABLE_STORECATEGORY, afieldsStoreCategories);
                    SQLiteStatement sqlstatementStoreCategories = dbaseStoreQ.compileStatement(sqlinsertStoreCategories);
                    dbaseStoreQ.beginTransaction();

                    // INSERT CATEGORIES PER STORE
                    if (cursCategoryOfStore.getCount() > 0) {

                        cursCategoryOfStore.moveToFirst();
                        while (!cursCategoryOfStore.isAfterLast()) {
                            sqlstatementStoreCategories.clearBindings();
                            sqlstatementStoreCategories.bindString(1, String.valueOf(General.selectedStore.storeID));
                            sqlstatementStoreCategories.bindString(2, cursCategoryOfStore.getString(cursCategoryOfStore.getColumnIndex(SQLiteDB.COLUMN_CATEGORY_id)));
                            sqlstatementStoreCategories.bindString(3, "0");
                            sqlstatementStoreCategories.bindString(4, "0");
                            sqlstatementStoreCategories.bindString(5, "");
                            sqlstatementStoreCategories.bindString(6, "0");
                            sqlstatementStoreCategories.execute();
                            cursCategoryOfStore.moveToNext();
                        }
                    }

                    dbaseStoreQ.setTransactionSuccessful();
                    dbaseStoreQ.endTransaction();

                    // GET GROUP BY CATEGORIES
                    Cursor cursCategories = sql.GetDataCursor(SQLiteDB.TABLE_STORECATEGORY, SQLiteDB.COLUMN_STORECATEGORY_storeid + " = '" + General.selectedStore.storeID + "'");
                    cursCategories.moveToFirst();
                    while (!cursCategories.isAfterLast()) {

                        String storecategoryid = cursCategories.getString(cursCategories.getColumnIndex(SQLiteDB.COLUMN_STORECATEGORY_id));
                        String categoryid = cursCategories.getString(cursCategories.getColumnIndex(SQLiteDB.COLUMN_STORECATEGORY_categoryid));

                        String[] aFieldsStorecateggroup = {
                                SQLiteDB.COLUMN_STORECATEGORYGROUP_storecategid,
                                SQLiteDB.COLUMN_STORECATEGORYGROUP_groupid,
                                SQLiteDB.COLUMN_STORECATEGORYGROUP_initial,
                                SQLiteDB.COLUMN_STORECATEGORYGROUP_exempt,
                                SQLiteDB.COLUMN_STORECATEGORYGROUP_final,
                                SQLiteDB.COLUMN_STORECATEGORYGROUP_status
                        };

                        Cursor cursGroupPerCategories = sql.RawQuerySelect("SELECT " + SQLiteDB.COLUMN_GROUP_id + "," + SQLiteDB.COLUMN_GROUP_groupid
                                + " FROM " + SQLiteDB.TABLE_GROUP
                                + " WHERE " + SQLiteDB.COLUMN_GROUP_categoryid + " = '" + categoryid + "'"
                                + " ORDER BY " + SQLiteDB.COLUMN_GROUP_grouporder);
                        cursGroupPerCategories.moveToFirst();

                        String sqlinsertStoreCategGroup = sql.createInsertBulkQuery(SQLiteDB.TABLE_STORECATEGORYGROUP, aFieldsStorecateggroup);
                        SQLiteStatement sqlstatementStoreCategGroup = dbaseStoreQ.compileStatement(sqlinsertStoreCategGroup);
                        dbaseStoreQ.beginTransaction();

                        while (!cursGroupPerCategories.isAfterLast()) {

                            String groupid = cursGroupPerCategories.getString(cursGroupPerCategories.getColumnIndex(SQLiteDB.COLUMN_GROUP_id));

                            sqlstatementStoreCategGroup.clearBindings();
                            sqlstatementStoreCategGroup.bindString(1, storecategoryid);
                            sqlstatementStoreCategGroup.bindString(2, groupid);
                            sqlstatementStoreCategGroup.bindString(3, "0");
                            sqlstatementStoreCategGroup.bindString(4, "0");
                            sqlstatementStoreCategGroup.bindString(5, "");
                            sqlstatementStoreCategGroup.bindString(6, "0"); //0 - pending, 1 - partial, 2 - complete
                            sqlstatementStoreCategGroup.execute();

                            cursGroupPerCategories.moveToNext();
                        }

                        dbaseStoreQ.setTransactionSuccessful();
                        dbaseStoreQ.endTransaction();

                        // GET QUESTION PER GROUP
                        Cursor cursGroup = sql.GetDataCursor(SQLiteDB.TABLE_STORECATEGORYGROUP, SQLiteDB.COLUMN_STORECATEGORYGROUP_storecategid + " = '" + storecategoryid + "'");

                        cursGroup.moveToFirst();

                        while (!cursGroup.isAfterLast()) {

                            String storeCategoryGroupID = cursGroup.getString(cursGroup.getColumnIndex(SQLiteDB.COLUMN_STORECATEGORYGROUP_id));
                            String storegroupID = cursGroup.getString(cursGroup.getColumnIndex(SQLiteDB.COLUMN_STORECATEGORYGROUP_groupid));

                            Cursor cursTempGroup = sql.GetDataCursor(SQLiteDB.TABLE_GROUP, SQLiteDB.COLUMN_GROUP_id + " = '" + storegroupID + "'");
                            cursTempGroup.moveToFirst();

                            String strGroupCategoryid = cursTempGroup.getString(cursTempGroup.getColumnIndex(SQLiteDB.COLUMN_GROUP_categoryid));
                            String strTempGroupId = cursTempGroup.getString(cursTempGroup.getColumnIndex(SQLiteDB.COLUMN_GROUP_groupid));

                            Cursor cursTempCategory = sql.GetDataCursor(SQLiteDB.TABLE_CATEGORY, SQLiteDB.COLUMN_CATEGORY_id + " = '" + strGroupCategoryid + "'");
                            cursTempCategory.moveToFirst();
                            String strTempcategoryid = cursTempCategory.getString(cursTempCategory.getColumnIndex(SQLiteDB.COLUMN_CATEGORY_categoryid));

                            // SECONDARY DISPLAY
                            General.arrBrandSelected.clear();
                            Cursor cursBrand = sql.RawQuerySelect("SELECT " + SQLiteDB.COLUMN_SECONDARYDISP_brand
                                    + " FROM " + SQLiteDB.TABLE_SECONDARYDISP
                                    + " WHERE " + SQLiteDB.COLUMN_SECONDARYDISP_storeid + " = " + General.selectedStore.webStoreID
                                    + " AND " + SQLiteDB.COLUMN_SECONDARYDISP_categoryid + " = " + strTempcategoryid);

                            cursBrand.moveToFirst();

                            while (!cursBrand.isAfterLast()) {

                                General.arrBrandSelected.add(cursBrand.getString(cursBrand.getColumnIndex(SQLiteDB.COLUMN_SECONDARYDISP_brand)).trim().toLowerCase(Locale.getDefault()));
                                cursBrand.moveToNext();
                            }
                            cursBrand.close();

                            // GET QUESTIONS PER GROUP
                            Cursor cursQuestionsPerGroup = sql.RawQuerySelect("SELECT " + SQLiteDB.COLUMN_QUESTION_prompt + "," + SQLiteDB.COLUMN_QUESTION_id + "," + SQLiteDB.COLUMN_QUESTION_groupid + "," + SQLiteDB.COLUMN_QUESTION_questionid
                                    + " FROM " + SQLiteDB.TABLE_QUESTION
                                    + " WHERE " + SQLiteDB.COLUMN_QUESTION_groupid + " = '" + storegroupID + "'"
                                    + " ORDER BY " + SQLiteDB.COLUMN_QUESTION_order);
                            cursQuestionsPerGroup.moveToFirst();

                            String[] aFieldsStorequestion = {
                                    SQLiteDB.COLUMN_STOREQUESTION_storecategorygroupid,
                                    SQLiteDB.COLUMN_STOREQUESTION_questionid,
                                    SQLiteDB.COLUMN_STOREQUESTION_isAnswered,
                                    SQLiteDB.COLUMN_STOREQUESTION_initial,
                                    SQLiteDB.COLUMN_STOREQUESTION_exempt,
                                    SQLiteDB.COLUMN_STOREQUESTION_final
                            };

                            String sqlinsertStoreQuestions = sql.createInsertBulkQuery(SQLiteDB.TABLE_STOREQUESTION, aFieldsStorequestion);
                            SQLiteStatement sqlstatementStoreQuestions = dbaseStoreQ.compileStatement(sqlinsertStoreQuestions);
                            dbaseStoreQ.beginTransaction();

                            while (!cursQuestionsPerGroup.isAfterLast()) {
                                String questionid = cursQuestionsPerGroup.getString(cursQuestionsPerGroup.getColumnIndex(SQLiteDB.COLUMN_QUESTION_id));
                                String strQuestionPrompt = cursQuestionsPerGroup.getString(cursQuestionsPerGroup.getColumnIndex(SQLiteDB.COLUMN_QUESTION_prompt)).trim().toLowerCase(Locale.getDefault());

                                if (General.arrSecondaryKeylist.contains(strTempGroupId)) {
                                    for (String brands : General.arrBrandSelected) {
                                        if (strQuestionPrompt.contains(brands)) {
                                            sqlstatementStoreQuestions.clearBindings();
                                            sqlstatementStoreQuestions.bindString(1, storeCategoryGroupID);
                                            sqlstatementStoreQuestions.bindString(2, questionid);
                                            sqlstatementStoreQuestions.bindString(3, "0");
                                            sqlstatementStoreQuestions.bindString(4, "0");
                                            sqlstatementStoreQuestions.bindString(5, "0");
                                            sqlstatementStoreQuestions.bindString(6, "0");
                                            sqlstatementStoreQuestions.execute();
                                        }
                                    }
                                } else {
                                    sqlstatementStoreQuestions.clearBindings();
                                    sqlstatementStoreQuestions.bindString(1, storeCategoryGroupID);
                                    sqlstatementStoreQuestions.bindString(2, questionid);
                                    sqlstatementStoreQuestions.bindString(3, "0");
                                    sqlstatementStoreQuestions.bindString(4, "0");
                                    sqlstatementStoreQuestions.bindString(5, "0");
                                    sqlstatementStoreQuestions.bindString(6, "0");
                                    sqlstatementStoreQuestions.execute();
                                }
                                cursQuestionsPerGroup.moveToNext();
                            }

                            dbaseStoreQ.setTransactionSuccessful();
                            dbaseStoreQ.endTransaction();
                            cursGroup.moveToNext();
                        }
                        cursCategories.moveToNext();
                    }
                    dbaseStoreQ.close();
                }
                result = true;
            }
            catch (Exception ex) {
                errMsg = "Can't load store templates.";
                String exErr = ex.getMessage() != null ? ex.getMessage() : errMsg;
                errorLog.appendLog(exErr, TAG);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean bResult) {
            progressDL.dismiss();
            if(!bResult) {
                Toast.makeText(StoreActivity.this, errMsg, Toast.LENGTH_LONG).show();
                return;
            }

            Intent intentActivity = new Intent(StoreActivity.this, CategoryActivity.class);
            intentActivity.putExtra("STORE_ID", String.valueOf(General.selectedStore.storeID));
            intentActivity.putExtra("STORE_NAME", String.valueOf(General.selectedStore.storeName));
            intentActivity.putExtra("GRADE_MATRIX", General.selectedStore.gradeMatrixId);
            startActivity(intentActivity);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        new LoadStores().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.stores_menu, menu);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            // Associate searchable configuration with the SearchView
            SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
            android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) menu.findItem(R.id.searchStore).getActionView();
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

            searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    adapter.filter(newText.trim().toLowerCase(Locale.getDefault()));
                    return true;
                }
            });
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_set_filter:
                String strQuery = "SELECT " + SQLiteDB.COLUMN_STORE_area + " FROM " + SQLiteDB.TABLE_STORE + " GROUP BY " + SQLiteDB.COLUMN_STORE_area;
                new LoadFilter(1, strQuery, SQLiteDB.COLUMN_STORE_area, "Area").execute();
                break;
            case R.id.action_filterby_showall:
                new LoadStores().execute();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.hold, R.anim.slide_in_right);
    }


}
