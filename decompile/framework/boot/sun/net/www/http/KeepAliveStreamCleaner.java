package sun.net.www.http;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.LinkedList;
import sun.net.NetProperties;

class KeepAliveStreamCleaner extends LinkedList<KeepAliveCleanerEntry> implements Runnable {
    protected static int MAX_CAPACITY = 0;
    protected static int MAX_DATA_REMAINING = 0;
    private static final int MAX_RETRIES = 5;
    protected static final int TIMEOUT = 5000;

    KeepAliveStreamCleaner() {
    }

    static {
        MAX_DATA_REMAINING = 512;
        MAX_CAPACITY = 10;
        String maxDataKey = "http.KeepAlive.remainingData";
        MAX_DATA_REMAINING = ((Integer) AccessController.doPrivileged(new PrivilegedAction<Integer>() {
            public Integer run() {
                return NetProperties.getInteger("http.KeepAlive.remainingData", KeepAliveStreamCleaner.MAX_DATA_REMAINING);
            }
        })).intValue() * 1024;
        String maxCapacityKey = "http.KeepAlive.queuedConnections";
        MAX_CAPACITY = ((Integer) AccessController.doPrivileged(new PrivilegedAction<Integer>() {
            public Integer run() {
                return NetProperties.getInteger("http.KeepAlive.queuedConnections", KeepAliveStreamCleaner.MAX_CAPACITY);
            }
        })).intValue();
    }

    public boolean offer(KeepAliveCleanerEntry e) {
        if (size() >= MAX_CAPACITY) {
            return false;
        }
        return super.offer(e);
    }

    public void run() {
        KeepAliveCleanerEntry keepAliveCleanerEntry = null;
        while (true) {
            try {
                synchronized (this) {
                    long before = System.currentTimeMillis();
                    long timeout = 5000;
                    while (true) {
                        keepAliveCleanerEntry = (KeepAliveCleanerEntry) poll();
                        if (keepAliveCleanerEntry == null) {
                            wait(timeout);
                            long after = System.currentTimeMillis();
                            long elapsed = after - before;
                            if (elapsed > timeout) {
                                break;
                            }
                            before = after;
                            timeout -= elapsed;
                        }
                        break;
                    }
                    keepAliveCleanerEntry = (KeepAliveCleanerEntry) poll();
                }
                if (keepAliveCleanerEntry != null) {
                    KeepAliveStream kas = keepAliveCleanerEntry.getKeepAliveStream();
                    if (kas != null) {
                        synchronized (kas) {
                            HttpClient hc = keepAliveCleanerEntry.getHttpClient();
                            if (hc != null) {
                                try {
                                    if (!hc.isInKeepAliveCache()) {
                                        int oldTimeout = hc.getReadTimeout();
                                        hc.setReadTimeout(TIMEOUT);
                                        long remainingToRead = kas.remainingToRead();
                                        if (remainingToRead > 0) {
                                            long n = 0;
                                            int retries = 0;
                                            while (n < remainingToRead && retries < 5) {
                                                remainingToRead -= n;
                                                n = kas.skip(remainingToRead);
                                                if (n == 0) {
                                                    retries++;
                                                }
                                            }
                                            remainingToRead -= n;
                                        }
                                        if (remainingToRead == 0) {
                                            hc.setReadTimeout(oldTimeout);
                                            hc.finished();
                                        } else {
                                            hc.closeServer();
                                        }
                                    }
                                } catch (IOException e) {
                                    hc.closeServer();
                                } finally {
                                    kas.setClosed();
                                }
                            }
                            kas.setClosed();
                        }
                    }
                    if (keepAliveCleanerEntry == null) {
                        return;
                    }
                } else {
                    return;
                }
            } catch (InterruptedException e2) {
            }
        }
    }
}
