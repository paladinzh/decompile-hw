package com.huawei.mms.ui.control;

import android.view.View;
import com.huawei.mms.ui.HwComposeBottomEditView;
import com.huawei.mms.ui.HwComposeBottomEditView.ScrollableCallback;

public class HwComposeBottomEditViewControl {
    public void setScrollableCallback(View view, ScrollableCallback scrollableCallback) {
        if (view != null && (view instanceof HwComposeBottomEditView)) {
            ((HwComposeBottomEditView) view).setScrollableCallback(scrollableCallback);
        }
    }

    public void setActionBarHeight(View view, int actionBarHeight) {
        if (view != null && (view instanceof HwComposeBottomEditView)) {
            ((HwComposeBottomEditView) view).setActionBarHeight(actionBarHeight);
        }
    }

    public void setmAttchmentDraftViewHeight(View view, int draftViewHeight) {
        if (view != null && (view instanceof HwComposeBottomEditView)) {
            ((HwComposeBottomEditView) view).setmAttchmentDraftViewHeight(draftViewHeight);
        }
    }

    public void setBottomGroupHeight(View view, int bottomGroupHeight) {
        if (view != null && (view instanceof HwComposeBottomEditView)) {
            ((HwComposeBottomEditView) view).setBottomGroupHeight(bottomGroupHeight);
        }
    }
}
