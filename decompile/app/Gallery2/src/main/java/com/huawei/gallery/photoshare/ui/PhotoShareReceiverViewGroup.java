package com.huawei.gallery.photoshare.ui;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import com.android.gallery3d.R;
import com.huawei.android.cg.vo.ShareReceiver;
import com.huawei.gallery.photoshare.ui.PhotoShareReceiverView.OnItemListener;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import java.util.ArrayList;

public class PhotoShareReceiverViewGroup extends ViewGroup {
    private int mHorizontalSpacing;
    protected ArrayList<RowInfo> mRows;
    private SyncReceiversListener mSyncReceiversListener = null;
    private ArrayList<ShareReceiver> mTotalAddedReceiverList = new ArrayList();
    protected int mVerticalSpacing;

    public interface SyncReceiversListener {
        void childViewCountChange();

        void deleteNewAddedReceivers(ShareReceiver shareReceiver);

        void deleteTotalReceivers(ShareReceiver shareReceiver);
    }

    public static class LayoutParams extends MarginLayoutParams {
        public LayoutParams(int i, int j) {
            super(i, j);
        }

        public LayoutParams(Context context, AttributeSet attributeset) {
            super(context, attributeset);
        }
    }

    public static class RowInfo {
        int childCount;
        int height;
        int width;
    }

    public void setSyncReceiversListener(SyncReceiversListener listener) {
        this.mSyncReceiversListener = listener;
    }

    public PhotoShareReceiverViewGroup(Context context) {
        super(context);
        setSpacing(context);
        this.mRows = new ArrayList();
    }

    public PhotoShareReceiverViewGroup(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        setSpacing(context);
        this.mRows = new ArrayList();
    }

    public PhotoShareReceiverViewGroup(Context context, AttributeSet attributeset, int i) {
        super(context, attributeset, i);
        setSpacing(context);
        this.mRows = new ArrayList();
    }

    private void setSpacing(Context context) {
        this.mVerticalSpacing = (int) context.getResources().getDimension(R.dimen.photoshare_receiver_vertical_spacing);
        this.mHorizontalSpacing = (int) context.getResources().getDimension(R.dimen.photoshare_receiver_horizontal_spacing);
    }

    public LayoutParams generateLayoutParams(AttributeSet attributeset) {
        return new LayoutParams(getContext(), attributeset);
    }

