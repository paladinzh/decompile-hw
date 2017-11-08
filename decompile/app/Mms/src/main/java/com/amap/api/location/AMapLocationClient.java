package com.amap.api.location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.amap.api.fence.Fence;
import com.loc.ay;
import com.loc.e;

public class AMapLocationClient implements LocationManagerBase {
    a a;
    Context b;
    LocationManagerBase c;
    AMapLocationClientOption d;
    AMapLocationListener e;
    AMapLocationClient f;

    static class a extends Handler {
        AMapLocationClient a = null;

        public a(AMapLocationClient aMapLocationClient) {
            this.a = aMapLocationClient;
        }

        public a(AMapLocationClient aMapLocationClient, Looper looper) {
            super(looper);
            this.a = aMapLocationClient;
        }

        public void handleMessage(Message message) {
            try {
                super.handleMessage(message);
                switch (message.arg1) {
                    case 1:
                        this.a.d = (AMapLocationClientOption) message.obj;
                        this.a.c.setLocationOption(this.a.d);
                        return;
                    case 2:
                        try {
                            this.a.e = (AMapLocationListener) message.obj;
                            this.a.c.setLocationListener(this.a.e);
                            return;
                        } catch (Throwable th) {
                            e.a(th, "AMapLocationClient", "handleMessage SET_LISTENER");
                            return;
                        }
                    case 3:
                        try {
                            this.a.c.startLocation();
                            return;
                        } catch (Throwable th2) {
                            e.a(th2, "AMapLocationClient", "handleMessage START_LOCATION");
                            return;
                        }
                    case 4:
                        try {
                            this.a.c.stopLocation();
                            return;
                        } catch (Throwable th22) {
                            e.a(th22, "AMapLocationClient", "handleMessage STOP_LOCATION");
                            return;
                        }
                    case 5:
                        try {
                            this.a.e = (AMapLocationListener) message.obj;
                            this.a.c.unRegisterLocationListener(this.a.e);
                            return;
                        } catch (Throwable th222) {
                            e.a(th222, "AMapLocationClient", "handleMessage REMOVE_LISTENER");
                            return;
                        }
                    case 6:
                        try {
                            Fence fence = (Fence) message.obj;
                            this.a.c.addGeoFenceAlert(fence.b, fence.d, fence.c, fence.e, fence.f, fence.a);
                            return;
                        } catch (Throwable th2222) {
                            e.a(th2222, "AMapLocationClient", "handleMessage ADD_GEOFENCE");
                            return;
                        }
                    case 7:
                        try {
                            this.a.c.removeGeoFenceAlert((PendingIntent) message.obj);
                            return;
                        } catch (Throwable th22222) {
                            e.a(th22222, "AMapLocationClient", "handleMessage REMOVE_GEOFENCE");
                            return;
                        }
                    case 8:
                        try {
                            this.a.c.startAssistantLocation();
                            return;
                        } catch (Throwable th222222) {
                            e.a(th222222, "AMapLocationClient", "handleMessage START_SOCKET");
                            return;
                        }
                    case 9:
                        try {
                            this.a.c.stopAssistantLocation();
                            return;
                        } catch (Throwable th2222222) {
                            e.a(th2222222, "AMapLocationClient", "handleMessage STOP_SOCKET");
                            return;
                        }
                    case 10:
                        try {
                            Fence fence2 = (Fence) message.obj;
                            this.a.c.removeGeoFenceAlert(fence2.a, fence2.b);
                            return;
                        } catch (Throwable th22222222) {
                            e.a(th22222222, "AMapLocationClient", "handleMessage REMOVE_GEOFENCE_ONE");
                            return;
                        }
                    case 11:
                        try {
                            this.a.c.onDestroy();
                            this.a.c = null;
                            this.a = null;
                            return;
                        } catch (Throwable th222222222) {
                            e.a(th222222222, "AMapLocationClient", "handleMessage DESTROY");
                            return;
                        }
                    default:
                        return;
                }
            } catch (Throwable th2222222222) {
                e.a(th2222222222, "AMapLocationClient", "handleMessage end");
            }
            e.a(th2222222222, "AMapLocationClient", "handleMessage end");
        }
    }

    public AMapLocationClient(Context context) {
        if (context != null) {
            try {
                this.b = context.getApplicationContext();
                this.f = new AMapLocationClient(this.b, null, true);
                if (Looper.myLooper() != null) {
                    this.a = new a(this.f);
                    return;
                } else {
                    this.a = new a(this.f, this.b.getMainLooper());
                    return;
                }
            } catch (Throwable th) {
                e.a(th, "AMapLocationClient", "AMapLocationClient 1");
                return;
            }
        }
        throw new IllegalArgumentException("Context参数不能为null");
    }

    public AMapLocationClient(Context context, Intent intent) {
        if (context != null) {
            try {
                this.b = context.getApplicationContext();
                this.f = new AMapLocationClient(this.b, intent, true);
                if (Looper.myLooper() != null) {
                    this.a = new a(this.f);
                    return;
                } else {
                    this.a = new a(this.f, this.b.getMainLooper());
                    return;
                }
            } catch (Throwable th) {
                e.a(th, "AMapLocationClient", "AMapLocationClient 2");
                return;
            }
        }
        throw new IllegalArgumentException("Context参数不能为null");
    }

