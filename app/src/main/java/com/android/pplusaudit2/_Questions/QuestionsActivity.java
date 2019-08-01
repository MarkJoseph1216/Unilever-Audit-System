package com.android.pplusaudit2._Questions;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.pplusaudit2.AppSettings;
import com.android.pplusaudit2.Database.SQLLibrary;
import com.android.pplusaudit2.Database.SQLiteDB;
import com.android.pplusaudit2.ErrorLogs.AutoErrorLog;
import com.android.pplusaudit2.ErrorLogs.ErrorLog;
import com.android.pplusaudit2.General;
import com.android.pplusaudit2.Math.Expression;
import com.android.pplusaudit2.MyMessageBox;
import com.android.pplusaudit2.R;
import com.android.pplusaudit2.Results;
import com.android.pplusaudit2.TCRLib;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by LLOYD on 10/8/2015.
 */
public class QuestionsActivity extends AppCompatActivity {

    private SQLLibrary sqlLibrary;
    private TCRLib tcrLib;
    private String TAG = "";

    private LinearLayout layoutrow = null;
    private TableLayout tblQuestion = null;

    //FOR CONDITIONAL
    private TableLayout tblFormsPerCondition = null;
    private HashMap<Integer, TableLayout> hmCondTableLayout;
    private HashMap<Integer, ArrayList<String>> hmformCondsid;
    private int ctrConditions;
    private HashMap<Integer, String> hmConditionalAnswers;

    //FOR COMPUTATIONAL
    private HashMap<Integer, EditText> hmPerfectStoreValue;
    private HashMap<String, EditText> hmNumericComputational;
    private ArrayList<EditText> arrCompRequired;

    private String storeCategoryGroupID;
    private String storeCategoryID;

    private String formgroupID;
    private String categoryid;

    private HashMap<String, Uri> hmUriImagequestionfile;
    private HashMap<String, Uri>  hmUriCondImagefile;
    private MyMessageBox messageBox;

    // FOR IMAGE CAPTURING
    private ImageView captureImgview;
    private int imgQuestionid;
    private String imgFormtypeid;
    private String imgFilename;

    private ProgressDialog saveProgress;

    private String SOSAnswer = "";

    // <INTEGER KEY = QUESTIONID, HASHMAP<INTEGER = VIEW CustomerStoreItemID, ANSWERCLASS>>
    private HashMap<Integer, HashMap<Integer, Pplus_AnswerClass>> hmlistAnswers;
    private HashMap<Integer, String> hmExpectedAnswers;
    private HashMap<Integer, String> hmFormulalistsByFormid;
    private HashMap<Integer, EditText> hmCompForms;
    private HashMap<Integer, Double> hmCompValues;

    private ArrayList<Integer> arrQuestionid;
    private ArrayList<Integer> arrConditionalQuestionid;
    private ArrayList<View> arrRequired;
    //ArrayList<View> hmChildRequired;
    private HashMap<Integer, View> hmChildRequired;
    private HashMap<Integer, String> hmChildAnswers;
    private HashMap<Integer, CheckBox> hmMultiCheckConditionAns;

    private RadioButton rbSelectedCondition = null;

    // CAMERA CAPTURE DATA
    private int CONDITIONAL_MODE;
    private int CAMERA_CONDFORM_ID;
    private int CAMERA_COND_QUESTION_ID;
    private String CAMERA_COND_CHILDANSWER;
    private int CAMERA_COND_FORMTYPE_ID;

    private ErrorLog errorLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.question_activity_layout);
        overridePendingTransition(R.anim.slide_in_left, R.anim.hold);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Thread.setDefaultUncaughtExceptionHandler(new AutoErrorLog(this, General.errlogFile));
        TAG = QuestionsActivity.this.getLocalClassName();
        errorLog = new ErrorLog(General.errlogFile, this);

        errorLog.appendLog("Auditing.", TAG);

        CONDITIONAL_MODE = 0;
        CAMERA_CONDFORM_ID = 0;
        CAMERA_COND_QUESTION_ID = 0;
        CAMERA_COND_CHILDANSWER = "";
        CAMERA_COND_FORMTYPE_ID = 0;

        sqlLibrary = new SQLLibrary(this);

        Typeface mainTypeface = Typeface.createFromAsset(this.getAssets(), General.typefacename);

        tblQuestion = (TableLayout) findViewById(R.id.tblQuestions);
        final LinearLayout lnrHeader = (LinearLayout) this.findViewById(R.id.lnrHeader);
        Button btnBack = (Button) this.findViewById(R.id.btnBackQuestion);
        Button btnSaveSurvey = (Button) this.findViewById(R.id.btnSaveSurvey);
        btnSaveSurvey.setTypeface(mainTypeface);
        btnSaveSurvey.setText("\uf164" + "  " + "SAVE SURVEY");

        messageBox = new MyMessageBox(this);
        tcrLib = new TCRLib(this);
        hmlistAnswers = new HashMap<>();
        arrQuestionid = new ArrayList<>();
        arrConditionalQuestionid = new ArrayList<>();
        General.hmSignature = new HashMap<>();
        General.hmCondSignature = new HashMap<>();
        hmExpectedAnswers = new HashMap<>();

        hmFormulalistsByFormid = new HashMap<>();
        hmCompForms = new HashMap<>();
        hmCompValues = new HashMap<>();
        hmNumericComputational = new HashMap<>();

        arrRequired = new ArrayList<View>();
        hmChildRequired = new HashMap<>();
        hmChildAnswers = new HashMap<>();
        hmMultiCheckConditionAns = new HashMap<>();
        hmformCondsid = new HashMap<>();
        hmCondTableLayout = new HashMap<>();
        hmConditionalAnswers = new HashMap<>();

        hmPerfectStoreValue = new HashMap<>();

        hmUriImagequestionfile = new HashMap<>();
        hmUriCondImagefile = new HashMap<>();

        arrCompRequired = new ArrayList<>();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnSaveSurvey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(QuestionsActivity.this, "", Toast.LENGTH_SHORT).show();
                String msg = "";
                int totalAnsweredQuestions = 0;

                final AlertDialog dialogSave = new AlertDialog.Builder(QuestionsActivity.this).create();
                dialogSave.setTitle("Survey");

                totalAnsweredQuestions = hmlistAnswers.size();

                if (!CheckRequiredQuestions()) {
                    if (totalAnsweredQuestions == 0)
                        msg = "All targets missed. Please complete survey.";
                    else msg = "Please answer all required questions.";

                    dialogSave.setMessage(msg);
                    dialogSave.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialogSave.dismiss();
                        }
                    });
                    dialogSave.show();
                    return;
                }

                if (totalAnsweredQuestions == arrQuestionid.size()) {
                    msg = "All survey questions accomplished, would you like to save this survey?";
                } else {
                    msg = "Some targets misssed, please verify.";
                }

                dialogSave.setMessage(msg);

                dialogSave.setButton(AlertDialog.BUTTON_POSITIVE, "Save survey", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //SAVE SURVEY
                        Toast.makeText(QuestionsActivity.this, "TEST ", Toast.LENGTH_SHORT).show();
                        errorLog.appendLog("Saving audit. User: " + General.userName, TAG);
                        new SaveSurvey().execute();
                    }
                });

                dialogSave.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialogSave.dismiss();
                    }
                });

                dialogSave.show();
            }
        });


        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            String groupdesc = extras.getString("GROUP_DESC");
            getSupportActionBar().setTitle(groupdesc.trim());

            storeCategoryGroupID = extras.getString("STORE_CATEGORY_GROUP_ID");
            storeCategoryID = extras.getString("STORE_CATEGORY_ID");
            categoryid = extras.getString("CATEGORY_ID");

            Cursor cursGetID = sqlLibrary.RawQuerySelect("SELECT * FROM " + SQLiteDB.TABLE_STORECATEGORYGROUP
                                        + " JOIN " + SQLiteDB.TABLE_GROUP + " ON " + SQLiteDB.TABLE_GROUP + "." + SQLiteDB.COLUMN_GROUP_id + " = " + SQLiteDB.TABLE_STORECATEGORYGROUP + "." + SQLiteDB.COLUMN_STORECATEGORYGROUP_groupid
                                        + " WHERE " + SQLiteDB.TABLE_STORECATEGORYGROUP + "." + SQLiteDB.COLUMN_STORECATEGORYGROUP_id + " = " + storeCategoryGroupID);
            cursGetID.moveToFirst();

            String a = cursGetID.getString(cursGetID.getColumnIndex(SQLiteDB.COLUMN_STORECATEGORYGROUP_groupid)).trim();
            formgroupID = cursGetID.getString(cursGetID.getColumnIndex(SQLiteDB.COLUMN_GROUP_groupid)).trim();
        }

        Cursor cursorQuestions = sqlLibrary.RawQuerySelect("SELECT * FROM " + SQLiteDB.TABLE_STOREQUESTION
                    + " JOIN " + SQLiteDB.TABLE_QUESTION + " ON " + SQLiteDB.TABLE_QUESTION + "." + SQLiteDB.COLUMN_QUESTION_id + " = " + SQLiteDB.TABLE_STOREQUESTION + "." + SQLiteDB.COLUMN_STOREQUESTION_questionid
                    + " WHERE " + SQLiteDB.COLUMN_STOREQUESTION_storecategorygroupid + " = " + storeCategoryGroupID
                    + " ORDER BY " + SQLiteDB.COLUMN_QUESTION_order);

        cursorQuestions.moveToFirst();

        tblQuestion.removeAllViews();

        final BitmapFactory.Options bmoptions = new BitmapFactory.Options();
        bmoptions.inSampleSize = 8;

        ctrConditions = 0;

        while (!cursorQuestions.isAfterLast()) {

                final String formid = cursorQuestions.getString(cursorQuestions.getColumnIndex(SQLiteDB.COLUMN_QUESTION_formid));
                final String formtypeid = cursorQuestions.getString(cursorQuestions.getColumnIndex(SQLiteDB.COLUMN_QUESTION_formtypeid));
                String strPrompt = cursorQuestions.getString(cursorQuestions.getColumnIndex(SQLiteDB.COLUMN_QUESTION_prompt));
                String strExpectedans = cursorQuestions.getString(cursorQuestions.getColumnIndex(SQLiteDB.COLUMN_QUESTION_expectedans));
                String strDefaultAnswer = cursorQuestions.getString(cursorQuestions.getColumnIndex(SQLiteDB.COLUMN_QUESTION_defaultans));
                int nRequired = cursorQuestions.getInt(cursorQuestions.getColumnIndex(SQLiteDB.COLUMN_QUESTION_required));
                int nExempt = cursorQuestions.getInt(cursorQuestions.getColumnIndex(SQLiteDB.COLUMN_QUESTION_exempt));
                String strImgBrand = cursorQuestions.getString(cursorQuestions.getColumnIndex(SQLiteDB.COLUMN_QUESTION_brandpic));

                final int nQuestionid = cursorQuestions.getInt(cursorQuestions.getColumnIndex(SQLiteDB.COLUMN_STOREQUESTION_id));

            String[] aPrompts = null;
            if(strPrompt.contains("<nl>")) {
                aPrompts = strPrompt.split("<nl>");
                if(aPrompts != null) {
                    strPrompt = "";
                    for (String aStrings : aPrompts) {
                        strPrompt += aStrings + "\n";
                    }
                }
            }

                String strBrandImage = "";
                if(!strImgBrand.isEmpty())
                    strBrandImage = strImgBrand.split("\\.")[0];

                String formAnswer = "";

                final int isAnswered = cursorQuestions.getInt(cursorQuestions.getColumnIndex(SQLiteDB.COLUMN_STOREQUESTION_isAnswered));
                if(isAnswered == 1) {
                    formAnswer = cursorQuestions.getString(cursorQuestions.getColumnIndex(SQLiteDB.COLUMN_STOREQUESTION_answer));
                }
                hmExpectedAnswers.put(nQuestionid, strExpectedans);

                switch (formtypeid) {

                    case "1": // 1LABEL
                        layoutrow = (LinearLayout) LayoutInflater.from(QuestionsActivity.this).inflate(R.layout.question_1_label, null);
                        TextView tvwLabel = (TextView) layoutrow.findViewById(R.id.tvwLabel);
                        tvwLabel.setText(strPrompt.trim());
                        tblQuestion.addView(layoutrow);
                        break;

                    case "2": // 1IMAGE CAPTURE
                        layoutrow = (LinearLayout) LayoutInflater.from(QuestionsActivity.this).inflate(R.layout.question_2_imagecapture, null);
                        Button btnAddphoto = (Button) layoutrow.findViewById(R.id.btnAddphoto);
                        final ImageView imgCapture = (ImageView) layoutrow.findViewById(R.id.imgCapture);
                        TextView tvwImageCapture = (TextView) layoutrow.findViewById(R.id.tvwImageCapture);
                        btnAddphoto.setText(strPrompt.trim());
                        tvwImageCapture.setTag(formtypeid + "," + nQuestionid);

                        if(nRequired == 1) arrRequired.add(tvwImageCapture);

                        View.OnClickListener btnAddphoto_clickListener = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                captureImgview = imgCapture;
                                String imageFilename = General.usercode + "_QUESTIONIMG_" + nQuestionid;
                                imgFilename = imageFilename + ".png";

                                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                hmUriImagequestionfile.put(imgFilename, AppSettings.GetUriQuestionImagePath(imageFilename));
                                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, hmUriImagequestionfile.get(imgFilename));

                                CONDITIONAL_MODE = 0;

                                imgQuestionid = nQuestionid;
                                imgFormtypeid = formtypeid;

                                //if(hasPermissionInManifest(QuestionsActivity.this, android.Manifest.permission.CAMERA))
                                startActivityForResult(cameraIntent, Results.CAMERA_REQUEST);
                            }
                        };

                        if(isAnswered == 1) {
                            SetFormAnswer(formAnswer, nQuestionid, imgCapture, formtypeid);
                        }

                        arrQuestionid.add(nQuestionid);
                        btnAddphoto.setOnClickListener(btnAddphoto_clickListener);
                        tblQuestion.addView(layoutrow);
                        break;

                    case "3": // 1NUMERIC
                        layoutrow = (LinearLayout) LayoutInflater.from(QuestionsActivity.this).inflate(R.layout.question_3_numeric, null);
                        TextView tvwNumeric = (TextView) layoutrow.findViewById(R.id.tvwNumeric);
                        EditText txtNumeric = (EditText) layoutrow.findViewById(R.id.txtNumeric);
                        //txtNumeric.setText("0");

                        txtNumeric.addTextChangedListener(new EditTextWatcher(txtNumeric));
                        txtNumeric.setTag(formtypeid + "," + nQuestionid + "," + formid + "," + strPrompt.trim().toUpperCase());

                        PutAnswers(nQuestionid, txtNumeric, "0", formtypeid);

                        if(isAnswered == 1) {
                            SetFormAnswer(formAnswer, nQuestionid, txtNumeric, formtypeid);
                            if(tcrLib.TrimAllWhiteSpaces(strPrompt).equals(TCRLib.SOS_PERFECT_STORE))
                                hmPerfectStoreValue.put(Integer.valueOf(formid), txtNumeric);
                        }
                        else {
                            if (strDefaultAnswer.equals("")) {
                                if (tcrLib.TrimAllWhiteSpaces(strPrompt).equals(TCRLib.SOS_PERFECT_STORE))
                                    hmPerfectStoreValue.put(Integer.valueOf(formid), txtNumeric);
                            }
                            else {
                                txtNumeric.setText(strDefaultAnswer);
                                SetFormAnswer(formAnswer, nQuestionid, txtNumeric, formtypeid);
                            }
                        }

                        if(GetSOSListLookup()) {
                            if(tcrLib.TrimAllWhiteSpaces(strPrompt).equals(TCRLib.SOS_PERFECT_STORE)) {

                                String sostargetperc = GetSOSTarget();

                                if(sostargetperc.isEmpty()) {
                                    sostargetperc = "0 %";
                                }

                                txtNumeric.setTag(formtypeid + "," + nQuestionid + "," + formid + "," + strPrompt.trim().toUpperCase() + "," + sostargetperc.replace("%", "").trim());
                            }
                        }

                        arrQuestionid.add(nQuestionid);
                        tvwNumeric.setText(strPrompt.trim());

                        hmNumericComputational.put(formid, txtNumeric); // to reference computation with bang(!)

                        if(nRequired == 1) arrRequired.add(txtNumeric);
                        tblQuestion.addView(layoutrow);

                        break;

                    case "4": // 1SINGLE LINE TEXT
                        layoutrow = (LinearLayout) LayoutInflater.from(QuestionsActivity.this).inflate(R.layout.question_4_singlelinetext, null);
                        TextView tvwSingleLine = (TextView) layoutrow.findViewById(R.id.tvwSingleLine);
                        EditText txtSingleLine = (EditText) layoutrow.findViewById(R.id.txtSingleline);

                        txtSingleLine.addTextChangedListener(new EditTextWatcher(txtSingleLine));

                        tvwSingleLine.setText(strPrompt.trim());
                        txtSingleLine.setTag(formtypeid + "," + nQuestionid);

                        if(isAnswered == 1) {
                            SetFormAnswer(formAnswer, nQuestionid, txtSingleLine, formtypeid);
                        }
                        else {
                            if (!strDefaultAnswer.equals("")) {
                                txtSingleLine.setText(strDefaultAnswer);
                                SetFormAnswer(formAnswer, nQuestionid, txtSingleLine, formtypeid);
                            }
                        }

                        if(nRequired == 1) arrRequired.add(txtSingleLine);
                        arrQuestionid.add(nQuestionid);
                        tblQuestion.addView(layoutrow);
                        break;

                    case "5": // 1MULTI LINE TEXT
                        layoutrow = (LinearLayout) LayoutInflater.from(QuestionsActivity.this).inflate(R.layout.question_5_multilinetext, null);
                        TextView tvwMultiLine = (TextView) layoutrow.findViewById(R.id.tvwMultiLine);
                        EditText txtMultiLine = (EditText) layoutrow.findViewById(R.id.txtMultiline);
                        txtMultiLine.addTextChangedListener(new EditTextWatcher(txtMultiLine));

                        txtMultiLine.setTag(formtypeid + "," + nQuestionid);
                        tvwMultiLine.setText(strPrompt.trim());



                        if(isAnswered == 1)
                            SetFormAnswer(formAnswer, nQuestionid, txtMultiLine, formtypeid);
                        else {
                            if(!strDefaultAnswer.equals("")) {
                                txtMultiLine.setText(strDefaultAnswer);
                                SetFormAnswer(formAnswer, nQuestionid, txtMultiLine, formtypeid);
                            }
                        }

                        if(nRequired == 1) arrRequired.add(txtMultiLine);

                        arrQuestionid.add(nQuestionid);
                        tblQuestion.addView(layoutrow);
                        break;

                    case "6": // 1SIGNATURE CAPTURE
                        layoutrow = (LinearLayout) LayoutInflater.from(QuestionsActivity.this).inflate(R.layout.question_6_signature, null);
                        TextView tvwSign = (TextView) layoutrow.findViewById(R.id.tvwSign);
                        ImageView imgSign = (ImageView) layoutrow.findViewById(R.id.imgSignature);
                        tvwSign.setText(strPrompt.trim().toUpperCase());
                        tvwSign.setTag(formtypeid + "," + nQuestionid);

                        Button btnAddsign = (Button) layoutrow.findViewById(R.id.btnAddSignature);

                        btnAddsign.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intentSignature = new Intent(QuestionsActivity.this, Pplus_Questions_signaturepad.class);
                                intentSignature.putExtra("QUESTION_ID", nQuestionid);
                                startActivity(intentSignature);
                            }
                        });

                        General.hmSignature.put(nQuestionid, imgSign);

                        String signatureFilename = General.usercode + "_SIGN_" + String.valueOf(nQuestionid)  + ".png";
                        File fileTempSign = new File(AppSettings.GetUriQuestionImagePath(signatureFilename).getPath());
                        if(fileTempSign.exists())
                            fileTempSign.delete();

                        if(isAnswered == 1) {
                            SetFormAnswer(formAnswer, nQuestionid, imgSign, formtypeid);
                        }

                        if(nRequired == 1) arrRequired.add(tvwSign);

                        arrQuestionid.add(nQuestionid);
                        tblQuestion.addView(layoutrow);
                        break;

                    case "7": // 1DATE
                        layoutrow = (LinearLayout) LayoutInflater.from(QuestionsActivity.this).inflate(R.layout.question_7_date, null);
                        TextView tvwDate = (TextView) layoutrow.findViewById(R.id.tvwDate);
                        final EditText txtDate = (EditText) layoutrow.findViewById(R.id.txtDate);
                        Button btnSetDate = (Button) layoutrow.findViewById(R.id.btnSetDate);

                        tvwDate.setText(strPrompt.trim());
                        txtDate.setTag(formtypeid + "," + nQuestionid);
                        txtDate.setFocusable(false);
                        String datetoday = General.getDateToday();
                        txtDate.setText(datetoday);

                        PutAnswers(nQuestionid, txtDate, datetoday, formtypeid);

                        final String formans = formAnswer;

                        btnSetDate.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Calendar calendarDate = Calendar.getInstance();
                                int mYear = calendarDate.get(Calendar.YEAR);
                                int mMonth = calendarDate.get(Calendar.MONTH);
                                int mDay = calendarDate.get(Calendar.DAY_OF_MONTH);

                                if(isAnswered == 1) {
                                    String[] strAns = formans.split("/");
                                    mYear = Integer.parseInt(strAns[2]);
                                    mMonth = Integer.parseInt(strAns[0]) - 1;
                                    mDay = Integer.parseInt(strAns[1]);
                                }

                                DatePickerDialog dialog = new DatePickerDialog(QuestionsActivity.this, new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                        String strdate = String.valueOf(monthOfYear+1) + "/" + String.valueOf(dayOfMonth) + "/" + String.valueOf(year);
                                        txtDate.setText(strdate);
                                        PutAnswers(nQuestionid, txtDate, strdate, formtypeid);
                                    }
                                }, mYear, mMonth, mDay);
                                dialog.show();
                            }
                        });

                        if(isAnswered == 1) {
                            SetFormAnswer(formAnswer, nQuestionid, txtDate, formtypeid);
                        }
                        else {
                            if(!strDefaultAnswer.equals("")) {
                                txtDate.setText(strDefaultAnswer);
                                PutAnswers(nQuestionid, txtDate, strDefaultAnswer, formtypeid);
                            }
                        }
                        if(nRequired == 1) arrRequired.add(txtDate);

                        arrQuestionid.add(nQuestionid);
                        tblQuestion.addView(layoutrow);
                        break;

                    case "8": // 1TIME
                        layoutrow = (LinearLayout) LayoutInflater.from(QuestionsActivity.this).inflate(R.layout.question_8_time, null);
                        TextView tvwTime = (TextView) layoutrow.findViewById(R.id.tvwTime);
                        final EditText txtTime = (EditText) layoutrow.findViewById(R.id.txtTime);
                        Button btnSetTime = (Button) layoutrow.findViewById(R.id.btnSetTime);

                        tvwTime.setText(strPrompt.trim());
                        txtTime.setTag(formtypeid + "," + nQuestionid);
                        String timetoday = General.getTimeToday();
                        txtTime.setText(timetoday);
                        txtTime.setFocusable(false);

                        PutAnswers(nQuestionid, txtTime, timetoday, formtypeid);

                        final String formtimeans = formAnswer;

                        btnSetTime.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Calendar calendarTime = Calendar.getInstance();
                                int mHours = calendarTime.get(Calendar.HOUR);
                                int mMinutes = calendarTime.get(Calendar.MINUTE);

                                if(isAnswered == 1) {
                                    String[] strAns = formtimeans.split(":");
                                    mHours = Integer.parseInt(strAns[0]);
                                    mMinutes = Integer.parseInt(strAns[1]);
                                }

                                TimePickerDialog timeDialog = new TimePickerDialog(QuestionsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        String strTime = String.valueOf(hourOfDay) + ":" + String.valueOf(minute);
                                        txtTime.setText(strTime);
                                        PutAnswers(nQuestionid, txtTime, strTime, formtypeid);
                                    }
                                },mHours,mMinutes,true);
                                timeDialog.show();
                            }
                        });

                        if(isAnswered == 1) {
                            SetFormAnswer(formAnswer, nQuestionid, txtTime, formtypeid);
                        }
                        else {
                            if(!strDefaultAnswer.equals("")) {
                                txtTime.setText(strDefaultAnswer);
                                PutAnswers(nQuestionid, txtTime, strDefaultAnswer, formtypeid);
                            }
                        }

                        if(nRequired == 1) arrRequired.add(txtTime);

                        arrQuestionid.add(nQuestionid);
                        tblQuestion.addView(layoutrow);
                        break;

                    case "9": // 1MULTI ITEM SELECT
                        layoutrow = (LinearLayout) LayoutInflater.from(QuestionsActivity.this).inflate(R.layout.question_9_multiitem, null);
                        TextView tvwMultiItem = (TextView) layoutrow.findViewById(R.id.tvwMultiitem);
                        tvwMultiItem.setText(strPrompt.trim());
                        tvwMultiItem.setTag(formtypeid + "," + nQuestionid);

