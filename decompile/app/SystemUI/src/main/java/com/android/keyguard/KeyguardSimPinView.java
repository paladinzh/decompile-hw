package com.android.keyguard;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.android.internal.telephony.ITelephony.Stub;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.keyguard.KeyguardSecurityModel.SecurityMode;
import com.huawei.cust.HwCustUtils;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.KeyguardUtils;
import fyusion.vislib.BuildConfig;

public class KeyguardSimPinView extends KeyguardPinBasedInputView {
    private CheckSimPin mCheckSimPinThread;
    protected HwCustKeyguardSimPinView mCustKeyguardSimPinView;
    private AlertDialog mRemainingAttemptsDialog;
    private ProgressDialog mSimUnlockProgressDialog;
    private int mSubId;
    KeyguardUpdateMonitorCallback mUpdateMonitorCallback;

    private abstract class CheckSimPin implements Runnable {
        private final String mPin;
        private int mSubId;

        abstract void onSimCheckResponse(int i, int i2);

        protected CheckSimPin(String pin, int subId) {
            this.mPin = pin;
            this.mSubId = subId;
        }

        public void run() {
            try {
                HwLog.v("KeyguardSimPinView", "call supplyPinReportResultForSubscriber(subid=" + this.mSubId + ")");
                final int[] result = Stub.asInterface(ServiceManager.checkService("phone")).supplyPinReportResultForSubscriber(this.mSubId, this.mPin);
                HwLog.v("KeyguardSimPinView", "supplyPinReportResult returned: " + result[0] + " " + result[1]);
                KeyguardSimPinView.this.post(new Runnable() {
                    public void run() {
                        CheckSimPin.this.onSimCheckResponse(result[0], result[1]);
                    }
                });
            } catch (RemoteException e) {
                HwLog.e("KeyguardSimPinView", "RemoteException for supplyPinReportResult:", e);
                KeyguardSimPinView.this.post(new Runnable() {
                    public void run() {
                        CheckSimPin.this.onSimCheckResponse(2, -1);
                    }
                });
            }
        }
    }

    public KeyguardSimPinView(Context context) {
        this(context, null);
    }

