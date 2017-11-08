package com.android.dialer.greeting.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.HwLog;
import com.android.dialer.greeting.GreetingsSaveService;
import com.google.android.gms.R;

public class GreetingSaveDialog extends DialogFragment {
    private static final String TAG = GreetingSaveDialog.class.getSimpleName();
    private AlertDialog mDialog;
    private EditText mEditText;
    private Handler mHandler = new Handler();
    private OnClickListener mListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            if (which == -1) {
                Bundle bundle = GreetingSaveDialog.this.getArguments();
                String file = bundle.getString("_data");
                ContentValues greeting = (ContentValues) bundle.getParcelable("name");
                if (greeting == null) {
                    HwLog.e(GreetingSaveDialog.TAG, "GreetingSaveDialog.mListener greeting null, missing name");
                    greeting = new ContentValues();
                }
                greeting.put("name", GreetingSaveDialog.this.mEditText.getText().toString());
                GreetingSaveDialog.this.getActivity().startService(GreetingsSaveService.createSaveIntent(GreetingSaveDialog.this.getActivity(), greeting, file));
                StatisticalHelper.report(5044);
                GreetingSaveDialog.this.dismiss();
            }
        }
    };
    private Button mPositiveButton;
    private OnShowListener mShowListener = new OnShowListener() {
        public void onShow(DialogInterface dialog) {
            GreetingSaveDialog.this.mPositiveButton = GreetingSaveDialog.this.mDialog.getButton(-1);
            GreetingSaveDialog.this.mPositiveButton.setEnabled(GreetingSaveDialog.this.hasValidGreetingName());
            GreetingSaveDialog.this.mHandler.postDelayed(GreetingSaveDialog.this.mShowSoftInputRunnable, 100);
        }
    };
    private Runnable mShowSoftInputRunnable = new Runnable() {
        public void run() {
            GreetingSaveDialog.this.showSoftInput();
        }
    };
    private TextWatcher mWatcher = new TextWatcher() {
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (GreetingSaveDialog.this.mPositiveButton == null) {
                return;
            }
            if (s == null || s.toString().trim().length() == 0) {
                GreetingSaveDialog.this.mPositiveButton.setEnabled(false);
            } else {
                GreetingSaveDialog.this.mPositiveButton.setEnabled(true);
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void afterTextChanged(Editable s) {
        }
    };

    public static void show(FragmentManager fm, ContentValues greeting, String fileName) {
        GreetingSaveDialog dialog = new GreetingSaveDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelable("name", greeting);
        bundle.putString("_data", fileName);
        dialog.setArguments(bundle);
        dialog.show(fm, TAG);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = new Builder(getActivity());
        builder.setTitle(getString(R.string.action_new_greeting));
        View view = ((LayoutInflater) getActivity().getSystemService("layout_inflater")).inflate(R.layout.greeting_create_dialog, null);
        this.mEditText = (EditText) view.findViewById(R.id.greeting_name);
        this.mEditText.addTextChangedListener(this.mWatcher);
        builder.setView(view);
        builder.setNegativeButton(17039360, this.mListener);
        builder.setPositiveButton(R.string.description_save_button, this.mListener);
        this.mDialog = builder.create();
        this.mDialog.setOnShowListener(this.mShowListener);
        return this.mDialog;
    }

    private void showSoftInput() {
        Activity context = getActivity();
        if (context != null) {
            HwLog.d(TAG, "showSoftInput,result = " + ((InputMethodManager) context.getSystemService("input_method")).showSoftInput(this.mEditText, 1));
        }
    }

    private boolean hasValidGreetingName() {
        return (this.mEditText == null || TextUtils.isEmpty(this.mEditText.getText())) ? false : true;
    }
}
