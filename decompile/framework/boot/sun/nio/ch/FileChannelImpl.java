package sun.nio.ch;

import android.system.ErrnoException;
import android.system.OsConstants;
import dalvik.system.BlockGuard;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DirectByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.FileLock;
import java.nio.channels.FileLockInterruptionException;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.OverlappingFileLockException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.List;
import libcore.io.Libcore;
import sun.misc.Cleaner;
import sun.misc.IoTrace;
import sun.security.action.GetPropertyAction;

public class FileChannelImpl extends FileChannel {
    static final /* synthetic */ boolean -assertionsDisabled;
    private static final long MAPPED_TRANSFER_SIZE = 8388608;
    private static final int MAP_PV = 2;
    private static final int MAP_RO = 0;
    private static final int MAP_RW = 1;
    private static final int TRANSFER_SIZE = 8192;
    private static final long allocationGranularity = initIDs();
    private static volatile boolean fileSupported = true;
    private static boolean isSharedFileLockTable;
    private static volatile boolean pipeSupported = true;
    private static volatile boolean propertyChecked;
    private static volatile boolean transferSupported = true;
    private final boolean append;
    public final FileDescriptor fd;
    private volatile FileLockTable fileLockTable;
    private final FileDispatcher nd;
    private final Object parent;
    private final String path;
    private final Object positionLock = new Object();
    private final boolean readable;
    private final NativeThreadSet threads = new NativeThreadSet(2);
    private final boolean writable;

    private static class SimpleFileLockTable extends FileLockTable {
        static final /* synthetic */ boolean -assertionsDisabled = (!SimpleFileLockTable.class.desiredAssertionStatus());
        private final List<FileLock> lockList = new ArrayList(2);

        private void checkList(long position, long size) throws OverlappingFileLockException {
            if (-assertionsDisabled || Thread.holdsLock(this.lockList)) {
                for (FileLock fl : this.lockList) {
                    if (fl.overlaps(position, size)) {
                        throw new OverlappingFileLockException();
                    }
                }
                return;
            }
            throw new AssertionError();
        }

        public void add(FileLock fl) throws OverlappingFileLockException {
            synchronized (this.lockList) {
                checkList(fl.position(), fl.size());
                this.lockList.add(fl);
            }
        }

        public void remove(FileLock fl) {
            synchronized (this.lockList) {
                this.lockList.remove((Object) fl);
            }
        }

        public List<FileLock> removeAll() {
            List<FileLock> result;
            synchronized (this.lockList) {
                result = new ArrayList(this.lockList);
                this.lockList.clear();
            }
            return result;
        }

        public void replace(FileLock fl1, FileLock fl2) {
            synchronized (this.lockList) {
                this.lockList.remove((Object) fl1);
                this.lockList.add(fl2);
            }
        }
    }

    private static class Unmapper implements Runnable {
        static final /* synthetic */ boolean -assertionsDisabled;
        static volatile int count;
        private static final NativeDispatcher nd = new FileDispatcherImpl();
        static volatile long totalCapacity;
        static volatile long totalSize;
        private volatile long address;
        private final int cap;
        private final FileDescriptor fd;
        private final long size;

        static {
            boolean z;
            if (Unmapper.class.desiredAssertionStatus()) {
                z = false;
            } else {
                z = true;
            }
            -assertionsDisabled = z;
        }

