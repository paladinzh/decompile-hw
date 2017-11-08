package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Message;
import android.text.TextUtils;
import com.amap.api.mapcore.util.ez.a;
import com.amap.api.mapcore.util.ez.a.d;
import com.amap.api.maps.MapsInitializer;

/* compiled from: AuthTask */
public class f extends Thread {
    private Context a;
    private l b;

    public f(Context context, l lVar) {
        this.a = context;
        this.b = lVar;
    }

    public void run() {
        try {
            if (MapsInitializer.getNetWorkEnable()) {
                fh e = eh.e();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("002");
                stringBuilder.append(";");
                stringBuilder.append("11K");
                stringBuilder.append(";");
                stringBuilder.append("001");
                a a = ez.a(this.a, eh.e(), stringBuilder.toString(), null);
                if (ez.a != 1) {
                    Message obtainMessage = this.b.getMainHandler().obtainMessage();
                    obtainMessage.what = 2;
                    if (a.a != null) {
                        obtainMessage.obj = a.a;
                    }
                    this.b.getMainHandler().sendMessage(obtainMessage);
                }
                if (a != null) {
                    if (a.p != null) {
                        eh.e().a(a.p.a);
                    }
                    if (a.r != null) {
                        new fg(this.a, "3dmap", a.r.a, a.r.b).a();
                    }
                }
                if (!(a == null || a.q == null)) {
                    d dVar = a.q;
                    if (dVar == null) {
                        new gh(this.a, null, eh.e()).a();
                    } else {
                        Object obj = dVar.b;
                        Object obj2 = dVar.a;
                        Object obj3 = dVar.c;
                        if (TextUtils.isEmpty(obj) || TextUtils.isEmpty(obj2) || TextUtils.isEmpty(obj3)) {
                            new gh(this.a, null, eh.e()).a();
                        } else {
                            new gh(this.a, new gi(obj2, obj, obj3), eh.e()).a();
                        }
                    }
                }
                g.f = e;
                fo.a(this.a, e);
                interrupt();
                this.b.setRunLowFrame(false);
            }
        } catch (Throwable th) {
            interrupt();
            fo.b(th, "AMapDelegateImpGLSurfaceView", "mVerfy");
            th.printStackTrace();
        }
    }
}
