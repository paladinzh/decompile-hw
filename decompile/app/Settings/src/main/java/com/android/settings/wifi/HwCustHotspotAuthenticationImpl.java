package com.android.settings.wifi;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.SQLException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.support.v7.preference.TwoStatePreference;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.HwCustSettingsUtils;

public class HwCustHotspotAuthenticationImpl extends HwCustHotspotAuthentication {
    private static final Uri CONTENT_URI = Uri.parse("content://telephony/carriers");
    private static final String DSS_INTENT_ACTION = "com.sprint.dsa.DSA_ACTIVITY";
    private static final String DSS_INTENT_EXTRA_NAME = "com.sprint.dsa.source";
    private static final String DSS_INTENT_EXTRA_VALUE = "hotspot";
    private static final String DSS_INTENT_TYPE = "vnd.sprint.dsa/vnd.sprint.dsa.main";
    private static final String DSS_PACKAGE_VALUE = "com.sprint.dsa";
    private static final boolean IS_SPRINT_WIFI_HOTSPOT_FEATURE_ENABLED = SystemProperties.getBoolean("ro.config.sprint_wifi_hotspot", false);
    private static final boolean LOG_DEBUG = false;
    private static final int LTE_APN_NAME_TYPE_INDEX = 1;
    private static final String[] LTE_APN_PROJECTION = new String[]{"name"};
    public static final int MOBILE_CONNECTED = 2;
    public static final int NOTHING_CONNECTED = 3;
    private static final String RAT_TYPE_EHRDP = "14";
    private static final String RAT_TYPE_LTE = "13";
    private static final String SPRINT_LTE_APN_TYPE = "pam";
    private static final String TAG = "HwCustHotspotAuthenticationImpl";
    private static final String URI_CURRENT = "current";
    private static final String WHERE_CLAUSE = "name = ? AND bearer in (13,14)";
    public static final int WIFI_CONNECTED = 1;
    private Context context;
    private AlertDialog mDialog;
    private ProgressDialog mProgressDialog;
    private WifiManager mWifiManager;

    public void initHwCustHotspotAuthenticationImpl(Context mContext) {
        this.context = mContext;
        this.mWifiManager = (WifiManager) this.context.getApplicationContext().getSystemService("wifi");
    }

    public void custReceiveBroadcast(Intent intent) {
        String action = intent.getAction();
        if (!"android.net.wifi.WIFI_STATE_CHANGED".equals(action) && !"android.net.conn.TETHER_STATE_CHANGED".equals(action) && this.mProgressDialog != null) {
            this.mProgressDialog.dismiss();
        }
    }

    public void custTetherReceiver(Intent intent) {
        String action = intent.getAction();
        if ("android.intent.action.MEDIA_SHARED".equals(action) || "android.intent.action.MEDIA_UNSHARED".equals(action) || "android.hardware.usb.action.USB_STATE".equals(action)) {
            Log.i(TAG, "BroadcastReceiver cancel the authorization progressDialog");
            custStop();
        }
    }

    public void custStop() {
        if (this.mProgressDialog != null) {
            this.mProgressDialog.dismiss();
        }
    }

    public boolean isHotspotAuthorization(boolean enable, TwoStatePreference mCheckBox, int mTetherChoice) {
        if (isSLEntitleSet() && beEntitleExist()) {
            Log.i(TAG, "setSoftapEnabled is open WIFIAP? : " + enable);
            if (enable) {
                int netType = getNetworkType();
                Log.i(TAG, "setSoftapEnabled netType is : " + netType);
                if (netType == 1) {
                    showTurnOffWifiDialog(mCheckBox, mTetherChoice);
                    return true;
                } else if (netType == 3) {
                    Toast.makeText(this.context, 2131629139, 1).show();
                    if (mCheckBox != null) {
                        mCheckBox.setChecked(false);
                    }
                    return true;
                } else {
                    showProgressDialog();
                }
            }
        }
        return false;
    }

