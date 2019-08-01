package com.android.pplusaudit2._Category;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.pplusaudit2.Database.SQLLibrary;
import com.android.pplusaudit2.Database.SQLiteDB;
import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.ErrorLogs.ErrorLog;
import com.android.pplusaudit2.General;
import com.android.pplusaudit2.HttpUtility.HttpUtility;
import com.android.pplusaudit2.MyMessageBox;
import com.android.pplusaudit2.R;
import com.android.pplusaudit2.TCRLib;
import com.android.pplusaudit2._Group.GroupActivity;

import java.util.ArrayList;

/**
 * Created by ULTRABOOK on 9/22/2015.
 */
public class CategoryActivity extends AppCompatActivity {

    private ArrayList<Category> arrActivityList;
    private ArrayList<Category> lstActivities;

    private SQLLibrary sqlLibrary;
    private TCRLib tcrLib;

    private static final int BUFFER_SIZE = 4096;
    private ProgressDialog progressDL;

    private String storeid = "";
    private int nGradeMatrix;

    private ListView lvwActivity;
    private String TAG = "";
    private ErrorLog errorLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_activity_layout);

        overridePendingTransition(R.anim.slide_in_left, R.anim.hold);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TAG = CategoryActivity.this.getLocalClassName();
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(this, General.errlogFile));

        errorLog = new ErrorLog(General.errlogFile, this);

        arrActivityList = new ArrayList<>();
        lstActivities = new ArrayList<>();

        sqlLibrary = new SQLLibrary(this);
        tcrLib = new TCRLib(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String storename = extras.getString("STORE_NAME");
            storeid = extras.getString("STORE_ID");
            nGradeMatrix = extras.getInt("GRADE_MATRIX");
            getSupportActionBar().setTitle(storename.toUpperCase());
        }

        lvwActivity = (ListView) findViewById(R.id.lvwActivity);
    }

    // LOAD ACTIVITY SCORES
    private class AsyncLoadActivities extends AsyncTask<Void, Void, Boolean> {

        String errMsg = "";
        int orderMode = 0;

        AsyncLoadActivities() {
            this.orderMode = 0;
        }

        AsyncLoadActivities(int orderMode) {
            this.orderMode = orderMode;
        }

        @Override
        protected void onPreExecute() {
            progressDL = ProgressDialog.show(CategoryActivity.this, "", "Loading categories. Please wait.");
            arrActivityList.clear();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            boolean result = false;

            try {
                String strQuery = "SELECT " + SQLiteDB.TABLE_STORECATEGORY + "." + SQLiteDB.COLUMN_STORECATEGORY_id + "," + SQLiteDB.COLUMN_CATEGORY_categoryorder
                        + "," + SQLiteDB.COLUMN_CATEGORY_categorydesc + "," + SQLiteDB.TABLE_STORECATEGORY + "." + SQLiteDB.COLUMN_STORECATEGORY_categoryid
                        + "," + SQLiteDB.COLUMN_STORECATEGORY_final + "," + SQLiteDB.COLUMN_STORECATEGORY_status + "," + SQLiteDB.TABLE_CATEGORY + "." + SQLiteDB.COLUMN_CATEGORY_categoryid + " AS webCategid"
                        + " FROM " + SQLiteDB.TABLE_STORECATEGORY
                        + " JOIN " + SQLiteDB.TABLE_CATEGORY + " ON " + SQLiteDB.TABLE_CATEGORY + "." + SQLiteDB.COLUMN_CATEGORY_id + " = " + SQLiteDB.TABLE_STORECATEGORY + "." + SQLiteDB.COLUMN_STORECATEGORY_categoryid
                        + " WHERE " + SQLiteDB.COLUMN_STORECATEGORY_storeid + " = '" + General.selectedStore.storeID + "'"
                        + " ORDER BY " + SQLiteDB.COLUMN_CATEGORY_categoryorder;

                if(this.orderMode == 1) { // SORT ALPHABETICALLY
                    strQuery = "SELECT " + SQLiteDB.TABLE_STORECATEGORY + "." + SQLiteDB.COLUMN_STORECATEGORY_id + "," + SQLiteDB.COLUMN_CATEGORY_categoryorder
                            + "," + SQLiteDB.COLUMN_CATEGORY_categorydesc + "," + SQLiteDB.TABLE_STORECATEGORY + "." + SQLiteDB.COLUMN_STORECATEGORY_categoryid
                            + "," + SQLiteDB.COLUMN_STORECATEGORY_final + "," + SQLiteDB.COLUMN_STORECATEGORY_status + "," + SQLiteDB.TABLE_CATEGORY + "." + SQLiteDB.COLUMN_CATEGORY_categoryid + " AS webCategid"
                            + " FROM " + SQLiteDB.TABLE_STORECATEGORY
                            + " JOIN " + SQLiteDB.TABLE_CATEGORY + " ON " + SQLiteDB.TABLE_CATEGORY + "." + SQLiteDB.COLUMN_CATEGORY_id + " = " + SQLiteDB.TABLE_STORECATEGORY + "." + SQLiteDB.COLUMN_STORECATEGORY_categoryid
                            + " WHERE " + SQLiteDB.COLUMN_STORECATEGORY_storeid + " = '" + General.selectedStore.storeID + "'"
                            + " ORDER BY " + SQLiteDB.COLUMN_CATEGORY_categorydesc;
                }

                Cursor cursStoreCategory = sqlLibrary.RawQuerySelect(strQuery);

                cursStoreCategory.moveToFirst();

                while (!cursStoreCategory.isAfterLast()) {
                    String storecategid = cursStoreCategory.getString(cursStoreCategory.getColumnIndex(SQLiteDB.COLUMN_STORECATEGORY_id));

                    String categFinal = cursStoreCategory.getString(cursStoreCategory.getColumnIndex(SQLiteDB.COLUMN_STORECATEGORY_final));
                    String categStatusno = cursStoreCategory.getString(cursStoreCategory.getColumnIndex(SQLiteDB.COLUMN_STORECATEGORY_status));
                    String strCategStatus = "";

                    strCategStatus = tcrLib.GetStatus(categStatusno);
                    General.SCORE_STATUS scoreStatus = tcrLib.GetScoreStatus(categFinal);

                    int categorder = cursStoreCategory.getInt(cursStoreCategory.getColumnIndex(SQLiteDB.COLUMN_CATEGORY_categoryorder));
                    String category = cursStoreCategory.getString(cursStoreCategory.getColumnIndex(SQLiteDB.COLUMN_CATEGORY_categorydesc)).trim().replace("\"", "");
                    int tempCategoryid = cursStoreCategory.getInt(cursStoreCategory.getColumnIndex(SQLiteDB.COLUMN_CATEGORY_categoryid));
                    int webCategoryId = cursStoreCategory.getInt(cursStoreCategory.getColumnIndex("webCategid"));

                    arrActivityList.add(new Category(categorder, tempCategoryid, category.trim().toUpperCase(), storecategid, strCategStatus, scoreStatus, webCategoryId));
                    cursStoreCategory.moveToNext();
                }
                result = true;
            }
            catch (Exception ex) {
                errMsg = "Can't load categories.";
                String exErr = ex.getMessage() != null ? ex.getMessage() : errMsg;
                errorLog.appendLog(exErr, TAG);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean bResult) {
            lstActivities.clear();
            lstActivities.addAll(arrActivityList);
            CategoryAdapter adapter = new CategoryAdapter(CategoryActivity.this, lstActivities);
            lvwActivity.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            progressDL.dismiss();
            if(!bResult) {
                Toast.makeText(CategoryActivity.this, errMsg, Toast.LENGTH_SHORT).show();
                return;
            }

            lvwActivity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    TextView tvwCateg = (TextView) view.findViewById(R.id.tvwActivity);
                    String storecategoryid = String.valueOf(tvwCateg.getTag().toString());

                    //Cursor cursGetcategid = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_STORECATEGORY, SQLiteDB.COLUMN_STORECATEGORY_id + " = '" + storecategoryid + "'");
                    Cursor cursGetcategid = sqlLibrary.RawQuerySelect("SELECT * FROM " + SQLiteDB.TABLE_STORECATEGORY
                                                                + " JOIN " + SQLiteDB.TABLE_CATEGORY + " ON " + SQLiteDB.TABLE_CATEGORY + "." + SQLiteDB.COLUMN_CATEGORY_id + " = " + SQLiteDB.TABLE_STORECATEGORY + "." + SQLiteDB.COLUMN_STORECATEGORY_categoryid
                                                                + " WHERE " + SQLiteDB.TABLE_STORECATEGORY + "." + SQLiteDB.COLUMN_STORECATEGORY_id + " = " + storecategoryid);
                    cursGetcategid.moveToFirst();

                    String categoryid = "";
                    if(cursGetcategid.getCount() > 0) categoryid = cursGetcategid.getString(cursGetcategid.getColumnIndex(SQLiteDB.COLUMN_CATEGORY_categoryid));

                    Intent intentActivity = new Intent(CategoryActivity.this, GroupActivity.class);
                    intentActivity.putExtra("STORE_CATEGORY_ID", storecategoryid);
                    intentActivity.putExtra("CATEGORY_ID", categoryid);
                    intentActivity.putExtra("CATEGORY_NAME", String.valueOf(tvwCateg.getText()));
                    startActivity(intentActivity);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //new AsyncLoadActivities().execute();
    }

    @Override
    protected void onStart() {
        super.onStart();
        new AsyncLoadActivities().execute();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.hold, R.anim.slide_in_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_sort_by_alphabetically:
                new AsyncLoadActivities(1).execute();
                break;
            case R.id.action_sort_by_template:
                new AsyncLoadActivities().execute();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.category_and_group_menu, menu);
        return true;
    }


}
