package com.android.settings.wifi.ap;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class WifiConfirmDialog implements OnClickListener, OnDismissListener {
    private Context mContext;
    private int mDialogId;
    private boolean mIsDismissByClickButton;
    private WifiConfirmDialogListener mListener;
    private int mResCancelId;
    private int mResOkId;
    private String mStrTitle;
    private View mView;
    private boolean mprocessBackAsNegativeFlag;

    interface WifiConfirmDialogListener {
        void onCancel(int i);

        void onOk(int i);
    }

    private WifiConfirmDialog(Context context, String strTitle) {
        this.mprocessBackAsNegativeFlag = true;
        this.mContext = context;
        this.mStrTitle = strTitle;
        this.mResOkId = 2131625656;
        this.mResCancelId = 2131625657;
        this.mIsDismissByClickButton = false;
    }

    private WifiConfirmDialog(Context context, int resTitleId) {
        this(context, context.getString(resTitleId));
    }

    public WifiConfirmDialog(Context context, View view, String strTitle) {
        this(context, strTitle);
        this.mView = view;
    }

    public WifiConfirmDialog(Context context, String strMsg, int resTitleId) {
        this(context, resTitleId);
        initView(strMsg);
    }

    private void initView(String strMsg) {
        this.mView = LayoutInflater.from(this.mContext).inflate(2130969276, null);
        ((TextView) this.mView.findViewById(2131887548)).setText(strMsg);
    }

    public Dialog showDialog(WifiConfirmDialogListener listener, int dialogId) {
        this.mListener = listener;
        this.mDialogId = dialogId;
        Builder builder = new Builder(this.mContext).setTitle(this.mStrTitle);
        if (this.mResOkId != -1) {
            builder.setPositiveButton(this.mResOkId, this);
        }
        if (this.mResCancelId != -1) {
            builder.setNegativeButton(this.mResCancelId, this);
        }
        if (this.mView != null) {
            builder.setView(this.mView);
        }
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(this);
        dialog.show();
        return dialog;
    }

    public void onClick(DialogInterface dialog, int which) {
        this.mIsDismissByClickButton = true;
        if (this.mListener == null) {
            return;
        }
        if (which == -1) {
            this.mListener.onOk(this.mDialogId);
        } else {
            this.mListener.onCancel(this.mDialogId);
        }
    }

    public void onDismiss(DialogInterface dialog) {
        if (!this.mIsDismissByClickButton && this.mprocessBackAsNegativeFlag && this.mListener != null) {
            this.mListener.onCancel(this.mDialogId);
        }
    }
}
