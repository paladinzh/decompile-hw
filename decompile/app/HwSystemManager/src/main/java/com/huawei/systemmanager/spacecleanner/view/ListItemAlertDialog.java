package com.huawei.systemmanager.spacecleanner.view;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.util.HwLog;

public class ListItemAlertDialog extends DialogFragment {
    public static final String ARG_ARRAY_SPINNER = "arg_array_spinner";
    public static final String ARG_TRASH_SORT = "arg_trash_sort";
    private static final String TAG = "ListItemAlertDialog";
    private CustomArrayAdapter<CharSequence> mCustomArrayAdapter;
    private OnClickListener mListener;

    private static class CustomArrayAdapter<T> extends ArrayAdapter<T> {
        public CustomArrayAdapter(Context ctx, T[] objects) {
            super(ctx, R.layout.simple_list_item_single_choice_lixing, objects);
        }
    }

    public ListItemAlertDialog(OnClickListener listener) {
        this.mListener = listener;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle == null) {
            HwLog.e(TAG, "dialog should have arguments.bundle is null!");
            return null;
        }
        int currentPosition = bundle.getInt(ARG_TRASH_SORT);
        int arrayId = bundle.getInt(ARG_ARRAY_SPINNER);
        if (this.mCustomArrayAdapter == null) {
            this.mCustomArrayAdapter = new CustomArrayAdapter(getActivity(), getActivity().getResources().getStringArray(arrayId));
        }
        AlertDialog dialog = new Builder(getActivity()).setTitle(R.string.order_clean_menu_title).setIconAttribute(16843605).setNegativeButton(R.string.cancel, this.mListener).setSingleChoiceItems(this.mCustomArrayAdapter, currentPosition, this.mListener).create();
        dialog.show();
        return dialog;
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }
}
