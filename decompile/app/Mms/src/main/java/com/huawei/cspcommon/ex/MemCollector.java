package com.huawei.cspcommon.ex;

import android.app.ActivityThread;
import android.os.Debug;
import android.os.Debug.MemoryInfo;
import android.os.Environment;
import com.android.internal.util.MemInfoReader;
import com.huawei.cspcommon.MLog;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class MemCollector {
    private static ArrayList<Class> mCriticalClazz = new ArrayList();

    public static void logMemInfo() {
        logMemInfo(15);
    }

    public static void logMemInfo(int level) {
        if ((level & 1) != 0) {
            checkBaseInfo();
            checkBaseInfo2();
        }
        if ((level & 2) != 0) {
            checkCriticalCalzz();
        }
        if ((level & 4) != 0) {
            generateMeminfoFile();
        }
        if ((level & 8) != 0) {
            generateProfileFile();
        }
    }

    private static void checkBaseInfo() {
        Debug.getMemoryInfo(new MemoryInfo());
        MLog.w("MemCollector", String.format("Native size=%d alloc=%d; dalvikPss=%d nativePss=%d TotalPss=%d", new Object[]{Long.valueOf(Debug.getNativeHeapSize()), Long.valueOf(Debug.getNativeHeapAllocatedSize()), Integer.valueOf(memInfo.dalvikPss), Integer.valueOf(memInfo.nativePss), Integer.valueOf(memInfo.getTotalPss())}));
    }

    private static void checkBaseInfo2() {
        new MemInfoReader().readMemInfo();
        MLog.w("MemCollector", String.format("Total %dKB; Cached %dKB; swap-free %dKB; swap-total %dKB;", new Object[]{Long.valueOf(r.getTotalSizeKb()), Long.valueOf(r.getCachedSizeKb()), Long.valueOf(r.getSwapFreeSizeKb()), Long.valueOf(r.getSwapTotalSizeKb())}));
    }

    public static void addCriticalClass(Class clazz) {
        synchronized (mCriticalClazz) {
            if (!mCriticalClazz.contains(clazz)) {
                mCriticalClazz.add(clazz);
            }
        }
    }

    private static void checkCriticalCalzz() {
        synchronized (mCriticalClazz) {
            for (Class clz : mCriticalClazz) {
                MLog.w("MemCollector", "Size of instance <" + clz.getName() + "> is " + Debug.countInstancesOfClass(clz));
            }
        }
    }

    private static String getDumpFileName(String pref, String dot) {
        return Environment.getExternalStorageDirectory() + pref + ErrorMonitor.getTimeMark() + dot;
    }

    private static String generateProfileFile() {
        String profFilePath = getDumpFileName("/mms_prof_", ".prof");
        try {
            Debug.dumpHprofData(profFilePath);
            MLog.i("MemCollector", "##### written hprof data to " + profFilePath);
            return profFilePath;
        } catch (IOException e) {
            MLog.e("MemCollector", "IOException when get prof data with " + profFilePath);
            return null;
        } catch (RuntimeException e2) {
            MLog.e("MemCollector", "RuntimeException when get prof data with " + profFilePath);
            return null;
        }
    }

    private static String generateMeminfoFile() {
        NoSuchMethodException e;
        Throwable th;
        FileNotFoundException e1;
        String infoFilePath = getDumpFileName("/mms_meminfo_", ".log");
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream fo = new FileOutputStream(infoFilePath);
            try {
                Debug.getMemoryInfo(new MemoryInfo());
                Object obj = ActivityThread.currentActivityThread().getApplicationThread();
                try {
                    obj.getClass().getDeclaredMethod("dumpMemInfo", new Class[]{FileDescriptor.class, MemoryInfo.class, Boolean.TYPE, Boolean.TYPE, Boolean.TYPE, String[].class}).invoke(obj, new Object[]{fo.getFD(), new MemoryInfo(), Boolean.valueOf(true), Boolean.valueOf(true), Boolean.valueOf(true), null});
                } catch (IllegalAccessException e2) {
                    e2.printStackTrace();
                } catch (IllegalArgumentException e3) {
                    e3.printStackTrace();
                } catch (InvocationTargetException e4) {
                    e4.printStackTrace();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
                if (fo != null) {
                    try {
                        fo.close();
                    } catch (IOException e6) {
                        MLog.e("MemCollector", "generateMeminfoFile Fail when close file stread");
                    }
                }
                fileOutputStream = fo;
            } catch (NoSuchMethodException e7) {
                e = e7;
                fileOutputStream = fo;
                try {
                    e.printStackTrace();
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e8) {
                            MLog.e("MemCollector", "generateMeminfoFile Fail when close file stread");
                        }
                    }
                    return infoFilePath;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e9) {
                            MLog.e("MemCollector", "generateMeminfoFile Fail when close file stread");
                        }
                    }
                    throw th;
                }
            } catch (FileNotFoundException e10) {
                e1 = e10;
                fileOutputStream = fo;
                e1.printStackTrace();
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e11) {
                        MLog.e("MemCollector", "generateMeminfoFile Fail when close file stread");
                    }
                }
                return infoFilePath;
            } catch (Throwable th3) {
                th = th3;
                fileOutputStream = fo;
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th;
            }
        } catch (NoSuchMethodException e12) {
            e = e12;
            e.printStackTrace();
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return infoFilePath;
        } catch (FileNotFoundException e13) {
            e1 = e13;
            e1.printStackTrace();
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return infoFilePath;
        }
        return infoFilePath;
    }
}
