package com.android.keyguard;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.view.View;

public class HwCustEmergencyButtonImpl extends HwCustEmergencyButton {
    private final boolean isDirectDialEmerCall = SystemProperties.getBoolean("ro.config.dial_emercall_direct", false);
    private Context mContext;
    private PowerManager mPowerManager;

    public boolean isDirectDialEmerCall(Context context) {
        this.mContext = context;
        return this.isDirectDialEmerCall;
    }

    public String checkAndGetEmergencyNumber(View v) {
        PasswordTextView passwordTextView = null;
        View linearView1 = (View) v.getParent();
        if (linearView1 == null) {
            return null;
        }
        View keyguardSimView = (View) linearView1.getParent();
        if (keyguardSimView == null) {
            return null;
        }
        if (keyguardSimView instanceof KeyguardSimPinView) {
            passwordTextView = (PasswordTextView) keyguardSimView.findViewById(R$id.simPinEntry);
        } else if (keyguardSimView instanceof KeyguardSimPukView) {
            passwordTextView = (PasswordTextView) keyguardSimView.findViewById(R$id.pukEntry);
        } else {
            View keyguardPINView = (View) keyguardSimView.getParent();
            if (keyguardPINView == null) {
                return null;
            }
            if (keyguardPINView instanceof KeyguardPINView) {
                passwordTextView = (PasswordTextView) keyguardPINView.findViewById(R$id.pinEntry);
            }
        }
        if (passwordTextView != null) {
            String pinEntryText = passwordTextView.getText();
            if (PhoneNumberUtils.isEmergencyNumber(pinEntryText)) {
                return pinEntryText;
            }
        }
        return null;
    }

    public void dialEmergencyCallDirectly(String EmergencyNumber) {
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mPowerManager.userActivity(SystemClock.uptimeMillis(), true);
        if (TelephonyManager.from(this.mContext).getCallState() == 2) {
            resumeCall();
            return;
        }
        Intent intent = new Intent("android.intent.action.CALL_EMERGENCY", Uri.fromParts("tel", EmergencyNumber, null));
        intent.setFlags(268435456);
        this.mContext.startActivityAsUser(intent, new UserHandle(KeyguardUpdateMonitor.getCurrentUser()));
    }

    private void resumeCall() {
        getTelecommManager().showInCallScreen(false);
    }

    private TelecomManager getTelecommManager() {
        return (TelecomManager) this.mContext.getSystemService("telecom");
    }
}