/*                        ImageView imgBrand = (ImageView) layoutrow.findViewById(R.id.imgBrandPicture);
                        if(!strBrandImage.isEmpty()) {
                            File imgFileMultiSelect = new File(AppSettings.imgFolder, strBrandImage + ".png");
                            if(!imgFileMultiSelect.exists()) {
                                imgFileMultiSelect = new File(AppSettings.imgFolder, strBrandImage + ".jpg");
                            }
                            bmBrand = tcrLib.decodeFile(imgFileMultiSelect);
                            imgBrand.setImageBitmap(bmBrand);
                            int picId = getResources().getIdentifier(strBrandImage, "drawable", getPackageName());
                            bmBrand = BitmapFactory.decodeResource(getResources(), picId);
                            imgBrand.setImageBitmap(bmBrand);*//*
                            try {
                                Drawable multiItemDrawable = tcrLib.getAssetImage(strBrandImage);
                                imgBrand.setImageDrawable(multiItemDrawable);
                            }
                            catch (IOException ioex) {
                                Log.d("IOException", ioex.getMessage());
                            }
                        }
                        else imgBrand.setVisibility(View.GONE);*/

                        LinearLayout lnrMultiItems = (LinearLayout) layoutrow.findViewById(R.id.lnrMultiitems);
                        lnrMultiItems.removeAllViews();

                        LinearLayout lnrCheckboxes = new LinearLayout(this);
                        lnrCheckboxes.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        lnrCheckboxes.setOrientation(LinearLayout.VERTICAL);

                        LinearLayout.LayoutParams cbParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                        Cursor cursOptions = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_MULTISELECT, SQLiteDB.COLUMN_MULTISELECT_formid + " = " + formid, SQLiteDB.COLUMN_MULTISELECT_optionid);
                        cursOptions.moveToFirst();

                        while (!cursOptions.isAfterLast()) {

                            int nOptionid = cursOptions.getInt(cursOptions.getColumnIndex(SQLiteDB.COLUMN_MULTISELECT_optionid));
                            String strOption = cursOptions.getString(cursOptions.getColumnIndex(SQLiteDB.COLUMN_MULTISELECT_option));

                            CheckBox cb = new CheckBox(this);
                            cb.setId(nOptionid);
                            cb.setText(strOption.trim().toUpperCase());
                            cb.setPadding(5, 5, 5, 5);
                            cb.setTextSize(17f);
                            cb.setTag(formtypeid + "," + nQuestionid);
                            cb.setOnClickListener(multiCheckEvent);
                            cb.setLayoutParams(cbParams);

                            if(isAnswered == 1) {
                                String[] cbperAnswer = formAnswer.split(",");
                                for (String ans : cbperAnswer) {
                                    String[] formIdAndAnswer = ans.split("_");
                                    int cbID = Integer.parseInt(formIdAndAnswer[0]);

                                    if(cbID == nOptionid)
                                        SetFormAnswer(ans, nQuestionid, cb, formtypeid);
                                }
                            }
                            else {
                                if(!strDefaultAnswer.equals("")) {
                                    String[] aDefaultAns = strDefaultAnswer.split(",");
                                    if(Arrays.asList(aDefaultAns).contains(strOption.trim())) {
                                        String ans = nOptionid + "_" + cb.getText().toString();
                                        SetFormAnswer(ans, nQuestionid, cb, formtypeid);
                                    }
                                }
                            }

                            lnrCheckboxes.addView(cb);
                            cursOptions.moveToNext();
                        }

                        arrQuestionid.add(nQuestionid);

                        lnrMultiItems.addView(lnrCheckboxes);
                        if(nRequired == 1) arrRequired.add(tvwMultiItem);

                        tblQuestion.addView(layoutrow);
                        break;

                    case "10": // 1SINGLE ITEM SELECT
                        layoutrow = (LinearLayout) LayoutInflater.from(QuestionsActivity.this).inflate(R.layout.question_10_singleitem, null);
                        TextView tvwSingleItem = (TextView) layoutrow.findViewById(R.id.tvwSingleItem);
                        tvwSingleItem.setText(strPrompt.trim().replace("\"", ""));
                        tvwSingleItem.setTag(formtypeid + "," + nQuestionid);

                        /*ImageView imgBrandPic = (ImageView) layoutrow.findViewById(R.id.imgBrandPictureRadio);
                        Bitmap bmSingleselect = null;
                        if(!strBrandImage.isEmpty()) {
                            File imgFileSingleselect = new File(AppSettings.imgFolder, strBrandImage + ".png");
                            if(!imgFileSingleselect.exists()) {
                                imgFileSingleselect = new File(AppSettings.imgFolder, strBrandImage + ".jpg");
                            }
                            bmBrand = tcrLib.decodeFile(imgFileSingleselect);
                            imgBrandPic.setImageBitmap(bmBrand);

                            int picId = getResources().getIdentifier(strBrandImage, "drawable", getPackageName());
                            bmBrand = BitmapFactory.decodeResource(getResources(), picId);

                            try {
                                Drawable singleItemDrawable = tcrLib.getAssetImage(strBrandImage);
                                imgBrandPic.setImageDrawable(singleItemDrawable);
                            }
                            catch (IOException ioex) {
                                messageBox.showMessage("", ioex.getMessage());
                                Log.d("IOException", ioex.getMessage());
                            }
                        }
                        else imgBrandPic.setVisibility(View.GONE);*/

                        LinearLayout lnrSingleitems = (LinearLayout) layoutrow.findViewById(R.id.lnrSingleitems);
                        lnrSingleitems.removeAllViews();

                        LinearLayout.LayoutParams rbLayouts = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                        // RB GROUP
                        RadioGroup rbGroup = new RadioGroup(this);
                        rbGroup.setOrientation(LinearLayout.VERTICAL);
                        rbGroup.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                        Cursor cursSingleitem = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_SINGLESELECT, SQLiteDB.COLUMN_SINGLESELECT_formid + " = " + formid, SQLiteDB.COLUMN_SINGLESELECT_optionid);
                        cursSingleitem.moveToFirst();

                        while(!cursSingleitem.isAfterLast()) {

                            String nOptionid = cursSingleitem.getString(cursSingleitem.getColumnIndex(SQLiteDB.COLUMN_SINGLESELECT_optionid));
                            String strOption = cursSingleitem.getString(cursSingleitem.getColumnIndex(SQLiteDB.COLUMN_SINGLESELECT_option));

                            RadioButton rb = new RadioButton(this);
                            rb.setId(Integer.parseInt(nOptionid));
                            rb.setTag(formtypeid + "," + nQuestionid);
                            rb.setText(strOption.trim().toUpperCase());
                            rb.setPadding(5, 5, 5, 5);
                            rb.setTextSize(17f);
                            rb.setLayoutParams(rbLayouts);
                            rb.setOnClickListener(singleCheckEvent);

                            if((isAnswered == 1) && (nOptionid.equals(formAnswer))) {
                                SetFormAnswer(formAnswer, nQuestionid, rb, formtypeid);
                            }
                            else {
                                if((!strDefaultAnswer.equals("")) && (strDefaultAnswer.equals(nOptionid)) && (isAnswered == 0)) {
                                    SetFormAnswer(strDefaultAnswer, nQuestionid, rb, formtypeid);
                                }
                            }

                            rbGroup.addView(rb);
                            cursSingleitem.moveToNext();
                        }

                        arrQuestionid.add(nQuestionid);
                        lnrSingleitems.addView(rbGroup);

                        if(nRequired == 1) arrRequired.add(tvwSingleItem);

                        tblQuestion.addView(layoutrow);
                        break;

                    case "11": // 1COMPUTATIONAL
                        layoutrow = (LinearLayout) LayoutInflater.from(QuestionsActivity.this).inflate(R.layout.question_11_computational, null);

                        TextView tvwTotComp = (TextView) layoutrow.findViewById(R.id.tvwComputationTotal);
                        final EditText txtTotComp = (EditText) layoutrow.findViewById(R.id.txtTotalComputational);

                        tvwTotComp.setText(strPrompt.trim().replace("\"", ""));
                        tvwTotComp.setTag(formtypeid + "," + nQuestionid);

                        txtTotComp.setId(Integer.parseInt(formid));
                        txtTotComp.setTag(formtypeid + "," + nQuestionid + "," + strPrompt);
                        txtTotComp.setFocusable(false);

                        if(nRequired == 1) arrRequired.add(tvwTotComp);

                        Cursor cursFormula = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_COMPUTATIONAL, SQLiteDB.COLUMN_COMPUTATIONAL_formid + " = " + formid);
                        cursFormula.moveToFirst();

                        final String strOriginalFormula = cursFormula.getString(cursFormula.getColumnIndex(SQLiteDB.COLUMN_COMPUTATIONAL_formula)).trim().replace("\"","").replace("!","");
                        hmFormulalistsByFormid.put(Integer.parseInt(formid), strOriginalFormula);

                        if(cursFormula.getCount() > 0) {

                            TableLayout tblComputational = (TableLayout) layoutrow.findViewById(R.id.tblComputational);
                            tblComputational.removeAllViews();

                            String aFormulaFormids = cursFormula.getString(cursFormula.getColumnIndex(SQLiteDB.COLUMN_COMPUTATIONAL_formula)).trim().replace("\"","").replace(")", "").replace("(", "").replace(" ", "");
                            String[] aFormids = aFormulaFormids.split("[-+*/]");

                            LinearLayout lnrSOSPerc = (LinearLayout) layoutrow.findViewById(R.id.lnrSOS);
                            lnrSOSPerc.setVisibility(View.GONE);

                            if(GetSOSListLookup()) {
                                if(strPrompt.toUpperCase().equals(TCRLib.SOS_TOTAL_PERC)) {
                                    lnrSOSPerc.setVisibility(View.VISIBLE);
                                    String sostargetperc = GetSOSTarget();
                                    if(sostargetperc.isEmpty()) {
                                        sostargetperc = "0 %";
                                    }
                                    txtTotComp.setTag(formtypeid + "," + nQuestionid + "," + strPrompt + "," + sostargetperc.replace("%", "").trim());
                                    TextView tvwTarget = (TextView) layoutrow.findViewById(R.id.tvwTargetSOS);
                                    tvwTarget.setText(sostargetperc);
                                }
                            }

                            hmCompForms.put(Integer.parseInt(formid), txtTotComp);

                            for (int i = 0; i < aFormids.length; i++) {
                                String compFormId = aFormids[i].replace("!", "");
                                boolean formIdHasBang = aFormids[i].substring(0,1).equals("!");

                                Cursor cursFormulaType = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_FORMS, SQLiteDB.COLUMN_FORMS_formid + " = '" + compFormId + "'");
                                cursFormulaType.moveToFirst();

                                LinearLayout lnrComp = (LinearLayout) LayoutInflater.from(QuestionsActivity.this).inflate(R.layout.question_11_computational_formula, null);

                                TextView tvwFormulaPrompt = (TextView) lnrComp.findViewById(R.id.tvwFormula);
                                EditText txtAnswFormula = null;

                                // if referenced computational form id with bang (!)
                                if(formIdHasBang && hmNumericComputational.containsKey(compFormId)) {
                                    txtAnswFormula = hmNumericComputational.get(compFormId);
                                }
                                else
                                    txtAnswFormula = (EditText) lnrComp.findViewById(R.id.txtFormula);

                                String strPromptFormula = cursFormulaType.getString(cursFormulaType.getColumnIndex(SQLiteDB.COLUMN_FORMS_prompt));
                                int compRequired = cursFormulaType.getInt(cursFormulaType.getColumnIndex(SQLiteDB.COLUMN_FORMS_required));
                                TextWatcher twComputational = new TextWatcherComputational(txtAnswFormula, txtTotComp, formtypeid);
                                tvwFormulaPrompt.setText(strPromptFormula.trim());

                                txtAnswFormula.setTag(nQuestionid + ",0," + strPromptFormula);
                                txtAnswFormula.setId(Integer.parseInt(compFormId));

                                if (isAnswered == 1)
                                    SetAnswerComputational(formAnswer, txtAnswFormula);
                                else
                                    hmCompValues.put(Integer.parseInt(compFormId), 0.0);

                                // TEXT CHANGE EVENT
                                txtAnswFormula.addTextChangedListener(twComputational);
                                hmCompForms.put(Integer.parseInt(compFormId), txtAnswFormula);
                                if (compRequired == 1) arrCompRequired.add(txtAnswFormula);

                                if(!formIdHasBang) {
                                    tblComputational.addView(lnrComp);
                                }

                                cursFormulaType.close();
                            }

                            if(isAnswered == 1)
                                SetFormAnswer(formAnswer, nQuestionid, txtTotComp, formtypeid);
                            else
                                hmCompValues.put(Integer.parseInt(formid), 0.0);
                        }

                        CalculateFormula();

                        arrQuestionid.add(nQuestionid);
                        tblQuestion.addView(layoutrow);
                        break;

                    case "12": // 1CONDITIONALS
                        layoutrow = (LinearLayout) LayoutInflater.from(QuestionsActivity.this).inflate(R.layout.question_12_conditional, null);
                        //conditionalAnswers = "";

                        arrQuestionid.add(nQuestionid);
                        tblQuestion.addView(layoutrow);

                        TextView tvwCondprompt = (TextView) layoutrow.findViewById(R.id.tvwCondprompt);
                        tvwCondprompt.setTag(formtypeid + "," + nQuestionid);
                        tvwCondprompt.setText(strPrompt.trim().toUpperCase());

                        if(nRequired == 1) arrRequired.add(tvwCondprompt);

                        final Cursor cursConditional = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_CONDITIONAL, SQLiteDB.COLUMN_CONDITIONAL_formid + " = " + formid);
                        cursConditional.moveToFirst();

                        if(cursConditional.moveToFirst()) {

                            final RadioGroup rbgConditions = (RadioGroup) layoutrow.findViewById(R.id.rbgConditions);
                            rbgConditions.removeAllViews();

                            LinearLayout.LayoutParams rbCondLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                            tblFormsPerCondition = (TableLayout) layoutrow.findViewById(R.id.tblFormsPerCondition);
                            tblFormsPerCondition.removeAllViews();
                            ctrConditions++;
                            hmCondTableLayout.put(ctrConditions, tblFormsPerCondition);

                            while (!cursConditional.isAfterLast()) {

                                String strConditionPrompt = cursConditional.getString(cursConditional.getColumnIndex(SQLiteDB.COLUMN_CONDITIONAL_condition));
                                String strformConditionsId = cursConditional.getString(cursConditional.getColumnIndex(SQLiteDB.COLUMN_CONDITIONAL_conditionformsid));
                                strformConditionsId = strformConditionsId.trim().equals("") ? null : strformConditionsId.trim();
                                final int nCondOptionID = cursConditional.getInt(cursConditional.getColumnIndex(SQLiteDB.COLUMN_CONDITIONAL_optionid));

                                final RadioButton rb = new RadioButton(this);
                                rb.setId(nCondOptionID);
                                rb.setText(strConditionPrompt);
                                rb.setPadding(5, 5, 5, 5);
                                rb.setTextSize(17f);
                                rb.setTag(ctrConditions);
                                rb.setLayoutParams(rbCondLayout);

                                // Get inner forms
                                ArrayList<String> arrChildFormids = new ArrayList<>();
                                if(strformConditionsId != null)
                                    arrChildFormids.addAll(Arrays.asList(strformConditionsId.split("\\^")));

                                if(arrChildFormids.size() > 0)
                                    hmformCondsid.put(nCondOptionID, arrChildFormids);

                                rb.setOnClickListener(new View.OnClickListener() {
                                    Cursor cursformsperCond;
                                    @Override
                                    public void onClick(View v) {

                                        int rbID = v.getId();

                                        RadioButton rbSub = (RadioButton) v;
                                        rbSelectedCondition = rbSub;
                                        int tblCount = (Integer) rbSub.getTag();

                                        TableLayout tblCond = hmCondTableLayout.get(tblCount);
                                        tblCond.removeAllViews();

                                        hmChildRequired.remove(nQuestionid);
                                        hmlistAnswers.remove(nQuestionid);
                                        RemoveChildAnswerFromArray(nQuestionid);

                                        final ArrayList<String> lstChildFormids = hmformCondsid.get(rbID);

                                        if(lstChildFormids != null && lstChildFormids.size() > 0) {

                                            String rbAnswer = rbSub.getText().toString();
                                            PutAnswers(nQuestionid, rbSub, rbAnswer, formtypeid);

                                            for (String formIDperCondition : lstChildFormids) {

                                                cursformsperCond = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_FORMS, SQLiteDB.COLUMN_FORMS_formid + " = '" + formIDperCondition + "'");
                                                cursformsperCond.moveToFirst();

                                                while (!cursformsperCond.isAfterLast()) {

                                                    String strcondFormtypeid = cursformsperCond.getString(cursformsperCond.getColumnIndex(SQLiteDB.COLUMN_FORMS_typeid));
                                                    String strcondPrompt = cursformsperCond.getString(cursformsperCond.getColumnIndex(SQLiteDB.COLUMN_FORMS_prompt));
                                                    String strcondExpected = cursformsperCond.getString(cursformsperCond.getColumnIndex(SQLiteDB.COLUMN_FORMS_expected));
                                                    int nCondRequired = cursformsperCond.getInt(cursformsperCond.getColumnIndex(SQLiteDB.COLUMN_FORMS_required));
                                                    int nCondExempt = cursformsperCond.getInt(cursformsperCond.getColumnIndex(SQLiteDB.COLUMN_FORMS_exempt));

                                                    CreateConditionalSurveyForms(nQuestionid, strcondFormtypeid, formIDperCondition, strcondPrompt, strcondExpected, nCondRequired, nCondExempt, rbSub.getText().toString(), isAnswered, tblCount);

                                                        // LOAD SIGNATURE IMAGE INSIDE CONDITIONAL
                                                    LoadCondSignatures(Integer.parseInt(formIDperCondition), Integer.parseInt(strcondFormtypeid));
                                                    cursformsperCond.moveToNext();
                                                }
                                            }

                                            cursformsperCond.close();
                                        }
                                        else {
                                            String rbPrompt = rb.getText().toString().trim();
                                            PutAnswers(nQuestionid, rbSub, rbPrompt, formtypeid);
                                        }
                                    }
                                });

                                if(isAnswered == 1) {
                                    if(strConditionPrompt.toUpperCase().equals(formAnswer)) {
                                        hmConditionalAnswers.put(ctrConditions, formAnswer);
                                        SetFormAnswer(formAnswer, nQuestionid, rb, formtypeid);
                                    }
                                }
                                else { // GET DEFAULT ANSWER IF AVAILABLE
                                    if(!strDefaultAnswer.equals("") && strDefaultAnswer.equals(String.valueOf(nCondOptionID))) {
                                        Cursor cursGetOptionid = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_CONDITIONAL, SQLiteDB.COLUMN_CONDITIONAL_optionid + " = '" + strDefaultAnswer + "'");
                                        cursGetOptionid.moveToFirst();
                                        if(cursGetOptionid.getCount() > 0) {
                                            String condDefAns = cursGetOptionid.getString(cursGetOptionid.getColumnIndex(SQLiteDB.COLUMN_CONDITIONAL_condition));
                                            hmConditionalAnswers.put(ctrConditions, condDefAns);
                                            SetFormAnswer(condDefAns, nQuestionid, rb, formtypeid);
                                        }
                                        cursGetOptionid.close();
                                    }
                                }

                                rbgConditions.addView(rb);
                                cursConditional.moveToNext();
                            }

