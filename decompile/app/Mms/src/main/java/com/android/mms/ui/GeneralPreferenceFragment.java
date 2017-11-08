package com.android.mms.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ListView;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.settings.SmartSmsSettingActivity;
import com.android.messaging.util.OsUtil;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.ui.views.CommonLisener;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.RcsGeneralPreferenceFragment;
import com.google.android.gms.Manifest.permission;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.ui.HwPreferenceFragment;
import com.huawei.mms.ui.RingToneAndVibrateSettings;
import com.huawei.mms.ui.SmartArchiveSettings;
import com.huawei.mms.util.DefaultSmsAppChangedReceiver;
import com.huawei.mms.util.DefaultSmsAppChangedReceiver.HwDefSmsAppChangedListener;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.MccMncConfig;
import com.huawei.mms.util.ProviderCallUtils;
import com.huawei.mms.util.ProviderCallUtils.CallRequest;
import com.huawei.mms.util.SmartArchiveSettingUtils;
import com.huawei.mms.util.StatisticalHelper;
import java.text.NumberFormat;

public class GeneralPreferenceFragment extends HwPreferenceFragment implements OnPreferenceChangeListener {
    private Activity mActivity;
    private Preference mCancelSmsSetting;
    private PreferenceCategory mCommonPrefCategory;
    private CryptoGeneralPreferenceFragment mCryptoGeneralPreferenceFragment = new CryptoGeneralPreferenceFragment();
    private HwCustGeneralPreferenceFragment mCust = ((HwCustGeneralPreferenceFragment) HwCustUtils.createObj(HwCustGeneralPreferenceFragment.class, new Object[0]));
    DefaultSmsAppChangedReceiver mDefSmsAppChangedReceiver = null;
    private HwPreference mDeliverReport;
    private boolean[] mDeliverReportChecked = new boolean[]{false, false};
    private boolean[] mDeliverReportCheckedOld = new boolean[]{false, false};
    AlertDialog mDeliverReportDialog;
    private OnDismissListener mDismissListener = new OnDismissListener() {
        public void onDismiss(DialogInterface dialog) {
            GeneralPreferenceFragment.this.mDeliverReportChecked[0] = GeneralPreferenceFragment.this.mDeliverReportCheckedOld[0];
            GeneralPreferenceFragment.this.mDeliverReportChecked[1] = GeneralPreferenceFragment.this.mDeliverReportCheckedOld[1];
        }
    };
    private ListPreference mDisplayModePref;
    private String[] mDisplayModeSummary;
    private View mFooterView = null;
    private boolean mIsFirstEnter = false;
    private boolean mIsSmsEnabled = false;
    private Preference mMmsAdvenceSetting;
    private OnClickListener mNegativeListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            if (GeneralPreferenceFragment.this.mDeliverReportDialog != null) {
                GeneralPreferenceFragment.this.mDeliverReportDialog.dismiss();
            }
        }
    };
    private PreferenceCategory mOtherPrefCategory;
    private Preference mPinupUnreadMessage;
    private ListPreference mPlayModePref;
    private OnClickListener mPositiveListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            GeneralPreferenceFragment.this.updateDeliverReport();
        }
    };
    private RcsGeneralPreferenceFragment mRcsGeneralPreferenceFragment;
    private Preference mRingTongAndVibrate;
    private Preference mRiskUriCheck;
    private AlertDialog mShowSmsStorageDailog = null;
    private Preference mSmartArchiveSetting;
    private Preference mSmartSmsSetting;
    private SwitchPreference mSmsRecoveryPref;
    private Preference mStorageStatusPref;
    public OnPreferenceChangeListener playModePrefListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if ("slideshowactivity".equals((String) newValue)) {
                GeneralPreferenceFragment.this.mPlayModePref.setSummary(R.string.pref_summary_sms_play_mode_slideshowactivity);
            } else {
                GeneralPreferenceFragment.this.mPlayModePref.setSummary(R.string.pref_summary_sms_play_mode_slidesmoothshowactivity);
            }
            return true;
        }
    };
    private OnMultiChoiceClickListener reportListener = new OnMultiChoiceClickListener() {
        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
            GeneralPreferenceFragment.this.mDeliverReportChecked[which] = isChecked;
        }
    };

    public interface ActivityCallback {
        void onCallBack();

        void switchFragment(int i);
    }

    public void onResume() {
        /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:80)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r11 = this;
        super.onResume();
        r11.checkSmsEnable();
        r7 = r11.mRcsGeneralPreferenceFragment;
        if (r7 == 0) goto L_0x000f;
    L_0x000a:
        r7 = r11.mRcsGeneralPreferenceFragment;
        r7.onResume();
    L_0x000f:
        r7 = r11.mSmartArchiveSetting;
        if (r7 == 0) goto L_0x0029;
    L_0x0013:
        r7 = com.android.mms.MmsConfig.isSupportSmartFolder();
        if (r7 == 0) goto L_0x0058;
    L_0x0019:
        r7 = r11.mActivity;
        r3 = com.huawei.mms.util.SmartArchiveSettingUtils.isSmartArchiveEnabled(r7);
        if (r3 == 0) goto L_0x0054;
    L_0x0021:
        r6 = 2131494104; // 0x7f0c04d8 float:1.8611707E38 double:1.053098011E-314;
    L_0x0024:
        r7 = r11.mSmartArchiveSetting;
        r7.setSummary(r6);
    L_0x0029:
        r7 = r11.mActivity;
        r5 = android.preference.PreferenceManager.getDefaultSharedPreferences(r7);
        r0 = com.android.mms.MmsConfig.getDefaultDeliveryReportState();
        r7 = "pref_key_delivery_reports";	 Catch:{ ClassCastException -> 0x0074, all -> 0x00ab }
        r8 = com.android.mms.MmsConfig.getDefaultDeliveryReportState();	 Catch:{ ClassCastException -> 0x0074, all -> 0x00ab }
        r0 = r5.getInt(r7, r8);	 Catch:{ ClassCastException -> 0x0074, all -> 0x00ab }
        r7 = r11.mCust;
        if (r7 == 0) goto L_0x0070;
    L_0x0042:
        r7 = r11.mCust;
        r7 = r7.isHideDeliveryReportsItem();
        if (r7 == 0) goto L_0x0070;
    L_0x004a:
        r7 = r11.mCust;
        r8 = r11.mOtherPrefCategory;
        r9 = r11.mDeliverReport;
        r7.hideDeliveryReportsItem(r8, r9);
    L_0x0053:
        return;
    L_0x0054:
        r6 = 2131494105; // 0x7f0c04d9 float:1.861171E38 double:1.0530980116E-314;
        goto L_0x0024;
    L_0x0058:
        r7 = r11.mCommonPrefCategory;
        r8 = "divider_pref_key_smart_archive_enable";
        r4 = r7.findPreference(r8);
        if (r4 == 0) goto L_0x0068;
    L_0x0063:
        r7 = r11.mCommonPrefCategory;
        r7.removePreference(r4);
    L_0x0068:
        r7 = r11.mCommonPrefCategory;
        r8 = r11.mSmartArchiveSetting;
        r7.removePreference(r8);
        goto L_0x0029;
    L_0x0070:
        r11.initDeliverReportState(r0);
        goto L_0x0053;
    L_0x0074:
        r2 = move-exception;
        r7 = "GeneralPreferenceFragment";	 Catch:{ ClassCastException -> 0x0074, all -> 0x00ab }
        r8 = "deliveryReportState can't convert to String from Int";	 Catch:{ ClassCastException -> 0x0074, all -> 0x00ab }
        com.huawei.cspcommon.MLog.e(r7, r8);	 Catch:{ ClassCastException -> 0x0074, all -> 0x00ab }
        r7 = "pref_key_delivery_reports";	 Catch:{ ClassCastException -> 0x0074, all -> 0x00ab }
        r8 = getDeliveryReportResId(r0);	 Catch:{ ClassCastException -> 0x0074, all -> 0x00ab }
        r8 = r11.getString(r8);	 Catch:{ ClassCastException -> 0x0074, all -> 0x00ab }
        r1 = r5.getString(r7, r8);	 Catch:{ ClassCastException -> 0x0074, all -> 0x00ab }
        r0 = r11.getDeliveryReportIndex(r1);	 Catch:{ ClassCastException -> 0x0074, all -> 0x00ab }
        r7 = r11.mCust;
        if (r7 == 0) goto L_0x00a7;
    L_0x0095:
        r7 = r11.mCust;
        r7 = r7.isHideDeliveryReportsItem();
        if (r7 == 0) goto L_0x00a7;
    L_0x009d:
        r7 = r11.mCust;
        r8 = r11.mOtherPrefCategory;
        r9 = r11.mDeliverReport;
        r7.hideDeliveryReportsItem(r8, r9);
        goto L_0x0053;
    L_0x00a7:
        r11.initDeliverReportState(r0);
        goto L_0x0053;
    L_0x00ab:
        r7 = move-exception;
        r8 = r11.mCust;
        if (r8 == 0) goto L_0x00c2;
    L_0x00b0:
        r8 = r11.mCust;
        r8 = r8.isHideDeliveryReportsItem();
        if (r8 == 0) goto L_0x00c2;
    L_0x00b8:
        r8 = r11.mCust;
        r9 = r11.mOtherPrefCategory;
        r10 = r11.mDeliverReport;
        r8.hideDeliveryReportsItem(r9, r10);
    L_0x00c1:
        throw r7;
    L_0x00c2:
        r11.initDeliverReportState(r0);
        goto L_0x00c1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.mms.ui.GeneralPreferenceFragment.onResume():void");
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.preferences);
        if (RcsCommonConfig.isRCSSwitchOn() && this.mRcsGeneralPreferenceFragment == null) {
            this.mRcsGeneralPreferenceFragment = new RcsGeneralPreferenceFragment(this);
        }
        if (this.mRcsGeneralPreferenceFragment != null) {
            this.mRcsGeneralPreferenceFragment.setGeneralPreferenceFragment(getActivity(), this);
        }
        this.mActivity = getActivity();
        this.mOtherPrefCategory = (PreferenceCategory) findPreference("pref_key_other_settings");
        this.mCommonPrefCategory = (PreferenceCategory) findPreference("pref_key_common_settings");
        this.mDisplayModePref = (ListPreference) findPreference("pref_key_display_mode");
        if (this.mRcsGeneralPreferenceFragment != null) {
            this.mRcsGeneralPreferenceFragment.onCreateRCS();
        }
        this.mMmsAdvenceSetting = findPreference("pref_key_mms_advanced_settings");
        this.mDeliverReport = (HwPreference) findPreference("pref_key_delivery_reports");
        this.mRingTongAndVibrate = findPreference("pref_key_ringTong_vibrate");
        setMessagePreferences();
        new IntentFilter().addAction("android.media.RINGER_MODE_CHANGED");
        setUserInited();
        registerDefSmsAppChanged();
        this.mCryptoGeneralPreferenceFragment.addSmsEncryptPref(this.mCommonPrefCategory);
        hideOptionsForSecondaryUser();
        keepOldFunction();
    }

    private String localNumberFormat(String cancelSummary) {
        StringBuffer strBuffer = new StringBuffer();
        NumberFormat nf = NumberFormat.getIntegerInstance();
        for (int i = 0; i < cancelSummary.length(); i++) {
            if (Character.isDigit(cancelSummary.charAt(i))) {
                strBuffer.append(nf.format((long) Integer.parseInt(String.valueOf(cancelSummary.charAt(i)))));
            } else {
                strBuffer.append(cancelSummary.charAt(i));
            }
        }
        return strBuffer.toString();
    }

    private void keepOldFunction() {
        PreferenceUtils.enableMessagePreview(true, this.mActivity);
    }

    private void setUserInited() {
        this.mActivity.getSharedPreferences("_has_set_default_values", 0).edit().putInt("mms_initialized_state", 4).apply();
    }

    private void initDeliverReportState(int deliveryReportIndex) {
        if (MmsConfig.getSMSDeliveryReportsEnabled() || this.mOtherPrefCategory == null || this.mDeliverReport == null) {
            Editor edit = PreferenceManager.getDefaultSharedPreferences(this.mActivity).edit();
            boolean mmsReportMode = false;
            boolean smsReportMode = false;
            boolean[] zArr;
            if (1 == deliveryReportIndex) {
                zArr = this.mDeliverReportChecked;
                this.mDeliverReportCheckedOld[0] = true;
                zArr[0] = true;
                zArr = this.mDeliverReportChecked;
                this.mDeliverReportCheckedOld[1] = false;
                zArr[1] = false;
                smsReportMode = true;
            } else if (2 == deliveryReportIndex) {
                zArr = this.mDeliverReportChecked;
                this.mDeliverReportCheckedOld[0] = false;
                zArr[0] = false;
                zArr = this.mDeliverReportChecked;
                this.mDeliverReportCheckedOld[1] = true;
                zArr[1] = true;
                mmsReportMode = true;
            } else if (3 == deliveryReportIndex) {
                zArr = this.mDeliverReportChecked;
                this.mDeliverReportCheckedOld[0] = true;
                zArr[0] = true;
                zArr = this.mDeliverReportChecked;
                this.mDeliverReportCheckedOld[1] = true;
                zArr[1] = true;
                smsReportMode = true;
                mmsReportMode = true;
            } else {
                zArr = this.mDeliverReportChecked;
                this.mDeliverReportCheckedOld[0] = false;
                zArr[0] = false;
                zArr = this.mDeliverReportChecked;
                this.mDeliverReportCheckedOld[1] = false;
                zArr[1] = false;
            }
            edit.putInt("pref_key_delivery_reports", deliveryReportIndex);
            edit.putBoolean("pref_key_mms_delivery_reports", mmsReportMode);
            edit.putBoolean("pref_key_sms_delivery_reports_sub0", smsReportMode);
            edit.putBoolean("pref_key_sms_delivery_reports_sub1", smsReportMode);
            edit.putBoolean("pref_key_sms_delivery_reports", smsReportMode).commit();
            if (this.mDeliverReport != null) {
                this.mDeliverReport.setState(getDeliveryReportResId(deliveryReportIndex));
            }
            return;
        }
        Preference listDivideLine = this.mOtherPrefCategory.findPreference("divider_pref_key_delivery_reports");
        if (listDivideLine != null) {
            this.mOtherPrefCategory.removePreference(listDivideLine);
        }
        this.mOtherPrefCategory.removePreference(this.mDeliverReport);
    }

    public static int getDeliveryReportResId(int deliveryReportState) {
        switch (deliveryReportState) {
            case 1:
                return R.string.text_message;
            case 2:
                return R.string.multimedia_message;
            case 3:
                return R.string.text_multimedia_message;
            default:
                return R.string.smart_sms_setting_status_close;
        }
    }

    private int getDeliveryReportIndex(String deliveryReportStr) {
        if (TextUtils.isEmpty(deliveryReportStr)) {
            return MmsConfig.getDefaultDeliveryReportState();
        }
        if (deliveryReportStr.equals(getString(R.string.smart_sms_setting_status_close))) {
            return 0;
        }
        if (deliveryReportStr.equals(getString(R.string.text_message))) {
            return 1;
        }
        if (deliveryReportStr.equals(getString(R.string.multimedia_message))) {
            return 2;
        }
        return 3;
    }

    private void checkSmsEnable() {
        boolean isSmsEnabled = MmsConfig.isSmsEnabled(this.mActivity);
        if (isSmsEnabled != this.mIsSmsEnabled) {
            this.mIsSmsEnabled = isSmsEnabled;
        }
        updateSmsEnabledState();
        if (this.mActivity instanceof ActivityCallback) {
            ((ActivityCallback) this.mActivity).onCallBack();
        }
    }

    private void updateSmsEnabledState() {
        this.mIsSmsEnabled = MmsConfig.isSmsEnabled(this.mActivity);
        this.mOtherPrefCategory.setEnabled(this.mIsSmsEnabled);
        this.mCommonPrefCategory.setEnabled(this.mIsSmsEnabled);
        if (this.mDisplayModePref != null) {
            this.mDisplayModePref.setEnabled(this.mIsSmsEnabled);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        unRegisterDefSmsAppChanged();
        if (this.mShowSmsStorageDailog != null) {
            this.mShowSmsStorageDailog.setOnDismissListener(null);
            if (this.mShowSmsStorageDailog.isShowing()) {
                this.mShowSmsStorageDailog.dismiss();
            }
            this.mShowSmsStorageDailog = null;
        }
        if (this.mDeliverReportDialog != null && this.mDeliverReportDialog.isShowing()) {
            this.mDeliverReportDialog.dismiss();
        }
        if (this.mCust != null) {
            this.mCust.onDestroy();
        }
    }

    private void setMessagePreferences() {
        Preference listDivideLine;
        if (SystemProperties.getBoolean("ro.config.show_mms_storage", false)) {
            MLog.e("GeneralPreferenceFragment", "ro.config.show_mms_storage set true");
            this.mStorageStatusPref = findPreference("pref_key_storage_status");
        } else {
            MLog.e("GeneralPreferenceFragment", "ro.config.show_mms_storage set false");
            Preference storageStatusPref = findPreference("pref_key_storage_status");
            PreferenceCategory otherOptions = (PreferenceCategory) findPreference("pref_key_other_settings");
            if (!(storageStatusPref == null || otherOptions == null)) {
                listDivideLine = otherOptions.findPreference("divider_pref_key_storage_status");
                if (listDivideLine != null) {
                    otherOptions.removePreference(listDivideLine);
                }
                otherOptions.removePreference(storageStatusPref);
            }
        }
        if (!MmsConfig.isFoldeModeEnabled()) {
            getPreferenceScreen().removePreference((ListPreference) findPreference("pref_key_display_mode"));
        }
        if (this.mRcsGeneralPreferenceFragment != null) {
            this.mRcsGeneralPreferenceFragment.hideDisplayModePref((ListPreference) findPreference("pref_key_display_mode"));
        }
        this.mRiskUriCheck = findPreference("pref_key_risk_url_check");
        this.mRiskUriCheck.setOnPreferenceChangeListener(this);
        if (!Contact.IS_CHINA_REGION) {
            this.mRiskUriCheck.setTitle(R.string.mms_pref_title_risk_url_check_oversea);
            this.mRiskUriCheck.setSummary(R.string.risk_url_check_summary_content_oversea);
        }
        this.mRiskUriCheck.setOnPreferenceChangeListener(this);
        this.mDisplayModePref = (ListPreference) findPreference("pref_key_display_mode");
        if (this.mDisplayModePref != null) {
            int i;
            this.mDisplayModeSummary = this.mActivity.getResources().getStringArray(R.array.display_mode_entries);
            ListPreference listPreference = this.mDisplayModePref;
            String[] strArr = this.mDisplayModeSummary;
            if (PreferenceUtils.getUsingConversation(this.mActivity)) {
                i = 0;
            } else {
                i = 1;
            }
            listPreference.setSummary(strArr[i]);
            this.mDisplayModePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int index = Integer.parseInt(String.valueOf(newValue));
                    GeneralPreferenceFragment.this.mDisplayModePref.setSummary(GeneralPreferenceFragment.this.mDisplayModeSummary[index]);
                    if (index != 0) {
                        Preference listDivideLine = GeneralPreferenceFragment.this.mCommonPrefCategory.findPreference("divider_pref_key_smart_archive_enable");
                        if (listDivideLine != null) {
                            GeneralPreferenceFragment.this.mCommonPrefCategory.removePreference(listDivideLine);
                        }
                        if (GeneralPreferenceFragment.this.mSmartArchiveSetting != null) {
                            GeneralPreferenceFragment.this.mCommonPrefCategory.removePreference(GeneralPreferenceFragment.this.mSmartArchiveSetting);
                        }
                    } else if (MmsConfig.isSupportSmartFolder()) {
                        GeneralPreferenceFragment.this.mCommonPrefCategory.addPreference(GeneralPreferenceFragment.this.mSmartArchiveSetting);
                    }
                    return true;
                }
            });
        }
        this.mSmartArchiveSetting = findPreference("pref_key_smart_archive_enable");
        if (MmsConfig.isSupportSmartFolder()) {
            this.mSmartArchiveSetting.setSummary(SmartArchiveSettingUtils.isSmartArchiveEnabled(this.mActivity) ? R.string.smart_sms_setting_status_open : R.string.smart_sms_setting_status_close);
        } else {
            listDivideLine = this.mCommonPrefCategory.findPreference("divider_pref_key_smart_archive_enable");
            if (listDivideLine != null) {
                this.mCommonPrefCategory.removePreference(listDivideLine);
            }
            if (this.mSmartArchiveSetting != null) {
                this.mCommonPrefCategory.removePreference(this.mSmartArchiveSetting);
            }
        }
        this.mSmsRecoveryPref = (SwitchPreference) findPreference("pref_key_recovery_support");
        if (MmsConfig.isSmsRecyclerEnable()) {
            this.mSmsRecoveryPref.setOnPreferenceChangeListener(this);
            checkRecoveryStatus();
        } else {
            listDivideLine = this.mCommonPrefCategory.findPreference("divider_pref_key_recovery_support");
            if (listDivideLine != null) {
                this.mCommonPrefCategory.removePreference(listDivideLine);
            }
            this.mCommonPrefCategory.removePreference(this.mSmsRecoveryPref);
        }
        this.mSmartSmsSetting = findPreference("pref_key_smart_sms_settings");
        this.mCancelSmsSetting = findPreference("pref_key_cancel_send_enable");
        this.mCancelSmsSetting.setOnPreferenceChangeListener(this);
        this.mCancelSmsSetting.setSummary(localNumberFormat(getResources().getString(R.string.mms_pref_cancel_send_summary_new, new Object[]{Integer.valueOf(6)})));
        this.mPinupUnreadMessage = findPreference("pref_key_pinup_unread_message_enable");
        this.mPinupUnreadMessage.setOnPreferenceChangeListener(this);
        if (!MmsConfig.getSupportSmartSmsFeature()) {
            listDivideLine = this.mCommonPrefCategory.findPreference("divider_pref_key_smart_sms_settings");
            if (listDivideLine != null) {
                this.mCommonPrefCategory.removePreference(listDivideLine);
            }
            if (this.mSmartSmsSetting != null) {
                this.mCommonPrefCategory.removePreference(this.mSmartSmsSetting);
            }
        }
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == this.mRingTongAndVibrate) {
            startActivity(new Intent(this.mActivity, RingToneAndVibrateSettings.class));
        } else if (preference == this.mSmartArchiveSetting) {
            StatisticalHelper.incrementReportCount(this.mActivity, 2168);
            startActivity(new Intent(this.mActivity, SmartArchiveSettings.class));
        } else if (preference == this.mSmartSmsSetting) {
            StatisticalHelper.incrementReportCount(this.mActivity, 2166);
            if (MmsConfig.getSupportSmartSmsFeature()) {
                startActivity(new Intent(this.mActivity, SmartSmsSettingActivity.class));
            }
        } else if (preference == this.mMmsAdvenceSetting) {
            if (this.mActivity instanceof ActivityCallback) {
                StatisticalHelper.incrementReportCount(this.mActivity, 2120);
                ((ActivityCallback) this.mActivity).switchFragment(1);
            }
        } else if (preference == this.mDeliverReport) {
            StatisticalHelper.incrementReportCount(this.mActivity, 2169);
            showDeliverReportDialog();
        } else if (preference == this.mStorageStatusPref && SystemProperties.getBoolean("ro.config.show_mms_storage", false)) {
            new AsyncTask<Void, Void, String>() {
                protected String doInBackground(Void... params) {
                    return MessageUtils.getStorageStatus(GeneralPreferenceFragment.this.mActivity.getApplicationContext());
                }

                protected void onPostExecute(String result) {
                    if (!GeneralPreferenceFragment.this.mActivity.isFinishing()) {
                        if (GeneralPreferenceFragment.this.mShowSmsStorageDailog == null) {
                            GeneralPreferenceFragment.this.mShowSmsStorageDailog = new Builder(GeneralPreferenceFragment.this.mActivity).setTitle(R.string.pref_title_storage_status).setIcon(17301659).setMessage(result).setPositiveButton(17039370, null).setCancelable(true).create();
                        }
                        if (GeneralPreferenceFragment.this.mShowSmsStorageDailog != null) {
                            GeneralPreferenceFragment.this.mShowSmsStorageDailog.setOnDismissListener(new OnDismissListener() {
                                public void onDismiss(DialogInterface dialogInterface) {
                                    GeneralPreferenceFragment.this.mShowSmsStorageDailog = null;
                                }
                            });
                            if (!GeneralPreferenceFragment.this.mShowSmsStorageDailog.isShowing()) {
                                GeneralPreferenceFragment.this.mShowSmsStorageDailog.setTitle(R.string.pref_title_storage_status);
                                GeneralPreferenceFragment.this.mShowSmsStorageDailog.show();
                            }
                        }
                    }
                }
            }.executeOnExecutor(ThreadEx.getDefaultExecutor(), new Void[0]);
        }
        this.mCryptoGeneralPreferenceFragment.onPreferenceTreeClick(this.mActivity, preference);
        if (this.mRcsGeneralPreferenceFragment != null) {
            this.mRcsGeneralPreferenceFragment.onPreferenceTreeClick(this.mActivity, preference);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void showDeliverReportDialog() {
        if (this.mDeliverReportDialog == null || !this.mDeliverReportDialog.isShowing()) {
            int itemTextColor = this.mActivity.getResources().getColor(R.color.dialog_list_text_color);
            String str = getString(R.string.text_message);
            SpannableStringBuilder textMessage = new SpannableStringBuilder(str);
            textMessage.setSpan(new ForegroundColorSpan(itemTextColor), 0, str.length(), 18);
            str = getString(R.string.multimedia_message);
            new SpannableStringBuilder(str).setSpan(new ForegroundColorSpan(itemTextColor), 0, str.length(), 18);
            CharSequence[] reportChoices = new CharSequence[]{textMessage, multiMediaMessage};
            if (!(this.mCust == null || MmsConfig.getMmsEnabled())) {
                reportChoices = this.mCust.getCustDeliveryReportItem(reportChoices, textMessage);
            }
            Builder builder = new Builder(this.mActivity);
            builder.setTitle(R.string.pref_title_mms_delivery_reports_content);
            builder.setMultiChoiceItems(reportChoices, this.mDeliverReportChecked, this.reportListener);
            builder.setPositiveButton(R.string.yes, this.mPositiveListener);
            builder.setNegativeButton(R.string.no, this.mNegativeListener);
            builder.setOnDismissListener(this.mDismissListener);
            this.mDeliverReportDialog = builder.show();
        }
    }

    private void updateDeliverReport() {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mActivity).edit();
        int deliveryReportIndex = 0;
        boolean smsReportMode = false;
        boolean mmsReportMode = false;
        int resId = R.string.smart_sms_setting_status_close;
        if (this.mDeliverReportChecked[0] && !this.mDeliverReportChecked[1]) {
            resId = R.string.text_message;
            smsReportMode = true;
            deliveryReportIndex = 1;
        } else if (!this.mDeliverReportChecked[0] && this.mDeliverReportChecked[1]) {
            resId = R.string.multimedia_message;
            mmsReportMode = true;
            deliveryReportIndex = 2;
        } else if (this.mDeliverReportChecked[0] && this.mDeliverReportChecked[1]) {
            resId = R.string.text_multimedia_message;
            smsReportMode = true;
            mmsReportMode = true;
            deliveryReportIndex = 3;
        }
        if (this.mDeliverReportCheckedOld[0] != this.mDeliverReportChecked[0]) {
            StatisticalHelper.reportEvent(this.mActivity, 2076, this.mDeliverReportChecked[0] ? "on" : "off");
        }
        if (this.mDeliverReportCheckedOld[1] != this.mDeliverReportChecked[1]) {
            StatisticalHelper.reportEvent(this.mActivity, 2081, this.mDeliverReportChecked[1] ? "on" : "off");
        }
        this.mDeliverReport.setState(resId);
        editor.putInt("pref_key_delivery_reports", deliveryReportIndex);
        editor.putBoolean("pref_key_mms_delivery_reports", mmsReportMode);
        editor.putBoolean("pref_key_sms_delivery_reports", smsReportMode);
        editor.putBoolean("pref_key_sms_delivery_reports_sub0", smsReportMode);
        editor.putBoolean("pref_key_sms_delivery_reports_sub1", smsReportMode);
        editor.commit();
        this.mDeliverReportCheckedOld[0] = this.mDeliverReportChecked[0];
        this.mDeliverReportCheckedOld[1] = this.mDeliverReportChecked[1];
    }

    protected void restoreDefaultPreferences() {
        boolean isSmsRecycleChecked = PreferenceUtils.isSmsRecoveryEnable(this.mActivity);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mActivity);
        String str = null;
        String custMessageRing = "no_cust_message_ring";
        if (this.mRcsGeneralPreferenceFragment != null && this.mRcsGeneralPreferenceFragment.getEnableCotaFeature()) {
            str = this.mRcsGeneralPreferenceFragment.getGeneralDefaultsBtlDigest(sp);
            custMessageRing = this.mRcsGeneralPreferenceFragment.getKeyCustMessageRing(sp);
        }
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mActivity).edit().clear();
        editor.putBoolean("pref_key_auto_delete", false).commit();
        if (this.mRcsGeneralPreferenceFragment != null && this.mRcsGeneralPreferenceFragment.getEnableCotaFeature()) {
            this.mRcsGeneralPreferenceFragment.setGeneralDefaultsBtlDigest(sp, str);
            this.mRcsGeneralPreferenceFragment.restoreKeyCustMessageRing(sp, custMessageRing);
        }
        String currentMccmnc = MccMncConfig.getDefault().getOperator();
        if (MccMncConfig.isValideOperator(currentMccmnc)) {
            editor.putString("pref_last_mccmnc", currentMccmnc);
        }
        editor.putBoolean("pref_key_enable_popup_message", MmsConfig.isEnablePopupMessage()).commit();
        editor.putString("pref_key_play_mode", MmsConfig.getPrefPlaymode());
        if (MmsConfig.isSupportSmartFolder()) {
            SmartArchiveSettingUtils.restoreSamrtArchiveSettings();
        }
        editor.apply();
        MmsConfig.setCustomDefaultValues(PreferenceManager.getDefaultSharedPreferences(this.mActivity));
        restoreDefaultRingtone();
        setPreferenceScreen(null);
        addPreferencesFromResource(R.xml.preferences);
        this.mOtherPrefCategory = (PreferenceCategory) findPreference("pref_key_other_settings");
        this.mCommonPrefCategory = (PreferenceCategory) findPreference("pref_key_common_settings");
        if (this.mRcsGeneralPreferenceFragment != null) {
            this.mRcsGeneralPreferenceFragment.restoreDefaultPreferences();
        }
        this.mRingTongAndVibrate = findPreference("pref_key_ringTong_vibrate");
        setMessagePreferences();
        this.mPlayModePref = (ListPreference) findPreference("pref_key_play_mode");
        if (this.mPlayModePref != null) {
            this.mPlayModePref.setOnPreferenceChangeListener(this.playModePrefListener);
        }
        updateSmsEnabledState();
        restoreDefaultMMSRetrievalDuringRoamingMultiSim(this.mActivity.getApplicationContext());
        if (isSmsRecycleChecked) {
            showSmsRecycleConfirmDialog(!isSmsRecycleChecked);
        }
        if (MmsConfig.getSupportSmartSmsFeature()) {
            SmartSmsSdkUtil.restoreSettingParam(this.mActivity);
        }
        this.mCryptoGeneralPreferenceFragment.restoreDefaultCrypto(this.mCommonPrefCategory);
        this.mDeliverReport = (HwPreference) findPreference("pref_key_delivery_reports");
        if (this.mDeliverReport != null) {
            int deliverReportState = MmsConfig.getDefaultDeliveryReportState();
            if (this.mCust != null) {
                deliverReportState = this.mCust.getCustDeliveryReportState(this.mActivity.getApplicationContext(), deliverReportState);
            }
            if (this.mCust == null || !this.mCust.isHideDeliveryReportsItem()) {
                initDeliverReportState(deliverReportState);
            } else {
                this.mCust.hideDeliveryReportsItem(this.mOtherPrefCategory, this.mDeliverReport);
            }
        }
        this.mMmsAdvenceSetting = findPreference("pref_key_mms_advanced_settings");
        hideOptionsForSecondaryUser();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Context context = MmsApp.getApplication();
        if (!(newValue instanceof Boolean)) {
            return false;
        }
        boolean isChecked = ((Boolean) newValue).booleanValue();
        String str;
        if (preference == this.mSmsRecoveryPref) {
            if (isChecked) {
                updateRecoveryStatus(isChecked);
            } else {
                showSmsRecycleConfirmDialog(isChecked);
            }
            if (isChecked) {
                str = "on";
            } else {
                str = "off";
            }
            StatisticalHelper.reportEvent(context, 2134, str);
            return false;
        } else if (preference == this.mSmartSmsSetting) {
            if (isChecked) {
                str = "on";
            } else {
                str = "off";
            }
            StatisticalHelper.reportEvent(context, 2121, str);
            return true;
        } else if (preference == this.mCancelSmsSetting) {
            if (isChecked) {
                str = "on";
            } else {
                str = "off";
            }
            StatisticalHelper.reportEvent(context, 2132, str);
            return true;
        } else if (preference == this.mRiskUriCheck) {
            if (isChecked) {
                str = "on";
            } else {
                str = "off";
            }
            StatisticalHelper.reportEvent(context, 2222, str);
            PreferenceUtils.setFunctionTipsNoShowAgain(getActivity(), true);
            return true;
        } else if (preference != this.mPinupUnreadMessage) {
            return false;
        } else {
            if (isChecked) {
                str = "on";
            } else {
                str = "off";
            }
            StatisticalHelper.reportEvent(context, 2225, str);
            return true;
        }
    }

    private void showSmsRecycleConfirmDialog(final boolean isChecked) {
        MessageUtils.setButtonTextColor(new Builder(this.mActivity).setIcon(17301543).setTitle(R.string.close_trash_sms_box_title).setCancelable(true).setMessage(R.string.close_trash_sms_box_body).setPositiveButton(R.string.close_trash_sms_box_btn, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                StatisticalHelper.reportEvent(GeneralPreferenceFragment.this.mActivity, 2134, "off");
                GeneralPreferenceFragment.this.updateRecoveryStatus(isChecked);
                dialog.dismiss();
            }
        }).setNegativeButton(R.string.no, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                GeneralPreferenceFragment.this.syncRecoveryStatus(true);
                dialog.dismiss();
            }
        }).setOnCancelListener(CommonLisener.getDismissCancelListener()).show(), -1, getResources().getColor(R.color.mms_unread_text_color));
    }

    private void updateRecoveryStatus(final boolean isChecked) {
        new CallRequest(getActivity(), "method_enable_recovery") {
            protected void setParam() {
                this.mRequest.putBoolean("recovery_status", isChecked);
            }

            protected void onCallBack() {
                boolean enabled = this.mResult.getBoolean("recovery_status", false);
                if (enabled == isChecked) {
                    GeneralPreferenceFragment.this.syncRecoveryStatus(enabled);
                }
            }
        }.makeCall();
        if (!isChecked) {
            ProviderCallUtils.cleanTrashBox(this.mActivity.getApplicationContext(), -1);
        }
    }

    private void checkRecoveryStatus() {
        new CallRequest(getActivity(), "method_is_support_recovery") {
            protected void onCallBack() {
                GeneralPreferenceFragment.this.syncRecoveryStatus(this.mResult.getBoolean("recovery_status", false));
            }
        }.makeCall();
    }

    private void syncRecoveryStatus(boolean enabled) {
        this.mSmsRecoveryPref.setOnPreferenceChangeListener(null);
        this.mSmsRecoveryPref.setChecked(enabled);
        this.mSmsRecoveryPref.setOnPreferenceChangeListener(this);
        if (enabled) {
            Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mActivity).edit();
            editor.putBoolean("pref_sms_recycle_not_show_again", enabled);
            editor.apply();
        }
    }

    public void registerDefSmsAppChanged() {
        if (this.mDefSmsAppChangedReceiver == null) {
            this.mDefSmsAppChangedReceiver = new DefaultSmsAppChangedReceiver(new HwDefSmsAppChangedListener() {
                public void onDefSmsAppChanged() {
                    GeneralPreferenceFragment.this.checkSmsEnable();
                }
            });
        }
        this.mActivity.registerReceiver(this.mDefSmsAppChangedReceiver, new IntentFilter("com.huawei.mms.default_smsapp_changed"), permission.DEFAULTCHANGED_PERMISSION, null);
    }

    public void unRegisterDefSmsAppChanged() {
        if (this.mDefSmsAppChangedReceiver != null) {
            this.mActivity.unregisterReceiver(this.mDefSmsAppChangedReceiver);
        }
    }

    public static void restoreDefaultMMSRetrievalDuringRoamingMultiSim(Context context) {
        if (MessageUtils.isMultiSimEnabled()) {
            MessageUtils.setRoamingAutoRetrieveValue(context.getContentResolver(), 1, MmsConfig.getDefaultMMSRetrievalDuringRoamingCard1());
            MessageUtils.setRoamingAutoRetrieveValue(context.getContentResolver(), 2, MmsConfig.getDefaultMMSRetrievalDuringRoamingCard2());
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        MLog.d("GeneralPreferenceFragment", "onActivityResult");
    }

    private void restoreDefaultRingtone() {
        boolean isDefaultFollowNotification;
        if (HwMessageUtils.getDefaultFollowNotificationState(this.mActivity) == 2) {
            isDefaultFollowNotification = false;
        } else {
            isDefaultFollowNotification = true;
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mActivity);
        if (MessageUtils.isMultiSimEnabled()) {
            sp.edit().putBoolean("pref_mms_is_follow_notification_sub0", isDefaultFollowNotification).commit();
            sp.edit().putBoolean("pref_mms_is_follow_notification_sub1", isDefaultFollowNotification).commit();
            if (!isDefaultFollowNotification) {
                String defaultCustomRingtone = MessageUtils.getDefaultRintoneStr(this.mActivity);
                MmsConfig.setRingToneUriToDatabase(this.mActivity, defaultCustomRingtone, 0);
                MmsConfig.setRingToneUriToDatabase(this.mActivity, defaultCustomRingtone, 1);
                return;
            }
            return;
        }
        sp.edit().putBoolean("pref_mms_is_follow_notification", isDefaultFollowNotification).commit();
        if (!isDefaultFollowNotification) {
            MmsConfig.setRingToneUriToDatabase(this.mActivity, MessageUtils.getDefaultRintoneStr(this.mActivity));
        }
    }

    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        int width = ((WindowManager) getActivity().getSystemService("window")).getDefaultDisplay().getWidth();
        if (!enter) {
            return ObjectAnimator.ofFloat(this, "translationX", new float[]{0.0f, (float) (0 - width)});
        } else if (this.mIsFirstEnter) {
            this.mIsFirstEnter = false;
            return super.onCreateAnimator(transit, enter, nextAnim);
        } else {
            return ObjectAnimator.ofFloat(this, "translationX", new float[]{(float) (0 - width), 0.0f});
        }
    }

    public void setIsFirstEnter(boolean first) {
        this.mIsFirstEnter = first;
    }

    private void hideOptionsForSecondaryUser() {
        if (OsUtil.isAtLeastL() && OsUtil.isSecondaryUser()) {
            Preference listDivideLine;
            MLog.d("GeneralPreferenceFragment", "Hide options for secondary user");
            if (!(this.mCommonPrefCategory == null || this.mSmartArchiveSetting == null)) {
                listDivideLine = this.mCommonPrefCategory.findPreference("divider_pref_key_smart_archive_enable");
                if (listDivideLine != null) {
                    this.mCommonPrefCategory.removePreference(listDivideLine);
                }
                MLog.d("GeneralPreferenceFragment", "mSmartArchiveSetting is removed " + this.mCommonPrefCategory.removePreference(this.mSmartArchiveSetting));
            }
            if (this.mOtherPrefCategory != null) {
                Preference deliverPref = findPreference("pref_key_delivery_reports");
                if (deliverPref != null) {
                    listDivideLine = this.mOtherPrefCategory.findPreference("divider_pref_key_delivery_reports");
                    if (listDivideLine != null) {
                        this.mOtherPrefCategory.removePreference(listDivideLine);
                    }
                    MLog.d("GeneralPreferenceFragment", "deliverPref is removed " + this.mOtherPrefCategory.removePreference(deliverPref));
                }
                if (this.mMmsAdvenceSetting != null) {
                    listDivideLine = this.mOtherPrefCategory.findPreference("divider_pref_key_mms_advanced_settings");
                    if (listDivideLine != null) {
                        this.mOtherPrefCategory.removePreference(listDivideLine);
                    }
                    MLog.d("GeneralPreferenceFragment", "mMmsAdvenceSetting is removed " + this.mOtherPrefCategory.removePreference(this.mMmsAdvenceSetting));
                }
            }
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListView list = (ListView) getView().findViewById(16908298);
        list.setDivider(null);
        this.mFooterView = LayoutInflater.from(getActivity()).inflate(R.layout.blank_footer_view, list, false);
        list.setFooterDividersEnabled(false);
        list.addFooterView(this.mFooterView, null, false);
        updateFooterViewHeight(null);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateFooterViewHeight(newConfig);
    }

    private void updateFooterViewHeight(Configuration newConfig) {
        if (this.mFooterView != null) {
            boolean isLandscape = newConfig == null ? getResources().getConfiguration().orientation == 2 : 2 == newConfig.orientation;
            LayoutParams lp = this.mFooterView.getLayoutParams();
            int dimension = (!isLandscape || isInMultiWindowMode()) ? (int) getResources().getDimension(R.dimen.toolbar_footer_height) : 0;
            lp.height = dimension;
            this.mFooterView.setLayoutParams(lp);
        }
    }

    private boolean isInMultiWindowMode() {
        if (getActivity() == null) {
            return false;
        }
        return getActivity().isInMultiWindowMode();
    }
}
