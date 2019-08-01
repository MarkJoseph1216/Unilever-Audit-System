package com.android.pplusaudit2.Dashboard;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.pplusaudit2.AutoUpdateApk.AutoUpdate;
import com.android.pplusaudit2.AutoUpdateApk.CheckUpdateApk;
import com.android.pplusaudit2.Database.SQLLibrary;
import com.android.pplusaudit2.Database.SQLiteDB;
import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.ErrorLogs.ErrorLog;
import com.android.pplusaudit2.General;
import com.android.pplusaudit2.MainActivity;
import com.android.pplusaudit2.MyMessageBox;
import com.android.pplusaudit2.PJP_Compliance.PjpActivity;
import com.android.pplusaudit2.Report.ReportDashboard.ReportsActivity;
import com.android.pplusaudit2.Settings.SettingsActivity;
import com.android.pplusaudit2._Questions.QuestionsActivity;
import com.android.pplusaudit2._Store.StoreActivity;
import com.android.pplusaudit2.R;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Created by ULTRABOOK on 9/30/2015.
 */
public class DashboardActivity extends AppCompatActivity {

    private SQLLibrary sql;
    private MyMessageBox messageBox;
    private ProgressDialog progressDL;
    private String TAG = "";
    private String urlDownload = "";
    private ErrorLog errorLog;

    private enum MENU_MODE {
        CHECK_NEW_UPDATE,
        SEND_ERROR,
        SYNC_MASTERFILE
    }

