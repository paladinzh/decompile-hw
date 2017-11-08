package com.android.keyguard;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import android.text.method.SingleLineTransformationMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.settingslib.WirelessUtils;
import com.huawei.keyguard.util.HwLog;
import fyusion.vislib.BuildConfig;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CarrierText extends TextView {
    private static final /* synthetic */ int[] -com-android-internal-telephony-IccCardConstants$StateSwitchesValues = null;
    private static final /* synthetic */ int[] -com-android-keyguard-CarrierText$StatusModeSwitchesValues = null;
    private static CharSequence mSeparator;
    private KeyguardUpdateMonitorCallback mCallback;
    private final boolean mIsEmergencyCallCapable;
    private KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private WifiManager mWifiManager;

    private class CarrierTextTransformationMethod extends SingleLineTransformationMethod {
        private final boolean mAllCaps;
        private final Locale mLocale;

        public CarrierTextTransformationMethod(Context context, boolean allCaps) {
            this.mLocale = context.getResources().getConfiguration().locale;
            this.mAllCaps = allCaps;
        }

        public CharSequence getTransformation(CharSequence source, View view) {
            source = super.getTransformation(source, view);
            if (!this.mAllCaps || source == null) {
                return source;
            }
            return source.toString().toUpperCase(this.mLocale);
        }
    }

    private enum StatusMode {
        Normal,
        NetworkLocked,
        SimMissing,
        SimMissingLocked,
        SimPukLocked,
        SimLocked,
        SimPermDisabled,
        SimNotReady
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-IccCardConstants$StateSwitchesValues() {
        if (-com-android-internal-telephony-IccCardConstants$StateSwitchesValues != null) {
            return -com-android-internal-telephony-IccCardConstants$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.ABSENT.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.CARD_IO_ERROR.ordinal()] = 17;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.DEACTIVED.ordinal()] = 18;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.NETWORK_LOCKED.ordinal()] = 2;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[State.NOT_READY.ordinal()] = 3;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[State.PERM_DISABLED.ordinal()] = 4;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[State.PIN_REQUIRED.ordinal()] = 5;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[State.PUK_REQUIRED.ordinal()] = 6;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[State.READY.ordinal()] = 7;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[State.UNKNOWN.ordinal()] = 8;
        } catch (NoSuchFieldError e10) {
        }
        -com-android-internal-telephony-IccCardConstants$StateSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getcom-android-keyguard-CarrierText$StatusModeSwitchesValues() {
        if (-com-android-keyguard-CarrierText$StatusModeSwitchesValues != null) {
            return -com-android-keyguard-CarrierText$StatusModeSwitchesValues;
        }
        int[] iArr = new int[StatusMode.values().length];
        try {
            iArr[StatusMode.NetworkLocked.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[StatusMode.Normal.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[StatusMode.SimLocked.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[StatusMode.SimMissing.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[StatusMode.SimMissingLocked.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[StatusMode.SimNotReady.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[StatusMode.SimPermDisabled.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[StatusMode.SimPukLocked.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        -com-android-keyguard-CarrierText$StatusModeSwitchesValues = iArr;
        return iArr;
    }

    public CarrierText(Context context) {
        this(context, null);
    }

    public CarrierText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mCallback = new KeyguardUpdateMonitorCallback() {
            public void onRefreshCarrierInfo() {
                CarrierText.this.updateCarrierText();
            }

            public void onFinishedGoingToSleep(int why) {
                CarrierText.this.setSelected(false);
            }

            public void onStartedWakingUp() {
                CarrierText.this.setSelected(true);
            }
        };
        this.mIsEmergencyCallCapable = context.getResources().getBoolean(17956956);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R$styleable.CarrierText, 0, 0);
        try {
            boolean useAllCaps = a.getBoolean(R$styleable.CarrierText_allCaps, false);
            setTransformationMethod(new CarrierTextTransformationMethod(this.mContext, useAllCaps));
            this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        } finally {
            a.recycle();
        }
    }

    protected void updateCarrierText() {
        boolean allSimsMissing = true;
        boolean anySimReadyAndInService = false;
        CharSequence displayText = null;
        List<SubscriptionInfo> subs = this.mKeyguardUpdateMonitor.getSubscriptionInfo(false);
        int N = subs.size();
        HwLog.d("CarrierText", "updateCarrierText(): " + N);
        for (int i = 0; i < N; i++) {
            int subId = ((SubscriptionInfo) subs.get(i)).getSubscriptionId();
            State simState = this.mKeyguardUpdateMonitor.getSimState(subId);
            CharSequence carrierName = ((SubscriptionInfo) subs.get(i)).getCarrierName();
            CharSequence carrierTextForSimState = getCarrierTextForSimState(simState, carrierName);
            HwLog.d("CarrierText", "Handling (subId=" + subId + "): " + simState + " " + carrierName);
            if (carrierTextForSimState != null) {
                allSimsMissing = false;
                displayText = concatenate(displayText, carrierTextForSimState);
            }
            if (simState == State.READY) {
                ServiceState ss = (ServiceState) this.mKeyguardUpdateMonitor.mServiceStates.get(Integer.valueOf(subId));
                if (!(ss == null || ss.getDataRegState() != 0 || (ss.getRilDataRadioTechnology() == 18 && (!this.mWifiManager.isWifiEnabled() || this.mWifiManager.getConnectionInfo() == null || this.mWifiManager.getConnectionInfo().getBSSID() == null)))) {
                    HwLog.d("CarrierText", "SIM ready and in service: subId=" + subId + ", ss=" + ss);
                    anySimReadyAndInService = true;
                }
            }
        }
        if (allSimsMissing) {
            if (N != 0) {
                displayText = makeCarrierStringOnEmergencyCapable(getContext().getText(R$string.keyguard_missing_sim_message_short), ((SubscriptionInfo) subs.get(0)).getCarrierName());
            } else {
                CharSequence text = getContext().getText(17040036);
                Intent i2 = getContext().registerReceiver(null, new IntentFilter("android.provider.Telephony.SPN_STRINGS_UPDATED"));
                if (i2 != null) {
                    String spn = BuildConfig.FLAVOR;
                    String plmn = BuildConfig.FLAVOR;
                    if (i2.getBooleanExtra("showSpn", false)) {
                        spn = i2.getStringExtra("spn");
                    }
                    if (i2.getBooleanExtra("showPlmn", false)) {
                        plmn = i2.getStringExtra("plmn");
                    }
                    HwLog.d("CarrierText", "Getting plmn/spn sticky brdcst " + plmn + "/" + spn);
                    if (Objects.equals(plmn, spn)) {
                        text = plmn;
                    } else {
                        text = concatenate(plmn, spn);
                    }
                }
                displayText = makeCarrierStringOnEmergencyCapable(getContext().getText(R$string.keyguard_missing_sim_message_short), text);
            }
        }
        if (!anySimReadyAndInService && WirelessUtils.isAirplaneModeOn(this.mContext)) {
            displayText = getContext().getString(R$string.airplane_mode);
        }
        setText(displayText);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        mSeparator = getResources().getString(17040659);
        setSelected(KeyguardUpdateMonitor.getInstance(this.mContext).isDeviceInteractive());
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (ConnectivityManager.from(this.mContext).isNetworkSupported(0)) {
            this.mKeyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
            this.mKeyguardUpdateMonitor.registerCallback(this.mCallback);
            return;
        }
        this.mKeyguardUpdateMonitor = null;
        setText(BuildConfig.FLAVOR);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mKeyguardUpdateMonitor != null) {
            this.mKeyguardUpdateMonitor.removeCallback(this.mCallback);
        }
    }

    private CharSequence getCarrierTextForSimState(State simState, CharSequence text) {
        switch (-getcom-android-keyguard-CarrierText$StatusModeSwitchesValues()[getStatusForIccState(simState).ordinal()]) {
            case 1:
                return makeCarrierStringOnEmergencyCapable(this.mContext.getText(R$string.keyguard_network_locked_message), text);
            case 2:
                return text;
            case 3:
                return makeCarrierStringOnEmergencyCapable(getContext().getText(R$string.keyguard_sim_locked_message), text);
            case 4:
                return null;
            case 5:
                return null;
            case 6:
                return BuildConfig.FLAVOR;
            case 7:
                return getContext().getText(R$string.keyguard_permanent_disabled_sim_message_short);
            case 8:
                return makeCarrierStringOnEmergencyCapable(getContext().getText(R$string.keyguard_sim_puk_locked_message), text);
            default:
                return null;
        }
    }

    private CharSequence makeCarrierStringOnEmergencyCapable(CharSequence simMessage, CharSequence emergencyCallMessage) {
        if (this.mIsEmergencyCallCapable) {
            return concatenate(simMessage, emergencyCallMessage);
        }
        return simMessage;
    }

    private StatusMode getStatusForIccState(State simState) {
        boolean missingAndNotProvisioned = true;
        if (simState == null) {
            return StatusMode.Normal;
        }
        if (KeyguardUpdateMonitor.getInstance(this.mContext).isDeviceProvisioned()) {
            missingAndNotProvisioned = false;
        } else if (!(simState == State.ABSENT || simState == State.PERM_DISABLED)) {
            missingAndNotProvisioned = false;
        }
        if (missingAndNotProvisioned) {
            simState = State.NETWORK_LOCKED;
        }
        switch (-getcom-android-internal-telephony-IccCardConstants$StateSwitchesValues()[simState.ordinal()]) {
            case 1:
                return StatusMode.SimMissing;
            case 2:
                return StatusMode.SimMissingLocked;
            case 3:
                return StatusMode.SimNotReady;
            case 4:
                return StatusMode.SimPermDisabled;
            case 5:
                return StatusMode.SimLocked;
            case 6:
                return StatusMode.SimPukLocked;
            case 7:
                return StatusMode.Normal;
            case 8:
                return StatusMode.SimMissing;
            default:
                return StatusMode.SimMissing;
        }
    }

    private static CharSequence concatenate(CharSequence plmn, CharSequence spn) {
        boolean plmnValid = !TextUtils.isEmpty(plmn);
        boolean spnValid = !TextUtils.isEmpty(spn);
        if (plmnValid && spnValid) {
            return plmn + mSeparator + spn;
        }
        if (plmnValid) {
            return plmn;
        }
        if (spnValid) {
            return spn;
        }
        return BuildConfig.FLAVOR;
    }
}
