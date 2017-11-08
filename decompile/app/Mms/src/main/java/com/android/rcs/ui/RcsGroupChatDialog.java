package com.android.rcs.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.EditText;

public class RcsGroupChatDialog extends AlertDialog {
    private EditText tvTopic;

    protected RcsGroupChatDialog(Context context, EditText tvTopic) {
        super(context);
        this.tvTopic = tvTopic;
    }

    public EditText getTvTopic() {
        return this.tvTopic;
    }
}
