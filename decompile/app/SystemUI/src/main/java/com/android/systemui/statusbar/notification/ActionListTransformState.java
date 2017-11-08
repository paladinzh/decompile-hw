package com.android.systemui.statusbar.notification;

import android.util.Pools.SimplePool;

public class ActionListTransformState extends TransformState {
    private static SimplePool<ActionListTransformState> sInstancePool = new SimplePool(40);

    protected boolean sameAs(TransformState otherState) {
        return otherState instanceof ActionListTransformState;
    }

    public static ActionListTransformState obtain() {
        ActionListTransformState instance = (ActionListTransformState) sInstancePool.acquire();
        if (instance != null) {
            return instance;
        }
        return new ActionListTransformState();
    }

    public void transformViewFullyFrom(TransformState otherState, float transformationAmount) {
    }

    public void transformViewFullyTo(TransformState otherState, float transformationAmount) {
    }

    protected void resetTransformedView() {
        float y = getTransformedView().getTranslationY();
        super.resetTransformedView();
        getTransformedView().setTranslationY(y);
    }

    public void recycle() {
        super.recycle();
        sInstancePool.release(this);
    }
}
