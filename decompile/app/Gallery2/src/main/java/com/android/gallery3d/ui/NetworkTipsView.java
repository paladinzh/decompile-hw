package com.android.gallery3d.ui;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.text.Layout.Alignment;
import android.text.TextPaint;
import android.view.MotionEvent;
import com.android.gallery3d.R;
import com.android.gallery3d.settings.GallerySettings;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.util.MyPrinter;

public class NetworkTipsView extends GLView {
    private static MyPrinter LOG = new MyPrinter("NetworkTipsView");
    private String mAgree;
    private RectF mAgreeRect = new RectF();
    private StringTexture mAgreeTexture;
    private Boolean mAllowNetWork = null;
    private int mBackgroundColor;
    private int mBottomLineColor;
    private int mBottomLineHeight;
    private int mBtnHeight;
    private int mBtnMinWidth;
    private TextPaint mBtnPaint;
    private int mBtnSpacing;
    private final NinePatchTexture mButtonBgNormal;
    private final NinePatchTexture mButtonBgPressed;
    private int mContentWidth;
    private Context mContext;
    private int mDefaultMargin;
    private String mDisagree;
    private RectF mDisagreeRect = new RectF();
    private StringTexture mDisagreeTexture;
    private boolean mEnable = false;
    private boolean mLayoutRTL;
    private String mMessage;
    private MultiLineTexture mMessageTexture;
    private int mMsgTextColor;
    private int mMsgTextSize;
    private Point mPressedPoint = new Point();
    private String mTitle;
    private TextPaint mTitlePaint;
    private StringTexture mTitleTexture;
    private int mTitleToMessage;

    public NetworkTipsView(Context context) {
        this.mContext = context;
        Resources r = context.getResources();
        this.mTitle = this.mContext.getString(R.string.use_network_view_title);
        this.mMessage = this.mContext.getString(R.string.use_network_view_message);
        this.mAgree = this.mContext.getString(R.string.use_network_agree);
        this.mDisagree = this.mContext.getString(R.string.use_network_disagree);
        this.mTitlePaint = new TextPaint();
        this.mTitlePaint.setColor(r.getColor(R.color.top_message_text_color));
        this.mTitlePaint.setTextSize((float) r.getDimensionPixelSize(R.dimen.use_network_title_text_size));
        this.mTitlePaint.setAntiAlias(true);
        this.mMsgTextColor = r.getColor(R.color.photoshare_login_desc_color);
        this.mMsgTextSize = r.getDimensionPixelSize(R.dimen.use_network_message_text_size);
        this.mBtnPaint = new TextPaint();
        this.mBtnPaint.setColor(r.getColor(R.color.time_line_group_title_text_color));
        this.mBtnPaint.setTextSize((float) r.getDimensionPixelSize(R.dimen.use_network_btn_text_size));
        this.mBtnPaint.setAntiAlias(true);
        this.mLayoutRTL = GalleryUtils.isLayoutRTL();
        this.mBackgroundColor = r.getColor(R.color.album_background);
        this.mBottomLineColor = r.getColor(R.color.use_network_bottom_line_color);
        this.mDefaultMargin = r.getDimensionPixelSize(R.dimen.use_network_default_margin);
        this.mTitleToMessage = r.getDimensionPixelSize(R.dimen.use_network_title_to_message_margin);
        this.mBtnHeight = r.getDimensionPixelSize(R.dimen.use_network_btn_height);
        this.mBtnSpacing = r.getDimensionPixelSize(R.dimen.use_network_btn_spacing);
        this.mBtnMinWidth = r.getDimensionPixelSize(R.dimen.use_network_btn_min_width);
        this.mBottomLineHeight = r.getDimensionPixelSize(R.dimen.use_network_bottom_line_heght);
        this.mButtonBgNormal = new NinePatchTexture(context, R.drawable.btn_small_flat_normal);
        this.mButtonBgPressed = new NinePatchTexture(context, R.drawable.btn_small_flat_pressed);
    }

