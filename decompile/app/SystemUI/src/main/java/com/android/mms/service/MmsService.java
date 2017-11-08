package com.android.mms.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.mms.service.IHwFloatMmsService;
import com.huawei.mms.service.IHwFloatMmsService.Stub;
import fyusion.vislib.BuildConfig;

public class MmsService {
    private static long WAIT_FOR_SERVICE_CONNECT = 150;
    private static boolean hasFailCall = false;
    private static ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            IHwFloatMmsService floatService = Stub.asInterface(service);
            synchronized (MmsService.class) {
                MmsService.mService = floatService;
                MmsService.class.notifyAll();
            }
            if (MmsService.hasFailCall) {
                MmsService.hasFailCall = false;
            }
            Log.w("HwFloatMmsServiceClient", "HwFloatMmsService is connected");
        }

        public void onServiceDisconnected(ComponentName name) {
            synchronized (MmsService.class) {
                MmsService.mService = null;
                MmsService.sIsClosed = true;
            }
            Log.w("HwFloatMmsServiceClient", "HwFloatMmsService is Disconnected");
        }
    };
    private static IHwFloatMmsService mService = null;
    private static boolean sIsClosed = true;

    public static int getSmsToMmsTextThreshhold(Context context) {
        try {
            IHwFloatMmsService service = checkService(context);
            if (service != null) {
                return service.getSmsToMmsTextThreshhold();
            }
            Log.e("HwFloatMmsServiceClient", "HwFloatMmsService is null @getIs7bitEnable");
            return 0;
        } catch (RemoteException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static boolean getIs7bitEnable(Context context) {
        try {
            IHwFloatMmsService service = checkService(context);
            if (service != null) {
                return service.is7bitEnable();
            }
            Log.e("HwFloatMmsServiceClient", "HwFloatMmsService is null @getIs7bitEnable");
            return false;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static CharSequence replaceAlphabetForGsm7Bit(Context context, CharSequence s) {
        CharSequence sResult = BuildConfig.FLAVOR;
        if (s == null || s.length() <= 0) {
            return BuildConfig.FLAVOR;
        }
        try {
            IHwFloatMmsService service = checkService(context);
            if (service == null) {
                Log.e("HwFloatMmsServiceClient", "HwFloatMmsService is null @replaceAlphabetForGsm7Bit");
                return s;
            }
            sResult = service.replaceAlphabetForGsm7Bit(s, 0, s.length());
            if (sResult == null) {
                sResult = s;
            }
            return sResult;
        } catch (RemoteException e) {
            e.printStackTrace();
            return BuildConfig.FLAVOR;
        }
    }

    public static void initHwFloatMmsService(Context context) {
        synchronized (MmsService.class) {
            if (sIsClosed) {
                sIsClosed = false;
                Log.d("HwFloatMmsServiceClient", "initHwFloatMmsService service");
                Intent intentService = new Intent("android.intent.actions.HwFloatMmsService");
                intentService.setComponent(new ComponentName("com.android.mms", "com.huawei.mms.service.HwFloatMmsService"));
                context.bindService(intentService, mConnection, 1);
                return;
            }
            Log.d("HwFloatMmsServiceClient", "initHwFloatMmsService service not closed");
        }
    }

    private static IHwFloatMmsService checkService(Context context) {
        IHwFloatMmsService service;
        synchronized (MmsService.class) {
            service = mService;
            boolean isClosed = sIsClosed;
        }
        if (isClosed) {
            Log.e("HwFloatMmsServiceClient", "HwFloatMmsService is closed, reinit service");
            initHwFloatMmsService(context);
        }
        if (service == null) {
            Log.e("HwFloatMmsServiceClient", "HwFloatMmsService is null, wait for service connected");
            service = waitForConnection();
        }
        if (service == null) {
            Log.e("HwFloatMmsServiceClient", "HwFloatMmsService can't be started");
            hasFailCall = true;
        }
        return service;
    }

    private static IHwFloatMmsService waitForConnection() {
        int loop = 2;
        while (true) {
            loop--;
            if (loop > 0) {
                synchronized (MmsService.class) {
                    try {
                        if (mService == null) {
                            MmsService.class.wait(WAIT_FOR_SERVICE_CONNECT);
                        }
                    } catch (InterruptedException e) {
                        Log.w("HwFloatMmsServiceClient", "HwFloatMmsService wait for service-connect InterruptedException");
                    } catch (Exception e2) {
                        Log.w("HwFloatMmsServiceClient", "HwFloatMmsService wait for service-connect", e2);
                    }
                }
            } else {
                IHwFloatMmsService iHwFloatMmsService;
                synchronized (MmsService.class) {
                    iHwFloatMmsService = mService;
                }
                return iHwFloatMmsService;
            }
        }
    }

    public static boolean isAlertLongSmsEnable(Context context) {
        try {
            IHwFloatMmsService service = checkService(context);
            if (service != null) {
                return service.isAlertLongSmsEnable();
            }
            Log.e("HwFloatMmsServiceClient", "HwFloatMmsService is null @getIsAlertLongSmsEnable");
            return false;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }
}
