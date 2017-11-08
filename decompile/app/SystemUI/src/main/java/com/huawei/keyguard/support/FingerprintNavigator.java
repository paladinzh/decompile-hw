package com.huawei.keyguard.support;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.telecom.TelecomManager;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.CheckBox;
import com.android.keyguard.R$id;
import com.android.keyguard.R$layout;
import com.android.keyguard.R$string;
import com.android.keyguard.fingerprint.HwCustQuickLaunchApp;
import com.huawei.cust.HwCustUtils;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.cover.CoverViewManager;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.support.magazine.MagazineUtils;
import com.huawei.keyguard.util.FpUtils;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.KeyguardUtils;
import com.huawei.keyguard.util.OsUtils;
import fyusion.vislib.BuildConfig;
import java.util.ArrayList;
import java.util.List;

public class FingerprintNavigator {
    private static String HW_PAY_ACTION = "com.huawei.oto.intent.action.QUICKPAY";
    private static String HW_PAY_APK = "com.huawei.wallet";
    private static final FingerprintNavigator inst = new FingerprintNavigator();
    private BroadcastReceiver closeDialogReceiver;
    private AlertDialog mAlipayAlertDialog;
    private int mBlockCounter;
    private FingerprintManager mFpm;
    private HwCustQuickLaunchApp mHwCustQuickLaunchApp;
    private long mLastAuthTime;
    private boolean mRegisterReceiver;
    private int mUnExecuteFinger;

    public static FingerprintNavigator getInst() {
        return inst;
    }

