package com.android.gallery3d.ui;

import android.content.res.Resources;
import android.text.Layout.Alignment;
import android.text.TextPaint;
import android.text.TextUtils;
import com.android.gallery3d.R;
import com.android.gallery3d.anim.FloatAnimation;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MultiWindowStatusHolder;
import com.huawei.gallery.anim.AlphaAnimation;
import com.huawei.gallery.anim.CanvasAnimation;
import com.huawei.gallery.animation.CubicBezierInterpolator;
import com.huawei.gallery.util.LayoutHelper;
import com.huawei.watermark.manager.parse.WMElement;

public class MediaItemInfoView extends GLView {
    private boolean mAllowDate = false;
    private boolean mAllowLocation = false;
    private final int mBackgroundColor;
    private String mComment = "";
    private FloatAnimation mCommentAnimation = new FloatAnimation(0.0f, WMElement.CAMERASIZEVALUE1B1, 500);
    private final int mCommentTextColor;
    private final int mCommentTextSize;
    private MultiLineTexture mCommentTexture;
    private String mDate;
    private StringTexture mDateTexure;
    private CubicBezierInterpolator mInterpolator = new CubicBezierInterpolator();
    private boolean mIsPort;
    private int mLabelLimitSize;
    private double mLatitude = 0.0d;
    private boolean mLayoutRTL = GalleryUtils.isLayoutRTL();
    private String mLocation;
    private StringTexture mLocationTexure;
    private double mLongitude = 0.0d;
    private final int mMarginBottom;
    private final int mMarginH;
    private final int mMarginTop;
    private int mNavigationBarHeight;
    private int mPrivateFlag = 0;
    private CanvasAnimation mShowAnimation = null;
    private boolean mSizeChanged = true;
    private TextPaint mTextPaint;
    private TextPaint mTextPaintWithShadow;
    private boolean mVisible = false;

    public MediaItemInfoView(GalleryContext context) {
        Resources r = context.getResources();
        int labelColor = r.getColor(R.color.date_label_text_color);
        int labelSize = r.getDimensionPixelSize(R.dimen.location_label_text_size);
        this.mTextPaint = StringTexture.getDefaultPaint((float) labelSize, labelColor);
        this.mTextPaintWithShadow = StringTexture.getDefaultPaint((float) labelSize, labelColor);
        this.mTextPaintWithShadow.setShadowLayer(2.0f, 0.0f, 0.0f, -16777216);
        this.mBackgroundColor = r.getColor(R.color.comment_text_background);
        this.mMarginTop = r.getDimensionPixelSize(R.dimen.comment_margin_top);
        this.mMarginBottom = r.getDimensionPixelSize(R.dimen.comment_margin_bottom);
        this.mMarginH = r.getDimensionPixelSize(R.dimen.comment_margin_h);
        this.mCommentTextColor = r.getColor(R.color.comment_text_color);
        this.mCommentTextSize = r.getDimensionPixelSize(R.dimen.comment_text_size);
    }

