package com.android.pplusaudit2._Store;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.pplusaudit2.AppSettings;
import com.android.pplusaudit2.Database.SQLLibrary;
import com.android.pplusaudit2.Database.SQLiteDB;
import com.android.pplusaudit2.Debug.DebugLog;
import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.ErrorLogs.ErrorLog;
import com.android.pplusaudit2.General;
import com.android.pplusaudit2.PJP_Compliance.Compliance;
import com.android.pplusaudit2.R;
import com.android.pplusaudit2.TCRLib;
import com.android.pplusaudit2._Category.Category;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ULTRABOOK on 10/28/2015.
 */
public class StorePreviewActivity extends AppCompatActivity {

    private SQLLibrary sqlLibrary;
    private TCRLib tcrLib;

    private String strOsa = "";
    private String strNpi = "";
    private String strPlanogram = "";

    private ProgressDialog progressDL;
    private File filepathToSend;
    private String strFilenameToSend;

    private AlertDialog postDialog;
    private ProgressDialog pDialog;

    private ListView lvwPreview;

    private String strDetailsBody;

    private String strImageFolder;

    private PowerManager.WakeLock wlStayAwake;

    private boolean toggleAudit;

    private ArrayList<Category> arrCategories;
    private String TAG;
    private ErrorLog errorLog;
    private ArrayList<Compliance> complianceArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.store_preview_layout_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        overridePendingTransition(R.anim.slide_up, R.anim.hold);

