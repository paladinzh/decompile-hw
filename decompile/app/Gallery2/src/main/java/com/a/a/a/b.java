package com.a.a.a;

import android.os.SystemClock;
import com.a.a.t;
import com.android.gallery3d.gadget.XmlUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/* compiled from: Unknown */
public class b implements com.a.a.b {
    private final Map<String, a> a;
    private long b;
    private final File c;
    private final int d;

    /* compiled from: Unknown */
    static class a {
        public long a;
        public String b;
        public String c;
        public long d;
        public long e;
        public long f;
        public Map<String, String> g;

        private a() {
        }

        public a(String str, com.a.a.b.a aVar) {
            this.b = str;
            this.a = (long) aVar.a.length;
            this.c = aVar.b;
            this.d = aVar.c;
            this.e = aVar.e;
            this.f = aVar.f;
            this.g = aVar.g;
        }

        public static a a(InputStream inputStream) throws IOException {
            a aVar = new a();
            if (b.a(inputStream) == 538051844) {
                aVar.b = b.c(inputStream);
                aVar.c = b.c(inputStream);
                if (aVar.c.equals("")) {
                    aVar.c = null;
                }
                aVar.d = b.b(inputStream);
                aVar.e = b.b(inputStream);
                aVar.f = b.b(inputStream);
                aVar.g = b.d(inputStream);
                return aVar;
            }
            throw new IOException();
        }

        public com.a.a.b.a a(byte[] bArr) {
            com.a.a.b.a aVar = new com.a.a.b.a();
            aVar.a = bArr;
            aVar.b = this.c;
            aVar.c = this.d;
            aVar.e = this.e;
            aVar.f = this.f;
            aVar.g = this.g;
            return aVar;
        }

        public boolean a(OutputStream outputStream) {
            try {
                b.a(outputStream, 538051844);
                b.a(outputStream, this.b);
                b.a(outputStream, this.c != null ? this.c : "");
                b.a(outputStream, this.d);
                b.a(outputStream, this.e);
                b.a(outputStream, this.f);
                b.a(this.g, outputStream);
                outputStream.flush();
                return true;
            } catch (IOException e) {
                t.b("%s", e.toString());
                return false;
            }
        }
    }

    /* compiled from: Unknown */
    private static class b extends FilterInputStream {
        private int a;

        private b(InputStream inputStream) {
            super(inputStream);
            this.a = 0;
        }

        public int read() throws IOException {
            int read = super.read();
            if (read != -1) {
                this.a++;
            }
            return read;
        }

        public int read(byte[] bArr, int i, int i2) throws IOException {
            int read = super.read(bArr, i, i2);
            if (read != -1) {
                this.a += read;
            }
            return read;
        }
    }

    public b(File file) {
        this(file, 5242880);
    }

    public b(File file, int i) {
        this.a = new LinkedHashMap(16, 0.75f, true);
        this.b = 0;
        this.c = file;
        this.d = i;
    }

    static int a(InputStream inputStream) throws IOException {
        return ((((e(inputStream) << 0) | 0) | (e(inputStream) << 8)) | (e(inputStream) << 16)) | (e(inputStream) << 24);
    }

    private void a(int i) {
        if ((this.b + ((long) i) >= ((long) this.d) ? 1 : null) != null) {
            int i2;
            if (t.b) {
                t.a("Pruning old cache entries.", new Object[0]);
            }
            long j = this.b;
            int i3 = 0;
            long elapsedRealtime = SystemClock.elapsedRealtime();
            Iterator it = this.a.entrySet().iterator();
            do {
                i2 = i3;
                if (!it.hasNext()) {
                    break;
                }
                a aVar = (a) ((Entry) it.next()).getValue();
                if (c(aVar.b).delete()) {
                    this.b -= aVar.a;
                } else {
                    t.b("Could not delete cache entry for key=%s, filename=%s", aVar.b, d(aVar.b));
                }
                it.remove();
                i3 = i2 + 1;
            } while (((float) (this.b + ((long) i))) >= ((float) this.d) * 0.9f);
            i2 = i3;
            if (t.b) {
                t.a("pruned %d files, %d bytes, %d ms", Integer.valueOf(i2), Long.valueOf(this.b - j), Long.valueOf(SystemClock.elapsedRealtime() - elapsedRealtime));
            }
        }
    }

