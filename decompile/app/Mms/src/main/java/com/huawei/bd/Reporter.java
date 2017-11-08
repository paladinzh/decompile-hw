package com.huawei.bd;

import android.content.Context;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.location.places.Place;
import java.lang.reflect.Method;

public final class Reporter {
    private static BDServiceDeathRecipient deathRecipient = new BDServiceDeathRecipient();
    private static int notAvailCount = 0;
    private static Method sGetServiceMethod;
    private static IBDService sService = null;
    private static Class<?> sServiceManagerClazz;
    private static Object syncObject = new Object();

    private static final class BDServiceDeathRecipient implements DeathRecipient {
        private BDServiceDeathRecipient() {
        }

        public void binderDied() {
            synchronized (Reporter.syncObject) {
                Reporter.sService = null;
            }
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

    public static boolean c(Context context, int eventID) {
        return c(context, eventID, 1);
    }

    public static boolean c(Context context, int eventID, int count) {
        if (context != null && eventID <= 65535 && count >= 1) {
            IBDService service = getService();
            if (service == null) {
                return false;
            }
            try {
                service.sendAccumulativeData(context.getPackageName(), restrictID(eventID), count);
                return true;
            } catch (RemoteException e) {
                Log.e("BD.Reporter", "sendAccumulativeData RemoteException " + e.getCause());
                return false;
            }
        }
        Log.e("BD.Reporter", "null == context || eventID > 65535 || count < 1");
        return false;
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
                if (message.length() > Place.TYPE_SUBLOCALITY_LEVEL_2) {
                    message = message.substring(0, Place.TYPE_SUBLOCALITY_LEVEL_2);
                }
                service.sendAppActionData(pkgName, restrictID(eventID), message, priority);
                return true;
            } catch (RemoteException e) {
                Log.e("BD.Reporter", "sendAppActionData RemoteException " + e.getCause());
                return false;
            }
        }
        Log.e("BD.Reporter", "null == context || eventID > 65535");
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static synchronized IBDService getService() {
        synchronized (Reporter.class) {
            if (ifServiceNotAvailable()) {
                return null;
            }
            synchronized (syncObject) {
                if (sService == null) {
                } else {
                    IBDService iBDService = sService;
                    return iBDService;
                }
            }
        }
        iBDService = sService;
        return iBDService;
    }
}
