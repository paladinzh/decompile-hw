package com.android.contacts.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListAdapter;

public class PinnedHeaderListView extends AutoScrollListView implements OnScrollListener, OnItemSelectedListener {
    private PinnedHeaderAdapter mAdapter;
    private int mAnimationDuration;
    private long mAnimationTargetTime;
    private RectF mBounds;
    private int mHeaderPaddingLeft;
    private int mHeaderWidth;
    private PinnedHeader[] mHeaders;
    protected boolean mIsAlphaIndexerEnabled;
    public boolean mIsHeaderScroll;
    private ListViewOperationListener mListViewOperationListener;
    private OnItemSelectedListener mOnItemSelectedListener;
    private OnScrollListener mOnScrollListener;
    private int mScrollState;
    private int mSize;

    public interface PinnedHeaderAdapter {
        void configurePinnedHeaders(PinnedHeaderListView pinnedHeaderListView);

        int getPinnedHeaderCount();

        View getPinnedHeaderView(int i, View view, ViewGroup viewGroup);

        int getScrollPositionForHeader(int i);
    }

    public interface ListViewOperationListener {
        boolean isMaxLimitReached(int i);

        void onMaxLimitReached();
    }

    private static final class PinnedHeader {
        int alpha;
        boolean animating;
        int height;
        int sourceY;
        int state;
        long targetTime;
        boolean targetVisible;
        int targetY;
        View view;
        boolean visible;
        int y;

        private PinnedHeader() {
        }
    }

    public PinnedHeaderListView(Context context) {
        this(context, null, 16842868);
    }

