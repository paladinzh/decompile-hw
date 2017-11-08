package com.huawei.systemmanager.applock.password.callback;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

public class PasswordInputClickListener implements OnClickListener {
    private PasswordPadInputCallback mCallback;
    private int mNum;

    public PasswordInputClickListener(PasswordPadInputCallback callback, int num) {
        this.mCallback = callback;
        this.mNum = num;
    }

    public void onClick(View view) {
        if (view instanceof Button) {
            this.mCallback.onPasswordInput(this.mNum);
        } else if (view instanceof ImageButton) {
            this.mCallback.onPasswordBackspace();
        }
    }
}
