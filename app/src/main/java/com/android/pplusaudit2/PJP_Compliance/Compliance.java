package com.android.pplusaudit2.PJP_Compliance;

import com.android.pplusaudit2._Store.Stores;

/**
 * Created by ULTRABOOK on 6/7/2016.
 */
public class Compliance {

    public final int complianceID;
    public final String userCode;
    public final int storeID;
    public final String webStoreId;
    public final String date;
    public final String time;
    public final String username;
    public final String longitude;
    public final String latitude;
    public String address;
    public boolean isPosted;

    public Compliance(int complianceID, String userCode, int storeID, String webStoreId, String date, String time, String username, String longitude, String latitude, boolean posted) {
        this.complianceID = complianceID;
        this.userCode = userCode;
        this.storeID = storeID;
        this.webStoreId = webStoreId;
        this.date = date;
        this.time = time;
        this.username = username;
        this.longitude = longitude;
        this.latitude = latitude;
        this.isPosted = posted;
    }
}
