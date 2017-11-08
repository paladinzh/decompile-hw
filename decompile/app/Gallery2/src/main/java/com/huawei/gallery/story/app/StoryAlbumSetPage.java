package com.huawei.gallery.story.app;

import android.content.Intent;
import android.os.Bundle;
import com.android.gallery3d.app.Config$CommonAlbumSetPage;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.app.CommonAlbumSetDataLoader;
import com.huawei.gallery.app.CommonAlbumSetPage;
import com.huawei.gallery.story.ui.StoryAlbumSetSlotRender;
import com.huawei.gallery.story.ui.StoryAlbumSetSlotView;
import com.huawei.gallery.ui.CommonAlbumSetSlotRender;

public class StoryAlbumSetPage extends CommonAlbumSetPage {
    protected CommonAlbumSetSlotRender onCreateSlotRender() {
        return new StoryAlbumSetSlotRender(this.mHost.getGalleryContext(), this.mSlotView, this.mSelectionManager, Config$CommonAlbumSetPage.get(this.mHost.getActivity()).placeholderColor);
    }

    protected StoryAlbumSetSlotView onCreateSlotView() {
        return new StoryAlbumSetSlotView(this.mHost.getGalleryContext(), Config$CommonAlbumSetPage.get(this.mHost.getActivity()).slotViewSpec);
    }

    protected CommonAlbumSetDataLoader onCreateDataLoader(MediaSet mediaSet) {
        return new CommonAlbumSetDataLoader(mediaSet, 64, 1);
    }

    protected void initializeView() {
        super.initializeView();
        this.mTopCover.setVisibility(1);
    }

    protected void onLoadingFinished(boolean loadingFailed) {
        super.onLoadingFinished(loadingFailed);
        if (this.mDataLoader.size() == 0) {
            this.mHost.getActivity().finish();
        }
    }

    public void onSingleTapUp(int index, boolean cornerPressed) {
        if (this.mIsActive) {
            MediaSet set = this.mDataLoader.getMediaSet(index);
            if (set != null) {
                Bundle data = new Bundle();
                data.putBoolean("get-content", false);
                data.putString("media-path", set.getPath().toString());
                Intent intent = new Intent(this.mHost.getActivity(), StoryAlbumActivity.class);
                intent.putExtras(data);
                GalleryUtils.startActivityCatchSecurityEx(this.mHost.getActivity(), intent);
            }
        }
    }
}
