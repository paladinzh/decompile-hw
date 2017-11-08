package com.android.contacts.interactions;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.gms.R;

public abstract class GroupNameDialogFragment extends DialogFragment {
    protected abstract int getTitleResourceId();

    protected abstract void initializeGroupLabelEditText(EditText editText);

    protected abstract void onCompleted(String str);

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int themeResId = getActivity().getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
        Context applicationContext = getActivity().getApplicationContext();
        if (themeResId == 0) {
            themeResId = 16974123;
        }
        Context lContext = new ContextThemeWrapper(applicationContext, themeResId);
        View view = LayoutInflater.from(lContext).inflate(R.layout.group_name_dialog, null);
        Builder builder = new Builder(getActivity());
        final EditText editText = (EditText) view.findViewById(R.id.group_label);
        initializeGroupLabelEditText(editText);
        builder.setTitle(getTitleResourceId());
        builder.setView(view);
        editText.requestFocus();
        builder.setPositiveButton(lContext.getResources().getString(R.string.description_save_button), new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int whichButton) {
                GroupNameDialogFragment.this.onCompleted(editText.getText().toString().trim());
                GroupNameDialogFragment.this.hideSoftInputWindow(editText);
            }
        });
        builder.setNegativeButton(17039360, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int whichButton) {
                GroupNameDialogFragment.this.hideSoftInputWindow(editText);
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new OnShowListener() {
            public void onShow(DialogInterface dialogInterface) {
                GroupNameDialogFragment.this.updateOkButtonState(dialog, editText);
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                GroupNameDialogFragment.this.updateOkButtonState(dialog, editText);
            }
        });
        dialog.getWindow().setSoftInputMode(5);
        return dialog;
    }

    void updateOkButtonState(AlertDialog dialog, EditText editText) {
        Button okButton = dialog.getButton(-1);
        if (okButton != null) {
            boolean z;
            if (TextUtils.isEmpty(editText.getText().toString().trim())) {
                z = false;
            } else {
                z = true;
            }
            okButton.setEnabled(z);
        }
    }

    public void hideSoftInputWindow(EditText aEditTextView) {
        Activity activity = getActivity();
        if (aEditTextView != null && activity != null) {
            ((InputMethodManager) activity.getSystemService("input_method")).hideSoftInputFromWindow(aEditTextView.getWindowToken(), 0);
        }
    }
}
