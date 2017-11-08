package com.huawei.watermark.manager.parse.util;

import android.content.Context;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.huawei.watermark.controller.callback.PressureValueChangeListener;
import com.huawei.watermark.controller.callback.SensorProcessorListener;
import com.huawei.watermark.manager.parse.SensorProcessor;
import com.huawei.watermark.manager.parse.util.WMWeatherHelper.ReferencePressureReceiveListener;
import com.huawei.watermark.wmdata.wmlogicdata.WMLogicData;
import com.huawei.watermark.wmutil.WMAltitudeUtil;
import com.huawei.watermark.wmutil.WMCollectionUtil;
import java.util.ArrayList;
import java.util.List;

public class WMAltitudeService {
    private List<AltitudeUpdateCallback> mAltitudeUpdateCallbacks = new ArrayList();
    private boolean mCanStart;
    private Context mContext;
    private float mCurrentPressure = GroundOverlayOptions.NO_DIMENSION;
    private PressureValueChangeListener mPressureValueChangeListener = new PressureValueChangeListener() {
        public void onPressureValueChanged(float pressureValue) {
            WMAltitudeService.this.mCurrentPressure = pressureValue;
            WMAltitudeService.this.reportCallbacks();
        }
    };
    private float mReferencePressure = GroundOverlayOptions.NO_DIMENSION;
    private ReferencePressureReceiveListener mReferencePressureReceiveListener = new ReferencePressureReceiveListener() {
        public void onReferencePressureReceived(float referencePressure) {
            WMAltitudeService.this.mReferencePressure = referencePressure;
            WMAltitudeService.this.reportCallbacks();
        }
    };
    private SensorProcessorListener mSensorProcessorListener;
    private Object mSyncObj = new Object();
    private WMWeatherHelper mWMWeatherHelper;

    public interface AltitudeUpdateCallback {
        void onAltitudeReport(String str);
    }

    public WMAltitudeService(Context mContext, WMWeatherHelper altitudeHelper) {
        this.mContext = mContext;
        this.mSensorProcessorListener = new SensorProcessor(mContext);
        this.mWMWeatherHelper = altitudeHelper;
    }

    public void start() {
        this.mCanStart = true;
        this.mSensorProcessorListener.registerSensor();
        this.mSensorProcessorListener.setPressureValueChangeListener(this.mPressureValueChangeListener);
        this.mWMWeatherHelper.setReferencePressureReceiveListener(this.mReferencePressureReceiveListener);
        reportCallbacks();
    }

    public void release() {
        this.mCanStart = false;
        this.mSensorProcessorListener.unRegisterSensor();
        this.mSensorProcessorListener.setPressureValueChangeListener(null);
        this.mWMWeatherHelper.setReferencePressureReceiveListener(null);
        synchronized (this.mSyncObj) {
            if (!WMCollectionUtil.isEmptyCollection(this.mAltitudeUpdateCallbacks)) {
                this.mAltitudeUpdateCallbacks.clear();
            }
        }
        this.mReferencePressure = GroundOverlayOptions.NO_DIMENSION;
        this.mCurrentPressure = GroundOverlayOptions.NO_DIMENSION;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addAltitudeUpdateCallback(AltitudeUpdateCallback altitudeUpdateCallback) {
        synchronized (this.mSyncObj) {
            if (WMCollectionUtil.isEmptyCollection(this.mAltitudeUpdateCallbacks)) {
                this.mAltitudeUpdateCallbacks = new ArrayList();
            }
            if (this.mAltitudeUpdateCallbacks == null || this.mAltitudeUpdateCallbacks.contains(altitudeUpdateCallback)) {
            } else {
                this.mAltitudeUpdateCallbacks.add(altitudeUpdateCallback);
                reportCallback(altitudeUpdateCallback);
            }
        }
    }

    private void reportCallback(AltitudeUpdateCallback altitudeUpdateCallback) {
        if (this.mCanStart) {
            altitudeUpdateCallback.onAltitudeReport(getAltitude());
        }
    }

    private void reportCallbacks() {
        if (this.mCanStart) {
            String altitude = getAltitude();
            synchronized (this.mSyncObj) {
                if (altitude != null) {
                    if (!altitude.isEmpty()) {
                        for (AltitudeUpdateCallback altitudeUpdateCallback : this.mAltitudeUpdateCallbacks) {
                            altitudeUpdateCallback.onAltitudeReport(altitude);
                        }
                    }
                }
            }
        }
    }

    private String getAltitude() {
        String altitude = null;
        if (this.mReferencePressure < 0.0f || this.mCurrentPressure < 0.0f) {
            return WMLogicData.getInstance(this.mContext).getAltitudeTextWithKeyname("ALTITUDE_TAG");
        }
        try {
            return String.valueOf(WMAltitudeUtil.getAltitude(this.mReferencePressure, this.mCurrentPressure));
        } catch (Exception e) {
            return altitude;
        }
    }
}
