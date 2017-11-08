package com.loc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import cn.com.xy.sms.sdk.net.NetUtil;
import com.amap.api.fence.Fence;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.APSService;
import com.amap.api.location.LocationManagerBase;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.autonavi.aps.amapapi.model.AmapLoc;
import java.util.ArrayList;
import java.util.Iterator;

/* compiled from: AMapLocationManager */
public class a implements LocationManagerBase {
    static boolean t = false;
    private ServiceConnection A = new b(this);
    private ArrayList<a> B = new ArrayList();
    private int C = 0;
    private AMapLocation D = null;
    AMapLocationClientOption a;
    c b;
    g c = null;
    ArrayList<AMapLocationListener> d = new ArrayList();
    f e;
    boolean f = false;
    i g;
    Messenger h = null;
    Messenger i = null;
    b j;
    Intent k = null;
    int l = 0;
    int m = 0;
    boolean n = false;
    long o = 0;
    long p = 0;
    AMapLocation q = null;
    long r = 0;
    long s = 0;
    private Context u;
    private boolean v = false;
    private boolean w = true;
    private long x = 0;
    private boolean y = true;
    private boolean z = false;

    /* compiled from: AMapLocationManager */
    class a {
        double a;
        double b;
        long c;
        float d;
        float e;
        int f;
        String g;
        final /* synthetic */ a h;

        a(a aVar, AMapLocation aMapLocation, int i) {
            this.h = aVar;
            this.a = aMapLocation.getLatitude();
            this.b = aMapLocation.getLongitude();
            this.c = aMapLocation.getTime();
            this.d = aMapLocation.getAccuracy();
            this.e = aMapLocation.getSpeed();
            this.f = i;
            this.g = aMapLocation.getProvider();
        }

        public boolean equals(Object obj) {
            boolean z = false;
            try {
                a aVar = (a) obj;
                if (this.a == aVar.a && this.b == aVar.b) {
                    z = true;
                }
                return z;
            } catch (Throwable th) {
                return false;
            }
        }

        public String toString() {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(this.a);
            stringBuffer.append(",");
            stringBuffer.append(this.b);
            stringBuffer.append(",");
            stringBuffer.append(this.d);
            stringBuffer.append(",");
            stringBuffer.append(this.c);
            stringBuffer.append(",");
            stringBuffer.append(this.e);
            stringBuffer.append(",");
            stringBuffer.append(this.f);
            stringBuffer.append(",");
            stringBuffer.append(this.g);
            return stringBuffer.toString();
        }
    }

    /* compiled from: AMapLocationManager */
    class b extends Thread {
        boolean a = false;
        final /* synthetic */ a b;

        public b(a aVar, String str) {
            this.b = aVar;
            super(str);
        }

