package com.fyusion.sdk.common.ext.util.exif;

import java.io.BufferedOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;

/* compiled from: Unknown */
class e extends FilterOutputStream {
    private c a;
    private int b = 0;
    private int c;
    private int d;
    private byte[] e = new byte[1];
    private ByteBuffer f = ByteBuffer.allocate(4);
    private final ExifInterface g;

    protected e(OutputStream outputStream, ExifInterface exifInterface) {
        super(new BufferedOutputStream(outputStream, 65536));
        this.g = exifInterface;
    }

    private int a(int i, byte[] bArr, int i2, int i3) {
        int position = i - this.f.position();
        if (i3 > position) {
            i3 = position;
        }
        this.f.put(bArr, i2, i3);
        return i3;
    }

    private int a(h hVar, int i) {
        int d = i + (((hVar.d() * 12) + 2) + 4);
        int i2 = d;
        for (ExifTag exifTag : hVar.b()) {
            if (exifTag.getDataSize() > 4) {
                exifTag.e(i2);
                i2 += exifTag.getDataSize();
            }
        }
        return i2;
    }

    private void a() throws IOException {
        if (this.a != null) {
            ArrayList b = b(this.a);
            b();
            int c = c();
            if (c + 8 <= 65535) {
                j jVar = new j(this.out);
                jVar.a(ByteOrder.BIG_ENDIAN);
                jVar.a((short) -31);
                jVar.a((short) (c + 8));
                jVar.a(1165519206);
                jVar.a((short) 0);
                if (this.a.e() != ByteOrder.BIG_ENDIAN) {
                    jVar.a((short) 18761);
                } else {
                    jVar.a((short) 19789);
                }
                jVar.a(this.a.e());
                jVar.a((short) 42);
                jVar.a(8);
                b(jVar);
                a(jVar);
                Iterator it = b.iterator();
                while (it.hasNext()) {
                    this.a.a((ExifTag) it.next());
                }
                return;
            }
            throw new IOException("Exif header is too large (>64Kb)");
        }
    }

    static void a(ExifTag exifTag, j jVar) throws IOException {
        int i = 0;
        int componentCount;
        switch (exifTag.getDataType()) {
            case (short) 1:
            case (short) 7:
                byte[] bArr = new byte[exifTag.getComponentCount()];
                exifTag.a(bArr);
                jVar.write(bArr);
                return;
            case (short) 2:
                byte[] a = exifTag.a();
                if (a.length != exifTag.getComponentCount()) {
                    jVar.write(a);
                    jVar.write(0);
                    return;
                }
                a[a.length - 1] = (byte) 0;
                jVar.write(a);
                return;
            case (short) 3:
                componentCount = exifTag.getComponentCount();
                while (i < componentCount) {
                    jVar.a((short) ((int) exifTag.c(i)));
                    i++;
                }
                return;
            case (short) 4:
            case (short) 9:
                componentCount = exifTag.getComponentCount();
                while (i < componentCount) {
                    jVar.a((int) exifTag.c(i));
                    i++;
                }
                return;
            case (short) 5:
            case (short) 10:
                componentCount = exifTag.getComponentCount();
                while (i < componentCount) {
                    jVar.a(exifTag.d(i));
                    i++;
                }
                return;
            default:
                return;
        }
    }

    private void a(h hVar, j jVar) throws IOException {
        int i;
        int i2 = 0;
        ExifTag[] b = hVar.b();
        jVar.a((short) b.length);
        for (ExifTag exifTag : b) {
            jVar.a(exifTag.getTagId());
            jVar.a(exifTag.getDataType());
            jVar.a(exifTag.getComponentCount());
            if (exifTag.getDataSize() <= 4) {
                a(exifTag, jVar);
                int dataSize = 4 - exifTag.getDataSize();
                for (i = 0; i < dataSize; i++) {
                    jVar.write(0);
                }
            } else {
                jVar.a(exifTag.b());
            }
        }
        jVar.a(hVar.e());
        i = b.length;
        while (i2 < i) {
            ExifTag exifTag2 = b[i2];
            if (exifTag2.getDataSize() > 4) {
                a(exifTag2, jVar);
            }
            i2++;
        }
    }

