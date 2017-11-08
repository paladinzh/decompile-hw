package com.fyusion.sdk.common.ext.util.exif;

import android.util.Log;
import fyusion.vislib.BuildConfig;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.TreeMap;

/* compiled from: Unknown */
class f {
    private static final Charset a = Charset.forName("US-ASCII");
    private static final short s = ((short) ExifInterface.getTrueTagKey(ExifInterface.TAG_EXIF_IFD));
    private static final short t = ((short) ExifInterface.getTrueTagKey(ExifInterface.TAG_GPS_IFD));
    private static final short u = ((short) ExifInterface.getTrueTagKey(ExifInterface.TAG_INTEROPERABILITY_IFD));
    private static final short v = ((short) ExifInterface.getTrueTagKey(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT));
    private static final short w = ((short) ExifInterface.getTrueTagKey(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH));
    private static final short x = ((short) ExifInterface.getTrueTagKey(ExifInterface.TAG_STRIP_OFFSETS));
    private static final short y = ((short) ExifInterface.getTrueTagKey(ExifInterface.TAG_STRIP_BYTE_COUNTS));
    private final b b;
    private final int c;
    private int d = 0;
    private int e = 0;
    private int f;
    private ExifTag g;
    private c h;
    private ExifTag i;
    private ExifTag j;
    private boolean k;
    private boolean l = false;
    private int m;
    private int n = 0;
    private byte[] o;
    private int p;
    private int q;
    private final ExifInterface r;
    private final TreeMap<Integer, Object> z = new TreeMap();

    /* compiled from: Unknown */
    private static class a {
        ExifTag a;
        boolean b;

        a(ExifTag exifTag, boolean z) {
            this.a = exifTag;
            this.b = z;
        }
    }

    /* compiled from: Unknown */
    private static class b {
        int a;
        boolean b;

        b(int i, boolean z) {
            this.a = i;
            this.b = z;
        }
    }

    /* compiled from: Unknown */
    private static class c {
        int a;
        int b;

        c(int i) {
            this.a = 0;
            this.b = i;
        }

        c(int i, int i2) {
            this.b = i;
            this.a = i2;
        }
    }

    private f(InputStream inputStream, int i, ExifInterface exifInterface) throws IOException, ExifInvalidFormatException {
        if (inputStream != null) {
            this.r = exifInterface;
            this.l = a(inputStream);
            this.b = new b(inputStream);
            this.c = i;
            if (this.l) {
                s();
                long f = this.b.f();
                if (f <= 2147483647L) {
                    this.p = (int) f;
                    this.f = 0;
                    if (b(0) || q()) {
                        a(0, f);
                        if (f != 8) {
                            this.o = new byte[(((int) f) - 8)];
                            a(this.o);
                        }
                    }
                    return;
                }
                throw new ExifInvalidFormatException("Invalid offset " + f);
            }
            return;
        }
        throw new IOException("Null argument inputStream to ExifParser");
    }

    protected static f a(InputStream inputStream, ExifInterface exifInterface) throws IOException, ExifInvalidFormatException {
        return new f(inputStream, 63, exifInterface);
    }

    private void a(int i, long j) {
        this.z.put(Integer.valueOf((int) j), new b(i, b(i)));
    }

    private void a(long j) {
        this.z.put(Integer.valueOf((int) j), new c(3));
    }

