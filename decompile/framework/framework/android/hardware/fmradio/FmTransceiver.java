package android.hardware.fmradio;

import android.bluetooth.BluetoothAdapter;
import android.os.SystemProperties;
import android.util.Log;

public class FmTransceiver {
    public static int FMState = 0;
    public static final int FMState_Rx_Turned_On = 1;
    public static final int FMState_Srch_InProg = 3;
    public static final int FMState_Turned_Off = 0;
    public static final int FMState_Tx_Turned_On = 2;
    private static final String FM_CHIPTYPE = SystemProperties.get("ro.connectivity.chiptype");
    public static final int FM_CHSPACE_100_KHZ = 1;
    public static final int FM_CHSPACE_200_KHZ = 0;
    public static final int FM_CHSPACE_50_KHZ = 2;
    public static final int FM_DE_EMP50 = 1;
    public static final int FM_DE_EMP75 = 0;
    public static final int FM_ENABLE_RETRY_TIMES = 100;
    public static final int FM_EU_BAND = 1;
    public static final int FM_JAPAN_STANDARD_BAND = 3;
    public static final int FM_JAPAN_WIDE_BAND = 2;
    public static final int FM_RDS_STD_NONE = 2;
    public static final int FM_RDS_STD_RBDS = 0;
    public static final int FM_RDS_STD_RDS = 1;
    protected static final int FM_RX = 1;
    protected static final int FM_TX = 2;
    public static final int FM_USER_DEFINED_BAND = 4;
    public static final int FM_US_BAND = 0;
    protected static int sFd = 0;
    public static final int subPwrLevel_FMRx_Starting = 4;
    public static final int subPwrLevel_FMTurning_Off = 6;
    public static final int subPwrLevel_FMTx_Starting = 5;
    public static final int subSrchLevel_ScanInProg = 1;
    public static final int subSrchLevel_SeekInPrg = 0;
    public static final int subSrchLevel_SrchAbort = 4;
    public static final int subSrchLevel_SrchComplete = 3;
    public static final int subSrchLevel_SrchListInProg = 2;
    private final int MUTE_EVENT = 4;
    private final int RDS_EVENT = 8;
    private final int READY_EVENT = 1;
    private final int SEEK_COMPLETE_EVENT = 3;
    private final String TAG = "FmTransceiver";
    private final int TUNE_EVENT = 2;
    protected FmRxControls mControl;
    protected int mPowerMode;
    protected FmRxRdsData mRdsData;
    protected FmRxEventListner mRxEvents;

    protected boolean acquire(String device) {
        boolean bStatus;
        int retry = 0;
        if (sFd <= 0) {
            if (!("hi110x".equals(FM_CHIPTYPE) || "hisi".equals(FM_CHIPTYPE) || "Qualcomm".equals(FM_CHIPTYPE))) {
                BluetoothAdapter btAdap = BluetoothAdapter.getDefaultAdapter();
                if (btAdap.enableRadio()) {
                    synchronized (this) {
                        Log.d("FmTransceiver", "wait Radio on");
                        while (!btAdap.isRadioEnabled() && retry < 100) {
                            try {
                                Log.d("FmTransceiver", "wait 100ms for radio on , retry =" + retry);
                                retry++;
                                wait(100);
                            } catch (InterruptedException e) {
                                Log.d("FmTransceiver", "Interrupted when waiting for radio on");
                            }
                        }
                    }
                } else {
                    Log.d("FmTransceiver", "fm enableRadio failed");
                    return false;
                }
            }
            Log.d("FmTransceiver", "Radio on");
            sFd = FmReceiverJNI.acquireFdNative("/dev/radio0");
            if (sFd > 0) {
                Log.d("FmTransceiver", "Opened " + sFd);
                bStatus = true;
            } else {
                Log.d("FmTransceiver", "Fail to Open " + sFd);
                bStatus = false;
            }
        } else {
            Log.d("FmTransceiver", "Alredy Opened " + sFd);
            bStatus = true;
        }
        return bStatus;
    }

    static boolean release(String device) {
        if (sFd != 0) {
            FmReceiverJNI.closeFdNative(sFd);
            sFd = 0;
            Log.d("FmTransceiver", "Turned off: " + sFd);
            if (!("hi110x".equals(FM_CHIPTYPE) || "hisi".equals(FM_CHIPTYPE) || "Qualcomm".equals(FM_CHIPTYPE))) {
                BluetoothAdapter.getDefaultAdapter().disableRadio();
            }
            Log.d("FmTransceiver", "Radio off");
        } else {
            Log.d("FmTransceiver", "Error turning off");
        }
        return true;
    }

    public boolean registerClient(FmRxEvCallbacks callback) {
        if (callback != null) {
            this.mRxEvents.startListner(sFd, callback);
            return true;
        }
        Log.d("FmTransceiver", "Null, do nothing");
        return false;
    }

    public boolean unregisterClient() {
        this.mRxEvents.stopListener();
        return true;
    }

    public boolean registerTransmitClient(FmRxEvCallbacks callback) {
        if (callback != null) {
            return true;
        }
        Log.d("FmTransceiver", "Null, do nothing");
        return false;
    }

    public boolean unregisterTransmitClient() {
        return true;
    }

    public boolean enable(FmConfig configSettings, int device) {
        if (!acquire("/dev/radio0")) {
            return false;
        }
        Log.d("FmTransceiver", "turning on %d" + device);
        this.mControl.fmOn(sFd, device);
        Log.d("FmTransceiver", "Calling fmConfigure");
        boolean status = FmConfig.fmConfigure(sFd, configSettings);
        if (!status) {
            Log.d("FmTransceiver", "fmConfigure failed");
            FmReceiverJNI.closeFdNative(sFd);
            sFd = 0;
        }
        return status;
    }

    public boolean disable() {
        this.mControl.fmOff(sFd);
        release("/dev/radio0");
        return true;
    }

    public boolean configure(FmConfig configSettings) {
        int lowerFreq = configSettings.getLowerLimit();
        Log.d("FmTransceiver", "fmConfigure");
        boolean status = FmConfig.fmConfigure(sFd, configSettings);
        return setStation(lowerFreq);
    }

    public boolean setStation(int frequencyKHz) {
        this.mControl.setFreq(frequencyKHz);
        if (this.mControl.setStation(sFd) < 0) {
            return false;
        }
        return true;
    }

    public void setNotchFilter(boolean value) {
        FmReceiverJNI.setNotchFilterNative(value);
    }

    static void setFMPowerState(int state) {
        FMState = state;
    }

    public static int getFMPowerState() {
        return FMState;
    }
}
