package com.huawei.systemmanager.antivirus.ui.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.antivirus.ui.view.IVirusScanProgressShow.ScanStatus;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class QuickScanProgressWrapper implements IVirusScanProgressShow {
    private ImageScrollShowAnimation mAnimation;
    private Context mContext = this.mViewGroup.getContext();
    private ImageView mFlashBackGroundView;
    private Handler mHandler;
    private ViewGroup mLayoutGroup;
    private TextView mScanInfoView;
    private ViewGroup mViewGroup;

    class ImageScrollShowAnimation {
        private static final int DELAY_TIME = 10;
        private static final int INDEX_1 = 1;
        private static final int INDEX_2 = 2;
        private static final int INDEX_3 = 3;
        private static final int INDEX_4 = 4;
        private static final int INDEX_5 = 5;
        private static final int INDEX_6 = 6;
        public static final int TOTAL_TIME = 500;
        private int mCount;
        private int mCurImage;
        private Handler mHandler;
        private int mImageSourceIndex;
        private List<Drawable> mImages;
        private Interpolator mInterpolator = new LinearInterpolator();
        private int[] mItems;
        private ViewGroup mLayoutGroup;
        private Runnable mRunnable = new Runnable() {
            public void run() {
                float during = (float) (SystemClock.uptimeMillis() - ImageScrollShowAnimation.this.sTime);
                if (during > 500.0f) {
                    ImageScrollShowAnimation.this.checkBoarder();
                    ImageScrollShowAnimation.this.mItems = new int[]{ImageScrollShowAnimation.this.getIndexForItem(1), ImageScrollShowAnimation.this.getIndexForItem(2), ImageScrollShowAnimation.this.getIndexForItem(3), ImageScrollShowAnimation.this.getIndexForItem(4), ImageScrollShowAnimation.this.getIndexForItem(5), ImageScrollShowAnimation.this.getIndexForItem(6)};
                    if (ImageScrollShowAnimation.this.mImageSourceIndex < ImageScrollShowAnimation.this.mImages.size()) {
                        ApkIconView apkIconView = (ApkIconView) ImageScrollShowAnimation.this.mLayoutGroup.getChildAt(ImageScrollShowAnimation.this.mItems[0]);
                        List -get2 = ImageScrollShowAnimation.this.mImages;
                        ImageScrollShowAnimation imageScrollShowAnimation = ImageScrollShowAnimation.this;
                        int -get1 = imageScrollShowAnimation.mImageSourceIndex;
                        imageScrollShowAnimation.mImageSourceIndex = -get1 + 1;
                        apkIconView.setImageDrawable((Drawable) -get2.get(-get1));
                    }
                    ImageScrollShowAnimation.this.sTime = SystemClock.uptimeMillis();
                    ImageScrollShowAnimation.this.mHandler.post(ImageScrollShowAnimation.this.mRunnable);
                    return;
                }
                float y = ImageScrollShowAnimation.this.mInterpolator.getInterpolation(during / 500.0f);
                for (int i = 0; i < ImageScrollShowAnimation.this.mItems.length; i++) {
                    ((ApkIconView) ImageScrollShowAnimation.this.mLayoutGroup.getChildAt(ImageScrollShowAnimation.this.mItems[i])).setAnmationData(i, y);
                }
                ImageScrollShowAnimation.this.mHandler.postDelayed(ImageScrollShowAnimation.this.mRunnable, 10);
            }
        };
        private long sTime;

        public ImageScrollShowAnimation(ViewGroup groupView, Handler handler) {
            this.mLayoutGroup = groupView;
            this.mHandler = handler;
            this.mCount = this.mLayoutGroup.getChildCount();
            this.mImages = new ArrayList();
        }

        public void bindData(List<Drawable> images) {
            if (this.mImages != null) {
                this.mImages.clear();
                this.mImages.addAll(images);
            }
        }

        public void play() {
            setCurImage(3);
            this.mItems = new int[]{getIndexForItem(1), getIndexForItem(2), getIndexForItem(3), getIndexForItem(4), getIndexForItem(5), getIndexForItem(6)};
            this.sTime = SystemClock.uptimeMillis();
            reset();
            this.mImageSourceIndex = 0;
            for (int i = 0; i < this.mItems.length; i++) {
                ((ApkIconView) this.mLayoutGroup.getChildAt(this.mItems[i])).setImageDrawable((Drawable) this.mImages.get(i));
                this.mImageSourceIndex++;
            }
            this.mLayoutGroup.setVisibility(0);
            this.mHandler.post(this.mRunnable);
            QuickScanProgressWrapper.this.mFlashBackGroundView.startAnimation(AnimationUtils.loadAnimation(QuickScanProgressWrapper.this.mContext, R.anim.left_right_vibration));
            QuickScanProgressWrapper.this.mFlashBackGroundView.setVisibility(0);
        }

        public void stop() {
            this.mHandler.removeCallbacks(this.mRunnable);
        }

        public void reset() {
            this.mHandler.removeCallbacks(this.mRunnable);
            QuickScanProgressWrapper.this.setViewDefaultValue();
        }

        private void setCurImage(int index) {
            this.mCurImage = index;
        }

        private int getIndexForItem(int item) {
            int index = (this.mCurImage + item) - 3;
            while (index < 0) {
                index += this.mCount;
            }
            while (index > this.mCount - 1) {
                index -= this.mCount;
            }
            return index;
        }

        private void checkBoarder() {
            int i = this.mCurImage - 1;
            this.mCurImage = i;
            if (i < 0) {
                this.mCurImage = this.mCount - 1;
            }
        }
    }

    public QuickScanProgressWrapper(ViewGroup viewGroup, Handler handler) {
        this.mViewGroup = viewGroup;
        this.mHandler = handler;
    }

    public void initView() {
        this.mViewGroup.removeAllViews();
        LayoutInflater.from(this.mContext).inflate(R.layout.virus_quickscan_progress_layout, this.mViewGroup);
        this.mLayoutGroup = (ViewGroup) this.mViewGroup.findViewById(R.id.app_icon_layout);
        this.mFlashBackGroundView = (ImageView) this.mViewGroup.findViewById(R.id.app_center_view);
        this.mScanInfoView = (TextView) this.mViewGroup.findViewById(R.id.scan_info);
        setViewDefaultValue();
        this.mAnimation = new ImageScrollShowAnimation(this.mLayoutGroup, this.mHandler);
    }

    public void show(String params) {
    }

    public void cancel() {
        this.mAnimation.reset();
        this.mScanInfoView.setText(this.mContext.getString(R.string.antivirus_scan_uncomplete01));
        this.mScanInfoView.setVisibility(0);
    }

    public void finish(ScanStatus status) {
        this.mAnimation.reset();
        if (status == ScanStatus.DANGER) {
            this.mScanInfoView.setText(this.mContext.getString(R.string.antivus_risk_status));
        } else if (status == ScanStatus.RISK) {
            this.mScanInfoView.setText(this.mContext.getString(R.string.antivus_potential_risk_status));
        } else if (status == ScanStatus.SAFE) {
            this.mScanInfoView.setText(this.mContext.getString(R.string.antivus_safe_status));
        }
        this.mScanInfoView.setVisibility(0);
    }

    public void play() {
        List<HsmPkgInfo> pkgInfoList = HsmPackageManager.getInstance().getAllPackages();
        List<Drawable> images = new ArrayList();
        for (HsmPkgInfo info : pkgInfoList) {
            images.add(info.icon());
        }
        shuffle(images);
        this.mAnimation.bindData(images);
        this.mAnimation.play();
    }

    private <TYPE> void shuffle(List<TYPE> list) {
        int size = list.size();
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            Collections.swap(list, i, random.nextInt(size));
        }
    }

    private void setViewDefaultValue() {
        this.mLayoutGroup.setVisibility(8);
        this.mFlashBackGroundView.setVisibility(8);
        this.mFlashBackGroundView.clearAnimation();
        this.mScanInfoView.setVisibility(8);
    }
}
