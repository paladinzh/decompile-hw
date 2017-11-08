package sun.net.www.http;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.net.ProgressSource;
import sun.net.www.MeteredStream;

public class KeepAliveStream extends MeteredStream implements Hurryable {
    private static Thread cleanerThread;
    private static final KeepAliveStreamCleaner queue = new KeepAliveStreamCleaner();
    HttpClient hc;
    boolean hurried;
    protected boolean queuedForCleanup = false;

    public KeepAliveStream(InputStream is, ProgressSource pi, long expected, HttpClient hc) {
        super(is, pi, expected);
        this.hc = hc;
    }

    public void close() throws IOException {
        if (!this.closed && !this.queuedForCleanup) {
            try {
                if (this.expected > this.count) {
                    long nskip = this.expected - this.count;
                    if (nskip <= ((long) available())) {
                        long n = 0;
                        while (n < nskip) {
                            nskip -= n;
                            n = skip(nskip);
                        }
                    } else if (this.expected > ((long) KeepAliveStreamCleaner.MAX_DATA_REMAINING) || this.hurried) {
                        this.hc.closeServer();
                    } else {
                        queueForCleanup(new KeepAliveCleanerEntry(this, this.hc));
                    }
                }
                if (!(this.closed || this.hurried)) {
                    if (!this.queuedForCleanup) {
                        this.hc.finished();
                    }
                }
                if (this.pi != null) {
                    this.pi.finishTracking();
                }
                if (!this.queuedForCleanup) {
                    this.in = null;
                    this.hc = null;
                    this.closed = true;
                }
            } catch (Throwable th) {
                if (this.pi != null) {
                    this.pi.finishTracking();
                }
                if (!this.queuedForCleanup) {
                    this.in = null;
                    this.hc = null;
                    this.closed = true;
                }
            }
        }
    }

    public boolean markSupported() {
        return false;
    }

    public void mark(int limit) {
    }

    public void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean hurry() {
        try {
            if (!this.closed && this.count < this.expected) {
                if (((long) this.in.available()) < this.expected - this.count) {
                    return false;
                }
                byte[] buf = new byte[((int) (this.expected - this.count))];
                new DataInputStream(this.in).readFully(buf);
                this.in = new ByteArrayInputStream(buf);
                this.hurried = true;
                return true;
            }
        } catch (IOException e) {
            return false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void queueForCleanup(KeepAliveCleanerEntry kace) {
        synchronized (queue) {
            if (!kace.getQueuedForCleanup()) {
                if (queue.offer(kace)) {
                    kace.setQueuedForCleanup();
                    queue.notifyAll();
                } else {
                    kace.getHttpClient().closeServer();
                    return;
                }
            }
            boolean startCleanupThread = cleanerThread == null;
            if (!(startCleanupThread || cleanerThread.isAlive())) {
                startCleanupThread = true;
            }
            if (startCleanupThread) {
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    public Void run() {
                        ThreadGroup grp = Thread.currentThread().getThreadGroup();
                        while (true) {
                            ThreadGroup parent = grp.getParent();
                            if (parent != null) {
                                grp = parent;
                            } else {
                                KeepAliveStream.cleanerThread = new Thread(grp, KeepAliveStream.queue, "Keep-Alive-SocketCleaner");
                                KeepAliveStream.cleanerThread.setDaemon(true);
                                KeepAliveStream.cleanerThread.setPriority(8);
                                KeepAliveStream.cleanerThread.setContextClassLoader(null);
                                KeepAliveStream.cleanerThread.start();
                                return null;
                            }
                        }
                    }
                });
            }
        }
    }

    protected long remainingToRead() {
        return this.expected - this.count;
    }

    protected void setClosed() {
        this.in = null;
        this.hc = null;
        this.closed = true;
    }
}
