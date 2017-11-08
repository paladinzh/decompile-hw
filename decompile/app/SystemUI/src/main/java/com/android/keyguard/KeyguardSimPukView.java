package com.android.keyguard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.android.internal.telephony.ITelephony.Stub;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.keyguard.KeyguardSecurityModel.SecurityMode;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.support.CustFeature;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.KeyguardUtils;
import fyusion.vislib.BuildConfig;

public class KeyguardSimPukView extends KeyguardPinBasedInputView {
    private CheckSimPuk mCheckSimPukThread;
    private String mPinText;
    private String mPukText;
    private AlertDialog mRemainingAttemptsDialog;
    private ProgressDialog mSimUnlockProgressDialog;
    private StateMachine mStateMachine;
    private int mSubId;
    KeyguardUpdateMonitorCallback mUpdateMonitorCallback;
    protected boolean mVerifyPukFailed;

    private abstract class CheckSimPuk implements Runnable {
        private final String mPin;
        private final String mPuk;
        private final int mSubId;

        abstract void onSimLockChangedResponse(int i, int i2);

        protected CheckSimPuk(String puk, String pin, int subId) {
            this.mPuk = puk;
            this.mPin = pin;
            this.mSubId = subId;
        }

        public void run() {
            try {
                HwLog.v("KeyguardSimPukView", "call supplyPukReportResult()");
                final int[] result = Stub.asInterface(ServiceManager.checkService("phone")).supplyPukReportResultForSubscriber(this.mSubId, this.mPuk, this.mPin);
                HwLog.v("KeyguardSimPukView", "supplyPukReportResult returned: " + result[0] + " " + result[1]);
                KeyguardSimPukView.this.post(new Runnable() {
                    public void run() {
                        CheckSimPuk.this.onSimLockChangedResponse(result[0], result[1]);
                    }
                });
            } catch (RemoteException e) {
                HwLog.e("KeyguardSimPukView", "RemoteException for supplyPukReportResult:", e);
                KeyguardSimPukView.this.post(new Runnable() {
                    public void run() {
                        CheckSimPuk.this.onSimLockChangedResponse(2, -1);
                    }
                });
            }
        }
    }

    private class StateMachine {
        private int state;

        private StateMachine() {
            this.state = 0;
        }

        public void next() {
            int msg = 0;
            if (this.state == 0) {
                if (KeyguardSimPukView.this.checkPuk()) {
                    this.state = 1;
                    msg = R$string.kg_puk_enter_pin_hint;
                } else {
                    msg = R$string.kg_invalid_sim_puk_hint;
                }
            } else if (this.state == 1) {
                if (KeyguardSimPukView.this.checkPin()) {
                    this.state = 2;
                    msg = R$string.kg_enter_confirm_pin_hint;
                } else {
                    msg = R$string.kg_invalid_sim_pin_hint;
                }
            } else if (this.state == 2) {
                if (KeyguardSimPukView.this.confirmPin()) {
                    this.state = 3;
                    msg = R$string.keyguard_sim_unlock_progress_dialog_message;
                    KeyguardSimPukView.this.updateSim();
                } else {
                    this.state = 1;
                    msg = R$string.kg_invalid_confirm_pin_hint;
                }
            }
            KeyguardSimPukView.this.resetPasswordText(true, true);
            if (msg != 0) {
                KeyguardSimPukView.this.mSecurityMessageDisplay.setMessage(msg, true);
            }
        }

