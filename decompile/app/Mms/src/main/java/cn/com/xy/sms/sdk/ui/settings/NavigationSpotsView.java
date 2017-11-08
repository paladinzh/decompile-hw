package cn.com.xy.sms.sdk.ui.settings;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.R;
import java.util.ArrayList;

public class NavigationSpotsView extends RelativeLayout {
    private final boolean IS_MIRROR_LANGUAGE = MessageUtils.isNeedLayoutRtl();
    private final ImageView mBgView;
    private Context mContext;
    private final Drawable mCurrentSpotDrawable;
    private LayoutParams mMatchParams;
    private int mNavHeight;
    private ArrayList<ImageView> mNavSpotImageViews;
    private int mNavWidth;
    private final Drawable mNormalSpotDrawable;
    private int mPageIdx;
    private int mTotalPages;

    public NavigationSpotsView(Context aContext, AttributeSet aAttrs) {
        super(aContext, aAttrs);
        this.mContext = aContext;
        this.mPageIdx = 0;
        this.mMatchParams = new LayoutParams(-1, -1);
        this.mBgView = new ImageView(this.mContext);
        this.mBgView.setBackgroundColor(0);
        this.mNavSpotImageViews = new ArrayList();
        this.mCurrentSpotDrawable = this.mContext.getResources().getDrawable(R.drawable.duoqu_point_off);
        this.mNormalSpotDrawable = this.mContext.getResources().getDrawable(R.drawable.duoqu_point_on);
        this.mNavWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.nav_icon_width);
        this.mNavHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.nav_icon_height);
        addSpotToView();
    }

    void setPageIndex(int aIndex) {
        if (aIndex == 0 || aIndex < 0) {
            aIndex = this.IS_MIRROR_LANGUAGE ? this.mNavSpotImageViews.size() - 1 : 0;
        } else if (aIndex == this.mNavSpotImageViews.size() - 1 || aIndex >= this.mNavSpotImageViews.size()) {
            aIndex = this.IS_MIRROR_LANGUAGE ? 0 : this.mNavSpotImageViews.size() - 1;
        }
        this.mPageIdx = aIndex;
        int lLimit = this.mNavSpotImageViews.size();
        for (int i = 0; i < lLimit; i++) {
            setImageViewParam((ImageView) this.mNavSpotImageViews.get(i));
        }
        ((ImageView) this.mNavSpotImageViews.get(this.mPageIdx)).setImageDrawable(this.mCurrentSpotDrawable);
        super.invalidate();
    }

    int getPageIndex() {
        return this.mPageIdx;
    }

    boolean isOnLastPage() {
        return getPageIndex() == this.mNavSpotImageViews.size() + -1;
    }

    boolean isOnFirstPage() {
        return getPageIndex() == 0;
    }

    protected void onLayout(boolean aChanged, int aLeft, int aTop, int aRight, int aBottom) {
        super.onLayout(aChanged, aLeft, aTop, aRight, aBottom);
        this.mBgView.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
        int numSpots = this.mNavSpotImageViews.size();
        int sideLength = this.mNavWidth + 0;
        aLeft = ((aRight - aLeft) - ((sideLength * numSpots) + ((numSpots - 1) * 0))) / 2;
        aTop = this.mContext.getResources().getDimensionPixelSize(R.dimen.nav_icon_padding_top);
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
        super.invalidate();
        if (this.mBgView.getParent() != null) {
            ((ViewGroup) this.mBgView.getParent()).removeView(this.mBgView);
        }
        int lLimit = this.mNavSpotImageViews.size();
        for (int i = 0; i < lLimit; i++) {
            ((ImageView) this.mNavSpotImageViews.get(i)).invalidate();
            if (((ImageView) this.mNavSpotImageViews.get(i)).getParent() != null) {
                ((ViewGroup) ((ImageView) this.mNavSpotImageViews.get(i)).getParent()).removeView((View) this.mNavSpotImageViews.get(i));
            }
        }
        addSpotToView();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    private void setImageViewParam(ImageView spot) {
        spot.setImageDrawable(this.mNormalSpotDrawable);
        spot.setMaxWidth(this.mNavWidth);
        spot.setMaxHeight(this.mNavHeight);
        spot.setPaddingRelative(0, 0, this.mContext.getResources().getDimensionPixelSize(R.dimen.nav_icon_spacing), 0);
    }

    private void addSpotToView() {
        this.mTotalPages = 3;
        addView(this.mBgView, this.mMatchParams);
        for (int i = 0; i < this.mTotalPages; i++) {
            ImageView spot = new ImageView(this.mContext);
            setImageViewParam(spot);
            this.mNavSpotImageViews.add(spot);
            addView(spot, this.mMatchParams);
        }
        setPageIndex(0);
    }
}
