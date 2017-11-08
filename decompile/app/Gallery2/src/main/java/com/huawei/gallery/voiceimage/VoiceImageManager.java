package com.huawei.gallery.voiceimage;

import android.view.View;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.MediaItem;
import com.huawei.gallery.app.PhotoPage.ActionBarProgressActionListener;
import com.huawei.gallery.app.plugin.PhotoExtraButton;
import com.huawei.gallery.app.plugin.PhotoFragmentPlugin;
import com.huawei.gallery.app.plugin.PhotoFragmentPlugin.PhotoDownloadButtonListener;

public class VoiceImageManager extends PhotoFragmentPlugin {
    private VoiceImageController mController;

    public VoiceImageManager(GalleryContext context, ActionBarProgressActionListener listener) {
        super(context);
        this.mController = new VoiceImageController(context.getAndroidContext(), listener);
    }

    public void onPause() {
        this.mController.stop();
    }

    public boolean onEventsHappens(MediaItem currentItem, View button) {
        if (isButtonDisabled(currentItem, button) || this.mFragmentPluginManager == null) {
            return false;
        }
        if (currentItem.getVoiceOffset() > 0 || currentItem.getFileInfo() == null) {
            this.mController.playPause();
            return false;
        }
        PhotoFragmentPlugin.showDownloadTipsDialog(this.mContext.getActivityContext(), currentItem, R.string.download_title, R.string.download_voice_image_when_play, new PhotoDownloadButtonListener());
        return false;
    }

    public boolean updateExtraButton(PhotoExtraButton button, MediaItem currentItem) {
        if (isButtonDisabled(currentItem, button)) {
            return false;
        }
        button.setImageResource(R.drawable.ic_gallery_info_soundphoto);
        button.setContentDescription(button.getContext().getString(R.string.drm_ro_operation_play));
        this.mController.refresh(button, currentItem);
        return true;
    }

    public void onPhotoChanged() {
        this.mController.stop();
    }

    private boolean isButtonDisabled(MediaItem currentItem, View button) {
        if (currentItem != null && currentItem.isVoiceImage() && button.getId() == R.id.plugin_button) {
            return false;
        }
        return true;
    }
}
