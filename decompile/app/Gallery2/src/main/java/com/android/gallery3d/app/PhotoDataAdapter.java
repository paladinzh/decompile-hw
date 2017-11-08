package com.android.gallery3d.app;

import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.os.Handler;
import android.os.Message;
import com.android.gallery3d.R;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.BitmapPool;
import com.android.gallery3d.data.ContentListener;
import com.android.gallery3d.data.GalleryImage;
import com.android.gallery3d.data.IImage;
import com.android.gallery3d.data.IVideo;
import com.android.gallery3d.data.LocalMediaItem;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.AbsPhotoView;
import com.android.gallery3d.ui.AbsPhotoView.Size;
import com.android.gallery3d.ui.BitmapScreenNail;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.ScreenNail;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.ui.TileImageViewAdapter;
import com.android.gallery3d.ui.TiledScreenNail;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.BusinessRadar.BugType;
import com.android.gallery3d.util.DisplayEngineUtils;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.MediaSetUtils;
import com.android.gallery3d.util.PerformanceRadar.Reporter;
import com.android.gallery3d.util.ThreadPool;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.android.gallery3d.util.ThumbnailReporter;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.app.AbsPhotoPage.Model;
import com.huawei.gallery.displayengine.BoostFullScreenNailDisplay;
import com.huawei.gallery.displayengine.ScreenNailCommonDisplayEngine;
import com.huawei.gallery.displayengine.ScreenNailCommonDisplayEnginePool;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class PhotoDataAdapter implements Model {
    private static final boolean DISPLAY_ENGINE_ENABLE = DisplayEngineUtils.isDisplayEngineEnable();
    private static final boolean DISPLAY_OPTIMIZATION_ENABLE = DisplayEngineUtils.isOptimizationEnable();
    private static ImageFetch[] sImageFetchSeq = new ImageFetch[16];
    private int mActiveEnd = 0;
    private int mActiveStart = 0;
    private int mCameraIndex;
    private final long[] mChanges = new long[7];
    private int mContentEnd = 0;
    private int mContentStart = 0;
    private int mCurrentIndex;
    private final MediaItem[] mData = new MediaItem[32];
    private DataListener mDataListener;
    private int mFocusHintDirection = 0;
    private Path mFocusHintPath = null;
    private boolean mFromCamera = false;
    private HashMap<Path, ImageEntry> mImageCache = new HashMap();
    private boolean mIsActive;
    private boolean mIsAllowFatchBigImage = true;
    private boolean mIsPanorama;
    private boolean mIsStaticCamera;
    private Path mItemPath;
    private Handler mMainHandler;
    private boolean mNeedFullImage;
    private final Path[] mPaths = new Path[7];
    private final AbsPhotoView mPhotoView;
    private ReloadTask mReloadTask;
    private ScreenNailCommonDisplayEnginePool mScreenNailCommonDisplayEnginePool = new ScreenNailCommonDisplayEnginePool();
    private int mSize = 0;
    private final MediaSet mSource;
    private final SourceListener mSourceListener = new SourceListener();
    private long mSourceVersion = -1;
    private final ThreadPool mThreadPool;
    private final TileImageViewAdapter mTileProvider = new TileImageViewAdapter();

    public interface DataListener extends LoadingListener {
        void onPhotoChanged(int i, Path path);
    }

    private class FullImageJob extends BaseJob<BitmapRegionDecoder> {
        private MediaItem mItem;

        public FullImageJob(MediaItem item) {
            this.mItem = item;
        }

        public BitmapRegionDecoder run(JobContext jc) {
            if (PhotoDataAdapter.this.isTemporaryItem(this.mItem)) {
                return null;
            }
            TraceController.traceBegin("FullImageJob.run");
            Job<BitmapRegionDecoder> job = this.mItem.requestLargeImage();
            if (job == null) {
                TraceController.traceEnd();
                return null;
            }
            BitmapRegionDecoder brd = (BitmapRegionDecoder) job.run(jc);
            TraceController.traceEnd();
            return brd;
        }

        public String workContent() {
            return "obtain region decoder. item: " + this.mItem.getFilePath();
        }
    }

    private class FullImageListener implements Runnable, FutureListener<BitmapRegionDecoder> {
        private int mDecoderIndex;
        private Future<BitmapRegionDecoder> mFuture;
        private int mHeight;
        private final Path mPath;
        private int mWidth;

        public FullImageListener(MediaItem item) {
            this.mPath = item.getPath();
            this.mDecoderIndex = 1;
        }

        public FullImageListener(MediaItem item, int index) {
            this.mPath = item.getPath();
            this.mDecoderIndex = index;
        }

        public void onFutureDone(Future<BitmapRegionDecoder> future) {
            TraceController.traceBegin("FullImageListener.onFutureDone, send MSG_RUN_OBJECT (lock gl thread)");
            this.mFuture = future;
            BitmapRegionDecoder fullImage = (BitmapRegionDecoder) future.get();
            if (fullImage != null) {
                this.mWidth = fullImage.getWidth();
                this.mHeight = fullImage.getHeight();
            }
            PhotoDataAdapter.this.mMainHandler.sendMessage(PhotoDataAdapter.this.mMainHandler.obtainMessage(3, this));
            TraceController.traceEnd();
        }

        public void run() {
            TraceController.traceBegin("FullImageListener.run");
            PhotoDataAdapter.this.updateFullImage(this.mPath, this.mFuture, this.mWidth, this.mHeight, this.mDecoderIndex);
            TraceController.traceEnd();
        }
    }

    private class GetUpdateInfo implements Callable<UpdateInfo> {
        private GetUpdateInfo() {
        }

        private boolean needContentReload() {
            boolean z = true;
            int n = PhotoDataAdapter.this.mContentEnd;
            for (int i = PhotoDataAdapter.this.mContentStart; i < n; i++) {
                if (PhotoDataAdapter.this.mData[i % 32] == null) {
                    return true;
                }
            }
            MediaItem current = PhotoDataAdapter.this.mData[PhotoDataAdapter.this.mCurrentIndex % 32];
            if (current != null && current.getPath() == PhotoDataAdapter.this.mItemPath) {
                z = false;
            }
            return z;
        }

        public UpdateInfo call() throws Exception {
            UpdateInfo info = new UpdateInfo();
            info.version = PhotoDataAdapter.this.mSourceVersion;
            info.reloadContent = needContentReload();
            info.target = PhotoDataAdapter.this.mItemPath;
            info.indexHint = PhotoDataAdapter.this.mCurrentIndex;
            info.contentStart = PhotoDataAdapter.this.mContentStart;
            info.contentEnd = PhotoDataAdapter.this.mContentEnd;
            info.size = PhotoDataAdapter.this.mSize;
            return info;
        }
    }

    private static class ImageEntry {
        public boolean failToLoad;
        public BitmapRegionDecoder fullImage;
        public BitmapRegionDecoder fullImage2;
        public Future<BitmapRegionDecoder> fullImageTask;
        public Future<BitmapRegionDecoder> fullImageTask2;
        public int height;
        public long requestedFullImage;
        public long requestedScreenNail;
        public ScreenNail screenNail;
        public Future<ScreenNail> screenNailTask;
        public int width;

        private ImageEntry() {
            this.requestedScreenNail = -1;
            this.requestedFullImage = -1;
            this.failToLoad = false;
        }
    }

    private static class ImageFetch {
        int imageBit;
        int indexOffset;

        public ImageFetch(int offset, int bit) {
            this.indexOffset = offset;
            this.imageBit = bit;
        }
    }

    private class ReloadTask extends Thread {
        private volatile boolean mActive;
        private volatile boolean mDirty;
        private boolean mIsLoading;

        private ReloadTask() {
            this.mActive = true;
            this.mDirty = true;
            this.mIsLoading = false;
        }

        private void updateLoading(boolean loading) {
            if (this.mIsLoading != loading) {
                this.mIsLoading = loading;
                PhotoDataAdapter.this.mMainHandler.sendEmptyMessage(loading ? 1 : 2);
            }
        }

        public void run() {
            TraceController.traceBegin("PhotoDataAdapter.ReloadTask.run");
            while (this.mActive) {
                synchronized (this) {
                    if (this.mDirty || !this.mActive) {
                        this.mDirty = false;
                        TraceController.traceBegin("PhotoDataAdapter.ReloadTask.run.executeAndWait(new GetUpdateInfo())");
                        UpdateInfo info = (UpdateInfo) PhotoDataAdapter.this.executeAndWait(new GetUpdateInfo());
                        TraceController.traceEnd();
                        if (info == null) {
                            GalleryLog.e("PhotoDataAdapter", "GetUpdateInfo is null, this is impossible!");
                        } else {
                            updateLoading(true);
                            TraceController.traceBegin("PhotoDataAdapter.ReloadTask.run.reload");
                            long version = PhotoDataAdapter.this.mSource.reload();
                            TraceController.traceEnd();
                            if (info.version != version) {
                                TraceController.traceBegin("PhotoDataAdapter.ReloadTask.run.getMediaItemCount");
                                info.reloadContent = true;
                                info.size = PhotoDataAdapter.this.mSource.getMediaItemCount();
                                GalleryLog.d("PhotoDataAdapter", "version changed, mediaItemCount is " + info.size);
                                TraceController.traceEnd();
                            }
                            if (info.reloadContent) {
                                TraceController.traceBegin("PhotoDataAdapter.ReloadTask.run.reload.getMediaItem");
                                info.items = PhotoDataAdapter.this.mSource.getMediaItem(info.contentStart, Math.max(info.contentEnd - info.contentStart, 0));
                                TraceController.traceEnd();
                                int index = -1;
                                if (PhotoDataAdapter.this.mFocusHintPath != null) {
                                    TraceController.traceBegin("PhotoDataAdapter.ReloadTask.run.findIndexOfPathInCache");
                                    index = PhotoDataAdapter.this.findIndexOfPathInCache(info, PhotoDataAdapter.this.mFocusHintPath);
                                    GalleryLog.d("PhotoDataAdapter", "findIndexOfPathInCache determained index: " + index);
                                    PhotoDataAdapter.this.mFocusHintPath = null;
                                    TraceController.traceEnd();
                                }
                                if (index == -1) {
                                    TraceController.traceBegin("PhotoDataAdapter.ReloadTask.run.findCurrentMediaItem");
                                    MediaItem item = findCurrentMediaItem(info);
                                    TraceController.traceEnd();
                                    if (item == null || item.getPath() != info.target) {
                                        TraceController.traceBegin("PhotoDataAdapter.ReloadTask.run.findIndexOfTarget");
                                        index = findIndexOfTarget(info);
                                        GalleryLog.d("PhotoDataAdapter", "findIndexOfTarget determained index: " + index);
                                        TraceController.traceEnd();
                                    } else {
                                        index = info.indexHint;
                                        GalleryLog.d("PhotoDataAdapter", "findCurrentMediaItem determained index: " + index);
                                    }
                                }
                                if (index == -1) {
                                    index = info.indexHint;
                                    int focusHintDirection = PhotoDataAdapter.this.mFocusHintDirection;
                                    if (index == PhotoDataAdapter.this.mCameraIndex + 1) {
                                        focusHintDirection = 0;
                                    }
                                    if (focusHintDirection == 1 && index > 0) {
                                        index--;
                                    }
                                }
                                if (info.size > 0) {
                                    if (index >= info.size) {
                                        index = info.size - 1;
                                    }
                                    GalleryLog.d("PhotoDataAdapter", "index large than size, change to " + index);
                                }
                                info.indexHint = index;
                                TraceController.traceBegin("PhotoDataAdapter.ReloadTask.run.executeAndWait(new UpdateContent())");
                                PhotoDataAdapter.this.executeAndWait(new UpdateContent(info));
                                TraceController.traceEnd();
                            } else {
                                GalleryLog.d("PhotoDataAdapter", "no need reload content.");
                            }
                        }
                    } else {
                        updateLoading(false);
                        Utils.waitWithoutInterrupt(this);
                    }
                }
            }
            TraceController.traceEnd();
        }

        public synchronized void notifyDirty() {
            this.mDirty = true;
            notifyAll();
        }

        public synchronized void terminate() {
            this.mActive = false;
            notifyAll();
        }

        private MediaItem findCurrentMediaItem(UpdateInfo info) {
            ArrayList<MediaItem> items = info.items;
            int index = info.indexHint - info.contentStart;
            return (index < 0 || index >= items.size()) ? null : (MediaItem) items.get(index);
        }

        private int findIndexOfTarget(UpdateInfo info) {
            if (info.target == null) {
                return info.indexHint;
            }
            if (info.items != null) {
                int i = PhotoDataAdapter.this.findIndexOfPathInCache(info, info.target);
                if (i != -1) {
                    return i;
                }
            }
            return PhotoDataAdapter.this.mSource.getIndexOfItem(info.target, info.indexHint);
        }
    }

    private class ScreenNailJob extends BaseJob<ScreenNail> {
        private boolean mFullImage;
        private MediaItem mItem;

        public ScreenNailJob(MediaItem item, boolean fullImage) {
            this.mItem = item;
            this.mFullImage = fullImage;
        }

        public ScreenNail run(JobContext jc) {
            TraceController.traceBegin("PhotoDataAdapter.ScreenNailJob.run");
            ScreenNail s = this.mItem.getScreenNail();
            if (s != null) {
                TraceController.traceEnd();
                return s;
            } else if (PhotoDataAdapter.this.isTemporaryItem(this.mItem)) {
                ScreenNail screenNail = PhotoDataAdapter.this.newPlaceholderScreenNail(this.mItem);
                TraceController.traceEnd();
                return screenNail;
            } else {
                Bitmap preparedFullScreenNailBitmap;
                boolean needFree = false;
                if (PhotoDataAdapter.this.mIsAllowFatchBigImage && this.mFullImage && (this.mItem instanceof IImage) && !isDNG(this.mItem.getMimeType())) {
                    preparedFullScreenNailBitmap = BoostFullScreenNailDisplay.getPreparedFullScreenNailBitmap(jc, this.mItem, PhotoDataAdapter.this.mScreenNailCommonDisplayEnginePool);
                    needFree = true;
                    PhotoDataAdapter.this.mIsAllowFatchBigImage = false;
                } else {
                    preparedFullScreenNailBitmap = (Bitmap) this.mItem.requestImage(1).run(jc);
                    if (PhotoDataAdapter.DISPLAY_ENGINE_ENABLE) {
                        preparedFullScreenNailBitmap = DisplayEngineUtils.processScreenNailACE(preparedFullScreenNailBitmap, this.mItem, PhotoDataAdapter.this.mScreenNailCommonDisplayEnginePool, 1);
                    }
                }
                boolean fromCache = false;
                boolean fileSaveComplete = true;
                if (preparedFullScreenNailBitmap == null) {
                    Job<Bitmap> job = this.mItem.requestCacheImage();
                    if (job != null) {
                        preparedFullScreenNailBitmap = (Bitmap) job.run(jc);
                        fromCache = preparedFullScreenNailBitmap != null;
                        fileSaveComplete = this.mItem.getSize() > 0;
                        GalleryLog.d("PhotoDataAdapter", "ScreenNailJob from camera cache:" + fromCache + ", fileSaveComplete:" + fileSaveComplete);
                    }
                }
                if (preparedFullScreenNailBitmap != null) {
                    preparedFullScreenNailBitmap = BitmapUtils.rotateBitmap(preparedFullScreenNailBitmap, this.mItem.getRotation() - this.mItem.getFullImageRotation(), true);
                } else {
                    ThumbnailReporter.reportThumbnailFail(BugType.DECODE_THUMB_FAILED_IMAGE, this.mItem.getFilePath(), new Exception());
                }
                if (preparedFullScreenNailBitmap == null) {
                    TraceController.traceEnd();
                    return null;
                }
                ScreenNail sn = new TiledScreenNail(preparedFullScreenNailBitmap, fromCache, fileSaveComplete, needFree);
                TraceController.traceEnd();
                return sn;
            }
        }

        public String workContent() {
            return "decode TYPE_THUMBNAIL for " + this.mItem.getFilePath();
        }

        public boolean needDecodeVideoFromOrigin() {
            return this.mItem instanceof IVideo ? this.mItem.requestImage(1).needDecodeVideoFromOrigin() : false;
        }

        private boolean isDNG(String mimeType) {
            if (mimeType == null) {
                return false;
            }
            return mimeType.toLowerCase(Locale.US).contains("dng");
        }
    }

    private class ScreenNailListener implements Runnable, FutureListener<ScreenNail> {
        private Future<ScreenNail> mFuture;
        private final Path mPath;

        public ScreenNailListener(MediaItem item) {
            this.mPath = item.getPath();
        }

        public void onFutureDone(Future<ScreenNail> future) {
            TraceController.traceBegin("ScreenNailListener.onFutureDone, send MSG_RUN_OBJECT (lock gl thread)");
            this.mFuture = future;
            PhotoDataAdapter.this.mMainHandler.sendMessage(PhotoDataAdapter.this.mMainHandler.obtainMessage(3, this));
            TraceController.traceEnd();
        }

        public void run() {
            TraceController.traceBegin("ScreenNailListener.run");
            PhotoDataAdapter.this.updateScreenNail(this.mPath, this.mFuture);
            TraceController.traceEnd();
        }
    }

    private class SourceListener implements ContentListener {
        private SourceListener() {
        }

        public void onContentDirty() {
            if (PhotoDataAdapter.this.mReloadTask != null) {
                PhotoDataAdapter.this.mReloadTask.notifyDirty();
            }
        }
    }

    private class UpdateContent implements Callable<Void> {
        UpdateInfo mUpdateInfo;

        public UpdateContent(UpdateInfo updateInfo) {
            this.mUpdateInfo = updateInfo;
        }

        public Void call() throws Exception {
            TraceController.traceBegin("PhotoDataAdapter.UpdateContent.call");
            GalleryLog.d("PhotoDataAdapter", "photo data adapter start update content");
            UpdateInfo info = this.mUpdateInfo;
            PhotoDataAdapter.this.mSourceVersion = info.version;
            if (info.size != PhotoDataAdapter.this.mSize) {
                PhotoDataAdapter.this.mSize = info.size;
                if (PhotoDataAdapter.this.mContentEnd > PhotoDataAdapter.this.mSize) {
                    PhotoDataAdapter.this.mContentEnd = PhotoDataAdapter.this.mSize;
                }
                if (PhotoDataAdapter.this.mActiveEnd > PhotoDataAdapter.this.mSize) {
                    PhotoDataAdapter.this.mActiveEnd = PhotoDataAdapter.this.mSize;
                }
            }
            if (PhotoDataAdapter.this.mFocusHintPath != null) {
                int index = PhotoDataAdapter.this.findIndexOfPathInCache(info, PhotoDataAdapter.this.mFocusHintPath);
                if (index != -1) {
                    info.indexHint = index;
                }
                PhotoDataAdapter.this.mFocusHintPath = null;
            }
            if (info.indexHint >= PhotoDataAdapter.this.mSize && PhotoDataAdapter.this.mSize > 0) {
                info.indexHint = PhotoDataAdapter.this.mSize - 1;
            }
            PhotoDataAdapter.this.mCurrentIndex = info.indexHint;
            PhotoDataAdapter.this.updateSlidingWindow();
            if (info.items != null) {
                int start = Math.max(info.contentStart, PhotoDataAdapter.this.mContentStart);
                int end = Math.min(info.contentEnd, PhotoDataAdapter.this.mContentEnd);
                int arrayEnd = info.contentStart + info.items.size();
                int dataIndex = start % 32;
                for (int i = start; i < end; i++) {
                    MediaItem mediaItem;
                    MediaItem[] -get6 = PhotoDataAdapter.this.mData;
                    if (i < arrayEnd) {
                        mediaItem = (MediaItem) info.items.get(i - info.contentStart);
                    } else {
                        mediaItem = null;
                    }
                    -get6[dataIndex] = mediaItem;
                    dataIndex++;
                    if (dataIndex == 32) {
                        dataIndex = 0;
                    }
                }
            }
            MediaItem current = PhotoDataAdapter.this.mData[PhotoDataAdapter.this.mCurrentIndex % 32];
            PhotoDataAdapter.this.mItemPath = current == null ? null : current.getPath();
            PhotoDataAdapter.this.updateImageCache();
            PhotoDataAdapter.this.updateTileProvider();
            PhotoDataAdapter.this.updateImageRequests();
            if (PhotoDataAdapter.this.mDataListener != null) {
                PhotoDataAdapter.this.mDataListener.onPhotoChanged(PhotoDataAdapter.this.mCurrentIndex, PhotoDataAdapter.this.mItemPath);
            }
            PhotoDataAdapter.this.fireDataChange();
            PhotoDataAdapter.this.mPhotoView.onDataUpdate();
            GalleryLog.d("PhotoDataAdapter", "photo data adapter end update content");
            TraceController.traceEnd();
            return null;
        }
    }

    private static class UpdateInfo {
        public int contentEnd;
        public int contentStart;
        public int indexHint;
        public ArrayList<MediaItem> items;
        public boolean reloadContent;
        public int size;
        public Path target;
        public long version;

        private UpdateInfo() {
        }
    }

    static {
        int i;
        sImageFetchSeq[0] = new ImageFetch(0, 3);
        int k = 1 + 1;
        sImageFetchSeq[1] = new ImageFetch(0, 2);
        for (int i2 = 1; i2 < 7; i2++) {
            i = k + 1;
            sImageFetchSeq[k] = new ImageFetch(i2, 1);
            k = i + 1;
            sImageFetchSeq[i] = new ImageFetch(-i2, 1);
        }
        i = k + 1;
        sImageFetchSeq[k] = new ImageFetch(1, 2);
        k = i + 1;
        sImageFetchSeq[i] = new ImageFetch(-1, 2);
    }

    public PhotoDataAdapter(GalleryContext activity, GLRoot glRoot, AbsPhotoView view, MediaSet mediaSet, Path itemPath, int indexHint, int cameraIndex, boolean isPanorama, boolean isStaticCamera, int setSize) {
        TiledScreenNail.setLoadingTip(activity.getString(R.string.loading));
        this.mSource = (MediaSet) Utils.checkNotNull(mediaSet);
        this.mPhotoView = (AbsPhotoView) Utils.checkNotNull(view);
        this.mItemPath = (Path) Utils.checkNotNull(itemPath);
        this.mCurrentIndex = indexHint;
        this.mCameraIndex = cameraIndex;
        this.mIsPanorama = isPanorama;
        this.mIsStaticCamera = isStaticCamera;
        this.mThreadPool = activity.getThreadPool();
        this.mNeedFullImage = true;
        this.mSize = Math.max(setSize, 0);
        this.mIsAllowFatchBigImage = true;
        Arrays.fill(this.mChanges, -1);
        this.mMainHandler = new SynchronizedHandler(glRoot) {
            public void handleMessage(Message message) {
                switch (message.what) {
                    case 1:
                        if (PhotoDataAdapter.this.mDataListener != null) {
                            TraceController.traceBegin("PhotoDataAdapter.mMainHandler.MSG_LOAD_START, gl locked");
                            PhotoDataAdapter.this.mDataListener.onLoadingStarted();
                            TraceController.traceEnd();
                        }
                        return;
                    case 2:
                        if (PhotoDataAdapter.this.mDataListener != null) {
                            TraceController.traceBegin("PhotoDataAdapter.mMainHandler.MSG_LOAD_FINISH, gl locked");
                            PhotoDataAdapter.this.mDataListener.onLoadingFinished(false);
                            TraceController.traceEnd();
                        }
                        return;
                    case 3:
                        TraceController.traceBegin("PhotoDataAdapter.mMainHandler.MSG_RUN_OBJECT, gl locked");
                        ((Runnable) message.obj).run();
                        TraceController.traceEnd();
                        return;
                    case 4:
                        TraceController.traceBegin("PhotoDataAdapter.mMainHandler.MSG_UPDATE_IMAGE_REQUESTS, gl locked");
                        PhotoDataAdapter.this.updateImageRequests();
                        TraceController.traceEnd();
                        return;
                    default:
                        throw new AssertionError();
                }
            }
        };
        updateSlidingWindow();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private MediaItem getItemInternal(int index) {
        if (index < 0 || index >= this.mSize || index < this.mContentStart || index >= this.mContentEnd) {
            return null;
        }
        return this.mData[index % 32];
    }

    private long getVersion(int index) {
        MediaItem item = getItemInternal(index);
        if (item == null) {
            return -1;
        }
        return item.getDataVersion();
    }

    private Path getPath(int index) {
        MediaItem item = getItemInternal(index);
        if (item == null) {
            return null;
        }
        return item.getPath();
    }

    private void fireDataChange() {
        fireDataChange(0);
    }

    private void fireDataChange(int maskOffset) {
        int i;
        TraceController.traceBegin("PhotoDataAdapter.fireDataChange");
        boolean changed = false;
        for (i = -3; i <= 3; i++) {
            long newVersion = getVersion(this.mCurrentIndex + i);
            if (this.mChanges[i + 3] != newVersion) {
                this.mChanges[i + 3] = newVersion;
                changed = true;
            }
        }
        if (changed) {
            int[] fromIndex = new int[7];
            Path[] oldPaths = new Path[7];
            System.arraycopy(this.mPaths, 0, oldPaths, 0, 7);
            for (i = 0; i < 7; i++) {
                this.mPaths[i] = getPath((this.mCurrentIndex + i) - 3);
            }
            for (i = 0; i < 7; i++) {
                Path p = this.mPaths[i];
                if (p == null) {
                    fromIndex[i] = Integer.MAX_VALUE;
                } else {
                    int i2;
                    int j = 0;
                    while (j < 7 && oldPaths[j] != p) {
                        j++;
                    }
                    if (j < 7) {
                        i2 = j - 3;
                    } else {
                        i2 = Integer.MAX_VALUE;
                    }
                    fromIndex[i] = i2;
                }
            }
            this.mPhotoView.notifyDataChange(fromIndex, -this.mCurrentIndex, (this.mSize - 1) - this.mCurrentIndex, maskOffset);
            TraceController.traceEnd();
            return;
        }
        TraceController.traceEnd();
    }

    public void setDataListener(DataListener listener) {
        this.mDataListener = listener;
    }

    public void setFromCamera(boolean isFromCamera) {
        this.mFromCamera = isFromCamera;
    }

    private void updateScreenNail(Path path, Future<ScreenNail> future) {
        boolean z = false;
        ImageEntry entry = (ImageEntry) this.mImageCache.get(path);
        ScreenNail screenNail = (ScreenNail) future.get();
        if (entry == null || entry.screenNailTask != future) {
            if (screenNail != null) {
                screenNail.recycle();
                this.mScreenNailCommonDisplayEnginePool.remove((MediaItem) path.getObject());
            }
            return;
        }
        if (!this.mFromCamera) {
            if (screenNail == null) {
                z = true;
            }
            entry.failToLoad = z;
        }
        entry.screenNailTask = null;
        if (entry.screenNail instanceof TiledScreenNail) {
            screenNail = entry.screenNail.combine(screenNail);
        }
        if (screenNail != null) {
            entry.screenNail = screenNail;
        }
        for (int i = -3; i <= 3; i++) {
            if (path == getPath(this.mCurrentIndex + i)) {
                if (i == 0) {
                    if (this.mCurrentIndex != 0 || shouldReportCameraPreview(future)) {
                        Reporter.CAMERA_SEE_TO_REVIEW.end(null);
                    }
                    MediaItem item = (MediaItem) path.getObject();
                    if (!(item == null || (item.getSupportedOperations() & 64) == 0)) {
                        entry.width = item.getWidth();
                        entry.height = item.getHeight();
                    }
                    updateTileProvider(entry);
                }
                this.mPhotoView.notifyImageChange(i);
                updateImageRequests();
            }
        }
        updateImageRequests();
    }

    private boolean shouldReportCameraPreview(Future<ScreenNail> future) {
        if (future == null || future.isCancelled()) {
            return true;
        }
        ScreenNail screenNail = (ScreenNail) future.get();
        if ((screenNail instanceof TiledScreenNail) && ((TiledScreenNail) screenNail).isBitmapFromCache()) {
            return false;
        }
        return true;
    }

    private void updateFullImage(Path path, Future<BitmapRegionDecoder> future, int width, int height, int decoderIndex) {
        TraceController.traceBegin("PhotoDataAdapter.updateFullImage decoderIndex=" + decoderIndex);
        ImageEntry entry = (ImageEntry) this.mImageCache.get(path);
        if (decoderIndex == 1) {
            if (entry != null && entry.fullImageTask == future) {
                entry.fullImageTask = null;
                entry.fullImage = (BitmapRegionDecoder) future.get();
                entry.width = width;
                entry.height = height;
            } else if (future == null) {
                TraceController.traceEnd();
                return;
            } else {
                BitmapRegionDecoder fullImage = (BitmapRegionDecoder) future.get();
                if (fullImage != null) {
                    fullImage.recycle();
                }
                TraceController.traceEnd();
                return;
            }
        } else if (decoderIndex == 2) {
            if (entry != null && entry.fullImageTask2 == future) {
                entry.fullImageTask2 = null;
                entry.fullImage2 = (BitmapRegionDecoder) future.get();
                entry.width = width;
                entry.height = height;
            } else if (future == null) {
                TraceController.traceEnd();
                return;
            } else {
                BitmapRegionDecoder fullImage2 = (BitmapRegionDecoder) future.get();
                if (fullImage2 != null) {
                    fullImage2.recycle();
                }
                TraceController.traceEnd();
                return;
            }
        }
        if (entry.fullImage != null && (!(DISPLAY_OPTIMIZATION_ENABLE && entry.fullImage2 == null) && path == getPath(this.mCurrentIndex))) {
            updateTileProvider(entry);
            this.mPhotoView.notifyImageChange(0);
        }
        updateImageRequests();
        TraceController.traceEnd();
    }

    public void resume() {
        TraceController.traceBegin("PhotoDataAdapter.resume");
        this.mIsActive = true;
        this.mSource.addContentListener(this.mSourceListener);
        updateImageCache();
        updateImageRequests();
        this.mReloadTask = new ReloadTask();
        this.mReloadTask.start();
        fireDataChange();
        TraceController.traceEnd();
    }

    public void pause() {
        TraceController.traceBegin("PhotoDataAdapter.pause");
        this.mIsActive = false;
        if (DisplayEngineUtils.isDisplayEngineEnable()) {
            DisplayEngineUtils.updateEffectImageReviewExit();
        }
        this.mReloadTask.terminate();
        this.mReloadTask = null;
        this.mSource.removeContentListener(this.mSourceListener);
        for (ImageEntry entry : this.mImageCache.values()) {
            if (entry.fullImageTask != null) {
                entry.fullImageTask.cancel();
            }
            if (entry.fullImageTask2 != null) {
                entry.fullImageTask2.cancel();
            }
            if (entry.screenNailTask != null) {
                entry.screenNailTask.cancel();
            }
            if (entry.screenNail != null) {
                entry.screenNail.recycle();
            }
        }
        this.mImageCache.clear();
        this.mTileProvider.clear();
        TraceController.traceEnd();
    }

    private MediaItem getItem(int index) {
        boolean z = false;
        if (index < 0 || index >= this.mSize || !this.mIsActive) {
            return null;
        }
        if (index >= this.mActiveStart && index < this.mActiveEnd) {
            z = true;
        }
        Utils.assertTrue(z);
        if (index < this.mContentStart || index >= this.mContentEnd) {
            return null;
        }
        return this.mData[index % 32];
    }

    private void updateCurrentIndex(int index, int maskOffset) {
        Path path = null;
        if (this.mCurrentIndex != index) {
            TraceController.traceBegin("PhotoDataAdapter.updateCurrentIndex");
            this.mCurrentIndex = index;
            updateSlidingWindow();
            MediaItem item = this.mData[index % 32];
            if (item != null) {
                path = item.getPath();
            }
            this.mItemPath = path;
            this.mFocusHintPath = this.mItemPath;
            updateImageCache();
            updateImageRequests();
            updateTileProvider();
            if (this.mDataListener != null) {
                this.mDataListener.onPhotoChanged(index, this.mItemPath);
            }
            fireDataChange(maskOffset);
            TraceController.traceEnd();
        }
    }

    public void moveTo(int index, int maskOffset) {
        TraceController.traceBegin("PhotoDataAdapter.moveTo");
        this.mIsAllowFatchBigImage = false;
        updateCurrentIndex(index, maskOffset);
        TraceController.traceEnd();
    }

    public ScreenNail getScreenNail(int offset) {
        boolean z = false;
        int index = this.mCurrentIndex + offset;
        if (index < 0 || index >= this.mSize || !this.mIsActive) {
            return null;
        }
        if (index >= this.mActiveStart && index < this.mActiveEnd) {
            z = true;
        }
        Utils.assertTrue(z);
        MediaItem item = getItem(index);
        if (item == null) {
            return null;
        }
        ImageEntry entry = (ImageEntry) this.mImageCache.get(item.getPath());
        if (entry == null) {
            return null;
        }
        if (!isCamera(offset) && (entry.failToLoad || (entry.screenNail == null && offset != 0))) {
            entry.screenNail = newPlaceholderScreenNail(item);
            if (entry.failToLoad && offset == 0) {
                updateTileProvider(entry);
            }
        }
        return entry.screenNail;
    }

    public void getImageSize(int offset, Size size) {
        MediaItem item = getItem(this.mCurrentIndex + offset);
        if (item == null) {
            size.width = 0;
            size.height = 0;
            return;
        }
        size.width = item.getWidth();
        size.height = item.getHeight();
    }

    public int getImageRotation(int offset) {
        MediaItem item = getItem(this.mCurrentIndex + offset);
        return item == null ? 0 : item.getFullImageRotation();
    }

    public void setNeedFullImage(boolean enabled) {
        this.mNeedFullImage = enabled;
        this.mMainHandler.sendEmptyMessage(4);
    }

    public boolean isCamera(int offset) {
        return this.mCurrentIndex + offset == this.mCameraIndex;
    }

    public boolean isPanorama(int offset) {
        return isCamera(offset) ? this.mIsPanorama : false;
    }

    public boolean isStaticCamera(int offset) {
        return isCamera(offset) ? this.mIsStaticCamera : false;
    }

    public boolean isVideo(int offset) {
        MediaItem item = getItem(this.mCurrentIndex + offset);
        if (item != null && item.getMediaType() == 4) {
            return true;
        }
        return false;
    }

    public int getLoadingState(int offset) {
        ImageEntry entry = (ImageEntry) this.mImageCache.get(getPath(this.mCurrentIndex + offset));
        if (entry == null) {
            return 0;
        }
        if (entry.failToLoad) {
            return 2;
        }
        if (entry.screenNail != null) {
            return 1;
        }
        return 0;
    }

    public ScreenNail getScreenNail() {
        return getScreenNail(0);
    }

    public int getImageHeight() {
        return this.mTileProvider.getImageHeight();
    }

    public int getImageWidth() {
        return this.mTileProvider.getImageWidth();
    }

    public int getLevelCount() {
        return this.mTileProvider.getLevelCount();
    }

    public Bitmap getTile(int level, int x, int y, int tileSize, int borderSize, BitmapPool pool) {
        return this.mTileProvider.getTile(level, x, y, tileSize, borderSize, pool);
    }

    public boolean isEmpty() {
        return this.mSize == 0;
    }

    public int getCurrentIndex() {
        return this.mCurrentIndex;
    }

    public MediaItem getMediaItem(int offset) {
        int index = this.mCurrentIndex + offset;
        if (index < this.mContentStart || index >= this.mContentEnd) {
            return null;
        }
        return this.mData[index % 32];
    }

    public MediaItem getCurrentMediaItem() {
        return getMediaItem(0);
    }

    public ScreenNailCommonDisplayEnginePool getScreenNailCommonDisplayEnginePool() {
        return this.mScreenNailCommonDisplayEnginePool;
    }

    public void setCurrentPhoto(Path path, int indexHint) {
        if (this.mItemPath != path) {
            TraceController.traceBegin("PhotoDataAdapter.setCurrentPhoto");
            this.mItemPath = path;
            this.mCurrentIndex = indexHint;
            updateSlidingWindow();
            updateImageCache();
            fireDataChange();
            if (this.mFocusHintPath != null && (path == null || !this.mFocusHintPath.equalsIgnoreCase(path.toString()))) {
                this.mFocusHintPath = null;
            }
            MediaItem item = getMediaItem(0);
            if (!(item == null || item.getPath() == path || this.mReloadTask == null)) {
                this.mReloadTask.notifyDirty();
            }
            TraceController.traceEnd();
        }
    }

    public void setFocusHintDirection(int direction) {
        this.mFocusHintDirection = direction;
    }

    public boolean isLCDDownloaded() {
        MediaItem item = getMediaItem(0);
        if (item == null || !(item instanceof GalleryImage)) {
            return true;
        }
        return ((GalleryImage) item).isLCDDownloaded();
    }

    private void updateTileProvider() {
        ImageEntry entry = (ImageEntry) this.mImageCache.get(getPath(this.mCurrentIndex));
        if (entry == null) {
            this.mTileProvider.clear();
        } else {
            updateTileProvider(entry);
        }
    }

    private void updateTileProvider(ImageEntry entry) {
        MediaItem currentItem = null;
        TraceController.traceBegin("PhotoDataAdapter.updateTileProvider");
        ScreenNail screenNail = entry.screenNail;
        BitmapRegionDecoder fullImage = entry.fullImage;
        BitmapRegionDecoder fullImage2 = entry.fullImage2;
        if (screenNail != null) {
            boolean checkFullImage2 = (DISPLAY_OPTIMIZATION_ENABLE && fullImage2 == null) ? false : true;
            if (fullImage == null || !checkFullImage2) {
                int width;
                int height;
                if (entry.width <= 0 || entry.height <= 0) {
                    width = screenNail.getWidth();
                    height = screenNail.getHeight();
                } else {
                    width = entry.width;
                    height = entry.height;
                }
                this.mTileProvider.setScreenNail(screenNail, width, height);
            } else {
                this.mTileProvider.setScreenNail(screenNail, entry.width, entry.height);
                if (fullImage.getWidth() == entry.width && fullImage.getHeight() == entry.height) {
                    this.mTileProvider.setRegionDecoder(fullImage, entry.width, entry.height);
                }
                if (fullImage2 != null && fullImage2.getWidth() == entry.width && fullImage2.getHeight() == entry.height) {
                    this.mTileProvider.setRegionDecoder2(fullImage2, entry.width, entry.height);
                }
            }
            Path path = getPath(this.mCurrentIndex);
            if (path != null) {
                currentItem = (MediaItem) path.getObject();
            }
            if (currentItem != null) {
                this.mTileProvider.setFilePath(currentItem.getFilePath());
            }
        } else {
            this.mTileProvider.clear();
        }
        TraceController.traceEnd();
    }

    private void updateSlidingWindow() {
        int start = Utils.clamp(this.mCurrentIndex - 3, 0, Math.max(0, this.mSize - 7));
        int end = Math.min(this.mSize, start + 7);
        if (this.mActiveStart != start || this.mActiveEnd != end) {
            this.mActiveStart = start;
            this.mActiveEnd = end;
            start = Utils.clamp(this.mCurrentIndex - 16, 0, Math.max(0, this.mSize - 32));
            end = Math.min(this.mSize, start + 32);
            if (this.mContentStart > this.mActiveStart || this.mContentEnd < this.mActiveEnd || Math.abs(start - this.mContentStart) > 8) {
                int i = this.mContentStart;
                while (i < this.mContentEnd) {
                    if (i < start || i >= end) {
                        this.mData[i % 32] = null;
                    }
                    i++;
                }
                this.mContentStart = start;
                this.mContentEnd = end;
                if (this.mReloadTask != null) {
                    this.mReloadTask.notifyDirty();
                }
            }
        }
    }

    private void updateImageRequests() {
        if (this.mIsActive) {
            TraceController.traceBegin("PhotoDataAdapter.updateImageRequests");
            int currentIndex = this.mCurrentIndex;
            MediaItem item = this.mData[currentIndex % 32];
            if (item == null || item.getPath() != this.mItemPath) {
                TraceController.traceEnd();
                return;
            }
            if (DisplayEngineUtils.isDisplayEngineEnable()) {
                ScreenNailCommonDisplayEngine commonDisplayEngine = this.mScreenNailCommonDisplayEnginePool.get(item);
                if (commonDisplayEngine != null) {
                    DisplayEngineUtils.updateEffectImageReview(item, commonDisplayEngine);
                }
            }
            Future<?> task = null;
            for (int i = 0; i < sImageFetchSeq.length; i++) {
                int offset = sImageFetchSeq[i].indexOffset;
                int bit = sImageFetchSeq[i].imageBit;
                if (bit != 2 || this.mNeedFullImage) {
                    task = startTaskIfNeeded(currentIndex + offset, bit);
                    if (task != null) {
                        break;
                    }
                }
            }
            for (ImageEntry entry : this.mImageCache.values()) {
                if (!(entry.screenNailTask == null || entry.screenNailTask == r8)) {
                    entry.screenNailTask.cancel();
                    entry.screenNailTask = null;
                    entry.requestedScreenNail = -1;
                }
                if (!(entry.fullImageTask == null || entry.fullImageTask == r8)) {
                    entry.fullImageTask.cancel();
                    entry.fullImageTask = null;
                    entry.requestedFullImage = -1;
                    if (entry.fullImageTask2 != null) {
                        entry.fullImageTask2.cancel();
                        entry.fullImageTask2 = null;
                    }
                }
            }
            TraceController.traceEnd();
        }
    }

    private boolean isTemporaryItem(MediaItem mediaItem) {
        if (this.mCameraIndex < 0 || !(mediaItem instanceof LocalMediaItem)) {
            return false;
        }
        LocalMediaItem item = (LocalMediaItem) mediaItem;
        if (item.getBucketId() == MediaSetUtils.getCameraBucketId() && item.getSize() == 0 && item.getWidth() != 0 && item.getHeight() != 0 && item.getDateInMs() - System.currentTimeMillis() <= 10000) {
            return true;
        }
        return false;
    }

    private ScreenNail newPlaceholderScreenNail(MediaItem item) {
        boolean z = false;
        int width = item.getWidth();
        int height = item.getHeight();
        TiledScreenNail nail = new TiledScreenNail(width, height);
        if (this.mCameraIndex >= 0 && width > 0 && height > 0) {
            z = true;
        }
        nail.enableLoadingTip(z);
        return nail;
    }

    private Future<?> startTaskIfNeeded(int index, int which) {
        boolean z = false;
        TraceController.traceBegin("PhotoDataAdapter.startTaskIfNeeded");
        if (index < this.mActiveStart || index >= this.mActiveEnd) {
            TraceController.traceEnd();
            return null;
        }
        ImageEntry entry = (ImageEntry) this.mImageCache.get(getPath(index));
        if (entry == null) {
            TraceController.traceEnd();
            return null;
        }
        boolean z2;
        MediaItem item = this.mData[index % 32];
        if (item != null) {
            z2 = true;
        } else {
            z2 = false;
        }
        Utils.assertTrue(z2);
        long version = item.getDataVersion();
        if (index != this.mCurrentIndex && item.isDrm() && item.hasCountConstraint()) {
            TraceController.traceEnd();
            return null;
        } else if ((which == 1 || which == 3) && entry.screenNailTask != null && entry.requestedScreenNail == version) {
            TraceController.traceEnd();
            return entry.screenNailTask;
        } else if (which == 2 && entry.fullImageTask != null && entry.requestedFullImage == version) {
            TraceController.traceEnd();
            return entry.fullImageTask;
        } else if (which == 3 && entry.requestedScreenNail != version) {
            entry.requestedScreenNail = version;
            ThreadPool threadPool = this.mThreadPool;
            if (!this.mFromCamera) {
                z = true;
            }
            entry.screenNailTask = threadPool.submit(new ScreenNailJob(item, z), new ScreenNailListener(item));
            TraceController.traceEnd();
            return entry.screenNailTask;
        } else if (which == 1 && entry.requestedScreenNail != version) {
            entry.requestedScreenNail = version;
            entry.screenNailTask = this.mThreadPool.submit(new ScreenNailJob(item, false), new ScreenNailListener(item));
            TraceController.traceEnd();
            return entry.screenNailTask;
        } else if (which != 2 || entry.requestedFullImage == version || (item.getSupportedOperations() & 64) == 0) {
            TraceController.traceEnd();
            return null;
        } else {
            entry.requestedFullImage = version;
            entry.fullImageTask = this.mThreadPool.submit(new FullImageJob(item), new FullImageListener(item));
            if (DISPLAY_OPTIMIZATION_ENABLE) {
                entry.fullImageTask2 = this.mThreadPool.submit(new FullImageJob(item), new FullImageListener(item, 2));
            }
            TraceController.traceEnd();
            return entry.fullImageTask;
        }
    }

    private void updateImageCache() {
        ImageEntry entry;
        TraceController.traceBegin("PhotoDataAdapter.updateImageCache");
        HashSet<Path> toBeRemoved = new HashSet(this.mImageCache.keySet());
        for (int i = this.mActiveStart; i < this.mActiveEnd; i++) {
            Path path;
            MediaItem item = this.mData[i % 32];
            if (item != null) {
                path = item.getPath();
                entry = (ImageEntry) this.mImageCache.get(path);
                toBeRemoved.remove(path);
                if (entry != null) {
                    if (Math.abs(i - this.mCurrentIndex) > 1) {
                        if (entry.fullImageTask != null) {
                            entry.fullImageTask.cancel();
                            entry.fullImageTask = null;
                        }
                        entry.fullImage = null;
                        if (entry.fullImageTask2 != null) {
                            entry.fullImageTask2.cancel();
                            entry.fullImageTask2 = null;
                        }
                        entry.fullImage2 = null;
                        entry.requestedFullImage = -1;
                    }
                    if (entry.requestedScreenNail != item.getDataVersion() && (entry.screenNail instanceof TiledScreenNail)) {
                        entry.screenNail.updatePlaceholderSize(item.getWidth(), item.getHeight());
                    }
                } else {
                    this.mImageCache.put(path, new ImageEntry());
                }
            }
        }
        for (Path path2 : toBeRemoved) {
            TraceController.traceBegin("for (Path path : toBeRemoved)");
            entry = (ImageEntry) this.mImageCache.remove(path2);
            if (entry.fullImageTask != null) {
                entry.fullImageTask.cancel();
            }
            if (entry.fullImageTask2 != null) {
                entry.fullImageTask2.cancel();
            }
            if (entry.screenNailTask != null) {
                entry.screenNailTask.cancel();
            }
            if (entry.screenNail != null) {
                entry.screenNail.recycle();
                this.mScreenNailCommonDisplayEnginePool.remove((MediaItem) path2.getObject());
            }
            TraceController.traceEnd();
        }
        TraceController.traceEnd();
    }

    private <T> T executeAndWait(Callable<T> callable) {
        FutureTask<T> task = new FutureTask(callable);
        this.mMainHandler.sendMessage(this.mMainHandler.obtainMessage(3, task));
        try {
            return task.get();
        } catch (InterruptedException e) {
            GalleryLog.printDFXLog("PhotoDataAdapter InterruptedException for DFX");
            return null;
        } catch (ExecutionException e2) {
            throw new RuntimeException(e2);
        }
    }

    private int findIndexOfPathInCache(UpdateInfo info, Path path) {
        ArrayList<MediaItem> items = info.items;
        int n = items.size();
        for (int i = 0; i < n; i++) {
            MediaItem item = (MediaItem) items.get(i);
            if (item != null && item.getPath() == path) {
                return info.contentStart + i;
            }
        }
        return -1;
    }

    public void invalidateData(byte[] bytes, int offset, int length) {
    }

    public void invalidateData(BitmapScreenNail bitmapScreenNail) {
    }

    public void invalidateData(Bitmap bitmap) {
    }

    public void resume(byte[] bytes, int offset, int length) {
    }
}
