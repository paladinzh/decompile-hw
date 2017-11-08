package com.fyusion.sdk.common;

import android.util.Log;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.fyusion.sdk.viewer.internal.request.target.Target;
import java.io.Serializable;

/* compiled from: Unknown */
public class n implements Serializable {
    boolean android = false;
    int[] bounds;
    int cameraHeight;
    int cameraWidth;
    float curvature;
    private String deviceId;
    float directionX;
    float directionY;
    private int endFrame;
    private int frontCamera;
    private float gravityX;
    private float gravityY;
    boolean has_pano = false;
    boolean has_tags = false;
    boolean has_tweens = false;
    int height;
    boolean horiz;
    private int imageOrientation = Target.SIZE_ORIGINAL;
    float imuDirectionX;
    boolean imuDirectionXCached;
    float imuDirectionY;
    boolean imuDirectionYCached;
    private boolean loopClosed;
    private int loopClosedEndFrame;
    private int numProcessedFrames;
    int p;
    int rotation_mode;
    a[] slices;
    int stabilizationDataFrameOffset;
    private int startFrame;
    float swipeDirectionX;
    boolean swipeDirectionXCached;
    float swipeDirectionY;
    boolean swipeDirectionYCached;
    int thumbSlice = -1;
    private int thumbnailIndex;
    private String uniqueDeviceId;
    int width;

    /* compiled from: Unknown */
    public static class a implements Serializable {
        private int a;
        private int b;
        private int c;

        public a(int i, int i2, int i3) {
            this.a = i;
            this.b = i2;
            this.c = i3;
        }
    }

    public void addSlice(int i, int i2, int i3) {
        this.slices[i3] = new a(i, i2, i3);
    }

    public int calculateThumbSlice() {
        for (a aVar : this.slices) {
            if (this.thumbnailIndex >= aVar.a && this.thumbnailIndex <= aVar.b) {
                return aVar.c;
            }
        }
        return 0;
    }

    void computeNavigationDirections() {
        float f;
        int i = -1;
        float directionX = getDirectionX();
        float directionY = getDirectionY();
        float curvature = getCurvature();
        boolean isFromFrontCamera = isFromFrontCamera();
        if (Math.abs(directionX) >= Math.abs(directionY)) {
            if (directionX > 0.0f) {
                i = 1;
            }
            directionX = (float) i;
            f = 0.0f;
        } else {
            if (directionY > 0.0f) {
                i = 1;
            }
            f = (float) i;
            directionX = 0.0f;
        }
        this.imuDirectionX = directionX;
        this.imuDirectionY = f;
        if (curvature > 0.0f) {
            directionX = -directionX;
        }
        this.swipeDirectionX = directionX;
        if (curvature > 0.0f) {
            f = -f;
        }
        this.swipeDirectionY = f;
        if (isFromFrontCamera) {
            this.swipeDirectionY *= GroundOverlayOptions.NO_DIMENSION;
            this.imuDirectionY *= GroundOverlayOptions.NO_DIMENSION;
        }
        this.imuDirectionXCached = true;
        this.imuDirectionYCached = true;
        this.swipeDirectionXCached = true;
        this.swipeDirectionYCached = true;
    }

    public int getCameraHeight() {
        return this.cameraHeight;
    }

    public int getCameraWidth() {
        return this.cameraWidth;
    }