    private boolean a(int i, int i2) {
        int i3 = this.r.a().get(i2);
        return i3 != 0 ? ExifInterface.a(i3, i) : false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean a(InputStream inputStream) throws IOException, ExifInvalidFormatException {
        b bVar = new b(inputStream);
        if (bVar.c() == (short) -40) {
            short c = bVar.c();
            while (true) {
                short s = c;
                if (!(s == (short) -39 || i.a(s))) {
                    int d = bVar.d();
                    if (s == (short) -31 && d >= 8) {
                        int e = bVar.e();
                        short c2 = bVar.c();
                        d -= 6;
                        if (e == 1165519206 && c2 == (short) 0) {
                            this.q = bVar.a();
                            this.m = d;
                            this.n = this.q + this.m;
                            return true;
                        }
                    }
                    if (d >= 2 && ((long) (d - 2)) == bVar.skip((long) (d - 2))) {
                        c = bVar.c();
                    }
                }
            }
            return false;
        }
        throw new ExifInvalidFormatException("Invalid JPEG format");
    }

    private void b(int i, long j) {
        this.z.put(Integer.valueOf((int) j), new c(4, i));
    }

    private boolean b(int i) {
        boolean z = false;
        switch (i) {
            case 0:
                if ((this.c & 1) != 0) {
                    z = true;
                }
                return z;
            case 1:
                if ((this.c & 2) != 0) {
                    z = true;
                }
                return z;
            case 2:
                if ((this.c & 4) != 0) {
                    z = true;
                }
                return z;
            case 3:
                if ((this.c & 16) != 0) {
                    z = true;
                }
                return z;
            case 4:
                if ((this.c & 8) != 0) {
                    z = true;
                }
                return z;
            default:
                return false;
        }
    }

    private void c(int i) throws IOException {
        this.b.b((long) i);
        while (!this.z.isEmpty() && ((Integer) this.z.firstKey()).intValue() < i) {
            this.z.pollFirstEntry();
        }
    }

    private void c(ExifTag exifTag) {
        int i = 0;
        if (exifTag.getComponentCount() != 0) {
            short tagId = exifTag.getTagId();
            int ifd = exifTag.getIfd();
            if (tagId == s && a(ifd, ExifInterface.TAG_EXIF_IFD)) {
                if (b(2) || b(3)) {
                    a(2, exifTag.c(0));
                }
            } else if (tagId == t && a(ifd, ExifInterface.TAG_GPS_IFD)) {
                if (b(4)) {
                    a(4, exifTag.c(0));
                }
            } else if (tagId == u && a(ifd, ExifInterface.TAG_INTEROPERABILITY_IFD)) {
                if (b(3)) {
                    a(3, exifTag.c(0));
                }
            } else if (tagId == v && a(ifd, ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT)) {
                if (p()) {
                    a(exifTag.c(0));
                }
            } else if (tagId == w && a(ifd, ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH)) {
                if (p()) {
                    this.j = exifTag;
                }
            } else if (tagId == x && a(ifd, ExifInterface.TAG_STRIP_OFFSETS)) {
                if (p()) {
                    if (exifTag.hasValue()) {
                        while (i < exifTag.getComponentCount()) {
                            b(i, exifTag.c(i));
                            i++;
                        }
                    } else {
                        this.z.put(Integer.valueOf(exifTag.b()), new a(exifTag, false));
                    }
                }
            } else if (tagId == y && a(ifd, ExifInterface.TAG_STRIP_BYTE_COUNTS) && p() && exifTag.hasValue()) {
                this.i = exifTag;
            }
        }
    }

    private boolean p() {
        return (this.c & 32) != 0;
    }

    private boolean q() {
        boolean z = false;
        switch (this.f) {
            case 0:
                if (b(2) || b(4) || b(3) || b(1)) {
                    z = true;
                }
                return z;
            case 1:
                return p();
            case 2:
                return b(3);
            default:
                return false;
        }
    }

    private ExifTag r() throws IOException, ExifInvalidFormatException {
        int i = 1;
        short c = this.b.c();
        short c2 = this.b.c();
        long f = this.b.f();
        if (f <= 2147483647L) {
            int i2 = 1;
        } else {
            boolean z = false;
        }
        if (i2 == 0) {
            throw new ExifInvalidFormatException("Number of component is larger then Integer.MAX_VALUE");
        } else if (ExifTag.isValidType(c2)) {
            ExifTag exifTag = new ExifTag(c, c2, (int) f, this.f, ((int) f) != 0);
            int dataSize = exifTag.getDataSize();
            if (dataSize <= 4) {
                boolean c3 = exifTag.c();
                exifTag.a(false);
                b(exifTag);
                exifTag.a(c3);
                this.b.skip((long) (4 - dataSize));
                exifTag.e(this.b.a() - 4);
            } else {
                long f2 = this.b.f();
                if (f2 <= 2147483647L) {
                    dataSize = 1;
                } else {
                    boolean z2 = false;
                }
                if (dataSize == 0) {
                    throw new ExifInvalidFormatException("offset is larger then Integer.MAX_VALUE");
                }
                if (f2 < ((long) this.p)) {
                    i = 0;
                }
                if (i == 0 && c2 == (short) 7) {
                    byte[] bArr = new byte[((int) f)];
                    if (this.o != null) {
                        System.arraycopy(this.o, ((int) f2) - 8, bArr, 0, (int) f);
                        exifTag.setValue(bArr);
                    }
                } else {
                    exifTag.e((int) f2);
                }
            }
            return exifTag;
        } else {
            Log.w("ExifParser", String.format(Locale.US, "Tag %04x: Invalid data type %d", new Object[]{Short.valueOf(c), Short.valueOf(c2)}));
            this.b.skip(4);
            return null;
        }
    }

    private void s() throws IOException, ExifInvalidFormatException {
        short c = this.b.c();
        if ((short) 18761 == c) {
            this.b.a(ByteOrder.LITTLE_ENDIAN);
        } else if ((short) 19789 != c) {
            throw new ExifInvalidFormatException("Invalid TIFF header");
        } else {
            this.b.a(ByteOrder.BIG_ENDIAN);
        }
        if (this.b.c() != (short) 42) {
            throw new ExifInvalidFormatException("Invalid TIFF header");
        }
    }

    protected int a() throws IOException, ExifInvalidFormatException {
        if (!this.l) {
            return 5;
        }
        int a = this.b.a();
        int i = (this.d + 2) + (this.e * 12);
        if (a >= i) {
            if (a == i) {
                if (this.f == 0) {
                    long k = k();
                    if (b(1) || p()) {
                        if (k != 0) {
                            a(1, k);
                        }
                    }
                } else if (this.z.size() > 0) {
                    ((Integer) this.z.firstEntry().getKey()).intValue();
                    this.b.a();
                }
            }
            while (this.z.size() != 0) {
                Entry pollFirstEntry = this.z.pollFirstEntry();
                Object value = pollFirstEntry.getValue();
                try {
                    c(((Integer) pollFirstEntry.getKey()).intValue());
                    if (value instanceof b) {
                        this.f = ((b) value).a;
                        this.e = this.b.d();
                        this.d = ((Integer) pollFirstEntry.getKey()).intValue();
                        if (((this.e * 12) + this.d) + 2 > this.m) {
                            return 5;
                        }
                        this.k = q();
                        if (((b) value).b) {
                            return 0;
                        }
                        b();
                    } else if (value instanceof c) {
                        this.h = (c) value;
                        return this.h.b;
                    } else {
                        a aVar = (a) value;
                        this.g = aVar.a;
                        if (this.g.getDataType() != (short) 7) {
                            b(this.g);
                            c(this.g);
                        }
                        if (aVar.b) {
                            return 2;
                        }
                    }
                } catch (IOException e) {
                }
            }
            return 5;
        }
        this.g = r();
        if (this.g == null) {
            return a();
        }
        if (this.k) {
            c(this.g);
        }
        return 1;
    }

    protected int a(byte[] bArr) throws IOException {
        return this.b.read(bArr);
    }

    protected String a(int i) throws IOException {
        return a(i, a);
    }

    protected String a(int i, Charset charset) throws IOException {
        return i <= 0 ? BuildConfig.FLAVOR : this.b.a(i, charset);
    }

    protected void a(ExifTag exifTag) {
        if (exifTag.b() >= this.b.a()) {
            this.z.put(Integer.valueOf(exifTag.b()), new a(exifTag, true));
        }
    }

    protected void b() throws IOException, ExifInvalidFormatException {
        int i = 0;
        int i2 = (this.e * 12) + (this.d + 2);
        int a = this.b.a();
        if (a <= i2) {
            if (this.k) {
                while (a < i2) {
                    this.g = r();
                    a += 12;
                    if (this.g != null) {
                        c(this.g);
                    }
                }
            } else {
                c(i2);
            }
            long k = k();
            if (this.f == 0) {
                if (b(1) || p()) {
                    if (k <= 0) {
                        i = 1;
                    }
                    if (i == 0) {
                        a(1, k);
                    }
                }
            }
        }
    }

    protected void b(ExifTag exifTag) throws IOException {
        int i = 0;
        short dataType = exifTag.getDataType();
        if (dataType == (short) 2 || dataType == (short) 7 || dataType == (short) 1) {
            int componentCount = exifTag.getComponentCount();
            if (this.z.size() > 0 && ((Integer) this.z.firstEntry().getKey()).intValue() < componentCount + this.b.a()) {
                Object value = this.z.firstEntry().getValue();
                if (value instanceof c) {
                    Log.w("ExifParser", "Thumbnail overlaps value for tag: \n" + exifTag.toString());
                    Log.w("ExifParser", "Invalid thumbnail offset: " + this.z.pollFirstEntry().getKey());
                } else {
                    if (value instanceof b) {
                        Log.w("ExifParser", "Ifd " + ((b) value).a + " overlaps value for tag: \n" + exifTag.toString());
                    } else if (value instanceof a) {
                        Log.w("ExifParser", "Tag value for tag: \n" + ((a) value).a.toString() + " overlaps value for tag: \n" + exifTag.toString());
                    }
                    int intValue = ((Integer) this.z.firstEntry().getKey()).intValue() - this.b.a();
                    Log.w("ExifParser", "Invalid size of tag: \n" + exifTag.toString() + " setting count to: " + intValue);
                    exifTag.b(intValue);
                }
            }
        }
        int[] iArr;
        switch (exifTag.getDataType()) {
            case (short) 1:
            case (short) 7:
                byte[] bArr = new byte[exifTag.getComponentCount()];
                a(bArr);
                exifTag.setValue(bArr);
                return;
            case (short) 2:
                exifTag.setValue(a(exifTag.getComponentCount()));
                return;
            case (short) 3:
                iArr = new int[exifTag.getComponentCount()];
                componentCount = iArr.length;
                while (i < componentCount) {
                    iArr[i] = j();
                    i++;
                }
                exifTag.setValue(iArr);
                return;
            case (short) 4:
                long[] jArr = new long[exifTag.getComponentCount()];
                componentCount = jArr.length;
                while (i < componentCount) {
                    jArr[i] = k();
                    i++;
                }
                exifTag.setValue(jArr);
                return;
            case (short) 5:
                Rational[] rationalArr = new Rational[exifTag.getComponentCount()];
                componentCount = rationalArr.length;
                while (i < componentCount) {
                    rationalArr[i] = l();
                    i++;
                }
                exifTag.setValue(rationalArr);
                return;
            case (short) 9:
                iArr = new int[exifTag.getComponentCount()];
                componentCount = iArr.length;
                while (i < componentCount) {
                    iArr[i] = m();
                    i++;
                }
                exifTag.setValue(iArr);
                return;
            case (short) 10:
                Rational[] rationalArr2 = new Rational[exifTag.getComponentCount()];
                int length = rationalArr2.length;
                for (intValue = 0; intValue < length; intValue++) {
                    rationalArr2[intValue] = n();
                }
                exifTag.setValue(rationalArr2);
                return;
            default:
                return;
        }
    }

    protected ExifTag c() {
        return this.g;
    }

    protected int d() {
        return this.f;
    }

    protected int e() {
        return this.h.a;
    }

    protected int f() {
        return this.i != null ? (int) this.i.c(0) : 0;
    }

    protected int g() {
        return this.j != null ? (int) this.j.c(0) : 0;
    }

    protected int j() throws IOException {
        return this.b.c() & 65535;
    }

    protected long k() throws IOException {
        return ((long) m()) & 4294967295L;
    }

    protected Rational l() throws IOException {
        return new Rational(k(), k());
    }

    protected int m() throws IOException {
        return this.b.e();
    }

    protected Rational n() throws IOException {
        return new Rational((long) m(), (long) m());
    }

    protected ByteOrder o() {
        return this.b.b();
    }
}
