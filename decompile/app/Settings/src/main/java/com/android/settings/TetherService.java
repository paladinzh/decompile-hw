package com.android.settings;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothPan;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.android.settingslib.TetherUtil;
import java.util.ArrayList;
import java.util.List;

public class TetherService extends Service {
    private static final boolean DEBUG = Log.isLoggable("TetherService", 3);
    public static final String EXTRA_RESULT = "EntitlementResult";
    private ArrayList<Integer> mCurrentTethers;
    private int mCurrentTypeIndex;
    private boolean mInProvisionCheck;
    private ArrayMap<Integer, List<ResultReceiver>> mPendingCallbacks;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (TetherService.DEBUG) {
                Log.d("TetherService", "Got provision result " + intent);
            }
            if (TetherService.this.getResources().getString(17039413).equals(intent.getAction())) {
                if (TetherService.this.mInProvisionCheck) {
                    int checkType = ((Integer) TetherService.this.mCurrentTethers.get(TetherService.this.mCurrentTypeIndex)).intValue();
                    TetherService.this.mInProvisionCheck = false;
                    int result = intent.getIntExtra(TetherService.EXTRA_RESULT, 0);
                    if (result != -1) {
                        switch (checkType) {
                            case 0:
                                TetherService.this.disableWifiTethering();
                                break;
                            case 1:
                                TetherService.this.disableUsbTethering();
                                break;
                            case 2:
                                TetherService.this.disableBtTethering();
                                break;
                        }
                    }
                    TetherService.this.fireCallbacksForType(checkType, result);
                    TetherService tetherService = TetherService.this;
                    if (tetherService.mCurrentTypeIndex = tetherService.mCurrentTypeIndex + 1 >= TetherService.this.mCurrentTethers.size()) {
                        TetherService.this.stopSelf();
                    } else {
                        TetherService.this.startProvisioning(TetherService.this.mCurrentTypeIndex);
                    }
                } else {
                    Log.e("TetherService", "Unexpected provision response " + intent);
                }
            }
        }
    };

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        if (DEBUG) {
            Log.d("TetherService", "Creating TetherService");
        }
        registerReceiver(this.mReceiver, new IntentFilter(getResources().getString(17039413)), "android.permission.CONNECTIVITY_INTERNAL", null);
        this.mCurrentTethers = stringToTethers(getSharedPreferences("tetherPrefs", 0).getString("currentTethers", ""));
        this.mCurrentTypeIndex = 0;
        this.mPendingCallbacks = new ArrayMap(3);
        this.mPendingCallbacks.put(Integer.valueOf(0), new ArrayList());
        this.mPendingCallbacks.put(Integer.valueOf(1), new ArrayList());
        this.mPendingCallbacks.put(Integer.valueOf(2), new ArrayList());
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        int type;
        if (intent.hasExtra("extraAddTetherType")) {
            type = intent.getIntExtra("extraAddTetherType", -1);
            ResultReceiver callback = (ResultReceiver) intent.getParcelableExtra("extraProvisionCallback");
            if (callback != null) {
                List<ResultReceiver> callbacksForType = (List) this.mPendingCallbacks.get(Integer.valueOf(type));
                if (callbacksForType != null) {
                    callbacksForType.add(callback);
                } else {
                    callback.send(1, null);
                    stopSelf();
                    return 2;
                }
            }
            if (!this.mCurrentTethers.contains(Integer.valueOf(type))) {
                if (DEBUG) {
                    Log.d("TetherService", "Adding tether " + type);
                }
                this.mCurrentTethers.add(Integer.valueOf(type));
            }
        }
        if (intent.hasExtra("extraRemTetherType")) {
            if (!this.mInProvisionCheck) {
                type = intent.getIntExtra("extraRemTetherType", -1);
                int index = this.mCurrentTethers.indexOf(Integer.valueOf(type));
                if (DEBUG) {
                    Log.d("TetherService", "Removing tether " + type + ", index " + index);
                }
                if (index >= 0) {
                    removeTypeAtIndex(index);
                }
                cancelAlarmIfNecessary();
            } else if (DEBUG) {
                Log.d("TetherService", "Don't cancel alarm during provisioning");
            }
        }
        if (intent.getBooleanExtra("extraSetAlarm", false) && this.mCurrentTethers.size() == 1) {
            scheduleAlarm();
        }
        if (intent.getBooleanExtra("extraRunProvision", false)) {
            startProvisioning(this.mCurrentTypeIndex);
        } else if (!this.mInProvisionCheck) {
            if (DEBUG) {
                Log.d("TetherService", "Stopping self.  startid: " + startId);
            }
            stopSelf();
            return 2;
        }
        return 3;
    }

    public void onDestroy() {
        if (this.mInProvisionCheck) {
            Log.e("TetherService", "TetherService getting destroyed while mid-provisioning" + this.mCurrentTethers.get(this.mCurrentTypeIndex));
        }
        getSharedPreferences("tetherPrefs", 0).edit().putString("currentTethers", tethersToString(this.mCurrentTethers)).commit();
        if (DEBUG) {
            Log.d("TetherService", "Destroying TetherService");
        }
        unregisterReceiver(this.mReceiver);
        super.onDestroy();
    }

    private void removeTypeAtIndex(int index) {
        this.mCurrentTethers.remove(index);
        if (DEBUG) {
            Log.d("TetherService", "mCurrentTypeIndex: " + this.mCurrentTypeIndex);
        }
        if (index <= this.mCurrentTypeIndex && this.mCurrentTypeIndex > 0) {
            this.mCurrentTypeIndex--;
        }
    }

    private ArrayList<Integer> stringToTethers(String tethersStr) {
        ArrayList<Integer> ret = new ArrayList();
        if (TextUtils.isEmpty(tethersStr)) {
            return ret;
        }
        String[] tethersSplit = tethersStr.split(",");
        for (String parseInt : tethersSplit) {
            ret.add(Integer.valueOf(Integer.parseInt(parseInt)));
        }
        return ret;
    }

    private String tethersToString(ArrayList<Integer> tethers) {
        StringBuffer buffer = new StringBuffer();
        int N = tethers.size();
        for (int i = 0; i < N; i++) {
            if (i != 0) {
                buffer.append(',');
            }
            buffer.append(tethers.get(i));
        }
        return buffer.toString();
    }

    private void disableWifiTethering() {
        TetherUtil.setWifiTethering(false, this);
    }

    private void disableUsbTethering() {
        ((ConnectivityManager) getSystemService("connectivity")).setUsbTethering(false);
    }

    private void disableBtTethering() {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            adapter.getProfileProxy(this, new ServiceListener() {
                public void onServiceDisconnected(int profile) {
                }

                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    ((BluetoothPan) proxy).setBluetoothTethering(false);
                    adapter.closeProfileProxy(5, proxy);
                }
            }, 5);
        }
    }

    private void startProvisioning(int index) {
        if (index < this.mCurrentTethers.size()) {
            String provisionAction = getResources().getString(17039412);
            if (DEBUG) {
                Log.d("TetherService", "Sending provisioning broadcast: " + provisionAction + " type: " + this.mCurrentTethers.get(index));
            }
            Intent intent = new Intent(provisionAction);
            intent.putExtra("TETHER_TYPE", ((Integer) this.mCurrentTethers.get(index)).intValue());
            intent.setFlags(268435456);
            sendBroadcast(intent);
            this.mInProvisionCheck = true;
        }
    }

    private void scheduleAlarm() {
        Intent intent = new Intent(this, TetherService.class);
        intent.putExtra("extraRunProvision", true);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService("alarm");
        long periodMs = ((long) getResources().getInteger(17694737)) * 3600000;
        long firstTime = SystemClock.elapsedRealtime() + periodMs;
        if (DEBUG) {
            Log.d("TetherService", "Scheduling alarm at interval " + periodMs);
        }
        alarmManager.setRepeating(3, firstTime, periodMs, pendingIntent);
    }

    public static void cancelRecheckAlarmIfNecessary(Context context, int type) {
        Intent intent = new Intent(context, TetherService.class);
        intent.putExtra("extraRemTetherType", type);
        context.startService(intent);
    }

    private void cancelAlarmIfNecessary() {
        if (this.mCurrentTethers.size() != 0) {
            if (DEBUG) {
                Log.d("TetherService", "Tethering still active, not cancelling alarm");
            }
            return;
        }
        ((AlarmManager) getSystemService("alarm")).cancel(PendingIntent.getService(this, 0, new Intent(this, TetherService.class), 0));
        if (DEBUG) {
            Log.d("TetherService", "Tethering no longer active, canceling recheck");
        }
    }

    private void fireCallbacksForType(int type, int result) {
        List<ResultReceiver> callbacksForType = (List) this.mPendingCallbacks.get(Integer.valueOf(type));
        if (callbacksForType != null) {
            int errorCode;
            if (result == -1) {
                errorCode = 0;
            } else {
                errorCode = 11;
            }
            for (ResultReceiver callback : callbacksForType) {
                if (DEBUG) {
                    Log.d("TetherService", "Firing result: " + errorCode + " to callback");
                }
                callback.send(errorCode, null);
            }
            callbacksForType.clear();
        }
    }
}
