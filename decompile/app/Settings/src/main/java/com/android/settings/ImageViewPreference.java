package com.android.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ImageViewPreference extends Preference {
    private AnimationDrawable mAnimationDrawable;
    private Drawable mDrawable;
    private ImageView mImageView;

    public ImageViewPreference(Context context) {
        this(context, null);
    }

    public ImageViewPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.ImageViewPreference);
        this.mDrawable = a.getDrawable(0);
        setLayoutResource(2130968833);
        setSelectable(false);
        a.recycle();
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.mImageView = (ImageView) view.findViewById(2131886716);
        this.mImageView.setBackground(this.mDrawable);
        if (this.mDrawable instanceof AnimationDrawable) {
            this.mAnimationDrawable = (AnimationDrawable) this.mDrawable;
            this.mAnimationDrawable.start();
        }
    }

    public void cancelAnimation() {
        if (this.mAnimationDrawable != null) {
            this.mAnimationDrawable.stop();
            this.mAnimationDrawable = null;
            this.mDrawable = null;
        }
        if (this.mImageView != null) {
            this.mImageView.setBackground(null);
        }
    }

    public void setDrawable(Drawable drawable) {
        this.mDrawable = drawable;
    }
}