    private FingerprintNavigator() {
        this.mHwCustQuickLaunchApp = null;
        this.mUnExecuteFinger = -1;
        this.mLastAuthTime = 0;
        this.mBlockCounter = 0;
        this.mRegisterReceiver = false;
        this.closeDialogReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action) || "android.intent.action.SCREEN_OFF".equals(action)) && FingerprintNavigator.this.mAlipayAlertDialog != null && FingerprintNavigator.this.mAlipayAlertDialog.isShowing()) {
                    FingerprintNavigator.this.mAlipayAlertDialog.dismiss();
                }
            }
        };
        this.mHwCustQuickLaunchApp = (HwCustQuickLaunchApp) HwCustUtils.createObj(HwCustQuickLaunchApp.class, new Object[0]);
    }

    public void blockNavigation(boolean block) {
        if (block) {
            this.mBlockCounter++;
        } else {
            this.mBlockCounter = 0;
        }
    }

    public void clearFingerState() {
        this.mUnExecuteFinger = -1;
        this.mLastAuthTime = 0;
    }

    public void storeFingerState(int fingerId) {
        this.mUnExecuteFinger = fingerId;
        this.mLastAuthTime = SystemClock.uptimeMillis();
    }

    public void checkUnexcecuteNavigation(Context context) {
        if (this.mUnExecuteFinger != -1 && this.mLastAuthTime > 0 && SystemClock.uptimeMillis() - this.mLastAuthTime < 180000) {
            launchAppForFinger(context, this.mUnExecuteFinger);
        }
        clearFingerState();
    }

    public void launchAppForFinger(Context context, int fingerId) {
        if (this.mBlockCounter > 0) {
            this.mBlockCounter--;
            HwLog.i("FPNav", "Fingerprint Navigation is Blocked." + this.mBlockCounter);
            return;
        }
        CoverViewManager coverManager = CoverViewManager.getInstance(context);
        if (coverManager != null && coverManager.isCoverAdded() && CoverViewManager.isWindowedCover(context)) {
            storeFingerState(fingerId);
            HwLog.i("FPNav", "Stop navigation as cover is closed");
        } else if (HwKeyguardUpdateMonitor.getInstance().getPhoneState() != 0) {
            HwLog.i("FPNav", "Stop navigation as phone busy");
        } else {
            try {
                launchAppInner(context, fingerId);
            } catch (ActivityNotFoundException e) {
                HwLog.e("FPNav", "launchAppForFinger has ActivityNotFoundException", e);
            } catch (Exception e2) {
                HwLog.e("FPNav", "launchAppForFinger has Exception", e2);
            }
        }
    }

    private void startActivity(Context context, Intent intent) {
        if (intent != null) {
            intent.setFlags(268468224);
            OsUtils.startUserActivity(context, intent);
            makeSureUserHasMultiFinger(context);
            HwLog.i("FPNav", "Keyguard not showing");
        }
    }

    private void launchAppInner(Context context, int fingerId) {
        if (1 == OsUtils.getSecureInt(context, "fp_shortcut_enabled", 0)) {
            startActivity(context, getFingerprintTarget(context, fingerId));
            return;
        }
        if (this.mHwCustQuickLaunchApp != null && isSupportCustLaunch()) {
            this.mHwCustQuickLaunchApp.setFingerprintId(fingerId);
            this.mHwCustQuickLaunchApp.launchApp(context);
        }
    }

    public Intent getFingerprintTarget(Context context, int fingerId) {
        if (OsUtils.getSecureInt(context, "fp_shortcut_payment_fp_id", 0) == fingerId) {
            Intent intent = new Intent();
            intent.setAction(HW_PAY_ACTION);
            intent.setPackage(HW_PAY_APK);
            intent.putExtra("channel", "settings");
            if (KeyguardUtils.getTargetActivitySize(context, intent) >= 1) {
                reportBD(context, "com.huawei.wallet");
                return intent;
            }
            intent = new Intent();
            intent.setComponent(new ComponentName(HW_PAY_APK, HW_PAY_APK + ".view.MainActivity"));
            if (KeyguardUtils.getTargetActivitySize(context, intent) >= 1) {
                reportBD(context, "com.huawei.wallet_main");
                return intent;
            }
        }
        return null;
    }

    private static boolean isSupportCustLaunch() {
        return SystemProperties.getBoolean("ro.config.fp_launch_app", false);
    }

    public static boolean isInFingerNavigation(Context context) {
        if (!FpUtils.isScreenOn(context)) {
            return false;
        }
        if (HwKeyguardUpdateMonitor.getInstance(context).isInBouncer()) {
            HwLog.i("FPNav", "isInFingerNavigation isInBouncer status!");
            return false;
        } else if (isCallIn(context) && isAnswerCallByFingerEnabled(context)) {
            return true;
        } else {
            if (HwKeyguardUpdateMonitor.getInstance(context).isOccluded() && ((KeyguardCfg.isFrontFpNavigationSupport() || FpUtils.isTakePhotoByFingerEnabled(context)) && FpUtils.isFpNativigationAppForeground(context))) {
                return true;
            }
            return HwKeyguardUpdateMonitor.getInstance(context).isOccluded() && ((getAlertSettingsSwitch(context) || getAlertBackSwitch(context)) && getAlertDspStatus(context));
        }
    }

    private static boolean isCallIn(Context context) {
        boolean result = false;
        if (context == null) {
            HwLog.w("FPNav", "isCallIn mContext is null ");
            return false;
        }
        TelecomManager telecomManager = (TelecomManager) context.getSystemService("telecom");
        if (telecomManager != null && telecomManager.isRinging()) {
            result = true;
        }
        return result;
    }

    public static boolean getAlertDspStatus(Context context) {
        if (context == null) {
            HwLog.w("FPNav", "getAlertDspStatus context is null");
            return false;
        }
        try {
            Bundle bundle = context.getContentResolver().call(OsUtils.getUserUri("com.android.deskclock"), "isLockAlarm", null, null);
            if (bundle != null) {
                return bundle.getBoolean("isAlerting", false);
            }
        } catch (Exception e) {
            HwLog.w("FPNav", " Exception " + e.getMessage());
        }
        return false;
    }

    public static boolean getAlertSettingsSwitch(Context context) {
        boolean z = false;
        if (context == null) {
            HwLog.w("FPNav", "getAlertSettingsSwitch context is null");
            return false;
        }
        if (OsUtils.getSecureInt(context, "fp_stop_alarm", 0) != 0) {
            z = true;
        }
        return z;
    }

    public static boolean isAnswerCallByFingerEnabled(Context context) {
        boolean z = false;
        if (context == null) {
            HwLog.w("FPNav", "getCallSwitch context is null");
            return false;
        }
        if (OsUtils.getSecureInt(context, "fp_answer_call", 0) != 0) {
            z = true;
        }
        return z;
    }

    public static boolean getAlertBackSwitch(Context context) {
        boolean z = false;
        if (context == null) {
            HwLog.w("FPNav", "getAlertSettingsSwitch context is null");
            return false;
        }
        if (OsUtils.getSecureInt(context, "fp_go_back", 0) != 0) {
            z = true;
        }
        return z;
    }

    private void makeSureUserHasMultiFinger(final Context context) {
        if (System.getIntForUser(context.getContentResolver(), "fingerprint_alipay_dialog", 1, OsUtils.getCurrentUser()) == 1 && getFingerprintListSize(context) == 1) {
            GlobalContext.getBackgroundHandler().postDelayed(new Runnable() {
                public void run() {
                    FingerprintNavigator.this.showDialog(context);
                }
            }, 700);
        }
    }

    public int getFingerprintListSize(Context context) {
        List<Fingerprint> mFingerprints = new ArrayList();
        this.mFpm = (FingerprintManager) context.getSystemService("fingerprint");
        if (this.mFpm != null) {
            mFingerprints = this.mFpm.getEnrolledFingerprints(OsUtils.getCurrentUser());
            if (mFingerprints == null) {
                return 0;
            }
        }
        return mFingerprints.size();
    }

    private void showDialog(final Context context) {
        ContextThemeWrapper hwThemeContext = MagazineUtils.getHwThemeContext(context, "androidhwext:style/Theme.Emui.Dialog.Alert");
        View confirmLayout = View.inflate(hwThemeContext, R$layout.fingerprint_alipay_settings, null);
        if (confirmLayout == null) {
            HwLog.w("FPNav", "The confirmLayout is invalid!");
            return;
        }
        final CheckBox checkBox = (CheckBox) confirmLayout.findViewById(R$id.confirm_checkbox);
        if (checkBox == null) {
            HwLog.w("FPNav", "The checkBox is invalid!");
            return;
        }
        OnClickListener listener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (checkBox.isChecked()) {
                    System.putIntForUser(context.getContentResolver(), "fingerprint_alipay_dialog", 0, OsUtils.getCurrentUser());
                }
                if (which == -1) {
                    FingerprintNavigator.this.startFingerprintManagerPage(context);
                } else if (which == -2) {
                    dialog.dismiss();
                }
            }
        };
        this.mAlipayAlertDialog = new Builder(hwThemeContext).setTitle(R$string.launch_app_fingerprint_unlock_settings_title).setMessage(R$string.launch_app_fingerprint_hint_for_hwpay).setView(confirmLayout).setPositiveButton(R$string.magazine_info_settings, listener).setNegativeButton(R$string.emui30_update_magazine_btn_cancel, listener).setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (FingerprintNavigator.this.mRegisterReceiver) {
                    context.unregisterReceiver(FingerprintNavigator.this.closeDialogReceiver);
                    FingerprintNavigator.this.mRegisterReceiver = false;
                }
                FingerprintNavigator.this.mAlipayAlertDialog = null;
            }
        }).create();
        this.mAlipayAlertDialog.getWindow().setType(2009);
        this.mAlipayAlertDialog.setCanceledOnTouchOutside(false);
        this.mAlipayAlertDialog.show();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        filter.addAction("android.intent.action.SCREEN_OFF");
        context.registerReceiver(this.closeDialogReceiver, filter);
        this.mRegisterReceiver = true;
    }

    private void startFingerprintManagerPage(Context context) {
        ComponentName component = new ComponentName("com.android.settings", "com.android.settings.fingerprint.FingerprintSettingsActivity");
        Intent intent = new Intent();
        intent.setComponent(component);
        intent.setFlags(335544320);
        OsUtils.startUserActivity(context, intent);
    }

    private void reportBD(Context context, String target) {
        HwLockScreenReporter.report(context, 176, BuildConfig.FLAVOR);
        HwLog.i("FPNav", "Finger extended for: " + target);
    }
}
