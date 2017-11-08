package com.android.contacts.list;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.list.FavoritesFrequentMultiSelectAdapter;
import com.android.contacts.util.ContactDisplayUtils;
import com.google.android.gms.R;

public class DragListView extends ListView {
    private int hyalineColor;
    private Context mContext;
    private Bitmap mDragBitmap;
    private FavoritesFrequentMultiSelectAdapter mDragListViewAdapter;
    private int mDragOffset;
    private int mDragPoint;
    private int mDragPos;
    private ImageView mDragView;
    private boolean mDragging = false;
    private DropListener mDropListener;
    private boolean mDropProcessing = false;
    private int mFirstDragPos;
    private int mHeight;
    private int mItemHeaderViewHeight;
    private int mItemHeightExpanded;
    private int mItemHeightHalf;
    private int mItemHeightNormal;
    private final int mLeftTouchTolerance;
    private int mLowerBound;
    private int mPaddingBottom;
    private int mPaddingTop;
    private int mPhotoViewHeight;
    private int mRemoveMode = 0;
    private final int mRightTouchTolerance;
    private int mStarredCount;
    private Rect mTempRect = new Rect();
    private final int mTouchSlop;
    private int mUpperBound;
    private WindowManager mWindowManager;
    private LayoutParams mWindowParams;
    private int selectedColor;

    public interface DropListener {
        void drop(int i, int i2);

        void onDragEnd();

        void onDragStart(int i);
    }

