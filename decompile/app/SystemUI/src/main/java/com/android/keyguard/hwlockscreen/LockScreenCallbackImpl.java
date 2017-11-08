package com.android.keyguard.hwlockscreen;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.animation.Animation;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitor.BatteryStatus;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.R$string;
import com.huawei.internal.telephony.IccCardEx;
import com.huawei.internal.telephony.IccCardEx.State;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.HwUnlockConstants$Status;
import com.huawei.keyguard.data.BatteryStateInfo;
import com.huawei.keyguard.support.RemoteLockUtils;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.HwUnlockUtils;
import com.huawei.telephony.HuaweiTelephonyManager;
import fyusion.vislib.BuildConfig;
import java.text.NumberFormat;

public class LockScreenCallbackImpl extends KeyguardUpdateMonitorCallback implements HwUnlockInterface$LockScreenCallback {
    private static final /* synthetic */ int[] -com-huawei-internal-telephony-IccCardEx$StateSwitchesValues = null;
    private static final /* synthetic */ int[] -com-huawei-keyguard-HwUnlockConstants$StatusSwitchesValues = null;
    private static final String DSDS_MODE_PROP = SystemProperties.get("ro.config.dsds_mode", BuildConfig.FLAVOR);
    private static final boolean MSIM_REMOVE_ABSENT_CARRIER_INFO;
    private boolean isDisplayEmergencyMessage = "true".equals(SystemProperties.get("ro.config.emergency_atick", "false"));
    private IccText mCDMAText;
    private IccText mCard1Text;
    private IccText mCard2Text;
    private IccText[] mCardsLayoutText;
    private CharSequence[] mCarrierInfo;
    private Drawable mChargeIcon = null;
    private int mChargeLevel = 100;
    private String mChargingInfo;
    private Context mContext;
    private int mDsdsMode;
    private IccText mGSMText;
    private HwLockScreenPanel mHwlockscreen;
    boolean mIsCharged = false;
    boolean mIsLow = false;
    private boolean mIsMultiSimEnabled;
    private LockPatternUtils mLockPatternUtils;
    private HwUnlockInterface$HwLockScreenReal mLockScreen = null;
    private String mOwnerInfo;
    private int mPhoneState;
    private boolean mPlugIn = false;
    private PowerManager mPowerManager;
    private boolean mShowBatteryInfo = true;
    private boolean mShowOwnerInfo = false;
    private boolean mShowUnlock = true;
    private MSimTelephonyManager mSimTeleManager;
    private IccText mSimText;
    private HwUnlockConstants$Status[] mStatus;
    private TelephonyManager mTeleManager;
    private IccText mUimText;
    private HwKeyguardUpdateMonitor mUpdateMonitor;

    private static final class IccText {
        int iccMissingMessageShort;
        int iccNotReady;
        int iccPinLockedMessage;
        int iccPukLockedMessage;
        int iccUnActivate;
        int networkLockedMessage;

        private IccText() {
            this.iccPukLockedMessage = 0;
            this.iccPinLockedMessage = 0;
            this.iccUnActivate = 0;
            this.iccNotReady = 0;
            this.iccMissingMessageShort = 0;
            this.networkLockedMessage = 0;
        }
    }

