package com.huawei.gallery.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.Menu;
import android.widget.EditText;
import com.android.gallery3d.R;
import com.android.gallery3d.data.CloudLocalAlbum;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.DataSourceType;
import com.android.gallery3d.data.DiscoverLocation;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.data.PhotoShareAlbum;
import com.android.gallery3d.data.PhotoShareSource;
import com.android.gallery3d.data.PhotoShareTagFile;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.android.cg.vo.FileInfo;
import com.huawei.android.cg.vo.TagFileInfo;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionMode;
import com.huawei.gallery.photoshare.ui.PhotoShareAlertDialogFragment;
import com.huawei.gallery.photoshare.ui.PhotoShareAlertDialogFragment.onDialogButtonClickListener;
import com.huawei.gallery.photoshare.ui.PhotoShareEditFriendsActivity;
import com.huawei.gallery.photoshare.ui.PhotoShareShowMemberActivity;
import com.huawei.gallery.photoshare.ui.PhotoShareTagAlbumSetActivity;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.ui.SlotView;
import com.huawei.gallery.ui.SlotView.AbsLayout;
import com.huawei.gallery.util.ColorfulUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class PhotoShareAlbumPage extends SlotAlbumPage {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    private static final Action[] GALLERY_MEDIA_MENU = new Action[]{Action.SHARE, Action.PHOTOSHARE_DELETE, Action.ALL, Action.NONE, Action.DETAIL};
    private static final Action[] PHOTOSHARE_LOCAL_MENU = new Action[]{Action.SHARE, Action.PHOTOSHARE_MULTI_DOWNLOAD, Action.PHOTOSHARE_DELETE, Action.ALL, Action.NONE, Action.PRINT, Action.DETAIL};
    private static final Action[] PHOTOSHARE_SHARE_MENU = new Action[]{Action.SHARE, Action.PHOTOSHARE_MULTI_DOWNLOAD, Action.PHOTOSHARE_DELETE, Action.ALL};
    private static final Action[] PHOTOSHARE_SHARE_NOPRESS_MENU = new Action[]{Action.PHOTOSHARE_ADDPICTURE, Action.PHOTOSHARE_CONTACT, Action.PHOTOSHARE_RENAME};
    private static final Action[] PHOTOSHARE_TAG_OTHER_CLASSIFY_MENU = new Action[]{Action.SHARE, Action.PHOTOSHARE_MOVE, Action.PHOTOSHARE_DELETE, Action.PHOTOSHARE_MULTI_DOWNLOAD, Action.ALL};
    private static final Action[] PHOTOSHARE_TAG_PEOPLE_MENU = new Action[]{Action.SHARE, Action.PHOTOSHARE_NOT_THIS_PERSON, Action.PHOTOSHARE_DELETE, Action.PHOTOSHARE_MULTI_DOWNLOAD, Action.ALL};
    private OnClickListener mAllowDataAccessListener;
    private AlertDialog mCreateDialog;
    private OnClickListener mDeleteClickListener;
    private AbsLayout mLayout;
    protected boolean mNeedFootBar = false;
    private int mOperationType = -1;
    private ProgressDialog mProgressDialog;
    private OnClickListener mReNameDialogButtonListener;
    private EditText mSetNameTextView;
    private int mSourceType;
    private HashMap<Object, Object> mVisibleIndexMap;
    private HashMap<Path, Object> mVisiblePathMap;

    private static /* synthetic */ int[] -getcom-huawei-gallery-actionbar-ActionSwitchesValues() {
        if (-com-huawei-gallery-actionbar-ActionSwitchesValues != null) {
            return -com-huawei-gallery-actionbar-ActionSwitchesValues;
        }
        int[] iArr = new int[Action.values().length];
        try {
            iArr[Action.ADD.ordinal()] = 9;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Action.ADD_ALBUM.ordinal()] = 10;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Action.ADD_COMMENT.ordinal()] = 11;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Action.AIRSHARE.ordinal()] = 12;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Action.ALBUM.ordinal()] = 13;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Action.ALL.ordinal()] = 14;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Action.BACK.ordinal()] = 15;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Action.CANCEL_DETAIL.ordinal()] = 16;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Action.COLLAGE.ordinal()] = 17;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Action.COMMENT.ordinal()] = 18;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Action.COPY.ordinal()] = 19;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Action.DEALL.ordinal()] = 20;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Action.DEL.ordinal()] = 21;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Action.DETAIL.ordinal()] = 22;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[Action.DYNAMIC_ALBUM.ordinal()] = 23;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[Action.EDIT.ordinal()] = 24;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[Action.EDIT_COMMENT.ordinal()] = 25;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[Action.GOTO_GALLERY.ordinal()] = 26;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[Action.HIDE.ordinal()] = 27;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[Action.INFO.ordinal()] = 28;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[Action.KEYGUARD_LIKE.ordinal()] = 29;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[Action.KEYGUARD_NOT_LIKE.ordinal()] = 30;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[Action.LOOPPLAY.ordinal()] = 31;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[Action.MAP.ordinal()] = 32;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[Action.MENU.ordinal()] = 33;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[Action.MORE_EDIT.ordinal()] = 34;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[Action.MOVE.ordinal()] = 35;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[Action.MOVEIN.ordinal()] = 36;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[Action.MOVEOUT.ordinal()] = 37;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[Action.MULTISCREEN.ordinal()] = 38;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[Action.MULTISCREEN_ACTIVITED.ordinal()] = 39;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[Action.MULTI_SELECTION.ordinal()] = 40;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[Action.MULTI_SELECTION_ON.ordinal()] = 41;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[Action.MYFAVORITE.ordinal()] = 42;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[Action.NO.ordinal()] = 43;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[Action.NONE.ordinal()] = 44;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[Action.NOT_MYFAVORITE.ordinal()] = 45;
        } catch (NoSuchFieldError e37) {
        }
        try {
            iArr[Action.OK.ordinal()] = 46;
        } catch (NoSuchFieldError e38) {
        }
        try {
            iArr[Action.PHOTOSHARE_ACCOUNT.ordinal()] = 47;
        } catch (NoSuchFieldError e39) {
        }
        try {
            iArr[Action.PHOTOSHARE_ADDPICTURE.ordinal()] = 1;
        } catch (NoSuchFieldError e40) {
        }
        try {
            iArr[Action.PHOTOSHARE_BACKUP.ordinal()] = 48;
        } catch (NoSuchFieldError e41) {
        }
        try {
            iArr[Action.PHOTOSHARE_CANCEL_RECEIVE.ordinal()] = 2;
        } catch (NoSuchFieldError e42) {
        }
        try {
            iArr[Action.PHOTOSHARE_CLEAR.ordinal()] = 49;
        } catch (NoSuchFieldError e43) {
        }
        try {
            iArr[Action.PHOTOSHARE_COMBINE.ordinal()] = 50;
        } catch (NoSuchFieldError e44) {
        }
        try {
            iArr[Action.PHOTOSHARE_CONTACT.ordinal()] = 3;
        } catch (NoSuchFieldError e45) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_PEOPLE_TAG.ordinal()] = 51;
        } catch (NoSuchFieldError e46) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_SHARE.ordinal()] = 52;
        } catch (NoSuchFieldError e47) {
        }
        try {
            iArr[Action.PHOTOSHARE_DELETE.ordinal()] = 4;
        } catch (NoSuchFieldError e48) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD.ordinal()] = 53;
        } catch (NoSuchFieldError e49) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOADING.ordinal()] = 54;
        } catch (NoSuchFieldError e50) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD_START.ordinal()] = 55;
        } catch (NoSuchFieldError e51) {
        }
        try {
            iArr[Action.PHOTOSHARE_EDITSHARE.ordinal()] = 56;
        } catch (NoSuchFieldError e52) {
        }
        try {
            iArr[Action.PHOTOSHARE_EMAIL.ordinal()] = 57;
        } catch (NoSuchFieldError e53) {
        }
        try {
            iArr[Action.PHOTOSHARE_LINK.ordinal()] = 58;
        } catch (NoSuchFieldError e54) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_DOWNLOAD.ordinal()] = 59;
        } catch (NoSuchFieldError e55) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_UPLOAD.ordinal()] = 60;
        } catch (NoSuchFieldError e56) {
        }
        try {
            iArr[Action.PHOTOSHARE_MESSAGE.ordinal()] = 61;
        } catch (NoSuchFieldError e57) {
        }
        try {
            iArr[Action.PHOTOSHARE_MOVE.ordinal()] = 5;
        } catch (NoSuchFieldError e58) {
        }
        try {
            iArr[Action.PHOTOSHARE_MULTI_DOWNLOAD.ordinal()] = 6;
        } catch (NoSuchFieldError e59) {
        }
        try {
            iArr[Action.PHOTOSHARE_NOT_THIS_PERSON.ordinal()] = 7;
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
            iArr[Action.PHOTOSHARE_RENAME.ordinal()] = 8;
        } catch (NoSuchFieldError e63) {
        }
        try {
            iArr[Action.PHOTOSHARE_SETTINGS.ordinal()] = 64;
        } catch (NoSuchFieldError e64) {
        }
        try {
            iArr[Action.PHOTOSHARE_UPLOAD_START.ordinal()] = 65;
        } catch (NoSuchFieldError e65) {
        }
        try {
            iArr[Action.PRINT.ordinal()] = 66;
        } catch (NoSuchFieldError e66) {
        }
        try {
            iArr[Action.RANGE_MEASURE.ordinal()] = 67;
        } catch (NoSuchFieldError e67) {
        }
        try {
            iArr[Action.RECYCLE_CLEAN_BIN.ordinal()] = 68;
        } catch (NoSuchFieldError e68) {
        }
        try {
            iArr[Action.RECYCLE_DELETE.ordinal()] = 69;
        } catch (NoSuchFieldError e69) {
        }
        try {
            iArr[Action.RECYCLE_RECOVERY.ordinal()] = 70;
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
        initializeAboutPhotoShare();
        this.mSourceType = DataSourceType.identifySourceType(this.mMediaSet);
        GalleryLog.v("PhotoShareAlbumPage", " MediaSetPath " + this.mMediaSetPath);
        new Thread() {
            public void run() {
                if (PhotoShareUtils.getServer() != null) {
                    try {
                        if (PhotoShareAlbumPage.this.mSourceType == 12) {
                            PhotoShareUtils.getServer().refreshSingleGeneralAlbum(PhotoShareAlbumPage.this.mMediaSet.getAlbumInfo().getId());
                        } else if (PhotoShareAlbumPage.this.mSourceType == 17 || PhotoShareAlbumPage.this.mSourceType == 18) {
                            PhotoShareUtils.getServer().refreshSingleTag(PhotoShareAlbumPage.this.mMediaSetPath.getParent().getSuffix(), PhotoShareAlbumPage.this.mMediaSetPath.getSuffix());
                        }
                    } catch (RemoteException e) {
                        PhotoShareUtils.dealRemoteException(e);
                    }
                }
            }
        }.start();
        this.mDeleteClickListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which != -1) {
                    return;
                }
                if (PhotoShareAlbumPage.this.mOperationType == 3) {
                    ReportToBigData.report(137, String.format("{OperationFrom:%s}", new Object[]{"AlbumPage"}));
                    PhotoShareAlbumPage.this.onChooseTargetAlbum(null, null, 2);
                } else if (PhotoShareAlbumPage.this.mOperationType == 1) {
                    PhotoShareAlbumPage.this.onPhotoShareDelete();
                }
            }
        };
        this.mAllowDataAccessListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    PhotoShareUtils.startOpenNetService(PhotoShareAlbumPage.this.mHost.getActivity());
                    switch (PhotoShareAlbumPage.this.mOperationType) {
                        case 1:
                            PhotoShareUtils.showDeleteAlertDialog(PhotoShareAlbumPage.this.mHost.getActivity(), PhotoShareAlbumPage.this.mSourceType, PhotoShareAlbumPage.this.mDeleteClickListener, PhotoShareAlbumPage.this.mSelectionManager.getSelectedCount(), PhotoShareAlbumPage.this.mMediaSet, false, PhotoShareAlbumPage.this.isSyncAlbum(), PhotoShareAlbumPage.this.isHicloudAlbum());
                            return;
                        case 2:
                            PhotoShareAlbumPage.this.chooseTargetAlbum();
                            return;
                        case 3:
                            PhotoShareUtils.showDeleteTagFileAlertDialog(PhotoShareAlbumPage.this.mHost.getActivity(), PhotoShareAlbumPage.this.mDeleteClickListener);
                            return;
                        default:
                            return;
                    }
                }
            }
        };
    }

    protected boolean onCreateActionBar(Menu menu) {
        super.onCreateActionBar(menu);
        if (this.mNeedFootBar || (this.mSelectionManager.inSelectionMode() && !this.mGetContent)) {
            this.mHost.requestFeature(296);
        } else {
            this.mHost.requestFeature(298);
        }
        return true;
    }

    protected Bundle getBundleForPhoto(int slotIndex, MediaItem item) {
        if (!(this.mMediaSet instanceof PhotoShareAlbum)) {
            return super.getBundleForPhoto(slotIndex, item);
        }
        int preViewCount = this.mDataLoader.preSize();
        int count = this.mDataLoader.size();
        Path noPreview = PhotoShareSource.convertToNoPreView(this.mMediaSetPath);
        Bundle data = new Bundle();
        data.putInt("index-hint", Math.max(0, slotIndex - preViewCount));
        data.putParcelable("open-animation-rect", getAnimSlotRect());
        data.putString("media-set-path", noPreview.toString());
        data.putString("media-item-path", item.getPath().toString());
        data.putInt("media-count", Math.max(0, count - preViewCount));
        return data;
    }

    protected void onHandleMessage(Message message) {
        Context context = this.mHost.getActivity();
        if (context != null) {
            switch (message.what) {
                case 10:
                    cancelReceive(message.arg1, context);
                    break;
                case 11:
                    deleteFile(message.arg1, message.arg2, context);
                    break;
                case 12:
                    addDownload(message.arg1, message.arg2, context);
                    break;
                case 13:
                    moveTagFile(message.arg1, context);
                    break;
                default:
                    super.onHandleMessage(message);
                    break;
            }
        }
    }

    private void cancelReceive(int arg1, Context context) {
        dismissProgressDialog();
        if (arg1 != 0) {
            ContextedUtils.showToastQuickly(context, context.getString(R.string.photoshare_toast_cancel_receive_fail, new Object[]{context.getString(R.string.photoshare_toast_fail_common_Toast)}), 0);
            return;
        }
        this.mHost.getStateManager().finishState(this);
    }

    private void deleteFile(int arg1, int arg2, Context context) {
        if (arg1 != 0) {
            ContextedUtils.showToastQuickly(context, String.format(context.getResources().getQuantityString(R.plurals.photoshare_toast_delete_file_fail, arg1), new Object[]{context.getResources().getString(R.string.photoshare_toast_fail_common_Toast)}), 0);
        }
        if (this.mSelectionManager.inSelectionMode()) {
            this.mSelectionManager.leaveSelectionMode();
        }
        if (arg2 != 0) {
            PhotoShareUtils.notifyPhotoShareContentChange(3, this.mMediaSet.getPath().getSuffix());
            onDeleteProgressComplete(this.mVisiblePathMap, this.mVisibleIndexMap, this.mLayout);
            return;
        }
        this.mDataLoader.unfreeze();
        dismissProgressDialog();
    }

    private void addDownload(int arg1, int arg2, Context context) {
        if (arg2 == 0) {
            if (arg1 != 0) {
                ContextedUtils.showToastQuickly(context, (int) R.string.photoshare_add_download_task_failed, 0);
            } else {
                PhotoShareUtils.enableDownloadStatusBarNotification(true);
                PhotoShareUtils.refreshStatusBar(true);
            }
        }
        if (this.mSelectionManager.inSelectionMode()) {
            this.mSelectionManager.leaveSelectionMode();
        }
        dismissProgressDialog();
    }

    private void moveTagFile(int arg1, Context context) {
        if (arg1 != 0) {
            ContextedUtils.showToastQuickly(context, (int) R.string.photoshare_move_classify_failed, 0);
        }
        PhotoShareUtils.notifyPhotoShareFolderChanged(2);
        if (this.mSelectionManager.inSelectionMode()) {
            this.mSelectionManager.leaveSelectionMode();
        }
        dismissProgressDialog();
    }

    protected boolean dismissDeleteProgressDialog() {
        if (this.mSourceType == 12 || this.mSourceType == 19 || this.mSourceType == 20) {
            return super.dismissDeleteProgressDialog();
        }
        dismissProgressDialog();
        return true;
    }

    protected void onInflateMenu(Menu menu) {
        switch (this.mSourceType) {
            case 12:
            case 19:
                this.mMenu = PHOTOSHARE_LOCAL_MENU;
                break;
            case 13:
                this.mMenu = PHOTOSHARE_SHARE_MENU;
                break;
            case 17:
                this.mMenu = PHOTOSHARE_TAG_PEOPLE_MENU;
                break;
            case 18:
                this.mMenu = PHOTOSHARE_TAG_OTHER_CLASSIFY_MENU;
                break;
            case 20:
                this.mMenu = GALLERY_MEDIA_MENU;
                break;
        }
        if (13 == this.mSourceType) {
            this.mNeedFootBar = true;
            ActionMode am = this.mActionBar.enterActionMode(false);
            if (13 == this.mSourceType) {
                am.setMenu(3, PHOTOSHARE_SHARE_NOPRESS_MENU);
            }
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mCreateDialog != null) {
            GalleryUtils.setDialogDismissable(this.mCreateDialog, true);
            GalleryUtils.dismissDialogSafely(this.mCreateDialog, null);
            this.mCreateDialog = null;
        }
        if (this.mProgressDialog != null) {
            GalleryUtils.setDialogDismissable(this.mProgressDialog, true);
            GalleryUtils.dismissDialogSafely(this.mProgressDialog, null);
            this.mProgressDialog = null;
        }
    }

    protected void leaveSelectionMode() {
        super.leaveSelectionMode();
        if (this.mNeedFootBar) {
            this.mHost.requestFeature(296);
        } else {
            this.mHost.requestFeature(298);
        }
    }

    protected boolean onItemSelected(Action action) {
        int resId = action.textResID;
        Context context = this.mHost.getActivity();
        if (context == null) {
            return true;
        }
        ReportToBigData.reportCloudSelectActionAtAlbumPage(action, this.mSelectionManager.getSelectedCount(), this.mMediaSet.getAlbumType() == 7);
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
                onPhotoShareAddPicture(context);
                return true;
            case 2:
                onPhotoShareCancelReceive(context, resId);
                return true;
            case 3:
                onPhotoShareContact();
                return true;
            case 4:
                onPhotoShareDelete(context);
                return true;
            case 5:
                onPhotoShareMove(context);
                return true;
            case 6:
                onPhotoShareDownLoad();
                return true;
            case 7:
                onPhotoShareNotThisPersion(context);
                return true;
            case 8:
                ReportToBigData.report(72);
                createDialogIfNeeded(this.mMediaSet.getName(), R.string.rename, this.mReNameDialogButtonListener);
                return true;
            default:
                return super.onItemSelected(action);
        }
    }

    private void onPhotoShareAddPicture(Context context) {
        Intent request = new Intent(context, ListAlbumPickerActivity.class).setAction("android.intent.action.GET_CONTENT").setType("*/*");
        request.putExtra("support-multipick-items", true);
        this.mHost.getActivity().startActivityForResult(request, 120);
    }

    private boolean onPhotoShareDelete(Context context) {
        if (this.mSourceType == 12 || this.mSourceType == 19 || this.mSourceType == 20) {
            onDelete();
            return true;
        }
        this.mOperationType = 1;
        PhotoShareUtils.showDeleteAlertDialog(context, this.mSourceType, this.mDeleteClickListener, this.mSelectionManager.getSelectedCount(), this.mMediaSet, false, isSyncAlbum(), isHicloudAlbum());
        return true;
    }

    private void onPhotoShareCancelReceive(Context context, final int resId) {
        PhotoShareAlertDialogFragment cancelMyReceiveDialog = PhotoShareAlertDialogFragment.newInstance(context.getString(R.string.photoshare_cancel_my_receive), context.getString(R.string.photoshare_cancel_my_receive_desc), context.getString(R.string.photoshare_cancel_my_receive), true);
        GalleryLog.printDFXLog("PhotoShareAlbumPage");
        cancelMyReceiveDialog.show(this.mHost.getActivity().getSupportFragmentManager(), "");
        GalleryLog.printDFXLog("DFX PHOTOSHARE_CANCEL_RECEIVE");
        cancelMyReceiveDialog.setOnDialogButtonClickListener(new onDialogButtonClickListener() {
            public void onPositiveClick() {
                ReportToBigData.report(70);
                PhotoShareAlbumPage.this.showProgressDialog(PhotoShareAlbumPage.this.mHost.getActivity().getString(resId));
                new Thread() {
                    public void run() {
                        try {
                            PhotoShareAlbumPage.this.mHandler.sendMessage(PhotoShareAlbumPage.this.mHandler.obtainMessage(10, PhotoShareUtils.getServer().cancelReceiveShare(PhotoShareAlbumPage.this.mMediaSet.getAlbumInfo().getId()), 0));
                        } catch (RemoteException e) {
                            PhotoShareUtils.dealRemoteException(e);
                            PhotoShareAlbumPage.this.mHandler.sendMessage(PhotoShareAlbumPage.this.mHandler.obtainMessage(10, -1, 0));
                            GalleryLog.printDFXLog("cancelReceiveShare RemoteException for DFX");
                        }
                    }
                }.start();
            }
        });
    }

    private void onPhotoShareContact() {
        ReportToBigData.reportGotoCloudAlbumMember(false);
        Bundle bundle = new Bundle();
        Intent intent = new Intent();
        if (this.mMediaSet.getAlbumType() == 3) {
            intent.setClass(this.mHost.getActivity(), PhotoShareShowMemberActivity.class);
            bundle.putString("sharePath", this.mMediaSet.getPath().toString());
            bundle.putString("shareName", this.mMediaSet.getName());
        } else {
            intent.setClass(this.mHost.getActivity(), PhotoShareEditFriendsActivity.class);
            bundle.putString("sharePath", this.mMediaSet.getPath().toString());
            bundle.putString("shareName", this.mMediaSet.getName());
        }
        intent.putExtras(bundle);
        this.mHost.getActivity().startActivity(intent);
    }

    private boolean onPhotoShareNotThisPersion(Context context) {
        if (PhotoShareUtils.isNetworkConnected(context)) {
            this.mOperationType = 2;
            if (PhotoShareUtils.isNetAllowed(context)) {
                ReportToBigData.report(138, String.format("{OperationFrom:%s}", new Object[]{"AlbumPage"}));
                chooseTargetAlbum();
                return true;
            }
            new Builder(context).setTitle(R.string.photoshare_allow_title).setMessage(R.string.photoshare_allow_message).setPositiveButton(R.string.photoshare_allow_btn, this.mAllowDataAccessListener).setNegativeButton(R.string.cancel, this.mAllowDataAccessListener).show();
            return true;
        }
        ContextedUtils.showToastQuickly(context, (int) R.string.photoshare_toast_nonetwork, 0);
        return true;
    }

    private boolean onPhotoShareMove(Context context) {
        if (PhotoShareUtils.isNetworkConnected(context)) {
            this.mOperationType = 3;
            if (PhotoShareUtils.isNetAllowed(context)) {
                PhotoShareUtils.showDeleteTagFileAlertDialog(this.mHost.getActivity(), this.mDeleteClickListener);
                return true;
            }
            new Builder(context).setTitle(R.string.photoshare_allow_title).setMessage(R.string.photoshare_allow_message).setPositiveButton(R.string.photoshare_allow_btn, this.mAllowDataAccessListener).setNegativeButton(R.string.cancel, this.mAllowDataAccessListener).show();
            GalleryLog.printDFXLog("PhotoShareUtils.isNetAllowed  Log for DFX");
            return true;
        }
        ContextedUtils.showToastQuickly(context, (int) R.string.photoshare_toast_nonetwork, 0);
        return true;
    }

    private void chooseTargetAlbum() {
        Bundle data = new Bundle();
        data.putString("media-path", "/photoshare/exclude/*".replace("*", this.mMediaSetPath.getSuffix()));
        data.putString("exclude-path", this.mMediaSetPath.getSuffix());
        Intent intent = new Intent(this.mHost.getActivity(), PhotoShareTagAlbumSetActivity.class);
        intent.putExtras(data);
        this.mHost.getActivity().startActivityForResult(intent, 121);
    }

    private void onPhotoShareDownLoad() {
        if (PhotoShareUtils.isNetworkConnected(this.mHost.getActivity())) {
            showProgressDialog(this.mHost.getActivity().getString(R.string.photoshare_add_downloading_task));
            GalleryLog.printDFXLog("PhotoShareAlbumPage onPhotoShareDownLoad called for DFX");
            new Thread() {
                public void run() {
                    ArrayList<Path> selectedPaths = PhotoShareAlbumPage.this.mSelectionManager.getSelected(true);
                    ArrayList<FileInfo> list = new ArrayList();
                    DataManager dm = PhotoShareAlbumPage.this.mHost.getGalleryContext().getDataManager();
                    for (int i = 0; i < selectedPaths.size(); i++) {
                        MediaObject obj = dm.getMediaObject((Path) selectedPaths.get(i));
                        if (obj != null) {
                            MediaItem mediaItem = (MediaItem) obj;
                            if (!mediaItem.isPhotoSharePreView()) {
                                FileInfo fileInfo = mediaItem.getFileInfo();
                                if (PhotoShareAlbum.getLocalRealPath(PhotoShareAlbumPage.this.mMediaSet.getName(), fileInfo) == null) {
                                    list.add(fileInfo);
                                }
                            }
                        } else {
                            GalleryLog.v("PhotoShareAlbumPage", "onPhotoShareDownLoad object not exists. Path " + selectedPaths.get(i));
                        }
                    }
                    if (list.size() == 0) {
                        PhotoShareAlbumPage.this.mHandler.sendMessage(PhotoShareAlbumPage.this.mHandler.obtainMessage(12, 0, -1));
                        return;
                    }
                    int result;
                    if (6 == PhotoShareAlbumPage.this.mMediaSet.getAlbumType() || 5 == PhotoShareAlbumPage.this.mMediaSet.getAlbumType()) {
                        ArrayList<FileInfo> localList = new ArrayList();
                        ArrayList<FileInfo> shareList = new ArrayList();
                        for (FileInfo info : list) {
                            if (TextUtils.isEmpty(info.getShareId())) {
                                localList.add(info);
                            } else {
                                shareList.add(info);
                            }
                        }
                        result = (PhotoShareUtils.addDownLoadTask(shareList, 2) + 0) + PhotoShareUtils.addDownLoadTask(localList, 1);
                    } else {
                        result = PhotoShareUtils.addDownLoadTask(list, PhotoShareAlbumPage.this.mMediaSet.getAlbumType()) + 0;
                    }
                    PhotoShareAlbumPage.this.mHandler.sendMessage(PhotoShareAlbumPage.this.mHandler.obtainMessage(12, result, 0));
                }
            }.start();
            return;
        }
        ContextedUtils.showToastQuickly(this.mHost.getActivity(), (int) R.string.photoshare_toast_nonetwork, 0);
    }

    private void onPhotoShareDelete() {
        this.mDataLoader.freeze();
        SlotView slotView = getSlotView();
        this.mVisiblePathMap = new HashMap();
        this.mVisibleIndexMap = new HashMap();
        slotView.prepareVisibleRangeItemIndex(this.mVisiblePathMap, this.mVisibleIndexMap);
        this.mLayout = slotView.cloneLayout();
        showProgressDialog(this.mHost.getActivity().getString(R.string.photoshare_deleting));
        new Thread() {
            public void run() {
                ArrayList<Path> selectedPaths = PhotoShareAlbumPage.this.mSelectionManager.getSelected(true);
                int group = ((selectedPaths.size() + 100) - 1) / 100;
                int albumType = PhotoShareAlbumPage.this.mMediaSet.getAlbumType();
                ArrayList<TagFileInfo> tagFileDeleteList = new ArrayList();
                boolean isTagFile = PhotoShareAlbumPage.this.mSourceType == 18 || PhotoShareAlbumPage.this.mSourceType == 17;
                int failedCount = 0;
                DataManager dm = PhotoShareAlbumPage.this.mHost.getGalleryContext().getDataManager();
                for (int i = 0; i < group; i++) {
                    tagFileDeleteList.clear();
                    int start = i * 100;
                    int end = Math.min(start + 100, selectedPaths.size());
                    for (int j = start; j < end; j++) {
                        MediaObject obj = dm.getMediaObject((Path) selectedPaths.get(j));
                        if (obj != null) {
                            if (isTagFile) {
                                tagFileDeleteList.add(((PhotoShareTagFile) obj).getTagFileInfo());
                            } else {
                                MediaItem item = (MediaItem) obj;
                                if (1 == albumType || 10 == PhotoShareAlbumPage.this.mMediaSet.getAlbumType()) {
                                    item.delete();
                                }
                            }
                        }
                    }
                    if (tagFileDeleteList.size() > 0) {
                        failedCount += PhotoShareAlbumPage.this.deleteTagFiles((TagFileInfo[]) tagFileDeleteList.toArray(new TagFileInfo[tagFileDeleteList.size()]));
                    }
                }
                PhotoShareAlbumPage.this.mHandler.sendMessage(PhotoShareAlbumPage.this.mHandler.obtainMessage(11, failedCount, failedCount == selectedPaths.size() ? 0 : 1));
            }
        }.start();
    }

    private void onChooseTargetAlbum(final String targetID, final String name, final int type) {
        showProgressDialog(this.mHost.getActivity().getString(R.string.photoshare_moving_classify));
        new Thread() {
            public void run() {
                int failedCount = 0;
                for (Entry<String, ArrayList<TagFileInfo>> tempEntry : PhotoShareAlbumPage.this.getTagFileInfo().entrySet()) {
                    ArrayList<TagFileInfo> list = (ArrayList) tempEntry.getValue();
                    String categoryId = (String) tempEntry.getKey();
                    int group = ((list.size() + 100) - 1) / 100;
                    for (int i = 0; i < group; i++) {
                        List<TagFileInfo> subList = list.subList(i * 100, Math.min((i + 1) * 100, list.size()));
                        failedCount += PhotoShareAlbumPage.this.moveAlbum((TagFileInfo[]) subList.toArray(new TagFileInfo[subList.size()]), categoryId, targetID, name, type);
                    }
                }
                PhotoShareAlbumPage.this.mHandler.sendMessage(PhotoShareAlbumPage.this.mHandler.obtainMessage(13, failedCount, 0));
            }
        }.start();
    }

    private HashMap<String, ArrayList<TagFileInfo>> getTagFileInfo() {
        HashMap<String, ArrayList<TagFileInfo>> result = new HashMap();
        ArrayList<Path> selectedPaths = this.mSelectionManager.getSelected(true);
        DataManager dm = this.mHost.getGalleryContext().getDataManager();
        for (int i = 0; i < selectedPaths.size(); i++) {
            MediaObject obj = dm.getMediaObject((Path) selectedPaths.get(i));
            if (obj != null) {
                TagFileInfo tagFileInfo = ((PhotoShareTagFile) obj).getTagFileInfo();
                if (tagFileInfo != null) {
                    String categoryId = tagFileInfo.getCategoryId();
                    ArrayList<TagFileInfo> temp = (ArrayList) result.get(categoryId);
                    if (temp == null) {
                        temp = new ArrayList();
                        result.put(categoryId, temp);
                    }
                    temp.add(tagFileInfo);
                }
            }
        }
        return result;
    }

    private int moveAlbum(TagFileInfo[] moveArray, String categoryID, String targetID, String name, int type) {
        List result = null;
        String tagID = this.mMediaSetPath.getSuffix();
        switch (type) {
            case 1:
                try {
                    result = PhotoShareUtils.getServer().moveToTagFileInfoList(categoryID, tagID, moveArray, targetID);
                    break;
                } catch (RemoteException e) {
                    PhotoShareUtils.dealRemoteException(e);
                    break;
                }
            case 2:
                GalleryLog.printDFXLog("DELETE_FROM_PEOPLE_CLASSIFY called  for DFX");
                result = PhotoShareUtils.getServer().deleteTagItemInfoList(categoryID, tagID, moveArray);
                break;
            case 3:
                result = PhotoShareUtils.getServer().modifyTagFileInfoList(categoryID, tagID, name, moveArray);
                break;
        }
        if (result == null) {
            GalleryLog.v("PhotoShareAlbumPage", "moveTagFile Files Ok");
            return 0;
        }
        GalleryLog.v("PhotoShareAlbumPage", "moveTagFile Failed Size " + result.size());
        return result.size();
    }

    private int deleteTagFiles(TagFileInfo[] deleteArray) {
        List result = null;
        try {
            result = PhotoShareUtils.getServer().deleteTagFileInfoList(this.mMediaSet.getPath().getParent().getSuffix(), this.mMediaSet.getPath().getSuffix(), deleteArray);
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        if (result == null) {
            GalleryLog.v("PhotoShareAlbumPage", "deleteTagFiles not login");
            return deleteArray.length;
        }
        GalleryLog.v("PhotoShareAlbumPage", "deleteTagFiles Failed Size " + result.size());
        return result.size();
    }

    private void initializeAboutPhotoShare() {
        this.mReNameDialogButtonListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                boolean z = false;
                switch (which) {
                    case -1:
                        PhotoShareUtils.hideSoftInput(PhotoShareAlbumPage.this.mSetNameTextView);
                        if (PhotoShareUtils.isNetworkConnected(PhotoShareAlbumPage.this.mHost.getActivity())) {
                            final String fileName = PhotoShareAlbumPage.this.mSetNameTextView.getText().toString().trim();
                            if (PhotoShareUtils.isShareNameValid(PhotoShareAlbumPage.this.mHost.getActivity(), fileName) && PhotoShareUtils.checkCharValid(fileName, PhotoShareAlbumPage.this.mHost.getActivity())) {
                                if (!PhotoShareAlbumPage.this.mMediaSet.getName().equals(fileName)) {
                                    PhotoShareAlbumPage.this.showProgressDialog(PhotoShareAlbumPage.this.mHost.getActivity().getString(R.string.photoshare_progress_message_modify_share_folder_name));
                                    new Thread() {
                                        public void run() {
                                            final int finalResult = PhotoShareAlbumPage.this.mMediaSet.getAlbumInfo().modifyName(fileName);
                                            Handler handler = PhotoShareAlbumPage.this.mHandler;
                                            final String str = fileName;
                                            handler.post(new Runnable() {
                                                public void run() {
                                                    Context context = PhotoShareAlbumPage.this.mHost.getActivity();
                                                    if (context != null) {
                                                        if (finalResult == 0) {
                                                            ActionMode am = (ActionMode) PhotoShareAlbumPage.this.mActionBar.getCurrentMode();
                                                            PhotoShareAlbumPage.this.mMediaSet.setName(str);
                                                            am.setTitle(PhotoShareAlbumPage.this.mMediaSet.getName());
                                                        } else if (1 == finalResult) {
                                                            ContextedUtils.showToastQuickly(context, (int) R.string.photoshare_toast_modify_folder_fail_Toast, 0);
                                                        } else if (2 == finalResult) {
                                                            ContextedUtils.showToastQuickly(context, context.getString(R.string.photoshare_album_toast_modify_folder_fail_Toast, new Object[]{context.getString(R.string.photoshare_toast_fail_common_Toast)}), 0);
                                                        } else if (7 == finalResult) {
                                                            ContextedUtils.showToastQuickly(context, (int) R.string.create_album_file_exist_Toast, 0);
                                                        }
                                                    }
                                                    PhotoShareAlbumPage.this.dismissProgressDialog();
                                                }
                                            });
                                        }
                                    }.start();
                                    break;
                                }
                                ContextedUtils.showToastQuickly(PhotoShareAlbumPage.this.mHost.getActivity(), (int) R.string.create_album_file_exist_Toast, 0);
                                return;
                            }
                            PhotoShareAlbumPage.this.mSetNameTextView.setFocusable(true);
                            PhotoShareAlbumPage.this.mSetNameTextView.setCursorVisible(true);
                            PhotoShareAlbumPage.this.mSetNameTextView.requestFocusFromTouch();
                            return;
                        }
                        ContextedUtils.showToastQuickly(PhotoShareAlbumPage.this.mHost.getActivity(), (int) R.string.photoshare_toast_nonetwork, 0);
                        return;
                        break;
                    default:
                        PhotoShareUtils.hideSoftInput(PhotoShareAlbumPage.this.mSetNameTextView);
                        GalleryUtils.setDialogDismissable(dialog, true);
                        StringBuilder append = new StringBuilder().append("PhotoShareAlbumPage for DFX mCreateDialog ");
                        if (PhotoShareAlbumPage.this.mCreateDialog == null) {
                            z = true;
                        }
                        GalleryLog.printDFXLog(append.append(z).toString());
                        if (PhotoShareAlbumPage.this.mCreateDialog != null) {
                            GalleryUtils.setDialogDismissable(PhotoShareAlbumPage.this.mCreateDialog, true);
                            GalleryUtils.dismissDialogSafely(PhotoShareAlbumPage.this.mCreateDialog, null);
                            PhotoShareAlbumPage.this.mCreateDialog = null;
                            break;
                        }
                        break;
                }
            }
        };
    }

    protected void onSingleTapUp(int slotIndex, boolean cornerPressed) {
        if (this.mIsActive) {
            MediaItem item = this.mAlbumDataAdapter.get(slotIndex);
            if (item != null && !item.isPhotoSharePreView()) {
                int request;
                if (!this.mSelectionManager.inSelectionMode()) {
                    request = 106;
                } else if (!cornerPressed || this.mSelectionManager.inSingleMode()) {
                    request = 102;
                } else {
                    this.mSelectionManager.toggle(item.getPath());
                    this.mSlotView.invalidate();
                    return;
                }
                pickPhotoWithAnimation(this.mSlotView, request, Integer.valueOf(slotIndex), slotIndex, item);
            }
        }
    }

    private void createDialogIfNeeded(String defaultName, int titleID, OnClickListener clickListener) {
        if (this.mCreateDialog == null || !this.mCreateDialog.isShowing()) {
            this.mSetNameTextView = new EditText(this.mHost.getActivity());
            this.mSetNameTextView.setSingleLine(true);
            ColorfulUtils.decorateColorfulForEditText(this.mHost.getActivity(), this.mSetNameTextView);
            this.mCreateDialog = GalleryUtils.createDialog(this.mHost.getActivity(), defaultName, titleID, clickListener, null, this.mSetNameTextView);
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    PhotoShareUtils.showSoftInput(PhotoShareAlbumPage.this.mSetNameTextView);
                }
            }, 300);
            return;
        }
        GalleryLog.d("PhotoShareAlbumPage", "PhotoShareAlbumPage The dialog is showing, do not create any more");
    }

    private void showProgressDialog(String message) {
        this.mProgressDialog = new ProgressDialog(this.mHost.getActivity());
        this.mProgressDialog.setCancelable(false);
        this.mProgressDialog.setMessage(message);
        this.mProgressDialog.show();
        GalleryLog.printDFXLog("showProgressDialog function for DFX");
    }

    private void dismissProgressDialog() {
        if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
            this.mProgressDialog.dismiss();
            this.mProgressDialog = null;
        }
    }

    protected boolean autoFinishWhenNoItems() {
        if (this.mSourceType == 12 || this.mSourceType == 19) {
            return true;
        }
        return false;
    }

    protected void onStateResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 106:
                if (data != null) {
                    this.mFocusIndex = data.getIntExtra("return-index-hint", 0) + this.mDataLoader.preSize();
                    this.mSlotView.makeSlotVisible(this.mFocusIndex);
                    break;
                }
                return;
            case 120:
                getPhoto(resultCode, data);
                break;
            case 121:
                getTargetAlbum(resultCode, data);
                break;
            default:
                super.onStateResult(requestCode, resultCode, data);
                break;
        }
    }

    private void getPhoto(int resultCode, Intent data) {
        if (resultCode == -1) {
            final ArrayList<String> fileList = data.getStringArrayListExtra("select-item-list");
            if (fileList != null) {
                ReportToBigData.reportAddCloudPicturesWithCount(fileList.size(), this.mMediaSet.getAlbumType() == 7);
                showProgressDialog(this.mHost.getActivity().getString(R.string.photoshare_adding_picture));
                new Thread() {
                    public void run() {
                        ArrayList<String> filePath = PhotoShareUtils.getFilePathFromPathString(PhotoShareAlbumPage.this.mHost.getGalleryContext(), fileList);
                        ArrayList<String> fileNeedToAdd = PhotoShareUtils.checkMd5ExistsInShare(PhotoShareAlbumPage.this.mMediaSet.getAlbumInfo().getId(), filePath);
                        if (filePath.size() > fileNeedToAdd.size()) {
                            PhotoShareUtils.showFileExitsTips(filePath.size() - fileNeedToAdd.size());
                        }
                        if (!fileNeedToAdd.isEmpty()) {
                            final int result = PhotoShareAlbumPage.this.mMediaSet.getAlbumInfo().addFileToAlbum((String[]) fileNeedToAdd.toArray(new String[fileNeedToAdd.size()]));
                            GalleryLog.v("PhotoShareAlbumPage", "addFileToShare result " + result);
                            final Activity activity = PhotoShareAlbumPage.this.mHost.getActivity();
                            if (activity != null) {
                                activity.runOnUiThread(new Runnable() {
                                    public void run() {
                                        if (result != 0) {
                                            ContextedUtils.showToastQuickly(activity, activity.getString(R.string.photoshare_toast_failed_add_picture, new Object[]{activity.getString(R.string.photoshare_toast_fail_common_Toast)}), 0);
                                            return;
                                        }
                                        PhotoShareUtils.enableUploadStatusBarNotification(true);
                                        PhotoShareUtils.refreshStatusBar(false);
                                    }
                                });
                            }
                        }
                        if (PhotoShareAlbumPage.this.mHost.getActivity() != null) {
                            PhotoShareAlbumPage.this.mHost.getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    PhotoShareAlbumPage.this.dismissProgressDialog();
                                }
                            });
                        }
                    }
                }.start();
            }
        }
    }

    private void getTargetAlbum(int resultCode, Intent data) {
        if (data != null) {
            Bundle bundle = data.getExtras();
            if (bundle != null && resultCode == -1) {
                switch (bundle.getInt("result-kind")) {
                    case 1:
                        onChooseTargetAlbum(bundle.getString("target-tagID"), null, 1);
                        break;
                    case 2:
                        onChooseTargetAlbum(null, null, 2);
                        break;
                    case 3:
                        onChooseTargetAlbum(null, bundle.getString("tag-name"), 3);
                        break;
                }
            }
        }
    }

    protected boolean supportPreview() {
        return false;
    }

    protected boolean isHicloudAlbum() {
        if ((this.mMediaSet instanceof CloudLocalAlbum) || (this.mMediaSet instanceof DiscoverLocation) || this.mMediaSet.getAlbumType() == 5 || this.mMediaSet.getAlbumType() == 6) {
            return true;
        }
        return false;
    }

    protected boolean isSyncAlbum() {
        return false;
    }
}
