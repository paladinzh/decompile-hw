package com.android.mms.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.RcsFavoritesListView;
import com.huawei.mms.ui.EmuiListView_V3;
import java.util.HashSet;

public class FavoritesListView extends EmuiListView_V3 {
    private RcsFavoritesListView mHwCust = null;

    public FavoritesListView(Context context) {
        super(context);
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mHwCust = new RcsFavoritesListView(context);
        }
    }

    public FavoritesListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mHwCust = new RcsFavoritesListView(context);
        }
    }

    public RcsFavoritesListView getHwCustFavoritesListView() {
        return this.mHwCust;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        if (mode == Integer.MIN_VALUE) {
            mode = 1073741824;
        }
        super.onMeasure(widthMeasureSpec, MeasureSpec.getSize(heightMeasureSpec) | mode);
    }

    public void onContactChange() {
        for (int index = 0; index < getChildCount(); index++) {
            ((FavoritesListItem) getChildAt(index)).refreshAddressText();
        }
        invalidateViews();
    }

    public boolean isAllSelected() {
        return getRecorder().size() == (getViewMode() == 2 ? getSmsCount() : getCount());
    }

    public int getMessageCount() {
        return getViewMode() == 2 ? getSmsCount() : getCount();
    }

    public void setAllSelected(boolean selected) {
        if (selected) {
            HashSet<Long> newSelected = new HashSet();
            for (int i = 0; i < getCount(); i++) {
                int type = ((FavoritesListAdapter) getAdapter()).getItemViewType(i);
                if (getViewMode() != 2 || 1 != type) {
                    newSelected.add(Long.valueOf(getItemIdAtPosition(i)));
                }
            }
            getRecorder().replace(newSelected);
        } else {
            getRecorder().clear();
        }
        setAllViewsChecked(selected);
    }

    private void setAllViewsChecked(boolean setChecked) {
        for (int index = 0; index < getChildCount(); index++) {
            Object obj;
            FavoritesListItem view = (FavoritesListItem) getChildAt(index);
            if (view.isMms() && getViewMode() == 2) {
                obj = 1;
            } else {
                obj = null;
            }
            if (obj == null) {
                view.setChecked(setChecked);
            }
        }
    }

    private int getSmsCount() {
        int smsCount = 0;
        for (int i = 0; i < getCount(); i++) {
            if (((FavoritesListAdapter) getAdapter()).getItemViewType(i) == 0) {
                smsCount++;
            }
        }
        return smsCount;
    }

    public boolean isInvalideItemId(long itemId) {
        return itemId == 0;
    }
}
