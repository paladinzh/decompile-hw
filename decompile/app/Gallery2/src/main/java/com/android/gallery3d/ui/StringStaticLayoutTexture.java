package com.android.gallery3d.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.StaticLayout.Builder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.editor.tools.EditorUtils;
import com.huawei.watermark.manager.parse.WMElement;

public class StringStaticLayoutTexture extends CanvasTexture {
    private static final int SavePaddingX = GalleryUtils.dpToPixel(6);
    private static final Rect sTempRect = new Rect();
    private final TextPaint mPaint;
    private StaticLayout mStaticLayout;
    private final String mText;
    private final RectF mTextInitRect = new RectF();

    public StringStaticLayoutTexture(String text, TextPaint paint, int width, int height, int limitLines) {
        super(width, height);
        this.mPaint = paint;
        this.mTextInitRect.set(0.0f, 0.0f, (float) width, (float) height);
        this.mText = TextUtils.ellipsize(text, paint, ((float) (width * limitLines)) * 0.9f, TruncateAt.END).toString();
        this.mStaticLayout = EditorUtils.getStaticLayout(this.mText, this.mPaint, this.mTextInitRect, this.mPaint.getTextSize(), this.mPaint.getTextSize());
    }

    public StringStaticLayoutTexture(String text, TextPaint paint, Alignment alignment, int width, int height, int limitLines) {
        super(width, height);
        this.mPaint = paint;
        this.mTextInitRect.set(0.0f, 0.0f, (float) width, (float) height);
        if (text == null) {
            text = "";
        }
        this.mStaticLayout = Builder.obtain(text, 0, text.length(), this.mPaint, width).setAlignment(alignment).setMaxLines(limitLines).setEllipsize(TruncateAt.END).build();
        this.mText = text;
    }

    public static Rect dealWithLayout(String text, TextPaint paint, int width, int height, int limitLines) {
        int limitWidth = width - (SavePaddingX * 2);
        sTempRect.set(0, 0, Math.max(1, limitWidth), Math.min(Math.max(1, new StaticLayout(TextUtils.ellipsize(text, paint, ((float) (limitWidth * limitLines)) * 0.9f, TruncateAt.END).toString(), paint, limitWidth, Alignment.ALIGN_CENTER, WMElement.CAMERASIZEVALUE1B1, 0.0f, false).getHeight()), height));
        return sTempRect;
    }

    protected void onDraw(Canvas canvas, Bitmap backing) {
        StaticLayout staticLayout = this.mStaticLayout;
        if (staticLayout != null) {
            canvas.save();
            canvas.translate(this.mTextInitRect.left + ((this.mTextInitRect.width() - ((float) staticLayout.getWidth())) / 2.0f), this.mTextInitRect.top + ((this.mTextInitRect.height() - ((float) staticLayout.getHeight())) / 2.0f));
            staticLayout.draw(canvas);
            canvas.restore();
        }
    }
}
