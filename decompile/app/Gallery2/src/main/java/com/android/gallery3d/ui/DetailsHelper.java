package com.android.gallery3d.ui;

import android.content.Context;
import android.view.View.MeasureSpec;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.MediaDetails;
import com.android.gallery3d.ui.DetailsAddressResolver.AddressResolvingListener;
import com.fyusion.sdk.viewer.internal.request.target.Target;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class DetailsHelper {
    private static final Object LOCK = new Object();
    private static DetailsAddressResolver sAddressResolver;
    private DetailsViewContainer mContainer;

    public interface CloseListener {
        void onClose();
    }

    public interface DetailsSource {
        MediaDetails getDetails();

        int setIndex();
    }

    public interface DetailsViewContainer {
        void hide();

        void reloadDetails();

        void setCloseListener(CloseListener closeListener);

        void show();
    }

    public DetailsHelper(GalleryContext activity, GLView rootPane, DetailsSource source) {
        this.mContainer = new DialogDetailsView(activity, source);
    }

    public DetailsHelper(GalleryContext activity, GLView rootPane, DetailsSource source, DetailLayout view) {
        this.mContainer = new DialogDetailsView(activity, source, view);
    }

    public void layout(int left, int top, int right, int bottom) {
        if (this.mContainer instanceof GLView) {
            GLView view = this.mContainer;
            view.measure(0, MeasureSpec.makeMeasureSpec(bottom - top, Target.SIZE_ORIGINAL));
            view.layout(0, top, view.getMeasuredWidth(), view.getMeasuredHeight() + top);
        }
    }

    public void reloadDetails() {
        this.mContainer.reloadDetails();
    }

    public void setCloseListener(CloseListener listener) {
        this.mContainer.setCloseListener(listener);
    }

    public static String resolveAddress(GalleryContext activity, double[] latlng, AddressResolvingListener listener, boolean resolveAllInfo) {
        return resolveAddress(activity, latlng, listener, resolveAllInfo, false);
    }

    public static String resolveAddress(GalleryContext activity, double[] latlng, AddressResolvingListener listener, boolean resolveAllInfo, boolean isResponseOnUIThread) {
        String resolveAddress;
        synchronized (LOCK) {
            if (sAddressResolver == null) {
                sAddressResolver = new DetailsAddressResolver(activity);
            } else {
                sAddressResolver.cancel(listener);
            }
            resolveAddress = sAddressResolver.resolveAddress(latlng, listener, resolveAllInfo, isResponseOnUIThread);
        }
        return resolveAddress;
    }

    public static void pause() {
        synchronized (LOCK) {
            if (sAddressResolver != null) {
                sAddressResolver.pause();
            }
        }
    }

    public static void cancel(AddressResolvingListener listener) {
        synchronized (LOCK) {
            if (sAddressResolver != null) {
                sAddressResolver.cancel(listener);
            }
        }
    }

    public void show() {
        this.mContainer.show();
    }

    public void hide() {
        this.mContainer.hide();
    }

    public static String getDetailsShortName(Context context, int key) {
        switch (key) {
            case 101:
                return context.getString(R.string.exposure_time_short);
            case 102:
                return context.getString(R.string.iso_short);
            case OfflineMapStatus.EXCEPTION_SDCARD /*103*/:
                return context.getString(R.string.aperture_short);
            case 104:
                return context.getString(R.string.aperture_short);
            case 109:
                return context.getString(R.string.exposure_bias_time);
            case 112:
                return context.getString(R.string.focus_model);
            default:
                return null;
        }
    }

    public static String getDetailsName(Context context, int key) {
        switch (key) {
            case 1:
                return context.getString(R.string.title);
            case 2:
                return context.getString(R.string.model);
            case 3:
                return context.getString(R.string.time);
            case 4:
                return context.getString(R.string.location);
            case 5:
            case 300:
            case 65535:
                return context.getString(R.string.file_size_abbreviate);
            case 6:
                return context.getString(R.string.width);
            case 7:
                return context.getString(R.string.height);
            case 8:
                return context.getString(R.string.duration);
            case 9:
                return context.getString(R.string.mimetype);
            case 101:
                return context.getString(R.string.exposure_time_abbreviate);
            case 102:
                return context.getString(R.string.iso);
            case OfflineMapStatus.EXCEPTION_SDCARD /*103*/:
                return context.getString(R.string.aperture);
            case 104:
                return context.getString(R.string.aperture);
            case 105:
            case 106:
                return context.getString(R.string.focal_length);
            case 107:
                return context.getString(R.string.white_balance);
            case 108:
                return context.getString(R.string.flash);
            case 109:
                return context.getString(R.string.exposure_bias_time);
            case 150:
                return context.getString(R.string.number);
            case 151:
                return context.getString(R.string.drm_ro_forward);
            case 152:
                return context.getString(R.string.drm_ro_license_num);
            case 153:
                return context.getString(R.string.drm_ro_license_status);
            case 154:
                return context.getString(R.string.drm_ro_operation_type);
            case SmsCheckResult.ESCT_200 /*200*/:
                return context.getString(R.string.path);
            case 998:
                return context.getString(R.string.orientation);
            case 999:
                return context.getString(R.string.maker);
            default:
                return "Unknown key" + key;
        }
    }
}
