package com.huawei.gallery.refocus.app;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.amap.api.services.core.AMapException;
import com.android.gallery3d.R;
import com.android.gallery3d.anim.Animation.AnimationListener;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.ImageCacheService;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.AbsPhotoView;
import com.android.gallery3d.ui.BitmapScreenNail;
import com.android.gallery3d.ui.EmptyPhotoView;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLRootView;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.MediaScannerClient;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.view.SimpleActionItem;
import com.huawei.gallery.app.AbsPhotoPage;
import com.huawei.gallery.refocus.ui.RefocusEditorHeadGroupView;
import com.huawei.gallery.refocus.ui.RefocusIndicator;
import com.huawei.gallery.refocus.ui.RefocusView;
import com.huawei.gallery.refocus.ui.RefocusView.Listener;
import com.huawei.gallery.refocus.wideaperture.app.WideApertureState;
import com.huawei.gallery.refocus.wideaperture.ui.RefocusEditorOpenOrQuitEffect;
import com.huawei.gallery.util.GalleryPool;
import com.huawei.gallery.util.LayoutHelper;
import java.io.File;

public class RefocusPage extends AbsPhotoPage implements Listener, OnClickListener {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    private ActionInfo mActionInfo;
    private AbsRefocusController mEditorController;
    private AbsRefocusDelegate mEditorDelegate;
    private int mEditorMode;
    private RefocusView mEditorView;
    private Rect mEditorViewBounds;
    private RefocusEditorOpenOrQuitEffect mEnterEffect = null;
    private RefocusEditorOpenOrQuitEffect mExitEffect = null;
    private RefocusIndicator mFocusIndicator;
    private RefocusEditorHeadGroupView mHeadGroupView;
    private boolean mIsEnterAnimEnd = false;
    private boolean mIsPhotoFileFirstDecodingComplete = false;
    private boolean mIsPhotoViewFirstDecodingComplete = false;
    private boolean mIsRefrshWithAnim = false;
    private MyOnLayoutChangeListener mLayoutChangeListener;
    private Runnable mPendingOperations;
    private boolean mPendingShowFocusIndicator = true;
    private ProgressBar mProgressbar;
    private RelativeLayout mRoot;
    private boolean mSaveCurrentState = true;
    private boolean mShowFocusIndicator;
    private StateManager mStateManager;
    private final OnSystemUiVisibilityChangeListener mUiVisibility = new OnSystemUiVisibilityChangeListener() {
        public void onSystemUiVisibilityChange(int visibility) {
            if (visibility == 0) {
                RefocusPage.this.mHandler.removeMessages(2);
                RefocusPage.this.mHandler.sendEmptyMessageDelayed(2, (long) RefocusPage.HIDE_BARS_TIMEOUT);
            }
        }
    };
    private View mView;

    private class AbsRefocusEditorDelegate extends AbsRefocusDelegate {
        private AbsRefocusEditorDelegate() {
        }

        public String getFilePath() {
            return RefocusPage.this.mModel.getMediaItem(0).getFilePath();
        }

        public Point getTouchPositionInImage(Point touchPosion) {
            return RefocusPage.this.mEditorView.getTouchPositionInImage(touchPosion);
        }

        public Point transformToScreenCoordinate(Point focusPoint) {
            return RefocusPage.this.mEditorView.transformToScreenCoordinate(focusPoint);
        }

        public int getPhotoHeight() {
            return RefocusPage.this.mModel.getImageHeight();
        }

        public int getPhotoWidth() {
            return RefocusPage.this.mModel.getImageWidth();
        }

        public void refreshPhoto(byte[] bytes, int offset, int length) {
            RefocusPage.this.mModel.invalidateData(bytes, offset, length);
        }

        public void refreshPhoto(BitmapScreenNail bitmapScreenNail) {
            RefocusPage.this.mModel.invalidateData(bitmapScreenNail);
        }

