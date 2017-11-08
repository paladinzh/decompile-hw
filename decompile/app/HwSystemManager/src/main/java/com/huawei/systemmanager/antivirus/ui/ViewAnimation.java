package com.huawei.systemmanager.antivirus.ui;

import android.content.Context;
import android.view.View;
import android.view.animation.TranslateAnimation;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.util.HwLog;

public class ViewAnimation {
    public static void upAndDownAnimation(View view) {
        if (view == null) {
            HwLog.e("ViewAnimation", "Null Point Exception");
            return;
        }
        float y = 0.0f;
        Context context = view.getContext();
        if (context != null) {
            y = (float) context.getResources().getDimensionPixelOffset(R.dimen.y_transtration);
        }
        TranslateAnimation animation = new TranslateAnimation(0.0f, 0.0f, 0.0f, -y);
        animation.setRepeatCount(1);
        animation.setRepeatMode(2);
        animation.setDuration(300);
        view.startAnimation(animation);
    }
}
