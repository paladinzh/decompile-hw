package android.content.res;

import android.bluetooth.BluetoothClass.Device;
import android.content.pm.ApplicationInfo;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.camera2.legacy.LegacyCameraDevice;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.WindowManager.LayoutParams;

public class CompatibilityInfo implements Parcelable {
    private static final int ALWAYS_NEEDS_COMPAT = 2;
    public static final Creator<CompatibilityInfo> CREATOR = new Creator<CompatibilityInfo>() {
        public CompatibilityInfo createFromParcel(Parcel source) {
            return new CompatibilityInfo(source);
        }

        public CompatibilityInfo[] newArray(int size) {
            return new CompatibilityInfo[size];
        }
    };
    static final boolean DEBUG = false;
    public static final CompatibilityInfo DEFAULT_COMPATIBILITY_INFO = new CompatibilityInfo() {
    };
    public static final int DEFAULT_NORMAL_SHORT_DIMENSION = 320;
    public static final float MAXIMUM_ASPECT_RATIO = 1.7791667f;
    private static final int NEEDS_SCREEN_COMPAT = 8;
    private static final int NEVER_NEEDS_COMPAT = 4;
    public static final int SCALE_FORCE = 1;
    public static final int SCALE_GL = 1;
    public static final int SCALE_NATIVE = 8;
    public static final int SCALE_PACKAGE = 4;
    public static final int SCALE_SURFACE = 2;
    private static final int SCALING_REQUIRED = 1;
    public int appScaleOptFlags;
    public float appScaleRatio;
    public final int applicationDensity;
    public final float applicationInvertedScale;
    public final float applicationScale;
    private final int mCompatibilityFlags;

    public class Translator {
        public final float applicationInvertedScale;
        public final float applicationScale;
        private Rect mContentInsetsBuffer;
        private Region mTouchableAreaBuffer;
        private Rect mVisibleInsetsBuffer;

        Translator(float applicationScale, float applicationInvertedScale) {
            this.mContentInsetsBuffer = null;
            this.mVisibleInsetsBuffer = null;
            this.mTouchableAreaBuffer = null;
            this.applicationScale = applicationScale;
            this.applicationInvertedScale = applicationInvertedScale;
        }

        Translator(CompatibilityInfo this$0) {
            this(this$0.applicationScale, this$0.applicationInvertedScale);
        }

        public void translateRectInScreenToAppWinFrame(Rect rect) {
            rect.scale(this.applicationInvertedScale);
        }

        public void translateRegionInWindowToScreen(Region transparentRegion) {
            transparentRegion.scale(this.applicationScale);
        }

        public void translateCanvas(Canvas canvas) {
            if (this.applicationScale == 1.5f) {
                canvas.translate(0.0026143792f, 0.0026143792f);
            }
            canvas.scale(this.applicationScale, this.applicationScale);
        }

        public void translateEventInScreenToAppWindow(MotionEvent event) {
            event.scale(this.applicationInvertedScale);
        }

        public void translateWindowLayout(LayoutParams params) {
            params.scale(this.applicationScale);
        }

        public void translateRectInAppWindowToScreen(Rect rect) {
            rect.scale(this.applicationScale);
        }

        public void translateRectInScreenToAppWindow(Rect rect) {
            rect.scale(this.applicationInvertedScale);
        }

        public void translatePointInScreenToAppWindow(PointF point) {
            float scale = this.applicationInvertedScale;
            if (scale != 1.0f) {
                point.x *= scale;
                point.y *= scale;
            }
        }

        public void translateLayoutParamsInAppWindowToScreen(LayoutParams params) {
            params.scale(this.applicationScale);
        }

        public Rect getTranslatedContentInsets(Rect contentInsets) {
            if (this.mContentInsetsBuffer == null) {
                this.mContentInsetsBuffer = new Rect();
            }
            this.mContentInsetsBuffer.set(contentInsets);
            translateRectInAppWindowToScreen(this.mContentInsetsBuffer);
            return this.mContentInsetsBuffer;
        }

        public Rect getTranslatedVisibleInsets(Rect visibleInsets) {
            if (this.mVisibleInsetsBuffer == null) {
                this.mVisibleInsetsBuffer = new Rect();
            }
            this.mVisibleInsetsBuffer.set(visibleInsets);
            translateRectInAppWindowToScreen(this.mVisibleInsetsBuffer);
            return this.mVisibleInsetsBuffer;
        }

