package com.android.gallery3d.ui;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.ui.SharedItemAdapter.ViewProvider;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.util.MyPrinter;

public class SharedItemView extends AdapterView<SharedItemAdapter> {
    private static boolean DEBUG = false;
    private static final MyPrinter LOG = new MyPrinter("SharedItemView");
    private static final int UNIT_GAP = GalleryUtils.dpToPixel(2);
    private SharedItemAdapter mAdapter;
    private SparseArray<View> mCachedChild;
    private DataSetObserver mDataSetObserver;
    private boolean mIsPhone;
    private SimpleOnClickListener[] mListeners;
    private WindowManager mWindowManager;

    private class SimpleOnClickListener implements OnClickListener, OnLongClickListener {
        int position;

        private SimpleOnClickListener(int pos) {
            this.position = pos;
        }

        public void onClick(View v) {
            SharedItemView.this.performItemClick(v, ((Integer) SharedItemView.this.mAdapter.getItem(this.position)).intValue(), SharedItemView.this.mAdapter.getItemId(this.position));
        }

        public boolean onLongClick(View v) {
            OnItemLongClickListener listener = SharedItemView.this.getOnItemLongClickListener();
            if (null != null || listener == null) {
                return false;
            }
            return listener.onItemLongClick(SharedItemView.this, v, ((Integer) SharedItemView.this.mAdapter.getItem(this.position)).intValue(), SharedItemView.this.mAdapter.getItemId(this.position));
        }
    }

    private static void debugd(String msg) {
        if (DEBUG) {
            LOG.d(msg);
        }
    }

    public SharedItemView(ViewProvider viewProvider, Context context) {
        boolean z;
        this(context, null);
        this.mAdapter = new SharedItemAdapter(viewProvider);
        this.mDataSetObserver = new PhotoShareDataSetObserver(this);
        this.mAdapter.registerDataSetObserver(this.mDataSetObserver);
        if (GalleryUtils.isTabletProduct(context)) {
            z = false;
        } else {
            z = true;
        }
        this.mIsPhone = z;
    }

    public SharedItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SharedItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mCachedChild = new SparseArray(2);
        this.mIsPhone = true;
        this.mListeners = new SimpleOnClickListener[2];
        this.mWindowManager = (WindowManager) context.getSystemService("window");
    }

    private void cache(int position, View view) {
        this.mCachedChild.put(position, view);
    }

    private int getActiveCount() {
        if (this.mAdapter != null) {
            return Utils.clamp(this.mAdapter.getCount(), 0, 2);
        }
        return 0;
    }

    private View getView(int position) {
        View ret = (View) this.mCachedChild.get(position);
        this.mCachedChild.remove(position);
        return ret;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getActiveCount() == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int height;
        DisplayMetrics metrics = new DisplayMetrics();
        this.mWindowManager.getDefaultDisplay().getRealMetrics(metrics);
        int displayWidth = metrics.widthPixels;
        int displayHeight = metrics.heightPixels;
        LOG.d(String.format("onLayout: displayWidth:%s, displayHeight:%s", new Object[]{Integer.valueOf(displayWidth), Integer.valueOf(displayHeight)}));
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (!this.mIsPhone || displayHeight <= displayWidth) {
            height = (width - (UNIT_GAP * 3)) / 4;
        } else {
            height = (width - UNIT_GAP) / 2;
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, 1073741824), MeasureSpec.makeMeasureSpec(height, 1073741824));
    }

    private View obtainAndLayoutChild(Adapter adapter, int index, int l, int t, int r, int b) {
        debugd(String.format("obtainAndLayoutChild: index:%s, left:%s, top:%s, right:%s, bottom:%s", new Object[]{Integer.valueOf(index), Integer.valueOf(l), Integer.valueOf(t), Integer.valueOf(r), Integer.valueOf(b)}));
        View child = getView(index);
        debugd("reuse view at " + index + " is " + child);
        if (adapter != null) {
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(r - l, 1073741824);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(b - t, 1073741824);
            View updated = adapter.getView(index, child, this);
            if (updated == null) {
                removeDetachedView(child, false);
                return null;
            }
            if (updated != child) {
                if (child != null) {
                    removeDetachedView(child, false);
                }
                addViewInLayout(updated, index, generateDefaultLayoutParams());
                updated.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                SimpleOnClickListener listener = getListener(index);
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

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        LOG.d(String.format("SharedItemView onLayout: left:%s, top:%s, right:%s, bottom:%s", new Object[]{Integer.valueOf(l), Integer.valueOf(t), Integer.valueOf(r), Integer.valueOf(b)}));
        int activeChildCount = getActiveCount();
        if (activeChildCount == 0) {
            super.onLayout(changed, l, t, r, b);
            return;
        }
        r -= l;
        b -= t;
        l = 0;
        invalidate();
        Adapter adapter = this.mAdapter;
        DisplayMetrics metrics = new DisplayMetrics();
        this.mWindowManager.getDefaultDisplay().getRealMetrics(metrics);
        int displayWidth = metrics.widthPixels;
        int displayHeight = metrics.heightPixels;
        int width = getMeasuredWidth();
        int childCount = getChildCount();
        for (int index = 0; index < childCount; index++) {
            cache(index, getChildAt(index));
        }
        detachAllViewsFromParent();
        if (!this.mIsPhone || displayHeight <= displayWidth) {
            int childWidth = (width - UNIT_GAP) / 2;
            switch (activeChildCount) {
                case 1:
                    if (isRtlLocale()) {
                        l = childWidth + UNIT_GAP;
                    } else {
                        r = childWidth;
                    }
                    obtainAndLayoutChild(adapter, 0, l, 0, r, b);
                    break;
                case 2:
                    if (!isRtlLocale()) {
                        obtainAndLayoutChild(adapter, 0, 0, 0, childWidth, b);
                        obtainAndLayoutChild(adapter, 1, childWidth + UNIT_GAP, 0, r, b);
                        break;
                    }
                    obtainAndLayoutChild(adapter, 0, childWidth + UNIT_GAP, 0, r, b);
                    obtainAndLayoutChild(adapter, 1, 0, 0, childWidth, b);
                    break;
                default:
                    GalleryLog.e("SharedItemView", "[land]there is too many children " + activeChildCount);
                    break;
            }
        } else if (activeChildCount == 1) {
            obtainAndLayoutChild(adapter, 0, 0, 0, r, b);
        } else {
            LOG.e("[port]there is too many children " + activeChildCount);
        }
        for (int i = activeChildCount; i < childCount; i++) {
            View child = getView(i);
            debugd("remove detached view " + i);
            removeDetachedView(child, false);
        }
        invalidate();
    }

    public View getSelectedView() {
        return null;
    }

    public void setSelection(int position) {
    }

    public void setAdapter(SharedItemAdapter adapter) {
        this.mAdapter = adapter;
    }

    public SharedItemAdapter getAdapter() {
        return this.mAdapter;
    }

    private SimpleOnClickListener getListener(int position) {
        SimpleOnClickListener listener = this.mListeners[position];
        if (listener != null) {
            return listener;
        }
        listener = new SimpleOnClickListener(position);
        this.mListeners[position] = listener;
        return listener;
    }
}
