package com.huawei.systemmanager.spacecleanner.ui.secondaryui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.OpenSecondaryParam;
import com.huawei.systemmanager.util.HwLog;

public class SecondaryDialogFragment extends DialogFragment {
    public static final String ARG_SEC_ALL_SELECT = "arg_sec_all_select";
    public static final String ARG_SEC_DATA = "arg_sec_data";
    public static final String ARG_SEC_SELECT = "arg_sec_select_data";
    private static final String TAG = "SecondaryDialogFragment";
    private OnClickListener mListener;

    public void setOnClickListener(OnClickListener listener) {
        if (listener != null) {
            this.mListener = listener;
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        OpenSecondaryParam param = (OpenSecondaryParam) bundle.getParcelable(ARG_SEC_DATA);
        int selectCount = bundle.getInt(ARG_SEC_SELECT);
        boolean isAllSelect = bundle.getBoolean(ARG_SEC_ALL_SELECT);
        if (param == null) {
            return null;
        }
        AlertDialog dialog;
        String dialogTitle = "";
        String messageStr = "";
        String positiveStr = "";
        int dialogTitleResId;
        if (isAllSelect) {
            try {
                dialogTitleResId = param.getAllDialogTitleId();
                if (dialogTitleResId > 0) {
                    dialogTitle = getResources().getString(dialogTitleResId);
                }
            } catch (NotFoundException e) {
                HwLog.e(TAG, "res id is not found");
            }
        } else {
            dialogTitleResId = param.getDialogTitleId();
            if (dialogTitleResId <= 0 || selectCount <= 0) {
                HwLog.e(TAG, "get dialog title failed.selectCount:" + selectCount + " dialogTitleResId:" + dialogTitleResId);
            } else {
                dialogTitle = getResources().getQuantityString(dialogTitleResId, selectCount, new Object[]{Integer.valueOf(selectCount)});
            }
        }
        int dialogContentResId = param.getDialogContentId();
        if (param.getDialogContentId() > 0) {
            messageStr = getResources().getQuantityString(dialogContentResId, selectCount, new Object[]{Integer.valueOf(selectCount)});
        } else {
            HwLog.e(TAG, "getDialogContentId,but DialogContentId is null");
        }
        int positiveButtonIdResId = param.gettDialogPositiveButtonId();
        if (positiveButtonIdResId > 0) {
            positiveStr = getResources().getString(positiveButtonIdResId);
        } else {
            HwLog.e(TAG, "getPositiveButtonIdResId,but positiveButtonIdResId is null");
        }
        if (TextUtils.isEmpty(messageStr)) {
            dialog = new Builder(getActivity()).setTitle(dialogTitle).setPositiveButton(positiveStr, this.mListener).setNegativeButton(R.string.common_cancel, this.mListener).show();
        } else {
            dialog = new Builder(getActivity()).setTitle(dialogTitle).setMessage(messageStr).setPositiveButton(positiveStr, this.mListener).setNegativeButton(R.string.common_cancel, this.mListener).show();
        }
        dialog.getButton(-1).setTextColor(getResources().getColor(R.color.space_clean_delete_text_color));
        return dialog;
    }
}
