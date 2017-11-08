package com.huawei.gallery.app;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.text.format.Formatter;
import com.android.gallery3d.R;
import com.android.gallery3d.data.GalleryImage;
import com.android.gallery3d.data.GalleryMediaItem;
import com.android.gallery3d.data.GalleryRecycleAlbum;
import com.android.gallery3d.data.GalleryVideo;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.android.cg.vo.FileInfo;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionBarStateBase;
import com.huawei.gallery.media.services.StorageService;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils.DownLoadProgressListener;
import com.huawei.gallery.ui.PhotoPageLoadingManager;

public class GalleryMediaPhotoPage extends PhotoPage implements DownLoadProgressListener, IPhotoPage {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    private OnClickListener mAllowDataAccessListener;
    private OnClickListener mDownloadListener;
    private boolean mIsDownloadFailed = false;
    private PhotoPageLoadingManager mManager = null;
    private Action[] mNeedDownloadMenu = new Action[]{Action.SHARE, Action.PHOTOSHARE_DOWNLOAD, Action.DEL, Action.EDIT, Action.ADD_COMMENT, Action.EDIT_COMMENT, Action.MORE_EDIT, Action.RANGE_MEASURE, Action.SLIDESHOW, Action.PRINT, Action.RENAME, Action.SETAS, Action.ROTATE_RIGHT, Action.SHOW_ON_MAP, Action.SEE_BARCODE_INFO};
    private boolean mNeedToPlayAfterDownLoad = false;
    private Action[] mOriginDownloadedMenu = new Action[]{Action.SHARE, Action.NOT_MYFAVORITE, Action.DEL, Action.EDIT, Action.ADD_COMMENT, Action.EDIT_COMMENT, Action.MORE_EDIT, Action.RANGE_MEASURE, Action.SLIDESHOW, Action.PRINT, Action.RENAME, Action.SETAS, Action.ROTATE_RIGHT, Action.SHOW_ON_MAP, Action.SEE_BARCODE_INFO};
    private Action[] mRecycledBinMenu = new Action[]{Action.RECYCLE_DELETE, Action.RECYCLE_RECOVERY};

