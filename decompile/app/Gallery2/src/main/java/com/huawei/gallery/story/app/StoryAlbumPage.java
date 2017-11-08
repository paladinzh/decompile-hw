package com.huawei.gallery.story.app;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MotionEvent;
import android.widget.EditText;
import com.android.gallery3d.R;
import com.android.gallery3d.app.Config$LocalCameraAlbumPage;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.DiscoverStoryAlbum;
import com.android.gallery3d.data.GallerySource.CodePath;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.ui.ActionDeleteAndConfirm;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.MenuExecutorFactory.Style;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MultiWindowStatusHolder;
import com.android.gallery3d.util.MultiWindowStatusHolder.IMultiWindowModeChangeListener;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.actionbar.AbstractTitleMode;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionBarStateBase;
import com.huawei.gallery.actionbar.ActionMode;
import com.huawei.gallery.actionbar.SelectionMode;
import com.huawei.gallery.app.AbsAlbumPage;
import com.huawei.gallery.app.AlbumDataLoader;
import com.huawei.gallery.app.CommonTimeBucketPage;
import com.huawei.gallery.app.TimeBucketItemsDataLoader;
import com.huawei.gallery.app.TimeBucketPage.LayoutSpec;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.story.ui.StoryListSlotRender;
import com.huawei.gallery.story.ui.StorySlideShowManager;
import com.huawei.gallery.story.ui.StoryVideoCoverRender;
import com.huawei.gallery.story.ui.StoryVideoCoverRender.Listener;
import com.huawei.gallery.story.utils.StoryAlbumFileDownLoader;
import com.huawei.gallery.story.utils.StoryAlbumFileDownLoader.FileDownloadListener;
import com.huawei.gallery.story.utils.StoryAlbumUtils;
import com.huawei.gallery.ui.ListSlotRender;
import com.huawei.gallery.util.ColorfulUtils;
import com.huawei.gallery.util.LayoutHelper;
import com.huawei.watermark.manager.parse.WMElement;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class StoryAlbumPage extends CommonTimeBucketPage implements Listener {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    private static FileDownloadListener fileDownloadListener = new FileDownloadListener() {
        public void onProgress(double percentage) {
        }

        public void onDownloadFinished() {
        }
    };
    private final int LIMIT_HEIGHT_LAND = GalleryUtils.dpToPixel(270);
    private final int LIMIT_HEIGHT_PORT = ((GalleryUtils.getWidthPixels() * 9) / 16);
    private final int MARGIN = GalleryUtils.dpToPixel(2);
    private ActionBarBackground mActionBarBg = new ActionBarBackground();
    private TimeBucketItemsDataLoader mAlbumDataAdapter = null;
    private volatile float mAlpha = WMElement.CAMERASIZEVALUE1B1;
    private Runnable mAlphaRunnable = new Runnable() {
        public void run() {
            if (StoryAlbumPage.this.isPort() && StoryAlbumPage.this.mIsActive) {
                StoryAlbumPage.this.setDoubleFaceAlpha();
            }
        }
    };
    private Config$LocalCameraAlbumPage mConfig;
    private StoryVideoCoverRender mCoverRender;
    private final Action[] mDefaultMenu = new Action[]{Action.STORY_RENAME, Action.STORY_ALBUM_REMOVE};
    private final Action[] mDefaultSelectMenu = new Action[]{Action.SHARE, Action.STORY_ITEM_REMOVE, Action.ALL};
    private ActionDeleteAndConfirm mDeleteDialog;
    private boolean mIsLayoutRtl;
    private LayoutSpec mLayoutSpec;
    private IMultiWindowModeChangeListener mMultiWindowModeChangeListener = new IMultiWindowModeChangeListener() {
        public void multiWindowModeChangeCallback(boolean isInMultiWindowMode) {
            StoryAlbumPage.this.updateStatus(StoryAlbumPage.this.mHost.getActivity().getResources().getConfiguration());
        }
    };
    private StorySlideShowManager mStorySlideShowManager;

    public class ActionBarBackground extends GLView {
        protected void render(GLCanvas canvas) {
            canvas.save();
            canvas.fillRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), StoryAlbumPage.this.mHost.getActivity().getColor(R.color.detail_title_color));
            canvas.fillRect(0.0f, (float) (getHeight() - StoryAlbumPage.this.MARGIN), (float) getWidth(), (float) StoryAlbumPage.this.MARGIN, StoryAlbumPage.this.mHost.getActivity().getColor(R.color.actionbar_background));
            canvas.restore();
        }
    }

    private static class ToggleSoftInputRunable implements Runnable {
        private EditText mText;

        ToggleSoftInputRunable(EditText text) {
            this.mText = text;
        }

        public void run() {
            PhotoShareUtils.showSoftInput(this.mText);
        }
    }

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
            iArr[Action.BACK.ordinal()] = 1;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Action.CANCEL_DETAIL.ordinal()] = 12;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Action.COLLAGE.ordinal()] = 13;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Action.COMMENT.ordinal()] = 14;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Action.COPY.ordinal()] = 15;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Action.DEALL.ordinal()] = 16;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Action.DEL.ordinal()] = 17;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Action.DETAIL.ordinal()] = 18;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[Action.DYNAMIC_ALBUM.ordinal()] = 19;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[Action.EDIT.ordinal()] = 20;
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
            iArr[Action.NO.ordinal()] = 2;
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
            iArr[Action.REMOVE.ordinal()] = 75;
        } catch (NoSuchFieldError e72) {
        }
        try {
            iArr[Action.RENAME.ordinal()] = 76;
        } catch (NoSuchFieldError e73) {
        }
        try {
            iArr[Action.RE_SEARCH.ordinal()] = 77;
        } catch (NoSuchFieldError e74) {
        }
        try {
            iArr[Action.ROTATE_LEFT.ordinal()] = 78;
        } catch (NoSuchFieldError e75) {
        }
        try {
            iArr[Action.ROTATE_RIGHT.ordinal()] = 79;
        } catch (NoSuchFieldError e76) {
        }
        try {
            iArr[Action.SAVE.ordinal()] = 80;
        } catch (NoSuchFieldError e77) {
        }
        try {
            iArr[Action.SAVE_BURST.ordinal()] = 81;
        } catch (NoSuchFieldError e78) {
        }
        try {
            iArr[Action.SEE_BARCODE_INFO.ordinal()] = 82;
        } catch (NoSuchFieldError e79) {
        }
        try {
            iArr[Action.SETAS.ordinal()] = 83;
        } catch (NoSuchFieldError e80) {
        }
        try {
            iArr[Action.SETAS_BOTH.ordinal()] = 84;
        } catch (NoSuchFieldError e81) {
        }
        try {
            iArr[Action.SETAS_FIXED.ordinal()] = 85;
        } catch (NoSuchFieldError e82) {
        }
        try {
            iArr[Action.SETAS_FIXED_ACTIVED.ordinal()] = 86;
        } catch (NoSuchFieldError e83) {
        }
        try {
            iArr[Action.SETAS_HOME.ordinal()] = 87;
        } catch (NoSuchFieldError e84) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE.ordinal()] = 88;
        } catch (NoSuchFieldError e85) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE_ACTIVED.ordinal()] = 89;
        } catch (NoSuchFieldError e86) {
        }
        try {
            iArr[Action.SETAS_UNLOCK.ordinal()] = 90;
        } catch (NoSuchFieldError e87) {
        }
        try {
            iArr[Action.SETTINGS.ordinal()] = 91;
        } catch (NoSuchFieldError e88) {
        }
        try {
            iArr[Action.SHARE.ordinal()] = 92;
        } catch (NoSuchFieldError e89) {
        }
        try {
            iArr[Action.SHOW_ON_MAP.ordinal()] = 93;
        } catch (NoSuchFieldError e90) {
        }
        try {
            iArr[Action.SINGLE_SELECTION.ordinal()] = 94;
        } catch (NoSuchFieldError e91) {
        }
        try {
            iArr[Action.SINGLE_SELECTION_ON.ordinal()] = 95;
        } catch (NoSuchFieldError e92) {
        }
        try {
            iArr[Action.SLIDESHOW.ordinal()] = 96;
        } catch (NoSuchFieldError e93) {
        }
        try {
            iArr[Action.STORY_ALBUM_REMOVE.ordinal()] = 3;
        } catch (NoSuchFieldError e94) {
        }
        try {
            iArr[Action.STORY_ITEM_REMOVE.ordinal()] = 4;
        } catch (NoSuchFieldError e95) {
        }
        try {
            iArr[Action.STORY_RENAME.ordinal()] = 5;
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

    private void downloadUnReadyFiles() {
        String code = getClusterCode();
        if (StoryAlbumUtils.queryAndDownloadUnReadyFiles(code, 3, this.mHost.getActivity().getContentResolver())) {
            StoryAlbumFileDownLoader.addDownloadListener(code, fileDownloadListener);
        }
    }

    private void finishDownloadListener() {
        StoryAlbumFileDownLoader.removeDownloadListener(getClusterCode());
    }

    protected String getMapPath() {
        return CodePath.GALLERY_ALBUM_SET_STORY.path + "/" + getClusterCode();
    }

    private String getClusterCode() {
        if (this.mMediaSet instanceof DiscoverStoryAlbum) {
            return ((DiscoverStoryAlbum) this.mMediaSet).getClusterCode();
        }
        return "";
    }

    protected void onCreate(Bundle data, Bundle storedState) {
        this.mConfig = Config$LocalCameraAlbumPage.get(this.mHost.getActivity());
        this.mLayoutSpec = this.mConfig.layoutSpec;
        super.onCreate(data, storedState);
        updateFlag();
        initViews();
        this.mIsLayoutRtl = GalleryUtils.isLayoutRTL();
        downloadUnReadyFiles();
    }

    private void updateFlag() {
        if (isPort()) {
            this.mFlags |= 64;
        } else {
            this.mFlags &= -65;
        }
    }

    protected boolean onCreateActionBar(Menu menu) {
        this.mHost.requestFeature(296);
        if (this.mSelectionManager.inSelectionMode()) {
            return true;
        }
        ActionMode am = this.mActionBar.enterActionMode(false);
        this.mMenu = this.mDefaultMenu;
        am.setLeftAction(Action.BACK);
        am.setRightAction(Action.NONE);
        am.setMenu(5, this.mDefaultMenu);
        am.setGravity(8388627);
        am.setTitle(this.mMediaSet.getName());
        am.setSupportDoubleFace(isPort());
        am.show();
        this.mHost.requestFeature(296);
        return true;
    }

    private void initViews() {
        this.mCoverRender = new StoryVideoCoverRender(this.mHost.getActivity(), this);
        this.mCoverRender.updateTitle(this.mMediaSet.getName(), this.mMediaSet.getSubName());
        this.mRootPane.addComponent(this.mSlotView);
        this.mRootPane.addComponent(this.mScrollBar);
        this.mStorySlideShowManager = new StorySlideShowManager(this.mRootPane, this.mHost, this.mMediaSet);
        this.mRootPane.addComponent(this.mActionBarBg);
    }

    protected Config$LocalCameraAlbumPage getConfig() {
        return this.mConfig;
    }

    protected ListSlotRender createListSlotRender(Config$LocalCameraAlbumPage config) {
        return new StoryListSlotRender(this.mHost.getGalleryContext(), this.mSlotView, this.mSelectionManager, config.placeholderColor);
    }

    protected void onGLRootRender(GLCanvas canvas) {
        if (this.mResumeEffect != null) {
            if (!this.mResumeEffect.draw(canvas)) {
                this.mResumeEffect = null;
                this.mSlotRender.setSlotFilter(null);
                onPhotoFallBackFinished();
            }
            this.mRootPane.invalidate();
        }
    }

    private boolean isPort() {
        return !LayoutHelper.isPort(this.mHost.getGalleryContext()) ? MultiWindowStatusHolder.isInMultiMaintained() : true;
    }

    protected void onGLRootLayout(int left, int top, int right, int bottom) {
        int navigationBarHeight = LayoutHelper.getNavigationBarHeight();
        if (isPort()) {
            this.mSlotView.setHeadCoverHeight(this.LIMIT_HEIGHT_PORT);
            this.mCoverRender.setTextLimitWidth((this.LIMIT_HEIGHT_PORT * 16) / 9);
            this.mActionBarBg.layout(0, 0, 0, 0);
            if (this.mIsLayoutRtl) {
                this.mSlotView.layout(this.mLayoutSpec.local_camera_page_right_padding, top, right - (this.mLayoutSpec.time_line_width + this.mLayoutSpec.local_camera_page_left_padding), bottom - navigationBarHeight);
                this.mScrollBar.layout(left, top, (right - this.mLayoutSpec.time_line_width) + (this.mLayoutSpec.local_camera_page_right_padding - this.mLayoutSpec.local_camera_page_left_padding), bottom - navigationBarHeight);
            } else {
                this.mSlotView.layout(this.mLayoutSpec.time_line_width + this.mLayoutSpec.local_camera_page_left_padding, top, right - this.mLayoutSpec.local_camera_page_right_padding, bottom - navigationBarHeight);
                this.mScrollBar.layout((this.mLayoutSpec.time_line_width + left) - (this.mLayoutSpec.local_camera_page_right_padding - this.mLayoutSpec.local_camera_page_left_padding), top, right, bottom - navigationBarHeight);
            }
        } else {
            this.mActionBarBg.layout(0, getStatusHeight() + top, right, (this.mActionBar.getActionBarHeight() + getStatusHeight()) + top);
            top += this.mActionBar.getActionBarHeight() + getStatusHeight();
            this.mSlotView.setHeadCoverHeight(this.LIMIT_HEIGHT_LAND);
            this.mCoverRender.setTextLimitWidth((this.LIMIT_HEIGHT_LAND * 16) / 9);
            if (this.mIsLayoutRtl) {
                this.mSlotView.layout(this.mLayoutSpec.local_camera_page_right_padding, top, (right - (this.mLayoutSpec.time_line_width + this.mLayoutSpec.local_camera_page_left_padding)) - navigationBarHeight, bottom);
                this.mScrollBar.layout(left, top, (right - this.mLayoutSpec.time_line_width) + (this.mLayoutSpec.local_camera_page_right_padding - this.mLayoutSpec.local_camera_page_left_padding), bottom);
            } else {
                this.mSlotView.layout(this.mLayoutSpec.time_line_width + this.mLayoutSpec.local_camera_page_left_padding, top, (right - this.mLayoutSpec.local_camera_page_right_padding) - navigationBarHeight, bottom);
                this.mScrollBar.layout((this.mLayoutSpec.time_line_width + left) - (this.mLayoutSpec.local_camera_page_right_padding - this.mLayoutSpec.local_camera_page_left_padding), top, right - navigationBarHeight, bottom);
            }
        }
        this.mCoverRender.invalidateTitle();
    }

    private void setDoubleFaceAlpha() {
        ActionBarStateBase actionBarStateBase = this.mActionBar.getCurrentMode();
        if (actionBarStateBase instanceof AbstractTitleMode) {
            AbstractTitleMode titleMode = (AbstractTitleMode) actionBarStateBase;
            if (isPort()) {
                titleMode.setDoubleFaceAlpha(this.mAlpha, WMElement.CAMERASIZEVALUE1B1 - this.mAlpha);
            } else {
                titleMode.setDoubleFaceAlpha(WMElement.CAMERASIZEVALUE1B1, 0.0f);
            }
        }
    }

    private void updateStatus(Configuration config) {
        if (this.mHost.getStateManager().getTopState() instanceof StoryAlbumPage) {
            ActionBarStateBase actionBarStateBase = this.mActionBar.getCurrentMode();
            if (actionBarStateBase instanceof AbstractTitleMode) {
                AbstractTitleMode titleMode = (AbstractTitleMode) actionBarStateBase;
                if (config.orientation != 2 || MultiWindowStatusHolder.isInMultiMaintained()) {
                    titleMode.setSupportDoubleFace(true);
                    setDoubleFaceAlpha();
                } else {
                    titleMode.setSupportDoubleFace(false);
                    titleMode.setDoubleFaceAlpha(WMElement.CAMERASIZEVALUE1B1, 0.0f);
                }
                titleMode.show();
            }
            updateFlag();
            this.mHost.requestFeature(296);
            setScreenFlags();
            this.mRootPane.requestLayout();
        }
    }

    protected void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        updateStatus(config);
    }

    public void renderHeadCover(GLCanvas canvas, int left, int top, int right, int bottom) {
        int centerX = (left + right) / 2;
        int width = Math.round(((((float) (bottom - top)) * 16.0f) / 9.0f) + 0.5f);
        if (right - left < width) {
            width = right - left;
        }
        left = centerX - (width / 2);
        right = centerX + (width / 2);
        if (bottom >= getOverflowMarginTop()) {
            this.mStorySlideShowManager.render(canvas, left, top, right, bottom);
            this.mCoverRender.render(canvas, left, top, right, bottom);
        }
        if (isPort()) {
            float alpha = Utils.clamp(WMElement.CAMERASIZEVALUE1B1 - (((float) (bottom - getOverflowMarginTop())) / ((float) (this.LIMIT_HEIGHT_PORT - getOverflowMarginTop()))), 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
            this.mAlpha = alpha;
            canvas.save();
            canvas.setAlpha(alpha);
            canvas.fillRect((float) left, 0.0f, (float) (right - left), (float) getOverflowMarginTop(), -1);
            canvas.fillRect((float) left, (float) (getOverflowMarginTop() - this.MARGIN), (float) (right - left), (float) this.MARGIN, this.mHost.getActivity().getColor(R.color.actionbar_background));
            canvas.restore();
            this.mHost.getActivity().runOnUiThread(this.mAlphaRunnable);
        }
    }

    private int getStatusHeight() {
        return LayoutHelper.getStatusBarHeight();
    }

    public int getOverflowMarginTop() {
        if (isPort()) {
            return this.mActionBar.getActionBarHeight() + getStatusHeight();
        }
        return 0;
    }

    protected void onStateResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 100:
                if (data != null) {
                    this.mFocusIndex = data.getIntExtra("return-index-hint", 0);
                    this.mSlotView.invalidate();
                    break;
                }
                return;
            case 102:
                if (data != null) {
                    this.mFocusIndex = data.getIntExtra("return-index-hint", 0);
                }
                this.mSelectionManager.setSelectionListener(this);
                enterSelectionMode();
                this.mSelectionManager.updateSelectMode(this.mGetContent);
                if (resultCode == -1) {
                    this.mSlotView.invalidate();
                    break;
                }
                break;
            case AbsAlbumPage.LAUNCH_QUIK_ACTIVITY /*400*/:
                String code = getClusterCode();
                if (resultCode != -1) {
                    if (resultCode == 0) {
                        QuikActivityLauncher.errorProcess(this.mHost.getActivity(), code, data);
                        break;
                    }
                }
                this.mHost.getActivity().getTheme().applyStyle(this.mHost.getActivity().getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null), true);
                QuikActivityLauncher.saveResultData(this.mHost.getActivity(), code, data);
                break;
                break;
            default:
                super.onStateResult(requestCode, resultCode, data);
                break;
        }
    }

    protected AlbumDataLoader onCreateDataLoader(MediaSet mediaSet) {
        if (this.mAlbumDataAdapter == null) {
            this.mAlbumDataAdapter = new TimeBucketItemsDataLoader(this.mHost.getGalleryContext(), mediaSet);
            setDataLoader(this.mAlbumDataAdapter);
        }
        return this.mAlbumDataAdapter;
    }

    protected void onLoadingFinished(boolean loadingFailed) {
        super.onLoadingFinished(loadingFailed);
        if (this.mDataLoader.size() == 0) {
            onRemoveAlbumReal();
        }
    }

    public void onResetMainView() {
    }

    protected void onResume() {
        super.onResume();
        updateFlag();
        this.mStorySlideShowManager.onResume();
        setDoubleFaceAlpha();
        MultiWindowStatusHolder.registerMultiWindowModeChangeListener(this.mMultiWindowModeChangeListener, false);
    }

    protected void onPause() {
        super.onPause();
        this.mStorySlideShowManager.onPause();
        finishDownloadListener();
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mRootPane.removeAllComponents();
    }

    protected boolean onItemSelected(Action action) {
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
            case 2:
                return onBackPressed();
            case 3:
                onRemoveAlbum();
                break;
            case 4:
                onRemoveItem();
                break;
            case 5:
                onRename();
                break;
            default:
                return super.onItemSelected(action);
        }
        return true;
    }

    private void rename(String name) {
        this.mMediaSet.rename(name);
        this.mCoverRender.updateTitle(this.mMediaSet.getName(), this.mMediaSet.getSubName());
        ActionBarStateBase am = this.mActionBar.getCurrentMode();
        if (am instanceof ActionMode) {
            ((ActionMode) am).setTitle(this.mMediaSet.getName());
        }
        this.mCoverRender.invalidateTitle();
    }

    public boolean onDeleteSlotAnimationEnd() {
        if (this.mSelectionManager.getTotalCount() == 0) {
            onRemoveAlbumReal();
        }
        return true;
    }

    private void onRemoveItem() {
        String confirmTitle;
        int selectedCount = this.mSelectionManager.getSelectedCount();
        int totalCount = this.mSelectionManager.getTotalCount();
        Activity activity = this.mHost.getActivity();
        if (selectedCount != totalCount || totalCount <= 1) {
            confirmTitle = activity.getResources().getQuantityString(R.plurals.story_album_remove_files, selectedCount, new Object[]{Integer.valueOf(selectedCount)});
        } else {
            confirmTitle = activity.getResources().getString(R.string.story_album_remove_all_files);
        }
        if (this.mDeleteDialog == null) {
            this.mDeleteDialog = new ActionDeleteAndConfirm(this.mHost.getActivity(), null, confirmTitle, R.string.move_out, R.string.cancel);
            this.mDeleteDialog.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    StoryAlbumPage.this.mDeleteDialog = null;
                }
            });
            this.mDeleteDialog.setOnClickListener(new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == -1) {
                        Bundle bundle = new Bundle();
                        String code = "";
                        if (StoryAlbumPage.this.mMediaSet instanceof DiscoverStoryAlbum) {
                            code = ((DiscoverStoryAlbum) StoryAlbumPage.this.mMediaSet).getClusterCode();
                        }
                        bundle.putString("keyValue", code);
                        StoryAlbumPage.this.mMenuExecutor.startAction(R.id.remove_from_story_album, R.string.action_remove_title, StoryAlbumPage.this.mSlotDeleteProgressListener, false, true, Style.NORMAL_STYLE, null, bundle, 0);
                        ReportToBigData.report(SmsCheckResult.ESCT_187);
                        if (StoryAlbumPage.this.mSelectionManager.inSelectAllMode()) {
                            StoryAlbumPage.this.mHandler.sendEmptyMessageDelayed(2, 100);
                        }
                    }
                    StoryAlbumPage.this.mRootPane.invalidate();
                }
            });
            this.mDeleteDialog.updateStatus(false, false);
            this.mDeleteDialog.show();
        } else if (this.mDeleteDialog.isShowing()) {
            this.mDeleteDialog.updateMessage(null, confirmTitle);
        } else {
            this.mDeleteDialog.updateStatus(false, false);
            this.mDeleteDialog.show(null, confirmTitle);
        }
    }

    private void onRemoveAlbumReal() {
        StoryAlbumUtils.removeStoryAlbum(getClusterCode(), this.mHost.getActivity().getContentResolver());
        ReportToBigData.report(SmsCheckResult.ESCT_186);
        this.mHost.getActivity().finish();
    }

    private void onRemoveAlbum() {
        ActionDeleteAndConfirm deleteDialog = new ActionDeleteAndConfirm(this.mHost.getActivity(), this.mHost.getActivity().getString(R.string.highlights_album_delete_album), this.mHost.getActivity().getString(R.string.highlights_album_delete_album_title));
        deleteDialog.setOnClickListener(new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    StoryAlbumPage.this.onRemoveAlbumReal();
                }
            }
        });
        deleteDialog.updateStatus(false, false);
        deleteDialog.show();
    }

    private void onRename() {
        ContextThemeWrapper context = GalleryUtils.getHwThemeContext(this.mHost.getActivity(), "androidhwext:style/Theme.Emui.Dialog");
        final EditText renameText = new EditText(context);
        renameText.setSingleLine(true);
        ColorfulUtils.decorateColorfulForEditText(context, renameText);
        GalleryUtils.createDialog(context, this.mMediaSet.getName(), R.string.rename, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                PhotoShareUtils.hideSoftInput(renameText);
                switch (which) {
                    case -2:
                        GalleryUtils.setDialogDismissable(dialog, true);
                        GalleryUtils.dismissDialogSafely(dialog, null);
                        StoryAlbumPage.this.mRootPane.invalidate();
                        return;
                    case -1:
                        String name = renameText.getText().toString().trim();
                        if (GalleryUtils.isFileNameValid(StoryAlbumPage.this.mHost.getActivity(), name)) {
                            StoryAlbumPage.this.rename(name);
                        }
                        GalleryUtils.setDialogDismissable(dialog, true);
                        GalleryUtils.dismissDialogSafely(dialog, null);
                        ReportToBigData.report(SmsCheckResult.ESCT_185);
                        return;
                    default:
                        return;
                }
            }
        }, null, renameText);
        this.mHandler.postDelayed(new ToggleSoftInputRunable(renameText), 300);
    }

    public void onClickVideoButton() {
        String code = getClusterCode();
        ReportToBigData.report(SmsCheckResult.ESCT_188);
        QuikActivityLauncher.launchQuikActivity(this.mHost.getActivity(), code);
    }

    protected boolean onBackPressed() {
        if (this.mSelectionManager.inSelectionMode()) {
            this.mSelectionManager.leaveSelectionMode();
            return true;
        }
        this.mHost.getActivity().finish();
        return true;
    }

    protected void enterSelectionMode() {
        SelectionMode sm = this.mActionBar.enterSelectionMode(true);
        sm.setLeftAction(Action.NO);
        sm.setTitle((int) R.string.has_selected);
        sm.setRightAction(Action.NONE);
        this.mMenu = this.mDefaultSelectMenu;
        sm.setMenu(Math.min(5, this.mMenu.length), this.mMenu);
        sm.setSupportDoubleFace(isPort());
        sm.show();
        setDoubleFaceAlpha();
        this.mHost.requestFeature(296);
        this.mRootPane.requestLayout();
    }

    protected void leaveSelectionMode() {
        this.mActionBar.leaveCurrentMode();
        ActionBarStateBase am = this.mActionBar.getCurrentMode();
        if (am instanceof ActionMode) {
            ((ActionMode) am).setTitle(this.mMediaSet.getName());
        }
        this.mRootPane.requestLayout();
    }

    public void onTouchUp(MotionEvent event) {
        super.onTouchUp(event);
        this.mCoverRender.onTouch(event);
    }

    protected boolean isSupportGlobelMap() {
        GalleryLog.d("StoryAlbumPage", "enter Map from StoryAlbum");
        return false;
    }
}
