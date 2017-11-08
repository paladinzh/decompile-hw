package com.huawei.gallery.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.android.gallery3d.R;
import com.android.gallery3d.app.Config$CloudSharePage;
import com.android.gallery3d.app.Config$LocalCameraAlbumPage;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.data.PhotoShareAlbum;
import com.android.gallery3d.data.PhotoShareAlbumSet;
import com.android.gallery3d.data.PhotoShareMediaItem;
import com.android.gallery3d.data.PhotoShareShareAlbumInfo;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MultiWindowStatusHolder;
import com.android.gallery3d.util.MultiWindowStatusHolder.IMultiWindowModeChangeListener;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.android.cg.vo.FileInfo;
import com.huawei.android.cg.vo.FileInfoDetail;
import com.huawei.android.cg.vo.ShareInfo;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionMode;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.app.MediaItemsDataLoader.LoadCountListener;
import com.huawei.gallery.app.TimeBucketPage.LayoutSpec;
import com.huawei.gallery.photoshare.ui.PhotoShareAlertDialogFragment;
import com.huawei.gallery.photoshare.ui.PhotoShareAlertDialogFragment.onDialogButtonClickListener;
import com.huawei.gallery.photoshare.ui.PhotoShareCreatingFamilyShareActivity;
import com.huawei.gallery.photoshare.ui.PhotoShareEditFriendsActivity;
import com.huawei.gallery.photoshare.ui.PhotoShareShowMemberActivity;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.photoshare.utils.PhotoShareVoHelper;
import com.huawei.gallery.ui.ActionBarPlaceHolderView;
import com.huawei.gallery.ui.ActionBarPlaceHolderView.Listener;
import com.huawei.gallery.ui.ListSlotRender;
import com.huawei.gallery.ui.PhotoShareTimeListSlotRender;
import com.huawei.gallery.ui.PlaceHolderView;
import com.huawei.gallery.ui.RectView;
import com.huawei.gallery.ui.SlotView;
import com.huawei.gallery.ui.SlotView.SlotRenderInterface;
import com.huawei.gallery.util.ColorfulUtils;
import com.huawei.gallery.util.LayoutHelper;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.util.ArrayList;
import java.util.List;

