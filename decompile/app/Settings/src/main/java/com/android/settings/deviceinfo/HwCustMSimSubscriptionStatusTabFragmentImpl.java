package com.android.settings.deviceinfo;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.MSimSmsManager;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.msim.ITelephonyMSim;
import com.android.internal.telephony.msim.ITelephonyMSim.Stub;
import com.android.settings.Utils;
import java.util.HashMap;
import java.util.HashSet;

public class HwCustMSimSubscriptionStatusTabFragmentImpl extends HwCustMSimSubscriptionStatusTabFragment {
    private static final int CDMA_NETWORK_STATION_UNKOWN = -1;
    private static final int CDMA_SYSTEM_STATION_UNKOWN = -1;
    private static final String DEFAULT_OPERATOR_NAME = "Home";
    private static final String EMPTY_STRING = "";
    private static final int GET_SMSC_DONE = 1001;
    private static final boolean HIDE_OPERATOR_NAME = SystemProperties.getBoolean("ro.config.hw_hide_operator_name", false);
    public static final int INVALID = Integer.MAX_VALUE;
    private static final boolean IS_CDMA_GSM = SystemProperties.get("ro.config.dsds_mode", EMPTY_STRING).equals("cdma_gsm");
    private static final String KEY_CELLID = "cell_id";
    private static final String KEY_ESN_NUMBER = "esn_number";
    private static final String KEY_ICC_ID = "icc_id";
    private static final String KEY_MCC_MNC = "mcc_mnc";
    private static final String KEY_MESSAGE_CENTER = "message_center";
    private static final String KEY_MIN_NUMBER = "min_number";
    private static final String KEY_MOBILE_NETWORK_STATE = "mobile_network_state";
    private static final String KEY_MOBILE_NETWORK_TYPE = "mobile_network_type";
    private static final String KEY_OPERATOR_NAME = "operator_name";
    private static final String KEY_PHONE_NUMBER = "number";
    private static final String KEY_ROAMING_STATE = "roaming_state";
    private static final String KEY_SERVICE_STATE = "service_state";
    private static final String KEY_SID_NID = "sid_nid";
    private static final String KEY_SIGNAL_STRENGTH = "signal_strength";
    private static final String KEY_TO_GET_SMSC_ADDRESS = "key_to_get_smsc_address";
    private static final boolean PLMN_TO_SETTINGS = SystemProperties.getBoolean("ro.config.plmn_to_settings", false);
    private static final String TAG = "HwCustMSimSubscriptionStatusTabFragmentImpl";
    private static final boolean isHideRoamingIcon = SystemProperties.getBoolean("ro.config.hw_hide_roaming_icon", false);
    private static final boolean isShow4_5g = SystemProperties.getBoolean("ro.config.hw_4g_to_4.5g", false);
    private static final boolean mLteShow4glte = SystemProperties.getBoolean("ro.config.lte_show_northamerica", false);
    private static String sUnknown;
    String digicelPlmn = null;
    String fourgBySimMccMnc = null;
    String lteBySimMccMnc = null;
    HashSet<String> m4gBySimMccMnc = new HashSet();
    HashSet<String> mDigicelMccmnc = new HashSet();
    private Handler mHandler = null;
    HashSet<String> mLteBySimMccMnc = new HashSet();
    private int mMainCard;
    HashSet<String> mMccMnc = new HashSet();
    private boolean mNeedDoubleSignalStrength = false;
    HashSet<String> mNextelMccMnc = new HashSet();
    private Resources mRes;
    String mStrTurkeyMccMnc = null;
    String strNextelMccMnc = null;

    public HwCustMSimSubscriptionStatusTabFragmentImpl(MSimSubscriptionStatusTabFragment mSimSubscriptionStatusTabFragment, int subscription) {
        super(mSimSubscriptionStatusTabFragment, subscription);
    }

