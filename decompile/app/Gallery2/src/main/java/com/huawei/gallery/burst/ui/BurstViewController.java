package com.huawei.gallery.burst.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import com.android.gallery3d.R;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.burst.BurstPhotoManager;
import com.huawei.gallery.burst.BurstThumbnailLoader;
import com.huawei.gallery.burst.ui.BurstImageThumbGallery.MyGestureListener;
import com.huawei.gallery.burst.ui.MyAdapterView.OnItemClickListener;
import com.huawei.gallery.burst.ui.MyAdapterView.OnItemSelectedListener;
import com.huawei.gallery.ui.stackblur.StackBlurUtils;
import com.huawei.watermark.manager.parse.WMElement;

public class BurstViewController implements OnItemSelectedListener, OnItemClickListener, OnTouchListener {
    float cache = 0.0f;
    private int mBestPhotoIndex = 0;
    private BitmapBlurRunnable mBitmapBlurRunnable = new BitmapBlurRunnable();
    private final BackgroundBlurListener mBlurListener = new BackgroundBlurListener();
    private BurstImagePreviewGallery mBurstPreviewView;
    private BurstImageThumbGallery mBurstThumbView;
    private ViewGroup mContainer;
    private final Context mContext;
    private final BurstPhotoManager mManager;
    private boolean mNeedResetRatio;
    float mOnePixelThreshold = WMElement.CAMERASIZEVALUE1B1;
    private final ViewGroup mParentLayout;
    private BurstImagePreviewAdapter mPreviewAdapter;
    float mPreviewThumbRatio = WMElement.CAMERASIZEVALUE1B1;
    private boolean mSingleTapMode = false;
    boolean mSuppressThumbTouch = false;
    private Thread mThread;
    private BurstImageThumbAdapter mThumbAdapter;

