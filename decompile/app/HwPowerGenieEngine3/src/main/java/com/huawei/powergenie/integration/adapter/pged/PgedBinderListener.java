package com.huawei.powergenie.integration.adapter.pged;

import android.util.Log;
import com.huawei.powergenie.debugtest.DbgUtils;
import com.huawei.powergenie.integration.adapter.pged.IPgedBinderListener.Stub;
import java.util.ArrayList;

public class PgedBinderListener extends Stub {
    private static final boolean DEBUG_USB = (DbgUtils.DBG_USB);
    private KStateMonitor mMonitor;

    protected PgedBinderListener(KStateMonitor monitor) {
        this.mMonitor = monitor;
    }

    private int byteArrayToInt(byte[] b, int offset) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            value += (b[i + offset] & 255) << (i * 8);
        }
        return value;
    }

    public int onKstateCallback(int len, byte[] buf) {
        if (buf == null) {
            Log.e("PgedBinderListener", "buf == null");
            return -1;
        }
        int type = byteArrayToInt(buf, 0);
        int time_s = byteArrayToInt(buf, 4);
        int time_us = byteArrayToInt(buf, 8);
        int length = byteArrayToInt(buf, 12);
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = buf[i + 16];
        }
        try {
            String msg = new String(data);
            ArrayList<Integer> value3 = new ArrayList();
            switch (type) {
                case 8:
                case 16:
                    String[] msgInfo = msg.split(" ");
                    String[] msgInfo1 = msgInfo[2].split("]");
                    String[] msgInfo2 = msgInfo[3].split("]");
                    int value1 = Integer.parseInt(msgInfo[1]);
                    String value2 = msgInfo1[0];
                    value3.add(Integer.valueOf(Integer.parseInt(msgInfo2[0])));
                    if (DEBUG_USB) {
                        Log.i("PgedBinderListener", "kstate callback type:" + type + " value1=" + value1 + " value2=" + value2);
                    }
                    if (this.mMonitor != null) {
                        this.mMonitor.onKStateEvent(type, value1, value2, value3);
                        break;
                    }
                    break;
                default:
                    Log.w("PgedBinderListener", "unknown kernel state Type:" + type + " msg:" + msg);
                    return 0;
            }
        } catch (Exception e) {
            Log.e("PgedBinderListener", "onKstateCallback Exception = ", e);
        }
        return 0;
    }

    public int onNetRecalledMsgCallback(int len, int[] uids) {
        if (len == 0) {
            Log.e("PgedBinderListener", "no uid to process !");
            return -1;
        } else if (uids == null) {
            Log.e("PgedBinderListener", "onNetRecalledMsgCallback uids == null");
            return -1;
        } else {
            ArrayList<Integer> uidsList = new ArrayList();
            for (int i = 0; i < len; i++) {
                if (!uidsList.contains(Integer.valueOf(uids[i]))) {
                    uidsList.add(Integer.valueOf(uids[i]));
                }
            }
            String value2 = "NET";
            Log.i("PgedBinderListener", "net callback uids list:" + uidsList);
            if (this.mMonitor != null) {
                this.mMonitor.onKStateEvent(8, -1, value2, uidsList);
            }
            return 0;
        }
    }

    public int onMessageCallback(String msg, int action) {
        Log.i("PgedBinderListener", " onMessageCallback, msg: " + msg + ", action: " + action);
        return 0;
    }
}
