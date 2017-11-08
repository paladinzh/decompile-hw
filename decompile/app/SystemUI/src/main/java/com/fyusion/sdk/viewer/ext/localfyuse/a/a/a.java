package com.fyusion.sdk.viewer.ext.localfyuse.a.a;

import com.fyusion.sdk.common.ext.f;
import com.fyusion.sdk.common.ext.filter.ImageFilter;
import com.fyusion.sdk.common.i;
import com.fyusion.sdk.common.j;
import com.fyusion.sdk.core.a.h;
import com.fyusion.sdk.viewer.internal.b.c.a.b;
import java.io.File;
import java.util.Iterator;
import java.util.List;

/* compiled from: Unknown */
public class a extends com.fyusion.sdk.viewer.internal.b.c.a {
    private List<ImageFilter> l;
    private f m;
    private File n;

    a(i iVar, f fVar, List<ImageFilter> list, File file) {
        super(iVar);
        this.m = fVar;
        this.l = list;
        this.n = file;
        this.b = (int) fVar.getProcessedSize().height;
        this.k = true;
    }

    public f a() {
        return this.m;
    }

    public h a(int i) {
        h a = super.a(i);
        if (a == null) {
            if (this.j == null) {
                return null;
            }
            try {
                a = this.j.a(this.d.getWidth(true), this.d.getHeight(true), i);
            } catch (IndexOutOfBoundsException e) {
                com.fyusion.sdk.common.h.c("LocalFyuseData", "Exception on getFile for frame: " + i);
            }
        }
        return a;
    }

    public void a(i iVar) {
        this.d = iVar;
        this.c = iVar.getMagic();
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
            this.j = new com.fyusion.sdk.common.ext.i(file);
            if (this.j.a(com.fyusion.sdk.common.j.a.READ_ONLY, j.b.NONE)) {
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
