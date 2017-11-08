package com.huawei.gallery.ui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Message;
import android.text.TextPaint;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.BitmapLoader;
import com.android.gallery3d.ui.BitmapTexture;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.StringTexture;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.ui.Texture;
import com.android.gallery3d.ui.TextureUploader;
import com.android.gallery3d.ui.UploadedTexture;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.JobLimiter;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.app.CommonAlbumSetDataLoader;
import com.huawei.gallery.app.CommonAlbumSetDataLoader.DataListener;
import com.huawei.gallery.util.GalleryPool;
import java.util.ArrayList;

public class CommonAlbumSetSlotSlidingWindow implements DataListener {
    protected int mActiveEnd = 0;
    private int mActiveRequestCount = 0;
    protected int mActiveStart = 0;
    private TextPaint mAlbumNamePaint;
    private int mAlbumNameTextLength;
    protected int mContentEnd = 0;
    protected int mContentStart = 0;
    protected GalleryContext mContext;
    protected final AlbumSetEntry[] mData;
    private SynchronizedHandler mHandler = null;
    private boolean mHasFreeSlotContent = true;
    protected boolean mIsActive = false;
    protected Listener mListener;
    private int mSize;
    protected final CommonAlbumSetDataLoader mSource;
    protected TextureUploader mTextureUploader = null;
    private final JobLimiter mThreadPool;
    private boolean mUpdateFinish;

    public static class AlbumSetEntry {
        public MediaSet album;
        public StringTexture albumNameTexture;
        public BitmapTexture bitmapTexture;
        public Texture content;
        public BitmapLoader contentLoader;
        public MediaItem coverItem;
        public int index;
        public boolean isNoThumb;
        public boolean needToReload = false;
        public int rotation;
        public Path setPath;
    }

    public interface Listener {
        TextPaint getMainNamePaint(int i);

        int getSlotHeight(int i);

        int getSlotWidth(int i);

        void onActiveTextureReady();

        void onContentChanged();

        void onSizeChanged(int i);
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
            CommonAlbumSetSlotSlidingWindow.this.recycleLoaderBitmap(bitmap, this.mItem);
        }

        protected Future<Bitmap> submitBitmapTask(FutureListener<Bitmap> futureListener) {
            return CommonAlbumSetSlotSlidingWindow.this.mThreadPool.submit(this.mItem.requestImage(CommonAlbumSetSlotSlidingWindow.this.mSource.getThumbnailType()), this);
        }

        protected void onLoadComplete(Bitmap bitmap) {
            if (CommonAlbumSetSlotSlidingWindow.this.mHandler != null) {
                CommonAlbumSetSlotSlidingWindow.this.mHandler.obtainMessage(0, this).sendToTarget();
            }
        }

        protected void onPreviewLoad(Bitmap bitmap) {
            BmpData data = new BmpData();
            data.obj = this;
            data.bmp = bitmap;
            if (CommonAlbumSetSlotSlidingWindow.this.mHandler != null) {
                CommonAlbumSetSlotSlidingWindow.this.mHandler.obtainMessage(1, data).sendToTarget();
            }
        }

        protected Path getPath() {
            return this.mItem.getPath();
        }

        protected long getTimeModified() {
            return this.mItem.getDateModifiedInSec();
        }

