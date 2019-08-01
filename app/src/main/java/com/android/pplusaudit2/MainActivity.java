package com.android.pplusaudit2;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.pplusaudit2.AutoUpdateApk.AutoUpdate;
import com.android.pplusaudit2.Dashboard.DashboardActivity;
import com.android.pplusaudit2.Database.SQLLibrary;
import com.android.pplusaudit2.Database.SQLiteDB;
import com.android.pplusaudit2.Debug.DebugLog;
import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.ErrorLogs.ErrorLog;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private String urlGet;
    private String password;
    private String username;

    private File dlpath;

    private File storeDIR;
    private File categoryDIR;
    private File groupDIR;
    private File questionDIR;
    private File formsDIR;
    private File formtypesDIR;
    private File singleselectDIR;
    private File multiselectDIR;
    private File computationalDIR;
    private File conditionalDIR;
    private File secondarylookupDIR;
    private File secondarylistDIR;
    private File osalistDIR;
    private File osalookupDIR;
    private File soslistDIR;
    private File soslookupDIR;
    private File imageProductDIR;
    private File npiDIR;
    private File planogramDIR;
    private File pcategoryDIR;
    private File pgroupDIR;

    private String urlDownload;
    private String urlDownloadperFile;
    private String urlImage;
    private String urlDownloadImage;
    private static final int BUFFER_SIZE = 4096;
    // -------

    private MyMessageBox messageBox;
    private AlertDialog alertDialog;

    private SQLLibrary sql;
    private SQLiteDB sqLiteDB;

    private View mainLayout;

    private EditText txtUsername;
    private EditText txtPassword;

    private ProgressDialog progressDL;

    private PowerManager.WakeLock wlStayAwake;

    private String TAG = "";
    private boolean isSendError = false;
    private String hashLogged;
    private ArrayList<String> arrStringTemplates;
    private ArrayList<Integer> arrTemplateId;

    private String userCodeLogged = "";
    private int selectedTemplateID = -1;
    private ErrorLog errorLog;
    private SharedPreferences sharedPreferences;
    private TextView tvwSendError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        PowerManager powerman = (PowerManager) getSystemService(POWER_SERVICE);
        wlStayAwake = powerman.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "wakelocktag");

        final TextView tvwVersion = (TextView) findViewById(R.id.tvwVersion);
        General.versionName = "v. " + General.getVersionName(this);
        General.versionCode = General.getVersionCode(this);
        tvwVersion.setText(General.versionName);

        General.deviceID = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        General.errlogFile = General.deviceID + ".txt";
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(this, General.errlogFile));

        TAG = MainActivity.this.getLocalClassName();

        urlGet =  General.mainURL + "/api/auth?";
        urlDownload = General.mainURL + "/api/download?";
        urlImage = General.mainURL + "/api/image?";

        messageBox = new MyMessageBox(this);
        sql = new SQLLibrary(this);

        arrStringTemplates = new ArrayList<>();
        arrTemplateId = new ArrayList<>();

        mainLayout = findViewById(R.id.lnrMain);

        File appfolder = new File(getExternalFilesDir(null), "");

        dlpath =  new File(appfolder, "Downloads");
        dlpath.mkdirs();

        AppSettings.CreateDirs(this);

        Button btnLogin = (Button) findViewById(R.id.btnLogin);

        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        tvwSendError = (TextView) findViewById(R.id.tvwSendError);
        tvwSendError.setMovementMethod(LinkMovementMethod.getInstance());

        tvwSendError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSendError = true;
                new CheckInternet().execute();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password = txtPassword.getText().toString().trim();
                username = txtUsername.getText().toString().trim();

                if(username.isEmpty()) {
                    txtUsername.setError("Username required!");
                    return;
                }

                if(password.isEmpty()) {
                    txtPassword.setError("Password required!");
                    return;
                }

                if(username.isEmpty() && password.isEmpty()) {
                    txtUsername.setError("Username required!");
                    txtPassword.setError("Password required!");
                    return;
                }

                if(General.mainAutoUpdate.isUpdating) {
                    Toast.makeText(MainActivity.this, "New update is downloading. Please wait", Toast.LENGTH_SHORT).show();
                    return;
                }

                isSendError = false;
                General.HideKeyboard(MainActivity.this);
                new CheckInternet().execute();
            }
        });

        sharedPreferences = getSharedPreferences(getString(R.string.tcr_sharedpref), Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean(getString(R.string.pref_isLogged), false);
        General.savedHashKey = sharedPreferences.getString(getString(R.string.pref_hash), "");
        General.oldversionCode = sharedPreferences.getInt(getString(R.string.pref_oldvcode), General.versionCode);
        General.dateLog = sharedPreferences.getString(getString(R.string.pref_date_log), General.getDateToday());
        General.isAdminMode = sharedPreferences.getBoolean(getString(R.string.pref_adminmode), false);

        sqLiteDB = new SQLiteDB(this);

        General.userName = sharedPreferences.getString(getString(R.string.pref_username), "");
        General.userPassword = sharedPreferences.getString(getString(R.string.pref_password), "");

        errorLog = new ErrorLog(General.errlogFile, this);
        errorLog.appendLog("New application run.", TAG);

        SharedPreferences.Editor spEdit = sharedPreferences.edit();
        spEdit.putInt(getString(R.string.pref_oldvcode), General.versionCode);
        spEdit.putString(getString(R.string.pref_date_log), General.getDateToday());
        spEdit.apply();

        if(isLoggedIn) {
            Cursor cursExisitingUser = sql.GetDataCursor(SQLiteDB.TABLE_USER);
            cursExisitingUser.moveToFirst();

            General.usercode = cursExisitingUser.getString(cursExisitingUser.getColumnIndex("code"));
            General.userFullName = cursExisitingUser.getString(cursExisitingUser.getColumnIndex("name"));
            Intent mainIntent = new Intent(MainActivity.this, DashboardActivity.class);
            startActivity(mainIntent);
            finish();
            return;
        }

        General.mainAutoUpdate = new AutoUpdate(this);
        System.setProperty("http.keepAlive", "false");
    }

    private void acquireWake() {
        if(wlStayAwake != null) {
            wlStayAwake.acquire();
        }
    }

    private void releaseWake() {
        if(wlStayAwake != null) {
            wlStayAwake.release();
        }
    }

    private class CheckInternet extends AsyncTask<Void, Void, Boolean> {
        String errmsg = "";

        @Override
        protected void onPreExecute() {
            progressDL = ProgressDialog.show(MainActivity.this, "", "Checking internet connection.", true);
            acquireWake();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;

            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
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
                releaseWake();
                Toast.makeText(MainActivity.this, errmsg, Toast.LENGTH_SHORT).show();
                return;
            }

            if(isSendError) {
                isSendError = false;
                new PostErrorReport().execute();
            }
            else
                new CheckUpdates().execute();
        }
    }

    private class CheckUpdates extends AsyncTask<Void, Void, Boolean> {

        String messages = "";
        private DefaultHttpClient httpclient = new DefaultHttpClient();

        String urlCheck = AutoUpdate.API_URL_CHECK;

        private boolean hasUpdate = false;

        @Override
        protected void onPreExecute() {
            progressDL = ProgressDialog.show(MainActivity.this, "", "Checking for new updates.", true);
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

                String packagename = MainActivity.this.getPackageName();

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
                messages = "Slow or unstable internet connection.";
                String exMsg = ex.getMessage() != null ? ex.getMessage() : messages;
                errorLog.appendLog(exMsg, TAG);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean bResult) {
            progressDL.dismiss();
            if (!bResult) {
                Toast.makeText(MainActivity.this, messages, Toast.LENGTH_LONG).show();
                return;
            }

            if (hasUpdate) {
                General.mainAutoUpdate.StartAutoUpdate();
                Toast.makeText(MainActivity.this, "An update has been released, please update system.", Toast.LENGTH_LONG).show();
                return;
            }

            new AsyncPingWebServer().execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send_error, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_send_error:
                isSendError = true;
                new CheckInternet().execute();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private class AsyncPingWebServer extends AsyncTask<Void, Void, Integer> {
        String errmsg = "";
        @Override
        protected void onPreExecute() {
            progressDL = ProgressDialog.show(MainActivity.this, "", "Checking web server availability.", true);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            int nRet = 0;

            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                try {
                    URL url = new URL(General.mainURL);   // Change to "http://google.com" for www  test.
                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                    urlc.setConnectTimeout(10 * 1000);          // 10 s.
                    urlc.connect();
                    if (urlc.getResponseCode() == 200) {        // 200 = "OK" code (http connection is fine).
                        Log.wtf("Connection", "Success !");
                        nRet = 1;
                    }
                } catch (MalformedURLException mue) {
                    errmsg = mue.getMessage() != null ? mue.getMessage() : "Can't connect to web server.";
                    errorLog.appendLog(errmsg, TAG);
                    Log.e("MalformedURLException", errmsg);

                    nRet = 3;
                } catch (IOException ie) {
                    errmsg = ie.getMessage() != null ? ie.getMessage() : "Slow or unstable internet connection.";
                    errorLog.appendLog(errmsg, TAG);
                    Log.e("IOException", errmsg);
                    nRet = 3;
                }
            }
            else {
                nRet = 2;
            }

            return nRet;
        }

        @Override
        protected void onPostExecute(Integer nReturn) {
            progressDL.dismiss();

            if(nReturn == 3) { // EXCEPTION ERROR
                Toast.makeText(MainActivity.this, errmsg, Toast.LENGTH_SHORT).show();
                errorLog.appendLog(errmsg, TAG);
                return;
            }

            if(nReturn == 0) { // WEB SERVER IS DOWN
                alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Server Down");
                alertDialog.setMessage(General.mainURL + " web server is down.\nPlease check the server.");
                alertDialog.setCancelable(true);
                alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
                return;
            }

            if(nReturn == 2) { // NO INTERNET CONNECTION
                Toast.makeText(MainActivity.this, "No internet connection. Please connect to the internet.", Toast.LENGTH_LONG).show();
                return;
            }

            hashLogged = "";
            selectedTemplateID = 0;
            new AsyncGetUser().execute();
        }
    }

    // GET USER FROM WEB
    private class AsyncGetUser extends AsyncTask<Void, Void, Boolean> {

        String errmsg = "";
        String usercode;
        String name;

        protected void onPreExecute() {
            progressDL = ProgressDialog.show(MainActivity.this, "", "Verifying user account.", true);
        }

        protected Boolean doInBackground(Void... urls) {

            boolean result = false;

            try {
                String urlfinal = urlGet + "email=" + username + "&pwd=" + password;
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
                    String roleName = data.getString("role_name");
                    int nActive = data.getInt("active");

                    if(nActive == 0) {
                        errmsg = "User: " + name + " is deactivated.";
                    }
                    else { // ACTIVE USERS WILL LOG

                        if (roleName.trim().toLowerCase().equals("admin")) {
                            JSONArray jsonArray = data.getJSONArray("audits");
                            if (jsonArray.length() > 0) {
                                arrTemplateId.clear();
                                arrStringTemplates.clear();
                                General.isAdminMode = true;
                                sharedPreferences.edit().putBoolean(getString(R.string.pref_adminmode), true).apply();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonTemplate = jsonArray.getJSONObject(i);

                                    int id = jsonTemplate.getInt("id");
                                    String strDesc = jsonTemplate.getString("description").trim();

                                    arrTemplateId.add(id);
                                    arrStringTemplates.add(strDesc);
                                }
                                result = true;
                            } else errmsg = "No templates found for admin mode.";
                        } else {
                            sharedPreferences.edit().putBoolean(getString(R.string.pref_adminmode), false).apply();
                            General.isAdminMode = false;
                            result = true;
                        }
                    }
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
                Toast.makeText(MainActivity.this, errmsg, Toast.LENGTH_LONG).show();
                return;
            }

            SharedPreferences.Editor spEditor = sharedPreferences.edit();

            General.usercode = usercode;
            General.userFullName = name;

            Cursor cursUser = sql.GetDataCursor(SQLiteDB.TABLE_USER);
            if(cursUser.moveToFirst()) {
                userCodeLogged = cursUser.getString(cursUser.getColumnIndex("code"));
            }
            cursUser.close();

            General.userName = username.trim();
            General.userPassword = password.trim();

            Log.e("HASH", General.savedHashKey + " = " + hashLogged);
            spEditor.putString(getString(R.string.pref_username), General.userName);
            spEditor.putString(getString(R.string.pref_password), General.userPassword);
            spEditor.apply();

            if(General.isAdminMode) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Choose a template")
                        .setItems(arrStringTemplates.toArray(new String[arrStringTemplates.size()]), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                selectedTemplateID = arrTemplateId.get(which);

                                sql.InitializeAllTables();

                                String[] afields = {SQLiteDB.COLUMN_USER_code, SQLiteDB.COLUMN_USER_name};
                                String[] avalues = {usercode, name};
                                sql.AddRecord(SQLiteDB.TABLE_USER, afields, avalues);

                                urlDownload = urlDownload + "id=" + General.usercode;
                                new DownloadFileTask().execute();

                            }
                        })
                        .setCancelable(true)
                        .create().show();
            }
            else {
                if (General.savedHashKey.trim().equals(hashLogged) && (usercode.equals(userCodeLogged))) {
                    General.savedHashKey = hashLogged;
                    Intent nxtIntent = new Intent(MainActivity.this, DashboardActivity.class);
                    startActivity(nxtIntent);
                    finish();
                    return;
                }

                sql.InitializeAllTables();

                String[] afields = {SQLiteDB.COLUMN_USER_code, SQLiteDB.COLUMN_USER_name};
                String[] avalues = {usercode, name};
                sql.AddRecord(SQLiteDB.TABLE_USER, afields, avalues);

                urlDownload = urlDownload + "id=" + General.usercode;
                new DownloadFileTask().execute();
            }
        }
    }

    // DOWNLOADING IMAGE
    public class AsyncDownloadImage extends AsyncTask<Void, Integer, String> {

        @Override
        protected void onPreExecute() {

            progressDL = new ProgressDialog(MainActivity.this);
            progressDL.setTitle("");
            progressDL.setMessage("Saving product images...");
            progressDL.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDL.setCancelable(false);

            progressDL.show();
        }/*            Cursor cursAllImage = sql.GetDataCursor(SQLiteDB.TABLE_PICTURES);
            cursAllImage.moveToFirst();
            int imgs = cursAllImage.getCount();
            progressDL.setMax(imgs);*/

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDL.setProgress(values[0]);
        }

        @Override
        protected String doInBackground(Void... params) {

            String saveImageDir = Uri.fromFile(AppSettings.imgFolder).getPath();

            Cursor cursAllImage = sql.GetDataCursor(SQLiteDB.TABLE_PICTURES);
            cursAllImage.moveToFirst();

            if(cursAllImage.getCount() > 0) {
                try{
                    cursAllImage.moveToFirst();
                    while (!cursAllImage.isAfterLast()) {

                        String productImageName = cursAllImage.getString(cursAllImage.getColumnIndex(SQLiteDB.COLUMN_PICTURES_name)).toLowerCase();

                        // CHECK IF EXISTING
                        File fExisting = new File(AppSettings.imgFolder, productImageName.toLowerCase().trim().replace("\"",""));
                        if(!fExisting.exists()) {
                            imageProductDIR = new File(AppSettings.imgFolder, productImageName);
                        }
                        else {
                            cursAllImage.moveToNext();
                            continue;
                        }

                        urlDownloadImage = urlImage + "name=" + productImageName;

                        URL url = new URL(urlDownloadImage);
                        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                        final int responseCode = httpConn.getResponseCode();

                        // always check HTTP response code first
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            String fileName = "";
                            String disposition = httpConn.getHeaderField("Content-Disposition");
                            String contentType = httpConn.getContentType();
                            int contentLength = httpConn.getContentLength();

                            if (disposition != null) {
                                // extracts file name from header field
                                int index = disposition.indexOf("filename=");
                                if (index > 0) {
                                    fileName = disposition.substring(index + 10,
                                            disposition.length() - 1);
                                }
                            } else {
                                // extracts file name from URL
/*                                fileName = urlDownloadImage.substring(urlDownloadImage.lastIndexOf("/") + 1, urlDownloadImage.length());*/
                                fileName = productImageName.toLowerCase();
                            }

                            // opens input stream from the HTTP connection
                            InputStream inputStream = httpConn.getInputStream();
                            String saveFilePath = saveImageDir + File.separator + fileName;

                            // opens an output stream to save into file
                            FileOutputStream outputStream = new FileOutputStream(saveFilePath);

                            int bytesRead = -1;
                            byte[] buffer = new byte[BUFFER_SIZE];

                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }

                            outputStream.close();
                            inputStream.close();

                        } else {
                            return "Error in downloading images.\nResponse code: " + String.valueOf(responseCode);
                        }

                        httpConn.disconnect();
                        cursAllImage.moveToNext();
                    }
                    cursAllImage.close();
                }
                catch (FileNotFoundException fex)
                {
                    Log.e("FileNotFoundException", fex.getMessage());
                    fex.printStackTrace();
                    return fex.getMessage();
                }
                catch (MalformedURLException mex)
                {
                    Log.e("MalformedURLException", mex.getMessage());
                    mex.printStackTrace();
                    return mex.getMessage();
                }
                catch (IOException ioex)
                {
                    Log.e("IOException", ioex.getMessage());
                    ioex.printStackTrace();
                    return ioex.getMessage();
                }
            }
            else {
                return "No Image data saved.";
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            progressDL.dismiss();
            if(s != null) {
                messageBox.ShowMessage("Image Download", s);
                try {
                    sql.TruncateTable(SQLiteDB.TABLE_USER);
                }
                catch (Exception e) {
                    String exErr = e.getMessage() != null ? e.getMessage() : "Error truncating user table.";
                    Toast.makeText(MainActivity.this, exErr, Toast.LENGTH_LONG).show();
                    errorLog.appendLog(TAG, exErr);
                }
                return;
            }

            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Done");
            alertDialog.setCancelable(false);
            alertDialog.setMessage("Saving Downloaded data done.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            releaseWake();
                            Intent mainIntent = new Intent(MainActivity.this, DashboardActivity.class);
                            startActivity(mainIntent);
                            finish();
/*                                                    Intent mainIntent = new Intent(MainActivity.this, DashboardActivity.class);
                                                    startActivity(mainIntent);*/
                        }
                    });
            alertDialog.show();
        }
    }

    // DOWNLOADING FILE
    private class DownloadFileTask extends AsyncTask<Void, String, Boolean> {
        String errmsg = "";
        @Override
        protected void onPreExecute() {
            progressDL = new ProgressDialog(MainActivity.this);
            progressDL.setMessage("Downloading files.");
            progressDL.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            //progressDL.setMax(General.ARRAY_FILE_LISTS.length);
            progressDL.setCancelable(false);
            progressDL.show();
        }

        @Override
        protected void onProgressUpdate(String... values) {
//            progressDL.incrementProgressBy(1);
//            progressDL.setMessage("Downloading " + values[0] + ".");
            progressDL.setMessage("Downloading " + values[1] + ".\n\n" + String.format(Locale.getDefault(), "%.2f", Double.valueOf(values[0])) + "Kb downloaded.");

        }

        @Override
        protected Boolean doInBackground(Void... params) {

            boolean result = false;
            String saveDir = Uri.fromFile(dlpath).getPath();

            try{
                for (String type : General.ARRAY_FILE_LISTS) {

                    if(General.isAdminMode)
                        urlDownloadperFile = urlDownload + "&type=" + type + "&audit=" + String.valueOf(selectedTemplateID) + "&version=" + String.valueOf(General.versionCode);
                    else
                        urlDownloadperFile = urlDownload + "&type=" + type + "&version=" + String.valueOf(General.versionCode);

                    URL url = new URL(urlDownloadperFile);
                    HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                    httpConn.setRequestMethod("GET");
                    httpConn.setDoInput(true);
                    final int responseCode = httpConn.getResponseCode();

                    // always check HTTP response code first
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        String fileName = "";
                        String disposition = httpConn.getHeaderField("Content-Disposition");
                        String contentType = httpConn.getContentType();
                        int contentLength = httpConn.getContentLength();

                        if (disposition != null) {
                            // extracts file name from header field
                            int index = disposition.indexOf("filename=");
                            if (index > 0) {
                                fileName = disposition.substring(index + 10, disposition.length() - 1);
                            }
                        } else {
                            // extracts file name from URL
                            fileName = type + ".txt";
                        }

                        // opens input stream from the HTTP connection
                        InputStream inputStream = httpConn.getInputStream();
                        String saveFilePath = saveDir + File.separator + fileName;

                        if(type.equals(General.STORE_LIST)) storeDIR = new File(dlpath, fileName);
                        if(type.equals(General.CATEGORY_LIST)) categoryDIR = new File(dlpath, fileName);
                        if(type.equals(General.GROUP_LIST)) groupDIR = new File(dlpath, fileName);
                        if(type.equals(General.QUESTION_LIST)) questionDIR = new File(dlpath, fileName);
                        if(type.equals(General.FORM_LIST)) formsDIR = new File(dlpath, fileName);
                        if(type.equals(General.FORM_TYPES)) formtypesDIR = new File(dlpath, fileName);
                        if(type.equals(General.QUESTION_SINGLESELECT)) singleselectDIR = new File(dlpath, fileName);
                        if(type.equals(General.QUESTION_MULTISELECT)) multiselectDIR = new File(dlpath, fileName);
                        if(type.equals(General.QUESTION_COMPUTATIONAL)) computationalDIR = new File(dlpath, fileName);
                        if(type.equals(General.QUESTION_CONDITIONAL)) conditionalDIR = new File(dlpath, fileName);
                        if(type.equals(General.SECONDARY_LOOKUP_LIST)) secondarylookupDIR = new File(dlpath, fileName);
                        if(type.equals(General.SECONDARY_LIST)) secondarylistDIR = new File(dlpath, fileName);
                        if(type.equals(General.OSA_LIST)) osalistDIR = new File(dlpath, fileName);
                        if(type.equals(General.OSA_LOOKUP)) osalookupDIR = new File(dlpath, fileName);
                        if(type.equals(General.SOS_LIST)) soslistDIR = new File(dlpath, fileName);
                        if(type.equals(General.SOS_LOOKUP)) soslookupDIR = new File(dlpath, fileName);
                        if(type.equals(General.NPI_LIST)) npiDIR = new File(dlpath, fileName);
                        if(type.equals(General.PLANOGRAM_LIST)) planogramDIR = new File(dlpath, fileName);
                        if(type.equals(General.PERFECT_CATEGORY_LIST)) pcategoryDIR = new File(dlpath, fileName);
                        if(type.equals(General.PERFECT_GROUP_LIST)) pgroupDIR = new File(dlpath, fileName);

                        // opens an output stream to save into file
                        FileOutputStream outputStream = new FileOutputStream(saveFilePath);

                        int bytesRead = -1;
                        byte[] buffer = new byte[BUFFER_SIZE];
                        double totalDownloaded = 0;

                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            totalDownloaded +=  Double.valueOf(bytesRead) / 1000;
                            publishProgress(String.valueOf(totalDownloaded), fileName);
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        outputStream.flush();
                        outputStream.close();
                        inputStream.close();
                        errorLog.appendLog("Download success for " + type, TAG);

                    } else {
                        errmsg = "Error in downloading files: " + type + ".\nResponse code: " + String.valueOf(responseCode);
                        return result;
                    }

                    httpConn.disconnect();
                }
                result = true;
            }
            catch(IllegalStateException ex) {
                errorLog.appendLog(errmsg, TAG);
                errmsg = ex.getMessage() != null ? ex.getMessage() : "Error in data.";
                Log.e(TAG, errmsg);
                ex.printStackTrace();
            }
            catch (Exception ex) {
                errorLog.appendLog(errmsg, TAG);
                errmsg = ex.getMessage() != null ? ex.getMessage() : "Slow or unstable internet connection.";
                Log.e(TAG, errmsg);
                ex.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean bResult) {
            progressDL.dismiss();
            if(!bResult) {
                General.messageBox(MainActivity.this, "Download", errmsg);
                try {
                    sql.TruncateTable(SQLiteDB.TABLE_USER);
                }
                catch (Exception e) {
                    String exErr = e.getMessage() != null ? e.getMessage() : "Error truncating user table.";
                    Toast.makeText(MainActivity.this, exErr, Toast.LENGTH_LONG).show();
                    errorLog.appendLog(TAG, exErr);
                }
                return;
            }

            new SaveDownloadedData().execute();
        }
    }

    private class SaveDownloadedData extends AsyncTask<Void, String, Boolean> {

        int nMaxprogress = 0;
        LineNumberReader lnReader;
        SQLiteDatabase dbase;
        String errmsg = "";
        String presentFile = "";
        int numOfRows = 0;

        @Override
        protected void onPreExecute() {
            dbase = sqLiteDB.getWritableDatabase();
            progressDL = new ProgressDialog(MainActivity.this);
            progressDL.setMessage("Storing downloaded data.. Please wait.");
            progressDL.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
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

            try {

                if(storeDIR.exists()) {
                    try {
                        presentFile = storeDIR.getPath();
                        lnReader = new LineNumberReader(new FileReader(storeDIR));
                        nMaxprogress += Integer.parseInt(lnReader.readLine().trim().replace("\uFEFF", "").replace("\"", ""));
                    }
                    catch (IOException ie) { DebugLog.log(ie.getMessage()); }
                }
                if(categoryDIR.exists()) {
                    try {
                        presentFile = categoryDIR.getPath();
                        lnReader = new LineNumberReader(new FileReader(categoryDIR));
                        nMaxprogress += Integer.parseInt(lnReader.readLine().trim().replace("\uFEFF", "").replace("\"", ""));
                    }
                    catch (IOException ie) { DebugLog.log(ie.getMessage()); }
                }
                if(groupDIR.exists()) {
                    try {
                        presentFile = groupDIR.getPath();
                        lnReader = new LineNumberReader(new FileReader(groupDIR));
                        nMaxprogress += Integer.parseInt(lnReader.readLine().trim().replace("\uFEFF", "").replace("\"", ""));
                    }
                    catch (IOException ie) { DebugLog.log(ie.getMessage()); }
                }
                if(questionDIR.exists()) {
                    try {
                        presentFile = questionDIR.getPath();
                        lnReader = new LineNumberReader(new FileReader(questionDIR));
                        nMaxprogress += Integer.parseInt(lnReader.readLine().trim().replace("\uFEFF", "").replace("\"", ""));
                    }
                    catch (IOException ie) { DebugLog.log(ie.getMessage()); }
                }
                if(formsDIR.exists()) {
                    try {
                        presentFile = formsDIR.getPath();
                        lnReader = new LineNumberReader(new FileReader(formsDIR));
                        nMaxprogress += Integer.parseInt(lnReader.readLine().trim().replace("\uFEFF", "").replace("\"", ""));
                    }
                    catch (IOException ie) { DebugLog.log(ie.getMessage()); }
                }
                if(formtypesDIR.exists()) {
                    try {
                        presentFile = formtypesDIR.getPath();
                        lnReader = new LineNumberReader(new FileReader(formtypesDIR));
                        nMaxprogress += Integer.parseInt(lnReader.readLine().trim().replace("\uFEFF", "").replace("\"", ""));
                    }
                    catch (IOException ie) { DebugLog.log(ie.getMessage()); }
                }
                if(singleselectDIR.exists()) {
                    try {
                        presentFile = singleselectDIR.getPath();
                        lnReader = new LineNumberReader(new FileReader(singleselectDIR));
                        nMaxprogress += Integer.parseInt(lnReader.readLine().trim().replace("\uFEFF", "").replace("\"", ""));
                    }
                    catch (IOException ie) { DebugLog.log(ie.getMessage()); }
                }
                if(multiselectDIR.exists()) {
                    try {
                        presentFile = multiselectDIR.getPath();
                        lnReader = new LineNumberReader(new FileReader(multiselectDIR));
                        nMaxprogress += Integer.parseInt(lnReader.readLine().trim().replace("\uFEFF", "").replace("\"", ""));
                    }
                    catch (IOException ie) { DebugLog.log(ie.getMessage()); }
                }
                if(computationalDIR.exists()) {
                    try {
                        presentFile = computationalDIR.getPath();
                        lnReader = new LineNumberReader(new FileReader(computationalDIR));
                        nMaxprogress += Integer.parseInt(lnReader.readLine().trim().replace("\uFEFF", "").replace("\"", ""));
                    }
                    catch (IOException ie) { DebugLog.log(ie.getMessage()); }
                }
                if(conditionalDIR.exists()) {
                    try {
                        presentFile = conditionalDIR.getPath();
                        lnReader = new LineNumberReader(new FileReader(conditionalDIR));
                        nMaxprogress += Integer.parseInt(lnReader.readLine().trim().replace("\uFEFF", "").replace("\"", ""));
                    }
                    catch (IOException ie) { DebugLog.log(ie.getMessage()); }
                }
                if(secondarylookupDIR.exists()) {
                    try {
                        presentFile = secondarylookupDIR.getPath();
                        lnReader = new LineNumberReader(new FileReader(secondarylookupDIR));
                        nMaxprogress += Integer.parseInt(lnReader.readLine().trim().replace("\uFEFF", "").replace("\"", ""));
                    }
                    catch (IOException ie) { DebugLog.log(ie.getMessage()); }
                }
                if(secondarylistDIR.exists()) {
                    try {
                        presentFile = secondarylistDIR.getPath();
                        lnReader = new LineNumberReader(new FileReader(secondarylistDIR));
                        nMaxprogress += Integer.parseInt(lnReader.readLine().trim().replace("\uFEFF", "").replace("\"", ""));
                    }
                    catch (IOException ie) { DebugLog.log(ie.getMessage()); }
                }
                if(osalistDIR.exists()) {
                    try{
                        presentFile = osalistDIR.getPath();
                        lnReader = new LineNumberReader(new FileReader(osalistDIR));
                        nMaxprogress += Integer.parseInt(lnReader.readLine().trim().replace("\uFEFF", "").replace("\"", ""));
                    }
                    catch (IOException ie) { DebugLog.log(ie.getMessage()); }
                }
                if(osalookupDIR.exists()) {
                    try {
                        presentFile = osalookupDIR.getPath();
                        lnReader = new LineNumberReader(new FileReader(osalookupDIR));
                        nMaxprogress += Integer.parseInt(lnReader.readLine().trim().replace("\uFEFF", "").replace("\"", ""));
                    }
                    catch (IOException ie) { DebugLog.log(ie.getMessage()); }
                }
                if(soslistDIR.exists()) {
                    try {
                        presentFile = soslistDIR.getPath();
                        lnReader = new LineNumberReader(new FileReader(soslistDIR));
                        nMaxprogress += Integer.parseInt(lnReader.readLine().trim().replace("\uFEFF", "").replace("\"", ""));
                    }
                    catch (IOException ie) { DebugLog.log(ie.getMessage()); }
                }
                if(soslookupDIR.exists()) {
                    try {
                        presentFile = soslookupDIR.getPath();
                        lnReader = new LineNumberReader(new FileReader(soslookupDIR));
                        nMaxprogress += Integer.parseInt(lnReader.readLine().trim().replace("\uFEFF", "").replace("\"", ""));
                    }
                    catch (IOException ie) { DebugLog.log(ie.getMessage()); }
                }
                if(npiDIR.exists()) {
                    try {
                        presentFile = npiDIR.getPath();
                        lnReader = new LineNumberReader(new FileReader(npiDIR));
                        nMaxprogress += Integer.parseInt(lnReader.readLine().trim().replace("\uFEFF", "").replace("\"", ""));
                    }
                    catch (IOException ie) { DebugLog.log(ie.getMessage()); }
                }
                if(planogramDIR.exists()) {
                    try {
                        presentFile = planogramDIR.getPath();
                        lnReader = new LineNumberReader(new FileReader(planogramDIR));
                        nMaxprogress += Integer.parseInt(lnReader.readLine().trim().replace("\uFEFF", "").replace("\"", ""));
                    }
                    catch (IOException ie) { DebugLog.log(ie.getMessage()); }
                }
                if(pcategoryDIR.exists()) {
                    try {
                        presentFile = pcategoryDIR.getPath();
                        lnReader = new LineNumberReader(new FileReader(pcategoryDIR));
                        nMaxprogress += Integer.parseInt(lnReader.readLine().trim().replace("\uFEFF", "").replace("\"", ""));
                    }
                    catch (IOException ie) { DebugLog.log(ie.getMessage()); }
                }
                if(pgroupDIR.exists()) {
                    try {
                        presentFile = pgroupDIR.getPath();
                        lnReader = new LineNumberReader(new FileReader(pgroupDIR));
                        nMaxprogress += Integer.parseInt(lnReader.readLine().trim().replace("\uFEFF", "").replace("\"", ""));
                    }
                    catch (IOException ie) { DebugLog.log(ie.getMessage()); }
                }

                progressDL.setMax(nMaxprogress);

                // STORES
                if(storeDIR.exists()) {
                    sql.TruncateTable(SQLiteDB.TABLE_STORE);

                    presentFile = storeDIR.getPath();

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
                            SQLiteDB.COLUMN_STORE_auditid,
                            SQLiteDB.COLUMN_STORE_area,
                            SQLiteDB.COLUMN_STORE_templatetype,
                            SQLiteDB.COLUMN_STORE_remarks
                    };

                    String sqlinsertStore = sql.createInsertBulkQuery(SQLiteDB.TABLE_STORE, afields);
                    SQLiteStatement sqlstatementStore = dbase.compileStatement(sqlinsertStore); // insert into tblsample (fields1,fields2)
                    dbase.beginTransaction();
                    BufferedReader bReader = new BufferedReader(new FileReader(storeDIR));

                    String line;
                    numOfRows = Integer.valueOf(General.cleanString(bReader.readLine().trim())); // num of rows
                    int ctr = 0;

                    while ((line = bReader.readLine()) != null) {
                        final String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                        sqlstatementStore.clearBindings();
                        for (int i = 0; i < afields.length; i++) {
                            sqlstatementStore.bindString((i+1), values[i].trim().replace("\"","").toUpperCase());
                        }
                        sqlstatementStore.execute();

                        ctr++;
                        publishProgress("Saving store data.");
                    }

                    dbase.setTransactionSuccessful();
                    dbase.endTransaction();

                    if(ctr != numOfRows) {
                        errmsg = "Stores data not downloaded completely. Please log again";
                        return false;
                    }
                    errorLog.appendLog("Data saved for " + presentFile, TAG);
                }


                // CATEGORY
                if(categoryDIR.exists()) {
                    sql.TruncateTable(SQLiteDB.TABLE_CATEGORY);

                    presentFile = categoryDIR.getPath();

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

                    BufferedReader bReader = new BufferedReader(new FileReader(categoryDIR));

                    String line;
                    numOfRows = Integer.valueOf(General.cleanString(bReader.readLine().trim())); // num of rows
                    int ctr = 0;

                    while ((line = bReader.readLine()) != null) {
                        final String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                        sqlstatementCategory.clearBindings();
                        for (int i = 0; i < afields.length; i++) {
                            sqlstatementCategory.bindString((i+1), values[i].trim().replace("\"",""));
                        }
                        sqlstatementCategory.execute();

                        ctr++;
                        publishProgress("Saving Category data.");
                    }
                    dbase.setTransactionSuccessful();
                    dbase.endTransaction();

                    if(ctr != numOfRows) {
                        errmsg = "Category data not downloaded completely. Please log again";
                        return false;
                    }
                    errorLog.appendLog("Data saved for " + presentFile, TAG);
                }


                // GROUP
                if(groupDIR.exists()) {
                    sql.TruncateTable(SQLiteDB.TABLE_GROUP);

                    presentFile = groupDIR.getPath();

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

                    BufferedReader bReader = new BufferedReader(new FileReader(groupDIR));

                    String line;

                    numOfRows = Integer.valueOf(General.cleanString(bReader.readLine().trim())); // num of rows
                    int ctr = 0;

                    while ((line = bReader.readLine()) != null) {
                        final String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                        sqlstatementGroup.clearBindings();
                        for (int i = 0; i < afields.length; i++) {
                            sqlstatementGroup.bindString((i+1), values[i].trim().replace("\"",""));
                        }
                        sqlstatementGroup.execute();

                        ctr++;
                        publishProgress("Saving Group data.");
                    }
                    dbase.setTransactionSuccessful();
                    dbase.endTransaction();

                    if(ctr != numOfRows) {
                        errmsg = "Groups data not downloaded completely. Please log again";
                        return false;
                    }
                    errorLog.appendLog("Data saved for " + presentFile, TAG);
                }

                // QUESTIONS
                if(questionDIR.exists()) {
                    sql.TruncateTable(SQLiteDB.TABLE_QUESTION);

                    presentFile = questionDIR.getPath();

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

                    BufferedReader bReader = new BufferedReader(new FileReader(questionDIR));

                    String line;
                    numOfRows = Integer.valueOf(General.cleanString(bReader.readLine().trim())); // num of rows
                    int ctr = 0;

                    while ((line = bReader.readLine()) != null) {
                        final String[] valuesquestion = line.trim().split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1); // split with comma delimeter, not including inside the ""

                        sqlstatementQuestions.clearBindings();
                        for (int i = 0; i < valuesquestion.length; i++) {
                            sqlstatementQuestions.bindString((i + 1), valuesquestion[i].trim().replace("\"", ""));
                        }
                        sqlstatementQuestions.execute();

                        ctr++;
                        publishProgress("Saving questions data.");
                    }
                    dbase.setTransactionSuccessful();
                    dbase.endTransaction();

                    if(ctr != numOfRows) {
                        errmsg = "Questions data not downloaded completely. Please log again";
                        return false;
                    }
                    errorLog.appendLog("Data saved for " + presentFile, TAG);
                }

                // FORMS
                if(formsDIR.exists()) {
                    sql.TruncateTable(SQLiteDB.TABLE_FORMS);

                    presentFile = formsDIR.getPath();

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

                    BufferedReader brForms = new BufferedReader(new FileReader(formsDIR));

                    String line;
                    numOfRows = Integer.valueOf(General.cleanString(brForms.readLine().trim())); // num of rows
                    int ctr = 0;

                    while ((line = brForms.readLine()) != null) {
                        String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                        sqlstatementForms.clearBindings();
                        for (int i = 0; i < values.length; i++) {
                            sqlstatementForms.bindString((i+1), values[i].trim().replace("\"",""));
                        }
                        sqlstatementForms.execute();

                        ctr++;
                        publishProgress("Saving forms data.");
                    }
                    dbase.setTransactionSuccessful();
                    dbase.endTransaction();

                    if(ctr != numOfRows) {
                        errmsg = "Forms data not downloaded completely. Please log again";
                        return false;
                    }
                    errorLog.appendLog("Data saved for " + presentFile, TAG);
                }


                // FORM TYPES
                if(formtypesDIR.exists()) {
                    sql.TruncateTable(SQLiteDB.TABLE_FORMTYPE);

                    presentFile = formtypesDIR.getPath();

                    String[] afields = {
                            SQLiteDB.COLUMN_FORMTYPE_code,
                            SQLiteDB.COLUMN_FORMTYPE_desc
                    };

                    String sqlinsertFormtypes = sql.createInsertBulkQuery(SQLiteDB.TABLE_FORMTYPE, afields);
                    SQLiteStatement sqlstatementFormtypes = dbase.compileStatement(sqlinsertFormtypes);
                    dbase.beginTransaction();

                    BufferedReader brFormtypes = new BufferedReader(new FileReader(formtypesDIR));

                    String line;
                    numOfRows = Integer.valueOf(General.cleanString(brFormtypes.readLine().trim())); // num of rows
                    int ctr = 0;

                    while ((line = brFormtypes.readLine()) != null) {
                        final String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                        sqlstatementFormtypes.clearBindings();
                        for (int i = 0; i < values.length; i++) {
                            sqlstatementFormtypes.bindString((i+1), values[i].trim().replace("\"",""));
                        }
                        sqlstatementFormtypes.execute();

                        ctr++;
                        publishProgress("Saving form types.");
                    }
                    dbase.setTransactionSuccessful();
                    dbase.endTransaction();

                    if(ctr != numOfRows) {
                        errmsg = "Form types data not downloaded completely. Please log again";
                        return false;
                    }
                    errorLog.appendLog("Data saved for " + presentFile, TAG);
                }

                // SINGLE SELECT
                if(singleselectDIR.exists()) {
                    sql.TruncateTable(SQLiteDB.TABLE_SINGLESELECT);

                    presentFile = singleselectDIR.getPath();

                    String[] afields = {
                            SQLiteDB.COLUMN_SINGLESELECT_formid,
                            SQLiteDB.COLUMN_SINGLESELECT_optionid,
                            SQLiteDB.COLUMN_SINGLESELECT_option
                    };

                    String sqlinsertSingle = sql.createInsertBulkQuery(SQLiteDB.TABLE_SINGLESELECT, afields);
                    SQLiteStatement sqlstatementSingle = dbase.compileStatement(sqlinsertSingle);
                    dbase.beginTransaction();

                    BufferedReader bSingleselect = new BufferedReader(new FileReader(singleselectDIR));
                    String line;

                    numOfRows = Integer.valueOf(General.cleanString(bSingleselect.readLine().trim())); // num of rows
                    int ctr = 0;

                    while ((line = bSingleselect.readLine()) != null) {
                        final String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                        sqlstatementSingle.clearBindings();
                        for (int i = 0; i < values.length; i++)
                        {
                            sqlstatementSingle.bindString((i+1), values[i].trim().replace("\"",""));
                        }
                        sqlstatementSingle.execute();

                        ctr++;
                        publishProgress("Saving single select data.");
                    }
                    dbase.setTransactionSuccessful();
                    dbase.endTransaction();

                    if(ctr != numOfRows) {
                        errmsg = "Single select data not downloaded completely. Please log again";
                        return false;
                    }
                    errorLog.appendLog("Data saved for " + presentFile, TAG);
                }

                // MULTI SELECT
                if(multiselectDIR.exists()) {
                    sql.TruncateTable(SQLiteDB.TABLE_MULTISELECT);

                    presentFile = multiselectDIR.getPath();

                    String[] afields = {
                            SQLiteDB.COLUMN_MULTISELECT_formid,
                            SQLiteDB.COLUMN_MULTISELECT_optionid,
                            SQLiteDB.COLUMN_MULTISELECT_option
                    };

                    String sqlinsertMulti = sql.createInsertBulkQuery(SQLiteDB.TABLE_MULTISELECT, afields);
                    SQLiteStatement sqlstatementMulti = dbase.compileStatement(sqlinsertMulti);
                    dbase.beginTransaction();

                    BufferedReader brMultiSelect = new BufferedReader(new FileReader(multiselectDIR));
                    String line;

                    numOfRows = Integer.valueOf(General.cleanString(brMultiSelect.readLine().trim())); // num of rows
                    int ctr = 0;

                    while ((line = brMultiSelect.readLine()) != null) {
                        final String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                        sqlstatementMulti.clearBindings();
                        for (int i = 0; i < values.length; i++) {
                            sqlstatementMulti.bindString((i+1), values[i].trim().replace("\"",""));
                        }
                        sqlstatementMulti.execute();

                        ctr++;
                        publishProgress("Saving multi select data.");
                    }
                    dbase.setTransactionSuccessful();
                    dbase.endTransaction();

                    if(ctr != numOfRows) {
                        errmsg = "Multi select data not downloaded completely. Please log again";
                        return false;
                    }
                    errorLog.appendLog("Data saved for " + presentFile, TAG);
                }

                // COMPUTATIONAL
                if(computationalDIR.exists()) {
                    sql.TruncateTable(SQLiteDB.TABLE_COMPUTATIONAL);

                    presentFile = computationalDIR.getPath();

                    String[] afields = {
                            SQLiteDB.COLUMN_COMPUTATIONAL_formid,
                            SQLiteDB.COLUMN_COMPUTATIONAL_formula
                    };

                    String sqlinsertComp = sql.createInsertBulkQuery(SQLiteDB.TABLE_COMPUTATIONAL, afields);
                    SQLiteStatement sqlstatementComp = dbase.compileStatement(sqlinsertComp);
                    dbase.beginTransaction();

                    BufferedReader brComputational = new BufferedReader(new FileReader(computationalDIR));
                    String line;

                    numOfRows = Integer.valueOf(General.cleanString(brComputational.readLine().trim())); // num of rows
                    int ctr = 0;

                    while ((line = brComputational.readLine()) != null) {
                        final String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                        sqlstatementComp.clearBindings();
                        for (int i = 0; i < values.length; i++) {
                            sqlstatementComp.bindString((i+1), values[i].trim().replace("\"",""));
                        }
                        sqlstatementComp.execute();

                        ctr++;
                        publishProgress("Saving computational data.");
                    }
                    dbase.setTransactionSuccessful();
                    dbase.endTransaction();

                    if(ctr != numOfRows) {
                        errmsg = "Computational data not downloaded completely. Please log again";
                        return false;
                    }
                    errorLog.appendLog("Data saved for " + presentFile, TAG);
                }

                // CONDITIONAL
                if(conditionalDIR.exists()) {
                    sql.TruncateTable(SQLiteDB.TABLE_CONDITIONAL);

                    presentFile = conditionalDIR.getPath();

                    String[] afields = {
                            SQLiteDB.COLUMN_CONDITIONAL_formid,
                            SQLiteDB.COLUMN_CONDITIONAL_condition,
                            SQLiteDB.COLUMN_CONDITIONAL_conditionformsid,
                            SQLiteDB.COLUMN_CONDITIONAL_optionid
                    };

                    String sqlinsertCond = sql.createInsertBulkQuery(SQLiteDB.TABLE_CONDITIONAL, afields);
                    SQLiteStatement sqlstatementCond = dbase.compileStatement(sqlinsertCond);
                    dbase.beginTransaction();

                    BufferedReader brConditional = new BufferedReader(new FileReader(conditionalDIR));
                    String line;
                    numOfRows = Integer.valueOf(General.cleanString(brConditional.readLine().trim())); // num of rows
                    int ctr = 0;

                    while ((line = brConditional.readLine()) != null) {
                        final String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                        sqlstatementCond.clearBindings();
                        for (int i = 0; i < values.length; i++) {
                            sqlstatementCond.bindString((i+1), values[i].trim().replace("\"",""));
                        }
                        sqlstatementCond.execute();

                        ctr++;
                        publishProgress("Saving conditional data.");
                    }
                    dbase.setTransactionSuccessful();
                    dbase.endTransaction();

                    if(ctr != numOfRows) {
                        errmsg = "Conditional data not downloaded completely. Please log again";
                        return false;
                    }
                    errorLog.appendLog("Data saved for " + presentFile, TAG);
                }


                // SECONDARY KEY LIST
                if(secondarylistDIR.exists()) {
                    sql.TruncateTable(SQLiteDB.TABLE_SECONDARYKEYLIST);

                    presentFile = secondarylistDIR.getPath();

                    String[] afields = {
                            SQLiteDB.COLUMN_SECONDARYKEYLIST_keygroupid
                    };

                    String sqlinsertKeyList = sql.createInsertBulkQuery(SQLiteDB.TABLE_SECONDARYKEYLIST, afields);
                    SQLiteStatement sqlstatementKeyList = dbase.compileStatement(sqlinsertKeyList); // insert into tblsample (fields1,fields2)
                    dbase.beginTransaction();

                    BufferedReader bReader = new BufferedReader(new FileReader(secondarylistDIR));

                    String line;

                    numOfRows = Integer.valueOf(General.cleanString(bReader.readLine().trim())); // num of rows
                    int ctr = 0;

                    while ((line = bReader.readLine()) != null) {
                        final String[] values = line.split(",");


                        sqlstatementKeyList.clearBindings();
                        for (int i = 0; i < afields.length; i++) {
                            sqlstatementKeyList.bindString((i+1), values[i].trim().replace("\"",""));
                        }
                        sqlstatementKeyList.execute();

                        ctr++;
                        publishProgress("Saving Secondary Keylist data.");
                    }
                    dbase.setTransactionSuccessful();
                    dbase.endTransaction();

                    if(ctr != numOfRows) {
                        errmsg = "Secondary keylists not downloaded completely. Please log again";
                        return false;
                    }
                    errorLog.appendLog("Data saved for " + presentFile, TAG);
                }

                // SECONDARY DISPLAY
                if(secondarylookupDIR.exists()) {
                    sql.TruncateTable(SQLiteDB.TABLE_SECONDARYDISP);

                    presentFile = secondarylookupDIR.getPath();

                    String[] afields = {
                            SQLiteDB.COLUMN_SECONDARYDISP_storeid,
                            SQLiteDB.COLUMN_SECONDARYDISP_categoryid,
                            SQLiteDB.COLUMN_SECONDARYDISP_brand
                    };

                    String sqlinsertSecDisp = sql.createInsertBulkQuery(SQLiteDB.TABLE_SECONDARYDISP, afields);
                    SQLiteStatement sqlstatementSecDisp = dbase.compileStatement(sqlinsertSecDisp); // insert into tblsample (fields1,fields2)
                    dbase.beginTransaction();

                    BufferedReader bReader = new BufferedReader(new FileReader(secondarylookupDIR));

                    String line;

                    numOfRows = Integer.valueOf(General.cleanString(bReader.readLine().trim())); // num of rows
                    int ctr = 0;

                    while ((line = bReader.readLine()) != null) {
                        final String[] values = line.split(",");


                        sqlstatementSecDisp.clearBindings();
                        for (int i = 0; i < afields.length; i++) {
                            sqlstatementSecDisp.bindString((i+1), values[i].trim().replace("\"",""));
                        }
                        sqlstatementSecDisp.execute();

                        ctr++;
                        publishProgress("Saving Secondary display data.");
                    }
                    dbase.setTransactionSuccessful();
                    dbase.endTransaction();

                    if(ctr != numOfRows) {
                        errmsg = "Secondary display data not downloaded completely. Please log again";
                        return false;
                    }
                    errorLog.appendLog("Data saved for " + presentFile, TAG);
                }


                // OSA LIST
                if(osalistDIR.exists()) {
                    sql.TruncateTable(SQLiteDB.TABLE_OSALIST);

                    presentFile = osalistDIR.getPath();

                    String[] afields = {
                            SQLiteDB.COLUMN_OSALIST_osakeygroupid
                    };

                    String sqlinsertOsalist = sql.createInsertBulkQuery(SQLiteDB.TABLE_OSALIST, afields);
                    SQLiteStatement sqlstatementOsalist = dbase.compileStatement(sqlinsertOsalist); // insert into tblsample (fields1,fields2)
                    dbase.beginTransaction();

                    BufferedReader bReader = new BufferedReader(new FileReader(osalistDIR));

                    String line;
                    numOfRows = Integer.valueOf(General.cleanString(bReader.readLine().trim())); // num of rows
                    int ctr = 0;

                    while ((line = bReader.readLine()) != null) {
                        final String[] values = line.split(",");

                        sqlstatementOsalist.clearBindings();
                        for (int i = 0; i < afields.length; i++) {
                            sqlstatementOsalist.bindString((i+1), values[i].trim().replace("\"",""));
                        }
                        sqlstatementOsalist.execute();

                        ctr++;
                        publishProgress("Saving OSA Lists data.");
                    }
                    dbase.setTransactionSuccessful();
                    dbase.endTransaction();

                    if(ctr != numOfRows) {
                        errmsg = "OSA lists not downloaded completely. Please log again";
                        return false;
                    }
                    errorLog.appendLog("Data saved for " + presentFile, TAG);
                }


                // OSA LOOKUP
                if(osalookupDIR.exists()) {
                    sql.TruncateTable(SQLiteDB.TABLE_OSALOOKUP);

                    presentFile = osalookupDIR.getPath();

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

                    BufferedReader bReader = new BufferedReader(new FileReader(osalookupDIR));

                    String line;
                    numOfRows = Integer.valueOf(General.cleanString(bReader.readLine().trim())); // num of rows
                    int ctr = 0;

                    while ((line = bReader.readLine()) != null) {
                        final String[] values = line.split(",");

                        sqlstatementOsalookup.clearBindings();
                        for (int i = 0; i < afields.length; i++) {
                            sqlstatementOsalookup.bindString((i+1), values[i].trim().replace("\"",""));
                        }
                        sqlstatementOsalookup.execute();

                        ctr++;
                        publishProgress("Saving OSA lookup data.");
                    }
                    dbase.setTransactionSuccessful();
                    dbase.endTransaction();

                    if(ctr != numOfRows) {
                        errmsg = "OSA lookup lists not downloaded completely. Please log again";
                        return false;
                    }
                    errorLog.appendLog("Data saved for " + presentFile, TAG);
                }


                // SOS LIST
                if(soslistDIR.exists()) {
                    sql.TruncateTable(SQLiteDB.TABLE_SOSLIST);

                    presentFile = soslistDIR.getPath();

                    String[] afields = {
                            SQLiteDB.COLUMN_SOSLIST_soskeygroupid
                    };

                    String sqlinsertSoslist = sql.createInsertBulkQuery(SQLiteDB.TABLE_SOSLIST, afields);
                    SQLiteStatement sqlstatementSoslist = dbase.compileStatement(sqlinsertSoslist); // insert into tblsample (fields1,fields2)
                    dbase.beginTransaction();

                    BufferedReader bReader = new BufferedReader(new FileReader(soslistDIR));

                    String line;
                    numOfRows = Integer.valueOf(General.cleanString(bReader.readLine().trim())); // num of rows
                    int ctr = 0;

                    while ((line = bReader.readLine()) != null) {
                        final String[] values = line.split(",");

                        sqlstatementSoslist.clearBindings();
                        for (int i = 0; i < afields.length; i++) {
                            sqlstatementSoslist.bindString((i+1), values[i].trim().replace("\"",""));
                        }
                        sqlstatementSoslist.execute();

                        ctr++;
                        publishProgress("Saving SOS Lists data.");
                    }
                    dbase.setTransactionSuccessful();
                    dbase.endTransaction();

                    if(ctr != numOfRows) {
                        errmsg = "SOS lists not downloaded completely. Please log again";
                        return false;
                    }
                    errorLog.appendLog("Data saved for " + presentFile, TAG);
                }


                // SOS LOOKUP
                if(soslookupDIR.exists()) {
                    sql.TruncateTable(SQLiteDB.TABLE_SOSLOOKUP);

                    presentFile = soslookupDIR.getPath();

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

                    BufferedReader bReader = new BufferedReader(new FileReader(soslookupDIR));

                    String line;
                    numOfRows = Integer.valueOf(General.cleanString(bReader.readLine().trim())); // num of rows
                    int ctr = 0;

                    while ((line = bReader.readLine()) != null) {
                        final String[] values = line.split(",");


                        sqlstatementSoslookup.clearBindings();
                        for (int i = 0; i < afields.length; i++) {
                            sqlstatementSoslookup.bindString((i+1), values[i].trim().replace("\"",""));
                        }
                        sqlstatementSoslookup.execute();

                        ctr++;
                        publishProgress("Saving SOS lookup data.");
                    }
                    dbase.setTransactionSuccessful();
                    dbase.endTransaction();

                    if(ctr != numOfRows) {
                        errmsg = "SOS lookup lists not downloaded completely. Please log again";
                        return false;
                    }
                    errorLog.appendLog("Data saved for " + presentFile, TAG);
                }

                // NPI LIST
                if(npiDIR.exists()) {
                    sql.TruncateTable(SQLiteDB.TABLE_NPI);

                    presentFile = npiDIR.getPath();

                    String[] afields = {
                            SQLiteDB.COLUMN_NPI_keygroupid
                    };

                    String strInsertNpi = sql.createInsertBulkQuery(SQLiteDB.TABLE_NPI, afields);
                    SQLiteStatement sqLiteStatement = dbase.compileStatement(strInsertNpi); // insert into tblsample (fields1,fields2)
                    dbase.beginTransaction();

                    BufferedReader bReader = new BufferedReader(new FileReader(npiDIR));

                    String line;
                    numOfRows = Integer.valueOf(General.cleanString(bReader.readLine().trim())); // num of rows
                    int ctr = 0;

                    while ((line = bReader.readLine()) != null) {
                        final String[] values = line.split(",");

                        sqLiteStatement.clearBindings();
                        for (int i = 0; i < afields.length; i++) {
                            sqLiteStatement.bindString((i+1), values[i].trim().replace("\"",""));
                        }
                        sqLiteStatement.execute();

                        ctr++;
                        publishProgress("Saving NPI Lists data.");
                    }
                    dbase.setTransactionSuccessful();
                    dbase.endTransaction();

                    if(ctr != numOfRows) {
                        errmsg = "NPI list not downloaded completely. Please log again";
                        return false;
                    }
                    errorLog.appendLog("Data saved for " + presentFile, TAG);
                }

                // PLANOGRAM LIST
                if(planogramDIR.exists()) {
                    sql.TruncateTable(SQLiteDB.TABLE_PLANOGRAM);

                    presentFile = planogramDIR.getPath();

                    String[] afields = {
                            SQLiteDB.COLUMN_PLANOGRAM_keygroupid
                    };

                    String strInsertPlanogram = sql.createInsertBulkQuery(SQLiteDB.TABLE_PLANOGRAM, afields);
                    SQLiteStatement sqLiteStatement = dbase.compileStatement(strInsertPlanogram); // insert into tblsample (fields1,fields2)
                    dbase.beginTransaction();

                    BufferedReader bReader = new BufferedReader(new FileReader(planogramDIR));

                    String line;
                    numOfRows = Integer.valueOf(General.cleanString(bReader.readLine().trim())); // num of rows
                    int ctr = 0;

                    while ((line = bReader.readLine()) != null) {
                        final String[] values = line.split(",");

                        sqLiteStatement.clearBindings();
                        for (int i = 0; i < afields.length; i++) {
                            sqLiteStatement.bindString((i+1), values[i].trim().replace("\"",""));
                        }
                        sqLiteStatement.execute();

                        ctr++;
                        publishProgress("Saving Planogram Lists data.");
                    }
                    dbase.setTransactionSuccessful();
                    dbase.endTransaction();

                    if(ctr != numOfRows) {
                        errmsg = "Planogram list not downloaded completely. Please log again";
                        return false;
                    }
                    errorLog.appendLog("Data saved for " + presentFile, TAG);
                }

                // PERFECT CATEGORY LIST
                if(pcategoryDIR.exists()) {
                    sql.TruncateTable(SQLiteDB.TABLE_PERFECT_CATEGORY);

                    presentFile = pcategoryDIR.getPath();

                    //JASONMOD
                    String[] afields = {
                            SQLiteDB.COLUMN_PCATEGORY_categoryid,
                            SQLiteDB.COLUMN_PCATEGORY_audittempid
                    };

                    String strInsertPcategory = sql.createInsertBulkQuery(SQLiteDB.TABLE_PERFECT_CATEGORY, afields);
                    SQLiteStatement sqLiteStatement = dbase.compileStatement(strInsertPcategory); // insert into tblsample (fields1,fields2)
                    dbase.beginTransaction();

                    BufferedReader bReader = new BufferedReader(new FileReader(pcategoryDIR));

                    String line;
                    numOfRows = Integer.valueOf(General.cleanString(bReader.readLine().trim())); // num of rows
                    int ctr = 0;

                    while ((line = bReader.readLine()) != null) {
                        final String[] values = line.split(",");

                        sqLiteStatement.clearBindings();
                        for (int i = 0; i < afields.length; i++) {
                            sqLiteStatement.bindString((i+1), values[i].trim().replace("\"",""));
                        }
                        sqLiteStatement.execute();

                        ctr++;
                        publishProgress("Saving Perfect Category Lists data.");
                    }
                    dbase.setTransactionSuccessful();
                    dbase.endTransaction();

                    if(ctr != numOfRows) {
                        errmsg = "Perfect category list not downloaded completely. Please log again";
                        return false;
                    }
                    errorLog.appendLog("Data saved for " + presentFile, TAG);
                }


                // PERFECT GROUP LIST
                if(pgroupDIR.exists()) {
                    sql.TruncateTable(SQLiteDB.TABLE_PERFECT_GROUP);

                    presentFile = pgroupDIR.getPath();

                    String[] afields = {
                            SQLiteDB.COLUMN_PGROUP_groupid
                    };

                    String strInsertgroup = sql.createInsertBulkQuery(SQLiteDB.TABLE_PERFECT_GROUP, afields);
                    SQLiteStatement sqLiteStatement = dbase.compileStatement(strInsertgroup); // insert into tblsample (fields1,fields2)
                    dbase.beginTransaction();

                    BufferedReader bReader = new BufferedReader(new FileReader(pgroupDIR));

                    String line;
                    numOfRows = Integer.valueOf(General.cleanString(bReader.readLine().trim())); // num of rows
                    int ctr = 0;

                    while ((line = bReader.readLine()) != null) {
                        final String[] values = line.split(",");

                        sqLiteStatement.clearBindings();
                        for (int i = 0; i < afields.length; i++) {
                            sqLiteStatement.bindString((i+1), values[i].trim().replace("\"",""));
                        }
                        sqLiteStatement.execute();

                        ctr++;
                        publishProgress("Saving Perfect Group Lists data.");
                    }
                    dbase.setTransactionSuccessful();
                    dbase.endTransaction();

                    if(ctr != numOfRows) {
                        errmsg = "Perfect group list not downloaded completely. Please log again";
                        return false;
                    }
                    errorLog.appendLog("Data saved for " + presentFile, TAG);
                }

                result = true;
            }
            catch (FileNotFoundException fex) {
                fex.printStackTrace();
                errmsg = "file not found. Please log again.\nFILE: " + presentFile;
                String exErr = fex.getMessage() != null ? fex.getMessage() : errmsg;
                errorLog.appendLog(exErr, TAG);
            }
            catch (IOException iex) {
                iex.printStackTrace();
                errmsg = "Data file error. Please log again.\nFILE: " + presentFile;
                String exErr = iex.getMessage() != null ? iex.getMessage() : errmsg;
                errorLog.appendLog(exErr, TAG);
            }
            catch (Exception ex) {
                ex.printStackTrace();
                errmsg = "Some data are corrupted. Please log again.\nFILE: " + presentFile;
                String exErr = ex.getMessage() != null ? ex.getMessage() : errmsg;
                errorLog.appendLog(exErr, TAG);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean bResult) {
            progressDL.dismiss();
            alertDialog = new AlertDialog.Builder(MainActivity.this).create();

            // CLOSE DATABASE CONN
            if(dbase.isOpen()) dbase.close();

            if(!bResult) {
                alertDialog.setTitle("Data error");
                alertDialog.setMessage(errmsg);
                alertDialog.setCancelable(false);
                alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        try {
                            sql.TruncateTable(SQLiteDB.TABLE_USER);
                            finish();
                        }
                        catch (Exception e) {
                            String exErr = e.getMessage() != null ? e.getMessage() : "Error truncating user table.";
                            Toast.makeText(MainActivity.this, exErr, Toast.LENGTH_LONG).show();
                            errorLog.appendLog(TAG, exErr);
                        }
                    }
                });
                alertDialog.show();
                return;
            }

            SharedPreferences.Editor spEditor = sharedPreferences.edit();
            spEditor.putBoolean(getString(R.string.pref_isLogged), true).apply();
            spEditor.putString(getString(R.string.pref_hash), hashLogged).apply();

            alertDialog.setTitle("Done");
            alertDialog.setMessage("Saving Downloaded data done.");
            alertDialog.setCancelable(false);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            releaseWake();
                            Intent mainIntent = new Intent(MainActivity.this, DashboardActivity.class);
                            startActivity(mainIntent);
                            finish();
                        }
                    });
            alertDialog.show();
        }
    }

    private class PostErrorReport extends AsyncTask<Void, Void, Boolean> {
        String errMsg = "";
        String response = "";
        @Override
        protected void onPreExecute() {
            progressDL = ProgressDialog.show(MainActivity.this, "", "Sending error report to dev team.");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;

            String urlSend = General.mainURL + "/api/uploadtrace";

            if(!errorLog.fileLog.exists()) {
                errMsg = "No errors to send.";
                return false;
            }

            String attachmentName = "data";
            String attachmentFileName = General.errlogFile;
            String crlf = "\r\n";
            String twoHyphens = "--";
            String boundary =  "*****";

            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1024 * 1024;

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

                InputStream responseStream = new BufferedInputStream(httpUrlConnection.getInputStream());

                BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));

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
                Toast.makeText(MainActivity.this, errMsg, Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
            errorLog.fileLog.delete();
        }
    }

}
