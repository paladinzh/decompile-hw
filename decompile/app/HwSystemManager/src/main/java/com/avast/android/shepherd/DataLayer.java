package com.avast.android.shepherd;

import com.avast.android.shepherd.obfuscated.bc.ae;
import com.avast.android.shepherd.obfuscated.bc.q;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public class DataLayer {
    private Map<String, Object> a;

    public DataLayer(q qVar) {
        a(qVar);
    }

    private void a(q qVar) {
        Map hashMap = new HashMap();
        if (qVar.d().r()) {
            for (ae aeVar : qVar.d().s().c()) {
                if (aeVar.i()) {
                    hashMap.put(aeVar.d().toStringUtf8(), aeVar.j().toStringUtf8());
                } else if (aeVar.e()) {
                    hashMap.put(aeVar.d().toStringUtf8(), Integer.valueOf(aeVar.f()));
                } else if (aeVar.g()) {
                    hashMap.put(aeVar.d().toStringUtf8(), Long.valueOf(aeVar.h()));
                } else if (aeVar.k()) {
                    hashMap.put(aeVar.d().toStringUtf8(), Boolean.valueOf(aeVar.l()));
                } else if (aeVar.m()) {
                    hashMap.put(aeVar.d().toStringUtf8(), Byte.valueOf(Integer.valueOf(aeVar.n()).byteValue()));
                }
            }
            this.a = Collections.unmodifiableMap(hashMap);
        }
    }

    public <T> T get(String str, T t) {
        if (this.a != null) {
            T t2 = this.a.get(str);
            if (t2 != null) {
                return t2;
            }
        }
        return t;
    }
}
