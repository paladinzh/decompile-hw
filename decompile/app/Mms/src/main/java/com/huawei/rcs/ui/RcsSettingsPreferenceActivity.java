package com.huawei.rcs.ui;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.provider.Telephony.Sms;
import android.telephony.MSimTelephonyManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import com.android.mms.ui.EmuiSwitchPreference;
import com.android.rcs.RcsCommonConfig;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.HwPreferenceActivity;
import com.huawei.rcs.commonInterface.IfMsgplus;
import com.huawei.rcs.utils.RcsProfile;
import com.huawei.rcs.utils.RcsProfileUtils;
import com.huawei.rcs.utils.RcsTransaction;

public class RcsSettingsPreferenceActivity extends HwPreferenceActivity implements OnPreferenceChangeListener {
    private static boolean mSupportAllCharacters = RcsProfile.isSupportAllCharacters();
    public OnPreferenceClickListener ftAutoAcceptListener = new OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
            RcsSettingsPreferenceActivity.this.showAutoRetrievialDialog();
            return true;
        }
    };
    public OnPreferenceChangeListener ftCropNotAskmeSetListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            RcsProfileUtils.saveRcsCropImageStatus(RcsSettingsPreferenceActivity.this, ((Boolean) newValue).booleanValue());
            return true;
        }
    };
    public OnPreferenceChangeListener groupInviteAcceptListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            RcsProfile.setGroupInviteAutoAccept(RcsSettingsPreferenceActivity.this, ((Boolean) newValue).booleanValue() ? 1 : 0);
            return true;
        }
    };
    public OnPreferenceChangeListener groupMessageDeliveryRequestListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            RcsSettingsPreferenceActivity.this.setGroupChatDeliveryStatus(((Boolean) newValue).booleanValue());
            return true;
        }
    };
    public OnPreferenceChangeListener imDeliveryRequestListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Editor editor = PreferenceManager.getDefaultSharedPreferences(RcsSettingsPreferenceActivity.this).edit();
            boolean request = ((Boolean) newValue).booleanValue();
            MLog.i("RcsSettingsPreferenceActivity", "imDeliveryRequestListener request now is " + request);
            editor.putBoolean("pref_key_im_enable_delivery_report", request);
            editor.commit();
            IfMsgplus aMsgPlus = RcsProfile.getRcsService();
            if (aMsgPlus != null) {
                try {
                    aMsgPlus.setRequestDeliveryStatus(request);
                } catch (RemoteException e) {
                    MLog.e("RcsSettingsPreferenceActivity", "imDeliveryRequestListener setRequestDeliveryStatus error");
                }
            }
            return true;
        }
    };
    public OnPreferenceChangeListener imDisplayReportListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Editor editor = PreferenceManager.getDefaultSharedPreferences(RcsSettingsPreferenceActivity.this).edit();
            boolean request = ((Boolean) newValue).booleanValue();
            MLog.i("RcsSettingsPreferenceActivity", "imDisplayRequestListener request now is " + request);
            editor.putBoolean("pref_key_im_enable_display_report", request);
            editor.commit();
            IfMsgplus aMsgPlus = RcsProfile.getRcsService();
            if (aMsgPlus != null) {
                try {
                    aMsgPlus.setRequestDisplayStatus(request);
                    aMsgPlus.setAllowSendDisplayStatus(request);
                } catch (RemoteException e) {
                    MLog.e("RcsSettingsPreferenceActivity", "imDisplayReportListener setRequestDisplayStatus error");
                }
            }
            return true;
        }
    };
    public OnPreferenceChangeListener imEnableListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Editor editor = PreferenceManager.getDefaultSharedPreferences(RcsSettingsPreferenceActivity.this).edit();
            boolean isImEnable = ((Boolean) newValue).booleanValue();
            MLog.i("RcsSettingsPreferenceActivity", "imEnableListener isImEnable now is " + newValue);
            editor.putBoolean("pref_key_im", isImEnable);
            editor.commit();
            if (isImEnable) {
                RcsSettingsPreferenceActivity.this.mImDeliveryReportRequest.setEnabled(true);
                RcsSettingsPreferenceActivity.this.mImDisplayReport.setEnabled(true);
                RcsSettingsPreferenceActivity.this.mGroupDeliveryReportRequest.setEnabled(true);
                RcsSettingsPreferenceActivity.this.mGroupInviteAccept.setEnabled(true);
            } else {
                RcsSettingsPreferenceActivity.this.mImDeliveryReportRequest.setEnabled(false);
                RcsSettingsPreferenceActivity.this.mImDisplayReport.setEnabled(false);
                RcsSettingsPreferenceActivity.this.mGroupDeliveryReportRequest.setEnabled(false);
                RcsSettingsPreferenceActivity.this.mGroupInviteAccept.setEnabled(false);
            }
            return true;
        }
    };
    public OnPreferenceChangeListener imThreadMixDisplayListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (((Boolean) newValue).booleanValue()) {
                RcsProfile.setIMThreadDisplayMergeStatus(RcsSettingsPreferenceActivity.this, 1);
            } else {
                RcsProfile.setIMThreadDisplayMergeStatus(RcsSettingsPreferenceActivity.this, 0);
            }
            return true;
        }
    };
    private boolean isRcsOn = RcsCommonConfig.isRCSSwitchOn();
    private AlertDialog mAutoRetrieviaDlg;
    private Preference mFtAutoAccept;
    private SwitchPreference mFtCropNotAskmeSet;
    private SwitchPreference mGroupDeliveryReportRequest;
    private SwitchPreference mGroupInviteAccept;
    private SwitchPreference mImDeliveryReportRequest;
    private SwitchPreference mImDisplayReport;
    private SwitchPreference mImEnable;
    private SwitchPreference mImThreadMixDisplay;
    private LoginStatusReceiver mLoginStatusReceiver;
    private PreferenceScreen mMessageInfo;
    private SwitchPreference mMmsRcsPref;
    private boolean mNicknameEnable = RcsProfile.isGroupChatNicknameEnabled();
    private EditText mNicknameEt;
    private Button mNicknamePositiveButton;
    private PhoneStateListener[] mPhoneStateListener;
    private Preference mRCSNickname;
    public OnPreferenceClickListener mRCSNicknameClickListener = new OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
            View view = View.inflate(RcsSettingsPreferenceActivity.this, R.layout.rcs_nickname_dialog, null);
            if (view == null) {
                MLog.w("RcsSettingsPreferenceActivity", "mRCSNicknameClickListener layout inflate fail");
                return true;
            }
            final AlertDialog setNickNameDialog = new Builder(RcsSettingsPreferenceActivity.this).setTitle(R.string.nickname_dialog_title).setView(view).setCancelable(false).setPositiveButton(R.string.nickname_dialog_confirm, null).setNegativeButton(R.string.nickname_dialog_cancel, null).create();
            setNickNameDialog.setCancelable(true);
            RcsSettingsPreferenceActivity.this.mNicknameEt = (EditText) view.findViewById(R.id.et_nickname);
            RcsSettingsPreferenceActivity.this.mNicknameEt.addTextChangedListener(RcsSettingsPreferenceActivity.this.nickNameTextWatcher);
            IfMsgplus aMsgPlus = RcsProfile.getRcsService();
            if (aMsgPlus != null) {
                try {
                    String groupNickname = aMsgPlus.getGroupNickname();
                    if (TextUtils.isEmpty(groupNickname)) {
                        groupNickname = aMsgPlus.getLocalPhoneNumber();
                        if (TextUtils.isEmpty(groupNickname)) {
                            groupNickname = "";
                        }
                    }
                    RcsSettingsPreferenceActivity.this.mNicknameEt.setText(groupNickname);
                } catch (RemoteException e) {
                    MLog.e("RcsSettingsPreferenceActivity", e.toString());
                }
            }
            String nicknameStr = RcsSettingsPreferenceActivity.this.mNicknameEt.getText().toString();
            if (!TextUtils.isEmpty(nicknameStr)) {
                RcsSettingsPreferenceActivity.this.mNicknameEt.setSelection(nicknameStr.length());
            }
            RcsSettingsPreferenceActivity.this.mNicknameEt.requestFocus();
            setNickNameDialog.getWindow().setSoftInputMode(37);
            setNickNameDialog.show();
            RcsSettingsPreferenceActivity.this.mNicknamePositiveButton = setNickNameDialog.getButton(-1);
            RcsSettingsPreferenceActivity.this.mNicknameEt.selectAll();
            RcsSettingsPreferenceActivity.this.mNicknamePositiveButton.setEnabled(true);
            RcsSettingsPreferenceActivity.this.mNicknamePositiveButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    String sNickname = RcsSettingsPreferenceActivity.this.mNicknameEt.getText().toString();
                    if (sNickname.length() != 0) {
                        sNickname = sNickname.trim();
                        if (TextUtils.isEmpty(sNickname)) {
                            Toast.makeText(RcsSettingsPreferenceActivity.this, R.string.nickname_isEmpty, 0).show();
                            return;
                        }
                        setNickNameDialog.dismiss();
                        int reason = 1;
                        try {
                            reason = RcsProfile.getRcsService().saveNickname(sNickname);
                        } catch (Exception e) {
                            MLog.e("RcsSettingsPreferenceActivity", e.toString());
                        }
                        RcsSettingsPreferenceActivity.this.setGroupNicknameValue();
                        MLog.d("RcsSettingsPreferenceActivity", "save nickname reason = " + reason);
                    }
                }
            });
            setNickNameDialog.getButton(-2).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    setNickNameDialog.dismiss();
                }
            });
            return false;
        }
    };
    private SimCardStateChangeReceive mSimCardStateChangeReceive;
    private boolean mSimIsPresent = false;
    private boolean mSimNetWorkIsPresent = false;
    private int mSub = 0;
    private TelephonyManager mTelephonyManager;
    private TextWatcher nickNameTextWatcher = new TextWatcher() {
        public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
            if (!RcsProfile.isSupportAllCharacters()) {
                RcsSettingsPreferenceActivity.this.showOrHideNicknamePrompt(!RcsSettingsPreferenceActivity.this.checkNicknameFormat(s.toString()));
            }
        }

        public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
        }

        public void afterTextChanged(Editable editable) {
            if (editable != null) {
                int length = editable.toString().length();
                if (length == 0) {
                    if (RcsSettingsPreferenceActivity.this.mNicknamePositiveButton != null) {
                        RcsSettingsPreferenceActivity.this.mNicknamePositiveButton.setEnabled(false);
                    }
                } else if (length > 24) {
                    editable.replace(24, editable.toString().length(), "");
                }
            }
        }
    };
    private OnPreferenceChangeListener rcsSwitcherListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (RcsSettingsPreferenceActivity.this.mMmsRcsPref.isChecked()) {
                RcsSettingsPreferenceActivity.this.setRcsSwitchStatus(0);
                RcsSettingsPreferenceActivity.this.mMmsRcsPref.setChecked(false);
                RcsSettingsPreferenceActivity.this.setIMStatus();
            } else {
                RcsSettingsPreferenceActivity.this.mMmsRcsPref.setChecked(true);
                RcsSettingsPreferenceActivity.this.updateRcsSwitchState();
                RcsSettingsPreferenceActivity.this.setRcsSwitchStatus(1);
            }
            return true;
        }
    };

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
            CharSequence[] titles = RcsSettingsPreferenceActivity.this.getResources().getStringArray(R.array.prefEntries_mms_auto_receive);
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
            String autoRetrievalAnyTime = RcsSettingsPreferenceActivity.this.getString(R.string.mms_auto_retrieval_any_time);
            if (convertView == null) {
                convertView = ((LayoutInflater) RcsSettingsPreferenceActivity.this.getSystemService("layout_inflater")).inflate(R.layout.mms_list_preference_item, null);
                this.mHolder = new ViewHolder();
                this.mHolder.mListTitle = (TextView) convertView.findViewById(R.id.list_title);
                this.mHolder.mListSummary = (TextView) convertView.findViewById(R.id.list_summary);
                this.mHolder.mRadio = (RadioButton) convertView.findViewById(R.id.list_radio);
                convertView.setTag(this.mHolder);
            } else {
                this.mHolder = (ViewHolder) convertView.getTag();
            }
            if (PreferenceManager.getDefaultSharedPreferences(RcsSettingsPreferenceActivity.this).getInt("autoRecieveFile", 1) == position) {
                this.mHolder.mRadio.setChecked(true);
            } else {
                this.mHolder.mRadio.setChecked(false);
            }
            if (this.mDatas[position].mTitle.toString().equals(autoRetrievalAnyTime)) {
                this.mHolder.mListTitle.setText(this.mDatas[position].mTitle);
                this.mHolder.mListSummary.setText(RcsSettingsPreferenceActivity.this.getString(R.string.auto_retrieval_fee));
                this.mHolder.mListSummary.setVisibility(0);
            } else {
                this.mHolder.mListTitle.setText(this.mDatas[position].mTitle);
                this.mHolder.mListSummary.setVisibility(8);
            }
            return convertView;
        }
    }

    public class LoginStatusReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if ("com.huawei.rcs.loginstatus".equals(intent.getAction())) {
                RcsSettingsPreferenceActivity.this.setGroupNicknameValue();
                MLog.d("RcsSettingsPreferenceActivity", "LoginStatusReceiver-login status changed,initPreference");
                RcsSettingsPreferenceActivity.this.updateRcsSwitchState();
            }
        }
    }

    class SimCardStateChangeReceive extends BroadcastReceiver {
        SimCardStateChangeReceive() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            MLog.d("RcsSettingsPreferenceActivity", "onReceive action :" + action);
            if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                MLog.d("RcsSettingsPreferenceActivity", "sim card status change,setRcsSettingStatus");
                RcsSettingsPreferenceActivity.this.setRcsSettingStatus();
                RcsSettingsPreferenceActivity.this.updateRcsSwitchState();
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.rcs_message);
        actionBar.setDisplayHomeAsUpEnabled(true);
        ((ListView) findViewById(16908298)).setDivider(null);
        registerBroadcast();
        initPreference();
        setMessagePreferences();
        initCustGeneral();
    }

    public void onResume() {
        super.onResume();
        new Handler().post(new Runnable() {
            public void run() {
                RcsSettingsPreferenceActivity.this.initFtPreferenceForFt();
                RcsSettingsPreferenceActivity.this.refreshFtAutoAccept();
            }
        });
        if (this.mImThreadMixDisplay != null) {
            this.mImThreadMixDisplay.setChecked(true);
        }
        boolean bRcsStatus = getRcsSwitchStatus();
        if (this.mMmsRcsPref != null) {
            this.mMmsRcsPref.setChecked(bRcsStatus);
        }
        setRcsSettingStatus();
        updateRcsSwitchState();
    }

    public void onDestroy() {
        super.onDestroy();
        unregisterBroadcast();
        unregisterReceiver(this.mSimCardStateChangeReceive);
        unregisterPhoneListener();
    }

    public boolean onPreferenceChange(Preference preference, Object enable) {
        return false;
    }

    private void initCustGeneral() {
        this.mTelephonyManager = new TelephonyManager(getApplicationContext());
        IntentFilter simCardIntentFilter = new IntentFilter();
        simCardIntentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        this.mSimCardStateChangeReceive = new SimCardStateChangeReceive();
        registerReceiver(this.mSimCardStateChangeReceive, simCardIntentFilter);
        registerPhoneListener();
    }

    private PhoneStateListener getPhoneStateListener(final int sub) {
        return new PhoneStateListener(sub) {
            public void onServiceStateChanged(ServiceState serviceState) {
                if (serviceState != null) {
                    MLog.d("RcsSettingsPreferenceActivity", "custMms:getPhoneStateListener:onServiceStateChanged getPhoneStateListener received on subscription :" + sub + " state=" + serviceState.getState());
                    if (sub == 0) {
                        if (serviceState.getState() == 0) {
                            RcsSettingsPreferenceActivity.this.mSimNetWorkIsPresent = true;
                        } else {
                            RcsSettingsPreferenceActivity.this.mSimNetWorkIsPresent = false;
                        }
                        MLog.d("RcsSettingsPreferenceActivity", "custMms:phoneStateListener: mSimNetWorkIsPresent=" + RcsSettingsPreferenceActivity.this.mSimNetWorkIsPresent);
                        RcsSettingsPreferenceActivity.this.setRcsSettingStatus();
                        RcsSettingsPreferenceActivity.this.updateRcsSwitchState();
                    } else {
                        return;
                    }
                }
                MLog.d("RcsSettingsPreferenceActivity", "custMms:getPhoneStateListener:serviceState = null");
            }
        };
    }

    private void registerPhoneListener() {
        this.mSub = MSimTelephonyManager.getDefault().getPhoneCount();
        this.mPhoneStateListener = new PhoneStateListener[this.mSub];
        if (this.mTelephonyManager == null) {
            MLog.d("RcsSettingsPreferenceActivity", "custMms:registerPhoneListener:mPhone = null");
            return;
        }
        for (int i = 0; i < this.mSub; i++) {
            this.mPhoneStateListener[i] = getPhoneStateListener(i);
            this.mTelephonyManager.listen(this.mPhoneStateListener[i], 1);
        }
    }

    private void unregisterPhoneListener() {
        this.mSub = MSimTelephonyManager.getDefault().getPhoneCount();
        if (this.mTelephonyManager == null) {
            MLog.d("RcsSettingsPreferenceActivity", "custMms:unregisterPhoneListener:mPhone = null");
            return;
        }
        for (int i = 0; i < this.mSub; i++) {
            if (this.mPhoneStateListener[i] != null) {
                this.mTelephonyManager.listen(this.mPhoneStateListener[i], 0);
            }
        }
    }

    private void setRcsSettingStatus() {
        if (this.mMmsRcsPref != null) {
            if (isDefaultSms()) {
                this.mMmsRcsPref.setEnabled(true);
                if (RcsProfile.getRcsService() == null) {
                    this.mMmsRcsPref.setEnabled(false);
                    MLog.d("RcsSettingsPreferenceActivity", "rcs service not exist, set enabled is false");
                    return;
                }
                boolean z;
                MLog.d("RcsSettingsPreferenceActivity", "rcs service exist, set enabled is true");
                this.mMmsRcsPref.setEnabled(true);
                int airplaneMode = System.getInt(getContentResolver(), "airplane_mode_on", 0);
                MLog.d("RcsSettingsPreferenceActivity", "setRcsSettingStatus-airplaneMode = " + airplaneMode);
                if (checkSimState() && airplaneMode == 0) {
                    z = true;
                } else {
                    z = false;
                }
                this.mSimIsPresent = z;
                MLog.d("RcsSettingsPreferenceActivity", "setRcsSettingStatus:mSimIsPresent = " + this.mSimIsPresent);
                MLog.d("RcsSettingsPreferenceActivity", "setRcsSettingStatus:mSimNetWorkIsPresent = " + this.mSimNetWorkIsPresent);
                if (RcsTransaction.isSupportNoSimMode() || this.mSimIsPresent) {
                    int sms_port = RcsTransaction.getSmsPort();
                    MLog.d("RcsSettingsPreferenceActivity", "setRcsSettingStatus:sms_port = " + sms_port);
                    if (!this.mSimIsPresent || this.mSimNetWorkIsPresent || sms_port == 0) {
                        this.mMmsRcsPref.setEnabled(true);
                    } else {
                        this.mMmsRcsPref.setEnabled(false);
                    }
                    return;
                }
                this.mMmsRcsPref.setEnabled(false);
                MLog.d("RcsSettingsPreferenceActivity", "setRcsSettingStatus:mSimIsPresent- Login mode does not support non-card and no sim card,set rcs switch enabled is false");
                return;
            }
            this.mMmsRcsPref.setEnabled(false);
        }
    }

    private boolean isDefaultSms() {
        return "com.android.mms".equals(Sms.getDefaultSmsPackage(this));
    }

    private boolean checkSimState() {
        if (this.mTelephonyManager == null) {
            this.mTelephonyManager = new TelephonyManager(getApplicationContext());
        }
        return 5 == this.mTelephonyManager.getSimState();
    }

    private boolean getRcsSwitchStatus() {
        int mRcsSwitchStatus = 0;
        try {
            mRcsSwitchStatus = Secure.getInt(getContentResolver(), "huawei_rcs_switcher", 1);
        } catch (Exception e) {
            MLog.e("RcsSettingsPreferenceActivity", e.toString());
        }
        if (1 == mRcsSwitchStatus) {
            return true;
        }
        return false;
    }

    private void registerBroadcast() {
        IntentFilter statusFilter = new IntentFilter();
        statusFilter.addAction("com.huawei.rcs.loginstatus");
        this.mLoginStatusReceiver = new LoginStatusReceiver();
        registerReceiver(this.mLoginStatusReceiver, statusFilter, "com.huawei.rcs.RCS_BROADCASTER", null);
    }

    private void unregisterBroadcast() {
        if (this.mLoginStatusReceiver != null) {
            unregisterReceiver(this.mLoginStatusReceiver);
        }
    }

    public void setMessagePreferences() {
        Preference listDivideLine;
        this.mImEnable = (SwitchPreference) findPreference("pref_key_im");
        Preference restrictedMode = findPreference("pref_key_im");
        if (restrictedMode != null) {
            listDivideLine = findPreference("divider_pref_key_im");
            if (listDivideLine != null) {
                ((PreferenceGroup) findPreference("pref_key_im_settings")).removePreference(listDivideLine);
            }
            ((PreferenceGroup) findPreference("pref_key_im_settings")).removePreference(restrictedMode);
        }
        this.mImDeliveryReportRequest = (SwitchPreference) findPreference("pref_key_im_enable_delivery_report");
        Preference deliveryReport = findPreference("pref_key_im_enable_delivery_report");
        if (deliveryReport != null) {
            listDivideLine = findPreference("divider_pref_key_im_enable_delivery_report");
            if (listDivideLine != null) {
                ((PreferenceGroup) findPreference("pref_key_im_settings")).removePreference(listDivideLine);
            }
            ((PreferenceGroup) findPreference("pref_key_im_settings")).removePreference(deliveryReport);
        }
        this.mImDisplayReport = (SwitchPreference) findPreference("pref_key_im_enable_display_report");
        this.mImThreadMixDisplay = (SwitchPreference) findPreference("pref_key_im_enable_thread_mix_display");
        if (this.mImThreadMixDisplay != null) {
            listDivideLine = findPreference("divider_pref_key_im_enable_thread_mix_display");
            if (listDivideLine != null) {
                ((PreferenceGroup) findPreference("pref_key_im_settings")).removePreference(listDivideLine);
            }
            ((PreferenceGroup) findPreference("pref_key_im_settings")).removePreference(this.mImThreadMixDisplay);
        }
        this.mGroupDeliveryReportRequest = (SwitchPreference) findPreference("pref_key_group_message_enable_delivery_report");
        this.mGroupInviteAccept = (SwitchPreference) findPreference("pref_key_group_invite_accept");
        hideSettingsIfNotConfigureInRcsXml();
        this.mFtAutoAccept = findPreference("pref_key_auto_accept_file");
        this.mFtCropNotAskmeSet = (SwitchPreference) findPreference("pref_key_crop_not_ask_me_again");
        this.mMessageInfo = (PreferenceScreen) findPreference("pref_key_message_info");
        updateRcsSwitchState();
        try {
            this.mImEnable.setOnPreferenceChangeListener(this.imEnableListener);
            this.mImDeliveryReportRequest.setOnPreferenceChangeListener(this.imDeliveryRequestListener);
            this.mImDisplayReport.setOnPreferenceChangeListener(this.imDisplayReportListener);
            this.mImThreadMixDisplay.setOnPreferenceChangeListener(this.imThreadMixDisplayListener);
            this.mGroupInviteAccept.setOnPreferenceChangeListener(this.groupInviteAcceptListener);
            this.mGroupDeliveryReportRequest.setOnPreferenceChangeListener(this.groupMessageDeliveryRequestListener);
            int autoAcceptFile = PreferenceManager.getDefaultSharedPreferences(this).getInt("autoRecieveFile", 1);
            this.mFtAutoAccept.setSummary(getResources().getStringArray(R.array.prefEntries_mms_auto_receive)[autoAcceptFile]);
            this.mFtAutoAccept.setOnPreferenceClickListener(this.ftAutoAcceptListener);
            this.mFtCropNotAskmeSet.setOnPreferenceChangeListener(this.ftCropNotAskmeSetListener);
        } catch (RuntimeException e) {
            MLog.e("RcsSettingsPreferenceActivity", "setOnPreferenceChangeListener error");
        }
    }

    private void initPreference() {
        addPreferencesFromResource(R.xml.rcs_advanced_preferences_cust);
        addRcsSwitcherPref();
        findPreference("pref_key_im_settings").setOrder(-2);
        initNicknamePreferences();
        initFtPreferenceForFt();
    }

    private void refreshFtAutoAccept() {
        this.mFtAutoAccept = findPreference("pref_key_auto_accept_file");
        try {
            int autoAcceptFile = PreferenceManager.getDefaultSharedPreferences(this).getInt("autoRecieveFile", 1);
            this.mFtAutoAccept.setSummary(getResources().getStringArray(R.array.prefEntries_mms_auto_receive)[autoAcceptFile]);
        } catch (RuntimeException e) {
            MLog.e("RcsSettingsPreferenceActivity", "setOnPreferenceChangeListener error");
        }
    }

    private void addRcsSwitcherPref() {
        this.mMmsRcsPref = new EmuiSwitchPreference(this);
        this.mMmsRcsPref.setPreferenceId(R.id.mms_rcs_preference);
        this.mMmsRcsPref.setKey("pref_key_mms_rcs_settings");
        this.mMmsRcsPref.setTitle(R.string.rich_communications_title);
        this.mMmsRcsPref.setDefaultValue(Boolean.valueOf(true));
        this.mMmsRcsPref.setSummary(R.string.rcs_about_message_sub);
        this.mMmsRcsPref.setOrder(-3);
        getPreferenceScreen().addPreference(this.mMmsRcsPref);
        this.mMmsRcsPref.setOnPreferenceChangeListener(this.rcsSwitcherListener);
        Preference listDivideLineRcs = new Preference(this);
        listDivideLineRcs.setPreferenceId(R.id.rcs_switcher_list_divide_line);
        listDivideLineRcs.setLayoutResource(R.layout.listdivider);
        listDivideLineRcs.setOrder(-4);
        getPreferenceScreen().addPreference(listDivideLineRcs);
    }

    private void initNicknamePreferences() {
        this.mRCSNickname = findPreference("pref_key_mms_rcs_nick_name");
        if (this.mRCSNickname != null) {
            if (RcsProfile.isGroupChatNicknameEnabled()) {
                this.mRCSNickname.setOnPreferenceClickListener(this.mRCSNicknameClickListener);
            } else {
                Preference listDivideLine = findPreference("divider_pref_key_mms_rcs_nick_name");
                if (listDivideLine != null) {
                    ((PreferenceGroup) findPreference("pref_key_im_settings")).removePreference(listDivideLine);
                }
                ((PreferenceGroup) findPreference("pref_key_im_settings")).removePreference(this.mRCSNickname);
                return;
            }
        }
        setGroupNicknameValue();
    }

    private void setGroupNicknameValue() {
        if (this.mRCSNickname != null) {
            String nickname = "";
            IfMsgplus aMsgPlus = RcsProfile.getRcsService();
            if (aMsgPlus != null) {
                try {
                    nickname = aMsgPlus.getGroupNickname();
                    if (TextUtils.isEmpty(nickname)) {
                        nickname = aMsgPlus.getLocalPhoneNumber();
                        if (TextUtils.isEmpty(nickname)) {
                            nickname = "";
                        }
                    }
                } catch (RemoteException e) {
                    MLog.d("RcsSettingsPreferenceActivity", "setGroupNicknameValue" + e.toString());
                }
            }
            this.mRCSNickname.setSummary(nickname);
        }
    }

    public void setIMStatus() {
        if (this.mRCSNickname != null && RcsProfile.isGroupChatNicknameEnabled()) {
            this.mRCSNickname.setEnabled(false);
        }
        this.mImEnable.setEnabled(false);
        this.mImDeliveryReportRequest.setEnabled(false);
        this.mImDisplayReport.setEnabled(false);
        this.mGroupDeliveryReportRequest.setEnabled(false);
        this.mGroupInviteAccept.setEnabled(false);
        this.mFtAutoAccept.setEnabled(false);
        this.mFtCropNotAskmeSet.setEnabled(false);
    }

    private void setGroupChatDeliveryStatus(boolean request) {
        MLog.i("RcsSettingsPreferenceActivity", "setGroupChatDeliveryStatus request is " + request);
        IfMsgplus aMsgPlus = RcsProfile.getRcsService();
        if (aMsgPlus != null) {
            try {
                aMsgPlus.setGroupMessageRequestDeliveryStatus(request);
            } catch (RemoteException e) {
                MLog.e("RcsSettingsPreferenceActivity", "setGroupMessageRequestDeliveryStatus error");
            }
        }
    }

    private boolean checkNicknameFormat(String s) {
        if (RcsProfile.isSupportAllCharacters() || TextUtils.isEmpty(s)) {
            return true;
        }
        return s.matches("[a-zA-Z_0-9]*");
    }

    private void showOrHideNicknamePrompt(boolean show) {
        if (this.mNicknameEt != null && this.mNicknamePositiveButton != null) {
            if (show) {
                this.mNicknameEt.setError(getString(R.string.only_support_english));
                this.mNicknamePositiveButton.setEnabled(false);
            } else if (this.mNicknameEt.getText().toString().length() == 24) {
                this.mNicknameEt.setError(getString(R.string.topic_max_tips));
            } else {
                this.mNicknamePositiveButton.setEnabled(true);
            }
        }
    }

    private void showAutoRetrievialDialog() {
        if (this.mAutoRetrieviaDlg == null || !this.mAutoRetrieviaDlg.isShowing()) {
            this.mAutoRetrieviaDlg = new Builder(this).setTitle(R.string.auto_accept_file_title).setIconAttribute(16843605).setAdapter(new AutoRetrievialAdapter(), null).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (RcsSettingsPreferenceActivity.this.mAutoRetrieviaDlg != null) {
                        RcsSettingsPreferenceActivity.this.mAutoRetrieviaDlg.dismiss();
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
                    RcsSettingsPreferenceActivity.this.setAutoReceivePrefState(pos);
                    RcsSettingsPreferenceActivity.this.mAutoRetrieviaDlg.dismiss();
                }
            });
            this.mAutoRetrieviaDlg.show();
        }
    }

    private void setAutoReceivePrefState(int pos) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        String[] prefEntriesAutoAcceptFile = getResources().getStringArray(R.array.prefEntries_mms_auto_receive);
        if (pos == 0) {
            this.mFtAutoAccept.setSummary(prefEntriesAutoAcceptFile[pos]);
            editor.putBoolean("pref_key_auto_accept_file", true);
            editor.putBoolean("pref_key_Roam_auto_accept", true);
            RcsProfile.setftFileAceeptSwitch(this, 1, "pref_key_auto_accept_file");
            RcsProfile.setftFileAceeptSwitch(this, 1, "pref_key_Roam_auto_accept");
        } else if (1 == pos) {
            editor.putBoolean("pref_key_Roam_auto_accept", false);
            editor.putBoolean("pref_key_auto_accept_file", true);
            RcsProfile.setftFileAceeptSwitch(this, 0, "pref_key_Roam_auto_accept");
            RcsProfile.setftFileAceeptSwitch(this, 1, "pref_key_auto_accept_file");
        } else if (2 == pos) {
            editor.putBoolean("pref_key_Roam_auto_accept", false);
            editor.putBoolean("pref_key_auto_accept_file", false);
            RcsProfile.setftFileAceeptSwitch(this, 0, "pref_key_Roam_auto_accept");
            RcsProfile.setftFileAceeptSwitch(this, 0, "pref_key_auto_accept_file");
        }
        this.mFtAutoAccept.setSummary(prefEntriesAutoAcceptFile[pos]);
        editor.putInt("autoRecieveFile", pos);
        editor.commit();
    }

    private void updateRcsSwitchState() {
        boolean isEnable = false;
        if (RcsProfile.isRcsServiceEnabledAndUserLogin()) {
            this.mImEnable.setEnabled(true);
            if (this.mImEnable.isChecked()) {
                if (this.mRCSNickname != null && RcsProfile.isGroupChatNicknameEnabled()) {
                    this.mRCSNickname.setEnabled(true);
                }
                this.mImDeliveryReportRequest.setEnabled(true);
                this.mImDisplayReport.setEnabled(true);
                this.mGroupInviteAccept.setEnabled(true);
                this.mGroupDeliveryReportRequest.setEnabled(true);
                this.mFtAutoAccept.setEnabled(true);
                this.mFtCropNotAskmeSet.setEnabled(true);
                return;
            }
            if (this.mRCSNickname != null && RcsProfile.isGroupChatNicknameEnabled()) {
                this.mRCSNickname.setEnabled(false);
            }
            this.mImDeliveryReportRequest.setEnabled(false);
            this.mImDisplayReport.setEnabled(false);
            this.mGroupDeliveryReportRequest.setEnabled(false);
            this.mGroupInviteAccept.setEnabled(false);
            this.mFtAutoAccept.setEnabled(false);
            this.mFtCropNotAskmeSet.setEnabled(false);
            return;
        }
        if (this.mMmsRcsPref.isChecked()) {
            isEnable = this.mMmsRcsPref.isEnabled();
        }
        setRcsPreferenceEnable(isEnable);
    }

    private void hideSettingsIfNotConfigureInRcsXml() {
        Preference listDivideLine;
        boolean is_groupChat_autoAccept_show = RcsTransaction.isShowGroupChatAutoAcceptSetting();
        boolean is_groupMessage_delivery_report_show = RcsTransaction.isShowGroupMessageDeliveryReportSetting();
        if (!is_groupChat_autoAccept_show) {
            Preference groupInviteAccept = findPreference("pref_key_group_invite_accept");
            if (groupInviteAccept != null) {
                listDivideLine = findPreference("divider_pref_key_group_invite_accept");
                if (listDivideLine != null) {
                    ((PreferenceGroup) findPreference("pref_key_im_settings")).removePreference(listDivideLine);
                }
                ((PreferenceGroup) findPreference("pref_key_im_settings")).removePreference(groupInviteAccept);
            }
        }
        if (!is_groupMessage_delivery_report_show) {
            Preference groupMessageDeliveryReport = findPreference("pref_key_group_message_enable_delivery_report");
            if (groupMessageDeliveryReport != null) {
                listDivideLine = findPreference("divider_pref_key_group_message_enable_delivery_report");
                if (listDivideLine != null) {
                    ((PreferenceGroup) findPreference("pref_key_im_settings")).removePreference(listDivideLine);
                }
                ((PreferenceGroup) findPreference("pref_key_im_settings")).removePreference(groupMessageDeliveryReport);
            }
        }
    }

    private void initFtPreferenceForFt() {
        int pref_key_auto_accept_file = System.getInt(getContentResolver(), "pref_key_auto_accept_file", 3);
        int pref_key_Roam_auto_accept = System.getInt(getContentResolver(), "pref_key_Roam_auto_accept", 3);
        if (pref_key_auto_accept_file == 3 || pref_key_Roam_auto_accept == 3) {
            MLog.i("RcsSettingsPreferenceActivity FileTrans: ", "initFtPreferenceForFt resetFtRcsSwitch ");
            resetFtRcsSwitch();
            return;
        }
        resetFtRcsSwitchForUI(pref_key_auto_accept_file, pref_key_Roam_auto_accept);
    }

    private void resetFtRcsSwitch() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean[] result = new boolean[]{sp.getBoolean("pref_key_auto_accept_file", true), sp.getBoolean("pref_key_Roam_auto_accept", false)};
        if (result[0]) {
            RcsProfile.setftFileAceeptSwitch(this, 1, "pref_key_auto_accept_file");
        } else {
            RcsProfile.setftFileAceeptSwitch(this, 0, "pref_key_auto_accept_file");
        }
        if (result[1]) {
            RcsProfile.setftFileAceeptSwitch(this, 1, "pref_key_Roam_auto_accept");
        } else {
            RcsProfile.setftFileAceeptSwitch(this, 0, "pref_key_Roam_auto_accept");
        }
    }

    private void resetFtRcsSwitchForUI(int pref_key_auto_accept_file, int pref_key_Roam_auto_accept) {
        boolean z = false;
        boolean[] result = new boolean[]{prefs.getBoolean("pref_key_auto_accept_file", false), PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_key_Roam_auto_accept", false)};
        boolean shouldSyncState = false;
        int pos = -1;
        if (pref_key_auto_accept_file == 1 && pref_key_Roam_auto_accept == 1) {
            if (result[0]) {
                z = result[1];
            }
            if (!z) {
                shouldSyncState = true;
                pos = 0;
                result[0] = true;
                result[1] = true;
            }
        } else if (pref_key_auto_accept_file == 1) {
            if (!result[0] || result[1]) {
                shouldSyncState = true;
                pos = 1;
                result[0] = true;
                result[1] = false;
            }
        } else if (result[0] || result[1]) {
            shouldSyncState = true;
            pos = 2;
            result[0] = false;
            result[1] = false;
        }
        if (shouldSyncState) {
            Editor editor = prefs.edit();
            editor.putBoolean("pref_key_auto_accept_file", result[0]);
            editor.putBoolean("pref_key_Roam_auto_accept", result[1]);
            editor.putInt("autoRecieveFile", pos);
            editor.apply();
        }
    }

    private boolean setRcsSwitchStatus(int value) {
        boolean bResult = false;
        try {
            bResult = Secure.putInt(getContentResolver(), "huawei_rcs_switcher", value);
        } catch (Exception e) {
            MLog.e("RcsSettingsPreferenceActivity", e.toString());
        }
        return bResult;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                onBackPressed();
                break;
        }
        return true;
    }

    private void setRcsPreferenceEnable(boolean isEnable) {
        if (this.mRCSNickname != null && RcsProfile.isGroupChatNicknameEnabled()) {
            this.mRCSNickname.setEnabled(isEnable);
        }
        this.mImEnable.setEnabled(isEnable);
        this.mImDeliveryReportRequest.setEnabled(isEnable);
        this.mImDisplayReport.setEnabled(isEnable);
        this.mGroupInviteAccept.setEnabled(isEnable);
        this.mGroupDeliveryReportRequest.setEnabled(isEnable);
        this.mFtAutoAccept.setEnabled(isEnable);
        this.mFtCropNotAskmeSet.setEnabled(isEnable);
    }
}