        private Unmapper(long address, long size, int cap, FileDescriptor fd) {
            if (!-assertionsDisabled) {
                if ((address != 0 ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            this.address = address;
            this.size = size;
            this.cap = cap;
            this.fd = fd;
            synchronized (Unmapper.class) {
                count++;
                totalSize += size;
                totalCapacity += (long) cap;
            }
        }

        public void run() {
            if (this.address != 0) {
                FileChannelImpl.unmap0(this.address, this.size);
                this.address = 0;
                if (this.fd.valid()) {
                    try {
                        nd.close(this.fd);
                    } catch (IOException e) {
                    }
                }
                synchronized (Unmapper.class) {
                    count--;
                    totalSize -= this.size;
                    totalCapacity -= (long) this.cap;
                }
            }
        }
    }

    private static native long initIDs();

    private native long map0(int i, long j, long j2) throws IOException;

    private native long position0(FileDescriptor fileDescriptor, long j);

    private native long transferTo0(int i, long j, long j2, int i2);

    private static native int unmap0(long j, long j2);

    private FileChannelImpl(FileDescriptor fd, String path, boolean readable, boolean writable, boolean append, Object parent) {
        this.fd = fd;
        this.readable = readable;
        this.writable = writable;
        this.append = append;
        this.parent = parent;
        this.path = path;
        this.nd = new FileDispatcherImpl(append);
    }

    public static FileChannel open(FileDescriptor fd, String path, boolean readable, boolean writable, Object parent) {
        return new FileChannelImpl(fd, path, readable, writable, false, parent);
    }

    public static FileChannel open(FileDescriptor fd, String path, boolean readable, boolean writable, boolean append, Object parent) {
        return new FileChannelImpl(fd, path, readable, writable, append, parent);
    }

    private void ensureOpen() throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
    }

    protected void implCloseChannel() throws IOException {
        if (this.fileLockTable != null) {
            for (FileLock fl : this.fileLockTable.removeAll()) {
                synchronized (fl) {
                    if (fl.isValid()) {
                        this.nd.release(this.fd, fl.position(), fl.size());
                        ((FileLockImpl) fl).invalidate();
                    }
                }
            }
        }
        this.threads.signalAndWait();
        if (this.parent != null) {
            ((Closeable) this.parent).close();
        } else {
            this.nd.close(this.fd);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int read(ByteBuffer dst) throws IOException {
        int normalize;
        boolean z = true;
        boolean z2 = false;
        ensureOpen();
        if (this.readable) {
            synchronized (this.positionLock) {
                int n = 0;
                int ti = -1;
                Object traceContext = IoTrace.fileReadBegin(this.path);
                try {
                    begin();
                    ti = this.threads.add();
                    if (isOpen()) {
                        int i;
                        do {
                            n = IOUtil.read(this.fd, dst, -1, this.nd);
                            if (n != -3) {
                                break;
                            }
                        } while (isOpen());
                        normalize = IOStatus.normalize(n);
                        this.threads.remove(ti);
                        if (n > 0) {
                            i = n;
                        } else {
                            i = 0;
                        }
                        IoTrace.fileReadEnd(traceContext, (long) i);
                        if (n <= 0) {
                            z = false;
                        }
                        end(z);
                        if (-assertionsDisabled || IOStatus.check(n)) {
                        } else {
                            throw new AssertionError();
                        }
                    }
                    this.threads.remove(ti);
                    IoTrace.fileReadEnd(traceContext, (long) null);
                    end(false);
                    if (-assertionsDisabled || IOStatus.check(0)) {
                    } else {
                        throw new AssertionError();
                    }
                } catch (Throwable th) {
                    this.threads.remove(ti);
                    if (n > 0) {
                        normalize = n;
                    } else {
                        normalize = 0;
                    }
                    IoTrace.fileReadEnd(traceContext, (long) normalize);
                    if (n > 0) {
                        z2 = true;
                    }
                    end(z2);
                    if (!-assertionsDisabled && !IOStatus.check(n)) {
                        AssertionError assertionError = new AssertionError();
                    }
                }
            }
        } else {
            throw new NonReadableChannelException();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        if (offset < 0 || length < 0 || offset > dsts.length - length) {
            throw new IndexOutOfBoundsException();
        }
        ensureOpen();
        if (this.readable) {
            synchronized (this.positionLock) {
                long n = 0;
                int ti = -1;
                Object traceContext = IoTrace.fileReadBegin(this.path);
                try {
                    begin();
                    ti = this.threads.add();
                    if (isOpen()) {
                        do {
                            n = IOUtil.read(this.fd, dsts, offset, length, this.nd);
                            if (n != -3) {
                                break;
                            }
                        } while (isOpen());
                        long normalize = IOStatus.normalize(n);
                        this.threads.remove(ti);
                        IoTrace.fileReadEnd(traceContext, n > 0 ? n : 0);
                        end(n > 0);
                        if (-assertionsDisabled || IOStatus.check(n)) {
                        } else {
                            throw new AssertionError();
                        }
                    }
                    this.threads.remove(ti);
                    IoTrace.fileReadEnd(traceContext, 0);
                    end(false);
                    if (-assertionsDisabled || IOStatus.check(0)) {
                        return 0;
                    }
                    throw new AssertionError();
                } catch (Throwable th) {
                    this.threads.remove(ti);
                    IoTrace.fileReadEnd(traceContext, n > 0 ? n : 0);
                    end(n > 0);
                    if (!-assertionsDisabled && !IOStatus.check(n)) {
                        AssertionError assertionError = new AssertionError();
                    }
                }
            }
        } else {
            throw new NonReadableChannelException();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int write(ByteBuffer src) throws IOException {
        boolean z = true;
        int i = 0;
        ensureOpen();
        if (this.writable) {
            synchronized (this.positionLock) {
                int n = 0;
                int ti = -1;
                Object traceContext = IoTrace.fileWriteBegin(this.path);
                try {
                    begin();
                    ti = this.threads.add();
                    if (isOpen()) {
                        do {
                            n = IOUtil.write(this.fd, src, -1, this.nd);
                            if (n != -3) {
                                break;
                            }
                        } while (isOpen());
                        int normalize = IOStatus.normalize(n);
                        this.threads.remove(ti);
                        if (n <= 0) {
                            z = false;
                        }
                        end(z);
                        if (n > 0) {
                            i = n;
                        }
                        IoTrace.fileWriteEnd(traceContext, (long) i);
                        if (-assertionsDisabled || IOStatus.check(n)) {
                        } else {
                            throw new AssertionError();
                        }
                    }
                    this.threads.remove(ti);
                    end(false);
                    IoTrace.fileWriteEnd(traceContext, (long) null);
                    if (-assertionsDisabled || IOStatus.check(0)) {
                    } else {
                        throw new AssertionError();
                    }
                } catch (Throwable th) {
                    this.threads.remove(ti);
                    if (n <= 0) {
                        z = false;
                    }
                    end(z);
                    if (n > 0) {
                        i = n;
                    }
                    IoTrace.fileWriteEnd(traceContext, (long) i);
                    if (!-assertionsDisabled && !IOStatus.check(n)) {
                        AssertionError assertionError = new AssertionError();
                    }
                }
            }
        } else {
            throw new NonWritableChannelException();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        if (offset < 0 || length < 0 || offset > srcs.length - length) {
            throw new IndexOutOfBoundsException();
        }
        ensureOpen();
        if (this.writable) {
            synchronized (this.positionLock) {
                long n = 0;
                int ti = -1;
                Object traceContext = IoTrace.fileWriteBegin(this.path);
                try {
                    begin();
                    ti = this.threads.add();
                    if (isOpen()) {
                        do {
                            n = IOUtil.write(this.fd, srcs, offset, length, this.nd);
                            if (n != -3) {
                                break;
                            }
                        } while (isOpen());
                        long normalize = IOStatus.normalize(n);
                        this.threads.remove(ti);
                        IoTrace.fileWriteEnd(traceContext, n > 0 ? n : 0);
                        end(n > 0);
                        if (-assertionsDisabled || IOStatus.check(n)) {
                        } else {
                            throw new AssertionError();
                        }
                    }
                    this.threads.remove(ti);
                    IoTrace.fileWriteEnd(traceContext, 0);
                    end(false);
                    if (-assertionsDisabled || IOStatus.check(0)) {
                        return 0;
                    }
                    throw new AssertionError();
                } catch (Throwable th) {
                    this.threads.remove(ti);
                    IoTrace.fileWriteEnd(traceContext, n > 0 ? n : 0);
                    end(n > 0);
                    if (!-assertionsDisabled && !IOStatus.check(n)) {
                        AssertionError assertionError = new AssertionError();
                    }
                }
            }
        } else {
            throw new NonWritableChannelException();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long position() throws IOException {
        boolean z = true;
        ensureOpen();
        synchronized (this.positionLock) {
            long p = -1;
            int ti = -1;
            try {
                begin();
                ti = this.threads.add();
                if (isOpen()) {
                    if (this.append) {
                        BlockGuard.getThreadPolicy().onWriteToDisk();
                    }
                    do {
                        p = this.append ? this.nd.size(this.fd) : position0(this.fd, -1);
                        if (p != -3) {
                            break;
                        }
                    } while (isOpen());
                    long normalize = IOStatus.normalize(p);
                    this.threads.remove(ti);
                    if (p <= -1) {
                        z = false;
                    }
                    end(z);
                    if (-assertionsDisabled || IOStatus.check(p)) {
                    } else {
                        throw new AssertionError();
                    }
                }
                this.threads.remove(ti);
                end(false);
                if (-assertionsDisabled || IOStatus.check(-1)) {
                    return 0;
                }
                throw new AssertionError();
            } catch (Throwable th) {
                this.threads.remove(ti);
                if (p <= -1) {
                    z = false;
                }
                end(z);
                if (!-assertionsDisabled && !IOStatus.check(p)) {
                    AssertionError assertionError = new AssertionError();
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public FileChannel position(long newPosition) throws IOException {
        boolean z = true;
        ensureOpen();
        if (newPosition < 0) {
            throw new IllegalArgumentException();
        }
        synchronized (this.positionLock) {
            int ti = -1;
            try {
                begin();
                ti = this.threads.add();
                if (isOpen()) {
                    long p;
                    BlockGuard.getThreadPolicy().onReadFromDisk();
                    do {
                        p = position0(this.fd, newPosition);
                        if (p == -3) {
                        }
                        break;
                    } while (isOpen());
                    this.threads.remove(ti);
                    if (p <= -1) {
                        z = false;
                    }
                    end(z);
                    if (-assertionsDisabled || IOStatus.check(p)) {
                    } else {
                        throw new AssertionError();
                    }
                }
                this.threads.remove(ti);
                end(false);
                if (-assertionsDisabled || IOStatus.check(-1)) {
                    return null;
                }
                throw new AssertionError();
            } catch (Throwable th) {
                this.threads.remove(ti);
                if (-1 <= -1) {
                    z = false;
                }
                end(z);
                if (!-assertionsDisabled && !IOStatus.check(-1)) {
                    AssertionError assertionError = new AssertionError();
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long size() throws IOException {
        boolean z = true;
        ensureOpen();
        synchronized (this.positionLock) {
            long s = -1;
            int ti = -1;
            try {
                begin();
                ti = this.threads.add();
                if (isOpen()) {
                    do {
                        s = this.nd.size(this.fd);
                        if (s != -3) {
                            break;
                        }
                    } while (isOpen());
                    long normalize = IOStatus.normalize(s);
                    this.threads.remove(ti);
                    if (s <= -1) {
                        z = false;
                    }
                    end(z);
                    if (-assertionsDisabled || IOStatus.check(s)) {
                    } else {
                        throw new AssertionError();
                    }
                }
                this.threads.remove(ti);
                end(false);
                if (-assertionsDisabled || IOStatus.check(-1)) {
                } else {
                    throw new AssertionError();
                }
            } catch (Throwable th) {
                this.threads.remove(ti);
                if (s <= -1) {
                    z = false;
                }
                end(z);
                if (!-assertionsDisabled && !IOStatus.check(s)) {
                    AssertionError assertionError = new AssertionError();
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public FileChannel truncate(long size) throws IOException {
        boolean z = true;
        ensureOpen();
        if (size < 0) {
            throw new IllegalArgumentException();
        } else if (this.writable) {
            synchronized (this.positionLock) {
                int rv = -1;
                int ti = -1;
                begin();
                ti = this.threads.add();
                if (isOpen()) {
                    long p;
                    do {
                        try {
                            p = position0(this.fd, -1);
                            if (p != -3) {
                                break;
                            }
                        } catch (Throwable th) {
                            this.threads.remove(ti);
                            if (rv <= -1) {
                                z = false;
                            }
                            end(z);
                            if (!-assertionsDisabled && !IOStatus.check(rv)) {
                                AssertionError assertionError = new AssertionError();
                            }
                        }
                    } while (isOpen());
                    if (isOpen()) {
                        if (!-assertionsDisabled) {
                            boolean z2;
                            if (p >= 0) {
                                z2 = true;
                            } else {
                                z2 = false;
                            }
                            if (!z2) {
                                throw new AssertionError();
                            }
                        }
                        if (size < size()) {
                            do {
                                rv = this.nd.truncate(this.fd, size);
                                if (rv != -3) {
                                    break;
                                }
                            } while (isOpen());
                            if (!isOpen()) {
                                this.threads.remove(ti);
                                if (rv <= -1) {
                                    z = false;
                                }
                                end(z);
                                if (-assertionsDisabled || IOStatus.check(rv)) {
                                } else {
                                    throw new AssertionError();
                                }
                            }
                        }
                        if (p > size) {
                            p = size;
                        }
                        do {
                            rv = (int) position0(this.fd, p);
                            if (rv == -3) {
                            }
                            break;
                        } while (isOpen());
                        this.threads.remove(ti);
                        if (rv <= -1) {
                            z = false;
                        }
                        end(z);
                        if (-assertionsDisabled || IOStatus.check(rv)) {
                        } else {
                            throw new AssertionError();
                        }
                    }
                    this.threads.remove(ti);
                    end(false);
                    if (-assertionsDisabled || IOStatus.check(-1)) {
                    } else {
                        throw new AssertionError();
                    }
                }
                this.threads.remove(ti);
                end(false);
                if (-assertionsDisabled || IOStatus.check(-1)) {
                } else {
                    throw new AssertionError();
                }
            }
        } else {
            throw new NonWritableChannelException();
        }
    }

    public void force(boolean metaData) throws IOException {
        boolean z = true;
        ensureOpen();
        int rv = -1;
        int ti = -1;
        try {
            begin();
            ti = this.threads.add();
            if (isOpen()) {
                do {
                    rv = this.nd.force(this.fd, metaData);
                    if (rv != -3) {
                        break;
                    }
                } while (isOpen());
                this.threads.remove(ti);
                if (rv <= -1) {
                    z = false;
                }
                end(z);
                if (!-assertionsDisabled && !IOStatus.check(rv)) {
                    throw new AssertionError();
                }
                return;
            }
            this.threads.remove(ti);
            end(false);
            if (!-assertionsDisabled && !IOStatus.check(-1)) {
                throw new AssertionError();
            }
        } catch (Throwable th) {
            this.threads.remove(ti);
            if (rv <= -1) {
                z = false;
            }
            end(z);
            if (!-assertionsDisabled && !IOStatus.check(rv)) {
                AssertionError assertionError = new AssertionError();
            }
        }
    }

    static {
        boolean z;
        if (FileChannelImpl.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        -assertionsDisabled = z;
    }

    private long transferToDirectly(long position, int icount, WritableByteChannel target) throws IOException {
        if (!transferSupported) {
            return -4;
        }
        FileDescriptor targetFD = null;
        if (target instanceof FileChannelImpl) {
            if (!fileSupported) {
                return -6;
            }
            targetFD = ((FileChannelImpl) target).fd;
        } else if (target instanceof SelChImpl) {
            if ((target instanceof SinkChannelImpl) && !pipeSupported) {
                return -6;
            }
            targetFD = ((SelChImpl) target).getFD();
        }
        if (targetFD == null) {
            return -4;
        }
        int thisFDVal = IOUtil.fdVal(this.fd);
        int targetFDVal = IOUtil.fdVal(targetFD);
        if (thisFDVal == targetFDVal) {
            return -4;
        }
        long n = -1;
        int ti = -1;
        try {
            begin();
            ti = this.threads.add();
            if (isOpen()) {
                BlockGuard.getThreadPolicy().onWriteToDisk();
                do {
                    n = transferTo0(thisFDVal, position, (long) icount, targetFDVal);
                    if (n != -3) {
                        break;
                    }
                } while (isOpen());
                boolean z;
                if (n == -6) {
                    if (target instanceof SinkChannelImpl) {
                        pipeSupported = false;
                    }
                    if (target instanceof FileChannelImpl) {
                        fileSupported = false;
                    }
                    this.threads.remove(ti);
                    if (n > -1) {
                        z = true;
                    } else {
                        z = false;
                    }
                    end(z);
                    return -6;
                } else if (n == -4) {
                    transferSupported = false;
                    this.threads.remove(ti);
                    if (n > -1) {
                        z = true;
                    } else {
                        z = false;
                    }
                    end(z);
                    return -4;
                } else {
                    long normalize = IOStatus.normalize(n);
                    this.threads.remove(ti);
                    end(n > -1);
                    return normalize;
                }
            }
            this.threads.remove(ti);
            end(false);
            return -1;
        } catch (Throwable th) {
            this.threads.remove(ti);
            end(n > -1);
        }
    }

    private long transferToTrustedChannel(long position, long count, WritableByteChannel target) throws IOException {
        MappedByteBuffer dbb;
        boolean isSelChImpl = target instanceof SelChImpl;
        if (!(!(target instanceof FileChannelImpl) ? isSelChImpl : true)) {
            return -4;
        }
        long remaining = count;
        while (remaining > 0) {
            try {
                dbb = map(MapMode.READ_ONLY, position, Math.min(remaining, (long) MAPPED_TRANSFER_SIZE));
                int n = target.write(dbb);
                if (!-assertionsDisabled) {
                    if ((n >= 0 ? 1 : null) == null) {
                        throw new AssertionError();
                    }
                }
                remaining -= (long) n;
                if (isSelChImpl) {
                    unmap(dbb);
                    break;
                }
                if (!-assertionsDisabled) {
                    if ((n > 0 ? 1 : null) == null) {
                        throw new AssertionError();
                    }
                }
                position += (long) n;
                unmap(dbb);
            } catch (ClosedByInterruptException e) {
                if (!-assertionsDisabled) {
                    Object obj;
                    if (target.isOpen()) {
                        obj = null;
                    } else {
                        obj = 1;
                    }
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
                try {
                    close();
                } catch (Throwable suppressed) {
                    e.addSuppressed(suppressed);
                }
                throw e;
            } catch (IOException ioe) {
                if (remaining == count) {
                    throw ioe;
                }
            } catch (Throwable th) {
                unmap(dbb);
            }
        }
        return count - remaining;
    }

    private long transferToArbitraryChannel(long r16, int r18, java.nio.channels.WritableByteChannel r19) throws java.io.IOException {
        /* JADX: method processing error */
/*
Error: java.lang.NullPointerException
	at java.util.BitSet.or(BitSet.java:940)
	at jadx.core.utils.BlockUtils.getPathCross(BlockUtils.java:432)
	at jadx.core.dex.visitors.regions.IfMakerHelper.restructureIf(IfMakerHelper.java:80)
	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:619)
	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:127)
	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:94)
	at jadx.core.dex.visitors.regions.RegionMaker.processExcHandler(RegionMaker.java:1002)
	at jadx.core.dex.visitors.regions.RegionMaker.processTryCatchBlocks(RegionMaker.java:937)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:52)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r15 = this;
        r11 = 8192; // 0x2000 float:1.14794E-41 double:4.0474E-320;
        r0 = r18;
        r3 = java.lang.Math.min(r0, r11);
        r2 = sun.nio.ch.Util.getTemporaryDirectBuffer(r3);
        r8 = 0;
        r6 = r16;
        sun.nio.ch.Util.erase(r2);	 Catch:{ IOException -> 0x0045 }
    L_0x0013:
        r0 = r18;	 Catch:{ IOException -> 0x0045 }
        r12 = (long) r0;	 Catch:{ IOException -> 0x0045 }
        r11 = (r8 > r12 ? 1 : (r8 == r12 ? 0 : -1));	 Catch:{ IOException -> 0x0045 }
        if (r11 >= 0) goto L_0x002e;	 Catch:{ IOException -> 0x0045 }
    L_0x001a:
        r0 = r18;	 Catch:{ IOException -> 0x0045 }
        r12 = (long) r0;	 Catch:{ IOException -> 0x0045 }
        r12 = r12 - r8;	 Catch:{ IOException -> 0x0045 }
        r11 = (int) r12;	 Catch:{ IOException -> 0x0045 }
        r12 = 8192; // 0x2000 float:1.14794E-41 double:4.0474E-320;	 Catch:{ IOException -> 0x0045 }
        r11 = java.lang.Math.min(r11, r12);	 Catch:{ IOException -> 0x0045 }
        r2.limit(r11);	 Catch:{ IOException -> 0x0045 }
        r4 = r15.read(r2, r6);	 Catch:{ IOException -> 0x0045 }
        if (r4 > 0) goto L_0x0032;
    L_0x002e:
        sun.nio.ch.Util.releaseTemporaryDirectBuffer(r2);
        return r8;
    L_0x0032:
        r2.flip();	 Catch:{ IOException -> 0x0045 }
        r0 = r19;	 Catch:{ IOException -> 0x0045 }
        r5 = r0.write(r2);	 Catch:{ IOException -> 0x0045 }
        r12 = (long) r5;	 Catch:{ IOException -> 0x0045 }
        r8 = r8 + r12;	 Catch:{ IOException -> 0x0045 }
        if (r5 != r4) goto L_0x002e;	 Catch:{ IOException -> 0x0045 }
    L_0x003f:
        r12 = (long) r5;	 Catch:{ IOException -> 0x0045 }
        r6 = r6 + r12;	 Catch:{ IOException -> 0x0045 }
        r2.clear();	 Catch:{ IOException -> 0x0045 }
        goto L_0x0013;
    L_0x0045:
        r10 = move-exception;
        r12 = 0;
        r11 = (r8 > r12 ? 1 : (r8 == r12 ? 0 : -1));
        if (r11 <= 0) goto L_0x0050;
    L_0x004c:
        sun.nio.ch.Util.releaseTemporaryDirectBuffer(r2);
        return r8;
    L_0x0050:
        throw r10;	 Catch:{ all -> 0x0051 }
    L_0x0051:
        r11 = move-exception;
        sun.nio.ch.Util.releaseTemporaryDirectBuffer(r2);
        throw r11;
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.FileChannelImpl.transferToArbitraryChannel(long, int, java.nio.channels.WritableByteChannel):long");
    }

    public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
        ensureOpen();
        if (!target.isOpen()) {
            throw new ClosedChannelException();
        } else if (!this.readable) {
            throw new NonReadableChannelException();
        } else if ((target instanceof FileChannelImpl) && !((FileChannelImpl) target).writable) {
            throw new NonWritableChannelException();
        } else if (position < 0 || count < 0) {
            throw new IllegalArgumentException();
        } else {
            long sz = size();
            if (position > sz) {
                return 0;
            }
            int icount = (int) Math.min(count, 2147483647L);
            if (sz - position < ((long) icount)) {
                icount = (int) (sz - position);
            }
            long n = transferToDirectly(position, icount, target);
            if (n >= 0) {
                return n;
            }
            n = transferToTrustedChannel(position, (long) icount, target);
            if (n >= 0) {
                return n;
            }
            return transferToArbitraryChannel(position, icount, target);
        }
    }

    private long transferFromFileChannel(sun.nio.ch.FileChannelImpl r25, long r26, long r28) throws java.io.IOException {
        /* JADX: method processing error */
/*
Error: java.lang.NullPointerException
	at java.util.BitSet.and(BitSet.java:916)
	at jadx.core.utils.BlockUtils.getPathCross(BlockUtils.java:433)
	at jadx.core.dex.visitors.regions.IfMakerHelper.restructureIf(IfMakerHelper.java:80)
	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:619)
	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:127)
	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:94)
	at jadx.core.dex.visitors.regions.RegionMaker.processExcHandler(RegionMaker.java:1002)
	at jadx.core.dex.visitors.regions.RegionMaker.processTryCatchBlocks(RegionMaker.java:937)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:52)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r24 = this;
        r0 = r25;
        r4 = r0.readable;
        if (r4 != 0) goto L_0x000c;
    L_0x0006:
        r4 = new java.nio.channels.NonReadableChannelException;
        r4.<init>();
        throw r4;
    L_0x000c:
        r0 = r25;
        r0 = r0.positionLock;
        r22 = r0;
        monitor-enter(r22);
        r18 = r25.position();	 Catch:{ all -> 0x0065 }
        r4 = r25.size();	 Catch:{ all -> 0x0065 }
        r4 = r4 - r18;	 Catch:{ all -> 0x0065 }
        r0 = r28;	 Catch:{ all -> 0x0065 }
        r12 = java.lang.Math.min(r0, r4);	 Catch:{ all -> 0x0065 }
        r20 = r12;	 Catch:{ all -> 0x0065 }
        r6 = r18;	 Catch:{ all -> 0x0065 }
    L_0x0027:
        r4 = 0;	 Catch:{ all -> 0x0065 }
        r4 = (r20 > r4 ? 1 : (r20 == r4 ? 0 : -1));	 Catch:{ all -> 0x0065 }
        if (r4 <= 0) goto L_0x0076;	 Catch:{ all -> 0x0065 }
    L_0x002d:
        r4 = 8388608; // 0x800000 float:1.17549435E-38 double:4.144523E-317;	 Catch:{ all -> 0x0065 }
        r0 = r20;	 Catch:{ all -> 0x0065 }
        r8 = java.lang.Math.min(r0, r4);	 Catch:{ all -> 0x0065 }
        r5 = java.nio.channels.FileChannel.MapMode.READ_ONLY;	 Catch:{ all -> 0x0065 }
        r4 = r25;	 Catch:{ all -> 0x0065 }
        r10 = r4.map(r5, r6, r8);	 Catch:{ all -> 0x0065 }
        r0 = r24;	 Catch:{ IOException -> 0x005a }
        r1 = r26;	 Catch:{ IOException -> 0x005a }
        r4 = r0.write(r10, r1);	 Catch:{ IOException -> 0x005a }
        r14 = (long) r4;	 Catch:{ IOException -> 0x005a }
        r4 = -assertionsDisabled;	 Catch:{ IOException -> 0x005a }
        if (r4 != 0) goto L_0x006a;	 Catch:{ IOException -> 0x005a }
    L_0x004b:
        r4 = 0;	 Catch:{ IOException -> 0x005a }
        r4 = (r14 > r4 ? 1 : (r14 == r4 ? 0 : -1));	 Catch:{ IOException -> 0x005a }
        if (r4 <= 0) goto L_0x0068;	 Catch:{ IOException -> 0x005a }
    L_0x0051:
        r4 = 1;	 Catch:{ IOException -> 0x005a }
    L_0x0052:
        if (r4 != 0) goto L_0x006a;	 Catch:{ IOException -> 0x005a }
    L_0x0054:
        r4 = new java.lang.AssertionError;	 Catch:{ IOException -> 0x005a }
        r4.<init>();	 Catch:{ IOException -> 0x005a }
        throw r4;	 Catch:{ IOException -> 0x005a }
    L_0x005a:
        r11 = move-exception;
        r4 = (r20 > r12 ? 1 : (r20 == r12 ? 0 : -1));
        if (r4 != 0) goto L_0x0073;
    L_0x005f:
        throw r11;	 Catch:{ all -> 0x0060 }
    L_0x0060:
        r4 = move-exception;
        unmap(r10);	 Catch:{ all -> 0x0065 }
        throw r4;	 Catch:{ all -> 0x0065 }
    L_0x0065:
        r4 = move-exception;
        monitor-exit(r22);
        throw r4;
    L_0x0068:
        r4 = 0;
        goto L_0x0052;
    L_0x006a:
        r6 = r6 + r14;
        r26 = r26 + r14;
        r20 = r20 - r14;
        unmap(r10);	 Catch:{ all -> 0x0065 }
        goto L_0x0027;	 Catch:{ all -> 0x0065 }
    L_0x0073:
        unmap(r10);	 Catch:{ all -> 0x0065 }
    L_0x0076:
        r16 = r12 - r20;	 Catch:{ all -> 0x0065 }
        r4 = r18 + r16;	 Catch:{ all -> 0x0065 }
        r0 = r25;	 Catch:{ all -> 0x0065 }
        r0.position(r4);	 Catch:{ all -> 0x0065 }
        monitor-exit(r22);
        return r16;
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.FileChannelImpl.transferFromFileChannel(sun.nio.ch.FileChannelImpl, long, long):long");
    }

    private long transferFromArbitraryChannel(java.nio.channels.ReadableByteChannel r17, long r18, long r20) throws java.io.IOException {
        /* JADX: method processing error */
/*
Error: java.lang.NullPointerException
	at java.util.BitSet.or(BitSet.java:940)
	at jadx.core.utils.BlockUtils.getPathCross(BlockUtils.java:432)
	at jadx.core.dex.visitors.regions.IfMakerHelper.restructureIf(IfMakerHelper.java:80)
	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:619)
	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:127)
	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:94)
	at jadx.core.dex.visitors.regions.RegionMaker.processExcHandler(RegionMaker.java:1002)
	at jadx.core.dex.visitors.regions.RegionMaker.processTryCatchBlocks(RegionMaker.java:937)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:52)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r16 = this;
        r12 = 8192; // 0x2000 float:1.14794E-41 double:4.0474E-320;
        r0 = r20;
        r12 = java.lang.Math.min(r0, r12);
        r3 = (int) r12;
        r2 = sun.nio.ch.Util.getTemporaryDirectBuffer(r3);
        r8 = 0;
        r6 = r18;
        sun.nio.ch.Util.erase(r2);	 Catch:{ IOException -> 0x0043 }
    L_0x0014:
        r11 = (r8 > r20 ? 1 : (r8 == r20 ? 0 : -1));	 Catch:{ IOException -> 0x0043 }
        if (r11 >= 0) goto L_0x002c;	 Catch:{ IOException -> 0x0043 }
    L_0x0018:
        r12 = r20 - r8;	 Catch:{ IOException -> 0x0043 }
        r14 = 8192; // 0x2000 float:1.14794E-41 double:4.0474E-320;	 Catch:{ IOException -> 0x0043 }
        r12 = java.lang.Math.min(r12, r14);	 Catch:{ IOException -> 0x0043 }
        r11 = (int) r12;	 Catch:{ IOException -> 0x0043 }
        r2.limit(r11);	 Catch:{ IOException -> 0x0043 }
        r0 = r17;	 Catch:{ IOException -> 0x0043 }
        r4 = r0.read(r2);	 Catch:{ IOException -> 0x0043 }
        if (r4 > 0) goto L_0x0030;
    L_0x002c:
        sun.nio.ch.Util.releaseTemporaryDirectBuffer(r2);
        return r8;
    L_0x0030:
        r2.flip();	 Catch:{ IOException -> 0x0043 }
        r0 = r16;	 Catch:{ IOException -> 0x0043 }
        r5 = r0.write(r2, r6);	 Catch:{ IOException -> 0x0043 }
        r12 = (long) r5;	 Catch:{ IOException -> 0x0043 }
        r8 = r8 + r12;	 Catch:{ IOException -> 0x0043 }
        if (r5 != r4) goto L_0x002c;	 Catch:{ IOException -> 0x0043 }
    L_0x003d:
        r12 = (long) r5;	 Catch:{ IOException -> 0x0043 }
        r6 = r6 + r12;	 Catch:{ IOException -> 0x0043 }
        r2.clear();	 Catch:{ IOException -> 0x0043 }
        goto L_0x0014;
    L_0x0043:
        r10 = move-exception;
        r12 = 0;
        r11 = (r8 > r12 ? 1 : (r8 == r12 ? 0 : -1));
        if (r11 <= 0) goto L_0x004e;
    L_0x004a:
        sun.nio.ch.Util.releaseTemporaryDirectBuffer(r2);
        return r8;
    L_0x004e:
        throw r10;	 Catch:{ all -> 0x004f }
    L_0x004f:
        r11 = move-exception;
        sun.nio.ch.Util.releaseTemporaryDirectBuffer(r2);
        throw r11;
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.FileChannelImpl.transferFromArbitraryChannel(java.nio.channels.ReadableByteChannel, long, long):long");
    }

    public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
        ensureOpen();
        if (!src.isOpen()) {
            throw new ClosedChannelException();
        } else if (!this.writable) {
            throw new NonWritableChannelException();
        } else if (position < 0 || count < 0) {
            throw new IllegalArgumentException();
        } else if (position > size()) {
            return 0;
        } else {
            if (src instanceof FileChannelImpl) {
                return transferFromFileChannel((FileChannelImpl) src, position, count);
            }
            return transferFromArbitraryChannel(src, position, count);
        }
    }

    public int read(ByteBuffer dst, long position) throws IOException {
        if (dst == null) {
            throw new NullPointerException();
        } else if (position < 0) {
            throw new IllegalArgumentException("Negative position");
        } else if (this.readable) {
            ensureOpen();
            if (!this.nd.needsPositionLock()) {
                return readInternal(dst, position);
            }
            int readInternal;
            synchronized (this.positionLock) {
                readInternal = readInternal(dst, position);
            }
            return readInternal;
        } else {
            throw new NonReadableChannelException();
        }
    }

    private int readInternal(ByteBuffer dst, long position) throws IOException {
        boolean z = true;
        boolean z2 = false;
        if (!-assertionsDisabled) {
            if (!(this.nd.needsPositionLock() ? Thread.holdsLock(this.positionLock) : true)) {
                throw new AssertionError();
            }
        }
        int n = 0;
        int ti = -1;
        Object traceContext = IoTrace.fileReadBegin(this.path);
        int normalize;
        try {
            begin();
            ti = this.threads.add();
            if (isOpen()) {
                int i;
                do {
                    n = IOUtil.read(this.fd, dst, position, this.nd);
                    if (n != -3) {
                        break;
                    }
                } while (isOpen());
                normalize = IOStatus.normalize(n);
                this.threads.remove(ti);
                if (n > 0) {
                    i = n;
                } else {
                    i = 0;
                }
                IoTrace.fileReadEnd(traceContext, (long) i);
                if (n > 0) {
                    z2 = true;
                }
                end(z2);
                if (-assertionsDisabled || IOStatus.check(n)) {
                    return normalize;
                }
                throw new AssertionError();
            }
            this.threads.remove(ti);
            IoTrace.fileReadEnd(traceContext, (long) null);
            end(false);
            if (-assertionsDisabled || IOStatus.check(0)) {
                return -1;
            }
            throw new AssertionError();
        } catch (Throwable th) {
            this.threads.remove(ti);
            if (n > 0) {
                normalize = n;
            } else {
                normalize = 0;
            }
            IoTrace.fileReadEnd(traceContext, (long) normalize);
            if (n <= 0) {
                z = false;
            }
            end(z);
            if (!-assertionsDisabled && !IOStatus.check(n)) {
                AssertionError assertionError = new AssertionError();
            }
        }
    }

    public int write(ByteBuffer src, long position) throws IOException {
        if (src == null) {
            throw new NullPointerException();
        } else if (position < 0) {
            throw new IllegalArgumentException("Negative position");
        } else if (this.writable) {
            ensureOpen();
            if (!this.nd.needsPositionLock()) {
                return writeInternal(src, position);
            }
            int writeInternal;
            synchronized (this.positionLock) {
                writeInternal = writeInternal(src, position);
            }
            return writeInternal;
        } else {
            throw new NonWritableChannelException();
        }
    }

    private int writeInternal(ByteBuffer src, long position) throws IOException {
        boolean z = true;
        int i = 0;
        if (!-assertionsDisabled) {
            if (!(this.nd.needsPositionLock() ? Thread.holdsLock(this.positionLock) : true)) {
                throw new AssertionError();
            }
        }
        int n = 0;
        int ti = -1;
        Object traceContext = IoTrace.fileWriteBegin(this.path);
        try {
            begin();
            ti = this.threads.add();
            if (isOpen()) {
                do {
                    n = IOUtil.write(this.fd, src, position, this.nd);
                    if (n != -3) {
                        break;
                    }
                } while (isOpen());
                int normalize = IOStatus.normalize(n);
                this.threads.remove(ti);
                if (n <= 0) {
                    z = false;
                }
                end(z);
                if (n > 0) {
                    i = n;
                }
                IoTrace.fileWriteEnd(traceContext, (long) i);
                if (-assertionsDisabled || IOStatus.check(n)) {
                    return normalize;
                }
                throw new AssertionError();
            }
            this.threads.remove(ti);
            end(false);
            IoTrace.fileWriteEnd(traceContext, (long) null);
            if (-assertionsDisabled || IOStatus.check(0)) {
                return -1;
            }
            throw new AssertionError();
        } catch (Throwable th) {
            this.threads.remove(ti);
            if (n <= 0) {
                z = false;
            }
            end(z);
            if (n > 0) {
                i = n;
            }
            IoTrace.fileWriteEnd(traceContext, (long) i);
            if (!-assertionsDisabled && !IOStatus.check(n)) {
                AssertionError assertionError = new AssertionError();
            }
        }
    }

    private static void unmap(MappedByteBuffer bb) {
        Cleaner cl = ((DirectBuffer) bb).cleaner();
        if (cl != null) {
            cl.clean();
        }
    }

    public MappedByteBuffer map(MapMode mode, long position, long size) throws IOException {
        long mapSize;
        ensureOpen();
        if (position < 0) {
            throw new IllegalArgumentException("Negative position");
        } else if (size < 0) {
            throw new IllegalArgumentException("Negative size");
        } else if (position + size < 0) {
            throw new IllegalArgumentException("Position + size overflow");
        } else if (size > 2147483647L) {
            throw new IllegalArgumentException("Size exceeds Integer.MAX_VALUE");
        } else {
            int imode = -1;
            if (mode == MapMode.READ_ONLY) {
                imode = 0;
            } else if (mode == MapMode.READ_WRITE) {
                imode = 1;
            } else if (mode == MapMode.PRIVATE) {
                imode = 2;
            }
            if (!-assertionsDisabled) {
                if ((imode >= 0 ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            if (mode != MapMode.READ_ONLY && !this.writable) {
                throw new NonWritableChannelException();
            } else if (this.readable) {
                long addr = -1;
                int ti = -1;
                begin();
                ti = this.threads.add();
                if (isOpen()) {
                    long mapPosition;
                    if (size() < position + size) {
                        do {
                            try {
                                if (this.nd.truncate(this.fd, position + size) != -3) {
                                    break;
                                }
                                try {
                                } catch (IOException ioe) {
                                    unmap0(addr, mapSize);
                                    throw ioe;
                                } catch (Throwable y) {
                                    throw new IOException("Map failed", y);
                                } catch (OutOfMemoryError e) {
                                    System.gc();
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e2) {
                                        Thread.currentThread().interrupt();
                                    }
                                    addr = map0(imode, mapPosition, mapSize);
                                } catch (Throwable th) {
                                    this.threads.remove(ti);
                                    end(IOStatus.checkAll(addr));
                                }
                            } catch (IOException r) {
                                if (OsConstants.S_ISREG(Libcore.os.fstat(this.fd).st_mode)) {
                                    throw r;
                                }
                            } catch (ErrnoException e3) {
                                e3.rethrowAsIOException();
                            }
                        } while (isOpen());
                    }
                    if (size == 0) {
                        FileDescriptor dummy = new FileDescriptor();
                        boolean z = !this.writable || imode == 0;
                        MappedByteBuffer directByteBuffer = new DirectByteBuffer(0, 0, dummy, null, z);
                        this.threads.remove(ti);
                        end(IOStatus.checkAll(0));
                        return directByteBuffer;
                    }
                    int pagePosition = (int) (position % allocationGranularity);
                    mapPosition = position - ((long) pagePosition);
                    mapSize = size + ((long) pagePosition);
                    BlockGuard.getThreadPolicy().onReadFromDisk();
                    addr = map0(imode, mapPosition, mapSize);
                    FileDescriptor mfd = this.nd.duplicateForMapping(this.fd);
                    if (-assertionsDisabled || IOStatus.checkAll(addr)) {
                        if (!-assertionsDisabled) {
                            if ((addr % allocationGranularity == 0 ? 1 : null) == null) {
                                throw new AssertionError();
                            }
                        }
                        int isize = (int) size;
                        Unmapper um = new Unmapper(addr, mapSize, isize, mfd);
                        long j = addr + ((long) pagePosition);
                        boolean z2 = !this.writable || imode == 0;
                        MappedByteBuffer directByteBuffer2 = new DirectByteBuffer(isize, j, mfd, um, z2);
                        this.threads.remove(ti);
                        end(IOStatus.checkAll(addr));
                        return directByteBuffer2;
                    }
                    throw new AssertionError();
                }
                this.threads.remove(ti);
                end(IOStatus.checkAll(addr));
                return null;
            } else {
                throw new NonReadableChannelException();
            }
        }
    }

    private static boolean isSharedFileLockTable() {
        boolean z = true;
        if (!propertyChecked) {
            synchronized (FileChannelImpl.class) {
                if (!propertyChecked) {
                    String value = (String) AccessController.doPrivileged(new GetPropertyAction("sun.nio.ch.disableSystemWideOverlappingFileLockCheck"));
                    if (value != null) {
                        z = value.equals("false");
                    }
                    isSharedFileLockTable = z;
                    propertyChecked = true;
                }
            }
        }
        return isSharedFileLockTable;
    }

    private FileLockTable fileLockTable() throws IOException {
        if (this.fileLockTable == null) {
            synchronized (this) {
                if (this.fileLockTable == null) {
                    if (isSharedFileLockTable()) {
                        int ti = this.threads.add();
                        try {
                            ensureOpen();
                            this.fileLockTable = FileLockTable.newSharedFileLockTable(this, this.fd);
                        } finally {
                            this.threads.remove(ti);
                        }
                    } else {
                        this.fileLockTable = new SimpleFileLockTable();
                    }
                }
            }
        }
        return this.fileLockTable;
    }

    public FileLock lock(long position, long size, boolean shared) throws IOException {
        ensureOpen();
        if (shared && !this.readable) {
            throw new NonReadableChannelException();
        } else if (shared || this.writable) {
            FileLockImpl fli = new FileLockImpl(this, position, size, shared);
            FileLockTable flt = fileLockTable();
            flt.add(fli);
            boolean completed = false;
            int i = -1;
            begin();
            i = this.threads.add();
            if (isOpen()) {
                int n;
                while (true) {
                    try {
                        n = this.nd.lock(this.fd, true, position, size, shared);
                        if (n == 2) {
                            if (!isOpen()) {
                                break;
                            }
                        }
                        break;
                    } finally {
                        if (null == null) {
                            flt.remove(fli);
                        }
                        this.threads.remove(i);
                        try {
                            end(false);
                        } catch (ClosedByInterruptException e) {
                            throw new FileLockInterruptionException();
                        }
                    }
                }
                if (isOpen()) {
                    if (n == 1) {
                        if (-assertionsDisabled || shared) {
                            FileLockImpl fli2 = new FileLockImpl(this, position, size, false);
                            flt.replace(fli, fli2);
                            fli = fli2;
                        } else {
                            throw new AssertionError();
                        }
                    }
                    completed = true;
                }
                if (!completed) {
                    flt.remove(fli);
                }
                this.threads.remove(i);
                try {
                    end(completed);
                    return fli;
                } catch (ClosedByInterruptException e2) {
                    throw new FileLockInterruptionException();
                }
            }
            try {
                end(false);
                return null;
            } catch (ClosedByInterruptException e3) {
                throw new FileLockInterruptionException();
            }
        } else {
            throw new NonWritableChannelException();
        }
    }

    public FileLock tryLock(long position, long size, boolean shared) throws IOException {
        ensureOpen();
        if (shared && !this.readable) {
            throw new NonReadableChannelException();
        } else if (shared || this.writable) {
            FileLockImpl fli = new FileLockImpl(this, position, size, shared);
            FileLockTable flt = fileLockTable();
            flt.add(fli);
            int ti = this.threads.add();
            try {
                ensureOpen();
                int result = this.nd.lock(this.fd, false, position, size, shared);
                if (result == -1) {
                    flt.remove(fli);
                    this.threads.remove(ti);
                    return null;
                } else if (result != 1) {
                    this.threads.remove(ti);
                    return fli;
                } else if (-assertionsDisabled || shared) {
                    FileLockImpl fli2 = new FileLockImpl(this, position, size, false);
                    flt.replace(fli, fli2);
                    this.threads.remove(ti);
                    return fli2;
                } else {
                    throw new AssertionError();
                }
            } catch (IOException e) {
                flt.remove(fli);
                throw e;
            } catch (Throwable th) {
                this.threads.remove(ti);
            }
        } else {
            throw new NonWritableChannelException();
        }
    }

    void release(FileLockImpl fli) throws IOException {
        int ti = this.threads.add();
        try {
            ensureOpen();
            this.nd.release(this.fd, fli.position(), fli.size());
            if (!-assertionsDisabled) {
                if ((this.fileLockTable != null ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            this.fileLockTable.remove(fli);
        } finally {
            this.threads.remove(ti);
        }
    }
}
