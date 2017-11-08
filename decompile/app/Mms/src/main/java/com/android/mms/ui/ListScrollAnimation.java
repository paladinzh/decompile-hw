package com.android.mms.ui;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ListView;
import com.google.android.gms.R;

public class ListScrollAnimation {
    private Context mContext;
    private float mDownTranslation;
    private ListView mListView;
    private float mUpTranslation;
    private float mVelocity;

    public ListScrollAnimation(Context context, ListView listView) {
        this.mContext = context;
        this.mListView = listView;
    }

    public void setUpTranslation(float translation) {
        this.mUpTranslation = translation;
    }

    public void setDownTranslation(float translation) {
        this.mDownTranslation = translation;
    }

    public void setVelocity(float mVelocity) {
        this.mVelocity = mVelocity;
    }

    public void startAnim(View visibleView, int position) {
        if (this.mVelocity != 0.0f) {
            float translation = computeTranslation(this.mVelocity);
            if (position >= this.mListView.getFirstVisiblePosition() || this.mVelocity <= 0.0f) {
                if (position > this.mListView.getLastVisiblePosition() && this.mVelocity < 0.0f) {
                    if (Math.abs(this.mDownTranslation) - Math.abs(translation) >= ((float) this.mListView.getDividerHeight())) {
                        translation = this.mDownTranslation;
                    } else {
                        this.mDownTranslation = translation;
                    }
                } else {
                    return;
                }
            } else if (Math.abs(this.mUpTranslation) - Math.abs(translation) >= ((float) this.mListView.getDividerHeight())) {
                translation = this.mUpTranslation;
            } else {
                this.mUpTranslation = translation;
            }
            Animation visibleAnimation = new TranslateAnimation(1, 0.0f, 1, 0.0f, 0, translation, 1, 0.0f);
            visibleAnimation.setDuration(800);
            visibleAnimation.setInterpolator(this.mContext, R.anim.cubic_bezier_interpolator_type_a);
            if (visibleView != null) {
                visibleView.startAnimation(visibleAnimation);
            }
        }
    }

    private float computeTranslation(float velocity) {
        float f = 90.0f;
        if (velocity < 0.0f) {
            if (velocity / 40.0f >= -90.0f) {
                f = (-velocity) / 40.0f;
            }
            return f;
        }
        if (velocity / 40.0f > 90.0f) {
            f = -90.0f;
        } else {
            f = (-velocity) / 40.0f;
        }
        return f;
    }
}
