package com.huawei.systemmanager.spacecleanner.ui.dialog;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.normalscan.AppProcessTrashItem;
import com.huawei.systemmanager.util.HwLog;

public abstract class LongClickDialog<T extends ITrashItem> extends DialogFragment implements OnClickListener {
    public static final String TAG = "LongClickDialog";
    private OnClickListener mDialogBtnClicker;
    protected T mTrashItem;

    public static class ProcessTrashClickDialog extends LongClickDialog<AppProcessTrashItem> {
        boolean mProtectState;

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (this.mTrashItem == null) {
                HwLog.e(LongClickDialog.TAG, "onCreate, trash item is null");
            } else {
                this.mProtectState = ((AppProcessTrashItem) this.mTrashItem).isProtect();
            }
        }

        protected int getMessageResId() {
            if (this.mProtectState) {
                return R.string.space_clean_longclick_dialog_content_removefrom_memory_white_change;
            }
            return R.string.space_clean_longclick_dialog_content_addto_memory_white_change;
        }

        protected int getPositiveBtnResId() {
            if (this.mProtectState) {
                return R.string.space_clean_longclick_dialog_btn_remove;
            }
            return R.string.space_clean_longclick_dialog_btn_add;
        }

        protected void doPositiveBtn() {
            if (this.mTrashItem == null) {
                HwLog.e(LongClickDialog.TAG, "doPositiveBtn, but trashitem is null");
                return;
            }
            boolean z;
            AppProcessTrashItem appProcessTrashItem = (AppProcessTrashItem) this.mTrashItem;
            if (this.mProtectState) {
                z = false;
            } else {
                z = true;
            }
            appProcessTrashItem.doSetItemProtect(z);
        }
    }

    protected abstract int getMessageResId();

    protected abstract int getPositiveBtnResId();

    public void setParam(OnClickListener dialogClicker, T trashItem) {
        this.mDialogBtnClicker = dialogClicker;
        this.mTrashItem = trashItem;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = new Builder(getActivity());
        builder.setTitle(R.string.common_dialog_title_tip);
        builder.setMessage(getMessageResId());
        builder.setPositiveButton(getPositiveBtnResId(), this);
        builder.setNegativeButton(R.string.cancel, this);
        return builder.create();
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case -2:
                doNegativeBtn();
                break;
            case -1:
                doPositiveBtn();
                break;
        }
        if (this.mDialogBtnClicker != null) {
            this.mDialogBtnClicker.onClick(dialog, which);
        }
    }

    protected void doPositiveBtn() {
    }

    protected void doNegativeBtn() {
    }

    public static DialogFragment getFragment(OnClickListener clicker, ITrashItem trashItem) {
        if (trashItem == null) {
            HwLog.i(TAG, "getFragment, trashItem is null");
            return null;
        } else if (!(trashItem instanceof AppProcessTrashItem)) {
            return null;
        } else {
            ProcessTrashClickDialog dialog = new ProcessTrashClickDialog();
            dialog.setParam(clicker, (AppProcessTrashItem) trashItem);
            HwLog.i(TAG, "getFragment, trashItem is AppProcessTrashItem");
            return dialog;
        }
    }
}
