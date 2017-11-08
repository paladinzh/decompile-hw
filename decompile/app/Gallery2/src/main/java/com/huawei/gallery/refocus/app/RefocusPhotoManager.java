package com.huawei.gallery.refocus.app;

import android.view.View;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.app.PhotoPage;
import com.huawei.gallery.app.plugin.PhotoExtraButton;
import com.huawei.gallery.app.plugin.PhotoFragmentPlugin;
import com.huawei.gallery.app.plugin.PhotoFragmentPlugin.PhotoDownloadButtonListener;
import com.huawei.gallery.refocus.wideaperture.utils.WideAperturePhotoUtil;

public class RefocusPhotoManager extends PhotoFragmentPlugin {
    public RefocusPhotoManager(GalleryContext context) {
        super(context);
    }

    public boolean onEventsHappens(MediaItem currentItem, View button) {
        if (isButtonDisabled(currentItem, button) || this.mFragmentPluginManager == null) {
            GalleryLog.d("RefocusPhotoManager", "refocus isButtonDisabled");
            return false;
        } else if (PhotoFragmentPlugin.isNeedDownloadOriginImage(currentItem)) {
            PhotoFragmentPlugin.showDownloadTipsDialog(this.mContext.getActivityContext(), currentItem, R.string.download_title, R.string.download_refocus_when_operate, new PhotoDownloadButtonListener());
            return false;
        } else {
            ((PhotoPage) this.mFragmentPluginManager.getHost()).setSwipingEnabled(false);
            ((PhotoPage) this.mFragmentPluginManager.getHost()).setWantPictureFullViewCallbacks(true, 0);
            if (!((PhotoPage) this.mFragmentPluginManager.getHost()).resetImageToFullView()) {
                ((PhotoPage) this.mFragmentPluginManager.getHost()).setWantPictureFullViewCallbacks(false, 0);
                ((PhotoPage) this.mFragmentPluginManager.getHost()).switchToAllFocusFragment();
            }
            return true;
        }
    }

    public boolean updateExtraButton(PhotoExtraButton button, MediaItem currentItem) {
        if (isButtonDisabled(currentItem, button)) {
            return false;
        }
        if (currentItem.getRefocusPhotoType() == 2 && !WideAperturePhotoUtil.supportPhotoEdit()) {
            return false;
        }
        button.setPhotoExtraButtonOverlay(null);
        if (currentItem.getRefocusPhotoType() == 1) {
            button.setImageResource(R.drawable.ic_gallery_info_allfocus);
        } else {
            button.setImageResource(R.drawable.ic_gallery_info_aperture);
        }
        button.setContentDescription(this.mContext.getResources().getText(R.string.ic_allfocus_normal));
        return true;
    }

    private boolean isButtonDisabled(MediaItem currentItem, View button) {
        if (currentItem != null && currentItem.isRefocusPhoto() && button.getId() == R.id.plugin_button) {
            return false;
        }
        return true;
    }
}
