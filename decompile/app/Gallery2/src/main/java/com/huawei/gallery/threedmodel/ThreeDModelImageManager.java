package com.huawei.gallery.threedmodel;

import android.view.View;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.app.PhotoPage;
import com.huawei.gallery.app.plugin.PhotoExtraButton;
import com.huawei.gallery.app.plugin.PhotoFragmentPlugin;
import com.huawei.gallery.app.plugin.PhotoFragmentPlugin.PhotoDownloadButtonListener;

public class ThreeDModelImageManager extends PhotoFragmentPlugin {
    public ThreeDModelImageManager(GalleryContext context) {
        super(context);
    }

    public boolean onEventsHappens(MediaItem currentItem, View button) {
        if (isButtonDisabled(currentItem, button) || this.mFragmentPluginManager == null) {
            GalleryLog.d("ThreeDModelImageManager", "3D Model isButtonDisabled");
            return false;
        } else if (PhotoFragmentPlugin.isNeedDownloadOriginImage(currentItem)) {
            PhotoFragmentPlugin.showDownloadTipsDialog(this.mContext.getActivityContext(), currentItem, R.string.download_title, R.string.portrait3d_if_download_original_model, new PhotoDownloadButtonListener());
            return false;
        } else {
            ((PhotoPage) this.mFragmentPluginManager.getHost()).switchTo3DModelPage();
            return true;
        }
    }

    public boolean updateExtraButton(PhotoExtraButton button, MediaItem currentItem) {
        if (isButtonDisabled(currentItem, button)) {
            return false;
        }
        button.setPhotoExtraButtonOverlay(null);
        button.setImageResource(R.drawable.ic_gallery_info_3d_portrait);
        button.setContentDescription(this.mContext.getResources().getText(R.string.portrait3d_view_3d_model));
        return true;
    }

    private boolean isButtonDisabled(MediaItem currentItem, View button) {
        if (currentItem != null && currentItem.is3DModelImage() && button.getId() == R.id.plugin_button) {
            return false;
        }
        return true;
    }
}
