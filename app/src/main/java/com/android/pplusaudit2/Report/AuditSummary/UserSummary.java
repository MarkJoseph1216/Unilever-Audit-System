package com.android.pplusaudit2.Report.AuditSummary;

/**
 * Created by ULTRABOOK on 5/10/2016.
 */
public class UserSummary {
    public final String username;
    public final String month;
    public final int storesMapped;
    public final int storesAudited;
    public final int perfectStores;
    public final double storeAchievement;
    public final int categoryDoors;
    public final double categoryAchievements;

    public UserSummary(String username, String month, int storesMapped, int storesAudited, int perfectStores, double storeAchievement, int categoryDoors, double categoryAchievements) {
        this.username = username;
        this.month = month;
        this.storesMapped = storesMapped;
        this.storesAudited = storesAudited;
        this.perfectStores = perfectStores;
        this.storeAchievement = storeAchievement;
        this.categoryDoors = categoryDoors;
        this.categoryAchievements = categoryAchievements;
    }
}