    private static /* synthetic */ int[] -getcom-huawei-internal-telephony-IccCardEx$StateSwitchesValues() {
        if (-com-huawei-internal-telephony-IccCardEx$StateSwitchesValues != null) {
            return -com-huawei-internal-telephony-IccCardEx$StateSwitchesValues;
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
            iArr[State.PERM_DISABLED.ordinal()] = 19;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[State.PERSO_LOCKED.ordinal()] = 4;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[State.PIN_REQUIRED.ordinal()] = 5;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[State.PUK_REQUIRED.ordinal()] = 6;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[State.READY.ordinal()] = 7;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[State.RUIM_CORPORATE_LOCKED.ordinal()] = 20;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[State.RUIM_HRPD_LOCKED.ordinal()] = 21;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[State.RUIM_NETWORK1_LOCKED.ordinal()] = 22;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[State.RUIM_NETWORK2_LOCKED.ordinal()] = 23;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[State.RUIM_RUIM_LOCKED.ordinal()] = 24;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[State.RUIM_SERVICE_PROVIDER_LOCKED.ordinal()] = 25;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[State.SIM_CORPORATE_LOCKED.ordinal()] = 26;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[State.SIM_CORPORATE_LOCKED_PUK.ordinal()] = 27;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[State.SIM_NETWORK_LOCKED_PUK.ordinal()] = 28;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[State.SIM_NETWORK_SUBSET_LOCKED.ordinal()] = 29;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[State.SIM_NETWORK_SUBSET_LOCKED_PUK.ordinal()] = 30;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[State.SIM_SERVICE_PROVIDER_LOCKED.ordinal()] = 31;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[State.SIM_SERVICE_PROVIDER_LOCKED_PUK.ordinal()] = 32;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[State.SIM_SIM_LOCKED.ordinal()] = 33;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[State.UNKNOWN.ordinal()] = 8;
        } catch (NoSuchFieldError e25) {
        }
        -com-huawei-internal-telephony-IccCardEx$StateSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getcom-huawei-keyguard-HwUnlockConstants$StatusSwitchesValues() {
        if (-com-huawei-keyguard-HwUnlockConstants$StatusSwitchesValues != null) {
            return -com-huawei-keyguard-HwUnlockConstants$StatusSwitchesValues;
        }
        int[] iArr = new int[HwUnlockConstants$Status.values().length];
        try {
            iArr[HwUnlockConstants$Status.CardDeActived.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[HwUnlockConstants$Status.CardNotReady.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[HwUnlockConstants$Status.CorporateLocked.ordinal()] = 17;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[HwUnlockConstants$Status.CorporatePukLocked.ordinal()] = 18;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[HwUnlockConstants$Status.NetworkLocked.ordinal()] = 3;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[HwUnlockConstants$Status.NetworkPukLocked.ordinal()] = 19;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[HwUnlockConstants$Status.NetworkSubsetLocked.ordinal()] = 20;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[HwUnlockConstants$Status.NetworkSubsetPukLocked.ordinal()] = 21;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[HwUnlockConstants$Status.Normal.ordinal()] = 4;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[HwUnlockConstants$Status.RuimCorporateLocked.ordinal()] = 22;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[HwUnlockConstants$Status.RuimHrpdLocked.ordinal()] = 23;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[HwUnlockConstants$Status.RuimNetwork1Locked.ordinal()] = 24;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[HwUnlockConstants$Status.RuimNetwork2Locked.ordinal()] = 25;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[HwUnlockConstants$Status.RuimRuimLocked.ordinal()] = 26;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[HwUnlockConstants$Status.RuimServiceProviderLocked.ordinal()] = 27;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[HwUnlockConstants$Status.ServiceProviderLocked.ordinal()] = 28;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[HwUnlockConstants$Status.ServiceProviderPukLocked.ordinal()] = 29;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[HwUnlockConstants$Status.SimLocked.ordinal()] = 5;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[HwUnlockConstants$Status.SimMissing.ordinal()] = 6;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[HwUnlockConstants$Status.SimMissingLocked.ordinal()] = 7;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[HwUnlockConstants$Status.SimPukLocked.ordinal()] = 8;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[HwUnlockConstants$Status.SimSimLocked.ordinal()] = 30;
        } catch (NoSuchFieldError e22) {
        }
        -com-huawei-keyguard-HwUnlockConstants$StatusSwitchesValues = iArr;
        return iArr;
    }

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.config.hw_opta", 0) == 92 && SystemProperties.getInt("ro.config.hw_optb", 0) == 156) {
            z = false;
        }
        MSIM_REMOVE_ABSENT_CARRIER_INFO = z;
    }

    public LockScreenCallbackImpl(Context context, HwLockScreenPanel hwlockscreen) {
        this.mContext = context;
        this.mHwlockscreen = hwlockscreen;
        this.mUpdateMonitor = HwKeyguardUpdateMonitor.getInstance();
        init(context);
    }

    private Context getContext() {
        return this.mContext;
    }

    private void init(Context context) {
        HwLog.i("LockScreenCallbackImpl", "LockScreenCallbackImpl init");
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        initDoubleCard();
    }

    public void setLockScreen(HwUnlockInterface$HwLockScreenReal lockScreen) {
        this.mLockScreen = lockScreen;
    }

    public void onTrigger(Intent intent, Animation anim) {
        this.mHwlockscreen.onTrigger(intent, anim);
    }

    public boolean isScreenOn() {
        if (this.mPowerManager != null) {
            return this.mPowerManager.isScreenOn();
        }
        return false;
    }

    public boolean isShowOwnerInfo() {
        return this.mShowOwnerInfo;
    }

    public String getOwnerInfo() {
        return this.mOwnerInfo;
    }

    public void refreshLockScreenInfo() {
        if (this.mUpdateMonitor != null) {
            updateCarriersInfo();
        }
        updateOwnerInfo();
    }

    private void initDoubleCard() {
        this.mTeleManager = GlobalContext.getTelephonyManager(this.mContext);
        try {
            this.mSimTeleManager = GlobalContext.getMSimTelephonyManager(this.mContext);
            this.mIsMultiSimEnabled = this.mSimTeleManager.isMultiSimEnabled();
        } catch (Throwable th) {
            HwLog.i("LockScreenCallbackImpl", "initDoubleCard mSimTeleManager.isMultiSimEnabled not found");
            this.mIsMultiSimEnabled = false;
        }
        HwLog.i("LockScreenCallbackImpl", "initDoubleCard .. mIsMultiSimEnabled :" + this.mIsMultiSimEnabled);
        this.mDsdsMode = getDsdsMode();
        if (this.mIsMultiSimEnabled) {
            this.mStatus = new HwUnlockConstants$Status[2];
            for (int card = 0; card < 2; card++) {
                this.mStatus[card] = HwUnlockConstants$Status.SimMissing;
            }
            this.mCardsLayoutText = new IccText[2];
            this.mCarrierInfo = new CharSequence[2];
        } else {
            this.mStatus = new HwUnlockConstants$Status[1];
            this.mStatus[0] = HwUnlockConstants$Status.SimMissing;
            this.mCardsLayoutText = new IccText[1];
            this.mCarrierInfo = new CharSequence[1];
        }
        setupCardsText();
    }

    private int getDsdsMode() {
        if ("cdma_gsm".equals(DSDS_MODE_PROP)) {
            return 1;
        }
        if ("umts_gsm".equals(DSDS_MODE_PROP)) {
            return 2;
        }
        if ("tdscdma_gsm".equals(DSDS_MODE_PROP)) {
            return 3;
        }
        return 0;
    }

    private void setupCardsText() {
        getCardText();
        boolean isCdma;
        if (!this.mIsMultiSimEnabled) {
            if (this.mTeleManager.getCurrentPhoneType() == 2) {
                isCdma = true;
            } else {
                isCdma = false;
            }
            if (isCdma) {
                this.mCardsLayoutText[0] = this.mUimText;
            } else {
                this.mCardsLayoutText[0] = this.mSimText;
            }
        } else if (this.mDsdsMode == 2) {
            this.mCardsLayoutText[0] = this.mCard1Text;
            this.mCardsLayoutText[1] = this.mCard2Text;
        } else {
            for (int card = 0; card < 2; card++) {
                if (this.mSimTeleManager.getCurrentPhoneType(card) == 2) {
                    isCdma = true;
                } else {
                    isCdma = false;
                }
                if (isCdma) {
                    this.mCardsLayoutText[card] = this.mCDMAText;
                } else {
                    this.mCardsLayoutText[card] = this.mGSMText;
                }
            }
        }
    }

    private void getCardText() {
        if (this.mCDMAText == null) {
            this.mCDMAText = createCDMAText();
        }
        if (this.mGSMText == null) {
            this.mGSMText = createGSMText();
        }
        if (this.mSimText == null) {
            this.mSimText = createSimText();
        }
        if (this.mUimText == null) {
            this.mUimText = createUimText();
        }
        if (this.mCard1Text == null) {
            this.mCard1Text = createCard1Text();
        }
        if (this.mCard2Text == null) {
            this.mCard2Text = createCard2Text();
        }
    }

    private IccText createSimText() {
        IccText simText = new IccText();
        simText.iccPukLockedMessage = R$string.lockscreen_sim_puk_locked_message;
        simText.iccPinLockedMessage = R$string.lockscreen_sim_locked_message;
        simText.iccUnActivate = R$string.lockscreen_sim_unactivate_message;
        simText.iccNotReady = R$string.no_service_message;
        simText.iccMissingMessageShort = R$string.lockscreen_missing_sim_message_short;
        simText.networkLockedMessage = R$string.lockscreen_sim_network_locked_message;
        return simText;
    }

    private IccText createUimText() {
        IccText uimText = new IccText();
        uimText.iccPukLockedMessage = R$string.lockscreen_uim_puk_locked_message;
        uimText.iccPinLockedMessage = R$string.lockscreen_uim_locked_message;
        uimText.iccUnActivate = R$string.lockscreen_uim_unactivate_message;
        uimText.iccNotReady = R$string.no_service_message;
        uimText.iccMissingMessageShort = R$string.lockscreen_missing_uim_message_short;
        uimText.networkLockedMessage = R$string.lockscreen_uim_network_locked_message;
        return uimText;
    }

    private IccText createCDMAText() {
        IccText cdmaText = new IccText();
        cdmaText.iccPukLockedMessage = R$string.lockscreen_cdma_puk_locked_message;
        cdmaText.iccPinLockedMessage = R$string.lockscreen_cdma_locked_message;
        cdmaText.iccUnActivate = R$string.lockscreen_cdma_unactivate_message;
        cdmaText.iccNotReady = R$string.no_service_message;
        cdmaText.iccMissingMessageShort = R$string.lockscreen_missing_cdma_message_short;
        cdmaText.networkLockedMessage = R$string.lockscreen_network_locked_message;
        return cdmaText;
    }

    private IccText createGSMText() {
        IccText gsmText = new IccText();
        gsmText.iccPukLockedMessage = R$string.lockscreen_gsm_puk_locked_message;
        gsmText.iccPinLockedMessage = R$string.lockscreen_gsm_locked_message;
        gsmText.iccUnActivate = R$string.lockscreen_gsm_unactivate_message;
        gsmText.iccNotReady = R$string.no_service_message;
        gsmText.iccMissingMessageShort = R$string.lockscreen_missing_gsm_message_short;
        gsmText.networkLockedMessage = R$string.lockscreen_network_locked_message;
        return gsmText;
    }

    private IccText createCard1Text() {
        IccText card1Text = new IccText();
        card1Text.iccPukLockedMessage = R$string.lockscreen_card1_puk_locked_message;
        card1Text.iccPinLockedMessage = R$string.lockscreen_card1_locked_message;
        card1Text.iccUnActivate = R$string.lockscreen_card1_unactivate_message;
        card1Text.iccNotReady = R$string.no_service_message;
        card1Text.iccMissingMessageShort = R$string.lockscreen_missing_card1_message_short;
        card1Text.networkLockedMessage = R$string.lockscreen_network_locked_message;
        return card1Text;
    }

    private IccText createCard2Text() {
        IccText card2Text = new IccText();
        card2Text.iccPukLockedMessage = R$string.lockscreen_card2_puk_locked_message;
        card2Text.iccPinLockedMessage = R$string.lockscreen_card2_locked_message;
        card2Text.iccUnActivate = R$string.lockscreen_card2_unactivate_message;
        card2Text.iccNotReady = R$string.no_service_message;
        card2Text.iccMissingMessageShort = R$string.lockscreen_missing_card2_message_short;
        card2Text.networkLockedMessage = R$string.lockscreen_network_locked_message;
        return card2Text;
    }

    public void updateCarriersInfo() {
        int card;
        if (this.mIsMultiSimEnabled) {
            for (card = 0; card < 2; card++) {
                traverseCardStatus(card);
                updateCarrierTexts(this.mStatus[card], card);
                removeMSimAbsentCarrierInfo();
            }
            return;
        }
        for (card = 0; card < 1; card++) {
            traverseCardStatus(card);
            updateCarrierTexts(this.mStatus[card], card);
        }
    }

    public void updateCarrierTexts(HwUnlockConstants$Status status, int card) {
        HwLog.d("LockScreenCallbackImpl", "updateCarrierTexts status :" + status.name());
        String mEmergencyMessage = BuildConfig.FLAVOR;
        String emergencyDisplay = BuildConfig.FLAVOR;
        if (this.isDisplayEmergencyMessage) {
            mEmergencyMessage = Resources.getSystem().getText(17040036).toString();
        }
        if (mEmergencyMessage.length() > 0) {
            emergencyDisplay = " | " + mEmergencyMessage;
        }
        int sub;
        switch (-getcom-huawei-keyguard-HwUnlockConstants$StatusSwitchesValues()[status.ordinal()]) {
            case 1:
                this.mCarrierInfo[card] = getContext().getText(this.mCardsLayoutText[card].iccUnActivate);
                return;
            case 2:
                this.mCarrierInfo[card] = getContext().getText(this.mCardsLayoutText[card].iccNotReady) + emergencyDisplay;
                return;
            case 3:
                if (this.mIsMultiSimEnabled) {
                    try {
                        this.mCarrierInfo[card] = getCarrierString(this.mUpdateMonitor.getTelephonyPlmn(card), getContext().getText(this.mCardsLayoutText[card].networkLockedMessage));
                        return;
                    } catch (Exception e) {
                        HwLog.i("LockScreenCallbackImpl", "updateCarrierTexts getTelephonyPlmn(card) not support");
                        return;
                    }
                }
                this.mCarrierInfo[card] = getCarrierString(this.mUpdateMonitor.getTelephonyPlmn(), getContext().getText(this.mCardsLayoutText[card].networkLockedMessage));
                return;
            case 4:
                if (!this.mIsMultiSimEnabled) {
                    this.mCarrierInfo[card] = getCarrierString(this.mUpdateMonitor.getTelephonyPlmn(), this.mUpdateMonitor.getTelephonySpn());
                } else if (HuaweiTelephonyManager.getDefault().getSubidFromSlotId(card) == -1) {
                    this.mCarrierInfo[card] = getContext().getText(R$string.no_service_message) + emergencyDisplay;
                    return;
                } else {
                    try {
                        this.mCarrierInfo[card] = getCarrierString(this.mUpdateMonitor.getTelephonyPlmn(card), this.mUpdateMonitor.getTelephonySpn(card));
                    } catch (Exception e2) {
                        HwLog.d("LockScreenCallbackImpl", "updateCarrierTexts getTelephonyPlmn(card) not support");
                    }
                }
                if (this.mCarrierInfo[card] == null || this.mCarrierInfo[card].equals(BuildConfig.FLAVOR)) {
                    this.mCarrierInfo[card] = getContext().getText(R$string.no_service_message) + emergencyDisplay;
                    return;
                }
                return;
            case 5:
                if (this.isDisplayEmergencyMessage) {
                    try {
                        if (this.mIsMultiSimEnabled) {
                            sub = HuaweiTelephonyManager.getDefault().getSubidFromSlotId(card);
                            HwLog.d("LockScreenCallbackImpl", "updateCarrierTexts SimLocked: sub=" + sub);
                            this.mCarrierInfo[card] = null;
                            if (sub != -1) {
                                this.mCarrierInfo[card] = getCarrierString(this.mUpdateMonitor.getTelephonyPlmn(card), this.mUpdateMonitor.getTelephonySpn(card));
                            }
                        } else {
                            this.mCarrierInfo[card] = getCarrierString(this.mUpdateMonitor.getTelephonyPlmn(), this.mUpdateMonitor.getTelephonySpn());
                        }
                    } catch (Exception e3) {
                        this.mCarrierInfo[card] = null;
                    }
                    if (this.mCarrierInfo[card] == null || this.mCarrierInfo[card].equals(BuildConfig.FLAVOR)) {
                        this.mCarrierInfo[card] = getContext().getText(this.mCardsLayoutText[card].iccPinLockedMessage) + " | " + getContext().getText(R$string.no_service_message).toString();
                    } else {
                        this.mCarrierInfo[card] = getContext().getText(this.mCardsLayoutText[card].iccPinLockedMessage) + " | " + this.mCarrierInfo[card].toString();
                    }
                    HwLog.d("LockScreenCallbackImpl", "updateCarrierTexts SimLocked end: card=" + card + "  mCarrierInfo[card]=" + this.mCarrierInfo[card]);
                    return;
                }
                this.mCarrierInfo[card] = getContext().getText(this.mCardsLayoutText[card].iccPinLockedMessage);
                return;
            case 6:
                if (this.isDisplayEmergencyMessage) {
                    try {
                        if (this.mIsMultiSimEnabled) {
                            sub = HuaweiTelephonyManager.getDefault().getSubidFromSlotId(card);
                            HwLog.d("LockScreenCallbackImpl", "updateCarrierTexts SimMissing: sub=" + sub);
                            this.mCarrierInfo[card] = null;
                            if (sub != -1) {
                                this.mCarrierInfo[card] = getCarrierString(this.mUpdateMonitor.getTelephonyPlmn(card), this.mUpdateMonitor.getTelephonySpn(card));
                            }
                        } else {
                            this.mCarrierInfo[card] = getCarrierString(this.mUpdateMonitor.getTelephonyPlmn(), this.mUpdateMonitor.getTelephonySpn());
                        }
                    } catch (Exception e4) {
                        this.mCarrierInfo[card] = null;
                    }
                    HwLog.d("LockScreenCallbackImpl", "updateCarrierTexts SimMissing begin: card=" + card + "  mCarrierInfo[card]=" + this.mCarrierInfo[card]);
                    if (this.mCarrierInfo[card] == null || this.mCarrierInfo[card].equals(BuildConfig.FLAVOR)) {
                        this.mCarrierInfo[card] = getContext().getText(this.mCardsLayoutText[card].iccMissingMessageShort) + " | " + getContext().getText(R$string.no_service_message).toString();
                    } else {
                        this.mCarrierInfo[card] = getContext().getText(this.mCardsLayoutText[card].iccMissingMessageShort) + " | " + this.mCarrierInfo[card].toString();
                    }
                    HwLog.d("LockScreenCallbackImpl", "updateCarrierTexts SimMissing end: card=" + card + "  mCarrierInfo[card]=" + this.mCarrierInfo[card]);
                    return;
                }
                this.mCarrierInfo[card] = getContext().getText(this.mCardsLayoutText[card].iccMissingMessageShort);
                return;
            case 7:
                if (this.mIsMultiSimEnabled) {
                    try {
                        this.mCarrierInfo[card] = getCarrierString(this.mUpdateMonitor.getTelephonyPlmn(card), getContext().getText(this.mCardsLayoutText[card].iccMissingMessageShort) + emergencyDisplay);
                    } catch (Exception e5) {
                        HwLog.i("LockScreenCallbackImpl", "updateCarrierTexts getTelephonyPlmn(card) not support");
                    }
                } else {
                    this.mCarrierInfo[card] = getCarrierString(this.mUpdateMonitor.getTelephonyPlmn(card), getContext().getText(this.mCardsLayoutText[card].iccMissingMessageShort)) + emergencyDisplay;
                }
                if (this.mIsMultiSimEnabled) {
                    if (this.mStatus[0] == HwUnlockConstants$Status.SimMissingLocked || this.mStatus[1] == HwUnlockConstants$Status.SimMissingLocked) {
                        this.mShowUnlock = false;
                        return;
                    }
                    return;
                } else if (this.mStatus[0] == HwUnlockConstants$Status.SimMissingLocked) {
                    this.mShowUnlock = false;
                    return;
                } else {
                    return;
                }
            case 8:
                if (this.isDisplayEmergencyMessage) {
                    try {
                        if (this.mIsMultiSimEnabled) {
                            sub = HuaweiTelephonyManager.getDefault().getSubidFromSlotId(card);
                            HwLog.d("LockScreenCallbackImpl", "updateCarrierTexts SimPukLocked: sub=" + sub);
                            this.mCarrierInfo[card] = null;
                            if (sub != -1) {
                                this.mCarrierInfo[card] = getCarrierString(this.mUpdateMonitor.getTelephonyPlmn(card), this.mUpdateMonitor.getTelephonySpn(card));
                            }
                        } else {
                            this.mCarrierInfo[card] = getCarrierString(this.mUpdateMonitor.getTelephonyPlmn(), this.mUpdateMonitor.getTelephonySpn());
                        }
                    } catch (Exception e6) {
                        this.mCarrierInfo[card] = null;
                    }
                    if (this.mCarrierInfo[card] == null || this.mCarrierInfo[card].equals(BuildConfig.FLAVOR)) {
                        this.mCarrierInfo[card] = getContext().getText(this.mCardsLayoutText[card].iccPukLockedMessage) + " | " + getContext().getText(R$string.no_service_message).toString();
                    } else {
                        this.mCarrierInfo[card] = getContext().getText(this.mCardsLayoutText[card].iccPukLockedMessage) + " | " + this.mCarrierInfo[card].toString();
                    }
                    HwLog.d("LockScreenCallbackImpl", "updateCarrierTexts SimPukLocked end: card=" + card + "  mCarrierInfo[card]=" + this.mCarrierInfo[card]);
                    return;
                }
                this.mCarrierInfo[card] = getContext().getText(this.mCardsLayoutText[card].iccPukLockedMessage);
                return;
            default:
                this.mCarrierInfo[card] = getContext().getText(this.mCardsLayoutText[card].iccMissingMessageShort) + emergencyDisplay;
                return;
        }
    }

    private void traverseCardStatus(int card) {
        IccCardConstants.State iccState = IccCardConstants.State.ABSENT;
        try {
            iccState = this.mUpdateMonitor.getSimState(card);
        } catch (Exception e) {
            HwLog.i("LockScreenCallbackImpl", "traverseCardStatus getSimState(card) not support");
        }
        this.mStatus[card] = getCurrentStatus(iccState);
    }

    private void updateOwnerInfo() {
        if (this.mLockPatternUtils != null) {
            this.mShowOwnerInfo = this.mLockPatternUtils.isOwnerInfoEnabled(KeyguardUpdateMonitor.getCurrentUser());
            this.mOwnerInfo = this.mLockPatternUtils.getOwnerInfo(KeyguardUpdateMonitor.getCurrentUser());
        } else {
            HwLog.w("LockScreenCallbackImpl", "updateOwnerInfo, mLockPatternUtils is null");
        }
        if (RemoteLockUtils.isDeviceRemoteLocked(getContext())) {
            String remoteInfo = RemoteLockUtils.getDeviceRemoteLockedInfo(getContext());
            HwLog.i("LockScreenCallbackImpl", "updateOwnerInfo remote lock and message is empty?:" + TextUtils.isEmpty(remoteInfo));
            if (!TextUtils.isEmpty(remoteInfo)) {
                this.mOwnerInfo = remoteInfo;
                this.mShowOwnerInfo = true;
            }
        }
    }

    private void refreshBatteryStringAndIcon() {
        if (this.mChargeIcon == null) {
            this.mChargeIcon = getContext().getResources().getDrawable(17301534);
        }
        if (this.mPlugIn) {
            if ("text".equalsIgnoreCase(HwUnlockUtils.getChargingType())) {
                if (this.mIsCharged) {
                    this.mChargingInfo = getContext().getString(R$string.lockscreen_charged);
                    return;
                }
                NumberFormat pnf = NumberFormat.getPercentInstance();
                this.mChargingInfo = getContext().getString(BatteryStateInfo.getInst().chargeLevelResID(), new Object[]{pnf.format(((double) this.mChargeLevel) / 100.0d)});
            } else if ("number".equalsIgnoreCase(HwUnlockUtils.getChargingType())) {
                this.mChargingInfo = String.valueOf(this.mChargeLevel);
            } else {
                this.mChargingInfo = getContext().getString(R$string.charge_percent, new Object[]{Integer.valueOf(this.mChargeLevel)});
            }
        } else if (this.mIsLow) {
            this.mChargingInfo = getContext().getString(R$string.emui40_lockscreen_low_battery, new Object[]{NumberFormat.getPercentInstance().format(((double) this.mChargeLevel) / 100.0d)});
        }
    }

    public void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
        this.mLockPatternUtils = lockPatternUtils;
    }

