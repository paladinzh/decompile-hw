package com.android.deskclock.drag;

import android.graphics.Point;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;

public class DragSortController extends SimpleFloatViewManager implements OnTouchListener, OnGestureListener {
    private boolean mCanDrag;
    private int mClickRemoveHitPos = -1;
    private int mClickRemoveId;
    private int mCurrX;
    private int mCurrY;
    private GestureDetector mDetector;
    private int mDragHandleId;
    private int mDragInitMode = 0;
    private boolean mDragging = false;
    private DragSortListView mDslv;
    private int mFlingHandleId;
    private int mFlingHitPos = -1;
    private GestureDetector mFlingRemoveDetector;
    private OnGestureListener mFlingRemoveListener = new SimpleOnGestureListener() {
        public final boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (DragSortController.this.mRemoveEnabled && DragSortController.this.mIsRemoving) {
                int minPos = DragSortController.this.mDslv.getWidth() / 5;
                if (velocityX > DragSortController.this.mFlingSpeed) {
                    if (DragSortController.this.mPositionX > (-minPos)) {
                        DragSortController.this.mDslv.stopDragWithVelocity(true, velocityX);
                    }
                } else if (velocityX < (-DragSortController.this.mFlingSpeed) && DragSortController.this.mPositionX < minPos) {
                    DragSortController.this.mDslv.stopDragWithVelocity(true, velocityX);
                }
                DragSortController.this.mIsRemoving = false;
            }
            return false;
        }
    };
    private float mFlingSpeed = 500.0f;
    private int mHitPos = -1;
    private boolean mIsRemoving = false;
    private int mItemX;
    private int mItemY;
    private int mPositionX;
    private boolean mRemoveEnabled = false;
    private int mRemoveMode;
    private boolean mSortEnabled = true;
    private int[] mTempLoc = new int[2];
    private int mTouchSlop;

    public DragSortController(DragSortListView dslv, int dragHandleId, int dragInitMode, int removeMode, int clickRemoveId, int flingHandleId) {
        super(dslv);
        this.mDslv = dslv;
        this.mDetector = new GestureDetector(dslv.getContext(), this);
        this.mFlingRemoveDetector = new GestureDetector(dslv.getContext(), this.mFlingRemoveListener);
        this.mFlingRemoveDetector.setIsLongpressEnabled(false);
        this.mTouchSlop = ViewConfiguration.get(dslv.getContext()).getScaledTouchSlop();
        this.mDragHandleId = dragHandleId;
        this.mClickRemoveId = clickRemoveId;
        this.mFlingHandleId = flingHandleId;
        setRemoveMode(removeMode);
        setDragInitMode(dragInitMode);
    }

    public void setDragInitMode(int mode) {
        this.mDragInitMode = mode;
    }

    public void setSortEnabled(boolean enabled) {
        this.mSortEnabled = enabled;
    }

    public void setRemoveMode(int mode) {
        this.mRemoveMode = mode;
    }

    public void setRemoveEnabled(boolean enabled) {
        this.mRemoveEnabled = enabled;
    }

    public boolean startDrag(int position, int deltaX, int deltaY) {
        int dragFlags = 0;
        if (this.mSortEnabled && !this.mIsRemoving) {
            dragFlags = 12;
        }
        if (this.mRemoveEnabled && this.mIsRemoving) {
            dragFlags = (dragFlags | 1) | 2;
        }
        this.mDragging = this.mDslv.startDrag(position - this.mDslv.getHeaderViewsCount(), dragFlags, deltaX, deltaY);
        return this.mDragging;
    }

    public boolean onTouch(View v, MotionEvent ev) {
        if (!this.mDslv.isDragEnabled() || this.mDslv.listViewIntercepted()) {
            return false;
        }
        this.mDetector.onTouchEvent(ev);
        if (this.mRemoveEnabled && this.mDragging && this.mRemoveMode == 1) {
            this.mFlingRemoveDetector.onTouchEvent(ev);
        }
        switch (ev.getAction() & 255) {
            case 0:
                this.mCurrX = (int) ev.getX();
                this.mCurrY = (int) ev.getY();
                break;
            case 1:
                if (this.mRemoveEnabled && this.mIsRemoving) {
                    if ((this.mPositionX >= 0 ? this.mPositionX : -this.mPositionX) > this.mDslv.getWidth() / 2) {
                        this.mDslv.stopDragWithVelocity(true, 0.0f);
                    }
                }
                this.mIsRemoving = false;
                this.mDragging = false;
                break;
            case 3:
                this.mIsRemoving = false;
                this.mDragging = false;
                break;
        }
        return false;
    }

    public void onDragFloatView(View floatView, Point position, Point touch) {
        if (this.mRemoveEnabled && this.mIsRemoving) {
            this.mPositionX = position.x;
        }
    }

    public int startDragPosition(MotionEvent ev) {
        return dragHandleHitPosition(ev);
    }

    public int startFlingPosition(MotionEvent ev) {
        return this.mRemoveMode == 1 ? flingHandleHitPosition(ev) : -1;
    }

    public int dragHandleHitPosition(MotionEvent ev) {
        return viewIdHitPosition(ev, this.mDragHandleId);
    }

    public int flingHandleHitPosition(MotionEvent ev) {
        return viewIdHitPosition(ev, this.mFlingHandleId);
    }

    public int viewIdHitPosition(MotionEvent ev, int id) {
        int touchPos = this.mDslv.pointToPosition((int) ev.getX(), (int) ev.getY());
        int numHeaders = this.mDslv.getHeaderViewsCount();
        int numFooters = this.mDslv.getFooterViewsCount();
        int count = this.mDslv.getCount();
        if (touchPos != -1 && touchPos >= numHeaders && touchPos < count - numFooters) {
            View item = this.mDslv.getChildAt(touchPos - this.mDslv.getFirstVisiblePosition());
            int rawX = (int) ev.getRawX();
            int rawY = (int) ev.getRawY();
            View dragBox = id == 0 ? item : item.findViewById(id);
            if (dragBox != null) {
                dragBox.getLocationOnScreen(this.mTempLoc);
                if (rawX > this.mTempLoc[0] && rawY > this.mTempLoc[1] && rawX < this.mTempLoc[0] + dragBox.getWidth() && rawY < this.mTempLoc[1] + dragBox.getHeight()) {
                    this.mItemX = item.getLeft();
                    this.mItemY = item.getTop();
                    return touchPos;
                }
            }
        }
        return -1;
    }

    public boolean onDown(MotionEvent ev) {
        if (this.mRemoveEnabled && this.mRemoveMode == 0) {
            this.mClickRemoveHitPos = viewIdHitPosition(ev, this.mClickRemoveId);
        }
        this.mHitPos = startDragPosition(ev);
        if (this.mHitPos != -1 && this.mDragInitMode == 0) {
            startDrag(this.mHitPos, ((int) ev.getX()) - this.mItemX, ((int) ev.getY()) - this.mItemY);
        }
        this.mIsRemoving = false;
        this.mCanDrag = true;
        this.mPositionX = 0;
        this.mFlingHitPos = startFlingPosition(ev);
        return true;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (e1 == null || e2 == null) {
            return false;
        }
        int x1 = (int) e1.getX();
        int y1 = (int) e1.getY();
        int x2 = (int) e2.getX();
        int y2 = (int) e2.getY();
        int deltaX = x2 - this.mItemX;
        int deltaY = y2 - this.mItemY;
        if (this.mCanDrag && !this.mDragging) {
            if (this.mHitPos != -1) {
                isStartDrag(x1, x2, y1, y2, deltaX, deltaY);
            } else if (this.mFlingHitPos != -1) {
                if (Math.abs(x2 - x1) > this.mTouchSlop && this.mRemoveEnabled) {
                    this.mIsRemoving = true;
                    startDrag(this.mFlingHitPos, deltaX, deltaY);
                } else if (Math.abs(y2 - y1) > this.mTouchSlop) {
                    this.mCanDrag = false;
                }
            }
        }
        return false;
    }

    private void isStartDrag(int x1, int x2, int y1, int y2, int deltaX, int deltaY) {
        if (this.mDragInitMode == 1 && Math.abs(y2 - y1) > this.mTouchSlop && this.mSortEnabled) {
            startDrag(this.mHitPos, deltaX, deltaY);
        } else if (this.mDragInitMode != 0 && Math.abs(x2 - x1) > this.mTouchSlop && this.mRemoveEnabled) {
            this.mIsRemoving = true;
            startDrag(this.mFlingHitPos, deltaX, deltaY);
        }
    }

    public void onLongPress(MotionEvent e) {
        if (this.mHitPos != -1 && this.mDragInitMode == 2) {
            this.mDslv.performHapticFeedback(0);
            startDrag(this.mHitPos, this.mCurrX - this.mItemX, this.mCurrY - this.mItemY);
        }
    }

    public final boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    public boolean onSingleTapUp(MotionEvent ev) {
        if (this.mRemoveEnabled && this.mRemoveMode == 0 && this.mClickRemoveHitPos != -1) {
            this.mDslv.removeItem(this.mClickRemoveHitPos - this.mDslv.getHeaderViewsCount());
        }
        return true;
    }

    public void onShowPress(MotionEvent ev) {
    }
}
