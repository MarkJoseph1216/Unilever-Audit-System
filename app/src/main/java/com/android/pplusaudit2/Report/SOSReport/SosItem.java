package com.android.pplusaudit2.Report.SOSReport;

/**
 * Created by Lloyd on 8/26/16.
 */

public class SosItem {

    public final int sosID;
    public String customerName;
    public int auditID;
    public String desc;
    public String auditTemplate;
    public String storeCode;
    public String storeName;
    public String category;
    public double target;
    public double psSosMeasurement;

    public SosItem(int sosID) {
        this.sosID = sosID;
        this.target = 0.0;
        this.psSosMeasurement = 0.0;
    }
}
