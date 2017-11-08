package com.huawei.gallery.wallpaper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.Process;
import android.os.SystemProperties;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.app.Wallpaper;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.DecodeUtils;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.ActionDeleteAndConfirm;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.InterruptableOutputStream;
import com.android.gallery3d.util.MultiWindowStatusHolder;
import com.android.gallery3d.util.ReportToBigData;
import com.android.gallery3d.util.ThreadPool;
import com.android.gallery3d.util.ThreadPool.CancelListener;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionMode;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.app.GLActivity;
import com.huawei.gallery.share.HwCustUtilsWrapper;
import com.huawei.gallery.ui.stackblur.StackBlurUtils;
import com.huawei.gallery.util.BundleUtils;
import com.huawei.gallery.util.ColorfulUtils;
import com.huawei.gallery.util.LayoutHelper;
import com.huawei.gallery.util.MyPrinter;
import com.huawei.gallery.util.ResourceUtils;
import com.huawei.gallery.util.UIUtils;
import com.huawei.gallery.wallpaper.ui.CropWallpaperView;
import com.huawei.gallery.wallpaper.ui.LabelView;
import com.huawei.watermark.ui.WMComponent;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import tmsdk.common.module.update.UpdateConfig;

@TargetApi(19)
public class CropWallpaperActivity extends GLActivity implements OnSystemUiVisibilityChangeListener {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    public static final File DOWNLOAD_BUCKET = new File(Environment.getExternalStorageDirectory(), "download");
    private static final int HIDE_BARS_TIMEOUT = GalleryUtils.getDelayTime(3500);
    private static final MyPrinter LOG = new MyPrinter("CropWallpaperActivity");
    private static final Path WALLPAPER_CURRENT_USED = Path.fromString("/virtual/image/wallpaper");
    private boolean MCCMNC_ENABLE;
    private ActionMode mActionMode;
    private ActivityGuard mActivityGuard = new ActivityGuard();
    private int mAspectX;
    private int mAspectY;
    private WallpaperStateManager mBackupWallpaperManager = new WallpaperStateManager();
    private Bitmap mBitmap;
    private int mBlurFactor = -1;
    private SeekBar mBlurSeekbar;
    private OnClickListener mCheckListener = new OnClickListener() {
        public void onClick(View v) {
            int i = 0;
            if (CropWallpaperActivity.this.mIsInProgress) {
                CropWallpaperActivity.LOG.d("seek bar inprogress, ingore click");
                return;
            }
            boolean isChecked = !v.isSelected();
            v.setSelected(isChecked);
            ((TextView) v).setTextColor(CropWallpaperActivity.this.getResources().getColor(R.color.wallpaper_controll_normal));
            switch (v.getId()) {
                case R.id.wallpaper_control_blur:
                    if (CropWallpaperActivity.this.mBlurFactor == -1) {
                        CropWallpaperActivity.this.mBlurSeekbar.setProgress(20);
                    }
                    LinearLayout -get19 = CropWallpaperActivity.this.mSeekbarControls;
                    if (!isChecked) {
                        i = 8;
                    }
                    -get19.setVisibility(i);
                    int progress = isChecked ? CropWallpaperActivity.this.mBlurSeekbar.getProgress() : 0;
                    CropWallpaperActivity.this.mCropView.setBlurFactor(progress);
                    CropWallpaperActivity.this.mBlurFactor = progress;
                    break;
                case R.id.wallpaper_control_scroll:
                    CropWallpaperActivity.this.mCropView.setScrollableWallper(isChecked);
                    CropWallpaperActivity.this.mScrollable = isChecked;
                    break;
            }
        }
    };
    private CropWallpaperView mCropView;
    private View mCurrentItem;
    private StateRestorer mCurrentStateJob = new StateRestorer();
    private View mCurrentWallpaper;
    private View mCurrentWallpaperDefault;
    private String mCurrentWallpaperPath;
    private String mCurrentWallpaperPathDefault;
    private DataManager mDataManager;
    private OnClickListener mDeleteDownloadPicListener = new OnClickListener() {
        public void onClick(final View v) {
            View view = v;
            ActionDeleteAndConfirm deleteDialog = new ActionDeleteAndConfirm(CropWallpaperActivity.this.getActivity(), CropWallpaperActivity.this.getResources().getString(R.string.del_download_wallpaper_tips));
            deleteDialog.setOnClickListener(new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == -1) {
                        CropWallpaperActivity.this.doDelete(v);
                    }
                }
            });
            deleteDialog.show();
        }
    };
    private boolean mDoSetWallpaperDefault = false;
    private boolean mDownloadOrThemeCurrentItem = true;
    private boolean mEnterOnline = false;
    private int mFeature = 268;
    private GLRoot mGLRootView;
    private boolean mInDeleteMode = false;
    private String mInputFilePath;
    private boolean mIsInProgress = false;
    private boolean mIsShowCustWallpaperFirst = false;
    private LinearLayout mListItemsLayout;
    private Future<Bitmap> mLoadBitmapTask;
    private boolean mLoadingCanceled = false;
    private Handler mMainHandler;
    private MediaItem mMediaItem;
    private String mMediaPath;
    private int mMinSideLength = -1;
    private FailSaver mOnceOnlyFailedSaver;
    private String mOutputFilePath;
    private int mOutputH;
    private Uri mOutputUri;
    private int mOutputW;
    private ProgressDialog mProgressDialog;
    private ViewGroup mRootLayout;
    private int mRotation = 0;
    private boolean mSaveFinished = false;
    private Future<Intent> mSaveTask;
    private boolean mScrollable = false;
    private TextView mScrollableSwitch;
    OnClickListener mSeekbarAdjClickListener = new OnClickListener() {
        public void onClick(View v) {
            int viewId = v.getId();
            int progress = CropWallpaperActivity.this.mBlurSeekbar.getProgress();
            if (viewId == R.id.progress_minus) {
                progress--;
            } else if (viewId == R.id.progress_plus) {
                progress++;
            } else {
                CropWallpaperActivity.LOG.w("not a seek button !!!");
            }
            CropWallpaperActivity.this.mBlurSeekbar.setProgress(progress);
        }
    };
    private LinearLayout mSeekbarControls;
    private String mSetAsTheme;
    private String mSetAsThemeResult = "";
    private boolean mSetLockScreen;
    private boolean mSetWallpaper;
    private boolean mShowSelectionMenu;
    private Point mSize;
    private float mSpotlightX;
    private float mSpotlightY;
    private int mState = 0;
    private int mThemesCount;
    WallpaperViewItemListener mViewItemListener = new WallpaperViewItemListener() {
        public void onClick(View v) {
            if (CropWallpaperActivity.this.mIsInProgress) {
                CropWallpaperActivity.LOG.d("seek bar inprogress, ingore click");
                return;
            }
            switch (v.getId()) {
                case R.id.online:
                    if (!CropWallpaperActivity.this.mInDeleteMode) {
                        CropWallpaperActivity.this.mEnterOnline = true;
                        Intent themeIntent = new Intent("android.intent.action.SET_WALLPAPER");
                        themeIntent.setPackage("com.huawei.android.thememanager");
                        themeIntent.putExtra("called-from-gallery", "home_wallpaper");
                        GalleryUtils.startActivityCatchSecurityEx(CropWallpaperActivity.this, themeIntent);
                        CropWallpaperActivity.this.mActivityGuard.waitToFinish();
                        CropWallpaperActivity.this.reportDataForWallpaperAction(ActionType.ONLINE);
                        break;
                    }
                    break;
                case R.id.gallery:
                    if (!CropWallpaperActivity.this.mInDeleteMode) {
                        Intent galleryIntent = new Intent("android.intent.action.GET_CONTENT");
                        galleryIntent.setType("image/*");
                        galleryIntent.setClass(CropWallpaperActivity.this, Wallpaper.class);
                        galleryIntent.setPackage(CropWallpaperActivity.this.getPackageName());
                        GalleryUtils.startActivityCatchSecurityEx(CropWallpaperActivity.this, galleryIntent);
                        CropWallpaperActivity.this.mActivityGuard.waitToFinish();
                        CropWallpaperActivity.this.reportDataForWallpaperAction(ActionType.GALLERY);
                        break;
                    }
                    break;
                default:
                    CropWallpaperActivity.this.setCurrentPreview(v);
                    break;
            }
        }

        public boolean onLongClick(View v) {
            if (CropWallpaperActivity.this.mIsInProgress || CropWallpaperActivity.this.mInDeleteMode) {
                CropWallpaperActivity.LOG.d("Can't enter delete mode.");
                return true;
            }
            CropWallpaperActivity.this.changeDeleteMode(true);
            return true;
        }
    };
    private LinearLayout mWallpaperControlsRoot;
    private String mWallpaperFrom;

    private interface WallpaperViewItemListener extends OnClickListener, OnLongClickListener {
    }

    private class FailSaver {
        private String mPath;
        private LabelView mView;

        FailSaver(LabelView view, String path) {
            this.mView = view;
            this.mPath = path;
        }

        public boolean save() {
            GalleryLog.d("CropWallpaperActivity", "save wallpaper with: " + this.mPath);
            Path.fromString(this.mPath).clearObject();
            if (!CropWallpaperActivity.this.switchToPath(this.mPath)) {
                return false;
            }
            MediaItem mediaItem = CropWallpaperActivity.this.mMediaItem;
            this.mView.setTag(this.mPath);
            CropWallpaperActivity.this.mRotation = 0;
            this.mView.setTag(33554432, Integer.valueOf(CropWallpaperActivity.this.mRotation));
            new ThemeLoader(CropWallpaperActivity.this.getThreadPool(), this.mView, mediaItem).submit();
            CropWallpaperActivity.this.initializeData();
            return true;
        }
    }

    private enum ActionType {
        ONLINE,
        GALLERY,
        PREVIEW,
        CLOSE
    }

    private class ActivityGuard extends BroadcastReceiver {
        private boolean mRegistered;
        private boolean mWaitting;

        private ActivityGuard() {
            this.mRegistered = false;
            this.mWaitting = true;
        }

        void finishWait() {
            this.mWaitting = false;
        }

        private void waitToFinish() {
            this.mWaitting = true;
            if (!this.mRegistered) {
                CropWallpaperActivity.LOG.d("waitToFinish wait with : com.android.huawei.gallery.action.ACTION_FINISH");
                CropWallpaperActivity.this.registerReceiver(this, new IntentFilter("com.android.huawei.gallery.action.ACTION_FINISH"), "com.huawei.gallery.permission.WALLPAPER_FINISHED", null);
                this.mRegistered = true;
            }
        }

        void notifyTofinish() {
            this.mWaitting = false;
            CropWallpaperActivity.LOG.d("notifyTofinish wait with : com.android.huawei.gallery.action.ACTION_FINISH");
            CropWallpaperActivity.this.sendBroadcast(new Intent("com.android.huawei.gallery.action.ACTION_FINISH"));
        }

        void releaseReceiverIfNeeded() {
            CropWallpaperActivity.LOG.d("releaseReceiverIfNeeded waitting : " + this.mWaitting);
            if (!this.mWaitting && this.mRegistered) {
                CropWallpaperActivity.this.unregisterReceiver(this);
                this.mRegistered = false;
            }
        }

        public void onReceive(Context context, Intent intent) {
            CropWallpaperActivity.LOG.d("received broadcast, will finish activity.");
            this.mWaitting = false;
            CropWallpaperActivity.this.finish();
        }
    }

    private static class BitmapOutputer {
        private Bitmap mBitmap;
        private CompressFormat mFormat;
        private int mQuality;

        BitmapOutputer(Bitmap bitmap, CompressFormat format, int quality) {
            this.mBitmap = bitmap;
            this.mFormat = format;
            this.mQuality = quality;
        }

        void writeToStream(OutputStream output) {
            this.mBitmap.compress(this.mFormat, this.mQuality, output);
        }
    }

    private class DefaultIconLoader extends BaseJob<Void> implements Runnable {
        private Drawable mDrawable;
        private LabelView mView;

        DefaultIconLoader(LabelView view) {
            this.mView = view;
        }

        public void run() {
            if (this.mDrawable != null) {
                this.mView.setDrawable(this.mDrawable);
            }
        }

        public String workContent() {
            return "load defalut icon for online and gallery";
        }

        public Void run(JobContext jc) {
            this.mDrawable = ResourceUtils.getDrawable(CropWallpaperActivity.this.getResources(), Integer.valueOf(R.drawable.btn_filter_add));
            CropWallpaperActivity.this.mMainHandler.post(this);
            return null;
        }
    }

    private class LoadBitmapDataTask extends BaseJob<Bitmap> {
        MediaItem mItem;

        public LoadBitmapDataTask(MediaItem item) {
            this.mItem = item;
        }

        public Bitmap run(JobContext jc) {
            MediaItem item = this.mItem;
            if (GalleryUtils.supportSetas(this.mItem)) {
                long start = System.currentTimeMillis();
                Options options = new Options();
                options.inJustDecodeBounds = true;
                if (jc.isCancelled()) {
                    return null;
                }
                BitmapFactory.decodeFile(item.getFilePath(), options);
                int imageWidth = options.outWidth;
                int imageHeight = options.outHeight;
                CropWallpaperActivity.LOG.d(String.format("image size (%sx%s)", new Object[]{Integer.valueOf(imageWidth), Integer.valueOf(imageHeight)}));
                if (imageWidth <= 0 || imageHeight <= 0) {
                    CropWallpaperActivity.LOG.d("decode bitmap failed(size error). use default bitmap ");
                    return (Bitmap) item.requestImage(1).run(jc);
                }
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Config.ARGB_8888;
                options.inSampleSize = BitmapUtils.computeSampleSizeShorter(imageWidth, imageHeight, CropWallpaperActivity.this.mMinSideLength);
                if (jc.isCancelled()) {
                    return null;
                }
                Bitmap result = BitmapFactory.decodeFile(item.getFilePath(), options);
                if (result != null) {
                    CropWallpaperActivity.LOG.d(String.format("decode from file, bitmap size (%sx%s)", new Object[]{Integer.valueOf(result.getWidth()), Integer.valueOf(result.getHeight())}));
                    if (jc.isCancelled()) {
                        return null;
                    }
                    result = BitmapUtils.resizeDownBySideLength(result, FragmentTransaction.TRANSIT_ENTER_MASK, true);
                    CropWallpaperActivity.LOG.d(String.format("resizeDownBySideLength(%s), bitmap size (%sx%s)", new Object[]{Integer.valueOf(FragmentTransaction.TRANSIT_ENTER_MASK), Integer.valueOf(result.getWidth()), Integer.valueOf(result.getHeight())}));
                    if (jc.isCancelled()) {
                        return null;
                    }
                    result = DecodeUtils.ensureGLCompatibleBitmap(result);
                } else if (jc.isCancelled()) {
                    return null;
                } else {
                    CropWallpaperActivity.LOG.d("decode bitmap failed. use default bitmap ");
                    result = (Bitmap) item.requestImage(1).run(jc);
                }
                CropWallpaperActivity.LOG.d("decode thumbnail cost time: " + (System.currentTimeMillis() - start));
                return result;
            }
            CropWallpaperActivity.LOG.d("media item is null, use default bitmap(thumbnail)");
            return null;
        }

        public String workContent() {
            return "request TYPE_THUMBNAIL image for " + this.mItem.getFilePath();
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
            String themeWallpaperPath;
            CropWallpaperActivity.this.mSaveFinished = false;
            Rect rect = this.mCropRect;
            CropWallpaperActivity.LOG.d("crop image rect : " + rect);
            Intent result = new Intent();
            Bitmap bitmap = null;
            Bundle extra = new Bundle();
            Uri uri = null;
            Intent intent = CropWallpaperActivity.this.getIntent();
            if (!(intent == null || intent.getExtras() == null)) {
                extra = intent.getExtras();
                uri = (Uri) extra.getParcelable("output");
            }
            GalleryLog.d("CropWallpaperActivity", "save out put uri is null? + " + (uri == null));
            if (uri != null) {
                if (jc.isCancelled()) {
                    return null;
                }
                bitmap = CropWallpaperActivity.this.getCroppedImage(rect);
                if (!CropWallpaperActivity.this.saveBitmapToUri(jc, bitmap, uri)) {
                    return null;
                }
            }
            if (extra.getBoolean("return-data", false)) {
                if (jc.isCancelled()) {
                    return null;
                }
                if (bitmap == null) {
                    bitmap = CropWallpaperActivity.this.getCroppedImage(rect);
                }
                result.putExtra(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH, bitmap);
            }
            if (CropWallpaperActivity.this.mSetLockScreen) {
                if (jc.isCancelled()) {
                    return null;
                }
                if (bitmap == null) {
                    bitmap = CropWallpaperActivity.this.getCroppedImage(rect);
                }
                CropWallpaperActivity.this.saveLockscreenBitmap(jc, BitmapUtils.resizeAndCropCenter(bitmap, CropWallpaperActivity.this.mSize.x, CropWallpaperActivity.this.mSize.y, false), false);
                if (!CropWallpaperActivity.this.setAsWallpaper(jc, bitmap, true)) {
                    return null;
                }
            }
            if (CropWallpaperActivity.this.mSetWallpaper) {
                if (jc.isCancelled()) {
                    return null;
                }
                if (bitmap == null) {
                    bitmap = CropWallpaperActivity.this.getCroppedImage(rect);
                }
                if (!GalleryUtils.checkDiskSpace(Environment.getDataDirectory().getPath(), ((long) bitmap.getByteCount()) + UpdateConfig.UPDATE_FLAG_DEEPCLEAN_SOFT_PATH_LIST)) {
                    return null;
                }
                CropWallpaperActivity.this.saveLockscreenBitmap(jc, bitmap, true);
                if (!CropWallpaperActivity.this.setAsWallpaper(jc, bitmap, false)) {
                    return null;
                }
                CropWallpaperActivity.this.backupOriginFile();
            }
            result.putExtra("set-as-theme-result", CropWallpaperActivity.this.mSetAsThemeResult);
            if (CropWallpaperActivity.this.mMediaItem.getFilePath().equals(CropWallpaperActivity.this.mBackupWallpaperManager.getBackupWallpaper())) {
                themeWallpaperPath = CropWallpaperActivity.this.mCurrentWallpaperPath;
            } else {
                themeWallpaperPath = CropWallpaperActivity.this.mMediaItem.getFilePath();
            }
            result.putExtra("wallpaper-file-path", themeWallpaperPath);
            CropWallpaperActivity.this.mSaveFinished = true;
            return result;
        }

        public boolean needDecodeVideoFromOrigin() {
            return false;
        }

        public String clazz() {
            return getClass().getName();
        }

        public boolean isHeavyJob() {
            return true;
        }

        public String workContent() {
            return String.format("save croped image file %s", new Object[]{CropWallpaperActivity.this.mMediaItem.getFilePath()});
        }
    }

    private class StateRestorer {
        private boolean mDone;

        private StateRestorer() {
            this.mDone = false;
        }

        public void run() {
            if (!this.mDone && CropWallpaperActivity.this.mShowSelectionMenu) {
                String oldState = CropWallpaperActivity.this.mBackupWallpaperManager.getWallpaperState();
                if (oldState != null) {
                    String[] stateParams = oldState.split("_");
                    if (stateParams.length >= 3) {
                        CropWallpaperActivity.this.mScrollable = Boolean.valueOf(stateParams[0]).booleanValue();
                        CropWallpaperActivity.this.mBlurFactor = Integer.valueOf(stateParams[1]).intValue();
                        String viewState = stateParams[2];
                        CropWallpaperActivity.this.mBlurSeekbar.setProgress(CropWallpaperActivity.this.mBlurFactor);
                        if (CropWallpaperActivity.this.mBlurFactor > 0) {
                            CropWallpaperActivity.this.mCheckListener.onClick(CropWallpaperActivity.this.findViewById(R.id.wallpaper_control_blur));
                        }
                        if (CropWallpaperActivity.this.mScrollable) {
                            CropWallpaperActivity.this.mCheckListener.onClick(CropWallpaperActivity.this.findViewById(R.id.wallpaper_control_scroll));
                        }
                        CropWallpaperActivity.this.mCropView.setState(viewState);
                        if (stateParams.length > 3) {
                            CropWallpaperActivity.this.mWallpaperFrom = stateParams[3];
                            if (!"download".equals(CropWallpaperActivity.this.mWallpaperFrom) || CropWallpaperActivity.this.mDownloadOrThemeCurrentItem) {
                                CropWallpaperActivity.this.setCurrentItem(CropWallpaperActivity.this.mCurrentItem);
                            } else {
                                CropWallpaperActivity.LOG.d("delete download item");
                                CropWallpaperActivity.this.removeViewItem(CropWallpaperActivity.this.mCurrentItem);
                                CropWallpaperActivity.this.mCurrentItem = null;
                            }
                        }
                    }
                }
                this.mDone = true;
            }
        }
    }

    private class ThemeLoader implements FutureListener<Bitmap>, Runnable {
        private Bitmap mBitmap;
        private Job<Bitmap> mJob;
        private MediaItem mMediaItem;
        private ThreadPool mThreadPool;
        private LabelView mView;

        ThemeLoader(ThreadPool pool, LabelView view, MediaItem item) {
            this.mThreadPool = pool;
            this.mView = view;
            this.mMediaItem = item;
        }

        void submit() {
            this.mThreadPool.submit(this.mJob != null ? this.mJob : this.mMediaItem.requestImage(8), this);
        }

        public void onFutureDone(Future<Bitmap> future) {
            this.mBitmap = (Bitmap) future.get();
            CropWallpaperActivity.this.mMainHandler.post(this);
        }

        public void run() {
            if (this.mBitmap == null) {
                CropWallpaperActivity.this.removeViewItem(this.mView.getTag(50331648) != null ? this.mView.getParent() : this.mView);
                return;
            }
            this.mView.setBitmap(this.mBitmap);
            this.mView.invalidate();
        }
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
            iArr[Action.BACK.ordinal()] = 1;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Action.CANCEL_DETAIL.ordinal()] = 10;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Action.COLLAGE.ordinal()] = 11;
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
            iArr[Action.DYNAMIC_ALBUM.ordinal()] = 17;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[Action.EDIT.ordinal()] = 18;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[Action.EDIT_COMMENT.ordinal()] = 19;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[Action.GOTO_GALLERY.ordinal()] = 20;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[Action.HIDE.ordinal()] = 21;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[Action.INFO.ordinal()] = 22;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[Action.KEYGUARD_LIKE.ordinal()] = 23;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[Action.KEYGUARD_NOT_LIKE.ordinal()] = 24;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[Action.LOOPPLAY.ordinal()] = 25;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[Action.MAP.ordinal()] = 26;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[Action.MENU.ordinal()] = 27;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[Action.MORE_EDIT.ordinal()] = 28;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[Action.MOVE.ordinal()] = 29;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[Action.MOVEIN.ordinal()] = 30;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[Action.MOVEOUT.ordinal()] = 31;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[Action.MULTISCREEN.ordinal()] = 32;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[Action.MULTISCREEN_ACTIVITED.ordinal()] = 33;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[Action.MULTI_SELECTION.ordinal()] = 34;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[Action.MULTI_SELECTION_ON.ordinal()] = 35;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[Action.MYFAVORITE.ordinal()] = 36;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[Action.NO.ordinal()] = 2;
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
            iArr[Action.OK.ordinal()] = 3;
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

    public void onSystemUiVisibilityChange(int visibility) {
        if (visibility == 0) {
            this.mMainHandler.removeMessages(7);
            this.mMainHandler.sendEmptyMessageDelayed(7, (long) HIDE_BARS_TIMEOUT);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extra = getIntent().getExtras();
        if (extra == null || BundleUtils.isValid(extra)) {
            this.mDataManager = getDataManager();
            initializeByIntent();
            setContentView(R.layout.layout_gl_activity);
            this.mGLRootView = getGLRoot();
            setOrientationForTablet();
            this.mMainHandler = new SynchronizedHandler(this.mGLRootView) {
                public void handleMessage(Message message) {
                    switch (message.what) {
                        case 2:
                            CropWallpaperActivity.this.dismissProgressDialogIfShown();
                            CropWallpaperActivity.this.onBitmapAvailable((Bitmap) message.obj);
                            return;
                        case 3:
                            CropWallpaperActivity.this.dismissProgressDialogIfShown();
                            Intent result = message.obj;
                            GalleryLog.d("CropWallpaperActivity", "sendBroadcast with action :com.huawei.gallery.action.SET_WALLPAPER");
                            Intent intent = new Intent(result);
                            intent.setPackage("com.huawei.android.thememanager");
                            intent.setAction("com.huawei.gallery.action.SET_WALLPAPER");
                            CropWallpaperActivity.this.sendBroadcast(intent);
                            CropWallpaperActivity.LOG.d("wallpaper file: " + intent.getStringExtra("wallpaper-file-path"));
                            CropWallpaperActivity.this.setResult(-1, result);
                            if (result.getBooleanExtra("exit-after-save", true)) {
                                CropWallpaperActivity.this.finish();
                                return;
                            }
                            return;
                        case 4:
                            CropWallpaperActivity.this.dismissProgressDialogIfShown();
                            ContextedUtils.showToastQuickly(CropWallpaperActivity.this.getActivityContext(), (int) R.string.save_error_Toast, 1);
                            CropWallpaperActivity.this.finish();
                            return;
                        case 5:
                            CropWallpaperActivity.this.setResult(1);
                            CropWallpaperActivity.this.finish();
                            return;
                        case 8:
                            CropWallpaperActivity.this.showLoadingMessage();
                            return;
                        default:
                            return;
                    }
                }
            };
            HwCustCropWallpaperActivity custCropWallpaperActivity = (HwCustCropWallpaperActivity) HwCustUtilsWrapper.createObj(HwCustCropWallpaperActivity.class, new Object[0]);
            if (custCropWallpaperActivity != null) {
                this.mIsShowCustWallpaperFirst = custCropWallpaperActivity.isShowCustWallpaperFirst(this);
            }
            this.mRootLayout = (ViewGroup) findViewById(R.id.gallery_main_root);
            this.mFeature = 768;
            requestNaviFeature(this.mRootLayout);
            requestWindow(78, this.mRootLayout);
            initializeView();
            this.mOutputW = getWallpaperDesiredMinimumWidth();
            this.mOutputH = getWallpaperDesiredMinimumHeight();
            this.mSize = getDefaultDisplaySize(this, new Point());
            this.mCropView.setScrollWallpaperSize(this.mOutputW, this.mOutputH);
            this.mCropView.setFixedWallpaperSize(this.mSize.x, this.mSize.y);
            this.mMinSideLength = Math.min(this.mSize.x, this.mSize.y);
            LOG.d(String.format("WallpaperDesired size (%sx%s)", new Object[]{Integer.valueOf(this.mOutputW), Integer.valueOf(this.mOutputH)}));
            LOG.d(String.format("display size (%sx%s)", new Object[]{Integer.valueOf(this.mSize.x), Integer.valueOf(this.mSize.y)}));
            this.mActionMode = (ActionMode) getGalleryActionBar().enterModeForced(false, 2);
            launchMode(this.mActionMode);
            this.mGLRootView.requestFullScreenLayout();
            this.mGLRootView.setContentPane(this.mCropView);
            this.mMainHandler.sendEmptyMessageDelayed(8, 1000);
            return;
        }
        GalleryLog.w("CropWallpaperActivity", "extras is illegal.");
        finish();
    }

    private void showLoadingMessage() {
        if (!this.mLoadingCanceled) {
            this.mProgressDialog = ProgressDialog.show(this, null, getString(R.string.loading_image), true, true);
            this.mProgressDialog.setCanceledOnTouchOutside(false);
            this.mProgressDialog.setCancelMessage(this.mMainHandler.obtainMessage(5));
        }
    }

    public void initializeView() {
        this.mCropView = new CropWallpaperView(this);
        this.mCropView.setBackgroundColor(GalleryUtils.intColorToFloatARGBArray(getResources().getColor(17170446)));
        View.inflate(this, R.layout.wallpaper_controls, this.mRootLayout);
        this.mWallpaperControlsRoot = (LinearLayout) findViewById(R.id.wallpaper_controls_root);
        updateMenu(this.mWallpaperControlsRoot);
        this.mSeekbarControls = (LinearLayout) findViewById(R.id.seekbar_controls);
        setupSeekbar();
        this.mScrollableSwitch = (TextView) findViewById(R.id.wallpaper_control_scroll);
        TextView blur = (TextView) findViewById(R.id.wallpaper_control_blur);
        setOnClickListener(this.mCheckListener, null, blur, this.mScrollableSwitch);
        if (!GalleryUtils.FIXED_WALLPAPER_ENANBLED) {
            this.mScrollable = true;
            this.mCropView.setScrollableWallper(this.mScrollable);
            hideScrollButton();
        }
        if (GalleryUtils.isTabletProduct(this.mGLRootView.getContext())) {
            setBottomMargin(this.mWallpaperControlsRoot);
        }
        if (this.mShowSelectionMenu) {
            this.mCropView.setHasItems(true);
            this.mListItemsLayout = (LinearLayout) findViewById(R.id.list_items);
            this.mListItemsLayout.setVisibility(0);
            LabelView viewItem = (LabelView) this.mWallpaperControlsRoot.findViewById(R.id.online);
            viewItem.setText(getString(R.string.more));
            viewItem.setUseFrame(false);
            getThreadPool().submit(new DefaultIconLoader(viewItem));
            viewItem = (LabelView) this.mWallpaperControlsRoot.findViewById(R.id.gallery);
            viewItem.setText(getString(R.string.app_name));
            viewItem.setUseFrame(false);
            getThreadPool().submit(new DefaultIconLoader(viewItem));
            setOnClickListener(this.mViewItemListener, null, R.id.online, R.id.gallery);
            ModuleInfo moduleInfo = ModuleInfo.loadModuleInfo(this, "home_wallpaper");
            this.mCurrentWallpaperPath = moduleInfo == null ? "" : moduleInfo.getPreviewPath();
            if (this.mCurrentWallpaperPath == null) {
                this.mCurrentWallpaperPath = "";
            }
            LOG.d("currentWallpaper:  " + this.mCurrentWallpaperPath);
            addThemeItem();
            addDownloadItem();
            if (this.mCurrentItem == null) {
                String path;
                String backupWallpaper = this.mBackupWallpaperManager.getBackupWallpaper();
                if (backupWallpaper != null) {
                    path = this.mDataManager.findPathByUri(Uri.parse("file://" + backupWallpaper), "image/*").toString();
                    this.mRotation = this.mBackupWallpaperManager.getBackupWallpaperRotation();
                } else {
                    path = WALLPAPER_CURRENT_USED.toString();
                    this.mCurrentStateJob.mDone = true;
                }
                viewItem = new LabelView(this);
                viewItem.setTag(path);
                viewItem.setTag(33554432, Integer.valueOf(this.mRotation));
                Path.fromString(path).clearObject();
                MediaItem mediaItem = getMediaItemFromPath(path);
                if (mediaItem != null) {
                    this.mMediaPath = path;
                    this.mCurrentItem = viewItem;
                    this.mDownloadOrThemeCurrentItem = false;
                    new ThemeLoader(getThreadPool(), viewItem, mediaItem).submit();
                    setOnClickListener(this.mViewItemListener, null, viewItem);
                    this.mListItemsLayout.addView(viewItem, 2, new LayoutParams(WallpaperConstant.VIEW_ITEM_SIZE, WallpaperConstant.VIEW_ITEM_SIZE));
                    this.mOnceOnlyFailedSaver = new FailSaver(this, viewItem, this.mDataManager.findPathByUri(Uri.parse("file://" + new File(WallpaperConstant.PATH_WALLPAPER, "gallery_home_wallpaper_0.jpg").getAbsolutePath()), "image/*").toString()) {
                        public boolean save() {
                            boolean ret = super.save();
                            this.mCurrentStateJob.mDone = true;
                            this.mOnceOnlyFailedSaver = null;
                            return ret;
                        }
                    };
                }
            }
        }
        this.mCurrentWallpaper = this.mCurrentItem;
        LOG.d("View init finish.");
    }

    private void addThemeItem() {
        boolean z;
        List<ThemeInfo> themes = ThemeInfo.getThemeInstallInfo(this);
        this.mThemesCount = themes.size();
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService("layout_inflater");
        String mccmnc = SystemProperties.get("persist.sys.mccmnc", "");
        if (TextUtils.isEmpty(mccmnc) || !mccmnc.startsWith("730")) {
            z = SystemProperties.getBoolean("ro.config.del_cust_wallpaper", false);
        } else {
            z = true;
        }
        this.MCCMNC_ENABLE = z;
        for (ThemeInfo themeInfo : themes) {
            for (File f : themeInfo.getWallpaper()) {
                String path = this.mDataManager.findPathByUri(Uri.parse("file://" + f.getAbsolutePath()), "image/*").toString();
                View themeItemView = inflater.inflate(R.layout.wallpaper_view_item, null, false);
                View viewItem = (LabelView) themeItemView.findViewById(R.id.wallpaper_Item);
                ImageView deleteItemImageView = (ImageView) themeItemView.findViewById(R.id.delete_wallpaper);
                viewItem.setTag(path);
                boolean canDele = (this.MCCMNC_ENABLE && path.contains("customization")) ? !path.contains("home_wallpaper_0_default.jpg") : false;
                if (this.MCCMNC_ENABLE && this.mCurrentWallpaperDefault == null && path.contains("customization") && path.contains("home_wallpaper_0_default.jpg")) {
                    this.mCurrentWallpaperDefault = viewItem;
                    this.mCurrentWallpaperPathDefault = f.getAbsolutePath();
                }
                if (canDele) {
                    deleteItemImageView.setVisibility(this.mInDeleteMode ? 0 : 8);
                    deleteItemImageView.setOnClickListener(this.mDeleteDownloadPicListener);
                    themeItemView.setTag(67108864, f);
                    deleteItemImageView.setTag(themeItemView);
                }
                themeItemView.setTag(path);
                if (this.mCurrentWallpaperPath.equals(f.getAbsolutePath())) {
                    this.mMediaPath = path;
                    setCurrentItem(viewItem);
                }
                MediaItem mediaItem = getMediaItemFromPath(path);
                if (mediaItem == null) {
                    LOG.d("can't find media for path: " + path);
                } else {
                    new ThemeLoader(getThreadPool(), viewItem, mediaItem).submit();
                    setOnClickListener((OnClickListener) this.mViewItemListener, (OnLongClickListener) canDele ? this.mViewItemListener : null, viewItem);
                    if (this.mCurrentItem == viewItem && this.mIsShowCustWallpaperFirst) {
                        this.mListItemsLayout.addView(themeItemView, 2, new LayoutParams(WallpaperConstant.VIEW_ITEM_SIZE, WallpaperConstant.VIEW_ITEM_SIZE));
                    } else {
                        this.mListItemsLayout.addView(themeItemView, WallpaperConstant.VIEW_ITEM_SIZE, WallpaperConstant.VIEW_ITEM_SIZE);
                    }
                }
            }
        }
    }

    private void addDownloadItem() {
        if (this.mListItemsLayout != null) {
            int totalCount = this.mListItemsLayout.getChildCount();
            int removePointer = 0;
            for (int i = 0; i < totalCount; i++) {
                if (this.mListItemsLayout.getChildAt(removePointer).getTag(50331648) == null) {
                    removePointer++;
                } else {
                    this.mListItemsLayout.removeViewAt(removePointer);
                }
            }
            List<File> downloadFilesList = WallpaperUtils.queryDownloadedWallpaper(getContentResolver());
            if (downloadFilesList.size() > 0) {
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService("layout_inflater");
                String backupWallpaper = this.mBackupWallpaperManager.getBackupWallpaper();
                String backupPath = this.mDataManager.findPathByUri(Uri.parse("file://" + backupWallpaper), "image/*").toString();
                int addPosition = 2;
                if (this.mListItemsLayout.getChildCount() > this.mThemesCount + 2) {
                    addPosition = 3;
                }
                for (File file : downloadFilesList) {
                    MediaItem mediaItem;
                    String fileAbsolutePath = file.getAbsolutePath();
                    String path = this.mDataManager.findPathByUri(Uri.parse("file://" + fileAbsolutePath), "image/*").toString();
                    View downLoadItemView = inflater.inflate(R.layout.wallpaper_view_item, null, false);
                    View viewItem = (LabelView) downLoadItemView.findViewById(R.id.wallpaper_Item);
                    ImageView deleteItemImageView = (ImageView) downLoadItemView.findViewById(R.id.delete_wallpaper);
                    deleteItemImageView.setVisibility(this.mInDeleteMode ? 0 : 8);
                    deleteItemImageView.setOnClickListener(this.mDeleteDownloadPicListener);
                    downLoadItemView.setTag(50331648, file);
                    viewItem.setTag(50331648, file);
                    deleteItemImageView.setTag(downLoadItemView);
                    if (!(this.mCurrentItem == null && fileAbsolutePath.equals(this.mCurrentWallpaperPath)) && (this.mCurrentItem == null || !file.equals(this.mCurrentItem.getTag(50331648)))) {
                        String itemPath;
                        if (fileAbsolutePath.equals(this.mCurrentWallpaperPath)) {
                            itemPath = backupPath;
                        } else {
                            itemPath = path;
                        }
                        mediaItem = getMediaItemFromPath(itemPath);
                        viewItem.setTag(itemPath);
                        downLoadItemView.setTag(itemPath);
                    } else {
                        String currentPath;
                        setCurrentItem(viewItem);
                        if ((this.mCurrentWallpaper == null || this.mCurrentItem.getTag(50331648).equals(this.mCurrentWallpaper.getTag(50331648))) && !TextUtils.isEmpty(backupWallpaper)) {
                            currentPath = backupPath;
                        } else {
                            currentPath = path;
                        }
                        Path.fromString(currentPath).clearObject();
                        this.mMediaPath = currentPath;
                        mediaItem = getMediaItemFromPath(currentPath);
                        viewItem.setTag(currentPath);
                        downLoadItemView.setTag(currentPath);
                    }
                    if (mediaItem == null) {
                        LOG.d("can't find media for path: " + path);
                    } else {
                        new ThemeLoader(getThreadPool(), viewItem, mediaItem).submit();
                        setOnClickListener((OnClickListener) this.mViewItemListener, (OnLongClickListener) this.mViewItemListener, viewItem);
                        if (this.mListItemsLayout.getChildCount() <= 2) {
                            this.mListItemsLayout.addView(downLoadItemView);
                        } else {
                            this.mListItemsLayout.addView(downLoadItemView, addPosition, new LayoutParams(WallpaperConstant.VIEW_ITEM_SIZE, WallpaperConstant.VIEW_ITEM_SIZE));
                        }
                    }
                }
            }
        }
    }

    private void setupSeekbar() {
        setOnClickListener(this.mSeekbarAdjClickListener, null, R.id.progress_minus, R.id.progress_plus);
        this.mBlurSeekbar = (SeekBar) findViewById(R.id.progress_seekbar);
        int color = ColorfulUtils.mappingColorfulColor(this, 0);
        if (color != 0) {
            this.mBlurSeekbar.getThumb().setTint(color);
            Drawable drawable = this.mBlurSeekbar.getProgressDrawable().getCurrent();
            if (drawable instanceof LayerDrawable) {
                Drawable drawable1 = ((LayerDrawable) drawable).findDrawableByLayerId(16908301);
                if (drawable1 != null) {
                    drawable1.setTint(color);
                }
            }
        }
        this.mBlurSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                CropWallpaperActivity.this.mIsInProgress = false;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                CropWallpaperActivity.this.mIsInProgress = true;
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                CropWallpaperActivity.this.mBlurFactor = progress;
                CropWallpaperActivity.this.mCropView.setBlurFactor(progress);
            }
        });
    }

    private void hideScrollButton() {
        if (this.mScrollableSwitch != null) {
            this.mScrollableSwitch.setVisibility(8);
        }
        findViewById(R.id.wallpaper_center_space).setVisibility(8);
    }

    private void doDelete(View view) {
        File file;
        View targetView = (View) view.getTag();
        if (targetView.getTag(50331648) == null) {
            file = (File) targetView.getTag(67108864);
        } else {
            file = (File) targetView.getTag(50331648);
        }
        WallpaperUtils.deleteDownloadedWallpaper(getContentResolver(), file);
        removeViewItem(targetView);
    }

    private void removeViewItem(View target) {
        if (this.MCCMNC_ENABLE && ((String) this.mCurrentWallpaper.getTag()).contains("customization") && this.mCurrentWallpaper == ((LabelView) target.findViewById(R.id.wallpaper_Item))) {
            this.mCurrentWallpaper = this.mCurrentWallpaperDefault;
            this.mDoSetWallpaperDefault = true;
        }
        if (target.findViewById(R.id.wallpaper_Item) == this.mCurrentItem || this.mDoSetWallpaperDefault) {
            setCurrentPreview(this.mCurrentWallpaper);
            for (int i = 2; i < this.mListItemsLayout.getChildCount(); i++) {
                if (this.mListItemsLayout.getChildAt(i).getTag().equals(this.mCurrentWallpaper.getTag())) {
                    setCurrentItem(this.mListItemsLayout.getChildAt(i));
                    break;
                }
            }
        }
        this.mListItemsLayout.removeView(target);
    }

    private void setOnClickListener(OnClickListener clickListener, OnLongClickListener longClickListener, int... viewIds) {
        if (viewIds != null) {
            for (int viewId : viewIds) {
                View view = findViewById(viewId);
                if (view != null) {
                    view.setOnClickListener(clickListener);
                    view.setOnLongClickListener(longClickListener);
                }
            }
        }
    }

    private void setOnClickListener(OnClickListener clickListener, OnLongClickListener longClickListener, View... views) {
        if (views != null) {
            for (View view : views) {
                view.setOnClickListener(clickListener);
                view.setOnLongClickListener(longClickListener);
            }
        }
    }

    private void setCurrentItem(View current) {
        if (this.mCurrentItem != null) {
            this.mCurrentItem.setSelected(false);
        }
        this.mCurrentItem = current;
        if (current != null) {
            current.setSelected(true);
        }
    }

    private void changeDeleteMode(boolean isDeleteMode) {
        for (int i = 2; i < this.mListItemsLayout.getChildCount(); i++) {
            View viewItem = this.mListItemsLayout.getChildAt(i);
            if (viewItem.getTag(50331648) != null || viewItem.getTag(67108864) != null) {
                viewItem.findViewById(R.id.delete_wallpaper).setVisibility(isDeleteMode ? 0 : 8);
            }
        }
        this.mInDeleteMode = isDeleteMode;
        if (this.mInDeleteMode) {
            this.mActionMode.setBothAction(Action.BACK, Action.NONE);
        } else {
            this.mActionMode.setBothAction(Action.NO, Action.OK);
        }
        this.mActionMode.setTitle(this.mInDeleteMode ? R.string.del_download_wallpaper : R.string.set_wallpaper);
        this.mActionMode.show();
        int showOrNot = isDeleteMode ? 8 : 0;
        this.mScrollableSwitch.setVisibility(showOrNot);
        View blur = findViewById(R.id.wallpaper_control_blur);
        blur.setVisibility(showOrNot);
        if (blur.isSelected()) {
            this.mSeekbarControls.setVisibility(showOrNot);
        }
    }

    private void setCurrentPreview(View v) {
        String path = (String) v.getTag();
        Integer rotation = (Integer) v.getTag(33554432);
        if (!(path == null || path.equals(this.mMediaPath) || !switchToPath(path))) {
            if (rotation == null) {
                rotation = Integer.valueOf(this.mMediaItem.getRotation());
            }
            this.mRotation = rotation.intValue();
            initializeData();
        }
        setCurrentItem(v);
        reportDataForWallpaperAction(ActionType.PREVIEW);
    }

    private void launchMode(ActionMode actionMode) {
        actionMode.setTitle((int) R.string.set_wallpaper);
        String mMode = this.mSetAsTheme;
        actionMode.setBothAction(Action.NO, Action.OK);
        this.mSetAsThemeResult = mMode;
        if ("both".equals(mMode)) {
            setAs(true, true);
        } else if ("home".equals(mMode)) {
            actionMode.setTitle((int) R.string.setas_home);
            setAs(true, false);
        } else if ("unlock".equals(mMode)) {
            hideScrollButton();
            actionMode.setTitle((int) R.string.setas_lock);
            setAs(false, true);
        } else {
            setAs(false, false);
            this.mSetAsTheme = null;
        }
        actionMode.setHeadBarSplitLineVisibility(false);
        actionMode.show();
    }

    private void setAs(boolean wallpaper, boolean lockScreen) {
        this.mSetWallpaper = wallpaper;
        this.mSetLockScreen = lockScreen;
    }

    public void onActionItemClicked(Action action) {
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
            case 2:
                if (this.mInDeleteMode) {
                    onBackPressed();
                    return;
                }
                this.mMainHandler.sendEmptyMessage(5);
                if (this.mShowSelectionMenu) {
                    reportDataForWallpaperAction(ActionType.CLOSE);
                    return;
                }
                return;
            case 3:
                onSaveClicked();
                return;
            default:
                return;
        }
    }

    public void onBackPressed() {
        if (this.mInDeleteMode) {
            changeDeleteMode(false);
            return;
        }
        setResult(1);
        finish();
    }

    private void backupOriginFile() {
        String from;
        if (this.mCurrentItem != null && this.mCurrentItem.getTag(50331648) != null) {
            from = "download";
        } else if ("download".equals(this.mWallpaperFrom) && this.mCurrentItem == null) {
            from = "download";
        } else if (WallpaperUtils.queryDownloadedWallpaperByMediaItem(getContentResolver(), this.mMediaItem)) {
            from = "download";
        } else {
            from = "other";
        }
        LOG.d("from: " + from);
        this.mBackupWallpaperManager.backupWallpaper(this.mMediaItem.getFilePath(), String.format(Locale.US, "%s_%s_%s_%s", new Object[]{Boolean.valueOf(this.mScrollable), Integer.valueOf(this.mBlurFactor), this.mCropView.getState(), from}), this.mRotation);
    }

    private boolean saveBitmapToOutputStream(JobContext jc, Bitmap bitmap, CompressFormat format, OutputStream os) {
        Closeable ios = new InterruptableOutputStream(os);
        jc.setCancelListener(new OutputStreamInterrupter(ios));
        try {
            bitmap.compress(format, 90, ios);
            boolean z = !jc.isCancelled();
            jc.setCancelListener(null);
            Utils.closeSilently(ios);
            return z;
        } catch (Throwable th) {
            jc.setCancelListener(null);
            Utils.closeSilently(ios);
        }
    }

    private boolean saveBitmapToUri(JobContext jc, Bitmap bitmap, Uri uri) {
        Closeable outputStream;
        try {
            outputStream = getContentResolver().openOutputStream(uri);
            boolean saveBitmapToOutputStream = saveBitmapToOutputStream(jc, bitmap, convertExtensionToCompressFormat(getFileExtension()), outputStream);
            Utils.closeSilently(outputStream);
            return saveBitmapToOutputStream;
        } catch (FileNotFoundException e) {
            LOG.w("cannot write output", e);
            return true;
        } catch (Throwable th) {
            Utils.closeSilently(outputStream);
        }
    }

    private CompressFormat convertExtensionToCompressFormat(String extension) {
        return extension.equals("png") ? CompressFormat.PNG : CompressFormat.JPEG;
    }

    private String getFileExtension() {
        String requestFormat = getIntent().getStringExtra("outputFormat");
        String outputFormat = (requestFormat == null ? determineCompressFormat(this.mMediaItem) : requestFormat).toLowerCase(Locale.ENGLISH);
        return (outputFormat.equals("png") || outputFormat.equals("gif")) ? "png" : "jpg";
    }

    public static String determineCompressFormat(MediaObject obj) {
        String compressFormat = "JPEG";
        if (!(obj instanceof MediaItem)) {
            return compressFormat;
        }
        String mime = ((MediaItem) obj).getMimeType();
        if (mime.contains("png") || mime.contains("gif")) {
            return "PNG";
        }
        return compressFormat;
    }

    private void saveLockscreenBitmap(JobContext jc, Bitmap cropped, boolean isHome) {
        if (cropped != null) {
            BitmapOutputer outputer;
            String targetFileName = isHome ? "gallery_home_wallpaper_0.jpg" : "unlock_wallpaper_0.jpg";
            if (isHome) {
                GalleryLog.w("CropWallpaperActivity", "isHome");
                outputer = new BitmapOutputer(cropped, CompressFormat.JPEG, 90);
            } else {
                GalleryLog.w("CropWallpaperActivity", "not isHome");
                outputer = new BitmapOutputer(cropped, CompressFormat.PNG, 90);
            }
            saveBitmapToFile(WallpaperConstant.PATH_WALLPAPER, targetFileName, outputer);
        }
    }

    private void saveBitmapToFile(String directory, String name, BitmapOutputer outputer) {
        Exception e;
        Throwable th;
        if (outputer == null) {
            LOG.d("nothing to save");
            return;
        }
        File dir = new File(directory);
        Closeable closeable = null;
        if (dir.isDirectory()) {
            try {
                File tempFile = new File(dir, name + ".temp");
                Closeable fileOutputSteam = new FileOutputStream(tempFile);
                try {
                    outputer.writeToStream(fileOutputSteam);
                    File targetFile = new File(dir, name);
                    renameFile(tempFile, targetFile);
                    FileUtils.setPermissions(targetFile.getPath(), 508, Process.myUid(), 1023);
                    Utils.closeSilently(fileOutputSteam);
                    closeable = fileOutputSteam;
                } catch (Exception e2) {
                    e = e2;
                    closeable = fileOutputSteam;
                    try {
                        LOG.d(e);
                        Utils.closeSilently(closeable);
                    } catch (Throwable th2) {
                        th = th2;
                        Utils.closeSilently(closeable);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    closeable = fileOutputSteam;
                    Utils.closeSilently(closeable);
                    throw th;
                }
            } catch (Exception e3) {
                e = e3;
                LOG.d(e);
                Utils.closeSilently(closeable);
            }
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

    private boolean setAsWallpaper(JobContext jc, Bitmap wallpaper, boolean isLockScreen) {
        try {
            WallpaperManager wallManager = WallpaperManager.getInstance(this);
            if (VERSION.SDK_INT > 23) {
                wallManager.setBitmap(wallpaper, null, true, isLockScreen ? 2 : 1);
            } else if (!isLockScreen) {
                wallManager.setBitmap(wallpaper);
            }
            return true;
        } catch (IOException e) {
            LOG.w("fail to set wall paper", e);
            return false;
        }
    }

    private void onSaveClicked() {
        if (this.mSetAsTheme != null) {
            beginSave(true);
        } else {
            new Builder(GalleryUtils.getHwThemeContext(this, "androidhwext:style/Theme.Emui.Dialog")).setItems(R.array.wallpaper_setas_option, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        CropWallpaperActivity.this.mSetAsThemeResult = "unlock";
                        CropWallpaperActivity.this.setAs(false, true);
                    } else if (which == 1) {
                        CropWallpaperActivity.this.mSetAsThemeResult = "home";
                        CropWallpaperActivity.this.setAs(true, false);
                    } else if (which == 2) {
                        CropWallpaperActivity.this.mSetAsThemeResult = "both";
                        CropWallpaperActivity.this.setAs(true, true);
                    } else {
                        return;
                    }
                    CropWallpaperActivity.this.beginSave(true);
                }
            }).create().show();
        }
    }

    private void resetToDefaultWallpaper() {
        this.mSetAsThemeResult = "both";
        setAs(true, true);
        beginSave(false);
    }

    private void beginSave(final boolean exitAfterSave) {
        printParameterInfo();
        reportDataForCropFragment();
        Rect cropRect = this.mCropView.getCropRectangle();
        if (this.mState != 2) {
            this.mState = 2;
            dismissProgressDialogIfShown();
            this.mProgressDialog = ProgressDialog.show(GalleryUtils.getHwThemeContext(this, "androidhwext:style/Theme.Emui.Dialog"), null, getString(R.string.wallpaper), true, false);
            this.mSaveTask = getThreadPool().submit(new SaveOutput(cropRect), new FutureListener<Intent>() {
                public void onFutureDone(Future<Intent> future) {
                    CropWallpaperActivity.this.mSaveTask = null;
                    if (!future.isCancelled() || CropWallpaperActivity.this.mSaveFinished) {
                        CropWallpaperActivity.this.mActivityGuard.notifyTofinish();
                        Intent intent = (Intent) future.get();
                        CropWallpaperActivity.LOG.d("cropped image, intent is " + intent);
                        if (intent != null) {
                            intent.putExtra("exit-after-save", exitAfterSave);
                            CropWallpaperActivity.this.mMainHandler.sendMessage(CropWallpaperActivity.this.mMainHandler.obtainMessage(3, intent));
                        } else {
                            CropWallpaperActivity.this.mMainHandler.sendEmptyMessage(4);
                        }
                    }
                }
            });
        }
    }

    private Bitmap getCroppedImage(Rect rect) {
        int outputY;
        if (rect.height() == 0) {
            int top = rect.top;
            if (top > 0) {
                rect.top = top - 1;
            } else {
                rect.bottom = top + 1;
            }
        }
        if (rect.width() == 0) {
            int left = rect.left;
            if (left > 0) {
                rect.left = left - 1;
            } else {
                rect.right = left + 1;
            }
        }
        int outputX = this.mScrollable ? this.mOutputW : this.mSize.x;
        if (this.mScrollable) {
            outputY = this.mOutputH;
        } else {
            outputY = this.mSize.y;
        }
        if (outputX * outputY > 5000000) {
            float scale = (float) Math.sqrt((5000000.0d / ((double) outputX)) / ((double) outputY));
            LOG.w("scale down the cropped image: " + scale);
            outputX = Math.round(((float) outputX) * scale);
            outputY = Math.round(((float) outputY) * scale);
        }
        Rect dest = new Rect(0, 0, outputX, outputY);
        float scaleY = ((float) outputY) / ((float) rect.height());
        int rectWidth = Math.round(((float) rect.width()) * (((float) outputX) / ((float) rect.width())));
        int rectHeight = Math.round(((float) rect.height()) * scaleY);
        dest.set(Math.round(((float) (outputX - rectWidth)) / 2.0f), Math.round(((float) (outputY - rectHeight)) / 2.0f), Math.round(((float) (outputX + rectWidth)) / 2.0f), Math.round(((float) (outputY + rectHeight)) / 2.0f));
        int rotation = this.mMediaItem.getRotation();
        LOG.d(String.format(" cropped region before rotate from %s to %s", new Object[]{rect, dest}));
        rotateRectangle(rect, this.mCropView.getImageWidth(), this.mCropView.getImageHeight(), 360 - rotation);
        rotateRectangle(dest, outputX, outputY, 360 - rotation);
        LOG.d(String.format(" cropped region after rotate from %s to %s", new Object[]{rect, dest}));
        Bitmap result = Bitmap.createBitmap(outputX, outputY, Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        rotateCanvas(canvas, outputX, outputY, rotation);
        canvas.drawBitmap(this.mBitmap, rect, dest, new Paint(2));
        int blurRadius = this.mCropView.getBlurRadius(this.mBlurFactor);
        LOG.d(String.format("imageSize(%sx%s), canvasSize(%sx%s) blurRadius=%s", new Object[]{Integer.valueOf(this.mCropView.getImageWidth()), Integer.valueOf(this.mCropView.getImageHeight()), Integer.valueOf(outputX), Integer.valueOf(outputY), Integer.valueOf(blurRadius)}));
        if (blurRadius == 0) {
            return result;
        }
        ReportToBigData.report(87);
        return StackBlurUtils.getBlurBitmap(this, result, blurRadius);
    }

    private static void rotateCanvas(Canvas canvas, int width, int height, int rotation) {
        int targetX = width / 2;
        int targetY = height / 2;
        canvas.translate((float) targetX, (float) targetY);
        canvas.rotate((float) rotation);
        if (((rotation / 90) & 1) == 0) {
            canvas.translate((float) (-targetX), (float) (-targetY));
        } else {
            canvas.translate((float) (-targetY), (float) (-targetX));
        }
    }

    private static void rotateRectangle(Rect rect, int width, int height, int rotation) {
        if (rotation != 0 && rotation != 360) {
            int rectW = rect.width();
            int rectH = rect.height();
            switch (rotation) {
                case WMComponent.ORI_90 /*90*/:
                    rect.top = rect.left;
                    rect.left = height - rect.bottom;
                    rect.right = rect.left + rectH;
                    rect.bottom = rect.top + rectW;
                    return;
                case 180:
                    rect.left = width - rect.right;
                    rect.top = height - rect.bottom;
                    rect.right = rect.left + rectW;
                    rect.bottom = rect.top + rectH;
                    return;
                case 270:
                    rect.left = rect.top;
                    rect.top = width - rect.right;
                    rect.right = rect.left + rectH;
                    rect.bottom = rect.top + rectW;
                    return;
                default:
                    throw new AssertionError();
            }
        }
    }

    private boolean loadImageFailed(Object arg) {
        if (arg != null) {
            return false;
        }
        ContextedUtils.showToastQuickly(getActivityContext(), (int) R.string.fail_to_load_image_Toast, 0);
        Object filePath = null;
        if (this.mMediaItem != null) {
            filePath = this.mMediaItem.getFilePath();
        }
        LOG.d("Can't decode media item, file path: " + filePath + ", file size:" + (TextUtils.isEmpty(filePath) ? "unknow" : Long.valueOf(new File(filePath).length())) + ", show selection menu: " + this.mShowSelectionMenu);
        if (!this.mShowSelectionMenu) {
            finish();
        }
        return true;
    }

    private void onBitmapAvailable(Bitmap bitmap) {
        if (bitmap == null && this.mOnceOnlyFailedSaver != null) {
            GalleryLog.d("CropWallpaperActivity", "decode failed, save wallpaper with mOnceOnlyFailedSaver");
            if (this.mOnceOnlyFailedSaver.save()) {
                return;
            }
        }
        if (!loadImageFailed(bitmap)) {
            this.mState = 1;
            this.mBitmap = bitmap;
            if (!this.mShowSelectionMenu) {
                this.mRotation = this.mMediaItem.getRotation();
            }
            this.mCropView.setDataModel(bitmap, this.mRotation);
            this.mCropView.setBlurFactor(this.mBlurFactor);
            if (this.mDoSetWallpaperDefault) {
                GalleryLog.d("CropWallpaperActivity", "set default wallpaper BOTH begin after preview completely done");
                this.mDoSetWallpaperDefault = false;
                resetToDefaultWallpaper();
            }
            this.mCurrentStateJob.run();
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
        int x = size.x;
        int y = size.y;
        if (x > y) {
            size.set(y, x);
        }
        return size;
    }

    private boolean switchToPath(String path) {
        String oldPath = this.mMediaPath;
        this.mMediaItem = getMediaItemFromPath(path);
        if (this.mMediaItem == null) {
            this.mMediaItem = getMediaItemFromPath(oldPath);
            return false;
        }
        this.mMediaPath = path;
        return true;
    }

    private void initializeData() {
        FutureListener<Bitmap> thumbListener = new FutureListener<Bitmap>() {
            public void onFutureDone(Future<Bitmap> future) {
                CropWallpaperActivity.this.mLoadBitmapTask = null;
                Bitmap bitmap = (Bitmap) future.get();
                if (future.isCancelled()) {
                    if (bitmap != null) {
                        bitmap.recycle();
                    }
                    return;
                }
                GalleryLog.d("CropWallpaperActivity", "future is not cancelled.");
                CropWallpaperActivity.this.mMainHandler.sendMessage(CropWallpaperActivity.this.mMainHandler.obtainMessage(2, bitmap));
            }
        };
        if (this.mLoadBitmapTask != null) {
            this.mLoadBitmapTask.cancel();
        }
        this.mLoadBitmapTask = loadBitmapData(thumbListener);
    }

    private Future<Bitmap> loadBitmapData(FutureListener<Bitmap> thumbListener) {
        return getThreadPool().submit(new LoadBitmapDataTask(this.mMediaItem), thumbListener);
    }

    public void onResume() {
        super.onResume();
        LOG.d("onResume");
        this.mActivityGuard.finishWait();
        switchToPath(this.mMediaPath);
        if (this.mState == 0 && !loadImageFailed(this.mMediaItem)) {
            initializeData();
        }
        if (this.mEnterOnline) {
            addDownloadItem();
            this.mEnterOnline = false;
        }
        this.mCropView.setScrollableWallper(this.mScrollable);
        this.mRootLayout.setOnSystemUiVisibilityChangeListener(this);
        this.mMainHandler.sendEmptyMessageDelayed(6, 300);
    }

    public void onPause() {
        super.onPause();
        this.mActivityGuard.releaseReceiverIfNeeded();
        dismissProgressDialogIfShown();
        Future<Bitmap> loadBitmapTask = this.mLoadBitmapTask;
        if (!(loadBitmapTask == null || loadBitmapTask.isDone())) {
            loadBitmapTask.cancel();
            loadBitmapTask.waitDone();
        }
        Future<Intent> saveTask = this.mSaveTask;
        if (!(saveTask == null || saveTask.isDone())) {
            saveTask.cancel();
            saveTask.waitDone();
        }
        if (this.mState == 2) {
            this.mState = 1;
        }
        this.mRootLayout.setOnSystemUiVisibilityChangeListener(null);
    }

    public void onDestroy() {
        super.onDestroy();
    }

    private void dismissProgressDialogIfShown() {
        this.mLoadingCanceled = true;
        if (this.mProgressDialog != null) {
            GalleryUtils.dismissDialogSafely(this.mProgressDialog, this);
            this.mProgressDialog = null;
        }
    }

    private MediaItem getMediaItemFromPath(String path) {
        if (path == null) {
            return null;
        }
        return (MediaItem) this.mDataManager.getMediaObject(path);
    }

    private boolean isFromContact() {
        Parcelable outPut = this.mOutputUri;
        if (outPut == null || !outPut.toString().contains("com.android.contacts")) {
            return false;
        }
        return true;
    }

    private void reportDataForCropFragment() {
        String format = "{SetAsType:%s,Scroll:%s}";
        if (this.mSetWallpaper && this.mSetLockScreen) {
            ReportToBigData.report(28, String.format(format, new Object[]{"Both", Boolean.valueOf(this.mScrollable)}));
        } else if (this.mSetWallpaper) {
            ReportToBigData.report(28, String.format(format, new Object[]{"HomeWallPaper", Boolean.valueOf(this.mScrollable)}));
        } else if (this.mSetLockScreen) {
            ReportToBigData.report(28, String.format(format, new Object[]{"LockscreenWallPaper", ""}));
        } else if (isFromContact()) {
            ReportToBigData.report(28, String.format(format, new Object[]{"ContactImage", ""}));
        }
    }

    private void reportDataForWallpaperAction(ActionType action) {
        ReportToBigData.report(104, String.format("{WallpaperOperation:%s}", new Object[]{action.toString()}));
    }

    protected void initializeByIntent() {
        Path path = getPathFromIntentData();
        if (path == null) {
            LOG.w("cannot get path for: " + getIntent().getData() + ", or no data given");
            this.mShowSelectionMenu = true;
        } else {
            this.mShowSelectionMenu = false;
            this.mMediaPath = path.toString();
            path.clearObject();
        }
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.mOutputUri = (Uri) extras.getParcelable("output");
            this.mOutputFilePath = GalleryUtils.convertUriToPath(this, this.mOutputUri);
            this.mSetAsTheme = extras.getString("set-as-theme");
            LOG.w("set as theme for target : " + this.mSetAsTheme);
            this.mAspectX = extras.getInt("aspectX", 0);
            this.mAspectY = extras.getInt("aspectY", 0);
            this.mSpotlightX = extras.getFloat("spotlightX", 0.0f);
            this.mSpotlightY = extras.getFloat("spotlightY", 0.0f);
        }
        this.mInputFilePath = GalleryUtils.convertUriToPath(this, getIntent().getData());
        printParameterInfo();
    }

    private void printParameterInfo() {
        LOG.d(String.format(Locale.US, "parameters: setAsTheme:%s, setAsThemeResult:%s, setWallpaper:%s, setLockScreen:%s,", new Object[]{this.mSetAsTheme, this.mSetAsThemeResult, Boolean.valueOf(this.mSetWallpaper), Boolean.valueOf(this.mSetLockScreen)}));
    }

    private Path getPathFromIntentData() {
        return this.mDataManager.findPathByUri(getIntent().getData(), getIntent().getType());
    }

    public void onNavigationBarChanged(boolean show, int height) {
        this.mCropView.requestLayout();
        if (this.mWallpaperControlsRoot != null) {
            updateMenu(this.mWallpaperControlsRoot);
        }
        if (GalleryUtils.isTabletProduct(this.mGLRootView.getContext())) {
            setBottomMargin(this.mWallpaperControlsRoot);
            setRightMargin(this.mWallpaperControlsRoot);
        }
    }

    private void updateMenu(View view) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.bottomMargin = LayoutHelper.getNavigationBarHeight();
        view.setLayoutParams(params);
    }

    public void requestNaviFeature(View view) {
        int feature = this.mFeature;
        if ((feature & 256) != 0) {
            UIUtils.setNavigationBarIsOverlay(view, true);
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
        getWindow().addFlags(1024);
        if (ApiHelper.HAS_MODIFY_STATUS_BAR_COLOR) {
            UIUtils.setNavigationBarColor(getWindow(), 0);
            UIUtils.setStatusBarColor(getWindow(), 0);
        }
        getGalleryActionBar().setHeadBackground(0);
        getGalleryActionBar().setActionPanelStyle(1);
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

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setBottomMargin(this.mWallpaperControlsRoot);
    }

    private void setRightMargin(View view) {
        if (view != null) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
            if (LayoutHelper.isPort()) {
                params.rightMargin = 0;
            } else {
                params.rightMargin = LayoutHelper.getNavigationBarHeight();
            }
            view.setLayoutParams(params);
        }
    }

    private void setBottomMargin(View view) {
        if (view != null) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
            if (LayoutHelper.isPort()) {
                params.bottomMargin = LayoutHelper.getNavigationBarHeight();
            } else {
                params.bottomMargin = 0;
            }
            if (!(!LayoutHelper.isDefaultLandOrientationProduct() || LayoutHelper.isPort() || MultiWindowStatusHolder.isInMultiWindowMode())) {
                params.bottomMargin += LayoutHelper.getNavigationBarHeightForDefaultLand();
            }
            view.setLayoutParams(params);
        }
    }
}
