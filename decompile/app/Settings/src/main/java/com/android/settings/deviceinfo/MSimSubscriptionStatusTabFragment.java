package com.android.settings.deviceinfo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.telephony.HwTelephonyManager;
import android.telephony.MSimSmsManager;
import android.telephony.MSimTelephonyManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.settings.MLog;
import com.android.settings.Utils;
import com.huawei.cust.HwCustUtils;

public class MSimSubscriptionStatusTabFragment extends PreferenceFragment {
    private static final boolean IS_CDMA_GSM = SystemProperties.get("ro.config.dsds_mode", "").equals("cdma_gsm");
    private static final String[] PHONE_RELATED_ENTRIES = new String[]{"service_state", "operator_name", "roaming_state", "number", "icc_id", "prl_version", "min_number", "esn_number", "signal_strength", "baseband_version", "mobile_network_type", "mobile_network_state"};
    private static final boolean PLMN_TO_SETTINGS = SystemProperties.getBoolean("ro.config.plmn_to_settings", false);
    private Activity mContext;
    private HwCustMSimSubscriptionStatusTabFragment mCust;
    private BroadcastReceiver mGetCAstate = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.LTE_CA_STATE".equals(intent.getAction())) {
                MSimSubscriptionStatusTabFragment.this.mIsCAstate = intent.getBooleanExtra("LteCAstate", false);
                MSimSubscriptionStatusTabFragment.this.mHandler.sendEmptyMessage(700);
            }
        }
    };
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 700:
                    MSimSubscriptionStatusTabFragment.this.updateDataState();
                    Log.v("MSimSubscriptionStatusTabFragment", "updateDataState");
                    break;
                case 1001:
                    String smscAddr = msg.getData().getString("key_to_get_smsc_address");
                    Log.v("MSimSubscriptionStatusTabFragment", "Message Center get from msg is:" + smscAddr);
                    Log.v("MSimSubscriptionStatusTabFragment", "Message Center after split is:" + MSimSubscriptionStatusTabFragment.this.splitSmscAddr(smscAddr));
                    MSimSubscriptionStatusTabFragment.this.setSummaryText("message_center", MSimSubscriptionStatusTabFragment.this.splitSmscAddr(smscAddr));
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private boolean mIsCAstate = false;
    private int mMainCardSlotId;
    private boolean mNeedDoubleSignalStrength = false;
    public Phone mPhone = null;
    private PhoneStateListener mPhoneStateListener;
    private Resources mRes;
    ServiceState mServiceState;
    private Preference mSigStrength;
    SignalStrength mSignalStrength;
    private final BroadcastReceiver mSimStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                MSimSubscriptionStatusTabFragment.this.enableOrDisablePreference(Utils.isCardReady(MSimSubscriptionStatusTabFragment.this.mSubscription));
            }
        }
    };
    int mSubscription;
    private TelephonyManager mTelephonyManager;
    private String sUnknown;

    public MSimSubscriptionStatusTabFragment(int subscription) {
        this.mSubscription = subscription;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }

    private void removePreferenceFromScreen(String key) {
        Preference pref = findPreference(key);
        if (pref != null) {
            getPreferenceScreen().removePreference(pref);
        }
    }

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mCust = (HwCustMSimSubscriptionStatusTabFragment) HwCustUtils.createObj(HwCustMSimSubscriptionStatusTabFragment.class, new Object[]{this, Integer.valueOf(this.mSubscription)});
        addPreferencesFromResource(2131230777);
        this.mRes = getResources();
        try {
            this.mPhone = PhoneFactory.getPhone(this.mSubscription);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (this.mPhone == null) {
            getActivity().finish();
            return;
        }
        this.mMainCardSlotId = Utils.getMainCardSlotId();
        if (!Utils.isWifiOnly(this.mContext)) {
            this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
            this.mPhoneStateListener = getPhoneStateListener(this.mSubscription);
            this.mTelephonyManager.listen(this.mPhoneStateListener, 321);
        }
        this.sUnknown = this.mRes.getString(2131624355);
        this.mSigStrength = findPreference("signal_strength");
        if (Utils.isWifiOnly(this.mContext.getApplicationContext())) {
            for (String key : PHONE_RELATED_ENTRIES) {
                removePreferenceFromScreen(key);
            }
        } else {
            try {
                Log.d("MSimSubscriptionStatusTabFragment", "multimode_cdma=" + SystemProperties.getBoolean("ro.config.multimode_cdma", false) + ",getPhoneName=" + this.mPhone.getPhoneName() + ", prl_version=" + this.mPhone.getCdmaPrlVersion() + ", telecom=" + Utils.isChinaTelecomArea() + ", main card=" + this.mMainCardSlotId);
                CdmaCellLocation cdmalocation;
                int sid;
                int nid;
                int cell_id;
                String sid_nid;
                String rawNumber;
                String formattedNumber;
                if (SystemProperties.getBoolean("ro.config.multimode_cdma", false) || this.mPhone.getPhoneName().equals("CDMA")) {
                    setSummaryText("prl_version", this.mPhone.getCdmaPrlVersion());
                    if (this.mPhone.getPhoneType() == 2) {
                        setSummaryText("esn_number", this.mPhone.getEsn());
                        setSummaryText("min_number", this.mPhone.getCdmaMin());
                        if (getResources().getBoolean(2131492917)) {
                            findPreference("min_number").setTitle(2131625236);
                        }
                        removePreferenceFromScreen("icc_id");
                        if (IS_CDMA_GSM) {
                            removePreferenceFromScreen("mobile_network_state");
                            removePreferenceFromScreen("number");
                            removePreferenceFromScreen("roaming_state");
                            removePreferenceFromScreen("service_state");
                            removePreferenceFromScreen("message_center");
                            cdmalocation = (CdmaCellLocation) this.mPhone.getCellLocation();
                            sid = cdmalocation.getSystemId();
                            nid = cdmalocation.getNetworkId();
                            cell_id = cdmalocation.getBaseStationId();
                            sid_nid = "";
                            if (-1 != sid) {
                                sid_nid = String.valueOf(sid);
                            }
                            if (-1 != nid) {
                                sid_nid = sid_nid + "," + String.valueOf(nid);
                            }
                            setSummaryText("sid_nid", sid_nid);
                            if (-1 != cell_id) {
                                setSummaryText("cell_id", String.valueOf(cell_id));
                            } else {
                                setSummaryText("cell_id", "");
                            }
                            removePreferenceFromScreen("esn_number");
                            removePreferenceFromScreen("min_number");
                        } else {
                            removePreferenceFromScreen("message_center");
                            removePreferenceFromScreen("mcc_mnc");
                        }
                    } else {
                        removePreferenceFromScreen("esn_number");
                        removePreferenceFromScreen("min_number");
                        removePreferenceFromScreen("icc_id");
                        if (IS_CDMA_GSM) {
                            removePreferenceFromScreen("mobile_network_state");
                            removePreferenceFromScreen("number");
                            removePreferenceFromScreen("roaming_state");
                            removePreferenceFromScreen("service_state");
                            removePreferenceFromScreen("sid_nid");
                            if (this.mSubscription != this.mMainCardSlotId) {
                                removePreferenceFromScreen("cell_id");
                            } else {
                                cell_id = ((GsmCellLocation) this.mPhone.getCellLocation()).getCid();
                                if (-1 != cell_id) {
                                    setSummaryText("cell_id", String.valueOf(cell_id));
                                } else {
                                    setSummaryText("cell_id", "");
                                }
                            }
                            new Thread() {
                                public void run() {
                                    String mSmscAddress = MSimSmsManager.getDefault().getSmscAddrOnSubscription(MSimSubscriptionStatusTabFragment.this.mSubscription);
                                    Log.v("MSimSubscriptionStatusTabFragment", "Message Center get via MSimSmsManager is:" + mSmscAddress);
                                    Message msg = Message.obtain();
                                    msg.what = 1001;
                                    Bundle b = new Bundle();
                                    b.putString("key_to_get_smsc_address", mSmscAddress);
                                    msg.setData(b);
                                    MSimSubscriptionStatusTabFragment.this.mHandler.sendMessage(msg);
                                }
                            }.start();
                        } else {
                            removePreferenceFromScreen("message_center");
                            removePreferenceFromScreen("sid_nid");
                            removePreferenceFromScreen("mcc_mnc");
                            removePreferenceFromScreen("cell_id");
                        }
                    }
                    rawNumber = "";
                    rawNumber = this.mPhone.getLine1Number();
                    formattedNumber = null;
                    if (!TextUtils.isEmpty(rawNumber)) {
                        formattedNumber = PhoneNumberUtils.formatNumber(rawNumber);
                    }
                    setSummaryText("number", formattedNumber);
                    setSummaryText("baseband_version", MSimTelephonyManager.getTelephonyProperty("gsm.version.baseband", this.mSubscription, ""));
                    removePreferenceFromScreen("baseband_version");
                } else if (Utils.isChinaTelecomArea() && this.mSubscription == this.mMainCardSlotId) {
                    setSummaryText("prl_version", this.mPhone.getCdmaPrlVersion());
                    if (this.mPhone.getPhoneType() == 2) {
                        setSummaryText("esn_number", this.mPhone.getEsn());
                        setSummaryText("min_number", this.mPhone.getCdmaMin());
                        if (getResources().getBoolean(2131492917)) {
                            findPreference("min_number").setTitle(2131625236);
                        }
                        removePreferenceFromScreen("icc_id");
                        if (IS_CDMA_GSM) {
                            removePreferenceFromScreen("mobile_network_state");
                            removePreferenceFromScreen("number");
                            removePreferenceFromScreen("roaming_state");
                            removePreferenceFromScreen("service_state");
                            removePreferenceFromScreen("message_center");
                            cdmalocation = (CdmaCellLocation) this.mPhone.getCellLocation();
                            sid = cdmalocation.getSystemId();
                            nid = cdmalocation.getNetworkId();
                            cell_id = cdmalocation.getBaseStationId();
                            sid_nid = "";
                            if (-1 != sid) {
                                sid_nid = String.valueOf(sid);
                            }
                            if (-1 != nid) {
                                sid_nid = sid_nid + "," + String.valueOf(nid);
                            }
                            setSummaryText("sid_nid", sid_nid);
                            if (-1 != cell_id) {
                                setSummaryText("cell_id", String.valueOf(cell_id));
                            } else {
                                setSummaryText("cell_id", "");
                            }
                            removePreferenceFromScreen("esn_number");
                            removePreferenceFromScreen("min_number");
                        } else {
                            removePreferenceFromScreen("message_center");
                            removePreferenceFromScreen("mcc_mnc");
                        }
                    } else {
                        removePreferenceFromScreen("esn_number");
                        removePreferenceFromScreen("min_number");
                        removePreferenceFromScreen("icc_id");
                        if (IS_CDMA_GSM) {
                            removePreferenceFromScreen("mobile_network_state");
                            removePreferenceFromScreen("number");
                            removePreferenceFromScreen("roaming_state");
                            removePreferenceFromScreen("service_state");
                            removePreferenceFromScreen("sid_nid");
                            if (this.mSubscription != this.mMainCardSlotId) {
                                removePreferenceFromScreen("cell_id");
                            } else {
                                cell_id = ((GsmCellLocation) this.mPhone.getCellLocation()).getCid();
                                if (-1 != cell_id) {
                                    setSummaryText("cell_id", String.valueOf(cell_id));
                                } else {
                                    setSummaryText("cell_id", "");
                                }
                            }
                            /* anonymous class already generated */.start();
                        } else {
                            removePreferenceFromScreen("message_center");
                            removePreferenceFromScreen("sid_nid");
                            removePreferenceFromScreen("mcc_mnc");
                            removePreferenceFromScreen("cell_id");
                        }
                    }
                    rawNumber = "";
                    rawNumber = this.mPhone.getLine1Number();
                    formattedNumber = null;
                    if (TextUtils.isEmpty(rawNumber)) {
                        formattedNumber = PhoneNumberUtils.formatNumber(rawNumber);
                    }
                    setSummaryText("number", formattedNumber);
                    setSummaryText("baseband_version", MSimTelephonyManager.getTelephonyProperty("gsm.version.baseband", this.mSubscription, ""));
                    removePreferenceFromScreen("baseband_version");
                } else {
                    removePreferenceFromScreen("prl_version");
                    if (this.mPhone.getPhoneType() == 2) {
                        removePreferenceFromScreen("esn_number");
                        removePreferenceFromScreen("min_number");
                        removePreferenceFromScreen("icc_id");
                        if (IS_CDMA_GSM) {
                            removePreferenceFromScreen("message_center");
                            removePreferenceFromScreen("sid_nid");
                            removePreferenceFromScreen("mcc_mnc");
                            removePreferenceFromScreen("cell_id");
                        } else {
                            removePreferenceFromScreen("mobile_network_state");
                            removePreferenceFromScreen("number");
                            removePreferenceFromScreen("roaming_state");
                            removePreferenceFromScreen("service_state");
                            removePreferenceFromScreen("sid_nid");
                            if (this.mSubscription != this.mMainCardSlotId) {
                                cell_id = ((GsmCellLocation) this.mPhone.getCellLocation()).getCid();
                                if (-1 != cell_id) {
                                    setSummaryText("cell_id", "");
                                } else {
                                    setSummaryText("cell_id", String.valueOf(cell_id));
                                }
                            } else {
                                removePreferenceFromScreen("cell_id");
                            }
                            /* anonymous class already generated */.start();
                        }
                    } else {
                        setSummaryText("esn_number", this.mPhone.getEsn());
                        setSummaryText("min_number", this.mPhone.getCdmaMin());
                        if (getResources().getBoolean(2131492917)) {
                            findPreference("min_number").setTitle(2131625236);
                        }
                        removePreferenceFromScreen("icc_id");
                        if (IS_CDMA_GSM) {
                            removePreferenceFromScreen("message_center");
                            removePreferenceFromScreen("mcc_mnc");
                        } else {
                            removePreferenceFromScreen("mobile_network_state");
                            removePreferenceFromScreen("number");
                            removePreferenceFromScreen("roaming_state");
                            removePreferenceFromScreen("service_state");
                            removePreferenceFromScreen("message_center");
                            cdmalocation = (CdmaCellLocation) this.mPhone.getCellLocation();
                            sid = cdmalocation.getSystemId();
                            nid = cdmalocation.getNetworkId();
                            cell_id = cdmalocation.getBaseStationId();
                            sid_nid = "";
                            if (-1 != sid) {
                                sid_nid = String.valueOf(sid);
                            }
                            if (-1 != nid) {
                                sid_nid = sid_nid + "," + String.valueOf(nid);
                            }
                            setSummaryText("sid_nid", sid_nid);
                            if (-1 != cell_id) {
                                setSummaryText("cell_id", "");
                            } else {
                                setSummaryText("cell_id", String.valueOf(cell_id));
                            }
                            removePreferenceFromScreen("esn_number");
                            removePreferenceFromScreen("min_number");
                        }
                    }
                    rawNumber = "";
                    rawNumber = this.mPhone.getLine1Number();
                    formattedNumber = null;
                    if (TextUtils.isEmpty(rawNumber)) {
                        formattedNumber = PhoneNumberUtils.formatNumber(rawNumber);
                    }
                    setSummaryText("number", formattedNumber);
                    setSummaryText("baseband_version", MSimTelephonyManager.getTelephonyProperty("gsm.version.baseband", this.mSubscription, ""));
                    removePreferenceFromScreen("baseband_version");
                }
            } catch (Exception ex2) {
                ex2.printStackTrace();
                removePreferenceFromScreen("prl_version");
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        if (getListView() != null) {
            setDivider(getResources().getDrawable(2130838529));
        }
        return root;
    }

    private String splitSmscAddr(String smscAddr) {
        String messageCenter = smscAddr;
        if (TextUtils.isEmpty(smscAddr)) {
            return "";
        }
        String[] strArray = smscAddr.split("\"");
        if (strArray.length > 1) {
            messageCenter = strArray[1];
        }
        return messageCenter;
    }

    public void onResume() {
        super.onResume();
        if (!Utils.isWifiOnly(this.mContext.getApplicationContext())) {
            this.mIsCAstate = this.mContext.getApplicationContext().getSharedPreferences("caStatePreferences", 0).getBoolean("isCAstate", false);
            this.mContext.registerReceiver(this.mSimStateReceiver, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
            this.mContext.registerReceiver(this.mGetCAstate, new IntentFilter("android.intent.action.LTE_CA_STATE"));
            try {
                this.mTelephonyManager.listen(this.mPhoneStateListener, 321);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            updateSignalStrength();
            updateServiceState();
            updateDataState();
        }
    }

    public void onPause() {
        super.onPause();
        try {
            if (!Utils.isWifiOnly(this.mContext.getApplicationContext())) {
                Log.v("MSimSubscriptionStatusTabFragment", "onpause : msubscription = " + this.mSubscription);
                this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
                this.mContext.unregisterReceiver(this.mSimStateReceiver);
                this.mContext.unregisterReceiver(this.mGetCAstate);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void enableOrDisablePreference(boolean enabled) {
        int count = getPreferenceScreen().getPreferenceCount();
        for (int index = 0; index < count; index++) {
            Preference pref = getPreferenceScreen().getPreference(index);
            if (pref != null) {
                pref.setEnabled(enabled);
            }
        }
    }

    private PhoneStateListener getPhoneStateListener(int subscription) {
        int subId = 0;
        if (SubscriptionManager.getSubId(subscription) == null || SubscriptionManager.getSubId(subscription).length < 1) {
            MLog.e("MSimSubscriptionStatusTabFragment", "can not get subIds");
        } else {
            subId = SubscriptionManager.getSubId(subscription)[0];
        }
        MLog.i("MSimSubscriptionStatusTabFragment", "getPhoneStateListener subId:" + subId);
        return new PhoneStateListener(subId) {
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                Log.i("MSimSubscriptionStatusTabFragment", "updateSignalStrength:" + signalStrength);
                MSimSubscriptionStatusTabFragment.this.mSignalStrength = signalStrength;
                MSimSubscriptionStatusTabFragment.this.updateSignalStrength();
            }

            public void onServiceStateChanged(ServiceState state) {
                Log.i("MSimSubscriptionStatusTabFragment", "updateServiceState");
                MSimSubscriptionStatusTabFragment.this.mServiceState = state;
                MSimSubscriptionStatusTabFragment.this.updateServiceState();
                MSimSubscriptionStatusTabFragment.this.updateMccMncPrefSummary();
            }

            public void onDataConnectionStateChanged(int state, int networkType) {
                Log.i("MSimSubscriptionStatusTabFragment", "updateDataState");
                MSimSubscriptionStatusTabFragment.this.updateDataState();
            }
        };
    }

    private void updateMccMncPrefSummary() {
        if (this.mServiceState != null) {
            String numeric = this.mServiceState.getOperatorNumeric();
            if (!IS_CDMA_GSM) {
                removePreferenceFromScreen("mcc_mnc");
            }
            if (numeric == null || numeric.length() <= 3) {
                setSummaryText("mcc_mnc", "");
                return;
            }
            String mcc = numeric.substring(0, 3);
            setSummaryText("mcc_mnc", mcc + "," + numeric.substring(3, numeric.length()));
        }
    }

    private void updateServiceState() {
        String display = this.mRes.getString(2131624395);
        if (this.mServiceState != null) {
            switch (this.mServiceState.getState()) {
                case 0:
                    display = this.mRes.getString(2131624382);
                    break;
                case 1:
                case 2:
                    display = this.mRes.getString(2131624383);
                    break;
                case 3:
                    display = this.mRes.getString(2131624385);
                    break;
            }
            setSummaryText("service_state", display);
            if (!this.mServiceState.getRoaming() || this.mCust == null || this.mCust.isHideRoaming()) {
                setSummaryText("roaming_state", this.mRes.getString(2131624387));
            } else {
                setSummaryText("roaming_state", this.mRes.getString(2131624386));
            }
            setSummaryText("operator_name", this.mServiceState.getOperatorAlphaLong());
            Context context = this.mPhone.getContext();
            String mSimCardName = TelephonyManager.getDefault().getNetworkOperatorName(this.mSubscription);
            Log.w("MSimSubscriptionStatusTabFragment", "mSimCardName: " + mSimCardName);
            String mOperatorName = this.mServiceState.getOperatorAlphaShort();
            String simMccmnc = TelephonyManager.getDefault().getSimOperator(this.mSubscription);
            if (PLMN_TO_SETTINGS) {
                String str = "";
                try {
                    str = System.getString(context.getContentResolver(), this.mSubscription + "_plmn_servicestate_to_settings");
                    Log.v("MSimSubscriptionStatusTabFragment", "updateServiceState plmn_servicestate_to_settings = " + this.mSubscription + "|" + str);
                } catch (Exception e) {
                    Log.v("MSimSubscriptionStatusTabFragment", "Exception when got plmn_servicestate_to_settings value", e);
                }
                if (mSimCardName.equals("") || mSimCardName.equals(this.mRes.getString(17040036)) || mSimCardName.equals(this.mRes.getString(17040012))) {
                    mSimCardName = this.mRes.getString(2131624355);
                } else if (!(str == null || "".equals(str))) {
                    mSimCardName = str;
                }
            }
            if (isNotEqualToUI(simMccmnc)) {
                Log.w("MSimSubscriptionStatusTabFragment", "mOperatorName: " + mOperatorName);
                mSimCardName = mOperatorName;
            }
            String customizedOperatorName = System.getString(context.getContentResolver(), "hw_customized_operator_name");
            if (!TextUtils.isEmpty(customizedOperatorName)) {
                mSimCardName = customizedOperatorName;
            }
            if (this.mCust == null || !this.mCust.isHideOperatorName("operator_name", mSimCardName, this.mRes.getString(2131624355))) {
                setSummaryText("operator_name", mSimCardName);
            }
        }
    }

    private boolean isNotEqualToUI(String mccmnc) {
        String custMccmncStrs = System.getString(this.mPhone.getContext().getContentResolver(), "plmnNotToSettings");
        if (TextUtils.isEmpty(custMccmncStrs) || TextUtils.isEmpty(mccmnc)) {
            Log.v("MSimSubscriptionStatusTabFragment", "isNotEqualToUI: plmnNotToSettings or mccmnc is empty");
            return false;
        }
        for (String area : custMccmncStrs.split(",")) {
            if (area.equals(mccmnc)) {
                Log.v("MSimSubscriptionStatusTabFragment", "isNotEqualToUI: mccmnc=" + mccmnc);
                return true;
            }
        }
        return false;
    }

    private void updateSignalStrength() {
        if (this.mPhone != null) {
            this.mServiceState = this.mPhone.getServiceState();
        }
        if (!(this.mSignalStrength == null || this.mServiceState == null)) {
            int signalDbm = 0;
            int signalAsu = 0;
            int voiceState = this.mServiceState.getState();
            boolean isOutOfVoiceService = false;
            if (1 == voiceState || 3 == voiceState) {
                isOutOfVoiceService = true;
                Log.i("MSimSubscriptionStatusTabFragment", "VoiceServiceState is out of service:" + voiceState);
            }
            int dataState = this.mServiceState.getDataRegState();
            boolean isOutOfDataService = false;
            if (1 == dataState || 3 == dataState) {
                isOutOfDataService = true;
                Log.i("MSimSubscriptionStatusTabFragment", "DataService is out of service:" + dataState);
            }
            if (isOutOfDataService && isOutOfVoiceService) {
                Log.i("MSimSubscriptionStatusTabFragment", "ServiceState is out of service:");
                this.mSigStrength.setSummary((CharSequence) "0");
                return;
            }
            int networkType = TelephonyManager.getDefault().getNetworkType(this.mSubscription);
            Log.i("MSimSubscriptionStatusTabFragment", "mSubscription = " + this.mSubscription + "; networkType = " + networkType);
            if (this.mPhone.getPhoneType() == 2) {
                Log.i("MSimSubscriptionStatusTabFragment", "getPhoneType is PHONE_TYPE_CDMA");
                if (networkType == 13) {
                    signalDbm = this.mSignalStrength.getLteDbm();
                    signalAsu = this.mSignalStrength.getLteAsuLevel();
                } else if (networkType == 5 || networkType == 6 || networkType == 12 || networkType == 14) {
                    signalDbm = this.mSignalStrength.getEvdoDbm();
                    signalAsu = this.mSignalStrength.getEvdoAsuLevel();
                } else if (networkType == 4 || networkType == 7) {
                    signalDbm = this.mSignalStrength.getCdmaDbm();
                    signalAsu = this.mSignalStrength.getCdmaAsuLevel();
                }
            } else {
                signalDbm = this.mSignalStrength.getDbm();
                signalAsu = this.mSignalStrength.getAsuLevel();
            }
            if (-1 == signalDbm || HwCustMSimSubscriptionStatusTabFragmentImpl.INVALID == signalDbm) {
                signalDbm = 0;
            }
            if (-1 == signalAsu || HwCustMSimSubscriptionStatusTabFragmentImpl.INVALID == signalAsu) {
                signalAsu = 0;
            }
            CharSequence singal = String.valueOf(signalDbm) + " " + this.mRes.getString(2131624398) + "   " + String.valueOf(signalAsu) + " " + this.mRes.getString(2131624399);
            this.mSigStrength.setSummary(singal);
            Log.i("MSimSubscriptionStatusTabFragment", "singal is:" + singal);
            if (this.mNeedDoubleSignalStrength) {
                updateDoubleSignalStrength();
            }
        }
    }

    private void updateDoubleSignalStrength() {
        if (this.mSignalStrength != null) {
            this.mSigStrength.setSummary("LTE " + String.valueOf(this.mSignalStrength.getLteDbm()) + " " + this.mRes.getString(2131624398) + "   " + String.valueOf(this.mSignalStrength.getLteAsuLevel()) + " " + this.mRes.getString(2131624399) + "\n" + "CDMA " + String.valueOf(this.mSignalStrength.getCdmaDbm()) + " " + this.mRes.getString(2131624398) + "   " + String.valueOf(this.mSignalStrength.getCdmaAsuLevel()) + " " + this.mRes.getString(2131624399));
        }
    }

    private void updateDataState() {
        int networkType;
        int state = HwTelephonyManager.getDefault().getDataState((long) this.mSubscription);
        String display = this.mRes.getString(2131624395);
        switch (state) {
            case 0:
                display = this.mRes.getString(2131624391);
                break;
            case 1:
                display = this.mRes.getString(2131624392);
                break;
            case 2:
                display = this.mRes.getString(2131624393);
                break;
            case 3:
                display = this.mRes.getString(2131624394);
                break;
        }
        setSummaryText("mobile_network_state", display);
        String networkTypeName = MSimTelephonyManager.getNetworkTypeName(this.mSubscription);
        if (IS_CDMA_GSM && this.mSubscription == this.mMainCardSlotId) {
            networkType = TelephonyManager.getDefault().getNetworkType(this.mSubscription);
            TelephonyManager.getDefault();
            if ("UMTS".equals(TelephonyManager.getNetworkTypeName(networkType))) {
                networkTypeName = "WCDMA";
            }
        }
        int voiceNetworkType = this.mTelephonyManager.getVoiceNetworkType(this.mSubscription);
        int dataNetworkType = this.mTelephonyManager.getDataNetworkType(this.mSubscription);
        Log.d("MSimSubscriptionStatusTabFragment", "updateNetworkTypeAndSignalStrength: voiceNetworkType is " + voiceNetworkType + " dataNetworkType is " + dataNetworkType);
        if (13 == dataNetworkType && (7 == voiceNetworkType || 4 == voiceNetworkType)) {
            networkTypeName = TelephonyManager.getNetworkTypeName(dataNetworkType) + "; " + TelephonyManager.getNetworkTypeName(voiceNetworkType);
            this.mNeedDoubleSignalStrength = true;
            updateDoubleSignalStrength();
        } else {
            this.mNeedDoubleSignalStrength = false;
        }
        if (networkTypeName.equals("")) {
            networkType = TelephonyManager.getDefault().getNetworkType(this.mSubscription);
            TelephonyManager.getDefault();
            networkTypeName = TelephonyManager.getNetworkTypeName(networkType);
        }
        if ("UNKNOWN".equals(networkTypeName)) {
            networkTypeName = this.sUnknown;
        }
        setSummaryText("mobile_network_type", networkTypeName);
        updateSidNid();
        if (this.mCust != null) {
            this.mCust.updateDataState(this.mTelephonyManager, this.mIsCAstate);
        }
    }

    private void updateSidNid() {
        if (!IS_CDMA_GSM) {
            return;
        }
        int cell_id;
        if (this.mPhone.getPhoneType() == 2) {
            CdmaCellLocation cdmalocation = (CdmaCellLocation) this.mPhone.getCellLocation();
            int sid = cdmalocation.getSystemId();
            int nid = cdmalocation.getNetworkId();
            cell_id = cdmalocation.getBaseStationId();
            String sid_nid = "";
            if (-1 != sid) {
                sid_nid = String.valueOf(sid);
            }
            if (-1 != nid) {
                sid_nid = sid_nid + "," + String.valueOf(nid);
            }
            setSummaryText("sid_nid", sid_nid);
            if (-1 != cell_id) {
                setSummaryText("cell_id", String.valueOf(cell_id));
            } else {
                setSummaryText("cell_id", "");
            }
        } else if (this.mSubscription == this.mMainCardSlotId) {
            cell_id = ((GsmCellLocation) this.mPhone.getCellLocation()).getCid();
            if (-1 != cell_id) {
                setSummaryText("cell_id", String.valueOf(cell_id));
            } else {
                setSummaryText("cell_id", "");
            }
        }
    }

    private void setSummaryText(String preference, String text) {
        if (TextUtils.isEmpty(text)) {
            CharSequence text2 = this.sUnknown;
        }
        if (findPreference(preference) != null) {
            findPreference(preference).setSummary(text2);
        }
    }
}
