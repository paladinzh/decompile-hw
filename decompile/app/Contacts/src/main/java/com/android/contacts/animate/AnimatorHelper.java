package com.android.contacts.animate;

public abstract class AnimatorHelper {
    public ContactAnimatorListener mAnimatorListener;

    public interface ContactAnimatorListener {
        void onAnimationEnd();

        void onAnimationStart();
    }

    public void setDetailAnimatorListener(ContactAnimatorListener animatorListener) {
        this.mAnimatorListener = animatorListener;
    }
}
