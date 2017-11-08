package com.huawei.systemmanager.util;

import android.util.Log;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

public class HwLogTagHelper {
    private static String DATA_DIR = "/data/app";
    private static final String TAG = "HwSystemManager";

    private static class MyFileNameFilter implements FilenameFilter {
        private MyFileNameFilter() {
        }

        public boolean accept(File dir, String filename) {
            String expr = "com.huawei.systemmanager-\\d.apk";
            return filename != null ? filename.matches("com.huawei.systemmanager-\\d.apk") : false;
        }
    }

    public static boolean checkHsmIsPushed() {
        boolean z = false;
        InputStream procIn = null;
        try {
            String cmd = "ls -l /system/priv-app/HwSystemManager/HwSystemManager.apk";
            procIn = Runtime.getRuntime().exec("ls -l /system/priv-app/HwSystemManager/HwSystemManager.apk").getInputStream();
            if (procIn == null) {
                Log.w("HwSystemManager", "check pushed, no input stream.");
                if (procIn != null) {
                    try {
                        procIn.close();
                    } catch (IOException e) {
                        Log.e("HwSystemManager", "In checkHsmIsPushed, Close input stream fail.");
                    }
                }
                return false;
            }
            byte[] readBuf = new byte[128];
            if (procIn.read(readBuf, 0, 128) <= 0) {
                Log.w("HwSystemManager", "check pushed, read no message.");
                if (procIn != null) {
                    try {
                        procIn.close();
                    } catch (IOException e2) {
                        Log.e("HwSystemManager", "In checkHsmIsPushed, Close input stream fail.");
                    }
                }
                return false;
            }
            String regex = "rw-rw-rw-";
            int idx = new String(readBuf, "utf-8").indexOf("rw-rw-rw-");
            Log.i("HwSystemManager", "check push opration, match idx:" + idx);
            if (idx != -1) {
                z = true;
            }
            if (procIn != null) {
                try {
                    procIn.close();
                } catch (IOException e3) {
                    Log.e("HwSystemManager", "In checkHsmIsPushed, Close input stream fail.");
                }
            }
            return z;
        } catch (IOException ex) {
            Log.e("HwSystemManager", "check push opration of Hsm error." + ex);
            if (procIn != null) {
                try {
                    procIn.close();
                } catch (IOException e4) {
                    Log.e("HwSystemManager", "In checkHsmIsPushed, Close input stream fail.");
                }
            }
            return false;
        } catch (Exception e5) {
            Log.e("HwSystemManager", "check push opration of Hsm error.");
            if (procIn != null) {
                try {
                    procIn.close();
                } catch (IOException e6) {
                    Log.e("HwSystemManager", "In checkHsmIsPushed, Close input stream fail.");
                }
            }
            return false;
        } catch (Throwable th) {
            if (procIn != null) {
                try {
                    procIn.close();
                } catch (IOException e7) {
                    Log.e("HwSystemManager", "In checkHsmIsPushed, Close input stream fail.");
                }
            }
        }
    }

    public static boolean scanHsmInDataApp() {
        boolean z = false;
        try {
            File dataApp = new File(DATA_DIR);
            if (dataApp.exists() && dataApp.isDirectory()) {
                File[] hsmApks = dataApp.listFiles(new MyFileNameFilter());
                if (hsmApks == null) {
                    Log.e("HwSystemManager", "scanHsmInDataApp hsmApks is null");
                    return false;
                }
                for (File file : hsmApks) {
                    Log.e("HwSystemManager", "find hsm apk in data app:" + file.getName());
                }
                if (hsmApks.length > 0) {
                    z = true;
                }
                return z;
            }
            Log.w("HwSystemManager", "dir not exist:" + DATA_DIR);
            return false;
        } catch (NullPointerException e) {
            Log.e("HwSystemManager", "Get Hsm from data/app error.");
            return false;
        } catch (Exception e2) {
            Log.e("HwSystemManager", "Get Hsm from data/app error.");
            return false;
        }
    }
}
