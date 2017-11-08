package com.google.android.gms.internal;

import java.io.IOException;
import java.util.List;

/* compiled from: Unknown */
public abstract class ka<M extends ka<M>> extends ke {
    protected List<kg> aae;

    public void a(jz jzVar) throws IOException {
        int i = 0;
        int size = this.aae != null ? this.aae.size() : 0;
        while (i < size) {
            kg kgVar = (kg) this.aae.get(i);
            jzVar.cF(kgVar.tag);
            jzVar.p(kgVar.aai);
            i++;
        }
    }

    public int c() {
        int i = 0;
        int i2 = 0;
        while (i < (this.aae != null ? this.aae.size() : 0)) {
            kg kgVar = (kg) this.aae.get(i);
            i++;
            i2 = kgVar.aai.length + (i2 + jz.cG(kgVar.tag));
        }
        this.DY = i2;
        return i2;
    }
}
