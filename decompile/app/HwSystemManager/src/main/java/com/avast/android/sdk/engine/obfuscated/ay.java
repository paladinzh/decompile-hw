package com.avast.android.sdk.engine.obfuscated;

import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

/* compiled from: Unknown */
public class ay<E> {
    private final HashMap<String, BlockingQueue<E>> a;
    private final HashMap<String, Boolean> b;
    private final Comparator<E> c;
    private final Semaphore d;
    private final Semaphore e;

    public ay(Comparator<E> comparator) {
        if (comparator != null) {
            this.a = new HashMap();
            this.b = new HashMap();
            this.c = comparator;
            this.d = new Semaphore(0);
            this.e = new Semaphore(1);
            return;
        }
        throw new IllegalArgumentException("Comparator must not be null");
    }

    private E b() {
        E c = c();
        for (String str : this.a.keySet()) {
            BlockingQueue blockingQueue = (BlockingQueue) this.a.get(str);
            if (blockingQueue.peek() != null && this.c.compare(c, blockingQueue.peek()) == 0) {
                blockingQueue.poll();
                break;
            }
        }
        return c;
    }

    private E c() {
        E e = null;
        for (String str : this.a.keySet()) {
            E peek = ((BlockingQueue) this.a.get(str)).peek();
            if (peek != null) {
                if (e == null) {
                    if (peek != null) {
                        e = peek;
                    }
                }
                if (peek != null) {
                    if (this.c.compare(e, peek) < 0) {
                        e = peek;
                    }
                }
            }
            peek = e;
            e = peek;
        }
        return e;
    }

    public E a() throws java.lang.InterruptedException {
        /* JADX: method processing error */
/*
Error: java.lang.NullPointerException
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.fixSplitterBlock(BlockFinish.java:63)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:34)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r4 = this;
        r0 = 0;
    L_0x0001:
        if (r0 == 0) goto L_0x0004;
    L_0x0003:
        return r0;
    L_0x0004:
        r1 = r4.d;	 Catch:{ InterruptedException -> 0x004a, all -> 0x0045 }
        r1.acquire();	 Catch:{ InterruptedException -> 0x004a, all -> 0x0045 }
        r2 = r4.a;	 Catch:{ InterruptedException -> 0x004a, all -> 0x0045 }
        monitor-enter(r2);	 Catch:{ InterruptedException -> 0x004a, all -> 0x0045 }
        r0 = r4.b();	 Catch:{ Exception -> 0x0025 }
    L_0x0010:
        monitor-exit(r2);	 Catch:{ all -> 0x0027, InterruptedException -> 0x002d }
        if (r0 != 0) goto L_0x0001;
    L_0x0013:
        r1 = r4.d;	 Catch:{ InterruptedException -> 0x0023 }
        r1.release();	 Catch:{ InterruptedException -> 0x0023 }
        r1 = r4.e;	 Catch:{ InterruptedException -> 0x0023 }
        r1.acquire();	 Catch:{ InterruptedException -> 0x0023 }
        r1 = r4.e;	 Catch:{ InterruptedException -> 0x0023 }
        r1.release();	 Catch:{ InterruptedException -> 0x0023 }
        goto L_0x0001;
    L_0x0023:
        r0 = move-exception;
        throw r0;
    L_0x0025:
        r1 = move-exception;
        goto L_0x0010;
    L_0x0027:
        r1 = move-exception;
        r3 = r1;
        r1 = r0;
        r0 = r3;
    L_0x002b:
        monitor-exit(r2);	 Catch:{ all -> 0x004f }
        throw r0;	 Catch:{ all -> 0x0027, InterruptedException -> 0x002d }
    L_0x002d:
        r0 = move-exception;
    L_0x002e:
        throw r0;	 Catch:{ all -> 0x002f }
    L_0x002f:
        r0 = move-exception;
    L_0x0030:
        if (r1 == 0) goto L_0x0033;
    L_0x0032:
        throw r0;
    L_0x0033:
        r1 = r4.d;	 Catch:{ InterruptedException -> 0x0043 }
        r1.release();	 Catch:{ InterruptedException -> 0x0043 }
        r1 = r4.e;	 Catch:{ InterruptedException -> 0x0043 }
        r1.acquire();	 Catch:{ InterruptedException -> 0x0043 }
        r1 = r4.e;	 Catch:{ InterruptedException -> 0x0043 }
        r1.release();	 Catch:{ InterruptedException -> 0x0043 }
        goto L_0x0032;
    L_0x0043:
        r0 = move-exception;
        throw r0;
    L_0x0045:
        r1 = move-exception;
        r3 = r1;
        r1 = r0;
        r0 = r3;
        goto L_0x0030;
    L_0x004a:
        r1 = move-exception;
        r3 = r1;
        r1 = r0;
        r0 = r3;
        goto L_0x002e;
    L_0x004f:
        r0 = move-exception;
        goto L_0x002b;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.avast.android.sdk.engine.obfuscated.ay.a():E");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void a(String str) {
        synchronized (this.a) {
            BlockingQueue blockingQueue = (BlockingQueue) this.a.get(str);
            if (blockingQueue == null) {
                blockingQueue = new LinkedBlockingQueue();
                this.a.put(str, blockingQueue);
                this.b.put(str, Boolean.valueOf(true));
            }
            ao.a("Going to pause queue " + str);
            int size = blockingQueue.size();
            if (((Boolean) this.b.put(str, Boolean.valueOf(false))).booleanValue()) {
            } else {
                ao.a("Queue " + str + " already paused");
            }
        }
    }

    public boolean a(String str, E e) {
        boolean offer;
        synchronized (this.a) {
            BlockingQueue blockingQueue = (BlockingQueue) this.a.get(str);
            if (blockingQueue == null) {
                blockingQueue = new LinkedBlockingQueue();
                this.a.put(str, blockingQueue);
                this.b.put(str, Boolean.valueOf(true));
            }
            offer = blockingQueue.offer(e);
            if (offer) {
                if (((Boolean) this.b.get(str)).booleanValue()) {
                    this.d.release();
                }
            }
        }
        return offer;
    }

    public void b(String str) {
        synchronized (this.a) {
            BlockingQueue blockingQueue = (BlockingQueue) this.a.get(str);
            if (blockingQueue == null) {
                blockingQueue = new LinkedBlockingQueue();
                this.a.put(str, blockingQueue);
                this.b.put(str, Boolean.valueOf(true));
            }
            BlockingQueue blockingQueue2 = blockingQueue;
            ao.a("Going to resume queue " + str);
            if (!((Boolean) this.b.put(str, Boolean.valueOf(true))).booleanValue()) {
                if (blockingQueue2.size() > 0) {
                    ao.a("Going to post " + blockingQueue2.size() + " permits due to resuming of " + str);
                    this.d.release(blockingQueue2.size());
                }
            }
            ao.a("Queue " + str + " resumed");
        }
    }
}
