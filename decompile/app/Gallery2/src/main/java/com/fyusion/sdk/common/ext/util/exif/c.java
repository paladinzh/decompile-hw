package com.fyusion.sdk.common.ext.util.exif;

import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* compiled from: Unknown */
class c {
    private static final byte[] a = new byte[]{(byte) 65, (byte) 83, (byte) 67, (byte) 73, (byte) 73, (byte) 0, (byte) 0, (byte) 0};
    private static final byte[] b = new byte[]{(byte) 74, (byte) 73, (byte) 83, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
    private static final byte[] c = new byte[]{(byte) 85, (byte) 78, (byte) 73, (byte) 67, (byte) 79, (byte) 68, (byte) 69, (byte) 0};
    private final h[] d = new h[5];
    private byte[] e;
    private ArrayList<byte[]> f = new ArrayList();
    private final ByteOrder g;

    c(ByteOrder byteOrder) {
        this.g = byteOrder;
    }

    protected ExifTag a(ExifTag exifTag) {
        return exifTag == null ? null : a(exifTag, exifTag.getIfd());
    }

    protected ExifTag a(ExifTag exifTag, int i) {
        return (exifTag != null && ExifTag.isValidIfd(i)) ? c(i).a(exifTag) : null;
    }

    protected ExifTag a(short s, int i) {
        h hVar = this.d[i];
        return hVar != null ? hVar.a(s) : null;
    }

    protected List<ExifTag> a(short s) {
        List arrayList = new ArrayList();
        for (h hVar : this.d) {
            if (hVar != null) {
                ExifTag a = hVar.a(s);
                if (a != null) {
                    arrayList.add(a);
                }
            }
        }
        return arrayList.size() != 0 ? arrayList : null;
    }

    protected void a(int i, byte[] bArr) {
        if (i >= this.f.size()) {
            for (int size = this.f.size(); size < i; size++) {
                this.f.add(null);
            }
            this.f.add(bArr);
            return;
        }
        this.f.set(i, bArr);
    }

    protected void a(h hVar) {
        this.d[hVar.c()] = hVar;
    }

    protected void a(byte[] bArr) {
        this.e = bArr;
    }

    protected byte[] a() {
        return this.e;
    }

    protected byte[] a(int i) {
        return (byte[]) this.f.get(i);
    }

    protected h b(int i) {
        return !ExifTag.isValidIfd(i) ? null : this.d[i];
    }

    protected void b(short s, int i) {
        h hVar = this.d[i];
        if (hVar != null) {
            hVar.b(s);
        }
    }

    protected boolean b() {
        return this.e != null;
    }

    protected int c() {
        return this.f.size();
    }

    protected h c(int i) {
        h hVar = this.d[i];
        if (hVar != null) {
            return hVar;
        }
        hVar = new h(i);
        this.d[i] = hVar;
        return hVar;
    }

    protected List<ExifTag> d(int i) {
        h hVar = this.d[i];
        if (hVar == null) {
            return null;
        }
        ExifTag[] b = hVar.b();
        if (b == null) {
            return null;
        }
        List arrayList = new ArrayList(b.length);
        for (Object add : b) {
            arrayList.add(add);
        }
        return arrayList.size() != 0 ? arrayList : null;
    }

    protected boolean d() {
        return this.f.size() != 0;
    }

    protected ByteOrder e() {
        return this.g;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof c)) {
            return false;
        }
        c cVar = (c) obj;
        if (cVar.g != this.g || cVar.f.size() != this.f.size() || !Arrays.equals(cVar.e, this.e)) {
            return false;
        }
        for (int i = 0; i < this.f.size(); i++) {
            if (!Arrays.equals((byte[]) cVar.f.get(i), (byte[]) this.f.get(i))) {
                return false;
            }
        }
        for (int i2 = 0; i2 < 5; i2++) {
            h b = cVar.b(i2);
            h b2 = b(i2);
            if (b != b2 && b != null && !b.equals(b2)) {
                return false;
            }
        }
        return true;
    }

    protected void f() {
        this.e = null;
        this.f.clear();
    }

    protected String g() {
        h hVar = this.d[0];
        if (hVar == null) {
            return null;
        }
        ExifTag a = hVar.a(ExifInterface.getTrueTagKey(ExifInterface.TAG_USER_COMMENT));
        if (a == null || a.getComponentCount() < 8) {
            return null;
        }
        byte[] bArr = new byte[a.getComponentCount()];
        a.a(bArr);
        Object obj = new byte[8];
        System.arraycopy(bArr, 0, obj, 0, 8);
        try {
            return !Arrays.equals(obj, a) ? !Arrays.equals(obj, b) ? !Arrays.equals(obj, c) ? null : new String(bArr, 8, bArr.length - 8, "UTF-16") : new String(bArr, 8, bArr.length - 8, "EUC-JP") : new String(bArr, 8, bArr.length - 8, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    protected List<ExifTag> h() {
        List arrayList = new ArrayList();
        for (h hVar : this.d) {
            if (hVar != null) {
                ExifTag[] b = hVar.b();
                if (b != null) {
                    for (Object add : b) {
                        arrayList.add(add);
                    }
                }
            }
        }
        return arrayList.size() != 0 ? arrayList : null;
    }
}