//                            cursConditional.moveToFirst();
//                            while (!cursConditional.isAfterLast()) {
//
//                                String strConditionPrompt = cursConditional.getString(cursConditional.getColumnIndex(SQLiteDB.COLUMN_CONDITIONAL_condition));
//                                final int nCondOptionID = cursConditional.getInt(cursConditional.getColumnIndex(SQLiteDB.COLUMN_CONDITIONAL_optionid));
//
//                                RadioButton rb = (RadioButton) hmCondRadioButtons.get(nCondOptionID);
//
//                                if(isAnswered == 1) {
//                                    if(strConditionPrompt.toUpperCase().equals(formAnswer)) {
//                                        hmConditionalAnswers.put(ctrConditions, formAnswer);
//                                        SetFormAnswer(formAnswer, nQuestionid, rb, formtypeid);
//                                    }
//                                }
//                                else { // GET DEFAULT ANSWER IF AVAILABLE
//                                    if(!strDefaultAnswer.equals("") && strDefaultAnswer.equals(String.valueOf(nCondOptionID))) {
//                                        Cursor cursGetOptionid = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_CONDITIONAL, SQLiteDB.COLUMN_CONDITIONAL_optionid + " = '" + strDefaultAnswer + "'");
//                                        cursGetOptionid.moveToFirst();
//                                        if(cursGetOptionid.getCount() > 0) {
//                                            String condDefAns = cursGetOptionid.getString(cursGetOptionid.getColumnIndex(SQLiteDB.COLUMN_CONDITIONAL_condition));
//                                            hmConditionalAnswers.put(ctrConditions, condDefAns);
//                                            SetFormAnswer(condDefAns, nQuestionid, rb, formtypeid);
//                                        }
//                                        cursGetOptionid.close();
//                                    }
//                                }
//                                cursConditional.moveToNext();
//                            }
                            cursConditional.close();
                        }
                        break;

                    case "13": // 1LABEL HEADER
                        layoutrow = (LinearLayout) LayoutInflater.from(QuestionsActivity.this).inflate(R.layout.question_13_header, null);
                        TextView tvwHeader = (TextView) layoutrow.findViewById(R.id.tvwHeader);
                        tvwHeader.setText(strPrompt.trim());
                        tvwHeader.setGravity(Gravity.CENTER);
                        lnrHeader.addView(layoutrow);
                        break;

                    default:
                        break;
                }
                cursorQuestions.moveToNext();
            }

