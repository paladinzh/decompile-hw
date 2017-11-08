package com.android.internal.telephony.gsm;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.res.Resources;
import android.media.ToneGenerator;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.SettingsEx.Systemex;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.view.KeyEvent;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.CsgSearch;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.uicc.IccRecords;

public class HwCustGsmServiceStateTrackerImpl extends HwCustGsmServiceStateTracker {
    private static final int DIALOG_TIMEOUT = 120000;
    protected static final int EVENT_POLL_STATE_REGISTRATION = 4;
    private static final int FOCUS_BEEP_VOLUME = 100;
    private static final boolean HW_ATT_SHOW_NET_REJ = SystemProperties.getBoolean("ro.config.hw_showNetworkReject", false);
    private static final boolean IS_DATAONLY_LOCATION_ENABLED = SystemProperties.getBoolean("ro.config.hw_gmap_enabled", false);
    private static final boolean IS_DELAY_UPDATENAME = SystemProperties.getBoolean("ro.config.delay_updatename", false);
    private static final boolean IS_DELAY_UPDATENAME_LAC_NULL = SystemProperties.getBoolean("ro.config.lac_null_delay_update", false);
    private static final boolean IS_EMERGENCY_SHOWS_NOSERVICE = SystemProperties.getBoolean("ro.config.LTE_NO_SERVICE", false);
    private static final boolean IS_SIM_POWER_DOWN = SystemProperties.getBoolean("ro.config.SimPowerOperation", false);
    private static final String LOG_TAG = "HwCustGsmServiceStateTrackerImpl";
    private static final int MSG_ID_TIMEOUT = 1;
    private static final boolean UPDATE_LAC_CID = SystemProperties.getBoolean("ro.config.hw_update_lac_cid", false);
    static final boolean VDBG = false;
    private static boolean mIsSupportCsgSearch = SystemProperties.getBoolean("ro.config.att.csg", false);
    private int dialogCanceled = 0;
    private boolean is_ext_plmn_sent = false;
    private CsgSearch mCsgSrch;
    OnCancelListener mShowRejMsgOnCancelListener = new OnCancelListener() {
        public void onCancel(DialogInterface dialog) {
            HwCustGsmServiceStateTrackerImpl.this.networkDialog = null;
            HwCustGsmServiceStateTrackerImpl.this.dialogCanceled = 0;
        }
    };
    OnKeyListener mShowRejMsgOnKeyListener = new OnKeyListener() {
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (82 == keyCode || dialog == null) {
                return false;
            }
            dialog.dismiss();
            HwCustGsmServiceStateTrackerImpl.this.networkDialog = null;
            HwCustGsmServiceStateTrackerImpl.this.mToneGenerator = null;
            HwCustGsmServiceStateTrackerImpl.this.dialogCanceled = 0;
            return true;
        }
    };
    private String mSimRecordVoicemail = "";
    Handler mTimeoutHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (HwCustGsmServiceStateTrackerImpl.this.dialogCanceled <= 0) {
                        if (HwCustGsmServiceStateTrackerImpl.this.networkDialog != null && HwCustGsmServiceStateTrackerImpl.this.dialogCanceled == 0) {
                            HwCustGsmServiceStateTrackerImpl.this.networkDialog.dismiss();
                            HwCustGsmServiceStateTrackerImpl.this.networkDialog = null;
                            HwCustGsmServiceStateTrackerImpl.this.mToneGenerator = null;
                            break;
                        }
                    }
                    HwCustGsmServiceStateTrackerImpl hwCustGsmServiceStateTrackerImpl = HwCustGsmServiceStateTrackerImpl.this;
                    hwCustGsmServiceStateTrackerImpl.dialogCanceled = hwCustGsmServiceStateTrackerImpl.dialogCanceled - 1;
                    return;
            }
        }
    };
    private ToneGenerator mToneGenerator = null;
    private AlertDialog networkDialog = null;
    private int oldRejCode = 0;

    public HwCustGsmServiceStateTrackerImpl(GsmCdmaPhone gsmPhone) {
        super(gsmPhone);
        if (CsgSearch.isSupportCsgSearch()) {
            this.mCsgSrch = new CsgSearch(gsmPhone);
        } else {
            this.mCsgSrch = null;
        }
    }

    public void updateRomingVoicemailNumber(ServiceState currentState) {
        Object custRoamingVoicemail = null;
        try {
            custRoamingVoicemail = Systemex.getString(this.mContext.getContentResolver(), "hw_cust_roamingvoicemail");
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "Exception get hw_cust_roamingvoicemail value", e);
        }
        if (currentState != null && !TextUtils.isEmpty(custRoamingVoicemail)) {
            IccRecords mIccRecord = (IccRecords) this.mGsmPhone.mIccRecords.get();
            if (mIccRecord != null) {
                String hplmn = mIccRecord.getOperatorNumeric();
                String rplmn = currentState.getOperatorNumeric();
                String mVoicemailNum = mIccRecord.getVoiceMailNumber();
                String[] plmns = custRoamingVoicemail.split(",");
                if (!TextUtils.isEmpty(hplmn) && !TextUtils.isEmpty(rplmn) && plmns.length == 3 && !TextUtils.isEmpty(plmns[2])) {
                    if (hplmn.equals(plmns[0]) && rplmn.equals(plmns[1])) {
                        if (!plmns[2].equals(mVoicemailNum)) {
                            this.mSimRecordVoicemail = mVoicemailNum;
                            mIccRecord.setVoiceMailNumber(plmns[2]);
                        }
                    } else if (!TextUtils.isEmpty(this.mSimRecordVoicemail) && plmns[2].equals(mVoicemailNum)) {
                        mIccRecord.setVoiceMailNumber(this.mSimRecordVoicemail);
                        this.mSimRecordVoicemail = "";
                    }
                }
            }
        }
    }

    public void setRadioPower(CommandsInterface ci, boolean enabled) {
        if (IS_SIM_POWER_DOWN && ci != null && this.mGsmPhone != null) {
            boolean bAirplaneMode = Global.getInt(this.mGsmPhone.getContext().getContentResolver(), "airplane_mode_on", 0) == 1;
            try {
                Rlog.d(LOG_TAG, "Set radio power: " + enabled + ", is airplane mode: " + bAirplaneMode);
                if (enabled) {
                    ci.setSimState(1, 1, null);
                } else if (bAirplaneMode) {
                    ci.setSimState(1, 0, null);
                }
            } catch (Exception e) {
                Rlog.e(LOG_TAG, "Exception in setRadioPower", e);
            }
        }
    }

    public String setEmergencyToNoService(ServiceState mSS, String plmn, boolean mEmergencyOnly) {
        if (IS_EMERGENCY_SHOWS_NOSERVICE && mSS.getRadioTechnology() == 14 && mEmergencyOnly) {
            return Resources.getSystem().getText(17040012).toString();
        }
        return plmn;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setPsCell(ServiceState mSS, GsmCellLocation mNewCellLoc, String[] states) {
        if (!(mSS == null || mNewCellLoc == null || states == null || !IS_DATAONLY_LOCATION_ENABLED)) {
            boolean mCsOutOfservice = mSS.getVoiceRegState() == 1;
            int Tac = -1;
            int Ci = -1;
            int Pci = -1;
            try {
                if (states.length >= 6) {
                    Ci = Integer.parseInt(states[5]);
                }
                if (states.length >= 7) {
                    Pci = Integer.parseInt(states[6]);
                }
                if (states.length >= 8) {
                    Tac = Integer.parseInt(states[7]);
                }
            } catch (NumberFormatException e) {
                Rlog.d(LOG_TAG, "error parsing GprsRegistrationState: ");
            }
            if (mCsOutOfservice && Tac >= 0 && Ci >= 0 && Pci >= 0) {
                Rlog.d(LOG_TAG, "data only card use ps cellid to location");
                mNewCellLoc.setLacAndCid(Tac, Ci);
                mNewCellLoc.setPsc(Pci);
            }
        }
    }

    public boolean isInServiceState(int combinedregstate) {
        return HW_ATT_SHOW_NET_REJ && this.is_ext_plmn_sent && combinedregstate == 0;
    }

    public boolean isInServiceState(ServiceState ss) {
        return isInServiceState(getCombinedRegState(ss));
    }

    public void setExtPlmnSent(boolean value) {
        if (HW_ATT_SHOW_NET_REJ) {
            this.is_ext_plmn_sent = value;
        }
    }

    public void custHandlePollStateResult(int what, AsyncResult ar, int[] pollingContext) {
        if (HW_ATT_SHOW_NET_REJ && ar.userObj != pollingContext && ar.exception == null && 4 == what) {
            try {
                String[] states = ar.result;
                if (states != null && states.length > 13) {
                    handleNetworkRejection(Integer.parseInt(states[13]));
                }
            } catch (RuntimeException e) {
            }
        }
    }

    public void handleNetworkRejection(int regState, String[] states) {
        if (HW_ATT_SHOW_NET_REJ && states == null) {
            Rlog.d(LOG_TAG, "States is null.");
            return;
        }
        if ((regState == 3 || regState == 13) && states.length >= 14) {
            try {
                handleNetworkRejection(Integer.parseInt(states[13]));
            } catch (NumberFormatException ex) {
                Rlog.e(LOG_TAG, "error parsing regCode: " + ex);
            }
        }
    }

    private void showDialog(String msg, int regCode) {
        if (regCode != this.oldRejCode) {
            if (this.networkDialog != null) {
                this.dialogCanceled++;
                this.networkDialog.dismiss();
            }
            this.networkDialog = new Builder(this.mGsmPhone.getContext()).setMessage(msg).setCancelable(true).create();
            this.networkDialog.getWindow().setType(2008);
            this.networkDialog.setOnKeyListener(this.mShowRejMsgOnKeyListener);
            this.networkDialog.setOnCancelListener(this.mShowRejMsgOnCancelListener);
            this.networkDialog.show();
            this.mTimeoutHandler.sendMessageDelayed(this.mTimeoutHandler.obtainMessage(1), 120000);
            try {
                this.mToneGenerator = new ToneGenerator(1, FOCUS_BEEP_VOLUME);
                this.mToneGenerator.startTone(28);
            } catch (RuntimeException e) {
                this.mToneGenerator = null;
            }
        }
    }

    private int getCombinedRegState(ServiceState ss) {
        if (ss == null) {
            return 1;
        }
        int regState = ss.getVoiceRegState();
        int dataRegState = ss.getDataRegState();
        if (regState == 1 && dataRegState == 0) {
            Rlog.e(LOG_TAG, "getCombinedRegState: return STATE_IN_SERVICE as Data is in service");
            regState = dataRegState;
        }
        return regState;
    }

    private void handleNetworkRejection(int rejCode) {
        Resources r = Resources.getSystem();
        String plmn = r.getText(17040036).toString();
        switch (rejCode) {
            case 2:
                showDialog(r.getString(33685925), rejCode);
                handleShowLimitedService(plmn);
                break;
            case 3:
                showDialog(r.getString(33685926), rejCode);
                handleShowLimitedService(plmn);
                break;
            case 6:
                showDialog(r.getString(33685927), rejCode);
                handleShowLimitedService(plmn);
                break;
            case 11:
            case 12:
            case 13:
            case 15:
            case 17:
                handleShowLimitedService(" ");
                break;
        }
        this.oldRejCode = rejCode;
    }

    private void handleShowLimitedService(String plmn) {
        Intent intent = new Intent("android.provider.Telephony.SPN_STRINGS_UPDATED");
        intent.putExtra("showSpn", false);
        intent.putExtra("spn", "");
        intent.putExtra("showPlmn", true);
        intent.putExtra("plmn", plmn);
        this.mGsmPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        this.is_ext_plmn_sent = true;
    }

    public void judgeToLaunchCsgPeriodicSearchTimer() {
        if (this.mCsgSrch != null && mIsSupportCsgSearch) {
            this.mCsgSrch.judgeToLaunchCsgPeriodicSearchTimer();
        }
    }

    public boolean isStopUpdateName(boolean SimCardLoaded) {
        Rlog.d(LOG_TAG, " isStopUpdateName: SimCardLoaded = " + SimCardLoaded + ", IS_DELAY_UPDATENAME = " + IS_DELAY_UPDATENAME);
        if (!IS_DELAY_UPDATENAME && !IS_DELAY_UPDATENAME_LAC_NULL) {
            return false;
        }
        if ((SimCardLoaded || !IS_DELAY_UPDATENAME) && (this.mGsmPhone == null || this.mGsmPhone.mSST == null || ((this.mGsmPhone.mSST.mCellLoc != null && ((GsmCellLocation) this.mGsmPhone.mSST.mCellLoc).getLac() != -1) || !IS_DELAY_UPDATENAME_LAC_NULL))) {
            return false;
        }
        return true;
    }

    public boolean isUpdateLacAndCidCust(ServiceStateTracker sst) {
        Rlog.d(LOG_TAG, "isUpdateLacAndCidCust: Update Lac and Cid when cid is 0, ServiceStateTracker = " + sst + ",UPDATE_LAC_CID = " + UPDATE_LAC_CID);
        return UPDATE_LAC_CID;
    }
}
