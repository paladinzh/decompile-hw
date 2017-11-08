package com.huawei.gallery.ui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Message;
import android.text.TextPaint;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.IRecycle;
import com.android.gallery3d.data.IVideo;
import com.android.gallery3d.data.LocalMediaItem;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.BitmapLoader;
import com.android.gallery3d.ui.BitmapTexture;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.StringTexture;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.ui.Texture;
import com.android.gallery3d.ui.TextureUploader;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.JobLimiter;
import com.android.gallery3d.util.MediaSetUtils;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.app.CommonAlbumDataLoader;
import com.huawei.gallery.app.CommonAlbumDataLoader.DataListener;
import com.huawei.gallery.photoshare.utils.JobBulk;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.util.GalleryPool;
import java.util.HashMap;
import java.util.Map.Entry;

public class CommonAlbumSlidingWindow implements DataListener {
    private int mActiveEnd = 0;
    private int mActiveRequestCount = 0;
    private int mActiveStart = 0;
    private int mContentEnd = 0;
    private int mContentStart = 0;
    private GalleryContext mContext;
    private final AlbumEntry[] mData;
    private SynchronizedHandler mHandler;
    private boolean mHasFreeSlotContent = true;
    private boolean mIsActive = false;
    private JobBulk mJobBulk;
    private int mLimitVideoTextLength;
    private Listener mListener;
    private boolean mNeedVideoTexture;
    private int mSize;
    private final CommonAlbumDataLoader mSource;
    private TextureUploader mTextureUploader;
    private final JobLimiter mThreadPool;
    private boolean mUpdateFinish;
    private TextPaint mVideoDurationPaint;
    private TextPaint mVideoTitlePaint;

    public interface Listener {
        int getSlotWidth();

        boolean needVideoTexture();

        void onActiveTextureReady();

        void onContentChanged();

        void onSizeChanged(int i);
    }

    public static class AlbumEntry {
        public BitmapTexture bitmapTexture;
        public Texture content;
        private BitmapLoader contentLoader;
        public boolean guessDeleted = true;
        public boolean inDeleteAnimation;
        public int index = -1;
        public boolean is3DModelImage;
        public boolean is3DPanorama;
        public boolean isBurstCover;
        public boolean isCloudPlaceHolder;
        public boolean isCloudRecycleItem = false;
        public boolean isFavoriteMagazine;
        public boolean isLivePhoto;
        public boolean isMyFavorite;
        public boolean isNewMagazine;
        public boolean isNoThumb;
        public boolean isPhotoSharePreView;
        public boolean isRectifyImage;
        public boolean isRefocusPhoto;
        public boolean isScreenShots;
        public boolean isVoiceImage;
        public MediaItem item;
        public int mediaType;
        public Path path;
        public long recycleTime = -1;
        public int rotation;
        public StringTexture videoDurationTexture;
        public StringTexture videoTitleTexture;
    }

    private static class BmpData {
        public Bitmap bmp;
        public Object obj;

        private BmpData() {
        }
    }

    private class ThumbnailLoader extends BitmapLoader {
        private final MediaItem mItem;
        private final int mSlotIndex;

        public ThumbnailLoader(int slotIndex, MediaItem item) {
            this.mSlotIndex = slotIndex;
            this.mItem = item;
        }

        protected void recycleBitmap(Bitmap bitmap) {
            GalleryPool.recycle(getPath(), this.mItem.getDateModifiedInSec(), bitmap, this.mItem.isDrm());
        }

        protected Future<Bitmap> submitBitmapTask(FutureListener<Bitmap> futureListener) {
            return CommonAlbumSlidingWindow.this.mThreadPool.submit(this.mItem.requestImage(2), this);
        }

        protected void onLoadComplete(Bitmap bitmap) {
            CommonAlbumSlidingWindow.this.mHandler.obtainMessage(0, this).sendToTarget();
        }

        protected void onPreviewLoad(Bitmap bitmap) {
            BmpData data = new BmpData();
            data.obj = this;
            data.bmp = bitmap;
            CommonAlbumSlidingWindow.this.mHandler.obtainMessage(1, data).sendToTarget();
        }

        protected Path getPath() {
            return this.mItem.getPath();
        }

        protected long getTimeModified() {
            return this.mItem.getDateModifiedInSec();
        }