    static void a(OutputStream outputStream, int i) throws IOException {
        outputStream.write((i >> 0) & 255);
        outputStream.write((i >> 8) & 255);
        outputStream.write((i >> 16) & 255);
        outputStream.write((i >> 24) & 255);
    }

    static void a(OutputStream outputStream, long j) throws IOException {
        outputStream.write((byte) ((int) (j >>> null)));
        outputStream.write((byte) ((int) (j >>> 8)));
        outputStream.write((byte) ((int) (j >>> 16)));
        outputStream.write((byte) ((int) (j >>> 24)));
        outputStream.write((byte) ((int) (j >>> 32)));
        outputStream.write((byte) ((int) (j >>> 40)));
        outputStream.write((byte) ((int) (j >>> 48)));
        outputStream.write((byte) ((int) (j >>> 56)));
    }

    static void a(OutputStream outputStream, String str) throws IOException {
        byte[] bytes = str.getBytes(XmlUtils.INPUT_ENCODING);
        a(outputStream, (long) bytes.length);
        outputStream.write(bytes, 0, bytes.length);
    }

    private void a(String str, a aVar) {
        if (this.a.containsKey(str)) {
            a aVar2 = (a) this.a.get(str);
            this.b = (aVar.a - aVar2.a) + this.b;
        } else {
            this.b += aVar.a;
        }
        this.a.put(str, aVar);
    }

    static void a(Map<String, String> map, OutputStream outputStream) throws IOException {
        if (map == null) {
            a(outputStream, 0);
            return;
        }
        a(outputStream, map.size());
        for (Entry entry : map.entrySet()) {
            a(outputStream, (String) entry.getKey());
            a(outputStream, (String) entry.getValue());
        }
    }

    private static byte[] a(InputStream inputStream, int i) throws IOException {
        byte[] bArr = new byte[i];
        int i2 = 0;
        while (i2 < i) {
            int read = inputStream.read(bArr, i2, i - i2);
            if (read == -1) {
                break;
            }
            i2 += read;
        }
        if (i2 == i) {
            return bArr;
        }
        throw new IOException("Expected " + i + " bytes, read " + i2 + " bytes");
    }

    static long b(InputStream inputStream) throws IOException {
        return (((((((((((long) e(inputStream)) & 255) << null) | 0) | ((((long) e(inputStream)) & 255) << 8)) | ((((long) e(inputStream)) & 255) << 16)) | ((((long) e(inputStream)) & 255) << 24)) | ((((long) e(inputStream)) & 255) << 32)) | ((((long) e(inputStream)) & 255) << 40)) | ((((long) e(inputStream)) & 255) << 48)) | ((((long) e(inputStream)) & 255) << 56);
    }

    static String c(InputStream inputStream) throws IOException {
        return new String(a(inputStream, (int) b(inputStream)), XmlUtils.INPUT_ENCODING);
    }

    private String d(String str) {
        int length = str.length() / 2;
        return String.valueOf(str.substring(0, length).hashCode()) + String.valueOf(str.substring(length).hashCode());
    }

    static Map<String, String> d(InputStream inputStream) throws IOException {
        int i = 0;
        int a = a(inputStream);
        Map<String, String> hashMap = a != 0 ? new HashMap(a) : Collections.emptyMap();
        while (i < a) {
            hashMap.put(c(inputStream).intern(), c(inputStream).intern());
            i++;
        }
        return hashMap;
    }

    private static int e(InputStream inputStream) throws IOException {
        int read = inputStream.read();
        if (read != -1) {
            return read;
        }
        throw new EOFException();
    }

