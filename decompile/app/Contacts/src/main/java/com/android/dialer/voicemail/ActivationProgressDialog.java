package com.android.dialer.voicemail;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.VoicemailContract.Status;
import android.telephony.HwTelephonyManager;
import android.widget.Button;
import com.android.common.io.MoreCloseables;
import com.android.contacts.hap.utils.MessageUtils;
import com.android.contacts.hap.utils.MessageUtils.Operator;
import com.android.contacts.util.AsyncTaskExecutor;
import com.android.contacts.util.AsyncTaskExecutors;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import java.util.concurrent.atomic.AtomicBoolean;

public class ActivationProgressDialog extends DialogFragment {
    public static final String TAG = ActivationProgressDialog.class.getSimpleName();
    private boolean mActivityReady = false;
    private boolean mAllowStateLoss;
    protected AsyncTaskExecutor mAsyncTaskExecutor = AsyncTaskExecutors.createAsyncTaskExecutor();
    private boolean mButtonEnable;
    private boolean mCalledSuperDismiss = false;
    private OnClickListener mCancelListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            ActivationProgressDialog.this.dismiss();
        }
    };
    private Context mContext;
    private final Runnable mDismisser = new Runnable() {
        public void run() {
            ActivationProgressDialog.this.superDismiss();
        }
    };
    private FetchResultHandler mFetchStatusHandler;
    private final Handler mHandler = new Handler();
    private long mMinDisplayTime = 1000;
    private Button mNegativeButton;
    private Dialog mOldDialog;
    private ProgressDialog mProgressDialogForActive;
    private long mShowTime = 0;
    private int mSlotId = -1;

    public static class ActivedDialog extends DialogFragment {
        private static final String TAG = ActivedDialog.class.getSimpleName();

        public static void showAllowingStateLoss(FragmentManager fm, int sloteId) {
            FragmentTransaction transaction = fm.beginTransaction();
            ActivedDialog dialog = new ActivedDialog();
            Bundle args = new Bundle();
            args.putInt("arg_slote", sloteId);
            dialog.setArguments(args);
            transaction.add(dialog, TAG);
            transaction.commitAllowingStateLoss();
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Builder builder = new Builder(getContext());
            int displaySimId = getArguments().getInt("arg_slote") + 1;
            builder.setMessage(getString(R.string.voicemail_primary_actived2, new Object[]{Integer.valueOf(displaySimId)})).setPositiveButton(17039370, null);
            return builder.create();
        }
    }

    private class FetchResultHandler extends ContentObserver implements Runnable {
        private final Uri mContentUri;
        private final Handler mFetchResultHandler;
        private AtomicBoolean mIsWaitingForResult = new AtomicBoolean(true);

        public FetchResultHandler(Handler handler, Uri uri) {
            super(handler);
            this.mFetchResultHandler = handler;
            this.mContentUri = uri;
            if (ActivationProgressDialog.this.mContext != null) {
                ActivationProgressDialog.this.mContext.getContentResolver().registerContentObserver(this.mContentUri, true, this);
                this.mFetchResultHandler.postDelayed(this, 20000);
            }
        }

        public void run() {
            if (this.mIsWaitingForResult.get() && ActivationProgressDialog.this.mContext != null) {
                ActivationProgressDialog.this.setFetchActiveStatusTimeout();
            }
        }

        public void destroy() {
            if (this.mIsWaitingForResult.getAndSet(false) && ActivationProgressDialog.this.mContext != null) {
                ActivationProgressDialog.this.mContext.getContentResolver().unregisterContentObserver(this);
                this.mFetchResultHandler.removeCallbacks(this);
            }
        }

        public void onChange(boolean selfChange) {
            ActivationProgressDialog.this.mAsyncTaskExecutor.submit(Tasks.CHECK_CONFIGURATION_STATE_AFTER_CHANGE, new AsyncTask<Void, Void, Boolean>() {
                public Boolean doInBackground(Void... params) {
                    return Boolean.valueOf(ActivationProgressDialog.this.queryConfigurationState(FetchResultHandler.this.mContentUri));
                }

                public void onPostExecute(Boolean state) {
                    if (state.booleanValue() && ActivationProgressDialog.this.mContext != null && FetchResultHandler.this.mIsWaitingForResult.getAndSet(false)) {
                        ActivationProgressDialog.this.mContext.getContentResolver().unregisterContentObserver(FetchResultHandler.this);
                        ActivationProgressDialog.this.activeSuccess();
                    }
                }
            }, new Void[0]);
        }
    }

    public enum Tasks {
        CHECK_CONFIGURATION_STATE_AFTER_CHANGE
    }

    public static void show(FragmentManager fm, Fragment targetFragment, int requestCode) {
        ActivationProgressDialog activingDialog = new ActivationProgressDialog();
        activingDialog.setTargetFragment(targetFragment, requestCode);
        activingDialog.setCancelable(false);
        activingDialog.show(fm, TAG);
        activingDialog.mShowTime = System.currentTimeMillis();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        this.mContext = getContext();
        startActivation();
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.mContext = getContext();
        String message = getString(R.string.voicemail_activating);
        this.mProgressDialogForActive = new ProgressDialog(getContext());
        this.mProgressDialogForActive.setMessage(message);
        this.mProgressDialogForActive.setButton(-2, getString(17039360), this.mCancelListener);
        this.mProgressDialogForActive.show();
        this.mNegativeButton = this.mProgressDialogForActive.getButton(-2);
        this.mNegativeButton.setEnabled(this.mButtonEnable);
        return this.mProgressDialogForActive;
    }

    public void onStart() {
        super.onStart();
        this.mActivityReady = true;
        if (this.mCalledSuperDismiss) {
            superDismiss();
        }
    }

    public void onStop() {
        super.onStop();
        this.mActivityReady = false;
    }

    public void onDismiss(DialogInterface dialog) {
        if (this.mOldDialog == null || this.mOldDialog != dialog) {
            super.onDismiss(dialog);
        }
    }

    public void onDestroyView() {
        this.mOldDialog = getDialog();
        super.onDestroyView();
    }

    public void dismiss() {
        this.mAllowStateLoss = false;
        dismissWhenReady();
    }

    public void dismissAllowingStateLoss() {
        this.mAllowStateLoss = true;
        dismissWhenReady();
    }

    private void dismissWhenReady() {
        long shownTime = System.currentTimeMillis() - this.mShowTime;
        if (shownTime >= this.mMinDisplayTime) {
            this.mHandler.post(this.mDismisser);
            return;
        }
        this.mHandler.postDelayed(this.mDismisser, this.mMinDisplayTime - shownTime);
    }

    private void superDismiss() {
        this.mCalledSuperDismiss = true;
        if (!this.mActivityReady) {
            return;
        }
        if (this.mAllowStateLoss) {
            super.dismissAllowingStateLoss();
        } else {
            super.dismiss();
        }
    }

    public void onDestroy() {
        HwLog.d(TAG, " onDestroy ");
        if (this.mFetchStatusHandler != null) {
            this.mFetchStatusHandler.destroy();
            this.mFetchStatusHandler = null;
        }
        super.onDestroy();
    }

    private void startActivation() {
        int[] slotId = MessageUtils.getSlotIdOfOperator(getContext(), Operator.CM);
        switch (slotId.length) {
            case 1:
                requestActivation(slotId[0]);
                return;
            case 2:
                startDefaultSlotActivation();
                return;
            default:
                return;
        }
    }

    private void requestActivation(int slotId) {
        HwLog.d(TAG, "startActivation : " + slotId);
        this.mFetchStatusHandler = new FetchResultHandler(new Handler(), Status.CONTENT_URI);
        Intent intent = new Intent("com.android.huawei.ACTION_ACTIVATION_VVM");
        intent.putExtra("phone", slotId);
        intent.putExtra("activation", true);
        getContext().sendBroadcast(intent);
    }

    private void startDefaultSlotActivation() {
        this.mSlotId = HwTelephonyManager.getDefault().getDefault4GSlotId();
        requestActivation(this.mSlotId);
    }

    private void setFetchActiveStatusTimeout() {
        HwLog.d(TAG, "setFetchActiveStatusTimeout");
        if (this.mNegativeButton != null) {
            this.mNegativeButton.setEnabled(true);
            this.mButtonEnable = true;
        }
    }

    private void activeSuccess() {
        HwLog.d(TAG, "activeSuccess");
        dismiss();
        Fragment fragment = getTargetFragment();
        HwLog.d(TAG, "getTargetFragment : " + fragment);
        if (fragment != null) {
            fragment.onActivityResult(getTargetRequestCode(), -1, new Intent());
        }
        if (this.mSlotId > -1 && this.mContext != null) {
            ActivedDialog.showAllowingStateLoss(getFragmentManager(), this.mSlotId);
        }
    }

    private boolean queryConfigurationState(Uri voicemailUri) {
        if (voicemailUri == null || this.mContext == null) {
            return false;
        }
        Cursor cursor = this.mContext.getContentResolver().query(voicemailUri, null, null, null, null);
        while (cursor != null) {
            try {
                if (!cursor.moveToNext()) {
                    break;
                }
                int state = cursor.getInt(cursor.getColumnIndex("configuration_state"));
                HwLog.d(TAG, "queryConfigurationState : " + state);
                if (state == 0) {
                    return true;
                }
            } finally {
                MoreCloseables.closeQuietly(cursor);
            }
        }
        MoreCloseables.closeQuietly(cursor);
        return false;
    }
}
