package com.huawei.keyguard.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import com.android.keyguard.R$id;
import com.fyusion.sdk.viewer.FyuseException;
import com.fyusion.sdk.viewer.FyuseViewer;
import com.fyusion.sdk.viewer.RequestListener;
import com.fyusion.sdk.viewer.view.FyuseView;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.support.magazine.HwFyuseUtils;
import com.huawei.keyguard.util.HwLog;
import java.io.File;

public class HwFyuseView extends FyuseView {
    private static Object object = new Object();
    private Handler mHandler = GlobalContext.getUIHandler();
    private boolean mIsLoadedSuccess = true;
    private boolean mMotionFlag = true;
    private String mPicPath = null;
    private ImageView mPreview;
    private Runnable mUpdatePreviewState = new Runnable() {
        public void run() {
            HwLog.i("HwFyuseView", "mUpdatePreviewState set gone");
            HwFyuseView.this.mPreview.setVisibility(8);
        }
    };

    public HwFyuseView(Context context) {
        super(context);
    }

    public HwFyuseView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    protected void doInit(Context context, LayoutParams params) {
        super.doInit(context, params);
        this.mPreview = new ImageView(context);
        this.mPreview.setLayoutParams(params);
        addView(this.mPreview);
        this.mPreview.setScaleY(1.05f);
        this.mPreview.setScaleX(1.05f);
    }

    public void setImageBitmapPath(String path) {
        this.mHandler.removeCallbacks(this.mUpdatePreviewState);
        this.mPreview.setVisibility(0);
    }

    protected void onFyuseShown() {
        super.onFyuseShown();
        HwLog.i("HwFyuseView", "onFyuseShown");
        setLoadedStatus(true);
        if (this.mPreview.getVisibility() == 0) {
            this.mHandler.removeCallbacks(this.mUpdatePreviewState);
            this.mHandler.postDelayed(this.mUpdatePreviewState, 200);
            return;
        }
        HwLog.i("HwFyuseView", "mPreview is GONE");
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mHandler.removeCallbacks(this.mUpdatePreviewState);
        this.mPicPath = null;
    }

    public void enableMotion(boolean flag) {
        if (this.mMotionFlag != flag) {
            String fileName = null;
            if (this.mPicPath != null) {
                fileName = new File(this.mPicPath).getName();
            }
            HwLog.i("HwFyuseView", "enableMotion flag: " + flag + ", fileName:" + fileName);
            this.mMotionFlag = flag;
            super.enableMotion(flag);
        }
    }

    public boolean isLoadedSuccess() {
        boolean z;
        synchronized (object) {
            z = this.mIsLoadedSuccess;
        }
        return z;
    }

    public void setLoadedStatus(boolean isLoadedSuccess) {
        synchronized (object) {
            this.mIsLoadedSuccess = isLoadedSuccess;
            if (!this.mIsLoadedSuccess) {
                HwLog.i("HwFyuseView", "setLoadedStatus mPicPath is null");
                this.mPicPath = null;
                setFyuseGone();
            }
        }
    }

    private void setFyuseGone() {
        setVisibility(8);
        ImageView imageView = (ImageView) ((View) getParent()).findViewById(R$id.magazine_img);
        if (imageView != null) {
            imageView.setVisibility(0);
        }
    }

    public void setDynamicFyuseView(final String path, boolean force) {
        if (TextUtils.isEmpty(path)) {
            HwLog.i("HwFyuseView", "Fyuse pic path is emtpty");
            return;
        }
        if (!path.equals(this.mPicPath) || force) {
            File file = new File(path);
            HwLog.i("HwFyuseView", "fyuse load file : " + file.getName());
            setImageBitmapPath(path);
            this.mPicPath = path;
            FyuseViewer.with(getContext()).load(file).highRes(true).listener(new RequestListener() {
                public boolean onLoadFailed(FyuseException e, Object model) {
                    HwLog.e("HwFyuseView", "Load is failing" + model + ", " + e.getMessage());
                    HwFyuseView.this.setLoadedStatus(false);
                    HwFyuseUtils.updateSinglePicFormat(path, 1);
                    return false;
                }

                public boolean onResourceReady(Object model) {
                    return false;
                }

                public void onProgress(int progress) {
                }
            }).into((FyuseView) this);
        }
    }

    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == 8) {
            enableMotion(false);
        }
    }

    public void setPreViewImage(Bitmap bitmap) {
        this.mPreview.setImageBitmap(bitmap);
    }

    public void destroySurface() {
        HwLog.i("HwFyuseView", "destroySurface");
        super.destroySurface();
        this.mPicPath = null;
    }
}
