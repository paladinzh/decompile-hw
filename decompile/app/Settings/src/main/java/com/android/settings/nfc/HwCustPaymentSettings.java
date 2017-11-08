package com.android.settings.nfc;

import android.content.Context;

public class HwCustPaymentSettings {
    public PaymentSettings mPaymentSettings;

    public HwCustPaymentSettings(PaymentSettings paymentSettings) {
        this.mPaymentSettings = paymentSettings;
    }

    public void checkCertificatesFromUICC(Context context, PaymentBackend mPaymentBackend) {
    }

    public void clearMap(Context context) {
    }
}
