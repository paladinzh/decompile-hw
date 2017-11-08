package com.huawei.gallery.app.plugin;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.format.Formatter;
import android.view.View;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.GalleryImage;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.PhotoShareImage;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.app.IPhotoPage;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;

public abstract class PhotoFragmentPlugin {
    protected final GalleryContext mContext;
    protected PhotoFragmentPluginManager mFragmentPluginManager = null;

    public class PhotoDownloadButtonListener implements OnClickListener {
        private PhotoFragmentPlugin mOwner = PhotoFragmentPlugin.this;

        public void onClick(DialogInterface dialog, int which) {
            if (-1 == which && (this.mOwner.mFragmentPluginManager.getHost() instanceof IPhotoPage)) {
                ((IPhotoPage) this.mOwner.mFragmentPluginManager.getHost()).photoShareDownLoadOrigin();
            }
        }
    }

    public PhotoFragmentPlugin(GalleryContext context) {
        this.mContext = context;
    }

    public void setManager(PhotoFragmentPluginManager manager) {
        this.mFragmentPluginManager = manager;
    }

    public void onResume() {
    }

    public void onPause() {
    }

    public boolean onBackPressed() {
        return false;
    }

    public boolean onInterceptActionItemClick(Action action) {
        return false;
    }

    public boolean onEventsHappens(MediaItem currentItem, View button) {
        return false;
    }

    public boolean updateExtraButton(PhotoExtraButton button, MediaItem currentItem) {
        return false;
    }

    public void onPhotoChanged() {
    }

    public static boolean isNeedDownloadOriginImage(MediaItem mediaItem) {
        if (mediaItem instanceof PhotoShareImage) {
            if (!PhotoShareUtils.isFileExists(mediaItem.getFileInfo().getLocalRealPath())) {
                return true;
            }
        } else if ((mediaItem instanceof GalleryImage) && ((GalleryImage) mediaItem).getLocalMediaId() == -1) {
            return true;
        }
        return false;
    }

    public static void showDownloadTipsDialog(Context context, MediaItem mediaItem, int titleId, int tipsId, OnClickListener clickListener) {
        String titleString;
        if (titleId == R.string.download_title) {
            titleString = context.getString(R.string.download_title, new Object[]{Formatter.formatFileSize(context, mediaItem.getFileInfo().getSize())});
        } else {
            titleString = context.getString(titleId);
        }
        PhotoShareUtils.getPhotoShareDialog(context, titleString, (int) R.string.photoshare_download_short, (int) R.string.cancel, context.getString(tipsId, new Object[]{Formatter.formatFileSize(context, mediaItem.getFileInfo().getSize())}), clickListener).show();
    }
}
