package com.android.gallery3d.common;

import android.os.AsyncTask;
import android.os.Build.VERSION;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

public class AsyncTaskUtil {
    private static Executor sExecutor;
    private static Method sMethodExecuteOnExecutor;

    static {
        if (VERSION.SDK_INT >= 11) {
            try {
                sExecutor = (Executor) AsyncTask.class.getField("THREAD_POOL_EXECUTOR").get(null);
                sMethodExecuteOnExecutor = AsyncTask.class.getMethod("executeOnExecutor", new Class[]{Executor.class, Object[].class});
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (NoSuchFieldException e2) {
                throw new RuntimeException(e2);
            } catch (NoSuchMethodException e3) {
                throw new RuntimeException(e3);
            }
        }
    }

    public static <Param> void executeInParallel(AsyncTask<Param, ?, ?> task, Param... params) {
        if (VERSION.SDK_INT < 11) {
            task.execute(params);
            return;
        }
        try {
            sMethodExecuteOnExecutor.invoke(task, new Object[]{sExecutor, params});
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e2) {
            throw new RuntimeException(e2);
        }
    }

    private AsyncTaskUtil() {
    }
}
