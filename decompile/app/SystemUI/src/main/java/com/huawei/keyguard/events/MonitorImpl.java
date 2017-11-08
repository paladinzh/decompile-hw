package com.huawei.keyguard.events;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import com.huawei.keyguard.util.HwLog;

public abstract class MonitorImpl {
    private MonitorBroadcastReceiver mBroadcasetReceiver;
    protected MonitorChangeListener mCallback;
    private MonitorContentObserver mContentObserver;
    final Context mContext;
    protected int mMonitorId;
    private DbWork mWorker;

    public interface MonitorChangeListener {
        void onMonitorChanged(int i, Object obj);

        void setWeatherRegisterFlag(boolean z);
    }

    private class DbWork extends AsyncTask<Void, Void, Object> {
        private DbWork() {
        }

        protected Object doInBackground(Void... params) {
            return MonitorImpl.this.onQueryDatabase();
        }

        protected void onPostExecute(Object result) {
            synchronized (MonitorImpl.this) {
                MonitorImpl.this.mWorker = null;
            }
            if (result == null) {
                HwLog.w("MonitorImpl", "onPostExecute query database fail: " + MonitorImpl.this);
                if (MonitorImpl.this.mMonitorId == 2 && MonitorImpl.this.mCallback != null) {
                    MonitorImpl.this.mCallback.setWeatherRegisterFlag(true);
                }
                return;
            }
            MonitorImpl.this.onGetDatabase(result);
            if (MonitorImpl.this.mCallback != null) {
                MonitorImpl.this.mCallback.onMonitorChanged(MonitorImpl.this.mMonitorId, result);
            }
        }
    }

    private class MonitorBroadcastReceiver extends BroadcastReceiver {
        private MonitorBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwLog.w("MonitorImpl", "onReceive, the intent is null!");
            } else if (MonitorImpl.this.onPreBrocastReceive(intent)) {
                MonitorImpl.this.startAsyncQuery();
            }
        }
    }

    private class MonitorContentObserver extends ContentObserver {
        public MonitorContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            HwLog.i("MonitorImpl", "onChange " + MonitorImpl.this);
            if (MonitorImpl.this.onPreContentChange(selfChange)) {
                MonitorImpl.this.startAsyncQuery();
            } else {
                HwLog.w("MonitorImpl", "onChange - onPreContentChange return false");
            }
        }
    }

    abstract Object onQueryDatabase();

    public abstract void register();

    public MonitorImpl(Context context, MonitorChangeListener callback, int monitorId) {
        if (context == null || callback == null) {
            HwLog.w("MonitorImpl", "MonitorImpl context = " + context + ", callback = " + callback);
        }
        this.mContext = context;
        this.mCallback = callback;
        this.mMonitorId = monitorId;
    }

    public void unRegister() {
        if (this.mContext == null) {
            HwLog.w("MonitorImpl", "unRegister context is null");
            return;
        }
        if (this.mContentObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mContentObserver);
        }
        if (this.mBroadcasetReceiver != null) {
            this.mContext.unregisterReceiver(this.mBroadcasetReceiver);
        }
    }

    void registerContent(Uri uri) {
        if (this.mContext != null) {
            this.mContentObserver = new MonitorContentObserver(null);
            this.mContext.getContentResolver().registerContentObserver(uri, true, this.mContentObserver);
        }
    }

    void registerBroadcast(IntentFilter filter, String permission) {
        if (this.mContext != null) {
            this.mBroadcasetReceiver = new MonitorBroadcastReceiver();
            this.mContext.registerReceiver(this.mBroadcasetReceiver, filter, permission, null);
        }
    }

    protected void startAsyncQuery() {
        synchronized (this) {
            if (this.mWorker != null) {
                HwLog.w("MonitorImpl", "database changes while still querying, cancel last query " + this);
                this.mWorker.cancel(true);
            }
            this.mWorker = new DbWork();
            this.mWorker.execute(new Void[0]);
        }
    }

    protected boolean onPreBrocastReceive(Intent intent) {
        return true;
    }

    protected boolean onPreContentChange(boolean selfChange) {
        return true;
    }

    protected void onGetDatabase(Object obj) {
    }
}
