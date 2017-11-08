package com.amap.api.location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Messenger;
import android.text.TextUtils;
import com.loc.ay;
import com.loc.d;
import com.loc.e;
import com.loc.m;
import com.loc.n;

public class APSService extends Service {
    Messenger a;
    APSServiceBase b;

    public IBinder onBind(Intent intent) {
        try {
            String stringExtra = intent.getStringExtra("apiKey");
            if (!TextUtils.isEmpty(stringExtra)) {
                n.a(stringExtra);
            }
            stringExtra = intent.getStringExtra("packageName");
            String stringExtra2 = intent.getStringExtra("sha1AndPackage");
            m.a(stringExtra);
            m.b(stringExtra2);
            this.a = new Messenger(this.b.getHandler());
            return this.a.getBinder();
        } catch (Throwable th) {
            e.a(th, "APSService", "onBind");
            return null;
        }
    }

    public void onCreate() {
        onCreate(this);
    }

    public void onCreate(Context context) {
        try {
            Context context2 = context;
            this.b = (APSServiceBase) ay.a(context2, e.a("2.4.0"), "com.amap.api.location.APSServiceWrapper", d.class, new Class[]{Context.class}, new Object[]{context});
        } catch (Throwable th) {
            this.b = new d(this);
            e.a(th, "APSService", "onCreate");
        }
        this.b.onCreate();
        super.onCreate();
    }

    public void onDestroy() {
        try {
            this.b.onDestroy();
        } catch (Throwable th) {
            e.a(th, "APSService", "onDestroy");
        }
        super.onDestroy();
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        try {
            return this.b.onStartCommand(intent, i, i2);
        } catch (Throwable th) {
            e.a(th, "APSService", "onStartCommand");
            return super.onStartCommand(intent, i, i2);
        }
    }
}
