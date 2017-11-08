package com.loc;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;

/* compiled from: AMapLocationManager */
class b implements ServiceConnection {
    final /* synthetic */ a a;

    b(a aVar) {
        this.a = aVar;
    }

    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        this.a.h = new Messenger(iBinder);
    }

    public void onServiceDisconnected(ComponentName componentName) {
        this.a.h = null;
    }
}
