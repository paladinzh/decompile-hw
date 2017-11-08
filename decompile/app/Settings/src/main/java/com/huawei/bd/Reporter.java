package com.huawei.bd;

import android.content.Context;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.bd.IBDService.Stub;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class Reporter {
    private static BDServiceDeathRecipient deathRecipient = new BDServiceDeathRecipient();
    private static int notAvailCount = 0;
    private static int sBetaState = -1;
    private static Method sGetServiceMethod;
    private static IBDService sService = null;
    private static Class<?> sServiceManagerClazz;

    private static final class BDServiceDeathRecipient implements DeathRecipient {
        private BDServiceDeathRecipient() {
        }

        public void binderDied() {
            Reporter.sService = null;
        }
    }

    static {
        sServiceManagerClazz = null;
        sGetServiceMethod = null;
        try {
            sServiceManagerClazz = Class.forName("android.os.ServiceManager");
            sGetServiceMethod = sServiceManagerClazz.getMethod("getService", new Class[]{String.class});
        } catch (ClassNotFoundException e) {
            Log.e("BD.Reporter", "ServiceManager ClassNotFoundException");
        } catch (NoSuchMethodException e2) {
            Log.e("BD.Reporter", "ServiceManager NoSuchMethodException");
        } catch (IllegalArgumentException e3) {
            Log.e("BD.Reporter", "ServiceManager IllegalArgumentException");
        } catch (Exception e4) {
            Log.e("BD.Reporter", "ServiceManager Exception");
        }
    }

    private Reporter() {
    }

    public static boolean e(Context context, int eventID, String eventMsg) {
        return handleEvent(context, null, null, eventID, eventMsg, 15, -1);
    }

    private static int restrictID(int eventID) {
        return (eventID & 65535) | 65536;
    }

    private static boolean ifServiceNotAvailable() {
        return notAvailCount > 5;
    }

    private static boolean handleEvent(Context context, String pkgName, String className, int eventID, String message, int priority, int taskId) {
        if (context != null && eventID <= 65535) {
            IBDService service = getService();
            if (service == null) {
                return false;
            }
            if (pkgName == null) {
                pkgName = context.getPackageName();
            }
            try {
                if (message.length() > 1024) {
                    message = message.substring(0, 1024);
                }
                service.sendAppActionData(pkgName, restrictID(eventID), message, priority);
                return true;
            } catch (RemoteException e) {
                Log.e("BD.Reporter", "sendAppActionData RemoteException");
                return false;
            }
        }
        Log.e("BD.Reporter", "null == context || eventID > 65535");
        return false;
    }

    private static IBDService getService() {
        if (ifServiceNotAvailable()) {
            return null;
        }
        if (sService != null) {
            return sService;
        }
        if (sServiceManagerClazz == null || sGetServiceMethod == null) {
            Log.e("BD.Reporter", "Can't support using ServiceManager");
            return null;
        }
        try {
            IBinder b = (IBinder) sGetServiceMethod.invoke(null, new Object[]{"com.huawei.bd.BDService"});
            if (b == null) {
                notAvailCount++;
                Log.e("BD.Reporter", "Can't getService HwBDService");
                return null;
            } else if (b.pingBinder()) {
                b.linkToDeath(deathRecipient, 0);
                sService = Stub.asInterface(b);
                Log.i("BD.Reporter", sService.toString());
                return sService;
            } else {
                Log.e("BD.Reporter", "HwBDService is not running");
                return null;
            }
        } catch (NullPointerException e) {
            Log.e("BD.Reporter", "getService ClassNotFoundException");
        } catch (IllegalArgumentException e2) {
            Log.e("BD.Reporter", "getService IllegalArgumentException");
        } catch (IllegalAccessException e3) {
            Log.e("BD.Reporter", "getService IllegalAccessException");
        } catch (InvocationTargetException e4) {
            Log.e("BD.Reporter", "getService InvocationTargetException");
        } catch (RuntimeException e5) {
            Log.e("BD.Reporter", "getService RuntimeException");
        } catch (RemoteException e6) {
            e6.printStackTrace();
        }
    }
}