    private void a(j jVar) throws IOException {
        if (this.a.b()) {
            jVar.write(this.a.a());
        } else if (this.a.d()) {
            for (int i = 0; i < this.a.c(); i++) {
                jVar.write(this.a.a(i));
            }
        }
    }

    private ArrayList<ExifTag> b(c cVar) {
        ArrayList<ExifTag> arrayList = new ArrayList();
        if (cVar == null || cVar.h() == null) {
            return arrayList;
        }
        for (ExifTag exifTag : cVar.h()) {
            if (exifTag.getValue() == null && !ExifInterface.a(exifTag.getTagId())) {
                cVar.b(exifTag.getTagId(), exifTag.getIfd());
                arrayList.add(exifTag);
            }
        }
        return arrayList;
    }

    private void b() throws IOException {
        h b = this.a.b(0);
        if (b == null) {
            b = new h(0);
            this.a.a(b);
        }
        ExifTag a = this.g.a(ExifInterface.TAG_EXIF_IFD);
        if (a != null) {
            ExifTag a2;
            b.a(a);
            h b2 = this.a.b(2);
            if (b2 == null) {
                b2 = new h(2);
                this.a.a(b2);
            }
            if (this.a.b(4) != null) {
                a2 = this.g.a(ExifInterface.TAG_GPS_IFD);
                if (a2 != null) {
                    b.a(a2);
                } else {
                    throw new IOException("No definition for crucial exif tag: " + ExifInterface.TAG_GPS_IFD);
                }
            }
            if (this.a.b(3) != null) {
                ExifTag a3 = this.g.a(ExifInterface.TAG_INTEROPERABILITY_IFD);
                if (a3 != null) {
                    b2.a(a3);
                } else {
                    throw new IOException("No definition for crucial exif tag: " + ExifInterface.TAG_INTEROPERABILITY_IFD);
                }
            }
            b = this.a.b(1);
            if (this.a.b()) {
                if (b == null) {
                    b = new h(1);
                    this.a.a(b);
                }
                a = this.g.a(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT);
                if (a != null) {
                    b.a(a);
                    a = this.g.a(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH);
                    if (a != null) {
                        a.setValue(this.a.a().length);
                        b.a(a);
                        b.b(ExifInterface.getTrueTagKey(ExifInterface.TAG_STRIP_OFFSETS));
                        b.b(ExifInterface.getTrueTagKey(ExifInterface.TAG_STRIP_BYTE_COUNTS));
                        return;
                    }
                    throw new IOException("No definition for crucial exif tag: " + ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH);
                }
                throw new IOException("No definition for crucial exif tag: " + ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT);
            } else if (this.a.d()) {
                if (b == null) {
                    b = new h(1);
                    this.a.a(b);
                }
                int c = this.a.c();
                a2 = this.g.a(ExifInterface.TAG_STRIP_OFFSETS);
                if (a2 != null) {
                    ExifTag a4 = this.g.a(ExifInterface.TAG_STRIP_BYTE_COUNTS);
                    if (a4 != null) {
                        long[] jArr = new long[c];
                        for (c = 0; c < this.a.c(); c++) {
                            jArr[c] = (long) this.a.a(c).length;
                        }
                        a4.setValue(jArr);
                        b.a(a2);
                        b.a(a4);
                        b.b(ExifInterface.getTrueTagKey(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT));
                        b.b(ExifInterface.getTrueTagKey(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH));
                        return;
                    }
                    throw new IOException("No definition for crucial exif tag: " + ExifInterface.TAG_STRIP_BYTE_COUNTS);
                }
                throw new IOException("No definition for crucial exif tag: " + ExifInterface.TAG_STRIP_OFFSETS);
            } else if (b != null) {
                b.b(ExifInterface.getTrueTagKey(ExifInterface.TAG_STRIP_OFFSETS));
                b.b(ExifInterface.getTrueTagKey(ExifInterface.TAG_STRIP_BYTE_COUNTS));
                b.b(ExifInterface.getTrueTagKey(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT));
                b.b(ExifInterface.getTrueTagKey(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH));
                return;
            } else {
                return;
            }
        }
        throw new IOException("No definition for crucial exif tag: " + ExifInterface.TAG_EXIF_IFD);
    }

