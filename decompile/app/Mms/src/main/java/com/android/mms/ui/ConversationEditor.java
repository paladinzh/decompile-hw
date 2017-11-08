package com.android.mms.ui;

import android.os.Bundle;

public class ConversationEditor extends ConversationList {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActionBar() != null) {
            getActionBar().setBackgroundDrawable(null);
        }
    }
}
