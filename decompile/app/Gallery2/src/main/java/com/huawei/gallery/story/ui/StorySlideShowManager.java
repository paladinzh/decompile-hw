package com.huawei.gallery.story.ui;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import com.android.gallery3d.app.SlideshowDataAdapter;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.huawei.gallery.app.GLHost;
import com.huawei.gallery.app.SlideShowPage.Model;
import com.huawei.gallery.app.SlideShowPage.SequentialSource;
import com.huawei.gallery.app.SlideShowPage.Slide;
import java.util.ArrayList;

public class StorySlideShowManager {
    private Handler mHandler;
    private GLHost mHost;
    private MediaSet mMediaSet;
    private Model mModel;
    private Slide mPendingSlide = null;
    private final GLView mRootPane;
    private StorySlideshowView mSlideshowView;

    public StorySlideShowManager(GLView rootPane, GLHost host, MediaSet mediaSet) {
        this.mRootPane = rootPane;
        this.mHost = host;
        this.mMediaSet = mediaSet;
        this.mSlideshowView = new StorySlideshowView(this.mRootPane);
        onCreate();
    }

    public void onCreate() {
        this.mHandler = new SynchronizedHandler(this.mHost.getGLRoot()) {
            public void handleMessage(Message message) {
                switch (message.what) {
                    case 1:
                        StorySlideShowManager.this.loadNextBitmap();
                        return;
                    case 2:
                        StorySlideShowManager.this.showPendingBitmap();
                        return;
                    default:
                        throw new AssertionError();
                }
            }
        };
        initPendingSlide();
        this.mModel = new SlideshowDataAdapter(this.mHost.getGalleryContext(), new SequentialSource(this.mMediaSet, true), this.mPendingSlide != null ? this.mPendingSlide.index + 1 : 0, null);
    }

    private void initPendingSlide() {
        ArrayList<MediaItem> arrayList = this.mMediaSet.getMediaItem(0, 1);
        if (arrayList.size() != 0) {
            MediaItem previewItem = (MediaItem) arrayList.get(0);
            Bitmap bitmap = previewItem.getScreenNailBitmap(1);
            if (bitmap != null) {
                this.mPendingSlide = new Slide(previewItem, 0, bitmap);
            }
        }
    }

    private void showPendingBitmap() {
        Slide slide = this.mPendingSlide;
        if (slide != null) {
            this.mSlideshowView.next(slide.bitmap, slide.item.getRotation());
            this.mHandler.sendEmptyMessageDelayed(1, 3000);
        }
    }

    private void loadNextBitmap() {
        this.mModel.nextSlide(new FutureListener<Slide>() {
            public void onFutureDone(Future<Slide> future) {
                StorySlideShowManager.this.mPendingSlide = (Slide) future.get();
                StorySlideShowManager.this.mHandler.sendEmptyMessage(2);
            }
        });
    }

    public void onResume() {
        this.mModel.resume();
        if (this.mPendingSlide != null) {
            showPendingBitmap();
        } else {
            loadNextBitmap();
        }
    }

    public void onPause() {
        this.mModel.pause();
        this.mSlideshowView.release();
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
    }

    public void render(GLCanvas canvas, int left, int top, int right, int bottom) {
        this.mSlideshowView.render(canvas, left, top, right, bottom);
    }
}
