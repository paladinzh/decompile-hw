package com.huawei.systemmanager.spacecleanner.ui.dialog;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import com.huawei.systemmanager.R;

public class CustomDetailDialog extends DialogFragment {
    String msg;

    public CustomDetailDialog(String msg) {
        this.msg = msg;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Builder(getActivity()).setMessage(this.msg).setPositiveButton(R.string.space_clean_list_detail_button, null).show();
    }
}
