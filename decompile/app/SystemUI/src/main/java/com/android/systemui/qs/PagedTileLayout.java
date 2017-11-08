package com.android.systemui.qs;

import android.content.Context;
import android.os.SystemProperties;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.LayoutParams;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import com.android.systemui.R;
import com.android.systemui.qs.QSPanel.QSTileLayout;
import com.android.systemui.qs.QSPanel.TileRecord;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.PerfAdjust;
import java.util.ArrayList;

public class PagedTileLayout extends ViewPager implements QSTileLayout {
    private final PagerAdapter mAdapter = new PagerAdapter() {
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        public Object instantiateItem(ViewGroup container, int position) {
            if (PagedTileLayout.this.isLayoutRtl()) {
                position = (PagedTileLayout.this.mPages.size() - 1) - position;
            }
            ViewGroup view = (ViewGroup) PagedTileLayout.this.mPages.get(position);
            if (view != null) {
                ViewParent parent = view.getParent();
                if (parent != null && (parent instanceof ViewGroup)) {
                    ((ViewGroup) parent).removeView(view);
                    Log.e("PagedTileLayout", "instantiateItem::view's parent is not null, remove it from parent first.");
                }
            }
            container.addView(view);
            return view;
        }

        public int getCount() {
            return PagedTileLayout.this.mNumPages;
        }

        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    };
    private View mBrightnessView;
    private View mDecorGroup;
    private final Runnable mDistribute = new Runnable() {
        public void run() {
            PagedTileLayout.this.distributeTiles();
        }
    };
    private boolean mListening;
    private int mNumPages;
    private boolean mOffPage;
    private PageIndicator mPageIndicator;
    private PageListener mPageListener;
    private final ArrayList<TilePage> mPages = new ArrayList();
    private int mPosition;
    private final ArrayList<TileRecord> mTiles = new ArrayList();

    public interface PageListener {
        void onPageChanged(boolean z);
    }

    public static class TilePage extends TileLayout {
        private int mMaxRows = 3;

        public TilePage(Context context, AttributeSet attrs) {
            super(context, attrs);
            updateResources();
            setContentDescription(this.mContext.getString(R.string.accessibility_desc_quick_settings));
        }

        public boolean updateResources() {
            int rows = getRows();
            boolean changed = rows != this.mMaxRows;
            if (changed) {
                this.mMaxRows = rows;
                requestLayout();
            }
            if (super.updateResources()) {
                return true;
            }
            return changed;
        }

        private int getRows() {
            return Math.max(1, getContext().getResources().getInteger(PerfAdjust.getQuickSettingsNumRows()));
        }

        public boolean isFull() {
            return this.mRecords.size() >= this.mColumns * this.mMaxRows;
        }

