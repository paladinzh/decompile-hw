package com.huawei.watermark.manager.parse.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.ConditionVariable;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.amap.api.maps.model.WeightedLatLng;
import com.huawei.android.totemweather.aidl.IRequestCallBack;
import com.huawei.android.totemweather.aidl.IRequestCityWeather;
import com.huawei.android.totemweather.aidl.IRequestCityWeather.Stub;
import com.huawei.android.totemweather.aidl.RequestData;
import org.json.JSONArray;
import org.json.JSONObject;

public class WMWeatherHelper {
    private RequestData mAltitudeRequestData;
    private AltitudeUpdateThread mAltitudeUpdateThread;
    private boolean mCanRequest = false;
    private Context mContext;
    private double mLatitude = -100000.0d;
    private LocationEventListener mLocationEventListener = new LocationEventListener() {
        public void onLocationSuccess(double longitude, double latitude) {
            WMWeatherHelper.this.mLatitude = latitude;
            WMWeatherHelper.this.mLongitude = longitude;
            if (WMWeatherHelper.this.mCanRequest && WMWeatherHelper.this.mContext != null) {
                ((Activity) WMWeatherHelper.this.mContext).runOnUiThread(WMWeatherHelper.this.requestRunnable);
            }
        }

        public void onLocationFailed() {
        }
    };
    private double mLongitude = -100000.0d;
    private ReferencePressureReceiveListener mReferencePressureReceiveListener;
    private IRequestCityWeather mRequestWeather;
    private ServiceConnection mWeatherConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d("Watermark_WMWeatherHelper", "onServiceConnected");
            try {
                WMWeatherHelper.this.mRequestWeather = Stub.asInterface(service);
                if (WMWeatherHelper.this.mRequestWeather != null) {
                    WMWeatherHelper.this.mRequestWeather.registerCallBack(WMWeatherHelper.this.mWeatherRequestCallBack, WMWeatherHelper.this.mContext.getPackageName());
                } else {
                    Log.w("Watermark_WMWeatherHelper", "mRequestWeather is null");
                }
                if (WMWeatherHelper.this.mCanRequest) {
                    WMWeatherHelper.this.request();
                }
            } catch (RemoteException e) {
                Log.e("Watermark_WMWeatherHelper", "onServiceConnected exception:");
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            WMWeatherHelper.this.requestRelease();
        }
    };
    private WeatherDateReceiveListener mWeatherDateReceiveListener;
    private IRequestCallBack mWeatherRequestCallBack = new IRequestCallBack.Stub() {
        public void onRequestResult(String weatherJsonData, RequestData requestData) throws RemoteException {
            if ("altitude".equalsIgnoreCase(requestData.getmRequesetFlag())) {
                WMWeatherHelper.this.handlerAltitudeData(weatherJsonData);
            } else {
                WMWeatherHelper.this.handlerWeatherData(weatherJsonData);
            }
        }
    };
    private RequestData mWeatherRequestData;
    private Runnable requestRunnable = new Runnable() {
        public void run() {
            WMWeatherHelper.this.request();
        }
    };

    public interface ReferencePressureReceiveListener {
        void onReferencePressureReceived(float f);
    }

    private class AltitudeUpdateThread extends Thread {
        private boolean mCancel;
        private ConditionVariable mSig = new ConditionVariable();

        public AltitudeUpdateThread() {
            setName("AltitudeUpdateThread");
        }

        public void run() {
            if (this.mCancel) {
                WMWeatherHelper.this.mAltitudeUpdateThread = null;
            } else if (WMWeatherHelper.this.mContext == null) {
                WMWeatherHelper.this.mAltitudeUpdateThread = null;
            } else {
                WMWeatherHelper.this.bindWeatherService();
                WMWeatherHelper.this.mAltitudeUpdateThread = null;
            }
        }

        public void cancel() {
            this.mCancel = true;
            this.mSig.open();
        }
    }

    public interface WeatherDateReceiveListener {
        void onWeatherDateReceived(String str);
    }

    public WMWeatherHelper(Context context) {
        this.mContext = context;
    }

    public void start() {
        startAltitudeUpdateThread();
        this.mCanRequest = true;
    }

    public void release() {
        this.mCanRequest = false;
        this.mLatitude = -100000.0d;
        this.mLongitude = -100000.0d;
        if (this.mAltitudeUpdateThread != null) {
            this.mAltitudeUpdateThread.cancel();
        }
        this.mAltitudeUpdateThread = null;
        try {
            Log.d("Watermark_WMWeatherHelper", "unbindWeatherService");
            this.mContext.unbindService(this.mWeatherConnection);
        } catch (Exception e) {
            Log.e("Watermark_WMWeatherHelper", "unbindWeatherService exception");
        }
        requestRelease();
    }

    public void startAltitudeUpdateThread() {
        if (this.mAltitudeUpdateThread == null) {
            Log.d("Watermark_WMWeatherHelper", "startAltitudeUpdateThread");
            this.mAltitudeUpdateThread = new AltitudeUpdateThread();
            this.mAltitudeUpdateThread.start();
        }
    }

    private void bindWeatherService() {
        try {
            Intent weatherIntent = new Intent();
            weatherIntent.setAction("com.huawei.totemweather.action.THIRD_REQUEST_WEATHER");
            weatherIntent.setPackage("com.huawei.android.totemweather");
            Log.d("Watermark_WMWeatherHelper", "bindWeatherService");
            this.mContext.bindService(weatherIntent, this.mWeatherConnection, 1);
        } catch (RuntimeException e) {
            Log.d("Watermark_WMWeatherHelper", "bindWeatherService exception !");
        }
    }

    public void setLatitudeAndLongitude(double latitude, double longitude) {
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        if (this.mCanRequest) {
            request();
        }
    }

    public LocationEventListener getLocationEventListener() {
        return this.mLocationEventListener;
    }

    private void request() {
        try {
            if (this.mRequestWeather == null || this.mLatitude == -100000.0d || this.mLongitude == -100000.0d) {
                Log.w("Watermark_WMWeatherHelper", "can't requese because mRequestWeather is null");
                return;
            }
            Log.w("Watermark_WMWeatherHelper", "request");
            if (this.mAltitudeRequestData == null) {
                this.mAltitudeRequestData = new RequestData(this.mContext, this.mLatitude, this.mLongitude);
                this.mAltitudeRequestData.setmAllDay(false);
                this.mAltitudeRequestData.setmRequesetFlag("altitude");
            }
            this.mRequestWeather.requestWeatherWithLocation(this.mAltitudeRequestData, 1, 1);
            if (this.mWeatherRequestData == null) {
                this.mWeatherRequestData = new RequestData(this.mContext, this.mLatitude, this.mLongitude);
                this.mWeatherRequestData.setmAllDay(true);
                this.mWeatherRequestData.setmRequesetFlag(ParseJson.KEY_WEATHER);
            }
            this.mRequestWeather.requestWeatherByLocation(this.mWeatherRequestData);
        } catch (RemoteException e) {
            Log.e("Watermark_WMWeatherHelper", "request weather exception !");
        }
    }

    private void requestRelease() {
        Log.w("Watermark_WMWeatherHelper", "requestRelease");
        try {
            if (this.mRequestWeather != null) {
                this.mRequestWeather.unregisterCallBack(this.mWeatherRequestCallBack, this.mContext.getPackageName());
            } else {
                Log.d("Watermark_WMWeatherHelper", "can't unregisterCallBack because mRequestWeather is null");
            }
            this.mRequestWeather = null;
            this.mAltitudeRequestData = null;
        } catch (RemoteException e) {
            Log.e("Watermark_WMWeatherHelper", "unregisterCallBack exception");
            this.mRequestWeather = null;
            this.mAltitudeRequestData = null;
        } catch (Throwable th) {
            this.mRequestWeather = null;
            this.mAltitudeRequestData = null;
            this.mWeatherRequestData = null;
        }
        this.mWeatherRequestData = null;
    }

    private void handlerAltitudeData(String weatherJsonData) {
        if (weatherJsonData != null) {
            Double pressureDouble = parserWeatherPressure(weatherJsonData);
            if (pressureDouble != null) {
                float referencePressure = pressureDouble.floatValue();
                Log.d("Watermark_WMWeatherHelper", "mReferencePressure:" + referencePressure);
                if (this.mReferencePressureReceiveListener != null) {
                    this.mReferencePressureReceiveListener.onReferencePressureReceived(referencePressure);
                    return;
                }
                return;
            }
            Log.d("Watermark_WMWeatherHelper", "pressureDouble is null");
            return;
        }
        Log.d("Watermark_WMWeatherHelper", "JsonData is null");
    }

    private void handlerWeatherData(String weatherJsonData) {
        if (this.mWeatherDateReceiveListener != null) {
            this.mWeatherDateReceiveListener.onWeatherDateReceived(weatherJsonData);
        }
    }

    public void setReferencePressureReceiveListener(ReferencePressureReceiveListener listener) {
        this.mReferencePressureReceiveListener = listener;
    }

    public void setWeatherDateReceiveListener(WeatherDateReceiveListener listener) {
        this.mWeatherDateReceiveListener = listener;
    }

    private Double parserWeatherPressure(String str) {
        if (str != null) {
            try {
                if (!"".equals(str)) {
                    JSONArray weatherArray = new JSONObject(str).getJSONArray(ParseJson.KEY_WEATHER);
                    if (weatherArray == null || weatherArray.length() <= 0) {
                        return null;
                    }
                    String pressureStr = null;
                    for (int i = 0; i < weatherArray.length(); i++) {
                        JSONObject jsonObject = (JSONObject) weatherArray.get(i);
                        if (jsonObject != null) {
                            pressureStr = jsonObject.optString("Pressure");
                            if (!(pressureStr == null || "".equals(pressureStr))) {
                                break;
                            }
                        }
                    }
                    if (pressureStr == null || "".equals(pressureStr)) {
                        return null;
                    }
                    double parseDouble;
                    if (pressureStr.contains(".")) {
                        parseDouble = Double.parseDouble(pressureStr);
                    } else {
                        parseDouble = ((double) Integer.valueOf(pressureStr).intValue()) * WeightedLatLng.DEFAULT_INTENSITY;
                    }
                    Double pressure = Double.valueOf(parseDouble);
                    if (Math.abs(pressure.doubleValue() - -1.0d) < 1.0E-5d) {
                        Log.d("Watermark_WMWeatherHelper", "parserWeahterPressure invalid value :-1");
                        pressure = null;
                    }
                    return pressure;
                }
            } catch (Exception e) {
                Log.d("Watermark_WMWeatherHelper", "parserWeahterPressure Exception");
                return null;
            }
        }
        return null;
    }
}
