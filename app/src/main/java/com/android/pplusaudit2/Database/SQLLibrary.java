package com.android.pplusaudit2.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.widget.Toast;

import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.General;

import java.sql.SQLException;
import java.util.Date;

/**
 * Created by ULTRABOOK on 9/30/2015.
 */
public class SQLLibrary {

    private SQLiteDB dbHelper;
    private Context mContext;

    public SQLLibrary(Context context) {
        dbHelper = new SQLiteDB(context);
        mContext = context;
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(context, General.errlogFile));
    }

    /** ADDING RECORDS ********************************************************/
    public boolean AddRecord(String tableName, String[] aFields, String[] aValues) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        if (aFields.length != aValues.length) {
            return false;
        }

        ContentValues values = new ContentValues();

        for (int i = 0; i < aFields.length ; i++)
        {
            values.put(aFields[i], aValues[i]);
        }

        db.insert(tableName, null,values);

        db.close();

        return true;

    }

    public String createInsertBulkQuery(String tableName, String[] aFields) {

        String strReturn = "";

        String questionMarks = "";
        String strColumns = "";

        for (int i = 1; i <= aFields.length; i++) {

            questionMarks += "?";
            strColumns += aFields[i-1];

            if(i < aFields.length) {
                questionMarks += ",";
                strColumns += ",";
            }
        }

        strReturn = "INSERT INTO " + tableName + "(" + strColumns + ") VALUES (" + questionMarks + ");";
        return strReturn;
    }

