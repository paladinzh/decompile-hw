package com.huawei.systemmanager.spacecleanner.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class BounceScrollView extends ScrollView {
    private static final int MAX_Y_OVERSCROLL_DISTANCE = 200;
    private Context mContext;
    private int mMaxYOverscrollDistance;

    public BounceScrollView(Context context) {
        super(context);
        this.mContext = context;
        initBounceScrollView();
    }

    public BounceScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initBounceScrollView();
    }

    public BounceScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        initBounceScrollView();
    }

    private void initBounceScrollView() {
        this.mMaxYOverscrollDistance = (int) (200.0f * this.mContext.getResources().getDisplayMetrics().density);
    }

    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        int newDeltaY = deltaY;
        if (isTouchEvent) {
            newDeltaY = getElasticInterpolation(deltaY, scrollY);
        }
        int maxOverScrollYDistance = maxOverScrollY;
        if (newDeltaY + scrollY > scrollRangeY) {
            maxOverScrollYDistance = getHeight();
        }
        invalidate();
        return super.overScrollBy(deltaX, newDeltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollYDistance, isTouchEvent);
    }

    private int getElasticInterpolation(int delta, int currentPos) {
        float len = (float) Math.abs(currentPos);
        return (int) ((Math.sqrt(((double) (250.0f * ((float) Math.abs(delta)))) + Math.pow((double) len, 2.0d)) - ((double) len)) * ((double) Math.signum((float) delta)));
    }
}
