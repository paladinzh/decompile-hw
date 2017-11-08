package com.android.mms.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings.System;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.xy.sms.sdk.net.NewXyHttpRunnable;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.transaction.TransactionService;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.ui.HwPreferenceFragment;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.HwSpecialUtils;
import com.huawei.mms.util.HwTelephony;
import com.huawei.mms.util.HwTelephony.HwSimStateListener;
import com.huawei.mms.util.StatisticalHelper;
import java.text.NumberFormat;

public class AdvancedPreferenceFragment extends HwPreferenceFragment {
    private static String sMmsExpireDefaultDay = NewXyHttpRunnable.ERROR_CODE_SERVICE_ERR;
    private OnPreferenceChangeListener autoRetrievalPrefListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Boolean isChecked = (Boolean) newValue;
            if (!(preference instanceof SwitchPreference)) {
                return false;
            }
            final SwitchPreference pref = (SwitchPreference) preference;
            if (isChecked.booleanValue()) {
                new Builder(AdvancedPreferenceFragment.this.mActivity).setIcon(17301543).setTitle(R.string.mms_remind_title).setCancelable(true).setMessage(R.string.mms_remind_roming_download_body).setPositiveButton(R.string.mms_enable, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        pref.setChecked(true);
                        if (AdvancedPreferenceFragment.this.mHwCustAdvancedPreferenceItem != null) {
                            AdvancedPreferenceFragment.this.mHwCustAdvancedPreferenceItem.setUserModifiedAutoRetreiveFlag(AdvancedPreferenceFragment.this.mActivity);
                        }
                        dialog.dismiss();
                        AdvancedPreferenceFragment.this.setMultiSimRoamingAutoRetrieveValue(pref, true);
                    }
                }).setNegativeButton(R.string.no, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        pref.setChecked(false);
                        dialog.dismiss();
                        AdvancedPreferenceFragment.this.setMultiSimRoamingAutoRetrieveValue(pref, false);
                    }
                }).setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        pref.setChecked(false);
                        dialog.dismiss();
                        AdvancedPreferenceFragment.this.setMultiSimRoamingAutoRetrieveValue(pref, false);
                    }
                }).show();
            } else {
                AdvancedPreferenceFragment.this.setMultiSimRoamingAutoRetrieveValue(pref, isChecked.booleanValue());
                if (AdvancedPreferenceFragment.this.mHwCustAdvancedPreferenceItem != null) {
                    AdvancedPreferenceFragment.this.mHwCustAdvancedPreferenceItem.setUserModifiedAutoRetreiveFlag(AdvancedPreferenceFragment.this.mActivity);
                }
            }
            return true;
        }
    };
    private OnPreferenceChangeListener expireModePrefListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Editor editor = PreferenceManager.getDefaultSharedPreferences(AdvancedPreferenceFragment.this.mActivity).edit();
            if (MmsConfig.isMmsExpiryMaxEnable()) {
                AdvancedPreferenceFragment.this.mExpireModeWithMaxPref.setValueIndex(AdvancedPreferenceFragment.this.mExpireModeWithMaxPref.findIndexOfValue((String) newValue));
                if ("2".equals((String) newValue)) {
                    AdvancedPreferenceFragment.sMmsExpireDefaultDay = "2";
                    AdvancedPreferenceFragment.this.mExpireModeWithMaxPref.setSummary(R.string.pref_summary_mms_expire_mode_2_days);
                } else if ("7".equals((String) newValue)) {
                    AdvancedPreferenceFragment.sMmsExpireDefaultDay = "7";
                    AdvancedPreferenceFragment.this.mExpireModeWithMaxPref.setSummary(R.string.pref_summary_mms_expire_mode_7_days);
                } else {
                    AdvancedPreferenceFragment.sMmsExpireDefaultDay = "0";
                    AdvancedPreferenceFragment.this.mExpireModeWithMaxPref.setSummary(R.string.pref_summary_mms_expire_mode_max);
                }
            } else {
                AdvancedPreferenceFragment.this.mExpireModePref.setValueIndex(AdvancedPreferenceFragment.this.mExpireModePref.findIndexOfValue((String) newValue));
                if ("1".equals((String) newValue)) {
                    AdvancedPreferenceFragment.sMmsExpireDefaultDay = "1";
                    AdvancedPreferenceFragment.this.mExpireModePref.setSummary(R.string.pref_summary_mms_expire_mode_slideshowactivity);
                } else if ("2".equals((String) newValue)) {
                    AdvancedPreferenceFragment.sMmsExpireDefaultDay = "2";
                    AdvancedPreferenceFragment.this.mExpireModePref.setSummary(R.string.pref_summary_mms_expire_mode_slideshowactivity2);
                } else {
                    AdvancedPreferenceFragment.sMmsExpireDefaultDay = NewXyHttpRunnable.ERROR_CODE_SERVICE_ERR;
                    AdvancedPreferenceFragment.this.mExpireModePref.setSummary(R.string.pref_summary_mms_expire_mode_slideshowactivity3);
                }
            }
            editor.putString("expireValue", AdvancedPreferenceFragment.sMmsExpireDefaultDay);
            editor.commit();
            return true;
        }
    };
    private Activity mActivity;
    private HwPreference mAddSignature;
    private HwPreference mAlwaysReceiveAndSendMms;
    private AlertDialog mAlwaysReceiveAndSendMmsDlg;
    private SwitchPreference mAutoRetrievalCard1Pref;
    private SwitchPreference mAutoRetrievalCard2Pref;
    private SwitchPreference mAutoRetrievalSingleCardPref;
    private AlertDialog mAutoRetrieviaDlg;
    private Preference mCommonPhrasePref;
    private OnPreferenceChangeListener mCommonSwitchChangeListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (!(preference instanceof SwitchPreference) || !(newValue instanceof Boolean)) {
                return true;
            }
            Boolean isChecked = (Boolean) newValue;
            if (preference == AdvancedPreferenceFragment.this.mWapPushMgrPref) {
                StatisticalHelper.reportTwoStateEvent(AdvancedPreferenceFragment.this.mActivity, 2080, isChecked.booleanValue());
            } else if (preference == AdvancedPreferenceFragment.this.mVerifitionSmsProtectPref) {
                PreferenceUtils.setVerifitionSmsProtectEnable(AdvancedPreferenceFragment.this.mActivity, isChecked.booleanValue());
                StatisticalHelper.reportTwoStateEvent(AdvancedPreferenceFragment.this.mActivity, 2165, isChecked.booleanValue());
            }
            return true;
        }
    };
    private ListPreference mExpireModePref;
    private ListPreference mExpireModeWithMaxPref;
    private View mFooterView = null;
    private HwCustAdvancedPreferenceItem mHwCustAdvancedPreferenceItem = ((HwCustAdvancedPreferenceItem) HwCustUtils.createObj(HwCustAdvancedPreferenceItem.class, new Object[0]));
    private boolean mIsSmsEnabled;
    private PhoneStateListener mListener = null;
    private Preference mManageSim2Pref;
    private Preference mManageSimPref;
    private Preference mMmsAutoReceivePref;
    private Preference mMmsGroupMmsPref;
    private Preference mMmsReadReportPref;
    private Preference mMmsReplyReadReportPref;
    private int mMutilSimMode = 0;
    private int mOldMutilSimMode = 0;
    private PreferenceScreen mPrefRoot;
    private ListPreference mSaveModePref;
    private ListPreference mSaveModePrefSub0;
    private ListPreference mSaveModePrefSub1;
    private String[] mSaveModeSummary;
    BroadcastReceiver mSingleSimChangeReceiver = null;
    private Preference mSmc1Pref;
    private Preference mSmc2Pref;
    private Preference mSmsCenterNumberPref;
    private SwitchPreference mSmsOptimizationCharacters;
    private TelephonyManager mTM = null;
    private SwitchPreference mVerifitionSmsProtectPref;
    private SwitchPreference mWapPushMgrPref;
    private OnPreferenceChangeListener smsCenterNumberPrefListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String smscAddress = (String) newValue;
            if (MessageUtils.setSmscAddr("\"" + smscAddress + "\"")) {
                AdvancedPreferenceFragment.this.mSmsCenterNumberPref.setSummary(smscAddress);
                return true;
            }
            Toast.makeText(AdvancedPreferenceFragment.this.mActivity, R.string.sms_center_set_fail_Toast, 0).show();
            return false;
        }
    };
    private OnPreferenceChangeListener smsOptimizationCharacterListener = new SmsOptimizationCharacterChangeListener();

    private class AlwaysReceiveAndSendMmsAdapter extends BaseAdapter {
        private Data[] mDatas;
        private ViewHolder mHolder;

        private class Data {
            public CharSequence mTitle;

            private Data() {
            }
        }

        private class ViewHolder {
            private TextView mListSummary;
            private TextView mListTitle;
            private RadioButton mRadio;

            private ViewHolder() {
            }
        }

        public AlwaysReceiveAndSendMmsAdapter() {
            CharSequence[] titles = AdvancedPreferenceFragment.this.getResources().getStringArray(R.array.prefEntries_mms_always_receive_and_send_mms);
            this.mDatas = new Data[titles.length];
            for (int i = 0; i < titles.length; i++) {
                Data data = new Data();
                data.mTitle = titles[i];
                this.mDatas[i] = data;
            }
        }

        public Object getItem(int position) {
            return this.mDatas[position];
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public int getCount() {
            return this.mDatas.length;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            String autoRetrievalAnyTime = AdvancedPreferenceFragment.this.getString(R.string.mms_auto_retrieval_any_time);
            if (convertView == null) {
                convertView = ((LayoutInflater) AdvancedPreferenceFragment.this.mActivity.getSystemService("layout_inflater")).inflate(R.layout.mms_list_preference_item, null);
                this.mHolder = new ViewHolder();
                this.mHolder.mRadio = (RadioButton) convertView.findViewById(R.id.list_radio);
                this.mHolder.mListTitle = (TextView) convertView.findViewById(R.id.list_title);
                this.mHolder.mListSummary = (TextView) convertView.findViewById(R.id.list_summary);
                convertView.setTag(this.mHolder);
            } else {
                this.mHolder = (ViewHolder) convertView.getTag();
            }
            if (PreferenceManager.getDefaultSharedPreferences(AdvancedPreferenceFragment.this.mActivity).getInt("alwaysAllowMms", MmsConfig.getDefaultAlwaysAllowMms()) != position) {
                this.mHolder.mRadio.setChecked(false);
            } else {
                this.mHolder.mRadio.setChecked(true);
            }
            if (this.mDatas[position].mTitle.toString().equals(autoRetrievalAnyTime)) {
                this.mHolder.mListTitle.setText(this.mDatas[position].mTitle);
                this.mHolder.mListSummary.setText(AdvancedPreferenceFragment.this.getString(R.string.auto_retrieval_fee));
                this.mHolder.mListSummary.setVisibility(0);
            } else {
                this.mHolder.mListTitle.setText(this.mDatas[position].mTitle);
                this.mHolder.mListSummary.setVisibility(8);
            }
            return convertView;
        }
    }

    private class AutoRetrievialAdapter extends BaseAdapter {
        private Data[] mDatas;
        private ViewHolder mHolder;

        private class Data {
            public CharSequence mTitle;

            private Data() {
            }
        }

        private class ViewHolder {
            private TextView mListSummary;
            private TextView mListTitle;
            private RadioButton mRadio;

            private ViewHolder() {
            }
        }

        public AutoRetrievialAdapter() {
            CharSequence[] titles = AdvancedPreferenceFragment.this.getResources().getStringArray(R.array.prefEntries_mms_auto_receive);
            this.mDatas = new Data[titles.length];
            for (int i = 0; i < titles.length; i++) {
                Data data = new Data();
                data.mTitle = titles[i];
                this.mDatas[i] = data;
            }
        }

        public int getCount() {
            return this.mDatas.length;
        }

        public Object getItem(int position) {
            return this.mDatas[position];
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            String autoRetrievalAnyTime = AdvancedPreferenceFragment.this.getString(R.string.mms_auto_retrieval_any_time);
            if (convertView == null) {
                convertView = ((LayoutInflater) AdvancedPreferenceFragment.this.mActivity.getSystemService("layout_inflater")).inflate(R.layout.mms_list_preference_item, null);
                this.mHolder = new ViewHolder();
                this.mHolder.mListTitle = (TextView) convertView.findViewById(R.id.list_title);
                this.mHolder.mListSummary = (TextView) convertView.findViewById(R.id.list_summary);
                this.mHolder.mRadio = (RadioButton) convertView.findViewById(R.id.list_radio);
                convertView.setTag(this.mHolder);
            } else {
                this.mHolder = (ViewHolder) convertView.getTag();
            }
            if (PreferenceManager.getDefaultSharedPreferences(AdvancedPreferenceFragment.this.mActivity).getInt("autoReceiveMms", MmsConfig.getDefaultAutoRetrievalMms()) != position) {
                this.mHolder.mRadio.setChecked(false);
            } else {
                this.mHolder.mRadio.setChecked(true);
            }
            if (this.mDatas[position].mTitle.toString().equals(autoRetrievalAnyTime)) {
                this.mHolder.mListSummary.setText(AdvancedPreferenceFragment.this.getString(R.string.auto_retrieval_fee));
                this.mHolder.mListTitle.setText(this.mDatas[position].mTitle);
                this.mHolder.mListSummary.setVisibility(0);
            } else {
                this.mHolder.mListTitle.setText(this.mDatas[position].mTitle);
                this.mHolder.mListSummary.setVisibility(8);
            }
            return convertView;
        }
    }

    private class ServiceStateListener extends PhoneStateListener {
        private final Context mContext;

        ServiceStateListener(Context context) {
            this.mContext = context;
        }

        public void onServiceStateChanged(ServiceState ss) {
            if (ss.getState() == 3) {
                if (AdvancedPreferenceFragment.this.isMultiSimState()) {
                    if (!(AdvancedPreferenceFragment.this.mManageSimPref == null || 1 == MessageUtils.getIccCardStatus(0))) {
                        AdvancedPreferenceFragment.this.mManageSimPref.setEnabled(false);
                    }
                    if (!(AdvancedPreferenceFragment.this.mManageSim2Pref == null || 1 == MessageUtils.getIccCardStatus(1))) {
                        AdvancedPreferenceFragment.this.mManageSim2Pref.setEnabled(false);
                    }
                } else if (MessageUtils.isMultiSimEnabled()) {
                    if (!(AdvancedPreferenceFragment.this.mManageSimPref == null || 1 == MessageUtils.getIccCardStatus(0) || 1 == MessageUtils.getIccCardStatus(1))) {
                        AdvancedPreferenceFragment.this.mManageSimPref.setEnabled(false);
                    }
                } else if (!(AdvancedPreferenceFragment.this.mManageSimPref == null || 1 == MessageUtils.getIccCardStatus())) {
                    AdvancedPreferenceFragment.this.mManageSimPref.setEnabled(false);
                }
            } else if (AdvancedPreferenceFragment.this.isMultiSimState()) {
                if (1 == MessageUtils.getIccCardStatus(0) && AdvancedPreferenceFragment.this.mManageSimPref != null) {
                    AdvancedPreferenceFragment.this.mManageSimPref.setEnabled(true);
                }
                if (1 == MessageUtils.getIccCardStatus(1) && AdvancedPreferenceFragment.this.mManageSim2Pref != null) {
                    AdvancedPreferenceFragment.this.mManageSim2Pref.setEnabled(true);
                }
            } else if (MessageUtils.isMultiSimEnabled()) {
                if ((1 == MessageUtils.getIccCardStatus(0) || 1 == MessageUtils.getIccCardStatus(1)) && AdvancedPreferenceFragment.this.mManageSimPref != null) {
                    AdvancedPreferenceFragment.this.mManageSimPref.setEnabled(true);
                }
            } else if (1 == MessageUtils.getIccCardStatus() && AdvancedPreferenceFragment.this.mManageSimPref != null) {
                AdvancedPreferenceFragment.this.mManageSimPref.setEnabled(true);
            }
            AdvancedPreferenceFragment.this.setSmsCenterNumberState();
            AdvancedPreferenceFragment.this.setAutoRetrievalAndSimMessage();
        }
    }

    private static class SmsOptimizationCharacterChangeListener implements OnPreferenceChangeListener {
        private SmsOptimizationCharacterChangeListener() {
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Boolean isChecked = (Boolean) newValue;
            if (!(preference instanceof SwitchPreference)) {
                return false;
            }
            Editor editor = PreferenceManager.getDefaultSharedPreferences(MmsApp.getApplication()).edit();
            editor.putBoolean("pref_key_sms_optimization_characters", isChecked.booleanValue());
            editor.commit();
            MessageUtils.reset7BitEnabledValue();
            int[] temp = new int[]{System.getInt(MmsApp.getApplication().getContentResolver(), "sms_coding_national_backup", 0)};
            if (isChecked.booleanValue()) {
                MessageUtils.setSmsCodingNationalCode(String.valueOf(temp[0]));
                MessageUtils.setSingleShiftTable(temp);
            } else if (temp[0] != 0) {
                temp[0] = 0;
                MessageUtils.setSmsCodingNationalCode("0");
                MessageUtils.setSingleShiftTable(temp);
            }
            return true;
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.advanced_preferences);
        this.mActivity = getActivity();
        setMessagePreferences();
        new IntentFilter().addAction("android.media.RINGER_MODE_CHANGED");
        this.mTM = (TelephonyManager) this.mActivity.getSystemService("phone");
        this.mListener = new ServiceStateListener(this.mActivity.getApplicationContext());
        this.mTM.listen(this.mListener, 1);
        registerSimStateChange();
        this.mOldMutilSimMode = this.mMutilSimMode;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListView list = (ListView) getView().findViewById(16908298);
        this.mFooterView = LayoutInflater.from(getActivity()).inflate(R.layout.divider_footer_view, list, false);
        list.setFooterDividersEnabled(false);
        list.addFooterView(this.mFooterView, null, false);
        updateFooterViewHeight(null);
    }

    public void onResume() {
        super.onResume();
        if (this.mMutilSimMode != this.mOldMutilSimMode) {
            this.mOldMutilSimMode = this.mMutilSimMode;
            setSmsCenterNumberState();
            setAutoRetrievalAndSimMessage();
        }
        updateSmsEnabledState();
        if (this.mAddSignature != null) {
            if (PreferenceManager.getDefaultSharedPreferences(this.mActivity).getBoolean("pref_key_signature", false)) {
                this.mAddSignature.setState(this.mActivity.getString(R.string.smart_sms_setting_status_open));
            } else {
                this.mAddSignature.setState(this.mActivity.getString(R.string.smart_sms_setting_status_close));
            }
        }
        initVerifitionSmsProtectState();
    }

    private void updateSmsEnabledState() {
        this.mIsSmsEnabled = MmsConfig.isSmsEnabled(this.mActivity);
        this.mPrefRoot.setEnabled(this.mIsSmsEnabled);
    }

    public void onDestroy() {
        super.onDestroy();
        if (!(this.mTM == null || this.mListener == null)) {
            this.mTM.listen(this.mListener, 0);
        }
        unregisterSimStateChange();
        if (this.mAutoRetrieviaDlg != null && this.mAutoRetrieviaDlg.isShowing()) {
            this.mAutoRetrieviaDlg.dismiss();
        }
        if (this.mAlwaysReceiveAndSendMmsDlg != null && this.mAlwaysReceiveAndSendMmsDlg.isShowing()) {
            this.mAlwaysReceiveAndSendMmsDlg.dismiss();
        }
    }

    private void setMessagePreferences() {
        this.mCommonPhrasePref = findPreference("pref_key_common_phrase");
        this.mPrefRoot = (PreferenceScreen) findPreference("pref_key_root");
        this.mManageSimPref = findPreference("pref_key_manage_sim_messages");
        this.mManageSim2Pref = findPreference("pref_key_manage_simuim2_messages");
        this.mMmsReadReportPref = findPreference("pref_key_mms_read_reports");
        this.mMmsReplyReadReportPref = findPreference("pref_key_mms_auto_reply_read_reports");
        this.mSaveModePref = (ListPreference) findPreference("pref_key_save_mode");
        this.mSaveModePrefSub0 = (ListPreference) findPreference("pref_key_save_mode_sub0");
        this.mSaveModePrefSub1 = (ListPreference) findPreference("pref_key_save_mode_sub1");
        this.mMmsGroupMmsPref = findPreference("pref_key_mms_group_mms");
        this.mAddSignature = (HwPreference) findPreference("pref_key_signature");
        if (!MmsConfig.isShowSignatureDialog()) {
            this.mPrefRoot.removePreference(this.mAddSignature);
        }
        this.mWapPushMgrPref = (SwitchPreference) findPreference("pref_key_sms_wappush_enable");
        this.mAutoRetrievalSingleCardPref = (SwitchPreference) findPreference("pref_key_mms_retrieval_during_roaming");
        this.mAutoRetrievalCard1Pref = (SwitchPreference) findPreference("pref_key_mms_retrieval_during_roaming_card1");
        this.mAutoRetrievalCard2Pref = (SwitchPreference) findPreference("pref_key_mms_retrieval_during_roaming_card2");
        setAutoRetrievalAndSimMessage();
        if (!MmsConfig.getWapPushSettingEnabled()) {
            this.mPrefRoot.removePreference(this.mWapPushMgrPref);
        }
        if (MmsConfig.getSmsOptimizationCharacters()) {
            this.mSmsOptimizationCharacters = (SwitchPreference) findPreference("pref_key_sms_optimization_characters");
            if (!(MmsConfig.getDefault7bitOptionValue() || this.mSmsOptimizationCharacters == null || MessageUtils.getIsAlwaysShowSmsOptimization())) {
                this.mPrefRoot.removePreference(this.mSmsOptimizationCharacters);
            }
        } else {
            this.mSmsOptimizationCharacters = (SwitchPreference) findPreference("pref_key_sms_optimization_characters");
            this.mPrefRoot.removePreference(this.mSmsOptimizationCharacters);
        }
        if (this.mSmsOptimizationCharacters != null) {
            this.mSmsOptimizationCharacters.setOnPreferenceChangeListener(this.smsOptimizationCharacterListener);
            String localNum = NumberFormat.getIntegerInstance().format(160);
            this.mSmsOptimizationCharacters.setSummary(getString(R.string.pref_summary_sms_optimization_characters_new, new Object[]{localNum}));
        }
        this.mMmsAutoReceivePref = findPreference("pref_key_mms_auto_retrieval_mms");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mActivity);
        setAutoReceivePrefState(sp.getInt("autoReceiveMms", MmsConfig.getDefaultAutoRetrievalMms()), true);
        this.mAlwaysReceiveAndSendMms = (HwPreference) findPreference("pref_key_always_receive_and_send_mms");
        setAlwaysReceiveAndSendMmsPrefState(sp.getInt("alwaysAllowMms", MmsConfig.getDefaultAlwaysAllowMms()), true);
        if (!(HwSpecialUtils.isAlwaysMms() && !HwSpecialUtils.isChinaRegion() && MmsConfig.getMmsEnabled())) {
            this.mPrefRoot.removePreference(this.mAlwaysReceiveAndSendMms);
            this.mAlwaysReceiveAndSendMms = null;
        }
        if (MmsConfig.getMmsEnabled()) {
            if (!MmsConfig.isShowMMSReadReports()) {
                this.mPrefRoot.removePreference(this.mMmsReadReportPref);
                this.mPrefRoot.removePreference(this.mMmsReplyReadReportPref);
            } else if ((!MmsConfig.getEnableMMSAutoReplyReadReports() || MmsConfig.isShowMmsReadReportDialog()) && this.mMmsReplyReadReportPref != null) {
                this.mPrefRoot.removePreference(this.mMmsReplyReadReportPref);
            }
            if (!MmsConfig.getGroupMmsEnabled() || TextUtils.isEmpty(MessageUtils.getLocalNumber())) {
                this.mPrefRoot.removePreference(this.mMmsGroupMmsPref);
            }
            if (MmsConfig.getCreationModeEnabled()) {
                ListPreference creationmodepref = (ListPreference) findPreference("pref_key_creation_mode");
                if (creationmodepref != null) {
                    String creationmode = PreferenceManager.getDefaultSharedPreferences(this.mActivity).getString("pref_key_creation_mode", "freemodemode");
                    String[] prefEntriesCreationMode = getResources().getStringArray(R.array.prefEntries_creation_mode);
                    if ("restrictionmode".equalsIgnoreCase(creationmode)) {
                        creationmodepref.setSummary(prefEntriesCreationMode[0]);
                    } else if ("warningmode".equalsIgnoreCase(creationmode)) {
                        creationmodepref.setSummary(prefEntriesCreationMode[1]);
                    } else if ("freemodemode".equalsIgnoreCase(creationmode)) {
                        creationmodepref.setSummary(prefEntriesCreationMode[2]);
                    }
                    creationmodepref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                        String[] prefEntriesCreationMode = AdvancedPreferenceFragment.this.getResources().getStringArray(R.array.prefEntries_creation_mode);

                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            if ("restrictionmode".equalsIgnoreCase((String) newValue)) {
                                preference.setSummary(this.prefEntriesCreationMode[0]);
                            } else if ("warningmode".equalsIgnoreCase((String) newValue)) {
                                preference.setSummary(this.prefEntriesCreationMode[1]);
                            } else if ("freemodemode".equalsIgnoreCase((String) newValue)) {
                                preference.setSummary(this.prefEntriesCreationMode[2]);
                            }
                            return true;
                        }
                    });
                }
            } else {
                removeRestrictedMode();
            }
        } else {
            if (!MmsConfig.getGroupMmsEnabled() || TextUtils.isEmpty(MessageUtils.getLocalNumber())) {
                this.mPrefRoot.removePreference(this.mMmsGroupMmsPref);
            }
            this.mMmsAutoReceivePref = findPreference("pref_key_mms_auto_retrieval_mms");
            if (this.mMmsAutoReceivePref != null) {
                this.mPrefRoot.removePreference(this.mMmsAutoReceivePref);
            }
            if (this.mMmsReadReportPref != null) {
                this.mPrefRoot.removePreference(this.mMmsReadReportPref);
            }
            if (this.mMmsReplyReadReportPref != null) {
                this.mPrefRoot.removePreference(this.mMmsReplyReadReportPref);
            }
            if (this.mCommonPhrasePref != null) {
                this.mPrefRoot.removePreference(this.mCommonPhrasePref);
            }
            removeRestrictedMode();
        }
        this.mVerifitionSmsProtectPref = (SwitchPreference) findPreference("pref_key_verifition_sms_protect_enable");
        if (this.mVerifitionSmsProtectPref != null) {
            if (!MmsConfig.isSupportSafeVerifitionSms()) {
                this.mPrefRoot.removePreference(this.mVerifitionSmsProtectPref);
            }
            PreferenceUtils.setVerifitionSmsProtectEnable(this.mActivity, this.mVerifitionSmsProtectPref.isChecked());
        }
        setAdvancedPreferenceChangeListener();
        this.mExpireModePref = (ListPreference) findPreference("pref_key_mms_expire");
        this.mExpireModeWithMaxPref = (ListPreference) findPreference("pref_key_mms_expire_with_max");
        if (!(this.mExpireModePref == null || this.mExpireModeWithMaxPref == null)) {
            String value;
            if (MmsConfig.isMmsExpiryMaxEnable()) {
                ((PreferenceCategory) findPreference("pref_key_mms_settings")).removePreference(this.mExpireModePref);
                this.mExpireModeWithMaxPref.setOnPreferenceChangeListener(this.expireModePrefListener);
                value = this.mExpireModeWithMaxPref.getValue();
                if (value != null && "2".equals(value)) {
                    this.mExpireModeWithMaxPref.setSummary(R.string.pref_summary_mms_expire_mode_2_days);
                } else if (value == null || !"7".equals(value)) {
                    this.mExpireModeWithMaxPref.setSummary(R.string.pref_summary_mms_expire_mode_max);
                } else {
                    this.mExpireModeWithMaxPref.setSummary(R.string.pref_summary_mms_expire_mode_7_days);
                }
            } else {
                this.mPrefRoot.removePreference(this.mExpireModeWithMaxPref);
                if (MmsConfig.isMmsExpiryModifyEnable()) {
                    this.mExpireModePref.setDefaultValue(Integer.valueOf(R.string.prefDefault_mms_expire_slidesmoothshowactivity));
                    this.mExpireModePref.setOnPreferenceChangeListener(this.expireModePrefListener);
                    value = this.mExpireModePref.getValue();
                    if (value != null && NewXyHttpRunnable.ERROR_CODE_SERVICE_ERR.equals(value)) {
                        this.mExpireModePref.setSummary(R.string.pref_summary_mms_expire_mode_slideshowactivity3);
                    } else if (value == null || !"2".equals(value)) {
                        this.mExpireModePref.setSummary(R.string.pref_summary_mms_expire_mode_slideshowactivity);
                    } else {
                        this.mExpireModePref.setSummary(R.string.pref_summary_mms_expire_mode_slideshowactivity2);
                    }
                } else {
                    this.mPrefRoot.removePreference(this.mExpireModePref);
                }
            }
        }
        setSaveMode();
        this.mSmsCenterNumberPref = findPreference("sms_center_number");
        this.mSmc1Pref = findPreference("pref_key_simuim1_message_center");
        this.mSmc2Pref = findPreference("pref_key_simuim2_message_center");
        setSmsCenterNumberState();
        enableForwardMessageFrom(getForwardMessageFrom(this.mActivity), this.mActivity);
        if (this.mHwCustAdvancedPreferenceItem != null) {
            this.mHwCustAdvancedPreferenceItem.updateCustPreference(this.mActivity, (PreferenceScreen) findPreference("pref_key_root"));
        }
    }

    private void initVerifitionSmsProtectState() {
        if (this.mVerifitionSmsProtectPref != null) {
            if (System.getInt(this.mActivity.getContentResolver(), "verifition_sms_protect_enable", 1) == 1) {
                this.mVerifitionSmsProtectPref.setChecked(true);
                PreferenceUtils.setVerifitionSmsProtectEnable(getContext(), true);
            } else {
                this.mVerifitionSmsProtectPref.setChecked(false);
                PreferenceUtils.setVerifitionSmsProtectEnable(getContext(), false);
            }
        }
    }

    private void setAutoRetrievalAndSimMessage() {
        preparePreference(this.mManageSimPref);
        preparePreference(this.mManageSim2Pref);
        if (isMultiSimState()) {
            if (this.mAutoRetrievalSingleCardPref != null) {
                this.mPrefRoot.removePreference(this.mAutoRetrievalSingleCardPref);
            }
            updateAutoRetreivePref();
            this.mAutoRetrievalCard1Pref.setOnPreferenceChangeListener(this.autoRetrievalPrefListener);
            this.mAutoRetrievalCard2Pref.setOnPreferenceChangeListener(this.autoRetrievalPrefListener);
            this.mManageSimPref.setTitle(R.string.pref_title_manage_simuim1_messages_ug);
            this.mManageSim2Pref.setTitle(R.string.pref_title_manage_simuim2_messages_ug);
            this.mManageSimPref.setEnabled(true);
            this.mManageSim2Pref.setEnabled(true);
            if (!MmsConfig.getMuilCardAutoRetrievalEnable()) {
                this.mPrefRoot.removePreference(this.mAutoRetrievalCard1Pref);
                this.mPrefRoot.removePreference(this.mAutoRetrievalCard2Pref);
                return;
            }
            return;
        }
        if (this.mAutoRetrievalCard1Pref != null) {
            this.mPrefRoot.removePreference(this.mAutoRetrievalCard1Pref);
        }
        if (this.mAutoRetrievalCard2Pref != null) {
            this.mPrefRoot.removePreference(this.mAutoRetrievalCard2Pref);
        }
        if (this.mManageSim2Pref != null) {
            this.mPrefRoot.removePreference(this.mManageSim2Pref);
        }
        this.mManageSimPref.setTitle(R.string.pref_title_manage_sim_messages);
        if (MessageUtils.isMultiSimEnabled()) {
            if (!(1 == MessageUtils.getIccCardStatus(0) || 1 == MessageUtils.getIccCardStatus(1))) {
                this.mManageSimPref.setEnabled(false);
            }
        } else if (!(MmsApp.getDefaultTelephonyManager().hasIccCard() && 1 == MessageUtils.getIccCardStatus())) {
            this.mManageSimPref.setEnabled(false);
        }
        if (MmsConfig.getAutoRetrievalSingleCardEnable()) {
            this.mAutoRetrievalSingleCardPref.setOnPreferenceChangeListener(this.autoRetrievalPrefListener);
        } else {
            this.mPrefRoot.removePreference(this.mAutoRetrievalSingleCardPref);
        }
    }

    private void setSmsCenterNumberSummary() {
        boolean z = true;
        if (this.mSmsCenterNumberPref == null) {
            return;
        }
        if (this.mHwCustAdvancedPreferenceItem == null || !(this.mHwCustAdvancedPreferenceItem.getEnableShowSmscNotEdit() || this.mHwCustAdvancedPreferenceItem.getCustMccmncEnableShowSmscNotEdit(-1))) {
            this.mSmsCenterNumberPref.setEnabled(MmsConfig.getEnableModifySMSCenterNumber());
            if (!MessageUtils.isMultiSimEnabled()) {
                Preference preference = this.mSmsCenterNumberPref;
                if (1 != MessageUtils.getIccCardStatus()) {
                    z = false;
                }
                preference.setEnabled(z);
                return;
            }
            return;
        }
        this.mHwCustAdvancedPreferenceItem.setSummaryAndDisableSmsCenterAddrPref(this.mSmsCenterNumberPref, -1);
    }

    private void setSmsCenterNumberState() {
        TelephonyManager telephonyMgr = MmsApp.getDefaultTelephonyManager();
        preparePreference(this.mSmsCenterNumberPref);
        preparePreference(this.mSmc1Pref);
        preparePreference(this.mSmc2Pref);
        if (isMultiSimState()) {
            if (this.mSmsCenterNumberPref != null) {
                this.mPrefRoot.removePreference(this.mSmsCenterNumberPref);
            }
            if (this.mSmc1Pref != null) {
                if (!MmsConfig.getEnableModifySMSCenterNumber() || (1 == MessageUtils.getDsdsMode() && 2 == MessageUtils.getCurrentPhoneType(0))) {
                    this.mPrefRoot.removePreference(this.mSmc1Pref);
                } else {
                    this.mSmc1Pref.setEnabled(true);
                    this.mSmc1Pref.setTitle(R.string.pref_title_simuim1_message_center_ug);
                    if (this.mHwCustAdvancedPreferenceItem != null && (this.mHwCustAdvancedPreferenceItem.getEnableShowSmscNotEdit() || this.mHwCustAdvancedPreferenceItem.getCustMccmncEnableShowSmscNotEdit(0))) {
                        this.mHwCustAdvancedPreferenceItem.setSummaryAndDisableSmsCenterAddrPref(this.mSmc1Pref, 0);
                    }
                }
            }
            if (this.mSmc2Pref != null) {
                if (!MmsConfig.getEnableModifySMSCenterNumber() || 2 == MessageUtils.getCurrentPhoneType(1)) {
                    this.mPrefRoot.removePreference(this.mSmc2Pref);
                } else {
                    this.mSmc2Pref.setEnabled(true);
                    if (MessageUtils.isCTCdmaCardInGsmMode()) {
                        this.mSmc2Pref.setTitle(R.string.pref_title_simuim1_message_center_ug);
                    } else {
                        this.mSmc2Pref.setTitle(R.string.pref_title_simuim2_message_center_ug);
                    }
                    if (this.mHwCustAdvancedPreferenceItem != null && (this.mHwCustAdvancedPreferenceItem.getEnableShowSmscNotEdit() || this.mHwCustAdvancedPreferenceItem.getCustMccmncEnableShowSmscNotEdit(1))) {
                        this.mHwCustAdvancedPreferenceItem.setSummaryAndDisableSmsCenterAddrPref(this.mSmc2Pref, 1);
                    }
                }
            }
            return;
        }
        if (this.mSmc1Pref != null) {
            this.mPrefRoot.removePreference(this.mSmc1Pref);
        }
        if (this.mSmc2Pref != null) {
            this.mPrefRoot.removePreference(this.mSmc2Pref);
        }
        if (this.mSmsCenterNumberPref != null) {
            this.mSmsCenterNumberPref.setOnPreferenceChangeListener(this.smsCenterNumberPrefListener);
        }
        if (MessageUtils.isMultiSimEnabled()) {
            if (1 != MessageUtils.getIccCardStatus(0) && 1 != MessageUtils.getIccCardStatus(1)) {
                if (this.mSmsCenterNumberPref != null) {
                    this.mSmsCenterNumberPref.setEnabled(false);
                }
                return;
            } else if (1 == MessageUtils.getIccCardStatus(0) && 1 == MessageUtils.getDsdsMode() && 2 == MessageUtils.getCurrentPhoneType(0)) {
                if (this.mSmsCenterNumberPref != null) {
                    this.mPrefRoot.removePreference(this.mSmsCenterNumberPref);
                }
                return;
            } else if (1 == MessageUtils.getIccCardStatus(1) && 2 == MessageUtils.getCurrentPhoneType(1)) {
                if (this.mSmsCenterNumberPref != null) {
                    this.mPrefRoot.removePreference(this.mSmsCenterNumberPref);
                }
                return;
            }
        } else if (!telephonyMgr.hasIccCard()) {
            if (this.mSmsCenterNumberPref != null) {
                this.mSmsCenterNumberPref.setEnabled(false);
            }
            return;
        } else if (2 == telephonyMgr.getPhoneType()) {
            if (this.mSmsCenterNumberPref != null) {
                if (this.mHwCustAdvancedPreferenceItem == null || !this.mHwCustAdvancedPreferenceItem.getEnableShowSmscNotEdit()) {
                    this.mPrefRoot.removePreference(this.mSmsCenterNumberPref);
                } else {
                    setSmsCenterNumberSummary();
                }
            }
            return;
        }
        if (MmsConfig.getEnableModifySMSCenterNumber()) {
            setSmsCenterNumberSummary();
            return;
        }
        if (this.mSmsCenterNumberPref != null) {
            this.mPrefRoot.removePreference(this.mSmsCenterNumberPref);
        }
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == this.mManageSimPref) {
            StatisticalHelper.incrementReportCount(this.mActivity, 2078);
            if (MessageUtils.isMultiSimEnabled()) {
                Intent intent = new Intent(this.mActivity, ManageSimMessages.class);
                if (1 == MessageUtils.getIccCardStatus(0)) {
                    intent = MessageUtils.setSimIdToIntent(intent, 0);
                } else if (1 == MessageUtils.getIccCardStatus(1)) {
                    intent = MessageUtils.setSimIdToIntent(intent, 1);
                }
                startActivity(intent);
            } else {
                startActivity(new Intent(this.mActivity, ManageSimMessages.class));
            }
        } else if (preference == this.mCommonPhrasePref) {
            StatisticalHelper.incrementReportCount(this.mActivity, 2020);
            startActivity(new Intent(this.mActivity, CommonPhrase.class));
        } else if (preference == this.mAddSignature) {
            startActivity(new Intent(this.mActivity, AddSignature.class));
        } else if (preference == this.mManageSim2Pref) {
            StatisticalHelper.incrementReportCount(this.mActivity, 2079);
            if (MessageUtils.isMultiSimEnabled()) {
                startActivity(MessageUtils.setSimIdToIntent(new Intent(this.mActivity, ManageSimMessages.class), 1));
            }
        } else if (preference == this.mSmsCenterNumberPref) {
            StatisticalHelper.incrementReportCount(this.mActivity, 2153);
            if (!MessageUtils.isMultiSimEnabled()) {
                startSmsCenterNumberSettingActivity(-1);
            } else if (1 == MessageUtils.getIccCardStatus(0)) {
                startSmsCenterNumberSettingActivity(0);
            } else if (1 == MessageUtils.getIccCardStatus(1)) {
                startSmsCenterNumberSettingActivity(1);
            }
        } else if (preference == this.mSmc1Pref) {
            StatisticalHelper.incrementReportCount(this.mActivity, 2154);
            startSmsCenterNumberSettingActivity(0);
        } else if (preference == this.mSmc2Pref) {
            StatisticalHelper.incrementReportCount(this.mActivity, 2155);
            startSmsCenterNumberSettingActivity(1);
        } else if (preference == this.mMmsAutoReceivePref) {
            StatisticalHelper.incrementReportCount(this.mActivity, 2084);
            showAutoRetrievialDialog();
        } else if (preference == this.mAlwaysReceiveAndSendMms) {
            showAlwaysReceiveAndSendMmsDialog();
        } else if (this.mHwCustAdvancedPreferenceItem != null) {
            this.mHwCustAdvancedPreferenceItem.onCustomPreferenceItemClick(this.mActivity, preference);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void showAutoRetrievialDialog() {
        if (this.mAutoRetrieviaDlg == null || !this.mAutoRetrieviaDlg.isShowing()) {
            this.mAutoRetrieviaDlg = new Builder(this.mActivity).setTitle(R.string.mms_auto_retrieval_text).setIconAttribute(16843605).setAdapter(new AutoRetrievialAdapter(), null).setNegativeButton(R.string.no, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (AdvancedPreferenceFragment.this.mAutoRetrieviaDlg != null) {
                        AdvancedPreferenceFragment.this.mAutoRetrieviaDlg.dismiss();
                    }
                }
            }).create();
            this.mAutoRetrieviaDlg.getListView().setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                    int count = parent.getChildCount();
                    for (int i = 0; i < count; i++) {
                        ((RadioButton) parent.getChildAt(i).findViewById(R.id.list_radio)).setChecked(false);
                    }
                    ((RadioButton) view.findViewById(R.id.list_radio)).setChecked(true);
                    AdvancedPreferenceFragment.this.setAutoReceivePrefState(pos, false);
                    AdvancedPreferenceFragment.this.mAutoRetrieviaDlg.dismiss();
                }
            });
            this.mAutoRetrieviaDlg.show();
        }
    }

    private void setAutoReceivePrefState(int which, boolean isInit) {
        addReportCount(which, isInit);
        String[] summaries = getResources().getStringArray(R.array.prefEntries_mms_auto_receive);
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mActivity).edit();
        if (which == 1) {
            startMmsDownload();
            setMultiSimRoamingAutoRetrieveValue(false);
            editor.putBoolean("pref_key_mms_retrieval_during_roaming", false);
            editor.putBoolean("pref_key_mms_auto_retrieval", true);
            editor.putInt("autoReceiveMms", 1);
        } else if (which == 0) {
            startMmsDownload();
            if (this.mHwCustAdvancedPreferenceItem != null) {
                this.mHwCustAdvancedPreferenceItem.setUserModifiedAutoRetreiveFlag(this.mActivity);
            }
            setMultiSimRoamingAutoRetrieveValue(true);
            editor.putBoolean("pref_key_mms_retrieval_during_roaming", true);
            editor.putBoolean("pref_key_mms_auto_retrieval", true);
            editor.putInt("autoReceiveMms", 0);
        } else {
            setMultiSimRoamingAutoRetrieveValue(false);
            editor.putBoolean("pref_key_mms_retrieval_during_roaming", false);
            editor.putBoolean("pref_key_mms_auto_retrieval", false);
            editor.putInt("autoReceiveMms", 2);
        }
        editor.commit();
        this.mMmsAutoReceivePref.setSummary(summaries[which]);
        PreferenceUtils.setAutoRetrieval(summaries[which], this.mActivity);
    }

    private void showAlwaysReceiveAndSendMmsDialog() {
        if (this.mAlwaysReceiveAndSendMmsDlg == null || !this.mAlwaysReceiveAndSendMmsDlg.isShowing()) {
            this.mAlwaysReceiveAndSendMmsDlg = new Builder(this.mActivity).setTitle(R.string.always_receive_and_send_mms).setIconAttribute(16843605).setAdapter(new AlwaysReceiveAndSendMmsAdapter(), null).setNegativeButton(R.string.no, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (AdvancedPreferenceFragment.this.mAlwaysReceiveAndSendMmsDlg != null) {
                        AdvancedPreferenceFragment.this.mAlwaysReceiveAndSendMmsDlg.dismiss();
                    }
                }
            }).create();
            this.mAlwaysReceiveAndSendMmsDlg.getListView().setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                    ((RadioButton) view.findViewById(R.id.list_radio)).setChecked(true);
                    if (AdvancedPreferenceFragment.this.mHwCustAdvancedPreferenceItem != null) {
                        AdvancedPreferenceFragment.this.mHwCustAdvancedPreferenceItem.setAlwaysTxRxMmsClick(AdvancedPreferenceFragment.this.getContext());
                    }
                    AdvancedPreferenceFragment.this.setAlwaysReceiveAndSendMmsPrefState(pos, false);
                    AdvancedPreferenceFragment.this.mAlwaysReceiveAndSendMmsDlg.dismiss();
                }
            });
            this.mAlwaysReceiveAndSendMmsDlg.show();
        }
    }

    private void setAlwaysReceiveAndSendMmsPrefState(int which, boolean isInit) {
        String[] summaries = getResources().getStringArray(R.array.prefEntries_mms_always_receive_and_send_mms);
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mActivity).edit();
        editor.putInt("alwaysAllowMms", which);
        System.putInt(MmsApp.getApplication().getContentResolver(), "enable_always_allow_mms", which > 1 ? 0 : 1);
        editor.commit();
        this.mAlwaysReceiveAndSendMms.setState(summaries[which]);
    }

    public static void setAlwaysReceiveAndSendMmsPrefState(int which) {
        Context context = MmsApp.getApplication().getApplicationContext();
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt("alwaysAllowMms", which);
        System.putInt(context.getContentResolver(), "enable_always_allow_mms", which > 1 ? 0 : 1);
        editor.commit();
    }

    private void addReportCount(int which, boolean isInit) {
        if (!isInit) {
            if (which == 1) {
                StatisticalHelper.incrementReportCount(this.mActivity, 2151);
            } else if (which == 0) {
                StatisticalHelper.incrementReportCount(this.mActivity, 2152);
            } else {
                StatisticalHelper.incrementReportCount(this.mActivity, 2150);
            }
        }
    }

    private void startSmsCenterNumberSettingActivity(int subID) {
        Intent intent = new Intent(this.mActivity.getApplicationContext(), HwSmsCenterNumberEditerActivity.class);
        intent.putExtra("intent_key_crad_sub_id", subID);
        intent.putExtra("intent_key_mutil_mode", this.mMutilSimMode);
        startActivity(intent);
    }

    private void startMmsDownload() {
        TransactionService.startMe(this.mActivity, "android.intent.action.ACTION_ENABLE_AUTO_RETRIEVE");
    }

    private static boolean getUsingSIMCardStorage(Context context) {
        if (Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("pref_key_save_mode", "1")) == 0) {
            return true;
        }
        return false;
    }

    private void updateAutoRetreivePref() {
        int autoRetreiveCard1 = MessageUtils.getRoamingAutoRetrieveValue(this.mActivity.getContentResolver(), 1);
        int autoRetreiveCard2 = MessageUtils.getRoamingAutoRetrieveValue(this.mActivity.getContentResolver(), 2);
        if (autoRetreiveCard1 == 1) {
            this.mAutoRetrievalCard1Pref.setChecked(true);
        } else {
            this.mAutoRetrievalCard1Pref.setChecked(false);
        }
        if (autoRetreiveCard2 == 1) {
            this.mAutoRetrievalCard2Pref.setChecked(true);
        } else {
            this.mAutoRetrievalCard2Pref.setChecked(false);
        }
        if (1 == MessageUtils.getDsdsMode()) {
            this.mAutoRetrievalCard1Pref.setTitle(R.string.pref_title_mms_auto_retrieval_during_roaming_ug_card1);
            this.mAutoRetrievalCard2Pref.setTitle(R.string.pref_title_mms_auto_retrieval_during_roaming_ug_card2);
        }
        if (MessageUtils.getIccCardStatus(0) == 1) {
            this.mAutoRetrievalCard1Pref.setEnabled(true);
        } else {
            this.mAutoRetrievalCard1Pref.setEnabled(false);
        }
        if (MessageUtils.getIccCardStatus(1) == 1) {
            this.mAutoRetrievalCard2Pref.setEnabled(true);
        } else {
            this.mAutoRetrievalCard2Pref.setEnabled(false);
        }
    }

    private static void enableForwardMessageFrom(boolean enabled, Context context) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("pref_key_forward_message_from_settings", enabled);
        editor.apply();
    }

    private static boolean getForwardMessageFrom(Context context) {
        return true;
    }

    private void removeRestrictedMode() {
        Preference restrictedMode = findPreference("pref_key_creation_mode");
        if (restrictedMode != null) {
            this.mPrefRoot.removePreference(restrictedMode);
        }
    }

    public void refreshFragmentView() {
        if (getView() == null) {
            MLog.e("AdvancedPreferenceFragment", "getView is null");
            return;
        }
        setPreferenceScreen(null);
        addPreferencesFromResource(R.xml.advanced_preferences);
        setMessagePreferences();
        MessageUtils.reset7BitEnabledValue();
    }

    private void setMultiSimRoamingAutoRetrieveValue(Preference pref, boolean isChecked) {
        if (pref == this.mAutoRetrievalCard1Pref) {
            MessageUtils.setRoamingAutoRetrieveValue(this.mActivity.getContentResolver(), 1, isChecked);
        } else if (pref == this.mAutoRetrievalCard2Pref) {
            MessageUtils.setRoamingAutoRetrieveValue(this.mActivity.getContentResolver(), 2, isChecked);
        }
    }

    private void setMultiSimRoamingAutoRetrieveValue(boolean isChecked) {
        if (1 == MessageUtils.getIccCardStatus(0)) {
            MessageUtils.setRoamingAutoRetrieveValue(this.mActivity.getContentResolver(), 1, isChecked);
        }
        if (1 == MessageUtils.getIccCardStatus(1)) {
            MessageUtils.setRoamingAutoRetrieveValue(this.mActivity.getContentResolver(), 2, isChecked);
        }
    }

    private void setSaveMode() {
        int i = 0;
        if (this.mPrefRoot == null) {
            MLog.v("AdvancedPreferenceFragment", "smsCategory is null!");
        } else if (MmsConfig.isSaveModeEnabled()) {
            if (MessageUtils.isMultiSimEnabled() && MmsConfig.isSaveModeMultiCardPerf()) {
                ListPreference listPreference;
                String[] strArr;
                int i2;
                if (this.mSaveModePref != null) {
                    this.mPrefRoot.removePreference(this.mSaveModePref);
                }
                if (this.mSaveModePrefSub0 != null) {
                    this.mSaveModeSummary = this.mActivity.getResources().getStringArray(R.array.save_mode_entries);
                    listPreference = this.mSaveModePrefSub0;
                    strArr = this.mSaveModeSummary;
                    if (PreferenceUtils.getUsingSIMCardStorageWithSubId(this.mActivity, 0)) {
                        i2 = 0;
                    } else {
                        i2 = 1;
                    }
                    listPreference.setSummary(strArr[i2]);
                    this.mSaveModePrefSub0.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            AdvancedPreferenceFragment.this.mSaveModePrefSub0.setSummary(AdvancedPreferenceFragment.this.mSaveModeSummary[Integer.parseInt(String.valueOf(newValue))]);
                            return true;
                        }
                    });
                }
                if (this.mSaveModePrefSub1 != null) {
                    this.mSaveModeSummary = this.mActivity.getResources().getStringArray(R.array.save_mode_entries);
                    listPreference = this.mSaveModePrefSub1;
                    strArr = this.mSaveModeSummary;
                    if (PreferenceUtils.getUsingSIMCardStorageWithSubId(this.mActivity, 1)) {
                        i2 = 0;
                    } else {
                        i2 = 1;
                    }
                    listPreference.setSummary(strArr[i2]);
                    this.mSaveModePrefSub1.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            AdvancedPreferenceFragment.this.mSaveModePrefSub1.setSummary(AdvancedPreferenceFragment.this.mSaveModeSummary[Integer.parseInt(String.valueOf(newValue))]);
                            return true;
                        }
                    });
                }
                if (this.mSaveModePrefSub0 != null) {
                    if (MessageUtils.getIccCardStatus(0) == 1) {
                        this.mSaveModePrefSub0.setEnabled(true);
                    } else {
                        this.mSaveModePrefSub0.setEnabled(false);
                    }
                }
                if (this.mSaveModePrefSub1 != null) {
                    if (MessageUtils.getIccCardStatus(1) == 1) {
                        this.mSaveModePrefSub1.setEnabled(true);
                    } else {
                        this.mSaveModePrefSub1.setEnabled(false);
                    }
                }
            } else {
                if (this.mSaveModePrefSub0 != null) {
                    this.mPrefRoot.removePreference(this.mSaveModePrefSub0);
                }
                if (this.mSaveModePrefSub1 != null) {
                    this.mPrefRoot.removePreference(this.mSaveModePrefSub1);
                }
                if (this.mSaveModePref != null) {
                    this.mSaveModeSummary = this.mActivity.getResources().getStringArray(R.array.save_mode_entries);
                    ListPreference listPreference2 = this.mSaveModePref;
                    String[] strArr2 = this.mSaveModeSummary;
                    if (!getUsingSIMCardStorage(this.mActivity)) {
                        i = 1;
                    }
                    listPreference2.setSummary(strArr2[i]);
                    this.mSaveModePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            AdvancedPreferenceFragment.this.mSaveModePref.setSummary(AdvancedPreferenceFragment.this.mSaveModeSummary[Integer.parseInt(String.valueOf(newValue))]);
                            return true;
                        }
                    });
                }
            }
        } else {
            this.mPrefRoot.removePreference(this.mSaveModePref);
            this.mPrefRoot.removePreference(this.mSaveModePrefSub0);
            this.mPrefRoot.removePreference(this.mSaveModePrefSub1);
        }
    }

    private void updatePreferencesState() {
        refreshFragmentView();
    }

    private void registerSimStateChange() {
        if (this.mSingleSimChangeReceiver == null) {
            this.mSingleSimChangeReceiver = HwTelephony.registeSimChange(MmsApp.getApplication(), new HwSimStateListener() {
                public void onSimStateChanged(int simState) {
                    AdvancedPreferenceFragment.this.updatePreferencesState();
                }

                public void onSimStateChanged(int simState, int subId) {
                    AdvancedPreferenceFragment.this.updatePreferencesState();
                }
            });
        }
    }

    private void unregisterSimStateChange() {
        try {
            if (this.mSingleSimChangeReceiver != null) {
                MmsApp.getApplication().unregisterReceiver(this.mSingleSimChangeReceiver);
                this.mSingleSimChangeReceiver = null;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private boolean isMultiSimState() {
        this.mMutilSimMode = HwMessageUtils.getMultiSimState();
        if (this.mMutilSimMode == 0) {
            return true;
        }
        return false;
    }

    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        int width = ((WindowManager) getActivity().getSystemService("window")).getDefaultDisplay().getWidth();
        if (enter) {
            return ObjectAnimator.ofFloat(this, "translationX", new float[]{(float) width, 0.0f});
        }
        return ObjectAnimator.ofFloat(this, "translationX", new float[]{0.0f, (float) width});
    }

    private void setAdvancedPreferenceChangeListener() {
        if (this.mWapPushMgrPref != null) {
            this.mWapPushMgrPref.setOnPreferenceChangeListener(this.mCommonSwitchChangeListener);
        }
        if (this.mVerifitionSmsProtectPref != null) {
            this.mVerifitionSmsProtectPref.setOnPreferenceChangeListener(this.mCommonSwitchChangeListener);
        }
    }

    private boolean containPreference(Preference preference) {
        int preferenceCount = this.mPrefRoot.getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            if (preference.getKey().equals(this.mPrefRoot.getPreference(i).getKey())) {
                return true;
            }
        }
        return false;
    }

    private void preparePreference(Preference preference) {
        if (!(preference == null || containPreference(preference))) {
            this.mPrefRoot.addPreference(preference);
        }
    }

    private void updateFooterViewHeight(Configuration newConfig) {
        if (this.mFooterView != null) {
            boolean isLandscape = newConfig == null ? getResources().getConfiguration().orientation == 2 : newConfig.orientation == 2;
            LayoutParams lp = this.mFooterView.getLayoutParams();
            int dimension = (!isLandscape || isInMultiWindowMode()) ? (int) getResources().getDimension(R.dimen.toolbar_footer_height) : 0;
            lp.height = dimension;
            this.mFooterView.setLayoutParams(lp);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateFooterViewHeight(newConfig);
    }

    private boolean isInMultiWindowMode() {
        if (getActivity() == null) {
            return false;
        }
        return getActivity().isInMultiWindowMode();
    }
}