        private String stringForTime(int totalSeconds) {
            int seconds = totalSeconds % 60;
            int minutes = (totalSeconds / 60) % 60;
            if (totalSeconds / 3600 > 0) {
                return String.format("%d:%02d:%02d", new Object[]{Integer.valueOf(totalSeconds / 3600), Integer.valueOf(minutes), Integer.valueOf(seconds)}).toString();
            }
            return String.format("%02d:%02d", new Object[]{Integer.valueOf(minutes), Integer.valueOf(seconds)}).toString();
        }

        private void updateVideoTexture(AlbumEntry entry) {
            if (CommonAlbumSlidingWindow.this.mNeedVideoTexture && (this.mItem instanceof IVideo)) {
                if (CommonAlbumSlidingWindow.this.mLimitVideoTextLength <= 0 && CommonAlbumSlidingWindow.this.mListener != null) {
                    CommonAlbumSlidingWindow.this.mLimitVideoTextLength = (int) (((float) CommonAlbumSlidingWindow.this.mListener.getSlotWidth()) * 0.85f);
                }
                IVideo videoItem = this.mItem;
                entry.videoTitleTexture = StringTexture.newInstance(videoItem.getName(), (float) CommonAlbumSlidingWindow.this.mLimitVideoTextLength, CommonAlbumSlidingWindow.this.mVideoTitlePaint);
                entry.videoDurationTexture = StringTexture.newInstance(stringForTime(videoItem.getDurationInSec()), (float) CommonAlbumSlidingWindow.this.mLimitVideoTextLength, CommonAlbumSlidingWindow.this.mVideoDurationPaint);
            }
        }

        public void updateEntry() {
            Bitmap bitmap = getBitmap();
            AlbumEntry entry = CommonAlbumSlidingWindow.this.mData[this.mSlotIndex % CommonAlbumSlidingWindow.this.mData.length];
            if (entry != null) {
                CommonAlbumSlidingWindow commonAlbumSlidingWindow;
                if (bitmap == null) {
                    if (isStateError()) {
                        entry.isNoThumb = true;
                        if (CommonAlbumSlidingWindow.this.isActiveSlot(this.mSlotIndex)) {
                            if (CommonAlbumSlidingWindow.this.mListener != null) {
                                CommonAlbumSlidingWindow.this.mListener.onContentChanged();
                            }
                            commonAlbumSlidingWindow = CommonAlbumSlidingWindow.this;
                            commonAlbumSlidingWindow.mActiveRequestCount = commonAlbumSlidingWindow.mActiveRequestCount - 1;
                            if (CommonAlbumSlidingWindow.this.mActiveRequestCount == 0) {
                                CommonAlbumSlidingWindow.this.requestNonactiveImages();
                            }
                        }
                    }
                } else if (!bitmap.isRecycled()) {
                    entry.bitmapTexture = new BitmapTexture(bitmap);
                    entry.content = entry.bitmapTexture;
                    updateVideoTexture(entry);
                    if (CommonAlbumSlidingWindow.this.isActiveSlot(this.mSlotIndex)) {
                        CommonAlbumSlidingWindow.this.mTextureUploader.addFgTexture(entry.bitmapTexture);
                        commonAlbumSlidingWindow = CommonAlbumSlidingWindow.this;
                        commonAlbumSlidingWindow.mActiveRequestCount = commonAlbumSlidingWindow.mActiveRequestCount - 1;
                        if (CommonAlbumSlidingWindow.this.mActiveRequestCount == 0) {
                            CommonAlbumSlidingWindow.this.requestNonactiveImages();
                        }
                        if (CommonAlbumSlidingWindow.this.mListener != null) {
                            CommonAlbumSlidingWindow.this.mListener.onContentChanged();
                        }
                    } else {
                        CommonAlbumSlidingWindow.this.mTextureUploader.addBgTexture(entry.bitmapTexture);
                    }
                }
            }
        }

        public void updateEntryPreview(Bitmap bitmap) {
            if (bitmap != null && !bitmap.isRecycled()) {
                AlbumEntry entry = CommonAlbumSlidingWindow.this.mData[this.mSlotIndex % CommonAlbumSlidingWindow.this.mData.length];
                if (entry != null) {
                    entry.bitmapTexture = new BitmapTexture(bitmap);
                    entry.content = entry.bitmapTexture;
                    updateVideoTexture(entry);
                    if (CommonAlbumSlidingWindow.this.isActiveSlot(this.mSlotIndex)) {
                        CommonAlbumSlidingWindow.this.mTextureUploader.addFgTexture(entry.bitmapTexture);
                        if (CommonAlbumSlidingWindow.this.mListener != null) {
                            CommonAlbumSlidingWindow.this.mListener.onContentChanged();
                        }
                    } else {
                        CommonAlbumSlidingWindow.this.mTextureUploader.addBgTexture(entry.bitmapTexture);
                    }
                }
            }
        }
    }

