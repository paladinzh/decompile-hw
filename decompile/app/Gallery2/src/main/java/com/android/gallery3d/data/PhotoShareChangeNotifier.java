package com.android.gallery3d.data;

import android.text.TextUtils;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.photoshare.receiver.PhotoShareSdkCallBackManager;
import com.huawei.gallery.photoshare.receiver.PhotoShareSdkCallBackManager.SdkListener;
import java.util.concurrent.atomic.AtomicBoolean;

public class PhotoShareChangeNotifier {
    private static final String TAG = PhotoShareChangeNotifier.class.getSimpleName();
    private int mClassifyAlbumType;
    private AtomicBoolean mContentDirty;
    private MediaSet mMediaSet;
    private String mMediaSetPath;
    private String mPhotoShareRoot;
    private SdkListener mSdkListener;
    private int mType;

    public PhotoShareChangeNotifier(MediaSet set, int type) {
        this.mContentDirty = new AtomicBoolean(true);
        this.mClassifyAlbumType = -1;
        this.mMediaSet = set;
        this.mType = type;
        this.mPhotoShareRoot = PhotoShareAlbumSet.PATH_ALL.toString();
        this.mMediaSetPath = this.mMediaSet.mPath.toString();
        this.mSdkListener = new SdkListener() {
            public void onContentChange(int type, String path) {
                PhotoShareChangeNotifier.this.sdkCallBackOnContentChange(type, path);
            }

            public void onFolderChange(int type) {
                PhotoShareChangeNotifier.this.sdkCallBackOnFolderChange(type);
            }

            public void onTagListChanged(String categoryID) {
                PhotoShareChangeNotifier.this.sdkCallBackOnTagListChanged(categoryID);
            }

            public void onTagContentChanged(String categoryID, String tagID) {
                PhotoShareChangeNotifier.this.sdkCallBackOnTagContentChanged(categoryID, tagID);
            }
        };
        PhotoShareSdkCallBackManager.getInstance().addListener(this.mSdkListener);
    }

    private void sdkCallBackOnContentChange(int type, String path) {
        if (!((this.mType & type) == 0 || TextUtils.isEmpty(path) || ((!this.mMediaSetPath.equals(this.mPhotoShareRoot) && !path.equals(this.mMediaSet.mPath.getSuffix())) || !this.mContentDirty.compareAndSet(false, true)))) {
            GalleryLog.v(TAG, "PhotoShareChangeNotifier onContentChange " + this.mMediaSetPath);
            this.mMediaSet.notifyContentChanged();
        }
    }

    private void sdkCallBackOnFolderChange(int type) {
        if ((this.mType & type) != 0 && this.mContentDirty.compareAndSet(false, true)) {
            this.mMediaSet.notifyContentChanged();
            GalleryLog.v(TAG, "PhotoShareChangeNotifier onFolderChange " + this.mMediaSetPath);
        }
    }

    private void sdkCallBackOnTagListChanged(String categoryID) {
        if ((this.mType & 2) != 0) {
            if (!(this.mMediaSetPath.equals(this.mPhotoShareRoot) || this.mClassifyAlbumType == 1 || (this.mClassifyAlbumType == 2 && this.mMediaSet.mPath.getSuffix().equalsIgnoreCase(categoryID)))) {
                if (this.mClassifyAlbumType == 3 && this.mMediaSet.mPath.getParent().getSuffix().equalsIgnoreCase(categoryID)) {
                }
            }
            if (this.mContentDirty.compareAndSet(false, true)) {
                GalleryLog.v(TAG, "PhotoShareChangeNotifier onTagListChanged " + this.mMediaSetPath);
                this.mMediaSet.notifyContentChanged();
            }
        }
    }

    private void sdkCallBackOnTagContentChanged(String categoryID, String tagID) {
        if ((this.mType & 2) != 0) {
            if (!(this.mMediaSetPath.equals(this.mPhotoShareRoot) || this.mClassifyAlbumType == 1 || (this.mClassifyAlbumType == 2 && this.mMediaSet.mPath.getSuffix().equalsIgnoreCase(categoryID)))) {
                if (this.mClassifyAlbumType == 3 && this.mMediaSet.mPath.getParent().getSuffix().equalsIgnoreCase(categoryID) && this.mMediaSet.mPath.getSuffix().equalsIgnoreCase(tagID)) {
                }
            }
            if (this.mContentDirty.compareAndSet(false, true)) {
                GalleryLog.v(TAG, "PhotoShareChangeNotifier onTagContentChanged " + this.mMediaSetPath);
                this.mMediaSet.notifyContentChanged();
            }
        }
    }

    public PhotoShareChangeNotifier(MediaSet set, int type, int classifyAlbumType) {
        this(set, type);
        this.mClassifyAlbumType = classifyAlbumType;
    }

    public boolean isDirty() {
        return this.mContentDirty.compareAndSet(true, false);
    }
}
