package com.huawei.gallery.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.Process;
import android.view.Display;
import android.view.View;
import com.android.gallery3d.R;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.BitmapScreenNail;
import com.android.gallery3d.ui.BitmapTileProvider;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.ui.TileImageViewAdapter;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.InterruptableOutputStream;
import com.android.gallery3d.util.ReportToBigData;
import com.android.gallery3d.util.ThreadPool.CancelListener;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionMode;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.ui.BaseCropView;
import com.huawei.gallery.ui.CropView;
import com.huawei.gallery.ui.SimpleCropView;
import com.huawei.gallery.util.BundleUtils;
import com.huawei.gallery.util.LayoutHelper;
import com.huawei.gallery.util.MyPrinter;
import com.huawei.gallery.util.UIUtils;
import com.huawei.watermark.ui.WMComponent;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import tmsdk.common.module.update.UpdateConfig;

@TargetApi(19)
public class CropImageActivity extends GLActivity {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    private static final MyPrinter LOG = new MyPrinter("CropImageActivity");
    private static final String PATH_WALLPAPER = (Environment.getDataDirectory() + "/skin/wallpaper");
    private final FutureListener<BitmapRegionDecoder> bitmapRegionDecoderFutureListener = new FutureListener<BitmapRegionDecoder>() {
        public void onFutureDone(Future<BitmapRegionDecoder> future) {
            CropImageActivity.this.mLoadTask = null;
            BitmapRegionDecoder decoder = (BitmapRegionDecoder) future.get();
            if (future.isCancelled()) {
                if (decoder != null) {
                    decoder.recycle();
                }
            } else if (decoder == null) {
                CropImageActivity.this.mLoadBitmapTask = CropImageActivity.this.loadBitmapData(CropImageActivity.this.thumbListener);
            } else {
                CropImageActivity.this.mMainHandler.sendMessage(CropImageActivity.this.mMainHandler.obtainMessage(1, decoder));
            }
        }
    };
    private ActionMode mActionMode;
    private Bitmap mBitmap;
    private BitmapScreenNail mBitmapScreenNail;
    private BaseCropView mCropView;
    private Bundle mData = new Bundle();
    private boolean mDoFaceDetection;
    private int mFeature = 268;
    private int mFlags = 0;
    private GLRoot mGLRootView;
    private int mLaunchMode = 0;
    private Future<Bitmap> mLoadBitmapTask;
    private Future<BitmapRegionDecoder> mLoadTask;
    private Handler mMainHandler;
    private MediaItem mMediaItem;
    int mOutputH;
    int mOutputW;
    private ProgressDialog mProgressDialog;
    private BitmapRegionDecoder mRegionDecoder;
    private Future<Intent> mSaveTask;
    private String mSetAsThemeResult = "";
    Point mSize;
    private int mState = 0;
    private int mSubMode = 0;
    private boolean mUseRegionDecoder = false;
    private View mView;
    private final FutureListener<Bitmap> thumbListener = new FutureListener<Bitmap>() {
        public void onFutureDone(Future<Bitmap> future) {
            CropImageActivity.this.mLoadBitmapTask = null;
            Bitmap bitmap = (Bitmap) future.get();
            if (future.isCancelled()) {
                if (bitmap != null) {
                    bitmap.recycle();
                }
                return;
            }
            CropImageActivity.this.mMainHandler.sendMessage(CropImageActivity.this.mMainHandler.obtainMessage(2, bitmap));
        }
    };

    private static class LoadBitmapDataTask extends BaseJob<Bitmap> {
        MediaItem mItem;

        public LoadBitmapDataTask(MediaItem item) {
            this.mItem = item;
        }

        public Bitmap run(JobContext jc) {
            return !GalleryUtils.supportSetas(this.mItem) ? null : (Bitmap) this.mItem.requestImage(1).run(jc);
        }

        public String workContent() {
            return "request TYPE_THUMBNAIL image for " + this.mItem.getFilePath();
        }
    }

    private static class LoadDataTask extends BaseJob<BitmapRegionDecoder> {
        MediaItem mItem;

        public LoadDataTask(MediaItem item) {
            this.mItem = item;
        }

        public BitmapRegionDecoder run(JobContext jc) {
            if (!GalleryUtils.supportSetas(this.mItem)) {
                return null;
            }
            Job<BitmapRegionDecoder> job = this.mItem.requestLargeImage();
            if (job == null) {
                return null;
            }
            return (BitmapRegionDecoder) job.run(jc);
        }

        public String workContent() {
            return "create region decoder for " + this.mItem.getFilePath();
        }
    }

    private static class OutputStreamInterrupter implements CancelListener {
        private InterruptableOutputStream mIos;

        OutputStreamInterrupter(InterruptableOutputStream ios) {
            this.mIos = ios;
        }

        public void onCancel() {
            if (this.mIos != null) {
                this.mIos.interrupt();
            }
        }
    }

    private class SaveOutput implements Job<Intent> {
        private final Rect mCropRect;

        public SaveOutput(Rect cropRect) {
            this.mCropRect = cropRect;
        }

