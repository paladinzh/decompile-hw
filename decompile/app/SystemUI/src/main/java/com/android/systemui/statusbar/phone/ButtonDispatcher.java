package com.android.systemui.statusbar.phone;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import com.android.systemui.statusbar.policy.KeyButtonView;
import java.util.ArrayList;

public class ButtonDispatcher {
    private Integer mAlpha;
    private OnClickListener mClickListener;
    private View mCurrentView;
    private final int mId;
    private Drawable mImageDrawable;
    private int mImageResource = -1;
    private OnLongClickListener mLongClickListener;
    private Boolean mLongClickable;
    private OnTouchListener mTouchListener;
    private final ArrayList<View> mViews = new ArrayList();
    private Integer mVisibility = Integer.valueOf(-1);

    public ButtonDispatcher(int id) {
        this.mId = id;
    }

    void clear() {
        this.mViews.clear();
    }

    void addView(View view) {
        this.mViews.add(view);
        view.setOnClickListener(this.mClickListener);
        view.setOnTouchListener(this.mTouchListener);
        view.setOnLongClickListener(this.mLongClickListener);
        if (this.mLongClickable != null) {
            view.setLongClickable(this.mLongClickable.booleanValue());
        }
        if (this.mAlpha != null) {
            view.setAlpha((float) this.mAlpha.intValue());
        }
        if (this.mVisibility != null) {
            view.setVisibility(this.mVisibility.intValue());
        }
        if (this.mImageResource > 0) {
            ((ImageView) view).setImageResource(this.mImageResource);
        } else if (this.mImageDrawable != null) {
            ((ImageView) view).setImageDrawable(this.mImageDrawable);
        }
    }

    public int getVisibility() {
        return this.mVisibility != null ? this.mVisibility.intValue() : 0;
    }

    public float getAlpha() {
        return (float) (this.mAlpha != null ? this.mAlpha.intValue() : 1);
    }

    public void setImageDrawable(Drawable drawable) {
        this.mImageDrawable = drawable;
        this.mImageResource = -1;
        int N = this.mViews.size();
        for (int i = 0; i < N; i++) {
            ((ImageView) this.mViews.get(i)).setImageDrawable(this.mImageDrawable);
        }
    }

    public void setVisibility(int visibility) {
        if (this.mVisibility.intValue() != visibility) {
            this.mVisibility = Integer.valueOf(visibility);
            int N = this.mViews.size();
            for (int i = 0; i < N; i++) {
                ((View) this.mViews.get(i)).setVisibility(this.mVisibility.intValue());
            }
        }
    }

    public void abortCurrentGesture() {
        int N = this.mViews.size();
        for (int i = 0; i < N; i++) {
            ((KeyButtonView) this.mViews.get(i)).abortCurrentGesture();
        }
    }

    public void setAlpha(int alpha) {
        this.mAlpha = Integer.valueOf(alpha);
        int N = this.mViews.size();
        for (int i = 0; i < N; i++) {
            ((View) this.mViews.get(i)).setAlpha((float) alpha);
        }
    }

    public void setOnClickListener(OnClickListener clickListener) {
        this.mClickListener = clickListener;
        int N = this.mViews.size();
        for (int i = 0; i < N; i++) {
            ((View) this.mViews.get(i)).setOnClickListener(this.mClickListener);
        }
    }

    public void setOnTouchListener(OnTouchListener touchListener) {
        this.mTouchListener = touchListener;
        int N = this.mViews.size();
        for (int i = 0; i < N; i++) {
            ((View) this.mViews.get(i)).setOnTouchListener(this.mTouchListener);
        }
    }

    public void setLongClickable(boolean isLongClickable) {
        this.mLongClickable = Boolean.valueOf(isLongClickable);
        int N = this.mViews.size();
        for (int i = 0; i < N; i++) {
            ((View) this.mViews.get(i)).setLongClickable(this.mLongClickable.booleanValue());
        }
    }

    public void setOnLongClickListener(OnLongClickListener longClickListener) {
        this.mLongClickListener = longClickListener;
        int N = this.mViews.size();
        for (int i = 0; i < N; i++) {
            ((View) this.mViews.get(i)).setOnLongClickListener(this.mLongClickListener);
        }
    }

    public View getCurrentView() {
        return this.mCurrentView;
    }

    public void setCurrentView(View currentView) {
        this.mCurrentView = currentView.findViewById(this.mId);
    }
}