public class PhotoShareTimeBucketPage extends CommonTimeBucketPage implements Listener {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    private static final Action[] PHOTOSHARE_FAMILY_SHARE_MENU = new Action[]{Action.PHOTOSHARE_ADDPICTURE, Action.PHOTOSHARE_CONTACT};
    private static final Action[] PHOTOSHARE_RECEIVER_MENU = new Action[]{Action.PHOTOSHARE_ADDPICTURE, Action.PHOTOSHARE_CONTACT, Action.PHOTOSHARE_CANCEL_RECEIVE};
    private static final Action[] PHOTOSHARE_RECEIVER_NOT_UPLOAD_MENU = new Action[]{Action.PHOTOSHARE_CONTACT, Action.PHOTOSHARE_CANCEL_RECEIVE};
    private static final Action[] PHOTOSHARE_SHARE_MENU = new Action[]{Action.PHOTOSHARE_ADDPICTURE, Action.PHOTOSHARE_CONTACT, Action.PHOTOSHARE_RENAME};
    private static final Action[] PHOTOSHARE_TIMEBUCKET_MENU_SUPPORT_DOWNLOAD = new Action[]{Action.SHARE, Action.PHOTOSHARE_MULTI_DOWNLOAD, Action.PHOTOSHARE_DELETE, Action.ALL};
    private ActionBarPlaceHolderView mActionBarPlaceHolderView;
    private PhotoShareTimeBucketDataLoader mAlbumDataAdapter;
    private OnClickListener mAllowDataAccessListener;
    private RectView mBottomCover;
    private Config$CloudSharePage mConfig;
    private AlertDialog mCreateDialog;
    private OnClickListener mDeleteClickListener;
    private LinearLayout mEmptyAlbumLayout;
    private String mFamilyAlbumTitle = null;
    private boolean mIsLayoutRtl;
    private LayoutSpec mLayoutSpec;
    private IMultiWindowModeChangeListener mMultiWindowModeChangeListener;
    private boolean mNeedUpdateMenu = false;
    private boolean mOnCreated = false;
    private ProgressDialog mProgressDialog;
    private OnClickListener mReNameDialogButtonListener;
    private EditText mSetNameTextView;
    private PlaceHolderView mTopCover;

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
            iArr[Action.EDIT.ordinal()] = 22;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[Action.EDIT_COMMENT.ordinal()] = 23;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[Action.GOTO_GALLERY.ordinal()] = 24;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[Action.HIDE.ordinal()] = 25;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[Action.INFO.ordinal()] = 26;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[Action.KEYGUARD_LIKE.ordinal()] = 27;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[Action.KEYGUARD_NOT_LIKE.ordinal()] = 28;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[Action.LOOPPLAY.ordinal()] = 29;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[Action.MAP.ordinal()] = 30;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[Action.MENU.ordinal()] = 31;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[Action.MORE_EDIT.ordinal()] = 32;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[Action.MOVE.ordinal()] = 33;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[Action.MOVEIN.ordinal()] = 34;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[Action.MOVEOUT.ordinal()] = 35;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[Action.MULTISCREEN.ordinal()] = 36;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[Action.MULTISCREEN_ACTIVITED.ordinal()] = 37;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[Action.MULTI_SELECTION.ordinal()] = 38;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[Action.MULTI_SELECTION_ON.ordinal()] = 39;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[Action.MYFAVORITE.ordinal()] = 40;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[Action.NO.ordinal()] = 41;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[Action.NONE.ordinal()] = 42;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[Action.NOT_MYFAVORITE.ordinal()] = 43;
        } catch (NoSuchFieldError e37) {
        }
        try {
            iArr[Action.OK.ordinal()] = 44;
        } catch (NoSuchFieldError e38) {
        }
        try {
            iArr[Action.PHOTOSHARE_ACCOUNT.ordinal()] = 45;
        } catch (NoSuchFieldError e39) {
        }
        try {
            iArr[Action.PHOTOSHARE_ADDPICTURE.ordinal()] = 1;
        } catch (NoSuchFieldError e40) {
        }
        try {
            iArr[Action.PHOTOSHARE_BACKUP.ordinal()] = 46;
        } catch (NoSuchFieldError e41) {
        }
        try {
            iArr[Action.PHOTOSHARE_CANCEL_RECEIVE.ordinal()] = 2;
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
            iArr[Action.PHOTOSHARE_CONTACT.ordinal()] = 3;
        } catch (NoSuchFieldError e45) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_PEOPLE_TAG.ordinal()] = 49;
        } catch (NoSuchFieldError e46) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_SHARE.ordinal()] = 50;
        } catch (NoSuchFieldError e47) {
        }
        try {
            iArr[Action.PHOTOSHARE_DELETE.ordinal()] = 4;
        } catch (NoSuchFieldError e48) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD.ordinal()] = 51;
        } catch (NoSuchFieldError e49) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOADING.ordinal()] = 52;
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
            iArr[Action.PHOTOSHARE_MULTI_DOWNLOAD.ordinal()] = 5;
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
            iArr[Action.PHOTOSHARE_RENAME.ordinal()] = 6;
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
        String userId = data.getString("user-id", "");
        if (!TextUtils.isEmpty(userId)) {
            if (userId.equalsIgnoreCase(PhotoShareUtils.getLoginUserId())) {
                Path path = Path.fromString(data.getString("media-path"));
                this.mFamilyAlbumTitle = data.getString("groupName");
                if (this.mHost.getGalleryContext().getDataManager().getMediaSet(path) == null) {
                    String familyID = path.getSuffix();
                    if (isFamilyIDExist(familyID)) {
                        Intent intent = new Intent();
                        intent.setClass(this.mHost.getActivity(), PhotoShareCreatingFamilyShareActivity.class);
                        data.clear();
                        data.putString("groupName", this.mFamilyAlbumTitle);
                        data.putString("familyID", familyID);
                        intent.putExtras(data);
                        try {
                            this.mHost.getActivity().startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            GalleryLog.v("PhotoShareTimeBucketPage", "AlbumType " + this.mMediaSet.getAlbumType() + " startActivity Exception");
                        }
                    }
                    this.mHost.getStateManager().finishState(this);
                    GalleryLog.v("PhotoShareTimeBucketPage", "set is null");
                    return;
                }
            }
            this.mHost.getStateManager().finishState(this);
            GalleryLog.v("PhotoShareTimeBucketPage", "userid is not login");
            return;
        }
        this.mConfig = Config$CloudSharePage.get(this.mHost.getActivity());
        super.onCreate(data, storedState);
        initViews();
        new Thread() {
            public void run() {
                if (PhotoShareUtils.getServer() != null) {
                    try {
                        PhotoShareUtils.getServer().refreshSingleShare(PhotoShareTimeBucketPage.this.mMediaSet.getAlbumInfo().getId());
                    } catch (RemoteException e) {
                        PhotoShareUtils.dealRemoteException(e);
                    }
                }
            }
        }.start();
        this.mIsLayoutRtl = GalleryUtils.isLayoutRTL();
        this.mOnCreated = true;
        this.mMultiWindowModeChangeListener = createModeChangeListener();
        this.mReNameDialogButtonListener = createButtonListener();
    }

    private IMultiWindowModeChangeListener createModeChangeListener() {
        return new IMultiWindowModeChangeListener() {
            public void multiWindowModeChangeCallback(boolean isInMultiWindowMode) {
                if (PhotoShareTimeBucketPage.this.mEmptyAlbumLayout != null && PhotoShareTimeBucketPage.this.mEmptyAlbumLayout.getVisibility() == 0) {
                    PhotoShareTimeBucketPage.this.updateEmptyLayout(PhotoShareTimeBucketPage.this.mEmptyAlbumLayout);
                }
            }
        };
    }

    private OnClickListener createButtonListener() {
        return new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                boolean z = false;
                switch (which) {
                    case -1:
                        PhotoShareUtils.hideSoftInput(PhotoShareTimeBucketPage.this.mSetNameTextView);
                        if (PhotoShareUtils.isNetworkConnected(PhotoShareTimeBucketPage.this.mHost.getActivity())) {
                            final String fileName = PhotoShareTimeBucketPage.this.mSetNameTextView.getText().toString().trim();
                            if (PhotoShareUtils.isShareNameValid(PhotoShareTimeBucketPage.this.mHost.getActivity(), fileName) && PhotoShareUtils.checkCharValid(fileName, PhotoShareTimeBucketPage.this.mHost.getActivity())) {
                                if (!PhotoShareTimeBucketPage.this.mMediaSet.getName().equals(fileName)) {
                                    PhotoShareTimeBucketPage.this.showProgressDialog(PhotoShareTimeBucketPage.this.mHost.getActivity().getString(R.string.photoshare_progress_message_modify_share_folder_name));
                                    new Thread() {
                                        public void run() {
                                            final int finalResult = PhotoShareTimeBucketPage.this.mMediaSet.getAlbumInfo().modifyName(fileName);
                                            Handler handler = PhotoShareTimeBucketPage.this.mHandler;
                                            final String str = fileName;
                                            handler.post(new Runnable() {
                                                public void run() {
                                                    Context context = PhotoShareTimeBucketPage.this.mHost.getActivity();
                                                    if (context != null) {
                                                        if (finalResult == 0) {
                                                            ActionMode am = (ActionMode) PhotoShareTimeBucketPage.this.mActionBar.getCurrentMode();
                                                            PhotoShareTimeBucketPage.this.mMediaSet.setName(str);
                                                            am.setTitle(PhotoShareTimeBucketPage.this.mMediaSet.getName());
                                                        } else if (1 == finalResult) {
                                                            ContextedUtils.showToastQuickly(context, (int) R.string.photoshare_toast_modify_folder_fail_Toast, 0);
                                                        } else if (2 == finalResult) {
                                                            ContextedUtils.showToastQuickly(context, context.getString(R.string.photoshare_album_toast_modify_folder_fail_Toast, new Object[]{context.getString(R.string.photoshare_toast_fail_common_Toast)}), 0);
                                                        } else if (7 == finalResult) {
                                                            ContextedUtils.showToastQuickly(context, (int) R.string.create_album_file_exist_Toast, 0);
                                                        }
                                                    }
                                                    PhotoShareTimeBucketPage.this.dismissProgressDialog();
                                                }
                                            });
                                        }
                                    }.start();
                                    break;
                                }
                                ContextedUtils.showToastQuickly(PhotoShareTimeBucketPage.this.mHost.getActivity(), (int) R.string.create_album_file_exist_Toast, 0);
                                return;
                            }
                            PhotoShareTimeBucketPage.this.mSetNameTextView.setFocusable(true);
                            PhotoShareTimeBucketPage.this.mSetNameTextView.setCursorVisible(true);
                            PhotoShareTimeBucketPage.this.mSetNameTextView.requestFocusFromTouch();
                            return;
                        }
                        ContextedUtils.showToastQuickly(PhotoShareTimeBucketPage.this.mHost.getActivity(), (int) R.string.photoshare_toast_nonetwork, 0);
                        return;
                        break;
                    default:
                        PhotoShareUtils.hideSoftInput(PhotoShareTimeBucketPage.this.mSetNameTextView);
                        GalleryUtils.setDialogDismissable(dialog, true);
                        StringBuilder append = new StringBuilder().append("PhotoShareTimeBucketPage for DFX mCreateDialog ");
                        if (PhotoShareTimeBucketPage.this.mCreateDialog == null) {
                            z = true;
                        }
                        GalleryLog.printDFXLog(append.append(z).toString());
                        if (PhotoShareTimeBucketPage.this.mCreateDialog != null) {
                            GalleryUtils.setDialogDismissable(PhotoShareTimeBucketPage.this.mCreateDialog, true);
                            GalleryUtils.dismissDialogSafely(PhotoShareTimeBucketPage.this.mCreateDialog, null);
                            PhotoShareTimeBucketPage.this.mCreateDialog = null;
                            break;
                        }
                        break;
                }
            }
        };
    }

    protected int[] getNotUpdateActions() {
        return new int[]{Action.ACTION_ID_PRINT, Action.ACTION_ID_PHOTOSHARE_DELETE};
    }

    protected void onHandleMessage(Message message) {
        Context context = this.mHost.getActivity();
        if (context != null) {
            switch (message.what) {
                case 10:
                    dismissProgressDialog();
                    if (message.arg1 == 0) {
                        this.mHost.getStateManager().finishState(this);
                        break;
                    }
                    ContextedUtils.showToastQuickly(context, context.getString(R.string.photoshare_toast_cancel_receive_fail, new Object[]{context.getString(R.string.photoshare_toast_fail_common_Toast)}), 0);
                    break;
                case 11:
                    if (message.arg1 != 0) {
                        ContextedUtils.showToastQuickly(context, String.format(context.getResources().getQuantityString(R.plurals.photoshare_toast_delete_file_fail, message.arg1), new Object[]{context.getResources().getString(R.string.photoshare_toast_fail_common_Toast)}), 0);
                    }
                    if (this.mSelectionManager.inSelectionMode()) {
                        this.mSelectionManager.leaveSelectionMode();
                    }
                    if (message.arg2 != 0) {
                        PhotoShareUtils.notifyPhotoShareContentChange(3, this.mMediaSet.getPath().getSuffix());
                    }
                    dismissProgressDialog();
                    break;
                case 12:
                    if (message.arg2 == 0) {
                        if (message.arg1 != 0) {
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
                    break;
                default:
                    super.onHandleMessage(message);
                    break;
            }
        }
    }

    protected boolean onCreateActionBar(Menu menu) {
        this.mHost.requestFeature(296);
        if (this.mSelectionManager.inSelectionMode()) {
            return true;
        }
        onInflateMenu(menu);
        ActionMode tm = this.mActionBar.enterStandardTitleActionMode(false);
        if (TextUtils.isEmpty(this.mFamilyAlbumTitle)) {
            tm.setTitle(this.mMediaSet.getName());
        } else {
            tm.setTitle(this.mFamilyAlbumTitle);
        }
        updateMenu(tm);
        tm.show();
        return true;
    }

    private void updateMenu(final ActionMode tm) {
        if (this.mMediaSet.getAlbumType() == 3) {
            if (this.mNeedUpdateMenu) {
                new Thread() {
                    public void run() {
                        try {
                            ShareInfo shareInfo = PhotoShareUtils.getServer().getShare(PhotoShareTimeBucketPage.this.mMediaSet.getPath().getSuffix());
                            if (shareInfo != null) {
                                PhotoShareTimeBucketPage.this.mMediaSet.setAlbumInfo(new PhotoShareShareAlbumInfo(shareInfo));
                            }
                            if (PhotoShareTimeBucketPage.this.mHost.getActivity() != null) {
                                Handler handler = PhotoShareTimeBucketPage.this.mHandler;
                                final ActionMode actionMode = tm;
                                handler.post(new Runnable() {
                                    public void run() {
                                        PhotoShareTimeBucketPage.this.updatePrivilegeReceiverMenu(actionMode);
                                    }
                                });
                            }
                        } catch (RemoteException e) {
                            PhotoShareUtils.dealRemoteException(e);
                        }
                    }
                }.start();
            } else {
                updatePrivilegeReceiverMenu(tm);
            }
        } else if (this.mMediaSet.getAlbumType() == 2) {
            tm.setMenu(PHOTOSHARE_SHARE_MENU.length, PHOTOSHARE_SHARE_MENU);
        } else {
            tm.setMenu(PHOTOSHARE_FAMILY_SHARE_MENU.length, PHOTOSHARE_FAMILY_SHARE_MENU);
        }
        this.mNeedUpdateMenu = false;
    }

    private void updatePrivilegeReceiverMenu(ActionMode tm) {
        if ("0".equals(this.mMediaSet.getAlbumInfo().getShareInfo().getLocalThumbPath().get(0))) {
            tm.setMenu(PHOTOSHARE_RECEIVER_MENU.length, PHOTOSHARE_RECEIVER_MENU);
        } else {
            tm.setMenu(PHOTOSHARE_RECEIVER_NOT_UPLOAD_MENU.length, PHOTOSHARE_RECEIVER_NOT_UPLOAD_MENU);
        }
    }

    protected void onInflateMenu(Menu menu) {
        this.mMenu = PHOTOSHARE_TIMEBUCKET_MENU_SUPPORT_DOWNLOAD;
    }

    protected ListSlotRender createListSlotRender(Config$LocalCameraAlbumPage config) {
        return new PhotoShareTimeListSlotRender(this.mHost.getGalleryContext(), this.mSlotView, this.mSelectionManager, config.placeholderColor);
    }

    private void initViews() {
        this.mLayoutSpec = this.mConfig.layoutSpec;
        this.mTopCover = new PlaceHolderView(this.mHost.getActivity());
        this.mActionBarPlaceHolderView = new ActionBarPlaceHolderView(this);
        this.mBottomCover = new RectView(getBackgroundColor(this.mHost.getActivity()), true);
        this.mRootPane.addComponent(this.mSlotView);
        this.mRootPane.addComponent(this.mScrollBar);
        this.mRootPane.addComponent(this.mTopCover);
        this.mRootPane.addComponent(this.mBottomCover);
        this.mRootPane.addComponent(this.mActionBarPlaceHolderView);
        this.mDeleteClickListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    PhotoShareTimeBucketPage.this.onPhotoShareDelete();
                }
            }
        };
        this.mAllowDataAccessListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    PhotoShareUtils.startOpenNetService(PhotoShareTimeBucketPage.this.mHost.getActivity());
                    PhotoShareUtils.showDeleteAlertDialog(PhotoShareTimeBucketPage.this.mHost.getActivity(), PhotoShareTimeBucketPage.this.mMediaSet.getAlbumType(), PhotoShareTimeBucketPage.this.mDeleteClickListener, PhotoShareTimeBucketPage.this.mSelectionManager.getSelectedCount(), PhotoShareTimeBucketPage.this.mMediaSet, false, false, false);
                }
            }
        };
    }

    private void onPhotoShareDelete() {
        showProgressDialog(this.mHost.getActivity().getString(R.string.photoshare_deleting));
        new Thread() {
            public void run() {
                ArrayList<Path> selectedPaths = PhotoShareTimeBucketPage.this.mSelectionManager.getSelected(true);
                ArrayList<FileInfo> deleteList = new ArrayList();
                ArrayList<FileInfoDetail> detailList = new ArrayList();
                int failedCount = 0;
                DataManager dm = PhotoShareTimeBucketPage.this.mHost.getGalleryContext().getDataManager();
                for (int i = 0; i < selectedPaths.size(); i++) {
                    MediaObject obj = dm.getMediaObject((Path) selectedPaths.get(i));
                    if (obj != null) {
                        PhotoShareMediaItem item = (PhotoShareMediaItem) obj;
                        if (item.isPhotoSharePreView()) {
                            detailList.add(PhotoShareVoHelper.getFileInfoDetail(item.getFileInfo()));
                        } else {
                            deleteList.add(item.getFileInfo());
                        }
                    }
                    if (deleteList.size() == 100) {
                        failedCount += PhotoShareTimeBucketPage.this.deleteFiles(deleteList, PhotoShareTimeBucketPage.this.mMediaSet.getPath().getSuffix());
                        deleteList.clear();
                    }
                    if (detailList.size() == 100) {
                        PhotoShareTimeBucketPage.this.deleteFileInfoDetail((FileInfoDetail[]) detailList.toArray(new FileInfoDetail[detailList.size()]));
                        detailList.clear();
                    }
                }
                if (deleteList.size() > 0) {
                    failedCount += PhotoShareTimeBucketPage.this.deleteFiles(deleteList, PhotoShareTimeBucketPage.this.mMediaSet.getPath().getSuffix());
                }
                if (detailList.size() > 0) {
                    PhotoShareTimeBucketPage.this.deleteFileInfoDetail((FileInfoDetail[]) detailList.toArray(new FileInfoDetail[detailList.size()]));
                }
                PhotoShareTimeBucketPage.this.mHandler.sendMessage(PhotoShareTimeBucketPage.this.mHandler.obtainMessage(11, failedCount, failedCount == selectedPaths.size() ? 0 : 1));
            }
        }.start();
    }

    private int deleteFiles(List<FileInfo> deleteArray, String albumId) {
        List result = null;
        try {
            result = PhotoShareUtils.getServer().deleteShareFile(albumId, deleteArray);
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        if (result == null) {
            GalleryLog.v("PhotoShareTimeBucketPage", "deleteAll Files Ok");
            return 0;
        }
        GalleryLog.v("PhotoShareTimeBucketPage", "deleteAll Files Failed Size " + result.size());
        return 0;
    }

    private void deleteFileInfoDetail(FileInfoDetail[] fileInfoDetailArray) {
        try {
            PhotoShareUtils.getServer().deleteUploadHistory(fileInfoDetailArray);
            PhotoShareUtils.notifyPhotoShareFolderChanged(1);
            PhotoShareUtils.updateNotify();
            PhotoShareUtils.refreshStatusBar(false);
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
    }

    @SuppressWarnings({"SIC_INNER_SHOULD_BE_STATIC_ANON"})
    protected void pickPhotoWithAnimation(SlotView slotView, int request, Object index, int absIndex, MediaItem item) {
        if (inSelectionMode()) {
            super.pickPhotoWithAnimation(slotView, request, index, absIndex, item);
        } else if (item == null || !item.isPhotoShareUploadFailItem()) {
            super.pickPhotoWithAnimation(slotView, 106, index, absIndex, item);
        } else {
            Intent statusIntent = new Intent();
            statusIntent.setComponent(new ComponentName(this.mHost.getActivity(), PhotoShareUploadActivity.class));
            statusIntent.setAction("com.huawei.gallery.app.photoshare.statusbar.main");
            statusIntent.setFlags(268435456);
            statusIntent.putExtra("key-enter-from", "TimeBucket");
            this.mHost.getActivity().startActivity(statusIntent);
        }
    }

    protected void onGLRootLayout(int left, int top, int right, int bottom) {
        int relativeNavigationBarHeight;
        int height = bottom - top;
        boolean isLandScape = !LayoutHelper.isPort();
        boolean isPort = LayoutHelper.isPort();
        int statusBarHeight = MultiWindowStatusHolder.isInMultiMaintained() ? 0 : LayoutHelper.getStatusBarHeight();
        int actionBarHeight = this.mActionBar.getActionBarHeight();
        int navigationBarHeight = LayoutHelper.getNavigationBarHeight();
        int paddingTop = actionBarHeight + statusBarHeight;
        int paddingRight = isPort ? 0 : navigationBarHeight;
        if (!isPort || MultiWindowStatusHolder.isInMultiWindowMode()) {
            relativeNavigationBarHeight = 0;
        } else {
            relativeNavigationBarHeight = navigationBarHeight;
        }
        int paddingBottom = relativeNavigationBarHeight;
        this.mTopCover.layout(left, top, right, paddingTop);
        this.mBottomCover.layout(left, height - relativeNavigationBarHeight, right, height);
        if (this.mIsLayoutRtl) {
            this.mSlotView.layout(this.mLayoutSpec.local_camera_page_right_padding, paddingTop, (right - (this.mLayoutSpec.time_line_width + this.mLayoutSpec.local_camera_page_left_padding)) - (isLandScape ? LayoutHelper.getNavigationBarHeight() : 0), height - paddingBottom);
            this.mScrollBar.layout(left, paddingTop, ((right - paddingRight) - this.mLayoutSpec.time_line_width) + (this.mLayoutSpec.local_camera_page_right_padding - this.mLayoutSpec.local_camera_page_left_padding), height - paddingBottom);
        } else {
            this.mSlotView.layout(this.mLayoutSpec.time_line_width + this.mLayoutSpec.local_camera_page_left_padding, paddingTop, (right - this.mLayoutSpec.local_camera_page_right_padding) - paddingRight, height - paddingBottom);
            this.mScrollBar.layout((this.mLayoutSpec.time_line_width + left) - (this.mLayoutSpec.local_camera_page_right_padding - this.mLayoutSpec.local_camera_page_left_padding), paddingTop, right - paddingRight, height - paddingBottom);
        }
        this.mActionBarPlaceHolderView.layout(0, statusBarHeight, isPort ? right - left : (right - left) - navigationBarHeight, paddingTop);
    }

    protected void onGLRootRender(GLCanvas canvas) {
        if (this.mResumeEffect != null) {
            GalleryLog.printDFXLog("onGLRootRender for DFX");
            if (!this.mResumeEffect.draw(canvas)) {
                this.mResumeEffect = null;
                this.mSlotRender.setSlotFilter(null);
                onPhotoFallBackFinished();
            }
            this.mRootPane.invalidate();
        }
    }

    protected void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        updateEmptyLayout(this.mEmptyAlbumLayout);
    }

    protected AlbumDataLoader onCreateDataLoader(MediaSet mediaSet) {
        if (this.mAlbumDataAdapter == null) {
            this.mAlbumDataAdapter = new PhotoShareTimeBucketDataLoader(this.mHost.getGalleryContext(), mediaSet);
            this.mAlbumDataAdapter.setLoadCountListener(new LoadCountListener() {
                public void onLoadCountChange(int count) {
                    if (count == 0) {
                        PhotoShareTimeBucketPage.this.showEmptyAlbum();
                    } else {
                        PhotoShareTimeBucketPage.this.hideEmptyAlbum();
                    }
                }
            });
            setDataLoader(this.mAlbumDataAdapter);
        }
        return this.mAlbumDataAdapter;
    }

    protected boolean onItemSelected(final Action action) {
        Context context = this.mHost.getActivity();
        if (context == null) {
            return true;
        }
        ReportToBigData.reportCloudSelectActionAtAlbumPage(action, this.mSelectedCount, this.mMediaSet.getAlbumType() == 7);
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
                Intent request = new Intent(context, ListAlbumPickerActivity.class).setAction("android.intent.action.GET_CONTENT").setType("*/*");
                request.putExtra("support-multipick-items", true);
                this.mHost.getActivity().startActivityForResult(request, 120);
                return true;
            case 2:
                GalleryLog.printDFXLog("PhotoShareTimeBucketPage");
                PhotoShareAlertDialogFragment cancelMyReceiveDialog = PhotoShareAlertDialogFragment.newInstance(context.getString(R.string.photoshare_cancel_my_receive), context.getString(R.string.photoshare_cancel_my_receive_desc), context.getString(R.string.photoshare_cancel_my_receive), true);
                cancelMyReceiveDialog.show(this.mHost.getActivity().getSupportFragmentManager(), "");
                cancelMyReceiveDialog.setOnDialogButtonClickListener(new onDialogButtonClickListener() {
                    public void onPositiveClick() {
                        PhotoShareTimeBucketPage.this.showProgressDialog(PhotoShareTimeBucketPage.this.mHost.getActivity().getString(action.textResID));
                        new Thread() {
                            public void run() {
                                try {
                                    PhotoShareTimeBucketPage.this.mHandler.sendMessage(PhotoShareTimeBucketPage.this.mHandler.obtainMessage(10, PhotoShareUtils.getServer().cancelReceiveShare(PhotoShareTimeBucketPage.this.mMediaSet.getAlbumInfo().getId()), 0));
                                } catch (RemoteException e) {
                                    PhotoShareUtils.dealRemoteException(e);
                                    PhotoShareTimeBucketPage.this.mHandler.sendMessage(PhotoShareTimeBucketPage.this.mHandler.obtainMessage(10, -1, 0));
                                }
                            }
                        }.start();
                    }
                });
                return true;
            case 3:
                Bundle bundle = new Bundle();
                Intent intent = new Intent();
                if (this.mMediaSet.getAlbumType() == 3) {
                    intent.setClass(this.mHost.getActivity(), PhotoShareShowMemberActivity.class);
                    bundle.putString("sharePath", this.mMediaSet.getPath().toString());
                    bundle.putString("shareName", this.mMediaSet.getName());
                } else if (this.mMediaSet.getAlbumType() == 2) {
                    intent.setClass(this.mHost.getActivity(), PhotoShareEditFriendsActivity.class);
                    bundle.putString("sharePath", this.mMediaSet.getPath().toString());
                    bundle.putString("shareName", this.mMediaSet.getName());
                } else if (this.mMediaSet.getAlbumType() == 7) {
                    intent.setAction("com.huawei.android.cg.startSnsActivity");
                    bundle.putInt("groupUiType", 2);
                    bundle.putLong("groupId", Long.parseLong(this.mMediaSet.getPath().getSuffix()));
                }
                intent.putExtras(bundle);
                try {
                    this.mHost.getActivity().startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    GalleryLog.v("PhotoShareTimeBucketPage", "AlbumType " + this.mMediaSet.getAlbumType() + " startActivity Exception");
                }
                ReportToBigData.reportGotoCloudAlbumMember(this.mMediaSet.getAlbumType() == 7);
                return true;
            case 4:
                PhotoShareUtils.showDeleteAlertDialog(this.mHost.getActivity(), this.mMediaSet.getAlbumType(), this.mDeleteClickListener, this.mSelectionManager.getSelectedCount(), this.mMediaSet, false, false, false);
                return true;
            case 5:
                onPhotoShareDownLoad();
                return true;
            case 6:
                createDialogIfNeeded(this.mMediaSet.getName(), R.string.rename, this.mReNameDialogButtonListener);
                return true;
            default:
                return super.onItemSelected(action);
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
                    PhotoShareUtils.showSoftInput(PhotoShareTimeBucketPage.this.mSetNameTextView);
                }
            }, 300);
            return;
        }
        GalleryLog.d("PhotoShareTimeBucketPage", "The dialog is showing, do not create any more");
    }

    private void onPhotoShareDownLoad() {
        if (PhotoShareUtils.isNetworkConnected(this.mHost.getActivity())) {
            showProgressDialog(this.mHost.getActivity().getString(R.string.photoshare_add_downloading_task));
            new Thread() {
                public void run() {
                    ArrayList<Path> selectedPaths = PhotoShareTimeBucketPage.this.mSelectionManager.getSelected(true);
                    ArrayList<FileInfo> list = new ArrayList();
                    DataManager dm = PhotoShareTimeBucketPage.this.mHost.getGalleryContext().getDataManager();
                    for (int i = 0; i < selectedPaths.size(); i++) {
                        MediaObject obj = dm.getMediaObject((Path) selectedPaths.get(i));
                        if (obj != null) {
                            PhotoShareMediaItem mediaItem = (PhotoShareMediaItem) obj;
                            if (!mediaItem.isPhotoSharePreView()) {
                                FileInfo fileInfo = mediaItem.getFileInfo();
                                if (PhotoShareAlbum.getLocalRealPath(PhotoShareTimeBucketPage.this.mMediaSet.getName(), fileInfo) == null) {
                                    list.add(fileInfo);
                                }
                            }
                        } else {
                            GalleryLog.v("PhotoShareTimeBucketPage", "PhotoShareTimeBucketPage onPhotoShareDownLoad object not exists. Path " + selectedPaths.get(i));
                        }
                    }
                    if (list.size() == 0) {
                        PhotoShareTimeBucketPage.this.mHandler.sendMessage(PhotoShareTimeBucketPage.this.mHandler.obtainMessage(12, 0, -1));
                        return;
                    }
                    PhotoShareTimeBucketPage.this.mHandler.sendMessage(PhotoShareTimeBucketPage.this.mHandler.obtainMessage(12, PhotoShareUtils.addDownLoadTask(list, PhotoShareTimeBucketPage.this.mMediaSet.getAlbumType()) + 0, 0));
                }
            }.start();
            return;
        }
        ContextedUtils.showToastQuickly(this.mHost.getActivity(), (int) R.string.photoshare_toast_nonetwork, 0);
    }

    private void cleanupEmptyButton() {
        if (this.mEmptyAlbumLayout != null && ((RelativeLayout) this.mHost.getActivity().findViewById(R.id.gallery_root)) != null) {
            this.mEmptyAlbumLayout = null;
        }
    }

    private boolean setupEmptyButton() {
        RelativeLayout galleryRoot = (RelativeLayout) this.mHost.getActivity().findViewById(R.id.gallery_root);
        if (galleryRoot == null) {
            return false;
        }
        this.mEmptyAlbumLayout = (LinearLayout) ((LayoutInflater) this.mHost.getActivity().getSystemService("layout_inflater")).inflate(R.layout.empty_album, galleryRoot, false);
        updateEmptyLayout(this.mEmptyAlbumLayout);
        galleryRoot.addView(this.mEmptyAlbumLayout);
        return true;
    }

    private void updateEmptyLayout(LinearLayout emptyLayout) {
        if (emptyLayout == null) {
            return;
        }
        if (MultiWindowStatusHolder.isInMultiWindowMode()) {
            emptyLayout.setPadding(0, 0, 0, 0);
            emptyLayout.setGravity(17);
            return;
        }
        emptyLayout.setPadding(0, this.mHost.getActivity().getResources().getDimensionPixelSize(R.dimen.empty_album_top_margin) + ((this.mActionBar == null ? 0 : this.mActionBar.getActionBarHeight()) + LayoutHelper.getStatusBarHeight()), LayoutHelper.isPort() ? 0 : LayoutHelper.getNavigationBarHeight(), 0);
        emptyLayout.setGravity(1);
    }

    protected void showEmptyAlbum() {
        if (this.mEmptyAlbumLayout != null || setupEmptyButton()) {
            this.mEmptyAlbumLayout.setVisibility(0);
        }
    }

    protected void hideEmptyAlbum() {
        if (this.mEmptyAlbumLayout != null) {
            this.mEmptyAlbumLayout.setVisibility(8);
        }
    }

    protected Rect getAnimSlotRect() {
        return this.mSlotView.getAnimRect();
    }

    protected void leaveSelectionMode() {
        this.mActionBar.leaveCurrentMode();
        if (this.mNeedUpdateMenu && (this.mActionBar.getCurrentMode() instanceof ActionMode)) {
            updateMenu((ActionMode) this.mActionBar.getCurrentMode());
        }
        this.mHost.requestFeature(296);
        this.mRootPane.requestLayout();
    }

    public void onNavigationBarChanged(boolean show, int height) {
        super.onNavigationBarChanged(show, height);
        updateEmptyLayout(this.mEmptyAlbumLayout);
    }

    protected void updateMenu(boolean isSizeZero) {
    }

    protected SlotView getSlotView() {
        return this.mSlotView;
    }

    protected void onResume() {
        if (this.mMediaSet.getAlbumType() == 7) {
            PhotoShareUtils.notifyPhotoShareContentChange(1, this.mMediaSet.getPath().getSuffix());
        }
        super.onResume();
        MultiWindowStatusHolder.registerMultiWindowModeChangeListener(this.mMultiWindowModeChangeListener, false);
    }

    protected void onPause() {
        super.onPause();
        if (this.mActionBar.getCurrentMode() != null) {
            this.mActionBar.getCurrentMode().hide();
        }
    }

    protected void onDestroy() {
        if (this.mOnCreated) {
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
            cleanupEmptyButton();
        }
    }

    protected void onLoadingFinished(boolean loadingFailed) {
        super.onLoadingFinished(loadingFailed);
        this.mNeedUpdateMenu = true;
        if (this.mActionBar != null && (this.mActionBar.getCurrentMode() instanceof ActionMode)) {
            updateMenu((ActionMode) this.mActionBar.getCurrentMode());
        }
    }

    protected void onLoadingStarted() {
        super.onLoadingStarted();
    }

    protected void onStateResult(int requestCode, int resultCode, Intent data) {
        boolean z = false;
        switch (requestCode) {
            case 102:
                if (data != null) {
                    this.mFocusIndex = data.getIntExtra("return-index-hint", 0);
                }
                GalleryLog.printDFXLog("PhotoShareTimeBucketPage REQUEST_PREVIEW called  for DFX");
                this.mSelectionManager.setSelectionListener(this);
                enterSelectionMode();
                this.mSelectionManager.updateSelectMode(this.mGetContent);
                if (resultCode == -1) {
                    this.mSlotView.invalidate();
                    break;
                }
                break;
            case 106:
                if (data != null) {
                    this.mFocusIndex = data.getIntExtra("return-index-hint", 0);
                    this.mSlotView.invalidate();
                    break;
                }
                return;
            case 120:
                if (resultCode == -1) {
                    final ArrayList<String> fileList = data.getStringArrayListExtra("select-item-list");
                    if (fileList != null) {
                        int size = fileList.size();
                        if (this.mMediaSet.getAlbumType() == 7) {
                            z = true;
                        }
                        ReportToBigData.reportAddCloudPicturesWithCount(size, z);
                        showProgressDialog(this.mHost.getActivity().getString(R.string.photoshare_adding_picture));
                        new Thread() {
                            public void run() {
                                ArrayList<String> filePath = PhotoShareUtils.getFilePathFromPathString(PhotoShareTimeBucketPage.this.mHost.getGalleryContext(), fileList);
                                ArrayList<String> fileNeedToAdd = PhotoShareUtils.checkMd5ExistsInShare(PhotoShareTimeBucketPage.this.mMediaSet.getAlbumInfo().getId(), filePath);
                                if (filePath.size() > fileNeedToAdd.size()) {
                                    PhotoShareUtils.showFileExitsTips(filePath.size() - fileNeedToAdd.size());
                                }
                                if (!fileNeedToAdd.isEmpty()) {
                                    final int result = PhotoShareTimeBucketPage.this.mMediaSet.getAlbumInfo().addFileToAlbum((String[]) fileNeedToAdd.toArray(new String[fileNeedToAdd.size()]));
                                    GalleryLog.v("PhotoShareTimeBucketPage", "addFileToShare result " + result);
                                    new Handler(PhotoShareTimeBucketPage.this.mHost.getActivity().getMainLooper()).post(new Runnable() {
                                        public void run() {
                                            if (result == 4) {
                                                ContextedUtils.showToastQuickly(PhotoShareTimeBucketPage.this.mHost.getActivity(), PhotoShareTimeBucketPage.this.mHost.getActivity().getString(R.string.no_privilege_upload), 0);
                                            } else if (result != 0) {
                                                ContextedUtils.showToastQuickly(PhotoShareTimeBucketPage.this.mHost.getActivity(), PhotoShareTimeBucketPage.this.mHost.getActivity().getString(R.string.add_photo_to_cloudAlbum_failed), 0);
                                            } else {
                                                PhotoShareUtils.enableUploadStatusBarNotification(true);
                                                PhotoShareUtils.refreshStatusBar(false);
                                            }
                                        }
                                    });
                                }
                                new Handler(PhotoShareTimeBucketPage.this.mHost.getActivity().getMainLooper()).post(new Runnable() {
                                    public void run() {
                                        PhotoShareTimeBucketPage.this.dismissProgressDialog();
                                    }
                                });
                            }
                        }.start();
                        break;
                    }
                }
                break;
            default:
                super.onStateResult(requestCode, resultCode, data);
                break;
        }
    }

    protected SlotRenderInterface getSlotRenderInterface() {
        return this.mSlotRender;
    }

    public void onBlurWallpaperChanged() {
        this.mTopCover.textureDirty();
    }

    private void showProgressDialog(String message) {
        this.mProgressDialog = new ProgressDialog(this.mHost.getActivity());
        this.mProgressDialog.setCancelable(false);
        this.mProgressDialog.setMessage(message);
        this.mProgressDialog.show();
    }

    private void dismissProgressDialog() {
        if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
            this.mProgressDialog.dismiss();
            this.mProgressDialog = null;
        }
    }

    public void onFirstRenderAfterUpdateActionBarPlaceHolderContent() {
        GLHost glHost = this.mHost;
        if (needSplitActionBarHide() && glHost != null) {
            Activity activity = glHost.getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        GalleryActionBar actionBar = PhotoShareTimeBucketPage.this.mActionBar;
                        if (actionBar != null) {
                            actionBar.hideHeadActionContainer();
                        }
                    }
                });
            }
        }
    }

    protected void onClickSlotAnimationStart() {
        copyActionBarToGL(this.mActionBarPlaceHolderView);
    }

    protected void onClearOpenAnimation() {
        this.mActionBarPlaceHolderView.setContent(null);
    }

    private boolean isFamilyIDExist(String groupID) {
        String[] groupArray = PhotoShareAlbumSet.getFamilyShare();
        if (groupArray == null || groupArray.length == 0) {
            return false;
        }
        for (String equals : groupArray) {
            if (equals.equals(groupID)) {
                return true;
            }
        }
        return false;
    }

    protected Config$LocalCameraAlbumPage getConfig() {
        return this.mConfig;
    }

    public void onResetMainView() {
    }
}
