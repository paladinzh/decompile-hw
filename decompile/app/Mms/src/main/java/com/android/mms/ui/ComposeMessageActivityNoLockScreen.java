package com.android.mms.ui;

import android.os.Bundle;

public class ComposeMessageActivityNoLockScreen extends ComposeMessageActivity {
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(4718592);
        super.onCreate(savedInstanceState);
    }
}
