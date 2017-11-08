package com.android.internal.telephony;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.os.AsyncResult;
import android.os.BaseBundle;
import android.os.Binder;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.provider.SettingsEx.Systemex;
import android.provider.Telephony.BaseMmsColumns;
import android.provider.Telephony.CellBroadcasts;
import android.telephony.CarrierConfigManager;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Pair;
import android.util.TimeUtils;
import android.view.Display;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.cdma.HwCustPlusAndIddNddConvertUtils;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.gsm.HwCustGsmServiceStateTracker;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.RuimRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

public class ServiceStateTracker extends Handler {
    private static final /* synthetic */ int[] -com-android-internal-telephony-CommandsInterface$RadioStateSwitchesValues = null;
    private static final String ACTION_COMMFORCE = "android.intent.action.COMMFORCE";
    private static final String ACTION_RADIO_OFF = "android.intent.action.ACTION_RADIO_OFF";
    private static final String ACTION_TIMEZONE_SELECTION = "android.intent.action.ACTION_TIMEZONE_SELECTION";
    private static final boolean CLEAR_NITZ_WHEN_REG = SystemProperties.getBoolean("ro.config.clear_nitz_when_reg", true);
    public static final int CS_DISABLED = 1004;
    public static final int CS_EMERGENCY_ENABLED = 1006;
    public static final int CS_ENABLED = 1003;
    public static final int CS_NORMAL_ENABLED = 1005;
    public static final int CS_NOTIFICATION = 999;
    protected static final boolean DBG = true;
    public static final int DEFAULT_GPRS_CHECK_PERIOD_MILLIS = 60000;
    public static final String DEFAULT_MNC = "00";
    private static final int DELAY_TIME_TO_NOTIF_NITZ = 60000;
    private static final boolean ENABLE_DEMO = SystemProperties.getBoolean("ro.config.enable_demo", false);
    protected static final int EVENT_ALL_DATA_DISCONNECTED = 49;
    protected static final int EVENT_CDMA_PRL_VERSION_CHANGED = 40;
    protected static final int EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED = 39;
    protected static final int EVENT_CHANGE_IMS_STATE = 45;
    protected static final int EVENT_CHECK_REPORT_GPRS = 22;
    protected static final int EVENT_ERI_FILE_LOADED = 36;
    protected static final int EVENT_GET_AD_DONE = 1002;
    protected static final int EVENT_GET_CELL_INFO_LIST = 43;
    protected static final int EVENT_GET_LOC_DONE = 15;
    protected static final int EVENT_GET_PREFERRED_NETWORK_TYPE = 19;
    protected static final int EVENT_GET_SIGNAL_STRENGTH = 3;
    public static final int EVENT_ICC_CHANGED = 42;
    protected static final int EVENT_IMS_CAPABILITY_CHANGED = 48;
    protected static final int EVENT_IMS_STATE_CHANGED = 46;
    protected static final int EVENT_IMS_STATE_DONE = 47;
    protected static final int EVENT_LOCATION_UPDATES_ENABLED = 18;
    protected static final int EVENT_NETWORK_STATE_CHANGED = 2;
    private static final int EVENT_NITZ_CAPABILITY_NOTIFICATION = 100;
    protected static final int EVENT_NITZ_TIME = 11;
    protected static final int EVENT_NV_READY = 35;
    protected static final int EVENT_OTA_PROVISION_STATUS_CHANGE = 37;
    protected static final int EVENT_PHONE_TYPE_SWITCHED = 50;
    protected static final int EVENT_POLL_SIGNAL_STRENGTH = 10;
    protected static final int EVENT_POLL_STATE_CDMA_SUBSCRIPTION = 34;
    protected static final int EVENT_POLL_STATE_GPRS = 5;
    protected static final int EVENT_POLL_STATE_NETWORK_SELECTION_MODE = 14;
    protected static final int EVENT_POLL_STATE_OPERATOR = 6;
    protected static final int EVENT_POLL_STATE_REGISTRATION = 4;
    protected static final int EVENT_RADIO_AVAILABLE = 13;
    protected static final int EVENT_RADIO_ON = 41;
    protected static final int EVENT_RADIO_STATE_CHANGED = 1;
    protected static final int EVENT_RESET_PREFERRED_NETWORK_TYPE = 21;
    protected static final int EVENT_RESTRICTED_STATE_CHANGED = 23;
    protected static final int EVENT_RUIM_READY = 26;
    protected static final int EVENT_RUIM_RECORDS_LOADED = 27;
    protected static final int EVENT_SET_PREFERRED_NETWORK_TYPE = 20;
    protected static final int EVENT_SET_RADIO_POWER_OFF = 38;
    protected static final int EVENT_SIGNAL_STRENGTH_UPDATE = 12;
    protected static final int EVENT_SIM_READY = 17;
    protected static final int EVENT_SIM_RECORDS_LOADED = 16;
    protected static final int EVENT_UNSOL_CELL_INFO_LIST = 44;
    private static final String EXTRA_SHOW_WIFI = "showWifi";
    private static final String EXTRA_WIFI = "wifi";
    private static final boolean FEATURE_DELAY_UPDATE_SIGANL_STENGTH = SystemProperties.getBoolean("ro.config.delay_send_signal", true);
    private static final boolean FEATURE_RECOVER_AUTO_NETWORK_MODE = SystemProperties.getBoolean("ro.hwpp.recover_auto_mode", false);
    protected static final String[] GMT_COUNTRY_CODES = new String[]{"bf", "ci", "eh", "fo", "gb", "gh", "gm", "gn", "gw", "ie", "lr", "is", "ma", "ml", "mr", "pt", "sl", "sn", BaseMmsColumns.STATUS, "tg"};
    protected static final boolean HW_FAST_SET_RADIO_OFF = SystemProperties.getBoolean("ro.config.hw_fast_set_radio_off", false);
    private static final int HW_OPTA = SystemProperties.getInt("ro.config.hw_opta", -1);
    private static final int HW_OPTB = SystemProperties.getInt("ro.config.hw_optb", -1);
    private static final boolean IGNORE_GOOGLE_NON_ROAMING = true;
    public static final String INVALID_MCC = "000";
    public static final boolean ISDEMO;
    private static final long LAST_CELL_INFO_LIST_MAX_AGE_MS = 2000;
    private static final int MAX_NITZ_YEAR = 2037;
    private static final boolean MDOEM_WORK_MODE_IS_SRLTE = SystemProperties.getBoolean("ro.config.hw_srlte", false);
    public static final int MS_PER_HOUR = 3600000;
    public static final int NITZ_UPDATE_DIFF_DEFAULT = 2000;
    public static final int NITZ_UPDATE_SPACING_DEFAULT = 600000;
    public static final int OTASP_NEEDED = 2;
    public static final int OTASP_NOT_NEEDED = 3;
    public static final int OTASP_SIM_UNPROVISIONED = 5;
    public static final int OTASP_UNINITIALIZED = 0;
    public static final int OTASP_UNKNOWN = 1;
    private static final String PERMISSION_COMM_FORCE = "android.permission.COMM_FORCE";
    private static boolean PLUS_TRANFER_IN_MDOEM = HwModemCapability.isCapabilitySupport(2);
    private static final int POLL_PERIOD_MILLIS = 20000;
    protected static final String PROP_FORCE_ROAMING = "telephony.test.forceRoaming";
    protected static final int PS_CS = 1;
    public static final int PS_DISABLED = 1002;
    public static final int PS_ENABLED = 1001;
    public static final int PS_NOTIFICATION = 888;
    protected static final int PS_ONLY = 0;
    protected static final String REGISTRATION_DENIED_AUTH = "Authentication Failure";
    protected static final String REGISTRATION_DENIED_GEN = "General";
    protected static final boolean RESET_PROFILE = SystemProperties.getBoolean("ro.hwpp_reset_profile", false);
    protected static final String TIMEZONE_PROPERTY = "persist.sys.timezone";
    public static final String UNACTIVATED_MIN2_VALUE = "000000";
    public static final String UNACTIVATED_MIN_VALUE = "1111110111";
    protected static final boolean VDBG = false;
    public static final String WAKELOCK_TAG = "ServiceStateTracker";
    private static String data = null;
    protected static final boolean display_blank_ons = "true".equals(SystemProperties.get("ro.config.hw_no_display_ons", "false"));
    private String LOG_TAG = WAKELOCK_TAG;
    protected boolean isCurrent3GPsCsAllowed = true;
    private boolean mAlarmSwitch = false;
    protected RegistrantList mAttachedRegistrants = new RegistrantList();
    private ContentObserver mAutoTimeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Rlog.i(ServiceStateTracker.this.LOG_TAG, "Auto time state changed");
            ServiceStateTracker.this.revertToNitzTime();
        }
    };
    private ContentObserver mAutoTimeZoneObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Rlog.i(ServiceStateTracker.this.LOG_TAG, "Auto time zone state changed");
            if (ServiceStateTracker.this.updateTimeByNitz()) {
                ServiceStateTracker.this.revertToNitzTimeZone();
            } else {
                ServiceStateTracker.this.updateTimeZoneNoneNitz();
            }
        }
    };
    private RegistrantList mCdmaForSubscriptionInfoReadyRegistrants = new RegistrantList();
    private CdmaSubscriptionSourceManager mCdmaSSM;
    public CellLocation mCellLoc;
    protected CommandsInterface mCi;
    private ContentResolver mCr;
    private String mCurDataSpn = null;
    private String mCurPlmn = null;
    private boolean mCurShowPlmn = false;
    private boolean mCurShowSpn = false;
    private boolean mCurShowWifi = false;
    private String mCurSpn = null;
    private String mCurWifi = null;
    private String mCurrentCarrier = null;
    private int mCurrentOtaspMode = 0;
    private RegistrantList mDataRegStateOrRatChangedRegistrants = new RegistrantList();
    private boolean mDataRoaming = false;
    private RegistrantList mDataRoamingOffRegistrants = new RegistrantList();
    private RegistrantList mDataRoamingOnRegistrants = new RegistrantList();
    private Display mDefaultDisplay;
    private int mDefaultDisplayState = 0;
    private int mDefaultRoamingIndicator;
    private int mDelayUpdateGpsState = 0;
    protected boolean mDesiredPowerState;
    protected RegistrantList mDetachedRegistrants = new RegistrantList();
    private boolean mDeviceShuttingDown = false;
    private final DisplayListener mDisplayListener = new SstDisplayListener();
    private boolean mDoRecoveryMarker = false;
    protected boolean mDontPollSignalStrength = false;
    private boolean mEmergencyOnly = false;
    private TelephonyEventLog mEventLog;
    private boolean mGotCountryCode = false;
    private boolean mGsmRoaming = false;
    private HbpcdUtils mHbpcdUtils = null;
    private int[] mHomeNetworkId = null;
    private int[] mHomeSystemId = null;
    private HwCustGsmServiceStateTracker mHwCustGsmServiceStateTracker;
    protected IccRecords mIccRecords = null;
    private boolean mImsRegistered = false;
    private boolean mImsRegistrationOnOff = false;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.AIRPLANE_MODE")) {
                ServiceStateTracker.this.loge("intent: " + intent.getAction());
                ServiceStateTracker.this.mCurPlmn = null;
                ServiceStateTracker.this.updateSpnDisplay();
            }
            if (ServiceStateTracker.this.mPhone.isPhoneTypeGsm()) {
                if (intent.getAction().equals("android.intent.action.LOCALE_CHANGED")) {
                    ServiceStateTracker.this.updateSpnDisplay();
                } else if (intent.getAction().equals(ServiceStateTracker.ACTION_RADIO_OFF)) {
                    ServiceStateTracker.this.mAlarmSwitch = false;
                    ServiceStateTracker.this.powerOffRadioSafely(ServiceStateTracker.this.mPhone.mDcTracker);
                } else if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(intent.getAction()) || "android.net.wifi.WIFI_STATE_CHANGED".equals(intent.getAction()) || ServiceStateTracker.ACTION_COMMFORCE.equals(intent.getAction()) || "android.intent.action.HEADSET_PLUG".equals(intent.getAction()) || "android.intent.action.PHONE_STATE".equals(intent.getAction())) {
                    ServiceStateTracker.this.mPhone.updateReduceSARState();
                }
                return;
            }
            ServiceStateTracker.this.loge("Ignoring intent " + intent + " received on CDMA phone");
        }
    };
    private boolean mIsEriTextLoaded = false;
    private boolean mIsInPrl;
    private boolean mIsMinInfoReady = false;
    private boolean mIsSubscriptionFromRuim = false;
    private boolean mKeepNwSelManual = SystemProperties.getBoolean("ro.config.hw_keep_sel_manual", false);
    private List<CellInfo> mLastCellInfoList = null;
    private long mLastCellInfoListTime;
    long mLastReceivedNITZReferenceTime;
    private SignalStrength mLastSignalStrength = null;
    private CellIdentityLte mLasteCellIdentityLte = new CellIdentityLte();
    private int mMaxDataCalls = 1;
    private String mMdn;
    private String mMin;
    protected String mMlplVersion;
    protected String mMsplVersion;
    protected boolean mNeedFixZoneAfterNitz = false;
    private RegistrantList mNetworkAttachedRegistrants = new RegistrantList();
    private CellIdentityLte mNewCellIdentityLte = new CellIdentityLte();
    private CellLocation mNewCellLoc;
    private int mNewMaxDataCalls = 1;
    private int mNewReasonDataDenied = -1;
    protected ServiceState mNewSS;
    private int mNitzUpdateDiff = SystemProperties.getInt("ro.nitz_update_diff", NITZ_UPDATE_DIFF_DEFAULT);
    private int mNitzUpdateSpacing = SystemProperties.getInt("ro.nitz_update_spacing", NITZ_UPDATE_SPACING_DEFAULT);
    protected boolean mNitzUpdatedTime = false;
    private Notification mNotification;
    private final SstSubscriptionsChangedListener mOnSubscriptionsChangedListener = new SstSubscriptionsChangedListener();
    private boolean mPendingRadioPowerOffAfterDataOff = false;
    private int mPendingRadioPowerOffAfterDataOffTag = 0;
    private GsmCdmaPhone mPhone;
    protected int[] mPollingContext;
    private boolean mPowerOffDelayNeed = true;
    private int mPreferredNetworkType;
    private String mPrlVersion;
    private RegistrantList mPsRestrictDisabledRegistrants = new RegistrantList();
    private RegistrantList mPsRestrictEnabledRegistrants = new RegistrantList();
    private boolean mRadioOffByDoRecovery = false;
    private PendingIntent mRadioOffIntent = null;
    private int mReasonDataDenied = -1;
    private boolean mRecoverAutoSelectMode = false;
    private String mRegistrationDeniedReason;
    private int mRegistrationState = -1;
    private boolean mReportedGprsNoReg;
    public RestrictedState mRestrictedState;
    private int mRoamingIndicator;
    public ServiceState mSS;
    private long mSavedAtTime;
    private long mSavedTime;
    private String mSavedTimeZone;
    protected SignalStrength mSignalStrength;
    private boolean mSimCardsLoaded = false;
    private boolean mSpnUpdatePending = false;
    private boolean mStartedGprsRegCheck;
    private int mSubId = -1;
    private SubscriptionController mSubscriptionController;
    private SubscriptionManager mSubscriptionManager;
    protected UiccCardApplication mUiccApplcation = null;
    private UiccController mUiccController = null;
    protected boolean mVoiceCapable;
    private RegistrantList mVoiceRoamingOffRegistrants = new RegistrantList();
    private RegistrantList mVoiceRoamingOnRegistrants = new RegistrantList();
    private WakeLock mWakeLock = null;
    private boolean mWantContinuousLocationUpdates;
    private boolean mWantSingleLocationUpdate;
    private boolean mZoneDst;
    private int mZoneOffset;
    private long mZoneTime;

    private class CellInfoResult {
        List<CellInfo> list;
        Object lockObj;

        private CellInfoResult() {
            this.lockObj = new Object();
        }
    }

    public class SstDisplayListener implements DisplayListener {
        public void onDisplayAdded(int displayId) {
        }

        public void onDisplayRemoved(int displayId) {
        }

        public void onDisplayChanged(int displayId) {
            if (displayId == 0) {
                int oldState = ServiceStateTracker.this.mDefaultDisplayState;
                ServiceStateTracker.this.mDefaultDisplayState = ServiceStateTracker.this.mDefaultDisplay.getState();
                if (ServiceStateTracker.this.mDefaultDisplayState != oldState && ServiceStateTracker.this.mDefaultDisplayState == 2 && !ServiceStateTracker.this.mDontPollSignalStrength) {
                    ServiceStateTracker.this.sendMessage(ServiceStateTracker.this.obtainMessage(10));
                }
            }
        }
    }

    private class SstSubscriptionsChangedListener extends OnSubscriptionsChangedListener {
        public final AtomicInteger mPreviousSubId;

        private SstSubscriptionsChangedListener() {
            this.mPreviousSubId = new AtomicInteger(-1);
        }

        public void onSubscriptionsChanged() {
            ServiceStateTracker.this.log("SubscriptionListener.onSubscriptionInfoChanged");
            int subId = ServiceStateTracker.this.mPhone.getSubId();
            if (this.mPreviousSubId.getAndSet(subId) != subId && SubscriptionManager.isValidSubscriptionId(subId)) {
                Context context = ServiceStateTracker.this.mPhone.getContext();
                ServiceStateTracker.this.mPhone.notifyPhoneStateChanged();
                ServiceStateTracker.this.mPhone.notifyCallForwardingIndicator();
                ServiceStateTracker.this.mPhone.sendSubscriptionSettings(!context.getResources().getBoolean(17956963));
                ServiceStateTracker.this.mPhone.setSystemProperty("gsm.network.type", ServiceState.rilRadioTechnologyToString(ServiceStateTracker.this.mSS.getRilDataRadioTechnology()));
                if (ServiceStateTracker.this.mSpnUpdatePending) {
                    ServiceStateTracker.this.mSubscriptionController.setPlmnSpn(ServiceStateTracker.this.mPhone.getPhoneId(), ServiceStateTracker.this.mCurShowPlmn, ServiceStateTracker.this.mCurPlmn, ServiceStateTracker.this.mCurShowSpn, ServiceStateTracker.this.mCurSpn);
                    ServiceStateTracker.this.mSpnUpdatePending = false;
                }
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                String oldNetworkSelection = sp.getString(Phone.NETWORK_SELECTION_KEY, "");
                String oldNetworkSelectionName = sp.getString(Phone.NETWORK_SELECTION_NAME_KEY, "");
                String oldNetworkSelectionShort = sp.getString(Phone.NETWORK_SELECTION_SHORT_KEY, "");
                if (!(TextUtils.isEmpty(oldNetworkSelection) && TextUtils.isEmpty(oldNetworkSelectionName) && TextUtils.isEmpty(oldNetworkSelectionShort))) {
                    Editor editor = sp.edit();
                    editor.putString(Phone.NETWORK_SELECTION_KEY + subId, oldNetworkSelection);
                    editor.putString(Phone.NETWORK_SELECTION_NAME_KEY + subId, oldNetworkSelectionName);
                    editor.putString(Phone.NETWORK_SELECTION_SHORT_KEY + subId, oldNetworkSelectionShort);
                    editor.remove(Phone.NETWORK_SELECTION_KEY);
                    editor.remove(Phone.NETWORK_SELECTION_NAME_KEY);
                    editor.remove(Phone.NETWORK_SELECTION_SHORT_KEY);
                    editor.commit();
                }
                ServiceStateTracker.this.updateSpnDisplay();
            }
        }
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-CommandsInterface$RadioStateSwitchesValues() {
        if (-com-android-internal-telephony-CommandsInterface$RadioStateSwitchesValues != null) {
            return -com-android-internal-telephony-CommandsInterface$RadioStateSwitchesValues;
        }
        int[] iArr = new int[RadioState.values().length];
        try {
            iArr[RadioState.RADIO_OFF.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[RadioState.RADIO_ON.ordinal()] = 3;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[RadioState.RADIO_UNAVAILABLE.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        -com-android-internal-telephony-CommandsInterface$RadioStateSwitchesValues = iArr;
        return iArr;
    }

    static {
        boolean z;
        if (HW_OPTA == 735 && HW_OPTB == 156) {
            z = true;
        } else {
            z = ENABLE_DEMO;
        }
        ISDEMO = z;
    }

    private void updateTimeZoneNoneNitz() {
        if (Global.getInt(this.mPhone.getContext().getContentResolver(), "auto_time_zone", 0) != 0) {
            String iso = "";
            String mcc = "";
            String operatorNumeric = this.mSS.getOperatorNumeric();
            if (operatorNumeric == null || TextUtils.isEmpty(operatorNumeric)) {
                log("updateTimeZoneNoneNitz:operatorNumeric is null");
                return;
            }
            try {
                mcc = operatorNumeric.substring(0, 3);
                iso = MccTable.countryCodeForMcc(Integer.parseInt(mcc));
            } catch (NumberFormatException ex) {
                loge("updateTimeZoneNoneNitz: countryCodeForMcc error" + ex);
            } catch (StringIndexOutOfBoundsException ex2) {
                loge("updateTimeZoneNoneNitz: countryCodeForMcc error" + ex2);
            }
            if (!(iso == null || TextUtils.isEmpty(iso))) {
                TimeZone zone = null;
                ArrayList<TimeZone> uniqueZones = TimeUtils.getTimeZonesWithUniqueOffsets(iso);
                TimeZone defaultZones = getTimeZoneFromMcc(mcc);
                if (uniqueZones.size() == 1 || defaultZones != null) {
                    zone = (TimeZone) uniqueZones.get(0);
                    if (defaultZones != null) {
                        zone = defaultZones;
                        log("some countrys has more than two timezone, choose a default");
                    }
                } else {
                    log("uniqueZones more than one");
                }
                if (zone != null) {
                    String zoneId = zone.getID();
                    log("zoneId:" + zoneId);
                    setAndBroadcastNetworkSetTimeZone(zoneId);
                }
            }
        }
    }

    private boolean updateTimeByNitz() {
        return !this.mNitzUpdatedTime ? Global.getInt(this.mPhone.getContext().getContentResolver(), "airplane_mode_on", 0) > 0 : true;
    }

    public ServiceStateTracker(GsmCdmaPhone phone, CommandsInterface ci) {
        initOnce(phone, ci);
        updatePhoneType();
        initDisplay();
    }

    private void initDisplay() {
        DisplayManager dm = (DisplayManager) this.mPhone.getContext().getSystemService("display");
        dm.registerDisplayListener(this.mDisplayListener, null);
        this.mDefaultDisplay = dm.getDisplay(0);
    }

    private void initOnce(GsmCdmaPhone phone, CommandsInterface ci) {
        boolean z;
        this.mPhone = phone;
        this.LOG_TAG += "[SUB" + this.mPhone.getPhoneId() + "]";
        this.mCi = ci;
        this.mVoiceCapable = this.mPhone.getContext().getResources().getBoolean(17956956);
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, 42, null);
        this.mCi.setOnSignalStrengthUpdate(this, 12, null);
        this.mCi.registerForCellInfoList(this, 44, null);
        this.mSubscriptionController = SubscriptionController.getInstance();
        this.mSubscriptionManager = SubscriptionManager.from(phone.getContext());
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        this.mCi.registerForImsNetworkStateChanged(this, EVENT_IMS_STATE_CHANGED, null);
        this.mWakeLock = ((PowerManager) phone.getContext().getSystemService("power")).newWakeLock(1, WAKELOCK_TAG);
        this.mCi.registerForRadioStateChanged(this, 1, null);
        this.mCi.registerForVoiceNetworkStateChanged(this, 2, null);
        this.mCi.setOnNITZTime(this, 11, null);
        this.mCr = phone.getContext().getContentResolver();
        int airplaneMode = Global.getInt(this.mCr, "airplane_mode_on", 0);
        if (Global.getInt(this.mCr, "enable_cellular_on_boot", 1) <= 0 || airplaneMode > 0) {
            z = false;
        } else {
            z = true;
        }
        this.mDesiredPowerState = z;
        this.mCr.registerContentObserver(Global.getUriFor("auto_time"), true, this.mAutoTimeObserver);
        this.mCr.registerContentObserver(Global.getUriFor("auto_time_zone"), true, this.mAutoTimeZoneObserver);
        setSignalStrengthDefaultValues();
        data = Systemex.getString(this.mCr, "enable_get_location");
        this.mCi.getSignalStrength(obtainMessage(3));
        Context context = this.mPhone.getContext();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        context.registerReceiver(this.mIntentReceiver, filter);
        filter = new IntentFilter();
        filter.addAction(ACTION_RADIO_OFF);
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.intent.action.HEADSET_PLUG");
        filter.addAction("android.intent.action.PHONE_STATE");
        filter.addAction("android.intent.action.AIRPLANE_MODE");
        context.registerReceiver(this.mIntentReceiver, filter);
        filter = new IntentFilter();
        filter.addAction(ACTION_COMMFORCE);
        context.registerReceiver(this.mIntentReceiver, filter, PERMISSION_COMM_FORCE, null);
        this.mHwCustGsmServiceStateTracker = (HwCustGsmServiceStateTracker) HwCustUtils.createObj(HwCustGsmServiceStateTracker.class, new Object[]{this.mPhone});
        this.mEventLog = new TelephonyEventLog(this.mPhone.getPhoneId());
        this.mPhone.notifyOtaspChanged(0);
    }

    public void updatePhoneType() {
        this.mSS = new ServiceState();
        this.mNewSS = new ServiceState();
        this.mLastCellInfoListTime = 0;
        this.mLastCellInfoList = null;
        this.mSignalStrength = new SignalStrength();
        this.mRestrictedState = new RestrictedState();
        this.mStartedGprsRegCheck = false;
        this.mReportedGprsNoReg = false;
        this.mMdn = null;
        this.mMin = null;
        this.mPrlVersion = null;
        this.mIsMinInfoReady = false;
        this.mNitzUpdatedTime = false;
        cancelPollState();
        if (this.mPhone.isPhoneTypeGsm()) {
            if (this.mCdmaSSM != null) {
                this.mCdmaSSM.dispose(this);
            }
            this.mCi.unregisterForCdmaPrlChanged(this);
            this.mPhone.unregisterForEriFileLoaded(this);
            this.mCi.unregisterForCdmaOtaProvision(this);
            this.mPhone.unregisterForSimRecordsLoaded(this);
            this.mCellLoc = new GsmCellLocation();
            this.mNewCellLoc = new GsmCellLocation();
            this.mCi.registerForAvailable(this, 13, null);
            this.mCi.setOnRestrictedStateChanged(this, 23, null);
        } else {
            if (HwModemCapability.isCapabilitySupport(9)) {
                this.mCurPlmn = null;
            }
            this.mCi.unregisterForAvailable(this);
            this.mCi.unSetOnRestrictedStateChanged(this);
            if (this.mPhone.isPhoneTypeCdmaLte()) {
                this.mPhone.registerForSimRecordsLoaded(this, 16, null);
            }
            this.mCellLoc = new CdmaCellLocation();
            this.mNewCellLoc = new CdmaCellLocation();
            this.mCdmaSSM = CdmaSubscriptionSourceManager.getInstance(this.mPhone.getContext(), this.mCi, this, 39, null);
            this.mIsSubscriptionFromRuim = this.mCdmaSSM.getCdmaSubscriptionSource() == 0;
            this.mCi.registerForCdmaPrlChanged(this, 40, null);
            this.mPhone.registerForEriFileLoaded(this, 36, null);
            this.mCi.registerForCdmaOtaProvision(this, 37, null);
            this.mHbpcdUtils = new HbpcdUtils(this.mPhone.getContext());
            updateOtaspState();
        }
        onUpdateIccAvailability();
        this.mPhone.setSystemProperty("gsm.network.type", ServiceState.rilRadioTechnologyToString(0));
        this.mCi.getSignalStrength(obtainMessage(3));
        sendMessage(obtainMessage(50));
    }

    public void requestShutdown() {
        if (!this.mDeviceShuttingDown) {
            this.mDeviceShuttingDown = true;
            this.mDesiredPowerState = false;
            setPowerStateToDesired();
        }
    }

    public void dispose() {
        this.mCi.unSetOnSignalStrengthUpdate(this);
        this.mUiccController.unregisterForIccChanged(this);
        this.mCi.unregisterForCellInfoList(this);
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        this.mCi.unregisterForImsNetworkStateChanged(this);
        if (this.mUiccApplcation != null) {
            this.mUiccApplcation.unregisterForGetAdDone(this);
        }
        unregisterForRuimEvents();
        HwTelephonyFactory.getHwNetworkManager().dispose(this);
    }

    public boolean getDesiredPowerState() {
        return this.mDesiredPowerState;
    }

    public void setDesiredPowerState(boolean dps) {
        log("setDesiredPowerState, dps = " + dps);
        this.mDesiredPowerState = dps;
    }

    protected boolean notifySignalStrength() {
        if (this.mSignalStrength.equals(this.mLastSignalStrength)) {
            return false;
        }
        try {
            this.mPhone.notifySignalStrength();
            return true;
        } catch (NullPointerException ex) {
            loge("updateSignalStrength() Phone already destroyed: " + ex + "SignalStrength not notified");
            return false;
        }
    }

    protected void notifyDataRegStateRilRadioTechnologyChanged() {
        int rat = this.mSS.getRilDataRadioTechnology();
        int drs = this.mSS.getDataRegState();
        log("notifyDataRegStateRilRadioTechnologyChanged: drs=" + drs + " rat=" + rat);
        this.mPhone.setSystemProperty("gsm.network.type", ServiceState.rilRadioTechnologyToString(rat));
        this.mDataRegStateOrRatChangedRegistrants.notifyResult(new Pair(Integer.valueOf(drs), Integer.valueOf(rat)));
    }

    protected void useDataRegStateForDataOnlyDevices() {
        if (!this.mVoiceCapable) {
            log("useDataRegStateForDataOnlyDevice: VoiceRegState=" + this.mNewSS.getVoiceRegState() + " DataRegState=" + this.mNewSS.getDataRegState());
            if (this.mNewSS.getDataRegState() != 1) {
                this.mNewSS.setVoiceRegState(this.mNewSS.getDataRegState());
            }
        }
    }

    protected void updatePhoneObject() {
        boolean isRegistered = true;
        if (this.mPhone.getContext().getResources().getBoolean(17957018)) {
            if (!(this.mSS.getVoiceRegState() == 0 || this.mSS.getVoiceRegState() == 2)) {
                isRegistered = false;
            }
            if (isRegistered) {
                this.mPhone.updatePhoneObject(this.mSS.getRilVoiceRadioTechnology());
            } else {
                log("updatePhoneObject: Ignore update");
            }
        }
    }

    public void registerForVoiceRoamingOn(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mVoiceRoamingOnRegistrants.add(r);
        if (this.mSS.getVoiceRoaming()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForVoiceRoamingOn(Handler h) {
        this.mVoiceRoamingOnRegistrants.remove(h);
    }

    public void registerForVoiceRoamingOff(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mVoiceRoamingOffRegistrants.add(r);
        if (!this.mSS.getVoiceRoaming()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForVoiceRoamingOff(Handler h) {
        this.mVoiceRoamingOffRegistrants.remove(h);
    }

    public void registerForDataRoamingOn(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mDataRoamingOnRegistrants.add(r);
        if (this.mSS.getDataRoaming()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForDataRoamingOn(Handler h) {
        this.mDataRoamingOnRegistrants.remove(h);
    }

    public void registerForDataRoamingOff(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mDataRoamingOffRegistrants.add(r);
        if (!this.mSS.getDataRoaming()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForDataRoamingOff(Handler h) {
        this.mDataRoamingOffRegistrants.remove(h);
    }

    public void reRegisterNetwork(Message onComplete) {
        int i = 1;
        if (HwModemCapability.isCapabilitySupport(7) && this.mPhone.isPhoneTypeGsm()) {
            int i2;
            log("modem support rettach, rettach");
            CommandsInterface commandsInterface = this.mCi;
            if (14 == this.mSS.getRilDataRadioTechnology()) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            commandsInterface.dataConnectionDetach(i2, null);
            CommandsInterface commandsInterface2 = this.mCi;
            if (14 != this.mSS.getRilDataRadioTechnology()) {
                i = 0;
            }
            commandsInterface2.dataConnectionAttach(i, null);
            return;
        }
        log("modem not support rettach, reRegisterNetwork");
        this.mCi.getPreferredNetworkType(obtainMessage(19, onComplete));
    }

    public void setRadioPower(boolean power) {
        this.mDesiredPowerState = power;
        setPowerStateToDesired();
    }

    protected void setPowerStateToDesired(boolean power, Message msg) {
        if (Global.getInt(this.mPhone.getContext().getContentResolver(), "airplane_mode_on", 0) <= 0) {
            this.mDesiredPowerState = power;
        }
        log("mDesiredPowerState = " + this.mDesiredPowerState);
        log("setPowerStateToDesired: callers = " + Debug.getCallers(6));
        this.mCi.setRadioPower(this.mDesiredPowerState, msg);
    }

    public void setRadioPower(boolean power, Message msg) {
        setPowerStateToDesired(power, msg);
    }

    public void enableSingleLocationUpdate() {
        String appName = null;
        if (!(this.mPhone == null || this.mPhone.getContext() == null || this.mPhone.getContext().getPackageManager() == null)) {
            appName = this.mPhone.getContext().getPackageManager().getNameForUid(Binder.getCallingUid());
        }
        boolean containPackage = isContainPackage(data, appName);
        if ((!HwTelephonyFactory.getHwNetworkManager().isCustScreenOff(this.mPhone) || containPackage) && !this.mWantSingleLocationUpdate && !this.mWantContinuousLocationUpdates) {
            this.mWantSingleLocationUpdate = true;
            this.mCi.setLocationUpdates(true, obtainMessage(18));
        }
    }

    public void enableLocationUpdates() {
        if (!HwTelephonyFactory.getHwNetworkManager().isCustScreenOff(this.mPhone) && !this.mWantSingleLocationUpdate && !this.mWantContinuousLocationUpdates) {
            this.mWantContinuousLocationUpdates = true;
            this.mCi.setLocationUpdates(true, obtainMessage(18));
        }
    }

    protected void disableSingleLocationUpdate() {
        this.mWantSingleLocationUpdate = false;
        if (!this.mWantSingleLocationUpdate && !this.mWantContinuousLocationUpdates) {
            this.mCi.setLocationUpdates(false, null);
        }
    }

    public void disableLocationUpdates() {
        this.mWantContinuousLocationUpdates = false;
        if (!this.mWantSingleLocationUpdate && !this.mWantContinuousLocationUpdates) {
            this.mCi.setLocationUpdates(false, null);
        }
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        switch (msg.what) {
            case 1:
            case 50:
                if (!this.mDesiredPowerState || !HwTelephonyFactory.getHwUiccManager().uiccHwdsdsNeedSetActiveMode() || this.mDoRecoveryMarker) {
                    setDoRecoveryMarker(false);
                    if (!this.mPhone.isPhoneTypeGsm() && this.mCi.getRadioState() == RadioState.RADIO_ON) {
                        handleCdmaSubscriptionSource(this.mCdmaSSM.getCdmaSubscriptionSource());
                        queueNextSignalStrengthPoll();
                    }
                    setPowerStateToDesired();
                    pollState();
                    break;
                }
                log("CdmaSerive/Gsmserive tracker need wait SetActiveMode ");
                break;
                break;
            case 2:
                modemTriggeredPollState();
                break;
            case 3:
                if (this.mCi.getRadioState().isOn()) {
                    onSignalStrengthResult((AsyncResult) msg.obj);
                    queueNextSignalStrengthPoll();
                    break;
                }
                return;
            case 4:
            case 5:
            case 6:
                handlePollStateResult(msg.what, (AsyncResult) msg.obj);
                break;
            case 10:
                this.mCi.getSignalStrength(obtainMessage(3));
                break;
            case 11:
                if (HwTelephonyFactory.getHwNetworkManager().needGsmUpdateNITZTime(this, this.mPhone)) {
                    ar = (AsyncResult) msg.obj;
                    String nitzString = ((Object[]) ar.result)[0];
                    long nitzReceiveTime = ((Long) ((Object[]) ar.result)[1]).longValue();
                    this.mLastReceivedNITZReferenceTime = nitzReceiveTime;
                    setTimeFromNITZString(nitzString, nitzReceiveTime);
                    break;
                }
                return;
            case 12:
                ar = (AsyncResult) msg.obj;
                this.mDontPollSignalStrength = true;
                onSignalStrengthResult(ar);
                break;
            case 13:
                break;
            case 14:
                log("EVENT_POLL_STATE_NETWORK_SELECTION_MODE");
                ar = (AsyncResult) msg.obj;
                if (!this.mPhone.isPhoneTypeGsm()) {
                    if (ar.exception == null && ar.result != null) {
                        if (ar.result[0] == 1) {
                            this.mPhone.setNetworkSelectionModeAutomatic(null);
                            break;
                        }
                    }
                    log("Unable to getNetworkSelectionMode");
                    break;
                }
                handlePollStateResult(msg.what, ar);
                break;
                break;
            case 15:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    String[] states = (String[]) ar.result;
                    if (this.mPhone.isPhoneTypeGsm()) {
                        int lac = -1;
                        int cid = -1;
                        if (states.length >= 3) {
                            try {
                                if (states[1] != null && states[1].length() > 0) {
                                    lac = Integer.parseInt(states[1], 16);
                                }
                                if (states[2] != null && states[2].length() > 0) {
                                    cid = Integer.parseInt(states[2], 16);
                                }
                            } catch (NumberFormatException ex) {
                                Rlog.w(this.LOG_TAG, "error parsing location: " + ex);
                            }
                        }
                        ((GsmCellLocation) this.mCellLoc).setLacAndCid(lac, cid);
                    } else {
                        int baseStationId = -1;
                        int baseStationLatitude = Integer.MAX_VALUE;
                        int baseStationLongitude = Integer.MAX_VALUE;
                        int systemId = -1;
                        int networkId = -1;
                        if (states.length > 9) {
                            try {
                                if (states[4] != null) {
                                    baseStationId = Integer.parseInt(states[4]);
                                }
                                if (states[5] != null) {
                                    baseStationLatitude = Integer.parseInt(states[5]);
                                }
                                if (states[6] != null) {
                                    baseStationLongitude = Integer.parseInt(states[6]);
                                }
                                if (baseStationLatitude == 0 && baseStationLongitude == 0) {
                                    baseStationLatitude = Integer.MAX_VALUE;
                                    baseStationLongitude = Integer.MAX_VALUE;
                                }
                                if (states[8] != null) {
                                    systemId = Integer.parseInt(states[8]);
                                }
                                if (states[9] != null) {
                                    networkId = Integer.parseInt(states[9]);
                                }
                            } catch (NumberFormatException ex2) {
                                loge("error parsing cell location data: " + ex2);
                            }
                        }
                        ((CdmaCellLocation) this.mCellLoc).setCellLocationData(baseStationId, baseStationLatitude, baseStationLongitude, systemId, networkId);
                    }
                    this.mPhone.notifyLocationChanged();
                }
                disableSingleLocationUpdate();
                break;
            case 16:
                log("EVENT_SIM_RECORDS_LOADED: what=" + msg.what);
                this.mSimCardsLoaded = true;
                updatePhoneObject();
                updateOtaspState();
                if (this.mPhone.isPhoneTypeGsm()) {
                    updateSpnDisplay();
                    if (this.mHwCustGsmServiceStateTracker != null) {
                        this.mHwCustGsmServiceStateTracker.updateRomingVoicemailNumber(this.mSS);
                        break;
                    }
                }
                break;
            case 17:
                this.mOnSubscriptionsChangedListener.mPreviousSubId.set(-1);
                this.mSimCardsLoaded = false;
                log("skip setPreferredNetworkType when EVENT_SIM_READY");
                boolean skipRestoringSelection = this.mPhone.getContext().getResources().getBoolean(17956963);
                if (FEATURE_RECOVER_AUTO_NETWORK_MODE) {
                    log("Feature recover network mode automatic is on..");
                    this.mRecoverAutoSelectMode = true;
                } else if (!skipRestoringSelection) {
                    if (!HwModemCapability.isCapabilitySupport(4) || this.mKeepNwSelManual) {
                        this.mPhone.restoreSavedNetworkSelection(null);
                    } else {
                        log("Modem can select network auto with manual mode");
                    }
                }
                pollState();
                queueNextSignalStrengthPoll();
                break;
            case 18:
                if (((AsyncResult) msg.obj).exception == null) {
                    this.mCi.getVoiceRegistrationState(obtainMessage(15, null));
                    break;
                }
                break;
            case 19:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    this.mPreferredNetworkType = ((int[]) ar.result)[0];
                } else {
                    this.mPreferredNetworkType = 7;
                }
                this.mCi.setPreferredNetworkType(7, obtainMessage(20, ar.userObj));
                break;
            case 20:
                this.mCi.setPreferredNetworkType(this.mPreferredNetworkType, obtainMessage(21, ((AsyncResult) msg.obj).userObj));
                break;
            case 21:
                ar = (AsyncResult) msg.obj;
                if (ar.userObj != null) {
                    AsyncResult.forMessage((Message) ar.userObj).exception = ar.exception;
                    ((Message) ar.userObj).sendToTarget();
                    break;
                }
                break;
            case 22:
                if (!(!this.mPhone.isPhoneTypeGsm() || this.mSS == null || isGprsConsistent(this.mSS.getDataRegState(), this.mSS.getVoiceRegState()))) {
                    GsmCellLocation loc = (GsmCellLocation) this.mPhone.getCellLocation();
                    String[] strArr = new Object[2];
                    strArr[0] = this.mSS.getOperatorNumeric();
                    strArr[1] = Integer.valueOf(loc != null ? loc.getCid() : -1);
                    EventLog.writeEvent(EventLogTags.DATA_NETWORK_REGISTRATION_FAIL, strArr);
                    this.mReportedGprsNoReg = true;
                }
                this.mStartedGprsRegCheck = false;
                break;
            case 23:
                if (this.mPhone.isPhoneTypeGsm()) {
                    onRestrictedStateChanged((AsyncResult) msg.obj);
                    break;
                }
                break;
            case 26:
                if (this.mPhone.getLteOnCdmaMode() == 1) {
                    log("Receive EVENT_RUIM_READY");
                    pollState();
                } else {
                    log("Receive EVENT_RUIM_READY and Send Request getCDMASubscription.");
                    getSubscriptionInfoAndStartPollingThreads();
                }
                this.mCi.getNetworkSelectionMode(obtainMessage(14));
                break;
            case 27:
                if (!this.mPhone.isPhoneTypeGsm()) {
                    log("EVENT_RUIM_RECORDS_LOADED: what=" + msg.what);
                    updatePhoneObject();
                    if (!this.mPhone.isPhoneTypeCdma()) {
                        RuimRecords ruim = (RuimRecords) this.mIccRecords;
                        if (ruim != null) {
                            if (ruim.isProvisioned()) {
                                this.mMdn = ruim.getMdn();
                                this.mMin = ruim.getMin();
                                parseSidNid(ruim.getSid(), ruim.getNid());
                                String prlVersion = ruim.getPrlVersion();
                                if (prlVersion != null) {
                                    prlVersion = prlVersion.trim();
                                    if (!("".equals(prlVersion) || "65535".equals(prlVersion))) {
                                        this.mPrlVersion = prlVersion;
                                        SystemProperties.set("persist.radio.hwprlversion", this.mPrlVersion);
                                    }
                                }
                                this.mIsMinInfoReady = true;
                            }
                            updateOtaspState();
                            notifyCdmaSubscriptionInfoReady();
                        }
                        pollState();
                        break;
                    }
                    updateSpnDisplay();
                    break;
                }
                break;
            case 34:
                if (!this.mPhone.isPhoneTypeGsm()) {
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        String[] cdmaSubscription = ar.result;
                        if (cdmaSubscription != null && cdmaSubscription.length >= 5) {
                            if (cdmaSubscription[0] != null) {
                                this.mMdn = cdmaSubscription[0];
                            }
                            parseSidNid(cdmaSubscription[1], cdmaSubscription[2]);
                            if (cdmaSubscription[3] != null) {
                                this.mMin = cdmaSubscription[3];
                            }
                            if (cdmaSubscription[4] != null) {
                                this.mPrlVersion = cdmaSubscription[4];
                                SystemProperties.set("persist.radio.hwprlversion", this.mPrlVersion);
                            }
                            log("GET_CDMA_SUBSCRIPTION: MDN=" + this.mMdn);
                            if (cdmaSubscription.length >= 7) {
                                log("GET_CDMA_SUBSCRIPTION: mMlplVersion=" + this.mMlplVersion + " mMsplVersion=" + this.mMsplVersion);
                                this.mMlplVersion = cdmaSubscription[5];
                                this.mMsplVersion = cdmaSubscription[6];
                            }
                            this.mIsMinInfoReady = true;
                            updateOtaspState();
                            notifyCdmaSubscriptionInfoReady();
                            if (!this.mIsSubscriptionFromRuim && this.mIccRecords != null) {
                                log("GET_CDMA_SUBSCRIPTION set imsi in mIccRecords");
                                this.mIccRecords.setImsi(getImsi());
                                break;
                            }
                            log("GET_CDMA_SUBSCRIPTION either mIccRecords is null or NV type device - not setting Imsi in mIccRecords");
                            break;
                        }
                        log("GET_CDMA_SUBSCRIPTION: error parsing cdmaSubscription params num=" + cdmaSubscription.length);
                        break;
                    }
                }
                break;
            case 35:
                updatePhoneObject();
                this.mCi.getNetworkSelectionMode(obtainMessage(14));
                getSubscriptionInfoAndStartPollingThreads();
                break;
            case 36:
                log("ERI file has been loaded, repolling.");
                pollState();
                break;
            case 37:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    int otaStatus = ((int[]) ar.result)[0];
                    if (otaStatus == 8 || otaStatus == 10) {
                        log("EVENT_OTA_PROVISION_STATUS_CHANGE: Complete, Reload MDN");
                        this.mCi.getCDMASubscription(obtainMessage(34));
                        break;
                    }
                }
                break;
            case 38:
                synchronized (this) {
                    if (this.mPendingRadioPowerOffAfterDataOff && msg.arg1 == this.mPendingRadioPowerOffAfterDataOffTag) {
                        log("EVENT_SET_RADIO_OFF, turn radio off now.");
                        hangupAndPowerOff();
                        this.mPendingRadioPowerOffAfterDataOffTag++;
                        this.mPendingRadioPowerOffAfterDataOff = false;
                    } else {
                        log("EVENT_SET_RADIO_OFF is stale arg1=" + msg.arg1 + "!= tag=" + this.mPendingRadioPowerOffAfterDataOffTag);
                    }
                    releaseWakeLock();
                    break;
                }
                break;
            case 39:
                handleCdmaSubscriptionSource(this.mCdmaSSM.getCdmaSubscriptionSource());
                break;
            case 40:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    this.mPrlVersion = Integer.toString(((int[]) ar.result)[0]);
                    SystemProperties.set("persist.radio.hwprlversion", this.mPrlVersion);
                    break;
                }
                break;
            case 42:
                onUpdateIccAvailability();
                break;
            case 43:
                ar = msg.obj;
                CellInfoResult result = ar.userObj;
                synchronized (result.lockObj) {
                    if (ar.exception != null) {
                        log("EVENT_GET_CELL_INFO_LIST: error ret null, e=" + ar.exception);
                        result.list = null;
                    } else {
                        result.list = (List) ar.result;
                    }
                    this.mLastCellInfoListTime = SystemClock.elapsedRealtime();
                    this.mLastCellInfoList = result.list;
                    result.lockObj.notify();
                }
                break;
            case 44:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    List<CellInfo> list = ar.result;
                    this.mLastCellInfoListTime = SystemClock.elapsedRealtime();
                    this.mLastCellInfoList = list;
                    this.mPhone.notifyCellInfo(list);
                    break;
                }
                log("EVENT_UNSOL_CELL_INFO_LIST: error ignoring, e=" + ar.exception);
                break;
            case EVENT_CHANGE_IMS_STATE /*45*/:
                log("EVENT_CHANGE_IMS_STATE:");
                setPowerStateToDesired();
                break;
            case EVENT_IMS_STATE_CHANGED /*46*/:
                this.mCi.getImsRegistrationState(obtainMessage(47));
                break;
            case 47:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    this.mImsRegistered = ((int[]) ar.result)[0] == 1;
                    break;
                }
                break;
            case EVENT_IMS_CAPABILITY_CHANGED /*48*/:
                log("EVENT_IMS_CAPABILITY_CHANGED");
                updateSpnDisplay();
                break;
            case 49:
                ProxyController.getInstance().unregisterForAllDataDisconnected(SubscriptionManager.getDefaultDataSubscriptionId(), this);
                synchronized (this) {
                    if (!this.mPendingRadioPowerOffAfterDataOff) {
                        log("EVENT_ALL_DATA_DISCONNECTED is stale");
                        break;
                    }
                    log("EVENT_ALL_DATA_DISCONNECTED, turn radio off now.");
                    hangupAndPowerOff();
                    this.mPendingRadioPowerOffAfterDataOff = false;
                    break;
                }
                break;
            case 100:
                log("[settimezone]EVENT_NITZ_CAPABILITY_NOTIFICATION");
                sendTimeZoneSelectionNotification();
                break;
            case 1002:
                if (this.mPollingContext[0] != 0) {
                    log("EVENT_GET_AD_DONE pollState working ,no need do again");
                    break;
                }
                log("EVENT_GET_AD_DONE pollState ");
                pollState();
                break;
            default:
                log("Unhandled message with number: " + msg.what);
                break;
        }
    }

    public String getRplmn() {
        if (this.mPhone.isPhoneTypeGsm()) {
            return HwTelephonyFactory.getHwNetworkManager().getGsmRplmn(this, this.mPhone);
        }
        return "";
    }

    public void setCurrent3GPsCsAllowed(boolean allowed) {
        log("setCurrent3GPsCsAllowed:" + allowed);
        this.isCurrent3GPsCsAllowed = allowed;
    }

    protected boolean isSidsAllZeros() {
        if (this.mHomeSystemId != null) {
            for (int i : this.mHomeSystemId) {
                if (i != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isHomeSid(int sid) {
        if (this.mHomeSystemId != null) {
            for (int i : this.mHomeSystemId) {
                if (sid == i) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getMdnNumber() {
        return this.mMdn;
    }

    public String getCdmaMin() {
        return this.mMin;
    }

    public String getPrlVersion() {
        int subId = this.mPhone.getSubId();
        int simCardState = TelephonyManager.getDefault().getSimState(subId);
        if (5 == simCardState) {
            this.mPrlVersion = this.mCi.getHwPrlVersion();
        } else {
            this.mPrlVersion = ProxyController.MODEM_0;
        }
        Rlog.d(this.LOG_TAG, "getPrlVersion: prlVersion=" + this.mPrlVersion + ", subid=" + subId + ", simState=" + simCardState);
        return this.mPrlVersion;
    }

    public String getMlplVersion() {
        String realMlplVersion = null;
        int subId = this.mPhone.getSubId();
        if (HwModemCapability.isCapabilitySupport(9) && 5 == TelephonyManager.getDefault().getSimState(subId)) {
            realMlplVersion = this.mCi.getHwCDMAMlplVersion();
        }
        if (realMlplVersion == null) {
            realMlplVersion = this.mMlplVersion;
        }
        Rlog.d(this.LOG_TAG, "getMlplVersion: mlplVersion=" + realMlplVersion);
        return realMlplVersion;
    }

    public String getMsplVersion() {
        String realMsplVersion = null;
        int subId = this.mPhone.getSubId();
        if (HwModemCapability.isCapabilitySupport(9) && 5 == TelephonyManager.getDefault().getSimState(subId)) {
            realMsplVersion = this.mCi.getHwCDMAMsplVersion();
        }
        if (realMsplVersion == null) {
            realMsplVersion = this.mMsplVersion;
        }
        Rlog.d(this.LOG_TAG, "getMsplVersion: msplVersion=" + realMsplVersion);
        return realMsplVersion;
    }

    public String getImsi() {
        String operatorNumeric = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNumericForPhone(this.mPhone.getPhoneId());
        if (TextUtils.isEmpty(operatorNumeric) || getCdmaMin() == null) {
            return null;
        }
        return operatorNumeric + getCdmaMin();
    }

    public boolean isMinInfoReady() {
        return this.mIsMinInfoReady;
    }

    public int getOtasp() {
        if (this.mPhone.isPhoneTypeGsm()) {
            log("getOtasp: otasp not needed for GSM");
            return 3;
        } else if (this.mIsSubscriptionFromRuim) {
            return 3;
        } else {
            int provisioningState;
            if (this.mMin == null || this.mMin.length() < 6) {
                log("getOtasp: bad mMin='" + this.mMin + "'");
                provisioningState = 1;
            } else if (this.mMin.equals(UNACTIVATED_MIN_VALUE) || this.mMin.substring(0, 6).equals(UNACTIVATED_MIN2_VALUE) || SystemProperties.getBoolean("test_cdma_setup", false)) {
                provisioningState = 2;
            } else {
                provisioningState = 3;
            }
            log("getOtasp: state=" + provisioningState);
            return provisioningState;
        }
    }

    protected void parseSidNid(String sidStr, String nidStr) {
        int i;
        if (sidStr != null) {
            String[] sid = sidStr.split(",");
            this.mHomeSystemId = new int[sid.length];
            for (i = 0; i < sid.length; i++) {
                try {
                    this.mHomeSystemId[i] = Integer.parseInt(sid[i]);
                } catch (NumberFormatException ex) {
                    loge("error parsing system id: " + ex);
                }
            }
        }
        log("CDMA_SUBSCRIPTION: SID=" + sidStr);
        if (nidStr != null) {
            String[] nid = nidStr.split(",");
            this.mHomeNetworkId = new int[nid.length];
            for (i = 0; i < nid.length; i++) {
                try {
                    this.mHomeNetworkId[i] = Integer.parseInt(nid[i]);
                } catch (NumberFormatException ex2) {
                    loge("CDMA_SUBSCRIPTION: error parsing network id: " + ex2);
                }
            }
        }
        log("CDMA_SUBSCRIPTION: NID=" + nidStr);
    }

    protected void updateOtaspState() {
        int otaspMode = getOtasp();
        int oldOtaspMode = this.mCurrentOtaspMode;
        this.mCurrentOtaspMode = otaspMode;
        if (oldOtaspMode != this.mCurrentOtaspMode) {
            log("updateOtaspState: call notifyOtaspChanged old otaspMode=" + oldOtaspMode + " new otaspMode=" + this.mCurrentOtaspMode);
            this.mPhone.notifyOtaspChanged(this.mCurrentOtaspMode);
        }
    }

    protected Phone getPhone() {
        return this.mPhone;
    }

    protected void handlePollStateResult(int what, AsyncResult ar) {
        if (ar.userObj == this.mPollingContext) {
            if (ar.exception != null) {
                Error err = null;
                if (ar.exception instanceof CommandException) {
                    err = ((CommandException) ar.exception).getCommandError();
                }
                if (err == Error.RADIO_NOT_AVAILABLE) {
                    cancelPollState();
                    return;
                } else if (err != Error.OP_NOT_ALLOWED_BEFORE_REG_NW) {
                    loge("RIL implementation has returned an error where it must succeed" + ar.exception);
                }
            } else {
                try {
                    handlePollStateResultMessage(what, ar);
                } catch (RuntimeException ex) {
                    loge("Exception while polling service state. Probably malformed RIL response." + ex);
                }
            }
            int[] iArr = this.mPollingContext;
            iArr[0] = iArr[0] - 1;
            if (this.mPollingContext[0] == 0) {
                if (this.mPhone.isPhoneTypeGsm()) {
                    updateRoamingState();
                    this.mNewSS.setEmergencyOnly(this.mEmergencyOnly);
                } else {
                    boolean namMatch = false;
                    if (!isSidsAllZeros() && isHomeSid(this.mNewSS.getSystemId())) {
                        namMatch = true;
                    }
                    if (this.mIsSubscriptionFromRuim) {
                        this.mNewSS.setVoiceRoaming(isRoamingBetweenOperators(this.mNewSS.getVoiceRoaming(), this.mNewSS));
                    }
                    boolean isVoiceInService = this.mNewSS.getVoiceRegState() == 0;
                    int dataRegType = this.mNewSS.getRilDataRadioTechnology();
                    if (isVoiceInService && ServiceState.isCdma(dataRegType)) {
                        this.mNewSS.setDataRoaming(this.mNewSS.getVoiceRoaming());
                    }
                    this.mNewSS.setCdmaDefaultRoamingIndicator(this.mDefaultRoamingIndicator);
                    this.mNewSS.setCdmaRoamingIndicator(this.mRoamingIndicator);
                    boolean isPrlLoaded = true;
                    if (TextUtils.isEmpty(this.mPrlVersion)) {
                        isPrlLoaded = false;
                    }
                    if (!isPrlLoaded || this.mNewSS.getRilVoiceRadioTechnology() == 0) {
                        log("Turn off roaming indicator if !isPrlLoaded or voice RAT is unknown");
                        this.mNewSS.setCdmaRoamingIndicator(1);
                    } else if (!isSidsAllZeros()) {
                        if (!namMatch && !this.mIsInPrl) {
                            this.mNewSS.setCdmaRoamingIndicator(this.mDefaultRoamingIndicator);
                        } else if (!namMatch || this.mIsInPrl) {
                            if (!namMatch && this.mIsInPrl) {
                                this.mNewSS.setCdmaRoamingIndicator(this.mRoamingIndicator);
                            } else if (this.mRoamingIndicator <= 2) {
                                this.mNewSS.setCdmaRoamingIndicator(1);
                            } else {
                                this.mNewSS.setCdmaRoamingIndicator(this.mRoamingIndicator);
                            }
                        } else if (this.mNewSS.getRilVoiceRadioTechnology() == 14) {
                            log("Turn off roaming indicator as voice is LTE");
                            this.mNewSS.setCdmaRoamingIndicator(1);
                        } else {
                            this.mNewSS.setCdmaRoamingIndicator(2);
                        }
                    }
                    int roamingIndicator = this.mNewSS.getCdmaRoamingIndicator();
                    this.mNewSS.setCdmaEriIconIndex(this.mPhone.mEriManager.getCdmaEriIconIndex(roamingIndicator, this.mDefaultRoamingIndicator));
                    this.mNewSS.setCdmaEriIconMode(this.mPhone.mEriManager.getCdmaEriIconMode(roamingIndicator, this.mDefaultRoamingIndicator));
                    HwTelephonyFactory.getHwNetworkManager().updateCTRoaming(this, this.mPhone, this.mNewSS, this.mNewSS.getRoaming());
                    log("Set CDMA Roaming Indicator to: " + this.mNewSS.getCdmaRoamingIndicator() + ". voiceRoaming = " + this.mNewSS.getVoiceRoaming() + ". dataRoaming = " + this.mNewSS.getDataRoaming() + ", isPrlLoaded = " + isPrlLoaded + ". namMatch = " + namMatch + " , mIsInPrl = " + this.mIsInPrl + ", mRoamingIndicator = " + this.mRoamingIndicator + ", mDefaultRoamingIndicator= " + this.mDefaultRoamingIndicator);
                }
                pollStateDone();
            }
        }
    }

    private boolean isRoamingBetweenOperators(boolean cdmaRoaming, ServiceState s) {
        return cdmaRoaming && !isSameOperatorNameFromSimAndSS(s);
    }

    private boolean isAutoTime(int registrationState) {
        boolean z = true;
        if (1 != registrationState && 5 != registrationState) {
            return false;
        }
        if (HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId() != getPhoneId()) {
            z = false;
        }
        return z;
    }

    void handlePollStateResultMessage(int what, AsyncResult ar) {
        String[] states;
        int type;
        int regState;
        switch (what) {
            case 4:
                int cssIndicator;
                if (this.mPhone.isPhoneTypeGsm()) {
                    states = (String[]) ar.result;
                    int lac = -1;
                    int cid = -1;
                    type = 0;
                    regState = 4;
                    int psc = -1;
                    cssIndicator = 0;
                    if (states.length > 0) {
                        try {
                            regState = Integer.parseInt(states[0]);
                            if (states.length >= 3) {
                                if (states[1] != null && states[1].length() > 0) {
                                    lac = Integer.parseInt(states[1], 16);
                                }
                                if (states[2] != null && states[2].length() > 0) {
                                    cid = Integer.parseInt(states[2], 16);
                                }
                                if (states.length >= 4 && states[3] != null) {
                                    type = Integer.parseInt(states[3]);
                                }
                            }
                            if (states.length >= 7 && states[7] != null) {
                                cssIndicator = Integer.parseInt(states[7]);
                            }
                            if (states.length > 14 && states[14] != null && states[14].length() > 0) {
                                psc = Integer.parseInt(states[14], 16);
                            }
                        } catch (NumberFormatException ex) {
                            loge("error parsing RegistrationState: " + ex);
                        }
                    }
                    type = HwTelephonyFactory.getHwNetworkManager().getCARilRadioType(this, this.mPhone, type);
                    this.mGsmRoaming = regCodeIsRoaming(regState);
                    this.mNewSS.setVoiceRegState(regCodeToServiceState(regState));
                    this.mNewSS.setRilVoiceRadioTechnology(type);
                    this.mNewSS.setCssIndicator(cssIndicator);
                    HwTelephonyFactory.getHwNetworkManager().sendGsmRoamingIntentIfDenied(this, this.mPhone, regState, states);
                    boolean isVoiceCapable = this.mPhone.getContext().getResources().getBoolean(17956956);
                    if ((regState == 13 || regState == 10 || regState == 12 || regState == 14) && isVoiceCapable) {
                        this.mEmergencyOnly = true;
                    } else {
                        this.mEmergencyOnly = false;
                    }
                    if (HwTelephonyFactory.getHwNetworkManager().isUpdateLacAndCid(this, this.mPhone, cid) || this.mHwCustGsmServiceStateTracker.isUpdateLacAndCidCust(this)) {
                        ((GsmCellLocation) this.mNewCellLoc).setLacAndCid(lac, cid);
                        ((GsmCellLocation) this.mNewCellLoc).setPsc(psc);
                        return;
                    }
                    return;
                }
                states = (String[]) ar.result;
                int registrationState = 4;
                int radioTechnology = -1;
                int baseStationId = -1;
                int baseStationLatitude = Integer.MAX_VALUE;
                int baseStationLongitude = Integer.MAX_VALUE;
                cssIndicator = 0;
                int systemId = 0;
                int networkId = 0;
                int roamingIndicator = -1;
                int systemIsInPrl = 0;
                int defaultRoamingIndicator = 0;
                int reasonForDenial = 0;
                if (states.length >= 14) {
                    try {
                        if (states[0] != null) {
                            registrationState = Integer.parseInt(states[0]);
                        }
                        if (states[3] != null) {
                            radioTechnology = Integer.parseInt(states[3]);
                        }
                        if (states[4] != null) {
                            baseStationId = Integer.parseInt(states[4]);
                        }
                        if (states[5] != null) {
                            baseStationLatitude = Integer.parseInt(states[5]);
                        }
                        if (states[6] != null) {
                            baseStationLongitude = Integer.parseInt(states[6]);
                        }
                        if (baseStationLatitude == 0 && baseStationLongitude == 0) {
                            baseStationLatitude = Integer.MAX_VALUE;
                            baseStationLongitude = Integer.MAX_VALUE;
                        }
                        if (states[7] != null) {
                            cssIndicator = Integer.parseInt(states[7]);
                        }
                        if (states[8] != null) {
                            systemId = Integer.parseInt(states[8]);
                        }
                        if (states[9] != null) {
                            networkId = Integer.parseInt(states[9]);
                        }
                        if (states[10] != null) {
                            roamingIndicator = Integer.parseInt(states[10]);
                        }
                        if (states[11] != null) {
                            systemIsInPrl = Integer.parseInt(states[11]);
                        }
                        if (states[12] != null) {
                            defaultRoamingIndicator = Integer.parseInt(states[12]);
                        }
                        if (states[13] != null) {
                            reasonForDenial = Integer.parseInt(states[13]);
                        }
                    } catch (NumberFormatException ex2) {
                        loge("EVENT_POLL_STATE_REGISTRATION_CDMA: error parsing: " + ex2);
                    }
                    radioTechnology = HwTelephonyFactory.getHwNetworkManager().getCARilRadioType(this, this.mPhone, radioTechnology);
                    this.mRegistrationState = registrationState;
                    boolean cdmaRoaming = regCodeIsRoaming(registrationState) && !isRoamIndForHomeSystem(states[10]);
                    this.mNewSS.setVoiceRoaming(cdmaRoaming);
                    this.mNewSS.setVoiceRegState(regCodeToServiceState(registrationState));
                    this.mNewSS.setRilVoiceRadioTechnology(radioTechnology);
                    if (isAutoTime(registrationState)) {
                        HwTelephonyFactory.getHwNetworkManager().setAutoTimeAndZoneForCdma(this, this.mPhone, radioTechnology);
                    }
                    this.mNewSS.setCssIndicator(cssIndicator);
                    this.mNewSS.setSystemAndNetworkId(systemId, networkId);
                    this.mRoamingIndicator = roamingIndicator;
                    this.mIsInPrl = systemIsInPrl != 0;
                    this.mDefaultRoamingIndicator = defaultRoamingIndicator;
                    ((CdmaCellLocation) this.mNewCellLoc).setCellLocationData(baseStationId, baseStationLatitude, baseStationLongitude, systemId, networkId);
                    if (reasonForDenial == 0) {
                        this.mRegistrationDeniedReason = REGISTRATION_DENIED_GEN;
                    } else if (reasonForDenial == 1) {
                        this.mRegistrationDeniedReason = REGISTRATION_DENIED_AUTH;
                    } else {
                        this.mRegistrationDeniedReason = "";
                    }
                    if (this.mRegistrationState == 3) {
                        log("Registration denied, " + this.mRegistrationDeniedReason);
                        return;
                    }
                    return;
                }
                throw new RuntimeException("Warning! Wrong number of parameters returned from RIL_REQUEST_REGISTRATION_STATE: expected 14 or more strings and got " + states.length + " strings");
            case 5:
                int dataRegState;
                if (this.mPhone.isPhoneTypeGsm()) {
                    states = (String[]) ar.result;
                    type = 0;
                    regState = 4;
                    this.mNewReasonDataDenied = -1;
                    this.mNewMaxDataCalls = 1;
                    if (states.length > 0) {
                        try {
                            regState = Integer.parseInt(states[0]);
                            if (states.length >= 4 && states[3] != null) {
                                type = Integer.parseInt(states[3]);
                            }
                            if (states.length >= 5 && regState == 3) {
                                this.mNewReasonDataDenied = Integer.parseInt(states[4]);
                            }
                            if (states.length >= 6) {
                                this.mNewMaxDataCalls = Integer.parseInt(states[5]);
                            }
                        } catch (NumberFormatException ex22) {
                            loge("error parsing GprsRegistrationState: " + ex22);
                        }
                    }
                    type = HwTelephonyFactory.getHwNetworkManager().updateCAStatus(this, this.mPhone, type);
                    dataRegState = regCodeToServiceState(regState);
                    this.mNewSS.setDataRegState(dataRegState);
                    this.mDataRoaming = regCodeIsRoaming(regState);
                    this.mNewSS.setRilDataRadioTechnology(type);
                    if (this.mHwCustGsmServiceStateTracker != null) {
                        this.mHwCustGsmServiceStateTracker.setPsCell(this.mSS, (GsmCellLocation) this.mNewCellLoc, states);
                    }
                    log("handlPollStateResultMessage: GsmSST setDataRegState=" + dataRegState + " regState=" + regState + " dataRadioTechnology=" + type);
                    return;
                } else if (this.mPhone.isPhoneTypeCdma()) {
                    states = (String[]) ar.result;
                    regState = 4;
                    int dataRadioTechnology = 0;
                    if (states.length > 0) {
                        try {
                            regState = Integer.parseInt(states[0]);
                            if (states.length >= 4 && states[3] != null) {
                                dataRadioTechnology = Integer.parseInt(states[3]);
                            }
                        } catch (NumberFormatException ex222) {
                            loge("handlePollStateResultMessage: error parsing GprsRegistrationState: " + ex222);
                        }
                    }
                    dataRadioTechnology = HwTelephonyFactory.getHwNetworkManager().updateCAStatus(this, this.mPhone, dataRadioTechnology);
                    dataRegState = regCodeToServiceState(regState);
                    this.mNewSS.setDataRegState(dataRegState);
                    this.mNewSS.setRilDataRadioTechnology(dataRadioTechnology);
                    this.mNewSS.setDataRoaming(regCodeIsRoaming(regState));
                    log("handlPollStateResultMessage: cdma setDataRegState=" + dataRegState + " regState=" + regState + " dataRadioTechnology=" + dataRadioTechnology);
                    return;
                } else {
                    Object states2 = (String[]) ar.result;
                    log("handlePollStateResultMessage: EVENT_POLL_STATE_GPRS states.length=" + states2.length + " states=" + states2);
                    int newDataRAT = 0;
                    regState = -1;
                    if (states2.length > 0) {
                        try {
                            regState = Integer.parseInt(states2[0]);
                            if (states2.length >= 4 && states2[3] != null) {
                                newDataRAT = Integer.parseInt(states2[3]);
                            }
                        } catch (NumberFormatException ex2222) {
                            loge("handlePollStateResultMessage: error parsing GprsRegistrationState: " + ex2222);
                        }
                    }
                    newDataRAT = HwTelephonyFactory.getHwNetworkManager().updateCAStatus(this, this.mPhone, newDataRAT);
                    int oldDataRAT = this.mSS.getRilDataRadioTechnology();
                    if ((oldDataRAT != 0 || newDataRAT == 0) && !(ServiceState.isCdma(oldDataRAT) && newDataRAT == 14)) {
                        if (oldDataRAT == 14 && ServiceState.isCdma(newDataRAT)) {
                        }
                        this.mNewSS.setRilDataRadioTechnology(newDataRAT);
                        dataRegState = regCodeToServiceState(regState);
                        this.mNewSS.setDataRegState(dataRegState);
                        this.mNewSS.setDataRoaming(regCodeIsRoaming(regState));
                        log("handlPollStateResultMessage: CdmaLteSST setDataRegState=" + dataRegState + " regState=" + regState + " dataRadioTechnology=" + newDataRAT);
                        return;
                    }
                    this.mCi.getSignalStrength(obtainMessage(3));
                    this.mNewSS.setRilDataRadioTechnology(newDataRAT);
                    dataRegState = regCodeToServiceState(regState);
                    this.mNewSS.setDataRegState(dataRegState);
                    this.mNewSS.setDataRoaming(regCodeIsRoaming(regState));
                    log("handlPollStateResultMessage: CdmaLteSST setDataRegState=" + dataRegState + " regState=" + regState + " dataRadioTechnology=" + newDataRAT);
                    return;
                }
            case 6:
                String[] opNames;
                String brandOverride;
                if (this.mPhone.isPhoneTypeGsm()) {
                    opNames = (String[]) ar.result;
                    if (opNames != null && opNames.length >= 3) {
                        brandOverride = this.mUiccController.getUiccCard(getPhoneId()) != null ? this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() : null;
                        if (brandOverride != null) {
                            log("EVENT_POLL_STATE_OPERATOR: use brandOverride=" + brandOverride);
                            this.mNewSS.setOperatorName(brandOverride, brandOverride, opNames[2]);
                            return;
                        }
                        this.mNewSS.setOperatorName(opNames[0], opNames[1], opNames[2]);
                        return;
                    }
                    return;
                }
                opNames = (String[]) ar.result;
                if (opNames == null || opNames.length < 3) {
                    log("EVENT_POLL_STATE_OPERATOR_CDMA: error parsing opNames");
                    return;
                }
                if (opNames[2] != null && opNames[2].length() >= 5) {
                    if ("00000".equals(opNames[2])) {
                    }
                    if (this.mIsSubscriptionFromRuim) {
                        this.mNewSS.setOperatorName(opNames[0], opNames[1], opNames[2]);
                        return;
                    }
                    brandOverride = this.mUiccController.getUiccCard(getPhoneId()) == null ? this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() : null;
                    if (brandOverride == null) {
                        this.mNewSS.setOperatorName(brandOverride, brandOverride, opNames[2]);
                        return;
                    } else {
                        this.mNewSS.setOperatorName(opNames[0], opNames[1], opNames[2]);
                        return;
                    }
                }
                opNames[0] = null;
                opNames[1] = null;
                opNames[2] = null;
                if (this.mIsSubscriptionFromRuim) {
                    if (this.mUiccController.getUiccCard(getPhoneId()) == null) {
                    }
                    if (brandOverride == null) {
                        this.mNewSS.setOperatorName(opNames[0], opNames[1], opNames[2]);
                        return;
                    } else {
                        this.mNewSS.setOperatorName(brandOverride, brandOverride, opNames[2]);
                        return;
                    }
                }
                this.mNewSS.setOperatorName(opNames[0], opNames[1], opNames[2]);
                return;
            case 14:
                int[] ints = (int[]) ar.result;
                this.mNewSS.setIsManualSelection(ints[0] == 1);
                if (ints[0] == 1 && (!this.mPhone.isManualNetSelAllowed() || this.mRecoverAutoSelectMode)) {
                    this.mPhone.setNetworkSelectionModeAutomatic(null);
                    log(" Forcing Automatic Network Selection, manual selection is not allowed");
                    this.mRecoverAutoSelectMode = false;
                    return;
                } else if (this.mRecoverAutoSelectMode) {
                    this.mRecoverAutoSelectMode = false;
                    return;
                } else {
                    return;
                }
            default:
                loge("handlePollStateResultMessage: Unexpected RIL response received: " + what);
                return;
        }
    }

    private boolean isRoamIndForHomeSystem(String roamInd) {
        String[] homeRoamIndicators = this.mPhone.getContext().getResources().getStringArray(17236032);
        if (homeRoamIndicators == null) {
            return false;
        }
        for (String homeRoamInd : homeRoamIndicators) {
            if (homeRoamInd.equals(roamInd)) {
                return true;
            }
        }
        return false;
    }

    protected void updateRoamingState() {
        CarrierConfigManager configLoader;
        PersistableBundle b;
        if (this.mPhone.isPhoneTypeGsm()) {
            boolean roaming = !this.mGsmRoaming ? this.mDataRoaming : true;
            log("updateRoamingState: original roaming = " + roaming + " mGsmRoaming:" + this.mGsmRoaming + " mDataRoaming:" + this.mDataRoaming);
            if (checkForRoamingForIndianOperators(this.mNewSS)) {
                log("indian operator,skip");
            } else if (this.mGsmRoaming && !isOperatorConsideredRoaming(this.mNewSS) && (isSameNamedOperators(this.mNewSS) || isOperatorConsideredNonRoaming(this.mNewSS))) {
                roaming = false;
                log("updateRoamingState: set roaming = false");
            }
            this.mNewSS.setDataRoamingFromRegistration(roaming);
            configLoader = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
            if (configLoader != null) {
                try {
                    b = configLoader.getConfigForSubId(this.mPhone.getSubId());
                    if (alwaysOnHomeNetwork(b)) {
                        log("updateRoamingState: carrier config override always on home network");
                        roaming = false;
                    } else if (isNonRoamingInGsmNetwork(b, this.mNewSS.getOperatorNumeric())) {
                        log("updateRoamingState: carrier config override set non roaming:" + this.mNewSS.getOperatorNumeric());
                        roaming = false;
                    } else if (isRoamingInGsmNetwork(b, this.mNewSS.getOperatorNumeric())) {
                        log("updateRoamingState: carrier config override set roaming:" + this.mNewSS.getOperatorNumeric());
                        roaming = true;
                    }
                } catch (Exception e) {
                    loge("updateRoamingState: unable to access carrier config service");
                }
            } else {
                log("updateRoamingState: no carrier config service available");
            }
            roaming = HwTelephonyFactory.getHwNetworkManager().getGsmRoamingState(this, this.mPhone, roaming);
            this.mNewSS.setVoiceRoaming(roaming);
            this.mNewSS.setDataRoaming(roaming);
            return;
        }
        this.mNewSS.setDataRoamingFromRegistration(this.mNewSS.getDataRoaming());
        configLoader = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        if (configLoader != null) {
            try {
                b = configLoader.getConfigForSubId(this.mPhone.getSubId());
                String systemId = Integer.toString(this.mNewSS.getSystemId());
                if (alwaysOnHomeNetwork(b)) {
                    log("updateRoamingState: carrier config override always on home network");
                    setRoamingOff();
                } else if (isNonRoamingInGsmNetwork(b, this.mNewSS.getOperatorNumeric()) || isNonRoamingInCdmaNetwork(b, systemId)) {
                    log("updateRoamingState: carrier config override set non-roaming:" + this.mNewSS.getOperatorNumeric() + ", " + systemId);
                    setRoamingOff();
                } else if (isRoamingInGsmNetwork(b, this.mNewSS.getOperatorNumeric()) || isRoamingInCdmaNetwork(b, systemId)) {
                    log("updateRoamingState: carrier config override set roaming:" + this.mNewSS.getOperatorNumeric() + ", " + systemId);
                    setRoamingOn();
                }
            } catch (Exception e2) {
                loge("updateRoamingState: unable to access carrier config service");
            }
        } else {
            log("updateRoamingState: no carrier config service available");
        }
        if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean(PROP_FORCE_ROAMING, false)) {
            this.mNewSS.setVoiceRoaming(true);
            this.mNewSS.setDataRoaming(true);
        }
    }

    private void setRoamingOn() {
        this.mNewSS.setVoiceRoaming(true);
        this.mNewSS.setDataRoaming(true);
        this.mNewSS.setCdmaEriIconIndex(0);
        this.mNewSS.setCdmaEriIconMode(0);
    }

    private void setRoamingOff() {
        this.mNewSS.setVoiceRoaming(false);
        this.mNewSS.setDataRoaming(false);
        this.mNewSS.setCdmaEriIconIndex(1);
    }

    public void updateSpnDisplay() {
        boolean showPlmn;
        String plmn;
        int subId;
        int[] subIds;
        if (this.mPhone.isPhoneTypeGsm()) {
            IccRecords iccRecords = this.mIccRecords;
            int rule = iccRecords != null ? iccRecords.getDisplayRule(this.mSS.getOperatorNumeric()) : 0;
            int combinedRegState = HwTelephonyFactory.getHwNetworkManager().getGsmCombinedRegState(this, this.mPhone, this.mSS);
            if (combinedRegState == 1 || combinedRegState == 2) {
                showPlmn = true;
                if (this.mEmergencyOnly) {
                    plmn = Resources.getSystem().getText(17040036).toString();
                } else {
                    plmn = Resources.getSystem().getText(17040012).toString();
                }
                if (this.mHwCustGsmServiceStateTracker != null) {
                    plmn = this.mHwCustGsmServiceStateTracker.setEmergencyToNoService(this.mSS, plmn, this.mEmergencyOnly);
                }
                log("updateSpnDisplay: radio is on but out of service, set plmn='" + plmn + "'");
            } else if (combinedRegState == 0) {
                plmn = HwTelephonyFactory.getHwNetworkManager().getGsmPlmn(this, this.mPhone);
                showPlmn = !TextUtils.isEmpty(plmn) ? (rule & 2) == 2 : false;
            } else {
                showPlmn = true;
                plmn = Resources.getSystem().getText(17040012).toString();
                log("updateSpnDisplay: radio is off w/ showPlmn=" + true + " plmn=" + plmn);
            }
            String spn = iccRecords != null ? iccRecords.getServiceProviderName() : "";
            String dataSpn = spn;
            boolean showSpn = (combinedRegState != 0 || TextUtils.isEmpty(spn)) ? false : (rule & 1) == 1;
            if (!TextUtils.isEmpty(spn) && this.mPhone.getImsPhone() != null && this.mPhone.getImsPhone().isWifiCallingEnabled()) {
                String[] wfcSpnFormats = this.mPhone.getContext().getResources().getStringArray(17236066);
                int voiceIdx = 0;
                int dataIdx = 0;
                CarrierConfigManager configLoader = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
                if (configLoader != null) {
                    try {
                        PersistableBundle b = configLoader.getConfigForSubId(this.mPhone.getSubId());
                        if (b != null) {
                            voiceIdx = b.getInt("wfc_spn_format_idx_int");
                            dataIdx = b.getInt("wfc_data_spn_format_idx_int");
                        }
                    } catch (Exception e) {
                        loge("updateSpnDisplay: carrier config error: " + e);
                    }
                }
                String formatVoice = wfcSpnFormats[voiceIdx];
                dataSpn = String.format(wfcSpnFormats[dataIdx], new Object[]{spn.trim()});
                showSpn = true;
                showPlmn = false;
            } else if (this.mSS.getVoiceRegState() == 3 || (showPlmn && TextUtils.equals(spn, plmn))) {
                spn = null;
                showSpn = false;
            }
            OnsDisplayParams onsDispalyParams = HwTelephonyFactory.getHwNetworkManager().getGsmOnsDisplayParams(this, this.mPhone, showSpn, showPlmn, rule, plmn, spn);
            showSpn = onsDispalyParams.mShowSpn;
            showPlmn = onsDispalyParams.mShowPlmn;
            rule = onsDispalyParams.mRule;
            plmn = onsDispalyParams.mPlmn;
            spn = onsDispalyParams.mSpn;
            boolean showWifi = onsDispalyParams.mShowWifi;
            String wifi = onsDispalyParams.mWifi;
            subId = -1;
            subIds = SubscriptionManager.getSubId(this.mPhone.getPhoneId());
            if (subIds != null && subIds.length > 0) {
                subId = subIds[0];
            }
            boolean show_blank_ons = false;
            if (this.mHwCustGsmServiceStateTracker != null && this.mHwCustGsmServiceStateTracker.isStopUpdateName(this.mSimCardsLoaded)) {
                show_blank_ons = true;
            }
            if ((display_blank_ons || show_blank_ons) && combinedRegState == 0) {
                log("In service , display blank ons for tracfone");
                plmn = " ";
                spn = " ";
                showPlmn = true;
                showSpn = false;
            }
            if (this.mSubId == subId && showPlmn == this.mCurShowPlmn && showSpn == this.mCurShowSpn && TextUtils.equals(spn, this.mCurSpn)) {
                if (TextUtils.equals(dataSpn, this.mCurDataSpn) && TextUtils.equals(plmn, this.mCurPlmn) && showWifi == this.mCurShowWifi) {
                    if (TextUtils.equals(wifi, this.mCurWifi)) {
                        this.mSubId = subId;
                        this.mCurShowSpn = showSpn;
                        this.mCurShowPlmn = showPlmn;
                        this.mCurSpn = spn;
                        this.mCurDataSpn = dataSpn;
                        this.mCurPlmn = plmn;
                        this.mCurShowWifi = showWifi;
                        this.mCurWifi = wifi;
                        return;
                    }
                }
            }
            log(String.format("updateSpnDisplay: changed sending intent rule=" + rule + " showPlmn='%b' plmn='%s' showSpn='%b' spn='%s' dataSpn='%s' subId='%d'", new Object[]{Boolean.valueOf(showPlmn), plmn, Boolean.valueOf(showSpn), spn, dataSpn, Integer.valueOf(subId)}));
            updateOperatorProp();
            Intent intent = new Intent("android.provider.Telephony.SPN_STRINGS_UPDATED");
            intent.addFlags(536870912);
            intent.putExtra("showSpn", showSpn);
            intent.putExtra("spn", spn);
            intent.putExtra("spnData", dataSpn);
            intent.putExtra("showPlmn", showPlmn);
            intent.putExtra(CellBroadcasts.PLMN, plmn);
            intent.putExtra(EXTRA_SHOW_WIFI, showWifi);
            intent.putExtra(EXTRA_WIFI, wifi);
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
            this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            if (!(this.mSS == null || this.mSS.getState() != 0 || this.mSubscriptionController.setPlmnSpn(this.mPhone.getPhoneId(), showPlmn, plmn, showSpn, spn))) {
                this.mSpnUpdatePending = true;
            }
            HwTelephonyFactory.getHwNetworkManager().sendGsmDualSimUpdateSpnIntent(this, this.mPhone, showSpn, spn, showPlmn, plmn);
            this.mSubId = subId;
            this.mCurShowSpn = showSpn;
            this.mCurShowPlmn = showPlmn;
            this.mCurSpn = spn;
            this.mCurDataSpn = dataSpn;
            this.mCurPlmn = plmn;
            this.mCurShowWifi = showWifi;
            this.mCurWifi = wifi;
            return;
        }
        plmn = HwTelephonyFactory.getHwNetworkManager().getCdmaPlmn(this, this.mPhone);
        showPlmn = plmn != null;
        subId = -1;
        subIds = SubscriptionManager.getSubId(this.mPhone.getPhoneId());
        if (subIds != null && subIds.length > 0) {
            subId = subIds[0];
        }
        if (display_blank_ons && (plmn != null || this.mSS.getState() == 0)) {
            log("In service , display blank ons for tracfone");
            plmn = " ";
        }
        if (!(this.mSubId == subId && TextUtils.equals(plmn, this.mCurPlmn))) {
            log(String.format("updateSpnDisplay: changed sending intent showPlmn='%b' plmn='%s' subId='%d'", new Object[]{Boolean.valueOf(showPlmn), plmn, Integer.valueOf(subId)}));
            intent = new Intent("android.provider.Telephony.SPN_STRINGS_UPDATED");
            intent.addFlags(536870912);
            intent.putExtra("showSpn", false);
            intent.putExtra("spn", "");
            intent.putExtra("showPlmn", showPlmn);
            intent.putExtra(CellBroadcasts.PLMN, plmn);
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
            this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            if (this.mSS != null && this.mSS.getState() == 0) {
                if (!this.mSubscriptionController.setPlmnSpn(this.mPhone.getPhoneId(), showPlmn, plmn, false, "")) {
                    this.mSpnUpdatePending = true;
                }
            }
            HwTelephonyFactory.getHwNetworkManager().sendCdmaDualSimUpdateSpnIntent(this, this.mPhone, false, "", showPlmn, plmn);
        }
        updateOperatorProp();
        this.mSubId = subId;
        this.mCurShowSpn = false;
        this.mCurShowPlmn = showPlmn;
        this.mCurSpn = "";
        this.mCurPlmn = plmn;
    }

    protected void setPowerStateToDesired() {
        log("setPowerStateToDesired: callers = " + Debug.getCallers(6));
        log("mDeviceShuttingDown=" + this.mDeviceShuttingDown + ", mDesiredPowerState=" + this.mDesiredPowerState + ", getRadioState=" + this.mCi.getRadioState() + ", mPowerOffDelayNeed=" + this.mPowerOffDelayNeed + ", mAlarmSwitch=" + this.mAlarmSwitch);
        if (ISDEMO) {
            this.mCi.setRadioPower(false, null);
        }
        if (this.mPhone.isPhoneTypeGsm() && this.mAlarmSwitch) {
            log("mAlarmSwitch == true");
            ((AlarmManager) this.mPhone.getContext().getSystemService("alarm")).cancel(this.mRadioOffIntent);
            this.mAlarmSwitch = false;
        }
        if (this.mDesiredPowerState && this.mCi.getRadioState() == RadioState.RADIO_OFF) {
            if (this.mHwCustGsmServiceStateTracker != null) {
                this.mHwCustGsmServiceStateTracker.setRadioPower(this.mCi, true);
            }
            this.mCi.setRadioPower(true, null);
        } else if (!this.mDesiredPowerState && this.mCi.getRadioState().isOn()) {
            if (!this.mPhone.isPhoneTypeGsm() || !this.mPowerOffDelayNeed) {
                powerOffRadioSafely(this.mPhone.mDcTracker);
            } else if (!this.mImsRegistrationOnOff || this.mAlarmSwitch) {
                powerOffRadioSafely(this.mPhone.mDcTracker);
            } else {
                log("mImsRegistrationOnOff == true");
                Context context = this.mPhone.getContext();
                AlarmManager am = (AlarmManager) context.getSystemService("alarm");
                this.mRadioOffIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_RADIO_OFF), 0);
                this.mAlarmSwitch = true;
                log("Alarm setting");
                am.set(2, SystemClock.elapsedRealtime() + 3000, this.mRadioOffIntent);
            }
            if (this.mHwCustGsmServiceStateTracker != null) {
                this.mHwCustGsmServiceStateTracker.setRadioPower(this.mCi, false);
            }
        } else if (this.mDeviceShuttingDown && this.mCi.getRadioState().isAvailable()) {
            this.mCi.requestShutdown(null);
        }
    }

    protected void onUpdateIccAvailability() {
        if (this.mUiccController != null) {
            UiccCardApplication newUiccApplication = getUiccCardApplication();
            if (this.mUiccApplcation != newUiccApplication) {
                if (this.mUiccApplcation != null) {
                    log("Removing stale icc objects.");
                    this.mUiccApplcation.unregisterForReady(this);
                    this.mUiccApplcation.unregisterForGetAdDone(this);
                    if (this.mIccRecords != null) {
                        this.mIccRecords.unregisterForRecordsLoaded(this);
                        HwTelephonyFactory.getHwNetworkManager().unregisterForSimRecordsEvents(this, this.mPhone, this.mIccRecords);
                    }
                    this.mIccRecords = null;
                    this.mUiccApplcation = null;
                }
                if (newUiccApplication != null) {
                    log("New card found");
                    this.mUiccApplcation = newUiccApplication;
                    this.mIccRecords = this.mUiccApplcation.getIccRecords();
                    if (this.mPhone.isPhoneTypeGsm()) {
                        this.mUiccApplcation.registerForReady(this, 17, null);
                        this.mUiccApplcation.registerForGetAdDone(this, 1002, null);
                        if (this.mIccRecords != null) {
                            this.mIccRecords.registerForRecordsLoaded(this, 16, null);
                            HwTelephonyFactory.getHwNetworkManager().registerForSimRecordsEvents(this, this.mPhone, this.mIccRecords);
                        }
                    } else if (this.mIsSubscriptionFromRuim) {
                        registerForRuimEvents();
                    }
                }
            }
        }
    }

    protected void log(String s) {
        Rlog.d(this.LOG_TAG, s);
    }

    protected void loge(String s) {
        Rlog.e(this.LOG_TAG, s);
    }

    public int getCurrentDataConnectionState() {
        return this.mSS.getDataRegState();
    }

    public boolean isConcurrentVoiceAndDataAllowed() {
        boolean z = true;
        if (!this.mPhone.isPhoneTypeGsm()) {
            return (this.mPhone.isPhoneTypeCdma() || this.mSS.getRilDataRadioTechnology() != 14 || MDOEM_WORK_MODE_IS_SRLTE) ? false : true;
        } else {
            if (SystemProperties.get("ro.hwpp.wcdma_voice_preference", "false").equals("true") && !this.isCurrent3GPsCsAllowed) {
                log("current not allow voice and data simultaneously by vp");
                return false;
            } else if (this.mSS.getRilDataRadioTechnology() >= 3 && this.mSS.getRilDataRadioTechnology() != 16) {
                return true;
            } else {
                if (this.mSS.getCssIndicator() != 1) {
                    z = false;
                }
                return z;
            }
        }
    }

    public void setImsRegistrationState(boolean registered) {
        log("ImsRegistrationState - registered : " + registered);
        if (this.mImsRegistrationOnOff && !registered && this.mAlarmSwitch) {
            this.mImsRegistrationOnOff = registered;
            ((AlarmManager) this.mPhone.getContext().getSystemService("alarm")).cancel(this.mRadioOffIntent);
            this.mAlarmSwitch = false;
            sendMessage(obtainMessage(EVENT_CHANGE_IMS_STATE));
            return;
        }
        this.mImsRegistrationOnOff = registered;
    }

    public void onImsCapabilityChanged() {
        if (this.mPhone.isPhoneTypeGsm()) {
            sendMessage(obtainMessage(EVENT_IMS_CAPABILITY_CHANGED));
        }
    }

    public void pollState() {
        pollState(false);
    }

    private void modemTriggeredPollState() {
        pollState(true);
    }

    public void pollState(boolean modemTriggered) {
        this.mPollingContext = new int[1];
        this.mPollingContext[0] = 0;
        switch (-getcom-android-internal-telephony-CommandsInterface$RadioStateSwitchesValues()[this.mCi.getRadioState().ordinal()]) {
            case 1:
                this.mNewSS.setStateOff();
                this.mNewCellLoc.setStateInvalid();
                setSignalStrengthDefaultValues();
                this.mGotCountryCode = false;
                this.mNitzUpdatedTime = false;
                if (!(modemTriggered || 18 == this.mSS.getRilDataRadioTechnology())) {
                    pollStateDone();
                    return;
                }
            case 2:
                this.mNewSS.setStateOutOfService();
                this.mNewCellLoc.setStateInvalid();
                setSignalStrengthDefaultValues();
                this.mGotCountryCode = false;
                this.mNitzUpdatedTime = false;
                pollStateDone();
                return;
        }
        int[] iArr = this.mPollingContext;
        iArr[0] = iArr[0] + 1;
        this.mCi.getOperator(obtainMessage(6, this.mPollingContext));
        iArr = this.mPollingContext;
        iArr[0] = iArr[0] + 1;
        this.mCi.getDataRegistrationState(obtainMessage(5, this.mPollingContext));
        iArr = this.mPollingContext;
        iArr[0] = iArr[0] + 1;
        this.mCi.getVoiceRegistrationState(obtainMessage(4, this.mPollingContext));
        if (this.mPhone.isPhoneTypeGsm()) {
            iArr = this.mPollingContext;
            iArr[0] = iArr[0] + 1;
            this.mCi.getNetworkSelectionMode(obtainMessage(14, this.mPollingContext));
            HwTelephonyFactory.getHwNetworkManager().getLocationInfo(this, this.mPhone);
        }
    }

    private void updateOperatorProp() {
        if (this.mPhone != null && this.mSS != null) {
            this.mPhone.setSystemProperty("gsm.operator.alpha", this.mSS.getOperatorAlphaLong());
        }
    }

    private void pollStateDone() {
        if (this.mPhone.isPhoneTypeGsm()) {
            pollStateDoneGsm();
        } else if (this.mPhone.isPhoneTypeCdma()) {
            pollStateDoneCdma();
        } else {
            pollStateDoneCdmaLte();
        }
    }

    private void pollStateDoneGsm() {
        if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean(PROP_FORCE_ROAMING, false)) {
            this.mNewSS.setVoiceRoaming(true);
            this.mNewSS.setDataRoaming(true);
        }
        useDataRegStateForDataOnlyDevices();
        resetServiceStateInIwlanMode();
        log("Poll ServiceState done:  oldSS=[" + this.mSS + "] newSS=[" + this.mNewSS + "]" + " oldMaxDataCalls=" + this.mMaxDataCalls + " mNewMaxDataCalls=" + this.mNewMaxDataCalls + " oldReasonDataDenied=" + this.mReasonDataDenied + " mNewReasonDataDenied=" + this.mNewReasonDataDenied);
        boolean hasRegistered = this.mSS.getVoiceRegState() != 0 ? this.mNewSS.getVoiceRegState() == 0 : false;
        if (this.mSS.getVoiceRegState() == 0) {
            if (this.mNewSS.getVoiceRegState() != 0) {
            }
        }
        boolean hasGprsAttached = this.mSS.getDataRegState() != 0 ? this.mNewSS.getDataRegState() == 0 : false;
        boolean hasGprsDetached = this.mSS.getDataRegState() == 0 ? this.mNewSS.getDataRegState() != 0 : false;
        boolean hasDataRegStateChanged = this.mSS.getDataRegState() != this.mNewSS.getDataRegState();
        boolean hasVoiceRegStateChanged = this.mSS.getVoiceRegState() != this.mNewSS.getVoiceRegState();
        boolean hasRilVoiceRadioTechnologyChanged = this.mSS.getRilVoiceRadioTechnology() != this.mNewSS.getRilVoiceRadioTechnology();
        boolean hasRilDataRadioTechnologyChanged = this.mSS.getRilDataRadioTechnology() != this.mNewSS.getRilDataRadioTechnology();
        boolean hasChanged = !this.mNewSS.equals(this.mSS);
        boolean voiceRoaming = !this.mSS.getVoiceRoaming() ? this.mNewSS.getVoiceRoaming() : false;
        boolean hasVoiceRoamingOff = this.mSS.getVoiceRoaming() && !this.mNewSS.getVoiceRoaming();
        boolean dataRoaming = !this.mSS.getDataRoaming() ? this.mNewSS.getDataRoaming() : false;
        boolean hasDataRoamingOff = this.mSS.getDataRoaming() && !this.mNewSS.getDataRoaming();
        boolean hasLocationChanged = !this.mNewCellLoc.equals(this.mCellLoc);
        boolean needNotifyData = this.mSS.getCssIndicator() != this.mNewSS.getCssIndicator();
        boolean hasLacChanged = ((GsmCellLocation) this.mNewCellLoc).getLac() != ((GsmCellLocation) this.mCellLoc).getLac();
        resetServiceStateInIwlanMode();
        TelephonyManager tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
        if (hasVoiceRegStateChanged || hasDataRegStateChanged) {
            EventLog.writeEvent(EventLogTags.GSM_SERVICE_STATE_CHANGE, new Object[]{Integer.valueOf(this.mSS.getVoiceRegState()), Integer.valueOf(this.mSS.getDataRegState()), Integer.valueOf(this.mNewSS.getVoiceRegState()), Integer.valueOf(this.mNewSS.getDataRegState())});
        }
        if (hasChanged && hasRegistered) {
            this.mPhone.mDcTracker.setMPDNByNetWork(this.mNewSS.getOperatorNumeric());
        }
        if (hasRegistered || hasGprsAttached) {
            log("service state hasRegistered , poll signal strength at once");
            sendMessage(obtainMessage(10));
        }
        if (hasRilVoiceRadioTechnologyChanged) {
            int cid = -1;
            GsmCellLocation loc = (GsmCellLocation) this.mNewCellLoc;
            if (loc != null) {
                cid = loc.getCid();
            }
            EventLog.writeEvent(EventLogTags.GSM_RAT_SWITCHED_NEW, new Object[]{Integer.valueOf(cid), Integer.valueOf(this.mSS.getRilVoiceRadioTechnology()), Integer.valueOf(this.mNewSS.getRilVoiceRadioTechnology())});
            log("RAT switched " + ServiceState.rilRadioTechnologyToString(this.mSS.getRilVoiceRadioTechnology()) + " -> " + ServiceState.rilRadioTechnologyToString(this.mNewSS.getRilVoiceRadioTechnology()) + " at cell " + cid);
        }
        if (HwTelephonyFactory.getHwNetworkManager().proccessGsmDelayUpdateRegisterStateDone(this, this.mPhone, this.mSS, this.mNewSS)) {
            this.mDelayUpdateGpsState = this.mNewSS.getDataRegState();
            return;
        }
        HwTelephonyFactory.getHwNetworkManager().gprsAttach(this, this.mSS, this.mNewSS);
        ServiceState tss = this.mSS;
        this.mSS = this.mNewSS;
        this.mNewSS = tss;
        this.mNewSS.setStateOutOfService();
        CellLocation tcl = (GsmCellLocation) this.mCellLoc;
        this.mCellLoc = this.mNewCellLoc;
        this.mNewCellLoc = tcl;
        this.mReasonDataDenied = this.mNewReasonDataDenied;
        this.mMaxDataCalls = this.mNewMaxDataCalls;
        if (hasRilVoiceRadioTechnologyChanged) {
            updatePhoneObject();
        }
        if (hasRilDataRadioTechnologyChanged) {
            tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
            if (18 == this.mSS.getRilDataRadioTechnology()) {
                log("pollStateDone: IWLAN enabled");
            }
        }
        if (hasRegistered) {
            this.mPhone.getContext().sendBroadcast(new Intent("com.android.net.wifi.countryCode"));
            this.mNetworkAttachedRegistrants.notifyRegistrants();
            if (CLEAR_NITZ_WHEN_REG && SystemClock.elapsedRealtime() - this.mLastReceivedNITZReferenceTime > 5000) {
                this.mNitzUpdatedTime = false;
                log("pollStateDone: registering current mNitzUpdatedTime=" + this.mNitzUpdatedTime + " changing to false because hasRegistered");
            }
        }
        if (hasChanged) {
            updateSpnDisplay();
            tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlphaLong());
            updateOperatorProp();
            judgeToLaunchCsgPeriodicSearchTimer();
            String prevOperatorNumeric = tm.getNetworkOperatorForPhone(this.mPhone.getPhoneId());
            String operatorNumeric = this.mSS.getOperatorNumeric();
            tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
            updateCarrierMccMncConfiguration(operatorNumeric, prevOperatorNumeric, this.mPhone.getContext());
            if (TextUtils.isEmpty(operatorNumeric)) {
                log("operatorNumeric is null");
                tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), "");
                this.mGotCountryCode = false;
                this.mNitzUpdatedTime = false;
            } else {
                String iso = "";
                String mcc = "";
                String prevMcc = "";
                try {
                    mcc = operatorNumeric.substring(0, 3);
                    iso = MccTable.countryCodeForMcc(Integer.parseInt(mcc));
                    TimeZone zone;
                    boolean testOneUniqueOffsetPath;
                    ArrayList<TimeZone> uniqueZones;
                    TimeZone defaultZones;
                    if (prevOperatorNumeric == null || prevOperatorNumeric.equals("")) {
                        prevMcc = "";
                        tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), iso);
                        this.mGotCountryCode = true;
                        zone = null;
                        if (!(this.mNitzUpdatedTime || mcc.equals(INVALID_MCC) || TextUtils.isEmpty(iso) || !getAutoTimeZone() || mcc.equals(prevMcc))) {
                            testOneUniqueOffsetPath = SystemProperties.getBoolean("telephony.test.ignore.nitz", false) ? (SystemClock.uptimeMillis() & 1) != 0 : false;
                            uniqueZones = TimeUtils.getTimeZonesWithUniqueOffsets(iso);
                            defaultZones = getTimeZoneFromMcc(mcc);
                            if (uniqueZones.size() == 1 && !testOneUniqueOffsetPath && defaultZones == null) {
                                log("pollStateDone: there are " + uniqueZones.size() + " unique offsets for iso-cc='" + iso + " testOneUniqueOffsetPath=" + testOneUniqueOffsetPath + "', do nothing");
                            } else {
                                zone = (TimeZone) uniqueZones.get(0);
                                if (defaultZones != null) {
                                    zone = defaultZones;
                                    log("some countrys has more than two timezone, choose a default");
                                }
                                log("pollStateDone: no nitz but one TZ for iso-cc=" + iso + " with zone.getID=" + zone.getID() + " testOneUniqueOffsetPath=" + testOneUniqueOffsetPath);
                                setAndBroadcastNetworkSetTimeZone(zone.getID());
                            }
                        }
                        if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                            fixTimeZone(iso, zone);
                        }
                    } else {
                        prevMcc = prevOperatorNumeric.substring(0, 3);
                        tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), iso);
                        this.mGotCountryCode = true;
                        zone = null;
                        if (SystemProperties.getBoolean("telephony.test.ignore.nitz", false)) {
                            if ((SystemClock.uptimeMillis() & 1) != 0) {
                            }
                        }
                        uniqueZones = TimeUtils.getTimeZonesWithUniqueOffsets(iso);
                        defaultZones = getTimeZoneFromMcc(mcc);
                        if (uniqueZones.size() == 1) {
                        }
                        zone = (TimeZone) uniqueZones.get(0);
                        if (defaultZones != null) {
                            zone = defaultZones;
                            log("some countrys has more than two timezone, choose a default");
                        }
                        log("pollStateDone: no nitz but one TZ for iso-cc=" + iso + " with zone.getID=" + zone.getID() + " testOneUniqueOffsetPath=" + testOneUniqueOffsetPath);
                        setAndBroadcastNetworkSetTimeZone(zone.getID());
                        if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                            fixTimeZone(iso, zone);
                        }
                    }
                } catch (NumberFormatException ex) {
                    loge("pollStateDone: countryCodeForMcc error" + ex);
                } catch (StringIndexOutOfBoundsException ex2) {
                    loge("pollStateDone: countryCodeForMcc error" + ex2);
                }
            }
            tm.setNetworkRoamingForPhone(this.mPhone.getPhoneId(), this.mSS.getVoiceRoaming());
            setRoamingType(this.mSS);
            log("Broadcasting ServiceState : " + this.mSS);
            this.mPhone.notifyServiceStateChanged(this.mSS);
            if (this.mHwCustGsmServiceStateTracker != null) {
                this.mHwCustGsmServiceStateTracker.updateRomingVoicemailNumber(this.mSS);
            }
            this.mEventLog.writeServiceStateChanged(this.mSS);
        }
        HwTelephonyFactory.getHwNetworkManager().processGsmCTNumMatch(this, this.mPhone, this.mSS.getRoaming(), this.mUiccApplcation);
        if (hasGprsAttached) {
            this.mAttachedRegistrants.notifyRegistrants();
        }
        if (hasGprsDetached) {
            this.mDetachedRegistrants.notifyRegistrants();
        }
        if (hasDataRegStateChanged || hasRilDataRadioTechnologyChanged) {
            notifyDataRegStateRilRadioTechnologyChanged();
            if (18 == this.mSS.getRilDataRadioTechnology()) {
                this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_IWLAN_AVAILABLE);
            } else {
                needNotifyData = true;
            }
        }
        if (needNotifyData) {
            this.mPhone.notifyDataConnection(null);
        }
        if (voiceRoaming) {
            if (SystemProperties.getBoolean("ro.config_hw_doubletime", false) && HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId() == getPhoneId()) {
                log("[settimezone]roaming on, waiting for a few minutes to see if the NITZ is supported by the current network.");
                Message roamingOn = obtainMessage();
                roamingOn.what = 100;
                sendMessageDelayed(roamingOn, 60000);
            }
            this.mVoiceRoamingOnRegistrants.notifyRegistrants();
        }
        if (hasVoiceRoamingOff) {
            this.mVoiceRoamingOffRegistrants.notifyRegistrants();
        }
        if (dataRoaming) {
            if (SystemProperties.getBoolean("ro.config_hw_doubletime", false) && HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId() == getPhoneId()) {
                log("[settimezone]roaming on, waiting for a few minutes to see if the NITZ is supported by the current network.");
                roamingOn = obtainMessage();
                roamingOn.what = 100;
                sendMessageDelayed(roamingOn, 60000);
            }
            this.mDataRoamingOnRegistrants.notifyRegistrants();
        }
        if (hasDataRoamingOff) {
            this.mDataRoamingOffRegistrants.notifyRegistrants();
        }
        if (hasLocationChanged) {
            this.mPhone.notifyLocationChanged();
        }
        if (hasLacChanged) {
            Rlog.i(this.LOG_TAG, "LAC changed, update operator name display");
            updateSpnDisplay();
        }
        if (isGprsConsistent(this.mSS.getDataRegState(), this.mSS.getVoiceRegState())) {
            this.mReportedGprsNoReg = false;
        } else if (!(this.mStartedGprsRegCheck || this.mReportedGprsNoReg)) {
            this.mStartedGprsRegCheck = true;
            sendMessageDelayed(obtainMessage(22), (long) Global.getInt(this.mPhone.getContext().getContentResolver(), "gprs_register_check_period_ms", 60000));
        }
    }

    protected TimeZone getTimeZoneFromMcc(String mcc) {
        if ("460".equals(mcc)) {
            return TimeZone.getTimeZone("Asia/Shanghai");
        }
        if ("255".equals(mcc)) {
            return TimeZone.getTimeZone("Europe/Kiev");
        }
        if ("214".equals(mcc)) {
            return TimeZone.getTimeZone("Europe/Madrid");
        }
        return null;
    }

    private void sendTimeZoneSelectionNotification() {
        String currentMcc = null;
        String operator = this.mSS.getOperatorNumeric();
        if (operator != null && operator.length() >= 3) {
            currentMcc = operator.substring(0, 3);
        }
        if (!this.mNitzUpdatedTime) {
            boolean isTheSameNWAsLast;
            ArrayList<TimeZone> timeZones = null;
            int tzListSize = 0;
            String iso = getSystemProperty("gsm.operator.iso-country", "");
            String lastMcc = Systemex.getString(this.mCr, "last_registed_mcc");
            if (lastMcc == null || currentMcc == null) {
                isTheSameNWAsLast = false;
            } else {
                isTheSameNWAsLast = lastMcc.equals(currentMcc);
            }
            log("[settimezone] the network " + operator + " don't support nitz! current network isTheSameNWAsLast" + isTheSameNWAsLast);
            if (!"".equals(iso)) {
                timeZones = TimeUtils.getTimeZones(iso);
                tzListSize = timeZones == null ? 0 : timeZones.size();
            }
            if (1 != tzListSize || isTheSameNWAsLast) {
                log("[settimezone] there are " + tzListSize + " timezones in " + iso);
                Intent intent = new Intent(ACTION_TIMEZONE_SELECTION);
                intent.putExtra(TelephonyEventLog.SERVICE_STATE_VOICE_NUMERIC, operator);
                intent.putExtra("iso", iso);
                this.mPhone.getContext().sendStickyBroadcast(intent);
            } else {
                TimeZone tz = (TimeZone) timeZones.get(0);
                log("[settimezone] time zone:" + tz.getID());
                setAndBroadcastNetworkSetTimeZone(tz.getID());
            }
        }
        if (currentMcc != null) {
            Systemex.putString(this.mCr, "last_registed_mcc", currentMcc);
        }
    }

    protected void pollStateDoneCdma() {
        updateRoamingState();
        useDataRegStateForDataOnlyDevices();
        resetServiceStateInIwlanMode();
        log("pollStateDone: cdma oldSS=[" + this.mSS + "] newSS=[" + this.mNewSS + "]");
        boolean hasRegistered = this.mSS.getVoiceRegState() != 0 ? this.mNewSS.getVoiceRegState() == 0 : false;
        boolean hasCdmaDataConnectionAttached = this.mSS.getDataRegState() != 0 ? this.mNewSS.getDataRegState() == 0 : false;
        boolean hasCdmaDataConnectionDetached = this.mSS.getDataRegState() == 0 ? this.mNewSS.getDataRegState() != 0 : false;
        boolean hasCdmaDataConnectionChanged = this.mSS.getDataRegState() != this.mNewSS.getDataRegState();
        boolean hasRilVoiceRadioTechnologyChanged = this.mSS.getRilVoiceRadioTechnology() != this.mNewSS.getRilVoiceRadioTechnology();
        boolean hasRilDataRadioTechnologyChanged = this.mSS.getRilDataRadioTechnology() != this.mNewSS.getRilDataRadioTechnology();
        boolean hasChanged = !this.mNewSS.equals(this.mSS);
        boolean voiceRoaming = !this.mSS.getVoiceRoaming() ? this.mNewSS.getVoiceRoaming() : false;
        boolean hasVoiceRoamingOff = this.mSS.getVoiceRoaming() && !this.mNewSS.getVoiceRoaming();
        boolean dataRoaming = !this.mSS.getDataRoaming() ? this.mNewSS.getDataRoaming() : false;
        boolean hasDataRoamingOff = this.mSS.getDataRoaming() && !this.mNewSS.getDataRoaming();
        boolean hasLocationChanged = !this.mNewCellLoc.equals(this.mCellLoc);
        TelephonyManager tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
        if (!(this.mSS.getVoiceRegState() == this.mNewSS.getVoiceRegState() && this.mSS.getDataRegState() == this.mNewSS.getDataRegState())) {
            EventLog.writeEvent(EventLogTags.CDMA_SERVICE_STATE_CHANGE, new Object[]{Integer.valueOf(this.mSS.getVoiceRegState()), Integer.valueOf(this.mSS.getDataRegState()), Integer.valueOf(this.mNewSS.getVoiceRegState()), Integer.valueOf(this.mNewSS.getDataRegState())});
        }
        if (hasRegistered || hasCdmaDataConnectionAttached) {
            log("service state hasRegistered , poll signal strength at once");
            sendMessage(obtainMessage(10));
        }
        ServiceState tss = this.mSS;
        this.mSS = this.mNewSS;
        this.mNewSS = tss;
        this.mNewSS.setStateOutOfService();
        CellLocation tcl = (CdmaCellLocation) this.mCellLoc;
        this.mCellLoc = this.mNewCellLoc;
        this.mNewCellLoc = tcl;
        if (hasRilVoiceRadioTechnologyChanged) {
            updatePhoneObject();
        }
        if (hasRilDataRadioTechnologyChanged) {
            tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
            if (18 == this.mSS.getRilDataRadioTechnology()) {
                log("pollStateDone: IWLAN enabled");
            }
        }
        if (hasRegistered) {
            if (SystemProperties.getBoolean("ro.config_hw_doubletime", false)) {
                String mccmnc = this.mSS.getOperatorNumeric();
                String mcc = "";
                if (mccmnc != null) {
                    Systemex.putString(this.mCr, "last_registed_mcc", mccmnc.substring(0, 3));
                }
            }
            this.mNetworkAttachedRegistrants.notifyRegistrants();
        }
        if (hasChanged) {
            if (this.mCi.getRadioState().isOn() && !this.mIsSubscriptionFromRuim) {
                String eriText;
                if (this.mSS.getVoiceRegState() == 0) {
                    eriText = this.mPhone.getCdmaEriText();
                } else {
                    eriText = this.mPhone.getContext().getText(17039589).toString();
                }
                this.mSS.setOperatorAlphaLong(eriText);
            }
            tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlphaLong());
            updateOperatorProp();
            String prevOperatorNumeric = tm.getNetworkOperatorForPhone(this.mPhone.getPhoneId());
            String operatorNumeric = this.mSS.getOperatorNumeric();
            if (isInvalidOperatorNumeric(operatorNumeric)) {
                operatorNumeric = fixUnknownMcc(operatorNumeric, this.mSS.getSystemId());
            }
            tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
            updateCarrierMccMncConfiguration(operatorNumeric, prevOperatorNumeric, this.mPhone.getContext());
            if (isInvalidOperatorNumeric(operatorNumeric)) {
                log("operatorNumeric " + operatorNumeric + "is invalid");
                tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), "");
                this.mGotCountryCode = false;
            } else {
                String isoCountryCode = "";
                mcc = operatorNumeric.substring(0, 3);
                try {
                    isoCountryCode = MccTable.countryCodeForMcc(Integer.parseInt(operatorNumeric.substring(0, 3)));
                } catch (NumberFormatException ex) {
                    loge("pollStateDone: countryCodeForMcc error" + ex);
                } catch (StringIndexOutOfBoundsException ex2) {
                    loge("pollStateDone: countryCodeForMcc error" + ex2);
                }
                tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), isoCountryCode);
                this.mGotCountryCode = true;
                setOperatorIdd(operatorNumeric);
                if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                    fixTimeZone(isoCountryCode);
                }
            }
            tm.setNetworkRoamingForPhone(this.mPhone.getPhoneId(), !this.mSS.getVoiceRoaming() ? this.mSS.getDataRoaming() : true);
            updateSpnDisplay();
            setRoamingType(this.mSS);
            log("Broadcasting ServiceState : " + this.mSS);
            this.mPhone.notifyServiceStateChanged(this.mSS);
        }
        HwTelephonyFactory.getHwNetworkManager().processCdmaCTNumMatch(this, this.mPhone, this.mSS.getRoaming(), this.mUiccApplcation);
        if (hasCdmaDataConnectionAttached) {
            this.mAttachedRegistrants.notifyRegistrants();
        }
        if (hasCdmaDataConnectionDetached) {
            this.mDetachedRegistrants.notifyRegistrants();
        }
        if (hasCdmaDataConnectionChanged || hasRilDataRadioTechnologyChanged) {
            notifyDataRegStateRilRadioTechnologyChanged();
            if (18 == this.mSS.getRilDataRadioTechnology()) {
                this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_IWLAN_AVAILABLE);
            } else {
                this.mPhone.notifyDataConnection(null);
            }
        }
        if (voiceRoaming) {
            this.mVoiceRoamingOnRegistrants.notifyRegistrants();
        }
        if (hasVoiceRoamingOff) {
            this.mVoiceRoamingOffRegistrants.notifyRegistrants();
        }
        if (dataRoaming) {
            this.mDataRoamingOnRegistrants.notifyRegistrants();
        }
        if (hasDataRoamingOff) {
            this.mDataRoamingOffRegistrants.notifyRegistrants();
        }
        if (hasLocationChanged) {
            this.mPhone.notifyLocationChanged();
        }
    }

    protected void pollStateDoneCdmaLte() {
        updateRoamingState();
        if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean(PROP_FORCE_ROAMING, false)) {
            this.mNewSS.setVoiceRoaming(true);
            this.mNewSS.setDataRoaming(true);
        }
        useDataRegStateForDataOnlyDevices();
        resetServiceStateInIwlanMode();
        log("pollStateDone: lte 1 ss=[" + this.mSS + "] newSS=[" + this.mNewSS + "]");
        boolean hasRegistered = this.mSS.getVoiceRegState() != 0 ? this.mNewSS.getVoiceRegState() == 0 : false;
        boolean hasDeregistered = this.mSS.getVoiceRegState() == 0 ? this.mNewSS.getVoiceRegState() != 0 : false;
        boolean hasCdmaDataConnectionAttached = this.mSS.getDataRegState() != 0 ? this.mNewSS.getDataRegState() == 0 : false;
        boolean hasCdmaDataConnectionDetached = this.mSS.getDataRegState() == 0 ? this.mNewSS.getDataRegState() != 0 : false;
        boolean hasCdmaDataConnectionChanged = this.mSS.getDataRegState() != this.mNewSS.getDataRegState();
        boolean hasVoiceRadioTechnologyChanged = this.mSS.getRilVoiceRadioTechnology() != this.mNewSS.getRilVoiceRadioTechnology();
        boolean hasDataRadioTechnologyChanged = this.mSS.getRilDataRadioTechnology() != this.mNewSS.getRilDataRadioTechnology();
        boolean hasChanged = !this.mNewSS.equals(this.mSS);
        boolean voiceRoaming = !this.mSS.getVoiceRoaming() ? this.mNewSS.getVoiceRoaming() : false;
        boolean hasVoiceRoamingOff = this.mSS.getVoiceRoaming() && !this.mNewSS.getVoiceRoaming();
        boolean dataRoaming = !this.mSS.getDataRoaming() ? this.mNewSS.getDataRoaming() : false;
        boolean hasDataRoamingOff = this.mSS.getDataRoaming() && !this.mNewSS.getDataRoaming();
        boolean hasLocationChanged = !this.mNewCellLoc.equals(this.mCellLoc);
        boolean has4gHandoff = this.mNewSS.getDataRegState() == 0 ? (this.mSS.getRilDataRadioTechnology() == 14 && this.mNewSS.getRilDataRadioTechnology() == 13) ? true : this.mSS.getRilDataRadioTechnology() == 13 ? this.mNewSS.getRilDataRadioTechnology() == 14 : false : false;
        boolean hasMultiApnSupport = (this.mNewSS.getRilDataRadioTechnology() == 14 || this.mNewSS.getRilDataRadioTechnology() == 13) ? this.mSS.getRilDataRadioTechnology() != 14 ? this.mSS.getRilDataRadioTechnology() != 13 : false : false;
        boolean hasLostMultiApnSupport = this.mNewSS.getRilDataRadioTechnology() >= 4 ? this.mNewSS.getRilDataRadioTechnology() <= 8 : false;
        boolean needNotifyData = this.mSS.getCssIndicator() != this.mNewSS.getCssIndicator();
        TelephonyManager tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
        if (hasRegistered || hasCdmaDataConnectionAttached) {
            log("service state hasRegistered , poll signal strength at once");
            sendMessage(obtainMessage(10));
        }
        log("pollStateDone: hasRegistered=" + hasRegistered + " hasDeegistered=" + hasDeregistered + " hasCdmaDataConnectionAttached=" + hasCdmaDataConnectionAttached + " hasCdmaDataConnectionDetached=" + hasCdmaDataConnectionDetached + " hasCdmaDataConnectionChanged=" + hasCdmaDataConnectionChanged + " hasVoiceRadioTechnologyChanged= " + hasVoiceRadioTechnologyChanged + " hasDataRadioTechnologyChanged=" + hasDataRadioTechnologyChanged + " hasChanged=" + hasChanged + " hasVoiceRoamingOn=" + voiceRoaming + " hasVoiceRoamingOff=" + hasVoiceRoamingOff + " hasDataRoamingOn=" + dataRoaming + " hasDataRoamingOff=" + hasDataRoamingOff + " hasLocationChanged=" + hasLocationChanged + " has4gHandoff = " + has4gHandoff + " hasMultiApnSupport=" + hasMultiApnSupport + " hasLostMultiApnSupport=" + hasLostMultiApnSupport);
        if (!(this.mSS.getVoiceRegState() == this.mNewSS.getVoiceRegState() && this.mSS.getDataRegState() == this.mNewSS.getDataRegState())) {
            EventLog.writeEvent(EventLogTags.CDMA_SERVICE_STATE_CHANGE, new Object[]{Integer.valueOf(this.mSS.getVoiceRegState()), Integer.valueOf(this.mSS.getDataRegState()), Integer.valueOf(this.mNewSS.getVoiceRegState()), Integer.valueOf(this.mNewSS.getDataRegState())});
        }
        if (HwTelephonyFactory.getHwNetworkManager().proccessCdmaLteDelayUpdateRegisterStateDone(this, this.mPhone, this.mSS, this.mNewSS)) {
            this.mDelayUpdateGpsState = this.mNewSS.getDataRegState();
            return;
        }
        HwTelephonyFactory.getHwNetworkManager().gprsAttach(this, this.mSS, this.mNewSS);
        ServiceState tss = this.mSS;
        this.mSS = this.mNewSS;
        this.mNewSS = tss;
        this.mNewSS.setStateOutOfService();
        CellLocation tcl = (CdmaCellLocation) this.mCellLoc;
        this.mCellLoc = this.mNewCellLoc;
        this.mNewCellLoc = tcl;
        this.mNewSS.setStateOutOfService();
        if (hasVoiceRadioTechnologyChanged) {
            updatePhoneObject();
        }
        if (hasDataRadioTechnologyChanged) {
            tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
            if (18 == this.mSS.getRilDataRadioTechnology()) {
                log("pollStateDone: IWLAN enabled");
            }
        }
        if (hasRegistered) {
            this.mNetworkAttachedRegistrants.notifyRegistrants();
        }
        if (hasChanged) {
            boolean hasBrandOverride = this.mUiccController.getUiccCard(getPhoneId()) == null ? false : this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() != null;
            if (!hasBrandOverride && this.mCi.getRadioState().isOn() && this.mPhone.isEriFileLoaded() && ((this.mSS.getRilVoiceRadioTechnology() != 14 || this.mPhone.getContext().getResources().getBoolean(17957024)) && !this.mIsSubscriptionFromRuim)) {
                String eriText = this.mSS.getOperatorAlphaLong();
                if (this.mSS.getVoiceRegState() == 0) {
                    eriText = this.mPhone.getCdmaEriText();
                } else if (this.mSS.getVoiceRegState() == 3) {
                    eriText = this.mIccRecords != null ? this.mIccRecords.getServiceProviderName() : null;
                    if (TextUtils.isEmpty(eriText)) {
                        eriText = SystemProperties.get("ro.cdma.home.operator.alpha");
                    }
                } else if (this.mSS.getDataRegState() != 0) {
                    eriText = this.mPhone.getContext().getText(17039589).toString();
                }
                this.mSS.setOperatorAlphaLong(eriText);
            }
            if (this.mUiccApplcation != null && this.mUiccApplcation.getState() == AppState.APPSTATE_READY && this.mIccRecords != null && ((this.mSS.getVoiceRegState() == 0 || this.mSS.getDataRegState() == 0) && this.mSS.getRilVoiceRadioTechnology() != 14)) {
                boolean showSpn = ((RuimRecords) this.mIccRecords).getCsimSpnDisplayCondition();
                int iconIndex = this.mSS.getCdmaEriIconIndex();
                if (showSpn && iconIndex == 1) {
                    if (!(!isInHomeSidNid(this.mSS.getSystemId(), this.mSS.getNetworkId()) || this.mIccRecords == null || TextUtils.isEmpty(this.mIccRecords.getServiceProviderName()))) {
                        this.mSS.setOperatorAlphaLong(this.mIccRecords.getServiceProviderName());
                    }
                }
            }
            tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlphaLong());
            String prevOperatorNumeric = tm.getNetworkOperatorForPhone(this.mPhone.getPhoneId());
            String operatorNumeric = this.mSS.getOperatorNumeric();
            if (isInvalidOperatorNumeric(operatorNumeric)) {
                operatorNumeric = fixUnknownMcc(operatorNumeric, this.mSS.getSystemId());
            }
            tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
            updateCarrierMccMncConfiguration(operatorNumeric, prevOperatorNumeric, this.mPhone.getContext());
            if (isInvalidOperatorNumeric(operatorNumeric)) {
                log("operatorNumeric is null");
                tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), "");
                this.mGotCountryCode = false;
            } else {
                String isoCountryCode = "";
                String mcc = operatorNumeric.substring(0, 3);
                try {
                    isoCountryCode = MccTable.countryCodeForMcc(Integer.parseInt(operatorNumeric.substring(0, 3)));
                } catch (NumberFormatException ex) {
                    loge("countryCodeForMcc error" + ex);
                } catch (StringIndexOutOfBoundsException ex2) {
                    loge("countryCodeForMcc error" + ex2);
                }
                tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), isoCountryCode);
                this.mGotCountryCode = true;
                setOperatorIdd(operatorNumeric);
                if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                    fixTimeZone(isoCountryCode);
                }
            }
            tm.setNetworkRoamingForPhone(this.mPhone.getPhoneId(), !this.mSS.getVoiceRoaming() ? this.mSS.getDataRoaming() : true);
            updateSpnDisplay();
            setRoamingType(this.mSS);
            log("Broadcasting ServiceState : " + this.mSS);
            this.mPhone.notifyServiceStateChanged(this.mSS);
        }
        HwTelephonyFactory.getHwNetworkManager().processCdmaCTNumMatch(this, this.mPhone, this.mSS.getRoaming(), this.mUiccApplcation);
        if (hasCdmaDataConnectionAttached || has4gHandoff) {
            this.mAttachedRegistrants.notifyRegistrants();
        }
        if (hasCdmaDataConnectionDetached) {
            this.mDetachedRegistrants.notifyRegistrants();
        }
        if (hasCdmaDataConnectionChanged || hasDataRadioTechnologyChanged) {
            notifyDataRegStateRilRadioTechnologyChanged();
            if (18 == this.mSS.getRilDataRadioTechnology()) {
                this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_IWLAN_AVAILABLE);
            } else {
                needNotifyData = true;
            }
        }
        if (needNotifyData) {
            this.mPhone.notifyDataConnection(null);
        }
        if (voiceRoaming) {
            this.mVoiceRoamingOnRegistrants.notifyRegistrants();
        }
        if (hasVoiceRoamingOff) {
            this.mVoiceRoamingOffRegistrants.notifyRegistrants();
        }
        if (dataRoaming) {
            this.mDataRoamingOnRegistrants.notifyRegistrants();
        }
        if (hasDataRoamingOff) {
            this.mDataRoamingOffRegistrants.notifyRegistrants();
        }
        if (hasLocationChanged) {
            this.mPhone.notifyLocationChanged();
        }
    }

    private boolean isInHomeSidNid(int sid, int nid) {
        if (isSidsAllZeros() || this.mHomeSystemId.length != this.mHomeNetworkId.length || sid == 0) {
            return true;
        }
        int i = 0;
        while (i < this.mHomeSystemId.length) {
            if (this.mHomeSystemId[i] == sid && (this.mHomeNetworkId[i] == 0 || this.mHomeNetworkId[i] == 65535 || nid == 0 || nid == 65535 || this.mHomeNetworkId[i] == nid)) {
                return true;
            }
            i++;
        }
        return false;
    }

    protected void setOperatorIdd(String operatorNumeric) {
        if (PLUS_TRANFER_IN_MDOEM) {
            log("setOperatorIdd() return. because of PLUS_TRANFER_IN_MDOEM=" + PLUS_TRANFER_IN_MDOEM);
            return;
        }
        String idd = this.mHbpcdUtils.getIddByMcc(Integer.parseInt(operatorNumeric.substring(0, 3)));
        if (idd == null || idd.isEmpty()) {
            this.mPhone.setSystemProperty("gsm.operator.idpstring", HwCustPlusAndIddNddConvertUtils.PLUS_PREFIX);
        } else {
            this.mPhone.setSystemProperty("gsm.operator.idpstring", idd);
        }
    }

    protected boolean isInvalidOperatorNumeric(String operatorNumeric) {
        if (operatorNumeric == null || operatorNumeric.length() < 5) {
            return true;
        }
        return operatorNumeric.startsWith(INVALID_MCC);
    }

    protected String fixUnknownMcc(String operatorNumeric, int sid) {
        int i = 0;
        if (sid <= 0) {
            return operatorNumeric;
        }
        boolean isNitzTimeZone = false;
        int timeZone = 0;
        if (this.mSavedTimeZone != null) {
            timeZone = TimeZone.getTimeZone(this.mSavedTimeZone).getRawOffset() / MS_PER_HOUR;
            isNitzTimeZone = true;
        } else {
            TimeZone tzone = getNitzTimeZone(this.mZoneOffset, this.mZoneDst, this.mZoneTime);
            if (tzone != null) {
                timeZone = tzone.getRawOffset() / MS_PER_HOUR;
            }
        }
        HbpcdUtils hbpcdUtils = this.mHbpcdUtils;
        if (this.mZoneDst) {
            i = 1;
        }
        int mcc = hbpcdUtils.getMcc(sid, timeZone, i, isNitzTimeZone);
        if (mcc > 0) {
            operatorNumeric = Integer.toString(mcc) + DEFAULT_MNC;
        }
        return operatorNumeric;
    }

    protected void fixTimeZone(String isoCountryCode) {
        fixTimeZone(isoCountryCode, null);
    }

    protected void fixTimeZone(String isoCountryCode, TimeZone mccZone) {
        TimeZone zone = mccZone;
        String zoneName = SystemProperties.get(TIMEZONE_PROPERTY);
        log("fixTimeZone zoneName='" + zoneName + "' mZoneOffset=" + this.mZoneOffset + " mZoneDst=" + this.mZoneDst + " iso-cc='" + isoCountryCode + "' iso-cc-idx=" + Arrays.binarySearch(GMT_COUNTRY_CODES, isoCountryCode));
        if ("".equals(isoCountryCode) && this.mNeedFixZoneAfterNitz) {
            zone = getNitzTimeZone(this.mZoneOffset, this.mZoneDst, this.mZoneTime);
            log("pollStateDone: using NITZ TimeZone");
        } else if (this.mZoneOffset != 0 || this.mZoneDst || zoneName == null || zoneName.length() <= 0 || Arrays.binarySearch(GMT_COUNTRY_CODES, isoCountryCode) >= 0) {
            zone = TimeUtils.getTimeZone(this.mZoneOffset, this.mZoneDst, this.mZoneTime, isoCountryCode);
            log("fixTimeZone: using getTimeZone(off, dst, time, iso)");
            if (zone == null) {
                zone = TimeUtils.getTimeZone(this.mZoneOffset, !this.mZoneDst, this.mZoneTime, isoCountryCode);
            }
        } else {
            if (mccZone == null) {
                zone = TimeZone.getDefault();
            }
            log("pollStateDone: default TimeZone is " + zone);
            if (this.mNeedFixZoneAfterNitz) {
                long ctm = System.currentTimeMillis();
                long tzOffset = (long) zone.getOffset(ctm);
                log("fixTimeZone: tzOffset=" + tzOffset + " ltod=" + TimeUtils.logTimeOfDay(ctm));
                if (getAutoTime()) {
                    long adj = ctm - tzOffset;
                    log("fixTimeZone: adj ltod=" + TimeUtils.logTimeOfDay(adj));
                    setAndBroadcastNetworkSetTime(adj);
                } else {
                    this.mSavedTime -= tzOffset;
                    log("fixTimeZone: adj mSavedTime=" + this.mSavedTime);
                }
            }
            log("fixTimeZone: using default TimeZone");
        }
        this.mNeedFixZoneAfterNitz = false;
        if (zone != null) {
            log("fixTimeZone: zone != null zone.getID=" + zone.getID());
            if (getAutoTimeZone()) {
                setAndBroadcastNetworkSetTimeZone(zone.getID());
            } else {
                log("fixTimeZone: skip changing zone as getAutoTimeZone was false");
            }
            saveNitzTimeZone(zone.getID());
            return;
        }
        log("fixTimeZone: zone == null, do nothing for zone");
    }

    private boolean isGprsConsistent(int dataRegState, int voiceRegState) {
        return voiceRegState != 0 || dataRegState == 0;
    }

    private TimeZone getNitzTimeZone(int offset, boolean dst, long when) {
        TimeZone guess = findTimeZone(offset, dst, when);
        if (guess == null) {
            guess = findTimeZone(offset, !dst, when);
        }
        log("getNitzTimeZone returning " + (guess == null ? guess : guess.getID()));
        return guess;
    }

    private TimeZone findTimeZone(int offset, boolean dst, long when) {
        int rawOffset = offset;
        if (dst) {
            rawOffset = offset - MS_PER_HOUR;
        }
        String[] zones = TimeZone.getAvailableIDs(rawOffset);
        Date d = new Date(when);
        for (String zone : zones) {
            TimeZone tz = TimeZone.getTimeZone(zone);
            if (tz.getOffset(when) == offset && tz.inDaylightTime(d) == dst) {
                return tz;
            }
        }
        return null;
    }

    private int regCodeToServiceState(int code) {
        switch (code) {
            case 0:
            case 2:
            case 3:
            case 4:
            case 10:
            case 12:
            case 13:
            case 14:
                return 1;
            case 1:
            case 5:
                return 0;
            default:
                loge("regCodeToServiceState: unexpected service state " + code);
                return 1;
        }
    }

    private boolean regCodeIsRoaming(int code) {
        return 5 == code;
    }

    private boolean isSameOperatorNameFromSimAndSS(ServiceState s) {
        String spn = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNameForPhone(getPhoneId());
        return !(!TextUtils.isEmpty(spn) ? spn.equalsIgnoreCase(s.getOperatorAlphaLong()) : false) ? !TextUtils.isEmpty(spn) ? spn.equalsIgnoreCase(s.getOperatorAlphaShort()) : false : true;
    }

    private boolean isSameNamedOperators(ServiceState s) {
        return currentMccEqualsSimMcc(s) ? isSameOperatorNameFromSimAndSS(s) : false;
    }

    private boolean currentMccEqualsSimMcc(ServiceState s) {
        boolean equalsMcc = true;
        try {
            equalsMcc = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNumericForPhone(getPhoneId()).substring(0, 3).equals(s.getOperatorNumeric().substring(0, 3));
        } catch (Exception e) {
        }
        return equalsMcc;
    }

    private boolean isOperatorConsideredNonRoaming(ServiceState s) {
        return false;
    }

    private boolean isOperatorConsideredRoaming(ServiceState s) {
        String operatorNumeric = s.getOperatorNumeric();
        String[] numericArray = this.mPhone.getContext().getResources().getStringArray(17236028);
        if (numericArray.length == 0 || operatorNumeric == null) {
            return false;
        }
        for (String numeric : numericArray) {
            if (operatorNumeric.startsWith(numeric)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkForRoamingForIndianOperators(ServiceState s) {
        String simNumeric = SystemProperties.get("gsm.sim.operator.numeric", "");
        String operatorNumeric = s.getOperatorNumeric();
        try {
            String simMCC = simNumeric.substring(0, 3);
            String operatorMCC = operatorNumeric.substring(0, 3);
            if ((simMCC.equals("404") || simMCC.equals("405")) && (operatorMCC.equals("404") || operatorMCC.equals("405"))) {
                return true;
            }
        } catch (RuntimeException e) {
        }
        return false;
    }

    private void onRestrictedStateChanged(AsyncResult ar) {
        boolean z = true;
        RestrictedState newRs = new RestrictedState();
        if (ar.exception == null) {
            boolean z2;
            int state = ar.result[0];
            if ((state & 1) != 0) {
                z2 = true;
            } else if ((state & 4) != 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            newRs.setCsEmergencyRestricted(z2);
            if (this.mUiccApplcation != null && this.mUiccApplcation.getState() == AppState.APPSTATE_READY) {
                if ((state & 2) != 0) {
                    z2 = true;
                } else if ((state & 4) != 0) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                newRs.setCsNormalRestricted(z2);
                if ((state & 16) == 0) {
                    z = false;
                }
                newRs.setPsRestricted(z);
            }
            if (!this.mRestrictedState.isPsRestricted() && newRs.isPsRestricted()) {
                this.mPsRestrictEnabledRegistrants.notifyRegistrants();
                setNotification(1001);
            } else if (this.mRestrictedState.isPsRestricted() && !newRs.isPsRestricted()) {
                this.mPsRestrictDisabledRegistrants.notifyRegistrants();
                setNotification(1002);
            }
            if (this.mRestrictedState.isCsRestricted()) {
                if (!newRs.isCsRestricted()) {
                    setNotification(1004);
                } else if (!newRs.isCsNormalRestricted()) {
                    setNotification(1006);
                } else if (!newRs.isCsEmergencyRestricted()) {
                    setNotification(1005);
                }
            } else if (!this.mRestrictedState.isCsEmergencyRestricted() || this.mRestrictedState.isCsNormalRestricted()) {
                if (this.mRestrictedState.isCsEmergencyRestricted() || !this.mRestrictedState.isCsNormalRestricted()) {
                    if (newRs.isCsRestricted()) {
                        setNotification(1003);
                    } else if (newRs.isCsEmergencyRestricted()) {
                        setNotification(1006);
                    } else if (newRs.isCsNormalRestricted()) {
                        setNotification(1005);
                    }
                } else if (!newRs.isCsRestricted()) {
                    setNotification(1004);
                } else if (newRs.isCsRestricted()) {
                    setNotification(1003);
                } else if (newRs.isCsEmergencyRestricted()) {
                    setNotification(1006);
                }
            } else if (!newRs.isCsRestricted()) {
                setNotification(1004);
            } else if (newRs.isCsRestricted()) {
                setNotification(1003);
            } else if (newRs.isCsNormalRestricted()) {
                setNotification(1005);
            }
            this.mRestrictedState = newRs;
        }
        log("onRestrictedStateChanged: X rs " + this.mRestrictedState);
    }

    public CellLocation getCellLocation() {
        if (((GsmCellLocation) this.mCellLoc).getLac() < 0 || ((GsmCellLocation) this.mCellLoc).getCid() < 0) {
            List<CellInfo> result = getAllCellInfo();
            if (result != null) {
                GsmCellLocation cellLocOther = new GsmCellLocation();
                for (CellInfo ci : result) {
                    if (ci instanceof CellInfoGsm) {
                        CellIdentityGsm cellIdentityGsm = ((CellInfoGsm) ci).getCellIdentity();
                        cellLocOther.setLacAndCid(cellIdentityGsm.getLac(), cellIdentityGsm.getCid());
                        cellLocOther.setPsc(cellIdentityGsm.getPsc());
                        log("getCellLocation(): X ret GSM info=" + cellLocOther);
                        return cellLocOther;
                    } else if (ci instanceof CellInfoWcdma) {
                        CellIdentityWcdma cellIdentityWcdma = ((CellInfoWcdma) ci).getCellIdentity();
                        cellLocOther.setLacAndCid(cellIdentityWcdma.getLac(), cellIdentityWcdma.getCid());
                        cellLocOther.setPsc(cellIdentityWcdma.getPsc());
                        log("getCellLocation(): X ret WCDMA info=" + cellLocOther);
                        return cellLocOther;
                    } else if ((ci instanceof CellInfoLte) && (cellLocOther.getLac() < 0 || cellLocOther.getCid() < 0)) {
                        CellIdentityLte cellIdentityLte = ((CellInfoLte) ci).getCellIdentity();
                        if (!(cellIdentityLte.getTac() == Integer.MAX_VALUE || cellIdentityLte.getCi() == Integer.MAX_VALUE)) {
                            cellLocOther.setLacAndCid(cellIdentityLte.getTac(), cellIdentityLte.getCi());
                            cellLocOther.setPsc(0);
                            log("getCellLocation(): possible LTE cellLocOther=" + cellLocOther);
                        }
                    }
                }
                log("getCellLocation(): X ret best answer cellLocOther=" + cellLocOther);
                return cellLocOther;
            }
            log("getCellLocation(): X empty mCellLoc and CellInfo mCellLoc=" + this.mCellLoc);
            return this.mCellLoc;
        }
        log("getCellLocation(): X good mCellLoc=" + this.mCellLoc);
        return this.mCellLoc;
    }

    private void setTimeFromNITZString(String nitz, long nitzReceiveTime) {
        long start = SystemClock.elapsedRealtime();
        log("NITZ: " + nitz + "," + nitzReceiveTime + " start=" + start + " delay=" + (start - nitzReceiveTime));
        long end;
        try {
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            c.clear();
            c.set(16, 0);
            String[] nitzSubs = nitz.split("[/:,+-]");
            int year = Integer.parseInt(nitzSubs[0]) + NITZ_UPDATE_DIFF_DEFAULT;
            if (year > MAX_NITZ_YEAR) {
                loge("NITZ year: " + year + " exceeds limit, skip NITZ time update");
                return;
            }
            int dst;
            String ignore;
            long millisSinceNitzReceived;
            long gained;
            long timeSinceLastUpdate;
            int nitzUpdateSpacing;
            int nitzUpdateDiff;
            c.set(1, year);
            c.set(2, Integer.parseInt(nitzSubs[1]) - 1);
            c.set(5, Integer.parseInt(nitzSubs[2]));
            c.set(10, Integer.parseInt(nitzSubs[3]));
            c.set(12, Integer.parseInt(nitzSubs[4]));
            c.set(13, Integer.parseInt(nitzSubs[5]));
            boolean sign = nitz.indexOf(EVENT_CHANGE_IMS_STATE) == -1;
            int tzOffset = Integer.parseInt(nitzSubs[6]);
            if (nitzSubs.length >= 8) {
                dst = Integer.parseInt(nitzSubs[7]);
            } else {
                dst = 0;
            }
            tzOffset = ((((sign ? 1 : -1) * tzOffset) * 15) * 60) * 1000;
            TimeZone zone = null;
            if (nitzSubs.length >= 9) {
                zone = TimeZone.getTimeZone(nitzSubs[8].replace('!', '/'));
            }
            String iso = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getNetworkCountryIsoForPhone(this.mPhone.getPhoneId());
            if (zone == null && this.mGotCountryCode) {
                if (iso == null || iso.length() <= 0) {
                    zone = getNitzTimeZone(tzOffset, dst != 0, c.getTimeInMillis());
                } else {
                    zone = TimeUtils.getTimeZone(tzOffset, dst != 0, c.getTimeInMillis(), iso);
                }
            }
            if (zone != null && this.mZoneOffset == tzOffset) {
                if (this.mZoneDst != (dst != 0)) {
                }
                log("NITZ: tzOffset=" + tzOffset + " dst=" + dst + " zone=" + (zone == null ? zone.getID() : "NULL") + " iso=" + iso + " mGotCountryCode=" + this.mGotCountryCode + " mNeedFixZoneAfterNitz=" + this.mNeedFixZoneAfterNitz);
                if (zone != null) {
                    if (getAutoTimeZone()) {
                        setAndBroadcastNetworkSetTimeZone(zone.getID());
                    }
                    saveNitzTimeZone(zone.getID());
                }
                ignore = SystemProperties.get("gsm.ignore-nitz");
                if (ignore == null && ignore.equals("yes")) {
                    log("NITZ: Not setting clock because gsm.ignore-nitz is set");
                    return;
                }
                this.mWakeLock.acquire();
                if (!this.mPhone.isPhoneTypeGsm() || getAutoTime()) {
                    millisSinceNitzReceived = SystemClock.elapsedRealtime() - nitzReceiveTime;
                    if (millisSinceNitzReceived < 0) {
                        log("NITZ: not setting time, clock has rolled backwards since NITZ time was received, " + nitz);
                        end = SystemClock.elapsedRealtime();
                        log("NITZ: end=" + end + " dur=" + (end - start));
                        this.mWakeLock.release();
                    } else if (millisSinceNitzReceived <= 2147483647L) {
                        log("NITZ: not setting time, processing has taken " + (millisSinceNitzReceived / 86400000) + " days");
                        end = SystemClock.elapsedRealtime();
                        log("NITZ: end=" + end + " dur=" + (end - start));
                        this.mWakeLock.release();
                    } else {
                        c.add(14, (int) millisSinceNitzReceived);
                        log("NITZ: Setting time of day to " + c.getTime() + " NITZ receive delay(ms): " + millisSinceNitzReceived + " gained(ms): " + (c.getTimeInMillis() - System.currentTimeMillis()) + " from " + nitz);
                        if (this.mPhone.isPhoneTypeGsm()) {
                            if (getAutoTime()) {
                                gained = c.getTimeInMillis() - System.currentTimeMillis();
                                timeSinceLastUpdate = SystemClock.elapsedRealtime() - this.mSavedAtTime;
                                nitzUpdateSpacing = Global.getInt(this.mCr, "nitz_update_spacing", this.mNitzUpdateSpacing);
                                nitzUpdateDiff = Global.getInt(this.mCr, "nitz_update_diff", this.mNitzUpdateDiff);
                                if (this.mSavedAtTime != 0 || timeSinceLastUpdate > ((long) nitzUpdateSpacing) || Math.abs(gained) > ((long) nitzUpdateDiff)) {
                                    log("NITZ: Auto updating time of day to " + c.getTime() + " NITZ receive delay=" + millisSinceNitzReceived + "ms gained=" + gained + "ms from " + nitz);
                                    setAndBroadcastNetworkSetTime(c.getTimeInMillis());
                                } else {
                                    log("NITZ: ignore, a previous update was " + timeSinceLastUpdate + "ms ago and gained=" + gained + "ms");
                                    end = SystemClock.elapsedRealtime();
                                    log("NITZ: end=" + end + " dur=" + (end - start));
                                    this.mWakeLock.release();
                                    return;
                                }
                            }
                        } else if (Math.abs(c.getTimeInMillis() - System.currentTimeMillis()) < 3000) {
                            setAndBroadcastNetworkSetTime(c.getTimeInMillis());
                            Rlog.i(this.LOG_TAG, "NITZ: Setting time ");
                        } else {
                            Rlog.i(this.LOG_TAG, "NITZ: skip Setting time");
                        }
                    }
                }
                SystemProperties.set("gsm.nitz.time", String.valueOf(c.getTimeInMillis()));
                SystemProperties.set("gsm.nitz.timereference", String.valueOf(SystemClock.elapsedRealtime()));
                saveNitzTime(c.getTimeInMillis());
                this.mNitzUpdatedTime = true;
                end = SystemClock.elapsedRealtime();
                log("NITZ: end=" + end + " dur=" + (end - start));
                this.mWakeLock.release();
            }
            this.mNeedFixZoneAfterNitz = true;
            this.mZoneOffset = tzOffset;
            this.mZoneDst = dst != 0;
            this.mZoneTime = c.getTimeInMillis();
            if (zone == null) {
            }
            log("NITZ: tzOffset=" + tzOffset + " dst=" + dst + " zone=" + (zone == null ? zone.getID() : "NULL") + " iso=" + iso + " mGotCountryCode=" + this.mGotCountryCode + " mNeedFixZoneAfterNitz=" + this.mNeedFixZoneAfterNitz);
            if (zone != null) {
                if (getAutoTimeZone()) {
                    setAndBroadcastNetworkSetTimeZone(zone.getID());
                }
                saveNitzTimeZone(zone.getID());
            }
            ignore = SystemProperties.get("gsm.ignore-nitz");
            if (ignore == null) {
            }
            this.mWakeLock.acquire();
            millisSinceNitzReceived = SystemClock.elapsedRealtime() - nitzReceiveTime;
            if (millisSinceNitzReceived < 0) {
                log("NITZ: not setting time, clock has rolled backwards since NITZ time was received, " + nitz);
                end = SystemClock.elapsedRealtime();
                log("NITZ: end=" + end + " dur=" + (end - start));
                this.mWakeLock.release();
            } else if (millisSinceNitzReceived <= 2147483647L) {
                c.add(14, (int) millisSinceNitzReceived);
                log("NITZ: Setting time of day to " + c.getTime() + " NITZ receive delay(ms): " + millisSinceNitzReceived + " gained(ms): " + (c.getTimeInMillis() - System.currentTimeMillis()) + " from " + nitz);
                if (this.mPhone.isPhoneTypeGsm()) {
                    if (getAutoTime()) {
                        gained = c.getTimeInMillis() - System.currentTimeMillis();
                        timeSinceLastUpdate = SystemClock.elapsedRealtime() - this.mSavedAtTime;
                        nitzUpdateSpacing = Global.getInt(this.mCr, "nitz_update_spacing", this.mNitzUpdateSpacing);
                        nitzUpdateDiff = Global.getInt(this.mCr, "nitz_update_diff", this.mNitzUpdateDiff);
                        if (this.mSavedAtTime != 0) {
                        }
                        log("NITZ: Auto updating time of day to " + c.getTime() + " NITZ receive delay=" + millisSinceNitzReceived + "ms gained=" + gained + "ms from " + nitz);
                        setAndBroadcastNetworkSetTime(c.getTimeInMillis());
                    }
                    SystemProperties.set("gsm.nitz.time", String.valueOf(c.getTimeInMillis()));
                    SystemProperties.set("gsm.nitz.timereference", String.valueOf(SystemClock.elapsedRealtime()));
                    saveNitzTime(c.getTimeInMillis());
                    this.mNitzUpdatedTime = true;
                    end = SystemClock.elapsedRealtime();
                    log("NITZ: end=" + end + " dur=" + (end - start));
                    this.mWakeLock.release();
                }
                if (Math.abs(c.getTimeInMillis() - System.currentTimeMillis()) < 3000) {
                    Rlog.i(this.LOG_TAG, "NITZ: skip Setting time");
                } else {
                    setAndBroadcastNetworkSetTime(c.getTimeInMillis());
                    Rlog.i(this.LOG_TAG, "NITZ: Setting time ");
                }
                SystemProperties.set("gsm.nitz.time", String.valueOf(c.getTimeInMillis()));
                SystemProperties.set("gsm.nitz.timereference", String.valueOf(SystemClock.elapsedRealtime()));
                saveNitzTime(c.getTimeInMillis());
                this.mNitzUpdatedTime = true;
                end = SystemClock.elapsedRealtime();
                log("NITZ: end=" + end + " dur=" + (end - start));
                this.mWakeLock.release();
            } else {
                log("NITZ: not setting time, processing has taken " + (millisSinceNitzReceived / 86400000) + " days");
                end = SystemClock.elapsedRealtime();
                log("NITZ: end=" + end + " dur=" + (end - start));
                this.mWakeLock.release();
            }
        } catch (RuntimeException ex) {
            loge("NITZ: Parsing NITZ time " + nitz + " ex=" + ex);
        } catch (Throwable th) {
            end = SystemClock.elapsedRealtime();
            log("NITZ: end=" + end + " dur=" + (end - start));
            this.mWakeLock.release();
        }
    }

    private boolean getAutoTime() {
        boolean z = true;
        try {
            if (Global.getInt(this.mCr, "auto_time") <= 0) {
                z = false;
            }
            return z;
        } catch (SettingNotFoundException e) {
            return true;
        }
    }

    protected boolean getAutoTimeZone() {
        boolean z = true;
        try {
            if (Global.getInt(this.mCr, "auto_time_zone") <= 0) {
                z = false;
            }
            return z;
        } catch (SettingNotFoundException e) {
            return true;
        }
    }

    private void saveNitzTimeZone(String zoneId) {
        this.mSavedTimeZone = zoneId;
        HwTelephonyFactory.getHwNetworkManager().saveNitzTimeZoneToDB(this.mCr, zoneId);
    }

    private void saveNitzTime(long time) {
        this.mSavedTime = time;
        this.mSavedAtTime = SystemClock.elapsedRealtime();
    }

    protected void setAndBroadcastNetworkSetTimeZone(String zoneId) {
        log("setAndBroadcastNetworkSetTimeZone: setTimeZone=" + zoneId);
        ((AlarmManager) this.mPhone.getContext().getSystemService("alarm")).setTimeZone(zoneId);
        Intent intent = new Intent("android.intent.action.NETWORK_SET_TIMEZONE");
        intent.addFlags(536870912);
        intent.putExtra("time-zone", zoneId);
        this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void setAndBroadcastNetworkSetTime(long time) {
        log("setAndBroadcastNetworkSetTime: time=" + time + "ms");
        SystemClock.setCurrentTimeMillis(time);
        Intent intent = new Intent("android.intent.action.NETWORK_SET_TIME");
        intent.addFlags(536870912);
        intent.putExtra("time", time);
        this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void revertToNitzTime() {
        if (Global.getInt(this.mCr, "auto_time", 0) != 0) {
            log("Reverting to NITZ Time: mSavedTime=" + this.mSavedTime + " mSavedAtTime=" + this.mSavedAtTime);
            if (!(this.mSavedTime == 0 || this.mSavedAtTime == 0)) {
                setAndBroadcastNetworkSetTime(this.mSavedTime + (SystemClock.elapsedRealtime() - this.mSavedAtTime));
            }
        }
    }

    private void revertToNitzTimeZone() {
        if (Global.getInt(this.mCr, "auto_time_zone", 0) != 0) {
            log("Reverting to NITZ TimeZone: tz='" + this.mSavedTimeZone);
            if (this.mSavedTimeZone != null) {
                setAndBroadcastNetworkSetTimeZone(this.mSavedTimeZone);
            }
        }
    }

    private void setNotification(int notifyType) {
        if (SystemProperties.getBoolean("ro.hwpp.cell_access_report", false)) {
            log("setNotification: create notification " + notifyType);
            if (this.mPhone.getContext().getResources().getBoolean(17956958)) {
                Context context = this.mPhone.getContext();
                CharSequence details = "";
                CharSequence title = context.getText(17039556);
                int notificationId = CS_NOTIFICATION;
                switch (notifyType) {
                    case 1001:
                        if (((long) SubscriptionManager.getDefaultDataSubscriptionId()) == ((long) this.mPhone.getSubId())) {
                            notificationId = PS_NOTIFICATION;
                            details = context.getText(17039556);
                            break;
                        }
                        return;
                    case 1002:
                        notificationId = PS_NOTIFICATION;
                        break;
                    case 1003:
                        details = context.getText(17039559);
                        break;
                    case 1005:
                        details = context.getText(17039558);
                        break;
                    case 1006:
                        details = context.getText(17039557);
                        break;
                }
                log("setNotification: put notification " + title + " / " + details);
                this.mNotification = new Builder(context).setWhen(System.currentTimeMillis()).setAutoCancel(true).setSmallIcon(17301642).setTicker(title).setColor(context.getResources().getColor(17170519)).setContentTitle(title).setContentText(details).build();
                NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
                if (notifyType == 1002 || notifyType == 1004) {
                    notificationManager.cancel(notificationId);
                } else {
                    notificationManager.notify(notificationId, this.mNotification);
                }
                return;
            }
            log("Ignore all the notifications");
        }
    }

    private UiccCardApplication getUiccCardApplication() {
        if (this.mPhone.isPhoneTypeGsm()) {
            return this.mUiccController.getUiccCardApplication(this.mPhone.getPhoneId(), 1);
        }
        return this.mUiccController.getUiccCardApplication(this.mPhone.getPhoneId(), 2);
    }

    private void queueNextSignalStrengthPoll() {
        if (!this.mDontPollSignalStrength && this.mDefaultDisplayState == 2) {
            Message msg = obtainMessage();
            msg.what = 10;
            sendMessageDelayed(msg, 20000);
        }
    }

    private void notifyCdmaSubscriptionInfoReady() {
        if (this.mCdmaForSubscriptionInfoReadyRegistrants != null) {
            log("CDMA_SUBSCRIPTION: call notifyRegistrants()");
            this.mCdmaForSubscriptionInfoReadyRegistrants.notifyRegistrants();
        }
    }

    public void registerForDataConnectionAttached(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mAttachedRegistrants.add(r);
        if (getCurrentDataConnectionState() == 0) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForDataConnectionAttached(Handler h) {
        this.mAttachedRegistrants.remove(h);
    }

    public void registerForDataConnectionDetached(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mDetachedRegistrants.add(r);
        if (getCurrentDataConnectionState() != 0) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForDataConnectionDetached(Handler h) {
        this.mDetachedRegistrants.remove(h);
    }

    public void registerForDataRegStateOrRatChanged(Handler h, int what, Object obj) {
        this.mDataRegStateOrRatChangedRegistrants.add(new Registrant(h, what, obj));
        notifyDataRegStateRilRadioTechnologyChanged();
    }

    public void unregisterForDataRegStateOrRatChanged(Handler h) {
        this.mDataRegStateOrRatChangedRegistrants.remove(h);
    }

    public void registerForNetworkAttached(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mNetworkAttachedRegistrants.add(r);
        if (this.mSS.getVoiceRegState() == 0) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForNetworkAttached(Handler h) {
        this.mNetworkAttachedRegistrants.remove(h);
    }

    public void registerForPsRestrictedEnabled(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mPsRestrictEnabledRegistrants.add(r);
        if (this.mRestrictedState.isPsRestricted()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForPsRestrictedEnabled(Handler h) {
        this.mPsRestrictEnabledRegistrants.remove(h);
    }

    public void registerForPsRestrictedDisabled(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mPsRestrictDisabledRegistrants.add(r);
        if (this.mRestrictedState.isPsRestricted()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForPsRestrictedDisabled(Handler h) {
        this.mPsRestrictDisabledRegistrants.remove(h);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void powerOffRadioSafely(DcTracker dcTracker) {
        synchronized (this) {
            if (RESET_PROFILE && this.mPhone.getContext() != null && Global.getInt(this.mPhone.getContext().getContentResolver(), "airplane_mode_on", -1) == 0) {
                Rlog.d(this.LOG_TAG, "powerOffRadioSafely, it is not airplaneMode, resetProfile.");
                this.mCi.resetProfile(null);
            }
            if (!this.mPendingRadioPowerOffAfterDataOff) {
                boolean isDisconnected;
                Message msg;
                int i;
                if (this.mPhone.isPhoneTypeGsm() || this.mPhone.isPhoneTypeCdmaLte()) {
                    int dds = SubscriptionManager.getDefaultDataSubscriptionId();
                    isDisconnected = false;
                    if (HW_FAST_SET_RADIO_OFF) {
                        isDisconnected = dcTracker.isDisconnectedOrConnecting();
                    }
                    if (isDisconnected || (dcTracker.isDisconnected() && (dds == this.mPhone.getSubId() || ((dds != this.mPhone.getSubId() && ProxyController.getInstance().isDataDisconnected(dds)) || !SubscriptionManager.isValidSubscriptionId(dds))))) {
                        dcTracker.cleanUpAllConnections(PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
                        log("Data disconnected, turn off radio right away.");
                        hangupAndPowerOff();
                    } else {
                        if (this.mPhone.isInCall()) {
                            this.mPhone.mCT.mRingingCall.hangupIfAlive();
                            this.mPhone.mCT.mBackgroundCall.hangupIfAlive();
                            this.mPhone.mCT.mForegroundCall.hangupIfAlive();
                        }
                        dcTracker.cleanUpAllConnections(PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
                        if (!(dds == this.mPhone.getSubId() || ProxyController.getInstance().isDataDisconnected(dds))) {
                            log("Data is active on DDS.  Wait for all data disconnect");
                            ProxyController.getInstance().registerForAllDataDisconnected(dds, this, 49, null);
                            this.mPendingRadioPowerOffAfterDataOff = true;
                        }
                        msg = Message.obtain(this);
                        msg.what = 38;
                        i = this.mPendingRadioPowerOffAfterDataOffTag + 1;
                        this.mPendingRadioPowerOffAfterDataOffTag = i;
                        msg.arg1 = i;
                        if (sendMessageDelayed(msg, 30000)) {
                            log("Wait upto 30s for data to disconnect, then turn off radio.");
                            acquireWakeLock();
                            this.mPendingRadioPowerOffAfterDataOff = true;
                        } else {
                            log("Cannot send delayed Msg, turn off radio right away.");
                            hangupAndPowerOff();
                            this.mPendingRadioPowerOffAfterDataOff = false;
                        }
                    }
                } else {
                    String[] networkNotClearData = this.mPhone.getContext().getResources().getStringArray(17236037);
                    String currentNetwork = this.mSS.getOperatorNumeric();
                    if (!(networkNotClearData == null || currentNetwork == null)) {
                        for (Object equals : networkNotClearData) {
                            if (currentNetwork.equals(equals)) {
                                log("Not disconnecting data for " + currentNetwork);
                                hangupAndPowerOff();
                                return;
                            }
                        }
                    }
                    if (HW_FAST_SET_RADIO_OFF) {
                        isDisconnected = dcTracker.isDisconnectedOrConnecting();
                    } else {
                        isDisconnected = dcTracker.isDisconnected();
                    }
                    if (isDisconnected) {
                        dcTracker.cleanUpAllConnections(PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
                        log("Data disconnected, turn off radio right away.");
                        hangupAndPowerOff();
                    } else {
                        dcTracker.cleanUpAllConnections(PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
                        msg = Message.obtain(this);
                        msg.what = 38;
                        i = this.mPendingRadioPowerOffAfterDataOffTag + 1;
                        this.mPendingRadioPowerOffAfterDataOffTag = i;
                        msg.arg1 = i;
                        if (sendMessageDelayed(msg, 30000)) {
                            log("Wait upto 30s for data to disconnect, then turn off radio.");
                            acquireWakeLock();
                            this.mPendingRadioPowerOffAfterDataOff = true;
                        } else {
                            log("Cannot send delayed Msg, turn off radio right away.");
                            hangupAndPowerOff();
                        }
                    }
                }
            }
        }
    }

    public boolean processPendingRadioPowerOffAfterDataOff() {
        synchronized (this) {
            if (this.mPendingRadioPowerOffAfterDataOff) {
                HwTelephonyFactory.getHwNetworkManager().delaySendDetachAfterDataOff(this.mPhone);
                this.mPendingRadioPowerOffAfterDataOffTag++;
                hangupAndPowerOff();
                this.mPendingRadioPowerOffAfterDataOff = false;
                return true;
            }
            return false;
        }
    }

    protected boolean onSignalStrengthResult(AsyncResult ar) {
        boolean isGsm = false;
        if (this.mPhone.isPhoneTypeGsm() || (this.mPhone.isPhoneTypeCdmaLte() && this.mSS.getRilDataRadioTechnology() == 14)) {
            isGsm = true;
        }
        if (FEATURE_DELAY_UPDATE_SIGANL_STENGTH) {
            return onSignalStrengthResultHW(ar, isGsm);
        }
        if (ar.exception != null || ar.result == null) {
            log("onSignalStrengthResult() Exception from RIL : " + ar.exception);
            this.mSignalStrength = new SignalStrength(isGsm);
        } else {
            this.mSignalStrength = (SignalStrength) ar.result;
            this.mSignalStrength.validateInput();
            this.mSignalStrength.setGsm(isGsm);
        }
        HwTelephonyFactory.getHwNetworkManager().updateHwnff(this, this.mSignalStrength);
        return notifySignalStrength();
    }

    protected boolean onSignalStrengthResultHW(AsyncResult ar, boolean isGsm) {
        SignalStrength newSignalStrength;
        SignalStrength oldSignalStrength = this.mSignalStrength;
        if (ar.exception != null || ar.result == null) {
            log("onSignalStrengthResult() Exception from RIL : " + ar.exception);
            newSignalStrength = new SignalStrength(isGsm);
        } else {
            newSignalStrength = ar.result;
            newSignalStrength.validateInput();
            newSignalStrength.setGsm(isGsm);
        }
        HwTelephonyFactory.getHwNetworkManager().updateHwnff(this, newSignalStrength);
        if (this.mPhone.isPhoneTypeGsm()) {
            return HwTelephonyFactory.getHwNetworkManager().notifyGsmSignalStrength(this, this.mPhone, oldSignalStrength, newSignalStrength);
        }
        newSignalStrength.setCdma(true);
        return HwTelephonyFactory.getHwNetworkManager().notifyCdmaSignalStrength(this, this.mPhone, oldSignalStrength, newSignalStrength);
    }

    protected void hangupAndPowerOff() {
        if (!this.mPhone.isPhoneTypeGsm() || this.mPhone.isInCall()) {
            this.mPhone.mCT.mRingingCall.hangupIfAlive();
            this.mPhone.mCT.mBackgroundCall.hangupIfAlive();
            this.mPhone.mCT.mForegroundCall.hangupIfAlive();
        }
        this.mCi.setRadioPower(false, null);
    }

    protected void cancelPollState() {
        this.mPollingContext = new int[1];
    }

    protected boolean shouldFixTimeZoneNow(Phone phone, String operatorNumeric, String prevOperatorNumeric, boolean needToFixTimeZone) {
        try {
            int prevMcc;
            int mcc = Integer.parseInt(operatorNumeric.substring(0, 3));
            try {
                if (TextUtils.isEmpty(prevOperatorNumeric)) {
                    prevMcc = mcc + 1;
                } else {
                    prevMcc = Integer.parseInt(prevOperatorNumeric.substring(0, 3));
                }
            } catch (Exception e) {
                prevMcc = mcc + 1;
            }
            boolean iccCardExist = false;
            if (this.mUiccApplcation != null) {
                iccCardExist = this.mUiccApplcation.getState() != AppState.APPSTATE_UNKNOWN;
            }
            boolean z = (!iccCardExist || mcc == prevMcc) ? needToFixTimeZone : true;
            log("shouldFixTimeZoneNow: retVal=" + z + " iccCardExist=" + iccCardExist + " operatorNumeric=" + operatorNumeric + " mcc=" + mcc + " prevOperatorNumeric=" + prevOperatorNumeric + " prevMcc=" + prevMcc + " needToFixTimeZone=" + needToFixTimeZone + " ltod=" + TimeUtils.logTimeOfDay(System.currentTimeMillis()));
            return z;
        } catch (Exception e2) {
            log("shouldFixTimeZoneNow: no mcc, operatorNumeric=" + operatorNumeric + " retVal=false");
            return false;
        }
    }

    public String getSystemProperty(String property, String defValue) {
        return TelephonyManager.getTelephonyProperty(this.mPhone.getPhoneId(), property, defValue);
    }

    public List<CellInfo> getAllCellInfo() {
        CellInfoResult result = new CellInfoResult();
        if (this.mCi.getRilVersion() < 8) {
            log("SST.getAllCellInfo(): not implemented");
            result.list = null;
        } else if (!isCallerOnDifferentThread()) {
            log("SST.getAllCellInfo(): return last, same thread can't block");
            result.list = this.mLastCellInfoList;
        } else if (SystemClock.elapsedRealtime() - this.mLastCellInfoListTime > LAST_CELL_INFO_LIST_MAX_AGE_MS) {
            Message msg = obtainMessage(43, result);
            synchronized (result.lockObj) {
                result.list = null;
                this.mCi.getCellInfoList(msg);
                try {
                    result.lockObj.wait(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            log("SST.getAllCellInfo(): return last, back to back calls");
            result.list = this.mLastCellInfoList;
        }
        synchronized (result.lockObj) {
            if (result.list != null) {
                List<CellInfo> list = result.list;
                return list;
            }
            log("SST.getAllCellInfo(): X size=0 list=null");
            return null;
        }
    }

    public SignalStrength getSignalStrength() {
        return this.mSignalStrength;
    }

    public void registerForSubscriptionInfoReady(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mCdmaForSubscriptionInfoReadyRegistrants.add(r);
        if (isMinInfoReady()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForSubscriptionInfoReady(Handler h) {
        this.mCdmaForSubscriptionInfoReadyRegistrants.remove(h);
    }

    private void saveCdmaSubscriptionSource(int source) {
        log("Storing cdma subscription source: " + source);
        Global.putInt(this.mPhone.getContext().getContentResolver(), "subscription_mode", source);
        log("Read from settings: " + Global.getInt(this.mPhone.getContext().getContentResolver(), "subscription_mode", -1));
    }

    private void getSubscriptionInfoAndStartPollingThreads() {
        this.mCi.getCDMASubscription(obtainMessage(34));
        pollState();
    }

    private void handleCdmaSubscriptionSource(int newSubscriptionSource) {
        boolean z = false;
        log("Subscription Source : " + newSubscriptionSource);
        if (newSubscriptionSource == 0) {
            z = true;
        }
        this.mIsSubscriptionFromRuim = z;
        log("isFromRuim: " + this.mIsSubscriptionFromRuim);
        saveCdmaSubscriptionSource(newSubscriptionSource);
        if (this.mIsSubscriptionFromRuim) {
            registerForRuimEvents();
            return;
        }
        unregisterForRuimEvents();
        sendMessage(obtainMessage(35));
    }

    private void registerForRuimEvents() {
        log("registerForRuimEvents");
        if (this.mUiccApplcation != null) {
            this.mUiccApplcation.registerForReady(this, 26, null);
        }
        if (this.mIccRecords != null) {
            this.mIccRecords.registerForRecordsLoaded(this, 27, null);
        }
    }

    private void unregisterForRuimEvents() {
        log("unregisterForRuimEvents");
        if (this.mUiccApplcation != null) {
            this.mUiccApplcation.unregisterForReady(this);
        }
        if (this.mIccRecords != null) {
            this.mIccRecords.unregisterForRecordsLoaded(this);
        }
    }

    public void setSignalStrength(SignalStrength signalStrength) {
        log("setSignalStrength : " + signalStrength);
        this.mSignalStrength = signalStrength;
    }

    public void setDoRecoveryTriggerState(boolean state) {
        this.mRadioOffByDoRecovery = state;
        log("setDoRecoveryTriggerState, " + this.mRadioOffByDoRecovery);
    }

    public boolean getDoRecoveryTriggerState() {
        return this.mRadioOffByDoRecovery;
    }

    public void setDoRecoveryMarker(boolean state) {
        this.mDoRecoveryMarker = state;
        log("setDoRecoveryMarker, " + this.mDoRecoveryMarker);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("ServiceStateTracker:");
        pw.println(" mSubId=" + this.mSubId);
        pw.println(" mSS=" + this.mSS);
        pw.println(" mNewSS=" + this.mNewSS);
        pw.println(" mVoiceCapable=" + this.mVoiceCapable);
        pw.println(" mRestrictedState=" + this.mRestrictedState);
        pw.println(" mPollingContext=" + this.mPollingContext + " - " + (this.mPollingContext != null ? Integer.valueOf(this.mPollingContext[0]) : ""));
        pw.println(" mDesiredPowerState=" + this.mDesiredPowerState);
        pw.println(" mDontPollSignalStrength=" + this.mDontPollSignalStrength);
        pw.println(" mSignalStrength=" + this.mSignalStrength);
        pw.println(" mLastSignalStrength=" + this.mLastSignalStrength);
        pw.println(" mRestrictedState=" + this.mRestrictedState);
        pw.println(" mPendingRadioPowerOffAfterDataOff=" + this.mPendingRadioPowerOffAfterDataOff);
        pw.println(" mPendingRadioPowerOffAfterDataOffTag=" + this.mPendingRadioPowerOffAfterDataOffTag);
        pw.println(" mCellLoc=" + this.mCellLoc);
        pw.println(" mNewCellLoc=" + this.mNewCellLoc);
        pw.println(" mLastCellInfoListTime=" + this.mLastCellInfoListTime);
        pw.println(" mPreferredNetworkType=" + this.mPreferredNetworkType);
        pw.println(" mMaxDataCalls=" + this.mMaxDataCalls);
        pw.println(" mNewMaxDataCalls=" + this.mNewMaxDataCalls);
        pw.println(" mReasonDataDenied=" + this.mReasonDataDenied);
        pw.println(" mNewReasonDataDenied=" + this.mNewReasonDataDenied);
        pw.println(" mGsmRoaming=" + this.mGsmRoaming);
        pw.println(" mDataRoaming=" + this.mDataRoaming);
        pw.println(" mEmergencyOnly=" + this.mEmergencyOnly);
        pw.println(" mNeedFixZoneAfterNitz=" + this.mNeedFixZoneAfterNitz);
        pw.flush();
        pw.println(" mZoneOffset=" + this.mZoneOffset);
        pw.println(" mZoneDst=" + this.mZoneDst);
        pw.println(" mZoneTime=" + this.mZoneTime);
        pw.println(" mGotCountryCode=" + this.mGotCountryCode);
        pw.println(" mNitzUpdatedTime=" + this.mNitzUpdatedTime);
        pw.println(" mSavedTimeZone=" + this.mSavedTimeZone);
        pw.println(" mSavedTime=" + this.mSavedTime);
        pw.println(" mSavedAtTime=" + this.mSavedAtTime);
        pw.println(" mStartedGprsRegCheck=" + this.mStartedGprsRegCheck);
        pw.println(" mReportedGprsNoReg=" + this.mReportedGprsNoReg);
        pw.println(" mNotification=" + this.mNotification);
        pw.println(" mWakeLock=" + this.mWakeLock);
        pw.println(" mCurSpn=" + this.mCurSpn);
        pw.println(" mCurDataSpn=" + this.mCurDataSpn);
        pw.println(" mCurShowSpn=" + this.mCurShowSpn);
        pw.println(" mCurPlmn=" + this.mCurPlmn);
        pw.println(" mCurShowPlmn=" + this.mCurShowPlmn);
        pw.flush();
        pw.println(" mCurrentOtaspMode=" + this.mCurrentOtaspMode);
        pw.println(" mRoamingIndicator=" + this.mRoamingIndicator);
        pw.println(" mIsInPrl=" + this.mIsInPrl);
        pw.println(" mDefaultRoamingIndicator=" + this.mDefaultRoamingIndicator);
        pw.println(" mRegistrationState=" + this.mRegistrationState);
        pw.println(" mMdn=" + this.mMdn);
        pw.println(" mHomeSystemId=" + this.mHomeSystemId);
        pw.println(" mHomeNetworkId=" + this.mHomeNetworkId);
        pw.println(" mMin=" + this.mMin);
        pw.println(" mPrlVersion=" + this.mPrlVersion);
        pw.println(" mIsMinInfoReady=" + this.mIsMinInfoReady);
        pw.println(" mIsEriTextLoaded=" + this.mIsEriTextLoaded);
        pw.println(" mIsSubscriptionFromRuim=" + this.mIsSubscriptionFromRuim);
        pw.println(" mCdmaSSM=" + this.mCdmaSSM);
        pw.println(" mRegistrationDeniedReason=" + this.mRegistrationDeniedReason);
        pw.println(" mCurrentCarrier=" + this.mCurrentCarrier);
        pw.flush();
        pw.println(" mImsRegistered=" + this.mImsRegistered);
        pw.println(" mImsRegistrationOnOff=" + this.mImsRegistrationOnOff);
        pw.println(" mAlarmSwitch=" + this.mAlarmSwitch);
        pw.println(" mPowerOffDelayNeed=" + this.mPowerOffDelayNeed);
        pw.println(" mDeviceShuttingDown=" + this.mDeviceShuttingDown);
        pw.println(" mSpnUpdatePending=" + this.mSpnUpdatePending);
    }

    public boolean isImsRegistered() {
        return this.mImsRegistered;
    }

    protected void checkCorrectThread() {
        if (Thread.currentThread() != getLooper().getThread()) {
            throw new RuntimeException("ServiceStateTracker must be used from within one thread");
        }
    }

    protected boolean isCallerOnDifferentThread() {
        return Thread.currentThread() != getLooper().getThread();
    }

    protected void updateCarrierMccMncConfiguration(String newOp, String oldOp, Context context) {
        if (newOp != null || TextUtils.isEmpty(oldOp)) {
            if (newOp == null || newOp.equals(oldOp)) {
                return;
            }
        }
        log("update mccmnc=" + newOp + " fromServiceState=true");
        MccTable.updateMccMncConfiguration(context, newOp, true);
    }

    protected boolean inSameCountry(String operatorNumeric) {
        if (TextUtils.isEmpty(operatorNumeric) || operatorNumeric.length() < 5) {
            return false;
        }
        String homeNumeric = getHomeOperatorNumeric();
        if (TextUtils.isEmpty(homeNumeric) || homeNumeric.length() < 5) {
            return false;
        }
        String networkMCC = operatorNumeric.substring(0, 3);
        String homeMCC = homeNumeric.substring(0, 3);
        String networkCountry = "";
        String homeCountry = "";
        try {
            networkCountry = MccTable.countryCodeForMcc(Integer.parseInt(networkMCC));
            homeCountry = MccTable.countryCodeForMcc(Integer.parseInt(homeMCC));
        } catch (NumberFormatException ex) {
            log("inSameCountry: get networkCountry or homeCountry error: " + ex);
        }
        if (networkCountry.isEmpty() || homeCountry.isEmpty()) {
            return false;
        }
        boolean inSameCountry = homeCountry.equals(networkCountry);
        if (inSameCountry) {
            return inSameCountry;
        }
        if ("us".equals(homeCountry) && "vi".equals(networkCountry)) {
            inSameCountry = true;
        } else if ("vi".equals(homeCountry) && "us".equals(networkCountry)) {
            inSameCountry = true;
        }
        return inSameCountry;
    }

    protected void setRoamingType(ServiceState currentServiceState) {
        boolean isVoiceInService;
        if (currentServiceState.getVoiceRegState() == 0) {
            isVoiceInService = true;
        } else {
            isVoiceInService = false;
        }
        if (isVoiceInService) {
            if (!currentServiceState.getVoiceRoaming()) {
                currentServiceState.setVoiceRoamingType(0);
            } else if (!this.mPhone.isPhoneTypeGsm()) {
                int[] intRoamingIndicators = this.mPhone.getContext().getResources().getIntArray(17236040);
                if (intRoamingIndicators != null && intRoamingIndicators.length > 0) {
                    currentServiceState.setVoiceRoamingType(2);
                    int curRoamingIndicator = currentServiceState.getCdmaRoamingIndicator();
                    for (int i : intRoamingIndicators) {
                        if (curRoamingIndicator == i) {
                            currentServiceState.setVoiceRoamingType(3);
                            break;
                        }
                    }
                } else if (inSameCountry(currentServiceState.getVoiceOperatorNumeric())) {
                    currentServiceState.setVoiceRoamingType(2);
                } else {
                    currentServiceState.setVoiceRoamingType(3);
                }
            } else if (inSameCountry(currentServiceState.getVoiceOperatorNumeric())) {
                currentServiceState.setVoiceRoamingType(2);
            } else {
                currentServiceState.setVoiceRoamingType(3);
            }
        }
        boolean isDataInService = currentServiceState.getDataRegState() == 0;
        int dataRegType = currentServiceState.getRilDataRadioTechnology();
        if (!isDataInService) {
            return;
        }
        if (!currentServiceState.getDataRoaming()) {
            currentServiceState.setDataRoamingType(0);
        } else if (this.mPhone.isPhoneTypeGsm()) {
            if (!ServiceState.isGsm(dataRegType)) {
                currentServiceState.setDataRoamingType(1);
            } else if (isVoiceInService) {
                currentServiceState.setDataRoamingType(currentServiceState.getVoiceRoamingType());
            } else {
                currentServiceState.setDataRoamingType(1);
            }
        } else if (ServiceState.isCdma(dataRegType)) {
            if (isVoiceInService) {
                currentServiceState.setDataRoamingType(currentServiceState.getVoiceRoamingType());
            } else {
                currentServiceState.setDataRoamingType(1);
            }
        } else if (inSameCountry(currentServiceState.getDataOperatorNumeric())) {
            currentServiceState.setDataRoamingType(2);
        } else {
            currentServiceState.setDataRoamingType(3);
        }
    }

    private void setSignalStrengthDefaultValues() {
        this.mSignalStrength = new SignalStrength(true);
    }

    protected String getHomeOperatorNumeric() {
        String numeric = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNumericForPhone(this.mPhone.getPhoneId());
        if (this.mPhone.isPhoneTypeGsm() || !TextUtils.isEmpty(numeric)) {
            return numeric;
        }
        return SystemProperties.get(GsmCdmaPhone.PROPERTY_CDMA_HOME_OPERATOR_NUMERIC, "");
    }

    protected int getPhoneId() {
        return this.mPhone.getPhoneId();
    }

    protected void resetServiceStateInIwlanMode() {
        if (this.mCi.getRadioState() == RadioState.RADIO_OFF) {
            boolean resetIwlanRatVal = false;
            log("set service state as POWER_OFF");
            if (18 == this.mNewSS.getRilDataRadioTechnology()) {
                log("pollStateDone: mNewSS = " + this.mNewSS);
                log("pollStateDone: reset iwlan RAT value");
                resetIwlanRatVal = true;
            }
            this.mNewSS.setStateOff();
            if (resetIwlanRatVal) {
                this.mNewSS.setRilDataRadioTechnology(18);
                this.mNewSS.setDataRegState(0);
                log("pollStateDone: mNewSS = " + this.mNewSS);
            }
        }
    }

    protected final boolean alwaysOnHomeNetwork(BaseBundle b) {
        return b.getBoolean("force_home_network_bool");
    }

    private boolean isInNetwork(BaseBundle b, String network, String key) {
        String[] networks = b.getStringArray(key);
        if (networks == null || !Arrays.asList(networks).contains(network)) {
            return false;
        }
        return true;
    }

    protected final boolean isRoamingInGsmNetwork(BaseBundle b, String network) {
        return isInNetwork(b, network, "gsm_roaming_networks_string_array");
    }

    protected final boolean isNonRoamingInGsmNetwork(BaseBundle b, String network) {
        return isInNetwork(b, network, "gsm_nonroaming_networks_string_array");
    }

    protected final boolean isRoamingInCdmaNetwork(BaseBundle b, String network) {
        return isInNetwork(b, network, "cdma_roaming_networks_string_array");
    }

    protected final boolean isNonRoamingInCdmaNetwork(BaseBundle b, String network) {
        return isInNetwork(b, network, "cdma_nonroaming_networks_string_array");
    }

    public boolean isDeviceShuttingDown() {
        return this.mDeviceShuttingDown;
    }

    protected void getCaller() {
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            log("    at " + ste.getClassName() + "." + ste.getMethodName() + "(" + ste.getFileName() + ":" + ste.getLineNumber() + ")");
        }
    }

    protected void acquireWakeLock() {
        if (this.mWakeLock == null) {
            this.mWakeLock = ((PowerManager) this.mPhone.getContext().getSystemService("power")).newWakeLock(1, "SERVICESTATE_WAIT_DISCONNECT_WAKELOCK");
        }
        this.mWakeLock.setReferenceCounted(false);
        log("Servicestate wait disconnect, acquire wakelock");
        this.mWakeLock.acquire();
    }

    protected void releaseWakeLock() {
        if (this.mWakeLock != null && this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
            log("release wakelock");
        }
    }

    protected void judgeToLaunchCsgPeriodicSearchTimer() {
        if (this.mHwCustGsmServiceStateTracker != null) {
            this.mHwCustGsmServiceStateTracker.judgeToLaunchCsgPeriodicSearchTimer();
            log("mHwCustGsmServiceStateTracker is not null");
        }
    }

    public boolean isContainPackage(String data, String packageName) {
        String[] enablePackage = null;
        if (!TextUtils.isEmpty(data)) {
            enablePackage = data.split(";");
        }
        if (enablePackage == null || enablePackage.length == 0) {
            return false;
        }
        int i = 0;
        while (i < enablePackage.length) {
            if (!TextUtils.isEmpty(packageName) && packageName.equals(enablePackage[i])) {
                return true;
            }
            i++;
        }
        return false;
    }

    public void setDelayUpdateGpsState(int state) {
        this.mDelayUpdateGpsState = state;
    }

    public int getDelayUpdateGpsState() {
        return this.mDelayUpdateGpsState;
    }
}
