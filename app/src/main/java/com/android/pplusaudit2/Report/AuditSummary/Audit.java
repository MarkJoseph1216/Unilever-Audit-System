package com.android.pplusaudit2.Report.AuditSummary;

/**
 * Created by ULTRABOOK on 5/10/2016.
 */
public class Audit {
    public final int auditID;
    public final String auditDesc;

    public Audit(int auditID, String auditDesc) {
        this.auditID = auditID;
        this.auditDesc = auditDesc;
    }
}
