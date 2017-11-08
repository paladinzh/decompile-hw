package com.huawei.gallery.app;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.android.gallery3d.R;
import com.android.gallery3d.app.Config$CommonAlbumFragment;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.LocalMergeCardAlbum;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.MenuExecutorFactory.Style;
import com.android.gallery3d.ui.SlotPreviewPhotoManager;
import com.android.gallery3d.ui.SlotPreviewPhotoManager.SlotPreviewModeListener;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.DrmUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MultiWindowStatusHolder;
import com.android.gallery3d.util.MultiWindowStatusHolder.IMultiWindowModeChangeListener;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.android.cg.vo.ShareInfo;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionBarStateBase;
import com.huawei.gallery.actionbar.ActionMode;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.actionbar.MergeCardActionMode;
import com.huawei.gallery.actionbar.SelectionMode;
import com.huawei.gallery.actionbar.StandardTitleActionMode;
import com.huawei.gallery.anim.PhotoFallbackEffect.PositionProvider;
import com.huawei.gallery.map.app.MapAlbumPage;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.ui.AbstractCommonAlbumSlotRender;
import com.huawei.gallery.ui.ActionBarPlaceHolderView;
import com.huawei.gallery.ui.CommonAlbumSlotView;
import com.huawei.gallery.ui.CommonAlbumSlotView.SimpleListener;
import com.huawei.gallery.ui.PlaceHolderView;
import com.huawei.gallery.ui.RectView;
import com.huawei.gallery.ui.ScrollSelectionManager;
import com.huawei.gallery.ui.ScrollSelectionManager.Listener;
import com.huawei.gallery.ui.SlotView;
import com.huawei.gallery.ui.SlotView.SlotRenderInterface;
import com.huawei.gallery.util.LayoutHelper;
import java.util.ArrayList;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class SlotAlbumPage extends AbsAlbumPage implements Listener, GalleryActionBar.Listener, MergeCardActionMode.Listener, ActionBarPlaceHolderView.Listener {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    private static final String TAG = "SlotAlbumPage";
    private ActionBarPlaceHolderView mActionBarPlaceHolderView;
    protected CommonAlbumDataLoader mAlbumDataAdapter;
    protected AbstractCommonAlbumSlotRender mAlbumRender;
    private RectView mBottomCover;
    private OnClickListener mCreateAlbumNetListener;
    private ProgressDialog mCreateAlbumProgressDialog;
    protected LinearLayout mEmptyAlbumLayout;
    protected boolean mIsEmptyAlbum = true;
    protected boolean mIsVideoAlbum;
    private IMultiWindowModeChangeListener mMultiWindowModeChangeListener;
    private String mNewPhotoShareName = null;
    private PositionProvider mPositionProvider = new PositionProvider() {
        public Rect getPosition(int index) {
            Rect rect = SlotAlbumPage.this.mSlotView.getSlotRect(index);
            Rect bounds = SlotAlbumPage.this.mSlotView.bounds();
            rect.offset(bounds.left - SlotAlbumPage.this.mSlotView.getScrollX(), bounds.top - SlotAlbumPage.this.mSlotView.getScrollY());
            return rect;
        }
    };
    private ScrollSelectionManager mScrollSelectionManager;
    private BroadcastReceiver mSdCardBrocardcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (SlotAlbumPage.this.mSelectionManager.inSelectionMode()) {
                SlotAlbumPage.this.mSelectionManager.leaveSelectionMode();
            }
            ActionBarStateBase mode = SlotAlbumPage.this.mActionBar.getCurrentMode();
            if (SlotAlbumPage.this.isCardActionMode(mode)) {
                String action = intent.getAction();
                if ("android.intent.action.MEDIA_MOUNTED".equals(action)) {
                    ((MergeCardActionMode) mode).updateCardLocationVisibility(0);
                    ((MergeCardActionMode) mode).updateLocationSelection(0);
                    SlotAlbumPage.this.onCardLocationFiltered(0);
                } else if ("android.intent.action.MEDIA_UNMOUNTED".equals(action)) {
                    ((MergeCardActionMode) mode).updateCardLocationVisibility(8);
                    SlotAlbumPage.this.onCardLocationFiltered(1);
                }
            }
        }
    };
    protected CommonAlbumSlotView mSlotView;
    private PlaceHolderView mTopCover;

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
            iArr[Action.OK.ordinal()] = 1;
        } catch (NoSuchFieldError e38) {
        }
        try {
            iArr[Action.PHOTOSHARE_ACCOUNT.ordinal()] = 39;
        } catch (NoSuchFieldError e39) {
        }
        try {
            iArr[Action.PHOTOSHARE_ADDPICTURE.ordinal()] = 40;
        } catch (NoSuchFieldError e40) {
        }
        try {
            iArr[Action.PHOTOSHARE_BACKUP.ordinal()] = 41;
        } catch (NoSuchFieldError e41) {
        }
        try {
            iArr[Action.PHOTOSHARE_CANCEL_RECEIVE.ordinal()] = 42;
        } catch (NoSuchFieldError e42) {
        }
        try {
            iArr[Action.PHOTOSHARE_CLEAR.ordinal()] = 43;
        } catch (NoSuchFieldError e43) {
        }
        try {
            iArr[Action.PHOTOSHARE_COMBINE.ordinal()] = 44;
        } catch (NoSuchFieldError e44) {
        }
        try {
            iArr[Action.PHOTOSHARE_CONTACT.ordinal()] = 45;
        } catch (NoSuchFieldError e45) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_PEOPLE_TAG.ordinal()] = 46;
        } catch (NoSuchFieldError e46) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_SHARE.ordinal()] = 47;
        } catch (NoSuchFieldError e47) {
        }
        try {
            iArr[Action.PHOTOSHARE_DELETE.ordinal()] = 48;
        } catch (NoSuchFieldError e48) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD.ordinal()] = 49;
        } catch (NoSuchFieldError e49) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOADING.ordinal()] = 50;
        } catch (NoSuchFieldError e50) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD_START.ordinal()] = 51;
        } catch (NoSuchFieldError e51) {
        }
        try {
            iArr[Action.PHOTOSHARE_EDITSHARE.ordinal()] = 52;
        } catch (NoSuchFieldError e52) {
        }
        try {
            iArr[Action.PHOTOSHARE_EMAIL.ordinal()] = 53;
        } catch (NoSuchFieldError e53) {
        }
        try {
            iArr[Action.PHOTOSHARE_LINK.ordinal()] = 54;
        } catch (NoSuchFieldError e54) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_DOWNLOAD.ordinal()] = 55;
        } catch (NoSuchFieldError e55) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_UPLOAD.ordinal()] = 56;
        } catch (NoSuchFieldError e56) {
        }
        try {
            iArr[Action.PHOTOSHARE_MESSAGE.ordinal()] = 57;
        } catch (NoSuchFieldError e57) {
        }
        try {
            iArr[Action.PHOTOSHARE_MOVE.ordinal()] = 58;
        } catch (NoSuchFieldError e58) {
        }
        try {
            iArr[Action.PHOTOSHARE_MULTI_DOWNLOAD.ordinal()] = 59;
        } catch (NoSuchFieldError e59) {
        }
        try {
            iArr[Action.PHOTOSHARE_NOT_THIS_PERSON.ordinal()] = 60;
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
        this.mIsVideoAlbum = data.getBoolean("only-local-camera-video-album", false);
        super.onCreate(data, storedState);
        if (isMergeCardAlbum()) {
            this.mCardLocation = data.getInt("key-camera-location", 0);
        }
        initViews();
        this.mNewPhotoShareName = data.getString("newShareName", null);
        if (data.getBoolean(AbsAlbumPage.KEY_AUTO_SELECT_ALL)) {
            this.mSelectionManager.selectAll();
        }
        this.mCreateAlbumNetListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    PhotoShareUtils.startOpenNetService(SlotAlbumPage.this.mHost.getActivity());
                    SlotAlbumPage.this.mHandler.postDelayed(new Runnable() {
                        public void run() {
                            SlotAlbumPage.this.createShareAlbum();
                        }
                    }, 50);
                }
            }
        };
        this.mMultiWindowModeChangeListener = new IMultiWindowModeChangeListener() {
            public void multiWindowModeChangeCallback(boolean isInMultiWindowMode) {
                if (SlotAlbumPage.this.mEmptyAlbumLayout != null && SlotAlbumPage.this.mEmptyAlbumLayout.getVisibility() == 0) {
                    SlotAlbumPage.this.updateEmptyLayout(SlotAlbumPage.this.mEmptyAlbumLayout);
                }
            }
        };
        this.mSlotPreviewPhotoManager = new SlotPreviewPhotoManager(this.mHost.getActivity(), this.mHost.getGLRoot());
        this.mSlotPreviewPhotoManager.setDelegate(this);
        this.mSlotPreviewPhotoManager.setListener(new SlotPreviewModeListener() {
            public void onEnterPreviewMode() {
                SlotAlbumPage.this.mSlotView.updatePreviewMode(true);
            }

            public void onLeavePreviewMode() {
                SlotAlbumPage.this.mSlotView.updatePreviewMode(false);
            }

            public void onStartPreview(MotionEvent event) {
                SlotAlbumPage.this.processPreview(event);
            }

            public void onClick(MotionEvent event) {
                if (event != null) {
                    int index = SlotAlbumPage.this.mSlotView.getSlotIndexByPosition(event.getX(), event.getY());
                    if (index >= 0) {
                        MediaItem item = SlotAlbumPage.this.mAlbumDataAdapter.get(index);
                        if (item != null) {
                            SlotAlbumPage.this.mSlotView.setIndexUp(index);
                            SlotAlbumPage.this.pickPhotoWithAnimation(SlotAlbumPage.this.mSlotView, 100, Integer.valueOf(index), index, item);
                        }
                    }
                }
            }

            public void onLongClick(MotionEvent event) {
                if (event != null) {
                    int slotIndex = SlotAlbumPage.this.mSlotView.getSlotIndexByPosition(event.getX(), event.getY());
                    if (!SlotAlbumPage.this.mGetContent && !SlotAlbumPage.this.mSelectionManager.inSelectionMode()) {
                        MediaItem item = SlotAlbumPage.this.mAlbumDataAdapter.get(slotIndex);
                        if (item != null && !item.isPhotoSharePreView()) {
                            SlotAlbumPage.this.mSelectionManager.toggle(item.getPath());
                            SlotAlbumPage.this.mSlotView.invalidate();
                            SlotAlbumPage.this.mSlotView.startClickSlotAnimation(Integer.valueOf(slotIndex), null);
                        }
                    }
                }
            }
        });
    }

    protected boolean onCreateActionBar(Menu menu) {
        this.mActionBar.setListener(this);
        if (!this.mSelectionManager.inSelectionMode() || this.mGetContent) {
            this.mHost.requestFeature(298);
            onInflateMenu(menu);
            if (this.mGetContent) {
                SelectionMode sm = this.mActionBar.enterSelectionMode(false);
                if (this.mSupportMultiPick) {
                    int count = this.mSelectionManager.getSelectedCount();
                    if (count == 0) {
                        sm.setTitle((int) R.string.no_selected);
                        sm.setCount(null);
                    } else {
                        sm.setTitle((int) R.string.has_selected);
                        if (this.mMaxSelectCount > 0) {
                            sm.setCount(count, this.mMaxSelectCount);
                        } else {
                            sm.setCount(count);
                        }
                    }
                    if (this.mMaxSelectCount > 0) {
                        this.mSelectionManager.setLimitExceedNum(this.mMaxSelectCount);
                    }
                    sm.setBothAction(Action.NO, Action.OK);
                    this.mSelectionManager.setAutoLeaveSelectionMode(false);
                } else {
                    sm.setTitle(this.mData.getInt("get-title", R.string.widget_type));
                    this.mSelectionManager.setSingleMode(true);
                }
                sm.show();
                this.mSelectionManager.enterSelectionMode();
            } else if (isMergeCardAlbum()) {
                MergeCardActionMode cam = this.mActionBar.enterMergeCardActionMode(false);
                cam.setTitle(this.mMediaSet.getName());
                cam.setBothAction(Action.NONE, Action.NONE);
                cam.show();
            } else {
                this.mActionBar.enterActionMode(false);
                StandardTitleActionMode am = this.mActionBar.enterStandardTitleActionMode(false);
                am.setTitle(this.mMediaSet.getName());
                am.setBothAction(Action.NONE, Action.NONE);
                am.show();
            }
            return true;
        }
        this.mHost.requestFeature(296);
        return true;
    }

    private void createShareAlbum() {
        if (this.mHost.getActivity() != null) {
            final ArrayList<Path> selectedItems = this.mSelectionManager.getSelected(true);
            showProgressDialog(this.mHost.getActivity().getString(R.string.photoshare_progress_message_create_new_share));
            new Thread() {
                public void run() {
                    ArrayList<String> fileList = PhotoShareUtils.getFilePathsFromPath(SlotAlbumPage.this.mHost.getGalleryContext(), selectedItems);
                    final ArrayList<String> fileNeedToAdd = PhotoShareUtils.checkMd5ExistsWhenCreateNewShare(fileList);
                    if (fileList.size() > fileNeedToAdd.size()) {
                        PhotoShareUtils.showSameFileTips(fileList.size() - fileNeedToAdd.size());
                    }
                    String[] sharePhotoOrgPaths = new String[fileNeedToAdd.size()];
                    String shareName = SlotAlbumPage.this.mData.getString("newShareName");
                    int createShareResult = 1;
                    try {
                        ShareInfo share = new ShareInfo();
                        share.setShareName(shareName);
                        createShareResult = PhotoShareUtils.getServer().createShare(share, (String[]) fileNeedToAdd.toArray(sharePhotoOrgPaths));
                        GalleryLog.v(SlotAlbumPage.TAG, "createShare result = " + createShareResult);
                    } catch (RemoteException e) {
                        PhotoShareUtils.dealRemoteException(e);
                    } finally {
                        int i = createShareResult;
                        i = createShareResult;
                        SlotAlbumPage.this.mHandler.post(new Runnable() {
                            public void run() {
                                Context activity = SlotAlbumPage.this.mHost.getActivity();
                                if (activity != null) {
                                    SlotAlbumPage.this.dismissProgressDialog();
                                    if (i == 0) {
                                        if (fileNeedToAdd.size() != 0) {
                                            PhotoShareUtils.enableUploadStatusBarNotification(true);
                                            PhotoShareUtils.refreshStatusBar(false);
                                        }
                                        activity.setResult(-1, null);
                                    } else {
                                        ContextedUtils.showToastQuickly(activity, activity.getString(R.string.photoshare_toast_create_folder_fail, new Object[]{activity.getString(R.string.photoshare_toast_fail_common_Toast)}), 0);
                                    }
                                    activity.finish();
                                }
                            }
                        });
                    }
                }
            }.start();
            if (this.mSelectionManager.inSelectionMode()) {
                this.mSelectionManager.leaveSelectionMode();
            }
        }
    }

    protected void onInflateMenu(Menu menu) {
        this.mMenu = this.mIsVideoAlbum ? VIDEO_ALBUM_MENU : COMMON_ALBUM_MENU;
        this.mMenuChangeable = true;
    }

    protected boolean onItemSelected(Action action) {
        String str;
        if (getClass() == MapAlbumPage.class) {
            str = "FromMapSlotView";
        } else {
            str = "FromNormalSlotView";
        }
        ReportToBigData.reportActionForFragment(str, action, this.mSelectionManager);
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
                if (this.mGetContent) {
                    if (this.mNewPhotoShareName != null) {
                        Context context = this.mHost.getActivity();
                        if (PhotoShareUtils.isNetAllowed(context)) {
                            createShareAlbum();
                        } else {
                            new Builder(context).setTitle(R.string.photoshare_allow_title).setMessage(R.string.photoshare_allow_message).setPositiveButton(R.string.photoshare_allow_btn, this.mCreateAlbumNetListener).setNegativeButton(R.string.cancel, this.mCreateAlbumNetListener).show();
                        }
                        return true;
                    } else if (onNewPhotoShareNameEmpty()) {
                        return true;
                    }
                }
                if (this.mSelectionManager.inSelectionMode()) {
                    this.mSelectionManager.leaveSelectionMode();
                }
                return true;
            default:
                return super.onItemSelected(action);
        }
    }

    private boolean onNewPhotoShareNameEmpty() {
        boolean isReturnUrisForMutipick = this.mData.getBoolean("return-uris-for-multipick", false);
        ArrayList<Path> selectedItems = this.mSelectionManager.getSelected(true);
        Intent result = new Intent();
        Bundle bundle = new Bundle();
        DataManager dm = this.mHost.getGalleryContext().getDataManager();
        if (isReturnUrisForMutipick) {
            ArrayList<Uri> selectedItemList = new ArrayList();
            for (Path path : selectedItems) {
                if (DrmUtils.canBeGotContent(dm, path)) {
                    selectedItemList.add(dm.getContentUri(path));
                }
            }
            if (!selectedItemList.isEmpty() || selectedItems.isEmpty()) {
                bundle.putParcelableArrayList("select-item-list", selectedItemList);
            } else {
                ContextedUtils.showToastQuickly(this.mHost.getActivity(), (int) R.string.choose_invalid_drmimage_Toast, 0);
                return true;
            }
        }
        ArrayList<String> selectedItemList2 = new ArrayList();
        for (Path path2 : selectedItems) {
            if (DrmUtils.canBeGotContent(dm, path2)) {
                selectedItemList2.add(path2.toString());
            }
        }
        if (!selectedItemList2.isEmpty() || selectedItems.isEmpty()) {
            bundle.putStringArrayList("select-item-list", selectedItemList2);
            if (this.mData.getBoolean("key-pick-keyguard-photo", false)) {
                bundle.putInt("key-bucket-id", this.mMediaSet.getBucketId());
                bundle.putString("key-album-label", this.mMediaSet.getLabel());
            }
        } else {
            ContextedUtils.showToastQuickly(this.mHost.getActivity(), (int) R.string.choose_invalid_drmimage_Toast, 0);
            return true;
        }
        result.putExtras(bundle);
        this.mHost.getActivity().setResult(-1, result);
        this.mHost.getActivity().finish();
        return false;
    }

    private void initViews() {
        boolean z;
        this.mScrollSelectionManager = new ScrollSelectionManager();
        this.mScrollSelectionManager.setListener(this);
        Config$CommonAlbumFragment config = Config$CommonAlbumFragment.get(this.mHost.getActivity());
        this.mSlotView = new CommonAlbumSlotView(this.mHost.getGalleryContext(), config.slotViewSpec);
        CommonAlbumSlotView commonAlbumSlotView = this.mSlotView;
        if (this.mIsVideoAlbum) {
            z = false;
        } else {
            z = true;
        }
        commonAlbumSlotView.setCommonLayout(z);
        this.mAlbumRender = onCreateSlotRender(config);
        this.mAlbumRender.setModel(this.mAlbumDataAdapter);
        this.mAlbumRender.setGLRoot(this.mHost.getGLRoot());
        this.mSlotView.setSlotRenderer(this.mAlbumRender);
        this.mSlotView.setListener(new SimpleListener() {
            public void onDown(int index) {
                SlotAlbumPage.this.onDown(index);
            }

            public void onUp(boolean followedByLongPress) {
                SlotAlbumPage.this.onUp(followedByLongPress);
            }

            public void onSingleTapUp(int index, boolean cornerPressed) {
                SlotAlbumPage.this.onSingleTapUp(index, cornerPressed);
            }

            public void onLongTap(int index) {
                SlotAlbumPage.this.onLongTap(index);
            }

            public void onScroll(int index) {
                SlotAlbumPage.this.onScroll(index);
            }

            public boolean inSelectionMode() {
                return SlotAlbumPage.this.mSelectionManager.inSelectionMode();
            }

            public void onTouchUp(MotionEvent event) {
                SlotAlbumPage.this.mScrollSelectionManager.clearup();
                if (SlotAlbumPage.this.supportPreviewMode()) {
                    SlotAlbumPage.this.mSlotPreviewPhotoManager.onTouchEvent(event);
                }
            }

            public void onTouchDown(MotionEvent event) {
                if (SlotAlbumPage.this.supportPreviewMode()) {
                    SlotAlbumPage.this.mSlotPreviewPhotoManager.onTouchEvent(event);
                }
            }

            public void onTouchMove(MotionEvent event) {
                if (SlotAlbumPage.this.supportPreviewMode()) {
                    SlotAlbumPage.this.mSlotPreviewPhotoManager.onTouchEvent(event);
                }
            }

            public boolean onDeleteSlotAnimationStart() {
                return SlotAlbumPage.this.onDeleteSlotAnimationStart();
            }
        });
        this.mRootPane.addComponent(this.mSlotView);
        this.mSlotView.setScrollBar(this.mScrollBar);
        this.mRootPane.addComponent(this.mScrollBar);
        this.mTopCover = new PlaceHolderView(this.mHost.getActivity());
        this.mBottomCover = new RectView(getBackgroundColor(this.mHost.getActivity()), true);
        this.mActionBarPlaceHolderView = new ActionBarPlaceHolderView(this);
        this.mRootPane.addComponent(this.mTopCover);
        this.mRootPane.addComponent(this.mActionBarPlaceHolderView);
        this.mRootPane.addComponent(this.mBottomCover);
    }

    protected AbstractCommonAlbumSlotRender onCreateSlotRender(Config$CommonAlbumFragment config) {
        return AbstractCommonAlbumSlotRender.getSlotRender(!this.mIsVideoAlbum, this.mHost.getGalleryContext(), this.mSlotView, this.mSelectionManager, config.placeholderColor);
    }

    private void processPreview(MotionEvent event) {
        int index = this.mSlotView.getSlotIndexByPosition(event.getX(), event.getY());
        if (index >= 0) {
            jumpToPreviewActivity(event, this.mAlbumDataAdapter.get(index));
        }
    }

    public Rect getPreviewImageRect(MotionEvent event) {
        return this.mSlotView.getPreviewImageRect(event.getX(), event.getY());
    }

    public Bitmap getPreviewBitmap(MotionEvent event) {
        int index = this.mSlotView.getSlotIndexByPosition(event.getX(), event.getY());
        if (index < 0) {
            return null;
        }
        return this.mAlbumRender.getContentBitmap(index);
    }

    public boolean isVideo(MotionEvent event) {
        boolean z = false;
        if (event == null) {
            return false;
        }
        int index = this.mSlotView.getSlotIndexByPosition(event.getX(), event.getY());
        if (index < 0) {
            return false;
        }
        MediaItem item = this.mAlbumDataAdapter.get(index);
        if (item != null) {
            z = item.getMimeType().startsWith("video/");
        }
        return z;
    }

    public boolean isScrolling() {
        return this.mSlotView.isScrolling();
    }

    protected boolean supportPreview() {
        return true;
    }

    protected void onScroll(int index) {
        this.mScrollSelectionManager.addScrollIndex(index);
    }

    protected void onLongTap(int slotIndex) {
        if (!this.mSlotPreviewPhotoManager.inPreviewMode() && !this.mGetContent && !this.mSelectionManager.inSelectionMode()) {
            if (this.mSlotPreviewPhotoManager.inPrepareMode()) {
                this.mSlotPreviewPhotoManager.onLongClickPrepare();
                return;
            }
            MediaItem item = this.mAlbumDataAdapter.get(slotIndex);
            if (item != null && !item.isPhotoSharePreView()) {
                this.mSelectionManager.toggle(item.getPath());
                this.mSlotView.invalidate();
                this.mSlotView.startClickSlotAnimation(Integer.valueOf(slotIndex), null);
            }
        }
    }

    protected void onSingleTapUp(int slotIndex, boolean cornerPressed) {
        if (!this.mSlotPreviewPhotoManager.inPreviewMode() && this.mIsActive) {
            int request;
            MediaItem item = this.mAlbumDataAdapter.get(slotIndex);
            if (!this.mSelectionManager.inSelectionMode()) {
                request = 100;
            } else if (item != null) {
                if (cornerPressed && !this.mSelectionManager.inSingleMode()) {
                    this.mSelectionManager.toggle(item.getPath());
                    this.mSlotView.invalidate();
                    return;
                } else if (shouldPickPhotoDirect()) {
                    request = 100;
                } else {
                    request = 102;
                }
            } else {
                return;
            }
            this.mSlotPreviewPhotoManager.leavePreviewMode();
            pickPhotoWithAnimation(this.mSlotView, request, Integer.valueOf(slotIndex), slotIndex, item);
        }
    }

    private boolean shouldPickPhotoDirect() {
        if (!this.mData.getBoolean("fetch-content-for-wallpaper", false) && this.mData.getParcelable("output") == null && this.mData.getString("crop") == null) {
            return false;
        }
        return true;
    }

    protected void onUp(boolean followedByLongPress) {
        this.mAlbumRender.setPressedIndex(-1);
    }

    protected void onDown(int index) {
        this.mAlbumRender.setPressedIndex(index);
    }

    protected void onResume() {
        super.onResume();
        registerSdcardMountBroadcastReceiver(this.mHost.getActivity());
        this.mAlbumRender.resume();
        this.mAlbumRender.setPressedIndex(-1);
        MultiWindowStatusHolder.registerMultiWindowModeChangeListener(this.mMultiWindowModeChangeListener, false);
        this.mSlotPreviewPhotoManager.leavePreviewMode();
        GalleryUtils.updateSupportPressurePreview(this.mHost.getActivity());
    }

    protected boolean onBackPressed() {
        updateCardFilterLocationToAllIfNeed();
        return super.onBackPressed();
    }

    protected void onPause() {
        super.onPause();
        this.mAlbumRender.pause(needFreeSlotContent());
        unregisterSdcardMountBroadcastReceiver(this.mHost.getActivity());
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mActionBar.setListener(null);
        this.mAlbumRender.destroy();
        if (isMergeCardAlbum() && isCardActionMode(this.mActionBar.getCurrentMode())) {
            ((MergeCardActionMode) this.mActionBar.getCurrentMode()).setListener(null);
        }
        cleanupEmptyButton();
        if (this.mCreateAlbumProgressDialog != null) {
            GalleryUtils.setDialogDismissable(this.mCreateAlbumProgressDialog, true);
            GalleryUtils.dismissDialogSafely(this.mCreateAlbumProgressDialog, null);
        }
    }

    private void cleanupEmptyButton() {
        if (this.mEmptyAlbumLayout != null && ((RelativeLayout) this.mHost.getActivity().findViewById(R.id.gallery_root)) != null) {
            this.mEmptyAlbumLayout = null;
        }
    }

    protected boolean setupEmptyButton() {
        RelativeLayout galleryRoot = (RelativeLayout) this.mHost.getActivity().findViewById(R.id.gallery_root);
        if (galleryRoot == null) {
            return false;
        }
        this.mEmptyAlbumLayout = (LinearLayout) ((LayoutInflater) this.mHost.getActivity().getSystemService("layout_inflater")).inflate(R.layout.empty_album, galleryRoot, false);
        updateEmptyLayout(this.mEmptyAlbumLayout);
        galleryRoot.addView(this.mEmptyAlbumLayout);
        return true;
    }

    protected void showEmptyAlbum() {
        this.mIsEmptyAlbum = true;
        if (this.mEmptyAlbumLayout != null || setupEmptyButton()) {
            this.mEmptyAlbumLayout.setVisibility(0);
        }
    }

    protected void hideEmptyAlbum() {
        this.mIsEmptyAlbum = false;
        if (this.mEmptyAlbumLayout != null) {
            this.mEmptyAlbumLayout.setVisibility(8);
        }
    }

    protected void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        updateEmptyLayout(this.mEmptyAlbumLayout);
    }

    protected void updateEmptyLayout(LinearLayout emptyLayout) {
        if (emptyLayout == null) {
            return;
        }
        if (MultiWindowStatusHolder.isInMultiWindowMode()) {
            emptyLayout.setPadding(0, 0, 0, 0);
            emptyLayout.setGravity(17);
            return;
        }
        emptyLayout.setGravity(1);
    }

    protected Rect getAnimSlotRect() {
        return this.mSlotView.getAnimRect();
    }

    protected void onGLRootLayout(int left, int top, int right, int bottom) {
        int paddingRight;
        int relativeNavigationBarHeight;
        this.mAlbumRender.setHighlightItemPath(null);
        int w = right - left;
        int h = bottom - top;
        boolean isPort = LayoutHelper.isPort();
        int navigationBarHeight = LayoutHelper.getNavigationBarHeight();
        int statusBarHeight = LayoutHelper.getStatusBarHeight();
        int paddingTop = statusBarHeight + this.mActionBar.getActionBarHeight();
        if (LayoutHelper.isDefaultLandOrientationProduct()) {
            paddingRight = 0;
            relativeNavigationBarHeight = LayoutHelper.getNavigationBarHeightForDefaultLand();
        } else {
            paddingRight = isPort ? 0 : navigationBarHeight;
            relativeNavigationBarHeight = (!isPort || MultiWindowStatusHolder.isInMultiMaintained()) ? 0 : navigationBarHeight;
        }
        int paddingBottom = relativeNavigationBarHeight;
        this.mSlotView.layout(0, paddingTop, w - paddingRight, h - paddingBottom);
        this.mScrollBar.layout(0, paddingTop, w - paddingRight, h - paddingBottom);
        this.mTopCover.layout(left, 0, right, paddingTop);
        this.mBottomCover.layout(0, h - relativeNavigationBarHeight, w, h);
        this.mActionBarPlaceHolderView.layout(0, statusBarHeight, isPort ? w : w - navigationBarHeight, paddingTop);
    }

    protected void onGLRootRender(GLCanvas canvas) {
        if (this.mResumeEffect != null) {
            if (!this.mResumeEffect.draw(canvas)) {
                this.mResumeEffect = null;
                this.mAlbumRender.setSlotFilter(null);
                onPhotoFallBackFinished();
            }
            this.mRootPane.invalidate();
        }
    }

    protected void onPhotoFallback() {
        this.mAlbumRender.setSlotFilter(this.mResumeEffect);
        this.mResumeEffect.setPositionProvider(this.mFocusIndex, this.mPositionProvider);
        this.mResumeEffect.start();
    }

    protected void onStateResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 100:
                if (data != null) {
                    this.mFocusIndex = data.getIntExtra("return-index-hint", 0);
                    this.mSlotView.makeSlotVisible(this.mFocusIndex);
                    break;
                }
                return;
            case 102:
                if (data != null) {
                    this.mFocusIndex = data.getIntExtra("return-index-hint", 0) + this.mDataLoader.preSize();
                    this.mSlotView.makeSlotVisible(this.mFocusIndex);
                }
                this.mSelectionManager.setSelectionListener(this);
                enterSelectionMode();
                this.mSelectionManager.updateSelectMode(this.mGetContent);
                if (resultCode == -1) {
                    this.mSlotView.invalidate();
                    GalleryLog.v(TAG, "invalidate the slot view");
                    break;
                }
                break;
            case 107:
                if (resultCode == -1) {
                    this.mMenuExecutor.startAction(R.id.action_paste, R.string.paste, null, false, true, Style.PASTE_STYLE, null, data.getExtras());
                    break;
                }
                break;
            case 108:
                if (resultCode == -1 || resultCode == 0) {
                    this.mHost.getActivity().setResult(resultCode, data);
                    this.mHost.getActivity().finish();
                    break;
                }
            default:
                super.onStateResult(requestCode, resultCode, data);
                break;
        }
    }

    protected AlbumDataLoader onCreateDataLoader(MediaSet mediaSet) {
        if (this.mAlbumDataAdapter == null) {
            String str;
            this.mAlbumDataAdapter = new CommonAlbumDataLoader(this.mHost.getGalleryContext(), mediaSet);
            this.mAlbumDataAdapter.setGLRoot(this.mHost.getGLRoot());
            String str2 = "{EnterSlotView:%s}";
            Object[] objArr = new Object[1];
            if (getClass() == MapAlbumPage.class) {
                str = "FromMapView";
            } else {
                str = "FromListView";
            }
            objArr[0] = str;
            ReportToBigData.report(35, String.format(str2, objArr));
        }
        return this.mAlbumDataAdapter;
    }

    protected void leaveSelectionMode() {
        if (!this.mGetContent) {
            this.mActionBar.leaveCurrentMode();
            this.mHost.requestFeature(298);
            this.mRootPane.requestLayout();
        }
    }

    public void onScrollSelect(int index, boolean selected) {
        if (this.mSelectionManager.inSelectionMode() && !this.mSelectionManager.inSingleMode()) {
            MediaItem item = this.mAlbumDataAdapter.get(index);
            if (item != null && !item.isPhotoSharePreView()) {
                Path path = item.getPath();
                if (this.mSelectionManager.isItemSelected(path) != selected) {
                    this.mSelectionManager.toggle(path);
                    this.mSlotView.invalidate();
                }
                ReportToBigData.report(SmsCheckResult.ESCT_176);
            }
        }
    }

    public boolean isSelected(int index) {
        if (!this.mSelectionManager.inSelectionMode()) {
            return false;
        }
        MediaItem item = this.mAlbumDataAdapter.get(index);
        if (item == null) {
            return false;
        }
        return this.mSelectionManager.isItemSelected(item.getPath());
    }

    protected void updateMenu(boolean isSizeZero) {
        ActionBarStateBase actionbarState = getCurrentActionBarState();
        if (actionbarState instanceof ActionMode) {
            ((ActionMode) actionbarState).setTitle(this.mMediaSet.getName());
        }
    }

    protected SlotView getSlotView() {
        return this.mSlotView;
    }

    protected void onLoadingStarted() {
        super.onLoadingStarted();
        this.mAlbumRender.onLoadingStarted();
    }

    protected void onLoadingFinished(boolean loadingFailed) {
        super.onLoadingFinished(loadingFailed);
        this.mAlbumRender.onLoadingFinished();
    }

    private void showProgressDialog(String message) {
        this.mCreateAlbumProgressDialog = new ProgressDialog(this.mHost.getActivity());
        this.mCreateAlbumProgressDialog.setCancelable(false);
        this.mCreateAlbumProgressDialog.setMessage(message);
        this.mCreateAlbumProgressDialog.show();
    }

    private void dismissProgressDialog() {
        if (this.mCreateAlbumProgressDialog != null && this.mCreateAlbumProgressDialog.isShowing()) {
            this.mCreateAlbumProgressDialog.dismiss();
            this.mCreateAlbumProgressDialog = null;
        }
    }

    private boolean supportPreviewMode() {
        if (!GalleryUtils.isSupportPressurePreview(this.mHost.getActivity()) || this.mSelectionManager.inSelectionMode() || this.mSlotView.isScrolling() || !supportPreview()) {
            return false;
        }
        return true;
    }

    protected boolean autoFinishWhenNoItems() {
        if (isMergeCardAlbum()) {
            return false;
        }
        return super.autoFinishWhenNoItems();
    }

    protected SlotRenderInterface getSlotRenderInterface() {
        return this.mAlbumRender;
    }

    public void onCardLocationFiltered(int locationType) {
        this.mCardLocation = locationType;
        this.mAlbumDataAdapter.filterCameraLocation(locationType);
    }

    public void onCardActionShowed(ActionBarStateBase mode) {
        if (mode instanceof MergeCardActionMode) {
            ((MergeCardActionMode) mode).updateLocationSelection(this.mCardLocation);
        }
    }

    public void onCreateModeCompleted(ActionBarStateBase mode) {
        if (isMergeCardAlbum() && (mode instanceof MergeCardActionMode)) {
            ((MergeCardActionMode) mode).setListener(this);
        }
    }

    private boolean isMergeCardAlbum() {
        return this.mMediaSet instanceof LocalMergeCardAlbum;
    }

    private boolean isCardActionMode(ActionBarStateBase mode) {
        return mode != null && mode.getMode() == 4;
    }

    private void registerSdcardMountBroadcastReceiver(Context context) {
        if (isMergeCardAlbum()) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.MEDIA_MOUNTED");
            filter.addAction("android.intent.action.MEDIA_UNMOUNTED");
            filter.addDataScheme("file");
            context.registerReceiver(this.mSdCardBrocardcastReceiver, filter);
        }
    }

    private void unregisterSdcardMountBroadcastReceiver(Context context) {
        if (isMergeCardAlbum()) {
            context.unregisterReceiver(this.mSdCardBrocardcastReceiver);
        }
    }

    private void updateCardFilterLocationToAllIfNeed() {
        if (isMergeCardAlbum()) {
            ActionBarStateBase mode = this.mActionBar.getCurrentMode();
            if (mode != null && mode.getMode() == 4 && this.mCardLocation != 0) {
                onCardLocationFiltered(0);
            }
        }
    }

    public void onBlurWallpaperChanged() {
        this.mTopCover.textureDirty();
    }

    protected void onClickSlotAnimationStart() {
        copyActionBarToGL(this.mActionBarPlaceHolderView);
    }

    protected void onClearOpenAnimation() {
        this.mActionBarPlaceHolderView.setContent(null);
    }

    public void onFirstRenderAfterUpdateActionBarPlaceHolderContent() {
        GLHost glHost = this.mHost;
        if (needSplitActionBarHide() && glHost != null) {
            Activity activity = glHost.getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        GalleryActionBar actionBar = SlotAlbumPage.this.mActionBar;
                        if (actionBar != null) {
                            actionBar.hideHeadActionContainer();
                        }
                    }
                });
            }
        }
    }

    public void onNavigationBarChanged(boolean show, int height) {
        super.onNavigationBarChanged(show, height);
        updateEmptyLayout(this.mEmptyAlbumLayout);
    }
}
