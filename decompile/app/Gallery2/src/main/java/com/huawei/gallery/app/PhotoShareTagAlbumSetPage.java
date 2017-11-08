package com.huawei.gallery.app;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MotionEvent;
import android.widget.EditText;
import com.android.gallery3d.R;
import com.android.gallery3d.app.Config$PhotoShareTagAlbumSetPage;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.DiscoverLocation;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.data.PhotoShareTagAlbum;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.android.cg.vo.TagInfo;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionMode;
import com.huawei.gallery.actionbar.SelectionMode;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.ui.CommonAlbumSetSlotView;
import com.huawei.gallery.ui.PhotoShareTagAlbumSetSlotView;
import com.huawei.gallery.ui.PhotoShareTagAlbumSetSlotView.Listener;
import com.huawei.gallery.ui.ScrollSelectionManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PhotoShareTagAlbumSetPage extends CommonAlbumSetPage implements Listener, ScrollSelectionManager.Listener {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    private static final Action[] CHOOSE_TARGET_TAG_ALBUM_MENU = new Action[]{Action.PHOTOSHARE_CREATE_NEW_PEOPLE_TAG, Action.PHOTOSHARE_REMOVE_PEOPLE_TAG};
    private static final Action[] TAG_ALBUM_LONG_PRESSED_MENU = new Action[]{Action.PHOTOSHARE_COMBINE, Action.PHOTOSHARE_MOVE, Action.ALL};
    private static final Comparator<MediaSet> mCountComparator = new Comparator<MediaSet>() {
        public int compare(MediaSet o1, MediaSet o2) {
            if (o1.getMediaItemCount() > o2.getMediaItemCount()) {
                return -1;
            }
            if (o1.getMediaItemCount() < o2.getMediaItemCount()) {
                return 1;
            }
            String name1 = o1.getName();
            if (name1 == null) {
                return 1;
            }
            String name2 = o2.getName();
            if (name2 == null) {
                return -1;
            }
            int result = name1.compareTo(name2);
            if (result < 0) {
                return -1;
            }
            return result > 0 ? 1 : 0;
        }
    };
    private static long sLastRefreshTime = 0;
    private OnClickListener mAllowDataAccessListener;
    private OnClickListener mCombineDialogButtonListener;
    private AlertDialog mCreateDialog;
    private String mExcludePath;
    private boolean mIsExclude;
    private boolean mIsLocalOnly = false;
    private long mLastClickTime = 0;
    private OnClickListener mMoveClickListener;
    private String mNewName;
    private int mOperationType = -1;
    private ProgressDialog mProgressDialog;
    private MediaSet mReNameAlbum;
    private ScrollSelectionManager mScrollSelectionManager;
    private EditText mSetNameTextView;
    private boolean mSupportRename = true;

    private static /* synthetic */ int[] -getcom-huawei-gallery-actionbar-ActionSwitchesValues() {
        if (-com-huawei-gallery-actionbar-ActionSwitchesValues != null) {
            return -com-huawei-gallery-actionbar-ActionSwitchesValues;
        }
        int[] iArr = new int[Action.values().length];
        try {
            iArr[Action.ADD.ordinal()] = 8;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Action.ADD_ALBUM.ordinal()] = 9;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Action.ADD_COMMENT.ordinal()] = 10;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Action.AIRSHARE.ordinal()] = 11;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Action.ALBUM.ordinal()] = 12;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Action.ALL.ordinal()] = 1;
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
            iArr[Action.DEALL.ordinal()] = 2;
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
            iArr[Action.EDIT.ordinal()] = 21;
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
            iArr[Action.NO.ordinal()] = 3;
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
            iArr[Action.PHOTOSHARE_COMBINE.ordinal()] = 4;
        } catch (NoSuchFieldError e44) {
        }
        try {
            iArr[Action.PHOTOSHARE_CONTACT.ordinal()] = 48;
        } catch (NoSuchFieldError e45) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_PEOPLE_TAG.ordinal()] = 5;
        } catch (NoSuchFieldError e46) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_SHARE.ordinal()] = 49;
        } catch (NoSuchFieldError e47) {
        }
        try {
            iArr[Action.PHOTOSHARE_DELETE.ordinal()] = 50;
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
            iArr[Action.PHOTOSHARE_MOVE.ordinal()] = 6;
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
            iArr[Action.PHOTOSHARE_REMOVE_PEOPLE_TAG.ordinal()] = 7;
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

    public void onScrollSelect(int index, boolean selected) {
        if (this.mSelectionManager.inSelectionMode() && !this.mSelectionManager.inSingleMode()) {
            MediaSet set = this.mDataLoader.getMediaSet(index);
            if (set != null) {
                Path path = set.getPath();
                if (this.mSelectionManager.isItemSelected(path) != selected) {
                    this.mSelectionManager.toggle(path);
                    updateMenu((SelectionMode) this.mActionBar.getCurrentMode());
                    this.mSlotView.invalidate();
                }
            }
        }
    }

    public boolean isSelected(int index) {
        if (!this.mSelectionManager.inSelectionMode()) {
            return false;
        }
        MediaSet set = this.mDataLoader.getMediaSet(index);
        if (set == null) {
            return false;
        }
        return this.mSelectionManager.isItemSelected(set.getPath());
    }

    protected boolean onCreateActionBar(Menu menu) {
        if (!this.mSelectionManager.inSelectionMode() || this.mGetContent) {
            if (this.mIsExclude) {
                this.mHost.requestFeature(256);
                ActionMode am = this.mActionBar.enterActionMode(false);
                am.setBothAction(Action.NO, Action.NONE);
                am.setTitle((int) R.string.cut_to);
                am.setMenu(2, CHOOSE_TARGET_TAG_ALBUM_MENU);
                am.show();
            } else {
                super.onCreateActionBar(menu);
            }
            return true;
        }
        this.mHost.requestFeature(296);
        return true;
    }

    protected void initializeData(Bundle data) {
        boolean z = true;
        super.initializeData(data);
        this.mSupportRename = data.getBoolean("support-rename", true);
        this.mIsLocalOnly = data.getBoolean("local-only", false);
        this.mExcludePath = data.getString("exclude-path", null);
        if (this.mExcludePath == null) {
            z = false;
        }
        this.mIsExclude = z;
        long now = System.currentTimeMillis();
        long interval = now - sLastRefreshTime;
        if (interval >= 45000 || interval <= 0) {
            setLastRefreshTime(now);
            refreshTag();
        } else {
            GalleryLog.v("PhotoShareTagAlbumSetPage", "REFRESH INTERVAL LESS THAN 45s");
        }
        this.mCombineDialogButtonListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case -1:
                        PhotoShareUtils.hideSoftInput(PhotoShareTagAlbumSetPage.this.mSetNameTextView);
                        String fileName = PhotoShareTagAlbumSetPage.this.mSetNameTextView.getText().toString().trim();
                        if (PhotoShareTagAlbumSetPage.this.mOperationType != 4) {
                            if (PhotoShareTagAlbumSetPage.this.mOperationType != 1) {
                                PhotoShareTagAlbumSetPage.this.afterInputAlbumName(fileName, PhotoShareTagAlbumSetPage.this.getMergedTagInfo());
                                break;
                            }
                            PhotoShareTagAlbumSetPage.this.afterInputAlbumName(fileName, new TagInfo[]{((PhotoShareTagAlbum) PhotoShareTagAlbumSetPage.this.mReNameAlbum).getTagInfo()});
                            break;
                        } else if (PhotoShareUtils.isShareNameValid(PhotoShareTagAlbumSetPage.this.mHost.getActivity(), fileName) && PhotoShareUtils.checkCharValid(fileName, PhotoShareTagAlbumSetPage.this.mHost.getActivity())) {
                            CharSequence excludeName = null;
                            if (PhotoShareTagAlbumSetPage.this.mIsExclude) {
                                try {
                                    TagInfo tagInfo = PhotoShareUtils.getServer().getTagInfo("0", PhotoShareTagAlbumSetPage.this.mExcludePath);
                                    if (tagInfo != null) {
                                        excludeName = tagInfo.getTagName();
                                    }
                                } catch (RemoteException e) {
                                    PhotoShareUtils.dealRemoteException(e);
                                }
                            }
                            if ((TextUtils.isEmpty(excludeName) || !fileName.equals(excludeName)) && !PhotoShareUtils.getAllTagName().contains(fileName)) {
                                Intent result = new Intent();
                                Bundle bundle = new Bundle();
                                bundle.putInt("result-kind", 3);
                                bundle.putString("tag-name", fileName);
                                result.putExtras(bundle);
                                PhotoShareTagAlbumSetPage.this.mHost.getActivity().setResult(-1, result);
                                PhotoShareTagAlbumSetPage.this.mHost.getActivity().finish();
                                break;
                            }
                            ContextedUtils.showToastQuickly(PhotoShareTagAlbumSetPage.this.mHost.getActivity(), (int) R.string.create_album_file_exist_Toast, 0);
                            return;
                        } else {
                            PhotoShareTagAlbumSetPage.this.mSetNameTextView.setFocusable(true);
                            PhotoShareTagAlbumSetPage.this.mSetNameTextView.setCursorVisible(true);
                            PhotoShareTagAlbumSetPage.this.mSetNameTextView.requestFocusFromTouch();
                            return;
                        }
                        break;
                    default:
                        PhotoShareUtils.hideSoftInput(PhotoShareTagAlbumSetPage.this.mSetNameTextView);
                        GalleryUtils.setDialogDismissable(dialog, true);
                        if (PhotoShareTagAlbumSetPage.this.mCreateDialog != null) {
                            GalleryUtils.setDialogDismissable(PhotoShareTagAlbumSetPage.this.mCreateDialog, true);
                            GalleryUtils.dismissDialogSafely(PhotoShareTagAlbumSetPage.this.mCreateDialog, null);
                            PhotoShareTagAlbumSetPage.this.mCreateDialog = null;
                            break;
                        }
                        break;
                }
            }
        };
        this.mAllowDataAccessListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    PhotoShareUtils.startOpenNetService(PhotoShareTagAlbumSetPage.this.mHost.getActivity());
                    if (PhotoShareTagAlbumSetPage.this.mOperationType == 1) {
                        PhotoShareTagAlbumSetPage.this.dealReNameAndCombine(PhotoShareTagAlbumSetPage.this.mNewName, new TagInfo[]{((PhotoShareTagAlbum) PhotoShareTagAlbumSetPage.this.mReNameAlbum).getTagInfo()});
                    } else if (PhotoShareTagAlbumSetPage.this.mOperationType == 2) {
                        PhotoShareTagAlbumSetPage.this.dealReNameAndCombine(PhotoShareTagAlbumSetPage.this.mSetNameTextView.getText().toString().trim(), PhotoShareTagAlbumSetPage.this.getSelected());
                    } else if (PhotoShareTagAlbumSetPage.this.mOperationType == 3) {
                        PhotoShareTagAlbumSetPage.this.showMoveAlertDialog();
                    }
                }
            }
        };
        this.mMoveClickListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    PhotoShareTagAlbumSetPage.this.onTagAlbumRemove();
                }
            }
        };
    }

    private void onTagAlbumRemove() {
        ReportToBigData.report(83);
        showProgressDialog(this.mHost.getActivity().getString(R.string.photoshare_moving_classify));
        new Thread() {
            public void run() {
                try {
                    List<TagInfo> result = PhotoShareUtils.getServer().deleteTagInfoList(PhotoShareTagAlbumSetPage.this.mMediaSet.getPath().getSuffix(), PhotoShareTagAlbumSetPage.this.getSelected());
                    final int failedCount = result == null ? 0 : result.size();
                    PhotoShareTagAlbumSetPage.this.mHandler.post(new Runnable() {
                        public void run() {
                            if (PhotoShareTagAlbumSetPage.this.mHost.getActivity() != null) {
                                PhotoShareTagAlbumSetPage.this.dismissProgressDialog();
                                PhotoShareTagAlbumSetPage.this.mSelectionManager.leaveSelectionMode();
                                GalleryLog.v("PhotoShareTagAlbumSetPage", " deleteTagInfoList failed count " + 0);
                            }
                        }
                    });
                } catch (RemoteException e) {
                    PhotoShareUtils.dealRemoteException(e);
                } finally {
                    PhotoShareTagAlbumSetPage.this.mHandler.post(/* anonymous class already generated */);
                }
            }
        }.start();
    }

    protected boolean onItemSelected(Action action) {
        GalleryLog.d("PhotoShareTagAlbumSetPage", "id = " + action);
        long now = System.currentTimeMillis();
        long interval = now - this.mLastClickTime;
        if (interval >= 300 || interval <= 0) {
            this.mLastClickTime = now;
            switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
                case 1:
                case 2:
                    if (this.mSelectionManager.inSelectAllMode()) {
                        this.mSelectionManager.deSelectAll();
                    } else {
                        this.mSelectionManager.selectAll();
                    }
                    ReportToBigData.report(95, String.format("{Action:%s}", new Object[]{action.toString()}));
                    break;
                case 3:
                    if (!this.mIsExclude) {
                        this.mSelectionManager.leaveSelectionMode();
                        break;
                    }
                    this.mHost.getActivity().finish();
                    return false;
                case 4:
                    createDialogIfNeeded(getDefaultName(), R.string.photoshare_combine_input_name, this.mCombineDialogButtonListener);
                    this.mOperationType = 2;
                    break;
                case 5:
                    createDialogIfNeeded(getDefaultName(), R.string.classify_input_name, this.mCombineDialogButtonListener);
                    this.mOperationType = 4;
                    break;
                case 6:
                    if (PhotoShareUtils.isNetworkConnected(this.mHost.getActivity())) {
                        this.mOperationType = 3;
                        if (PhotoShareUtils.isNetAllowed(this.mHost.getActivity())) {
                            showMoveAlertDialog();
                            break;
                        }
                        new Builder(this.mHost.getActivity()).setTitle(R.string.photoshare_allow_title).setMessage(R.string.photoshare_allow_message).setPositiveButton(R.string.photoshare_allow_btn, this.mAllowDataAccessListener).setNegativeButton(R.string.cancel, this.mAllowDataAccessListener).show();
                        return false;
                    }
                    ContextedUtils.showToastQuickly(this.mHost.getActivity(), (int) R.string.photoshare_toast_nonetwork, 0);
                    return true;
                case 7:
                    Intent result = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putInt("result-kind", 2);
                    result.putExtras(bundle);
                    this.mHost.getActivity().setResult(-1, result);
                    this.mHost.getActivity().finish();
                    break;
            }
            return true;
        }
        GalleryLog.v("PhotoShareTagAlbumSetPage", " CLICK INTERVAL LESS THAN 300");
        return false;
    }

    private void showMoveAlertDialog() {
        GalleryUtils.setTextColor(new Builder(this.mHost.getActivity()).setTitle(R.string.photoshare_move_tag_tips_shorter).setPositiveButton(R.string.photoshare_move_classify, this.mMoveClickListener).setNegativeButton(R.string.cancel, this.mMoveClickListener).show().getButton(-1), this.mHost.getActivity().getResources());
    }

    private String getDefaultName() {
        String name = "";
        List<Path> list = this.mSelectionManager.getSelected(false);
        DataManager dataManager = this.mHost.getGalleryContext().getDataManager();
        ArrayList<MediaSet> allList = new ArrayList();
        for (Path path : list) {
            MediaSet tempSet = dataManager.getMediaSet(path);
            if (tempSet != null) {
                allList.add(tempSet);
            }
        }
        Collections.sort(allList, mCountComparator);
        for (MediaSet set : allList) {
            if (!this.mUnNamedString.equalsIgnoreCase(set.getName())) {
                return set.getName();
            }
        }
        return name;
    }

    private ArrayList<String> getSelectedAlbumName(TagInfo[] selectedAlbums) {
        ArrayList<String> albumNames = new ArrayList();
        if (selectedAlbums != null) {
            for (TagInfo tagName : selectedAlbums) {
                String name = tagName.getTagName();
                if (!TextUtils.isEmpty(name)) {
                    albumNames.add(name);
                }
            }
        }
        return albumNames;
    }

    private void afterInputAlbumName(String fileName, TagInfo[] selectedAlbums) {
        if (!PhotoShareUtils.isShareNameValid(this.mHost.getActivity(), fileName) || !PhotoShareUtils.checkCharValid(fileName, this.mHost.getActivity())) {
            if (this.mSetNameTextView != null) {
                this.mSetNameTextView.setFocusable(true);
                this.mSetNameTextView.setCursorVisible(true);
                this.mSetNameTextView.requestFocusFromTouch();
            }
        } else if (PhotoShareUtils.getAllTagName().contains(fileName) && (this.mOperationType == 1 || !getSelectedAlbumName(selectedAlbums).contains(fileName))) {
            ContextedUtils.showToastQuickly(this.mHost.getActivity(), (int) R.string.create_album_file_exist_Toast, 0);
        } else if (!PhotoShareUtils.isNetworkConnected(this.mHost.getActivity())) {
            ContextedUtils.showToastQuickly(this.mHost.getActivity(), (int) R.string.photoshare_toast_nonetwork, 0);
        } else if (PhotoShareUtils.isNetAllowed(this.mHost.getActivity())) {
            dealReNameAndCombine(fileName, selectedAlbums);
        } else {
            this.mNewName = fileName;
            new Builder(this.mHost.getActivity()).setTitle(R.string.photoshare_allow_title).setMessage(R.string.photoshare_allow_message).setPositiveButton(R.string.photoshare_allow_btn, this.mAllowDataAccessListener).setNegativeButton(R.string.cancel, this.mAllowDataAccessListener).show();
        }
    }

    protected CommonAlbumSetSlotView onCreateSlotView() {
        PhotoShareTagAlbumSetSlotView slotView = new PhotoShareTagAlbumSetSlotView(this.mHost.getGalleryContext(), Config$PhotoShareTagAlbumSetPage.get(this.mHost.getActivity()).slotViewSpec);
        slotView.setListener(this);
        return slotView;
    }

    protected void initializeView() {
        super.initializeView();
        this.mScrollSelectionManager = new ScrollSelectionManager();
        this.mScrollSelectionManager.setListener(this);
    }

    private void dealReNameAndCombine(final String fileName, final TagInfo[] selectedAlbums) {
        int progressMessageID;
        if (this.mOperationType == 1) {
            ReportToBigData.report(85);
            progressMessageID = R.string.photoshare_progress_message_modify_share_folder_name;
        } else if (this.mOperationType == 2) {
            ReportToBigData.report(84);
            progressMessageID = R.string.photoshare_combining;
        } else {
            return;
        }
        GalleryLog.v("PhotoShareTagAlbumSetPage", "mOperationType=" + this.mOperationType + ",fileName " + fileName);
        showProgressDialog(this.mHost.getActivity().getString(progressMessageID));
        new Thread() {
            public void run() {
                int failedCount;
                try {
                    List<TagInfo> result = PhotoShareUtils.getServer().modifyTagInfoList(PhotoShareTagAlbumSetPage.this.mMediaSet.getPath().getSuffix(), selectedAlbums, fileName);
                    if (result != null) {
                        failedCount = result.size();
                    }
                } catch (RemoteException e) {
                    PhotoShareUtils.dealRemoteException(e);
                } finally {
                    failedCount = selectedAlbums == null ? 0 : selectedAlbums.length;
                    PhotoShareTagAlbumSetPage.this.mHandler.post(new Runnable() {
                        public void run() {
                            Context context = PhotoShareTagAlbumSetPage.this.mHost.getActivity();
                            if (context != null) {
                                PhotoShareTagAlbumSetPage.this.dismissProgressDialog();
                                PhotoShareTagAlbumSetPage.this.mSelectionManager.leaveSelectionMode();
                                GalleryLog.v("PhotoShareTagAlbumSetPage", "mOperationType =" + PhotoShareTagAlbumSetPage.this.mOperationType + ", modifyTagInfoList failed count " + failedCount);
                                if (failedCount > 0) {
                                    if (PhotoShareTagAlbumSetPage.this.mOperationType == 1) {
                                        ContextedUtils.showToastQuickly(context, (int) R.string.photoshare_rename_failed, 0);
                                    } else if (PhotoShareTagAlbumSetPage.this.mOperationType == 2) {
                                        ContextedUtils.showToastQuickly(context, (int) R.string.photoshare_combine_failed, 0);
                                    }
                                }
                            }
                        }
                    });
                }
            }
        }.start();
    }

    protected void showProgressDialog(String message) {
        this.mProgressDialog = new ProgressDialog(this.mHost.getActivity());
        this.mProgressDialog.setMessage(message);
        this.mProgressDialog.setCancelable(false);
        this.mProgressDialog.show();
    }

    protected void dismissProgressDialog() {
        if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
            this.mProgressDialog.dismiss();
            this.mProgressDialog = null;
        }
    }

    private void enterSelectionMode() {
        SelectionMode sm = this.mActionBar.enterSelectionMode(true);
        sm.setLeftAction(Action.NO);
        sm.setTitle((int) R.string.has_selected);
        sm.setRightAction(Action.NONE);
        sm.setMenu(TAG_ALBUM_LONG_PRESSED_MENU.length, TAG_ALBUM_LONG_PRESSED_MENU);
        sm.show();
        if (!this.mGetContent) {
            this.mHost.requestFeature(296);
            this.mRootPane.requestLayout();
        }
    }

    private void leaveSelectionMode() {
        if (!this.mGetContent) {
            this.mActionBar.leaveCurrentMode();
            this.mHost.requestFeature(298);
            this.mRootPane.requestLayout();
        }
    }

    private void updateMenu(SelectionMode mode) {
        if (mode != null) {
            int count = this.mSelectionManager.getSelectedCount();
            if (count == 0) {
                mode.setActionEnable(false, Action.ACTION_ID_PHOTOSHARE_RENAME);
                mode.setActionEnable(false, Action.ACTION_ID_PHOTOSHARE_MOVE);
                mode.setActionEnable(false, Action.ACTION_ID_PHOTOSHARE_COMBINE);
                return;
            }
            mode.setActionEnable(true, Action.ACTION_ID_PHOTOSHARE_MOVE);
            if (count == 1) {
                mode.setActionEnable(true, Action.ACTION_ID_PHOTOSHARE_RENAME);
                mode.setActionEnable(false, Action.ACTION_ID_PHOTOSHARE_COMBINE);
                return;
            }
            mode.setActionEnable(true, Action.ACTION_ID_PHOTOSHARE_COMBINE);
            mode.setActionEnable(false, Action.ACTION_ID_PHOTOSHARE_RENAME);
        }
    }

    private void selectAll() {
        int count = this.mSelectionManager.getSelectedCount();
        SelectionMode sm = (SelectionMode) this.mActionBar.getCurrentMode();
        sm.setTitle((int) R.string.has_selected);
        sm.setCount(count);
        sm.setActionEnable(true, Action.ALL.id);
        sm.changeAction(Action.ACTION_ID_ALL, Action.ACTION_ID_DEALL);
        updateMenu(sm);
        this.mRootPane.invalidate();
    }

    private void deSelectAll() {
        SelectionMode sm = (SelectionMode) this.mActionBar.getCurrentMode();
        sm.setTitle((int) R.string.no_selected);
        sm.setCount(null);
        sm.changeAction(Action.ACTION_ID_DEALL, Action.ACTION_ID_ALL);
        sm.setActionEnable(true, Action.ALL.id);
        updateMenu(sm);
        this.mRootPane.invalidate();
    }

    public void onSelectionModeChange(int mode) {
        switch (mode) {
            case 1:
                enterSelectionMode();
                break;
            case 2:
                leaveSelectionMode();
                break;
            case 3:
                selectAll();
                break;
            case 4:
                deSelectAll();
                break;
        }
        super.onSelectionModeChange(mode);
    }

    public void onSelectionChange(Path path, boolean selected) {
        int count = this.mSelectionManager.getSelectedCount();
        SelectionMode sm = (SelectionMode) this.mActionBar.getCurrentMode();
        if (count != this.mSelectionManager.getTotalCount()) {
            sm.changeAction(Action.ACTION_ID_DEALL, Action.ACTION_ID_ALL);
        }
        if (count == 0) {
            sm.setTitle((int) R.string.no_selected);
            sm.setCount(null);
            sm.setActionEnable(true, Action.ALL.id);
            return;
        }
        sm.setTitle((int) R.string.has_selected);
        sm.setCount(count);
        sm.setActionEnable(true, Action.ALL.id);
        if (!(count == 1 || count == this.mSelectionManager.getTotalCount())) {
            sm.setMenu(TAG_ALBUM_LONG_PRESSED_MENU.length, TAG_ALBUM_LONG_PRESSED_MENU);
        }
        updateMenu(sm);
        super.onSelectionChange(path, selected);
    }

    public void onSingleTapUp(int slotIndex, boolean cornerPressed) {
        if (this.mIsActive) {
            MediaSet set = this.mDataLoader.getMediaSet(slotIndex);
            if (set != null) {
                if (this.mIsExclude) {
                    Intent result = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putInt("result-kind", 1);
                    bundle.putString("target-tagID", set.getPath().getSuffix());
                    result.putExtras(bundle);
                    this.mHost.getActivity().setResult(-1, result);
                    this.mHost.getActivity().finish();
                    return;
                }
                if (this.mSelectionManager.inSelectionMode()) {
                    this.mSelectionManager.toggle(set.getPath());
                    updateMenu((SelectionMode) this.mActionBar.getCurrentMode());
                    this.mSlotView.invalidate();
                } else {
                    Bundle data = new Bundle();
                    data.putBoolean("get-content", false);
                    data.putString("media-path", set.getPath().toString());
                    data.putBoolean("local-only", this.mIsLocalOnly);
                    Intent intent = new Intent(this.mHost.getActivity(), PhotoShareAlbumActivity.class);
                    intent.putExtras(data);
                    this.mHost.getActivity().startActivity(intent);
                }
            }
        }
    }

    public void onScroll(int index) {
        super.onScroll(index);
        this.mScrollSelectionManager.addScrollIndex(index);
    }

    public void onTouchUp(MotionEvent event) {
        super.onTouchUp(event);
        this.mScrollSelectionManager.clearup();
    }

    public void onAlbumNameTapUp(int index) {
        reName(index);
    }

    public void onLongTap(int slotIndex) {
        super.onLongTap(slotIndex);
        if (!this.mIsExclude && !this.mSelectionManager.inSelectionMode()) {
            MediaSet set = this.mDataLoader.getMediaSet(slotIndex);
            if (set != null && !(set instanceof DiscoverLocation)) {
                this.mSelectionManager.toggle(set.getPath());
                updateMenu((SelectionMode) this.mActionBar.getCurrentMode());
                this.mSlotView.invalidate();
                this.mSlotView.startClickSlotAnimation(Integer.valueOf(slotIndex), null);
                ReportToBigData.report(94);
            }
        }
    }

    private void reName(int slotIndex) {
        if (this.mIsActive && this.mSupportRename) {
            MediaSet set = this.mDataLoader.getMediaSet(slotIndex);
            if (set != null && !this.mIsExclude && !this.mSelectionManager.inSelectionMode()) {
                String inputName;
                if (this.mUnNamedString.equalsIgnoreCase(set.getName())) {
                    inputName = "";
                } else {
                    inputName = set.getName();
                }
                createDialogIfNeeded(inputName, R.string.rename, this.mCombineDialogButtonListener);
                this.mOperationType = 1;
                this.mReNameAlbum = set;
            }
        }
    }

    private void createDialogIfNeeded(String defaultName, int titleID, OnClickListener clickListener) {
        if (this.mCreateDialog == null || !this.mCreateDialog.isShowing()) {
            this.mSetNameTextView = new EditText(this.mHost.getActivity());
            this.mSetNameTextView.setSingleLine(true);
            this.mCreateDialog = GalleryUtils.createDialog(this.mHost.getActivity(), defaultName, titleID, clickListener, null, this.mSetNameTextView);
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    PhotoShareUtils.showSoftInput(PhotoShareTagAlbumSetPage.this.mSetNameTextView);
                }
            }, 300);
            return;
        }
        GalleryLog.v("PhotoShareTagAlbumSetPage", " The dialog is showing, do not create any more");
    }

    private TagInfo[] getMergedTagInfo() {
        ArrayList<TagInfo> tagInfoList = getSelectedTagInfoList();
        for (int i = 0; i < tagInfoList.size(); i++) {
            TagInfo tagInfo = (TagInfo) tagInfoList.get(i);
            if (!TextUtils.isEmpty(tagInfo.getTagName())) {
                tagInfoList.remove(i);
                tagInfoList.add(0, tagInfo);
                break;
            }
        }
        return (TagInfo[]) tagInfoList.toArray(new TagInfo[tagInfoList.size()]);
    }

    private ArrayList<TagInfo> getSelectedTagInfoList() {
        List<Path> list = this.mSelectionManager.getSelected(false);
        DataManager dataManager = this.mHost.getGalleryContext().getDataManager();
        ArrayList<MediaSet> allList = new ArrayList();
        for (Path path : list) {
            MediaSet tempSet = dataManager.getMediaSet(path);
            if (tempSet != null) {
                allList.add(tempSet);
            }
        }
        ArrayList<TagInfo> tagInfoList = new ArrayList();
        for (MediaSet set : allList) {
            tagInfoList.add(((PhotoShareTagAlbum) set).getTagInfo());
        }
        return tagInfoList;
    }

    private TagInfo[] getSelected() {
        ArrayList<TagInfo> tagInfoList = getSelectedTagInfoList();
        return (TagInfo[]) tagInfoList.toArray(new TagInfo[tagInfoList.size()]);
    }

    private static void refreshTag() {
        new Thread() {
            public void run() {
                if (PhotoShareUtils.getServer() != null) {
                    try {
                        PhotoShareUtils.getServer().refreshTag();
                    } catch (RemoteException e) {
                        PhotoShareUtils.dealRemoteException(e);
                    }
                }
            }
        }.start();
    }

    private static void setLastRefreshTime(long time) {
        sLastRefreshTime = time;
    }
}
