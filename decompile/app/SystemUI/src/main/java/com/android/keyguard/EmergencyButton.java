package com.android.keyguard;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.internal.widget.LockPatternUtils;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.support.CustFeature;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.HwUnlockUtils;
import com.huawei.keyguard.util.OsUtils;
import fyusion.vislib.BuildConfig;

public class EmergencyButton extends Button implements Callback {
    private static final Intent INTENT_EMERGENCY_DIAL = new Intent().setAction("com.android.phone.EmergencyDialer.DIAL").setPackage("com.android.phone").setFlags(343932928);
    private EmergencyButtonCallback mEmergencyButtonCallback;
    private final boolean mEnableEmergencyCallWhileSimLocked;
    KeyguardUpdateMonitorCallback mInfoCallback;
    private final boolean mIsVoiceCapable;
    private LockPatternUtils mLockPatternUtils;
    private PowerManager mPowerManager;

    public interface EmergencyButtonCallback {
        void onEmergencyButtonClickedWhenInCall();
    }

    public EmergencyButton(Context context) {
        this(context, null);
    }

    public EmergencyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mInfoCallback = new KeyguardUpdateMonitorCallback() {
            public void onSimStateChanged(int subId, int slotId, State simState) {
                EmergencyButton.this.updateEmergencyCallButton();
            }

            public void onPhoneStateChanged(int phoneState) {
                EmergencyButton.this.updateEmergencyCallButton();
            }
        };
        this.mIsVoiceCapable = context.getResources().getBoolean(17956956);
        this.mEnableEmergencyCallWhileSimLocked = this.mContext.getResources().getBoolean(17956940);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mInfoCallback);
        AppHandler.addListener(this);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).removeCallback(this.mInfoCallback);
        AppHandler.removeListener(this);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (CustFeature.isDirectDialEmerCall(v, EmergencyButton.this.mContext)) {
                    CustFeature.directDialEmerCall();
                } else {
                    EmergencyButton.this.takeEmergencyCallAction();
                }
            }
        });
        setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == 0) {
                    HwUnlockUtils.vibrate(EmergencyButton.this.mContext);
                }
                return false;
            }
        });
        updateEmergencyCallButton();
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateEmergencyCallButton();
    }

    public void takeEmergencyCallAction() {
        MetricsLogger.action(this.mContext, 200);
        this.mPowerManager.userActivity(SystemClock.uptimeMillis(), true);
        if (isInCall()) {
            resumeCall();
            if (this.mEmergencyButtonCallback != null) {
                this.mEmergencyButtonCallback.onEmergencyButtonClickedWhenInCall();
            }
        } else {
            KeyguardUpdateMonitor.getInstance(this.mContext).reportEmergencyCallAction(true);
            getContext().startActivityAsUser(INTENT_EMERGENCY_DIAL, ActivityOptions.makeCustomAnimation(getContext(), 0, 0).toBundle(), new UserHandle(KeyguardUpdateMonitor.getCurrentUser()));
        }
        HwLockScreenReporter.report(this.mContext, 158, BuildConfig.FLAVOR);
    }

    private void updateEmergencyCallButton() {
        if (this.mIsVoiceCapable) {
            boolean visible;
            if (isInCall()) {
                visible = true;
            } else if (KeyguardUpdateMonitor.getInstance(this.mContext).isSimPinVoiceSecure()) {
                visible = this.mEnableEmergencyCallWhileSimLocked;
            } else {
                visible = this.mLockPatternUtils.isSecure(KeyguardUpdateMonitor.getCurrentUser());
            }
            if (hasCdmaSimCard()) {
                visible = true;
            } else {
                boolean isAirplaneModeOn;
                if (OsUtils.getSystemInt(this.mContext, "airplane_mode_on", 0) != 0) {
                    isAirplaneModeOn = true;
                } else {
                    isAirplaneModeOn = false;
                }
                if (!(isAirplaneModeOn || KeyguardUpdateMonitor.getInstance(this.mContext).isSupportEmergencyCall() || isVolteAvailable())) {
                    HwLog.i("EmergencyButton", "service state is not support Emergency call");
                    visible = false;
                }
            }
            if (visible) {
                int textId;
                setVisibility(0);
                if (isInCall()) {
                    textId = 17040018;
                } else {
                    textId = 17040017;
                }
                setText(textId);
            } else {
                setVisibility(4);
            }
            return;
        }
        setVisibility(4);
    }

    public void setCallback(EmergencyButtonCallback callback) {
        this.mEmergencyButtonCallback = callback;
    }

    private void resumeCall() {
        getTelecommManager().showInCallScreen(false);
    }

    private boolean isInCall() {
        return getTelecommManager().isInCall();
    }

    private TelecomManager getTelecommManager() {
        return (TelecomManager) this.mContext.getSystemService("telecom");
    }

    public boolean handleMessage(Message msg) {
        if (msg.what == 117) {
            updateEmergencyCallButton();
        }
        return false;
    }

    private boolean hasCdmaSimCard() {
        boolean isCdma;
        if (GlobalContext.getTelephonyManager(this.mContext).isMultiSimEnabled()) {
            for (int i = 0; i <= 1; i++) {
                if (2 == GlobalContext.getMSimTelephonyManager(this.mContext).getCurrentPhoneType(i)) {
                    isCdma = true;
                } else {
                    isCdma = false;
                }
                if (isCdma) {
                    return true;
                }
            }
        } else {
            if (2 == GlobalContext.getTelephonyManager(this.mContext).getPhoneType()) {
                isCdma = true;
            } else {
                isCdma = false;
            }
            if (isCdma) {
                return true;
            }
        }
        return false;
    }

    private boolean isVolteAvailable() {
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm != null) {
            return tm.isImsRegistered();
        }
        HwLog.i("EmergencyButton", "isVolteAvailable::tm is null!");
        return false;
    }
}
