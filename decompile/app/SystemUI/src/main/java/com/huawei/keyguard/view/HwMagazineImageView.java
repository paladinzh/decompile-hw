package com.huawei.keyguard.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.keyguard.R$id;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.inf.HwKeyguardPolicy;
import com.huawei.keyguard.support.magazine.BigPicture;
import com.huawei.keyguard.support.magazine.BigPictureInfo;
import com.huawei.keyguard.support.magazine.HwFyuseUtils;
import com.huawei.keyguard.support.magazine.KeyguardWallpaper;
import com.huawei.keyguard.support.magazine.MagazineWallpaper;
import com.huawei.keyguard.util.BitmapUtils;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.view.effect.bokeh.BokehDrawable;

public class HwMagazineImageView extends FrameLayout {
    private static boolean mIsCreatingBitmap = false;
    private static boolean mShouldEnableFyuseMotion = true;
    private FyuseBitmapWorkerTask mBitmapWorkerTask;
    private HwFyuseView mFyuseView;
    private ImageView mImageView;

    private class FyuseBitmapWorkerTask extends AsyncTask<Void, Void, BokehDrawable> {
        BigPicture bigPicture;
        boolean isLoadSuccess;

        private FyuseBitmapWorkerTask() {
            this.isLoadSuccess = true;
        }

        protected BokehDrawable doInBackground(Void... params) {
            if (isCancelled()) {
                HwLog.i("MagazineView", "task is isCancelled0");
                return null;
            }
            Bitmap bm = null;
            try {
                bm = HwMagazineImageView.this.mFyuseView.getView().getBitmap();
            } catch (OutOfMemoryError ex) {
                HwLog.i("MagazineView", "OutOfMemoryError : " + ex.toString());
            }
            if (bm == null) {
                HwLog.w("MagazineView", "doInBackground fyuseview get bitmap is null.");
                this.isLoadSuccess = false;
                return null;
            } else if (isCancelled()) {
                HwLog.i("MagazineView", "task is isCancelled1");
                return null;
            } else {
                MagazineWallpaper magazineWallpaper = MagazineWallpaper.getInst(HwMagazineImageView.this.getContext());
                BigPictureInfo bigPictureInfo = magazineWallpaper.getPictureInfo(0);
                this.bigPicture = magazineWallpaper.getPortarit3DBigPicture(bigPictureInfo, bm);
                if (this.bigPicture == null) {
                    cancel(false);
                    return null;
                }
                BokehDrawable bokehDrawable = this.bigPicture.getBokehDrawable(HwMagazineImageView.this.getContext());
                if (bokehDrawable == null) {
                    HwLog.w("MagazineView", "doInBackground bigPicture getBokehDrawable is null.");
                    return null;
                }
                bokehDrawable.setBitmapPath(bigPictureInfo.getPicPath());
                return bokehDrawable;
            }
        }

        protected void onPostExecute(BokehDrawable bokehDrawable) {
            if (bokehDrawable == null || !this.isLoadSuccess) {
                HwLog.w("MagazineView", "onPostExecute bokehDrawable null");
                HwMagazineImageView.mIsCreatingBitmap = false;
                HwMagazineImageView.this.mImageView.setVisibility(0);
                HwMagazineImageView.this.mFyuseView.setVisibility(8);
                return;
            }
            Drawable groundPic = HwMagazineImageView.this.mImageView.getDrawable();
            if (groundPic instanceof BokehDrawable) {
                float bokehValue = ((BokehDrawable) groundPic).getBokehValue();
                bokehDrawable.setBokehValue(bokehValue);
                HwLog.i("MagazineView", "onPostExecute setBokehValue: " + bokehValue);
                HwMagazineImageView.this.mImageView.setImageDrawable(bokehDrawable);
                if (bokehValue > 0.001f) {
                    HwMagazineImageView.this.setImageViewVisible();
                }
                if (HwMagazineImageView.this.mImageView.getVisibility() == 0) {
                    bokehDrawable.invalidateSelf();
                }
            }
            HwMagazineImageView.mIsCreatingBitmap = false;
        }
    }

    public HwMagazineImageView(Context context) {
        super(context);
    }

    public HwMagazineImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HwMagazineImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void resetStatus() {
        mIsCreatingBitmap = false;
        mShouldEnableFyuseMotion = true;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mFyuseView = (HwFyuseView) findViewById(R$id.magazine_fyuse);
        if (this.mFyuseView != null) {
            this.mFyuseView.setRotateWithGravity(false);
            this.mFyuseView.enableGesture(false);
            this.mFyuseView.setVisibility(8);
            this.mFyuseView.enableMotion(false);
        }
        this.mImageView = (ImageView) findViewById(R$id.magazine_img);
    }

