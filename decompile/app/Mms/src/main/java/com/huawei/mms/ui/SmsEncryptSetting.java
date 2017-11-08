package com.huawei.mms.ui;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.amap.api.services.core.AMapException;
import com.android.mms.MmsApp;
import com.android.mms.ui.CryptoGeneralPreferenceFragment.OnRestoreDefaultCryptoListener;
import com.android.mms.ui.EmuiSwitchPreference;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.R;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.places.Place;
import com.huawei.cloudservice.CloudAccount;
import com.huawei.cloudservice.LoginHandler;
import com.huawei.cspcommon.MLog;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.mms.crypto.CryptoMessageServiceProxy;
import com.huawei.mms.crypto.CryptoMessageUtil;
import com.huawei.mms.crypto.account.AccountCheckHandler;
import com.huawei.mms.crypto.account.AccountManager;
import com.huawei.mms.util.HwDualCardNameHelper;
import com.huawei.mms.util.HwTelephony;
import com.huawei.mms.util.HwTelephony.HwSimStateListener;

public class SmsEncryptSetting extends HwPreferenceActivity implements OnPreferenceChangeListener {
    protected AccountManager mAccountManager;
    protected Handler mBindHandler;
    protected HandlerThread mBindHandlerThread;
    private Context mContext;
    protected boolean mIsLandDialogShow = false;
    protected LoginHandler mLoginHandler = new AccountLoginHandler();
    protected Handler mMainHandler;
    private EmuiSwitchPreference mSimCardPref;
    protected BroadcastReceiver mSimStateChangedReceiver = null;
    protected int mSubId = 0;
    OnRestoreDefaultCryptoListener onRestoreDefaultCryptoListener = new OnRestoreDefaultCryptoListener() {
    };

    private final class AccountLoginHandler implements LoginHandler {
        private AccountLoginHandler() {
        }

        public void onLogin(CloudAccount[] mAccounts, int index) {
            if (mAccounts == null || index == -1 || mAccounts.length <= index) {
                MLog.e("SmsEncryptSetting", "onLogin invalid params");
                return;
            }
            MLog.d("SmsEncryptSetting", "onLogin: mAccounts length= " + mAccounts.length);
            MLog.d("SmsEncryptSetting", "onLogin: index=" + index);
            CloudAccount account = mAccounts[index];
            SmsEncryptSetting.this.mAccountManager.setCurrentSysAccount(account);
            CryptoMessageUtil.storeSystemHwIdInfo(SmsEncryptSetting.this, account);
            SmsEncryptSetting.this.startToBindAccount();
        }

        public void onError(ErrorStatus status) {
            MLog.i("SmsEncryptSetting", "onError, the error status is: " + status.getErrorCode());
        }
    }

