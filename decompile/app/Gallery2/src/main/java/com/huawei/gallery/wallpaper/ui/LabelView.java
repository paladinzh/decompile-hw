package com.huawei.gallery.wallpaper.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.editor.category.IconView;
import com.huawei.gallery.util.ColorfulUtils;
import com.huawei.gallery.util.ResourceUtils;
import com.huawei.watermark.manager.parse.WMElement;

public class LabelView extends IconView {
    private static final int LABEL_SIZE = GalleryUtils.dpToPixel(12);
    private static final int TEXT_MARGION = GalleryUtils.dpToPixel(4);
    private Drawable mDrawable;
    private Paint mLabelPaint;
    private Paint mMaskPaint;
    private boolean mPressed = false;
    protected int mSelectedDrawable = R.drawable.edit_frame_selected;
    private TextPaint mTextPaint;
    protected int mUnSelectedDrawable = R.drawable.btn_check_off_pressed_emui_black;
    private boolean mUseFrame = true;

    public LabelView(Context context) {
        super(context);
        init(context);
    }

    public LabelView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    private void init(Context context) {
        this.mNeedMask = false;
        this.mLabelPaint = new Paint();
        this.mLabelPaint.setStyle(Style.FILL);
        this.mLabelPaint.setTextSize((float) LABEL_SIZE);
        this.mLabelPaint.setColor(context.getColor(R.color.label_view_label_color));
        this.mLabelPaint.setStrokeWidth(WMElement.CAMERASIZEVALUE1B1);
        this.mMaskPaint = new Paint();
        this.mMaskPaint.setStyle(Style.FILL);
        this.mMaskPaint.setColor(context.getColor(R.color.label_view_mask_pressed));
        this.mTextPaint = new TextPaint();
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean z = true;
        int action = event.getAction();
        if (!(action == 0 || action == 2)) {
            z = false;
        }
        this.mPressed = z;
        invalidate();
        return super.onTouchEvent(event);
    }

    public void setUseFrame(boolean use) {
        this.mUseFrame = use;
    }

    public void setDrawable(Drawable drawable) {
        this.mDrawable = drawable;
    }

    public void onDraw(Canvas canvas) {
        drawImage(canvas);
        drawOverlay(canvas);
        drawLabel(canvas);
    }

    private void drawFrame(Canvas canvas) {
        if (this.mUseFrame) {
            NinePatchDrawable frame;
            if (isSelected()) {
                frame = (NinePatchDrawable) ColorfulUtils.mappingColorfulDrawableForce(getContext(), this.mSelectedDrawable);
            } else {
                frame = (NinePatchDrawable) ResourceUtils.getDrawable(getResources(), Integer.valueOf(this.mUnSelectedDrawable));
            }
            frame.setBounds(getMargin(), getMargin(), getWidth() - getMargin(), getWidth() - getMargin());
            frame.draw(canvas);
        }
    }

    protected void drawImage(Canvas canvas) {
        Drawable drawable = this.mDrawable;
        int margin = getMargin();
        if (drawable == null) {
            super.onDraw(canvas);
        } else {
            drawable.setBounds(margin, margin, getWidth() - margin, getWidth() - margin);
            drawable.draw(canvas);
        }
        if (this.mPressed) {
            canvas.drawRect((float) margin, (float) margin, (float) (getWidth() - margin), (float) (getWidth() - margin), this.mMaskPaint);
        }
    }

    protected void drawOverlay(Canvas canvas) {
        drawFrame(canvas);
    }

    protected void drawLabel(Canvas canvas) {
        String text = getText();
        if (!TextUtils.isEmpty(text)) {
            float textWidth = this.mLabelPaint.measureText(text);
            int margin = getMargin();
            int x = (int) ((((float) canvas.getWidth()) - textWidth) / 2.0f);
            if (x < TEXT_MARGION + margin) {
                x = TEXT_MARGION + margin;
            }
            int y = (canvas.getHeight() / 2) + margin;
            FontMetricsInt metrics = this.mLabelPaint.getFontMetricsInt();
            this.mLabelPaint.setAntiAlias(true);
            this.mTextPaint.reset();
            this.mTextPaint.set(this.mLabelPaint);
            canvas.translate(0.0f, (float) ((-(metrics.ascent + metrics.descent)) / 2));
            text = TextUtils.ellipsize(text, this.mTextPaint, (float) ((canvas.getWidth() - (margin * 2)) - TEXT_MARGION), TruncateAt.END).toString();
            this.mLabelPaint.setAlpha(Math.round(255.0f * (this.mPressed ? 0.5f : WMElement.CAMERASIZEVALUE1B1)));
            canvas.drawText(text, (float) x, (float) y, this.mLabelPaint);
        }
    }
}
