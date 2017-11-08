package com.huawei.mms.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.style.ReplacementSpan;
import android.util.DisplayMetrics;

public class ChipsTextSpan extends ReplacementSpan {
    private IChipsConfiguration mAdapter;
    private Context mContext;
    private String mSource;
    private int mStyle;
    private CharSequence mText;
    private int mVerticalAlignment;
    private int mWidth;

    public interface IChipsConfiguration {
        float getAvailableWidth();

        Drawable getBackgroundDrawable();

        int getChipsHeight();

        int getChipsPadding();

        int getClearAndTextPadidng();

        Drawable getClearDrawable();

        int getFontColor();

        float getFontSize();

        int getIconWidth();

        int getMargin();

        TextPaint getTextPaint();

        int getXOffset(int i);

        int getYAdjust();

        int getYOffset();

        boolean isCurrentSelectedState(int i);

        boolean isRtlLayout();
    }

    public ChipsTextSpan(Context context, CharSequence text, int chipType, IChipsConfiguration cfg) {
        this(context, text, chipType, 0, cfg);
    }

    public void updateStyle(int style) {
        this.mStyle = style;
    }

    public int getStyle() {
        return this.mStyle;
    }

    public ChipsTextSpan(Context context, CharSequence text, int chipType, int verticalAlignment, IChipsConfiguration cfg) {
        this.mVerticalAlignment = verticalAlignment;
        this.mContext = context;
        this.mSource = text.toString();
        this.mText = ellipsizeText(text, cfg);
        this.mAdapter = cfg;
        this.mStyle = chipType;
    }

    private static CharSequence ellipsizeText(CharSequence oText, IChipsConfiguration adapter) {
        return TextUtils.ellipsize(oText, adapter.getTextPaint(), (adapter.getAvailableWidth() - ((float) adapter.getChipsHeight())) - ((float) adapter.getMargin()), TruncateAt.END);
    }

    public void measuereWidth() {
        int chipsLen;
        int iconWidth = this.mAdapter.getIconWidth();
        DisplayMetrics dm = this.mContext.getResources().getDisplayMetrics();
        int padding = this.mAdapter.getChipsPadding();
        TextPaint paint = this.mAdapter.getTextPaint();
        paint.setTextSize(this.mAdapter.getFontSize());
        int textLen = (int) Math.floor((double) paint.measureText(this.mText, 0, this.mText.length()));
        if (this.mAdapter.isCurrentSelectedState(this.mStyle)) {
            chipsLen = (((padding * 2) + textLen) + iconWidth) + this.mAdapter.getClearAndTextPadidng();
        } else {
            chipsLen = textLen + (padding * 2);
        }
        this.mWidth = Math.max(((int) (dm.density * 10.0f)) + iconWidth, chipsLen);
    }

    public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm) {
        int height = this.mAdapter.getChipsHeight();
        if (fm != null) {
            fm.ascent = -height;
            fm.descent = 0;
            fm.top = fm.ascent;
            fm.bottom = 0;
        }
        return this.mWidth;
    }

    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        int xOff = this.mAdapter.getXOffset(this.mStyle);
        int yAdjust = this.mAdapter.getYAdjust();
        int height = this.mAdapter.getChipsHeight();
        int iconWidth = this.mAdapter.getIconWidth();
        int chipsPadding = this.mAdapter.getChipsPadding();
        int yOff = this.mAdapter.getYOffset();
        Drawable background = this.mAdapter.getBackgroundDrawable();
        background.setBounds(0, 0, this.mWidth, height);
        Drawable drawable = null;
        if (this.mAdapter.isCurrentSelectedState(this.mStyle)) {
            drawable = this.mAdapter.getClearDrawable();
            if (this.mAdapter.isRtlLayout()) {
                drawable.setBounds(chipsPadding, (height - iconWidth) / 2, chipsPadding + iconWidth, (height + iconWidth) / 2);
            } else {
                drawable.setBounds((this.mWidth - chipsPadding) - iconWidth, (height - iconWidth) / 2, this.mWidth - chipsPadding, (height + iconWidth) / 2);
            }
        }
        canvas.save();
        canvas.translate(x, (float) ((y - height) + yAdjust));
        background.draw(canvas);
        if (drawable != null) {
            drawable.draw(canvas);
        }
        paint.setTextSize(this.mAdapter.getFontSize());
        paint.setColor(this.mAdapter.getFontColor());
        canvas.drawText(this.mText, 0, this.mText.length(), (float) xOff, (float) yOff, paint);
        canvas.restore();
    }
}
