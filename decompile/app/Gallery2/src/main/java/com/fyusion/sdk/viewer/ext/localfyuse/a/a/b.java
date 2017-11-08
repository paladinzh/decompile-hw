package com.fyusion.sdk.viewer.ext.localfyuse.a.a;

import com.fyusion.sdk.common.ext.e;
import com.fyusion.sdk.common.ext.filter.ImageFilterFactory;
import com.fyusion.sdk.common.ext.l;
import com.fyusion.sdk.common.m;
import com.fyusion.sdk.viewer.FyuseException;
import com.fyusion.sdk.viewer.ext.localfyuse.d;
import com.fyusion.sdk.viewer.internal.b.c.a;
import java.io.File;

/* compiled from: Unknown */
public class b {
    private com.fyusion.sdk.common.ext.b a;

    public b(com.fyusion.sdk.common.ext.b bVar) {
        this.a = bVar;
    }

    public a a(File file) throws Exception {
        l lVar = new l(this.a, file);
        e eVar = new e();
        m bVar = new com.fyusion.sdk.viewer.view.b();
        if (lVar.a(eVar, bVar)) {
            a aVar = new a(d.a(file.getName(), eVar), eVar, lVar.a(eVar, new ImageFilterFactory()), lVar.c());
            aVar.a(file);
            aVar.a(bVar);
            return aVar;
        }
        throw new FyuseException("Corrupt fyuse file.");
    }
}
