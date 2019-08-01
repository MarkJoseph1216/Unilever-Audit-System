package com.android.pplusaudit2.ErrorLogs;

import android.content.Context;
import android.util.Log;

import com.android.pplusaudit2.Database.SQLiteDB;
import com.android.pplusaudit2.General;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by ULTRABOOK on 5/19/2016.
 */
public class ErrorLog {

    public File fileLog;
    private Context mContext;

    public ErrorLog(String logName, Context ctx) {
        this.mContext = ctx;
        this.fileLog = new File(ctx.getExternalFilesDir(null), logName);
    }

    public void appendLog(String text, String classname)
    {
        if(!General.dateLog.equals(General.getDateToday())) {
            fileLog.delete();
        }

        if (!fileLog.exists())
        {
            try
            {
                fileLog.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try
        {
            String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
            int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();

            //BufferedWriter for performance, true to set append to file flag
            String strLog = "[" + General.versionName + "][" + General.getDateTimeToday("-") + "][" + classname + "][" + methodName + "][" + lineNumber + "][" + General.getDeviceName() + "][" + General.getDeviceOsVersion() + "]" + General.versionCode + "-" + SQLiteDB.DATABASE_VERSION + ": " + text;
            Log.e(classname, strLog);
            BufferedWriter buf = new BufferedWriter(new FileWriter(fileLog, true));
            buf.append(strLog + "\n");
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
