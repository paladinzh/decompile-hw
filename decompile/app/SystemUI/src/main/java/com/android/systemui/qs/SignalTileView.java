package com.android.systemui.qs;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile.SignalState;
import com.android.systemui.qs.QSTile.State;

public final class SignalTileView extends QSIconView {
    private static final long DEFAULT_DURATION = new ValueAnimator().getDuration();
    private static final long SHORT_DURATION = (DEFAULT_DURATION / 3);
    private FrameLayout mIconFrame;
    private ImageView mIn = addTrafficView(R.drawable.ic_qs_signal_in);
    private ImageView mOut = addTrafficView(R.drawable.ic_qs_signal_out);
    private ImageView mOverlay;
    private ImageView mSignal;
    private int mWideOverlayIconStartPadding;

    public SignalTileView(Context context) {
        super(context);
        this.mWideOverlayIconStartPadding = context.getResources().getDimensionPixelSize(R.dimen.wide_type_icon_start_padding_qs);
    }

    private ImageView addTrafficView(int icon) {
        ImageView traffic = new ImageView(this.mContext);
        traffic.setImageResource(icon);
        traffic.setAlpha(0.0f);
        addView(traffic);
        return traffic;
    }

    protected View createIcon() {
        this.mIconFrame = new FrameLayout(this.mContext);
        this.mSignal = new ImageView(this.mContext);
        this.mIconFrame.addView(this.mSignal);
        this.mOverlay = new ImageView(this.mContext);
        this.mIconFrame.addView(this.mOverlay, -2, -2);
        return this.mIconFrame;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int hs = MeasureSpec.makeMeasureSpec(this.mIconFrame.getMeasuredHeight(), 1073741824);
        int ws = MeasureSpec.makeMeasureSpec(this.mIconFrame.getMeasuredHeight(), Integer.MIN_VALUE);
        this.mIn.measure(ws, hs);
        this.mOut.measure(ws, hs);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        layoutIndicator(this.mIn);
        layoutIndicator(this.mOut);
    }

    protected int getIconMeasureMode() {
        return Integer.MIN_VALUE;
    }

    private void layoutIndicator(View indicator) {
        int right;
        int left;
        boolean isRtl = true;
        if (getLayoutDirection() != 1) {
            isRtl = false;
        }
        if (isRtl) {
            right = this.mIconFrame.getLeft();
            left = right - indicator.getMeasuredWidth();
        } else {
            left = this.mIconFrame.getRight();
            right = left + indicator.getMeasuredWidth();
        }
        indicator.layout(left, this.mIconFrame.getBottom() - indicator.getMeasuredHeight(), right, this.mIconFrame.getBottom());
    }

    public void setIcon(State state) {
        SignalState s = (SignalState) state;
        setIcon(this.mSignal, s);
        if (s.overlayIconId > 0) {
            this.mOverlay.setVisibility(0);
            this.mOverlay.setImageResource(s.overlayIconId);
        } else {
            this.mOverlay.setVisibility(8);
        }
        if (s.overlayIconId <= 0 || !s.isOverlayIconWide) {
            this.mSignal.setPaddingRelative(0, 0, 0, 0);
        } else {
            this.mSignal.setPaddingRelative(this.mWideOverlayIconStartPadding, 0, 0, 0);
        }
        Drawable drawable = this.mSignal.getDrawable();
        if (state.autoMirrorDrawable && drawable != null) {
            drawable.setAutoMirrored(true);
        }
        boolean shown = isShown();
        setVisibility(this.mIn, shown, s.activityIn);
        setVisibility(this.mOut, shown, s.activityOut);
    }

    private void setVisibility(View view, boolean shown, boolean visible) {
        int i = (shown && visible) ? 1 : 0;
        float newAlpha = (float) i;
        if (view.getAlpha() != newAlpha) {
            if (shown) {
                view.animate().setDuration(visible ? SHORT_DURATION : DEFAULT_DURATION).alpha(newAlpha).start();
            } else {
                view.setAlpha(newAlpha);
            }
        }
    }
}
