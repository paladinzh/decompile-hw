package com.huawei.systemmanager.power.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncStatusObserver;
import android.database.ContentObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import com.android.internal.view.RotationPolicy;
import com.android.internal.view.RotationPolicy.RotationPolicyListener;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.concurrent.HsmSingleExecutor;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.RollingView;
import com.huawei.systemmanager.comm.widget.SimpleTextView;
import com.huawei.systemmanager.comm.wrapper.SharePrefWrapper;
import com.huawei.systemmanager.customize.AbroadUtils;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.optimize.process.ProtectActivity;
import com.huawei.systemmanager.optimize.process.ProtectAppControl;
import com.huawei.systemmanager.power.batterychart.BatterHistoryActivity;
import com.huawei.systemmanager.power.comm.ActionConst;
import com.huawei.systemmanager.power.comm.ApplicationConstant;
import com.huawei.systemmanager.power.comm.SharedPrefKeyConst;
import com.huawei.systemmanager.power.comm.TimeConst;
import com.huawei.systemmanager.power.data.battery.BatteryInfo;
import com.huawei.systemmanager.power.data.charge.ChargeInfo;
import com.huawei.systemmanager.power.model.PowerManagementModel;
import com.huawei.systemmanager.power.model.PowerModeControl;
import com.huawei.systemmanager.power.model.PowerModeDialogControl;
import com.huawei.systemmanager.power.service.DarkThemeChanageService;
import com.huawei.systemmanager.power.ui.HwPowerManagerActivity.RogChangeListener;
import com.huawei.systemmanager.power.util.PowerNotificationUtils;
import com.huawei.systemmanager.power.util.SysCoreUtils;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.numberlocation.NumberLocationPercent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PowerManagerFragment extends Fragment {
    private static final String ACTION_BATTERY_DETAIL = "com.android.settings.BATTERY_HISTORY_DETAIL";
    private static final int AVALIABLE_TIME_OK = 4;
    private static final int BATTERY_CHARGING = 2;
    private static final int BATTERY_STATUS_UNKNOWN = 1;
    static final int BETTERY_FULL = 100;
    private static final int BETTERY_ZERO = 0;
    private static final String BUNDLE_DOUBLE_KEY_SURPLUS_TIME = "dtime";
    private static final String BUNDLE_DOUBLE_KEY_THRESHOLD = "threshold_time";
    private static final String BUNDLE_STRING_KEY_SURPLUS_TIME = "stime";
    private static final int CHANGE_DARK_SUMMARY = 8;
    private static final int CHARGE_TIME_OK = 7;
    private static final int COULUMB_LIMIT = 20;
    private static final int CURR_MODE_AVALIABLE_TIME_OK = 10;
    private static final String DB_BATTERY_PERCENT_SWITCH = "battery_percent_switch";
    private static final long DELAY_EXPAND_SLIDE_VIEW = 600;
    private static final int DELAY_OBSERVER = 500;
    private static final int DELAY_TIME_FOR_THREAD = 2000;
    private static final int FIVE_MINUTES = 5;
    private static final int FRESH_NUMBER = 5;
    public static final int NO_CHARGER = 0;
    private static final int SETTING_SLEEP_TIME_DEFAULT = 20;
    private static final int START_TO_CHANGE_FRAGMENT = 3;
    private static final String SUPER_SAVE_MODE_CHECKED = "super_save_mode_checked";
    private static final String TAG = "PowerManagerFragment";
    private static final int TIME_FIVE = 5;
    public static final String URI_FOR_SUBID_0 = "mobile_data0";
    public static final String URI_FOR_SUBID_1 = "mobile_data1";
    private static boolean isPowerSaveState = false;
    private static boolean islowBatteryPowerNotified = false;
    private static final BroadcastReceiver mBTReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null && !TextUtils.isEmpty(intent.getAction())) {
                if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(intent.getAction())) {
                    HwLog.i(PowerManagerFragment.TAG, "BroadcastReceiver mBTReceiver is receive ");
                }
            }
        }
    };
    private static int mPlugged = 0;
    private static PowerManagementModel mPowerManager = null;
    private static Map<String, Object> mTimeByCurrentBattery = null;
    private static BroadcastReceiver mVibrateChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null && !TextUtils.isEmpty(intent.getAction())) {
                if ("android.media.RINGER_MODE_CHANGED".equals(intent.getAction())) {
                    HwLog.i(PowerManagerFragment.TAG, "mVibrateChangedReceiver is receive ");
                }
            }
        }
    };
    private static final BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null && !TextUtils.isEmpty(intent.getAction())) {
                if ("android.net.wifi.WIFI_STATE_CHANGED".equals(intent.getAction())) {
                    HwLog.i(PowerManagerFragment.TAG, "BroadcastReceiver mWifiReceiver is receive ");
                }
            }
        }
    };
    private TextView bgConsumeAppsTv;
    private double currModeLeftTime = 0.0d;
    private View fragmentView = null;
    private boolean isDarkThemeBroadCastRegister;
    private boolean isFailCheck = false;
    private boolean isregister;
    private Activity mActivity = null;
    private ArrayList<Double> mAllModeLeftTimeList = new ArrayList();
    private Context mAppContext = null;
    private DataAsyncLoader mAsyncLoader = null;
    private AvaliableTime mAvaliableTimeRunnable = new AvaliableTime(this.mHandler);
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null && !TextUtils.isEmpty(intent.getAction())) {
                String action = intent.getAction();
                if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                    PowerManagerFragment.this.mBatteryStatus = intent.getIntExtra("status", 1);
                    PowerManagerFragment.setmPlugged(intent.getIntExtra("plugged", 0));
                    HwLog.e(PowerManagerFragment.TAG, "BroadcastReceiver mPlugged = " + PowerManagerFragment.getmPlugged() + ",mBatteryStatus = " + PowerManagerFragment.this.mBatteryStatus);
                    PowerManagerFragment.this.mRawlevel = intent.getIntExtra("level", 0);
                    PowerManagerFragment.this.mbatteryFL.updateWaterLevel(PowerManagerFragment.this.mRawlevel);
                    HwLog.d(PowerManagerFragment.TAG, "in PowerModeAndSwitchFragment Receiver and mPlugged is " + PowerManagerFragment.mPlugged);
                    if (20 > PowerManagerFragment.this.mRawlevel && !PowerManagerFragment.islowBatteryPowerNotified) {
                        context.sendBroadcast(new Intent(ActionConst.INTENT_POWER_STATISTIC), "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
                        HwLog.e(PowerManagerFragment.TAG, "Battey LowLevel Trigger INTENT_POWER_STATISTIC.");
                        PowerManagerFragment.islowBatteryPowerNotified = true;
                    }
                }
                if ("android.intent.action.ACTION_POWER_DISCONNECTED".equals(action)) {
                    PowerManagerFragment.setmPlugged(0);
                    PowerManagerFragment.this.initModeSwitch();
                }
                if ("android.intent.action.ACTION_POWER_CONNECTED".equals(action)) {
                    PowerManagerFragment.this.initModeSwitch();
                }
                if (PowerModeControl.CHANGE_MODE_ACTION.equals(action)) {
                    HwLog.e(PowerManagerFragment.TAG, "Power mode change Receive and action is " + action);
                    PowerManagerFragment.this.initModeSwitch();
                }
                HwLog.e(PowerManagerFragment.TAG, "in Receiver and action is " + action);
                if (PowerManagerFragment.getmPlugged() != 0) {
                    PowerManagerFragment.this.expectFullTimeShow();
                }
                PowerManagerFragment.this.getAvaliableTime();
            }
        }
    };
    private RelativeLayout mBatteryLayout;
    private int mBatteryStatus = 1;
    private Handler mBatteryTimeHandler = null;
    private RelativeLayout mBkgConsume;
    private BrightnessModeObserver mBrightnessModeObserver;
    private BrightnessObserver mBrightnessObserver;
    private OnCancelListener mCancelListener = new OnCancelListener() {
        public void onCancel(DialogInterface dialog) {
            HwLog.i(PowerManagerFragment.TAG, "OnCancelListener");
            if (PowerManagerFragment.this.mSuperSaveModeChecked) {
                PowerManagerFragment.this.mSuperSaveModeChecked = !PowerManagerFragment.this.mSuperSaveModeChecked;
                PowerManagerFragment.this.mSuperModeSwitch.setChecked(PowerManagerFragment.this.mSuperSaveModeChecked);
            }
        }
    };
    private TextView mChargingView;
    private RelativeLayout mCheckAll;
    private RelativeLayout mConsumeBatteryPercent;
    private TextView mConsumeBatteryPercentDes;
    private Switch mConsumeBatteryPercentSwitch;
    private RelativeLayout mConsumeLevel;
    private CurrModeAvaliableTime mCurrModeAvaliableTimeRunnable = new CurrModeAvaliableTime(this.mHandler);
    private BroadcastReceiver mDarkThemeChangerReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null && !TextUtils.isEmpty(intent.getAction())) {
                if (ActionConst.INTENT_CHANGE_POWER_SAVE_THEME_SELF.equals(intent.getAction())) {
                    Bundle b = intent.getExtras();
                    if (b != null) {
                        PowerManagerFragment.this.doDarkSwitchChange(b.getInt(DarkThemeChanageService.CHANGE_UI_ACTION));
                    }
                }
            }
        }
    };
    private boolean mDarkThemeControlChecked = false;
    private Switch mDarkThemeControlSwitch;
    private RelativeLayout mDarkThemeLayout;
    private TextView mDarkThemeName;
    private View mDarkThemeOnePx;
    private ProgressBar mDarkThemeProgressBar;
    private TextView mDarkThemeSummary;
    private ImageView mDetailListArrow = null;
    private TextView mEduTimeView = null;
    private HsmSingleExecutor mExecutor = new HsmSingleExecutor();
    private Runnable mExpandeViewRunnable = new Runnable() {
        public void run() {
            View container = PowerManagerFragment.this.getView();
            if (container != null) {
                ScrollView scrollView = (ScrollView) container.findViewById(R.id.power_mode_switch_scrollview);
                if (scrollView != null) {
                    scrollView.fullScroll(130);
                }
            }
        }
    };
    private ExpectedChargeTime mExpectedChargeTime = new ExpectedChargeTime(this.mHandler);
    private FeedBackObserver mFeedBackObserver;
    private GPSObserver mGPSObserver;
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 4:
                    if (PowerManagerFragment.getmPlugged() == 0 || !SysCoreUtils.isCharging(PowerManagerFragment.this.mBatteryStatus)) {
                        PowerManagerFragment.this.handleAvaliableTimeOk();
                    }
                    PowerManagerFragment.this.setModeAvaliableTime();
                    return;
                case 5:
                    PowerManagerFragment.this.handleRefreshNumber(msg.arg1, msg.arg2);
                    return;
                case 7:
                    PowerManagerFragment.this.updateExpectedChargingView((double) msg.arg1);
                    return;
                case 8:
                    PowerManagerFragment.this.mDarkThemeSummary.setText(R.string.dark_theme_switch_summary);
                    return;
                case 10:
                    if (PowerManagerFragment.this.getActivity() != null) {
                        HsmStat.statE(Events.E_POWER_HISTORY_DETAIL);
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putDouble("estimatedTime", PowerManagerFragment.this.currModeLeftTime);
                        intent.putExtras(bundle);
                        intent.setClass(PowerManagerFragment.this.mActivity, BatterHistoryActivity.class);
                        PowerManagerFragment.this.startActivity(intent);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private HandlerThread mHandlerThread = null;
    private int mHour = -1;
    private LayoutInflater mInflater = null;
    private boolean mIsAutoStarted = false;
    private boolean mIsBatteryPercent = false;
    private boolean mIsFromPhoneService = false;
    private boolean mLoadedStats = false;
    private boolean mLowResolutionControlChecked = false;
    private RelativeLayout mLowResolutionControlLayout;
    private Switch mLowResolutionControlSwitch;
    private OnCheckedChangeListener mLowResolutionSwitchCheckListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (PowerManagerFragment.this.mLowResolutionControlChecked != isChecked) {
                String[] strArr = new String[2];
                strArr[0] = HsmStatConst.PARAM_OP;
                strArr[1] = isChecked ? "1" : "0";
                HsmStat.statE((int) Events.E_POWER_ROG_SWITCH, HsmStatConst.constructJsonParams(strArr));
                SysCoreUtils.setLowResolutionSwitchState(PowerManagerFragment.this.mAppContext, isChecked);
                PowerManagerFragment.this.mLowResolutionControlChecked = isChecked;
                PowerManagerFragment.this.getAvaliableTime();
            }
        }
    };
    private int mMin = -1;
    private MobileDataObserver mMobileDataObserver;
    private LinearLayout mModeRL;
    Runnable mObserve = new Runnable() {
        public void run() {
            PowerManagerFragment.this.registerObservers();
        }
    };
    private boolean mObserved = false;
    private SimpleTextView mPercentView = null;
    private OnClickListener mPowerDetailClickListener = new PowerDetailClickListener();
    private PowerModeObserver mPowerModeObserver;
    private ProtectDataAsyncLoader mProtectAsynLoader = null;
    private int mRawlevel;
    private final RotationPolicyListener mRotationPolicyListener = new RotationPolicyListener() {
        public void onChange() {
            HwLog.i(PowerManagerFragment.TAG, "RotationPolicyListener");
            PowerManagerFragment.this.getAvaliableTime();
        }
    };
    private RelativeLayout mRunningScreenOff;
    private TextView mRunningSummary;
    private OnCheckedChangeListener mSaveModeCheckListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (PowerManagerFragment.this.mSaveModeChecked != isChecked) {
                String[] strArr = new String[2];
                strArr[0] = HsmStatConst.PARAM_OP;
                strArr[1] = isChecked ? "1" : "0";
                HsmStat.statE((int) Events.E_POWER_POWERMODE_SWITCH_STATUS, HsmStatConst.constructJsonParams(strArr));
                String statParam1;
                if (isChecked) {
                    HwLog.i(PowerManagerFragment.TAG, "PowerModelStateChange, open save mode from PowerManager layout.");
                    PowerNotificationUtils.showPowerModeQuitNotification(PowerManagerFragment.this.mAppContext);
                    PowerModeControl.getInstance(PowerManagerFragment.this.mAppContext).changePowerMode(4);
                    statParam1 = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "1");
                    HsmStat.statE((int) Events.E_POWER_POWERMODE_SELECT, statParam1);
                } else {
                    HwLog.i(PowerManagerFragment.TAG, "PowerModelStateChange, close save mode from PowerManager layout.");
                    PowerModeControl.getInstance(PowerManagerFragment.this.mAppContext).changePowerMode(1);
                    PowerNotificationUtils.cancleLowBatterySaveModeNotification(PowerManagerFragment.this.mAppContext);
                    PowerNotificationUtils.canclePowerModeOpenNotification(PowerManagerFragment.this.mAppContext);
                    statParam1 = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "0");
                    HsmStat.statE((int) Events.E_POWER_POWERMODE_SELECT, statParam1);
                }
                PowerManagerFragment.this.mSaveModeChecked = isChecked;
            }
        }
    };
    private boolean mSaveModeChecked = false;
    private RelativeLayout mSaveModeLayout;
    private Switch mSaveModeSwitch;
    private TextView mSaveModeTimeTv;
    private RelativeLayout mScreenSaveLayout;
    private SleepTimeOutObserver mSleepTimeOutObserver;
    private Object mStatusChangeListenerHandle;
    private DialogInterface.OnClickListener mSuperModeDialogClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            String statParam;
            if (which == -1) {
                statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_KEY, "com.huawei.systemmanager", HsmStatConst.PARAM_VAL, "1");
                HsmStat.statE((int) Events.E_POWER_SUPERSVAEMODE_DIALOG_ENTER, statParam);
                SysCoreUtils.enterSuperPowerSavingMode(PowerManagerFragment.this.mAppContext);
                dialog.dismiss();
            } else if (which == -2) {
                statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_KEY, "com.huawei.systemmanager", HsmStatConst.PARAM_VAL, "0");
                HsmStat.statE((int) Events.E_POWER_SUPERSVAEMODE_DIALOG_ENTER, statParam);
                dialog.cancel();
            }
        }
    };
    private Switch mSuperModeSwitch;
    private boolean mSuperSaveModeChecked = false;
    private TextView mSuperSaveModeDes;
    private RelativeLayout mSuperSaveModeLayout;
    private TextView mSuperSaveModeTimeTv;
    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        public void onStatusChanged(int which) {
            HwLog.i(PowerManagerFragment.TAG, "SyncStatusObserver");
            PowerManagerFragment.this.getAvaliableTime();
        }
    };
    private LinearLayout mUpperView;
    private PowerWaveView mbatteryFL;
    private RollingView mbatteryView = null;
    private Boolean sMultiSim = null;

    private class AvaliableTime implements Runnable {
        private Handler mTimeHandler;

        public AvaliableTime(Handler handler) {
            this.mTimeHandler = handler;
        }

        public void run() {
            try {
                PowerManagerFragment.this.initModel();
                PowerManagementModel pm = PowerManagerFragment.getPowerManager();
                if (pm != null) {
                    PowerManagerFragment.mTimeByCurrentBattery = pm.getTimeByCurrentBatteryLevel(PowerManagerFragment.this.mAppContext, PowerManagerFragment.this.mRawlevel);
                    this.mTimeHandler.removeMessages(4);
                    this.mTimeHandler.sendMessage(this.mTimeHandler.obtainMessage(4, null));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private class BrightnessModeObserver extends ContentObserver {
        public BrightnessModeObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HwLog.i(PowerManagerFragment.TAG, "BrightnessModeObserver");
            PowerManagerFragment.this.getAvaliableTime();
        }
    }

    private class BrightnessObserver extends ContentObserver {
        public BrightnessObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HwLog.i(PowerManagerFragment.TAG, "BrightnessObserver");
            PowerManagerFragment.this.getAvaliableTime();
        }
    }

    private class CurrModeAvaliableTime implements Runnable {
        private Handler mTimeHandler;

        public CurrModeAvaliableTime(Handler handler) {
            this.mTimeHandler = handler;
        }

        public void run() {
            try {
                PowerManagerFragment.this.initModel();
                PowerManagementModel pm = PowerManagerFragment.getPowerManager();
                if (pm != null) {
                    PowerManagerFragment.mTimeByCurrentBattery = pm.getTimeByCurrentBatteryLevel(PowerManagerFragment.this.mAppContext, PowerManagerFragment.this.mRawlevel);
                    String mModeInString = ApplicationConstant.SMART_MODE_KEY;
                    switch (PowerManagerFragment.this.getCurrentSaveMode()) {
                        case 0:
                            mModeInString = ApplicationConstant.SMART_MODE_KEY;
                            break;
                        case 1:
                            mModeInString = ApplicationConstant.SMART_MODE_KEY;
                            break;
                        case 4:
                            mModeInString = ApplicationConstant.SAVE_MODE_KEY;
                            break;
                        default:
                            mModeInString = ApplicationConstant.SUPER_MODE_POWER_KEY;
                            break;
                    }
                    PowerManagerFragment.this.currModeLeftTime = Double.parseDouble(PowerManagerFragment.mTimeByCurrentBattery.get(mModeInString).toString());
                    this.mTimeHandler.removeMessages(10);
                    this.mTimeHandler.sendMessage(this.mTimeHandler.obtainMessage(10, null));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private class DataAsyncLoader extends AsyncTask<Void, Void, List<String>> {
        private DataAsyncLoader() {
        }

        protected List<String> doInBackground(Void... param) {
            return SysCoreUtils.getBackgroundConsumeData(PowerManagerFragment.this.mAppContext);
        }

        protected void onPostExecute(List<String> result) {
            if (isCancelled()) {
                HwLog.w(PowerManagerFragment.TAG, "onPostExecute, The task is canceled");
                return;
            }
            if (result != null) {
                int num = result.size();
                PowerManagerFragment.this.bgConsumeAppsTv.setText(PowerManagerFragment.this.getResources().getQuantityString(R.plurals.power_management_bgPowerAppsNum, num, new Object[]{Integer.valueOf(num)}));
            }
            PowerManagerFragment.this.mAsyncLoader = null;
        }
    }

    private class ExpectedChargeTime implements Runnable {
        private Handler mTimeHandler;

        public ExpectedChargeTime(Handler handler) {
            this.mTimeHandler = handler;
        }

        public void run() {
            int leftTime = PowerManagerFragment.this.getExpectedFullTime();
            this.mTimeHandler.removeMessages(4);
            Message msg = this.mTimeHandler.obtainMessage(7, null);
            msg.arg1 = leftTime;
            this.mTimeHandler.sendMessage(msg);
        }
    }

    private class FeedBackObserver extends ContentObserver {
        public FeedBackObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HwLog.i(PowerManagerFragment.TAG, "FeedBackObserver");
            PowerManagerFragment.this.getAvaliableTime();
        }
    }

    private class GPSObserver extends ContentObserver {
        public GPSObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HwLog.i(PowerManagerFragment.TAG, "GPSObserver");
            PowerManagerFragment.this.getAvaliableTime();
        }
    }

    private class MobileDataObserver extends ContentObserver {
        public MobileDataObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HwLog.i(PowerManagerFragment.TAG, "MobileDataObserver");
            PowerManagerFragment.this.getAvaliableTime();
        }
    }

    private class PowerDetailClickListener implements OnClickListener {
        private PowerDetailClickListener() {
        }

        public void onClick(View v) {
            PowerManagerFragment.this.doPowerDetailClick();
        }
    }

    private static class PowerModeObserver extends ContentObserver {
        public PowerModeObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HwLog.i(PowerManagerFragment.TAG, "PowerModeObserver onChange");
        }
    }

    private class ProtectDataAsyncLoader extends AsyncTask<Void, Void, Map<String, Integer>> {
        private ProtectDataAsyncLoader() {
        }

        protected Map<String, Integer> doInBackground(Void... param) {
            return ProtectAppControl.getInstance(PowerManagerFragment.this.mAppContext).getProtectListInfo();
        }

        protected void onPostExecute(Map<String, Integer> result) {
            if (isCancelled()) {
                HwLog.w(PowerManagerFragment.TAG, "onPostExecute, The ProtectDataAsyncLoader task is canceled");
                return;
            }
            int protectedAppsNum = ((Integer) result.get(ApplicationConstant.PROTECTED_APP_KEY)).intValue();
            int mAllProtectNum = ((Integer) result.get(ApplicationConstant.PROTECT_ALL_KEY)).intValue();
            HwLog.i(PowerManagerFragment.TAG, " initProtectAppsInfo protectedAppsNum = " + protectedAppsNum + " ,mAllProtectNum= " + mAllProtectNum);
            if (PowerManagerFragment.this.getActivity() != null) {
                if (AbroadUtils.isAbroad()) {
                    int unProtectedAppNum = mAllProtectNum - protectedAppsNum;
                    PowerManagerFragment.this.mRunningSummary.setText(PowerManagerFragment.this.getResources().getQuantityString(R.plurals.power_protect_app_abroad_des, unProtectedAppNum, new Object[]{Integer.valueOf(unProtectedAppNum)}));
                } else {
                    PowerManagerFragment.this.mRunningSummary.setText(PowerManagerFragment.this.getResources().getQuantityString(R.plurals.power_protect_app_des, protectedAppsNum, new Object[]{Integer.valueOf(protectedAppsNum)}));
                }
            }
            PowerManagerFragment.this.mProtectAsynLoader = null;
        }
    }

    private class SleepTimeOutObserver extends ContentObserver {
        public SleepTimeOutObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HwLog.i(PowerManagerFragment.TAG, "SleepTimeOutObserver");
            PowerManagerFragment.this.getAvaliableTime();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        this.mActivity = getActivity();
        this.mAppContext = this.mActivity.getApplicationContext();
        HwLog.i(TAG, "PowerManagerFragment is created");
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mBatteryTimeHandler = new Handler(this.mHandlerThread.getLooper());
        if (savedInstanceState != null) {
            HwLog.i(TAG, "mSuperSaveModeChecked befor =" + this.mSuperSaveModeChecked);
            this.mSuperSaveModeChecked = savedInstanceState.getBoolean(SUPER_SAVE_MODE_CHECKED);
            HwLog.i(TAG, "mSuperSaveModeChecked after =" + this.mSuperSaveModeChecked);
        }
        registerIntent();
        registerDarkThemeIntent();
        getPowerManager(this.mAppContext);
        this.mHandler.postDelayed(this.mObserve, 500);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        HwLog.i(TAG, "mSuperSaveModeChecked on SaveInstanceState=" + this.mSuperSaveModeChecked);
        outState.putBoolean(SUPER_SAVE_MODE_CHECKED, this.mSuperSaveModeChecked);
    }

    private static synchronized void getPowerManager(Context context) {
        synchronized (PowerManagerFragment.class) {
            mPowerManager = PowerManagementModel.getInstance(context);
        }
    }

    private static synchronized PowerManagementModel getPowerManager() {
        PowerManagementModel powerManagementModel;
        synchronized (PowerManagerFragment.class) {
            powerManagementModel = mPowerManager;
        }
        return powerManagementModel;
    }

    public void setPhoneService(boolean autoStarted, boolean fromPhoneService) {
        this.mIsAutoStarted = autoStarted;
        this.mIsFromPhoneService = fromPhoneService;
    }

    private void registerObservers() {
        if (!this.mObserved) {
            Handler handler = new Handler();
            this.mBrightnessModeObserver = new BrightnessModeObserver(handler);
            this.mBrightnessObserver = new BrightnessObserver(handler);
            this.mMobileDataObserver = new MobileDataObserver(handler);
            this.mGPSObserver = new GPSObserver(handler);
            this.mSleepTimeOutObserver = new SleepTimeOutObserver(handler);
            this.mFeedBackObserver = new FeedBackObserver(handler);
            this.mPowerModeObserver = new PowerModeObserver(handler);
            this.mAppContext.getContentResolver().registerContentObserver(System.getUriFor("screen_brightness_mode"), true, this.mBrightnessModeObserver);
            this.mAppContext.getContentResolver().registerContentObserver(System.getUriFor("screen_brightness"), true, this.mBrightnessObserver);
            this.mAppContext.getContentResolver().registerContentObserver(System.getUriFor("screen_auto_brightness"), true, this.mBrightnessObserver);
            if (isMulityCard(this.mAppContext)) {
                HwLog.d(TAG, " multi card register");
                this.mAppContext.getContentResolver().registerContentObserver(Global.getUriFor("mobile_data0"), true, this.mMobileDataObserver);
                this.mAppContext.getContentResolver().registerContentObserver(Global.getUriFor("mobile_data1"), true, this.mMobileDataObserver);
            } else {
                HwLog.d(TAG, " single card register");
                this.mAppContext.getContentResolver().registerContentObserver(Global.getUriFor("mobile_data"), true, this.mMobileDataObserver);
            }
            this.mAppContext.getContentResolver().registerContentObserver(Global.getUriFor("airplane_mode_on"), true, this.mMobileDataObserver);
            this.mAppContext.getContentResolver().registerContentObserver(Secure.getUriFor("location_providers_allowed"), true, this.mGPSObserver);
            this.mStatusChangeListenerHandle = ContentResolver.addStatusChangeListener(1, this.mSyncStatusObserver);
            this.mAppContext.getContentResolver().registerContentObserver(System.getUriFor("screen_off_timeout"), true, this.mSleepTimeOutObserver);
            this.mAppContext.getContentResolver().registerContentObserver(System.getUriFor("haptic_feedback_enabled"), true, this.mFeedBackObserver);
            this.mAppContext.getContentResolver().registerContentObserver(System.getUriFor(ApplicationConstant.SMART_MODE_STATUS), true, this.mPowerModeObserver);
            RotationPolicy.registerRotationPolicyListener(this.mAppContext, this.mRotationPolicyListener, -1);
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.media.RINGER_MODE_CHANGED");
            this.mActivity.registerReceiver(mVibrateChangedReceiver, filter);
            this.mActivity.registerReceiver(mWifiReceiver, new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED"));
            this.mActivity.registerReceiver(mBTReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
            this.mObserved = true;
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mInflater = inflater;
        this.fragmentView = this.mInflater.inflate(R.layout.power_manager_main, null);
        findControllerView();
        findConsumeView();
        addModeAndSwitchFragment();
        setOnClickListener();
        return this.fragmentView;
    }

    private void registerDarkThemeIntent() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActionConst.INTENT_CHANGE_POWER_SAVE_THEME_SELF);
        this.mActivity.registerReceiver(this.mDarkThemeChangerReceiver, intentFilter, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
        this.isDarkThemeBroadCastRegister = true;
    }

    private void doDarkSwitchChange(int changeUiAction) {
        switch (changeUiAction) {
            case 0:
                getAvaliableTime();
                enableDarkSwitch();
                return;
            case 1:
                failChangeTheme(false);
                return;
            case 2:
                failChangeTheme(true);
                return;
            default:
                return;
        }
    }

    private void disableDarkSwitch() {
        if (this.mDarkThemeLayout != null) {
            this.mDarkThemeLayout.setEnabled(false);
        }
        if (this.mDarkThemeControlSwitch != null) {
            this.mDarkThemeControlSwitch.setEnabled(false);
        }
        if (this.mDarkThemeProgressBar != null) {
            this.mDarkThemeProgressBar.setVisibility(0);
        }
        if (this.mDarkThemeSummary != null) {
            this.mDarkThemeSummary.setText(R.string.dark_theme_change_loading);
        }
    }

    private void enableDarkSwitch() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if (PowerManagerFragment.this.mDarkThemeLayout != null) {
                    PowerManagerFragment.this.mDarkThemeLayout.setEnabled(true);
                }
                if (PowerManagerFragment.this.mDarkThemeControlSwitch != null) {
                    PowerManagerFragment.this.mDarkThemeControlSwitch.setEnabled(true);
                }
                if (PowerManagerFragment.this.mDarkThemeProgressBar != null) {
                    PowerManagerFragment.this.mDarkThemeProgressBar.setVisibility(8);
                }
                if (PowerManagerFragment.this.mDarkThemeSummary != null) {
                    PowerManagerFragment.this.mDarkThemeSummary.setText(R.string.dark_theme_switch_summary);
                }
            }
        });
    }

    private void failChangeTheme(final boolean isThemeLack) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                boolean z = true;
                PowerManagerFragment.this.enableDarkSwitch();
                PowerManagerFragment.this.isFailCheck = true;
                if (PowerManagerFragment.this.mDarkThemeControlSwitch != null) {
                    Switch -get10 = PowerManagerFragment.this.mDarkThemeControlSwitch;
                    if (PowerManagerFragment.this.mDarkThemeControlChecked) {
                        z = false;
                    }
                    -get10.setChecked(z);
                }
                if (isThemeLack) {
                    PowerManagerFragment.this.mDarkThemeSummary.setText(R.string.dark_theme_lack_summary);
                } else {
                    PowerManagerFragment.this.mDarkThemeSummary.setText(R.string.dark_theme_change_fail);
                }
                if (PowerManagerFragment.this.mDarkThemeProgressBar != null) {
                    PowerManagerFragment.this.mDarkThemeProgressBar.setVisibility(8);
                }
                PowerManagerFragment.this.mHandler.removeMessages(8);
                PowerManagerFragment.this.mHandler.sendMessageDelayed(PowerManagerFragment.this.mHandler.obtainMessage(8, null), 60000);
            }
        });
    }

    private void findConsumeView() {
        HwLog.d(TAG, "findConsumeView()");
        this.mRunningScreenOff = (RelativeLayout) this.fragmentView.findViewById(R.id.running_screen_off_layout);
        this.mRunningScreenOff.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                Bundle data = new Bundle();
                data.putString("comefrom", ProtectActivity.POWER);
                intent.putExtras(data);
                intent.setClass(PowerManagerFragment.this.mAppContext, ProtectActivity.class);
                PowerManagerFragment.this.startActivity(intent);
                String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "2");
                HsmStat.statE((int) Events.E_ENTER_PROTECTEDAPP, statParam);
            }
        });
        this.mRunningSummary = (TextView) this.fragmentView.findViewById(R.id.summary_textview);
        this.mBkgConsume = (RelativeLayout) this.fragmentView.findViewById(R.id.background_consume);
        this.mBkgConsume.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                PowerManagerFragment.this.startActivity(new Intent(PowerManagerFragment.this.mAppContext, BackgroundConsumeActivity.class));
                HsmStat.statE(24);
            }
        });
        this.mConsumeLevel = (RelativeLayout) this.fragmentView.findViewById(R.id.consume_level);
        this.mConsumeLevel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                PowerManagerFragment.this.startActivity(new Intent(PowerManagerFragment.this.mAppContext, ConsumeLevelActivity.class));
                HsmStat.statE(26);
            }
        });
        this.mConsumeBatteryPercentSwitch = (Switch) this.fragmentView.findViewById(R.id.battery_percent_switch);
        this.mConsumeBatteryPercentDes = (TextView) this.fragmentView.findViewById(R.id.battery_percent_des);
        this.mSaveModeLayout = (RelativeLayout) this.fragmentView.findViewById(R.id.save_mode_layout);
        this.mSaveModeTimeTv = (TextView) this.fragmentView.findViewById(R.id.save_mode_remain_time);
        this.mSaveModeSwitch = (Switch) this.fragmentView.findViewById(R.id.save_mode_switch);
        this.mSaveModeLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (PowerManagerFragment.this.mSaveModeSwitch.isEnabled()) {
                    PowerManagerFragment.this.mSaveModeSwitch.setChecked(!PowerManagerFragment.this.mSaveModeChecked);
                }
            }
        });
        this.mSaveModeSwitch.setOnCheckedChangeListener(null);
        this.mSuperSaveModeLayout = (RelativeLayout) this.fragmentView.findViewById(R.id.super_save_mode_layout);
        if (!Utility.superPowerEntryEnable() || Utility.isWifiOnlyMode() || Utility.isDataOnlyMode()) {
            this.fragmentView.findViewById(R.id.power_list_seperate_super).setVisibility(8);
            this.mSuperSaveModeLayout.setVisibility(8);
        }
        this.mSuperSaveModeTimeTv = (TextView) this.fragmentView.findViewById(R.id.super_save_mode_remain_time);
        this.mSuperModeSwitch = (Switch) this.fragmentView.findViewById(R.id.super_save_mode_switch);
        this.mSuperSaveModeLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                boolean z = false;
                if (PowerManagerFragment.this.mSuperModeSwitch.isEnabled()) {
                    boolean z2;
                    String str = PowerManagerFragment.TAG;
                    StringBuilder append = new StringBuilder().append("mSuperSaveModeLayout is clicked and switch turn to ");
                    if (PowerManagerFragment.this.mSuperSaveModeChecked) {
                        z2 = false;
                    } else {
                        z2 = true;
                    }
                    HwLog.i(str, append.append(z2).toString());
                    Switch -get23 = PowerManagerFragment.this.mSuperModeSwitch;
                    if (!PowerManagerFragment.this.mSuperSaveModeChecked) {
                        z = true;
                    }
                    -get23.setChecked(z);
                }
            }
        });
        this.mSuperModeSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                HwLog.i(PowerManagerFragment.TAG, "isChecked=" + isChecked);
                if (PowerManagerFragment.this.mSuperSaveModeChecked != isChecked) {
                    String[] strArr = new String[2];
                    strArr[0] = HsmStatConst.PARAM_OP;
                    strArr[1] = isChecked ? "1" : "0";
                    HsmStat.statE((int) Events.E_POWER_SUPERPOWERMODE_SWITCH_STATUS, HsmStatConst.constructJsonParams(strArr));
                    if (isChecked) {
                        HwLog.i(PowerManagerFragment.TAG, "mSuperModeSwitch is clicked ");
                        PowerModeDialogControl.showSuperModeDialog(PowerManagerFragment.this.getActivity(), PowerManagerFragment.this.mSuperModeDialogClickListener, PowerManagerFragment.this.mCancelListener);
                    }
                    PowerManagerFragment.this.mSuperSaveModeChecked = isChecked;
                }
            }
        });
        this.mSuperSaveModeDes = (TextView) this.fragmentView.findViewById(R.id.super_save_mode_des);
        this.mConsumeBatteryPercent = (RelativeLayout) this.fragmentView.findViewById(R.id.consume_battery_percent);
        this.mConsumeBatteryPercent.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (PowerManagerFragment.this.mConsumeBatteryPercentSwitch.isEnabled()) {
                    PowerManagerFragment.this.mConsumeBatteryPercentSwitch.setChecked(!PowerManagerFragment.this.mIsBatteryPercent);
                }
            }
        });
        this.mConsumeBatteryPercentSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (PowerManagerFragment.this.mIsBatteryPercent != isChecked) {
                    int i;
                    ContentResolver contentResolver = PowerManagerFragment.this.getActivity().getContentResolver();
                    String str = "battery_percent_switch";
                    if (isChecked) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    System.putInt(contentResolver, str, i);
                    String[] strArr = new String[2];
                    strArr[0] = HsmStatConst.PARAM_OP;
                    strArr[1] = isChecked ? "1" : "0";
                    HsmStat.statE((int) Events.E_POWER_BATTERY_PERCENT, HsmStatConst.constructJsonParams(strArr));
                    PowerManagerFragment.this.mIsBatteryPercent = isChecked;
                }
            }
        });
        this.mCheckAll = (RelativeLayout) this.fragmentView.findViewById(R.id.consume_checkAll);
        this.mScreenSaveLayout = (RelativeLayout) this.fragmentView.findViewById(R.id.save_screen_layout);
        this.mLowResolutionControlLayout = (RelativeLayout) this.fragmentView.findViewById(R.id.rog_control_layout);
        this.mLowResolutionControlSwitch = (Switch) this.fragmentView.findViewById(R.id.rog_control_switch);
        this.mLowResolutionControlLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                PowerManagerFragment.this.mLowResolutionControlSwitch.setChecked(!PowerManagerFragment.this.mLowResolutionControlChecked);
            }
        });
        this.mLowResolutionControlSwitch.setOnCheckedChangeListener(null);
        this.mDarkThemeLayout = (RelativeLayout) this.fragmentView.findViewById(R.id.dark_theme_layout);
        this.mDarkThemeControlSwitch = (Switch) this.fragmentView.findViewById(R.id.dark_theme_control_switch);
        this.mDarkThemeProgressBar = (ProgressBar) this.fragmentView.findViewById(R.id.dark_theme_control_progress);
        this.mDarkThemeName = (TextView) this.fragmentView.findViewById(R.id.dark_theme_control_name);
        this.mDarkThemeSummary = (TextView) this.fragmentView.findViewById(R.id.dark_theme_control_summary);
        this.mDarkThemeOnePx = this.fragmentView.findViewById(R.id.dark_theme_one_px);
        if (!SharePrefWrapper.getPrefValue(this.mAppContext, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.DARK_THEME_SWITCH_KEY, true)) {
            disableDarkSwitch();
        }
        this.mDarkThemeControlChecked = System.getIntForUser(this.mAppContext.getContentResolver(), DarkThemeChanageService.DB_DARK_THEME, 0, -2) != 0;
        this.mDarkThemeControlSwitch.setChecked(this.mDarkThemeControlChecked);
        this.mDarkThemeLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (PowerManagerFragment.this.mDarkThemeControlSwitch.isChecked()) {
                    PowerManagerFragment.this.mDarkThemeControlSwitch.setChecked(false);
                    HwLog.i(PowerManagerFragment.TAG, "mDarkThemeControlSwitch.isChecked() = " + PowerManagerFragment.this.mDarkThemeControlSwitch.isChecked());
                    return;
                }
                PowerManagerFragment.this.mDarkThemeControlSwitch.setChecked(true);
                HwLog.i(PowerManagerFragment.TAG, "mDarkThemeControlSwitch.isChecked() = " + PowerManagerFragment.this.mDarkThemeControlSwitch.isChecked());
            }
        });
        this.mDarkThemeControlSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (PowerManagerFragment.this.mDarkThemeControlChecked != isChecked) {
                    HwLog.i(PowerManagerFragment.TAG, "dark theme switch checked ==" + isChecked);
                    String[] strArr = new String[2];
                    strArr[0] = HsmStatConst.PARAM_OP;
                    strArr[1] = isChecked ? "1" : "0";
                    HsmStat.statE((int) Events.E_POWER_DARK_THEME_SWITCH, HsmStatConst.constructJsonParams(strArr));
                    PowerManagerFragment.this.mDarkThemeControlChecked = isChecked;
                    if (PowerManagerFragment.this.isFailCheck) {
                        PowerManagerFragment.this.isFailCheck = false;
                        HwLog.i(PowerManagerFragment.TAG, "Fail to change check without doing anything");
                        return;
                    }
                    PowerManagerFragment.this.disableDarkSwitch();
                    Intent intent = new Intent(PowerManagerFragment.this.mActivity, DarkThemeChanageService.class);
                    intent.putExtra(DarkThemeChanageService.DARK_THEME_IS_CHECK_NAME, isChecked);
                    PowerManagerFragment.this.mAppContext.startServiceAsUser(intent, UserHandle.CURRENT);
                }
            }
        });
        this.bgConsumeAppsTv = (TextView) this.fragmentView.findViewById(R.id.bgconsumeAppsNum);
    }

    private synchronized void initModel() {
        if (!this.mLoadedStats) {
            try {
                synchronized (PowerManagerFragment.class) {
                    mPowerManager = PowerManagementModel.getInstance(this.mAppContext).load();
                }
                this.mLoadedStats = true;
            } catch (IllegalArgumentException e) {
                HwLog.e(TAG, "PowerManagementModel init failed! ");
            }
        }
    }

    public int getRawLevel() {
        return this.mRawlevel;
    }

    private void updateBatteryPercentSwitch() {
        boolean z;
        if (System.getInt(getActivity().getContentResolver(), "battery_percent_switch", 0) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mIsBatteryPercent = z;
        this.mConsumeBatteryPercentSwitch.setChecked(this.mIsBatteryPercent);
        if (this.mSaveModeChecked) {
            this.mConsumeBatteryPercentDes.setText(R.string.power_save_mode_battery_percent_des);
            this.mConsumeBatteryPercentSwitch.setEnabled(false);
            return;
        }
        this.mConsumeBatteryPercentSwitch.setEnabled(true);
        this.mConsumeBatteryPercentDes.setText(R.string.consume_battery_percent_summary);
    }

    private void initProtectAppsInfo() {
        if (this.mProtectAsynLoader == null && getActivity() != null) {
            this.mProtectAsynLoader = new ProtectDataAsyncLoader();
            this.mProtectAsynLoader.execute(new Void[0]);
        }
    }

    public void onResume() {
        super.onResume();
        initProtectAppsInfo();
        initBgConsumeAppsNum();
        if (!this.isregister) {
            registerIntent();
        }
        if (!this.isDarkThemeBroadCastRegister) {
            registerDarkThemeIntent();
        }
        HwLog.e(TAG, "Activity OnResume and mPlugged is " + getmPlugged() + ",mBatteryStatus = " + this.mBatteryStatus);
        if (getmPlugged() != 0) {
            expectFullTimeShow();
        }
        getAvaliableTime();
        if (this.mSaveModeCheckListener != null) {
            this.mSaveModeSwitch.setOnCheckedChangeListener(this.mSaveModeCheckListener);
        }
        initModeSwitch();
        if (this.mIsFromPhoneService && !this.mIsAutoStarted) {
            this.mCheckAll.performClick();
            this.mIsAutoStarted = true;
        }
        if (SysCoreUtils.isLowResolutionSupported()) {
            this.mLowResolutionControlLayout.setVisibility(0);
            this.mLowResolutionControlChecked = SysCoreUtils.getLowResolutionSwitchState(this.mAppContext);
            this.mLowResolutionControlSwitch.setChecked(this.mLowResolutionControlChecked);
            if (this.mLowResolutionSwitchCheckListener != null) {
                this.mLowResolutionControlSwitch.setOnCheckedChangeListener(this.mLowResolutionSwitchCheckListener);
            }
        } else {
            this.mLowResolutionControlLayout.setVisibility(8);
        }
        if (!(SysCoreUtils.isLowResolutionSupported() || SysCoreUtils.isAmoledPanel())) {
            this.mScreenSaveLayout.setVisibility(8);
        }
        if (SysCoreUtils.isAmoledPanel()) {
            this.mDarkThemeLayout.setVisibility(0);
            if (SysCoreUtils.isLowResolutionSupported()) {
                this.mDarkThemeOnePx.setVisibility(0);
                return;
            } else {
                this.mDarkThemeOnePx.setVisibility(8);
                return;
            }
        }
        this.mDarkThemeLayout.setVisibility(8);
        this.mDarkThemeOnePx.setVisibility(8);
    }

    private void initBgConsumeAppsNum() {
        if (this.mAsyncLoader == null && getActivity() != null) {
            this.mAsyncLoader = new DataAsyncLoader();
            this.mAsyncLoader.execute(new Void[0]);
        }
    }

    private void initModeSwitch() {
        boolean z;
        if (PowerModeControl.getInstance(this.mAppContext).readSaveMode() == 4) {
            z = true;
        } else {
            z = false;
        }
        this.mSaveModeChecked = z;
        this.mSuperSaveModeChecked = SystemProperties.getBoolean("sys.super_power_save", false);
        HwLog.i(TAG, "initModeSwitch mSuperSaveModeChecked= " + this.mSuperSaveModeChecked);
        this.mSuperModeSwitch.setChecked(this.mSuperSaveModeChecked);
        HwLog.i(TAG, "initModeSwitch mSaveModeChecked= " + this.mSaveModeChecked);
        if (!this.mSaveModeSwitch.isEnabled()) {
            this.mSaveModeSwitch.setEnabled(true);
            HwLog.i(TAG, "initModeSwitch mSaveModeSwitch setEnabled to true");
        }
        this.mSaveModeSwitch.setChecked(this.mSaveModeChecked);
        if (Utility.isOwnerUser(false)) {
            this.mSuperModeSwitch.setEnabled(true);
            this.mSuperSaveModeDes.setText(R.string.super_power_summmery_upgrade);
        } else {
            this.mSuperModeSwitch.setEnabled(false);
            this.mSuperSaveModeDes.setText(R.string.alert_toast_multi_users);
        }
        if (this.mSuperSaveModeChecked) {
            this.mSaveModeSwitch.setEnabled(false);
            this.mSuperModeSwitch.setEnabled(false);
        }
        updateBatteryPercentSwitch();
    }

    private int getCurrentSaveMode() {
        if (SystemProperties.getBoolean("sys.super_power_save", false)) {
            return 3;
        }
        return PowerModeControl.getInstance(this.mAppContext).readSaveMode();
    }

    public void onDestroy() {
        if (this.isregister) {
            this.mActivity.unregisterReceiver(this.mBatInfoReceiver);
            this.isregister = false;
        }
        if (this.isDarkThemeBroadCastRegister) {
            this.mActivity.unregisterReceiver(this.mDarkThemeChangerReceiver);
            this.isDarkThemeBroadCastRegister = false;
        }
        unRegisterObservers();
        if (this.mHandlerThread != null) {
            this.mHandlerThread.quitSafely();
        }
        if (this.mAsyncLoader != null) {
            this.mAsyncLoader.cancel(false);
            this.mAsyncLoader = null;
        }
        super.onDestroy();
    }

    public void onPause() {
        this.mHandler.removeCallbacks(this.mObserve);
        if (this.isregister) {
            this.mActivity.unregisterReceiver(this.mBatInfoReceiver);
            this.isregister = false;
        }
        if (this.isDarkThemeBroadCastRegister) {
            this.mActivity.unregisterReceiver(this.mDarkThemeChangerReceiver);
            this.isDarkThemeBroadCastRegister = false;
        }
        super.onPause();
    }

    private void findControllerView() {
        this.mbatteryFL = (PowerWaveView) this.fragmentView.findViewById(R.id.circle);
        this.mBatteryLayout = (RelativeLayout) this.fragmentView.findViewById(R.id.power_battery_layout);
        this.mbatteryView = (RollingView) this.fragmentView.findViewById(R.id.bettary_charge_textview);
        this.mEduTimeView = (TextView) this.fragmentView.findViewById(R.id.edutime_textview);
        this.mDetailListArrow = (ImageView) this.fragmentView.findViewById(R.id.detail_list_arrow);
        this.mPercentView = (SimpleTextView) this.fragmentView.findViewById(R.id.percent_textview);
        this.mChargingView = (TextView) this.fragmentView.findViewById(R.id.bettary_charging_textview);
        this.mModeRL = (LinearLayout) this.fragmentView.findViewById(R.id.power_mode_switch);
        this.mDetailListArrow.setOnClickListener(this.mPowerDetailClickListener);
        this.mBatteryLayout.setOnClickListener(this.mPowerDetailClickListener);
        this.mUpperView = (LinearLayout) this.fragmentView.findViewById(R.id.sliding_layout_upperview);
        int upperLayoutHeight = (int) ((((double) SysCoreUtils.getScreenWidth(this.mAppContext)) * 0.5625d) - ((double) this.mAppContext.getResources().getDimensionPixelSize(R.dimen.battery_history_actionBar_height)));
        LayoutParams params = (LayoutParams) this.mUpperView.getLayoutParams();
        HwLog.i(TAG, "upperLayoutHeight=" + upperLayoutHeight);
        params.height = upperLayoutHeight;
    }

    private void doPowerDetailClick() {
        if (this.mBatteryTimeHandler != null) {
            HwLog.d(TAG, "doPowerDetailClick start");
            HwLog.i(TAG, "PowerDetail Click, execute mCurrModeAvaliableTimeRunnable");
            this.mExecutor.execute(this.mCurrModeAvaliableTimeRunnable);
            HwLog.d(TAG, "doPowerDetailClick over");
        }
    }

    private void unRegisterObservers() {
        this.mHandler.removeCallbacks(this.mObserve);
        if (this.mObserved) {
            this.mAppContext.getContentResolver().unregisterContentObserver(this.mBrightnessModeObserver);
            this.mAppContext.getContentResolver().unregisterContentObserver(this.mBrightnessObserver);
            this.mAppContext.getContentResolver().unregisterContentObserver(this.mMobileDataObserver);
            this.mAppContext.getContentResolver().unregisterContentObserver(this.mGPSObserver);
            this.mAppContext.getContentResolver().unregisterContentObserver(this.mSleepTimeOutObserver);
            this.mAppContext.getContentResolver().unregisterContentObserver(this.mPowerModeObserver);
            ContentResolver.removeStatusChangeListener(this.mStatusChangeListenerHandle);
            this.mAppContext.getContentResolver().unregisterContentObserver(this.mFeedBackObserver);
            RotationPolicy.unregisterRotationPolicyListener(this.mAppContext, this.mRotationPolicyListener);
            this.mActivity.unregisterReceiver(mVibrateChangedReceiver);
            this.mActivity.unregisterReceiver(mWifiReceiver);
            this.mActivity.unregisterReceiver(mBTReceiver);
            this.mObserved = false;
        }
    }

    private void setOnClickListener() {
        this.mCheckAll.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(PowerManagerFragment.this.mActivity, PowerWholeCheckActivity.class);
                PowerManagerFragment.this.startActivity(intent);
                HsmStat.statE(18);
            }
        });
    }

    private void handleRefreshNumber(int hour, int min) {
        if (-1 != hour) {
            this.mHour = hour;
        }
        if (-1 != min) {
            this.mMin = min;
        }
        if (this.mMin < 0) {
            this.mMin = 0;
        }
        TextView textView;
        String string;
        Object[] objArr;
        if (getmPlugged() == 0 || !SysCoreUtils.isCharging(this.mBatteryStatus)) {
            if (this.mHour <= 0) {
                this.mEduTimeView.setText(getCombinableString(this.mAppContext, R.plurals.Other_SystemManager_Tip02_array, this.mMin));
                return;
            }
            textView = this.mEduTimeView;
            string = this.mAppContext.getResources().getString(R.string.Other_SystemManager_Tip01_connect);
            objArr = new Object[2];
            objArr[0] = this.mAppContext.getResources().getQuantityString(R.plurals.power_time_hour_array, this.mHour, new Object[]{Integer.valueOf(this.mHour)});
            objArr[1] = this.mAppContext.getResources().getQuantityString(R.plurals.power_time_min_array, this.mMin, new Object[]{Integer.valueOf(this.mMin)});
            textView.setText(String.format(string, objArr));
        } else if (this.mHour <= 0) {
            this.mEduTimeView.setText(this.mAppContext.getResources().getQuantityString(R.plurals.Other_SystemManager_Tip04_array, this.mMin, new Object[]{Utility.getLocaleNumber(this.mMin)}));
        } else {
            textView = this.mEduTimeView;
            string = this.mAppContext.getResources().getString(R.string.Other_SystemManager_Tip03_connect);
            objArr = new Object[2];
            objArr[0] = this.mAppContext.getResources().getQuantityString(R.plurals.power_time_hour_array, this.mHour, new Object[]{Integer.valueOf(this.mHour)});
            objArr[1] = this.mAppContext.getResources().getQuantityString(R.plurals.power_time_min_array, this.mMin, new Object[]{Integer.valueOf(this.mMin)});
            textView.setText(String.format(string, objArr));
        }
    }

    private String getCombinableString(Context context, int stringId, int singleNum) {
        return context.getResources().getQuantityString(stringId, singleNum, new Object[]{Utility.getLocaleNumber(singleNum)});
    }

    private void handleAvaliableTimeOk() {
        String mModeInString = ApplicationConstant.SMART_MODE_KEY;
        switch (getCurrentSaveMode()) {
            case 0:
                mModeInString = ApplicationConstant.SMART_MODE_KEY;
                break;
            case 1:
                mModeInString = ApplicationConstant.SMART_MODE_KEY;
                break;
            case 4:
                mModeInString = ApplicationConstant.SAVE_MODE_KEY;
                break;
            default:
                mModeInString = ApplicationConstant.SUPER_MODE_POWER_KEY;
                break;
        }
        double currentModeLeftTime = Double.parseDouble(mTimeByCurrentBattery.get(mModeInString).toString());
        if (currentModeLeftTime >= 0.0d) {
            HwLog.e(TAG, "currentModeLeftTime is " + currentModeLeftTime + " mRawlevel is " + this.mRawlevel);
            int[] mHourandMin = convertDoubleTimeToSeprateInt(currentModeLeftTime);
            HwLog.e(TAG, " hour is " + mHourandMin[0] + " min is " + mHourandMin[1]);
            if (getmPlugged() == 0 || !SysCoreUtils.isCharging(this.mBatteryStatus)) {
                numberFreshAnimation(mHourandMin[0], mHourandMin[1]);
            }
        }
    }

    private int[] convertDoubleTimeToSeprateInt(double mTime) {
        int[] mHourMin = new int[]{0, 0};
        if (mTime < 0.0d) {
            return mHourMin;
        }
        mHourMin[0] = (int) (mTime / 60.0d);
        mHourMin[1] = (int) (mTime % 60.0d);
        return mHourMin;
    }

    private void numberFreshAnimation(int finalHour, int finalMin) {
        HwLog.e(TAG, "tempNowHour is" + this.mHour + "; tempNowMin is" + this.mMin + " ;finalHour is" + finalHour + " ;finalMin is" + finalMin);
        this.mHandler.removeMessages(5);
        if (this.mHour == -1 && this.mMin == -1) {
            handleRefreshNumber(finalHour, finalMin);
            return;
        }
        int i;
        int gapHour = Math.abs(this.mHour - finalHour);
        int gapMin = Math.abs(this.mMin - finalMin);
        int showHour = this.mHour;
        int showMin = this.mMin;
        for (i = 0; i < gapMin; i++) {
            if (this.mMin < finalMin) {
                showMin++;
            } else {
                showMin--;
            }
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(5, -1, showMin), (long) ((TimeConst.TEN_HOURS / gapMin) * i));
        }
        for (i = 0; i < gapHour; i++) {
            if (this.mHour < finalHour) {
                showHour++;
            } else {
                showHour--;
            }
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(5, showHour, -1), (long) ((TimeConst.TEN_HOURS / gapHour) * i));
        }
    }

    public void addModeAndSwitchFragment() {
        setPowerSaveStatus(false);
        this.mModeRL.setVisibility(0);
        setChargeOrBattery();
        this.mEduTimeView.setVisibility(0);
    }

    public static void setPowerSaveStatus(boolean status) {
        isPowerSaveState = status;
    }

    public static boolean getPowerSaveStatus() {
        return isPowerSaveState;
    }

    public void registerIntent() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        intentFilter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        intentFilter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.media.RINGER_MODE_CHANGED");
        intentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
        intentFilter.addAction(PowerModeControl.CHANGE_MODE_ACTION);
        Intent batteryStatus = this.mActivity.registerReceiver(this.mBatInfoReceiver, intentFilter);
        this.isregister = true;
        setmPlugged(batteryStatus.getIntExtra("plugged", 0));
        this.mRawlevel = batteryStatus.getIntExtra("level", 0);
        this.mBatteryStatus = batteryStatus.getIntExtra("status", 1);
        HwLog.i(TAG, "registerIntent mBatteryStatus = " + this.mBatteryStatus);
    }

    private void updateExpectedChargingView(double leftTime) {
        HwLog.e(TAG, " updateExpectedChargingView mBatteryStatus = " + this.mBatteryStatus);
        if (!SysCoreUtils.isCharging(this.mBatteryStatus)) {
            getAvaliableTimeWithPlugged();
        } else if (leftTime <= 5.0d && this.mRawlevel != 100) {
            this.mChargingView.setVisibility(0);
            this.mChargingView.setText(this.mAppContext.getResources().getString(R.string.Other_SystemManager_Charging));
            this.mbatteryView.setText(getNumber(this.mRawlevel));
            this.mEduTimeView.setText(String.format(this.mAppContext.getResources().getString(R.string.Other_SystemManager_Tip05_new), new Object[]{Integer.valueOf(5)}));
        } else if (this.mRawlevel == 100) {
            this.mbatteryView.setVisibility(0);
            this.mPercentView.setVisibility(0);
            this.mChargingView.setVisibility(8);
            HwLog.e(TAG, "estimateExpectFullTimeShow charging is full!");
            this.mbatteryView.setText(getNumber(100));
            this.mEduTimeView.setText(this.mAppContext.getString(R.string.charing_full_already));
        } else {
            this.mChargingView.setVisibility(0);
            this.mChargingView.setText(this.mAppContext.getResources().getString(R.string.Other_SystemManager_Charging));
            this.mbatteryView.setText(getNumber(this.mRawlevel));
            double mTimeInHour = leftTime / 60.0d;
            int hour = (int) mTimeInHour;
            int min = (int) ((mTimeInHour - ((double) hour)) * 60.0d);
            HwLog.e(TAG, "mTimeInHour is " + mTimeInHour + " hour is " + hour + " min is " + min);
            numberFreshAnimation(hour, min);
        }
    }

    private int getExpectedFullTime() {
        int leftTime;
        initModel();
        long start = SystemClock.elapsedRealtime();
        if (BatteryInfo.isHisic()) {
            HwLog.i(TAG, "getExpectedFullTime isHisic = true");
            leftTime = ((Integer) ChargeInfo.getTimeFullyChargedByHisic(this.mAppContext, getmPlugged()).get("full_time")).intValue();
        } else {
            HwLog.i(TAG, "getExpectedFullTime isHisic = false");
            leftTime = ChargeInfo.getTimeFullyCharged(this.mAppContext, getmPlugged());
        }
        HwLog.e(TAG, "mChargingLeftTime is " + leftTime + " mRawlevel is " + this.mRawlevel);
        HwLog.i(TAG, "getExpectedFullTime cost time = " + (SystemClock.elapsedRealtime() - start));
        return leftTime;
    }

    private void expectFullTimeShow() {
        if (this.mBatteryTimeHandler != null) {
            HwLog.i(TAG, "expectFullTimeShow, post mExpectedChargeTime");
            this.mBatteryTimeHandler.removeCallbacks(this.mExpectedChargeTime);
            this.mBatteryTimeHandler.post(this.mExpectedChargeTime);
        }
    }

    private void getAvaliableTime() {
        if (getmPlugged() == 0) {
            this.mbatteryView.setVisibility(0);
            this.mPercentView.setVisibility(0);
            this.mChargingView.setVisibility(8);
            this.mbatteryView.setText(getNumber(this.mRawlevel));
        }
        if (this.mBatteryTimeHandler != null) {
            HwLog.i(TAG, "getAvaliableTime, post mAvaliableTimeRunnable");
            this.mBatteryTimeHandler.removeCallbacks(this.mAvaliableTimeRunnable);
            this.mBatteryTimeHandler.post(this.mAvaliableTimeRunnable);
        }
    }

    private void getAvaliableTimeWithPlugged() {
        if (getmPlugged() == 0 || !SysCoreUtils.isCharging(this.mBatteryStatus)) {
            this.mbatteryView.setVisibility(0);
            this.mPercentView.setVisibility(0);
            this.mChargingView.setVisibility(8);
            this.mbatteryView.setText(getNumber(this.mRawlevel));
        }
        if (this.mBatteryTimeHandler != null) {
            HwLog.i(TAG, "getAvaliableTimeWithoutCharger, post mAvaliableTimeRunnable");
            this.mBatteryTimeHandler.removeCallbacks(this.mAvaliableTimeRunnable);
            this.mBatteryTimeHandler.post(this.mAvaliableTimeRunnable);
        }
    }

    public static int getmPlugged() {
        return mPlugged;
    }

    public static void setmPlugged(int mPlugged) {
        mPlugged = mPlugged;
    }

    private void setModeAvaliableTime() {
        this.mAllModeLeftTimeList.clear();
        this.mAllModeLeftTimeList.add(Double.valueOf(Double.parseDouble(mTimeByCurrentBattery.get(ApplicationConstant.SAVE_MODE_KEY).toString())));
        this.mAllModeLeftTimeList.add(Double.valueOf(Double.parseDouble(mTimeByCurrentBattery.get(ApplicationConstant.SUPER_MODE_POWER_KEY).toString())));
        String[] allModeLeftTimeText = new String[2];
        for (int i = 0; i < 2; i++) {
            String mTimeForShow;
            HwLog.d(TAG, "available time is " + this.mAllModeLeftTimeList.get(i) + " mRawlevel is " + this.mRawlevel);
            int[] mHourAndMin = convertDoubleTimeToSeprateInt(((Double) this.mAllModeLeftTimeList.get(i)).doubleValue());
            HwLog.d(TAG, "hour is " + mHourAndMin[0] + " min is " + mHourAndMin[1]);
            if (mHourAndMin[0] == 0) {
                mTimeForShow = getCombinableString(this.mAppContext, R.plurals.Other_SystemManager_Tip02_array, mHourAndMin[1]);
            } else {
                String string = this.mAppContext.getResources().getString(R.string.Other_SystemManager_Tip01_connect);
                Object[] objArr = new Object[2];
                objArr[0] = this.mAppContext.getResources().getQuantityString(R.plurals.power_time_hour_array, mHourAndMin[0], new Object[]{Integer.valueOf(mHourAndMin[0])});
                objArr[1] = this.mAppContext.getResources().getQuantityString(R.plurals.power_time_min_array, mHourAndMin[1], new Object[]{Integer.valueOf(mHourAndMin[1])});
                mTimeForShow = String.format(string, objArr);
            }
            allModeLeftTimeText[i] = mTimeForShow;
        }
        HwLog.i(TAG, "mAllModeLeftTimeText[0] is " + allModeLeftTimeText[0]);
        HwLog.i(TAG, "mAllModeLeftTimeText[1] is " + allModeLeftTimeText[1]);
        this.mSaveModeTimeTv.setText(allModeLeftTimeText[0]);
        this.mSuperSaveModeTimeTv.setText(allModeLeftTimeText[1]);
    }

    private void setChargeOrBattery() {
        if (getRawLevel() == 100) {
            this.mbatteryView.setVisibility(0);
            this.mPercentView.setVisibility(0);
            this.mChargingView.setVisibility(8);
            this.mbatteryView.setText(getNumber(100));
        } else if (getmPlugged() == 0 || this.mBatteryStatus != 2) {
            getAvaliableTime();
        } else {
            this.mChargingView.setVisibility(0);
            this.mPercentView.setVisibility(0);
            this.mChargingView.setText(this.mAppContext.getResources().getString(R.string.Other_SystemManager_Charging));
            this.mbatteryView.setText(getNumber(this.mRawlevel));
        }
    }

    public boolean isWaveTopLevel() {
        if (this.mbatteryFL != null) {
            return this.mbatteryFL.isTopLevel();
        }
        return false;
    }

    private boolean isMulityCard(Context context) {
        if (this.sMultiSim != null) {
            return this.sMultiSim.booleanValue();
        }
        this.sMultiSim = Boolean.valueOf(TelephonyManager.from(context).isMultiSimEnabled());
        return this.sMultiSim.booleanValue();
    }

    private String getNumber(int number) {
        if (this.mAppContext.getResources().getBoolean(R.bool.spaceclean_percent_small_mode)) {
            return NumberFormat.getInstance().format((long) number);
        }
        this.mPercentView.setVisibility(8);
        return NumberLocationPercent.getPercentage((double) number, 0);
    }

    public void setRogChangeListener(RogChangeListener listener) {
        HwLog.i(TAG, "setRogChangeListener...");
    }

    public void setRogChecked(boolean checked) {
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        expandSlideView();
    }

    public void onDestroyView() {
        super.onDestroyView();
        this.mHandler.removeCallbacks(this.mExpandeViewRunnable);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void expandSlideView() {
        Activity ac = getActivity();
        if (ac != null && (ac instanceof HwPowerManagerActivity) && ((HwPowerManagerActivity) ac).checkShouldExpand()) {
            this.mHandler.postDelayed(this.mExpandeViewRunnable, 600);
        }
    }
}