        public Intent run(JobContext jc) {
            Rect rect = this.mCropRect;
            Intent result = new Intent();
            Bitmap bitmap = null;
            Bundle extra = new Bundle();
            Uri uri = null;
            Intent intent = CropImageActivity.this.getIntent();
            if (!(intent == null || intent.getExtras() == null)) {
                extra = intent.getExtras();
                uri = (Uri) extra.getParcelable("output");
            }
            if (uri != null) {
                if (jc.isCancelled()) {
                    return null;
                }
                bitmap = CropImageActivity.this.getCroppedImage(rect);
                if (!CropImageActivity.this.saveBitmapToUri(jc, bitmap, uri)) {
                    return null;
                }
                result.setAction(uri.toString());
            }
            if (extra.getBoolean("return-data", false)) {
                if (jc.isCancelled()) {
                    return null;
                }
                if (bitmap == null) {
                    bitmap = CropImageActivity.this.getCroppedImage(rect);
                }
                result.putExtra(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH, bitmap);
            }
            if ((CropImageActivity.this.mFlags & 1) != 0) {
                if (jc.isCancelled()) {
                    return null;
                }
                if (bitmap == null) {
                    bitmap = CropImageActivity.this.getCroppedImage(rect);
                }
                CropImageActivity.this.saveLockscreenBitmap(jc, bitmap, false);
            }
            if (setHomeScreenBitmap(jc, bitmap, rect)) {
                return null;
            }
            result.putExtra("set-as-theme-result", CropImageActivity.this.mSetAsThemeResult);
            return result;
        }

        private boolean setHomeScreenBitmap(JobContext jc, Bitmap cropped, Rect rect) {
            boolean needReturn = false;
            if ((CropImageActivity.this.mFlags & 6) == 0) {
                return false;
            }
            if (jc.isCancelled()) {
                needReturn = true;
            }
            if (cropped == null) {
                cropped = CropImageActivity.this.getCroppedImage(rect);
            }
            if (!GalleryUtils.checkDiskSpace(Environment.getDataDirectory().getPath(), ((long) cropped.getByteCount()) + UpdateConfig.UPDATE_FLAG_DEEPCLEAN_SOFT_PATH_LIST)) {
                needReturn = true;
            }
            CropImageActivity.this.saveLockscreenBitmap(jc, cropped, true);
            if (CropImageActivity.this.setAsWallpaper(jc, cropped)) {
                return needReturn;
            }
            return true;
        }

        public boolean needDecodeVideoFromOrigin() {
            return false;
        }

        public boolean isHeavyJob() {
            return true;
        }

        public String clazz() {
            return getClass().getName();
        }

