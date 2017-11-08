package com.huawei.mms.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import com.huawei.cspcommon.MLog;

public class EmuiListView_V3 extends MultiModeListView {
    private float SCROLL_FRICTION = 0.0075f;
    private float SCROLL_VELOCITYSCALE = 0.65f;
    private boolean mActionBarDragCalled;
    private ListViewDragoutListener mActionBarDragListener = null;
    boolean mBlockLayoutList = false;
    private float mFirstMotionX;
    private float mFirstMotionY;
    private HandleTouchListener mHandleTouchListener = null;
    private boolean mIsBeingDragged;
    private boolean mIsMultiPointTouched = false;
    private boolean mIsOnTop = false;
    private float mLastDownY;
    private float mLastMoveY;
    private LazyModeDragoutListener mLazyModeDragListener = null;
    OnItemLongClickListener mOnItemLongClickListenerInner;
    OnItemLongClickListener mOnItemLongClickListenerProxy = new OnItemLongClickListener() {
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (EmuiListView_V3.this.mOnItemLongClickListenerInner == null) {
                return false;
            }
            EmuiListView_V3.this.mHasLongPressed = true;
            return EmuiListView_V3.this.mOnItemLongClickListenerInner.onItemLongClick(parent, view, position, id);
        }
    };
    private int mTouchSlop;
    private EmuiListViewListener mViewListener;

    public interface HandleTouchListener {
        void handleTouchEvent(View view, MotionEvent motionEvent);
    }

    public interface ListViewDragoutListener {
        void hideTheKeyboard();

        boolean isLayoutExpand();

        void onMove(int i);

        void onPullUP(int i);

        void resetHideKeyBoardSign();
    }

    public interface LazyModeDragoutListener {
        boolean checkActionbarHideOrNot();

        void handleLazyModeTouchEvent(View view, MotionEvent motionEvent);
    }

    public EmuiListView_V3(Context context) {
        super(context);
        calculateTouchSlop(context);
        setScrollParams();
    }

    public EmuiListView_V3(Context context, AttributeSet attrs) {
        super(context, attrs);
        calculateTouchSlop(context);
        setScrollParams();
    }

    public EmuiListView_V3(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        calculateTouchSlop(context);
        setScrollParams();
    }

    private void setScrollParams() {
        setFriction(this.SCROLL_FRICTION);
        setVelocityScale(this.SCROLL_VELOCITYSCALE);
    }

    private void calculateTouchSlop(Context context) {
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public MultiModeListView getListView() {
        return this;
    }

    public void onDataReload() {
        notifySelectChange();
    }

    public void onMenuPrepared() {
        notifySelectChange();
    }

    public boolean isDragListenerCalled() {
        return this.mActionBarDragCalled;
    }

    private void notifySelectChange() {
        if (isInEditMode() && this.mSelectChangeListener != null) {
            this.mSelectChangeListener.onSelectChange(getSelectedCount(), getMessageCount());
        }
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        if (listener == null) {
            setOnLongClickListener(null);
        }
        this.mOnItemLongClickListenerInner = listener;
        super.setOnItemLongClickListener(this.mOnItemLongClickListenerProxy);
    }

    public void setListViewListener(EmuiListViewListener l) {
        this.mViewListener = l;
    }

    public void enterEditMode(int type) {
        super.enterEditMode(type);
        setLongClickable(false);
        if (this.mViewListener != null) {
            this.mViewListener.onEnterEditMode();
        }
    }

    public void exitEditMode() {
        super.exitEditMode();
        setLongClickable(true);
        if (this.mViewListener != null) {
            this.mViewListener.onExitEditMode();
        }
    }

    public void setAllSelected(boolean selected) {
        super.setAllSelected(selected);
        invalidate();
    }

    public void setSeleceted(long itemId, boolean selected) {
        super.setSeleceted(itemId, selected);
        invalidate();
    }

    public void setHandleTouchListener(HandleTouchListener listener) {
        this.mHandleTouchListener = listener;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (this.mHandleTouchListener != null) {
            this.mHandleTouchListener.handleTouchEvent(getListView(), ev);
        }
        if (ev.getAction() == 0) {
            this.mHasLongPressed = false;
        }
        if (this.mLazyModeDragListener == null && this.mActionBarDragListener == null) {
            return super.onTouchEvent(ev);
        }
        int action = ev.getAction();
        if (ev.getPointerCount() > 1) {
            this.mIsMultiPointTouched = true;
        } else if (1 == action || 3 == action) {
            this.mIsMultiPointTouched = false;
        }
        if (!this.mIsMultiPointTouched || this.mLazyModeDragListener == null) {
            boolean isLazyMode = false;
            if (this.mLazyModeDragListener != null) {
                isLazyMode = !this.mLazyModeDragListener.checkActionbarHideOrNot();
            }
            boolean needIgnoreSuper = false;
            if (isLazyMode && 2 == action) {
                needIgnoreSuper = true;
            }
            switch (action) {
                case 0:
                    this.mFirstMotionX = ev.getRawX();
                    this.mFirstMotionY = ev.getRawY();
                    this.mLastMoveY = this.mFirstMotionY;
                    if (this.mLazyModeDragListener != null) {
                        this.mLazyModeDragListener.handleLazyModeTouchEvent(getListView(), ev);
                    }
                    this.mIsOnTop = !canScrollVertically(-1);
                    this.mActionBarDragCalled = false;
                    if (this.mActionBarDragListener != null) {
                        this.mActionBarDragListener.resetHideKeyBoardSign();
                        break;
                    }
                    break;
                case 1:
                case 3:
                    this.mIsBeingDragged = false;
                    if (this.mLazyModeDragListener != null) {
                        this.mLazyModeDragListener.handleLazyModeTouchEvent(getListView(), ev);
                    }
                    if (this.mActionBarDragCalled && this.mActionBarDragListener != null) {
                        this.mActionBarDragListener.onPullUP(getMovedDistance((int) (this.mFirstMotionY - ev.getRawY()), this.mActionBarDragListener.isLayoutExpand()));
                        this.mActionBarDragListener.resetHideKeyBoardSign();
                    }
                    this.mIsOnTop = false;
                    break;
                case 2:
                    float x = ev.getRawX();
                    float y = ev.getRawY();
                    if (this.mLazyModeDragListener != null) {
                        if (this.mIsBeingDragged) {
                            this.mLazyModeDragListener.handleLazyModeTouchEvent(getListView(), ev);
                            break;
                        }
                        float xDiff = Math.abs(x - this.mFirstMotionX);
                        float yDiff = Math.abs(y - this.mFirstMotionY);
                        boolean isMoveDown = y - this.mLastMoveY > 0.0f;
                        this.mLastMoveY = y;
                        if (xDiff > ((float) this.mTouchSlop) && xDiff > yDiff) {
                            if (!this.mIsBeingDragged) {
                                requestDisallowInterceptTouchEvent(false);
                                this.mIsBeingDragged = true;
                            }
                            this.mLazyModeDragListener.handleLazyModeTouchEvent(getListView(), ev);
                        } else if (yDiff > ((float) this.mTouchSlop) && yDiff > xDiff && ((isMoveDown && this.mIsOnTop) || isLazyMode)) {
                            if (!this.mIsBeingDragged) {
                                requestDisallowInterceptTouchEvent(true);
                                this.mIsBeingDragged = true;
                            }
                            this.mLazyModeDragListener.handleLazyModeTouchEvent(getListView(), ev);
                        }
                    }
                    if (this.mActionBarDragListener != null) {
                        int touchY = (int) ev.getRawY();
                        int movedDis = (int) (this.mFirstMotionY - ((float) touchY));
                        boolean isExpand = this.mActionBarDragListener.isLayoutExpand();
                        if (!this.mActionBarDragCalled) {
                            if (!isExpand) {
                                if (this.mIsOnTop && ((float) touchY) > this.mFirstMotionY && Math.abs(movedDis) > this.mTouchSlop) {
                                    requestDisallowInterceptTouchEvent(true);
                                    this.mActionBarDragListener.hideTheKeyboard();
                                    this.mActionBarDragListener.onMove(getMovedDistance(movedDis, false));
                                    this.mActionBarDragCalled = true;
                                    break;
                                }
                            } else if (Math.abs(movedDis) > this.mTouchSlop && ((float) touchY) < this.mFirstMotionY) {
                                requestDisallowInterceptTouchEvent(true);
                                this.mActionBarDragListener.onMove(getMovedDistance(movedDis, true));
                                this.mActionBarDragCalled = true;
                                break;
                            }
                        }
                        this.mActionBarDragListener.onMove(getMovedDistance(movedDis, isExpand));
                        break;
                    }
                    break;
            }
            boolean ret = true;
            if (!needIgnoreSuper || this.mActionBarDragCalled) {
                ret = super.onTouchEvent(ev);
            }
            return ret;
        }
        MLog.d("ListV3", "onTouchEvent::multi point touched, return");
        return super.onTouchEvent(ev);
    }

    public void setBlockLayoutList(boolean block) {
        this.mBlockLayoutList = block;
    }

    protected void layoutChildren() {
        try {
            if (!this.mBlockLayoutList) {
                super.layoutChildren();
            }
        } catch (Exception e) {
            MLog.d("ListV3", "layoutChildren: exception: " + e);
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (this.mLazyModeDragListener == null && this.mActionBarDragListener == null) {
            MLog.d("ListV3", "onInterceptTouchEvent::mLazyModeDragListener and mActionBarDragListener are null, return");
            this.mLastDownY = ev.getRawY();
            return super.onInterceptTouchEvent(ev);
        }
        boolean ret;
        boolean isLazyMode = false;
        if (this.mLazyModeDragListener != null) {
            isLazyMode = !this.mLazyModeDragListener.checkActionbarHideOrNot();
        }
        boolean canScrollVertically = canScrollVertically(1) ? canScrollVertically(-1) : false;
        switch (ev.getAction()) {
            case 0:
                this.mFirstMotionX = ev.getRawX();
                this.mFirstMotionY = ev.getRawY();
                if (canScrollVertically && isLazyMode && this.mLazyModeDragListener != null) {
                    this.mLazyModeDragListener.handleLazyModeTouchEvent(getListView(), ev);
                }
                if (this.mActionBarDragListener != null) {
                    this.mActionBarDragListener.resetHideKeyBoardSign();
                    this.mIsOnTop = !canScrollVertically(-1);
                    this.mActionBarDragCalled = false;
                    break;
                }
                break;
            case 1:
            case 3:
                if (canScrollVertically && isLazyMode) {
                    this.mIsBeingDragged = false;
                    if (this.mLazyModeDragListener != null) {
                        this.mLazyModeDragListener.handleLazyModeTouchEvent(getListView(), ev);
                    }
                }
                if (this.mActionBarDragCalled && this.mActionBarDragListener != null) {
                    this.mActionBarDragListener.onPullUP(getMovedDistance((int) (this.mFirstMotionY - ev.getRawY()), this.mActionBarDragListener.isLayoutExpand()));
                    this.mActionBarDragListener.resetHideKeyBoardSign();
                }
                this.mIsOnTop = false;
                break;
        }
        float xDiff = Math.abs(ev.getRawX() - this.mFirstMotionX);
        float y = ev.getRawY();
        float yDiff = Math.abs(y - this.mFirstMotionY);
        boolean z = (isLazyMode && ev.getAction() == 2) ? canScrollVertically : false;
        boolean actionBarDragCalled = false;
        if (this.mActionBarDragListener != null) {
            boolean isExpand = this.mActionBarDragListener.isLayoutExpand();
            if (ev.getAction() == 2) {
                if (!this.mActionBarDragCalled && (!isExpand || y >= this.mFirstMotionY)) {
                    if (!this.mIsOnTop || isExpand || y <= this.mFirstMotionY) {
                        actionBarDragCalled = false;
                    }
                }
                actionBarDragCalled = true;
            } else {
                actionBarDragCalled = false;
            }
        }
        if (actionBarDragCalled) {
            ret = false;
            if (this.mActionBarDragListener != null) {
                int movedDis = (int) (this.mFirstMotionY - y);
                isExpand = this.mActionBarDragListener.isLayoutExpand();
                if (this.mActionBarDragCalled) {
                    this.mActionBarDragListener.onMove(getMovedDistance(movedDis, isExpand));
                } else if (isExpand) {
                    if (Math.abs(movedDis) > this.mTouchSlop && y < this.mFirstMotionY) {
                        this.mActionBarDragListener.onMove(getMovedDistance(movedDis, true));
                        this.mActionBarDragCalled = true;
                    }
                } else if (this.mIsOnTop && y > this.mFirstMotionY && Math.abs(movedDis) > this.mTouchSlop) {
                    this.mActionBarDragListener.hideTheKeyboard();
                    this.mActionBarDragListener.onMove(getMovedDistance(movedDis, false));
                    this.mActionBarDragCalled = true;
                }
            }
        } else if (z) {
            ret = false;
            if (xDiff > ((float) this.mTouchSlop) && xDiff > yDiff) {
                if (!this.mIsBeingDragged) {
                    requestDisallowInterceptTouchEvent(false);
                    this.mIsBeingDragged = true;
                }
                if (this.mLazyModeDragListener != null) {
                    this.mLazyModeDragListener.handleLazyModeTouchEvent(getListView(), ev);
                }
            } else if (yDiff > ((float) this.mTouchSlop) && yDiff > xDiff) {
                if (!this.mIsBeingDragged) {
                    requestDisallowInterceptTouchEvent(true);
                    this.mIsBeingDragged = true;
                }
                if (this.mLazyModeDragListener != null) {
                    this.mLazyModeDragListener.handleLazyModeTouchEvent(getListView(), ev);
                }
            }
        } else {
            ret = super.onInterceptTouchEvent(ev);
        }
        return ret;
    }

    private int getMovedDistance(int movDis, boolean isExpand) {
        if (isExpand) {
            return movDis - this.mTouchSlop;
        }
        return this.mTouchSlop + movDis;
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case 0:
                this.mLastDownY = ev.getRawY();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    public float getDownPosition() {
        return this.mLastDownY;
    }
}
