package com.huawei.powergenie.integration.adapter;

import android.util.Log;
import java.util.List;

public final class PGManagerAdapter {
    private static Object mPGM;
    private static boolean mSupportProxyApp = false;

    static {
        mPGM = null;
        if (mPGM == null) {
            try {
                mPGM = ReflectUtils.invokeMethod("getInstance", "com.huawei.pgmng.api.PGManager", new Object[]{null});
            } catch (Exception e) {
                Log.e("PGManagerAdapter", "init PGManager error");
            }
        }
    }

    public static long proxyBroadcast(List<String> pkgs, boolean proxy) {
        if (mPGM != null) {
            try {
                return ((Long) ReflectUtils.invokeMethod("proxyBroadcast", mPGM, new Object[]{pkgs, Boolean.valueOf(proxy)})).longValue();
            } catch (Exception e) {
            }
        } else {
            Log.w("PGManagerAdapter", "mPGM is not prepared ");
            return -1;
        }
    }

    public static long proxyBroadcastByPid(List<Integer> pids, boolean proxy) {
        if (mPGM != null) {
            try {
                return ((Long) ReflectUtils.invokeMethod("proxyBroadcastByPid", mPGM, new Object[]{pids, Boolean.valueOf(proxy)})).longValue();
            } catch (Exception e) {
            }
        } else {
            Log.w("PGManagerAdapter", "mPGM is not prepared ");
            return -1;
        }
    }

    public static boolean setProxyBCActions(List<String> actions) {
        if (mPGM != null) {
            try {
                ReflectUtils.invokeMethod("setProxyBCActions", mPGM, new Object[]{actions});
                Log.i("PGManagerAdapter", "set proxy broadcast actions:" + actions);
                return true;
            } catch (Exception e) {
            }
        } else {
            Log.w("PGManagerAdapter", "mPGM is not prepared ");
            return false;
        }
    }

    public static boolean setActionExcludePkg(String action, String pkg) {
        if (mPGM != null) {
            try {
                ReflectUtils.invokeMethod("setActionExcludePkg", mPGM, new Object[]{action, pkg});
                Log.i("PGManagerAdapter", "setActionExcludePkg action:" + action + " pkg:" + pkg);
                return true;
            } catch (Exception e) {
            }
        } else {
            Log.w("PGManagerAdapter", "mPGM is not prepared ");
            return false;
        }
    }

    public static boolean proxyBCConfig(int type, String key, List<String> value) {
        if (mPGM != null) {
            try {
                ReflectUtils.invokeMethod("proxyBCConfig", mPGM, new Object[]{Integer.valueOf(type), key, value});
                Log.i("PGManagerAdapter", "proxy Config type:" + type + " key:" + key + " value:" + value);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        Log.w("PGManagerAdapter", "mPGM is not prepared ");
        return false;
    }

    public static boolean proxyWakeLockByPidUid(int pid, int uid, boolean proxy) {
        if (mPGM != null) {
            try {
                ReflectUtils.invokeMethod("proxyWakeLockByPidUid", mPGM, new Object[]{Integer.valueOf(pid), Integer.valueOf(uid), Boolean.valueOf(proxy)});
                return true;
            } catch (Exception e) {
            }
        } else {
            Log.w("PGManagerAdapter", "mPGM is not prepared ");
            return false;
        }
    }

    public static boolean forceReleaseWakeLockByPidUid(int pid, int uid) {
        if (mPGM != null) {
            try {
                ReflectUtils.invokeMethod("forceReleaseWakeLockByPidUid", mPGM, new Object[]{Integer.valueOf(pid), Integer.valueOf(uid)});
                Log.i("PGManagerAdapter", "Force release wakelock, pid = " + pid + " uid = " + uid);
                return true;
            } catch (Exception e) {
            }
        } else {
            Log.w("PGManagerAdapter", "mPGM is not prepared ");
            return false;
        }
    }

    public static boolean forceRestoreWakeLockByPidUid(int pid, int uid) {
        if (mPGM != null) {
            try {
                ReflectUtils.invokeMethod("forceRestoreWakeLockByPidUid", mPGM, new Object[]{Integer.valueOf(pid), Integer.valueOf(uid)});
                Log.i("PGManagerAdapter", "Force restore wakelock, pid = " + pid + " uid = " + uid);
                return true;
            } catch (Exception e) {
            }
        } else {
            Log.w("PGManagerAdapter", "mPGM is not prepared ");
            return false;
        }
    }

    public static int isHoldWakeLock(int uid, int wakeflag) {
        int i = 1;
        if (mPGM != null) {
            try {
                if (!((Boolean) ReflectUtils.invokeMethod("getWakeLockByUid", mPGM, new Object[]{Integer.valueOf(uid), Integer.valueOf(wakeflag)})).booleanValue()) {
                    i = 0;
                }
                return i;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.w("PGManagerAdapter", "mPGM is not prepared ");
            return -1;
        }
    }

    public static boolean setLcdRatio(int ratio, boolean autoAdjust) {
        if (mPGM != null) {
            try {
                ReflectUtils.invokeMethod("setLcdRatio", mPGM, new Object[]{Integer.valueOf(ratio), Boolean.valueOf(autoAdjust)});
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.w("PGManagerAdapter", "mPGM is not prepared ");
            return false;
        }
    }

    public static boolean configBrightnessRange(int ratioMin, int ratioMax, int autoLimit) {
        if (mPGM != null) {
            try {
                ReflectUtils.invokeMethod("configBrightnessRange", mPGM, new Object[]{Integer.valueOf(ratioMin), Integer.valueOf(ratioMax), Integer.valueOf(autoLimit)});
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.w("PGManagerAdapter", "mPGM is not prepared ");
            return false;
        }
    }

    public static boolean proxyApp(String pkg, int uid, boolean proxy) {
        if (mPGM != null) {
            try {
                Object ret = ReflectUtils.invokeMethod("proxyApp", mPGM, new Object[]{pkg, Integer.valueOf(uid), Boolean.valueOf(proxy)});
                mSupportProxyApp = true;
                return ((Boolean) ret).booleanValue();
            } catch (Exception e) {
                Log.w("PGManagerAdapter", "not find the method: proxyApp");
            }
        } else {
            Log.w("PGManagerAdapter", "mPGM is not prepared for proxyApp");
            return false;
        }
    }

    public static void killProc(int pid) {
        if (mPGM != null) {
            try {
                ReflectUtils.invokeMethod("killProc", mPGM, new Object[]{Integer.valueOf(pid)});
                return;
            } catch (Exception e) {
                Log.w("PGManagerAdapter", "not find the method: killProc");
                return;
            }
        }
        Log.w("PGManagerAdapter", "mPGM is not prepared ");
    }

    public static boolean closeSocketsForUid(int uid) {
        if (mPGM != null) {
            try {
                return ((Boolean) ReflectUtils.invokeMethod("closeSocketsForUid", mPGM, new Object[]{Integer.valueOf(uid)})).booleanValue();
            } catch (Exception e) {
                Log.w("PGManagerAdapter", "not find the method: closeSocketsForUid");
            }
        } else {
            Log.w("PGManagerAdapter", "mPGM is not prepared for closeSocketsForUid");
            return false;
        }
    }
}