    private HwUnlockConstants$Status getCurrentStatus(IccCardConstants.State simState) {
        boolean missingAndNotProvisioned = true;
        if (this.mUpdateMonitor == null) {
            HwLog.w("LockScreenCallbackImpl", "getCurrentStatus while mUpdateMonitor is null");
            return HwUnlockConstants$Status.SimMissing;
        }
        if (this.mUpdateMonitor.isDeviceProvisioned()) {
            missingAndNotProvisioned = false;
        } else if (!(simState == IccCardConstants.State.ABSENT || simState == IccCardConstants.State.PERM_DISABLED)) {
            missingAndNotProvisioned = false;
        }
        if (missingAndNotProvisioned) {
            return HwUnlockConstants$Status.SimMissingLocked;
        }
        try {
            switch (-getcom-huawei-internal-telephony-IccCardEx$StateSwitchesValues()[IccCardEx.getIccCardExState(simState).ordinal()]) {
                case 1:
                    return HwUnlockConstants$Status.SimMissing;
                case 2:
                    return HwUnlockConstants$Status.SimMissingLocked;
                case 3:
                    return HwUnlockConstants$Status.Normal;
                case 4:
                    return HwUnlockConstants$Status.NetworkLocked;
                case 5:
                    return HwUnlockConstants$Status.SimLocked;
                case 6:
                    return HwUnlockConstants$Status.SimPukLocked;
                case 7:
                    return HwUnlockConstants$Status.Normal;
                case 8:
                    return HwUnlockConstants$Status.SimMissing;
            }
        } catch (Exception e) {
            HwLog.e("LockScreenCallbackImpl", "getIccCardExState get error");
        }
        return HwUnlockConstants$Status.SimMissing;
    }

