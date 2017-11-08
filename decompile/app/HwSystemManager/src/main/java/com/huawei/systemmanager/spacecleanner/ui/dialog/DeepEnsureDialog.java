package com.huawei.systemmanager.spacecleanner.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import com.huawei.systemmanager.R;

public class DeepEnsureDialog extends DialogFragment {
    public static final String TAG = "DeepEnsureDialog";
    private OnClickListener mDetailClicker;
    private DialogInterface.OnClickListener mDialogBtnClicker;

    public void setDialogClicker(DialogInterface.OnClickListener dialogClicker, OnClickListener detailClicker) {
        this.mDialogBtnClicker = dialogClicker;
        this.mDetailClicker = detailClicker;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity ac = getActivity();
        Builder builder = new Builder(ac);
        builder.setTitle(R.string.space_clean_deep_ensure_dialog_title);
        View view = ac.getLayoutInflater().inflate(R.layout.spaceclean_deep_scan_ensure_dialog, null);
        view.findViewById(R.id.detail_txt).setOnClickListener(this.mDetailClicker);
        builder.setView(view);
        builder.setPositiveButton(R.string.confirm, this.mDialogBtnClicker);
        builder.setNegativeButton(R.string.cancel, null);
        return builder.create();
    }
}