    private MENU_MODE menuMode;
    static PowerManager.WakeLock wlStayAwake;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity_layout);
        overridePendingTransition(R.anim.slide_up, R.anim.hold);

        PowerManager powerman = (PowerManager) getSystemService(POWER_SERVICE);
        wlStayAwake = powerman.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "wakelocktag");

        General.deviceID = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        General.errlogFile = General.deviceID + ".txt";
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(this, General.errlogFile));

        General.versionName = General.getVersionName(this);
        General.versionCode = General.getVersionCode(this);

        try {
            getSupportActionBar().setTitle(General.versionName);
        }
        catch (NullPointerException ex) { Log.e("TAG", ex.getMessage()); }

        TAG = DashboardActivity.this.getLocalClassName();

        sharedPreferences = getSharedPreferences(getString(R.string.tcr_sharedpref), Context.MODE_PRIVATE);
        General.savedHashKey = sharedPreferences.getString(getString(R.string.pref_hash), "");
        General.oldversionCode = sharedPreferences.getInt(getString(R.string.pref_oldvcode), General.versionCode);
        General.dateLog = sharedPreferences.getString(getString(R.string.pref_date_log), General.getDateToday());
        General.isAdminMode = sharedPreferences.getBoolean(getString(R.string.pref_adminmode), false);

        SharedPreferences.Editor spEdit = sharedPreferences.edit();
        spEdit.putInt(getString(R.string.pref_oldvcode), General.versionCode);
        spEdit.putString(getString(R.string.pref_date_log), General.getDateToday());
        spEdit.apply();

        sql = new SQLLibrary(this);
        messageBox = new MyMessageBox(this);

        final TextView tvwUser = (TextView) findViewById(R.id.tvwUser);

        String strUser = "USER: " + General.userFullName.toUpperCase();
        tvwUser.setText(strUser);

        //MAIN MENU
        final ListView lvwMenu = (ListView) this.findViewById(R.id.lvwDashBoard);
        DashboardAdapter dashboardAdapter = new DashboardAdapter(this, General.mainIconsFont);
        if(General.isAdminMode)
            dashboardAdapter = new DashboardAdapter(this, General.mainIconsFont_admin);

        lvwMenu.setAdapter(dashboardAdapter);

        lvwMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(General.isAdminMode) { // ADMIN MODULES

                    switch (position) {
                        case 0: // AUDIT
                            Cursor cursorStores = sql.GetDataCursor(SQLiteDB.TABLE_STORE);
                            if(cursorStores.getCount() <= 0) {
                                messageBox.ShowMessage("Unavailable", "Store records are empty. Please re-download the files.");
                            }
                            else {
                                Intent intentStore = new Intent(DashboardActivity.this, StoreActivity.class);
                                startActivity(intentStore);
                            }
                            break;
                        case 1: // LOG OUT
                            new LoadStores().execute();
                            break;
                        default:
                            break;
                    }
                }
                else {

                    switch (position) {
                        case 0: // AUDIT
                            Cursor cursorStores = sql.GetDataCursor(SQLiteDB.TABLE_STORE);
                            if (cursorStores.getCount() <= 0) {
                                messageBox.ShowMessage("Unavailable", "Store records are empty. Please re-download the files.");
                            } else {
                                Intent intentStore = new Intent(DashboardActivity.this, StoreActivity.class);
                                startActivity(intentStore);
                            }
                            break;
                        case 1: // PJP COMPLIANCE
                            Intent intentPjp = new Intent(DashboardActivity.this, PjpActivity.class);
                            startActivity(intentPjp);
                            break;
                        case 2: // REPORTS
                            Intent intentReport = new Intent(DashboardActivity.this, ReportsActivity.class);
                            startActivity(intentReport);
                            break;
                        case 3: // SETTINGS
                            Intent intentSettings = new Intent(DashboardActivity.this, SettingsActivity.class);
                            startActivity(intentSettings);
                            break;
                        case 4: // LOGOUT
                            new LoadStores().execute();
                        default:
                            break;
                    }
                }
            }
        });
        // SET UP AUTO UPDATE OF APK
        //new AutoUpdateApk(this);
        errorLog = new ErrorLog(General.errlogFile, this);
        General.mainAutoUpdate = new AutoUpdate(this);
        General.checkUpdateApk = new CheckUpdateApk(this);
        General.checkUpdateApk.started = true;

        errorLog.appendLog("Dashboard run. User: " + General.userFullName, TAG);
    }

    private class LoadStores extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            progressDL = ProgressDialog.show(DashboardActivity.this, "", "Logging out.");
        }

        @Override
        protected Void doInBackground(Void... params) {
            Cursor cursstores = sql.RawQuerySelect("SELECT * FROM " + SQLiteDB.TABLE_STORE + " ORDER BY " + SQLiteDB.COLUMN_STORE_status + " > 0 DESC");
            boolean isAudited = false;
            boolean isPosted = false;

            General.arrPendingStores.clear();

            cursstores.moveToFirst();
            while(!cursstores.isAfterLast()) {

                String storeName = cursstores.getString(cursstores.getColumnIndex(SQLiteDB.COLUMN_STORE_name)).trim().replace("\"", "");
                isAudited = cursstores.getInt(cursstores.getColumnIndex(SQLiteDB.COLUMN_STORE_status)) > 0;
                isPosted = cursstores.getInt(cursstores.getColumnIndex(SQLiteDB.COLUMN_STORE_posted)) == 1;

                if(isAudited && !isPosted) {
                    General.arrPendingStores.add(storeName);
                }

                cursstores.moveToNext();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDL.dismiss();
            final AlertDialog alert = new AlertDialog.Builder(DashboardActivity.this).create();

            String msg = "No previous audit found. Do you want to log out?";
            if(General.arrPendingStores.size() > 0) {
                msg = "Logging out may delete your previous audit survey. Do you want to log out?\n\nThe following store's audit are not yet posted:";
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.logout_prompt_layout, null);
                TextView tvwPrompt = (TextView) layout.findViewById(R.id.tvwLogoutStore);

                String strStore = "";
                for (String storeName : General.arrPendingStores) {
                    strStore += storeName.toUpperCase() + "\n";
                }
                tvwPrompt.setText(strStore);
                alert.setView(layout);
            }

            alert.setMessage(msg);
            alert.setTitle("Log out");
            alert.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            SharedPreferences.Editor spEditor = sharedPreferences.edit();
                            spEditor.putBoolean(getString(R.string.pref_isLogged), false);
                            spEditor.apply();

                            Intent intentmenu = new Intent(DashboardActivity.this, MainActivity.class);
                            startActivity(intentmenu);
                            finish();
                        }
                    });
            alert.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            alert.show();
        }
    }

    private class CheckUpdates extends AsyncTask<Void, Void, Boolean> {

        String messages = "";
        private DefaultHttpClient httpclient = new DefaultHttpClient();

        String urlCheck = AutoUpdate.API_URL_CHECK;

        private boolean hasUpdate = false;

        @Override
        protected void onPreExecute() {
            progressDL = ProgressDialog.show(DashboardActivity.this, "", "Checking for new updates.");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;

            if(General.BETA) urlCheck = AutoUpdate.API_BETA_URL_CHECK;

            try {

                HttpPost post = new HttpPost(urlCheck);
                long start = System.currentTimeMillis();

                HttpParams httpParameters = new BasicHttpParams();
                int timeoutConnection = 10000;
                HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
                int timeoutSocket = 6000;
                HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

                httpclient.setParams(httpParameters);

                String packagename = DashboardActivity.this.getPackageName();

                StringEntity parameters = new StringEntity("pkgname=" + packagename);

                post.setHeader("Content-Type", "application/x-www-form-urlencoded");
                post.setEntity(parameters);
                String response = EntityUtils.toString(httpclient.execute(post).getEntity(), "UTF-8");

                int vcodeAPI = Integer.valueOf(response.trim());
                if (vcodeAPI > General.versionCode) {
                    hasUpdate = true;
                }

                result = true;
            } catch (Exception ex) {
                ex.printStackTrace();
                messages = ex.getMessage() != null ? ex.getMessage() : ex.getLocalizedMessage();
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean bResult) {
            progressDL.dismiss();
            if (!bResult) {
                Toast.makeText(DashboardActivity.this, "Slow or unstable internet connection. Please try again.", Toast.LENGTH_LONG).show();
                return;
            }

            if (hasUpdate) {
                General.mainAutoUpdate.StartAutoUpdate();
                Toast.makeText(DashboardActivity.this, "An update has been released, please update system.", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(DashboardActivity.this, "No new updates available.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        General.verifyStoragePermissions(DashboardActivity.this);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_check_apk, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemID = item.getItemId();

        switch (itemID) {
            case R.id.action_check_apk:
                menuMode = MENU_MODE.CHECK_NEW_UPDATE;
                new CheckInternet().execute();
                break;
            case R.id.action_error:
                menuMode = MENU_MODE.SEND_ERROR;
                new CheckInternet().execute();
                break;
            case R.id.action_sync_masterfile:
                menuMode = MENU_MODE.SYNC_MASTERFILE;
                new CheckInternet().execute();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public class PostErrorReport extends AsyncTask<Void, Void, Boolean> {
        String errMsg = "";
        String response = "";
        @Override
        protected void onPreExecute() {
            progressDL = ProgressDialog.show(DashboardActivity.this, "", "Sending error report to dev team.");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;

            String urlSend = General.mainURL + "/api/uploadtrace";

            if(!errorLog.fileLog.exists()) {
                errMsg = "No errors to send.";
                return result;
            }

            String attachmentName = "data";
            String attachmentFileName = General.errlogFile;
            String crlf = "\r\n";
            String twoHyphens = "--";
            String boundary =  "*****";

            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1*1024*1024;

            try {

                FileInputStream fileInputStream = new FileInputStream(errorLog.fileLog); // text file to upload
                HttpURLConnection httpUrlConnection = null;
                URL url = new URL(urlSend); // url to post
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

                JSONObject jsonResp = new JSONObject(response);
                if(!jsonResp.isNull("msg")) {
                    response = jsonResp.getString("msg");
                }

                responseStream.close();
                httpUrlConnection.disconnect();

                result = true;
            }
            catch (IOException ex) {
                errMsg = ex.getMessage() != null ? ex.getMessage() : "Slow or unstable internet connection.";
                Log.e(TAG, errMsg);
                errorLog.appendLog(errMsg, TAG);
            }
            catch (JSONException ex) {
                errMsg = ex.getMessage() != null ? ex.getMessage() : "Slow or unstable internet connection.";
                Log.e(TAG, errMsg);
                errorLog.appendLog(errMsg, TAG);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean bResult) {
            progressDL.dismiss();
            if(!bResult) {
                Toast.makeText(DashboardActivity.this, errMsg, Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(DashboardActivity.this, response, Toast.LENGTH_SHORT).show();
            errorLog.fileLog.delete();
        }
    }

    private class CheckInternet extends AsyncTask<Void, Void, Boolean> {
        String errmsg = "";

        @Override
        protected void onPreExecute() {
            progressDL = ProgressDialog.show(DashboardActivity.this, "", "Checking internet connection.");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;

            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
            if(activeNetwork != null) {
                if(activeNetwork.isFailover()) errmsg = "Internet connection fail over.";
                result = activeNetwork.isAvailable() || activeNetwork.isConnectedOrConnecting();
            }
            else {
                errmsg = "No internet connection.";
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean bResult) {
            progressDL.dismiss();
            if(!bResult) {
                Toast.makeText(DashboardActivity.this, errmsg, Toast.LENGTH_SHORT).show();
                return;
            }

            switch (menuMode) {
                case CHECK_NEW_UPDATE:
                    new CheckUpdates().execute();
                    break;
                case SYNC_MASTERFILE:
                    new AsyncGetUser().execute();
                    break;
                case SEND_ERROR:
                    new PostErrorReport().execute();
                    break;
                default:
                    break;
            }
        }
    }


    // GET USER FROM WEB
    private class AsyncGetUser extends AsyncTask<Void, Void, Boolean> {

        String errmsg = "";
        String usercode;
        String hashLogged;
        String name;

        protected void onPreExecute() {
            progressDL = ProgressDialog.show(DashboardActivity.this, "", "Verifying user account.", true);
        }

        protected Boolean doInBackground(Void... urls) {

            boolean result = false;

            try {
                String urlfinal = General.mainURL + "/api/auth?email=" + General.userName + "&pwd=" + General.userPassword;
                URL url = new URL(urlfinal);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();

                String response = stringBuilder.toString();

                urlConnection.disconnect();

                JSONObject data = new JSONObject(response);

                if(!data.isNull("msg")) {
                    errmsg = data.getString("msg");
                }
                else {
                    usercode = data.getString("id").trim();
                    name = data.getString("name").trim();
                    hashLogged = data.getString("hash").trim();

                    result = true;
                }
            }
            catch(UnknownHostException e) {
                errmsg = e.getMessage() != null ? e.getMessage() : "Web Host not available. Please check connection.";
                errorLog.appendLog(errmsg, TAG);
                e.printStackTrace();
                Log.e(TAG, errmsg, e);
            }
            catch(IOException e) {
                errmsg = e.getMessage() != null ? e.getMessage() : "Slow or unstable internet connection. Please try again.";
                errorLog.appendLog(errmsg, TAG);
                e.printStackTrace();
                Log.e(TAG, errmsg);
            }
            catch (JSONException e) {
                errmsg = e.getMessage() != null ? e.getMessage() : "Error in data.";
                errorLog.appendLog(errmsg, TAG);
                e.printStackTrace();
                Log.e(TAG, e.getMessage(), e);
            }

            return result;
        }

        protected void onPostExecute(Boolean bResult) {
            progressDL.dismiss();
            if(!bResult) {
                Toast.makeText(DashboardActivity.this, errmsg, Toast.LENGTH_LONG).show();
                return;
            }

            SharedPreferences.Editor spEditor = sharedPreferences.edit();

            General.usercode = usercode;
            General.userFullName = name;

            Cursor cursUser = sql.GetDataCursor(SQLiteDB.TABLE_USER);
            String userCodeLogged = "";
            if(cursUser.moveToFirst()) {
                userCodeLogged = cursUser.getString(cursUser.getColumnIndex("code"));
            }

            Log.e("HASH", General.savedHashKey + " = " + hashLogged);
            spEditor.putString(getString(R.string.pref_hash), hashLogged);
            spEditor.putBoolean(getString(R.string.pref_isLogged), true);
            spEditor.apply();

            if(General.savedHashKey.trim().equals(hashLogged) && (usercode.equals(userCodeLogged))) {
                General.savedHashKey = hashLogged;
                Toast.makeText(DashboardActivity.this, "The masterfile is updated.", Toast.LENGTH_LONG).show();
                return;
            }

            new AlertDialog.Builder(DashboardActivity.this)
                    .setTitle("New Masterfile")
                    .setMessage("New masterfile is available to download.\nThis will erase your previous audits. Would you like to continue?")
                    .setCancelable(false)
                    .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            wlStayAwake.acquire();
                            General.savedHashKey = hashLogged;
                            sql.InitializeAllTables();

                            String[] afields = { SQLiteDB.COLUMN_USER_code, SQLiteDB.COLUMN_USER_name };
                            String[] avalues = { usercode, name };
                            sql.AddRecord(SQLiteDB.TABLE_USER, afields, avalues);
                            new DownloadFiles(DashboardActivity.this, General.usercode).execute();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        }
    }
}
