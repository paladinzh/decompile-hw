package com.huawei.cspcommon.performance;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import com.android.contacts.util.HwLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PLogPrinter {
    private static PrintHandler mPrintHandler;
    private static volatile Looper mPrintServiceLooper;
    private static HandlerThread mPrintThread;

    private static class PrintHandler extends Handler {
        public PrintHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    PLogPrinter.saveToFile(PLogPrinter.getPrintInfo(msg.obj));
                    return;
                default:
                    return;
            }
        }
    }

    public static void init() {
        mPrintThread = new HandlerThread("PLogPrinter");
        mPrintThread.start();
        mPrintServiceLooper = mPrintThread.getLooper();
        mPrintHandler = new PrintHandler(mPrintServiceLooper);
    }

    public static void print(PLogInfo info) {
        if (mPrintHandler != null) {
            Message msg = mPrintHandler.obtainMessage();
            msg.what = 1;
            msg.obj = info;
            mPrintHandler.sendMessage(msg);
        }
    }

    private static String getSceneString(int sceneId) {
        return PLogTable.getDescription(sceneId);
    }

    private static String getTimeString(long time) {
        return new SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new Date(time));
    }

    private static void saveToFile(String info) {
        IOException ioe;
        Throwable th;
        File dir = Environment.getExternalStorageDirectory();
        if (dir != null && "mounted".equals(Environment.getExternalStorageState()) && hasSpace(dir)) {
            String path = dir.getPath() + "/TempTrace";
            File parent = new File(path);
            if (parent.exists() || parent.mkdirs()) {
                File logFile = new File(path, "trace.0");
                FileOutputStream fos = null;
                try {
                    if (!inLimitSize(logFile)) {
                        reArrangeFiles(path);
                    }
                    FileOutputStream fos2 = new FileOutputStream(logFile, true);
                    try {
                        fos2.write(info.getBytes("utf-8"));
                        fos2.flush();
                        if (fos2 != null) {
                            try {
                                fos2.close();
                            } catch (IOException e) {
                                HwLog.e("PLogPrinter", "failed to close FileOutputStream");
                            }
                        }
                    } catch (IOException e2) {
                        ioe = e2;
                        fos = fos2;
                        try {
                            HwLog.e("PLogPrinter", "failed to save trace info", ioe);
                            if (fos != null) {
                                try {
                                    fos.close();
                                } catch (IOException e3) {
                                    HwLog.e("PLogPrinter", "failed to close FileOutputStream");
                                }
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (fos != null) {
                                try {
                                    fos.close();
                                } catch (IOException e4) {
                                    HwLog.e("PLogPrinter", "failed to close FileOutputStream");
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        fos = fos2;
                        if (fos != null) {
                            fos.close();
                        }
                        throw th;
                    }
                } catch (IOException e5) {
                    ioe = e5;
                    HwLog.e("PLogPrinter", "failed to save trace info", ioe);
                    if (fos != null) {
                        fos.close();
                    }
                }
            }
            HwLog.e("PLogPrinter", "failed to make dir");
            return;
        }
        HwLog.e("PLogPrinter", "exteranl storage not ready");
    }

    private static void reArrangeFiles(String path) {
        for (int i = 9; i >= 0; i--) {
            File origFile = new File(path, "trace." + Integer.toString(i));
            if (origFile.exists()) {
                if (i == 9) {
                    if (!origFile.delete()) {
                        HwLog.i("PLogPrinter", "Failed to delete file !");
                    }
                } else if (!origFile.renameTo(new File(path, "trace." + Integer.toString(i + 1)))) {
                    HwLog.i("PLogPrinter", "Failed to renames file to newPath !");
                }
            }
        }
    }

    private static boolean inLimitSize(File logFile) throws IOException {
        Throwable th;
        boolean ret = true;
        if (logFile.exists()) {
            FileInputStream fileInputStream = null;
            try {
                FileInputStream fis = new FileInputStream(logFile);
                try {
                    if (((long) fis.available()) > 4194304) {
                        ret = false;
                    }
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            HwLog.e("PLogPrinter", "failed to close FileOutputStream");
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    fileInputStream = fis;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e2) {
                            HwLog.e("PLogPrinter", "failed to close FileOutputStream");
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
        }
        return ret;
    }

    private static boolean hasSpace(File dir) {
        StatFs fs = new StatFs(dir.getPath());
        return fs.getAvailableBlocksLong() * fs.getBlockSizeLong() > 5242880;
    }

    private static String getPrintInfo(PLogInfo info) {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        sb.append(getSceneString(info.getSceneId()));
        sb.append("]: ");
        sb.append(getTimeString(info.getCreationSystemTime()));
        for (PLogNode node : info.getInfoQueue()) {
            sb.append("\n");
            sb.append(node.getMsg());
            sb.append(" | ");
            sb.append(node.getCostTimeMs());
        }
        sb.append("\n");
        return sb.toString();
    }
}
