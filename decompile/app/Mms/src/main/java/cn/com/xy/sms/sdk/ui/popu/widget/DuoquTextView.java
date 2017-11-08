package cn.com.xy.sms.sdk.ui.popu.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

public class DuoquTextView extends TextView {
    public DuoquTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DuoquTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DuoquTextView(Context context) {
        super(context);
    }

    protected void onDraw(Canvas canvas) {
        Drawable[] drawables = getCompoundDrawables();
        if (drawables != null) {
            Drawable drawableLeft = drawables[0];
            if (drawableLeft != null) {
                canvas.translate((((float) getWidth()) - ((((float) drawableLeft.getIntrinsicWidth()) + getPaint().measureText(getText().toString())) + ((float) getCompoundDrawablePadding()))) / 2.0f, 0.0f);
            }
        }
        super.onDraw(canvas);
    }
}
