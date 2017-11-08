package com.google.android.gms.common.api;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* compiled from: Unknown */
public abstract class zzk {
    private static final ExecutorService zzaaz = Executors.newFixedThreadPool(2);

    public static ExecutorService zznG() {
        return zzaaz;
    }
}
