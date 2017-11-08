package com.a.a.a;

import android.os.SystemClock;
import android.support.v4.app.FragmentTransaction;
import com.a.a.f;
import com.a.a.h;
import com.a.a.i;
import com.a.a.j;
import com.a.a.l;
import com.a.a.n.d;
import com.a.a.p;
import com.a.a.q;
import com.a.a.r;
import com.a.a.s;
import com.a.a.t;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.cookie.DateUtils;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

/* compiled from: Unknown */
public class a implements f {
    protected static final boolean a = t.b;
    protected final e b;
    protected final c c;
    long d;

    public a(e eVar) {
        this(eVar, new c(FragmentTransaction.TRANSIT_ENTER_MASK));
    }

    public a(e eVar, c cVar) {
        this.d = 0;
        this.b = eVar;
        this.c = cVar;
    }

    protected static Map<String, String> a(Header[] headerArr) {
        Map<String, String> treeMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < headerArr.length; i++) {
            treeMap.put(headerArr[i].getName(), headerArr[i].getValue());
        }
        return treeMap;
    }

    private static void a(String str, l<?> lVar, s sVar) throws s {
        p t = lVar.t();
        int s = lVar.s();
        try {
            t.a(sVar);
            lVar.a(String.format(Locale.US, "%s-retry [timeout=%s]", new Object[]{str, Integer.valueOf(s)}));
        } catch (s e) {
            lVar.a(String.format(Locale.US, "%s-timeout-giveup [timeout=%s]", new Object[]{str, Integer.valueOf(s)}));
            throw e;
        }
    }

    private void a(Map<String, String> map, com.a.a.b.a aVar) {
        if (aVar != null) {
            if (aVar.b != null) {
                map.put("If-None-Match", aVar.b);
            }
            if ((aVar.d <= 0 ? 1 : null) == null) {
                map.put("If-Modified-Since", DateUtils.formatDate(new Date(aVar.d)));
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private byte[] a(l<?> lVar, HttpEntity httpEntity) throws IOException, q {
        i iVar = new i(this.c, (int) httpEntity.getContentLength());
        byte[] bArr = null;
        long contentLength = (long) ((int) httpEntity.getContentLength());
        try {
            d dVar = !(lVar instanceof d) ? null : (d) lVar;
            com.a.a.n.a aVar = !(lVar instanceof com.a.a.n.a) ? null : (com.a.a.n.a) lVar;
            InputStream content = httpEntity.getContent();
            if (content != null) {
                bArr = this.c.a(16384);
                int i = 0;
                while (true) {
                    int read = content.read(bArr);
                    if (read != -1) {
                        if (lVar.g()) {
                            break;
                        }
                        iVar.write(bArr, 0, read);
                        i += read;
                        if (dVar != null) {
                            dVar.a(((long) i) + this.d, this.d + contentLength);
                        }
                        if (aVar != null) {
                            aVar.a(iVar);
                        }
                    } else {
                        break;
                    }
                }
                byte[] toByteArray = iVar.toByteArray();
                return toByteArray;
            }
            throw new q();
        } finally {
            try {
                httpEntity.consumeContent();
            } catch (IOException e) {
                t.a("Error occured when calling consumingContent", new Object[0]);
            }
            this.c.a(bArr);
            iVar.close();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public i a(l<?> lVar) throws s {
        byte[] bArr;
        Throwable e;
        long elapsedRealtime = SystemClock.elapsedRealtime();
        while (true) {
            HttpResponse httpResponse = null;
            Map emptyMap = Collections.emptyMap();
            HttpResponse a;
            int statusCode;
            try {
                Map hashMap = new HashMap();
                a(hashMap, lVar.f());
                this.d = 0;
                a = this.b.a(lVar, hashMap);
                statusCode = a.getStatusLine().getStatusCode();
                emptyMap = a(a.getAllHeaders());
                if (statusCode == SmsCheckResult.ESCT_304) {
                    break;
                }
                if (statusCode == SmsCheckResult.ESCT_301 || statusCode == SmsCheckResult.ESCT_302) {
                    lVar.c((String) emptyMap.get("Location"));
                }
                if (a.getEntity() == null) {
                    bArr = new byte[0];
                } else {
                    bArr = a((l) lVar, a.getEntity());
                }
                if (statusCode >= SmsCheckResult.ESCT_200 && statusCode <= 299) {
                    return new i(statusCode, bArr, emptyMap, false, SystemClock.elapsedRealtime() - elapsedRealtime);
                }
                t.c("Unexpected response code %d", Integer.valueOf(statusCode));
                throw new IOException();
            } catch (SocketTimeoutException e2) {
                a("socket", lVar, new r());
            } catch (ConnectTimeoutException e3) {
                a("connection", lVar, new r());
            } catch (Throwable e4) {
                throw new RuntimeException("Bad URL " + lVar.c(), e4);
            } catch (IOException e5) {
                e4 = e5;
                httpResponse = a;
                if (httpResponse == null) {
                    throw new j(e4);
                }
                i iVar;
                statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode != SmsCheckResult.ESCT_301) {
                    t.c("Unexpected response code %d for %s", Integer.valueOf(statusCode), lVar.c());
                    if (bArr == null) {
                        iVar = new i(statusCode, bArr, emptyMap, false, SystemClock.elapsedRealtime() - elapsedRealtime);
                        if (statusCode != 401) {
                            if (statusCode != SmsCheckResult.ESCT_301) {
                                throw new q(iVar);
                            }
                            a("redirect", lVar, new com.a.a.a(iVar));
                        }
                        a("auth", lVar, new com.a.a.a(iVar));
                    } else {
                        throw new h(null);
                    }
                }
                t.c("Request at %s has been redirected to %s", lVar.d(), lVar.c());
                if (bArr == null) {
                    throw new h(null);
                }
                iVar = new i(statusCode, bArr, emptyMap, false, SystemClock.elapsedRealtime() - elapsedRealtime);
                if (statusCode != 401) {
                    if (statusCode != SmsCheckResult.ESCT_301) {
                        throw new q(iVar);
                    }
                    a("redirect", lVar, new com.a.a.a(iVar));
                }
                a("auth", lVar, new com.a.a.a(iVar));
            }
        }
        com.a.a.b.a f = lVar.f();
        if (f == null) {
            return new i(SmsCheckResult.ESCT_304, null, emptyMap, true, SystemClock.elapsedRealtime() - elapsedRealtime);
        }
        f.g.putAll(emptyMap);
        return new i(SmsCheckResult.ESCT_304, f.a, f.g, true, SystemClock.elapsedRealtime() - elapsedRealtime);
    }
}
