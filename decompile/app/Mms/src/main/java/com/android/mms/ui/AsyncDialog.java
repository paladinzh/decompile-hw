package com.android.mms.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;

public class AsyncDialog {
    private final Activity mActivity;
    private final Handler mHandler;
    private ProgressDialog mProgressDialog;
    private Runnable mShowProgressDialogRunnable = new Runnable() {
        public void run() {
            if (AsyncDialog.this.mActivity != null && !AsyncDialog.this.mActivity.isFinishing() && !AsyncDialog.this.mActivity.isDestroyed() && AsyncDialog.this.mProgressDialog != null) {
                AsyncDialog.this.mProgressDialog.show();
            }
        }
    };

    private class ModalDialogAsyncTask extends AsyncTask<Runnable, Void, Void> {
        final Runnable mPostExecuteTask;

        public ModalDialogAsyncTask(int dialogStringId, Runnable postExecuteTask) {
            this.mPostExecuteTask = postExecuteTask;
            if (AsyncDialog.this.mProgressDialog == null) {
                AsyncDialog.this.mProgressDialog = createProgressDialog();
            }
            AsyncDialog.this.mProgressDialog.setMessage(AsyncDialog.this.mActivity.getText(dialogStringId));
        }

        private ProgressDialog createProgressDialog() {
            ProgressDialog dialog = new ProgressDialog(AsyncDialog.this.mActivity);
            dialog.setIndeterminate(true);
            dialog.setProgressStyle(0);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            return dialog;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            AsyncDialog.this.mHandler.postDelayed(AsyncDialog.this.mShowProgressDialogRunnable, 500);
        }

        protected Void doInBackground(Runnable... params) {
            Void voidR = null;
            if (params != null) {
                int i = 0;
                while (i < params.length) {
                    try {
                        params[i].run();
                        i++;
                    } finally {
                        Handler -get1 = AsyncDialog.this.mHandler;
                        voidR = AsyncDialog.this.mShowProgressDialogRunnable;
                        -get1.removeCallbacks(voidR);
                    }
                }
            }
            return voidR;
        }

        protected void onPostExecute(Void result) {
            if (!AsyncDialog.this.mActivity.isFinishing()) {
                if (AsyncDialog.this.mProgressDialog != null && AsyncDialog.this.mProgressDialog.isShowing()) {
                    AsyncDialog.this.mProgressDialog.dismiss();
                }
                if (this.mPostExecuteTask != null) {
                    this.mPostExecuteTask.run();
                }
            }
        }
    }

    public AsyncDialog(Activity activity) {
        this.mActivity = activity;
        this.mHandler = new Handler();
    }

    public void runAsync(Runnable backgroundTask, Runnable postExecuteTask, int dialogStringId) {
        new ModalDialogAsyncTask(dialogStringId, postExecuteTask).execute(new Runnable[]{backgroundTask});
    }

    public void clearPendingProgressDialog() {
        this.mHandler.removeCallbacks(this.mShowProgressDialogRunnable);
        this.mProgressDialog = null;
    }

    public Activity getActivity() {
        return this.mActivity;
    }
}
