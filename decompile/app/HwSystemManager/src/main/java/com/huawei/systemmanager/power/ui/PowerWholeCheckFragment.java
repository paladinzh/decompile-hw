package com.huawei.systemmanager.power.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncStatusObserver;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import com.android.internal.view.RotationPolicy;
import com.android.internal.view.RotationPolicy.RotationPolicyListener;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.SystemManagerConst;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.mainscreen.view.MainCircleProgressView;
import com.huawei.systemmanager.mainscreen.view.PowerMainScreenRollingView;
import com.huawei.systemmanager.optimize.process.ProtectActivity;
import com.huawei.systemmanager.optimize.process.ProtectAppControl;
import com.huawei.systemmanager.power.batteryoptimize.AutoRotationDetectItem;
import com.huawei.systemmanager.power.batteryoptimize.AutoSyncDetectItem;
import com.huawei.systemmanager.power.batteryoptimize.BluetoothDetectItem;
import com.huawei.systemmanager.power.batteryoptimize.BrightnessDetectItem;
import com.huawei.systemmanager.power.batteryoptimize.FeedBackDetectitem;
import com.huawei.systemmanager.power.batteryoptimize.GpsDetectItem;
import com.huawei.systemmanager.power.batteryoptimize.MobileDataDetectItem;
import com.huawei.systemmanager.power.batteryoptimize.PowerDetectItem;
import com.huawei.systemmanager.power.batteryoptimize.PowerOptimizeControl;
import com.huawei.systemmanager.power.batteryoptimize.PowerOptimizeItemRollingView;
import com.huawei.systemmanager.power.batteryoptimize.PowerOptimizeSuccessControl;
import com.huawei.systemmanager.power.batteryoptimize.PowerOptimizeTimeControl;
import com.huawei.systemmanager.power.batteryoptimize.PowerUnOptimizedController;
import com.huawei.systemmanager.power.batteryoptimize.ProtectAppDetectItem;
import com.huawei.systemmanager.power.batteryoptimize.ScreenTimeout;
import com.huawei.systemmanager.power.batteryoptimize.VibrateDetectItem;
import com.huawei.systemmanager.power.batteryoptimize.WlanDetectItem;
import com.huawei.systemmanager.power.data.stats.PowerStatsHelper;
import com.huawei.systemmanager.power.data.stats.UidAndPower;
import com.huawei.systemmanager.power.model.PowerManagementModel;
import com.huawei.systemmanager.spacecleanner.engine.base.ITrashEngine.IUpdateListener;
import com.huawei.systemmanager.util.HSMConst;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PowerWholeCheckFragment extends Fragment {
    private static final int AUTO_SYNC_MSG = 5;
    private static final int BETTERY_ZERO = 0;
    private static int BG_CONSUME_APPS_SCORE = 12;
    private static final int DELAY_OBSERVER = 500;
    private static final int DELAY_TIME_SCAN = 80;
    private static final int FIFTEEN_SECONDS = 15;
    private static final int MILLI_SECOND = 1000;
    private static final int MSG_SHOW_OPTIMIZE_RESULT = 3;
    private static final int PROBLEAM_OFF = 0;
    private static final int PROBLEAM_ON = 1;
    private static final int RESULT_END_OPTIMIZE = 4;
    private static final int SECOND_PER_MINUTE = 60;
    private static final int STOP_BATTERY_OPTIMIZE = 0;
    private static int SYSTEM_SWITCH_SCORE = 8;
    private static final String TAG = "PowerWholeCheckFragment";
    private static final int TEN_MINUTES = 10;
    private static final int TYPE_BEST_SAVING = 4;
    private static final int TYPE_CLOSE_BGCONSUME_APPS = 1;
    private static final int TYPE_MANUAL_OPTIMIZE_CONSUME_SWITCH = 3;
    private static final int TYPE_OPTIMIZED_CONSUME_SWITCH = 2;
    public static final String URI_FOR_SUBID_0 = "mobile_data0";
    public static final String URI_FOR_SUBID_1 = "mobile_data1";
    private static int ZERO_MANUAL_ITEM = 0;
    private static BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null && !TextUtils.isEmpty(intent.getAction())) {
                if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                    PowerWholeCheckFragment.mRawlevel = intent.getIntExtra("level", 0);
                }
            }
        }
    };
    private static PowerManagementModel mPowerManager = null;
    private static int mRawlevel;
    private List<UidAndPower> backAppList;
    private PowerOptimizeItemRollingView bestSavingText;
    private int bestsavingItemNum = 0;
    private PowerOptimizeItemRollingView bgConsumeText;
    private View fragmentView = null;
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 3:
                    PowerWholeCheckFragment.this.mResultPresentTask = new ResultPresentTask();
                    PowerWholeCheckFragment.this.mResultPresentTask.execute(new Void[0]);
                    break;
                case 4:
                    if (PowerWholeCheckFragment.this.mBgAppCloseNum == 0 && PowerWholeCheckFragment.this.optimizedItemNum == 0) {
                        PowerWholeCheckFragment.this.mPowerWholeCheck.setText(PowerWholeCheckFragment.this.mAppContext.getResources().getString(R.string.common_finish));
                    } else {
                        PowerWholeCheckFragment.this.mCheckInfoView.setVisibility(8);
                        PowerWholeCheckFragment.this.mPowerWholeCheck.setText(PowerWholeCheckFragment.this.mAppContext.getString(R.string.common_finish));
                    }
                    PowerWholeCheckFragment.this.updateNeedManualItemNum();
                    PowerWholeCheckFragment.this.isOptimizeOver = true;
                    break;
                case 5:
                    PowerWholeCheckFragment.this.refreshOptimizeResult(9);
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private boolean isOptimizeOver = false;
    private boolean isregister;
    private LayoutInflater layoutInflater;
    private Activity mActivity = null;
    private boolean mAnimEnd = false;
    private Context mAppContext = null;
    private int mBenefitOneKeyOptimize = 0;
    private List<PowerDetectItem> mBestSaveSettingsList = null;
    private int mBgAppCloseNum = 0;
    private BrightnessModeObserver mBrightnessModeObserver;
    private BrightnessObserver mBrightnessObserver;
    private RelativeLayout mBtnContainer;
    private ScrollView mCheckAllLoading;
    private TextView mCheckInfoView;
    private MainCircleProgressView mCircleImageOptimize;
    private RelativeLayout mCircleLayout;
    private ViewGroup mContanerResult;
    private FeedBackObserver mFeedBackObserver;
    private GPSObserver mGPSObserver;
    private Map<Integer, List<PowerDetectItem>> mGroupItems = new LinkedHashMap();
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    PowerWholeCheckFragment.this.finishWholeCheckActivity();
                    return;
                default:
                    return;
            }
        }
    };
    private List<PowerDetectItem> mItems = Lists.newArrayList();
    private boolean mLoadedStats = false;
    private TextView mManuItemNumView;
    private List<PowerDetectItem> mManualOptimizSettingsList = null;
    private MobileDataObserver mMobileDataObserver;
    private RelativeLayout mNuoyiUpParent;
    Runnable mObserve = new Runnable() {
        public void run() {
            PowerWholeCheckFragment.this.registerObservers();
        }
    };
    private boolean mObserved = false;
    private OnClickListener mOptimizeBtnClick = new OnClickListener() {
        public void onClick(View v) {
            PowerDetectItem item = (PowerDetectItem) v.getTag();
            if (item == null) {
                HwLog.e(PowerWholeCheckFragment.TAG, "mOptimizeBtnClick onClick item is null!");
                return;
            }
            int itemType = item.getItemType();
            HwLog.i(PowerWholeCheckFragment.TAG, " user click opimitze btn, item type:" + itemType);
            if (itemType == 4) {
                Intent intent = new Intent();
                intent.setClass(PowerWholeCheckFragment.this.mActivity, ProtectActivity.class);
                PowerWholeCheckFragment.this.startActivity(intent);
                String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "3");
                HsmStat.statE((int) Events.E_ENTER_PROTECTEDAPP, statParam);
            } else {
                item.doOptimize();
                if (PowerWholeCheckFragment.this.mUnoptimizedItems.containsKey(Integer.valueOf(item.getItemType()))) {
                    ((PowerUnOptimizedController) PowerWholeCheckFragment.this.mUnoptimizedItems.get(Integer.valueOf(item.getItemType()))).update(item);
                    PowerWholeCheckFragment powerWholeCheckFragment = PowerWholeCheckFragment.this;
                    powerWholeCheckFragment.manualOptimizeNum = powerWholeCheckFragment.manualOptimizeNum - 1;
                    PowerWholeCheckFragment.this.updateNeedManualItemNum();
                    PowerWholeCheckFragment.this.doHsmStatOfResultClickEvent(item.getItemType());
                }
            }
        }
    };
    private PowerMainScreenRollingView mOptimizeRollView;
    private TextView mOptimizeRollViewunit;
    private OptimizeTask mOptimizeTask;
    private List<PowerDetectItem> mOptimizedSettingsList = null;
    private RelativeLayout mPowerMainLayout;
    private PowerOptimizeControl mPowerOptimizeControl;
    private Button mPowerWholeCheck;
    private int mProtectAppSize = 0;
    private ResultPresentTask mResultPresentTask;
    private LinearLayout mResultUpParent;
    private final RotationPolicyListener mRotationPolicyListener = new RotationPolicyListener() {
        public void onChange() {
            HwLog.i(PowerWholeCheckFragment.TAG, "RotationPolicyListener");
            PowerWholeCheckFragment.this.refreshOptimizeResult(12);
        }
    };
    private SleepTimeOutObserver mSleepTimeOutObserver;
    private PowerStatsHelper mStatsHelper = null;
    private Object mStatusChangeListenerHandle;
    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        public void onStatusChanged(int which) {
            HwLog.i(PowerWholeCheckFragment.TAG, "SyncStatusObserver");
            Message message = new Message();
            message.what = 5;
            PowerWholeCheckFragment.this.handler.sendMessage(message);
        }
    };
    private Map<String, Object> mTimeByCurrentBattery = null;
    private final Map<Integer, PowerUnOptimizedController> mUnoptimizedItems = new LinkedHashMap();
    private BroadcastReceiver mVibrateChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null && !TextUtils.isEmpty(intent.getAction())) {
                if ("android.media.RINGER_MODE_CHANGED".equals(intent.getAction())) {
                    PowerWholeCheckFragment.this.refreshOptimizeResult(10);
                }
            }
        }
    };
    private int manualOptimizeNum = 0;
    private PowerOptimizeItemRollingView manualText;
    private int optimizeProgressNum = 0;
    private int optimizedItemNum = 0;
    private PowerOptimizeItemRollingView optimizedText;
    private ViewGroup resultUpperContentLayout;
    private ViewGroup resultUpperHeadLayout;
    private Boolean sMultiSim = null;
    private ViewGroup scrollViewEnd;
    private ViewGroup scrollViewEndContent;

    private class BrightnessModeObserver extends ContentObserver {
        public BrightnessModeObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HwLog.i(PowerWholeCheckFragment.TAG, "BrightnessModeObserver");
            PowerWholeCheckFragment.this.refreshOptimizeResult(5);
        }
    }

    private class BrightnessObserver extends ContentObserver {
        public BrightnessObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HwLog.i(PowerWholeCheckFragment.TAG, "BrightnessObserver");
            PowerWholeCheckFragment.this.refreshOptimizeResult(5);
        }
    }

    private class FeedBackObserver extends ContentObserver {
        public FeedBackObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HwLog.i(PowerWholeCheckFragment.TAG, "FeedBackObserver");
            PowerWholeCheckFragment.this.refreshOptimizeResult(11);
        }
    }

    private class GPSObserver extends ContentObserver {
        public GPSObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HwLog.i(PowerWholeCheckFragment.TAG, "GPSObserver");
            PowerWholeCheckFragment.this.refreshOptimizeResult(8);
        }
    }

    private class MobileDataObserver extends ContentObserver {
        public MobileDataObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HwLog.i(PowerWholeCheckFragment.TAG, "MobileDataObserver");
            PowerWholeCheckFragment.this.refreshOptimizeResult(7);
        }
    }

    private class OptimizeTask extends AsyncTask<Void, Integer, Void> {
        private OptimizeTask() {
        }

        protected void onPreExecute() {
            super.onPreExecute();
            PowerWholeCheckFragment.this.optimizeProgressNum = 0;
            PowerWholeCheckFragment.this.mBgAppCloseNum = 0;
            PowerWholeCheckFragment.this.optimizedItemNum = 0;
            PowerWholeCheckFragment.this.manualOptimizeNum = 0;
            PowerWholeCheckFragment.this.bestsavingItemNum = 0;
            PowerWholeCheckFragment.this.mCheckInfoView.setVisibility(0);
            PowerWholeCheckFragment.this.mCheckInfoView.setText(R.string.space_optimize_info);
        }

        protected Void doInBackground(Void... params) {
            long start = SystemClock.elapsedRealtime();
            HwLog.i(PowerWholeCheckFragment.TAG, "BatteryOptimizeTask start optimized, ");
            PowerWholeCheckFragment.this.initializeData();
            if (isCancelled()) {
                return null;
            }
            PowerWholeCheckFragment powerWholeCheckFragment;
            for (UidAndPower up : PowerWholeCheckFragment.this.backAppList) {
                if (isCancelled()) {
                    HwLog.i(PowerWholeCheckFragment.TAG, "BatteryOptimizeTask bgconsumeapps canceled!");
                    return null;
                }
                String[] pkgs = PowerWholeCheckFragment.this.mAppContext.getPackageManager().getPackagesForUid(up.getUid());
                if (!(pkgs == null || pkgs.length == 0)) {
                    for (String pkg : pkgs) {
                        PowerWholeCheckFragment.this.killAppsByPackageName(PowerWholeCheckFragment.this.mAppContext, pkg);
                    }
                    powerWholeCheckFragment = PowerWholeCheckFragment.this;
                    powerWholeCheckFragment.mBgAppCloseNum = powerWholeCheckFragment.mBgAppCloseNum + 1;
                }
            }
            powerWholeCheckFragment = PowerWholeCheckFragment.this;
            powerWholeCheckFragment.optimizeProgressNum = powerWholeCheckFragment.optimizeProgressNum + PowerWholeCheckFragment.BG_CONSUME_APPS_SCORE;
            PowerWholeCheckFragment.this.optimiezeDelay(200);
            publishProgress(new Integer[]{Integer.valueOf(1)});
            PowerWholeCheckFragment.this.optimiezeDelay(600);
            if (isCancelled()) {
                HwLog.i(PowerWholeCheckFragment.TAG, "BatteryOptimizeTask optimized canceled!");
                return null;
            }
            List<PowerDetectItem> tList = (List) PowerWholeCheckFragment.this.mGroupItems.get(Integer.valueOf(2));
            if (tList != null && tList.size() > 0) {
                for (PowerDetectItem item : tList) {
                    item.doOptimize();
                    powerWholeCheckFragment = PowerWholeCheckFragment.this;
                    powerWholeCheckFragment.optimizedItemNum = powerWholeCheckFragment.optimizedItemNum + 1;
                }
            }
            powerWholeCheckFragment = PowerWholeCheckFragment.this;
            powerWholeCheckFragment.optimizeProgressNum = powerWholeCheckFragment.optimizeProgressNum + PowerWholeCheckFragment.SYSTEM_SWITCH_SCORE;
            powerWholeCheckFragment = PowerWholeCheckFragment.this;
            powerWholeCheckFragment.optimizeProgressNum = powerWholeCheckFragment.optimizeProgressNum + PowerWholeCheckFragment.SYSTEM_SWITCH_SCORE;
            publishProgress(new Integer[]{Integer.valueOf(2)});
            PowerWholeCheckFragment.this.optimiezeDelay(600);
            if (isCancelled()) {
                HwLog.i(PowerWholeCheckFragment.TAG, "BatteryOptimizeTask manual canceled!");
                return null;
            }
            List<PowerDetectItem> manualList = (List) PowerWholeCheckFragment.this.mGroupItems.get(Integer.valueOf(3));
            if (manualList != null && manualList.size() > 0) {
                powerWholeCheckFragment = PowerWholeCheckFragment.this;
                powerWholeCheckFragment.manualOptimizeNum = powerWholeCheckFragment.manualOptimizeNum + manualList.size();
                powerWholeCheckFragment = PowerWholeCheckFragment.this;
                powerWholeCheckFragment.optimizeProgressNum = powerWholeCheckFragment.optimizeProgressNum + (manualList.size() * PowerWholeCheckFragment.SYSTEM_SWITCH_SCORE);
            }
            publishProgress(new Integer[]{Integer.valueOf(3)});
            PowerWholeCheckFragment.this.optimiezeDelay(600);
            if (isCancelled()) {
                HwLog.i(PowerWholeCheckFragment.TAG, "BatteryOptimizeTask best saving canceled!");
                return null;
            }
            List<PowerDetectItem> bestSavingList = (List) PowerWholeCheckFragment.this.mGroupItems.get(Integer.valueOf(4));
            if (bestSavingList != null && bestSavingList.size() > 0) {
                powerWholeCheckFragment = PowerWholeCheckFragment.this;
                powerWholeCheckFragment.bestsavingItemNum = powerWholeCheckFragment.bestsavingItemNum + bestSavingList.size();
                powerWholeCheckFragment = PowerWholeCheckFragment.this;
                powerWholeCheckFragment.optimizeProgressNum = powerWholeCheckFragment.optimizeProgressNum + (bestSavingList.size() * PowerWholeCheckFragment.SYSTEM_SWITCH_SCORE);
            }
            publishProgress(new Integer[]{Integer.valueOf(4)});
            HwLog.i(PowerWholeCheckFragment.TAG, "BatteryOptimizeTask end optimized, cost Time= " + (SystemClock.elapsedRealtime() - start));
            return null;
        }

        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int typeGroup = values[0].intValue();
            if (typeGroup == 1) {
                PowerWholeCheckFragment.this.updateProgress(true);
                PowerWholeCheckFragment.this.bgConsumeText.setItemNumResId(R.plurals.power_optimize_app_item_num);
                PowerWholeCheckFragment.this.bgConsumeText.setNumberByDuration(PowerWholeCheckFragment.this.mBgAppCloseNum, IUpdateListener.ERROR_CODE_NO_NETWORK);
            }
            if (typeGroup == 2) {
                PowerWholeCheckFragment.this.updateProgress(true);
                PowerWholeCheckFragment.this.optimizedText.setNumberByDuration(PowerWholeCheckFragment.this.optimizedItemNum, IUpdateListener.ERROR_CODE_NO_NETWORK);
            }
            if (typeGroup == 3) {
                PowerWholeCheckFragment.this.updateProgress(true);
                PowerWholeCheckFragment.this.manualText.setNumberByDuration(PowerWholeCheckFragment.this.manualOptimizeNum, IUpdateListener.ERROR_CODE_NO_NETWORK);
            }
            if (typeGroup == 4) {
                PowerWholeCheckFragment.this.updateProgress(true);
                PowerWholeCheckFragment.this.bestSavingText.setNumberByDuration(PowerWholeCheckFragment.this.bestsavingItemNum, IUpdateListener.ERROR_CODE_NO_NETWORK);
                Message message = new Message();
                message.what = 3;
                PowerWholeCheckFragment.this.handler.sendMessageDelayed(message, 500);
            }
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    private class ResultPresentTask extends AsyncTask<Void, PowerDetectItem, Void> {
        private ResultPresentTask() {
        }

        protected void onPreExecute() {
            super.onPreExecute();
            PowerWholeCheckFragment.this.mUnoptimizedItems.clear();
            PowerWholeCheckFragment.this.displayResultUpperLayout();
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            PowerWholeCheckFragment.this.displayOptimized();
            PowerWholeCheckFragment.this.outputAnim();
        }

        protected void onProgressUpdate(PowerDetectItem... values) {
            super.onProgressUpdate(new PowerDetectItem[]{values[0]});
            PowerWholeCheckFragment.this.mUnoptimizedItems.put(Integer.valueOf(values[0].getItemType()), PowerUnOptimizedController.create(PowerWholeCheckFragment.this.layoutInflater, PowerWholeCheckFragment.this.scrollViewEndContent, PowerWholeCheckFragment.this.mOptimizeBtnClick, values[0]));
        }

        protected Void doInBackground(Void... params) {
            if (PowerWholeCheckFragment.this.mManualOptimizSettingsList != null) {
                Iterator item$iterator = PowerWholeCheckFragment.this.mManualOptimizSettingsList.iterator();
                while (item$iterator.hasNext()) {
                    publishProgress(new PowerDetectItem[]{(PowerDetectItem) item$iterator.next()});
                }
            }
            return null;
        }
    }

    private class SleepTimeOutObserver extends ContentObserver {
        public SleepTimeOutObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HwLog.i(PowerWholeCheckFragment.TAG, "SleepTimeOutObserver");
            PowerWholeCheckFragment.this.refreshOptimizeResult(6);
        }
    }

    private void doHsmStatOfResultClickEvent(int type) {
        int eventStatkey = 0;
        switch (type) {
            case 5:
                eventStatkey = 6;
                break;
            case 6:
                eventStatkey = 7;
                break;
            case 7:
                eventStatkey = 8;
                break;
            case 8:
                eventStatkey = 9;
                break;
            case 9:
                eventStatkey = 13;
                break;
            case 10:
                eventStatkey = 11;
                break;
            case 11:
                eventStatkey = 12;
                break;
            case 12:
                eventStatkey = 13;
                break;
        }
        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_KEY, String.valueOf(eventStatkey), HsmStatConst.PARAM_VAL, "1");
        HsmStat.statE((int) Events.E_POWER_WHOLECHECK_ADVICEITEM_CLICK, statParam);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mActivity = getActivity();
        this.mAppContext = this.mActivity.getApplicationContext();
        registerIntent();
        this.mStatsHelper = PowerStatsHelper.newInstance(this.mAppContext, true);
        this.mHandler.postDelayed(this.mObserve, 500);
    }

    void finishWholeCheckActivity() {
        if (this.mOptimizeTask != null) {
            HwLog.i(TAG, "mOptimizeTask is cancel");
            this.mOptimizeTask.cancel(true);
        }
        if (this.mResultPresentTask != null) {
            HwLog.i(TAG, "mResultPresentTask is cancel");
            this.mResultPresentTask.cancel(true);
        }
        this.handler.removeMessages(4);
        this.mActivity.finish();
    }

    void turnToStartOptimize() {
        this.isOptimizeOver = false;
        this.mPowerWholeCheck.setText(this.mActivity.getResources().getString(R.string.alert_dialog_cancel));
        initModel();
        if (mPowerManager != null) {
            this.mTimeByCurrentBattery = mPowerManager.getTimeByCurrentBatteryLevel(this.mAppContext, mRawlevel);
        }
        updateProgress(false);
        this.mBenefitOneKeyOptimize = getBenefitOneKeyOptimize();
        this.mOptimizeTask = new OptimizeTask();
        this.mOptimizeTask.execute(new Void[0]);
    }

    private void initModel() {
        if (!this.mLoadedStats) {
            try {
                mPowerManager = PowerManagementModel.getInstance(this.mAppContext).load();
                this.mLoadedStats = true;
            } catch (IllegalArgumentException e) {
                HwLog.e(TAG, "PowerManagementModel init failed! ");
            }
        }
    }

    private void displayResultUpperLayout() {
        if (getActivity() != null) {
            boolean isLand = HSMConst.isLand();
            if (this.mBgAppCloseNum == 0 && this.optimizedItemNum == 0) {
                this.mPowerOptimizeControl = PowerOptimizeSuccessControl.create(this.layoutInflater, getNuoyiUpperParent(isLand));
                this.mManuItemNumView = this.mPowerOptimizeControl.getManualItemNumView();
                this.resultUpperContentLayout = this.mPowerOptimizeControl.getContentLayout();
                this.resultUpperHeadLayout = this.mPowerOptimizeControl.getHeadLayout();
            } else {
                this.mPowerOptimizeControl = PowerOptimizeTimeControl.create(this.layoutInflater, getNuoyiUpperParent(isLand));
                if (this.mPowerOptimizeControl instanceof PowerOptimizeTimeControl) {
                    ((PowerOptimizeTimeControl) this.mPowerOptimizeControl).updateOptimizedTime(this.mBenefitOneKeyOptimize);
                }
                this.mManuItemNumView = this.mPowerOptimizeControl.getManualItemNumView();
                this.resultUpperContentLayout = this.mPowerOptimizeControl.getContentLayout();
                this.resultUpperHeadLayout = this.mPowerOptimizeControl.getHeadLayout();
            }
        }
    }

    private ViewGroup getNuoyiUpperParent(boolean isLand) {
        if (isLand) {
            return this.mResultUpParent;
        }
        return this.scrollViewEndContent;
    }

    private void inputAnim() {
        this.mContanerResult.setVisibility(0);
        Animation animation = AnimationUtils.loadAnimation(this.mActivity, R.anim.mainscreen_optimize_stepthree);
        LayoutAnimationController anima = new LayoutAnimationController(animation, 0.1f);
        for (int i = 0; i < this.scrollViewEndContent.getChildCount(); i++) {
            if (i == 0) {
                if (this.resultUpperHeadLayout != null) {
                    this.resultUpperHeadLayout.setAnimation(AnimationUtils.loadAnimation(this.mActivity, R.anim.virus_show));
                }
                if (this.resultUpperContentLayout != null) {
                    this.resultUpperContentLayout.setAnimation(animation);
                }
            } else {
                this.scrollViewEndContent.getChildAt(i).setAnimation(animation);
            }
        }
        this.scrollViewEndContent.setLayoutAnimation(anima);
        this.mBtnContainer.setAnimation(animation);
        this.mBtnContainer.setVisibility(0);
        Message message = new Message();
        message.what = 4;
        this.handler.sendMessage(message);
    }

    private void outputAnim() {
        final Animation animation = AnimationUtils.loadAnimation(this.mActivity, R.anim.mainscreen_optimize_steptwo);
        Animation animation_hide = AnimationUtils.loadAnimation(this.mActivity, R.anim.mainscreen_optimize_hide);
        final LayoutAnimationController anima = new LayoutAnimationController(animation, 0.2f);
        anima.setOrder(1);
        animation_hide.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation a) {
                PowerWholeCheckFragment.this.mCircleImageOptimize.updateSocre(0, SystemManagerConst.CIRCLE_DEGREE);
                PowerWholeCheckFragment.this.mBtnContainer.setAnimation(animation);
                HwLog.i(PowerWholeCheckFragment.TAG, "optimize item count = " + PowerWholeCheckFragment.this.mCheckAllLoading.getChildCount());
                PowerWholeCheckFragment.this.mCheckAllLoading.setLayoutAnimation(anima);
                PowerWholeCheckFragment.this.mCheckAllLoading.startLayoutAnimation();
            }

            public void onAnimationRepeat(Animation a) {
            }

            public void onAnimationEnd(Animation a) {
                PowerWholeCheckFragment.this.doSthOnAnimationEnd();
            }
        });
        this.mCircleLayout.startAnimation(animation_hide);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                PowerWholeCheckFragment.this.mCheckAllLoading.setVisibility(8);
                if (!HSMConst.isLand()) {
                    PowerWholeCheckFragment.this.mNuoyiUpParent.setVisibility(8);
                }
                PowerWholeCheckFragment.this.mCircleLayout.setVisibility(8);
                PowerWholeCheckFragment.this.mBtnContainer.setVisibility(8);
                PowerWholeCheckFragment.this.inputAnim();
            }
        }, animation_hide.getDuration() + 50);
    }

    private void doSthOnAnimationEnd() {
        if (!this.mAnimEnd) {
            this.mAnimEnd = true;
            if (!HSMConst.isLand()) {
                this.mNuoyiUpParent.setVisibility(8);
            }
            this.mCircleLayout.setVisibility(8);
            this.mBtnContainer.setVisibility(8);
            inputAnim();
        }
    }

    private void displayOptimized() {
        ViewGroup textGroup;
        if (this.mBgAppCloseNum > 0 && this.backAppList.size() > 0) {
            View optView = this.layoutInflater.inflate(R.layout.power_optimize_bgconsume_item_layout, this.scrollViewEndContent, false);
            ((TextView) optView.findViewById(R.id.title)).setText(String.format(this.mAppContext.getResources().getQuantityString(R.plurals.power_wholecheck_close_bgApps, this.mBgAppCloseNum, new Object[]{Integer.valueOf(this.mBgAppCloseNum)}), new Object[0]));
            this.scrollViewEndContent.addView(optView);
        }
        if (this.mOptimizedSettingsList != null && this.mOptimizedSettingsList.size() > 0) {
            optView = this.layoutInflater.inflate(R.layout.power_optimize_parent_item_layout, this.scrollViewEndContent, false);
            ((TextView) optView.findViewById(R.id.parent_title)).setText(R.string.power_wholecheck_optimized_settings_item);
            textGroup = (ViewGroup) optView.findViewById(R.id.textitem_container);
            for (PowerDetectItem item : this.mOptimizedSettingsList) {
                View singleitem = this.layoutInflater.inflate(R.layout.power_optimize_child_item_layout, textGroup, false);
                ((TextView) singleitem.findViewById(R.id.child_title)).setText(item.getTitle());
                textGroup.addView(singleitem);
            }
            this.scrollViewEndContent.addView(optView);
        }
        if (this.mBestSaveSettingsList != null && this.mBestSaveSettingsList.size() > 0) {
            optView = this.layoutInflater.inflate(R.layout.power_optimize_parent_item_layout, this.scrollViewEndContent, false);
            ((TextView) optView.findViewById(R.id.parent_title)).setText(R.string.power_wholecheck_best_saving_item);
            textGroup = (ViewGroup) optView.findViewById(R.id.textitem_container);
            for (PowerDetectItem item2 : this.mBestSaveSettingsList) {
                singleitem = this.layoutInflater.inflate(R.layout.power_optimize_child_item_layout, textGroup, false);
                ((TextView) singleitem.findViewById(R.id.child_title)).setText(item2.getTitle());
                textGroup.addView(singleitem);
            }
            this.scrollViewEndContent.addView(optView);
        }
        HwLog.i(TAG, "scrollViewEndContent child count = " + this.scrollViewEndContent.getChildCount());
        this.mContanerResult.addView(this.scrollViewEnd);
    }

    private void updateProgress(boolean anima) {
        HwLog.i(TAG, "updateInfo called");
        if (anima) {
            this.mCircleImageOptimize.updateSocre(this.optimizeProgressNum, 500);
            this.mOptimizeRollView.setNumberByDuration(this.optimizeProgressNum, 500);
        } else {
            this.mCircleImageOptimize.updateScoreImmidiately(this.optimizeProgressNum);
            this.mOptimizeRollView.setNumberByDuration(this.optimizeProgressNum, 0);
        }
        if (!GlobalContext.getContext().getResources().getBoolean(R.bool.spaceclean_percent_small_mode)) {
            this.mOptimizeRollViewunit.setVisibility(8);
        }
    }

    private void optimiezeDelay(long delayTime) {
        try {
            Thread.sleep(delayTime);
        } catch (InterruptedException e) {
            HwLog.i(TAG, "OptimizeTask, doInBackground is interrupted");
        }
    }

    private void killAppsByPackageName(Context context, String pkg) {
        ((ActivityManager) context.getSystemService("activity")).forceStopPackage(pkg);
        HwLog.i(TAG, "killAppsByPackageName Force stop package: " + pkg);
    }

    private void initializeData() {
        this.backAppList = this.mStatsHelper.getPowerAppList(this.mAppContext, false);
        this.mItems.add(new WlanDetectItem());
        this.mItems.add(new BluetoothDetectItem());
        this.mItems.add(new ProtectAppDetectItem(this.mProtectAppSize));
        if (isSupportAutoBrightness(this.mAppContext)) {
            this.mItems.add(new BrightnessDetectItem());
        }
        this.mItems.add(new ScreenTimeout());
        this.mItems.add(new MobileDataDetectItem());
        this.mItems.add(new GpsDetectItem());
        this.mItems.add(new AutoSyncDetectItem());
        this.mItems.add(new VibrateDetectItem());
        this.mItems.add(new FeedBackDetectitem());
        this.mItems.add(new AutoRotationDetectItem());
        this.mGroupItems.clear();
        this.mGroupItems.put(Integer.valueOf(2), null);
        this.mGroupItems.put(Integer.valueOf(3), null);
        this.mGroupItems.put(Integer.valueOf(4), null);
        Iterator<PowerDetectItem> it = this.mItems.iterator();
        while (it.hasNext()) {
            PowerDetectItem item = (PowerDetectItem) it.next();
            if (item.isEnable()) {
                item.doScan();
                HwLog.i(TAG, "type = " + item.getItemType() + " isoptimized= " + item.isOptimized());
                divideGroup(item);
            } else {
                HwLog.i(TAG, "item is not enabled, remove it, itemType:" + item.getItemType());
                it.remove();
            }
        }
        this.mOptimizedSettingsList = (List) this.mGroupItems.get(Integer.valueOf(2));
        this.mManualOptimizSettingsList = (List) this.mGroupItems.get(Integer.valueOf(3));
        this.mBestSaveSettingsList = (List) this.mGroupItems.get(Integer.valueOf(4));
    }

    private boolean isSupportAutoBrightness(Context context) {
        boolean z = true;
        if (context == null) {
            return true;
        }
        SensorManager sensors = (SensorManager) context.getSystemService("sensor");
        if (sensors == null) {
            return true;
        }
        if (sensors.getDefaultSensor(5) == null) {
            z = false;
        }
        return z;
    }

    private void divideGroup(PowerDetectItem item) {
        int groupType;
        int itemType = item.getItemType();
        if (itemType == 2 || itemType == 3) {
            groupType = 2;
        } else if (item.isOptimized()) {
            groupType = 4;
        } else {
            groupType = 3;
        }
        if (this.mGroupItems.get(Integer.valueOf(groupType)) == null) {
            List<PowerDetectItem> tList = new ArrayList();
            tList.add(item);
            this.mGroupItems.put(Integer.valueOf(groupType), tList);
            return;
        }
        tList = (List) this.mGroupItems.get(Integer.valueOf(groupType));
        tList.add(item);
        this.mGroupItems.put(Integer.valueOf(groupType), tList);
    }

    private int getBenefitOneKeyOptimize() {
        if (this.mTimeByCurrentBattery != null) {
            return (int) Double.parseDouble(this.mTimeByCurrentBattery.get(PowerManagementModel.BENEFIT_ONE_KEY_OPTIMIZE_KEY).toString());
        }
        return 0;
    }

    public void onBackPressed() {
        if (getActivity() != null && getActivity().isResumed()) {
            HwLog.i(TAG, " PowerWholeCheck is onBackPressed ");
            finishWholeCheckActivity();
        }
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
            this.mAppContext.getContentResolver().registerContentObserver(System.getUriFor("screen_brightness_mode"), true, this.mBrightnessModeObserver);
            this.mAppContext.getContentResolver().registerContentObserver(System.getUriFor("screen_brightness"), true, this.mBrightnessObserver);
            this.mAppContext.getContentResolver().registerContentObserver(System.getUriFor("screen_auto_brightness_adj"), true, this.mBrightnessObserver);
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
            RotationPolicy.registerRotationPolicyListener(this.mAppContext, this.mRotationPolicyListener, -1);
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.media.RINGER_MODE_CHANGED");
            this.mActivity.registerReceiver(this.mVibrateChangedReceiver, filter);
            this.mObserved = true;
        }
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
        this.mActivity.registerReceiver(mBatInfoReceiver, intentFilter);
        this.isregister = true;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.layoutInflater = inflater;
        this.fragmentView = this.layoutInflater.inflate(R.layout.power_wholecheck_main, container, false);
        setscrollViewloadOverScrollMode();
        findControllerView();
        setOnClickListener();
        turnToStartOptimize();
        return this.fragmentView;
    }

    private void setscrollViewloadOverScrollMode() {
        this.mCheckAllLoading = (ScrollView) this.fragmentView.findViewById(R.id.power_loading);
        this.mCheckAllLoading.setOverScrollMode(2);
    }

    private void setOnClickListener() {
        this.mPowerWholeCheck.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (PowerWholeCheckFragment.this.mPowerWholeCheck.getText().equals(PowerWholeCheckFragment.this.getString(R.string.alert_dialog_cancel))) {
                    HsmStat.statE(Events.E_POWER_WHOLECHECK_CANCLE);
                } else if (PowerWholeCheckFragment.this.mPowerWholeCheck.getText().equals(PowerWholeCheckFragment.this.getString(R.string.common_finish))) {
                    HsmStat.statE(Events.E_POWER_WHOLECHECK_COMPLETE);
                }
                PowerWholeCheckFragment.this.mHandler.removeMessages(0);
                PowerWholeCheckFragment.this.mHandler.sendMessageDelayed(PowerWholeCheckFragment.this.mHandler.obtainMessage(0), 80);
            }
        });
    }

    private void findControllerView() {
        this.mContanerResult = (ViewGroup) this.fragmentView.findViewById(R.id.power_check);
        this.scrollViewEnd = (ViewGroup) this.fragmentView.findViewById(R.id.scroll_view_end);
        this.scrollViewEndContent = (ViewGroup) this.fragmentView.findViewById(R.id.scroll_view_end_content);
        this.scrollViewEndContent.getParent().requestDisallowInterceptTouchEvent(true);
        this.mContanerResult.removeAllViews();
        this.mPowerWholeCheck = (Button) this.fragmentView.findViewById(R.id.consume_check_btn);
        this.mCheckInfoView = (TextView) this.fragmentView.findViewById(R.id.checkinfo_textview);
        this.bgConsumeText = (PowerOptimizeItemRollingView) this.fragmentView.findViewById(R.id.bg_consume_success);
        this.optimizedText = (PowerOptimizeItemRollingView) this.fragmentView.findViewById(R.id.optimized_items_success);
        this.manualText = (PowerOptimizeItemRollingView) this.fragmentView.findViewById(R.id.manual_items_success);
        this.bestSavingText = (PowerOptimizeItemRollingView) this.fragmentView.findViewById(R.id.bestsaving_items_success);
        this.bgConsumeText.setItemNumResId(R.plurals.power_optimize_app_item_num);
        this.bgConsumeText.setNumberImmediately(ZERO_MANUAL_ITEM);
        this.optimizedText.setNumberImmediately(ZERO_MANUAL_ITEM);
        this.manualText.setNumberImmediately(ZERO_MANUAL_ITEM);
        this.bestSavingText.setNumberImmediately(ZERO_MANUAL_ITEM);
        this.mOptimizeRollView = (PowerMainScreenRollingView) this.fragmentView.findViewById(R.id.score_optimize);
        this.mOptimizeRollViewunit = (TextView) this.fragmentView.findViewById(R.id.score_unit_optimize);
        this.mBtnContainer = (RelativeLayout) this.fragmentView.findViewById(R.id.btn_container);
        this.mCircleImageOptimize = (MainCircleProgressView) this.fragmentView.findViewById(R.id.scan_optimize);
        this.mNuoyiUpParent = (RelativeLayout) this.fragmentView.findViewById(R.id.power_nuoyi_up_parent);
        this.mCircleLayout = (RelativeLayout) this.fragmentView.findViewById(R.id.upper_layout_optimize);
        this.mResultUpParent = (LinearLayout) this.fragmentView.findViewById(R.id.power_result_parent);
        this.mPowerMainLayout = (RelativeLayout) this.fragmentView.findViewById(R.id.power_main_listview);
        initNuoyiUpParent(HSMConst.isLand());
    }

    public void onResume() {
        super.onResume();
        this.mCircleImageOptimize.setCompleteStatus();
        this.mProtectAppSize = ProtectAppControl.getInstance(this.mAppContext).getProtectNum();
        HwLog.i(TAG, "mProtectAppSize = " + this.mProtectAppSize);
        if (!this.isregister) {
            registerIntent();
        }
        if (this.isOptimizeOver) {
            refreshOptimizeResult(4);
        }
    }

    public void onPause() {
        this.mHandler.removeCallbacks(this.mObserve);
        if (this.isregister) {
            this.mActivity.unregisterReceiver(mBatInfoReceiver);
            this.isregister = false;
        }
        super.onPause();
    }

    public void onDestroy() {
        if (this.isregister) {
            this.mActivity.unregisterReceiver(mBatInfoReceiver);
            this.isregister = false;
        }
        unRegisterObservers();
        super.onDestroy();
    }

    private void unRegisterObservers() {
        this.mHandler.removeCallbacks(this.mObserve);
        if (this.mObserved) {
            this.mAppContext.getContentResolver().unregisterContentObserver(this.mBrightnessModeObserver);
            this.mAppContext.getContentResolver().unregisterContentObserver(this.mBrightnessObserver);
            this.mAppContext.getContentResolver().unregisterContentObserver(this.mMobileDataObserver);
            this.mAppContext.getContentResolver().unregisterContentObserver(this.mGPSObserver);
            this.mAppContext.getContentResolver().unregisterContentObserver(this.mSleepTimeOutObserver);
            ContentResolver.removeStatusChangeListener(this.mStatusChangeListenerHandle);
            this.mAppContext.getContentResolver().unregisterContentObserver(this.mFeedBackObserver);
            RotationPolicy.unregisterRotationPolicyListener(this.mAppContext, this.mRotationPolicyListener);
            this.mActivity.unregisterReceiver(this.mVibrateChangedReceiver);
            this.mObserved = false;
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initNuoyiUpParent(newConfig.orientation == 2);
    }

    private void initNuoyiUpParent(boolean isLand) {
        if (this.mNuoyiUpParent != null && this.mPowerMainLayout != null) {
            int nuoyiLeftWidth;
            LayoutParams mNuoyiUpParentParams = (LayoutParams) this.mNuoyiUpParent.getLayoutParams();
            if (isLand) {
                nuoyiLeftWidth = HSMConst.getNuoyiLeftWidth();
            } else {
                nuoyiLeftWidth = -1;
            }
            mNuoyiUpParentParams.width = nuoyiLeftWidth;
            mNuoyiUpParentParams.height = -1;
            this.mNuoyiUpParent.setLayoutParams(mNuoyiUpParentParams);
            LayoutParams mPowerMainLayoutParams = (LayoutParams) this.mPowerMainLayout.getLayoutParams();
            if (isLand) {
                mPowerMainLayoutParams.removeRule(3);
                mPowerMainLayoutParams.addRule(17, this.mNuoyiUpParent.getId());
            } else {
                mPowerMainLayoutParams.addRule(3, this.mNuoyiUpParent.getId());
                mPowerMainLayoutParams.removeRule(17);
            }
            this.mPowerMainLayout.setLayoutParams(mPowerMainLayoutParams);
            if (this.mResultPresentTask != null && !Status.PENDING.equals(this.mResultPresentTask.getStatus())) {
                ViewGroup vg = (ViewGroup) this.mPowerOptimizeControl.newView().getParent();
                if (vg != null) {
                    vg.removeViewAt(0);
                }
                getNuoyiUpperParent(isLand).addView(this.mPowerOptimizeControl.newView(), 0);
                if (this.mPowerOptimizeControl instanceof PowerOptimizeTimeControl) {
                    ((PowerOptimizeTimeControl) this.mPowerOptimizeControl).onConfigurationChanged();
                }
                this.mNuoyiUpParent.setVisibility(isLand ? 0 : 8);
            } else if (this.mCheckInfoView != null) {
                ((LayoutParams) this.mCheckInfoView.getLayoutParams()).bottomMargin = GlobalContext.getContext().getResources().getDimensionPixelSize(R.dimen.hsm_nuoyi_circleinfo_marginbottom);
            }
        }
    }

    private void refreshOptimizeResult(int itemType) {
        if (this.mManualOptimizSettingsList != null) {
            for (PowerDetectItem item : this.mManualOptimizSettingsList) {
                if (itemType == 4) {
                    boolean flag = false;
                    if (!item.isOptimized()) {
                        flag = true;
                    }
                    item.setExData(this.mProtectAppSize);
                    if (this.mUnoptimizedItems.containsKey(Integer.valueOf(item.getItemType()))) {
                        ((PowerUnOptimizedController) this.mUnoptimizedItems.get(Integer.valueOf(item.getItemType()))).update(item);
                        if (item.isOptimized() && flag) {
                            this.manualOptimizeNum--;
                            updateNeedManualItemNum();
                        }
                    }
                    return;
                } else if (itemType == item.getItemType() && !item.isOptimized()) {
                    if (this.mUnoptimizedItems.containsKey(Integer.valueOf(item.getItemType()))) {
                        HwLog.i(TAG, "refreshOptimizeResult itemType = " + itemType);
                        item.doRefreshOptimize();
                        this.manualOptimizeNum--;
                        updateNeedManualItemNum();
                        ((PowerUnOptimizedController) this.mUnoptimizedItems.get(Integer.valueOf(item.getItemType()))).update(item);
                    }
                }
            }
        }
    }

    private void updateNeedManualItemNum() {
        if (this.mManuItemNumView != null) {
            if (this.manualOptimizeNum == ZERO_MANUAL_ITEM) {
                this.mManuItemNumView.setVisibility(8);
            }
            this.mManuItemNumView.setText(String.format(this.mAppContext.getResources().getQuantityString(R.plurals.power_optimize_manual_items_text, this.manualOptimizeNum, new Object[]{Integer.valueOf(this.manualOptimizeNum)}), new Object[0]));
        }
    }

    private boolean isMulityCard(Context context) {
        if (this.sMultiSim != null) {
            return this.sMultiSim.booleanValue();
        }
        this.sMultiSim = Boolean.valueOf(TelephonyManager.from(context).isMultiSimEnabled());
        return this.sMultiSim.booleanValue();
    }
}