    protected void onLayout(boolean flag, int l, int t, int r, int b) {
        int childIndex = 0;
        int height = t + getPaddingTop();
        for (int cnt1 = 0; cnt1 < this.mRows.size(); cnt1++) {
            int width = l + getPaddingLeft();
            for (int cnt2 = 0; cnt2 < ((RowInfo) this.mRows.get(cnt1)).childCount; cnt2++) {
                View view = getChildAt(childIndex);
                view.layout(width, height, view.getMeasuredWidth() + width, view.getMeasuredHeight() + height);
                width = (view.getMeasuredWidth() + width) + this.mHorizontalSpacing;
                childIndex++;
            }
            height += ((RowInfo) this.mRows.get(cnt1)).height + this.mVerticalSpacing;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int HeightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int childCnt = getChildCount();
        int height = 0;
        int i2 = 0;
        this.mRows.clear();
        RowInfo rowinfo = new RowInfo();
        for (int index = 0; index < childCnt; index++) {
            View view = getChildAt(index);
            measureChildWithMargins(view, widthMeasureSpec, 0, heightMeasureSpec, height);
            int viewHeight = view.getMeasuredHeight();
            int viewWidth = view.getMeasuredWidth() + 1;
            int width = viewWidth + rowinfo.width;
            if (rowinfo.childCount > 0) {
                width += this.mHorizontalSpacing;
            }
            if (widthMode != 0 && width > widthSize) {
                if (HeightMode != 0 && height >= heightSize) {
                    break;
                }
                if (this.mRows.size() > 0) {
                    height += this.mVerticalSpacing;
                }
                height += rowinfo.height;
                this.mRows.add(rowinfo);
                rowinfo = new RowInfo();
            }
            if (rowinfo.childCount > 0) {
                rowinfo.width += this.mHorizontalSpacing;
            }
            rowinfo.width += viewWidth;
            rowinfo.childCount++;
            i2 = Math.max(i2, rowinfo.width);
            rowinfo.height = Math.max(rowinfo.height, viewHeight);
        }
        if (rowinfo.childCount > 0) {
            if (this.mRows.size() > 0) {
                height += this.mVerticalSpacing;
            }
            height += rowinfo.height;
            this.mRows.add(rowinfo);
        }
        int measuredHeight = resolveSize((getPaddingTop() + height) + getPaddingBottom(), heightMeasureSpec);
        int maxHeight = PhotoShareUtils.getScrollViewMaxHeight();
        if (measuredHeight <= maxHeight) {
            maxHeight = measuredHeight;
        }
        ((View) getParent()).setLayoutParams(new android.widget.LinearLayout.LayoutParams(-1, maxHeight));
        setMeasuredDimension(resolveSize((getPaddingLeft() + i2) + getPaddingRight(), widthMeasureSpec), resolveSize((getPaddingTop() + height) + getPaddingBottom(), heightMeasureSpec));
    }

    public void addReceiver(final ShareReceiver friendsInfo, final PhotoShareReceiverView recipientView) {
        recipientView.setFriendsInfo(friendsInfo);
        if (TextUtils.isEmpty(friendsInfo.getReceiverName())) {
            recipientView.setText(friendsInfo.getReceiverAcc());
        } else {
            recipientView.setText(friendsInfo.getReceiverName());
        }
        recipientView.setTextSize(14.0f);
        recipientView.setTextColor(getResources().getColorStateList(R.color.photoshare_login_color));
        recipientView.setAlpha(0.85f);
        recipientView.setGravity(16);
        recipientView.setFocusable(true);
        recipientView.setSingleLine(true);
        recipientView.setBackgroundResource(R.drawable.photoshare_share_receiver_input_box_selector);
        recipientView.setOnItemClickListener(new OnItemListener() {
            public void onDelete(View v) {
                PhotoShareReceiverViewGroup.this.deleteReceiver(friendsInfo, recipientView);
                PhotoShareReceiverViewGroup.this.mSyncReceiversListener.deleteTotalReceivers(friendsInfo);
                PhotoShareReceiverViewGroup.this.mSyncReceiversListener.deleteNewAddedReceivers(friendsInfo);
            }
        });
        addView(recipientView, getChildCount() - 1, new LayoutParams(-2, -2));
        this.mSyncReceiversListener.childViewCountChange();
        this.mTotalAddedReceiverList.add(friendsInfo);
    }

    public void deleteReceiver(ShareReceiver friendsInfo, PhotoShareReceiverView view) {
        this.mTotalAddedReceiverList.remove(friendsInfo);
        removeView(view);
        this.mSyncReceiversListener.childViewCountChange();
    }

    public boolean exist(ShareReceiver info) {
        if (info == null || TextUtils.isEmpty(info.getReceiverAcc())) {
            return false;
        }
        String account = info.getReceiverAcc();
        for (ShareReceiver item : this.mTotalAddedReceiverList) {
            if (item.getReceiverAcc().equalsIgnoreCase(account) && item.getStatus() != 2) {
                return true;
            }
        }
        return false;
    }

    public boolean exist(String account, ArrayList<ShareReceiver> hasAddedFriends) {
        if (TextUtils.isEmpty(account)) {
            return false;
        }
        for (ShareReceiver item : this.mTotalAddedReceiverList) {
            if (item.getReceiverAcc().equalsIgnoreCase(account)) {
                return true;
            }
        }
        for (ShareReceiver item2 : hasAddedFriends) {
            if (item2.getReceiverAcc().equalsIgnoreCase(account) && item2.getStatus() != 2) {
                return true;
            }
        }
        return false;
    }
}
