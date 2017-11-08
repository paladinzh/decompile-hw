package com.huawei.gallery.util;

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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import com.android.gallery3d.R;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.app.AlbumSetDataLoader;
import com.huawei.watermark.manager.parse.WMElement;

public class DragListView extends ListView {
    private static final int TOUCH_TOLERANCE = GalleryUtils.dpToPixel(20);
    private int mCategoryDividerHeight;
    private int mCoordOffset;
    private AlbumSetDataLoader mDataSource;
    private Bitmap mDragBitmap;
    private int mDragPoint;
    private int mDragPos;
    private ImageView mDragView;
    private DropListener mDropListener;
    private boolean mDropProcessing = false;
    private boolean mEnableMultiSelection = true;
    private int mFirstDragPos;
    private int mFooterViewHeightExpanded;
    private int mFooterViewHeightNormal;
    private int mHeight;
    private int mItemHeightExpanded;
    private int mItemHeightHalf;
    private int mItemHeightNormal;
    private int mLowerBound;
    private int mRemoveMode = 0;
    private Rect mTempRect = new Rect();
    private final int mTouchSlop;
    private int mUpperBound;
    private WindowManager mWindowManager;
    private LayoutParams mWindowParams;

    public interface DropListener {
        void drop(int i, int i2);

        void onDragEnd();

        void onDragStart(int i);
    }

