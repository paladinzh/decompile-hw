package com.android.contacts.interactions;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.os.Bundle;
import com.android.contacts.util.HwLog;
import com.google.common.annotations.VisibleForTesting;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import junit.framework.Assert;

public class TestLoaderManager extends LoaderManager {
    private LoaderManager mDelegate;
    private final HashSet<Integer> mFinishedLoaders = new HashSet();

    public void setDelegate(LoaderManager delegate) {
        if (delegate == null || !(this.mDelegate == null || this.mDelegate == delegate)) {
            throw new IllegalArgumentException("TestLoaderManager cannot be shared");
        }
        this.mDelegate = delegate;
    }

    @VisibleForTesting
    synchronized void waitForLoaders(int... loaderIds) {
        synchronized (this) {
            List<Loader<?>> loaders = new ArrayList(loaderIds.length);
            for (int loaderId : loaderIds) {
                if (!this.mFinishedLoaders.contains(Integer.valueOf(loaderId))) {
                    AsyncTaskLoader<?> loader = (AsyncTaskLoader) this.mDelegate.getLoader(loaderId);
                    if (loader == null) {
                        Assert.fail("Loader does not exist: " + loaderId);
                        return;
                    }
                    loaders.add(loader);
                }
            }
            waitForLoaders((Loader[]) loaders.toArray(new Loader[0]));
        }
    }

    public static void waitForLoaders(Loader<?>... loaders) {
        int i;
        int size = loaders.length;
        Thread[] waitThreads = new Thread[size];
        final boolean[] isFailed = new boolean[size];
        for (i = 0; i < loaders.length; i++) {
            final int j = i;
            final AsyncTaskLoader<?> loader = loaders[i];
            waitThreads[i] = new Thread("LoaderWaitingThread" + i) {
                public void run() {
                    try {
                        loader.waitForLoader();
                    } catch (Throwable e) {
                        HwLog.e("TestLoaderManager", "Exception while waiting for loader: " + loader.getId(), e);
                        isFailed[j] = true;
                    }
                }
            };
            waitThreads[i].start();
        }
        for (Thread thread : waitThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
        }
        for (i = 0; i < size; i++) {
            if (isFailed[i]) {
                Assert.fail("Exception while waiting for loader: " + loaders[i].getId());
            }
        }
    }

    public <D> Loader<D> initLoader(final int id, Bundle args, final LoaderCallbacks<D> callback) {
        return this.mDelegate.initLoader(id, args, new LoaderCallbacks<D>() {
            public Loader<D> onCreateLoader(int id, Bundle args) {
                return callback.onCreateLoader(id, args);
            }

            public void onLoadFinished(Loader<D> loader, D data) {
                callback.onLoadFinished(loader, data);
                synchronized (this) {
                    TestLoaderManager.this.mFinishedLoaders.add(Integer.valueOf(id));
                }
            }

            public void onLoaderReset(Loader<D> loader) {
                callback.onLoaderReset(loader);
            }
        });
    }

    public <D> Loader<D> restartLoader(int id, Bundle args, LoaderCallbacks<D> callback) {
        return this.mDelegate.restartLoader(id, args, callback);
    }

    public void destroyLoader(int id) {
        this.mDelegate.destroyLoader(id);
    }

    public <D> Loader<D> getLoader(int id) {
        return this.mDelegate.getLoader(id);
    }

    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        this.mDelegate.dump(prefix, fd, writer, args);
    }
}
