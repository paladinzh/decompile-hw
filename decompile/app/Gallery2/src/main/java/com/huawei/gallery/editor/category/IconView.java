package com.huawei.gallery.editor.category;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.NinePatchDrawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.View;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.util.ResourceUtils;
import com.huawei.watermark.manager.parse.WMElement;

public class IconView extends View {
    private static final int MASK_HEIGHT = GalleryUtils.dpToPixel(20);
    private static final int TEXT_MARGION = GalleryUtils.dpToPixel(4);
    protected Bitmap mBitmap;
    protected Rect mBitmapBounds;
    private int mMargin = 16;
    protected boolean mNeedMask = true;
    private int mOrientation = 1;
    private Paint mPaint = new Paint();
    private String mText;
    private Rect mTextBounds = new Rect();
    protected int mTextColor;
    private TextPaint mTextPaint = new TextPaint();
    private int mTextSize = 32;
    private boolean mUseOnlyDrawable = false;

    public IconView(Context context) {
        super(context);
        setup(context);
    }

    public IconView(Context context, AttributeSet attr) {
        super(context, attr);
        setup(context);
    }

    protected void setup(Context context) {
        Resources res = context.getResources();
        this.mTextColor = res.getColor(R.color.filtershow_categoryview_text);
        this.mMargin = res.getDimensionPixelOffset(R.dimen.category_panel_margin);
        this.mTextSize = res.getDimensionPixelSize(R.dimen.category_panel_text_size);
    }

    protected void computeTextPosition(String text) {
        if (text != null) {
            this.mPaint.setTextSize((float) this.mTextSize);
            if (getOrientation() == 0) {
                text = text.toUpperCase();
                this.mPaint.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                GalleryUtils.setTypeFaceAsSlim(this.mPaint);
            }
            this.mPaint.getTextBounds(text, 0, text.length(), this.mTextBounds);
        }
    }

    public boolean needsCenterText() {
        return false;
    }

    protected void drawText(Canvas canvas, String text) {
        if (text != null) {
            float textWidth = this.mPaint.measureText(text);
            int x = TEXT_MARGION + this.mMargin;
            if (needsCenterText()) {
                x = (int) ((((float) canvas.getWidth()) - textWidth) / 2.0f);
            }
            if (x < 0) {
                x = this.mMargin;
            }
            int y = (canvas.getHeight() - TEXT_MARGION) - (this.mMargin * 2);
            this.mTextPaint.reset();
            this.mTextPaint.set(this.mPaint);
            canvas.drawText(TextUtils.ellipsize(text, this.mTextPaint, (float) (canvas.getWidth() - (this.mMargin * 5)), TruncateAt.END).toString(), (float) x, (float) y, this.mPaint);
        }
    }

    protected void drawOutlinedText(Canvas canvas, String text) {
        this.mPaint.setColor(getTextColor());
        this.mPaint.setStyle(Style.FILL);
        this.mPaint.setStrokeWidth(WMElement.CAMERASIZEVALUE1B1);
        drawText(canvas, text);
    }

    public int getOrientation() {
        return this.mOrientation;
    }

    public void setOrientation(int orientation) {
        this.mOrientation = orientation;
    }

    public int getMargin() {
        return this.mMargin;
    }

    public int getTextColor() {
        return this.mTextColor;
    }

    public void setText(String text) {
        this.mText = text;
    }

    public String getText() {
        return this.mText;
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    public void setUseOnlyDrawable(boolean value) {
        this.mUseOnlyDrawable = value;
    }

    public void computeBitmapBounds() {
        this.mBitmapBounds = new Rect(this.mMargin, this.mMargin, getWidth() - this.mMargin, getWidth() - this.mMargin);
    }

    public void onDraw(Canvas canvas) {
        this.mPaint.reset();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setFilterBitmap(true);
        computeBitmapBounds();
        computeTextPosition(getText());
        if (!(this.mBitmap == null || this.mBitmap.isRecycled())) {
            canvas.save();
            canvas.clipRect(this.mBitmapBounds);
            Matrix m = new Matrix();
            if (this.mUseOnlyDrawable) {
                this.mPaint.setFilterBitmap(true);
                m.setRectToRect(new RectF(0.0f, 0.0f, (float) this.mBitmap.getWidth(), (float) this.mBitmap.getHeight()), new RectF(this.mBitmapBounds), ScaleToFit.CENTER);
            } else {
                float scale = Math.max(((float) this.mBitmapBounds.width()) / ((float) this.mBitmap.getWidth()), ((float) this.mBitmapBounds.height()) / ((float) this.mBitmap.getHeight()));
                float dx = ((((float) this.mBitmapBounds.width()) - (((float) this.mBitmap.getWidth()) * scale)) / 2.0f) + ((float) this.mBitmapBounds.left);
                float dy = ((((float) this.mBitmapBounds.height()) - (((float) this.mBitmap.getHeight()) * scale)) / 2.0f) + ((float) this.mBitmapBounds.top);
                m.postScale(scale, scale);
                m.postTranslate(dx, dy);
            }
            canvas.drawBitmap(this.mBitmap, m, this.mPaint);
            canvas.restore();
        }
        if (this.mNeedMask) {
            NinePatchDrawable frame = (NinePatchDrawable) ResourceUtils.getDrawable(getResources(), Integer.valueOf(R.drawable.filter_in_mask));
            frame.setBounds(getMargin(), (getWidth() - getMargin()) - MASK_HEIGHT, getWidth() - getMargin(), getWidth() - getMargin());
            frame.draw(canvas);
        }
    }
}
