package com.android.mms.ui;

public class DeliveryReportItem {
    String deliveryDate;
    String recipient;
    String status;

    public DeliveryReportItem(String recipient, String status, String deliveryDate) {
        this.recipient = recipient;
        this.status = status;
        this.deliveryDate = deliveryDate;
    }
}
