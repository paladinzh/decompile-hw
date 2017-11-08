package com.android.mms.model.control;

import com.android.mms.MmsConfig;
import com.android.mms.model.ImageModel;

public class ImageModelControl {
    public boolean isHeightRestrictedConfig(ImageModel imageModel) {
        boolean z = false;
        if (imageModel == null) {
            return false;
        }
        if (imageModel.getHeight() <= MmsConfig.getMaxRestrictedImageHeight()) {
            z = true;
        }
        return z;
    }

    public boolean isWidthRestrictedConfig(ImageModel imageModel) {
        boolean z = false;
        if (imageModel == null) {
            return false;
        }
        if (imageModel.getWidth() <= MmsConfig.getMaxRestrictedImageWidth()) {
            z = true;
        }
        return z;
    }

    public boolean isWidthLTRestrictedConfig(ImageModel imageModel) {
        boolean z = false;
        if (imageModel == null) {
            return false;
        }
        if (imageModel.getWidth() <= MmsConfig.getMaxRestrictedImageHeight()) {
            z = true;
        }
        return z;
    }

    public boolean isHeightLTRestrictedConfig(ImageModel imageModel) {
        boolean z = false;
        if (imageModel == null) {
            return false;
        }
        if (imageModel.getHeight() <= MmsConfig.getMaxRestrictedImageWidth()) {
            z = true;
        }
        return z;
    }

    public int addSize(ImageModel imageModel, int addSize) {
        if (imageModel == null) {
            return addSize;
        }
        return imageModel.getMediaSize() + addSize;
    }
}
