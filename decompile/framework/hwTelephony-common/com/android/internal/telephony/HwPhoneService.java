package com.android.internal.telephony;

import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.telephony.CellLocation;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyManager.MultiSimVariants;
import android.telephony.UiccAuthResponse;
import android.text.TextUtils;
import com.android.ims.ImsManager;
import com.android.internal.telephony.HwPhoneManager.PhoneServiceInterface;
import com.android.internal.telephony.IHwTelephony.Stub;
import com.android.internal.telephony.dataconnection.TelephonyNetworkFactory;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class HwPhoneService extends Stub implements PhoneServiceInterface {
    private static final int CMD_ENCRYPT_CALL_INFO = 200;
    private static final int CMD_HANDLE_DEMO = 1;
    private static final int CMD_IMS_GET_DOMAIN = 103;
    private static final int CMD_UICC_AUTH = 101;
    private static final String DEVICEID_PREF = "deviceid";
    private static final int EVENT_COMMON_IMSA_MAPCON_INFO = 53;
    private static final int EVENT_ENCRYPT_CALL_INFO_DONE = 201;
    private static final int EVENT_GET_CELL_BAND_DONE = 6;
    private static final int EVENT_GET_PREF_NETWORKS = 3;
    private static final int EVENT_GET_PREF_NETWORK_TYPE_DONE = 9;
    private static final int EVENT_IMS_GET_DOMAIN_DONE = 104;
    private static final int EVENT_NOTIFY_CMODEM_STATUS = 110;
    private static final int EVENT_NOTIFY_DEVICE_STATE = 111;
    private static final int EVENT_QUERY_ENCRYPT_FEATURE = 202;
    private static final int EVENT_QUERY_ENCRYPT_FEATURE_DONE = 203;
    private static final int EVENT_RADIO_AVAILABLE = 51;
    private static final int EVENT_RADIO_NOT_AVAILABLE = 52;
    private static final int EVENT_REG_ANT_STATE_IND = 11;
    private static final int EVENT_REG_BAND_CLASS_IND = 10;
    private static final int EVENT_REG_MAX_TX_POWER_IND = 12;
    private static final int EVENT_SET_LTE_SWITCH_DONE = 5;
    private static final int EVENT_SET_PREF_NETWORKS = 4;
    private static final int EVENT_UICC_AUTH_DONE = 102;
    private static final String IMEI_PREF = "imei";
    private static final int INVALID_NETWORK_MODE = -1;
    private static final boolean IS_4G_SWITCH_SUPPORTED = SystemProperties.getBoolean("persist.sys.dualcards", false);
    private static final boolean IS_FULL_NETWORK_SUPPORTED = SystemProperties.getBoolean("ro.config.full_network_support", false);
    private static final boolean IS_MODEM_FULL_PREFMODE_SUPPORTED = HwModemCapability.isCapabilitySupport(3);
    private static final boolean IS_QCRIL_CROSS_MAPPING = SystemProperties.getBoolean("ro.hwpp.qcril_cross_mapping", false);
    private static final String KEY1 = "key1";
    private static final String LOG_TAG = "HwPhoneService";
    private static final int LTE_SERVICE_OFF = 0;
    private static final int LTE_SERVICE_ON = 1;
    private static final int MAX_QUERY_COUNT = 10;
    private static final int MSG_ENCRYPT_CALL_BASE = 200;
    private static final int NOTIFY_CMODEM_STATUS_FAIL = -1;
    private static final int NOTIFY_CMODEM_STATUS_SUCCESS = 1;
    private static final int REDUCE_TYPE_CELL = 1;
    private static final int REDUCE_TYPE_WIFI = 2;
    private static final int REGISTER_TYPE = 1;
    private static final int REGISTER_TYPE_ANTENNA = 2;
    private static final int REGISTER_TYPE_BAND = 1;
    private static final int REGISTER_TYPE_MAX_TX_POWER = 4;
    private static final int SIM_NUM = TelephonyManager.getDefault().getPhoneCount();
    public static final int SUB1 = 0;
    public static final int SUB2 = 1;
    public static final int SUB_NONE = -1;
    private static final int UNREGISTER_TYPE = 2;
    private static final String USER_DATACALL_SUBSCRIPTION = "user_datacall_sub";
    private static int lteOffMappingMode;
    private static int lteOnMappingMode;
    private static HashMap<Integer, Integer> mLteOnOffMapping = new HashMap();
    private static int queryCount = 0;
    private static final boolean sIsPlatformSupportVSim = SystemProperties.getBoolean("ro.radio.vsim_support", false);
    private final int ENCRYPT_CALL_FEATURE_CLOSE = 0;
    private final String ENCRYPT_CALL_FEATURE_KEY = "encrypt_version";
    private final int ENCRYPT_CALL_FEATURE_OPEN = 1;
    private final int ENCRYPT_CALL_FEATURE_SUPPORT = 1;
    private final int ENCRYPT_CALL_NV_OFFSET = 4;
    private AppOpsManager mAppOps;
    private Context mContext;
    private int mEncryptCallStatus = 0;
    private IPhoneCallback mImsaToMapconInfoCB = null;
    protected final Object mLock = new Object();
    private MainHandler mMainHandler;
    private HandlerThread mMessageThread = new HandlerThread("HuaweiPhoneTempService");
    private HwPhone mPhone;
    private PhoneServiceReceiver mPhoneServiceReceiver;
    private HwPhone[] mPhones = null;
    private IPhoneCallback mRadioAvailableIndCB = null;
    private IPhoneCallback mRadioNotAvailableIndCB = null;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && HwPhoneService.this.mPhone != null && "android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction()) && "READY".equals(intent.getExtra("ss"))) {
                HwPhoneService.this.log("mReceiver receive ACTION_SIM_STATE_CHANGED READY,check pref network type");
                HwPhoneService.this.mPhone.getPreferredNetworkType(HwPhoneService.this.mMainHandler.obtainMessage(9));
            }
        }
    };
    private Object[] mRegAntStateCallbackArray = new Object[]{this.mRegAntStateCallbackLists0, this.mRegAntStateCallbackLists1};
    private final ArrayList<Record> mRegAntStateCallbackLists0 = new ArrayList();
    private final ArrayList<Record> mRegAntStateCallbackLists1 = new ArrayList();
    private Object[] mRegBandClassCallbackArray = new Object[]{this.mRegBandClassCallbackLists0, this.mRegBandClassCallbackLists1};
    private final ArrayList<Record> mRegBandClassCallbackLists0 = new ArrayList();
    private final ArrayList<Record> mRegBandClassCallbackLists1 = new ArrayList();
    private Object[] mRegMaxTxPowerCallbackArray = new Object[]{this.mRegMaxTxPowerCallbackList0, this.mRegMaxTxPowerCallbackList1};
    private final ArrayList<Record> mRegMaxTxPowerCallbackList0 = new ArrayList();
    private final ArrayList<Record> mRegMaxTxPowerCallbackList1 = new ArrayList();
    private String[] mServiceCellBand = new String[2];
    boolean mSupportEncryptCall = SystemProperties.getBoolean("persist.sys.cdma_encryption", false);

    private static final class EncryptCallPara {
        byte[] buf = null;
        int event;
        HwPhone phone = null;

        public EncryptCallPara(HwPhone phone, int event, byte[] buf) {
            this.phone = phone;
            this.event = event;
            this.buf = buf;
        }
    }

    private final class MainHandler extends Handler {
        MainHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            MainThreadRequest request;
            AsyncResult ar;
            HwPhone phone;
            switch (msg.what) {
                case 3:
                    handleGetPreferredNetworkTypeResponse(msg);
                    return;
                case 4:
                    handleSetPreferredNetworkTypeResponse(msg);
                    return;
                case 5:
                    HwPhoneService.this.log("4G-Switch EVENT_SET_LTE_SWITCH_DONE");
                    HwPhoneService.this.handleSetLteSwitchDone(msg);
                    return;
                case 6:
                    HwPhoneService.this.handleQueryCellBandDone(msg);
                    return;
                case 9:
                    HwPhoneService.this.log("EVENT_GET_PREF_NETWORK_TYPE_DONE");
                    HwPhoneService.this.handleGetPrefNetworkTypeDone(msg);
                    return;
                case 10:
                    HwPhoneService.this.log("EVENT_REG_BAND_CLASS_IND");
                    HwPhoneService.this.handleSarInfoUploaded(1, msg);
                    return;
                case 11:
                    HwPhoneService.this.log("EVENT_REG_ANT_STATE_IND");
                    HwPhoneService.this.handleSarInfoUploaded(2, msg);
                    return;
                case 12:
                    HwPhoneService.this.log("EVENT_REG_MAX_TX_POWER_IND");
                    HwPhoneService.this.handleSarInfoUploaded(4, msg);
                    return;
                case 51:
                    HwPhoneService.this.handleRadioAvailableInd(msg);
                    return;
                case 52:
                    HwPhoneService.this.handleRadioNotAvailableInd(msg);
                    return;
                case 53:
                    HwPhoneService.this.handleCommonImsaToMapconInfoInd(msg);
                    return;
                case HwPhoneService.CMD_UICC_AUTH /*101*/:
                    request = msg.obj;
                    UiccAuthPara para = request.argument;
                    HwPhoneService.this.mPhone.handleUiccAuth(para.auth_type, para.rand, para.auth, obtainMessage(HwPhoneService.EVENT_UICC_AUTH_DONE, request));
                    return;
                case HwPhoneService.EVENT_UICC_AUTH_DONE /*102*/:
                    ar = msg.obj;
                    request = (MainThreadRequest) ar.userObj;
                    HwPhoneService.loge("EVENT_UICC_AUTH_DONE");
                    request.result = new UiccAuthResponse();
                    if (ar.exception == null && ar.result != null) {
                        request.result = ar.result;
                    } else if (ar.result == null) {
                        HwPhoneService.loge("UiccAuthReq: Empty response");
                    } else if (ar.exception instanceof CommandException) {
                        HwPhoneService.loge("UiccAuthReq: CommandException: " + ar.exception);
                    } else {
                        HwPhoneService.loge("UiccAuthReq: Unknown exception");
                    }
                    synchronized (request) {
                        request.notifyAll();
                        break;
                    }
                case HwPhoneService.CMD_IMS_GET_DOMAIN /*103*/:
                    HwPhoneService.this.mPhone.getImsDomain(obtainMessage(HwPhoneService.EVENT_IMS_GET_DOMAIN_DONE, (MainThreadRequest) msg.obj));
                    return;
                case HwPhoneService.EVENT_IMS_GET_DOMAIN_DONE /*104*/:
                    ar = (AsyncResult) msg.obj;
                    request = (MainThreadRequest) ar.userObj;
                    if (ar.exception == null && ar.result != null) {
                        request.result = ar.result;
                    } else if (ar.result == null) {
                        request.result = new int[]{2};
                        HwPhoneService.loge("getImsDomain: Empty response,return 2");
                    } else if (ar.exception instanceof CommandException) {
                        request.result = new int[]{2};
                        HwPhoneService.loge("getImsDomain: CommandException:return 2 " + ar.exception);
                    } else {
                        request.result = new int[]{2};
                        HwPhoneService.loge("getImsDomain: Unknown exception,return 2");
                    }
                    synchronized (request) {
                        request.notifyAll();
                        break;
                    }
                case HwPhoneService.EVENT_NOTIFY_CMODEM_STATUS /*110*/:
                    Rlog.d(HwPhoneService.LOG_TAG, "EVENT_NOTIFY_CMODEM_STATUS");
                    ar = (AsyncResult) msg.obj;
                    if (ar != null) {
                        IPhoneCallback callback = ar.userObj;
                        if (callback != null) {
                            int result = 1;
                            if (ar.exception != null) {
                                result = -1;
                            }
                            try {
                                Rlog.d(HwPhoneService.LOG_TAG, "EVENT_NOTIFY_CMODEM_STATUS onCallback1");
                                callback.onCallback1(result);
                                return;
                            } catch (RemoteException ex) {
                                Rlog.e(HwPhoneService.LOG_TAG, "EVENT_NOTIFY_CMODEM_STATUS onCallback1 RemoteException:" + ex);
                                return;
                            }
                        }
                        return;
                    }
                    return;
                case HwPhoneService.EVENT_NOTIFY_DEVICE_STATE /*111*/:
                    ar = (AsyncResult) msg.obj;
                    if (ar == null) {
                        HwPhoneService.loge("EVENT_NOTIFY_DEVICE_STATE, ar is null.");
                        return;
                    } else if (ar.exception == null) {
                        Rlog.d(HwPhoneService.LOG_TAG, "EVENT_NOTIFY_DEVICE_STATE success.");
                        return;
                    } else if (ar.exception instanceof CommandException) {
                        HwPhoneService.loge("EVENT_NOTIFY_DEVICE_STATE, " + ar.exception);
                        return;
                    } else {
                        HwPhoneService.loge("EVENT_NOTIFY_DEVICE_STATE, unknown exception.");
                        return;
                    }
                case 200:
                    Rlog.d(HwPhoneService.LOG_TAG, "requestForECInfo receive event");
                    request = (MainThreadRequest) msg.obj;
                    EncryptCallPara ECpara = request.argument;
                    Message onCompleted = obtainMessage(HwPhoneService.EVENT_ENCRYPT_CALL_INFO_DONE, request);
                    HwPhone sPhone = ECpara.phone;
                    if (sPhone != null) {
                        sPhone.requestForECInfo(onCompleted, ECpara.event, ECpara.buf);
                        return;
                    }
                    return;
                case HwPhoneService.EVENT_ENCRYPT_CALL_INFO_DONE /*201*/:
                    Rlog.d(HwPhoneService.LOG_TAG, "requestForECInfo receive event done");
                    ar = (AsyncResult) msg.obj;
                    request = (MainThreadRequest) ar.userObj;
                    if (ar.exception == null) {
                        if (ar.result == null || ((byte[]) ar.result).length <= 0) {
                            request.result = new byte[]{(byte) 1};
                            Rlog.d(HwPhoneService.LOG_TAG, "requestForECInfo success,return 1");
                        } else {
                            request.result = ar.result;
                            Rlog.d(HwPhoneService.LOG_TAG, "requestForECInfo success,return ar.result");
                        }
                    } else if (ar.exception instanceof CommandException) {
                        request.result = new byte[]{(byte) -1};
                        HwPhoneService.loge("requestForECInfo: CommandException:return -1 " + ar.exception);
                    } else {
                        request.result = new byte[]{(byte) -2};
                        HwPhoneService.loge("requestForECInfo: Unknown exception,return -2");
                    }
                    synchronized (request) {
                        request.notifyAll();
                        break;
                    }
                case HwPhoneService.EVENT_QUERY_ENCRYPT_FEATURE /*202*/:
                    Rlog.d(HwPhoneService.LOG_TAG, "radio available, query encrypt call feature");
                    if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                        for (HwPhone phone2 : HwPhoneService.this.mPhones) {
                            HwPhoneService.this.handleEventQueryEncryptCall(phone2);
                        }
                        return;
                    }
                    HwPhoneService.this.handleEventQueryEncryptCall(HwPhoneService.this.mPhone);
                    return;
                case HwPhoneService.EVENT_QUERY_ENCRYPT_FEATURE_DONE /*203*/:
                    Rlog.d(HwPhoneService.LOG_TAG, "query encrypt call feature received");
                    ar = (AsyncResult) msg.obj;
                    phone2 = (HwPhone) ar.userObj;
                    if (ar.exception != null || ar.result == null) {
                        HwPhoneService.loge("query encrypt call feature failed " + ar.exception);
                        if (msg.arg1 < 10) {
                            sendEmptyMessageDelayed(HwPhoneService.EVENT_QUERY_ENCRYPT_FEATURE, 1000);
                            return;
                        } else {
                            HwPhoneService.queryCount = 0;
                            return;
                        }
                    }
                    byte[] res = ar.result;
                    HwPhoneService.this.mSupportEncryptCall = (res[0] & 15) == 1;
                    HwPhoneService.this.mEncryptCallStatus = res[0] >>> 4;
                    if (HwPhoneService.this.mSupportEncryptCall) {
                        SystemProperties.set("persist.sys.cdma_encryption", Boolean.toString(HwPhoneService.this.mSupportEncryptCall));
                        HwPhoneService.this.checkEcSwitchStatusInNV(phone2, HwPhoneService.this.mEncryptCallStatus);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }

        private void handleGetPreferredNetworkTypeResponse(Message msg) {
            Rlog.d(HwPhoneService.LOG_TAG, "[enter]handleGetPreferredNetworkTypeResponse");
            AsyncResult ar = msg.obj;
            if (ar.exception == null) {
                Rlog.d(HwPhoneService.LOG_TAG, "getPreferredNetworkType is " + ((int[]) ar.result)[0]);
                return;
            }
            Rlog.d(HwPhoneService.LOG_TAG, "getPreferredNetworkType exception=" + ar.exception);
        }

        private void handleSetPreferredNetworkTypeResponse(Message msg) {
            Rlog.d(HwPhoneService.LOG_TAG, "[enter]handleSetPreferredNetworkTypeResponse");
            AsyncResult ar = msg.obj;
            if (ar == null) {
                Rlog.d(HwPhoneService.LOG_TAG, "setPreferredNetworkType ar == null");
            } else if (ar.exception != null) {
                Rlog.d(HwPhoneService.LOG_TAG, "setPreferredNetworkType exception=" + ar.exception);
                HwPhoneService.this.mPhone.getPreferredNetworkType(obtainMessage(3));
            } else {
                int setPrefMode = ((Integer) ar.userObj).intValue();
                int curPrefMode = 0;
                try {
                    curPrefMode = Global.getInt(HwPhoneService.this.mContext.getContentResolver(), "preferred_network_mode", -1);
                } catch (RuntimeException e) {
                } catch (Exception e2) {
                }
                if (curPrefMode != setPrefMode) {
                    Global.putInt(HwPhoneService.this.mContext.getContentResolver(), "preferred_network_mode", setPrefMode);
                }
            }
        }
    }

    private static final class MainThreadRequest {
        public Object argument;
        public Object result;
        public Integer subId;

        public MainThreadRequest(Object argument) {
            this.argument = argument;
        }

        public MainThreadRequest(Object argument, Integer subId) {
            this.argument = argument;
            this.subId = subId;
        }
    }

    private class PhoneServiceReceiver extends BroadcastReceiver {
        public PhoneServiceReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.RADIO_TECHNOLOGY");
            HwPhoneService.this.mContext.registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            int i = 0;
            Rlog.d(HwPhoneService.LOG_TAG, "radio tech changed, query encrypt call feature");
            if (intent != null && "android.intent.action.RADIO_TECHNOLOGY".equals(intent.getAction())) {
                HwPhoneService.queryCount = 0;
                if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                    HwPhone[] -get4 = HwPhoneService.this.mPhones;
                    int length = -get4.length;
                    while (i < length) {
                        HwPhoneService.this.handleEventQueryEncryptCall(-get4[i]);
                        i++;
                    }
                    return;
                }
                HwPhoneService.this.handleEventQueryEncryptCall(HwPhoneService.this.mPhone);
            }
        }
    }

    private static class Record {
        IBinder binder;
        IPhoneCallback callback;

        private Record() {
            this.binder = null;
            this.callback = null;
        }

        public String toString() {
            return "{binder=" + this.binder + " callback=" + this.callback + "}";
        }
    }

    private static final class UiccAuthPara {
        byte[] auth;
        int auth_type;
        byte[] rand;

        public UiccAuthPara(int auth_type, byte[] rand, byte[] auth) {
            this.auth_type = auth_type;
            this.rand = rand;
            this.auth = auth;
        }
    }

    static {
        lteOnMappingMode = -1;
        lteOffMappingMode = -1;
        mLteOnOffMapping.put(Integer.valueOf(20), Integer.valueOf(18));
        mLteOnOffMapping.put(Integer.valueOf(9), Integer.valueOf(3));
        mLteOnOffMapping.put(Integer.valueOf(15), Integer.valueOf(13));
        mLteOnOffMapping.put(Integer.valueOf(8), Integer.valueOf(4));
        mLteOnOffMapping.put(Integer.valueOf(10), Integer.valueOf(7));
        mLteOnOffMapping.put(Integer.valueOf(11), Integer.valueOf(7));
        mLteOnOffMapping.put(Integer.valueOf(12), Integer.valueOf(2));
        mLteOnOffMapping.put(Integer.valueOf(17), Integer.valueOf(16));
        mLteOnOffMapping.put(Integer.valueOf(19), Integer.valueOf(14));
        mLteOnOffMapping.put(Integer.valueOf(22), Integer.valueOf(21));
        mLteOnOffMapping.put(Integer.valueOf(25), Integer.valueOf(24));
        lteOnMappingMode = SystemProperties.getInt("ro.telephony.default_network", -1);
        if (mLteOnOffMapping.containsKey(Integer.valueOf(lteOnMappingMode))) {
            lteOffMappingMode = ((Integer) mLteOnOffMapping.get(Integer.valueOf(lteOnMappingMode))).intValue();
        } else {
            lteOnMappingMode = -1;
        }
        String[] lteOnOffMapings = SystemProperties.get("ro.hwpp.lteonoff_mapping", "0,0").split(",");
        if (lteOnOffMapings.length == 2 && Integer.parseInt(lteOnOffMapings[0]) != 0) {
            lteOnMappingMode = Integer.parseInt(lteOnOffMapings[0]);
            lteOffMappingMode = Integer.parseInt(lteOnOffMapings[1]);
        }
    }

    public HwPhoneService() {
        this.mMessageThread.start();
        this.mMainHandler = new MainHandler(this.mMessageThread.getLooper());
    }

    public void setPhone(Phone phone, Context context) {
        this.mPhone = new HwPhone(phone);
        this.mContext = context;
        initService();
    }

    public void setPhone(Phone[] phones, Context context) {
        int numPhones = TelephonyManager.getDefault().getPhoneCount();
        this.mPhones = new HwPhone[numPhones];
        for (int i = 0; i < numPhones; i++) {
            Rlog.d(LOG_TAG, "Creating HwPhone sub = " + i);
            this.mPhones[i] = new HwPhone(phones[i]);
        }
        this.mPhone = this.mPhones[0];
        this.mContext = context;
        log("setPhone mPhones = " + this.mPhones);
        initService();
    }

    private void initService() {
        Rlog.d(LOG_TAG, "initService()");
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        ServiceManager.addService("phone_huawei", this);
        initPrefNetworkTypeChecker();
        registerForRadioOnInner();
        if (this.mPhoneServiceReceiver == null) {
            this.mPhoneServiceReceiver = new PhoneServiceReceiver();
        }
    }

    private void log(String msg) {
        Rlog.d(LOG_TAG, "[PhoneIntfMgr] " + msg);
    }

    public String getDemoString() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        return "" + this.mPhone + this.mContext;
    }

    public String getMeidForSubscriber(int slot) {
        if (!canReadPhoneState("getMeid") || slot < 0 || slot >= this.mPhones.length) {
            return null;
        }
        if (-1 != SystemProperties.getInt("persist.radio.stack_id_0", -1)) {
            slot = SystemProperties.getInt("persist.radio.stack_id_0", -1);
            log("QC after switch slot = " + slot);
        }
        return this.mPhones[slot].getMeid();
    }

    public String getPesnForSubscriber(int slot) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (slot < 0 || slot >= this.mPhones.length) {
            return null;
        }
        if (-1 != SystemProperties.getInt("persist.radio.stack_id_0", -1)) {
            slot = SystemProperties.getInt("persist.radio.stack_id_0", -1);
            log("QC after switch slot = " + slot);
        }
        return this.mPhones[slot].getPesn();
    }

    public int getSubState(int subId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (SubscriptionController.getInstance() != null) {
            return SubscriptionController.getInstance().getSubState(subId);
        }
        return 0;
    }

    public void setUserPrefDataSlotId(int slotId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (HwSubscriptionManager.getInstance() != null) {
            HwSubscriptionManager.getInstance().setUserPrefDataSlotId(slotId);
        } else {
            Rlog.e(LOG_TAG, "HwSubscriptionManager is null!!");
        }
    }

    public int getDataStateForSubscriber(int subId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        return DefaultPhoneNotifier.convertDataState(this.mPhones[SubscriptionController.getInstance().getPhoneId(subId)].getDataConnectionState());
    }

    public String getNVESN() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        return this.mPhone.getNVESN();
    }

    public void closeRrc() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        int dataSubId = SubscriptionController.getInstance().getDefaultDataSubId();
        if (dataSubId >= 0 && dataSubId < this.mPhones.length) {
            this.mPhones[dataSubId].closeRrc();
        }
    }

    public boolean isCTCdmaCardInGsmMode() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!HuaweiTelephonyConfigs.isChinaTelecom()) {
            return false;
        }
        boolean noCdmaPhone = true;
        for (HwPhone isCDMAPhone : this.mPhones) {
            if (isCDMAPhone.isCDMAPhone()) {
                noCdmaPhone = false;
                break;
            }
        }
        return noCdmaPhone;
    }

    public boolean isLTESupported() {
        boolean result;
        switch (RILConstants.PREFERRED_NETWORK_MODE) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 13:
            case 14:
            case 16:
            case 18:
            case 21:
            case 52:
                result = false;
                break;
            default:
                result = true;
                break;
        }
        Rlog.i(LOG_TAG, "isLTESupported " + result);
        return result;
    }

    public void setDefaultMobileEnable(boolean enabled) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        Rlog.d(LOG_TAG, "setDefaultMobileEnable to " + enabled);
        int numPhones = TelephonyManager.getDefault().getPhoneCount();
        for (int i = 0; i < numPhones; i++) {
            this.mPhones[i].setDefaultMobileEnable(enabled);
            TelephonyNetworkFactory telephonyNetworkFactory = PhoneFactory.getTelephonyNetworkFactory(i);
            if (telephonyNetworkFactory != null) {
                telephonyNetworkFactory.reconnectDefaultRequestRejectByWifi();
            }
        }
    }

    public void setDataEnabledWithoutPromp(boolean enabled) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        int phoneId = SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId());
        if (phoneId >= 0 && phoneId < this.mPhones.length) {
            this.mPhones[phoneId].setDataEnabledWithoutPromp(enabled);
        }
    }

    private Object sendRequest(int command, Object argument) {
        return sendRequest(command, argument, null);
    }

    private Object sendRequest(int command, Object argument, Integer subId) {
        if (Looper.myLooper() == this.mMainHandler.getLooper()) {
            throw new RuntimeException("This method will deadlock if called from the main thread.");
        }
        MainThreadRequest request = new MainThreadRequest(argument, subId);
        this.mMainHandler.obtainMessage(command, request).sendToTarget();
        synchronized (request) {
            while (request.result == null) {
                try {
                    request.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        return request.result;
    }

    public void setPreferredNetworkType(int nwMode) {
        enforceModifyPermissionOrCarrierPrivilege();
        Rlog.d(LOG_TAG, "[enter]setPreferredNetworkType " + nwMode);
        if (TelephonyManager.getDefault().isMultiSimEnabled() && HwAllInOneController.getInstance().getBalongSimSlot() == 1) {
            this.mPhone = this.mPhones[1];
        } else {
            this.mPhone = this.mPhones[0];
        }
        if (this.mPhone == null) {
            Rlog.e(LOG_TAG, "4G-Switch mPhone is null. return!");
        } else {
            this.mPhone.setPreferredNetworkType(nwMode, this.mMainHandler.obtainMessage(4, Integer.valueOf(nwMode)));
        }
    }

    public void setLteServiceAbility(int ability) {
        enforceModifyPermissionOrCarrierPrivilege();
        log("=4G-Switch= setLteServiceAbility: ability=" + ability);
        if (IS_QCRIL_CROSS_MAPPING) {
            HwAllInOneController.getInstance().setLteServiceAbilityForQCOM(ability, lteOnMappingMode);
            return;
        }
        if (TelephonyManager.getDefault().isMultiSimEnabled() && getDefault4GSlotId() == 1) {
            this.mPhone = this.mPhones[1];
        } else {
            this.mPhone = this.mPhones[0];
        }
        if (this.mPhone == null) {
            loge("4G-Switch mPhone is null. return!");
            return;
        }
        boolean isQcomSRAL;
        int networkType = IS_MODEM_FULL_PREFMODE_SUPPORTED ? calculateNetworkType(ability) : this.mPhone.isCDMAPhone() ? ability == 1 ? 8 : 4 : ability == 1 ? 9 : 3;
        if (!HuaweiTelephonyConfigs.isQcomPlatform() || TelephonyManager.getDefault().isMultiSimEnabled()) {
            isQcomSRAL = false;
        } else {
            isQcomSRAL = IS_FULL_NETWORK_SUPPORTED;
        }
        if (isQcomSRAL) {
            networkType = this.mPhone.isCDMAPhone() ? ability == 1 ? 10 : 7 : ability == 1 ? 20 : 18;
        }
        if (-1 == networkType) {
            sendLteServiceSwitchResult(false);
            return;
        }
        this.mPhone.setPreferredNetworkType(networkType, this.mMainHandler.obtainMessage(5, Integer.valueOf(networkType)));
        log("=4G-Switch= setPreferredNetworkType-> " + networkType);
    }

    private int calculateNetworkType(int ability) {
        int networkType = ability == 1 ? lteOnMappingMode : lteOffMappingMode;
        int curPrefMode = getCurrentNetworkTypeFromDB();
        int mappingNetworkType = ability == 1 ? getOnKeyFromMapping(curPrefMode) : getOffValueFromMapping(curPrefMode);
        log("=4G-Switch= curPrefMode = " + curPrefMode + " ,mappingNetworkType = " + mappingNetworkType);
        if (-1 != mappingNetworkType) {
            return mappingNetworkType;
        }
        return networkType;
    }

    private int getCurrentNetworkTypeFromDB() {
        try {
            if (!IS_MODEM_FULL_PREFMODE_SUPPORTED || !TelephonyManager.getDefault().isMultiSimEnabled()) {
                return Global.getInt(this.mContext.getContentResolver(), "preferred_network_mode", -1);
            }
            return TelephonyManager.getIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", IS_4G_SWITCH_SUPPORTED ? getDefault4GSlotId() : 0);
        } catch (RuntimeException e) {
            loge("=4G-Switch=  PREFERRED_NETWORK_MODE RuntimeException = " + e);
            return -1;
        } catch (Exception e2) {
            loge("=4G-Switch=  PREFERRED_NETWORK_MODE Exception = " + e2);
            return -1;
        }
    }

    private void saveNetworkTypeToDB(int setPrefMode) {
        if (IS_MODEM_FULL_PREFMODE_SUPPORTED && TelephonyManager.getDefault().isMultiSimEnabled()) {
            TelephonyManager.putIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", IS_4G_SWITCH_SUPPORTED ? getDefault4GSlotId() : 0, setPrefMode);
        } else {
            Global.putInt(this.mContext.getContentResolver(), "preferred_network_mode", setPrefMode);
        }
    }

    private int getOnKeyFromMapping(int curPrefMode) {
        int onKey = -1;
        for (Entry<Integer, Integer> entry : mLteOnOffMapping.entrySet()) {
            if (curPrefMode == ((Integer) entry.getValue()).intValue()) {
                onKey = ((Integer) entry.getKey()).intValue();
                if (7 != curPrefMode || 11 != onKey) {
                    break;
                }
            }
        }
        return onKey;
    }

    private int getOffValueFromMapping(int curPrefMode) {
        if (mLteOnOffMapping.containsKey(Integer.valueOf(curPrefMode))) {
            return ((Integer) mLteOnOffMapping.get(Integer.valueOf(curPrefMode))).intValue();
        }
        return -1;
    }

    private void handleSetLteSwitchDone(Message msg) {
        log("=4G-Switch= in handleSetLteSwitchDone");
        AsyncResult ar = msg.obj;
        if (ar == null || ar.exception != null) {
            loge("=4G-Switch= set prefer network mode failed!");
            sendLteServiceSwitchResult(false);
            return;
        }
        int setPrefMode = ((Integer) ar.userObj).intValue();
        log("=4G-Switch= set preferred network mode database to " + setPrefMode);
        int curPrefMode = getCurrentNetworkTypeFromDB();
        log("=4G-Switch= curPrefMode = " + curPrefMode);
        if (curPrefMode != setPrefMode) {
            saveNetworkTypeToDB(setPrefMode);
        }
        log("=4G-Switch= set prefer network mode success!");
        sendLteServiceSwitchResult(true);
    }

    private void sendLteServiceSwitchResult(boolean result) {
        log("=4G-Switch= LTE service Switch result is " + result + ". broadcast PREFERRED_4G_SWITCH_DONE");
        if (this.mContext == null) {
            loge("=4G-Switch= mContext is null. return!");
            return;
        }
        Intent intent = new Intent("com.huawei.telephony.PREF_4G_SWITCH_DONE");
        intent.putExtra("setting_result", result);
        this.mContext.sendBroadcast(intent);
    }

    public int getLteServiceAbility() {
        int ability;
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        int curPrefMode = getCurrentNetworkTypeFromDB();
        log("=4G-Switch= curPrefMode = " + curPrefMode);
        if (IS_MODEM_FULL_PREFMODE_SUPPORTED) {
            if (mLteOnOffMapping.containsKey(Integer.valueOf(curPrefMode))) {
                ability = 1;
            } else {
                ability = 0;
            }
        } else if ((curPrefMode < 8 || curPrefMode > 31) && curPrefMode != 61) {
            ability = 0;
        } else {
            ability = 1;
        }
        log("=4G-Switch= getLteServiceAbility() ability is " + ability);
        return ability;
    }

    private void enforceModifyPermissionOrCarrierPrivilege() {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") != 0) {
            log("No modify permission, check carrier privilege next.");
            if (hasCarrierPrivileges() != 1) {
                loge("No Carrier Privilege.");
                throw new SecurityException("No modify permission or carrier privilege.");
            }
        }
    }

    private int hasCarrierPrivileges() {
        if (this.mPhone == null) {
            log("hasCarrierPrivileges: mPhone is null");
            return -1;
        }
        UiccCard card = UiccController.getInstance().getUiccCard(this.mPhone.getPhone().getPhoneId());
        if (card != null) {
            return card.getCarrierPrivilegeStatusForCurrentTransaction(this.mContext.getPackageManager());
        }
        loge("hasCarrierPrivileges: No UICC");
        return -1;
    }

    private static void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }

    public boolean isSubDeactivedByPowerOff(long sub) {
        enforceReadPermission();
        Rlog.d(LOG_TAG, "isSubDeactivedByPowerOff: in HuaweiPhoneService");
        SubscriptionController sc = SubscriptionController.getInstance();
        if (TelephonyManager.getDefault().getSimState(sc.getSlotId((int) sub)) == 5 && sc.getSubState((int) sub) == 0) {
            return true;
        }
        return false;
    }

    public boolean isNeedToRadioPowerOn(long sub) {
        enforceReadPermission();
        Rlog.d(LOG_TAG, "isNeedToRadioPowerOn: in HuaweiPhoneService");
        if (HwModemCapability.isCapabilitySupport(9) || !(MultiSimVariants.DSDS == TelephonyManager.getDefault().getMultiSimConfiguration() || SystemProperties.getBoolean("ro.hwpp.set_uicc_by_radiopower", false))) {
            Rlog.d(LOG_TAG, "isNeedToRadioPowerOn: hisi dsds not in");
            int phoneId = SubscriptionController.getInstance().getPhoneId((int) sub);
            if (PhoneFactory.getPhone(phoneId) != null && PhoneFactory.getPhone(phoneId).getServiceState().getState() == 3) {
                return true;
            }
        }
        if (!isSubDeactivedByPowerOff((long) ((int) sub))) {
            return true;
        }
        SubscriptionController.getInstance().activateSubId((int) sub);
        return false;
    }

    private void enforceReadPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
    }

    public void updateCrurrentPhone(int lteSlot) {
        Rlog.d(LOG_TAG, "updateCrurrentPhone with lteSlot = " + lteSlot);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (TelephonyManager.getDefault().getPhoneCount() > lteSlot) {
            this.mPhone = this.mPhones[lteSlot];
        } else {
            Rlog.e(LOG_TAG, "Invalid slot ID");
        }
    }

    public void setDefaultDataSlotId(int slotId) {
        Rlog.d(LOG_TAG, "setDefaultDataSlotId: slotId = " + slotId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (slotId < 0 || slotId >= SIM_NUM) {
            Rlog.d(LOG_TAG, "setDefaultDataSlotId: invalid slotId!!!");
            return;
        }
        Global.putInt(this.mContext.getContentResolver(), USER_DATACALL_SUBSCRIPTION, slotId);
        if (SubscriptionController.getInstance() != null) {
            SubscriptionController.getInstance().setDefaultDataSubId(SubscriptionController.getInstance().getSubId(slotId)[0]);
        } else {
            Rlog.d(LOG_TAG, "SubscriptionController.getInstance()! null");
        }
        Rlog.d(LOG_TAG, "setDefaultDataSlotId done");
    }

    public int getDefault4GSlotId() {
        int subscription = 0;
        try {
            subscription = System.getInt(this.mContext.getContentResolver(), "switch_dual_card_slots");
        } catch (SettingNotFoundException e) {
            Rlog.d(LOG_TAG, "Settings Exception Reading Dual Sim Switch Dual Card Slots Values");
        }
        Rlog.d(LOG_TAG, "4GSlot: " + subscription);
        return subscription;
    }

    public void setDefault4GSlotId(int slotId, Message msg) {
        int uid = Binder.getCallingUid();
        if (!HwAllInOneController.IS_CMCC_4GSWITCH_DISABLE || uid == HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE || uid == HwVSimEventHandler.EVENT_VSIM_DISABLE_RETRY || uid == 0) {
            enforceModifyPermissionOrCarrierPrivilege();
            log("in setDefault4GSlotId.");
            HwAllInOneController.getInstance().setDefault4GSlot(slotId, msg);
            return;
        }
        loge("setDefault4GSlotId: Disallowed call for uid " + uid);
    }

    public boolean isSetDefault4GSlotIdEnabled() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        log("in isSetDefault4GSlotIdEnabled.");
        if (!TelephonyManager.getDefault().isMultiSimEnabled()) {
            return false;
        }
        SubscriptionController sc = SubscriptionController.getInstance();
        int[] sub1 = sc.getSubIdUsingSlotId(0);
        int[] sub2 = sc.getSubIdUsingSlotId(1);
        if (TelephonyManager.getDefault().getSimState(0) == 5 && sc.getSubState(sub1[0]) == 0 && TelephonyManager.getDefault().getSimState(1) == 5 && sc.getSubState(sub2[0]) == 0) {
            return false;
        }
        if (HwAllInOneController.IS_HISI_DSDX && ((TelephonyManager.getDefault().getSimState(0) == 5 && sc.getSubState(sub1[0]) == 0) || (TelephonyManager.getDefault().getSimState(1) == 5 && sc.getSubState(sub2[0]) == 0))) {
            log("isSetDefault4GSlotIdEnabled return false when has sim INACTIVE when IS_HISI_DSDS_AUTO_SWITCH_4G_SLOT");
            return false;
        } else if (!HwAllInOneController.IS_CMCC_4GSWITCH_DISABLE || !HwAllInOneController.getInstance().isCMCCHybird()) {
            return HwAllInOneController.getInstance().isSwitchDualCardSlotsEnabled();
        } else {
            log("isSetDefault4GSlotIdEnabled: CMCC hybird return false");
            return false;
        }
    }

    public void waitingSetDefault4GSlotDone(boolean waiting) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!HwModemCapability.isCapabilitySupport(9)) {
            HwAllInOneController.getInstance().setWaitingSwitchBalongSlot(waiting);
        }
    }

    public int getPreferredDataSubscription() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        int subId = SubscriptionController.getInstance().getPreferredDataSubscription();
        log("getPreferredDataSubscription return subId = " + subId);
        return subId;
    }

    public int getOnDemandDataSubId() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        int subId = SubscriptionController.getInstance().getOnDemandDataSubId();
        log("getOnDemandDataSubId return subId = " + subId);
        return subId;
    }

    public String getCdmaGsmImsi() {
        Rlog.d(LOG_TAG, "getCdmaGsmImsi: in HWPhoneService");
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        for (HwPhone hp : this.mPhones) {
            if (hp.getHwPhoneType() == 2) {
                return hp.getCdmaGsmImsi();
            }
        }
        return null;
    }

    public int getUiccCardType(int slotId) {
        Rlog.d(LOG_TAG, "getUiccCardType: in HwPhoneService");
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (slotId < 0 || slotId >= this.mPhones.length) {
            return this.mPhone.getUiccCardType();
        }
        return this.mPhones[slotId].getUiccCardType();
    }

    public boolean isCardUimLocked(int slotId) {
        log("isCardUimLocked for slotId " + slotId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        UiccCard card = UiccController.getInstance().getUiccCard(slotId);
        if (card != null) {
            return card.isCardUimLocked();
        }
        loge("isCardUimLocked: No UICC for slotId" + slotId);
        return false;
    }

    public int getSpecCardType(int slotId) {
        log("getSpecCardType for slotId " + slotId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        return HwAllInOneController.getInstance().getSpecCardType(slotId);
    }

    public boolean isRadioOn(int slot) {
        log("isRadioOn for slotId " + slot);
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (slot < 0 || slot >= this.mPhones.length) {
            return false;
        }
        return this.mPhones[slot].getPhone().isRadioOn();
    }

    public Bundle getCellLocation(int slotId) {
        Rlog.d(LOG_TAG, "getCellLocation: in HwPhoneService");
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        Bundle data = new Bundle();
        CellLocation cellLoc;
        if (slotId >= 0 && slotId < this.mPhones.length) {
            cellLoc = this.mPhones[slotId].getCellLocation();
            if (cellLoc != null) {
                cellLoc.fillInNotifierBundle(data);
            }
        } else if (sIsPlatformSupportVSim && slotId == 2) {
            cellLoc = getVSimPhone().getCellLocation();
            if (cellLoc != null) {
                cellLoc.fillInNotifierBundle(data);
            }
        } else {
            cellLoc = this.mPhone.getCellLocation();
            if (cellLoc != null) {
                cellLoc.fillInNotifierBundle(data);
            }
        }
        return data;
    }

    public String getCdmaMlplVersion() {
        Rlog.d(LOG_TAG, "getCdmaMlplVersion: in HwPhoneService");
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        for (HwPhone hp : this.mPhones) {
            if (hp.getHwPhoneType() == 2) {
                return hp.getCdmaMlplVersion();
            }
        }
        return null;
    }

    public String getCdmaMsplVersion() {
        Rlog.d(LOG_TAG, "getCdmaMsplVersion: in HwPhoneService");
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        for (HwPhone hp : this.mPhones) {
            if (hp.getHwPhoneType() == 2) {
                return hp.getCdmaMsplVersion();
            }
        }
        return null;
    }

    private boolean isSystemApp(String packageName) {
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            return false;
        }
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            if (!(appInfo == null || (appInfo.flags & 1) == 0)) {
                return true;
            }
        } catch (NameNotFoundException e) {
            Rlog.e(LOG_TAG, packageName + " not found.");
        }
        return false;
    }

    private boolean canReadPhoneState(String message) {
        boolean z = true;
        String str = null;
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", message);
            return true;
        } catch (SecurityException e) {
            PackageManager pm = this.mContext.getPackageManager();
            if (pm != null) {
                String[] callingPackageName = pm.getPackagesForUid(Binder.getCallingUid());
                str = callingPackageName[0];
                if (isSystemApp(callingPackageName[0])) {
                    Rlog.d(LOG_TAG, callingPackageName[0] + " allowed.");
                    return true;
                }
            }
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", message);
            if (this.mAppOps.noteOp(51, Binder.getCallingUid(), str) != 0) {
                z = false;
            }
            return z;
        }
    }

    public String getUniqueDeviceId(int scope) {
        isReadPhoneNumberBlocked();
        if (canReadPhoneState("getDeviceId")) {
            String sharedPref = DEVICEID_PREF;
            if (1 == scope) {
                sharedPref = IMEI_PREF;
            }
            String deviceId = getDeviceIdFromSP(sharedPref);
            if (deviceId != null) {
                return deviceId;
            }
            String newDeviceId = readDeviceIdFromLL(scope);
            if (!(newDeviceId == null || newDeviceId.matches("^0*$"))) {
                deviceId = newDeviceId;
                saveDeviceIdToSP(newDeviceId, sharedPref);
            }
            if (TextUtils.isEmpty(deviceId) && isWifiOnly(this.mContext)) {
                Rlog.d(LOG_TAG, "Current is wifi-only version, return SN number as DeviceId");
                deviceId = Build.SERIAL;
            }
            return deviceId;
        }
        Rlog.e(LOG_TAG, "getUniqueDeviceId can't read phone state.");
        return null;
    }

    private boolean isReadPhoneNumberBlocked() {
        try {
            return ProxyController.getInstance().getPhoneSubInfoController().isReadPhoneNumberBlocked();
        } catch (Exception e) {
            Rlog.d(LOG_TAG, "isReadPhoneNumberBlocked, exception=" + e);
            return false;
        }
    }

    private void saveDeviceIdToSP(String deviceId, String sharedPref) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        editor.putString(sharedPref, deviceId);
        editor.commit();
    }

    private String getDeviceIdFromSP(String sharedPref) {
        return PreferenceManager.getDefaultSharedPreferences(this.mContext).getString(sharedPref, null);
    }

    private String readDeviceIdFromLL(int scope) {
        int phoneId = 0;
        if (HwModemCapability.isCapabilitySupport(15) && TelephonyManager.getDefault().isMultiSimEnabled() && SystemProperties.getBoolean("persist.sys.dualcards", false)) {
            if (getWaitingSwitchBalongSlot()) {
                Rlog.d(LOG_TAG, "readDeviceIdFromLL getWaitingSwitchBalongSlot");
                return null;
            }
            phoneId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        }
        Rlog.d(LOG_TAG, "readDeviceIdFromLL: phoneId=" + phoneId);
        if (this.mPhones == null || this.mPhones[phoneId] == null) {
            return null;
        }
        if (1 == scope) {
            return this.mPhones[phoneId].getImei();
        }
        if (HuaweiTelephonyConfigs.isChinaTelecom()) {
            return this.mPhones[phoneId].getMeid();
        }
        String deviceId = this.mPhones[phoneId].getImei();
        if (deviceId != null) {
            return deviceId;
        }
        return this.mPhones[phoneId].getMeid();
    }

    public void testVoiceLoopBack(int mode) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        for (HwPhone phone : this.mPhones) {
            phone.testVoiceLoopBack(mode);
        }
    }

    private Phone getVSimPhone() {
        return HwVSimPhoneFactory.getVSimPhone();
    }

    public boolean getWaitingSwitchBalongSlot() {
        log("getWaitingSwitchBalongSlot start");
        return HwAllInOneController.getInstance().getWaitingSwitchBalongSlot();
    }

    public boolean setISMCOEX(String setISMCoex) {
        enforceModifyPermissionOrCarrierPrivilege();
        int phoneId = 0;
        if (TelephonyManager.getDefault().isMultiSimEnabled() && SystemProperties.getBoolean("persist.sys.dualcards", false)) {
            phoneId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        }
        if (this.mPhones == null || this.mPhones[phoneId] == null) {
            loge("mPhones is invalid!");
            return false;
        }
        log("setISMCoex =" + setISMCoex);
        return this.mPhones[phoneId].setISMCOEX(setISMCoex);
    }

    public boolean isDomesticCard(int slotId) {
        log("isDomesticCard start");
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (HwForeignUsimForTelecom.IS_OVERSEA_USIM_SUPPORT) {
            return HwForeignUsimForTelecom.getInstance().isDomesticCard(slotId);
        }
        return true;
    }

    public boolean setWifiTxPower(int power) {
        Rlog.d(LOG_TAG, "setWifiTxPower: start=" + power);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        Rlog.d(LOG_TAG, "setWifiTxPower: end=" + power);
        return true;
    }

    public boolean setCellTxPower(int power) {
        Rlog.d(LOG_TAG, "setCellTxPower: start=" + power);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        getCommandsInterface().setPowerGrade(power, null);
        Rlog.d(LOG_TAG, "setCellTxPower: end=" + power);
        return true;
    }

    private CommandsInterface getCommandsInterface() {
        return PhoneFactory.getDefaultPhone().mCi;
    }

    public String[] queryServiceCellBand() {
        Rlog.d(LOG_TAG, "queryServiceCellBand");
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        Phone phone = PhoneFactory.getPhone(SubscriptionController.getInstance().getDefaultDataSubId());
        if (ServiceState.isCdma(phone.getServiceState().getRilDataRadioTechnology())) {
            this.mServiceCellBand = new String[2];
            this.mServiceCellBand[0] = "CDMA";
            this.mServiceCellBand[1] = "0";
        } else {
            synchronized (this.mLock) {
                phone.mCi.queryServiceCellBand(this.mMainHandler.obtainMessage(6));
                boolean isWait = true;
                while (isWait) {
                    try {
                        this.mLock.wait();
                        isWait = false;
                    } catch (InterruptedException e) {
                        log("interrupted while trying to update by index");
                    }
                }
            }
        }
        if (this.mServiceCellBand == null) {
            return new String[0];
        }
        return (String[]) this.mServiceCellBand.clone();
    }

    public void handleQueryCellBandDone(Message msg) {
        AsyncResult ar = msg.obj;
        if (ar == null || ar.exception != null) {
            this.mServiceCellBand = null;
        } else {
            this.mServiceCellBand = (String[]) ar.result;
        }
        synchronized (this.mLock) {
            this.mLock.notifyAll();
        }
    }

    public boolean registerForRadioAvailable(IPhoneCallback callback) {
        log("registerForRadioAvailable");
        if (TelephonyManager.getDefault().isMultiSimEnabled() && getDefault4GSlotId() == 1) {
            this.mPhone = this.mPhones[1];
        } else {
            this.mPhone = this.mPhones[0];
        }
        if (this.mPhone == null) {
            loge("phone is null!");
            return false;
        }
        this.mRadioAvailableIndCB = callback;
        this.mPhone.getPhone().mCi.registerForAvailable(this.mMainHandler, 51, null);
        return true;
    }

    private void handleRadioAvailableInd(Message msg) {
        AsyncResult ar = msg.obj;
        if (this.mRadioAvailableIndCB == null) {
            loge("handleRadioAvailableInd mRadioAvailableIndCB is null");
            return;
        }
        if (ar.exception == null) {
            try {
                this.mRadioAvailableIndCB.onCallback1(0);
            } catch (RemoteException ex) {
                loge("handleRadioAvailableInd RemoteException: ex = " + ex);
            }
        } else {
            loge("radio available ind exception: " + ar.exception);
        }
    }

    public boolean unregisterForRadioAvailable(IPhoneCallback callback) {
        log("unregisterForRadioAvailable");
        if (TelephonyManager.getDefault().isMultiSimEnabled() && getDefault4GSlotId() == 1) {
            this.mPhone = this.mPhones[1];
        } else {
            this.mPhone = this.mPhones[0];
        }
        if (this.mPhone == null) {
            loge("phone is null!");
            return false;
        }
        this.mRadioAvailableIndCB = null;
        this.mPhone.getPhone().mCi.unregisterForAvailable(this.mMainHandler);
        return true;
    }

    public boolean registerForRadioNotAvailable(IPhoneCallback callback) {
        log("registerForRadioNotAvailable");
        if (TelephonyManager.getDefault().isMultiSimEnabled() && getDefault4GSlotId() == 1) {
            this.mPhone = this.mPhones[1];
        } else {
            this.mPhone = this.mPhones[0];
        }
        if (this.mPhone == null) {
            loge("phone is null!");
            return false;
        }
        this.mRadioNotAvailableIndCB = callback;
        this.mPhone.getPhone().mCi.registerForNotAvailable(this.mMainHandler, 52, null);
        return true;
    }

    private void handleRadioNotAvailableInd(Message msg) {
        AsyncResult ar = msg.obj;
        if (this.mRadioNotAvailableIndCB == null) {
            loge("handleRadioNotAvailableInd mRadioNotAvailableIndCB is null");
            return;
        }
        if (ar.exception == null) {
            try {
                this.mRadioNotAvailableIndCB.onCallback1(0);
            } catch (RemoteException ex) {
                loge("handleRadioNotAvailableInd RemoteException: ex = " + ex);
            }
        } else {
            loge("radio not available ind exception: " + ar.exception);
        }
    }

    public boolean unregisterForRadioNotAvailable(IPhoneCallback callback) {
        log("unregisterForRadioNotAvailable");
        if (TelephonyManager.getDefault().isMultiSimEnabled() && getDefault4GSlotId() == 1) {
            this.mPhone = this.mPhones[1];
        } else {
            this.mPhone = this.mPhones[0];
        }
        if (this.mPhone == null) {
            loge("phone is null!");
            return false;
        }
        this.mRadioNotAvailableIndCB = null;
        this.mPhone.getPhone().mCi.unregisterForNotAvailable(this.mMainHandler);
        return true;
    }

    public boolean registerCommonImsaToMapconInfo(IPhoneCallback callback) {
        log("registerCommonImsaToMapconInfo");
        if (TelephonyManager.getDefault().isMultiSimEnabled() && getDefault4GSlotId() == 1) {
            this.mPhone = this.mPhones[1];
        } else {
            this.mPhone = this.mPhones[0];
        }
        if (this.mPhone == null) {
            loge("phone is null!");
            return false;
        }
        this.mImsaToMapconInfoCB = callback;
        this.mPhone.getPhone().mCi.registerCommonImsaToMapconInfo(this.mMainHandler, 53, null);
        return true;
    }

    private void handleCommonImsaToMapconInfoInd(Message msg) {
        AsyncResult ar = msg.obj;
        log("handleCommonImsaToMapconInfoInd");
        if (this.mImsaToMapconInfoCB == null) {
            loge("handleCommonImsaToMapconInfoInd mImsaToMapconInfoCB is null");
            return;
        }
        if (ar.exception == null) {
            byte[] data = ar.result;
            Bundle bundle = new Bundle();
            bundle.putByteArray("imsa2mapcon_msg", data);
            try {
                this.mImsaToMapconInfoCB.onCallback3(0, 0, bundle);
            } catch (RemoteException ex) {
                loge("handleCommonImsaToMapconInfoInd RemoteException: ex = " + ex);
            }
        } else {
            loge("imsa to mapcon info exception: " + ar.exception);
        }
    }

    public boolean unregisterCommonImsaToMapconInfo(IPhoneCallback callback) {
        log("unregisterCommonImsaToMapconInfo");
        if (TelephonyManager.getDefault().isMultiSimEnabled() && getDefault4GSlotId() == 1) {
            this.mPhone = this.mPhones[1];
        } else {
            this.mPhone = this.mPhones[0];
        }
        if (this.mPhone == null) {
            loge("phone is null!");
            return false;
        }
        this.mImsaToMapconInfoCB = null;
        this.mPhone.getPhone().mCi.unregisterCommonImsaToMapconInfo(this.mMainHandler);
        return true;
    }

    public boolean isRadioAvailable() {
        if (TelephonyManager.getDefault().isMultiSimEnabled() && getDefault4GSlotId() == 1) {
            this.mPhone = this.mPhones[1];
        } else {
            this.mPhone = this.mPhones[0];
        }
        if (this.mPhone != null) {
            return this.mPhone.isRadioAvailable();
        }
        loge("phone is null!");
        return false;
    }

    public void setImsSwitch(boolean value) {
        if (TelephonyManager.getDefault().isMultiSimEnabled() && getDefault4GSlotId() == 1) {
            this.mPhone = this.mPhones[1];
        } else {
            this.mPhone = this.mPhones[0];
        }
        if (this.mPhone == null) {
            loge("phone is null!");
        } else {
            this.mPhone.setImsSwitch(value);
        }
    }

    public boolean getImsSwitch() {
        if (TelephonyManager.getDefault().isMultiSimEnabled() && getDefault4GSlotId() == 1) {
            this.mPhone = this.mPhones[1];
        } else {
            this.mPhone = this.mPhones[0];
        }
        if (this.mPhone == null) {
            loge("phone is null!");
            return false;
        }
        boolean result = this.mPhone.getImsSwitch();
        Rlog.d(LOG_TAG, "ImsSwitch = " + result);
        return result;
    }

    public void setImsDomainConfig(int domainType) {
        if (TelephonyManager.getDefault().isMultiSimEnabled() && getDefault4GSlotId() == 1) {
            this.mPhone = this.mPhones[1];
        } else {
            this.mPhone = this.mPhones[0];
        }
        if (this.mPhone == null) {
            loge("phone is null!");
        } else {
            this.mPhone.setImsDomainConfig(domainType);
        }
    }

    public boolean handleMapconImsaReq(byte[] Msg) {
        if (TelephonyManager.getDefault().isMultiSimEnabled() && getDefault4GSlotId() == 1) {
            this.mPhone = this.mPhones[1];
        } else {
            this.mPhone = this.mPhones[0];
        }
        if (this.mPhone == null) {
            loge("phone is null!");
            return false;
        }
        this.mPhone.handleMapconImsaReq(Msg);
        return true;
    }

    public int getUiccAppType() {
        if (TelephonyManager.getDefault().isMultiSimEnabled() && getDefault4GSlotId() == 1) {
            this.mPhone = this.mPhones[1];
        } else {
            this.mPhone = this.mPhones[0];
        }
        if (this.mPhone != null) {
            return this.mPhone.getUiccAppType();
        }
        loge("phone is null!");
        return 0;
    }

    public int getImsDomain() {
        if (this.mPhone == null) {
            loge("phone is null!");
            return -1;
        } else if (ImsManager.isWfcEnabledByPlatform(this.mContext)) {
            return ((int[]) sendRequest(CMD_IMS_GET_DOMAIN, null))[0];
        } else {
            log("vowifi not support!");
            return -1;
        }
    }

    public UiccAuthResponse handleUiccAuth(int auth_type, byte[] rand, byte[] auth) {
        if (this.mPhone != null) {
            return (UiccAuthResponse) sendRequest(CMD_UICC_AUTH, new UiccAuthPara(auth_type, rand, auth));
        }
        loge("phone is null!");
        return null;
    }

    private void initPrefNetworkTypeChecker() {
        TelephonyManager mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        if (mTelephonyManager != null && !mTelephonyManager.isMultiSimEnabled() && HwModemCapability.isCapabilitySupport(9)) {
            log("initPrefNetworkTypeChecker");
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
        }
    }

    private void handleGetPrefNetworkTypeDone(Message msg) {
        Rlog.d(LOG_TAG, "handleGetPrefNetworkTypeDone");
        AsyncResult ar = msg.obj;
        if (ar.exception == null) {
            int prefNetworkType = ((int[]) ar.result)[0];
            int currentprefNetworkTypeInDB = getCurrentNetworkTypeFromDB();
            log("prefNetworkType:" + prefNetworkType + " currentprefNetworkTypeInDB:" + currentprefNetworkTypeInDB);
            if (prefNetworkType == currentprefNetworkTypeInDB) {
                return;
            }
            if (currentprefNetworkTypeInDB == -1 || this.mPhone == null) {
                log("INVALID_NETWORK_MODE in DB,set 4G-Switch on");
                setLteServiceAbility(1);
                return;
            }
            this.mPhone.setPreferredNetworkType(currentprefNetworkTypeInDB, this.mMainHandler.obtainMessage(5, Integer.valueOf(currentprefNetworkTypeInDB)));
            log("setPreferredNetworkType -> currentprefNetworkTypeInDB:" + currentprefNetworkTypeInDB);
            return;
        }
        log("getPreferredNetworkType exception=" + ar.exception);
    }

    private static boolean isWifiOnly(Context context) {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService("phone");
        boolean isWifiOnly = false;
        if (telephony != null) {
            isWifiOnly = (telephony.isVoiceCapable() || telephony.isSmsCapable()) ? false : true;
            Rlog.d(LOG_TAG, "isWifiOnly:" + isWifiOnly);
        }
        return isWifiOnly;
    }

    public boolean registerForWirelessState(int type, int slotId, IPhoneCallback callback) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        boolean isSuccess = handleWirelessStateRequest(1, type, slotId, callback);
        log("registerForWirelessState type=" + type + ",isSuccess=" + isSuccess);
        return isSuccess;
    }

    public boolean unregisterForWirelessState(int type, int slotId, IPhoneCallback callback) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        boolean isSuccess = handleWirelessStateRequest(2, type, slotId, callback);
        log("unregisterForWirelessState type=" + type + ",isSuccess=" + isSuccess);
        return isSuccess;
    }

    public boolean setMaxTxPower(int type, int power) {
        Rlog.d(LOG_TAG, "setMaxTxPower: start=" + power);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (type == 2) {
            getCommandsInterface().setWifiTxPowerGrade(power, null);
        } else if (type == 1) {
            getCommandsInterface().setPowerGrade(power, null);
        }
        Rlog.d(LOG_TAG, "setMaxTxPower: end=" + power);
        return false;
    }

    private boolean handleWirelessStateRequest(int opertype, int type, int slotId, IPhoneCallback callback) {
        log("In handleWirelessStateRequest service type=" + type + ",slotId=" + slotId);
        boolean isSuccess = false;
        CommandsInterface ci = getCommandsInterface(slotId);
        if (callback == null) {
            loge("handleWirelessStateRequest callback is null.");
            return false;
        } else if (ci == null) {
            loge("handleWirelessStateRequest ci is null.");
            return false;
        } else {
            switch (opertype) {
                case 1:
                    isSuccess = registerUnitSarControl(type, slotId, ci, callback);
                    break;
                case 2:
                    isSuccess = unregisterUnitSarControl(type, slotId, ci, callback);
                    break;
            }
            log("handleWirelessStateRequest type=" + type + ",isSuccess=" + isSuccess);
            return isSuccess;
        }
    }

    private boolean isValidSlotId(int slotId) {
        return slotId >= 0 && slotId < SIM_NUM;
    }

    private void handleSarInfoUploaded(int type, Message msg) {
        log("in handleSarInfoUploaded.");
        AsyncResult ar = msg.obj;
        if (ar == null || ar.exception != null) {
            loge("handleSarInfoUploaded error, ar exception.");
            return;
        }
        int slotId = ((Integer) ar.userObj).intValue();
        CommandsInterface ci = getCommandsInterface(slotId);
        if (ci == null) {
            loge("handleSarInfoUploaded ci is null.");
            return;
        }
        if (!handleSarDataFromModem(type, ar)) {
            log("handleSarInfoUploaded hasClient is false,so to close the switch of upload Sar Info in modem");
            closeSarInfoUploadSwitch(type, slotId, ci);
            unregisterSarRegistrant(type, slotId, ci);
        }
    }

    private boolean handleSarDataFromModem(int type, AsyncResult ar) {
        log("handleSarDataFromModem start");
        boolean hasClient = false;
        int slotId = ((Integer) ar.userObj).intValue();
        Bundle bundle = getBundleData(ar);
        Object[] callbackArray = getCallbackArray(type);
        if (callbackArray == null) {
            loge("handleSarDataFromModem callbackArray is null.");
            return false;
        }
        synchronized (callbackArray[slotId]) {
            ArrayList<Record> callbackList = callbackArray[slotId];
            for (int i = callbackList.size() - 1; i >= 0; i--) {
                Record r = (Record) callbackList.get(i);
                try {
                    r.callback.onCallback3(type, slotId, bundle);
                    hasClient = true;
                    log("handleSarDataFromModem oncallback r=" + r);
                } catch (RemoteException e) {
                    log("handleSarDataFromModem callback false,ex is RemoteException");
                    callbackList.remove(r);
                } catch (Exception e2) {
                    log("handleSarDataFromModem callback false,ex is Exception");
                    callbackList.remove(r);
                }
            }
            log("handleSarDataFromModem record size=" + callbackList.size());
        }
        return hasClient;
    }

    private boolean closeSarInfoUploadSwitch(int type, int slotId, CommandsInterface ci) {
        boolean isSuccess = false;
        switch (type) {
            case 1:
                ci.closeSwitchOfUploadBandClass(this.mMainHandler.obtainMessage(10, Integer.valueOf(slotId)));
                isSuccess = true;
                break;
            case 2:
                ci.closeSwitchOfUploadAntOrMaxTxPower(2);
                isSuccess = true;
                break;
            case 4:
                ci.closeSwitchOfUploadAntOrMaxTxPower(4);
                isSuccess = true;
                break;
        }
        log("closeSarInfoUploadSwitch mPhones[" + slotId + "]: type = " + type + ",isSuccess = " + isSuccess);
        return isSuccess;
    }

    private Bundle getBundleData(AsyncResult ar) {
        log("getBundleData start");
        if (ar == null) {
            Rlog.d(LOG_TAG, "getBundleData: ar is null");
            return null;
        }
        ByteBuffer buf = ByteBuffer.wrap((byte[]) ar.result, 0, 4);
        buf.order(ByteOrder.nativeOrder());
        int result = buf.getInt();
        Bundle bundle = new Bundle();
        bundle.putInt(KEY1, result);
        log("getBundleData result = " + result);
        return bundle;
    }

    private CommandsInterface getCommandsInterface(int slotId) {
        if (isValidSlotId(slotId)) {
            return PhoneFactory.getPhones()[slotId].mCi;
        }
        log("getCommandsInterface the slotId is invalid");
        return null;
    }

    private Object[] getCallbackArray(int type) {
        Object[] callbackArray = null;
        switch (type) {
            case 1:
                callbackArray = this.mRegBandClassCallbackArray;
                break;
            case 2:
                callbackArray = this.mRegAntStateCallbackArray;
                break;
            case 4:
                callbackArray = this.mRegMaxTxPowerCallbackArray;
                break;
        }
        log("getCallbackArray type=" + type);
        return callbackArray;
    }

    private boolean unregisterUnitSarControl(int type, int slotId, CommandsInterface ci, IPhoneCallback callback) {
        boolean hasFind = false;
        Object[] callbackArray = getCallbackArray(type);
        if (callbackArray == null) {
            loge("unregisterUnitSarControl callbackArray is null.");
            return false;
        }
        boolean isSuccess;
        synchronized (callbackArray[slotId]) {
            ArrayList<Record> callbackList = callbackArray[slotId];
            IBinder b = callback.asBinder();
            int recordCount = callbackList.size();
            log("callbackArray[" + slotId + "] lenght is " + recordCount);
            for (int i = recordCount - 1; i >= 0; i--) {
                if (((Record) callbackList.get(i)).binder == b) {
                    log("unregisterUnitSarControl remove: " + ((Record) callbackList.get(i)));
                    callbackList.remove(i);
                    hasFind = true;
                    break;
                }
            }
            if (hasFind) {
                recordCount = callbackList.size();
                log("unregisterUnitSarControl record size = " + recordCount);
                if (recordCount == 0 && closeSarInfoUploadSwitch(type, slotId, ci)) {
                    isSuccess = unregisterSarRegistrant(type, slotId, ci);
                } else {
                    isSuccess = true;
                }
            } else {
                log("unregisterUnitSarControl not find the callback,type=" + type);
                isSuccess = true;
            }
        }
        return isSuccess;
    }

    private boolean registerUnitSarControl(int type, int slotId, CommandsInterface ci, IPhoneCallback callback) {
        log("registerUnitSarControl start slotId=" + slotId + ",type=" + type);
        boolean hasFind = false;
        Object[] callbackArray = getCallbackArray(type);
        if (callbackArray == null) {
            loge("registerUnitSarControl callbackArray is null.");
            return false;
        } else if (registerSarRegistrant(type, slotId, ci)) {
            synchronized (callbackArray[slotId]) {
                ArrayList<Record> callbackList = callbackArray[slotId];
                IBinder b = callback.asBinder();
                int N = callbackList.size();
                for (int i = 0; i < N; i++) {
                    if (b == ((Record) callbackList.get(i)).binder) {
                        hasFind = true;
                        break;
                    }
                }
                if (!hasFind) {
                    Record r = new Record();
                    r.binder = b;
                    r.callback = callback;
                    callbackList.add(r);
                    log("registerUnitSarControl: add new record");
                }
                log("registerUnitSarControl record size=" + callbackList.size());
            }
            return openSarInfoUploadSwitch(type, slotId, ci);
        } else {
            loge("registerUnitSarControl mPhones[" + slotId + "] register return false");
            return false;
        }
    }

    private boolean openSarInfoUploadSwitch(int type, int slotId, CommandsInterface ci) {
        boolean isSuccess = false;
        switch (type) {
            case 1:
                ci.openSwitchOfUploadBandClass(this.mMainHandler.obtainMessage(10, Integer.valueOf(slotId)));
                isSuccess = true;
                break;
            case 2:
                ci.openSwitchOfUploadAntOrMaxTxPower(2);
                isSuccess = true;
                break;
            case 4:
                ci.openSwitchOfUploadAntOrMaxTxPower(4);
                isSuccess = true;
                break;
        }
        log("openSarInfoUploadSwitch mPhones[" + slotId + "]: type = " + type + ",isSuccess = " + isSuccess);
        return isSuccess;
    }

    private boolean registerSarRegistrant(int type, int slotId, CommandsInterface ci) {
        boolean isSuccess = false;
        Message message = null;
        switch (type) {
            case 1:
                message = this.mMainHandler.obtainMessage(10, Integer.valueOf(slotId));
                break;
            case 2:
                message = this.mMainHandler.obtainMessage(11, Integer.valueOf(slotId));
                break;
            case 4:
                message = this.mMainHandler.obtainMessage(12, Integer.valueOf(slotId));
                break;
        }
        if (message != null && ci.registerSarRegistrant(type, message)) {
            isSuccess = true;
        }
        loge("registerSarRegistrant mPhones[" + slotId + "]: type = " + type + ",isSuccess = " + (!isSuccess));
        return isSuccess;
    }

    private boolean unregisterSarRegistrant(int type, int slotId, CommandsInterface ci) {
        boolean isSuccess = false;
        Message message = null;
        switch (type) {
            case 1:
                message = this.mMainHandler.obtainMessage(10, Integer.valueOf(slotId));
                break;
            case 2:
                message = this.mMainHandler.obtainMessage(11, Integer.valueOf(slotId));
                break;
            case 4:
                message = this.mMainHandler.obtainMessage(12, Integer.valueOf(slotId));
                break;
        }
        if (message != null && ci.unregisterSarRegistrant(type, message)) {
            isSuccess = true;
        }
        loge("unregisterSarRegistrant mPhones[" + slotId + "]: type = " + type + ",isSuccess = " + (!isSuccess));
        return isSuccess;
    }

    public boolean cmdForECInfo(int event, int action, byte[] buf) {
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
            boolean res = false;
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                for (HwPhone phone : this.mPhones) {
                    if (phone != null && phone.isCDMAPhone()) {
                        if (phone.cmdForECInfo(event, action, buf)) {
                            res = true;
                        } else {
                            res = requestForECInfo(phone, event, action, buf);
                        }
                    }
                }
            } else if (this.mPhone != null && this.mPhone.isCDMAPhone()) {
                res = !this.mPhone.cmdForECInfo(event, action, buf) ? requestForECInfo(this.mPhone, event, action, buf) : true;
            }
            return res;
        } catch (Exception ex) {
            Rlog.e(LOG_TAG, "cmdForECInfo fail:" + ex);
            return false;
        }
    }

    private void registerForRadioOnInner() {
        Rlog.d(LOG_TAG, "registerForRadioOnInner");
        if (this.mPhone == null) {
            Rlog.e(LOG_TAG, "registerForRadioOnInner failed, phone is null!");
        } else {
            this.mPhone.getPhone().mCi.registerForOn(this.mMainHandler, EVENT_QUERY_ENCRYPT_FEATURE, null);
        }
    }

    private void handleEventQueryEncryptCall(HwPhone phone) {
        int i = 1;
        if (phone != null && phone.isCDMAPhone()) {
            byte[] req = new byte[]{(byte) 0};
            if (HuaweiTelephonyConfigs.isQcomPlatform()) {
                this.mSupportEncryptCall = phone.cmdForECInfo(7, 0, req);
                if (this.mSupportEncryptCall) {
                    SystemProperties.set("persist.sys.cdma_encryption", Boolean.toString(this.mSupportEncryptCall));
                }
                CommandsInterface ci = getCommandsInterface(phone.getPhone().getPhoneId());
                if (ci != null) {
                    if (!((HwQualcommRIL) ci).getEcCdmaCallVersion()) {
                        i = 0;
                    }
                    this.mEncryptCallStatus = i;
                    checkEcSwitchStatusInNV(phone, this.mEncryptCallStatus);
                    return;
                }
                loge("qcomRil is null");
                return;
            }
            Message msg = this.mMainHandler.obtainMessage(EVENT_QUERY_ENCRYPT_FEATURE_DONE, phone);
            msg.arg1 = queryCount;
            phone.requestForECInfo(msg, 7, req);
            queryCount++;
            Rlog.d(LOG_TAG, "query EncryptCall Count : " + queryCount);
        }
    }

    private boolean requestForECInfo(HwPhone phone, int event, int action, byte[] buf) {
        EncryptCallPara para;
        boolean z = true;
        switch (event) {
            case 3:
            case 5:
            case 6:
            case 7:
                para = new EncryptCallPara(phone, event, null);
                break;
            default:
                para = new EncryptCallPara(phone, event, buf);
                break;
        }
        byte[] res = (byte[]) sendRequest(200, para);
        int len = res.length;
        if (len == 1) {
            if (res[0] <= (byte) 0 || ((byte) (res[0] & 15)) != (byte) 1) {
                z = false;
            }
            return z;
        } else if (buf == null || 1 >= len || len > buf.length) {
            return false;
        } else {
            System.arraycopy(res, 0, buf, 0, len);
            Rlog.d(LOG_TAG, "requestForECInfo res length:" + len);
            return true;
        }
    }

    public boolean isCtSimCard(int slotId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        int numPhones = TelephonyManager.getDefault().getPhoneCount();
        if (slotId < 0 || slotId >= numPhones) {
            return false;
        }
        return this.mPhones[slotId].isCtSimCard();
    }

    public void notifyCModemStatus(int status, IPhoneCallback callback) {
        if (Binder.getCallingUid() == HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE) {
            Message msg = this.mMainHandler.obtainMessage(EVENT_NOTIFY_CMODEM_STATUS);
            msg.obj = callback;
            try {
                getCommandsInterface().notifyCModemStatus(status, msg);
            } catch (NullPointerException e) {
                loge("notifyCModemStatus got e = " + e);
                e.printStackTrace();
                if (callback != null) {
                    try {
                        callback.onCallback1(-1);
                    } catch (RemoteException ex) {
                        Rlog.e(LOG_TAG, "notifyCModemStatus onCallback1 RemoteException:" + ex);
                    }
                }
            }
        }
    }

    public boolean notifyDeviceState(String device, String state, String extra) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "notifyDeviceState");
        if (TextUtils.isEmpty(device) || TextUtils.isEmpty(state)) {
            return false;
        }
        try {
            getCommandsInterface().notifyDeviceState(device, state, extra, this.mMainHandler.obtainMessage(EVENT_NOTIFY_DEVICE_STATE));
            return true;
        } catch (NullPointerException e) {
            loge("notifyDeviceState got e = " + e);
            return false;
        }
    }

    private void checkEcSwitchStatusInNV(HwPhone phone, int statusInNV) {
        if (phone != null && phone.isCDMAPhone()) {
            int statusInDB = Secure.getInt(this.mContext.getContentResolver(), "encrypt_version", 0);
            log("checkEcSwitchStatus, encryptCall statusInNV=" + statusInNV + " statusInDB=" + statusInDB);
            if (statusInNV != statusInDB) {
                int action = statusInDB;
                byte[] buf = new byte[]{(byte) statusInDB};
                if (!HuaweiTelephonyConfigs.isQcomPlatform()) {
                    phone.requestForECInfo(this.mMainHandler.obtainMessage(EVENT_QUERY_ENCRYPT_FEATURE_DONE, phone), 8, buf);
                } else if (phone.cmdForECInfo(8, statusInDB, buf)) {
                    log("qcom reset NV success.");
                } else {
                    loge("qcom reset NV fail!");
                }
            }
        }
    }
}
