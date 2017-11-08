package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Message;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimController.EnableParam;
import com.android.internal.telephony.vsim.HwVSimController.ProcessState;
import com.android.internal.telephony.vsim.HwVSimEventReport.VSimEventInfoUtils;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimModemAdapter.SimStateInfo;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import java.util.Arrays;

public abstract class HwVSimWorkProcessor extends HwVSimProcessor {
    public static final String LOG_TAG = "VSimWorkProcessor";
    protected static final int PHONE_COUNT = TelephonyManager.getDefault().getPhoneCount();
    protected boolean mInDSDSPreProcess = false;
    protected HwVSimController mVSimController;

    protected abstract void logd(String str);

    protected abstract void onCardPowerOffDone(Message message);

    protected abstract void onCardPowerOnDone(Message message);

    protected abstract void onRadioPowerOnDone(Message message);

    protected abstract void onSetActiveModemModeDone(Message message);

    protected abstract void onSetPreferredNetworkTypeDone(Message message);

    protected abstract void onSetTeeDataReadyDone(Message message);

    protected abstract void onSwitchSlotDone(Message message);

    public HwVSimWorkProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(modemAdapter, request);
        this.mVSimController = controller;
    }

    public void onEnter() {
        logd("onEnter");
        HwVSimRequest request = this.mRequest;
        if (request != null) {
            this.mModemAdapter.handleSubSwapProcess(this, request);
            if (this.mVSimController.isEnableProcess() && !this.mVSimController.isDirectProcess()) {
                this.mModemAdapter.setHwVSimPowerOn(this, request);
            }
            if (HwVSimUtilsInner.isPlatformRealTripple() && HwVSimUtilsInner.IS_DSDSPOWER_SUPPORT && isSwapProcess()) {
                this.mInDSDSPreProcess = true;
                int slaveSlot = request.getMainSlot() == 0 ? 1 : 0;
                this.mVSimController.setProhibitSubUpdateSimNoChange(slaveSlot, true);
                this.mModemAdapter.radioPowerOff(this, request, slaveSlot);
            } else {
                this.mModemAdapter.radioPowerOff(this, request);
            }
            setProcessState(ProcessState.PROCESS_STATE_WORK);
        }
    }

    public void onExit() {
        logd("onExit");
    }

    public void transitionToState(int state) {
        this.mVSimController.transitionToState(state);
    }

    public Message obtainMessage(int what, Object obj) {
        return this.mVSimController.obtainMessage(what, obj);
    }

    protected void onRadioPowerOffDone(Message msg) {
        logd("onRadioPowerOffDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 2);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            if (this.mInDSDSPreProcess) {
                this.mInDSDSPreProcess = false;
                HwVSimRequest request = this.mRequest;
                if (request != null) {
                    this.mModemAdapter.radioPowerOff(this, request);
                } else {
                    return;
                }
            }
            this.mModemAdapter.onRadioPowerOffDone(this, ar);
        }
    }

    protected void onGetSimStateDone(Message msg) {
        logd("onGetSimStateDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 3);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            SimStateInfo ssInfo = this.mModemAdapter.onGetSimStateDone(this, ar);
            if (ssInfo != null) {
                logd("onGetSimStateDone ssInfo index = " + ssInfo.simIndex);
                logd("onGetSimStateDone ssInfo simEnable = " + ssInfo.simEnable);
                logd("onGetSimStateDone ssInfo simSub = " + ssInfo.simSub);
                logd("onGetSimStateDone ssInfo simNetInfo = " + ssInfo.simNetInfo);
            }
            HwVSimRequest request = ar.userObj;
            int subCount = request.getSubCount();
            int subId = request.mSubId;
            for (int i = 0; i < subCount; i++) {
                if (subId == request.getSubIdByIndex(i)) {
                    request.setSimStateMark(i, false);
                }
            }
            if (ssInfo != null) {
                this.mModemAdapter.cardPowerOff(this, request, subId, ssInfo.simIndex);
            }
        }
    }

    protected void onGetPreferredNetworkTypeDone(Message msg) {
        logd("onGetPreferredNetworkTypeDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 9);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar)) {
            this.mModemAdapter.onGetPreferredNetworkTypeDone(this, ar);
            HwVSimRequest request = ar.userObj;
            int subId = request.mSubId;
            EnableParam param = getEnableParam(request);
            if (param == null) {
                doEnableProcessException(ar, request, Integer.valueOf(3));
                return;
            }
            int networkMode = acqorderToNetworkMode(param.acqorder);
            logd("set preferred network to " + networkMode);
            this.mModemAdapter.setPreferredNetworkType(this, request, subId, networkMode);
        }
    }

    protected int acqorderToNetworkMode(String acqorder) {
        boolean isVSimOn = false;
        HwVSimRequest request = this.mRequest;
        if (request == null) {
            return 3;
        }
        int[] cardTypes = request.getCardTypes();
        if (cardTypes == null || cardTypes.length == 0) {
            return 3;
        }
        boolean isULOnly;
        if (isDirectProcess()) {
            isVSimOn = true;
        }
        if (isVSimOn) {
            isULOnly = getULOnlyProp();
        } else {
            isULOnly = checkHasIccCardOnM2(cardTypes);
            setULOnlyProp(Boolean.valueOf(isULOnly));
        }
        return calcNetworkModeByAcqorder(acqorder, isULOnly);
    }

    private boolean checkHasIccCardOnM2(int[] cardTypes) {
        if (HwVSimUtilsInner.isVSimDsdsVersionOne()) {
            logd("checkHasIccCardOnM2: isVSimDsdsVersionOne , return false ");
            return false;
        }
        logd("checkHasIccCardOnM2 cardTypes = " + Arrays.toString(cardTypes));
        boolean[] isCardPresent = HwVSimUtilsInner.getCardState(cardTypes);
        int insertedCardCount = HwVSimUtilsInner.getInsertedCardCount(cardTypes);
        boolean hasIccCardOnM2 = false;
        boolean ulOnlyMode = getVSimULOnlyMode();
        if (HwVSimUtilsInner.isChinaTelecom() && HwVSimUtilsInner.isPlatformRealTripple()) {
            int userReservedSubId = getUserReservedSubId();
            int mainSlot = HwVSimPhoneFactory.getVSimSavedMainSlot();
            if (mainSlot == -1) {
                mainSlot = 0;
            }
            int slaveSlot = mainSlot == 0 ? 1 : 0;
            if (insertedCardCount != 0) {
                if (isCardPresent[mainSlot] && isCardPresent[slaveSlot] && (ulOnlyMode || slaveSlot == userReservedSubId)) {
                    hasIccCardOnM2 = true;
                } else if (!isCardPresent[mainSlot] && isCardPresent[slaveSlot]) {
                    hasIccCardOnM2 = true;
                }
            }
        } else if (ulOnlyMode && insertedCardCount == PHONE_COUNT) {
            hasIccCardOnM2 = true;
        }
        return hasIccCardOnM2;
    }

    private int calcNetworkModeByAcqorder(String acqorder, boolean isULOnly) {
        if (isULOnly) {
            if ("0201".equals(acqorder) || "02".equals(acqorder)) {
                return 2;
            }
            return 12;
        } else if ("0201".equals(acqorder)) {
            return 3;
        } else {
            if ("01".equals(acqorder)) {
                return 1;
            }
            return 9;
        }
    }

    protected int modifyNetworkMode(int oldNetworkMode, boolean removeG) {
        int networkMode = oldNetworkMode;
        if (!removeG) {
            switch (oldNetworkMode) {
                case 2:
                    networkMode = 3;
                    break;
                case 12:
                    networkMode = 9;
                    break;
                default:
                    break;
            }
        }
        switch (oldNetworkMode) {
            case 3:
                networkMode = 2;
                break;
            case 9:
                networkMode = 12;
                break;
        }
        if (networkMode != oldNetworkMode) {
            setULOnlyProp(Boolean.valueOf(removeG));
        }
        return networkMode;
    }

    protected EnableParam getEnableParam(HwVSimRequest request) {
        if (this.mVSimController == null) {
            return null;
        }
        return this.mVSimController.getEnableParam(request);
    }

    protected void setULOnlyProp(Boolean isULOnly) {
        if (this.mVSimController != null) {
            this.mVSimController.setULOnlyProp(isULOnly);
        }
    }

    protected boolean getULOnlyProp() {
        if (this.mVSimController != null) {
            return this.mVSimController.getULOnlyProp();
        }
        return false;
    }

    protected boolean getVSimULOnlyMode() {
        if (this.mVSimController != null) {
            return this.mVSimController.getVSimULOnlyMode();
        }
        return false;
    }

    protected int getUserReservedSubId() {
        if (this.mVSimController != null) {
            return this.mVSimController.getUserReservedSubId();
        }
        return -1;
    }

    public boolean isDirectProcess() {
        if (this.mVSimController == null) {
            return false;
        }
        return this.mVSimController.isDirectProcess();
    }
}
