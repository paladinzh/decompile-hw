package com.android.systemui.statusbar.notification;

import android.util.Pools.SimplePool;
import android.view.NotificationHeaderView;
import android.view.View;
import com.android.systemui.statusbar.CrossFadeHelper;

public class HeaderTransformState extends TransformState {
    private static SimplePool<HeaderTransformState> sInstancePool = new SimplePool(40);
    private View mExpandButton;
    private View mWorkProfileIcon;
    private TransformState mWorkProfileState;

    public void initFrom(View view) {
        super.initFrom(view);
        if (view instanceof NotificationHeaderView) {
            NotificationHeaderView header = (NotificationHeaderView) view;
            this.mExpandButton = header.getExpandButton();
            this.mWorkProfileState = TransformState.obtain();
            this.mWorkProfileIcon = header.getWorkProfileIcon();
            this.mWorkProfileState.initFrom(this.mWorkProfileIcon);
        }
    }

    public boolean transformViewTo(TransformState otherState, float transformationAmount) {
        if (!(this.mTransformedView instanceof NotificationHeaderView)) {
            return false;
        }
        NotificationHeaderView header = this.mTransformedView;
        int childCount = header.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View headerChild = header.getChildAt(i);
            if (headerChild.getVisibility() != 8) {
                if (headerChild != this.mExpandButton) {
                    headerChild.setVisibility(4);
                } else {
                    CrossFadeHelper.fadeOut(this.mExpandButton, transformationAmount);
                }
            }
        }
        return true;
    }

    public void transformViewFrom(TransformState otherState, float transformationAmount) {
        if (this.mTransformedView instanceof NotificationHeaderView) {
            NotificationHeaderView header = this.mTransformedView;
            header.setVisibility(0);
            header.setAlpha(1.0f);
            int childCount = header.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View headerChild = header.getChildAt(i);
                if (headerChild.getVisibility() != 8) {
                    if (headerChild == this.mExpandButton) {
                        CrossFadeHelper.fadeIn(this.mExpandButton, transformationAmount);
                    } else {
                        headerChild.setVisibility(0);
                        if (headerChild == this.mWorkProfileIcon) {
                            this.mWorkProfileState.transformViewFullyFrom(((HeaderTransformState) otherState).mWorkProfileState, transformationAmount);
                        }
                    }
                }
            }
        }
    }

    public static HeaderTransformState obtain() {
        HeaderTransformState instance = (HeaderTransformState) sInstancePool.acquire();
        if (instance != null) {
            return instance;
        }
        return new HeaderTransformState();
    }

    public void recycle() {
        super.recycle();
        sInstancePool.release(this);
    }

    protected void reset() {
        super.reset();
        this.mExpandButton = null;
        this.mWorkProfileState = null;
        if (this.mWorkProfileState != null) {
            this.mWorkProfileState.recycle();
            this.mWorkProfileState = null;
        }
    }

    public void setVisible(boolean visible, boolean force) {
        super.setVisible(visible, force);
        if (this.mTransformedView instanceof NotificationHeaderView) {
            NotificationHeaderView header = this.mTransformedView;
            int childCount = header.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View headerChild = header.getChildAt(i);
                if (force || headerChild.getVisibility() != 8) {
                    headerChild.animate().cancel();
                    if (headerChild.getVisibility() != 8) {
                        headerChild.setVisibility(visible ? 0 : 4);
                    }
                    if (headerChild == this.mExpandButton) {
                        float f;
                        if (visible) {
                            f = 1.0f;
                        } else {
                            f = 0.0f;
                        }
                        headerChild.setAlpha(f);
                    }
                    if (headerChild == this.mWorkProfileIcon) {
                        headerChild.setTranslationX(0.0f);
                        headerChild.setTranslationY(0.0f);
                    }
                }
            }
        }
    }

    public void prepareFadeIn() {
        super.prepareFadeIn();
        if (this.mTransformedView instanceof NotificationHeaderView) {
            NotificationHeaderView header = this.mTransformedView;
            int childCount = header.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View headerChild = header.getChildAt(i);
                if (headerChild.getVisibility() != 8) {
                    headerChild.animate().cancel();
                    headerChild.setVisibility(0);
                    headerChild.setAlpha(1.0f);
                    if (headerChild == this.mWorkProfileIcon) {
                        headerChild.setTranslationX(0.0f);
                        headerChild.setTranslationY(0.0f);
                    }
                }
            }
        }
    }
}
