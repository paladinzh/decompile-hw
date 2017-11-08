package com.huawei.systemmanager.comm.component;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import com.huawei.systemmanager.util.HwLog;
import java.util.concurrent.Executor;

public abstract class TaskFragment<Params, Progress, Result> extends DialogFragment {
    public static final String ARG_EXECUTOR = "arg_executor";
    public static final String ARG_PARAM = "arg_param";
    public static final String TAG = "TaskFragment";
    private volatile boolean mCancelled;
    private Params mParams;
    private InnerTask<Params, Progress, Result> mTask;

    private static class InnerTask<Params2, Progress2, Result2> extends AsyncTask<Params2, Progress2, Result2> {
        private final TaskFragment<Params2, Progress2, Result2> mOwner;

        public InnerTask(TaskFragment<Params2, Progress2, Result2> owner) {
            this.mOwner = owner;
        }

        protected void onPreExecute() {
            this.mOwner.onPreExecute();
        }

        protected Result2 doInBackground(Params2... params) {
            return this.mOwner.doInBackground(params[0]);
        }

        protected void onCancelled(Result2 result) {
            this.mOwner.mCancelled = true;
            this.mOwner.taskFinished(result);
        }

        protected void onPostExecute(Result2 result) {
            this.mOwner.taskFinished(result);
        }
    }

    protected abstract Result doInBackground(Params params);

    public TaskFragment<Params, Progress, Result> setParams(Params params) {
        this.mParams = params;
        return this;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setCancelable(false);
        if (this.mTask == null) {
            this.mTask = new InnerTask(this);
        }
        if (this.mParams == null) {
            throw new IllegalArgumentException("Params is null!");
        }
        Executor executor = getExecutor();
        if (executor != null) {
            this.mTask.executeOnExecutor(executor, new Object[]{this.mParams});
            return;
        }
        this.mTask.execute(new Object[]{this.mParams});
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new ProgressDialog(getActivity());
    }

    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (this.mTask != null) {
            HwLog.e(TAG, "Dialog is dismissing,but the task is not null!!");
            this.mCancelled = true;
            this.mTask.cancel(true);
            this.mTask = null;
        }
    }

    public void onResume() {
        super.onResume();
        if (this.mTask == null) {
            dismiss();
        }
    }

    public void setMessage(CharSequence message) {
        ProgressDialog dialog = (ProgressDialog) getDialog();
        if (dialog == null) {
            HwLog.e(TAG, "setMessage dialog is null");
        } else {
            dialog.setMessage(message);
        }
    }

    public void taskFinished(Result result) {
        if (isResumed()) {
            dismiss();
        }
        this.mTask = null;
        onPostExecute(this.mCancelled, result);
    }

    public void taskStart() {
        onPreExecute();
    }

    protected Executor getExecutor() {
        return null;
    }

    protected void onPreExecute() {
    }

    protected void onPostExecute(boolean isCancel, Result result) {
    }
}
