package com.fyusion.sdk.common.ext;

import com.fyusion.sdk.common.i;
import com.fyusion.sdk.common.i.a;
import com.fyusion.sdk.common.i.b;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

/* compiled from: Unknown */
public class h extends i {
    private boolean n = false;
    private int o;
    private int p;
    private int q = 0;
    private long r = 0;

    public h(File file) {
        super(file);
    }

    private boolean p() {
        this.n = new File(this.g.getParent() + File.separator + String.format(Locale.US, j.aI, new Object[]{Integer.valueOf(0)})).exists();
        return this.n;
    }

    private int q() {
        int i = (this.o + this.p) / 2;
        if (new File(this.g.getParent() + File.separator + String.format(Locale.US, j.aI, new Object[]{Integer.valueOf(i)})).exists()) {
            this.o = i;
            return this.o;
        }
        this.p = i;
        while (this.o < this.p - 1) {
            q();
        }
        return this.o;
    }

    public com.fyusion.sdk.core.a.h a(int i, int i2, int i3) throws IndexOutOfBoundsException {
        if (!this.n) {
            return super.a(i, i2, i3);
        }
        if (i3 >= 0 && i3 < c()) {
            com.fyusion.sdk.core.a.h hVar = new com.fyusion.sdk.core.a.h(this, i3, i, i2, 0);
            hVar.b(0);
            return hVar;
        }
        throw new IndexOutOfBoundsException();
    }

    public FileOutputStream a(int i) throws IOException {
        throw new IOException("Local fyuses are always read-only");
    }

    public void a(FileInputStream fileInputStream) {
        try {
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void a(FileOutputStream fileOutputStream) {
    }

    public boolean a(a aVar, b bVar) {
        boolean z = true;
        if (aVar != a.READ_ONLY) {
            return false;
        }
        if (this.a) {
            return true;
        }
        if (super.d()) {
            try {
                this.r = (long) g.a().e(this.g);
                if (this.r < 0) {
                    z = false;
                }
                return !z ? false : super.a(a.READ_ONLY, b.NONE);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else if (!p()) {
            return false;
        } else {
            synchronized (this) {
                this.o = 0;
                this.p = 300;
                this.q = q() + 1;
            }
            if (this.q <= 0) {
                z = false;
            }
            this.a = z;
            return this.a;
        }
    }

    public FileInputStream b(int i) throws IOException, IndexOutOfBoundsException {
        if (!this.a) {
            throw new IOException("File is not open");
        } else if (!this.n) {
            return super.b(i);
        } else {
            String parent = this.g.getParent();
            if (i >= 0 && i < c()) {
                return new FileInputStream(new File(parent + File.separator + String.format(Locale.US, j.aI, new Object[]{Integer.valueOf(i)})));
            }
            throw new IndexOutOfBoundsException();
        }
    }

    public synchronized int c() {
        if (!this.a) {
            return 0;
        }
        if (this.n) {
            return this.q;
        }
        return this.b.size();
    }

    public boolean d() {
        try {
            return f.c(this.g) != null ? true : p();
        } catch (IOException e) {
            return p();
        }
    }

    public void e() {
    }

    public void h() {
        this.a = false;
        this.n = false;
        this.q = 0;
    }

    protected String j() {
        return "fyuse.fyu";
    }

    protected long k() {
        return this.r;
    }

    protected long l() {
        return 0;
    }

    public boolean o() {
        return this.n;
    }
}
