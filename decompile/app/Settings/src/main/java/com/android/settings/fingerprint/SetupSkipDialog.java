package com.android.settings.fingerprint;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.android.setupwizardlib.util.SystemBarHelper;

public class SetupSkipDialog extends DialogFragment implements OnClickListener {
    public static SetupSkipDialog newInstance(boolean isFrpSupported) {
        SetupSkipDialog dialog = new SetupSkipDialog();
        Bundle args = new Bundle();
        args.putBoolean("frp_supported", isFrpSupported);
        dialog.setArguments(args);
        return dialog;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = onCreateDialogBuilder().create();
        SystemBarHelper.hideSystemBars(dialog);
        return dialog;
    }

    @NonNull
    public Builder onCreateDialogBuilder() {
        int i;
        Bundle args = getArguments();
        Builder negativeButton = new Builder(getContext()).setPositiveButton(2131624656, this).setNegativeButton(2131625657, this);
        if (args.getBoolean("frp_supported")) {
            i = 2131624654;
        } else {
            i = 2131624655;
        }
        return negativeButton.setMessage(i);
    }

    public void onClick(DialogInterface dialog, int button) {
        switch (button) {
            case -1:
                Activity activity = getActivity();
                activity.setResult(11);
                activity.finish();
                return;
            default:
                return;
        }
    }

    public void show(FragmentManager manager) {
        show(manager, "skip_dialog");
    }
}