        public String workContent() {
            return String.format("save croped image file %s, flags: %s", new Object[]{CropImageActivity.this.mMediaItem.getFilePath(), Integer.valueOf(CropImageActivity.this.mFlags)});
        }
    }

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
            iArr[Action.ALL.ordinal()] = 13;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Action.BACK.ordinal()] = 14;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Action.CANCEL_DETAIL.ordinal()] = 15;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Action.COLLAGE.ordinal()] = 16;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Action.COMMENT.ordinal()] = 17;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Action.COPY.ordinal()] = 18;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Action.DEALL.ordinal()] = 19;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Action.DEL.ordinal()] = 20;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Action.DETAIL.ordinal()] = 21;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[Action.DYNAMIC_ALBUM.ordinal()] = 22;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[Action.EDIT.ordinal()] = 23;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[Action.EDIT_COMMENT.ordinal()] = 24;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[Action.GOTO_GALLERY.ordinal()] = 25;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[Action.HIDE.ordinal()] = 26;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[Action.INFO.ordinal()] = 27;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[Action.KEYGUARD_LIKE.ordinal()] = 28;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[Action.KEYGUARD_NOT_LIKE.ordinal()] = 29;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[Action.LOOPPLAY.ordinal()] = 30;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[Action.MAP.ordinal()] = 31;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[Action.MENU.ordinal()] = 32;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[Action.MORE_EDIT.ordinal()] = 33;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[Action.MOVE.ordinal()] = 34;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[Action.MOVEIN.ordinal()] = 35;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[Action.MOVEOUT.ordinal()] = 36;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[Action.MULTISCREEN.ordinal()] = 37;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[Action.MULTISCREEN_ACTIVITED.ordinal()] = 38;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[Action.MULTI_SELECTION.ordinal()] = 39;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[Action.MULTI_SELECTION_ON.ordinal()] = 40;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[Action.MYFAVORITE.ordinal()] = 41;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[Action.NO.ordinal()] = 1;
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
            iArr[Action.OK.ordinal()] = 2;
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
            iArr[Action.PHOTOSHARE_DELETE.ordinal()] = 53;
        } catch (NoSuchFieldError e48) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD.ordinal()] = 54;
        } catch (NoSuchFieldError e49) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOADING.ordinal()] = 55;
        } catch (NoSuchFieldError e50) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD_START.ordinal()] = 56;
        } catch (NoSuchFieldError e51) {
        }
        try {
            iArr[Action.PHOTOSHARE_EDITSHARE.ordinal()] = 57;
        } catch (NoSuchFieldError e52) {
        }
        try {
            iArr[Action.PHOTOSHARE_EMAIL.ordinal()] = 58;
        } catch (NoSuchFieldError e53) {
        }
        try {
            iArr[Action.PHOTOSHARE_LINK.ordinal()] = 59;
        } catch (NoSuchFieldError e54) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_DOWNLOAD.ordinal()] = 60;
        } catch (NoSuchFieldError e55) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_UPLOAD.ordinal()] = 61;
        } catch (NoSuchFieldError e56) {
        }
        try {
            iArr[Action.PHOTOSHARE_MESSAGE.ordinal()] = 62;
        } catch (NoSuchFieldError e57) {
        }
        try {
            iArr[Action.PHOTOSHARE_MOVE.ordinal()] = 63;
        } catch (NoSuchFieldError e58) {
        }
        try {
            iArr[Action.PHOTOSHARE_MULTI_DOWNLOAD.ordinal()] = 64;
        } catch (NoSuchFieldError e59) {
        }
        try {
            iArr[Action.PHOTOSHARE_NOT_THIS_PERSON.ordinal()] = 65;
        } catch (NoSuchFieldError e60) {
        }
        try {
            iArr[Action.PHOTOSHARE_PAUSE.ordinal()] = 66;
        } catch (NoSuchFieldError e61) {
        }
        try {
            iArr[Action.PHOTOSHARE_REMOVE_PEOPLE_TAG.ordinal()] = 67;
        } catch (NoSuchFieldError e62) {
        }
        try {
            iArr[Action.PHOTOSHARE_RENAME.ordinal()] = 68;
        } catch (NoSuchFieldError e63) {
        }
        try {
            iArr[Action.PHOTOSHARE_SETTINGS.ordinal()] = 69;
        } catch (NoSuchFieldError e64) {
        }
        try {
            iArr[Action.PHOTOSHARE_UPLOAD_START.ordinal()] = 70;
        } catch (NoSuchFieldError e65) {
        }
        try {
            iArr[Action.PRINT.ordinal()] = 71;
        } catch (NoSuchFieldError e66) {
        }
        try {
            iArr[Action.RANGE_MEASURE.ordinal()] = 72;
        } catch (NoSuchFieldError e67) {
        }
        try {
            iArr[Action.RECYCLE_CLEAN_BIN.ordinal()] = 73;
        } catch (NoSuchFieldError e68) {
        }
        try {
            iArr[Action.RECYCLE_DELETE.ordinal()] = 74;
        } catch (NoSuchFieldError e69) {
        }
        try {
            iArr[Action.RECYCLE_RECOVERY.ordinal()] = 75;
        } catch (NoSuchFieldError e70) {
        }
        try {
            iArr[Action.REDO.ordinal()] = 76;
        } catch (NoSuchFieldError e71) {
        }
        try {
            iArr[Action.REMOVE.ordinal()] = 77;
        } catch (NoSuchFieldError e72) {
        }
        try {
            iArr[Action.RENAME.ordinal()] = 78;
        } catch (NoSuchFieldError e73) {
        }
        try {
            iArr[Action.RE_SEARCH.ordinal()] = 79;
        } catch (NoSuchFieldError e74) {
        }
        try {
            iArr[Action.ROTATE_LEFT.ordinal()] = 80;
        } catch (NoSuchFieldError e75) {
        }
        try {
            iArr[Action.ROTATE_RIGHT.ordinal()] = 81;
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
            iArr[Action.SETAS.ordinal()] = 85;
        } catch (NoSuchFieldError e80) {
        }
        try {
            iArr[Action.SETAS_BOTH.ordinal()] = 3;
        } catch (NoSuchFieldError e81) {
        }
        try {
            iArr[Action.SETAS_FIXED.ordinal()] = 4;
        } catch (NoSuchFieldError e82) {
        }
        try {
            iArr[Action.SETAS_FIXED_ACTIVED.ordinal()] = 86;
        } catch (NoSuchFieldError e83) {
        }
        try {
            iArr[Action.SETAS_HOME.ordinal()] = 5;
        } catch (NoSuchFieldError e84) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE.ordinal()] = 6;
        } catch (NoSuchFieldError e85) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE_ACTIVED.ordinal()] = 87;
        } catch (NoSuchFieldError e86) {
        }
        try {
            iArr[Action.SETAS_UNLOCK.ordinal()] = 7;
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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extra = getIntent().getExtras();
        if (extra == null || BundleUtils.isValid(extra)) {
            initializeByIntent();
            setContentView(R.layout.layout_gl_activity);
            this.mView = findViewById(R.id.gallery_main_root);
            setOrientationForTablet();
            onActivityCreated(savedInstanceState);
            onCreate();
            onCreateView();
            getWindow().addFlags(1024);
            if (ApiHelper.HAS_MODIFY_STATUS_BAR_COLOR) {
                UIUtils.setStatusBarColor(getWindow(), 0);
                UIUtils.setNavigationBarColor(getWindow(), 0);
            }
            return;
        }
        GalleryLog.w("CropImageActivity", "extras is illegal.");
        finish();
    }

    public void onCreate() {
        String setAsTheme = this.mData.getString("set-as-theme");
        int aspectX = this.mData.getInt("aspectX", 0);
        int aspectY = this.mData.getInt("aspectY", 0);
        boolean fromTheme = setAsTheme != null;
        boolean customRatio = (aspectX == 0 || aspectY == 0) ? false : true;
        if (fromTheme || (customRatio && aspectX == aspectY)) {
            this.mCropView = new SimpleCropView(this);
        } else {
            this.mCropView = new CropView(this);
        }
        this.mCropView.setBackgroundColor(GalleryUtils.intColorToFloatARGBArray(getResources().getColor(17170446)));
        if (customRatio) {
            this.mCropView.setAspectRatio(((float) aspectX) / ((float) aspectY));
        }
        float spotlightX = this.mData.getFloat("spotlightX", 0.0f);
        float spotlightY = this.mData.getFloat("spotlightY", 0.0f);
        if (spotlightX != 0.0f && spotlightY != 0.0f) {
            this.mCropView.setSpotlightRatio(spotlightX, spotlightY);
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        this.mDoFaceDetection = this.mData.getBoolean("detect-face");
        if (savedInstanceState != null) {
            this.mFlags = savedInstanceState.getInt("flag");
            this.mLaunchMode = savedInstanceState.getInt("mode");
            this.mSubMode = savedInstanceState.getInt("sub-mode");
        } else if (this.mData.containsKey("launch-mode")) {
            this.mLaunchMode = this.mData.getInt("launch-mode");
        } else {
            this.mLaunchMode = 3;
        }
        this.mActionMode = (ActionMode) getGalleryActionBar().enterModeForced(false, 2);
        requestWindow((this.mLaunchMode == 4 ? 2 : 20) | 76, this.mView);
        this.mFeature = 768;
        requestNaviFeature(this.mView);
        launchMode(this.mActionMode, this.mLaunchMode);
    }

    private void launchMode(ActionMode actionMode, int mode) {
        actionMode.setTitle((int) R.string.set_wallpaper);
        actionMode.show();
        switch (mode) {
            case 1:
                actionMode.setTitle((int) R.string.setas_home);
                this.mFlags = 2;
                this.mSetAsThemeResult = "home";
                actionMode.setBothAction(Action.NO, Action.OK);
                actionMode.setMenu(2, Action.SETAS_FIXED_ACTIVED, Action.SETAS_SCROLLABLE);
                return;
            case 2:
                actionMode.setTitle((int) R.string.setas_lock);
                this.mFlags = 1;
                this.mSetAsThemeResult = "unlock";
                actionMode.setBothAction(Action.NO, Action.OK);
                getGalleryActionBar().setActionPanelVisible(false);
                return;
            case 3:
                this.mSetAsThemeResult = "both";
                actionMode.setBothAction(Action.NO, Action.NONE);
                actionMode.setMenu(3, Action.SETAS_UNLOCK, Action.SETAS_HOME, Action.SETAS_BOTH);
                return;
            case 4:
                if (isFromContact()) {
                    actionMode.setTitle((int) R.string.contact_photo);
                } else {
                    actionMode.setTitle((int) R.string.crop_label);
                }
                actionMode.setBothAction(Action.NO, Action.OK);
                return;
            case 255:
                actionMode.setTitle((int) R.string.crop_home_wallpaper);
                this.mFlags = 2;
                actionMode.setBothAction(Action.NO, Action.OK);
                actionMode.setMenu(2, Action.SETAS_FIXED_ACTIVED, Action.SETAS_SCROLLABLE);
                return;
            case 256:
                actionMode.setTitle((int) R.string.crop_home_wallpaper);
                this.mFlags = 4;
                actionMode.setBothAction(Action.NO, Action.OK);
                actionMode.setMenu(2, Action.SETAS_FIXED, Action.SETAS_SCROLLABLE_ACTIVED);
                return;
            default:
                throw new IllegalStateException("wrong mode for crop.");
        }
    }

    public View onCreateView() {
        this.mGLRootView = getGLRoot();
        this.mGLRootView.requestFullScreenLayout();
        this.mGLRootView.setContentPane(this.mCropView);
        this.mCropView.setGLRoot(this.mGLRootView);
        this.mMainHandler = new SynchronizedHandler(this.mGLRootView) {
            public void handleMessage(Message message) {
                switch (message.what) {
                    case 1:
                        CropImageActivity.this.dismissProgressDialogIfShown();
                        CropImageActivity.this.onBitmapRegionDecoderAvailable((BitmapRegionDecoder) message.obj);
                        return;
                    case 2:
                        CropImageActivity.this.dismissProgressDialogIfShown();
                        CropImageActivity.this.onBitmapAvailable((Bitmap) message.obj);
                        return;
                    case 3:
                        CropImageActivity.this.dismissProgressDialogIfShown();
                        CropImageActivity.this.setResult(-1, (Intent) message.obj);
                        CropImageActivity.this.finish();
                        return;
                    case 4:
                        CropImageActivity.this.dismissProgressDialogIfShown();
                        ContextedUtils.showToastQuickly(CropImageActivity.this.getActivityContext(), (int) R.string.save_error_Toast, 1);
                        CropImageActivity.this.finish();
                        return;
                    case 5:
                        CropImageActivity.this.setResult(0);
                        CropImageActivity.this.finish();
                        return;
                    default:
                        return;
                }
            }
        };
        return this.mView;
    }

    public void onSaveInstanceState(Bundle saveState) {
        saveState.putInt("flag", this.mFlags);
        saveState.putInt("mode", this.mLaunchMode);
        saveState.putInt("sub-mode", this.mSubMode);
    }

    public void onActionItemClicked(Action action) {
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
                this.mMainHandler.sendEmptyMessage(5);
                break;
            case 2:
                onSaveClicked(this.mFlags);
                break;
            case 3:
                this.mSetAsThemeResult = "both";
                this.mFlags = 3;
                onSaveClicked(this.mFlags);
                break;
            case 4:
                this.mCropView.setScrollableWallper(false);
                launchMode(this.mActionMode, 255);
                this.mFlags = 2;
                break;
            case 5:
                this.mFlags = 2;
                this.mSetAsThemeResult = "home";
                launchMode(this.mActionMode, 255);
                break;
            case 6:
                launchMode(this.mActionMode, 256);
                this.mCropView.setScrollableWallper(true);
                this.mFlags = 4;
                break;
            case 7:
                this.mFlags = 1;
                this.mSetAsThemeResult = "unlock";
                onSaveClicked(this.mFlags);
                break;
        }
        LOG.d("mFlags is :" + this.mFlags);
    }

    public void onBackPressed() {
        if ((this.mFlags == 2 || this.mFlags == 4) && this.mLaunchMode == 3) {
            launchMode(this.mActionMode, 3);
            this.mFlags = 1;
            this.mCropView.setScrollableWallper(false);
            return;
        }
        setResult(1);
        finish();
    }

    private boolean saveBitmapToOutputStream(JobContext jc, Bitmap bitmap, CompressFormat format, OutputStream os) {
        Closeable outputStream = new InterruptableOutputStream(os);
        jc.setCancelListener(new OutputStreamInterrupter(outputStream));
        try {
            bitmap.compress(format, 90, outputStream);
            boolean z = !jc.isCancelled();
            jc.setCancelListener(null);
            Utils.closeSilently(outputStream);
            return z;
        } catch (Throwable th) {
            jc.setCancelListener(null);
            Utils.closeSilently(outputStream);
        }
    }

    private boolean saveBitmapToUri(JobContext jc, Bitmap bitmap, Uri uri) {
        Closeable out;
        try {
            out = getContentResolver().openOutputStream(uri);
            boolean saveBitmapToOutputStream = saveBitmapToOutputStream(jc, bitmap, convertExtensionToCompressFormat(getFileExtension()), out);
            Utils.closeSilently(out);
            return saveBitmapToOutputStream;
        } catch (FileNotFoundException e) {
            LOG.w("cannot write output", e);
            return true;
        } catch (Throwable th) {
            Utils.closeSilently(out);
        }
    }

    private String getFileExtension() {
        String requestFormat = getIntent().getStringExtra("outputFormat");
        String outputFormat = (requestFormat == null ? determineCompressFormat(this.mMediaItem) : requestFormat).toLowerCase(Locale.ENGLISH);
        return (outputFormat.equals("png") || outputFormat.equals("gif")) ? "png" : "jpg";
    }

    private CompressFormat convertExtensionToCompressFormat(String extension) {
        return extension.equals("png") ? CompressFormat.PNG : CompressFormat.JPEG;
    }

    public static String determineCompressFormat(MediaObject obj) {
        String compressTargetFormat = "JPEG";
        if (!(obj instanceof MediaItem)) {
            return compressTargetFormat;
        }
        String mime = ((MediaItem) obj).getMimeType();
        if (mime.contains("png") || mime.contains("gif")) {
            return "PNG";
        }
        return compressTargetFormat;
    }

    private void saveLockscreenBitmap(JobContext jc, Bitmap cropped, boolean isHome) {
        Exception e;
        Throwable th;
        File screenDir = new File(PATH_WALLPAPER);
        Closeable closeable = null;
        if (screenDir.isDirectory() && cropped != null) {
            String fileName;
            if (isHome) {
                try {
                    fileName = "gallery_home_wallpaper_0.tmp";
                } catch (Exception e2) {
                    e = e2;
                    try {
                        LOG.d(e.getMessage());
                        Utils.closeSilently(closeable);
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        Utils.closeSilently(closeable);
                        throw th;
                    }
                }
            }
            fileName = "unlock_wallpaper_0.tmp";
            File screenFile = new File(screenDir, fileName);
            Closeable fileOutputSteam = new FileOutputStream(screenFile);
            if (isHome) {
                try {
                    GalleryLog.w("CropImageActivity", "isHome");
                    cropped.compress(CompressFormat.JPEG, 90, fileOutputSteam);
                } catch (Exception e3) {
                    e = e3;
                    closeable = fileOutputSteam;
                    LOG.d(e.getMessage());
                    Utils.closeSilently(closeable);
                    return;
                } catch (Throwable th3) {
                    th = th3;
                    closeable = fileOutputSteam;
                    Utils.closeSilently(closeable);
                    throw th;
                }
            }
            GalleryLog.w("CropImageActivity", "not isHome");
            cropped.compress(CompressFormat.PNG, 90, fileOutputSteam);
            File targetFile = new File(screenDir, isHome ? "gallery_home_wallpaper_0.jpg" : "unlock_wallpaper_0.jpg");
            renameFile(screenFile, targetFile);
            FileUtils.setPermissions(targetFile.getPath(), 508, Process.myUid(), 1023);
            Utils.closeSilently(fileOutputSteam);
            closeable = fileOutputSteam;
        }
    }

    public static void renameFile(File fileSrc, File fileTarget) {
        try {
            if (!fileSrc.renameTo(fileTarget)) {
                LOG.e("rename file failed.");
            }
        } catch (Exception e) {
        }
    }

    private boolean setAsWallpaper(JobContext jc, Bitmap wallpaper) {
        try {
            WallpaperManager.getInstance(this).setBitmap(wallpaper);
        } catch (IOException e) {
            LOG.w("fail to set wall paper", e);
        }
        return true;
    }

    private void onSaveClicked(int flag) {
        reportDataForCropContact();
        Rect cropRect = this.mCropView.getCropRectangle();
        if (cropRect != null && this.mState != 2) {
            int messageId;
            this.mState = 2;
            if ((flag & 7) != 0) {
                messageId = R.string.wallpaper;
            } else {
                messageId = R.string.saving_image;
            }
            dismissProgressDialogIfShown();
            this.mProgressDialog = ProgressDialog.show(GalleryUtils.getHwThemeContext(this, "androidhwext:style/Theme.Emui.Dialog"), null, getString(messageId), true, false);
            this.mSaveTask = getThreadPool().submit(new SaveOutput(cropRect), new FutureListener<Intent>() {
                public void onFutureDone(Future<Intent> future) {
                    CropImageActivity.this.mSaveTask = null;
                    if (!future.isCancelled()) {
                        Intent intent = (Intent) future.get();
                        CropImageActivity.LOG.d("cropped image, intent is " + intent);
                        if (intent != null) {
                            CropImageActivity.this.mMainHandler.sendMessage(CropImageActivity.this.mMainHandler.obtainMessage(3, intent));
                        } else {
                            CropImageActivity.this.mMainHandler.sendEmptyMessage(4);
                        }
                    }
                }
            });
        }
    }

    private Bitmap getCroppedImage(Rect rect) {
        rect = getRect(rect);
        int outputX = rect.width();
        int outputY = rect.height();
        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            outputX = extra.getInt("outputX", outputX);
            outputY = extra.getInt("outputY", outputY);
        }
        if (this.mFlags != 0) {
            boolean scrollable = (this.mFlags & 4) != 0;
            outputX = scrollable ? this.mOutputW : this.mSize.x;
            if (scrollable) {
                outputY = this.mOutputH;
            } else {
                outputY = this.mSize.y;
            }
        }
        if (outputX * outputY > 5000000) {
            float scale = (float) Math.sqrt((5000000.0d / ((double) outputX)) / ((double) outputY));
            LOG.w("scale down the cropped image: " + scale);
            outputX = Math.round(((float) outputX) * scale);
            outputY = Math.round(((float) outputY) * scale);
        }
        Rect dest = new Rect(0, 0, outputX, outputY);
        float scaleX = ((float) outputX) / ((float) rect.width());
        float scaleY = ((float) outputY) / ((float) rect.height());
        int rectW = Math.round(((float) rect.width()) * scaleX);
        int rectH = Math.round(((float) rect.height()) * scaleY);
        dest.set(Math.round(((float) (outputX - rectW)) / 2.0f), Math.round(((float) (outputY - rectH)) / 2.0f), Math.round(((float) (outputX + rectW)) / 2.0f), Math.round(((float) (outputY + rectH)) / 2.0f));
        int rotation;
        if (this.mUseRegionDecoder) {
            rotation = this.mMediaItem.getFullImageRotation();
            rotateRectangle(rect, this.mCropView.getImageWidth(), this.mCropView.getImageHeight(), 360 - rotation);
            rotateRectangle(dest, outputX, outputY, 360 - rotation);
            Options options = new Options();
            int sample = BitmapUtils.computeSampleSizeLarger(Math.max(scaleX, scaleY));
            options.inSampleSize = sample;
            if (rect.width() / sample == dest.width() && rect.height() / sample == dest.height() && outputX == dest.width() && outputY == dest.height() && rotation == 0) {
                Bitmap decodeRegion;
                synchronized (this.mRegionDecoder) {
                    decodeRegion = this.mRegionDecoder.decodeRegion(rect, options);
                }
                return decodeRegion;
            }
            Bitmap result = Bitmap.createBitmap(outputX, outputY, Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            rotateCanvas(canvas, outputX, outputY, rotation);
            drawInTiles(canvas, this.mRegionDecoder, rect, dest, sample);
            return result;
        }
        rotation = this.mMediaItem.getRotation();
        rotateRectangle(rect, this.mCropView.getImageWidth(), this.mCropView.getImageHeight(), 360 - rotation);
        rotateRectangle(dest, outputX, outputY, 360 - rotation);
        result = Bitmap.createBitmap(outputX, outputY, Config.ARGB_8888);
        canvas = new Canvas(result);
        rotateCanvas(canvas, outputX, outputY, rotation);
        canvas.drawBitmap(this.mBitmap, rect, dest, new Paint(2));
        return result;
    }

    private static Rect getRect(Rect rect) {
        if (rect.width() == 0) {
            int left = rect.left;
            if (left > 0) {
                rect.left = left - 1;
            } else {
                rect.right = left + 1;
            }
        }
        if (rect.height() == 0) {
            int top = rect.top;
            if (top > 0) {
                rect.top = top - 1;
            } else {
                rect.bottom = top + 1;
            }
        }
        return rect;
    }

    private static void rotateCanvas(Canvas canvas, int width, int height, int rotation) {
        int x = width / 2;
        int y = height / 2;
        canvas.translate((float) x, (float) y);
        canvas.rotate((float) rotation);
        if (((rotation / 90) & 1) == 0) {
            canvas.translate((float) (-x), (float) (-y));
        } else {
            canvas.translate((float) (-y), (float) (-x));
        }
    }

    private static void rotateRectangle(Rect rect, int width, int height, int rotation) {
        if (rotation != 0 && rotation != 360) {
            int w = rect.width();
            int h = rect.height();
            switch (rotation) {
                case WMComponent.ORI_90 /*90*/:
                    rect.top = rect.left;
                    rect.left = height - rect.bottom;
                    rect.right = rect.left + h;
                    rect.bottom = rect.top + w;
                    return;
                case 180:
                    rect.left = width - rect.right;
                    rect.top = height - rect.bottom;
                    rect.right = rect.left + w;
                    rect.bottom = rect.top + h;
                    return;
                case 270:
                    rect.left = rect.top;
                    rect.top = width - rect.right;
                    rect.right = rect.left + h;
                    rect.bottom = rect.top + w;
                    return;
                default:
                    throw new AssertionError();
            }
        }
    }

    private void drawInTiles(Canvas canvas, BitmapRegionDecoder decoder, Rect rect, Rect dest, int sample) {
        int tileSize = sample * 512;
        Rect tileRect = new Rect();
        rect = ensureRectEffective(rect);
        Options options = new Options();
        options.inPreferredConfig = Config.ARGB_8888;
        options.inSampleSize = sample;
        canvas.translate((float) dest.left, (float) dest.top);
        canvas.scale((((float) sample) * ((float) dest.width())) / ((float) rect.width()), (((float) sample) * ((float) dest.height())) / ((float) rect.height()));
        Paint paint = new Paint(2);
        int tx = rect.left;
        int x = 0;
        while (tx < rect.right) {
            int ty = rect.top;
            int y = 0;
            while (ty < rect.bottom) {
                tileRect.set(tx, ty, tx + tileSize, ty + tileSize);
                if (tileRect.intersect(rect)) {
                    Bitmap bitmap;
                    synchronized (decoder) {
                        bitmap = decoder.decodeRegion(tileRect, options);
                    }
                    if (bitmap != null) {
                        canvas.drawBitmap(bitmap, (float) x, (float) y, paint);
                        bitmap.recycle();
                    }
                }
                ty += tileSize;
                y += 512;
            }
            tx += tileSize;
            x += 512;
        }
    }

    private Rect ensureRectEffective(Rect rect) {
        Rect result = rect;
        if (rect.left < 0) {
            rect.left = 0;
        }
        if (rect.top < 0) {
            rect.top = 0;
        }
        return rect;
    }

    private void onBitmapRegionDecoderAvailable(BitmapRegionDecoder regionDecoder) {
        if (!loadImageFailed(regionDecoder)) {
            this.mRegionDecoder = regionDecoder;
            this.mUseRegionDecoder = true;
            this.mState = 1;
            Options options = new Options();
            int width = regionDecoder.getWidth();
            int height = regionDecoder.getHeight();
            options.inSampleSize = BitmapUtils.computeSampleSize(width, height, -1, 480000);
            this.mBitmap = regionDecoder.decodeRegion(new Rect(0, 0, width, height), options);
            if (!loadImageFailed(this.mBitmap)) {
                if (this.mDoFaceDetection) {
                    this.mCropView.detectFaces(this.mBitmap);
                } else {
                    this.mCropView.initializeHighlightRectangle();
                }
                this.mBitmapScreenNail = new BitmapScreenNail(this.mBitmap);
                TileImageViewAdapter adapter = new TileImageViewAdapter();
                adapter.setScreenNail(this.mBitmapScreenNail, width, height);
                adapter.setRegionDecoder(regionDecoder);
                this.mCropView.setDataModel(adapter, this.mMediaItem.getFullImageRotation());
            }
        }
    }

    private boolean loadImageFailed(Object arg) {
        if (arg != null) {
            return false;
        }
        ContextedUtils.showToastQuickly(getActivityContext(), (int) R.string.fail_to_load_image_Toast, 0);
        finish();
        return true;
    }

    private void onBitmapAvailable(Bitmap bitmap) {
        if (!loadImageFailed(bitmap)) {
            this.mUseRegionDecoder = false;
            this.mState = 1;
            this.mBitmap = bitmap;
            if (this.mDoFaceDetection) {
                this.mCropView.detectFaces(this.mBitmap);
            } else {
                this.mCropView.initializeHighlightRectangle();
            }
            this.mCropView.setDataModel(new BitmapTileProvider(bitmap, 512), this.mMediaItem.getRotation());
        }
    }

    @TargetApi(13)
    private Point getDefaultDisplaySize(Activity activity, Point size) {
        Display d = activity.getWindowManager().getDefaultDisplay();
        if (VERSION.SDK_INT >= 13) {
            d.getRealSize(size);
        } else {
            size.set(d.getWidth(), d.getHeight());
        }
        return size;
    }

    private void initializeData() {
        boolean supportedByBitmapRegionDecoder = true;
        Activity activity = this;
        this.mOutputW = getWallpaperDesiredMinimumWidth();
        this.mOutputH = getWallpaperDesiredMinimumHeight();
        this.mSize = getDefaultDisplaySize(this, new Point());
        dismissProgressDialogIfShown();
        this.mProgressDialog = ProgressDialog.show(this, null, getString(R.string.loading_image), true, true);
        this.mProgressDialog.setCanceledOnTouchOutside(false);
        this.mProgressDialog.setCancelMessage(this.mMainHandler.obtainMessage(5));
        this.mMediaItem = getMediaItemFromIntentData();
        if (!loadImageFailed(this.mMediaItem)) {
            if ((this.mMediaItem.getSupportedOperations() & 64) == 0) {
                supportedByBitmapRegionDecoder = false;
            }
            if (supportedByBitmapRegionDecoder && shouldUseRegionDecoder()) {
                this.mLoadTask = getThreadPool().submit(new LoadDataTask(this.mMediaItem), this.bitmapRegionDecoderFutureListener);
            } else {
                this.mLoadBitmapTask = loadBitmapData(this.thumbListener);
            }
        }
    }

    private boolean shouldUseRegionDecoder() {
        boolean z = true;
        String outputFile = this.mData.getString("output-file-path");
        if (outputFile == null) {
            return true;
        }
        if (outputFile.equals(this.mData.getString("input-file-path"))) {
            z = false;
        }
        return z;
    }

    private Future<Bitmap> loadBitmapData(FutureListener<Bitmap> thumbListener) {
        return getThreadPool().submit(new LoadBitmapDataTask(this.mMediaItem), thumbListener);
    }

    public void onResume() {
        boolean z = false;
        super.onResume();
        LOG.d("onResume");
        if (this.mState == 0) {
            initializeData();
        }
        this.mCropView.setWallpaperSize(this.mOutputW, this.mOutputH);
        BaseCropView baseCropView = this.mCropView;
        if ((this.mFlags & 4) != 0) {
            z = true;
        }
        baseCropView.setScrollableWallper(z);
        this.mMainHandler.sendEmptyMessageDelayed(6, 300);
        GLRoot root = this.mGLRootView;
        root.lockRenderThread();
        try {
            this.mCropView.resume();
        } finally {
            root.unlockRenderThread();
        }
    }

    public void onPause() {
        super.onPause();
        dismissProgressDialogIfShown();
        Future<BitmapRegionDecoder> loadTask = this.mLoadTask;
        if (!(loadTask == null || loadTask.isDone())) {
            loadTask.cancel();
            loadTask.waitDone();
        }
        Future<Bitmap> loadBitmapTask = this.mLoadBitmapTask;
        if (!(loadBitmapTask == null || loadBitmapTask.isDone())) {
            loadBitmapTask.cancel();
            loadBitmapTask.waitDone();
        }
        GalleryLog.d("CropImageActivity", "load bitmap task state: " + loadBitmapTask);
        Future<Intent> saveTask = this.mSaveTask;
        if (!(saveTask == null || saveTask.isDone())) {
            saveTask.cancel();
            saveTask.waitDone();
        }
        GalleryLog.d("CropImageActivity", "save task state: " + saveTask);
        if (this.mState == 2) {
            this.mState = 1;
        }
        GLRoot root = this.mGLRootView;
        root.lockRenderThread();
        try {
            this.mCropView.pause();
        } finally {
            root.unlockRenderThread();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mBitmapScreenNail != null) {
            this.mBitmapScreenNail.recycle();
            this.mBitmapScreenNail = null;
        }
    }

    private void dismissProgressDialogIfShown() {
        if (this.mProgressDialog != null) {
            GalleryUtils.dismissDialogSafely(this.mProgressDialog, this);
            this.mProgressDialog = null;
        }
    }

    private MediaItem getMediaItemFromIntentData() {
        DataManager manager = getDataManager();
        String path = this.mData.getString("media-path");
        if (path == null) {
            return null;
        }
        return (MediaItem) manager.getMediaObject(path);
    }

    private boolean isFromContact() {
        Parcelable outPut = this.mData.getParcelable("output");
        if (outPut == null || !outPut.toString().contains("com.android.contacts")) {
            return false;
        }
        return true;
    }

    private void reportDataForCropContact() {
        if (isFromContact()) {
            ReportToBigData.report(28, String.format("{SetAsType:%s,Scroll:%s}", new Object[]{"ContactImage", ""}));
        }
    }

    protected void initializeByIntent() {
        Bundle para = new Bundle();
        Path path = getPathFromIntentData();
        if (path == null) {
            LOG.w("cannot get path for: " + getIntent().getData() + ", or no data given");
            finish();
            return;
        }
        path.clearObject();
        Bundle extras = getIntent().getExtras();
        Object obj = null;
        boolean setAsWallpaper = false;
        if (extras != null) {
            para.putAll(extras);
            para.putString("output-file-path", GalleryUtils.convertUriToPath(this, (Uri) extras.getParcelable("output")));
            obj = extras.getString("set-as-theme");
            setAsWallpaper = extras.getBoolean("set-as-wallpaper", false);
        }
        para.putString("input-file-path", GalleryUtils.convertUriToPath(this, getIntent().getData()));
        if ("both".equals(obj)) {
            para.putInt("launch-mode", 3);
        } else if ("home".equals(obj) || r4) {
            para.putInt("launch-mode", 1);
        } else if ("unlock".equals(obj)) {
            para.putInt("launch-mode", 2);
        } else {
            para.putInt("launch-mode", 4);
            para.putBoolean("detect-face", true);
        }
        para.putString("media-path", path.toString());
        this.mData.clear();
        this.mData.putAll(para);
    }

    private Path getPathFromIntentData() {
        return getDataManager().findPathByUri(getIntent().getData(), getIntent().getType());
    }

    public void onNavigationBarChanged(boolean show, int height) {
        requestNaviFeature(this.mView);
    }

    public void requestNaviFeature(View view) {
        int feature = this.mFeature;
        if ((feature & 256) != 0) {
            UIUtils.setNavigationBarIsOverlay(view, true);
            getGalleryActionBar().setNavigationMargin(LayoutHelper.getNavigationBarHeight());
        } else {
            UIUtils.setNavigationBarIsOverlay(view, false);
            getGalleryActionBar().setNavigationMargin(0);
        }
        if ((feature & 768) == 768) {
            getWindow().addFlags(134217728);
        } else {
            getWindow().clearFlags(134217728);
        }
    }

    protected void requestWindow(int feature, View fragView) {
        boolean z;
        boolean z2 = true;
        if ((feature & 4) != 0) {
            getGalleryActionBar().setHeadBackground(0);
            getGalleryActionBar().setActionPanelStyle(1);
        } else {
            getGalleryActionBar().setHeadDefaultBackground();
            getGalleryActionBar().setActionPanelStyle(0);
        }
        GalleryActionBar galleryActionBar = getGalleryActionBar();
        if ((feature & 2) == 0) {
            z = true;
        } else {
            z = false;
        }
        galleryActionBar.setActionPanelVisible(z);
        GalleryActionBar galleryActionBar2 = getGalleryActionBar();
        if ((feature & 1) != 0) {
            z2 = false;
        }
        galleryActionBar2.setHeadBarVisible(z2);
    }

    protected boolean needToRequestPermissions() {
        return false;
    }
}
