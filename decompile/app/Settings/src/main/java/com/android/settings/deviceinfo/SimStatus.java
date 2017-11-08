package com.android.settings.deviceinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.telephony.CarrierConfigManager;
import android.telephony.CellBroadcastMessage;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import com.android.internal.telephony.DefaultPhoneNotifier;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import java.util.List;

public class SimStatus extends SettingsPreferenceFragment {
    private BroadcastReceiver mAreaInfoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.cellbroadcastreceiver.CB_AREA_INFO_RECEIVED".equals(intent.getAction())) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    CellBroadcastMessage cbMessage = (CellBroadcastMessage) extras.get("message");
                    if (cbMessage != null && cbMessage.getServiceCategory() == 50) {
                        SimStatus.this.updateAreaInfo(cbMessage.getMessageBody());
                    }
                }
            }
        }
    };
    private CarrierConfigManager mCarrierConfigManager;
    private String mDefaultText;
    private TabContentFactory mEmptyTabContent = new TabContentFactory() {
        public View createTabContent(String tag) {
            return new View(SimStatus.this.mTabHost.getContext());
        }
    };
    private ListView mListView;
    private Phone mPhone = null;
    private PhoneStateListener mPhoneStateListener;
    private Resources mRes;
    private List<SubscriptionInfo> mSelectableSubInfos;
    private boolean mShowICCID;
    private boolean mShowLatestAreaInfo;
    private Preference mSignalStrength;
    private SubscriptionInfo mSir;
    private TabHost mTabHost;
    private OnTabChangeListener mTabListener = new OnTabChangeListener() {
        public void onTabChanged(String tabId) {
            SimStatus.this.mSir = (SubscriptionInfo) SimStatus.this.mSelectableSubInfos.get(Integer.parseInt(tabId));
            SimStatus.this.updatePhoneInfos();
            SimStatus.this.mTelephonyManager.listen(SimStatus.this.mPhoneStateListener, 321);
            SimStatus.this.updateDataState();
            SimStatus.this.updateNetworkType();
            SimStatus.this.updatePreference();
        }
    };
    private TabWidget mTabWidget;
    private TelephonyManager mTelephonyManager;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mTelephonyManager = (TelephonyManager) getSystemService("phone");
        this.mCarrierConfigManager = (CarrierConfigManager) getSystemService("carrier_config");
        this.mSelectableSubInfos = SubscriptionManager.from(getContext()).getActiveSubscriptionInfoList();
        addPreferencesFromResource(2131230773);
        this.mRes = getResources();
        this.mDefaultText = this.mRes.getString(2131624355);
        this.mSignalStrength = findPreference("signal_strength");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SubscriptionInfo subscriptionInfo = null;
        if (this.mSelectableSubInfos == null) {
            this.mSir = null;
        } else {
            if (this.mSelectableSubInfos.size() > 0) {
                subscriptionInfo = (SubscriptionInfo) this.mSelectableSubInfos.get(0);
            }
            this.mSir = subscriptionInfo;
            if (this.mSelectableSubInfos.size() > 1) {
                View view = inflater.inflate(2130968831, container, false);
                ViewGroup prefs_container = (ViewGroup) view.findViewById(2131886191);
                Utils.prepareCustomPreferencesList(container, view, prefs_container, false);
                prefs_container.addView(super.onCreateView(inflater, prefs_container, savedInstanceState));
                this.mTabHost = (TabHost) view.findViewById(16908306);
                this.mTabWidget = (TabWidget) view.findViewById(16908307);
                this.mListView = (ListView) view.findViewById(16908298);
                this.mTabHost.setup();
                this.mTabHost.setOnTabChangedListener(this.mTabListener);
                this.mTabHost.clearAllTabs();
                for (int i = 0; i < this.mSelectableSubInfos.size(); i++) {
                    this.mTabHost.addTab(buildTabSpec(String.valueOf(i), String.valueOf(((SubscriptionInfo) this.mSelectableSubInfos.get(i)).getDisplayName())));
                }
                return view;
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updatePhoneInfos();
    }

    protected int getMetricsCategory() {
        return 43;
    }

    public void onResume() {
        super.onResume();
        if (this.mPhone != null) {
            updatePreference();
            updateSignalStrength(this.mPhone.getSignalStrength());
            updateServiceState(this.mPhone.getServiceState());
            updateDataState();
            this.mTelephonyManager.listen(this.mPhoneStateListener, 321);
            if (this.mShowLatestAreaInfo) {
                getContext().registerReceiver(this.mAreaInfoReceiver, new IntentFilter("android.cellbroadcastreceiver.CB_AREA_INFO_RECEIVED"), "android.permission.RECEIVE_EMERGENCY_BROADCAST", null);
                getContext().sendBroadcastAsUser(new Intent("android.cellbroadcastreceiver.GET_LATEST_CB_AREA_INFO"), UserHandle.ALL, "android.permission.RECEIVE_EMERGENCY_BROADCAST");
            }
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mPhone != null) {
            this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
        }
        if (this.mShowLatestAreaInfo) {
            getContext().unregisterReceiver(this.mAreaInfoReceiver);
        }
    }

    private void removePreferenceFromScreen(String key) {
        Preference pref = findPreference(key);
        if (pref != null) {
            getPreferenceScreen().removePreference(pref);
        }
    }

    private void setSummaryText(String key, String text) {
        if (TextUtils.isEmpty(text)) {
            CharSequence text2 = this.mDefaultText;
        }
        Preference preference = findPreference(key);
        if (preference != null) {
            preference.setSummary(text2);
        }
    }

    private void updateNetworkType() {
        String networktype = null;
        int subId = this.mSir.getSubscriptionId();
        int actualDataNetworkType = this.mTelephonyManager.getDataNetworkType(this.mSir.getSubscriptionId());
        int actualVoiceNetworkType = this.mTelephonyManager.getVoiceNetworkType(this.mSir.getSubscriptionId());
        TelephonyManager telephonyManager;
        if (actualDataNetworkType != 0) {
            telephonyManager = this.mTelephonyManager;
            networktype = TelephonyManager.getNetworkTypeName(actualDataNetworkType);
        } else if (actualVoiceNetworkType != 0) {
            telephonyManager = this.mTelephonyManager;
            networktype = TelephonyManager.getNetworkTypeName(actualVoiceNetworkType);
        }
        boolean show4GForLTE = false;
        try {
            Context con = getActivity().createPackageContext("com.android.systemui", 0);
            show4GForLTE = con.getResources().getBoolean(con.getResources().getIdentifier("config_show4GForLTE", "bool", "com.android.systemui"));
        } catch (NameNotFoundException e) {
            Log.e("SimStatus", "NameNotFoundException for show4GFotLTE");
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        if (networktype != null && networktype.equals("LTE") && r7) {
            networktype = "4G";
        }
        setSummaryText("network_type", networktype);
    }

    private void updateDataState() {
        int state = DefaultPhoneNotifier.convertDataState(this.mPhone.getDataConnectionState());
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
        setSummaryText("data_state", display);
    }

    private void updateServiceState(ServiceState serviceState) {
        int state = serviceState.getState();
        String display = this.mRes.getString(2131624395);
        switch (state) {
            case 0:
                display = this.mRes.getString(2131624382);
                break;
            case 1:
                this.mSignalStrength.setSummary((CharSequence) "0");
                break;
            case 2:
                break;
            case 3:
                display = this.mRes.getString(2131624385);
                this.mSignalStrength.setSummary((CharSequence) "0");
                break;
        }
        display = this.mRes.getString(2131624383);
        setSummaryText("service_state", display);
        if (serviceState.getRoaming()) {
            setSummaryText("roaming_state", this.mRes.getString(2131624386));
        } else {
            setSummaryText("roaming_state", this.mRes.getString(2131624387));
        }
        setSummaryText("operator_name", serviceState.getOperatorAlphaLong());
    }

    private void updateAreaInfo(String areaInfo) {
        if (areaInfo != null) {
            setSummaryText("latest_area_info", areaInfo);
        }
    }

    void updateSignalStrength(SignalStrength signalStrength) {
        if (this.mSignalStrength != null) {
            int state = this.mPhone.getServiceState().getState();
            if (1 == state || 3 == state) {
                this.mSignalStrength.setSummary((CharSequence) "0");
                return;
            }
            int signalDbm = signalStrength.getDbm();
            int signalAsu = signalStrength.getAsuLevel();
            if (-1 == signalDbm) {
                signalDbm = 0;
            }
            if (-1 == signalAsu) {
                signalAsu = 0;
            }
            this.mSignalStrength.setSummary(this.mRes.getString(2131626630, new Object[]{Integer.valueOf(signalDbm), Integer.valueOf(signalAsu)}));
        }
    }

    private void updatePreference() {
        if (this.mPhone.getPhoneType() != 2 && "br".equals(this.mTelephonyManager.getSimCountryIso(this.mSir.getSubscriptionId()))) {
            this.mShowLatestAreaInfo = true;
        }
        this.mShowICCID = this.mCarrierConfigManager.getConfigForSubId(this.mSir.getSubscriptionId()).getBoolean("show_iccid_in_sim_status_bool");
        String rawNumber = this.mTelephonyManager.getLine1Number(this.mSir.getSubscriptionId());
        String formattedNumber = null;
        if (!TextUtils.isEmpty(rawNumber)) {
            formattedNumber = PhoneNumberUtils.formatNumber(rawNumber);
        }
        setSummaryText("number", formattedNumber);
        setSummaryText("imei", this.mPhone.getImei());
        setSummaryText("imei_sv", this.mPhone.getDeviceSvn());
        if (this.mShowICCID) {
            setSummaryText("iccid", this.mTelephonyManager.getSimSerialNumber(this.mSir.getSubscriptionId()));
        } else {
            removePreferenceFromScreen("iccid");
        }
        if (!this.mShowLatestAreaInfo) {
            removePreferenceFromScreen("latest_area_info");
        }
    }

    private void updatePhoneInfos() {
        if (this.mSir != null) {
            Phone phone = PhoneFactory.getPhone(SubscriptionManager.getPhoneId(this.mSir.getSubscriptionId()));
            if (UserManager.get(getContext()).isAdminUser() && SubscriptionManager.isValidSubscriptionId(this.mSir.getSubscriptionId())) {
                if (phone == null) {
                    Log.e("SimStatus", "Unable to locate a phone object for the given Subscription ID.");
                } else {
                    this.mPhone = phone;
                    this.mPhoneStateListener = new PhoneStateListener(this.mSir.getSubscriptionId()) {
                        public void onDataConnectionStateChanged(int state) {
                            SimStatus.this.updateDataState();
                            SimStatus.this.updateNetworkType();
                        }

                        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                            SimStatus.this.updateSignalStrength(signalStrength);
                        }

                        public void onServiceStateChanged(ServiceState serviceState) {
                            SimStatus.this.updateServiceState(serviceState);
                        }
                    };
                }
            }
        }
    }

    private TabSpec buildTabSpec(String tag, String title) {
        return this.mTabHost.newTabSpec(tag).setIndicator(title).setContent(this.mEmptyTabContent);
    }
}