    private void e(String str) {
        a aVar = (a) this.a.get(str);
        if (aVar != null) {
            this.b -= aVar.a;
            this.a.remove(str);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized com.a.a.b.a a(String str) {
        b bVar;
        IOException e;
        Throwable th;
        Object obj = 1;
        synchronized (this) {
            a aVar = (a) this.a.get(str);
            if (aVar != null) {
                File c = c(str);
                if (!c.exists()) {
                    if (c.length() < 1) {
                        obj = null;
                    }
                    if (obj == null) {
                        return null;
                    }
                }
                try {
                    bVar = new b(new BufferedInputStream(new FileInputStream(c)));
                    try {
                        a.a((InputStream) bVar);
                        com.a.a.b.a a = aVar.a(a((InputStream) bVar, (int) (c.length() - ((long) bVar.a))));
                        if (bVar != null) {
                            try {
                                bVar.close();
                            } catch (IOException e2) {
                                return null;
                            }
                        }
                    } catch (IOException e3) {
                        e = e3;
                        try {
                            t.b("%s: %s", c.getAbsolutePath(), e.toString());
                            b(str);
                            if (bVar != null) {
                                try {
                                    bVar.close();
                                } catch (IOException e4) {
                                    return null;
                                }
                            }
                            return null;
                        } catch (Throwable th2) {
                            th = th2;
                            if (bVar != null) {
                                try {
                                    bVar.close();
                                } catch (IOException e5) {
                                    return null;
                                }
                            }
                            throw th;
                        }
                    }
                } catch (IOException e6) {
                    e = e6;
                    bVar = null;
                    t.b("%s: %s", c.getAbsolutePath(), e.toString());
                    b(str);
                    if (bVar != null) {
                        bVar.close();
                    }
                    return null;
                } catch (Throwable th3) {
                    th = th3;
                    bVar = null;
                    if (bVar != null) {
                        bVar.close();
                    }
                    throw th;
                }
            } else {
                return null;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void a() {
        Throwable th;
        int i = 0;
        BufferedInputStream bufferedInputStream = null;
        synchronized (this) {
            if (this.c.exists()) {
                File[] listFiles = this.c.listFiles();
                if (listFiles != null) {
                    int length = listFiles.length;
                    while (true) {
                        int i2 = i;
                        if (i2 >= length) {
                            return;
                        }
                        File file = listFiles[i2];
                        BufferedInputStream bufferedInputStream2;
                        try {
                            bufferedInputStream2 = new BufferedInputStream(new FileInputStream(file));
                            try {
                                a a = a.a((InputStream) bufferedInputStream2);
                                a.a = file.length();
                                a(a.b, a);
                                if (bufferedInputStream2 != null) {
                                    try {
                                        bufferedInputStream2.close();
                                    } catch (IOException e) {
                                    }
                                }
                            } catch (IOException e2) {
                                if (file != null) {
                                    try {
                                        file.delete();
                                    } catch (Throwable th2) {
                                        Throwable th3 = th2;
                                        bufferedInputStream = bufferedInputStream2;
                                        th = th3;
                                    }
                                }
                                if (bufferedInputStream2 != null) {
                                    try {
                                        bufferedInputStream2.close();
                                    } catch (IOException e3) {
                                    }
                                }
                                i = i2 + 1;
                            }
                        } catch (IOException e4) {
                            bufferedInputStream2 = null;
                            if (file != null) {
                                file.delete();
                            }
                            if (bufferedInputStream2 != null) {
                                bufferedInputStream2.close();
                            }
                            i = i2 + 1;
                        } catch (Throwable th4) {
                            th = th4;
                        }
                        i = i2 + 1;
                    }
                } else {
                    return;
                }
            } else if (!this.c.mkdirs()) {
                t.c("Unable to create cache dir %s", this.c.getAbsolutePath());
            }
        }
        if (bufferedInputStream != null) {
            try {
                bufferedInputStream.close();
            } catch (IOException e5) {
            }
        }
        throw th;
        throw th;
    }

    public synchronized void a(String str, com.a.a.b.a aVar) {
        a(aVar.a.length);
        File c = c(str);
        try {
            OutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(c));
            a aVar2 = new a(str, aVar);
            aVar2.a(bufferedOutputStream);
            bufferedOutputStream.write(aVar.a);
            bufferedOutputStream.close();
            a(str, aVar2);
        } catch (IOException e) {
            t.b("%s", e.toString());
            if (!c.delete()) {
                t.b("Could not clean up file %s", c.getAbsolutePath());
            }
        }
    }

    public synchronized void b(String str) {
        boolean delete = c(str).delete();
        e(str);
        if (!delete) {
            t.b("Could not delete cache entry for key=%s, filename=%s", str, d(str));
        }
    }

    public File c(String str) {
        return new File(this.c, d(str));
    }
}
