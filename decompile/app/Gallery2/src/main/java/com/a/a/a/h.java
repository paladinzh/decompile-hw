package com.a.a.a;

import com.a.a.i;
import com.a.a.l;
import com.a.a.l.a;
import com.a.a.n;
import com.a.a.n.b;
import com.a.a.n.c;
import com.a.a.t;
import java.io.UnsupportedEncodingException;

/* compiled from: Unknown */
public abstract class h<T> extends l<T> {
    private static final String a = String.format("application/json; charset=%s", new Object[]{"utf-8"});
    private final c<T> b;
    private final String c;

    public h(int i, String str, String str2, c<T> cVar, b bVar) {
        super(i, str, bVar);
        this.b = cVar;
        this.c = str2;
    }

    protected abstract n<T> a(i iVar);

    protected void a(T t) {
        this.b.a(t);
    }

    public String k() {
        return o();
    }

    public byte[] l() {
        return p();
    }

    public String o() {
        return a;
    }

    public byte[] p() {
        byte[] bArr = null;
        try {
            if (this.c != null) {
                bArr = this.c.getBytes("utf-8");
            }
            return bArr;
        } catch (UnsupportedEncodingException e) {
            t.d("Unsupported Encoding while trying to get the bytes of %s using %s", this.c, "utf-8");
            return null;
        }
    }

    public a r() {
        return a.IMMEDIATE;
    }
}