/*    public void Update(String tableName, String strWhere, String[] aFields, String[] aValues) {
        try{

            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues contentValues = new ContentValues();

            for (int i = 0; i < aFields.length ; i++)
            {
                contentValues.put(aFields[i], aValues[i]);
            }

            db.update(tableName, contentValues, strWhereField + " = ?", new String[] { });
            db.close();

        }catch(Exception e){
            e.printStackTrace();
        }
    }*/

    public String GetTemplateName(String strStoreid) {
        Cursor curs = GetDataCursor(SQLiteDB.TABLE_STORE, SQLiteDB.COLUMN_STORE_id + " = " + strStoreid);
        curs.moveToFirst();
        String result = curs.getString(curs.getColumnIndex(SQLiteDB.COLUMN_STORE_templatename));
        curs.close();
        return result;
    }

    public String GetStoreCode(String strStoreid) {
        Cursor curs = GetDataCursor(SQLiteDB.TABLE_STORE, SQLiteDB.COLUMN_STORE_id + " = " + strStoreid);
        curs.moveToFirst();
        String result = curs.getString(curs.getColumnIndex(SQLiteDB.COLUMN_STORE_storecode));
        curs.close();
        return result;
    }

    public String GetStartDateOfStore(String strStoreid) {
        Cursor curs = GetDataCursor(SQLiteDB.TABLE_STORE, SQLiteDB.COLUMN_STORE_id + " = " + strStoreid);
        curs.moveToFirst();
        String result = curs.getString(curs.getColumnIndex(SQLiteDB.COLUMN_STORE_startdate));
        curs.close();
        return result;
    }

    public String GetEndDateOfStore(String strStoreid) {
        Cursor curs = GetDataCursor(SQLiteDB.TABLE_STORE, SQLiteDB.COLUMN_STORE_id + " = " + strStoreid);
        curs.moveToFirst();
        String result = curs.getString(curs.getColumnIndex(SQLiteDB.COLUMN_STORE_enddate));
        curs.close();
        return result;
    }

    public void UpdateRecord(String tableName, String strWhereField, String strWhereValue, String[] aFields, String[] aValues) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();

            for (int i = 0; i < aFields.length ; i++) {
                contentValues.put(aFields[i], aValues[i]);
            }

            db.update(tableName, contentValues, strWhereField + " = ?", new String[] { strWhereValue });
            db.close();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void UpdateRecord(String tableName, String[] strWhereField, String[] strWhereValue, String[] aFields, String[] aValues) {

        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues contentValues = new ContentValues();

            for (int i = 0; i < aFields.length ; i++)
            {
                contentValues.put(aFields[i], aValues[i]);
            }

            String wheres = "";
            for (String wherefields : strWhereField) {
                wheres += wherefields + " = ? AND ";
            }

            db.update(tableName, contentValues, wheres, strWhereValue);

            db.close();

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /*public boolean AddRecordBulk(String[] insertQuery) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String stringSQL = "";


        SQLiteStatement sqLiteStatement = db.compileStatement(stringSQL);

        db.beginTransaction();


        sqLiteStatement.clearBindings();

        for (int i = 0; i < aFields.length ; i++)
        {
            sqLiteStatement.bindString((i+1), aValues[i]);
        }

        sqLiteStatement.execute();

        db.setTransactionSuccessful();
        db.endTransaction();



        db.close();

        return true;

    }*/

    public Cursor GetDataCursor(String tableName) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + tableName, null);
        //db.close();
        return cursor;

    }

    public Cursor RawQuerySelect(String strQuery) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(strQuery, null);
        //db.close();
        return cursor;
    }

    public void ExecSQLWrite(String strQuery) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(strQuery);
        db.close();
    }

    public int GetNumberOfAnsweredInGroup(String storecateggroupid) {

        Cursor cursAnswered = RawQuerySelect("SELECT COUNT(*) as answered"
                + " FROM " + SQLiteDB.TABLE_STOREQUESTION + " JOIN " + SQLiteDB.TABLE_STORECATEGORYGROUP
                + " ON " + SQLiteDB.TABLE_STOREQUESTION + "." + SQLiteDB.COLUMN_STOREQUESTION_storecategorygroupid + " = " + SQLiteDB.TABLE_STORECATEGORYGROUP + "." + SQLiteDB.COLUMN_STORECATEGORYGROUP_id
                + " JOIN " + SQLiteDB.TABLE_QUESTION + " ON " + SQLiteDB.TABLE_STOREQUESTION + "." + SQLiteDB.COLUMN_STOREQUESTION_questionid + " = " + SQLiteDB.TABLE_QUESTION + "." + SQLiteDB.COLUMN_QUESTION_id
                + " WHERE " + SQLiteDB.COLUMN_QUESTION_formtypeid + " IN ('2','3','4','5','6','7','8','9','10','11','12')"
                + " AND " + SQLiteDB.TABLE_STOREQUESTION + "." + SQLiteDB.COLUMN_STOREQUESTION_storecategorygroupid + " = " + storecateggroupid
                + " AND " + SQLiteDB.COLUMN_STOREQUESTION_isAnswered + " = 1");

        cursAnswered.moveToFirst();
        int ret = cursAnswered.getInt(cursAnswered.getColumnIndex("answered"));
        cursAnswered.close();
        return ret;
    }

    public int GetTotalQuestions(String storecateggroupid) {
        Cursor cursTotalQuestions = RawQuerySelect("SELECT COUNT(*) AS totQuestions FROM " + SQLiteDB.TABLE_STOREQUESTION
                + " JOIN " + SQLiteDB.TABLE_QUESTION + " ON " + SQLiteDB.TABLE_QUESTION + "." + SQLiteDB.COLUMN_QUESTION_id + " = " + SQLiteDB.TABLE_STOREQUESTION + "." + SQLiteDB.COLUMN_STOREQUESTION_questionid
                + " WHERE " + SQLiteDB.COLUMN_QUESTION_formtypeid + " IN ('2','3','4','5','6','7','8','9','10','11','12')"
                + " AND " + SQLiteDB.COLUMN_STOREQUESTION_storecategorygroupid + " = " + storecateggroupid);
        cursTotalQuestions.moveToFirst();

        int ret = 0;
        ret = cursTotalQuestions.getInt(cursTotalQuestions.getColumnIndex("totQuestions"));
        cursTotalQuestions.close();
        return ret;
    }

    public int GetTotalQuestionsComputation(String storecateggroupid) {
        Cursor cursTotalQuestions = RawQuerySelect("SELECT COUNT(*) AS totQuestions FROM " + SQLiteDB.TABLE_STOREQUESTION
                + " JOIN " + SQLiteDB.TABLE_QUESTION + " ON " + SQLiteDB.TABLE_QUESTION + "." + SQLiteDB.COLUMN_QUESTION_id + " = " + SQLiteDB.TABLE_STOREQUESTION + "." + SQLiteDB.COLUMN_STOREQUESTION_questionid
                + " WHERE " + SQLiteDB.COLUMN_QUESTION_formtypeid + " IN ('3','4','9','10','11','12')"
                + " AND " + SQLiteDB.COLUMN_QUESTION_expectedans + " != ''"
                + " AND " + SQLiteDB.COLUMN_STOREQUESTION_storecategorygroupid + " = " + storecateggroupid);
        cursTotalQuestions.moveToFirst();

        int ret = 0;
        ret = cursTotalQuestions.getInt(cursTotalQuestions.getColumnIndex("totQuestions"));
        cursTotalQuestions.close();
        return ret;
    }

    public int GetCorrectAnswersComp(String storecateggroupid) {

//        String strQuery = "SELECT COUNT(*) AS correctAns FROM " + SQLiteDB.TABLE_STOREQUESTION
//                + " WHERE " + SQLiteDB.COLUMN_STOREQUESTION_final + " = '1'"
//                + " AND " + SQLiteDB.COLUMN_STOREQUESTION_storecategorygroupid + " = " + storecateggroupid;

        String strQuery = "SELECT COUNT(*) AS correctAns FROM " + SQLiteDB.TABLE_STOREQUESTION
                + " JOIN " + SQLiteDB.TABLE_QUESTION + " ON " + SQLiteDB.TABLE_QUESTION + "." + SQLiteDB.COLUMN_QUESTION_id + " = " + SQLiteDB.TABLE_STOREQUESTION + "." + SQLiteDB.COLUMN_STOREQUESTION_questionid
                + " WHERE " + SQLiteDB.COLUMN_QUESTION_formtypeid + " IN ('3','4','9','10','11','12')"
                + " AND " + SQLiteDB.COLUMN_QUESTION_expectedans + " != ''"
                + " AND " + SQLiteDB.COLUMN_STOREQUESTION_final + " = '1'"
                + " AND " + SQLiteDB.COLUMN_STOREQUESTION_storecategorygroupid + " = '" + storecateggroupid + "'";

        Cursor cursQuestions = RawQuerySelect(strQuery);
        cursQuestions.moveToFirst();

        int ret = cursQuestions.getInt(cursQuestions.getColumnIndex("correctAns"));
        cursQuestions.close();
        return ret;
    }

    public int GetRequiredQuestions(String storecateggroupid) {
        Cursor cursRequiredQuestions = RawQuerySelect("SELECT COUNT(*) AS requiredAns FROM " + SQLiteDB.TABLE_STOREQUESTION
                + " JOIN " + SQLiteDB.TABLE_QUESTION + " ON " + SQLiteDB.TABLE_QUESTION + "." + SQLiteDB.COLUMN_QUESTION_id + " = " + SQLiteDB.TABLE_STOREQUESTION + "." + SQLiteDB.COLUMN_STOREQUESTION_questionid
                + " WHERE " + SQLiteDB.COLUMN_QUESTION_required + " = '1'"
                + " AND " + SQLiteDB.COLUMN_STOREQUESTION_storecategorygroupid + " = " + storecateggroupid);
        cursRequiredQuestions.moveToFirst();

        int ret = cursRequiredQuestions.getInt(cursRequiredQuestions.getColumnIndex("requiredAns"));
        cursRequiredQuestions.close();
        return ret;
    }

    public boolean HasQuestionsPerGroup(int storeCategorygroupID) {
        boolean res = false;
        Cursor cursGetQuestions = GetDataCursor(SQLiteDB.TABLE_STOREQUESTION, SQLiteDB.COLUMN_STOREQUESTION_storecategorygroupid + " = " + storeCategorygroupID);
        cursGetQuestions.moveToFirst();
        int nQuestions = cursGetQuestions.getCount();
        if(nQuestions > 0) {
            res = true;
        }
        cursGetQuestions.close();
        return res;
    }

    public String GetPostingDateTime(int storeID) {
        String ret = "";

        Cursor cursor = GetDataCursor(SQLiteDB.TABLE_STORE, SQLiteDB.COLUMN_STORE_id + " = '" + storeID + "'");
        cursor.moveToFirst();
        if(cursor.getCount() > 0) {
            ret = "POSTING DATE: " + cursor.getString(cursor.getColumnIndex(SQLiteDB.COLUMN_STORE_postingdate)) + " " + cursor.getString(cursor.getColumnIndex(SQLiteDB.COLUMN_STORE_postingtime));
        }

        return ret;
    }

    public String GetFormTypeDesc(int formtypeid) {
        String ret = "";

        Cursor cursFormtype = GetDataCursor(SQLiteDB.TABLE_FORMTYPE, SQLiteDB.COLUMN_FORMTYPE_code + " = '" + formtypeid + "'");
        cursFormtype.moveToFirst();
        if(cursFormtype.getCount() > 0) {
            ret = cursFormtype.getString(cursFormtype.getColumnIndex(SQLiteDB.COLUMN_FORMTYPE_desc)).trim().toUpperCase();
        }

        return ret;
    }

    public String GetAnswer(String answer, int formtypeid, int formid) {
        String ret = "";
        ret = answer;

        switch (formtypeid) {
            case 9: // MULTI ITEM
                ret = GetMultiItemAnswer(answer);
                break;
            case 10: // SINGLE ITEM
                ret = GetSingleItemAnswer(formid, Integer.valueOf(answer));
                break;
            case 11: // COMPUTATIONAL
                ret = answer.split("=")[1].trim();
                break;
            case 12: // CONDITIONAL
                ret = answer.split("\\|")[0].trim();
                break;
            default:
                break;
        }

        return ret;
    }

    private String GetMultiItemAnswer(String answer) {
        String ret = "";
        String[] aAns = answer.split(",");
        for (int i = 0; i < aAns.length; i++) {
            if(i == (aAns.length - 1))
                ret += aAns[i].trim();
            else
                ret += aAns[i].trim() + "-";
        }
        return ret;
    }

    private String GetSingleItemAnswer(int formid, int optionid) {
        String sRet = "";

        Cursor cursSingle = GetDataCursor(SQLiteDB.TABLE_SINGLESELECT, SQLiteDB.COLUMN_SINGLESELECT_formid + " = '" + formid + "' AND " + SQLiteDB.COLUMN_SINGLESELECT_optionid + " = '" + optionid + "'");
        cursSingle.moveToFirst();
        if(cursSingle.getCount() > 0)
            sRet = cursSingle.getString(cursSingle.getColumnIndex(SQLiteDB.COLUMN_SINGLESELECT_option));

        return  sRet;
    }

    public int GetCorrectAnswers(String storecateggroupid) {

        Cursor cursQuestions = RawQuerySelect("SELECT COUNT(*) AS correctAns FROM " + SQLiteDB.TABLE_STOREQUESTION
                + " WHERE " + SQLiteDB.COLUMN_STOREQUESTION_final + " = '1'"
                + " AND " + SQLiteDB.COLUMN_STOREQUESTION_storecategorygroupid + " = '" + storecateggroupid + "'");
        cursQuestions.moveToFirst();

        int ret = cursQuestions.getInt(cursQuestions.getColumnIndex("correctAns"));
        cursQuestions.close();
        return ret;
    }

    public Cursor GetStoreQuestions(String strCondition, SQLiteDatabase db, SQLiteDB dbh) {
        db = dbh.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + SQLiteDB.COLUMN_QUESTION_questionid + " FROM " + SQLiteDB.TABLE_QUESTION + " WHERE " + strCondition, null);
        return cursor;
    }

    public Cursor GetDataCursor(String tableName, String strCondition) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + tableName + " where " + strCondition, null);
        return cursor;
    }

    /*public Cursor GetQuestions2(String strCondition) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT tblquestion." + SQLiteDB.COLUMN_QUESTION_formid + ",tblquestion."
                + SQLiteDB.COLUMN_QUESTION_formtypeid + ",tblquestion." + SQLiteDB.COLUMN_QUESTION_prompt + ",tblquestion."
                + SQLiteDB.COLUMN_QUESTION_expectedans + ",tblquestion." + SQLiteDB.COLUMN_QUESTION_required + ",tblquestion."
                + SQLiteDB.COLUMN_QUESTION_exempt + ",tblquestion." + SQLiteDB.COLUMN_QUESTION_questionid + " FROM " + SQLiteDB.TABLE_QUESTION
                + " JOIN " + SQLiteDB.TABLE_STORE + " ON " + SQLiteDB.TABLE_STORE + "." + SQLiteDB.COLUMN_STORE_audittempid + " = "
                + SQLiteDB.TABLE_QUESTION + "." + SQLiteDB.COLUMN_QUESTION_audittempid
                + " WHERE " + strCondition + " ORDER BY " + SQLiteDB.COLUMN_QUESTION_categorder, null);
*//*        Cursor cursor = db.rawQuery("SELECT tblquestion." + SQLiteDB.COLUMN_QUESTION_formid + ",tblquestion."
                + SQLiteDB.COLUMN_QUESTION_formtypeid + ",tblquestion." + SQLiteDB.COLUMN_QUESTION_prompt + ",tblquestion."
                + SQLiteDB.COLUMN_QUESTION_expectedans + ",tblquestion." + SQLiteDB.COLUMN_QUESTION_required + ",tblquestion."
                + SQLiteDB.COLUMN_QUESTION_exempt + ",tblquestion." + SQLiteDB.COLUMN_QUESTION_questionid + " FROM " + SQLiteDB.TABLE_QUESTION
                + " JOIN " + SQLiteDB.TABLE_STORE + " ON " + SQLiteDB.TABLE_STORE + "." + SQLiteDB.COLUMN_STORE_audittempid + " = "
                + SQLiteDB.TABLE_QUESTION + "." + SQLiteDB.COLUMN_QUESTION_audittempid, null);*//*
        return cursor;
    }*/

    public Cursor GetDataCursor(String tableName, String strCondition, String strOrderby) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + tableName + " where " + strCondition + " ORDER BY " + strOrderby, null);
        return cursor;

    }

    /*public Cursor GetGroupItem(String tableName, String strCondition) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + tableName + " where " + strCondition + " GROUP BY " + SQLiteDB.COLUMN_QUESTION_formgrpid + " ORDER BY " + SQLiteDB.COLUMN_QUESTION_order, null);
        return cursor;
    }


    public Cursor GetCategoryGrouped(String tableName, String strCondition) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + tableName + " where " + strCondition + " GROUP BY " + SQLiteDB.COLUMN_QUESTION_formcategoryid + " ORDER BY " + SQLiteDB.COLUMN_QUESTION_categorder, null);
        return cursor;

    }

    public Cursor GetQuestions(String strCondition) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + SQLiteDB.TABLE_QUESTION + " WHERE " + strCondition + " ORDER BY " + SQLiteDB.COLUMN_QUESTION_categorder, null);
        return cursor;

    }

    public Cursor GetQuestions3(String strCondition) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + SQLiteDB.TABLE_QUESTION + " JOIN " + SQLiteDB.TABLE_STOREQUESTION + " ON " + SQLiteDB.TABLE_QUESTION + "." + SQLiteDB.COLUMN_QUESTION_questionid + " = " + SQLiteDB.TABLE_STOREQUESTION + "." + SQLiteDB.COLUMN_STOREQUESTION_questionid + " WHERE " + strCondition + " ORDER BY " + SQLiteDB.COLUMN_QUESTION_categorder, null);
        return cursor;
    }*/

    public boolean InitializeAllTables() {
        boolean result = false;
        try {
            TruncateTable(SQLiteDB.TABLE_USER);
            TruncateTable(SQLiteDB.TABLE_STORE);
            TruncateTable(SQLiteDB.TABLE_QUESTION);
            TruncateTable(SQLiteDB.TABLE_CATEGORY);
            TruncateTable(SQLiteDB.TABLE_GROUP);
            TruncateTable(SQLiteDB.TABLE_FORMS);
            TruncateTable(SQLiteDB.TABLE_SINGLESELECT);
            TruncateTable(SQLiteDB.TABLE_MULTISELECT);
            TruncateTable(SQLiteDB.TABLE_COMPUTATIONAL);
            TruncateTable(SQLiteDB.TABLE_CONDITIONAL);
            TruncateTable(SQLiteDB.TABLE_CONDITIONAL_ANSWERS);
            TruncateTable(SQLiteDB.TABLE_STORECATEGORY);
            TruncateTable(SQLiteDB.TABLE_STORECATEGORYGROUP);
            TruncateTable(SQLiteDB.TABLE_STOREQUESTION);
            TruncateTable(SQLiteDB.TABLE_SECONDARYDISP);
            TruncateTable(SQLiteDB.TABLE_SECONDARYKEYLIST);
            TruncateTable(SQLiteDB.TABLE_OSALIST);
            TruncateTable(SQLiteDB.TABLE_OSALOOKUP);
            TruncateTable(SQLiteDB.TABLE_SOSLIST);
            TruncateTable(SQLiteDB.TABLE_SOSLOOKUP);
            TruncateTable(SQLiteDB.TABLE_PICTURES);
            TruncateTable(SQLiteDB.TABLE_NPI);
            TruncateTable(SQLiteDB.TABLE_PLANOGRAM);
            TruncateTable(SQLiteDB.TABLE_PERFECT_CATEGORY);
            TruncateTable(SQLiteDB.TABLE_PERFECT_GROUP);
            TruncateTable(SQLiteDB.TABLE_PJPCOMP);
            result = true;
        }
        catch (Exception e) {
            String exErr = e.getMessage() != null ? e.getMessage() : "Error in truncating tables.";
            Toast.makeText(this.mContext, exErr, Toast.LENGTH_LONG).show();
        }

        return result;
    }

    public boolean TruncateTable(String tableName) throws SQLException, Exception {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = db.delete(tableName, null, null);
            db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + tableName.trim() + "'");

        db.close();

        return count==0;
    }


}
