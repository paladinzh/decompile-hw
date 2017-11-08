package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v17.leanback.R$color;
import android.util.AttributeSet;
import android.view.View;

class MediaRowFocusView extends View {
    private final Paint mPaint;
    private final RectF mRoundRectF = new RectF();
    private int mRoundRectRadius;

    public MediaRowFocusView(Context context) {
        super(context);
        this.mPaint = createPaint(context);
    }

    public MediaRowFocusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mPaint = createPaint(context);
    }

    public MediaRowFocusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mPaint = createPaint(context);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mRoundRectRadius = getHeight() / 2;
        int drawOffset = ((this.mRoundRectRadius * 2) - getHeight()) / 2;
        this.mRoundRectF.set(0.0f, (float) (-drawOffset), (float) getWidth(), (float) (getHeight() + drawOffset));
        canvas.drawRoundRect(this.mRoundRectF, (float) this.mRoundRectRadius, (float) this.mRoundRectRadius, this.mPaint);
    }

    private Paint createPaint(Context context) {
        Paint paint = new Paint();
        paint.setColor(context.getResources().getColor(R$color.lb_playback_media_row_highlight_color));
        return paint;
    }
}