        public void updateEntry() {
            int i = 0;
            Bitmap bitmap = getBitmap();
            AlbumSetEntry entry = CommonAlbumSetSlotSlidingWindow.this.mData[this.mSlotIndex % CommonAlbumSetSlotSlidingWindow.this.mData.length];
            if (entry != null) {
                if (bitmap == null) {
                    if (isStateError() && entry.content == null) {
                        entry.isNoThumb = true;
                    }
                    if (CommonAlbumSetSlotSlidingWindow.this.isActiveSlot(this.mSlotIndex) && CommonAlbumSetSlotSlidingWindow.this.mListener != null) {
                        CommonAlbumSetSlotSlidingWindow.this.mListener.onContentChanged();
                    }
                    CommonAlbumSetSlotSlidingWindow.this.updateAlbumNameTexture(entry);
                } else if (!bitmap.isRecycled()) {
                    entry.isNoThumb = false;
                    CommonAlbumSetSlotSlidingWindow.this.freeBitmapTexture(entry);
                    CommonAlbumSetSlotSlidingWindow.this.freeAlbumNameTexture(entry);
                    CommonAlbumSetSlotSlidingWindow.this.updateBitmapTexture(entry, bitmap);
                    entry.needToReload = false;
                    if (entry.coverItem != null) {
                        i = entry.coverItem.getRotation();
                    }
                    entry.rotation = i;
                    CommonAlbumSetSlotSlidingWindow.this.updateAlbumNameTexture(entry);
                    if (CommonAlbumSetSlotSlidingWindow.this.isActiveSlot(this.mSlotIndex)) {
                        CommonAlbumSetSlotSlidingWindow.this.uploadTexture(entry);
                        CommonAlbumSetSlotSlidingWindow commonAlbumSetSlotSlidingWindow = CommonAlbumSetSlotSlidingWindow.this;
                        commonAlbumSetSlotSlidingWindow.mActiveRequestCount = commonAlbumSetSlotSlidingWindow.mActiveRequestCount - 1;
                        if (CommonAlbumSetSlotSlidingWindow.this.mActiveRequestCount == 0) {
                            CommonAlbumSetSlotSlidingWindow.this.requestNonactiveImages();
                        }
                        if (CommonAlbumSetSlotSlidingWindow.this.mListener != null) {
                            CommonAlbumSetSlotSlidingWindow.this.mListener.onContentChanged();
                        }
                    } else {
                        CommonAlbumSetSlotSlidingWindow.this.uploadTexture(entry);
                    }
                }
            }
        }

