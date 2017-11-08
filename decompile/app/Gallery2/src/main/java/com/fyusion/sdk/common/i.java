package com.fyusion.sdk.common;

import com.fyusion.sdk.core.a.h;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/* compiled from: Unknown */
public class i {
    protected boolean a = false;
    protected HashMap<Integer, Long> b = new HashMap();
    protected HashMap<Integer, Integer> c = new HashMap();
    protected Lock d = new ReentrantLock();
    protected a e = null;
    protected boolean f = false;
    protected File g = null;
    protected FileOutputStream h = null;
    protected long i = 0;
    protected int j = -1;
    protected Semaphore k = new Semaphore(10000);
    protected Semaphore l = new Semaphore(1);
    protected int m = 1;

    /* compiled from: Unknown */
    public enum a {
        READ_ONLY,
        READ_WRITE
    }

    /* compiled from: Unknown */
    public enum b {
        NONE,
        TRUNCATE
    }

    public i(File file) {
        if (file.isDirectory()) {
            this.g = new File(file, j());
        } else {
            this.g = file;
        }
    }

    private void a(RandomAccessFile randomAccessFile) throws IOException {
        int i = 0;
        int readInt = randomAccessFile.readInt();
        long readLong = randomAccessFile.readLong();
        if (readLong == 0 || readInt == 0) {
            randomAccessFile.close();
            if (this.e != a.READ_WRITE) {
                throw new IOException("Corrupted fyuse file");
            }
            this.g.delete();
            throw new FileNotFoundException("No fyuse file in workingdir " + this.g + " (corrupted one deleted)");
        }
        DLog.d("FyuseFIO", "index table starting at offset " + readLong + k() + " (" + readLong + " frame FRME header)");
        randomAccessFile.seek(readLong + k());
        while (i < readInt) {
            int readInt2 = randomAccessFile.readInt();
            if (readInt2 >= 0) {
                this.b.put(Integer.valueOf(readInt2), Long.valueOf(randomAccessFile.readLong()));
                this.c.put(Integer.valueOf(readInt2), Integer.valueOf(randomAccessFile.readInt()));
                i++;
            } else {
                throw new IOException("Illegal frame index " + readInt2 + " encountered at offset " + (randomAccessFile.getFilePointer() - 4));
            }
        }
    }

    private void b(RandomAccessFile randomAccessFile) throws IOException {
        byte[] bArr = new byte[524288];
        long k = k();
        randomAccessFile.seek(k);
        long j = 0;
        Object obj = null;
        int i = 0;
        long j2 = 0;
        int read = randomAccessFile.read(bArr);
        while (read > 0) {
            if (j != 0) {
                byte b = bArr[0];
                if (b == (byte) -40) {
                    this.b.put(Integer.valueOf(i), Long.valueOf(j2 - 1));
                    obj = 1;
                } else if (b == (byte) -39 && r2 != null) {
                    this.c.put(Integer.valueOf(i), Integer.valueOf((int) (j2 - ((Long) this.b.get(Integer.valueOf(i))).longValue())));
                    i++;
                    obj = null;
                }
                j = 0;
            }
            for (int i2 = 0; i2 < read - 1; i2++) {
                if (bArr[i2] == (byte) -1) {
                    byte b2 = bArr[i2 + 1];
                    if (b2 == (byte) -40) {
                        this.b.put(Integer.valueOf(i), Long.valueOf(((long) i2) + j2));
                        obj = 1;
                    } else if (b2 == (byte) -39 && r2 != null) {
                        this.c.put(Integer.valueOf(i), Integer.valueOf((int) (((((long) i2) + j2) - ((Long) this.b.get(Integer.valueOf(i))).longValue()) + 1)));
                        i++;
                        obj = null;
                    }
                }
            }
            if (bArr[read - 1] == (byte) -1) {
                j = (((long) read) + j2) - 1;
            }
            j2 += (long) read;
            randomAccessFile.seek(k + j2);
            read = randomAccessFile.read(bArr, 0, 524288);
        }
    }

    public h a(int i, int i2, int i3) throws IndexOutOfBoundsException {
        int i4 = 0;
        if (!this.a) {
            return null;
        }
        synchronized (this) {
            if (i3 >= 0) {
                if (this.e == a.READ_ONLY) {
                    if (!this.b.containsKey(Integer.valueOf(i3))) {
                        throw new IndexOutOfBoundsException();
                    }
                }
            } else {
                throw new IndexOutOfBoundsException();
            }
        }
        Integer num = (Integer) this.c.get(Integer.valueOf(i3));
        if (num != null) {
            i4 = num.intValue();
        }
        h hVar = new h(this, i3, i, i2, i4);
        hVar.b(this.m);
        return hVar;
    }

