package com.android.pplusaudit2;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by ULTRABOOK on 11/11/2015.
 */
public class TCRLib {

    private Context mContext;

    public static ArrayList<Integer> arrPGroupList = new ArrayList<>();
    public static ArrayList<Integer> arrPCategoryList = new ArrayList<>();

    //public static String SOS_TOTAL_PERC = "FINAL ULP SOS PERCENTAGE";
    public static String SOS_TOTAL_PERC = "ULP SHARE OF SHELF PERCENTAGE";
    public static String SOS_PERFECT_STORE = "PERFECTSTORE-ULPSOSPERCENTAGE";

    public TCRLib(Context ctx) {
        this.mContext = ctx;
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(ctx, General.errlogFile));
    }

    public String GetStatus(String statusNo, TextView tvw) {

        String strReturn = "";

        switch (statusNo) {

            case "0":
                strReturn = General.STATUS_PENDING;
                tvw.setTextColor(mContext.getResources().getColor(R.color.red));
                break;

            case "1":
                strReturn = General.STATUS_PARTIAL;
                tvw.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
                break;

            case "2":
                strReturn = General.STATUS_COMPLETE;
                tvw.setTextColor(mContext.getResources().getColor(R.color.green));
                break;

            default:
                break;
        }

        return strReturn;
    }

    public String GetStatus(String statusNo) {

        String strReturn = "";

        switch (statusNo) {

            case "0":
                strReturn = General.STATUS_PENDING;
                break;

            case "1":
                strReturn = General.STATUS_PARTIAL;
                break;

            case "2":
                strReturn = General.STATUS_COMPLETE;
                break;

            default:
                break;
        }

        return strReturn;
    }

    public General.SCORE_STATUS GetScoreStatus(String finalValue) {

        General.SCORE_STATUS scoreStatus = General.SCORE_STATUS.NONE;

        switch (finalValue) {
            case "0": // FAILED
                scoreStatus = General.SCORE_STATUS.FAILED;
                break;
            case "1": // PASSED
                scoreStatus = General.SCORE_STATUS.PASSED;
                break;
            default:
                scoreStatus = General.SCORE_STATUS.NONE;
                break;
        }

        return scoreStatus;

    }

    public General.SCORE_STATUS GetScoreStatus(String finalValue, TextView tvw) {

        General.SCORE_STATUS scoreStatus = General.SCORE_STATUS.NONE;

        switch (finalValue) {
            case "0": // FAILED
                scoreStatus = General.SCORE_STATUS.FAILED;
                tvw.setTextColor(mContext.getResources().getColor(R.color.red));
                break;
            case "1": // PASSED
                scoreStatus = General.SCORE_STATUS.PASSED;
                tvw.setTextColor(mContext.getResources().getColor(R.color.green));
                break;
            default:
                scoreStatus = General.SCORE_STATUS.NONE;
                break;
        }

        return scoreStatus;

    }

    public String GetScoreStatusDesc(General.SCORE_STATUS scoreStatus) {

        String strReturn = "";

        switch (scoreStatus) {
            case PASSED:
                strReturn = General.SCORE_STATUS_PASSED;
                break;
            case FAILED:
                strReturn = General.SCORE_STATUS_FAILED;
                break;
            default:
                strReturn = "";
                break;
        }

        return strReturn;
    }

    public String ValidateNumericValue(String val) {
        String strRet = "0";

        String strValue = val.trim();

        if(!strValue.equals("")) {
            strRet = val;
        }

        return strRet;
    }

    public String TrimAllWhiteSpaces(String val) {
        String ret = "";
        ret = val.trim().replace(" ","");
        return ret;
    }

    public Drawable getAssetImage(String filename) throws IOException {
        AssetManager assets = mContext.getResources().getAssets();
        InputStream buffer = new BufferedInputStream((assets.open("drawable/" + filename + ".jpg")));
        Bitmap bitmap = BitmapFactory.decodeStream(buffer);
        return new BitmapDrawable(mContext.getResources(), bitmap);
    }

    public Bitmap decodeFile(File f) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE=70;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }
}
