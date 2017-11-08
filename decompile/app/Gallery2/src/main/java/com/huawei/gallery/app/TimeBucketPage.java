package com.huawei.gallery.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.anim.Animation;
import com.android.gallery3d.anim.FloatAnimation;
import com.android.gallery3d.app.Config$LocalCameraAlbumPage;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.GallerySource.CodePath;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.data.TimeBucketPageViewMode;
import com.android.gallery3d.settings.GallerySettings;
import com.android.gallery3d.settings.HicloudAccountManager;
import com.android.gallery3d.ui.AbstractGifScreenNail;
import com.android.gallery3d.ui.AnimationTime;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.MenuExecutorFactory.Style;
import com.android.gallery3d.ui.NetworkTipsView;
import com.android.gallery3d.ui.ProgressbarDelegate;
import com.android.gallery3d.ui.SlotPreviewManager;
import com.android.gallery3d.ui.SlotPreviewManager.SlotPreviewState;
import com.android.gallery3d.ui.SlotPreviewPhotoManager;
import com.android.gallery3d.ui.SlotPreviewPhotoManager.SlotPreviewModeListener;
import com.android.gallery3d.ui.SlotPreviewView;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MultiWindowStatusHolder;
import com.android.gallery3d.util.MultiWindowStatusHolder.IMultiWindowModeChangeListener;
import com.android.gallery3d.util.ReportToBigData;
import com.android.gallery3d.util.TraceController;
import com.autonavi.amap.mapcore.MapConfig;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionBarStateBase;
import com.huawei.gallery.actionbar.TabMode;
import com.huawei.gallery.anim.ProgressAnimation;
import com.huawei.gallery.animation.CubicBezierInterpolator;
import com.huawei.gallery.app.MediaItemsDataLoader.LoadCountListener;
import com.huawei.gallery.app.TimeBucketItemsDataLoader.MediaSetRegion;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.photoshare.utils.RefreshHelper;
import com.huawei.gallery.quickcamera.ActionbarView;
import com.huawei.gallery.servicemanager.CloudManager;
import com.huawei.gallery.servicemanager.CloudManager.UploadListener;
import com.huawei.gallery.ui.ListSlotView.ItemCoordinate;
import com.huawei.gallery.ui.PlaceHolderView;
import com.huawei.gallery.ui.RectView;
import com.huawei.gallery.ui.SlotView;
import com.huawei.gallery.ui.SlotView.SlotRenderInterface;
import com.huawei.gallery.ui.TopMessageView;
import com.huawei.gallery.util.LayoutHelper;
import com.huawei.gallery.util.SyncUtils;
import com.huawei.watermark.manager.parse.WMElement;
import java.util.ArrayList;
import java.util.List;

public class TimeBucketPage extends CommonTimeBucketPage implements OnPageChangedListener, UploadListener {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    private static final int COEFFICIENT = 250;
    private static int MAX_ITEMS_COUNT = 4;
    public static final String SOURCE_DATA_PATH = CodePath.GALLERY_TIMEGROUP_ALBUM.path;
    public static final String TAG = "TimeBucketPage";
    private static int mSlideShowIndex = 0;
    private final int LIMIT_MAX = GalleryUtils.dpToPixel(180);
    private final int LIMIT_STABLE = GalleryUtils.dpToPixel(34);
    private final int PROGRESS_SIZE = GalleryUtils.dpToPixel(16);
    private ActionbarView mActionbarView;
    private Thread mAsynchronousThread;
    private CloudManager mCloudManager;
    private Config$LocalCameraAlbumPage mConfig;
    private Action[] mDisplayMenu = null;
    private final DynApkItem[] mDynApkList = new DynApkItem[]{new DynApkItem(Action.DYNAMIC_ALBUM, "com.tencent.weishi.photo", "com.tencent.weishi.action.PIC_VIEW_ALL"), new DynApkItem(Action.COLLAGE, "com.tencent.ttpic4huawei", "android.intent.action.SEND_MULTIPLE")};
    private RelativeLayout mEmptyAlbumLayout;
    private TextView mEmptyAlbumTips;
    protected boolean mIsLayoutRtl;
    private boolean mIsPullingdown;
    private LayoutSpec mLayoutSpec;
    private int mMainViewOffsetTop = 0;
    private Animation mMainViewReseter = new Animation() {
        private int holder;
        private int limit;
        private float mDeceleration;
        private Interpolator mInterpolator = new CubicBezierInterpolator(0.2f, 0.65f, 0.28f, 0.97f);
        private int mOver;
        private int mVelocity;

        private float getDeceleration(int velocity) {
            return velocity > 0 ? -2000.0f : 2000.0f;
        }

        public void start() {
            this.holder = TimeBucketPage.this.mMainViewOffsetTop;
            this.limit = TimeBucketPage.this.mMainViewOffsetTop > TimeBucketPage.this.LIMIT_STABLE ? TimeBucketPage.this.LIMIT_STABLE : 0;
            int delta = this.holder - this.limit;
            this.mDeceleration = getDeceleration(delta);
            this.mVelocity = -delta;
            this.mOver = Math.abs(delta);
            this.mDuration = (int) (Math.sqrt((((double) delta) * -2.0d) / ((double) this.mDeceleration)) * 1000.0d);
            setDuration(this.mDuration);
            super.start();
            TimeBucketPage.this.mRootPane.requestLayout();
        }

        protected void onCalculate(float progress) {
            float t = this.mInterpolator.getInterpolation(progress);
            float t2 = t * t;
            TimeBucketPage.this.mMainViewOffsetTop = Math.max(this.holder + ((int) Math.round((double) ((((float) this.mOver) * Math.signum((float) this.mVelocity)) * ((MapConfig.MIN_ZOOM * t2) - ((2.0f * t) * t2))))), this.limit);
            TimeBucketPage.this.updateProgressBar();
        }
    };
    private IMultiWindowModeChangeListener mMultiWindowModeChangeListener;
    private RectView mNavigationBarCover;
    private ProgressAnimation mNetworkAnim;
    private NetworkTipsView mNetworkTipsView;
    private ItemCoordinate mPressedIndex;
    private float mPressureValueThreshold;
    private AbstractGifScreenNail mProgressBarScreenNail = new AbstractGifScreenNail() {
        public void noDraw() {
        }

        public int getWidth() {
            return TimeBucketPage.this.PROGRESS_SIZE;
        }

        public int getHeight() {
            return TimeBucketPage.this.PROGRESS_SIZE;
        }

        public void draw(GLCanvas canvas, RectF source, RectF dest) {
        }

        public void draw(GLCanvas canvas, int x, int y, int width, int height) {
            drawGifIfNecessary(canvas, x, y, width, height);
        }
    };
    private ProgressbarDelegate mProgressbar;
    private MotionEvent mSlotPreviewEvent;
    private SlotPreviewManager mSlotPreviewManager;
    private final Runnable mSlotPreviewTask = new Runnable() {
        public void run() {
            MotionEvent event = TimeBucketPage.this.mSlotPreviewEvent;
            if (event != null) {
                if (TimeBucketPage.this.mSlotPreviewManager.inPreviewMode()) {
                    float x = event.getX();
                    float y = event.getY();
                    ItemCoordinate index = TimeBucketPage.this.mSlotView.getSlotIndexByPosition(x, y);
                    Rect slotViewBounds = TimeBucketPage.this.mSlotView.bounds();
                    int touchX = ((int) x) + slotViewBounds.left;
                    int touchY = ((int) y) + slotViewBounds.top;
                    if (index == null && slotViewBounds.contains(touchX, touchY)) {
                        index = TimeBucketPage.this.getSlotIndexByPositionWithThreshold(x, y);
                    }
                    if (index != null) {
                        TimeBucketPage.this.mSlotPreviewView.setVisibility(0);
                        TimeBucketPage.this.mSlotPreviewManager.setPreviewViewScale(TimeBucketPage.this.getPreviewViewScale(event.getPressure()));
                        TimeBucketPage.this.showSlotPreView(x, y, index);
                    } else {
                        TimeBucketPage.this.mPressedIndex = null;
                        TimeBucketPage.this.mSlotPreviewView.setVisibility(1);
                    }
                    return;
                }
                if (TimeBucketPage.this.supportSlotPreviewMode(event.getPressure())) {
                    TimeBucketPage.this.enterPreviewMode(event, false);
                }
            }
        }
    };
    private SlotPreviewView mSlotPreviewView;
    private Button mSyncPhotoButton;
    private Button mTakePhotoButton;
    private TimeBucketItemsDataLoader mTimeBucketItemsDataLoader = null;
    private PlaceHolderView mTopCover;
    private TopMessageView mTopMessageView;
    private Runnable mUpdateProgressBar = new Runnable() {
        public void run() {
            if (TimeBucketPage.this.getProgressbar() != null) {
                int visibility = (!TimeBucketPage.this.mCloudManager.isUploading() || TimeBucketPage.this.mMainViewOffsetTop <= 0) ? 4 : 0;
                if (visibility != TimeBucketPage.this.mProgressbar.getVisibility()) {
                    TimeBucketPage.this.mProgressbar.setVisibility(visibility);
                }
            }
        }
    };
    private boolean mUserHaveFirstLook;
    private int mWindowHeight;

