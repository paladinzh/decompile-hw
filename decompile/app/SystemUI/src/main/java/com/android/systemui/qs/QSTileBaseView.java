package com.android.systemui.qs;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.LinearLayout;
import android.widget.Switch;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSTile.State;

public class QSTileBaseView extends LinearLayout {
    private String mAccessibilityClass;
    private boolean mCollapsedView;
    private final H mHandler = new H();
    private QSIconView mIcon;
    private int mLabelTint;
    private RippleDrawable mRipple;
    private Drawable mTileBackground;
    private boolean mTileState;

    private class H extends Handler {
        public H() {
            super(Looper.getMainLooper());
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                QSTileBaseView.this.handleStateChanged((State) msg.obj);
            }
        }
    }

    public QSTileBaseView(Context context, QSIconView icon, boolean collapsedView) {
        super(context);
        this.mIcon = icon;
        addView(this.mIcon);
        this.mTileBackground = newTileBackground();
        if (this.mTileBackground instanceof RippleDrawable) {
            setRipple((RippleDrawable) this.mTileBackground);
        }
        setImportantForAccessibility(1);
        setBackground(this.mTileBackground);
        setClipChildren(false);
        setClipToPadding(false);
        this.mCollapsedView = collapsedView;
    }

    private Drawable newTileBackground() {
        return this.mContext.getDrawable(R.drawable.ripple_drawable_dark);
    }

    private void setRipple(RippleDrawable tileBackground) {
        this.mRipple = tileBackground;
        if (getWidth() != 0) {
            updateRippleSize(getWidth(), getHeight());
        }
    }

    private void updateRippleSize(int width, int height) {
        int cx = width / 2;
        int cy = height / 2;
        int rad = (int) (((float) this.mIcon.getHeight()) * 0.85f);
        this.mRipple.setHotspotBounds(cx - rad, cy - rad, cx + rad, cy + rad);
    }

    public void init(OnClickListener click, OnLongClickListener longClick) {
        setClickable(true);
        setOnClickListener(click);
        setOnLongClickListener(longClick);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        if (this.mRipple != null) {
            updateRippleSize(w, h);
        }
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public View updateAccessibilityOrder(View previousView) {
        setAccessibilityTraversalAfter(previousView.getId());
        return this;
    }

    public void onStateChanged(State state) {
        this.mHandler.obtainMessage(1, state).sendToTarget();
    }

    protected void handleStateChanged(State state) {
        this.mIcon.setIcon(state);
        if (!this.mCollapsedView || TextUtils.isEmpty(state.minimalContentDescription)) {
            setContentDescription(state.contentDescription);
        } else {
            setContentDescription(state.minimalContentDescription);
        }
        if (this.mCollapsedView) {
            this.mAccessibilityClass = state.minimalAccessibilityClassName;
        } else {
            this.mAccessibilityClass = state.expandedAccessibilityClassName;
        }
        if (state instanceof BooleanState) {
            this.mTileState = ((BooleanState) state).value;
            this.mLabelTint = ((BooleanState) state).labelTint;
        }
    }

    public QSIconView getIcon() {
        return this.mIcon;
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        if (!TextUtils.isEmpty(this.mAccessibilityClass)) {
            event.setClassName(this.mAccessibilityClass);
            if (Switch.class.getName().equals(this.mAccessibilityClass)) {
                boolean z;
                event.setContentDescription(getResources().getString(!this.mTileState ? R.string.switch_bar_on : R.string.switch_bar_off));
                if (this.mTileState) {
                    z = false;
                } else {
                    z = true;
                }
                event.setChecked(z);
            }
        }
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        if (!TextUtils.isEmpty(this.mAccessibilityClass)) {
            info.setClassName(this.mAccessibilityClass);
            if (Switch.class.getName().equals(this.mAccessibilityClass)) {
                String label = getResources().getString(this.mTileState ? R.string.switch_bar_on : R.string.switch_bar_off);
                if (this.mLabelTint == 2) {
                    label = getResources().getString(R.string.switch_bar_off);
                }
                info.setText(label);
                info.setChecked(this.mTileState);
                info.setCheckable(true);
            }
        }
    }
}
