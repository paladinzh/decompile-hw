package com.android.contacts.hap.copy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class CancelActivity extends Activity {
    private final String LOG_TAG = "CopyContactCancel";
    private boolean lExportToSimFlag;
    private boolean lImportToSimFlag;
    private final CancelListener mCancelListener = new CancelListener();
    private AlertDialog mDialog;
    private String mDisplayName;
    private int mJobId;

    private class CancelListener implements OnClickListener, OnCancelListener {
        private CancelListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            CancelActivity.this.finishActivity();
        }

        public void onCancel(DialogInterface dialog) {
            CancelActivity.this.finishActivity();
        }
    }

    private class RequestCancelListener implements OnClickListener {
        private RequestCancelListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            CancelActivity.this.startService(CopyContactService.createCancelRequestIntent(CancelActivity.this.getApplicationContext(), CancelActivity.this.mJobId, CancelActivity.this.mDisplayName));
            CancelActivity.this.finishActivity();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent lIntent = getIntent();
        if (lIntent == null || lIntent.getData() == null) {
            finishActivity();
            return;
        }
        Uri uri = getIntent().getData();
        this.lExportToSimFlag = getIntent().getBooleanExtra("export_to_sim", false);
        this.lImportToSimFlag = getIntent().getBooleanExtra("import_to_sim", false);
        this.mJobId = Integer.parseInt(uri.getQueryParameter("job_id"));
        this.mDisplayName = uri.getQueryParameter("display_name");
        showDialog(R.id.dialog_cancel_confirmation);
    }

    protected Dialog onCreateDialog(int id, Bundle bundle) {
        Builder builder;
        switch (id) {
            case R.id.dialog_cancel_confirmation:
                String title;
                builder = new Builder(this);
                if ("phone".equalsIgnoreCase(this.mDisplayName)) {
                    if (this.lExportToSimFlag) {
                        title = getString(R.string.cancel_export_contacts_confirm_title, new Object[]{getString(R.string.phoneLabelsGroup)});
                    } else if (this.lImportToSimFlag) {
                        title = getString(R.string.cancel_import_contacts_confirm_title, new Object[]{getString(R.string.phoneLabelsGroup)});
                    } else {
                        title = getString(R.string.cancel_copy_contacts_confirmation_titles, new Object[]{getString(R.string.phoneLabelsGroup)});
                    }
                } else if (this.lExportToSimFlag) {
                    title = getString(R.string.cancel_export_contacts_confirm_title, new Object[]{this.mDisplayName});
                } else if (this.lImportToSimFlag) {
                    title = getString(R.string.cancel_import_contacts_confirm_title, new Object[]{this.mDisplayName});
                } else {
                    title = getString(R.string.cancel_copy_contacts_confirmation_titles, new Object[]{this.mDisplayName});
                }
                builder.setMessage(title).setOnCancelListener(this.mCancelListener).setNegativeButton(R.string.button_continue_text, this.mCancelListener);
                if (this.lExportToSimFlag) {
                    builder.setPositiveButton(R.string.button_cancel_export_text, new RequestCancelListener());
                } else if (this.lImportToSimFlag) {
                    builder.setPositiveButton(R.string.button_cancel_import_text, new RequestCancelListener());
                } else {
                    builder.setPositiveButton(R.string.button_cancel_copy_text, new RequestCancelListener());
                }
                this.mDialog = builder.create();
                this.mDialog.setMessageNotScrolling();
                return this.mDialog;
            case R.id.dialog_cancel_failed:
                builder = new Builder(this).setTitle(R.string.cancel_copy_contacts_failed).setIconAttribute(16843605).setOnCancelListener(this.mCancelListener).setPositiveButton(R.string.contact_known_button_text, this.mCancelListener);
                View view = getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(getString(R.string.failed_reason_index, new Object[]{getString(R.string.fail_reason_unknown)}));
                builder.setView(view);
                this.mDialog = builder.create();
                return this.mDialog;
            default:
                HwLog.w("CopyContactCancel", "Unknown dialog id: " + id);
                return super.onCreateDialog(id, bundle);
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
}
