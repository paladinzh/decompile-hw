package com.android.settings.smartcover;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.Global;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import java.lang.Character.UnicodeBlock;

@SuppressLint({"NewApi"})
public final class StandByWordDialogFragment extends DialogFragment implements TextWatcher {
    private AlertDialog mAlertDialog;
    private Button mOkButton;
    private boolean mWordEdited;
    private boolean mWordUpdated;
    EditText mWordView;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String standByWord = Global.getString(getActivity().getContentResolver(), "cover_standby_word");
        if (savedInstanceState != null) {
            standByWord = savedInstanceState.getString("standBy_workd", standByWord);
            this.mWordEdited = savedInstanceState.getBoolean("word_edited", false);
        }
        this.mAlertDialog = new Builder(getActivity()).setIcon(17301659).setTitle(2131629292).setView(createDialogView(standByWord)).setPositiveButton(2131629208, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                StandByWordDialogFragment.this.setStandByWord(StandByWordDialogFragment.this.mWordView.getText().toString());
            }
        }).setNegativeButton(17039360, null).create();
        this.mAlertDialog.getWindow().setSoftInputMode(5);
        return this.mAlertDialog;
    }

    private void setStandByWord(String standByWord) {
        Log.d("StandByWordDialogFragment", "Setting device name to " + standByWord);
        Intent intent = new Intent("action_animation_mode_text_changed");
        intent.putExtra("animation_mode_text", standByWord);
        getActivity().sendBroadcast(intent);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mWordView != null) {
            outState.putString("standBy_workd", this.mWordView.getText().toString());
        }
        outState.putBoolean("word_edited", this.mWordEdited);
    }

    private View createDialogView(String standByWord) {
        View view = ((LayoutInflater) getActivity().getSystemService("layout_inflater")).inflate(2130968743, null);
        this.mWordView = (EditText) view.findViewById(2131886503);
        this.mWordView.setFilters(new InputFilter[]{new Utf8ByteLengthFilter(30)});
        this.mWordView.setText(standByWord);
        if (standByWord != null) {
            this.mWordView.setSelection(standByWord.length());
        }
        this.mWordView.addTextChangedListener(this);
        this.mWordView.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId != 6 || v.getText().length() <= 0) {
                    return false;
                }
                StandByWordDialogFragment.this.setStandByWord(v.getText().toString());
                StandByWordDialogFragment.this.mAlertDialog.dismiss();
                return true;
            }
        });
        return view;
    }

    private boolean isSpecialUnicodeBlock(char c) {
        UnicodeBlock ub = UnicodeBlock.of(c);
        if (ub == UnicodeBlock.HIGH_SURROGATES || ub == UnicodeBlock.LOW_SURROGATES) {
            return true;
        }
        return false;
    }

    public void onDestroy() {
        super.onDestroy();
        this.mAlertDialog = null;
        this.mWordView = null;
        this.mOkButton = null;
    }

    public void onResume() {
        boolean z = false;
        super.onResume();
        if (this.mOkButton == null && this.mAlertDialog != null) {
            this.mOkButton = this.mAlertDialog.getButton(-1);
            String standByWordViewStr = "";
            if (this.mWordView != null) {
                standByWordViewStr = this.mWordView.getText().toString();
            }
            Button button = this.mOkButton;
            if (this.mWordEdited && standByWordViewStr.length() != 0) {
                z = true;
            }
            button.setEnabled(z);
        }
    }

    public void onPause() {
        super.onPause();
    }

    public void afterTextChanged(Editable editableStr) {
        boolean z = true;
        if (this.mOkButton != null) {
            if (editableStr != null && editableStr.length() > 0) {
                for (int i = 0; i < editableStr.length(); i++) {
                    if (isSpecialUnicodeBlock(editableStr.charAt(i))) {
                        editableStr.delete(i, i + 1);
                    }
                }
            }
            if (this.mWordUpdated) {
                this.mWordUpdated = false;
                this.mOkButton.setEnabled(false);
            } else if (editableStr != null) {
                this.mWordEdited = true;
                Button button = this.mOkButton;
                if (editableStr.length() == 0) {
                    z = false;
                }
                button.setEnabled(z);
            }
        }
    }

    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }

    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }
}
