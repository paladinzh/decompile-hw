package com.android.mms.transaction;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import cn.com.xy.sms.sdk.net.NetUtil;
import com.amap.api.services.geocoder.GeocodeSearch;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SendSmsService extends Service {
    private LocationListener GPSlocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            SendSmsService.this.mGPSLatitude = "" + location.getLatitude();
            SendSmsService.this.mGPSLongitude = "" + location.getLatitude();
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
    private LocationListener WIFIlocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            SendSmsService.this.mWifiLatitude = "" + location.getLatitude();
            SendSmsService.this.mWifiLongitude = "" + location.getLongitude();
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
    private LocationManager locationManager;
    private int mBatteryLevel = 0;
    private String mCellLatitude = null;
    private String mCellLongitude = null;
    private Context mContext;
    private String mGPSLatitude = null;
    private String mGPSLongitude = null;
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    SendSmsService.this.startLocationQuery();
                    return;
                case 2:
                    SendSmsService.this.sendSMS();
                    return;
                default:
                    return;
            }
        }
    };
    private SMSSendResultReceiver mSMSReceiver = new SMSSendResultReceiver();
    private TelephonyManager mTelephonyManager = null;
    private String mWifiLatitude = null;
    private String mWifiLongitude = null;
    private boolean unableToDetermineLocation = false;

    class SMSSendResultReceiver extends BroadcastReceiver {
        SMSSendResultReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()) {
                case -1:
                    Log.d("SendSmsService", "Send Message to success!");
                    break;
                default:
                    Log.d("SendSmsService", "Send Message to  fail!");
                    break;
            }
            SendSmsService.this.unregisterReceiver(SendSmsService.this.mSMSReceiver);
        }
    }

    private void startLocationQuery() {
        Message delayMessage = new Message();
        delayMessage.what = 2;
        getGpsLocation();
        if (TextUtils.isEmpty(this.mGPSLatitude) || TextUtils.isEmpty(this.mGPSLongitude)) {
            getDataLocation();
            new Thread(new Runnable() {
                public void run() {
                    SendSmsService.this.getCellLocation();
                }
            }).start();
            this.mHandler.sendMessageDelayed(delayMessage, 10000);
            return;
        }
        this.mHandler.sendMessageDelayed(delayMessage, 0);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        this.mContext = getApplicationContext();
        this.mTelephonyManager = (TelephonyManager) getSystemService("phone");
        super.onCreate();
    }

    public void onStart(Intent intent, int startId) {
        Log.d("SendSmsService", "onStart saveWifiDataGPS");
        this.mBatteryLevel = registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED")).getIntExtra("level", -1);
        Log.i("SendSmsService", "BatteryLevel " + this.mBatteryLevel);
        if (this.mBatteryLevel > 15) {
            saveWifiDataGPS(this.mContext);
            openWifiDataGPS(this.mContext);
        } else {
            saveData(this.mContext);
            openData(this.mContext);
        }
        Message delayMessage = new Message();
        delayMessage.what = 1;
        this.mHandler.sendMessageDelayed(delayMessage, 10000);
    }

    private String getMcc() {
        String mcc = "0";
        String operator = this.mTelephonyManager.getNetworkOperator();
        if (TextUtils.isEmpty(operator)) {
            return mcc;
        }
        return operator.substring(0, 3);
    }

    public String getMNC() {
        String mnc = "0";
        String operator = this.mTelephonyManager.getNetworkOperator();
        if (TextUtils.isEmpty(operator)) {
            return mnc;
        }
        return operator.substring(3);
    }

    private String getIMEINumber() {
        String imei = this.mTelephonyManager.getDeviceId();
        if (TextUtils.isEmpty(imei)) {
            return "0";
        }
        return imei;
    }

    private String getIMSINumber() {
        String imsi = this.mTelephonyManager.getSubscriberId();
        if (TextUtils.isEmpty(imsi)) {
            return "0";
        }
        return imsi;
    }

    private String getCurrentTime() {
        SimpleDateFormat formatt = new SimpleDateFormat("yyyyMMddhhmmss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        return formatt.format(calendar.getTime());
    }

    public void sendSMS() {
        Log.i("SendSmsService", "Start Send SMS");
        IntentFilter mSMSResultFilter = new IntentFilter();
        mSMSResultFilter.addAction("SENT_SMS_ACTION");
        registerReceiver(this.mSMSReceiver, mSMSResultFilter);
        SmsManager smsManager = SmsManager.getDefault();
        Intent itSend = new Intent("SENT_SMS_ACTION");
        itSend.setPackage("com.android.mms");
        String finallatitude = "+00.00000";
        String finallongitude = "+000.00000";
        boolean locationbyGPS = false;
        boolean locationbyWifi = false;
        if (!(TextUtils.isEmpty(this.mGPSLatitude) || TextUtils.isEmpty(this.mGPSLongitude))) {
            finallatitude = this.mGPSLatitude;
            finallongitude = this.mGPSLongitude;
            locationbyGPS = true;
            Log.i("SendSmsService", "Location using GPS");
        }
        if (!(locationbyGPS || TextUtils.isEmpty(this.mWifiLatitude) || TextUtils.isEmpty(this.mWifiLongitude))) {
            finallatitude = this.mWifiLatitude;
            finallongitude = this.mWifiLongitude;
            locationbyWifi = true;
            Log.i("SendSmsService", "Location using Wifi");
        }
        if (!(locationbyGPS || locationbyWifi || TextUtils.isEmpty(this.mCellLatitude) || TextUtils.isEmpty(this.mCellLongitude))) {
            finallatitude = this.mCellLatitude;
            finallongitude = this.mCellLongitude;
            Log.i("SendSmsService", "Location using Cell");
        }
        String smsbody = "A\"\"ML=1;lt=" + finallatitude + ";" + "lg" + "=" + finallongitude + ";" + "rd" + "=" + "N" + ";" + "top" + "=" + getCurrentTime() + ";" + "lc" + "=" + "0" + ";" + "pm" + "=" + "N" + ";" + "si" + "=" + getIMSINumber() + ";" + "ei" + "=" + getIMEINumber() + ";" + "mcc" + "=" + getMcc() + ";" + "mnc" + "=" + getMNC() + ";" + "ml" + "=";
        int length = smsbody.length();
        Log.i("SendSmsService", "Sms body lenth" + length);
        if (length >= 98) {
            length += 3;
        } else {
            length += 2;
        }
        smsManager.sendTextMessage("999", null, smsbody + length, PendingIntent.getBroadcast(this.mContext, 0, itSend, 0), null);
        this.locationManager.removeUpdates(this.GPSlocationListener);
        this.locationManager.removeUpdates(this.WIFIlocationListener);
        if (this.mBatteryLevel > 15) {
            restoreWifiDataGPS(this.mContext);
        } else {
            restoreData(this.mContext);
        }
        stopSelf();
    }

    public void openWifiDataGPS(Context context) {
        Log.d("SendSmsService", "openWifiDataGPS");
        toggleWifi(context, 3);
        toggleGps(context, true);
        toggleAirPlane(context, 0);
        toggleData(context, true);
    }

    public void openData(Context context) {
        Log.d("SendSmsService", "openData");
        toggleAirPlane(context, 0);
        toggleData(context, true);
    }

    public void restoreWifiDataGPS(Context context) {
        Log.d("SendSmsService", "restorePhoneState  WifiDataGPS");
        String emergency_sms_state = System.getString(context.getContentResolver(), "hw_emergency_sms_state");
        if (TextUtils.isEmpty(emergency_sms_state)) {
            Log.e("SendSmsService", "restore WifiDataGPS not context");
            return;
        }
        Map<String, Integer> mMap = new HashMap();
        for (String trim : emergency_sms_state.trim().split(";")) {
            String[] tempValues = trim.trim().split(":");
            mMap.put(tempValues[0], Integer.valueOf(Integer.parseInt(tempValues[1])));
        }
        Log.d("SendSmsService", "emergency_sms_state" + emergency_sms_state);
        Integer wifiState = (Integer) mMap.get("wifistate");
        if (wifiState == null) {
            Log.w("SendSmsService", "restorePhoneState wifiState get failed use the default value");
            wifiState = Integer.valueOf(1);
        }
        toggleWifi(context, wifiState.intValue());
        Integer gpsState = (Integer) mMap.get("gpsstate");
        if (gpsState == null) {
            Log.d("SendSmsService", "restorePhoneState gpsState get failed use the default value");
            gpsState = Integer.valueOf(0);
        }
        boolean bgpsState = gpsState.intValue() == 1;
        Log.d("SendSmsService", "restorePhoneState gpsState:" + gpsState + " bgpsState:" + bgpsState);
        toggleGps(context, bgpsState);
        Integer dataState = (Integer) mMap.get("datastate");
        if (dataState == null) {
            Log.d("SendSmsService", "restorePhoneState dataState get failed use the default value");
            dataState = Integer.valueOf(0);
        }
        boolean bdataState = dataState.intValue() == 1;
        Log.d("SendSmsService", "restorePhoneState dataState:" + dataState + " bdataState:" + bdataState);
        toggleData(context, bdataState);
        Integer airplaneMode = (Integer) mMap.get("airplanemode");
        if (airplaneMode == null) {
            Log.w("SendSmsService", "restorePhoneState airplaneMode get failed use the default value");
            airplaneMode = Integer.valueOf(0);
        }
        Log.d("SendSmsService", "restorePhoneState airplaneMode:" + airplaneMode);
        toggleAirPlane(context, airplaneMode.intValue());
    }

    public void restoreData(Context context) {
        Log.d("SendSmsService", "restorePhoneState  restoreData");
        String emergency_sms_state = System.getString(context.getContentResolver(), "hw_emergency_sms_state");
        if (TextUtils.isEmpty(emergency_sms_state)) {
            Log.e("SendSmsService", "restore WifiDataGPS not context");
            return;
        }
        String[] custValues = emergency_sms_state.trim().split(";");
        Map<String, Integer> mMap = new HashMap();
        for (String trim : custValues) {
            String[] tempValues = trim.trim().split(":");
            mMap.put(tempValues[0], Integer.valueOf(Integer.parseInt(tempValues[1])));
        }
        Log.d("SendSmsService", "emergency_sms_state" + emergency_sms_state);
        Integer dataState = (Integer) mMap.get("datastate");
        if (dataState == null) {
            Log.d("SendSmsService", "restorePhoneState dataState get failed, so will use the default value");
            dataState = Integer.valueOf(0);
        }
        boolean bdataState = dataState.intValue() == 1;
        Log.d("SendSmsService", "restorePhoneState dataState:" + dataState + " bdataState:" + bdataState);
        toggleData(context, bdataState);
        Integer airplaneMode = (Integer) mMap.get("airplanemode");
        if (airplaneMode == null) {
            Log.w("SendSmsService", "restorePhoneState airplaneMode get failed, so will use the default value");
            airplaneMode = Integer.valueOf(0);
        }
        Log.d("SendSmsService", "restorePhoneState airplaneMode:" + airplaneMode);
        toggleAirPlane(context, airplaneMode.intValue());
    }

    private static int getAirplaneModeOn(Context context) {
        return System.getInt(context.getContentResolver(), "airplane_mode_on", 0);
    }

    private static int getWifiState(Context context) {
        WifiManager wfm = (WifiManager) context.getSystemService("wifi");
        if (wfm != null) {
            return wfm.getWifiState();
        }
        return 4;
    }

    private static int getDataState(Context context) {
        return ((ConnectivityManager) context.getSystemService("connectivity")).getMobileDataEnabled() ? 1 : 0;
    }

    private static boolean getGpsState(Context context) {
        return Secure.isLocationProviderEnabled(context.getContentResolver(), GeocodeSearch.GPS);
    }

    private static void toggleWifi(Context context, int state) {
        WifiManager wifiManager = (WifiManager) context.getSystemService("wifi");
        if (wifiManager != null) {
            if (wifiManager.getWifiState() == state) {
                Log.w("SendSmsService", "WifiState now is: " + state + " , nothing then  return");
                return;
            }
            if (state == 3) {
                wifiManager.setWifiEnabled(true);
                Log.w("SendSmsService", "wifi state is enable");
            } else if (state == 1) {
                wifiManager.setWifiEnabled(false);
                Log.w("SendSmsService", "wifi state is disable");
            }
        }
    }

    private static void toggleData(Context context, boolean state) {
        ((TelephonyManager) context.getSystemService("phone")).setDataEnabled(state);
    }

    private static void toggleGps(Context context, boolean enabled) {
        ContentResolver resolver = context.getContentResolver();
        Log.w("SendSmsService", "toggleGps " + enabled);
        Secure.setLocationProviderEnabled(resolver, GeocodeSearch.GPS, enabled);
    }

    private static void toggleAirPlane(Context mContext, int isOn) {
        int stateAirPlane = System.getInt(mContext.getContentResolver(), "airplane_mode_on", 0);
        Log.w("SendSmsService", "toggle AirPlane now stateAirPlane is :" + stateAirPlane);
        if (isOn == stateAirPlane) {
            Log.w("SendSmsService", "toggle AirPlane now is :" + isOn + "  then return");
            return;
        }
        Log.w("SendSmsService", "toggleAirPlane switch air mode: " + isOn);
        System.putInt(mContext.getContentResolver(), "airplane_mode_on", isOn);
    }

    public static void saveWifiDataGPS(Context context) {
        StringBuffer sbBuffer = new StringBuffer();
        sbBuffer.append("wifistate:" + getWifiState(context) + ";");
        sbBuffer.append("gpsstate:" + (getGpsState(context) ? 1 : 0) + ";");
        sbBuffer.append("datastate:" + getDataState(context) + ";");
        sbBuffer.append("airplanemode:" + getAirplaneModeOn(context));
        System.putString(context.getContentResolver(), "hw_emergency_sms_state", sbBuffer.toString());
        Log.w("SendSmsService", "saveWifiDataGPS=" + sbBuffer.toString());
    }

    public static void saveData(Context context) {
        StringBuffer sbBuffer = new StringBuffer();
        sbBuffer.append("datastate:" + getDataState(context) + ";");
        sbBuffer.append("airplanemode:" + getAirplaneModeOn(context));
        System.putString(context.getContentResolver(), "hw_emergency_sms_state", sbBuffer.toString());
        Log.w("SendSmsService", "Data=" + sbBuffer.toString());
    }

    private void getCellLocation() {
        IOException e;
        Exception e2;
        Throwable th;
        TelephonyManager tm = (TelephonyManager) getSystemService("phone");
        if (tm == null) {
            Log.e("SendSmsService", "getCellLocation getSystemService : null");
            return;
        }
        GsmCellLocation gsmCell = (GsmCellLocation) tm.getCellLocation();
        if (gsmCell == null) {
            Log.e("SendSmsService", "getCellLocation GsmCellLocation: null");
            return;
        }
        int cid = gsmCell.getCid();
        int lac = gsmCell.getLac();
        OutputStream outputStream = null;
        InputStream inputStream = null;
        DataInputStream dataInputStream = null;
        try {
            HttpURLConnection httpConn = (HttpURLConnection) new URL("http://www.google.com/glm/mmap").openConnection();
            httpConn.setRequestMethod("POST");
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            httpConn.connect();
            outputStream = httpConn.getOutputStream();
            writeData(outputStream, cid, lac);
            inputStream = httpConn.getInputStream();
            DataInputStream dataInputStream2 = new DataInputStream(inputStream);
            try {
                dataInputStream2.readShort();
                dataInputStream2.readByte();
                if (dataInputStream2.readInt() == 0) {
                    int myLatitude = dataInputStream2.readInt();
                    int myLongitude = dataInputStream2.readInt();
                    this.mCellLatitude = String.valueOf(((float) myLatitude) / 1000000.0f);
                    this.mCellLongitude = String.valueOf(((float) myLongitude) / 1000000.0f);
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e3) {
                        Log.e("SendSmsService", "getCellLocation: failed colse the inputStream");
                    }
                }
                if (dataInputStream2 != null) {
                    try {
                        dataInputStream2.close();
                    } catch (IOException e4) {
                        Log.e("SendSmsService", "getCellLocation: failed colse the dataInputStream");
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e5) {
                        Log.e("SendSmsService", "getCellLocation: failed colse the outputStream");
                    }
                }
                dataInputStream = dataInputStream2;
            } catch (IOException e6) {
                e = e6;
                dataInputStream = dataInputStream2;
                e.printStackTrace();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e7) {
                        Log.e("SendSmsService", "getCellLocation: failed colse the inputStream");
                    }
                }
                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e8) {
                        Log.e("SendSmsService", "getCellLocation: failed colse the dataInputStream");
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e9) {
                        Log.e("SendSmsService", "getCellLocation: failed colse the outputStream");
                    }
                }
            } catch (Exception e10) {
                e2 = e10;
                dataInputStream = dataInputStream2;
                try {
                    e2.printStackTrace();
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e11) {
                            Log.e("SendSmsService", "getCellLocation: failed colse the inputStream");
                        }
                    }
                    if (dataInputStream != null) {
                        try {
                            dataInputStream.close();
                        } catch (IOException e12) {
                            Log.e("SendSmsService", "getCellLocation: failed colse the dataInputStream");
                        }
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e13) {
                            Log.e("SendSmsService", "getCellLocation: failed colse the outputStream");
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e14) {
                            Log.e("SendSmsService", "getCellLocation: failed colse the inputStream");
                        }
                    }
                    if (dataInputStream != null) {
                        try {
                            dataInputStream.close();
                        } catch (IOException e15) {
                            Log.e("SendSmsService", "getCellLocation: failed colse the dataInputStream");
                        }
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e16) {
                            Log.e("SendSmsService", "getCellLocation: failed colse the outputStream");
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                dataInputStream = dataInputStream2;
                if (inputStream != null) {
                    inputStream.close();
                }
                if (dataInputStream != null) {
                    dataInputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                throw th;
            }
        } catch (IOException e17) {
            e = e17;
            e.printStackTrace();
            if (inputStream != null) {
                inputStream.close();
            }
            if (dataInputStream != null) {
                dataInputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (Exception e18) {
            e2 = e18;
            e2.printStackTrace();
            if (inputStream != null) {
                inputStream.close();
            }
            if (dataInputStream != null) {
                dataInputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    private void writeData(OutputStream out, int cid, int lac) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(out);
        dataOutputStream.writeShort(21);
        dataOutputStream.writeLong(0);
        dataOutputStream.writeUTF("en");
        dataOutputStream.writeUTF("Android");
        dataOutputStream.writeUTF("1.0");
        dataOutputStream.writeUTF("Web");
        dataOutputStream.writeByte(27);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(3);
        dataOutputStream.writeUTF("");
        dataOutputStream.writeInt(cid);
        dataOutputStream.writeInt(lac);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.flush();
    }

    private void getGpsLocation() {
        this.locationManager = (LocationManager) getSystemService(NetUtil.REQ_QUERY_LOCATION);
        if (this.locationManager == null) {
            Log.e("SendSmsService", "getGpsLocation getSystemService : null");
            return;
        }
        LocationProvider provider = this.locationManager.getProvider(GeocodeSearch.GPS);
        if (provider == null) {
            Log.e("SendSmsService", "getGpsLocation getProvider : null");
            return;
        }
        String currentProvider = provider.getName();
        if (this.locationManager.getLastKnownLocation(currentProvider) == null) {
            this.locationManager.requestLocationUpdates(currentProvider, 0, 0.0f, this.GPSlocationListener);
        }
        Location currentLocation = this.locationManager.getLastKnownLocation(currentProvider);
        if (currentLocation != null) {
            this.mGPSLatitude = "" + currentLocation.getLatitude();
            this.mGPSLongitude = "" + currentLocation.getLatitude();
        } else {
            Log.d("SendSmsService", "getGpsLocation : currentLocation = null");
        }
    }

    private void getDataLocation() {
        this.locationManager = (LocationManager) getSystemService(NetUtil.REQ_QUERY_LOCATION);
        if (this.locationManager == null) {
            Log.e("SendSmsService", "getSystemService LOCATION_SERVICE : null");
            return;
        }
        LocationProvider provider = this.locationManager.getProvider("network");
        if (provider == null) {
            Log.e("SendSmsService", "getDataLocation getProvider : null");
            return;
        }
        String currentProvider = provider.getName();
        Location currentLocation = this.locationManager.getLastKnownLocation(currentProvider);
        if (currentLocation == null) {
            this.locationManager.requestLocationUpdates(currentProvider, 1000, 0.0f, this.WIFIlocationListener);
        }
        if (currentLocation != null) {
            this.mWifiLatitude = "" + currentLocation.getLatitude();
            this.mWifiLongitude = "" + currentLocation.getLongitude();
        } else {
            Log.d("SendSmsService", "getDataLocation : currentLocation = null");
        }
    }
}
