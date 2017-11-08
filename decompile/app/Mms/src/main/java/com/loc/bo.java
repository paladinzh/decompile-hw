package com.loc;

import com.amap.api.maps.AMapException;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;

/* compiled from: BaseNetManager */
public class bo {
    private static bo a;

    public static bo a() {
        if (a == null) {
            a = new bo();
        }
        return a;
    }

    public HttpURLConnection a(bs bsVar, boolean z) throws l {
        l e;
        try {
            c(bsVar);
            Proxy proxy = bsVar.c != null ? bsVar.c : null;
            HttpURLConnection a = (!z ? new bq(bsVar.a, bsVar.b, proxy, false) : new bq(bsVar.a, bsVar.b, proxy, true)).a(bsVar.e(), bsVar.a(), true);
            byte[] f = bsVar.f();
            if (f != null) {
                if (f.length > 0) {
                    DataOutputStream dataOutputStream = new DataOutputStream(a.getOutputStream());
                    dataOutputStream.write(f);
                    dataOutputStream.close();
                }
            }
            a.connect();
            return a;
        } catch (l e2) {
            throw e2;
        } catch (Throwable th) {
            th.printStackTrace();
            e2 = new l(AMapException.ERROR_UNKNOWN);
        }
    }

    public byte[] a(bs bsVar) throws l {
        l e;
        try {
            bt b = b(bsVar, true);
            return b == null ? null : b.a;
        } catch (l e2) {
            throw e2;
        } catch (Throwable th) {
            e2 = new l(AMapException.ERROR_UNKNOWN);
        }
    }

    protected bt b(bs bsVar, boolean z) throws l {
        l e;
        Proxy proxy = null;
        try {
            c(bsVar);
            if (bsVar.c != null) {
                proxy = bsVar.c;
            }
            return new bq(bsVar.a, bsVar.b, proxy, z).a(bsVar.e(), bsVar.a(), bsVar.f());
        } catch (l e2) {
            throw e2;
        } catch (Throwable th) {
            th.printStackTrace();
            e2 = new l(AMapException.ERROR_UNKNOWN);
        }
    }

    public byte[] b(bs bsVar) throws l {
        l e;
        try {
            bt b = b(bsVar, false);
            return b == null ? null : b.a;
        } catch (l e2) {
            throw e2;
        } catch (Throwable th) {
            aa.a(th, "BaseNetManager", "makeSyncPostRequest");
            e2 = new l(AMapException.ERROR_UNKNOWN);
        }
    }

    protected void c(bs bsVar) throws l {
        if (bsVar == null) {
            throw new l("requeust is null");
        } else if (bsVar.c() == null || "".equals(bsVar.c())) {
            throw new l("request url is empty");
        }
    }
}
