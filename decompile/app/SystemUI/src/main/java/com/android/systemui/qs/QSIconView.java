package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile.State;
import com.android.systemui.utils.HwLog;
import java.util.Objects;

public class QSIconView extends ViewGroup {
    private boolean mAnimationEnabled = true;
    protected final View mIcon;
    protected final int mIconSizePx;
    protected final int mTilePaddingBelowIconPx;

    public QSIconView(Context context) {
        super(context);
        Resources res = context.getResources();
        this.mIconSizePx = res.getDimensionPixelSize(R.dimen.qs_tile_icon_size);
        this.mTilePaddingBelowIconPx = res.getDimensionPixelSize(R.dimen.qs_tile_padding_below_icon);
        this.mIcon = createIcon();
        addView(this.mIcon);
    }

    public void disableAnimation() {
        this.mAnimationEnabled = false;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        this.mIcon.measure(MeasureSpec.makeMeasureSpec(w, getIconMeasureMode()), exactly(this.mIconSizePx));
        setMeasuredDimension(w, this.mIcon.getMeasuredHeight() + (this.mTilePaddingBelowIconPx * 2));
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        layout(this.mIcon, (w - this.mIcon.getMeasuredWidth()) / 2, this.mTilePaddingBelowIconPx + 0);
    }

    public void setIcon(State state) {
        setIcon((ImageView) this.mIcon, state);
    }

    protected void setIcon(ImageView iv, State state) {
        Object obj = null;
        if (!Objects.equals(state.icon, iv.getTag(R.id.qs_icon_tag))) {
            Drawable d = state.icon != null ? (iv.isShown() && this.mAnimationEnabled) ? state.icon.getDrawable(this.mContext) : state.icon.getInvisibleDrawable(this.mContext) : null;
            int padding = state.icon != null ? state.icon.getPadding() : 0;
            if (d != null && state.autoMirrorDrawable) {
                d.setAutoMirrored(true);
            }
            if (d == null) {
                Object drawable;
                String str = "QSIconView";
                StringBuilder append = new StringBuilder().append("setIcon::drawable is null, icon=").append(state.icon).append(", getDrawable=");
                if (state.icon != null) {
                    drawable = state.icon.getDrawable(this.mContext);
                } else {
                    drawable = null;
                }
                StringBuilder append2 = append.append(drawable).append(", getInvisibleDrawable=");
                if (state.icon != null) {
                    obj = state.icon.getInvisibleDrawable(this.mContext);
                }
                HwLog.e(str, append2.append(obj).toString());
            }
            iv.setImageDrawable(d);
            iv.setTag(R.id.qs_icon_tag, state.icon);
            iv.setPadding(0, padding, 0, padding);
            if ((d instanceof Animatable) && iv.isShown()) {
                Animatable a = (Animatable) d;
                a.start();
                if (!iv.isShown()) {
                    a.stop();
                }
            }
        }
        if (state.disabledByPolicy) {
            iv.setColorFilter(getContext().getColor(R.color.qs_tile_disabled_color));
        } else {
            iv.clearColorFilter();
        }
    }

    protected int getIconMeasureMode() {
        return 1073741824;
    }

    protected View createIcon() {
        ImageView icon = new ImageView(this.mContext);
        icon.setId(16908294);
        icon.setScaleType(ScaleType.FIT_CENTER);
        return icon;
    }

    protected final int exactly(int size) {
        return MeasureSpec.makeMeasureSpec(size, 1073741824);
    }

    protected final void layout(View child, int left, int top) {
        child.layout(left, top, child.getMeasuredWidth() + left, child.getMeasuredHeight() + top);
    }
}
