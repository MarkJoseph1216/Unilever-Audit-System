package com.android.pplusaudit2.Database;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.provider.Settings;
import android.widget.Toast;

import com.android.pplusaudit2.AppSettings;
import com.android.pplusaudit2.ErrorLogs.ErrorLog;
import com.android.pplusaudit2.General;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by Lloyd on 6/14/16.
 */
public class SQLPortable {

    private final ErrorLog errorLog;
    private Context mContext;
    private SQLiteDatabase mDataBase;
    private String TAG;
    private String deviceId = "";
    private String exportedDbName = "";

    public SQLPortable(Context mContext) {
        this.mContext = mContext;
        this.TAG = SQLPortable.this.getClass().getSimpleName();
        this.deviceId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        this.exportedDbName = SQLiteDB.PORTABLE_DATABASE_NAME + "_" + General.getDeviceID(mContext) + "_" + General.getDbDateTimeToday();
        errorLog = new ErrorLog(General.errlogFile, mContext);
    }

    public String getExportedDBName() {
        return exportedDbName;
    }

    //Open the database, so we can query it
    public boolean OpenPortableDatabase() throws SQLException
    {
        File dbFile = new File(AppSettings.dbFolder, exportedDbName);
        if(!dbFile.exists()) {
            Toast.makeText(mContext, SQLiteDB.PORTABLE_DATABASE_NAME + " is not existing.", Toast.LENGTH_LONG).show();
            return false;
        }

        String mPath = dbFile.getPath();
        errorLog.appendLog("Portable database path: " + mPath, "OpenPortableDatabase");
        mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);

        return mDataBase != null;
    }

    public Cursor GetDataCursor(String tableName, String strCondition) {
        Cursor cursor = mDataBase.rawQuery("select * from " + tableName + " where " + strCondition, null);
        return cursor;
    }

    public Cursor GetDataCursor(String tableName) {
        Cursor cursor = mDataBase.rawQuery("select * from " + tableName, null);
        return cursor;
    }

    public void BeginBulkInsert(String query, String[] aFields, Cursor cursTable) {
        SQLiteStatement sqlstatementGroup = mDataBase.compileStatement(query); // insert into tblsample (fields1,fields2)
        mDataBase.beginTransaction();

        sqlstatementGroup.clearBindings();

        while (cursTable.moveToFirst()) {

            for (int i = 1; i <= aFields.length; i++) {
                sqlstatementGroup.bindString((i), cursTable.getString(i - 1).trim().replace("\"", ""));
            }

            cursTable.moveToNext();
        }

        cursTable.close();

        sqlstatementGroup.execute();

        mDataBase.setTransactionSuccessful();
        mDataBase.endTransaction();
    }

    public boolean copyMainDatabaseFileToSd() {

        boolean result = false;
        ProgressDialog pdl = ProgressDialog.show(mContext, "", "Copying database. Please wait.");

        try {

            File dbFile = mContext.getDatabasePath(SQLiteDB.DATABASE_NAME);
            File fDest = new File(AppSettings.dbFolder, exportedDbName);
//            if(fDest.exists()) fDest.delete();
//            MainLibrary.CopyFile(dbFile, fDest);
            copyFile(new FileInputStream(dbFile), new FileOutputStream(fDest));
            result = true;
        }
        catch (IOException ex) {
            String uErr = "Can't copy tcr database. Please try again.";
            String exError = ex.getMessage() != null ? ex.getMessage() : uErr;
            errorLog.appendLog(exError, TAG);
            Toast.makeText(mContext, uErr, Toast.LENGTH_LONG).show();
        }

        pdl.dismiss();
        return result;
    }

    public boolean copyImportedDBToMain(String dbImportedName) {

        boolean result = false;
        ProgressDialog pdl = ProgressDialog.show(mContext, "", "Copying database. Please wait.");

        try {

            File dbFile = new File(AppSettings.dbFolder, dbImportedName);
            File fDest = mContext.getDatabasePath(SQLiteDB.DATABASE_NAME);
//            if(fDest.exists()) fDest.delete();
//            MainLibrary.CopyFile(dbFile, fDest);
            copyFile(new FileInputStream(dbFile), new FileOutputStream(fDest));
            result = true;
        }
        catch (IOException ex) {
            String uErr = "Can't copy pcount database. Please try again.";
            String exError = ex.getMessage() != null ? ex.getMessage() : uErr;
            errorLog.appendLog(exError, TAG);
            Toast.makeText(mContext, uErr, Toast.LENGTH_LONG).show();
        }

        pdl.dismiss();
        return result;
    }

    private void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException {
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            fromChannel = fromFile.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                }
            }
        }
    }

    public boolean importDatabase(FileInputStream fromFile) throws IOException {
        boolean result = false;
        try {

            File dbFile = mContext.getDatabasePath(SQLiteDB.DATABASE_NAME);
            File fDest = new File(AppSettings.dbFolder, exportedDbName);
//            if(fDest.exists()) fDest.delete();
//            MainLibrary.CopyFile(dbFile, fDest);
            copyFile(fromFile, new FileOutputStream(mContext.getDatabasePath(SQLiteDB.DATABASE_NAME)));
            result = true;
        }
        catch (IOException ex) {
            String uErr = "Can't copy pcount database. Please try again.";
            String exError = ex.getMessage() != null ? ex.getMessage() : uErr;
            errorLog.appendLog(exError, TAG);
            Toast.makeText(mContext, uErr, Toast.LENGTH_LONG).show();
        }

        return result;
    }


}
