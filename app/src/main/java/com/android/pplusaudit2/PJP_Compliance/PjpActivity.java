package com.android.pplusaudit2.PJP_Compliance;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.pplusaudit2.AppSettings;
import com.android.pplusaudit2.Database.SQLLibrary;
import com.android.pplusaudit2.Database.SQLiteDB;
import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.ErrorLogs.ErrorLog;
import com.android.pplusaudit2.General;
import com.android.pplusaudit2.R;
import com.android.pplusaudit2._Store.Stores;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PjpActivity extends AppCompatActivity {

    private ListView rvwPjpStores;
    private ProgressDialog progressDialog;
    private SQLLibrary sqlLibrary;
    private PowerManager.WakeLock wlStayAwake;

    private ArrayList<Stores> arrStores;
    private String TAG;
    private LocationManager locManager;
    private LocationListener locListener;
    private double dblLatitude;
    private double dblLongitude;
    private ErrorLog errorLog;
    private Stores storeSel;

    private ArrayList<Compliance> arrCompliances;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pjp_activity_layout);

        TAG = PjpActivity.this.getLocalClassName();
        errorLog = new ErrorLog(General.errlogFile, this);

        PowerManager powerman = (PowerManager) getSystemService(POWER_SERVICE);
        wlStayAwake = powerman.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "wakelocktag");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(this, General.errlogFile));
        overridePendingTransition(R.anim.slide_in_left, R.anim.hold);

        sqlLibrary = new SQLLibrary(this);
        arrStores = new ArrayList<>();
        arrCompliances = new ArrayList<>();
        rvwPjpStores = (ListView) findViewById(R.id.lvwPjpStores);

        dblLatitude = 0;
        dblLongitude = 0;

        this.locListener = new LocationPjpListener();
        this.locManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Button btnBack = (Button) findViewById(R.id.btnBackPjp);
        Button btnPost = (Button) findViewById(R.id.btnPostPjp);

        btnBack.setVisibility(View.GONE);
        btnPost.setVisibility(View.GONE);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(PjpActivity.this)
                        .setCancelable(false)
                        .setTitle("Upload compliance")
                        .setMessage("Do you want to upload all compliance records?")
                        .setPositiveButton("Upload", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                new CheckInternet().execute();
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
        });

        new LoadStores().execute();
    }

    private class LocationPjpListener implements LocationListener {

        public void onLocationChanged(Location location) {
            if (location != null) {
                if (ActivityCompat.checkSelfPermission(PjpActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(PjpActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return;
                }

                locManager.removeUpdates(locListener);

                dblLatitude = location.getLatitude();
                dblLongitude = location.getLongitude();

                String strCity = "";
                String strCountry = "";
                String strSubLocality = "";
                String strFeatureName = "";
                String strAdminArea = "";
                Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());

                try {
                    List<Address> addresses = gcd.getFromLocation(dblLatitude, dblLongitude, 1);
                    if (addresses.size() > 0) {
                        System.out.println(((Address) addresses.get(0)).getLocality());
                        strCity = addresses.get(0).getLocality();
                        strCountry = addresses.get(0).getCountryName();
                        strSubLocality = addresses.get(0).getSubLocality();
                        strFeatureName = addresses.get(0).getFeatureName();
                        strAdminArea = addresses.get(0).getAdminArea();
                    }
                    ((Address) addresses.get(0)).getLocality();
                } catch (IOException ex) {
                    String err = "Can't get location.";
                    String excErr = ex.getMessage() != null ? ex.getMessage() : err;
                    errorLog.appendLog(excErr, TAG);
                }

                String strCompleteAddress = strFeatureName + ", " + strSubLocality + ", " + strCity + " city, " + strAdminArea + ", " + strCountry;
                errorLog.appendLog("You are at " + strCompleteAddress, TAG);

                if(dblLongitude == 0.0 || dblLatitude == 0.0) {
                    progressDialog.dismiss();
                    Toast.makeText(PjpActivity.this, "Can't locate GPS coordinates. Please try again.", Toast.LENGTH_LONG).show();
                    return;
                }

                AddPjpRecord(storeSel, strCompleteAddress);
            }
        }

        public void onProviderDisabled(String provider) { }
        public void onProviderEnabled(String provider) { }
        public void onStatusChanged(String provider, int status, Bundle extras) { }
    }

    private class CheckInternet extends AsyncTask<Void, Void, Boolean> {
        String errmsg = "";

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(PjpActivity.this, "", "Checking internet connection.");
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
            progressDialog.dismiss();
            if(!bResult) {
                Toast.makeText(PjpActivity.this, "No internet connection.", Toast.LENGTH_SHORT).show();
                dblLatitude = 0;
                dblLongitude = 0;
                AddPjpRecord(storeSel, "");
                return;
            }

            progressDialog = ProgressDialog.show(PjpActivity.this, "", "Recording data and location. Please wait.");
            CheckIn();
        }
    }

    private class PostCheckedPjp extends AsyncTask<Void, Void, Boolean> {
        private String errMsg;
        private String response;
        private String strFileName;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(PjpActivity.this, "", "Posting PJP compliance record. Please wait.");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;

            if(arrStores.size() == 0) {
                errMsg = "No stores found.";
                return false;
            }

            strFileName = General.usercode + "_" + String.valueOf(arrStores.get(0).auditID) + "_" + General.getDateToday().trim().replace("/", "") + ".csv";
            File fileCheckIns = new File(AppSettings.PjpFolder, strFileName);

            try {

                if(fileCheckIns.exists()) fileCheckIns.delete();

                FileWriter fileWriter = new FileWriter(fileCheckIns);
                String strBody = "";

                for (Stores stores : arrStores) {

                    if(stores.complianceArrayList.size() > 0 || stores.complianceArrayList != null) {

                        for (Compliance compliancePerStore : stores.complianceArrayList) {
                            strBody += General.usercode + "|";
                            strBody += String.valueOf(stores.auditID) + "|";
                            strBody += stores.account + "|";
                            strBody += stores.customerCode + "|";
                            strBody += stores.customer + "|";
                            strBody += stores.area + "|";
                            strBody += stores.regionCode + "|";
                            strBody += stores.region + "|";
                            strBody += stores.distributorCode + "|";
                            strBody += stores.distributor + "|";
                            strBody += stores.storeCode + "|";
                            strBody += stores.storeName + "|";
                            strBody += compliancePerStore.date.trim() + "-" + compliancePerStore.time + "|";
                            strBody += String.valueOf(compliancePerStore.latitude) + "|";
                            strBody += String.valueOf(compliancePerStore.longitude);
                            strBody += "\n";
                        }
                    }
                }
                fileWriter.append(strBody);
                fileWriter.flush();
                fileWriter.close();

                String attachmentName = "data";
                String attachmentFileName;
                String crlf = "\r\n";
                String twoHyphens = "--";
                String boundary =  "*****";

                response = "";

                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1024 * 1024;

                HttpURLConnection httpUrlConnection = null;

                FileInputStream fileInputStream = new FileInputStream(fileCheckIns); // text file to upload

                URL url = new URL(General.URL_UPLOAD_CHECKIN); // url to post
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
                        attachmentName + "\";filename=\"" + strFileName + "\"" + crlf);
                request.writeBytes(crlf);

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // Read file
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0)
                {
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
                responseStream.close();

                JSONObject jsonObject = new JSONObject(response);
                if(!jsonObject.isNull("msg")) {
                    int status = jsonObject.getInt("status");
                    String msg = jsonObject.getString("msg");

                    if(status == 0) {
                        response = msg.trim();

                        SetCompliancesToPosted();
                        
                        result = true;
                    }
                    else {
                        errMsg = msg.trim();
                    }
                }
            }
            catch (IOException ex) {
                errMsg = "Slow or unstable internet connection. Please try again.";
                String exErr = ex.getMessage() != null ? ex.getMessage() : errMsg;
                errorLog.appendLog(exErr, TAG);
            }
            catch (JSONException ex) {
                ex.printStackTrace();
                errMsg = "Error in web response of server. Please try again";
                String errmsg = ex.getMessage() != null ? ex.getMessage() : errMsg;
                errorLog.appendLog(errmsg, TAG);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean bResult) {
            progressDialog.dismiss();
            releaseWake();
            if(!bResult) {
                Toast.makeText(PjpActivity.this, errMsg, Toast.LENGTH_LONG).show();
                return;
            }

            Toast.makeText(PjpActivity.this, "Checked in records are successfully uploaded.", Toast.LENGTH_LONG).show();
        }
    }

    private void SetCompliancesToPosted() {
        for (Stores stores : arrStores) {
            if (stores.complianceArrayList.size() > 0 || stores.complianceArrayList != null) {

                for (Compliance compliancePerStore : stores.complianceArrayList) {
                    sqlLibrary.ExecSQLWrite("UPDATE " + SQLiteDB.TABLE_PJPCOMP
                            + " SET " + SQLiteDB.COLUMN_PJPCOMP_posted + " = '1'"
                            + " WHERE " + SQLiteDB.COLUMN_PJPCOMP_storeid + " = '" + stores.storeID + "'"
                            + " AND " + SQLiteDB.COLUMN_PJPCOMP_id + " = '" + compliancePerStore.complianceID + "'");

                }
            }

        }
    }

    private class LoadStores extends AsyncTask<Void, Void, Boolean> {
        private String errorMsg;
        List<Stores> lstStores;

        @Override
        protected void onPreExecute() {
            lstStores = new ArrayList<>();
            arrCompliances.clear();
            progressDialog = ProgressDialog.show(PjpActivity.this, "", "Loading Stores. Please wait");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean res = false;

            try {
                Cursor cursorPjp = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_STORE);

                if (cursorPjp.moveToFirst()) {
                    while (!cursorPjp.isAfterLast()) {

                        int nstoreid = cursorPjp.getInt(cursorPjp.getColumnIndex(SQLiteDB.COLUMN_STORE_id));
                        String storeCode = cursorPjp.getString(cursorPjp.getColumnIndex(SQLiteDB.COLUMN_STORE_storecode));
                        String webStoreid = cursorPjp.getString(cursorPjp.getColumnIndex(SQLiteDB.COLUMN_STORE_storeid));
                        String storename = cursorPjp.getString(cursorPjp.getColumnIndex(SQLiteDB.COLUMN_STORE_name)).trim().replace("\"", "");
                        int templateid = cursorPjp.getInt(cursorPjp.getColumnIndex(SQLiteDB.COLUMN_STORE_audittempid));
                        String templatename = cursorPjp.getString(cursorPjp.getColumnIndex(SQLiteDB.COLUMN_STORE_templatename)).trim().replace("\"", "");
                        boolean isAudited = cursorPjp.getInt(cursorPjp.getColumnIndex(SQLiteDB.COLUMN_STORE_status)) > 0;
                        int nStatus = cursorPjp.getInt(cursorPjp.getColumnIndex(SQLiteDB.COLUMN_STORE_status));
                        int finalValue = cursorPjp.getInt(cursorPjp.getColumnIndex(SQLiteDB.COLUMN_STORE_final));
                        int initialValue = cursorPjp.getInt(cursorPjp.getColumnIndex(SQLiteDB.COLUMN_STORE_initial));
                        boolean isPosted = cursorPjp.getInt(cursorPjp.getColumnIndex(SQLiteDB.COLUMN_STORE_posted)) == 1;
                        int gMatrixId = cursorPjp.getInt(cursorPjp.getColumnIndex(SQLiteDB.COLUMN_STORE_gradematrixid));
                        int auditID = cursorPjp.getInt(cursorPjp.getColumnIndex(SQLiteDB.COLUMN_STORE_auditid));
                        String account = cursorPjp.getString(cursorPjp.getColumnIndex(SQLiteDB.COLUMN_STORE_account));
                        String customerCode = cursorPjp.getString(cursorPjp.getColumnIndex(SQLiteDB.COLUMN_STORE_customercode));
                        String customer = cursorPjp.getString(cursorPjp.getColumnIndex(SQLiteDB.COLUMN_STORE_customer));
                        String area = cursorPjp.getString(cursorPjp.getColumnIndex(SQLiteDB.COLUMN_STORE_area));
                        String regionCode = cursorPjp.getString(cursorPjp.getColumnIndex(SQLiteDB.COLUMN_STORE_regioncode));
                        String region = cursorPjp.getString(cursorPjp.getColumnIndex(SQLiteDB.COLUMN_STORE_region));
                        String distCode = cursorPjp.getString(cursorPjp.getColumnIndex(SQLiteDB.COLUMN_STORE_distributorcode));
                        String dist = cursorPjp.getString(cursorPjp.getColumnIndex(SQLiteDB.COLUMN_STORE_distributor));
                        String remarks = cursorPjp.getString(cursorPjp.getColumnIndex(SQLiteDB.COLUMN_STORE_remarks));

                        Stores newStore = new Stores(nstoreid, storeCode, webStoreid, storename, templateid, templatename, finalValue, initialValue, isAudited, isPosted, gMatrixId);
                        newStore.auditID = auditID;
                        newStore.account = account;
                        newStore.customerCode = customerCode;
                        newStore.customer = customer;
                        newStore.area = area;
                        newStore.regionCode = regionCode;
                        newStore.region = region;
                        newStore.distributorCode = distCode;
                        newStore.distributor = dist;
                        newStore.remarks = remarks;
                        newStore.status = nStatus;

                        Cursor cursorCompliance = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_PJPCOMP, SQLiteDB.COLUMN_PJPCOMP_storeid + " = '" + nstoreid + "' AND " + SQLiteDB.COLUMN_PJPCOMP_usercode + " = '" + General.usercode + "' ORDER BY " + SQLiteDB.COLUMN_PJPCOMP_id);
                        if (cursorCompliance.moveToFirst()) {
                            arrCompliances.clear();
                            while (!cursorCompliance.isAfterLast()) {

                                int id = cursorCompliance.getInt(cursorCompliance.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_id));
                                String usercode = cursorCompliance.getString(cursorCompliance.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_usercode));
                                int storeid = cursorCompliance.getInt(cursorCompliance.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_storeid));
                                String date = cursorCompliance.getString(cursorCompliance.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_date));
                                String time = cursorCompliance.getString(cursorCompliance.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_time));
                                String longitude = cursorCompliance.getString(cursorCompliance.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_longitude));
                                String latitude = cursorCompliance.getString(cursorCompliance.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_latitude));
                                String address = cursorCompliance.getString(cursorCompliance.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_address));
                                boolean posted = cursorCompliance.getInt(cursorCompliance.getColumnIndex(SQLiteDB.COLUMN_PJPCOMP_posted)) == 1;

                                Compliance compliance = new Compliance(id, usercode, storeid, webStoreid, date, time, General.userFullName, longitude, latitude, posted);
                                compliance.address = address.trim();

                                if(date.trim().equals(General.getDateToday())) {
                                    newStore.dateCheckedIn = date;
                                    newStore.timeChecked = time;
                                    newStore.addressChecked = address;
                                    newStore.isChecked = true;
                                }

                                arrCompliances.add(compliance);
                                cursorCompliance.moveToNext();
                            }

                            newStore.complianceArrayList.clear();
                            newStore.complianceArrayList.addAll(arrCompliances);
                        }
                        cursorCompliance.close();

                        lstStores.add(newStore);
                        cursorPjp.moveToNext();
                    }
                }

                cursorPjp.close();
                res = true;
            } catch (Exception ex) {
                errorMsg = "Data Error in stores.";
                String exMsg = ex.getMessage() != null ? ex.getMessage() : errorMsg;
                errorLog.appendLog(exMsg, TAG);
            }

            return res;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            progressDialog.dismiss();
            if (!aBoolean) {
                General.messageBox(PjpActivity.this, "Loading stores", errorMsg);
                return;
            }
            arrStores.clear();
            arrStores.addAll(lstStores);
            StoreCompAdapter adapter = new StoreCompAdapter(PjpActivity.this, arrStores, arrCompliances);
            rvwPjpStores.setAdapter(adapter);
        }
    }

    // btnCheckin
    public void onClickCheckIn(View v) {

        if (CheckGpsLocation()) {

            storeSel = (Stores) v.getTag();

            Cursor cursor = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_PJPCOMP, SQLiteDB.COLUMN_PJPCOMP_storeid + " = '" + storeSel.storeID + "' AND " + SQLiteDB.COLUMN_PJPCOMP_date + " = '" + General.getDateToday() + "'");
            cursor.moveToFirst();
            int count = cursor.getCount();
            if (count == 0) {

                new AlertDialog.Builder(PjpActivity.this)
                        .setTitle("Check in")
                        .setMessage("Do you want to check this store for audit?")
                        .setPositiveButton("Check in", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                new CheckInternet().execute();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
            } else
                Toast.makeText(PjpActivity.this, "This store is already checked in.", Toast.LENGTH_LONG).show();
        }
    }

    private void AddPjpRecord(Stores storeSelected, String address) {

        String[] aFields = new String[]{
                SQLiteDB.COLUMN_PJPCOMP_usercode,
                SQLiteDB.COLUMN_PJPCOMP_storeid,
                SQLiteDB.COLUMN_PJPCOMP_webstoreid,
                SQLiteDB.COLUMN_PJPCOMP_date,
                SQLiteDB.COLUMN_PJPCOMP_time,
                SQLiteDB.COLUMN_PJPCOMP_longitude,
                SQLiteDB.COLUMN_PJPCOMP_latitude,
                SQLiteDB.COLUMN_PJPCOMP_address,
        };

        String[] aValues = new String[]{
                General.usercode,
                String.valueOf(storeSelected.storeID),
                storeSelected.webStoreID,
                General.getDateToday(),
                General.getTimeToday(),
                String.valueOf(dblLongitude),
                String.valueOf(dblLatitude),
                address.trim()
        };

        try {
            progressDialog.dismiss();
            sqlLibrary.AddRecord(SQLiteDB.TABLE_PJPCOMP, aFields, aValues);
            Toast.makeText(PjpActivity.this, "Successfully checked in for " + storeSelected.storeName, Toast.LENGTH_LONG).show();
            new LoadStores().execute();
        } catch (Exception ex) {
            Toast.makeText(PjpActivity.this, "Error in saving: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean CheckIn() {
        boolean result = false;

        try {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return result;
            }

            locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locListener);
            result = true;
        }
        catch (Exception ex) {
            String strMsg = "Error in getting gps and network provider.";
            String err = ex.getMessage() != null ? ex.getMessage() : strMsg;
            errorLog.appendLog(err, TAG);
            Toast.makeText(this, strMsg, Toast.LENGTH_LONG).show();
        }

        return result;
    }

    private boolean CheckGpsLocation() {
        boolean result = false;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }

        boolean isNetworkEnabled = false;
        boolean isGpsEnabled = false;

        try {
            isNetworkEnabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            isGpsEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            String strMsg = "Error in getting gps and network provider.";
            String err = ex.getMessage() != null ? ex.getMessage() : strMsg;
            errorLog.appendLog(err, TAG);
            return false;
        }

        if (!isGpsEnabled) {
            showGPSDisabledAlertToUser();
            return false;
        }

        result = isNetworkEnabled || isGpsEnabled;

        if(!result)
            Toast.makeText(PjpActivity.this, "GPS Not available in this device.", Toast.LENGTH_LONG).show();

        return result;
    }

    private void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PjpActivity.this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Enable GPS",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void releaseWake() {
        if(wlStayAwake.isHeld()) wlStayAwake.release();
    }

    private void acquireWake() {
        if(wlStayAwake.isHeld()) wlStayAwake.acquire();
    }


    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.hold, R.anim.slide_in_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.hold, R.anim.slide_in_right);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
