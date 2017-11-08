package com.android.gallery3d.ui;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.gadget.XmlUtils;
import com.android.gallery3d.ui.DiscoverItemAdapter.DiscoverStyleKey;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.photoshare.DiscoverHeadDataLoader.AlbumSet;
import com.huawei.gallery.util.MyPrinter;

public class DiscoverItemView extends AdapterView<DiscoverItemAdapter> {
    private static boolean DEBUG = false;
    private static final MyPrinter LOG = new MyPrinter("DiscoverItemView");
    private static final int UNIT_GAP = GalleryUtils.dpToPixel(2);
    private DiscoverItemAdapter mAdapter;
    private MyOnClickListener[] mClickListeners;
    private DataSetObserver mDataSetObserver;
    private boolean mIsPhone;
    private Style mStyle;
    private WindowManager mWindowManager;

    private class MyOnClickListener implements OnClickListener, OnLongClickListener {
        int position;

        private MyOnClickListener(int pos) {
            this.position = pos;
        }

        public void onClick(View v) {
            DiscoverItemView.this.performItemClick(v, this.position, DiscoverItemView.this.mAdapter.getItemId(this.position));
        }

        public boolean onLongClick(View v) {
            OnItemLongClickListener listener = DiscoverItemView.this.getOnItemLongClickListener();
            if (listener == null) {
                return false;
            }
            return listener.onItemLongClick(DiscoverItemView.this, v, this.position, DiscoverItemView.this.mAdapter.getItemId(this.position));
        }
    }

    public enum Style {
        Nomal(6),
        Story(3);
        
        int mMaxCount;

        private Style(int maxCount) {
            this.mMaxCount = maxCount;
        }
    }

    private static void debugd(String msg) {
        if (DEBUG) {
            LOG.d(msg);
        }
    }

    public DiscoverItemView(Context context) {
        this(context, null);
    }

