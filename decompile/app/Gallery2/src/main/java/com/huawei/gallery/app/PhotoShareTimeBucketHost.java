package com.huawei.gallery.app;

public class PhotoShareTimeBucketHost extends PhotoShareAlbumHost {
    protected void initTag() {
        this.mTag = "PhotoShareTimeBucketHost";
    }

    protected void onInflateFinished() {
        getStateManager().startState(PhotoShareTimeBucketPage.class, getArguments());
    }
}
