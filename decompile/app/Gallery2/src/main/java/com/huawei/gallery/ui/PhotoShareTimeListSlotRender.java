package com.huawei.gallery.ui;

import android.content.Context;
import com.android.gallery3d.app.Config$CloudSharePage;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.ui.SelectionManager;
import com.huawei.gallery.app.MediaItemsDataLoader;
import com.huawei.gallery.ui.TimeAxisLabel.TitleSpec;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class PhotoShareTimeListSlotRender extends ListSlotRender {
    public PhotoShareTimeListSlotRender(GalleryContext activity, ListSlotView slotView, SelectionManager selectionManager, int placeholderColor) {
        super(activity, slotView, selectionManager, placeholderColor);
    }

    protected ListSlotRenderData createDataWindow(MediaItemsDataLoader model) {
        return new PhotoShareTimeSlidingWindow(this.mActivity, model, SmsCheckResult.ESCT_216, getTitleSpec(this.mActivity.getAndroidContext()));
    }

    protected TitleSpec getTitleSpec(Context context) {
        return Config$CloudSharePage.get(context).titleLabelSpec;
    }

    protected boolean needOverlayWhenRendTitle() {
        return false;
    }
}