    public DragListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mItemHeightNormal = Float.valueOf(getResources().getDimension(R.dimen.list_album_height)).intValue();
        this.mFooterViewHeightNormal = Float.valueOf(getResources().getDimension(R.dimen.toolbar_footer_height)).intValue();
        this.mCategoryDividerHeight = Float.valueOf(getResources().getDimension(R.dimen.list_category_divider_height)).intValue();
        this.mFooterViewHeightExpanded = this.mFooterViewHeightNormal + this.mItemHeightNormal;
        this.mItemHeightHalf = this.mItemHeightNormal / 2;
        this.mItemHeightExpanded = this.mItemHeightNormal * 2;
    }

    public void setListDataSource(AlbumSetDataLoader source) {
        this.mDataSource = source;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (this.mDropListener != null) {
            switch (ev.getAction()) {
                case 0:
                    int x = (int) ev.getX();
                    int y = (int) ev.getY();
                    int itemnum = pointToPosition(x, y);
                    if (!(itemnum == -1 || canNotDrag(itemnum))) {
                        ViewGroup item = (ViewGroup) getChildAt(itemnum - getFirstVisiblePosition());
                        this.mDragPoint = y - item.getTop();
                        int[] location = new int[2];
                        getLocationInWindow(location);
                        this.mCoordOffset = location[1];
                        View dragger = item.findViewById(R.id.list_drag_control_btn);
                        dragger.getDrawingRect(this.mTempRect);
                        int realX = x - getPaddingLeft();
                        if (realX < dragger.getLeft() - 20 || realX >= dragger.getRight() + TOUCH_TOLERANCE) {
                            stopDragging();
                            break;
                        }
                        Context context = getContext();
                        item.setBackgroundColor(context.getColor(R.color.drag_list_view_moved_item_background));
                        Bitmap bitmap = Bitmap.createBitmap(item.getWidth(), item.getHeight(), Config.ARGB_8888);
                        item.draw(new Canvas(bitmap));
                        item.setBackgroundColor(context.getColor(R.color.transparent));
                        startDragging(bitmap, y);
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
                    break;
            }
        }
        return super.onInterceptTouchEvent(ev);
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
        int footerCount = getFooterViewsCount();
        int i = 0;
        while (true) {
            View v = getChildAt(i);
            if (v == null || !isCloudAlbum(getPositionForView(v))) {
                if (v == null) {
                    if (deletion) {
                        int position = getFirstVisiblePosition();
                        int y = getChildAt(0).getTop();
                        setAdapter(getAdapter());
                        setSelectionFromTop(position, y);
                    }
                    layoutChildren();
                    v = getChildAt(i);
                    if (v == null) {
                        return;
                    }
                }
                ViewGroup.LayoutParams params = v.getLayoutParams();
                params.height = this.mItemHeightNormal;
                if (footerCount != 0 && getPositionForView(v) == getCount() - 1) {
                    params.height = this.mFooterViewHeightNormal;
                }
                v.setLayoutParams(params);
                v.setVisibility(0);
                v.setTag(R.id.list_albumset, null);
            }
            i++;
        }
    }

    private void doExpansion() {
        int childnum = this.mDragPos - getFirstVisiblePosition();
        if (this.mDragPos > this.mFirstDragPos) {
            childnum++;
        }
        View first = getChildAt(this.mFirstDragPos - getFirstVisiblePosition());
        int footerCount = getFooterViewsCount();
        int lastItemPosition = getCount() - 1;
        int i = 0;
        while (true) {
            View vv = getChildAt(i);
            if (vv != null) {
                int height = this.mItemHeightNormal;
                int visibility = 0;
                if (vv == first) {
                    if (!(this.mDragPos == this.mFirstDragPos || getPositionForView(vv) == lastItemPosition)) {
                        height = 1;
                    }
                    visibility = 4;
                } else if (i == childnum) {
                    if (this.mDragPos < lastItemPosition) {
                        height = this.mItemHeightExpanded;
                        setLayoutGravityWhenExpanded(vv, 80);
                    }
                } else if (getPositionForView(vv) == lastItemPosition && !canNotDrag(getPositionForView(vv))) {
                    if (this.mDragPos == lastItemPosition) {
                        if (footerCount != 0) {
                            height = this.mFooterViewHeightExpanded;
                        } else {
                            height = this.mItemHeightExpanded;
                        }
                        setLayoutGravityWhenExpanded(vv, 48);
                    } else {
                        height = this.mItemHeightNormal;
                    }
                }
                if (!isCloudAlbum(getPositionForView(vv))) {
                    if (height != this.mItemHeightNormal) {
                        vv.setTag(R.id.list_albumset, Integer.valueOf(height));
                    }
                    ViewGroup.LayoutParams params = vv.getLayoutParams();
                    params.height = height;
                    vv.setLayoutParams(params);
                    vv.setVisibility(visibility);
                }
                i++;
            } else {
                return;
            }
        }
    }

    private void setLayoutGravityWhenExpanded(View view, int gravity) {
        if (view != null) {
            View listLayout = view.findViewById(R.id.list_item_content);
            if (listLayout != null) {
                ViewGroup.LayoutParams params = listLayout.getLayoutParams();
                if (params instanceof FrameLayout.LayoutParams) {
                    ((FrameLayout.LayoutParams) params).gravity = gravity;
                }
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
                int y = (int) ev.getY();
                dragView((int) ev.getX(), y);
                int itemnum = getItemForPosition(y);
                if (!(itemnum == -1 || canNotDrag(itemnum) || itemnum < 0)) {
                    if (action == 0 || itemnum != this.mDragPos) {
                        this.mDragPos = itemnum;
                        doExpansion();
                    }
                    int speed = 0;
                    adjustScrollBounds(y);
                    if (y > this.mLowerBound) {
                        speed = getLastVisiblePosition() < getCount() + -1 ? y > (this.mHeight + this.mLowerBound) / 2 ? 16 : 4 : 1;
                    } else if (y < this.mUpperBound) {
                        speed = y < this.mUpperBound / 2 ? -16 : -4;
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
                if (this.mDropListener != null && this.mDragPos >= 0 && this.mDragPos < getCount() && !this.mDropProcessing) {
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

    private void startDragging(Bitmap bm, int y) {
        stopDragging();
        this.mWindowParams = new LayoutParams();
        this.mWindowParams.gravity = 48;
        this.mWindowParams.x = 0;
        this.mWindowParams.y = Math.max(this.mCoordOffset, (y - this.mDragPoint) + this.mCoordOffset);
        this.mWindowParams.height = -2;
        this.mWindowParams.width = -1;
        this.mWindowParams.flags = 920;
        this.mWindowParams.format = -3;
        this.mWindowParams.windowAnimations = 0;
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
            float alpha = WMElement.CAMERASIZEVALUE1B1;
            int width = this.mDragView.getWidth();
            if (x > width / 2) {
                int int2 = width / 2;
                alpha = Integer.valueOf(width - x).floatValue() / Integer.valueOf(int2).floatValue();
            }
            this.mWindowParams.alpha = alpha;
        }
        this.mWindowParams.y = Math.max(this.mCoordOffset, (y - this.mDragPoint) + this.mCoordOffset);
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

    private boolean canNotDrag(int position) {
        MediaSet mediaSet = this.mDataSource.getMediaSet(position);
        return mediaSet != null ? mediaSet.isVirtual() : true;
    }

    public void setMultiSelectEnable(boolean enable) {
        this.mEnableMultiSelection = enable;
    }

    protected void onMultiSelectMove(MotionEvent ev, int pointerIndex) {
        if (this.mEnableMultiSelection) {
            super.onMultiSelectMove(ev, pointerIndex);
        }
    }

    public int getItemHeightNormal() {
        return this.mItemHeightNormal;
    }

    public int getPhotoShareItemHeight() {
        return this.mItemHeightNormal + this.mCategoryDividerHeight;
    }

    private boolean isCloudAlbum(int position) {
        MediaSet mediaSet = this.mDataSource.getMediaSet(position);
        if (mediaSet == null) {
            return false;
        }
        return "photoshare".equalsIgnoreCase(mediaSet.getLabel());
    }
}