    public CommonAlbumSlidingWindow(GalleryContext activity, CommonAlbumDataLoader source, int cacheSize) {
        source.setDataListener(this);
        this.mContext = activity;
        this.mSource = source;
        this.mData = new AlbumEntry[cacheSize];
        this.mSize = source.size();
        this.mJobBulk = PhotoShareUtils.getBulk(1);
        this.mThreadPool = new JobLimiter(activity.getThreadPool(), 2);
    }

    public void setGLRoot(GLRoot glRoot) {
        this.mTextureUploader = new TextureUploader(glRoot);
        this.mHandler = new SynchronizedHandler(glRoot) {
            public void handleMessage(Message message) {
                if (!CommonAlbumSlidingWindow.this.mIsActive) {
                    GalleryLog.d("CommonAlbumSlidingWindow", "current is not active, handleMessage msg:" + message.what + ", hasFreeSlotContent:" + CommonAlbumSlidingWindow.this.mHasFreeSlotContent);
                }
                switch (message.what) {
                    case 0:
                        ((ThumbnailLoader) message.obj).updateEntry();
                        return;
                    case 1:
                        BmpData data = message.obj;
                        ((ThumbnailLoader) data.obj).updateEntryPreview(data.bmp);
                        GalleryLog.printDFXLog("DFX MSG_UPDATE_PREVIEW_ENTRY called");
                        return;
                    case 2:
                        if (CommonAlbumSlidingWindow.this.mListener != null && CommonAlbumSlidingWindow.this.mUpdateFinish && CommonAlbumSlidingWindow.this.mIsActive) {
                            GalleryLog.d("CommonAlbumSlidingWindow", "MSG_LOAD_ACTIVE_TEXTURE_OVER_TIME");
                            CommonAlbumSlidingWindow.this.mListener.onActiveTextureReady();
                            return;
                        }
                        return;
                    case 7:
                        CommonAlbumSlidingWindow.this.freeSlotContent();
                        CommonAlbumSlidingWindow.this.mHasFreeSlotContent = true;
                        return;
                    default:
                        return;
                }
            }
        };
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
        this.mNeedVideoTexture = this.mListener != null ? this.mListener.needVideoTexture() : false;
        if (this.mNeedVideoTexture) {
            this.mLimitVideoTextLength = (int) (((float) this.mListener.getSlotWidth()) * 0.85f);
            Resources r = this.mContext.getResources();
            this.mVideoTitlePaint = StringTexture.getDefaultPaint((float) r.getDimensionPixelSize(R.dimen.video_title_text_size), r.getColor(R.color.video_title_text_color));
            this.mVideoDurationPaint = StringTexture.getDefaultPaint((float) r.getDimensionPixelSize(R.dimen.video_duration_text_size), r.getColor(R.color.video_duration_text_color));
        }
    }

    public AlbumEntry get(int slotIndex) {
        if (!isActiveSlot(slotIndex)) {
            Utils.fail("invalid slot: %s outsides (%s, %s)", Integer.valueOf(slotIndex), Integer.valueOf(this.mActiveStart), Integer.valueOf(this.mActiveEnd));
        }
        return this.mData[slotIndex % this.mData.length];
    }

    public void prepareVisibleRangeItemIndex(HashMap<Path, Object> visiblePathMap, HashMap<Object, Object> visibleIndexMap) {
        for (int index = this.mContentStart; index < this.mContentEnd; index++) {
            AlbumEntry entry = this.mData[index % this.mData.length];
            if (!(entry == null || entry.path == null)) {
                entry.index = index;
                entry.inDeleteAnimation = true;
                entry.guessDeleted = true;
                visiblePathMap.put(entry.path, entry);
                visibleIndexMap.put(Integer.valueOf(index), entry);
            }
        }
    }

