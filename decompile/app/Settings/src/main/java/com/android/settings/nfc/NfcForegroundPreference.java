package com.android.settings.nfc;

import android.content.Context;
import android.support.v7.preference.DropDownPreference;
import com.android.settings.nfc.PaymentBackend.Callback;
import com.android.settings.nfc.PaymentBackend.PaymentAppInfo;

public class NfcForegroundPreference extends DropDownPreference implements Callback {
    private final PaymentBackend mPaymentBackend;

    public NfcForegroundPreference(Context context, PaymentBackend backend) {
        super(context);
        this.mPaymentBackend = backend;
        this.mPaymentBackend.registerCallback(this);
        refresh();
    }

    public void onPaymentAppsChanged() {
        refresh();
    }

    void refresh() {
        PaymentAppInfo defaultApp = this.mPaymentBackend.getDefaultApp();
        boolean foregroundMode = this.mPaymentBackend.isForegroundMode();
        setPersistent(false);
        setTitle(getContext().getString(2131626506));
        setEntries(new CharSequence[]{getContext().getString(2131626508), getContext().getString(2131626507)});
        setEntryValues(new CharSequence[]{"1", "0"});
        if (foregroundMode) {
            setValue("1");
        } else {
            setValue("0");
        }
    }

    protected boolean persistString(String value) {
        boolean z = false;
        PaymentBackend paymentBackend = this.mPaymentBackend;
        if (Integer.parseInt(value) != 0) {
            z = true;
        }
        paymentBackend.setForegroundMode(z);
        return true;
    }
}
