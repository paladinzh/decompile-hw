package com.huawei.systemmanager.adblock.ui.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import com.huawei.systemmanager.R;

public class InstallAppmarketDialog extends AlertDialog implements OnClickListener {
    private final Callback mCallback;

    public interface Callback {
        void onChooseInstallAppmarket(boolean z);
    }

    public InstallAppmarketDialog(Context context, int themeResId, Callback callback) {
        super(context, themeResId);
        getWindow().setType(2003);
        String message = context.getString(R.string.ad_appmarket_message);
        String cancel = context.getString(R.string.cancel);
        String install = context.getString(R.string.large_file_dialog_btn_text_install);
        setTitle(R.string.ad_appmarket_title);
        setMessage(message);
        setButton(-2, cancel, this);
        setButton(-1, install, this);
        this.mCallback = callback;
    }

    public void onClick(DialogInterface dialog, int which) {
        this.mCallback.onChooseInstallAppmarket(-1 == which);
    }

    public void onBackPressed() {
        super.onBackPressed();
        this.mCallback.onChooseInstallAppmarket(false);
    }
}
