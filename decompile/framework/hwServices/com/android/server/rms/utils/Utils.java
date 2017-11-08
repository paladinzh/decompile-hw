package com.android.server.rms.utils;

import android.os.SystemProperties;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Utils {
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final long DATE_TIME_24HOURS = 86400000;
    public static final boolean DEBUG;
    public static final int FLAG_BIGDATA_STATISTIC = 1;
    public static final int FLAG_CRASH_MONITOR = 16;
    public static final int FLAG_IO_STATISTIC = 8;
    public static final int FLAG_SCREENOFF_TRIM = 4;
    public static final int FLAG_TIMING_TRIM = 2;
    public static final boolean HWFLOW;
    public static final boolean HWLOGW_E = true;
    private static final String PARAM_SPLIT = ":";
    public static final int RMSVERSION = SystemProperties.getInt("ro.config.RmsVersion", 31);
    public static final String TAG = "RMS";

    static {
        boolean z;
        boolean z2 = true;
        if (Log.HWLog) {
            z = true;
        } else if (Log.HWModuleLog) {
            z = Log.isLoggable(TAG, 3);
        } else {
            z = false;
        }
        DEBUG = z;
        if (!Log.HWINFO) {
            if (Log.HWModuleLog) {
                z2 = Log.isLoggable(TAG, 4);
            } else {
                z2 = false;
            }
        }
        HWFLOW = z2;
    }

    public static final boolean writeFile(String path, String data) {
        IOException e;
        FileOutputStream fos = null;
        try {
            FileOutputStream fos2 = new FileOutputStream(path);
            try {
                fos2.write(data.getBytes("UTF-8"));
                if (fos2 != null) {
                    try {
                        fos2.close();
                    } catch (IOException e2) {
                    }
                }
                return true;
            } catch (IOException e3) {
                e = e3;
                fos = fos2;
                try {
                    Log.w(TAG, "Unable to write " + path + " msg=" + e.getMessage());
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e4) {
                        }
                    }
                    return false;
                } catch (Throwable th) {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e5) {
                        }
                    }
                    return true;
                }
            } catch (Throwable th2) {
                fos = fos2;
                if (fos != null) {
                    fos.close();
                }
                return true;
            }
        } catch (IOException e6) {
            e = e6;
            Log.w(TAG, "Unable to write " + path + " msg=" + e.getMessage());
            if (fos != null) {
                fos.close();
            }
            return false;
        }
    }

    public static final void wait(int ms) {
        try {
            Thread.sleep((long) ms);
        } catch (InterruptedException e) {
        }
    }

    public static boolean scanArgs(String[] args, String value) {
        if (args != null) {
            for (String arg : args) {
                if (value.equals(arg)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String scanArgsWithParam(String[] args, String key) {
        if (args == null || key == null) {
            Log.e(TAG, "scanArgsWithParam,neither args or key is null");
            return null;
        }
        String result = null;
        for (String arg : args) {
            if (arg != null && arg.contains(key)) {
                String[] splitsArray = arg.split(PARAM_SPLIT);
                if (splitsArray.length < 2) {
                    break;
                }
                result = splitsArray[1];
            }
        }
        return result;
    }

    public static Object invokeMethod(Object instance, String methodName, Class[] parameterType, Object... argsValues) {
        if (instance == null) {
            Log.e(TAG, "invokeMethod,instance is null");
            return null;
        }
        Object resultObj = null;
        try {
            Method method;
            Class<?> classObj = instance.getClass();
            if (parameterType != null) {
                method = classObj.getDeclaredMethod(methodName, parameterType);
            } else {
                method = classObj.getDeclaredMethod(methodName, new Class[0]);
            }
            final Method methodResult = method;
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Void run() {
                    methodResult.setAccessible(true);
                    return null;
                }
            });
            resultObj = method.invoke(instance, argsValues);
        } catch (RuntimeException e) {
            Log.e(TAG, "invokeMethod,RuntimeException method:" + methodName + ",msg:" + e.getMessage());
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, "invokeMethod,no such method:" + methodName + ",msg:" + e2.getMessage());
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "invokeMethod,IllegalAccessException,method:" + methodName + ",msg:" + e3.getMessage());
        } catch (Exception ex) {
            Log.e(TAG, "invokeMethod,Exception,method:" + methodName + ",msg:" + ex.getMessage());
        }
        return resultObj;
    }

    public static String getDateFormatValue(long time) {
        return new SimpleDateFormat(DATE_FORMAT, Locale.US).format(new Date(time));
    }

    public static long getShortDateFormatValue(long time) {
        SimpleDateFormat sdFormatter = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        try {
            return sdFormatter.parse(sdFormatter.format(new Date(time))).getTime();
        } catch (Exception e) {
            Log.e(TAG, "getShortDateFormatValue:" + e.getMessage());
            return 0;
        }
    }

    public static long getDifferencesByDay(long time1, long time2) {
        return (time1 - time2) / 86400000;
    }

    public static long getSizeOfDirectory(File directory) {
        long totalSizeInDirectory = 0;
        try {
            if (directory.exists()) {
                String[] subFiles = directory.list();
                if (subFiles != null) {
                    for (String name : subFiles) {
                        totalSizeInDirectory += new File(directory, name).length();
                    }
                }
                return totalSizeInDirectory;
            }
            Log.e(TAG, "getSizeOfDirectory," + directory.getCanonicalPath() + " not exists");
            return 0;
        } catch (IOException ex) {
            Log.e(TAG, "getSizeOfDirectory,IOException occurs:" + ex.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "getSizeOfDirectory,Exception occurs:" + e.getMessage());
        }
    }
}
