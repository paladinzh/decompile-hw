package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.SparseArray;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.MobileSignalController.MobileIconGroup;
import com.android.systemui.statusbar.policy.MobileSignalController.MobileState;
import com.android.systemui.statusbar.policy.NetworkController.SignalCallback;
import com.android.systemui.statusbar.policy.NetworkControllerImpl.SubscriptionDefaults;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SimCardMethod;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.SystemUiUtil;
import com.huawei.cust.HwCustUtils;
import fyusion.vislib.BuildConfig;

public class HwMobileSignalController extends MobileSignalController implements HwMobileSignalInterface {
    private static final boolean IS_FIVE_SIGNAL = SystemProperties.getBoolean("ro.config.hw_show_5_sigbar", true);
    public static final boolean IS_SHOW_ROAMING = SystemProperties.getBoolean("ro.config.isShowRoaming", true);
    public static final boolean mDistinguishHspaCust = SystemProperties.getBoolean("ro.config.distinguish_hspa", true);
    public static final int mHsdpaOpen = SystemProperties.getInt("ro.config.hspap_hsdpa_open", 1);
    public static final boolean mIsSRLTE = SystemProperties.getBoolean("ro.config.hw_srlte", false);
    private static int mSignalLevel = 0;
    private static boolean signalPlusBroadCastSendedAlready = false;
    private final boolean EVDO_DROPTO_1X = SystemProperties.getBoolean("ro.config.evdo_dropto_1x", false);
    private final boolean LTEONLY = SystemProperties.getBoolean("ro.config.hw_lteonly", false);
    protected final String TAG = HwMobileSignalController.class.getSimpleName();
    public int mCallState = 0;
    private HwCustMobileSignalController mCustMobileSignal;
    public ExtData mExtData;
    public ExtData mExtDataOld;
    private boolean mIsCAState = false;
    private boolean mIsRoaming;
    private boolean mIsShowActivity;
    private boolean mIsShowNetworkType = true;
    public ServiceState mServiceState;
    public SignalStrength mSignalStrength;
    public SubscriptionInfo mSubInfo;
    private TelephonyManager mTelephonyManager;

    public static class ExtData {
        public int mDataActivity = 0;
        public int mDataNetType = 0;
        public int mDataState = 0;
        public int mMasterNetWorkLevel = -1;
        public int mMasterNetWorkType = 0;
        public int mSlaveNetWorkLevel = -1;
        public int mSlaveNetWorkType = 0;

        public int[] getExtData() {
            return new int[]{this.mMasterNetWorkType, this.mMasterNetWorkLevel, this.mSlaveNetWorkType, this.mSlaveNetWorkLevel, this.mDataState, this.mDataActivity, this.mDataNetType};
        }

        public void copyFrom(ExtData data) {
            this.mMasterNetWorkType = data.mMasterNetWorkType;
            this.mSlaveNetWorkType = data.mSlaveNetWorkType;
            this.mMasterNetWorkLevel = data.mMasterNetWorkLevel;
            this.mSlaveNetWorkLevel = data.mSlaveNetWorkLevel;
            this.mDataState = data.mDataState;
            this.mDataActivity = data.mDataActivity;
            this.mDataNetType = data.mDataNetType;
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (o == null || !o.getClass().equals(getClass())) {
                return false;
            }
            ExtData other = (ExtData) o;
            if (other.mMasterNetWorkType == this.mMasterNetWorkType && other.mSlaveNetWorkType == this.mSlaveNetWorkType && other.mMasterNetWorkLevel == this.mMasterNetWorkLevel && other.mSlaveNetWorkLevel == this.mSlaveNetWorkLevel && other.mDataState == this.mDataState && other.mDataActivity == this.mDataActivity && other.mDataNetType == this.mDataNetType) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return super.hashCode();
        }

        public String toString() {
            return "ExtData [mMasterNetWorkType=" + this.mMasterNetWorkType + ", mSlaveNetWorkType=" + this.mSlaveNetWorkType + ", mMasterNetWorkLevel=" + this.mMasterNetWorkLevel + ", mSlaveNetWorkLevel=" + this.mSlaveNetWorkLevel + ", mDataState=" + this.mDataState + ", mDataActivity=" + this.mDataActivity + ", mDataNetType=" + this.mDataNetType + "]";
        }
    }

