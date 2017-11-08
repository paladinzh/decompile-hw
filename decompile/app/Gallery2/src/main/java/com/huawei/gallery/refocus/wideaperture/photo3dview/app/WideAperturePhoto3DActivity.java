package com.huawei.gallery.refocus.wideaperture.photo3dview.app;

import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.FrameLayout.LayoutParams;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.exif.ExifInterface;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.app.AbstractGalleryActivity;
import com.huawei.gallery.refocus.app.AbsRefocusDelegate;
import com.huawei.gallery.refocus.wideaperture.photo3dview.ui.WideAperturePhoto3DView;
import java.io.IOException;

public class WideAperturePhoto3DActivity extends AbstractGalleryActivity {
    private WideAperturePhoto3DView m3DView;
    private float[] mAngle = new float[]{0.0f, 0.0f};
    private WideAperturePhoto3DViewController mController;
    private boolean mDestroyed = false;
    private String mFilePath;
    private boolean mNeedCreateView = true;
    private int mPhotoHeight;
    private int mPhotoOrientation = -1;
    private int mPhotoWidth;
    private float mRatio;
    private int mScreenHeight;
    private int mScreenWidth;
    private SensorEventListener mSenserEventListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            float[] gravity = new float[]{event.values[0], event.values[1]};
            switch (WideAperturePhoto3DActivity.this.getOrientation()) {
                case 0:
                    gravity[0] = gravity[0] * GroundOverlayOptions.NO_DIMENSION;
                    break;
                case 1:
                    Utils.swap(gravity, 0, 1);
                    break;
                case 2:
                    gravity[1] = gravity[1] * GroundOverlayOptions.NO_DIMENSION;
                    break;
                case 3:
                    gravity[0] = gravity[0] * GroundOverlayOptions.NO_DIMENSION;
                    gravity[1] = gravity[1] * GroundOverlayOptions.NO_DIMENSION;
                    Utils.swap(gravity, 0, 1);
                    break;
            }
            if (WideAperturePhoto3DActivity.this.m3DView != null) {
                WideAperturePhoto3DActivity.this.m3DView.setViewAngle(gravity[0], gravity[1]);
                WideAperturePhoto3DActivity.this.m3DView.requestRender();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private int mViewHeight;
    private int mViewWidth;

    private class WideAperture3DPhotoDelegateAbs extends AbsRefocusDelegate {
        private WideAperture3DPhotoDelegateAbs() {
        }

        public String getFilePath() {
            return WideAperturePhoto3DActivity.this.mFilePath;
        }

        public int getPhotoHeight() {
            return WideAperturePhoto3DActivity.this.mPhotoHeight;
        }

        public int getPhotoWidth() {
            return WideAperturePhoto3DActivity.this.mPhotoWidth;
        }

        public void preparePhotoComplete() {
            if (WideAperturePhoto3DActivity.this.mNeedCreateView) {
                WideAperturePhoto3DActivity.this.mNeedCreateView = false;
                WideAperturePhoto3DActivity.this.m3DView.create3DView();
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);
        getWindow().setBackgroundDrawable(new ColorDrawable(-16777216));
        this.m3DView = new WideAperturePhoto3DView(this);
        setContentView(this.m3DView);
        initData();
    }

    protected void onResume() {
        super.onResume();
        if (this.mNeedCreateView && this.mController != null && this.mController.prepareComplete()) {
            this.mNeedCreateView = false;
            this.m3DView.create3DView();
        }
        this.m3DView.onResume();
        changeLayout();
        SensorManager sensorManager = (SensorManager) getSystemService("sensor");
        sensorManager.registerListener(this.mSenserEventListener, sensorManager.getDefaultSensor(4), 1);
    }

    protected void onPause() {
        super.onPause();
        this.m3DView.destroyView();
        this.mNeedCreateView = true;
        ((SensorManager) getActivityContext().getSystemService("sensor")).unregisterListener(this.mSenserEventListener);
        this.m3DView.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mDestroyed = true;
        this.mController.cleanUp();
    }

    public boolean isDestroyed() {
        return this.mDestroyed;
    }

    public void onBackPressed() {
        setResult(-1, null);
        super.onBackPressed();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        changeLayout();
    }

    private void changeLayout() {
        getScreenSize();
        get3DViewWidthAndHeight();
        int screenWidth = this.mScreenWidth;
        LayoutParams layoutParams = (LayoutParams) this.m3DView.getLayoutParams();
        layoutParams.topMargin = (this.mScreenHeight - this.mViewHeight) / 2;
        layoutParams.leftMargin = (screenWidth - this.mViewWidth) / 2;
        layoutParams.width = this.mViewWidth;
        layoutParams.height = this.mViewHeight;
        this.m3DView.setLayoutParams(layoutParams);
    }

    private void initData() {
        Bundle data = getIntent().getExtras();
        if (data != null) {
            Path itemPath = getItemPath(data);
            if (itemPath != null) {
                MediaItem mediaItem = (MediaItem) getDataManager().getMediaObject(itemPath);
                if (mediaItem != null) {
                    this.mFilePath = mediaItem.getFilePath();
                } else {
                    return;
                }
            }
            getPhotoOrientation();
            getScreenSize();
            getPhotoWidthAndHeight();
            get3DViewWidthAndHeight();
            this.mController = new WideAperturePhoto3DViewController(this, new WideAperture3DPhotoDelegateAbs());
            this.mController.setViewMode(1);
            if (this.mPhotoOrientation == 90 || this.mPhotoOrientation == 270) {
                this.mController.init3DViewDisplayParams(this.mAngle, getOrientation(), this.mViewHeight, this.mViewWidth);
            } else {
                this.mController.init3DViewDisplayParams(this.mAngle, getOrientation(), this.mViewWidth, this.mViewHeight);
            }
            this.mController.prepare();
            this.m3DView.set3DViewController(this.mController);
        }
    }

    private Path getItemPath(Bundle data) {
        String itemPathString = data.getString("media-item-path");
        if (itemPathString != null) {
            return Path.fromString(itemPathString);
        }
        return null;
    }

    private int getPhotoOrientation() {
        if (this.mPhotoOrientation > 0) {
            return this.mPhotoOrientation;
        }
        ExifInterface exifInterface = new ExifInterface();
        this.mPhotoOrientation = 0;
        try {
            exifInterface.readExif(this.mFilePath);
            Integer orientationValue = exifInterface.getTagIntValue(ExifInterface.TAG_ORIENTATION);
            if (orientationValue != null) {
                this.mPhotoOrientation = ExifInterface.getRotationForOrientationValue(orientationValue.shortValue());
            }
            return this.mPhotoOrientation;
        } catch (IOException e) {
            GalleryLog.e("WideAperturePhoto3DActivity", "getPhotoOrientation exception");
            return this.mPhotoOrientation;
        } catch (Throwable t) {
            GalleryLog.w("WideAperturePhoto3DActivity", "fail to operate exif." + t.getMessage());
            return this.mPhotoOrientation;
        }
    }

    private void getPhotoWidthAndHeight() {
        if (this.mFilePath == null) {
            this.mPhotoWidth = 0;
            this.mPhotoHeight = 0;
            return;
        }
        Options option = new Options();
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(this.mFilePath, option);
        if (this.mPhotoOrientation == 90 || this.mPhotoOrientation == 270) {
            this.mPhotoWidth = option.outHeight;
            this.mPhotoHeight = option.outWidth;
        } else {
            this.mPhotoWidth = option.outWidth;
            this.mPhotoHeight = option.outHeight;
        }
        this.mRatio = ((float) this.mPhotoWidth) / ((float) this.mPhotoHeight);
    }

    private void get3DViewWidthAndHeight() {
        if (this.mPhotoWidth == 0 || this.mPhotoHeight == 0) {
            this.mViewWidth = 0;
            this.mViewHeight = 0;
        }
        int screenWidth = this.mScreenWidth;
        int screenHeight = this.mScreenHeight;
        float screenRatio = ((float) screenWidth) / ((float) screenHeight);
        if (Float.compare(screenRatio, this.mRatio) == 0) {
            this.mViewWidth = screenWidth;
            this.mViewHeight = screenHeight;
        } else if (Float.compare(screenRatio, this.mRatio) > 0) {
            this.mViewHeight = screenHeight;
            this.mViewWidth = (int) (((float) this.mViewHeight) * this.mRatio);
        } else {
            this.mViewWidth = screenWidth;
            this.mViewHeight = (int) (((float) this.mViewWidth) / this.mRatio);
        }
    }

    private void getScreenSize() {
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) getSystemService("window")).getDefaultDisplay().getRealMetrics(metrics);
        this.mScreenWidth = metrics.widthPixels;
        this.mScreenHeight = metrics.heightPixels;
    }

    private int getOrientation() {
        return ((WindowManager) getSystemService("window")).getDefaultDisplay().getRotation();
    }
}
