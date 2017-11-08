package com.android.internal.telephony;

import android.content.ContentResolver;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;

public class HwNetworkManagerImpl implements HwNetworkManager {
    private static final boolean DBG = true;
    private static final int DEFAULT_DELAY_POWER_OFF_MISEC = 2000;
    private static final String HWNFF_RSSI_SIM1 = "gsm.rssi.sim1";
    private static final String HWNFF_RSSI_SIM2 = "gsm.rssi.sim2";
    private static final int MAX_DELAY_POWER_OFF_MISEC = 6000;
    private static final String TAG = "HwNetworkManagerImpl";
    private static HwNetworkManager mInstance = new HwNetworkManagerImpl();
    private int hwnff_value_sim1 = 0;
    private int hwnff_value_sim2 = 0;

    private static class SaveThread extends Thread {
        ContentResolver mCr;
        String mMcc;
        String mTimeZoneId;

        public SaveThread(ContentResolver cr, String mcc, String timeZoneId) {
            this.mCr = cr;
            this.mMcc = mcc;
            this.mTimeZoneId = timeZoneId;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            super.run();
            synchronized (SaveThread.class) {
                if (!(this.mCr == null || this.mMcc == null)) {
                    if (this.mTimeZoneId != null) {
                        try {
                            System.putString(this.mCr, "nitz_timezone_info", this.mTimeZoneId + "||" + this.mMcc + "||" + System.currentTimeMillis());
                            String timezoneId = System.getString(this.mCr, "keyguard_default_time_zone");
                            if (timezoneId == null || timezoneId.length() == 0) {
                                System.putString(this.mCr, "keyguard_default_time_zone", this.mTimeZoneId);
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }
    }

    public static HwNetworkManager getDefault() {
        return mInstance;
    }

    public String getCdmaPlmn(ServiceStateTracker stateTracker, GsmCdmaPhone phone) {
        return HwServiceStateManager.getHwCdmaServiceStateManager(stateTracker, phone).getPlmn();
    }

    public String getCdmaRplmn(ServiceStateTracker stateTracker, GsmCdmaPhone phone) {
        return HwServiceStateManager.getHwCdmaServiceStateManager(stateTracker, phone).getRplmn();
    }

    public String getGsmPlmn(Object stateTracker, GsmCdmaPhone phone) {
        return HwServiceStateManager.getHwGsmServiceStateManager((ServiceStateTracker) stateTracker, phone).getPlmn();
    }

    public String getGsmRplmn(Object stateTracker, GsmCdmaPhone phone) {
        return HwServiceStateManager.getHwGsmServiceStateManager((ServiceStateTracker) stateTracker, phone).getRplmn();
    }

    public boolean getGsmRoamingState(Object stateTracker, GsmCdmaPhone phone, boolean roaming) {
        return HwServiceStateManager.getHwGsmServiceStateManager((ServiceStateTracker) stateTracker, phone).getRoamingStateHw(roaming);
    }

    public OnsDisplayParams getGsmOnsDisplayParams(Object stateTracker, GsmCdmaPhone phone, boolean showSpn, boolean showPlmn, int rule, String plmn, String spn) {
        return HwServiceStateManager.getHwGsmServiceStateManager((ServiceStateTracker) stateTracker, phone).getOnsDisplayParamsHw(showSpn, showPlmn, rule, plmn, spn);
    }

    public void sendGsmDualSimUpdateSpnIntent(Object stateTracker, GsmCdmaPhone phone, boolean showSpn, String spn, boolean showPlmn, String plmn) {
        HwServiceStateManager.getHwGsmServiceStateManager((ServiceStateTracker) stateTracker, phone).sendDualSimUpdateSpnIntent(showSpn, spn, showPlmn, plmn);
    }

    public void sendCdmaDualSimUpdateSpnIntent(ServiceStateTracker stateTracker, GsmCdmaPhone phone, boolean showSpn, String spn, boolean showPlmn, String plmn) {
        HwServiceStateManager.getHwCdmaServiceStateManager(stateTracker, phone).sendDualSimUpdateSpnIntent(showSpn, spn, showPlmn, plmn);
    }

    public void delaySendDetachAfterDataOff() {
    }

    public void delaySendDetachAfterDataOff(GsmCdmaPhone phone) {
        String sim_plmn;
        int delay_milsec = SystemProperties.getInt("ro.config.hw_delay_detach_time", 0) * HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE;
        String delay_plmn = "";
        delay_plmn = Systemex.getString(phone.getContext().getContentResolver(), "hw_delay_imsi_detach_plmn");
        if (TextUtils.isEmpty(delay_plmn)) {
            delay_plmn = SystemProperties.get("ro.config.hw_delay_detach_plmn", "");
        }
        Rlog.d(TAG, " delay_plmn = " + delay_plmn);
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            sim_plmn = TelephonyManager.getDefault().getSimOperator(SubscriptionManager.getDefaultDataSubscriptionId());
            Rlog.d(TAG, "isMultiSimEnabled + sim_plmn = " + sim_plmn);
        } else {
            sim_plmn = SystemProperties.get("gsm.sim.operator.numeric", "");
            Rlog.d(TAG, " not isMultiSimEnabled + sim_plmn = " + sim_plmn);
        }
        if ("".equals(delay_plmn) || "".equals(sim_plmn)) {
            Rlog.d(TAG, "Process pending request to turn radio off; delay_plmn:" + delay_plmn + " sim_plmn:" + sim_plmn);
        } else if (-1 == delay_plmn.indexOf(sim_plmn) || delay_milsec <= 0) {
            Rlog.d(TAG, "Process pending request to turn radio off");
        } else {
            if (delay_milsec >= MAX_DELAY_POWER_OFF_MISEC) {
                delay_milsec = DEFAULT_DELAY_POWER_OFF_MISEC;
            }
            Rlog.d(TAG, "Process pending request to turn radio off " + delay_milsec + " milliseconds later.");
            SystemClock.sleep((long) delay_milsec);
            Rlog.d(TAG, delay_milsec + " milliseconds past, Process pending request to turn radio off");
        }
    }

    public void setAutoTimeAndZoneForCdma(ServiceStateTracker stateTracker, GsmCdmaPhone phone, int rt) {
        HwServiceStateManager.getHwCdmaServiceStateManager(stateTracker, phone).setAutoTimeAndZoneForCdma(rt);
    }

    public void saveNitzTimeZoneToDB(ContentResolver cr, String timeZoneId) {
        String mccMnc = SystemProperties.get("gsm.operator.numeric");
        if (mccMnc != null && mccMnc.length() > 3) {
            new SaveThread(cr, mccMnc.substring(0, 3), timeZoneId).start();
        }
    }

    public void dispose(ServiceStateTracker serviceStateTracker) {
        HwServiceStateManager.dispose(serviceStateTracker);
    }

    public int getGsmCombinedRegState(Object stateTracker, GsmCdmaPhone phone, ServiceState serviceState) {
        return HwServiceStateManager.getHwGsmServiceStateManager((ServiceStateTracker) stateTracker, phone).getCombinedRegState(serviceState);
    }

    public int getCdmaCombinedRegState(Object stateTracker, GsmCdmaPhone phone, ServiceState serviceState) {
        return HwServiceStateManager.getHwCdmaServiceStateManager((ServiceStateTracker) stateTracker, phone).getCombinedRegState(serviceState);
    }

    public boolean needGsmUpdateNITZTime(Object stateTracker, GsmCdmaPhone phone) {
        return HwServiceStateManager.getHwGsmServiceStateManager((ServiceStateTracker) stateTracker, phone).needUpdateNITZTime();
    }

    public void processCdmaCTNumMatch(Object stateTracker, GsmCdmaPhone phone, boolean roaming, UiccCardApplication uiccCardApplication) {
        HwServiceStateManager.getHwCdmaServiceStateManager((ServiceStateTracker) stateTracker, phone).processCTNumMatch(roaming, uiccCardApplication);
    }

    public void processGsmCTNumMatch(Object stateTracker, GsmCdmaPhone phone, boolean roaming, UiccCardApplication uiccCardApplication) {
        HwServiceStateManager.getHwGsmServiceStateManager((ServiceStateTracker) stateTracker, phone).processCTNumMatch(roaming, uiccCardApplication);
    }

    public boolean updateCTRoaming(Object stateTracker, GsmCdmaPhone phone, ServiceState newSS, boolean cdmaRoaming) {
        return HwServiceStateManager.getHwCdmaServiceStateManager((ServiceStateTracker) stateTracker, phone).updateCTRoaming(newSS, cdmaRoaming);
    }

    public boolean notifyGsmSignalStrength(Object stateTracker, GsmCdmaPhone phone, SignalStrength oldSS, SignalStrength newSS) {
        return HwServiceStateManager.getHwGsmServiceStateManager((ServiceStateTracker) stateTracker, phone).notifySignalStrength(oldSS, newSS);
    }

    public boolean notifyCdmaSignalStrength(Object stateTracker, GsmCdmaPhone phone, SignalStrength oldSS, SignalStrength newSS) {
        return HwServiceStateManager.getHwCdmaServiceStateManager((ServiceStateTracker) stateTracker, phone).notifySignalStrength(oldSS, newSS);
    }

    public int getCARilRadioType(Object stateTracker, GsmCdmaPhone phone, int type) {
        return HwServiceStateManager.getHwGsmServiceStateManager((ServiceStateTracker) stateTracker, phone).getCARilRadioType(type);
    }

    public int updateCAStatus(Object stateTracker, GsmCdmaPhone phone, int currentType) {
        return HwServiceStateManager.getHwGsmServiceStateManager((ServiceStateTracker) stateTracker, phone).updateCAStatus(currentType);
    }

    public void unregisterForSimRecordsEvents(Object stateTracker, GsmCdmaPhone phone, IccRecords r) {
        HwServiceStateManager.getHwGsmServiceStateManager((ServiceStateTracker) stateTracker, phone).unregisterForRecordsEvents(r);
    }

    public void registerForSimRecordsEvents(Object stateTracker, GsmCdmaPhone phone, IccRecords r) {
        HwServiceStateManager.getHwGsmServiceStateManager((ServiceStateTracker) stateTracker, phone).registerForRecordsEvents(r);
    }

    public boolean isCustScreenOff(GsmCdmaPhone phoneBase) {
        return HwServiceStateManager.isCustScreenOff(phoneBase);
    }

    public boolean proccessGsmDelayUpdateRegisterStateDone(Object stateTracker, GsmCdmaPhone phone, ServiceState oldSS, ServiceState newSS) {
        return HwServiceStateManager.getHwGsmServiceStateManager((ServiceStateTracker) stateTracker, phone).proccessGsmDelayUpdateRegisterStateDone(oldSS, newSS);
    }

    public boolean proccessCdmaLteDelayUpdateRegisterStateDone(Object stateTracker, GsmCdmaPhone phone, ServiceState oldSS, ServiceState newSS) {
        return HwServiceStateManager.getHwCdmaServiceStateManager((ServiceStateTracker) stateTracker, phone).proccessCdmaLteDelayUpdateRegisterStateDone(oldSS, newSS);
    }

    public void setGsmOOSFlag(Object stateTracker, GsmCdmaPhone phone, boolean flag) {
        HwServiceStateManager.getHwGsmServiceStateManager((ServiceStateTracker) stateTracker, phone).setOOSFlag(flag);
    }

    public void setCdmaOOSFlag(Object stateTracker, GsmCdmaPhone phone, boolean flag) {
        HwServiceStateManager.getHwCdmaServiceStateManager((ServiceStateTracker) stateTracker, phone).setOOSFlag(flag);
    }

    public boolean isUpdateLacAndCid(Object stateTracker, GsmCdmaPhone phone, int cid) {
        return HwServiceStateManager.getHwGsmServiceStateManager((ServiceStateTracker) stateTracker, phone).isUpdateLacAndCid(cid);
    }

    public void sendGsmRoamingIntentIfDenied(Object stateTracker, GsmCdmaPhone phone, int regState, String[] states) {
        HwServiceStateManager.getHwGsmServiceStateManager((ServiceStateTracker) stateTracker, phone).sendGsmRoamingIntentIfDenied(regState, states);
    }

    public void updateHwnff(ServiceStateTracker stateTracker, SignalStrength newSS) {
        int subId = stateTracker.getPhone().getSubId();
        int simCardState = TelephonyManager.getDefault().getSimState(subId);
        if (simCardState != 1 && simCardState != 0) {
            if (subId == 0) {
                try {
                    if (this.hwnff_value_sim1 != newSS.getDbm()) {
                        this.hwnff_value_sim1 = newSS.getDbm();
                        SystemProperties.set(HWNFF_RSSI_SIM1, Integer.toString(this.hwnff_value_sim1));
                        Rlog.d(TAG, "update hwnff Sub0 to " + this.hwnff_value_sim1);
                        return;
                    }
                } catch (RuntimeException e) {
                    Rlog.e(TAG, "write hwnff Sub" + subId + " prop failed ");
                    return;
                }
            }
            if (1 == subId && this.hwnff_value_sim2 != newSS.getDbm()) {
                this.hwnff_value_sim2 = newSS.getDbm();
                SystemProperties.set(HWNFF_RSSI_SIM2, Integer.toString(this.hwnff_value_sim2));
                Rlog.d(TAG, "update hwnff Sub1 to " + this.hwnff_value_sim2);
            }
        }
    }

    public void setPreferredNetworkTypeSafely(Phone phoneBase, ServiceStateTracker stateTracker, int networkType, Message response) {
        HwServiceStateManager.getHwServiceStateManager(stateTracker, phoneBase).setPreferredNetworkTypeSafely(phoneBase, networkType, response);
    }

    public void getLocationInfo(ServiceStateTracker stateTracker, GsmCdmaPhone phone) {
        HwServiceStateManager.getHwGsmServiceStateManager(stateTracker, phone).getLocationInfo();
    }

    public void checkAndSetNetworkType(ServiceStateTracker stateTracker, Phone phoneBase) {
        HwServiceStateManager.getHwServiceStateManager(stateTracker, phoneBase).checkAndSetNetworkType();
    }

    public void gprsAttach(ServiceStateTracker stateTracker, ServiceState mSS, ServiceState mNewSS) {
        if (mSS.getDataRegState() == 0 && mNewSS.getDataRegState() == 0 && stateTracker.getDelayUpdateGpsState() != 0) {
            stateTracker.mAttachedRegistrants.notifyRegistrants();
            stateTracker.setDelayUpdateGpsState(0);
        } else if (stateTracker.getDelayUpdateGpsState() != 0 && mNewSS.getDataRegState() == 0 && mSS.getDataRegState() != 0) {
            stateTracker.setDelayUpdateGpsState(0);
        }
    }
}
