package com.android.contacts.vcard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;
import com.android.contacts.hap.util.RefelctionUtils;
import com.android.contacts.hap.util.UnsupportedException;
import com.android.contacts.util.HwLog;
import com.android.contacts.vcard.VCardService.MyBinder;
import com.google.android.gms.R;
import com.google.common.collect.Sets;
import java.util.Set;

public class CancelActivity extends Activity implements ServiceConnection {
    private static Set<Integer> mRequestedJobIds = Sets.newHashSet();
    private final CancelListener mCancelListener = new CancelListener();
    private AlertDialog mDialog;
    private String mDisplayName;
    private int mJobId;
    private int mType;

    private class CancelListener implements OnClickListener, OnCancelListener {
        private CancelListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            if (CancelActivity.mRequestedJobIds.contains(Integer.valueOf(CancelActivity.this.mJobId))) {
                CancelActivity.mRequestedJobIds.remove(Integer.valueOf(CancelActivity.this.mJobId));
            }
            CancelActivity.this.finishActivity();
        }

        public void onCancel(DialogInterface dialog) {
            if (CancelActivity.mRequestedJobIds.contains(Integer.valueOf(CancelActivity.this.mJobId))) {
                CancelActivity.mRequestedJobIds.remove(Integer.valueOf(CancelActivity.this.mJobId));
            }
            CancelActivity.this.finishActivity();
        }
    }

    private class RequestCancelListener implements OnClickListener {
        private RequestCancelListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            if (CancelActivity.mRequestedJobIds.contains(Integer.valueOf(CancelActivity.this.mJobId))) {
                CancelActivity.mRequestedJobIds.remove(Integer.valueOf(CancelActivity.this.mJobId));
            }
            CancelActivity.this.bindService(new Intent(CancelActivity.this, VCardService.class), CancelActivity.this, 1);
        }
    }

    private void finishActivity() {
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
        this.mDialog = null;
        finish();
        overridePendingTransition(0, 0);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            RefelctionUtils.invokeMethod("setHwFloating", getWindow(), new Object[]{Boolean.valueOf(true)});
        } catch (UnsupportedException e) {
            HwLog.e("VCardCancel", "UnsupportedException");
        }
        Intent lIntent = getIntent();
        if (lIntent == null || lIntent.getData() == null) {
            finishActivity();
            return;
        }
        Uri uri = lIntent.getData();
        int jobId = Integer.parseInt(uri.getQueryParameter("job_id"));
        if (savedInstanceState != null) {
            this.mJobId = savedInstanceState.getInt("job_id");
            this.mDisplayName = savedInstanceState.getString("display_name");
            this.mType = savedInstanceState.getInt("type");
            showDialog(R.id.dialog_cancel_confirmation);
        } else if (mRequestedJobIds.contains(Integer.valueOf(jobId))) {
            finishActivity();
        } else {
            this.mJobId = jobId;
            this.mDisplayName = uri.getQueryParameter("display_name");
            this.mType = Integer.parseInt(uri.getQueryParameter("type"));
            mRequestedJobIds.add(Integer.valueOf(this.mJobId));
            showDialog(R.id.dialog_cancel_confirmation);
        }
    }

    protected Dialog onCreateDialog(int id, Bundle bundle) {
        Builder builder;
        switch (id) {
            case R.id.dialog_cancel_confirmation:
                String title;
                builder = new Builder(this);
                if (this.mType == 1) {
                    title = getString(R.string.cancel_import_confirmation_message, new Object[]{this.mDisplayName});
                    builder.setPositiveButton(R.string.button_cancel_import_text, new RequestCancelListener());
                } else {
                    title = getString(R.string.cancel_export_confirmation_message, new Object[]{this.mDisplayName});
                    builder.setPositiveButton(R.string.button_cancel_export_text, new RequestCancelListener());
                }
                builder.setMessage(title).setOnCancelListener(this.mCancelListener).setNegativeButton(R.string.button_continue_text, this.mCancelListener);
                this.mDialog = builder.create();
                this.mDialog.setMessageNotScrolling();
                return this.mDialog;
            case R.id.dialog_cancel_failed:
                builder = new Builder(this).setTitle(R.string.cancel_vcard_import_or_export_failed).setIconAttribute(16843605).setOnCancelListener(this.mCancelListener).setPositiveButton(R.string.contact_known_button_text, this.mCancelListener);
                View view = getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(getString(R.string.failed_reason_index, new Object[]{getString(R.string.fail_reason_unknown)}));
                builder.setView(view);
                this.mDialog = builder.create();
                return this.mDialog;
            default:
                HwLog.w("VCardCancel", "Unknown dialog id: " + id);
                return super.onCreateDialog(id, bundle);
        }
    }

    public void onServiceConnected(ComponentName name, IBinder binder) {
        try {
            ((MyBinder) binder).getService().handleCancelRequest(new CancelRequest(this.mJobId, this.mDisplayName));
            finishActivity();
        } finally {
            unbindService(this);
        }
    }

    public void onServiceDisconnected(ComponentName name) {
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("job_id", this.mJobId);
        outState.putString("display_name", this.mDisplayName);
        outState.putInt("type", this.mType);
        super.onSaveInstanceState(outState);
    }
}