    private static /* synthetic */ int[] -getcom-huawei-gallery-actionbar-ActionSwitchesValues() {
        if (-com-huawei-gallery-actionbar-ActionSwitchesValues != null) {
            return -com-huawei-gallery-actionbar-ActionSwitchesValues;
        }
        int[] iArr = new int[Action.values().length];
        try {
            iArr[Action.ADD.ordinal()] = 6;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Action.ADD_ALBUM.ordinal()] = 7;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Action.ADD_COMMENT.ordinal()] = 8;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Action.AIRSHARE.ordinal()] = 9;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Action.ALBUM.ordinal()] = 10;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Action.ALL.ordinal()] = 11;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Action.BACK.ordinal()] = 12;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Action.CANCEL_DETAIL.ordinal()] = 13;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Action.COLLAGE.ordinal()] = 14;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Action.COMMENT.ordinal()] = 15;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Action.COPY.ordinal()] = 16;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Action.DEALL.ordinal()] = 17;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Action.DEL.ordinal()] = 18;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Action.DETAIL.ordinal()] = 19;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[Action.DYNAMIC_ALBUM.ordinal()] = 20;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[Action.EDIT.ordinal()] = 1;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[Action.EDIT_COMMENT.ordinal()] = 21;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[Action.GOTO_GALLERY.ordinal()] = 22;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[Action.HIDE.ordinal()] = 23;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[Action.INFO.ordinal()] = 24;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[Action.KEYGUARD_LIKE.ordinal()] = 25;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[Action.KEYGUARD_NOT_LIKE.ordinal()] = 26;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[Action.LOOPPLAY.ordinal()] = 27;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[Action.MAP.ordinal()] = 28;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[Action.MENU.ordinal()] = 29;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[Action.MORE_EDIT.ordinal()] = 30;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[Action.MOVE.ordinal()] = 31;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[Action.MOVEIN.ordinal()] = 32;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[Action.MOVEOUT.ordinal()] = 33;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[Action.MULTISCREEN.ordinal()] = 34;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[Action.MULTISCREEN_ACTIVITED.ordinal()] = 35;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[Action.MULTI_SELECTION.ordinal()] = 36;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[Action.MULTI_SELECTION_ON.ordinal()] = 37;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[Action.MYFAVORITE.ordinal()] = 38;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[Action.NO.ordinal()] = 39;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[Action.NONE.ordinal()] = 40;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[Action.NOT_MYFAVORITE.ordinal()] = 41;
        } catch (NoSuchFieldError e37) {
        }
        try {
            iArr[Action.OK.ordinal()] = 42;
        } catch (NoSuchFieldError e38) {
        }
        try {
            iArr[Action.PHOTOSHARE_ACCOUNT.ordinal()] = 43;
        } catch (NoSuchFieldError e39) {
        }
        try {
            iArr[Action.PHOTOSHARE_ADDPICTURE.ordinal()] = 44;
        } catch (NoSuchFieldError e40) {
        }
        try {
            iArr[Action.PHOTOSHARE_BACKUP.ordinal()] = 45;
        } catch (NoSuchFieldError e41) {
        }
        try {
            iArr[Action.PHOTOSHARE_CANCEL_RECEIVE.ordinal()] = 46;
        } catch (NoSuchFieldError e42) {
        }
        try {
            iArr[Action.PHOTOSHARE_CLEAR.ordinal()] = 47;
        } catch (NoSuchFieldError e43) {
        }
        try {
            iArr[Action.PHOTOSHARE_COMBINE.ordinal()] = 48;
        } catch (NoSuchFieldError e44) {
        }
        try {
            iArr[Action.PHOTOSHARE_CONTACT.ordinal()] = 49;
        } catch (NoSuchFieldError e45) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_PEOPLE_TAG.ordinal()] = 50;
        } catch (NoSuchFieldError e46) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_SHARE.ordinal()] = 51;
        } catch (NoSuchFieldError e47) {
        }
        try {
            iArr[Action.PHOTOSHARE_DELETE.ordinal()] = 52;
        } catch (NoSuchFieldError e48) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD.ordinal()] = 2;
        } catch (NoSuchFieldError e49) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOADING.ordinal()] = 3;
        } catch (NoSuchFieldError e50) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD_START.ordinal()] = 53;
        } catch (NoSuchFieldError e51) {
        }
        try {
            iArr[Action.PHOTOSHARE_EDITSHARE.ordinal()] = 54;
        } catch (NoSuchFieldError e52) {
        }
        try {
            iArr[Action.PHOTOSHARE_EMAIL.ordinal()] = 55;
        } catch (NoSuchFieldError e53) {
        }
        try {
            iArr[Action.PHOTOSHARE_LINK.ordinal()] = 56;
        } catch (NoSuchFieldError e54) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_DOWNLOAD.ordinal()] = 57;
        } catch (NoSuchFieldError e55) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_UPLOAD.ordinal()] = 58;
        } catch (NoSuchFieldError e56) {
        }
        try {
            iArr[Action.PHOTOSHARE_MESSAGE.ordinal()] = 59;
        } catch (NoSuchFieldError e57) {
        }
        try {
            iArr[Action.PHOTOSHARE_MOVE.ordinal()] = 60;
        } catch (NoSuchFieldError e58) {
        }
        try {
            iArr[Action.PHOTOSHARE_MULTI_DOWNLOAD.ordinal()] = 61;
        } catch (NoSuchFieldError e59) {
        }
        try {
            iArr[Action.PHOTOSHARE_NOT_THIS_PERSON.ordinal()] = 62;
        } catch (NoSuchFieldError e60) {
        }
        try {
            iArr[Action.PHOTOSHARE_PAUSE.ordinal()] = 63;
        } catch (NoSuchFieldError e61) {
        }
        try {
            iArr[Action.PHOTOSHARE_REMOVE_PEOPLE_TAG.ordinal()] = 64;
        } catch (NoSuchFieldError e62) {
        }
        try {
            iArr[Action.PHOTOSHARE_RENAME.ordinal()] = 65;
        } catch (NoSuchFieldError e63) {
        }
        try {
            iArr[Action.PHOTOSHARE_SETTINGS.ordinal()] = 66;
        } catch (NoSuchFieldError e64) {
        }
        try {
            iArr[Action.PHOTOSHARE_UPLOAD_START.ordinal()] = 67;
        } catch (NoSuchFieldError e65) {
        }
        try {
            iArr[Action.PRINT.ordinal()] = 68;
        } catch (NoSuchFieldError e66) {
        }
        try {
            iArr[Action.RANGE_MEASURE.ordinal()] = 69;
        } catch (NoSuchFieldError e67) {
        }
        try {
            iArr[Action.RECYCLE_CLEAN_BIN.ordinal()] = 70;
        } catch (NoSuchFieldError e68) {
        }
        try {
            iArr[Action.RECYCLE_DELETE.ordinal()] = 4;
        } catch (NoSuchFieldError e69) {
        }
        try {
            iArr[Action.RECYCLE_RECOVERY.ordinal()] = 5;
        } catch (NoSuchFieldError e70) {
        }
        try {
            iArr[Action.REDO.ordinal()] = 71;
        } catch (NoSuchFieldError e71) {
        }
        try {
            iArr[Action.REMOVE.ordinal()] = 72;
        } catch (NoSuchFieldError e72) {
        }
        try {
            iArr[Action.RENAME.ordinal()] = 73;
        } catch (NoSuchFieldError e73) {
        }
        try {
            iArr[Action.RE_SEARCH.ordinal()] = 74;
        } catch (NoSuchFieldError e74) {
        }
        try {
            iArr[Action.ROTATE_LEFT.ordinal()] = 75;
        } catch (NoSuchFieldError e75) {
        }
        try {
            iArr[Action.ROTATE_RIGHT.ordinal()] = 76;
        } catch (NoSuchFieldError e76) {
        }
        try {
            iArr[Action.SAVE.ordinal()] = 77;
        } catch (NoSuchFieldError e77) {
        }
        try {
            iArr[Action.SAVE_BURST.ordinal()] = 78;
        } catch (NoSuchFieldError e78) {
        }
        try {
            iArr[Action.SEE_BARCODE_INFO.ordinal()] = 79;
        } catch (NoSuchFieldError e79) {
        }
        try {
            iArr[Action.SETAS.ordinal()] = 80;
        } catch (NoSuchFieldError e80) {
        }
        try {
            iArr[Action.SETAS_BOTH.ordinal()] = 81;
        } catch (NoSuchFieldError e81) {
        }
        try {
            iArr[Action.SETAS_FIXED.ordinal()] = 82;
        } catch (NoSuchFieldError e82) {
        }
        try {
            iArr[Action.SETAS_FIXED_ACTIVED.ordinal()] = 83;
        } catch (NoSuchFieldError e83) {
        }
        try {
            iArr[Action.SETAS_HOME.ordinal()] = 84;
        } catch (NoSuchFieldError e84) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE.ordinal()] = 85;
        } catch (NoSuchFieldError e85) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE_ACTIVED.ordinal()] = 86;
        } catch (NoSuchFieldError e86) {
        }
        try {
            iArr[Action.SETAS_UNLOCK.ordinal()] = 87;
        } catch (NoSuchFieldError e87) {
        }
        try {
            iArr[Action.SETTINGS.ordinal()] = 88;
        } catch (NoSuchFieldError e88) {
        }
        try {
            iArr[Action.SHARE.ordinal()] = 89;
        } catch (NoSuchFieldError e89) {
        }
        try {
            iArr[Action.SHOW_ON_MAP.ordinal()] = 90;
        } catch (NoSuchFieldError e90) {
        }
        try {
            iArr[Action.SINGLE_SELECTION.ordinal()] = 91;
        } catch (NoSuchFieldError e91) {
        }
        try {
            iArr[Action.SINGLE_SELECTION_ON.ordinal()] = 92;
        } catch (NoSuchFieldError e92) {
        }
        try {
            iArr[Action.SLIDESHOW.ordinal()] = 93;
        } catch (NoSuchFieldError e93) {
        }
        try {
            iArr[Action.STORY_ALBUM_REMOVE.ordinal()] = 94;
        } catch (NoSuchFieldError e94) {
        }
        try {
            iArr[Action.STORY_ITEM_REMOVE.ordinal()] = 95;
        } catch (NoSuchFieldError e95) {
        }
        try {
            iArr[Action.STORY_RENAME.ordinal()] = 96;
        } catch (NoSuchFieldError e96) {
        }
        try {
            iArr[Action.TIME.ordinal()] = 97;
        } catch (NoSuchFieldError e97) {
        }
        try {
            iArr[Action.TOGIF.ordinal()] = 98;
        } catch (NoSuchFieldError e98) {
        }
        try {
            iArr[Action.UNDO.ordinal()] = 99;
        } catch (NoSuchFieldError e99) {
        }
        try {
            iArr[Action.WITHOUT_UPDATE.ordinal()] = 100;
        } catch (NoSuchFieldError e100) {
        }
        try {
            iArr[Action.WITH_UPDATE.ordinal()] = 101;
        } catch (NoSuchFieldError e101) {
        }
        -com-huawei-gallery-actionbar-ActionSwitchesValues = iArr;
        return iArr;
    }

    protected void onCreate(Bundle data, Bundle storedState) {
        super.onCreate(data, storedState);
        this.mDownloadListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (-1 == which) {
                    GalleryMediaPhotoPage.this.photoShareDownLoadOrigin();
                }
            }
        };
        this.mAllowDataAccessListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    GalleryMediaPhotoPage.this.addDownloadOriginTask(true);
                }
            }
        };
    }

    public void onPhotoTranslationChange(float x, float y, int index, boolean visible, MediaItem item) {
        boolean z = false;
        boolean lcdDownloaded = true;
        if (item != null && (item instanceof GalleryMediaItem)) {
            lcdDownloaded = ((GalleryMediaItem) item).isLCDDownloaded();
        }
        PhotoPageLoadingManager photoPageLoadingManager = this.mManager;
        float width = x - (((float) this.mPhotoView.getWidth()) / 2.0f);
        if (visible && !r0 && this.mIsActive && !this.mIsDownloadFailed) {
            z = true;
        }
        photoPageLoadingManager.onPhotoTranslationChange(width, index, z);
    }

    public void onSlidePicture() {
        super.onSlidePicture();
        onPhotoSharePhotoChanged();
        this.mManager.onSlidePicture();
    }

    protected void onResume() {
        super.onResume();
        if (this.mManager == null) {
            this.mManager = new PhotoPageLoadingManager(this.mHost.getActivity());
        }
        this.mManager.onResume();
        PhotoShareUtils.addListener(this);
        setPhotoShareDownLoadGone();
    }

    protected void onPause() {
        super.onPause();
        if (this.mManager != null) {
            this.mManager.onPause();
        }
        PhotoShareUtils.removeListener(this);
        setPhotoShareDownLoadGone();
    }

    protected boolean updateCurrentPhoto(MediaItem photo) {
        boolean changed = super.updateCurrentPhoto(photo);
        if (changed) {
            this.mNeedToPlayAfterDownLoad = false;
        } else if (this.mNeedToPlayAfterDownLoad && ((GalleryMediaItem) this.mCurrentPhoto).getLocalMediaId() != -1) {
            this.mNeedToPlayAfterDownLoad = false;
            playVideo(this.mHost.getActivity(), this.mCurrentPhoto.getPlayUri(), this.mCurrentPhoto.getName());
        }
        if (this.mCurrentPhoto == null || ((this.mCurrentPhoto instanceof GalleryImage) && !((GalleryImage) this.mCurrentPhoto).isLCDDownloaded())) {
            hideBars(false);
        }
        return changed;
    }

    protected void showBars(boolean barWithAnim) {
        if (this.mCurrentPhoto != null && (!(this.mCurrentPhoto instanceof GalleryImage) || ((GalleryImage) this.mCurrentPhoto).isLCDDownloaded() || this.mIsDownloadFailed)) {
            super.showBars(barWithAnim);
        }
    }

    protected void onPhotoSharePhotoChanged() {
        boolean z = true;
        if (this.mCurrentPhoto != null) {
            GalleryMediaItem galleryMediaItem = this.mCurrentPhoto;
            FileInfo fileInfo = galleryMediaItem.getFileInfo();
            FileInfo[] info = new FileInfo[]{fileInfo};
            if (galleryMediaItem.getLocalMediaId() != -1 || TextUtils.isEmpty(fileInfo.getFileId())) {
                setPhotoShareDownLoadGone();
                return;
            }
            boolean downloadOrigin = false;
            int thumbType = galleryMediaItem.getThumbType();
            int addDownLoadResult = 1;
            try {
                if (PhotoShareUtils.getServer().isGeneralFileDownloading(info, 0) == 0) {
                    addDownLoadResult = PhotoShareUtils.getServer().downloadPhotoThumb(info, 0, 1, false);
                    downloadOrigin = true;
                    GalleryLog.v("GalleryMediaPhotoPage", "download origin FileName = " + fileInfo.getFileName());
                }
                if (!downloadOrigin && galleryMediaItem.getLocalMediaId() == -1 && thumbType < 2) {
                    addDownLoadResult = PhotoShareUtils.getServer().downloadPhotoThumb(info, 1, 1, false);
                    if (addDownLoadResult == 0) {
                        z = false;
                    }
                    this.mIsDownloadFailed = z;
                    GalleryLog.v("GalleryMediaPhotoPage", "download lcd FileName = " + fileInfo.getFileName());
                    StorageService.checkStorageSpace();
                }
            } catch (RemoteException e) {
                PhotoShareUtils.dealRemoteException(e);
            }
            if (addDownLoadResult != 0) {
                setPhotoShareDownLoadGone();
                this.mActionProgressActionListener.onEnd();
            } else if (downloadOrigin) {
                enableReNameMenu(false);
            }
        }
    }

    public void downloadProgress(String hash, String id, String uniqueId, int thumbType, Long totalSize, Long currentSize) {
        if (this.mCurrentPhoto != null) {
            GalleryMediaItem item = this.mCurrentPhoto;
            if (thumbType != 1 || this.mCurrentPhoto.getFileInfo().getFileType() != 4) {
                FileInfo fileInfo = item.getFileInfo();
                if (!TextUtils.isEmpty(fileInfo.getFileId())) {
                    String albumId = fileInfo.getAlbumId();
                    if (fileInfo.getHash().equals(hash) && albumId.equals(id) && PhotoShareUtils.checkUniqueId(uniqueId, fileInfo) && !inEditorMode()) {
                        setPhotoShareDownLoadVisibility(totalSize, currentSize, thumbType, fileInfo.getFileType(), hash, id);
                    }
                }
            }
        }
    }

    protected boolean onItemSelected(Action action) {
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
                if (needDownloadOrigin()) {
                    return true;
                }
                setPhotoShareDownLoadGone();
                return super.onItemSelected(action);
            case 2:
                photoShareDownLoadOrigin();
                return true;
            case 3:
                cancelDownloading();
                return true;
            case 4:
                if (this.mPhotoView.isExtraActionDoing()) {
                    GalleryLog.d("GalleryMediaPhotoPage", "but ExtraAction is doing!");
                } else {
                    delete();
                }
                return true;
            case 5:
                if (this.mPhotoView.isExtraActionDoing()) {
                    GalleryLog.d("GalleryMediaPhotoPage", "but ExtraAction is doing!");
                } else {
                    recovery();
                }
                return true;
            default:
                return super.onItemSelected(action);
        }
    }

    private boolean needDownloadOrigin() {
        if (this.mCurrentPhoto == null) {
            return false;
        }
        Context context = this.mHost.getActivity();
        if (context == null) {
            return false;
        }
        GalleryMediaItem galleryMediaItem = this.mCurrentPhoto;
        boolean isVideo = galleryMediaItem.isVideo();
        if (galleryMediaItem.getLocalMediaId() != -1 || !isVideo) {
            return false;
        }
        PhotoShareUtils.getPhotoShareDialog(context, context.getString(R.string.download_title, new Object[]{Formatter.formatFileSize(context, galleryMediaItem.getFileInfo().getSize())}), (int) R.string.photoshare_download_short, (int) R.string.cancel, context.getString(R.string.download_video_when_edit, new Object[]{Formatter.formatFileSize(context, galleryMediaItem.getFileInfo().getSize())}), this.mDownloadListener).show();
        return true;
    }

    public boolean photoShareDownLoadOrigin() {
        Context context = this.mHost.getActivity();
        if (context == null) {
            return false;
        }
        if (!PhotoShareUtils.isNetworkConnected(context)) {
            ContextedUtils.showToastQuickly(context, (int) R.string.photoshare_toast_nonetwork, 0);
            return false;
        } else if (!PhotoShareUtils.isMobileNetConnected(context)) {
            return addDownloadOriginTask(false);
        } else {
            new Builder(context).setTitle(R.string.mobile_data_download_title).setMessage(context.getString(R.string.mobile_data_download_content, new Object[]{Formatter.formatFileSize(context, this.mCurrentPhoto.getFileInfo().getSize())})).setPositiveButton(R.string.photoshare_download_short, this.mAllowDataAccessListener).setNegativeButton(R.string.cancel, this.mAllowDataAccessListener).show();
            return false;
        }
    }

    protected void onPlayVideo(MediaItem origin) {
        if (origin instanceof GalleryVideo) {
            GalleryVideo item = (GalleryVideo) origin;
            if (item.isOnlyCloudItem() && item.isRecycleItem()) {
                ContextedUtils.showToastQuickly(this.mHost.getActivity(), (int) R.string.toast_playvideoinrecentlydeletedalbum, 0);
            }
            if (!item.isOnlyCloudItem() || item.isRecycleItem()) {
                super.onPlayVideo(origin);
            } else {
                OnClickListener dialogButtonListener = new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (-1 == which && GalleryMediaPhotoPage.this.photoShareDownLoadOrigin()) {
                            GalleryMediaPhotoPage.this.mNeedToPlayAfterDownLoad = true;
                        }
                    }
                };
                PhotoShareUtils.getPhotoShareDialog(this.mHost.getActivity(), this.mHost.getActivity().getString(R.string.download_title, new Object[]{Formatter.formatFileSize(this.mHost.getActivity(), item.getFileInfo().getSize())}), (int) R.string.photoshare_download_short, (int) R.string.cancel, this.mHost.getActivity().getString(R.string.download_video_when_play, new Object[]{Formatter.formatFileSize(this.mHost.getActivity(), item.getFileInfo().getSize())}), dialogButtonListener).show();
            }
        }
    }

    public boolean addDownloadOriginTask(boolean canUseMobileNetWork) {
        FileInfo fileInfo = this.mCurrentPhoto.getFileInfo();
        int addDownLoadResult = 1;
        try {
            addDownLoadResult = PhotoShareUtils.getServer().downloadPhotoThumb(new FileInfo[]{fileInfo}, 0, 1, canUseMobileNetWork);
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        getGalleryActionBar().setProgress(0);
        if (addDownLoadResult == 0) {
            setPhotoShareDownLoadVisibility(Long.valueOf(0), Long.valueOf(0), 0, fileInfo.getFileType(), fileInfo.getHash(), fileInfo.getAlbumId());
            enableReNameMenu(false);
        }
        if (addDownLoadResult == 0) {
            return true;
        }
        return false;
    }

    private void enableReNameMenu(boolean enable) {
        ActionBarStateBase actionBarStateBase = this.mActionBar.getCurrentMode();
        if (actionBarStateBase != null) {
            actionBarStateBase.setActionEnable(enable, Action.ACTION_ID_RENAME);
        }
    }

    private void setPhotoShareDownLoadVisibility(Long totalSize, Long currentSize, int thumbType, int fileType, String hash, String id) {
        if (this.mHost.getActivity() != null) {
            if (thumbType != 1 || fileType != 4) {
                getGalleryActionBar().setProgress(totalSize.longValue() == 0 ? 0 : (int) ((currentSize.longValue() * 100) / totalSize.longValue()));
                ((GalleryMediaItem) this.mCurrentPhoto).mDownloadState = 1;
                getGalleryActionBar().getCurrentMode().changeAction(Action.PHOTOSHARE_DOWNLOAD.ordinal(), Action.PHOTOSHARE_DOWNLOADING.ordinal());
                this.mActionProgressActionListener.onStart();
            }
        }
    }

    protected boolean noActionBar() {
        if (this.mCurrentPhoto == null || !(this.mCurrentPhoto instanceof GalleryImage)) {
            return super.noActionBar();
        }
        boolean z = super.noActionBar() || !((GalleryImage) this.mCurrentPhoto).isLCDDownloaded();
        return z;
    }

    public void downloadFinish(String hash, String albumId, String uniqueId, int thumbType, int result) {
        if (this.mCurrentPhoto != null && hash != null && albumId != null) {
            if (thumbType == 1 && this.mCurrentPhoto.getFileInfo().getFileType() == 4) {
                this.mIsDownloadFailed = true;
                return;
            }
            GalleryMediaItem item = this.mCurrentPhoto;
            FileInfo fileInfo = item.getFileInfo();
            if (!TextUtils.isEmpty(fileInfo.getFileId()) && albumId.equals(fileInfo.getAlbumId()) && hash.equals(fileInfo.getHash()) && PhotoShareUtils.checkUniqueId(uniqueId, fileInfo)) {
                setPhotoShareDownLoadGone();
                if (this.mHost.getActivity() != null) {
                    if (result != 0) {
                        ContextedUtils.showToastQuickly(this.mHost.getActivity(), (int) R.string.photoshare_download_fail_tips, 0);
                        item.mDownloadState = 0;
                        if (thumbType == 1) {
                            this.mIsDownloadFailed = true;
                        }
                    } else if (((GalleryMediaItem) this.mCurrentPhoto).mDownloadState == 1 && getGalleryActionBar().getProgress() > 0) {
                        item.mDownloadState = 2;
                        this.mActionProgressActionListener.onEnd();
                    }
                }
            }
        }
    }

    private void setPhotoShareDownLoadGone() {
        enableReNameMenu(true);
    }

    private void cancelDownloading() {
        if (this.mCurrentPhoto != null) {
            FileInfo fileInfo = this.mCurrentPhoto.getFileInfo();
            if (!TextUtils.isEmpty(fileInfo.getFileId())) {
                try {
                    GalleryLog.v("GalleryMediaPhotoPage", "cancelDownloading albumId " + fileInfo.getAlbumId() + " hash " + fileInfo.getHash());
                    GalleryLog.v("GalleryMediaPhotoPage", "cancel DownloadTask result" + PhotoShareUtils.getServer().cancelDownloadPhotoThumb(new FileInfo[]{fileInfo}, 0));
                } catch (RemoteException e) {
                    PhotoShareUtils.dealRemoteException(e);
                }
                ((GalleryMediaItem) this.mCurrentPhoto).mDownloadState = 0;
                getGalleryActionBar().getCurrentMode().changeAction(Action.PHOTOSHARE_DOWNLOADING.ordinal(), Action.PHOTOSHARE_DOWNLOAD.ordinal());
                this.mActionProgressActionListener.onEnd();
                setPhotoShareDownLoadGone();
            }
        }
    }

    protected void onInflateMenu(ActionBarStateBase mode) {
        if (this.mCurrentPhoto == null) {
            super.onInflateMenu(mode);
            return;
        }
        GalleryMediaItem galleryMediaItem = this.mCurrentPhoto;
        FileInfo fileInfo = galleryMediaItem.getFileInfo();
        if (this.mMediaSet instanceof GalleryRecycleAlbum) {
            mode.setMenu(Math.min(5, this.mRecycledBinMenu.length), this.mRecycledBinMenu);
        } else if (galleryMediaItem.getLocalMediaId() != -1 || TextUtils.isEmpty(fileInfo.getFileId()) || ((GalleryMediaItem) this.mCurrentPhoto).mDownloadState == 2) {
            mode.setMenu(Math.min(5, this.mOriginDownloadedMenu.length), this.mOriginDownloadedMenu);
        } else {
            mode.setMenu(Math.min(5, this.mNeedDownloadMenu.length), this.mNeedDownloadMenu);
            if (((GalleryMediaItem) this.mCurrentPhoto).mDownloadState == 1) {
                getGalleryActionBar().getCurrentMode().changeAction(Action.PHOTOSHARE_DOWNLOAD.ordinal(), Action.PHOTOSHARE_DOWNLOADING.ordinal());
            }
        }
    }

    protected void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        if (this.mManager != null) {
            this.mManager.onConfigurationChanged();
        }
    }

    private void delete() {
        MediaItem current = this.mModel.getMediaItem(0);
        if (current != null) {
            Path path = current.getPath();
            Bundle bundle = new Bundle();
            bundle.putInt("recycle_flag", 3);
            this.mSelectionManager.deSelectAll();
            this.mSelectionManager.toggle(path);
            this.mMenuExecutor.onMenuClicked(Action.RECYCLE_DELETE, this.mHost.getActivity().getResources().getQuantityString(R.plurals.delete_recycled_photo_msg, this.mSelectionManager.getSelectedCount()), this.mHost.getActivity().getString(R.string.photoshare_allow_title), this.mConfirmDialogListener, bundle);
        }
    }

    private void recovery() {
        MediaItem current = this.mModel.getMediaItem(0);
        if (current != null) {
            Path path = current.getPath();
            Bundle bundle = new Bundle();
            this.mConfirmDialogListener.setOnCompleteToastContent(this.mHost.getGalleryContext().getResources().getQuantityString(R.plurals.toast_restorephoto01, 1, new Object[]{Integer.valueOf(1)}));
            bundle.putInt("recycle_flag", 1);
            this.mSelectionManager.deSelectAll();
            this.mSelectionManager.toggle(path);
            this.mMenuExecutor.onMenuClicked(Action.RECYCLE_RECOVERY, this.mHost.getActivity().getString(R.string.toolbarbutton_recover), null, this.mConfirmDialogListener, bundle);
        }
    }
}
