package com.huawei.keyguard.view.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.EditText;
import com.huawei.keyguard.view.effect.HintErrorEffect;

public class HintErrorEffectEditText extends EditText {
    private HintErrorEffect mHintErrorEffect = new HintErrorEffect(this);

    public HintErrorEffectEditText(Context context) {
        super(context);
    }

    public HintErrorEffectEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HintErrorEffectEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void showErrorEffect() {
        this.mHintErrorEffect.showErrEffect(true);
        invalidate();
    }

    public void hideErrorEffect() {
        this.mHintErrorEffect.showErrEffect(false);
        invalidate();
    }

    public boolean isShownErrEffect() {
        return this.mHintErrorEffect.isShownErrEffect();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mHintErrorEffect.drawErrorEffect(canvas);
    }
}
