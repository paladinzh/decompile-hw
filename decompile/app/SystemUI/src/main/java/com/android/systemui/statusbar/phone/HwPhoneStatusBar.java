package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.LayoutAnimationController;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.emui.blur.WindowBlurView;
import com.android.emui.blur.WindowBlurView.BlurStateListener;
import com.android.systemui.FloatTipsService;
import com.android.systemui.R;
import com.android.systemui.battery.HwBatteryManager;
import com.android.systemui.compat.ActivityInfoWrapper;
import com.android.systemui.linkplus.RoamPlus;
import com.android.systemui.observer.SystemUIObserver;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.customize.QSCustomizer;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.screenshot.HwScreenshotNotifications;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.stackdivider.WindowManagerProxy;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.NotificationData.Entry;
import com.android.systemui.statusbar.NotificationUserManager;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.PowerModeController;
import com.android.systemui.statusbar.policy.PowerModeController.CallBack;
import com.android.systemui.tint.TintManager;
import com.android.systemui.traffic.TrafficPanelManager;
import com.android.systemui.traffic.TrafficPanelViewContent;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.UserSwitchUtils;
import com.android.systemui.utils.analyze.BDReporter;
import com.android.systemui.utils.analyze.MonitorReporter;
import com.huawei.keyguard.data.KeyguardInfo;
import com.huawei.keyguard.inf.HwKeyguardPolicy;
import com.huawei.keyguard.util.MusicUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class HwPhoneStatusBar extends PhoneStatusBar implements HwPhoneStatusBarItf, CallBack {
    private static HwPhoneStatusBar mInstance;
    private boolean mBackgroundState = false;
    private HwBatteryManager mBatteryManager;
    private WindowBlurView mBlurView;
    private BackgrounCallingLayout mCallLinearLayout;
    private View mCoverStatusBarView;
    private ImageView mDelete;
    private BroadcastReceiver mFringPrintKeyEventReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean isMmiRunning = SystemProperties.get("runtime.mmitest.isrunning", "false").equals("true");
            boolean isFactoryMode = SystemProperties.get("ro.runmode", "normal").equals("factory");
            HwLog.i("HwPhoneStatusBar", "mFringPrintKeyEventReceiver::isMmiRunning = " + isMmiRunning + ", isFactoryMode = " + isFactoryMode);
            if (intent != null && intent.getAction() != null && !isMmiRunning && !isFactoryMode) {
                if ("com.android.huawei.FINGER_PRINT_ACTION_KEYEVENT".equals(intent.getAction())) {
                    Bundle bundle = intent.getExtras();
                    boolean isNeverShowEnable = false;
                    try {
                        isNeverShowEnable = System.getIntForUser(HwPhoneStatusBar.this.mContext.getContentResolver(), "systemui_tips_already_shown", UserSwitchUtils.getCurrentUser()) == 1;
                    } catch (SettingNotFoundException e) {
                        HwLog.i("HwPhoneStatusBar", "get int from settings db error " + e.getMessage());
                    }
                    if (!(bundle == null || isNeverShowEnable)) {
                        int keycode = bundle.getInt("keycode", -1);
                        Intent tipsIntent = new Intent(HwPhoneStatusBar.this.mContext, FloatTipsService.class);
                        tipsIntent.putExtra("keycode", keycode);
                        HwPhoneStatusBar.this.mContext.startServiceAsUser(tipsIntent, UserHandle.of(UserSwitchUtils.getCurrentUser()));
                    }
                }
            }
        }
    };
    private boolean mHideDropViewStatus = false;
    private boolean mIsPanelFullCollapsed = true;
    private boolean mIsPanelFullExpanded = false;
    private Configuration mLastConfig = new Configuration();
    private int mLastOrientation = -1;
    private BroadcastReceiver mNavigationBarChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                String action = intent.getAction();
                if ("com.huawei.navigationbar.statuschange".equals(action)) {
                    boolean minNavigationBar = intent.getBooleanExtra("minNavigationBar", false);
                    HwLog.i("HwPhoneStatusBar", "onReceive::action=" + action + ", minNavigationBar=" + minNavigationBar);
                    if (minNavigationBar || !((HwNavigationBarView) HwPhoneStatusBar.this.mNavigationBarView).isHideNavibar()) {
                        ((HwNavigationBarView) HwPhoneStatusBar.this.mNavigationBarView).updateNavigationBar(minNavigationBar);
                    } else {
                        HwLog.i("HwPhoneStatusBar", "NavigationBar should be hidden.");
                    }
                }
            }
        }
    };
    protected TextView mNotificationToast;
    private BroadcastReceiver mNotifyStateChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                String action = intent.getAction();
                if ("InCallScreenIsForegroundActivity".equals(action)) {
                    if (intent.getBooleanExtra("IsForegroundActivity", true)) {
                        HwPhoneStatusBar.this.mBackgroundState = false;
                        HwPhoneStatusBar.this.updateStatusBar();
                    } else if (HwPhoneStatusBar.this.mPowerManager.isInteractive() || HwPhoneStatusBar.this.mBackgroundState) {
                        HwPhoneStatusBar.this.mBackgroundState = true;
                        HwPhoneStatusBar.this.updateStatusBar();
                    } else {
                        HwLog.w("HwPhoneStatusBar", "mNotifyStateChangedReceiver do nothing");
                    }
                    HwLog.i("HwPhoneStatusBar", "mNotifyStateChangedReceiver::onReceive:action=" + action + ", mBackgroundState=" + HwPhoneStatusBar.this.mBackgroundState);
                }
            }
        }
    };
    private PowerModeController mPowerModeController = null;
    private boolean mRemoteInputing = false;
    private int mScreenHeight = 1920;
    HwScreenshotNotifications mScreenShotNotification = null;
    private int mScreenWidth = 1080;
    private boolean mShowMirror = false;
    private boolean mShowNotificationInLock = false;
    private ArrayList<View> mStackChilds = new ArrayList();
    private LayoutAnimationController mStackLayoutTransition;
    private long mStartKeyguardTime = Long.MAX_VALUE;
    private long mStartLockTime = Long.MAX_VALUE;
    private TrafficPanelViewContent mTrafficPanelView;
    private BroadcastReceiver mfloatTipsNotificationReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean tipsAlreadyShow = false;
            if (!SystemUiUtil.NAVBAR_REMOVABLE || intent == null || intent.getAction() == null) {
                HwLog.i("HwPhoneStatusBar", "FloatTipsNotificationReceiver : NAVBAR_REMOVABLE = " + SystemUiUtil.NAVBAR_REMOVABLE + "; intent = " + intent);
                return;
            }
            String flag = System.getStringForUser(HwPhoneStatusBar.this.mContext.getContentResolver(), "float_tips_notification", UserSwitchUtils.getCurrentUser());
            if ("state_send".equals(flag)) {
                HwLog.i("HwPhoneStatusBar", "Stop Timer; flag = " + flag);
                return;
            }
            if (System.getIntForUser(HwPhoneStatusBar.this.mContext.getContentResolver(), "systemui_tips_already_shown", 0, UserSwitchUtils.getCurrentUser()) != 0) {
                tipsAlreadyShow = true;
            }
            if (tipsAlreadyShow) {
                System.putStringForUser(HwPhoneStatusBar.this.mContext.getContentResolver(), "float_tips_notification", "state_send", UserSwitchUtils.getCurrentUser());
                HwLog.i("HwPhoneStatusBar", "Tips already show! tipsAlreadyShow = " + tipsAlreadyShow);
                return;
            }
            if ("com.android.huawei.FLOAT_TIPS_SERVICE_NOTIFICATION".equals(intent.getAction())) {
                String value = intent.getStringExtra("state");
                HwLog.i("HwPhoneStatusBar", "onReceive: tipsAlreadyShow=" + tipsAlreadyShow + "; flag = " + flag + "; value=" + value);
                if ("state_one".equals(value)) {
                    HwPhoneStatusBar.this.timingSendBroadcast(System.currentTimeMillis() + 259200000, "state_three");
                    HwPhoneStatusBar.this.sendFloatTipsNotification();
                } else if ("state_three".equals(value)) {
                    HwPhoneStatusBar.this.timingSendBroadcast(System.currentTimeMillis() + 345600000, "state_seven");
                    HwPhoneStatusBar.this.sendFloatTipsNotification();
                } else if ("state_seven".equals(value)) {
                    HwPhoneStatusBar.this.sendFloatTipsNotification();
                    System.putStringForUser(HwPhoneStatusBar.this.mContext.getContentResolver(), "float_tips_notification", "state_send", UserSwitchUtils.getCurrentUser());
                    HwLog.i("HwPhoneStatusBar", "Stop Timer,Changge SettingsDB float_tips_notification:state_send");
                }
            }
        }
    };

    private boolean transactToStatusBarManager(java.lang.String r9, java.lang.String r10, int r11, int r12) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x005d in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r8 = this;
        r7 = 0;
        r1 = android.os.Parcel.obtain();
        r4 = android.os.Parcel.obtain();
        r5 = r8.mBarService;	 Catch:{ RemoteException -> 0x004e, Exception -> 0x0039, all -> 0x0063 }
        if (r5 == 0) goto L_0x002d;	 Catch:{ RemoteException -> 0x004e, Exception -> 0x0039, all -> 0x0063 }
    L_0x000d:
        r5 = r8.mBarService;	 Catch:{ RemoteException -> 0x004e, Exception -> 0x0039, all -> 0x0063 }
        r0 = r5.asBinder();	 Catch:{ RemoteException -> 0x004e, Exception -> 0x0039, all -> 0x0063 }
        if (r0 == 0) goto L_0x002d;	 Catch:{ RemoteException -> 0x004e, Exception -> 0x0039, all -> 0x0063 }
    L_0x0015:
        r5 = "com.android.internal.statusbar.IStatusBarService";	 Catch:{ RemoteException -> 0x004e, Exception -> 0x0039, all -> 0x0063 }
        r1.writeInterfaceToken(r5);	 Catch:{ RemoteException -> 0x004e, Exception -> 0x0039, all -> 0x0063 }
        r1.writeString(r9);	 Catch:{ RemoteException -> 0x004e, Exception -> 0x0039, all -> 0x0063 }
        r1.writeString(r10);	 Catch:{ RemoteException -> 0x004e, Exception -> 0x0039, all -> 0x0063 }
        r1.writeInt(r11);	 Catch:{ RemoteException -> 0x004e, Exception -> 0x0039, all -> 0x0063 }
        r1.writeInt(r12);	 Catch:{ RemoteException -> 0x004e, Exception -> 0x0039, all -> 0x0063 }
        r5 = 105; // 0x69 float:1.47E-43 double:5.2E-322;	 Catch:{ RemoteException -> 0x004e, Exception -> 0x0039, all -> 0x0063 }
        r6 = 0;	 Catch:{ RemoteException -> 0x004e, Exception -> 0x0039, all -> 0x0063 }
        r0.transact(r5, r1, r4, r6);	 Catch:{ RemoteException -> 0x004e, Exception -> 0x0039, all -> 0x0063 }
    L_0x002d:
        if (r1 == 0) goto L_0x0032;
    L_0x002f:
        r1.recycle();
    L_0x0032:
        if (r4 == 0) goto L_0x0037;
    L_0x0034:
        r4.recycle();
    L_0x0037:
        r5 = 1;
        return r5;
    L_0x0039:
        r3 = move-exception;
        r5 = "HwPhoneStatusBar";	 Catch:{ RemoteException -> 0x004e, Exception -> 0x0039, all -> 0x0063 }
        r6 = "remote exception.";	 Catch:{ RemoteException -> 0x004e, Exception -> 0x0039, all -> 0x0063 }
        android.util.Log.e(r5, r6, r3);	 Catch:{ RemoteException -> 0x004e, Exception -> 0x0039, all -> 0x0063 }
        if (r1 == 0) goto L_0x0048;
    L_0x0045:
        r1.recycle();
    L_0x0048:
        if (r4 == 0) goto L_0x004d;
    L_0x004a:
        r4.recycle();
    L_0x004d:
        return r7;
    L_0x004e:
        r2 = move-exception;
        r5 = "HwPhoneStatusBar";	 Catch:{ RemoteException -> 0x004e, Exception -> 0x0039, all -> 0x0063 }
        r6 = "remote remoteexception.";	 Catch:{ RemoteException -> 0x004e, Exception -> 0x0039, all -> 0x0063 }
        android.util.Log.e(r5, r6, r2);	 Catch:{ RemoteException -> 0x004e, Exception -> 0x0039, all -> 0x0063 }
        if (r1 == 0) goto L_0x005d;
    L_0x005a:
        r1.recycle();
    L_0x005d:
        if (r4 == 0) goto L_0x0062;
    L_0x005f:
        r4.recycle();
    L_0x0062:
        return r7;
    L_0x0063:
        r5 = move-exception;
        if (r1 == 0) goto L_0x0069;
    L_0x0066:
        r1.recycle();
    L_0x0069:
        if (r4 == 0) goto L_0x006e;
    L_0x006b:
        r4.recycle();
    L_0x006e:
        throw r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.HwPhoneStatusBar.transactToStatusBarManager(java.lang.String, java.lang.String, int, int):boolean");
    }

    public boolean showStatusBar() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0075 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r8 = this;
        r7 = 0;
        r4 = r8.isStatusBarHidden();
        if (r4 != 0) goto L_0x0010;
    L_0x0007:
        r4 = "HwPhoneStatusBar";
        r5 = "Can not use it if is statusbar is not Hidden";
        com.android.systemui.utils.HwLog.i(r4, r5);
    L_0x0010:
        r0 = android.os.Parcel.obtain();
        r3 = android.os.Parcel.obtain();
        r4 = "HwPhoneStatusBar";	 Catch:{ RemoteException -> 0x005f, Exception -> 0x0043, all -> 0x007b }
        r5 = "showStatusBar";	 Catch:{ RemoteException -> 0x005f, Exception -> 0x0043, all -> 0x007b }
        android.util.Log.i(r4, r5);	 Catch:{ RemoteException -> 0x005f, Exception -> 0x0043, all -> 0x007b }
        r4 = "android.view.IWindowManager";	 Catch:{ RemoteException -> 0x005f, Exception -> 0x0043, all -> 0x007b }
        r0.writeInterfaceToken(r4);	 Catch:{ RemoteException -> 0x005f, Exception -> 0x0043, all -> 0x007b }
        r4 = r8.mWindowManagerService;	 Catch:{ RemoteException -> 0x005f, Exception -> 0x0043, all -> 0x007b }
        if (r4 == 0) goto L_0x0037;	 Catch:{ RemoteException -> 0x005f, Exception -> 0x0043, all -> 0x007b }
    L_0x002b:
        r4 = r8.mWindowManagerService;	 Catch:{ RemoteException -> 0x005f, Exception -> 0x0043, all -> 0x007b }
        r4 = r4.asBinder();	 Catch:{ RemoteException -> 0x005f, Exception -> 0x0043, all -> 0x007b }
        r5 = 205; // 0xcd float:2.87E-43 double:1.013E-321;	 Catch:{ RemoteException -> 0x005f, Exception -> 0x0043, all -> 0x007b }
        r6 = 0;	 Catch:{ RemoteException -> 0x005f, Exception -> 0x0043, all -> 0x007b }
        r4.transact(r5, r0, r3, r6);	 Catch:{ RemoteException -> 0x005f, Exception -> 0x0043, all -> 0x007b }
    L_0x0037:
        if (r0 == 0) goto L_0x003c;
    L_0x0039:
        r0.recycle();
    L_0x003c:
        if (r3 == 0) goto L_0x0041;
    L_0x003e:
        r3.recycle();
    L_0x0041:
        r4 = 1;
        return r4;
    L_0x0043:
        r2 = move-exception;
        r4 = "HwPhoneStatusBar";	 Catch:{ RemoteException -> 0x005f, Exception -> 0x0043, all -> 0x007b }
        r5 = "remote exception.";	 Catch:{ RemoteException -> 0x005f, Exception -> 0x0043, all -> 0x007b }
        android.util.Log.e(r4, r5);	 Catch:{ RemoteException -> 0x005f, Exception -> 0x0043, all -> 0x007b }
        r4 = r2.getMessage();	 Catch:{ RemoteException -> 0x005f, Exception -> 0x0043, all -> 0x007b }
        r8.triggerStatusBarShowError(r4);	 Catch:{ RemoteException -> 0x005f, Exception -> 0x0043, all -> 0x007b }
        if (r0 == 0) goto L_0x0059;
    L_0x0056:
        r0.recycle();
    L_0x0059:
        if (r3 == 0) goto L_0x005e;
    L_0x005b:
        r3.recycle();
    L_0x005e:
        return r7;
    L_0x005f:
        r1 = move-exception;
        r4 = "HwPhoneStatusBar";	 Catch:{ RemoteException -> 0x005f, Exception -> 0x0043, all -> 0x007b }
        r5 = "remote remoteexception.";	 Catch:{ RemoteException -> 0x005f, Exception -> 0x0043, all -> 0x007b }
        android.util.Log.e(r4, r5);	 Catch:{ RemoteException -> 0x005f, Exception -> 0x0043, all -> 0x007b }
        r4 = r1.getMessage();	 Catch:{ RemoteException -> 0x005f, Exception -> 0x0043, all -> 0x007b }
        r8.triggerStatusBarShowError(r4);	 Catch:{ RemoteException -> 0x005f, Exception -> 0x0043, all -> 0x007b }
        if (r0 == 0) goto L_0x0075;
    L_0x0072:
        r0.recycle();
    L_0x0075:
        if (r3 == 0) goto L_0x007a;
    L_0x0077:
        r3.recycle();
    L_0x007a:
        return r7;
    L_0x007b:
        r4 = move-exception;
        if (r0 == 0) goto L_0x0081;
    L_0x007e:
        r0.recycle();
    L_0x0081:
        if (r3 == 0) goto L_0x0086;
    L_0x0083:
        r3.recycle();
    L_0x0086:
        throw r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.HwPhoneStatusBar.showStatusBar():boolean");
    }

    public HwPhoneStatusBar() {
        setInstance(this);
    }

    public static HwPhoneStatusBar getInstance() {
        return mInstance;
    }

    private static void setInstance(HwPhoneStatusBar instance) {
        mInstance = instance;
        HwKeyguardPolicy.getInst().setPhoneStatusBar(mInstance);
    }

    public boolean canOpenFlashLight() {
        if (this.mBatteryManager != null) {
            return this.mBatteryManager.canOpenFlashLight();
        }
        return true;
    }

    public void start() {
        HwLog.i("HwPhoneStatusBar", "start called");
        super.start();
        initView();
        initTrafficPanelView();
        initController();
        registerReceivers();
        RoamPlus.getInstance(this.mContext).register();
        this.mBatteryManager = new HwBatteryManager(this.mBatteryController, this.mPowerModeController, this.mStatusBarWindow);
        this.mDelete = (ImageView) this.mNotificationPanel.findViewById(R.id.delete);
        this.mDelete.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BDReporter.c(HwPhoneStatusBar.this.mContext, 345);
                HwPhoneStatusBar.this.clearAllNotifications();
            }
        });
        this.mScreenShotNotification = new HwScreenshotNotifications(this.mContext);
        this.mScreenShotNotification.register(this.mContext);
        this.mStatusBarWindow.initKeyguardDrager();
        this.mShowNotificationInLock = this.mContext.getResources().getBoolean(R.bool.show_notification_in_lock);
        initNavigationBar();
    }

    private void initNavigationBar() {
        if (SystemUiUtil.isChinaArea()) {
            HwLog.i("HwPhoneStatusBar", "China area don't need this notice");
            return;
        }
        boolean enableNavBar = ((Boolean) SystemUIObserver.get(18)).booleanValue();
        HwLog.i("HwPhoneStatusBar", "is Front fingerprint phone :" + SystemUiUtil.NAVBAR_REMOVABLE + "; To show NavBar :" + enableNavBar);
        if (SystemUiUtil.NAVBAR_REMOVABLE) {
            if (enableNavBar) {
                timmingFloatTipsNotification();
            } else {
                boolean tipsChangedNavigation;
                if (System.getIntForUser(this.mContext.getContentResolver(), "tips_changed_navbar", 0, UserSwitchUtils.getCurrentUser()) != 0) {
                    tipsChangedNavigation = true;
                } else {
                    tipsChangedNavigation = false;
                }
                if (tipsChangedNavigation) {
                    System.putIntForUser(this.mContext.getContentResolver(), "enable_navbar", 1, UserSwitchUtils.getCurrentUser());
                    System.putIntForUser(this.mContext.getContentResolver(), "tips_changed_navbar", 0, UserSwitchUtils.getCurrentUser());
                    timmingFloatTipsNotification();
                    HwLog.i("HwPhoneStatusBar", "FloatTips has changed to show NavigationBar");
                }
            }
        }
    }

    private void timmingFloatTipsNotification() {
        String flag = System.getStringForUser(this.mContext.getContentResolver(), "float_tips_notification", UserSwitchUtils.getCurrentUser());
        if ("state_send".equals(flag)) {
            HwLog.i("HwPhoneStatusBar", "Stop Timer; flag = " + flag);
            return;
        }
        long currentTime = System.currentTimeMillis();
        if (flag == null || "state_one".equals(flag)) {
            timingSendBroadcast(3600000 + currentTime, "state_one");
            Log.i("HwPhoneStatusBar", "Timer flag is " + flag + ",Start the first timer");
            return;
        }
        Long timingTime = Long.valueOf(System.getLongForUser(this.mContext.getContentResolver(), "float_tips_time_flag", 0, UserSwitchUtils.getCurrentUser()));
        if (timingTime.longValue() - currentTime > 0) {
            timingSendBroadcast(timingTime.longValue(), flag);
            Log.i("HwPhoneStatusBar", "The timer time is not reached.timingTime = " + timingTime + "; TimerFlag = " + flag);
        } else {
            timingSendBroadcast(currentTime, flag);
            Log.i("HwPhoneStatusBar", "The timer is Time-out.currentTime = " + currentTime + "; TimerFlag = " + flag);
        }
    }

    private void timingSendBroadcast(long atTimeInMillis, String value) {
        HwLog.i("HwPhoneStatusBar", "Start the timer; state = " + value);
        AlarmManager am = (AlarmManager) this.mContext.getSystemService("alarm");
        Intent intent = new Intent("com.android.huawei.FLOAT_TIPS_SERVICE_NOTIFICATION");
        intent.putExtra("state", value);
        am.setExact(0, atTimeInMillis, PendingIntent.getBroadcast(this.mContext, 0, intent, 268435456));
        System.putLongForUser(this.mContext.getContentResolver(), "float_tips_time_flag", atTimeInMillis, UserSwitchUtils.getCurrentUser());
        System.putStringForUser(this.mContext.getContentResolver(), "float_tips_notification", value, UserSwitchUtils.getCurrentUser());
    }

    private void sendFloatTipsNotification() {
        PendingIntent pendingIntent = PendingIntent.getService(this.mContext, 0, new Intent(this.mContext, FloatTipsService.class), 134217728);
        BigTextStyle style = new BigTextStyle();
        style.bigText(this.mContext.getString(R.string.float_notification_text));
        ((NotificationManager) this.mContext.getSystemService("notification")).notifyAsUser(null, R.drawable.ic_notify_fingerprint, new Builder(this.mContext).setTicker(this.mContext.getString(R.string.float_notification_title)).setContentTitle(this.mContext.getString(R.string.float_notification_title)).setContentText(this.mContext.getString(R.string.float_notification_text)).setWhen(System.currentTimeMillis()).setVisibility(2).setPriority(1).setSmallIcon(R.drawable.ic_notify_fingerprint).setAppName(this.mContext.getString(R.string.float_tips_notifi_name)).setStyle(style).setDefaults(-1).setContentIntent(pendingIntent).setOngoing(true).setAutoCancel(true).build(), new UserHandle(0));
    }

    @FindBugsSuppressWarnings({"SIC_INNER_SHOULD_BE_STATIC_ANON"})
    private void initTrafficPanelView() {
        this.mTrafficPanelView = (TrafficPanelViewContent) View.inflate(this.mContext, R.layout.status_bar_expanded_traffic_content, null);
        this.mTrafficPanelView.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                HwLog.i("HwPhoneStatusBar", "panelView onClick");
                TrafficPanelManager.getInstance().performOnClick();
            }
        });
        this.mStackScroller.addView(this.mTrafficPanelView, 0);
        TrafficPanelManager.getInstance().setTrafficPanelChangeListener(this.mTrafficPanelView);
    }

    public PowerModeController getPowerModeController() {
        return this.mPowerModeController;
    }

    private void initController() {
        this.mComponents.put(FlashlightController.class, this.mFlashlightController);
        this.mPowerModeController = new PowerModeController();
        this.mPowerModeController.init(this.mContext);
        this.mPowerModeController.register(this);
        TrafficPanelManager.getInstance().init(this.mContext);
    }

    public void setCoverMode(boolean isCoverAdd) {
        HwLog.i("HwPhoneStatusBar", "setCoverMode , Cover added: " + isCoverAdd);
        if (this.mStatusBarWindow == null) {
            HwLog.e("HwPhoneStatusBar", "mHwStatusBarRootView == null, and return !");
            return;
        }
        if (isCoverAdd) {
            HwLog.i("HwPhoneStatusBar", "Cover: restore bar window");
            this.mStatusBarWindow.setVisibility(4);
        } else {
            HwLog.i("HwPhoneStatusBar", "Cover: hide bar window");
            this.mStatusBarWindow.setVisibility(0);
        }
    }

    protected void prepareNavigationBarView() {
        super.prepareNavigationBarView();
        ((HwNavigationBarView) this.mNavigationBarView).updateHomeButton();
    }

    private void initView() {
        getScreenSize();
        this.mCallLinearLayout = (BackgrounCallingLayout) this.mStatusBarWindow.findViewById(R.id.call_going_frame_layout);
        this.mCallLinearLayout.setStatusBarView(this.mStatusBarView);
        this.mBlurView = (WindowBlurView) this.mStatusBarWindow.findViewById(R.id.blur);
        this.mBlurView.setBlurRadius(18);
        this.mBlurView.setScale(0.125f);
        this.mBlurView.setScreenLayer(0, 159999);
        this.mBlurView.setRrefreshDuration(0);
        this.mBlurView.setMaskDrawable(new ColorDrawable(-1291845632));
        this.mBlurView.setBlurStateListener(new BlurStateListener() {
            public void onBlurFailed() {
            }

            public void onBlurSuccess(Bitmap blurBitmap) {
                HwPhoneStatusBar.this.mQSCustomizer.setBackground(blurBitmap == null ? new ColorDrawable(-16777216) : new BitmapDrawable(HwPhoneStatusBar.this.mContext.getResources(), blurBitmap));
                HwPhoneStatusBar.this.mBrightnessMirrorController.setBlur(blurBitmap);
            }
        });
        this.mNotificationToast = (TextView) this.mStatusBarWindow.findViewById(R.id.hw_keyguard_notification_touch_toast);
    }

    private void registerReceivers() {
        IntentFilter navigationFilter = new IntentFilter();
        navigationFilter.addAction("com.huawei.navigationbar.statuschange");
        this.mContext.registerReceiverAsUser(this.mNavigationBarChangedReceiver, UserHandle.ALL, navigationFilter, "android.permission.REBOOT", null);
        IntentFilter notifyStateFilter = new IntentFilter();
        notifyStateFilter.addAction("InCallScreenIsForegroundActivity");
        this.mContext.registerReceiverAsUser(this.mNotifyStateChangedReceiver, UserHandle.ALL, notifyStateFilter, "com.android.systemui.permission.BackgrounCallingLayout", null);
        IntentFilter fringPrintFilter = new IntentFilter();
        fringPrintFilter.addAction("com.android.huawei.FINGER_PRINT_ACTION_KEYEVENT");
        this.mContext.registerReceiverAsUser(this.mFringPrintKeyEventReceiver, UserHandle.ALL, fringPrintFilter, "android.permission.STATUS_BAR", null);
        IntentFilter floatTipsNotification = new IntentFilter();
        floatTipsNotification.addAction("com.android.huawei.FLOAT_TIPS_SERVICE_NOTIFICATION");
        this.mContext.registerReceiverAsUser(this.mfloatTipsNotificationReceiver, UserHandle.ALL, floatTipsNotification, "android.permission.STATUS_BAR", null);
        if (this.mCust != null) {
            this.mCust.registerReceivers(this.mQSPanel);
        }
        TintManager.getInstance().registerBroadcast(this.mContext);
    }

    private void unregisterReceivers() {
        if (this.mNavigationBarChangedReceiver != null) {
            this.mContext.unregisterReceiver(this.mNavigationBarChangedReceiver);
            this.mNavigationBarChangedReceiver = null;
        }
        if (this.mNotifyStateChangedReceiver != null) {
            this.mContext.unregisterReceiver(this.mNotifyStateChangedReceiver);
            this.mNotifyStateChangedReceiver = null;
        }
        if (this.mFringPrintKeyEventReceiver != null) {
            this.mContext.unregisterReceiver(this.mFringPrintKeyEventReceiver);
            this.mFringPrintKeyEventReceiver = null;
        }
        if (this.mfloatTipsNotificationReceiver != null) {
            this.mContext.unregisterReceiver(this.mfloatTipsNotificationReceiver);
            this.mfloatTipsNotificationReceiver = null;
        }
        if (this.mCust != null) {
            this.mCust.unregisterReceivers();
        }
        TintManager.getInstance().unRegisterBroadcast(this.mContext);
    }

    protected int getMaxKeyguardNotifications(boolean recompute) {
        int maxSportMusicNotifications = HwKeyguardPolicy.getInst().getMaxKeyguardSportMusicNotifications();
        if (maxSportMusicNotifications != -1) {
            return maxSportMusicNotifications;
        }
        return super.getMaxKeyguardNotifications(recompute);
    }

    public void destroy() {
        TrafficPanelManager.getInstance().destory();
        unregisterReceivers();
        RoamPlus.getInstance(this.mContext).unRegister();
        if (this.mScreenShotNotification != null) {
            this.mScreenShotNotification.unregister(this.mContext);
        }
        if (this.mPowerModeController != null) {
            this.mPowerModeController.release();
        }
    }

    public void onHideButtonClick() {
        if ((this.mSystemUiVisibility & 6144) == 0 || (this.mSystemUiVisibility & 2) == 0) {
            requestUpdateNavigationBar(true);
        } else {
            cancelAutohide();
            this.mHandler.post(this.mAutohide);
        }
        BDReporter.c(this.mContext, 28);
    }

    public void requestUpdateNavigationBar(boolean minNaviBar) {
        HwLog.i("HwPhoneStatusBar", "requestUpdateNavigationBar:" + minNaviBar);
        Intent intent = new Intent("com.huawei.navigationbar.statuschange");
        intent.putExtra("minNavigationBar", minNaviBar);
        try {
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM");
        } catch (Exception e) {
            HwLog.e("HwPhoneStatusBar", "not allowed to send broadcast com.huawei.navigationbar.statuschange");
        }
        SystemUiUtil.setNavigationBarHiddenbyButton(true);
    }

    public void onAllPanelsCollapsed() {
        this.mIsPanelFullExpanded = false;
        this.mIsPanelFullCollapsed = true;
        ((HwNavigationBarView) this.mNavigationBarView).updateExpandButton();
        onHideRemoteInput();
        this.mQSCustomizer.hide(0, 0);
        refreshPanelView();
    }

    public void onPanelFullyOpened() {
        this.mIsPanelFullExpanded = true;
        this.mIsPanelFullCollapsed = false;
        ((HwNavigationBarView) this.mNavigationBarView).updateExpandButton();
    }

    public boolean isPanelFullExpanded() {
        return this.mIsPanelFullExpanded;
    }

    public boolean isPanelFullCollapsed() {
        return this.mIsPanelFullCollapsed;
    }

    public boolean isStatusBarHidden() {
        if (this.mStatusBarWindowState != 2) {
            return false;
        }
        return true;
    }

    private void triggerStatusBarShowError(String errorMsg) {
        MonitorReporter.doMonitor(MonitorReporter.createInfoIntent(907033004, MonitorReporter.createMapInfo((short) 0, errorMsg)));
    }

    public WindowManager getWindowManager() {
        return this.mWindowManager;
    }

    public boolean hasSearchPanel() {
        return this.mAssistManager.hasSearchPanel();
    }

    public void updateStatusBar() {
    }

    public BackgrounCallingLayout getCallingLayout() {
        return this.mCallLinearLayout;
    }

    public KeyguardStatusBarView getKeyguardStatusBarView() {
        return this.mKeyguardStatusBar;
    }

    public View getNotificationPanelView() {
        return this.mNotificationPanel;
    }

    protected void updateKeyguardState(boolean goingToFullShade, boolean fromShadeLocked) {
        int oldState = this.mState;
        super.updateKeyguardState(goingToFullShade, fromShadeLocked);
        HwKeyguardPolicy.getInst().updateKeyguardState(oldState, this.mState, goingToFullShade, fromShadeLocked);
    }

    public void updateMediaMetaData(boolean metaDataChanged, boolean allowEnterAnimation) {
        if (!HwKeyguardPolicy.getInst().updateMediaMetaData(metaDataChanged, allowEnterAnimation)) {
            super.updateMediaMetaData(metaDataChanged, allowEnterAnimation);
        }
    }

    public void onCameraLaunchGestureDetected(int source) {
        if (HwKeyguardPolicy.getInst().isSupportCameraGesture()) {
            super.onCameraLaunchGestureDetected(source);
        }
    }

    protected void createIconController() {
        this.mCoverStatusBarView = View.inflate(this.mContext, R.layout.cover_status_bar, null);
        this.mIconController = new HwStatusBarIconController(this.mContext, this.mStatusBarView, this.mKeyguardStatusBar, this);
    }

    public void onShowRemoteInput(ExpandableNotificationRow row, View clickedView) {
        super.onShowRemoteInput(row, clickedView);
        this.mRemoteInputing = true;
        for (int i = 0; i < this.mStackScroller.getChildCount(); i++) {
            View child = this.mStackScroller.getChildAt(i);
            if (child != row && child != row.getNotificationParent()) {
                child.setVisibility(8);
            } else if (child == row.getNotificationParent()) {
                List<ExpandableNotificationRow> sibs = row.getNotificationParent().getNotificationChildren();
                if (sibs != null) {
                    for (ExpandableNotificationRow sib : new ArrayList(sibs)) {
                        if (sib != row) {
                            row.getNotificationParent().removeChildNotification(sib);
                        }
                    }
                }
            }
        }
        updateClearAll();
    }

    public void onHideRemoteInput() {
        this.mRemoteInputing = false;
        updateNotifications();
        TrafficPanelManager.getInstance().showTraffic();
    }

    protected void updateNotifications() {
        if (!this.mRemoteInputing && this.mPendingRemoteInputView == null) {
            super.updateNotifications();
            KeyguardInfo.getInst(this.mContext).setKeyguardNotificationSize(this.mNotificationData.getActiveNotifications().size());
        }
    }

    public boolean isRemoteInputing() {
        return this.mRemoteInputing;
    }

    public void updateClearAll() {
        int i = 0;
        if (this.mDelete != null && this.mNotificationPanel != null && this.mQSCustomizer != null) {
            boolean showDismissView = (this.mState != 0 || !this.mNotificationData.hasActiveClearableNotifications() || this.mQSCustomizer.isShown() || this.mRemoteInputing) ? false : !this.mNotificationPanel.isPanelVisibleBecauseOfHeadsUp();
            View view = this.mDelete;
            if (!showDismissView) {
                i = 8;
            }
            SystemUiUtil.setViewVisibility(view, i);
        }
    }

    public void goToFullShade() {
        if (this.mDelete != null) {
            this.mDelete.setVisibility(8);
            HwLog.i("HwPhoneStatusBar", "show delete: false, go to full shade");
        }
    }

    public void onQsExpanded(boolean expanded) {
        if (this.mDelete != null) {
            updateClearAll();
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getScreenSize();
        this.mQSCustomizer = (QSCustomizer) this.mNotificationPanel.findViewById(R.id.qs_customize);
        if (newConfig.orientation != this.mLastOrientation) {
            TintManager.getInstance().updateBarBgColorWhenWallpaperChanged();
            updateLayoutParams(2 == newConfig.orientation);
        }
        if (ActivityInfoWrapper.isThemeChanged(ActivityInfoWrapper.activityInfoConfigToNative(this.mLastConfig.updateFrom(newConfig)))) {
            HwLog.i("HwPhoneStatusBar", "onConfigurationChanged::theme changed");
            onDensityOrFontScaleChanged();
            if (this.mNavigationBarView != null) {
                this.mNavigationBarView.onThemeChanged(newConfig);
            }
            if (this.mAutoContainer != null) {
                this.mAutoContainer.onThemeChanged(newConfig);
            }
        }
        this.mLastOrientation = newConfig.orientation;
        if (this.mBatteryManager != null) {
            HwLog.i("HwPhoneStatusBar", "Update the battery info string");
            this.mBatteryManager.update();
        }
        ((HwPhoneStatusBarPolicy) this.mIconPolicy).updateHeadsetIcon();
    }

    private void updateLayoutParams(boolean isLandScape) {
        if (this.mDelete == null || this.mStackScroller == null) {
            HwLog.e("HwPhoneStatusBar", "updateLayoutParams::mDelete or mStackScroller is null");
            return;
        }
        LayoutParams lp = (LayoutParams) this.mDelete.getLayoutParams();
        LayoutParams lp1 = (LayoutParams) this.mStackScroller.getLayoutParams();
        if (isLandScape) {
            int marginright = (((this.mScreenWidth - this.mContext.getResources().getDimensionPixelSize(R.dimen.notification_panel_width)) / 2) - this.mDelete.getWidth()) / 2;
            lp.gravity = 21;
            lp.setMargins(0, 0, marginright, 0);
            lp1.setMargins(lp1.leftMargin, lp1.topMargin, lp1.rightMargin, this.mContext.getResources().getDimensionPixelSize(R.dimen.close_handle_underlap_land));
        } else {
            lp.gravity = 81;
            lp.setMargins(0, 0, 0, this.mContext.getResources().getDimensionPixelSize(R.dimen.notification_delete_margin_bottom));
            lp1.setMargins(lp1.leftMargin, lp1.topMargin, lp1.rightMargin, this.mContext.getResources().getDimensionPixelSize(R.dimen.close_handle_underlap));
        }
        this.mDelete.setLayoutParams(lp);
        this.mStackScroller.setLayoutParams(lp1);
    }

    protected void onDensityOrFontScaleChanged() {
        if (!(this.mStackScroller == null || this.mTrafficPanelView == null)) {
            this.mTrafficPanelView.setVisibility(8);
            this.mStackScroller.removeView(this.mTrafficPanelView);
            initTrafficPanelView();
        }
        super.onDensityOrFontScaleChanged();
    }

    public PhoneStatusBarView getStatusBarView() {
        return this.mStatusBarView;
    }

    public void launchSplitScreenMode() {
        if (this.mRecents != null && ActivityManager.supportsMultiWindow() && ((Divider) getComponent(Divider.class)).getView().getSnapAlgorithm().isSplitScreenFeasible()) {
            toggleSplitScreenMode(271, 286);
        }
    }

    protected View bindVetoButtonClickListener(View row, StatusBarNotification n) {
        if (n.isClearable()) {
            return super.bindVetoButtonClickListener(row, n);
        }
        View vetoButton = row.findViewById(R.id.veto);
        final String _pkg = n.getPackageName();
        final String _tag = n.getTag();
        final int _id = n.getId();
        final int _userId = n.getUserId();
        final StatusBarNotification statusBarNotification = n;
        vetoButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                v.announceForAccessibility(HwPhoneStatusBar.this.mContext.getString(R.string.accessibility_notification_dismissed));
                HwLog.i("HwPhoneStatusBar", "force clear clearable notification,_pkg:" + _pkg + " , key:" + statusBarNotification.getKey());
                HwPhoneStatusBar.this.transactToStatusBarManager(_pkg, _tag, _id, _userId);
            }
        });
        vetoButton.setImportantForAccessibility(2);
        return vetoButton;
    }

    public void onSupperPowerSaveChanged(boolean supperPowerSave) {
        if (supperPowerSave) {
            clearNotificationInLimitPowerMode();
        }
    }

    public void onSaveChanged(boolean save) {
    }

    private void clearNotificationInLimitPowerMode() {
        HwLog.i("HwPhoneStatusBar", "clearNotificationInLimitPowerMode");
        synchronized (this.mNotificationData.mEntries) {
            ArrayMap<String, Entry> tempEntries = this.mNotificationData.mEntries;
        }
        if (tempEntries != null) {
            for (int i = 0; i < tempEntries.size(); i++) {
                StatusBarNotification statusBarNotification = ((Entry) tempEntries.valueAt(i)).notification;
                if (!(SystemUiUtil.isMarketPlaceSbn(statusBarNotification) || statusBarNotification == null || checkNotification(statusBarNotification))) {
                    if (statusBarNotification.isOngoing()) {
                        transactToStatusBarManager(statusBarNotification.getPackageName(), statusBarNotification.getTag(), statusBarNotification.getId(), statusBarNotification.getUserId());
                    } else {
                        try {
                            this.mBarService.onNotificationClear(statusBarNotification.getPackageName(), statusBarNotification.getTag(), statusBarNotification.getId(), statusBarNotification.getUserId());
                        } catch (RemoteException e) {
                            Log.d("HwPhoneStatusBar", "clearNotification remoteException");
                        }
                    }
                }
            }
        }
    }

    private boolean checkNotification(StatusBarNotification statusBarNotification) {
        String pkgName = statusBarNotification.getPackageName();
        if ("com.android.phone".equals(pkgName) || "com.android.mms".equals(pkgName) || "com.android.deskclock".equals(pkgName) || "com.android.calendar".equals(pkgName) || "com.android.incallui".equals(pkgName) || "com.android.phone.recorder".equals(pkgName) || "com.android.server.telecom".equals(pkgName) || "com.android.contacts".equals(pkgName)) {
            return true;
        }
        return false;
    }

    public boolean interceptMediaKey(KeyEvent event) {
        if (processDoubleTapKey(event)) {
            return true;
        }
        return super.interceptMediaKey(event);
    }

    public boolean processDoubleTapKey(KeyEvent event) {
        if (event.getKeyCode() == 501) {
            boolean clearNotification = ((Boolean) SystemUIObserver.get(12)).booleanValue();
            boolean isLandscape = SystemUiUtil.isLandscape();
            HwLog.i("HwPhoneStatusBar", "fingerprint double tap:clear=" + clearNotification + ", land=" + isLandscape);
            if (clearNotification && !isLandscape) {
                BDReporter.c(this.mContext, 360);
                clearAllNotifications();
                return true;
            }
        }
        return false;
    }

    public void showMirror() {
        HwLog.i("HwPhoneStatusBar", "show mirror");
        this.mIconController.showNotificationIconArea(false);
        this.mIconController.showSystemIconArea(false);
        this.mStackChilds.clear();
        for (int i = 0; i < this.mStackScroller.getChildCount(); i++) {
            this.mStackChilds.add(this.mStackScroller.getChildAt(i));
        }
        this.mStackLayoutTransition = this.mStackScroller.getLayoutAnimation();
        this.mStackScroller.setLayoutAnimation(null);
        this.mStackScroller.setAnimationsEnabled(false);
        this.mStackScroller.removeAllViews();
        this.mShowMirror = true;
        this.mBlurView.setProgress(0.0f);
        this.mBlurView.setVisibility(8);
        TintManager.getInstance().updateBarAlpha(1.0f);
    }

    public void hideMirror() {
        HwLog.i("HwPhoneStatusBar", "hide mirror, is expand=" + this.mExpandedVisible);
        if (this.mExpandedVisible) {
            this.mIconController.hideNotificationIconArea(false);
            this.mIconController.hideSystemIconArea(false);
        }
        for (View child : this.mStackChilds) {
            if (child.getParent() != null) {
                HwLog.e("HwPhoneStatusBar", "child has already added, tag=" + child.getTag());
            } else if (this.mStackScroller.getChildCount() <= 0) {
                this.mStackScroller.addView(child);
            } else if (child instanceof ExpandableNotificationRow) {
                this.mStackScroller.addView(child);
            } else {
                HwLog.e("HwPhoneStatusBar", "child has already expired, tag=" + child.getTag() + ", " + child);
            }
        }
        this.mStackChilds.clear();
        this.mStackScroller.setLayoutAnimation(this.mStackLayoutTransition);
        this.mStackScroller.setAnimationsEnabled(true);
        this.mShowMirror = false;
        updateNotifications();
        this.mBlurView.setProgress(1.0f);
        this.mBlurView.setVisibility(0);
        if (this.mExpandedVisible) {
            TintManager.getInstance().updateBarAlpha(0.0f);
        }
    }

    protected void updateNotificationShade() {
        HwLog.i("HwPhoneStatusBar", "updateNotificationShade");
        if (this.mShowMirror) {
            HwLog.i("HwPhoneStatusBar", "show mirror, ingnore updateNotificationShade");
        } else {
            super.updateNotificationShade();
        }
    }

    public void setBarState(int state) {
        boolean z;
        HwLog.i("HwPhoneStatusBar", "setBarState:" + state);
        boolean isKeyguardState = false;
        if (this.mState != state) {
            HwLog.i("HwPhoneStatusBar", "change state: " + this.mState + " --> " + state + ", StartLockTime=" + this.mStartLockTime);
            if (state == 1 || state == 2) {
                if (this.mStartLockTime == Long.MAX_VALUE) {
                    isKeyguardState = true;
                    this.mStartLockTime = System.currentTimeMillis();
                    HwLog.i("HwPhoneStatusBar", "enter lock mode:" + this.mStartLockTime);
                    TrafficPanelManager.getInstance().hideTraffic();
                    updateNotifications();
                    TintManager.getInstance().setScreenLocked(true);
                }
                if (this.mRemoteInputController != null) {
                    this.mRemoteInputController.closeRemoteInputs();
                }
            } else if (state == 0) {
                isKeyguardState = false;
                this.mStartLockTime = Long.MAX_VALUE;
                HwLog.i("HwPhoneStatusBar", "exit lock mode:" + this.mStartLockTime);
                TintManager.getInstance().setScreenLocked(false);
                if (this.mFingerprintUnlockController.isInfastScreenMode()) {
                    this.mHandler.postDelayed(new Runnable() {
                        public void run() {
                            TrafficPanelManager.getInstance().showTraffic();
                            HwPhoneStatusBar.this.updateNotifications();
                            ((NotificationPanelView) HwPhoneStatusBar.this.getNotificationPanelView()).restoreDrawState();
                        }
                    }, 150);
                } else {
                    TrafficPanelManager.getInstance().showTraffic();
                    updateNotifications();
                    ((NotificationPanelView) getNotificationPanelView()).restoreDrawState();
                }
            }
            KeyguardInfo.getInst(this.mContext).updateNotificationOnKeyguard(isKeyguardState);
        }
        super.setBarState(state);
        BackgrounCallingLayout backgrounCallingLayout = this.mCallLinearLayout;
        if (state == 1 || state == 2) {
            z = true;
        } else {
            z = false;
        }
        backgrounCallingLayout.onKeyguardShowing(z);
    }

    public boolean showLockNotification(StatusBarNotification sbn) {
        return showLockNotification(sbn, 0);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean showLockNotification(StatusBarNotification sbn, int depth) {
        HwLog.d("HwPhoneStatusBar", "showLockNotification:state=" + this.mState + ", key=" + sbn.getKey() + ", postTime=" + sbn.getNotification().when + ", depth=" + depth + ", ShowNotificationInLock=" + this.mShowNotificationInLock);
        if (this.mState == 1 && SystemUiUtil.isCalibrationSbn(sbn)) {
            return true;
        }
        if (this.mState == 1 && SystemUiUtil.isMarketPlaceSbn(sbn)) {
            return false;
        }
        if (this.mShowNotificationInLock || SystemUiUtil.isAndroidSecureNotification(sbn)) {
            return true;
        }
        if (depth >= 3) {
            return false;
        }
        if (this.mState == 3) {
            HwLog.i("HwPhoneStatusBar", "showLockNotification: false, full screen switcher)");
            return false;
        } else if (this.mState == 0) {
            return true;
        } else {
            if (this.mState == 1 && MusicUtils.isSupportMusic(this.mContext, sbn.getPackageName())) {
                HwLog.w("HwPhoneStatusBar", "sbn.getPackageName = " + sbn.getPackageName());
                return false;
            } else if ((!SystemUiUtil.isChina() && (sbn.isOngoing() || isMedia(sbn))) || sbn.getPostTime() > this.mStartKeyguardTime - 1000) {
                return true;
            } else {
                if (this.mGroupManager.isSummaryOfGroup(sbn)) {
                    HashSet<Entry> children = this.mGroupManager.getGroupChildren(sbn);
                    if (children == null) {
                        HwLog.i("HwPhoneStatusBar", "showLockNotification: false, children == null)");
                        return false;
                    }
                    for (Entry child : children) {
                        if (showLockNotification(child.notification, depth + 1)) {
                            return true;
                        }
                    }
                }
                HwLog.d("HwPhoneStatusBar", "showLockNotification: false, key=" + sbn.getKey());
                return false;
            }
        }
    }

    public View getCoverStatusBarView() {
        return this.mCoverStatusBarView;
    }

    public void onExpandingStarted() {
        HwLog.i("HwPhoneStatusBar", "onExpandingStarted");
        TrafficPanelManager.getInstance().adjustTrafficMeal();
        updateClearAll();
        refreshUserView();
        if (SystemUiUtil.isDefaultLandOrientationProduct()) {
            updateLayoutParams(SystemUiUtil.isLandscape());
        }
    }

    public void onQsExpansionStarted() {
        HwLog.i("HwPhoneStatusBar", "onExpandingStarted");
        TrafficPanelManager.getInstance().adjustTrafficMeal();
        refreshUserView();
    }

    public void updateTraffic() {
        if (this.mState != 0) {
            TrafficPanelManager.getInstance().hideTraffic();
        }
    }

    public void updateBlurView() {
        int i = 8;
        float showHeight = ((float) this.mScreenHeight) * 0.2f;
        float showHeightDeleteView = ((float) this.mScreenHeight) * 0.4f;
        if (SystemUiUtil.isTablet(this.mContext)) {
            showHeight = ((float) this.mScreenHeight) * 0.1f;
            showHeightDeleteView = ((float) this.mScreenHeight) * 0.2f;
        }
        if (this.mBlurView != null && this.mDelete != null) {
            if (this.mState != 0) {
                if (this.mNotificationPanel.getQsExpansionFraction() > 0.0f) {
                    this.mBlurView.setShowBlur(false);
                    this.mBlurView.setProgress(this.mNotificationPanel.getQsExpansionFraction() * 0.5f);
                    SystemUiUtil.setViewVisibility(this.mBlurView, this.mShowMirror ? 8 : 0);
                } else {
                    this.mBlurView.setShowBlur(true);
                    this.mBlurView.setProgress(0.0f);
                    SystemUiUtil.setViewVisibility(this.mBlurView, 8);
                }
                if (this.mNotificationPanel.isUseGgStatusView()) {
                    if (this.mBlurView.getVisibility() == 0) {
                        this.mKeyguardStatusView.setVisibility(8);
                    } else {
                        this.mKeyguardStatusView.setVisibility(0);
                    }
                }
            } else if (this.mNotificationPanel.getExpandedHeight() > 0.0f) {
                float alpha = 1.0f;
                if (this.mNotificationPanel.getExpandedHeight() < showHeight) {
                    alpha = 0.0f;
                } else if (this.mNotificationPanel.getExpandedHeight() < showHeightDeleteView) {
                    alpha = (this.mNotificationPanel.getExpandedHeight() - showHeight) / (showHeightDeleteView - showHeight);
                }
                this.mDelete.setAlpha(alpha);
                this.mBlurView.setShowBlur(true);
                this.mBlurView.setProgress(alpha);
                View view = this.mBlurView;
                if (!this.mShowMirror) {
                    i = 0;
                }
                SystemUiUtil.setViewVisibility(view, i);
            } else {
                this.mBlurView.setShowBlur(true);
                this.mBlurView.setProgress(0.0f);
                SystemUiUtil.setViewVisibility(this.mBlurView, 8);
            }
        }
    }

    public void updateCallingLayout() {
        float f = 0.0f;
        if (getInstance().getCallingLayout() != null) {
            BackgrounCallingLayout callingLayout;
            if (this.mState != 0) {
                callingLayout = getInstance().getCallingLayout();
                if (this.mNotificationPanel.getQsExpansionFraction() <= 0.0f) {
                    f = 1.0f;
                }
                callingLayout.setAlpha(f);
            } else {
                callingLayout = getInstance().getCallingLayout();
                if (this.mNotificationPanel.getExpandedHeight() <= 0.0f) {
                    f = 1.0f;
                }
                callingLayout.setAlpha(f);
            }
        }
    }

    public BatteryController getBatteryController() {
        return this.mBatteryController;
    }

    public void toggleSplitScreenByLineGesture(final int centerX, final int centerY) {
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            int dockSide;
            boolean isTopTaskApp = false;

            public boolean runInThread() {
                this.dockSide = WindowManagerProxy.getInstance().getDockSide();
                this.isTopTaskApp = HwPhoneStatusBar.this.isTopTaskApp();
                return true;
            }

            public void runInUI() {
                if (this.dockSide == -1) {
                    if (!this.isTopTaskApp) {
                        SystemUiUtil.showToastForAllUser(HwPhoneStatusBar.this.mContext, R.string.split_app_knuckle_gesture_message);
                        return;
                    } else if (!(centerX == 0 && centerY == 0)) {
                        ((Divider) HwPhoneStatusBar.this.getComponent(Divider.class)).getView().setGestureCoordinates(centerX, centerY);
                    }
                }
                super.toggleSplitScreenMode(271, 286);
            }
        });
    }

    public boolean hideKeyguard() {
        HwLog.i("HwPhoneStatusBar", "hideKeyguard:" + this.mStatusBarKeyguardViewManager.isOccluded());
        if (!this.mStatusBarKeyguardViewManager.isOccluded()) {
            HwLog.i("HwPhoneStatusBar", "exit keyguard:" + this.mStartKeyguardTime);
            this.mStartKeyguardTime = Long.MAX_VALUE;
        }
        return super.hideKeyguard();
    }

    public void showKeyguard() {
        HwLog.i("HwPhoneStatusBar", "showKeyguard:" + this.mStatusBarKeyguardViewManager.isOccluded());
        this.mStackScroller.updateEmptyShadeView(false, false);
        super.showKeyguard();
        if (this.mStartKeyguardTime == Long.MAX_VALUE) {
            this.mStartKeyguardTime = System.currentTimeMillis();
            HwLog.i("HwPhoneStatusBar", "enter keyguard:" + this.mStartKeyguardTime);
        }
    }

    public boolean isBouncerShowing() {
        return (this.mStatusBarKeyguardViewManager.isBouncerShowing() || this.mStatusBarKeyguardViewManager.willShowBouncer()) ? true : this.mBouncerShowing;
    }

    public void onKeyguardViewManagerStatesUpdated() {
        super.onKeyguardViewManagerStatesUpdated();
        boolean show = (!this.mStatusBarKeyguardViewManager.isShowing() || this.mStatusBarKeyguardViewManager.isInDismiss()) ? false : !this.mStatusBarKeyguardViewManager.isOccluded();
        this.mNotificationPanel.updateKeyguardState(show, this.mStatusBarKeyguardViewManager.isBouncerShowing());
    }

    public boolean isFullscreenBouncer() {
        return this.mStatusBarKeyguardViewManager.isFullscreenBouncer();
    }

    public boolean isTopTaskApp() {
        SystemServicesProxy ssp = Recents.getSystemServices();
        RunningTaskInfo topTask = ssp.getRunningTask();
        boolean screenPinningActive = ssp.isScreenPinningActive();
        boolean isHomeStack = topTask != null ? SystemServicesProxy.isHomeStack(topTask.stackId) : false;
        if (topTask == null || isHomeStack || screenPinningActive) {
            return false;
        }
        return true;
    }

    public HwStatusBarKeyguardViewManager getStatusBarKeyguardViewManager() {
        return (HwStatusBarKeyguardViewManager) this.mStatusBarKeyguardViewManager;
    }

    public void refreshUserView() {
        ((HwQuickStatusBarHeader) this.mNotificationPanel.findViewById(R.id.header)).refreshUserView();
    }

    public void refreshPanelView() {
        if (this.mNotificationPanel.isQsDetailShowing()) {
            this.mNotificationPanel.closeQsDetail();
        }
    }

    public int getScreenWidth() {
        return this.mScreenWidth;
    }

    public int getScreenHeight() {
        return this.mScreenHeight;
    }

    public void getScreenSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getRealMetrics(displayMetrics);
        this.mScreenWidth = displayMetrics.widthPixels;
        this.mScreenHeight = displayMetrics.heightPixels;
    }

    public void hideDropbackView() {
        this.mHideDropViewStatus = true;
        if (this.mBackdrop != null) {
            this.mBackdrop.setVisibility(8);
        }
        if (this.mKeyguardStatusBar != null) {
            this.mKeyguardStatusBar.setVisibility(8);
        }
        if (this.mNotificationPanel != null) {
            this.mNotificationPanel.setVisibility(8);
        }
    }

    public void showDropbackView() {
        this.mHideDropViewStatus = false;
        if (this.mStatusBarKeyguardViewManager.isShowing()) {
            if (this.mBackdrop != null) {
                this.mBackdrop.setVisibility(0);
            }
            if (this.mKeyguardStatusBar != null) {
                this.mKeyguardStatusBar.setVisibility(0);
            }
            if ((!SystemProperties.getBoolean("ro.config.dismiss_hwlock", false) || this.mIsScreenOn) && this.mNotificationPanel != null) {
                this.mNotificationPanel.setVisibility(0);
            }
        }
    }

    public boolean getDropbackViewHideStatus() {
        return this.mHideDropViewStatus;
    }

    public void setBokehChangeStatus(boolean change) {
        if (this.mNotificationPanel != null) {
            this.mNotificationPanel.setBokehChangeStatus(change);
        }
    }

    public QSPanel getMQSPanel() {
        return this.mQSPanel;
    }

    public void resetBrightnessMirror() {
        if (this.mQSPanel == null || this.mBrightnessMirrorController == null) {
            HwLog.e("HwPhoneStatusBar", "resetBrightnessMirror::mQSPanel or mBrightnessMirrorController is null");
        } else {
            this.mQSPanel.setBrightnessMirror(this.mBrightnessMirrorController);
        }
    }

    protected boolean shouldPeek(Entry entry, StatusBarNotification sbn) {
        if (((Boolean) SystemUIObserver.get(21)).booleanValue()) {
            HwLog.i("HwPhoneStatusBar", "No peeking: in drive mode: notification:" + sbn.getKey());
            return false;
        }
        Notification notification = sbn.getNotification();
        boolean z = false;
        boolean z2 = false;
        if (sbn.getPackageName().equals("com.android.email")) {
            z = notification.extras.getBoolean("hw_email_vip_notification", false);
            boolean isVIPStatusbar = notification.extras.getBoolean("hw_email_vip_statusbar", false);
            z2 = notification.extras.getBoolean("hw_email_vip_headsup", false);
            HwLog.i("HwPhoneStatusBar", "shouldInterrupt:isVIP:" + z + ", isVIPStatusbar:" + isVIPStatusbar + ", isVIPHeadsup:" + z2 + ", isVIPLock:" + notification.extras.getBoolean("hw_email_vip_lockscreen", false));
        }
        if (!z || r2) {
            int allowPeek = NotificationUserManager.getInstance(this.mContext).getNotificationState(sbn.getUserId(), sbn.getPackageName(), "2");
            if (z || allowPeek != 0) {
                return super.shouldPeek(entry, sbn);
            }
            Log.d("HwPhoneStatusBar", "No peeking: not allowed notification: " + sbn.getKey());
            return false;
        }
        Log.d("HwPhoneStatusBar", "No peeking: not allowed vip notification: " + sbn.getKey());
        return false;
    }

    public boolean getFpUnlockingStatus() {
        boolean z = false;
        if (this.mFingerprintUnlockController == null) {
            return false;
        }
        if (5 == this.mFingerprintUnlockController.getMode()) {
            z = true;
        }
        return z;
    }

    public boolean getFastUnlockMode() {
        if (this.mFingerprintUnlockController != null) {
            return this.mFingerprintUnlockController.isInfastScreenMode();
        }
        return false;
    }

    public void showNotificationToast(boolean shouldShowNoDetails) {
        HwLog.i("HwPhoneStatusBar", "showNotificationToast");
        if (this.mState == 1 && this.mNotificationToast.getVisibility() != 0) {
            int[] point = new int[2];
            View firstRow = this.mStackScroller.getFirstChildNotGone();
            if (firstRow != null) {
                firstRow.getLocationOnScreen(point);
            }
            ((LayoutParams) this.mNotificationToast.getLayoutParams()).topMargin = (point[1] - this.mContext.getResources().getDimensionPixelSize(R.dimen.hw_keyguard_notification_touch_toast_bottom)) - this.mNotificationToast.getHeight();
            this.mNotificationToast.setVisibility(0);
            if (shouldShowNoDetails) {
                this.mNotificationToast.setText(this.mContext.getResources().getText(R.string.hw_keyguard_notification_touch_toast_no_details));
            } else {
                this.mNotificationToast.setText(this.mContext.getResources().getText(R.string.hw_keyguard_notification_touch_toast));
            }
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    HwPhoneStatusBar.this.mNotificationToast.setVisibility(8);
                }
            }, 1400);
        }
    }

    public void hideNotificationToast() {
        if (this.mNotificationToast != null && this.mNotificationToast.getVisibility() == 0) {
            this.mNotificationToast.setVisibility(8);
        }
    }

    public void collapsePanelViewWhenScreenShot() {
        if (1 != this.mState || this.mNotificationPanel == null) {
            animateCollapsePanels(0, true);
        } else {
            this.mNotificationPanel.collapsePanelViewWhenScreenShot();
        }
    }

    public boolean isScreenOn() {
        return this.mIsScreenOn;
    }

    public void showScreenPinningDialog(final int taskId, boolean allowCancel) {
        final SystemUIDialog dialog = new SystemUIDialog(this.mContext);
        dialog.setTitle(R.string.screen_pinning_title_new);
        dialog.setMessage(R.string.screen_pinning_description_new);
        dialog.setPositiveButton(R.string.screen_pinning_positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    ActivityManagerNative.getDefault().startSystemLockTaskMode(taskId);
                } catch (RemoteException e) {
                    HwLog.e("HwPhoneStatusBar", "showScreenPinningDialog::startSystemLockTaskMode exception=" + e);
                } catch (Exception e2) {
                    HwLog.e("HwPhoneStatusBar", "showScreenPinningDialog::startSystemLockTaskMode exception=" + e2);
                }
            }
        });
        if (allowCancel) {
            dialog.setNegativeButton(R.string.screen_pinning_negative, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialog.dismiss();
                }
            });
        }
        dialog.setCancelable(false);
        dialog.show();
    }

    protected void inflateNavigationBarView(Context context) {
        this.mNavigationBarView = (NavigationBarView) View.inflate(context, SystemUiUtil.isDefaultLandOrientationProduct() ? R.layout.navigation_bar_land : R.layout.navigation_bar, null);
    }

    public void executeRunnableDismissingKeyguard(Runnable runnable, Runnable cancelAction, boolean dismissShade, boolean afterKeyguardGone, boolean deferred) {
        if (runnable instanceof ShowEditRunner) {
            this.mStatusBarKeyguardViewManager.setFobiddenCollpaseNotification(true);
        }
        super.executeRunnableDismissingKeyguard(runnable, cancelAction, dismissShade, afterKeyguardGone, deferred);
    }

    protected void onLockedRemoteInput(ExpandableNotificationRow row, View clicked) {
        this.mStatusBarKeyguardViewManager.setFobiddenCollpaseNotification(true);
        super.onLockedRemoteInput(row, clicked);
    }

    public boolean isMedia(StatusBarNotification sbn) {
        return sbn != null ? sbn.getKey().equals(getCurrentMediaNotificationKey()) : false;
    }
}
