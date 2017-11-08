package com.huawei.gallery.refocus.wideaperture.photo3dview.app;

import android.view.View;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.MediaItem;
import com.huawei.gallery.app.PhotoPage;
import com.huawei.gallery.app.plugin.PhotoExtraButton;
import com.huawei.gallery.app.plugin.PhotoFragmentPlugin;
import com.huawei.gallery.app.plugin.PhotoFragmentPlugin.PhotoDownloadButtonListener;
import com.huawei.gallery.refocus.wideaperture.utils.WideAperturePhotoUtil;

public class WideAperture3DPhotoManager extends PhotoFragmentPlugin {
    public WideAperture3DPhotoManager(GalleryContext context) {
        super(context);
    }

    public boolean onEventsHappens(MediaItem currentItem, View button) {
        if (!isButtonEnabled(currentItem, button) || this.mFragmentPluginManager == null) {
            return false;
        }
        if (PhotoFragmentPlugin.isNeedDownloadOriginImage(currentItem)) {
            PhotoFragmentPlugin.showDownloadTipsDialog(this.mContext.getActivityContext(), currentItem, R.string.photoshare_download_short, R.string.dialog_photoshare_download_refocus_content, new PhotoDownloadButtonListener());
            return false;
        }
        ((PhotoPage) this.mFragmentPluginManager.getHost()).setSwipingEnabled(false);
        ((PhotoPage) this.mFragmentPluginManager.getHost()).setWantPictureFullViewCallbacks(true, 2);
        if (!((PhotoPage) this.mFragmentPluginManager.getHost()).resetImageToFullView()) {
            ((PhotoPage) this.mFragmentPluginManager.getHost()).setWantPictureFullViewCallbacks(false, 0);
            ((PhotoPage) this.mFragmentPluginManager.getHost()).switchTo3DViewPage();
        }
        return true;
    }

    public boolean updateExtraButton(PhotoExtraButton button, MediaItem currentItem) {
        if (!isButtonEnabled(currentItem, button)) {
            return false;
        }
        button.setPhotoExtraButtonOverlay(null);
        button.setImageResource(R.drawable.btn_wideaperturephoto_3d_view);
        button.setContentDescription(this.mContext.getResources().getText(R.string.ic_allfocus_normal));
        return true;
    }

    private boolean isButtonEnabled(MediaItem currentItem, View v) {
        if (currentItem != null && currentItem.getRefocusPhotoType() == 2 && WideAperturePhotoUtil.support3DView() && v.getId() == R.id.plugin_button1) {
            return true;
        }
        return false;
    }
}
