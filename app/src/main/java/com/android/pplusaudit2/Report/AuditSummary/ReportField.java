package com.android.pplusaudit2.Report.AuditSummary;

/**
 * Created by ULTRABOOK on 6/1/2016.
 */
public class ReportField {
    public final int fieldID;
    public final String fieldTitle;
    public final String fieldDesc;

    public ReportField(int fieldID, String fieldTitle, String fieldDesc) {
        this.fieldID = fieldID;
        this.fieldTitle = fieldTitle;
        this.fieldDesc = fieldDesc;
    }
}
