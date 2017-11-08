package com.huawei.systemmanager.comm.component;

import android.os.AsyncTask;
import android.view.View;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class SimpleAsyncLoader<Container extends View, DataSource, Result> {
    private static final Executor DEFAULT_EXECUTOR = Executors.newFixedThreadPool(3);
    private static final int DEFAULT_THREAD_NUM = 3;
    public static final String TAG = SimpleAsyncLoader.class.getSimpleName();
    private boolean mEffect = true;
    private Map<Container, LoadTask> mLoadTasks = new WeakHashMap();

    private class LoadTask extends AsyncTask<Void, Void, Result> {
        private final DataSource mData;
        private final WeakReference<Container> mViewRef;

        private LoadTask(Container view, DataSource data) {
            this.mViewRef = new WeakReference(view);
            this.mData = data;
        }

        protected Result doInBackground(Void... params) {
            return SimpleAsyncLoader.this.onLoadInBackground(this.mData);
        }

        protected void onPostExecute(Result result) {
            if (SimpleAsyncLoader.this.onLoadFinishBeforeCheck(this.mData, result)) {
                View view = (View) this.mViewRef.get();
                if (view != null) {
                    LoadTask task = (LoadTask) view.getTag();
                    if (task != null && task == this) {
                        SimpleAsyncLoader.this.onDataLoadFinish(view, this.mData, result);
                    }
                }
            }
        }
    }

    protected abstract void onDataLoadFinish(Container container, DataSource dataSource, Result result);

    protected abstract Result onLoadInBackground(DataSource dataSource);

    public void loadData(Container view, DataSource data) {
        view.setTag(null);
        if (!onPreLoadData(view, data)) {
            this.mLoadTasks.remove(view);
            LoadTask task = new LoadTask(view, data);
            view.setTag(task);
            if (this.mEffect) {
                task.executeOnExecutor(getExecutor(), new Void[]{(Void) null});
            } else {
                this.mLoadTasks.put(view, task);
            }
        }
    }

    public void setEffect(boolean effect) {
        if (this.mEffect != effect) {
            this.mEffect = effect;
            if (this.mEffect) {
                for (LoadTask task : this.mLoadTasks.values()) {
                    task.executeOnExecutor(getExecutor(), new Void[]{(Void) null});
                }
                this.mLoadTasks.clear();
            }
        }
    }

    protected boolean onPreLoadData(Container container, DataSource dataSource) {
        return false;
    }

    protected boolean onLoadFinishBeforeCheck(DataSource dataSource, Result result) {
        return true;
    }

    public void clear() {
        this.mLoadTasks.clear();
    }

    protected Executor getExecutor() {
        return DEFAULT_EXECUTOR;
    }
}
