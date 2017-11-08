package com.android.settings.nfc;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.settings.nfc.PaymentBackend.PaymentAppInfo;
import java.util.HashSet;
import java.util.List;

public class NfcPayPreference extends Preference implements OnClickListener {
    private static final boolean IS_NFC_PAYMENT_OPENAPP = SystemProperties.getBoolean("ro.config.nfc_ce_transevt", false);
    private PaymentAppInfo mAppInfo;
    private NfcPayChangeListener mNfcPayChangeListener;
    private PaymentBackend mPaymentBackend;
    private PreferenceViewHolder mView;
    private MyLongClickListener myLongClickListener = null;

    public interface NfcPayChangeListener {
        void onNfcPayChanged(NfcPayPreference nfcPayPreference);
    }

    private static class MyLongClickListener implements OnLongClickListener {
        final Context mContext;

        MyLongClickListener(Context context) {
            this.mContext = context;
        }

        public boolean onLongClick(View v) {
            if (v.getTag() instanceof PaymentAppInfo) {
                PaymentAppInfo appInfo = (PaymentAppInfo) v.getTag();
                if (!(appInfo.componentName == null || this.mContext == null)) {
                    Log.d("AndroidBeam", "Current long click app component is: " + appInfo.componentName.toString());
                    startPayApp(appInfo);
                }
            }
            return true;
        }

        private void startPayApp(PaymentAppInfo appInfo) {
            Intent gsmaIntent = new Intent();
            PackageManager pm = this.mContext.getPackageManager();
            if (pm != null) {
                gsmaIntent.setAction("com.gsma.services.nfc.SELECT_DEFAULT_SERVICE");
                gsmaIntent.addFlags(32);
                gsmaIntent.setPackage(appInfo.componentName.getPackageName());
                List<ResolveInfo> ris = pm.queryIntentActivities(gsmaIntent, 0);
                if (ris == null || ris.size() <= 0) {
                    Log.d("AndroidBeam", "Can not find SELECT_DEFAULT_SERVICE activity, check the activity for PaymentApp");
                    gsmaIntent = pm.getLaunchIntentForPackage(appInfo.componentName.getPackageName());
                    if (gsmaIntent == null) {
                        Log.e("AndroidBeam", "Can not find matched activity to start");
                        return;
                    }
                    gsmaIntent.setAction("com.gsma.services.nfc.SELECT_DEFAULT_SERVICE");
                    gsmaIntent.addFlags(32);
                    gsmaIntent.setPackage(appInfo.componentName.getPackageName());
                    if (gsmaIntent.getCategories() != null) {
                        for (String c : new HashSet(gsmaIntent.getCategories())) {
                            gsmaIntent.removeCategory(c);
                        }
                    }
                }
                try {
                    this.mContext.startActivity(gsmaIntent);
                } catch (ActivityNotFoundException e) {
                    Log.e("AndroidBeam", "Can not find matched activity to start");
                }
            }
        }
    }

    public NfcPayPreference(PaymentAppInfo appInfo, PaymentBackend backend, Context context) {
        super(context);
        setLayoutResource(2130968881);
        setWidgetLayoutResource(2130969002);
        this.mAppInfo = appInfo;
        this.mPaymentBackend = backend;
        refresh();
        if (IS_NFC_PAYMENT_OPENAPP) {
            this.myLongClickListener = new MyLongClickListener(context);
        }
    }

    public PaymentAppInfo getNfcPayment() {
        return this.mAppInfo;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        if (this.mAppInfo != null) {
            this.mView = view;
            updateWidgetView(view);
        }
    }

    public void setListener(NfcPayChangeListener mlistener) {
        this.mNfcPayChangeListener = mlistener;
    }

    public void clearDefault(PaymentAppInfo appInfo) {
        Log.d("AndroidBeam", "NfcPayPreference clearDefault");
        appInfo.isDefault = false;
    }

    public void setTrustSummary(boolean istrusted) {
        if (this.mView != null) {
            TextView summary_istrusted = (TextView) this.mView.findViewById(2131886825);
            if (istrusted && summary_istrusted != null) {
                summary_istrusted.setVisibility(0);
                summary_istrusted.setText(2131628486);
            }
        }
    }

    public void refresh() {
        if (this.mView != null) {
            Log.d("AndroidBeam", "NfcPayPreference refresh " + this.mAppInfo.label + " " + this.mAppInfo.isDefault);
            ((RadioButton) this.mView.findViewById(2131886824)).setChecked(this.mAppInfo.isDefault);
        }
    }

    private void updateWidgetView(PreferenceViewHolder view) {
        ImageView imageView = (ImageView) view.findViewById(2131886823);
        TextView textView = (TextView) view.findViewById(2131886246);
        RadioButton radioButton = (RadioButton) view.findViewById(2131886824);
        RelativeLayout layout = view.itemView;
        if (imageView == null || textView == null || radioButton == null) {
            Log.w("AndroidBeam", "view not found!");
            return;
        }
        layout.setOnClickListener(this);
        layout.setTag(this.mAppInfo);
        imageView.setImageDrawable(this.mAppInfo.banner);
        imageView.setTag(this.mAppInfo);
        imageView.setContentDescription(this.mAppInfo.label);
        imageView.setOnClickListener(this);
        textView.setText(this.mAppInfo.label);
        radioButton.setChecked(this.mAppInfo.isDefault);
        radioButton.setContentDescription(this.mAppInfo.label);
        radioButton.setOnClickListener(this);
        radioButton.setTag(this.mAppInfo);
        if (IS_NFC_PAYMENT_OPENAPP) {
            layout.setOnLongClickListener(this.myLongClickListener);
            imageView.setOnLongClickListener(this.myLongClickListener);
        }
    }

    public void onClick(View view) {
        makeDefault((PaymentAppInfo) view.getTag());
        Log.d("AndroidBeam", "NfcPayPreference onCheckedChanged");
        refresh();
    }

    void makeDefault(PaymentAppInfo appInfo) {
        if (!(appInfo == null || appInfo.isDefault)) {
            this.mPaymentBackend.setDefaultPaymentApp(appInfo.componentName);
            appInfo.isDefault = true;
        }
        if (this.mNfcPayChangeListener != null) {
            this.mNfcPayChangeListener.onNfcPayChanged(this);
        }
    }
}
