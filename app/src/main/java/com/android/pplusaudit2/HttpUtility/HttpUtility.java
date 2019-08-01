package com.android.pplusaudit2.HttpUtility;

import android.app.ProgressDialog;
import android.content.Context;

import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.General;
import com.android.pplusaudit2.MyMessageBox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ULTRABOOK on 10/6/2015.
 */
public class HttpUtility {

    private static final int BUFFER_SIZE = 4096;
    private Context mContext;
    private MyMessageBox messageBox;

    public HttpUtility(Context ctx) {
        mContext = ctx;
        messageBox = new MyMessageBox(mContext);
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(ctx, General.errlogFile));
    }

    /**
     * Downloads a file from a URL
     * @param fileURL HTTP URL of the file to be downloaded
     * @param saveDir path of the directory to save the file
     * @throws IOException
     */
    public void DownloadFile(String fileURL, String saveDir) throws IOException {

        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

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
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                        fileURL.length());
            }

            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePath = saveDir + File.separator + fileName;

            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);

            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            messageBox.ShowMessage("Download", "Download complete");
        } else {
            messageBox.ShowMessage("Failed" , "Reponse: " + responseCode);
        }
        httpConn.disconnect();
    }
}
