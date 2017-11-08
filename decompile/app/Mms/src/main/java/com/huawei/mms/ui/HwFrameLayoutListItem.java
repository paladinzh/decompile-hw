package com.huawei.mms.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HeaderViewListAdapter;
import com.android.mms.ui.ConversationListAdapter;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.R;
import com.huawei.mms.ui.MultiModeListView.CheckableView;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwMessageUtils;
import huawei.android.widget.SwipeLayout;
import huawei.android.widget.SwipeLayout.DragEdge;
import huawei.android.widget.SwipeLayout.ShowMode;

public class HwFrameLayoutListItem extends SwipeLayout implements CheckableView {
    private Runnable mChangeBackgroundRunnable = new Runnable() {
        public void run() {
            HwFrameLayoutListItem.this.setBackground(HwFrameLayoutListItem.this.getResources().getDrawable(R.drawable.conversation_list_item_pressed_bg));
            HwFrameLayoutListItem.this.mHasPressedBgColor = true;
        }
    };
    private boolean mHasPressedBgColor = false;
    private CheckableView mMmsListItem;
    private int mStartX = 0;
    private int mStartY = 0;

    public HwFrameLayoutListItem(Context context) {
        super(context);
    }

    public HwFrameLayoutListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        View view = findViewById(R.id.mms_animation_list_item_view);
        if (view instanceof CheckableView) {
            this.mMmsListItem = (CheckableView) view;
        }
        setShowMode(ShowMode.PullOut);
        setClickToClose(true);
        if (MessageUtils.isNeedLayoutRtl()) {
            setDrag(DragEdge.Left, findViewById(R.id.bottom_wrapper));
        } else {
            setDrag(DragEdge.Right, findViewById(R.id.bottom_wrapper));
        }
    }

    public boolean isChecked() {
        if (this.mMmsListItem != null) {
            return this.mMmsListItem.isChecked();
        }
        return false;
    }

    public void setChecked(boolean checked) {
        if (this.mMmsListItem != null) {
            this.mMmsListItem.setChecked(checked);
        }
    }

    public void toggle() {
        if (this.mMmsListItem != null) {
            this.mMmsListItem.toggle();
        }
    }

    public void setEditAble(final boolean editable) {
        HwBackgroundLoader.getUIHandler().post(new Runnable() {
            public void run() {
                if (HwFrameLayoutListItem.this.mMmsListItem != null) {
                    HwFrameLayoutListItem.this.mMmsListItem.setEditAble(editable);
                }
            }
        });
    }

    public void setEditAble(boolean editable, boolean checked) {
        if (this.mMmsListItem != null) {
            this.mMmsListItem.setEditAble(editable, checked);
        }
    }

    public boolean isEditAble() {
        if (this.mMmsListItem != null) {
            return this.mMmsListItem.isEditAble();
        }
        return false;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (HwMessageUtils.isSplitOn()) {
            return super.onTouchEvent(ev);
        }
        switch (ev.getAction()) {
            case 0:
                this.mStartX = (int) ev.getRawX();
                this.mStartY = (int) ev.getRawY();
                if (getParent() instanceof EmuiListView_V3) {
                    EmuiListView_V3 listView = (EmuiListView_V3) getParent();
                    listView.mHasLongPressed = false;
                    if (listView.getAdapter() instanceof HeaderViewListAdapter) {
                        HeaderViewListAdapter headerViewListAdapter = (HeaderViewListAdapter) listView.getAdapter();
                        if (headerViewListAdapter.getWrappedAdapter() instanceof ConversationListAdapter) {
                            if (!((ConversationListAdapter) headerViewListAdapter.getWrappedAdapter()).hasOpenSwipe()) {
                                HwBackgroundLoader.getUIHandler().postDelayed(this.mChangeBackgroundRunnable, 50);
                                break;
                            }
                            setBackground(null);
                            break;
                        }
                    }
                }
                break;
            case 1:
            case 3:
                HwBackgroundLoader.getUIHandler().postDelayed(new Runnable() {
                    public void run() {
                        HwBackgroundLoader.getUIHandler().removeCallbacks(HwFrameLayoutListItem.this.mChangeBackgroundRunnable);
                        HwFrameLayoutListItem.this.setBackground(null);
                    }
                }, 50);
                break;
            case 2:
                int nowY = (int) ev.getRawY();
                if (Math.abs(((int) ev.getRawX()) - this.mStartX) > 10 || Math.abs(nowY - this.mStartY) > 10) {
                    HwBackgroundLoader.getUIHandler().removeCallbacks(this.mChangeBackgroundRunnable);
                    if (this.mHasPressedBgColor) {
                        setBackground(null);
                        this.mHasPressedBgColor = false;
                        break;
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }
}
