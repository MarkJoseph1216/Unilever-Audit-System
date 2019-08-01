package com.android.pplusaudit2.Report.ReportDashboard;

/**
 * Created by ULTRABOOK on 5/10/2016.
 */
public class Reports {

    public final int reportID;
    public final String reportName;
    public final String reportDesc;
    public final String reportIcon;

    public Reports(int id, String reportName, String reportDesc, String reportIcon) {
        this.reportID = id;
        this.reportName = reportName;
        this.reportDesc = reportDesc;
        this.reportIcon = reportIcon;
    }
}