/*        if(bmBrand != null) {
            bmBrand.recycle();
            bmBrand = null;
        }*/

        System.gc();
    }

    private void RemoveChildAnswers(int questionID) {
        Cursor cursChildAnswers = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_CONDITIONAL_ANSWERS, SQLiteDB.COLUMN_CONDANS_questionid + " = '" + String.valueOf(questionID) + "'");
        if(cursChildAnswers.moveToFirst()) {
            while (!cursChildAnswers.isAfterLast()) {

                int condformid = cursChildAnswers.getInt(cursChildAnswers.getColumnIndex(SQLiteDB.COLUMN_CONDANS_conditionalformid));

                hmChildAnswers.remove(condformid);

                cursChildAnswers.moveToNext();
            }
        }
        cursChildAnswers.close();

        sqlLibrary.ExecSQLWrite("DELETE FROM " + SQLiteDB.TABLE_CONDITIONAL_ANSWERS + " WHERE " + SQLiteDB.COLUMN_CONDANS_questionid + " = '" + String.valueOf(questionID) + "'");
    }

    private void RemoveChildAnswerFromArray(int questionID) {
        Cursor cursChildAnswers = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_CONDITIONAL_ANSWERS, SQLiteDB.COLUMN_CONDANS_questionid + " = '" + String.valueOf(questionID) + "'");
        if(cursChildAnswers.moveToFirst()) {
            while (!cursChildAnswers.isAfterLast()) {

                int condformid = cursChildAnswers.getInt(cursChildAnswers.getColumnIndex(SQLiteDB.COLUMN_CONDANS_conditionalformid));

                hmChildAnswers.remove(condformid);

                cursChildAnswers.moveToNext();
            }
        }
        cursChildAnswers.close();
    }

    private void LoadCondSignatures(int conformid, int formtypeid) {

        File signfile = null;

        for (HashMap.Entry<Integer, ImageView> entry : General.hmCondSignature.entrySet()) {

            int qid = entry.getKey();

            String filename = General.usercode + "_COND_SIGN_" + String.valueOf(qid) + ".png";
            Uri uriSignpath = AppSettings.GetUriQuestionImagePath(filename);

            signfile = new File(uriSignpath.getPath());

            if (signfile.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();

                options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                ImageView imgvw = General.hmCondSignature.get(qid);

                Bitmap bitmapMaster = BitmapFactory.decodeFile(signfile.getAbsolutePath(), options);
                imgvw.setImageBitmap(bitmapMaster);

                PutChildAnswer(conformid, qid, filename, formtypeid);
            }
        }
    }

    private boolean CheckRequiredQuestions() {
        boolean res = true;

        for (View requiredView : arrRequired) {
            String strViewTag = String.valueOf(requiredView.getTag());
            int reqQuestionid = Integer.parseInt(strViewTag.split(",")[1]);
            int reqFormtypeid = Integer.parseInt(strViewTag.split(",")[0]);

            HashMap<Integer, Pplus_AnswerClass> hmRequiredNotAnswered = hmlistAnswers.get(reqQuestionid);

            if(hmRequiredNotAnswered == null) {

                if(reqFormtypeid == 2 || reqFormtypeid == 6 || (reqFormtypeid >= 9 && reqFormtypeid <= 12)) {
                    TextView tvwReq = (TextView) requiredView;
                    tvwReq.setError("This question is required!");
                }
                else {
                    EditText txtReq = (EditText) requiredView;
                    txtReq.setError("This field is required!");
                }

                res = false;
            }
            else {
                if(reqFormtypeid == 2 || reqFormtypeid == 6 || (reqFormtypeid >= 9 && reqFormtypeid <= 12)) {
                    TextView tvwReq = (TextView) requiredView;
                    tvwReq.setError(null);
                }
                else {
                    EditText txtReq = (EditText) requiredView;
                    txtReq.setError(null);
                }
            }
        }

        if(hmChildRequired.size() > 0) {
            for (HashMap.Entry<Integer, View> mReq : hmChildRequired.entrySet()) {
                View requiredView = mReq.getValue();

                String strViewTag = String.valueOf(requiredView.getTag());
                int reqFormtypeid = Integer.valueOf(strViewTag.split(",")[0]);
                int formid = Integer.valueOf(strViewTag.split(",")[2]);

                if(hmChildAnswers.get(formid) == null) {

                    if(reqFormtypeid == 2 || reqFormtypeid == 6 || (reqFormtypeid >= 9 && reqFormtypeid <= 12)) {
                        TextView tvwReq = (TextView) requiredView;
                        tvwReq.setError("This question is required!");
                    }
                    else {
                        EditText txtReq = (EditText) requiredView;
                        txtReq.setError("This field is required!");
                    }

                    res = false;
                }
                else {
                    if(reqFormtypeid == 2 || reqFormtypeid == 6 || (reqFormtypeid >= 9 && reqFormtypeid <= 12)) {
                        TextView tvwReq = (TextView) requiredView;
                        tvwReq.setError(null);
                    }
                    else {
                        EditText txtReq = (EditText) requiredView;
                        txtReq.setError(null);
                    }
                }
            }
        }

        if(arrCompRequired.size() > 0) {
            for (EditText requiredView : arrCompRequired) {
                String val = requiredView.getText().toString().trim();
                if(val.equals("")) {
                    requiredView.setError("This field is required.");
                    res = false;
                }
                else {
                    requiredView.setError(null);
                }
            }
        }

        return res;
    }

    // EVENT SINGLE SELECT
    View.OnClickListener singleCheckEvent = new View.OnClickListener() {

        RadioButton rb = null;
        String strtag;
        String ftypeid;
        int questionid;
        int rbuttonID;

        @Override
        public void onClick(View v) {
            rb = (RadioButton) v;
            strtag = (String) rb.getTag();
            rbuttonID = rb.getId();
            questionid = Integer.parseInt(strtag.split(",")[1]);
            //questionid = Integer.parseInt(strtag.split(",")[2]);
            ftypeid = strtag.split(",")[0];

            hmlistAnswers.remove(questionid);
            String rbAnswer = String.valueOf(rbuttonID);
            PutAnswers(questionid, rb, rbAnswer, ftypeid);

            //RemoveAnswers(questionid, rb);
        }
    };

    // EVENT SINGLE ITEM FOR CONDITIONAL
    View.OnClickListener singleCheckEventConditional = new View.OnClickListener() {

        RadioButton rb = null;
        String strtag;
        int questionid;
        int condformid;
        int condformtypeid;
        int rbuttonID;

        @Override
        public void onClick(View v) {
            rb = (RadioButton) v;
            strtag = (String) rb.getTag();
            rbuttonID = rb.getId();

            String[] sTags = strtag.split(",");
            questionid = Integer.parseInt(sTags[0]);
            condformid = Integer.parseInt(sTags[1]);
            condformtypeid = Integer.parseInt(sTags[2]);

            String rbAnswer = String.valueOf(rbuttonID);

            PutChildAnswer(condformid, questionid, rbAnswer, condformtypeid);
        }
    };

    // EVENT COND MULTI ITEM
    View.OnClickListener multiCheckEventConditional = new View.OnClickListener() {

        CheckBox cb = null;
        String strtag;
        int condformid;
        int condformtypeid;
        int questionid;
        int cboxId;

        @Override
        public void onClick(View v) {
            cb = (CheckBox) v;
            strtag = (String) cb.getTag();
            cboxId = cb.getId();

            String[] tags = strtag.split(",");
            questionid = Integer.parseInt(tags[0]);
            condformid = Integer.parseInt(tags[1]);
            condformtypeid = Integer.parseInt(tags[2]);

            if(cb.isChecked())
                hmMultiCheckConditionAns.put(cboxId, cb);
            else
                hmMultiCheckConditionAns.remove(cboxId);


            String cbAnswer = "";
            for (HashMap.Entry<Integer, CheckBox> cbSelEntry : hmMultiCheckConditionAns.entrySet()) {
                CheckBox cbSel = cbSelEntry.getValue();
                if(cbAnswer.isEmpty())
                    cbAnswer += String.valueOf(cbSel.getId());
                else
                    cbAnswer += "," + String.valueOf(cbSel.getId());
            }
            PutChildAnswer(condformid, questionid, cbAnswer, condformtypeid);
        }
    };

    // EVENT MULTI SELECT
    View.OnClickListener multiCheckEvent = new View.OnClickListener() {

        CheckBox cb = null;
        String strtag;
        String ftypeid;
        int questionid;
        int cboxId;

        @Override
        public void onClick(View v) {
            cb = (CheckBox) v;
            strtag = (String) cb.getTag();
            cboxId = cb.getId();
            questionid = Integer.parseInt(strtag.split(",")[1]);
            ftypeid = strtag.split(",")[0];

            if(cb.isChecked()) {
                String cbAnswer = String.valueOf(cboxId) + "_" + String.valueOf(cb.getText());
                PutAnswers(questionid, cb, cbAnswer, ftypeid);
            }
            else {
                RemoveAnswers(questionid, cb);
            }
        }
    };

    private void PutChildAnswer(int condformid, int questionid, String answer, int condformtypeid) {
        hmChildAnswers.remove(condformid);
        hmChildAnswers.put(condformid, String.valueOf(questionid) + "|" + answer + "|" + String.valueOf(condformtypeid));
    }

    //EVENT FOR EDITEXTS
    private class EditTextWatcher implements TextWatcher {

        private View mView;

        EditText edt = null;
        String strtag;
        String ftypeid;
        String answerText;
        int eQuestionid;

        private EditTextWatcher(View v) {
            this.mView = v;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            edt = (EditText) mView;
            strtag = (String) edt.getTag();
            String[] tags = strtag.split(",");
            ftypeid = tags[0];
            eQuestionid = Integer.parseInt(tags[1]);
            answerText = tcrLib.ValidateNumericValue(edt.getText().toString());
            PutAnswers(eQuestionid, edt, answerText, ftypeid);

            // for numeric form type
            if(ftypeid.equals("3")) {
                int nFormid = Integer.valueOf(tags[2]);
                String strPromptTxt = tags[3];
                if (strPromptTxt.trim().toUpperCase().replace(" ", "").equals(TCRLib.SOS_PERFECT_STORE))
                    hmPerfectStoreValue.put(nFormid, edt);
            }
        }
    }

    //EVENT FOR CONDITIONAL EDITTEXT
    private class EditTextWatcherCond implements TextWatcher {

        private View mView;

        EditText edt = null;
        String strtag;
        int ftypeid;
        String answerText;
        int eQuestionid;
        int formid;

        private EditTextWatcherCond(View v) {
            this.mView = v;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            edt = (EditText) mView;
            strtag = (String) edt.getTag();
            String[] tags = strtag.split(",");
            ftypeid = Integer.parseInt(tags[0]);
            eQuestionid = Integer.parseInt(tags[1]);
            formid = Integer.parseInt(tags[2]);
            answerText = edt.getText().toString();
            PutChildAnswer(formid, eQuestionid, answerText, ftypeid);
        }
    }


    //EVENT FOR computational
    private class TextWatcherComputational implements TextWatcher {

        private View mView;
        private EditText edt;
        private String value;

        private TextWatcherComputational(View v, EditText totalComp, String ftypeid) {
            this.mView = v;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence cValue, int start, int before, int count) {
            value = String.valueOf(cValue).isEmpty() ? "0" : String.valueOf(cValue);
        }

        @Override
        public void afterTextChanged(Editable s) {

            edt = (EditText) mView;
            int selformid = edt.getId();
            String strValue = tcrLib.ValidateNumericValue(value.trim());

            if(hmCompForms.containsKey(selformid)) {
                Double dVal = Double.parseDouble(strValue);
                hmCompValues.put(selformid, dVal);
            }

            CalculateFormula();
            CalculateFormula();
        }
    }

    private void CalculateFormula() {
        for (HashMap.Entry<Integer, String> entryFormulas : hmFormulalistsByFormid.entrySet()) {

            int formula_formid = entryFormulas.getKey();
            String strEquation = entryFormulas.getValue().replace("!", "");
            String compAnswer = strEquation;
            String[] aFormids = strEquation.replace(" ","").replace("(","").replace(")","").replace("!", "").split("[-+*/]");

            EditText txtTotals = (EditText) hmCompForms.get(formula_formid);
            String[] tags = String.valueOf(txtTotals.getTag()).split(",");
            String sPrompt = tags[2].trim();
            String ftypeid = tags[0].trim();
            int nqid = Integer.parseInt(tags[1].trim());

            for (String sFormid : aFormids) {
                Double dValue = 0.0;
                dValue = hmCompValues.get(Integer.parseInt(sFormid)) != null ? hmCompValues.get(Integer.parseInt(sFormid)) : 0.0;
                compAnswer = compAnswer.replace(sFormid, sFormid + "_" + String.valueOf(dValue));
                strEquation = strEquation.replace(sFormid, String.valueOf(dValue));
            }

            Expression exp = new Expression(strEquation);
            double dResult = exp.resolve();

            if (sPrompt.toUpperCase().equals(TCRLib.SOS_TOTAL_PERC)) {
                dResult = dResult * 100;
            }

            dResult = (dResult == Double.NaN) ||
                    (dResult == Double.NEGATIVE_INFINITY) ||
                    (dResult == Double.POSITIVE_INFINITY) ? 0.0 : dResult;

            dResult = String.valueOf(dResult).equals("NaN") ? 0.0 : dResult;

            dResult = Double.valueOf(String.format("%.2f", dResult));

            hmCompValues.put(formula_formid, dResult);
            txtTotals.setText(String.valueOf(dResult));
            compAnswer = compAnswer + "=" + String.valueOf(dResult);
            PutAnswers(nqid, txtTotals, compAnswer, ftypeid);
        }
    }

    // PUT ANSWERS TO HASH
    private void PutAnswers(int putQuestionid, View putView, String putAnswerValue, String putFormtypeid) {

        HashMap<Integer, Pplus_AnswerClass> ansClass = new HashMap<Integer, Pplus_AnswerClass>();

        if(hmlistAnswers.containsKey(putQuestionid)) {
            HashMap<Integer, Pplus_AnswerClass> existingAnsClass = hmlistAnswers.get(putQuestionid);
            existingAnsClass.put(putView.getId(), new Pplus_AnswerClass(putQuestionid, putFormtypeid, putView, putAnswerValue));
            hmlistAnswers.put(putQuestionid, existingAnsClass);
        }
        else {
            ansClass.put(putView.getId(), new Pplus_AnswerClass(putQuestionid, putFormtypeid, putView, putAnswerValue));
            hmlistAnswers.put(putQuestionid, ansClass);
        }
    }

    private void PutAnswersWithId(int putQuestionid, int answerID, View putView, String putAnswerValue, String putFormtypeid) {

        HashMap<Integer, Pplus_AnswerClass> ansClass = new HashMap<Integer, Pplus_AnswerClass>();

        if(hmlistAnswers.containsKey(putQuestionid)) {
            HashMap<Integer, Pplus_AnswerClass> existingAnsClass = hmlistAnswers.get(putQuestionid);
            existingAnsClass.put(answerID, new Pplus_AnswerClass(putQuestionid, putFormtypeid, putView, putAnswerValue));
            hmlistAnswers.put(putQuestionid, existingAnsClass);
        }
        else {
            ansClass.put(answerID, new Pplus_AnswerClass(putQuestionid, putFormtypeid, putView, putAnswerValue));
            hmlistAnswers.put(putQuestionid, ansClass);
        }
    }

    private void SaveAnswerConditional(int storeqid, int formid, int formtypeid, String answer) {
        String[] aFields = {
                SQLiteDB.COLUMN_CONDANS_questionid,
                SQLiteDB.COLUMN_CONDANS_conditionalformid,
                SQLiteDB.COLUMN_CONDANS_conditionalformtypeid,
                SQLiteDB.COLUMN_CONDANS_conditionalanswer
        };

        String[] aValues = {
                String.valueOf(storeqid),
                String.valueOf(formid),
                String.valueOf(formtypeid),
                answer.trim()
        };

//        int count = 0;
//        Cursor cursSave = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_CONDITIONAL_ANSWERS, SQLiteDB.COLUMN_CONDANS_conditionalformid + " = '" + formid + "'");
//        cursSave.moveToFirst();
//        count = cursSave.getCount();
//        cursSave.close();
//
//        if(count > 0)
//            sqlLibrary.UpdateRecord(SQLiteDB.TABLE_CONDITIONAL_ANSWERS, SQLiteDB.COLUMN_CONDANS_conditionalformid, String.valueOf(formid), aFields, aValues);
//        else
            sqlLibrary.AddRecord(SQLiteDB.TABLE_CONDITIONAL_ANSWERS, aFields, aValues);
    }

    private void RemoveAnswers(int removeQuestionid, View removeView) {

        if(hmlistAnswers.containsKey(removeQuestionid)) {
            HashMap<Integer, Pplus_AnswerClass> existingAnsClass = hmlistAnswers.get(removeQuestionid);
            existingAnsClass.remove(removeView.getId());

            if(existingAnsClass.size() == 0) {
                hmlistAnswers.remove(removeQuestionid);
            }
        }
    }

    // CREATING SURVEY FOR CONDITIONAL
    private void CreateConditionalSurveyForms(final int condQuestionid, final String strFormtypeid, String strCondFormid, String strPrompt, String strExpAns, int nCondRequired, int nExempt, final String conditionAnswer, int isParentAnswered, int tblID) {

        final int formid = Integer.parseInt(strCondFormid);
        arrConditionalQuestionid.clear();

        //String parentAnswer = conditionalAnswers.split("\\|")[0].toUpperCase();
        String parentAnswer = conditionAnswer;
        String answeredYesno = rbSelectedCondition.getText().toString().toUpperCase();
        TableLayout tbl = (TableLayout) hmCondTableLayout.get(tblID);

        switch (strFormtypeid) {

            case "1": // LABEL
                layoutrow = (LinearLayout) LayoutInflater.from(QuestionsActivity.this).inflate(R.layout.question_1_label, null);
                TextView tvwLabel = (TextView) layoutrow.findViewById(R.id.tvwLabel);
                tvwLabel.setText(strPrompt.trim());
                //tblFormsPerCondition.addView(layoutrow);
                tbl.addView(layoutrow);
                break;

            case "2": // IMAGE CAPTURE
                layoutrow = (LinearLayout) LayoutInflater.from(QuestionsActivity.this).inflate(R.layout.question_2_imagecapture, null);
                Button btnAddphoto = (Button) layoutrow.findViewById(R.id.btnAddphoto);
                final ImageView imgCapture = (ImageView) layoutrow.findViewById(R.id.imgCapture);
                TextView tvwImageCapture = (TextView) layoutrow.findViewById(R.id.tvwImageCapture);
                btnAddphoto.setText(strPrompt.trim());

                tvwImageCapture.setTag(strFormtypeid + "," + condQuestionid + "," + strCondFormid);

                if(nCondRequired == 1) hmChildRequired.put(condQuestionid, tvwImageCapture);

                View.OnClickListener btnAddphoto_clickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        captureImgview = imgCapture;

                        String imageFilename = General.usercode + "_CONDITIONALIMG_" + condQuestionid;
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        imgFilename = imageFilename + ".jpg";

                        hmUriCondImagefile.put(imgFilename, AppSettings.GetUriQuestionImagePath(imageFilename));
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, AppSettings.GetUriQuestionImagePath(imageFilename));

                        CONDITIONAL_MODE = 1;
                        CAMERA_CONDFORM_ID = formid;
                        CAMERA_COND_QUESTION_ID = condQuestionid;
                        CAMERA_COND_CHILDANSWER = imgFilename;
                        CAMERA_COND_FORMTYPE_ID = Integer.parseInt(strFormtypeid);

                        imgQuestionid = condQuestionid;
                        imgFormtypeid = String.valueOf(condQuestionid);

                        startActivityForResult(cameraIntent, Results.CAMERA_REQUEST);
                    }
                };

                arrConditionalQuestionid.add(condQuestionid);
                btnAddphoto.setOnClickListener(btnAddphoto_clickListener);
                //tblFormsPerCondition.addView(layoutrow);
                tbl.addView(layoutrow);
                break;

            case "3": // NUMERIC
                layoutrow = (LinearLayout) LayoutInflater.from(QuestionsActivity.this).inflate(R.layout.question_3_numeric, null);
                TextView tvwNumeric = (TextView) layoutrow.findViewById(R.id.tvwNumeric);
                EditText txtNumeric = (EditText) layoutrow.findViewById(R.id.txtNumeric);

                txtNumeric.addTextChangedListener(new EditTextWatcherCond(txtNumeric));
                txtNumeric.setTag(strFormtypeid + "," + condQuestionid + "," + strCondFormid);

                if(nCondRequired == 1) hmChildRequired.put(condQuestionid, txtNumeric);

                if(isParentAnswered == 1 && parentAnswer.equals(answeredYesno)) {
                    SetFormAnswerInner(txtNumeric, strFormtypeid, formid, condQuestionid);
                }

                arrConditionalQuestionid.add(condQuestionid);
                tvwNumeric.setText(strPrompt.trim());
                //tblFormsPerCondition.addView(layoutrow);
                tbl.addView(layoutrow);
                break;

            case "4": // SINGLE LINE TEXT
                layoutrow = (LinearLayout) LayoutInflater.from(QuestionsActivity.this).inflate(R.layout.question_4_singlelinetext, null);
                TextView tvwSingleLine = (TextView) layoutrow.findViewById(R.id.tvwSingleLine);
                EditText txtSingleLine = (EditText) layoutrow.findViewById(R.id.txtSingleline);

                tvwSingleLine.setText(strPrompt.trim());
                txtSingleLine.setTag(strFormtypeid + "," + condQuestionid + "," + strCondFormid);

                if(nCondRequired == 1) hmChildRequired.put(condQuestionid, txtSingleLine);

                txtSingleLine.addTextChangedListener(new EditTextWatcherCond(txtSingleLine));

                if(isParentAnswered == 1 && parentAnswer.equals(answeredYesno)) {
                    SetFormAnswerInner(txtSingleLine, strFormtypeid, formid, condQuestionid);
                }

                arrConditionalQuestionid.add(condQuestionid);
                //tblFormsPerCondition.addView(layoutrow);
                tbl.addView(layoutrow);
                break;

            case "5": // MULTI LINE TEXT
                layoutrow = (LinearLayout) LayoutInflater.from(QuestionsActivity.this).inflate(R.layout.question_5_multilinetext, null);
                TextView tvwMultiLine = (TextView) layoutrow.findViewById(R.id.tvwMultiLine);
                EditText txtMultiLine = (EditText) layoutrow.findViewById(R.id.txtMultiline);
                txtMultiLine.addTextChangedListener(new EditTextWatcherCond(txtMultiLine));

                txtMultiLine.setTag(strFormtypeid + "," + condQuestionid + "," + strCondFormid);
                tvwMultiLine.setText(strPrompt.trim());

                if(nCondRequired == 1) hmChildRequired.put(condQuestionid, txtMultiLine);

                if(isParentAnswered == 1 && parentAnswer.equals(answeredYesno)) {
                    SetFormAnswerInner(txtMultiLine, strFormtypeid, formid, condQuestionid);
                }

                arrConditionalQuestionid.add(condQuestionid);
                //tblFormsPerCondition.addView(layoutrow);
                tbl.addView(layoutrow);
                break;

            case "6": // SIGNATURE CAPTURE
                layoutrow = (LinearLayout) LayoutInflater.from(QuestionsActivity.this).inflate(R.layout.question_6_signature, null);
                TextView tvwSign = (TextView) layoutrow.findViewById(R.id.tvwSign);
                ImageView imgSign = (ImageView) layoutrow.findViewById(R.id.imgSignature);
                tvwSign.setText(strPrompt.trim().toUpperCase());

                tvwSign.setTag(strFormtypeid + "," + condQuestionid + "," + strCondFormid);

                if(nCondRequired == 1) hmChildRequired.put(condQuestionid, tvwSign);

                Button btnAddsign = (Button) layoutrow.findViewById(R.id.btnAddSignature);

                btnAddsign.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intentSignature = new Intent(QuestionsActivity.this, Pplus_Questions_signaturepad.class);
                        intentSignature.putExtra("QUESTION_ID", condQuestionid);
                        startActivity(intentSignature);
                    }
                });

                General.hmCondSignature.put(condQuestionid, imgSign);

                String signatureFilename = General.usercode + "_SIGN_" + String.valueOf(condQuestionid)  + ".png";
                File fileTempSign = new File(AppSettings.GetUriQuestionImagePath(signatureFilename).getPath());
                if(fileTempSign.exists())
                    fileTempSign.delete();

