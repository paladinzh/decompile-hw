package com.huawei.gallery.app;

import android.annotation.SuppressLint;
import android.app.AbsWallpaperManager.IBlurWallpaperCallback;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.app.IntentChooser;
import com.android.gallery3d.app.LoadingListener;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.CloudLocalAlbum;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.DiscoverLocation;
import com.android.gallery3d.data.DiscoverStoryAlbum;
import com.android.gallery3d.data.GalleryMediaTimegroupAlbum;
import com.android.gallery3d.data.GalleryRecycleAlbum;
import com.android.gallery3d.data.LocalScreenshotsAlbum;
import com.android.gallery3d.data.MediaDetails;
import com.android.gallery3d.data.MediaDetailsSetSizeTask;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.data.PhotoShareSource;
import com.android.gallery3d.settings.GallerySettings;
import com.android.gallery3d.ui.ActionDeleteAndConfirm;
import com.android.gallery3d.ui.ActionRecycleAndConfirm;
import com.android.gallery3d.ui.AlbumSlotScrollBarView;
import com.android.gallery3d.ui.DetailsHelper;
import com.android.gallery3d.ui.DetailsHelper.CloseListener;
import com.android.gallery3d.ui.DetailsHelper.DetailsSource;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.MenuExecutor;
import com.android.gallery3d.ui.MenuExecutor.SimpleSlotDeleteProgressListener;
import com.android.gallery3d.ui.MenuExecutor.SlotDeleteProgressListener;
import com.android.gallery3d.ui.MenuExecutorFactory.Style;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.SelectionManager.SelectionListener;
import com.android.gallery3d.ui.SlotPreviewPhotoManager;
import com.android.gallery3d.ui.SlotPreviewPhotoManager.SlotPreviewModeDelegate;
import com.android.gallery3d.ui.SlotScrollBarView;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.ui.WindowFlag;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.DrmUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReportToBigData;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionBarStateBase;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.actionbar.SelectionMode;
import com.huawei.gallery.anim.PhotoFallbackEffect;
import com.huawei.gallery.extfile.FyuseManager;
import com.huawei.gallery.map.app.MapAlbumPage;
import com.huawei.gallery.map.data.MapAlbum;
import com.huawei.gallery.photoshare.PhotoShareItem;
import com.huawei.gallery.photoshare.ui.ShareToCloudAlbumActivity;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.recycle.utils.RecycleUtils;
import com.huawei.gallery.share.HwCustUtilsWrapper;
import com.huawei.gallery.ui.ActionBarPlaceHolderView;
import com.huawei.gallery.ui.ActionModeHandler;
import com.huawei.gallery.ui.ActionModeHandler.ActionModeDelegate;
import com.huawei.gallery.ui.OpenAnimationProxyView;
import com.huawei.gallery.ui.SlotView;
import com.huawei.gallery.ui.SlotView.AbsLayout;
import com.huawei.gallery.ui.SlotView.SlotRenderInterface;
import com.huawei.gallery.util.ColorfulUtils;
import com.huawei.gallery.util.DeleteMsgUtil;
import com.huawei.gallery.util.DeleteMsgUtil.Delegate;
import com.huawei.gallery.util.SyncUtils;
import com.huawei.gallery.util.VideoEditorController;
import com.huawei.gallery.wallpaper.WallpaperConstant;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class AbsAlbumPage extends ActivityState implements SelectionListener, ActionModeDelegate, IBlurWallpaperCallback, SlotPreviewModeDelegate {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    static final Action[] COMMON_ALBUM_MENU = new Action[]{Action.SHARE, Action.MOVE, Action.DEL, Action.ALL, Action.NONE, Action.COPY, Action.PRINT, Action.DETAIL};
    protected static final int INVALID_REQUEST = -1;
    public static final String KEY_ACTION = "KEY_ACTION";
    public static final String KEY_AUTO_SELECT_ALL = "auto-select-all";
    public static final String KEY_PHOTO_PAGE_IS_EMPTY = "photo_page_is_empty";
    public static final String KEY_PHOTO_PAGE_MEDIA_SET_PATH = "photo_page_media_set_path";
    public static final String KEY_PROXY_VIEW = "key_proxy_view";
    public static final String KEY_RESUME_ANIMATION = "resume_animation";
    public static final String KEY_SELECTION_MANAGER = "key_selection_manager";
    public static final String KEY_VALUE = "KEY_VALUE";
    public static final int LAUNCH_QUIK_ACTIVITY = 400;
    protected static final int MSG_ADD_DOWNLOAD = 12;
    protected static final int MSG_CANCEL_RECEIVE = 10;
    protected static final int MSG_DELETE_FILE = 11;
    protected static final int MSG_DELETE_INSIDE_OUTSIDE_FILE = 2;
    protected static final int MSG_FINISH_SELF = 16;
    protected static final int MSG_HIDE_BARS = 20;
    protected static final int MSG_HIDE_STATUS_BAR = 15;
    protected static final int MSG_MOVE_TAG_FILE = 13;
    protected static final int MSG_PICK_PHOTO = 0;
    protected static final int MSG_PLAY_SLIDESHOW = 1;
    protected static final int REQUEST_CLOUD_PHOTO = 106;
    protected static final int REQUEST_CROP_WALLPAPER = 108;
    protected static final int REQUEST_EDIT_VIDEO = 200;
    protected static final int REQUEST_KEYGUARD_PHOTO = 104;
    protected static final int REQUEST_PASTE_TARGET_CHOOSED = 107;
    protected static final int REQUEST_PHOTO = 100;
    protected static final int REQUEST_PHOTOSHARE_GET_PHOTO = 120;
    protected static final int REQUEST_PHOTOSHARE_GET_TARGETALBUM = 121;
    protected static final int REQUEST_PREVIEW = 102;
    public static final int REQUEST_SHARE_PHOTO = 500;
    protected static final int REQUEST_SLIDESHOW = 103;
    protected static final int REQUSET_CAMERA_PHOTO = 109;
    public static final int REQUSET_PRESSURE = 1;
    private static final String TAG = "AbsAlbumPage";
    static final Action[] TIME_BUCKET_ALBUM_MENU = new Action[]{Action.SHARE, Action.DEL, Action.ALL, Action.NONE, Action.PRINT, Action.DETAIL};
    static final Action[] VIDEO_ALBUM_MENU = new Action[]{Action.SHARE, Action.MOVE, Action.DEL, Action.ALL, Action.NONE, Action.COPY, Action.DETAIL};
    protected GalleryActionBar mActionBar;
    protected ActionModeHandler mActionModeHandler;
    private List<Integer> mActionNotUpdate;
    protected int mCardLocation = -1;
    private AlertDialog mCreateDialog;
    private HwCustAbsAlbumPage mCust = ((HwCustAbsAlbumPage) HwCustUtilsWrapper.createObj(HwCustAbsAlbumPage.class, new Object[0]));
    protected AlbumDataLoader mDataLoader;
    protected int mDefaultMenuItemCount;
    private ActionDeleteAndConfirm mDeleteDialog;
    private DetailsHelper mDetailsHelper;
    protected int mFocusIndex = 0;
    protected boolean mGetContent;
    protected Handler mHandler;
    private boolean mHasSomeItems;
    protected IntentChooser mIntentChooser;
    protected boolean mIsActive = false;
    protected boolean mLoading = false;
    protected int mMaxSelectCount;
    protected MediaSet mMediaSet;
    protected Path mMediaSetPath;
    protected Action[] mMenu;
    protected boolean mMenuChangeable = false;
    protected MenuExecutor mMenuExecutor;
    protected PhotoShareItem mPhotoShareItem;
    private volatile boolean mRenderUntilWaitLoadFinish;
    protected PhotoFallbackEffect mResumeEffect;
    protected final GLView mRootPane = new GLView() {
        private final float[] mMatrix = new float[16];

        protected void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
            AbsAlbumPage.this.onGLRootLayout(left, top, right, bottom);
            GalleryUtils.setViewPointMatrix(this.mMatrix, ((float) (right - left)) / 2.0f, ((float) (bottom - top)) / 2.0f, -AbsAlbumPage.this.mUserDistance);
        }

        protected void render(GLCanvas canvas) {
            canvas.save(2);
            canvas.multiplyMatrix(this.mMatrix, 0);
            super.render(canvas);
            AbsAlbumPage.this.onGLRootRender(canvas);
            canvas.restore();
        }

        public Rect getAnimRect() {
            return AbsAlbumPage.this.getAnimSlotRect();
        }

        protected void renderChild(GLCanvas canvas, GLView component) {
            if (!AbsAlbumPage.this.mRenderUntilWaitLoadFinish) {
                super.renderChild(canvas, component);
            }
        }
    };
    protected SlotScrollBarView mScrollBar;
    protected int mSelectedCount;
    protected SelectionManager mSelectionManager;
    private boolean mShowDetails;
    protected SlotDeleteProgressListener mSlotDeleteProgressListener = new SimpleSlotDeleteProgressListener() {
        private AbsLayout layout;
        private String toastContent = null;
        private HashMap<Object, Object> visibleIndexMap;
        private HashMap<Path, Object> visiblePathMap;

        public void setOnCompleteToastContent(String content) {
            this.toastContent = content;
        }

        public void onDeleteStart() {
            AbsAlbumPage.this.mDataLoader.freeze();
            SlotView slotView = AbsAlbumPage.this.getSlotView();
            this.visiblePathMap = new HashMap();
            this.visibleIndexMap = new HashMap();
            slotView.prepareVisibleRangeItemIndex(this.visiblePathMap, this.visibleIndexMap);
            this.layout = slotView.cloneLayout();
        }

        public void onProgressComplete(int result) {
            AbsAlbumPage.this.onDeleteProgressComplete(this.visiblePathMap, this.visibleIndexMap, this.layout, result, this.toastContent);
        }
    };
    protected SlotPreviewPhotoManager mSlotPreviewPhotoManager;
    protected boolean mSupportMultiPick = false;
    private final float mUserDistance = ((float) GalleryUtils.meterToPixel(0.3f));
    private int mVirtualFlags = 0;
    private WindowFlag mWindowFlag;

    private class DataLoaderListener implements LoadingListener {
        private DataLoaderListener() {
        }

        public void onLoadingStarted() {
            AbsAlbumPage.this.onLoadingStarted();
        }

        public void onLoadingFinished(boolean loadingFailed) {
            AbsAlbumPage.this.onLoadingFinished(loadingFailed);
        }

        public void onVisibleRangeLoadFinished() {
            AbsAlbumPage.this.onVisibleRangeLoadFinished();
        }
    }

    private class MyDetailsSource implements DetailsSource {
        private Path mPath;
        private int mSelectedCount;

        private MyDetailsSource() {
        }

        public int setIndex() {
            this.mSelectedCount = AbsAlbumPage.this.mSelectionManager.getSelectedCount();
            if (this.mSelectedCount > 1) {
                return 0;
            }
            ArrayList<Path> selectedList = AbsAlbumPage.this.mSelectionManager.getSelected(false);
            if (selectedList.isEmpty()) {
                this.mPath = null;
                return -1;
            }
            this.mPath = (Path) selectedList.get(0);
            return AbsAlbumPage.this.mDataLoader.size();
        }

        public MediaDetails getDetails() {
            MediaDetails mediaDetails = null;
            if (this.mSelectedCount > 1) {
                return getMultiSelectedDetails(this.mSelectedCount);
            }
            if (this.mPath == null) {
                return null;
            }
            MediaObject mo = AbsAlbumPage.this.mHost.getGalleryContext().getDataManager().getMediaObject(this.mPath);
            if (mo != null) {
                mediaDetails = mo.getDetails();
            }
            return mediaDetails;
        }

        private MediaDetails getMultiSelectedDetails(int count) {
            MediaDetails details = new MediaDetails();
            details.addDetail(65535, new MediaDetailsSetSizeTask(AbsAlbumPage.this.mHost.getGalleryContext(), AbsAlbumPage.this.mSelectionManager));
            details.addDetail(150, Integer.valueOf(count));
            return details;
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
            iArr[Action.ADD.ordinal()] = 20;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Action.ADD_ALBUM.ordinal()] = 21;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Action.ADD_COMMENT.ordinal()] = 22;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Action.AIRSHARE.ordinal()] = 23;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Action.ALBUM.ordinal()] = 24;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Action.ALL.ordinal()] = 1;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Action.BACK.ordinal()] = 25;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Action.CANCEL_DETAIL.ordinal()] = 26;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Action.COLLAGE.ordinal()] = 27;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Action.COMMENT.ordinal()] = 28;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Action.COPY.ordinal()] = 2;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Action.DEALL.ordinal()] = 3;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Action.DEL.ordinal()] = 4;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Action.DETAIL.ordinal()] = 5;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[Action.DYNAMIC_ALBUM.ordinal()] = 29;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[Action.EDIT.ordinal()] = 6;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[Action.EDIT_COMMENT.ordinal()] = 30;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[Action.GOTO_GALLERY.ordinal()] = 31;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[Action.HIDE.ordinal()] = 32;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[Action.INFO.ordinal()] = 33;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[Action.KEYGUARD_LIKE.ordinal()] = 34;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[Action.KEYGUARD_NOT_LIKE.ordinal()] = 35;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[Action.LOOPPLAY.ordinal()] = 36;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[Action.MAP.ordinal()] = 37;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[Action.MENU.ordinal()] = 38;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[Action.MORE_EDIT.ordinal()] = 7;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[Action.MOVE.ordinal()] = 8;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[Action.MOVEIN.ordinal()] = 39;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[Action.MOVEOUT.ordinal()] = 40;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[Action.MULTISCREEN.ordinal()] = 41;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[Action.MULTISCREEN_ACTIVITED.ordinal()] = 42;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[Action.MULTI_SELECTION.ordinal()] = 43;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[Action.MULTI_SELECTION_ON.ordinal()] = 44;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[Action.MYFAVORITE.ordinal()] = 45;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[Action.NO.ordinal()] = 9;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[Action.NONE.ordinal()] = 46;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[Action.NOT_MYFAVORITE.ordinal()] = 47;
        } catch (NoSuchFieldError e37) {
        }
        try {
            iArr[Action.OK.ordinal()] = 48;
        } catch (NoSuchFieldError e38) {
        }
        try {
            iArr[Action.PHOTOSHARE_ACCOUNT.ordinal()] = 49;
        } catch (NoSuchFieldError e39) {
        }
        try {
            iArr[Action.PHOTOSHARE_ADDPICTURE.ordinal()] = 50;
        } catch (NoSuchFieldError e40) {
        }
        try {
            iArr[Action.PHOTOSHARE_BACKUP.ordinal()] = 10;
        } catch (NoSuchFieldError e41) {
        }
        try {
            iArr[Action.PHOTOSHARE_CANCEL_RECEIVE.ordinal()] = 51;
        } catch (NoSuchFieldError e42) {
        }
        try {
            iArr[Action.PHOTOSHARE_CLEAR.ordinal()] = 52;
        } catch (NoSuchFieldError e43) {
        }
        try {
            iArr[Action.PHOTOSHARE_COMBINE.ordinal()] = 53;
        } catch (NoSuchFieldError e44) {
        }
        try {
            iArr[Action.PHOTOSHARE_CONTACT.ordinal()] = 54;
        } catch (NoSuchFieldError e45) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_PEOPLE_TAG.ordinal()] = 55;
        } catch (NoSuchFieldError e46) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_SHARE.ordinal()] = 56;
        } catch (NoSuchFieldError e47) {
        }
        try {
            iArr[Action.PHOTOSHARE_DELETE.ordinal()] = 57;
        } catch (NoSuchFieldError e48) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD.ordinal()] = 58;
        } catch (NoSuchFieldError e49) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOADING.ordinal()] = 59;
        } catch (NoSuchFieldError e50) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD_START.ordinal()] = 60;
        } catch (NoSuchFieldError e51) {
        }
        try {
            iArr[Action.PHOTOSHARE_EDITSHARE.ordinal()] = 61;
        } catch (NoSuchFieldError e52) {
        }
        try {
            iArr[Action.PHOTOSHARE_EMAIL.ordinal()] = 62;
        } catch (NoSuchFieldError e53) {
        }
        try {
            iArr[Action.PHOTOSHARE_LINK.ordinal()] = 63;
        } catch (NoSuchFieldError e54) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_DOWNLOAD.ordinal()] = 64;
        } catch (NoSuchFieldError e55) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_UPLOAD.ordinal()] = 65;
        } catch (NoSuchFieldError e56) {
        }
        try {
            iArr[Action.PHOTOSHARE_MESSAGE.ordinal()] = 66;
        } catch (NoSuchFieldError e57) {
        }
        try {
            iArr[Action.PHOTOSHARE_MOVE.ordinal()] = 67;
        } catch (NoSuchFieldError e58) {
        }
        try {
            iArr[Action.PHOTOSHARE_MULTI_DOWNLOAD.ordinal()] = 68;
        } catch (NoSuchFieldError e59) {
        }
        try {
            iArr[Action.PHOTOSHARE_NOT_THIS_PERSON.ordinal()] = 69;
        } catch (NoSuchFieldError e60) {
        }
        try {
            iArr[Action.PHOTOSHARE_PAUSE.ordinal()] = 70;
        } catch (NoSuchFieldError e61) {
        }
        try {
            iArr[Action.PHOTOSHARE_REMOVE_PEOPLE_TAG.ordinal()] = 71;
        } catch (NoSuchFieldError e62) {
        }
        try {
            iArr[Action.PHOTOSHARE_RENAME.ordinal()] = 72;
        } catch (NoSuchFieldError e63) {
        }
        try {
            iArr[Action.PHOTOSHARE_SETTINGS.ordinal()] = 73;
        } catch (NoSuchFieldError e64) {
        }
        try {
            iArr[Action.PHOTOSHARE_UPLOAD_START.ordinal()] = 74;
        } catch (NoSuchFieldError e65) {
        }
        try {
            iArr[Action.PRINT.ordinal()] = 11;
        } catch (NoSuchFieldError e66) {
        }
        try {
            iArr[Action.RANGE_MEASURE.ordinal()] = 75;
        } catch (NoSuchFieldError e67) {
        }
        try {
            iArr[Action.RECYCLE_CLEAN_BIN.ordinal()] = 76;
        } catch (NoSuchFieldError e68) {
        }
        try {
            iArr[Action.RECYCLE_DELETE.ordinal()] = 77;
        } catch (NoSuchFieldError e69) {
        }
        try {
            iArr[Action.RECYCLE_RECOVERY.ordinal()] = 78;
        } catch (NoSuchFieldError e70) {
        }
        try {
            iArr[Action.REDO.ordinal()] = 79;
        } catch (NoSuchFieldError e71) {
        }
        try {
            iArr[Action.REMOVE.ordinal()] = 80;
        } catch (NoSuchFieldError e72) {
        }
        try {
            iArr[Action.RENAME.ordinal()] = 12;
        } catch (NoSuchFieldError e73) {
        }
        try {
            iArr[Action.RE_SEARCH.ordinal()] = 81;
        } catch (NoSuchFieldError e74) {
        }
        try {
            iArr[Action.ROTATE_LEFT.ordinal()] = 13;
        } catch (NoSuchFieldError e75) {
        }
        try {
            iArr[Action.ROTATE_RIGHT.ordinal()] = 14;
        } catch (NoSuchFieldError e76) {
        }
        try {
            iArr[Action.SAVE.ordinal()] = 82;
        } catch (NoSuchFieldError e77) {
        }
        try {
            iArr[Action.SAVE_BURST.ordinal()] = 83;
        } catch (NoSuchFieldError e78) {
        }
        try {
            iArr[Action.SEE_BARCODE_INFO.ordinal()] = 84;
        } catch (NoSuchFieldError e79) {
        }
        try {
            iArr[Action.SETAS.ordinal()] = 15;
        } catch (NoSuchFieldError e80) {
        }
        try {
            iArr[Action.SETAS_BOTH.ordinal()] = 85;
        } catch (NoSuchFieldError e81) {
        }
        try {
            iArr[Action.SETAS_FIXED.ordinal()] = 86;
        } catch (NoSuchFieldError e82) {
        }
        try {
            iArr[Action.SETAS_FIXED_ACTIVED.ordinal()] = 87;
        } catch (NoSuchFieldError e83) {
        }
        try {
            iArr[Action.SETAS_HOME.ordinal()] = 88;
        } catch (NoSuchFieldError e84) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE.ordinal()] = 89;
        } catch (NoSuchFieldError e85) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE_ACTIVED.ordinal()] = 90;
        } catch (NoSuchFieldError e86) {
        }
        try {
            iArr[Action.SETAS_UNLOCK.ordinal()] = 91;
        } catch (NoSuchFieldError e87) {
        }
        try {
            iArr[Action.SETTINGS.ordinal()] = 16;
        } catch (NoSuchFieldError e88) {
        }
        try {
            iArr[Action.SHARE.ordinal()] = 17;
        } catch (NoSuchFieldError e89) {
        }
        try {
            iArr[Action.SHOW_ON_MAP.ordinal()] = 18;
        } catch (NoSuchFieldError e90) {
        }
        try {
            iArr[Action.SINGLE_SELECTION.ordinal()] = 92;
        } catch (NoSuchFieldError e91) {
        }
        try {
            iArr[Action.SINGLE_SELECTION_ON.ordinal()] = 93;
        } catch (NoSuchFieldError e92) {
        }
        try {
            iArr[Action.SLIDESHOW.ordinal()] = 19;
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

    protected abstract Rect getAnimSlotRect();

    protected abstract SlotRenderInterface getSlotRenderInterface();

    protected abstract SlotView getSlotView();

    protected abstract AlbumDataLoader onCreateDataLoader(MediaSet mediaSet);

    protected abstract void onGLRootLayout(int i, int i2, int i3, int i4);

    protected void onGLRootRender(GLCanvas canvas) {
    }

    protected void onPhotoFallback() {
    }

    protected void showEmptyAlbum() {
    }

    protected void hideEmptyAlbum() {
    }

    protected void leaveSelectionMode() {
    }

    protected void updateMenu(boolean isSizeZero) {
    }

    protected void hideLoadingTips() {
    }

    protected int getBackgroundColor(Context context) {
        return context.getResources().getColor(R.color.album_background);
    }

    protected boolean needLazyLoad() {
        return false;
    }

    protected void onPhotoFallBackFinished() {
        Activity activity = this.mHost.getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    AbsAlbumPage.this.mHost.getGalleryActionBar().setShoudTransition(true);
                }
            });
        }
    }

    protected void onCreate(Bundle data, Bundle storedState) {
        super.onCreate(data, storedState);
        this.mActionBar = this.mHost.getGalleryActionBar();
        this.mHandler = new SynchronizedHandler(this.mHost.getGLRoot()) {
            public void handleMessage(Message msg) {
                AbsAlbumPage.this.onHandleMessage(msg);
            }
        };
        initMenuItemCount();
        TraceController.beginSection("initialize");
        initialize(data);
        TraceController.endSection();
        setBlurWallpaperChanged();
    }

    protected void initMenuItemCount() {
        this.mDefaultMenuItemCount = 5;
    }

    protected void onHandleMessage(Message message) {
        switch (message.what) {
            case 0:
                TraceController.traceBegin("AbsAlbumPage MSG_PICK_PHOTO");
                pickPhoto(message.arg1, message.arg2);
                TraceController.traceEnd();
                return;
            case 1:
                startSlideShow();
                return;
            case 2:
                deleteExtraFiles(this.mHost.getActivity());
                return;
            case 16:
                this.mHost.getStateManager().finishState(this);
                return;
            default:
                throw new AssertionError(message.what);
        }
    }

    protected void onInflateMenu(Menu menu) {
        throw new UnsupportedOperationException("need implement!");
    }

    protected Bundle getBundleForPhoto(int slotIndex, MediaItem item) {
        Bundle data = new Bundle();
        data.putInt("index-hint", slotIndex);
        data.putParcelable("open-animation-rect", getAnimSlotRect());
        data.putString("media-set-path", this.mMediaSetPath.toString());
        data.putString("media-item-path", item.getPath().toString());
        data.putInt("media-count", this.mDataLoader.size());
        return data;
    }

    protected boolean forbidPickPhoto() {
        return false;
    }

    protected void onClickSlotAnimationStart() {
    }

    protected void copyActionBarToGL(ActionBarPlaceHolderView actionBarPlaceHolderView) {
        Activity activity = this.mHost.getActivity();
        if (activity != null) {
            View headActionBar = activity.getWindow().getDecorView().findViewById(16909290);
            if (headActionBar != null) {
                long start = System.currentTimeMillis();
                headActionBar.setDrawingCacheEnabled(true);
                Bitmap actionbar = headActionBar.getDrawingCache();
                Bitmap bitmap = null;
                if (actionbar != null) {
                    bitmap = Bitmap.createBitmap(actionbar);
                }
                headActionBar.setDrawingCacheEnabled(false);
                actionBarPlaceHolderView.setContent(bitmap);
                GalleryLog.d(TAG, "copyActionBarToGL:" + (System.currentTimeMillis() - start));
            }
        }
    }

    protected boolean needSplitActionBarHide() {
        return true;
    }

    @SuppressWarnings({"SIC_INNER_SHOULD_BE_STATIC_ANON"})
    protected void pickPhotoWithAnimation(final SlotView slotView, final int request, Object index, final int absIndex, MediaItem item) {
        if (request == -1) {
            slotView.startClickSlotAnimation(index, null);
        } else if (!needAnimationWhenOpenPhoto(absIndex, request)) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(0, absIndex, request));
        } else if (this.mHost.getTransitionStore().get(KEY_PROXY_VIEW, null) != null) {
            GalleryLog.d(TAG, "there is click slot animation already, ignore this click.");
        } else {
            slotView.startClickSlotAnimation(index, new Runnable() {
                public void run() {
                    AbsAlbumPage.this.mHandler.sendMessage(AbsAlbumPage.this.mHandler.obtainMessage(0, absIndex, request));
                }
            });
            onClickSlotAnimationStart();
            this.mActionBar.hideHeadActionContainer();
            this.mActionBar.hideFootActionContainer();
            OpenAnimationProxyView openAnimationProxyView = new OpenAnimationProxyView(this.mHost.getActivity(), this.mRootPane.getWidth(), this.mRootPane.getHeight(), new Runnable() {
                public void run() {
                    slotView.clearDisabledClickSlotAnimation();
                }
            });
            openAnimationProxyView.updateProxyTexture(item);
            this.mHost.getTransitionStore().put(KEY_PROXY_VIEW, openAnimationProxyView);
            this.mWindowFlag = new WindowFlag(this.mHost.getActivity().getWindow());
        }
    }

    private void pickPhoto(int slotIndex, int type) {
        boolean hasOpenPicture = false;
        try {
            if (this.mIsActive) {
                MediaItem item = this.mDataLoader.get(slotIndex);
                if (item == null) {
                    clearOpenAnimationParameter(false);
                    return;
                }
                Bundle data = getBundleForPhoto(slotIndex, item);
                switch (type) {
                    case 100:
                        if (!forbidPickPhoto()) {
                            if (!this.mGetContent) {
                                if (acquireDrmRight(item)) {
                                    hasOpenPicture = true;
                                    requestPhoto(data);
                                    reportForEnterPhotoView();
                                    break;
                                }
                            }
                            onGetContent(item);
                            break;
                        }
                        clearOpenAnimationParameter(false);
                        return;
                        break;
                    case 102:
                        hasOpenPicture = true;
                        data.putBoolean("key-multi-choose", this.mSelectionManager.inSingleMode());
                        data.putBoolean("get-content", this.mGetContent);
                        data.putInt("max-select-count", this.mMaxSelectCount);
                        data.putInt("selected-count", this.mSelectedCount);
                        data.putBoolean("support-multipick-items", this.mSupportMultiPick);
                        this.mHost.getTransitionStore().put(KEY_SELECTION_MANAGER, this.mSelectionManager);
                        this.mHost.getStateManager().startStateForResult(SelectionPreview.class, 102, data);
                        break;
                    case REQUEST_KEYGUARD_PHOTO /*104*/:
                        hasOpenPicture = true;
                        this.mHost.getStateManager().startStateForResult(KeyguardPhotoPage.class, REQUEST_KEYGUARD_PHOTO, data);
                        break;
                    case REQUEST_CLOUD_PHOTO /*106*/:
                        hasOpenPicture = true;
                        requestCloudPhoto(data);
                        break;
                    case REQUSET_CAMERA_PHOTO /*109*/:
                        if (acquireDrmRight(item)) {
                            hasOpenPicture = true;
                            this.mHost.getStateManager().startStateForResult(PhotoPage.class, 100, data);
                            ReportToBigData.report(36, String.format("{EnterPhotoView:%s}", new Object[]{"FromQuickCameraView"}));
                            break;
                        }
                        break;
                }
                clearOpenAnimationParameter(hasOpenPicture);
            }
        } finally {
            clearOpenAnimationParameter(false);
        }
    }

    private void requestCloudPhoto(Bundle data) {
        boolean z = true;
        if ((this.mMediaSet instanceof CloudLocalAlbum) || (this.mMediaSet instanceof DiscoverLocation) || (this.mMediaSet instanceof DiscoverStoryAlbum) || (this.mMediaSet instanceof GalleryRecycleAlbum)) {
            this.mHost.getStateManager().startStateForResult(GalleryMediaPhotoPage.class, REQUEST_CLOUD_PHOTO, data);
        } else {
            this.mHost.getStateManager().startStateForResult(PhotoSharePhotoPage.class, REQUEST_CLOUD_PHOTO, data);
        }
        String str = "{IsFamilyShare:%s}";
        Object[] objArr = new Object[1];
        if (this.mMediaSet.getAlbumType() != 7) {
            z = false;
        }
        objArr[0] = Boolean.valueOf(z);
        ReportToBigData.report(92, String.format(str, objArr));
    }

    private void requestPhoto(Bundle data) {
        if ((this.mMediaSet instanceof GalleryMediaTimegroupAlbum) || (this.mMediaSet instanceof MapAlbum) || (this.mMediaSet instanceof GalleryRecycleAlbum)) {
            this.mHost.getStateManager().startStateForResult(GalleryMediaPhotoPage.class, 100, data);
        } else {
            this.mHost.getStateManager().startStateForResult(PhotoPage.class, 100, data);
        }
    }

    private boolean canOpenItemWhateverDrm(MediaItem item) {
        if (item == null) {
            return false;
        }
        return item.isDrm() ? item.hasRight() : true;
    }

    private boolean needAnimationWhenOpenPhoto(int slotIndex, int type) {
        boolean z = false;
        if (!this.mIsActive) {
            return false;
        }
        MediaItem item = this.mDataLoader.get(slotIndex);
        if (item == null) {
            return false;
        }
        switch (type) {
            case 100:
                if (!(forbidPickPhoto() || this.mGetContent)) {
                    z = canOpenItemWhateverDrm(item);
                }
                return z;
            case 102:
            case REQUEST_KEYGUARD_PHOTO /*104*/:
            case REQUEST_CLOUD_PHOTO /*106*/:
                return true;
            case REQUSET_CAMERA_PHOTO /*109*/:
                return canOpenItemWhateverDrm(item);
            default:
                return false;
        }
    }

    protected void onClearOpenAnimation() {
    }

    protected void clearOpenAnimationParameter(boolean hasOpenPicture) {
        this.mActionBar.resetHeadAndFootActionContainer();
        onClearOpenAnimation();
        if (!hasOpenPicture) {
            if (this.mWindowFlag != null) {
                this.mWindowFlag.reset(this.mHost.getActivity().getWindow());
                this.mWindowFlag = null;
            }
            GalleryLog.d(TAG, "we have not open picture, so need clear openAnimationProxyView");
            OpenAnimationProxyView openAnimationProxyView = (OpenAnimationProxyView) this.mHost.getTransitionStore().get(KEY_PROXY_VIEW, null);
            if (openAnimationProxyView != null) {
                this.mHost.getTransitionStore().put(KEY_PROXY_VIEW, null);
                openAnimationProxyView.clear();
            }
        }
    }

    private void reportForEnterPhotoView() {
        String fromWhich = "";
        if (getClass() == MapAlbumPage.class) {
            fromWhich = "FromMapSlotView";
        } else if (getClass() == TimeBucketPage.class) {
            fromWhich = "FromTimeView";
        } else {
            fromWhich = "FromNormalSlotView";
        }
        ReportToBigData.report(36, String.format("{EnterPhotoView:%s}", new Object[]{fromWhich}));
    }

    protected void startSlideShow() {
        if (this.mCust == null || !this.mCust.handleCustStartSlideShow(this.mHost)) {
            Bundle data = new Bundle();
            data.putString("media-set-path", this.mMediaSet.getPath().toString());
            data.putBoolean("repeat", true);
            MediaItem previewItem = this.mDataLoader.get(0);
            if (previewItem != null) {
                data.putString("media-preview-item-path", previewItem.getPath().toString());
                data.putInt("media-preview-item-index", 0);
            }
            this.mHost.getStateManager().startStateForResult(SlideShowPage.class, 103, data);
        }
    }

    private void initialize(Bundle data) {
        ConditionVariable lock = SyncUtils.runWithConditionVariable(new Runnable() {
            public void run() {
                AbsAlbumPage.this.initializeView();
            }
        });
        initializeData(data);
        lock.block();
    }

    private void initializeView() {
        this.mScrollBar = createSlotScrollBarView();
        this.mScrollBar.setGLRoot(this.mHost.getGLRoot());
    }

    protected SlotScrollBarView createSlotScrollBarView() {
        return new AlbumSlotScrollBarView(this.mHost.getGalleryContext(), R.drawable.bg_scrollbar, R.drawable.bg_quick_scrollbar_gallery);
    }

    protected int[] getNotUpdateActions() {
        return new int[]{Action.ACTION_ID_PRINT};
    }

    private void initializeData(Bundle data) {
        TraceController.beginSection("getMediaset");
        this.mMediaSetPath = Path.fromString(data.getString("media-path"));
        this.mMediaSet = this.mHost.getGalleryContext().getDataManager().getMediaSet(this.mMediaSetPath);
        TraceController.endSection();
        if (this.mMediaSet == null) {
            Utils.fail("MediaSet is null. Path = %s", this.mMediaSetPath);
        }
        ConditionVariable lock = SyncUtils.runWithConditionVariable(new Runnable() {
            public void run() {
                TraceController.beginSection("create DataLoader");
                AbsAlbumPage.this.mDataLoader = AbsAlbumPage.this.onCreateDataLoader(AbsAlbumPage.this.mMediaSet);
                AbsAlbumPage.this.mDataLoader.setLoadingListener(new DataLoaderListener());
                TraceController.endSection();
            }
        });
        this.mActionNotUpdate = new ArrayList();
        for (int action : getNotUpdateActions()) {
            this.mActionNotUpdate.add(Integer.valueOf(action));
        }
        this.mGetContent = data.getBoolean("get-content", false);
        this.mMaxSelectCount = data.getInt("max-select-count", -1);
        this.mSupportMultiPick = data.getBoolean("support-multipick-items", false);
        GalleryContext context = this.mHost.getGalleryContext();
        this.mSelectionManager = createSelectionManager(context);
        this.mSelectionManager.setAutoLeaveSelectionMode(false);
        Path noPrePath = PhotoShareSource.convertToNoPreView(this.mMediaSetPath);
        if (noPrePath.equalsIgnoreCase(this.mMediaSet)) {
            this.mSelectionManager.setSourceMediaSet(this.mMediaSet);
        } else {
            this.mSelectionManager.setSourceMediaSet(this.mHost.getGalleryContext().getDataManager().getMediaSet(noPrePath));
        }
        this.mSelectionManager.setSelectionListener(this);
        this.mIntentChooser = new IntentChooser(this.mHost.getActivity());
        this.mPhotoShareItem = new PhotoShareItem(this.mHost.getActivity());
        this.mMenuExecutor = new MenuExecutor(context, this.mSelectionManager, this.mIntentChooser, this.mHost.getGLRoot());
        this.mActionModeHandler = new ActionModeHandler(this.mHost.getGalleryContext(), this.mSelectionManager);
        this.mActionModeHandler.setActionModeDelegate(this);
        lock.block();
    }

    protected SelectionManager createSelectionManager(GalleryContext context) {
        return new SelectionManager(context, false);
    }

    protected void onResume() {
        boolean z = false;
        super.onResume();
        this.mWindowFlag = null;
        this.mIsActive = true;
        PhotoFallbackEffect effect = (PhotoFallbackEffect) this.mHost.getTransitionStore().get(KEY_RESUME_ANIMATION);
        if (effect != null) {
            this.mResumeEffect = effect;
            onPhotoFallback();
        }
        setContentPane(this.mRootPane);
        if (!needLazyLoad()) {
            this.mDataLoader.resume();
        }
        this.mActionModeHandler.resume();
        this.mIntentChooser.resume();
        Activity activity = this.mHost.getActivity();
        if (activity != null) {
            activity.getWindow().getDecorView().setTag("TryForcedCloseAnimation");
        }
        Path path = (Path) this.mHost.getTransitionStore().get(KEY_PHOTO_PAGE_MEDIA_SET_PATH);
        Boolean isPhotoPageEmpty = (Boolean) this.mHost.getTransitionStore().get(KEY_PHOTO_PAGE_IS_EMPTY);
        Path noPrePath = PhotoShareSource.convertToNoPreView(this.mMediaSet.getPath());
        if (!(isPhotoPageEmpty == null || !isPhotoPageEmpty.booleanValue() || path == null)) {
            z = path.equalsIgnoreCase(noPrePath.toString());
        }
        this.mRenderUntilWaitLoadFinish = z;
    }

    protected boolean onItemSelected(Action action) {
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
            case 3:
                this.mSelectionManager.modifyAllSelection();
                break;
            case 2:
                onPaste(1);
                break;
            case 4:
                onDelete();
                break;
            case 5:
                onDetails();
                break;
            case 6:
                onEdit();
                break;
            case 7:
            case 13:
            case 14:
            case 15:
            case 18:
                this.mMenuExecutor.onMenuClicked(action, null, null, null);
                break;
            case 8:
                onPaste(2);
                break;
            case 9:
                this.mSelectionManager.leaveSelectionMode();
                if (this.mGetContent) {
                    this.mHost.getStateManager().finishState(this);
                    break;
                }
                break;
            case 10:
                onPhotoShareBackUp();
                break;
            case 11:
                onPrint();
                break;
            case 12:
                onRename();
                break;
            case 16:
                this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        AbsAlbumPage.this.mHost.getActivity().startActivity(new Intent(AbsAlbumPage.this.mHost.getActivity(), GallerySettings.class));
                    }
                }, 150);
                break;
            case 17:
                onShare();
                break;
            case 19:
                this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        AbsAlbumPage.this.startSlideShow();
                    }
                }, 150);
                break;
            default:
                return super.onItemSelected(action);
        }
        return true;
    }

    protected boolean onBackPressed() {
        if (this.mShowDetails) {
            hideDetails();
            return true;
        } else if (!this.mSelectionManager.inSelectionMode() || this.mGetContent) {
            if (getClass() == TimeBucketPage.class) {
                ReportToBigData.report(37, String.format("{ExitGalleryView:%s}", new Object[]{"FromTimeView"}));
            }
            return super.onBackPressed();
        } else {
            this.mSelectionManager.leaveSelectionMode();
            return true;
        }
    }

    protected void onPause() {
        super.onPause();
        this.mIsActive = false;
        this.mIntentChooser.hideIfShowing();
        if (this.mDeleteDialog != null) {
            this.mDeleteDialog.dismiss();
        }
        if (this.mShowDetails) {
            hideDetails();
        }
        if (!(this.mActionBar == null || this.mActionBar.getCurrentMode() == null)) {
            this.mActionBar.getCurrentMode().hide();
        }
        this.mDataLoader.pause();
        this.mDataLoader.unfreeze();
        this.mActionModeHandler.pause();
        this.mIntentChooser.pause();
        SlotView slotView = getSlotView();
        if (slotView.needToDoDeleteAnimation()) {
            dismissDeleteProgressDialog();
            slotView.clearDeleteVisibleRangeItem();
        }
        slotView.pause();
    }

    public void process(Action action, ArrayList<Path> paths) {
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 4:
                this.mMenuExecutor.startAction(R.id.action_delete, R.string.delete, this.mSlotDeleteProgressListener, false, true, Style.NORMAL_STYLE, paths, null);
                return;
            case 17:
                this.mIntentChooser.share(this.mHost.getGalleryContext(), this.mActionBar.getCurrentMode(), this.mMenuExecutor, this.mMediaSet.getPath(), paths);
                return;
            default:
                return;
        }
    }

    public Rect getPreviewImageRect(MotionEvent event) {
        return null;
    }

    public Bitmap getPreviewBitmap(MotionEvent event) {
        return null;
    }

    public boolean isVideo(MotionEvent event) {
        return false;
    }

    public boolean isScrolling() {
        return false;
    }

    protected void jumpToPreviewActivity(MotionEvent event, MediaItem item) {
        if (this.mSlotPreviewPhotoManager != null && item != null) {
            if (!item.getMimeType().startsWith("video/")) {
                if (item.getScreenNailBitmap(1) == null) {
                    this.mHost.getGalleryContext().getThreadPool().submit(item.requestImage(1));
                }
                this.mHost.getActivity().startActivityForResult(GalleryUtils.getPeekAcitivtyIntent(this.mHost.getActivity(), item, this.mMediaSetPath), 1);
            }
            this.mHost.getActivity().overridePendingTransition(0, 0);
        }
    }

    protected void onStateResult(int requestCode, int resultCode, Intent data) {
        Path path = null;
        if (resultCode == -1 && requestCode == 1) {
            Action action = (Action) data.getSerializableExtra(KEY_ACTION);
            String itemPathString = data.getStringExtra(KEY_VALUE);
            if (itemPathString != null) {
                path = Path.fromString(itemPathString);
            }
            if (path != null) {
                ArrayList<Path> items = new ArrayList(1);
                items.add(path);
                if (this.mSlotPreviewPhotoManager != null) {
                    this.mSlotPreviewPhotoManager.addEvent(action, items);
                }
            }
        } else if (requestCode == 500) {
            this.mIntentChooser.onReceiveShareResult(requestCode, resultCode, data);
        } else {
            this.mIntentChooser.onResult();
            if (this.mCust != null) {
                this.mCust.handleCustStateResult(requestCode, resultCode, data, this.mHost, this.mMediaSet);
            }
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mDataLoader != null) {
            this.mDataLoader.setLoadingListener(null);
        }
        this.mSelectionManager.setSelectionListener(null);
        this.mHandler.removeCallbacksAndMessages(null);
        if (this.mCreateDialog != null) {
            GalleryUtils.setDialogDismissable(this.mCreateDialog, true);
            GalleryUtils.dismissDialogSafely(this.mCreateDialog, null);
        }
    }

    protected void onLoadingStarted() {
        this.mLoading = true;
    }

    protected void onLoadingFinished(boolean loadingFailed) {
        boolean z;
        boolean z2 = true;
        this.mRenderUntilWaitLoadFinish = false;
        this.mRootPane.invalidate();
        hideLoadingTips();
        if (this.mHasSomeItems || this.mDataLoader.size() > 0) {
            z = true;
        } else {
            z = false;
        }
        this.mHasSomeItems = z;
        if (this.mDataLoader.size() != 0) {
            z2 = false;
        }
        updateMenu(z2);
        if (!this.mIsActive || this.mDataLoader.size() != 0) {
            hideEmptyAlbum();
        } else if (this.mHasSomeItems && autoFinishWhenNoItems()) {
            this.mHost.getStateManager().finishState(this);
        } else {
            showEmptyAlbum();
        }
        this.mLoading = false;
        if (this.mSelectionManager.inSelectionMode() && (this.mActionBar.getCurrentMode() instanceof SelectionMode)) {
            this.mSelectionManager.updateSelectMode(this.mGetContent);
        }
        if (this.mDeleteDialog != null && this.mDeleteDialog.isShowing()) {
            onDelete();
        }
        if (this.mSlotPreviewPhotoManager != null) {
            this.mSlotPreviewPhotoManager.processPreviewModeEvent();
            this.mRootPane.invalidate();
        }
        GalleryLog.d(TAG, "loadingFailed:" + loadingFailed + ", mLoading:" + this.mLoading);
    }

    protected void onVisibleRangeLoadFinished() {
        hideLoadingTips();
    }

    protected boolean autoFinishWhenNoItems() {
        return true;
    }

    protected final boolean acquireDrmRight(MediaItem item) {
        if (!item.isDrm() || item.hasRight()) {
            return true;
        }
        if (item.canForward()) {
            item.getRight();
        }
        return false;
    }

    private final void onGetContent(MediaItem item) {
        boolean z = true;
        if (canGetContent(item)) {
            String crop = this.mData.getString("crop");
            if ("wallpaper".equals(crop)) {
                Intent intent = new Intent("com.android.camera.action.CROP", item.getContentUri()).putExtras(this.mData);
                intent.setClass(this.mHost.getActivity(), WallpaperConstant.CROP_WALLPAPER_CLASS);
                this.mHost.getActivity().startActivityForResult(intent, REQUEST_CROP_WALLPAPER);
            } else if (crop != null) {
                Intent dataAndType = new Intent("com.android.camera.action.CROP", item.getContentUri()).addFlags(33554432).putExtras(this.mData).setDataAndType(item.getContentUri(), item.getMimeType());
                String str = "return-data";
                if (this.mData.getParcelable("output") != null) {
                    z = false;
                }
                this.mHost.getActivity().startActivity(dataAndType.putExtra(str, z));
                this.mHost.getActivity().finish();
            } else {
                this.mHost.getActivity().setResult(-1, new Intent(null, item.getContentUri()).addFlags(1));
                this.mHost.getActivity().finish();
            }
        }
    }

    private boolean canGetContent(MediaItem item) {
        boolean fetchWallpaper = this.mData.getBoolean("fetch-content-for-wallpaper", false);
        if (!item.isDrm()) {
            return true;
        }
        if (fetchWallpaper && DrmUtils.canSetAsWallPaper(item)) {
            return true;
        }
        ContextedUtils.showToastQuickly(this.mHost.getActivity(), (int) R.string.choose_invalid_drmimage_Toast, 0);
        return false;
    }

    public void onSelectionModeChange(int mode) {
        switch (mode) {
            case 1:
                enterSelectionMode();
                return;
            case 2:
                leaveSelectionMode();
                return;
            case 3:
                selectAll();
                return;
            case 4:
                deSelectAll();
                return;
            default:
                return;
        }
    }

    protected void enterSelectionMode() {
        SelectionMode sm = this.mActionBar.enterSelectionMode(true);
        sm.setLeftAction(Action.NO);
        if (!this.mGetContent) {
            sm.setTitle((int) R.string.has_selected);
            sm.setRightAction(Action.NONE);
        } else if (this.mSupportMultiPick) {
            sm.setTitle((int) R.string.no_selected);
            sm.setRightAction(Action.OK);
        } else {
            sm.setTitle(this.mData.getInt("get-title", R.string.widget_type));
            sm.setRightAction(Action.NONE);
        }
        sm.setMenu(Math.min(this.mDefaultMenuItemCount, this.mMenu.length), this.mMenu);
        sm.show();
        this.mActionModeHandler.updateSupportedOperation(sm);
        if (!this.mGetContent) {
            this.mHost.requestFeature(296);
            this.mRootPane.requestLayout();
        }
    }

    private void selectAll() {
        int count = this.mSelectionManager.getSelectedCount();
        this.mSelectedCount = count;
        SelectionMode sm = (SelectionMode) this.mActionBar.getCurrentMode();
        sm.setTitle((int) R.string.has_selected);
        sm.setCount(count);
        dealAllAction(sm, true);
        sm.changeAction(Action.ACTION_ID_ALL, Action.ACTION_ID_DEALL);
        this.mActionModeHandler.updateSupportedOperation(sm);
        this.mRootPane.invalidate();
    }

    private void deSelectAll() {
        this.mSelectedCount = 0;
        SelectionMode sm = (SelectionMode) this.mActionBar.getCurrentMode();
        sm.setTitle((int) R.string.no_selected);
        sm.setCount(null);
        sm.changeAction(Action.ACTION_ID_DEALL, Action.ACTION_ID_ALL);
        dealAllAction(sm, false);
        this.mActionModeHandler.updateSupportedOperation(sm);
        this.mRootPane.invalidate();
    }

    protected void dealAllAction(ActionBarStateBase state, boolean enable) {
        for (Action action : this.mMenu) {
            boolean z;
            if (this.mActionNotUpdate.contains(Integer.valueOf(action.id))) {
                z = enable;
            } else {
                z = false;
            }
            if (!z) {
                state.setActionEnable(enable, action.id);
            }
        }
        state.setActionEnable(true, Action.ALL.id);
    }

    public void onSelectionChange(Path path, boolean selected) {
        int count = this.mSelectionManager.getSelectedCount();
        this.mSelectedCount = count;
        final SelectionMode sm = (SelectionMode) this.mActionBar.getCurrentMode();
        if (count != this.mSelectionManager.getTotalCount()) {
            sm.changeAction(Action.ACTION_ID_DEALL, Action.ACTION_ID_ALL);
        }
        if (this.mGetContent) {
            if (this.mSupportMultiPick) {
                if (count == 0) {
                    sm.setTitle((int) R.string.no_selected);
                    sm.setCount(null);
                } else if (this.mMaxSelectCount > 0) {
                    sm.setTitle((int) R.string.has_selected);
                    sm.setCount(count, this.mMaxSelectCount);
                } else {
                    sm.setTitle((int) R.string.has_selected);
                    sm.setCount(count);
                }
            }
        } else if (count == 0) {
            sm.setTitle((int) R.string.no_selected);
            sm.setCount(null);
            this.mHandler.post(new Runnable() {
                public void run() {
                    AbsAlbumPage.this.dealAllAction(sm, false);
                }
            });
        } else {
            sm.setTitle((int) R.string.has_selected);
            sm.setCount(count);
            dealAllAction(sm, true);
            if (count == 1 && this.mSelectionManager.getTotalCount() == 1) {
                sm.changeAction(Action.ACTION_ID_ALL, Action.ACTION_ID_DEALL);
            }
            this.mActionModeHandler.updateSupportedOperation(sm);
        }
    }

    public void onSelectionLimitExceed() {
    }

    protected void onShare() {
        ReportToBigData.report(180);
        int selectedCount = this.mSelectionManager.getSelectedCount();
        if (selectedCount > 500) {
            GalleryUtils.showLimitExceedDialog(this.mHost.getActivity());
            return;
        }
        if (selectedCount == 1) {
            ArrayList<Path> paths = this.mSelectionManager.getSelected(true);
            if (!paths.isEmpty()) {
                MediaItem item = (MediaItem) this.mHost.getGalleryContext().getDataManager().getMediaObject((Path) paths.get(0));
                if (item != null) {
                    if (item.is3DPanorama() && FyuseManager.getInstance().startShareFyuseFile(this.mHost.getActivity(), item, "GRIDVIEW")) {
                        return;
                    }
                }
                return;
            }
            return;
        }
        if (PhotoShareUtils.isSupportShareToCloud()) {
            this.mIntentChooser.addShareItem(this.mPhotoShareItem);
        } else {
            this.mIntentChooser.removeShareItem(this.mPhotoShareItem);
        }
        this.mIntentChooser.share(this.mHost.getGalleryContext(), this.mActionBar.getCurrentMode(), this.mMenuExecutor, this.mMediaSet.getPath(), this.mSelectionManager.getSelected(false));
    }

    private String getDeleteMessage() {
        return DeleteMsgUtil.getDeleteMsg(this.mHost.getGalleryContext().getResources(), this.mHost.getGalleryContext().getDataManager(), this.mMediaSet, this.mVirtualFlags, this.mSelectionManager.getSelectedCount(), new Delegate() {
            public MediaItem getSelectedItem() {
                ArrayList<Path> paths = AbsAlbumPage.this.mSelectionManager.getSelected(true);
                if (paths.size() == 0) {
                    return null;
                }
                return (MediaItem) AbsAlbumPage.this.mHost.getGalleryContext().getDataManager().getMediaObject((Path) paths.get(0));
            }
        }, null);
    }

    private String getDeleteTitle() {
        return DeleteMsgUtil.getDeleteTitle(this.mHost.getGalleryContext().getResources(), this.mMediaSet, this.mSelectionManager.getSelectedCount(), false, isHicloudAlbum(), isSyncAlbum());
    }

    public void onDelete() {
        Bundle data = new Bundle();
        if (RecycleUtils.supportRecycle()) {
            data.putInt("recycle_flag", 2);
        } else {
            data.putInt("recycle_flag", 0);
        }
        onDelete(data);
    }

    public void onDelete(final Bundle data) {
        String message = getDeleteMessage();
        String title = getDeleteTitle();
        if (this.mSelectionManager.getSelectedCount() == 1) {
            ArrayList<Path> paths = this.mSelectionManager.getSelected(true);
            if (paths != null && paths.size() > 0) {
                MediaItem item = (MediaItem) this.mHost.getGalleryContext().getDataManager().getMediaObject((Path) paths.get(0));
                if (!(item == null || !item.isBurstCover() || RecycleUtils.supportRecycle())) {
                    title = message;
                    message = null;
                }
            } else {
                return;
            }
        }
        if (this.mDeleteDialog == null) {
            ActionDeleteAndConfirm actionRecycleAndConfirm;
            if (data.getInt("recycle_flag", 0) == 2) {
                actionRecycleAndConfirm = new ActionRecycleAndConfirm(this.mHost.getActivity(), message, title);
            } else {
                actionRecycleAndConfirm = new ActionDeleteAndConfirm(this.mHost.getActivity(), message, title);
            }
            this.mDeleteDialog = actionRecycleAndConfirm;
            this.mDeleteDialog.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    AbsAlbumPage.this.mDeleteDialog = null;
                }
            });
            this.mDeleteDialog.setOnClickListener(new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == -1) {
                        AbsAlbumPage.this.mMenuExecutor.startAction(R.id.action_delete, R.string.delete, AbsAlbumPage.this.mSlotDeleteProgressListener, false, true, Style.NORMAL_STYLE, null, data, AbsAlbumPage.this.getDeleteFlag());
                        if (AbsAlbumPage.this.mSelectionManager.inSelectAllMode()) {
                            AbsAlbumPage.this.mHandler.sendEmptyMessageDelayed(2, 100);
                        }
                        ReportToBigData.report(AbsAlbumPage.REQUEST_PASTE_TARGET_CHOOSED);
                    }
                    AbsAlbumPage.this.mRootPane.invalidate();
                }
            });
            this.mDeleteDialog.updateStatus(isSyncAlbum(), isHicloudAlbum());
            this.mDeleteDialog.show();
        } else if (this.mDeleteDialog.isShowing()) {
            this.mDeleteDialog.updateMessage(message, title);
        } else {
            this.mDeleteDialog.updateStatus(isSyncAlbum(), isHicloudAlbum());
            this.mDeleteDialog.show(message, title);
        }
    }

    private int getDeleteFlag() {
        int deleteFlag = 0;
        if (PhotoShareUtils.isHiCloudLogin() && PhotoShareUtils.isCloudPhotoSwitchOpen() && (isSyncAlbum() || isHicloudAlbum())) {
            deleteFlag = (isHicloudAlbum() || this.mDeleteDialog.getCheckBoxStatus()) ? 3 : 1;
            ReportToBigData.reportForDeleteLocalOrAll(deleteFlag, TAG);
        }
        return deleteFlag;
    }

    protected void onDeleteProgressComplete(HashMap<Path, Object> visiblePathMap, HashMap<Object, Object> visibleIndexMap, AbsLayout layout) {
        onDeleteProgressComplete(visiblePathMap, visibleIndexMap, layout, 1, null);
    }

    protected void onDeleteProgressComplete(HashMap<Path, Object> visiblePathMap, HashMap<Object, Object> visibleIndexMap, AbsLayout layout, int result, String toastContent) {
        if (this.mIsActive && this.mDataLoader.isFreezed() && result == 1) {
            getSlotView().enableDeleteAnimation(visiblePathMap, visibleIndexMap, layout);
            if (toastContent != null) {
                ContextedUtils.showToastQuickly(this.mHost.getActivity(), (CharSequence) toastContent, 0);
            }
        } else {
            SlotRenderInterface slotRenderInterface = getSlotRenderInterface();
            if (slotRenderInterface != null) {
                slotRenderInterface.freeVisibleRangeItem(visiblePathMap);
            }
            visiblePathMap.clear();
            visibleIndexMap.clear();
            dismissDeleteProgressDialog();
        }
        this.mSlotDeleteProgressListener.setOnCompleteToastContent(null);
        this.mDataLoader.unfreeze();
    }

    protected boolean dismissDeleteProgressDialog() {
        return this.mMenuExecutor.stopTaskAndDismissDialog();
    }

    protected boolean onDeleteSlotAnimationStart() {
        GalleryLog.d(TAG, "onDeleteSlotAnimationStart");
        return dismissDeleteProgressDialog();
    }

    protected boolean onDeleteSlotAnimationEnd() {
        return true;
    }

    private void deleteExtraFiles(Context context) {
        String bucketPath = this.mMediaSet.getBucketPath();
        if (bucketPath != null) {
            GalleryUtils.deleteExtraFile(context.getContentResolver(), bucketPath, ".outside", ".inside", ".empty_out", ".empty_in");
        }
    }

    private void onEdit() {
        if (this.mSelectionManager.getSelectedCount() == 1) {
            ArrayList<Path> paths = this.mSelectionManager.getSelected(true);
            if (!paths.isEmpty()) {
                MediaItem item = (MediaItem) this.mHost.getGalleryContext().getDataManager().getMediaObject((Path) paths.get(0));
                if (item != null) {
                    if (4 == item.getMediaType()) {
                        VideoEditorController.editVideo(this.mHost.getActivity(), item.getFilePath(), 200);
                    } else if (FyuseManager.getInstance().startEditFyuseFile(this.mHost.getActivity(), item)) {
                        if (this.mSelectionManager.inSelectionMode()) {
                            this.mSelectionManager.leaveSelectionMode();
                        }
                        return;
                    } else {
                        Intent intent = new Intent("android.intent.action.EDIT");
                        intent.setDataAndType(item.getContentUri(), item.getMimeType()).setFlags(1);
                        intent.setClass(this.mHost.getActivity(), SinglePhotoActivity.class);
                        this.mHost.getActivity().startActivity(intent);
                    }
                    if (this.mSelectionManager.inSelectionMode()) {
                        this.mSelectionManager.leaveSelectionMode();
                    }
                }
            }
        }
    }

    private void onPhotoShareBackUp() {
        PhotoShareUtils.cacheShareItemList(PhotoShareUtils.getFilePathsFromPath(this.mHost.getGalleryContext(), this.mSelectionManager.getSelected(true)));
        Intent intent = new Intent(this.mHost.getActivity(), ShareToCloudAlbumActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean("inner_share", true);
        intent.putExtras(bundle);
        this.mHost.getActivity().startActivity(intent);
        if (this.mSelectionManager.inSelectionMode()) {
            this.mSelectionManager.leaveSelectionMode();
        }
    }

    private void onPaste(int type) {
        Bundle data = new Bundle();
        data.putString("media-path", this.mHost.getGalleryContext().getDataManager().getTopSetPath(16777216));
        data.putInt("key-source-bucketid", this.mMediaSet.getBucketId());
        data.putInt("key-pastestate", type);
        data.putInt("camera-location-type", this.mCardLocation);
        data.putBoolean("is-screenshots-type", this.mMediaSet instanceof LocalScreenshotsAlbum);
        Intent intent = new Intent();
        intent.setClass(this.mHost.getActivity(), PasteActivity.class);
        intent.putExtras(data);
        this.mHost.getActivity().startActivityForResult(intent, REQUEST_PASTE_TARGET_CHOOSED);
    }

    private void onDetails() {
        if (this.mShowDetails) {
            hideDetails();
        } else {
            showDetails();
        }
    }

    private void onRename() {
        if (this.mSelectionManager.getSelectedCount() == 1) {
            final MediaItem item = (MediaItem) this.mHost.getGalleryContext().getDataManager().getMediaObject((Path) this.mSelectionManager.getSelected(true).get(0));
            if (item != null) {
                final EditText renameText = new EditText(this.mHost.getActivity());
                renameText.setSingleLine(true);
                ColorfulUtils.decorateColorfulForEditText(this.mHost.getActivity(), renameText);
                this.mCreateDialog = GalleryUtils.createDialog(this.mHost.getActivity(), GalleryUtils.getMediaItemName(item), R.string.rename, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        PhotoShareUtils.hideSoftInput(renameText);
                        switch (which) {
                            case -2:
                                GalleryUtils.setDialogDismissable(dialog, true);
                                GalleryUtils.dismissDialogSafely(dialog, null);
                                AbsAlbumPage.this.mRootPane.invalidate();
                                return;
                            case -1:
                                String name = renameText.getText().toString().trim();
                                if (GalleryUtils.isNewFileNameLegal(AbsAlbumPage.this.mHost.getActivity(), item, dialog, name)) {
                                    Bundle data = new Bundle();
                                    data.putString("key_bucket_name_alias", name);
                                    AbsAlbumPage.this.mMenuExecutor.startAction(R.string.rename, R.string.rename, null, false, false, Style.NORMAL_STYLE, AbsAlbumPage.this.mSelectionManager.getProcessingList(false), data);
                                    GalleryUtils.setDialogDismissable(dialog, true);
                                    GalleryUtils.dismissDialogSafely(dialog, null);
                                    AbsAlbumPage.this.mRootPane.invalidate();
                                    ReportToBigData.report(AbsAlbumPage.REQUEST_CLOUD_PHOTO);
                                    return;
                                }
                                return;
                            default:
                                return;
                        }
                    }
                }, null, renameText);
                this.mHandler.postDelayed(new ToggleSoftInputRunable(renameText), 300);
            }
        }
    }

    private void onPrint() {
        if (this.mSelectionManager.getSelectedCount() > 0) {
            ArrayList<Path> paths = this.mSelectionManager.getSelected(true);
            DataManager dataManager = this.mHost.getGalleryContext().getDataManager();
            List mediaItems = new ArrayList(paths.size());
            for (Path path : paths) {
                MediaItem item = (MediaItem) dataManager.getMediaObject(path);
                if (item != null && item.getMediaType() == 2) {
                    mediaItems.add(item);
                }
            }
            GalleryUtils.printSelectedImage(this.mHost.getActivity(), mediaItems);
            if (this.mSelectionManager.inSelectionMode()) {
                this.mSelectionManager.leaveSelectionMode();
            }
        }
    }

    public ActionBarStateBase getCurrentActionBarState() {
        return this.mActionBar.getCurrentMode();
    }

    public void hasFoundVirtualFlag(int flag) {
        this.mVirtualFlags |= flag;
        if (this.mDeleteDialog != null && this.mDeleteDialog.isShowing()) {
            onDelete();
        }
    }

    public void resetVirtualFlag() {
        this.mVirtualFlags = 0;
    }

    public void onNavigationBarChanged(boolean show, int height) {
        this.mRootPane.requestLayout();
    }

    protected void showDetails() {
        this.mShowDetails = true;
        if (this.mDetailsHelper == null) {
            this.mDetailsHelper = new DetailsHelper(this.mHost.getGalleryContext(), null, new MyDetailsSource());
            this.mDetailsHelper.setCloseListener(new CloseListener() {
                public void onClose() {
                    AbsAlbumPage.this.hideDetails();
                }
            });
        }
        this.mDetailsHelper.show();
    }

    protected void hideDetails() {
        this.mShowDetails = false;
        this.mDetailsHelper.hide();
        this.mRootPane.invalidate();
    }

    @SuppressLint({"ServiceCast"})
    private void setBlurWallpaperChanged() {
        WallpaperManager wm = (WallpaperManager) this.mHost.getActivity().getSystemService("wallpaper");
        try {
            wm.getClass().getDeclaredMethod("setCallback", new Class[]{IBlurWallpaperCallback.class}).invoke(wm, new Object[]{this});
        } catch (NoSuchMethodException e) {
            GalleryLog.d(TAG, "can not find setCallback: NoSuchMethodException !!!");
        } catch (RuntimeException e2) {
            GalleryLog.d(TAG, "can not find setCallback: RuntimeException !!!");
        } catch (InvocationTargetException e3) {
            GalleryLog.d(TAG, "can not find setCallback: InvocationTargetException  !!!");
        } catch (IllegalAccessException e4) {
            GalleryLog.d(TAG, "can not find setCallback: IllegalAccessException !!!");
        }
    }

    protected boolean needFreeSlotContent() {
        boolean z = false;
        if (this.mSlotPreviewPhotoManager == null) {
            return super.needFreeSlotContent();
        }
        if (super.needFreeSlotContent() && !this.mSlotPreviewPhotoManager.inPreviewMode()) {
            z = true;
        }
        return z;
    }

    public void onBlurWallpaperChanged() {
    }

    protected boolean isSyncAlbum() {
        return !(this.mMediaSet instanceof GalleryMediaTimegroupAlbum) ? this.mMediaSet instanceof MapAlbum : true;
    }

    protected boolean isHicloudAlbum() {
        return this.mMediaSet instanceof CloudLocalAlbum;
    }
}
