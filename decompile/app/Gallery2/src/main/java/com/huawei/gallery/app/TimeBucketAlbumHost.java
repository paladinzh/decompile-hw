package com.huawei.gallery.app;

import android.os.Bundle;
import com.android.gallery3d.util.TraceController;

public class TimeBucketAlbumHost extends GLFragment {
    protected void onInflateFinished() {
        TraceController.beginSection("TimeBucketAlbumHost.onInflateFinished");
        Bundle data = new Bundle();
        data.putString("media-path", TimeBucketPage.SOURCE_DATA_PATH);
        getStateManager().startState(TimeBucketPage.class, data);
        if (this.mUserHaveFirstLook) {
            super.onUserSelected(true);
        }
        TraceController.endSection();
    }

    protected void onRestoreFinished() {
        if (this.mUserHaveFirstLook) {
            super.onUserSelected(true);
        }
    }
}