    public void freeVisibleRangeItem(HashMap<Path, Object> visiblePathMap) {
        for (int index = this.mContentStart; index < this.mContentEnd; index++) {
            AlbumEntry entry = this.mData[index % this.mData.length];
            if (!(entry == null || entry.path == null)) {
                AlbumEntry lastEntry = (AlbumEntry) visiblePathMap.get(entry.path);
                if (lastEntry != null && entry == lastEntry) {
                    lastEntry.index = -1;
                    lastEntry.inDeleteAnimation = false;
                    visiblePathMap.remove(entry.path);
                }
            }
        }
        for (Entry<Path, Object> entry2 : visiblePathMap.entrySet()) {
            AlbumEntry albumEntry = (AlbumEntry) entry2.getValue();
            if (albumEntry != null) {
                if (albumEntry.contentLoader != null) {
                    albumEntry.contentLoader.recycle();
                }
                if (albumEntry.bitmapTexture != null) {
                    albumEntry.bitmapTexture.recycle();
                }
                if (albumEntry.videoTitleTexture != null) {
                    albumEntry.videoTitleTexture.recycle();
                }
                if (albumEntry.videoDurationTexture != null) {
                    albumEntry.videoDurationTexture.recycle();
                }
            }
        }
    }

    public boolean isActiveSlot(int slotIndex) {
        return slotIndex >= this.mActiveStart && slotIndex < this.mActiveEnd;
    }

    private void setContentWindow(int contentStart, int contentEnd) {
        if (contentStart != this.mContentStart || contentEnd != this.mContentEnd) {
            if (this.mIsActive) {
                int n;
                int i;
                if (contentStart >= this.mContentEnd || this.mContentStart >= contentEnd) {
                    n = this.mContentEnd;
                    for (i = this.mContentStart; i < n; i++) {
                        freeSlotContent(i);
                    }
                    this.mSource.setActiveWindow(contentStart, contentEnd);
                    for (i = contentStart; i < contentEnd; i++) {
                        prepareSlotContent(i);
                    }
                } else {
                    for (i = this.mContentStart; i < contentStart; i++) {
                        freeSlotContent(i);
                    }
                    n = this.mContentEnd;
                    for (i = contentEnd; i < n; i++) {
                        freeSlotContent(i);
                    }
                    this.mSource.setActiveWindow(contentStart, contentEnd);
                    n = this.mContentStart;
                    for (i = contentStart; i < n; i++) {
                        prepareSlotContent(i);
                    }
                    for (i = this.mContentEnd; i < contentEnd; i++) {
                        prepareSlotContent(i);
                    }
                }
                this.mContentStart = contentStart;
                this.mContentEnd = contentEnd;
                return;
            }
            this.mContentStart = contentStart;
            this.mContentEnd = contentEnd;
            this.mSource.setActiveWindow(contentStart, contentEnd);
        }
    }

    public void setActiveWindow(int start, int end) {
        AlbumEntry[] data;
        int contentStart;
        if (start <= end && end - start <= this.mData.length) {
            if (end > this.mSize) {
            }
            data = this.mData;
            this.mActiveStart = start;
            this.mActiveEnd = end;
            contentStart = Utils.clamp(((start + end) / 2) - (data.length / 2), 0, Math.max(0, this.mSize - data.length));
            setContentWindow(contentStart, Math.min(data.length + contentStart, this.mSize));
            updateTextureUploadQueue();
            if (this.mIsActive) {
                updateAllImageRequests();
            }
        }
        Utils.fail("%s, %s, %s, %s", Integer.valueOf(start), Integer.valueOf(end), Integer.valueOf(this.mData.length), Integer.valueOf(this.mSize));
        data = this.mData;
        this.mActiveStart = start;
        this.mActiveEnd = end;
        contentStart = Utils.clamp(((start + end) / 2) - (data.length / 2), 0, Math.max(0, this.mSize - data.length));
        setContentWindow(contentStart, Math.min(data.length + contentStart, this.mSize));
        updateTextureUploadQueue();
        if (this.mIsActive) {
            updateAllImageRequests();
        }
    }

    private void uploadBgTextureInSlot(int index) {
        if (index < this.mContentEnd && index >= this.mContentStart) {
            AlbumEntry entry = this.mData[index % this.mData.length];
            if (entry.bitmapTexture != null) {
                this.mTextureUploader.addBgTexture(entry.bitmapTexture);
            }
        }
    }

