package com.fyusion.sdk.common.ext.util.exif;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
class d {
    private final ByteBuffer a;
    private final c b;
    private final List<a> c = new ArrayList();
    private final ExifInterface d;
    private int e;

    /* compiled from: Unknown */
    private static class a {
        final int a;
        final ExifTag b;

        a(ExifTag exifTag, int i) {
            this.b = exifTag;
            this.a = i;
        }
    }

    protected d(ByteBuffer byteBuffer, ExifInterface exifInterface) throws IOException, ExifInvalidFormatException {
        Closeable aVar;
        Throwable th;
        this.a = byteBuffer;
        this.e = byteBuffer.position();
        this.d = exifInterface;
        try {
            aVar = new a(byteBuffer);
            try {
                f a = f.a((InputStream) aVar, this.d);
                this.b = new c(a.o());
                this.e = a.i() + this.e;
                this.a.position(0);
                ExifInterface.a(aVar);
            } catch (Throwable th2) {
                th = th2;
                ExifInterface.a(aVar);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            aVar = null;
            ExifInterface.a(aVar);
            throw th;
        }
    }

    private void a(ExifTag exifTag, int i) {
        int i2 = 0;
        this.a.position(this.e + i);
        int componentCount;
        switch (exifTag.getDataType()) {
            case (short) 1:
            case (short) 7:
                byte[] bArr = new byte[exifTag.getComponentCount()];
                exifTag.a(bArr);
                this.a.put(bArr);
                return;
            case (short) 2:
                byte[] a = exifTag.a();
                if (a.length != exifTag.getComponentCount()) {
                    this.a.put(a);
                    this.a.put((byte) 0);
                    return;
                }
                a[a.length - 1] = (byte) 0;
                this.a.put(a);
                return;
            case (short) 3:
                componentCount = exifTag.getComponentCount();
                while (i2 < componentCount) {
                    this.a.putShort((short) ((int) exifTag.c(i2)));
                    i2++;
                }
                return;
            case (short) 4:
            case (short) 9:
                componentCount = exifTag.getComponentCount();
                while (i2 < componentCount) {
                    this.a.putInt((int) exifTag.c(i2));
                    i2++;
                }
                return;
            case (short) 5:
            case (short) 10:
                componentCount = exifTag.getComponentCount();
                while (i2 < componentCount) {
                    Rational d = exifTag.d(i2);
                    this.a.putInt((int) d.getNumerator());
                    this.a.putInt((int) d.getDenominator());
                    i2++;
                }
                return;
            default:
                return;
        }
    }

    private void c() {
        this.a.order(a());
        for (a aVar : this.c) {
            a(aVar.b, aVar.a);
        }
    }

    protected ByteOrder a() {
        return this.b.e();
    }

    public void a(ExifTag exifTag) {
        this.b.a(exifTag);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean b() throws IOException, ExifInvalidFormatException {
        Throwable th;
        Closeable aVar;
        try {
            aVar = new a(this.a);
            h[] hVarArr = new h[]{this.b.b(0), this.b.b(1), this.b.b(2), this.b.b(3), this.b.b(4)};
            int i = hVarArr[0] == null ? 0 : 1;
            if (hVarArr[1] != null) {
                i |= 2;
            }
            if (hVarArr[2] != null) {
                i |= 4;
            }
            if (hVarArr[4] != null) {
                i |= 8;
            }
            if (hVarArr[3] != null) {
                i |= 16;
            }
            f a = f.a(aVar, i, this.d);
            int a2 = a.a();
            h hVar = null;
            while (a2 != 5) {
                switch (a2) {
                    case 0:
                        hVar = hVarArr[a.d()];
                        if (hVar == null) {
                            a.b();
                        }
                    case 1:
                        ExifTag c = a.c();
                        ExifTag a3 = hVar != null ? hVar.a(c.getTagId()) : null;
                        if (a3 != null) {
                            if (a3.getComponentCount() == c.getComponentCount()) {
                                if (a3.getDataType() == c.getDataType()) {
                                    this.c.add(new a(a3, c.b()));
                                    hVar.b(c.getTagId());
                                    if (hVar.d() == 0) {
                                        a.b();
                                    }
                                }
                            }
                            ExifInterface.a(aVar);
                            return false;
                        }
                }
                try {
                    a2 = a.a();
                } catch (Throwable th2) {
                    th = th2;
                }
            }
            for (h hVar2 : hVarArr) {
                if (hVar2 != null && hVar2.d() > 0) {
                    ExifInterface.a(aVar);
                    return false;
                }
            }
            c();
            ExifInterface.a(aVar);
            return true;
        } catch (Throwable th3) {
            th = th3;
            aVar = null;
            ExifInterface.a(aVar);
            throw th;
        }
    }
}
