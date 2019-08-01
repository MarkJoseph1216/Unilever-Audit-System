package com.android.pplusaudit2._Store;

import com.android.pplusaudit2.PJP_Compliance.Compliance;

import java.util.ArrayList;

/**
 * Created by ULTRABOOK on 9/22/2015.
 */
public class Stores {

    public final int storeID;
    public final String storeName;
    public final String storeCode;
    public final String webStoreID;
    public final int auditTemplateId;
    public final String templateName;
    public int finalValue;
    public int initialValue;
    public final boolean isAudited;
    public final boolean isPosted;
    public boolean isChecked = false;
    public String dateCheckedIn = "";
    public String timeChecked = "";
    public String addressChecked = "";
    public final int gradeMatrixId;
    public String remarks = "";

    public int auditID = 0;
    public String account = "";
    public String customerCode = "";
    public String customer = "";
    public String area = "";
    public String regionCode = "";
    public String region = "";
    public String distributorCode = "";
    public String distributor = "";
    public String startDate = "";
    public String endDate = "";
    public String templateCode = "";
    public String templateDate = "";

    public double osa;
    public double npi;
    public double planogram;
    public double perfectStore;

    public int status;

    public ArrayList<Compliance> complianceArrayList = new ArrayList<>();

    public Stores(int id, String code, String webstoreid, String storename, int templateid, String templatename, int finalval, int initialVal, boolean bAns, boolean posted, int gmatrixID) {
        this.storeID = id;
        this.storeName = storename;
        this.auditTemplateId = templateid;
        this.templateName = templatename;
        this.storeCode = code;
        this.webStoreID = webstoreid;
        this.finalValue = finalval;
        this.initialValue = initialVal;
        this.isAudited = bAns;
        this.isPosted = posted;
        this.gradeMatrixId = gmatrixID;
        this.osa = 0.00;
        this.npi = 0.00;
        this.planogram = 0.00;
        this.perfectStore = 0.00;
        this.status = 0;
    }
}
