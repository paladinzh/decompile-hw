package com.android.server.location;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import huawei.android.debug.HwDBGSwitchController;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class HwBcmGnssManager {
    private static final int BCM_NUM_PWR_MODE_STATUS = 4;
    private static final int BCM_PM_DISABLED = 0;
    private static final int BCM_PM_FULL = 1;
    private static final int BCM_PM_HOST_OFFLOAD = 3;
    private static final int BCM_PM_SAVE = 2;
    private static final String CALL_SENTRY = "CallSentry";
    private static final boolean DEBUG = HwDBGSwitchController.getDBGSwitch();
    private static final int LBS_AGC_DATA = 260;
    private static final int LBS_AIDING_EPH = 4;
    private static final int LBS_AIDING_FREQ = 8;
    private static final int LBS_AIDING_NONE = 0;
    private static final int LBS_AIDING_POS = 2;
    private static final int LBS_AIDING_STATUS = 259;
    private static final int LBS_AIDING_TIME = 1;
    private static final int LBS_ASSERT_MSG = 512;
    private static final int LBS_DOP_DATA = 261;
    private static final int LBS_DYN_CITY = 1;
    private static final int LBS_DYN_DRIVING = 2;
    private static final int LBS_DYN_INVALID = 3;
    private static final int LBS_DYN_WALKING = 0;
    private static final int LBS_POS_AUTONOMOUS = 3;
    private static final int LBS_POS_CELL_ID = 4;
    private static final int LBS_POS_HULA = 7;
    private static final int LBS_POS_LAST_KNOWN = 5;
    private static final int LBS_POS_SOURCE = 256;
    private static final int LBS_POS_UE_ASSISTED_AGPS = 1;
    private static final int LBS_POS_UE_BASED_AGPS = 2;
    private static final int LBS_POS_UE_E_ASSISTED_AGPS = 6;
    private static final int LBS_POS_UNKNOWN = 0;
    private static final int LBS_TCXO_OFFSET = 258;
    private static final int LBS_TIME_FROM_ASSIST = 2;
    private static final int LBS_TIME_FROM_POSITION = 3;
    private static final int LBS_TIME_FROM_STANDBY = 1;
    private static final int LBS_TIME_FROM_TOW = 4;
    private static final int LBS_TIME_FROM_TOW_CONFIRMED = 5;
    private static final int LBS_TIME_ROURCE = 257;
    private static final int LBS_TIME_UNKNOWN = 0;
    private static final String TAG = "HwGnssLog_BcmGnss";
    private static final boolean VERBOSE = HwDBGSwitchController.getDBGSwitch();
    private GpsPosErrorEvent mBcmGnssErr;
    private Handler mBcmGnssHander;
    private GpsSessionEvent mBcmGnssSession;
    private LocalServerSocket serverSocket;

    public class ServerThread extends Thread {
        private LocalSocket localSoc;

        private ServerThread(LocalSocket localSocket) throws IOException {
            this.localSoc = localSocket;
        }

        public void run() {
            if (HwBcmGnssManager.DEBUG) {
                Log.d(HwBcmGnssManager.TAG, "enter handle_bcm_process !localSocket is : " + this.localSoc);
            }
            InputStream inputStream = null;
            try {
                inputStream = this.localSoc.getInputStream();
                byte[] buf = new byte[264];
                while (inputStream.read(buf, 0, 264) != -1) {
                    bcmGnssMsgDecoder(buf);
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e2) {
                e2.printStackTrace();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e22) {
                        e22.printStackTrace();
                    }
                }
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e222) {
                        e222.printStackTrace();
                    }
                }
            }
        }

        private byte[] toLH(int n) {
            return new byte[]{(byte) (n & 255), (byte) ((n >> 8) & 255), (byte) ((n >> 16) & 255), (byte) ((n >> 24) & 255)};
        }

        private String toStr(byte[] valArr, int startpoint, int maxLen) {
            int index = 0;
            while (index + startpoint < valArr.length && index < maxLen && valArr[index + startpoint] != (byte) 0) {
                index++;
            }
            byte[] temp = new byte[index];
            System.arraycopy(valArr, startpoint, temp, 0, index);
            return new String(temp);
        }

        private int vtolh(byte[] bArr, int offset) {
            int n = 0;
            int i = 0;
            while (i < bArr.length && i < 4) {
                n += (bArr[offset + i] & 255) << (i * 8);
                i++;
            }
            return n;
        }

        private int byteArrayToInt(byte[] b, int offset) {
            int value = 0;
            for (int i = 0; i < 4; i++) {
                value += (b[i + offset] & 255) << ((3 - i) * 8);
            }
            return value;
        }

        private void bcmGnssMsgDecoder(byte[] bArr) {
            if (HwBcmGnssManager.DEBUG) {
                Log.d(HwBcmGnssManager.TAG, "Server Enter ProcessMessage !!!!!!");
            }
            byte[] msg = new byte[256];
            byte[] temp = new byte[4];
            System.arraycopy(bArr, 0, temp, 0, 4);
            int cmdID = vtolh(temp, 0);
            System.arraycopy(bArr, 4, temp, 0, 4);
            int len = vtolh(temp, 0);
            if (HwBcmGnssManager.DEBUG) {
                Log.d(HwBcmGnssManager.TAG, "cmdID= " + cmdID + ", len= " + len);
            }
            if (len >= 0 && len <= 248) {
                System.arraycopy(bArr, 8, msg, 0, len);
                switch (cmdID) {
                    case 256:
                        if (len == 4) {
                            int lbsPosFixSource = vtolh(msg, 0);
                            HwBcmGnssManager.this.handlerBcmPosSource(lbsPosFixSource);
                            if (HwBcmGnssManager.DEBUG) {
                                Log.d(HwBcmGnssManager.TAG, "Server: received: lbsPosFixSource is :" + lbsPosFixSource);
                                break;
                            }
                        }
                        break;
                    case HwBcmGnssManager.LBS_TIME_ROURCE /*257*/:
                        if (len == 4) {
                            int lbsTimeValidity = vtolh(msg, 0);
                            HwBcmGnssManager.this.handlerBcmTimeSource(lbsTimeValidity);
                            if (HwBcmGnssManager.DEBUG) {
                                Log.d(HwBcmGnssManager.TAG, "Server: received: lbsTimeValidity is :" + lbsTimeValidity);
                                break;
                            }
                        }
                        break;
                    case HwBcmGnssManager.LBS_TCXO_OFFSET /*258*/:
                        if (len == 4) {
                            int tcxo = vtolh(msg, 0);
                            HwBcmGnssManager.this.handlerBcmTcxoOffset(tcxo);
                            if (HwBcmGnssManager.DEBUG) {
                                Log.d(HwBcmGnssManager.TAG, "Server: received: tcxo is :" + tcxo);
                                break;
                            }
                        }
                        break;
                    case HwBcmGnssManager.LBS_AIDING_STATUS /*259*/:
                        if (len == 4) {
                            int aidingStatus = vtolh(msg, 0);
                            HwBcmGnssManager.this.handlerBcmAidingStatus(aidingStatus);
                            if (HwBcmGnssManager.DEBUG) {
                                Log.d(HwBcmGnssManager.TAG, "Server: received: aidingStatus is :" + aidingStatus);
                                break;
                            }
                        }
                        break;
                    case 260:
                        if (len == 12) {
                            float agcGPS = Float.intBitsToFloat(vtolh(msg, 0));
                            float agcGLO = Float.intBitsToFloat(vtolh(msg, 4));
                            float agcBDS = Float.intBitsToFloat(vtolh(msg, 8));
                            HwBcmGnssManager.this.handlerBcmAgcData(agcGPS, agcGLO, agcBDS);
                            if (HwBcmGnssManager.DEBUG) {
                                Log.d(HwBcmGnssManager.TAG, "Server: received: agcGPS is :" + agcGPS + "agcGPS is :" + agcGLO + "agcBDS is :" + agcBDS);
                                break;
                            }
                        }
                        break;
                    case HwBcmGnssManager.LBS_DOP_DATA /*261*/:
                        if (len == 12 && HwBcmGnssManager.DEBUG) {
                            Log.d(HwBcmGnssManager.TAG, "Server: received: LBS_DOP_DATA");
                            break;
                        }
                    case 512:
                        if (len != 0) {
                            String assertmsg = toStr(msg, 0, len);
                            if (HwBcmGnssManager.DEBUG) {
                                Log.d(HwBcmGnssManager.TAG, "Server: received: assertmsg is :" + assertmsg);
                            }
                            HwBcmGnssManager.this.handlerBcmAssert(assertmsg);
                            break;
                        }
                        break;
                    default:
                        Log.d(HwBcmGnssManager.TAG, "cmdID id is not defiend !");
                        break;
                }
            }
        }
    }

    HwBcmGnssManager(Handler handler, GpsSessionEvent gpsSessionEvent, GpsPosErrorEvent gpsPosErrorEvent) {
        this.mBcmGnssHander = handler;
        this.mBcmGnssSession = gpsSessionEvent;
        this.mBcmGnssErr = gpsPosErrorEvent;
    }

    public void bcmGnssSocketinit() {
        for (int i = 1; i <= 9; i++) {
            if (!bcmGnssSocketServer()) {
                Log.d(TAG, "bcmGnssSocketinit: CONNECT SERVER FAILD, retry count = " + i);
            }
        }
    }

    private boolean bcmGnssSocketServer() {
        try {
            this.serverSocket = new LocalServerSocket("gpssock:huawei");
            FileDescriptor bcmGnssSocket_fd = this.serverSocket.getFileDescriptor();
            if (DEBUG) {
                Log.d(TAG, "serverSocket.getFileDescriptor() IS : " + bcmGnssSocket_fd);
            }
            if (bcmGnssSocket_fd != null && handle_bcm_process() == 0) {
                Log.e(TAG, "bcmGnssSocketServer error!");
            }
            if (this.serverSocket != null) {
                try {
                    this.serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return false;
        } catch (IOException e2) {
            e2.printStackTrace();
            if (this.serverSocket != null) {
                try {
                    this.serverSocket.close();
                } catch (IOException e22) {
                    e22.printStackTrace();
                    return false;
                }
            }
            return false;
        } catch (Throwable th) {
            if (this.serverSocket != null) {
                try {
                    this.serverSocket.close();
                } catch (IOException e222) {
                    e222.printStackTrace();
                    return false;
                }
            }
        }
    }

    private int handle_bcm_process() {
        while (true) {
            try {
                new ServerThread(this.serverSocket.accept()).start();
            } catch (IOException e) {
                e.printStackTrace();
                return 1;
            } catch (Throwable th) {
                return 1;
            }
        }
    }

    private void handlerBcmPosSource(int posSource) {
        this.mBcmGnssSession.setBrcmPosSource(posSource);
    }

    private void handlerBcmTimeSource(int timeSource) {
        this.mBcmGnssSession.setBrcmTimeSource(timeSource);
    }

    private void handlerBcmAidingStatus(int status) {
        this.mBcmGnssSession.setBrcmAidingStatus(status);
    }

    private void handlerBcmTcxoOffset(int offset) {
        this.mBcmGnssSession.setBrcmTcxoOffset(offset);
    }

    private void handlerBcmAgcData(float gps, float glo, float dbs) {
        this.mBcmGnssSession.setBrcmAgcData(gps, glo, dbs);
    }

    private void handlerBcmAssert(String assertInfo) {
        boolean triggerNeeded = true;
        if (assertInfo.contains(CALL_SENTRY)) {
            triggerNeeded = false;
        }
        this.mBcmGnssErr.setBrcmAssertInfo(assertInfo);
        sendMsgToTriggerErr(30, triggerNeeded);
    }

    private void sendMsgToTriggerErr(int errorcode, boolean trigger) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(Integer.valueOf(errorcode));
        list.add(Boolean.valueOf(trigger));
        msg.what = 22;
        msg.obj = list;
        this.mBcmGnssHander.sendMessage(msg);
    }
}