        void reset() {
            KeyguardSimPukView.this.mPinText = BuildConfig.FLAVOR;
            KeyguardSimPukView.this.mPukText = BuildConfig.FLAVOR;
            this.state = 0;
            KeyguardSimPukView.this.mSubId = KeyguardUpdateMonitor.getInstance(KeyguardSimPukView.this.mContext).getNextSubIdForState(State.PUK_REQUIRED);
            if (SubscriptionManager.isValidSubscriptionId(KeyguardSimPukView.this.mSubId)) {
                int count = TelephonyManager.getDefault().getSimCount();
                int pukRetries = KeyguardSimPukView.this.getPukRetries(1, KeyguardSimPukView.this.mSubId);
                Resources rez = KeyguardSimPukView.this.getResources();
                CharSequence msg;
                if (count < 2) {
                    msg = rez.getString(R$string.kg_invalid_puk, new Object[]{Integer.valueOf(pukRetries)});
                    if (KeyguardSimPukView.this.mVerifyPukFailed) {
                        KeyguardSimPukView.this.mSecurityMessageDisplay.setMessage(msg, true);
                    } else {
                        KeyguardSimPukView.this.mSecurityMessageDisplay.setMessage(rez.getString(R$string.kg_puk_enter_puk_hint, new Object[]{Integer.valueOf(pukRetries)}), true);
                    }
                } else if (KeyguardSimPukView.this.mVerifyPukFailed) {
                    msg = rez.getString(R$string.kg_invalid_puk, new Object[]{Integer.valueOf(pukRetries)});
                    KeyguardSimPukView.this.mSecurityMessageDisplay.setMessage(rez.getString(R$string.msim_kg_sim_pin_msg_format, new Object[]{Integer.valueOf(KeyguardSimPukView.this.mSubId + 1), msg}), true);
                } else {
                    msg = rez.getString(R$string.kg_puk_enter_puk_hint, new Object[]{Integer.valueOf(pukRetries)});
                    KeyguardSimPukView.this.mSecurityMessageDisplay.setMessage(rez.getString(R$string.msim_kg_sim_pin_msg_format, new Object[]{Integer.valueOf(KeyguardSimPukView.this.mSubId + 1), msg}), true);
                }
            }
            KeyguardSimPukView.this.mPasswordEntry.requestFocus();
        }
    }

    public KeyguardSimPukView(Context context) {
        this(context, null);
    }

