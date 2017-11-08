package com.huawei.gallery.animation;

import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.android.gallery3d.common.Utils;
import com.huawei.watermark.manager.parse.WMElement;

public class DeletePhotoAnimationFactor {
    private static final Interpolator DEFAULT_INTERPOLATOR = new DecelerateInterpolator();

    public static float getCurrentContentAlpha(float progress) {
        return (WMElement.CAMERASIZEVALUE1B1 * DEFAULT_INTERPOLATOR.getInterpolation(Utils.clamp(progress < 0.0f ? progress + WMElement.CAMERASIZEVALUE1B1 : WMElement.CAMERASIZEVALUE1B1 - progress, 0.0f, (float) WMElement.CAMERASIZEVALUE1B1))) + 0.0f;
    }

    public static float getOverlayAlpha(float progress) {
        return (GroundOverlayOptions.NO_DIMENSION * DEFAULT_INTERPOLATOR.getInterpolation(Utils.clamp(Math.abs(progress), 0.0f, (float) WMElement.CAMERASIZEVALUE1B1))) + WMElement.CAMERASIZEVALUE1B1;
    }

    public static float getOverlayScale(float progress) {
        return (-0.39999998f * DEFAULT_INTERPOLATOR.getInterpolation(Utils.clamp(Math.abs(progress), 0.0f, (float) WMElement.CAMERASIZEVALUE1B1))) + WMElement.CAMERASIZEVALUE1B1;
    }
}
