package com.huawei.deskclock.ui;

import android.os.AsyncTask;
import com.android.util.Log;

public class FragmentReplacerTask extends AsyncTask<FragmentReplacer, Void, Void> {
    private FragmentReplacer mFragmentReplacer;

    protected Void doInBackground(FragmentReplacer... params) {
        this.mFragmentReplacer = params[0];
        Log.dRelease("ReplacerTask", "doInBackground thid = " + Thread.currentThread().getId());
        this.mFragmentReplacer.checkFragmentsAndPrepareAndWait();
        return null;
    }

    protected void onPostExecute(Void entityList) {
        Log.dRelease("ReplacerTask", "onPostExecute thid = " + Thread.currentThread().getId());
        if (isCancelled()) {
            Log.d("ReplacerTask", "onPostExecute return");
            return;
        }
        if (this.mFragmentReplacer != null) {
            this.mFragmentReplacer.replaceDummyFragments();
        }
    }
}
