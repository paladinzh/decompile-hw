package com.android.settings.nfc;

import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.SystemProperties;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import com.android.settings.CustomDialogPreference;
import com.android.settings.nfc.PaymentBackend.Callback;
import com.android.settings.nfc.PaymentBackend.PaymentAppInfo;
import java.util.List;

public class NfcPaymentPreference extends CustomDialogPreference implements Callback, OnClickListener {
    private static final boolean NFC_PAYMENT_OPENAPP = "true".equals(SystemProperties.get("ro.config.nfc_ce_transevt", "false"));
    private final NfcPaymentAdapter mAdapter = new NfcPaymentAdapter();
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private final PaymentBackend mPaymentBackend;
    private ImageView mSettingsButtonView;

    class NfcPaymentAdapter extends BaseAdapter implements OnCheckedChangeListener, OnClickListener {
        private PaymentAppInfo[] appInfos;

        public class ViewHolder {
            public ImageView imageView;
            public RadioButton radioButton;
        }

        public void updateApps(PaymentAppInfo[] appInfos, PaymentAppInfo currentDefault) {
            this.appInfos = appInfos;
            notifyDataSetChanged();
        }

        public int getCount() {
            return this.appInfos.length;
        }

        public PaymentAppInfo getItem(int i) {
            return this.appInfos[i];
        }

        public long getItemId(int i) {
            return (long) this.appInfos[i].componentName.hashCode();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            PaymentAppInfo appInfo = this.appInfos[position];
            if (convertView == null) {
                convertView = NfcPaymentPreference.this.mLayoutInflater.inflate(2130968881, parent, false);
                holder = new ViewHolder();
                holder.imageView = (ImageView) convertView.findViewById(2131886823);
                holder.radioButton = (RadioButton) convertView.findViewById(2131886824);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.imageView.setImageDrawable(appInfo.banner);
            holder.imageView.setTag(appInfo);
            holder.imageView.setOnClickListener(this);
            if (isSupportLongPress()) {
                holder.imageView.setLongClickable(true);
                holder.imageView.setOnLongClickListener(new OnLongClickListener() {
                    public boolean onLongClick(View view) {
                        if (view.getTag() instanceof PaymentAppInfo) {
                            Intent gsmaIntent = NfcPaymentPreference.this.mContext.getPackageManager().getLaunchIntentForPackage(((PaymentAppInfo) view.getTag()).componentName.getPackageName());
                            if (gsmaIntent != null) {
                                gsmaIntent.setAction("com.gsma.services.nfc.SELECT_DEFAULT_SERVICE");
                                gsmaIntent.addFlags(32);
                                gsmaIntent.setPackage(gsmaIntent.getPackage());
                                NfcPaymentPreference.this.mContext.startActivity(gsmaIntent);
                            }
                        }
                        return true;
                    }
                });
            }
            holder.radioButton.setOnCheckedChangeListener(null);
            holder.radioButton.setChecked(appInfo.isDefault);
            holder.radioButton.setContentDescription(appInfo.label);
            holder.radioButton.setOnCheckedChangeListener(this);
            holder.radioButton.setTag(appInfo);
            return convertView;
        }

        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            makeDefault((PaymentAppInfo) compoundButton.getTag());
        }

        public void onClick(View view) {
            makeDefault((PaymentAppInfo) view.getTag());
        }

        private boolean isSupportLongPress() {
            return NfcPaymentPreference.NFC_PAYMENT_OPENAPP;
        }

        void makeDefault(PaymentAppInfo appInfo) {
            if (!appInfo.isDefault) {
                NfcPaymentPreference.this.mPaymentBackend.setDefaultPaymentApp(appInfo.componentName);
            }
            NfcPaymentPreference.this.getDialog().dismiss();
        }
    }

    public NfcPaymentPreference(Context context, PaymentBackend backend) {
        super(context, null);
        this.mPaymentBackend = backend;
        this.mContext = context;
        backend.registerCallback(this);
        setDialogTitle(context.getString(2131626509));
        this.mLayoutInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        setWidgetLayoutResource(2130969002);
        refresh();
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.mSettingsButtonView = (ImageView) view.findViewById(2131886941);
        this.mSettingsButtonView.setOnClickListener(this);
        updateSettingsVisibility();
    }

    public void refresh() {
        List<PaymentAppInfo> appInfos = this.mPaymentBackend.getPaymentAppInfos();
        PaymentAppInfo defaultApp = this.mPaymentBackend.getDefaultApp();
        if (appInfos != null) {
            this.mAdapter.updateApps((PaymentAppInfo[]) appInfos.toArray(new PaymentAppInfo[appInfos.size()]), defaultApp);
        }
        setTitle(2131626503);
        if (defaultApp != null) {
            setSummary(defaultApp.label);
        } else {
            setSummary(this.mContext.getString(2131626504));
        }
        updateSettingsVisibility();
    }

    protected void onPrepareDialogBuilder(Builder builder, DialogInterface.OnClickListener listener) {
        super.onPrepareDialogBuilder(builder, listener);
        builder.setSingleChoiceItems(this.mAdapter, 0, listener);
    }

    public void onPaymentAppsChanged() {
        refresh();
    }

    public void onClick(View view) {
        PaymentAppInfo defaultAppInfo = this.mPaymentBackend.getDefaultApp();
        if (defaultAppInfo != null && defaultAppInfo.settingsComponent != null) {
            Intent settingsIntent = new Intent("android.intent.action.MAIN");
            settingsIntent.setComponent(defaultAppInfo.settingsComponent);
            settingsIntent.addFlags(268435456);
            try {
                this.mContext.startActivity(settingsIntent);
            } catch (ActivityNotFoundException e) {
                Log.e("NfcPaymentPreference", "Settings activity not found.");
            }
        }
    }

    void updateSettingsVisibility() {
        if (this.mSettingsButtonView != null) {
            PaymentAppInfo defaultApp = this.mPaymentBackend.getDefaultApp();
            if (defaultApp == null || defaultApp.settingsComponent == null) {
                this.mSettingsButtonView.setVisibility(8);
            } else {
                this.mSettingsButtonView.setVisibility(0);
            }
        }
    }
}
