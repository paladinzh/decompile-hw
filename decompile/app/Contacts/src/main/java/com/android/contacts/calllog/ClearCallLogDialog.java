package com.android.contacts.calllog;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.android.contacts.activities.PeopleActivity;
import com.google.android.gms.R;

public class ClearCallLogDialog extends DialogFragment {
    private clickListener mListener;
    private int mMode = 0;

    public interface clickListener {
        void clickdone(int i);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mode", this.mMode);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.mMode = savedInstanceState.getInt("mode", 1);
        }
        switch (this.mMode) {
            case 0:
                return onCreateNotifaDialog();
            case 1:
                return ProgressDialog.show(getActivity(), "", getString(R.string.clearCallLogProgress_title), true, false);
            case 2:
                return onCreateClearCallLogDialog();
            default:
                return onCreateClearCallLogDialog();
        }
    }

    private Dialog onCreateClearCallLogDialog() {
        OnClickListener okListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (ClearCallLogDialog.this.mListener != null) {
                    ClearCallLogDialog.this.mListener.clickdone(2);
                }
                ClearCallLogDialog.this.dismiss();
            }
        };
        Builder builder = new Builder(getActivity());
        builder.setTitle(R.string.clearCallLogConfirmation_title);
        builder.setIconAttribute(16843605);
        builder.setNegativeButton(17039360, null);
        builder.setPositiveButton(R.string.clearCallLogConfirmation_clearButton, okListener);
        builder.setCancelable(true);
        if (!isAdded() || getActivity() == null) {
            builder.setMessage(R.string.all_calllog_clear_confirmation_message);
        } else {
            View view = getActivity().getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
            ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(getString(R.string.all_calllog_clear_confirmation_message));
            builder.setView(view);
        }
        final AlertDialog aAlertDialog = builder.create();
        if (getActivity() instanceof PeopleActivity) {
            ((PeopleActivity) getActivity()).mGlobalDialogReference = aAlertDialog;
        }
        aAlertDialog.setOnShowListener(new OnShowListener() {
            public void onShow(DialogInterface dialog) {
                Button aPositiveButton = aAlertDialog.getButton(-1);
                if (aPositiveButton != null && aPositiveButton.getText().toString().equalsIgnoreCase(ClearCallLogDialog.this.getString(R.string.clearCallLogConfirmation_clearButton)) && ClearCallLogDialog.this.getActivity() != null) {
                    aPositiveButton.setTextColor(ClearCallLogDialog.this.getActivity().getResources().getColor(R.color.delete_text_color));
                }
            }
        });
        return aAlertDialog;
    }

    private Dialog onCreateNotifaDialog() {
        OnClickListener listener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (ClearCallLogDialog.this.mListener != null) {
                    ClearCallLogDialog.this.mListener.clickdone(0);
                }
                ClearCallLogDialog.this.dismiss();
            }
        };
        Builder builder = new Builder(getActivity());
        builder.setTitle(R.string.voicemail_notifa_title);
        builder.setMessage(R.string.voicemail_notifa_content);
        builder.setNegativeButton(17039360, null);
        builder.setPositiveButton(R.string.voicemail_notifa_active, listener);
        return builder.create();
    }

    public static ClearCallLogDialog showNotifacation(FragmentManager fragmentManager) {
        ClearCallLogDialog fragment = new ClearCallLogDialog();
        fragment.mMode = 0;
        fragment.show(fragmentManager, "ClearCallLogDialog");
        return fragment;
    }

    public static ClearCallLogDialog show(FragmentManager fragmentManager) {
        ClearCallLogDialog fragment = new ClearCallLogDialog();
        fragment.mMode = 2;
        fragment.show(fragmentManager, "ClearCallLogDialog");
        return fragment;
    }

    public static ClearCallLogDialog showProgress(FragmentManager fragmentManager) {
        ClearCallLogDialog fragment = new ClearCallLogDialog();
        fragment.mMode = 1;
        fragment.show(fragmentManager, "progressTag");
        return fragment;
    }

    public void setListener(clickListener aListener) {
        this.mListener = aListener;
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() != null && (getActivity() instanceof PeopleActivity)) {
            ((PeopleActivity) getActivity()).mGlobalDialogReference = null;
        }
    }
}
