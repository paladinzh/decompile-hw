package com.huawei.gallery.app;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.text.format.Formatter;
import com.android.gallery3d.R;
import com.android.gallery3d.data.DataSourceType;
import com.android.gallery3d.data.DiscoverLocation;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.PhotoShareImage;
import com.android.gallery3d.data.PhotoShareMediaItem;
import com.android.gallery3d.data.PhotoShareTagFile;
import com.android.gallery3d.data.PhotoShareVideo;
import com.android.gallery3d.menuexecutor.MenuEnableCtrller;
import com.android.gallery3d.ui.MenuExecutor.ExtraActionListener;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.android.cg.vo.FileInfo;
import com.huawei.android.cg.vo.TagFileInfo;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionBarStateBase;
import com.huawei.gallery.photoshare.ui.PhotoShareTagAlbumSetActivity;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils.DownLoadProgressListener;
import com.huawei.gallery.ui.PhotoPageLoadingManager;
import java.util.List;

public class PhotoSharePhotoPage extends PhotoPage implements DownLoadProgressListener, IPhotoPage {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    private static ExtraActionListener sPhotoShareDeleteListener = new ExtraActionListener() {
        public void onExecuteExtraActionEnd() {
            PhotoShareUtils.unLockReload();
            PhotoShareUtils.notifyPhotoShareFolderChanged(3);
        }
    };
    private final Action[] CAMERA_BACKUP_SHARE_MENU = new Action[]{Action.SHARE, Action.PHOTOSHARE_DOWNLOAD, Action.PHOTOSHARE_DELETE, Action.EDIT, Action.PRINT, Action.SETAS};
    private final Action[] CAMERA_BACKUP_SHARE_MENU_DOWNLOADING = new Action[]{Action.SHARE, Action.PHOTOSHARE_DOWNLOADING, Action.PHOTOSHARE_DELETE, Action.EDIT, Action.PRINT, Action.SETAS};
    private final Action[] TAG_OTHER_CLASSIFY_MENU = new Action[]{Action.SHARE, Action.PHOTOSHARE_MOVE, Action.PHOTOSHARE_DELETE, Action.PHOTOSHARE_DOWNLOAD};
    private final Action[] TAG_OTHER_CLASSIFY_MENU_DOWNLOADING = new Action[]{Action.SHARE, Action.PHOTOSHARE_MOVE, Action.PHOTOSHARE_DELETE, Action.PHOTOSHARE_DOWNLOADING};
    private final Action[] TAG_PEOPLE_MENU = new Action[]{Action.SHARE, Action.PHOTOSHARE_NOT_THIS_PERSON, Action.PHOTOSHARE_DELETE, Action.PHOTOSHARE_DOWNLOAD};
    private final Action[] TAG_PEOPLE_MENU_DOWNLOADING = new Action[]{Action.SHARE, Action.PHOTOSHARE_NOT_THIS_PERSON, Action.PHOTOSHARE_DELETE, Action.PHOTOSHARE_DOWNLOADING};
    private OnClickListener mAllowDataAccessListener;
    private OnClickListener mDownloadListener;
    private boolean mIsDownLoading = false;
    private boolean mIsDownloadFailed = false;
    private PhotoPageLoadingManager mManager = null;
    private boolean mNeedToPlayAfterDownLoad = false;
    private OnClickListener mOnClickListener;
    private int mOperationType = -1;
    private Handler mPhotoShareHandler;
    private ProgressDialog mProgressDialog;
    private int mSourceType;

