package com.android.pplusaudit2._Questions;

import android.view.View;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by ULTRABOOK on 10/26/2015.
 */
public class Pplus_AnswerClass {

    public final int questionid;
    public final String formtypeid;
    public final View vElement;
    public final String viewValue;

    public Pplus_AnswerClass(int qid, String ftypeid, View vElem, String value) {
        this.questionid = qid;
        this.formtypeid = ftypeid;
        this.vElement = vElem;
        this.viewValue = value;
    }
}
