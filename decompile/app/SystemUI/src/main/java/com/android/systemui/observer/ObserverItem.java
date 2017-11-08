package com.android.systemui.observer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import com.android.systemui.HwSystemUIApplication;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.SystemUiUtil;
import java.util.LinkedList;

public abstract class ObserverItem<T> extends ContentObserver {
    protected String TAG = "ObserverItem";
    boolean hasRegister = false;
    Context mContext = null;
    LinkedList<OnChangeListener> mListeners = new LinkedList();
    private Object mSyncObj = new Object();

    public interface OnChangeListener {
        void onChange(Object obj);
    }

    public abstract Uri getUri();

    public abstract T getValue();

    public abstract void onChange();

    public ObserverItem(Handler handler) {
        super(handler);
    }

    public boolean init() {
        this.mContext = SystemUiUtil.getContextCurrentUser(HwSystemUIApplication.getContext());
        if (getUri() != null) {
            this.mContext.getContentResolver().registerContentObserver(getUri(), false, this, -1);
            HwLog.i(this.TAG, "register Observer for " + getUri());
            this.hasRegister = true;
        } else {
            HwLog.e(this.TAG, "uri is null for " + getClass().getSimpleName());
        }
        onChange(false);
        HwLog.i(this.TAG, getUri().toString() + " init value:" + getValue());
        return true;
    }

    public ContentResolver getContentResolve() {
        return this.mContext.getContentResolver();
    }

    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            public boolean runInThread() {
                ObserverItem.this.onChange();
                return super.runInThread();
            }

            public void runInUI() {
                ObserverItem.this.notifyChanged();
                super.runInUI();
            }
        });
        HwLog.i(this.TAG, getUri().toString() + " has changed to " + getValue());
    }

    public void notifyChanged() {
        synchronized (this.mSyncObj) {
            LinkedList<OnChangeListener> mTempList = (LinkedList) this.mListeners.clone();
        }
        for (OnChangeListener listener : mTempList) {
            listener.onChange(getValue());
        }
    }

    public final void release() {
        if (this.hasRegister) {
            getContentResolve().unregisterContentObserver(this);
            this.hasRegister = false;
            return;
        }
        HwLog.i(this.TAG, "has aready unregitster:" + getClass().getSimpleName());
    }

    public T getValue(int type, Object defaultVal) {
        T value = getValue();
        if (value == null) {
            return defaultVal;
        }
        return value;
    }

    public Object getValue(int valueType) {
        return getValue();
    }

    public void addOnChangeListener(OnChangeListener listener) {
        synchronized (this.mSyncObj) {
            this.mListeners.add(listener);
        }
    }

    public void removeOnChangeListener(OnChangeListener listener) {
        synchronized (this.mSyncObj) {
            this.mListeners.remove(listener);
        }
    }
}
