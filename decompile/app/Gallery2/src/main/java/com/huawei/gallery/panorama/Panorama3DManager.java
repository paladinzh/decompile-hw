package com.huawei.gallery.panorama;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.text.format.Formatter;
import android.view.View;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.PhotoShareImage;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.app.IPhotoPage;
import com.huawei.gallery.app.plugin.PhotoExtraButton;
import com.huawei.gallery.app.plugin.PhotoFragmentPlugin;
import com.huawei.gallery.extfile.FyuseFile;
import com.huawei.gallery.extfile.FyuseManager;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;

public class Panorama3DManager extends PhotoFragmentPlugin {

    private class ButtonListener implements OnClickListener {
        private ButtonListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            if (-1 == which && (Panorama3DManager.this.mFragmentPluginManager.getHost() instanceof IPhotoPage)) {
                ((IPhotoPage) Panorama3DManager.this.mFragmentPluginManager.getHost()).photoShareDownLoadOrigin();
            }
        }
    }

    public Panorama3DManager(GalleryContext context) {
        super(context);
    }

    public boolean onEventsHappens(MediaItem currentItem, View button) {
        if (!isButtonEnabled(currentItem, button)) {
            return false;
        }
        long size = 0;
        boolean needDownload = false;
        if (currentItem.getSpecialFileType() == 11) {
            if (currentItem instanceof PhotoShareImage) {
                PhotoShareImage item = (PhotoShareImage) currentItem;
                if (!(item.getFilePathType() == 1 && FyuseFile.queryFyuseData(this.mContext.getAndroidContext(), item.getFilePath()))) {
                    size = item.getFileInfo().getSize() + ((long) item.getPanorama3dDataSize());
                    needDownload = true;
                }
            } else if (PhotoFragmentPlugin.isNeedDownloadOriginImage(currentItem)) {
                size = currentItem.getFileInfo().getSize();
                needDownload = true;
            }
        } else if (PhotoFragmentPlugin.isNeedDownloadOriginImage(currentItem)) {
            size = currentItem.getFileInfo().getSize();
            needDownload = true;
        }
        if (needDownload) {
            PhotoShareUtils.getPhotoShareDialog(this.mContext.getAndroidContext(), (int) R.string.dialog_photoshare_download_3dimage_title, (int) R.string.photoshare_download_short, (int) R.string.cancel, this.mContext.getString(R.string.dialog_photoshare_download_3dimage_content, Formatter.formatFileSize(this.mContext.getAndroidContext(), size)), new ButtonListener()).show();
            return false;
        } else if (FyuseManager.getInstance().startViewFyuseFile(this.mContext.getActivityContext(), currentItem)) {
            return false;
        } else {
            return startPlayFyuseFile(currentItem);
        }
    }

    public boolean startPlayFyuseFile(MediaItem currentItem) {
        if (this.mContext == null || currentItem == null || currentItem.getSpecialFileType() != 20) {
            GalleryLog.d("Panorama3DManager", "startPlayFyuseFile failed");
            return true;
        }
        Intent intent = new Intent();
        intent.putExtra("extra.fyuse.path", currentItem.getFilePath());
        intent.putExtra("media-item-path", currentItem.getPath().toString());
        intent.setClass(this.mContext.getAndroidContext(), Panorama3DActivity.class);
        try {
            this.mContext.getAndroidContext().startActivity(intent);
        } catch (Exception e) {
            GalleryLog.d("Panorama3DManager", "StartPlayFyuseFile fail");
        }
        return false;
    }

    public boolean updateExtraButton(PhotoExtraButton button, MediaItem currentItem) {
        if (!isButtonEnabled(currentItem, button)) {
            return false;
        }
        button.setPhotoExtraButtonOverlay(null);
        button.setImageResource(R.drawable.ic_unlock_3d_info);
        button.setContentDescription(this.mContext.getResources().getText(R.string.panorama));
        return true;
    }

    private boolean isButtonEnabled(MediaItem currentItem, View v) {
        if (currentItem != null && FyuseFile.isSupport3DPanorama() && currentItem.is3DPanorama() && v.getId() == R.id.plugin_button1) {
            return true;
        }
        return false;
    }
}
