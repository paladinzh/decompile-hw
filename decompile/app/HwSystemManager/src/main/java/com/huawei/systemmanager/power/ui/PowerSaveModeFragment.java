package com.huawei.systemmanager.power.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncStatusObserver;
import android.database.ContentObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.internal.view.RotationPolicy;
import com.android.internal.view.RotationPolicy.RotationPolicyListener;
import com.huawei.android.app.ActionBarEx;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.ToastUtils;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.power.comm.ActionConst;
import com.huawei.systemmanager.power.comm.ApplicationConstant;
import com.huawei.systemmanager.power.model.ChangeMode;
import com.huawei.systemmanager.power.model.PowerManagementModel;
import com.huawei.systemmanager.power.service.SuperDialogShowService;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Map;

public class PowerSaveModeFragment extends Fragment {
    private static final int ACTIONBAR_CANCEL = 10;
    private static final int ACTIONBAR_CONFIRM = 11;
    private static final int BETTERY_ZERO = 0;
    private static final int COULUMB_LIMIT = 20;
    private static final int DELAY_OBSERVER = 500;
    public static final int NO_CHARGER = 0;
    private static final String TAG = "PowerManagerFragment";
    private static boolean islowBatteryPowerNotified = false;
    private static int mPlugged = 0;
    private static PowerManagementModel mPowerManager = null;
    private static int mRawlevel;
    private View fragmentView = null;
    private boolean isregister;
    private ActionBar mActionBar;
    private final OnClickListener mActionBarListener = new OnClickListener() {
        public void onClick(View v) {
            PowerSaveModeFragment.this.onActionBarItemSelected(v.getId());
        }
    };
    private Activity mActivity = null;
    private ArrayList<Double> mAllModeLeftTimeList = new ArrayList();
    private Context mAppContext = null;
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null && !TextUtils.isEmpty(intent.getAction())) {
                String action = intent.getAction();
                if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                    PowerSaveModeFragment.setmPlugged(intent.getIntExtra("plugged", 0));
                    HwLog.e(PowerSaveModeFragment.TAG, "BroadcastReceiver mPlugged = " + PowerSaveModeFragment.getmPlugged());
                    PowerSaveModeFragment.mRawlevel = intent.getIntExtra("level", 0);
                    HwLog.d(PowerSaveModeFragment.TAG, "in PowerModeAndSwitchFragment Receiver and mPlugged is " + PowerSaveModeFragment.mPlugged);
                    PowerSaveModeFragment.this.sendMessageToGetTime();
                    if (20 > PowerSaveModeFragment.mRawlevel && !PowerSaveModeFragment.islowBatteryPowerNotified) {
                        context.sendBroadcast(new Intent(ActionConst.INTENT_POWER_STATISTIC), "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
                        HwLog.e(PowerSaveModeFragment.TAG, "Battey LowLevel Trigger INTENT_POWER_STATISTIC.");
                        PowerSaveModeFragment.islowBatteryPowerNotified = true;
                    }
                }
                if ("android.intent.action.ACTION_POWER_DISCONNECTED".equals(action)) {
                    PowerSaveModeFragment.setmPlugged(0);
                }
                HwLog.e(PowerSaveModeFragment.TAG, "in Receiver and action is " + action);
                if ("android.net.wifi.WIFI_STATE_CHANGED".endsWith(action) || "android.intent.action.AIRPLANE_MODE".equals(action) || "android.media.RINGER_MODE_CHANGED".equals(action) || "android.bluetooth.adapter.action.STATE_CHANGED".equals(action)) {
                    PowerSaveModeFragment.this.sendMessageToGetTime();
                }
            }
        }
    };
    private BrightnessModeObserver mBrightnessModeObserver;
    private BrightnessObserver mBrightnessObserver;
    private RelativeLayout mEnduranceModeLayout;
    private RadioButton mEnduranceModeRadio;
    private BroadcastReceiver mEnterSuperPowerModeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null && !TextUtils.isEmpty(intent.getAction())) {
                if (ActionConst.INTENT_USE_POWER_GENIE_CHANGE_MODE.equals(intent.getAction())) {
                    PowerSaveModeFragment.this.mTempSaveMode = ChangeMode.getInstance(PowerSaveModeFragment.this.mAppContext).readSaveMode();
                }
            }
        }
    };
    private FeedBackObserver mFeedBackObserver;
    private GPSObserver mGPSObserver;
    private Object mGettingTimeSync = new Object();
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 10:
                    PowerSaveModeFragment.this.mActivity.finish();
                    return;
                case 11:
                    if (PowerSaveModeFragment.this.mTempSaveMode == 3) {
                        PowerSaveModeFragment.this.changeModeToSuper();
                        return;
                    }
                    PowerSaveModeFragment.this.doPowerModeChange(PowerSaveModeFragment.this.mTempSaveMode);
                    PowerSaveModeFragment.this.mActivity.finish();
                    return;
                default:
                    return;
            }
        }
    };
    private LayoutInflater mInflater = null;
    private boolean mIsGettingTime = false;
    private boolean mLoadedStats = false;
    private MobileDataObserver mMobileDataObserver;
    private TextView mModeSummaryEndurance;
    private TextView mModeSummaryNormal;
    private TextView mModeSummarySmart;
    private TextView mModeSummarySuper;
    private RelativeLayout mNormalModeLayout;
    private RadioButton mNormalModeRadio;
    Runnable mObserve = new Runnable() {
        public void run() {
            PowerSaveModeFragment.this.registerObservers();
        }
    };
    private boolean mObserved = false;
    private PowerModeObserver mPowerModeObserver;
    private final RotationPolicyListener mRotationPolicyListener = new RotationPolicyListener() {
        public void onChange() {
            HwLog.i(PowerSaveModeFragment.TAG, "RotationPolicyListener");
            PowerSaveModeFragment.this.sendMessageToGetTime();
        }
    };
    private int mSaveMode = 1;
    private SleepTimeOutObserver mSleepTimeOutObserver;
    private RelativeLayout mSmartModeLayout;
    private RadioButton mSmartModeRadio;
    private Object mStatusChangeListenerHandle;
    private RelativeLayout mSuperModeLayout;
    private RadioButton mSuperModeRadio;
    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        public void onStatusChanged(int which) {
            HwLog.i(PowerSaveModeFragment.TAG, "SyncStatusObserver");
            PowerSaveModeFragment.this.sendMessageToGetTime();
        }
    };
    private int mTempSaveMode = 1;
    private int[][] timeForShow = new int[][]{new int[]{0, 0}, new int[]{0, 0}, new int[]{0, 0}, new int[]{0, 0}};

    private class BrightnessModeObserver extends ContentObserver {
        public BrightnessModeObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HwLog.i(PowerSaveModeFragment.TAG, "BrightnessModeObserver");
            PowerSaveModeFragment.this.sendMessageToGetTime();
        }
    }

    private class BrightnessObserver extends ContentObserver {
        public BrightnessObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HwLog.i(PowerSaveModeFragment.TAG, "BrightnessObserver");
            PowerSaveModeFragment.this.sendMessageToGetTime();
        }
    }

    private class FeedBackObserver extends ContentObserver {
        public FeedBackObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HwLog.i(PowerSaveModeFragment.TAG, "FeedBackObserver");
            PowerSaveModeFragment.this.sendMessageToGetTime();
        }
    }

    private class GPSObserver extends ContentObserver {
        public GPSObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HwLog.i(PowerSaveModeFragment.TAG, "GPSObserver");
            PowerSaveModeFragment.this.sendMessageToGetTime();
        }
    }

    class GetTimeTask extends AsyncTask<Void, Void, String[]> {
        GetTimeTask() {
        }

        protected String[] doInBackground(Void... arg0) {
            synchronized (PowerSaveModeFragment.this.mGettingTimeSync) {
                PowerSaveModeFragment.this.mIsGettingTime = true;
            }
            return PowerSaveModeFragment.this.getModeAvaliableTime();
        }

        protected void onPostExecute(String[] result) {
            synchronized (PowerSaveModeFragment.this.mGettingTimeSync) {
                PowerSaveModeFragment.this.mIsGettingTime = false;
            }
            if (result != null && PowerSaveModeFragment.this.getActivity() != null && PowerSaveModeFragment.this.isResumed()) {
                PowerSaveModeFragment.this.mModeSummaryNormal.setText(result[0]);
                PowerSaveModeFragment.this.mModeSummarySmart.setText(result[1]);
                PowerSaveModeFragment.this.mModeSummaryEndurance.setText(result[2]);
                PowerSaveModeFragment.this.mModeSummarySuper.setText(result[3]);
            }
        }
    }

    private class MobileDataObserver extends ContentObserver {
        public MobileDataObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HwLog.i(PowerSaveModeFragment.TAG, "MobileDataObserver");
            PowerSaveModeFragment.this.sendMessageToGetTime();
        }
    }

    private class PowerModeObserver extends ContentObserver {
        public PowerModeObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            PowerSaveModeFragment.this.sendMessageToGetTime();
        }
    }

    private class SleepTimeOutObserver extends ContentObserver {
        public SleepTimeOutObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HwLog.i(PowerSaveModeFragment.TAG, "SleepTimeOutObserver");
            PowerSaveModeFragment.this.sendMessageToGetTime();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        this.mActivity = getActivity();
        this.mAppContext = this.mActivity.getApplicationContext();
        registerIntent();
        getPowerManager(this.mAppContext);
        this.mHandler.postDelayed(this.mObserve, 500);
        if (SystemProperties.getBoolean("sys.super_power_save", false)) {
            this.mTempSaveMode = 3;
        } else {
            this.mTempSaveMode = getCurrentSaveMode();
        }
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActionBar = activity.getActionBar();
        this.mActionBar.setTitle(R.string.save_mode);
        ActionBarEx.setStartIcon(this.mActionBar, true, null, this.mActionBarListener);
        ActionBarEx.setEndIcon(this.mActionBar, true, null, this.mActionBarListener);
    }

    private boolean onActionBarItemSelected(int itemId) {
        HwLog.d(TAG, "onActionBarItemSelected itemId =" + itemId);
        if (itemId == 16908296) {
            this.mHandler.removeMessages(11);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(11), 80);
        } else if (itemId == 16908295) {
            this.mHandler.removeMessages(10);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(10));
        }
        return true;
    }

    private static synchronized void getPowerManager(Context context) {
        synchronized (PowerSaveModeFragment.class) {
            mPowerManager = PowerManagementModel.getInstance(context);
        }
    }

    private static synchronized PowerManagementModel getPowerManager() {
        PowerManagementModel powerManagementModel;
        synchronized (PowerSaveModeFragment.class) {
            powerManagementModel = mPowerManager;
        }
        return powerManagementModel;
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
            this.mAppContext.getContentResolver().registerContentObserver(System.getUriFor("screen_auto_brightness_adj"), true, this.mBrightnessObserver);
            this.mAppContext.getContentResolver().registerContentObserver(Secure.getUriFor("mobile_data"), true, this.mMobileDataObserver);
            this.mAppContext.getContentResolver().registerContentObserver(Global.getUriFor("airplane_mode_on"), true, this.mMobileDataObserver);
            this.mAppContext.getContentResolver().registerContentObserver(Secure.getUriFor("location_providers_allowed"), true, this.mGPSObserver);
            this.mStatusChangeListenerHandle = ContentResolver.addStatusChangeListener(1, this.mSyncStatusObserver);
            this.mAppContext.getContentResolver().registerContentObserver(System.getUriFor("screen_off_timeout"), true, this.mSleepTimeOutObserver);
            this.mAppContext.getContentResolver().registerContentObserver(System.getUriFor("haptic_feedback_enabled"), true, this.mFeedBackObserver);
            this.mAppContext.getContentResolver().registerContentObserver(Secure.getUriFor(ApplicationConstant.SMART_MODE_STATUS), true, this.mPowerModeObserver);
            RotationPolicy.registerRotationPolicyListener(this.mAppContext, this.mRotationPolicyListener, -1);
            this.mObserved = true;
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mInflater = inflater;
        this.fragmentView = this.mInflater.inflate(R.layout.power_mode_main_new, null);
        findModeControllerView();
        addModeAndSwitchFragment();
        return this.fragmentView;
    }

    private void findModeControllerView() {
        int currentMode;
        OnClickListener modeRadioClickListener = new OnClickListener() {
            public void onClick(View v) {
                Object radioObj = v.getTag();
                if (radioObj instanceof Integer) {
                    PowerSaveModeFragment.this.mTempSaveMode = ((Integer) radioObj).intValue();
                }
                PowerSaveModeFragment.this.actionModeChanged(PowerSaveModeFragment.this.mTempSaveMode);
            }
        };
        OnClickListener radioOclickListener = new OnClickListener() {
            public void onClick(View view) {
                if (!SystemProperties.getBoolean("sys.super_power_save", false) && Utility.isOwnerUser()) {
                    PowerSaveModeFragment.this.mTempSaveMode = 3;
                    PowerSaveModeFragment.this.actionModeChanged(PowerSaveModeFragment.this.mTempSaveMode);
                }
            }
        };
        this.mNormalModeLayout = (RelativeLayout) this.fragmentView.findViewById(R.id.mode_list_item_normal);
        ((TextView) this.mNormalModeLayout.findViewById(R.id.mode_title_textview)).setText(R.string.performance_mode_title);
        this.mModeSummaryNormal = (TextView) this.mNormalModeLayout.findViewById(R.id.mode_summary_textview);
        ((TextView) this.mNormalModeLayout.findViewById(R.id.mode_content_textview)).setText(R.string.normal_mode_description);
        this.mNormalModeRadio = (RadioButton) this.mNormalModeLayout.findViewById(R.id.mode_radio_radiobutton);
        this.mNormalModeLayout.setOnClickListener(modeRadioClickListener);
        this.mNormalModeLayout.setTag(Integer.valueOf(0));
        this.mNormalModeRadio.setTag(Integer.valueOf(0));
        this.mEnduranceModeLayout = (RelativeLayout) this.fragmentView.findViewById(R.id.mode_list_item_endurance);
        ((TextView) this.mEnduranceModeLayout.findViewById(R.id.mode_title_textview)).setText(R.string.endurance_mode_title);
        this.mModeSummaryEndurance = (TextView) this.mEnduranceModeLayout.findViewById(R.id.mode_summary_textview);
        ((TextView) this.mEnduranceModeLayout.findViewById(R.id.mode_content_textview)).setText(R.string.endurance_mode_description);
        this.mEnduranceModeRadio = (RadioButton) this.mEnduranceModeLayout.findViewById(R.id.mode_radio_radiobutton);
        this.mEnduranceModeLayout.setOnClickListener(modeRadioClickListener);
        this.mEnduranceModeLayout.setTag(Integer.valueOf(2));
        this.mEnduranceModeRadio.setTag(Integer.valueOf(2));
        this.fragmentView.findViewById(R.id.mode_list_item_seperate_endurance).setVisibility(8);
        this.mEnduranceModeLayout.setVisibility(8);
        this.mSmartModeLayout = (RelativeLayout) this.fragmentView.findViewById(R.id.mode_list_item_smart);
        ((TextView) this.mSmartModeLayout.findViewById(R.id.mode_title_textview)).setText(R.string.smart_mode_title);
        this.mModeSummarySmart = (TextView) this.mSmartModeLayout.findViewById(R.id.mode_summary_textview);
        ((TextView) this.mSmartModeLayout.findViewById(R.id.mode_content_textview)).setText(R.string.smart_mode_description);
        this.mSmartModeRadio = (RadioButton) this.mSmartModeLayout.findViewById(R.id.mode_radio_radiobutton);
        this.mSmartModeLayout.setOnClickListener(modeRadioClickListener);
        this.mSmartModeLayout.setTag(Integer.valueOf(1));
        this.mSmartModeRadio.setTag(Integer.valueOf(1));
        this.mSuperModeLayout = (RelativeLayout) this.fragmentView.findViewById(R.id.mode_list_item_super);
        ((TextView) this.mSuperModeLayout.findViewById(R.id.mode_title_textview)).setText(R.string.super_power_saving_title);
        this.mModeSummarySuper = (TextView) this.mSuperModeLayout.findViewById(R.id.mode_summary_textview);
        ((TextView) this.mSuperModeLayout.findViewById(R.id.mode_content_textview)).setText(R.string.super_power_summmary);
        this.mSuperModeRadio = (RadioButton) this.mSuperModeLayout.findViewById(R.id.mode_radio_radiobutton);
        this.mSuperModeLayout.setOnClickListener(radioOclickListener);
        this.mSuperModeLayout.setTag(Integer.valueOf(3));
        this.mSuperModeRadio.setTag(Integer.valueOf(3));
        if (!Utility.superPowerEntryEnable() || Utility.isWifiOnlyMode() || Utility.isDataOnlyMode()) {
            this.fragmentView.findViewById(R.id.mode_list_item_seperate_super).setVisibility(8);
            this.mSuperModeLayout.setVisibility(8);
        }
        this.mNormalModeRadio.setContentDescription(this.mAppContext.getResources().getString(R.string.performance_mode_title));
        this.mSmartModeRadio.setContentDescription(this.mAppContext.getResources().getString(R.string.smart_mode_title));
        this.mSuperModeRadio.setContentDescription(this.mAppContext.getResources().getString(R.string.super_power_saving_title));
        this.mEnduranceModeRadio.setContentDescription(this.mAppContext.getResources().getString(R.string.endurance_mode_title));
        if (SystemProperties.getBoolean("sys.super_power_save", false)) {
            currentMode = 3;
        } else {
            currentMode = ChangeMode.getInstance(getActivity()).readSaveMode();
        }
        HwLog.d(TAG, "setupModeView and mode is" + currentMode);
        changeModeRadioView(currentMode);
    }

    private void actionModeChanged(int modeNum) {
        if (SystemProperties.getBoolean("sys.super_power_save", false)) {
            ToastUtils.toastShortMsg((int) R.string.super_power_quit_promote_message_Toast);
            changeModeRadioView(3);
            this.mTempSaveMode = 3;
            return;
        }
        changeModeRadioView(modeNum);
    }

    private void doPowerModeChange(int modeNum) {
        if (SystemProperties.getBoolean("sys.super_power_save", false)) {
            ToastUtils.toastShortMsg((int) R.string.super_power_quit_promote_message_Toast);
            this.mTempSaveMode = 3;
            return;
        }
        ChangeMode changeMode = ChangeMode.getInstance(this.mAppContext);
        if (changeMode.readSaveMode() != modeNum) {
            changeMode.change(modeNum);
            String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, String.valueOf(modeNum));
            HsmStat.statE(23, statParam);
        }
    }

    private void changeModeToSuper() {
        if (ActivityManager.isUserAMonkey()) {
            HwLog.d(TAG, "Monkey testing!");
        } else if (SystemProperties.getBoolean("sys.super_power_save", false)) {
            ToastUtils.toastShortMsg((int) R.string.super_power_quit_promote_message_Toast);
            this.mTempSaveMode = 3;
        } else {
            setRadio();
            HsmStat.statSuperPowerDialogAction("d", "com.huawei.systemmanager");
            Intent serviceIntent = new Intent();
            serviceIntent.setClass(this.mAppContext, SuperDialogShowService.class);
            Bundle bundle = new Bundle();
            bundle.putString(ApplicationConstant.SUPER_DIALOG_PACKAGEFROM, "com.huawei.systemmanager");
            bundle.putString(ApplicationConstant.SUPER_DIALOG_LABEL, ApplicationConstant.NORMAL_SUPER_DIALOG);
            serviceIntent.putExtras(bundle);
            this.mAppContext.startServiceAsUser(serviceIntent, UserHandle.CURRENT);
        }
    }

    private void changeModeRadioView(int mode) {
        this.mNormalModeRadio.setChecked(false);
        this.mSmartModeRadio.setChecked(false);
        this.mSuperModeRadio.setChecked(false);
        this.mEnduranceModeRadio.setChecked(false);
        switch (mode) {
            case 0:
                this.mNormalModeRadio.setChecked(true);
                return;
            case 1:
                this.mSmartModeRadio.setChecked(true);
                return;
            case 2:
                this.mEnduranceModeRadio.setChecked(true);
                return;
            case 3:
                this.mSuperModeRadio.setChecked(true);
                return;
            default:
                HwLog.w(TAG, "changeModeRadioView invalid mode: " + mode);
                return;
        }
    }

    private void setRadio() {
        changeModeRadioView(this.mTempSaveMode);
    }

    private void sendMessageToGetTime() {
        synchronized (this.mGettingTimeSync) {
            if (!this.mIsGettingTime) {
                new GetTimeTask().execute(new Void[0]);
            }
        }
    }

    private synchronized void initModel() {
        if (!this.mLoadedStats) {
            try {
                synchronized (PowerSaveModeFragment.class) {
                    mPowerManager = PowerManagementModel.getInstance(this.mAppContext).load();
                }
                this.mLoadedStats = true;
            } catch (IllegalArgumentException e) {
                HwLog.e(TAG, "PowerManagementModel init failed! ");
            }
        }
    }

    public static int getRawLevel() {
        return mRawlevel;
    }

    public void onResume() {
        super.onResume();
        if (!this.isregister) {
            registerIntent();
        }
        HwLog.e(TAG, "Activity OnResume and mPlugged is " + getmPlugged());
        initModeSwitch();
    }

    private void initModeSwitch() {
        sendMessageToGetTime();
        changeModeRadioView(this.mTempSaveMode);
    }

    private int getCurrentSaveMode() {
        if (SystemProperties.getBoolean("sys.super_power_save", false)) {
            return 3;
        }
        return ChangeMode.getInstance(this.mAppContext).readSaveMode();
    }

    public void onDestroy() {
        if (this.isregister) {
            this.mActivity.unregisterReceiver(this.mBatInfoReceiver);
            this.mActivity.unregisterReceiver(this.mEnterSuperPowerModeReceiver);
            this.isregister = false;
        }
        unRegisterObservers();
        super.onDestroy();
    }

    public void onPause() {
        this.mHandler.removeCallbacks(this.mObserve);
        if (this.isregister) {
            this.mActivity.unregisterReceiver(this.mBatInfoReceiver);
            this.mActivity.unregisterReceiver(this.mEnterSuperPowerModeReceiver);
            this.isregister = false;
        }
        super.onPause();
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
            this.mObserved = false;
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

    public void addModeAndSwitchFragment() {
        initModeSwitch();
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
        Intent batteryStatus = this.mActivity.registerReceiver(this.mBatInfoReceiver, intentFilter);
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(ActionConst.INTENT_USE_POWER_GENIE_CHANGE_MODE);
        this.mActivity.registerReceiver(this.mEnterSuperPowerModeReceiver, iFilter, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
        this.isregister = true;
        setmPlugged(batteryStatus.getIntExtra("plugged", 0));
    }

    public static int getmPlugged() {
        return mPlugged;
    }

    public static void setmPlugged(int mPlugged) {
        mPlugged = mPlugged;
    }

    public void onBackPressed() {
        if (getActivity() != null && getActivity().isResumed()) {
            addModeAndSwitchFragment();
        }
    }

    private String[] getModeAvaliableTime() {
        initModel();
        Map<String, Object> mTimeByCurrentBattery = getPowerManager().getTimeByCurrentBatteryLevel(this.mAppContext, mRawlevel);
        this.mAllModeLeftTimeList.clear();
        this.mAllModeLeftTimeList.add(Double.valueOf(Double.parseDouble(mTimeByCurrentBattery.get(ApplicationConstant.NORMAL_MODE_KEY).toString())));
        this.mAllModeLeftTimeList.add(Double.valueOf(Double.parseDouble(mTimeByCurrentBattery.get(ApplicationConstant.SMART_MODE_KEY).toString())));
        this.mAllModeLeftTimeList.add(Double.valueOf(Double.parseDouble(mTimeByCurrentBattery.get(ApplicationConstant.ENDURANCE_MODE_KEY).toString())));
        this.mAllModeLeftTimeList.add(Double.valueOf(Double.parseDouble(mTimeByCurrentBattery.get(ApplicationConstant.SUPER_MODE_POWER_KEY).toString())));
        String[] allModeLeftTimeText = new String[4];
        for (int i = 0; i < 4; i++) {
            String mTimeForShow;
            HwLog.d(TAG, "available time is " + this.mAllModeLeftTimeList.get(i) + " mRawlevel is " + mRawlevel);
            int[] mHourAndMin = convertDoubleTimeToSeprateInt(((Double) this.mAllModeLeftTimeList.get(i)).doubleValue());
            HwLog.d(TAG, "hour is " + mHourAndMin[0] + " min is " + mHourAndMin[1]);
            if (mHourAndMin[0] == 0) {
                mTimeForShow = this.mAppContext.getResources().getQuantityString(R.plurals.power_time_min_array, mHourAndMin[1], new Object[]{Integer.valueOf(mHourAndMin[1])});
            } else {
                String string = this.mAppContext.getResources().getString(R.string.power_time_connect);
                Object[] objArr = new Object[2];
                objArr[0] = this.mAppContext.getResources().getQuantityString(R.plurals.power_time_hour_array, mHourAndMin[0], new Object[]{Integer.valueOf(mHourAndMin[0])});
                objArr[1] = this.mAppContext.getResources().getQuantityString(R.plurals.power_time_min_array, mHourAndMin[1], new Object[]{Integer.valueOf(mHourAndMin[1])});
                mTimeForShow = String.format(string, objArr);
            }
            this.timeForShow[i][0] = mHourAndMin[0];
            this.timeForShow[i][1] = mHourAndMin[1];
            allModeLeftTimeText[i] = mTimeForShow;
        }
        HwLog.d(TAG, "mAllModeLeftTimeText[0] is " + allModeLeftTimeText[0]);
        HwLog.d(TAG, "mAllModeLeftTimeText[1] is " + allModeLeftTimeText[1]);
        HwLog.d(TAG, "mAllModeLeftTimeText[2] is " + allModeLeftTimeText[2]);
        HwLog.d(TAG, "mAllModeLeftTimeText[3] is " + allModeLeftTimeText[3]);
        return allModeLeftTimeText;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        return onActionBarItemSelected(item.getItemId());
    }
}
