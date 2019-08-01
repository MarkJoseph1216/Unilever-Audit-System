package com.android.pplusaudit2.Report.CustomerSummary;

import java.util.ArrayList;

/**
 * Created by Lloyd on 8/22/16.
 */

public class Customer {

    public final int customerID;
    public int auditID;
    public String auditName;
    public String customerCode;
    public String customerName;
    public String regionCode;
    public String regionName;
    public String storeName;
    public String channelCode;
    public String template;
    public int perfectStores;

    public double osaAve;
    public double npiAve;
    public double planogramAve;
    public double perfectStoresAve;

    public double osa;
    public double npi;
    public double planogram;

    public double psDoors;
    public int mappedStores;
    public int visitedStores;

    public ArrayList<CustomerStoreItem> customerStoreItems;

    public Customer(int customerID) {
        this.customerID = customerID;
        this.customerStoreItems = new ArrayList<>();
    }
}