        public void updateEntryPreview(Bitmap bitmap) {
            if (bitmap != null && !bitmap.isRecycled()) {
                AlbumSetEntry entry = CommonAlbumSetSlotSlidingWindow.this.mData[this.mSlotIndex % CommonAlbumSetSlotSlidingWindow.this.mData.length];
                if (entry != null) {
                    int i;
                    CommonAlbumSetSlotSlidingWindow.this.freeBitmapTexture(entry);
                    CommonAlbumSetSlotSlidingWindow.this.freeAlbumNameTexture(entry);
                    CommonAlbumSetSlotSlidingWindow.this.updateBitmapTexture(entry, bitmap);
                    if (entry.coverItem == null) {
                        i = 0;
                    } else {
                        i = entry.coverItem.getRotation();
                    }
                    entry.rotation = i;
                    entry.needToReload = false;
                    CommonAlbumSetSlotSlidingWindow.this.updateAlbumNameTexture(entry);
                    if (CommonAlbumSetSlotSlidingWindow.this.isActiveSlot(this.mSlotIndex)) {
                        CommonAlbumSetSlotSlidingWindow.this.uploadTexture(entry);
                        if (CommonAlbumSetSlotSlidingWindow.this.mListener != null) {
                            CommonAlbumSetSlotSlidingWindow.this.mListener.onContentChanged();
                        }
                    } else {
                        CommonAlbumSetSlotSlidingWindow.this.uploadTexture(entry);
                    }
                }
            }
        }
    }

    public CommonAlbumSetSlotSlidingWindow(GalleryContext activity, CommonAlbumSetDataLoader source, int cacheSize) {
        source.setModelListener(this);
        this.mContext = activity;
        this.mSource = source;
        this.mData = createDataArray(cacheSize);
        this.mSize = source.size();
        this.mThreadPool = new JobLimiter(activity.getThreadPool(), 2);
    }

    protected AlbumSetEntry[] createDataArray(int cacheSize) {
        return new AlbumSetEntry[cacheSize];
    }

    protected AlbumSetEntry createData() {
        return new AlbumSetEntry();
    }

    public ArrayList<MediaItem> getCoverItems() {
        return this.mSource.getCoverItems();
    }

    public void setGLRoot(GLRoot glRoot) {
        this.mTextureUploader = new TextureUploader(glRoot);
        this.mHandler = new SynchronizedHandler(glRoot) {
            public void handleMessage(Message message) {
                if (!CommonAlbumSetSlotSlidingWindow.this.mIsActive) {
                    GalleryLog.d("CommonAlbumSetSlotSlidingWindow", "current is not active, handleMessage msg:" + message.what + ", hasFreeSlotContent:" + CommonAlbumSetSlotSlidingWindow.this.mHasFreeSlotContent);
                }
                switch (message.what) {
                    case 0:
                        ((ThumbnailLoader) message.obj).updateEntry();
                        return;
                    case 1:
                        BmpData data = message.obj;
                        ((ThumbnailLoader) data.obj).updateEntryPreview(data.bmp);
                        return;
                    case 7:
                        CommonAlbumSetSlotSlidingWindow.this.freeSlotContent();
                        CommonAlbumSetSlotSlidingWindow.this.mHasFreeSlotContent = true;
                        return;
                    default:
                        return;
                }
            }
        };
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
        Resources r = this.mContext.getResources();
        this.mAlbumNamePaint = StringTexture.getDefaultPaint((float) r.getDimensionPixelSize(R.dimen.photoshare_tag_albumSet_name_size), r.getColor(R.color.photoshare_tag_albumSet_name_color));
    }

    protected int getTextLength() {
        if (this.mAlbumNameTextLength <= 0 && this.mListener != null) {
            this.mAlbumNameTextLength = (int) (((float) this.mListener.getSlotWidth(0)) * 0.85f);
        }
        return this.mAlbumNameTextLength;
    }

    public AlbumSetEntry get(int slotIndex) {
        if (!isActiveSlot(slotIndex)) {
            Utils.fail("invalid slot: %s outsides (%s, %s)", Integer.valueOf(slotIndex), Integer.valueOf(this.mActiveStart), Integer.valueOf(this.mActiveEnd));
        }
        return this.mData[slotIndex % this.mData.length];
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
        AlbumSetEntry[] data;
        int contentStart;
        int contentEnd;
        if (start <= end && end - start <= this.mData.length) {
            if (end > this.mSize) {
            }
            data = this.mData;
            this.mActiveStart = start;
            this.mActiveEnd = end;
            contentStart = Utils.clamp(((start + end) / 2) - (data.length / 2), 0, Math.max(0, this.mSize - data.length));
            contentEnd = Math.min(data.length + contentStart, this.mSize);
            this.mSource.setUIRange(this.mActiveStart, this.mActiveEnd);
            setContentWindow(contentStart, contentEnd);
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
        contentEnd = Math.min(data.length + contentStart, this.mSize);
        this.mSource.setUIRange(this.mActiveStart, this.mActiveEnd);
        setContentWindow(contentStart, contentEnd);
        updateTextureUploadQueue();
        if (this.mIsActive) {
            updateAllImageRequests();
        }
    }

    protected void uploadBgTextureInSlot(int index) {
        if (index < this.mContentEnd && index >= this.mContentStart) {
            AlbumSetEntry entry = this.mData[index % this.mData.length];
            if (entry.bitmapTexture != null && this.mTextureUploader != null) {
                this.mTextureUploader.addBgTexture(entry.bitmapTexture);
            }
        }
    }

    protected void updateTextureUploadQueue() {
        if (this.mIsActive && this.mTextureUploader != null) {
            int i;
            this.mTextureUploader.clear();
            int n = this.mActiveEnd;
            for (i = this.mActiveStart; i < n; i++) {
                AlbumSetEntry entry = this.mData[i % this.mData.length];
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
        AlbumSetEntry entry = this.mData[slotIndex % this.mData.length];
        if ((entry.content != null && !entry.needToReload) || entry.coverItem == null) {
            return false;
        }
        entry.contentLoader.startLoad(needForceStartTask());
        return entry.contentLoader.isRequestInProgress();
    }

    protected boolean needForceStartTask() {
        return false;
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
            AlbumSetEntry item = this.mData[slotIndex % this.mData.length];
            if (item.contentLoader != null) {
                item.contentLoader.cancelLoad();
            }
        }
    }

    protected void freeSlotContent(int slotIndex) {
        AlbumSetEntry[] data = this.mData;
        int index = slotIndex % data.length;
        AlbumSetEntry entry = data[index];
        TraceController.traceBegin("CommonAlbumSetSlotSlidingWindow.freeSlotContent:" + entry.setPath);
        if (entry.contentLoader != null) {
            entry.contentLoader.recycle();
        }
        freeBitmapTexture(entry);
        freeAlbumNameTexture(entry);
        data[index] = null;
        TraceController.traceEnd();
    }

    protected AlbumSetEntry getAlbumSetEntry(int slotIndex) {
        AlbumSetEntry[] data = this.mData;
        return data[slotIndex % data.length];
    }

    protected void freeAlbumNameTexture(AlbumSetEntry entry) {
        if (entry != null) {
            UploadedTexture texture = entry.albumNameTexture;
            if (texture != null) {
                texture.recycle();
            }
        }
    }

    protected void updateAlbumNameTexture(AlbumSetEntry entry) {
        if (entry.album != null) {
            entry.albumNameTexture = StringTexture.newInstance(entry.album.getName(), (float) getTextLength(), this.mAlbumNamePaint);
        }
    }

    protected void freeBitmapTexture(AlbumSetEntry entry) {
        if (entry != null) {
            BitmapTexture texture = entry.bitmapTexture;
            if (texture != null) {
                texture.recycle();
            }
        }
    }

    protected void updateBitmapTexture(AlbumSetEntry entry, Bitmap bitmap) {
        if (entry != null) {
            entry.bitmapTexture = new BitmapTexture(bitmap);
            entry.content = entry.bitmapTexture;
        }
    }

    protected void uploadTexture(AlbumSetEntry entry) {
        if (this.mTextureUploader != null) {
            this.mTextureUploader.addFgTexture(entry.bitmapTexture);
        }
    }

    private void prepareSlotContent(int slotIndex) {
        AlbumSetEntry entry = createData();
        MediaSet album = this.mSource.getMediaSet(slotIndex);
        entry.album = album;
        entry.setPath = album == null ? null : album.getPath();
        MediaItem[] itemArray = this.mSource.getCoverItem(slotIndex);
        if (itemArray == null || itemArray[0] == null) {
            entry.coverItem = null;
            if (album != null) {
                entry.isNoThumb = true;
                updateAlbumNameTexture(entry);
            }
        } else {
            entry.coverItem = itemArray[0];
        }
        entry.rotation = entry.coverItem == null ? 0 : entry.coverItem.getRotation();
        entry.contentLoader = new ThumbnailLoader(slotIndex, entry.coverItem);
        entry.index = slotIndex;
        this.mData[slotIndex % this.mData.length] = entry;
    }

    private void updateAllImageRequests() {
        this.mActiveRequestCount = 0;
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
    }

    protected void recycleLoaderBitmap(Bitmap bitmap, MediaItem item) {
        if (bitmap != null) {
            if (item == null) {
                bitmap.recycle();
            } else {
                GalleryPool.recycle(item.getPath(), item.getDateModifiedInSec(), bitmap, item.isDrm());
            }
        }
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

    public void onLoadingStarted() {
        this.mUpdateFinish = false;
    }

    public void onLoadingFinished() {
        this.mUpdateFinish = true;
        if (this.mIsActive) {
            updateAllImageRequests();
        }
    }

    public void onContentChanged(int index) {
        if (index >= this.mContentStart && index < this.mContentEnd && this.mIsActive) {
            AlbumSetEntry[] data = this.mData;
            AlbumSetEntry oldEntry = data[index % data.length];
            MediaSet album = this.mSource.getMediaSet(index);
            if (album == null || oldEntry == null || oldEntry.album == null || !oldEntry.setPath.toString().equals(album.getPath().toString())) {
                freeSlotContent(index);
                prepareSlotContent(index);
                updateAllImageRequests();
            } else {
                MediaItem[] itemArray = this.mSource.getCoverItem(index);
                if (!(itemArray == null || itemArray[0] == null)) {
                    if (!(oldEntry.coverItem == null || !oldEntry.coverItem.getPath().toString().equalsIgnoreCase(itemArray[0].getPath().toString()) || (oldEntry.content == null && oldEntry.isNoThumb))) {
                        if (oldEntry.needToReload) {
                        }
                    }
                    oldEntry.coverItem = itemArray[0];
                    if (oldEntry.contentLoader != null) {
                        oldEntry.contentLoader.recycle();
                    }
                    oldEntry.contentLoader = new ThumbnailLoader(index, oldEntry.coverItem);
                    oldEntry.needToReload = true;
                    updateAllImageRequests();
                }
            }
            if (this.mListener != null && isActiveSlot(index)) {
                this.mListener.onContentChanged();
            }
        }
    }

    public void resume() {
        this.mIsActive = true;
        if (this.mHandler != null) {
            this.mHandler.removeMessages(7);
        }
        GalleryLog.d("CommonAlbumSetSlotSlidingWindow", "resume hasFreeSlotContent:" + this.mHasFreeSlotContent);
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
        GalleryLog.d("CommonAlbumSetSlotSlidingWindow", "pause freeSlotContent:" + needFreeSlotContent);
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

    protected void freeSlotContent() {
        if (this.mTextureUploader != null) {
            this.mTextureUploader.clear();
        }
        if (this.mHandler != null) {
            this.mHandler.removeCallbacksAndMessages(null);
        }
        int n = this.mContentEnd;
        for (int i = this.mContentStart; i < n; i++) {
            freeSlotContent(i);
        }
    }
}
