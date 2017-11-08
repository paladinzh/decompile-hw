package com.autonavi.amap.mapcore;

import com.amap.api.mapcore.util.bq;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionManager extends SingalThread {
    private static final int MAX_THREAD_COUNT = 1;
    private ArrayList<BaseMapLoader> connPool = new ArrayList();
    MapCore mGLMapEngine;
    boolean threadFlag = true;
    private ExecutorService threadPool = Executors.newFixedThreadPool(1);
    private ArrayList<a> threadPoolList = new ArrayList();

    public ConnectionManager(MapCore mapCore) {
        this.mGLMapEngine = mapCore;
    }

    public void shutDown() {
        if (this.connPool != null) {
            this.threadPool.shutdownNow();
        }
    }

    public void insertConntionTask(BaseMapLoader baseMapLoader) {
        synchronized (this.connPool) {
            this.connPool.add(baseMapLoader);
        }
        doAwake();
    }

    void checkListPoolOld() {
        Iterator it = this.threadPoolList.iterator();
        while (it.hasNext()) {
            BaseMapLoader baseMapLoader = ((a) it.next()).a;
            if (!baseMapLoader.isRequestValid() || baseMapLoader.hasFinished()) {
                baseMapLoader.doCancel();
                it.remove();
            }
        }
    }

    private void checkListPool() {
        Collection arrayList = new ArrayList();
        int size = this.threadPoolList.size();
        for (int i = 0; i < size; i++) {
            a aVar = (a) this.threadPoolList.get(i);
            BaseMapLoader baseMapLoader = aVar.a;
            if (!baseMapLoader.isRequestValid() || baseMapLoader.hasFinished()) {
                arrayList.add(aVar);
                baseMapLoader.doCancel();
            }
        }
        this.threadPoolList.removeAll(arrayList);
    }

    public void run() {
        try {
            bq.a();
            doAsyncRequest();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private void doAsyncRequest() {
        while (this.threadFlag) {
            synchronized (this.connPool) {
                checkListPool();
                while (this.connPool.size() > 0) {
                    if (this.threadPoolList.size() > 1) {
                        int i = 1;
                        break;
                    }
                    Runnable aVar = new a((BaseMapLoader) this.connPool.remove(0));
                    this.threadPoolList.add(aVar);
                    if (!this.threadPool.isShutdown()) {
                        this.threadPool.execute(aVar);
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