/*                if(isAnswered == 1) {
                    SetFormAsnwer(formAnswer, nQuestionid, imgSign, formtypeid);
                }*/

                arrConditionalQuestionid.add(condQuestionid);
                //tblFormsPerCondition.addView(layoutrow);
                tbl.addView(layoutrow);
                break;

            case "7": // DATE
                layoutrow = (LinearLayout) LayoutInflater.from(QuestionsActivity.this).inflate(R.layout.question_7_date, null);
                TextView tvwDate = (TextView) layoutrow.findViewById(R.id.tvwDate);
                final EditText txtDate = (EditText) layoutrow.findViewById(R.id.txtDate);
                Button btnSetDate = (Button) layoutrow.findViewById(R.id.btnSetDate);

                tvwDate.setText(strPrompt.trim());
                txtDate.setTag(strFormtypeid + "," + condQuestionid + "," + strCondFormid);
                txtDate.setFocusable(false);
                String strdate = General.getDateToday();
                txtDate.setText(strdate);

                PutChildAnswer(formid, condQuestionid, strdate, Integer.parseInt(strFormtypeid));

                if(nCondRequired == 1) hmChildRequired.put(condQuestionid, txtDate);

                if(isParentAnswered == 1 && parentAnswer.equals(answeredYesno)) {
                    SetFormAnswerInner(txtDate, strFormtypeid, formid, condQuestionid);
                }

                btnSetDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar calendarDate = Calendar.getInstance();
                        int mYear = calendarDate.get(Calendar.YEAR);
                        int mMonth = calendarDate.get(Calendar.MONTH);
                        int mDay = calendarDate.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog dialog =
                                new DatePickerDialog(QuestionsActivity.this, new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                        String strdate = String.valueOf(monthOfYear+1) + "/" + String.valueOf(dayOfMonth) + "/" + String.valueOf(year);
                                        txtDate.setText(strdate);

                                        PutChildAnswer(formid, condQuestionid, strdate, Integer.parseInt(strFormtypeid));
                                    }
                                }, mYear, mMonth, mDay);
                        dialog.show();
                    }
                });

                //if(isAnswered == 1) SetFormAsnwer(formAnswer, nQuestionid, txtDate, formtypeid);

                arrConditionalQuestionid.add(condQuestionid);
                //tblFormsPerCondition.addView(layoutrow);
                tbl.addView(layoutrow);
                break;

            case "8": // TIME
                layoutrow = (LinearLayout) LayoutInflater.from(QuestionsActivity.this).inflate(R.layout.question_8_time, null);
                TextView tvwTime = (TextView) layoutrow.findViewById(R.id.tvwTime);
                final EditText txtTime = (EditText) layoutrow.findViewById(R.id.txtTime);
                Button btnSetTime = (Button) layoutrow.findViewById(R.id.btnSetTime);

                tvwTime.setText(strPrompt.trim());
                txtTime.setTag(strFormtypeid + "," + condQuestionid + "," + strCondFormid);
                String timetoday = General.getTimeToday();
                txtTime.setText(timetoday);
                txtTime.setFocusable(false);

                PutChildAnswer(formid, condQuestionid, timetoday, Integer.parseInt(strFormtypeid));

                if(nCondRequired == 1) hmChildRequired.put(condQuestionid, txtTime);

                if(isParentAnswered == 1 && parentAnswer.equals(answeredYesno)) {
                    SetFormAnswerInner(txtTime, strFormtypeid, formid, condQuestionid);
                }

                btnSetTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Calendar calendarTime = Calendar.getInstance();
                        int mHours = calendarTime.get(Calendar.HOUR);
                        int mMinutes = calendarTime.get(Calendar.MINUTE);

                        TimePickerDialog timeDialog = new TimePickerDialog(QuestionsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                String strTime = String.valueOf(hourOfDay) + ":" + String.valueOf(minute);
                                txtTime.setText(strTime);

                                PutChildAnswer(formid, condQuestionid, strTime, Integer.parseInt(strFormtypeid));

                            }
                        },mHours,mMinutes,true);
                        timeDialog.show();
                    }
                });

                //if(isAnswered == 1) SetFormAsnwer(formAnswer, nQuestionid, txtTime, formtypeid);

                arrConditionalQuestionid.add(condQuestionid);
                //tblFormsPerCondition.addView(layoutrow);
                tbl.addView(layoutrow);
                break;

            case "9": // MULTI ITEM SELECT
                layoutrow = (LinearLayout) LayoutInflater.from(QuestionsActivity.this).inflate(R.layout.question_9_multiitem, null);
                TextView tvwMultiItem = (TextView) layoutrow.findViewById(R.id.tvwMultiitem);
                tvwMultiItem.setText(strPrompt.trim());
                tvwMultiItem.setTag(strFormtypeid + "," + condQuestionid + "," + strCondFormid);

                if(nCondRequired == 1) hmChildRequired.put(condQuestionid, tvwMultiItem);

                LinearLayout lnrMultiitems = (LinearLayout) layoutrow.findViewById(R.id.lnrMultiitems);
                lnrMultiitems.removeAllViews();

                LinearLayout lnrCheckboxes = new LinearLayout(this);
                lnrCheckboxes.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                lnrCheckboxes.setOrientation(LinearLayout.VERTICAL);

                LinearLayout.LayoutParams cbParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                Cursor cursOptions = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_MULTISELECT, SQLiteDB.COLUMN_MULTISELECT_formid + " = " + formid);
                cursOptions.moveToFirst();

                while (!cursOptions.isAfterLast()) {

                    int nOptionid = cursOptions.getInt(cursOptions.getColumnIndex(SQLiteDB.COLUMN_MULTISELECT_optionid));
                    String strOption = cursOptions.getString(cursOptions.getColumnIndex(SQLiteDB.COLUMN_MULTISELECT_option));

                    CheckBox cb = new CheckBox(this);
                    cb.setId(nOptionid);
                    cb.setText(strOption.trim().toUpperCase());
                    cb.setPadding(5, 5, 5, 5);
                    cb.setTextSize(17f);
                    cb.setTag(condQuestionid + "," + strCondFormid + "," + strFormtypeid);
                    cb.setOnClickListener(multiCheckEventConditional);
                    cb.setLayoutParams(cbParams);

                    if(isParentAnswered == 1 && parentAnswer.equals(answeredYesno)) {
                        SetFormAnswerInner(cb, strFormtypeid, formid, condQuestionid);
                    }

                    lnrCheckboxes.addView(cb);
                    cursOptions.moveToNext();
                }

                arrConditionalQuestionid.add(condQuestionid);
                lnrMultiitems.addView(lnrCheckboxes);
                //tblFormsPerCondition.addView(layoutrow);
                tbl.addView(layoutrow);
                break;

            case "10": // SINGLE ITEM SELECT
                layoutrow = (LinearLayout) LayoutInflater.from(QuestionsActivity.this).inflate(R.layout.question_10_singleitem, null);
                TextView tvwSingleItem = (TextView) layoutrow.findViewById(R.id.tvwSingleItem);
                tvwSingleItem.setText(strPrompt.trim().replace("\"", ""));
                tvwSingleItem.setTag(strFormtypeid + "," + condQuestionid + "," + strCondFormid);

                if(nCondRequired == 1) hmChildRequired.put(condQuestionid, tvwSingleItem);

                LinearLayout lnrSingleitems = (LinearLayout) layoutrow.findViewById(R.id.lnrSingleitems);
                lnrSingleitems.removeAllViews();

                LinearLayout.LayoutParams rbLayouts = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                // RB GROUP
                RadioGroup rbGroup = new RadioGroup(this);
                rbGroup.setOrientation(LinearLayout.VERTICAL);
                rbGroup.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                Cursor cursSingleitem = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_SINGLESELECT, SQLiteDB.COLUMN_SINGLESELECT_formid + " = " + formid, SQLiteDB.COLUMN_SINGLESELECT_optionid);
                cursSingleitem.moveToFirst();

                while(!cursSingleitem.isAfterLast()) {

                    String nOptid = cursSingleitem.getString(cursSingleitem.getColumnIndex(SQLiteDB.COLUMN_SINGLESELECT_id));
                    String strOption = cursSingleitem.getString(cursSingleitem.getColumnIndex(SQLiteDB.COLUMN_SINGLESELECT_option));

                    RadioButton rb = new RadioButton(this);
                    rb.setId(Integer.parseInt(nOptid));
                    rb.setTag(condQuestionid + "," + strCondFormid + "," + strFormtypeid);
                    rb.setText(strOption.trim().toUpperCase());
                    rb.setPadding(5, 5, 5, 5);
                    rb.setTextSize(17f);
                    rb.setLayoutParams(rbLayouts);
                    rb.setOnClickListener(singleCheckEventConditional);
                    rbGroup.addView(rb);

                    if(isParentAnswered == 1 && parentAnswer.equals(answeredYesno)) {
                        SetFormAnswerInner(rb, strFormtypeid, formid, condQuestionid);
                    }

                    cursSingleitem.moveToNext();
                }

                arrConditionalQuestionid.add(condQuestionid);
                lnrSingleitems.addView(rbGroup);
                //tblFormsPerCondition.addView(layoutrow);
                tbl.addView(layoutrow);
                break;

            case "11": // COMPUTATIONAL
                layoutrow = (LinearLayout) LayoutInflater.from(QuestionsActivity.this).inflate(R.layout.question_11_computational, null);


                TextView tvwComp = (TextView) layoutrow.findViewById(R.id.tvwComputationTotal);
                final EditText txtTotComp = (EditText) layoutrow.findViewById(R.id.txtTotalComputational);


                tvwComp.setText(strPrompt.trim().replace("\"", ""));
                txtTotComp.setFocusable(false);

                Cursor cursFormula = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_COMPUTATIONAL, SQLiteDB.COLUMN_COMPUTATIONAL_formid + " = " + formid);
                cursFormula.moveToFirst();

                final String stroriginalFormula = cursFormula.getString(cursFormula.getColumnIndex(SQLiteDB.COLUMN_COMPUTATIONAL_formula)).trim().replace("\"","");
                if(cursFormula.getCount() > 0) {

                    TableLayout tblComputational = (TableLayout) layoutrow.findViewById(R.id.tblComputational);
                    tblComputational.removeAllViews();

                    String aFormulaFormids = cursFormula.getString(cursFormula.getColumnIndex(SQLiteDB.COLUMN_COMPUTATIONAL_formula)).trim().replace("\"","").replace(")", "").replace("(", "").replace(" ", "");
                    String[] aFormids = aFormulaFormids.split("\\+");
                    txtTotComp.setTag("0,1");

                    for (int i = 0; i < aFormids.length; i++) {
                        Cursor cursFormulaType = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_FORMS, SQLiteDB.COLUMN_FORMS_formid + " = " + aFormids[i]);
                        cursFormulaType.moveToFirst();

                        String formtypeidFormula = cursFormulaType.getString(cursFormulaType.getColumnIndex(SQLiteDB.COLUMN_FORMS_typeid));
                        String strPromptFormula = cursFormulaType.getString(cursFormulaType.getColumnIndex(SQLiteDB.COLUMN_FORMS_prompt));
                        String strExpectedansFormula = cursFormulaType.getString(cursFormulaType.getColumnIndex(SQLiteDB.COLUMN_FORMS_expected));
                        int nRequiredFormula = cursFormulaType.getInt(cursFormulaType.getColumnIndex(SQLiteDB.COLUMN_FORMS_required));
                        int nExemptFormula = cursFormulaType.getInt(cursFormulaType.getColumnIndex(SQLiteDB.COLUMN_FORMS_exempt));

                        LinearLayout lnrComp = (LinearLayout) LayoutInflater.from(QuestionsActivity.this).inflate(R.layout.question_11_computational_formula, null);
                        TextView tvwFormulaPrompt = (TextView) lnrComp.findViewById(R.id.tvwFormula);
                        tvwFormulaPrompt.setText(strPromptFormula.trim());

                        EditText txtAnswFormula = (EditText) lnrComp.findViewById(R.id.txtFormula);
                        txtAnswFormula.setText("0");
                        txtAnswFormula.setTag(condQuestionid + ",0");
                        txtAnswFormula.setId(Integer.parseInt(aFormids[i]));

                        //if(isAnswered == 1) SetFormAsnwer(formAnswer, nQuestionid, txtAnswFormula, formtypeid);

                        txtAnswFormula.addTextChangedListener(new TextWatcherComputational(txtAnswFormula, txtTotComp, strFormtypeid));

                        cursFormulaType.close();
                        tblComputational.addView(lnrComp);
                    }
                    //if(isSubAnswered == 1) SetFormAnswer(formAnswer, nQuestionid, txtTotComp, formtypeid);
                }

                cursFormula.close();
                arrConditionalQuestionid.add(condQuestionid);
                //tblFormsPerCondition.addView(layoutrow);
                tbl.addView(layoutrow);
                break;

            default:
                break;
        }
    }

    // SAVE SURVEY
    private class SaveSurvey extends AsyncTask<Void, Void, Boolean> {
        private String errmsg = "";
        @Override
        protected void onPreExecute() {
            saveProgress = ProgressDialog.show(QuestionsActivity.this, "", "Saving answers. Please wait.");
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            boolean result = false;

            try {
                HashMap<Integer, Pplus_AnswerClass> hmAns = new HashMap<>();

                int expFormtypeid = 0;

                for (int qid : arrQuestionid) {
                    hmAns = hmlistAnswers.get(qid); // get inner hashmap hmAns: <view id, answer class>
                    ArrayList<String> arrExpectedAns = new ArrayList<>(Arrays.asList(hmExpectedAnswers.get(qid).split("\\~"))); // get expected answer by questionid

                    // FIRST, CHANGE ALL QUESTION TO NOT ANSWERED
                    if (hmAns != null) { // if question id is found

                        //CheckBox cbAnswered;
                        String answer = "";

                        Cursor cursStoreQuestions = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_STOREQUESTION, SQLiteDB.COLUMN_STOREQUESTION_storecategorygroupid + " = " + storeCategoryGroupID + " AND " + SQLiteDB.COLUMN_STOREQUESTION_id + " = " + qid);
                        cursStoreQuestions.moveToFirst();
                        String storequestionid = cursStoreQuestions.getString(cursStoreQuestions.getColumnIndex(SQLiteDB.COLUMN_STOREQUESTION_id));

                        for (HashMap.Entry<Integer, Pplus_AnswerClass> entry : hmAns.entrySet()) {
                            final Pplus_AnswerClass answerValueClass = entry.getValue();

                            switch (answerValueClass.formtypeid) {

                                case "2": // IMAGE CAPTURE
                                    answer = answerValueClass.viewValue;
                                    File imageFile;

                                    if (hmUriImagequestionfile.size() == 0) {
                                        break;
                                    }

                                    Uri imgUriPath = hmUriImagequestionfile.get(answerValueClass.viewValue);
                                    if(imgUriPath != null) {

                                        imageFile = new File(imgUriPath.getPath());
                                        File destFile = new File(AppSettings.captureFolder, answerValueClass.viewValue);

                                        try {
                                            if (imageFile.exists()) {
                                                AppSettings.CopyFile(imageFile, destFile);
                                                imageFile.delete();
                                            }
                                        } catch (IOException ex) {
                                            errmsg = "Error in saving image captured. Please try again";
                                            String exErr = ex.getMessage() != null ? ex.getMessage() : errmsg;
                                            errorLog.appendLog(exErr, TAG);
                                            return false;
                                        }
                                    }
                                    break;

                                case "3": // NUMERIC
                                    answer = tcrLib.ValidateNumericValue(answerValueClass.viewValue.trim());
                                    break;

                                case "6": // SIGNATURE CAPTURE
                                    answer = answerValueClass.viewValue;

                                    File imageSignatureFile = new File(AppSettings.GetUriQuestionImagePath(answerValueClass.viewValue).getPath());
                                    File destSignatureFile = new File(AppSettings.signatureFolder, answerValueClass.viewValue);

                                    try {
                                        if (imageSignatureFile.exists()) {
                                            AppSettings.CopyFile(imageSignatureFile, destSignatureFile);
                                            imageSignatureFile.delete();
                                        }
                                    } catch (final IOException ex) {
                                        errmsg = "Error in saving image signature. Please try again";
                                        String exErr = ex.getMessage() != null ? ex.getMessage() : errmsg;
                                        errorLog.appendLog(exErr, TAG);
                                        return false;
                                    }

                                    break;

                                case "9": // MULTI SELECT
                                    //cbAnswered = (CheckBox) answerValueClass.vElement;
                                    answer += answerValueClass.viewValue + ",";
                                    break;

                                case "11": // COMPUTATIONAL
                                    final EditText txtTotalComp = (EditText) answerValueClass.vElement;

                                    SOSAnswer = "";

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            if (hmPerfectStoreValue.size() > 0) {
                                                for (HashMap.Entry<Integer, EditText> hmapEntry : hmPerfectStoreValue.entrySet()) {
                                                    EditText txtCurrent = hmapEntry.getValue();
                                                    String[] curTags = txtCurrent.getTag().toString().split(",");
                                                    String strVal = tcrLib.ValidateNumericValue(txtCurrent.getText().toString().trim());

                                                    if (curTags.length == 5) {
                                                        if (curTags[3].replace(" ", "").equals(TCRLib.SOS_PERFECT_STORE)) {
                                                            double perc = Double.parseDouble(curTags[4]);
                                                            double totalAns = Double.parseDouble(strVal);
                                                            SOSAnswer = String.valueOf(totalAns) + "," + String.valueOf(perc);
                                                        }
                                                    }
/*                                                    if (strTags.length == 4) {
                                                        if (strTags[2].equals(TCRLib.SOS_TOTAL_PERC)) {
                                                            double perc = Double.parseDouble(strTags[3]);
                                                            double totalAns = Double.parseDouble(answerValueClass.viewValue.trim().split("=")[1]);
                                                            SOSAnswer = String.valueOf(totalAns) + "," + String.valueOf(perc);
                                                        }
                                                    }*/
                                                }
                                            }
                                        }
                                    });
                                    answer = answerValueClass.viewValue;
                                    break;

                                case "12": // CONDITIONAL
                                    answer = answerValueClass.viewValue;
                                    sqlLibrary.ExecSQLWrite("DELETE FROM " + SQLiteDB.TABLE_CONDITIONAL_ANSWERS + " WHERE " + SQLiteDB.COLUMN_CONDANS_questionid + " = '" + String.valueOf(answerValueClass.questionid) + "'");
                                    for (HashMap.Entry<Integer, String> childCondEntry : hmChildAnswers.entrySet()) {
                                        int childformid = childCondEntry.getKey();
                                        String[] childValues = childCondEntry.getValue().split("\\|");
                                        int childquestionid = Integer.parseInt(childValues[0]);
                                        String sChildAnswer = childValues[1];
                                        int childformtypeid = Integer.parseInt(childValues[2]);

                                        SaveAnswerConditional(childquestionid, childformid, childformtypeid, sChildAnswer);
                                    }
                                    expFormtypeid = Integer.valueOf(answerValueClass.formtypeid);
                                    break;

                                default:
                                    answer = answerValueClass.viewValue;
                                    break;
                            }
                        }

                        // UPDATE QUESTION CORRECT ANSWER
                        int questionInitStatus = 0;
                        // 0 = not correct answered
                        // 1 = correct answer
                        if (arrExpectedAns.contains(answer)) {
                            questionInitStatus = 1;
                        }
                        else {
                            if(arrExpectedAns.size() == 1) {
                                if (arrExpectedAns.get(0).trim().equals("") && !answer.trim().equals("")) {
                                    questionInitStatus = 1;
                                }
                            }
                        }

                        // GET EXPECTED ANSWER VALUE AND CHECK CORRECT ANSWER FOR CONDITIONAL
                        switch (expFormtypeid) {

                            case 9: // MULTI ITEM
                                String[] cbAnswers = answer.split(",");
                                if (cbAnswers.length > 0) {
                                    for (String optionidAns : cbAnswers) {
                                        String cbid = optionidAns.split("_")[0];
                                        if (arrExpectedAns.contains(cbid)) {
                                            questionInitStatus = 1;
                                        } else {
                                            questionInitStatus = 0;
                                            break;
                                        }
                                    }
                                }
                                break;

                            case 12: // CONDITIONAL
                                for (String expAns : arrExpectedAns) {
                                    Cursor cursCond = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_CONDITIONAL, SQLiteDB.COLUMN_CONDITIONAL_optionid + " = '" + expAns + "'");
                                    cursCond.moveToFirst();
                                    String wordAns = "";
                                    if (cursCond.getCount() > 0) {
                                        wordAns = cursCond.getString(cursCond.getColumnIndex(SQLiteDB.COLUMN_CONDITIONAL_condition)).toUpperCase().trim();
                                        if (answer.equals(wordAns)) {
                                            questionInitStatus = 1;
                                            break;
                                        }
                                    }
                                }
                                break;
                            default:
                                break;
                        }

                        sqlLibrary.ExecSQLWrite("UPDATE " + SQLiteDB.TABLE_STOREQUESTION
                                + " SET " + SQLiteDB.COLUMN_STOREQUESTION_isAnswered + " = '1'"
                                + "," + SQLiteDB.COLUMN_STOREQUESTION_answer + " = '" + answer + "'"
                                + "," + SQLiteDB.COLUMN_STOREQUESTION_initial + " = '" + questionInitStatus + "'"
                                + "," + SQLiteDB.COLUMN_STOREQUESTION_final + " = '" + questionInitStatus + "'"
                                + " WHERE " + SQLiteDB.COLUMN_STOREQUESTION_id + " = '" + storequestionid + "'");
                    } else {
                        sqlLibrary.ExecSQLWrite("UPDATE " + SQLiteDB.TABLE_STOREQUESTION
                                + " SET " + SQLiteDB.COLUMN_STOREQUESTION_isAnswered + " = '0'"
                                + "," + SQLiteDB.COLUMN_STOREQUESTION_answer + " = ''"
                                + "," + SQLiteDB.COLUMN_STOREQUESTION_initial + " = '0'"
                                + "," + SQLiteDB.COLUMN_STOREQUESTION_final + " = '0'"
                                + " WHERE " + SQLiteDB.COLUMN_STOREQUESTION_storecategorygroupid + " = '" + storeCategoryGroupID + "'"
                                + " AND " + SQLiteDB.COLUMN_STOREQUESTION_id + " = '" + qid + "'");
                    }
                }

                UpdateStoreCategoryGroup();
                UpdateStoreCategory();
                UpdateStoreStatus();

                UpdatePerfectStoreCategory();
                UpdatePerfectStore();
                result = true;
            }
            catch (Exception ex) {
                errmsg = "Error in saving audit. Please try again";
                String exErr = ex.getMessage() != null ? ex.getMessage() : errmsg;
                errorLog.appendLog(exErr, TAG);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean bResult) {
            saveProgress.dismiss();
            if(!bResult) {
                messageBox.ShowMessage("Error in saving", errmsg);
                return;
            }

            AlertDialog dl = new AlertDialog.Builder(QuestionsActivity.this).create();
            dl.setTitle("Saved");
            dl.setMessage("Survey are successfully saved! Tap OK to view your result.");
            dl.setCancelable(false);
            dl.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    onBackPressed();
                }
            });

            errorLog.appendLog("Successfully saved audit. User: " + General.userName, TAG);
            dl.show();
        }
    }

    // UPDATE PERFECT STORE
    private void UpdatePerfectStore() throws Exception {
        double dTotalPerfectScore = 0.00;
        double dScorePerCategory = 0.00;
        int nStorePerfect = 0;
        double nReq = 0;
        String strPerfectStoreScore = "0.00";


        //JASONMOD
        /*Cursor cursReqStore = sqlLibrary.RawQuerySelect("SELECT tblcategory.category_id FROM " + SQLiteDB.TABLE_STORECATEGORY
                + " JOIN " + SQLiteDB.TABLE_CATEGORY
                + " ON " + SQLiteDB.TABLE_STORECATEGORY + "." + SQLiteDB.COLUMN_STORECATEGORY_categoryid + " = " + SQLiteDB.TABLE_CATEGORY + "." + SQLiteDB.COLUMN_CATEGORY_id
                + " JOIN " + SQLiteDB.TABLE_PERFECT_CATEGORY
                + " ON " + SQLiteDB.TABLE_CATEGORY + "." + SQLiteDB.COLUMN_CATEGORY_categoryid + " = " + SQLiteDB.TABLE_PERFECT_CATEGORY + "." + SQLiteDB.COLUMN_PCATEGORY_categoryid
                + " WHERE " + SQLiteDB.COLUMN_STORECATEGORY_storeid + " = '" + General.selectedStore.storeID + "'");*/

        Cursor cursReqStore = sqlLibrary.RawQuerySelect("SELECT tblcategory.category_id FROM " + SQLiteDB.TABLE_STORECATEGORY
                + " JOIN " + SQLiteDB.TABLE_CATEGORY
                + " ON " + SQLiteDB.TABLE_STORECATEGORY + "." + SQLiteDB.COLUMN_STORECATEGORY_categoryid + " = " + SQLiteDB.TABLE_CATEGORY + "." + SQLiteDB.COLUMN_CATEGORY_id
                + " JOIN " + SQLiteDB.TABLE_PERFECT_CATEGORY
                + " ON " + SQLiteDB.TABLE_CATEGORY + "." + SQLiteDB.COLUMN_CATEGORY_categoryid + " = " + SQLiteDB.TABLE_PERFECT_CATEGORY + "." + SQLiteDB.COLUMN_PCATEGORY_categoryid
                + " WHERE " + SQLiteDB.COLUMN_STORECATEGORY_storeid + " = '" + General.selectedStore.storeID + "' AND " + SQLiteDB.COLUMN_PCATEGORY_audittempid + " = '" + General.selectedStore.auditTemplateId+"'");

        cursReqStore.moveToFirst();
        nReq = cursReqStore.getCount();
        dScorePerCategory = 100 / nReq;
        cursReqStore.close();

        if(nReq > 0) {


            //JASONMOD
            Cursor cursPassedCateg = sqlLibrary.RawQuerySelect("SELECT tblcategory.category_id AS categID FROM " + SQLiteDB.TABLE_STORECATEGORY
                    + " JOIN " + SQLiteDB.TABLE_CATEGORY
                    + " ON " + SQLiteDB.TABLE_STORECATEGORY + "." + SQLiteDB.COLUMN_STORECATEGORY_categoryid + " = " + SQLiteDB.TABLE_CATEGORY + "." + SQLiteDB.COLUMN_CATEGORY_id
                    + " JOIN " + SQLiteDB.TABLE_PERFECT_CATEGORY
                    + " ON " + SQLiteDB.TABLE_CATEGORY + "." + SQLiteDB.COLUMN_CATEGORY_categoryid + " = " + SQLiteDB.TABLE_PERFECT_CATEGORY + "." + SQLiteDB.COLUMN_PCATEGORY_categoryid
                    + " WHERE " + SQLiteDB.COLUMN_STORECATEGORY_storeid + " = '" + General.selectedStore.storeID + "'"
                    + " AND " + SQLiteDB.COLUMN_STORECATEGORYGROUP_final + " = '1'  AND " + SQLiteDB.COLUMN_PCATEGORY_audittempid + " = '" + General.selectedStore.auditTemplateId+"'");


            /*Cursor cursPassedCateg = sqlLibrary.RawQuerySelect("SELECT tblcategory.category_id AS categID FROM " + SQLiteDB.TABLE_STORECATEGORY
                    + " JOIN " + SQLiteDB.TABLE_CATEGORY
                    + " ON " + SQLiteDB.TABLE_STORECATEGORY + "." + SQLiteDB.COLUMN_STORECATEGORY_categoryid + " = " + SQLiteDB.TABLE_CATEGORY + "." + SQLiteDB.COLUMN_CATEGORY_id
                    + " JOIN " + SQLiteDB.TABLE_PERFECT_CATEGORY
                    + " ON " + SQLiteDB.TABLE_CATEGORY + "." + SQLiteDB.COLUMN_CATEGORY_categoryid + " = " + SQLiteDB.TABLE_PERFECT_CATEGORY + "." + SQLiteDB.COLUMN_PCATEGORY_categoryid
                    + " WHERE " + SQLiteDB.COLUMN_STORECATEGORY_storeid + " = '" + General.selectedStore.storeID + "'"
                    + " AND " + SQLiteDB.COLUMN_STORECATEGORYGROUP_final + " = '1' ");*/


            cursPassedCateg.moveToFirst();

            if (cursPassedCateg.getCount() > 0) {
                int nPCategoryMatched = 0;
                while (!cursPassedCateg.isAfterLast()) {
                    int categoryId = cursPassedCateg.getInt(cursPassedCateg.getColumnIndex("categID"));
                    if (TCRLib.arrPCategoryList.contains(categoryId)) {
                        nPCategoryMatched++;
                        dTotalPerfectScore += dScorePerCategory;
                    }
                    cursPassedCateg.moveToNext();
                }
                if (nReq == nPCategoryMatched) {
                    dTotalPerfectScore = 100.00;
                    nStorePerfect = 1;
                }
            }

            strPerfectStoreScore = String.format(Locale.getDefault(), "%.2f", dTotalPerfectScore);

            String[] aUpdateFields = new String[]{
                    SQLiteDB.COLUMN_STORE_initial,
                    SQLiteDB.COLUMN_STORE_final,
                    SQLiteDB.COLUMN_STORE_perfectstore
            };
            String[] aUpdateValues = new String[] {
                    String.valueOf(nStorePerfect),
                    String.valueOf(nStorePerfect),
                    strPerfectStoreScore
            };

            sqlLibrary.UpdateRecord(SQLiteDB.TABLE_STORE, SQLiteDB.COLUMN_STORE_id, String.valueOf(General.selectedStore.storeID), aUpdateFields, aUpdateValues);
        }
    }

    // UPDATE PERFECT STORE CATEGORY
    private void UpdatePerfectStoreCategory() throws Exception {

        int nCategoryPerfect = 0;
        int nReq = 0;

        Cursor cursReq = sqlLibrary.RawQuerySelect("SELECT formgroupid FROM " + SQLiteDB.TABLE_STORECATEGORYGROUP
                + " JOIN " + SQLiteDB.TABLE_GROUP
                + " ON " + SQLiteDB.TABLE_STORECATEGORYGROUP + "." + SQLiteDB.COLUMN_STORECATEGORYGROUP_groupid + " = " + SQLiteDB.TABLE_GROUP + "." + SQLiteDB.COLUMN_GROUP_id
                + " JOIN " + SQLiteDB.TABLE_PERFECT_GROUP
                + " ON " + SQLiteDB.TABLE_GROUP + "." + SQLiteDB.COLUMN_GROUP_groupid + " = " + SQLiteDB.TABLE_PERFECT_GROUP + "." + SQLiteDB.COLUMN_PGROUP_groupid
                + " WHERE " + SQLiteDB.COLUMN_STORECATEGORYGROUP_storecategid + " = '" + storeCategoryID + "'");
        cursReq.moveToFirst();
        nReq = cursReq.getCount();
        cursReq.close();

        if(nReq > 0) {

            Cursor cursPassedGroup = sqlLibrary.RawQuerySelect("SELECT formgroupid FROM " + SQLiteDB.TABLE_STORECATEGORYGROUP
                    + " JOIN " + SQLiteDB.TABLE_GROUP
                    + " ON " + SQLiteDB.TABLE_STORECATEGORYGROUP + "." + SQLiteDB.COLUMN_STORECATEGORYGROUP_groupid + " = " + SQLiteDB.TABLE_GROUP + "." + SQLiteDB.COLUMN_GROUP_id
                    + " WHERE " + SQLiteDB.COLUMN_STORECATEGORYGROUP_storecategid + " = '" + storeCategoryID + "'"
                    + " AND " + SQLiteDB.COLUMN_STORECATEGORYGROUP_final + " = '1'");
            cursPassedGroup.moveToFirst();

            if (cursPassedGroup.getCount() > 0) {
                int nPGroupMatched = 0;
                while (!cursPassedGroup.isAfterLast()) {
                    int groupId = cursPassedGroup.getInt(cursPassedGroup.getColumnIndex("formgroupid"));
                    if (TCRLib.arrPGroupList.contains(groupId)) {
                        nPGroupMatched++;
                    }
                    cursPassedGroup.moveToNext();
                }

                if (nReq == nPGroupMatched)
                    nCategoryPerfect = 1;
            }

            String[] aUpdateFields = new String[] {
                    SQLiteDB.COLUMN_STORECATEGORY_initial,
                    SQLiteDB.COLUMN_STORECATEGORY_final
            };

            String[] aUpdateValues = new String[] {
                    String.valueOf(nCategoryPerfect),
                    String.valueOf(nCategoryPerfect)
            };

            sqlLibrary.UpdateRecord(SQLiteDB.TABLE_STORECATEGORY, SQLiteDB.COLUMN_STORECATEGORY_id, storeCategoryID, aUpdateFields, aUpdateValues);

            cursPassedGroup.close();
        }
    }

    //UPDATE STORE STATUS
    private void UpdateStoreStatus() throws Exception {

        Cursor cursCountCorrect = sqlLibrary.RawQuerySelect("SELECT COUNT(*) AS countCorrect FROM " + SQLiteDB.TABLE_STORECATEGORY
                + " WHERE " + SQLiteDB.COLUMN_STORECATEGORY_storeid + " = '" + General.selectedStore.storeID + "'"
                + " AND " + SQLiteDB.COLUMN_STORECATEGORY_final + " = '1' ");
        cursCountCorrect.moveToFirst();

        Cursor cursCountComplete = sqlLibrary.RawQuerySelect("SELECT COUNT(*) AS countComplete FROM " + SQLiteDB.TABLE_STORECATEGORY
                + " WHERE " + SQLiteDB.COLUMN_STORECATEGORY_storeid + " = '" + General.selectedStore.storeID + "'"
                + " AND " + SQLiteDB.COLUMN_STORECATEGORY_status + " = '2'");
        cursCountComplete.moveToFirst();

        Cursor cursCountCategory = sqlLibrary.RawQuerySelect("SELECT COUNT(*) AS countCategory FROM " + SQLiteDB.TABLE_STORECATEGORY
                + " WHERE " + SQLiteDB.COLUMN_STORECATEGORY_storeid + " = '" + General.selectedStore.storeID + "'");
        cursCountCategory.moveToFirst();

        int nTotalCategory = 0;
        int nCorrectCategory = 0;
        int nCompleted = 0;
        nCorrectCategory = cursCountCorrect.getInt(cursCountCorrect.getColumnIndex("countCorrect"));
        nCompleted = cursCountComplete.getInt(cursCountComplete.getColumnIndex("countComplete"));
        nTotalCategory = cursCountCategory.getInt(cursCountCategory.getColumnIndex("countCategory"));

        String storeStatus = "1";
        String storeFinalvalue = "0";

        if(nTotalCategory == nCompleted)
            storeStatus = "2";

        if(nTotalCategory == nCorrectCategory)
            storeFinalvalue = "1";

        int nTotalCorrectOSA = 0;
        int nTotalQuestionsOSA = 0;

        int nTotalCorrectNPI = 0;
        int nTotalQuestionsNPI = 0;

        int nTotalCorrectPlano = 0;
        int nTotalQuestionsPlano = 0;

        String osa = "0.0";
        String npi = "0.0";
        String planogram = "0.0";

        // SAVING OF TOTAL OSA, NPI AND PLANOGRAM PERCENTAGE
        // STORE CATEGORY
        Cursor cursStoreCategory = sqlLibrary.RawQuerySelect("SELECT " + SQLiteDB.TABLE_STORECATEGORY + "." + SQLiteDB.COLUMN_STORECATEGORY_id + " FROM " + SQLiteDB.TABLE_STORECATEGORY
                + " JOIN " + SQLiteDB.TABLE_CATEGORY + " ON " + SQLiteDB.TABLE_CATEGORY + "." + SQLiteDB.COLUMN_CATEGORY_id + " = " + SQLiteDB.TABLE_STORECATEGORY + "." + SQLiteDB.COLUMN_STORECATEGORY_categoryid
                + " WHERE " + SQLiteDB.COLUMN_STORECATEGORY_storeid + " = '" + General.selectedStore.storeID + "'"
                + " ORDER BY " + SQLiteDB.COLUMN_CATEGORY_categoryorder);
        cursStoreCategory.moveToFirst();

        while (!cursStoreCategory.isAfterLast()) {

            int storecategoryID = cursStoreCategory.getInt(cursStoreCategory.getColumnIndex(SQLiteDB.COLUMN_STORECATEGORY_id));

            // STORE CATEGORY GROUP
            Cursor cursStoreCategoryGroups = sqlLibrary.RawQuerySelect("SELECT tblstorecateggroup.id, " + SQLiteDB.TABLE_GROUP + "." + SQLiteDB.COLUMN_GROUP_groupid + " FROM " + SQLiteDB.TABLE_STORECATEGORYGROUP
                    + " JOIN " + SQLiteDB.TABLE_GROUP + " ON " + SQLiteDB.TABLE_GROUP + "." + SQLiteDB.COLUMN_GROUP_id + " = " + SQLiteDB.TABLE_STORECATEGORYGROUP + "." + SQLiteDB.COLUMN_STORECATEGORYGROUP_groupid
                    + " WHERE " + SQLiteDB.TABLE_STORECATEGORYGROUP + "." + SQLiteDB.COLUMN_STORECATEGORYGROUP_storecategid + " = " + storecategoryID
                    + " ORDER BY " + SQLiteDB.COLUMN_GROUP_grouporder);
            cursStoreCategoryGroups.moveToFirst();

            while (!cursStoreCategoryGroups.isAfterLast()) {

                String groupID = cursStoreCategoryGroups.getString(cursStoreCategoryGroups.getColumnIndex(SQLiteDB.COLUMN_GROUP_groupid));
                int storeCategroupID = cursStoreCategoryGroups.getInt(cursStoreCategoryGroups.getColumnIndex(SQLiteDB.COLUMN_STORECATEGORYGROUP_id));

                if (!sqlLibrary.HasQuestionsPerGroup(storeCategroupID)) {
                    cursStoreCategoryGroups.moveToNext();
                    continue;
                }

                // UPDATE OSA
                Cursor cursOsalist = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_OSALIST, SQLiteDB.COLUMN_OSALIST_osakeygroupid + " = '" + groupID + "'");
                cursOsalist.moveToFirst();
                if (cursOsalist.getCount() > 0) {
                    nTotalCorrectOSA += sqlLibrary.GetCorrectAnswersComp(String.valueOf(storeCategroupID));
                    nTotalQuestionsOSA += sqlLibrary.GetTotalQuestionsComputation(String.valueOf(storeCategroupID));
                }
                cursOsalist.close();

                // UPDATE NPI
                Cursor cursNpi = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_NPI, SQLiteDB.COLUMN_NPI_keygroupid + " = '" + groupID + "'");
                cursNpi.moveToFirst();
                if (cursNpi.getCount() > 0) {
                    nTotalCorrectNPI += sqlLibrary.GetCorrectAnswersComp(String.valueOf(storeCategroupID));
                    nTotalQuestionsNPI += sqlLibrary.GetTotalQuestionsComputation(String.valueOf(storeCategroupID));
                }
                cursNpi.close();

                // UPDATE PLANOGRAM
                Cursor cursPlanogram = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_PLANOGRAM, SQLiteDB.COLUMN_PLANOGRAM_keygroupid + " = '" + groupID + "'");
                cursPlanogram.moveToFirst();
                if (cursPlanogram.getCount() > 0) {
                    nTotalCorrectPlano += sqlLibrary.GetCorrectAnswersComp(String.valueOf(storeCategroupID));
                    nTotalQuestionsPlano += sqlLibrary.GetTotalQuestionsComputation(String.valueOf(storeCategroupID));;
                }
                cursPlanogram.close();

                cursStoreCategoryGroups.moveToNext();
            }
            cursStoreCategoryGroups.close();
            cursStoreCategory.moveToNext();
        }
        cursStoreCategory.close();

        // TOTAL OSA COMPUTATION
        double dTotalOsa = ((double) nTotalCorrectOSA / (double) nTotalQuestionsOSA) * 100;
        double dTotalNpi = ((double) nTotalCorrectNPI / (double) nTotalQuestionsNPI) * 100;
        double dTotalPlanogram = ((double) nTotalCorrectPlano / (double) nTotalQuestionsPlano) * 100;

        if(Double.isNaN(dTotalOsa) || Double.isInfinite(dTotalOsa)) dTotalOsa = 0.00;
        if(Double.isNaN(dTotalNpi) || Double.isInfinite(dTotalNpi)) dTotalNpi = 0.00;
        if(Double.isNaN(dTotalPlanogram) || Double.isInfinite(dTotalPlanogram)) dTotalPlanogram = 0.00;

        osa = String.format(Locale.getDefault(), "%.2f", dTotalOsa);
        npi = String.format(Locale.getDefault(), "%.2f", dTotalNpi);
        planogram = String.format(Locale.getDefault(), "%.2f", dTotalPlanogram);

        String[] aUpdateFields = new String[]{
                SQLiteDB.COLUMN_STORE_status,
                SQLiteDB.COLUMN_STORE_initial,
                SQLiteDB.COLUMN_STORE_final,
                SQLiteDB.COLUMN_STORE_osa,
                SQLiteDB.COLUMN_STORE_npi,
                SQLiteDB.COLUMN_STORE_planogram
        };

        String[] aUpdateValues = new String[] {
                storeStatus,
                storeFinalvalue,
                storeFinalvalue,
                osa,
                npi,
                planogram
        };

        if(General.selectedStore != null) {
            General.selectedStore.finalValue = Integer.parseInt(storeFinalvalue);
            General.selectedStore.initialValue = Integer.parseInt(storeFinalvalue);
            General.selectedStore.status = Integer.parseInt(storeStatus);
            General.selectedStore.osa = dTotalOsa;
            General.selectedStore.npi = dTotalNpi;
            General.selectedStore.planogram = dTotalPlanogram;
        }

        sqlLibrary.UpdateRecord(SQLiteDB.TABLE_STORE, SQLiteDB.COLUMN_STORE_id, String.valueOf(General.selectedStore.storeID), aUpdateFields, aUpdateValues);

        cursCountCorrect.close();
        cursCountComplete.close();
