package com.a.a.a;

import com.a.a.i;
import com.a.a.l;
import com.a.a.n;
import com.a.a.n.b;
import com.a.a.n.c;
import java.io.UnsupportedEncodingException;

/* compiled from: Unknown */
public class j extends l<String> {
    private final c<String> a;

    public j(int i, String str, c<String> cVar, b bVar) {
        super(i, str, bVar);
        this.a = cVar;
    }

    protected n<String> a(i iVar) {
        Object str;
        try {
            str = new String(iVar.b, d.a(iVar.c));
        } catch (UnsupportedEncodingException e) {
            str = new String(iVar.b);
        }
        return n.a(str, d.a(iVar));
    }

    protected /* synthetic */ void a(Object obj) {
        d((String) obj);
    }

    protected void d(String str) {
        this.a.a(str);
    }
}
