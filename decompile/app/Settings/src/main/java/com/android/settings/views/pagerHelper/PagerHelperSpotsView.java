package com.android.settings.views.pagerHelper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import java.util.ArrayList;

public class PagerHelperSpotsView extends RelativeLayout {
    private Context mContext;
    private final Drawable mCurrentSpotDrawable;
    private LayoutParams mMatchParams;
    private int mNavHeight;
    private ArrayList<ImageView> mNavSpotImageViews;
    private int mNavWidth;
    private final Drawable mNormalSpotDrawable;
    private int mPageIdx;
    private int mTotalPages;

    public PagerHelperSpotsView(Context context) {
        this(context, null);
    }

    public PagerHelperSpotsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagerHelperSpotsView(Context aContext, AttributeSet aAttrs, int defStyle) {
        super(aContext, aAttrs, defStyle);
        this.mContext = aContext;
        this.mPageIdx = 0;
        this.mMatchParams = new LayoutParams(-1, -2);
        this.mNavSpotImageViews = new ArrayList();
        this.mCurrentSpotDrawable = this.mContext.getResources().getDrawable(2130838513);
        this.mNormalSpotDrawable = this.mContext.getResources().getDrawable(2130838512);
        this.mNavWidth = this.mContext.getResources().getDimensionPixelSize(2131558927);
        this.mNavHeight = this.mContext.getResources().getDimensionPixelSize(2131558928);
    }

    public void setPageIndex(int aIndex) {
        if (aIndex >= this.mNavSpotImageViews.size()) {
            aIndex = this.mNavSpotImageViews.size() - 1;
        } else if (aIndex < 0) {
            aIndex = 0;
        }
        this.mPageIdx = aIndex;
        boolean isLayoutRtl = this.mContext.getResources().getConfiguration().getLayoutDirection() == 1;
        int lLimit = this.mNavSpotImageViews.size();
        int i = 0;
        while (i < lLimit) {
            boolean isSelectedIndex = isLayoutRtl ? i == (lLimit + -1) - this.mPageIdx : i == this.mPageIdx;
            ((ImageView) this.mNavSpotImageViews.get(i)).setImageDrawable(isSelectedIndex ? this.mCurrentSpotDrawable : this.mNormalSpotDrawable);
            i++;
        }
    }

    protected void onLayout(boolean aChanged, int aLeft, int aTop, int aRight, int aBottom) {
        super.onLayout(aChanged, aLeft, aTop, aRight, aBottom);
        int numSpots = this.mNavSpotImageViews.size();
        int width = aRight - aLeft;
        int pad = this.mContext.getResources().getDimensionPixelSize(2131558929);
        aLeft = (width - ((this.mNavWidth * numSpots) + ((numSpots - 1) * pad))) / 2;
        aRight = aLeft + this.mNavWidth;
        aBottom = this.mNavHeight + 0;
        int limit = this.mNavSpotImageViews.size();
        for (int i = 0; i < limit; i++) {
            ((ImageView) this.mNavSpotImageViews.get(i)).layout(aLeft, 0, aRight, aBottom);
            aLeft += this.mNavWidth + pad;
            aRight = aLeft + this.mNavWidth;
        }
    }

    protected void onMeasure(int aWidthMeasureSpec, int aHeightMeasureSpec) {
        super.onMeasure(aWidthMeasureSpec, aHeightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
    }

    public void invalidate() {
        super.invalidate();
        int lLimit = this.mNavSpotImageViews.size();
        for (int i = 0; i < lLimit; i++) {
            ((ImageView) this.mNavSpotImageViews.get(i)).invalidate();
            if (((ImageView) this.mNavSpotImageViews.get(i)).getParent() != null) {
                ((ViewGroup) ((ImageView) this.mNavSpotImageViews.get(i)).getParent()).removeView((View) this.mNavSpotImageViews.get(i));
            }
        }
        initSpotViews();
    }

    public void setPageCount(int length) {
        this.mTotalPages = length;
        initSpotViews();
    }

    private void initSpotViews() {
        if (this.mTotalPages > 0) {
            this.mNavSpotImageViews.clear();
            for (int i = 0; i < this.mTotalPages; i++) {
                ImageView lSpot = new ImageView(this.mContext);
                lSpot.setImageDrawable(this.mNormalSpotDrawable);
                this.mNavSpotImageViews.add(lSpot);
                addView(lSpot, this.mMatchParams);
            }
            setPageIndex(this.mPageIdx);
        }
    }
}
