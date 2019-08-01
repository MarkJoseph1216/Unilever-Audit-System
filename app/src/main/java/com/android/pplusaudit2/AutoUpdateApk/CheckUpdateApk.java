package com.android.pplusaudit2.AutoUpdateApk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.ErrorLogs.ErrorLog;
import com.android.pplusaudit2.General;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Created by ULTRABOOK on 5/19/2016.
 */
public class CheckUpdateApk extends Observable {

    // every 10 seconds
    private static long WAKEUP_INTERVAL = 15000;
    private static long CHECK_INTERVAL = 15000;
    private final static Handler updateHandler = new Handler();
    private Context mContext;
    private String TAG;
    private BroadcastReceiver checkingReceiver;
    private Runnable periodicUpdate;
    private ErrorLog errorLog;
    public boolean started;

    public CheckUpdateApk(Context ctx) {
        this.mContext = ctx;
        General.hasUpdate = false;
        started = false;
        this.TAG = getClass().getSimpleName();

        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(ctx, General.errlogFile));
        errorLog = new ErrorLog(General.errlogFile, ctx);

            checkingReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    new CheckUpdates().execute();
                    updateHandler.postDelayed(periodicUpdate, CHECK_INTERVAL);
                }
            };

            periodicUpdate = new Runnable() {
                @Override
                public void run() {
                    new CheckUpdates().execute();
                    updateHandler.removeCallbacks(periodicUpdate);    // remove whatever others may have posted
                    updateHandler.postDelayed(this, WAKEUP_INTERVAL);
                }
            };

            ctx.registerReceiver( checkingReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private class CheckUpdates extends AsyncTask<Void, Void, Boolean> {

        String messages = "";
        HttpURLConnection urlConnection;

        String urlCheck = AutoUpdate.API_URL_CHECK;

        private boolean hasUpdate = false;

        @Override
        protected void onPreExecute() {
            Log.e(TAG, "checking updates");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;

            if(General.hasUpdate) {
                Log.e(TAG, "Has update!");
                hasUpdate = true;
                return true;
            }

            if(!started) {
                Log.e(TAG, "Auto update not enabled.");
                return true;
            }

            if(General.BETA) urlCheck = AutoUpdate.API_BETA_URL_CHECK;

            String packageName = mContext.getPackageName();

            try {

                ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

                if(activeNetwork != null) {
                    if(activeNetwork.isAvailable() || activeNetwork.isConnectedOrConnecting()) {

                        URL url = new URL(urlCheck);
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setReadTimeout(10000);
                        urlConnection.setConnectTimeout(15000);
                        urlConnection.setRequestMethod("POST");
                        urlConnection.setDoInput(true);
                        urlConnection.setDoOutput(true);

                        List<NameValuePair> nvParams = new ArrayList<>();
                        nvParams.add(new BasicNameValuePair("pkgname", packageName));

                        OutputStream os = urlConnection.getOutputStream();
                        BufferedWriter writer = new BufferedWriter(
                                new OutputStreamWriter(os, "UTF-8"));
                        writer.write(getQuery(nvParams));
                        writer.flush();
                        writer.close();
                        os.close();

                        urlConnection.connect();

                        InputStream is = urlConnection.getInputStream();
                        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                        String line = "";
                        StringBuffer sbResponse = new StringBuffer();
                        while((line = rd.readLine()) != null) {
                            sbResponse.append(line);
                            sbResponse.append('\r');
                        }
                        rd.close();

                        String response = sbResponse.toString().trim();

                        int vcodeAPI = Integer.valueOf(response.trim());
                        if (vcodeAPI > General.versionCode) {
                            hasUpdate = true;
                        }
                        else Log.wtf(TAG, "No updates found.");

                        result = true;
                    }
                    else {
                        messages = "Slow or unstable internet connection.";
                        Log.wtf(TAG, messages);
                    }
                }
                else {
                    messages = "No internet connection.";
                    Log.wtf(TAG, messages);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                messages = ex.getMessage() != null ? ex.getMessage() : "Slow or unstable connection";
                errorLog.appendLog(messages, TAG);
            }
            finally {
                if(urlConnection != null)
                    urlConnection.disconnect();
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean bResult) {
            if (!bResult) {
                Log.e(TAG, messages);
                return;
            }

            if (hasUpdate && !General.hasUpdate) {
                Log.wtf(TAG, "Update is downloading..");
                General.mainAutoUpdate.StartAutoUpdate();
            }
        }
    }

    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}
