package com.android.connection;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import com.android.util.HwLog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi.GetConnectedNodesResult;
import com.google.android.gms.wearable.Wearable;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

public class AlarmWearableSendMsgService extends IntentService implements ConnectionCallbacks, OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private boolean mServiceAvailable = true;

    public AlarmWearableSendMsgService() {
        super(AlarmWearableSendMsgService.class.getSimpleName());
    }

    public void onCreate() {
        super.onCreate();
        checkPlayServicesAvailable();
        if (this.mServiceAvailable) {
            this.mGoogleApiClient = new Builder(this).addApi(Wearable.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        }
    }

    public void checkPlayServicesAvailable() {
        try {
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        } catch (IllegalStateException e) {
            HwLog.i("AlarmWearableSendMsgService", "checkPlayServicesAvailable IllegalStateException" + e.getMessage());
            this.mServiceAvailable = false;
        } catch (Exception e2) {
            HwLog.i("AlarmWearableSendMsgService", "checkPlayServicesAvailable exception" + e2.getMessage());
            this.mServiceAvailable = false;
        }
    }

    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet();
        for (Node node : ((GetConnectedNodesResult) Wearable.NodeApi.getConnectedNodes(this.mGoogleApiClient).await()).getNodes()) {
            results.add(node.getId());
        }
        return results;
    }

    protected void onHandleIntent(Intent intent) {
        if (intent != null && this.mServiceAvailable) {
            for (int count = 0; count < 3; count++) {
                this.mGoogleApiClient.blockingConnect(((long) (count + 1)) * 100, TimeUnit.MILLISECONDS);
                if (this.mGoogleApiClient.isConnected()) {
                    String action = intent.getAction();
                    if ("com.android.connection.action.alarm_fire".equals(action)) {
                        handleActionAlarmFire(Long.valueOf(intent.getLongExtra("alarm_time", 0)), Long.valueOf(intent.getLongExtra("alarm_id", 0)), Boolean.valueOf(intent.getBooleanExtra("alarm_vibrate", true)), Boolean.valueOf(intent.getBooleanExtra("alarm_ring", true)));
                    } else if ("com.android.connection.action.handle_alarm".equals(action)) {
                        handleActionAlarmHandle(Long.valueOf(intent.getLongExtra("alarm_id", 0)), intent.getIntExtra("alarm_state", 0));
                    } else if ("com.android.connection.action.mute_alarm".equals(action)) {
                        handleActionAlarmMute(Long.valueOf(intent.getLongExtra("alarm_id", 0)), intent.getBooleanExtra("alarm_mute", false));
                    }
                    this.mGoogleApiClient.disconnect();
                }
                HwLog.i("connection", "Failed to send data item:  - Client disconnected from Google Play Services");
            }
            this.mGoogleApiClient.disconnect();
        }
    }

    private void handleActionAlarmMute(Long alarmID, boolean bMute) {
        DataMap dataMap = new DataMap();
        dataMap.putLong("alarm_id", alarmID.longValue());
        dataMap.putBoolean("alarm_mute", bMute);
        HwLog.i("connection", "send mute alarm message alramid = " + alarmID);
        try {
            for (String node : getNodes()) {
                Wearable.MessageApi.sendMessage(this.mGoogleApiClient, node, "/alarm_mute", dataMap.toByteArray());
            }
        } catch (RuntimeException e) {
            HwLog.i("connection", "handleActionAlarmMute exception");
        }
    }

    public void handleActionAlarmFire(Long alarm_time, Long alarm_id, Boolean bAlarmVibrate, Boolean bAlarmRing) {
        DataMap dataMap = new DataMap();
        dataMap.putLong("alarm_time", alarm_time.longValue());
        dataMap.putLong("alarm_id", alarm_id.longValue());
        dataMap.putBoolean("alarm_vibrate", bAlarmVibrate.booleanValue());
        dataMap.putBoolean("alarm_ring", bAlarmRing.booleanValue());
        HwLog.i("connection", "send start alarm message alramid = " + alarm_id);
        try {
            for (String node : getNodes()) {
                Wearable.MessageApi.sendMessage(this.mGoogleApiClient, node, "/alarm_firing", dataMap.toByteArray());
            }
        } catch (RuntimeException e) {
            HwLog.i("connection", "handleActionAlarmFire exception");
        }
    }

    public void handleActionAlarmHandle(Long alarmID, int alarmState) {
        DataMap dataMap = new DataMap();
        dataMap.putLong("alarm_id", alarmID.longValue());
        dataMap.putInt("alarm_state", alarmState);
        if (alarmState == 1) {
            HwLog.i("connection", "send snooze alarm message, alramid = " + alarmID);
        } else {
            HwLog.i("connection", "send stop alarm message  alramid = " + alarmID);
        }
        try {
            for (String node : getNodes()) {
                Wearable.MessageApi.sendMessage(this.mGoogleApiClient, node, "/alarm_end_state", dataMap.toByteArray());
            }
        } catch (RuntimeException e) {
            HwLog.i("connection", "handleActionAlarmHandle exception");
        }
    }

    public void onConnected(Bundle connectionHint) {
        HwLog.i("connection", "connection google service");
    }

    public void onConnectionSuspended(int cause) {
        HwLog.i("connection", "connection suspended");
    }

    public void onConnectionFailed(ConnectionResult result) {
        HwLog.i("connection", "connection fail");
    }
}
