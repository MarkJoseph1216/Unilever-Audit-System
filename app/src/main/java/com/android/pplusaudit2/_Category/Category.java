package com.android.pplusaudit2._Category;

import com.android.pplusaudit2.General;

/**
 * Created by ULTRABOOK on 9/22/2015.
 */
public class Category {

    public final int activityID;
    public final String activityName;
    public final String categoryAndTempid;
    public final String categoryStatus; // PENDING, PARTIAL, COMPLETE
    public final General.SCORE_STATUS categoryScoreStatus; // true - PASSED, false = FAILED
    public final int tempCategoryID;
    public final int webCategoryID;

    public Category(int id, int tempcatid, String activityName, String categTempid, String categStatus, General.SCORE_STATUS categScoreStatus, int webCategid) {
        this.activityID = id;
        this.activityName = activityName;
        this.categoryAndTempid = categTempid;
        this.categoryStatus = categStatus;
        this.categoryScoreStatus = categScoreStatus;
        this.tempCategoryID =  tempcatid;
        this.webCategoryID = webCategid;
    }
}
