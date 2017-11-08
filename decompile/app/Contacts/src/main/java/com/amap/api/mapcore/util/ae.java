package com.amap.api.mapcore.util;

import android.text.TextUtils;
import com.google.android.gms.location.places.Place;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/* compiled from: UnZipFile */
public class ae {
    private b a;

    /* compiled from: UnZipFile */
    public interface c {
        void a();

        void a(long j);
    }

    /* compiled from: UnZipFile */
    public static class a {
        public boolean a = false;
    }

    /* compiled from: UnZipFile */
    private class b {
        final /* synthetic */ ae a;
        private String b;
        private String c;
        private aa d = null;
        private a e = new a();
        private String f;

        public b(ae aeVar, ab abVar, aa aaVar) {
            this.a = aeVar;
            this.b = abVar.A();
            this.c = abVar.B();
            this.d = aaVar;
        }

        public void a(String str) {
            if (str.length() > 1) {
                this.f = str;
            }
        }

        public String a() {
            return this.b;
        }

        public String b() {
            return this.c;
        }

        public String c() {
            return this.f;
        }

        public aa d() {
            return this.d;
        }

        public a e() {
            return this.e;
        }

        public void f() {
            this.e.a = true;
        }
    }

    public ae(ab abVar, aa aaVar) {
        this.a = new b(this, abVar, aaVar);
    }

    public void a() {
        if (this.a != null) {
            this.a.f();
        }
    }

    public void b() {
        if (this.a != null) {
            a(this.a);
        }
    }

    private static void a(b bVar) {
        if (bVar != null) {
            final aa d = bVar.d();
            if (d != null) {
                d.p();
            }
            Object a = bVar.a();
            Object b = bVar.b();
            if (TextUtils.isEmpty(a) || TextUtils.isEmpty(b)) {
                if (bVar.e().a) {
                    if (d != null) {
                        d.r();
                    }
                } else if (d != null) {
                    d.q();
                }
                return;
            }
            File file = new File(a);
            if (file.exists()) {
                File file2 = new File(b);
                if (!file2.exists() && file2.mkdirs()) {
                }
                c anonymousClass1 = new c() {
                    public void a(long j) {
                        try {
                            if (d != null) {
                                d.a(j);
                            }
                        } catch (Exception e) {
                        }
                    }

                    public void a() {
                        if (d != null) {
                            d.q();
                        }
                    }
                };
                try {
                    if (bVar.e().a && d != null) {
                        d.r();
                    }
                    a(file, file2, anonymousClass1, bVar);
                    if (bVar.e().a) {
                        if (d != null) {
                            d.r();
                        }
                    } else if (d != null) {
                        d.b(bVar.c());
                    }
                } catch (Exception e) {
                    if (bVar.e().a) {
                        if (d != null) {
                            d.r();
                        }
                    } else if (d != null) {
                        d.q();
                    }
                }
                return;
            }
            if (bVar.e().a) {
                if (d != null) {
                    d.r();
                }
            } else if (d != null) {
                d.q();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void a(File file, File file2, c cVar, b bVar) throws Exception {
        StringBuffer stringBuffer = new StringBuffer();
        a e = bVar.e();
        long j = 0;
        if (cVar != null) {
            try {
                InputStream fileInputStream = new FileInputStream(file);
                InputStream checkedInputStream = new CheckedInputStream(fileInputStream, new CRC32());
                ZipInputStream zipInputStream = new ZipInputStream(checkedInputStream);
                while (true) {
                    ZipEntry nextEntry = zipInputStream.getNextEntry();
                    if (nextEntry == null) {
                        break;
                    } else if (e != null && e.a) {
                        break;
                    } else {
                        if (!nextEntry.isDirectory()) {
                            if (!a(nextEntry.getName())) {
                                break;
                            }
                            stringBuffer.append(nextEntry.getName()).append(";");
                        }
                        j += nextEntry.getSize();
                        zipInputStream.closeEntry();
                    }
                }
                zipInputStream.closeEntry();
                zipInputStream.close();
                checkedInputStream.close();
                fileInputStream.close();
                bVar.a(stringBuffer.toString());
                zipInputStream.close();
                checkedInputStream.close();
                fileInputStream.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        InputStream fileInputStream2 = new FileInputStream(file);
        InputStream checkedInputStream2 = new CheckedInputStream(fileInputStream2, new CRC32());
        ZipInputStream zipInputStream2 = new ZipInputStream(checkedInputStream2);
        a(file2, zipInputStream2, j, cVar, e);
        zipInputStream2.close();
        checkedInputStream2.close();
        fileInputStream2.close();
    }

    private static void a(File file, ZipInputStream zipInputStream, long j, c cVar, a aVar) throws Exception {
        int i = 0;
        while (true) {
            ZipEntry nextEntry = zipInputStream.getNextEntry();
            if (nextEntry != null) {
                if (aVar == null || !aVar.a) {
                    String str = file.getPath() + File.separator + nextEntry.getName();
                    if (!a(str)) {
                        break;
                    }
                    int i2;
                    File file2 = new File(str);
                    a(file2);
                    if (nextEntry.isDirectory()) {
                        i2 = file2.mkdirs() ? i : i;
                    } else {
                        i2 = i + a(file2, zipInputStream, (long) i, j, cVar, aVar);
                    }
                    zipInputStream.closeEntry();
                    i = i2;
                } else {
                    zipInputStream.closeEntry();
                    return;
                }
            }
            return;
        }
        if (cVar != null) {
            cVar.a();
        }
    }

    private static boolean a(String str) {
        if (str.contains("../")) {
            return false;
        }
        return true;
    }

    private static int a(File file, ZipInputStream zipInputStream, long j, long j2, c cVar, a aVar) throws Exception {
        int i = 0;
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
        byte[] bArr = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
        while (true) {
            int read = zipInputStream.read(bArr, 0, Place.TYPE_SUBLOCALITY_LEVEL_2);
            if (read == -1) {
                bufferedOutputStream.close();
                return i;
            } else if (aVar != null && aVar.a) {
                bufferedOutputStream.close();
                return i;
            } else {
                bufferedOutputStream.write(bArr, 0, read);
                i += read;
                if ((j2 <= 0 ? 1 : null) == null && cVar != null) {
                    long j3 = ((((long) i) + j) * 100) / j2;
                    if (aVar == null || !aVar.a) {
                        cVar.a(j3);
                    }
                }
            }
        }
    }

    private static void a(File file) {
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            a(parentFile);
            if (!parentFile.mkdir()) {
            }
        }
    }
}