    public KeyguardSimPukView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mVerifyPukFailed = false;
        this.mSimUnlockProgressDialog = null;
        this.mStateMachine = new StateMachine();
        this.mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
            public void onSimStateChanged(int subId, int slotId, State simState) {
                HwLog.v("KeyguardSimPukView", "onSimStateChanged(subId=" + subId + ",state=" + simState + ")");
                KeyguardSimPukView.this.resetState();
            }
        };
    }

    protected int getPukRetries(int puk, int subscription) {
        String strPukRetryNum = BuildConfig.FLAVOR;
        String[] puk1 = new String[]{"gsm.slot1.num.puk1", "gsm.slot2.num.puk1"};
        switch (puk) {
            case 1:
                if (GlobalContext.getTelephonyManager(this.mContext).isMultiSimEnabled()) {
                    if (subscription != 0) {
                        strPukRetryNum = SystemProperties.get(puk1[1]);
                        break;
                    }
                    strPukRetryNum = SystemProperties.get(puk1[0]);
                    break;
                }
                strPukRetryNum = SystemProperties.get("gsm.sim.num.puk");
                break;
        }
        if (strPukRetryNum.equals(BuildConfig.FLAVOR) || strPukRetryNum.equals(Integer.toString(-1)) || strPukRetryNum.equals(Integer.toString(0))) {
            strPukRetryNum = "0";
        }
        return Integer.parseInt(strPukRetryNum);
    }

    protected int getPromtReasonStringRes(int reason) {
        return 0;
    }

    private String getPukPasswordErrorMessage(int attemptsRemaining) {
        String displayMessage;
        if (attemptsRemaining == 0) {
            displayMessage = getContext().getString(R$string.kg_password_wrong_puk_code_dead);
        } else if (attemptsRemaining > 0) {
            displayMessage = getContext().getResources().getQuantityString(R$plurals.kg_password_wrong_puk_code, attemptsRemaining, new Object[]{Integer.valueOf(attemptsRemaining)});
        } else {
            displayMessage = getContext().getString(R$string.kg_password_puk_failed);
        }
        HwLog.d("KeyguardSimPukView", "getPukPasswordErrorMessage: attemptsRemaining=" + attemptsRemaining + " displayMessage=" + displayMessage);
        return displayMessage;
    }

    public void resetState() {
        super.resetState();
        this.mStateMachine.reset();
    }

    protected int getPasswordTextViewId() {
        return R$id.pukEntry;
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

    protected void showCancelButton() {
        View cancel = findViewById(R$id.cancel);
        if (cancel != null) {
            cancel.setVisibility(0);
            cancel.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    HwLog.d("KeyguardSimPukView", "cancel button was clicked !");
                    KeyguardSimPukView.this.doHapticKeyClick();
                    KeyguardSimPukView.this.closeKeyGuard(false, "simPuk");
                    KeyguardSimPukView.this.mPasswordEntry.reset(true, true);
                }
            });
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mUpdateMonitorCallback);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).removeCallback(this.mUpdateMonitorCallback);
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
            if (!(this.mContext instanceof Activity)) {
                this.mSimUnlockProgressDialog.getWindow().setType(2009);
            }
        }
        return this.mSimUnlockProgressDialog;
    }

    private Dialog getPukRemainingAttemptsDialog(int remaining) {
        String msg = getPukPasswordErrorMessage(remaining);
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

    private boolean checkPuk() {
        if (this.mPasswordEntry.getText().length() != 8) {
            return false;
        }
        this.mPukText = this.mPasswordEntry.getText();
        return true;
    }

    private boolean checkPin() {
        int length = this.mPasswordEntry.getText().length();
        if (length < 4 || length > 8) {
            return false;
        }
        this.mPinText = this.mPasswordEntry.getText();
        return true;
    }

    public boolean confirmPin() {
        return this.mPinText.equals(this.mPasswordEntry.getText());
    }

    private void updateSim() {
        getSimUnlockProgressDialog().show();
        if (this.mCheckSimPukThread == null) {
            final KeyguardSimPukView keyguardSimPukView = this;
            this.mCheckSimPukThread = new CheckSimPuk(this, this.mPukText, this.mPinText, this.mSubId) {
                void onSimLockChangedResponse(final int result, final int attemptsRemaining) {
                    keyguardSimPukView.post(new Runnable() {
                        public void run() {
                            boolean z;
                            if (keyguardSimPukView.mSimUnlockProgressDialog != null) {
                                keyguardSimPukView.mSimUnlockProgressDialog.hide();
                            }
                            KeyguardSimPukView keyguardSimPukView = keyguardSimPukView;
                            if (result != 0) {
                                z = true;
                            } else {
                                z = false;
                            }
                            keyguardSimPukView.resetPasswordText(true, z);
                            if (result == 0) {
                                CustFeature.showPINChangeSuccessToast(keyguardSimPukView.getContext());
                                keyguardSimPukView.mVerifyPukFailed = false;
                                KeyguardUpdateMonitor.getInstance(keyguardSimPukView.getContext()).reportSimUnlocked(keyguardSimPukView.mSubId);
                                keyguardSimPukView.mPasswordEntry.hideErrorEffect();
                                keyguardSimPukView.mCallback.dismiss(true);
                            } else {
                                keyguardSimPukView.mVerifyPukFailed = true;
                                keyguardSimPukView.mPasswordEntry.showErrorEffect();
                                if (result != 1) {
                                    keyguardSimPukView.mSecurityMessageDisplay.setMessage(keyguardSimPukView.getContext().getString(R$string.kg_password_puk_failed), true);
                                } else if (attemptsRemaining <= 2) {
                                    keyguardSimPukView.getPukRemainingAttemptsDialog(attemptsRemaining).show();
                                }
                                HwLog.d("KeyguardSimPukView", "verifyPasswordAndUnlock  UpdateSim.onSimCheckResponse:  attemptsRemaining=" + attemptsRemaining);
                                keyguardSimPukView.mStateMachine.reset();
                            }
                            keyguardSimPukView.mCheckSimPukThread = null;
                        }
                    });
                }
            };
            GlobalContext.getPoolExecutor().execute(this.mCheckSimPukThread);
        }
    }

    protected void verifyPasswordAndUnlock() {
        this.mStateMachine.next();
    }

    public void startAppearAnimation() {
        startAppearAnimationHw(null);
    }

    protected void onUserInput() {
        super.onUserInput();
        if (this.mPasswordEntry.isShownErrEffect()) {
            this.mPasswordEntry.hideErrorEffect();
        }
    }

    public boolean startDisappearAnimation(Runnable finishRunnable) {
        return startDisappearAnimationHw(finishRunnable);
    }

    protected SecurityMode getSecurityMode() {
        return SecurityMode.SimPuk;
    }

    protected boolean isSupportFastVerify() {
        return false;
    }
}
