package com.huawei.gallery.photoshare.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import com.android.gallery3d.util.GalleryUtils;

public class PhotoShareAlertDialogFragment extends DialogFragment {
    private boolean isNeedNegativeButton = true;
    private onDialogButtonClickListener mListener;

    public interface onDialogButtonClickListener {
        void onPositiveClick();
    }

    public void setOnDialogButtonClickListener(onDialogButtonClickListener listener) {
        this.mListener = listener;
    }

    public static PhotoShareAlertDialogFragment newInstance(String title, String message, String positiveButtonString, boolean positiveButtonIsRed) {
        PhotoShareAlertDialogFragment dialog = new PhotoShareAlertDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("message", message);
        bundle.putString("positiveButtonString", positiveButtonString);
        bundle.putBoolean("positiveButtonIsRed", positiveButtonIsRed);
        dialog.setArguments(bundle);
        return dialog;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        String message = getArguments().getString("message");
        String positiveButtonString = getArguments().getString("positiveButtonString");
        boolean positiveButtonIsRed = getArguments().getBoolean("positiveButtonIsRed");
        Builder builder = new Builder(getActivity()).setTitle(title).setMessage(message).setPositiveButton(positiveButtonString, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (PhotoShareAlertDialogFragment.this.mListener != null) {
                    PhotoShareAlertDialogFragment.this.mListener.onPositiveClick();
                }
            }
        });
        if (this.isNeedNegativeButton) {
            builder.setNegativeButton(17039360, null);
        }
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        if (positiveButtonIsRed) {
            GalleryUtils.setTextColor(alertDialog.getButton(-1), getActivity().getResources());
        }
        return alertDialog;
    }
}
