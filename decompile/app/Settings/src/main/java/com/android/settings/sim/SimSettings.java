package com.android.settings.sim;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.RestrictedSettingsFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import java.util.ArrayList;
import java.util.List;

public class SimSettings extends RestrictedSettingsFragment implements Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            ArrayList<SearchIndexableResource> result = new ArrayList();
            if (Utils.showSimCardTile(context)) {
                SearchIndexableResource sir = new SearchIndexableResource(context);
                sir.xmlResId = 2131230890;
                result.add(sir);
            }
            return result;
        }
    };
    private List<SubscriptionInfo> mAvailableSubInfos = null;
    private int[] mCallState = new int[this.mPhoneCount];
    private Context mContext;
    private int mNumSlots;
    private final OnSubscriptionsChangedListener mOnSubscriptionsChangeListener = new OnSubscriptionsChangedListener() {
        public void onSubscriptionsChanged() {
            SimSettings.this.updateSubscriptions();
        }
    };
    private int mPhoneCount = TelephonyManager.getDefault().getPhoneCount();
    private PhoneStateListener[] mPhoneStateListener = new PhoneStateListener[this.mPhoneCount];
    private List<SubscriptionInfo> mSelectableSubInfos = null;
    private PreferenceScreen mSimCards = null;
    private List<SubscriptionInfo> mSubInfoList = null;
    private SubscriptionManager mSubscriptionManager;

    private class SimPreference extends Preference {
        Context mContext;
        private int mSlotId;
        private SubscriptionInfo mSubInfoRecord;

        public SimPreference(Context context, SubscriptionInfo subInfoRecord, int slotId) {
            super(context);
            this.mContext = context;
            this.mSubInfoRecord = subInfoRecord;
            this.mSlotId = slotId;
            setKey("sim" + this.mSlotId);
            update();
        }

        public void update() {
            Resources res = this.mContext.getResources();
            setTitle(String.format(this.mContext.getResources().getString(2131626614), new Object[]{Integer.valueOf(this.mSlotId + 1)}));
            if (this.mSubInfoRecord != null) {
                if (TextUtils.isEmpty(SimSettings.this.getPhoneNumber(this.mSubInfoRecord))) {
                    setSummary(this.mSubInfoRecord.getDisplayName());
                } else {
                    setSummary(this.mSubInfoRecord.getDisplayName() + " - " + PhoneNumberUtils.createTtsSpannable(SimSettings.this.getPhoneNumber(this.mSubInfoRecord)));
                    setEnabled(true);
                }
                setIcon(new BitmapDrawable(res, this.mSubInfoRecord.createIconBitmap(this.mContext)));
                return;
            }
            setSummary(2131626611);
            setFragment(null);
            setEnabled(false);
        }

        private int getSlotId() {
            return this.mSlotId;
        }
    }

    public SimSettings() {
        super("no_config_sim");
    }

    protected int getMetricsCategory() {
        return 88;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mContext = getActivity();
        this.mSubscriptionManager = SubscriptionManager.from(getActivity());
        TelephonyManager tm = (TelephonyManager) getActivity().getSystemService("phone");
        addPreferencesFromResource(2131230890);
        this.mNumSlots = tm.getSimCount();
        this.mSimCards = (PreferenceScreen) findPreference("sim_cards");
        this.mAvailableSubInfos = new ArrayList(this.mNumSlots);
        this.mSelectableSubInfos = new ArrayList();
        SimSelectNotification.cancelNotification(getActivity());
    }

    private void updateSubscriptions() {
        int i;
        this.mSubInfoList = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        for (i = 0; i < this.mNumSlots; i++) {
            Preference pref = this.mSimCards.findPreference("sim" + i);
            if (pref instanceof SimPreference) {
                this.mSimCards.removePreference(pref);
            }
        }
        this.mAvailableSubInfos.clear();
        this.mSelectableSubInfos.clear();
        for (i = 0; i < this.mNumSlots; i++) {
            SubscriptionInfo sir = this.mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(i);
            SimPreference simPreference = new SimPreference(getPrefContext(), sir, i);
            simPreference.setOrder(i - this.mNumSlots);
            this.mSimCards.addPreference(simPreference);
            this.mAvailableSubInfos.add(sir);
            if (sir != null) {
                this.mSelectableSubInfos.add(sir);
            }
        }
        updateAllOptions();
    }

    private void updateAllOptions() {
        updateSimSlotValues();
        updateActivitesCategory();
    }

    private void updateSimSlotValues() {
        int prefSize = this.mSimCards.getPreferenceCount();
        for (int i = 0; i < prefSize; i++) {
            Preference pref = this.mSimCards.getPreference(i);
            if (pref instanceof SimPreference) {
                ((SimPreference) pref).update();
            }
        }
    }

    private void updateActivitesCategory() {
        updateCellularDataValues();
        updateCallValues();
        updateSmsValues();
    }

    private void updateSmsValues() {
        boolean z = true;
        Preference simPref = findPreference("sim_sms");
        SubscriptionInfo sir = this.mSubscriptionManager.getDefaultSmsSubscriptionInfo();
        simPref.setTitle(2131624586);
        if (sir != null) {
            simPref.setSummary(sir.getDisplayName());
            if (this.mSelectableSubInfos.size() <= 1) {
                z = false;
            }
            simPref.setEnabled(z);
        } else if (sir == null) {
            simPref.setSummary(2131626635);
            if (this.mSelectableSubInfos.size() < 1) {
                z = false;
            }
            simPref.setEnabled(z);
        }
    }

    private void updateCellularDataValues() {
        boolean z = false;
        Preference simPref = findPreference("sim_cellular_data");
        SubscriptionInfo sir = this.mSubscriptionManager.getDefaultDataSubscriptionInfo();
        simPref.setTitle(2131624584);
        boolean callStateIdle = isCallStateIdle();
        boolean ecbMode = SystemProperties.getBoolean("ril.cdma.inecmmode", false);
        if (sir != null) {
            simPref.setSummary(sir.getDisplayName());
            if (this.mSelectableSubInfos.size() > 1 && callStateIdle && !ecbMode) {
                z = true;
            }
            simPref.setEnabled(z);
        } else if (sir == null) {
            simPref.setSummary(2131626635);
            if (this.mSelectableSubInfos.size() >= 1 && callStateIdle && !ecbMode) {
                z = true;
            }
            simPref.setEnabled(z);
        }
    }

    private void updateCallValues() {
        CharSequence string;
        boolean z;
        Preference simPref = findPreference("sim_calls");
        TelecomManager telecomManager = TelecomManager.from(this.mContext);
        PhoneAccountHandle phoneAccount = telecomManager.getUserSelectedOutgoingPhoneAccount();
        List<PhoneAccountHandle> allPhoneAccounts = telecomManager.getCallCapablePhoneAccounts();
        simPref.setTitle(2131624585);
        if (phoneAccount == null) {
            string = this.mContext.getResources().getString(2131626634);
        } else {
            String str = (String) telecomManager.getPhoneAccount(phoneAccount).getLabel();
        }
        simPref.setSummary(string);
        if (allPhoneAccounts.size() > 1) {
            z = true;
        } else {
            z = false;
        }
        simPref.setEnabled(z);
    }

    public void onResume() {
        super.onResume();
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangeListener);
        updateSubscriptions();
        TelephonyManager tm = (TelephonyManager) getActivity().getSystemService("phone");
        if (this.mSelectableSubInfos.size() > 1) {
            Log.d("SimSettings", "Register for call state change");
            for (int i = 0; i < this.mPhoneCount; i++) {
                tm.listen(getPhoneStateListener(i, ((SubscriptionInfo) this.mSelectableSubInfos.get(i)).getSubscriptionId()), 32);
            }
        }
    }

    public void onPause() {
        super.onPause();
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mOnSubscriptionsChangeListener);
        TelephonyManager tm = (TelephonyManager) getSystemService("phone");
        for (int i = 0; i < this.mPhoneCount; i++) {
            if (this.mPhoneStateListener[i] != null) {
                tm.listen(this.mPhoneStateListener[i], 0);
                this.mPhoneStateListener[i] = null;
            }
        }
    }

    private PhoneStateListener getPhoneStateListener(final int phoneId, int subId) {
        int i = phoneId;
        this.mPhoneStateListener[phoneId] = new PhoneStateListener(subId) {
            public void onCallStateChanged(int state, String incomingNumber) {
                SimSettings.this.mCallState[phoneId] = state;
                SimSettings.this.updateCellularDataValues();
            }
        };
        return this.mPhoneStateListener[phoneId];
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        Context context = this.mContext;
        new Intent(context, SimDialogActivity.class).addFlags(268435456);
        if (preference instanceof SimPreference) {
            Intent newIntent = new Intent(context, SimPreferenceDialog.class);
            newIntent.putExtra("slot_id", ((SimPreference) preference).getSlotId());
            startActivity(newIntent);
        }
        return true;
    }

    private String getPhoneNumber(SubscriptionInfo info) {
        return ((TelephonyManager) this.mContext.getSystemService("phone")).getLine1Number(info.getSubscriptionId());
    }

    private boolean isCallStateIdle() {
        boolean callStateIdle = true;
        for (int i : this.mCallState) {
            if (i != 0) {
                callStateIdle = false;
            }
        }
        Log.d("SimSettings", "isCallStateIdle " + callStateIdle);
        return callStateIdle;
    }
}
