package com.android.pplusaudit2.Dashboard;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.android.pplusaudit2.Database.SQLLibrary;
import com.android.pplusaudit2.Database.SQLiteDB;
import com.android.pplusaudit2.Debug.DebugLog;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

/**
 * Created by ULTRABOOK on 6/1/2016.
 */
public class SaveDownloadedData extends AsyncTask<Void, String, Boolean> {

    int nMaxprogress = 0;
    LineNumberReader lnReader;
    SQLiteDatabase dbase;
    SQLLibrary sql;
    String errmsg = "";
    String presentFile = "";
    ProgressDialog progressDL;

    public SaveDownloadedData() {
        dbase = new SQLiteDB(DownloadFiles.mContext).getWritableDatabase();
        sql = new SQLLibrary(DownloadFiles.mContext);
    }

    @Override
    protected void onPreExecute() {
        progressDL = new ProgressDialog(DownloadFiles.mContext);
        progressDL.setTitle("Loading");
        progressDL.setMessage("Storing downloaded data.. Please wait.");
        progressDL.setProgressStyle(progressDL.STYLE_HORIZONTAL);
        progressDL.setCancelable(false);
        progressDL.show();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        progressDL.incrementProgressBy(1);
        progressDL.setMessage(values[0]);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean result = false;

        if(DownloadFiles.storeDIR.exists()) {
            try{
                lnReader = new LineNumberReader(new FileReader(DownloadFiles.storeDIR));
                while (lnReader.readLine() != null) { }
                nMaxprogress += lnReader.getLineNumber();
            }
            catch (IOException ie) { DebugLog.log(ie.getMessage()); }
        }
        if(DownloadFiles.categoryDIR.exists()) {
            try{
                lnReader = new LineNumberReader(new FileReader(DownloadFiles.categoryDIR));
                while (lnReader.readLine() != null) { }
                nMaxprogress += lnReader.getLineNumber();
            }
            catch (IOException ie) { DebugLog.log(ie.getMessage()); }
        }
        if(DownloadFiles.groupDIR.exists()) {
            try{
                lnReader = new LineNumberReader(new FileReader(DownloadFiles.groupDIR));
                while ((lnReader.readLine()) != null) { }
                nMaxprogress += lnReader.getLineNumber();
            }
            catch (IOException ie) { DebugLog.log(ie.getMessage()); }
        }
        if(DownloadFiles.questionDIR.exists()) {
            try{
                lnReader = new LineNumberReader(new FileReader(DownloadFiles.questionDIR));
                while ((lnReader.readLine()) != null) { }
                nMaxprogress += lnReader.getLineNumber();
            }
            catch (IOException ie) { DebugLog.log(ie.getMessage()); }
        }
        if(DownloadFiles.formsDIR.exists()) {
            try{
                lnReader = new LineNumberReader(new FileReader(DownloadFiles.formsDIR));
                while ((lnReader.readLine()) != null) { }
                nMaxprogress += lnReader.getLineNumber();
            }
            catch (IOException ie) { DebugLog.log(ie.getMessage()); }
        }
        if(DownloadFiles.formtypesDIR.exists()) {
            try{
                lnReader = new LineNumberReader(new FileReader(DownloadFiles.formtypesDIR));
                while ((lnReader.readLine()) != null) { }
                nMaxprogress += lnReader.getLineNumber();
            }
            catch (IOException ie) { DebugLog.log(ie.getMessage()); }
        }
        if(DownloadFiles.singleselectDIR.exists()) {
            try{
                lnReader = new LineNumberReader(new FileReader(DownloadFiles.singleselectDIR));
                while ((lnReader.readLine()) != null) { }
                nMaxprogress += lnReader.getLineNumber();
            }
            catch (IOException ie) { DebugLog.log(ie.getMessage()); }
        }
        if(DownloadFiles.multiselectDIR.exists()) {
            try{
                lnReader = new LineNumberReader(new FileReader(DownloadFiles.multiselectDIR));
                while ((lnReader.readLine()) != null) { }
                nMaxprogress += lnReader.getLineNumber();
            }
            catch (IOException ie) { DebugLog.log(ie.getMessage()); }
        }
        if(DownloadFiles.computationalDIR.exists()) {
            try{
                lnReader = new LineNumberReader(new FileReader(DownloadFiles.computationalDIR));
                while ((lnReader.readLine()) != null) { }
                nMaxprogress += lnReader.getLineNumber();
            }
            catch (IOException ie) { DebugLog.log(ie.getMessage()); }
        }
        if(DownloadFiles.conditionalDIR.exists()) {
            try{
                lnReader = new LineNumberReader(new FileReader(DownloadFiles.conditionalDIR));
                while ((lnReader.readLine()) != null) { }
                nMaxprogress += lnReader.getLineNumber();
            }
            catch (IOException ie) { DebugLog.log(ie.getMessage()); }
        }
        if(DownloadFiles.secondarylookupDIR.exists()) {
            try{
                lnReader = new LineNumberReader(new FileReader(DownloadFiles.secondarylookupDIR));
                while ((lnReader.readLine()) != null) { }
                nMaxprogress += lnReader.getLineNumber();
            }
            catch (IOException ie) { DebugLog.log(ie.getMessage()); }
        }
        if(DownloadFiles.secondarylistDIR.exists()) {
            try{
                lnReader = new LineNumberReader(new FileReader(DownloadFiles.secondarylistDIR));
                while ((lnReader.readLine()) != null) { }
                nMaxprogress += lnReader.getLineNumber();
            }
            catch (IOException ie) { DebugLog.log(ie.getMessage()); }
        }
        if(DownloadFiles.osalistDIR.exists()) {
            try{
                lnReader = new LineNumberReader(new FileReader(DownloadFiles.osalistDIR));
                while ((lnReader.readLine()) != null) { }
                nMaxprogress += lnReader.getLineNumber();
            }
            catch (IOException ie) { DebugLog.log(ie.getMessage()); }
        }
        if(DownloadFiles.osalookupDIR.exists()) {
            try{
                lnReader = new LineNumberReader(new FileReader(DownloadFiles.osalookupDIR));
                while ((lnReader.readLine()) != null) { }
                nMaxprogress += lnReader.getLineNumber();
            }
            catch (IOException ie) { DebugLog.log(ie.getMessage()); }
        }
        if(DownloadFiles.soslistDIR.exists()) {
            try{
                lnReader = new LineNumberReader(new FileReader(DownloadFiles.soslistDIR));
                while ((lnReader.readLine()) != null) { }
                nMaxprogress += lnReader.getLineNumber();
            }
            catch (IOException ie) { DebugLog.log(ie.getMessage()); }
        }
        if(DownloadFiles.soslookupDIR.exists()) {
            try{
                lnReader = new LineNumberReader(new FileReader(DownloadFiles.soslookupDIR));
                while ((lnReader.readLine()) != null) { }
                nMaxprogress += lnReader.getLineNumber();
            }
            catch (IOException ie) { DebugLog.log(ie.getMessage()); }
        }
        if(DownloadFiles.imageListDIR.exists()) {
            try{
                lnReader = new LineNumberReader(new FileReader(DownloadFiles.imageListDIR));
                while ((lnReader.readLine()) != null) { }
                nMaxprogress += lnReader.getLineNumber();
            }
            catch (IOException ie) { DebugLog.log(ie.getMessage()); }
        }
        if(DownloadFiles.npiDIR.exists()) {
            try{
                lnReader = new LineNumberReader(new FileReader(DownloadFiles.npiDIR));
                while ((lnReader.readLine()) != null) { }
                nMaxprogress += lnReader.getLineNumber();
            }
            catch (IOException ie) { DebugLog.log(ie.getMessage()); }
        }
        if(DownloadFiles.planogramDIR.exists()) {
            try{
                lnReader = new LineNumberReader(new FileReader(DownloadFiles.planogramDIR));
                while ((lnReader.readLine()) != null) { }
                nMaxprogress += lnReader.getLineNumber();
            }
            catch (IOException ie) { DebugLog.log(ie.getMessage()); }
        }
        if(DownloadFiles.pcategoryDIR.exists()) {
            try{
                lnReader = new LineNumberReader(new FileReader(DownloadFiles.pcategoryDIR));
                while ((lnReader.readLine()) != null) { }
                nMaxprogress += lnReader.getLineNumber();
            }
            catch (IOException ie) { DebugLog.log(ie.getMessage()); }
        }
        if(DownloadFiles.pgroupDIR.exists()) {
            try{
                lnReader = new LineNumberReader(new FileReader(DownloadFiles.pgroupDIR));
                while ((lnReader.readLine()) != null) { }
                nMaxprogress += lnReader.getLineNumber();
            }
            catch (IOException ie) { DebugLog.log(ie.getMessage()); }
        }


        progressDL.setMax(nMaxprogress);

        try {

            // STORES
            if(DownloadFiles.storeDIR.exists()) {
                sql.TruncateTable(SQLiteDB.TABLE_STORE);

                presentFile = DownloadFiles.storeDIR.getPath();

                String[] afields = {
                        SQLiteDB.COLUMN_STORE_storeid,
                        SQLiteDB.COLUMN_STORE_name,
                        SQLiteDB.COLUMN_STORE_gradematrixid,
                        SQLiteDB.COLUMN_STORE_audittempid,
                        SQLiteDB.COLUMN_STORE_templatename,
                        SQLiteDB.COLUMN_STORE_status,
                        SQLiteDB.COLUMN_STORE_initial,
                        SQLiteDB.COLUMN_STORE_exempt,
                        SQLiteDB.COLUMN_STORE_final,
                        SQLiteDB.COLUMN_STORE_startdate,
                        SQLiteDB.COLUMN_STORE_enddate,
                        SQLiteDB.COLUMN_STORE_storecode,
                        SQLiteDB.COLUMN_STORE_account,
                        SQLiteDB.COLUMN_STORE_customercode,
                        SQLiteDB.COLUMN_STORE_customer,
                        SQLiteDB.COLUMN_STORE_regioncode,
                        SQLiteDB.COLUMN_STORE_region,
                        SQLiteDB.COLUMN_STORE_distributorcode,
                        SQLiteDB.COLUMN_STORE_distributor,
                        SQLiteDB.COLUMN_STORE_templatecode,
                        SQLiteDB.COLUMN_STORE_auditid
                };

                String sqlinsertStore = sql.createInsertBulkQuery(SQLiteDB.TABLE_STORE, afields);
                SQLiteStatement sqlstatementStore = dbase.compileStatement(sqlinsertStore); // insert into tblsample (fields1,fields2)
                dbase.beginTransaction();
                BufferedReader bReader = new BufferedReader(new FileReader(DownloadFiles.storeDIR));

                String line;
                line = bReader.readLine();

                while ((line = bReader.readLine()) != null) {
                    final String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                    sqlstatementStore.clearBindings();
                    for (int i = 0; i < afields.length; i++) {
                        sqlstatementStore.bindString((i+1), values[i].trim().replace("\"",""));
                    }
                    sqlstatementStore.execute();

                    publishProgress("Saving Store data.." + values[1].trim());
                }

                dbase.setTransactionSuccessful();
                dbase.endTransaction();
            }


            // CATEGORY
            if(DownloadFiles.categoryDIR.exists()) {
                sql.TruncateTable(SQLiteDB.TABLE_CATEGORY);

                presentFile = DownloadFiles.categoryDIR.getPath();

                String[] afields = {
                        SQLiteDB.COLUMN_CATEGORY_id,
                        SQLiteDB.COLUMN_CATEGORY_audittempid,
                        SQLiteDB.COLUMN_CATEGORY_categoryorder,
                        SQLiteDB.COLUMN_CATEGORY_categoryid,
                        SQLiteDB.COLUMN_CATEGORY_categorydesc
                };

                String sqlinsertCategory = sql.createInsertBulkQuery(SQLiteDB.TABLE_CATEGORY, afields);

                SQLiteStatement sqlstatementCategory = dbase.compileStatement(sqlinsertCategory); // insert into tblsample (fields1,fields2)
                dbase.beginTransaction();

                BufferedReader bReader = new BufferedReader(new FileReader(DownloadFiles.categoryDIR));

                String line;

                line = bReader.readLine();

                while ((line = bReader.readLine()) != null) {
                    final String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                    sqlstatementCategory.clearBindings();
                    for (int i = 0; i < afields.length; i++) {
                        sqlstatementCategory.bindString((i+1), values[i].trim().replace("\"",""));
                    }
                    sqlstatementCategory.execute();

                    publishProgress("Saving Category data.." + values[1].trim());
                }
                dbase.setTransactionSuccessful();
                dbase.endTransaction();
            }


            // GROUP
            if(DownloadFiles.groupDIR.exists()) {
                sql.TruncateTable(SQLiteDB.TABLE_GROUP);

                presentFile = DownloadFiles.groupDIR.getPath();

                String[] afields = {
                        SQLiteDB.COLUMN_GROUP_id,
                        SQLiteDB.COLUMN_GROUP_audittempid,
                        SQLiteDB.COLUMN_GROUP_categoryid,
                        SQLiteDB.COLUMN_GROUP_grouporder,
                        SQLiteDB.COLUMN_GROUP_groupid,
                        SQLiteDB.COLUMN_GROUP_groupdesc
                };

                String sqlinsertGroup = sql.createInsertBulkQuery(SQLiteDB.TABLE_GROUP, afields);

                SQLiteStatement sqlstatementGroup = dbase.compileStatement(sqlinsertGroup); // insert into tblsample (fields1,fields2)
                dbase.beginTransaction();

                BufferedReader bReader = new BufferedReader(new FileReader(DownloadFiles.groupDIR));

                String line;

                line = bReader.readLine();

                while ((line = bReader.readLine()) != null) {
                    final String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                    sqlstatementGroup.clearBindings();
                    for (int i = 0; i < afields.length; i++) {
                        sqlstatementGroup.bindString((i+1), values[i].trim().replace("\"",""));
                    }
                    sqlstatementGroup.execute();

                    publishProgress("Saving Group data.." + values[1].trim());
                }
                dbase.setTransactionSuccessful();
                dbase.endTransaction();
            }

            // QUESTIONS
            if(DownloadFiles.questionDIR.exists()) {
                sql.TruncateTable(SQLiteDB.TABLE_QUESTION);

                presentFile = DownloadFiles.questionDIR.getPath();

                String[] afields = {
                        SQLiteDB.COLUMN_QUESTION_questionid,
                        SQLiteDB.COLUMN_QUESTION_order,
                        SQLiteDB.COLUMN_QUESTION_groupid,
                        SQLiteDB.COLUMN_QUESTION_audittempid,
                        SQLiteDB.COLUMN_QUESTION_formid,
                        SQLiteDB.COLUMN_QUESTION_formtypeid,
                        SQLiteDB.COLUMN_QUESTION_prompt,
                        SQLiteDB.COLUMN_QUESTION_required,
                        SQLiteDB.COLUMN_QUESTION_expectedans,
                        SQLiteDB.COLUMN_QUESTION_exempt,
                        SQLiteDB.COLUMN_QUESTION_brandpic,
                        SQLiteDB.COLUMN_QUESTION_defaultans
                };

                String sqlinsertQuestions = sql.createInsertBulkQuery(SQLiteDB.TABLE_QUESTION, afields);
                SQLiteStatement sqlstatementQuestions = dbase.compileStatement(sqlinsertQuestions);
                dbase.beginTransaction();

                BufferedReader bReader = new BufferedReader(new FileReader(DownloadFiles.questionDIR));

                String line;

                line = bReader.readLine();

                while ((line = bReader.readLine()) != null) {
                    final String[] valuesquestion = line.trim().split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1); // split with comma delimeter, not including inside the ""

                    sqlstatementQuestions.clearBindings();
                    for (int i = 0; i < valuesquestion.length; i++) {
                        sqlstatementQuestions.bindString((i + 1), valuesquestion[i].trim().replace("\"", ""));
                    }
                    sqlstatementQuestions.execute();

                    //sql.AddRecord(SQLiteDB.TABLE_QUESTION, afields, avalues);

                    publishProgress("Saving questions data.. " + valuesquestion[4]);
                }
                dbase.setTransactionSuccessful();
                dbase.endTransaction();
            }

            // FORMS
            if(DownloadFiles.formsDIR.exists()) {
                sql.TruncateTable(SQLiteDB.TABLE_FORMS);

                presentFile = DownloadFiles.formsDIR.getPath();

                String[] afields = {
                        SQLiteDB.COLUMN_FORMS_formid,
                        SQLiteDB.COLUMN_FORMS_audittempid,
                        SQLiteDB.COLUMN_FORMS_typeid,
                        SQLiteDB.COLUMN_FORMS_prompt,
                        SQLiteDB.COLUMN_FORMS_required,
                        SQLiteDB.COLUMN_FORMS_expected,
                        SQLiteDB.COLUMN_FORMS_exempt,
                        SQLiteDB.COLUMN_FORMS_picture,
                        SQLiteDB.COLUMN_FORMS_defaultans
                };

                String sqlinsertForms = sql.createInsertBulkQuery(SQLiteDB.TABLE_FORMS, afields);
                SQLiteStatement sqlstatementForms = dbase.compileStatement(sqlinsertForms);
                dbase.beginTransaction();

                BufferedReader brForms = new BufferedReader(new FileReader(DownloadFiles.formsDIR));

                String line;

                line = brForms.readLine();

                while ((line = brForms.readLine()) != null) {
                    String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                    sqlstatementForms.clearBindings();
                    for (int i = 0; i < values.length; i++) {
                        sqlstatementForms.bindString((i+1), values[i].trim().replace("\"",""));
                    }
                    sqlstatementForms.execute();

                    publishProgress("Saving forms data..");
                }
                dbase.setTransactionSuccessful();
                dbase.endTransaction();
            }


            // FORM TYPES
            if(DownloadFiles.formtypesDIR.exists()) {
                sql.TruncateTable(SQLiteDB.TABLE_FORMTYPE);

                presentFile = DownloadFiles.formtypesDIR.getPath();

                String[] afields = {
                        SQLiteDB.COLUMN_FORMTYPE_code,
                        SQLiteDB.COLUMN_FORMTYPE_desc
                };

                String sqlinsertFormtypes = sql.createInsertBulkQuery(SQLiteDB.TABLE_FORMTYPE, afields);
                SQLiteStatement sqlstatementFormtypes = dbase.compileStatement(sqlinsertFormtypes);
                dbase.beginTransaction();

                BufferedReader brFormtypes = new BufferedReader(new FileReader(DownloadFiles.formtypesDIR));

                String line;

                line = brFormtypes.readLine();

                while ((line = brFormtypes.readLine()) != null) {
                    final String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                    sqlstatementFormtypes.clearBindings();
                    for (int i = 0; i < values.length; i++) {
                        sqlstatementFormtypes.bindString((i+1), values[i].trim().replace("\"",""));
                    }
                    sqlstatementFormtypes.execute();

                    publishProgress("Saving form types..");
                }
                dbase.setTransactionSuccessful();
                dbase.endTransaction();
            }

            // SINGLE SELECT
            if(DownloadFiles.singleselectDIR.exists()) {
                sql.TruncateTable(SQLiteDB.TABLE_SINGLESELECT);

                presentFile = DownloadFiles.singleselectDIR.getPath();

                String[] afields = {
                        SQLiteDB.COLUMN_SINGLESELECT_formid,
                        SQLiteDB.COLUMN_SINGLESELECT_optionid,
                        SQLiteDB.COLUMN_SINGLESELECT_option
                };

                String sqlinsertSingle = sql.createInsertBulkQuery(SQLiteDB.TABLE_SINGLESELECT, afields);
                SQLiteStatement sqlstatementSingle = dbase.compileStatement(sqlinsertSingle);
                dbase.beginTransaction();

                BufferedReader bSingleselect = new BufferedReader(new FileReader(DownloadFiles.singleselectDIR));
                String line;

                line = bSingleselect.readLine();

                while ((line = bSingleselect.readLine()) != null) {
                    final String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                    sqlstatementSingle.clearBindings();
                    for (int i = 0; i < values.length; i++)
                    {
                        sqlstatementSingle.bindString((i+1), values[i].trim().replace("\"",""));
                    }
                    sqlstatementSingle.execute();

                    publishProgress("Saving single select data..");
                }
                dbase.setTransactionSuccessful();
                dbase.endTransaction();
            }

            // MULTI SELECT
            if(DownloadFiles.multiselectDIR.exists()) {
                sql.TruncateTable(SQLiteDB.TABLE_MULTISELECT);

                presentFile = DownloadFiles.multiselectDIR.getPath();

                String[] afields = {
                        SQLiteDB.COLUMN_MULTISELECT_formid,
                        SQLiteDB.COLUMN_MULTISELECT_optionid,
                        SQLiteDB.COLUMN_MULTISELECT_option
                };

                String sqlinsertMulti = sql.createInsertBulkQuery(SQLiteDB.TABLE_MULTISELECT, afields);
                SQLiteStatement sqlstatementMulti = dbase.compileStatement(sqlinsertMulti);
                dbase.beginTransaction();

                BufferedReader brMultiSelect = new BufferedReader(new FileReader(DownloadFiles.multiselectDIR));
                String line;
                line = brMultiSelect.readLine();

                while ((line = brMultiSelect.readLine()) != null) {
                    final String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                    sqlstatementMulti.clearBindings();
                    for (int i = 0; i < values.length; i++) {
                        sqlstatementMulti.bindString((i+1), values[i].trim().replace("\"",""));
                    }
                    sqlstatementMulti.execute();

                    publishProgress("Saving multi select data..");
                }
                dbase.setTransactionSuccessful();
                dbase.endTransaction();
            }

            // COMPUTATIONAL
            if(DownloadFiles.computationalDIR.exists()) {
                sql.TruncateTable(SQLiteDB.TABLE_COMPUTATIONAL);

                presentFile = DownloadFiles.computationalDIR.getPath();

                String[] afields = {
                        SQLiteDB.COLUMN_COMPUTATIONAL_formid,
                        SQLiteDB.COLUMN_COMPUTATIONAL_formula
                };

                String sqlinsertComp = sql.createInsertBulkQuery(SQLiteDB.TABLE_COMPUTATIONAL, afields);
                SQLiteStatement sqlstatementComp = dbase.compileStatement(sqlinsertComp);
                dbase.beginTransaction();

                BufferedReader brComputational = new BufferedReader(new FileReader(DownloadFiles.computationalDIR));
                String line;
                line = brComputational.readLine();

                while ((line = brComputational.readLine()) != null) {
                    final String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                    sqlstatementComp.clearBindings();
                    for (int i = 0; i < values.length; i++) {
                        sqlstatementComp.bindString((i+1), values[i].trim().replace("\"",""));
                    }
                    sqlstatementComp.execute();

                    publishProgress("Saving computational data..");
                }
                dbase.setTransactionSuccessful();
                dbase.endTransaction();
            }

            // CONDITIONAL
            if(DownloadFiles.conditionalDIR.exists()) {
                sql.TruncateTable(SQLiteDB.TABLE_CONDITIONAL);

                presentFile = DownloadFiles.conditionalDIR.getPath();

                String[] afields = {
                        SQLiteDB.COLUMN_CONDITIONAL_formid,
                        SQLiteDB.COLUMN_CONDITIONAL_condition,
                        SQLiteDB.COLUMN_CONDITIONAL_conditionformsid,
                        SQLiteDB.COLUMN_CONDITIONAL_optionid
                };

                String sqlinsertCond = sql.createInsertBulkQuery(SQLiteDB.TABLE_CONDITIONAL, afields);
                SQLiteStatement sqlstatementCond = dbase.compileStatement(sqlinsertCond);
                dbase.beginTransaction();

                BufferedReader brConditional = new BufferedReader(new FileReader(DownloadFiles.conditionalDIR));
                String line;
                line = brConditional.readLine();

                while ((line = brConditional.readLine()) != null) {
                    final String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                    sqlstatementCond.clearBindings();
                    for (int i = 0; i < values.length; i++) {
                        sqlstatementCond.bindString((i+1), values[i].trim().replace("\"",""));
                    }
                    sqlstatementCond.execute();

                    publishProgress("Saving conditional data..");
                }
                dbase.setTransactionSuccessful();
                dbase.endTransaction();
            }


            // SECONDARY KEY LIST
            if(DownloadFiles.secondarylistDIR.exists()) {
                sql.TruncateTable(SQLiteDB.TABLE_SECONDARYKEYLIST);

                presentFile = DownloadFiles.secondarylistDIR.getPath();

                String[] afields = {
                        SQLiteDB.COLUMN_SECONDARYKEYLIST_keygroupid
                };

                String sqlinsertKeyList = sql.createInsertBulkQuery(SQLiteDB.TABLE_SECONDARYKEYLIST, afields);
                SQLiteStatement sqlstatementKeyList = dbase.compileStatement(sqlinsertKeyList); // insert into tblsample (fields1,fields2)
                dbase.beginTransaction();

                BufferedReader bReader = new BufferedReader(new FileReader(DownloadFiles.secondarylistDIR));

                String line;

                line = bReader.readLine();

                while ((line = bReader.readLine()) != null) {
                    final String[] values = line.split(",");


                    sqlstatementKeyList.clearBindings();
                    for (int i = 0; i < afields.length; i++) {
                        sqlstatementKeyList.bindString((i+1), values[i].trim().replace("\"",""));
                    }
                    sqlstatementKeyList.execute();

                    publishProgress("Saving Secondary Keylist data..");
                }
                dbase.setTransactionSuccessful();
                dbase.endTransaction();
            }

            // SECONDARY DISPLAY
            if(DownloadFiles.secondarylookupDIR.exists()) {
                sql.TruncateTable(SQLiteDB.TABLE_SECONDARYDISP);

                presentFile = DownloadFiles.secondarylookupDIR.getPath();

                String[] afields = {
                        SQLiteDB.COLUMN_SECONDARYDISP_storeid,
                        SQLiteDB.COLUMN_SECONDARYDISP_categoryid,
                        SQLiteDB.COLUMN_SECONDARYDISP_brand
                };

                String sqlinsertSecDisp = sql.createInsertBulkQuery(SQLiteDB.TABLE_SECONDARYDISP, afields);
                SQLiteStatement sqlstatementSecDisp = dbase.compileStatement(sqlinsertSecDisp); // insert into tblsample (fields1,fields2)
                dbase.beginTransaction();

                BufferedReader bReader = new BufferedReader(new FileReader(DownloadFiles.secondarylookupDIR));

                String line;

                line = bReader.readLine();

                while ((line = bReader.readLine()) != null) {
                    final String[] values = line.split(",");


                    sqlstatementSecDisp.clearBindings();
                    for (int i = 0; i < afields.length; i++) {
                        sqlstatementSecDisp.bindString((i+1), values[i].trim().replace("\"",""));
                    }
                    sqlstatementSecDisp.execute();

                    publishProgress("Saving Secondary display data..");
                }
                dbase.setTransactionSuccessful();
                dbase.endTransaction();
            }


            // OSA LIST
            if(DownloadFiles.osalistDIR.exists()) {
                sql.TruncateTable(SQLiteDB.TABLE_OSALIST);

                presentFile = DownloadFiles.osalistDIR.getPath();

                String[] afields = {
                        SQLiteDB.COLUMN_OSALIST_osakeygroupid
                };

                String sqlinsertOsalist = sql.createInsertBulkQuery(SQLiteDB.TABLE_OSALIST, afields);
                SQLiteStatement sqlstatementOsalist = dbase.compileStatement(sqlinsertOsalist); // insert into tblsample (fields1,fields2)
                dbase.beginTransaction();

                BufferedReader bReader = new BufferedReader(new FileReader(DownloadFiles.osalistDIR));

                String line;

                line = bReader.readLine();

                while ((line = bReader.readLine()) != null) {
                    final String[] values = line.split(",");

                    sqlstatementOsalist.clearBindings();
                    for (int i = 0; i < afields.length; i++) {
                        sqlstatementOsalist.bindString((i+1), values[i].trim().replace("\"",""));
                    }
                    sqlstatementOsalist.execute();

                    publishProgress("Saving OSA Lists data..");
                }
                dbase.setTransactionSuccessful();
                dbase.endTransaction();
            }


            // OSA LOOKUP
            if(DownloadFiles.osalookupDIR.exists()) {
                sql.TruncateTable(SQLiteDB.TABLE_OSALOOKUP);

                presentFile = DownloadFiles.osalookupDIR.getPath();

                String[] afields = {
                        SQLiteDB.COLUMN_OSALOOKUP_storeid,
                        SQLiteDB.COLUMN_OSALOOKUP_categoryid,
                        SQLiteDB.COLUMN_OSALOOKUP_target,
                        SQLiteDB.COLUMN_OSALOOKUP_total,
                        SQLiteDB.COLUMN_OSALOOKUP_lookupid
                };

                String sqlinsertOsalookup = sql.createInsertBulkQuery(SQLiteDB.TABLE_OSALOOKUP, afields);
                SQLiteStatement sqlstatementOsalookup = dbase.compileStatement(sqlinsertOsalookup); // insert into tblsample (fields1,fields2)
                dbase.beginTransaction();

                BufferedReader bReader = new BufferedReader(new FileReader(DownloadFiles.osalookupDIR));

                String line;

                line = bReader.readLine();

                while ((line = bReader.readLine()) != null) {
                    final String[] values = line.split(",");


                    sqlstatementOsalookup.clearBindings();
                    for (int i = 0; i < afields.length; i++) {
                        sqlstatementOsalookup.bindString((i+1), values[i].trim().replace("\"",""));
                    }
                    sqlstatementOsalookup.execute();

                    publishProgress("Saving OSA lookup data..");
                }
                dbase.setTransactionSuccessful();
                dbase.endTransaction();
            }


            // SOS LIST
            if(DownloadFiles.soslistDIR.exists()) {
                sql.TruncateTable(SQLiteDB.TABLE_SOSLIST);

                presentFile = DownloadFiles.soslistDIR.getPath();

                String[] afields = {
                        SQLiteDB.COLUMN_SOSLIST_soskeygroupid
                };

                String sqlinsertSoslist = sql.createInsertBulkQuery(SQLiteDB.TABLE_SOSLIST, afields);
                SQLiteStatement sqlstatementSoslist = dbase.compileStatement(sqlinsertSoslist); // insert into tblsample (fields1,fields2)
                dbase.beginTransaction();

                BufferedReader bReader = new BufferedReader(new FileReader(DownloadFiles.soslistDIR));

                String line;

                line = bReader.readLine();

                while ((line = bReader.readLine()) != null) {
                    final String[] values = line.split(",");

                    sqlstatementSoslist.clearBindings();
                    for (int i = 0; i < afields.length; i++) {
                        sqlstatementSoslist.bindString((i+1), values[i].trim().replace("\"",""));
                    }
                    sqlstatementSoslist.execute();

                    publishProgress("Saving SOS Lists data..");
                }
                dbase.setTransactionSuccessful();
                dbase.endTransaction();
            }


            // SOS LOOKUP
            if(DownloadFiles.soslookupDIR.exists()) {
                sql.TruncateTable(SQLiteDB.TABLE_SOSLOOKUP);

                presentFile = DownloadFiles.soslookupDIR.getPath();

                String[] afields = {
                        SQLiteDB.COLUMN_SOSLOOKUP_storeid,
                        SQLiteDB.COLUMN_SOSLOOKUP_categoryid,
                        SQLiteDB.COLUMN_SOSLOOKUP_sosid,
                        SQLiteDB.COLUMN_SOSLOOKUP_less,
                        SQLiteDB.COLUMN_SOSLOOKUP_value,
                        SQLiteDB.COLUMN_SOSLOOKUP_lookupid
                };

                String sqlinsertSoslookup = sql.createInsertBulkQuery(SQLiteDB.TABLE_SOSLOOKUP, afields);
                SQLiteStatement sqlstatementSoslookup = dbase.compileStatement(sqlinsertSoslookup); // insert into tblsample (fields1,fields2)
                dbase.beginTransaction();

                BufferedReader bReader = new BufferedReader(new FileReader(DownloadFiles.soslookupDIR));

                String line;

                line = bReader.readLine();

                while ((line = bReader.readLine()) != null) {
                    final String[] values = line.split(",");


                    sqlstatementSoslookup.clearBindings();
                    for (int i = 0; i < afields.length; i++) {
                        sqlstatementSoslookup.bindString((i+1), values[i].trim().replace("\"",""));
                    }
                    sqlstatementSoslookup.execute();

                    publishProgress("Saving SOS lookup data..");
                }
                dbase.setTransactionSuccessful();
                dbase.endTransaction();
            }

            // IMAGE LISTS
            if(DownloadFiles.imageListDIR.exists()) {
                sql.TruncateTable(SQLiteDB.TABLE_PICTURES);

                presentFile = DownloadFiles.imageListDIR.getPath();

                String[] afields = {
                        SQLiteDB.COLUMN_PICTURES_name
                };

                String sqlinsertPictures = sql.createInsertBulkQuery(SQLiteDB.TABLE_PICTURES, afields);
                SQLiteStatement sqlstatementPictures = dbase.compileStatement(sqlinsertPictures); // insert into tblsample (fields1,fields2)
                dbase.beginTransaction();

                BufferedReader bReader = new BufferedReader(new FileReader(DownloadFiles.imageListDIR));

                String line;

                line = bReader.readLine();

                while ((line = bReader.readLine()) != null) {
                    final String[] values = line.split(",");

                    sqlstatementPictures.clearBindings();
                    for (int i = 0; i < afields.length; i++) {
                        sqlstatementPictures.bindString((i+1), values[i].trim().replace("\"",""));
                    }
                    sqlstatementPictures.execute();

                    publishProgress("Saving brand images data..");
                }
                dbase.setTransactionSuccessful();
                dbase.endTransaction();
            }

            // NPI LIST
            if(DownloadFiles.npiDIR.exists()) {
                sql.TruncateTable(SQLiteDB.TABLE_NPI);

                presentFile = DownloadFiles.npiDIR.getPath();

                String[] afields = {
                        SQLiteDB.COLUMN_NPI_keygroupid
                };

                String strInsertNpi = sql.createInsertBulkQuery(SQLiteDB.TABLE_NPI, afields);
                SQLiteStatement sqLiteStatement = dbase.compileStatement(strInsertNpi); // insert into tblsample (fields1,fields2)
                dbase.beginTransaction();

                BufferedReader bReader = new BufferedReader(new FileReader(DownloadFiles.npiDIR));

                String line;

                line = bReader.readLine();

                while ((line = bReader.readLine()) != null) {
                    final String[] values = line.split(",");

                    sqLiteStatement.clearBindings();
                    for (int i = 0; i < afields.length; i++) {
                        sqLiteStatement.bindString((i+1), values[i].trim().replace("\"",""));
                    }
                    sqLiteStatement.execute();

                    publishProgress("Saving NPI Lists data..");
                }
                dbase.setTransactionSuccessful();
                dbase.endTransaction();
            }

            // PLANOGRAM LIST
            if(DownloadFiles.planogramDIR.exists()) {
                sql.TruncateTable(SQLiteDB.TABLE_PLANOGRAM);

                presentFile = DownloadFiles.planogramDIR.getPath();

                String[] afields = {
                        SQLiteDB.COLUMN_PLANOGRAM_keygroupid
                };

                String strInsertPlanogram = sql.createInsertBulkQuery(SQLiteDB.TABLE_PLANOGRAM, afields);
                SQLiteStatement sqLiteStatement = dbase.compileStatement(strInsertPlanogram); // insert into tblsample (fields1,fields2)
                dbase.beginTransaction();

                BufferedReader bReader = new BufferedReader(new FileReader(DownloadFiles.planogramDIR));

                String line;

                line = bReader.readLine();

                while ((line = bReader.readLine()) != null) {
                    final String[] values = line.split(",");

                    sqLiteStatement.clearBindings();
                    for (int i = 0; i < afields.length; i++) {
                        sqLiteStatement.bindString((i+1), values[i].trim().replace("\"",""));
                    }
                    sqLiteStatement.execute();

                    publishProgress("Saving Planogram Lists data..");
                }
                dbase.setTransactionSuccessful();
                dbase.endTransaction();
            }

            // PERFECT CATEGORY LIST
            if(DownloadFiles.pcategoryDIR.exists()) {
                sql.TruncateTable(SQLiteDB.TABLE_PERFECT_CATEGORY);

                presentFile = DownloadFiles.pcategoryDIR.getPath();

                String[] afields = {
                        SQLiteDB.COLUMN_PCATEGORY_categoryid,
                        //SQLiteDB.COLUMN_PCATEGORY_audittempid
                };

                String strInsertPcategory = sql.createInsertBulkQuery(SQLiteDB.TABLE_PERFECT_CATEGORY, afields);
                SQLiteStatement sqLiteStatement = dbase.compileStatement(strInsertPcategory); // insert into tblsample (fields1,fields2)
                dbase.beginTransaction();

                BufferedReader bReader = new BufferedReader(new FileReader(DownloadFiles.pcategoryDIR));

                String line;

                line = bReader.readLine();

                while ((line = bReader.readLine()) != null) {
                    final String[] values = line.split(",");

                    sqLiteStatement.clearBindings();
                    for (int i = 0; i < afields.length; i++) {
                        sqLiteStatement.bindString((i+1), values[i].trim().replace("\"",""));
                    }
                    sqLiteStatement.execute();

                    publishProgress("Saving Perfect Category Lists data..");
                }
                dbase.setTransactionSuccessful();
                dbase.endTransaction();
            }


            // PERFECT GROUP LIST
            if(DownloadFiles.pgroupDIR.exists()) {
                sql.TruncateTable(SQLiteDB.TABLE_PERFECT_GROUP);

                presentFile = DownloadFiles.pgroupDIR.getPath();

                String[] afields = {
                        SQLiteDB.COLUMN_PGROUP_groupid
                };

                String strInsertgroup = sql.createInsertBulkQuery(SQLiteDB.TABLE_PERFECT_GROUP, afields);
                SQLiteStatement sqLiteStatement = dbase.compileStatement(strInsertgroup); // insert into tblsample (fields1,fields2)
                dbase.beginTransaction();

                BufferedReader bReader = new BufferedReader(new FileReader(DownloadFiles.pgroupDIR));

                String line;

                line = bReader.readLine();

                while ((line = bReader.readLine()) != null) {
                    final String[] values = line.split(",");

                    sqLiteStatement.clearBindings();
                    for (int i = 0; i < afields.length; i++) {
                        sqLiteStatement.bindString((i+1), values[i].trim().replace("\"",""));
                    }
                    sqLiteStatement.execute();

                    publishProgress("Saving Perfect Group Lists data..");
                }
                dbase.setTransactionSuccessful();
                dbase.endTransaction();
            }

            result = true;
        }
        catch (FileNotFoundException fex)
        {
            fex.printStackTrace();
            Log.e("Exception", fex.getMessage());
            errmsg = fex.getMessage() + ", file not found.\nFILE: " + presentFile;
            dbase.setTransactionSuccessful();
            dbase.endTransaction();
        }
        catch (IOException iex) {
            iex.printStackTrace();
            Log.e("Exception", iex.getMessage());
            errmsg = iex.getMessage() + "\nFILE: " + presentFile;
            dbase.setTransactionSuccessful();
            dbase.endTransaction();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Log.e("Exception", ex.getMessage());
            errmsg = ex.getMessage() + ", Some files are corrupted.\nFILE: " + presentFile;
            dbase.setTransactionSuccessful();
            dbase.endTransaction();
        }

        return result;
    }

    @Override
    protected void onPostExecute(Boolean bResult) {
        progressDL.dismiss();
        if(DashboardActivity.wlStayAwake.isHeld())
            DashboardActivity.wlStayAwake.release();
        // CLOSE DATABASE CONN
        if(dbase.isOpen()) dbase.close();

        if(!bResult) {
            Toast.makeText(DownloadFiles.mContext, errmsg, Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(DownloadFiles.mContext)
                .setTitle("Success")
                .setMessage("Successfully downloaded the updated masterfile.")
                .setCancelable(false)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();
    }
}