        public void showFocusIndicator(Point pointer) {
            int i = 0;
            if (RefocusPage.this.mFocusIndicator != null) {
                Rect focusEdge = new Rect();
                Point ImageTopLeft = new Point(0, 0);
                RelativeLayout galleryRoot = (RelativeLayout) RefocusPage.this.mHost.getActivity().findViewById(R.id.allfocus_root);
                if (galleryRoot != null && pointer.x != -1 && pointer.y != -1) {
                    Point ImageBottomRight;
                    int i2;
                    if (RefocusPage.this.mCurrentPhoto.getRotation() == 90 || RefocusPage.this.mCurrentPhoto.getRotation() == 270) {
                        ImageBottomRight = new Point(getPhotoHeight(), getPhotoWidth());
                    } else {
                        ImageBottomRight = new Point(getPhotoWidth(), getPhotoHeight());
                    }
                    int displayWidth = galleryRoot.getWidth();
                    int displayHeight = galleryRoot.getHeight();
                    ImageTopLeft = transformToScreenCoordinate(ImageTopLeft);
                    ImageBottomRight = transformToScreenCoordinate(ImageBottomRight);
                    if (ImageTopLeft.x > 0) {
                        i2 = ImageTopLeft.x;
                    } else {
                        i2 = 0;
                    }
                    focusEdge.left = i2;
                    if (ImageTopLeft.y > 0) {
                        i = ImageTopLeft.y;
                    }
                    focusEdge.top = i;
                    if (ImageBottomRight.x < displayWidth) {
                        displayWidth = ImageBottomRight.x;
                    }
                    focusEdge.right = displayWidth;
                    if (ImageBottomRight.y < displayHeight) {
                        displayHeight = ImageBottomRight.y;
                    }
                    focusEdge.bottom = displayHeight;
                    RefocusPage.this.mFocusIndicator.setScale(focusEdge);
                    RefocusPage.this.mFocusIndicator.setLocation(pointer.x, pointer.y);
                    RefocusPage.this.mFocusIndicator.showFocuing();
                }
            }
        }

        public int getWideApertureValueChangedMessageID() {
            return 29;
        }

        public void finishRefocus() {
            RefocusPage.this.mHandler.sendEmptyMessage(22);
        }

        public void sendEmptyMessage(int what) {
            RefocusPage.this.mHandler.sendEmptyMessage(what);
        }

        public void sendMessageDelayed(int what, int arg1, int arg2, long delayMillis) {
            RefocusPage.this.mHandler.obtainMessage(what, arg1, arg2).sendToTarget();
        }

        public void removeMessages(int what) {
            RefocusPage.this.mHandler.removeMessages(what);
        }

        public int getDoRefocusMessageID() {
            return 21;
        }

        public int getApplyFilterMessageID() {
            return 30;
        }

        public int getRefocusSaveMessageID() {
            return 24;
        }

        public int getFinishRefocusMessageID() {
            return 22;
        }

        public int getShowProgressMessageID() {
            return 27;
        }

        public void saveAsComplete(int saveState) {
            RefocusPage.this.mHandler.sendEmptyMessage(28);
            RefocusPage.this.mHandler.sendEmptyMessage(31);
        }

        public void saveFileComplete(int actionState) {
            if (actionState == 0) {
                MediaScannerClient mediaScannerClient = new MediaScannerClient(RefocusPage.this.mHost.getActivity(), new File(RefocusPage.this.mCurrentPhoto.getFilePath()), RefocusPage.this.mCurrentPhoto);
                ImageCacheService cacheService = RefocusPage.this.mHost.getGalleryContext().getGalleryApplication().getImageCacheService();
                if (cacheService != null) {
                    cacheService.removeImageData(RefocusPage.this.mCurrentPhoto.getPath(), RefocusPage.this.mCurrentPhoto.getDateModifiedInSec(), 2);
                    cacheService.removeImageData(RefocusPage.this.mCurrentPhoto.getPath(), RefocusPage.this.mCurrentPhoto.getDateModifiedInSec(), 1);
                    cacheService.removeImageData(RefocusPage.this.mCurrentPhoto.getPath(), RefocusPage.this.mCurrentPhoto.getDateModifiedInSec(), 8);
                }
                GalleryPool.remove(RefocusPage.this.mCurrentPhoto.getPath(), RefocusPage.this.mCurrentPhoto.getDateModifiedInSec());
            } else if (actionState == -1) {
                RefocusPage.this.mHandler.sendEmptyMessage(26);
            }
            RefocusPage.this.mHandler.sendEmptyMessageDelayed(25, 50);
        }

