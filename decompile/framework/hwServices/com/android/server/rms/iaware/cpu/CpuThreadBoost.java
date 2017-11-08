package com.android.server.rms.iaware.cpu;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.os.Process;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.cpu.CPUFeature.CPUFeatureHandler;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class CpuThreadBoost {
    private static final String ACTION = "com.huawei.thermal.KEY_THREAD_SCHED";
    private static final int INVALID_PID = -1;
    private static final String PERMISSION = "com.huawei.thermal.receiverPermission";
    private static final String STR_BINDER = "Binder:";
    private static final String STR_INCALLUI_MAIN_THREAD = "ndroid.incallui";
    private static final String STR_INCALLUI_PROC_NAME = "com.android.incallui";
    private static final String STR_SYSTEM_SERVER_PROC_NAME = "system_server";
    private static final String TAG = "CpuThreadBoost";
    private static CpuThreadBoost sInstance;
    private List<String> mBoostThreadsList = new ArrayList();
    private CPUFeatureHandler mCPUFeatureHandler;
    private CPUFeature mCPUFeatureInstance;
    private Context mContext;
    private boolean mEnable = false;
    private boolean mIsIncallUIBoost = false;
    private ThermalReceiver mThermalReceiver;

    private class ThermalReceiver extends BroadcastReceiver {
        private ThermalReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                AwareLog.e(CpuThreadBoost.TAG, "PowerStateChangeReceiver onReceive intent null!");
                return;
            }
            if (CpuThreadBoost.ACTION.equals(intent.getAction())) {
                String limitStr = intent.getStringExtra("key_thread_sched");
                if (limitStr == null || limitStr.length() == 0) {
                    AwareLog.e(CpuThreadBoost.TAG, "recv thermal msg err");
                    return;
                }
                AwareLog.d(CpuThreadBoost.TAG, "recv thermal msg, key_thread_sched=" + limitStr);
                try {
                    int limit = Integer.parseInt(limitStr);
                    if (CpuThreadBoost.this.mCPUFeatureInstance == null) {
                        AwareLog.e(CpuThreadBoost.TAG, "ThermalReceiver mCPUFeatureInstance is null");
                    } else if (limit == 1) {
                        CpuThreadBoost.this.mCPUFeatureInstance.sendPacketByMsgCode(CPUFeature.MSG_THERMAL_LIMITED);
                    } else if (limit == 0) {
                        CpuThreadBoost.this.mCPUFeatureInstance.sendPacketByMsgCode(CPUFeature.MSG_THERMAL_NOT_LIMITED);
                    }
                } catch (NumberFormatException e) {
                    AwareLog.e(CpuThreadBoost.TAG, "parseInt NumberFormatException e = " + e.getMessage());
                }
            }
        }
    }

    private CpuThreadBoost() {
    }

    public static synchronized CpuThreadBoost getInstance() {
        CpuThreadBoost cpuThreadBoost;
        synchronized (CpuThreadBoost.class) {
            if (sInstance == null) {
                sInstance = new CpuThreadBoost();
            }
            cpuThreadBoost = sInstance;
        }
        return cpuThreadBoost;
    }

    public void setBoostThreadsList(String[] threadsBoostInfo) {
        if (threadsBoostInfo == null) {
            AwareLog.i(TAG, "threadBootInfo is empty");
            return;
        }
        int len = threadsBoostInfo.length;
        this.mIsIncallUIBoost = false;
        this.mBoostThreadsList.clear();
        int i = 0;
        while (i < len) {
            if (ThreadBoostConfig.GAP_IDENTIFIER.equals(threadsBoostInfo[i])) {
                i = obtainBoostProcInfo(threadsBoostInfo, i + 1, len);
            }
            i++;
        }
    }

    private int obtainBoostProcInfo(String[] threadsBoostInfo, int start, int len) {
        int i = start;
        if (start >= len) {
            return start;
        }
        int i2;
        if (STR_SYSTEM_SERVER_PROC_NAME.equals(threadsBoostInfo[start])) {
            i2 = start + 1;
            while (i2 < len && !ThreadBoostConfig.GAP_IDENTIFIER.equals(threadsBoostInfo[i2])) {
                i = i2 + 1;
                this.mBoostThreadsList.add(threadsBoostInfo[i2]);
                i2 = i;
            }
            this.mBoostThreadsList.add(STR_BINDER);
            i = i2;
        } else if (STR_INCALLUI_PROC_NAME.equals(threadsBoostInfo[start])) {
            i2 = start + 1;
            while (i2 < len && !ThreadBoostConfig.GAP_IDENTIFIER.equals(threadsBoostInfo[i2])) {
                i = i2 + 1;
                if (STR_INCALLUI_MAIN_THREAD.equals(threadsBoostInfo[i2])) {
                    this.mIsIncallUIBoost = true;
                    break;
                }
                i2 = i;
            }
            i = i2;
        }
        return i - 1;
    }

    public void start(CPUFeature feature, CPUFeatureHandler handler, Context contex) {
        this.mEnable = true;
        this.mContext = contex;
        this.mCPUFeatureInstance = feature;
        this.mCPUFeatureHandler = handler;
        registerThermalReceiver();
        List<String> tidStrArray = new ArrayList();
        getSystemThreads(tidStrArray);
        if (this.mIsIncallUIBoost && getUIThreads(tidStrArray) < 0) {
            AwareLog.w(TAG, "start getUIThreads failed!");
        }
        sendPacket(tidStrArray);
    }

    public void stop() {
        this.mEnable = false;
        unregisterThermalReceiver();
        if (this.mCPUFeatureHandler != null) {
            this.mCPUFeatureHandler.removeMessages(CPUFeature.MSG_UI_BOOST);
        }
    }

    private void registerThermalReceiver() {
        if (this.mContext != null && this.mThermalReceiver == null) {
            this.mThermalReceiver = new ThermalReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION);
            this.mContext.registerReceiver(this.mThermalReceiver, filter, PERMISSION, null);
        }
    }

    private void unregisterThermalReceiver() {
        if (!(this.mContext == null || this.mThermalReceiver == null)) {
            this.mContext.unregisterReceiver(this.mThermalReceiver);
            this.mThermalReceiver = null;
        }
    }

    public void uiBoost(int pid, Object procName) {
        if (this.mEnable && this.mIsIncallUIBoost && procName != null && pid >= 0 && STR_INCALLUI_PROC_NAME.equals(procName)) {
            AwareLog.d(TAG, "uiBoost pid=" + pid + ", procName=" + procName);
            List<String> tidStrArray = new ArrayList();
            tidStrArray.add(Integer.toString(pid));
            sendPacket(tidStrArray);
        }
    }

    private void closeBufferedReader(BufferedReader br) {
        if (br != null) {
            try {
                br.close();
            } catch (IOException e) {
                AwareLog.e(TAG, "closeBufferedReader exception " + e.getMessage());
            }
        }
    }

    private void closeInputStreamReader(InputStreamReader isr) {
        if (isr != null) {
            try {
                isr.close();
            } catch (IOException e) {
                AwareLog.e(TAG, "closeInputStreamReader exception " + e.getMessage());
            }
        }
    }

    private void closeFileInputStream(FileInputStream fis) {
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                AwareLog.e(TAG, "closeFileInputStream exception " + e.getMessage());
            }
        }
    }

    private int getUIThreads(List<String> tidStrArray) {
        if (this.mContext == null) {
            return -1;
        }
        ActivityManager activityManager = (ActivityManager) this.mContext.getSystemService("activity");
        if (activityManager == null) {
            AwareLog.e(TAG, "get system service failed! activityManager is null");
            return -1;
        }
        List<RunningAppProcessInfo> procList = activityManager.getRunningAppProcesses();
        if (procList == null) {
            AwareLog.e(TAG, "get running app processes failed! procList is null");
            return -1;
        }
        for (RunningAppProcessInfo process : procList) {
            if (process != null && process.processName != null && STR_INCALLUI_PROC_NAME.equals(process.processName)) {
                int pid = process.pid;
                tidStrArray.add(Integer.toString(pid));
                AwareLog.d(TAG, "getUIThreads pid=" + pid);
                return pid;
            }
        }
        return -1;
    }

    private String getThreadName(String tidPath) {
        Throwable th;
        String commFilePath = tidPath + "/" + "comm";
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        String tidName = null;
        try {
            InputStreamReader inputStreamReader2;
            BufferedReader bufReader;
            FileInputStream input = new FileInputStream(commFilePath);
            try {
                inputStreamReader2 = new InputStreamReader(input, "UTF-8");
                try {
                    bufReader = new BufferedReader(inputStreamReader2);
                } catch (FileNotFoundException e) {
                    inputStreamReader = inputStreamReader2;
                    fileInputStream = input;
                    AwareLog.e(TAG, "exception file not found, file path: " + commFilePath);
                    closeBufferedReader(bufferedReader);
                    closeInputStreamReader(inputStreamReader);
                    closeFileInputStream(fileInputStream);
                    return tidName;
                } catch (UnsupportedEncodingException e2) {
                    inputStreamReader = inputStreamReader2;
                    fileInputStream = input;
                    AwareLog.e(TAG, "UnsupportedEncodingException ");
                    closeBufferedReader(bufferedReader);
                    closeInputStreamReader(inputStreamReader);
                    closeFileInputStream(fileInputStream);
                    return tidName;
                } catch (IOException e3) {
                    inputStreamReader = inputStreamReader2;
                    fileInputStream = input;
                    try {
                        AwareLog.e(TAG, "getSystemServerThreads failed!");
                        closeBufferedReader(bufferedReader);
                        closeInputStreamReader(inputStreamReader);
                        closeFileInputStream(fileInputStream);
                        return tidName;
                    } catch (Throwable th2) {
                        th = th2;
                        closeBufferedReader(bufferedReader);
                        closeInputStreamReader(inputStreamReader);
                        closeFileInputStream(fileInputStream);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    inputStreamReader = inputStreamReader2;
                    fileInputStream = input;
                    closeBufferedReader(bufferedReader);
                    closeInputStreamReader(inputStreamReader);
                    closeFileInputStream(fileInputStream);
                    throw th;
                }
            } catch (FileNotFoundException e4) {
                fileInputStream = input;
                AwareLog.e(TAG, "exception file not found, file path: " + commFilePath);
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
                return tidName;
            } catch (UnsupportedEncodingException e5) {
                fileInputStream = input;
                AwareLog.e(TAG, "UnsupportedEncodingException ");
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
                return tidName;
            } catch (IOException e6) {
                fileInputStream = input;
                AwareLog.e(TAG, "getSystemServerThreads failed!");
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
                return tidName;
            } catch (Throwable th4) {
                th = th4;
                fileInputStream = input;
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
                throw th;
            }
            try {
                tidName = bufReader.readLine();
                closeBufferedReader(bufReader);
                closeInputStreamReader(inputStreamReader2);
                closeFileInputStream(input);
                fileInputStream = input;
            } catch (FileNotFoundException e7) {
                bufferedReader = bufReader;
                inputStreamReader = inputStreamReader2;
                fileInputStream = input;
                AwareLog.e(TAG, "exception file not found, file path: " + commFilePath);
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
                return tidName;
            } catch (UnsupportedEncodingException e8) {
                bufferedReader = bufReader;
                inputStreamReader = inputStreamReader2;
                fileInputStream = input;
                AwareLog.e(TAG, "UnsupportedEncodingException ");
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
                return tidName;
            } catch (IOException e9) {
                bufferedReader = bufReader;
                inputStreamReader = inputStreamReader2;
                fileInputStream = input;
                AwareLog.e(TAG, "getSystemServerThreads failed!");
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
                return tidName;
            } catch (Throwable th5) {
                th = th5;
                bufferedReader = bufReader;
                inputStreamReader = inputStreamReader2;
                fileInputStream = input;
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
                throw th;
            }
        } catch (FileNotFoundException e10) {
            AwareLog.e(TAG, "exception file not found, file path: " + commFilePath);
            closeBufferedReader(bufferedReader);
            closeInputStreamReader(inputStreamReader);
            closeFileInputStream(fileInputStream);
            return tidName;
        } catch (UnsupportedEncodingException e11) {
            AwareLog.e(TAG, "UnsupportedEncodingException ");
            closeBufferedReader(bufferedReader);
            closeInputStreamReader(inputStreamReader);
            closeFileInputStream(fileInputStream);
            return tidName;
        } catch (IOException e12) {
            AwareLog.e(TAG, "getSystemServerThreads failed!");
            closeBufferedReader(bufferedReader);
            closeInputStreamReader(inputStreamReader);
            closeFileInputStream(fileInputStream);
            return tidName;
        }
        return tidName;
    }

    private void getSystemThreads(List<String> tidStrArray) {
        File[] subFiles = new File("/proc/" + Process.myPid() + "/task/").listFiles();
        if (subFiles != null) {
            for (File eachTidFile : subFiles) {
                String str = AppHibernateCst.INVALID_PKG;
                try {
                    str = eachTidFile.getCanonicalPath();
                    String tidName = getThreadName(str);
                    if (tidName != null) {
                        for (int i = 0; i < this.mBoostThreadsList.size(); i++) {
                            if (tidName.contains((CharSequence) this.mBoostThreadsList.get(i))) {
                                String tidStr = getTidStr(str);
                                if (tidStr != null) {
                                    tidStrArray.add(tidStr);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                }
            }
        }
    }

    private String getTidStr(String tidPath) {
        String[] subStr = tidPath.split("task/");
        if (subStr.length == 2) {
            return subStr[1];
        }
        AwareLog.e(TAG, "getTid failed, error path is " + tidPath);
        return null;
    }

    private void sendPacket(List<String> tidStrArray) {
        int num = tidStrArray.size();
        int[] tids = new int[num];
        int i = 0;
        while (i < num) {
            try {
                tids[i] = Integer.parseInt((String) tidStrArray.get(i));
                i++;
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "parseInt failed!");
                return;
            }
        }
        ByteBuffer buffer = ByteBuffer.allocate((len + 2) * 4);
        buffer.putInt(CPUFeature.MSG_THREAD_BOOST);
        buffer.putInt(len);
        for (int putInt : tids) {
            buffer.putInt(putInt);
        }
        if (this.mCPUFeatureInstance != null) {
            this.mCPUFeatureInstance.sendPacket(buffer);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyProcStart(int pid, String procName) {
        if (procName != null && STR_INCALLUI_PROC_NAME.equals(procName) && this.mEnable && this.mIsIncallUIBoost && this.mCPUFeatureHandler != null) {
            this.mCPUFeatureHandler.removeMessages(CPUFeature.MSG_UI_BOOST);
            Message msg = this.mCPUFeatureHandler.obtainMessage(CPUFeature.MSG_UI_BOOST);
            msg.arg1 = pid;
            msg.obj = procName;
            this.mCPUFeatureHandler.sendMessage(msg);
        }
    }

    private void removeCpusMsg() {
        if (this.mCPUFeatureHandler != null) {
            this.mCPUFeatureHandler.removeMessages(CPUFeature.MSG_SET_BOOST_CPUS);
            this.mCPUFeatureHandler.removeMessages(CPUFeature.MSG_RESET_BOOST_CPUS);
        }
    }

    public void setBoostCpus() {
        if (this.mEnable && this.mCPUFeatureHandler != null) {
            removeCpusMsg();
            this.mCPUFeatureHandler.sendEmptyMessage(CPUFeature.MSG_SET_BOOST_CPUS);
        }
    }

    public void resetBoostCpus() {
        if (this.mEnable && this.mCPUFeatureHandler != null) {
            removeCpusMsg();
            this.mCPUFeatureHandler.sendEmptyMessage(CPUFeature.MSG_RESET_BOOST_CPUS);
        }
    }
}
