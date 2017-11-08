package com.huawei.gallery.burst;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.GalleryMediaItem;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReportToBigData;
import com.android.gallery3d.util.ThreadPool;
import com.huawei.android.cg.vo.FileInfo;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.actionbar.SelectionMode;
import com.huawei.gallery.app.plugin.PhotoExtraButton;
import com.huawei.gallery.app.plugin.PhotoFragmentPlugin;
import com.huawei.gallery.burst.BurstActionExecutor.ExecutorListener;
import com.huawei.gallery.burst.BurstDataLoader.Listener;
import com.huawei.gallery.burst.BurstThumbnailLoader.ThumbNailListener;
import com.huawei.gallery.burst.ui.BurstViewController;
import com.huawei.gallery.media.GalleryMedia;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import java.io.Closeable;
import java.util.ArrayList;

public class BurstPhotoManager extends PhotoFragmentPlugin implements Listener, ThumbNailListener, ExecutorListener, OnClickListener {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    private static final String[] FILEINFO_PROJECTION = new String[]{"_display_name", "fileType", "hash", "cloud_bucket_id", "datetaken", "expand", "localThumbPath", "localBigThumbPath", "videoThumbId", "fileId", "_size", "source"};
    private Action mAction;
    private SelectionMode mActionBar;
    private BurstActionExecutor mActionExecutor = new BurstActionExecutor(this);
    private MediaItem mBurstCoverItem;
    private BurstPhotoSet mBurstPhotoSet = null;
    private BurstViewController mBurstView;
    private Path mBustCover = null;
    private AlertDialog mConfirmDialog;
    private BurstDataLoader mDataLoader;
    private boolean mDeleteBurst;
    private OnClickListener mDownloadListener;
    private Path mFocusTarget = null;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                case 3:
                    BurstPhotoManager.this.leaveBurstMode();
                    return;
                case 2:
                    ContextedUtils.showToastQuickly(BurstPhotoManager.this.mContext.getActivityContext(), (int) R.string.paste_nospace_title, 1);
                    return;
                case 4:
                    BurstPhotoManager.this.mStarting = false;
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mIsInBurstMode = false;
    private BurstSelectManager mSelectManager = new BurstSelectManager();
    private boolean mStarting = false;
    private BurstThumbnailLoader mThumbNailLoader;

