package com.huawei.watermark.ui.watermarklib;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.huawei.watermark.manager.parse.WMElement;
import com.huawei.watermark.ui.baseview.HorizontalListView;
import com.huawei.watermark.ui.baseview.HorizontalListViewAdapter;
import com.huawei.watermark.wmutil.WMResourceUtil;
import com.huawei.watermark.wmutil.WMUIUtil;

public abstract class WMCategoryListView extends HorizontalListView {
    public static final int SHOWTYPEALWAYS = 2;
    public static final int SHOWTYPEWHENMENUOPEN = 1;
    private Context mContext;
    public int mShowType = 0;

    public abstract void setLayoutParams();

    public abstract void setShowType();

    public WMCategoryListView(Context context) {
        super(context);
        setLayoutParams();
        setShowType();
        this.mContext = context;
    }

    public int getShowType() {
        return this.mShowType;
    }

    public void showAlways(ViewGroup viewGroup) {
        if (this.mShowType == 2) {
            WMUIUtil.separateView(this);
            viewGroup.addView(this);
        }
    }

    public void hideAlways(ViewGroup viewGroup) {
        if (this.mShowType == 2) {
            viewGroup.removeViewInLayout(this);
        }
    }

    public void showWhenMenuOpen(ViewGroup viewGroup) {
        if (this.mShowType == 1) {
            WMUIUtil.separateView(this);
            viewGroup.addView(this);
        }
    }

    public void hideWhenMenuClose(ViewGroup viewGroup) {
        if (this.mShowType == 1) {
            viewGroup.removeViewInLayout(this);
        }
    }

    public void refreshListViewWhenCategoryDataPrepared(int categoryindex) {
        if (this.mShowType == 2) {
            setSelection(categoryindex);
            HorizontalListViewAdapter hListViewAdapter = (HorizontalListViewAdapter) getAdapter();
            if (hListViewAdapter != null) {
                hListViewAdapter.notifyDataSetChanged();
            }
        }
    }

    protected synchronized void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (this.mScroller.isFinished() && this.mAdapter != null) {
            int selectIndex = ((HorizontalListViewAdapter) this.mAdapter).selectIndex;
            for (int i = 0; i < getChildCount(); i++) {
                setItemAlpha(i, selectIndex, this.mContext, getChildAt(i));
            }
        }
    }

    public void onOrientationChanged(int ori) {
        if (ori == 180) {
            ori = 0;
        }
        super.onOrientationChanged(ori);
    }

    private void setItemAlpha(int position, int selectIndex, Context context, View view) {
        if (position == selectIndex) {
            view.setAlpha(WMElement.CAMERASIZEVALUE1B1);
        } else if (isLeftOverBounder(context, position) || isRightOverBounder(context, position)) {
            view.setAlpha(0.5f);
        } else {
            view.setAlpha(WMElement.CAMERASIZEVALUE1B1);
        }
    }

    private boolean isLeftOverBounder(Context context, int index) {
        if (((index * WMResourceUtil.getDimensionPixelSize(context, "watermark_category_list_item_width")) - getScrollToLeft()) + 60 < 0) {
            return true;
        }
        return false;
    }

    private boolean isRightOverBounder(Context context, int index) {
        if ((((index + 1) * WMResourceUtil.getDimensionPixelSize(context, "watermark_category_list_item_width")) - getScrollToLeft()) - 60 > getWidth()) {
            return true;
        }
        return false;
    }
}
