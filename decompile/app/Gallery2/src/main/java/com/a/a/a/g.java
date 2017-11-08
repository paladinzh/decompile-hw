package com.a.a.a;

import com.a.a.i;
import com.a.a.k;
import com.a.a.n;
import com.a.a.n.b;
import com.a.a.n.c;
import org.json.JSONObject;

/* compiled from: Unknown */
public class g extends h<JSONObject> {
    public g(int i, String str, JSONObject jSONObject, c<JSONObject> cVar, b bVar) {
        String str2 = null;
        if (jSONObject != null) {
            str2 = jSONObject.toString();
        }
        super(i, str, str2, cVar, bVar);
    }

    protected n<JSONObject> a(i iVar) {
        try {
            return n.a(new JSONObject(new String(iVar.b, d.a(iVar.c, "utf-8"))), d.a(iVar));
        } catch (Throwable e) {
            return n.a(new k(e));
        } catch (Throwable e2) {
            return n.a(new k(e2));
        }
    }
}