    public FileOutputStream a(int i) throws IOException {
        if (this.e == a.READ_WRITE) {
            FileOutputStream fileOutputStream;
            this.d.lock();
            synchronized (this) {
                if (this.a) {
                    try {
                        if (this.h == null) {
                            this.h = new FileOutputStream(this.g, true);
                            this.i = this.h.getChannel().size();
                        }
                        this.j = i;
                        if (this.f) {
                            fileOutputStream = this.h;
                        } else {
                            fileOutputStream = this.h;
                        }
                    } catch (IOException e) {
                        this.d.unlock();
                        throw e;
                    }
                }
                this.d.unlock();
                throw new FileNotFoundException("File not open (maybe it was deleted in the meantime, not necessarily an error)");
            }
            return fileOutputStream;
        }
        throw new IOException("File not writable");
    }

    public void a(FileInputStream fileInputStream) {
        try {
            fileInputStream.close();
            this.k.release();
        } catch (Throwable e) {
            DLog.e("FyuseFIO", "Failed to releaseInputStream.", e);
        }
    }

    public void a(FileOutputStream fileOutputStream) {
        try {
            long position = fileOutputStream.getChannel().position();
            if ((position <= this.i ? 1 : null) == null) {
                synchronized (this) {
                    this.b.put(Integer.valueOf(this.j), Long.valueOf(this.i - l()));
                    this.c.put(Integer.valueOf(this.j), Integer.valueOf((int) (position - this.i)));
                }
            }
            this.i = position;
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.d.unlock();
    }

    public boolean a() {
        return this.a;
    }

    public boolean a(a aVar, b bVar) {
        Throwable th;
        boolean z = true;
        try {
            RandomAccessFile randomAccessFile;
            Throwable th2;
            DLog.d("FyuseFIO", "Trying to open " + this.g);
            this.l.acquire();
            DLog.d("FyuseFIO", "Instance for open " + this.g + " acquired");
            synchronized (this) {
                if (this.a) {
                    if (aVar != this.e) {
                        h();
                    } else {
                        this.l.release();
                        return true;
                    }
                }
                this.e = aVar;
                try {
                    if (bVar != b.TRUNCATE) {
                        m();
                        z = false;
                        if (z) {
                            randomAccessFile = new RandomAccessFile(this.g, "rw");
                            randomAccessFile.writeInt(1179798853);
                            randomAccessFile.writeInt(0);
                            randomAccessFile.writeLong(0);
                            this.a = true;
                            if (randomAccessFile != null) {
                                randomAccessFile.close();
                            }
                        } else {
                            this.a = true;
                        }
                    } else if (aVar == a.READ_WRITE) {
                        if (this.g.exists()) {
                            this.g.delete();
                        }
                        if (z) {
                            this.a = true;
                        } else {
                            try {
                                randomAccessFile = new RandomAccessFile(this.g, "rw");
                                try {
                                    randomAccessFile.writeInt(1179798853);
                                    randomAccessFile.writeInt(0);
                                    randomAccessFile.writeLong(0);
                                    this.a = true;
                                    if (randomAccessFile != null) {
                                        randomAccessFile.close();
                                    }
                                } catch (Throwable th22) {
                                    Throwable th3 = th22;
                                    th22 = th;
                                    th = th3;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        this.l.release();
                        return false;
                    }
                } catch (Throwable e2) {
                    if (aVar != a.READ_WRITE) {
                        DLog.e("FyuseFIO", "File " + this.g + " not found for read only access", e2);
                        this.l.release();
                        return false;
                    }
                } catch (Throwable e22) {
                    DLog.e("FyuseFIO", "Fail open file " + this.g, e22);
                    if (aVar != a.READ_WRITE) {
                        this.l.release();
                        return false;
                    } else if (this.g.exists()) {
                        this.g.delete();
                    } else {
                        DLog.e("FyuseFIO", "I/O error though no file was existing, assuming serious problem and bailing out");
                        this.l.release();
                        return false;
                    }
                }
            }
            this.l.release();
            return this.a;
            if (randomAccessFile != null) {
                if (th22 == null) {
                    randomAccessFile.close();
                } else {
                    try {
                        randomAccessFile.close();
                    } catch (Throwable e222) {
                        th22.addSuppressed(e222);
                    }
                }
            }
            throw th;
            throw th;
        } catch (InterruptedException e3) {
            try {
                z = this.a;
                return z;
            } finally {
                this.l.release();
            }
        }
    }

    public File b() {
        return this.g;
    }

    public FileInputStream b(int i) throws IOException, IndexOutOfBoundsException {
        if (this.a) {
            long longValue;
            synchronized (this) {
                if (i >= 0) {
                    if (this.b.containsKey(Integer.valueOf(i))) {
                        if (this.a) {
                            longValue = ((Long) this.b.get(Integer.valueOf(i))).longValue() + k();
                        } else {
                            DLog.e("FyuseFIO", this.g.getPath() + " is not open");
                            throw new IOException("File is not open (anymore)");
                        }
                    }
                }
                throw new IndexOutOfBoundsException();
            }
            FileInputStream fileInputStream = new FileInputStream(this.g);
            fileInputStream.skip(longValue);
            if (this.k.tryAcquire(1)) {
                return fileInputStream;
            }
            fileInputStream.close();
            throw new IOException("No more I/Os available");
        }
        throw new IOException("File is not open");
    }

    public synchronized int c() {
        if (!this.a) {
            return 0;
        }
        return this.b.size();
    }

    public boolean d() {
        return this.g.exists();
    }

    public void e() {
        try {
            this.d.lock();
            synchronized (this) {
                if (this.e == a.READ_WRITE) {
                    if (!this.f) {
                        n();
                    }
                }
            }
            this.d.unlock();
        } catch (IOException e) {
            try {
                e.printStackTrace();
            } finally {
                this.d.unlock();
            }
        }
    }

    public synchronized boolean f() {
        if (this.a) {
            return false;
        }
        if (this.l.availablePermits() > 0) {
            this.l.drainPermits();
            DLog.d("FyuseFIO", "Locked IO " + this.g);
            return true;
        }
        DLog.w("FyuseFIO", "Could not lock IO " + this.g);
        return false;
    }

    public synchronized void g() {
        if (this.l.availablePermits() <= 0) {
            this.l.release();
            DLog.d("FyuseFIO", "Unlocked IO " + this.g);
            return;
        }
        DLog.w("FyuseFIO", "Unexpected # of permits: " + this.l.availablePermits() + " on " + this.g);
    }

    public void h() {
        try {
            this.d.lock();
            synchronized (this) {
                if (this.a) {
                    if (this.e == a.READ_WRITE && !this.f) {
                        n();
                    }
                }
                this.a = false;
                if (this.h != null) {
                    this.h.close();
                    this.h = null;
                }
                try {
                    this.k.acquire(10000);
                } catch (InterruptedException e) {
                }
                DLog.d("FyuseFIO", "Closing IO " + this.g);
            }
            this.k.release(10000);
            this.a = false;
            this.f = false;
            this.b.clear();
            this.c.clear();
            this.j = -1;
            this.d.unlock();
        } catch (Throwable e2) {
            try {
                DLog.e("FyuseFIO", "Faile to close file " + this.g, e2);
            } finally {
                this.a = false;
                this.f = false;
                this.b.clear();
                this.c.clear();
                this.j = -1;
                this.d.unlock();
            }
        }
    }

    public void i() {
        this.d.lock();
        try {
            this.a = false;
            this.f = false;
            this.b.clear();
            this.c.clear();
            this.j = -1;
            if (this.h != null) {
                this.h.close();
                this.h = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Throwable th) {
            this.d.unlock();
        }
        f();
        this.d.unlock();
    }

    protected String j() {
        return "refyuse.fyu";
    }

    protected long k() {
        return 0;
    }

    protected long l() {
        return 0;
    }

    protected void m() throws IOException {
        RandomAccessFile randomAccessFile;
        Throwable th;
        Throwable th2 = null;
        this.f = false;
        if (this.g.exists()) {
            randomAccessFile = new RandomAccessFile(this.g, "r");
            try {
                randomAccessFile.seek(k());
                if (randomAccessFile.readInt() != 1179798853) {
                    randomAccessFile.seek(k());
                    if (randomAccessFile.readChar() != '￘') {
                        throw new IOException("Illegal format (invalid header)");
                    }
                    this.m = 0;
                    b(randomAccessFile);
                    if (randomAccessFile != null) {
                        randomAccessFile.close();
                    }
                    return;
                }
                a(randomAccessFile);
                this.f = true;
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
                return;
            } catch (Throwable th22) {
                Throwable th3 = th22;
                th22 = th;
                th = th3;
            }
        } else {
            throw new FileNotFoundException("No fyuse file in workingdir " + this.g);
        }
        if (randomAccessFile != null) {
            if (th22 == null) {
                randomAccessFile.close();
            } else {
                try {
                    randomAccessFile.close();
                } catch (Throwable th4) {
                    th22.addSuppressed(th4);
                }
            }
        }
        throw th;
        throw th;
    }

    protected void n() throws IOException {
        RandomAccessFile randomAccessFile;
        Throwable th;
        Throwable th2;
        if (this.g.exists()) {
            randomAccessFile = new RandomAccessFile(this.g, "rw");
            try {
                randomAccessFile.writeInt(1179798853);
                randomAccessFile.writeInt(this.b.size());
                randomAccessFile.writeLong(randomAccessFile.length());
                randomAccessFile.seek(randomAccessFile.length());
                for (Entry entry : this.b.entrySet()) {
                    randomAccessFile.writeInt(((Integer) entry.getKey()).intValue());
                    randomAccessFile.writeLong(((Long) entry.getValue()).longValue());
                    randomAccessFile.writeInt(((Integer) this.c.get(entry.getKey())).intValue());
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
                this.f = true;
                return;
            } catch (Throwable th22) {
                Throwable th3 = th22;
                th22 = th;
                th = th3;
            }
        } else {
            throw new FileNotFoundException("No fyuse file in workingdir " + this.g);
        }
        if (randomAccessFile != null) {
            if (th22 == null) {
                randomAccessFile.close();
            } else {
                try {
                    randomAccessFile.close();
                } catch (Throwable th4) {
                    th22.addSuppressed(th4);
                }
            }
        }
        throw th;
        throw th;
    }
}
