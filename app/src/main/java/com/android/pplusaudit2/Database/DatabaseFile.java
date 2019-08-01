package com.android.pplusaudit2.Database;

/**
 * Created by Lloyd on 7/20/16.
 */

public class DatabaseFile {

    public int ID;
    public int backupID;
    public String fileName;
    public String createdAt;
    public String updatedAt;
    public String databaseVersion;

    public DatabaseFile(int ID, int backupID, String fileName, String createdAt, String updatedAt, String databaseVersion) {
        this.ID = ID;
        this.backupID = backupID;
        this.fileName = fileName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.databaseVersion = databaseVersion;
    }
}
