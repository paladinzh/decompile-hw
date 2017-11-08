package sun.net.www.http;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import sun.security.action.GetIntegerAction;

public class KeepAliveCache extends HashMap<KeepAliveKey, ClientVector> implements Runnable {
    static final int LIFETIME = 5000;
    static final int MAX_CONNECTIONS = 5;
    static int result = -1;
    private static final long serialVersionUID = -2937172892064557949L;
    private Thread keepAliveTimer = null;

    static int getMaxConnections() {
        if (result == -1) {
            result = ((Integer) AccessController.doPrivileged(new GetIntegerAction("http.maxConnections", 5))).intValue();
            if (result <= 0) {
                result = 5;
            }
        }
        return result;
    }

    public synchronized void put(URL url, Object obj, HttpClient http) {
        boolean startThread = this.keepAliveTimer == null;
        if (!(startThread || this.keepAliveTimer.isAlive())) {
            startThread = true;
        }
        if (startThread) {
            clear();
            KeepAliveCache cache = this;
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    ThreadGroup grp = Thread.currentThread().getThreadGroup();
                    while (true) {
                        ThreadGroup parent = grp.getParent();
                        if (parent != null) {
                            grp = parent;
                        } else {
                            KeepAliveCache.this.keepAliveTimer = new Thread(grp, this, "Keep-Alive-Timer");
                            KeepAliveCache.this.keepAliveTimer.setDaemon(true);
                            KeepAliveCache.this.keepAliveTimer.setPriority(8);
                            KeepAliveCache.this.keepAliveTimer.setContextClassLoader(null);
                            KeepAliveCache.this.keepAliveTimer.start();
                            return null;
                        }
                    }
                }
            });
        }
        KeepAliveKey key = new KeepAliveKey(url, obj);
        ClientVector v = (ClientVector) super.get(key);
        if (v == null) {
            int keepAliveTimeout = http.getKeepAliveTimeout();
            v = new ClientVector(keepAliveTimeout > 0 ? keepAliveTimeout * 1000 : LIFETIME);
            v.put(http);
            super.put(key, v);
        } else {
            v.put(http);
        }
    }

    public synchronized void remove(HttpClient h, Object obj) {
        KeepAliveKey key = new KeepAliveKey(h.url, obj);
        ClientVector v = (ClientVector) super.get(key);
        if (v != null) {
            v.remove((Object) h);
            if (v.empty()) {
                removeVector(key);
            }
        }
    }

    synchronized void removeVector(KeepAliveKey k) {
        super.remove(k);
    }

    public synchronized HttpClient get(URL url, Object obj) {
        ClientVector v = (ClientVector) super.get(new KeepAliveKey(url, obj));
        if (v == null) {
            return null;
        }
        return v.get();
    }

    public void run() {
        do {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
            synchronized (this) {
                long currentTime = System.currentTimeMillis();
                ArrayList<KeepAliveKey> keysToRemove = new ArrayList();
                for (KeepAliveKey key : keySet()) {
                    ClientVector v = (ClientVector) get(key);
                    synchronized (v) {
                        int i = 0;
                        while (i < v.size()) {
                            KeepAliveEntry e2 = (KeepAliveEntry) v.elementAt(i);
                            if (currentTime - e2.idleStartTime <= ((long) v.nap)) {
                                break;
                            }
                            e2.hc.closeServer();
                            i++;
                        }
                        v.subList(0, i).clear();
                        if (v.size() == 0) {
                            keysToRemove.add(key);
                        }
                    }
                }
                for (KeepAliveKey key2 : keysToRemove) {
                    removeVector(key2);
                }
            }
        } while (size() > 0);
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        throw new NotSerializableException();
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        throw new NotSerializableException();
    }
}
