package com.huawei.gallery.util;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;

public class DialogTipsWithCheckBox {
    private int mContentMessageId;
    private Context mContext;
    private OnClickListener mDialogListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            boolean isChecked = DialogTipsWithCheckBox.this.mNoTipsCheckBox != null ? DialogTipsWithCheckBox.this.mNoTipsCheckBox.isChecked() : false;
            switch (which) {
                case -1:
                    DialogTipsWithCheckBox.this.mListener.onPositiveButtonClicked(isChecked);
                    return;
                default:
                    DialogTipsWithCheckBox.this.mListener.onNegativeButtonClicked(isChecked);
                    return;
            }
        }
    };
    private Listener mListener;
    private CheckBox mNoTipsCheckBox;
    private int mTitleId;
    AlertDialog thizz;

    public interface Listener {
        void onNegativeButtonClicked(boolean z);

        void onPositiveButtonClicked(boolean z);
    }

    public DialogTipsWithCheckBox(Context context, int titleId, int contentMessageId, Listener listener) {
        this.mContext = context;
        this.mTitleId = titleId;
        this.mContentMessageId = contentMessageId;
        this.mListener = listener;
    }

    public void show() {
        dissmiss();
        View tipsView = LayoutInflater.from(this.mContext).inflate(R.layout.use_network_dialog_first_page, null);
        this.mNoTipsCheckBox = (CheckBox) tipsView.findViewById(R.id.check_notips);
        ((TextView) tipsView.findViewById(R.id.message_content)).setText(this.mContentMessageId);
        AlertDialog tipsDialog = new Builder(this.mContext).setTitle(this.mTitleId).setCancelable(false).setNegativeButton(R.string.cancel, this.mDialogListener).setPositiveButton(R.string.allow, this.mDialogListener).setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                DialogTipsWithCheckBox.this.thizz = null;
            }
        }).create();
        int padding = this.mContext.getResources().getDimensionPixelSize(R.dimen.alter_dialog_padding_left_right);
        tipsDialog.setView(tipsView, padding, padding, padding, 0);
        tipsDialog.show();
        this.thizz = tipsDialog;
    }

    public void dissmiss() {
        GalleryUtils.dismissDialogSafely(this.thizz, null);
        this.thizz = null;
    }
}
