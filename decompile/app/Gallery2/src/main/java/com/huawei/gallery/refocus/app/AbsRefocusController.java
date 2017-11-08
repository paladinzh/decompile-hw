package com.huawei.gallery.refocus.app;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.MediaScannerClient;
import java.io.File;

public abstract class AbsRefocusController {
    protected Context mContext;
    protected AbsRefocusDelegate mDelegate;
    protected int mPhotoHeight;
    protected int mPhotoWidth;
    protected boolean mPrepareComplete = false;
    protected Uri mSaveAsFileUri;

    protected class RefocusMediaScannerClient extends MediaScannerClient {
        public RefocusMediaScannerClient(Context context, File file) {
            super(context, file, null);
        }

        public void onScanCompleted(String path, Uri uri) {
            AbsRefocusController.this.mSaveAsFileUri = uri;
            super.onScanCompleted(path, uri);
        }
    }

    public AbsRefocusController(Context context, AbsRefocusDelegate delegate) {
        this.mContext = context;
        this.mDelegate = delegate;
        this.mPhotoWidth = this.mDelegate.getPhotoWidth();
        this.mPhotoHeight = this.mDelegate.getPhotoHeight();
        this.mSaveAsFileUri = null;
    }

    public boolean prepare() {
        return false;
    }

    public void resizePhoto() {
    }

    public boolean doRefocus(Point touchPoint) {
        return false;
    }

    public boolean saveFileIfNecessary() {
        return false;
    }

    public void saveAs() {
    }

    public void cleanUp() {
    }

    public void showFocusIndicator() {
    }

    public boolean ifPhotoChanged() {
        return false;
    }

    public void setWideApertureValue(int value) {
    }

    public Uri getSaveAsFileUri() {
        return this.mSaveAsFileUri;
    }

    public boolean prepareComplete() {
        return this.mPrepareComplete;
    }

    public boolean isRefocusPhoto() {
        return false;
    }

    public String getSaveMessage() {
        return "";
    }

    public static void showHint(Context context, String message, int duration) {
        ContextedUtils.showToastQuickly(context, (CharSequence) message, duration);
    }

    protected String getFilePath() {
        int i;
        File currentFile = new File(this.mDelegate.getFilePath());
        int index = currentFile.getName().lastIndexOf(".");
        int maxIndex = 0;
        String newFileName = currentFile.getParent() + File.separator + currentFile.getName().substring(0, index);
        for (i = 1; i < 127; i++) {
            if (new File(newFileName + "_" + i + currentFile.getName().substring(index)).exists()) {
                maxIndex = i;
            }
        }
        if (maxIndex < 126) {
            return new File(newFileName + "_" + (maxIndex + 1) + currentFile.getName().substring(index)).getAbsolutePath();
        }
        for (i = 126; i < Integer.MAX_VALUE; i++) {
            File newFile = new File(newFileName + "_" + i + currentFile.getName().substring(index));
            if (!newFile.exists()) {
                return newFile.getAbsolutePath();
            }
        }
        return null;
    }
}