    public KeyguardSimPinView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mSimUnlockProgressDialog = null;
        this.mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
            public void onSimStateChanged(int subId, int slotId, State simState) {
                HwLog.v("KeyguardSimPinView", "onSimStateChanged(subId=" + subId + ",state=" + simState + ")");
                KeyguardSimPinView.this.resetState();
            }
        };
        this.mCustKeyguardSimPinView = (HwCustKeyguardSimPinView) HwCustUtils.createObj(HwCustKeyguardSimPinView.class, new Object[]{context, this});
    }

    public void resetState() {
        super.resetState();
        HwLog.v("KeyguardSimPinView", "Resetting state");
        this.mSubId = KeyguardUpdateMonitor.getInstance(this.mContext).getNextSubIdForState(State.PIN_REQUIRED);
        if (SubscriptionManager.isValidSubscriptionId(this.mSubId)) {
            if (getPinRetries(1, this.mSubId) == 0) {
                this.mSecurityMessageDisplay.setMessage(getResources().getString(R$string.emui50_enter_sim_pin), true);
                return;
            }
            int count = TelephonyManager.getDefault().getSimCount();
            CharSequence msg = getResources().getString(R$string.emui50_enter_sim_pin);
            if (count < 2) {
                this.mSecurityMessageDisplay.setMessage(msg, true);
            } else {
                this.mSecurityMessageDisplay.setMessage(getResources().getString(R$string.msim_kg_sim_pin_msg_format, new Object[]{Integer.valueOf(this.mSubId + 1), msg}), true);
            }
        }
    }

    protected int getPinRetries(int pin, int subscription) {
        String strPinRetryNum = BuildConfig.FLAVOR;
        String[] pin1 = new String[]{"gsm.slot1.num.pin1", "gsm.slot2.num.pin1"};
        String singlePin = "gsm.sim.num.pin";
        String configPin = SystemProperties.get("ro.config.pinItem");
        if (!TextUtils.isEmpty(configPin)) {
            String[] config = configPin.split(",");
            pin1[0] = config[0];
            singlePin = config[0];
            if (config.length >= 2) {
                pin1[1] = config[1];
            }
        }
        switch (pin) {
            case 1:
                if (GlobalContext.getTelephonyManager(this.mContext).isMultiSimEnabled()) {
                    if (subscription != 0) {
                        strPinRetryNum = SystemProperties.get(pin1[1]);
                        break;
                    }
                    strPinRetryNum = SystemProperties.get(pin1[0]);
                    break;
                }
                strPinRetryNum = SystemProperties.get(singlePin);
                break;
        }
        if (strPinRetryNum.equals(BuildConfig.FLAVOR) || strPinRetryNum.equals(Integer.toString(-1)) || strPinRetryNum.equals(Integer.toString(0))) {
            strPinRetryNum = "0";
        }
        return Integer.parseInt(strPinRetryNum);
    }

    protected void showCancelButton() {
        View cancel = findViewById(R$id.cancel);
        if (cancel != null) {
            cancel.setVisibility(0);
            cancel.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    HwLog.d("KeyguardSimPinView", "cancel button was clicked !");
                    KeyguardSimPinView.this.doHapticKeyClick();
                    KeyguardSimPinView.this.closeKeyGuard(false, "simPin");
                    KeyguardSimPinView.this.mPasswordEntry.reset(true, true);
                }
            });
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetState();
    }

    protected int getPromtReasonStringRes(int reason) {
        return 0;
    }

    private String getPinPasswordErrorMessage(int attemptsRemaining) {
        String displayMessage;
        if (attemptsRemaining == 0) {
            displayMessage = getContext().getString(R$string.kg_password_wrong_pin_code_pukked);
        } else if (attemptsRemaining > 0) {
            displayMessage = getContext().getResources().getQuantityString(R$plurals.kg_password_wrong_pin_code, attemptsRemaining, new Object[]{Integer.valueOf(attemptsRemaining)});
        } else {
            displayMessage = getContext().getString(R$string.kg_password_pin_failed);
        }
        HwLog.d("KeyguardSimPinView", "getPinPasswordErrorMessage: attemptsRemaining=" + attemptsRemaining + " displayMessage=" + displayMessage);
        return displayMessage;
    }

    protected int getPasswordTextViewId() {
        return R$id.simPinEntry;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mSecurityMessageDisplay.setTimeout(0);
        if (this.mEcaView instanceof EmergencyCarrierArea) {
            ((EmergencyCarrierArea) this.mEcaView).setCarrierTextVisible(true);
        }
        ((TextView) findViewById(R$id.carrier_text)).setVisibility(8);
        if (this.mContext.getResources().getBoolean(R$bool.sim_pinpuk_cancel)) {
            showCancelButton();
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mUpdateMonitorCallback);
        if (this.mCustKeyguardSimPinView != null) {
            this.mCustKeyguardSimPinView.registerReceiver();
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).removeCallback(this.mUpdateMonitorCallback);
        if (this.mCustKeyguardSimPinView != null) {
            this.mCustKeyguardSimPinView.unregisterReceiver();
        }
    }

    public void onPause() {
        if (this.mSimUnlockProgressDialog != null) {
            this.mSimUnlockProgressDialog.dismiss();
            this.mSimUnlockProgressDialog = null;
        }
    }

    private Dialog getSimUnlockProgressDialog() {
        if (this.mSimUnlockProgressDialog == null) {
            this.mSimUnlockProgressDialog = new ProgressDialog(KeyguardUtils.getHwThemeContext(this.mContext));
            this.mSimUnlockProgressDialog.setMessage(this.mContext.getString(R$string.kg_sim_unlock_progress_dialog_message));
            this.mSimUnlockProgressDialog.setIndeterminate(true);
            this.mSimUnlockProgressDialog.setCancelable(false);
            this.mSimUnlockProgressDialog.getWindow().setType(2009);
        }
        return this.mSimUnlockProgressDialog;
    }

    private Dialog getSimRemainingAttemptsDialog(int remaining) {
        String msg = getPinPasswordErrorMessage(remaining);
        if (this.mRemainingAttemptsDialog == null) {
            Builder builder = new Builder(KeyguardUtils.getHwThemeContext(this.mContext));
            builder.setMessage(msg);
            builder.setCancelable(false);
            builder.setNeutralButton(R$string.ok, null);
            this.mRemainingAttemptsDialog = builder.create();
            this.mRemainingAttemptsDialog.getWindow().setType(2009);
        } else {
            this.mRemainingAttemptsDialog.setMessage(msg);
        }
        return this.mRemainingAttemptsDialog;
    }

    protected void verifyPasswordAndUnlock() {
        String entry = this.mPasswordEntry.getText();
        if (entry.length() < 4 || entry.length() > 8) {
            this.mSecurityMessageDisplay.setMessage(R$string.kg_invalid_sim_pin_hint, true);
            resetPasswordText(true, true);
            this.mCallback.userActivity();
            return;
        }
        getSimUnlockProgressDialog().show();
        if (this.mCheckSimPinThread == null) {
            this.mCheckSimPinThread = new CheckSimPin(this, this.mPasswordEntry.getText(), this.mSubId) {
                void onSimCheckResponse(final int result, final int attemptsRemaining) {
                    this.post(new Runnable() {
                        public void run() {
                            boolean z;
                            if (this.mSimUnlockProgressDialog != null) {
                                this.mSimUnlockProgressDialog.hide();
                            }
                            KeyguardSimPinView keyguardSimPinView = this;
                            if (result != 0) {
                                z = true;
                            } else {
                                z = false;
                            }
                            keyguardSimPinView.resetPasswordText(true, z);
                            if (result == 0) {
                                KeyguardUpdateMonitor.getInstance(this.getContext()).reportSimUnlocked(this.mSubId);
                                this.mPasswordEntry.hideErrorEffect();
                                this.mCallback.dismiss(true);
                            } else {
                                this.mPasswordEntry.showErrorEffect();
                                if (result != 1) {
                                    this.mSecurityMessageDisplay.setMessage(this.getContext().getString(R$string.kg_password_pin_failed), true);
                                } else if (attemptsRemaining <= 2) {
                                    this.getSimRemainingAttemptsDialog(attemptsRemaining).show();
                                    this.mSecurityMessageDisplay.setMessage(this.getResources().getString(R$string.kg_password_enter_wrong_pin_code, new Object[]{Integer.valueOf(attemptsRemaining)}), true);
                                } else {
                                    this.mSecurityMessageDisplay.setMessage(this.getPinPasswordErrorMessage(attemptsRemaining), true);
                                }
                                HwLog.d("KeyguardSimPinView", "verifyPasswordAndUnlock  CheckSimPin.onSimCheckResponse: " + result + " attemptsRemaining=" + attemptsRemaining);
                            }
                            this.mCallback.userActivity();
                            this.mCheckSimPinThread = null;
                        }
                    });
                }
            };
            GlobalContext.getPoolExecutor().execute(this.mCheckSimPinThread);
        }
    }

    public void startAppearAnimation() {
    }

    protected void onUserInput() {
        super.onUserInput();
        if (this.mPasswordEntry.isShownErrEffect()) {
            this.mPasswordEntry.hideErrorEffect();
        }
    }

    public boolean startDisappearAnimation(Runnable finishRunnable) {
        return false;
    }

    protected SecurityMode getSecurityMode() {
        return SecurityMode.SimPin;
    }

    protected boolean isSupportFastVerify() {
        return false;
    }
}
