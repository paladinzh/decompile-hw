package com.huawei.hwid.ui.common;

import android.content.Context;
import android.content.Intent;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.ui.common.login.PrivacyPolicyActivity;

/* compiled from: ClickSpan */
public class g extends ClickableSpan {
    private int a = -1;
    private Context b;
    private boolean c;

    public g(Context context, int i, boolean z) {
        this.b = context;
        this.a = i;
        this.c = z;
    }

    public g(Context context) {
        this.b = context;
    }

    public void onClick(View view) {
        Intent intent = new Intent(this.b, PrivacyPolicyActivity.class);
        intent.putExtra("isEmotionIntroduce", this.c);
        intent.putExtra("privacyType", String.valueOf(this.a));
        this.b.startActivity(intent);
    }

    public void updateDrawState(TextPaint textPaint) {
        super.updateDrawState(textPaint);
        textPaint.setColor(this.b.getResources().getColor(m.f(this.b, "CS_textview_jump_color")));
        textPaint.setUnderlineText(false);
    }
}
