
package com.android.pplusaudit2.AutoUpdateApk;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.ErrorLogs.ErrorLog;
import com.android.pplusaudit2.General;
import com.android.pplusaudit2.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class AutoUpdate {
    private String TAG = this.getClass().getSimpleName();

    private Context mContext;

    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotifyManager;

    private File fileToDownload;
    private String strFilename = "";

    private int NOTIF_ID = 1;
    private NotificationReceiver nReceiver;
    private static int appIcon = android.R.drawable.ic_popup_reminder;

    private File fUpdates;

    private final static String ANDROID_PACKAGE = "application/vnd.android.package-archive";

    private ArrayList<AsyncTask<Void, String, Boolean>> arr;

    private String packageName;
    private String appName;
    private int device_id;
    private int versionCode = 0;
    public boolean isUpdating = false;

    private static SharedPreferences preferences;
    private final static String UPDATE_FILE = "update_file";
    private final static String SILENT_FAILED = "silent_failed";
    private final static String MD5_TIME = "md5_time";
    private final static String MD5_KEY = "md5";
    private final static String LAST_UPDATE_KEY = "last_update";

    private final static String API_URL_APK = "http://www.apps.chasetech.com/api/check";
    public final static String API_URL_CHECK = "http://www.apps.chasetech.com/api/verify";

    private final static String API_BETA_URL_APK = "http://www.apps.chasetech.com/api/betacheck";
    public final static String API_BETA_URL_CHECK = "http://www.apps.chasetech.com/api/betaverify";

    private long downloadReference = 0;
    private DownloadManager downloadManager;
    private ErrorLog errorLog;

    public AutoUpdate(Context ctx) {
        this.mContext = ctx;
        arr = new ArrayList<AsyncTask<Void, String, Boolean>>();
        SetUpVars(ctx);
        errorLog = new ErrorLog(General.errlogFile, ctx);

        //set filter to only when download is complete and register broadcast receiver
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        mContext.registerReceiver(downloadReceiver, filter);
    }

    private void SetUpVars(Context ctx) {

        try {
            PackageManager pm = ctx.getPackageManager();
            String packageName = ctx.getPackageName();
            int flags = PackageManager.GET_PERMISSIONS;
            PackageInfo packageInfo = null;
            packageInfo = pm.getPackageInfo(packageName, flags);
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }

        fUpdates = new File(mContext.getExternalFilesDir(null), "APK Updates");
        if(!fUpdates.exists())
            fUpdates.mkdirs();

        preferences = ctx.getSharedPreferences( packageName + "_" + TAG, Context.MODE_PRIVATE);
        device_id = crc32(Settings.Secure.getString( ctx.getContentResolver(), Settings.Secure.ANDROID_ID));

        ApplicationInfo appinfo = ctx.getApplicationInfo();
        if( appinfo.icon != 0 ) {
            appIcon = appinfo.icon;
        } else {
            Log.e(TAG, "unable to find application icon");
        }
        if( appinfo.labelRes != 0 ) {
            appName = ctx.getString(appinfo.labelRes);
        } else {
            Log.e(TAG, "unable to find application label");
        }

        if( new File(appinfo.sourceDir).lastModified() > preferences.getLong(MD5_TIME, 0) ) {
            preferences.edit().putString( MD5_KEY, MD5Hex(appinfo.sourceDir)).apply();
            preferences.edit().putLong( MD5_TIME, System.currentTimeMillis()).apply();

            String update_file = preferences.getString(UPDATE_FILE, "");
            if( update_file.length() > 0 ) {
                if( new File( ctx.getFilesDir().getAbsolutePath() + "/" + update_file ).delete() ) {
                    preferences.edit().remove(UPDATE_FILE).remove(SILENT_FAILED).apply();
                }
            }
        }
    }

    private static int crc32(String str) {
        byte bytes[] = str.getBytes();
        Checksum checksum = new CRC32();
        checksum.update(bytes,0,bytes.length);
        return (int) checksum.getValue();
    }

    private String MD5Hex(String filename)
    {
        final int BUFFER_SIZE = 8192;
        byte[] buf = new byte[BUFFER_SIZE];
        int length;
        try {
            FileInputStream fis = new FileInputStream( filename );
            BufferedInputStream bis = new BufferedInputStream(fis);
            MessageDigest md = MessageDigest.getInstance("MD5");
            while( (length = bis.read(buf)) != -1 ) {
                md.update(buf, 0, length);
            }

            byte[] array = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte anArray : array) {
                sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
            }
            Log.v(TAG, "md5sum: " + sb.toString());
            return sb.toString();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return "md5bad";
    }

    public void StartAutoUpdate() {

        mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(mContext);
        //mBuilder.setContentTitle("Downloading updates...").setContentText("Download in progress").setSmallIcon(appIcon);
        //mBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
        //mBuilder.setTicker("Downloading update.");
        //mBuilder.setProgress(0, 0, true);
        mBuilder.setAutoCancel(false);
        //mNotifyManager.notify(NOTIF_ID, mBuilder.build());

        ContentResolver contentResolver = mContext.getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        packageName = mContext.getPackageName();

        CheckUpdateTask checkUpdateTask = new CheckUpdateTask();
        checkUpdateTask.execute();
        arr.add(checkUpdateTask);

        long last_update = System.currentTimeMillis();
        preferences.edit().putLong( LAST_UPDATE_KEY, last_update).apply();

        // check to see if the enabledNotificationListeners String contains our
        // package name
        if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName)) {
            // in this situation we know that the user has not granted the app
            // the Notification access permission
            // Check if notification is enabled for this application
            Log.i("ACC", "Dont Have Notification access");
//            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
//            mContext.startActivity(intent);
        } else {
            Log.i("ACC", "Have Notification access");
        }