    public HwMobileSignalController(Context context, Config config, boolean hasMobileData, TelephonyManager phone, CallbackHandler callbackHandler, NetworkControllerImpl networkController, SubscriptionInfo info, SubscriptionDefaults defaults, Looper receiverLooper) {
        super(context, config, hasMobileData, phone, callbackHandler, networkController, info, defaults, receiverLooper);
        this.mCustMobileSignal = (HwCustMobileSignalController) HwCustUtils.createObj(HwCustMobileSignalController.class, new Object[]{this, context, info});
        this.mSubInfo = info;
        this.mExtData = new ExtData();
        this.mExtDataOld = new ExtData();
        this.mExtDataOld.copyFrom(this.mExtData);
        this.mTelephonyManager = TelephonyManager.from(context);
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            public boolean runInThread() {
                boolean z = true;
                HwMobileSignalController hwMobileSignalController = HwMobileSignalController.this;
                if (System.getInt(HwMobileSignalController.this.mContext.getContentResolver(), "isShowNetworkType", 1) != 1) {
                    z = false;
                }
                hwMobileSignalController.mIsShowNetworkType = z;
                return false;
            }
        });
    }

    public void updateCallState(int state, String incomingNumber) {
        HwLog.i(this.TAG, "onCallStateChanged: state=" + state);
        this.mCallState = state;
        updateExtNetworkData(this.mSignalStrength, this.mServiceState, this.mCallState);
    }

    public void updateSignalStrength(SignalStrength signalStrength) {
        HwLog.i(this.TAG, "updateSignalStrength: signalStrength=" + signalStrength);
        this.mSignalStrength = signalStrength;
        updateExtNetworkData(signalStrength, this.mServiceState, this.mCallState);
    }

    public int getDataNetType(int subId, int defaultValue) {
        if (!(this.mCurrentState instanceof MobileState) || ((MobileState) this.mCurrentState).dataConnected) {
            return defaultValue;
        }
        return this.mTelephonyManager.getNetworkType(subId);
    }

    public void updateDataConnectionState(int state, int networkType) {
        HwLog.i(this.TAG, "updateDataConnectionState: state=" + state + ",networkType=" + networkType);
        this.mExtData.mDataState = state;
        this.mExtData.mDataNetType = networkType;
        if (this.mCustMobileSignal != null) {
            this.mCustMobileSignal.updateDataNetType(networkType);
        }
    }

    public void updateServiceState(ServiceState state) {
        HwLog.i(this.TAG, "onServiceStateChanged: state=" + state);
        this.mServiceState = state;
        this.mCustMobileSignal.updateCarrierSwitchSettings(this.mContext, this.mTelephonyManager, state);
        updateExtNetworkData(this.mSignalStrength, this.mServiceState, this.mCallState);
    }

    public void updateDataActivity(int direction) {
        this.mExtData.mDataActivity = direction;
        if (this.mCustMobileSignal != null) {
            this.mCustMobileSignal.updateDataConnectedIcon(direction);
        }
    }

    public int getTypeIconHuawei(TelephonyManager telephonyManager, int subId, int typeIcon, boolean hasService, boolean isCAState, boolean dataConnected) {
        if (!hasService) {
            return 0;
        }
        int dataNetType;
        dataConnected &= this.mNetworkController.isSuspend(subId, ((MobileState) this.mCurrentState).airplaneMode, this.mServiceState) ? 0 : 1;
        if (!dataConnected) {
            if (!this.mIsShowNetworkType) {
                return 0;
            }
            dataNetType = SystemUiUtil.getCurrentNetWorkTypeBySlotId(subId);
            int type = TelephonyManager.getNetworkClass(dataNetType);
            Log.d(this.TAG, "getTypeIcon type:" + type);
            switch (type) {
                case 2:
                    Log.d(this.TAG, " 3G");
                    typeIcon = R.drawable.stat_sys_data_connected_3g;
                    break;
                case 3:
                    Log.d(this.TAG, " 4G, isCAState=" + isCAState);
                    if (!isCAState) {
                        typeIcon = R.drawable.stat_sys_data_connected_4g;
                        break;
                    }
                    typeIcon = R.drawable.stat_sys_data_fully_connected_4gplus;
                    break;
                default:
                    Log.d(this.TAG, " 2G");
                    typeIcon = R.drawable.stat_sys_data_connected_2g;
                    break;
            }
            if (this.mCustMobileSignal != null) {
                typeIcon = this.mCustMobileSignal.updateDataTypeNoDataConnected(typeIcon, dataNetType, subId, isCAState);
            }
        }
        dataNetType = SystemUiUtil.getCurrentNetWorkTypeBySlotId(subId);
        if (this.mCustMobileSignal != null && dataConnected) {
            typeIcon = this.mCustMobileSignal.updateDataType(typeIcon, dataNetType, subId, isCAState);
            if (typeIcon == -1) {
                HwLog.i(this.TAG, "if dataNetType equals 0, return 0");
                return 0;
            }
        }
        if (18 == dataNetType && this.mCustMobileSignal != null && this.mCustMobileSignal.isUsingVoWifi()) {
            return typeIcon;
        }
        typeIcon = checkAndGetCmccNetworkTypeIcon(typeIcon, dataNetType);
        if (typeIcon == 0) {
            HwLog.i(this.TAG, "typeIcon == 0, ensure the dataType is not 0, but this represents unusually");
            typeIcon = R.drawable.stat_sys_data_connected_2g;
        }
        return typeIcon;
    }

    public void addIconGroupsHuawei(SparseArray<MobileIconGroup> networkToIconLookup) {
        addMoreIconGroup(networkToIconLookup);
        if (this.mCustMobileSignal != null) {
            this.mCustMobileSignal.mapIconSets(networkToIconLookup);
        }
    }

    private void addMoreIconGroup(SparseArray<MobileIconGroup> networkToIconLookup) {
        if (this.mConfig.hspaDataDistinguishable) {
            addIconGroup(networkToIconLookup, 15, getHSPAPIconGroup());
        }
        boolean show4gForLte = this.mContext.getResources().getBoolean(R.bool.config_show4GForLTE);
        if (networkToIconLookup != null) {
            addIconGroup(networkToIconLookup, 17, HwTelephonyIcons.TDS);
            addIconGroup(networkToIconLookup, 30, TelephonyIcons.THREE_G);
            if (show4gForLte) {
                addIconGroup(networkToIconLookup, 31, TelephonyIcons.FOUR_G);
                addIconGroup(networkToIconLookup, 18, TelephonyIcons.FOUR_G);
            } else {
                addIconGroup(networkToIconLookup, 31, TelephonyIcons.LTE);
                addIconGroup(networkToIconLookup, 18, TelephonyIcons.LTE);
            }
            if (this.mCustMobileSignal != null && this.mCustMobileSignal.isUsingVoWifi()) {
                addIconGroup(networkToIconLookup, 18, HwTelephonyIcons.IWLAN);
            }
        }
    }

    private void addIconGroup(SparseArray<MobileIconGroup> networkToIconLookup, int networkType, MobileIconGroup iconGroup) {
        networkToIconLookup.put(networkType, iconGroup);
    }

    private MobileIconGroup getHSPAPIconGroup() {
        if (1 == mHsdpaOpen && mDistinguishHspaCust) {
            return HwTelephonyIcons.H_PLUS;
        }
        return TelephonyIcons.THREE_G;
    }

    public boolean isDirty() {
        boolean z = false;
        if (this.mExtDataOld != null) {
            if (this.mExtDataOld.equals(this.mExtData)) {
                z = false;
            } else {
                z = true;
            }
            if (z && this.mExtData != null) {
                HwLog.i(this.TAG, "mExtDataOld:" + this.mExtDataOld.toString() + " mExtData:" + this.mExtData.toString());
            }
        }
        if (super.isDirty()) {
            return true;
        }
        return z;
    }

    public void setCAState(boolean isCAState) {
        this.mIsCAState = isCAState;
    }

    public boolean isCAStateEnable() {
        return this.mIsCAState;
    }

    public void handleBroadcastHuawei(Intent intent) {
    }

    public boolean isWifiCharged() {
        return false;
    }

    public boolean isWifiNoInternet() {
        return false;
    }

    public MobileIconGroup getIconsHuawei() {
        MobileIconGroup icons = (MobileIconGroup) super.getIcons();
        if (SystemUiUtil.isSupportVSim()) {
            int vimSub = NetWorkUtils.getVSimSubId();
            if (vimSub == this.mSubInfo.getSubscriptionId() && vimSub != -1) {
                icons = HwTelephonyIcons.TIANJITONG;
            }
        }
        if (IS_FIVE_SIGNAL) {
            return HwTelephonyIcons.getFiveSignalIconGroup(icons);
        }
        if (this.mCustMobileSignal != null) {
            icons = this.mCustMobileSignal.getIcons(icons);
        }
        return icons;
    }

    public int getDefaultDataSubId(int sub) {
        if (!SystemUiUtil.isSupportVSim()) {
            return sub;
        }
        int vimSubid = NetWorkUtils.getVSimSubId();
        if (vimSubid != -1) {
            return vimSubid;
        }
        return sub;
    }

    private void updateExtNetworkData(SignalStrength signalStrength, ServiceState serviceState, int callState) {
        this.mExtDataOld.copyFrom(this.mExtData);
        if (serviceState == null) {
            Log.i(this.TAG, "serviceState is null");
            return;
        }
        int voiceState = serviceState.getVoiceRegState();
        boolean hasService = hasService(serviceState);
        if (signalStrength == null || !hasService) {
            this.mExtData.mMasterNetWorkType = 0;
            this.mExtData.mMasterNetWorkLevel = -1;
            this.mExtData.mSlaveNetWorkType = 0;
            this.mExtData.mSlaveNetWorkLevel = -1;
            Log.i(this.TAG, "signalStrength=" + signalStrength + " hasService=" + hasService);
            return;
        }
        int subId = this.mSubInfo.getSubscriptionId();
        TelephonyManager telephonyManager = TelephonyManager.from(this.mContext);
        int networkType = telephonyManager.getNetworkType(subId);
        int networkClass = TelephonyManager.getNetworkClass(networkType);
        int phoneType = telephonyManager.getCurrentPhoneType(subId);
        int targetClass = networkClass;
        if (phoneType == 2) {
            if (serviceState.getDataRegState() != 0) {
                targetClass = 1;
                Log.i(this.TAG, "PHONE_TYPE_CDMA PS is out service, display 2G(1x)");
            }
            if (mIsSRLTE && callState != 0) {
                targetClass = 1;
                Log.i(this.TAG, "CDMA card1 on call in SRLTE mode,so we force refresh views to display 2G(1x)");
            }
            if (this.EVDO_DROPTO_1X && targetClass == 2 && callState != 0) {
                targetClass = 1;
                NetWorkUtils.set3GCallingState(true, subId);
            } else {
                NetWorkUtils.set3GCallingState(false, subId);
            }
            if (targetClass == 3) {
                this.mExtData.mMasterNetWorkType = 3;
                this.mExtData.mMasterNetWorkLevel = signalStrength.getLteLevel();
                this.mExtData.mSlaveNetWorkType = 1;
                if (voiceState == 1) {
                    this.mExtData.mSlaveNetWorkLevel = 0;
                } else {
                    this.mExtData.mSlaveNetWorkLevel = signalStrength.getCdmaLevel();
                }
                String dataSettingMode = BuildConfig.FLAVOR;
                if (this.LTEONLY) {
                    if (this.mCustMobileSignal != null) {
                        dataSettingMode = this.mCustMobileSignal.getDataSettingMode(dataSettingMode);
                    }
                    if ("MODE_LTETDD_ONLY_EX".equals(dataSettingMode)) {
                        this.mExtData.mSlaveNetWorkType = 0;
                        this.mExtData.mSlaveNetWorkLevel = -1;
                    }
                }
            } else if (targetClass == 2) {
                this.mExtData.mMasterNetWorkType = 2;
                this.mExtData.mMasterNetWorkLevel = signalStrength.getEvdoLevel();
                this.mExtData.mSlaveNetWorkType = 1;
                if (voiceState == 1) {
                    this.mExtData.mSlaveNetWorkLevel = 0;
                } else {
                    this.mExtData.mSlaveNetWorkLevel = signalStrength.getCdmaLevel();
                }
            } else if (targetClass == 1) {
                this.mExtData.mMasterNetWorkType = 1;
                this.mExtData.mMasterNetWorkLevel = signalStrength.getCdmaLevel();
                this.mExtData.mSlaveNetWorkType = 0;
                this.mExtData.mSlaveNetWorkLevel = -1;
            } else {
                this.mExtData.mMasterNetWorkType = 0;
                this.mExtData.mMasterNetWorkLevel = -1;
                this.mExtData.mSlaveNetWorkType = 0;
                this.mExtData.mSlaveNetWorkLevel = -1;
            }
        } else if (phoneType == 1 || phoneType == 6) {
            this.mExtData.mMasterNetWorkType = networkClass;
            this.mExtData.mMasterNetWorkLevel = signalStrength.getLevel();
            this.mExtData.mSlaveNetWorkType = 0;
            this.mExtData.mSlaveNetWorkLevel = -1;
            if (this.mCustMobileSignal != null && phoneType == 1) {
                this.mCustMobileSignal.updateExtNetworkData(this.mSignalStrength, this.mServiceState, this.mCallState);
            }
        } else {
            Log.e(this.TAG, "error phone type");
        }
        Log.i(this.TAG, "subId:" + subId + " phoneType:" + phoneType + " networktype:" + networkType + " targetClass:" + targetClass + " masterLevel:" + this.mExtData.mMasterNetWorkLevel + " slaveLevel:" + this.mExtData.mSlaveNetWorkLevel);
    }

    private boolean hasService(ServiceState serviceState) {
        boolean z = true;
        if (serviceState == null) {
            return false;
        }
        switch (serviceState.getVoiceRegState()) {
            case 1:
            case 2:
                if (serviceState.getDataRegState() != 0) {
                    z = false;
                }
                return z;
            case 3:
                return false;
            default:
                return true;
        }
    }

    public boolean isShowMmsNetworkIcon() {
        if (this.mCustMobileSignal != null) {
            return this.mCustMobileSignal.isShowMmsNetworkIcon();
        }
        return false;
    }

    public boolean isRoamingHuawei(ServiceState serviceState) {
        if (serviceState == null) {
            return false;
        }
        return serviceState.getRoaming();
    }

    public void updateRoamingState(boolean isRoaming) {
        if (this.mIsRoaming != isRoaming) {
            HwLog.i(this.TAG, "updateRoamingState:isRoaming=" + isRoaming);
            if (isRoaming) {
                Intent intent = new Intent();
                intent.setAction("com.android.systemui.statusbar.policy.GET_ROAM_STATE");
                this.mContext.sendBroadcast(intent);
            }
        }
        this.mIsRoaming = isRoaming;
    }

    public boolean isRoamingHuawei() {
        return this.mIsRoaming;
    }

    public void updateActivityShowState(boolean showActivity) {
        this.mIsShowActivity = showActivity;
    }

    public boolean isShowActivity() {
        return this.mIsShowActivity;
    }

    public void notifyListeners(final SignalCallback callback) {
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            public boolean runInThread() {
                int subId = HwMobileSignalController.this.mSubscriptionInfo.getSubscriptionId();
                SystemUiUtil.setCurrentNetWorkTypeBySlotId(subId, HwMobileSignalController.this.mPhone.getNetworkType(subId));
                if (!SimCardMethod.isCardAbsent(TelephonyManager.from(HwMobileSignalController.this.mContext), subId)) {
                    return true;
                }
                HwLog.i(HwMobileSignalController.this.TAG, " subId :" + subId + " is not present, so return and not update view.");
                return false;
            }

            public void runInUI() {
                HwMobileSignalController.this.callSupernotifyListeners(callback);
            }
        });
    }

    private void callSupernotifyListeners(SignalCallback callback) {
        super.notifyListeners(callback);
    }

    public void notifyListenerHuawei(SignalCallback callback, MobileIconGroup icons, int typeIcon, boolean showDataIcon) {
        boolean z = false;
        boolean isSuspend = this.mNetworkController.isSuspend(this.mSubInfo.getSubscriptionId(), ((MobileState) this.mCurrentState).airplaneMode, this.mServiceState);
        boolean z2 = showDataIcon && !isSuspend;
        updateActivityShowState(z2);
        if (((MobileState) this.mCurrentState).isRoaming) {
            z2 = IS_SHOW_ROAMING;
        } else {
            z2 = false;
        }
        updateRoamingState(z2);
        int subscriptionId = this.mSubInfo.getSubscriptionId();
        int i = ((MobileState) this.mCurrentState).inetCondition;
        if (((MobileState) this.mCurrentState).isRoaming) {
            z = IS_SHOW_ROAMING;
        }
        callback.setExtData(subscriptionId, i, z, isSuspend, this.mExtData.getExtData());
        HwLog.i(this.TAG, "notifyListenerHuawei::origin typeIcon:" + typeIcon + " icons.mName:" + icons.mName + " isSuspend:" + isSuspend + ", showDataIcon=" + showDataIcon + ", dataConnected=" + ((MobileState) this.mCurrentState).dataConnected + ", isRoaming=" + this.mIsRoaming);
    }

    public int handleShowFiveSignalException(int signalLevel) {
        if (signalLevel != 5 || IS_FIVE_SIGNAL) {
            return signalLevel;
        }
        return signalLevel - 1;
    }

    private int checkAndGetCmccNetworkTypeIcon(int typeIcon, int networkType) {
        if (!SystemUiUtil.isChinaMobileArea()) {
            return typeIcon;
        }
        switch (networkType) {
            case 1:
            case 16:
                HwLog.i(this.TAG, "CMCC 2G --> G icon customization");
                return R.drawable.stat_sys_data_connected_g_for_gsm_gprs;
            case 2:
                HwLog.i(this.TAG, "CMCC EDGE show E icon customization");
                return R.drawable.stat_sys_data_fully_connected_e;
            default:
                return typeIcon;
        }
    }
}
