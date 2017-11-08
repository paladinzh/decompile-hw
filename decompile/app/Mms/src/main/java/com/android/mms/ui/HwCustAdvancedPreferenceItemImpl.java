package com.android.mms.ui;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Log;
import com.android.mms.HwCustMmsConfigImpl;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.transaction.TransactionService;
import com.android.mms.ui.NumberPickerDialog.OnNumberSetListener;
import com.android.mms.util.Recycler;
import com.google.android.gms.R;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.mms.util.StatisticalHelper;
import java.util.Locale;
import libcore.icu.LocaleData;

public class HwCustAdvancedPreferenceItemImpl extends HwCustAdvancedPreferenceItem {
    private static final String AUTO_RETRIEVAL_WITHOUT_ROAMING = "pref_key_mms_auto_retrieval_mms_withoutRoaming";
    private static final String CMAS_SETTINGS = "pref_key_cmas_settings";
    private static final String IS_USER_MODIFIED_MMS_AUTO_RETREIVE = "is_user_modified_auto_retreive";
    private static final String PREFERENCE_AUTO_RETREIVE_MODIFIED = "mms_auto_retreive_modified";
    private static final String TAG = "HwCustAdvancedPreferenceItemImpl";
    private SwitchPreference mAutoDeletePref;
    private SwitchPreference mAutoRetrievalNoRoamingPref;
    private SwitchPreference mAutoRetrievalSingleCardPref;
    private Preference mCmasSettings;
    private Context mContext;
    private Preference mMmsAutoRetrievialPref;
    OnNumberSetListener mMmsLimitListener = new OnNumberSetListener() {
        public void onNumberSet(int limit) {
            if (limit != MmsConfig.getDefaultMMSMessagesPerThread()) {
                StatisticalHelper.incrementReportCount(HwCustAdvancedPreferenceItemImpl.this.mContext, 2074);
            }
            HwCustAdvancedPreferenceItemImpl.this.mMmsRecycler.setMessageLimit(HwCustAdvancedPreferenceItemImpl.this.mContext, limit);
            HwCustAdvancedPreferenceItemImpl.this.setMmsDisplayLimit();
        }
    };
    private Preference mMmsLimitPref;
    private Recycler mMmsRecycler = Recycler.getMmsRecycler();
    private PreferenceScreen mPrefRoot;
    OnNumberSetListener mSmsLimitListener = new OnNumberSetListener() {
        public void onNumberSet(int limit) {
            if (limit != MmsConfig.getDefaultSMSMessagesPerThread()) {
                StatisticalHelper.incrementReportCount(HwCustAdvancedPreferenceItemImpl.this.mContext, 2073);
            }
            HwCustAdvancedPreferenceItemImpl.this.mSmsRecycler.setMessageLimit(HwCustAdvancedPreferenceItemImpl.this.mContext, limit);
            HwCustAdvancedPreferenceItemImpl.this.setSmsDisplayLimit();
        }
    };
    private Preference mSmsLimitPref;
    private Recycler mSmsRecycler = Recycler.getSmsRecycler();

    private static class AutoRetrieveNoRoamingChangeListener implements OnPreferenceChangeListener {
        Context mContext;

        protected AutoRetrieveNoRoamingChangeListener(Context aContext) {
            this.mContext = aContext;
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Boolean isChecked = (Boolean) newValue;
            if (!(preference instanceof SwitchPreference)) {
                return false;
            }
            Editor editor = PreferenceManager.getDefaultSharedPreferences(MmsApp.getApplication()).edit();
            editor.putBoolean(HwCustAdvancedPreferenceItemImpl.AUTO_RETRIEVAL_WITHOUT_ROAMING, isChecked.booleanValue());
            editor.putBoolean("pref_key_mms_retrieval_during_roaming", isChecked.booleanValue());
            editor.putBoolean("pref_key_mms_auto_retrieval", isChecked.booleanValue());
            editor.commit();
            if (isChecked.booleanValue()) {
                HwCustAdvancedPreferenceItemImpl.startMmsDownload(this.mContext);
            }
            return true;
        }
    }

