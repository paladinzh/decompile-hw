package com.huawei.openalliance.ad.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy;
import java.util.concurrent.TimeUnit;

/* compiled from: Unknown */
public class k {
    public static final Executor a = new ThreadPoolExecutor(3, 7, 1, TimeUnit.SECONDS, new LinkedBlockingQueue(512), new DiscardOldestPolicy());
    public static final Executor b = new ThreadPoolExecutor(3, 7, 1, TimeUnit.SECONDS, new LinkedBlockingQueue(512), new DiscardOldestPolicy());
    public static final Executor c = new ThreadPoolExecutor(3, 7, 1, TimeUnit.SECONDS, new LinkedBlockingQueue(512), new DiscardOldestPolicy());
}
