package com.android.contacts.calllog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import com.android.contacts.hap.delete.ExtendedContactSaveService;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class DeleteCallLogDialog extends DialogFragment {
    private long[] deleteIds;
    private int mCalllogcount;
    private boolean mIsAllSelected;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CharSequence string;
        if (savedInstanceState != null) {
            this.deleteIds = savedInstanceState.getLongArray("delete_ids");
            this.mCalllogcount = savedInstanceState.getInt("calllog_count");
            this.mIsAllSelected = savedInstanceState.getBoolean("all_selected");
        }
        OnClickListener okListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                final ProgressDialog progressDialog = ProgressDialog.show(DeleteCallLogDialog.this.getActivity(), "", DeleteCallLogDialog.this.getResources().getQuantityText(R.plurals.deleteCallLogProgress_title, DeleteCallLogDialog.this.mCalllogcount), true, false);
                AsyncTask<Context, Void, Void> task = new AsyncTask<Context, Void, Void>() {
                    protected Void doInBackground(Context... params) {
                        Context context = params[0];
                        context.startService(ExtendedContactSaveService.createRemoveCallLogEntriesIntent(context, DeleteCallLogDialog.this.deleteIds));
                        return null;
                    }

                    protected void onPostExecute(Void result) {
                        try {
                            progressDialog.dismiss();
                        } catch (IllegalArgumentException e) {
                            HwLog.e("DeleteCallLogDialog", e.toString(), e);
                        }
                        Activity localRef = DeleteCallLogDialog.this.getActivity();
                        if (localRef != null) {
                            localRef.finish();
                        }
                    }
                };
                progressDialog.show();
                task.execute(new Context[]{DeleteCallLogDialog.this.getActivity().getApplicationContext()});
            }
        };
        Builder builder = new Builder(getActivity());
        if (this.mIsAllSelected) {
            string = getResources().getString(R.string.delete_callog_all_title);
        } else {
            string = getResources().getQuantityString(R.plurals.delete_callog_multi_title, this.deleteIds.length, new Object[]{Integer.valueOf(this.deleteIds.length)});
        }
        final AlertDialog lAlertDialog = builder.setMessage(string).setIconAttribute(16843605).setNegativeButton(17039360, null).setPositiveButton(R.string.menu_deleteContact, okListener).setCancelable(true).create();
        lAlertDialog.setMessageNotScrolling();
        lAlertDialog.setOnShowListener(new OnShowListener() {
            public void onShow(DialogInterface dialog) {
                Button aPositiveButton = lAlertDialog.getButton(-1);
                if (aPositiveButton != null && DeleteCallLogDialog.this.getActivity() != null) {
                    aPositiveButton.setTextColor(DeleteCallLogDialog.this.getActivity().getResources().getColor(R.color.delete_text_color));
                }
            }
        });
        return lAlertDialog;
    }

    public static void show(FragmentManager fragmentManager, long[] ids, int calllogcount, boolean allselected) {
        DeleteCallLogDialog fragment = new DeleteCallLogDialog();
        fragment.setSelection(ids);
        fragment.setCallLogCount(calllogcount);
        fragment.setIsAllSelected(allselected);
        fragment.show(fragmentManager, "DeleteCallLogDialog");
    }

    void setSelection(long[] ids) {
        this.deleteIds = ids;
    }

    public void setCallLogCount(int calllogcount) {
        this.mCalllogcount = calllogcount;
    }

    public void setIsAllSelected(boolean allselected) {
        this.mIsAllSelected = allselected;
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putLongArray("delete_ids", this.deleteIds);
        outState.putInt("calllog_count", this.mCalllogcount);
        outState.putBoolean("all_selected", this.mIsAllSelected);
        super.onSaveInstanceState(outState);
    }
}
