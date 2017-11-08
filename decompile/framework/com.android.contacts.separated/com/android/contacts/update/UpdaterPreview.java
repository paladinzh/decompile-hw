package com.android.contacts.update;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;

public class UpdaterPreview extends Activity {
    private CancelListener mCancelListener = new CancelListener();
    private Updater updater;

    private class CancelListener implements OnClickListener, OnCancelListener {
        private CancelListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            UpdaterPreview.this.finish();
        }

        public void onCancel(DialogInterface dialog) {
            UpdaterPreview.this.finish();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.updater = (Updater) UpdateHelper.getUpdaterInstance(getIntent().getIntExtra("fileId", 1), this);
        showDialog(1);
    }

    protected Dialog onCreateDialog(int id, Bundle args) {
        if (this.updater == null) {
            return null;
        }
        Builder builder = new Builder(this).setTitle(this.updater.getTitle()).setOnCancelListener(this.mCancelListener).setNegativeButton(17039360, this.mCancelListener);
        final int item = this.updater.getItem();
        CharSequence[] items = new CharSequence[3];
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        stringBuilder.append(getString(2131363260));
        stringBuilder.append('\n');
        int indexToBeSpanned = stringBuilder.length();
        stringBuilder.append(getString(2131363261));
        stringBuilder.setSpan(new RelativeSizeSpan(0.8f), indexToBeSpanned, stringBuilder.length(), 33);
        items[0] = stringBuilder;
        items[1] = getString(2131363262);
        items[2] = getString(2131363672);
        builder.setSingleChoiceItems(items, item, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (item != which) {
                    UpdaterPreview.this.updater.setItem(which, true);
                }
                UpdaterPreview.this.finish();
            }
        });
        return builder.create();
    }

    public void finish() {
        removeDialog(1);
        super.finish();
        overridePendingTransition(0, 0);
    }
}