    public void forceLoadFyuseFile() {
        if (HwFyuseUtils.isSupport3DFyuse() && this.mFyuseView != null) {
            if (this.mImageView.getDrawable() == null) {
                HwLog.i("MagazineView", "mImageView.getDrawable is null");
            }
            BigPictureInfo bigPictureInfo = MagazineWallpaper.getInst(getContext()).getCurrentWallpaper();
            if (bigPictureInfo != null && HwFyuseUtils.isCurrent3DWallpaper(getContext())) {
                this.mFyuseView.setDynamicFyuseView(bigPictureInfo.getPicPath(), true);
            }
        }
    }

    public void setImageViewVisible() {
        if (HwFyuseUtils.isSupport3DFyuse()) {
            this.mImageView.setVisibility(0);
            if (this.mFyuseView != null) {
                this.mFyuseView.setVisibility(8);
            }
        }
    }

    public void resetCurrentWallPaper() {
        if (HwFyuseUtils.isSupport3DFyuse() && this.mFyuseView != null) {
            BigPictureInfo bigPictureInfo = MagazineWallpaper.getInst(getContext()).getCurrentWallpaper();
            if (bigPictureInfo != null && bigPictureInfo.isFyuseFormatPic()) {
                Drawable currentWallpaper = KeyguardWallpaper.getInst(getContext()).getCurrentWallPaper();
                if (currentWallpaper == null) {
                    HwLog.i("MagazineView", "Current WallPaper is null");
                    return;
                }
                Drawable drawable = this.mImageView.getDrawable();
                if (!BitmapUtils.isSameDrawable(drawable, currentWallpaper) && (drawable instanceof BokehDrawable)) {
                    ((BokehDrawable) drawable).releaseFyuseRotateBitmap();
                }
                this.mImageView.setImageDrawable(currentWallpaper);
            }
        }
    }

    public HwFyuseView getFyuseView() {
        return this.mFyuseView;
    }

    public ImageView getImageView() {
        return this.mImageView;
    }

    public void releaseFyuseData() {
        if (this.mFyuseView != null) {
            this.mFyuseView.setVisibility(8);
            this.mFyuseView.destroySurface();
        }
        this.mImageView.setVisibility(0);
    }

    public void loadMagazineWallPaper(BigPictureInfo bigPictureInfo, Bitmap bitmap) {
        if (this.mFyuseView == null || bigPictureInfo == null) {
            this.mImageView.setVisibility(0);
            return;
        }
        if (bigPictureInfo.isFyuseFormatPic()) {
            this.mFyuseView.setVisibility(0);
            this.mFyuseView.setPreViewImage(bitmap);
            this.mFyuseView.setDynamicFyuseView(bigPictureInfo.getPicPath(), false);
            this.mFyuseView.setContentDescription(bigPictureInfo.getPicPath());
        } else {
            this.mFyuseView.setVisibility(8);
            if (bigPictureInfo.getPicFormat() != 11) {
                this.mFyuseView.destroySurface();
            }
        }
    }

    public void handleUpdateWallpaper(boolean isScreenOn) {
        if (this.mFyuseView == null) {
            this.mImageView.setVisibility(0);
            return;
        }
        BigPictureInfo bigPictureInfo = MagazineWallpaper.getInst(getContext()).getCurrentWallpaper();
        if (bigPictureInfo == null || !HwFyuseUtils.isCurrent3DWallpaper(getContext())) {
            setImageViewVisible();
            if (!(bigPictureInfo == null || bigPictureInfo.getPicFormat() == 11)) {
                this.mFyuseView.destroySurface();
            }
            HwLog.i("MagazineView", "update Wallpaper imageview");
        } else {
            HwLog.i("MagazineView", "update Wallpaper fyuseView");
            resetPreview();
            this.mFyuseView.setDynamicFyuseView(bigPictureInfo.getPicPath(), false);
            if (!checkCurrentBokehDrawableAttr()) {
                this.mImageView.setVisibility(8);
                this.mFyuseView.setVisibility(0);
                this.mFyuseView.enableMotion(isScreenOn);
                HwLog.i("MagazineView", "update Wallpaper mFyuseView");
            }
        }
    }

    private void resetPreview() {
        if (this.mFyuseView != null) {
            BigPicture bigPicture = MagazineWallpaper.getInst(getContext()).getCurrentPicture();
            if (!(bigPicture == null || bigPicture.getBitmap() == null)) {
                this.mFyuseView.setPreViewImage(bigPicture.getBitmap());
            }
        }
    }

    public void creatFyuseCurrentBitmap() {
        mIsCreatingBitmap = true;
        if (!(this.mBitmapWorkerTask == null || this.mBitmapWorkerTask.isCancelled())) {
            this.mBitmapWorkerTask.cancel(true);
        }
        this.mBitmapWorkerTask = new FyuseBitmapWorkerTask();
        this.mBitmapWorkerTask.execute(new Void[0]);
    }