//        nReceiver = new NotificationReceiver();
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(NLService.NOT_TAG);
//        mContext.registerReceiver(nReceiver, filter);
    }

    class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String event = intent.getExtras().getString(NLService.NOT_EVENT_KEY);
            Log.i("NotificationReceiver", "NotificationReceiver onReceive : " + event);
            assert event != null;
            if (event.trim().contentEquals(NLService.NOT_REMOVED)) {
                killTasks();
            }
        }
    }


    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //check if the broadcast message is for our Enqueued download
            try {
                String[] statusAndReason = checkStatus().split(",");

                int status = Integer.valueOf(statusAndReason[0]);
                int reason = Integer.valueOf(statusAndReason[1]);

                String update_file_path = fileToDownload.getPath();
                String hash = MD5Hex(update_file_path);
                preferences.edit().putString(MD5_KEY, hash).apply();
                preferences.edit().putLong(MD5_TIME, System.currentTimeMillis()).apply();
                preferences.edit().putString(UPDATE_FILE, strFilename).apply();

                isUpdating = false;
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
//                    long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
//                    if (downloadReference == referenceId) {
//
//                    }
                    General.hasUpdate = false;
                } else {
                    DeleteAllApk();
                    String strStatus = GetStatusMessage(status).toLowerCase();
                    String strReason = GetReasonMessage(status, reason);
                    General.messageBox(mContext, "Download " + strStatus, "Download status: " + strStatus + "\nReason: " + strReason);
                }
            }
            catch (Exception ex) {
                String err = "Download failed. Please try to log again.";
                String exceptionErr = ex.getMessage() != null ? ex.getMessage() : err;
                errorLog.appendLog(exceptionErr, TAG);
            }
        }
    };

    private void killTasks() {
        if (null != arr & arr.size() > 0) {
            for (AsyncTask<Void, String, Boolean> a : arr) {
                if (a != null) {
                    Log.i("NotificationReceiver", "Killing download thread");
                    a.cancel(true);
                }
            }

            mNotifyManager.cancelAll();
        }
    }

    private void DeleteAllApk() {
        if(fUpdates.isDirectory()) {
            String[] files = fUpdates.list();
            for (String file : files) {
                new File(fUpdates, file).delete();
            }
        }
    }

    private class CheckUpdateTask extends AsyncTask<Void, String, Boolean> {

        String statusMessage = "";
        String[] response;

        private DefaultHttpClient httpclient = new DefaultHttpClient();

        @Override
        protected void onPreExecute() {
            //progressDialog = ProgressDialog.show(mContext, "", "Updating application. Please wait.");
        }

        @Override
        protected void onProgressUpdate(String... values) {
            String[] aValues = values[0].split(",");
            mBuilder.setContentText("Downloaded (" + aValues[0] + "MB/" + aValues[1] + "MB)");
            int per = Integer.parseInt(aValues[2]);
            int max = Integer.parseInt(aValues[3]);
            mBuilder.setProgress(max, per, false);
            // Displays the progress bar for the first time.
            mNotifyManager.notify(NOTIF_ID, mBuilder.build());
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;

            String md5Hash = preferences.getString(MD5_KEY, "0");

            String strParams = "pkgname=" + packageName + "&version=" + versionCode +
                    "&md5=" + md5Hash +
                    "&id=" + String.format("%08x", device_id);

            String urlCheck = API_URL_APK;
            if(General.BETA) {
                urlCheck = API_BETA_URL_APK;
            }

            try {
                HttpPost httpPost = new HttpPost(urlCheck);
                HttpParams httpParameters = new BasicHttpParams();
                int timeoutConnection = 10000;
                HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
                int timeoutSocket = 8000;
                HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
                httpclient.setParams(httpParameters);

                StringEntity urlParams = new StringEntity(strParams);
                httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
                httpPost.setEntity(urlParams);
                HttpResponse httpResponse = httpclient.execute( httpPost );
                String strResponse = EntityUtils.toString(httpResponse.getEntity(), "UTF-8" );

                response = strResponse.split("\n");
                if( response.length > 1 && response[0].equalsIgnoreCase("have update") ) {
                    strFilename = response[1].substring(response[1].lastIndexOf('/')+1);

                    fileToDownload = new File(fUpdates, strFilename);

                    result = true;

//                    HttpGet httpGet = new HttpGet(response[1]);
//                    HttpEntity entity = httpclient.execute( httpGet ).getEntity();
//
//                    if( entity.getContentType().getValue().equalsIgnoreCase(ANDROID_PACKAGE)) {
//
//                        //FileOutputStream fos = mContext.openFileOutput(strFilename, Context.MODE_WORLD_READABLE);
//
//                        if(fileToDownload.exists()) fileToDownload.delete();
//
//                        OutputStream output = new FileOutputStream(fileToDownload.getPath());
//                        InputStream inputStream = entity.getContent();
//
//                        double lenghtOfFile = Double.valueOf(entity.getContentLength()) / 1000000;
//                        long nLengthOfFile = entity.getContentLength();
//                        byte data[] = new byte[1024 * 1000];
//                        double total = 0;
//                        int nTotal = 0;
//                        int count = 0;
//                        while ((count = inputStream.read(data)) != -1) {
//                            total +=  Double.valueOf(count) / 1000000;
//                            nTotal += count;
//                            publishProgress(String.format("%.2f", total) + "," + String.format("%.2f", lenghtOfFile) + "," + nTotal + "," + nLengthOfFile);
//                            Thread.sleep(100);
//                            output.write(data, 0, count);
//                        }
//
//                        output.flush();
//                        output.close();
//                        inputStream.close();
//
//                        //entity.writeTo(fos);
//                        //fos.close();
//                    }
                }
                else statusMessage = "No update available.";
            }
            catch (IOException ex) {
                String prompt = "Slow or unstable internet connection. Please try again.";
                String errmsg = ex.getMessage() != null ? ex.getMessage() : prompt;
                errorLog.appendLog(errmsg, TAG);
            }

            return result;
        }

        @Override
        protected void onPostExecute (Boolean bResult){
            //progressDialog.dismiss();
            if(!bResult) {
                isUpdating = false;
                killTasks();
                mContext.unregisterReceiver(nReceiver);
                Toast.makeText(mContext, statusMessage, Toast.LENGTH_LONG).show();
                fileToDownload.delete();
                return;
            }

            try {

                if(response != null) {
                    if(response[0].equalsIgnoreCase("have update")) {

                        if(!fileToDownload.exists()) {
                            isUpdating = true;
                            DeleteAllApk();
                            DownloadApkUpdate(response[1], strFilename);
                        }
                        else {
                            String hashFile = MD5Hex(fileToDownload.getPath());
                            String hashWebFile = preferences.getString(MD5_KEY, "0");

                            if(!hashFile.equals(hashWebFile)) {
                                isUpdating = true;
                                DeleteAllApk();
                                DownloadApkUpdate(response[1], strFilename);
                                return;
                            }

                            isUpdating = false;

                            final Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
                            Uri uri = Uri.fromFile(fileToDownload);
                            notificationIntent.setDataAndType(uri, ANDROID_PACKAGE);
                            PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);

                            General.hasUpdate = true;

                            mBuilder.setContentTitle("Update already downloaded");
                            mBuilder.setContentText(strFilename + " successfully downloaded");
                            mBuilder.setProgress(0, 0, false);
                            mBuilder.setSmallIcon(appIcon);
                            mBuilder.setTicker("Download complete. An update is now available.");
                            mBuilder.setContentIntent(contentIntent);
                            mNotifyManager.notify(NOTIF_ID, mBuilder.build());

                            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            AlertDialog mAlertDialog = new AlertDialog.Builder(mContext).create();
                            mAlertDialog.setCancelable(false);
                            mAlertDialog.setTitle("Existing update");
                            mAlertDialog.setMessage("There's an " + appName + " update available. Tap UPDATE to install.");
                            mAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "UPDATE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    General.hasUpdate = false;
                                    mContext.startActivity(notificationIntent);
                                }
                            });
                            mAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    ((Activity) mContext).finish();
                                }
                            });
                            mAlertDialog.show();
                        }
                    }
                    else {
                        Toast.makeText(mContext, statusMessage, Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(mContext, statusMessage, Toast.LENGTH_SHORT).show();
                }
            }
            catch (IOException e) {
                String prompt = "File not found. Please try again.";
                String errmsg = e.getMessage() != null ? e.getMessage() : prompt;
                errorLog.appendLog(errmsg, TAG);
            }

