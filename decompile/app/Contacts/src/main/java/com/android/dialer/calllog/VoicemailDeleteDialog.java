package com.android.dialer.calllog;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.gms.R;

public class VoicemailDeleteDialog extends DialogFragment {
    private static final String TAG = VoicemailDeleteDialog.class.getSimpleName();
    private OnClickListener mClickListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            if (which == -1) {
                Uri voicemailUri = (Uri) VoicemailDeleteDialog.this.getArguments().getParcelable("arg_voicemail_uri");
                if (voicemailUri != null) {
                    CallLogAsyncTaskUtil.deleteVoicemail(VoicemailDeleteDialog.this.getActivity(), voicemailUri, null);
                }
            }
        }
    };
    private AlertDialog mDialog;
    private OnShowListener mShowListener = new OnShowListener() {
        public void onShow(DialogInterface dialog) {
            if (VoicemailDeleteDialog.this.getActivity() != null) {
                VoicemailDeleteDialog.this.mDialog.getButton(-1).setTextColor(VoicemailDeleteDialog.this.getActivity().getResources().getColor(R.color.delete_text_color));
            }
        }
    };

    public static void show(FragmentManager fm, Uri voicemailUri) {
        VoicemailDeleteDialog dialog = new VoicemailDeleteDialog();
        Bundle args = new Bundle();
        args.putParcelable("arg_voicemail_uri", voicemailUri);
        dialog.setArguments(args);
        dialog.show(fm, TAG);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = new Builder(getActivity());
        builder.setTitle(R.string.menu_deleteContact);
        builder.setMessage(R.string.voicemail_delete_notify);
        builder.setNegativeButton(17039360, null);
        builder.setPositiveButton(R.string.menu_deleteContact, this.mClickListener);
        this.mDialog = builder.create();
        this.mDialog.setOnShowListener(this.mShowListener);
        return this.mDialog;
    }
}
