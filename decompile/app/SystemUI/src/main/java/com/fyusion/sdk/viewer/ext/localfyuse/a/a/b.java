package com.fyusion.sdk.viewer.ext.localfyuse.a.a;

import com.fyusion.sdk.common.ext.c;
import com.fyusion.sdk.common.ext.f;
import com.fyusion.sdk.common.ext.filter.ImageFilterFactory;
import com.fyusion.sdk.common.ext.m;
import com.fyusion.sdk.common.o;
import com.fyusion.sdk.viewer.FyuseException;
import com.fyusion.sdk.viewer.ext.localfyuse.d;
import com.fyusion.sdk.viewer.internal.b.c.a;
import java.io.File;

/* compiled from: Unknown */
public class b {
    private c a;

    public b(c cVar) {
        this.a = cVar;
    }

    public a a(File file) throws Exception {
        m mVar = new m(this.a, file);
        f fVar = new f();
        o bVar = new com.fyusion.sdk.viewer.view.b();
        if (mVar.a(fVar, bVar)) {
            a aVar = new a(d.a(file.getName(), fVar), fVar, mVar.a(fVar, new ImageFilterFactory()), mVar.c());
            aVar.a(file);
            aVar.a(bVar);
            return aVar;
        }
        throw new FyuseException("Corrupt fyuse file.");
    }
}
