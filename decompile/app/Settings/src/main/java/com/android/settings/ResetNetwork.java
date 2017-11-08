package com.android.settings;

import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import com.android.settings.deviceinfo.RadioPreference;
import com.android.settings.widget.ResetNetworkPreference;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResetNetwork extends OptionsMenuFragment implements OnPreferenceChangeListener {
    private View mContentView;
    private Button mInitiateButton;
    private final OnClickListener mInitiateListener = new OnClickListener() {
        public void onClick(View v) {
            if (!ResetNetwork.this.runKeyguardConfirmation(55)) {
                ResetNetwork.this.showFinalConfirmation();
            }
        }
    };
    private Map<String, Integer> mPrefSubMap = new HashMap();
    private String mSelectedPrefKey;
    private List<SubscriptionInfo> mSubscriptions;

    private boolean runKeyguardConfirmation(int request) {
        return new ChooseLockSettingsHelper(getActivity(), this).launchConfirmationActivity(request, getActivity().getResources().getText(2131628397));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 55) {
            if (resultCode == -1) {
                showFinalConfirmation();
            } else {
                initializeState();
            }
        }
    }

    private void showFinalConfirmation() {
        Bundle args = new Bundle();
        if (this.mSubscriptions != null && this.mSubscriptions.size() > 0) {
            args.putInt("subscription", ((SubscriptionInfo) this.mSubscriptions.get(((Integer) this.mPrefSubMap.get(this.mSelectedPrefKey)).intValue())).getSubscriptionId());
        }
        ((SettingsActivity) getActivity()).startPreferencePanel(ResetNetworkConfirm.class.getName(), args, 2131628397, null, null, 0);
    }

    private static String getSimcardPrefKey(int index) {
        return String.format("simcard_pref_%d", new Object[]{Integer.valueOf(index)});
    }

    private void setRadioChecked(RadioPreference pref, boolean checked) {
        OnPreferenceChangeListener listener = pref.getOnPreferenceChangeListener();
        if (listener != null) {
            pref.setOnPreferenceChangeListener(null);
        }
        pref.setChecked(checked);
        if (listener != null) {
            pref.setOnPreferenceChangeListener(listener);
        }
    }

    private void initializeState() {
        this.mSubscriptions = SubscriptionManager.from(getActivity()).getActiveSubscriptionInfoList();
        PreferenceCategory simcardCategory = (PreferenceCategory) findPreference("reset_network_simcard_category");
        if (this.mSubscriptions != null && this.mSubscriptions.size() > 0) {
            int defaultSubscription = SubscriptionManager.getDefaultDataSubscriptionId();
            if (!SubscriptionManager.isUsableSubIdValue(defaultSubscription)) {
                defaultSubscription = SubscriptionManager.getDefaultVoiceSubscriptionId();
            }
            if (!SubscriptionManager.isUsableSubIdValue(defaultSubscription)) {
                defaultSubscription = SubscriptionManager.getDefaultSmsSubscriptionId();
            }
            if (!SubscriptionManager.isUsableSubIdValue(defaultSubscription)) {
                defaultSubscription = SubscriptionManager.getDefaultSubscriptionId();
            }
            int selectedIndex = 0;
            int size = this.mSubscriptions.size();
            List<String> subscriptionNames = new ArrayList();
            for (SubscriptionInfo record : this.mSubscriptions) {
                if (record.getSubscriptionId() == defaultSubscription) {
                    selectedIndex = subscriptionNames.size();
                }
                String name = record.getCarrierName().toString();
                if (TextUtils.isEmpty(name)) {
                    name = record.getNumber();
                }
                if (TextUtils.isEmpty(name)) {
                    name = record.getDisplayName().toString();
                }
                if (TextUtils.isEmpty(name)) {
                    name = String.format("MCC:%s MNC:%s Slot:%s Id:%s", new Object[]{Integer.valueOf(record.getMcc()), Integer.valueOf(record.getMnc()), Integer.valueOf(record.getSimSlotIndex()), Integer.valueOf(record.getSubscriptionId())});
                }
                subscriptionNames.add(name);
            }
            if (simcardCategory != null) {
                simcardCategory.removeAll();
                simcardCategory.setOrderingAsAdded(true);
                for (int index = 0; index < size; index++) {
                    String prefTitle = getString(2131628159, new Object[]{Integer.valueOf(index + 1), subscriptionNames.get(index)});
                    if (size == 1) {
                        if (prefTitle.endsWith(" 1") || prefTitle.endsWith(" 2")) {
                            prefTitle = prefTitle.substring(0, prefTitle.length() - 2);
                        }
                        prefTitle = prefTitle.replaceFirst("\\d+", "");
                    }
                    Log.d("ResetNetwork", "Add preference = " + prefTitle);
                    RadioPreference pref = new RadioPreference(getActivity());
                    String key = getSimcardPrefKey(index);
                    pref.setWidgetLayoutResource(2130969046);
                    pref.setLayoutResource(2130968964);
                    pref.setKey(key);
                    pref.setTitle((CharSequence) prefTitle);
                    pref.setOnPreferenceChangeListener(this);
                    if (index == selectedIndex) {
                        pref.setChecked(true);
                        this.mSelectedPrefKey = key;
                    } else {
                        pref.setChecked(false);
                    }
                    simcardCategory.addPreference(pref);
                    this.mPrefSubMap.put(key, Integer.valueOf(index));
                }
            }
        } else if (simcardCategory != null) {
            getPreferenceScreen().removePreference(simcardCategory);
        }
        ResetNetworkPreference descPref = (ResetNetworkPreference) findPreference("reset_network_desc");
        if (descPref != null) {
            descPref.setSelectable(false);
            if (Utils.isWifiOnly(getActivity())) {
                descPref.setSummaryTextView(2131628656);
            }
        }
        this.mInitiateButton = (Button) this.mContentView.findViewById(2131887083);
        this.mInitiateButton.setOnClickListener(this.mInitiateListener);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        UserManager um = UserManager.get(getActivity());
        EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(getActivity(), "no_network_reset", UserHandle.myUserId());
        if (!um.isAdminUser() || RestrictedLockUtils.hasBaseUserRestriction(getActivity(), "no_network_reset", UserHandle.myUserId())) {
            return inflater.inflate(2130968876, null);
        }
        if (admin != null) {
            View view = inflater.inflate(2130968617, null);
            ShowAdminSupportDetailsDialog.setAdminSupportDetails(getActivity(), view, admin, false);
            view.setVisibility(0);
            return view;
        }
        this.mContentView = inflater.inflate(2130969056, (ViewGroup) root, true);
        initializeState();
        return this.mContentView;
    }

    protected int getMetricsCategory() {
        return 83;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230851);
    }

    public boolean onPreferenceChange(Preference pref, Object newValue) {
        PreferenceCategory simcardCategory = (PreferenceCategory) findPreference("reset_network_simcard_category");
        if (simcardCategory == null) {
            return true;
        }
        for (int index = 0; index < simcardCategory.getPreferenceCount(); index++) {
            RadioPreference curPref = (RadioPreference) simcardCategory.getPreference(index);
            if (!curPref.getKey().equals(pref.getKey())) {
                setRadioChecked(curPref, false);
            } else if (curPref.isChecked()) {
                return false;
            } else {
                setRadioChecked(curPref, true);
                this.mSelectedPrefKey = curPref.getKey();
            }
        }
        return true;
    }
}
