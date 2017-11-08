package com.android.contacts.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContactsThreadPool {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int THREAD_POOL_MAX_LENGTH = ((CPU_COUNT * 2) + 1);
    private static volatile ContactsThreadPool mInstance = new ContactsThreadPool();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_MAX_LENGTH);

    private ContactsThreadPool() {
    }

    public static ContactsThreadPool getInstance() {
        if (mInstance == null) {
            mInstance = new ContactsThreadPool();
        }
        return mInstance;
    }

    public void execute(Runnable runnable) {
        if (runnable != null) {
            this.threadPool.execute(runnable);
        }
    }

    public ExecutorService getExecutorservice() {
        return this.threadPool;
    }
}