    public boolean setFyuseBokehValue(float param, boolean isScreenOn) {
        if (checkToSetFyuseGone()) {
            return false;
        }
        if (!HwKeyguardUpdateMonitor.getInstance(getContext()).shouldShowing()) {
            HwLog.i("MagazineView", "keyguard is not showing");
            return false;
        } else if (checkCurrentBokehDrawableAttr()) {
            return false;
        } else {
            float fraction = param < 0.0f ? 0.0f : param > 1.0f ? 1.0f : param;
            Drawable groundPic = this.mImageView.getDrawable();
            BokehDrawable bokehDrawable = null;
            if (groundPic instanceof BokehDrawable) {
                bokehDrawable = (BokehDrawable) groundPic;
                bokehDrawable.setBokehValue(fraction);
            }
            if (fraction > 0.001f || !mShouldEnableFyuseMotion) {
                this.mFyuseView.enableMotion(false);
                if (!(bokehDrawable == null || mIsCreatingBitmap)) {
                    setImageViewVisible();
                    groundPic.invalidateSelf();
                }
                return true;
            }
            this.mImageView.setVisibility(8);
            this.mFyuseView.setVisibility(0);
            this.mFyuseView.enableMotion(isScreenOn);
            return true;
        }
    }

    public void proFyuseMotionEvent(MotionEvent event, boolean isScreenOn) {
        if (!checkToSetFyuseGone()) {
            if (!HwKeyguardUpdateMonitor.getInstance(getContext()).isShowing()) {
                HwLog.i("MagazineView", "procUnlockMotionEvent keyguard is not showing");
            } else if (checkCurrentBokehDrawableAttr()) {
                HwLog.i("MagazineView", "it is fyuse file, but it is not fyuse drawable");
            } else {
                int action = event.getAction();
                if (action == 0) {
                    this.mFyuseView.enableMotion(false);
                    if (!HwKeyguardPolicy.getInst().isQsExpanded()) {
                        HwLog.i("MagazineView", "ACTION_DOWN FyuseBitmapWorkerTask");
                        creatFyuseCurrentBitmap();
                    }
                    return;
                }
                switch (action) {
                    case 1:
                    case 3:
                    case 6:
                        mShouldEnableFyuseMotion = true;
                        handleActionUp(isScreenOn);
                        break;
                    case 2:
                        mShouldEnableFyuseMotion = false;
                        break;
                }
            }
        }
    }

    public void forceToUpdateFyusePic() {
        if (!HwFyuseUtils.isCurrent3DWallpaper(getContext()) || this.mFyuseView == null) {
            setImageViewVisible();
            HwLog.i("MagazineView", "forceToUpdate imageView");
            return;
        }
        Drawable drawable = this.mImageView.getDrawable();
        if ((drawable instanceof BokehDrawable) && ((BokehDrawable) drawable).getBokehValue() > 0.001f) {
            setImageViewVisible();
        } else if (!checkCurrentBokehDrawableAttr()) {
            this.mImageView.setVisibility(8);
            this.mFyuseView.setVisibility(0);
            HwLog.i("MagazineView", "forceToUpdate fyuseView");
            BigPictureInfo bigPictureInfo = MagazineWallpaper.getInst(getContext()).getCurrentWallpaper();
            if (bigPictureInfo != null) {
                resetPreview();
                this.mFyuseView.setDynamicFyuseView(bigPictureInfo.getPicPath(), true);
            }
            this.mFyuseView.enableMotion(true);
        }
    }

    private boolean checkCurrentBokehDrawableAttr() {
        Drawable drawable = KeyguardWallpaper.getInst(getContext()).getCurrentWallPaper();
        if (!(drawable instanceof BokehDrawable) || ((BokehDrawable) drawable).isFyuseDrawble()) {
            return false;
        }
        setImageViewVisible();
        HwLog.i("MagazineView", "it is not fyuse bokehDrawable, but it is fyuse file.");
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setFyuseAnimationParamInner() {
        if (HwFyuseUtils.isSupport3DFyuse() && HwFyuseUtils.isCurrent3DWallpaper(getContext()) && this.mImageView.getVisibility() != 0) {
            Drawable drawable = KeyguardWallpaper.getInst(getContext()).getCurrentWallPaper();
            if ((drawable instanceof BokehDrawable) && ((BokehDrawable) drawable).isFyuseDrawble()) {
                this.mFyuseView.enableMotion(false);
                creatFyuseCurrentBitmap();
            }
        }
    }

    private boolean checkToSetFyuseGone() {
        if (HwFyuseUtils.isCurrent3DWallpaper(getContext()) && this.mFyuseView != null && this.mFyuseView.isLoadedSuccess()) {
            return false;
        }
        setImageViewVisible();
        return true;
    }

    private void handleActionUp(boolean isScreenOn) {
        mIsCreatingBitmap = false;
        if (this.mFyuseView == null) {
            this.mImageView.setVisibility(0);
            return;
        }
        Drawable groundPic = this.mImageView.getDrawable();
        if ((groundPic instanceof BokehDrawable) && ((BokehDrawable) groundPic).getBokehValue() <= 0.001f) {
            this.mImageView.setVisibility(8);
            this.mFyuseView.setVisibility(0);
            this.mFyuseView.enableMotion(isScreenOn);
        }
    }
}
