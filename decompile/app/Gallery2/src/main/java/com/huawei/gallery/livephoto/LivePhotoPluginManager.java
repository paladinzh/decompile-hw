package com.huawei.gallery.livephoto;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.app.PhotoPage;
import com.huawei.gallery.app.plugin.PhotoExtraButton;
import com.huawei.gallery.app.plugin.PhotoFragmentPlugin;
import com.huawei.gallery.app.plugin.PhotoFragmentPlugin.PhotoDownloadButtonListener;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class LivePhotoPluginManager extends PhotoFragmentPlugin {
    private OnDownloadClickListener mClickListener = new OnDownloadClickListener();

    private class OnDownloadClickListener extends PhotoDownloadButtonListener {
        private CheckBox mCheckBox;

        private OnDownloadClickListener() {
            super();
        }

        private void setCheckBox(CheckBox checkBox) {
            this.mCheckBox = checkBox;
        }

        @SuppressWarnings({"ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD"})
        public void onClick(DialogInterface dialog, int which) {
            if (this.mCheckBox != null && this.mCheckBox.isChecked()) {
                LiveUtils.sNeedDownloadTips = false;
            }
            super.onClick(dialog, which);
        }
    }

    public LivePhotoPluginManager(GalleryContext context) {
        super(context);
    }

    public boolean onEventsHappens(MediaItem currentItem, View button) {
        if (isButtonDisabled(currentItem, button) || this.mFragmentPluginManager == null) {
            GalleryLog.d("LivePhotoPluginManager", "live photo isButtonDisabled");
            return false;
        } else if (PhotoFragmentPlugin.isNeedDownloadOriginImage(currentItem)) {
            if (LiveUtils.sNeedDownloadTips) {
                showDownloadTipsDialogWithCheckbox(this.mContext.getActivityContext(), currentItem, R.string.download_title, R.string.download_live_photo_when_play, this.mClickListener);
            } else {
                this.mClickListener.setCheckBox(null);
                this.mClickListener.onClick(null, -1);
            }
            return false;
        } else {
            ((PhotoPage) this.mFragmentPluginManager.getHost()).playLivePhoto();
            return false;
        }
    }

    public void showDownloadTipsDialogWithCheckbox(Context context, MediaItem mediaItem, int titleId, int tipsId, OnDownloadClickListener clickListener) {
        String titleString;
        if (titleId == R.string.download_title) {
            titleString = context.getString(R.string.download_title, new Object[]{Formatter.formatFileSize(context, mediaItem.getFileInfo().getSize())});
        } else {
            titleString = context.getString(titleId);
        }
        String contentString = context.getString(tipsId, new Object[]{Formatter.formatFileSize(context, mediaItem.getFileInfo().getSize())});
        AlertDialog dialog = new Builder(context).setTitle(titleString).create();
        View view = LayoutInflater.from(context).inflate(R.layout.download_origin_file_tips, null);
        ((TextView) view.findViewById(R.id.message)).setText(contentString);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.check_download_without_tips_later);
        dialog.setView(view);
        clickListener.setCheckBox(checkBox);
        dialog.setButton(-2, context.getString(R.string.cancel), clickListener);
        dialog.setButton(-1, context.getString(R.string.photoshare_download_short), clickListener);
        dialog.show();
    }

    public boolean updateExtraButton(PhotoExtraButton button, MediaItem currentItem) {
        if (isButtonDisabled(currentItem, button)) {
            return false;
        }
        button.setPhotoExtraButtonOverlay(null);
        button.setImageResource(R.drawable.ic_camera_livephoto);
        button.setContentDescription(this.mContext.getResources().getText(R.string.view_live_photo));
        return true;
    }

    private boolean isButtonDisabled(MediaItem currentItem, View button) {
        if (LiveUtils.LIVE_ENABLE && currentItem != null && currentItem.getSpecialFileType() == 50 && button.getId() == R.id.plugin_button) {
            return false;
        }
        return true;
    }
}
