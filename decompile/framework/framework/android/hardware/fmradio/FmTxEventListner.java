package android.hardware.fmradio;

import android.util.Log;

class FmTxEventListner {
    private static final String TAG = "FMTxEventListner";
    private final int EVENT_LISTEN = 1;
    private final int RADIO_DISABLED = 18;
    private final int READY_EVENT = 0;
    private final int TUNE_EVENT = 1;
    private final int TXRDSDAT_EVENT = 16;
    private final int TXRDSDONE_EVENT = 17;
    private Thread mThread;

    FmTxEventListner() {
    }

    public void startListner(final int fd, final FmTransmitterCallbacks cb) {
        this.mThread = new Thread() {
            public void run() {
                Log.d(FmTxEventListner.TAG, "Starting Tx Event listener " + fd);
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        byte[] buff = new byte[128];
                        Log.d(FmTxEventListner.TAG, "getBufferNative called");
                        int eventCount = FmReceiverJNI.getBufferNative(fd, buff, 1);
                        Log.d(FmTxEventListner.TAG, "Received event. Count: " + eventCount);
                        for (int index = 0; index < eventCount; index++) {
                            Log.d(FmTxEventListner.TAG, "Received <" + buff[index] + ">");
                            switch (buff[index]) {
                                case (byte) 0:
                                    Log.d(FmTxEventListner.TAG, "Got RADIO_ENABLED");
                                    break;
                                case (byte) 1:
                                    Log.d(FmTxEventListner.TAG, "Got TUNE_EVENT");
                                    cb.onTuneStatusChange(FmReceiverJNI.getFreqNative(fd));
                                    break;
                                case (byte) 16:
                                    Log.d(FmTxEventListner.TAG, "Got TXRDSDAT_EVENT");
                                    cb.onRDSGroupsAvailable();
                                    break;
                                case (byte) 17:
                                    Log.d(FmTxEventListner.TAG, "Got TXRDSDONE_EVENT");
                                    cb.onContRDSGroupsComplete();
                                    break;
                                case (byte) 18:
                                    Log.d(FmTxEventListner.TAG, "Got RADIO_DISABLED");
                                    FmTransceiver.release("/dev/radio0");
                                    Thread.currentThread().interrupt();
                                    break;
                                default:
                                    Log.d(FmTxEventListner.TAG, "Unknown event");
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        Log.d(FmTxEventListner.TAG, "RunningThread InterruptedException");
                        Thread.currentThread().interrupt();
                    }
                }
                Log.d(FmTxEventListner.TAG, "Came out of the while loop");
            }
        };
        this.mThread.start();
    }

    public void stopListener() {
        Log.d(TAG, "Thread Stopped\n");
        Log.d(TAG, "stopping the Listener\n");
        if (this.mThread != null) {
            this.mThread.interrupt();
        }
        Log.d(TAG, "Thread Stopped\n");
    }
}