        public void preparePhotoComplete() {
            if (!RefocusPage.this.mIsPhotoFileFirstDecodingComplete) {
                RefocusPage.this.mIsPhotoFileFirstDecodingComplete = true;
                RefocusPage.this.showFocusIndicator();
            }
            RefocusPage.this.mStateManager.getCurrentState().enableSaveAction();
            RefocusPage.this.mStateManager.getCurrentState().showActionToast();
        }

        public void onGotFocusPoint() {
            RefocusPage.this.showDefaultFocusPoint();
        }

        public void enableSaveAction(boolean enabled) {
            RefocusPage.this.enableSave(enabled);
        }

        public void doBackPress() {
            RefocusPage.this.mHost.getActivity().onBackPressed();
        }

        public void refreshLayout() {
            RefocusPage.this.refreshLayout();
        }
    }

    public static class ActionInfo {
        private Action mLeftAction;
        private boolean mNeedHideActionBar;
        private Action mRightAction;
        private String mTitle;

        public ActionInfo(String title, Action left, Action right, boolean hideActionBar) {
            this.mTitle = title;
            this.mLeftAction = left;
            this.mRightAction = right;
            this.mNeedHideActionBar = hideActionBar;
        }

        public String getTitle() {
            return this.mTitle;
        }

        public Action getLeftAction() {
            return this.mLeftAction;
        }

        public Action getRightAction() {
            return this.mRightAction;
        }

        public boolean needHideActionBar() {
            return this.mNeedHideActionBar;
        }
    }

    private class MyOnLayoutChangeListener implements OnLayoutChangeListener {
        private int mNavigationBarHeight;