//            if(response != null) {
//                if (response[0].equalsIgnoreCase("have update")) {
//                    String update_file_path = mContext.getFilesDir().getAbsolutePath() + "/" + strFilename;
//                    preferences.edit().putString(MD5_KEY, MD5Hex(update_file_path)).apply();
//                    preferences.edit().putLong(MD5_TIME, System.currentTimeMillis()).apply();
//                    preferences.edit().putString(UPDATE_FILE, strFilename).apply();
//
//                    final Intent notificationIntent = new Intent(Intent.ACTION_VIEW );
//
//                    String fData = fileToDownload.getAbsolutePath();
//                    //String fData = "/sdcard/update.apk";
//                    Uri uri = Uri.fromFile(fileToDownload);
//
//                    notificationIntent.setDataAndType(uri, ANDROID_PACKAGE);
//                    PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
//
//                    mBuilder.setContentTitle("Download complete");
//                    mBuilder.setContentText(strFilename + " successfully downloaded");
//                    mBuilder.setProgress(0, 0, false);
//                    mBuilder.setSmallIcon(appIcon);
//                    mBuilder.setTicker("Download complete. An update is now available.");
//                    mBuilder.setContentIntent(contentIntent);
//                    mNotifyManager.notify(NOTIF_ID, mBuilder.build());
//
//                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//                    try {
//                        mAlertDialog = new AlertDialog.Builder(mContext).create();
//                        mAlertDialog.setCancelable(false);
//                        mAlertDialog.setTitle("Existing update");
//                        mAlertDialog.setMessage("There's an " + appName + " update available. Tap UPDATE to install.");
//                        mAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "UPDATE", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                                mContext.startActivity(notificationIntent);
//                            }
//                        });
//                        mAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                                ((Activity) mContext).finish();
//                            }
//                        });
//                        mAlertDialog.show();
//                    }
//                    catch (Exception e) {
//                        String err = e.getMessage() != null ? e.getMessage() : "Can't display alertdialog.";
//                        Log.e(TAG, err);
//                    }
//                }
//                else
//                    Toast.makeText(mContext, statusMessage, Toast.LENGTH_SHORT).show();
//            }
        }
    }

    private void DownloadApkUpdate(String url, String strFilename) throws IOException {

        General.hasUpdate = true;

        downloadManager = (DownloadManager) mContext.getSystemService(mContext.DOWNLOAD_SERVICE);
        Uri Download_Uri = Uri.parse(url.trim());
        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);

        //Restrict the types of networks over which this download may proceed.
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        //Set whether this download may proceed over a roaming connection.
        request.setAllowedOverRoaming(false);
        //Set the title of this download, to be displayed in notifications (if enabled).
        request.setTitle("TCR application update");
        //Set a description of this download, to be displayed in notifications (if enabled)
        request.setDescription("Download in progress");

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        //Set the local destination for the downloaded file to a path within the application's external files directory
        request.setDestinationInExternalFilesDir(mContext, "APK Updates", strFilename);

        //Enqueue a new download and same the referenceId

        downloadReference = downloadManager.enqueue(request);
    }

    private String checkStatus(){

        String strReturn = "";

        try {
            DownloadManager.Query myDownloadQuery = new DownloadManager.Query();
            //set the query filter to our previously Enqueued download
            myDownloadQuery.setFilterById(downloadReference);

            //Query the download manager about downloads that have been requested.
            Cursor cursor = downloadManager.query(myDownloadQuery);
            if (!cursor.moveToFirst()) {
                Toast.makeText(mContext, "Cursor empty", Toast.LENGTH_SHORT).show();
                return strReturn;
            }

            //column for status
            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int status = cursor.getInt(columnIndex);
            //column for reason code if the download failed or paused
            int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
            int reason = cursor.getInt(columnReason);
            //get the download filename
            int filenameIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
            String filename = cursor.getString(filenameIndex);

            strReturn = String.valueOf(status) + "," + String.valueOf(reason);

        }
        catch (Exception ex) {
            String err = "Download failed. Please try to log again.";
            String exceptionErr = ex.getMessage() != null ? ex.getMessage() : err;
            errorLog.appendLog(exceptionErr, TAG);
        }

        return strReturn;
    }

    private String GetStatusMessage(int status) {
        String statusMessage = "";
        switch(status){
            case DownloadManager.STATUS_FAILED:
                statusMessage = "FAILED";
                break;
            case DownloadManager.STATUS_PAUSED:
                statusMessage = "PAUSED";
                break;
            case DownloadManager.STATUS_PENDING:
                statusMessage = "PENDING";
                break;
            case DownloadManager.STATUS_RUNNING:
                statusMessage = "RUNNING";
                break;
            case DownloadManager.STATUS_SUCCESSFUL:
                statusMessage = "SUCCESS";
                break;
            default:
                statusMessage = "";
                break;
        }

        return statusMessage;
    }

    private String GetReasonMessage(int status, int reason) {
        String reasonText = "";

        switch(status) {

            case DownloadManager.STATUS_FAILED:
                switch(reason){
                    case DownloadManager.ERROR_CANNOT_RESUME:
                        reasonText = "ERROR_CANNOT_RESUME";
                        break;
                    case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                        reasonText = "ERROR_DEVICE_NOT_FOUND";
                        break;
                    case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                        reasonText = "ERROR_FILE_ALREADY_EXISTS";
                        break;
                    case DownloadManager.ERROR_FILE_ERROR:
                        reasonText = "ERROR_FILE_ERROR";
                        break;
                    case DownloadManager.ERROR_HTTP_DATA_ERROR:
                        reasonText = "ERROR_HTTP_DATA_ERROR";
                        break;
                    case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                        reasonText = "ERROR_INSUFFICIENT_SPACE";
                        break;
                    case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                        reasonText = "ERROR_TOO_MANY_REDIRECTS";
                        break;
                    case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                        reasonText = "ERROR_UNHANDLED_HTTP_CODE";
                        break;
                    case DownloadManager.ERROR_UNKNOWN:
                        reasonText = "ERROR_UNKNOWN";
                        break;
                }
                break;

            case DownloadManager.STATUS_PAUSED:
                switch(reason){
                    case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                        reasonText = "PAUSED_QUEUED_FOR_WIFI";
                        break;
                    case DownloadManager.PAUSED_UNKNOWN:
                        reasonText = "PAUSED_UNKNOWN";
                        break;
                    case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                        reasonText = "PAUSED_WAITING_FOR_NETWORK";
                        break;
                    case DownloadManager.PAUSED_WAITING_TO_RETRY:
                        reasonText = "PAUSED_WAITING_TO_RETRY";
                        break;
                }
                break;
        }
        return reasonText;
    }
}