    public float getCurvature() {
        return this.curvature;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public float getDirectionX() {
        return this.directionX;
    }

    public float getDirectionY() {
        return this.directionY;
    }

    public float getEffectiveGravityX() {
        return !isFromFrontCamera() ? this.gravityX : this.gravityX * GroundOverlayOptions.NO_DIMENSION;
    }

    public int getEndFrame() {
        return this.endFrame;
    }

    public int getFrontCamera() {
        return this.frontCamera;
    }

    public float getGravityX() {
        return this.gravityX;
    }

    public float getGravityY() {
        return this.gravityY;
    }

    public int getHeight() {
        return this.height;
    }

    public boolean getHoriz() {
        return this.horiz;
    }

    public float getIMUDirectionX() {
        if (this.imuDirectionXCached) {
            return this.imuDirectionX;
        }
        computeNavigationDirections();
        this.imuDirectionXCached = true;
        return this.imuDirectionX;
    }

    public float getIMUDirectionY() {
        if (this.imuDirectionYCached) {
            return this.imuDirectionY;
        }
        computeNavigationDirections();
        this.imuDirectionYCached = true;
        return this.imuDirectionY;
    }

    public int getLoopClosedEndFrame() {
        return this.loopClosedEndFrame;
    }

    public int getNoSlices() {
        return this.slices.length;
    }

    public int getNumProcessedFrames() {
        return this.numProcessedFrames;
    }

    public double getRotationAngleBasedOnGravityX() {
        return this.gravityX > 0.0f ? -1.5707963267948966d : 1.5707963267948966d;
    }

    public int getRotationMode() {
        return this.rotation_mode;
    }

    public int getSliceEndFrame(int i) {
        return i >= this.slices.length ? -1 : this.slices[i].b;
    }

    public int getSliceFrames(int i) {
        return (this.slices[i].b - this.slices[i].a) + 1;
    }

    public int getSliceStartFrame(int i) {
        if (i < this.slices.length) {
            return this.slices[i].a;
        }
        Log.e("Magic", "getSliceStartFrame returns -1, highResolutionSlices.length " + this.slices.length);
        return -1;
    }

    public int getStabilizationDataFrameOffset() {
        return this.stabilizationDataFrameOffset;
    }

    public int getStartFrame() {
        return this.startFrame;
    }

    public float getSwipeDirectionX() {
        if (this.swipeDirectionXCached) {
            return this.swipeDirectionX;
        }
        computeNavigationDirections();
        this.swipeDirectionXCached = true;
        return this.swipeDirectionX;
    }

    public float getSwipeDirectionY() {
        if (this.swipeDirectionYCached) {
            return this.swipeDirectionY;
        }
        computeNavigationDirections();
        this.swipeDirectionYCached = true;
        return this.swipeDirectionY;
    }

    public int getThumbSlice() {
        if (this.thumbSlice >= 0) {
            return this.thumbSlice;
        }
        this.thumbSlice = calculateThumbSlice();
        return this.thumbSlice;
    }

    public int getThumbSliceFrames() {
        return (this.slices[getThumbSlice()].b - this.slices[getThumbSlice()].a) + 1;
    }

    public int getThumbnailIndex() {
        return this.thumbnailIndex;
    }

    public String getUniqueDeviceId() {
        return this.uniqueDeviceId;
    }

    public int getWidth() {
        return this.width;
    }

    public boolean hasTweens() {
        return this.has_tweens;
    }

    public boolean isAndroid() {
        return this.android;
    }

    public boolean isConvex() {
        return this.curvature < 0.0f;
    }

    public boolean isFromFrontCamera() {
        return this.frontCamera == 1;
    }

    public boolean isLoopClosed() {
        return this.loopClosed;
    }

    public boolean isPortrait() {
        return Math.abs(this.gravityX) < Math.abs(this.gravityY);
    }

    public void setAndroid(boolean z) {
        this.android = z;
    }

    public void setBounds(int[] iArr) {
        this.bounds = iArr;
        if (iArr.length > 1) {
            this.startFrame = iArr[0];
            this.endFrame = iArr[1];
        }
    }

    public void setCameraHeight(int i) {
        this.cameraHeight = i;
    }

    public void setCameraWidth(int i) {
        this.cameraWidth = i;
    }

    public void setCurvature(float f) {
        this.curvature = f;
    }

    public void setDeviceId(String str) {
        this.deviceId = str;
    }

    public void setDirectionX(float f) {
        this.directionX = f;
    }

    public void setDirectionY(float f) {
        this.directionY = f;
    }

    public void setEndFrame(int i) {
        this.endFrame = i;
    }

    public void setFlags(int i) {
        boolean z = false;
        this.has_tags = (i & 1) != 0;
        this.has_tweens = (i & 2) != 0;
        this.loopClosed = (i & 4) != 0;
        if ((i & 8) != 0) {
            z = true;
        }
        this.has_pano = z;
    }

    public void setFrontCamera(int i) {
        this.frontCamera = i;
    }

    public void setGravityX(float f) {
        this.gravityX = f;
    }

    public void setGravityY(float f) {
        this.gravityY = f;
    }

    public void setHeight(int i) {
        this.height = i;
    }

    public void setHoriz(boolean z) {
        this.horiz = z;
    }

    public void setLoopClosed(boolean z) {
        this.loopClosed = z;
    }

    public void setLoopClosedEndFrame(int i) {
        this.loopClosedEndFrame = i;
    }

    public void setNumProcessedFrames(int i) {
        this.numProcessedFrames = i;
    }

    public void setRotation_mode(int i) {
        this.rotation_mode = i;
    }

    public void setSlicesLength(int i) {
        this.slices = new a[i];
    }

    public void setStabilizationDataFrameOffset(int i) {
        this.stabilizationDataFrameOffset = i;
    }

    public void setStartFrame(int i) {
        this.startFrame = i;
    }

    public void setThumbSlice(int i) {
        this.thumbSlice = i;
    }

    public void setThumbnailIndex(int i) {
        this.thumbnailIndex = i;
    }

    public void setUniqueDeviceId(String str) {
        this.uniqueDeviceId = str;
    }

    public void setWidth(int i) {
        this.width = i;
    }
}