    public DragListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mRightTouchTolerance = Float.valueOf(getResources().getDimension(R.dimen.drag_touch_tolerance_right)).intValue();
        this.mLeftTouchTolerance = Float.valueOf(getResources().getDimension(R.dimen.drag_touch_tolerance_left)).intValue();
        this.mPhotoViewHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.default_detail_contact_photo_margin);
        this.mPaddingTop = this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_list_item_padding_top);
        this.mPaddingBottom = this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_list_item_padding_bottom);
        int height = (this.mPhotoViewHeight + this.mPaddingTop) + this.mPaddingBottom;
        if (ContactDisplayUtils.isSimpleDisplayMode()) {
            height = context.getResources().getDimensionPixelSize(R.dimen.drag_list_view_double_line_height);
        }
        this.mItemHeightNormal = height;
        this.mItemHeightHalf = this.mItemHeightNormal / 2;
        this.mItemHeightExpanded = this.mItemHeightNormal * 2;
        this.mItemHeaderViewHeight = context.getResources().getDimensionPixelSize(R.dimen.drag_list_view_header_height);
        this.selectedColor = this.mContext.getResources().getColor(R.color.draglistview_backgroud_selected_color);
        this.hyalineColor = this.mContext.getResources().getColor(R.color.draglistview_backgroud_hyaline_color);
    }

    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        if (this.mDragging) {
            return true;
        }
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (this.mDropListener != null && ev.getAction() == 0) {
            int x = (int) ev.getX();
            int y = (int) ev.getY();
            int itemnum = pointToPosition(x, y);
            if (itemnum > this.mStarredCount || itemnum <= 0) {
                return super.onInterceptTouchEvent(ev);
            }
            LinearLayout item = (LinearLayout) getChildAt(itemnum - getFirstVisiblePosition());
            this.mDragPoint = y - item.getTop();
            this.mDragOffset = ((int) ev.getRawY()) - y;
            if (item.getChildAt(0) instanceof ContactListItemView) {
                ImageView dragger = ((ContactListItemView) item.getChildAt(0)).getDragIcon();
                if (x - getPaddingLeft() >= dragger.getLeft() - this.mLeftTouchTolerance && x - getPaddingLeft() < dragger.getRight() + this.mRightTouchTolerance) {
                    this.mDragging = true;
                    item.setDrawingCacheEnabled(true);
                    item.setBackgroundColor(this.selectedColor);
                    Bitmap bitmap = Bitmap.createBitmap(item.getWidth(), item.getHeight(), Config.ARGB_8888);
                    item.draw(new Canvas(bitmap));
                    item.setBackgroundColor(this.hyalineColor);
                    startDragging(bitmap, y, item.getWidth());
                    this.mDragPos = itemnum;
                    this.mFirstDragPos = this.mDragPos;
                    if (this.mDropListener != null) {
                        this.mDropListener.onDragStart(this.mFirstDragPos);
                    }
                    this.mHeight = getHeight();
                    int touchSlop = this.mTouchSlop;
                    this.mUpperBound = Math.min(y - touchSlop, this.mHeight / 3);
                    this.mLowerBound = Math.max(y + touchSlop, (this.mHeight * 2) / 3);
                    return false;
                }
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void setStarredCount(int count) {
        this.mStarredCount = count;
    }

    private int myPointToPosition(int y) {
        if (y < 0) {
            int pos = myPointToPosition(this.mItemHeightNormal + y);
            if (pos > 0) {
                return pos - 1;
            }
        }
        Rect frame = this.mTempRect;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            getChildAt(i).getHitRect(frame);
            if (frame.contains(frame.left, y)) {
                return getFirstVisiblePosition() + i;
            }
        }
        return -1;
    }

    private int getItemForPosition(int y) {
        int adjustedy = (y - this.mDragPoint) - this.mItemHeightHalf;
        int pos = myPointToPosition(adjustedy);
        if (pos >= 0) {
            if (pos <= this.mFirstDragPos) {
                return pos + 1;
            }
            return pos;
        } else if (adjustedy < 0) {
            return 0;
        } else {
            return pos;
        }
    }

    private void adjustScrollBounds(int y) {
        if (y >= this.mHeight / 3) {
            this.mUpperBound = this.mHeight / 3;
        }
        if (y <= (this.mHeight * 2) / 3) {
            this.mLowerBound = (this.mHeight * 2) / 3;
        }
    }

    private void unExpandViews(boolean deletion) {
        int i = 0;
        if (getChildAt(0) instanceof RelativeLayout) {
            i = 1;
        }
        while (true) {
            LinearLayout ll = (LinearLayout) getChildAt(i);
            int position = getFirstVisiblePosition();
            if (ll == null) {
                if (deletion) {
                    int y = getChildAt(0).getTop();
                    setAdapter(getAdapter());
                    setSelectionFromTop(position, y);
                }
                layoutChildren();
                ll = (LinearLayout) getChildAt(i);
                if (ll == null) {
                    this.mDragListViewAdapter.setDragItemHeight(false, -1);
                    this.mDragListViewAdapter.notifyChange();
                    this.mDragging = false;
                    return;
                }
            }
            int height = this.mItemHeightNormal;
            if (position + i > this.mStarredCount && ((double) Math.abs(this.mContext.getResources().getConfiguration().fontScale - 1.30001f)) < 1.0E-7d) {
                height += ((this.mPaddingTop + this.mContext.getResources().getDimensionPixelSize(R.dimen.drag_list_view_largest_font_line_height)) + this.mPaddingBottom) - height;
            }
            if (position + i == this.mStarredCount + 1) {
                height += this.mItemHeaderViewHeight;
            }
            ViewGroup.LayoutParams params = ll.getLayoutParams();
            params.height = height;
            ll.setLayoutParams(params);
            ll.setVisibility(0);
            i++;
        }
    }

    private void doExpansion() {
        int childnum = this.mDragPos - getFirstVisiblePosition();
        if (this.mDragPos > this.mFirstDragPos) {
            childnum++;
        }
        View first = getChildAt(this.mFirstDragPos - getFirstVisiblePosition());
        int i = 0;
        if (getChildAt(0) instanceof RelativeLayout) {
            i = 1;
        }
        while (true) {
            View ll = (LinearLayout) getChildAt(i);
            if (ll != null) {
                int height = this.mItemHeightNormal;
                int visibility = 0;
                int position = getFirstVisiblePosition();
                if (ll == first) {
                    if (this.mDragPos == this.mFirstDragPos || getPositionForView(ll) == getCount() - 1) {
                        visibility = 4;
                    } else {
                        height = 1;
                        this.mDragListViewAdapter.setDragItemHeight(true, (position + i) - 1);
                    }
                } else if (i == childnum && this.mDragPos < getCount() - 1) {
                    height = this.mItemHeightExpanded;
                }
                if (position + i > this.mStarredCount && ((double) Math.abs(this.mContext.getResources().getConfiguration().fontScale - 1.30001f)) < 1.0E-7d) {
                    int contentHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.drag_list_view_largest_font_line_height);
                    if (i == childnum) {
                        height += (((this.mPaddingTop + contentHeight) + this.mPaddingBottom) + this.mItemHeightNormal) - height;
                    } else {
                        height += ((this.mPaddingTop + contentHeight) + this.mPaddingBottom) - height;
                    }
                }
                if (position + i == this.mStarredCount + 1) {
                    height += this.mItemHeaderViewHeight;
                }
                ViewGroup.LayoutParams params = ll.getLayoutParams();
                params.height = height;
                ll.setLayoutParams(params);
                ll.setVisibility(visibility);
                i++;
            } else {
                return;
            }
        }
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (this.mDropListener == null || this.mDragView == null) {
            return super.onTouchEvent(ev);
        }
        int action = ev.getAction();
        switch (action) {
            case 0:
            case 2:
                int x = (int) ev.getX();
                int y = (int) ev.getY();
                int itemnum = getItemForPosition(y);
                if (itemnum <= this.mStarredCount && itemnum > 0) {
                    dragView(x, y);
                    if (action == 0 || itemnum != this.mDragPos) {
                        this.mDragPos = itemnum;
                        doExpansion();
                    }
                    int speed = 0;
                    adjustScrollBounds(y);
                    if (y > this.mLowerBound) {
                        if (getLastVisiblePosition() < getCount() - 1) {
                            speed = y > (this.mHeight + this.mLowerBound) / 2 ? 32 : 8;
                        }
                        if (getLastVisiblePosition() > this.mStarredCount) {
                            speed = 0;
                        }
                    } else if (y < this.mUpperBound) {
                        speed = y < this.mUpperBound / 2 ? -32 : -8;
                        if (getFirstVisiblePosition() == 0 && getChildAt(0).getTop() >= getPaddingTop()) {
                            speed = 0;
                        }
                    }
                    if (speed != 0) {
                        smoothScrollBy(speed, 30);
                        break;
                    }
                }
                break;
            case 1:
            case 3:
                this.mDragView.setVisibility(4);
                this.mDragView.getDrawingRect(this.mTempRect);
                stopDragging();
                if (this.mDragPos >= 0 && this.mDragPos < getCount() && !this.mDropProcessing) {
                    this.mDropProcessing = true;
                    this.mDropListener.drop(this.mFirstDragPos, this.mDragPos);
                    this.mDropProcessing = false;
                }
                if (this.mDropListener != null) {
                    this.mDropListener.onDragEnd();
                }
                unExpandViews(false);
                break;
        }
        return true;
    }

    private void startDragging(Bitmap bm, int y, int width) {
        stopDragging();
        this.mWindowParams = new LayoutParams();
        if (CommonUtilMethods.isLayoutRTL()) {
            this.mWindowParams.gravity = 53;
        } else {
            this.mWindowParams.gravity = 51;
        }
        this.mWindowParams.x = 0;
        this.mWindowParams.y = Math.max(this.mDragOffset, (y - this.mDragPoint) + this.mDragOffset);
        this.mWindowParams.height = -2;
        this.mWindowParams.width = width;
        this.mWindowParams.flags = 920;
        this.mWindowParams.format = -3;
        this.mWindowParams.windowAnimations = 0;
        this.mWindowParams.type = 1000;
        Context context = getContext();
        ImageView v = new ImageView(context);
        v.setImageBitmap(bm);
        this.mDragBitmap = bm;
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mWindowManager.addView(v, this.mWindowParams);
        this.mDragView = v;
    }

    private void dragView(int x, int y) {
        if (this.mRemoveMode == 1) {
            float alpha = 1.0f;
            int width = this.mDragView.getWidth();
            if (x > width / 2) {
                int int2 = width / 2;
                alpha = Integer.valueOf(width - x).floatValue() / Integer.valueOf(int2).floatValue();
            }
            this.mWindowParams.alpha = alpha;
        }
        this.mWindowParams.y = Math.max(this.mDragOffset, (y - this.mDragPoint) + this.mDragOffset);
        this.mWindowManager.updateViewLayout(this.mDragView, this.mWindowParams);
    }

    private void stopDragging() {
        if (this.mDragView != null) {
            ((WindowManager) getContext().getSystemService("window")).removeView(this.mDragView);
            this.mDragView.setImageDrawable(null);
            this.mDragView = null;
        }
        if (this.mDragBitmap != null) {
            this.mDragBitmap.recycle();
            this.mDragBitmap = null;
        }
    }

    public void setDropListener(DropListener l) {
        this.mDropListener = l;
    }

    public void setDragListViewAdapter(FavoritesFrequentMultiSelectAdapter adapter) {
        this.mDragListViewAdapter = adapter;
    }
}
