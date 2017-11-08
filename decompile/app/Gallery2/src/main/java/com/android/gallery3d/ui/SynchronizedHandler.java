package com.android.gallery3d.ui;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.android.gallery3d.util.GalleryLog;

public class SynchronizedHandler extends Handler {
    private GLRoot mRoot;

    public SynchronizedHandler(GLRoot root) {
        this.mRoot = root;
    }

    public SynchronizedHandler(Looper looper, GLRoot root) {
        super(looper);
        this.mRoot = root;
    }

    public void dispatchMessage(Message message) {
        GLRoot root = this.mRoot;
        long start = System.currentTimeMillis();
        if (root == null) {
            super.dispatchMessage(message);
            printExecuteTime(start, message);
            return;
        }
        root.lockRenderThread();
        try {
            start = System.currentTimeMillis();
            super.dispatchMessage(message);
        } finally {
            printExecuteTime(start, message);
            root.unlockRenderThread();
        }
    }

    public void setGLRoot(GLRoot root) {
        this.mRoot = root;
    }

    private void printExecuteTime(long start, Message message) {
        long executeTime = System.currentTimeMillis() - start;
        if (executeTime >= 50) {
            GalleryLog.w("SynchronizedHandler", this + ", message:" + message.what + " has execute " + executeTime + "ms.");
        }
    }
}