        public void run() {
            this.a = true;
            while (this.a) {
                try {
                    if (Thread.interrupted()) {
                        break;
                    } else if (AMapLocationMode.Device_Sensors.equals(this.b.a.getLocationMode())) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        Message obtain;
                        Bundle bundle;
                        if (AMapLocationMode.Hight_Accuracy.equals(this.b.a.getLocationMode())) {
                            if (this.b.d()) {
                                if (this.b.a.isGpsFirst() && this.b.a.isOnceLocation() && !this.b.e()) {
                                    if (this.b.n) {
                                    }
                                }
                            }
                            try {
                                if (this.b.a.isOnceLocation()) {
                                    if (this.b.p == 0) {
                                        this.b.p = cw.b();
                                    }
                                }
                                Thread.sleep(2000);
                            } catch (InterruptedException e2) {
                                Thread.currentThread().interrupt();
                            }
                        }
                        while (this.b.h == null) {
                            try {
                                a aVar = this.b;
                                aVar.l++;
                                if (this.b.l <= 100) {
                                    Thread.sleep(50);
                                } else {
                                    obtain = Message.obtain();
                                    bundle = new Bundle();
                                    Parcelable amapLoc = new AmapLoc();
                                    amapLoc.b(10);
                                    amapLoc.b("请检查配置文件是否配置服务");
                                    bundle.putParcelable(NetUtil.REQ_QUERY_LOCATION, amapLoc);
                                    obtain.setData(bundle);
                                    obtain.what = 1;
                                    if (this.b.b != null) {
                                        this.b.b.sendMessage(obtain);
                                    }
                                    this.b.p = 0;
                                    this.b.v = true;
                                    obtain = Message.obtain();
                                    obtain.what = 1;
                                    bundle = new Bundle();
                                    bundle.putBoolean("isfirst", this.b.y);
                                    bundle.putBoolean("wifiactivescan", this.b.a.isWifiActiveScan());
                                    bundle.putBoolean("isNeedAddress", this.b.a.isNeedAddress());
                                    bundle.putBoolean("isKillProcess", this.b.a.isKillProcess());
                                    bundle.putBoolean("isOffset", this.b.a.isOffset());
                                    bundle.putLong("httptimeout", this.b.a.getHttpTimeOut());
                                    obtain.setData(bundle);
                                    obtain.replyTo = this.b.i;
                                    if (this.b.h != null) {
                                        this.b.h.send(obtain);
                                    }
                                    this.b.y = false;
                                    Thread.sleep(this.b.a.getInterval());
                                }
                            } catch (InterruptedException e3) {
                                Thread.currentThread().interrupt();
                            }
                        }
                        this.b.p = 0;
                        this.b.v = true;
                        obtain = Message.obtain();
                        obtain.what = 1;
                        bundle = new Bundle();
                        bundle.putBoolean("isfirst", this.b.y);
                        bundle.putBoolean("wifiactivescan", this.b.a.isWifiActiveScan());
                        bundle.putBoolean("isNeedAddress", this.b.a.isNeedAddress());
                        bundle.putBoolean("isKillProcess", this.b.a.isKillProcess());
                        bundle.putBoolean("isOffset", this.b.a.isOffset());
                        bundle.putLong("httptimeout", this.b.a.getHttpTimeOut());
                        obtain.setData(bundle);
                        obtain.replyTo = this.b.i;
                        try {
                            if (this.b.h != null) {
                                this.b.h.send(obtain);
                            }
                        } catch (Throwable th) {
                            e.a(th, "AMapLocationManager", "run part4");
                        }
                        this.b.y = false;
                        Thread.sleep(this.b.a.getInterval());
                    }
                } catch (InterruptedException e4) {
                    Thread.currentThread().interrupt();
                } catch (Throwable th2) {
                    e.a(th2, "AMapLocationManager", "run part6");
                }
            }
            this.b.v = false;
        }
    }

    /* compiled from: AMapLocationManager */
    public static class c extends Handler {
        a a = null;

        public c(a aVar) {
            this.a = aVar;
        }

        public c(a aVar, Looper looper) {
            super(looper);
            this.a = aVar;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message message) {
            AMapLocation aMapLocation;
            Throwable th;
            Throwable th2;
            a a;
            StringBuffer stringBuffer;
            Iterator it;
            Iterator it2;
            AMapLocation aMapLocation2 = null;
            super.handleMessage(message);
            Message obtain;
            a aVar;
            switch (message.what) {
                case 1:
                    try {
                        Bundle data = message.getData();
                        if (data == null) {
                            aMapLocation = null;
                        } else {
                            data.setClassLoader(AmapLoc.class.getClassLoader());
                            aMapLocation = e.a((AmapLoc) data.getParcelable(NetUtil.REQ_QUERY_LOCATION));
                            try {
                                aMapLocation.setProvider("lbs");
                            } catch (Throwable th3) {
                                th = th3;
                                aMapLocation2 = aMapLocation;
                                th2 = th;
                                e.a(th2, "AMapLocationManager", "handleMessage LBS_LOCATIONSUCCESS");
                                if (this.a.e != null) {
                                    this.a.e.a(aMapLocation2);
                                }
                                if (aMapLocation2 != null) {
                                    try {
                                        if (!this.a.w) {
                                            if (aMapLocation2.getErrorCode() == 0) {
                                                aMapLocation2 = this.a.a(this.a.q, aMapLocation2);
                                                if (this.a.C != 0) {
                                                    a = this.a.a(this.a.D, this.a.C);
                                                    this.a.B.add(a);
                                                    break;
                                                }
                                                a = this.a.a(aMapLocation2, this.a.C);
                                                if (this.a.B.size() > 1) {
                                                    this.a.B.add(a);
                                                } else if (this.a.B.size() != 1) {
                                                    this.a.B.add(a);
                                                } else {
                                                    this.a.B.set(0, a);
                                                }
                                                if (this.a.B.size() >= 10) {
                                                    stringBuffer = new StringBuffer();
                                                    it = this.a.B.iterator();
                                                    while (it.hasNext()) {
                                                        stringBuffer.append(((a) it.next()).toString());
                                                        stringBuffer.append("#");
                                                    }
                                                    stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                                                    e.b(stringBuffer.toString());
                                                    this.a.B.clear();
                                                }
                                            }
                                            this.a.r = cw.b();
                                            this.a.q = aMapLocation2;
                                            if (!GeocodeSearch.GPS.equals(aMapLocation2.getProvider())) {
                                                if (!this.a.d()) {
                                                }
                                            }
                                            if (this.a.g != null) {
                                                this.a.g.a(aMapLocation2);
                                            }
                                            it2 = this.a.d.iterator();
                                            while (it2.hasNext()) {
                                                ((AMapLocationListener) it2.next()).onLocationChanged(aMapLocation2);
                                            }
                                        }
                                    } catch (Throwable th22) {
                                        e.a(th22, "AMapLocationManager", "handleMessage part7");
                                    }
                                }
                                if (this.a.a.isOnceLocation()) {
                                    this.a.stopLocation();
                                }
                            }
                        }
                    } catch (Throwable th4) {
                        th22 = th4;
                        e.a(th22, "AMapLocationManager", "handleMessage LBS_LOCATIONSUCCESS");
                        if (this.a.e != null) {
                            this.a.e.a(aMapLocation2);
                        }
                        if (aMapLocation2 != null) {
                            if (this.a.w) {
                                if (aMapLocation2.getErrorCode() == 0) {
                                    aMapLocation2 = this.a.a(this.a.q, aMapLocation2);
                                    if (this.a.C != 0) {
                                        a = this.a.a(aMapLocation2, this.a.C);
                                        if (this.a.B.size() > 1) {
                                            this.a.B.add(a);
                                        } else if (this.a.B.size() != 1) {
                                            this.a.B.set(0, a);
                                        } else {
                                            this.a.B.add(a);
                                        }
                                    } else {
                                        a = this.a.a(this.a.D, this.a.C);
                                        this.a.B.add(a);
                                    }
                                    if (this.a.B.size() >= 10) {
                                        stringBuffer = new StringBuffer();
                                        it = this.a.B.iterator();
                                        while (it.hasNext()) {
                                            stringBuffer.append(((a) it.next()).toString());
                                            stringBuffer.append("#");
                                        }
                                        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                                        e.b(stringBuffer.toString());
                                        this.a.B.clear();
                                    }
                                }
                                this.a.r = cw.b();
                                this.a.q = aMapLocation2;
                                if (GeocodeSearch.GPS.equals(aMapLocation2.getProvider())) {
                                    if (this.a.d()) {
                                    }
                                }
                                if (this.a.g != null) {
                                    this.a.g.a(aMapLocation2);
                                }
                                it2 = this.a.d.iterator();
                                while (it2.hasNext()) {
                                    ((AMapLocationListener) it2.next()).onLocationChanged(aMapLocation2);
                                }
                            }
                        }
                        if (this.a.a.isOnceLocation()) {
                            this.a.stopLocation();
                        }
                    }
                case 2:
                    try {
                        aMapLocation = (AMapLocation) message.obj;
                        try {
                            if (aMapLocation.getErrorCode() == 0) {
                                this.a.o = cw.b();
                                this.a.n = true;
                            }
                            if (!(a.t || this.a.h == null)) {
                                Message obtain2 = Message.obtain();
                                obtain2.what = 0;
                                obtain2.replyTo = this.a.i;
                                this.a.h.send(obtain2);
                                a.t = true;
                            }
                        } catch (Throwable th32) {
                            th = th32;
                            aMapLocation2 = aMapLocation;
                            th22 = th;
                            e.a(th22, "AMapLocationManager", "handleMessage GPS_LOCATIONSUCCESS");
                            if (this.a.e != null) {
                                this.a.e.a(aMapLocation2);
                            }
                            if (aMapLocation2 != null) {
                                if (this.a.w) {
                                    if (aMapLocation2.getErrorCode() == 0) {
                                        aMapLocation2 = this.a.a(this.a.q, aMapLocation2);
                                        if (this.a.C != 0) {
                                            a = this.a.a(this.a.D, this.a.C);
                                            this.a.B.add(a);
                                            break;
                                        }
                                        a = this.a.a(aMapLocation2, this.a.C);
                                        if (this.a.B.size() > 1) {
                                            this.a.B.add(a);
                                        } else if (this.a.B.size() != 1) {
                                            this.a.B.add(a);
                                        } else {
                                            this.a.B.set(0, a);
                                        }
                                        if (this.a.B.size() >= 10) {
                                            stringBuffer = new StringBuffer();
                                            it = this.a.B.iterator();
                                            while (it.hasNext()) {
                                                stringBuffer.append(((a) it.next()).toString());
                                                stringBuffer.append("#");
                                            }
                                            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                                            e.b(stringBuffer.toString());
                                            this.a.B.clear();
                                        }
                                    }
                                    this.a.r = cw.b();
                                    this.a.q = aMapLocation2;
                                    if (GeocodeSearch.GPS.equals(aMapLocation2.getProvider())) {
                                        if (this.a.d()) {
                                        }
                                    }
                                    if (this.a.g != null) {
                                        this.a.g.a(aMapLocation2);
                                    }
                                    it2 = this.a.d.iterator();
                                    while (it2.hasNext()) {
                                        ((AMapLocationListener) it2.next()).onLocationChanged(aMapLocation2);
                                    }
                                }
                            }
                            if (this.a.a.isOnceLocation()) {
                                this.a.stopLocation();
                            }
                        }
                    } catch (Throwable th5) {
                        th22 = th5;
                        e.a(th22, "AMapLocationManager", "handleMessage GPS_LOCATIONSUCCESS");
                        if (this.a.e != null) {
                            this.a.e.a(aMapLocation2);
                        }
                        if (aMapLocation2 != null) {
                            if (this.a.w) {
                                if (aMapLocation2.getErrorCode() == 0) {
                                    aMapLocation2 = this.a.a(this.a.q, aMapLocation2);
                                    if (this.a.C != 0) {
                                        a = this.a.a(aMapLocation2, this.a.C);
                                        if (this.a.B.size() > 1) {
                                            this.a.B.add(a);
                                        } else if (this.a.B.size() != 1) {
                                            this.a.B.set(0, a);
                                        } else {
                                            this.a.B.add(a);
                                        }
                                    } else {
                                        a = this.a.a(this.a.D, this.a.C);
                                        this.a.B.add(a);
                                    }
                                    if (this.a.B.size() >= 10) {
                                        stringBuffer = new StringBuffer();
                                        it = this.a.B.iterator();
                                        while (it.hasNext()) {
                                            stringBuffer.append(((a) it.next()).toString());
                                            stringBuffer.append("#");
                                        }
                                        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                                        e.b(stringBuffer.toString());
                                        this.a.B.clear();
                                    }
                                }
                                this.a.r = cw.b();
                                this.a.q = aMapLocation2;
                                if (GeocodeSearch.GPS.equals(aMapLocation2.getProvider())) {
                                    if (this.a.d()) {
                                    }
                                }
                                if (this.a.g != null) {
                                    this.a.g.a(aMapLocation2);
                                }
                                it2 = this.a.d.iterator();
                                while (it2.hasNext()) {
                                    ((AMapLocationListener) it2.next()).onLocationChanged(aMapLocation2);
                                }
                            }
                        }
                        if (this.a.a.isOnceLocation()) {
                            this.a.stopLocation();
                        }
                    }
                case 5:
                    this.a.o = cw.b();
                    this.a.n = true;
                    return;
                case 6:
                    return;
                case 100:
                    try {
                        this.a.f();
                    } catch (Throwable th222) {
                        e.a(th222, "AMapLocationManager", "handleMessage FASTSKY");
                    }
                    return;
                case 101:
                    try {
                        obtain = Message.obtain();
                        obtain.what = 2;
                        if (this.a.h == null) {
                            aVar = this.a;
                            aVar.m++;
                            if (this.a.m < 10) {
                                this.a.b.sendEmptyMessageDelayed(101, 50);
                            }
                        } else {
                            this.a.m = 0;
                            this.a.h.send(obtain);
                        }
                    } catch (Throwable th2222) {
                        e.a(th2222, "AMapLocationManager", "handleMessage START_SOCKET");
                    }
                    return;
                case 102:
                    try {
                        obtain = Message.obtain();
                        obtain.what = 3;
                        if (this.a.h == null) {
                            aVar = this.a;
                            aVar.m++;
                            if (this.a.m < 10) {
                                this.a.b.sendEmptyMessageDelayed(102, 50);
                            }
                        } else {
                            this.a.m = 0;
                            this.a.h.send(obtain);
                        }
                    } catch (Throwable th22222) {
                        e.a(th22222, "AMapLocationManager", "handleMessage STOP_SOCKET");
                    }
                    return;
                default:
                    return;
            }
            try {
                if (this.a.e != null) {
                    this.a.e.a(aMapLocation2);
                }
            } catch (Throwable th222222) {
                e.a(th222222, "AMapLocationManager", "handleMessage part6");
            }
            if (aMapLocation2 != null) {
                if (this.a.w) {
                    if (aMapLocation2.getErrorCode() == 0) {
                        aMapLocation2 = this.a.a(this.a.q, aMapLocation2);
                        if (this.a.C != 0) {
                            a = this.a.a(this.a.D, this.a.C);
                            if (!this.a.B.contains(a) && this.a.B.size() <= 9) {
                                this.a.B.add(a);
                            }
                        } else {
                            a = this.a.a(aMapLocation2, this.a.C);
                            if (this.a.B.size() > 1) {
                                this.a.B.add(a);
                            } else if (this.a.B.size() != 1) {
                                this.a.B.add(a);
                            } else {
                                this.a.B.set(0, a);
                            }
                        }
                        if (this.a.B.size() >= 10) {
                            stringBuffer = new StringBuffer();
                            it = this.a.B.iterator();
                            while (it.hasNext()) {
                                stringBuffer.append(((a) it.next()).toString());
                                stringBuffer.append("#");
                            }
                            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                            e.b(stringBuffer.toString());
                            this.a.B.clear();
                        }
                    }
                    this.a.r = cw.b();
                    this.a.q = aMapLocation2;
                    if (GeocodeSearch.GPS.equals(aMapLocation2.getProvider())) {
                        if (this.a.d()) {
                        }
                    }
                    if (this.a.g != null) {
                        this.a.g.a(aMapLocation2);
                    }
                    it2 = this.a.d.iterator();
                    while (it2.hasNext()) {
                        ((AMapLocationListener) it2.next()).onLocationChanged(aMapLocation2);
                    }
                }
            }
            try {
                if (this.a.a.isOnceLocation()) {
                    this.a.stopLocation();
                }
            } catch (Throwable th2222222) {
                e.a(th2222222, "AMapLocationManager", "handleMessage part8");
            }
        }
    }

    public a(Context context, Intent intent) {
        this.u = context;
        this.k = intent;
        b();
    }

    private AMapLocation a(AMapLocation aMapLocation, AMapLocation aMapLocation2) {
        this.D = aMapLocation2;
        long b = cw.b();
        if (aMapLocation == null) {
            this.s = 0;
            this.C = 0;
            return aMapLocation2;
        } else if (aMapLocation.getLocationType() != 1) {
            this.s = 0;
            this.C = 0;
            return aMapLocation2;
        } else if (aMapLocation2.getLocationType() != 1) {
            this.s = 0;
            this.C = 0;
            return aMapLocation2;
        } else {
            if ((b - this.r >= 5000 ? 1 : 0) == 0) {
                if (cw.a(new double[]{aMapLocation.getLatitude(), aMapLocation.getLongitude(), aMapLocation2.getLatitude(), aMapLocation2.getLongitude()}) > ((((aMapLocation.getSpeed() + aMapLocation2.getSpeed()) * ((float) (aMapLocation2.getTime() - aMapLocation.getTime()))) / 2000.0f) + ((aMapLocation.getAccuracy() + aMapLocation2.getAccuracy()) * 2.0f)) + 3000.0f) {
                    if (this.s == 0) {
                        this.s = cw.b();
                    }
                    if ((b - this.s >= 30000 ? 1 : 0) == 0) {
                        this.C = 1;
                        return aMapLocation;
                    }
                    this.s = 0;
                    this.C = 0;
                    return aMapLocation2;
                }
                this.s = 0;
                this.C = 0;
                return aMapLocation2;
            }
            this.s = 0;
            this.C = 0;
            return aMapLocation2;
        }
    }

    private a a(AMapLocation aMapLocation, int i) {
        return new a(this, aMapLocation, i);
    }

    private void a(Intent intent) {
        if (intent == null) {
            intent = new Intent(this.u, APSService.class);
        }
        try {
            intent.putExtra("apiKey", e.a);
            String e = m.e(this.u);
            intent.putExtra("packageName", this.u.getPackageName());
            intent.putExtra("sha1AndPackage", e);
            this.u.bindService(intent, this.A, 1);
        } catch (Throwable th) {
            e.a(th, "AMapLocationManager", "startService");
        }
    }

    private void b() {
        a(this.k);
        this.g = i.a(this.u);
        this.b = Looper.myLooper() != null ? new c(this) : new c(this, this.u.getMainLooper());
        this.i = new Messenger(this.b);
        this.c = new g(this.u, this.b);
        try {
            this.e = new f(this.u);
        } catch (Throwable th) {
            e.a(th, "AMapLocationManager", "init");
        }
    }

    private void c() {
        if (this.j == null) {
            this.j = new b(this, "locationThread");
            this.j.start();
        }
    }

    private boolean d() {
        return !(((cw.b() - this.o) > 10000 ? 1 : ((cw.b() - this.o) == 10000 ? 0 : -1)) <= 0);
    }

    private boolean e() {
        long b = cw.b();
        if (this.p == 0) {
            return false;
        }
        return !(((b - this.p) > 30000 ? 1 : ((b - this.p) == 30000 ? 0 : -1)) <= 0);
    }

    private void f() {
        Object obj = 1;
        Object obj2 = null;
        try {
            if (this.u.checkCallingOrSelfPermission("android.permission.SYSTEM_ALERT_WINDOW") == 0) {
                int i = 1;
            } else if (!(this.u instanceof Activity)) {
                obj = null;
            }
            if (obj == null) {
                g();
                return;
            }
            Builder builder = new Builder(this.u);
            builder.setMessage(ct.j());
            if (!"".equals(ct.k())) {
                if (ct.k() != null) {
                    builder.setPositiveButton(ct.k(), new OnClickListener(this) {
                        final /* synthetic */ a a;

                        {
                            this.a = r1;
                        }

                        public void onClick(DialogInterface dialogInterface, int i) {
                            this.a.g();
                            dialogInterface.cancel();
                        }
                    });
                }
            }
            builder.setNegativeButton(ct.l(), new OnClickListener(this) {
                final /* synthetic */ a a;

                {
                    this.a = r1;
                }

                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            AlertDialog create = builder.create();
            if (obj2 != null) {
                create.getWindow().setType(AMapException.CODE_AMAP_ENGINE_TABLEID_NOT_EXIST);
            }
            create.setCanceledOnTouchOutside(false);
            create.show();
        } catch (Throwable th) {
            g();
            e.a(th, "AMapLocationManager", "showDialog");
        }
    }

    private void g() {
        Intent intent;
        try {
            intent = new Intent();
            intent.setComponent(new ComponentName("com.autonavi.minimap", ct.o()));
            intent.setFlags(268435456);
            intent.setData(Uri.parse(ct.m()));
            this.u.startActivity(intent);
        } catch (Throwable th) {
            e.a(th, "AMapLocationManager", "callAMap part2");
        }
    }

    void a() {
        if (this.j != null) {
            this.j.a = false;
            this.j.interrupt();
        }
        this.j = null;
    }

    public void addGeoFenceAlert(String str, double d, double d2, float f, long j, PendingIntent pendingIntent) {
        Fence fence = new Fence();
        fence.b = str;
        fence.d = d;
        fence.c = d2;
        fence.e = f;
        fence.a = pendingIntent;
        fence.a(j);
        if (this.e != null) {
            this.e.a(fence, fence.a);
        }
    }

    public AMapLocation getLastKnownLocation() {
        try {
            return this.g.a();
        } catch (Throwable th) {
            e.a(th, "AMapLocationManager", "getLastKnownLocation");
            return null;
        }
    }

    public String getVersion() {
        return "2.4.0";
    }

    public boolean isStarted() {
        return this.v;
    }

    public void onDestroy() {
        this.y = true;
        t = false;
        stopLocation();
        if (this.e != null) {
            this.e.a();
        }
        if (this.A != null) {
            this.u.unbindService(this.A);
        }
        if (this.d != null) {
            this.d.clear();
            this.d = null;
        }
        this.A = null;
        if (this.b != null) {
            this.b.removeCallbacksAndMessages(null);
        }
    }

    public void removeGeoFenceAlert(PendingIntent pendingIntent) {
        if (this.e != null) {
            this.e.a(pendingIntent);
        }
    }

    public void removeGeoFenceAlert(PendingIntent pendingIntent, String str) {
        if (this.e != null) {
            this.e.a(pendingIntent, str);
        }
    }

    public void setLocationListener(AMapLocationListener aMapLocationListener) {
        if (aMapLocationListener != null) {
            if (this.d == null) {
                this.d = new ArrayList();
            }
            if (!this.d.contains(aMapLocationListener)) {
                this.d.add(aMapLocationListener);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("listener参数不能为null");
    }

    public void setLocationOption(AMapLocationClientOption aMapLocationClientOption) {
        this.a = aMapLocationClientOption;
    }

    public void startAssistantLocation() {
        if (this.b != null) {
            this.b.sendEmptyMessage(101);
        }
    }

    public void startLocation() {
        if (this.a == null) {
            this.a = new AMapLocationClientOption();
        }
        this.w = false;
        c();
        switch (c.a[this.a.getLocationMode().ordinal()]) {
            case 1:
                this.c.a();
                this.z = false;
                return;
            case 2:
            case 3:
                if (!this.z) {
                    this.c.a(this.a);
                    this.z = true;
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void stopAssistantLocation() {
        if (this.b != null) {
            this.b.sendEmptyMessage(102);
        }
    }

    public void stopLocation() {
        a();
        this.c.a();
        this.z = false;
        this.n = false;
        this.v = false;
        this.w = true;
        this.p = 0;
        this.o = 0;
        this.m = 0;
        this.l = 0;
        this.q = null;
        this.r = 0;
        this.B.clear();
        this.C = 0;
        this.D = null;
    }

    public void unRegisterLocationListener(AMapLocationListener aMapLocationListener) {
        if (!this.d.isEmpty() && this.d.contains(aMapLocationListener)) {
            this.d.remove(aMapLocationListener);
        }
        if (this.d.isEmpty()) {
            stopLocation();
        }
    }
}