    private AMapLocationClient(Context context, Intent intent, boolean z) {
        try {
            this.b = context;
            Context context2 = context;
            this.c = (LocationManagerBase) ay.a(context2, e.a("2.4.0"), "com.amap.api.location.LocationManagerWrapper", com.loc.a.class, new Class[]{Context.class, Intent.class}, new Object[]{context, intent});
        } catch (Throwable th) {
            this.c = new com.loc.a(context, intent);
            e.a(th, "AMapLocationClient", "AMapLocationClient 3");
        }
    }

    public static void setApiKey(String str) {
        try {
            e.a = str;
        } catch (Throwable th) {
            e.a(th, "AMapLocationClient", "setApiKey");
        }
    }

    public void addGeoFenceAlert(String str, double d, double d2, float f, long j, PendingIntent pendingIntent) {
        try {
            Message obtain = Message.obtain();
            Fence fence = new Fence();
            fence.b = str;
            fence.d = d;
            fence.c = d2;
            fence.e = f;
            fence.a = pendingIntent;
            fence.f = j;
            obtain.obj = fence;
            obtain.arg1 = 6;
            this.a.sendMessage(obtain);
        } catch (Throwable th) {
            e.a(th, "AMapLocationClient", "addGeoFenceAlert");
        }
    }

    public AMapLocation getLastKnownLocation() {
        try {
            if (!(this.f == null || this.f.c == null)) {
                return this.f.c.getLastKnownLocation();
            }
        } catch (Throwable th) {
            e.a(th, "AMapLocationClient", "getLastKnownLocation");
        }
        return null;
    }

    public String getVersion() {
        try {
            if (this.f != null) {
                return this.f.c.getVersion();
            }
        } catch (Throwable th) {
            e.a(th, "AMapLocationClient", "getVersion");
        }
        return null;
    }

    public boolean isStarted() {
        try {
            if (this.f != null) {
                return this.f.c.isStarted();
            }
        } catch (Throwable th) {
            e.a(th, "AMapLocationClient", "isStarted");
        }
        return false;
    }

    public void onDestroy() {
        try {
            Message obtain = Message.obtain();
            obtain.arg1 = 11;
            this.a.sendMessage(obtain);
        } catch (Throwable th) {
            e.a(th, "AMapLocationClient", "onDestroy");
        }
    }

    public void removeGeoFenceAlert(PendingIntent pendingIntent) {
        try {
            Message obtain = Message.obtain();
            obtain.obj = pendingIntent;
            obtain.arg1 = 7;
            this.a.sendMessage(obtain);
        } catch (Throwable th) {
            e.a(th, "AMapLocationClient", "removeGeoFenceAlert 2");
        }
    }

    public void removeGeoFenceAlert(PendingIntent pendingIntent, String str) {
        try {
            Message obtain = Message.obtain();
            Fence fence = new Fence();
            fence.b = str;
            fence.a = pendingIntent;
            obtain.obj = fence;
            obtain.arg1 = 10;
            this.a.sendMessage(obtain);
        } catch (Throwable th) {
            e.a(th, "AMapLocationClient", "removeGeoFenceAlert 1");
        }
    }

    public void setLocationListener(AMapLocationListener aMapLocationListener) {
        if (aMapLocationListener != null) {
            try {
                Message obtain = Message.obtain();
                obtain.arg1 = 2;
                obtain.obj = aMapLocationListener;
                this.a.sendMessage(obtain);
                return;
            } catch (Throwable th) {
                e.a(th, "AMapLocationClient", "setLocationListener");
                return;
            }
        }
        throw new IllegalArgumentException("listener参数不能为null");
    }

    public void setLocationOption(AMapLocationClientOption aMapLocationClientOption) {
        if (aMapLocationClientOption != null) {
            try {
                Message obtain = Message.obtain();
                obtain.arg1 = 1;
                obtain.obj = aMapLocationClientOption;
                this.a.sendMessage(obtain);
                return;
            } catch (Throwable th) {
                e.a(th, "AMapLocationClient", "setLocationOption");
                return;
            }
        }
        throw new IllegalArgumentException("LocationManagerOption参数不能为null");
    }

    public void startAssistantLocation() {
        try {
            Message obtain = Message.obtain();
            obtain.arg1 = 8;
            this.a.sendMessage(obtain);
        } catch (Throwable th) {
            e.a(th, "AMapLocationClient", "startAssistantLocation");
        }
    }

    public void startLocation() {
        try {
            Message obtain = Message.obtain();
            obtain.arg1 = 3;
            this.a.sendMessage(obtain);
        } catch (Throwable th) {
            e.a(th, "AMapLocationClient", "startLocation");
        }
    }

    public void stopAssistantLocation() {
        try {
            Message obtain = Message.obtain();
            obtain.arg1 = 9;
            this.a.sendMessage(obtain);
        } catch (Throwable th) {
            e.a(th, "AMapLocationClient", "stopAssistantLocation");
        }
    }

    public void stopLocation() {
        try {
            Message obtain = Message.obtain();
            obtain.arg1 = 4;
            this.a.sendMessage(obtain);
        } catch (Throwable th) {
            e.a(th, "AMapLocationClient", "stopLocation");
        }
    }

    public void unRegisterLocationListener(AMapLocationListener aMapLocationListener) {
        try {
            Message obtain = Message.obtain();
            obtain.arg1 = 5;
            obtain.obj = aMapLocationListener;
            this.a.sendMessage(obtain);
        } catch (Throwable th) {
            e.a(th, "AMapLocationClient", "unRegisterLocationListener");
        }
    }
}
