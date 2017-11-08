package com.huawei.gallery.recycle.utils;

import android.content.ContentResolver;
import android.os.Handler;
import android.os.Message;

public class RecycleAsync {
    private static RecycleAsync sInstance;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if ((msg.obj instanceof ContentResolver) && (RecycleAsync.this.mTask == null || RecycleAsync.this.mTask.isFinished())) {
                        removeMessages(1);
                        RecycleAsync.this.mTask = new RecycleAsyncThread((ContentResolver) msg.obj);
                        RecycleAsync.this.mTask.start();
                        return;
                    }
                    RecycleAsync.this.mHandler.sendMessageDelayed(Message.obtain(this, msg.what, msg.obj), 200);
                    return;
                default:
                    return;
            }
        }
    };
    private RecycleAsyncThread mTask = null;

    private static class RecycleAsyncThread extends Thread {
        private ContentResolver mContentResolver;
        private boolean mIsFinished = false;

        public RecycleAsyncThread(ContentResolver resolver) {
            this.mContentResolver = resolver;
        }

        public void run() {
            RecycleUtils.recoverLocalRecycleTable(this.mContentResolver);
            this.mIsFinished = true;
        }

        public boolean isFinished() {
            return this.mIsFinished;
        }
    }

    public static synchronized RecycleAsync getInstance() {
        RecycleAsync recycleAsync;
        synchronized (RecycleAsync.class) {
            if (sInstance == null) {
                sInstance = new RecycleAsync();
            }
            recycleAsync = sInstance;
        }
        return recycleAsync;
    }

    public void start(ContentResolver resolver) {
        if (RecycleUtils.supportRecycle()) {
            this.mHandler.removeCallbacksAndMessages(null);
            this.mHandler.sendMessage(Message.obtain(this.mHandler, 1, resolver));
        }
    }
}
