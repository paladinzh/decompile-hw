package com.huawei.gallery.photorectify;

import android.view.View;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.MediaItem;
import com.huawei.gallery.app.PhotoPage;
import com.huawei.gallery.app.plugin.PhotoExtraButton;
import com.huawei.gallery.app.plugin.PhotoFragmentPlugin;
import com.huawei.gallery.app.plugin.PhotoFragmentPlugin.PhotoDownloadButtonListener;

public class PhotoRectifyImageManager extends PhotoFragmentPlugin {
    public PhotoRectifyImageManager(GalleryContext context) {
        super(context);
    }

    public boolean onEventsHappens(MediaItem currentItem, View button) {
        if (isButtonDisabled(currentItem, button) || this.mFragmentPluginManager == null) {
            return false;
        }
        if (PhotoFragmentPlugin.isNeedDownloadOriginImage(currentItem)) {
            PhotoFragmentPlugin.showDownloadTipsDialog(this.mContext.getActivityContext(), currentItem, R.string.download_title, R.string.dialog_photoshare_download_refocus_content, new PhotoDownloadButtonListener());
            return false;
        }
        ((PhotoPage) this.mFragmentPluginManager.getHost()).switchToDocRectifyPage();
        return true;
    }

    public boolean updateExtraButton(PhotoExtraButton button, MediaItem currentItem) {
        if (isButtonDisabled(currentItem, button)) {
            return false;
        }
        button.setPhotoExtraButtonOverlay(null);
        button.setImageResource(R.drawable.ic_gallery_info_rectify);
        button.setContentDescription(this.mContext.getResources().getText(R.string.folder_doc_rectify));
        return true;
    }

    private boolean isButtonDisabled(MediaItem currentItem, View button) {
        if (currentItem != null && currentItem.isRectifyImage() && button.getId() == R.id.plugin_button) {
            return false;
        }
        return true;
    }
}
