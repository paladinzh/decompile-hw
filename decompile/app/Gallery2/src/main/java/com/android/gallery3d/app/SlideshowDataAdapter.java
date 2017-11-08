package com.android.gallery3d.app;

import android.graphics.Bitmap;
import com.android.gallery3d.data.ContentListener;
import com.android.gallery3d.data.IVideo;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.ThreadPool;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.huawei.gallery.app.SlideShowPage.Model;
import com.huawei.gallery.app.SlideShowPage.Slide;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class SlideshowDataAdapter implements Model {
    private boolean mDataReady;
    private long mDataVersion = -1;
    private final LinkedList<Slide> mImageQueue = new LinkedList();
    private Path mInitialPath;
    private boolean mIsActive = false;
    private int mLoadIndex = 0;
    private final AtomicBoolean mNeedReload = new AtomicBoolean(false);
    private boolean mNeedReset;
    private int mNextOutput = 0;
    private Future<Void> mReloadTask;
    private final SlideshowSource mSource;
    private final SourceListener mSourceListener = new SourceListener();
    private final ThreadPool mThreadPool;

    private class ReloadTask extends BaseJob<Void> {
        private ReloadTask() {
        }

        public Void run(JobContext jc) {
            while (true) {
                synchronized (SlideshowDataAdapter.this) {
                    while (SlideshowDataAdapter.this.mIsActive && (!SlideshowDataAdapter.this.mDataReady || SlideshowDataAdapter.this.mImageQueue.size() >= 3)) {
                        try {
                            SlideshowDataAdapter.this.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
                if (!SlideshowDataAdapter.this.mIsActive) {
                    return null;
                }
                SlideshowDataAdapter.this.mNeedReset = false;
                MediaItem item = SlideshowDataAdapter.this.loadItem();
                SlideshowDataAdapter slideshowDataAdapter;
                if (SlideshowDataAdapter.this.mNeedReset) {
                    slideshowDataAdapter = SlideshowDataAdapter.this;
                    synchronized (slideshowDataAdapter) {
                        SlideshowDataAdapter.this.mImageQueue.clear();
                        SlideshowDataAdapter.this.mLoadIndex = SlideshowDataAdapter.this.mNextOutput;
                    }
                } else if (item == null) {
                    slideshowDataAdapter = SlideshowDataAdapter.this;
                    synchronized (slideshowDataAdapter) {
                        if (!SlideshowDataAdapter.this.mNeedReload.get()) {
                            SlideshowDataAdapter.this.mDataReady = false;
                        }
                        SlideshowDataAdapter.this.notifyAll();
                    }
                } else {
                    int type;
                    if (item.isDrm()) {
                        type = 2;
                    } else {
                        type = 1;
                    }
                    Bitmap bitmap = (Bitmap) item.requestImage(type).run(jc);
                    if (bitmap != null) {
                        synchronized (SlideshowDataAdapter.this) {
                            SlideshowDataAdapter.this.mImageQueue.addLast(new Slide(item, SlideshowDataAdapter.this.mLoadIndex, bitmap));
                            if (SlideshowDataAdapter.this.mImageQueue.size() == 1) {
                                SlideshowDataAdapter.this.notifyAll();
                            }
                        }
                    }
                    slideshowDataAdapter = SlideshowDataAdapter.this;
                    slideshowDataAdapter.mLoadIndex = slideshowDataAdapter.mLoadIndex + 1;
                }
            }
        }

        public boolean isHeavyJob() {
            return true;
        }

        public String workContent() {
            return "reload item current is " + SlideshowDataAdapter.this.mLoadIndex;
        }

        public boolean needDecodeVideoFromOrigin() {
            MediaItem item = SlideshowDataAdapter.this.loadItem();
            if (!(item instanceof IVideo)) {
                return false;
            }
            int i;
            if (item.isDrm()) {
                i = 2;
            } else {
                i = 1;
            }
            return item.requestImage(i).needDecodeVideoFromOrigin();
        }
    }

    public interface SlideshowSource {
        void addContentListener(ContentListener contentListener);

        int findItemIndex(Path path, int i);

        MediaItem getMediaItem(int i);

        long reload();

        void removeContentListener(ContentListener contentListener);
    }

    private class SourceListener implements ContentListener {
        private SourceListener() {
        }

        public void onContentDirty() {
            synchronized (SlideshowDataAdapter.this) {
                SlideshowDataAdapter.this.mNeedReload.set(true);
                SlideshowDataAdapter.this.mDataReady = true;
                SlideshowDataAdapter.this.notifyAll();
            }
        }
    }

    public SlideshowDataAdapter(GalleryContext context, SlideshowSource source, int index, Path initialPath) {
        this.mSource = source;
        this.mInitialPath = initialPath;
        this.mLoadIndex = index;
        this.mNextOutput = index;
        this.mThreadPool = context.getThreadPool();
    }

    private MediaItem loadItem() {
        if (this.mNeedReload.compareAndSet(true, false)) {
            long v = this.mSource.reload();
            if (v != this.mDataVersion) {
                this.mDataVersion = v;
                this.mNeedReset = true;
                return null;
            }
        }
        int index = this.mLoadIndex;
        if (this.mInitialPath != null) {
            index = this.mSource.findItemIndex(this.mInitialPath, index);
            this.mInitialPath = null;
        }
        return this.mSource.getMediaItem(index);
    }

    private synchronized Slide innerNextBitmap() {
        while (this.mIsActive && this.mDataReady && this.mImageQueue.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new AssertionError();
            }
        }
        if (this.mImageQueue.isEmpty()) {
            return null;
        }
        this.mNextOutput++;
        notifyAll();
        return (Slide) this.mImageQueue.removeFirst();
    }

    public Future<Slide> nextSlide(FutureListener<Slide> listener) {
        return this.mThreadPool.submit(new BaseJob<Slide>() {
            public Slide run(JobContext jc) {
                jc.setMode(0);
                return SlideshowDataAdapter.this.innerNextBitmap();
            }

            public boolean isHeavyJob() {
                return true;
            }

            public String workContent() {
                return "decode next bitmap for slide";
            }
        }, listener);
    }

    public void pause() {
        synchronized (this) {
            this.mIsActive = false;
            notifyAll();
        }
        this.mSource.removeContentListener(this.mSourceListener);
        this.mReloadTask.cancel();
        this.mReloadTask.waitDone();
        this.mReloadTask = null;
    }

    public synchronized void resume() {
        this.mIsActive = true;
        this.mSource.addContentListener(this.mSourceListener);
        this.mNeedReload.set(true);
        this.mDataReady = true;
        this.mReloadTask = this.mThreadPool.submit(new ReloadTask());
    }
}
