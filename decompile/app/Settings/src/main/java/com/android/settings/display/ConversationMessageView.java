package com.android.settings.display;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settings.R$styleable;

public class ConversationMessageView extends FrameLayout {
    private ImageView mContactIconView;
    private final Drawable mIconView;
    private final boolean mIncoming;
    private LinearLayout mMessageBubble;
    private final CharSequence mMessageText;
    private ViewGroup mMessageTextAndInfoView;
    private TextView mMessageTextView;

    public ConversationMessageView(Context context) {
        this(context, null);
    }

    public ConversationMessageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConversationMessageView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ConversationMessageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.ConversationMessageView);
        this.mIncoming = a.getBoolean(0, true);
        this.mMessageText = a.getString(1);
        this.mIconView = a.getDrawable(3);
        LayoutInflater.from(context).inflate(2130968692, this);
        LayoutInflater.from(context).inflate(2130968691, this);
    }

    protected void onFinishInflate() {
        this.mMessageBubble = (LinearLayout) findViewById(2131886410);
        this.mMessageTextAndInfoView = (ViewGroup) findViewById(2131886411);
        this.mMessageTextView = (TextView) findViewById(2131886412);
        this.mContactIconView = (ImageView) findViewById(2131886413);
        updateViewContent();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        updateViewAppearance();
        int horizontalSpace = MeasureSpec.getSize(widthMeasureSpec);
        int unspecifiedMeasureSpec = MeasureSpec.makeMeasureSpec(0, 0);
        int iconMeasureSpec = MeasureSpec.makeMeasureSpec(0, 0);
        this.mContactIconView.measure(iconMeasureSpec, iconMeasureSpec);
        iconMeasureSpec = MeasureSpec.makeMeasureSpec(Math.max(this.mContactIconView.getMeasuredWidth(), this.mContactIconView.getMeasuredHeight()), 1073741824);
        this.mContactIconView.measure(iconMeasureSpec, iconMeasureSpec);
        this.mMessageBubble.measure(MeasureSpec.makeMeasureSpec((((horizontalSpace - (this.mContactIconView.getMeasuredWidth() * 2)) - getResources().getDimensionPixelSize(2131558734)) - getPaddingLeft()) - getPaddingRight(), Integer.MIN_VALUE), unspecifiedMeasureSpec);
        setMeasuredDimension(horizontalSpace, (getPaddingBottom() + Math.max(this.mContactIconView.getMeasuredHeight(), this.mMessageBubble.getMeasuredHeight())) + getPaddingTop());
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int iconLeft;
        int contentLeft;
        boolean isRtl = isLayoutRtl(this);
        int iconWidth = this.mContactIconView.getMeasuredWidth();
        int iconHeight = this.mContactIconView.getMeasuredHeight();
        int iconTop = getPaddingTop();
        int contentWidth = (((right - left) - iconWidth) - getPaddingLeft()) - getPaddingRight();
        int contentHeight = this.mMessageBubble.getMeasuredHeight();
        int contentTop = iconTop;
        if (this.mIncoming) {
            if (isRtl) {
                iconLeft = ((right - left) - getPaddingRight()) - iconWidth;
                contentLeft = iconLeft - contentWidth;
            } else {
                iconLeft = getPaddingLeft();
                contentLeft = (iconLeft + iconWidth) + 18;
            }
        } else if (isRtl) {
            iconLeft = getPaddingLeft();
            contentLeft = iconLeft + iconWidth;
        } else {
            iconLeft = ((right - left) - getPaddingRight()) - iconWidth;
            contentLeft = (iconLeft - contentWidth) - 18;
        }
        this.mContactIconView.layout(iconLeft, iconTop, iconLeft + iconWidth, iconTop + iconHeight);
        this.mMessageBubble.layout(contentLeft, iconTop, contentLeft + contentWidth, iconTop + contentHeight);
    }

    private static boolean isLayoutRtl(View view) {
        return 1 == view.getLayoutDirection();
    }

    private void updateViewContent() {
        this.mMessageTextView.setText(this.mMessageText);
        this.mContactIconView.setImageDrawable(this.mIconView);
    }

    private void updateViewAppearance() {
        int textLeftPadding;
        int textRightPadding;
        int gravity;
        int bubbleDrawableResId;
        int bubbleColorResId;
        Resources res = getResources();
        int arrowWidth = res.getDimensionPixelOffset(2131558734);
        int messageTextLeftRightPadding = res.getDimensionPixelOffset(2131558736);
        int textTopPadding = res.getDimensionPixelOffset(2131558737);
        int textBottomPadding = res.getDimensionPixelOffset(2131558738);
        if (this.mIncoming) {
            textLeftPadding = messageTextLeftRightPadding + arrowWidth;
            textRightPadding = messageTextLeftRightPadding;
        } else {
            textLeftPadding = messageTextLeftRightPadding;
            textRightPadding = messageTextLeftRightPadding + arrowWidth;
        }
        if (this.mIncoming) {
            gravity = 8388627;
        } else {
            gravity = 8388629;
        }
        int messageTopPadding = res.getDimensionPixelSize(2131558735);
        int metadataTopPadding = res.getDimensionPixelOffset(2131558739);
        if (this.mIncoming) {
            bubbleDrawableResId = 2130838547;
        } else {
            bubbleDrawableResId = 2130838548;
        }
        if (this.mIncoming) {
            bubbleColorResId = 2131427485;
        } else {
            bubbleColorResId = 2131427486;
        }
        Context context = getContext();
        this.mMessageTextAndInfoView.setBackground(getTintedDrawable(context, context.getDrawable(bubbleDrawableResId), context.getColor(bubbleColorResId)));
        if (isLayoutRtl(this)) {
            this.mMessageTextAndInfoView.setPadding(textRightPadding, textTopPadding + metadataTopPadding, textLeftPadding, textBottomPadding);
        } else {
            this.mMessageTextAndInfoView.setPadding(textLeftPadding, textTopPadding + metadataTopPadding, textRightPadding, textBottomPadding);
        }
        setPadding(getPaddingLeft(), messageTopPadding, getPaddingRight(), 0);
        this.mMessageBubble.setGravity(gravity);
        updateTextAppearance();
    }

    private void updateTextAppearance() {
        int messageColorResId;
        if (this.mIncoming) {
            messageColorResId = 2131427481;
        } else {
            messageColorResId = 2131427482;
        }
        int messageColor = getContext().getColor(messageColorResId);
        this.mMessageTextView.setTextColor(messageColor);
        this.mMessageTextView.setLinkTextColor(messageColor);
    }

    private static Drawable getTintedDrawable(Context context, Drawable drawable, int color) {
        Drawable retDrawable;
        ConstantState constantStateDrawable = drawable.getConstantState();
        if (constantStateDrawable != null) {
            retDrawable = constantStateDrawable.newDrawable(context.getResources()).mutate();
        } else {
            retDrawable = drawable;
        }
        retDrawable.setColorFilter(color, Mode.SRC_ATOP);
        return retDrawable;
    }
}
