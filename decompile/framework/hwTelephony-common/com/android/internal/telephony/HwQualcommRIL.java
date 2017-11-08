package com.android.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.provider.Settings.System;
import android.telephony.Rlog;
import com.android.ims.ImsManager;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccCardStatus;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class HwQualcommRIL extends RIL {
    private static final int BUF_NOT_ENOUGH = -3;
    private static final int BYTE_SIZE = 1;
    private static final byte ERROR = (byte) -1;
    private static final int FILE_NOT_EXIST = -2;
    private static Class HWNV = null;
    private static final int INT_SIZE = 4;
    private static final String ITEM_FILE_CA_DISABLE = "/nv/item_files/modem/lte/common/ca_disable";
    private static final int LOCK = 1;
    private static final int OEMHOOK_BASE = 524288;
    private static final int OEMHOOK_EVT_HOOK_CSG_PERFORM_NW_SCAN = 524438;
    private static final int OEMHOOK_EVT_HOOK_CSG_SET_SYS_SEL_PREF = 524439;
    private static final int OEM_HOOK_RAW_REQUEST_HEADERSIZE = ("QOEMHOOK".length() + 8);
    private static final int OPEN = 1;
    private static final int READ_EFS_FAIL = -1;
    private static final String RILJ_LOG_TAG = "RILJ-HwQualcommRIL";
    private static final int UNLOCK = 0;
    private static final int VALUE_SIZE = 1;
    private static final int WRITE_FAIL = 0;
    private static final int WRITE_SUCCESS = 1;
    private Integer mRilInstanceId = null;

    public static boolean checkExistinSys(String path) {
        if (new File(Environment.getRootDirectory().getPath() + "/framework", path).exists()) {
            return true;
        }
        return false;
    }

    public static synchronized Class getHWNV() {
        Class cls;
        synchronized (HwQualcommRIL.class) {
            Rlog.d(RILJ_LOG_TAG, "Enter HwQualcommRIL getClass");
            if (HWNV == null) {
                String realProvider = "com.huawei.android.hwnv.HWNVFuncation";
                String realProviderPath = "system/framework/hwnv.jar";
                String realProviderPath_vendor = "/system/vendor/framework/hwnv.jar";
                try {
                    PathClassLoader classLoader;
                    if (checkExistinSys("hwnv.jar")) {
                        classLoader = new PathClassLoader(realProviderPath, ClassLoader.getSystemClassLoader());
                    } else {
                        classLoader = new PathClassLoader(realProviderPath_vendor, ClassLoader.getSystemClassLoader());
                    }
                    HWNV = classLoader.loadClass(realProvider);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (RuntimeException e2) {
                    e2.printStackTrace();
                }
            }
            cls = HWNV;
        }
        return cls;
    }

    public HwQualcommRIL(Context context, int preferredNetworkType, int cdmaSubscription) {
        super(context, preferredNetworkType, cdmaSubscription, null);
    }

    public HwQualcommRIL(Context context, int preferredNetworkType, int cdmaSubscription, Integer instanceId) {
        super(context, preferredNetworkType, cdmaSubscription, instanceId);
        this.mRilInstanceId = instanceId;
    }

    public String getNVESN() {
        Class hwnv = getHWNV();
        if (hwnv == null) {
            return null;
        }
        try {
            return (String) hwnv.getMethod("getNVESN", new Class[0]).invoke(null, new Object[0]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        } catch (RuntimeException e2) {
            e2.printStackTrace();
            return null;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return null;
        } catch (InvocationTargetException e4) {
            e4.printStackTrace();
            return null;
        }
    }

    public Object processSolicitedEx(int rilRequest, Parcel p) {
        Object ret = super.processSolicitedEx(rilRequest, p);
        Rlog.d(RILJ_LOG_TAG, "Enter HwQualcommRIL processSolicitedEx,just for test");
        if (ret != null) {
            return ret;
        }
        switch (rilRequest) {
            case 136:
                ret = null;
                break;
            case 528:
                ret = Integer.valueOf(responsecardType(p));
                break;
            default:
                return ret;
        }
        return ret;
    }

    public void queryServiceCellBand(Message result) {
        Rlog.d(RILJ_LOG_TAG, "sending response as NULL");
        sendResponseToTarget(result, 2);
    }

    public boolean openSwitchOfUploadAntOrMaxTxPower(int mask) {
        riljLog("openSwitchOfUploadAntOrMaxTxPower, mask = " + mask);
        boolean z = false;
        Class hwnv = getHWNV();
        if (hwnv == null) {
            return false;
        }
        try {
            z = ((Boolean) hwnv.getMethod("registerModemGenericIndication", new Class[]{Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(mask)})).booleanValue();
            riljLog("registModem.invoke() result is " + z + ",mask = " + mask);
            return z;
        } catch (NoSuchMethodException e) {
            riljLog("occur NoSuchMethodException!");
            return z;
        } catch (RuntimeException e2) {
            riljLog("occur RuntimeException!");
            return z;
        } catch (IllegalAccessException e3) {
            riljLog("occur IllegalAccessException!");
            return z;
        } catch (InvocationTargetException e4) {
            riljLog("occur InvocationTargetException!");
            return z;
        } catch (Exception e5) {
            riljLog("occur Exception!");
            return z;
        }
    }

    public boolean closeSwitchOfUploadAntOrMaxTxPower(int mask) {
        riljLog("closeSwitchOfUploadAntOrMaxTxPower, mask = " + mask);
        Class hwnv = getHWNV();
        boolean result = false;
        if (hwnv == null) {
            return false;
        }
        try {
            return ((Boolean) hwnv.getMethod("unregisterModemGenericIndication", new Class[]{Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(mask)})).booleanValue();
        } catch (NoSuchMethodException e) {
            riljLog("occur NoSuchMethodException!");
            return result;
        } catch (RuntimeException e2) {
            riljLog("occur RuntimeException!");
            return result;
        } catch (IllegalAccessException e3) {
            riljLog("occur IllegalAccessException!");
            return result;
        } catch (InvocationTargetException e4) {
            riljLog("occur InvocationTargetException!");
            return result;
        } catch (Exception e5) {
            riljLog("occur Exception!");
            return result;
        }
    }

    protected Object handleUnsolicitedDefaultMessagePara(int response, Parcel p) {
        Object ret = super.handleUnsolicitedDefaultMessagePara(response, p);
        if (ret != null) {
            return ret;
        }
        switch (response) {
            case 3031:
                return null;
            default:
                return ret;
        }
    }

    public void handleUnsolicitedDefaultMessage(int response, Object ret, Context context) {
        super.handleUnsolicitedDefaultMessage(response, ret, context);
        switch (response) {
            case 136:
                return;
            default:
                return;
        }
    }

    private String unsolResponseToString(int request) {
        switch (request) {
            case 3032:
                return "UNSOL_HOOK_HW_VP_STATUS";
            default:
                return "<unknown response>=" + request;
        }
    }

    static String requestToString(int request) {
        Rlog.d(RILJ_LOG_TAG, "Enter HwQualcommRIL requestToString,");
        switch (request) {
            case 528:
                return "RIL_REQUEST_HW_QUERY_CARDTYPE";
            default:
                return "<unknown request>";
        }
    }

    private int responsecardType(Parcel p) {
        int cardmode = 0;
        int has_c = 0;
        int has_g = 0;
        IccCardStatus cardStatus = new IccCardStatus();
        cardStatus.setCardState(p.readInt());
        cardStatus.setUniversalPinState(p.readInt());
        cardStatus.mGsmUmtsSubscriptionAppIndex = p.readInt();
        cardStatus.mCdmaSubscriptionAppIndex = p.readInt();
        cardStatus.mImsSubscriptionAppIndex = p.readInt();
        int numApplications = p.readInt();
        if (numApplications > 8) {
            numApplications = 8;
        }
        cardStatus.mApplications = new IccCardApplicationStatus[numApplications];
        for (int i = 0; i < numApplications; i++) {
            IccCardApplicationStatus appStatus = new IccCardApplicationStatus();
            appStatus.app_type = appStatus.AppTypeFromRILInt(p.readInt());
            if (AppType.APPTYPE_RUIM == appStatus.app_type || AppType.APPTYPE_CSIM == appStatus.app_type) {
                has_c = 1;
            } else if (AppType.APPTYPE_USIM == appStatus.app_type || AppType.APPTYPE_SIM == appStatus.app_type) {
                has_g = 1;
            }
            if (cardmode == 0) {
                if (AppType.APPTYPE_CSIM == appStatus.app_type || AppType.APPTYPE_USIM == appStatus.app_type) {
                    cardmode = 2;
                } else if (AppType.APPTYPE_RUIM == appStatus.app_type || AppType.APPTYPE_SIM == appStatus.app_type) {
                    cardmode = 1;
                }
            }
        }
        return ((has_c << 1) | has_g) | (cardmode << 4);
    }

    public void queryCardType(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(528, result);
        Rlog.d(RILJ_LOG_TAG, rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void resetProfile(Message response) {
        Rlog.d(RILJ_LOG_TAG, "resetProfile");
        sendOemRilRequestRaw(524588, 0, null, response);
    }

    public void sendCloudMessageToModem(int event_id) {
        Rlog.d(RILJ_LOG_TAG, "sendCloudMessageToModem event :" + event_id);
        Class hwnv = getHWNV();
        if (hwnv != null) {
            try {
                hwnv.getMethod("sendCloudOtaCmd", new Class[]{Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(event_id)});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (RuntimeException e2) {
                e2.printStackTrace();
            } catch (IllegalAccessException e3) {
                e3.printStackTrace();
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
            }
        }
    }

    public void registerForModemCapEvent(Handler h, int what, Object obj) {
        this.mModemCapRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForModemCapEvent(Handler h) {
        this.mModemCapRegistrants.remove(h);
    }

    public void getModemCapability(Message response) {
        Rlog.d(RILJ_LOG_TAG, "GetModemCapability");
        sendOemRilRequestRaw(524323, 0, null, response);
    }

    public void updateStackBinding(int stack, int enable, Message response) {
        byte[] payload = new byte[]{(byte) stack, (byte) enable};
        Rlog.d(RILJ_LOG_TAG, "UpdateStackBinding: on Stack: " + stack + ", enable/disable: " + enable);
        sendOemRilRequestRaw(524324, 2, payload, response);
    }

    private void riljLog(String msg) {
        Rlog.d(RILJ_LOG_TAG, msg + (this.mRilInstanceId != null ? " [SUB" + this.mRilInstanceId + "]" : ""));
    }

    public void switchVoiceCallBackgroundState(int state, Message result) {
        byte[] payload = new byte[]{(byte) (state & 127)};
        Rlog.d(RILJ_LOG_TAG, "switchVoiceCallBackgroundState: state is " + state);
        sendOemRilRequestRaw(524301, 1, payload, null);
    }

    private void sendOemRilRequestRaw(int requestId, int numPayload, byte[] payload, Message response) {
        int length;
        int i = 0;
        byte[] request = new byte[(this.mHeaderSize + (numPayload * 1))];
        ByteBuffer buf = ByteBuffer.wrap(request);
        buf.order(ByteOrder.nativeOrder());
        for (char c : "QOEMHOOK".toCharArray()) {
            buf.put((byte) c);
        }
        buf.putInt(requestId);
        if (numPayload > 0 && payload != null) {
            buf.putInt(numPayload * 1);
            length = payload.length;
            while (i < length) {
                buf.put(payload[i]);
                i++;
            }
        }
        invokeOemRilRequestRaw(request, response);
    }

    public String getHwPrlVersion() {
        String prl = "0";
        Class hwnvCls = getHWNV();
        if (hwnvCls != null) {
            try {
                prl = (String) hwnvCls.getMethod("getCDMAPrlVersion", new Class[0]).invoke(null, new Object[0]);
            } catch (RuntimeException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            } catch (IllegalAccessException e3) {
                e3.printStackTrace();
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
            }
        }
        return prl == null ? "0" : prl;
    }

    public String getHwUimid() {
        String esn = getNVESN();
        return esn == null ? "0" : esn;
    }

    public void closeRrc() {
        closeRrc("");
    }

    public String getHwCDMAMsplVersion() {
        String MsplVersion = "0";
        Class hwnvCls = getHWNV();
        if (hwnvCls != null) {
            try {
                MsplVersion = (String) hwnvCls.getMethod("getCDMAMsplVersion", new Class[0]).invoke(null, new Object[0]);
            } catch (RuntimeException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            } catch (IllegalAccessException e3) {
                e3.printStackTrace();
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
            }
        }
        return MsplVersion == null ? "0" : MsplVersion;
    }

    public String getHwCDMAMlplVersion() {
        String MlplVersion = "0";
        Class hwnvCls = getHWNV();
        if (hwnvCls != null) {
            try {
                MlplVersion = (String) hwnvCls.getMethod("getCDMAMlplVersion", new Class[0]).invoke(null, new Object[0]);
            } catch (RuntimeException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            } catch (IllegalAccessException e3) {
                e3.printStackTrace();
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
            }
        }
        return MlplVersion == null ? "0" : MlplVersion;
    }

    public void closeRrc(String interfaceName) {
        if (1 == this.mPhoneType) {
            if (interfaceName == null) {
                Rlog.d(RILJ_LOG_TAG, "interfaceName is null");
                return;
            }
            Rlog.d(RILJ_LOG_TAG, "request interface " + interfaceName + " go dormant for GSMphone");
            try {
                sendOemRilRequestRaw(524291, interfaceName.length(), interfaceName.getBytes("UTF-8"), null);
            } catch (Exception e) {
                Rlog.d(RILJ_LOG_TAG, "Rilj, closeRrc");
            }
        } else if (2 == this.mPhoneType) {
            Rlog.d(RILJ_LOG_TAG, "request all interfaces go dormant for CDMAphone");
            Class hwnv = getHWNV();
            if (hwnv != null) {
                try {
                    hwnv.getMethod("closeRrc", new Class[0]).invoke(null, new Object[0]);
                } catch (NoSuchMethodException e2) {
                    e2.printStackTrace();
                } catch (RuntimeException e3) {
                    e3.printStackTrace();
                } catch (IllegalAccessException e4) {
                    e4.printStackTrace();
                } catch (InvocationTargetException e5) {
                    e5.printStackTrace();
                }
            }
        } else {
            Rlog.d(RILJ_LOG_TAG, "not CDMA or GSM phone, not support fast dormancy");
        }
    }

    public boolean setEhrpdByQMI(boolean enable) {
        Class hwnv = getHWNV();
        if (hwnv == null) {
            return false;
        }
        try {
            return ((Boolean) hwnv.getMethod("setEhrpdByQMI", new Class[]{Boolean.TYPE}).invoke(null, new Object[]{Boolean.valueOf(enable)})).booleanValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (RuntimeException e2) {
            e2.printStackTrace();
            return false;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return false;
        } catch (InvocationTargetException e4) {
            e4.printStackTrace();
            return false;
        }
    }

    public void setImsSwitch(boolean on) {
        try {
            ImsManager.getInstance(this.mContext, this.mRilInstanceId.intValue());
            ImsManager.setEnhanced4gLteModeSetting(this.mContext, on);
            System.putInt(this.mContext.getContentResolver(), "hw_volte_user_switch", on ? 1 : 0);
        } catch (NullPointerException e) {
            Rlog.e(RILJ_LOG_TAG, "e = " + e);
        } catch (Exception ex) {
            Rlog.e(RILJ_LOG_TAG, "ex = " + ex);
        }
    }

    public void getImsDomain(Message result) {
        riljLog("getImsDomain");
        AsyncResult.forMessage(result, null, null);
        result.sendToTarget();
    }

    public boolean getImsSwitch() {
        boolean z = true;
        try {
            if (System.getInt(this.mContext.getContentResolver(), "hw_volte_user_switch", 0) != 1) {
                z = false;
            }
            return z;
        } catch (NullPointerException e) {
            Rlog.e(RILJ_LOG_TAG, "e = " + e);
            return false;
        } catch (Exception ex) {
            Rlog.e(RILJ_LOG_TAG, "ex = " + ex);
            return false;
        }
    }

    public void setLTEReleaseVersion(boolean state, Message response) {
        Class hwnv = getHWNV();
        Integer ret = Integer.valueOf(0);
        byte[] value = new byte[1];
        Throwable tr = null;
        if (state) {
            value[0] = (byte) 0;
        } else {
            value[0] = (byte) 1;
        }
        if (hwnv != null) {
            try {
                ret = (Integer) hwnv.getMethod("writeEFSItemFile", new Class[]{String.class, byte[].class, Integer.TYPE}).invoke(null, new Object[]{ITEM_FILE_CA_DISABLE, value, Integer.valueOf(1)});
                Rlog.e(RILJ_LOG_TAG, "write ca_disable value=" + value[0] + " ret=" + ret);
            } catch (Throwable nsme) {
                nsme.printStackTrace();
                tr = nsme;
            } catch (Throwable re) {
                re.printStackTrace();
                tr = re;
            } catch (Throwable iae) {
                iae.printStackTrace();
                tr = iae;
            } catch (Throwable ite) {
                ite.printStackTrace();
                tr = ite;
            }
        } else {
            Rlog.e(RILJ_LOG_TAG, "getHWNV() return null !!");
        }
        if (response != null) {
            AsyncResult.forMessage(response, ret, tr);
            response.sendToTarget();
        }
    }

    public void getLteReleaseVersion(Message response) {
        Class hwnv = getHWNV();
        byte[] value = new byte[1];
        Throwable tr = null;
        Integer ret = Integer.valueOf(-1);
        int[] result = new int[]{-1};
        if (hwnv != null) {
            try {
                ret = (Integer) hwnv.getMethod("readEFSItemFile", new Class[]{String.class, byte[].class, Integer.TYPE}).invoke(null, new Object[]{ITEM_FILE_CA_DISABLE, value, Integer.valueOf(1)});
                Rlog.e(RILJ_LOG_TAG, "read ca_disable value=" + value[0] + " ret=" + ret);
            } catch (Throwable nsme) {
                nsme.printStackTrace();
                tr = nsme;
            } catch (Throwable re) {
                re.printStackTrace();
                tr = re;
            } catch (Throwable iae) {
                iae.printStackTrace();
                tr = iae;
            } catch (Throwable ite) {
                ite.printStackTrace();
                tr = ite;
            }
        } else {
            Rlog.e(RILJ_LOG_TAG, "getHWNV() return null !!");
        }
        if (response != null) {
            if (ret.intValue() == 1) {
                if (value[0] == (byte) 1) {
                    result[0] = 0;
                } else if (value[0] == (byte) 0) {
                    result[0] = 1;
                }
            } else if (ret.intValue() == -2) {
                result[0] = 1;
            }
            AsyncResult.forMessage(response, result, tr);
            response.sendToTarget();
        }
    }

    public void setPowerGrade(int powerGrade, Message response) {
        byte[] payload = new byte[]{(byte) (powerGrade & 127)};
        Rlog.d(RILJ_LOG_TAG, "setPowerGrade: state is " + powerGrade);
        sendOemRilRequestRaw(524489, 1, payload, response);
    }

    public void setWifiTxPowerGrade(int powerGrade, Message response) {
        byte[] payload = new byte[]{(byte) (powerGrade & 127)};
        Rlog.d(RILJ_LOG_TAG, "setWifiTXPowerGrade: state is " + powerGrade);
        sendOemRilRequestRaw(598042, 1, payload, response);
    }

    public void openSwitchOfUploadBandClass(Message result) {
        riljLog("Rilj, openSwitchOfUploadBandClass");
        sendOemRilRequestRaw(598041, 1, new byte[]{(byte) 1}, result);
    }

    public void closeSwitchOfUploadBandClass(Message result) {
        riljLog("Rilj, closeSwitchOfUploadBandClass");
        sendOemRilRequestRaw(598041, 1, new byte[]{(byte) 0}, result);
    }

    public void getAvailableCSGNetworks(byte[] data, Message response) {
        sendOemRilRequestRawBytes(OEMHOOK_EVT_HOOK_CSG_PERFORM_NW_SCAN, data, response);
    }

    public void setCSGNetworkSelectionModeManual(byte[] data, Message response) {
        sendOemRilRequestRawBytes(OEMHOOK_EVT_HOOK_CSG_SET_SYS_SEL_PREF, data, response);
    }

    private void sendOemRilRequestRawBytes(int requestId, byte[] payload, Message response) {
        byte[] request = new byte[(this.mHeaderSize + payload.length)];
        ByteBuffer buf = ByteBuffer.wrap(request);
        buf.order(ByteOrder.nativeOrder());
        for (char c : "QOEMHOOK".toCharArray()) {
            buf.put((byte) c);
        }
        buf.putInt(requestId);
        buf.putInt(payload.length);
        buf.put(payload);
        invokeOemRilRequestRaw(request, response);
    }

    public boolean cmdForECInfo(int event, int action, byte[] buf) {
        boolean z = true;
        Rlog.i(RILJ_LOG_TAG, "cmd for get/set nv, event is : " + event);
        switch (event) {
            case 2:
                if (action != 1) {
                    z = false;
                }
                return setEcTestControl(z);
            case 3:
                if (getNVInterface(action, buf) != (byte) 0) {
                    z = false;
                }
                return z;
            case 4:
                return setKmcPubKey(buf);
            case 5:
                return getKmcPubKey(buf);
            case 6:
                return getRandomInterface(buf);
            case 7:
                if (action == 0) {
                    if (getEcCdmaCallInfo() != 0) {
                        z = false;
                    }
                    return z;
                } else if (action == 1) {
                    return setEcCdmaCallInfo(action);
                }
                break;
            case 8:
                break;
            default:
                Rlog.w(RILJ_LOG_TAG, "error event, return false");
                return false;
        }
        if (action != 1) {
            z = false;
        }
        return setEcCdmaCallVersion(z);
    }

    public byte getNVInterface(int nvItem, byte[] buf) {
        Class hwnv = getHWNV();
        if (hwnv != null) {
            try {
                return ((Byte) hwnv.getMethod("getNVInterface", new Class[]{Integer.TYPE, byte[].class}).invoke(null, new Object[]{Integer.valueOf(nvItem), buf})).byteValue();
            } catch (NoSuchMethodException e) {
                Rlog.e(RILJ_LOG_TAG, "getNVInterface NoSuchMethodException " + e);
            } catch (RuntimeException e2) {
                Rlog.e(RILJ_LOG_TAG, "getNVInterface RuntimeException " + e2);
            } catch (IllegalAccessException e3) {
                Rlog.e(RILJ_LOG_TAG, "getNVInterface IllegalAccessException " + e3);
            } catch (InvocationTargetException e4) {
                Rlog.e(RILJ_LOG_TAG, "getNVInterface InvocationTargetException " + e4);
            }
        }
        return ERROR;
    }

    public boolean setEcTestControl(boolean Control_flag) {
        Class hwnv = getHWNV();
        if (hwnv != null) {
            try {
                return ((Boolean) hwnv.getMethod("setEcTestControl", new Class[]{Boolean.TYPE}).invoke(null, new Object[]{Boolean.valueOf(Control_flag)})).booleanValue();
            } catch (NoSuchMethodException e) {
                Rlog.e(RILJ_LOG_TAG, "setEcTestControl NoSuchMethodException " + e);
            } catch (RuntimeException e2) {
                Rlog.e(RILJ_LOG_TAG, "setEcTestControl RuntimeException " + e2);
            } catch (IllegalAccessException e3) {
                Rlog.e(RILJ_LOG_TAG, "setEcTestControl IllegalAccessException " + e3);
            } catch (InvocationTargetException e4) {
                Rlog.e(RILJ_LOG_TAG, "setEcTestControl InvocationTargetException " + e4);
            }
        }
        return false;
    }

    public boolean setKmcPubKey(byte[] buf) {
        Class hwnv = getHWNV();
        if (hwnv != null) {
            try {
                return ((Boolean) hwnv.getMethod("setKmcPubKey", new Class[]{byte[].class}).invoke(null, new Object[]{buf})).booleanValue();
            } catch (NoSuchMethodException e) {
                Rlog.e(RILJ_LOG_TAG, "setKmcPubKey NoSuchMethodException " + e);
            } catch (RuntimeException e2) {
                Rlog.e(RILJ_LOG_TAG, "setKmcPubKey RuntimeException " + e2);
            } catch (IllegalAccessException e3) {
                Rlog.e(RILJ_LOG_TAG, "setKmcPubKey IllegalAccessException " + e3);
            } catch (InvocationTargetException e4) {
                Rlog.e(RILJ_LOG_TAG, "setKmcPubKey InvocationTargetException " + e4);
            }
        }
        return false;
    }

    public boolean getKmcPubKey(byte[] buf) {
        Class hwnv = getHWNV();
        if (hwnv != null) {
            try {
                return ((Boolean) hwnv.getMethod("getKmcPubKey", new Class[]{byte[].class}).invoke(null, new Object[]{buf})).booleanValue();
            } catch (NoSuchMethodException e) {
                Rlog.e(RILJ_LOG_TAG, "getKmcPubKey NoSuchMethodException " + e);
            } catch (RuntimeException e2) {
                Rlog.e(RILJ_LOG_TAG, "getKmcPubKey RuntimeException " + e2);
            } catch (IllegalAccessException e3) {
                Rlog.e(RILJ_LOG_TAG, "getKmcPubKey IllegalAccessException " + e3);
            } catch (InvocationTargetException e4) {
                Rlog.e(RILJ_LOG_TAG, "getKmcPubKey InvocationTargetException " + e4);
            }
        }
        return false;
    }

    public boolean getRandomInterface(byte[] buf) {
        Class hwnv = getHWNV();
        if (hwnv != null) {
            try {
                return ((Boolean) hwnv.getMethod("getRandomInterface", new Class[]{byte[].class}).invoke(null, new Object[]{buf})).booleanValue();
            } catch (NoSuchMethodException e) {
                Rlog.e(RILJ_LOG_TAG, "getRandomInterface NoSuchMethodException " + e);
            } catch (RuntimeException e2) {
                Rlog.e(RILJ_LOG_TAG, "getRandomInterface RuntimeException " + e2);
            } catch (IllegalAccessException e3) {
                Rlog.e(RILJ_LOG_TAG, "getRandomInterface IllegalAccessException " + e3);
            } catch (InvocationTargetException e4) {
                Rlog.e(RILJ_LOG_TAG, "getRandomInterface InvocationTargetException " + e4);
            }
        }
        return false;
    }

    public boolean getEcCdmaCallVersion() {
        Class hwnv = getHWNV();
        if (hwnv != null) {
            try {
                return ((Boolean) hwnv.getMethod("getEcCdmaCallVersion", new Class[0]).invoke(null, new Object[0])).booleanValue();
            } catch (NoSuchMethodException e) {
                Rlog.e(RILJ_LOG_TAG, "getEcCdmaCallVersion NoSuchMethodException " + e);
            } catch (RuntimeException e2) {
                Rlog.e(RILJ_LOG_TAG, "getEcCdmaCallVersion RuntimeException " + e2);
            } catch (IllegalAccessException e3) {
                Rlog.e(RILJ_LOG_TAG, "getEcCdmaCallVersion IllegalAccessException " + e3);
            } catch (InvocationTargetException e4) {
                Rlog.e(RILJ_LOG_TAG, "getEcCdmaCallVersion InvocationTargetException " + e4);
            }
        }
        return false;
    }

    public boolean setEcCdmaCallVersion(boolean version) {
        Class hwnv = getHWNV();
        if (hwnv != null) {
            try {
                return ((Boolean) hwnv.getMethod("setEcCdmaCallVersion", new Class[]{Boolean.TYPE}).invoke(null, new Object[]{Boolean.valueOf(version)})).booleanValue();
            } catch (NoSuchMethodException e) {
                Rlog.e(RILJ_LOG_TAG, "setEcCdmaCallVersion NoSuchMethodException " + e);
            } catch (RuntimeException e2) {
                Rlog.e(RILJ_LOG_TAG, "setEcCdmaCallVersion RuntimeException " + e2);
            } catch (IllegalAccessException e3) {
                Rlog.e(RILJ_LOG_TAG, "setEcCdmaCallVersion IllegalAccessException " + e3);
            } catch (InvocationTargetException e4) {
                Rlog.e(RILJ_LOG_TAG, "setEcCdmaCallVersion InvocationTargetException " + e4);
            }
        }
        return false;
    }

    public int getEcCdmaCallInfo() {
        Class hwnv = getHWNV();
        if (hwnv != null) {
            try {
                return ((Integer) hwnv.getMethod("getEcCdmaCallInfo", new Class[0]).invoke(null, new Object[0])).intValue();
            } catch (NoSuchMethodException e) {
                Rlog.e(RILJ_LOG_TAG, "getEcCdmaCallInfo NoSuchMethodException " + e);
            } catch (RuntimeException e2) {
                Rlog.e(RILJ_LOG_TAG, "getEcCdmaCallInfo RuntimeException " + e2);
            } catch (IllegalAccessException e3) {
                Rlog.e(RILJ_LOG_TAG, "getEcCdmaCallInfo IllegalAccessException " + e3);
            } catch (InvocationTargetException e4) {
                Rlog.e(RILJ_LOG_TAG, "getEcCdmaCallInfo InvocationTargetException " + e4);
            }
        }
        return 0;
    }

    public boolean setEcCdmaCallInfo(int info) {
        Class hwnv = getHWNV();
        if (hwnv != null) {
            try {
                return ((Boolean) hwnv.getMethod("setEcCdmaCallInfo", new Class[]{Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(info)})).booleanValue();
            } catch (NoSuchMethodException e) {
                Rlog.e(RILJ_LOG_TAG, "setEcCdmaCallInfo NoSuchMethodException " + e);
            } catch (RuntimeException e2) {
                Rlog.e(RILJ_LOG_TAG, "setEcCdmaCallInfo RuntimeException " + e2);
            } catch (IllegalAccessException e3) {
                Rlog.e(RILJ_LOG_TAG, "setEcCdmaCallInfo IllegalAccessException " + e3);
            } catch (InvocationTargetException e4) {
                Rlog.e(RILJ_LOG_TAG, "setEcCdmaCallInfo InvocationTargetException " + e4);
            }
        }
        return false;
    }
}