    public PinnedHeaderListView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842868);
    }

    public PinnedHeaderListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mBounds = new RectF();
        this.mAnimationDuration = 100;
        super.setCacheColorHint(getResources().getColor(17170445));
        super.setOnScrollListener(this);
        super.setOnItemSelectedListener(this);
        super.setFooterDividersEnabled(false);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        this.mHeaderPaddingLeft = getPaddingLeft();
        this.mHeaderWidth = ((r - l) - this.mHeaderPaddingLeft) - getPaddingRight();
        super.onLayout(changed, l, t, r, b);
    }

    public void setAdapter(ListAdapter adapter) {
        this.mAdapter = (PinnedHeaderAdapter) adapter;
        super.setAdapter(adapter);
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.mOnScrollListener = onScrollListener;
        super.setOnScrollListener(this);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.mOnItemSelectedListener = listener;
        super.setOnItemSelectedListener(this);
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        boolean z;
        if (this.mIsHeaderScroll) {
            z = false;
        } else {
            z = true;
        }
        if (z || !this.mIsAlphaIndexerEnabled) {
            if (this.mAdapter != null) {
                int count = this.mAdapter.getPinnedHeaderCount();
                if (count != this.mSize) {
                    this.mSize = count;
                    if (this.mHeaders == null) {
                        this.mHeaders = new PinnedHeader[this.mSize];
                    } else if (this.mHeaders.length < this.mSize) {
                        PinnedHeader[] headers = this.mHeaders;
                        this.mHeaders = new PinnedHeader[this.mSize];
                        System.arraycopy(headers, 0, this.mHeaders, 0, headers.length);
                    }
                }
                for (int i = 0; i < this.mSize; i++) {
                    if (this.mHeaders[i] == null) {
                        this.mHeaders[i] = new PinnedHeader();
                    }
                    this.mHeaders[i].view = this.mAdapter.getPinnedHeaderView(i, this.mHeaders[i].view, this);
                }
                this.mAnimationTargetTime = System.currentTimeMillis() + ((long) this.mAnimationDuration);
                this.mAdapter.configurePinnedHeaders(this);
                invalidateIfAnimating();
            }
            if (this.mOnScrollListener != null) {
                this.mOnScrollListener.onScroll(this, firstVisibleItem, visibleItemCount, totalItemCount);
            }
            this.mIsHeaderScroll = true;
            return;
        }
        this.mIsHeaderScroll = false;
        this.mSize = 0;
    }

    protected float getTopFadingEdgeStrength() {
        return this.mSize > 0 ? 0.0f : super.getTopFadingEdgeStrength();
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.mScrollState = scrollState;
        if (this.mOnScrollListener != null) {
            this.mOnScrollListener.onScrollStateChanged(this, scrollState);
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int windowTop = 0;
        int windowBottom = getHeight();
        for (int i = 0; i < this.mSize; i++) {
            PinnedHeader header = this.mHeaders[i];
            if (header.visible) {
                if (header.state == 0) {
                    windowTop = header.y + header.height;
                } else if (header.state == 1) {
                    windowBottom = header.y;
                    break;
                }
            }
        }
        View selectedView = getSelectedView();
        if (selectedView != null) {
            if (selectedView.getTop() < windowTop) {
                setSelectionFromTop(position, windowTop);
            } else if (selectedView.getBottom() > windowBottom) {
                setSelectionFromTop(position, windowBottom - selectedView.getHeight());
            }
        }
        if (this.mOnItemSelectedListener != null) {
            this.mOnItemSelectedListener.onItemSelected(parent, view, position, id);
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        if (this.mOnItemSelectedListener != null) {
            this.mOnItemSelectedListener.onNothingSelected(parent);
        }
    }

    public int getPinnedHeaderHeight(int viewIndex) {
        ensurePinnedHeaderLayout(viewIndex);
        return this.mHeaders[viewIndex].view.getHeight();
    }

    public void setHeaderPinnedAtTop(int viewIndex, int y, boolean animate) {
        ensurePinnedHeaderLayout(viewIndex);
        PinnedHeader header = this.mHeaders[viewIndex];
        header.visible = true;
        header.y = y;
        header.state = 0;
        header.animating = false;
    }

    public void setHeaderPinnedAtBottom(int viewIndex, int y, boolean animate) {
        ensurePinnedHeaderLayout(viewIndex);
        PinnedHeader header = this.mHeaders[viewIndex];
        header.state = 1;
        if (header.animating) {
            header.targetTime = this.mAnimationTargetTime;
            header.sourceY = header.y;
            header.targetY = y;
        } else if (!animate || (header.y == y && header.visible)) {
            header.visible = true;
            header.y = y;
        } else {
            if (header.visible) {
                header.sourceY = header.y;
            } else {
                header.visible = true;
                header.sourceY = header.height + y;
            }
            header.animating = true;
            header.targetVisible = true;
            header.targetTime = this.mAnimationTargetTime;
            header.targetY = y;
        }
    }

    public void setFadingHeader(int viewIndex, int position, boolean fade) {
        ensurePinnedHeaderLayout(viewIndex);
        View child = getChildAt(position - getFirstVisiblePosition());
        if (child != null) {
            PinnedHeader header = this.mHeaders[viewIndex];
            header.visible = true;
            header.state = 2;
            header.alpha = 255;
            header.animating = false;
            int top = getTotalTopPinnedHeaderHeight();
            header.y = top;
            if (fade) {
                int bottom = child.getBottom() - top;
                int headerHeight = header.height;
                if (bottom < headerHeight) {
                    int portion = bottom - headerHeight;
                    header.alpha = ((headerHeight + portion) * 255) / headerHeight;
                    header.y = top + portion;
                }
            }
        }
    }

    public void setHeaderInvisible(int viewIndex, boolean animate) {
        PinnedHeader header = this.mHeaders[viewIndex];
        if (header.visible && ((animate || header.animating) && header.state == 1)) {
            header.sourceY = header.y;
            if (!header.animating) {
                header.visible = true;
                header.targetY = getBottom() + header.height;
            }
            header.animating = true;
            header.targetTime = this.mAnimationTargetTime;
            header.targetVisible = false;
            return;
        }
        header.visible = false;
    }

    private void ensurePinnedHeaderLayout(int viewIndex) {
        View view = this.mHeaders[viewIndex].view;
        if (view.isLayoutRequested()) {
            int heightSpec;
            int widthSpec = MeasureSpec.makeMeasureSpec(this.mHeaderWidth, 1073741824);
            LayoutParams layoutParams = view.getLayoutParams();
            if (layoutParams == null || layoutParams.height <= 0) {
                heightSpec = MeasureSpec.makeMeasureSpec(0, 0);
            } else {
                heightSpec = MeasureSpec.makeMeasureSpec(layoutParams.height, 1073741824);
            }
            view.measure(widthSpec, heightSpec);
            int height = view.getMeasuredHeight();
            this.mHeaders[viewIndex].height = height;
            view.layout(0, 0, this.mHeaderWidth, height);
        }
    }

    public int getTotalTopPinnedHeaderHeight() {
        int i = this.mSize;
        while (true) {
            i--;
            if (i < 0) {
                return 0;
            }
            PinnedHeader header = this.mHeaders[i];
            if (header.visible && header.state == 0) {
                return header.y + header.height;
            }
        }
    }

    public int getPositionAt(int y) {
        do {
            int position = pointToPosition(getPaddingLeft() + 1, y);
            if (position != -1) {
                return position;
            }
            y--;
        } while (y > 0);
        return 0;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (this.mScrollState == 0) {
            int y = Float.valueOf(ev.getY()).intValue();
            int i = this.mSize;
            while (true) {
                i--;
                if (i < 0) {
                    break;
                }
                PinnedHeader header = this.mHeaders[i];
                if (header.visible && header.y <= y && header.y + header.height > y) {
                    break;
                }
            }
            if (ev.getAction() == 0) {
                return smoothScrollToPartition(i);
            }
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private boolean smoothScrollToPartition(int partition) {
        int position = this.mAdapter.getScrollPositionForHeader(partition);
        if (position == -1) {
            return false;
        }
        int offset = 0;
        for (int i = 0; i < partition; i++) {
            PinnedHeader header = this.mHeaders[i];
            if (header.visible) {
                offset += header.height;
            }
        }
        smoothScrollToPositionFromTop(getHeaderViewsCount() + position, offset);
        return true;
    }

    private void invalidateIfAnimating() {
        for (int i = 0; i < this.mSize; i++) {
            if (this.mHeaders[i].animating) {
                invalidate();
                return;
            }
        }
    }

    protected void dispatchDraw(Canvas canvas) {
        int top = 0;
        int bottom = getBottom();
        for (int i = 0; i < this.mSize; i++) {
            PinnedHeader header = this.mHeaders[i];
            if (header.visible) {
                if (header.state == 1 && header.y < bottom) {
                    bottom = header.y;
                } else if (header.state == 0 || header.state == 2) {
                    int newTop = header.y + header.height;
                    if (newTop > top) {
                        top = newTop;
                    }
                }
            }
        }
        super.dispatchDraw(canvas);
        invalidateIfAnimating();
    }

    public void setListViewOperationListener(ListViewOperationListener aListener) {
        this.mListViewOperationListener = aListener;
    }

    public boolean performItemClick(View view, int position, long id) {
        if (getChoiceMode() != 2 || this.mListViewOperationListener == null || !this.mListViewOperationListener.isMaxLimitReached(position)) {
            return super.performItemClick(view, position, id);
        }
        this.mListViewOperationListener.onMaxLimitReached();
        return true;
    }
}