    public DiscoverItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DiscoverItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mStyle = Style.Nomal;
        this.mIsPhone = true;
        this.mClickListeners = new MyOnClickListener[6];
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mAdapter = new DiscoverItemAdapter(context);
        this.mDataSetObserver = new PhotoShareDataSetObserver(this);
        this.mAdapter.registerDataSetObserver(this.mDataSetObserver);
        this.mIsPhone = !GalleryUtils.isTabletProduct(context);
    }

    private int getActiveCount() {
        if (this.mAdapter != null) {
            return Utils.clamp(this.mAdapter.getCount(), 0, this.mStyle.mMaxCount);
        }
        return 0;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int activeChildCount = getActiveCount();
        if (activeChildCount == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int height;
        DisplayMetrics metrics = new DisplayMetrics();
        this.mWindowManager.getDefaultDisplay().getRealMetrics(metrics);
        int displayWidth = metrics.widthPixels;
        int displayHeight = metrics.heightPixels;
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (this.mIsPhone && displayHeight > displayWidth) {
            int unitLength = (width - (UNIT_GAP * 2)) / 3;
            int biggerUnit = (width - UNIT_GAP) / 2;
            if (activeChildCount == 1 || activeChildCount == 2) {
                height = biggerUnit;
            } else if (activeChildCount == 4 || activeChildCount == 5) {
                height = (biggerUnit + unitLength) + UNIT_GAP;
            } else {
                height = (unitLength * 2) + UNIT_GAP;
            }
        } else if (activeChildCount <= 4) {
            height = (width - (UNIT_GAP * 3)) / 4;
        } else {
            height = (width - (UNIT_GAP * 4)) / 5;
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, 1073741824), MeasureSpec.makeMeasureSpec(height, 1073741824));
    }

    private View obtainAndLayoutChild(Adapter adapter, int index, int l, int t, int r, int b, boolean needMarkness) {
        debugd(String.format("obtainAndLayoutChild: index:%s, left:%s, top:%s, right:%s, bottom:%s", new Object[]{Integer.valueOf(index), Integer.valueOf(l), Integer.valueOf(t), Integer.valueOf(r), Integer.valueOf(b)}));
        View child = getChildAt(index);
        if (adapter != null) {
            View updated;
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(r - l, 1073741824);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(b - t, 1073741824);
            if (adapter instanceof DiscoverItemAdapter) {
                updated = ((DiscoverItemAdapter) adapter).getView(new DiscoverStyleKey(this.mStyle, index, needMarkness), child, (ViewGroup) this);
            } else {
                updated = adapter.getView(index, child, this);
            }
            if (updated == null) {
                removeDetachedView(child, false);
                return null;
            }
            if (updated != child) {
                MyOnClickListener listener = getListener(index);
                if (child != null) {
                    removeDetachedView(child, false);
                }
                addViewInLayout(updated, index, generateDefaultLayoutParams());
                updated.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                updated.setOnClickListener(listener);
                updated.setOnLongClickListener(listener);
            } else {
                attachViewToParent(child, index, child.getLayoutParams());
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
            updated.layout(l, t, r, b);
            return updated;
        }
        if (child != null) {
            child.layout(l, t, r, b);
        }
        return child;
    }

    private View obtainAndLayoutChild(Adapter adapter, int index, int l, int t, int r, int b) {
        return obtainAndLayoutChild(adapter, index, l, t, r, b, false);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        LOG.d(String.format("onLayout: left:%s, top:%s, right:%s, bottom:%s", new Object[]{Integer.valueOf(l), Integer.valueOf(t), Integer.valueOf(r), Integer.valueOf(b)}));
        int activeChildCount = getActiveCount();
        if (activeChildCount == 0) {
            super.onLayout(changed, l, t, r, b);
            removeAllViewsInLayout();
            return;
        }
        if (isRtlLocale()) {
            layoutChildrenRtl(activeChildCount, l, t, r, b);
        } else {
            layoutChidrenLtr(activeChildCount, l, t, r, b);
        }
    }

    private void layoutChidrenLtr(int activeChildCount, int l, int t, int r, int b) {
        invalidate();
        r -= l;
        b -= t;
        DisplayMetrics metrics = new DisplayMetrics();
        this.mWindowManager.getDefaultDisplay().getRealMetrics(metrics);
        int displayWidth = metrics.widthPixels;
        int displayHeight = metrics.heightPixels;
        detachAllViewsFromParent();
        if (!this.mIsPhone || displayHeight <= displayWidth) {
            layoutChidrenLtrLand(activeChildCount, 0, 0, r, b);
        } else {
            layoutChidrenLtrPort(activeChildCount, 0, 0, r, b);
        }
        int childCount = getChildCount();
        for (int i = activeChildCount; i < childCount; i++) {
            removeDetachedView(getChildAt(i), false);
        }
    }

    private void layoutChidrenLtrPort(int activeChildCount, int l, int t, int r, int b) {
        Adapter adapter = this.mAdapter;
        int width = getMeasuredWidth();
        int unitLength = (width - (UNIT_GAP * 2)) / 3;
        int biggerUnit = (width - UNIT_GAP) / 2;
        int left;
        int bottom;
        int top;
        switch (activeChildCount) {
            case 1:
                obtainAndLayoutChild(adapter, 0, l, t, r, b, true);
                return;
            case 2:
                obtainAndLayoutChild(adapter, 0, l, t, biggerUnit, b);
                obtainAndLayoutChild(adapter, 1, biggerUnit + UNIT_GAP, t, r, b);
                return;
            case 3:
                obtainAndLayoutChild(adapter, 0, l, t, (r - unitLength) - UNIT_GAP, b, true);
                left = r - unitLength;
                obtainAndLayoutChild(adapter, 1, left, t, r, unitLength);
                obtainAndLayoutChild(adapter, 2, left, b - unitLength, r, b);
                return;
            case 4:
                bottom = unitLength;
                obtainAndLayoutChild(adapter, 0, l, t, unitLength, unitLength);
                obtainAndLayoutChild(adapter, 1, (l + unitLength) + UNIT_GAP, t, (r - unitLength) - UNIT_GAP, unitLength);
                obtainAndLayoutChild(adapter, 2, r - unitLength, t, r, unitLength);
                obtainAndLayoutChild(adapter, 3, l, unitLength + UNIT_GAP, r, b);
                return;
            case 5:
                bottom = (b - unitLength) - UNIT_GAP;
                obtainAndLayoutChild(adapter, 0, l, t, biggerUnit, bottom);
                obtainAndLayoutChild(adapter, 1, r - biggerUnit, t, r, bottom);
                top = b - unitLength;
                obtainAndLayoutChild(adapter, 2, l, top, unitLength, b);
                obtainAndLayoutChild(adapter, 3, (l + unitLength) + UNIT_GAP, top, (r - unitLength) - UNIT_GAP, b);
                obtainAndLayoutChild(adapter, 4, r - unitLength, top, r, b);
                return;
            case 6:
                left = (l + unitLength) + UNIT_GAP;
                int right = (r - unitLength) - UNIT_GAP;
                bottom = unitLength;
                obtainAndLayoutChild(adapter, 0, l, t, unitLength, unitLength);
                obtainAndLayoutChild(adapter, 1, left, t, right, unitLength);
                obtainAndLayoutChild(adapter, 2, r - unitLength, t, r, unitLength);
                top = b - unitLength;
                obtainAndLayoutChild(adapter, 3, l, top, unitLength, b);
                obtainAndLayoutChild(adapter, 4, left, top, right, b);
                obtainAndLayoutChild(adapter, 5, r - unitLength, top, r, b);
                return;
            default:
                GalleryLog.e("DiscoverItemView", "[port]there is too many children, activeChildCount " + activeChildCount);
                return;
        }
    }

    private void layoutChidrenLtrLand(int activeChildCount, int l, int t, int r, int b) {
        int unitLength;
        Adapter adapter = this.mAdapter;
        int width = getMeasuredWidth();
        if (activeChildCount > 5) {
            activeChildCount = 5;
        }
        if (activeChildCount <= 4) {
            unitLength = (width - (UNIT_GAP * 3)) / 4;
        } else {
            unitLength = (width - (UNIT_GAP * 4)) / 5;
        }
        int biggerUnit = (width - UNIT_GAP) / 2;
        int left;
        int right;
        switch (activeChildCount) {
            case 1:
                r = biggerUnit;
                obtainAndLayoutChild(adapter, 0, l, t, biggerUnit, b, true);
                return;
            case 2:
                obtainAndLayoutChild(adapter, 0, l, t, biggerUnit, b, true);
                obtainAndLayoutChild(adapter, 1, biggerUnit + UNIT_GAP, t, r, b, true);
                return;
            case 3:
                obtainAndLayoutChild(adapter, 0, l, t, unitLength, b);
                obtainAndLayoutChild(adapter, 1, unitLength + UNIT_GAP, t, (r - unitLength) - UNIT_GAP, b, true);
                obtainAndLayoutChild(adapter, 2, r - unitLength, t, r, b);
                return;
            case 4:
                obtainAndLayoutChild(adapter, 0, l, t, unitLength, b);
                left = unitLength + UNIT_GAP;
                obtainAndLayoutChild(adapter, 1, left, t, left + unitLength, b);
                right = (r - unitLength) - UNIT_GAP;
                obtainAndLayoutChild(adapter, 2, right - unitLength, t, right, b);
                obtainAndLayoutChild(adapter, 3, r - unitLength, t, r, b);
                return;
            case 5:
                obtainAndLayoutChild(adapter, 0, l, t, unitLength, b);
                left = unitLength + UNIT_GAP;
                obtainAndLayoutChild(adapter, 1, left, t, left + unitLength, b);
                obtainAndLayoutChild(adapter, 2, (UNIT_GAP + unitLength) * 2, t, r - ((UNIT_GAP + unitLength) * 2), b);
                right = (r - unitLength) - UNIT_GAP;
                obtainAndLayoutChild(adapter, 3, right - unitLength, t, right, b);
                obtainAndLayoutChild(adapter, 4, r - unitLength, t, r, b);
                return;
            default:
                GalleryLog.e("DiscoverItemView", "[land]there is too many children " + activeChildCount);
                return;
        }
    }

    private void layoutChildrenRtl(int activeChildCount, int l, int t, int r, int b) {
        r -= l;
        b -= t;
        invalidate();
        debugd(XmlUtils.START_TAG);
        DisplayMetrics metrics = new DisplayMetrics();
        this.mWindowManager.getDefaultDisplay().getRealMetrics(metrics);
        int displayWidth = metrics.widthPixels;
        int displayHeight = metrics.heightPixels;
        detachAllViewsFromParent();
        if (!this.mIsPhone || displayHeight <= displayWidth) {
            layoutChildrenRtlLand(activeChildCount, 0, 0, r, b);
        } else {
            layoutChildrenRtlPort(activeChildCount, 0, 0, r, b);
        }
        int childCount = getChildCount();
        for (int i = activeChildCount; i < childCount; i++) {
            removeDetachedView(getChildAt(i), false);
        }
    }

    private void layoutChildrenRtlPort(int activeChildCount, int l, int t, int r, int b) {
        Adapter adapter = this.mAdapter;
        int width = getMeasuredWidth();
        int unitLength = (width - (UNIT_GAP * 2)) / 3;
        int biggerUnit = (width - UNIT_GAP) / 2;
        int right;
        int bottom;
        int left;
        int top;
        switch (activeChildCount) {
            case 1:
                obtainAndLayoutChild(adapter, 0, l, t, r, b);
                return;
            case 2:
                obtainAndLayoutChild(adapter, 0, biggerUnit + UNIT_GAP, t, r, b);
                obtainAndLayoutChild(adapter, 1, l, t, biggerUnit, b);
                return;
            case 3:
                right = unitLength;
                obtainAndLayoutChild(adapter, 0, unitLength + UNIT_GAP, t, r, b);
                obtainAndLayoutChild(adapter, 1, l, t, unitLength, unitLength);
                obtainAndLayoutChild(adapter, 2, l, b - unitLength, unitLength, b);
                return;
            case 4:
                bottom = unitLength;
                left = (l + unitLength) + UNIT_GAP;
                right = (r - unitLength) - UNIT_GAP;
                obtainAndLayoutChild(adapter, 0, r - unitLength, t, r, unitLength);
                obtainAndLayoutChild(adapter, 1, left, t, right, unitLength);
                obtainAndLayoutChild(adapter, 2, l, t, unitLength, unitLength);
                obtainAndLayoutChild(adapter, 3, l, unitLength + UNIT_GAP, r, b);
                return;
            case 5:
                bottom = (b - unitLength) - UNIT_GAP;
                obtainAndLayoutChild(adapter, 0, r - biggerUnit, t, r, bottom);
                obtainAndLayoutChild(adapter, 1, l, t, biggerUnit, bottom);
                top = b - unitLength;
                left = (l + unitLength) + UNIT_GAP;
                right = (r - unitLength) - UNIT_GAP;
                obtainAndLayoutChild(adapter, 2, r - unitLength, top, r, b);
                obtainAndLayoutChild(adapter, 3, left, top, right, b);
                obtainAndLayoutChild(adapter, 4, l, top, unitLength, b);
                return;
            case 6:
                left = (l + unitLength) + UNIT_GAP;
                right = (r - unitLength) - UNIT_GAP;
                bottom = unitLength;
                obtainAndLayoutChild(adapter, 0, r - unitLength, t, r, unitLength);
                obtainAndLayoutChild(adapter, 1, left, t, right, unitLength);
                obtainAndLayoutChild(adapter, 2, l, t, unitLength, unitLength);
                top = b - unitLength;
                obtainAndLayoutChild(adapter, 3, r - unitLength, top, r, b);
                obtainAndLayoutChild(adapter, 4, left, top, right, b);
                obtainAndLayoutChild(adapter, 5, l, top, unitLength, b);
                return;
            default:
                GalleryLog.e("DiscoverItemView", "[port]there is too many children " + activeChildCount);
                return;
        }
    }

    private void layoutChildrenRtlLand(int activeChildCount, int l, int t, int r, int b) {
        int unitLength;
        Adapter adapter = this.mAdapter;
        int width = getMeasuredWidth();
        if (activeChildCount > 5) {
            activeChildCount = 5;
        }
        if (activeChildCount <= 4) {
            unitLength = (width - (UNIT_GAP * 3)) / 4;
        } else {
            unitLength = (width - (UNIT_GAP * 4)) / 5;
        }
        int biggerUnit = (width - UNIT_GAP) / 2;
        int right;
        int left;
        switch (activeChildCount) {
            case 1:
                obtainAndLayoutChild(adapter, 0, biggerUnit + UNIT_GAP, t, r, b);
                return;
            case 2:
                obtainAndLayoutChild(adapter, 0, biggerUnit + UNIT_GAP, t, r, b);
                obtainAndLayoutChild(adapter, 1, l, t, biggerUnit, b);
                return;
            case 3:
                obtainAndLayoutChild(adapter, 0, r - unitLength, t, r, b);
                obtainAndLayoutChild(adapter, 1, unitLength + UNIT_GAP, t, (r - unitLength) - UNIT_GAP, b);
                obtainAndLayoutChild(adapter, 2, l, t, unitLength, b);
                return;
            case 4:
                obtainAndLayoutChild(adapter, 0, r - unitLength, t, r, b);
                right = (r - unitLength) - UNIT_GAP;
                obtainAndLayoutChild(adapter, 1, right - unitLength, t, right, b);
                left = unitLength + UNIT_GAP;
                obtainAndLayoutChild(adapter, 2, left, t, left + unitLength, b);
                obtainAndLayoutChild(adapter, 3, l, t, unitLength, b);
                return;
            case 5:
                obtainAndLayoutChild(adapter, 0, r - unitLength, t, r, b);
                right = (r - unitLength) - UNIT_GAP;
                obtainAndLayoutChild(adapter, 1, right - unitLength, t, right, b);
                obtainAndLayoutChild(adapter, 2, (UNIT_GAP + unitLength) * 2, t, r - ((UNIT_GAP + unitLength) * 2), b);
                left = unitLength + UNIT_GAP;
                obtainAndLayoutChild(adapter, 3, left, t, left + unitLength, b);
                obtainAndLayoutChild(adapter, 4, l, t, unitLength, b);
                return;
            default:
                GalleryLog.e("DiscoverItemView", "[land]there is too many children " + activeChildCount);
                return;
        }
    }

    public View getSelectedView() {
        return null;
    }

    public void setSelection(int position) {
    }

    public void setEntry(AlbumSet data) {
        this.mAdapter.setEntry(data);
    }

    public void setStyle(Style style) {
        this.mStyle = style;
    }

    public void setAdapter(DiscoverItemAdapter adapter) {
        if (this.mAdapter != null) {
            this.mAdapter.unregisterDataSetObserver(this.mDataSetObserver);
        }
        this.mAdapter = adapter;
        if (adapter != null) {
            adapter.registerDataSetObserver(this.mDataSetObserver);
        }
    }

    public DiscoverItemAdapter getAdapter() {
        return this.mAdapter;
    }

    private MyOnClickListener getListener(int position) {
        MyOnClickListener listener = this.mClickListeners[position];
        if (listener != null) {
            return listener;
        }
        listener = new MyOnClickListener(position);
        this.mClickListeners[position] = listener;
        return listener;
    }
}
