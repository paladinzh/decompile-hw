package com.fyusion.sdk.viewer.internal.b.b.a;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/* compiled from: Unknown */
public final class d implements Closeable {
    final ThreadPoolExecutor a = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue(), new a());
    private final File b;
    private final File c;
    private final File d;
    private final File e;
    private final int f;
    private long g;
    private final int h;
    private long i = 0;
    private Writer j;
    private final LinkedHashMap<String, c> k = new LinkedHashMap(0, 0.75f, true);
    private int l;
    private long m = 0;
    private final Callable<Void> n = new Callable<Void>(this) {
        final /* synthetic */ d a;

        {
            this.a = r1;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public Void a() throws Exception {
            synchronized (this.a) {
                if (this.a.j != null) {
                    this.a.g();
                    if (this.a.e()) {
                        this.a.d();
                        this.a.l = 0;
                    }
                } else {
                    return null;
                }
            }
        }

        public /* synthetic */ Object call() throws Exception {
            return a();
        }
    };

    /* compiled from: Unknown */
    private static final class a implements ThreadFactory {
        private a() {
        }

        public synchronized Thread newThread(Runnable runnable) {
            Thread thread;
            thread = new Thread(runnable, "disk-lru-cache-thread");
            thread.setPriority(1);
            return thread;
        }
    }

    /* compiled from: Unknown */
    public final class b {
        final /* synthetic */ d a;
        private final c b;
        private final boolean[] c;
        private boolean d;

        private b(d dVar, c cVar) {
            this.a = dVar;
            this.b = cVar;
            this.c = !cVar.f ? new boolean[dVar.h] : null;
        }

        public File a(int i) throws IOException {
            File b;
            synchronized (this.a) {
                if (this.b.g == this) {
                    if (!this.b.f) {
                        this.c[i] = true;
                    }
                    b = this.b.b(i);
                    if (!this.a.b.exists()) {
                        this.a.b.mkdirs();
                    }
                } else {
                    throw new IllegalStateException();
                }
            }
            return b;
        }

        public void a() throws IOException {
            this.a.a(this, true);
            this.d = true;
        }

        public void b() throws IOException {
            this.a.a(this, false);
        }

        public void c() {
            if (!this.d) {
                try {
                    b();
                } catch (IOException e) {
                }
            }
        }
    }

    /* compiled from: Unknown */
    private final class c {
        File[] a;
        File[] b;
        final /* synthetic */ d c;
        private final String d;
        private final long[] e;
        private boolean f;
        private b g;
        private long h;

        private c(d dVar, String str) {
            this.c = dVar;
            this.d = str;
            this.e = new long[dVar.h];
            this.a = new File[dVar.h];
            this.b = new File[dVar.h];
            StringBuilder append = new StringBuilder(str).append('.');
            int length = append.length();
            for (int i = 0; i < dVar.h; i++) {
                append.append(i);
                this.a[i] = new File(dVar.b, append.toString());
                append.append(".tmp");
                this.b[i] = new File(dVar.b, append.toString());
                append.setLength(length);
            }
        }

        private void a(String[] strArr) throws IOException {
            if (strArr.length == this.c.h) {
                int i = 0;
                while (i < strArr.length) {
                    try {
                        this.e[i] = Long.parseLong(strArr[i]);
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
            return this.a[i];
        }

        public String a() throws IOException {
            StringBuilder stringBuilder = new StringBuilder();
            for (long append : this.e) {
                stringBuilder.append(' ').append(append);
            }
            return stringBuilder.toString();
        }

        public File b(int i) {
            return this.b[i];
        }
    }

    /* compiled from: Unknown */
    public final class d {
        final /* synthetic */ d a;
        private final String b;
        private final long c;
        private final long[] d;
        private final File[] e;

        private d(d dVar, String str, long j, File[] fileArr, long[] jArr) {
            this.a = dVar;
            this.b = str;
            this.c = j;
            this.e = fileArr;
            this.d = jArr;
        }

        public File a(int i) {
            return this.e[i];
        }
    }

    private d(File file, int i, int i2, long j) {
        this.b = file;
        this.f = i;
        this.c = new File(file, "journal");
        this.d = new File(file, "journal.tmp");
        this.e = new File(file, "journal.bkp");
        this.h = i2;
        this.g = j;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized b a(String str, long j) throws IOException {
        f();
        c cVar = (c) this.k.get(str);
        if (j != -1) {
            if (cVar != null) {
            }
        }
        if (cVar == null) {
            cVar = new c(str);
            this.k.put(str, cVar);
        } else if (cVar.g != null) {
            return null;
        }
        b bVar = new b(cVar);
        cVar.g = bVar;
        this.j.append("DIRTY");
        this.j.append(' ');
        this.j.append(str);
        this.j.append('\n');
        this.j.flush();
        return bVar;
    }

    public static d a(File file, int i, int i2, long j) throws IOException {
        if (!(j > 0)) {
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
            d dVar = new d(file, i, i2, j);
            if (dVar.c.exists()) {
                try {
                    dVar.b();
                    dVar.c();
                    return dVar;
                } catch (IOException e) {
                    System.out.println("DiskLruCache " + file + " is corrupt: " + e.getMessage() + ", removing");
                    dVar.a();
                }
            }
            file.mkdirs();
            dVar = new d(file, i, i2, j);
            dVar.d();
            return dVar;
        } else {
            throw new IllegalArgumentException("valueCount <= 0");
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void a(b bVar, boolean z) throws IOException {
        Object obj = 1;
        synchronized (this) {
            c a = bVar.b;
            if (a.g == bVar) {
                int i;
                long length;
                if (z) {
                    if (!a.f) {
                        i = 0;
                        while (i < this.h) {
                            if (!bVar.c[i]) {
                                bVar.b();
                                throw new IllegalStateException("Newly created entry didn't create value for index " + i);
                            } else if (a.b(i).exists()) {
                                i++;
                            } else {
                                bVar.b();
                                return;
                            }
                        }
                    }
                }
                for (i = 0; i < this.h; i++) {
                    File b = a.b(i);
                    if (!z) {
                        a(b);
                    } else if (b.exists()) {
                        File a2 = a.a(i);
                        b.renameTo(a2);
                        long j = a.e[i];
                        length = a2.length();
                        a.e[i] = length;
                        this.i = length + (this.i - j);
                    }
                }
                this.l++;
                a.g = null;
                if ((a.f | z) == 0) {
                    this.k.remove(a.d);
                    this.j.append("REMOVE");
                    this.j.append(' ');
                    this.j.append(a.d);
                    this.j.append('\n');
                } else {
                    a.f = true;
                    this.j.append("CLEAN");
                    this.j.append(' ');
                    this.j.append(a.d);
                    this.j.append(a.a());
                    this.j.append('\n');
                    if (z) {
                        length = this.m;
                        this.m = 1 + length;
                        a.h = length;
                    }
                }
                this.j.flush();
                if (this.i <= this.g) {
                    obj = null;
                }
                if (obj != null || e()) {
                    this.a.submit(this.n);
                }
            } else {
                throw new IllegalStateException();
            }
        }
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

    private void b() throws IOException {
        int i = 0;
        Closeable nVar = new n(new FileInputStream(this.c), o.a);
        try {
            String a = nVar.a();
            String a2 = nVar.a();
            String a3 = nVar.a();
            String a4 = nVar.a();
            String a5 = nVar.a();
            if ("libcore.io.DiskLruCache".equals(a)) {
                if ("1".equals(a2) && Integer.toString(this.f).equals(a3) && Integer.toString(this.h).equals(a4) && "".equals(a5)) {
                    while (true) {
                        d(nVar.a());
                        i++;
                    }
                }
            }
            throw new IOException("unexpected journal header: [" + a + ", " + a2 + ", " + a4 + ", " + a5 + "]");
        } catch (EOFException e) {
            this.l = i - this.k.size();
            if (nVar.b()) {
                d();
            } else {
                this.j = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.c, true), o.a));
            }
            o.a(nVar);
        } catch (Throwable th) {
            o.a(nVar);
        }
    }

    private void c() throws IOException {
        a(this.d);
        Iterator it = this.k.values().iterator();
        while (it.hasNext()) {
            c cVar = (c) it.next();
            int i;
            if (cVar.g != null) {
                cVar.g = null;
                for (i = 0; i < this.h; i++) {
                    a(cVar.a(i));
                    a(cVar.b(i));
                }
                it.remove();
            } else {
                for (i = 0; i < this.h; i++) {
                    this.i += cVar.e[i];
                }
            }
        }
    }

    private synchronized void d() throws IOException {
        if (this.j != null) {
            this.j.close();
        }
        Writer bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.d), o.a));
        bufferedWriter.write("libcore.io.DiskLruCache");
        bufferedWriter.write("\n");
        bufferedWriter.write("1");
        bufferedWriter.write("\n");
        bufferedWriter.write(Integer.toString(this.f));
        bufferedWriter.write("\n");
        bufferedWriter.write(Integer.toString(this.h));
        bufferedWriter.write("\n");
        bufferedWriter.write("\n");
        for (c cVar : this.k.values()) {
            if (cVar.g == null) {
                bufferedWriter.write("CLEAN " + cVar.d + cVar.a() + '\n');
            } else {
                try {
                    bufferedWriter.write("DIRTY " + cVar.d + '\n');
                } finally {
                    bufferedWriter.close();
                }
            }
        }
        if (this.c.exists()) {
            a(this.c, this.e, true);
        }
        a(this.d, this.c, false);
        this.e.delete();
        this.j = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.c, true), o.a));
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
                substring = str.substring(i);
                if (indexOf == "REMOVE".length() && str.startsWith("REMOVE")) {
                    this.k.remove(substring);
                    return;
                }
            }
            String str2 = substring;
            c cVar = (c) this.k.get(str2);
            if (cVar == null) {
                cVar = new c(str2);
                this.k.put(str2, cVar);
            }
            if (indexOf2 != -1 && indexOf == "CLEAN".length() && str.startsWith("CLEAN")) {
                String[] split = str.substring(indexOf2 + 1).split(" ");
                cVar.f = true;
                cVar.g = null;
                cVar.a(split);
            } else if (indexOf2 == -1 && indexOf == "DIRTY".length() && str.startsWith("DIRTY")) {
                cVar.g = new b(cVar);
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

    private boolean e() {
        return this.l >= 2000 && this.l >= this.k.size();
    }

    private void f() {
        if (this.j == null) {
            throw new IllegalStateException("cache is closed");
        }
    }

    private void g() throws IOException {
        while (true) {
            if ((this.i <= this.g ? 1 : null) == null) {
                c((String) ((Entry) this.k.entrySet().iterator().next()).getKey());
            } else {
                return;
            }
        }
    }

    public synchronized d a(String str) throws IOException {
        int i = 0;
        synchronized (this) {
            f();
            c cVar = (c) this.k.get(str);
            if (cVar == null) {
                return null;
            } else if (cVar.f) {
                File[] fileArr = cVar.a;
                int length = fileArr.length;
                while (i < length) {
                    if (fileArr[i].exists()) {
                        i++;
                    } else {
                        return null;
                    }
                }
                this.l++;
                this.j.append("READ");
                this.j.append(' ');
                this.j.append(str);
                this.j.append('\n');
                if (e()) {
                    this.a.submit(this.n);
                }
                d dVar = new d(str, cVar.h, cVar.a, cVar.e);
                return dVar;
            } else {
                return null;
            }
        }
    }

    public void a() throws IOException {
        close();
        o.a(this.b);
    }

    public b b(String str) throws IOException {
        return a(str, -1);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean c(String str) throws IOException {
        int i = 0;
        synchronized (this) {
            f();
            c cVar = (c) this.k.get(str);
            if (cVar != null) {
                if (cVar.g == null) {
                    while (i < this.h) {
                        File a = cVar.a(i);
                        if (a.exists() && !a.delete()) {
                            throw new IOException("failed to delete " + a);
                        }
                        this.i -= cVar.e[i];
                        cVar.e[i] = 0;
                        i++;
                    }
                    this.l++;
                    this.j.append("REMOVE");
                    this.j.append(' ');
                    this.j.append(str);
                    this.j.append('\n');
                    this.k.remove(str);
                    if (e()) {
                        this.a.submit(this.n);
                    }
                    return true;
                }
            }
        }
    }

    public synchronized void close() throws IOException {
        if (this.j != null) {
            Iterator it = new ArrayList(this.k.values()).iterator();
            while (it.hasNext()) {
                c cVar = (c) it.next();
                if (cVar.g != null) {
                    cVar.g.b();
                }
            }
            g();
            this.j.close();
            this.j = null;
        }
    }
}
