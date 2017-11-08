package com.android.systemui.tint;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.RemotableViewMethod;
import android.view.View;
import android.widget.ImageView;
import java.util.Observable;
import java.util.Observer;

public class TintImageView extends ImageView implements Observer {
    public static final boolean DEBUG = Log.isLoggable("TintImageView", 3);
    protected boolean mIsResever = true;
    protected String mTintType = "statusBarType";

    public TintImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TintImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TintImageView(Context context) {
        super(context);
    }

    public TintImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void onAttachedToWindow() {
        if (DEBUG) {
            Log.d("TintImageView", "onAttachedToWindow:" + this);
        }
        TintManager.getInstance().addObserver(this);
        setTint();
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        if (DEBUG) {
            Log.d("TintImageView", "onDetachedFromWindow:" + this);
        }
        TintManager.getInstance().deleteObserver(this);
        super.onDetachedFromWindow();
    }

    public void update(Observable o, Object arg) {
        setTint();
    }

    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (drawable != null) {
            drawable.mutate();
            setTint();
        }
    }

    @RemotableViewMethod
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        if (getDrawable() != null) {
            getDrawable().mutate();
            setTint();
        }
    }

    public void setTintType(String tintType) {
        this.mTintType = tintType;
    }

    public void setTint() {
        if (getVisibility() == 0) {
            Drawable drawable = getDrawable();
            if (drawable != null) {
                setTint(drawable);
            }
        }
    }

    protected void setTint(Drawable drawable) {
        if (drawable != null) {
            if (this.mIsResever) {
                int color = TintManager.getInstance().getIconColorByType(this.mTintType);
                if (-1275068417 != color) {
                    if (DEBUG) {
                        Log.d("TintImageView", "setTint:" + String.format("#%08X", new Object[]{Integer.valueOf(color)}) + " " + this);
                    }
                    drawable.setTintList(null);
                    drawable.setTint(color);
                } else {
                    if (DEBUG) {
                        Log.d("TintImageView", "setTintList " + this);
                    }
                    drawable.setTintList(null);
                    drawable.setTint(color);
                }
                invalidate();
                return;
            }
            if (DEBUG) {
                Log.d("TintImageView", "no resever setTintList " + this);
            }
        }
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        if (visibility == 0 && !TintUtil.isAnyParentNodeInvisible(this)) {
            setTint();
        }
        super.onVisibilityChanged(changedView, visibility);
    }

    public void setImageTintList(ColorStateList tint) {
        if (DEBUG) {
            Log.d("TintImageView", "setImageTintList:" + tint + " " + this);
        }
        if (this.mIsResever) {
            super.setImageTintList(tint);
        }
    }

    public void setIsResever(boolean isResever) {
        if (DEBUG) {
            Log.d("TintImageView", "setIsResever:" + isResever + " " + this);
        }
        this.mIsResever = isResever;
        if (!isResever && getDrawable() != null) {
            getDrawable().setTintList(null);
        }
    }
}
