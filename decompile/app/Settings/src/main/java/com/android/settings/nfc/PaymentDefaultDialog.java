package com.android.settings.nfc;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController.AlertParams;
import com.android.settings.nfc.PaymentBackend.PaymentAppInfo;

public final class PaymentDefaultDialog extends AlertActivity implements OnClickListener {
    private PaymentBackend mBackend;
    private ComponentName mNewDefault;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mBackend = new PaymentBackend(this);
        Intent intent = getIntent();
        ComponentName component = null;
        Parcelable parcelable = intent.getParcelableExtra("component");
        if (parcelable instanceof ComponentName) {
            component = (ComponentName) parcelable;
        }
        String category = intent.getStringExtra("category");
        setResult(0);
        if (!buildDialog(component, category)) {
            finish();
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case -1:
                this.mBackend.setDefaultPaymentApp(this.mNewDefault);
                setResult(-1);
                return;
            default:
                return;
        }
    }

    private boolean buildDialog(ComponentName component, String category) {
        if (component == null || category == null) {
            Log.e("PaymentDefaultDialog", "Component or category are null");
            return false;
        } else if ("payment".equals(category)) {
            PaymentAppInfo requestedPaymentApp = null;
            PaymentAppInfo defaultPaymentApp = null;
            for (PaymentAppInfo service : this.mBackend.getPaymentAppInfos()) {
                if (component.equals(service.componentName)) {
                    requestedPaymentApp = service;
                }
                if (service.isDefault) {
                    defaultPaymentApp = service;
                }
            }
            if (requestedPaymentApp == null) {
                Log.e("PaymentDefaultDialog", "Component " + component + " is not a registered payment service.");
                return false;
            }
            ComponentName defaultComponent = this.mBackend.getDefaultPaymentApp();
            if (defaultComponent == null || !defaultComponent.equals(component)) {
                this.mNewDefault = component;
                AlertParams p = this.mAlertParams;
                p.mTitle = getString(2131626514);
                if (defaultPaymentApp == null) {
                    p.mMessage = String.format(getString(2131626515), new Object[]{sanitizePaymentAppCaption(requestedPaymentApp.label.toString())});
                } else {
                    p.mMessage = String.format(getString(2131626516), new Object[]{sanitizePaymentAppCaption(requestedPaymentApp.label.toString()), sanitizePaymentAppCaption(defaultPaymentApp.label.toString())});
                }
                p.mPositiveButtonText = getString(2131624348);
                p.mNegativeButtonText = getString(2131624349);
                p.mPositiveButtonListener = this;
                p.mNegativeButtonListener = this;
                setupAlert();
                return true;
            }
            Log.e("PaymentDefaultDialog", "Component " + component + " is already default.");
            return false;
        } else {
            Log.e("PaymentDefaultDialog", "Don't support defaults for category " + category);
            return false;
        }
    }

    private String sanitizePaymentAppCaption(String input) {
        String sanitizedString = input.replace('\n', ' ').replace('\r', ' ').trim();
        if (sanitizedString.length() > 40) {
            return sanitizedString.substring(0, 40);
        }
        return sanitizedString;
    }
}
