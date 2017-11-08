package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.android.keyguard.AlphaOptimizedLinearLayout;
import com.android.systemui.R;
import com.android.systemui.ViewInvertHelper;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.TransformableView;
import com.android.systemui.statusbar.ViewTransformationHelper;
import com.android.systemui.statusbar.ViewTransformationHelper.CustomTransformation;

public class HybridNotificationView extends AlphaOptimizedLinearLayout implements TransformableView {
    private ViewInvertHelper mInvertHelper;
    protected TextView mTextView;
    protected TextView mTitleView;
    private ViewTransformationHelper mTransformationHelper;

    public HybridNotificationView(Context context) {
        this(context, null);
    }

    public HybridNotificationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HybridNotificationView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HybridNotificationView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public TextView getTitleView() {
        return this.mTitleView;
    }

    public TextView getTextView() {
        return this.mTextView;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mTitleView = (TextView) findViewById(R.id.notification_title);
        this.mTextView = (TextView) findViewById(R.id.notification_text);
        this.mInvertHelper = new ViewInvertHelper((View) this, 700);
        this.mTransformationHelper = new ViewTransformationHelper();
        this.mTransformationHelper.setCustomTransformation(new CustomTransformation() {
            public boolean transformTo(TransformState ownState, TransformableView notification, float transformationAmount) {
                TransformState otherState = notification.getCurrentState(1);
                CrossFadeHelper.fadeOut(HybridNotificationView.this.mTextView, transformationAmount);
                if (otherState != null) {
                    ownState.transformViewVerticalTo(otherState, transformationAmount);
                    otherState.recycle();
                }
                return true;
            }

            public boolean transformFrom(TransformState ownState, TransformableView notification, float transformationAmount) {
                TransformState otherState = notification.getCurrentState(1);
                CrossFadeHelper.fadeIn(HybridNotificationView.this.mTextView, transformationAmount);
                if (otherState != null) {
                    ownState.transformViewVerticalFrom(otherState, transformationAmount);
                    otherState.recycle();
                }
                return true;
            }
        }, 2);
        this.mTransformationHelper.addTransformedView(1, this.mTitleView);
        this.mTransformationHelper.addTransformedView(2, this.mTextView);
    }

    public void bind(CharSequence title, CharSequence text) {
        int i;
        this.mTitleView.setText(title);
        TextView textView = this.mTitleView;
        if (TextUtils.isEmpty(title)) {
            i = 8;
        } else {
            i = 0;
        }
        textView.setVisibility(i);
        if (TextUtils.isEmpty(text)) {
            this.mTextView.setVisibility(8);
            this.mTextView.setText(null);
        } else {
            this.mTextView.setVisibility(0);
            this.mTextView.setText(text.toString());
        }
        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(text)) {
            this.mTitleView.setVisibility(4);
            this.mTextView.setVisibility(4);
        }
        requestLayout();
    }

    public void setDark(boolean dark, boolean fade, long delay) {
        this.mInvertHelper.setInverted(dark, fade, delay);
    }

    public TransformState getCurrentState(int fadingView) {
        return this.mTransformationHelper.getCurrentState(fadingView);
    }

    public void transformTo(TransformableView notification, Runnable endRunnable) {
        this.mTransformationHelper.transformTo(notification, endRunnable);
    }

    public void transformTo(TransformableView notification, float transformationAmount) {
        this.mTransformationHelper.transformTo(notification, transformationAmount);
    }

    public void transformFrom(TransformableView notification) {
        this.mTransformationHelper.transformFrom(notification);
    }

    public void transformFrom(TransformableView notification, float transformationAmount) {
        this.mTransformationHelper.transformFrom(notification, transformationAmount);
    }

    public void setVisible(boolean visible) {
        setVisibility(visible ? 0 : 4);
        this.mTransformationHelper.setVisible(visible);
    }
}
