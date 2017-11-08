package android.hardware.fmradio;

import android.util.Log;
import java.util.Arrays;

class FmRxEventListner {
    private static final String TAG = "FMRadio";
    private final int EVENT_LISTEN = 1;
    private final int STD_BUF_SIZE = 128;
    private Thread mThread;

    private enum FmRxEvents {
        READY_EVENT,
        TUNE_EVENT,
        SEEK_COMPLETE_EVENT,
        SCAN_NEXT_EVENT,
        RAW_RDS_EVENT,
        RT_EVENT,
        PS_EVENT,
        ERROR_EVENT,
        BELOW_TH_EVENT,
        ABOVE_TH_EVENT,
        STEREO_EVENT,
        MONO_EVENT,
        RDS_AVAL_EVENT,
        RDS_NOT_AVAL_EVENT,
        TAVARUA_EVT_NEW_SRCH_LIST,
        TAVARUA_EVT_NEW_AF_LIST,
        SIGNAL_UPDATE_EVENT
    }

    FmRxEventListner() {
    }

    public void startListner(final int fd, final FmRxEvCallbacks cb) {
        this.mThread = new Thread() {
            public void run() {
                byte[] buff = new byte[128];
                Log.d(FmRxEventListner.TAG, "Starting listener " + fd);
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Arrays.fill(buff, (byte) 0);
                        int eventCount = FmReceiverJNI.getBufferNative(fd, buff, 1);
                        Log.d(FmRxEventListner.TAG, "Received event. Count: " + eventCount);
                        for (int index = 0; index < eventCount; index++) {
                            Log.d(FmRxEventListner.TAG, "Received <" + buff[index] + ">");
                            switch (buff[index]) {
                                case (byte) 0:
                                    Log.d(FmRxEventListner.TAG, "Got READY_EVENT");
                                    cb.FmRxEvEnableReceiver();
                                    break;
                                case (byte) 1:
                                    Log.d(FmRxEventListner.TAG, "Got TUNE_EVENT");
                                    cb.FmRxEvRadioTuneStatus(FmReceiverJNI.getFreqNative(fd));
                                    break;
                                case (byte) 2:
                                    Log.d(FmRxEventListner.TAG, "Got SEEK_COMPLETE_EVENT");
                                    cb.FmRxEvSearchComplete(FmReceiverJNI.getFreqNative(fd));
                                    break;
                                case (byte) 3:
                                    Log.d(FmRxEventListner.TAG, "Got SCAN_NEXT_EVENT");
                                    cb.FmRxEvSearchInProgress();
                                    break;
                                case (byte) 4:
                                    Log.d(FmRxEventListner.TAG, "Got RAW_RDS_EVENT");
                                    cb.FmRxEvRdsGroupData();
                                    break;
                                case (byte) 5:
                                    Log.d(FmRxEventListner.TAG, "Got RT_EVENT");
                                    cb.FmRxEvRdsRtInfo();
                                    break;
                                case (byte) 6:
                                    Log.d(FmRxEventListner.TAG, "Got PS_EVENT");
                                    cb.FmRxEvRdsPsInfo();
                                    break;
                                case (byte) 7:
                                    Log.d(FmRxEventListner.TAG, "Got ERROR_EVENT");
                                    break;
                                case (byte) 8:
                                    Log.d(FmRxEventListner.TAG, "Got BELOW_TH_EVENT");
                                    cb.FmRxEvServiceAvailable(false);
                                    break;
                                case (byte) 9:
                                    Log.d(FmRxEventListner.TAG, "Got ABOVE_TH_EVENT");
                                    cb.FmRxEvServiceAvailable(true);
                                    break;
                                case (byte) 10:
                                    Log.d(FmRxEventListner.TAG, "Got STEREO_EVENT");
                                    cb.FmRxEvStereoStatus(true);
                                    break;
                                case (byte) 11:
                                    Log.d(FmRxEventListner.TAG, "Got MONO_EVENT");
                                    cb.FmRxEvStereoStatus(false);
                                    break;
                                case (byte) 12:
                                    Log.d(FmRxEventListner.TAG, "Got RDS_AVAL_EVENT");
                                    cb.FmRxEvRdsLockStatus(true);
                                    break;
                                case (byte) 13:
                                    Log.d(FmRxEventListner.TAG, "Got RDS_NOT_AVAL_EVENT");
                                    cb.FmRxEvRdsLockStatus(false);
                                    break;
                                case (byte) 14:
                                    Log.d(FmRxEventListner.TAG, "Got NEW_SRCH_LIST");
                                    cb.FmRxEvSearchListComplete();
                                    break;
                                case (byte) 15:
                                    Log.d(FmRxEventListner.TAG, "Got NEW_AF_LIST");
                                    cb.FmRxEvRdsAfInfo();
                                    break;
                                case (byte) 16:
                                    Log.d(FmRxEventListner.TAG, "Got SIGNAL_UPDATE_EVENT");
                                    cb.FmRxEvSignalUpdate();
                                    break;
                                default:
                                    Log.d(FmRxEventListner.TAG, "Unknown event");
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        Log.d(FmRxEventListner.TAG, "RunningThread InterruptedException");
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        this.mThread.start();
    }

    public void stopListener() {
        Log.d(TAG, "stopping the Listener\n");
        if (this.mThread != null) {
            this.mThread.interrupt();
        }
    }
}
