package com.android.internal.telephony;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.cdma.HwCdmaServiceStateManager;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.gsm.HwGsmServiceStateManager;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccCardApplicationUtils;
import com.android.internal.telephony.vsim.HwVSimConstants;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HwServiceStateManager extends Handler {
    protected static final int CT_NUM_MATCH_HOME = 11;
    protected static final int CT_NUM_MATCH_ROAMING = 10;
    protected static final int CT_SID_1st_END = 14335;
    protected static final int CT_SID_1st_START = 13568;
    protected static final int CT_SID_2nd_END = 26111;
    protected static final int CT_SID_2nd_START = 25600;
    protected static final int DEFAULT_SID = 0;
    protected static final int DELAYED_TIME_DEFAULT_VALUE = SystemProperties.getInt("ro.lostnetwork.default_timer", 20);
    protected static final int DELAYED_TIME_NETWORKSTATUS_CS_2G = (SystemProperties.getInt("ro.lostnetwork.delaytimer_cs2G", DELAYED_TIME_DEFAULT_VALUE) * HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE);
    protected static final int DELAYED_TIME_NETWORKSTATUS_CS_3G = (SystemProperties.getInt("ro.lostnetwork.delaytimer_cs3G", DELAYED_TIME_DEFAULT_VALUE) * HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE);
    protected static final int DELAYED_TIME_NETWORKSTATUS_CS_4G = (SystemProperties.getInt("ro.lostnetwork.delaytimer_cs4G", DELAYED_TIME_DEFAULT_VALUE) * HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE);
    protected static final int DELAYED_TIME_NETWORKSTATUS_PS_2G = (SystemProperties.getInt("ro.lostnetwork.delaytimer_ps2G", DELAYED_TIME_DEFAULT_VALUE) * HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE);
    protected static final int DELAYED_TIME_NETWORKSTATUS_PS_3G = (SystemProperties.getInt("ro.lostnetwork.delaytimer_ps3G", DELAYED_TIME_DEFAULT_VALUE) * HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE);
    protected static final int DELAYED_TIME_NETWORKSTATUS_PS_4G = (SystemProperties.getInt("ro.lostnetwork.delaytimer_ps4G", DELAYED_TIME_DEFAULT_VALUE) * HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE);
    protected static final int EVENT_DELAY_UPDATE_REGISTER_STATE_DONE = 0;
    protected static final int EVENT_ICC_RECORDS_EONS_UPDATED = 1;
    protected static final int EVENT_RESUME_DATA = 203;
    protected static final int EVENT_SET_PRE_NETWORKTYPE = 202;
    protected static final String INVAILD_PLMN = "1023127-123456-1023456-123127-";
    protected static final boolean IS_CHINATELECOM = SystemProperties.get("ro.config.hw_opta", "0").equals("92");
    protected static final boolean IS_MULTI_SIM_ENABLED = TelephonyManager.getDefault().isMultiSimEnabled();
    protected static final int RESUME_DATA_TIME = 8000;
    protected static final int SET_PRE_NETWORK_TIME = 5000;
    protected static final int SET_PRE_NETWORK_TIME_DELAY = 2000;
    protected static final int SPN_RULE_SHOW_BOTH = 3;
    protected static final int SPN_RULE_SHOW_PLMN_ONLY = 2;
    protected static final int SPN_RULE_SHOW_PNN_PRIOR = 4;
    protected static final int SPN_RULE_SHOW_SPN_ONLY = 1;
    private static final String TAG = "HwServiceStateManager";
    private static Map<Object, HwCdmaServiceStateManager> cdmaServiceStateManagers = new HashMap();
    private static Map<Object, HwGsmServiceStateManager> gsmServiceStateManagers = new HashMap();
    private static final boolean isScreenOffNotUpdateLocation = SystemProperties.getBoolean("ro.config.updatelocation", false);
    private static Map<Object, HwServiceStateManager> serviceStateManagers = new HashMap();
    protected static final UiccCardApplicationUtils uiccCardApplicationUtils = new UiccCardApplicationUtils();
    private static final boolean voice_reg_state_for_ons = "true".equals(SystemProperties.get("ro.hwpp.voice_reg_state_for_ons", "false"));
    protected int mMainSlot;
    protected int mPendingPreNwType = 0;
    protected Message mPendingsavemessage;
    private Phone mPhoneBase;
    protected boolean mRefreshState = false;
    private ServiceStateTracker mServiceStateTracker;
    protected boolean mSetPreNwTypeRequested = false;

    protected HwServiceStateManager(Phone phoneBase) {
        super(Looper.getMainLooper());
    }

    public String getPlmn() {
        return "";
    }

    public void sendDualSimUpdateSpnIntent(boolean showSpn, String spn, boolean showPlmn, String plmn) {
    }

    public OnsDisplayParams getOnsDisplayParamsHw(boolean showSpn, boolean showPlmn, int rule, String plmn, String spn) {
        return new OnsDisplayParams(showSpn, showPlmn, rule, plmn, spn);
    }

    public HwServiceStateManager(ServiceStateTracker serviceStateTracker, Phone phoneBase) {
        super(Looper.getMainLooper());
        this.mServiceStateTracker = serviceStateTracker;
        this.mPhoneBase = phoneBase;
    }

    public static synchronized HwServiceStateManager getHwServiceStateManager(ServiceStateTracker serviceStateTracker, Phone phoneBase) {
        HwServiceStateManager hwServiceStateManager;
        synchronized (HwServiceStateManager.class) {
            hwServiceStateManager = (HwServiceStateManager) serviceStateManagers.get(serviceStateTracker);
            if (hwServiceStateManager == null) {
                hwServiceStateManager = new HwServiceStateManager(serviceStateTracker, phoneBase);
                serviceStateManagers.put(serviceStateTracker, hwServiceStateManager);
            }
        }
        return hwServiceStateManager;
    }

    public static synchronized HwGsmServiceStateManager getHwGsmServiceStateManager(ServiceStateTracker serviceStateTracker, GsmCdmaPhone phone) {
        HwGsmServiceStateManager hwGsmServiceStateManager;
        synchronized (HwServiceStateManager.class) {
            hwGsmServiceStateManager = (HwGsmServiceStateManager) gsmServiceStateManagers.get(serviceStateTracker);
            if (hwGsmServiceStateManager == null) {
                hwGsmServiceStateManager = new HwGsmServiceStateManager(serviceStateTracker, phone);
                gsmServiceStateManagers.put(serviceStateTracker, hwGsmServiceStateManager);
            }
        }
        return hwGsmServiceStateManager;
    }

    public static synchronized HwCdmaServiceStateManager getHwCdmaServiceStateManager(ServiceStateTracker serviceStateTracker, GsmCdmaPhone phone) {
        HwCdmaServiceStateManager hwCdmaServiceStateManager;
        synchronized (HwServiceStateManager.class) {
            hwCdmaServiceStateManager = (HwCdmaServiceStateManager) cdmaServiceStateManagers.get(serviceStateTracker);
            if (hwCdmaServiceStateManager == null) {
                hwCdmaServiceStateManager = new HwCdmaServiceStateManager(serviceStateTracker, phone);
                cdmaServiceStateManagers.put(serviceStateTracker, hwCdmaServiceStateManager);
            }
        }
        return hwCdmaServiceStateManager;
    }

    public static synchronized void dispose(ServiceStateTracker serviceStateTracker) {
        synchronized (HwServiceStateManager.class) {
            if (serviceStateTracker == null) {
                return;
            }
            HwGsmServiceStateManager hwGsmServiceStateManager = (HwGsmServiceStateManager) gsmServiceStateManagers.get(serviceStateTracker);
            if (hwGsmServiceStateManager != null) {
                hwGsmServiceStateManager.dispose();
            }
            gsmServiceStateManagers.put(serviceStateTracker, null);
            HwCdmaServiceStateManager hwCdmaServiceStateManager = (HwCdmaServiceStateManager) cdmaServiceStateManagers.get(serviceStateTracker);
            if (hwCdmaServiceStateManager != null) {
                hwCdmaServiceStateManager.dispose();
            }
            cdmaServiceStateManagers.put(serviceStateTracker, null);
        }
    }

    public int getCombinedRegState(ServiceState serviceState) {
        int regState = serviceState.getVoiceRegState();
        int dataRegState = serviceState.getDataRegState();
        if (voice_reg_state_for_ons) {
            return regState;
        }
        if (regState == 1 && dataRegState == 0) {
            Rlog.d(TAG, "getCombinedRegState: return STATE_IN_SERVICE as Data is in service");
            regState = dataRegState;
        }
        return regState;
    }

    public void processCTNumMatch(boolean roaming, UiccCardApplication uiccCardApplication) {
    }

    protected void checkMultiSimNumMatch() {
        int[] matchArray = new int[]{SystemProperties.getInt("gsm.hw.matchnum0", -1), SystemProperties.getInt("gsm.hw.matchnum.short0", -1), SystemProperties.getInt("gsm.hw.matchnum1", -1), SystemProperties.getInt("gsm.hw.matchnum.short1", -1)};
        Arrays.sort(matchArray);
        int numMatch = matchArray[3];
        int numMatchShort = numMatch;
        int i = 2;
        while (i >= 0) {
            if (matchArray[i] < numMatch && matchArray[i] > 0) {
                numMatchShort = matchArray[i];
            }
            i--;
        }
        SystemProperties.set("gsm.hw.matchnum", Integer.toString(numMatch));
        SystemProperties.set("gsm.hw.matchnum.short", Integer.toString(numMatchShort));
        Rlog.d(TAG, "checkMultiSimNumMatch: after setprop numMatch = " + SystemProperties.getInt("gsm.hw.matchnum", 0) + ", numMatchShort = " + SystemProperties.getInt("gsm.hw.matchnum.short", 0));
    }

    protected void setCTNumMatchHomeForSlot(int slotId) {
        if (IS_MULTI_SIM_ENABLED) {
            SystemProperties.set("gsm.hw.matchnum" + slotId, Integer.toString(11));
            SystemProperties.set("gsm.hw.matchnum.short" + slotId, Integer.toString(11));
            checkMultiSimNumMatch();
            return;
        }
        SystemProperties.set("gsm.hw.matchnum", Integer.toString(11));
        SystemProperties.set("gsm.hw.matchnum.short", Integer.toString(11));
    }

    protected void setCTNumMatchRoamingForSlot(int slotId) {
        if (IS_MULTI_SIM_ENABLED) {
            SystemProperties.set("gsm.hw.matchnum" + slotId, Integer.toString(10));
            SystemProperties.set("gsm.hw.matchnum.short" + slotId, Integer.toString(10));
            checkMultiSimNumMatch();
            return;
        }
        SystemProperties.set("gsm.hw.matchnum", Integer.toString(10));
        SystemProperties.set("gsm.hw.matchnum.short", Integer.toString(10));
    }

    public static boolean isCustScreenOff(GsmCdmaPhone phoneBase) {
        if (!(!isScreenOffNotUpdateLocation || phoneBase == null || phoneBase.getContext() == null)) {
            PowerManager powerManager = (PowerManager) phoneBase.getContext().getSystemService("power");
            if (!(powerManager == null || powerManager.isScreenOn())) {
                Rlog.d(TAG, " ScreenOff do nothing");
                return true;
            }
        }
        return false;
    }

    public void setOOSFlag(boolean flag) {
    }

    private void setPreferredNetworkType(int networkType, int phoneId, Message response) {
        if (!HwModemCapability.isCapabilitySupport(9) || TelephonyManager.getDefault().getPhoneCount() <= 1) {
            this.mPhoneBase.mCi.setPreferredNetworkType(networkType, response);
            return;
        }
        Rlog.d(TAG, "PhoneCount > 1");
        HwModemBindingPolicyHandler.getInstance().setPreferredNetworkType(networkType, phoneId, response);
    }

    public void setPreferredNetworkTypeSafely(Phone phoneBase, int networkType, Message response) {
        this.mPhoneBase = phoneBase;
        DcTracker dcTracker = this.mPhoneBase.mDcTracker;
        if (this.mServiceStateTracker == null) {
            Rlog.d(TAG, "mServiceStateTracker is null, it is unexpected!");
        }
        if (networkType != 10) {
            if (this.mSetPreNwTypeRequested) {
                removeMessages(EVENT_SET_PRE_NETWORKTYPE);
                Rlog.d(TAG, "cancel setPreferredNetworkType");
            }
            this.mSetPreNwTypeRequested = false;
            Rlog.d(TAG, "PreNetworkType is not LTE, setPreferredNetworkType now!");
            setPreferredNetworkType(networkType, this.mPhoneBase.getPhoneId(), response);
        } else if (!this.mSetPreNwTypeRequested) {
            if (dcTracker.isDisconnected()) {
                setPreferredNetworkType(networkType, this.mPhoneBase.getPhoneId(), response);
                Rlog.d(TAG, "data is Disconnected, setPreferredNetworkType now!");
                return;
            }
            dcTracker.setInternalDataEnabled(false);
            Rlog.d(TAG, "Data is disabled and wait up to 8s to resume data.");
            sendMessageDelayed(obtainMessage(EVENT_RESUME_DATA), 8000);
            this.mPendingsavemessage = response;
            this.mPendingPreNwType = networkType;
            Message msg = Message.obtain(this);
            msg.what = EVENT_SET_PRE_NETWORKTYPE;
            msg.arg1 = networkType;
            msg.obj = response;
            Rlog.d(TAG, "Wait up to 5s for data disconnect to setPreferredNetworkType.");
            sendMessageDelayed(msg, 5000);
            this.mSetPreNwTypeRequested = true;
        }
    }

    public void checkAndSetNetworkType() {
        if (this.mSetPreNwTypeRequested) {
            Rlog.d(TAG, "mSetPreNwTypeRequested is true and wait a few seconds to setPreferredNetworkType");
            removeMessages(EVENT_SET_PRE_NETWORKTYPE);
            Message msg = Message.obtain(this);
            msg.what = EVENT_SET_PRE_NETWORKTYPE;
            msg.arg1 = this.mPendingPreNwType;
            msg.obj = this.mPendingsavemessage;
            sendMessageDelayed(msg, HwVSimConstants.GET_MODEM_SUPPORT_VERSION_INTERVAL);
            return;
        }
        Rlog.d(TAG, "No need to setPreferredNetworkType");
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case EVENT_SET_PRE_NETWORKTYPE /*202*/:
                if (this.mSetPreNwTypeRequested) {
                    Rlog.d(TAG, "EVENT_SET_PRE_NETWORKTYPE, setPreferredNetworkType now.");
                    setPreferredNetworkType(msg.arg1, this.mPhoneBase.getPhoneId(), (Message) msg.obj);
                    this.mSetPreNwTypeRequested = false;
                    return;
                }
                Rlog.d(TAG, "No need to setPreferredNetworkType");
                return;
            case EVENT_RESUME_DATA /*203*/:
                this.mPhoneBase.mDcTracker.setInternalDataEnabled(true);
                Rlog.d(TAG, "EVENT_RESUME_DATA, resume data now.");
                return;
            default:
                Rlog.d(TAG, "Unhandled message with number: " + msg.what);
                return;
        }
    }
}
