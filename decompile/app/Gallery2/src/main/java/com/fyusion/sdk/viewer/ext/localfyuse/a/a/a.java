package com.fyusion.sdk.viewer.ext.localfyuse.a.a;

import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.ext.e;
import com.fyusion.sdk.common.ext.filter.ImageFilter;
import com.fyusion.sdk.common.h;
import com.fyusion.sdk.common.i;
import com.fyusion.sdk.viewer.internal.b.c.a.b;
import java.io.File;
import java.util.Iterator;
import java.util.List;

/* compiled from: Unknown */
public class a extends com.fyusion.sdk.viewer.internal.b.c.a {
    private List<ImageFilter> l;
    private e m;
    private File n;

    a(h hVar, e eVar, List<ImageFilter> list, File file) {
        super(hVar);
        this.m = eVar;
        this.l = list;
        this.n = file;
        this.b = (int) eVar.getProcessedSize().height;
        this.k = true;
    }

    public e a() {
        return this.m;
    }

    public com.fyusion.sdk.core.a.h a(int i) {
        com.fyusion.sdk.core.a.h a = super.a(i);
        if (a == null) {
            if (this.j == null) {
                return null;
            }
            try {
                a = this.j.a(this.d.getWidth(true), this.d.getHeight(true), i);
            } catch (IndexOutOfBoundsException e) {
                DLog.w("LocalFyuseData", "Exception on getFile for frame: " + i);
            }
        }
        return a;
    }

    public void a(h hVar) {
        this.d = hVar;
        this.c = hVar.getMagic();
        if (this.i != null) {
            a(new File(this.i));
        }
        Iterator it = this.f.iterator();
        while (it.hasNext()) {
            b bVar = (b) it.next();
            bVar.a(this);
            if (this.c.getEndFrame() - this.c.getStartFrame() > 0) {
                bVar.a();
            }
        }
    }

    public synchronized void a(File file) {
        this.i = file.getAbsolutePath();
        try {
            this.j = new com.fyusion.sdk.common.ext.h(file);
            if (this.j.a(com.fyusion.sdk.common.i.a.READ_ONLY, i.b.NONE)) {
                this.g = 0;
                this.h = this.j.c() - 1;
            } else {
                this.j = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<ImageFilter> b() {
        return this.l;
    }

    public File c() {
        return this.n;
    }
}
