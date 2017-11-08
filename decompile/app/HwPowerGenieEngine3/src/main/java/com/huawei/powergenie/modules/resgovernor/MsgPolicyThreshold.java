package com.huawei.powergenie.modules.resgovernor;

import android.util.Log;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MsgPolicyThreshold {
    private static MsgPolicyThreshold instance = new MsgPolicyThreshold();
    private static int mLastThreshold = 0;

    private MsgPolicyThreshold() {
    }

    public static MsgPolicyThreshold getInstance() {
        return instance;
    }

    private FileOutputStream getStream(String path) {
        try {
            return new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private void closeFos(FileOutputStream fos) {
        if (fos != null) {
            try {
                fos.close();
            } catch (IOException e) {
            }
        }
    }

    private boolean write(byte[] bytes) {
        FileOutputStream fos = null;
        int i = 0;
        while (i < 8) {
            try {
                fos = getStream("/sys/devices/system/cpu/cpu" + Integer.toString(i) + "/cpufreq/msg_policy");
                if (fos != null) {
                    fos.write(bytes, 0, bytes.length);
                    Log.i("MsgPolicyThreshold", "write ok");
                    closeFos(fos);
                    break;
                }
                closeFos(fos);
                i++;
            } catch (IOException e) {
                if (i == 7) {
                    Log.w("MsgPolicyThreshold", "write fail:" + e);
                }
                closeFos(fos);
            } catch (Throwable th) {
                closeFos(fos);
            }
        }
        return true;
    }

    public boolean setThreshold(int threshold, int actionId) {
        if (mLastThreshold == threshold) {
            return true;
        }
        mLastThreshold = threshold;
        Log.i("MsgPolicyThreshold", "setThreshold,value:" + threshold + ",actionId:" + actionId);
        if (threshold <= 1) {
            threshold = 0;
        }
        if (threshold > 100) {
            threshold = 100;
        }
        write(Integer.toString(threshold).getBytes());
        return true;
    }

    public boolean requireToProcessMsgPlicy(int threshold) {
        return mLastThreshold != threshold;
    }
}
