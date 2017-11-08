package com.android.contacts.list;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import com.android.contacts.util.AsyncTaskExecutors;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.MVersionUpgradeUtils.ProviderStatus;
import com.google.common.collect.Lists;
import java.util.ArrayList;

public class ProviderStatusWatcher extends ContentObserver {
    private static final boolean DEBUG = HwLog.HWDBG;
    private static final String[] PROJECTION = new String[]{"status"};
    private static ProviderStatusWatcher sInstance;
    private final Context mContext;
    private final Handler mHandler = new Handler();
    private final ArrayList<ProviderStatusListener> mListeners = Lists.newArrayList();
    private LoaderTask mLoaderTask;
    private Integer mProviderStatus;
    private final Object mSignal = new Object();
    private final Runnable mStartLoadingRunnable = new Runnable() {
        public void run() {
            ProviderStatusWatcher.this.startLoading();
        }
    };
    private int mStartRequestedCount;

    public interface ProviderStatusListener {
        void onProviderStatusChange();
    }

    private class LoaderTask extends AsyncTask<Void, Void, Boolean> {
        private LoaderTask() {
        }

        protected Boolean doInBackground(Void... params) {
            Cursor cursor;
            try {
                Boolean valueOf;
                cursor = ProviderStatusWatcher.this.mContext.getContentResolver().query(ProviderStatus.CONTENT_URI, ProviderStatusWatcher.PROJECTION, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        ProviderStatusWatcher.this.mProviderStatus = Integer.valueOf(cursor.getInt(0));
                        valueOf = Boolean.valueOf(true);
                        cursor.close();
                        synchronized (ProviderStatusWatcher.this.mSignal) {
                            ProviderStatusWatcher.this.mSignal.notifyAll();
                        }
                        return valueOf;
                    }
                    cursor.close();
                }
                valueOf = Boolean.valueOf(false);
                synchronized (ProviderStatusWatcher.this.mSignal) {
                    ProviderStatusWatcher.this.mSignal.notifyAll();
                }
                return valueOf;
            } catch (Throwable th) {
                synchronized (ProviderStatusWatcher.this.mSignal) {
                    ProviderStatusWatcher.this.mSignal.notifyAll();
                }
            }
        }

        protected void onCancelled(Boolean result) {
            cleanUp();
        }

        protected void onPostExecute(Boolean loaded) {
            cleanUp();
            if (loaded != null && loaded.booleanValue()) {
                ProviderStatusWatcher.this.notifyListeners();
            }
        }

        private void cleanUp() {
            ProviderStatusWatcher.this.mLoaderTask = null;
        }
    }

    public static synchronized ProviderStatusWatcher getInstance(Context context) {
        ProviderStatusWatcher providerStatusWatcher;
        synchronized (ProviderStatusWatcher.class) {
            if (sInstance == null) {
                sInstance = new ProviderStatusWatcher(context);
            }
            providerStatusWatcher = sInstance;
        }
        return providerStatusWatcher;
    }

    private ProviderStatusWatcher(Context context) {
        super(null);
        this.mContext = context.getApplicationContext();
    }

    public void addListener(ProviderStatusListener listener) {
        this.mListeners.add(listener);
    }

    public void removeListener(ProviderStatusListener listener) {
        this.mListeners.remove(listener);
    }

    private void notifyListeners() {
        if (DEBUG) {
            HwLog.d("ProviderStatusWatcher", "notifyListeners: " + this.mListeners.size());
        }
        if (isStarted()) {
            for (ProviderStatusListener listener : this.mListeners) {
                listener.onProviderStatusChange();
            }
        }
    }

    private boolean isStarted() {
        return this.mStartRequestedCount > 0;
    }

    public void start() {
        int i = this.mStartRequestedCount + 1;
        this.mStartRequestedCount = i;
        if (i == 1) {
            this.mContext.getContentResolver().registerContentObserver(ProviderStatus.CONTENT_URI, false, this);
            startLoading();
            if (DEBUG) {
                HwLog.d("ProviderStatusWatcher", "Start observing");
            }
        }
    }

    public void stop() {
        if (isStarted()) {
            int i = this.mStartRequestedCount - 1;
            this.mStartRequestedCount = i;
            if (i == 0) {
                this.mHandler.removeCallbacks(this.mStartLoadingRunnable);
                this.mContext.getContentResolver().unregisterContentObserver(this);
                if (DEBUG) {
                    HwLog.d("ProviderStatusWatcher", "Stop observing");
                }
            }
            return;
        }
        HwLog.e("ProviderStatusWatcher", "Already stopped");
    }

    public int getProviderStatus() {
        waitForLoaded();
        if (this.mProviderStatus == null) {
            return 1;
        }
        return this.mProviderStatus.intValue();
    }

    private void waitForLoaded() {
        if (this.mProviderStatus == null) {
            if (this.mLoaderTask == null) {
                startLoading();
            }
            synchronized (this.mSignal) {
                try {
                    this.mSignal.wait(1000);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private void startLoading() {
        if (this.mLoaderTask == null) {
            if (DEBUG) {
                HwLog.d("ProviderStatusWatcher", "Start loading");
            }
            this.mLoaderTask = new LoaderTask();
            this.mLoaderTask.executeOnExecutor(AsyncTaskExecutors.THREAD_POOL_EXECUTOR, new Void[0]);
        }
    }

    public void onChange(boolean selfChange, Uri uri) {
        if (ProviderStatus.CONTENT_URI.equals(uri)) {
            HwLog.i("ProviderStatusWatcher", "Provider status changed.");
            this.mHandler.removeCallbacks(this.mStartLoadingRunnable);
            this.mHandler.post(this.mStartLoadingRunnable);
        }
    }
}
