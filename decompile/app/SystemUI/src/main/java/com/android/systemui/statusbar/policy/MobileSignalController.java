package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.android.systemui.R;
import com.android.systemui.linkplus.RoamPlus;
import com.android.systemui.statusbar.policy.NetworkController.IconState;
import com.android.systemui.statusbar.policy.NetworkController.SignalCallback;
import com.android.systemui.statusbar.policy.NetworkControllerImpl.SubscriptionDefaults;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIIdleHandler;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import fyusion.vislib.BuildConfig;
import java.io.PrintWriter;
import java.util.BitSet;
import java.util.Objects;

public abstract class MobileSignalController extends SignalController<MobileState, MobileIconGroup> implements HwMobileSignalInterface {
    protected Config mConfig;
    protected Context mContext;
    protected int mDataNetType = 0;
    private int mDataState = 0;
    private MobileIconGroup mDefaultIcons;
    private final SubscriptionDefaults mDefaults;
    private final String mNetworkNameDefault;
    private final String mNetworkNameSeparator;
    final SparseArray<MobileIconGroup> mNetworkToIconLookup;
    protected final TelephonyManager mPhone;
    protected final PhoneStateListener mPhoneStateListener;
    private ServiceState mServiceState;
    protected ServiceState mServiceStateLast;
    private SignalStrength mSignalStrength;
    protected SignalStrength mSignalStrengthLast;
    protected final SubscriptionInfo mSubscriptionInfo;

    class MobilePhoneStateListener extends PhoneStateListener {
        public MobilePhoneStateListener(int subId, Looper looper) {
            super(subId, looper);
        }

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            MobileSignalController.this.mSignalStrength = signalStrength;
        }

        public void onServiceStateChanged(ServiceState state) {
            MobileSignalController.this.mServiceState = state;
            MobileSignalController.this.mDataNetType = state.getDataNetworkType();
        }

        public void onDataConnectionStateChanged(int state, int networkType) {
            MobileSignalController.this.mDataState = state;
            MobileSignalController.this.mDataNetType = networkType;
        }

        public void onDataActivity(int direction) {
        }

