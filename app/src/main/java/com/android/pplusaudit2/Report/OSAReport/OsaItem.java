package com.android.pplusaudit2.Report.OSAReport;

/**
 * Created by Lloyd on 8/23/16.
 */

public class OsaItem {

    public final int osaItemID;
    public double osaPercent;
    public String template;
    public String category;
    public String description;
    public int auditID;
    public int userID;
    public int storeCount;
    public String prompt;
    public String customerName;
    public String group;
    public String channelCode;
    public int availability;

    public OsaItem(int osaItemID) {
        this.osaItemID = osaItemID;
        this.osaPercent = 0.0;
    }
}
