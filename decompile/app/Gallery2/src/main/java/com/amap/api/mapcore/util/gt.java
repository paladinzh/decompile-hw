package com.amap.api.mapcore.util;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/* compiled from: DiskLruCache */
public final class gt implements Closeable {
    static final Pattern a = Pattern.compile("[a-z0-9_-]{1,120}");
    private static final OutputStream q = new OutputStream() {
        public void write(int i) throws IOException {
        }
    };
    final ThreadPoolExecutor b = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue());
    private final File c;
    private final File d;
    private final File e;
    private final File f;
    private final int g;
    private long h;
    private final int i;
    private long j = 0;
    private Writer k;
    private final LinkedHashMap<String, c> l = new LinkedHashMap(0, 0.75f, true);
    private int m;
    private gu n;
    private long o = 0;
    private final Callable<Void> p = new Callable<Void>(this) {
        final /* synthetic */ gt a;

        {
            this.a = r1;
        }

        public /* synthetic */ Object call() throws Exception {
            return a();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public Void a() throws Exception {
            synchronized (this.a) {
                if (this.a.k != null) {
                    this.a.j();
                    if (this.a.h()) {
                        this.a.g();
                        this.a.m = 0;
                    }
                } else {
                    return null;
                }
            }
        }
    };

    /* compiled from: DiskLruCache */
    public final class a {
        final /* synthetic */ gt a;
        private final c b;
        private final boolean[] c;
        private boolean d;
        private boolean e;

        /* compiled from: DiskLruCache */
        private class a extends FilterOutputStream {
            final /* synthetic */ a a;

            private a(a aVar, OutputStream outputStream) {
                this.a = aVar;
                super(outputStream);
            }

            public void write(int i) {
                try {
                    this.out.write(i);
                } catch (IOException e) {
                    this.a.d = true;
                }
            }

            public void write(byte[] bArr, int i, int i2) {
                try {
                    this.out.write(bArr, i, i2);
                } catch (IOException e) {
                    this.a.d = true;
                }
            }

            public void close() {
                try {
                    this.out.close();
                } catch (IOException e) {
                    this.a.d = true;
                }
            }

            public void flush() {
                try {
                    this.out.flush();
                } catch (IOException e) {
                    this.a.d = true;
                }
            }
        }

        private a(gt gtVar, c cVar) {
            boolean[] zArr;
            this.a = gtVar;
            this.b = cVar;
            if (cVar.d) {
                zArr = null;
            } else {
                zArr = new boolean[gtVar.i];
            }
            this.c = zArr;
        }

        public OutputStream a(int i) throws IOException {
            if (i >= 0 && i < this.a.i) {
                OutputStream aVar;
                synchronized (this.a) {
                    if (this.b.e == this) {
                        OutputStream fileOutputStream;
                        if (!this.b.d) {
                            this.c[i] = true;
                        }
                        File b = this.b.b(i);
                        try {
                            fileOutputStream = new FileOutputStream(b);
                        } catch (FileNotFoundException e) {
                            this.a.c.mkdirs();
                            try {
                                fileOutputStream = new FileOutputStream(b);
                            } catch (FileNotFoundException e2) {
                                return gt.q;
                            }
                        }
                        aVar = new a(fileOutputStream);
                    } else {
                        throw new IllegalStateException();
                    }
                }
                return aVar;
            }
            throw new IllegalArgumentException("Expected index " + i + " to " + "be greater than 0 and less than the maximum value count " + "of " + this.a.i);
        }

        public void a() throws IOException {
            if (this.d) {
                this.a.a(this, false);
                this.a.c(this.b.b);
            } else {
                this.a.a(this, true);
            }
            this.e = true;
        }

        public void b() throws IOException {
            this.a.a(this, false);
        }
    }

    /* compiled from: DiskLruCache */
    public final class b implements Closeable {
        final /* synthetic */ gt a;
        private final String b;
        private final long c;
        private final InputStream[] d;
        private final long[] e;

        private b(gt gtVar, String str, long j, InputStream[] inputStreamArr, long[] jArr) {
            this.a = gtVar;
            this.b = str;
            this.c = j;
            this.d = inputStreamArr;
            this.e = jArr;
        }

        public InputStream a(int i) {
            return this.d[i];
        }

        public void close() {
            for (Closeable a : this.d) {
                gw.a(a);
            }
        }
    }

    /* compiled from: DiskLruCache */
    private final class c {
        final /* synthetic */ gt a;
        private final String b;
        private final long[] c;
        private boolean d;
        private a e;
        private long f;

        private c(gt gtVar, String str) {
            this.a = gtVar;
            this.b = str;
            this.c = new long[gtVar.i];
        }

        public String a() throws IOException {
            StringBuilder stringBuilder = new StringBuilder();
            for (long append : this.c) {
                stringBuilder.append(' ').append(append);
            }
            return stringBuilder.toString();
        }

        private void a(String[] strArr) throws IOException {
            if (strArr.length == this.a.i) {
                int i = 0;
                while (i < strArr.length) {
                    try {
                        this.c[i] = Long.parseLong(strArr[i]);
                        i++;
                    } catch (NumberFormatException e) {
                        throw b(strArr);
                    }
                }
                return;
            }
            throw b(strArr);
        }

        private IOException b(String[] strArr) throws IOException {
            throw new IOException("unexpected journal line: " + Arrays.toString(strArr));
        }

        public File a(int i) {
            return new File(this.a.c, this.b + "." + i);
        }

        public File b(int i) {
            return new File(this.a.c, this.b + "." + i + ".tmp");
        }
    }

    public void a(gu guVar) {
        this.n = guVar;
    }

    private gt(File file, int i, int i2, long j) {
        this.c = file;
        this.g = i;
        this.d = new File(file, "journal");
        this.e = new File(file, "journal.tmp");
        this.f = new File(file, "journal.bkp");
        this.i = i2;
        this.h = j;
    }

    public static gt a(File file, int i, int i2, long j) throws IOException {
        boolean z = true;
        if (j <= 0) {
            z = false;
        }
        if (!z) {
            throw new IllegalArgumentException("maxSize <= 0");
        } else if (i2 > 0) {
            File file2 = new File(file, "journal.bkp");
            if (file2.exists()) {
                File file3 = new File(file, "journal");
                if (file3.exists()) {
                    file2.delete();
                } else {
                    a(file2, file3, false);
                }
            }
            gt gtVar = new gt(file, i, i2, j);
            if (gtVar.d.exists()) {
                try {
                    gtVar.e();
                    gtVar.f();
                    gtVar.k = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(gtVar.d, true), gw.a));
                    return gtVar;
                } catch (Throwable th) {
                    gtVar.c();
                }
            }
            file.mkdirs();
            gtVar = new gt(file, i, i2, j);
            gtVar.g();
            return gtVar;
        } else {
            throw new IllegalArgumentException("valueCount <= 0");
        }
    }

    private void e() throws IOException {
        int i = 0;
        Closeable gvVar = new gv(new FileInputStream(this.d), gw.a);
        try {
            String a = gvVar.a();
            String a2 = gvVar.a();
            String a3 = gvVar.a();
            String a4 = gvVar.a();
            String a5 = gvVar.a();
            if ("libcore.io.DiskLruCache".equals(a)) {
                if ("1".equals(a2) && Integer.toString(this.g).equals(a3) && Integer.toString(this.i).equals(a4) && "".equals(a5)) {
                    while (true) {
                        d(gvVar.a());
                        i++;
                    }
                }
            }
            throw new IOException("unexpected journal header: [" + a + ", " + a2 + ", " + a4 + ", " + a5 + "]");
        } catch (EOFException e) {
            this.m = i - this.l.size();
            gw.a(gvVar);
        } catch (Throwable th) {
            gw.a(gvVar);
        }
    }

    private void d(String str) throws IOException {
        int indexOf = str.indexOf(32);
        if (indexOf != -1) {
            String substring;
            int i = indexOf + 1;
            int indexOf2 = str.indexOf(32, i);
            if (indexOf2 != -1) {
                substring = str.substring(i, indexOf2);
            } else {
                String substring2 = str.substring(i);
                if (indexOf == "REMOVE".length() && str.startsWith("REMOVE")) {
                    this.l.remove(substring2);
                    return;
                }
                substring = substring2;
            }
            c cVar = (c) this.l.get(substring);
            if (cVar == null) {
                cVar = new c(substring);
                this.l.put(substring, cVar);
            }
            if (indexOf2 != -1 && indexOf == "CLEAN".length() && str.startsWith("CLEAN")) {
                String[] split = str.substring(indexOf2 + 1).split(" ");
                cVar.d = true;
                cVar.e = null;
                cVar.a(split);
            } else if (indexOf2 == -1 && indexOf == "DIRTY".length() && str.startsWith("DIRTY")) {
                cVar.e = new a(cVar);
            } else {
                if (indexOf2 == -1 && indexOf == "READ".length()) {
                    if (!str.startsWith("READ")) {
                    }
                }
                throw new IOException("unexpected journal line: " + str);
            }
            return;
        }
        throw new IOException("unexpected journal line: " + str);
    }

    private void f() throws IOException {
        a(this.e);
        Iterator it = this.l.values().iterator();
        while (it.hasNext()) {
            c cVar = (c) it.next();
            int i;
            if (cVar.e != null) {
                cVar.e = null;
                for (i = 0; i < this.i; i++) {
                    a(cVar.a(i));
                    a(cVar.b(i));
                }
                it.remove();
            } else {
                for (i = 0; i < this.i; i++) {
                    this.j += cVar.c[i];
                }
            }
        }
    }

    private synchronized void g() throws IOException {
        if (this.k != null) {
            this.k.close();
        }
        Writer bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.e), gw.a));
        bufferedWriter.write("libcore.io.DiskLruCache");
        bufferedWriter.write("\n");
        bufferedWriter.write("1");
        bufferedWriter.write("\n");
        bufferedWriter.write(Integer.toString(this.g));
        bufferedWriter.write("\n");
        bufferedWriter.write(Integer.toString(this.i));
        bufferedWriter.write("\n");
        bufferedWriter.write("\n");
        for (c cVar : this.l.values()) {
            if (cVar.e == null) {
                bufferedWriter.write("CLEAN " + cVar.b + cVar.a() + '\n');
            } else {
                try {
                    bufferedWriter.write("DIRTY " + cVar.b + '\n');
                } finally {
                    bufferedWriter.close();
                }
            }
        }
        if (this.d.exists()) {
            a(this.d, this.f, true);
        }
        a(this.e, this.d, false);
        this.f.delete();
        this.k = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.d, true), gw.a));
    }

    private static void a(File file) throws IOException {
        if (file.exists() && !file.delete()) {
            throw new IOException();
        }
    }

    private static void a(File file, File file2, boolean z) throws IOException {
        if (z) {
            a(file2);
        }
        if (!file.renameTo(file2)) {
            throw new IOException();
        }
    }

    public synchronized b a(String str) throws IOException {
        int i = 0;
        synchronized (this) {
            i();
            e(str);
            c cVar = (c) this.l.get(str);
            if (cVar == null) {
                return null;
            } else if (cVar.d) {
                r6 = new InputStream[this.i];
                int i2 = 0;
                while (i2 < this.i) {
                    try {
                        r6[i2] = new FileInputStream(cVar.a(i2));
                        i2++;
                    } catch (FileNotFoundException e) {
                        while (i < this.i) {
                            InputStream[] inputStreamArr;
                            if (inputStreamArr[i] == null) {
                                break;
                            }
                            gw.a(inputStreamArr[i]);
                            i++;
                        }
                        return null;
                    }
                }
                this.m++;
                this.k.append("READ " + str + '\n');
                if (h()) {
                    this.b.submit(this.p);
                }
                b bVar = new b(str, cVar.f, inputStreamArr, cVar.c);
                return bVar;
            } else {
                return null;
            }
        }
    }

    public a b(String str) throws IOException {
        return a(str, -1);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized a a(String str, long j) throws IOException {
        i();
        e(str);
        c cVar = (c) this.l.get(str);
        if (j != -1) {
            if (cVar != null) {
            }
        }
        if (cVar == null) {
            cVar = new c(str);
            this.l.put(str, cVar);
        } else if (cVar.e != null) {
            return null;
        }
        a aVar = new a(cVar);
        cVar.e = aVar;
        this.k.write("DIRTY " + str + '\n');
        this.k.flush();
        return aVar;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void a(a aVar, boolean z) throws IOException {
        Object obj = 1;
        synchronized (this) {
            c a = aVar.b;
            if (a.e == aVar) {
                int i;
                long length;
                if (z) {
                    if (!a.d) {
                        i = 0;
                        while (i < this.i) {
                            if (!aVar.c[i]) {
                                aVar.b();
                                throw new IllegalStateException("Newly created entry didn't create value for index " + i);
                            } else if (a.b(i).exists()) {
                                i++;
                            } else {
                                aVar.b();
                                return;
                            }
                        }
                    }
                }
                for (i = 0; i < this.i; i++) {
                    File b = a.b(i);
                    if (!z) {
                        a(b);
                    } else if (b.exists()) {
                        File a2 = a.a(i);
                        b.renameTo(a2);
                        long j = a.c[i];
                        length = a2.length();
                        a.c[i] = length;
                        this.j = length + (this.j - j);
                    }
                }
                this.m++;
                a.e = null;
                if ((a.d | z) == 0) {
                    this.l.remove(a.b);
                    this.k.write("REMOVE " + a.b + '\n');
                } else {
                    a.d = true;
                    this.k.write("CLEAN " + a.b + a.a() + '\n');
                    if (z) {
                        length = this.o;
                        this.o = 1 + length;
                        a.f = length;
                    }
                }
                this.k.flush();
                if (this.j <= this.h) {
                    obj = null;
                }
                if (obj != null || h()) {
                    this.b.submit(this.p);
                }
            } else {
                throw new IllegalStateException();
            }
        }
    }

    private boolean h() {
        if (this.m >= 2000 && this.m >= this.l.size()) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean c(String str) throws IOException {
        int i = 0;
        synchronized (this) {
            i();
            e(str);
            c cVar = (c) this.l.get(str);
            if (cVar != null) {
                if (cVar.e == null) {
                    while (i < this.i) {
                        File a = cVar.a(i);
                        if (a.exists() && !a.delete()) {
                            throw new IOException("failed to delete " + a);
                        }
                        this.j -= cVar.c[i];
                        cVar.c[i] = 0;
                        i++;
                    }
                    this.m++;
                    this.k.append("REMOVE " + str + '\n');
                    this.l.remove(str);
                    if (h()) {
                        this.b.submit(this.p);
                    }
                    return true;
                }
            }
        }
    }

    public synchronized boolean a() {
        return this.k == null;
    }

    private void i() {
        if (this.k == null) {
            throw new IllegalStateException("cache is closed");
        }
    }

    public synchronized void b() throws IOException {
        i();
        j();
        this.k.flush();
    }

    public synchronized void close() throws IOException {
        if (this.k != null) {
            Iterator it = new ArrayList(this.l.values()).iterator();
            while (it.hasNext()) {
                c cVar = (c) it.next();
                if (cVar.e != null) {
                    cVar.e.b();
                }
            }
            j();
            this.k.close();
            this.k = null;
        }
    }

    private void j() throws IOException {
        while (true) {
            if ((this.j <= this.h ? 1 : null) == null) {
                String str = (String) ((Entry) this.l.entrySet().iterator().next()).getKey();
                c(str);
                if (this.n != null) {
                    this.n.a(str);
                }
            } else {
                return;
            }
        }
    }

    public void c() throws IOException {
        close();
        gw.a(this.c);
    }

    private void e(String str) {
        if (!a.matcher(str).matches()) {
            throw new IllegalArgumentException("keys must match regex [a-z0-9_-]{1,120}: \"" + str + "\"");
        }
    }
}