        public Region getTranslatedTouchableArea(Region touchableArea) {
            if (this.mTouchableAreaBuffer == null) {
                this.mTouchableAreaBuffer = new Region();
            }
            this.mTouchableAreaBuffer.set(touchableArea);
            this.mTouchableAreaBuffer.scale(this.applicationScale);
            return this.mTouchableAreaBuffer;
        }
    }

    public CompatibilityInfo(ApplicationInfo appInfo, int screenLayout, int sw, boolean forceCompat) {
        int compatFlags = 0;
        this.appScaleRatio = 1.0f;
        if (forceCompat) {
            this.appScaleOptFlags = 1;
        } else {
            this.appScaleOptFlags = 0;
        }
        if (appInfo.requiresSmallestWidthDp == 0 && appInfo.compatibleWidthLimitDp == 0 && appInfo.largestWidthLimitDp == 0) {
            int sizeInfo = 0;
            boolean anyResizeable = false;
            if ((appInfo.flags & 2048) != 0) {
                sizeInfo = 8;
                anyResizeable = true;
                if (!forceCompat) {
                    sizeInfo = 8 | 34;
                }
            }
            if ((appInfo.flags & 524288) != 0) {
                anyResizeable = true;
                if (!forceCompat) {
                    sizeInfo |= 34;
                }
            }
            if ((appInfo.flags & 4096) != 0) {
                anyResizeable = true;
                sizeInfo |= 2;
            }
            if (forceCompat) {
                sizeInfo &= -3;
            }
            compatFlags = 8;
            switch (screenLayout & 15) {
                case 3:
                    if ((sizeInfo & 8) != 0) {
                        compatFlags = 8 & -9;
                    }
                    if ((appInfo.flags & 2048) != 0) {
                        compatFlags |= 4;
                        break;
                    }
                    break;
                case 4:
                    if ((sizeInfo & 32) != 0) {
                        compatFlags = 8 & -9;
                    }
                    if ((appInfo.flags & 524288) != 0) {
                        compatFlags |= 4;
                        break;
                    }
                    break;
            }
            if ((268435456 & screenLayout) == 0) {
                compatFlags = (compatFlags & -9) | 4;
            } else if ((sizeInfo & 2) != 0) {
                compatFlags &= -9;
            } else if (!anyResizeable) {
                compatFlags |= 2;
            }
            if ((appInfo.flags & 8192) != 0) {
                this.applicationDensity = DisplayMetrics.DENSITY_DEVICE;
                this.applicationScale = 1.0f;
                this.applicationInvertedScale = 1.0f;
            } else {
                this.applicationDensity = 160;
                this.applicationScale = ((float) DisplayMetrics.DENSITY_DEVICE) / 160.0f;
                this.applicationInvertedScale = 1.0f / this.applicationScale;
                compatFlags |= 1;
            }
        } else {
            int required;
            int compat;
            if (appInfo.requiresSmallestWidthDp != 0) {
                required = appInfo.requiresSmallestWidthDp;
            } else {
                required = appInfo.compatibleWidthLimitDp;
            }
            if (required == 0) {
                required = appInfo.largestWidthLimitDp;
            }
            if (appInfo.compatibleWidthLimitDp != 0) {
                compat = appInfo.compatibleWidthLimitDp;
            } else {
                compat = required;
            }
            if (compat < required) {
                compat = required;
            }
            int largest = appInfo.largestWidthLimitDp;
            if (required > 320) {
                compatFlags = 4;
            } else if (largest != 0 && sw > largest) {
                compatFlags = 10;
            } else if (compat >= sw) {
                compatFlags = 4;
            } else if (forceCompat) {
                compatFlags = 8;
            }
            this.applicationDensity = DisplayMetrics.DENSITY_DEVICE;
            this.applicationScale = 1.0f;
            this.applicationInvertedScale = 1.0f;
        }
        this.mCompatibilityFlags = compatFlags;
    }

    private CompatibilityInfo(int compFlags, int dens, float scale, float invertedScale) {
        this.mCompatibilityFlags = compFlags;
        this.applicationDensity = dens;
        this.applicationScale = scale;
        this.applicationInvertedScale = invertedScale;
    }

    private CompatibilityInfo(int compFlags, int dens, float scale, float invertedScale, int flags) {
        this.mCompatibilityFlags = compFlags;
        this.applicationDensity = dens;
        this.applicationScale = scale;
        this.applicationInvertedScale = invertedScale;
        this.appScaleOptFlags = flags;
    }

    private CompatibilityInfo() {
        this(4, DisplayMetrics.DENSITY_DEVICE, 1.0f, 1.0f);
    }

    public boolean isScalingRequired() {
        return (this.mCompatibilityFlags & 1) != 0;
    }

