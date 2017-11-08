package com.android.contacts.util;

import android.os.AsyncTask;
import java.lang.ref.WeakReference;

public abstract class WeakAsyncTask<Params, Progress, Result, WeakTarget> extends AsyncTask<Params, Progress, Result> {
    protected WeakReference<WeakTarget> mTarget;

    protected abstract Result doInBackground(WeakTarget weakTarget, Params... paramsArr);

    public WeakAsyncTask(WeakTarget target) {
        this.mTarget = new WeakReference(target);
    }

    protected final void onPreExecute() {
        WeakTarget target = this.mTarget.get();
        if (target != null) {
            onPreExecute(target);
        }
    }

    protected final Result doInBackground(Params... params) {
        WeakTarget target = this.mTarget.get();
        if (target != null) {
            return doInBackground(target, params);
        }
        return null;
    }

    protected final void onPostExecute(Result result) {
        WeakTarget target = this.mTarget.get();
        if (target != null) {
            onPostExecute(target, result);
        }
    }

    protected void onPreExecute(WeakTarget weakTarget) {
    }

    protected void onPostExecute(WeakTarget weakTarget, Result result) {
    }
}
