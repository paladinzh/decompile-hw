package com.huawei.rcs.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

public class AnimationDialog extends Dialog {
    public AnimationDialog(Context context, int theme) {
        super(context, theme);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(8, 8);
        setCanceledOnTouchOutside(false);
    }
}