        public void onCarrierNetworkChange(boolean active) {
            if (MobileSignalController.DEBUG) {
                Log.d(MobileSignalController.this.mTag, "onCarrierNetworkChange: active=" + active);
            }
            ((MobileState) MobileSignalController.this.mCurrentState).carrierNetworkChangeMode = active;
            MobileSignalController.this.updateTelephony(true);
        }
    }

    class HwMobilePhoneStateListener extends MobilePhoneStateListener {
        int mSimSubId;

        public HwMobilePhoneStateListener(int subId, Looper looper) {
            super(subId, looper);
            this.mSimSubId = subId;
        }

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            MobileSignalController.this.mSignalStrengthLast = signalStrength;
            boolean isSuspend = MobileSignalController.this.mNetworkController.isSuspend(this.mSimSubId, ((MobileState) MobileSignalController.this.mCurrentState).airplaneMode, MobileSignalController.this.mServiceState);
            int level = MobileSignalController.this.mSignalStrength == null ? 0 : MobileSignalController.this.mSignalStrength.getLevel();
            HwLog.i(MobileSignalController.this.mTag, "onSignalStrengthsChanged signalStrength=" + signalStrength + (signalStrength == null ? BuildConfig.FLAVOR : " level=" + signalStrength.getLevel()) + " isSuspend=" + isSuspend + (MobileSignalController.this.mSignalStrength == null ? BuildConfig.FLAVOR : " levelOld=" + MobileSignalController.this.mSignalStrength.getLevel()) + ",mSignalStrength level=" + level);
            if (!isSuspend || (signalStrength != null && signalStrength.getLevel() >= level)) {
                super.onSignalStrengthsChanged(signalStrength);
                SystemUIThread.runAsync(new SimpleAsyncTask() {
                    boolean isDirty = false;

                    public boolean runInThread() {
                        MobileSignalController.this.updateSignalStrength(MobileSignalController.this.mSignalStrength);
                        MobileSignalController.this.mDataNetType = MobileSignalController.this.getDataNetType(HwMobilePhoneStateListener.this.mSimSubId, MobileSignalController.this.mDataNetType);
                        this.isDirty = MobileSignalController.this.isDirty();
                        return true;
                    }

                    public void runInUI() {
                        MobileSignalController.this.updateTelephony(this.isDirty);
                    }
                });
            }
        }

        public void onServiceStateChanged(ServiceState state) {
            if (state == null) {
                HwLog.e(MobileSignalController.this.mTag, "onServiceStateChanged state == null, return !!");
                return;
            }
            MobileSignalController.this.mServiceStateLast = state;
            boolean isSuspend = MobileSignalController.this.mNetworkController.isSuspend(this.mSimSubId, ((MobileState) MobileSignalController.this.mCurrentState).airplaneMode, MobileSignalController.this.mServiceState);
            HwLog.i(MobileSignalController.this.mTag, "onServiceStateChanged voiceState=" + state.getVoiceRegState() + " dataState=" + state.getDataRegState() + " isSuspend=" + isSuspend + (MobileSignalController.this.mServiceState == null ? BuildConfig.FLAVOR : " voiceStateOld=" + MobileSignalController.this.mServiceState.getVoiceRegState() + " dataStateOld=" + MobileSignalController.this.mServiceState.getDataRegState()));
            if (!isSuspend || MobileSignalController.this.hasService(state)) {
                super.onServiceStateChanged(state);
                SystemUIThread.runAsync(new SimpleAsyncTask() {
                    boolean isDirty = false;

                    public boolean runInThread() {
                        MobileSignalController.this.updateServiceState(MobileSignalController.this.mServiceState);
                        this.isDirty = MobileSignalController.this.isDirty();
                        return true;
                    }

                    public void runInUI() {
                        MobileSignalController.this.updateTelephony(this.isDirty);
                    }
                });
                RoamPlus.searchServiceSuccess(MobileSignalController.this.mServiceState, MobileSignalController.this.mContext);
            }
        }

        public void onDataConnectionStateChanged(final int state, final int networkType) {
            HwLog.i(MobileSignalController.this.mTag, "onDataConnectionStateChanged: state=" + state + " type=" + networkType);
            super.onDataConnectionStateChanged(state, networkType);
            SystemUIThread.runAsync(new SimpleAsyncTask() {
                boolean isDirty = false;

                public boolean runInThread() {
                    MobileSignalController.this.updateDataConnectionState(state, networkType);
                    this.isDirty = MobileSignalController.this.isDirty();
                    return true;
                }

                public void runInUI() {
                    MobileSignalController.this.updateTelephony(this.isDirty);
                }
            });
        }

        public void onDataActivity(final int direction) {
            HwLog.i(MobileSignalController.this.mTag, "onDataActivity: direction=" + direction);
            super.onDataActivity(direction);
            SystemUIIdleHandler.addToIdleMessage(new Runnable() {
                public void run() {
                    Log.d(MobileSignalController.this.mTag, "mSimSubId:" + HwMobilePhoneStateListener.this.mSimSubId + " onDataActivity: direction=" + direction);
                    MobileSignalController.this.updateDataActivity(direction);
                    MobileSignalController.this.setActivity(direction);
                }
            }, 1001);
        }

        public void onCallStateChanged(final int state, final String incomingNumber) {
            HwLog.i(MobileSignalController.this.mTag, "mSimSubId:" + this.mSimSubId + " state:" + state);
            SystemUIThread.runAsync(new SimpleAsyncTask() {
                public boolean runInThread() {
                    MobileSignalController.this.updateCallState(state, incomingNumber);
                    MobileSignalController.this.mNetworkController.updateOtherSubState(HwMobilePhoneStateListener.this.mSimSubId, state);
                    return true;
                }

                public void runInUI() {
                    MobileSignalController.this.notifyListeners();
                }
            });
        }
    }

    static class HwMobileStateBase extends State {
        boolean isRoaming;

        HwMobileStateBase() {
        }

        public void copyFrom(State s) {
            super.copyFrom(s);
            if (s instanceof MobileState) {
                this.isRoaming = ((MobileState) s).isRoaming;
            }
        }

        protected void toString(StringBuilder builder) {
            super.toString(builder);
            builder.append(',');
            builder.append("isRoaming=").append(this.isRoaming);
        }

        public boolean equals(Object o) {
            if (super.equals(o) && ((MobileState) o).isRoaming == this.isRoaming) {
                return true;
            }
            return false;
        }
    }

    public static class MobileIconGroup extends IconGroup {
        final int mDataContentDescription;
        final int mDataType;
        final boolean mIsWide;
        final int mQsDataType;

        public /* bridge */ /* synthetic */ String toString() {
            return super.toString();
        }

        public MobileIconGroup(String name, int[][] sbIcons, int[][] qsIcons, int[] contentDesc, int sbNullState, int qsNullState, int sbDiscState, int qsDiscState, int discContentDesc, int dataContentDesc, int dataType, boolean isWide, int qsDataType) {
            super(name, sbIcons, qsIcons, contentDesc, sbNullState, qsNullState, sbDiscState, qsDiscState, discContentDesc);
            this.mDataContentDescription = dataContentDesc;
            this.mDataType = dataType;
            this.mIsWide = isWide;
            this.mQsDataType = qsDataType;
        }
    }

    public static class MobileState extends HwMobileStateBase {
        boolean airplaneMode;
        boolean carrierNetworkChangeMode;
        boolean dataConnected;
        boolean dataSim;
        boolean isDefault;
        boolean isEmergency;
        public String networkName;
        String networkNameData;
        boolean userSetup;

        public /* bridge */ /* synthetic */ String toString() {
            return super.toString();
        }

        public void copyFrom(State s) {
            super.copyFrom(s);
            MobileState state = (MobileState) s;
            this.dataSim = state.dataSim;
            this.networkName = state.networkName;
            this.networkNameData = state.networkNameData;
            this.dataConnected = state.dataConnected;
            this.isDefault = state.isDefault;
            this.isEmergency = state.isEmergency;
            this.airplaneMode = state.airplaneMode;
            this.carrierNetworkChangeMode = state.carrierNetworkChangeMode;
            this.userSetup = state.userSetup;
        }

        protected void toString(StringBuilder builder) {
            super.toString(builder);
            builder.append(',');
            builder.append("dataSim=").append(this.dataSim).append(',');
            builder.append("networkName=").append(this.networkName).append(',');
            builder.append("networkNameData=").append(this.networkNameData).append(',');
            builder.append("dataConnected=").append(this.dataConnected).append(',');
            builder.append("isDefault=").append(this.isDefault).append(',');
            builder.append("isEmergency=").append(this.isEmergency).append(',');
            builder.append("airplaneMode=").append(this.airplaneMode).append(',');
            builder.append("carrierNetworkChangeMode=").append(this.carrierNetworkChangeMode).append(',');
            builder.append("userSetup=").append(this.userSetup);
        }

        public boolean equals(Object o) {
            if (super.equals(o) && Objects.equals(((MobileState) o).networkName, this.networkName) && Objects.equals(((MobileState) o).networkNameData, this.networkNameData) && ((MobileState) o).dataSim == this.dataSim && ((MobileState) o).dataConnected == this.dataConnected && ((MobileState) o).isEmergency == this.isEmergency && ((MobileState) o).airplaneMode == this.airplaneMode && ((MobileState) o).carrierNetworkChangeMode == this.carrierNetworkChangeMode && ((MobileState) o).userSetup == this.userSetup) {
                return ((MobileState) o).isDefault == this.isDefault;
            } else {
                return false;
            }
        }
    }

    public MobileSignalController(Context context, Config config, boolean hasMobileData, TelephonyManager phone, CallbackHandler callbackHandler, NetworkControllerImpl networkController, SubscriptionInfo info, SubscriptionDefaults defaults, Looper receiverLooper) {
        super("MobileSignalController(" + info.getSubscriptionId() + ")", context, 0, callbackHandler, networkController);
        this.mContext = context;
        this.mNetworkToIconLookup = new SparseArray();
        this.mConfig = config;
        this.mPhone = phone;
        this.mDefaults = defaults;
        this.mSubscriptionInfo = info;
        this.mPhoneStateListener = new HwMobilePhoneStateListener(info.getSubscriptionId(), receiverLooper);
        this.mNetworkNameSeparator = getStringIfExists(R.string.status_bar_network_name_separator);
        this.mNetworkNameDefault = getStringIfExists(17040012);
        mapIconSets();
        if ((this.mCurrentState instanceof MobileState) && (this.mLastState instanceof MobileState)) {
            String networkName;
            if (info.getCarrierName() != null) {
                networkName = info.getCarrierName().toString();
            } else {
                networkName = this.mNetworkNameDefault;
            }
            MobileState mobileState = (MobileState) this.mLastState;
            ((MobileState) this.mCurrentState).networkName = networkName;
            mobileState.networkName = networkName;
            mobileState = (MobileState) this.mLastState;
            ((MobileState) this.mCurrentState).networkNameData = networkName;
            mobileState.networkNameData = networkName;
            mobileState = (MobileState) this.mLastState;
            ((MobileState) this.mCurrentState).enabled = hasMobileData;
            mobileState.enabled = hasMobileData;
            mobileState = (MobileState) this.mLastState;
            IconGroup iconGroup = this.mDefaultIcons;
            ((MobileState) this.mCurrentState).iconGroup = iconGroup;
            mobileState.iconGroup = iconGroup;
            updateDataSim();
        }
    }

    public void setConfiguration(Config config) {
        this.mConfig = config;
        mapIconSets();
        updateTelephony(true);
    }

    public void setAirplaneMode(boolean airplaneMode) {
        ((MobileState) this.mCurrentState).airplaneMode = airplaneMode;
        notifyListenersIfNecessary();
    }

    public void setUserSetupComplete(boolean userSetup) {
        ((MobileState) this.mCurrentState).userSetup = userSetup;
        notifyListenersIfNecessary();
    }

    public void updateConnectivity(BitSet connectedTransports, BitSet validatedTransports) {
        boolean isValidated = validatedTransports.get(this.mTransportType);
        ((MobileState) this.mCurrentState).isDefault = connectedTransports.get(this.mTransportType);
        MobileState mobileState = (MobileState) this.mCurrentState;
        int i = (isValidated || !((MobileState) this.mCurrentState).isDefault) ? 1 : 0;
        mobileState.inetCondition = i;
        notifyListenersIfNecessary();
    }

    public void setCarrierNetworkChangeMode(boolean carrierNetworkChangeMode) {
        ((MobileState) this.mCurrentState).carrierNetworkChangeMode = carrierNetworkChangeMode;
        updateTelephony(true);
    }

    public void registerListener() {
        this.mPhone.listen(this.mPhoneStateListener, 66017);
    }

    public void unregisterListener() {
        this.mPhone.listen(this.mPhoneStateListener, 0);
    }

    private void mapIconSets() {
        this.mNetworkToIconLookup.clear();
        this.mNetworkToIconLookup.put(5, TelephonyIcons.THREE_G);
        this.mNetworkToIconLookup.put(6, TelephonyIcons.THREE_G);
        this.mNetworkToIconLookup.put(12, TelephonyIcons.THREE_G);
        this.mNetworkToIconLookup.put(14, TelephonyIcons.THREE_G);
        this.mNetworkToIconLookup.put(3, TelephonyIcons.THREE_G);
        if (this.mConfig.showAtLeast3G) {
            this.mNetworkToIconLookup.put(0, TelephonyIcons.THREE_G);
            this.mNetworkToIconLookup.put(2, TelephonyIcons.THREE_G);
            this.mNetworkToIconLookup.put(4, TelephonyIcons.THREE_G);
            this.mNetworkToIconLookup.put(7, TelephonyIcons.THREE_G);
            this.mDefaultIcons = TelephonyIcons.THREE_G;
        } else {
            this.mNetworkToIconLookup.put(0, TelephonyIcons.UNKNOWN);
            this.mNetworkToIconLookup.put(2, TelephonyIcons.E);
            this.mNetworkToIconLookup.put(4, TelephonyIcons.ONE_X);
            this.mNetworkToIconLookup.put(7, TelephonyIcons.ONE_X);
            this.mDefaultIcons = TelephonyIcons.G;
        }
        MobileIconGroup hGroup = TelephonyIcons.THREE_G;
        if (this.mConfig.hspaDataDistinguishable) {
            hGroup = TelephonyIcons.H;
        }
        this.mNetworkToIconLookup.put(8, hGroup);
        this.mNetworkToIconLookup.put(9, hGroup);
        this.mNetworkToIconLookup.put(10, hGroup);
        this.mNetworkToIconLookup.put(15, hGroup);
        if (isCAStateEnable()) {
            this.mNetworkToIconLookup.put(13, HwTelephonyIcons.FORGPLUS);
        } else if (this.mConfig.show4gForLte) {
            this.mNetworkToIconLookup.put(13, TelephonyIcons.FOUR_G);
        } else {
            this.mNetworkToIconLookup.put(13, TelephonyIcons.LTE);
        }
        this.mNetworkToIconLookup.put(18, TelephonyIcons.WFC);
        addIconGroupsHuawei(this.mNetworkToIconLookup);
    }

    public void notifyListeners(SignalCallback callback) {
        boolean z;
        boolean z2;
        boolean z3;
        boolean z4;
        int typeIcon;
        MobileIconGroup icons = getIconsHuawei();
        String contentDescription = getStringIfExists(getContentDescription());
        String dataContentDescription = getStringIfExists(icons.mDataContentDescription);
        if (((MobileState) this.mCurrentState).iconGroup == TelephonyIcons.DATA_DISABLED) {
            z = ((MobileState) this.mCurrentState).userSetup;
        } else {
            z = false;
        }
        if (((MobileState) this.mCurrentState).dataConnected || ((MobileState) this.mCurrentState).iconGroup == TelephonyIcons.ROAMING || ((MobileState) this.mCurrentState).iconGroup == TelephonyIcons.DATA_DISABLED || isShowMmsNetworkIcon()) {
            z2 = true;
        } else {
            z2 = z;
        }
        boolean z5 = ((MobileState) this.mCurrentState).enabled && !((MobileState) this.mCurrentState).airplaneMode;
        IconState iconState = new IconState(z5, getCurrentIconId(), contentDescription);
        int qsTypeIcon = 0;
        IconState qsIcon = null;
        String description = null;
        if (((MobileState) this.mCurrentState).dataSim) {
            qsTypeIcon = z2 ? icons.mQsDataType : 0;
            z5 = ((MobileState) this.mCurrentState).enabled ? !((MobileState) this.mCurrentState).isEmergency : false;
            iconState = new IconState(z5, getQsCurrentIconId(), contentDescription);
            description = ((MobileState) this.mCurrentState).isEmergency ? null : ((MobileState) this.mCurrentState).networkName;
        }
        if (!((MobileState) this.mCurrentState).dataConnected || ((MobileState) this.mCurrentState).carrierNetworkChangeMode) {
            z3 = false;
        } else {
            z3 = ((MobileState) this.mCurrentState).activityIn;
        }
        if (!((MobileState) this.mCurrentState).dataConnected || ((MobileState) this.mCurrentState).carrierNetworkChangeMode) {
            z4 = false;
        } else {
            z4 = ((MobileState) this.mCurrentState).activityOut;
        }
        if (z2) {
            typeIcon = icons.mDataType;
        }
        typeIcon = getTypeIconHuawei(this.mPhone, this.mSubscriptionInfo.getSubscriptionId(), icons.mDataType, hasService(), isCAStateEnable(), ((MobileState) this.mCurrentState).dataConnected);
        notifyListenerHuawei(callback, icons, typeIcon, z2);
        callback.setMobileDataIndicators(iconState, qsIcon, typeIcon, qsTypeIcon, z3, z4, dataContentDescription, description, icons.mIsWide, this.mSubscriptionInfo.getSubscriptionId());
    }

    protected MobileState cleanState() {
        return new MobileState();
    }

    private boolean hasService() {
        boolean z = true;
        if (this.mServiceState == null) {
            return false;
        }
        switch (this.mServiceState.getVoiceRegState()) {
            case 1:
            case 2:
                if (this.mServiceState.getDataRegState() != 0) {
                    z = false;
                }
                return z;
            case 3:
                return false;
            default:
                return true;
        }
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

    public boolean isEmergencyOnly() {
        return this.mServiceState != null ? this.mServiceState.isEmergencyOnly() : false;
    }

    private boolean isCarrierNetworkChangeActive() {
        return ((MobileState) this.mCurrentState).carrierNetworkChangeMode;
    }

    public void handleBroadcast(Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.provider.Telephony.SPN_STRINGS_UPDATED") || action.equals("android.provider.Telephony.SPN_STRINGS_UPDATED_VSIM")) {
            updateNetworkName(intent.getBooleanExtra("showSpn", false), intent.getStringExtra("spn"), intent.getStringExtra("spnData"), intent.getBooleanExtra("showPlmn", false), intent.getStringExtra("plmn"));
            notifyListenersIfNecessary();
        } else if (action.equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")) {
            updateDataSim();
            notifyListenersIfNecessary();
        }
    }

    private void updateDataSim() {
        boolean z = true;
        int defaultDataSub = getDefaultDataSubId(this.mDefaults.getDefaultDataSubId());
        if (SubscriptionManager.isValidSubscriptionId(defaultDataSub)) {
            MobileState mobileState = (MobileState) this.mCurrentState;
            if (defaultDataSub != this.mSubscriptionInfo.getSubscriptionId()) {
                z = false;
            }
            mobileState.dataSim = z;
            return;
        }
        ((MobileState) this.mCurrentState).dataSim = true;
    }

    void updateNetworkName(boolean showSpn, String spn, String dataSpn, boolean showPlmn, String plmn) {
        if (CHATTY) {
            Log.d("CarrierLabel", "updateNetworkName showSpn=" + showSpn + " spn=" + spn + " dataSpn=" + dataSpn + " showPlmn=" + showPlmn + " plmn=" + plmn);
        }
        StringBuilder str = new StringBuilder();
        StringBuilder strData = new StringBuilder();
        if (showPlmn && plmn != null) {
            str.append(plmn);
            strData.append(plmn);
        }
        if (showSpn && !TextUtils.isEmpty(spn)) {
            if (str.length() != 0) {
                str.append(this.mNetworkNameSeparator);
            }
            str.append(spn);
        }
        if (str.length() != 0) {
            ((MobileState) this.mCurrentState).networkName = str.toString();
        } else {
            ((MobileState) this.mCurrentState).networkName = this.mNetworkNameDefault;
        }
        if (showSpn && dataSpn != null) {
            if (strData.length() != 0) {
                strData.append(this.mNetworkNameSeparator);
            }
            strData.append(dataSpn);
        }
        if (strData.length() != 0) {
            ((MobileState) this.mCurrentState).networkNameData = strData.toString();
            return;
        }
        ((MobileState) this.mCurrentState).networkNameData = this.mNetworkNameDefault;
    }

    protected final void updateTelephony(boolean isDirty) {
        boolean z;
        boolean z2 = false;
        if (DEBUG) {
            Log.d(this.mTag, "updateTelephonySignalStrength: hasService=" + hasService() + " ss=" + this.mSignalStrength);
        }
        MobileState mobileState = (MobileState) this.mCurrentState;
        if (!hasService() || this.mSignalStrength == null) {
            z = false;
        } else {
            z = true;
        }
        mobileState.connected = z;
        if (((MobileState) this.mCurrentState).connected) {
            if (this.mSignalStrength.isGsm() || !this.mConfig.alwaysShowCdmaRssi) {
                ((MobileState) this.mCurrentState).level = this.mSignalStrength.getLevel();
            } else {
                ((MobileState) this.mCurrentState).level = this.mSignalStrength.getCdmaLevel();
            }
        }
        if (this.mNetworkToIconLookup.indexOfKey(this.mDataNetType) >= 0) {
            ((MobileState) this.mCurrentState).iconGroup = (IconGroup) this.mNetworkToIconLookup.get(this.mDataNetType);
        } else {
            ((MobileState) this.mCurrentState).iconGroup = this.mDefaultIcons;
        }
        ((MobileState) this.mCurrentState).level = handleShowFiveSignalException(((MobileState) this.mCurrentState).level);
        mobileState = (MobileState) this.mCurrentState;
        if (((MobileState) this.mCurrentState).connected && this.mDataState == 2) {
            z2 = true;
        }
        mobileState.dataConnected = z2;
        if (isCarrierNetworkChangeActive()) {
            ((MobileState) this.mCurrentState).iconGroup = TelephonyIcons.CARRIER_NETWORK_CHANGE;
        }
        ((MobileState) this.mCurrentState).isRoaming = isRoamingHuawei(this.mServiceState);
        if (isEmergencyOnly() != ((MobileState) this.mCurrentState).isEmergency) {
            ((MobileState) this.mCurrentState).isEmergency = isEmergencyOnly();
            this.mNetworkController.recalculateEmergency();
        }
        if (!(!this.mNetworkNameDefault.equals(((MobileState) this.mCurrentState).networkName) || this.mServiceState == null || TextUtils.isEmpty(this.mServiceState.getOperatorAlphaShort()))) {
            ((MobileState) this.mCurrentState).networkName = this.mServiceState.getOperatorAlphaShort();
        }
        if (isDirty) {
            HwLog.i(this.mTag, "need to force refresh view");
            notifyListenersForce();
            return;
        }
        notifyListenersIfNecessary();
    }

    void setActivity(int activity) {
        boolean z;
        boolean z2 = true;
        MobileState mobileState = (MobileState) this.mCurrentState;
        if (activity == 3) {
            z = true;
        } else if (activity == 1) {
            z = true;
        } else {
            z = false;
        }
        mobileState.activityIn = z;
        mobileState = (MobileState) this.mCurrentState;
        if (!(activity == 3 || activity == 2)) {
            z2 = false;
        }
        mobileState.activityOut = z2;
        notifyListenersIfNecessary();
    }

    public void dump(PrintWriter pw) {
        super.dump(pw);
        pw.println("  mSubscription=" + this.mSubscriptionInfo + ",");
        pw.println("  mServiceState=" + this.mServiceState + ",");
        pw.println("  mSignalStrength=" + this.mSignalStrength + ",");
        pw.println("  mDataState=" + this.mDataState + ",");
        pw.println("  mDataNetType=" + this.mDataNetType + ",");
    }
}