    private void updateTextureUploadQueue() {
        if (this.mIsActive) {
            int i;
            this.mTextureUploader.clear();
            int n = this.mActiveEnd;
            for (i = this.mActiveStart; i < n; i++) {
                AlbumEntry entry = this.mData[i % this.mData.length];
                if (entry.bitmapTexture != null) {
                    this.mTextureUploader.addFgTexture(entry.bitmapTexture);
                }
            }
            int range = Math.max(this.mContentEnd - this.mActiveEnd, this.mActiveStart - this.mContentStart);
            for (i = 0; i < range; i++) {
                uploadBgTextureInSlot(this.mActiveEnd + i);
                uploadBgTextureInSlot((this.mActiveStart - i) - 1);
            }
        }
    }

    private void requestNonactiveImages() {
        if (this.mListener != null && this.mUpdateFinish && this.mIsActive) {
            this.mListener.onActiveTextureReady();
        }
        int range = Math.max(this.mContentEnd - this.mActiveEnd, this.mActiveStart - this.mContentStart);
        for (int i = 0; i < range; i++) {
            requestSlotImage(this.mActiveEnd + i);
            requestSlotImage((this.mActiveStart - 1) - i);
        }
    }

    private boolean requestSlotImage(int slotIndex) {
        if (slotIndex < this.mContentStart || slotIndex >= this.mContentEnd) {
            return false;
        }
        AlbumEntry entry = this.mData[slotIndex % this.mData.length];
        if (entry.content != null || entry.item == null) {
            return false;
        }
        if (this.mActiveStart <= slotIndex && this.mActiveEnd > slotIndex) {
            this.mJobBulk.addItem(entry.path);
        }
        entry.contentLoader.startLoad();
        return entry.contentLoader.isRequestInProgress();
    }

    private void cancelNonactiveImages() {
        int range = Math.max(this.mContentEnd - this.mActiveEnd, this.mActiveStart - this.mContentStart);
        for (int i = 0; i < range; i++) {
            cancelSlotImage(this.mActiveEnd + i);
            cancelSlotImage((this.mActiveStart - 1) - i);
        }
    }

    private void cancelSlotImage(int slotIndex) {
        if (slotIndex >= this.mContentStart && slotIndex < this.mContentEnd) {
            AlbumEntry item = this.mData[slotIndex % this.mData.length];
            if (item.contentLoader != null) {
                item.contentLoader.cancelLoad();
            }
        }
    }

    private void freeSlotContent(int slotIndex) {
        AlbumEntry[] data = this.mData;
        int index = slotIndex % data.length;
        AlbumEntry entry = data[index];
        TraceController.traceBegin("CommonAlbumSlidingWindow.freeSlotContent:" + entry.path);
        if (!entry.inDeleteAnimation) {
            if (entry.contentLoader != null) {
                entry.contentLoader.recycle();
            }
            if (entry.bitmapTexture != null) {
                entry.bitmapTexture.recycle();
            }
            if (entry.videoTitleTexture != null) {
                entry.videoTitleTexture.recycle();
            }
            if (entry.videoDurationTexture != null) {
                entry.videoDurationTexture.recycle();
            }
        }
        data[index] = null;
        TraceController.traceEnd();
    }

    private void prepareSlotContent(int slotIndex) {
        int i;
        boolean z = true;
        boolean z2 = false;
        AlbumEntry entry = new AlbumEntry();
        MediaItem item = this.mSource.get(slotIndex);
        entry.item = item;
        if (item == null) {
            i = 1;
        } else {
            i = entry.item.getMediaType();
        }
        entry.mediaType = i;
        entry.path = item == null ? null : item.getPath();
        entry.rotation = item == null ? 0 : item.getRotation();
        entry.contentLoader = new ThumbnailLoader(slotIndex, entry.item);
        entry.isVoiceImage = item == null ? false : item.isVoiceImage();
        entry.isMyFavorite = item == null ? false : item.isMyFavorite();
        entry.isBurstCover = item == null ? false : item.isBurstCover();
        boolean z3 = (item == null || (item.getExtraTag() & 1) == 0) ? false : true;
        entry.isNewMagazine = z3;
        z3 = (item == null || (item.getExtraTag() & 4) == 0) ? false : true;
        entry.isFavoriteMagazine = z3;
        entry.isCloudPlaceHolder = item == null ? false : item.isCloudPlaceholder();
        entry.isRefocusPhoto = item == null ? false : item.isRefocusPhoto();
        entry.is3DPanorama = item == null ? false : item.is3DPanorama();
        entry.isPhotoSharePreView = item == null ? false : item.isPhotoSharePreView();
        if (item != null && (item instanceof LocalMediaItem) && ((LocalMediaItem) item).getBucketId() == MediaSetUtils.getScreenshotsBucketID()) {
            z3 = true;
        } else {
            z3 = false;
        }
        entry.isScreenShots = z3;
        entry.isRectifyImage = item == null ? false : item.isRectifyImage();
        entry.is3DModelImage = item == null ? false : item.is3DModelImage();
        if (item == null || item.getSpecialFileType() != 50) {
            z = false;
        }
        entry.isLivePhoto = z;
        entry.recycleTime = !(item instanceof IRecycle) ? -1 : ((IRecycle) item).getRecycleTime();
        if (item != null) {
            z2 = item.isContainCloud();
        }
        entry.isCloudRecycleItem = z2;
        this.mData[slotIndex % this.mData.length] = entry;
    }

