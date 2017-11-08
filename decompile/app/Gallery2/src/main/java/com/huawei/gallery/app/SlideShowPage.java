package com.huawei.gallery.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import com.android.gallery3d.app.SlideshowDataAdapter;
import com.android.gallery3d.app.SlideshowDataAdapter.SlideshowSource;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.ContentListener;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.SlideshowView;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.multiscreen.MultiScreen;
import com.huawei.gallery.share.HwCustUtilsWrapper;
import java.util.ArrayList;
import java.util.Random;

public class SlideShowPage extends ActivityState {
    private HwCustSlideShowPage mCust = ((HwCustSlideShowPage) HwCustUtilsWrapper.createObj(HwCustSlideShowPage.class, new Object[0]));
    private Handler mHandler;
    private boolean mIsActive = false;
    private boolean mLaunchedFromPhotoPage = false;
    private MediaSet mMediaSet = null;
    private Model mModel;
    private Slide mPendingSlide = null;
    private final Intent mResultIntent = new Intent();
    private final GLView mRootPane = new GLView() {
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            SlideShowPage.this.mSlideshowView.layout(0, 0, right - left, bottom - top);
        }

        protected boolean onTouch(MotionEvent event) {
            if (event.getAction() == 1) {
                SlideShowPage.this.onBackPressed();
            }
            return true;
        }

