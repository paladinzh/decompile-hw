package com.huawei.systemmanager.optimize.process.Predicate;

import com.google.common.base.Predicate;
import com.huawei.systemmanager.comm.concurrent.HsmExecutor;
import com.huawei.systemmanager.util.HwLog;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public abstract class FutureTaskPredicate<T, V> implements Predicate<V> {
    private static final String TAG = "FutureTaskPredicate";
    private T mResultData;
    private FutureTask<T> mTask;

    protected abstract T doInbackground() throws Exception;

    public void executeTask() {
        this.mTask = new FutureTask(new Callable<T>() {
            public T call() throws Exception {
                return FutureTaskPredicate.this.doInbackground();
            }
        });
        HsmExecutor.executeTask(this.mTask, getClass().getSimpleName());
    }

    protected final T getResult() {
        if (this.mTask == null) {
            HwLog.e(TAG, getClass().getSimpleName() + " mTask is null, maybe you forget call executeTask method!");
            return this.mResultData;
        }
        if (this.mResultData == null) {
            try {
                this.mResultData = this.mTask.get();
            } catch (Exception e) {
                HwLog.e(TAG, getClass().getSimpleName() + " get result error!");
                e.printStackTrace();
            }
        }
        return this.mResultData;
    }
}
