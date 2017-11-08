package android.support.v17.leanback.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.R$attr;
import android.support.v17.leanback.R$id;
import android.support.v17.leanback.R$layout;
import android.support.v17.leanback.R$style;
import android.support.v17.leanback.R$styleable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class ImageCardView extends BaseCardView {
    private boolean mAttachedToWindow;
    private ImageView mBadgeImage;
    private TextView mContentView;
    private ImageView mImageView;
    private ViewGroup mInfoArea;
    private TextView mTitleView;

    public ImageCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        buildImageCardView(attrs, defStyleAttr, R$style.Widget_Leanback_ImageCardView);
    }

    private void buildImageCardView(AttributeSet attrs, int defStyleAttr, int defStyle) {
        setFocusable(true);
        setFocusableInTouchMode(true);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R$layout.lb_image_card_view, this);
        TypedArray cardAttrs = getContext().obtainStyledAttributes(attrs, R$styleable.lbImageCardView, defStyleAttr, defStyle);
        int cardType = cardAttrs.getInt(R$styleable.lbImageCardView_lbImageCardViewType, 0);
        boolean hasImageOnly = cardType == 0;
        boolean hasTitle = (cardType & 1) == 1;
        boolean hasContent = (cardType & 2) == 2;
        boolean hasIconRight = (cardType & 4) == 4;
        boolean hasIconLeft = !hasIconRight && (cardType & 8) == 8;
        this.mImageView = (ImageView) findViewById(R$id.main_image);
        if (this.mImageView.getDrawable() == null) {
            this.mImageView.setVisibility(4);
        }
        this.mInfoArea = (ViewGroup) findViewById(R$id.info_field);
        if (hasImageOnly) {
            removeView(this.mInfoArea);
            cardAttrs.recycle();
            return;
        }
        LayoutParams relativeLayoutParams;
        if (hasTitle) {
            this.mTitleView = (TextView) inflater.inflate(R$layout.lb_image_card_view_themed_title, this.mInfoArea, false);
            this.mInfoArea.addView(this.mTitleView);
        }
        if (hasContent) {
            this.mContentView = (TextView) inflater.inflate(R$layout.lb_image_card_view_themed_content, this.mInfoArea, false);
            this.mInfoArea.addView(this.mContentView);
        }
        if (hasIconRight || hasIconLeft) {
            int layoutId = R$layout.lb_image_card_view_themed_badge_right;
            if (hasIconLeft) {
                layoutId = R$layout.lb_image_card_view_themed_badge_left;
            }
            this.mBadgeImage = (ImageView) inflater.inflate(layoutId, this.mInfoArea, false);
            this.mInfoArea.addView(this.mBadgeImage);
        }
        if (!(!hasTitle || hasContent || this.mBadgeImage == null)) {
            relativeLayoutParams = (LayoutParams) this.mTitleView.getLayoutParams();
            if (hasIconLeft) {
                relativeLayoutParams.addRule(17, this.mBadgeImage.getId());
            } else {
                relativeLayoutParams.addRule(16, this.mBadgeImage.getId());
            }
            this.mTitleView.setLayoutParams(relativeLayoutParams);
        }
        if (hasContent) {
            relativeLayoutParams = (LayoutParams) this.mContentView.getLayoutParams();
            if (!hasTitle) {
                relativeLayoutParams.addRule(10);
            }
            if (hasIconLeft) {
                relativeLayoutParams.removeRule(16);
                relativeLayoutParams.removeRule(20);
                relativeLayoutParams.addRule(17, this.mBadgeImage.getId());
            }
            this.mContentView.setLayoutParams(relativeLayoutParams);
        }
        if (this.mBadgeImage != null) {
            relativeLayoutParams = (LayoutParams) this.mBadgeImage.getLayoutParams();
            if (hasContent) {
                relativeLayoutParams.addRule(8, this.mContentView.getId());
            } else if (hasTitle) {
                relativeLayoutParams.addRule(8, this.mTitleView.getId());
            }
            this.mBadgeImage.setLayoutParams(relativeLayoutParams);
        }
        Drawable background = cardAttrs.getDrawable(R$styleable.lbImageCardView_infoAreaBackground);
        if (background != null) {
            setInfoAreaBackground(background);
        }
        if (this.mBadgeImage != null && this.mBadgeImage.getDrawable() == null) {
            this.mBadgeImage.setVisibility(8);
        }
        cardAttrs.recycle();
    }

    public ImageCardView(Context context, AttributeSet attrs) {
        this(context, attrs, R$attr.imageCardViewStyle);
    }

    public void setInfoAreaBackground(Drawable drawable) {
        if (this.mInfoArea != null) {
            this.mInfoArea.setBackground(drawable);
        }
    }

    private void fadeIn() {
        this.mImageView.setAlpha(0.0f);
        if (this.mAttachedToWindow) {
            this.mImageView.animate().alpha(1.0f).setDuration((long) this.mImageView.getResources().getInteger(17694720));
        }
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mAttachedToWindow = true;
        if (this.mImageView.getAlpha() == 0.0f) {
            fadeIn();
        }
    }

    protected void onDetachedFromWindow() {
        this.mAttachedToWindow = false;
        this.mImageView.animate().cancel();
        this.mImageView.setAlpha(1.0f);
        super.onDetachedFromWindow();
    }
}