    private class BackgroundBlurListener implements OnLayoutChangeListener {
        private BackgroundBlurListener() {
        }

        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            boolean changed = true;
            if (left - oldLeft == 0 && top - oldTop == 0 && right - oldRight == 0 && bottom - oldBottom == 0) {
                changed = false;
            }
            if (changed && BurstViewController.this.mPreviewAdapter != null) {
                Bitmap bitmap = BurstViewController.this.mPreviewAdapter.getCoverBitmap();
                if (bitmap != null && !bitmap.isRecycled()) {
                    BurstViewController.this.setBackground(bitmap, right - left, bottom - top);
                }
            }
        }
    }

    private class BitmapBlurRunnable implements Runnable {
        private int mHeight;
        private Bitmap mSrc;
        private int mWidth;

        private BitmapBlurRunnable() {
        }

        public void set(Bitmap src, int width, int height) {
            this.mSrc = src;
            this.mWidth = width;
            this.mHeight = height;
            GalleryLog.d("BurstViewController", String.format("blur bitmap to %sx%s", new Object[]{Integer.valueOf(this.mWidth), Integer.valueOf(this.mHeight)}));
        }

        public void run() {
            if (this.mSrc != null) {
                Runnable setBgRunnable = new SetBgRunnable(StackBlurUtils.getDefaultBlurBackground(BurstViewController.this.mContext, BitmapUtils.resizeAndCropCenter(this.mSrc, this.mWidth, this.mHeight, false)));
                View container = BurstViewController.this.mContainer;
                if (container != null) {
                    container.post(setBgRunnable);
                }
                this.mSrc = null;
            }
        }
    }

    private class SetBgRunnable implements Runnable {
        private Drawable mBackground;

        SetBgRunnable(Drawable bg) {
            this.mBackground = bg;
        }

        public void run() {
            GalleryLog.d("BurstViewController", "set background with blur bitmap");
            View container = BurstViewController.this.mContainer;
            if (container != null && this.mBackground != null) {
                container.setBackground(this.mBackground);
            }
        }
    }

    public BurstViewController(ViewGroup parentLayout, Context context, BurstPhotoManager manager) {
        this.mParentLayout = parentLayout;
        this.mContext = context;
        this.mManager = manager;
        this.mPreviewAdapter = new BurstImagePreviewAdapter(context, manager.getSelectManager());
        this.mThumbAdapter = new BurstImageThumbAdapter(context, manager.getSelectManager());
    }

    public void enterBurstMode(Bitmap coverThumbnail, Bitmap coverMicroThumbnail) {
        if (this.mContainer == null) {
            initView();
        }
        this.mParentLayout.addView(this.mContainer);
        this.mContainer.addOnLayoutChangeListener(this.mBlurListener);
        setBackground(coverThumbnail, this.mParentLayout.getWidth(), this.mParentLayout.getHeight());
        this.mNeedResetRatio = true;
    }

    public void setThumbnailLoader(BurstThumbnailLoader loader) {
        this.mPreviewAdapter.setThumbnailLoader(loader);
        this.mThumbAdapter.setThumbnailLoader(loader);
    }

    private void setBackground(Bitmap bitmap, int width, int height) {
        if (this.mThread != null) {
            this.mThread.interrupt();
        }
        this.mBitmapBlurRunnable.set(bitmap, width, height);
        this.mThread = new Thread(this.mBitmapBlurRunnable, "blur bitmap thread");
        this.mThread.start();
    }

    private void initView() {
        if (this.mContainer == null) {
            this.mContainer = (ViewGroup) ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.photo_fragment_burst_gallery, this.mParentLayout, false);
            this.mBurstPreviewView = (BurstImagePreviewGallery) this.mContainer.findViewById(R.id.burst_preview_gallery);
            this.mBurstPreviewView.setOnItemSelectedListener(this);
            this.mBurstPreviewView.setOnItemClickListener(this);
            this.mBurstPreviewView.setAdapter(this.mPreviewAdapter);
            this.mBurstPreviewView.setCallbackDuringFling(false);
            this.mBurstPreviewView.setOnTouchListener(this);
            this.mBurstPreviewView.setController(this);
            this.mBurstThumbView = (BurstImageThumbGallery) this.mContainer.findViewById(R.id.burst_thumb_gallery);
            this.mBurstThumbView.setOnItemSelectedListener(this);
            this.mBurstThumbView.setOnItemClickListener(this);
            this.mBurstThumbView.setAdapter(this.mThumbAdapter);
            this.mBurstThumbView.setOnTouchListener(this);
            this.mBurstThumbView.setOnFlingListener(new MyGestureListener() {
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    if (BurstViewController.this.mSuppressThumbTouch) {
                        return false;
                    }
                    return BurstViewController.this.mBurstThumbView.superFling(e1, e2, velocityX, velocityY);
                }

                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    BurstViewController.this.getRatio();
                    return BurstViewController.this.mBurstThumbView.superScroll(e1, e2, distanceX * (BurstViewController.this.mSuppressThumbTouch ? BurstViewController.this.mPreviewThumbRatio : WMElement.CAMERASIZEVALUE1B1), distanceY);
                }
            });
        }
    }

    public void leaveBurstMode() {
        this.mContainer.removeOnLayoutChangeListener(this.mBlurListener);
        this.mParentLayout.removeView(this.mContainer);
        this.mContainer.setBackground(null);
        this.mContainer = null;
        this.mPreviewAdapter.clear();
        this.mThumbAdapter.clear();
        this.mPreviewAdapter.notifyDataSetChanged();
        this.mThumbAdapter.notifyDataSetChanged();
        if (this.mBurstPreviewView != null) {
            this.mBurstPreviewView.setOnItemSelectedListener(null);
            this.mBurstPreviewView.setOnItemClickListener(null);
            this.mBurstPreviewView.setAdapter(null);
            this.mBurstPreviewView.setOnTouchListener(null);
            this.mBurstPreviewView.setController(null);
            this.mBurstPreviewView = null;
        }
        if (this.mBurstThumbView != null) {
            this.mBurstThumbView.setOnItemSelectedListener(null);
            this.mBurstThumbView.setAdapter(null);
            this.mBurstThumbView.setOnItemClickListener(null);
            this.mBurstThumbView.setOnTouchListener(null);
            this.mBurstThumbView.setOnFlingListener(null);
            this.mBurstThumbView = null;
        }
    }

    public void setBest(int index) {
        this.mBestPhotoIndex = index;
        GalleryLog.d("BurstViewController", "set best photo : " + index);
        this.mBurstPreviewView.setSelection(index, true);
        this.mBurstThumbView.setSelection(index, true);
        this.mManager.onToggle(index);
        this.mBurstPreviewView.scrollToPosition(index, true);
        this.mBurstThumbView.scrollToPosition(index, true);
    }

    public void updateView(Bitmap thumbnail, Bitmap microThumbNail, int index) {
        this.mPreviewAdapter.updateViewForPosition(index, thumbnail);
        this.mThumbAdapter.updateViewForPosition(index, microThumbNail);
    }

    public void onItemSelected(MyAdapterView<?> parent, View view, int position, long id) {
        if (this.mBestPhotoIndex != -1) {
            this.mBurstThumbView.setSelection(this.mBestPhotoIndex, true);
            this.mBurstPreviewView.setSelection(this.mBestPhotoIndex, true);
            return;
        }
        if (parent == this.mBurstPreviewView && !this.mSuppressThumbTouch && !this.mSingleTapMode) {
            this.mBurstThumbView.setSelection(position, true);
        } else if (!(parent != this.mBurstThumbView || this.mSuppressThumbTouch || this.mSingleTapMode)) {
            this.mBurstPreviewView.setSelection(position, true);
        }
    }

    public void onNothingSelected(MyAdapterView<?> myAdapterView) {
    }

    public void onItemClick(MyAdapterView<?> parent, View view, int position, long id) {
        if (parent == this.mBurstPreviewView) {
            selectItem(position);
            return;
        }
        this.mSingleTapMode = true;
        this.mBurstPreviewView.scrollToPosition(position, true);
    }

    private void selectItem(int position) {
        if (!this.mManager.isPending()) {
            BurstImagePreviewView previewView = this.mPreviewAdapter.getViewAt(position);
            BurstImageThumbView thumbView = this.mThumbAdapter.getViewAt(position);
            if (!(previewView == null || thumbView == null || !previewView.isWholeWidthVisible())) {
                boolean state = this.mManager.onToggle(position);
                previewView.setSelectState(state);
                thumbView.setSelectState(state);
                this.mBurstPreviewView.invalidate();
                this.mBurstThumbView.invalidate();
            }
        }
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (this.mBestPhotoIndex != -1) {
            this.mBestPhotoIndex = -1;
        }
        if (view instanceof BurstImagePreviewGallery) {
            this.mSingleTapMode = false;
            this.mBurstPreviewView.invalidate();
            boolean ret = this.mBurstPreviewView.onTouchEvent(motionEvent);
            this.mSuppressThumbTouch = true;
            this.mBurstThumbView.setCallbackDuringFling(false);
            return ret;
        }
        if (view instanceof BurstImageThumbGallery) {
            this.mSingleTapMode = false;
            this.mSuppressThumbTouch = false;
            this.mBurstThumbView.setCallbackDuringFling(true);
        }
        return false;
    }

    public void trackScroll(int deltaX) {
        if (!this.mSingleTapMode) {
            if (((float) Math.abs(deltaX)) >= this.mOnePixelThreshold || Math.abs(this.cache) >= this.mOnePixelThreshold) {
                int dX;
                if (this.cache != 0.0f) {
                    this.cache += (float) deltaX;
                    dX = Math.round(this.cache * getRatio());
                    this.cache = 0.0f;
                } else {
                    dX = Math.round(((float) deltaX) * getRatio());
                }
                this.mBurstThumbView.trackMotionScroll(dX);
                return;
            }
            this.cache += (float) deltaX;
        }
    }

    private float getRatio() {
        if (this.mNeedResetRatio || Math.abs(this.mPreviewThumbRatio - WMElement.CAMERASIZEVALUE1B1) < 0.05f) {
            View v = this.mBurstThumbView.getSelectedView();
            View x = this.mBurstPreviewView.getSelectedView();
            if (v == null || x == null) {
                return this.mPreviewThumbRatio;
            }
            int width = v.getWidth() + this.mBurstThumbView.getSpacing();
            int wX = x.getWidth() + this.mBurstPreviewView.getSpacing();
            this.mPreviewThumbRatio = ((float) width) / ((float) wX);
            this.mOnePixelThreshold = ((float) wX) / ((float) width);
            this.mNeedResetRatio = false;
        }
        return this.mPreviewThumbRatio;
    }

    public void onFinishedMovement() {
        GalleryLog.d("BurstViewController", "onFinishedMovement");
        this.mBurstThumbView.onFinishedMovement();
    }

    public void onConfigurationChanged(boolean needReset) {
        this.mNeedResetRatio = needReset;
    }
}
