package com.amap.api.mapcore.util;

import android.content.Context;
import com.amap.api.mapcore.util.ez.a;
import com.amap.api.maps.AMapException;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: ProtocalHandler */
public abstract class by<T, V> {
    protected T a;
    protected int b = 3;
    protected Context c;

    protected abstract String a();

    protected abstract JSONObject a(a aVar);

    protected abstract V b(JSONObject jSONObject) throws AMapException;

    protected abstract Map<String, String> b();

    public by(Context context, T t) {
        a(context, t);
    }

    private void a(Context context, T t) {
        this.c = context;
        this.a = t;
    }

    public V c() throws AMapException {
        if (this.a == null) {
            return null;
        }
        return d();
    }

    protected V d() throws AMapException {
        int i = 0;
        V v = null;
        a aVar = null;
        while (i < this.b) {
            try {
                aVar = ez.a(this.c, eh.e(), a(), b());
                v = b(a(aVar));
                i = this.b;
            } catch (Throwable th) {
                Throwable th2 = th;
                V v2 = v;
                Throwable th3 = th2;
                fo.b(th3, "ProtocalHandler", "getDataMayThrow AMapException");
                th3.printStackTrace();
                i++;
                if (i < this.b) {
                    v = v2;
                } else if (aVar == null || aVar.a == null) {
                    v = null;
                } else {
                    AMapException aMapException = new AMapException(aVar.a);
                }
            }
        }
        return v;
    }
}
