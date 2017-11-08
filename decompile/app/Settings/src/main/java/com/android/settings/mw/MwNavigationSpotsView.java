package com.android.settings.mw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import java.util.ArrayList;

public class MwNavigationSpotsView extends RelativeLayout {
    private final ImageView mBgView = new ImageView(this.mContext);
    private Context mContext;
    private final Drawable mCurrentSpotDrawable;
    private LayoutParams mMatchParams = new LayoutParams(-1, -1);
    private int mNavHeight;
    private ArrayList<ImageView> mNavSpotImageViews;
    private int mNavWidth;
    private final Drawable mNormalSpotDrawable;
    private int mPageIdx = 0;
    private int mTotalPages = 4;

    public MwNavigationSpotsView(Context aContext, AttributeSet aAttrs) {
        super(aContext, aAttrs);
        this.mContext = aContext;
        this.mBgView.setBackgroundColor(0);
        this.mNavSpotImageViews = new ArrayList();
        this.mCurrentSpotDrawable = this.mContext.getResources().getDrawable(2130838553);
        this.mNormalSpotDrawable = this.mContext.getResources().getDrawable(2130838554);
        this.mNavWidth = this.mContext.getResources().getDimensionPixelSize(2131558857);
        this.mNavHeight = this.mContext.getResources().getDimensionPixelSize(2131558858);
        if (this.mTotalPages > 0) {
            addView(this.mBgView, this.mMatchParams);
            for (int i = 0; i < this.mTotalPages; i++) {
                ImageView lSpot = new ImageView(this.mContext);
                lSpot.setImageDrawable(this.mNormalSpotDrawable);
                lSpot.setMaxWidth(this.mNavWidth);
                lSpot.setMaxHeight(this.mNavHeight);
                lSpot.setPadding(0, 0, this.mContext.getResources().getDimensionPixelSize(2131558859), 0);
                this.mNavSpotImageViews.add(lSpot);
                addView(lSpot, this.mMatchParams);
            }
            setPageIndex(0);
        }
    }

    void setPageIndex(int aIndex) {
        if (aIndex >= this.mNavSpotImageViews.size()) {
            aIndex = this.mNavSpotImageViews.size() - 1;
        } else if (aIndex < 0) {
            aIndex = 0;
        }
        this.mPageIdx = aIndex;
        int lLimit = this.mNavSpotImageViews.size();
        for (int i = 0; i < lLimit; i++) {
            ImageView lNavSpot = (ImageView) this.mNavSpotImageViews.get(i);
            lNavSpot.setImageDrawable(this.mNormalSpotDrawable);
            lNavSpot.setMaxWidth(this.mNavWidth);
            lNavSpot.setMaxHeight(this.mNavHeight);
            lNavSpot.setPadding(0, 0, this.mContext.getResources().getDimensionPixelSize(2131558859), 0);
        }
        ((ImageView) this.mNavSpotImageViews.get(this.mPageIdx)).setImageDrawable(this.mCurrentSpotDrawable);
        super.invalidate();
    }

    protected void onLayout(boolean aChanged, int aLeft, int aTop, int aRight, int aBottom) {
        super.onLayout(aChanged, aLeft, aTop, aRight, aBottom);
        this.mBgView.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
        int numSpots = this.mNavSpotImageViews.size();
        int sideLength = this.mNavWidth + 0;
        aLeft = ((aRight - aLeft) - ((sideLength * numSpots) + ((numSpots - 1) * 0))) / 2;
        aTop = this.mContext.getResources().getDimensionPixelSize(2131558860);
        aRight = aLeft + this.mNavWidth;
        aBottom = aTop + this.mNavHeight;
        int limit = this.mNavSpotImageViews.size();
        for (int i = 0; i < limit; i++) {
            ((ImageView) this.mNavSpotImageViews.get(i)).layout(aLeft, aTop, aRight, aBottom);
            aLeft += sideLength + 0;
            aRight = aLeft + this.mNavWidth;
        }
    }

    protected void onMeasure(int aWidthMeasureSpec, int aHeightMeasureSpec) {
        super.onMeasure(aWidthMeasureSpec, aHeightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
    }

    public void invalidate() {
        int i;
        super.invalidate();
        if (this.mBgView.getParent() != null) {
            ((ViewGroup) this.mBgView.getParent()).removeView(this.mBgView);
        }
        int lLimit = this.mNavSpotImageViews.size();
        for (i = 0; i < lLimit; i++) {
            ((ImageView) this.mNavSpotImageViews.get(i)).invalidate();
            if (((ImageView) this.mNavSpotImageViews.get(i)).getParent() != null) {
                ((ViewGroup) ((ImageView) this.mNavSpotImageViews.get(i)).getParent()).removeView((View) this.mNavSpotImageViews.get(i));
            }
        }
        this.mTotalPages = 4;
        this.mNavSpotImageViews.clear();
        if (this.mTotalPages > 0) {
            addView(this.mBgView, this.mMatchParams);
            for (i = 0; i < this.mTotalPages; i++) {
                ImageView lSpot = new ImageView(this.mContext);
                lSpot.setImageDrawable(this.mNormalSpotDrawable);
                lSpot.setMaxWidth(this.mNavWidth);
                lSpot.setMaxHeight(this.mNavHeight);
                lSpot.setPadding(0, 0, this.mContext.getResources().getDimensionPixelSize(2131558859), 0);
                this.mNavSpotImageViews.add(lSpot);
                addView(lSpot, this.mMatchParams);
            }
        }
        setPageIndex(0);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
