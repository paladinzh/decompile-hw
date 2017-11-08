package com.android.gallery3d.ui;

import android.net.Uri;
import android.os.Bundle;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.MenuExecutor.ShareProgressListener;
import com.android.gallery3d.ui.MenuExecutorFactory.Style;
import com.android.gallery3d.util.ImageVideoTranser;
import java.io.File;
import java.util.ArrayList;

public class ShareExecutor {

    public interface ShareExecutorListener {
        void onProcessDone(ArrayList<Uri> arrayList);

        void onProgress(MediaObject mediaObject);

        boolean shouldConvertVI();
    }

    public static void convertShareItems(ArrayList<Path> items, GalleryContext activity, MenuExecutor executor, ShareExecutorListener listener) {
        if (executor == null) {
            listener.onProcessDone(null);
            return;
        }
        final boolean shouldConvertVI = listener.shouldConvertVI();
        Style style = shouldConvertVI ? Style.SHARE_TRANS_STYLE : Style.WAIT_STYLE;
        final int maxProgress = ((shouldConvertVI ? ImageVideoTranser.getVoiceImageCountInArray(items, activity) : 0) * 99) + items.size();
        final GalleryContext galleryContext = activity;
        final ShareExecutorListener shareExecutorListener = listener;
        final MenuExecutor menuExecutor = executor;
        executor.setShareProcessor(new ShareProgressListener() {
            DataManager dataManager = galleryContext.getDataManager();
            int finishedProgress = 0;
            File outputFileDir;
            ArrayList<Uri> uris = new ArrayList();

            public void process(Path path) {
                MediaObject mo = this.dataManager.getMediaObject(path);
                if ((mo.getSupportedOperations() & 4) == 0) {
                    this.finishedProgress++;
                    return;
                }
                shareExecutorListener.onProgress(mo);
                if (shouldConvertVI && ImageVideoTranser.isItemSupportTransVer(mo)) {
                    this.uris.add(ImageVideoTranser.translateVoiceImageToVideo(mo, this.finishedProgress, maxProgress, menuExecutor, this.outputFileDir));
                    this.finishedProgress += 100;
                } else {
                    this.uris.add(mo.getContentUri());
                    this.finishedProgress++;
                }
                menuExecutor.updateProgress((this.finishedProgress * 100) / maxProgress, null);
            }

            public void onProcessDone() {
                shareExecutorListener.onProcessDone(this.uris);
            }
        });
        boolean needShowWaitDialogAndDisableAction = !shouldConvertVI ? needShowWaitDialogAndDisableAction(items.size()) : true;
        Bundle data = new Bundle();
        data.putBoolean("key-customprogress", true);
        executor.startAction(R.id.action_share, R.string.share, null, false, needShowWaitDialogAndDisableAction, style, items, data);
    }

    public static boolean needShowWaitDialogAndDisableAction(int itemSize) {
        return itemSize > 50;
    }
}
