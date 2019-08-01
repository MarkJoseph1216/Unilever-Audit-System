package com.android.pplusaudit2.Report.CustomerSummary;

import com.android.pplusaudit2.Report.StoreSummary.StoreItem;

/**
 * Created by Lloyd on 8/22/16.
 */
public class CustomerStoreItem extends StoreItem {

    public final int CustomerStoreItemID;
    public Customer customer;

    public CustomerStoreItem(int CustomerStoreItemID) {
        this.CustomerStoreItemID = CustomerStoreItemID;
    }

}
