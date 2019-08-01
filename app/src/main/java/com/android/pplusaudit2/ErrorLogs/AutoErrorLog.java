package com.android.pplusaudit2.ErrorLogs;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.pplusaudit2.Database.SQLiteDB;
import com.android.pplusaudit2.General;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by ULTRABOOK on 5/20/2016.
 */
public class AutoErrorLog implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler defaultUEH;
    private Context app;
    private String className;
    private File fileLog;

    public AutoErrorLog(Context app, String fileLogName) {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        this.app = app;
        this.fileLog = new File(app.getExternalFilesDir(null), fileLogName);
        this.className = this.getClass().getSimpleName();
    }

    public void uncaughtException(Thread t, Throwable e) {

        try {

            StackTraceElement[] arr = e.getStackTrace();
            String report = "";

            // If the exception was thrown in a background thread inside
            // AsyncTask, then the actual exception can be found with getCause
            report += "--------- Cause ---------\n\n";
            Throwable cause = e.getCause();
            if (cause != null) {
                report += cause.toString() + "\n\n";
                arr = cause.getStackTrace();
                for (int i = 0; i < arr.length; i++)
                    report += "    " + arr[i].toString() + "\n";
            }
            report += "Datetime: " + General.getDateTimeToday("-") + "\n";
            report += "Model: " + General.getDeviceName() + "\n";
            report += "SQLite Version: " + String.valueOf(SQLiteDB.DATABASE_VERSION) + "\n";
            report += "Version code: " + String.valueOf(General.versionCode) + "\n";
            report += "Version name: " + String.valueOf(General.versionName) + "\n";
            report += "Device OS: " + General.getDeviceOsVersion() + "\n";
            report += "Device API level: " + General.GetApiLevelDevice() + "\n";
            report += "\n-------------------------------\n\n";

            try {
                FileOutputStream trace = new FileOutputStream(fileLog);
                trace.write(report.getBytes());
                trace.close();
            } catch (IOException ioe) {
                String err = ioe.getMessage() != null ? ioe.getMessage() : "Error in logs.";
                Log.e(className, err);
            }

            defaultUEH.uncaughtException(t, e);
        }
        catch (Exception ex) { }
    }
}
