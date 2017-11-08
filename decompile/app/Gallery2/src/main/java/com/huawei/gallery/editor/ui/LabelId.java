package com.huawei.gallery.editor.ui;

public class LabelId {
    private static long sNextId = 1;

    public static synchronized long nextId() {
        long j;
        synchronized (LabelId.class) {
            j = sNextId;
            sNextId = 1 + j;
        }
        return j;
    }
}