    protected void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
        super.onLayout(changeSize, left, top, right, bottom);
        this.mIsPort = LayoutHelper.isPort();
        this.mLabelLimitSize = (right - left) / 2;
        this.mSizeChanged = changeSize;
    }

    protected void render(GLCanvas canvas) {
        super.render(canvas);
        if (this.mVisible) {
            int height;
            int width;
            int left;
            if (this.mPrivateFlag > 0 || this.mSizeChanged) {
                updateTexture();
            }
            BasicTexture commentTexture = this.mCommentTexture;
            BasicTexture dateTexture = this.mDateTexure;
            BasicTexture locaTexture = this.mLocationTexure;
            int lableHeight = 0;
            if (dateTexture != null) {
                lableHeight = dateTexture.getHeight();
            }
            if (locaTexture != null) {
                lableHeight = Math.max(lableHeight, locaTexture.getHeight());
            }
            int w = getWidth();
            int h = getHeight();
            int naviHeight = this.mNavigationBarHeight;
            if (!this.mIsPort) {
                w -= this.mNavigationBarHeight;
                naviHeight = getNaviHeightForLandscape();
            }
            int top = (h - naviHeight) - this.mMarginBottom;
            int left2 = this.mMarginH;
            canvas.save();
            if (commentTexture != null) {
                int textH = commentTexture.getHeight();
                int textW = commentTexture.getWidth();
                top -= textH;
                int totalHeight = (((this.mMarginTop + this.mMarginBottom) + textH) + naviHeight) + lableHeight;
                if (this.mCommentAnimation.calculate(AnimationTime.get())) {
                    canvas.translate(0.0f, (WMElement.CAMERASIZEVALUE1B1 - this.mCommentAnimation.get()) * ((float) totalHeight));
                    invalidate();
                }
                canvas.fillRect(0.0f, (float) (h - totalHeight), (float) w, (float) totalHeight, this.mBackgroundColor);
                canvas.drawTexture(commentTexture, left2, top, textW, textH);
            }
            if (dateTexture != null) {
                height = dateTexture.getHeight();
                width = dateTexture.getWidth();
                if (this.mLayoutRTL) {
                    canvas.drawTexture(dateTexture, (w - width) - left2, top - height, width, height);
                } else {
                    canvas.drawTexture(dateTexture, left2, top - height, width, height);
                }
                left = (this.mMarginH + left2) + width;
            } else {
                left = left2;
            }
            if (locaTexture != null) {
                height = locaTexture.getHeight();
                width = locaTexture.getWidth();
                if (this.mLayoutRTL) {
                    canvas.drawTexture(locaTexture, (w - width) - left, top - height, width, height);
                } else {
                    canvas.drawTexture(locaTexture, left, top - height, width, height);
                }
            }
            canvas.restore();
        }
    }

    private int getNaviHeightForLandscape() {
        if (!LayoutHelper.isDefaultLandOrientationProduct() || MultiWindowStatusHolder.isInMultiWindowMode() || this.mIsPort) {
            return 0;
        }
        return LayoutHelper.getNavigationBarHeightForDefaultLand();
    }

    public void recycle() {
        stopAnimations();
        recycleTexture(this.mCommentTexture);
        recycleTexture(this.mDateTexure);
        recycleTexture(this.mLocationTexure);
    }

    public void setLableVisible(boolean visible) {
        int i = 1;
        if (this.mVisible != visible) {
            int i2;
            this.mVisible = visible;
            invalidate();
            stopAnimations();
            this.mCommentAnimation.start();
            this.mCommentAnimation.setInterpolator(this.mInterpolator);
            if (visible) {
                i2 = 0;
            } else {
                i2 = 1;
            }
            float f = (float) i2;
            if (!visible) {
                i = 0;
            }
            this.mShowAnimation = new AlphaAnimation(f, (float) i);
            this.mShowAnimation.setDuration(500);
            startAnimation(this.mShowAnimation);
        }
    }

    private void stopAnimations() {
        if (this.mShowAnimation != null) {
            this.mShowAnimation.forceStop();
        }
        if (this.mCommentAnimation.isAnimating()) {
            this.mCommentAnimation.forceStop();
        }
    }

    public void onNavigationBarChange(int currHeight) {
        this.mNavigationBarHeight = currHeight;
        this.mPrivateFlag |= 4;
        invalidate();
    }

    public void setComment(String content) {
        if (content == null || !content.equals(this.mComment)) {
            if (TextUtils.isEmpty(content) != TextUtils.isEmpty(this.mComment)) {
                this.mPrivateFlag |= 3;
            }
            this.mComment = content;
            this.mPrivateFlag |= 4;
            invalidate();
        }
    }

    public void setLocation(String location) {
        if (location == null || !location.equals(this.mLocation)) {
            this.mLocation = location;
            this.mPrivateFlag |= 2;
            invalidate();
        }
    }

    public void setDate(String date) {
        if (date == null || !date.equals(this.mDate)) {
            this.mDate = date;
            this.mPrivateFlag |= 1;
            invalidate();
        }
    }

    public boolean isVisible() {
        return this.mVisible;
    }

    private void updateTexture() {
        int changeFlags = this.mPrivateFlag;
        boolean sizeChanged = this.mSizeChanged;
        this.mPrivateFlag = 0;
        this.mSizeChanged = false;
        if ((changeFlags & 1) != 0 || sizeChanged) {
            changeDateTexture();
        }
        if ((changeFlags & 2) != 0 || sizeChanged) {
            changeLocationTexture();
        }
        if ((changeFlags & 4) != 0 || sizeChanged) {
            changeCommentTexture();
        }
    }

    private void changeCommentTexture() {
        MultiLineTexture texture = this.mCommentTexture;
        String content = this.mComment;
        if (TextUtils.isEmpty(content)) {
            this.mCommentTexture = null;
        } else {
            int maxWidth = (getWidth() - (this.mMarginH * 2)) - (this.mIsPort ? 0 : this.mNavigationBarHeight);
            Alignment alignment = this.mLayoutRTL ? Alignment.ALIGN_OPPOSITE : Alignment.ALIGN_NORMAL;
            if (content.length() > 140) {
                content = content.substring(0, 140) + "...";
            }
            this.mCommentTexture = MultiLineTexture.newInstance(content, maxWidth, (float) this.mCommentTextSize, this.mCommentTextColor, alignment);
        }
        recycleTexture(texture);
    }

    private void changeDateTexture() {
        StringTexture texture = this.mDateTexure;
        this.mDateTexure = generateStringTexture(this.mDate, this.mAllowDate);
        recycleTexture(texture);
    }

    private void changeLocationTexture() {
        StringTexture texture = this.mLocationTexure;
        this.mLocationTexure = generateStringTexture(this.mLocation, this.mAllowLocation);
        recycleTexture(texture);
    }

    private StringTexture generateStringTexture(String content, boolean allow) {
        if (TextUtils.isEmpty(content) || !allow) {
            return null;
        }
        return StringTexture.newInstance(content, (float) this.mLabelLimitSize, TextUtils.isEmpty(this.mComment) ? this.mTextPaintWithShadow : this.mTextPaint);
    }

    private void recycleTexture(BasicTexture texture) {
        if (texture != null) {
            texture.recycle();
        }
    }

    public void setDateSwitch(boolean enable) {
        this.mAllowDate = enable;
    }

    public void setLocationSwitch(boolean enable) {
        this.mAllowLocation = enable;
    }

    public void setLocation(double latitude, double longitude) {
        this.mLatitude = latitude;
        this.mLongitude = longitude;
    }

    public boolean needResolveAddress(double latitude, double longitude) {
        return (Double.compare(this.mLatitude, latitude) == 0 && Double.compare(this.mLongitude, longitude) == 0) ? TextUtils.isEmpty(this.mLocation) : true;
    }
}