    public CharSequence getCarrierString(CharSequence telephonyPlmn, CharSequence telephonySpn) {
        HwLog.d("LockScreenCallbackImpl", "Telephony Plmn :" + telephonyPlmn + "Spn :" + telephonySpn);
        if (telephonyPlmn != null && telephonySpn == null) {
            return telephonyPlmn;
        }
        if (telephonyPlmn == null || telephonySpn == null) {
            if (telephonyPlmn != null || telephonySpn == null) {
                return BuildConfig.FLAVOR;
            }
            return telephonySpn;
        } else if (telephonyPlmn.length() == 0 || telephonySpn.length() == 0) {
            return telephonyPlmn + BuildConfig.FLAVOR + telephonySpn;
        } else {
            if ("true".equals(SystemProperties.get("ro.config.replace_operator_name", "false"))) {
                return telephonySpn + " | " + telephonyPlmn;
            }
            return telephonyPlmn + " | " + telephonySpn;
        }
    }

    public void onRefreshBatteryInfo(BatteryStatus status) {
        this.mShowBatteryInfo = !status.isPluggedIn() ? status.isBatteryLow() : true;
        this.mPlugIn = status.isPluggedIn();
        this.mChargeLevel = status.level;
        this.mIsCharged = status.isCharged();
        this.mIsLow = status.isBatteryLow();
        refreshBatteryStringAndIcon();
        if (this.mLockScreen != null && isScreenOn()) {
            this.mLockScreen.onBatteryInfoChanged();
        }
    }