/*        if(nCorrectCategory >= nGradeMatrix) {
            sqlLibrary.UpdateRecord(SQLiteDB.TABLE_STORE, SQLiteDB.COLUMN_STORE_id, General.storeid, new String[] { SQLiteDB.COLUMN_STORE_initial, SQLiteDB.COLUMN_STORE_final }, new String[] { "1","1" });
        }*/
    }

    //UPDATING STATUS AND GROUP STATUS OF STORE CATEGORY GROUP
    private void UpdateStoreCategoryGroup() throws Exception {

        Cursor cursTempQuestion = sqlLibrary.RawQuerySelect("SELECT COUNT(*) AS totexpectedans FROM " + SQLiteDB.TABLE_STOREQUESTION
                + " JOIN " + SQLiteDB.TABLE_QUESTION + " ON " + SQLiteDB.TABLE_QUESTION + "." + SQLiteDB.COLUMN_QUESTION_id + " = " + SQLiteDB.TABLE_STOREQUESTION + "." + SQLiteDB.COLUMN_STOREQUESTION_questionid
                + " WHERE " + SQLiteDB.COLUMN_QUESTION_expectedans + " != ''"
                + " AND " + SQLiteDB.COLUMN_STOREQUESTION_storecategorygroupid + " = " + storeCategoryGroupID);
        cursTempQuestion.moveToFirst();

        int nCorrectAns = sqlLibrary.GetCorrectAnswers(storeCategoryGroupID);
        int nTotExpectedAns = cursTempQuestion.getInt(cursTempQuestion.getColumnIndex("totexpectedans"));
        int nTotRequiredAns = sqlLibrary.GetRequiredQuestions(storeCategoryGroupID);
        int nTotAnswered = sqlLibrary.GetNumberOfAnsweredInGroup(storeCategoryGroupID);
        int nTotQuestions = sqlLibrary.GetTotalQuestions(storeCategoryGroupID);

        int status = 0;
        // 0 = FAILED
        // 1 = PASSED

        // GET OSA LIST AND LOOKUP
        if(GetOsaListLookup(nCorrectAns)) {
            status = 1;
        }

        else if(GetSOSListLookup()) { // GET SOS LIST AND LOOKUP
            if(!SOSAnswer.isEmpty()) {
                String[] arrSOS = SOSAnswer.split(",");
                double totalAns = Double.parseDouble(arrSOS[0]);
                double percentage = Double.parseDouble(arrSOS[1]);
                if (totalAns >= percentage)
                    status = 1;
            }

            else {
                if(((nTotExpectedAns == 0) || (nCorrectAns >= nTotExpectedAns)))
                    status = 1;
            }
        }
        else {
            if(((nTotExpectedAns == 0) || (nCorrectAns >= nTotExpectedAns)))
                status = 1;
        }

        int groupStatus = 0;
        // 0 = PENDING
        // 1 = PARTIAL
        // 2 = COMPLETE


        if(nTotAnswered != nTotQuestions) groupStatus = 1;
        if(nTotAnswered >= nTotRequiredAns) groupStatus = 2;

        String[] aUpdateFields = new String[] {
                SQLiteDB.COLUMN_STORECATEGORYGROUP_status,
                SQLiteDB.COLUMN_STORECATEGORYGROUP_initial,
                SQLiteDB.COLUMN_STORECATEGORYGROUP_final
        };
        String[] aUpdateValues = new String[] {
                String.valueOf(groupStatus),
                String.valueOf(status),
                String.valueOf(status)
        };

        cursTempQuestion.close();
        sqlLibrary.UpdateRecord(SQLiteDB.TABLE_STORECATEGORYGROUP, SQLiteDB.COLUMN_STORECATEGORYGROUP_id, storeCategoryGroupID, aUpdateFields, aUpdateValues);
    }

    // UPDATE STORE CATEGORY STATUS
    private void UpdateStoreCategory() throws Exception {
        Cursor cursCorrectAns = sqlLibrary.RawQuerySelect("SELECT COUNT(*) AS groupCorrectAns FROM " + SQLiteDB.TABLE_STORECATEGORYGROUP
                                    + " WHERE " + SQLiteDB.COLUMN_STORECATEGORYGROUP_storecategid + " = " + storeCategoryID
                                    + " AND " + SQLiteDB.COLUMN_STORECATEGORYGROUP_final + " = '1'");
        cursCorrectAns.moveToFirst();

        Cursor cursTotalAnswered = sqlLibrary.RawQuerySelect("SELECT COUNT(*) AS groupTotAns FROM " + SQLiteDB.TABLE_STORECATEGORYGROUP
                + " WHERE " + SQLiteDB.COLUMN_STORECATEGORYGROUP_storecategid + " = " + storeCategoryID);
        cursTotalAnswered.moveToFirst();

        Cursor cursCompleted = sqlLibrary.RawQuerySelect("SELECT COUNT(*) AS grpCompleted FROM " + SQLiteDB.TABLE_STORECATEGORYGROUP
                + " WHERE " + SQLiteDB.COLUMN_STORECATEGORYGROUP_storecategid + " = " + storeCategoryID
                + " AND " + SQLiteDB.COLUMN_STORECATEGORYGROUP_status + " = '2'");
        cursCompleted.moveToFirst();

        int nGrpCorrectAns = cursCorrectAns.getInt(cursCorrectAns.getColumnIndex("groupCorrectAns"));
        int nGrpTotalAns = cursTotalAnswered.getInt(cursTotalAnswered.getColumnIndex("groupTotAns"));
        int nGrpTotalCompleted = cursCompleted.getInt(cursCompleted.getColumnIndex("grpCompleted"));

        int categStatus = 0;
        // 0 = PENDING
        // 1 = PARTIAL
        // 2 = COMPLETE

        if(nGrpTotalCompleted > 0) categStatus = 1;
        if(nGrpTotalCompleted == nGrpTotalAns) categStatus = 2;

        int categFinalValue = 0;
        // 0 = FAILED
        // 1 = PASSED

        if(nGrpCorrectAns == nGrpTotalAns) categFinalValue = 1;

        cursCorrectAns.close();
        cursTotalAnswered.close();
        cursCompleted.close();

        String[] aUpdateFields = new String[] {
                SQLiteDB.COLUMN_STORECATEGORY_status,
                SQLiteDB.COLUMN_STORECATEGORY_initial,
                SQLiteDB.COLUMN_STORECATEGORY_final,
        };
        String[] aUpdateValues = new String[] {
                String.valueOf(categStatus),
                String.valueOf(categFinalValue),
                String.valueOf(categFinalValue)
        };

        sqlLibrary.UpdateRecord(SQLiteDB.TABLE_STORECATEGORY, SQLiteDB.COLUMN_STORECATEGORY_id, storeCategoryID, aUpdateFields, aUpdateValues);
    }

    private String GetSOSTarget() {
        String strRet = "";

        Cursor cursGetPercent = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_SOSLOOKUP, SQLiteDB.COLUMN_SOSLOOKUP_storeid + " = '" + General.selectedStore.webStoreID + "' AND " + SQLiteDB.COLUMN_SOSLOOKUP_categoryid + " = '" + categoryid + "'" );
        cursGetPercent.moveToFirst();

        if(cursGetPercent.getCount() > 0) {
            double perc = cursGetPercent.getDouble(cursGetPercent.getColumnIndex(SQLiteDB.COLUMN_SOSLOOKUP_value));
            double converted = perc * 100;
            strRet = String.format("%.2f", converted) + " %";
        }

        cursGetPercent.close();
        return strRet;
    }

    // GET IF FORM GROUP CustomerStoreItemID IS EXISTING IN SOS LIST
    private boolean GetSOSListLookup() {
        boolean res = false;

        Cursor cursGetSOSList = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_SOSLIST, SQLiteDB.COLUMN_SOSLIST_soskeygroupid + " = " + formgroupID); // 562
        cursGetSOSList.moveToFirst();
        if(cursGetSOSList.getCount() > 0) {
            res = true;
        }
        cursGetSOSList.close();
        return res;
    }

    // GET OSA LIST AND LOOKUP
    private boolean GetOsaListLookup(int correctAnswer) {

        boolean res = false;

        Cursor cursOsalist = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_OSALIST, SQLiteDB.COLUMN_OSALIST_osakeygroupid + " = '" + formgroupID + "'");
        cursOsalist.moveToFirst();

        if(cursOsalist.getCount() > 0) {

            Cursor cursGetOsalookup = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_OSALOOKUP, SQLiteDB.COLUMN_OSALOOKUP_storeid + " = " + General.selectedStore.webStoreID + " AND " + SQLiteDB.COLUMN_OSALOOKUP_categoryid + " = " + categoryid);
            cursGetOsalookup.moveToFirst();

            if(cursGetOsalookup.getCount() > 0) {
                int target = cursGetOsalookup.getInt(cursGetOsalookup.getColumnIndex(SQLiteDB.COLUMN_OSALOOKUP_target));
                if(correctAnswer >= target)
                    res = true;
            }
        }

        return res;
    }

    // SET FORM ANSWER
    private void SetFormAnswer(String sAnswer, int nqid, View viewForm, String sformtypeid) {

        try {

            PutAnswers(nqid, viewForm, sAnswer, sformtypeid);

            switch (sformtypeid) {

                case "2": // IMAGE CAPTURE
                    ImageView imgvw = (ImageView) viewForm;
                    File imgFile = new File(AppSettings.captureFolder, sAnswer);

                    if (imgFile.exists()) {
                        Bitmap bitmapMaster = SetImageOptions(imgFile);
                        imgvw.setImageBitmap(bitmapMaster);
                    }
                    break;

                case "3":
                    EditText txtNum = (EditText) viewForm;
                    txtNum.setText(tcrLib.ValidateNumericValue(sAnswer.trim()));
                    break;

                case "6": // SIGNATURE CAPTURE
                    File signfile = new File(AppSettings.signatureFolder, sAnswer);
                    ImageView imgvwSign = (ImageView) viewForm;
                    if (signfile.exists()) {
                        Bitmap bitmapMaster = SetImageOptions(signfile);
                        imgvwSign.setImageBitmap(bitmapMaster);
                    }
                    break;

                case "9": // MULTI ITEM
                    CheckBox cbSetAnswer = (CheckBox) viewForm;
                    cbSetAnswer.setChecked(true);
                    break;

                case "10": // SINGLE ITEM
                    RadioButton rbSetAnswer = (RadioButton) viewForm;
                    rbSetAnswer.setChecked(true);
                    break;

                case "11": // COMPUTATIONAL
                    EditText txtAnswerComp = (EditText) viewForm;
                    int ntxtID = txtAnswerComp.getId();
                    String sAnswerTrimmed = sAnswer.trim().replace("\"", "").replace(" ", "").replace("(", "").replace(")", "");
                    String finalTotalValue = sAnswerTrimmed.split("=")[1];
                    hmCompValues.put(ntxtID, Double.parseDouble(finalTotalValue));
                    txtAnswerComp.setText(finalTotalValue);
                    break;

                case "12":
                    RadioButton rbConditional = (RadioButton) viewForm;

                    String[] wholeAnswer = sAnswer.split("\\|");
                    String outerConditionAns = wholeAnswer[0];
                    String rbText = rbConditional.getText().toString().toUpperCase();

                    if (outerConditionAns.equals(rbText)) { // if condition answer == radiobutton value
                        rbConditional.setChecked(true);
                        rbConditional.performClick();
                    }
                    break;

                default: //NUMERIC, SINGLE LINE, MULTI LINE, DATE, TIME,
                    EditText txtBox = (EditText) viewForm;
                    txtBox.setText(sAnswer.trim());
                    break;
            }
        }
        catch (Exception ex) {
            String err = "Error in setting form answer.";
            String exErr = ex.getMessage() != null ? ex.getMessage() : err;
            Toast.makeText(QuestionsActivity.this, err, Toast.LENGTH_SHORT).show();
            errorLog.appendLog(exErr, TAG);
        }
    }

    private Bitmap SetImageOptions(File imgFile) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        options.inJustDecodeBounds = true;

        options.inSampleSize = calculateInSampleSize(options, 180, 180);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    // SET ANSWER FOR INNER FORMS
    private void SetFormAnswerInner(View viewForm, String sformtypeid, int childFormid, int qid) {

        Cursor cursChildanswer = sqlLibrary.GetDataCursor(SQLiteDB.TABLE_CONDITIONAL_ANSWERS, SQLiteDB.COLUMN_CONDANS_questionid + " = '" + qid + "' AND " + SQLiteDB.COLUMN_CONDANS_conditionalformid + " = '" + childFormid + "'");
        cursChildanswer.moveToFirst();
        String sAnswer = "";
        int condformid = 0;
        int condformtypeid = 0;


        if(cursChildanswer.getCount() > 0) {
            sAnswer = cursChildanswer.getString(cursChildanswer.getColumnIndex(SQLiteDB.COLUMN_CONDANS_conditionalanswer)).trim();
            condformid = cursChildanswer.getInt(cursChildanswer.getColumnIndex(SQLiteDB.COLUMN_CONDANS_conditionalformid));
            condformtypeid = cursChildanswer.getInt(cursChildanswer.getColumnIndex(SQLiteDB.COLUMN_CONDANS_conditionalformtypeid));
        }
        else return;

        PutChildAnswer(condformid, qid, sAnswer, condformtypeid);

        switch (sformtypeid) {

            case "2": // IMAGE CAPTURE
                ImageView imgvw = (ImageView) viewForm;
                File imgFile = new  File(AppSettings.captureFolder, sAnswer);

                if(imgFile.exists()) {
                    Bitmap bitmapMaster = SetImageOptions(imgFile);
                    imgvw.setImageBitmap(bitmapMaster);
                }
                break;

            case "6": // SIGNATURE CAPTURE
                File signfile = new File(AppSettings.signatureFolder, sAnswer);
                ImageView imgvwSign = (ImageView) viewForm;
                if(signfile.exists()) {
                    Bitmap bitmapMaster = SetImageOptions(signfile);
                    imgvwSign.setImageBitmap(bitmapMaster);
                }
                break;

            case "9": // MULTI ITEM
                CheckBox cbSetAnswer = (CheckBox) viewForm;
                String cbID = String.valueOf(cbSetAnswer.getId());
                String[] arrCbAnswers = sAnswer.split(",");

                for (String cbAns : arrCbAnswers) {
                    if(cbAns.equals(cbID)) {
                        cbSetAnswer.setChecked(true);
                        hmMultiCheckConditionAns.put(Integer.valueOf(cbID), cbSetAnswer);
                        break;
                    }
                }

                break;

            case "10": // SINGLE ITEM
                RadioButton rbSetAnswer = (RadioButton) viewForm;
                if(String.valueOf(rbSetAnswer.getId()).equals(sAnswer))
                    rbSetAnswer.setChecked(true);

                break;

            default: // SINGLE LINE, MULTI LINE, DATE, TIME,
                EditText txtBox = (EditText) viewForm;
                txtBox.setText(sAnswer.trim());
                break;
        }

        cursChildanswer.close();
    }

    // SET ANSWER FOR INNER FORMS
    private void SetAnswerComputational(String sAnswer, View viewForm) {

        EditText txtAnswerComp = (EditText) viewForm;

        String ansFormid = String.valueOf(txtAnswerComp.getId());
        String sAnswerTrimmed = sAnswer.trim().replace("\"", "").replace(" ","").replace("(","").replace(")", "");
        String[] arrAnswerValue = sAnswerTrimmed.split("=")[0].split("[-+*/]");

        String finalValue = "";

        for (String answerValue : arrAnswerValue) {
            String[] answerpair = answerValue.split("_");
            String answerformid = answerpair[0];

            if(answerformid.equals(ansFormid)) {
                finalValue = answerpair[1];
                hmCompValues.put(Integer.parseInt(answerformid), Double.parseDouble(finalValue));
                break;
            }
        }
        txtAnswerComp.setText(finalValue);
    }

    // SIGNATURE LOAD
    @Override
    protected void onResume() {
        super.onResume();

        File signfile = null;

            for (HashMap.Entry<Integer, ImageView> entry : General.hmSignature.entrySet()) {

                int qid = entry.getKey();

                String filename = General.usercode + "_SIGN_" + String.valueOf(qid) + ".png";
                Uri uriSignpath = AppSettings.GetUriQuestionImagePath(filename);

                signfile = new File(uriSignpath.getPath());

                if (signfile.exists()) {
                    ImageView imgvw = General.hmSignature.get(qid);

                    if(imgvw != null) {
                        Bitmap bitmapMaster = SetImageOptions(signfile);
                        imgvw.setImageBitmap(bitmapMaster);
                        this.PutAnswers(qid, imgvw, filename, "6");
                    }
                }
            }
    }

    // FOR CAMERA IMAGE QUESTION
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_OK && requestCode == Results.CAMERA_REQUEST) {

            switch (CONDITIONAL_MODE) {
                case 0: // MAIN IMAGE CAPTURE
                    if(hmUriImagequestionfile.size() > 0) {

                        File fImageCapture = new File(hmUriImagequestionfile.get(imgFilename).getPath());
                        if(fImageCapture == null)
                            return;

                        if(fImageCapture.exists()) {
                            Bitmap bitmapMaster = SetImageOptions(fImageCapture);
                            captureImgview.setImageBitmap(bitmapMaster);
                            PutAnswers(imgQuestionid, captureImgview, imgFilename, imgFormtypeid);
                        }
                        else {
                            Toast.makeText(QuestionsActivity.this, fImageCapture.getPath() + " is not existing", Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
                case 1: // IMAGE CAPTURE INSIDE CONDITIONAL)
                    if(hmUriCondImagefile.size() > 0) {
                        File fImageCaptureCond = new File(hmUriCondImagefile.get(imgFilename).getPath());
                        if(fImageCaptureCond == null)
                            return;

                        if(fImageCaptureCond.exists()) {
                            Bitmap bitmapMaster = SetImageOptions(fImageCaptureCond);
                            captureImgview.setImageBitmap(bitmapMaster);
                            PutChildAnswer(CAMERA_CONDFORM_ID, CAMERA_COND_QUESTION_ID, CAMERA_COND_CHILDANSWER, CAMERA_COND_FORMTYPE_ID);
                        }
                        else {
                            Toast.makeText(QuestionsActivity.this, fImageCaptureCond.getPath() + " is not existing", Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.hold, R.anim.slide_in_right);
        System.gc();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
