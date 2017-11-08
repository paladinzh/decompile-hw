package com.huawei.gallery.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.android.gallery3d.R;
import com.android.gallery3d.data.KeyguardItem;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.MenuExecutor.ProgressListener;
import com.android.gallery3d.ui.MenuExecutor.SimpleProgressListener;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ReportToBigData;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionBarStateBase;
import com.huawei.gallery.actionbar.DetailActionMode;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.app.PhotoPage.ActionBarProgressActionListener;
import com.huawei.gallery.app.plugin.PhotoExtraButton;
import com.huawei.gallery.app.plugin.PhotoFragmentPluginManager;
import com.huawei.gallery.app.plugin.PhotoFragmentPluginManager.PluginHost;

public class KeyguardPhotoPage extends AbsPhotoPage implements OnClickListener, PluginHost {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    protected final ActionBarProgressActionListener mActionProgressActionListener = new ActionBarProgressActionListener() {
        public void onStart() {
            if (KeyguardPhotoPage.this.mHandler != null) {
                KeyguardPhotoPage.this.mHandler.removeMessages(1);
            }
        }

        public void onEnd() {
            KeyguardPhotoPage.this.refreshHidingMessage();
        }
    };
    private ProgressListener mConfirmDialogListener = new SimpleProgressListener() {
        public void onProgressExecuteSuccess(String path) {
            MediaObject mediaObj = KeyguardPhotoPage.this.mHost.getGalleryContext().getDataManager().getMediaObject(path);
            if (mediaObj instanceof KeyguardItem) {
                String removeType = "Local";
                switch (((KeyguardItem) mediaObj).type) {
                    case 0:
                        removeType = "UnlockMagazine";
                        break;
                    case 1:
                        removeType = "Local";
                        break;
                }
                ReportToBigData.report(48, String.format("{RemoveMagazine:%s}", new Object[]{removeType}));
            }
        }

        public void onConfirmDialogShown() {
            KeyguardPhotoPage.this.mHandler.removeMessages(1);
        }

        public void onConfirmDialogDismissed(boolean confirmed) {
            KeyguardPhotoPage.this.refreshHidingMessage();
        }
    };
    private boolean mInPluginMode = false;
    private PhotoFragmentPluginManager mPluginManager;

