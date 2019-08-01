package com.android.pplusaudit2._Questions;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.android.pplusaudit2.AppSettings;
import com.android.pplusaudit2.Database.SQLLibrary;
import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.General;
import com.android.pplusaudit2.MyMessageBox;
import com.android.pplusaudit2.R;
import com.simplify.ink.InkView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by LLOYD on 10/13/2015.
 */
public class Pplus_Questions_signaturepad extends AppCompatActivity {

    MyMessageBox messageBox;
    SQLLibrary sqlLibrary;

    int qid = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.question_6_singature_pad);

        overridePendingTransition(R.anim.slide_up, R.anim.hold);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(this, General.errlogFile));

        messageBox = new MyMessageBox(this);
        sqlLibrary = new SQLLibrary(this);

        final InkView inkSign = (InkView) findViewById(R.id.inkSignature);
        inkSign.setColor(getResources().getColor(android.R.color.black));
        inkSign.setMinStrokeWidth(1.5f);
        inkSign.setMaxStrokeWidth(6f);

        Button btnClear = (Button) this.findViewById(R.id.btnClearSign);
        Button btnSaveSign = (Button) this.findViewById(R.id.btnSaveSign);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            qid = extras.getInt("QUESTION_ID");
        }

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inkSign.clear();
            }
        });
        btnSaveSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog signDialog = new AlertDialog.Builder(Pplus_Questions_signaturepad.this).create();
                signDialog.setTitle("Signature");
                signDialog.setMessage("Are you sure with this signature?");
                signDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String signatureFilename = General.usercode + "_SIGN_" + String.valueOf(qid)  + ".png";
                        
                        Bitmap bmSignature = inkSign.getBitmap(getResources().getColor(R.color.white));
                        AppSettings.uriSignatureTempPath = AppSettings.GetUriQuestionImagePath(signatureFilename);
                        File signfile = new File(AppSettings.uriSignatureTempPath.getPath());

                        FileOutputStream out = null;
                        try {
                            out = new FileOutputStream(signfile);
                            bmSignature.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                            // PNG is a lossless format, the compression factor (100) is ignored
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (out != null) {
                                    out.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        finish();
                    }
                });
                signDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        signDialog.dismiss();
                    }
                });
                signDialog.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition( R.anim.hold, R.anim.slide_down );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == android.R.id.home) {
            finish();
            overridePendingTransition( R.anim.hold, R.anim.slide_down );
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
