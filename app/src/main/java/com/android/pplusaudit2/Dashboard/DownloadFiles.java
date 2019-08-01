package com.android.pplusaudit2.Dashboard;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.android.pplusaudit2.ErrorLogs.ErrorLog;
import com.android.pplusaudit2.General;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ULTRABOOK on 6/1/2016.
 */
class DownloadFiles extends AsyncTask<Void, String, Boolean> {

    public static Context mContext;
    private ProgressDialog progressDialog;
    private String errmsg = "";
    private File dlpath;
    private String urlDownload;
    private String TAG = "";
    private int BUFFER_SIZE = 4096;

    static File storeDIR;
    static File categoryDIR;
    static File groupDIR;
    static File questionDIR;
    static File formsDIR;
    static File formtypesDIR;
    static File singleselectDIR;
    static File multiselectDIR;
    static File computationalDIR;
    static File conditionalDIR;
    static File secondarylookupDIR;
    static File secondarylistDIR;
    static File osalistDIR;
    static File osalookupDIR;
    static File soslistDIR;
    static File soslookupDIR;
    static File imageListDIR;
    static File npiDIR;
    static File planogramDIR;
    static File pcategoryDIR;
    static File pgroupDIR;
    private ErrorLog errorLog;

    DownloadFiles(Context mContext, String userCode) {
        this.mContext = mContext;
        File appfolder = new File(mContext.getExternalFilesDir(null),"");
        dlpath =  new File(appfolder, "Downloads");
        dlpath.mkdirs();
        urlDownload = General.mainURL + "/api/download?id=" + userCode;
        TAG = mContext.getClass().getSimpleName();
        errorLog = new ErrorLog(General.errlogFile, mContext);
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage("Downloading files.");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(General.ARRAY_FILE_LISTS.length);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        progressDialog.incrementProgressBy(1);
        progressDialog.setMessage("Downloading " + values[0] + ".");
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean result = false;
        String saveDir = Uri.fromFile(dlpath).getPath();

        try{
            for (String type : General.ARRAY_FILE_LISTS) {

                String urlDownloadperFile = urlDownload + "&type=" + type;

                URL url = new URL(urlDownloadperFile);
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
                        fileName = urlDownload.substring(urlDownload.lastIndexOf("/") + 1,
                                urlDownload.length());
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
                    if(type.equals(General.IMG_LIST)) imageListDIR = new File(dlpath, fileName);
                    if(type.equals(General.NPI_LIST)) npiDIR = new File(dlpath, fileName);
                    if(type.equals(General.PLANOGRAM_LIST)) planogramDIR = new File(dlpath, fileName);
                    if(type.equals(General.PERFECT_CATEGORY_LIST)) pcategoryDIR = new File(dlpath, fileName);
                    if(type.equals(General.PERFECT_GROUP_LIST)) pgroupDIR = new File(dlpath, fileName);

                    publishProgress(fileName);

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
                    errmsg = "Error in downloading files.\nResponse code: " + String.valueOf(responseCode);
                }

                httpConn.disconnect();
            }
            result = true;
        }
        catch(IllegalStateException ex) {
            errmsg = "Error in data.";
            String exmsg = ex.getMessage() != null ? ex.getMessage() : errmsg;
            errorLog.appendLog(exmsg, TAG);
            ex.printStackTrace();
        }
        catch (Exception ex) {
            errmsg = "Slow or unstable internet connection";
            String exmsg = ex.getMessage() != null ? ex.getMessage() : errmsg;
            errorLog.appendLog(exmsg, TAG);
            ex.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        progressDialog.dismiss();
        if(DashboardActivity.wlStayAwake.isHeld())
            DashboardActivity.wlStayAwake.release();
        if(!aBoolean) {
            Toast.makeText(mContext, errmsg, Toast.LENGTH_LONG).show();
            return;
        }

        new SaveDownloadedData().execute();
    }
}