    private void removeMSimAbsentCarrierInfo() {
        if (!this.mIsMultiSimEnabled || !MSIM_REMOVE_ABSENT_CARRIER_INFO || this.mStatus == null || this.mStatus.length != 2 || this.mCarrierInfo == null || this.mCarrierInfo.length != 2) {
            return;
        }
        if (isSimMissing(this.mStatus[0]) && !isSimMissing(this.mStatus[1])) {
            this.mCarrierInfo[0] = BuildConfig.FLAVOR;
        } else if (!isSimMissing(this.mStatus[0]) && isSimMissing(this.mStatus[1])) {
            this.mCarrierInfo[1] = BuildConfig.FLAVOR;
        }
    }

    private boolean isSimMissing(HwUnlockConstants$Status simStatus) {
        if (simStatus == null) {
            return false;
        }
        if (simStatus == HwUnlockConstants$Status.SimMissing || simStatus == HwUnlockConstants$Status.SimMissingLocked) {
            return true;
        }
        return false;
    }

    public void onTimeChanged() {
        if (this.mLockScreen != null && isScreenOn()) {
            this.mLockScreen.onTimeChanged();
        }
    }

    public void onPhoneStateChanged(int state) {
        this.mPhoneState = state;
        if (this.mLockScreen != null) {
            this.mLockScreen.onPhoneStateChanged();
        }
    }

    public void setClickKey(int value) {
        this.mHwlockscreen.setClickKey(value);
    }
}