        TAG = StorePreviewActivity.this.getLocalClassName();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(this, General.errlogFile));
        errorLog = new ErrorLog(General.errlogFile, this);

        getSupportActionBar().setTitle("AUDIT SUMMARY");

        complianceArrayList = new ArrayList<>();

        Typeface fontIcon = Typeface.createFromAsset(getAssets(), General.typefacename);
        PowerManager powerman = (PowerManager) getSystemService(POWER_SERVICE);
        wlStayAwake = powerman.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "wakelocktag");

        sqlLibrary = new SQLLibrary(this);
        tcrLib = new TCRLib(this);
        toggleAudit = false;

        arrCategories = new ArrayList<>();
        lvwPreview = (ListView) findViewById(R.id.lvwPreview);

        TextView tvwStoreTemplate = (TextView) findViewById(R.id.tvwStoreTemplate);
        TextView tvwOsa = (TextView) findViewById(R.id.tvwOsa);
        TextView tvwNpi = (TextView) findViewById(R.id.tvwNpi);
        TextView tvwPlanogram = (TextView) findViewById(R.id.tvwPlanogram);
        TextView tvwPerfectStore = (TextView) findViewById(R.id.tvwPerfectStorePerc);

        strOsa = String.format(Locale.getDefault(), "%.2f", General.selectedStore.osa) + " %";
        strNpi = String.format(Locale.getDefault(), "%.2f", General.selectedStore.npi) + " %";
        strPlanogram = String.format(Locale.getDefault(), "%.2f", General.selectedStore.planogram) + " %";
        String strPerfectStore = String.format(Locale.getDefault(), "%.2f", General.selectedStore.perfectStore) + " %";

        String strTemplate = General.selectedStore.storeName + " - " + General.selectedStore.templateName;
        tvwStoreTemplate.setText(strTemplate);
        tvwOsa.setText(strOsa);
        tvwNpi.setText(strNpi);
        tvwPlanogram.setText(strPlanogram);
        tvwPerfectStore.setText(strPerfectStore);

        strFilenameToSend = General.usercode + "_" + General.selectedStore.storeCode + ".csv";
        filepathToSend = new File(AppSettings.postingFolder, strFilenameToSend);

        Button btnBack = (Button) findViewById(R.id.btnBackPreview);
        Button btnPost = (Button) findViewById(R.id.btnPostAudit);
        TextView tvwOsaIcon = (TextView) findViewById(R.id.tvwOsaIcon);
        TextView tvwNpiIcon = (TextView) findViewById(R.id.tvwNpiIcon);
        TextView tvwPlanoIcon = (TextView) findViewById(R.id.tvwPlanogramIcon);
        TextView tvwPerfectIcon = (TextView) findViewById(R.id.tvwPerfectIcon);

        tvwOsaIcon.setTypeface(fontIcon);
        tvwNpiIcon.setTypeface(fontIcon);
        tvwPlanoIcon.setTypeface(fontIcon);
        tvwPerfectIcon.setTypeface(fontIcon);

        tvwOsaIcon.setText("\uf201");
        tvwNpiIcon.setText("\uf0a1");
        tvwPlanoIcon.setText("\uf07a");
        tvwPerfectIcon.setText("\uf0e4");

        btnPost.setTypeface(fontIcon);
        btnBack.setTypeface(fontIcon);
        btnPost.setText("\uf1d8" + " POST AUDIT");
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if(!CheckDateValidation(General.selectedStore.startDate, General.selectedStore.endDate)) {
            btnPost.setEnabled(false);
            General.messageBox(StorePreviewActivity.this, "End of posting", "End date of posting");
        }

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postDialog = new AlertDialog.Builder(StorePreviewActivity.this).create();
                postDialog.setTitle("Post survey");
                postDialog.setMessage("Do you want to post this survey result?");
                postDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Post", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        postDialog.dismiss();
                        wlStayAwake.acquire();
                        new CheckInternet().execute();
                    }
                });
                postDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { postDialog.dismiss(); }
                });
                postDialog.show();
            }
        });

        new LoadAuditSummary().execute();
    }

    private class CheckInternet extends AsyncTask<Void, Void, Boolean> {
        String errmsg = "";

        @Override
        protected void onPreExecute() {
            progressDL = ProgressDialog.show(StorePreviewActivity.this, "", "Checking internet connection.");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;

            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if(activeNetwork != null) {
                if(activeNetwork.isFailover()) errmsg = "Internet connection fail over.";
                result = activeNetwork.isAvailable() || activeNetwork.isConnectedOrConnecting();
            }
            else errmsg = "No internet connection.";

            return result;
        }

        @Override
        protected void onPostExecute(Boolean bResult) {
            progressDL.dismiss();
            if(!bResult) {
                Toast.makeText(StorePreviewActivity.this, errmsg, Toast.LENGTH_SHORT).show();
                return;
            }

            new AsyncGenerateTextFile().execute();
        }
    }

    private boolean CheckDateValidation(String dtFrom, String dtTo) {
        boolean result = false;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date dateToday = new Date();
            Date dateFrom = dateFormat.parse(dtFrom);
            Date dateTo = dateFormat.parse(dtTo);

            String strDateToday = dateFormat.format(dateToday);

            if((dateToday.after(dateFrom) || strDateToday.equals(dtFrom)) && (dateToday.before(dateTo) || strDateToday.equals(dtTo))) {
                result = true;
            }
        }
        catch (ParseException pex) {
            pex.printStackTrace();
            DebugLog.log("ParseException: " + pex.getMessage());
        }

        return result;
    }

    public boolean CheckIfPosted(String storeid) {
        boolean res = false;

        Cursor cursCheck = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_STORE, SQLiteDB.COLUMN_STORE_id + " = '" + storeid + "' AND " + SQLiteDB.COLUMN_STORE_posted + " = '1'");
        cursCheck.moveToFirst();
        if(cursCheck.getCount() > 0) res = true;

        return res;
    }

    private class LoadCompliances extends AsyncTask<Void, Void, Boolean> {
        private String errMsg;

        @Override
        protected void onPreExecute() {
            progressDL = ProgressDialog.show(StorePreviewActivity.this, "", "Loading compliances.");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;

            try {
                complianceArrayList.clear();

                Cursor cursorCompliance = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_PJPCOMP, SQLiteDB.COLUMN_PJPCOMP_storeid + " = '" + General.selectedStore.storeID + "'");
                if (cursorCompliance.moveToFirst()) {
                    while (!cursorCompliance.isAfterLast()) {

                        int id = cursorCompliance.getInt(cursorCompliance.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_id));
                        String usercode = cursorCompliance.getString(cursorCompliance.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_usercode));
                        int storeid = cursorCompliance.getInt(cursorCompliance.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_storeid));
                        String date = cursorCompliance.getString(cursorCompliance.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_date));
                        String time = cursorCompliance.getString(cursorCompliance.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_time));
                        String longitude = cursorCompliance.getString(cursorCompliance.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_longitude));
                        String latitude = cursorCompliance.getString(cursorCompliance.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_latitude));
                        String address = cursorCompliance.getString(cursorCompliance.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_address));
                        boolean posted = cursorCompliance.getInt(cursorCompliance.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_posted)) == 1;

                        Compliance compliance = new Compliance(id, usercode, storeid, General.selectedStore.webStoreID, date, time, General.userFullName, longitude, latitude, posted);
                        compliance.address = address.trim();

                        complianceArrayList.add(compliance);
                        cursorCompliance.moveToNext();
                    }
                    result = true;
                }
                else errMsg = "No compliance found.";
                cursorCompliance.close();
            }
            catch (Exception ex) {
                errMsg = "Can't load compliance.";
                String exErr = ex.getMessage() != null ? ex.getMessage() : errMsg;
                errorLog.appendLog(exErr, TAG);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            progressDL.dismiss();
        }
    }

    private class PostCheckedPjp extends AsyncTask<Void, Void, Boolean> {
        private String errMsg;
        private String response;
        private String strFileName;

        @Override
        protected void onPreExecute() {
            progressDL = ProgressDialog.show(StorePreviewActivity.this, "", "Posting PJP compliance record. Please wait.");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;

            strFileName = General.usercode + "_" + String.valueOf(General.selectedStore.auditID) + "_" + General.getDateToday().trim().replace("/", "") + ".csv";
            File fileCheckIns = new File(AppSettings.PjpFolder, strFileName);

            try {

                if(fileCheckIns.exists()) fileCheckIns.delete();

                FileWriter fileWriter = new FileWriter(fileCheckIns);
                String strBody = "";

                    if(complianceArrayList.size() > 0) {

                        for (Compliance compliancePerStore : complianceArrayList) {

                            if(compliancePerStore.isPosted) continue;

                            strBody += General.usercode + "|";
                            strBody += String.valueOf(General.selectedStore.auditID) + "|";
                            strBody += General.selectedStore.account + "|";
                            strBody += General.selectedStore.customerCode + "|";
                            strBody += General.selectedStore.customer + "|";
                            strBody += General.selectedStore.area + "|";
                            strBody += General.selectedStore.regionCode + "|";
                            strBody += General.selectedStore.region + "|";
                            strBody += General.selectedStore.distributorCode + "|";
                            strBody += General.selectedStore.distributor + "|";
                            strBody += General.selectedStore.storeCode + "|";
                            strBody += General.selectedStore.storeName + "|";
                            strBody += compliancePerStore.date.trim() + "-" + compliancePerStore.time.trim() + "|";
                            strBody += String.valueOf(compliancePerStore.latitude) + "|";
                            strBody += String.valueOf(compliancePerStore.longitude);
                            strBody += "\n";
                        }
                    }

                fileWriter.append(strBody);
                fileWriter.flush();
                fileWriter.close();

                String attachmentName = "data";
                String attachmentFileName;
                String crlf = "\r\n";
                String twoHyphens = "--";
                String boundary =  "*****";

                response = "";

                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1024 * 1024;

                HttpURLConnection httpUrlConnection = null;

                FileInputStream fileInputStream = new FileInputStream(fileCheckIns); // text file to upload

                URL url = new URL(General.URL_UPLOAD_CHECKIN); // url to post
                httpUrlConnection = (HttpURLConnection) url.openConnection();
                httpUrlConnection.setUseCaches(false);
                httpUrlConnection.setDoOutput(true);

                httpUrlConnection.setRequestMethod("POST");
                httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
                httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
                httpUrlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                DataOutputStream request = new DataOutputStream(
                        httpUrlConnection.getOutputStream());

                request.writeBytes(twoHyphens + boundary + crlf);
                request.writeBytes("Content-Disposition: form-data; name=\"" +
                        attachmentName + "\";filename=\"" + strFileName + "\"" + crlf);
                request.writeBytes(crlf);

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // Read file
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0)
                {
                    request.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                request.writeBytes(crlf);
                request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
                request.flush();
                request.close();

                InputStream responseStream = new
                        BufferedInputStream(httpUrlConnection.getInputStream());

                BufferedReader responseStreamReader =
                        new BufferedReader(new InputStreamReader(responseStream));

                String line = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                responseStreamReader.close();

                response = stringBuilder.toString();
                responseStream.close();

                JSONObject jsonObject = new JSONObject(response);
                if(!jsonObject.isNull("msg")) {
                    int status = jsonObject.getInt("status");
                    String msg = jsonObject.getString("msg");

                    if(status == 0) {
                        response = msg.trim();
                        SetCompliancesToPosted();
                        result = true;
                    }
                    else {
                        errMsg = msg.trim();
                    }
                }
            }
            catch (IOException ex) {
                errMsg = "Slow or unstable internet connection. Please try again.";
                String exErr = ex.getMessage() != null ? ex.getMessage() : errMsg;
                errorLog.appendLog(exErr, TAG);
            }
            catch (JSONException ex) {
                ex.printStackTrace();
                errMsg = "Error in web response of server. Please try again";
                String errmsg = ex.getMessage() != null ? ex.getMessage() : errMsg;
                errorLog.appendLog(errmsg, TAG);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean bResult) {
            progressDL.dismiss();
            if(wlStayAwake.isHeld()) wlStayAwake.release();
            if(!bResult) {
                Toast.makeText(StorePreviewActivity.this, errMsg, Toast.LENGTH_LONG).show();
                return;
            }

            SetPostingSuccessful(response);
        }
    }

    private void SetCompliancesToPosted() {
            if (complianceArrayList.size() > 0) {
                for (Compliance compliancePerStore : complianceArrayList) {

                    if(compliancePerStore.isPosted) continue;

                    sqlLibrary.ExecSQLWrite("UPDATE " + SQLiteDB.TABLE_PJPCOMP
                            + " SET " + SQLiteDB.COLUMN_PJPCOMP_posted + " = '1'"
                            + " WHERE " + SQLiteDB.COLUMN_PJPCOMP_storeid + " = '" + General.selectedStore.storeID + "'"
                            + " AND " + SQLiteDB.COLUMN_PJPCOMP_id + " = '" + compliancePerStore.complianceID + "'");

                }
            }
    }

    private class LoadAuditSummary extends AsyncTask<Void, Void, Boolean> {

        private String errmsg;

        @Override
        protected void onPreExecute() {
            pDialog = ProgressDialog.show(StorePreviewActivity.this, "", "Loading audit summary.");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;

            strDetailsBody = "";
            String strSummaryBody = "";

            try {

                // STORE CATEGORY
                Cursor cursStoreCategory = sqlLibrary.RawQuerySelect("SELECT " + SQLiteDB.TABLE_STORECATEGORY + "." + SQLiteDB.COLUMN_STORECATEGORY_id + "," + SQLiteDB.COLUMN_CATEGORY_categoryorder
                        + "," + SQLiteDB.COLUMN_CATEGORY_categorydesc + "," + SQLiteDB.TABLE_STORECATEGORY + "." + SQLiteDB.COLUMN_STORECATEGORY_categoryid
                        + "," + SQLiteDB.COLUMN_STORECATEGORY_final + "," + SQLiteDB.COLUMN_STORECATEGORY_status + "," + SQLiteDB.TABLE_CATEGORY + "." + SQLiteDB.COLUMN_CATEGORY_categoryid + " AS webCategid"
                        + " FROM " + SQLiteDB.TABLE_STORECATEGORY
                        + " JOIN " + SQLiteDB.TABLE_CATEGORY + " ON " + SQLiteDB.TABLE_CATEGORY + "." + SQLiteDB.COLUMN_CATEGORY_id + " = " + SQLiteDB.TABLE_STORECATEGORY + "." + SQLiteDB.COLUMN_STORECATEGORY_categoryid
                        + " WHERE " + SQLiteDB.COLUMN_STORECATEGORY_storeid + " = " + General.selectedStore.storeID
                        + " ORDER BY " + SQLiteDB.COLUMN_CATEGORY_categoryorder);
                cursStoreCategory.moveToFirst();

                while (!cursStoreCategory.isAfterLast()) {

                    int storecategoryID = cursStoreCategory.getInt(cursStoreCategory.getColumnIndex(SQLiteDB.COLUMN_STORECATEGORY_id));
                    String categoryName = cursStoreCategory.getString(cursStoreCategory.getColumnIndex(SQLiteDB.COLUMN_CATEGORY_categorydesc)).trim().toUpperCase();
                    int categOrder = cursStoreCategory.getInt(cursStoreCategory.getColumnIndex(SQLiteDB.COLUMN_CATEGORY_categoryorder));
                    int categoryid = cursStoreCategory.getInt(cursStoreCategory.getColumnIndex(SQLiteDB.COLUMN_CATEGORY_categoryid));
                    String categoryFinal = cursStoreCategory.getString(cursStoreCategory.getColumnIndex(SQLiteDB.COLUMN_STORECATEGORY_final));
                    String categStatusno = cursStoreCategory.getString(cursStoreCategory.getColumnIndex(SQLiteDB.COLUMN_STORECATEGORY_status));
                    int webCategoryId = cursStoreCategory.getInt(cursStoreCategory.getColumnIndex("webCategid"));

                    String strCategStatus = tcrLib.GetStatus(categStatusno);
                    General.SCORE_STATUS categoryScore = tcrLib.GetScoreStatus(categoryFinal);

                    arrCategories.add(new Category(categOrder, categoryid, categoryName, String.valueOf(storecategoryID), strCategStatus, categoryScore, webCategoryId));

                    // STORE CATEGORY GROUP
                    Cursor cursStoreCategoryGroups = sqlLibrary.RawQuerySelect("SELECT tblstorecateggroup.id, tblgroup.groupdesc, " + SQLiteDB.TABLE_GROUP + "." + SQLiteDB.COLUMN_GROUP_groupid + ", " + SQLiteDB.TABLE_STORECATEGORYGROUP + "." + SQLiteDB.COLUMN_STORECATEGORYGROUP_final
                            + "," + SQLiteDB.COLUMN_STORECATEGORYGROUP_status + "," + SQLiteDB.TABLE_STORECATEGORYGROUP + "." + SQLiteDB.COLUMN_STORECATEGORYGROUP_exempt
                            + "," + SQLiteDB.TABLE_STORECATEGORYGROUP + "." + SQLiteDB.COLUMN_STORECATEGORYGROUP_initial
                            + " FROM " + SQLiteDB.TABLE_STORECATEGORYGROUP
                            + " JOIN " + SQLiteDB.TABLE_GROUP + " ON " + SQLiteDB.TABLE_GROUP + "." + SQLiteDB.COLUMN_GROUP_id + " = " + SQLiteDB.TABLE_STORECATEGORYGROUP + "." + SQLiteDB.COLUMN_STORECATEGORYGROUP_groupid
                            + " WHERE " + SQLiteDB.TABLE_STORECATEGORYGROUP + "." + SQLiteDB.COLUMN_STORECATEGORYGROUP_storecategid + " = " + storecategoryID
                            + " ORDER BY " + SQLiteDB.COLUMN_GROUP_grouporder);
                    cursStoreCategoryGroups.moveToFirst();

                    while (!cursStoreCategoryGroups.isAfterLast()) {

                        String groupDesc = cursStoreCategoryGroups.getString(cursStoreCategoryGroups.getColumnIndex(SQLiteDB.COLUMN_GROUP_groupdesc));
                        int storeCategroupID = cursStoreCategoryGroups.getInt(cursStoreCategoryGroups.getColumnIndex(SQLiteDB.COLUMN_STORECATEGORYGROUP_id));
                        String groupExempt = cursStoreCategoryGroups.getString(cursStoreCategoryGroups.getColumnIndex(SQLiteDB.COLUMN_STORECATEGORYGROUP_exempt));
                        String groupInitial = cursStoreCategoryGroups.getString(cursStoreCategoryGroups.getColumnIndex(SQLiteDB.COLUMN_STORECATEGORYGROUP_initial));
                        String groupFinal = cursStoreCategoryGroups.getString(cursStoreCategoryGroups.getColumnIndex(SQLiteDB.COLUMN_STORECATEGORYGROUP_final));

                        if (!sqlLibrary.HasQuestionsPerGroup(storeCategroupID)) {
                            cursStoreCategoryGroups.moveToNext();
                            continue;
                        }

                        // STORE QUESTION
                        Cursor cursStoreQuestion = sqlLibrary.RawQuerySelect("SELECT * FROM " + SQLiteDB.TABLE_STOREQUESTION
                                + " JOIN " + SQLiteDB.TABLE_QUESTION + " ON " + SQLiteDB.TABLE_QUESTION + "." + SQLiteDB.COLUMN_QUESTION_id + " = " + SQLiteDB.TABLE_STOREQUESTION + "." + SQLiteDB.COLUMN_STOREQUESTION_questionid
                                + " WHERE " + SQLiteDB.COLUMN_STOREQUESTION_storecategorygroupid + " = " + storeCategroupID
                                + " ORDER BY " + SQLiteDB.COLUMN_QUESTION_order);
                        cursStoreQuestion.moveToFirst();

                        String answer = "";
                        String fullAnswer = "";
                        String formtypedesc = "";
                        int finalValue = 0;
                        int exemptValue = 0;
                        int initialValue = 0;

                        while (!cursStoreQuestion.isAfterLast()) {
                            String prompt = cursStoreQuestion.getString(cursStoreQuestion.getColumnIndex(SQLiteDB.COLUMN_QUESTION_prompt)).trim();
                            int formtypeid = cursStoreQuestion.getInt(cursStoreQuestion.getColumnIndex(SQLiteDB.COLUMN_QUESTION_formtypeid));
                            int formid = cursStoreQuestion.getInt(cursStoreQuestion.getColumnIndex(SQLiteDB.COLUMN_QUESTION_formid));
                            fullAnswer = "";
                            try {
                                answer = cursStoreQuestion.getString(cursStoreQuestion.getColumnIndex(SQLiteDB.COLUMN_STOREQUESTION_answer)).trim();
                                if (!answer.equals(""))
                                    fullAnswer = sqlLibrary.GetAnswer(answer, formtypeid, formid);
                                finalValue = cursStoreQuestion.getInt(cursStoreQuestion.getColumnIndex(SQLiteDB.COLUMN_STOREQUESTION_final));
                                exemptValue = cursStoreQuestion.getInt(cursStoreQuestion.getColumnIndex(SQLiteDB.COLUMN_STOREQUESTION_exempt));
                                initialValue = cursStoreQuestion.getInt(cursStoreQuestion.getColumnIndex(SQLiteDB.COLUMN_STOREQUESTION_initial));
                            } catch (NullPointerException nex) { }

                            formtypedesc = sqlLibrary.GetFormTypeDesc(formtypeid);

                            // FOR MULTI LINE, REPLACE ENDLINES WITH WHITESPACE
                            if (formtypeid == 5) {
                                fullAnswer = fullAnswer.trim().replace("\n", " ");
                            }

                            strDetailsBody += categoryName + "|";
                            strDetailsBody += groupDesc + "|";
                            strDetailsBody += prompt + "|";
                            strDetailsBody += formtypedesc + "|";
                            strDetailsBody += fullAnswer + "|";
                            strDetailsBody += finalValue + "|";
                            strDetailsBody += exemptValue + "|";
                            strDetailsBody += initialValue;
                            strDetailsBody += "\n";

                            // FOR CONDITIONAL
                            if (formtypeid == 12) {
                                Cursor cursConditional = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_CONDITIONAL, SQLiteDB.COLUMN_CONDITIONAL_formid + " = '" + formid + "' AND " + SQLiteDB.COLUMN_CONDITIONAL_condition + " = '" + fullAnswer.trim().toUpperCase() + "'");
                                cursConditional.moveToFirst();

                                if (cursConditional.getCount() == 0) {
                                    cursConditional.close();
                                    cursStoreQuestion.moveToNext();
                                    continue;
                                }

                                String[] aCondformids = null;

                                try {
                                    aCondformids = cursConditional.getString(cursConditional.getColumnIndex(SQLiteDB.COLUMN_CONDITIONAL_conditionformsid)).trim().split("\\^");
                                } catch (NullPointerException nex) {
                                    nex.printStackTrace();
                                    cursConditional.close();
                                    cursStoreQuestion.moveToNext();
                                    continue;
                                }

                                if (aCondformids.length > 0) {

                                    for (String conformid : aCondformids) {

                                        if (conformid.equals("")) continue;

                                        Cursor cursforms = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_FORMS, SQLiteDB.COLUMN_FORMS_formid + " = '" + conformid + "'");
                                        cursforms.moveToFirst();

                                        String condPrompt = cursforms.getString(cursforms.getColumnIndex(SQLiteDB.COLUMN_FORMS_prompt));
                                        int condtypeid = cursforms.getInt(cursforms.getColumnIndex(SQLiteDB.COLUMN_FORMS_typeid));
                                        formtypedesc = sqlLibrary.GetFormTypeDesc(condtypeid);

                                        // GET CHILD ANSWERS OF CONDITIONAL
                                        Cursor cursCondAns = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_CONDITIONAL_ANSWERS, SQLiteDB.COLUMN_CONDANS_conditionalformid + " = '" + conformid + "' AND " + SQLiteDB.COLUMN_CONDANS_conditionalformtypeid + " = '" + condtypeid + "'");
                                        cursCondAns.moveToFirst();
                                        String childAnswer = "";
                                        if (cursCondAns.getCount() > 0) {
                                            childAnswer = cursCondAns.getString(cursCondAns.getColumnIndex(SQLiteDB.COLUMN_CONDANS_conditionalanswer)).trim();
                                        }

                                        cursCondAns.close();

                                        fullAnswer = childAnswer;

                                        // FOR MULTI LINE, REPLACE ENDLINES WITH WHITESPACE
                                        if (condtypeid == 5 && !childAnswer.equals("")) {
                                            fullAnswer = childAnswer.trim().replace("\n", " ");
                                        }

                                        // SINGLE ITEM
                                        if (condtypeid == 10 && !childAnswer.equals("")) { // set full answer for single item
                                            int nSingleselectId = Integer.valueOf(childAnswer);
                                            // Get option id by formid and singleselect id
                                            Cursor cursCondSingle = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_SINGLESELECT, SQLiteDB.COLUMN_SINGLESELECT_formid + " = '" + conformid + "' AND " + SQLiteDB.COLUMN_SINGLESELECT_id + " = '" + nSingleselectId + "'");
                                            cursCondSingle.moveToFirst();
                                            String condOptionid = cursCondSingle.getString(cursCondSingle.getColumnIndex(SQLiteDB.COLUMN_SINGLESELECT_optionid)).trim();
                                            fullAnswer = sqlLibrary.GetAnswer(condOptionid, condtypeid, Integer.valueOf(conformid));
                                            cursCondSingle.close();
                                        }

                                        // MULTI ITEM
                                        if (condtypeid == 9 && !childAnswer.equals("")) { // set full answer for multi item
                                            String[] arrAns = childAnswer.split(",");
                                            String ans = "";
                                            for (String cboxAns : arrAns) {
                                                Cursor cursMulti = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_MULTISELECT, SQLiteDB.COLUMN_MULTISELECT_optionid + " = '" + cboxAns + "'");
                                                cursMulti.moveToNext();
                                                ans += cursMulti.getString(cursMulti.getColumnIndex(SQLiteDB.COLUMN_MULTISELECT_option)).trim() + "-";
                                                cursMulti.close();
                                            }
                                            fullAnswer = ans;
                                        }

                                        // LABEL
                                        if (condtypeid == 1) {
                                            fullAnswer = "";
                                            finalValue = 0;
                                            exemptValue = 0;
                                            initialValue = 0;
                                        }

                                        strDetailsBody += categoryName + "|";
                                        strDetailsBody += groupDesc + "|";
                                        strDetailsBody += condPrompt + "|";
                                        strDetailsBody += formtypedesc + "|";
                                        strDetailsBody += fullAnswer + "|";
                                        strDetailsBody += finalValue + "|";
                                        strDetailsBody += exemptValue + "|";
                                        strDetailsBody += initialValue;
                                        strDetailsBody += "\n";

                                        cursforms.close();
                                    }
                                }
                                cursConditional.close();
                            }
                            cursStoreQuestion.moveToNext();
                        }

                        if (!toggleAudit) {
                            strSummaryBody += "audit_summary\n";
                            toggleAudit = true;
                        }

                        strSummaryBody += categoryName + "|" + groupDesc + "|" + groupFinal + "|" + groupExempt + "|" + groupInitial;
                        strSummaryBody += "\n";

                        cursStoreQuestion.close();
                        cursStoreCategoryGroups.moveToNext();
                    }
                    cursStoreCategoryGroups.close();
                    cursStoreCategory.moveToNext();
                }

                strDetailsBody += strSummaryBody;
                cursStoreCategory.close();
                result = true;
            }
            catch (Exception ex) {
                errmsg = ex.getMessage() != null ? ex.getMessage() : "Can't preview store audits.";
                errorLog.appendLog(errmsg, TAG);
            }

            return result;
        }


        @Override
        protected void onPostExecute(Boolean aBoolean) {
            pDialog.dismiss();
            if(!aBoolean) {
                Toast.makeText(StorePreviewActivity.this, errmsg, Toast.LENGTH_LONG).show();
                return;
            }

            lvwPreview.setAdapter(new PreviewCategoryAdapter(StorePreviewActivity.this, arrCategories));
            lvwPreview.setSmoothScrollbarEnabled(true);
            new LoadCompliances().execute();
        }
    }

    private class AsyncGenerateTextFile extends AsyncTask<Void, Void, Boolean> {

        private String errorMsg;

        @Override
        protected void onPreExecute() {
            progressDL = ProgressDialog.show(StorePreviewActivity.this, "", "Creating textfile....", true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            boolean result = false;

            if(filepathToSend.exists())
                filepathToSend.delete();

            try
            {
                FileWriter writer = new FileWriter(filepathToSend);

                String sBody = "";

                    sBody += General.usercode + "|"
                            + General.selectedStore.auditID + "|"
                            + General.selectedStore.account + "|"
                            + General.selectedStore.customerCode + "|"
                            + General.selectedStore.customer + "|"
                            + General.selectedStore.regionCode + "|"
                            + General.selectedStore.region + "|"
                            + General.selectedStore.distributorCode + "|"
                            + General.selectedStore.distributor + "|"
                            + General.selectedStore.storeCode + "|"
                            + General.selectedStore.storeName + "|"
                            + General.selectedStore.templateCode + "|"
                            + General.selectedStore.templateName + "|"
                            + String.valueOf(General.selectedStore.finalValue) + "|"
                            + strOsa + "|"
                            + strNpi + "|"
                            + strPlanogram + "|"
                            + General.selectedStore.area;

                    sBody += "\n";
                    sBody += strDetailsBody;

                writer.append(sBody);
                writer.flush();
                writer.close();
                result = true;
            }
            catch(IOException ex) {
                ex.printStackTrace();
                errorMsg = "Error in generating CSV. Please check error log.";
                String errmsg = ex.getMessage() != null ? ex.getMessage() : errorMsg;
                errorLog.appendLog(errmsg, TAG);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean s) {
            progressDL.dismiss();
            if(!s) {
                wlStayAwake.release();
                Toast.makeText(StorePreviewActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                return;
            }

            new AsyncPostFiles(filepathToSend, strFilenameToSend, General.POSTING_URL).execute();
        }
    }

    private class AsyncPostImages extends AsyncTask<Integer, Integer, Boolean> {

        private Integer maxnum = 0;
        private String response;
        private ArrayList<String> aStrFilename;
        private ArrayList<File> aFileImage;
        private String errorMsg;

        AsyncPostImages(Integer nMax, ArrayList<String> arrStrFilename, ArrayList<File> arrfilePic) {
            this.maxnum = nMax;
            this.aStrFilename = arrStrFilename;
            this.aFileImage = arrfilePic;
        }

        @Override
        protected void onPreExecute() {
            progressDL = ProgressDialog.show(StorePreviewActivity.this, "", "Posting images.");
            progressDL.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDL.setMax(this.maxnum);
            wlStayAwake.acquire();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {

            Boolean bReturn = false;

            String attachmentName = "data";
            String attachmentFileName;
            String crlf = "\r\n";
            String twoHyphens = "--";
            String boundary =  "*****";

            response = "";

            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1024 * 1024;

            HttpURLConnection httpUrlConnection = null;

            try {

                for (int i = 0; i < aFileImage.size(); i++) {

                    attachmentFileName = aStrFilename.get(i);
                    FileInputStream fileInputStream = new FileInputStream(aFileImage.get(i)); // text file to upload

                    URL url = new URL(General.POSTING_IMAGE + "/" + strImageFolder); // url to post
                    httpUrlConnection = (HttpURLConnection) url.openConnection();
                    httpUrlConnection.setUseCaches(false);
                    httpUrlConnection.setDoOutput(true);

                    httpUrlConnection.setRequestMethod("POST");
                    httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
                    httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
                    httpUrlConnection.setRequestProperty(
                            "Content-Type", "multipart/form-data;boundary=" + boundary);

                    DataOutputStream request = new DataOutputStream(
                            httpUrlConnection.getOutputStream());

                    request.writeBytes(twoHyphens + boundary + crlf);
                    request.writeBytes("Content-Disposition: form-data; name=\"" +
                            attachmentName + "\";filename=\"" + attachmentFileName + "\"" + crlf);
                    request.writeBytes(crlf);

                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // Read file
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {
                        request.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    request.writeBytes(crlf);
                    request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
                    request.flush();
                    request.close();

                    InputStream responseStream = new
                            BufferedInputStream(httpUrlConnection.getInputStream());

                    BufferedReader responseStreamReader =
                            new BufferedReader(new InputStreamReader(responseStream));

                    String line = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((line = responseStreamReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    responseStreamReader.close();

                    response = stringBuilder.toString();

                    responseStream.close();

                    JSONObject jsonObject = new JSONObject(response);
                    if(!jsonObject.isNull("msg")) {
                        int status = jsonObject.getInt("status");
                        String msg = jsonObject.getString("msg");

                        if(status == 0) {
                            response = msg.trim();
                            bReturn =  true;
                        }
                        else {
                            errorMsg = msg.trim();
                            bReturn = false;
                            break;
                        }
                    }
                }
            }
            catch (IOException ex) {
                ex.printStackTrace();
                errorMsg = "Slow or unstable internet connection. Please try again";
                String errmsg = ex.getMessage() != null ? ex.getMessage() : errorMsg;
                errorLog.appendLog(errmsg, TAG);
            }
            catch (JSONException ex) {
                ex.printStackTrace();
                errorMsg = "Error in web response from server. Please try again";
                String errmsg = ex.getMessage() != null ? ex.getMessage() : errorMsg;
                errorLog.appendLog(errmsg, TAG);
            }
            finally {
                if(httpUrlConnection != null)
                    httpUrlConnection.disconnect();
            }

            return bReturn;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            progressDL.dismiss();
            if(!aBoolean) {
                General.messageBox(StorePreviewActivity.this, "Posting of image unsuccessful", errorMsg);
                return;
            }

            new PostCheckedPjp().execute();

        }
    }

    private void SetPostingSuccessful(String msg) {
        sqlLibrary.ExecSQLWrite("UPDATE " + SQLiteDB.TABLE_STORE
                + " SET " + SQLiteDB.COLUMN_STORE_posted + " = '1', "
                + SQLiteDB.COLUMN_STORE_postingdate + " = '" + General.getDateToday() + "', " + SQLiteDB.COLUMN_STORE_postingtime + " = '" + General.getTimeToday() + "'  WHERE " + SQLiteDB.COLUMN_STORE_id + " = '" + General.selectedStore.storeID + "'");

        postDialog = new AlertDialog.Builder(StorePreviewActivity.this).create();
        postDialog.setTitle("Success");
        postDialog.setMessage("Survey is successfully posted!\n\nMessage: " + msg);
        postDialog.setCancelable(false);
        postDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                postDialog.dismiss();
                finish();
            }
        });
        postDialog.show();
    }

    // send file
    private class AsyncPostFiles extends AsyncTask<Void, Void, Boolean> {

        private final File fileToSend;
        private final String postingURL;
        private final String strFilename;
        private String errorMsg;
        private String response;
        private String auditID;

        AsyncPostFiles(File filepath, String strFilename, String url) {
            this.fileToSend = filepath;
            this.postingURL = url.trim();
            this.strFilename = strFilename;
        }

        @Override
        protected void onPreExecute() {
            progressDL = ProgressDialog.show(StorePreviewActivity.this, "", "Posting audit summary result.", true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            boolean result = false;

            String attachmentName = "data";
            String attachmentFileName = strFilename;
            String crlf = "\r\n";
            String twoHyphens = "--";
            String boundary =  "*****";

            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1024 * 1024;

            HttpURLConnection httpUrlConnection = null;

            try {

                FileInputStream fileInputStream = new FileInputStream(fileToSend); // text file to upload

                URL url = new URL(postingURL); // url to post
                httpUrlConnection = (HttpURLConnection) url.openConnection();
                httpUrlConnection.setUseCaches(false);
                httpUrlConnection.setDoOutput(true);

                httpUrlConnection.setRequestMethod("POST");
                httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
                httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
                httpUrlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                DataOutputStream request = new DataOutputStream(httpUrlConnection.getOutputStream());

                request.writeBytes(twoHyphens + boundary + crlf);
                request.writeBytes("Content-Disposition: form-data; name=\"" +
                        attachmentName + "\";filename=\"" + attachmentFileName + "\"" + crlf);
                request.writeBytes(crlf);

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // Read file
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    request.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                request.writeBytes(crlf);
                request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
                request.flush();
                request.close();

                InputStream responseStream = new BufferedInputStream(httpUrlConnection.getInputStream());

                BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));

                String line = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                responseStreamReader.close();

                response = stringBuilder.toString().trim();
                responseStream.close();

                JSONObject jsonObject = new JSONObject(response);
                if(!jsonObject.isNull("msg")) {
                    int status = jsonObject.getInt("status");
                    String msg = jsonObject.getString("msg");

                    if(status == 0) {
                        response = msg.trim();
                        auditID = jsonObject.getString("audit_id");
                        result = true;
                    }
                    else {
                        errorMsg = msg.trim();
                    }
                }
            }
            catch (JSONException ex) {
                ex.printStackTrace();
                errorMsg = "Error in web response of server. Please try again\n\nResponse: " + response;
                String errmsg = ex.getMessage() != null ? ex.getMessage() : errorMsg;
                errorLog.appendLog(errmsg, TAG);
            }
            catch (final MalformedURLException ex) {
                ex.printStackTrace();
                errorMsg = "Can't connect to web server. Please try again.";
                String errmsg = ex.getMessage() != null ? ex.getMessage() : errorMsg;
                errorLog.appendLog(errmsg, TAG);
            }
            catch (final ProtocolException ex) {
                ex.printStackTrace();
                errorMsg = "Error in web protocol. Please try again.";
                String errmsg = ex.getMessage() != null ? ex.getMessage() : errorMsg;
                errorLog.appendLog(errmsg, TAG);
            }
            catch (final IOException ex) {
                ex.printStackTrace();
                errorMsg = "Slow or unstable internet connection. Please try again";
                String errmsg = ex.getMessage() != null ? ex.getMessage() : errorMsg;
                errorLog.appendLog(errmsg, TAG);
            }
            finally {
                if(httpUrlConnection != null)
                    httpUrlConnection.disconnect();
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean bResult) {
            progressDL.dismiss();
            wlStayAwake.release();
            if(!bResult) {
                General.messageBox(StorePreviewActivity.this, "Posting unsuccessful", errorMsg);
                return;
            }

            strImageFolder = auditID;

            new AsyncPostFile2(filepathToSend, strFilenameToSend, General.POSTING_DETAILS_URL).execute();

        }
    }

    private class AsyncPostFile2 extends AsyncTask<Void, Void, Boolean> {

        private final File fileToSend;
        private final String postingURL;
        private final String strFilename;
        String response = "";
        private String errorMsg;

        AsyncPostFile2(File filepath, String strFilename, String url) {
            this.fileToSend = filepath;
            this.postingURL = url.trim();
            this.strFilename = strFilename;
        }

        @Override
        protected void onPreExecute() {
            wlStayAwake.acquire();
            progressDL = ProgressDialog.show(StorePreviewActivity.this, "", "Posting audit details.", true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            Boolean result = false;

            String attachmentName = "data";
            String attachmentFileName = strFilename;
            String crlf = "\r\n";
            String twoHyphens = "--";
            String boundary =  "*****";

            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1024 * 1024;
            HttpURLConnection httpUrlConnection = null;

            try {

                FileInputStream fileInputStream = new FileInputStream(fileToSend); // text file to upload

                URL url = new URL(postingURL); // url to post
                httpUrlConnection = (HttpURLConnection) url.openConnection();
                httpUrlConnection.setUseCaches(false);
                httpUrlConnection.setDoOutput(true);

                httpUrlConnection.setRequestMethod("POST");
                httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
                httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
                httpUrlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                DataOutputStream request = new DataOutputStream(
                        httpUrlConnection.getOutputStream());

                request.writeBytes(twoHyphens + boundary + crlf);
                request.writeBytes("Content-Disposition: form-data; name=\"" +
                        attachmentName + "\";filename=\"" + attachmentFileName + "\"" + crlf);
                request.writeBytes(crlf);

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // Read file
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0)
                {
                    request.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                request.writeBytes(crlf);
                request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
                request.flush();
                request.close();

                InputStream responseStream = new
                        BufferedInputStream(httpUrlConnection.getInputStream());

                BufferedReader responseStreamReader =
                        new BufferedReader(new InputStreamReader(responseStream));

                String line = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                responseStreamReader.close();

                response = stringBuilder.toString();
                responseStream.close();

                JSONObject jsonObject = new JSONObject(response);
                if(!jsonObject.isNull("msg")) {
                    int status = jsonObject.getInt("status");
                    String msg = jsonObject.getString("msg");

                    if(status == 0) {
                        response = msg.trim();
                        result = true;
                    }
                    else {
                        errorMsg = msg.trim();
                    }
                }
            }
            catch (JSONException ex) {
                ex.printStackTrace();
                errorMsg = "Error in web response of server. Please try again";
                String errmsg = ex.getMessage() != null ? ex.getMessage() : errorMsg;
                errorLog.appendLog(errmsg, TAG);
            }
            catch (final MalformedURLException ex) {
                ex.printStackTrace();
                errorMsg = "Can't connect to web server. Please try again.";
                String errmsg = ex.getMessage() != null ? ex.getMessage() : errorMsg;
                errorLog.appendLog(errmsg, TAG);
            }
            catch (final ProtocolException ex) {
                ex.printStackTrace();
                errorMsg = "Error in web protocol. Please try again.";
                String errmsg = ex.getMessage() != null ? ex.getMessage() : errorMsg;
                errorLog.appendLog(errmsg, TAG);
            }
            catch (final IOException ex) {
                ex.printStackTrace();
                errorMsg = "Slow or unstable internet connection. Please try again";
                String errmsg = ex.getMessage() != null ? ex.getMessage() : errorMsg;
                errorLog.appendLog(errmsg, TAG);
            }
            finally {
                if(httpUrlConnection != null)
                    httpUrlConnection.disconnect();
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            progressDL.dismiss();
            if(!aBoolean) {
                General.messageBox(StorePreviewActivity.this, "Unsuccessful posting", errorMsg);
                return;
            }

            // STORE CATEGORY
            Cursor cursStoreCategory = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_STORECATEGORY, SQLiteDB.COLUMN_STORECATEGORY_storeid + " = '" + General.selectedStore.storeID + "' AND " + SQLiteDB.COLUMN_STORECATEGORY_status + " > '0'");
            cursStoreCategory.moveToFirst();

            int totalImages = 0;
            ArrayList<String> aStrFilenames = new ArrayList<>();
            ArrayList<File> aFileImages = new ArrayList<>();

            while (!cursStoreCategory.isAfterLast()) {

                int storecategoryID = cursStoreCategory.getInt(cursStoreCategory.getColumnIndex(SQLiteDB.COLUMN_STORECATEGORY_id));

                // STORE CATEGORY GROUP
                Cursor cursStoreCategoryGroups = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_STORECATEGORYGROUP, SQLiteDB.COLUMN_STORECATEGORYGROUP_storecategid + " = '" + storecategoryID + "' AND " + SQLiteDB.COLUMN_STORECATEGORYGROUP_status + " > '0'");
                cursStoreCategoryGroups.moveToFirst();

                while (!cursStoreCategoryGroups.isAfterLast()) {

                    int storeCategroupID = cursStoreCategoryGroups.getInt(cursStoreCategoryGroups.getColumnIndex(SQLiteDB.COLUMN_STORECATEGORYGROUP_id));

                    if(!sqlLibrary.HasQuestionsPerGroup(storeCategroupID)) {
                        cursStoreCategoryGroups.moveToNext();
                        continue;
                    }

                    // STORE QUESTION IMAGES
                    Cursor cursImages = sqlLibrary.RawQuerySelect("SELECT " + SQLiteDB.COLUMN_STOREQUESTION_storecategorygroupid + "," + SQLiteDB.COLUMN_STOREQUESTION_answer + "," + SQLiteDB.COLUMN_STOREQUESTION_isAnswered
                            + " FROM " + SQLiteDB.TABLE_STOREQUESTION
                            + " JOIN " + SQLiteDB.TABLE_QUESTION + " ON " + SQLiteDB.TABLE_QUESTION + "." + SQLiteDB.COLUMN_QUESTION_id + " = " + SQLiteDB.TABLE_STOREQUESTION + "." + SQLiteDB.COLUMN_STOREQUESTION_questionid
                            + " WHERE " + SQLiteDB.COLUMN_QUESTION_formtypeid + " = '2' AND " + SQLiteDB.COLUMN_STOREQUESTION_isAnswered + " = '1' AND " + SQLiteDB.COLUMN_STOREQUESTION_storecategorygroupid + " = " + storeCategroupID
                            + " ORDER BY " + SQLiteDB.COLUMN_QUESTION_order);
                    cursImages.moveToFirst();
                    totalImages += cursImages.getCount();

                    while (!cursImages.isAfterLast()) {
                        String strFilename = cursImages.getString(cursImages.getColumnIndex(SQLiteDB.COLUMN_STOREQUESTION_answer)).trim();
                        File file = new File(AppSettings.captureFolder, strFilename);
                        aStrFilenames.add(strFilename);
                        aFileImages.add(file);
                        cursImages.moveToNext();
                    }
                    cursImages.close();

                    cursStoreCategoryGroups.moveToNext();
                }

                cursStoreCategoryGroups.close();
                cursStoreCategory.moveToNext();
            }

            cursStoreCategory.close();

            if(aFileImages.size() > 0) {
                new AsyncPostImages(totalImages, aStrFilenames, aFileImages).execute();
            }
            else {
                new PostCheckedPjp().execute();
            }
        }
    }

    // GET OSA LIST AND LOOKUP
    private boolean GetOsaListLookup(int correctAnswer, int formgroupID, int categoryid) {

        boolean res = false;

        Cursor cursOsalist = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_OSALIST, SQLiteDB.COLUMN_OSALIST_osakeygroupid + " = '" + formgroupID + "'");
        cursOsalist.moveToFirst();

        if(cursOsalist.getCount() > 0) {

            Cursor cursGetOsalookup = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_OSALOOKUP, SQLiteDB.COLUMN_OSALOOKUP_storeid + " = " + General.selectedStore.webStoreID + " AND " + SQLiteDB.COLUMN_OSALOOKUP_categoryid + " = " + categoryid);
            cursGetOsalookup.moveToFirst();

            if(cursGetOsalookup.getCount() > 0) {
                int target = cursGetOsalookup.getInt(cursGetOsalookup.getColumnIndex(SQLiteDB.COLUMN_OSALOOKUP_target));
                if(correctAnswer >= target)
                    res = true;
            }
        }

        return res;
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition( R.anim.hold, R.anim.slide_down );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
