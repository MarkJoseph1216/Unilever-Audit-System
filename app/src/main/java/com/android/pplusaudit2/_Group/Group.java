package com.android.pplusaudit2._Group;

import com.android.pplusaudit2.General;

/**
 * Created by ULTRABOOK on 9/22/2015.
 */
public class Group {

    public final int Groupid;
    public final String Groupdesc;
    public final String audittempid_categid_grpid;
    public final String groupQuestionsStatus; // PENDING, PARTIAL, COMPLETE
    public final General.SCORE_STATUS groupScoreStatus; // true - PASSED, false = FAILED
    public int webGroupID;

    public Group(int grpid, String grpdesc, String tempid_categid_grpid, String grpQuestionStatus, General.SCORE_STATUS grpScoreStatus) {
        this.Groupid = grpid;
        this.Groupdesc = grpdesc;
        this.audittempid_categid_grpid = tempid_categid_grpid;
        this.groupQuestionsStatus = grpQuestionStatus;
        this.groupScoreStatus = grpScoreStatus;
        this.webGroupID = -1;
    }
}
