package com.android.pplusaudit2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;

/**
 * Created by ULTRABOOK on 7/30/2015.
 */
public class MyMessageBox {

    private Context mContext;
    boolean result = false;

    public MyMessageBox(Context context) {

        this.mContext = context;
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(context, General.errlogFile));
    }

    public void ShowMessage(String title, String message) {

        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public void ShowExceptionError(Exception ex, String title)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(ex.getMessage());
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public boolean ShowQuestion(String title, String msg) {

        result = false;

        final AlertDialog alert = new AlertDialog.Builder(mContext).create();
        alert.setMessage(msg);
        alert.setTitle(title);
        alert.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        result = true;
                    }
                });

        alert.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        result = false;
                    }
                });

        alert.show();

        return result;
    }
}