    private static /* synthetic */ int[] -getcom-huawei-gallery-actionbar-ActionSwitchesValues() {
        if (-com-huawei-gallery-actionbar-ActionSwitchesValues != null) {
            return -com-huawei-gallery-actionbar-ActionSwitchesValues;
        }
        int[] iArr = new int[Action.values().length];
        try {
            iArr[Action.ADD.ordinal()] = 4;
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
            iArr[Action.KEYGUARD_LIKE.ordinal()] = 1;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[Action.KEYGUARD_NOT_LIKE.ordinal()] = 2;
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
            iArr[Action.REMOVE.ordinal()] = 3;
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

    protected void onGLRootLayout(boolean changeSize, int left, int top, int right, int bottom) {
        this.mPhotoView.layout(0, 0, right - left, bottom - top);
    }

    protected void onCreate(Bundle data, Bundle storedState) {
        super.onCreate(data, storedState);
        this.mFlags |= 16;
        this.mPhotoView.setFilmModeAllowed(false);
        TraceController.traceBegin("KeyguardPhotoPage.onCreate.mPluginManager");
        this.mPluginManager = new PhotoFragmentPluginManager();
        this.mPluginManager.init((ViewGroup) this.mHost.getActivity().findViewById(R.id.gallery_root), this.mHost.getGalleryContext(), this, this.mActionProgressActionListener);
        TraceController.traceEnd();
    }

    protected boolean onCreateActionBar(Menu menu) {
        this.mHost.requestFeature(348);
        DetailActionMode am = this.mActionBar.enterDetailActionMode(false);
        am.setBothAction(Action.NONE, Action.NONE);
        am.setMenu(2, Action.KEYGUARD_NOT_LIKE, Action.REMOVE);
        am.show();
        am.hideRightAction(false);
        return true;
    }

    protected void onResume() {
        super.onResume();
        this.mPluginManager.onResume();
        updateMenuOperations();
        updateExtraButton();
        hideBars(false);
    }

    protected void onPause() {
        super.onPause();
        this.mPluginManager.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mPluginManager.onDestroy();
    }

    protected boolean onItemSelected(Action action) {
        refreshHidingMessage();
        if (this.mPluginManager.onInterceptActionItemClick(action)) {
            return false;
        }
        MediaItem current = this.mModel.getMediaItem(0);
        if (current == null) {
            return false;
        }
        Path path = current.getPath();
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
            case 2:
                handleFavoriteAction(action, path);
                return true;
            case 3:
                if (this.mPhotoView.isExtraActionDoing()) {
                    return true;
                }
                String title = this.mHost.getActivity().getResources().getString(R.string.remove_single_file);
                this.mSelectionManager.deSelectAll();
                this.mSelectionManager.toggle(path);
                this.mMenuExecutor.onMenuClicked(action, null, title, this.mConfirmDialogListener);
                return true;
            default:
                throw new IllegalArgumentException("undefined action : " + action.name());
        }
    }

    protected boolean onBackPressed() {
        setStateResult(-1, getDefaultResult());
        if (this.mPluginManager.onBackPressed()) {
            return true;
        }
        return super.onBackPressed();
    }

    protected boolean updateCurrentPhoto(MediaItem photo) {
        updateWidthAndHeightForDefaultType(photo);
        if (super.updateCurrentPhoto(photo)) {
            updateMenuOperations();
            return true;
        }
        updateMenuOperations();
        updateExtraButton();
        return false;
    }

    private void updateExtraButton() {
        int i = 0;
        GalleryActionBar galleryActionBar = this.mHost.getGalleryActionBar();
        if (galleryActionBar != null) {
            ActionBarStateBase actionBarStateBase = galleryActionBar.getCurrentMode();
            if ((actionBarStateBase instanceof DetailActionMode) && isExtraButtonEnable()) {
                int i2;
                PhotoExtraButton extraButton = ((DetailActionMode) actionBarStateBase).getExtraButton();
                PhotoExtraButton extraButton1 = ((DetailActionMode) actionBarStateBase).getExtraButton1();
                extraButton.setOnClickListener(this);
                extraButton1.setOnClickListener(this);
                if (this.mPluginManager.updatePhotoExtraButton(extraButton, this.mCurrentPhoto)) {
                    i2 = 0;
                } else {
                    i2 = 8;
                }
                extraButton.setVisibility(i2);
                if (!this.mPluginManager.updatePhotoExtraButton(extraButton1, this.mCurrentPhoto)) {
                    i = 8;
                }
                extraButton1.setVisibility(i);
            }
        }
    }

    private boolean isExtraButtonEnable() {
        return (this.mCurrentPhoto == null || this.mInPluginMode || this.mPhotoView.getFilmMode()) ? false : true;
    }

    protected void onPhotoChanged(int index, Path item) {
        boolean isPhotoChanged = true;
        if (item != null && item.equalsIgnoreCase(this.mCurrentPhoto)) {
            isPhotoChanged = false;
        }
        if (isPhotoChanged) {
            this.mPluginManager.onPhotoChanged();
        }
        super.onPhotoChanged(index, item);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateWidthAndHeightForDefaultType(MediaItem photo) {
        if (photo != null && this.mModel != null && ((KeyguardItem) photo).type == 0 && (photo.getWidth() <= 0 || photo.getHeight() <= 0)) {
            try {
                int width = this.mModel.getImageWidth();
                int height = this.mModel.getImageHeight();
                if (width <= 0 || height <= 0) {
                    Options opts = new Options();
                    opts.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(((KeyguardItem) photo).filePath, opts);
                    width = opts.outWidth;
                    height = opts.outHeight;
                }
                if (width > 0 && height > 0) {
                    ((KeyguardItem) photo).updateItemWidthAndHeight(this.mHost.getActivity(), width, height);
                }
            } catch (Throwable ex) {
                GalleryLog.w("KeyguardPhotoPage", ex);
            }
        }
    }

    private void updateMenuOperations() {
        MediaItem currentPhoto = this.mCurrentPhoto;
        if (currentPhoto != null && this.mActionBar != null) {
            ActionBarStateBase mode = this.mActionBar.getCurrentMode();
            if (mode != null) {
                if (((KeyguardItem) currentPhoto).type == 0) {
                    mode.setActionEnable(true, Action.ACTION_ID_KEYGUARD_LIKE);
                    mode.setActionEnable(true, Action.ACTION_ID_KEYGUARD_NOT_LIKE);
                    if ((currentPhoto.getExtraTag() & 4) != 0) {
                        mode.changeAction(Action.ACTION_ID_KEYGUARD_NOT_LIKE, Action.ACTION_ID_KEYGUARD_LIKE);
                    } else {
                        mode.changeAction(Action.ACTION_ID_KEYGUARD_LIKE, Action.ACTION_ID_KEYGUARD_NOT_LIKE);
                    }
                } else {
                    mode.changeAction(Action.ACTION_ID_KEYGUARD_LIKE, Action.ACTION_ID_KEYGUARD_NOT_LIKE);
                    mode.setActionEnable(false, Action.ACTION_ID_KEYGUARD_LIKE);
                    mode.setActionEnable(false, Action.ACTION_ID_KEYGUARD_NOT_LIKE);
                }
            }
        }
    }

    public void onSingleTapUp(int x, int y) {
        if (this.mModel.getMediaItem(0) != null) {
            toggleBars();
        }
    }

    protected void showBars(boolean wantAnim) {
        if (!this.mShowBars) {
            this.mShowBars = true;
            this.mActionBar.setActionBarVisible(true, wantAnim);
            updateExtraButton();
            refreshHidingMessage();
        }
    }

    protected void hideBars(boolean wantAnim) {
        if (this.mShowBars) {
            this.mShowBars = false;
            this.mActionBar.setActionBarVisible(false, wantAnim);
            this.mHandler.removeMessages(1);
        }
    }

    public void onNavigationBarChanged(boolean show, int height) {
        this.mRootPane.requestLayout();
    }

    protected boolean onFingprintKeyActivated() {
        return true;
    }

    private void handleFavoriteAction(Action action, Path path) {
        if (Action.KEYGUARD_LIKE.equalAction(action)) {
            this.mActionBar.getCurrentMode().changeAction(Action.ACTION_ID_KEYGUARD_LIKE, Action.ACTION_ID_KEYGUARD_NOT_LIKE);
        } else {
            this.mActionBar.getCurrentMode().changeAction(Action.ACTION_ID_KEYGUARD_NOT_LIKE, Action.ACTION_ID_KEYGUARD_LIKE);
            if (shouldShowToast()) {
                ContextedUtils.showToastQuickly(this.mHost.getActivity(), (int) R.string.emui30_keyguard_cover_like_toast, 0);
            }
        }
        this.mSelectionManager.deSelectAll();
        this.mSelectionManager.toggle(path);
        this.mMenuExecutor.onMenuClicked(action, null, false, false);
    }

    private boolean shouldShowToast() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.mHost.getActivity());
        int num = 0;
        try {
            num = pref.getInt("key-show-like-toast-num", 0);
        } catch (Throwable th) {
        }
        if (num >= 3) {
            return false;
        }
        pref.edit().putInt("key-show-like-toast-num", num + 1).commit();
        return true;
    }

    public void onClick(View v) {
        if (isExtraButtonEnable() && this.mPluginManager.onEventsHappens(this.mCurrentPhoto, v)) {
            this.mHandler.removeMessages(1);
            this.mInPluginMode = true;
        }
    }

    public GalleryActionBar getGalleryActionBar() {
        return this.mActionBar;
    }

    public void onLeavePluginMode(int pluginID, Intent intent) {
        this.mInPluginMode = false;
        refreshHidingMessage();
        if (this.mShowBars) {
            hideBars(true);
        } else {
            this.mActionBar.setActionBarVisible(false);
        }
    }
}
