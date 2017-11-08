package com.huawei.securitymgr;

import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.amap.api.services.core.AMapException;
import com.huawei.securitymgr.IAuthenticationClient.Stub;

public class AuthenticationManager {
    private DeathRecipient mAuServiceDeathRecipient = new DeathRecipient() {
        public void binderDied() {
            Log.e("AuthenticationManager", "xFinger--authentication service binderDied");
            if (AuthenticationManager.this.mIdentifyCallback != null) {
                AuthenticationManager.this.mIdentifyCallback.onNoMatch(AMapException.CODE_AMAP_NEARBY_KEY_NOT_BIND);
                Log.d("AuthenticationManager", "xFinger--IDENTIFY_FAILED_FPSERVICE_CRASHED");
            }
        }
    };
    private int mAuthenticationType;
    private CaptureCallback mCaptureCallback;
    private IAuthenticationClient mClient;
    private EventHandler mEventhHandler;
    private IdentifyCallback mIdentifyCallback;
    private IAuthenticationService mService;

    public interface CaptureCallback {
        void onCaptureCompleted();

        void onInput();

        void onWaitingForInput();
    }

    private class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            switch (msg.what) {
                case 1:
                    if (AuthenticationManager.this.mCaptureCallback != null) {
                        AuthenticationManager.this.mCaptureCallback.onWaitingForInput();
                        return;
                    }
                    return;
                case 2:
                    Log.d("AuthenticationManager", "xFinger--onInput :MSG_FINGER_PRESENT in AuthenticationManager");
                    if (AuthenticationManager.this.mCaptureCallback != null) {
                        AuthenticationManager.this.mCaptureCallback.onInput();
                        return;
                    }
                    return;
                case 3:
                    Log.d("AuthenticationManager", "xFinger--onCaptureCompleted :MSG_FINGER_UP in AuthenticationManager");
                    if (AuthenticationManager.this.mCaptureCallback != null) {
                        AuthenticationManager.this.mCaptureCallback.onCaptureCompleted();
                        return;
                    }
                    return;
                case 6:
                    if (AuthenticationManager.this.mIdentifyCallback != null) {
                        IdentifyCallback -get2 = AuthenticationManager.this.mIdentifyCallback;
                        int i = msg.arg1;
                        byte[] bArr = (byte[]) msg.obj;
                        if (msg.arg2 != 1) {
                            z = false;
                        }
                        -get2.onIdentified(i, bArr, z);
                        return;
                    }
                    return;
                case 7:
                    if (AuthenticationManager.this.mIdentifyCallback != null) {
                        AuthenticationManager.this.mIdentifyCallback.onNoMatch(msg.arg1);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public interface IdentifyCallback {
        void onIdentified(int i, byte[] bArr, boolean z);

        void onNoMatch(int i);
    }

    public static AuthenticationManager open(int authenticationType) {
        IAuthenticationService service = getAuthenticationService();
        if (service == null) {
            Log.e("AuthenticationManager", "AuthenticationManager open failed: the AuthenticationService is null");
            return null;
        }
        Looper looper = Looper.getMainLooper();
        if (looper == null) {
            Log.e("AuthenticationManager", "AuthenticationManager open failed: looper is null!");
            return null;
        }
        try {
            return new AuthenticationManager(service, looper, authenticationType);
        } catch (Exception e) {
            Log.e("AuthenticationManager", "fail to open AuthenticationManager");
            return null;
        }
    }

    private AuthenticationManager(IAuthenticationService service, Looper looper, int authenticationType) throws RemoteException {
        this.mService = service;
        EventHandler handler = new EventHandler(looper);
        this.mAuthenticationType = authenticationType;
        this.mClient = new Stub() {
            public void onMessage(int what, int arg1, int arg2, byte[] payload) throws RemoteException {
                if (AuthenticationManager.this.mEventhHandler != null) {
                    AuthenticationManager.this.mEventhHandler.sendMessage(AuthenticationManager.this.mEventhHandler.obtainMessage(what, arg1, arg2, payload));
                }
            }
        };
        if (this.mService.open(this.mClient, authenticationType)) {
            this.mEventhHandler = handler;
            service.asBinder().linkToDeath(this.mAuServiceDeathRecipient, 0);
            return;
        }
        Log.e("AuthenticationManager", "!mService.open(mClient, authenticationType)");
        throw new RuntimeException();
    }

    public void release() {
        if (this.mClient != null) {
            try {
                this.mService.release(this.mClient);
                this.mService.asBinder().unlinkToDeath(this.mAuServiceDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public int[] getIds() {
        try {
            return this.mService.getIds(this.mClient);
        } catch (RemoteException e) {
            e.printStackTrace();
            return new int[0];
        }
    }

    private static IAuthenticationService getAuthenticationService() {
        IBinder b = ServiceManager.getService("authentication_service");
        if (b == null) {
            Log.d("AuthenticationManager", "get authentication_service return null");
        }
        return IAuthenticationService.Stub.asInterface(b);
    }
}