    public boolean custUpdateTethering(boolean enabled, TwoStatePreference mUsbTether) {
        if (!isSLEntitleSet() || !beEntitleExist()) {
            return false;
        }
        if (!enabled) {
            mUsbTether.setSummary((CharSequence) "");
        }
        return true;
    }

    private int getNetworkType() {
        ConnectivityManager cm = (ConnectivityManager) this.context.getSystemService("connectivity");
        boolean isWifiConn = false;
        boolean isMobileConn = false;
        if (cm == null) {
            return 3;
        }
        NetworkInfo wifiNetworkInfo = cm.getNetworkInfo(1);
        NetworkInfo mobileNetworkInfo = cm.getNetworkInfo(0);
        if (wifiNetworkInfo != null) {
            isWifiConn = wifiNetworkInfo.isConnected();
        }
        if (mobileNetworkInfo != null) {
            isMobileConn = mobileNetworkInfo.isConnected();
        }
        if (isWifiConn) {
            return 1;
        }
        if (isMobileConn) {
            return 2;
        }
        return 3;
    }

    private void showTurnOffWifiDialog(final TwoStatePreference mCheckBox, final int mTetherChoice) {
        Builder builder = new Builder(this.context);
        builder.setIcon(17301543);
        builder.setTitle(2131629140);
        if (mTetherChoice == 0) {
            builder.setMessage(2131629141);
        } else if (mTetherChoice == 1) {
            builder.setMessage(2131629142);
        }
        builder.setNegativeButton(17039369, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (mCheckBox != null) {
                    mCheckBox.setChecked(false);
                }
            }
        });
        builder.setPositiveButton(17039379, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (mTetherChoice) {
                    case 0:
                        HwCustHotspotAuthenticationImpl.this.setWifiApEnable(true, mCheckBox);
                        return;
                    case 1:
                        HwCustHotspotAuthenticationImpl.this.setUsbTetherEnable(true);
                        return;
                    default:
                        return;
                }
            }
        });
        builder.create().show();
    }

    private void setWifiApEnable(boolean enable, TwoStatePreference mCheckBox) {
        setCommonEnable(enable);
        if (this.mWifiManager.setWifiApEnabled(null, enable)) {
            mCheckBox.setEnabled(false);
        } else {
            mCheckBox.setSummary(2131624910);
        }
    }

    private void setUsbTetherEnable(boolean enable) {
        setCommonEnable(enable);
        ((ConnectivityManager) this.context.getSystemService("connectivity")).setUsbTethering(enable);
    }

    private void setCommonEnable(boolean enable) {
        showProgressDialog();
        ContentResolver cr = this.context.getContentResolver();
        if (this.mWifiManager != null) {
            int wifiState = this.mWifiManager.getWifiState();
            if (!enable) {
                return;
            }
            if (wifiState == 2 || wifiState == 3) {
                this.mWifiManager.setWifiEnabled(false);
                Global.putInt(cr, "wifi_saved_state", 1);
            }
        }
    }

    private void showProgressDialog() {
        if (this.mProgressDialog == null) {
            this.mProgressDialog = new ProgressDialog(this.context);
            this.mProgressDialog.setMessage(this.context.getString(2131629137));
            this.mProgressDialog.setCanceledOnTouchOutside(false);
            this.mProgressDialog.setCancelable(false);
        }
        Log.i(TAG, "showProgressDialog excute");
        this.mProgressDialog.show();
    }

    private boolean beEntitleExist() {
        Intent intent = new Intent();
        if (intent == null) {
            return false;
        }
        intent.setClassName("com.android.huawei.Entitle", "com.android.huawei.Entitle.EntitleSystemEventReceiver");
        if (this.context.getPackageManager().queryBroadcastReceivers(intent, 0).size() > 0) {
            return true;
        }
        return false;
    }

    private boolean isSLEntitleSet() {
        boolean isSLEntitleSet = SystemProperties.getBoolean("ro.config.isSLEntitleSet", false);
        if (!isSLEntitleSet) {
            return isSLEntitleSet;
        }
        boolean isAttCard = isSLEntitleSet;
        String attPlmn = System.getString(this.context.getContentResolver(), "hw_att_operator_numeric");
        String mccmnc = TelephonyManager.getDefault().getSimOperatorNumeric(SubscriptionManager.getDefaultSubscriptionId());
        if (!TextUtils.isEmpty(attPlmn) && !TextUtils.isEmpty(mccmnc)) {
            isAttCard = false;
            String[] custList = attPlmn.split(";");
            for (String equals : custList) {
                isAttCard = equals.equals(mccmnc);
                if (isAttCard) {
                    break;
                }
            }
        }
        return isAttCard;
    }

    public boolean isTetheringAllowed(TwoStatePreference aCheckBox) {
        if (aCheckBox.isChecked()) {
            return Boolean.TRUE.booleanValue();
        }
        if (!IS_SPRINT_WIFI_HOTSPOT_FEATURE_ENABLED || isPamPDNProfileProvisioned()) {
            return Boolean.TRUE.booleanValue();
        }
        aCheckBox.setChecked(false);
        showSprintErrorDialog();
        return Boolean.FALSE.booleanValue();
    }

    private boolean isPamPDNProfileProvisioned() {
        Cursor cursor = null;
        try {
            cursor = this.context.getContentResolver().query(Uri.withAppendedPath(CONTENT_URI, URI_CURRENT), LTE_APN_PROJECTION, WHERE_CLAUSE, new String[]{SPRINT_LTE_APN_TYPE}, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return Boolean.FALSE.booleanValue();
            }
            boolean booleanValue = Boolean.TRUE.booleanValue();
            if (cursor != null) {
                cursor.close();
            }
            return booleanValue;
        } catch (SQLException sqle) {
            Log.e(TAG, "isPamPDNProfileProvisioned -> Exception occurs while getting Sprint APN type " + sqle);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void showSprintErrorDialog() {
        Builder builder = new Builder(this.context);
        builder.setIcon(17301543);
        builder.setTitle(2131629179);
        builder.setMessage(2131629180);
        builder.setNeutralButton(2131629178, null);
        builder.setPositiveButton(2131629177, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (HwCustHotspotAuthenticationImpl.this.checkAppInstalledOrNot(HwCustHotspotAuthenticationImpl.DSS_PACKAGE_VALUE)) {
                    Intent intent = new Intent();
                    intent.setAction(HwCustHotspotAuthenticationImpl.DSS_INTENT_ACTION);
                    intent.setType(HwCustHotspotAuthenticationImpl.DSS_INTENT_TYPE);
                    intent.putExtra(HwCustHotspotAuthenticationImpl.DSS_INTENT_EXTRA_NAME, HwCustHotspotAuthenticationImpl.DSS_INTENT_EXTRA_VALUE);
                    HwCustHotspotAuthenticationImpl.this.context.startActivity(intent);
                    return;
                }
                Log.i(HwCustHotspotAuthenticationImpl.TAG, "DSSClient app not found");
            }
        });
        builder.create().show();
    }

    private boolean checkAppInstalledOrNot(String packagename) {
        try {
            this.context.getPackageManager().getPackageInfo(packagename, 1);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public void handleCustErrorView(TwoStatePreference aCheckBox) {
        if (HwCustSettingsUtils.IS_SPRINT) {
            if (aCheckBox != null) {
                aCheckBox.setChecked(false);
            }
            showDialogForTetheringConnectionError();
        }
    }

    private void showDialogForTetheringConnectionError() {
        if (this.mDialog == null) {
            Builder builder = new Builder(this.context);
            builder.setIcon(17301543);
            builder.setTitle(2131629179);
            builder.setMessage(2131629181);
            builder.setNeutralButton(17039370, null);
            this.mDialog = builder.create();
            this.mDialog.show();
        }
    }
}
