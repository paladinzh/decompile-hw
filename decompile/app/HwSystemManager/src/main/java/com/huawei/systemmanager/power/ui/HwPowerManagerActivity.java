package com.huawei.systemmanager.power.ui;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import android.view.LayoutInflater;
import android.view.SurfaceControl;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.common.collect.Lists;
import com.huawei.android.app.ActionBarEx;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.customize.FearureConfigration;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.power.util.PowerNotificationUtils;
import com.huawei.systemmanager.power.util.SysCoreUtils;
import com.huawei.systemmanager.settingsearch.BaseSearchIndexProvider;
import com.huawei.systemmanager.settingsearch.SearchIndexableRaw;
import com.huawei.systemmanager.settingsearch.SettingSearchUtil;
import com.huawei.systemmanager.util.BlurUtils;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONObject;

public class HwPowerManagerActivity extends HsmActivity {
    private static final int BLUR_RADIUS = 18;
    public static final int CONFIG_OK = 0;
    public static final String KEY_SHOULD_EXPAND_SLIDE_VIEW = "expande";
    private static final String KILL_APP_END_ACTION = "com.huawei.systemmamanger.action.KILL_ROGAPP_END";
    private static final String KILL_APP_START_ACTION = "com.huawei.systemmamanger.action.KILL_ROGAPP_STARTED";
    private static final String[] KILL_WHITE_PCK_NAME = new String[]{"com.android.bluetooth", "com.google.android.marvin.talkback", "com.huawei.android.pushagent", "com.huawei.parentcontrol"};
    private static final int MAXLAYER = 159999;
    private static final int MINLAYER = 0;
    private static final int MSG_BEGIN_CHANGE_SCREEN = 18;
    private static final int MSG_BEGIN_FREEZE_SCREEN = 256;
    private static final int MSG_BEGIN_KILL_APP = 19;
    private static final int MSG_BEGIN_KILL_HWSYSTEMMANAGER_SELF_APP = 20;
    private static final int MSG_BEGIN_OPEN_CHANGE = 16;
    private static final int MSG_BEGIN_PROGRESS = 17;
    private static final int MSG_BEGIN_UNFREEZE_SCREEN = 257;
    private static final int MSG_CANCEL_CIRCLE_ANIMATION = 35;
    private static final int MSG_CHANGE_ERROR = 24;
    private static final int MSG_CREATE_SCREENSHOT = 33;
    private static final int MSG_GO_LAUNCHER = 22;
    private static final int MSG_LAUNCHER_HAVE_STARTED = 21;
    private static final int MSG_SET_BLUR_BITMAP = 25;
    private static final int MSG_SHOW_CONFIRM_DIALOG = 34;
    private static final int MSG_START_CIRCLE_ANIMATION = 32;
    private static final int MSG_START_LAUNCHER = 23;
    private static final long MSG_TIME_DELEAYED = 200;
    private static final String[] PACKAGE_NAMES = new String[]{"com.android.systemui"};
    private static final String ROG_CHANGE_ACTION = "com.huawei.systemmamanger.action.ROG_CHANGED";
    private static final String ROG_CHANGE_KEY = "rog_value";
    private static final String ROG_KILL_APPS_KEY = "rog_kill_apps_key";
    private static final float SCALE = 0.3f;
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            if (context == null) {
                HwLog.e(HwPowerManagerActivity.TAG, "getRawDataToIndex context is null!");
                return Lists.newArrayList();
            }
            List<SearchIndexableRaw> result = Lists.newArrayList();
            result.add(buildCommonPowerData(context, context.getString(R.string.save_mode), false));
            if (SystemProperties.getBoolean("ro.config.show_superpower", true)) {
                result.add(buildCommonPowerData(context, context.getString(R.string.super_power_saving_title), false));
            }
            result.add(buildCommonPowerData(context, context.getString(R.string.power_rog_switch_name), false));
            result.add(buildCommonPowerData(context, context.getString(R.string.systemmanager_module_title_lockscreencleanup), false));
            result.add(buildCommonPowerData(context, context.getString(R.string.power_high_consume_title), true));
            result.add(buildCommonPowerData(context, context.getString(R.string.consume_detail_button_new), true));
            result.add(buildCommonPowerData(context, context.getString(R.string.power_optimize_title), true));
            result.add(buildCommonPowerData(context, context.getString(R.string.power_management_moredetail), false));
            result.add(buildCommonPowerData(context, context.getString(R.string.consume_battery_percent_title), true));
            return result;
        }

        private SearchIndexableRaw buildCommonPowerData(Context context, String title, boolean shouldExpandSlideView) {
            SearchIndexableRaw raw = new SearchIndexableRaw(context);
            raw.screenTitle = context.getString(R.string.app_name);
            raw.title = title;
            raw.iconResId = R.drawable.ic_settings_battery;
            raw.intentAction = "huawei.intent.action.POWER_MANAGER";
            raw.intentTargetPackage = "com.huawei.systemmanager";
            raw.intentTargetClass = HwPowerManagerActivity.class.getName();
            try {
                raw.key = new JSONObject().put("title", title).put(HwPowerManagerActivity.KEY_SHOULD_EXPAND_SLIDE_VIEW, shouldExpandSlideView).toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return raw;
        }
    };
    private static String TAG = "HwPowerManagerActivity";
    private final OnClickListener mActionBarListener = new OnClickListener() {
        public void onClick(View v) {
            HwPowerManagerActivity.this.onActionBarItemSelected(v.getId());
        }
    };
    private ImageView mActionBarSettingImg = null;
    private ActivityManager mActivityManager;
    private Context mAppContext = null;
    private BitmapDrawable mBlurDrawable;
    private ImageView mCircleview;
    private WorkHandler mHandler;
    private HandlerThread mHandlerThread;
    private IWindowManager mIWm = null;
    private boolean mIsAutoStarted = false;
    private boolean mIsFromPhoneService = false;
    final Object mLock = new Object[0];
    private DialogInterface.OnClickListener mPositiveListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            String[] strArr = new String[6];
            strArr[0] = HsmStatConst.PARAM_KEY;
            strArr[1] = "0";
            strArr[2] = HsmStatConst.PARAM_VAL;
            strArr[3] = HwPowerManagerActivity.this.mRogChecked ? "1" : "0";
            strArr[4] = HsmStatConst.PARAM_OP;
            strArr[5] = HwPowerManagerActivity.this.mRogChecked ? "1" : "0";
            HsmStat.statE((int) Events.E_POWER_ROG_DIALOG, HsmStatConst.constructJsonParams(strArr));
            HwPowerManagerActivity.this.mRogChanged = true;
            HwPowerManagerActivity.this.mRogConfirmDialog.cancel();
        }
    };
    private PowerManagerFragment mPowerManagerFragment;
    private RogChangeListener mRogChangeListener = new RogChangeListener() {
        public void onChanged(boolean checked) {
            HwPowerManagerActivity.this.mRogChecked = checked;
            HwPowerManagerActivity.this.mHandler.sendEmptyMessage(33);
        }
    };
    private boolean mRogChanged = false;
    private boolean mRogChecked = false;
    private AlertDialog mRogConfirmDialog;
    private Builder mRogConfirmDialogBuild;
    private RelativeLayout mRogProcessView;
    private int mRogReturnValue = 0;
    private BitmapThread mThread;
    private final BroadcastReceiver mToTargetBroadcastDone = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            HwLog.i(HwPowerManagerActivity.TAG, "mToTargetBroadcastDone onReceive intent==" + (intent != null ? intent.getAction() : "null"));
            HwPowerManagerActivity.this.mHandler.sendEmptyMessage(19);
        }
    };
    private Handler mUIHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 17:
                    HwPowerManagerActivity.this.createRogProcessView(HwPowerManagerActivity.this.mRogChecked, false);
                    return;
                case 22:
                    if (!(HwPowerManagerActivity.this.mRogProcessView == null || HwPowerManagerActivity.this.isFinishing())) {
                        HwPowerManagerActivity.this.mWinManager.removeView(HwPowerManagerActivity.this.mRogProcessView);
                        HwPowerManagerActivity.this.mRogProcessView = null;
                    }
                    if (HwPowerManagerActivity.this.mRogProcessView != null) {
                        HwPowerManagerActivity.this.mRogProcessView = null;
                    }
                    Toast.makeText(HwPowerManagerActivity.this.getApplicationContext(), HwPowerManagerActivity.this.mRogChecked ? R.string.rog_change_to_open_toast_sucess : R.string.rog_change_to_close_toast_sucess, 1).show();
                    Message msgSelf = HwPowerManagerActivity.this.mHandler.obtainMessage(20);
                    msgSelf.arg1 = msg.arg1;
                    HwPowerManagerActivity.this.mHandler.sendMessageDelayed(msgSelf, 200);
                    return;
                case 23:
                    Intent home = new Intent("android.intent.action.MAIN");
                    home.addCategory("android.intent.category.HOME");
                    home.addFlags(ShareCfg.PERMISSION_MODIFY_CALENDAR);
                    HwPowerManagerActivity.this.getApplicationContext().startActivity(home);
                    Message msgGoLauncher = HwPowerManagerActivity.this.mUIHandler.obtainMessage(22);
                    msgGoLauncher.arg1 = msg.arg1;
                    HwPowerManagerActivity.this.mUIHandler.sendMessageDelayed(msgGoLauncher, 1000);
                    return;
                case 24:
                    boolean z;
                    int i;
                    String[] strArr = new String[4];
                    strArr[0] = HsmStatConst.PARAM_KEY;
                    strArr[1] = HwPowerManagerActivity.this.mRogChecked ? "1" : "0";
                    strArr[2] = HsmStatConst.PARAM_VAL;
                    strArr[3] = "1";
                    HsmStat.statE((int) Events.E_POWER_ROG_REPORT, HsmStatConst.constructJsonParams(strArr));
                    HwLog.e(HwPowerManagerActivity.TAG, "MSG_CHANGE_ERROR");
                    if (!(HwPowerManagerActivity.this.mRogProcessView == null || HwPowerManagerActivity.this.isFinishing())) {
                        HwPowerManagerActivity.this.mWinManager.removeView(HwPowerManagerActivity.this.mRogProcessView);
                        HwPowerManagerActivity.this.mRogProcessView = null;
                    }
                    if (HwPowerManagerActivity.this.mRogProcessView != null) {
                        HwPowerManagerActivity.this.mRogProcessView = null;
                    }
                    PowerManagerFragment -get6 = HwPowerManagerActivity.this.mPowerManagerFragment;
                    if (HwPowerManagerActivity.this.mRogChecked) {
                        z = false;
                    } else {
                        z = true;
                    }
                    -get6.setRogChecked(z);
                    Context applicationContext = HwPowerManagerActivity.this.getApplicationContext();
                    if (HwPowerManagerActivity.this.mRogChecked) {
                        i = R.string.rog_change_to_open_toast_error;
                    } else {
                        i = R.string.rog_change_to_close_toast_error;
                    }
                    Toast.makeText(applicationContext, i, 1).show();
                    return;
                case 32:
                    HwPowerManagerActivity.this.startCircleAnimation();
                    return;
                case 34:
                    if (HwPowerManagerActivity.this.mRogConfirmDialogBuild == null) {
                        HwPowerManagerActivity.this.mRogConfirmDialogBuild = new Builder(HwPowerManagerActivity.this);
                        HwPowerManagerActivity.this.mRogConfirmDialogBuild.setCancelable(false);
                        HwPowerManagerActivity.this.mRogConfirmDialogBuild.setNegativeButton(R.string.rog_dialog_negative, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                boolean z;
                                String[] strArr = new String[6];
                                strArr[0] = HsmStatConst.PARAM_KEY;
                                strArr[1] = "0";
                                strArr[2] = HsmStatConst.PARAM_VAL;
                                strArr[3] = HwPowerManagerActivity.this.mRogChecked ? "1" : "0";
                                strArr[4] = HsmStatConst.PARAM_OP;
                                strArr[5] = "2";
                                HsmStat.statE((int) Events.E_POWER_ROG_DIALOG, HsmStatConst.constructJsonParams(strArr));
                                HwPowerManagerActivity hwPowerManagerActivity = HwPowerManagerActivity.this;
                                if (HwPowerManagerActivity.this.mRogChecked) {
                                    z = false;
                                } else {
                                    z = true;
                                }
                                hwPowerManagerActivity.mRogChecked = z;
                                HwPowerManagerActivity.this.mPowerManagerFragment.setRogChecked(HwPowerManagerActivity.this.mRogChecked);
                                HwPowerManagerActivity.this.mRogConfirmDialog.dismiss();
                            }
                        });
                        HwPowerManagerActivity.this.mRogConfirmDialogBuild.setOnCancelListener(new OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                HwPowerManagerActivity.this.mUIHandler.sendEmptyMessage(17);
                            }
                        });
                    }
                    if (HwPowerManagerActivity.this.mRogChecked) {
                        HwPowerManagerActivity.this.mRogConfirmDialogBuild.setTitle(R.string.rog_dialog_title_to_open);
                        HwPowerManagerActivity.this.mRogConfirmDialogBuild.setMessage(R.string.rog_dialog_summary_to_open);
                        HwPowerManagerActivity.this.mRogConfirmDialogBuild.setPositiveButton(R.string.rog_dialog_open_positive, HwPowerManagerActivity.this.mPositiveListener);
                    } else {
                        HwPowerManagerActivity.this.mRogConfirmDialogBuild.setTitle(R.string.rog_dialog_title_to_close);
                        HwPowerManagerActivity.this.mRogConfirmDialogBuild.setMessage(R.string.rog_dialog_summary_to_close);
                        HwPowerManagerActivity.this.mRogConfirmDialogBuild.setPositiveButton(R.string.rog_dialog_close_positive, HwPowerManagerActivity.this.mPositiveListener);
                    }
                    HwPowerManagerActivity.this.mRogConfirmDialog = HwPowerManagerActivity.this.mRogConfirmDialogBuild.create();
                    HwPowerManagerActivity.this.mRogConfirmDialog.show();
                    return;
                case 35:
                    if (HwPowerManagerActivity.this.mCircleview != null) {
                        HwPowerManagerActivity.this.mCircleview.clearAnimation();
                        return;
                    }
                    return;
                case 256:
                    SurfaceControl.freezeDisplay();
                    return;
                case 257:
                    HwLog.d(HwPowerManagerActivity.TAG, "MSG_BEGIN_UNFREEZE_SCREEN");
                    SurfaceControl.unfreezeDisplay();
                    HwPowerManagerActivity.this.mUIHandler.sendEmptyMessage(32);
                    return;
                default:
                    return;
            }
        }
    };
    private WindowManager mWinManager = null;

    public interface RogChangeListener {
        void onChanged(boolean z);
    }

    private class BitmapThread extends Thread {
        public void run() {
            synchronized (HwPowerManagerActivity.this.mLock) {
                if (isInterrupted()) {
                    return;
                }
                Bitmap screenShot = null;
                try {
                    screenShot = BlurUtils.screenShotBitmap(HwPowerManagerActivity.this, 0, HwPowerManagerActivity.MAXLAYER, HwPowerManagerActivity.SCALE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } catch (Error err) {
                    HwLog.e(HwPowerManagerActivity.TAG, "startBlurScreenshotThread  Error er = " + err.getMessage());
                }
                if (screenShot == null) {
                    HwLog.e(HwPowerManagerActivity.TAG, "start screen shot fail,notify caller");
                    HwPowerManagerActivity.this.notifyBlurResult(null);
                } else if (screenShot == null) {
                } else {
                    Bitmap tmp;
                    if (!(screenShot.isMutable() && screenShot.getConfig() == Config.ARGB_8888)) {
                        tmp = BlurUtils.covertToARGB888(screenShot);
                        screenShot.recycle();
                        screenShot = tmp;
                    }
                    BlurUtils.blurImage(HwPowerManagerActivity.this, screenShot, screenShot, 18);
                    if (screenShot != null) {
                        tmp = BlurUtils.addBlackBoard(screenShot, HwPowerManagerActivity.this.getResources().getColor(R.color.hsm_black_alpha60_for_canvas));
                        screenShot.recycle();
                        screenShot = tmp;
                    }
                    if (isInterrupted()) {
                        return;
                    }
                    HwPowerManagerActivity.this.notifyBlurResult(screenShot);
                }
            }
        }
    }

    private class WorkHandler extends Handler {
        public WorkHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Bitmap currBitmap = null;
            super.handleMessage(msg);
            switch (msg.what) {
                case 18:
                    int mode = SysCoreUtils.getToSwitchMode();
                    Point point = SysCoreUtils.getToSwitchPoint(mode);
                    HwPowerManagerActivity.this.changeScreenMode(point.x, point.y, SysCoreUtils.getToSwitchROGDensity(mode), mode);
                    return;
                case 19:
                    HwPowerManagerActivity.this.killAllApp();
                    return;
                case 20:
                    HwPowerManagerActivity.this.finish();
                    int sysUid = msg.arg1;
                    HwLog.e(HwPowerManagerActivity.TAG, "MSG_BEGIN_KILL_SHARED_SYSTEM_UID_APP  system Uid===" + sysUid);
                    HwPowerManagerActivity.this.mActivityManager.killUid(sysUid, "kill hwsystemmanager self by hwsystemmanager for ROG  selfUid==" + sysUid);
                    return;
                case 21:
                    HwPowerManagerActivity.this.startLauncher(msg.arg1);
                    return;
                case 25:
                    HwPowerManagerActivity.this.mThread = null;
                    Bitmap blurBitmap = msg.obj;
                    if (HwPowerManagerActivity.this.mBlurDrawable != null) {
                        currBitmap = HwPowerManagerActivity.this.mBlurDrawable.getBitmap();
                    }
                    if (!(currBitmap == null || currBitmap == blurBitmap)) {
                        currBitmap.recycle();
                    }
                    if (blurBitmap != null) {
                        HwPowerManagerActivity.this.mBlurDrawable = new BitmapDrawable(HwPowerManagerActivity.this.getResources(), blurBitmap);
                        HwPowerManagerActivity.this.setDrawableBound();
                    }
                    HwPowerManagerActivity.this.mUIHandler.sendEmptyMessage(34);
                    return;
                case 33:
                    HwPowerManagerActivity.this.mThread = new BitmapThread();
                    HwPowerManagerActivity.this.mThread.start();
                    return;
                default:
                    return;
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.power_main_frame);
        if (!checkNetworkServiceAvailable()) {
            finish();
        }
        if (this.mAppContext == null) {
            this.mAppContext = getApplicationContext();
        }
        Intent intent = getIntent();
        if (intent != null) {
            String str = intent.getStringExtra("comefrom");
            if (str != null) {
                if (str.equals(PowerNotificationUtils.POWER_NOTIFICATION_SAVEMODE_VALUE)) {
                    PowerNotificationUtils.cancleLowBatterySaveModeNotification(this.mAppContext);
                } else if (str.equals(PowerNotificationUtils.POWER_NOTIFICATION_SUPERMODE_VALUE)) {
                    PowerNotificationUtils.cancleLowBatterySuperModeNotification(this.mAppContext);
                }
            }
        }
        this.mIWm = Stub.asInterface(ServiceManager.checkService("window"));
        this.mWinManager = (WindowManager) getSystemService("window");
        this.mActivityManager = (ActivityManager) this.mAppContext.getSystemService("activity");
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mHandler = new WorkHandler(this.mHandlerThread.getLooper());
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.power_management_title);
        ActionBarEx.setEndIcon(actionBar, true, getDrawable(R.drawable.settings_menu_btn_selector), this.mActionBarListener);
        ActionBarEx.setEndContentDescription(actionBar, getString(R.string.ActionBar_AddAppSettings_Title));
        actionBar.show();
        initUI();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        HwLog.i(TAG, "newConfig:" + newConfig);
        if (this.mRogChanged) {
            HwLog.i(TAG, "onConfigurationChanged=== " + newConfig + "****" + newConfig.densityDpi + "***" + 0 + "****" + 4096 + "***");
            String[] strArr = new String[4];
            strArr[0] = HsmStatConst.PARAM_KEY;
            strArr[1] = this.mRogChecked ? "1" : "0";
            strArr[2] = HsmStatConst.PARAM_VAL;
            strArr[3] = "0";
            HsmStat.statE((int) Events.E_POWER_ROG_REPORT, HsmStatConst.constructJsonParams(strArr));
            Intent rogChangeIntent = new Intent(ROG_CHANGE_ACTION);
            rogChangeIntent.putExtra(ROG_CHANGE_KEY, this.mRogReturnValue);
            GlobalContext.getContext().sendBroadcast(rogChangeIntent);
            this.mRogChanged = false;
            createRogProcessView(this.mRogChecked, true);
        }
    }

    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent != null) {
            this.mIsFromPhoneService = intent.getBooleanExtra(FearureConfigration.AUTO_START, false);
        }
        if (this.mPowerManagerFragment != null) {
            this.mPowerManagerFragment.setPhoneService(this.mIsAutoStarted, this.mIsFromPhoneService);
            if (this.mIsFromPhoneService) {
                this.mIsAutoStarted = true;
            }
        }
    }

    private void initUI() {
        if (this.mPowerManagerFragment == null) {
            this.mPowerManagerFragment = new PowerManagerFragment();
        }
        this.mPowerManagerFragment.setRogChangeListener(this.mRogChangeListener);
        this.mPowerManagerFragment.setPhoneService(this.mIsAutoStarted, this.mIsFromPhoneService);
        if (this.mIsFromPhoneService) {
            this.mIsAutoStarted = true;
        }
        if (!this.mPowerManagerFragment.isAdded()) {
            getFragmentManager().beginTransaction().replace(R.id.power_frame, this.mPowerManagerFragment).commit();
        }
    }

    private boolean checkNetworkServiceAvailable() {
        INetworkManagementService networkService = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        if (networkService == null) {
            return false;
        }
        try {
            if (networkService.isBandwidthControlEnabled()) {
                return true;
            }
            HwLog.e(TAG, "No bandwidth control; leaving");
            return false;
        } catch (RemoteException e) {
            HwLog.e(TAG, "No bandwidth control; leaving");
            return false;
        }
    }

    private void onActionBarItemSelected(int itemId) {
        HwLog.d(TAG, "BatteryManager Setting Button is clicked ");
        startActivity(new Intent(this.mAppContext, PowerSettingActivity.class));
        HsmStat.statE(Events.E_POWER_CLICK_SETTING);
    }

    private void changeScreenMode(int width, int height, int density, int mode) {
        HwLog.i(TAG, "enter begin changeScreenMode(width:" + width + ", height:" + height + " density:" + density + " mode:" + mode + ")");
        try {
            this.mRogReturnValue = SurfaceControl.setRogDisplayConfigFull(width, height, density, mode);
            if (this.mRogReturnValue == 0) {
                this.mRogChanged = true;
                HwLog.i(TAG, "SurfaceControl.setRogDisplayConfigFull ok");
            } else {
                HwLog.e(TAG, "SurfaceControl.setRogDisplayConfigFull error mRogReturnValue==" + this.mRogReturnValue);
                this.mRogChanged = false;
            }
            if (this.mRogChanged) {
                SurfaceControl.freezeDisplay();
                this.mIWm.setForcedDisplayDensityAndSize(0, density, width, height);
                return;
            }
            this.mUIHandler.sendEmptyMessage(24);
        } catch (Exception e) {
            this.mRogChanged = false;
            this.mUIHandler.sendEmptyMessage(24);
            HwLog.e(TAG, e.toString(), e);
        }
    }

    private void killAllApp() {
        int i;
        HwLog.i(TAG, "enter killAllApp()");
        List<RunningAppProcessInfo> appInfo = this.mActivityManager.getRunningAppProcesses();
        int lastKillUid = this.mAppContext.getApplicationInfo().uid;
        List<String> killAppNames = new ArrayList();
        List<Integer> killUids = new ArrayList();
        List<Integer> whiteUids = new ArrayList();
        List<String> whitePckages = Arrays.asList(KILL_WHITE_PCK_NAME);
        HsmPackageManager hsmPackageManager = HsmPackageManager.getInstance();
        int whiteSize = KILL_WHITE_PCK_NAME.length;
        StringBuilder logStr = new StringBuilder();
        for (i = 0; i < whiteSize; i++) {
            HsmPkgInfo hsmPkgInfo = null;
            try {
                hsmPkgInfo = hsmPackageManager.getPkgInfo((String) whitePckages.get(i), 0);
            } catch (NameNotFoundException e) {
                HwLog.e(TAG, e.getMessage());
            }
            if (!(hsmPkgInfo == null || hsmPkgInfo.mUid == lastKillUid || whiteUids.contains(Integer.valueOf(hsmPkgInfo.mUid)))) {
                whiteUids.add(Integer.valueOf(hsmPkgInfo.mUid));
                logStr.append("white packagename ==").append(hsmPkgInfo.mPkgName).append("white uid ==").append(hsmPkgInfo.mUid).append("\n");
            }
        }
        HwLog.i(TAG, logStr.toString());
        logStr.delete(0, logStr.length());
        int size = appInfo.size();
        for (i = 0; i < size; i++) {
            RunningAppProcessInfo info = (RunningAppProcessInfo) appInfo.get(i);
            if (info != null) {
                logStr.append("running  app processName =").append(info.processName).append("   flags=").append(info.flags).append("   uid=").append(info.uid).append("\n");
                if (info.uid != lastKillUid) {
                    if (!whiteUids.contains(Integer.valueOf(info.uid))) {
                        if (!containsInMultUsers(whiteUids, info.uid)) {
                            killAppNames.add(info.processName);
                            if (!killUids.contains(Integer.valueOf(info.uid))) {
                                killUids.add(Integer.valueOf(info.uid));
                            }
                        }
                    }
                    logStr.append("running  app processName =").append(info.processName).append("   uid=").append(info.uid).append("  in whiteUids \n");
                }
            }
        }
        HwLog.i(TAG, logStr.toString());
        logStr.delete(0, logStr.length());
        for (Integer intValue : killUids) {
            int uid = intValue.intValue();
            logStr.append("kill  app uid=").append(uid).append("\n");
            this.mActivityManager.killUid(uid, "kill app by hwsystemmanager for ROG");
        }
        HwLog.i(TAG, logStr.toString());
        String[] apps = (String[]) killAppNames.toArray(new String[killAppNames.size()]);
        Intent intent = new Intent(KILL_APP_END_ACTION);
        intent.putExtra(ROG_CHANGE_KEY, this.mRogReturnValue);
        intent.putExtra(ROG_KILL_APPS_KEY, apps);
        GlobalContext.getContext().sendBroadcast(intent);
        HwLog.i(TAG, "send KILL_APP_END_ACTION intent");
        this.mHandler.removeMessages(21);
        Message msgLauncher = this.mHandler.obtainMessage(21);
        msgLauncher.arg1 = lastKillUid;
        this.mHandler.sendMessageDelayed(msgLauncher, 200);
        HwLog.e(TAG, "exit killAllApp() lastKillUid===" + lastKillUid);
    }

    private void startLauncher(int lastKillUid) {
        int haveSize = 0;
        List<RunningAppProcessInfo> appInfo = this.mActivityManager.getRunningAppProcesses();
        int appInfoSize = appInfo.size();
        int pckSize = PACKAGE_NAMES.length;
        for (int i = 0; i < appInfoSize && haveSize < pckSize; i++) {
            for (int j = 0; j < pckSize; j++) {
                if (PACKAGE_NAMES[j].equals(((RunningAppProcessInfo) appInfo.get(i)).processName)) {
                    haveSize++;
                    HwLog.d(TAG, "pckName==" + PACKAGE_NAMES[j] + "****" + haveSize);
                    break;
                }
            }
        }
        if (haveSize == pckSize) {
            Message msgStartLauncher = this.mUIHandler.obtainMessage(23);
            msgStartLauncher.arg1 = lastKillUid;
            this.mUIHandler.sendMessageDelayed(msgStartLauncher, 4000);
            return;
        }
        this.mHandler.removeMessages(21);
        Message msgLauncher = this.mHandler.obtainMessage(21);
        msgLauncher.arg1 = lastKillUid;
        this.mHandler.sendMessageDelayed(msgLauncher, 200);
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mHandlerThread.quitSafely();
        if (this.mRogProcessView != null) {
            this.mWinManager.removeView(this.mRogProcessView);
            this.mRogProcessView = null;
        }
    }

    private void createRogProcessView(boolean open, boolean isrecreate) {
        LayoutParams lp = new LayoutParams(-1, -1, 2009, 16909569, -1);
        lp.privateFlags |= Integer.MIN_VALUE;
        lp.windowAnimations = R.style.rog_process_view_animation;
        lp.screenOrientation = 14;
        if (!(this.mRogProcessView == null || isFinishing())) {
            this.mWinManager.removeView(this.mRogProcessView);
            this.mRogProcessView = null;
        }
        if (this.mRogProcessView != null) {
            this.mRogProcessView = null;
        }
        this.mRogProcessView = (RelativeLayout) LayoutInflater.from(GlobalContext.getContext()).inflate(R.layout.rog_process_view, null);
        this.mCircleview = (ImageView) this.mRogProcessView.findViewById(R.id.rog_process_circleImage);
        TextView processText = (TextView) this.mRogProcessView.findViewById(R.id.rog_process_message);
        if (isrecreate) {
            processText.setText(open ? R.string.rog_change_process_to_open_title : R.string.rog_change_process_to_close_title);
        } else {
            processText.setText(R.string.rog_change_processing);
        }
        this.mRogProcessView.setBackground(this.mBlurDrawable);
        this.mRogProcessView.requestFocus();
        this.mWinManager.addView(this.mRogProcessView, lp);
        this.mRogProcessView.setSystemUiVisibility(16909573);
        if (isrecreate) {
            Message msgChange = this.mUIHandler.obtainMessage(257);
            if (this.mAppContext.getUserId() != 0) {
                HwLog.i(TAG, "now at not user owner we should delay 2s");
                this.mUIHandler.sendMessageDelayed(msgChange, 2000);
                return;
            }
            HwLog.i(TAG, "now at user owner we should delay 1s");
            this.mUIHandler.sendMessageDelayed(msgChange, 1000);
            return;
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(18), 200);
    }

    public void startCircleAnimation() {
        Animation circleImageRotate = AnimationUtils.loadAnimation(this, R.anim.circle_rotate);
        if (this.mCircleview != null) {
            this.mCircleview.startAnimation(circleImageRotate);
            sendBroadCastToTarget(this.mAppContext);
        }
    }

    private void notifyBlurResult(Bitmap bitmap) {
        Message msg = Message.obtain();
        msg.obj = bitmap;
        msg.what = 25;
        this.mHandler.sendMessage(msg);
    }

    private void setDrawableBound() {
        if (this.mBlurDrawable != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            this.mWinManager.getDefaultDisplay().getRealMetrics(displayMetrics);
            this.mBlurDrawable.setBounds(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        }
    }

    private void sendBroadCastToTarget(Context context) {
        HwLog.i(TAG, "begin sendBroadCastToTarget");
        Intent targetIntent = new Intent(KILL_APP_START_ACTION);
        targetIntent.addFlags(ShareCfg.PERMISSION_MODIFY_CALENDAR);
        context.sendOrderedBroadcastAsUser(targetIntent, UserHandle.ALL, null, this.mToTargetBroadcastDone, this.mHandler, 0, null, null);
    }

    private boolean containsInMultUsers(List<Integer> whiteUids, int comparedUid) {
        for (Integer uid : whiteUids) {
            if (UserHandle.isSameApp(uid.intValue(), comparedUid)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkShouldExpand() {
        Intent intent = getIntent();
        if (intent == null) {
            return false;
        }
        String extra = intent.getStringExtra(SettingSearchUtil.KEY_EXTRA_SETTING);
        if (TextUtils.isEmpty(extra)) {
            return false;
        }
        try {
            return new JSONObject(extra).getBoolean(KEY_SHOULD_EXPAND_SLIDE_VIEW);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
