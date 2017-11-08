package com.huawei.powergenie.integration.adapter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.IPowerManager;
import android.os.IPowerManager.Stub;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class CommonAdapter {
    private static long mFirstOffRRCTime = 0;
    private static Object mHwCloseRRC = null;
    private static Object mHwNetworkPolicyManager = null;
    private static Object mHwSysResManager = null;
    private static boolean mIsValidOffRRC = true;
    private static int mMajorSub = 0;
    private static int mMaxConfigBrightness = SystemProperties.getInt("ro.config.light_ratio_max", 185);
    private static int mOffRRCCount = 0;
    private static TelephonyManager mTelephonymanager = null;
    private static boolean mUseHwCloseRrc = true;

    public static boolean offRRC(Context context) {
        boolean result = false;
        if (mIsValidOffRRC) {
            if (mOffRRCCount == 0) {
                mFirstOffRRCTime = SystemClock.elapsedRealtime();
            } else if (mOffRRCCount > 200) {
                if (SystemClock.elapsedRealtime() - mFirstOffRRCTime <= 1800000) {
                    mIsValidOffRRC = false;
                    Log.w("CommonAdapter", "It's so frequently to off rrc");
                    return false;
                }
                mOffRRCCount = 0;
            }
            result = offRRCLock(context);
        }
        return result;
    }

    private static boolean offRRCLock(Context context) {
        if (mUseHwCloseRrc) {
            return hWoffRRCInner(context);
        }
        return offRRCInner(context);
    }

    private static boolean offRRCInner(Context context) {
        try {
            if (mTelephonymanager == null) {
                mTelephonymanager = (TelephonyManager) context.getSystemService("phone");
            }
            mTelephonymanager.getClass().getMethod("closeRrc", new Class[0]).invoke(mTelephonymanager, new Object[0]);
            mOffRRCCount++;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean hWoffRRCInner(Context context) {
        try {
            if (mHwCloseRRC == null) {
                Method getDefault = Class.forName("android.telephony.HwTelephonyManager").getMethod("getDefault", new Class[0]);
                if (getDefault != null) {
                    mHwCloseRRC = getDefault.invoke(null, new Object[0]);
                }
            }
            if (mHwCloseRRC == null) {
                return false;
            }
            mHwCloseRRC.getClass().getMethod("closeRrc", new Class[0]).invoke(mHwCloseRRC, new Object[0]);
            mOffRRCCount++;
            return true;
        } catch (ClassNotFoundException e) {
            mUseHwCloseRrc = false;
            Log.w("CommonAdapter", "the class android.telephony.HwTelephonyManager not exist");
            return false;
        } catch (NoSuchMethodException e2) {
            mUseHwCloseRrc = false;
            Log.w("CommonAdapter", "the class android.telephony.HwTelephonyManager NoSuchMethod: closeRrc()");
            return false;
        } catch (LinkageError e3) {
            Log.w("CommonAdapter", "class HwTelephonyManager LinkageError", e3);
            return false;
        } catch (IllegalAccessException e4) {
            Log.w("CommonAdapter", "class HwTelephonyManager IllegalAccessException", e4);
            return false;
        } catch (IllegalArgumentException e5) {
            Log.w("CommonAdapter", "class HwTelephonyManager IllegalArgumentException", e5);
            return false;
        } catch (InvocationTargetException e6) {
            Log.w("CommonAdapter", "class HwTelephonyManager InvocationTargetException", e6);
            return false;
        }
    }

    public static int getBatteryLevelFromNode() {
        NumberFormatException ex;
        Exception e;
        Throwable th;
        int batteryLevel = -1;
        File batteryFile = new File("/sys/class/power_supply/Battery", "capacity");
        if (batteryFile == null || !batteryFile.exists()) {
            batteryFile = new File("/sys/class/power_supply/battery", "capacity");
            if (batteryFile == null || !batteryFile.exists()) {
                batteryFile = new File("/sys/class/power_supply/MainBattery", "capacity");
                if (!batteryFile.exists()) {
                    Log.w("CommonAdapter", "The battery node is not existed!");
                    return -1;
                }
            }
        }
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            BufferedReader buffreader;
            FileReader reader = new FileReader(batteryFile);
            try {
                buffreader = new BufferedReader(reader);
            } catch (NumberFormatException e2) {
                ex = e2;
                fileReader = reader;
                Log.w("CommonAdapter", "NumberFormatException ! " + ex);
                if (bufferedReader == null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException ex2) {
                        Log.w("CommonAdapter", "IOException ! " + ex2);
                    }
                } else if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException ex22) {
                        Log.w("CommonAdapter", "IOException ! " + ex22);
                    }
                }
                return batteryLevel;
            } catch (Exception e3) {
                e = e3;
                fileReader = reader;
                try {
                    Log.w("CommonAdapter", "getCurBatteryLevel Exception ! " + e);
                    if (bufferedReader == null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException ex222) {
                            Log.w("CommonAdapter", "IOException ! " + ex222);
                        }
                    } else if (fileReader != null) {
                        try {
                            fileReader.close();
                        } catch (IOException ex2222) {
                            Log.w("CommonAdapter", "IOException ! " + ex2222);
                        }
                    }
                    return batteryLevel;
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferedReader == null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException ex22222) {
                            Log.w("CommonAdapter", "IOException ! " + ex22222);
                        }
                    } else if (fileReader != null) {
                        try {
                            fileReader.close();
                        } catch (IOException ex222222) {
                            Log.w("CommonAdapter", "IOException ! " + ex222222);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileReader = reader;
                if (bufferedReader == null) {
                    bufferedReader.close();
                } else if (fileReader != null) {
                    fileReader.close();
                }
                throw th;
            }
            try {
                String batteryStr = buffreader.readLine();
                if (batteryStr != null) {
                    batteryLevel = Integer.parseInt(batteryStr.trim());
                }
                if (buffreader != null) {
                    try {
                        buffreader.close();
                    } catch (IOException ex2222222) {
                        Log.w("CommonAdapter", "IOException ! " + ex2222222);
                    }
                } else if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex22222222) {
                        Log.w("CommonAdapter", "IOException ! " + ex22222222);
                    }
                }
                fileReader = reader;
            } catch (NumberFormatException e4) {
                ex = e4;
                bufferedReader = buffreader;
                fileReader = reader;
                Log.w("CommonAdapter", "NumberFormatException ! " + ex);
                if (bufferedReader == null) {
                    bufferedReader.close();
                } else if (fileReader != null) {
                    fileReader.close();
                }
                return batteryLevel;
            } catch (Exception e5) {
                e = e5;
                bufferedReader = buffreader;
                fileReader = reader;
                Log.w("CommonAdapter", "getCurBatteryLevel Exception ! " + e);
                if (bufferedReader == null) {
                    bufferedReader.close();
                } else if (fileReader != null) {
                    fileReader.close();
                }
                return batteryLevel;
            } catch (Throwable th4) {
                th = th4;
                bufferedReader = buffreader;
                fileReader = reader;
                if (bufferedReader == null) {
                    bufferedReader.close();
                } else if (fileReader != null) {
                    fileReader.close();
                }
                throw th;
            }
        } catch (NumberFormatException e6) {
            ex = e6;
            Log.w("CommonAdapter", "NumberFormatException ! " + ex);
            if (bufferedReader == null) {
                bufferedReader.close();
            } else if (fileReader != null) {
                fileReader.close();
            }
            return batteryLevel;
        } catch (Exception e7) {
            e = e7;
            Log.w("CommonAdapter", "getCurBatteryLevel Exception ! " + e);
            if (bufferedReader == null) {
                bufferedReader.close();
            } else if (fileReader != null) {
                fileReader.close();
            }
            return batteryLevel;
        }
        return batteryLevel;
    }

    public static boolean setLCDBrightness(Context context, int lcd_value) {
        System.putInt(context.getContentResolver(), "screen_brightness", lcd_value);
        IPowerManager power = Stub.asInterface(ServiceManager.getService("power"));
        boolean foundMethod = false;
        try {
            power.getClass().getMethod("setBacklightBrightness", new Class[]{Integer.TYPE}).invoke(power, new Object[]{Integer.valueOf(lcd_value)});
            foundMethod = true;
        } catch (SecurityException e) {
            Log.w("CommonAdapter", e.getCause());
        } catch (NoSuchMethodException e2) {
            Log.w("CommonAdapter", "NoSuchMethod: IPowerManager setBacklightBrightness [int]");
        } catch (IllegalAccessException e3) {
            Log.w("CommonAdapter", e3.getCause());
        } catch (InvocationTargetException e4) {
            Log.w("CommonAdapter", e4.getCause());
        }
        if (!foundMethod) {
            PowerManager pm = (PowerManager) context.getSystemService("power");
            try {
                pm.getClass().getMethod("setBacklightBrightness", new Class[]{Integer.TYPE}).invoke(pm, new Object[]{Integer.valueOf(lcd_value)});
                foundMethod = true;
            } catch (SecurityException e5) {
                Log.w("CommonAdapter", e5.getCause());
            } catch (NoSuchMethodException e6) {
                Log.w("CommonAdapter", "NoSuchMethod: PowerManager setBacklightBrightness [int]");
            } catch (IllegalAccessException e32) {
                Log.w("CommonAdapter", e32.getCause());
            } catch (InvocationTargetException e42) {
                Log.w("CommonAdapter", e42.getCause());
            }
        }
        if (!foundMethod) {
            return false;
        }
        Intent intent = new Intent("com.android.huawei.BRIGHTNESS_ACTION_SETTING_CHANGED");
        intent.setComponent(new ComponentName("com.huawei.android.toolbox", "com.huawei.android.toolbox.ToolBoxProvider"));
        context.sendBroadcastAsUser(intent, UserHandle.ALL);
        return true;
    }

    public static boolean setMobileDataEnabled(Context context, boolean enable) {
        boolean isFoundMethod = false;
        TelephonyManager tm = TelephonyManager.from(context);
        if (tm != null) {
            try {
                tm.getClass().getMethod("setDataEnabled", new Class[]{Boolean.TYPE}).invoke(tm, new Object[]{Boolean.valueOf(enable)});
                isFoundMethod = true;
                Log.d("CommonAdapter", "setDataEnabled : " + enable);
                return true;
            } catch (NoSuchMethodException e) {
                Log.w("CommonAdapter", "Method: setDataEnabled does not exists");
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        if (!isFoundMethod) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
            if (cm != null) {
                try {
                    cm.getClass().getMethod("setMobileDataEnabled", new Class[]{Boolean.TYPE}).invoke(cm, new Object[]{Boolean.valueOf(enable)});
                    Log.d("CommonAdapter", "setMobileDataEnabled : " + enable);
                    return true;
                } catch (NoSuchMethodException e3) {
                    Log.w("CommonAdapter", "Method: setMobileDataEnabled does not exists");
                } catch (Exception e22) {
                    e22.printStackTrace();
                }
            }
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int getNetworkMode(Context context) {
        int lteMode = 1;
        boolean isMultiSimEnabled = false;
        try {
            if (1 == System.getInt(context.getContentResolver(), "airplane_mode_on")) {
                Log.d("CommonAdapter", "getNetMode: Airplane mode is on !");
                return -1;
            }
            int dataMode;
            Class mSimTelMngrClazz = Class.forName("android.telephony.MSimTelephonyManager");
            Object mSimTelManager = mSimTelMngrClazz.getMethod("getDefault", new Class[0]).invoke(mSimTelMngrClazz, new Object[0]);
            if (mSimTelManager != null) {
                isMultiSimEnabled = ((Boolean) mSimTelManager.getClass().getMethod("isMultiSimEnabled", new Class[0]).invoke(mSimTelManager, new Object[0])).booleanValue();
            }
            if (isMultiSimEnabled) {
                Class getUserDefaultSubClazz = Class.forName("com.huawei.android.telephony.MSimTelephonyManagerCustEx");
                mMajorSub = ((Integer) getUserDefaultSubClazz.getMethod("getUserDefaultSubscription", new Class[]{Context.class}).invoke(getUserDefaultSubClazz, new Object[]{context})).intValue();
                boolean networkRoaming = ((Boolean) mSimTelManager.getClass().getMethod("isNetworkRoaming", new Class[]{Integer.TYPE}).invoke(mSimTelManager, new Object[]{Integer.valueOf(mMajorSub)})).booleanValue();
                int simState = ((Integer) mSimTelManager.getClass().getMethod("getSimState", new Class[]{Integer.TYPE}).invoke(mSimTelManager, new Object[]{Integer.valueOf(mMajorSub)})).intValue();
                int state = ((Integer) Class.forName("com.huawei.android.telephony.TelephonyManagerEx").getField("SIM_STATE_READY").get(null)).intValue();
                if (networkRoaming || simState != state) {
                    Log.d("CommonAdapter", "getNetMode: Msim card is not ready !");
                    return -1;
                }
                dataMode = getIntAtIndex(mMajorSub, context);
            } else if (TelephonyManager.getDefault().isNetworkRoaming() || TelephonyManager.getDefault().getSimState() != 5) {
                Log.d("CommonAdapter", "getNetMode: sim card is not ready !");
                return -1;
            } else {
                dataMode = Global.getInt(context.getContentResolver(), "preferred_network_mode", -1);
            }
            Class clazz = Class.forName("com.huawei.android.telephony.TelephonyManagerCustEx");
            if (((Integer) clazz.getMethod("checkLteServiceAbiltiy", new Class[]{Integer.TYPE, Integer.TYPE}).invoke(clazz, new Object[]{Integer.valueOf(mMajorSub), Integer.valueOf(dataMode)})).intValue() == 0) {
                lteMode = 0;
            }
            Log.i("CommonAdapter", "getNetMode: LTE Mode is " + lteMode);
            return lteMode;
        } catch (Exception e) {
            Log.w("CommonAdapter", "MajorSub Exception !");
            mMajorSub = 0;
        } catch (ClassNotFoundException e2) {
            lteMode = -1;
            Log.w("CommonAdapter", "NetWork class does not exists !");
        }
    }

    private static int getIntAtIndex(int index, Context context) {
        String val = Global.getString(context.getContentResolver(), "preferred_network_mode");
        if (val != null) {
            String[] valArray = val.split(",");
            if (index >= 0 && index < valArray.length && valArray[index] != null) {
                try {
                    return Integer.parseInt(valArray[index]);
                } catch (NumberFormatException e) {
                    Log.e("CommonAdapter", "Exception while parsing Integer!");
                }
            }
        }
        return -1;
    }

    public static boolean setNetworkMode(int lteMode) {
        Log.i("CommonAdapter", "setNetMode: LTE Mode is " + lteMode);
        try {
            Class clazz = Class.forName("com.huawei.android.telephony.TelephonyManagerCustEx");
            clazz.getMethod("setLteServiceAbility", new Class[]{Integer.TYPE, Integer.TYPE}).invoke(clazz, new Object[]{Integer.valueOf(mMajorSub), Integer.valueOf(lteMode)});
            return true;
        } catch (ClassNotFoundException e) {
            Log.w("CommonAdapter", "Class TelephonyManagerCustEx does not exists");
            return false;
        } catch (Exception e2) {
            Log.w("CommonAdapter", "setNetWorkMode Exception !");
            return false;
        }
    }

    public static String getDefaultSmsApplication(Context context) {
        try {
            ComponentName component = (ComponentName) ReflectUtils.invokeMethod("getDefaultSmsApplication", "com.android.internal.telephony.SmsApplication", new Object[]{context, Boolean.valueOf(false)});
            if (component == null) {
                return null;
            }
            String defaultSmsPackage = component.getPackageName();
            Log.i("CommonAdapter", "defaultSmsApplication: " + defaultSmsPackage);
            return defaultSmsPackage;
        } catch (Exception e) {
            Log.w("CommonAdapter", "no method getDefaultSmsApplication");
            return null;
        }
    }

    public static boolean isHwProduct() {
        return "huawei".equalsIgnoreCase(SystemProperties.get("ro.product.manufacturer", ""));
    }

    public static boolean isChinaRegion() {
        return "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    }

    public static boolean isChinaMarketProduct() {
        if (SystemProperties.getInt("ro.config.hw_optb", 0) == 156) {
            return true;
        }
        return false;
    }

    public static int getPropLowBatteryLevel() {
        return SystemProperties.getInt("ro.config.pg_lowbatterylevel", 10);
    }

    public static boolean configBrightnessRange(Context context, boolean updateRange) {
        if (updateRange) {
            PGManagerAdapter.configBrightnessRange(0, 10001, 4329);
        } else if (isBrightnessHighPrecision(context)) {
            PGManagerAdapter.configBrightnessRange(1404, (mMaxConfigBrightness + 1) * 39, 4329);
        } else {
            PGManagerAdapter.configBrightnessRange(35, mMaxConfigBrightness, 110);
        }
        return true;
    }

    private static boolean isBrightnessHighPrecision(Context context) {
        try {
            PowerManager pm = (PowerManager) context.getSystemService("power");
            return ((Boolean) pm.getClass().getMethod("isHighPrecision", new Class[0]).invoke(pm, new Object[0])).booleanValue();
        } catch (Exception e) {
            Log.w("CommonAdapter", "isHighPrecision Exception !");
            return false;
        }
    }

    public static List<String> getIAwareProtectList() {
        if (mHwSysResManager == null) {
            try {
                mHwSysResManager = ReflectUtils.invokeMethod("getInstance", "android.rms.HwSysResManager", new Object[0]);
            } catch (Exception e) {
                Log.e("CommonAdapter", "not find getInstance for HwSysResManager");
                return null;
            }
        }
        try {
            Method m = mHwSysResManager.getClass().getDeclaredMethod("getIAwareProtectList", new Class[]{Integer.TYPE});
            m.setAccessible(true);
            return (List) m.invoke(mHwSysResManager, new Object[]{Integer.valueOf(5)});
        } catch (NoSuchMethodException e2) {
            Log.w("CommonAdapter", "method not found: " + e2);
            return null;
        } catch (Exception e3) {
            Log.w("CommonAdapter", "Exception: " + e3);
            return null;
        }
    }

    public static boolean isNetworkRestricted(Context context, int uid) {
        if (mHwNetworkPolicyManager == null) {
            try {
                mHwNetworkPolicyManager = ReflectUtils.invokeMethod("from", "android.net.HwNetworkPolicyManager", new Object[]{context});
            } catch (Exception e) {
                Log.e("CommonAdapter", "not find instance for HwNetworkPolicyManager");
                return false;
            }
        }
        try {
            int state = ((Integer) mHwNetworkPolicyManager.getClass().getMethod("getHwUidPolicy", new Class[]{Integer.TYPE}).invoke(mHwNetworkPolicyManager, new Object[]{Integer.valueOf(uid)})).intValue();
            Class clazz = Class.forName("android.net.HwNetworkPolicyManager");
            if ((state & clazz.getField("POLICY_HW_RESTRICT_MOBILE").getInt(clazz)) == 1) {
                Log.i("CommonAdapter", "mobile restrict. uid=" + uid);
                return true;
            }
            Log.i("CommonAdapter", "no net restrict. uid=" + uid);
            return false;
        } catch (NoSuchMethodException e2) {
            Log.w("CommonAdapter", "method not found: " + e2);
        } catch (Exception e3) {
            Log.w("CommonAdapter", "Exception: " + e3);
        }
    }

    public static void restrictAppAutoStart(boolean restrict, List<String> pkgs) {
        try {
            ReflectUtils.invokeMethod("restrictAppAutoStart", Class.forName("huawei.android.pfw.IHwPFWManager$Stub").getMethod("asInterface", new Class[]{IBinder.class}).invoke(null, new Object[]{ServiceManager.getService("hwPfwService")}), new Object[]{Boolean.valueOf(restrict), pkgs});
        } catch (Exception ex) {
            Log.w("CommonAdapter", "restrictAppAutoStart Exception: ", ex);
        }
    }
}