    public boolean supportsScreen() {
        return (this.mCompatibilityFlags & 8) == 0;
    }

    public boolean neverSupportsScreen() {
        return (this.mCompatibilityFlags & 2) != 0;
    }

    public boolean alwaysSupportsScreen() {
        return (this.mCompatibilityFlags & 4) != 0;
    }

    public Translator getTranslator() {
        return isScalingRequired() ? new Translator(this) : null;
    }

    public boolean realNeedCompat() {
        return true;
    }

    public void applyToDisplayMetrics(DisplayMetrics inoutDm) {
        float invertedRatio = 1.0f;
        if (supportsScreen() || !realNeedCompat()) {
            inoutDm.widthPixels = inoutDm.noncompatWidthPixels;
            inoutDm.heightPixels = inoutDm.noncompatHeightPixels;
        } else if (this.appScaleOptFlags != 1) {
            invertedRatio = computeCompatibleScaling(inoutDm, inoutDm);
        } else if (Float.compare(inoutDm.density, inoutDm.noncompatDensity) == 0) {
            invertedRatio = computeForceCompatibleScaling(inoutDm, inoutDm);
        } else if (Float.compare(inoutDm.density, 0.0f) != 0) {
            invertedRatio = inoutDm.noncompatDensity / inoutDm.density;
        }
        if (isScalingRequired()) {
            invertedRatio = this.applicationInvertedScale;
            inoutDm.density = inoutDm.noncompatDensity * invertedRatio;
            inoutDm.densityDpi = (int) ((((float) inoutDm.noncompatDensityDpi) * invertedRatio) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
            inoutDm.scaledDensity = inoutDm.noncompatScaledDensity * invertedRatio;
            inoutDm.xdpi = inoutDm.noncompatXdpi * invertedRatio;
            inoutDm.ydpi = inoutDm.noncompatYdpi * invertedRatio;
            inoutDm.widthPixels = (int) ((((float) inoutDm.widthPixels) * invertedRatio) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
            inoutDm.heightPixels = (int) ((((float) inoutDm.heightPixels) * invertedRatio) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
        } else if (invertedRatio != 1.0f) {
            this.appScaleRatio = invertedRatio;
            invertedRatio = 1.0f / invertedRatio;
            inoutDm.density = inoutDm.noncompatDensity * invertedRatio;
            inoutDm.densityDpi = (int) ((inoutDm.density * 160.0f) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
            inoutDm.scaledDensity = inoutDm.noncompatScaledDensity * invertedRatio;
            inoutDm.xdpi = inoutDm.noncompatXdpi * invertedRatio;
            inoutDm.ydpi = inoutDm.noncompatYdpi * invertedRatio;
        }
    }

    public void applyToConfiguration(int displayDensity, Configuration inoutConfig) {
        inoutConfig.densityDpi = displayDensity;
        if (isScalingRequired()) {
            inoutConfig.densityDpi = (int) ((((float) inoutConfig.densityDpi) * this.applicationInvertedScale) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
        }
    }

    public void applyToConfigurationExt(DisplayMetrics metrics, int displayDensity, Configuration inoutConfig) {
        if (!isScalingRequired()) {
            boolean noNeedToChange = (metrics == null || inoutConfig.densityDpi == metrics.noncompatDensityDpi) ? false : true;
            if (this.appScaleOptFlags != 0 && this.appScaleRatio != 1.0f && !noNeedToChange) {
                inoutConfig.densityDpi = (int) ((((float) inoutConfig.densityDpi) * (1.0f / this.appScaleRatio)) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
            }
        }
    }

    public static float computeCompatibleScaling(DisplayMetrics dm, DisplayMetrics outDm) {
        if (dm.noncompatDensity != dm.density && dm.noncompatDensity != 0.0f && dm.density != 0.0f) {
            return 1.0f;
        }
        int shortSize;
        int longSize;
        int width = dm.noncompatWidthPixels;
        int height = dm.noncompatHeightPixels;
        if (width < height) {
            shortSize = width;
            longSize = height;
        } else {
            shortSize = height;
            longSize = width;
        }
        return computeScale(320.0f, longSize, shortSize, dm, outDm);
    }

    public static float computeForceCompatibleScaling(DisplayMetrics dm, DisplayMetrics outDm) {
        if (dm.noncompatDensity != dm.density && dm.noncompatDensity != 0.0f && dm.density != 0.0f) {
            return 1.0f;
        }
        int shortSize;
        int longSize;
        int width = dm.noncompatWidthPixels;
        int height = dm.noncompatHeightPixels;
        if (width < height) {
            shortSize = width;
            longSize = height;
        } else {
            shortSize = height;
            longSize = width;
        }
        float factor = 320.0f;
        if (longSize < Device.AUDIO_VIDEO_VIDEO_MONITOR) {
            factor = 320.0f;
        } else if (longSize <= 1200) {
            factor = 276.0f;
        } else if (longSize <= 1280) {
            factor = 288.0f;
        } else if (longSize <= LegacyCameraDevice.MAX_DIMEN_FOR_ROUNDING) {
            factor = 240.0f;
        } else if (longSize <= 2560) {
            factor = 270.0f;
        }
        return computeScale(factor, longSize, shortSize, dm, outDm);
    }

    public static float computeScale(float factor, int longSize, int shortSize, DisplayMetrics dm, DisplayMetrics outDm) {
        int newWidth;
        int newHeight;
        float scale;
        int width = dm.noncompatWidthPixels;
        int height = dm.noncompatHeightPixels;
        int newShortSize = (int) ((dm.density * factor) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
        float aspect = ((float) longSize) / ((float) shortSize);
        if (aspect > MAXIMUM_ASPECT_RATIO) {
            aspect = MAXIMUM_ASPECT_RATIO;
        }
        int newLongSize = (int) ((((float) newShortSize) * aspect) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
        if (width < height) {
            newWidth = newShortSize;
            newHeight = newLongSize;
        } else {
            newWidth = newLongSize;
            newHeight = newShortSize;
        }
        float sw = ((float) width) / ((float) newWidth);
        float sh = ((float) height) / ((float) newHeight);
        if (sw < sh) {
            scale = sw;
        } else {
            scale = sh;
        }
        if (scale < 1.0f) {
            scale = 1.0f;
        }
        if (outDm != null) {
            outDm.widthPixels = newWidth;
            outDm.heightPixels = newHeight;
        }
        return scale;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        try {
            CompatibilityInfo oc = (CompatibilityInfo) o;
            return this.mCompatibilityFlags == oc.mCompatibilityFlags && this.applicationDensity == oc.applicationDensity && this.applicationScale == oc.applicationScale && this.applicationInvertedScale == oc.applicationInvertedScale && this.appScaleOptFlags == oc.appScaleOptFlags;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("{");
        sb.append(this.applicationDensity);
        sb.append("dpi");
        if (isScalingRequired()) {
            sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            sb.append(this.applicationScale);
            sb.append("x");
        }
        if (!supportsScreen()) {
            sb.append(" resizing");
        }
        if (neverSupportsScreen()) {
            sb.append(" never-compat");
        }
        if (alwaysSupportsScreen()) {
            sb.append(" always-compat");
        }
        if (this.appScaleOptFlags != 0) {
            sb.append(" enabledAppScaleOpt");
        }
        sb.append("}");
        return sb.toString();
    }

    public int hashCode() {
        return ((((((((this.mCompatibilityFlags + 527) * 31) + this.applicationDensity) * 31) + Float.floatToIntBits(this.applicationScale)) * 31) + Float.floatToIntBits(this.applicationInvertedScale)) * 31) + this.appScaleOptFlags;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mCompatibilityFlags);
        dest.writeInt(this.applicationDensity);
        dest.writeInt(this.appScaleOptFlags);
        dest.writeFloat(this.applicationScale);
        dest.writeFloat(this.applicationInvertedScale);
    }

    public static final CompatibilityInfo makeNewCompatibilityInfo(int flags) {
        return new CompatibilityInfo(11, 160, 1.33125f, 0.75117373f, flags);
    }

    public static final CompatibilityInfo makeNewPackageCompatibilityInfo(int flags) {
        return new CompatibilityInfo(11, 160, ((float) DisplayMetrics.DENSITY_DEVICE) / 160.0f, 160.0f / ((float) DisplayMetrics.DENSITY_DEVICE), flags);
    }

    public static final CompatibilityInfo makeNoneCompatibilityInfo(int flags) {
        return new CompatibilityInfo(4, DisplayMetrics.DENSITY_DEVICE, 1.0f, 1.0f, flags);
    }

    public static final CompatibilityInfo makeCompatibilityInfo(int flags) {
        return new CompatibilityInfo(8, DisplayMetrics.DENSITY_DEVICE, 1.0f, 1.0f, flags);
    }

    private CompatibilityInfo(Parcel source) {
        this.mCompatibilityFlags = source.readInt();
        this.applicationDensity = source.readInt();
        this.appScaleOptFlags = source.readInt();
        this.applicationScale = source.readFloat();
        this.applicationInvertedScale = source.readFloat();
    }
}