        protected void renderBackground(GLCanvas canvas) {
            canvas.clearBuffer(getBackgroundColor());
        }
    };
    private boolean mSaveCurrentState = false;
    private SlideshowView mSlideshowView;

    public interface Model {
        Future<Slide> nextSlide(FutureListener<Slide> futureListener);

        void pause();

        void resume();
    }

    public static class SequentialSource implements SlideshowSource {
        private ArrayList<MediaItem> mData = new ArrayList();
        private int mDataStart = 0;
        private long mDataVersion = -1;
        private final MediaSet mMediaSet;
        private final boolean mRepeat;

        public SequentialSource(MediaSet mediaSet, boolean repeat) {
            this.mMediaSet = mediaSet;
            this.mRepeat = repeat;
        }

        public int findItemIndex(Path path, int hint) {
            return this.mMediaSet.getIndexOfItem(path, hint);
        }

        public MediaItem getMediaItem(int index) {
            MediaItem mediaItem = null;
            int dataEnd = this.mDataStart + this.mData.size();
            if (this.mRepeat) {
                int count = this.mMediaSet.getMediaItemCount();
                if (count == 0) {
                    return null;
                }
                index %= count;
            }
            if (index < this.mDataStart || index >= dataEnd) {
                this.mData = this.mMediaSet.getMediaItem(index, 32);
                this.mDataStart = index;
                dataEnd = index + this.mData.size();
            }
            if (index >= this.mDataStart && index < dataEnd) {
                mediaItem = (MediaItem) this.mData.get(index - this.mDataStart);
            }
            return mediaItem;
        }

        public long reload() {
            long version = this.mMediaSet.reload();
            if (version != this.mDataVersion) {
                this.mDataVersion = version;
                this.mData.clear();
            }
            return this.mDataVersion;
        }

        public void addContentListener(ContentListener listener) {
            this.mMediaSet.addContentListener(listener);
        }

        public void removeContentListener(ContentListener listener) {
            this.mMediaSet.removeContentListener(listener);
        }
    }

    public static class ShuffleSource implements SlideshowSource {
        private int mLastIndex = -1;
        private final MediaSet mMediaSet;
        private int[] mOrder = new int[0];
        private final Random mRandom = new Random();
        private final boolean mRepeat;
        private long mSourceVersion = -1;

        public ShuffleSource(MediaSet mediaSet, boolean repeat) {
            this.mMediaSet = (MediaSet) Utils.checkNotNull(mediaSet);
            this.mRepeat = repeat;
        }

        public int findItemIndex(Path path, int hint) {
            return hint;
        }

        public MediaItem getMediaItem(int index) {
            if ((!this.mRepeat && index >= this.mOrder.length) || this.mOrder.length == 0) {
                return null;
            }
            this.mLastIndex = this.mOrder[index % this.mOrder.length];
            MediaItem item = SlideShowPage.findMediaItem(this.mMediaSet, this.mLastIndex);
            for (int i = 0; i < 5 && item == null; i++) {
                GalleryLog.w("SildeshowPageFragment", "fail to find image: " + this.mLastIndex);
                this.mLastIndex = this.mRandom.nextInt(this.mOrder.length);
                item = SlideShowPage.findMediaItem(this.mMediaSet, this.mLastIndex);
            }
            return item;
        }

        public long reload() {
            long version = this.mMediaSet.reload();
            if (version != this.mSourceVersion) {
                this.mSourceVersion = version;
                int count = this.mMediaSet.getTotalMediaItemCount();
                if (count != this.mOrder.length) {
                    generateOrderArray(count);
                }
            }
            return version;
        }

        private void generateOrderArray(int totalCount) {
            int i;
            if (this.mOrder.length != totalCount) {
                this.mOrder = new int[totalCount];
                for (i = 0; i < totalCount; i++) {
                    this.mOrder[i] = i;
                }
            }
            for (i = totalCount - 1; i > 0; i--) {
                Utils.swap(this.mOrder, i, this.mRandom.nextInt(i + 1));
            }
            if (this.mOrder[0] == this.mLastIndex && totalCount > 1) {
                Utils.swap(this.mOrder, 0, this.mRandom.nextInt(totalCount - 1) + 1);
            }
        }

        public void addContentListener(ContentListener listener) {
            this.mMediaSet.addContentListener(listener);
        }

        public void removeContentListener(ContentListener listener) {
            this.mMediaSet.removeContentListener(listener);
        }
    }

    public static class Slide {
        public Bitmap bitmap;
        public int index;
        public MediaItem item;

        public Slide(MediaItem item, int index, Bitmap bitmap) {
            this.bitmap = bitmap;
            this.item = item;
            this.index = index;
        }
    }

    protected void onCreate(Bundle data, Bundle storedState) {
        this.mSaveCurrentState = storedState == null;
        super.onCreate(data, storedState);
        this.mFlags |= 48;
        initializeViews();
        initializeData(this.mData);
        this.mHandler = new SynchronizedHandler(this.mHost.getGLRoot()) {
            public void handleMessage(Message message) {
                switch (message.what) {
                    case 1:
                        SlideShowPage.this.loadNextBitmap();
                        return;
                    case 2:
                        SlideShowPage.this.showPendingBitmap();
                        return;
                    case 8:
                        if (SlideShowPage.this.mIsActive) {
                            SlideShowPage.this.mHost.getStateManager().finishState(SlideShowPage.this);
                            return;
                        }
                        return;
                    default:
                        throw new AssertionError();
                }
            }
        };
        this.mHost.requestFeature(95);
    }

    protected boolean onCreateActionBar(Menu menu) {
        this.mHost.getGalleryActionBar().enterActionMode(this.mSaveCurrentState);
        return super.onCreateActionBar(menu);
    }

    protected void onResume() {
        super.onResume();
        MultiScreen.get().enter();
        this.mIsActive = true;
        setContentPane(this.mRootPane);
        setKeepScreenOnFlag(true);
        this.mModel.resume();
        if (this.mPendingSlide != null) {
            showPendingBitmap();
        } else {
            loadNextBitmap();
        }
        if (this.mCust != null) {
            this.mCust.handleResumeCustomizations();
        }
    }

    protected void onPause() {
        super.onPause();
        MultiScreen.get().exit();
        if (this.mCust != null) {
            this.mCust.handlePauseCustomizations();
        }
        this.mIsActive = false;
        setKeepScreenOnFlag(false);
        this.mModel.pause();
        this.mSlideshowView.release();
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
    }

    protected void onDestroy() {
        if (this.mSaveCurrentState) {
            GalleryLog.d("SildeshowPageFragment", "onDestroy mActionBar.leaveCurrentMode()");
            this.mHost.getGalleryActionBar().leaveCurrentMode();
        }
        super.onDestroy();
        this.mHandler.removeCallbacksAndMessages(null);
        if (this.mCust != null) {
            this.mCust.releaseCustmizations();
        }
    }

    protected boolean onBackPressed() {
        if (!this.mLaunchedFromPhotoPage) {
            MultiScreen.get().exit();
        } else if (this.mPendingSlide != null) {
            MultiScreen.get().play(this.mPendingSlide.item, true);
        }
        return super.onBackPressed();
    }

    private void initializeViews() {
        this.mSlideshowView = new SlideshowView();
        this.mRootPane.addComponent(this.mSlideshowView);
    }

    private void initializeData(Bundle data) {
        boolean z = false;
        boolean random = data.getBoolean("random-order", false);
        MediaSet mediaSet = this.mHost.getGalleryContext().getDataManager().getMediaSet(data.getString("media-set-path"));
        if (this.mCust != null) {
            this.mCust.initializeCustData(data, this.mHost);
        }
        if (random) {
            this.mModel = new SlideshowDataAdapter(this.mHost.getGalleryContext(), new ShuffleSource(mediaSet, data.getBoolean("repeat")), 0, null);
            this.mResultIntent.putExtra("photo-index", 0);
        } else {
            initPendingSlide(data);
            int index = data.getInt("photo-index");
            String itemPath = data.getString("media-item-path");
            Path fromString = itemPath != null ? Path.fromString(itemPath) : null;
            if (itemPath != null) {
                z = true;
            }
            this.mLaunchedFromPhotoPage = z;
            boolean repeat = data.getBoolean("repeat");
            this.mResultIntent.putExtra("photo-index", index);
            if (this.mPendingSlide != null) {
                index = this.mPendingSlide.index + 1;
                fromString = null;
            }
            this.mMediaSet = mediaSet;
            this.mModel = new SlideshowDataAdapter(this.mHost.getGalleryContext(), new SequentialSource(mediaSet, repeat), index, fromString);
        }
        setStateResult(-1, this.mResultIntent);
    }

    private void initPendingSlide(Bundle data) {
        String previewPath = data.getString("media-preview-item-path");
        if (previewPath != null) {
            Path preview = Path.fromString(previewPath);
            if (preview != null) {
                MediaItem previewItem = (MediaItem) this.mHost.getGalleryContext().getDataManager().getMediaObject(preview);
                if (previewItem != null) {
                    Bitmap bitmap = previewItem.getScreenNailBitmap(1);
                    if (bitmap != null) {
                        this.mPendingSlide = new Slide(previewItem, data.getInt("media-preview-item-index", 0), bitmap);
                    }
                }
            }
        }
    }

    private void showPendingBitmap() {
        Slide slide = this.mPendingSlide;
        if (slide == null) {
            this.mHandler.sendEmptyMessage(8);
            return;
        }
        MultiScreen.get().play(slide.item, false);
        this.mSlideshowView.next(slide.bitmap, slide.item.getRotation());
        this.mResultIntent.putExtra("media-item-path", slide.item.getPath().toString()).putExtra("photo-index", slide.index + (this.mMediaSet.getMediaItemCount() * 2));
        setStateResult(-1, this.mResultIntent);
        if (this.mCust == null || !this.mCust.handleCustShowPendingBitmap(this.mHandler, 1)) {
            this.mHandler.sendEmptyMessageDelayed(1, 3000);
        }
    }

    private void loadNextBitmap() {
        this.mModel.nextSlide(new FutureListener<Slide>() {
            public void onFutureDone(Future<Slide> future) {
                SlideShowPage.this.mPendingSlide = (Slide) future.get();
                SlideShowPage.this.mHandler.sendEmptyMessage(2);
            }
        });
    }

    private static MediaItem findMediaItem(MediaSet mediaSet, int index) {
        MediaItem mediaItem = null;
        int n = mediaSet.getSubMediaSetCount();
        for (int i = 0; i < n; i++) {
            MediaSet subset = mediaSet.getSubMediaSet(i);
            if (subset != null) {
                int count = subset.getTotalMediaItemCount();
                if (index < count) {
                    return findMediaItem(subset, index);
                }
                index -= count;
            }
        }
        ArrayList<MediaItem> list = mediaSet.getMediaItem(index, 1);
        if (!list.isEmpty()) {
            mediaItem = (MediaItem) list.get(0);
        }
        return mediaItem;
    }

    private void setKeepScreenOnFlag(boolean isOn) {
        Window win = this.mHost.getActivity().getWindow();
        LayoutParams params = win.getAttributes();
        if (isOn) {
            params.flags |= 128;
        } else {
            params.flags &= -129;
        }
        win.setAttributes(params);
    }
}