    protected void render(GLCanvas canvas) {
        if (this.mEnable) {
            super.render(canvas);
            int width = getWidth();
            if (this.mBackgroundColor != 0) {
                canvas.fillRect(0.0f, 0.0f, (float) width, (float) getHeight(), this.mBackgroundColor);
            }
            Texture texture = this.mTitleTexture;
            int top = this.mDefaultMargin;
            if (texture != null) {
                texture.draw(canvas, (width - texture.getWidth()) / 2, top);
                top += texture.getHeight();
            }
            top += this.mTitleToMessage;
            texture = this.mMessageTexture;
            if (texture != null) {
                texture.draw(canvas, this.mDefaultMargin, top);
                top += texture.getHeight();
            }
            top += this.mDefaultMargin;
            int btnLength = Math.max(this.mBtnMinWidth, (this.mBtnSpacing * 2) + Math.max(this.mDisagreeTexture.getWidth(), this.mAgreeTexture.getWidth()));
            RectF btnRect1 = new RectF();
            RectF btnRect2 = new RectF();
            int left = ((width - this.mDefaultMargin) / 2) - btnLength;
            btnRect1.set((float) left, (float) top, (float) (left + btnLength), (float) (this.mBtnHeight + top));
            if (this.mPressedPoint == null || !btnRect1.contains((float) this.mPressedPoint.x, (float) this.mPressedPoint.y)) {
                this.mButtonBgNormal.draw(canvas, left, top, btnLength, this.mBtnHeight);
            } else {
                this.mButtonBgPressed.draw(canvas, left, top, btnLength, this.mBtnHeight);
            }
            left = (left + btnLength) + this.mDefaultMargin;
            btnRect2.set((float) left, (float) top, (float) (left + btnLength), (float) (this.mBtnHeight + top));
            if (this.mPressedPoint == null || !btnRect2.contains((float) this.mPressedPoint.x, (float) this.mPressedPoint.y)) {
                this.mButtonBgNormal.draw(canvas, left, top, btnLength, this.mBtnHeight);
            } else {
                this.mButtonBgPressed.draw(canvas, left, top, btnLength, this.mBtnHeight);
            }
            int spacingTop = (this.mBtnHeight - this.mDisagreeTexture.getHeight()) / 2;
            top += spacingTop;
            if (this.mLayoutRTL) {
                this.mAgreeRect.set(btnRect1);
                this.mAgreeTexture.draw(canvas, ((int) btnRect1.left) + ((btnLength - this.mAgreeTexture.getWidth()) / 2), top);
                this.mDisagreeRect.set(btnRect2);
                this.mDisagreeTexture.draw(canvas, ((int) btnRect2.left) + ((btnLength - this.mDisagreeTexture.getWidth()) / 2), top);
            } else {
                this.mDisagreeRect.set(btnRect1);
                this.mDisagreeTexture.draw(canvas, ((int) btnRect1.left) + ((btnLength - this.mDisagreeTexture.getWidth()) / 2), top);
                this.mAgreeRect.set(btnRect2);
                this.mAgreeTexture.draw(canvas, ((int) btnRect2.left) + ((btnLength - this.mAgreeTexture.getWidth()) / 2), top);
            }
            canvas.fillRect(0.0f, (float) (((top - spacingTop) + this.mBtnHeight) + this.mDefaultMargin), (float) width, (float) this.mBottomLineHeight, this.mBottomLineColor);
        }
    }

    protected boolean onTouch(MotionEvent event) {
        if (event.getAction() == 0) {
            this.mPressedPoint = new Point((int) event.getX(), (int) event.getY());
        } else if (event.getAction() == 3 || event.getAction() == 1) {
            if (this.mPressedPoint != null) {
                if (this.mAllowNetWork != null) {
                    LOG.d("you pressed " + (this.mAllowNetWork.booleanValue() ? "agree." : "disagree"));
                    if (this.mAllowNetWork.booleanValue()) {
                        setNetworkPreference(true);
                    }
                }
                setTipsClicked(true);
                requestLayout();
                onDismiss();
            }
            this.mPressedPoint = null;
        }
        if (this.mAgreeRect.contains(event.getX(), event.getY())) {
            this.mAllowNetWork = Boolean.valueOf(true);
        } else if (this.mDisagreeRect.contains(event.getX(), event.getY())) {
            this.mAllowNetWork = Boolean.valueOf(false);
        } else {
            LOG.d("you pressed other place");
            this.mPressedPoint = null;
        }
        invalidate();
        return true;
    }

    public void measureSize(int width, int height) {
        updateTexture(width, height);
    }

    public void setEnable(boolean enable) {
        this.mEnable = enable;
        requestLayout();
    }

    public int getMeasuredHeight() {
        if (this.mEnable) {
            return super.getMeasuredHeight();
        }
        return 0;
    }

    protected void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
        if (this.mEnable) {
            super.onLayout(changeSize, left, top, right, bottom);
            if (changeSize) {
                updateTexture(right - left, bottom - top);
            }
        }
    }

    private void updateTexture(int width, int height) {
        if (width <= 0 || height <= 0 || !this.mEnable) {
            LOG.d(" width: " + width + ", height: " + height);
        } else if (this.mContentWidth != width) {
            this.mContentWidth = width;
            if (this.mDisagreeTexture == null) {
                this.mDisagreeTexture = StringTexture.newInstance(this.mDisagree, this.mBtnPaint);
            }
            if (this.mAgreeTexture == null) {
                this.mAgreeTexture = StringTexture.newInstance(this.mAgree, this.mBtnPaint);
            }
            if (this.mTitleTexture == null) {
                this.mTitleTexture = StringTexture.newInstance(this.mTitle, this.mTitlePaint);
            }
            MultiLineTexture oldMessage = this.mMessageTexture;
            this.mMessageTexture = MultiLineTexture.newInstance(this.mMessage, width - (this.mDefaultMargin * 2), (float) this.mMsgTextSize, this.mMsgTextColor, Alignment.ALIGN_NORMAL);
            recycleTexture(oldMessage);
            setMeasuredSize(width, (((((this.mDefaultMargin * 3) + this.mTitleTexture.getHeight()) + this.mTitleToMessage) + this.mMessageTexture.getHeight()) + this.mBtnHeight) + this.mBottomLineHeight);
        }
    }

    private void recycleTexture(CanvasTexture texture) {
        if (texture != null) {
            texture.recycle();
        }
    }

    private void setNetworkPreference(boolean allowNetWork) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        editor.putBoolean(GallerySettings.KEY_USE_NETWORK, allowNetWork);
        editor.apply();
        String str = "{NetworkSettingOnStart:%s,Notips:%s}";
        Object[] objArr = new Object[2];
        objArr[0] = allowNetWork ? "Allow" : "Cancel";
        objArr[1] = "True";
        ReportToBigData.report(8, String.format(str, objArr));
    }

    private void setTipsClicked(boolean checkBoxSelected) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        editor.putBoolean(GallerySettings.KEY_NETWORK_NO_TIPS, checkBoxSelected);
        editor.apply();
    }

    public void onDismiss() {
    }
}
