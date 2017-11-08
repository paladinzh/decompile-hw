package cn.com.xy.sms.sdk.ui.popu.util;

import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;

/* compiled from: Unknown */
public final class a extends CharacterStyle implements UpdateAppearance {
    private static int a = 0;

    public static void a(int i) {
        a = i;
    }

    public final void updateDrawState(TextPaint textPaint) {
        textPaint.bgColor = a;
    }
}
