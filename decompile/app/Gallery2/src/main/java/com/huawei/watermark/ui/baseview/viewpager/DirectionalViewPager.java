package com.huawei.watermark.ui.baseview.viewpager;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Scroller;
import com.huawei.watermark.ui.baseview.viewpager.WMViewPager.OnPageChangeListener;
import java.util.ArrayList;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class DirectionalViewPager extends WMViewPager {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    private int mActivePointerId = -1;
    private WMBasePagerAdapter mAdapter;
    private int mChildHeightMeasureSpec;
    private int mChildWidthMeasureSpec;
    private int mCurItem;
    private boolean mInLayout;
    private float mInitialMotion;
    private boolean mIsBeingDragged;
    private boolean mIsUnableToDrag;
    private final ArrayList<ItemInfo> mItems = new ArrayList();
    private float mLastMotionX;
    private float mLastMotionY;
    private int mMaximumVelocity;
    private int mMinimumVelocity;
    private com.huawei.watermark.ui.baseview.viewpager.VerticalViewPagerCompat.DataSetObserver mObserver;
    private OnPageChangeListener mOnPageChangeListener;
    private int mOrientation = 0;
    private boolean mPopulatePending;
    private int mRestoredCurItem = -1;
    private int mScrollState = 0;
    private Scroller mScroller;
    private int mScrollerDuration = SmsCheckResult.ESCT_200;
    private boolean mScrolling;
    private boolean mScrollingCacheEnabled;
    private int mTouchSlop;
    private VelocityTracker mVelocityTracker;

    private class DataSetObserver implements com.huawei.watermark.ui.baseview.viewpager.VerticalViewPagerCompat.DataSetObserver {
        private DataSetObserver() {
        }

        public void onDataSetChanged() {
            DirectionalViewPager.this.dataSetChanged();
        }
    }

    private class FixedSpeedScroller extends Scroller {
        public FixedSpeedScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, DirectionalViewPager.this.mScrollerDuration);
        }

        public void startScroll(int startX, int startY, int dx, int dy) {
            super.startScroll(startX, startY, dx, dy, DirectionalViewPager.this.mScrollerDuration);
        }
    }

    public static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {
            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in, loader);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        });
        Parcelable adapterState;
        ClassLoader loader;
        int position;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.position);
            out.writeParcelable(this.adapterState, flags);
        }

        public String toString() {
            return "FragmentPager.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " position=" + this.position + "}";
        }

        private static Parcelable readParentState(Parcel parcel, ClassLoader loader) {
            if (loader == null) {
                loader = SavedState.class.getClassLoader();
            }
            Parcelable state = parcel.readParcelable(loader);
            return state != null ? state : EMPTY_STATE;
        }

        SavedState(Parcel in, ClassLoader loader) {
            super(readParentState(in, loader));
            if (loader == null) {
                loader = getClass().getClassLoader();
            }
            this.position = in.readInt();
            this.adapterState = in.readParcelable(loader);
            this.loader = loader;
        }
    }

    public DirectionalViewPager(Context context) {
        super(context);
    }

    public DirectionalViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        int orientation = attrs.getAttributeIntValue("http://schemas.android.com/apk/res/android", "orientation", -1);
        if (orientation != -1) {
            setOrientation(orientation);
        }
    }

    void initViewPager() {
        setWillNotDraw(false);
        this.mScroller = new FixedSpeedScroller(getContext(), new DecelerateInterpolator(0.5f));
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        this.mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
        this.mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        this.mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    private void setScrollState(int newState) {
        if (this.mScrollState != newState) {
            this.mScrollState = newState;
            if (this.mOnPageChangeListener != null) {
                this.mOnPageChangeListener.onPageScrollStateChanged(newState);
            }
        }
    }

    public void setAdapter(WMBasePagerAdapter adapter) {
        if (this.mAdapter != null) {
            VerticalViewPagerCompat.setDataSetObserver(this.mAdapter, null);
        }
        this.mAdapter = adapter;
        if (this.mAdapter != null) {
            if (this.mObserver == null) {
                this.mObserver = new DataSetObserver();
            }
            VerticalViewPagerCompat.setDataSetObserver(this.mAdapter, this.mObserver);
            this.mPopulatePending = false;
            if (this.mRestoredCurItem >= 0) {
                setCurrentItemInternal(this.mRestoredCurItem, false, true);
                this.mRestoredCurItem = -1;
                return;
            }
            populate();
        }
    }

    public WMBasePagerAdapter getAdapter() {
        return this.mAdapter;
    }

    public void setCurrentItem(int item) {
        this.mPopulatePending = false;
        setCurrentItemInternal(item, true, false);
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        this.mPopulatePending = false;
        setCurrentItemInternal(item, smoothScroll, false);
    }

    void setCurrentItemInternal(int item, boolean smoothScroll, boolean always) {
        if (this.mAdapter == null || this.mAdapter.getCount() <= 0) {
            setScrollingCacheEnabled(false);
        } else if (always || this.mCurItem != item || this.mItems.size() == 0) {
            item = getItem(item);
            if (item > this.mCurItem + 1 || item < this.mCurItem - 1) {
                for (int i = 0; i < this.mItems.size(); i++) {
                    ((ItemInfo) this.mItems.get(i)).scrolling = true;
                }
            }
            boolean dispatchSelected = this.mCurItem != item;
            this.mCurItem = item;
            populate();
            if (smoothScroll) {
                getSmoothScrollPosition(item);
                if (dispatchSelected && this.mOnPageChangeListener != null) {
                    this.mOnPageChangeListener.onPageSelected(item);
                }
            } else {
                if (dispatchSelected && this.mOnPageChangeListener != null) {
                    this.mOnPageChangeListener.onPageSelected(item);
                }
                completeScroll();
                getScrollPosition(item);
            }
        } else {
            setScrollingCacheEnabled(false);
        }
    }

    private int getItem(int item) {
        if (item < 0) {
            return 0;
        }
        if (item >= this.mAdapter.getCount()) {
            return this.mAdapter.getCount() - 1;
        }
        return item;
    }

    private void getScrollPosition(int item) {
        if (this.mOrientation == 0) {
            scrollTo(getWidth() * item, 0);
        } else {
            scrollTo(0, getHeight() * item);
        }
    }

    private void getSmoothScrollPosition(int item) {
        if (this.mOrientation == 0) {
            smoothScrollTo(getWidth() * item, 0);
        } else {
            smoothScrollTo(0, getHeight() * item);
        }
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.mOnPageChangeListener = listener;
    }

    public OnPageChangeListener getOnPageChangeListener() {
        return this.mOnPageChangeListener;
    }

    void smoothScrollTo(int x, int y) {
        if (getChildCount() == 0) {
            setScrollingCacheEnabled(false);
            return;
        }
        int sx = getScrollX();
        int sy = getScrollY();
        int dx = x - sx;
        int dy = y - sy;
        if (dx == 0 && dy == 0) {
            completeScroll();
            return;
        }
        setScrollingCacheEnabled(true);
        this.mScrolling = true;
        setScrollState(2);
        this.mScroller.startScroll(sx, sy, dx, dy);
        invalidate();
    }

    void addNewItem(int position, int index) {
        ItemInfo ii = new ItemInfo();
        ii.position = position;
        ii.object = this.mAdapter.instantiateItem(this, position);
        if (index < 0) {
            this.mItems.add(ii);
        } else {
            this.mItems.add(index, ii);
        }
    }

    void dataSetChanged() {
        boolean needPopulate = this.mItems.isEmpty() && this.mAdapter.getCount() > 0;
        int newCurrItem = -1;
        int i = 0;
        while (i < this.mItems.size()) {
            ItemInfo ii = (ItemInfo) this.mItems.get(i);
            int newPos = this.mAdapter.getItemPosition(ii.object);
            if (newPos != -1) {
                if (newPos == -2) {
                    this.mItems.remove(i);
                    i--;
                    this.mAdapter.destroyItem(this, ii.position, ii.object);
                    needPopulate = true;
                    if (this.mCurItem == ii.position) {
                        newCurrItem = Math.max(0, Math.min(this.mCurItem, this.mAdapter.getCount() - 1));
                    }
                } else if (ii.position != newPos) {
                    if (ii.position == this.mCurItem) {
                        newCurrItem = newPos;
                    }
                    ii.position = newPos;
                    needPopulate = true;
                }
            }
            i++;
        }
        if (newCurrItem >= 0) {
            setCurrentItemInternal(newCurrItem, false, true);
            needPopulate = true;
        }
        if (needPopulate) {
            populate();
            requestLayout();
        }
    }

    void populate() {
        if (this.mAdapter != null && !this.mPopulatePending && getWindowToken() != null) {
            int startPos = this.mCurItem > 0 ? this.mCurItem - 1 : this.mCurItem;
            int count = this.mAdapter.getCount();
            int endPos = this.mCurItem < count + -1 ? this.mCurItem + 1 : count - 1;
            getLastPosByAddAndRemovePages(startPos, endPos, -1);
            getLastPosByAddNewPages(startPos, endPos);
        }
    }

    private void getLastPosByAddNewPages(int startPos, int endPos) {
        int lastPos;
        if (this.mItems.size() > 0) {
            lastPos = ((ItemInfo) this.mItems.get(this.mItems.size() - 1)).position;
        } else {
            lastPos = -1;
        }
        if (lastPos < endPos) {
            lastPos++;
            if (lastPos <= startPos) {
                lastPos = startPos;
            }
            while (lastPos <= endPos) {
                addNewItem(lastPos, -1);
                lastPos++;
            }
        }
    }

    private void getLastPosByAddAndRemovePages(int startPos, int endPos, int lastPos) {
        int i = 0;
        while (i < this.mItems.size()) {
            ItemInfo ii = (ItemInfo) this.mItems.get(i);
            if ((ii.position < startPos || ii.position > endPos) && !ii.scrolling) {
                this.mItems.remove(i);
                i--;
                this.mAdapter.destroyItem(this, ii.position, ii.object);
            } else if (lastPos < endPos && ii.position > startPos) {
                lastPos++;
                if (lastPos < startPos) {
                    lastPos = startPos;
                }
                while (lastPos <= endPos && lastPos < ii.position) {
                    addNewItem(lastPos, i);
                    lastPos++;
                    i++;
                }
            }
            lastPos = ii.position;
            i++;
        }
    }

    public int getCurrentItem() {
        return this.mCurItem;
    }

    public Parcelable onSaveInstanceState() {
        try {
            SavedState ss = new SavedState(super.onSaveInstanceState());
            ss.position = this.mCurItem;
            if (this.mAdapter != null) {
                ss.adapterState = this.mAdapter.saveState();
            }
            return ss;
        } catch (Exception e) {
            Log.e("DirectionalViewPager", "onSaveInstanceState exception." + e.getMessage());
            return null;
        }
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState ss = (SavedState) state;
            super.onRestoreInstanceState(ss.getSuperState());
            if (this.mAdapter != null) {
                setCurrentItemInternal(ss.position, false, true);
            } else {
                this.mRestoredCurItem = ss.position;
            }
            return;
        }
        super.onRestoreInstanceState(state);
    }

    public int getOrientation() {
        return this.mOrientation;
    }

    public void setOrientation(int orientation) {
        switch (orientation) {
            case 0:
            case 1:
                if (orientation != this.mOrientation) {
                    completeScroll();
                    this.mInitialMotion = 0.0f;
                    this.mLastMotionX = 0.0f;
                    this.mLastMotionY = 0.0f;
                    if (this.mVelocityTracker != null) {
                        this.mVelocityTracker.clear();
                    }
                    this.mOrientation = orientation;
                    if (this.mOrientation == 0) {
                        scrollTo(this.mCurItem * getWidth(), 0);
                    } else {
                        scrollTo(0, this.mCurItem * getHeight());
                    }
                    requestLayout();
                    return;
                }
                return;
            default:
                throw new IllegalArgumentException("Only HORIZONTAL and VERTICAL are valid orientations.");
        }
    }

    public void addView(View child, int index, LayoutParams params) {
        if (this.mInLayout) {
            addViewInLayout(child, index, params);
            child.measure(this.mChildWidthMeasureSpec, this.mChildHeightMeasureSpec);
            return;
        }
        super.addView(child, index, params);
    }

    ItemInfo infoForChild(View child) {
        if (this.mAdapter == null) {
            return null;
        }
        for (int i = 0; i < this.mItems.size(); i++) {
            ItemInfo ii = (ItemInfo) this.mItems.get(i);
            if (this.mAdapter.isViewFromObject(child, ii.object)) {
                return ii;
            }
        }
        return null;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mAdapter != null) {
            populate();
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
        this.mChildWidthMeasureSpec = MeasureSpec.makeMeasureSpec((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight(), 1073741824);
        this.mChildHeightMeasureSpec = MeasureSpec.makeMeasureSpec((getMeasuredHeight() - getPaddingTop()) - getPaddingBottom(), 1073741824);
        this.mInLayout = true;
        populate();
        this.mInLayout = false;
        int size = getChildCount();
        for (int i = 0; i < size; i++) {
            View child = getChildAt(i);
            if (!(child == null || child.getVisibility() == 8)) {
                child.measure(this.mChildWidthMeasureSpec, this.mChildHeightMeasureSpec);
            }
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int scrollPos;
        if (this.mOrientation == 0) {
            scrollPos = this.mCurItem * w;
            if (scrollPos != getScrollX()) {
                completeScroll();
                scrollTo(scrollPos, getScrollY());
                return;
            }
            return;
        }
        scrollPos = this.mCurItem * h;
        if (scrollPos != getScrollY()) {
            completeScroll();
            scrollTo(getScrollX(), scrollPos);
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        this.mInLayout = true;
        populate();
        this.mInLayout = false;
        int count = getChildCount();
        int size = this.mOrientation == 0 ? r - l : b - t;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                ItemInfo ii = infoForChild(child);
                if (ii != null) {
                    int off = size * ii.position;
                    int childLeft = getPaddingLeft();
                    int childTop = getPaddingTop();
                    if (this.mOrientation == 0) {
                        childLeft += off;
                    } else {
                        childTop += off;
                    }
                    child.layout(childLeft, childTop, child.getMeasuredWidth() + childLeft, child.getMeasuredHeight() + childTop);
                }
            }
        }
    }

    public void computeScroll() {
        if (this.mScroller.isFinished() || !this.mScroller.computeScrollOffset()) {
            completeScroll();
            return;
        }
        int oldX = getScrollX();
        int oldY = getScrollY();
        int x = this.mScroller.getCurrX();
        int y = this.mScroller.getCurrY();
        if (!(oldX == x && oldY == y)) {
            scrollTo(x, y);
        }
        if (this.mOnPageChangeListener != null) {
            int size;
            int value;
            if (this.mOrientation == 0) {
                size = getWidth();
                value = x;
            } else {
                size = getHeight();
                value = y;
            }
            int offsetPixels = value % size;
            this.mOnPageChangeListener.onPageScrolled(value / size, ((float) offsetPixels) / ((float) size), offsetPixels);
        }
        invalidate();
    }

    private void completeScroll() {
        boolean needPopulate = this.mScrolling;
        if (needPopulate) {
            setScrollingCacheEnabled(false);
            this.mScroller.abortAnimation();
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = this.mScroller.getCurrX();
            int y = this.mScroller.getCurrY();
            if (!(oldX == x && oldY == y)) {
                scrollTo(x, y);
            }
            setScrollState(0);
        }
        this.mPopulatePending = false;
        this.mScrolling = false;
        for (int i = 0; i < this.mItems.size(); i++) {
            ItemInfo ii = (ItemInfo) this.mItems.get(i);
            if (ii.scrolling) {
                needPopulate = true;
                ii.scrolling = false;
            }
        }
        if (needPopulate) {
            populate();
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction() & 255;
        if (action == 3 || action == 1) {
            releaseDrag();
            return false;
        }
        if (action != 0) {
            if (this.mIsBeingDragged) {
                return true;
            }
            if (this.mIsUnableToDrag) {
                return false;
            }
        }
        switch (action) {
            case 0:
                getLastMotionXY(ev);
                this.mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                if (this.mScrollState != 2) {
                    completeScroll();
                    this.mIsBeingDragged = false;
                    this.mIsUnableToDrag = false;
                    break;
                }
                this.mIsBeingDragged = true;
                this.mIsUnableToDrag = false;
                setScrollState(1);
                break;
            case 2:
                if (this.mActivePointerId != -1 || VERSION.SDK_INT <= 4) {
                    float primaryDiff;
                    float secondaryDiff;
                    float x = MotionEventCompat.getX(ev, 0);
                    float y = MotionEventCompat.getY(ev, 0);
                    float xDiff = Math.abs(x - this.mLastMotionX);
                    float yDiff = Math.abs(y - this.mLastMotionY);
                    if (this.mOrientation == 0) {
                        primaryDiff = xDiff;
                        secondaryDiff = yDiff;
                    } else {
                        primaryDiff = yDiff;
                        secondaryDiff = xDiff;
                    }
                    ifStartDrag(x, y, primaryDiff, secondaryDiff);
                    break;
                }
            case 6:
                onSecondaryPointerUp(ev);
                break;
        }
        return this.mIsBeingDragged;
    }

    private void releaseDrag() {
        this.mIsBeingDragged = false;
        this.mIsUnableToDrag = false;
        this.mActivePointerId = -1;
    }

    private void getLastMotionXY(MotionEvent ev) {
        if (this.mOrientation == 0) {
            float x = ev.getX();
            this.mInitialMotion = x;
            this.mLastMotionX = x;
            this.mLastMotionY = ev.getY();
            return;
        }
        this.mLastMotionX = ev.getX();
        x = ev.getY();
        this.mInitialMotion = x;
        this.mLastMotionY = x;
    }

    private void ifStartDrag(float x, float y, float primaryDiff, float secondaryDiff) {
        if (primaryDiff > ((float) this.mTouchSlop) && primaryDiff > secondaryDiff) {
            this.mIsBeingDragged = true;
            setScrollState(1);
            if (this.mOrientation == 0) {
                this.mLastMotionX = x;
            } else {
                this.mLastMotionY = y;
            }
            setScrollingCacheEnabled(true);
        } else if (secondaryDiff > ((float) this.mTouchSlop)) {
            this.mIsUnableToDrag = true;
        }
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if ((ev.getAction() == 0 && ev.getEdgeFlags() != 0) || this.mAdapter == null || this.mAdapter.getCount() == 0) {
            return false;
        }
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(ev);
        switch (ev.getAction() & 255) {
            case 0:
                actionDown(ev);
                break;
            case 1:
                actionUp();
                break;
            case 2:
                actionMove(ev);
                break;
            case 3:
                actionCancel();
                break;
            case 5:
                actionPointerDown(ev);
                break;
            case 6:
                actionPointerUp(ev);
                break;
        }
        return true;
    }

    private void actionPointerUp(MotionEvent ev) {
        onSecondaryPointerUp(ev);
        int index = MotionEventCompat.findPointerIndex(ev, this.mActivePointerId);
        if (this.mOrientation == 0) {
            this.mLastMotionX = MotionEventCompat.getX(ev, index);
        } else {
            this.mLastMotionY = MotionEventCompat.getY(ev, index);
        }
    }

    private void actionPointerDown(MotionEvent ev) {
        int index = MotionEventCompat.getActionIndex(ev);
        if (this.mOrientation == 0) {
            this.mLastMotionX = MotionEventCompat.getX(ev, index);
        } else {
            this.mLastMotionY = MotionEventCompat.getY(ev, index);
        }
        this.mActivePointerId = MotionEventCompat.getPointerId(ev, index);
    }

    private void actionCancel() {
        if (this.mIsBeingDragged) {
            setCurrentItemInternal(this.mCurItem, true, true);
            this.mActivePointerId = -1;
            endDrag();
        }
    }

    private void actionUp() {
        if (this.mIsBeingDragged) {
            int initialVelocity;
            float lastMotion;
            VelocityTracker velocityTracker = this.mVelocityTracker;
            velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
            int sizeOverThree;
            if (this.mOrientation == 0) {
                initialVelocity = (int) VelocityTrackerCompat.getXVelocity(velocityTracker, this.mActivePointerId);
                lastMotion = this.mLastMotionX;
                sizeOverThree = getWidth() / 3;
            } else {
                initialVelocity = (int) VelocityTrackerCompat.getYVelocity(velocityTracker, this.mActivePointerId);
                lastMotion = this.mLastMotionY;
                sizeOverThree = getHeight() / 3;
            }
            this.mPopulatePending = true;
            if (Math.abs(initialVelocity) <= this.mMinimumVelocity && Math.abs(this.mInitialMotion - lastMotion) < ((float) sizeOverThree)) {
                setCurrentItemInternal(this.mCurItem, true, true);
            } else if (lastMotion > this.mInitialMotion) {
                setCurrentItemInternal(this.mCurItem - 1, true, true);
            } else {
                setCurrentItemInternal(this.mCurItem + 1, true, true);
            }
            this.mActivePointerId = -1;
            endDrag();
        }
    }

    private void actionMove(MotionEvent ev) {
        if (!this.mIsBeingDragged) {
            isNotBeingDragged(ev);
        }
        if (this.mIsBeingDragged) {
            isBeingDragged(ev);
        }
    }

    private void isBeingDragged(MotionEvent ev) {
        int size;
        float scroll;
        float x = MotionEventCompat.getX(ev, 0);
        float y = MotionEventCompat.getY(ev, 0);
        if (this.mOrientation == 0) {
            size = getWidth();
            scroll = ((float) getScrollX()) + (this.mLastMotionX - x);
            this.mLastMotionX = x;
        } else {
            size = getHeight();
            scroll = ((float) getScrollY()) + (this.mLastMotionY - y);
            this.mLastMotionY = y;
        }
        float lowerBound = (float) Math.max(0, (this.mCurItem - 1) * size);
        float upperBound = (float) (Math.min(this.mCurItem + 1, this.mAdapter.getCount() - 1) * size);
        if (scroll < lowerBound) {
            scroll = lowerBound;
        } else if (scroll > upperBound) {
            scroll = upperBound;
        }
        if (this.mOrientation == 0) {
            this.mLastMotionX += scroll - ((float) ((int) scroll));
            scrollTo((int) scroll, getScrollY());
        } else {
            this.mLastMotionY += scroll - ((float) ((int) scroll));
            scrollTo(getScrollX(), (int) scroll);
        }
        if (this.mOnPageChangeListener != null) {
            int positionOffsetPixels = ((int) scroll) % size;
            this.mOnPageChangeListener.onPageScrolled(((int) scroll) / size, ((float) positionOffsetPixels) / ((float) size), positionOffsetPixels);
        }
    }

    private void isNotBeingDragged(MotionEvent ev) {
        float primaryDiff;
        float x = MotionEventCompat.getX(ev, 0);
        float y = MotionEventCompat.getY(ev, 0);
        float xDiff = Math.abs(x - this.mLastMotionX);
        float yDiff = Math.abs(y - this.mLastMotionY);
        float secondaryDiff;
        if (this.mOrientation == 0) {
            primaryDiff = xDiff;
            secondaryDiff = yDiff;
        } else {
            primaryDiff = yDiff;
            secondaryDiff = xDiff;
        }
        if (primaryDiff > ((float) this.mTouchSlop) && primaryDiff > secondaryDiff) {
            this.mIsBeingDragged = true;
            if (this.mOrientation == 0) {
                this.mLastMotionX = x;
            } else {
                this.mLastMotionY = y;
            }
            setScrollState(1);
            setScrollingCacheEnabled(true);
        }
    }

    private void actionDown(MotionEvent ev) {
        completeScroll();
        float x;
        if (this.mOrientation == 0) {
            x = ev.getX();
            this.mInitialMotion = x;
            this.mLastMotionX = x;
        } else {
            x = ev.getY();
            this.mInitialMotion = x;
            this.mLastMotionY = x;
        }
        this.mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int pointerIndex = MotionEventCompat.getActionIndex(ev);
        if (MotionEventCompat.getPointerId(ev, pointerIndex) == this.mActivePointerId) {
            int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            if (this.mOrientation == 0) {
                this.mLastMotionX = MotionEventCompat.getX(ev, newPointerIndex);
            } else {
                this.mLastMotionY = MotionEventCompat.getY(ev, newPointerIndex);
            }
            this.mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
            if (this.mVelocityTracker != null) {
                this.mVelocityTracker.clear();
            }
        }
    }

    private void endDrag() {
        this.mIsBeingDragged = false;
        this.mIsUnableToDrag = false;
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private void setScrollingCacheEnabled(boolean enabled) {
        if (this.mScrollingCacheEnabled != enabled) {
            this.mScrollingCacheEnabled = enabled;
        }
    }

    public void setScrollerDuration(int mScrollerDuration) {
        this.mScrollerDuration = mScrollerDuration;
    }
}
