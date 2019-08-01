package com.android.pplusaudit2.PJP_Compliance;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.android.pplusaudit2.Database.SQLLibrary;
import com.android.pplusaudit2.Database.SQLiteDB;
import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.ErrorLogs.ErrorLog;
import com.android.pplusaudit2.General;
import com.android.pplusaudit2.R;

import java.util.ArrayList;

public class PjpPreviewActivity extends AppCompatActivity {

    private String TAG;
    private SQLLibrary sqlLibrary;
    private ArrayList<Compliance> arrCompliance;
    private ProgressDialog progressDialog;
    private ListView lvwComp;
    private ErrorLog errorLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pjppreview_activity_layout);

        overridePendingTransition(R.anim.slide_up, R.anim.hold);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(this, General.errlogFile));
        TAG = PjpPreviewActivity.this.getLocalClassName();

        errorLog = new ErrorLog(General.errlogFile, this);

        arrCompliance = new ArrayList<>();
        sqlLibrary = new SQLLibrary(this);
        lvwComp = (ListView) findViewById(R.id.lvwCheckIns);
        new LoadCheckins().execute();
    }

    private class LoadCheckins extends AsyncTask<Void, Void, Boolean> {
        String errmsg = "";

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(PjpPreviewActivity.this, "", "Loading records");
            arrCompliance.clear();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            boolean result = false;

            try {

                Cursor cursorCheckIn = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_PJPCOMP, SQLiteDB.COLUMN_PJPCOMP_storeid + " = '" + General.selectedStore.storeID + "'");
                if (cursorCheckIn.moveToFirst()) {
                    while (!cursorCheckIn.isAfterLast()) {

                        int id = cursorCheckIn.getInt(cursorCheckIn.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_id));
                        String usercode = cursorCheckIn.getString(cursorCheckIn.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_usercode));
                        int storeid = cursorCheckIn.getInt(cursorCheckIn.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_storeid));
                        String webstoreid = cursorCheckIn.getString(cursorCheckIn.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_webstoreid));
                        String date = cursorCheckIn.getString(cursorCheckIn.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_date));
                        String time = cursorCheckIn.getString(cursorCheckIn.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_time));
                        String longitude = cursorCheckIn.getString(cursorCheckIn.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_time));
                        String latitude = cursorCheckIn.getString(cursorCheckIn.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_time));
                        String address = cursorCheckIn.getString(cursorCheckIn.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_address));
                        boolean posted = cursorCheckIn.getInt(cursorCheckIn.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_posted)) == 1;

                        Compliance compliance = new Compliance(id, usercode, storeid, webstoreid, date, time, General.userFullName, longitude, latitude, posted);
                        compliance.address = address.trim();

                        arrCompliance.add(compliance);
                        cursorCheckIn.moveToNext();
                    }
                }

                cursorCheckIn.close();
                result = true;
            }
            catch (Exception ex) {
                errmsg = "Date error in loading pjp check records.";
                String exMsg = ex.getMessage() != null ? ex.getMessage() : errmsg;
                errorLog.appendLog(exMsg, TAG);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            progressDialog.dismiss();
            if(!aBoolean) {
                Toast.makeText(PjpPreviewActivity.this, errmsg, Toast.LENGTH_SHORT).show();
                return;
            }

            lvwComp.setAdapter(new PjpPreviewAdapter(PjpPreviewActivity.this, arrCompliance));
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.hold, R.anim.slide_down);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