    private final class BindHandler extends Handler {
        public BindHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    SmsEncryptSetting.this.doBindAccount(msg);
                    return;
                case 2:
                    SmsEncryptSetting.this.doCloseAccountBind();
                    return;
                case 3:
                    SmsEncryptSetting.this.doUnbindAccount(msg);
                    return;
                default:
                    return;
            }
        }
    }

    private final class MainHandler extends Handler {
        private MainHandler() {
        }

        public void handleMessage(Message msg) {
            MLog.d("SmsEncryptSetting", "mainHandler handleMessage : " + msg.what);
            switch (msg.what) {
                case 5:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                    SmsEncryptSetting.this.doBindAccountFinish(msg);
                    return;
                case 101:
                    SmsEncryptSetting.this.showDialog(1);
                    return;
                case 102:
                    SmsEncryptSetting.this.updatePrefStateWhenBindStart(SmsEncryptSetting.this.mSubId);
                    return;
                case OfflineMapStatus.EXCEPTION_SDCARD /*103*/:
                    CryptoMessageUtil.clearImsiState();
                    SmsEncryptSetting.this.doCloseAccountBindFinish(msg);
                    return;
                case LocationRequest.PRIORITY_LOW_POWER /*104*/:
                    SmsEncryptSetting.this.showDialog(2);
                    return;
                case LocationRequest.PRIORITY_NO_POWER /*105*/:
                    SmsEncryptSetting.this.refreshUi();
                    return;
                case 106:
                    SmsEncryptSetting.this.startToBindAccount();
                    return;
                case 1011:
                case Place.TYPE_TRANSIT_STATION /*1030*/:
                case 1031:
                case 1032:
                case 1033:
                    SmsEncryptSetting.this.doUnbindAccountFisish(msg);
                    return;
                case AMapException.CODE_AMAP_ID_NOT_EXIST /*2001*/:
                    SmsEncryptSetting.this.doCardActivateStateChange(msg.arg1);
                    return;
                case 3001:
                    if (SmsEncryptSetting.this.mAccountManager.isNeedReactive(SmsEncryptSetting.this)) {
                        SmsEncryptSetting.this.mAccountManager.updateStateForNewKeyVersion(SmsEncryptSetting.this);
                        SmsEncryptSetting.this.showDialog(4);
                        return;
                    }
                    return;
                default:
                    MLog.d("SmsEncryptSetting", "mainHandler unsupported message type : " + msg.what);
                    return;
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        if (!MessageUtils.isMultiSimEnabled()) {
            MLog.i("SmsEncryptSetting", "start to onCreate");
            getActionBar().setTitle(R.string.sms_encrypt_setting_title);
            addPreferencesFromResource(R.xml.sms_encrypt_setting);
            registerSimChangedReceiver();
        }
    }

    private void init() {
        MLog.i("SmsEncryptSetting", "start to init");
        this.mContext = this;
        this.mBindHandlerThread = new HandlerThread("SmsEncryptSetting");
        this.mBindHandlerThread.start();
        this.mBindHandler = new BindHandler(this.mBindHandlerThread.getLooper());
        this.mMainHandler = new MainHandler();
        this.mAccountManager = AccountManager.getInstance();
    }

    private void initPreferenceState() {
        this.mSimCardPref = (EmuiSwitchPreference) findPreference("pref_key_sim_card");
        if (this.mSimCardPref != null) {
            this.mSimCardPref.setOnPreferenceChangeListener(this);
            if (MessageUtils.getIccCardStatus(0) != 1) {
                MLog.i("SmsEncryptSetting", "initPreferenceState, the card is not present");
                this.mSimCardPref.setEnabled(false);
                this.mSimCardPref.setChecked(false);
                this.mSimCardPref.setSummary(R.string.unbind_state_summary);
                return;
            }
            int cardActivateState = this.mAccountManager.getCardActivatedState();
            MLog.i("SmsEncryptSetting", "initPreferenceState, the card state is: " + cardActivateState);
            if (this.mIsLandDialogShow) {
                this.mSimCardPref.setEnabled(true);
                this.mSimCardPref.setChecked(true);
                this.mSimCardPref.setSummary(R.string.unbind_state_summary);
            } else {
                initPrefByActivationState(this.mSimCardPref, 0, cardActivateState);
            }
        }
    }

    protected void onStart() {
        super.onStart();
        CryptoMessageServiceProxy.addListener(this.mMainHandler);
    }

    protected void onResume() {
        super.onResume();
        showActivateStatePrompt();
        refreshUi();
    }

    protected void updateLandDialogState(boolean isShow) {
        this.mIsLandDialogShow = isShow;
    }

    protected void refreshUi() {
        initPreferenceState();
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onStop() {
        super.onStop();
        CryptoMessageServiceProxy.removeListener(this.mMainHandler);
    }

    protected void showActivateStatePrompt() {
        if (this.mAccountManager.isNeedReactive(this)) {
            this.mAccountManager.updateStateForNewKeyVersion(this);
            showDialog(4);
            this.mAccountManager.setShouldBindFingerPrompt(this, false);
            this.mAccountManager.setNeedUnbindPrompt(false);
        } else if (this.mAccountManager.isNeedUnbindPrompt()) {
            showNeedUnbindPrompt();
            this.mAccountManager.setShouldBindFingerPrompt(this, false);
        } else {
            this.mAccountManager.bindFingerPrompt(this);
        }
    }

    protected void showNeedUnbindPrompt() {
        MLog.d("SmsEncryptSetting", "showNeedUnbindPrompt");
        if (this.mAccountManager.isNeedUnbindPrompt()) {
            this.mSubId = this.mAccountManager.getNeedUnbindSubId();
            this.mAccountManager.checkPasswordIndependent(this, this.mAccountManager.getNeedUnbindAccountName(), new AccountCheckHandler(2, this.mBindHandler, this.mMainHandler), 2);
            this.mAccountManager.setNeedUnbindPrompt(false);
        }
    }

    protected void initPrefByActivationState(EmuiSwitchPreference pref, int subId, int state) {
        switch (state) {
            case 1:
                pref.setEnabled(true);
                pref.setChecked(true);
                String accountName = this.mAccountManager.getCardCloudAccount(subId);
                pref.setSummary(String.format(getString(R.string.bind_state_summary), new Object[]{accountName}));
                return;
            case 2:
                pref.setEnabled(true);
                pref.setChecked(true);
                pref.setSummary(R.string.binding_dialog_title);
                this.mSubId = subId;
                return;
            case 4:
                pref.setEnabled(true);
                pref.setChecked(false);
                pref.setSummary(R.string.unbind_state_summary);
                this.mSubId = subId;
                showDialog(2);
                return;
            case 5:
                pref.setEnabled(false);
                pref.setChecked(false);
                pref.setSummary(R.string.bind_state_unknow_summary);
                return;
            default:
                pref.setEnabled(true);
                pref.setChecked(false);
                pref.setSummary(R.string.unbind_state_summary);
                return;
        }
    }

    public boolean onPreferenceChange(Preference preference, Object enable) {
        if (preference != this.mSimCardPref) {
            return false;
        }
        if (isMainLandCard(this.mSubId)) {
            if (((Boolean) enable).booleanValue()) {
                updateLandDialogState(true);
                showDialog(1);
            } else {
                MLog.i("SmsEncryptSetting", "start to verify and close the account bind");
                if (this.mAccountManager.getCardActivatedState() == 2) {
                    MLog.i("SmsEncryptSetting", "start to close the account bind when acticating");
                    doCloseAccountBind();
                    refreshUi();
                    return false;
                }
                String accountName = this.mAccountManager.getCardCloudAccount();
                if (TextUtils.isEmpty(accountName)) {
                    MLog.e("SmsEncryptSetting", "onPreferenceChange, unbind the account, accountName is empty");
                    return false;
                }
                this.mAccountManager.checkHwIDPassword(this, accountName, new AccountCheckHandler(1, this.mBindHandler, this.mMainHandler));
            }
            return true;
        }
        updateLandDialogState(true);
        showDialog(5);
        return true;
    }

    private void updatePreference(boolean checked, boolean enabled) {
        if (this.mSimCardPref != null) {
            String summary;
            this.mSimCardPref.setEnabled(enabled);
            this.mSimCardPref.setChecked(checked);
            if (checked) {
                String accountName = this.mAccountManager.getCardCloudAccount(this.mSubId);
                summary = String.format(getString(R.string.bind_state_summary), new Object[]{accountName});
            } else {
                summary = getString(R.string.unbind_state_summary);
            }
            this.mSimCardPref.setSummary(summary);
        }
    }

    protected void dismissDialogSafely(int id) {
        try {
            dismissDialog(id);
        } catch (IllegalArgumentException e) {
            MLog.e("SmsEncryptSetting", "invalid params whenin dismissDialog");
        }
    }

    protected Dialog onCreateDialog(int id) {
        if (id == 2) {
            ProgressDialog proressDialog = new ProgressDialog(this);
            proressDialog.setIndeterminate(true);
            proressDialog.setCancelable(false);
            proressDialog.setMessage(getText(R.string.unbinding_dialog_title));
            return proressDialog;
        } else if (id == 3) {
            builder = new Builder(this);
            builder.setView(View.inflate(this, R.layout.unbind_success_dialog, null));
            builder.setCancelable(false);
            return builder.create();
        } else if (id == 1) {
            View contents = View.inflate(this, R.layout.encrypted_need_dialog_content, null);
            ((TextView) contents.findViewById(R.id.dialog_msg)).setText(R.string.encrypted_active_remind_dialog_message);
            builder = new Builder(this);
            builder.setTitle(R.string.need_send_sms_dialog_title);
            builder.setView(contents);
            builder.setPositiveButton(R.string.need_send_sms_dialog_ok, new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    MLog.i("SmsEncryptSetting", "start to login and bind the account");
                    SmsEncryptSetting.this.mAccountManager.loginOrCheckSystemAccount(MmsApp.getApplication().getApplicationContext(), new AccountCheckHandler(0, SmsEncryptSetting.this.mBindHandler, SmsEncryptSetting.this.mMainHandler), SmsEncryptSetting.this.mLoginHandler);
                    SmsEncryptSetting.this.updateLandDialogState(false);
                }
            });
            builder.setNegativeButton(17039360, new OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    SmsEncryptSetting.this.updateLandDialogState(false);
                    SmsEncryptSetting.this.dialogDismissBackState();
                    SmsEncryptSetting.this.refreshUi();
                }
            });
            builder.setCancelable(false);
            dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        } else if (id == 4) {
            builder = new Builder(this);
            builder.setTitle(R.string.mms_remind_title);
            builder.setMessage(R.string.esms_credentials_overdue);
            builder.setPositiveButton(17039370, new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    SmsEncryptSetting.this.refreshUi();
                }
            });
            return builder.create();
        } else if (id != 5) {
            return super.onCreateDialog(id);
        } else {
            builder = new Builder(this);
            builder.setTitle(R.string.mms_remind_title);
            builder.setMessage(R.string.isnotmainlandcard_message);
            builder.setPositiveButton(R.string.encrypted_esms_user_know, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    SmsEncryptSetting.this.updateLandDialogState(false);
                    SmsEncryptSetting.this.refreshUi();
                }
            });
            dialog = builder.create();
            dialog.setOnKeyListener(new OnKeyListener() {
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == 4) {
                        SmsEncryptSetting.this.updateLandDialogState(false);
                        SmsEncryptSetting.this.refreshUi();
                    }
                    return false;
                }
            });
            return dialog;
        }
    }

    private void startToBindAccount() {
        CloudAccount account = AccountManager.getInstance().getCurrentSysAccount();
        if (account == null) {
            MLog.w("SmsEncryptSetting", "account is null when startToBindAccount");
            return;
        }
        if (this.mMainHandler != null) {
            this.mMainHandler.sendMessage(Message.obtain(this.mMainHandler, 102));
        }
        MLog.i("SmsEncryptSetting", "start to bind the account");
        if (this.mBindHandler != null) {
            this.mBindHandler.sendMessage(Message.obtain(this.mBindHandler, 1, account));
        }
    }

    protected void dialogDismissBackState() {
        this.mSimCardPref.setEnabled(true);
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mBindHandlerThread != null) {
            this.mBindHandlerThread.quit();
        }
        unregisterSimChangedReceiver();
        this.mContext = null;
    }

    protected void doBindAccount(Message msg) {
        if (msg.obj instanceof CloudAccount) {
            if (this.mAccountManager.bindAccount(msg.obj) == -1) {
                MLog.i("SmsEncryptSetting", "bindAccount, operation failed");
                Toast.makeText(this, R.string.open_encrypt_sms_function_failed, 0).show();
                this.mMainHandler.sendMessage(Message.obtain(this.mMainHandler, 27));
            }
            return;
        }
        MLog.e("SmsEncryptSetting", "doBindAccount, msg.obj invalid param");
    }

    protected void doUnbindAccount(Message msg) {
        if (msg.obj instanceof AccountCheckHandler) {
            AccountCheckHandler handler = msg.obj;
            String pwd = handler.getPwd();
            String accountName = handler.getAccountName();
            if (TextUtils.isEmpty(accountName) || TextUtils.isEmpty(pwd)) {
                MLog.e("SmsEncryptSetting", "doUnbindAccount, empty params");
                return;
            }
            if (this.mAccountManager.unbindAccount(accountName, pwd) == -1) {
                MLog.i("SmsEncryptSetting", "unbindAccount, operation failed");
                this.mMainHandler.sendMessage(Message.obtain(this.mMainHandler, 1033));
            }
            return;
        }
        MLog.e("SmsEncryptSetting", "doUnbindAccount, msg.obj invalid param");
    }

    protected void doCloseAccountBind() {
        MLog.i("SmsEncryptSetting", "doCloseAccountBind");
        int result = this.mAccountManager.closeAccountBind(this.mAccountManager.getCardCloudAccount(), null);
        Message message = Message.obtain(this.mMainHandler, OfflineMapStatus.EXCEPTION_SDCARD);
        message.arg1 = result;
        this.mMainHandler.sendMessage(message);
    }

    protected void doBindAccountFinish(Message msg) {
        switch (msg.what) {
            case 5:
                updatePreference(true, true);
                this.mAccountManager.bindFingerPrompt(this);
                break;
            case 20:
            case 21:
            case 22:
            case 24:
            case 25:
            case 26:
            case 27:
                updatePreference(false, true);
                break;
            case 23:
                showNeedUnbindPrompt();
                updatePreference(false, true);
                break;
        }
        refreshUi();
    }

    protected void doCloseAccountBindFinish(Message msg) {
        switch (msg.arg1) {
            case -1:
                Toast.makeText(this, R.string.close_encrypt_sms_function_failed, 0).show();
                updatePreference(true, true);
                return;
            case 0:
                updatePreference(false, true);
                clearSwitchPreference();
                return;
            default:
                return;
        }
    }

    protected void doUnbindAccountFisish(Message msg) {
        dismissDialogSafely(2);
        switch (msg.what) {
            case 1011:
                showDialog(3);
                this.mMainHandler.postDelayed(new Runnable() {
                    public void run() {
                        SmsEncryptSetting.this.dismissDialog(3);
                        SmsEncryptSetting.this.mAccountManager.loginOrCheckSystemAccount(SmsEncryptSetting.this, new AccountCheckHandler(0, SmsEncryptSetting.this.mBindHandler, SmsEncryptSetting.this.mMainHandler), SmsEncryptSetting.this.mLoginHandler);
                    }
                }, 3000);
                return;
            case Place.TYPE_TRANSIT_STATION /*1030*/:
            case 1032:
            case 1033:
                Toast.makeText(this, R.string.unbind_account_failed, 0).show();
                return;
            case 1031:
                Toast.makeText(this, R.string.unbind_account_time_out, 0).show();
                return;
            default:
                return;
        }
    }

    protected void doCardActivateStateChange(int subId) {
        initPrefByActivationState(this.mSimCardPref, subId, this.mAccountManager.getCardActivatedState(subId));
    }

    protected void updatePrefStateWhenBindStart(int subId) {
        initPrefByActivationState(this.mSimCardPref, subId, 2);
    }

    protected void registerSimChangedReceiver() {
        if (this.mSimStateChangedReceiver == null) {
            this.mSimStateChangedReceiver = HwTelephony.registeSimChange(this.mContext, new HwSimStateListener() {
                public void onSimStateChanged(int simState) {
                    HwDualCardNameHelper.self().clearAndResetCurrentCardName(-1);
                    SmsEncryptSetting.this.mMainHandler.sendMessage(Message.obtain(SmsEncryptSetting.this.mMainHandler, LocationRequest.PRIORITY_NO_POWER));
                }

                public void onSimStateChanged(int simState, int subId) {
                    HwDualCardNameHelper.self().clearAndResetCurrentCardName(subId);
                    SmsEncryptSetting.this.mMainHandler.sendMessage(Message.obtain(SmsEncryptSetting.this.mMainHandler, LocationRequest.PRIORITY_NO_POWER));
                }
            });
        }
    }

    protected void unregisterSimChangedReceiver() {
        if (this.mSimStateChangedReceiver != null) {
            this.mContext.unregisterReceiver(this.mSimStateChangedReceiver);
            this.mSimStateChangedReceiver = null;
        }
    }

    protected void clearSwitchPreference() {
        if (!this.mSimCardPref.isChecked()) {
            CryptoMessageUtil.asyncClearSwitch();
        }
    }

    private boolean isMainLandCard(int subId) {
        String subIdImsi = MmsApp.getDefaultMSimTelephonyManager().getSubscriberId(0);
        if (subIdImsi != null) {
            return subIdImsi.startsWith("460");
        }
        return false;
    }
}
