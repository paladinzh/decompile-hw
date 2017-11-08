package com.huawei.gallery.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.gallery3d.R;
import com.android.gallery3d.app.Config$CommonAlbumFragment;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.Keyguard;
import com.android.gallery3d.data.KeyguardItem;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.MenuExecutor.SimpleSlotDeleteProgressListener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MultiWindowStatusHolder;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionBarStateBase;
import com.huawei.gallery.actionbar.ActionMode;
import com.huawei.gallery.actionbar.SelectionMode;
import com.huawei.gallery.actionbar.StandardTitleActionMode;
import com.huawei.gallery.anim.PhotoFallbackEffect.PositionProvider;
import com.huawei.gallery.ui.AbstractCommonAlbumSlotRender;
import com.huawei.gallery.ui.CommonAlbumSlotView;
import com.huawei.gallery.ui.CommonAlbumSlotView.SimpleListener;
import com.huawei.gallery.ui.PlaceHolderView;
import com.huawei.gallery.ui.RectView;
import com.huawei.gallery.ui.ScrollSelectionManager;
import com.huawei.gallery.ui.ScrollSelectionManager.Listener;
import com.huawei.gallery.ui.SlotView;
import com.huawei.gallery.ui.SlotView.AbsLayout;
import com.huawei.gallery.ui.SlotView.SlotRenderInterface;
import com.huawei.gallery.util.LayoutHelper;
import com.huawei.keyguard.hiad.HiAdInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class KeyguardPage extends AbsAlbumPage implements Listener {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    private static final Action[] DEFAULT_MENU = new Action[]{Action.ADD, Action.WITH_UPDATE, Action.WITHOUT_UPDATE};
    private static final Action[] SELECTION_MENU = new Action[]{Action.REMOVE, Action.ALL};
    private CommonAlbumDataLoader mAlbumAdapter = null;
    private RectView mBottomCover;
    private KeyguardBroadcastReceiver mBroadcastReceiver = new KeyguardBroadcastReceiver();
    private LinearLayout mEmptyAlbumLayout;
    private KeyguardHandler mHandler = new KeyguardHandler();
    private boolean mIsCustomAlbum = false;
    private boolean mIsDownLoadAlbum = false;
    private boolean mIsEmptyAlbum = true;
    private boolean mIsUpdateAvailable = false;
    private PositionProvider mPositionProvider = new PositionProvider() {
        public Rect getPosition(int index) {
            Rect keyguradRect = KeyguardPage.this.mSlotView.getSlotRect(index);
            Rect bounds = KeyguardPage.this.mSlotView.bounds();
            keyguradRect.offset(bounds.left - KeyguardPage.this.mSlotView.getScrollX(), bounds.top - KeyguardPage.this.mSlotView.getScrollY());
            return keyguradRect;
        }
    };
    private ProgressDialog mProgressDialog;
    private ScrollSelectionManager mScrollSelectionManager;
    private AbstractCommonAlbumSlotRender mSlotRenderer;
    private CommonAlbumSlotView mSlotView;
    private PlaceHolderView mTopCover;

    public final class KeyguardBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if ("com.android.keyguard.magazinulock.update.CHECK_FINISHED_GALLERY".equalsIgnoreCase(intent.getAction())) {
                boolean isServerResAvaiabile = intent.getBooleanExtra("updateFlag", true);
                KeyguardPage.this.clearAllErrorMsg();
                if (isServerResAvaiabile) {
                    KeyguardPage.this.mHandler.sendMessage(KeyguardPage.this.mHandler.obtainMessage(2, intent.getParcelableExtra("update_ad_list")));
                    return;
                }
                KeyguardPage.this.mActionBar.getCurrentMode().changeAction(Action.WITH_UPDATE.id, Action.WITHOUT_UPDATE.id);
                if (KeyguardPage.this.mIsUpdateAvailable) {
                    KeyguardPage.this.mHandler.sendEmptyMessage(3);
                    KeyguardPage.this.mIsUpdateAvailable = false;
                    return;
                }
                KeyguardPage.this.mHandler.sendEmptyMessage(1);
            }
        }
    }

    public class KeyguardHandler extends Handler {
        private int resId;

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    this.resId = R.string.keyguard_network_fail;
                    break;
                case 1:
                    this.resId = R.string.keyguard_magazine_no_update_description;
                    break;
                case 2:
                    KeyguardPage.this.mIsUpdateAvailable = false;
                    KeyguardPage.this.mActionBar.getCurrentMode().changeAction(Action.WITH_UPDATE.id, Action.WITHOUT_UPDATE.id);
                    KeyguardPage.this.dismissProgressDialog();
                    Intent intentPreview = new Intent();
                    intentPreview.setComponent(new ComponentName("com.android.keyguard", "com.huawei.keyguard.magazine.preview.UpdatePreviewActivity"));
                    Object adInfo = msg.obj;
                    if (adInfo instanceof HiAdInfo) {
                        intentPreview.putExtra("update_ad_list", (HiAdInfo) adInfo);
                    }
                    GalleryUtils.startActivityForResultCatchSecurityEx(KeyguardPage.this.mHost.getActivity(), intentPreview, SmsCheckResult.ESCT_302);
                    this.resId = -1;
                    break;
                case 3:
                    Keyguard.setCheckedNewVersion(KeyguardPage.this.mHost.getActivity().getContentResolver(), false);
                    if (KeyguardPage.this.mIsDownLoadAlbum) {
                        ActionMode am = KeyguardPage.this.mActionBar.enterActionMode(false);
                        am.setMenu(1, KeyguardPage.DEFAULT_MENU[2]);
                        am.show();
                    }
                    this.resId = R.string.keyguard_server_fail;
                    break;
            }
            postDelayed(new Runnable() {
                public void run() {
                    KeyguardPage.this.finishProgressDialog(KeyguardHandler.this.resId);
                }
            }, 1000);
        }
    }

    private class RemoveProgressListener extends SimpleSlotDeleteProgressListener {
        private DataManager dataManager;
        private int imageType;
        private AbsLayout layout;
        private HashMap<Object, Object> visibleIndexMap;
        private HashMap<Path, Object> visiblePathMap;

        private RemoveProgressListener() {
            this.dataManager = KeyguardPage.this.mHost.getGalleryContext().getDataManager();
            this.imageType = 0;
        }

        public void onProgressExecuteSuccess(String path) {
            if (this.imageType != 3) {
                MediaObject mediaObj = this.dataManager.getMediaObject(path);
                if (mediaObj instanceof KeyguardItem) {
                    this.imageType |= 1 << ((KeyguardItem) mediaObj).type;
                }
            }
        }

        public void onProgressComplete(int result) {
            String removeType;
            switch (this.imageType) {
                case 1:
                    removeType = "UnlockMagazine";
                    break;
                case 2:
                    removeType = "Local";
                    break;
                case 3:
                    removeType = "AllType";
                    break;
                default:
                    return;
            }
            ReportToBigData.report(48, String.format("{RemoveMagazine:%s}", new Object[]{removeType}));
            KeyguardPage.this.onDeleteProgressComplete(this.visiblePathMap, this.visibleIndexMap, this.layout);
        }

        public void onDeleteStart() {
            KeyguardPage.this.mDataLoader.freeze();
            SlotView slotView = KeyguardPage.this.getSlotView();
            this.visiblePathMap = new HashMap();
            this.visibleIndexMap = new HashMap();
            slotView.prepareVisibleRangeItemIndex(this.visiblePathMap, this.visibleIndexMap);
            this.layout = slotView.cloneLayout();
        }
    }

    private static /* synthetic */ int[] -getcom-huawei-gallery-actionbar-ActionSwitchesValues() {
        if (-com-huawei-gallery-actionbar-ActionSwitchesValues != null) {
            return -com-huawei-gallery-actionbar-ActionSwitchesValues;
        }
        int[] iArr = new int[Action.values().length];
        try {
            iArr[Action.ADD.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Action.ADD_ALBUM.ordinal()] = 5;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Action.ADD_COMMENT.ordinal()] = 6;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Action.AIRSHARE.ordinal()] = 7;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Action.ALBUM.ordinal()] = 8;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Action.ALL.ordinal()] = 9;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Action.BACK.ordinal()] = 10;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Action.CANCEL_DETAIL.ordinal()] = 11;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Action.COLLAGE.ordinal()] = 12;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Action.COMMENT.ordinal()] = 13;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Action.COPY.ordinal()] = 14;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Action.DEALL.ordinal()] = 15;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Action.DEL.ordinal()] = 16;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Action.DETAIL.ordinal()] = 17;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[Action.DYNAMIC_ALBUM.ordinal()] = 18;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[Action.EDIT.ordinal()] = 19;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[Action.EDIT_COMMENT.ordinal()] = 20;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[Action.GOTO_GALLERY.ordinal()] = 21;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[Action.HIDE.ordinal()] = 22;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[Action.INFO.ordinal()] = 23;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[Action.KEYGUARD_LIKE.ordinal()] = 24;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[Action.KEYGUARD_NOT_LIKE.ordinal()] = 25;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[Action.LOOPPLAY.ordinal()] = 26;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[Action.MAP.ordinal()] = 27;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[Action.MENU.ordinal()] = 28;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[Action.MORE_EDIT.ordinal()] = 29;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[Action.MOVE.ordinal()] = 30;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[Action.MOVEIN.ordinal()] = 31;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[Action.MOVEOUT.ordinal()] = 32;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[Action.MULTISCREEN.ordinal()] = 33;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[Action.MULTISCREEN_ACTIVITED.ordinal()] = 34;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[Action.MULTI_SELECTION.ordinal()] = 35;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[Action.MULTI_SELECTION_ON.ordinal()] = 36;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[Action.MYFAVORITE.ordinal()] = 37;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[Action.NO.ordinal()] = 38;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[Action.NONE.ordinal()] = 39;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[Action.NOT_MYFAVORITE.ordinal()] = 40;
        } catch (NoSuchFieldError e37) {
        }
        try {
            iArr[Action.OK.ordinal()] = 41;
        } catch (NoSuchFieldError e38) {
        }
        try {
            iArr[Action.PHOTOSHARE_ACCOUNT.ordinal()] = 42;
        } catch (NoSuchFieldError e39) {
        }
        try {
            iArr[Action.PHOTOSHARE_ADDPICTURE.ordinal()] = 43;
        } catch (NoSuchFieldError e40) {
        }
        try {
            iArr[Action.PHOTOSHARE_BACKUP.ordinal()] = 44;
        } catch (NoSuchFieldError e41) {
        }
        try {
            iArr[Action.PHOTOSHARE_CANCEL_RECEIVE.ordinal()] = 45;
        } catch (NoSuchFieldError e42) {
        }
        try {
            iArr[Action.PHOTOSHARE_CLEAR.ordinal()] = 46;
        } catch (NoSuchFieldError e43) {
        }
        try {
            iArr[Action.PHOTOSHARE_COMBINE.ordinal()] = 47;
        } catch (NoSuchFieldError e44) {
        }
        try {
            iArr[Action.PHOTOSHARE_CONTACT.ordinal()] = 48;
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
            iArr[Action.PHOTOSHARE_DELETE.ordinal()] = 51;
        } catch (NoSuchFieldError e48) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD.ordinal()] = 52;
        } catch (NoSuchFieldError e49) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOADING.ordinal()] = 53;
        } catch (NoSuchFieldError e50) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD_START.ordinal()] = 54;
        } catch (NoSuchFieldError e51) {
        }
        try {
            iArr[Action.PHOTOSHARE_EDITSHARE.ordinal()] = 55;
        } catch (NoSuchFieldError e52) {
        }
        try {
            iArr[Action.PHOTOSHARE_EMAIL.ordinal()] = 56;
        } catch (NoSuchFieldError e53) {
        }
        try {
            iArr[Action.PHOTOSHARE_LINK.ordinal()] = 57;
        } catch (NoSuchFieldError e54) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_DOWNLOAD.ordinal()] = 58;
        } catch (NoSuchFieldError e55) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_UPLOAD.ordinal()] = 59;
        } catch (NoSuchFieldError e56) {
        }
        try {
            iArr[Action.PHOTOSHARE_MESSAGE.ordinal()] = 60;
        } catch (NoSuchFieldError e57) {
        }
        try {
            iArr[Action.PHOTOSHARE_MOVE.ordinal()] = 61;
        } catch (NoSuchFieldError e58) {
        }
        try {
            iArr[Action.PHOTOSHARE_MULTI_DOWNLOAD.ordinal()] = 62;
        } catch (NoSuchFieldError e59) {
        }
        try {
            iArr[Action.PHOTOSHARE_NOT_THIS_PERSON.ordinal()] = 63;
        } catch (NoSuchFieldError e60) {
        }
        try {
            iArr[Action.PHOTOSHARE_PAUSE.ordinal()] = 64;
        } catch (NoSuchFieldError e61) {
        }
        try {
            iArr[Action.PHOTOSHARE_REMOVE_PEOPLE_TAG.ordinal()] = 65;
        } catch (NoSuchFieldError e62) {
        }
        try {
            iArr[Action.PHOTOSHARE_RENAME.ordinal()] = 66;
        } catch (NoSuchFieldError e63) {
        }
        try {
            iArr[Action.PHOTOSHARE_SETTINGS.ordinal()] = 67;
        } catch (NoSuchFieldError e64) {
        }
        try {
            iArr[Action.PHOTOSHARE_UPLOAD_START.ordinal()] = 68;
        } catch (NoSuchFieldError e65) {
        }
        try {
            iArr[Action.PRINT.ordinal()] = 69;
        } catch (NoSuchFieldError e66) {
        }
        try {
            iArr[Action.RANGE_MEASURE.ordinal()] = 70;
        } catch (NoSuchFieldError e67) {
        }
        try {
            iArr[Action.RECYCLE_CLEAN_BIN.ordinal()] = 71;
        } catch (NoSuchFieldError e68) {
        }
        try {
            iArr[Action.RECYCLE_DELETE.ordinal()] = 72;
        } catch (NoSuchFieldError e69) {
        }
        try {
            iArr[Action.RECYCLE_RECOVERY.ordinal()] = 73;
        } catch (NoSuchFieldError e70) {
        }
        try {
            iArr[Action.REDO.ordinal()] = 74;
        } catch (NoSuchFieldError e71) {
        }
        try {
            iArr[Action.REMOVE.ordinal()] = 2;
        } catch (NoSuchFieldError e72) {
        }
        try {
            iArr[Action.RENAME.ordinal()] = 75;
        } catch (NoSuchFieldError e73) {
        }
        try {
            iArr[Action.RE_SEARCH.ordinal()] = 76;
        } catch (NoSuchFieldError e74) {
        }
        try {
            iArr[Action.ROTATE_LEFT.ordinal()] = 77;
        } catch (NoSuchFieldError e75) {
        }
        try {
            iArr[Action.ROTATE_RIGHT.ordinal()] = 78;
        } catch (NoSuchFieldError e76) {
        }
        try {
            iArr[Action.SAVE.ordinal()] = 79;
        } catch (NoSuchFieldError e77) {
        }
        try {
            iArr[Action.SAVE_BURST.ordinal()] = 80;
        } catch (NoSuchFieldError e78) {
        }
        try {
            iArr[Action.SEE_BARCODE_INFO.ordinal()] = 81;
        } catch (NoSuchFieldError e79) {
        }
        try {
            iArr[Action.SETAS.ordinal()] = 82;
        } catch (NoSuchFieldError e80) {
        }
        try {
            iArr[Action.SETAS_BOTH.ordinal()] = 83;
        } catch (NoSuchFieldError e81) {
        }
        try {
            iArr[Action.SETAS_FIXED.ordinal()] = 84;
        } catch (NoSuchFieldError e82) {
        }
        try {
            iArr[Action.SETAS_FIXED_ACTIVED.ordinal()] = 85;
        } catch (NoSuchFieldError e83) {
        }
        try {
            iArr[Action.SETAS_HOME.ordinal()] = 86;
        } catch (NoSuchFieldError e84) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE.ordinal()] = 87;
        } catch (NoSuchFieldError e85) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE_ACTIVED.ordinal()] = 88;
        } catch (NoSuchFieldError e86) {
        }
        try {
            iArr[Action.SETAS_UNLOCK.ordinal()] = 89;
        } catch (NoSuchFieldError e87) {
        }
        try {
            iArr[Action.SETTINGS.ordinal()] = 90;
        } catch (NoSuchFieldError e88) {
        }
        try {
            iArr[Action.SHARE.ordinal()] = 91;
        } catch (NoSuchFieldError e89) {
        }
        try {
            iArr[Action.SHOW_ON_MAP.ordinal()] = 92;
        } catch (NoSuchFieldError e90) {
        }
        try {
            iArr[Action.SINGLE_SELECTION.ordinal()] = 93;
        } catch (NoSuchFieldError e91) {
        }
        try {
            iArr[Action.SINGLE_SELECTION_ON.ordinal()] = 94;
        } catch (NoSuchFieldError e92) {
        }
        try {
            iArr[Action.SLIDESHOW.ordinal()] = 95;
        } catch (NoSuchFieldError e93) {
        }
        try {
            iArr[Action.STORY_ALBUM_REMOVE.ordinal()] = 96;
        } catch (NoSuchFieldError e94) {
        }
        try {
            iArr[Action.STORY_ITEM_REMOVE.ordinal()] = 97;
        } catch (NoSuchFieldError e95) {
        }
        try {
            iArr[Action.STORY_RENAME.ordinal()] = 98;
        } catch (NoSuchFieldError e96) {
        }
        try {
            iArr[Action.TIME.ordinal()] = 99;
        } catch (NoSuchFieldError e97) {
        }
        try {
            iArr[Action.TOGIF.ordinal()] = 100;
        } catch (NoSuchFieldError e98) {
        }
        try {
            iArr[Action.UNDO.ordinal()] = 101;
        } catch (NoSuchFieldError e99) {
        }
        try {
            iArr[Action.WITHOUT_UPDATE.ordinal()] = 3;
        } catch (NoSuchFieldError e100) {
        }
        try {
            iArr[Action.WITH_UPDATE.ordinal()] = 4;
        } catch (NoSuchFieldError e101) {
        }
        -com-huawei-gallery-actionbar-ActionSwitchesValues = iArr;
        return iArr;
    }

    protected void onGLRootLayout(int left, int top, int right, int bottom) {
        boolean isPort = LayoutHelper.isPort();
        int statusBarHeight = LayoutHelper.getStatusBarHeight();
        int actionBarHeight = this.mActionBar.getActionBarHeight();
        int navigationBarHeight = LayoutHelper.getNavigationBarHeight();
        int paddingTop = statusBarHeight + actionBarHeight;
        int paddingRight = isPort ? 0 : navigationBarHeight;
        int currentFootBarHeight = this.mActionBar.getCurrentFoorBarHeight(this.mHost.getActivity(), true);
        int relativeNavigationBarHeight = isPort ? navigationBarHeight : 0;
        this.mSlotView.layout(left, top + paddingTop, right - paddingRight, bottom - (currentFootBarHeight + relativeNavigationBarHeight));
        this.mTopCover.layout(left, 0, right, paddingTop);
        RectView rectView = this.mBottomCover;
        if (currentFootBarHeight == 0) {
            relativeNavigationBarHeight = 0;
        }
        rectView.layout(left, bottom - relativeNavigationBarHeight, right, bottom);
    }

    protected void onGLRootRender(GLCanvas canvas) {
        if (this.mResumeEffect != null) {
            if (!this.mResumeEffect.draw(canvas)) {
                this.mResumeEffect = null;
                this.mSlotRenderer.setSlotFilter(null);
                onPhotoFallBackFinished();
            }
            this.mRootPane.invalidate();
        }
    }

    protected void onCreate(Bundle data, Bundle storedState) {
        super.onCreate(data, storedState);
        this.mIsCustomAlbum = "/keyguard/custom".equals(data.getString("media-path"));
        this.mIsDownLoadAlbum = "/keyguard/download".equals(data.getString("media-path"));
        this.mIsUpdateAvailable = data.getBoolean("updateAvailable", false);
        initViews();
    }

    protected boolean onCreateActionBar(Menu menu) {
        this.mHost.requestFeature(296);
        if (this.mSelectionManager.inSelectionMode()) {
            return true;
        }
        onInflateMenu(menu);
        this.mActionBar.enterActionMode(false);
        StandardTitleActionMode am = this.mActionBar.enterStandardTitleActionMode(false);
        am.setTitle(this.mMediaSet.getName());
        am.setBothAction(Action.NONE, Action.NONE);
        am.show();
        return true;
    }

    protected void onInflateMenu(Menu menu) {
        ActionMode am;
        if (this.mIsCustomAlbum) {
            am = this.mActionBar.enterActionMode(false);
            am.setTitle(this.mMediaSet.getName());
            am.setBothAction(Action.NONE, Action.NONE);
            am.setMenu(1, DEFAULT_MENU[0]);
            am.show();
        } else if (this.mIsDownLoadAlbum) {
            am = this.mActionBar.enterActionMode(false);
            am.setTitle(this.mMediaSet.getName());
            am.setBothAction(Action.NONE, Action.NONE);
            if (this.mIsUpdateAvailable) {
                am.setMenu(1, DEFAULT_MENU[1]);
            } else {
                am.setMenu(1, DEFAULT_MENU[2]);
            }
            am.show();
        } else {
            Log.i("keyguard", "invalid phototype");
        }
    }

    private void initViews() {
        this.mScrollSelectionManager = new ScrollSelectionManager();
        this.mScrollSelectionManager.setListener(this);
        Config$CommonAlbumFragment config = Config$CommonAlbumFragment.get(this.mHost.getActivity());
        this.mSlotView = new CommonAlbumSlotView(this.mHost.getGalleryContext(), config.slotViewSpec);
        this.mSlotRenderer = AbstractCommonAlbumSlotRender.getSlotRender(true, this.mHost.getGalleryContext(), this.mSlotView, this.mSelectionManager, config.placeholderColor);
        this.mSlotRenderer.setModel(this.mAlbumAdapter);
        this.mSlotRenderer.setGLRoot(this.mHost.getGLRoot());
        this.mSlotView.setSlotRenderer(this.mSlotRenderer);
        this.mSlotView.setScrollBar(this.mScrollBar);
        this.mRootPane.addComponent(this.mSlotView);
        this.mRootPane.addComponent(this.mScrollBar);
        this.mTopCover = new PlaceHolderView(this.mHost.getActivity());
        this.mBottomCover = new RectView(getBackgroundColor(this.mHost.getActivity()), true);
        this.mRootPane.addComponent(this.mTopCover);
        this.mRootPane.addComponent(this.mBottomCover);
        this.mSlotView.setSlotRenderer(this.mSlotRenderer);
        this.mSlotView.setListener(new SimpleListener() {
            public void onDown(int index) {
                KeyguardPage.this.onDown(index);
            }

            public void onUp(boolean followedByLongPress) {
                KeyguardPage.this.onUp(followedByLongPress);
            }

            public void onSingleTapUp(int index, boolean cornerPressed) {
                KeyguardPage.this.onSingleTapUp(index, cornerPressed);
            }

            public boolean onDeleteSlotAnimationStart() {
                return KeyguardPage.this.onDeleteSlotAnimationStart();
            }

            public void onLongTap(int index) {
                KeyguardPage.this.onLongTap(index);
            }

            public void onScroll(int index) {
                KeyguardPage.this.onScroll(index);
            }

            public boolean inSelectionMode() {
                return KeyguardPage.this.mSelectionManager.inSelectionMode();
            }

            public void onTouchDown(MotionEvent event) {
                if (KeyguardPage.this.isSupportPreviewMode()) {
                    KeyguardPage.this.mSlotPreviewPhotoManager.onTouchEvent(event);
                }
            }

            public void onTouchUp(MotionEvent event) {
                KeyguardPage.this.mScrollSelectionManager.clearup();
                if (KeyguardPage.this.isSupportPreviewMode()) {
                    KeyguardPage.this.mSlotPreviewPhotoManager.onTouchEvent(event);
                }
            }

            public void onTouchMove(MotionEvent event) {
                if (KeyguardPage.this.isSupportPreviewMode()) {
                    KeyguardPage.this.mSlotPreviewPhotoManager.onTouchEvent(event);
                }
            }
        });
    }

    public void onDown(int index) {
        this.mSlotRenderer.setPressedIndex(index);
    }

    public void onUp(boolean followedByLongPress) {
        this.mSlotRenderer.setPressedIndex(-1);
    }

    public void onSingleTapUp(int index, boolean cornerPressed) {
        if (this.mIsActive) {
            MediaItem item = this.mAlbumAdapter.get(index);
            if (!this.mSelectionManager.inSelectionMode()) {
                pickPhotoWithAnimation(this.mSlotView, 104, Integer.valueOf(index), index, item);
            } else if (item != null) {
                this.mSelectionManager.toggle(item.getPath());
                this.mSlotView.invalidate();
            }
        }
    }

    public void onLongTap(int index) {
        if (!this.mSelectionManager.inSelectionMode()) {
            MediaItem item = this.mAlbumAdapter.get(index);
            if (item != null) {
                this.mSelectionManager.toggle(item.getPath());
                this.mSlotView.startClickSlotAnimation(Integer.valueOf(index), null);
                this.mSlotView.invalidate();
            }
        }
    }

    protected void onScroll(int index) {
        this.mScrollSelectionManager.addScrollIndex(index);
    }

    private boolean isSupportPreviewMode() {
        if (!GalleryUtils.isSupportPressurePreview(this.mHost.getActivity()) || this.mSelectionManager.inSelectionMode() || this.mSlotView.isScrolling()) {
            return false;
        }
        return true;
    }

    protected void onResume() {
        super.onResume();
        this.mSlotRenderer.resume();
        this.mSlotRenderer.setPressedIndex(-1);
        LayoutHelper.getNavigationBarHandler().update();
        this.mHost.getActivity().registerReceiver(this.mBroadcastReceiver, new IntentFilter("com.android.keyguard.magazinulock.update.CHECK_FINISHED_GALLERY"), "com.huawei.gallery.permission.ACCESS_PHOTO_MANAGER", null);
    }

    protected boolean onItemSelected(Action action) {
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
                startAddPictureActivity();
                return true;
            case 2:
                onRemove();
                return true;
            case 3:
            case 4:
                onDownload();
                return true;
            default:
                return super.onItemSelected(action);
        }
    }

    private void startAddPictureActivity() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        intent.setClass(this.mHost.getActivity(), ListAlbumPickerActivity.class);
        intent.putExtra("key-pick-keyguard-photo", true);
        intent.putExtra("support-multipick-items", true);
        intent.putExtra("max-select-count", 500);
        this.mHost.getActivity().startActivityForResult(intent, SmsCheckResult.ESCT_301);
    }

    private void onRemove() {
        String confirmTitle;
        int selectedCount = this.mSelectionManager.getSelectedCount();
        int totalCount = this.mSelectionManager.getTotalCount();
        Activity activity = this.mHost.getActivity();
        if (selectedCount != totalCount || totalCount <= 1) {
            confirmTitle = activity.getResources().getQuantityString(R.plurals.remove_selection_title, selectedCount, new Object[]{Integer.valueOf(selectedCount)});
        } else {
            confirmTitle = activity.getResources().getString(R.string.remove_all_files);
        }
        this.mMenuExecutor.onMenuClicked(Action.REMOVE, null, confirmTitle, new RemoveProgressListener());
    }

    protected void onPause() {
        super.onPause();
        this.mHost.getActivity().unregisterReceiver(this.mBroadcastReceiver);
        this.mSlotRenderer.pause(needFreeSlotContent());
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mSlotRenderer.destroy();
        dismissProgressDialog();
        cleanupEmptyButton();
        this.mSlotRenderer.setGLRoot(null);
        this.mAlbumAdapter.setGLRoot(null);
    }

    protected void onStateResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 104:
                if (data != null) {
                    this.mFocusIndex = data.getIntExtra("return-index-hint", 0);
                    this.mSlotView.invalidate();
                    break;
                }
                return;
            case SmsCheckResult.ESCT_301 /*301*/:
                if (resultCode == -1) {
                    final ArrayList<String> result = data.getStringArrayListExtra("select-item-list");
                    final int bucketId = data.getIntExtra("key-bucket-id", -1);
                    String albumLabel = data.getStringExtra("key-album-label");
                    new Thread(new Runnable() {
                        public void run() {
                            GalleryLog.v("KeyguardAlbumPage", "execute result : " + KeyguardPage.this.executeAddOperation(result, bucketId));
                        }
                    }).start();
                    ReportToBigData.reportChooseSourceForKeyguardMagazine(albumLabel, bucketId, this.mHost.getActivity());
                    break;
                }
                break;
            case SmsCheckResult.ESCT_302 /*302*/:
                if (resultCode == SmsCheckResult.ESCT_302) {
                    if (!this.mIsUpdateAvailable) {
                        this.mHandler.sendEmptyMessage(1);
                        break;
                    }
                    this.mHandler.sendEmptyMessage(3);
                    this.mIsUpdateAvailable = false;
                    break;
                }
                break;
            default:
                super.onStateResult(requestCode, resultCode, data);
                break;
        }
    }

    private boolean executeAddOperation(ArrayList<String> result, int bucketId) {
        if (result == null || result.isEmpty()) {
            return false;
        }
        ContentValues cv = new ContentValues();
        DataManager dm = this.mHost.getGalleryContext().getDataManager();
        ArrayList<ContentProviderOperation> operations = new ArrayList();
        for (String path : result) {
            MediaItem item = (MediaItem) dm.getMediaObject(Path.fromString(path));
            if (item == null) {
                GalleryLog.e("KeyguardAlbumPage", "error item");
                return false;
            }
            int correctBucketId;
            if (bucketId == 0) {
                correctBucketId = GalleryUtils.getBucketId(new File(item.getFilePath()).getParent());
            } else {
                correctBucketId = bucketId;
            }
            cv.clear();
            cv.put("path", item.getFilePath());
            cv.put("name", item.getName());
            cv.put("date_modified", Long.valueOf(item.getDateModifiedInSec()));
            cv.put("bucket_id", Integer.valueOf(correctBucketId));
            cv.put("isHidden", Integer.valueOf(0));
            cv.put("isNew", Integer.valueOf(0));
            cv.put("isCustom", Integer.valueOf(1));
            cv.put("width", Integer.valueOf(item.getWidth()));
            cv.put("height", Integer.valueOf(item.getHeight()));
            operations.add(ContentProviderOperation.newInsert(Keyguard.URI).withValues(cv).build());
        }
        try {
            this.mHost.getActivity().getContentResolver().applyBatch(Keyguard.URI.getAuthority(), operations);
            return true;
        } catch (RemoteException e) {
            GalleryLog.e("KeyguardAlbumPage", "execute error" + e.getMessage());
            return false;
        } catch (OperationApplicationException e2) {
            GalleryLog.e("KeyguardAlbumPage", "execute error" + e2.getMessage());
            return false;
        }
    }

    protected AlbumDataLoader onCreateDataLoader(MediaSet mediaSet) {
        if (this.mAlbumAdapter == null) {
            this.mAlbumAdapter = new CommonAlbumDataLoader(this.mHost.getGalleryContext(), mediaSet);
            this.mAlbumAdapter.setGLRoot(this.mHost.getGLRoot());
        }
        return this.mAlbumAdapter;
    }

    protected void showEmptyAlbum() {
        this.mIsEmptyAlbum = true;
        if (this.mEmptyAlbumLayout != null || setupEmptyButton()) {
            this.mEmptyAlbumLayout.setVisibility(0);
            GalleryLog.d("KeyguardAlbumPage", "Keygurad Album is empty");
        }
    }

    protected void hideEmptyAlbum() {
        this.mIsEmptyAlbum = false;
        if (this.mEmptyAlbumLayout != null) {
            this.mEmptyAlbumLayout.setVisibility(8);
            GalleryLog.d("KeyguardAlbumPage", "picture added,Keygurad Album is not empty");
        }
    }

    protected void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        updateEmptyLayout(this.mEmptyAlbumLayout);
        GalleryLog.d("KeyguardAlbumPage", "keyguard page configuration changed");
    }

    protected Rect getAnimSlotRect() {
        return this.mSlotView.getAnimRect();
    }

    public void onSelectionModeChange(int mode) {
        switch (mode) {
            case 1:
                GalleryLog.printDFXLog("ENTER_SELECTION_MODE");
                enterSelectionMode();
                return;
            case 2:
                GalleryLog.printDFXLog("LEAVE_SELECTION_MODE");
                leaveSelectionMode();
                return;
            case 3:
            case 4:
                onSelectionChange(null, false);
                this.mRootPane.invalidate();
                return;
            default:
                return;
        }
    }

    protected void enterSelectionMode() {
        SelectionMode sm = this.mActionBar.enterSelectionMode(true);
        sm.setTitle((int) R.string.has_selected);
        sm.setBothAction(Action.NO, Action.NONE);
        sm.setMenu(SELECTION_MENU.length, SELECTION_MENU);
        sm.show();
    }

    public void onSelectionChange(Path path, boolean selected) {
        int count = this.mSelectionManager.getSelectedCount();
        int total = this.mSelectionManager.getTotalCount();
        SelectionMode sm = (SelectionMode) this.mActionBar.getCurrentMode();
        if (count == 0) {
            sm.changeAction(Action.ACTION_ID_DEALL, Action.ACTION_ID_ALL);
            sm.setTitle((int) R.string.no_selected);
            sm.setCount(null);
            dealAllAction(false);
            return;
        }
        if (count == total) {
            sm.changeAction(Action.ACTION_ID_ALL, Action.ACTION_ID_DEALL);
        } else {
            sm.changeAction(Action.ACTION_ID_DEALL, Action.ACTION_ID_ALL);
        }
        sm.setTitle((int) R.string.has_selected);
        sm.setCount(count);
        dealAllAction(true);
    }

    protected void dealAllAction(boolean enable) {
        ActionBarStateBase currentMode = this.mActionBar.getCurrentMode();
        for (Action action : SELECTION_MENU) {
            if (!(Action.ALL.equalAction(action) || Action.DEALL.equalAction(action))) {
                currentMode.setActionEnable(enable, action.id);
            }
        }
    }

    protected void leaveSelectionMode() {
        this.mActionBar.leaveCurrentMode();
        ActionBarStateBase am = this.mActionBar.getCurrentMode();
        if (this.mIsCustomAlbum) {
            am.setMenu(1, DEFAULT_MENU[0]);
        } else {
            am.setMenu(1, DEFAULT_MENU[2]);
        }
        this.mSlotView.invalidate();
    }

    protected boolean autoFinishWhenNoItems() {
        return false;
    }

    protected void updateMenu(boolean isSizeZero) {
    }

    protected SlotView getSlotView() {
        return this.mSlotView;
    }

    public void onNavigationBarChanged(boolean show, int height) {
        super.onNavigationBarChanged(show, height);
        this.mRootPane.requestLayout();
    }

    protected SlotRenderInterface getSlotRenderInterface() {
        return this.mSlotRenderer;
    }

    protected void onPhotoFallback() {
        this.mSlotRenderer.setSlotFilter(this.mResumeEffect);
        this.mResumeEffect.setPositionProvider(this.mFocusIndex, this.mPositionProvider);
        this.mResumeEffect.start();
    }

    public void onBlurWallpaperChanged() {
        this.mTopCover.textureDirty();
    }

    public void onScrollSelect(int index, boolean selected) {
        if (this.mSelectionManager.inSelectionMode() && !this.mSelectionManager.inSingleMode()) {
            MediaItem item = this.mAlbumAdapter.get(index);
            if (item != null && !item.isPhotoSharePreView()) {
                Path path = item.getPath();
                GalleryLog.d("KeyguardAlbumPage", "Get Keyguard scrollSelect path successed");
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
        MediaItem item = this.mAlbumAdapter.get(index);
        if (item == null) {
            return false;
        }
        return this.mSelectionManager.isItemSelected(item.getPath());
    }

    protected void onLoadingStarted() {
        super.onLoadingStarted();
        this.mSlotRenderer.onLoadingStarted();
    }

    protected void onLoadingFinished(boolean loadingFailed) {
        super.onLoadingFinished(loadingFailed);
        this.mSlotRenderer.onLoadingFinished();
    }

    private void onDownload() {
        if (isNetworkConnected()) {
            showProgressDialog();
            ComponentName componetName = new ComponentName("com.android.keyguard", "com.huawei.keyguard.magazine.update.DownloadService");
            Intent intent = new Intent("com.android.keyguard.magazinulock.update.CHECK_CHANNEL");
            intent.setComponent(componetName);
            intent.putExtra("audoCheck", false);
            intent.putExtra("type", 6);
            GalleryLog.d("KeyguardAlbumPage", "start Check Service");
            this.mHost.getActivity().startService(intent);
            return;
        }
        this.mHandler.removeMessages(0);
        this.mHandler.sendEmptyMessage(0);
    }

    public void clearAllErrorMsg() {
        if (this.mHandler != null) {
            this.mHandler.removeCallbacksAndMessages(null);
        }
    }

    private boolean setupEmptyButton() {
        RelativeLayout galleryRoot = (RelativeLayout) this.mHost.getActivity().findViewById(R.id.gallery_root);
        if (galleryRoot == null) {
            return false;
        }
        this.mEmptyAlbumLayout = (LinearLayout) ((LayoutInflater) this.mHost.getActivity().getSystemService("layout_inflater")).inflate(R.layout.empty_album, galleryRoot, false);
        TextView mTextView = (TextView) this.mEmptyAlbumLayout.findViewById(R.id.no_picture_name);
        if (this.mIsCustomAlbum) {
            mTextView.setText(R.string.no_added_pictures);
        } else {
            mTextView.setText(R.string.no_downloaded_pictures);
        }
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
        emptyLayout.setGravity(1);
    }

    private void cleanupEmptyButton() {
        if (this.mEmptyAlbumLayout != null && ((RelativeLayout) this.mHost.getActivity().findViewById(R.id.gallery_root)) != null) {
            this.mEmptyAlbumLayout = null;
        }
    }

    private void showProgressDialog() {
        int i;
        String message = this.mHost.getActivity().getResources().getString(R.string.keyguard_settings_update);
        if (this.mProgressDialog == null) {
            this.mProgressDialog = ProgressDialog.show(this.mHost.getActivity(), "", message, false, false);
        }
        this.mProgressDialog.show();
        KeyguardHandler keyguardHandler = this.mHandler;
        if (this.mIsUpdateAvailable) {
            i = 3;
        } else {
            i = 1;
        }
        keyguardHandler.sendEmptyMessageDelayed(i, 1000);
    }

    private void dismissProgressDialog() {
        if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
            this.mProgressDialog.dismiss();
        }
        this.mProgressDialog = null;
    }

    public void finishProgressDialog(int resId) {
        dismissProgressDialog();
        if (resId != -1) {
            Toast.makeText(this.mHost.getActivity(), resId, 0).show();
        }
    }

    private boolean isNetworkConnected() {
        NetworkInfo mNetworkInfo = ((ConnectivityManager) this.mHost.getActivity().getSystemService("connectivity")).getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }
}