    private void b(j jVar) throws IOException {
        a(this.a.b(0), jVar);
        a(this.a.b(2), jVar);
        h b = this.a.b(3);
        if (b != null) {
            a(b, jVar);
        }
        b = this.a.b(4);
        if (b != null) {
            a(b, jVar);
        }
        if (this.a.b(1) != null) {
            a(this.a.b(1), jVar);
        }
    }

    private int c() {
        h b = this.a.b(0);
        int a = a(b, 8);
        b.a(ExifInterface.getTrueTagKey(ExifInterface.TAG_EXIF_IFD)).setValue(a);
        h b2 = this.a.b(2);
        a = a(b2, a);
        h b3 = this.a.b(3);
        if (b3 != null) {
            b2.a(ExifInterface.getTrueTagKey(ExifInterface.TAG_INTEROPERABILITY_IFD)).setValue(a);
            a = a(b3, a);
        }
        b2 = this.a.b(4);
        if (b2 != null) {
            b.a(ExifInterface.getTrueTagKey(ExifInterface.TAG_GPS_IFD)).setValue(a);
            a = a(b2, a);
        }
        b2 = this.a.b(1);
        if (b2 == null) {
            return a;
        }
        b.a(a);
        a = a(b2, a);
        if (this.a.b()) {
            b2.a(ExifInterface.getTrueTagKey(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT)).setValue(a);
            a += this.a.a().length;
        } else if (this.a.d()) {
            long[] jArr = new long[this.a.c()];
            int i = a;
            for (a = 0; a < this.a.c(); a++) {
                jArr[a] = (long) i;
                i += this.a.a(a).length;
            }
            b2.a(ExifInterface.getTrueTagKey(ExifInterface.TAG_STRIP_OFFSETS)).setValue(jArr);
            a = i;
        }
        return a;
    }

    protected void a(c cVar) {
        this.a = cVar;
    }

    public void write(int i) throws IOException {
        this.e[0] = (byte) ((byte) (i & 255));
        write(this.e);
    }

    public void write(byte[] bArr) throws IOException {
        write(bArr, 0, bArr.length);
    }

    public void write(byte[] bArr, int i, int i2) throws IOException {
        while (true) {
            if (this.c > 0 || this.d > 0 || this.b != 2) {
                if (i2 > 0) {
                    int i3;
                    if (this.c > 0) {
                        i3 = i2 <= this.c ? i2 : this.c;
                        i2 -= i3;
                        this.c -= i3;
                        i += i3;
                    }
                    if (this.d > 0) {
                        i3 = i2 <= this.d ? i2 : this.d;
                        this.out.write(bArr, i, i3);
                        i2 -= i3;
                        this.d -= i3;
                        i += i3;
                    }
                    if (i2 != 0) {
                        switch (this.b) {
                            case 0:
                                i3 = a(2, bArr, i, i2);
                                i += i3;
                                i2 -= i3;
                                if (this.f.position() >= 2) {
                                    this.f.rewind();
                                    if (this.f.getShort() == (short) -40) {
                                        this.out.write(this.f.array(), 0, 2);
                                        this.b = 1;
                                        this.f.rewind();
                                        a();
                                        break;
                                    }
                                    throw new IOException("Not a valid jpeg image, cannot write exif");
                                }
                                return;
                            case 1:
                                i3 = a(4, bArr, i, i2);
                                i += i3;
                                i2 -= i3;
                                if (this.f.position() == 2 && this.f.getShort() == (short) -39) {
                                    this.out.write(this.f.array(), 0, 2);
                                    this.f.rewind();
                                }
                                if (this.f.position() >= 4) {
                                    this.f.rewind();
                                    short s = this.f.getShort();
                                    if (s != (short) -31) {
                                        if (!i.a(s)) {
                                            this.out.write(this.f.array(), 0, 4);
                                            this.d = (this.f.getShort() & 65535) - 2;
                                            this.f.rewind();
                                            break;
                                        }
                                        this.out.write(this.f.array(), 0, 4);
                                    } else {
                                        this.c = (this.f.getShort() & 65535) - 2;
                                    }
                                    this.b = 2;
                                    this.f.rewind();
                                } else {
                                    return;
                                }
                            default:
                                break;
                        }
                    }
                    return;
                }
            }
            if (i2 > 0) {
                this.out.write(bArr, i, i2);
            }
            return;
        }
    }
}
