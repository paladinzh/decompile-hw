package com.huawei.powergenie.core.thermal;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Log;
import android.util.Xml;
import com.huawei.powergenie.api.ActionsExportMap;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.api.IPolicy;
import com.huawei.powergenie.api.ISdkService;
import com.huawei.powergenie.api.IThermal;
import com.huawei.powergenie.api.Multimap;
import com.huawei.powergenie.core.BaseService;
import com.huawei.powergenie.core.PowerAction;
import com.huawei.powergenie.core.ThermalAction;
import com.huawei.powergenie.core.security.DecodeXmlFile;
import com.huawei.powergenie.integration.adapter.BroadcastAdapter;
import com.huawei.powergenie.integration.adapter.HardwareAdapter;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import com.huawei.powergenie.integration.eventhub.Event;
import com.huawei.powergenie.integration.eventhub.HookEvent;
import com.huawei.powergenie.integration.eventhub.MsgEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class ThermalStateManager extends BaseService implements IThermal {
    private static final String INVALID_STRING_CHAEGE_PARAM = null;
    private static final String THERMALCONFIG_PRODUCT = ("thermald_" + SystemProperties.get("ro.product.name", "") + ".xml");
    private static final String THERMAL_PFMC_CONFIG_PRODUCT = ("thermald_performance_" + SystemProperties.get("ro.product.name", "") + ".xml");
    private static final String THERMAL_VR_CONFIG_PRODUCT = ("thermald_vr_" + SystemProperties.get("ro.product.name", "") + ".xml");
    private static final HashMap<String, String> mActionInterface = new HashMap();
    private static int mBatteryLevel = 0;
    private static HashMap<String, Integer> mCustCombActionId = new HashMap();
    private final HashMap<Integer, Integer> mA15CpuMaxFreq = new HashMap();
    private final HashMap<Integer, Integer> mAcChargeAuxLimit = new HashMap();
    private final HashMap<Integer, Integer> mAcChargeLimit = new HashMap();
    private boolean mAlarmCleared = false;
    private boolean mAlarmRaised = false;
    private final HashMap<Integer, Integer> mAppAction = new HashMap();
    private boolean mBatteryCleared = false;
    private final HashMap<Integer, Integer> mBatteryLimit = new HashMap();
    private boolean mBatteryTrigged = false;
    private final HashMap<Integer, Integer> mCallBatteryLimit = new HashMap();
    private final HashMap<Integer, Integer> mCameraFps = new HashMap();
    private int mCatchActionlvl = -1;
    private Context mContext;
    private final HashMap<Integer, Integer> mCpu1MaxFreq = new HashMap();
    private final HashMap<Integer, Integer> mCpu2MaxFreq = new HashMap();
    private final HashMap<Integer, Integer> mCpu3MaxFreq = new HashMap();
    private final HashMap<Integer, Integer> mCpuMaxFreq = new HashMap();
    private int mCurA15Cpufreq = 0;
    private int mCurAcChargeAuxLimt = 0;
    private int mCurAcChargeLimt = 0;
    private int mCurActionID = 208;
    private int mCurAppAction = 0;
    private String mCurCallBatteryPolicy = "0";
    private int mCurCallBatteryTemp = -100000;
    private int mCurCallBatteryType = -1;
    private int mCurCameraFps = 0;
    private int mCurChargingLvl = 0;
    private int mCurCpu1freq = 0;
    private int mCurCpu2freq = 0;
    private int mCurCpu3freq = 0;
    private int mCurCpufreq = 0;
    private int mCurDirectChargeLimt = 0;
    private int mCurFlashLimt = 0;
    private int mCurForkOnBig = -1;
    private int mCurFrontFlashLimt = 0;
    private int mCurGpufreq = 0;
    private int mCurIpaPower = 0;
    private int mCurIpaSwitch = 0;
    private int mCurIpaTemp = 0;
    private int mCurLcdLimt = 0;
    private int mCurPAFallbackLimt = 0;
    private final HashMap<Integer, Integer> mCurTemp = new HashMap();
    private int mCurThresholdDownPolicy = 0;
    private int mCurThresholdUpPolicy = 0;
    private int mCurUsbChargeAuxLimt = 0;
    private int mCurUsbChargeLimt = 0;
    private int mCurVRWarningLevel = 0;
    private int mCurWlanLvl = 0;
    private int mCurpopState = 0;
    private String mCurrentScreenCtrlBcurrentVal = INVALID_STRING_CHAEGE_PARAM;
    private final HashMap<Integer, String> mCustScenes = new HashMap();
    private final HashMap<Integer, Integer> mDirectChargeLimit = new HashMap();
    private int mDisabledLowSensorType = -1;
    private int mDisabledLowTemp = -5;
    private Multimap<Integer, String> mExecAction = new Multimap();
    private final HashMap<Integer, Integer> mFlashLimit = new HashMap();
    private final HashMap<Integer, Integer> mForkOnBig = new HashMap();
    private final HashMap<Integer, Integer> mFrontFlashLimit = new HashMap();
    private final HashMap<Integer, Integer> mGpuMaxFreq = new HashMap();
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            boolean z = true;
            switch (msg.what) {
                case 100:
                    if (!ThermalStateManager.this.mIsWarningBroadcast) {
                        BroadcastAdapter.sendThermalComUIEvent(ThermalStateManager.this.mContext, "warning_temperature", "hot");
                        ThermalStateManager.this.mIsWarningBroadcast = true;
                        return;
                    }
                    return;
                case 101:
                    if (ThermalStateManager.this.mIsWarningBroadcast) {
                        BroadcastAdapter.sendThermalComUIEvent(ThermalStateManager.this.mContext, "warning_temperature", "normal");
                        ThermalStateManager.this.mIsWarningBroadcast = false;
                        return;
                    }
                    return;
                case 102:
                    if (!ThermalStateManager.this.mIsColdChargingBroadcast) {
                        BroadcastAdapter.sendThermalComUIEvent(ThermalStateManager.this.mContext, "charging_lowtemperature", "cold");
                        ThermalStateManager.this.mIsColdChargingBroadcast = true;
                        return;
                    }
                    return;
                case 103:
                    if (ThermalStateManager.this.mIsColdChargingBroadcast) {
                        BroadcastAdapter.sendThermalComUIEvent(ThermalStateManager.this.mContext, "charging_lowtemperature", "normal");
                        ThermalStateManager.this.mIsColdChargingBroadcast = false;
                        return;
                    }
                    return;
                case 200:
                    ThermalStateManager thermalStateManager = ThermalStateManager.this;
                    if (msg.arg1 != 1) {
                        z = false;
                    }
                    thermalStateManager.handleThermalPolicyChange(z);
                    return;
                case 201:
                    ThermalStateManager.this.handleQuickChargeStateChange(((Boolean) msg.obj).booleanValue());
                    return;
                case 202:
                    ThermalStateManager.this.handleVRModeChange(((Boolean) msg.obj).booleanValue());
                    return;
                case 300:
                    synchronized (ThermalStateManager.this.mLock) {
                        ThermalStateManager.this.mScrnCtrlChargeCounter.incrementAndGet();
                    }
                    if (ThermalStateManager.this.mSimDevt != null) {
                        ThermalStateManager.this.handleThermalAction(ThermalStateManager.this.mSimDevt, false);
                        return;
                    }
                    return;
                case 301:
                    ThermalStateManager.this.checkQuickCharger();
                    return;
                default:
                    return;
            }
        }
    };
    private final HashMap<Integer, Integer> mHmpThresholdDown = new HashMap();
    private final HashMap<Integer, Integer> mHmpThresholdUp = new HashMap();
    private final ICoreContext mICoreContext;
    private final IDeviceState mIDeviceState;
    private final IPolicy mIPolicy;
    private final ISdkService mISdkService;
    private final HashMap<Integer, Integer> mIpaPower = new HashMap();
    private final HashMap<Integer, Integer> mIpaSwitch = new HashMap();
    private final HashMap<Integer, Integer> mIpaTemp = new HashMap();
    private boolean mIsColdChargingBroadcast = false;
    private boolean mIsConfigBatteryLevel = false;
    private boolean mIsLowTempWaringBroadcast = false;
    private boolean mIsQuickChargeOff = false;
    private boolean mIsSupportQCOff = false;
    private boolean mIsSwitchBroadcast = false;
    private boolean mIsVRMode = false;
    private boolean mIsWarningBroadcast = false;
    private final HashMap<Integer, Integer> mLcdLimit = new HashMap();
    private Object mLock = new Object();
    private final HashMap<Integer, Boolean[]> mLvlAlarm = new HashMap();
    private boolean mMutilSensorBatteryClr = false;
    private String mNotLimitChargeVal = INVALID_STRING_CHAEGE_PARAM;
    private final HashMap<Integer, Integer> mPAFallbackLimit = new HashMap();
    private int mParentActionId = 208;
    private String mParentPowerPkgName = null;
    private final HashMap<Integer, Integer> mPopUpDialogState = new HashMap();
    private int mPostPoneTimeInMSec = -1000;
    private final ContentObserver mQuickChargeDBObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            boolean isClosed = ThermalStateManager.this.isQuickChargeSwitchClosed();
            Log.i("ThermalStateManager", "quick charge DB change ! isClosed = " + isClosed);
            ThermalStateManager.this.notifyQuickChargeState(isClosed);
        }
    };
    private final HashMap<Integer, HashMap<Integer, SensorTempAction>> mSceneSensorActions = new HashMap();
    private String mScreenOnMaxBcurrentVal = INVALID_STRING_CHAEGE_PARAM;
    private volatile AtomicInteger mScrnCtrlChargeCounter = new AtomicInteger(0);
    private boolean mScrnStateCtrlChargeActive = false;
    private boolean mScrnStateCtrlChargeEnable = false;
    private final HashMap<Integer, SensorTempAction> mSensorActions = new HashMap();
    private HookEvent mSimDevt = null;
    private boolean mSupportPfmcThermalPolicy = false;
    private boolean mSupportVRMode = false;
    private final HashMap<Integer, Integer> mUsbChargeAuxLimit = new HashMap();
    private final HashMap<Integer, Integer> mUsbChargeLimit = new HashMap();
    private final HashMap<Integer, Integer> mVRWarningLevel = new HashMap();
    private int mWarningTemp = 68;
    private int mWarningType = 0;
    private final HashMap<Integer, Integer> mWlanLimit = new HashMap();

    protected static class ActionItem {
        private int mActionSize = 0;
        protected final HashMap<String, String> mActions = new HashMap();
        protected int mBatLevelClear = -1;
        protected int mBatLevelTrigger = -1;

        protected ActionItem() {
        }

        protected void setBatTrigger(int battery) {
            this.mBatLevelTrigger = battery;
        }

        protected void setBatClear(int battery) {
            this.mBatLevelClear = battery;
        }

        protected boolean addAction(String action, String value) {
            if (this.mActionSize >= 16) {
                return false;
            }
            this.mActions.put(action, value);
            this.mActionSize++;
            return true;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(" mBatLevelTrigger =").append(this.mBatLevelTrigger);
            builder.append(" mBatLevelClear =").append(this.mBatLevelClear);
            builder.append(" action =").append(this.mActions.toString());
            return builder.toString();
        }
    }

    protected static class SensorTempAction {
        private int mLevelClear = -100000;
        private int mLevelTrigger = -100000;
        protected int mNumThresholds = 0;
        protected int mPostiveMiniClrTemperature = Integer.MAX_VALUE;
        protected final String mSensorName;
        protected final int mSensorType;
        protected final TriggerAction[] mTriggerAction = new TriggerAction[8];

        protected SensorTempAction(int sensorType, String sensorName) {
            this.mSensorType = sensorType;
            this.mSensorName = sensorName;
            this.mNumThresholds = 0;
        }

        protected boolean addTriggerAction(TriggerAction triggerAction) {
            boolean ret;
            if (triggerAction.mLevelTrigger <= this.mLevelTrigger || triggerAction.mLevelClear <= this.mLevelClear || this.mNumThresholds >= 8) {
                ret = false;
                Log.e("ThermalStateManager", "thermal config error, because of the format of file");
            } else {
                this.mTriggerAction[this.mNumThresholds] = triggerAction;
                this.mNumThresholds++;
                ret = true;
            }
            if (triggerAction.mLevelClear > 0 && this.mPostiveMiniClrTemperature > triggerAction.mLevelClear) {
                this.mPostiveMiniClrTemperature = triggerAction.mLevelClear;
            }
            this.mLevelTrigger = triggerAction.mLevelTrigger;
            this.mLevelClear = triggerAction.mLevelClear;
            return ret;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(" Sensor Type =").append(this.mSensorType);
            builder.append(" Sensor Name =").append(this.mSensorName);
            builder.append(" mPostiveMiniClrTemperature = ").append("").append(this.mPostiveMiniClrTemperature);
            for (int i = 0; i < this.mNumThresholds; i++) {
                builder.append(" Action List=").append(this.mTriggerAction[i].toString());
            }
            return builder.toString();
        }
    }

    protected static class TriggerAction {
        protected final ActionItem[] mActionItems;
        protected int mActionSize;
        protected boolean[] mActionState;
        protected HashMap<String, String> mActions;
        protected int mLevelClear;
        protected final HashMap<Integer, Integer> mLevelClearBattery;
        protected int mLevelTrigger;
        protected int mNumActionItems;

        protected TriggerAction() {
            this.mActionSize = 0;
            this.mActionItems = new ActionItem[8];
            this.mActionState = new boolean[8];
            this.mNumActionItems = 0;
            this.mLevelTrigger = -100000;
            this.mLevelClear = -100000;
            this.mLevelClearBattery = new HashMap();
            this.mActions = null;
            this.mActionSize = 0;
            this.mNumActionItems = 0;
        }

        protected void setTrigger(int temp) {
            this.mLevelTrigger = temp;
        }

        protected void setClear(int temp) {
            this.mLevelClear = temp;
        }

        protected void addBatteryClear(int sensor, int temp) {
            this.mLevelClearBattery.put(Integer.valueOf(sensor), Integer.valueOf(temp));
        }

        protected boolean addAction(String action, String value) {
            if (this.mActions == null) {
                this.mActions = new HashMap();
            }
            if (this.mActionSize >= 16) {
                return false;
            }
            this.mActions.put(action, value);
            this.mActionSize++;
            return true;
        }

        protected void addActionItem(ActionItem actionItem) {
            this.mActionItems[this.mNumActionItems] = actionItem;
            this.mActionState[this.mNumActionItems] = false;
            this.mNumActionItems++;
        }

        protected int getActionItemsNum() {
            return this.mNumActionItems;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(" thresholds =").append(this.mLevelTrigger);
            builder.append(" thresholds_clr =").append(this.mLevelClear);
            builder.append(" thresholds_clr_battery =").append(this.mLevelClearBattery.toString());
            for (int i = 0; i < this.mNumActionItems; i++) {
                builder.append(" Action List=").append(this.mActionItems[i].toString());
                builder.append(" Action state =").append(this.mActionState[i]);
            }
            if (this.mActions != null) {
                builder.append(" no config battery, action =").append(this.mActions.toString());
            }
            return builder.toString();
        }
    }

    public ThermalStateManager(ICoreContext context, IPolicy policy, IDeviceState device) {
        this.mContext = context.getContext();
        this.mICoreContext = context;
        this.mIDeviceState = device;
        this.mIPolicy = policy;
        this.mISdkService = (ISdkService) this.mICoreContext.getService("sdk");
    }

    public void start() {
        initialize();
        addThermalActions();
    }

    private void initialize() {
        boolean loadResult;
        this.mSupportPfmcThermalPolicy = checkPfmcThermalConfigFileExist();
        this.mIsSupportQCOff = checkQCThermalConfigFileExist();
        this.mSupportVRMode = checkVRThermalConfigFileExist();
        Log.i("ThermalStateManager", "in initialize, mSupportPfmcThermalPolicy = " + this.mSupportPfmcThermalPolicy + " ,mIsSupportQCOff = " + this.mIsSupportQCOff + " ,mSupportVRMode = " + this.mSupportVRMode);
        if (this.mIsSupportQCOff) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(301), 10000);
        }
        if (this.mSupportPfmcThermalPolicy) {
            boolean initPfmcThermalConfig = this.mIPolicy.isOffPowerMode();
            loadResult = loadThermalConf(initPfmcThermalConfig);
            if (!loadResult && initPfmcThermalConfig) {
                loadResult = loadThermalConf(false);
            }
        } else {
            loadResult = loadThermalConf(false);
        }
        if (loadResult) {
            Log.i("ThermalStateManager", "mutil battery clr feature:" + this.mMutilSensorBatteryClr);
        } else {
            Log.e("ThermalStateManager", "error: because of thermald config");
        }
        if (this.mScreenOnMaxBcurrentVal != INVALID_STRING_CHAEGE_PARAM) {
            this.mScrnCtrlChargeCounter.incrementAndGet();
        }
    }

    public void onInputHookEvent(HookEvent evt) {
        if (evt.getEventId() == 146) {
            if (this.mScreenOnMaxBcurrentVal != INVALID_STRING_CHAEGE_PARAM) {
                HookEvent tempDefinedEvent = evt;
                this.mSimDevt = new HookEvent(evt.getEventId());
                this.mSimDevt.updatePkgName(evt.getPkgName());
                this.mSimDevt.setValue1(evt.getValue1());
                this.mSimDevt.setValue2(evt.getValue2());
                this.mSimDevt.setValue3(evt.getValue3());
                this.mSimDevt.setValue4(evt.getValue4());
            }
            handleThermalAction(evt, true);
        }
    }

    public void onInputMsgEvent(MsgEvent evt) {
        int evtId = evt.getEventId();
        if (evtId == 322 || evtId == 323) {
            handleCallAction(evt);
        } else if (evtId == 308) {
            handleBatteryAction(evt);
        } else if (evtId == 300) {
            if (this.mScreenOnMaxBcurrentVal != INVALID_STRING_CHAEGE_PARAM) {
                if (this.mHandler.hasMessages(300)) {
                    this.mHandler.removeMessages(300);
                }
                synchronized (this.mLock) {
                    this.mScrnCtrlChargeCounter.incrementAndGet();
                }
                if (this.mSimDevt != null) {
                    Log.i("ThermalStateManager", "screen on, handle screen state ctrl charge immediately");
                    handleThermalAction(this.mSimDevt, false);
                }
            }
        } else if (evtId == 301) {
            if (this.mScreenOnMaxBcurrentVal != INVALID_STRING_CHAEGE_PARAM && this.mPostPoneTimeInMSec >= 0) {
                Log.i("ThermalStateManager", "will handle screen state ctrl charge after " + this.mPostPoneTimeInMSec + " ms");
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(300), (long) this.mPostPoneTimeInMSec);
            }
        } else if (evtId == 310 || evtId == 302 || evtId == 364) {
            if (!this.mIsSupportQCOff) {
                return;
            }
            if (evtId == 364) {
                MsgEvent event = evt;
                if ("1".equals(evt.getIntent().getStringExtra("quick_charge_status"))) {
                    this.mHandler.removeMessages(301);
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(301), 3000);
                    return;
                }
                return;
            }
            this.mHandler.removeMessages(301);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(301), 10000);
        } else if (evtId == 311 && this.mIsSupportQCOff) {
            this.mHandler.removeMessages(301);
        }
    }

    public ArrayList<Integer> getConfigThermalScene() {
        ArrayList<Integer> scenes = new ArrayList();
        for (Entry entry : this.mSceneSensorActions.entrySet()) {
            int sceneID = ((Integer) entry.getKey()).intValue();
            if (sceneID < 100000) {
                if (mCustCombActionId.containsValue(Integer.valueOf(sceneID))) {
                    for (Entry ety : mCustCombActionId.entrySet()) {
                        if (sceneID == ((Integer) ety.getValue()).intValue()) {
                            String key = (String) ety.getKey();
                            String[] idArray = key.split("\\|");
                            if (idArray != null && idArray.length >= 2) {
                                try {
                                    int subId = Integer.parseInt(idArray[0]);
                                    if (subId > 0) {
                                        scenes.add(Integer.valueOf(subId));
                                        if (PowerAction.mSubActionMap.containsKey(Integer.valueOf(subId))) {
                                            scenes.add((Integer) PowerAction.mSubActionMap.get(Integer.valueOf(subId)));
                                        }
                                    }
                                    try {
                                        int parentId = Integer.parseInt(idArray[1]);
                                        if (parentId > 0) {
                                            scenes.add(Integer.valueOf(parentId));
                                        }
                                    } catch (Exception e) {
                                    }
                                } catch (Exception e2) {
                                    Log.e("ThermalStateManager", "error for " + key);
                                }
                            }
                        }
                    }
                } else {
                    scenes.add(Integer.valueOf(sceneID));
                    if (PowerAction.mSubActionMap.containsKey(Integer.valueOf(sceneID))) {
                        scenes.add((Integer) PowerAction.mSubActionMap.get(Integer.valueOf(sceneID)));
                    }
                }
            }
        }
        Log.i("ThermalStateManager", "scenes:" + scenes);
        return scenes;
    }

    private void addThermalActions() {
        ArrayList<Integer> configScene = getConfigThermalScene();
        if (configScene != null) {
            for (Integer id : configScene) {
                if (configScene.size() != 1 || id.intValue() != 208) {
                    addAction(this.mICoreContext, id.intValue());
                    Log.d("ThermalStateManager", "add action:" + id);
                } else {
                    return;
                }
            }
        }
    }

    public boolean handleAction(PowerAction action) {
        if (!super.handleAction(action)) {
            return true;
        }
        int actionId = action.getActionId();
        String pkg = action.getPkgName();
        int vaildActionID = actionId;
        int subActionFlag = action.getSubFlag();
        if (subActionFlag == 2) {
            vaildActionID = this.mParentActionId;
        } else if (subActionFlag == 1) {
            int custId = matchCustComb(actionId, this.mParentPowerPkgName);
            if (custId != 0) {
                vaildActionID = custId;
            } else {
                Integer combId = (Integer) mCustCombActionId.get(Integer.toString(actionId) + "|" + Integer.toString(this.mParentActionId));
                vaildActionID = (combId == null || !this.mSceneSensorActions.containsKey(combId)) ? actionId : combId.intValue();
            }
        } else {
            this.mParentActionId = actionId;
            this.mParentPowerPkgName = action.getPkgName();
            vaildActionID = actionId;
            if (requireMatchCust(actionId, subActionFlag)) {
                int matchCustId = matchCustPkgName(pkg);
                if (matchCustId != 0) {
                    vaildActionID = matchCustId;
                    this.mParentActionId = matchCustId;
                }
            }
        }
        if (this.mSceneSensorActions.containsKey(Integer.valueOf(vaildActionID)) && vaildActionID != this.mCurActionID) {
            Log.i("ThermalStateManager", "handle action thermal policy:" + vaildActionID);
            handleActionThermalPolicyChange(vaildActionID);
            this.mCurActionID = vaildActionID;
        }
        return true;
    }

    private boolean requireMatchCust(int actionId, int subActionFlag) {
        if (subActionFlag == 2 || subActionFlag == 1) {
            return false;
        }
        switch (actionId) {
            case 210:
            case 234:
            case 236:
            case 245:
            case 246:
            case 258:
            case 259:
            case 267:
                return false;
            default:
                return true;
        }
    }

    private static int matchCustComb(int subActionID, String packageName) {
        if (packageName == null || packageName.equals("")) {
            return 0;
        }
        for (Entry entry : mCustCombActionId.entrySet()) {
            String key = (String) entry.getKey();
            if (key != null && key.startsWith(Integer.toString(subActionID)) && Pattern.compile(key, 66).matcher(packageName).find()) {
                return ((Integer) entry.getValue()).intValue();
            }
        }
        return 0;
    }

    private int matchCustPkgName(String packageName) {
        if (packageName == null || packageName.equals("")) {
            return 0;
        }
        int custActionId = 0;
        for (Entry entry : this.mCustScenes.entrySet()) {
            Integer actionid = (Integer) entry.getKey();
            String actionName = (String) entry.getValue();
            if (actionName == null) {
                Log.w("ThermalStateManager", "match cust pkg name, pkg is null!");
            } else if (actionName.equalsIgnoreCase(packageName)) {
                return actionid.intValue();
            } else {
                if (Pattern.compile(actionName, 66).matcher(packageName).find()) {
                    custActionId = actionid.intValue();
                }
            }
        }
        return custActionId;
    }

    private void handleActionThermalPolicyChange(int sceneID) {
        synchronized (this.mLock) {
            clearCurrentPolicy();
            clearCachedConf();
            this.mSensorActions.clear();
            this.mSensorActions.putAll((HashMap) this.mSceneSensorActions.get(Integer.valueOf(sceneID)));
            trigerThermal();
        }
    }

    private boolean checkPfmcThermalConfigFileExist() {
        if (new File("/product/etc/hwpg/", THERMAL_PFMC_CONFIG_PRODUCT).exists() || new File("/product/etc/hwpg/", "thermald_performance.xml").exists() || new File("/system/etc/", THERMAL_PFMC_CONFIG_PRODUCT).exists() || new File("/system/etc/", "thermald_performance.xml").exists()) {
            return true;
        }
        if (this.mIsQuickChargeOff) {
            return checkPfmcQCThermalConfigFileExist();
        }
        return false;
    }

    private boolean checkQCThermalConfigFileExist() {
        if (new File("/product/etc/hwpg/", "thermald_qcoff.xml").exists() || new File("/system/etc/", "thermald_qcoff.xml").exists()) {
            return true;
        }
        return checkPfmcQCThermalConfigFileExist();
    }

    private boolean checkPfmcQCThermalConfigFileExist() {
        if (new File("/product/etc/hwpg/", "thermald_performance_qcoff.xml").exists()) {
            return true;
        }
        return new File("/system/etc/", "thermald_performance_qcoff.xml").exists();
    }

    private boolean checkVRThermalConfigFileExist() {
        if (new File("/product/etc/hwpg/", "thermald_vr.xml").exists() || new File("/product/etc/hwpg/", THERMAL_VR_CONFIG_PRODUCT).exists() || new File("/system/etc/", "thermald_vr.xml").exists() || new File("/system/etc/", THERMAL_VR_CONFIG_PRODUCT).exists()) {
            return true;
        }
        return false;
    }

    private boolean isQuickChargeSwitchClosed() {
        int isClosed = -1;
        try {
            isClosed = System.getIntForUser(this.mContext.getContentResolver(), "smart_charge_switch", -1, -2);
        } catch (Exception e) {
            Log.e("ThermalStateManager", "error: get quick charge state fail !");
        }
        if (isClosed == 0) {
            return true;
        }
        return false;
    }

    public void notifyVRMode(boolean connect) {
        if (this.mSupportVRMode) {
            this.mHandler.removeMessages(202);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(202, Boolean.valueOf(connect)), 3000);
        }
    }

    public void notifyUsePfmcThermalPolicy(boolean usePfmcThermalConf) {
        if (this.mSupportPfmcThermalPolicy) {
            int i;
            if (this.mHandler.hasMessages(200)) {
                this.mHandler.removeMessages(200);
            }
            Handler handler = this.mHandler;
            if (usePfmcThermalConf) {
                i = 1;
            } else {
                i = 0;
            }
            this.mHandler.sendMessageDelayed(handler.obtainMessage(200, i, 0), 5000);
        }
    }

    private void notifyQuickChargeState(boolean isClosed) {
        if (isClosed == this.mIsQuickChargeOff) {
            Log.w("ThermalStateManager", "warning: quick charge state is no change, just return !");
            return;
        }
        this.mHandler.removeMessages(201);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(201, Boolean.valueOf(isClosed)), 5000);
    }

    private void handleThermalPolicyChange(boolean usePfmcThermalConf) {
        synchronized (this.mLock) {
            Log.w("ThermalStateManager", usePfmcThermalConf ? "Switch to performance thermal configure" : "Switch to default thermal configure");
            clearCurrentPolicy();
            clearCachedConf();
            if (!loadThermalConf(usePfmcThermalConf) && usePfmcThermalConf) {
                Log.e("ThermalStateManager", "error: failed to parse thermal file for performance, use the default");
                this.mIsQuickChargeOff = false;
                loadThermalConf(false);
                if (this.mIsSupportQCOff) {
                    this.mIsQuickChargeOff = isQuickChargeSwitchClosed();
                }
            }
        }
    }

    private void handleVRModeChange(boolean connect) {
        if (this.mSupportVRMode) {
            synchronized (this.mLock) {
                if (connect == this.mIsVRMode) {
                    Log.w("ThermalStateManager", "warning: same VR mode");
                    return;
                }
                this.mIsVRMode = connect;
                Log.i("ThermalStateManager", connect ? "Switch to VR thermal configure" : "Switch to close VR thermal configure");
                clearCurrentPolicy();
                clearCachedConf();
                loadThermalConf(this.mIPolicy.isOffPowerMode());
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleQuickChargeStateChange(boolean isClosed) {
        synchronized (this.mLock) {
            if (isClosed == this.mIsQuickChargeOff) {
                Log.w("ThermalStateManager", "warning: Quick charge state is no change, just return !");
                return;
            }
            this.mIsQuickChargeOff = isClosed;
            Log.i("ThermalStateManager", isClosed ? "Switch to off quick charge state thermal configure" : "Switch to quick charge state thermal configure");
            clearCurrentPolicy();
            clearCachedConf();
            if (!loadThermalConf(this.mIPolicy.isOffPowerMode())) {
                Log.e("ThermalStateManager", "error: failed to parse thermal file for quick charge, use the default!");
                this.mIsQuickChargeOff = false;
                loadThermalConf(false);
                this.mIsQuickChargeOff = isQuickChargeSwitchClosed();
            }
        }
    }

    private void clearCachedConf() {
        HashMap<Integer, Boolean[]> lvlAlarm = new HashMap();
        for (Entry entry : this.mLvlAlarm.entrySet()) {
            Integer sensorType = (Integer) entry.getKey();
            Boolean[] alarm = new Boolean[8];
            for (int i = 0; i < 8; i++) {
                alarm[i] = Boolean.valueOf(false);
            }
            lvlAlarm.put(sensorType, alarm);
        }
        this.mLvlAlarm.clear();
        this.mLvlAlarm.putAll(lvlAlarm);
        this.mCpuMaxFreq.clear();
        this.mCpu1MaxFreq.clear();
        this.mCpu2MaxFreq.clear();
        this.mCpu3MaxFreq.clear();
        this.mA15CpuMaxFreq.clear();
        this.mGpuMaxFreq.clear();
        this.mWlanLimit.clear();
        this.mLcdLimit.clear();
        this.mFlashLimit.clear();
        this.mFrontFlashLimit.clear();
        this.mBatteryLimit.clear();
        this.mCallBatteryLimit.clear();
        this.mPAFallbackLimit.clear();
        this.mUsbChargeLimit.clear();
        this.mAcChargeLimit.clear();
        this.mUsbChargeAuxLimit.clear();
        this.mAcChargeAuxLimit.clear();
        this.mHmpThresholdUp.clear();
        this.mHmpThresholdDown.clear();
        this.mPopUpDialogState.clear();
        this.mIpaTemp.clear();
        this.mIpaPower.clear();
        this.mIpaSwitch.clear();
        this.mCameraFps.clear();
        this.mAppAction.clear();
        this.mDirectChargeLimit.clear();
        this.mSensorActions.clear();
        resetScreenStateCharge();
    }

    private void trigerThermal() {
        HookEvent devt = new HookEvent(146);
        for (Entry entry : this.mSensorActions.entrySet()) {
            int sensorType = ((Integer) entry.getKey()).intValue();
            if (this.mCurTemp.containsKey(Integer.valueOf(sensorType))) {
                int curTmp = ((Integer) this.mCurTemp.get(Integer.valueOf(sensorType))).intValue();
                devt.setValue1("" + sensorType);
                devt.setValue2("" + curTmp);
                handleThermalAction(devt, false);
            }
        }
    }

    private void resetScreenStateCharge() {
        this.mNotLimitChargeVal = INVALID_STRING_CHAEGE_PARAM;
        this.mScreenOnMaxBcurrentVal = INVALID_STRING_CHAEGE_PARAM;
        this.mPostPoneTimeInMSec = -1000;
    }

    private void clearCurrentPolicy() {
        HookEvent devt = new HookEvent(146);
        for (Entry entry : this.mSensorActions.entrySet()) {
            int sensorType = ((Integer) entry.getKey()).intValue();
            int lowestTmp = ((SensorTempAction) entry.getValue()).mPostiveMiniClrTemperature;
            devt.setValue1("" + sensorType);
            devt.setValue2("" + lowestTmp);
            handleThermalAction(devt, false);
        }
    }

    private void handleBatteryAction(Event evt) {
        synchronized (this.mLock) {
            if (this.mIsConfigBatteryLevel) {
                this.mBatteryTrigged = false;
                this.mBatteryCleared = false;
                int level = ((MsgEvent) evt).getIntent().getIntExtra("level", 0);
                if (mBatteryLevel == level) {
                    return;
                }
                mBatteryLevel = level;
                for (Entry entry : this.mSensorActions.entrySet()) {
                    int sensorType = ((Integer) entry.getKey()).intValue();
                    int sensorTmp = getThermalTemp(sensorType);
                    if (sensorTmp != -100000) {
                        multipleToTrigger(sensorType, sensorTmp, level, true);
                    }
                }
                return;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleCallAction(Event evt) {
        synchronized (this.mLock) {
            if (this.mCurCallBatteryType == -1) {
                return;
            }
            ThermalAction thermalAction = generateThermalAction(((MsgEvent) evt).getTimeStamp(), this.mCurCallBatteryType, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(this.mCurCallBatteryType))).mSensorName, this.mCurCallBatteryTemp, "call_battery", this.mCurCallBatteryPolicy);
            if (thermalAction != null) {
                notifyPowerActionChanged(this.mICoreContext, thermalAction);
            }
        }
    }

    private void checkQuickCharger() {
        if (hasChargerType()) {
            synchronized (this.mLock) {
                if (this.mIDeviceState.isCharging()) {
                    if (isQuickCharger()) {
                        handleQuickChargeStateChange(false);
                    } else {
                        handleQuickChargeStateChange(true);
                    }
                }
            }
        }
    }

    private boolean isQuickCharger() {
        return getChargerType() == 4;
    }

    private boolean hasChargerType() {
        return getChargerType() != -1;
    }

    private int getChargerType() {
        Throwable th;
        FileInputStream fileInputStream = null;
        byte[] bytes = new byte[10];
        int type = -1;
        String chargerTypePath = "/sys/class/hw_power/charger/charge_data/chargerType";
        try {
            Arrays.fill(bytes, (byte) 0);
            FileInputStream fis = new FileInputStream(chargerTypePath);
            try {
                int len = fis.read(bytes);
                if (len > 0) {
                    String strType = new String(bytes, 0, len, "UTF-8");
                    try {
                        type = Integer.parseInt(strType.trim());
                        String str = strType;
                    } catch (Exception e) {
                        fileInputStream = fis;
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (Exception e2) {
                                Log.w("ThermalStateManager", "close failed: " + chargerTypePath);
                            }
                        }
                        Log.i("ThermalStateManager", "charger type: " + type);
                        return type;
                    } catch (Throwable th2) {
                        th = th2;
                        fileInputStream = fis;
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (Exception e3) {
                                Log.w("ThermalStateManager", "close failed: " + chargerTypePath);
                            }
                        }
                        throw th;
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (Exception e4) {
                        Log.w("ThermalStateManager", "close failed: " + chargerTypePath);
                    }
                }
                fileInputStream = fis;
            } catch (Exception e5) {
                fileInputStream = fis;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                Log.i("ThermalStateManager", "charger type: " + type);
                return type;
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = fis;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
        } catch (Exception e6) {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            Log.i("ThermalStateManager", "charger type: " + type);
            return type;
        } catch (Throwable th4) {
            th = th4;
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            throw th;
        }
        Log.i("ThermalStateManager", "charger type: " + type);
        return type;
    }

    private void handleThermalAction(Event evt, boolean fromEvent) {
        synchronized (this.mLock) {
            HookEvent devt = (HookEvent) evt;
            int sensorType = -1;
            int temperature = -100000;
            this.mAlarmRaised = false;
            this.mAlarmCleared = false;
            try {
                if (devt.getValue1() != null) {
                    sensorType = Integer.parseInt(devt.getValue1());
                }
                if (devt.getValue2() != null) {
                    temperature = Integer.parseInt(devt.getValue2());
                }
            } catch (NumberFormatException e) {
                Log.e("ThermalStateManager", "thermal event : " + devt + ", exception:" + e);
            }
            if (sensorType == -1 || temperature == -100000) {
                Log.w("ThermalStateManager", "warning: invalid value");
                return;
            }
            if (sensorType == 9 && (temperature <= -1000 || temperature >= 1000)) {
                temperature /= 1000;
            }
            if (fromEvent && temperature > 40) {
                Log.d("ThermalStateManager", "Thermal type: " + sensorType + " cur_temperature:" + temperature);
            }
            if (fromEvent) {
                this.mCurTemp.put(Integer.valueOf(sensorType), Integer.valueOf(temperature));
            }
            if (sensorType == this.mWarningType) {
                if (temperature < this.mWarningTemp) {
                    this.mHandler.removeMessages(100);
                    if (this.mIsWarningBroadcast) {
                        this.mHandler.sendMessage(this.mHandler.obtainMessage(101));
                    }
                } else if (!(this.mHandler.hasMessages(100) || this.mIsWarningBroadcast)) {
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(100), 120000);
                }
            } else if (sensorType == 2) {
                if (temperature <= 0) {
                    if (!(this.mHandler.hasMessages(102) || this.mIsColdChargingBroadcast)) {
                        this.mHandler.sendMessage(this.mHandler.obtainMessage(102));
                    }
                } else if (temperature >= 2) {
                    this.mHandler.removeMessages(102);
                    if (this.mIsColdChargingBroadcast) {
                        this.mHandler.sendMessage(this.mHandler.obtainMessage(103));
                    }
                }
            } else if (this.mDisabledLowSensorType >= 0 && sensorType == this.mDisabledLowSensorType) {
                if (temperature <= this.mDisabledLowTemp) {
                    if (!SystemProperties.getBoolean("hw.flash.disabled.by.low_temp", false)) {
                        Log.i("ThermalStateManager", "set system prop hw.flash.disabled.by.low_temp to true to notify camera disable the flash func");
                        SystemProperties.set("hw.flash.disabled.by.low_temp", "true");
                    }
                } else if (SystemProperties.getBoolean("hw.flash.disabled.by.low_temp", false)) {
                    Log.i("ThermalStateManager", "set system prop hw.flash.disabled.by.low_temp to false to notify camera enable the flash func");
                    SystemProperties.set("hw.flash.disabled.by.low_temp", "false");
                }
            }
            multipleToTrigger(sensorType, temperature, this.mIDeviceState.getBatteryLevel(), false);
        }
    }

    private void handleScrnCtrlCharge(int sensorType, int temperature, Boolean[] lvl_alarm, int lvl_min) {
        String tempPolicy = this.mNotLimitChargeVal;
        int trigLevel = -1;
        int mSimTrigLevel = -1;
        SensorTempAction sensorTemp = (SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType));
        for (int i = sensorTemp.mNumThresholds - 1; i >= 0; i--) {
            if (temperature > 0) {
                if (lvl_alarm[i].booleanValue()) {
                    trigLevel = i;
                    break;
                }
            } else if (lvl_alarm[i].booleanValue()) {
                trigLevel = i;
            }
        }
        if (trigLevel >= 0 && sensorTemp.mTriggerAction[trigLevel].mActions != null) {
            mSimTrigLevel = trigLevel;
        } else if (lvl_min >= 0 && lvl_min <= sensorTemp.mNumThresholds && sensorTemp.mTriggerAction[lvl_min].mActions != null) {
            mSimTrigLevel = lvl_min;
        }
        if (mSimTrigLevel != -1) {
            String bcurrent = (String) sensorTemp.mTriggerAction[trigLevel].mActions.get("bcurrent");
            if (bcurrent != null) {
                tempPolicy = bcurrent;
            }
        }
        Log.w("ThermalStateManager", "trigLevel = " + trigLevel + ", mSimTrigLevel = " + mSimTrigLevel + ", tempPolicy = " + tempPolicy);
        ThermalAction simulateThermalAction = generateThermalAction(System.currentTimeMillis(), sensorType, sensorTemp.mSensorName, temperature, "bcurrent", tempPolicy);
        if (simulateThermalAction != null) {
            notifyPowerActionChanged(this.mICoreContext, simulateThermalAction);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void multipleToTrigger(int sensorType, int temperature, int batteryLevel, boolean isBatteryTrigger) {
        if (this.mSensorActions.containsKey(Integer.valueOf(sensorType))) {
            SensorTempAction sensorTemp = (SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType));
            int max_thr = sensorTemp.mNumThresholds;
            int lvl_max = -1;
            int lvl_min = 9;
            Object lvl_alarm = (Boolean[]) this.mLvlAlarm.get(Integer.valueOf(sensorType));
            int i = max_thr - 1;
            while (i >= 0) {
                if (thresholdTrigger(temperature, sensorType, i)) {
                    if (!lvl_alarm[i].booleanValue()) {
                        lvl_alarm[i] = Boolean.valueOf(true);
                        this.mAlarmRaised = true;
                    }
                    if (i > lvl_max) {
                        lvl_max = i;
                    }
                }
                if (thresholdClear(temperature, sensorType, i) && lvl_alarm[i].booleanValue()) {
                    lvl_alarm[i] = Boolean.valueOf(false);
                    this.mAlarmCleared = true;
                    if (i < lvl_min) {
                        lvl_min = i;
                    }
                }
                i--;
            }
            if (this.mNotLimitChargeVal != null) {
                synchronized (this.mLock) {
                    if (this.mScrnCtrlChargeCounter.get() > 0) {
                        this.mScrnStateCtrlChargeActive = true;
                        this.mCurrentScreenCtrlBcurrentVal = this.mScreenOnMaxBcurrentVal;
                    } else {
                        this.mScrnCtrlChargeCounter.set(0);
                        this.mScrnStateCtrlChargeActive = false;
                        this.mCurrentScreenCtrlBcurrentVal = this.mNotLimitChargeVal;
                    }
                }
                if (!(isBatteryTrigger || this.mAlarmRaised || this.mAlarmCleared)) {
                    handleScrnCtrlCharge(sensorType, temperature, lvl_alarm, lvl_min);
                }
            }
            if (isBatteryTrigger || this.mAlarmRaised || this.mAlarmCleared) {
                this.mIsSwitchBroadcast = false;
                this.mIsLowTempWaringBroadcast = false;
                long time = System.currentTimeMillis();
                int triggerLevel = -1;
                for (i = max_thr - 1; i >= 0; i--) {
                    if (temperature > 0) {
                        if (lvl_alarm[i].booleanValue()) {
                            this.mCatchActionlvl = i;
                            triggerLevel = i;
                            break;
                        }
                    } else if (lvl_alarm[i].booleanValue()) {
                        triggerLevel = i;
                    }
                }
                if (this.mIsConfigBatteryLevel && !isBatteryTrigger && this.mAlarmCleared && lvl_min < sensorTemp.mNumThresholds) {
                    TriggerAction clrAction = sensorTemp.mTriggerAction[lvl_min];
                    for (i = 0; i < clrAction.getActionItemsNum(); i++) {
                        if (clrAction.mActionState[i]) {
                            clrAction.mActionState[i] = false;
                        }
                    }
                }
                HashMap actions = null;
                if (triggerLevel >= 0) {
                    TriggerAction triggerAction = sensorTemp.mTriggerAction[triggerLevel];
                    if (triggerAction.mActions != null) {
                        actions = (HashMap) triggerAction.mActions.clone();
                    } else if (this.mIsConfigBatteryLevel) {
                        actions = getActionsByBattery(triggerAction, batteryLevel);
                    }
                }
                if (actions != null) {
                    Log.i("ThermalStateManager", "triggerLevel = " + triggerLevel + ", triggerAction :" + actions.toString());
                    for (Entry entry : actions.entrySet()) {
                        String action = (String) entry.getKey();
                        String policy = (String) entry.getValue();
                        if (this.mScreenOnMaxBcurrentVal != INVALID_STRING_CHAEGE_PARAM) {
                            if (this.mICoreContext.isScreenOff() || !"bcurrent".equals(action)) {
                                if (this.mICoreContext.isScreenOff() && "bcurrent".equals(action)) {
                                    synchronized (this.mLock) {
                                        this.mScrnCtrlChargeCounter.set(0);
                                    }
                                    this.mScrnStateCtrlChargeActive = false;
                                    Log.w("ThermalStateManager", "set mScrnCtrlChargeCounter to 0 for handle screen off event");
                                }
                            } else if (!(this.mNotLimitChargeVal == null || this.mScreenOnMaxBcurrentVal == null)) {
                                int policyVal = Integer.parseInt(policy);
                                int notLimitChargeVal = Integer.parseInt(this.mNotLimitChargeVal);
                                int screenOnMaxBcurrentVal = Integer.parseInt(this.mScreenOnMaxBcurrentVal);
                                Log.i("ThermalStateManager", "check policyVal = " + policyVal + ", notLimitChargeVal = " + notLimitChargeVal + ", screenOnMaxBcurrentVal = " + screenOnMaxBcurrentVal);
                                if (policyVal == notLimitChargeVal || policyVal > screenOnMaxBcurrentVal) {
                                    this.mCurrentScreenCtrlBcurrentVal = this.mScreenOnMaxBcurrentVal;
                                    this.mScrnStateCtrlChargeActive = true;
                                }
                            }
                        }
                        if (!this.mExecAction.containsEntry(Integer.valueOf(sensorType), action)) {
                            this.mExecAction.put(Integer.valueOf(sensorType), action);
                        }
                        if ("call_battery".equals(action)) {
                            this.mCurCallBatteryType = sensorType;
                            this.mCurCallBatteryTemp = temperature;
                            this.mCurCallBatteryPolicy = policy;
                        }
                        PowerAction thermalAction = generateThermalAction(time, sensorType, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, temperature, action, policy);
                        if (thermalAction != null) {
                            notifyPowerActionChanged(this.mICoreContext, thermalAction);
                        }
                    }
                }
                if (this.mAlarmCleared && temperature >= 0) {
                    if (!isMatchClearAll(lvl_min, sensorTemp)) {
                    }
                    clearAllAction(sensorType, temperature, time);
                }
                if (!isBatteryTrigger) {
                    this.mLvlAlarm.put(Integer.valueOf(sensorType), lvl_alarm);
                }
            }
        }
    }

    private boolean isSameState(Boolean[] array, int len, boolean state) {
        if (this.mIsConfigBatteryLevel) {
            for (int index = 0; index < len; index++) {
                if (array[index].booleanValue() != state) {
                    return false;
                }
            }
            Log.i("ThermalStateManager", "is same state, will to clr all.");
            return true;
        }
        Log.i("ThermalStateManager", "not config low temperature policy, not to handle.");
        return false;
    }

    private int getMinValue(HashMap<Integer, Integer> checkHashMap, int exceptionValue) {
        int retValue = Integer.MAX_VALUE;
        for (Entry entry : checkHashMap.entrySet()) {
            int val = ((Integer) entry.getValue()).intValue();
            if (val != exceptionValue && val < retValue) {
                retValue = val;
            }
        }
        if (retValue == Integer.MAX_VALUE) {
            return 0;
        }
        return retValue;
    }

    private HashMap<String, String> getActionsByBattery(TriggerAction triggerAction, int batteryLevel) {
        if (triggerAction == null) {
            return null;
        }
        int triggerLevel = -1;
        int actionsNum = triggerAction.getActionItemsNum();
        boolean[] lvl_alarm = triggerAction.mActionState;
        int i = 0;
        while (i < actionsNum) {
            if (batteryLevelTrigger(batteryLevel, triggerAction, i) && !lvl_alarm[i]) {
                lvl_alarm[i] = true;
                this.mBatteryTrigged = true;
            }
            if (batteryLevelClear(batteryLevel, triggerAction, i) && lvl_alarm[i]) {
                lvl_alarm[i] = false;
                this.mBatteryCleared = true;
            }
            i++;
        }
        if (!this.mBatteryTrigged && !this.mBatteryCleared) {
            return null;
        }
        triggerAction.mActionState = lvl_alarm;
        for (i = 0; i < actionsNum; i++) {
            if (lvl_alarm[i]) {
                triggerLevel = i;
                break;
            }
        }
        if (triggerLevel == -1) {
            return null;
        }
        Log.i("ThermalStateManager", "battery trigger: batteryLevel = " + batteryLevel + ", triggerLevel = " + triggerLevel + ", actions = " + triggerAction.mActionItems[triggerLevel].mActions.toString());
        return triggerAction.mActionItems[triggerLevel].mActions;
    }

    private boolean isMatchClearAll(int minLevel, SensorTempAction sensorTemp) {
        boolean ret = false;
        if (minLevel == 0) {
            ret = sensorTemp.mTriggerAction[minLevel].mLevelClear > 0;
        } else if (minLevel > 0) {
            if (minLevel > sensorTemp.mNumThresholds) {
                return false;
            }
            if (sensorTemp.mTriggerAction[minLevel].mLevelClear <= 0 || sensorTemp.mTriggerAction[minLevel - 1].mLevelClear > 0) {
                if (sensorTemp.mTriggerAction[minLevel].mLevelClear <= 0 && sensorTemp.mTriggerAction[minLevel + 1].mLevelClear > 0) {
                }
            }
            ret = true;
        }
        return ret;
    }

    private void clearAllAction(int sensorType, int temperature, long time) {
        for (String action : this.mExecAction.getAll(Integer.valueOf(sensorType))) {
            boolean isOkay = true;
            String policy = null;
            if ("cpu".equals(action)) {
                policy = "0";
            } else if ("cpu1".equals(action)) {
                policy = "0";
            } else if ("cpu2".equals(action)) {
                policy = "0";
            } else if ("cpu3".equals(action)) {
                policy = "0";
            } else if ("cpu_a15".equals(action)) {
                policy = "0";
            } else if ("gpu".equals(action)) {
                policy = "0";
            } else if ("lcd".equals(action)) {
                policy = "0";
            } else if ("battery".equals(action)) {
                policy = "0";
            } else if ("call_battery".equals(action)) {
                policy = "0";
            } else if ("paback".equals(action)) {
                policy = "0";
            } else if ("flash".equals(action)) {
                policy = "0";
            } else if ("flash_front".equals(action)) {
                policy = "0";
            } else if ("wlan".equals(action)) {
                policy = "0";
            } else if ("ucurrent".equals(action)) {
                policy = "0";
            } else if ("ucurrent_aux".equals(action)) {
                policy = "0";
            } else if ("ipa_power".equals(action)) {
                policy = "0";
            } else if ("ipa_temp".equals(action)) {
                policy = "0";
            } else if ("ipa_switch".equals(action)) {
                policy = "0";
            } else if ("fork_on_big".equals(action)) {
                policy = "1";
            } else if ("camera_fps".equals(action)) {
                policy = "0";
            } else if ("app_action".equals(action)) {
                policy = "0";
            } else if ("vr_warning_level".equals(action)) {
                policy = "0";
            } else if ("bcurrent".equals(action)) {
                if (this.mScreenOnMaxBcurrentVal == INVALID_STRING_CHAEGE_PARAM || !this.mScrnStateCtrlChargeActive || this.mICoreContext.isScreenOff()) {
                    this.mCurrentScreenCtrlBcurrentVal = this.mNotLimitChargeVal;
                } else {
                    Log.i("ThermalStateManager", "assign exec policy from: " + null + " to: " + this.mScreenOnMaxBcurrentVal);
                    this.mCurrentScreenCtrlBcurrentVal = this.mScreenOnMaxBcurrentVal;
                }
                policy = "0";
            } else if ("bcurrent_aux".equals(action)) {
                policy = "0";
            } else if ("threshold_up".equals(action)) {
                policy = "0";
            } else if ("threshold_down".equals(action)) {
                policy = "0";
            } else if ("direct_charger".equals(action)) {
                policy = "0";
            } else {
                isOkay = false;
            }
            if (isOkay) {
                ThermalAction thermalAction = generateThermalAction(time, sensorType, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, temperature, action, policy);
                if (thermalAction != null) {
                    notifyPowerActionChanged(this.mICoreContext, thermalAction);
                }
            }
        }
        this.mExecAction.removeAll(Integer.valueOf(sensorType));
    }

    private ThermalAction generateThermalAction(long time, int sensorType, String sensorName, int temperature, String action, String policy) {
        ThermalAction thermalAction = null;
        try {
            int reqFrequency;
            if ("cpu".equals(action)) {
                this.mCpuMaxFreq.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                reqFrequency = getThermalActionPolicy(this.mCurCpufreq, this.mCpuMaxFreq, false);
                if (reqFrequency != -1) {
                    this.mCurCpufreq = reqFrequency;
                    thermalAction = newThermalAction(251, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(reqFrequency));
                }
            } else if ("cpu1".equals(action)) {
                this.mCpu1MaxFreq.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                reqFrequency = getThermalActionPolicy(this.mCurCpu1freq, this.mCpu1MaxFreq, false);
                if (reqFrequency != -1) {
                    this.mCurCpu1freq = reqFrequency;
                    thermalAction = newThermalAction(251, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(reqFrequency));
                }
            } else if ("cpu2".equals(action)) {
                this.mCpu2MaxFreq.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                reqFrequency = getThermalActionPolicy(this.mCurCpu2freq, this.mCpu2MaxFreq, false);
                if (reqFrequency != -1) {
                    this.mCurCpu2freq = reqFrequency;
                    thermalAction = newThermalAction(251, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(reqFrequency));
                }
            } else if ("cpu3".equals(action)) {
                this.mCpu3MaxFreq.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                reqFrequency = getThermalActionPolicy(this.mCurCpu3freq, this.mCpu3MaxFreq, false);
                if (reqFrequency != -1) {
                    this.mCurCpu3freq = reqFrequency;
                    thermalAction = newThermalAction(251, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(reqFrequency));
                }
            } else if ("cpu_a15".equals(action)) {
                this.mA15CpuMaxFreq.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                reqFrequency = getThermalActionPolicy(this.mCurA15Cpufreq, this.mA15CpuMaxFreq, false);
                if (reqFrequency != -1) {
                    this.mCurA15Cpufreq = reqFrequency;
                    thermalAction = newThermalAction(251, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(reqFrequency));
                }
            } else if ("gpu".equals(action)) {
                this.mGpuMaxFreq.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                reqFrequency = getThermalActionPolicy(this.mCurGpufreq, this.mGpuMaxFreq, false);
                if (reqFrequency != -1) {
                    this.mCurGpufreq = reqFrequency;
                    thermalAction = newThermalAction(251, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(reqFrequency));
                }
            } else if ("ipa_power".equals(action)) {
                this.mIpaPower.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                int reqIpaPower = getThermalActionPolicy(this.mCurIpaPower, this.mIpaPower, false);
                if (reqIpaPower != -1) {
                    this.mCurIpaPower = reqIpaPower;
                    thermalAction = newThermalAction(251, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(reqIpaPower));
                }
            } else if ("ipa_temp".equals(action)) {
                this.mIpaTemp.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                int reqIpaTemp = getThermalActionPolicy(this.mCurIpaTemp, this.mIpaTemp, false);
                if (reqIpaTemp != -1) {
                    this.mCurIpaTemp = reqIpaTemp;
                    thermalAction = newThermalAction(251, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(reqIpaTemp));
                }
            } else if ("ipa_switch".equals(action)) {
                this.mIpaSwitch.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                int reqIpaSwitch = getThermalActionPolicy(this.mCurIpaSwitch, this.mIpaSwitch, false);
                if (reqIpaSwitch != -1) {
                    this.mCurIpaSwitch = reqIpaSwitch;
                    thermalAction = newThermalAction(251, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(reqIpaSwitch));
                }
            } else if ("fork_on_big".equals(action)) {
                this.mForkOnBig.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                int reqForkOnBig = 1;
                for (Entry entry : this.mForkOnBig.entrySet()) {
                    lvl = ((Integer) entry.getValue()).intValue();
                    if (lvl == 0) {
                        reqForkOnBig = lvl;
                        break;
                    }
                }
                if (reqForkOnBig != this.mCurForkOnBig) {
                    this.mCurForkOnBig = reqForkOnBig;
                    thermalAction = newThermalAction(251, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(reqForkOnBig));
                }
            } else if ("app".equals(action)) {
                if (!this.mIsSwitchBroadcast) {
                    BroadcastAdapter.sendThermalComUIEvent(this.mContext, "close_powerswitch_app", null);
                    this.mIsSwitchBroadcast = true;
                }
                thermalAction = newThermalAction(252, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(policy));
            } else if ("lcd".equals(action)) {
                this.mLcdLimit.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                int lcdLimit = getThermalActionPolicy(this.mCurLcdLimt, this.mLcdLimit, true);
                if (lcdLimit != -1) {
                    this.mCurLcdLimt = lcdLimit;
                    thermalAction = newThermalAction(254, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(lcdLimit));
                }
            } else if ("wlan".equals(action)) {
                int wlanLvl = Integer.parseInt(policy);
                if (isVaildWlanLimitLvl(wlanLvl)) {
                    this.mWlanLimit.put(Integer.valueOf(sensorType), Integer.valueOf(wlanLvl));
                    lvl = getThermalActionPolicy(this.mCurWlanLvl, this.mWlanLimit, true);
                    if (lvl != -1) {
                        this.mCurWlanLvl = lvl;
                        thermalAction = newThermalAction(253, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(lvl));
                    }
                }
            } else if ("flash".equals(action)) {
                this.mFlashLimit.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                flashLimit = getThermalActionPolicy(this.mCurFlashLimt, this.mFlashLimit, true);
                if (flashLimit != -1) {
                    if (!(this.mIsSwitchBroadcast || flashLimit == 0)) {
                        BroadcastAdapter.sendThermalComUIEvent(this.mContext, "close_powerswitch_app", null);
                        this.mIsSwitchBroadcast = true;
                    }
                    this.mCurFlashLimt = flashLimit;
                    thermalAction = newThermalAction(253, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(flashLimit));
                }
            } else if ("flash_front".equals(action)) {
                this.mFrontFlashLimit.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                flashLimit = getThermalActionPolicy(this.mCurFrontFlashLimt, this.mFrontFlashLimit, true);
                if (flashLimit != -1) {
                    if (!(this.mIsSwitchBroadcast || flashLimit == 0)) {
                        BroadcastAdapter.sendThermalComUIEvent(this.mContext, "close_powerswitch_app", null);
                        this.mIsSwitchBroadcast = true;
                    }
                    this.mCurFrontFlashLimt = flashLimit;
                    thermalAction = newThermalAction(253, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(flashLimit));
                }
            } else if ("camera_fps".equals(action)) {
                this.mCameraFps.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                int cameraFpsLimit = getThermalActionPolicy(this.mCurCameraFps, this.mCameraFps, true);
                if (cameraFpsLimit != -1) {
                    this.mCurCameraFps = cameraFpsLimit;
                    thermalAction = newThermalAction(253, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(cameraFpsLimit));
                }
            } else if ("app_action".equals(action)) {
                this.mAppAction.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                int appActionLimit = getThermalActionPolicy(this.mCurAppAction, this.mAppAction, true);
                if (appActionLimit != -1) {
                    this.mISdkService.handleStateChanged(9, sensorType, temperature, String.valueOf(appActionLimit), 0);
                    this.mCurAppAction = appActionLimit;
                    thermalAction = newThermalAction(253, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(appActionLimit));
                }
            } else if ("vr_warning_level".equals(action)) {
                this.mVRWarningLevel.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                int warningLevel = getThermalActionPolicy(this.mCurVRWarningLevel, this.mVRWarningLevel, true);
                if (warningLevel != -1) {
                    this.mCurVRWarningLevel = warningLevel;
                    thermalAction = newThermalAction(253, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(warningLevel));
                }
            } else if ("shutdown".equals(action)) {
                BroadcastAdapter.sendThermalComUIEvent(this.mContext, "shutdown", null);
                thermalAction = newThermalAction(253, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(policy));
            } else if ("paback".equals(action)) {
                this.mPAFallbackLimit.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                int fallback = getThermalActionPolicy(this.mCurPAFallbackLimt, this.mPAFallbackLimit, true);
                if (fallback != -1) {
                    this.mCurPAFallbackLimt = fallback;
                    thermalAction = newThermalAction(253, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(fallback));
                }
            } else if ("modem".equals(action) || "wifiap".equals(action) || "wifioff".equals(action) || "camera".equals(action) || "gps".equals(action) || "camera_warning".equals(action) || "camera_stop".equals(action)) {
                if (!this.mIsSwitchBroadcast) {
                    BroadcastAdapter.sendThermalComUIEvent(this.mContext, "close_powerswitch_app", null);
                    this.mIsSwitchBroadcast = true;
                }
                thermalAction = newThermalAction(253, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(policy));
            } else if ("call_battery".equals(action) || "battery".equals(action)) {
                if (!this.mIDeviceState.isCalling() && this.mCallBatteryLimit.size() > 0) {
                    this.mCallBatteryLimit.clear();
                }
                int reqLimit = Integer.parseInt(policy);
                if ("call_battery".equals(action) && this.mIDeviceState.isCalling()) {
                    this.mCallBatteryLimit.put(Integer.valueOf(sensorType), Integer.valueOf(reqLimit));
                } else if ("battery".equals(action)) {
                    this.mBatteryLimit.put(Integer.valueOf(sensorType), Integer.valueOf(reqLimit));
                }
                if (this.mMutilSensorBatteryClr && "battery".equals(action) && this.mAlarmCleared) {
                    HashMap<Integer, Integer> clearBattery = ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mTriggerAction[this.mCatchActionlvl + 1].mLevelClearBattery;
                    if (clearBattery.size() > 0 && !multiThresholdClear(clearBattery)) {
                        Log.i("ThermalStateManager", "clear battery, but other sensor can't satisfy the conditions at the same time, do nothing");
                        return null;
                    }
                }
                lvl = getBatteryPolicy(this.mBatteryLimit, this.mCallBatteryLimit);
                if (this.mCurChargingLvl != lvl) {
                    this.mCurChargingLvl = lvl;
                    thermalAction = newThermalAction(253, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(lvl));
                }
            } else if ("direct_charger".equals(action)) {
                this.mDirectChargeLimit.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                int directChargeLimit = getThermalActionPolicy(this.mCurDirectChargeLimt, this.mDirectChargeLimit, false);
                if (directChargeLimit != -1) {
                    this.mCurDirectChargeLimt = directChargeLimit;
                    thermalAction = newThermalAction(253, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(directChargeLimit));
                }
            } else if ("ucurrent".equals(action)) {
                this.mUsbChargeLimit.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                int usbChargeLimit = getThermalActionPolicy(this.mCurUsbChargeLimt, this.mUsbChargeLimit, false);
                if (usbChargeLimit != -1) {
                    this.mCurUsbChargeLimt = usbChargeLimit;
                    thermalAction = newThermalAction(253, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(usbChargeLimit));
                }
            } else if ("ucurrent_aux".equals(action)) {
                this.mUsbChargeAuxLimit.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                int usbChargeAuxLimit = getThermalActionPolicy(this.mCurUsbChargeAuxLimt, this.mUsbChargeAuxLimit, false);
                if (usbChargeAuxLimit != -1) {
                    this.mCurUsbChargeAuxLimt = usbChargeAuxLimit;
                    thermalAction = newThermalAction(253, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(usbChargeAuxLimit));
                }
            } else if ("bcurrent".equals(action)) {
                this.mAcChargeLimit.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                int acChargeLimit = getThermalActionPolicy(this.mCurAcChargeLimt, this.mAcChargeLimit, false);
                if (!(this.mNotLimitChargeVal == null || this.mICoreContext.isScreenOff())) {
                    if (this.mCurrentScreenCtrlBcurrentVal == null) {
                        this.mCurrentScreenCtrlBcurrentVal = this.mScreenOnMaxBcurrentVal;
                    }
                    int mCrntScrnCtrlBCurntVal = Integer.parseInt(this.mCurrentScreenCtrlBcurrentVal);
                    acChargeLimit = getMinValue(this.mAcChargeLimit, Integer.parseInt(this.mNotLimitChargeVal));
                    if (!(acChargeLimit == -1 || acChargeLimit == Integer.parseInt(this.mNotLimitChargeVal))) {
                        if (acChargeLimit > mCrntScrnCtrlBCurntVal) {
                        }
                    }
                    acChargeLimit = mCrntScrnCtrlBCurntVal;
                }
                if (acChargeLimit != -1) {
                    this.mCurAcChargeLimt = acChargeLimit;
                    thermalAction = newThermalAction(253, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(acChargeLimit));
                }
            } else if ("bcurrent_aux".equals(action)) {
                this.mAcChargeAuxLimit.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                int acChargeAuxLimit = getThermalActionPolicy(this.mCurAcChargeAuxLimt, this.mAcChargeAuxLimit, false);
                if (acChargeAuxLimit != -1) {
                    this.mCurAcChargeAuxLimt = acChargeAuxLimit;
                    thermalAction = newThermalAction(253, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(acChargeAuxLimit));
                }
            } else if ("threshold_up".equals(action)) {
                this.mHmpThresholdUp.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                int upPolicyValue = getThermalActionPolicy(this.mCurThresholdUpPolicy, this.mHmpThresholdUp, false);
                if (upPolicyValue != -1) {
                    this.mCurThresholdUpPolicy = upPolicyValue;
                    thermalAction = newThermalAction(251, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(upPolicyValue));
                }
            } else if ("threshold_down".equals(action)) {
                this.mHmpThresholdDown.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                int downPolicyValue = getThermalActionPolicy(this.mCurThresholdDownPolicy, this.mHmpThresholdDown, false);
                if (downPolicyValue != -1) {
                    this.mCurThresholdDownPolicy = downPolicyValue;
                    thermalAction = newThermalAction(251, time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(downPolicyValue));
                }
            } else if ("pop_up_dialog".equals(action)) {
                this.mPopUpDialogState.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                int popState = getThermalActionPolicy(this.mCurpopState, this.mPopUpDialogState, true);
                if (popState != -1) {
                    if (!(this.mIsLowTempWaringBroadcast || popState == 0)) {
                        BroadcastAdapter.sendLowTempWarningEvent(this.mContext, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, temperature, this.mIDeviceState.getBatteryLevel());
                        Log.i("ThermalStateManager", "pop up dialog!");
                        this.mIsLowTempWaringBroadcast = true;
                    }
                    this.mCurpopState = popState;
                }
            }
        } catch (Throwable e) {
            Log.e("ThermalStateManager", "generate thermal action exception", e);
        } catch (Throwable e2) {
            Log.e("ThermalStateManager", "generate thermal action index out of bounds exception", e2);
        } catch (Throwable e3) {
            Log.e("ThermalStateManager", "generate thermal action null pointer exception", e3);
        }
        if (thermalAction != null) {
            Log.i("ThermalStateManager", "sensor name: " + thermalAction.getSensorName() + " temperature: " + thermalAction.getTemperature() + " , thermal action: " + thermalAction.getAction() + " policy:" + thermalAction.getValue());
        }
        return thermalAction;
    }

    private ThermalAction newThermalAction(int actionId, long timestamp, String name, int type, int temperature, String action, String value) {
        ThermalAction thermalAction = ThermalAction.obtain();
        thermalAction.resetAs(actionId, timestamp, name, type, temperature, action, value);
        thermalAction.putExtra(temperature);
        thermalAction.putExtra(Long.parseLong(value));
        return thermalAction;
    }

    private int getThermalActionPolicy(int curLvl, HashMap<Integer, Integer> map, boolean isMax) {
        int limit_lvl;
        boolean clearAll = true;
        if (isMax) {
            limit_lvl = -1;
        } else {
            limit_lvl = Integer.MAX_VALUE;
        }
        for (Entry entry : map.entrySet()) {
            int lvl = ((Integer) entry.getValue()).intValue();
            if (lvl > 0) {
                clearAll = false;
                if (isMax) {
                    if (lvl > limit_lvl) {
                        limit_lvl = lvl;
                    }
                } else if (lvl < limit_lvl) {
                    limit_lvl = lvl;
                }
            }
        }
        if (clearAll) {
            limit_lvl = 0;
        }
        if (limit_lvl == curLvl) {
            return -1;
        }
        curLvl = limit_lvl;
        return limit_lvl;
    }

    private int getBatteryPolicy(HashMap<Integer, Integer> batteryMap, HashMap<Integer, Integer> callBatteryMap) {
        boolean clearAll = true;
        int battery_lvl = Integer.MAX_VALUE;
        int call_battery_lvl = Integer.MAX_VALUE;
        for (Entry entry : batteryMap.entrySet()) {
            int lvl = ((Integer) entry.getValue()).intValue();
            if (lvl > 0) {
                clearAll = false;
                if (lvl < battery_lvl) {
                    battery_lvl = lvl;
                }
            }
        }
        for (Entry entry2 : callBatteryMap.entrySet()) {
            lvl = ((Integer) entry2.getValue()).intValue();
            if (lvl > 0) {
                clearAll = false;
                if (lvl < call_battery_lvl) {
                    call_battery_lvl = lvl;
                }
            }
        }
        if (clearAll) {
            return 0;
        }
        return Math.min(battery_lvl, call_battery_lvl);
    }

    private boolean thresholdTrigger(int temp, int sensorType, int level) {
        SensorTempAction mSensorTempAction = (SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType));
        if (temp > 0 && mSensorTempAction.mTriggerAction[level].mLevelTrigger > 0) {
            return temp >= mSensorTempAction.mTriggerAction[level].mLevelTrigger;
        } else {
            if (temp >= 0 || mSensorTempAction.mTriggerAction[level].mLevelTrigger >= 0) {
                return false;
            }
            return temp <= mSensorTempAction.mTriggerAction[level].mLevelTrigger;
        }
    }

    private boolean thresholdClear(int temp, int sensorType, int level) {
        SensorTempAction mSensorTempAction = (SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType));
        if (temp > 0) {
            if (mSensorTempAction.mTriggerAction[level].mLevelClear <= 0) {
                return true;
            }
            if (temp <= mSensorTempAction.mTriggerAction[level].mLevelClear) {
                return true;
            }
            return false;
        } else if (mSensorTempAction.mTriggerAction[level].mLevelClear <= 0) {
            return temp >= mSensorTempAction.mTriggerAction[level].mLevelClear;
        } else {
            return true;
        }
    }

    private boolean batteryLevelTrigger(int batteryLevel, TriggerAction triggerAction, int level) {
        if (batteryLevel <= triggerAction.mActionItems[level].mBatLevelTrigger) {
            return true;
        }
        return false;
    }

    private boolean batteryLevelClear(int batteryLevel, TriggerAction triggerAction, int level) {
        if (batteryLevel >= triggerAction.mActionItems[level].mBatLevelClear) {
            return true;
        }
        return false;
    }

    private boolean multiThresholdClear(HashMap<Integer, Integer> clear) {
        for (Entry entry : clear.entrySet()) {
            int cur_temp;
            int trigger_sensor = ((Integer) entry.getKey()).intValue();
            int trigger_temp = ((Integer) entry.getValue()).intValue();
            if (this.mCurTemp.get(Integer.valueOf(trigger_sensor)) != null) {
                cur_temp = ((Integer) this.mCurTemp.get(Integer.valueOf(trigger_sensor))).intValue();
                continue;
            } else {
                cur_temp = -1;
                continue;
            }
            if (cur_temp > trigger_temp) {
                return false;
            }
        }
        return true;
    }

    public String getThermalInterface(String action) {
        return (String) mActionInterface.get(action);
    }

    public int getThermalTemp(int sensorType) {
        if (this.mCurTemp.containsKey(Integer.valueOf(sensorType))) {
            return ((Integer) this.mCurTemp.get(Integer.valueOf(sensorType))).intValue();
        }
        return -100000;
    }

    public int getCurThermalStep() {
        return this.mCurAppAction;
    }

    private String getThermalProductFile(boolean usePfmcThermalConf) {
        String configDir = this.mIsVRMode ? THERMAL_VR_CONFIG_PRODUCT : usePfmcThermalConf ? THERMAL_PFMC_CONFIG_PRODUCT : THERMALCONFIG_PRODUCT;
        Log.i("ThermalStateManager", "load thermald config : " + configDir);
        return configDir;
    }

    private String getThermalDefaultFile(boolean usePfmcThermalConf) {
        String configDir = this.mIsVRMode ? "thermald_vr.xml" : this.mIsQuickChargeOff ? usePfmcThermalConf ? "thermald_performance_qcoff.xml" : "thermald_qcoff.xml" : usePfmcThermalConf ? "thermald_performance.xml" : "thermald.xml";
        Log.i("ThermalStateManager", "load thermald config : " + configDir);
        return configDir;
    }

    private InputStream getThermalStream(String dir, boolean usePfmcThermalConf) {
        InputStream inStream;
        try {
            inStream = new FileInputStream(dir + getThermalProductFile(usePfmcThermalConf));
        } catch (FileNotFoundException e) {
            try {
                inStream = new FileInputStream(dir + getThermalDefaultFile(usePfmcThermalConf));
            } catch (FileNotFoundException e2) {
                Log.w("ThermalStateManager", "thermald config not found:" + dir);
                return null;
            }
        }
        return inStream;
    }

    private InputStream getThermalStreamCrypt(String dir, boolean usePfmcThermalConf) {
        InputStream inStream;
        InputStream inStreamDecode;
        InputStream inputStream = null;
        try {
            inStream = new FileInputStream(dir + getThermalProductFile(usePfmcThermalConf));
            try {
                inStreamDecode = DecodeXmlFile.getDecodeInputStream(inStream);
            } catch (Exception e) {
                inputStream = inStream;
                try {
                    inStream = new FileInputStream(dir + getThermalDefaultFile(usePfmcThermalConf));
                    try {
                        inStreamDecode = DecodeXmlFile.getDecodeInputStream(inStream);
                        return inStreamDecode;
                    } catch (Exception e2) {
                        inputStream = inStream;
                        Log.w("ThermalStateManager", "crypt thermald config not found:" + dir);
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        return null;
                    }
                } catch (Exception e3) {
                    Log.w("ThermalStateManager", "crypt thermald config not found:" + dir);
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    return null;
                }
            }
        } catch (Exception e4) {
            inStream = new FileInputStream(dir + getThermalDefaultFile(usePfmcThermalConf));
            inStreamDecode = DecodeXmlFile.getDecodeInputStream(inStream);
            return inStreamDecode;
        }
        return inStreamDecode;
    }

    private boolean isVaildWlanLimitLvl(int lvl) {
        if (lvl < 0 || lvl > 4) {
            return false;
        }
        return true;
    }

    private int getPGSceneId(int thermalScenceId) {
        return ActionsExportMap.getPGActionID(thermalScenceId);
    }

    private boolean loadThermalConf(boolean usePfmcThermalConf) {
        InputStream inStream = null;
        XmlPullParser xmlpp = Xml.newPullParser();
        try {
            inStream = getThermalStreamCrypt("/product/etc/hwpg/", usePfmcThermalConf);
            if (inStream == null) {
                inStream = getThermalStream("/product/etc/hwpg/", usePfmcThermalConf);
                if (inStream == null) {
                    inStream = getThermalStreamCrypt("/system/etc/", usePfmcThermalConf);
                    if (inStream == null) {
                        inStream = getThermalStream("/system/etc/", usePfmcThermalConf);
                    }
                }
            }
            if (inStream == null) {
                Log.e("ThermalStateManager", "error: not find the thermald.xml");
                if (inStream != null) {
                    try {
                        inStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
            xmlpp.setInput(inStream, "UTF-8");
            boolean sceneThermalEnable = false;
            String sceneName = "default";
            int sceneId = 208;
            int sensorType = 0;
            boolean sensorEnable = false;
            boolean isConfigbatteryLevel = false;
            TriggerAction triggerAction = null;
            ActionItem actionItem = null;
            SensorTempAction sensorTempAction = null;
            int batLevelTrigger = -1;
            int batLevelClear = -1;
            int count = 0;
            HashMap<Integer, SensorTempAction> sensorActions = new HashMap();
            mActionInterface.clear();
            this.mSceneSensorActions.clear();
            for (int eventType = xmlpp.getEventType(); eventType != 1; eventType = xmlpp.next()) {
                String nodeName = xmlpp.getName();
                switch (eventType) {
                    case NativeAdapter.PLATFORM_HI /*2*/:
                        int i;
                        int thermalSceneID;
                        if ("scene".equals(nodeName)) {
                            sceneThermalEnable = true;
                            sceneName = xmlpp.getAttributeValue(0);
                            thermalSceneID = Integer.parseInt(xmlpp.getAttributeValue(1));
                            if (thermalSceneID >= 100000) {
                                sceneId = thermalSceneID;
                                this.mCustScenes.put(Integer.valueOf(thermalSceneID), sceneName);
                            } else {
                                sceneId = getPGSceneId(thermalSceneID);
                                if (sceneId == -1) {
                                    Log.w("ThermalStateManager", "not support scene:" + thermalSceneID + " chg to default scene");
                                    sceneId = 208;
                                }
                            }
                        } else if ("comb_scene".equals(nodeName)) {
                            sceneThermalEnable = true;
                            sceneName = xmlpp.getAttributeValue(0);
                            thermalSceneID = Integer.parseInt(xmlpp.getAttributeValue(1));
                            int thermalParentSceneID = Integer.parseInt(xmlpp.getAttributeValue(2));
                            int subSceneId = getPGSceneId(thermalSceneID);
                            if (subSceneId == -1) {
                                Log.e("ThermalStateManager", "not support sub scene:" + thermalSceneID);
                                sensorActions.clear();
                                this.mSceneSensorActions.clear();
                                if (inStream != null) {
                                    try {
                                        inStream.close();
                                    } catch (Exception e2) {
                                        e2.printStackTrace();
                                    }
                                }
                                return false;
                            } else if (thermalParentSceneID >= 0) {
                                String key;
                                if (thermalParentSceneID == 0) {
                                    key = Integer.toString(subSceneId) + "|" + sceneName;
                                } else {
                                    int parentSceneID = getPGSceneId(thermalParentSceneID);
                                    if (parentSceneID == -1) {
                                        Log.e("ThermalStateManager", "not support parent scene:" + thermalSceneID);
                                        sensorActions.clear();
                                        this.mSceneSensorActions.clear();
                                        if (inStream != null) {
                                            try {
                                                inStream.close();
                                            } catch (Exception e22) {
                                                e22.printStackTrace();
                                            }
                                        }
                                        return false;
                                    }
                                    key = Integer.toString(subSceneId) + "|" + Integer.toString(parentSceneID);
                                }
                                if (mCustCombActionId.containsKey(key)) {
                                    sceneId = ((Integer) mCustCombActionId.get(key)).intValue();
                                } else {
                                    count++;
                                    sceneId = 70000 + count;
                                    mCustCombActionId.put(key, Integer.valueOf(sceneId));
                                    Log.i("ThermalStateManager", "comb scene, key = (" + key + ") sceneId = (" + sceneId + ")");
                                }
                            }
                        } else if ("sensor_temp".equals(nodeName)) {
                            String sensorName = xmlpp.getAttributeValue(0);
                            sensorType = Integer.parseInt(xmlpp.getAttributeValue(1));
                            sensorEnable = "true".equals(xmlpp.getAttributeValue(2));
                            if (sensorEnable) {
                                Object lvlalarm = new Boolean[8];
                                for (i = 0; i < 8; i++) {
                                    lvlalarm[i] = Boolean.valueOf(false);
                                }
                                this.mLvlAlarm.put(Integer.valueOf(sensorType), lvlalarm);
                                SensorTempAction sensorTempAction2 = new SensorTempAction(sensorType, sensorName);
                            }
                        } else if ("action_filenode".equals(nodeName)) {
                            mActionInterface.put(xmlpp.getAttributeValue(0), xmlpp.nextText());
                        } else if ("warning_temperature".equals(nodeName)) {
                            this.mWarningType = Integer.parseInt(xmlpp.getAttributeValue(0));
                            this.mWarningTemp = Integer.parseInt(xmlpp.nextText());
                        } else if ("flash_disable_by_low_temp".equals(nodeName)) {
                            this.mDisabledLowSensorType = Integer.parseInt(xmlpp.getAttributeValue(0));
                            this.mDisabledLowTemp = Integer.parseInt(xmlpp.nextText());
                        } else if ("screen_on_charge_control".equals(nodeName)) {
                            this.mNotLimitChargeVal = xmlpp.getAttributeValue(0);
                            this.mScreenOnMaxBcurrentVal = xmlpp.getAttributeValue(1);
                            this.mPostPoneTimeInMSec = Integer.parseInt(xmlpp.getAttributeValue(2));
                            this.mScrnStateCtrlChargeEnable = true;
                            Log.i("ThermalStateManager", "mNotLimitChargeVal = " + this.mNotLimitChargeVal + ", mPostPoneTimeInMSec = " + this.mPostPoneTimeInMSec + ", mScreenOnMaxBcurrentVal = " + this.mScreenOnMaxBcurrentVal);
                        }
                        if (sensorEnable) {
                            if (!"item".equals(nodeName)) {
                                if (!"thresholds".equals(nodeName)) {
                                    if (!"thresholds_clr".equals(nodeName)) {
                                        if (!"thresholds_clr_battery".equals(nodeName)) {
                                            if (!"sensor".equals(nodeName)) {
                                                if (!"action".equals(nodeName)) {
                                                    if (!"cpu".equals(nodeName) && !"cpu1".equals(nodeName) && !"cpu2".equals(nodeName) && !"cpu3".equals(nodeName) && !"cpu_a15".equals(nodeName) && !"gpu".equals(nodeName) && !"shutdown".equals(nodeName) && !"lcd".equals(nodeName) && !"battery".equals(nodeName) && !"call_battery".equals(nodeName) && !"wlan".equals(nodeName) && !"paback".equals(nodeName) && !"app".equals(nodeName) && !"wifiap".equals(nodeName) && !"wifioff".equals(nodeName) && !"flash".equals(nodeName) && !"flash_front".equals(nodeName) && !"camera".equals(nodeName) && !"gps".equals(nodeName) && !"modem".equals(nodeName) && !"ucurrent".equals(nodeName) && !"ucurrent_aux".equals(nodeName) && !"bcurrent".equals(nodeName) && !"bcurrent_aux".equals(nodeName) && !"direct_charger".equals(nodeName) && !"threshold_up".equals(nodeName) && !"threshold_down".equals(nodeName) && !"pop_up_dialog".equals(nodeName) && !"camera_warning".equals(nodeName) && !"camera_stop".equals(nodeName) && !"ipa_power".equals(nodeName) && !"ipa_temp".equals(nodeName) && !"ipa_switch".equals(nodeName) && !"fork_on_big".equals(nodeName) && !"vr_warning_level".equals(nodeName) && !"camera_fps".equals(nodeName) && !"app_action".equals(nodeName)) {
                                                        break;
                                                    }
                                                    String value = xmlpp.nextText();
                                                    if (isConfigbatteryLevel) {
                                                        if (!(actionItem == null || actionItem.addAction(nodeName, value))) {
                                                            Log.e("ThermalStateManager", "error: the thermal config format for action " + nodeName);
                                                            sensorActions.clear();
                                                            this.mSceneSensorActions.clear();
                                                            if (inStream != null) {
                                                                try {
                                                                    inStream.close();
                                                                } catch (Exception e222) {
                                                                    e222.printStackTrace();
                                                                }
                                                            }
                                                            return false;
                                                        }
                                                    } else if (triggerAction != null) {
                                                        if (triggerAction.addAction(nodeName, value)) {
                                                            break;
                                                        }
                                                        Log.e("ThermalStateManager", "error: the thermal config format for action " + nodeName);
                                                        sensorActions.clear();
                                                        this.mSceneSensorActions.clear();
                                                        if (inStream != null) {
                                                            try {
                                                                inStream.close();
                                                            } catch (Exception e2222) {
                                                                e2222.printStackTrace();
                                                            }
                                                        }
                                                        return false;
                                                    } else {
                                                        continue;
                                                    }
                                                } else {
                                                    for (i = 0; i < xmlpp.getAttributeCount(); i++) {
                                                        if ("battery_level_tri".equals(xmlpp.getAttributeName(i))) {
                                                            batLevelTrigger = Integer.parseInt(xmlpp.getAttributeValue(i));
                                                        } else if ("battery_level_clr".equals(xmlpp.getAttributeName(i))) {
                                                            batLevelClear = Integer.parseInt(xmlpp.getAttributeValue(i));
                                                        }
                                                    }
                                                    if (batLevelTrigger >= 0 && batLevelClear >= 0) {
                                                        this.mIsConfigBatteryLevel = true;
                                                        isConfigbatteryLevel = true;
                                                        actionItem = new ActionItem();
                                                        actionItem.setBatTrigger(batLevelTrigger);
                                                        actionItem.setBatClear(batLevelClear);
                                                        break;
                                                    }
                                                }
                                            } else if (!this.mMutilSensorBatteryClr) {
                                                break;
                                            } else {
                                                triggerAction.addBatteryClear(Integer.parseInt(xmlpp.getAttributeValue(0)), Integer.parseInt(xmlpp.nextText()));
                                                break;
                                            }
                                        }
                                        this.mMutilSensorBatteryClr = true;
                                        break;
                                    }
                                    triggerAction.setClear(Integer.parseInt(xmlpp.nextText()));
                                    break;
                                }
                                triggerAction.setTrigger(Integer.parseInt(xmlpp.nextText()));
                                break;
                            }
                            triggerAction = new TriggerAction();
                            break;
                        }
                        continue;
                        break;
                    case NativeAdapter.PLATFORM_K3V3 /*3*/:
                        if (sensorEnable) {
                            if ("action".equals(nodeName)) {
                                if (isConfigbatteryLevel) {
                                    triggerAction.addActionItem(actionItem);
                                }
                                isConfigbatteryLevel = false;
                                batLevelTrigger = -1;
                                batLevelClear = -1;
                                break;
                            }
                        }
                        if (sensorEnable && "item".equals(nodeName)) {
                            if (sensorTempAction.addTriggerAction(triggerAction)) {
                                break;
                            }
                            Log.e("ThermalStateManager", "error: the thermal config format");
                            sensorActions.clear();
                            this.mSceneSensorActions.clear();
                            if (inStream != null) {
                                try {
                                    inStream.close();
                                } catch (Exception e22222) {
                                    e22222.printStackTrace();
                                }
                            }
                            return false;
                        }
                        if (sensorEnable) {
                            if ("sensor_temp".equals(nodeName)) {
                                sensorActions.put(Integer.valueOf(sensorType), sensorTempAction);
                                sensorType = 0;
                                sensorEnable = false;
                                break;
                            }
                        }
                        if (!"scene".equals(nodeName) && !"comb_scene".equals(nodeName)) {
                            break;
                        }
                        sceneThermalEnable = true;
                        HashMap<Integer, SensorTempAction> temp = new HashMap();
                        temp.putAll(sensorActions);
                        this.mSceneSensorActions.put(Integer.valueOf(sceneId), temp);
                        sensorActions = new HashMap();
                        break;
                    default:
                        break;
                }
            }
            if (!sceneThermalEnable) {
                this.mSceneSensorActions.put(Integer.valueOf(208), sensorActions);
                Log.i("ThermalStateManager", "old thermal xml");
            }
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Exception e222222) {
                    e222222.printStackTrace();
                }
            }
            this.mSensorActions.clear();
            HashMap<Integer, SensorTempAction> defaultSensorActions = (HashMap) this.mSceneSensorActions.get(Integer.valueOf(this.mCurActionID));
            if (defaultSensorActions == null) {
                defaultSensorActions = (HashMap) this.mSceneSensorActions.get(Integer.valueOf(208));
            }
            this.mSensorActions.putAll(defaultSensorActions);
            return true;
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
            this.mSceneSensorActions.clear();
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Exception e2222222) {
                    e2222222.printStackTrace();
                }
            }
            return false;
        } catch (XmlPullParserException e4) {
            e4.printStackTrace();
            this.mSceneSensorActions.clear();
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Exception e22222222) {
                    e22222222.printStackTrace();
                }
            }
            return false;
        } catch (IOException e5) {
            e5.printStackTrace();
            this.mSceneSensorActions.clear();
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Exception e222222222) {
                    e222222222.printStackTrace();
                }
            }
            return false;
        } catch (Throwable th) {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Exception e2222222222) {
                    e2222222222.printStackTrace();
                }
            }
        }
    }

    public void dump(PrintWriter pw, String[] args) {
        pw.println("\nTHERMAL STATE MANAGER ");
        pw.println("  Config: ");
        pw.println("        mMutilSensorBatteryClr : " + this.mMutilSensorBatteryClr);
        pw.println("        mNotLimitChargeVal: " + this.mNotLimitChargeVal);
        pw.println("        mScreenOnMaxBcurrentVal: " + this.mScreenOnMaxBcurrentVal);
        pw.println("        mCurrentScreenCtrlBcurrentVal: " + this.mCurrentScreenCtrlBcurrentVal);
        pw.println("        mPostPoneTimeInMSec: " + this.mPostPoneTimeInMSec);
        pw.println("        mSupportPfmcThermalPolicy: " + this.mSupportPfmcThermalPolicy);
        pw.println("        mIsSupportQCOff: " + this.mIsSupportQCOff);
        pw.println("        mIsQuickChargeOff: " + this.mIsQuickChargeOff);
        pw.println("");
        pw.println("  Cust Comb Scene Config: ");
        for (Entry entry : mCustCombActionId.entrySet()) {
            Integer sceneId = (Integer) entry.getValue();
            pw.println("        Comb Scene Key: " + ((String) entry.getKey()) + " Scene Id:" + sceneId);
        }
        pw.println("");
        pw.println("  Sensor Temperature: ");
        for (Entry entry2 : this.mCurTemp.entrySet()) {
            int type = ((Integer) entry2.getKey()).intValue();
            pw.println("    Sensor:" + type + " Temperature:" + ((Integer) entry2.getValue()).intValue());
        }
        pw.println("");
        pw.println("  Current Policy: ");
        pw.println("    Current Action ID:" + getPGSceneId(this.mCurActionID) + "(" + this.mCurActionID + ")");
        pw.println("    CPU  :" + this.mCurCpufreq + "  CPU1 :" + this.mCurCpu1freq + "  CPU2 :" + this.mCurCpu2freq + "  CPU3 :" + this.mCurCpu3freq);
        pw.println("    A15  :" + this.mCurA15Cpufreq);
        pw.println("    GPU  : " + this.mCurGpufreq);
        pw.println("    IPA Power : " + this.mCurIpaPower + " Control Temperature : " + this.mCurIpaTemp + " Switch Temperature : " + this.mCurIpaSwitch);
        pw.println("    Fork On Big Cluster : " + this.mCurForkOnBig);
        pw.println("    Battery Charging Level : " + this.mCurChargingLvl);
        pw.println("    Lcd Limit Level: " + this.mCurLcdLimt);
        pw.println("    Flash Limit, Backgroud : " + this.mCurFlashLimt + " Foregroud : " + this.mCurFrontFlashLimt);
        pw.println("    Charge current, USB : " + this.mCurUsbChargeLimt + "  AC : " + this.mCurAcChargeLimt);
        pw.println("    Charge current, USB AUX: " + this.mCurUsbChargeAuxLimt + "  AC AUX: " + this.mCurAcChargeAuxLimt);
        pw.println("    Charge Direct Charge limit : " + this.mCurDirectChargeLimt);
        pw.println("    HMP ThresholdDown : " + this.mCurThresholdDownPolicy + " ThresholdUp : " + this.mCurThresholdUpPolicy);
    }

    public boolean setChargeHotLimit(int mode, int value) {
        return NativeAdapter.setChargeHotLimit(mode, value);
    }

    public void setChargingLimit(int limitCurrent, String filePath) {
        HardwareAdapter.setChargingLimit(limitCurrent, filePath);
    }

    public boolean setFlashLimit(boolean isFront, boolean limit) {
        return NativeAdapter.setFlashLimit(isFront, limit);
    }

    public boolean setPAFallback(boolean fallback) {
        return NativeAdapter.setPAFallback(fallback);
    }

    public boolean setIspLimit(int value) {
        return NativeAdapter.setIspLimit(value);
    }

    public void setWlanLimit(int level, String filePath) {
        HardwareAdapter.setWlanLimit(level, filePath);
    }

    public boolean setCameraFps(int value) {
        return HardwareAdapter.setCameraFps(value);
    }

    public void sendThermalUIEvent(Context context, String event) {
        BroadcastAdapter.sendThermalUIEvent(context, event);
    }

    public void sendThermalComUIEvent(Context context, String event, String flag) {
        BroadcastAdapter.sendThermalComUIEvent(context, event, flag);
    }

    public void sendVRWarningLevel(String key, int value) {
        BroadcastAdapter.sendVRWarningLevel(this.mContext, key, String.valueOf(value));
    }
}