    private static /* synthetic */ int[] -getcom-huawei-gallery-actionbar-ActionSwitchesValues() {
        if (-com-huawei-gallery-actionbar-ActionSwitchesValues != null) {
            return -com-huawei-gallery-actionbar-ActionSwitchesValues;
        }
        int[] iArr = new int[Action.values().length];
        try {
            iArr[Action.ADD.ordinal()] = 2;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Action.ADD_ALBUM.ordinal()] = 3;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Action.ADD_COMMENT.ordinal()] = 4;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Action.AIRSHARE.ordinal()] = 5;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Action.ALBUM.ordinal()] = 6;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Action.ALL.ordinal()] = 7;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Action.BACK.ordinal()] = 8;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Action.CANCEL_DETAIL.ordinal()] = 9;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Action.COLLAGE.ordinal()] = 10;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Action.COMMENT.ordinal()] = 11;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Action.COPY.ordinal()] = 12;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Action.DEALL.ordinal()] = 13;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Action.DEL.ordinal()] = 14;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Action.DETAIL.ordinal()] = 15;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[Action.DYNAMIC_ALBUM.ordinal()] = 16;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[Action.EDIT.ordinal()] = 17;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[Action.EDIT_COMMENT.ordinal()] = 18;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[Action.GOTO_GALLERY.ordinal()] = 19;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[Action.HIDE.ordinal()] = 20;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[Action.INFO.ordinal()] = 21;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[Action.KEYGUARD_LIKE.ordinal()] = 22;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[Action.KEYGUARD_NOT_LIKE.ordinal()] = 23;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[Action.LOOPPLAY.ordinal()] = 24;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[Action.MAP.ordinal()] = 25;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[Action.MENU.ordinal()] = 26;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[Action.MORE_EDIT.ordinal()] = 27;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[Action.MOVE.ordinal()] = 28;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[Action.MOVEIN.ordinal()] = 29;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[Action.MOVEOUT.ordinal()] = 30;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[Action.MULTISCREEN.ordinal()] = 31;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[Action.MULTISCREEN_ACTIVITED.ordinal()] = 32;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[Action.MULTI_SELECTION.ordinal()] = 33;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[Action.MULTI_SELECTION_ON.ordinal()] = 34;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[Action.MYFAVORITE.ordinal()] = 35;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[Action.NO.ordinal()] = 36;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[Action.NONE.ordinal()] = 37;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[Action.NOT_MYFAVORITE.ordinal()] = 38;
        } catch (NoSuchFieldError e37) {
        }
        try {
            iArr[Action.OK.ordinal()] = 39;
        } catch (NoSuchFieldError e38) {
        }
        try {
            iArr[Action.PHOTOSHARE_ACCOUNT.ordinal()] = 40;
        } catch (NoSuchFieldError e39) {
        }
        try {
            iArr[Action.PHOTOSHARE_ADDPICTURE.ordinal()] = 41;
        } catch (NoSuchFieldError e40) {
        }
        try {
            iArr[Action.PHOTOSHARE_BACKUP.ordinal()] = 42;
        } catch (NoSuchFieldError e41) {
        }
        try {
            iArr[Action.PHOTOSHARE_CANCEL_RECEIVE.ordinal()] = 43;
        } catch (NoSuchFieldError e42) {
        }
        try {
            iArr[Action.PHOTOSHARE_CLEAR.ordinal()] = 44;
        } catch (NoSuchFieldError e43) {
        }
        try {
            iArr[Action.PHOTOSHARE_COMBINE.ordinal()] = 45;
        } catch (NoSuchFieldError e44) {
        }
        try {
            iArr[Action.PHOTOSHARE_CONTACT.ordinal()] = 46;
        } catch (NoSuchFieldError e45) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_PEOPLE_TAG.ordinal()] = 47;
        } catch (NoSuchFieldError e46) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_SHARE.ordinal()] = 48;
        } catch (NoSuchFieldError e47) {
        }
        try {
            iArr[Action.PHOTOSHARE_DELETE.ordinal()] = 49;
        } catch (NoSuchFieldError e48) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD.ordinal()] = 50;
        } catch (NoSuchFieldError e49) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOADING.ordinal()] = 51;
        } catch (NoSuchFieldError e50) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD_START.ordinal()] = 52;
        } catch (NoSuchFieldError e51) {
        }
        try {
            iArr[Action.PHOTOSHARE_EDITSHARE.ordinal()] = 53;
        } catch (NoSuchFieldError e52) {
        }
        try {
            iArr[Action.PHOTOSHARE_EMAIL.ordinal()] = 54;
        } catch (NoSuchFieldError e53) {
        }
        try {
            iArr[Action.PHOTOSHARE_LINK.ordinal()] = 55;
        } catch (NoSuchFieldError e54) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_DOWNLOAD.ordinal()] = 56;
        } catch (NoSuchFieldError e55) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_UPLOAD.ordinal()] = 57;
        } catch (NoSuchFieldError e56) {
        }
        try {
            iArr[Action.PHOTOSHARE_MESSAGE.ordinal()] = 58;
        } catch (NoSuchFieldError e57) {
        }
        try {
            iArr[Action.PHOTOSHARE_MOVE.ordinal()] = 59;
        } catch (NoSuchFieldError e58) {
        }
        try {
            iArr[Action.PHOTOSHARE_MULTI_DOWNLOAD.ordinal()] = 60;
        } catch (NoSuchFieldError e59) {
        }
        try {
            iArr[Action.PHOTOSHARE_NOT_THIS_PERSON.ordinal()] = 61;
        } catch (NoSuchFieldError e60) {
        }
        try {
            iArr[Action.PHOTOSHARE_PAUSE.ordinal()] = 62;
        } catch (NoSuchFieldError e61) {
        }
        try {
            iArr[Action.PHOTOSHARE_REMOVE_PEOPLE_TAG.ordinal()] = 63;
        } catch (NoSuchFieldError e62) {
        }
        try {
            iArr[Action.PHOTOSHARE_RENAME.ordinal()] = 64;
        } catch (NoSuchFieldError e63) {
        }
        try {
            iArr[Action.PHOTOSHARE_SETTINGS.ordinal()] = 65;
        } catch (NoSuchFieldError e64) {
        }
        try {
            iArr[Action.PHOTOSHARE_UPLOAD_START.ordinal()] = 66;
        } catch (NoSuchFieldError e65) {
        }
        try {
            iArr[Action.PRINT.ordinal()] = 67;
        } catch (NoSuchFieldError e66) {
        }
        try {
            iArr[Action.RANGE_MEASURE.ordinal()] = 68;
        } catch (NoSuchFieldError e67) {
        }
        try {
            iArr[Action.RECYCLE_CLEAN_BIN.ordinal()] = 69;
        } catch (NoSuchFieldError e68) {
        }
        try {
            iArr[Action.RECYCLE_DELETE.ordinal()] = 70;
        } catch (NoSuchFieldError e69) {
        }
        try {
            iArr[Action.RECYCLE_RECOVERY.ordinal()] = 71;
        } catch (NoSuchFieldError e70) {
        }
        try {
            iArr[Action.REDO.ordinal()] = 72;
        } catch (NoSuchFieldError e71) {
        }
        try {
            iArr[Action.REMOVE.ordinal()] = 73;
        } catch (NoSuchFieldError e72) {
        }
        try {
            iArr[Action.RENAME.ordinal()] = 74;
        } catch (NoSuchFieldError e73) {
        }
        try {
            iArr[Action.RE_SEARCH.ordinal()] = 75;
        } catch (NoSuchFieldError e74) {
        }
        try {
            iArr[Action.ROTATE_LEFT.ordinal()] = 76;
        } catch (NoSuchFieldError e75) {
        }
        try {
            iArr[Action.ROTATE_RIGHT.ordinal()] = 77;
        } catch (NoSuchFieldError e76) {
        }
        try {
            iArr[Action.SAVE.ordinal()] = 78;
        } catch (NoSuchFieldError e77) {
        }
        try {
            iArr[Action.SAVE_BURST.ordinal()] = 1;
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

    public BurstPhotoManager(ViewGroup parentLayout, GalleryContext context) {
        super(context);
        this.mBurstView = new BurstViewController(parentLayout, context.getAndroidContext(), this);
        this.mDownloadListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (-1 == which) {
                    BurstPhotoManager.this.downloadBurstPhoto();
                }
            }
        };
    }

    private void downloadBurstPhoto() {
        Context context = this.mContext.getAndroidContext();
        if (PhotoShareUtils.isNetworkConnected(context)) {
            ArrayList<FileInfo> fileInfoArrayList = getBurstPhotoFileInfo(context.getContentResolver(), this.mBurstCoverItem.getBurstSetPath().getSuffix(), this.mBurstCoverItem.getBurstSetPath().getParent().getParent().getSuffix(), 1);
            try {
                PhotoShareUtils.getServer().downloadPhotoThumb((FileInfo[]) fileInfoArrayList.toArray(new FileInfo[fileInfoArrayList.size()]), 0, 0, false);
            } catch (RemoteException e) {
                PhotoShareUtils.dealRemoteException(e);
            }
            return;
        }
        ContextedUtils.showToastQuickly(context, (int) R.string.photoshare_toast_nonetwork, 0);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ArrayList<FileInfo> getBurstPhotoFileInfo(ContentResolver contentResolver, String burstId, String bucketId, int queryKind) {
        ArrayList<FileInfo> fileInfoArrayList = new ArrayList();
        String whereClause = "bucket_id = ? AND _display_name LIKE ?||'_BURST%.JPG' ";
        switch (queryKind) {
            case 1:
                whereClause = whereClause + " AND local_media_id =-1";
                break;
            case 2:
                whereClause = whereClause + " AND cloud_media_id !=-1";
                break;
        }
        try {
            Closeable cursor = contentResolver.query(GalleryMedia.URI, FILEINFO_PROJECTION, whereClause, new String[]{bucketId, burstId}, null);
            while (cursor != null && cursor.moveToNext()) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFileName(cursor.getString(0));
                fileInfo.setFileType(cursor.getInt(1));
                fileInfo.setHash(cursor.getString(2));
                fileInfo.setAlbumId(cursor.getString(3));
                fileInfo.setAddTime(cursor.getLong(4));
                fileInfo.setExpand(cursor.getString(5));
                fileInfo.setLocalThumbPath(cursor.getString(6));
                fileInfo.setLocalBigThumbPath(cursor.getString(7));
                fileInfo.setVideoThumbId(cursor.getString(8));
                fileInfo.setFileId(cursor.getString(9));
                fileInfo.setSize(cursor.getLong(10));
                fileInfo.setShareId("");
                String source = cursor.getString(11);
                if (TextUtils.isEmpty(source)) {
                    source = "UNKNOW";
                }
                fileInfo.setSource(source);
                fileInfoArrayList.add(fileInfo);
            }
            Utils.closeSilently(cursor);
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        return fileInfoArrayList;
    }

    private void createActionBar(GalleryActionBar actionBar) {
        actionBar.setActionBarVisible(true);
        this.mActionBar = actionBar.enterSelectionMode(true);
        this.mActionBar.setTitle((int) R.string.select_image);
        setActionBarCount(0);
        this.mActionBar.setBothAction(Action.NO, Action.SAVE_BURST);
        this.mActionBar.show();
        actionBar.setActionPanelVisible(false, false);
        this.mActionBar.getRightActionItem().asView().setEnabled(false);
    }

    private void setActionBarCount(int cnt) {
        int maxCount = -1;
        if (this.mBurstPhotoSet != null) {
            maxCount = this.mBurstPhotoSet.getMediaItemCount();
        }
        if (maxCount > 0) {
            this.mActionBar.setCount(cnt, maxCount);
        } else {
            this.mActionBar.setCount(cnt);
        }
    }

    private void enterBurstMode(MediaItem burstCoverItem) {
        this.mIsInBurstMode = true;
        this.mBustCover = burstCoverItem.getPath();
        Bitmap coverMicroThumbnail = (Bitmap) burstCoverItem.requestImage(2).run(ThreadPool.JOB_CONTEXT_STUB);
        this.mBurstView.enterBurstMode(BitmapUtils.rotateBitmap((Bitmap) burstCoverItem.requestImage(1).run(ThreadPool.JOB_CONTEXT_STUB), burstCoverItem.getRotation(), true), coverMicroThumbnail);
        this.mBurstPhotoSet = (BurstPhotoSet) this.mContext.getDataManager().getMediaSet(burstCoverItem.getBurstSetPath());
        this.mDataLoader = new BurstDataLoader(this.mBurstPhotoSet, this);
        this.mDataLoader.resume();
        createActionBar(this.mFragmentPluginManager.getHost().getGalleryActionBar());
    }

    private void leaveBurstMode() {
        if (this.mIsInBurstMode) {
            this.mIsInBurstMode = false;
            this.mBurstView.leaveBurstMode();
            this.mBurstPhotoSet = null;
            this.mDataLoader.pause();
            this.mDataLoader = null;
            if (this.mThumbNailLoader != null) {
                this.mThumbNailLoader.pause();
            }
            this.mThumbNailLoader = null;
            this.mSelectManager.onDestroy();
            this.mFragmentPluginManager.getHost().getGalleryActionBar().leaveCurrentMode();
            this.mActionBar = null;
            Intent result = new Intent();
            if (this.mFocusTarget != null) {
                result.putExtra("focus-target", this.mFocusTarget.toString());
            }
            this.mFragmentPluginManager.getHost().onLeavePluginMode(1, result);
        }
    }

    public boolean onBackPressed() {
        if (!this.mIsInBurstMode) {
            return false;
        }
        leaveBurstMode();
        return true;
    }

    public void onPhotoChanged() {
    }

    public void onClick(DialogInterface dialog, int which) {
        if (this.mAction != null) {
            switch (which) {
                case -3:
                    this.mDeleteBurst = true;
                    ArrayList<MediaItem> items = this.mSelectManager.getSelectItems();
                    if (!(items == null || items.isEmpty())) {
                        this.mFocusTarget = ((MediaItem) items.get(0)).getPath();
                    }
                    this.mActionExecutor.startAction(this.mContext, this.mAction, items, null);
                    break;
                case -2:
                    this.mDeleteBurst = false;
                    this.mFocusTarget = this.mBustCover;
                    this.mActionExecutor.startAction(this.mContext, this.mAction, this.mSelectManager.getSelectItems(), null);
                    break;
                case -1:
                    this.mFocusTarget = null;
                    break;
            }
        }
    }

    public boolean isPending() {
        return this.mStarting;
    }

    private void showSaveSelectionDialogue(Action action) {
        if (this.mStarting) {
            GalleryLog.d("BurstPhotoManager", "dialog is starting !!!");
            return;
        }
        this.mAction = action;
        this.mStarting = true;
        Context context = this.mContext.getActivityContext();
        int selectedCount = this.mSelectManager.getToggleCount();
        int leftCount = Math.max(0, this.mThumbNailLoader.size() - selectedCount);
        String title = context.getResources().getQuantityString(R.plurals.burst_save_message_content, leftCount, new Object[]{Integer.valueOf(leftCount)});
        AlertDialog dialog = new Builder(context).setTitle(title).setNegativeButton(R.string.burst_save_items_all, this).setNeutralButton(context.getResources().getQuantityString(R.plurals.burst_save_items_selected, selectedCount, new Object[]{Integer.valueOf(selectedCount)}), this).setPositiveButton(R.string.burst_save_items_cancel, this).create();
        dialog.show();
        this.mHandler.sendEmptyMessageDelayed(4, 500);
        this.mConfirmDialog = dialog;
    }

    public boolean onInterceptActionItemClick(Action action) {
        if (!this.mIsInBurstMode) {
            return false;
        }
        if (this.mActionExecutor.isProcessing()) {
            return true;
        }
        if (action == Action.SAVE_BURST) {
            showSaveSelectionDialogue(action);
        } else if (action != Action.TOGIF && action == Action.NO) {
            leaveBurstMode();
        }
        return true;
    }

    public boolean onEventsHappens(MediaItem currentItem, View button) {
        if (isButtonDisabled(currentItem, button)) {
            return false;
        }
        this.mBurstCoverItem = currentItem;
        if ((currentItem instanceof GalleryMediaItem) && !canExpand()) {
            return false;
        }
        enterBurstMode(currentItem);
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean canExpand() {
        Context context = this.mContext.getActivityContext();
        int count = 0;
        long size = 0;
        String burstId = this.mBurstCoverItem.getBurstSetPath().getSuffix();
        String bucketId = this.mBurstCoverItem.getBurstSetPath().getParent().getParent().getSuffix();
        try {
            Closeable cursor = context.getContentResolver().query(GalleryMedia.URI, BurstPhotoSet.copyCountProjection(), "bucket_id = ? AND _display_name LIKE ?||'_BURST%.JPG'  AND local_media_id =-1", new String[]{bucketId, burstId}, null);
            if (cursor != null) {
                Utils.assertTrue(cursor.moveToNext());
                count = cursor.getInt(0);
                size = cursor.getLong(1);
            }
            Utils.closeSilently(cursor);
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        if (count <= 0) {
            return true;
        }
        Context context2 = context;
        PhotoShareUtils.getPhotoShareDialog(context2, context.getString(R.string.download_title, new Object[]{Formatter.formatFileSize(context, size)}), (int) R.string.photoshare_download_short, (int) R.string.cancel, context.getResources().getQuantityString(R.plurals.download_burst_when_expand, count, new Object[]{Integer.valueOf(count), Formatter.formatFileSize(context, size)}), this.mDownloadListener).show();
        return false;
    }

    public boolean updateExtraButton(PhotoExtraButton button, MediaItem currentItem) {
        if (isButtonDisabled(currentItem, button)) {
            return false;
        }
        button.setPhotoExtraButtonOverlay(null);
        button.setImageResource(R.drawable.ic_gallery_info_burst);
        button.setContentDescription(button.getContext().getString(R.string.view_title));
        return true;
    }

    public void onResume() {
        if (this.mDataLoader != null) {
            this.mDataLoader.resume();
        }
        if (this.mThumbNailLoader != null) {
            this.mThumbNailLoader.resume();
        }
    }

    public void onPause() {
        if (this.mDataLoader != null) {
            this.mDataLoader.pause();
        }
        if (this.mThumbNailLoader != null) {
            this.mThumbNailLoader.pause();
        }
        GalleryUtils.dismissDialogSafely(this.mConfirmDialog, null);
    }

    public void onLoadFinished(long oldDataVersion, long newDataVersion, ArrayList<MediaItem> items) {
        if (!this.mIsInBurstMode) {
            GalleryLog.d("BurstPhotoManager", "Already exit");
        } else if (this.mThumbNailLoader != null) {
            GalleryLog.d("BurstPhotoManager", "Already start");
        } else {
            this.mSelectManager.setData(items);
            this.mThumbNailLoader = new BurstThumbnailLoader(items, this);
            this.mThumbNailLoader.resume();
            this.mBurstView.setThumbnailLoader(this.mThumbNailLoader);
        }
    }

    public void onScreenNailLoaded(Bitmap thumbnail, Bitmap microThumNail, int index) {
        if (this.mIsInBurstMode) {
            this.mBurstView.updateView(thumbnail, microThumNail, index);
        }
    }

    public void onBestPhotoFound(int index) {
        if (this.mIsInBurstMode) {
            this.mBurstView.setBest(index);
        }
    }

    public boolean onToggle(int position) {
        boolean z = false;
        boolean result = this.mSelectManager.toggle(position);
        if (this.mActionBar != null) {
            setActionBarCount(this.mSelectManager.getToggleCount());
            if (this.mActionBar.getRightActionItem() != null) {
                View asView = this.mActionBar.getRightActionItem().asView();
                if (this.mSelectManager.getToggleCount() > 0) {
                    z = true;
                }
                asView.setEnabled(z);
            }
        }
        return result;
    }

    public BurstSelectManager getSelectManager() {
        return this.mSelectManager;
    }

    public void onActionDone(Action action, boolean success, Bundle data) {
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
                ReportToBigData.report(41);
                onSaveBurstDone(success, data);
                return;
            default:
                return;
        }
    }

    private void onSaveBurstDone(boolean success, Bundle data) {
        if (success) {
            Message msg = this.mHandler.obtainMessage(1);
            msg.arg1 = data.getInt("KEY_SUCCESS_COUNT", 0);
            msg.sendToTarget();
            if (this.mDeleteBurst) {
                this.mDeleteBurst = false;
                this.mBurstPhotoSet.delete(this.mContext);
            }
            return;
        }
        if (data.getInt("KEY_ERROR_CODE") == 1) {
            this.mHandler.sendEmptyMessage(2);
        } else {
            this.mHandler.sendEmptyMessage(3);
        }
    }

    private boolean isButtonDisabled(MediaItem currentItem, View button) {
        if (currentItem != null && currentItem.isBurstCover() && button.getId() == R.id.plugin_button) {
            return false;
        }
        return true;
    }
}
