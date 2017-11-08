package com.android.server;

import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class LockGuard {
    private static final String TAG = "LockGuard";
    private static ArrayMap<Object, LockInfo> sKnown = new ArrayMap(0, true);

    private static class LockInfo {
        public ArraySet<Object> children;
        public String label;

        private LockInfo() {
            this.children = new ArraySet(0, true);
        }
    }

    private static LockInfo findOrCreateLockInfo(Object lock) {
        LockInfo info = (LockInfo) sKnown.get(lock);
        if (info != null) {
            return info;
        }
        info = new LockInfo();
        info.label = "0x" + Integer.toHexString(System.identityHashCode(lock)) + " [" + new Throwable().getStackTrace()[2].toString() + "]";
        sKnown.put(lock, info);
        return info;
    }

    public static Object guard(Object lock) {
        if (lock == null || Thread.holdsLock(lock)) {
            return lock;
        }
        int i;
        boolean triggered = false;
        LockInfo info = findOrCreateLockInfo(lock);
        for (i = 0; i < info.children.size(); i++) {
            Object child = info.children.valueAt(i);
            if (child != null && Thread.holdsLock(child)) {
                Slog.w(TAG, "Calling thread " + Thread.currentThread().getName() + " is holding " + lockToString(child) + " while trying to acquire " + lockToString(lock), new Throwable());
                triggered = true;
            }
        }
        if (!triggered) {
            for (i = 0; i < sKnown.size(); i++) {
                Object test = sKnown.keyAt(i);
                if (!(test == null || test == lock || !Thread.holdsLock(test))) {
                    ((LockInfo) sKnown.valueAt(i)).children.add(lock);
                }
            }
        }
        return lock;
    }

    public static void installLock(Object lock, String label) {
        findOrCreateLockInfo(lock).label = label;
    }

    private static String lockToString(Object lock) {
        LockInfo info = (LockInfo) sKnown.get(lock);
        if (info != null) {
            return info.label;
        }
        return "0x" + Integer.toHexString(System.identityHashCode(lock));
    }

    public static void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        for (int i = 0; i < sKnown.size(); i++) {
            LockInfo info = (LockInfo) sKnown.valueAt(i);
            pw.println("Lock " + lockToString(sKnown.keyAt(i)) + ":");
            for (int j = 0; j < info.children.size(); j++) {
                pw.println("  Child " + lockToString(info.children.valueAt(j)));
            }
            pw.println();
        }
    }
}
