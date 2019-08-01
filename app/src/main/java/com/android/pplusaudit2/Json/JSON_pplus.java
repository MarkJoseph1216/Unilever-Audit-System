package com.android.pplusaudit2.Json;

import android.content.Context;
import android.os.NetworkOnMainThreadException;

import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.General;
import com.android.pplusaudit2.MyMessageBox;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

/**
 * Created by ULTRABOOK on 9/30/2015.
 */
public class JSON_pplus {

    public static final MediaType JSON = MediaType.parse("application/json; charset=UTF-8");

    private Context mContext;
    private MyMessageBox messageBox;

    public JSON_pplus(Context ctx) {
        mContext = ctx;
        messageBox = new MyMessageBox(mContext);
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(ctx, General.errlogFile));
    }

    public String bodyValue(String email, String password) {
        return "{'email':'1','password':'1'}";
    }

    public String Post(String url, String data) {

        try {
            OkHttpClient client = new OkHttpClient();

            RequestBody body = RequestBody.create(JSON, data);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Call call = client.newCall(request);
            Response response = call.execute();
            return response.body().string().trim();
        }
        catch (NetworkOnMainThreadException ex) {
            messageBox.ShowMessage("NetworkOnMainThreadException", ex.getLocalizedMessage());
            ex.printStackTrace();
        }
        catch (Exception ex) {
            messageBox.ShowMessage("Exception", ex.getMessage());
            ex.printStackTrace();
        }
        return "";
    }
}