    private static /* synthetic */ int[] -getcom-huawei-gallery-actionbar-ActionSwitchesValues() {
        if (-com-huawei-gallery-actionbar-ActionSwitchesValues != null) {
            return -com-huawei-gallery-actionbar-ActionSwitchesValues;
        }
        int[] iArr = new int[Action.values().length];
        try {
            iArr[Action.ADD.ordinal()] = 7;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Action.ADD_ALBUM.ordinal()] = 8;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Action.ADD_COMMENT.ordinal()] = 9;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Action.AIRSHARE.ordinal()] = 10;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Action.ALBUM.ordinal()] = 11;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Action.ALL.ordinal()] = 12;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Action.BACK.ordinal()] = 13;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Action.CANCEL_DETAIL.ordinal()] = 14;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Action.COLLAGE.ordinal()] = 15;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Action.COMMENT.ordinal()] = 16;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Action.COPY.ordinal()] = 17;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Action.DEALL.ordinal()] = 18;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Action.DEL.ordinal()] = 19;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Action.DETAIL.ordinal()] = 20;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[Action.DYNAMIC_ALBUM.ordinal()] = 21;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[Action.EDIT.ordinal()] = 1;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[Action.EDIT_COMMENT.ordinal()] = 22;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[Action.GOTO_GALLERY.ordinal()] = 23;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[Action.HIDE.ordinal()] = 24;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[Action.INFO.ordinal()] = 25;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[Action.KEYGUARD_LIKE.ordinal()] = 26;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[Action.KEYGUARD_NOT_LIKE.ordinal()] = 27;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[Action.LOOPPLAY.ordinal()] = 28;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[Action.MAP.ordinal()] = 29;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[Action.MENU.ordinal()] = 30;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[Action.MORE_EDIT.ordinal()] = 31;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[Action.MOVE.ordinal()] = 32;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[Action.MOVEIN.ordinal()] = 33;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[Action.MOVEOUT.ordinal()] = 34;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[Action.MULTISCREEN.ordinal()] = 35;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[Action.MULTISCREEN_ACTIVITED.ordinal()] = 36;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[Action.MULTI_SELECTION.ordinal()] = 37;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[Action.MULTI_SELECTION_ON.ordinal()] = 38;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[Action.MYFAVORITE.ordinal()] = 39;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[Action.NO.ordinal()] = 40;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[Action.NONE.ordinal()] = 41;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[Action.NOT_MYFAVORITE.ordinal()] = 42;
        } catch (NoSuchFieldError e37) {
        }
        try {
            iArr[Action.OK.ordinal()] = 43;
        } catch (NoSuchFieldError e38) {
        }
        try {
            iArr[Action.PHOTOSHARE_ACCOUNT.ordinal()] = 44;
        } catch (NoSuchFieldError e39) {
        }
        try {
            iArr[Action.PHOTOSHARE_ADDPICTURE.ordinal()] = 45;
        } catch (NoSuchFieldError e40) {
        }
        try {
            iArr[Action.PHOTOSHARE_BACKUP.ordinal()] = 46;
        } catch (NoSuchFieldError e41) {
        }
        try {
            iArr[Action.PHOTOSHARE_CANCEL_RECEIVE.ordinal()] = 47;
        } catch (NoSuchFieldError e42) {
        }
        try {
            iArr[Action.PHOTOSHARE_CLEAR.ordinal()] = 48;
        } catch (NoSuchFieldError e43) {
        }
        try {
            iArr[Action.PHOTOSHARE_COMBINE.ordinal()] = 49;
        } catch (NoSuchFieldError e44) {
        }
        try {
            iArr[Action.PHOTOSHARE_CONTACT.ordinal()] = 50;
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
            iArr[Action.PHOTOSHARE_DELETE.ordinal()] = 2;
        } catch (NoSuchFieldError e48) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD.ordinal()] = 3;
        } catch (NoSuchFieldError e49) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOADING.ordinal()] = 4;
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
            iArr[Action.PHOTOSHARE_MOVE.ordinal()] = 5;
        } catch (NoSuchFieldError e58) {
        }
        try {
            iArr[Action.PHOTOSHARE_MULTI_DOWNLOAD.ordinal()] = 60;
        } catch (NoSuchFieldError e59) {
        }
        try {
            iArr[Action.PHOTOSHARE_NOT_THIS_PERSON.ordinal()] = 6;
        } catch (NoSuchFieldError e60) {
        }
        try {
            iArr[Action.PHOTOSHARE_PAUSE.ordinal()] = 61;
        } catch (NoSuchFieldError e61) {
        }
        try {
            iArr[Action.PHOTOSHARE_REMOVE_PEOPLE_TAG.ordinal()] = 62;
        } catch (NoSuchFieldError e62) {
        }
        try {
            iArr[Action.PHOTOSHARE_RENAME.ordinal()] = 63;
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
        this.mDownloadListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (-1 == which) {
                    PhotoSharePhotoPage.this.photoShareDownLoadOrigin();
                }
            }
        };
        this.mOnClickListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which != -1) {
                    return;
                }
                if (PhotoSharePhotoPage.this.mOperationType == 2) {
                    PhotoSharePhotoPage.this.onPhotoShareDelete();
                } else if (PhotoSharePhotoPage.this.mOperationType == 4) {
                    ReportToBigData.report(137, String.format("{OperationFrom:%s}", new Object[]{"PhotoPage"}));
                    PhotoSharePhotoPage.this.onChooseTargetAlbum(null, null, 2);
                }
            }
        };
        this.mAllowDataAccessListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    PhotoShareUtils.startOpenNetService(PhotoSharePhotoPage.this.mHost.getActivity());
                    switch (PhotoSharePhotoPage.this.mOperationType) {
                        case 1:
                            PhotoSharePhotoPage.this.mPhotoShareHandler.postDelayed(new Runnable() {
                                public void run() {
                                    PhotoSharePhotoPage.this.addDownloadOriginTask(true);
                                }
                            }, 50);
                            return;
                        case 2:
                            PhotoShareUtils.showDeleteAlertDialog(PhotoSharePhotoPage.this.mHost.getActivity(), PhotoSharePhotoPage.this.mSourceType, PhotoSharePhotoPage.this.mOnClickListener, PhotoSharePhotoPage.this.mSelectionManager.getSelectedCount(), PhotoSharePhotoPage.this.mMediaSet, true, PhotoSharePhotoPage.this.isSyncAlbum(), PhotoSharePhotoPage.this.isHicloudAlbum());
                            return;
                        case 3:
                            PhotoSharePhotoPage.this.chooseTargetAlbum();
                            return;
                        default:
                            return;
                    }
                }
            }
        };
        this.mPhotoShareHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        if (PhotoSharePhotoPage.this.mHost.getActivity() != null) {
                            PhotoSharePhotoPage.this.dismissProgressDialog();
                            if (msg.arg1 == 0) {
                                PhotoShareUtils.unLockReload();
                                break;
                            }
                            PhotoSharePhotoPage.this.mHost.getGLRoot().lockRenderThread();
                            try {
                                PhotoSharePhotoPage.this.mPhotoView.autoSlidePicture(PhotoSharePhotoPage.sPhotoShareDeleteListener);
                                break;
                            } finally {
                                PhotoSharePhotoPage.this.mHost.getGLRoot().unlockRenderThread();
                            }
                        } else {
                            PhotoShareUtils.unLockReload();
                            return;
                        }
                }
            }
        };
        this.mSourceType = DataSourceType.identifySourceType(this.mMediaSet);
    }

    private void onPhotoShareDelete() {
        showProgressDialog(this.mHost.getActivity().getString(R.string.photoshare_deleting));
        new Thread() {
            public void run() {
                int i = 1;
                PhotoShareUtils.lockReload();
                Handler -get2;
                Handler -get22;
                if (PhotoSharePhotoPage.this.mSourceType == 18 || PhotoSharePhotoPage.this.mSourceType == 17) {
                    boolean isDeleteSuccess = false;
                    try {
                        int i2;
                        List<String> result = PhotoShareUtils.getServer().deleteTagFileInfoList(PhotoSharePhotoPage.this.mMediaSet.getPath().getParent().getSuffix(), PhotoSharePhotoPage.this.mMediaSet.getPath().getSuffix(), new TagFileInfo[]{((PhotoShareTagFile) PhotoSharePhotoPage.this.mCurrentPhoto).getTagFileInfo()});
                        if (result == null || result.size() != 0) {
                            PhotoSharePhotoPage.this.mHandler.post(new Runnable() {
                                public void run() {
                                    Context context = PhotoSharePhotoPage.this.mHost.getActivity();
                                    if (context != null) {
                                        ContextedUtils.showToastQuickly(context, String.format(context.getResources().getQuantityString(R.plurals.photoshare_toast_delete_file_fail, 1), new Object[]{context.getResources().getString(R.string.photoshare_toast_fail_common_Toast)}), 0);
                                    }
                                }
                            });
                        } else {
                            isDeleteSuccess = true;
                        }
                        -get2 = PhotoSharePhotoPage.this.mPhotoShareHandler;
                        -get22 = PhotoSharePhotoPage.this.mPhotoShareHandler;
                        if (isDeleteSuccess) {
                            i2 = 1;
                        } else {
                            i2 = 0;
                        }
                        -get2.sendMessage(-get22.obtainMessage(0, i2, 0));
                    } catch (RemoteException e) {
                        PhotoShareUtils.dealRemoteException(e);
                    } finally {
                        -get2 = PhotoSharePhotoPage.this.mPhotoShareHandler;
                        -get22 = PhotoSharePhotoPage.this.mPhotoShareHandler;
                        if (null == null) {
                            i = 0;
                        }
                        -get2.sendMessage(-get22.obtainMessage(0, i, 0));
                    }
                } else {
                    -get2 = PhotoSharePhotoPage.this.mPhotoShareHandler;
                    -get22 = PhotoSharePhotoPage.this.mPhotoShareHandler;
                    if (!((PhotoShareMediaItem) PhotoSharePhotoPage.this.mCurrentPhoto).deletePhotoShareFile()) {
                        i = 0;
                    }
                    -get2.sendMessage(-get22.obtainMessage(0, i, 0));
                }
            }
        }.start();
    }

    private void onChooseTargetAlbum(final String targetID, final String name, final int type) {
        showProgressDialog(this.mHost.getActivity().getString(R.string.photoshare_moving_classify));
        new Thread() {
            public void run() {
                int i = 1;
                PhotoShareUtils.lockReload();
                String categoryID = PhotoSharePhotoPage.this.mMediaSet.getPath().getParent().getSuffix();
                String tagID = PhotoSharePhotoPage.this.mMediaSet.getPath().getSuffix();
                boolean isMoveSuccess = false;
                Handler -get2;
                Handler -get22;
                try {
                    int i2;
                    TagFileInfo[] moveArray = new TagFileInfo[]{((PhotoShareTagFile) PhotoSharePhotoPage.this.mCurrentPhoto).getTagFileInfo()};
                    List result = null;
                    switch (type) {
                        case 1:
                            result = PhotoShareUtils.getServer().moveToTagFileInfoList(categoryID, tagID, moveArray, targetID);
                            break;
                        case 2:
                            result = PhotoShareUtils.getServer().deleteTagItemInfoList(categoryID, tagID, moveArray);
                            break;
                        case 3:
                            result = PhotoShareUtils.getServer().modifyTagFileInfoList(categoryID, tagID, name, moveArray);
                            break;
                    }
                    if (result == null || result.size() != 0) {
                        PhotoSharePhotoPage.this.mHandler.post(new Runnable() {
                            public void run() {
                                Context context = PhotoSharePhotoPage.this.mHost.getActivity();
                                if (context != null) {
                                    ContextedUtils.showToastQuickly(context, (int) R.string.photoshare_move_classify_failed, 0);
                                }
                            }
                        });
                    } else {
                        isMoveSuccess = true;
                    }
                    -get2 = PhotoSharePhotoPage.this.mPhotoShareHandler;
                    -get22 = PhotoSharePhotoPage.this.mPhotoShareHandler;
                    if (isMoveSuccess) {
                        i2 = 1;
                    } else {
                        i2 = 0;
                    }
                    -get2.sendMessage(-get22.obtainMessage(0, i2, 0));
                } catch (RemoteException e) {
                    PhotoShareUtils.dealRemoteException(e);
                } finally {
                    -get2 = PhotoSharePhotoPage.this.mPhotoShareHandler;
                    -get22 = PhotoSharePhotoPage.this.mPhotoShareHandler;
                    if (null == null) {
                        i = 0;
                    }
                    -get2.sendMessage(-get22.obtainMessage(0, i, 0));
                }
            }
        }.start();
    }

    protected void onInflateMenu(ActionBarStateBase mode) {
        if (12 == this.mSourceType || 13 == this.mSourceType) {
            if (this.mIsDownLoading) {
                mode.setMenu(5, this.CAMERA_BACKUP_SHARE_MENU_DOWNLOADING);
            } else {
                mode.setMenu(5, this.CAMERA_BACKUP_SHARE_MENU);
            }
        } else if (18 == this.mSourceType) {
            if (this.mIsDownLoading) {
                mode.setMenu(4, this.TAG_OTHER_CLASSIFY_MENU_DOWNLOADING);
            } else {
                mode.setMenu(4, this.TAG_OTHER_CLASSIFY_MENU);
            }
        } else if (17 == this.mSourceType) {
            if (this.mIsDownLoading) {
                mode.setMenu(4, this.TAG_PEOPLE_MENU_DOWNLOADING);
            } else {
                mode.setMenu(4, this.TAG_PEOPLE_MENU);
            }
        }
        if (this.mCurrentPhoto != null) {
            PhotoShareMediaItem item = this.mCurrentPhoto;
            if (!item.isThumbNail() || TextUtils.isEmpty(item.getFileInfo().getFileId())) {
                changePhotoShareDownloadAction(false);
            }
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mPhotoShareHandler.removeCallbacksAndMessages(null);
        PhotoShareUtils.unLockReload();
        if (this.mProgressDialog != null) {
            GalleryUtils.setDialogDismissable(this.mProgressDialog, true);
            GalleryUtils.dismissDialogSafely(this.mProgressDialog, null);
            this.mProgressDialog = null;
        }
    }

    private void changePhotoShareDownloadAction(boolean isDownloading) {
        boolean z = false;
        ActionBarStateBase actionBar = getGalleryActionBar().getCurrentMode();
        if (actionBar != null) {
            this.mIsDownLoading = isDownloading;
            if (isDownloading) {
                actionBar.changeAction(Action.ACTION_ID_PHOTOSHARE_DOWNLOAD, Action.ACTION_ID_PHOTOSHARE_DOWNLOADING);
                this.mActionProgressActionListener.onStart();
            } else {
                actionBar.changeAction(Action.ACTION_ID_PHOTOSHARE_DOWNLOADING, Action.ACTION_ID_PHOTOSHARE_DOWNLOAD);
                this.mActionProgressActionListener.onEnd();
                if (this.mCurrentPhoto != null) {
                    if ((this.mCurrentPhoto.getSupportedOperations() & 268435456) != 0) {
                        z = true;
                    }
                    actionBar.setActionEnable(z, Action.ACTION_ID_PHOTOSHARE_DOWNLOAD);
                }
            }
        }
    }

    protected void onPhotoSharePhotoChanged() {
        boolean z = true;
        if (this.mCurrentPhoto != null) {
            PhotoShareMediaItem photoShareItem = this.mCurrentPhoto;
            FileInfo fileInfo = photoShareItem.getFileInfo();
            FileInfo[] info = new FileInfo[]{fileInfo};
            int folderType = photoShareItem.getFolderType();
            if (onStateCheck(photoShareItem, fileInfo)) {
                boolean downloadOrigin = false;
                int addDownLoadResult = 1;
                if (1 == folderType) {
                    try {
                        if (PhotoShareUtils.getServer().isGeneralFileDownloading(info, 0) == 0) {
                            addDownLoadResult = PhotoShareUtils.getServer().downloadPhotoThumb(info, 0, 1, false);
                            downloadOrigin = true;
                            GalleryLog.v("PhotoSharePhotoPage", "download origin FileName = " + fileInfo.getFileName());
                        }
                    } catch (RemoteException e) {
                        PhotoShareUtils.dealRemoteException(e);
                    }
                } else if (PhotoShareUtils.getServer().isShareFileDownloading(fileInfo, 0) == 0) {
                    addDownLoadResult = PhotoShareUtils.getServer().downloadSharePhotoThumb(info, 0, 1, false);
                    downloadOrigin = true;
                    GalleryLog.v("PhotoSharePhotoPage", "download origin FileName = " + fileInfo.getFileName());
                }
                if (!(downloadOrigin || PhotoShareUtils.isFileExists(fileInfo.getLocalBigThumbPath()))) {
                    if (1 == folderType) {
                        addDownLoadResult = PhotoShareUtils.getServer().downloadPhotoThumb(info, 1, 1, false);
                        this.mIsDownloadFailed = false;
                        GalleryLog.v("PhotoSharePhotoPage", "download lcd FileName = " + fileInfo.getFileName());
                    } else {
                        addDownLoadResult = PhotoShareUtils.getServer().downloadSharePhotoThumb(info, 1, 1, false);
                        if (addDownLoadResult == 0) {
                            z = false;
                        }
                        this.mIsDownloadFailed = z;
                        GalleryLog.v("PhotoSharePhotoPage", "download lcd FileName = " + fileInfo.getFileName());
                    }
                }
                onAddDownloadResult(addDownLoadResult, downloadOrigin, fileInfo, folderType);
            }
        }
    }

    private boolean onStateCheck(PhotoShareMediaItem photoShareItem, FileInfo fileInfo) {
        if (photoShareItem.isThumbNail() && !TextUtils.isEmpty(fileInfo.getFileId())) {
            return true;
        }
        changePhotoShareDownloadAction(false);
        return false;
    }

    private void onAddDownloadResult(int addDownLoadResult, boolean downloadOrigin, FileInfo fileInfo, int folderType) {
        if (addDownLoadResult == 0) {
            int i;
            String albumId;
            Long valueOf = Long.valueOf(0);
            Long valueOf2 = Long.valueOf(0);
            if (downloadOrigin) {
                i = 0;
            } else {
                i = 1;
            }
            int fileType = fileInfo.getFileType();
            String hash = fileInfo.getHash();
            if (1 == folderType) {
                albumId = fileInfo.getAlbumId();
            } else {
                albumId = fileInfo.getShareId();
            }
            setPhotoShareDownLoadVisibility(valueOf, valueOf2, i, fileType, hash, albumId);
        }
        if (downloadOrigin && addDownLoadResult == 0) {
            changePhotoShareDownloadAction(true);
        } else {
            changePhotoShareDownloadAction(false);
        }
    }

    protected void onResume() {
        super.onResume();
        if (this.mManager == null) {
            this.mManager = new PhotoPageLoadingManager(this.mHost.getActivity());
        }
        this.mManager.onResume();
        PhotoShareUtils.addListener(this);
    }

    public boolean photoShareDownLoadOrigin() {
        Context context = this.mHost.getActivity();
        if (context == null) {
            return false;
        }
        if (!PhotoShareUtils.isNetworkConnected(context)) {
            ContextedUtils.showToastQuickly(context, (int) R.string.photoshare_toast_nonetwork, 0);
            return false;
        } else if (PhotoShareUtils.isMobileNetConnected(context)) {
            GalleryLog.printDFXLog("DFX photoShareDownLoadOrigin isMobileNetConnected called");
            new Builder(context).setTitle(R.string.mobile_data_download_title).setMessage(context.getString(R.string.mobile_data_download_content, new Object[]{Formatter.formatFileSize(context, this.mCurrentPhoto.getFileInfo().getSize())})).setPositiveButton(R.string.photoshare_download_short, this.mAllowDataAccessListener).setNegativeButton(R.string.cancel, this.mAllowDataAccessListener).show();
            return false;
        } else {
            getGalleryActionBar().setProgress(0);
            return addDownloadOriginTask(false);
        }
    }

    private boolean addDownloadOriginTask(boolean canUseMobileNetWork) {
        PhotoShareMediaItem item = this.mCurrentPhoto;
        FileInfo fileInfo = item.getFileInfo();
        boolean downloadResult = item.photoShareDownLoadOperation(this.mHost.getActivity(), canUseMobileNetWork);
        if (downloadResult) {
            setPhotoShareDownLoadVisibility(Long.valueOf(0), Long.valueOf(0), 0, fileInfo.getFileType(), fileInfo.getHash(), 1 == item.getFolderType() ? fileInfo.getAlbumId() : fileInfo.getShareId());
            changePhotoShareDownloadAction(true);
        }
        return downloadResult;
    }

    protected boolean onItemSelected(Action action) {
        if (this.mCurrentPhoto == null) {
            return false;
        }
        Context context = this.mHost.getActivity();
        if (context == null) {
            return true;
        }
        ReportToBigData.reportCloudActionAtPhotoPage(action, isFamilyAlbumSet());
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
                if (needDownloadOrigin()) {
                    return true;
                }
                return super.onItemSelected(action);
            case 2:
                this.mOperationType = 2;
                PhotoShareUtils.showDeleteAlertDialog(context, this.mSourceType, this.mOnClickListener, this.mSelectionManager.getSelectedCount(), this.mMediaSet, true, isSyncAlbum(), isHicloudAlbum());
                return true;
            case 3:
                this.mOperationType = 1;
                photoShareDownLoadOrigin();
                return true;
            case 4:
                cancelDownloading();
                return true;
            case 5:
                onPhotoShareMove(context);
                return true;
            case 6:
                onPhotoShareNotThisPerson(context);
                return true;
            default:
                return super.onItemSelected(action);
        }
    }

    private boolean onPhotoShareNotThisPerson(Context context) {
        this.mOperationType = 3;
        if (!PhotoShareUtils.isNetworkConnected(context)) {
            ContextedUtils.showToastQuickly(this.mHost.getActivity(), (int) R.string.photoshare_toast_nonetwork, 0);
            return true;
        } else if (PhotoShareUtils.isNetAllowed(context)) {
            ReportToBigData.report(138, String.format("{OperationFrom:%s}", new Object[]{"PhotoPage"}));
            chooseTargetAlbum();
            return true;
        } else {
            new Builder(context).setTitle(R.string.photoshare_allow_title).setMessage(R.string.photoshare_allow_message).setPositiveButton(R.string.photoshare_allow_btn, this.mAllowDataAccessListener).setNegativeButton(R.string.cancel, this.mAllowDataAccessListener).show();
            return true;
        }
    }

    private boolean onPhotoShareMove(Context context) {
        if (PhotoShareUtils.isNetworkConnected(context)) {
            this.mOperationType = 4;
            if (PhotoShareUtils.isNetAllowed(context)) {
                PhotoShareUtils.showDeleteTagFileAlertDialog(this.mHost.getActivity(), this.mOnClickListener);
                return true;
            }
            new Builder(context).setTitle(R.string.photoshare_allow_title).setMessage(R.string.photoshare_allow_message).setPositiveButton(R.string.photoshare_allow_btn, this.mAllowDataAccessListener).setNegativeButton(R.string.cancel, this.mAllowDataAccessListener).show();
            return true;
        }
        ContextedUtils.showToastQuickly(this.mHost.getActivity(), (int) R.string.photoshare_toast_nonetwork, 0);
        return true;
    }

    private boolean needDownloadOrigin() {
        if (this.mCurrentPhoto == null) {
            return false;
        }
        Context context = this.mHost.getActivity();
        if (context == null) {
            return false;
        }
        PhotoShareMediaItem photoShareMediaItem = this.mCurrentPhoto;
        boolean isVideo = 4 == photoShareMediaItem.getFileInfo().getFileType();
        if (!photoShareMediaItem.isThumbNail() || !isVideo) {
            return false;
        }
        PhotoShareUtils.getPhotoShareDialog(context, context.getString(R.string.download_title, new Object[]{Formatter.formatFileSize(context, photoShareMediaItem.getFileInfo().getSize())}), (int) R.string.photoshare_download_short, (int) R.string.cancel, context.getString(R.string.download_video_when_edit, new Object[]{Formatter.formatFileSize(context, photoShareMediaItem.getFileInfo().getSize())}), this.mDownloadListener).show();
        return true;
    }

    private void chooseTargetAlbum() {
        Bundle data = new Bundle();
        data.putString("media-path", "/photoshare/exclude/*".replace("*", this.mMediaSet.getPath().getSuffix()));
        data.putString("exclude-path", this.mMediaSet.getPath().getSuffix());
        Intent intent = new Intent(this.mHost.getActivity(), PhotoShareTagAlbumSetActivity.class);
        intent.putExtras(data);
        this.mHost.getActivity().startActivityForResult(intent, 300);
    }

    protected void onPause() {
        super.onPause();
        if (this.mManager != null) {
            this.mManager.onPause();
        }
        PhotoShareUtils.removeListener(this);
    }

    protected void onPlayVideo(MediaItem origin) {
        if (origin instanceof PhotoShareVideo) {
            if (PhotoShareUtils.isFileExists(((PhotoShareVideo) origin).getFileInfo().getLocalRealPath())) {
                super.onPlayVideo(origin);
            } else {
                OnClickListener dialogButtonListener = new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (-1 == which && PhotoSharePhotoPage.this.photoShareDownLoadOrigin()) {
                            PhotoSharePhotoPage.this.mNeedToPlayAfterDownLoad = true;
                        }
                    }
                };
                PhotoShareUtils.getPhotoShareDialog(this.mHost.getActivity(), this.mHost.getActivity().getString(R.string.download_title, new Object[]{Formatter.formatFileSize(this.mHost.getActivity(), item.getFileInfo().getSize())}), (int) R.string.photoshare_download_short, (int) R.string.cancel, this.mHost.getActivity().getString(R.string.download_video_when_play, new Object[]{Formatter.formatFileSize(this.mHost.getActivity(), item.getFileInfo().getSize())}), dialogButtonListener).show();
                GalleryLog.printDFXLog("DFX onPlayVideo clicked");
            }
        }
    }

    protected void showBars(boolean barWithAnim) {
        if (this.mCurrentPhoto != null && (!(this.mCurrentPhoto instanceof PhotoShareImage) || ((PhotoShareImage) this.mCurrentPhoto).isLCDDownloaded() || this.mIsDownloadFailed)) {
            super.showBars(barWithAnim);
        }
    }

    protected boolean updateCurrentPhoto(MediaItem photo) {
        boolean changed = super.updateCurrentPhoto(photo);
        if (changed) {
            this.mNeedToPlayAfterDownLoad = false;
        } else {
            if (this.mNeedToPlayAfterDownLoad && PhotoShareUtils.isFileExists(((PhotoShareMediaItem) this.mCurrentPhoto).getFileInfo().getLocalRealPath())) {
                this.mNeedToPlayAfterDownLoad = false;
                playVideo(this.mHost.getActivity(), this.mCurrentPhoto.getPlayUri(), this.mCurrentPhoto.getName());
            }
            MenuEnableCtrller.updateMenuOperation(getGalleryActionBar().getCurrentMode(), this.mCurrentPhoto.getSupportedOperations());
        }
        if (this.mCurrentPhoto == null || ((this.mCurrentPhoto instanceof PhotoShareImage) && !((PhotoShareImage) this.mCurrentPhoto).isLCDDownloaded())) {
            hideBars(false);
        }
        return changed;
    }

    public void downloadProgress(String hash, String id, String uniqueId, int thumbType, Long totalSize, Long currentSize) {
        if (this.mCurrentPhoto != null) {
            PhotoShareMediaItem item = this.mCurrentPhoto;
            if (thumbType != 1 || this.mCurrentPhoto.getFileInfo().getFileType() != 4) {
                int albumType = item.getFolderType();
                FileInfo fileInfo = item.getFileInfo();
                if (!TextUtils.isEmpty(fileInfo.getFileId())) {
                    String albumId;
                    if (1 == albumType) {
                        albumId = fileInfo.getAlbumId();
                    } else {
                        albumId = fileInfo.getShareId();
                    }
                    if (fileInfo.getHash().equals(hash) && albumId.equals(id) && PhotoShareUtils.checkUniqueId(uniqueId, fileInfo) && !inEditorMode()) {
                        setPhotoShareDownLoadVisibility(totalSize, currentSize, thumbType, fileInfo.getFileType(), hash, id);
                    }
                }
            }
        }
    }

    private void setPhotoShareDownLoadVisibility(Long totalSize, Long currentSize, int thumbType, int fileType, String hash, String id) {
        if (this.mHost.getActivity() != null) {
            if (thumbType != 1 || fileType != 4) {
                getGalleryActionBar().setProgress(totalSize.longValue() == 0 ? 0 : (int) ((currentSize.longValue() * 100) / totalSize.longValue()));
                getGalleryActionBar().getCurrentMode().changeAction(Action.PHOTOSHARE_DOWNLOAD.ordinal(), Action.PHOTOSHARE_DOWNLOADING.ordinal());
                getGalleryActionBar().getCurrentMode().setActionEnable(true, Action.PHOTOSHARE_DOWNLOADING.ordinal());
            }
        }
    }

    protected boolean noActionBar() {
        if (this.mCurrentPhoto == null || !(this.mCurrentPhoto instanceof PhotoShareImage)) {
            return super.noActionBar();
        }
        boolean z = super.noActionBar() || !((PhotoShareImage) this.mCurrentPhoto).isLCDDownloaded();
        return z;
    }

    public void downloadFinish(String hash, String albumId, String uniqueId, int thumbType, int result) {
        if (this.mCurrentPhoto != null && hash != null && albumId != null) {
            PhotoShareMediaItem item = this.mCurrentPhoto;
            if (thumbType == 1 && this.mCurrentPhoto.getFileInfo().getFileType() == 4) {
                this.mIsDownloadFailed = true;
                return;
            }
            FileInfo fileInfo = item.getFileInfo();
            if (!TextUtils.isEmpty(fileInfo.getFileId()) && hash.equalsIgnoreCase(fileInfo.getHash()) && PhotoShareUtils.checkUniqueId(uniqueId, fileInfo) && this.mHost.getActivity() != null) {
                if (result != 0) {
                    onResultFail(thumbType);
                } else {
                    onResultSuccess(item, fileInfo);
                }
            }
        }
    }

    private void onResultFail(int thumbType) {
        ContextedUtils.showToastQuickly(this.mHost.getActivity(), (int) R.string.photoshare_download_fail_tips, 0);
        if (thumbType == 1) {
            this.mIsDownloadFailed = true;
        }
        if (thumbType == 0) {
            changePhotoShareDownloadAction(false);
        }
    }

    private void onResultSuccess(PhotoShareMediaItem item, FileInfo fileInfo) {
        if (this.mSourceType == 18 || this.mSourceType == 17) {
            PhotoShareUtils.notifyPhotoShareTagContentChanged(this.mMediaSet.getPath().getParent().getSuffix(), this.mMediaSet.getPath().getSuffix());
        }
        if (!item.isThumbNail() || TextUtils.isEmpty(fileInfo.getFileId())) {
            changePhotoShareDownloadAction(false);
        }
    }

    private void cancelDownloading() {
        if (this.mCurrentPhoto != null) {
            PhotoShareMediaItem item = this.mCurrentPhoto;
            FileInfo fileInfo = item.getFileInfo();
            if (!TextUtils.isEmpty(fileInfo.getFileId())) {
                String albumId;
                boolean isShare = false;
                if (1 == item.getFolderType()) {
                    albumId = fileInfo.getAlbumId();
                } else {
                    isShare = true;
                    albumId = fileInfo.getShareId();
                }
                try {
                    int result;
                    GalleryLog.v("PhotoSharePhotoPage", "cancelDownloading albumId " + albumId + " hash " + fileInfo.getHash());
                    FileInfo[] fileInfoList = new FileInfo[]{fileInfo};
                    if (isShare) {
                        result = PhotoShareUtils.getServer().cancelShareDownloadTask(fileInfoList, 0);
                    } else {
                        result = PhotoShareUtils.getServer().cancelDownloadPhotoThumb(fileInfoList, 0);
                    }
                    GalleryLog.v("PhotoSharePhotoPage", "cancel DownloadTask result" + result);
                } catch (RemoteException e) {
                    PhotoShareUtils.dealRemoteException(e);
                }
                changePhotoShareDownloadAction(false);
            }
        }
    }

    private void showProgressDialog(String message) {
        this.mProgressDialog = new ProgressDialog(this.mHost.getActivity());
        this.mProgressDialog.setCancelable(false);
        this.mProgressDialog.setMessage(message);
        this.mProgressDialog.show();
    }

    private void dismissProgressDialog() {
        GalleryLog.printDFXLog("dismissProgressDialog log for DFX");
        if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
            this.mProgressDialog.dismiss();
            this.mProgressDialog = null;
        }
    }

    protected void onStateResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 300:
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
                            default:
                                break;
                        }
                    }
                }
                return;
            default:
                super.onStateResult(requestCode, resultCode, data);
                break;
        }
    }

    private boolean isFamilyAlbumSet() {
        if (this.mMediaSet == null || this.mMediaSet.getAlbumType() != 7) {
            return false;
        }
        return true;
    }

    public void onPhotoTranslationChange(float x, float y, int index, boolean visible, MediaItem item) {
        boolean z = false;
        boolean lcdDownloaded = true;
        if (item != null && (item instanceof PhotoShareMediaItem)) {
            lcdDownloaded = ((PhotoShareMediaItem) item).isLCDDownloaded();
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

    protected void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        if (this.mManager != null) {
            this.mManager.onConfigurationChanged();
        }
    }

    protected boolean isSyncAlbum() {
        return false;
    }

    protected boolean isHicloudAlbum() {
        if ((this.mMediaSet instanceof DiscoverLocation) || this.mMediaSet.getAlbumType() == 5 || this.mMediaSet.getAlbumType() == 6) {
            return true;
        }
        return false;
    }
}