    public void onCustomPreferenceItemClick(Context context, Preference preference) {
        int themeID = context.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
        if (themeID == 0) {
            themeID = 3;
        }
        if (preference == this.mCmasSettings) {
            Intent cellBroadcastIntent = new Intent();
            cellBroadcastIntent.setComponent(new ComponentName("com.android.cellbroadcastreceiver", "com.android.cellbroadcastreceiver.ui.CellBroadcastSettings"));
            try {
                context.startActivity(cellBroadcastIntent);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "onCustomPreferenceItemClick ActivityNotFoundException for CellBroadcastSettings");
            }
        } else if (preference == this.mSmsLimitPref) {
            new NumberPickerDialog(this.mContext, themeID, this.mSmsLimitListener, this.mSmsRecycler.getMessageLimit(this.mContext), this.mSmsRecycler.getMessageMinLimit(), this.mSmsRecycler.getMessageMaxLimit(), R.string.pref_title_sms_delete).show();
        } else if (preference == this.mMmsLimitPref) {
            new NumberPickerDialog(this.mContext, themeID, this.mMmsLimitListener, this.mMmsRecycler.getMessageLimit(this.mContext), this.mMmsRecycler.getMessageMinLimit(), this.mMmsRecycler.getMessageMaxLimit(), R.string.pref_title_mms_delete).show();
        } else if (this.mAutoDeletePref != null && preference.getKey().equals(this.mAutoDeletePref.getKey())) {
            setSmsDisplayLimit();
            setMmsDisplayLimit();
        }
    }

    public void updateCustPreference(Context context, PreferenceScreen prefRoot) {
        this.mPrefRoot = prefRoot;
        this.mContext = context;
        if (HwCustMmsConfigImpl.supportAutoDelete()) {
            this.mPrefRoot.getPreferenceManager().inflateFromResource(context, R.xml.autodelete_prefrence, this.mPrefRoot);
            this.mAutoDeletePref = (SwitchPreference) this.mPrefRoot.findPreference("pref_key_auto_delete");
            this.mSmsLimitPref = this.mPrefRoot.findPreference("pref_key_sms_delete_limit");
            this.mMmsLimitPref = this.mPrefRoot.findPreference("pref_key_mms_delete_limit");
            setSmsDisplayLimit();
            setMmsDisplayLimit();
        }
        if (!HwCustMmsConfigImpl.isHideRetrievalWithoutRoaming()) {
            this.mPrefRoot.getPreferenceManager().inflateFromResource(context, R.xml.prefrence_retrieve_noroaming, this.mPrefRoot);
            this.mAutoRetrievalNoRoamingPref = (SwitchPreference) this.mPrefRoot.findPreference(AUTO_RETRIEVAL_WITHOUT_ROAMING);
            this.mAutoRetrievalNoRoamingPref.setOnPreferenceChangeListener(new AutoRetrieveNoRoamingChangeListener(this.mContext));
        }
        if (HwCustMmsConfigImpl.isEnableCmasSettings()) {
            this.mPrefRoot.getPreferenceManager().inflateFromResource(context, R.xml.cmas_prefrence, this.mPrefRoot);
            this.mCmasSettings = this.mPrefRoot.findPreference(CMAS_SETTINGS);
        }
        this.mAutoRetrievalSingleCardPref = (SwitchPreference) this.mPrefRoot.findPreference("pref_key_mms_retrieval_during_roaming");
        if (this.mAutoRetrievalSingleCardPref != null && HwCustMmsConfigImpl.isHideMmsAutoRetrievalRoaming()) {
            this.mPrefRoot.removePreference(this.mAutoRetrievalSingleCardPref);
        }
        this.mMmsAutoRetrievialPref = this.mPrefRoot.findPreference("pref_key_mms_auto_retrieval_mms");
        if (this.mMmsAutoRetrievialPref != null && HwCustMmsConfigImpl.isHideMmsAutoRetrieval()) {
            this.mPrefRoot.removePreference(this.mMmsAutoRetrievialPref);
        }
    }

    public boolean getEnableShowSmscNotEdit() {
        return HwCustMmsConfigImpl.getEnableShowSmscNotEdit();
    }

    public boolean getCustMccmncEnableShowSmscNotEdit(int subID) {
        String custMccmnc = HwCustMmsConfigImpl.getCustMccmncForShowSmscNotEdit();
        if (!TextUtils.isEmpty(custMccmnc)) {
            String currentMccMnc = getOperatorMccMnc(subID);
            if (!TextUtils.isEmpty(currentMccMnc)) {
                String[] custMccmncAfterSplit = custMccmnc.split(",");
                for (String startsWith : custMccmncAfterSplit) {
                    if (currentMccMnc.startsWith(startsWith)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void setSummaryAndDisableSmsCenterAddrPref(final Preference preference, final int subID) {
        if (preference != null) {
            preference.setEnabled(false);
            preference.setWidgetLayoutResource(0);
            new AsyncTask<Void, Void, String>() {
                protected String doInBackground(Void... params) {
                    if (HwCustMmsConfigImpl.getCustSMSCAddress() != null) {
                        return HwCustMmsConfigImpl.getCustSMSCAddress();
                    }
                    return HwCustAdvancedPreferenceItemImpl.this.getSmsCenterAddrOnSubscription(subID);
                }

                protected void onPostExecute(String result) {
                    if (!TextUtils.isEmpty(result)) {
                        preference.setSummary(result);
                    }
                }
            }.executeOnExecutor(ThreadEx.getDefaultExecutor(), new Void[0]);
        }
    }

    public String getSmsCenterAddrOnSubscription(int subID) {
        String smsCenterAddr = MessageUtils.getSmsAddressBySubID(subID);
        if (!TextUtils.isEmpty(smsCenterAddr)) {
            String[] strArray = smsCenterAddr.split("\"");
            if (strArray.length > 1) {
                smsCenterAddr = strArray[1];
            }
        }
        if (!TextUtils.isEmpty(smsCenterAddr) || MmsConfig.getSMSCAddress() == null) {
            return smsCenterAddr;
        }
        return MmsConfig.getSMSCAddress();
    }

    public String getOperatorMccMnc(int subID) {
        return HwCustMessageUtilsImpl.getOperatorMccMnc(subID);
    }

    public boolean getEnableCotaFeature() {
        return HwCustMmsConfigImpl.getEnableCotaFeature();
    }

    public void setAlwaysTxRxMmsClick(Context context) {
        System.putInt(context.getContentResolver(), "always_send_recv_mms_click", 1);
    }

    public void setUserModifiedAutoRetreiveFlag(Context context) {
        if (HwCustPreferenceUtilsImpl.IS_SPRINT) {
            Editor edit = context.getSharedPreferences(PREFERENCE_AUTO_RETREIVE_MODIFIED, 0).edit();
            edit.putBoolean(IS_USER_MODIFIED_MMS_AUTO_RETREIVE, Boolean.TRUE.booleanValue());
            edit.commit();
        }
    }

    private static void startMmsDownload(Context aContext) {
        TransactionService.startMe(aContext, "android.intent.action.ACTION_ENABLE_AUTO_RETRIEVE");
    }

    private void setSmsDisplayLimit() {
        this.mSmsLimitPref.setSummary(this.mContext.getString(R.string.pref_summary_delete_limit, new Object[]{localizeMsgNumbers(this.mSmsRecycler.getMessageLimit(this.mContext))}));
    }

    private void setMmsDisplayLimit() {
        this.mMmsLimitPref.setSummary(this.mContext.getString(R.string.pref_summary_delete_limit, new Object[]{localizeMsgNumbers(this.mMmsRecycler.getMessageLimit(this.mContext))}));
    }

    private static String localizeMsgNumbers(int messageLimitValue) {
        char sZeroDigit = LocaleData.get(Locale.getDefault()).zeroDigit;
        String messageLimitString = String.valueOf(messageLimitValue);
        if ('0' == sZeroDigit || !MessageUtils.isNeedLayoutRtl()) {
            return messageLimitString;
        }
        int length = messageLimitString.length();
        int offsetToLocalizedNumbers = sZeroDigit - 48;
        StringBuilder result = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            char ch = messageLimitString.charAt(index);
            if (ch >= '0' && ch <= '9') {
                ch = (char) (ch + offsetToLocalizedNumbers);
            }
            result.append(ch);
        }
        return result.toString();
    }
}
