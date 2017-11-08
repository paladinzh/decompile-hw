package com.huawei.systemmanager.power.util;

import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileAtTimeCheckUtils {
    private static final String FILE_ATTIME_CHECK = "FILE_ATTIME_CHECK";
    private static final String FILE_ATTIME_CHECK_FLAG = "FILE_ATTIME_CHECK_FLAG";
    private static final int KB = 1024;
    private static String PATH = (GlobalContext.getContext().getExternalCacheDir() + "/temp");
    private static final long SLEEP_TIME = 5000;
    private static final String TAG = "FileAtTimeCheckUtils";
    private static AtomicBoolean isCheckThreadRunned = new AtomicBoolean(false);

    private static class AtTimeCheckThread extends Thread {
        public AtTimeCheckThread(String threadName) {
            super(threadName);
        }

        public void run() {
            long secondTime;
            Throwable th;
            if (FileAtTimeCheckUtils.makeFile()) {
                long firstTime = FileUtil.getlastAccess(FileAtTimeCheckUtils.PATH);
                HwLog.i("TAG", "firstTime:   " + firstTime);
                try {
                    Thread.sleep(FileAtTimeCheckUtils.SLEEP_TIME);
                } catch (InterruptedException e) {
                    HwLog.i(FileAtTimeCheckUtils.TAG, "doWait is interrupt");
                } catch (Exception e2) {
                    HwLog.i(FileAtTimeCheckUtils.TAG, "doWait is interrupt");
                }
                BufferedReader bufferedReader = null;
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(FileAtTimeCheckUtils.PATH), "utf-8"));
                    try {
                        br.read();
                        if (br != null) {
                            try {
                                br.close();
                            } catch (IOException e3) {
                                HwLog.i(FileAtTimeCheckUtils.TAG, "exception in close");
                            }
                        }
                        bufferedReader = br;
                    } catch (IOException e4) {
                        bufferedReader = br;
                        try {
                            HwLog.i(FileAtTimeCheckUtils.TAG, "exception in read");
                            if (bufferedReader != null) {
                                try {
                                    bufferedReader.close();
                                } catch (IOException e5) {
                                    HwLog.i(FileAtTimeCheckUtils.TAG, "exception in close");
                                }
                            }
                            secondTime = FileUtil.getlastAccess(FileAtTimeCheckUtils.PATH);
                            HwLog.i("TAG", "secondTime:   " + secondTime);
                            if (secondTime != firstTime) {
                                FileAtTimeCheckUtils.setChangeAtTimeSuccess();
                            } else {
                                FileAtTimeCheckUtils.setChangeAtTimeFail();
                            }
                            return;
                        } catch (Throwable th2) {
                            th = th2;
                            if (bufferedReader != null) {
                                try {
                                    bufferedReader.close();
                                } catch (IOException e6) {
                                    HwLog.i(FileAtTimeCheckUtils.TAG, "exception in close");
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        bufferedReader = br;
                        if (bufferedReader != null) {
                            bufferedReader.close();
                        }
                        throw th;
                    }
                } catch (IOException e7) {
                    HwLog.i(FileAtTimeCheckUtils.TAG, "exception in read");
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    secondTime = FileUtil.getlastAccess(FileAtTimeCheckUtils.PATH);
                    HwLog.i("TAG", "secondTime:   " + secondTime);
                    if (secondTime != firstTime) {
                        FileAtTimeCheckUtils.setChangeAtTimeFail();
                    } else {
                        FileAtTimeCheckUtils.setChangeAtTimeSuccess();
                    }
                    return;
                }
                secondTime = FileUtil.getlastAccess(FileAtTimeCheckUtils.PATH);
                HwLog.i("TAG", "secondTime:   " + secondTime);
                if (secondTime != firstTime) {
                    FileAtTimeCheckUtils.setChangeAtTimeSuccess();
                } else {
                    FileAtTimeCheckUtils.setChangeAtTimeFail();
                }
                return;
            }
            HwLog.i(FileAtTimeCheckUtils.TAG, "make file failed ");
        }
    }

    private static void setChangeAtTimeSuccess() {
        writePref(1);
    }

    private static void setChangeAtTimeFail() {
        writePref(-1);
    }

    private static void writePref(int value) {
        GlobalContext.getContext().getSharedPreferences(FILE_ATTIME_CHECK, 0).edit().putInt(FILE_ATTIME_CHECK_FLAG, value).commit();
    }

    public static boolean isChangeAtTimeSuccess() {
        int value = GlobalContext.getContext().getSharedPreferences(FILE_ATTIME_CHECK, 0).getInt(FILE_ATTIME_CHECK_FLAG, 0);
        HwLog.i(TAG, "isChangeAtTimeSuccess, result is: " + value);
        if (value == 0) {
            if (!isCheckThreadRunned.get()) {
                isCheckThreadRunned.set(true);
                new AtTimeCheckThread("AtTimeCheckThread").start();
            }
        } else if (value == 1) {
            return true;
        }
        return false;
    }

    private static boolean makeFile() {
        File file = GlobalContext.getContext().getExternalCacheDir();
        if (file == null) {
            HwLog.i("TAG", "external cache is not found");
            return false;
        }
        if (!file.exists()) {
            HwLog.d("TAG", "makeFile result=" + file.mkdirs());
        }
        return writeFileInKb(PATH, 11264);
    }

    private static boolean writeFileInKb(String filePath, long nKB) {
        Throwable th;
        byte[] date = new byte[1024];
        for (int i = 0; i < 1024; i++) {
            date[i] = (byte) 48;
        }
        int current = 0;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
            while (((long) current) < nKB) {
                try {
                    outputStream.write(date);
                    current++;
                } catch (FileNotFoundException e) {
                    bufferedOutputStream = outputStream;
                } catch (IOException e2) {
                    bufferedOutputStream = outputStream;
                } catch (Throwable th2) {
                    th = th2;
                    bufferedOutputStream = outputStream;
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e3) {
                    HwLog.i(TAG, "io exception in close file");
                }
            }
            return true;
        } catch (FileNotFoundException e4) {
            HwLog.i(TAG, "file exception in write file");
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (IOException e5) {
                    HwLog.i(TAG, "io exception in close file");
                }
            }
            return false;
        } catch (IOException e6) {
            try {
                HwLog.i(TAG, "io exception in write file");
                if (bufferedOutputStream != null) {
                    try {
                        bufferedOutputStream.close();
                    } catch (IOException e7) {
                        HwLog.i(TAG, "io exception in close file");
                    }
                }
                return false;
            } catch (Throwable th3) {
                th = th3;
                if (bufferedOutputStream != null) {
                    try {
                        bufferedOutputStream.close();
                    } catch (IOException e8) {
                        HwLog.i(TAG, "io exception in close file");
                    }
                }
                throw th;
            }
        }
    }
}
