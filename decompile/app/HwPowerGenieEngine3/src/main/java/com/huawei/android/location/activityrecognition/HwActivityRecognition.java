package com.huawei.android.location.activityrecognition;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.location.activityrecognition.IActivityRecognitionHardwareService.Stub;
import com.huawei.powergenie.integration.adapter.NativeAdapter;

public class HwActivityRecognition {
    private ServiceDeathHandler deathHandler;
    private int mConnectCount = 0;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName arg0, IBinder service) {
            Log.d("HwActivityRecognition", "Connection service ok");
            HwActivityRecognition.this.mHandler.removeMessages(1);
            HwActivityRecognition.this.mService = Stub.asInterface(service);
            HwActivityRecognition.this.registerSink();
            HwActivityRecognition.this.notifyServiceDied();
            HwActivityRecognition.this.mHandler.sendEmptyMessage(0);
        }

        public void onServiceDisconnected(ComponentName name) {
            HwActivityRecognition.this.mService = null;
            HwActivityRecognition.this.mServiceConnection.onServiceDisconnected();
        }
    };
    private Context mContext = null;
    private boolean mDebug = true;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NativeAdapter.PLATFORM_QCOM /*0*/:
                    HwActivityRecognition.this.handleProviderLoad();
                    return;
                case NativeAdapter.PLATFORM_MTK /*1*/:
                    if (HwActivityRecognition.this.mConnectCount <= 20) {
                        HwActivityRecognition.this.bindService();
                        return;
                    } else {
                        Log.d("HwActivityRecognition", "try connect 20 times, connection fail");
                        return;
                    }
                default:
                    return;
            }
        }
    };
    private IActivityRecognitionHardwareService mService = null;
    private HwActivityRecognitionServiceConnection mServiceConnection = null;
    private IActivityRecognitionHardwareSink mSink;

    private class ServiceDeathHandler implements DeathRecipient {
        private ServiceDeathHandler() {
        }

        public void binderDied() {
            Log.d("HwActivityRecognition", "Ar service has died!");
            HwActivityRecognition.this.mServiceConnection.onServiceDisconnected();
            if (HwActivityRecognition.this.mService != null) {
                HwActivityRecognition.this.mService.asBinder().unlinkToDeath(HwActivityRecognition.this.deathHandler, 0);
                HwActivityRecognition.this.mService = null;
            }
        }
    }

    public HwActivityRecognition(Context context) {
        if (this.mDebug) {
            Log.d("HwActivityRecognition", "HwActivityRecognition");
        }
        this.mContext = context;
    }

    private void handleProviderLoad() {
        try {
            if (this.mService != null) {
                if (this.mService.providerLoadOk()) {
                    this.mHandler.removeMessages(0);
                    this.mServiceConnection.onServiceConnected();
                    return;
                }
                this.mHandler.sendEmptyMessageDelayed(0, 500);
            }
        } catch (RemoteException e) {
            Log.e("HwActivityRecognition", "providerLoadOk fail");
        }
    }

    private void notifyServiceDied() {
        this.deathHandler = new ServiceDeathHandler();
        try {
            if (this.mService != null) {
                this.mService.asBinder().linkToDeath(this.deathHandler, 0);
            }
        } catch (RemoteException e) {
            Log.e("HwActivityRecognition", "IBinder register linkToDeath function fail.");
        }
    }

    private void bindService() {
        if (this.mService == null) {
            Log.d("HwActivityRecognition", new StringBuilder(String.valueOf(this.mContext.getPackageName())).append(" bind ar service.").toString());
            Intent bindIntent = new Intent();
            bindIntent.setClassName("com.huawei.android.location.activityrecognition", "com.huawei.android.location.activityrecognition.ActivityRecognitionService");
            this.mContext.bindService(bindIntent, this.mConnection, 1);
            this.mConnectCount++;
            this.mHandler.sendEmptyMessageDelayed(1, 2000);
        }
    }

    public boolean connectService(HwActivityRecognitionHardwareSink sink, HwActivityRecognitionServiceConnection connection) {
        if (this.mDebug) {
            Log.d("HwActivityRecognition", "connectService");
        }
        if (connection == null || sink == null) {
            Log.e("HwActivityRecognition", "connection or sink is null.");
            return false;
        }
        if (this.mService == null) {
            this.mServiceConnection = connection;
            this.mSink = createActivityRecognitionHardwareSink(sink);
            bindService();
        }
        return true;
    }

    private boolean registerSink() {
        boolean result = false;
        if (this.mDebug) {
            Log.d("HwActivityRecognition", "registerSink");
        }
        if (this.mService == null || this.mSink == null) {
            return false;
        }
        try {
            result = this.mService.registerSink(this.mSink);
        } catch (RemoteException e) {
            Log.e("HwActivityRecognition", "registerSink error:" + e.getMessage());
        }
        return result;
    }

    public boolean enableActivityEvent(String activity, int eventType, long reportLatencyNs) {
        boolean result = false;
        if (this.mDebug) {
            Log.d("HwActivityRecognition", "enableActivityEvent");
        }
        if (this.mService == null) {
            return false;
        }
        Log.d("HwActivityRecognition", new StringBuilder(String.valueOf(activity)).append(",").append(eventType).append(",").append(reportLatencyNs).toString());
        try {
            result = this.mService.enableActivityEvent(activity, eventType, reportLatencyNs);
        } catch (RemoteException e) {
            Log.e("HwActivityRecognition", "enableActivityEvent error:" + e.getMessage());
        }
        return result;
    }

    public boolean disableActivityEvent(String activity, int eventType) {
        boolean result = false;
        if (this.mDebug) {
            Log.d("HwActivityRecognition", "disableActivityEvent");
        }
        if (this.mService == null) {
            return false;
        }
        Log.d("HwActivityRecognition", new StringBuilder(String.valueOf(activity)).append(",").append(eventType).toString());
        try {
            result = this.mService.disableActivityEvent(activity, eventType);
        } catch (RemoteException e) {
            Log.e("HwActivityRecognition", "disableActivityEvent error:" + e.getMessage());
        }
        return result;
    }

    private IActivityRecognitionHardwareSink createActivityRecognitionHardwareSink(final HwActivityRecognitionHardwareSink sink) {
        if (sink != null) {
            return new IActivityRecognitionHardwareSink.Stub() {
                public void onActivityChanged(HwActivityChangedEvent event) throws RemoteException {
                    sink.onActivityChanged(event);
                }
            };
        }
        return null;
    }

    public String[] getSupportedActivities() {
        if (this.mDebug) {
            Log.d("HwActivityRecognition", "getSupportedActivities");
        }
        if (this.mService == null) {
            return new String[0];
        }
        try {
            return this.mService.getSupportedActivities();
        } catch (RemoteException e) {
            Log.e("HwActivityRecognition", "getSupportedActivities error:" + e.getMessage());
            return new String[0];
        }
    }
}
