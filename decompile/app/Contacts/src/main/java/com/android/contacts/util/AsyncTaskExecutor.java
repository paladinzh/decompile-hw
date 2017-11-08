package com.android.contacts.util;

import android.os.AsyncTask;

public interface AsyncTaskExecutor {
    <T> AsyncTask<T, ?, ?> submit(Object obj, AsyncTask<T, ?, ?> asyncTask, T... tArr);
}
