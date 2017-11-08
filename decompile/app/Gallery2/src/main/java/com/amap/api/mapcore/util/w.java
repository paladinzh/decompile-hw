package com.amap.api.mapcore.util;

import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;

/* compiled from: UiSettingsDelegateImp */
class w implements o {
    final Handler a = new Handler(this) {
        final /* synthetic */ w a;

        {
            this.a = r1;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message message) {
            if (message != null && this.a.b != null) {
                try {
                    switch (message.what) {
                        case 0:
                            this.a.b.a(this.a.h);
                            break;
                        case 1:
                            this.a.b.e(this.a.j);
                            break;
                        case 2:
                            this.a.b.d(this.a.i);
                            break;
                        case 3:
                            this.a.b.c(this.a.f);
                            break;
                        case 4:
                            this.a.b.b(this.a.m);
                            break;
                    }
                } catch (Throwable th) {
                    fo.b(th, "UiSettingsDelegateImp", "handleMessage");
                }
            }
        }
    };
    private l b;
    private boolean c = true;
    private boolean d = true;
    private boolean e = true;
    private boolean f = false;
    private boolean g = true;
    private boolean h = true;
    private boolean i = true;
    private boolean j = false;
    private int k = 0;
    private int l = 1;
    private boolean m = true;
    private boolean n = false;

    w(l lVar) {
        this.b = lVar;
    }

    public boolean isIndoorSwitchEnabled() throws RemoteException {
        return this.m;
    }

    public void setIndoorSwitchEnabled(boolean z) throws RemoteException {
        this.m = z;
        this.a.obtainMessage(4).sendToTarget();
    }

    public void setScaleControlsEnabled(boolean z) throws RemoteException {
        this.j = z;
        this.a.obtainMessage(1).sendToTarget();
    }

    public void setZoomControlsEnabled(boolean z) throws RemoteException {
        this.h = z;
        this.a.obtainMessage(0).sendToTarget();
    }

    public void setCompassEnabled(boolean z) throws RemoteException {
        this.i = z;
        this.a.obtainMessage(2).sendToTarget();
    }

    public void setMyLocationButtonEnabled(boolean z) throws RemoteException {
        this.f = z;
        this.a.obtainMessage(3).sendToTarget();
    }

    public void setScrollGesturesEnabled(boolean z) throws RemoteException {
        this.d = z;
    }

    public void setZoomGesturesEnabled(boolean z) throws RemoteException {
        this.g = z;
    }

    public void setTiltGesturesEnabled(boolean z) throws RemoteException {
        this.e = z;
    }

    public void setRotateGesturesEnabled(boolean z) throws RemoteException {
        this.c = z;
    }

    public void setAllGesturesEnabled(boolean z) throws RemoteException {
        setRotateGesturesEnabled(z);
        setTiltGesturesEnabled(z);
        setZoomGesturesEnabled(z);
        setScrollGesturesEnabled(z);
    }

    public void setLogoPosition(int i) throws RemoteException {
        this.k = i;
        this.b.b(i);
    }

    public void setZoomPosition(int i) throws RemoteException {
        this.l = i;
        this.b.f(i);
    }

    public boolean isScaleControlsEnabled() throws RemoteException {
        return this.j;
    }

    public boolean isZoomControlsEnabled() throws RemoteException {
        return this.h;
    }

    public boolean isCompassEnabled() throws RemoteException {
        return this.i;
    }

    public boolean isMyLocationButtonEnabled() throws RemoteException {
        return this.f;
    }

    public boolean isScrollGesturesEnabled() throws RemoteException {
        return this.d;
    }

    public boolean isZoomGesturesEnabled() throws RemoteException {
        return this.g;
    }

    public boolean isTiltGesturesEnabled() throws RemoteException {
        return this.e;
    }

    public boolean isRotateGesturesEnabled() throws RemoteException {
        return this.c;
    }

    public int getLogoPosition() throws RemoteException {
        return this.k;
    }

    public int getZoomPosition() throws RemoteException {
        return this.l;
    }

    public void setZoomInByScreenCenter(boolean z) {
        this.n = z;
    }

    public boolean isZoomInByScreenCenter() {
        return this.n;
    }

    public void setLogoBottomMargin(int i) {
        this.b.c(i);
    }

    public void setLogoLeftMargin(int i) {
        this.b.d(i);
    }

    public float getLogoMarginRate(int i) {
        return this.b.e(i);
    }

    public void setLogoMarginRate(int i, float f) {
        this.b.a(i, f);
    }
}