        private MyOnLayoutChangeListener() {
            this.mNavigationBarHeight = -1;
        }

        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            boolean changed = true;
            if (bottom - oldBottom == 0 && this.mNavigationBarHeight == LayoutHelper.getNavigationBarHeight()) {
                changed = false;
            }
            if (changed) {
                this.mNavigationBarHeight = LayoutHelper.getNavigationBarHeight();
                RefocusPage.this.onLayoutChanged(this.mNavigationBarHeight);
                RefocusPage.this.mStateManager.getCurrentState().scrollToSelectedFilter();
                if (RefocusPage.this.mFocusIndicator != null) {
                    RefocusPage.this.mFocusIndicator.clear();
                }
            }
        }
    }

    class PendingEnableAllFocusOperations implements Runnable {
        PendingEnableAllFocusOperations() {
        }

        public void run() {
            RefocusPage.this.enableAllFocusOperations(true);
        }
    }

    public class WideAperturePhotoPhotoEditorDelegateAbs extends AbsRefocusEditorDelegate {
        public /* bridge */ /* synthetic */ void doBackPress() {
            super.doBackPress();
        }

        public /* bridge */ /* synthetic */ void enableSaveAction(boolean enabled) {
            super.enableSaveAction(enabled);
        }

        public /* bridge */ /* synthetic */ void finishRefocus() {
            super.finishRefocus();
        }

        public /* bridge */ /* synthetic */ int getApplyFilterMessageID() {
            return super.getApplyFilterMessageID();
        }

        public /* bridge */ /* synthetic */ int getDoRefocusMessageID() {
            return super.getDoRefocusMessageID();
        }

        public /* bridge */ /* synthetic */ String getFilePath() {
            return super.getFilePath();
        }

        public /* bridge */ /* synthetic */ int getFinishRefocusMessageID() {
            return super.getFinishRefocusMessageID();
        }

        public /* bridge */ /* synthetic */ int getPhotoHeight() {
            return super.getPhotoHeight();
        }

        public /* bridge */ /* synthetic */ int getPhotoWidth() {
            return super.getPhotoWidth();
        }

        public /* bridge */ /* synthetic */ int getRefocusSaveMessageID() {
            return super.getRefocusSaveMessageID();
        }

        public /* bridge */ /* synthetic */ int getShowProgressMessageID() {
            return super.getShowProgressMessageID();
        }

        public /* bridge */ /* synthetic */ Point getTouchPositionInImage(Point touchPosion) {
            return super.getTouchPositionInImage(touchPosion);
        }

        public /* bridge */ /* synthetic */ int getWideApertureValueChangedMessageID() {
            return super.getWideApertureValueChangedMessageID();
        }

        public /* bridge */ /* synthetic */ void onGotFocusPoint() {
            super.onGotFocusPoint();
        }

        public /* bridge */ /* synthetic */ void preparePhotoComplete() {
            super.preparePhotoComplete();
        }

        public /* bridge */ /* synthetic */ void refreshLayout() {
            super.refreshLayout();
        }

        public /* bridge */ /* synthetic */ void refreshPhoto(byte[] bytes, int offset, int length) {
            super.refreshPhoto(bytes, offset, length);
        }

        public /* bridge */ /* synthetic */ void removeMessages(int what) {
            super.removeMessages(what);
        }

        public /* bridge */ /* synthetic */ void saveAsComplete(int saveState) {
            super.saveAsComplete(saveState);
        }

        public /* bridge */ /* synthetic */ void saveFileComplete(int actionState) {
            super.saveFileComplete(actionState);
        }

        public /* bridge */ /* synthetic */ void sendEmptyMessage(int what) {
            super.sendEmptyMessage(what);
        }

        public /* bridge */ /* synthetic */ void sendMessageDelayed(int what, int arg1, int arg2, long delayMillis) {
            super.sendMessageDelayed(what, arg1, arg2, delayMillis);
        }

        public /* bridge */ /* synthetic */ void showFocusIndicator(Point pointer) {
            super.showFocusIndicator(pointer);
        }

        public /* bridge */ /* synthetic */ Point transformToScreenCoordinate(Point focusPoint) {
            return super.transformToScreenCoordinate(focusPoint);
        }

        public WideAperturePhotoPhotoEditorDelegateAbs() {
            super();
        }

        public void refreshPhoto(Bitmap bitmap) {
            RefocusPage.this.mModel.invalidateData(bitmap);
        }

        public void setWideApertureValue(int value) {
            RefocusPage.this.mStateManager.getCurrentState().setWideApertureValue(value);
        }

        public void finishActivity() {
            RefocusPage.this.setResult();
            RefocusPage.this.exitEditorModeWithAnimIfNeed();
            RefocusPage.this.mPendingOperations = new PendingEnableAllFocusOperations();
        }

        public Point getImageRightBottomPoint() {
            if (RefocusPage.this.mCurrentPhoto.getRotation() == 90 || RefocusPage.this.mCurrentPhoto.getRotation() == 270) {
                return new Point(getPhotoHeight(), getPhotoWidth());
            }
            return new Point(getPhotoWidth(), getPhotoHeight());
        }
    }

    private static /* synthetic */ int[] -getcom-huawei-gallery-actionbar-ActionSwitchesValues() {
        if (-com-huawei-gallery-actionbar-ActionSwitchesValues != null) {
            return -com-huawei-gallery-actionbar-ActionSwitchesValues;
        }
        int[] iArr = new int[Action.values().length];
        try {
            iArr[Action.ADD.ordinal()] = 3;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Action.ADD_ALBUM.ordinal()] = 4;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Action.ADD_COMMENT.ordinal()] = 5;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Action.AIRSHARE.ordinal()] = 6;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Action.ALBUM.ordinal()] = 7;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Action.ALL.ordinal()] = 8;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Action.BACK.ordinal()] = 1;
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
            iArr[Action.SAVE.ordinal()] = 2;
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
        boolean z;
        super.onCreate(data, storedState);
        if (storedState == null) {
            z = true;
        } else {
            z = false;
        }
        this.mSaveCurrentState = z;
        this.mFlags |= 16;
        if (data.getBoolean("is-secure-camera-album", false)) {
            this.mFlags |= FragmentTransaction.TRANSIT_EXIT_MASK;
        }
        this.mEditorMode = data.getInt("Visual-Effects-Mode");
        this.mStateManager = new StateManager();
        initViews();
        initData(data);
    }

    private void initData(Bundle data) {
        MediaItem mediaItem = getMediaItem(data);
        if (mediaItem == null) {
            ContextedUtils.showToastQuickly(this.mHost.getActivity(), (int) R.string.fail_to_load_image_Toast, 1);
            return;
        }
        this.mModel = new RefocusSinglePhotoDataAdapter(this.mHost.getGalleryContext(), this.mHost.getGLRoot(), this.mEditorView, mediaItem);
        this.mEditorView.setModel(this.mModel);
        this.mEditorView.setMediaItemScreenNail(mediaItem);
        this.mEditorController = this.mStateManager.getCurrentState().getAllFocusController();
        this.mEditorViewBounds = new Rect();
        updateCurrentPhoto(mediaItem);
    }

    private MediaItem getMediaItem(Bundle data) {
        if (data == null) {
            return null;
        }
        Path itemPath = getItemPath(data);
        MediaItem mediaItem = null;
        if (itemPath != null) {
            mediaItem = (MediaItem) this.mHost.getGalleryContext().getDataManager().getMediaObject(itemPath);
        }
        return mediaItem;
    }

    private void initViews() {
        this.mRoot = (RelativeLayout) this.mHost.getActivity().findViewById(R.id.gallery_root);
        this.mRoot = (RelativeLayout) LayoutInflater.from(this.mHost.getActivity()).inflate(R.layout.allfocus_fragment_main, this.mRoot);
        this.mHeadGroupView = (RefocusEditorHeadGroupView) this.mRoot.findViewById(R.id.head_layout);
        refreshHeadBarHeight();
        this.mView = this.mRoot.findViewById(R.id.allfocus_root);
        this.mEditorView = new RefocusView(this.mHost.getGalleryContext(), this.mHost.getGLRoot());
        this.mEditorView.setGLRoot(this.mHost.getGLRoot());
        this.mEditorView.setListener(this);
        this.mRootPane.addComponent(this.mEditorView);
        if (this.mEditorMode == 1) {
            this.mEditorDelegate = new WideAperturePhotoPhotoEditorDelegateAbs();
            this.mEnterEffect = new RefocusEditorOpenOrQuitEffect();
        } else if (this.mEditorMode == 0) {
            this.mEditorDelegate = new AbsRefocusEditorDelegate();
        } else if (this.mEditorMode == 2) {
            this.mEditorDelegate = new WideAperturePhotoPhotoEditorDelegateAbs();
        }
        this.mStateManager.enterState(this.mHost.getGalleryContext(), this.mView, this.mEditorDelegate, this.mEditorMode, this.mEditorView, null);
        this.mFocusIndicator = this.mStateManager.getCurrentState().getFocusIndicatorView();
        this.mProgressbar = this.mStateManager.getCurrentState().getProgressBar();
        this.mActionInfo = this.mStateManager.getCurrentState().getActionInfo();
        this.mShowFocusIndicator = true;
        this.mLayoutChangeListener = new MyOnLayoutChangeListener();
        this.mView.addOnLayoutChangeListener(this.mLayoutChangeListener);
    }

    protected void onHandleMessage(Message msg) {
        switch (msg.what) {
            case 2:
                return;
            case 21:
                this.mStateManager.getCurrentState().doRefocus(msg);
                return;
            case 22:
                this.mStateManager.getCurrentState().finishRefocus(this.mModel.getScreenNail());
                return;
            case 23:
                showFocusPoint();
                return;
            case 24:
                handleSaveRefocusFile();
                return;
            case 25:
                handleExitRefocus();
                return;
            case AMapException.ERROR_CODE_URL /*26*/:
                ContextedUtils.showToastQuickly(this.mHost.getActivity(), (int) R.string.photoshare_toast_nospace_Toast, 0);
                return;
            case AMapException.ERROR_CODE_UNKNOW_HOST /*27*/:
                showProgressbar();
                return;
            case AMapException.ERROR_CODE_UNKNOW_SERVICE /*28*/:
                hideProgressbar();
                return;
            case AMapException.ERROR_CODE_PROTOCOL /*29*/:
                this.mStateManager.getCurrentState().onWideApertureValueChanged(msg.arg1);
                return;
            case 30:
                this.mStateManager.getCurrentState().applyFilter(msg);
                return;
            case 31:
                enableAllFocusOperations(true);
                return;
            default:
                super.onHandleMessage(msg);
                return;
        }
    }

    private void hideProgressbar() {
        if (this.mProgressbar != null) {
            this.mProgressbar.setVisibility(8);
        }
    }

    private void showProgressbar() {
        if (this.mProgressbar != null) {
            refreshProgressBarPosition();
            this.mProgressbar.setVisibility(0);
        }
    }

    private void handleExitRefocus() {
        if (this.mProgressbar != null && this.mProgressbar.getVisibility() == 0) {
            this.mProgressbar.setVisibility(8);
            setResult();
            exitEditorModeWithAnimIfNeed();
            this.mStateManager.getCurrentState().disableExitEditPage(false);
            this.mPendingOperations = new PendingEnableAllFocusOperations();
        }
    }

    private void handleSaveRefocusFile() {
        if (this.mFocusIndicator != null) {
            this.mFocusIndicator.clear();
        }
        showProgressbar();
        if (this.mEditorController != null) {
            this.mEditorController.saveFileIfNecessary();
        }
    }

    private void showFocusPoint() {
        if (this.mShowFocusIndicator && this.mEditorController != null) {
            this.mShowFocusIndicator = false;
            this.mEditorController.showFocusIndicator();
        }
    }

    private void refreshProgressBarPosition() {
        if (this.mProgressbar != null) {
            if (isPort()) {
                this.mProgressbar.setTranslationY(((float) (-(this.mStateManager.getCurrentState().getScrollViewHeight() - this.mActionBar.getActionBarHeight()))) / 2.0f);
                this.mProgressbar.setTranslationX(0.0f);
            } else {
                this.mProgressbar.setTranslationX(((float) (-this.mStateManager.getCurrentState().getScrollViewWidth())) / 2.0f);
                this.mProgressbar.setTranslationY(((float) this.mActionBar.getActionBarHeight()) / 2.0f);
            }
        }
    }

    private boolean isPort() {
        return this.mHost.getActivity().getResources().getConfiguration().orientation == 1;
    }

    protected boolean onCreateActionBar(Menu menu) {
        this.mHost.requestFeature(351);
        return true;
    }

    protected void onResume() {
        super.onResume();
        this.mActionBar.setActionBarVisible(false);
        this.mActionBar.setMenuVisible(false);
        if (this.mModel != null) {
            this.mEditorView.resume();
            if (this.mActionInfo != null) {
                changeActionBar(this.mActionInfo);
            }
            if (this.mPendingOperations != null) {
                this.mPendingOperations.run();
                this.mPendingOperations = null;
            }
            this.mStateManager.getCurrentState().enableSaveAction();
            this.mStateManager.getCurrentState().resume();
            ((GLRootView) this.mHost.getGLRoot()).setOnSystemUiVisibilityChangeListener(this.mUiVisibility);
            LayoutHelper.getNavigationBarHandler().update();
        }
    }

    protected void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        this.mStateManager.getCurrentState().onConfigurationChanged(config);
        refreshHeadBarHeight();
        refreshProgressBarPosition();
    }

    private void refreshHeadBarHeight() {
        this.mHeadGroupView.setLayoutParams(new LayoutParams(-1, this.mActionBar.getActionBarHeight()));
    }

    public void onNavigationBarChanged(boolean show, int height) {
        super.onNavigationBarChanged(show, height);
        this.mStateManager.getCurrentState().onNavigationBarChanged(show, height);
        LayoutParams params = (LayoutParams) this.mHeadGroupView.getLayoutParams();
        if (isPort()) {
            height = 0;
        }
        params.rightMargin = height;
        this.mHeadGroupView.setLayoutParams(params);
        refreshProgressBarPosition();
    }

    private void refreshLayout() {
        this.mRootPane.requestLayout();
    }

    protected boolean onItemSelected(Action action) {
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
            case 2:
                return this.mStateManager.getCurrentState().onActionItemClick(action);
            default:
                return false;
        }
    }

    protected boolean onBackPressed() {
        GalleryLog.d("RefocusPage", "AllFocus Fragment onBackPressed called.");
        if (!this.mStateManager.getCurrentState().onBackPressed()) {
            exitEditorModeWithAnimIfNeed();
            setResult();
            this.mPendingOperations = new PendingEnableAllFocusOperations();
        }
        return true;
    }

    protected void onPause() {
        super.onPause();
        this.mShowFocusIndicator = false;
        ((GLRootView) this.mHost.getGLRoot()).setOnSystemUiVisibilityChangeListener(null);
        this.mEditorView.pause();
        this.mStateManager.getCurrentState().pause();
        if (this.mActionInfo != null && this.mSaveCurrentState) {
            leaveCurrentActionBarMode();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mView.removeOnLayoutChangeListener(this.mLayoutChangeListener);
        this.mEditorView.destroy();
        this.mStateManager.getCurrentState().destroy();
        this.mRoot.removeView(this.mView);
    }

    public void onSingleTapUp(int x, int y) {
        if (this.mModel.getMediaItem(0) != null) {
            this.mHandler.removeMessages(21);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(21, x, this.mActionBar.getActionBarHeight() + y));
        }
    }

    protected void onGLRootLayout(boolean changeSize, int left, int top, int right, int bottom) {
        this.mEditorViewBounds.set(0, this.mActionBar.getActionBarHeight(), (right - left) - (isPort() ? 0 : this.mStateManager.getCurrentState().getScrollViewWidth()), (bottom - top) - (isPort() ? this.mStateManager.getCurrentState().getScrollViewHeight() : 0));
        if (this.mEnterEffect != null) {
            startEnterAnim(left, top, right, bottom);
        } else if (this.mExitEffect != null) {
            startExitAnim(left, top, right, bottom);
        } else {
            if (!this.mIsRefrshWithAnim) {
                this.mEditorView.layout(this.mEditorViewBounds.left, this.mEditorViewBounds.top, this.mEditorViewBounds.right, this.mEditorViewBounds.bottom);
                this.mStateManager.getCurrentState().resetIndicatorLocation();
            }
            this.mIsRefrshWithAnim = false;
        }
    }

    private void startEnterAnim(int left, int top, int right, int bottom) {
        this.mIsRefrshWithAnim = true;
        if (this.mEnterEffect.isActive()) {
            this.mEnterEffect.calculate(System.currentTimeMillis());
            int[] currentPosition = this.mEnterEffect.getCurrentPosition();
            if (currentPosition.length == 0) {
                this.mEnterEffect.forceStop();
                this.mEnterEffect = null;
                this.mRootPane.requestLayout();
                this.mIsRefrshWithAnim = false;
                return;
            }
            this.mEditorView.layout(currentPosition[0], currentPosition[1], currentPosition[2], currentPosition[3]);
            if (!this.mEnterEffect.isActive()) {
                this.mEnterEffect = null;
            }
        } else {
            this.mEnterEffect.setAnimationListener(new AnimationListener() {
                public void onAnimationEnd() {
                    RefocusPage.this.mIsEnterAnimEnd = true;
                    RefocusPage.this.showFocusIndicator();
                }
            });
            this.mEnterEffect.init(new int[]{left, top, right, bottom}, new int[]{this.mEditorViewBounds.left, this.mEditorViewBounds.top, this.mEditorViewBounds.right, this.mEditorViewBounds.bottom});
            this.mEnterEffect.start();
            this.mEditorView.layout(left, top, right, bottom);
        }
        this.mRootPane.requestLayout();
    }

    private void startExitAnim(int left, int top, int right, int bottom) {
        this.mIsRefrshWithAnim = true;
        if (this.mExitEffect.isActive()) {
            this.mExitEffect.calculate(System.currentTimeMillis());
            int[] currentPosition = this.mExitEffect.getCurrentPosition();
            if (currentPosition.length == 0) {
                this.mExitEffect.forceStop();
                this.mExitEffect = null;
                this.mRootPane.requestLayout();
                this.mIsRefrshWithAnim = false;
                return;
            }
            this.mEditorView.layout(currentPosition[0], currentPosition[1], currentPosition[2], currentPosition[3]);
            if (!this.mExitEffect.isActive()) {
                this.mExitEffect = null;
            }
        } else {
            this.mExitEffect.init(new int[]{this.mEditorViewBounds.left, this.mEditorViewBounds.top, this.mEditorViewBounds.right, this.mEditorViewBounds.bottom}, new int[]{left, top, right, bottom});
            this.mExitEffect.start();
            this.mEditorView.layout(this.mEditorViewBounds.left, this.mEditorViewBounds.top, this.mEditorViewBounds.right, this.mEditorViewBounds.bottom);
        }
        this.mRootPane.requestLayout();
    }

    public boolean onTouch(MotionEvent event) {
        return false;
    }

    public void onLoadStateChange(int state) {
        if (state == 2) {
            exitEditorModeWithAnimIfNeed();
        }
    }

    protected void hideBars(boolean barWithAnim) {
        if (this.mShowBars) {
            this.mShowBars = false;
            this.mHandler.removeMessages(1);
            this.mActionBar.setActionBarVisible(false, false);
        }
    }

    public void onDecodeImageComplete() {
        if (!this.mIsPhotoViewFirstDecodingComplete) {
            this.mIsPhotoViewFirstDecodingComplete = true;
            showFocusIndicator();
        }
        if (this.mEditorDelegate != null) {
            this.mEditorDelegate.finishRefocus();
        }
        if (this.mEditorController != null) {
            this.mEditorController.resizePhoto();
        }
        if (!(this.mStateManager.getCurrentState() instanceof WideApertureState)) {
            showDefaultFocusPoint();
        }
    }

    public void changeActionBar(ActionInfo info) {
        this.mHeadGroupView.initView(this);
        this.mHeadGroupView.changeActionBar(info);
        hideBars(false);
        if (info.needHideActionBar() && !this.mShowBars) {
            this.mHeadGroupView.setVisibility(4);
        }
    }

    public void leaveCurrentActionBarMode() {
        hideBars(true);
        this.mShowBars = false;
    }

    public void enableSave(boolean enable) {
        this.mHeadGroupView.setSaveActionItemEnable(enable);
    }

    public void onClick(View v) {
        if (v instanceof SimpleActionItem) {
            onItemSelected(((SimpleActionItem) v).getAction());
        }
    }

    public void showFocusIndicator() {
        if (this.mIsPhotoViewFirstDecodingComplete && this.mIsEnterAnimEnd && this.mIsPhotoFileFirstDecodingComplete) {
            GalleryLog.d("RefocusPage", "start show indicator.");
            this.mShowFocusIndicator = true;
            this.mHandler.removeMessages(23);
            this.mHandler.sendEmptyMessageDelayed(23, 250);
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    RefocusPage.this.mStateManager.getCurrentState().showFootGroupView();
                }
            }, 250);
            return;
        }
        GalleryLog.d("RefocusPage", "Wait until the picture decoding or anim is complete to show focus indicator, view decoding status: " + this.mIsPhotoViewFirstDecodingComplete + ", enter anim status: " + this.mIsEnterAnimEnd + ", file prepare status: " + this.mIsPhotoFileFirstDecodingComplete);
    }

    private void onLayoutChanged(int naviHeight) {
        this.mStateManager.getCurrentState().onLayoutChanged(this.mHost, naviHeight);
    }

    private void exitEditorModeWithAnimIfNeed() {
        if (this.mStateManager.getCurrentState() instanceof WideApertureState) {
            if (this.mFocusIndicator != null) {
                this.mFocusIndicator.clear();
            }
            this.mExitEffect = new RefocusEditorOpenOrQuitEffect();
            this.mExitEffect.setAnimationListener(new AnimationListener() {
                public void onAnimationEnd() {
                    RefocusPage.this.mHandler.sendEmptyMessage(50);
                }
            });
            refreshLayout();
            return;
        }
        this.mHandler.sendEmptyMessage(50);
    }

    private void setResult() {
        Uri uri = null;
        Intent data = new Intent();
        if (this.mEditorController != null) {
            uri = this.mEditorController.getSaveAsFileUri();
        }
        data.setData(uri);
        setStateResult(-1, data);
    }

    protected boolean onKeyDown(int keyCode, KeyEvent event) {
        return this.mStateManager.getCurrentState().onKeyDown(keyCode, event);
    }

    protected boolean onKeyUp(int keyCode, KeyEvent event) {
        return this.mStateManager.getCurrentState().onKeyUp(keyCode, event);
    }

    private void showDefaultFocusPoint() {
        if (!this.mShowFocusIndicator || this.mPendingShowFocusIndicator) {
            this.mPendingShowFocusIndicator = false;
            return;
        }
        this.mHandler.removeMessages(23);
        this.mHandler.sendEmptyMessageDelayed(23, 250);
    }

    private void enableAllFocusOperations(boolean enabled) {
        this.mStateManager.getCurrentState().enableOperations(enabled);
        enableSave(enabled);
    }

    public boolean isRangeMeasureMode() {
        return false;
    }

    public void refreshRangeMeasureView() {
    }

    public int getActionBarHeight() {
        return this.mActionBar.getActionBarHeight();
    }

    protected AbsPhotoView createPhotoView(GalleryContext context, GLRoot root) {
        return new EmptyPhotoView();
    }
}