        public boolean onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            return false;
        }
    }

    public PagedTileLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAdapter(this.mAdapter);
        setOnPageChangeListener(new OnPageChangeListener() {
            public void onPageSelected(int position) {
                boolean z = true;
                if (!(PagedTileLayout.this.mPageIndicator == null || PagedTileLayout.this.mPageListener == null)) {
                    PageListener -get2 = PagedTileLayout.this.mPageListener;
                    if (PagedTileLayout.this.isLayoutRtl()) {
                        if (position != PagedTileLayout.this.mPages.size() - 1) {
                            z = false;
                        }
                    } else if (position != 0) {
                        z = false;
                    }
                    -get2.onPageChanged(z);
                }
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                boolean z = false;
                if (PagedTileLayout.this.mPageIndicator != null) {
                    boolean z2;
                    PagedTileLayout pagedTileLayout = PagedTileLayout.this;
                    if (positionOffset != 0.0f) {
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                    pagedTileLayout.setCurrentPage(position, z2);
                    PagedTileLayout.this.mPageIndicator.setLocation(((float) position) + positionOffset);
                    if (PagedTileLayout.this.mPageListener != null) {
                        PageListener -get2 = PagedTileLayout.this.mPageListener;
                        if (positionOffsetPixels == 0) {
                            if (!PagedTileLayout.this.isLayoutRtl()) {
                                if (position == 0) {
                                }
                            }
                            z = true;
                        }
                        -get2.onPageChanged(z);
                    }
                }
            }

            public void onPageScrollStateChanged(int state) {
            }
        });
        setCurrentItem(0);
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        setAdapter(this.mAdapter);
        setCurrentItem(0, false);
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        if (isLayoutRtl()) {
            item = (this.mPages.size() - 1) - item;
        }
        super.setCurrentItem(item, smoothScroll);
    }

    public void setListening(boolean listening) {
        if (this.mListening != listening) {
            this.mListening = listening;
            if (this.mListening) {
                ((TilePage) this.mPages.get(this.mPosition)).setListening(listening);
                if (this.mOffPage && this.mPosition < this.mPages.size() - 1) {
                    ((TilePage) this.mPages.get(this.mPosition + 1)).setListening(listening);
                }
            } else {
                for (int i = 0; i < this.mPages.size(); i++) {
                    ((TilePage) this.mPages.get(i)).setListening(false);
                }
            }
        }
    }

    private void setCurrentPage(int position, boolean offPage) {
        if (this.mPosition != position || this.mOffPage != offPage) {
            if (this.mListening) {
                if (this.mPosition != position) {
                    setPageListening(this.mPosition, false);
                    if (this.mOffPage) {
                        setPageListening(this.mPosition + 1, false);
                    }
                    setPageListening(position, true);
                    if (offPage) {
                        setPageListening(position + 1, true);
                    }
                } else if (this.mOffPage != offPage) {
                    setPageListening(this.mPosition + 1, offPage);
                }
            }
            this.mPosition = position;
            this.mOffPage = offPage;
        }
    }

    private void setPageListening(int position, boolean listening) {
        if (position < this.mPages.size()) {
            ((TilePage) this.mPages.get(position)).setListening(listening);
        }
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mPageIndicator = (PageIndicator) findViewById(R.id.page_indicator);
        this.mDecorGroup = findViewById(R.id.page_decor);
        ((LayoutParams) this.mDecorGroup.getLayoutParams()).isDecor = true;
        this.mPages.add((TilePage) LayoutInflater.from(this.mContext).inflate(R.layout.qs_paged_page, this, false));
    }

    public int getOffsetTop(TileRecord tile) {
        ViewGroup parent = (ViewGroup) tile.tileView.getParent();
        if (parent == null) {
            return 0;
        }
        return parent.getTop() + getTop();
    }

    public void addTile(TileRecord tile) {
        this.mTiles.add(tile);
        postDistributeTiles();
    }

    public void removeTile(TileRecord tile) {
        if (this.mTiles.remove(tile)) {
            postDistributeTiles();
        }
    }

    private void postDistributeTiles() {
        removeCallbacks(this.mDistribute);
        post(this.mDistribute);
    }

    private void distributeTiles() {
        int i;
        int NP = this.mPages.size();
        for (i = 0; i < NP; i++) {
            ((TilePage) this.mPages.get(i)).removeAllViews();
        }
        int NT = this.mTiles.size();
        for (i = 0; i < NT; i++) {
            TileRecord tile = (TileRecord) this.mTiles.get(i);
            if (((TilePage) this.mPages.get(0)).isFull()) {
                break;
            }
            ((TilePage) this.mPages.get(0)).addTile(tile);
        }
        if (this.mNumPages != 1) {
            this.mNumPages = 1;
            while (this.mPages.size() > this.mNumPages) {
                this.mPages.remove(this.mPages.size() - 1);
            }
            this.mPageIndicator.setNumPages(this.mNumPages);
            setAdapter(this.mAdapter);
            this.mAdapter.notifyDataSetChanged();
            setCurrentItem(0, false);
        }
    }

    public boolean updateResources() {
        boolean changed = false;
        for (int i = 0; i < this.mPages.size(); i++) {
            changed |= ((TilePage) this.mPages.get(i)).updateResources();
        }
        if (changed && !SystemProperties.getBoolean("sys.super_power_save", false)) {
            distributeTiles();
        }
        return changed;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int maxHeight = 0;
        int N = getChildCount();
        for (int i = 0; i < N; i++) {
            int height = getChildAt(i).getMeasuredHeight();
            if (height > maxHeight) {
                maxHeight = height;
            }
        }
        setMeasuredDimension(getMeasuredWidth(), this.mDecorGroup.getMeasuredHeight() + maxHeight);
    }

    public boolean onTouchEvent(MotionEvent event) {
        HwLog.i("PagedTileLayout", "onTouchEvent:" + event);
        return false;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (this.mBrightnessView.pointInView(((ev.getX() + ((float) getLeft())) - ((float) this.mBrightnessView.getLeft())) - this.mBrightnessView.getTranslationX(), ((ev.getY() + ((float) getTop())) - ((float) this.mBrightnessView.getTop())) - this.mBrightnessView.getTranslationY(), 0.0f)) {
            return true;
        }
        return false;
    }

    public void setBrightnessView(View brightnessView) {
        this.mBrightnessView = brightnessView;
    }

    public void addCallback(HwSuperpowerModeManager modeManager) {
        for (int i = 0; i < this.mPages.size(); i++) {
            ((TilePage) this.mPages.get(i)).addCallback(modeManager);
        }
    }
}