    private void updateAllImageRequests() {
        this.mActiveRequestCount = 0;
        this.mJobBulk.beginUpdateActiveList();
        int n = this.mActiveEnd;
        for (int i = this.mActiveStart; i < n; i++) {
            if (requestSlotImage(i)) {
                this.mActiveRequestCount++;
            }
        }
        if (this.mActiveRequestCount == 0) {
            requestNonactiveImages();
        } else {
            cancelNonactiveImages();
        }
        this.mJobBulk.endUpdateActiveList();
    }

    public void onLoadingStarted() {
        this.mUpdateFinish = false;
    }

    public void onSizeChanged(int size) {
        if (this.mSize != size) {
            this.mSize = size;
            if (this.mListener != null) {
                this.mListener.onSizeChanged(this.mSize);
            }
            if (this.mContentEnd > this.mSize) {
                this.mContentEnd = this.mSize;
            }
            if (this.mActiveEnd > this.mSize) {
                this.mActiveEnd = this.mSize;
            }
        }
    }

    public void onLoadingFinished() {
        this.mUpdateFinish = true;
        if (this.mIsActive) {
            updateAllImageRequests();
            if (this.mHandler != null) {
                this.mHandler.removeMessages(2);
                this.mHandler.sendEmptyMessageDelayed(2, 2000);
            }
        }
    }

    public void onContentChanged(int index) {
        if (index >= this.mContentStart && index < this.mContentEnd && this.mIsActive) {
            freeSlotContent(index);
            prepareSlotContent(index);
            updateAllImageRequests();
            if (this.mListener != null && isActiveSlot(index)) {
                this.mListener.onContentChanged();
            }
        }
    }

    public void resume() {
        this.mIsActive = true;
        GalleryLog.d("CommonAlbumSlidingWindow", "resume hasFreeSlotContent:" + this.mHasFreeSlotContent);
        if (this.mHandler != null) {
            this.mHandler.removeMessages(7);
        }
        if (this.mHasFreeSlotContent) {
            int n = this.mContentEnd;
            for (int i = this.mContentStart; i < n; i++) {
                prepareSlotContent(i);
            }
        }
        updateAllImageRequests();
        this.mHasFreeSlotContent = false;
    }

    public void pause(boolean needFreeSlotContent) {
        this.mIsActive = false;
        if (!needFreeSlotContent) {
            this.mHasFreeSlotContent = false;
        } else if (this.mHandler != null) {
            this.mHandler.sendEmptyMessageDelayed(7, 250);
        } else {
            freeSlotContent();
        }
        if (this.mHandler != null) {
            this.mHandler.removeMessages(2);
        }
        GalleryLog.d("CommonAlbumSlidingWindow", "pause freeSlotContent:" + needFreeSlotContent);
    }

    public void destroy() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(7);
        }
        if (!this.mHasFreeSlotContent) {
            freeSlotContent();
            this.mHasFreeSlotContent = true;
        }
    }

    private void freeSlotContent() {
        this.mTextureUploader.clear();
        if (this.mHandler != null) {
            this.mHandler.removeCallbacksAndMessages(null);
        }
        int n = this.mContentEnd;
        for (int i = this.mContentStart; i < n; i++) {
            freeSlotContent(i);
        }
    }
}
