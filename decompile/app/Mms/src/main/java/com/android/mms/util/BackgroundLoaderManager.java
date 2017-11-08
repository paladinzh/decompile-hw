package com.android.mms.util;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.ThreadEx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executor;

public abstract class BackgroundLoaderManager {
    protected final Handler mCallbackHandler;
    protected final HashMap<Uri, Set<ItemLoadedCallback>> mCallbacks = new HashMap();
    protected final Executor mExecutor = ThreadEx.createExecutor(2, 2, 5, getTag());
    protected final Set<Uri> mPendingTaskUris = new HashSet();

    public abstract String getTag();

    public BackgroundLoaderManager(Context context, Handler handler) {
        this.mCallbackHandler = handler;
    }

    public void onLowMemory() {
        clear();
    }

    public void clear() {
    }

    public boolean addCallback(Uri uri, ItemLoadedCallback callback) {
        if (MLog.isLoggable("BackgroundLoaderManager", 3)) {
            MLog.d("BackgroundLoaderManager", "Adding image callback " + callback);
        }
        if (uri == null) {
            throw new NullPointerException("uri is null");
        } else if (callback == null) {
            throw new NullPointerException("callback is null");
        } else {
            Set<ItemLoadedCallback> callbacks = (Set) this.mCallbacks.get(uri);
            if (callbacks == null) {
                callbacks = new HashSet(4);
                this.mCallbacks.put(uri, callbacks);
            }
            callbacks.add(callback);
            return true;
        }
    }

    public void cancelCallback(ItemLoadedCallback callback) {
        if (MLog.isLoggable("BackgroundLoaderManager", 3)) {
            MLog.d("BackgroundLoaderManager", "Cancelling image callback " + callback);
        }
        for (Entry<Uri, Set<ItemLoadedCallback>> entry : this.mCallbacks.entrySet()) {
            ((Set) entry.getValue()).remove(callback);
        }
    }

    protected static <T> ArrayList<T> asList(Set<T> source) {
        return new ArrayList(source);
    }
}