    public void updateCustPreference(Context context) {
        this.mMainCard = Utils.getMainCardSlotId();
        this.mMSimSubscriptionStatusTabFragment.getPreferenceManager().inflateFromResource(context, 2131230778, this.mMSimSubscriptionStatusTabFragment.getPreferenceScreen());
        PreferenceScreen root = this.mMSimSubscriptionStatusTabFragment.getPreferenceScreen();
        this.mRes = this.mMSimSubscriptionStatusTabFragment.getResources();
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HwCustMSimSubscriptionStatusTabFragmentImpl.GET_SMSC_DONE /*1001*/:
                        String smscAddr = msg.getData().getString(HwCustMSimSubscriptionStatusTabFragmentImpl.KEY_TO_GET_SMSC_ADDRESS);
                        Log.v(HwCustMSimSubscriptionStatusTabFragmentImpl.TAG, "Message Center get from msg is:" + smscAddr);
                        Log.v(HwCustMSimSubscriptionStatusTabFragmentImpl.TAG, "Message Center after split is:" + HwCustMSimSubscriptionStatusTabFragmentImpl.this.splitSmscAddr(smscAddr));
                        HwCustMSimSubscriptionStatusTabFragmentImpl.this.setSummaryText(HwCustMSimSubscriptionStatusTabFragmentImpl.KEY_MESSAGE_CENTER, HwCustMSimSubscriptionStatusTabFragmentImpl.this.splitSmscAddr(smscAddr));
                        break;
                }
                super.handleMessage(msg);
            }
        };
        if (this.mMSimSubscriptionStatusTabFragment.mPhone == null) {
            return;
        }
        int cell_id;
        if (this.mMSimSubscriptionStatusTabFragment.mPhone.getPhoneType() == 2) {
            setSummaryText(KEY_ESN_NUMBER, this.mMSimSubscriptionStatusTabFragment.mPhone.getEsn());
            setSummaryText(KEY_MIN_NUMBER, this.mMSimSubscriptionStatusTabFragment.mPhone.getCdmaMin());
            if (this.mRes.getBoolean(2131492917)) {
                this.mMSimSubscriptionStatusTabFragment.findPreference(KEY_MIN_NUMBER).setTitle(2131625236);
            }
            removePreferenceFromScreen(KEY_ICC_ID);
            if (IS_CDMA_GSM) {
                removePreferenceFromScreen(KEY_MOBILE_NETWORK_STATE);
                removePreferenceFromScreen(KEY_PHONE_NUMBER);
                removePreferenceFromScreen(KEY_ROAMING_STATE);
                removePreferenceFromScreen(KEY_SERVICE_STATE);
                removePreferenceFromScreen(KEY_MESSAGE_CENTER);
                CdmaCellLocation cdmalocation = (CdmaCellLocation) this.mMSimSubscriptionStatusTabFragment.mPhone.getCellLocation();
                int sid = cdmalocation.getSystemId();
                int nid = cdmalocation.getNetworkId();
                cell_id = cdmalocation.getBaseStationId();
                String sid_nid = EMPTY_STRING;
                if (-1 != sid) {
                    sid_nid = String.valueOf(sid);
                }
                if (-1 != nid) {
                    sid_nid = sid_nid + "," + String.valueOf(nid);
                }
                setSummaryText(KEY_SID_NID, sid_nid);
                if (-1 != cell_id) {
                    setSummaryText(KEY_CELLID, String.valueOf(cell_id));
                } else {
                    setSummaryText(KEY_CELLID, EMPTY_STRING);
                }
                removePreferenceFromScreen(KEY_ESN_NUMBER);
                removePreferenceFromScreen(KEY_MIN_NUMBER);
                return;
            }
            removePreferenceFromScreen(KEY_MESSAGE_CENTER);
            removePreferenceFromScreen(KEY_MCC_MNC);
            return;
        }
        removePreferenceFromScreen(KEY_ICC_ID);
        removePreferenceFromScreen(KEY_MIN_NUMBER);
        removePreferenceFromScreen(KEY_ESN_NUMBER);
        if (IS_CDMA_GSM) {
            removePreferenceFromScreen(KEY_MOBILE_NETWORK_STATE);
            removePreferenceFromScreen(KEY_PHONE_NUMBER);
            removePreferenceFromScreen(KEY_ROAMING_STATE);
            removePreferenceFromScreen(KEY_SERVICE_STATE);
            removePreferenceFromScreen(KEY_SID_NID);
            if (this.mMSimSubscriptionStatusTabFragment.mSubscription != this.mMainCard) {
                removePreferenceFromScreen(KEY_CELLID);
            } else {
                cell_id = ((GsmCellLocation) this.mMSimSubscriptionStatusTabFragment.mPhone.getCellLocation()).getCid();
                if (-1 != cell_id) {
                    setSummaryText(KEY_CELLID, String.valueOf(cell_id));
                } else {
                    setSummaryText(KEY_CELLID, EMPTY_STRING);
                }
            }
            new Thread() {
                public void run() {
                    String mSmscAddress = MSimSmsManager.getDefault().getSmscAddrOnSubscription(HwCustMSimSubscriptionStatusTabFragmentImpl.this.mMSimSubscriptionStatusTabFragment.mSubscription);
                    Log.v(HwCustMSimSubscriptionStatusTabFragmentImpl.TAG, "Message Center get via MSimSmsManager is:" + mSmscAddress);
                    Message msg = Message.obtain();
                    msg.what = HwCustMSimSubscriptionStatusTabFragmentImpl.GET_SMSC_DONE;
                    Bundle b = new Bundle();
                    b.putString(HwCustMSimSubscriptionStatusTabFragmentImpl.KEY_TO_GET_SMSC_ADDRESS, mSmscAddress);
                    msg.setData(b);
                    HwCustMSimSubscriptionStatusTabFragmentImpl.this.mHandler.sendMessage(msg);
                }
            }.start();
            return;
        }
        removePreferenceFromScreen(KEY_MESSAGE_CENTER);
        removePreferenceFromScreen(KEY_SID_NID);
        removePreferenceFromScreen(KEY_MCC_MNC);
        removePreferenceFromScreen(KEY_CELLID);
    }

    public void updateMccMncPrefSummary() {
        if (this.mMSimSubscriptionStatusTabFragment.mServiceState != null) {
            String numeric = this.mMSimSubscriptionStatusTabFragment.mServiceState.getOperatorNumeric();
            if (!IS_CDMA_GSM) {
                removePreferenceFromScreen(KEY_MCC_MNC);
            }
            if (numeric == null || numeric.length() <= 3) {
                setSummaryText(KEY_MCC_MNC, EMPTY_STRING);
                return;
            }
            String mcc = numeric.substring(0, 3);
            setSummaryText(KEY_MCC_MNC, mcc + "," + numeric.substring(3, numeric.length()));
        }
    }

    private String splitSmscAddr(String smscAddr) {
        String messageCenter = smscAddr;
        if (TextUtils.isEmpty(smscAddr)) {
            return EMPTY_STRING;
        }
        String[] strArray = smscAddr.split("\"");
        if (strArray.length > 1) {
            messageCenter = strArray[1];
        }
        return messageCenter;
    }

    public int getVoiceNetworkType(int subscription) {
        try {
            ITelephonyMSim iTelephony = Stub.asInterface(ServiceManager.getService("phone_msim"));
            if (iTelephony != null) {
                return iTelephony.getVoiceNetworkType(subscription);
            }
            return -1;
        } catch (Exception e) {
            return -1;
        }
    }

    public int getDataNetworkType(int subscription) {
        try {
            ITelephonyMSim iTelephony = Stub.asInterface(ServiceManager.getService("phone_msim"));
            if (iTelephony != null) {
                return iTelephony.getDataNetworkType(subscription);
            }
            return -1;
        } catch (Exception e) {
            return -1;
        }
    }

    private void setSummaryText(String preference, String text) {
        if (sUnknown == null) {
            sUnknown = this.mMSimSubscriptionStatusTabFragment.getResources().getString(2131624355);
        }
        if (TextUtils.isEmpty(text)) {
            CharSequence text2 = sUnknown;
        }
        if (this.mMSimSubscriptionStatusTabFragment.findPreference(preference) != null) {
            this.mMSimSubscriptionStatusTabFragment.findPreference(preference).setSummary(text2);
        }
    }

    private void removePreferenceFromScreen(String key) {
        Preference pref = this.mMSimSubscriptionStatusTabFragment.findPreference(key);
        if (pref != null) {
            this.mMSimSubscriptionStatusTabFragment.getPreferenceScreen().removePreference(pref);
        }
    }

    public void updateDataState(TelephonyManager mTelephonyManager, boolean isCAstate) {
        int networkType;
        String networkTypeName = EMPTY_STRING;
        if (IS_CDMA_GSM && this.mMSimSubscriptionStatusTabFragment.mSubscription == this.mMainCard) {
            networkType = TelephonyManager.getDefault().getNetworkType(this.mSubscription);
            TelephonyManager.getDefault();
            if ("UMTS".equals(TelephonyManager.getNetworkTypeName(networkType))) {
                networkTypeName = "WCDMA";
            }
        }
        int voiceNetworkType = mTelephonyManager.getVoiceNetworkType(this.mMSimSubscriptionStatusTabFragment.mSubscription);
        int dataNetworkType = mTelephonyManager.getDataNetworkType(this.mMSimSubscriptionStatusTabFragment.mSubscription);
        Log.d(TAG, "updateNetworkTypeAndSignalStrength: voiceNetworkType is " + voiceNetworkType + " dataNetworkType is " + dataNetworkType);
        if (13 == dataNetworkType && (7 == voiceNetworkType || 4 == voiceNetworkType)) {
            networkTypeName = TelephonyManager.getNetworkTypeName(dataNetworkType) + "; " + TelephonyManager.getNetworkTypeName(voiceNetworkType);
            this.mNeedDoubleSignalStrength = true;
            updateDoubleSignalStrength();
        } else {
            this.mNeedDoubleSignalStrength = false;
        }
        if (networkTypeName.equals(EMPTY_STRING)) {
            networkType = TelephonyManager.getDefault().getNetworkType(this.mSubscription);
            TelephonyManager.getDefault();
            networkTypeName = TelephonyManager.getNetworkTypeName(networkType);
        }
        networkTypeName = getCustomizedNetworkType(networkTypeName);
        if ("UNKNOWN".equals(networkTypeName) && this.mRes != null) {
            networkTypeName = this.mRes.getString(2131624395);
        }
        if ("4G".equals(networkTypeName) || "LTE".equals(networkTypeName)) {
            if (isCustomMcc() && isCAstate) {
                networkTypeName = "LTE+";
            } else if (isCustomMcc()) {
                networkTypeName = "LTE";
            } else if (isCAstate) {
                networkTypeName = "4G+";
            }
            if (isTurkeySIMByMcc(this.mSubscription)) {
                networkTypeName = "4.5G";
            }
            if (isIranSIMByMccMnc(this.mSubscription) && isCAstate) {
                networkTypeName = "4.5G";
            }
            if (isShow4_5g) {
                networkTypeName = "4.5G";
            }
        }
        if (mLteShow4glte) {
            int netType = TelephonyManager.getDefault().getNetworkType(this.mSubscription);
            TelephonyManager.getDefault();
            networkTypeName = getNorthAmericanNetworkType(TelephonyManager.getNetworkTypeName(netType));
        }
        if (isNextelMccMmc(this.mSubscription) && "HSPA+".equals(networkTypeName)) {
            networkTypeName = "4G";
        }
        if (isDigicelPlmn(this.mSubscription)) {
            if ("GPRS".equals(networkTypeName)) {
                networkTypeName = "EDGE";
            }
            if ("UMTS".equals(networkTypeName)) {
                networkTypeName = "HSPA";
            }
        }
        if (isLteBySimMccMnc(this.mSubscription) && ("4G".equals(networkTypeName) || "4G+".equals(networkTypeName))) {
            networkTypeName = "LTE";
        }
        if (is4gBySimMccMnc(this.mSubscription) && "4G+".equals(networkTypeName)) {
            networkTypeName = "4G";
        }
        setSummaryText(KEY_MOBILE_NETWORK_TYPE, networkTypeName);
        updateSidNid();
    }

    private String getCustomizedNetworkType(String networkType) {
        String customizedNetworkType = networkType;
        String networkTypeStr = Systemex.getString(this.mMSimSubscriptionStatusTabFragment.mPhone.getContext().getContentResolver(), "hw_customized_networkType");
        if (TextUtils.isEmpty(networkTypeStr)) {
            return customizedNetworkType;
        }
        String resultNetworkType = (String) parseCustomizedNetworkTypeString(networkTypeStr).get(networkType);
        if (TextUtils.isEmpty(resultNetworkType)) {
            return customizedNetworkType;
        }
        return resultNetworkType;
    }

    private String getNorthAmericanNetworkType(String networkType) {
        String northamericaNetworkType = networkType;
        String networkTypeStr = Systemex.getString(this.mMSimSubscriptionStatusTabFragment.mPhone.getContext().getContentResolver(), "hw_northamerica_networkType");
        if (TextUtils.isEmpty(networkTypeStr)) {
            return northamericaNetworkType;
        }
        String resultNetworkType = (String) parseCustomizedNetworkTypeString(networkTypeStr).get(networkType);
        if (TextUtils.isEmpty(resultNetworkType)) {
            return northamericaNetworkType;
        }
        return resultNetworkType;
    }

    private boolean isCustomMcc() {
        initMccMnc();
        String currentMccMnc = TelephonyManager.getDefault().getSimOperator();
        if (this.mMccMnc.size() == 0 || currentMccMnc.length() < 3) {
            return false;
        }
        if (this.mMccMnc.contains(currentMccMnc.substring(0, 3)) || this.mMccMnc.contains(currentMccMnc)) {
            return true;
        }
        return false;
    }

    private void initMccMnc() {
        String strMccMnc = Systemex.getString(this.mMSimSubscriptionStatusTabFragment.mPhone.getContext().getContentResolver(), "hw_mcc_mnc");
        if (strMccMnc != null && this.mMccMnc.size() == 0) {
            String[] mccmnc = strMccMnc.split(";");
            for (String trim : mccmnc) {
                this.mMccMnc.add(trim.trim());
            }
        }
    }

    private boolean isNextelMccMmc(int sub) {
        if (this.strNextelMccMnc == null) {
            this.strNextelMccMnc = Systemex.getString(this.mMSimSubscriptionStatusTabFragment.mPhone.getContext().getContentResolver(), "hw_hspap_show_4g");
        }
        if (!TextUtils.isEmpty(this.strNextelMccMnc) && this.mNextelMccMnc.size() == 0) {
            String[] mccmnc = this.strNextelMccMnc.split(";");
            for (String trim : mccmnc) {
                this.mNextelMccMnc.add(trim.trim());
            }
        }
        String currentMccMnc = TelephonyManager.getDefault().getSimOperator(sub);
        if (this.mNextelMccMnc.size() != 0) {
            return this.mNextelMccMnc.contains(currentMccMnc);
        }
        return false;
    }

    private boolean isDigicelPlmn(int sub) {
        String currentMccMnc = TelephonyManager.getDefault().getSimOperator(sub);
        if (this.digicelPlmn == null) {
            this.digicelPlmn = Systemex.getString(this.mMSimSubscriptionStatusTabFragment.mPhone.getContext().getContentResolver(), "hw_customized_networkType_entry");
        }
        if (TextUtils.isEmpty(this.digicelPlmn)) {
            return false;
        }
        String[] mccmnc = this.digicelPlmn.split(";");
        for (String trim : mccmnc) {
            this.mDigicelMccmnc.add(trim.trim());
        }
        if (this.mDigicelMccmnc.contains(currentMccMnc)) {
            return true;
        }
        return false;
    }

    private boolean isLteBySimMccMnc(int sub) {
        String currentMccMnc = TelephonyManager.getDefault().getSimOperator(sub);
        if (this.lteBySimMccMnc == null) {
            this.lteBySimMccMnc = Systemex.getString(this.mMSimSubscriptionStatusTabFragment.mPhone.getContext().getContentResolver(), "hw_show_lte");
        }
        if (TextUtils.isEmpty(this.lteBySimMccMnc)) {
            return false;
        }
        String[] mccmnc = this.lteBySimMccMnc.split(";");
        for (String trim : mccmnc) {
            this.mLteBySimMccMnc.add(trim.trim());
        }
        if (this.mLteBySimMccMnc.contains(currentMccMnc)) {
            return true;
        }
        return false;
    }

    private boolean is4gBySimMccMnc(int sub) {
        String currentMccMnc = TelephonyManager.getDefault().getSimOperator(sub);
        if (this.fourgBySimMccMnc == null) {
            this.fourgBySimMccMnc = Systemex.getString(this.mMSimSubscriptionStatusTabFragment.mPhone.getContext().getContentResolver(), "hw_show_4g");
        }
        if (TextUtils.isEmpty(this.fourgBySimMccMnc)) {
            return false;
        }
        String[] mccmnc = this.fourgBySimMccMnc.split(";");
        for (String trim : mccmnc) {
            this.m4gBySimMccMnc.add(trim.trim());
        }
        if (this.m4gBySimMccMnc.contains(currentMccMnc)) {
            return true;
        }
        return false;
    }

    private boolean isTurkeySIMByMcc(int sub) {
        String currentMccMnc = TelephonyManager.getDefault().getSimOperator(sub);
        if (this.mStrTurkeyMccMnc == null) {
            this.mStrTurkeyMccMnc = System.getString(this.mMSimSubscriptionStatusTabFragment.mPhone.getContext().getContentResolver(), "hw_show_4_5G_for_mcc");
        }
        if (TextUtils.isEmpty(this.mStrTurkeyMccMnc)) {
            return false;
        }
        String[] mccmnc = this.mStrTurkeyMccMnc.split(";");
        int i = 0;
        while (i < mccmnc.length) {
            if (currentMccMnc.startsWith(mccmnc[i]) || currentMccMnc.equalsIgnoreCase(mccmnc[i])) {
                return true;
            }
            i++;
        }
        return false;
    }

    private boolean isIranSIMByMccMnc(int sub) {
        String currentMccMnc = TelephonyManager.getDefault().getSimOperator(sub);
        String customMccMnc = System.getString(this.mMSimSubscriptionStatusTabFragment.mPhone.getContext().getContentResolver(), "hw_show_4g_to_4_5g_ca");
        if (TextUtils.isEmpty(customMccMnc)) {
            return false;
        }
        String[] mccmnc = customMccMnc.split(";");
        int i = 0;
        while (i < mccmnc.length) {
            if (currentMccMnc.startsWith(mccmnc[i]) || currentMccMnc.equalsIgnoreCase(mccmnc[i])) {
                return true;
            }
            i++;
        }
        return false;
    }

    private HashMap<String, String> parseCustomizedNetworkTypeString(String networkTypeStr) {
        HashMap<String, String> networkMap = new HashMap();
        String[] units = networkTypeStr.split(";");
        if (units.length > 0) {
            for (int i = 0; i < units.length; i++) {
                if (!TextUtils.isEmpty(units[i])) {
                    String[] networkUnit = units[i].split(":");
                    if (networkUnit.length == 2) {
                        networkMap.put(networkUnit[0], networkUnit[1]);
                    }
                }
            }
        }
        return networkMap;
    }

    private void updateDoubleSignalStrength() {
        if (this.mMSimSubscriptionStatusTabFragment.mSignalStrength != null && this.mRes != null) {
            this.mMSimSubscriptionStatusTabFragment.findPreference(KEY_SIGNAL_STRENGTH).setSummary("LTE " + String.valueOf(this.mMSimSubscriptionStatusTabFragment.mSignalStrength.getLteDbm()) + " " + this.mRes.getString(2131624398) + "   " + String.valueOf(this.mMSimSubscriptionStatusTabFragment.mSignalStrength.getLteAsuLevel()) + " " + this.mRes.getString(2131624399) + "\n" + "CDMA " + String.valueOf(this.mMSimSubscriptionStatusTabFragment.mSignalStrength.getCdmaDbm()) + " " + this.mRes.getString(2131624398) + "   " + String.valueOf(this.mMSimSubscriptionStatusTabFragment.mSignalStrength.getCdmaAsuLevel()) + " " + this.mRes.getString(2131624399));
        }
    }

    public void updateSignalStrength() {
        int signalDbm = 0;
        int signalAsu = 0;
        if (this.mMSimSubscriptionStatusTabFragment.mSignalStrength != null && this.mMSimSubscriptionStatusTabFragment.mServiceState != null) {
            int state = this.mMSimSubscriptionStatusTabFragment.mServiceState.getState();
            if (1 == state || 3 == state) {
                this.mMSimSubscriptionStatusTabFragment.findPreference(KEY_SIGNAL_STRENGTH).setSummary((CharSequence) "0");
            }
            int networkType = TelephonyManager.getDefault().getNetworkType(this.mMSimSubscriptionStatusTabFragment.mSubscription);
            if (this.mMSimSubscriptionStatusTabFragment.mPhone.getPhoneType() != 2) {
                signalDbm = this.mMSimSubscriptionStatusTabFragment.mSignalStrength.getDbm();
                signalAsu = this.mMSimSubscriptionStatusTabFragment.mSignalStrength.getAsuLevel();
            } else if (networkType == 13) {
                signalDbm = this.mMSimSubscriptionStatusTabFragment.mSignalStrength.getLteDbm();
                signalAsu = this.mMSimSubscriptionStatusTabFragment.mSignalStrength.getLteAsuLevel();
            } else if (networkType == 5 || networkType == 6 || networkType == 12 || networkType == 14) {
                signalDbm = this.mMSimSubscriptionStatusTabFragment.mSignalStrength.getEvdoDbm();
                signalAsu = this.mMSimSubscriptionStatusTabFragment.mSignalStrength.getEvdoAsuLevel();
            } else if (networkType == 4 || networkType == 7) {
                signalDbm = this.mMSimSubscriptionStatusTabFragment.mSignalStrength.getCdmaDbm();
                signalAsu = this.mMSimSubscriptionStatusTabFragment.mSignalStrength.getCdmaAsuLevel();
            }
            if (-1 == signalDbm || INVALID == signalDbm) {
                signalDbm = 0;
            }
            if (-1 == signalAsu || INVALID == signalAsu) {
                signalAsu = 0;
            }
            this.mMSimSubscriptionStatusTabFragment.findPreference(KEY_SIGNAL_STRENGTH).setSummary(String.valueOf(signalDbm) + " " + this.mRes.getString(2131624398) + "   " + String.valueOf(signalAsu) + " " + this.mRes.getString(2131624399));
            if (this.mNeedDoubleSignalStrength) {
                updateDoubleSignalStrength();
            }
        }
    }

    private void updateSidNid() {
        if (!IS_CDMA_GSM) {
            return;
        }
        int cell_id;
        if (this.mMSimSubscriptionStatusTabFragment.mPhone.getPhoneType() == 2) {
            CdmaCellLocation cdmalocation = (CdmaCellLocation) this.mMSimSubscriptionStatusTabFragment.mPhone.getCellLocation();
            int sid = cdmalocation.getSystemId();
            int nid = cdmalocation.getNetworkId();
            cell_id = cdmalocation.getBaseStationId();
            String sid_nid = EMPTY_STRING;
            if (-1 != sid) {
                sid_nid = String.valueOf(sid);
            }
            if (-1 != nid) {
                sid_nid = sid_nid + "," + String.valueOf(nid);
            }
            setSummaryText(KEY_SID_NID, sid_nid);
            if (-1 != cell_id) {
                setSummaryText(KEY_CELLID, String.valueOf(cell_id));
            } else {
                setSummaryText(KEY_CELLID, EMPTY_STRING);
            }
        } else if (this.mMSimSubscriptionStatusTabFragment.mSubscription == this.mMainCard) {
            cell_id = ((GsmCellLocation) this.mMSimSubscriptionStatusTabFragment.mPhone.getCellLocation()).getCid();
            if (-1 != cell_id) {
                setSummaryText(KEY_CELLID, String.valueOf(cell_id));
            } else {
                setSummaryText(KEY_CELLID, EMPTY_STRING);
            }
        }
    }

    public void updateServiceState() {
        ServiceState mSS = this.mMSimSubscriptionStatusTabFragment.mServiceState;
        if (mSS != null) {
            int sub = this.mMSimSubscriptionStatusTabFragment.mSubscription;
            Context context = this.mMSimSubscriptionStatusTabFragment.mPhone.getContext();
            String mSimCardName = TelephonyManager.getDefault().getNetworkOperatorName(sub);
            Log.w(TAG, "mSimCardName: " + mSimCardName);
            String mOperatorName = mSS.getOperatorAlphaShort();
            String simMccmnc = TelephonyManager.getDefault().getSimOperator(sub);
            if (PLMN_TO_SETTINGS) {
                String str = EMPTY_STRING;
                try {
                    str = Systemex.getString(context.getContentResolver(), sub + "_plmn_servicestate_to_settings");
                    Log.v(TAG, "updateServiceState plmn_servicestate_to_settings = " + sub + "|" + str);
                } catch (Exception e) {
                    Log.v(TAG, "Exception when got plmn_servicestate_to_settings value", e);
                }
                if (mSimCardName.equals(EMPTY_STRING) || mSimCardName.equals(this.mRes.getString(17040036)) || mSimCardName.equals(this.mRes.getString(17040012))) {
                    mSimCardName = this.mRes.getString(2131624355);
                } else if (!(str == null || EMPTY_STRING.equals(str))) {
                    mSimCardName = str;
                }
            }
            if (isNotEqualToUI(simMccmnc)) {
                mSimCardName = mOperatorName;
            }
            setSummaryText(KEY_OPERATOR_NAME, mSimCardName);
        }
    }

    private boolean isNotEqualToUI(String mccmnc) {
        String custMccmncStrs = Systemex.getString(this.mMSimSubscriptionStatusTabFragment.mPhone.getContext().getContentResolver(), "plmnNotToSettings");
        if (TextUtils.isEmpty(custMccmncStrs) || TextUtils.isEmpty(mccmnc)) {
            Log.v(TAG, "isNotEqualToUI: plmnNotToSettings or mccmnc is empty");
            return false;
        }
        for (String area : custMccmncStrs.split(",")) {
            if (area.equals(mccmnc)) {
                return true;
            }
        }
        return false;
    }

    public boolean isHideRoaming() {
        return isHideRoamingIcon;
    }

    public boolean isHideOperatorName(String preference, String text, String unknownOperatorName) {
        if (!HIDE_OPERATOR_NAME) {
            return false;
        }
        if (unknownOperatorName.equalsIgnoreCase(text)) {
            setSummaryText(preference, unknownOperatorName);
        } else {
            setSummaryText(preference, DEFAULT_OPERATOR_NAME);
        }
        return true;
    }
}
