package com.huawei.keyguard.view.widget;

import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.R$array;
import com.android.keyguard.R$drawable;
import com.android.keyguard.R$id;
import com.android.keyguard.R$string;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.data.BatteryStateInfo;
import com.huawei.keyguard.inf.HwKeyguardPolicy;
import com.huawei.keyguard.inf.IFlashlightController;
import com.huawei.keyguard.inf.IFlashlightController.FlashlightListener;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.monitor.StateMonitor;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.HwUnlockUtils;
import com.huawei.keyguard.util.KeyguardToast;
import com.huawei.keyguard.util.OsUtils;
import com.huawei.openalliance.ad.inter.constant.EventType;
import fyusion.vislib.BuildConfig;

public class ToolBoxView extends LinearLayout implements FlashlightListener, OnClickListener {
    private InfoCenterView mInfoCenterView;
    private boolean mIsChanged = false;
    private String mLastPackageName = BuildConfig.FLAVOR;
    private long mNextClickTime = 0;

    public ToolBoxView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean ret = super.dispatchTouchEvent(ev);
        HwLog.w("ToolBoxView", "MagazineControlView dispatchTouchEvent " + ret + " " + ev.getActionMasked());
        return ret;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        setQuickEntryViewListener();
        setDescendantFocusability(262144);
    }

    private InfoCenterView getInfoCenterView() {
        for (ViewParent vp = getParent(); vp != null; vp = vp.getParent()) {
            if (vp instanceof InfoCenterView) {
                return (InfoCenterView) vp;
            }
        }
        return null;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mInfoCenterView = getInfoCenterView();
        HwUnlockUtils.resetPoint(getContext());
        IFlashlightController mController = HwKeyguardPolicy.getInst().getFlashlightController();
        if (mController != null) {
            mController.addListener(this);
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        IFlashlightController mController = HwKeyguardPolicy.getInst().getFlashlightController();
        if (mController != null) {
            mController.removeListener(this);
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean ret = super.onInterceptTouchEvent(ev);
        HwLog.w("ToolBoxView", "ToolBoxView onTouchEvent: " + ret + "  " + ev.getActionMasked());
        return ret;
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = super.onTouchEvent(event);
        HwLog.w("ToolBoxView", "ToolBoxView onTouchEvent: " + ret + "  " + event.getActionMasked());
        return ret;
    }

    private void setQuickEntryViewListener() {
        ImageView first = (ImageView) findViewById(R$id.info_center_shortcut_1);
        first.setContentDescription(getResources().getString(R$string.accessibility_keyguard_recorder));
        ImageView second = (ImageView) findViewById(R$id.info_center_shortcut_2);
        second.setContentDescription(getResources().getString(R$string.accessibility_keyguard_calculator));
        ImageView third = (ImageView) findViewById(R$id.info_center_shortcut_3);
        if (third != null) {
            third.setContentDescription(getResources().getString(R$string.accessibility_keyguard_flashlight));
        }
        ImageView forth = (ImageView) findViewById(R$id.info_center_shortcut_4);
        forth.setContentDescription(getResources().getString(R$string.accessibility_keyguard_clock));
        ImageView fifth = (ImageView) findViewById(R$id.info_center_shortcut_5);
        fifth.setContentDescription(getResources().getString(R$string.accessibility_keyguard_scan));
        ImageView firstCircle = (ImageView) findViewById(R$id.ic_circle_shortcut_1);
        firstCircle.setContentDescription(getResources().getString(R$string.accessibility_keyguard_recorder));
        ImageView secondCircle = (ImageView) findViewById(R$id.ic_circle_shortcut_2);
        secondCircle.setContentDescription(getResources().getString(R$string.accessibility_keyguard_calculator));
        ImageView thirdCircle = (ImageView) findViewById(R$id.ic_circle_shortcut_3);
        if (thirdCircle != null) {
            thirdCircle.setContentDescription(getResources().getString(R$string.accessibility_keyguard_flashlight));
        }
        ImageView forthCircle = (ImageView) findViewById(R$id.ic_circle_shortcut_4);
        forthCircle.setContentDescription(getResources().getString(R$string.accessibility_keyguard_clock));
        ImageView fifthCircle = (ImageView) findViewById(R$id.ic_circle_shortcut_5);
        fifthCircle.setContentDescription(getResources().getString(R$string.accessibility_keyguard_scan));
        setDefaultClickListener(first);
        setDefaultClickListener(second);
        setDefaultClickListener(third);
        setDefaultClickListener(forth);
        setDefaultClickListener(fifth);
        setDefaultClickListener(firstCircle);
        setDefaultClickListener(secondCircle);
        setDefaultClickListener(thirdCircle);
        setDefaultClickListener(forthCircle);
        setDefaultClickListener(fifthCircle);
        updateFlashlightView();
    }

    public void updateContentDescription() {
        ((ImageView) findViewById(R$id.info_center_shortcut_1)).setContentDescription(getResources().getString(R$string.accessibility_keyguard_recorder));
        ((ImageView) findViewById(R$id.info_center_shortcut_2)).setContentDescription(getResources().getString(R$string.accessibility_keyguard_calculator));
        ImageView third = (ImageView) findViewById(R$id.info_center_shortcut_3);
        if (third != null) {
            third.setContentDescription(getResources().getString(R$string.accessibility_keyguard_flashlight));
        }
        ((ImageView) findViewById(R$id.info_center_shortcut_4)).setContentDescription(getResources().getString(R$string.accessibility_keyguard_clock));
        ((ImageView) findViewById(R$id.info_center_shortcut_5)).setContentDescription(getResources().getString(R$string.accessibility_keyguard_scan));
        ((ImageView) findViewById(R$id.ic_circle_shortcut_1)).setContentDescription(getResources().getString(R$string.accessibility_keyguard_recorder));
        ((ImageView) findViewById(R$id.ic_circle_shortcut_2)).setContentDescription(getResources().getString(R$string.accessibility_keyguard_calculator));
        ImageView thirdCircle = (ImageView) findViewById(R$id.ic_circle_shortcut_3);
        if (thirdCircle != null) {
            thirdCircle.setContentDescription(getResources().getString(R$string.accessibility_keyguard_flashlight));
        }
        ((ImageView) findViewById(R$id.ic_circle_shortcut_4)).setContentDescription(getResources().getString(R$string.accessibility_keyguard_clock));
        ((ImageView) findViewById(R$id.ic_circle_shortcut_5)).setContentDescription(getResources().getString(R$string.accessibility_keyguard_scan));
    }

    private void setDefaultClickListener(View v) {
        if (v != null) {
            v.setOnClickListener(this);
            v.setFocusable(true);
        }
    }

    public void onClick(View v) {
        int id = v.getId();
        HwLog.i("ToolBoxView", "shortcut click event. " + id);
        if (id == R$id.info_center_shortcut_1 || id == R$id.ic_circle_shortcut_1) {
            startActivityById(0);
        } else if (id == R$id.info_center_shortcut_2 || id == R$id.ic_circle_shortcut_2) {
            startActivityById(1);
        } else if (id == R$id.info_center_shortcut_3 || id == R$id.ic_circle_shortcut_3) {
            StateMonitor.getInst().triggerEvent(501);
            startActivityById(2);
        } else if (id == R$id.info_center_shortcut_4 || id == R$id.ic_circle_shortcut_4) {
            startActivityById(3);
        } else if (id == R$id.info_center_shortcut_5 || id == R$id.ic_circle_shortcut_5) {
            startActivityById(4);
        } else if (id == R$id.change_lockscreen_style) {
            changeLockStyle();
        } else {
            HwLog.w("ToolBoxView", "unsupport view " + id);
        }
    }

    private void changeLockStyle() {
        startThemeChange(new Intent("huawei.intent.action.HUAWEI_UNLOCK_STYLE").addFlags(8388608).addFlags(268435456).addFlags(536870912).addFlags(67108864).setPackage("com.huawei.android.thememanager"));
        HwLockScreenReporter.report(this.mContext, 120, BuildConfig.FLAVOR);
    }

    private void startApp(String pkgName, String className) {
        KeyguardManager km = (KeyguardManager) getContext().getSystemService("keyguard");
        HwUnlockUtils.vibrate(getContext());
        KeyguardUpdateMonitor mUpdateMonitor = KeyguardUpdateMonitor.getInstance(getContext());
        if (mUpdateMonitor == null || mUpdateMonitor.isDeviceProvisioned()) {
            if (km.isDeviceSecure(OsUtils.getCurrentUser()) || (mUpdateMonitor != null && mUpdateMonitor.isSimPinSecure())) {
                startSecurityApp("android.security.action.START_APP_SECURE", pkgName);
            } else {
                startUnSecurityApp(pkgName, className);
            }
        }
    }

    private void dimissInfoCenter() {
        if (this.mInfoCenterView != null) {
            this.mInfoCenterView.dismiss();
            HwLog.w("ToolBoxView", "dimissInfoCenter");
            return;
        }
        HwLog.w("ToolBoxView", "dimiss skiped as InfoCenterView not found");
    }

    private void startUnSecurityApp(String pkgName, String className) {
        ComponentName component = new ComponentName(pkgName, className);
        Intent intent = new Intent();
        intent.setComponent(component);
        intent.addFlags(268435456);
        if ("com.huawei.scanner".equals(pkgName)) {
            intent.putExtra("packageName", "com.android.keyguard");
        } else if ("com.android.deskclock".equals(pkgName)) {
            intent.putExtra("deskclock.select.tab", 3);
        }
        if (this.mInfoCenterView != null) {
            this.mInfoCenterView.reset();
        }
        try {
            HwKeyguardPolicy.getInst().startActivity(intent, false);
        } catch (ActivityNotFoundException ex) {
            HwLog.w("ToolBoxView", "start activity fail, just dismiss keyguard " + ex);
        }
    }

    private void startSecurityApp(String intentAction, String pkgName) {
        Intent intent = new Intent(intentAction).addFlags(8388608).addFlags(268435456).addFlags(536870912).addFlags(67108864).addFlags(32768).setPackage(pkgName);
        if ("com.huawei.scanner".equals(pkgName)) {
            intent.putExtra("packageName", "com.android.keyguard");
        } else if ("com.android.deskclock".equals(pkgName)) {
            intent.putExtra("deskclock.select.tab", 3);
        }
        boolean restrict = (!HwKeyguardUpdateMonitor.getInstance(this.mContext).isRestrictAsEncrypt() || "com.android.calculator2".equals(pkgName) || "com.huawei.flashlight".equals(pkgName)) ? false : true;
        try {
            HwLog.d("ToolBoxView", "startSecurityApp for " + intentAction + "; " + restrict + " " + pkgName);
            if (restrict) {
                HwKeyguardPolicy.getInst().startActivity(intent, true);
            } else {
                OsUtils.startUserActivity(getContext(), intent);
            }
        } catch (ActivityNotFoundException e) {
            HwLog.e("ToolBoxView", "startSecurityApp failed, package not installed ? : " + pkgName + e.toString());
        }
    }

    private void startThemeChange(Intent intent) {
        HwUnlockUtils.vibrate(this.mContext);
        if (HwKeyguardUpdateMonitor.getInstance(this.mContext).isSecure()) {
            OsUtils.startUserActivity(this.mContext, intent);
            HwKeyguardPolicy.getInst().dismiss();
        } else {
            HwKeyguardPolicy.getInst().startActivity(intent, false);
        }
        dimissInfoCenter();
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        updateFlashlightView();
        if (hasWindowFocus && this.mIsChanged) {
            invalidate();
            this.mIsChanged = false;
        }
    }

    private void startActivityById(int index) {
        String[] s = getContext().getResources().getStringArray(R$array.infocenter_item_info);
        if (s == null || s.length <= index) {
            HwLog.e("ToolBoxView", "Index outofbound when startActivityById." + index);
            return;
        }
        String[] shortCutInfo = s[index].split("/");
        String str = shortCutInfo.length >= 1 ? shortCutInfo[0] : null;
        String str2 = shortCutInfo.length >= 2 ? shortCutInfo[0] + shortCutInfo[1] : null;
        if (str == null) {
            HwLog.e("ToolBoxView", "startActivity skip as no pkg Name. ID " + index);
            dimissInfoCenter();
        } else if (isClickedTooQuickly(str)) {
            HwLog.i("ToolBoxView", "User click too quickly");
        } else {
            this.mNextClickTime = SystemClock.uptimeMillis() + ("com.huawei.flashlight".equalsIgnoreCase(str) ? 200 : 1000);
            this.mLastPackageName = str;
            doDbReporterByPackageNameWhenEntry(str);
            if (str.equalsIgnoreCase("com.huawei.flashlight")) {
                HwUnlockUtils.vibrate(this.mContext);
                respondFlashLight();
                return;
            }
            if (str.equalsIgnoreCase("com.huawei.camera")) {
                HwUnlockUtils.vibrate(this.mContext);
                startCamera();
            } else if (str2 != null) {
                startApp(str, str2);
            }
            dimissInfoCenter();
        }
    }

    private void startCamera() {
        HwKeyguardUpdateMonitor.getInstance(this.mContext).transitionToCamera();
    }

    private boolean isClickedTooQuickly(String pkgName) {
        boolean z = false;
        if ("com.huawei.flashlight".equalsIgnoreCase(this.mLastPackageName) && !"com.huawei.flashlight".equalsIgnoreCase(pkgName)) {
            return false;
        }
        if (SystemClock.uptimeMillis() < this.mNextClickTime) {
            z = true;
        }
        return z;
    }

    private void doDbReporterByPackageNameWhenEntry(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            HwLog.w("ToolBoxView", "doDbReporterByPackageNameWhenEntry pkgName is null");
            return;
        }
        if ("com.huawei.camera".equalsIgnoreCase(pkgName)) {
            HwLockScreenReporter.report(this.mContext, 116, BuildConfig.FLAVOR);
        } else if ("com.android.soundrecorder".equalsIgnoreCase(pkgName)) {
            HwLockScreenReporter.report(this.mContext, 115, BuildConfig.FLAVOR);
        } else if ("com.android.calculator2".equalsIgnoreCase(pkgName)) {
            HwLockScreenReporter.report(this.mContext, 113, BuildConfig.FLAVOR);
        } else if ("com.android.mediacenter".equalsIgnoreCase(pkgName)) {
            HwLockScreenReporter.report(this.mContext, 123, BuildConfig.FLAVOR);
        } else if ("com.huawei.scanner".equalsIgnoreCase(pkgName)) {
            HwLockScreenReporter.report(this.mContext, 156, BuildConfig.FLAVOR);
        } else if ("com.android.deskclock".equalsIgnoreCase(pkgName)) {
            HwLockScreenReporter.report(this.mContext, 157, BuildConfig.FLAVOR);
        }
        HwLockScreenReporter.reportPicInfoAdEvent(this.mContext, EventType.SHOWEND, 1002, 0);
    }

    public void onFlashlightChanged(boolean enabled) {
        updateFlashlightView();
    }

    public void onFlashlightError() {
        updateFlashlightView();
    }

    public void onFlashlightAvailabilityChanged(boolean available) {
        updateFlashlightView();
    }

    protected void respondFlashLight() {
        if (BatteryStateInfo.getInst().isExhaustBatteryLevel()) {
            KeyguardToast.showKeyguardToast(this.mContext, this.mContext.getResources().getString(R$string.emui30_keyguard_flashlight_not_open));
            return;
        }
        IFlashlightController lightController = HwKeyguardPolicy.getInst().getFlashlightController();
        if (lightController == null) {
            HwLog.e("ToolBoxView", "Flastlight can't be turnon/off as no controller");
            return;
        }
        lightController.setFlashlight(!lightController.isEnabled());
        HwLockScreenReporter.report(this.mContext, 114, "{status: " + (!lightController.isEnabled() ? "on}" : "off}"));
    }

    private void updateFlashlightView() {
        post(new Runnable() {
            public void run() {
                ToolBoxView.this.updateFlashlightViewInner();
            }
        });
    }

    private void updateFlashlightViewInner() {
        IFlashlightController lightController = HwKeyguardPolicy.getInst().getFlashlightController();
        boolean isAvailable = lightController != null ? lightController.isAvailable() : false;
        boolean isEnabled = lightController != null ? lightController.isEnabled() : false;
        ImageView lightControlView = (ImageView) findViewById(R$id.info_center_shortcut_3);
        ImageView lightControlCicleView = (ImageView) findViewById(R$id.ic_circle_shortcut_3);
        if (lightControlView != null) {
            if (lightControlCicleView != null) {
                lightControlCicleView.setImageResource(isEnabled ? R$drawable.ic_circle_pressed : R$drawable.ic_circle);
            }
            int id = isAvailable ? isEnabled ? R$drawable.ic_unlock_flashlight_on : R$drawable.ic_unlock_flashlight : R$drawable.ic_unlock_flashlight_disable;
            lightControlView.setImageResource(id);
            StateMonitor.getInst().cancelEvent(511);
        }
    }
}
