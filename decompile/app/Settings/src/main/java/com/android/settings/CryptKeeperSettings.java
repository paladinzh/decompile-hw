package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.android.settings.deviceinfo.DefaultStorageLocation;
import com.android.settingslib.Utils;

public class CryptKeeperSettings extends InstrumentedFragment {
    private View mBatteryWarning;
    private View mContentView;
    private AlertDialog mDialog;
    private Button mInitiateButton;
    private OnClickListener mInitiateListener = new OnClickListener() {
        public void onClick(View v) {
            if (!CryptKeeperSettings.this.runKeyguardConfirmation(55)) {
                CryptKeeperSettings.this.mDialog = new Builder(CryptKeeperSettings.this.getActivity()).setTitle(2131624700).setMessage(2131624701).setPositiveButton(17039370, null).create();
                CryptKeeperSettings.this.mDialog.show();
            }
        }
    };
    private IntentFilter mIntentFilter;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int i = 8;
            if (intent.getAction().equals("android.intent.action.BATTERY_CHANGED")) {
                boolean z;
                int i2;
                int level = intent.getIntExtra("level", 0);
                int plugged = intent.getIntExtra("plugged", 0);
                int invalidCharger = intent.getIntExtra("invalid_charger", 0);
                boolean levelOk = level >= 80;
                boolean pluggedOk = (plugged & 7) != 0 ? invalidCharger == 0 : false;
                Button -get2 = CryptKeeperSettings.this.mInitiateButton;
                if (levelOk) {
                    z = pluggedOk;
                } else {
                    z = false;
                }
                -get2.setEnabled(z);
                View -get3 = CryptKeeperSettings.this.mPowerWarning;
                if (pluggedOk) {
                    i2 = 8;
                } else {
                    i2 = 0;
                }
                -get3.setVisibility(i2);
                View -get0 = CryptKeeperSettings.this.mBatteryWarning;
                if (!levelOk) {
                    i = 0;
                }
                -get0.setVisibility(i);
            }
        }
    };
    private View mPowerWarning;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        this.mContentView = inflater.inflate(2130968707, container, false);
        this.mContentView.setScrollBarStyle(33554432);
        Utils.prepareCustomPreferencesList(container, this.mContentView, this.mContentView, true);
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        this.mInitiateButton = (Button) this.mContentView.findViewById(2131886433);
        this.mInitiateButton.setOnClickListener(this.mInitiateListener);
        this.mInitiateButton.setEnabled(false);
        this.mPowerWarning = this.mContentView.findViewById(2131886432);
        this.mBatteryWarning = this.mContentView.findViewById(2131886431);
        ((TextView) this.mContentView.findViewById(2131886430)).setText(getString(2131627905, new Object[]{Utils.formatPercentage(80)}));
        return this.mContentView;
    }

    protected int getMetricsCategory() {
        return 32;
    }

    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(this.mIntentReceiver, this.mIntentFilter);
    }

    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(this.mIntentReceiver);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        Intent intent = activity.getIntent();
        if (intent != null && "android.app.action.START_ENCRYPTION".equals(intent.getAction())) {
            DevicePolicyManager dpm = (DevicePolicyManager) activity.getSystemService("device_policy");
            if (!(dpm == null || dpm.getStorageEncryptionStatus() == 1)) {
                activity.finish();
            }
        }
        if (DefaultStorageLocation.isSdcard()) {
            activity.finish();
        }
    }

    private boolean runKeyguardConfirmation(int request) {
        Resources res = getActivity().getResources();
        ChooseLockSettingsHelper helper = new ChooseLockSettingsHelper(getActivity(), this);
        if (helper.utils().getKeyguardStoredPasswordQuality(UserHandle.myUserId()) != 0) {
            return helper.launchConfirmationActivity(request, res.getText(2131624694), true);
        }
        showFinalConfirmation(1, "");
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 55 && resultCode == -1 && data != null) {
            int type = data.getIntExtra("type", -1);
            String password = data.getStringExtra("password");
            if (!TextUtils.isEmpty(password)) {
                showFinalConfirmation(type, password);
            }
        }
    }

    private void showFinalConfirmation(int type, String password) {
        Preference preference = new Preference(getPreferenceManager().getContext());
        preference.setFragment(CryptKeeperConfirm.class.getName());
        preference.setTitle(2131624702);
        addEncryptionInfoToPreference(preference, type, password);
        ((SettingsActivity) getActivity()).onPreferenceStartFragment(null, preference);
    }

    private void addEncryptionInfoToPreference(Preference preference, int type, String password) {
        if (((DevicePolicyManager) getActivity().getSystemService("device_policy")).getDoNotAskCredentialsOnBoot()) {
            preference.getExtras().putInt("type", 1);
            preference.getExtras().putString("password", "");
            return;
        }
        preference.getExtras().putInt("type", type);
        preference.getExtras().putString("password", password);
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
    }
}