    private static class DynApkItem {
        Action action;
        String activity;
        String packageName;

        DynApkItem(Action act, String name, String intent) {
            this.action = act;
            this.packageName = name;
            this.activity = intent;
        }
    }

    public static class LayoutSpec {
        public int local_camera_page_left_padding;
        public int local_camera_page_right_padding;
        public int time_line_width;
    }

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
            iArr[Action.COLLAGE.ordinal()] = 1;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Action.COMMENT.ordinal()] = 12;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Action.COPY.ordinal()] = 13;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Action.DEALL.ordinal()] = 14;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Action.DEL.ordinal()] = 15;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Action.DETAIL.ordinal()] = 16;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[Action.DYNAMIC_ALBUM.ordinal()] = 2;
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
            iArr[Action.MAP.ordinal()] = 3;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[Action.MENU.ordinal()] = 25;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[Action.MORE_EDIT.ordinal()] = 26;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[Action.MOVE.ordinal()] = 27;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[Action.MOVEIN.ordinal()] = 28;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[Action.MOVEOUT.ordinal()] = 29;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[Action.MULTISCREEN.ordinal()] = 30;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[Action.MULTISCREEN_ACTIVITED.ordinal()] = 31;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[Action.MULTI_SELECTION.ordinal()] = 32;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[Action.MULTI_SELECTION_ON.ordinal()] = 33;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[Action.MYFAVORITE.ordinal()] = 34;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[Action.NO.ordinal()] = 35;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[Action.NONE.ordinal()] = 36;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[Action.NOT_MYFAVORITE.ordinal()] = 37;
        } catch (NoSuchFieldError e37) {
        }
        try {
            iArr[Action.OK.ordinal()] = 38;
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

    protected Config$LocalCameraAlbumPage getConfig() {
        return this.mConfig;
    }

    protected void onCreate(Bundle data, Bundle storedState) {
        Config$LocalCameraAlbumPage config = Config$LocalCameraAlbumPage.get(this.mHost.getActivity());
        this.mConfig = config;
        this.mLayoutSpec = config.layoutSpec;
        TraceController.beginSection("TimeBucketPage.onCreate");
        super.onCreate(data, storedState);
        ConditionVariable lock = SyncUtils.runWithConditionVariable(new Runnable() {
            public void run() {
                TimeBucketPage.this.buildTimeBucketActionBar();
                TimeBucketPage.this.mCloudManager = (CloudManager) TimeBucketPage.this.mHost.getGalleryContext().getGalleryApplication().getAppComponent(CloudManager.class);
                TimeBucketPage.this.mIsLayoutRtl = GalleryUtils.isLayoutRTL();
            }
        });
        TraceController.beginSection("initViews");
        initViews();
        TraceController.endSection();
        lock.block();
        if (this.mCloudManager != null) {
            this.mCloudManager.registerUploadListener(this);
        }
        TraceController.endSection();
    }

    private synchronized ProgressbarDelegate getProgressbar() {
        final Activity activity = this.mHost.getActivity();
        if (activity == null) {
            return this.mProgressbar;
        }
        final RelativeLayout root = (RelativeLayout) activity.findViewById(R.id.gallery_root);
        TraceController.beginSection("TimeBucketPage.Progressbar");
        if (this.mProgressbar == null && root != null) {
            final ConditionVariable lock = new ConditionVariable(false);
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    LayoutParams params = new LayoutParams(TimeBucketPage.this.PROGRESS_SIZE, TimeBucketPage.this.PROGRESS_SIZE);
                    TimeBucketPage.this.mProgressbar = new ProgressbarDelegate(activity, TimeBucketPage.this.mTopMessageView, TimeBucketPage.this.mProgressBarScreenNail);
                    root.addView(TimeBucketPage.this.mProgressbar, params);
                    lock.open();
                    TimeBucketPage.this.updateProgressBar();
                }
            });
            lock.block();
        }
        TraceController.endSection();
        return this.mProgressbar;
    }

    private void updateProgressBar() {
        Activity activity = this.mHost.getActivity();
        if (activity != null) {
            activity.runOnUiThread(this.mUpdateProgressBar);
        }
    }

    protected void onLoadingFinished(boolean loadingFailed) {
        super.onLoadingFinished(loadingFailed);
        this.mHost.getGalleryActionBar().disableAnimation(false);
        updateProgressBar();
        Activity context = this.mHost.getActivity();
        if (context != null && this.mNetworkTipsView != null) {
            this.mNetworkTipsView.setEnable(isShouldShowNetworkTips(context));
        }
    }

    protected void initMenuItemCount() {
        this.mDefaultMenuItemCount = 4;
    }

    public void onStatusChanged(CloudManager manager, int type) {
        if (!this.mDataLoader.isFreezed()) {
            TraceController.beginSection("TimeBucketPage.onStatusChanged");
            this.mTopMessageView.setMessage(manager.getUploadingString());
            this.mTopMessageView.setUploading(manager.isUploading());
            TraceController.endSection();
        }
    }

    protected boolean onCreateActionBar(Menu menu) {
        TraceController.beginSection("TimeBucketPage.onCreateActionBar");
        int featureFlag = 296;
        if (this.mEmptyAlbumLayout != null && this.mEmptyAlbumLayout.getVisibility() == 0) {
            featureFlag = 298;
        }
        this.mHost.requestFeature(featureFlag);
        if (this.mSelectionManager.inSelectionMode()) {
            return true;
        }
        this.mMenu = TIME_BUCKET_ALBUM_MENU;
        TabMode tm = this.mActionBar.enterTabMode(false);
        tm.setMenu(Math.min(this.mDisplayMenu.length - 1, MAX_ITEMS_COUNT), this.mDisplayMenu);
        tm.show();
        TraceController.endSection();
        return true;
    }

    private void disableViewPagerScroll(boolean disabled) {
        Activity activity = this.mHost.getActivity();
        if (activity instanceof GalleryMain) {
            ((GalleryMain) activity).disableViewPagerScrool(disabled);
        }
    }

    private void disableTabSelection(boolean disabled) {
        Activity activity = this.mHost.getActivity();
        if (activity instanceof GalleryMain) {
            ((GalleryMain) activity).disableTabSelection(disabled);
        }
    }

    private void initViews() {
        ConditionVariable lock = SyncUtils.runWithConditionVariable(new Runnable() {
            public void run() {
                TraceController.beginSection("SlotPreviewPhotoManager");
                TimeBucketPage.this.mSlotPreviewPhotoManager = new SlotPreviewPhotoManager(TimeBucketPage.this.mHost.getActivity(), TimeBucketPage.this.mHost.getGLRoot());
                TimeBucketPage.this.mSlotPreviewPhotoManager.setDelegate(TimeBucketPage.this);
                TimeBucketPage.this.mSlotPreviewPhotoManager.setListener(new SlotPreviewModeListener() {
                    public void onEnterPreviewMode() {
                        TimeBucketPage.this.disableViewPagerScroll(true);
                        TimeBucketPage.this.disableTabSelection(true);
                        TimeBucketPage.this.mSlotView.updatePreviewMode(true);
                    }

                    public void onLeavePreviewMode() {
                        TimeBucketPage.this.disableViewPagerScroll(false);
                        TimeBucketPage.this.disableTabSelection(false);
                        TimeBucketPage.this.mSlotView.updatePreviewMode(false);
                    }

                    public void onStartPreview(MotionEvent event) {
                        TimeBucketPage.this.processPreview(event);
                    }

                    public void onClick(MotionEvent event) {
                        if (event != null) {
                            ItemCoordinate itemCoordinate = TimeBucketPage.this.mSlotView.getSlotIndexByPosition(event.getX(), event.getY());
                            if (itemCoordinate != null && !itemCoordinate.isTitle()) {
                                int index = TimeBucketPage.this.getItemIndex(itemCoordinate);
                                if (index >= 0) {
                                    MediaItem item = TimeBucketPage.this.getMediaItem(index);
                                    if (item != null) {
                                        TimeBucketPage.this.mSlotView.setIndexUp(itemCoordinate);
                                        TimeBucketPage.this.pickPhotoWithAnimation(TimeBucketPage.this.mSlotView, 100, itemCoordinate, index, item);
                                    }
                                }
                            }
                        }
                    }

                    public void onLongClick(MotionEvent event) {
                        if (event != null && TimeBucketPage.this.mSlotView.getViewMode() == TimeBucketPageViewMode.DAY) {
                            ItemCoordinate slotIndex = TimeBucketPage.this.mSlotView.getSlotIndexByPosition(event.getX(), event.getY());
                            if (slotIndex != null) {
                                MediaItem item = TimeBucketPage.this.getMediaItem(TimeBucketPage.this.getItemIndex(slotIndex));
                                if (item != null) {
                                    TimeBucketPage.this.mTimeAxisSelectionManager.toggle(slotIndex, item.getPath());
                                    TimeBucketPage.this.mSlotView.invalidate();
                                    TimeBucketPage.this.mSlotView.startClickSlotAnimation(slotIndex, null);
                                }
                            }
                        }
                    }
                });
                TraceController.endSection();
                TimeBucketPage.this.mSlotPreviewView = new SlotPreviewView(TimeBucketPage.this.mHost.getGalleryContext());
                TimeBucketPage.this.mSlotPreviewView.setSlotRender(TimeBucketPage.this.mSlotRender);
                TimeBucketPage.this.mSlotPreviewManager = new SlotPreviewManager();
                TimeBucketPage.this.mSlotPreviewManager.setSlotPreviewModeListener(new SlotPreviewManager.SlotPreviewModeListener() {
                    public void onLeavePreviewMode() {
                        TimeBucketPage.this.mActionBar.setActionClickable(true);
                        TimeBucketPage.this.disableViewPagerScroll(false);
                        TimeBucketPage.this.disableTabSelection(false);
                        TimeBucketPage.this.mSlotView.updatePreviewMode(false);
                        TimeBucketPage.this.mSlotPreviewView.setVisibility(1);
                        TimeBucketPage.this.mActionbarView.hide();
                    }

                    public void onEnterPreviewMode() {
                        TimeBucketPage.this.mActionBar.setActionClickable(false);
                        TimeBucketPage.this.disableViewPagerScroll(true);
                        TimeBucketPage.this.disableTabSelection(true);
                        TimeBucketPage.this.mSlotView.updatePreviewMode(true);
                        TimeBucketPage.this.mActionbarView.show();
                        TimeBucketPage.this.mSlotPreviewView.setVisibility(0);
                        ReportToBigData.report(60);
                    }
                });
            }
        });
        ConditionVariable initNetworkLock = SyncUtils.runWithConditionVariable(new Runnable() {
            public void run() {
                Context context = TimeBucketPage.this.mHost.getActivity();
                if (context == null) {
                    GalleryLog.v(TimeBucketPage.TAG, "context is null");
                }
                TimeBucketPage.this.createNetworkTips(context);
                if (TimeBucketPage.this.isShouldShowNetworkTips(context)) {
                    TimeBucketPage.this.mNetworkTipsView.setEnable(true);
                }
            }
        });
        this.mActionbarView = new ActionbarView(this.mHost.getActivity());
        this.mTopMessageView = new TopMessageView(this.mHost.getActivity());
        this.mTopMessageView.setProgressbar(this.mProgressBarScreenNail);
        this.mTopMessageView.setBackgroundColor(getBackgroundColor(this.mHost.getActivity()));
        this.mTopCover = new PlaceHolderView(this.mHost.getActivity());
        this.mNavigationBarCover = new RectView(getBackgroundColor(this.mHost.getActivity()), true);
        lock.block();
        initNetworkLock.block();
        this.mRootPane.addComponent(this.mSlotView);
        this.mRootPane.addComponent(this.mScrollBar);
        this.mRootPane.addComponent(this.mTopCover);
        this.mRootPane.addComponent(this.mTopMessageView);
        this.mRootPane.addComponent(this.mNavigationBarCover);
        this.mRootPane.addComponent(this.mNetworkTipsView);
        this.mRootPane.addComponent(this.mActionbarView);
        this.mRootPane.addComponent(this.mSlotPreviewView);
    }

    private boolean isShouldShowNetworkTips(Context context) {
        if (GalleryUtils.IS_CHINESE_VERSION) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs.getBoolean(GallerySettings.KEY_NETWORK_NO_TIPS, false)) {
                GalleryLog.v(TAG, "never show tips is true");
                return false;
            } else if (prefs.getBoolean(GallerySettings.KEY_USE_NETWORK, false)) {
                prefs.edit().putBoolean(GallerySettings.KEY_NETWORK_NO_TIPS, true).commit();
                GalleryLog.v(TAG, "network has been turned on.");
                return false;
            } else if (prefs.getBoolean("key-network-view-is-showing", false)) {
                GalleryLog.v(TAG, "network tips is showing");
                return true;
            } else if (this.mMediaSet == null) {
                GalleryLog.v(TAG, "media set is null");
                return false;
            } else {
                this.mMediaSet.reload();
                if (this.mMediaSet.getMediaItemCount() > 10) {
                    prefs.edit().putBoolean("key-network-view-is-showing", true).commit();
                    GalleryLog.v(TAG, "item count more than 10");
                    return true;
                }
                GalleryLog.v(TAG, "item count less than 10");
                return false;
            }
        }
        GalleryLog.v(TAG, "not chinese version");
        return false;
    }

    private void createNetworkTips(Context context) {
        this.mNetworkAnim = new ProgressAnimation() {
            protected void onAnimationEnd() {
                TimeBucketPage.this.mRootPane.removeComponent(TimeBucketPage.this.mNetworkTipsView);
            }
        };
        this.mNetworkTipsView = new NetworkTipsView(context) {
            public void onDismiss() {
                TimeBucketPage.this.mNetworkAnim.setDuration(180);
                TimeBucketPage.this.mNetworkAnim.start();
                new Thread(new Runnable() {
                    public void run() {
                        if (PhotoShareUtils.isSupportPhotoShare()) {
                            ((HicloudAccountManager) TimeBucketPage.this.mHost.getGalleryContext().getGalleryApplication().getAppComponent(HicloudAccountManager.class)).queryHicloudAccount(TimeBucketPage.this.mHost.getActivity(), false);
                        }
                    }
                }).start();
            }
        };
    }

    public void disableRender(boolean disable) {
        this.mTopCover.disableRender(disable);
        this.mNavigationBarCover.disableRender(disable);
    }

    public Rect getPreviewImageRect(MotionEvent event) {
        return this.mSlotView.getPreviewImageRect(event.getX(), event.getY());
    }

    public Bitmap getPreviewBitmap(MotionEvent event) {
        ItemCoordinate index = this.mSlotView.getSlotIndexByPosition(event.getX(), event.getY());
        if (index == null || index.isTitle()) {
            return null;
        }
        return this.mSlotRender.getContentBitmap(index);
    }

    public boolean isVideo(MotionEvent event) {
        boolean z = false;
        if (event == null) {
            return false;
        }
        ItemCoordinate index = this.mSlotView.getSlotIndexByPosition(event.getX(), event.getY());
        if (index == null || index.isTitle()) {
            return false;
        }
        MediaItem item = getMediaItem(index);
        if (item != null) {
            z = item.getMimeType().startsWith("video/");
        }
        return z;
    }

    private void processPreview(MotionEvent event) {
        ItemCoordinate index = this.mSlotView.getSlotIndexByPosition(event.getX(), event.getY());
        if (index != null) {
            MediaItem item = getMediaItem(index);
            if (item != null) {
                jumpToPreviewActivity(event, item);
            }
        }
    }

    public void onDown(ItemCoordinate index) {
        super.onDown(index);
        this.mPressedIndex = index;
    }

    public void onUp(boolean followedByLongPress) {
        if (!this.mSlotPreviewManager.inPreviewMode()) {
            super.onUp(followedByLongPress);
            this.mPressedIndex = null;
        }
    }

    private boolean supportSlotPreviewMode(float pressure) {
        Context context = this.mHost.getActivity();
        if (this.mSlotView.getViewMode() == TimeBucketPageViewMode.DAY || !GalleryUtils.isSupportPressureResponse(context) || Float.compare(pressure, this.mPressureValueThreshold - 0.05f) <= 0) {
            return false;
        }
        return true;
    }

    private boolean supportPreviewMode() {
        if (!GalleryUtils.isSupportPressurePreview(this.mHost.getActivity()) || this.mSelectionManager.inSelectionMode() || this.mSlotView.isScrolling()) {
            return false;
        }
        return true;
    }

    public void onMove(MotionEvent event) {
        if (this.mSlotPreviewManager.inPreviewMode()) {
            disableRender(false);
        } else {
            disableRender(true);
        }
        if (supportPreviewMode()) {
            this.mSlotPreviewPhotoManager.onTouchEvent(event);
        } else if (!this.mSlotView.isScrolling()) {
            this.mSlotPreviewEvent = MotionEvent.obtain(event);
            this.mHandler.removeCallbacks(this.mSlotPreviewTask);
            this.mHandler.post(this.mSlotPreviewTask);
        }
    }

    private ItemCoordinate getSlotIndexByPositionWithThreshold(float x, float y) {
        int thresHoldX = this.mSlotView.getSlotWidthGap() * 2;
        int thresHoldY = this.mSlotView.getSlotHeightGap() * 2;
        ItemCoordinate index = this.mSlotView.getSlotIndexByPosition(((float) thresHoldX) + x, y);
        if (index != null) {
            return index;
        }
        index = this.mSlotView.getSlotIndexByPosition(x, ((float) thresHoldY) + y);
        if (index != null) {
            return index;
        }
        return this.mSlotView.getSlotIndexByPosition(((float) thresHoldX) + x, ((float) thresHoldY) + y);
    }

    protected boolean forbidPickPhoto() {
        Activity activity = this.mHost.getActivity();
        if (activity instanceof GalleryMain) {
            return ((GalleryMain) activity).forbidPickPhoto();
        }
        return false;
    }

    public void onSingleTapUp(ItemCoordinate slotIndex, boolean cornerPressed) {
        if (!this.mSlotPreviewPhotoManager.inPreviewMode() && this.mIsActive && !this.mSlotView.isAnimating()) {
            super.onSingleTapUp(slotIndex, cornerPressed);
            leavePreviewMode();
        }
    }

    protected void onJumpToMapAlbum(ItemCoordinate slotIndex) {
        super.onJumpToMapAlbum(slotIndex);
        ReportToBigData.report(14, String.format("{EnterMapMethod:%s}", new Object[]{"From" + this.mTimeBucketItemsDataLoader.getMode().toString() + "View"}));
    }

    public void onLongTap(MotionEvent event) {
        if (!this.mSlotPreviewPhotoManager.inPreviewMode() && !this.mSelectionManager.inSelectionMode()) {
            if (this.mSlotView.getViewMode() == TimeBucketPageViewMode.DAY) {
                if (this.mSlotPreviewPhotoManager.inPrepareMode()) {
                    this.mSlotPreviewPhotoManager.onLongClickPrepare();
                    return;
                }
                super.onLongTap(event);
            } else if (!(GalleryUtils.isSupportPressureResponse(this.mHost.getActivity()) || GalleryUtils.isSupportPressurePreview(this.mHost.getActivity()))) {
                enterPreviewMode(event, true);
            }
        }
    }

    private float getPreviewViewScale(float pressure) {
        return Utils.clamp(((pressure - this.mPressureValueThreshold) + 0.05f) / 0.05f, 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
    }

    private void showSlotPreView(float x, float y, ItemCoordinate slotIndex) {
        Rect previewView = getPreviewViewLayout(x, y);
        this.mSlotPreviewView.layout(previewView.left, previewView.top, previewView.right, previewView.bottom);
        this.mSlotRender.setPressedIndex(slotIndex);
        this.mPressedIndex = slotIndex;
    }

    private void enterPreviewMode(MotionEvent event, boolean byLongPress) {
        if (!this.mSlotPreviewManager.inPreviewMode()) {
            if (getProgressbar() != null) {
                this.mProgressbar.setVisibility(4);
            }
            float x = event.getX();
            float y = event.getY();
            ItemCoordinate slotIndex = this.mSlotView.getSlotIndexByPosition(x, y);
            Rect slotViewBounds = this.mSlotView.bounds();
            if (slotIndex == null && slotViewBounds.contains(slotViewBounds.left + ((int) x), slotViewBounds.top + ((int) y))) {
                slotIndex = getSlotIndexByPositionWithThreshold(x, y);
                if (slotIndex == null) {
                    return;
                }
            }
            if (!byLongPress) {
                FloatAnimation slotPreviewAnimation = this.mSlotPreviewManager.getSlotPreviewAnimation();
                slotPreviewAnimation.reset();
                this.mSlotPreviewView.setAnimation(slotPreviewAnimation);
                slotPreviewAnimation.start();
            }
            this.mSlotPreviewManager.enterPreviewMode();
            this.mSlotPreviewManager.setPreviewViewScale(getPreviewViewScale(event.getPressure()));
            if (byLongPress) {
                this.mSlotPreviewManager.setPreviewState(SlotPreviewState.PREVIEW);
            }
            showSlotPreView(x, y, slotIndex);
        }
    }

    private void leavePreviewMode() {
        if (this.mSlotPreviewManager.inPreviewMode()) {
            updateProgressBar();
            this.mSlotPreviewView.setAnimation(null);
            this.mHandler.removeCallbacks(this.mSlotPreviewTask);
            this.mSlotPreviewManager.leavePreviewMode();
            this.mSlotPreviewEvent = null;
            this.mSlotRender.setPressedIndex(null);
            this.mPressedIndex = null;
        }
    }

    public void onScroll(ItemCoordinate index) {
        disableRender(true);
        super.onScroll(index);
    }

    public void onTouchUp(MotionEvent event) {
        super.onTouchUp(event);
        disableRender(false);
        if (supportPreviewMode()) {
            this.mSlotPreviewPhotoManager.onTouchEvent(event);
        } else if (this.mSlotPreviewManager.inPreviewMode()) {
            if (this.mPressedIndex != null && SlotPreviewState.PREVIEW.equal(this.mSlotPreviewManager.getPreviewState())) {
                int index = getItemIndex(this.mPressedIndex);
                MediaItem item = getMediaItem(index);
                if (item != null) {
                    this.mSlotView.setIndexUp(this.mPressedIndex);
                    pickPhotoWithAnimation(this.mSlotView, 100, this.mPressedIndex, index, item);
                }
            }
            leavePreviewMode();
        }
    }

    public void onTouchDown(MotionEvent event) {
        super.onTouchDown(event);
        disableRender(false);
        if (supportPreviewMode()) {
            this.mSlotPreviewPhotoManager.onTouchEvent(event);
        } else if (supportSlotPreviewMode(event.getPressure())) {
            enterPreviewMode(event, false);
        }
    }

    public void onCancel() {
        disableRender(false);
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                TimeBucketPage.this.leavePreviewMode();
            }
        }, 200);
    }

    public void onResetMainView() {
        this.mMainViewOffsetTop = 0;
        this.mMainViewReseter.forceStop();
        updateProgressBar();
        this.mRootPane.requestLayout();
    }

    protected void onGLRootLayout(int left, int top, int right, int bottom) {
        int relativeNavigationBarHeight;
        int width = right - left;
        int height = bottom - top;
        boolean isLandScape = !LayoutHelper.isPort();
        boolean isPort = LayoutHelper.isPort();
        int actionBarHeight = this.mActionBar.getActionBarHeight();
        int statusBarHeight = MultiWindowStatusHolder.isInMultiMaintained() ? 0 : LayoutHelper.getStatusBarHeight();
        int navigationBarHeight = LayoutHelper.getNavigationBarHeight();
        int paddingTop = actionBarHeight + statusBarHeight;
        int paddingRight = isPort ? 0 : navigationBarHeight;
        if (!isPort || MultiWindowStatusHolder.isInMultiWindowMode()) {
            relativeNavigationBarHeight = 0;
        } else {
            relativeNavigationBarHeight = navigationBarHeight;
        }
        int paddingBottom = relativeNavigationBarHeight;
        boolean more = this.mMainViewReseter.calculate(AnimationTime.get());
        int networkHeight = 0;
        if (this.mNetworkTipsView != null) {
            more |= this.mNetworkAnim.calculate(AnimationTime.get());
            this.mNetworkTipsView.measureSize(width, height);
            int networkTipsHeight = this.mNetworkTipsView.getMeasuredHeight();
            int heghtElipsed = (int) (this.mNetworkAnim.get() * ((float) networkTipsHeight));
            this.mNetworkTipsView.layout(0, paddingTop - heghtElipsed, width, (paddingTop + networkTipsHeight) - heghtElipsed);
            networkHeight = networkTipsHeight - heghtElipsed;
        }
        int offset = this.mMainViewOffsetTop + networkHeight;
        paddingTop += offset;
        this.mTopMessageView.layout(left, paddingTop - offset, right, paddingTop);
        this.mTopCover.layout(left, 0, right, paddingTop);
        this.mNavigationBarCover.layout(left, height - relativeNavigationBarHeight, right, height);
        paddingBottom = (paddingBottom + networkHeight) + adaptForDefaultLandOrientationProduct();
        if (this.mIsLayoutRtl) {
            this.mSlotView.layout(this.mLayoutSpec.local_camera_page_right_padding, paddingTop, (right - (this.mLayoutSpec.time_line_width + this.mLayoutSpec.local_camera_page_left_padding)) - (isLandScape ? LayoutHelper.getNavigationBarHeight() : 0), (height + offset) - paddingBottom);
            this.mScrollBar.layout(left, paddingTop, ((right - paddingRight) - this.mLayoutSpec.time_line_width) + (this.mLayoutSpec.local_camera_page_right_padding - this.mLayoutSpec.local_camera_page_left_padding), (height + offset) - paddingBottom);
        } else {
            this.mSlotView.layout(this.mLayoutSpec.time_line_width + this.mLayoutSpec.local_camera_page_left_padding, paddingTop, (right - this.mLayoutSpec.local_camera_page_right_padding) - paddingRight, (height + offset) - paddingBottom);
            this.mScrollBar.layout((this.mLayoutSpec.time_line_width + left) - (this.mLayoutSpec.local_camera_page_right_padding - this.mLayoutSpec.local_camera_page_left_padding), paddingTop, right - paddingRight, (height + offset) - paddingBottom);
        }
        this.mHandler.post(new Runnable() {
            public void run() {
                TimeBucketPage.this.leavePreviewMode();
            }
        });
        this.mActionbarView.layout(0, statusBarHeight, isPort ? width : width - navigationBarHeight, statusBarHeight + actionBarHeight);
        if (more) {
            this.mRootPane.requestLayout();
        }
    }

    private int adaptForDefaultLandOrientationProduct() {
        if (!LayoutHelper.isDefaultLandOrientationProduct() || LayoutHelper.isPort() || MultiWindowStatusHolder.isInMultiWindowMode()) {
            return 0;
        }
        return LayoutHelper.getNavigationBarHeightForDefaultLand();
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

    protected AlbumDataLoader onCreateDataLoader(MediaSet mediaSet) {
        if (this.mTimeBucketItemsDataLoader == null) {
            this.mTimeBucketItemsDataLoader = new TimeBucketItemsDataLoader(this.mHost.getGalleryContext(), mediaSet);
            setDataLoader(this.mTimeBucketItemsDataLoader);
            this.mTimeBucketItemsDataLoader.setLoadCountListener(new LoadCountListener() {
                public void onLoadCountChange(int count) {
                    if (count == 0) {
                        TimeBucketPage.this.showEmptyAlbum();
                    } else {
                        TimeBucketPage.this.hideEmptyAlbum();
                    }
                }
            });
        }
        return this.mTimeBucketItemsDataLoader;
    }

    protected boolean onItemSelected(Action action) {
        ReportToBigData.reportActionForFragment("FromTimeView", action, this.mSelectionManager);
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
            case 2:
                launchDymaicApk(action);
                return true;
            case 3:
                MediaSetRegion region = this.mTimeBucketItemsDataLoader.getRegion();
                Bundle bundle = new Bundle();
                bundle.putBoolean("KEY_INIT_VISIBLE_MAP_FOR_ALL", true);
                bundle.putBoolean("KEY_INIT_VISIBLE_MAP_NO_PIC", region.mNoPictureHasLatLng);
                bundle.putParcelable("KEY_INIT_VISIBLE_MAP_RECT", region.mRectF);
                GalleryUtils.startMapAlbum(this.mHost.getActivity(), bundle);
                ReportToBigData.report(14, String.format("{EnterMapMethod:%s}", new Object[]{"FromToolBar"}));
                return true;
            default:
                return super.onItemSelected(action);
        }
    }

    private boolean createEmptyView() {
        RelativeLayout root = (RelativeLayout) this.mHost.getActivity().findViewById(R.id.gallery_root);
        if (root == null) {
            return false;
        }
        this.mEmptyAlbumLayout = (RelativeLayout) ((LayoutInflater) this.mHost.getActivity().getSystemService("layout_inflater")).inflate(R.layout.empty_localcamera, root, false);
        this.mEmptyAlbumTips = (TextView) this.mEmptyAlbumLayout.findViewById(R.id.no_picture_name_horizon);
        this.mEmptyAlbumTips.setText(R.string.no_media_title);
        this.mTakePhotoButton = (Button) this.mEmptyAlbumLayout.findViewById(R.id.photoshare_button1);
        this.mTakePhotoButton.setText(R.string.take_a_shot);
        this.mTakePhotoButton.setVisibility(0);
        this.mTakePhotoButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Activity activity = TimeBucketPage.this.mHost.getActivity();
                GalleryUtils.startActivityCatchSecurityEx(activity, GalleryUtils.getStartCameraIntent(activity));
            }
        });
        this.mSyncPhotoButton = (Button) this.mEmptyAlbumLayout.findViewById(R.id.photoshare_button2);
        this.mSyncPhotoButton.setText(R.string.enable_photo_synchronized);
        this.mSyncPhotoButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                PhotoShareUtils.login(TimeBucketPage.this.mHost.getActivity());
            }
        });
        setSyncPhotoButtonVisibility();
        setFootButtonLayoutParams();
        this.mEmptyAlbumLayout.setPadding(0, computeEmptyAlbumLayoutTopPadding(), 0, 0);
        this.mEmptyAlbumLayout.setGravity(1);
        root.addView(this.mEmptyAlbumLayout);
        this.mMultiWindowModeChangeListener = new IMultiWindowModeChangeListener() {
            public void multiWindowModeChangeCallback(boolean isInMultiWindowMode) {
                if (TimeBucketPage.this.mEmptyAlbumLayout != null && TimeBucketPage.this.mEmptyAlbumLayout.getVisibility() == 0) {
                    TimeBucketPage.this.setEmptyAlbumLayoutVisibility(false);
                    TimeBucketPage.this.setEmptyAlbumLayoutParams();
                }
            }
        };
        MultiWindowStatusHolder.registerMultiWindowModeChangeListener(this.mMultiWindowModeChangeListener, false);
        return true;
    }

    private void destroyEmptyView() {
        if (this.mEmptyAlbumLayout != null) {
            RelativeLayout root = (RelativeLayout) this.mHost.getActivity().findViewById(R.id.gallery_root);
            if (root != null) {
                root.removeView(this.mEmptyAlbumLayout);
                MultiWindowStatusHolder.unregisterMultiWindowModeChangeListener(this.mMultiWindowModeChangeListener);
                this.mMultiWindowModeChangeListener = null;
                this.mEmptyAlbumLayout = null;
            }
        }
    }

    protected void showEmptyAlbum() {
        if (this.mEmptyAlbumLayout != null || createEmptyView()) {
            setEmptyAlbumLayoutVisibility(true);
            setSyncPhotoButtonVisibility();
            setEmptyAlbumLayoutParams();
            if (this.mIsActive) {
                this.mHost.requestFeature(298);
            }
        }
    }

    protected void hideEmptyAlbum() {
        if (this.mEmptyAlbumLayout != null) {
            this.mEmptyAlbumLayout.setVisibility(8);
            if (this.mIsActive) {
                this.mHost.requestFeature(296);
            }
        }
    }

    private void showLoadingTips() {
        ViewGroup tips = (ViewGroup) this.mHost.getActivity().findViewById(R.id.loading_tips);
        if (tips == null) {
            ViewStub stub = (ViewStub) this.mHost.getActivity().findViewById(R.id.loading_tips_stub);
            if (stub != null) {
                stub.inflate();
            }
            tips = (ViewGroup) this.mHost.getActivity().findViewById(R.id.loading_tips);
        }
        tips.setVisibility(0);
        int paddingTop = LayoutHelper.getStatusBarHeight() + this.mHost.getGalleryActionBar().getActionBarHeight();
        if (LayoutHelper.isPort()) {
            tips.setPadding(0, paddingTop, 0, LayoutHelper.getNavigationBarHeight());
        } else {
            tips.setPadding(0, paddingTop, 0, 0);
        }
    }

    protected void hideLoadingTips() {
        ViewGroup tips = (ViewGroup) this.mHost.getActivity().findViewById(R.id.loading_tips);
        if (tips != null) {
            tips.setVisibility(8);
        }
    }

    protected void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        this.mWindowHeight = GalleryUtils.dpToPixel(config.screenHeightDp);
        if (this.mEmptyAlbumLayout != null) {
            setEmptyAlbumLayoutParams();
            setEmptyAlbumLayoutVisibility(false);
        }
    }

    private void setEmptyAlbumLayoutVisibility(boolean force) {
        if (force) {
            this.mEmptyAlbumLayout.setVisibility(0);
        }
    }

    protected Rect getAnimSlotRect() {
        return this.mSlotView.getAnimRect();
    }

    protected void enterSelectionMode() {
        super.enterSelectionMode();
        this.mActionbarView.setVisibility(1);
    }

    protected void leaveSelectionMode() {
        this.mActionbarView.setVisibility(0);
        this.mActionBar.leaveCurrentMode();
        TabMode tm = this.mActionBar.enterTabMode(false);
        tm.setMenu(Math.min(this.mDisplayMenu.length - 1, MAX_ITEMS_COUNT), this.mDisplayMenu);
        tm.show();
        this.mActionbarView.refreshTexture();
        this.mHost.requestFeature(296);
        this.mRootPane.requestLayout();
    }

    protected void updateMenu(boolean isSizeZero) {
        ActionBarStateBase actionbar = this.mActionBar.getCurrentMode();
        if (isSizeZero) {
            if (actionbar instanceof TabMode) {
                actionbar.changeAction(Action.ACTION_ID_SLIDESHOW, Action.ACTION_ID_NONE);
            }
            this.mDisplayMenu[mSlideShowIndex] = Action.NONE;
            return;
        }
        if (actionbar instanceof TabMode) {
            this.mActionBar.getCurrentMode().changeAction(Action.ACTION_ID_NONE, Action.ACTION_ID_SLIDESHOW);
        }
        this.mDisplayMenu[mSlideShowIndex] = Action.SLIDESHOW;
    }

    protected SlotView getSlotView() {
        return this.mSlotView;
    }

    protected void onResume() {
        TraceController.beginSection("TimeBucketPage.onResume");
        super.onResume();
        Activity activity = this.mHost.getActivity();
        if (activity instanceof GalleryMain) {
            ((GalleryMain) activity).addPageChangedListener(this);
        }
        if (RefreshHelper.getSyncFailedStatus()) {
            RefreshHelper.refreshAlbum(System.currentTimeMillis());
        }
        this.mPressedIndex = null;
        if (needLazyLoad()) {
            showLoadingTips();
        }
        TraceController.endSection();
        this.mPressureValueThreshold = Math.max(0.13f, GalleryUtils.getPressureResponseThreshold(this.mHost.getActivity()) * 0.9f);
        MultiWindowStatusHolder.registerMultiWindowModeChangeListener(this.mMultiWindowModeChangeListener, false);
        this.mSlotPreviewPhotoManager.leavePreviewMode();
        GalleryUtils.updateSupportPressurePreview(this.mHost.getActivity());
        this.mAsynchronousThread = new Thread(new Runnable() {
            public void run() {
                TimeBucketPage.this.updateProgressBar();
                TimeBucketPage.this.onStatusChanged(TimeBucketPage.this.mCloudManager, 0);
                TimeBucketPage.this.mTopMessageView.setVisibility(0);
            }
        });
        this.mAsynchronousThread.start();
    }

    protected void onPause() {
        super.onPause();
        Activity activity = this.mHost.getActivity();
        if (activity instanceof GalleryMain) {
            ((GalleryMain) activity).removePageChangedListener(this);
        }
        try {
            this.mAsynchronousThread.join(200);
        } catch (InterruptedException e) {
            GalleryLog.i(TAG, "asynchronous thread get exception");
        }
        if (this.mActionBar.getCurrentMode() != null) {
            this.mActionBar.getCurrentMode().hide();
        }
        leavePreviewMode();
        this.mTopMessageView.setVisibility(1);
        if (getProgressbar() != null) {
            this.mProgressbar.setVisibility(4);
        }
        this.mProgressBarScreenNail.recycle();
    }

    protected void onDestroy() {
        super.onDestroy();
        destroyEmptyView();
    }

    protected boolean needLazyLoad() {
        return !this.mUserHaveFirstLook;
    }

    protected void onUserSelected(boolean selected) {
        boolean z = false;
        if (selected) {
            if (this.mDataLoader != null) {
                this.mDataLoader.unfreeze();
                if (this.mIsActive) {
                    this.mDataLoader.resume();
                    if (this.mDataLoader.size() == 0) {
                        z = true;
                    }
                    updateMenu(z);
                }
            }
            this.mUserHaveFirstLook = true;
            this.mRootPane.requestLayout();
        } else if (this.mDataLoader != null) {
            this.mDataLoader.freeze();
        }
    }

    protected void onStateResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 100:
                if (data != null) {
                    this.mFocusIndex = data.getIntExtra("return-index-hint", 0);
                    this.mSlotView.invalidate();
                    GalleryLog.printDFXLog("TimeBucketPage REQUEST_PHOTO called  for DFX");
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
            case 107:
                if (resultCode == -1) {
                    this.mMenuExecutor.startAction(R.id.action_paste, R.string.paste, null, false, true, Style.PASTE_STYLE, null, data.getExtras());
                    break;
                }
                break;
            default:
                super.onStateResult(requestCode, resultCode, data);
                break;
        }
    }

    public void onBlurWallpaperChanged() {
        this.mTopCover.textureDirty();
    }

    public void onSelectionChange(Path path, boolean selected) {
        super.onSelectionChange(path, selected);
        this.mActionbarView.refreshTexture();
    }

    protected SlotRenderInterface getSlotRenderInterface() {
        return this.mSlotRender;
    }

    private float onGetFragmentPadding() {
        return (float) this.mMainViewOffsetTop;
    }

    public void onHeightUpdated(int height) {
        this.mMainViewOffsetTop = height;
        this.mRootPane.requestLayout();
        updateProgressBar();
    }

    public void resetMainView(boolean down) {
        if (this.LIMIT_STABLE < this.mMainViewOffsetTop || (!down && this.LIMIT_STABLE >= this.mMainViewOffsetTop)) {
            this.mMainViewReseter.start();
        }
    }

    public void onScrollOver(float distance) {
        boolean z = false;
        onHeightUpdated(Utils.clamp(this.mMainViewOffsetTop - Utils.getElasticInterpolation((int) distance, this.mMainViewOffsetTop, 250), 0, this.LIMIT_MAX));
        if (distance < 0.0f) {
            z = true;
        }
        this.mIsPullingdown = z;
    }

    public void onScrollOverBegin() {
        this.mMainViewReseter.forceStop();
        this.mTopMessageView.setMessage(this.mCloudManager.getUploadingString());
        this.mTopMessageView.setUploading(this.mCloudManager.isUploading());
    }

    public void onScrollOverDone() {
        ReportToBigData.report(150);
        GalleryLog.v("photoshareLogTag", "onScrollOverDone");
        resetMainView(this.mIsPullingdown);
        if (this.mCloudManager.isCloudAutoUploadSwitchOpen() || RefreshHelper.getSyncFailedStatus()) {
            RefreshHelper.refreshAlbum(System.currentTimeMillis());
        }
    }

    public float getOffset() {
        return onGetFragmentPadding();
    }

    public void onPageChanged(int pageIndex) {
        if (pageIndex == 0) {
            this.mActionbarView.refreshTexture();
        }
    }

    private void buildTimeBucketActionBar() {
        List<Action> list = new ArrayList();
        list.add(Action.MAP);
        PackageManager pm = this.mHost.getActivity().getApplicationContext().getPackageManager();
        for (DynApkItem item : this.mDynApkList) {
            String packageName = item.packageName;
            if (!(item.action == Action.NONE || packageName == null || packageName.length() <= 0)) {
                try {
                    ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
                    if (!(info == null || (info.flags & 1) == 0)) {
                        list.add(item.action);
                    }
                } catch (NameNotFoundException e) {
                }
            }
        }
        list.add(Action.SLIDESHOW);
        mSlideShowIndex = list.size() - 1;
        list.add(Action.SETTINGS);
        this.mDisplayMenu = (Action[]) list.toArray(new Action[list.size()]);
    }

    private void launchDymaicApk(Action action) {
        int i = 0;
        while (i < this.mDynApkList.length) {
            if (action == this.mDynApkList[i].action) {
                try {
                    this.mHost.getActivity().startActivity(buildDymaicIntent(i));
                    return;
                } catch (Exception e) {
                    GalleryLog.w(TAG, "start Activity failed! " + this.mDynApkList[i].activity);
                }
            } else {
                GalleryLog.w(TAG, "no action found " + this.mDynApkList[i].activity);
                i++;
            }
        }
    }

    private Intent buildDymaicIntent(int index) {
        Intent intent = new Intent();
        if ("com.tencent.ttpic4huawei".equals(this.mDynApkList[index].packageName)) {
            intent.setPackage("com.tencent.ttpic4huawei");
            intent.setAction("android.intent.action.SEND_MULTIPLE");
            intent.addCategory("com.tencent.ttpic4huawei.intent.category.OPENAPI");
            intent.putExtra("invoked_to_module", Uri.parse("pitu://TTPTCOLLAGE"));
        } else {
            intent.setAction(this.mDynApkList[index].activity);
        }
        return intent;
    }

    private int computeEmptyAlbumLayoutTopPadding() {
        int topPadding = (!MultiWindowStatusHolder.isInMultiWindowMode() || this.mWindowHeight == 0) ? LayoutHelper.isPort() ? GalleryUtils.getHeightPixels() : GalleryUtils.getWidthPixels() : this.mWindowHeight;
        return (int) (((float) topPadding) * 0.3f);
    }

    private Rect getPreviewViewLayout(float x, float y) {
        boolean pressureSwitchOn = GalleryUtils.isSupportPressureResponse(this.mHost.getActivity());
        float scale = pressureSwitchOn ? this.mSlotPreviewManager.getPreviewViewScale() : WMElement.CAMERASIZEVALUE1B1;
        float progress = pressureSwitchOn ? this.mSlotPreviewManager.getSlotPreviewAnimation().get() : WMElement.CAMERASIZEVALUE1B1;
        int defaultWidth = this.mSlotView.getSlotWidthByMonth();
        int defaultHeight = this.mSlotView.getSlotHeightByMonth();
        int slotPreviewViewWidth = defaultWidth + ((int) (((float) (this.mSlotView.getSlotWidthByWeek() - defaultWidth)) * scale));
        int slotPreviewViewHeight = defaultHeight + ((int) (((float) (this.mSlotView.getSlotHeightByWeek() - defaultHeight)) * scale));
        int slotPreviewGap = (int) (((float) (defaultHeight + slotPreviewViewHeight)) * progress);
        Rect slotViewBounds = this.mSlotView.bounds();
        Rect rect = new Rect();
        int width = LayoutHelper.isPort() ? GalleryUtils.getWidthPixels() : GalleryUtils.getHeightPixels() - LayoutHelper.getNavigationBarHeight();
        rect.left = (((int) x) - (slotPreviewViewWidth / 2)) + slotViewBounds.left;
        rect.left = Math.max(rect.left, 0);
        rect.top = ((((int) y) - slotPreviewGap) + slotViewBounds.top) - ((int) ((((float) defaultHeight) / 2.0f) * (WMElement.CAMERASIZEVALUE1B1 - progress)));
        if (rect.top < LayoutHelper.getStatusBarHeight()) {
            rect.top = LayoutHelper.getStatusBarHeight();
        }
        rect.right = rect.left + slotPreviewViewWidth;
        if (rect.right > width) {
            rect.right = width;
            rect.left = rect.right - slotPreviewViewWidth;
        }
        rect.bottom = rect.top + slotPreviewViewHeight;
        return rect;
    }

    private void setEmptyAlbumLayoutParams() {
        LayoutParams emptyAlbumLayoutParams = (LayoutParams) this.mEmptyAlbumLayout.getLayoutParams();
        int emptyAlbumTopPadding = computeEmptyAlbumLayoutTopPadding();
        emptyAlbumLayoutParams.setMarginEnd(LayoutHelper.isPort() ? 0 : LayoutHelper.getNavigationBarHeight());
        this.mEmptyAlbumLayout.setLayoutParams(emptyAlbumLayoutParams);
        this.mEmptyAlbumLayout.setPadding(0, emptyAlbumTopPadding, 0, 0);
        this.mEmptyAlbumLayout.setGravity(1);
        setFootButtonLayoutParams();
    }

    private void setFootButtonLayoutParams() {
        if (this.mTakePhotoButton != null) {
            Resources res = this.mTakePhotoButton.getResources();
            LayoutParams takePhotoButtonParams = (LayoutParams) this.mTakePhotoButton.getLayoutParams();
            this.mTakePhotoButton.getLayoutParams().width = LayoutHelper.getScreenShortSide() - (res.getDimensionPixelSize(R.dimen.photoshare_login_button_leftandright_padding) * 2);
            if (this.mSyncPhotoButton == null || this.mSyncPhotoButton.getVisibility() != 0) {
                takePhotoButtonParams.topMargin = GalleryUtils.dpToPixel(4);
                takePhotoButtonParams.bottomMargin = res.getDimensionPixelSize(R.dimen.photoshare_login_bottom_padding);
                if (LayoutHelper.isPort()) {
                    takePhotoButtonParams.bottomMargin += LayoutHelper.getNavigationBarHeight();
                }
                if (!(!LayoutHelper.isDefaultLandOrientationProduct() || LayoutHelper.isPort() || MultiWindowStatusHolder.isInMultiWindowMode())) {
                    takePhotoButtonParams.bottomMargin += LayoutHelper.getNavigationBarHeightForDefaultLand();
                }
            } else {
                LayoutParams syncPhotoButtonParams = (LayoutParams) this.mSyncPhotoButton.getLayoutParams();
                syncPhotoButtonParams.bottomMargin = this.mSyncPhotoButton.getResources().getDimensionPixelSize(R.dimen.photoshare_login_bottom_padding);
                if (LayoutHelper.isPort()) {
                    syncPhotoButtonParams.bottomMargin += LayoutHelper.getNavigationBarHeight();
                }
                if (!(!LayoutHelper.isDefaultLandOrientationProduct() || LayoutHelper.isPort() || MultiWindowStatusHolder.isInMultiWindowMode())) {
                    syncPhotoButtonParams.bottomMargin += LayoutHelper.getNavigationBarHeightForDefaultLand();
                }
                syncPhotoButtonParams.topMargin = GalleryUtils.dpToPixel(16);
                this.mSyncPhotoButton.getLayoutParams().width = this.mTakePhotoButton.getLayoutParams().width;
                this.mSyncPhotoButton.setLayoutParams(syncPhotoButtonParams);
                takePhotoButtonParams.bottomMargin = 0;
            }
            this.mTakePhotoButton.setLayoutParams(takePhotoButtonParams);
        }
    }

    private void setSyncPhotoButtonVisibility() {
        if ((TextUtils.isEmpty(PhotoShareUtils.getLoginUserId()) || !PhotoShareUtils.isCloudPhotoSwitchOpen()) && PhotoShareUtils.isSupportPhotoShare()) {
            this.mSyncPhotoButton.setVisibility(0);
        } else {
            this.mSyncPhotoButton.setVisibility(8);
        }
    }

    public void onNavigationBarChanged(boolean show, int height) {
        super.onNavigationBarChanged(show, height);
        if (this.mEmptyAlbumLayout != null && this.mEmptyAlbumLayout.getVisibility() == 0) {
            setEmptyAlbumLayoutParams();
            setEmptyAlbumLayoutVisibility(false);
        }
    }
}
