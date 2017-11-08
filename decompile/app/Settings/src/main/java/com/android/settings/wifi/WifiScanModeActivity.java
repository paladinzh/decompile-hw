package com.android.settings.wifi;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.provider.Settings.Global;
import com.android.settings.fingerprint.HwCustFingerprintSettingsFragmentImpl;

public class WifiScanModeActivity extends Activity {
    private String mApp;
    private DialogFragment mDialog;

    public static class AlertDialogFragment extends DialogFragment {
        private final String mApp;

        static AlertDialogFragment newInstance(String app) {
            return new AlertDialogFragment(app);
        }

        public AlertDialogFragment(String app) {
            this.mApp = app;
        }

        public AlertDialogFragment() {
            this.mApp = null;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new Builder(getActivity()).setMessage(getString(2131624998, new Object[]{this.mApp})).setPositiveButton(2131625000, new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    ((WifiScanModeActivity) AlertDialogFragment.this.getActivity()).doPositiveClick();
                }
            }).setNegativeButton(2131625001, new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    ((WifiScanModeActivity) AlertDialogFragment.this.getActivity()).doNegativeClick();
                }
            }).create();
        }

        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            ((WifiScanModeActivity) getActivity()).doNegativeClick();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (savedInstanceState != null) {
            this.mApp = savedInstanceState.getString(HwCustFingerprintSettingsFragmentImpl.APP_PATTERN);
        } else if (intent == null || !"android.net.wifi.action.REQUEST_SCAN_ALWAYS_AVAILABLE".equals(intent.getAction())) {
            finish();
            return;
        } else {
            this.mApp = getCallingPackage();
            try {
                PackageManager pm = getPackageManager();
                this.mApp = (String) pm.getApplicationLabel(pm.getApplicationInfo(this.mApp, 0));
            } catch (NameNotFoundException e) {
            }
        }
        createDialog();
    }

    private void createDialog() {
        if (this.mDialog == null) {
            this.mDialog = AlertDialogFragment.newInstance(this.mApp);
            this.mDialog.show(getFragmentManager(), "dialog");
        }
    }

    private void dismissDialog() {
        if (this.mDialog != null) {
            this.mDialog.dismiss();
            this.mDialog = null;
        }
    }

    private void doPositiveClick() {
        Global.putInt(getContentResolver(), "wifi_scan_always_enabled", 1);
        Intent intent = new Intent();
        intent.setAction("com.huawei.chr.wifi.action.WIFI_SCANALWAYS_CHANGED");
        intent.putExtra("apkName", this.mApp);
        intent.putExtra("userAction", "positiveClick");
        sendBroadcast(intent);
        setResult(-1);
        finish();
    }

    private void doNegativeClick() {
        Intent intent = new Intent();
        intent.setAction("com.huawei.chr.wifi.action.WIFI_SCANALWAYS_CHANGED");
        intent.putExtra("apkName", this.mApp);
        intent.putExtra("userAction", "negativeClick");
        sendBroadcast(intent);
        setResult(0);
        finish();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(HwCustFingerprintSettingsFragmentImpl.APP_PATTERN, this.mApp);
    }

    public void onPause() {
        super.onPause();
        dismissDialog();
    }

    public void onResume() {
        super.onResume();
        createDialog();
    }
}
