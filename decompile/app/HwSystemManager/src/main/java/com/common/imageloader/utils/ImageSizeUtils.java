package com.common.imageloader.utils;

import android.opengl.GLES10;
import com.common.imageloader.core.DisplayImageOptions;
import com.common.imageloader.core.assist.ImageSize;
import com.common.imageloader.core.assist.ViewScaleType;
import com.common.imageloader.core.imageaware.ImageAware;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.spacecleanner.view.SquareFrameLayout;

public final class ImageSizeUtils {
    private static final /* synthetic */ int[] -com-common-imageloader-core-assist-ViewScaleTypeSwitchesValues = null;
    private static final int DEFAULT_MAX_BITMAP_DIMENSION = 2048;
    private static ImageSize maxBitmapSize;

    private static /* synthetic */ int[] -getcom-common-imageloader-core-assist-ViewScaleTypeSwitchesValues() {
        if (-com-common-imageloader-core-assist-ViewScaleTypeSwitchesValues != null) {
            return -com-common-imageloader-core-assist-ViewScaleTypeSwitchesValues;
        }
        int[] iArr = new int[ViewScaleType.values().length];
        try {
            iArr[ViewScaleType.CROP.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ViewScaleType.FIT_INSIDE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        -com-common-imageloader-core-assist-ViewScaleTypeSwitchesValues = iArr;
        return iArr;
    }

    static {
        int[] maxTextureSize = new int[1];
        GLES10.glGetIntegerv(3379, maxTextureSize, 0);
        int maxBitmapDimension = Math.max(maxTextureSize[0], 2048);
        maxBitmapSize = new ImageSize(maxBitmapDimension, maxBitmapDimension);
    }

    private ImageSizeUtils() {
    }

    public static ImageSize defineTargetSizeForView(ImageAware imageAware, ImageSize maxImageSize, DisplayImageOptions options) {
        int width = imageAware.getWidth();
        if (options.isSquarelayout()) {
            width = SquareFrameLayout.getSquareWidth();
        } else if (width <= 0) {
            width = maxImageSize.getWidth();
        }
        int height = imageAware.getHeight();
        if (options.isSquarelayout()) {
            height = SquareFrameLayout.getSquareWidth();
        } else if (height <= 0) {
            height = maxImageSize.getHeight();
        }
        return new ImageSize(width, height);
    }

    public static int computeImageSampleSize(ImageSize srcSize, ImageSize targetSize, ViewScaleType viewScaleType, boolean powerOf2Scale) {
        int srcWidth = srcSize.getWidth();
        int srcHeight = srcSize.getHeight();
        int targetWidth = targetSize.getWidth();
        int targetHeight = targetSize.getHeight();
        int scale = 1;
        int halfWidth;
        int halfHeight;
        switch (-getcom-common-imageloader-core-assist-ViewScaleTypeSwitchesValues()[viewScaleType.ordinal()]) {
            case 1:
                if (!powerOf2Scale) {
                    scale = Math.min(srcWidth / targetWidth, srcHeight / targetHeight);
                    break;
                }
                halfWidth = srcWidth / 2;
                halfHeight = srcHeight / 2;
                while (halfWidth / scale > targetWidth && halfHeight / scale > targetHeight) {
                    scale *= 2;
                }
                break;
            case 2:
                if (!powerOf2Scale) {
                    scale = Math.max(srcWidth / targetWidth, srcHeight / targetHeight);
                    break;
                }
                halfWidth = srcWidth / 2;
                halfHeight = srcHeight / 2;
                while (true) {
                    if (halfWidth / scale <= targetWidth && halfHeight / scale <= targetHeight) {
                        break;
                    }
                    scale *= 2;
                }
                break;
        }
        if (scale < 1) {
            scale = 1;
        }
        return considerMaxTextureSize(srcWidth, srcHeight, scale, powerOf2Scale);
    }

    private static int considerMaxTextureSize(int srcWidth, int srcHeight, int scale, boolean powerOf2) {
        int maxWidth = maxBitmapSize.getWidth();
        int maxHeight = maxBitmapSize.getHeight();
        while (true) {
            if (srcWidth / scale <= maxWidth && srcHeight / scale <= maxHeight) {
                return scale;
            }
            if (powerOf2) {
                scale *= 2;
            } else {
                scale++;
            }
        }
    }

    public static int computeMinImageSampleSize(ImageSize srcSize) {
        int srcWidth = srcSize.getWidth();
        int srcHeight = srcSize.getHeight();
        return Math.max((int) Math.ceil((double) (((float) srcWidth) / ((float) maxBitmapSize.getWidth()))), (int) Math.ceil((double) (((float) srcHeight) / ((float) maxBitmapSize.getHeight()))));
    }

    public static float computeImageScale(ImageSize srcSize, ImageSize targetSize, ViewScaleType viewScaleType, boolean stretch) {
        int destWidth;
        int srcWidth = srcSize.getWidth();
        int srcHeight = srcSize.getHeight();
        int targetWidth = targetSize.getWidth();
        int targetHeight = targetSize.getHeight();
        float widthScale = ((float) srcWidth) / ((float) targetWidth);
        float heightScale = ((float) srcHeight) / ((float) targetHeight);
        int destHeight;
        if ((viewScaleType != ViewScaleType.FIT_INSIDE || widthScale < heightScale) && (viewScaleType != ViewScaleType.CROP || widthScale >= heightScale)) {
            destWidth = (int) (((float) srcWidth) / heightScale);
            destHeight = targetHeight;
        } else {
            destWidth = targetWidth;
            destHeight = (int) (((float) srcHeight) / widthScale);
        }
        if (stretch || destWidth >= srcWidth || destHeight >= srcHeight) {
            if (!stretch || destWidth == srcWidth || destHeight == srcHeight) {
                return Utility.ALPHA_MAX;
            }
        }
        return ((float) destWidth) / ((float) srcWidth);
    }
}
