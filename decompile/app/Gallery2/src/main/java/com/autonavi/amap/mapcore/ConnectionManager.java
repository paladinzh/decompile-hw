package com.autonavi.amap.mapcore;

import com.amap.api.mapcore.util.fc;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionManager extends SingalThread {
    private static final int MAX_THREAD_COUNT = 1;
    private ArrayList<BaseMapLoader> connPool = new ArrayList();
    boolean threadFlag = true;
    private ExecutorService threadPool = Executors.newFixedThreadPool(1);
    private ArrayList<AsMapRequestor> threadPoolList = new ArrayList();

    public void shutDown() {
        synchronized (this.connPool) {
            if (this.connPool != null) {
                Iterator it = this.connPool.iterator();
                while (it.hasNext()) {
                    try {
                        ((BaseMapLoader) it.next()).doCancel();
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                }
                this.connPool.clear();
                this.threadPoolList.clear();
                this.threadPool.shutdownNow();
            }
        }
    }

    public void insertConntionTask(BaseMapLoader baseMapLoader) {
        synchronized (this.connPool) {
            this.connPool.add(baseMapLoader);
        }
        doAwake();
    }

    void checkListPoolOld() throws UnsupportedEncodingException {
        Iterator it = this.threadPoolList.iterator();
        while (it.hasNext()) {
            BaseMapLoader baseMapLoader = ((AsMapRequestor) it.next()).mMapLoader;
            if (!baseMapLoader.isRequestValid() || baseMapLoader.hasFinished()) {
                baseMapLoader.doCancel();
                it.remove();
            }
        }
    }

    private void checkListPool() throws UnsupportedEncodingException {
        try {
            Collection arrayList = new ArrayList();
            int size = this.threadPoolList.size();
            for (int i = 0; i < size; i++) {
                AsMapRequestor asMapRequestor = (AsMapRequestor) this.threadPoolList.get(i);
                BaseMapLoader baseMapLoader = asMapRequestor.mMapLoader;
                if (baseMapLoader.hasFinished() || !baseMapLoader.isRequestValid()) {
                    arrayList.add(asMapRequestor);
                    baseMapLoader.doCancel();
                }
            }
            this.threadPoolList.removeAll(arrayList);
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public void run() {
        try {
            fc.a();
            doAsyncRequest();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private void doAsyncRequest() throws UnsupportedEncodingException {
        while (this.threadFlag) {
            synchronized (this.connPool) {
                checkListPool();
                while (this.connPool.size() > 0) {
                    if (this.threadPoolList.size() > 1) {
                        int i = 1;
                        break;
                    }
                    Runnable asMapRequestor = new AsMapRequestor((BaseMapLoader) this.connPool.remove(0));
                    this.threadPoolList.add(asMapRequestor);
                    if (!this.threadPool.isShutdown()) {
                        this.threadPool.execute(asMapRequestor);
                    }
                }
                Object obj = null;
            }
            if (obj != null) {
                try {
                    sleep(30);
                } catch (Exception e) {
                }
            } else if (this.threadFlag) {
                try {
                    doWait();
                } catch (Throwable th) {
                }
            }
        }
    }
}
