package com.huawei.mms.ui;

import android.app.ActionBar;
import android.os.Bundle;
import android.os.Message;
import android.preference.Preference;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.android.mms.MmsApp;
import com.android.mms.ui.CryptoGeneralPreferenceFragment.OnRestoreDefaultCryptoListener;
import com.android.mms.ui.EmuiSwitchPreference;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.R;
import com.huawei.cloudservice.CloudAccount;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.crypto.CryptoMessageUtil;
import com.huawei.mms.crypto.account.AccountCheckHandler;

public class MsimSmsEncryptSetting extends SmsEncryptSetting {
    private boolean mCard1LandDialogState = false;
    private boolean mCard2LandDialogState = false;
    private EmuiSwitchPreference mSimCard1Pref;
    private EmuiSwitchPreference mSimCard2Pref;
    OnRestoreDefaultCryptoListener onRestoreDefaultCryptoListener = new OnRestoreDefaultCryptoListener() {
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MessageUtils.isMultiSimEnabled()) {
            MLog.i("MsimSmsEncryptSetting", "start to onCreate");
            ActionBar actionBar = getActionBar();
            actionBar.setTitle(R.string.sms_encrypt_setting_title);
            actionBar.setDisplayHomeAsUpEnabled(true);
            addPreferencesFromResource(R.xml.msim_sms_encrypt_setting);
            init();
            registerSimChangedReceiver();
            return;
        }
        finish();
    }

    private void init() {
        this.mSimCard1Pref = (EmuiSwitchPreference) findPreference("pref_key_sim_card1");
        this.mSimCard2Pref = (EmuiSwitchPreference) findPreference("pref_key_sim_card2");
        this.mSimCard1Pref.setOnPreferenceChangeListener(this);
        this.mSimCard2Pref.setOnPreferenceChangeListener(this);
    }

    protected void onResume() {
        super.onResume();
    }

    protected void updateLandDialogState(boolean isShow) {
        if (!isShow) {
            this.mCard1LandDialogState = false;
            this.mCard2LandDialogState = false;
        } else if (this.mSubId == 0) {
            this.mCard1LandDialogState = true;
            this.mCard2LandDialogState = false;
        } else {
            this.mCard1LandDialogState = false;
            this.mCard2LandDialogState = true;
        }
    }

    private void updateLandDialogState(boolean isShow, int subId) {
        if (subId == 0) {
            if (isShow) {
                this.mCard1LandDialogState = true;
                this.mCard2LandDialogState = false;
                return;
            }
            this.mCard1LandDialogState = false;
            this.mCard2LandDialogState = false;
        } else if (isShow) {
            this.mCard1LandDialogState = false;
            this.mCard2LandDialogState = true;
        } else {
            this.mCard1LandDialogState = false;
            this.mCard2LandDialogState = false;
        }
    }

    protected void refreshUi() {
        int card1ActivateState = this.mAccountManager.getCardActivatedState(0);
        int card2ActivateState = this.mAccountManager.getCardActivatedState(1);
        initPreferenceState(0, card1ActivateState);
        initPreferenceState(1, card2ActivateState);
        disableOtherPrefWhenActivating(card1ActivateState, card2ActivateState);
    }

    private void initPreferenceState(int subId, int state) {
        EmuiSwitchPreference pref = subId == 0 ? this.mSimCard1Pref : this.mSimCard2Pref;
        if (MessageUtils.getIccCardStatus(subId) != 1) {
            MLog.i("MsimSmsEncryptSetting", "initPreferenceState, the card with subId " + subId + " is not present");
            pref.setEnabled(false);
            pref.setChecked(false);
            pref.setSummary(R.string.unbind_state_summary);
            return;
        }
        if ((subId == 0 && this.mCard1LandDialogState) || (subId == 1 && this.mCard2LandDialogState)) {
            pref.setEnabled(true);
            pref.setChecked(true);
            pref.setSummary(R.string.unbind_state_summary);
        } else {
            MLog.i("MsimSmsEncryptSetting", "initPreferenceState, subId is: " + subId + " and state is: " + state);
            initPrefByActivationState(pref, subId, state);
        }
    }

    private void disableOtherPrefWhenActivating(int card1State, int card2State) {
        if (card1State == 2) {
            this.mSimCard2Pref.setEnabled(false);
        }
        if (card2State == 2) {
            this.mSimCard1Pref.setEnabled(false);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object enable) {
        if (preference == this.mSimCard1Pref) {
            this.mSubId = 0;
        } else if (preference == this.mSimCard2Pref) {
            this.mSubId = 1;
        }
        if (isMainLandCard(this.mSubId)) {
            MLog.d("MsimSmsEncryptSetting", "onPreferenceChange: current subId is: " + this.mSubId);
            if (((Boolean) enable).booleanValue()) {
                updateLandDialogState(true, this.mSubId);
                showDialog(1);
            } else {
                MLog.i("MsimSmsEncryptSetting", "start to verify and close the account bind");
                if (this.mAccountManager.getCardActivatedState(this.mSubId) == 2) {
                    MLog.i("MsimSmsEncryptSetting", "start to close the account bind when acticating");
                    doCloseAccountBind();
                    refreshUi();
                    return false;
                }
                String accountName = this.mAccountManager.getCardCloudAccount(this.mSubId);
                if (TextUtils.isEmpty(accountName)) {
                    MLog.e("MsimSmsEncryptSetting", "onPreferenceChange, unbind the account, accountName is empty");
                    return false;
                }
                this.mAccountManager.checkHwIDPassword(this, accountName, new AccountCheckHandler(1, this.mBindHandler, this.mMainHandler));
            }
            return true;
        }
        showDialog(5);
        updateLandDialogState(true, this.mSubId);
        return true;
    }

    private void updatePreference(int subId, boolean checked, boolean enabled) {
        EmuiSwitchPreference preference;
        String summary;
        if (subId == 0) {
            preference = this.mSimCard1Pref;
        } else {
            preference = this.mSimCard2Pref;
        }
        preference.setEnabled(enabled);
        preference.setChecked(checked);
        if (checked) {
            String accountName = this.mAccountManager.getCardCloudAccount(this.mSubId);
            summary = String.format(getString(R.string.bind_state_summary), new Object[]{accountName});
        } else {
            summary = getString(R.string.unbind_state_summary);
        }
        preference.setSummary(summary);
    }

    protected void doBindAccount(Message msg) {
        if (msg.obj instanceof CloudAccount) {
            if (this.mAccountManager.bindAccount(this.mSubId, msg.obj) == -1) {
                int tipStringId;
                MLog.i("MsimSmsEncryptSetting", "bindAccount, operation failed");
                if (this.mSubId == 0) {
                    tipStringId = R.string.card1_open_encrypt_sms_function_failed;
                } else {
                    tipStringId = R.string.card2_open_encrypt_sms_function_failed;
                }
                Toast.makeText(this, tipStringId, 0).show();
                this.mMainHandler.sendMessage(Message.obtain(this.mMainHandler, 27));
            }
            return;
        }
        MLog.e("MsimSmsEncryptSetting", "doBindAccount, msg.obj invalid param");
    }

    protected void dialogDismissBackState() {
        if (this.mSubId == 0) {
            this.mSimCard1Pref.setEnabled(true);
        } else {
            this.mSimCard2Pref.setEnabled(true);
        }
    }

    protected void doUnbindAccount(Message msg) {
        if (msg.obj instanceof AccountCheckHandler) {
            AccountCheckHandler handler = msg.obj;
            String accountName = handler.getAccountName();
            String pwd = handler.getPwd();
            if (TextUtils.isEmpty(accountName) || TextUtils.isEmpty(pwd)) {
                MLog.e("MsimSmsEncryptSetting", "doUnbindAccount, empty params");
                return;
            }
            if (this.mAccountManager.unbindAccount(this.mSubId, accountName, pwd) == -1) {
                MLog.i("MsimSmsEncryptSetting", "unbindAccount, operation failed");
                this.mMainHandler.sendMessage(Message.obtain(this.mMainHandler, 1033));
            }
            return;
        }
        MLog.e("MsimSmsEncryptSetting", "doUnbindAccount, msg.obj invalid param");
    }

    protected void doCloseAccountBind() {
        MLog.i("MsimSmsEncryptSetting", "doCloseAccountBind");
        int result = this.mAccountManager.closeAccountBind(this.mSubId, this.mAccountManager.getCardCloudAccount(this.mSubId), null);
        Message message = Message.obtain(this.mMainHandler, OfflineMapStatus.EXCEPTION_SDCARD);
        message.arg1 = result;
        this.mMainHandler.sendMessage(message);
    }

    protected void doBindAccountFinish(Message msg) {
        switch (msg.what) {
            case 5:
                updatePreference(this.mSubId, true, true);
                this.mAccountManager.bindFingerPrompt(this);
                break;
            case 20:
            case 21:
            case 22:
            case 24:
            case 25:
            case 26:
            case 27:
                updatePreference(this.mSubId, false, true);
                break;
            case 23:
                showNeedUnbindPrompt();
                updatePreference(this.mSubId, false, true);
                break;
        }
        refreshUi();
    }

    protected void doCloseAccountBindFinish(Message msg) {
        MLog.i("MsimSmsEncryptSetting", "doCloseAccountBindFinish");
        switch (msg.arg1) {
            case -1:
                int tipStringId;
                if (this.mSubId == 0) {
                    tipStringId = R.string.card1_close_encrypt_sms_function_failed;
                } else {
                    tipStringId = R.string.card2_close_encrypt_sms_function_failed;
                }
                Toast.makeText(this, tipStringId, 0).show();
                updatePreference(this.mSubId, true, true);
                return;
            case 0:
                updatePreference(this.mSubId, false, true);
                clearSwitchPreference();
                return;
            default:
                return;
        }
    }

    protected void doCardActivateStateChange(int subId) {
        initPrefByActivationState(subId == 0 ? this.mSimCard1Pref : this.mSimCard2Pref, subId, this.mAccountManager.getCardActivatedState(subId));
    }

    protected void updatePrefStateWhenBindStart(int subId) {
        initPrefByActivationState(subId == 0 ? this.mSimCard1Pref : this.mSimCard2Pref, subId, 2);
        if (subId == 0) {
            this.mSimCard2Pref.setEnabled(false);
        } else {
            this.mSimCard1Pref.setEnabled(false);
        }
    }

    protected void clearSwitchPreference() {
        if (!this.mSimCard1Pref.isChecked() && !this.mSimCard2Pref.isChecked()) {
            CryptoMessageUtil.asyncClearSwitch();
        }
    }

    private boolean isMainLandCard(int subId) {
        String subIdImsi = MmsApp.getDefaultMSimTelephonyManager().getSubscriberId(subId);
        return subIdImsi != null ? subIdImsi.startsWith("460") : false;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
