package com.android.settings.bluetooth;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings.Global;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.CachedBluetoothDeviceManager;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.bluetooth.LocalBluetoothProfile;
import com.android.settingslib.bluetooth.LocalBluetoothProfileManager;
import com.android.settingslib.bluetooth.LocalBluetoothProfileManager.ServiceListener;
import java.util.Collection;
import java.util.Set;

public final class DockService extends Service implements ServiceListener {
    private boolean isServiceStarted = false;
    private CheckBox mAudioMediaCheckbox;
    private final OnCheckedChangeListener mCheckedChangeListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (DockService.this.mDevice != null) {
                LocalBluetoothPreferences.saveDockAutoConnectSetting(DockService.this, DockService.this.mDevice.getAddress(), isChecked);
            } else {
                Global.putInt(DockService.this.getContentResolver(), "dock_audio_media_enabled", isChecked ? 1 : 0);
            }
        }
    };
    private boolean[] mCheckedItems;
    private final OnClickListener mClickListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            int i = 1;
            if (which != -1) {
                return;
            }
            if (DockService.this.mDevice != null) {
                if (!LocalBluetoothPreferences.hasDockAutoConnectSetting(DockService.this, DockService.this.mDevice.getAddress())) {
                    LocalBluetoothPreferences.saveDockAutoConnectSetting(DockService.this, DockService.this.mDevice.getAddress(), true);
                }
                DockService.this.applyBtSettings(DockService.this.mDevice, DockService.this.mStartIdAssociatedWithDialog);
            } else if (DockService.this.mAudioMediaCheckbox != null) {
                ContentResolver contentResolver = DockService.this.getContentResolver();
                String str = "dock_audio_media_enabled";
                if (!DockService.this.mAudioMediaCheckbox.isChecked()) {
                    i = 0;
                }
                Global.putInt(contentResolver, str, i);
            }
        }
    };
    private BluetoothDevice mDevice;
    private CachedBluetoothDeviceManager mDeviceManager;
    private AlertDialog mDialog;
    private final OnDismissListener mDismissListener = new OnDismissListener() {
        public void onDismiss(DialogInterface dialog) {
            if (DockService.this.mPendingDevice == null) {
                DockEventReceiver.finishStartingService(DockService.this, DockService.this.mStartIdAssociatedWithDialog);
            }
            DockService.this.stopForeground(true);
        }
    };
    private LocalBluetoothAdapter mLocalAdapter;
    private final OnMultiChoiceClickListener mMultiClickListener = new OnMultiChoiceClickListener() {
        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
            DockService.this.mCheckedItems[which] = isChecked;
        }
    };
    private BluetoothDevice mPendingDevice;
    private int mPendingStartId;
    private int mPendingTurnOffStartId = -100;
    private int mPendingTurnOnStartId = -100;
    private LocalBluetoothProfileManager mProfileManager;
    private LocalBluetoothProfile[] mProfiles;
    private Runnable mRunnable;
    private volatile ServiceHandler mServiceHandler;
    private volatile Looper mServiceLooper;
    private int mStartIdAssociatedWithDialog;

    public static class DockBluetoothCallback implements BluetoothCallback {
        private final Context mContext;

        public DockBluetoothCallback(Context context) {
            this.mContext = context;
        }

        public void onBluetoothStateChanged(int bluetoothState) {
        }

        public void onDeviceAdded(CachedBluetoothDevice cachedDevice) {
        }

        public void onDeviceDeleted(CachedBluetoothDevice cachedDevice) {
        }

        public void onConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state) {
        }

        public void onScanningStateChanged(boolean started) {
            LocalBluetoothPreferences.persistDiscoveringTimestamp(this.mContext);
        }

        public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState) {
            BluetoothDevice device = cachedDevice.getDevice();
            if (bondState == 10 && device.isBluetoothDock()) {
                LocalBluetoothPreferences.removeDockAutoConnectSetting(this.mContext, device.getAddress());
                if (!device.getAddress().equals(getDockedDeviceAddress(this.mContext))) {
                    cachedDevice.setVisible(false);
                }
            }
        }

        private static String getDockedDeviceAddress(Context context) {
            Intent i = context.registerReceiver(null, new IntentFilter("android.intent.action.DOCK_EVENT"));
            if (!(i == null || i.getIntExtra("android.intent.extra.DOCK_STATE", 0) == 0)) {
                BluetoothDevice device = (BluetoothDevice) i.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                if (device != null) {
                    return device.getAddress();
                }
            }
            return null;
        }
    }

    private final class ServiceHandler extends Handler {
        private ServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            DockService.this.processMessage(msg);
        }
    }

    public void onCreate() {
        super.onCreate();
        this.isServiceStarted = false;
        LocalBluetoothManager manager = Utils.getLocalBtManager(this);
        if (manager == null) {
            Log.e("DockService", "Can't get LocalBluetoothManager: exiting");
            return;
        }
        this.mLocalAdapter = manager.getBluetoothAdapter();
        this.mDeviceManager = manager.getCachedDeviceManager();
        this.mProfileManager = manager.getProfileManager();
        if (this.mProfileManager == null) {
            Log.e("DockService", "Can't get LocalBluetoothProfileManager: exiting");
            return;
        }
        HandlerThread thread = new HandlerThread("DockService");
        thread.start();
        this.mServiceLooper = thread.getLooper();
        this.mServiceHandler = new ServiceHandler(this.mServiceLooper);
    }

    public void onDestroy() {
        this.mRunnable = null;
        if (this.mDialog != null) {
            this.mDialog.dismiss();
            this.mDialog = null;
        }
        if (this.mProfileManager != null) {
            this.mProfileManager.removeServiceListener(this);
        }
        if (this.mServiceLooper != null) {
            this.mServiceLooper.quit();
        }
        this.mLocalAdapter = null;
        this.mDeviceManager = null;
        this.mProfileManager = null;
        this.mServiceLooper = null;
        this.mServiceHandler = null;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    private SharedPreferences getPrefs() {
        return getSharedPreferences("dock_settings", 0);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (this.isServiceStarted && intent != null) {
            DockEventReceiver.releaseStartingService();
        }
        this.isServiceStarted = true;
        if (intent == null) {
            DockEventReceiver.finishStartingService(this, startId);
            return 2;
        } else if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(intent.getAction())) {
            handleBtStateChange(intent, startId);
            return 2;
        } else {
            SharedPreferences prefs = getPrefs();
            BluetoothDevice disconnectedDevice;
            int retryCount;
            if ("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED".equals(intent.getAction())) {
                disconnectedDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                retryCount = prefs.getInt("connect_retry_count", 0);
                if (retryCount < 6) {
                    prefs.edit().putInt("connect_retry_count", retryCount + 1).apply();
                    handleUnexpectedDisconnect(disconnectedDevice, this.mProfileManager.getHeadsetProfile(), startId);
                }
                return 2;
            } else if ("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED".equals(intent.getAction())) {
                disconnectedDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                retryCount = prefs.getInt("connect_retry_count", 0);
                if (retryCount < 6) {
                    prefs.edit().putInt("connect_retry_count", retryCount + 1).apply();
                    handleUnexpectedDisconnect(disconnectedDevice, this.mProfileManager.getA2dpProfile(), startId);
                }
                return 2;
            } else {
                Message msg = parseIntent(intent);
                if (msg == null) {
                    DockEventReceiver.finishStartingService(this, startId);
                    return 2;
                }
                if (msg.what == 222) {
                    prefs.edit().remove("connect_retry_count").apply();
                }
                msg.arg2 = startId;
                processMessage(msg);
                return 2;
            }
        }
    }

    private synchronized void processMessage(Message msg) {
        int msgType = msg.what;
        int state = msg.arg1;
        int startId = msg.arg2;
        BluetoothDevice bluetoothDevice = null;
        if (msg.obj != null) {
            bluetoothDevice = msg.obj;
        }
        boolean deferFinishCall = false;
        switch (msgType) {
            case 111:
                if (bluetoothDevice != null) {
                    createDialog(bluetoothDevice, state, startId);
                    break;
                }
                break;
            case 222:
                deferFinishCall = msgTypeDocked(bluetoothDevice, state, startId);
                break;
            case 333:
                msgTypeUndockedTemporary(bluetoothDevice, state, startId);
                break;
            case 444:
                deferFinishCall = msgTypeUndockedPermanent(bluetoothDevice, startId);
                break;
            case 555:
                deferFinishCall = msgTypeDisableBluetooth(startId);
                break;
        }
        if (this.mDialog == null && this.mPendingDevice == null && msgType != 333 && !r0) {
            DockEventReceiver.finishStartingService(this, startId);
        }
    }

    private boolean msgTypeDisableBluetooth(int startId) {
        SharedPreferences prefs = getPrefs();
        if (this.mLocalAdapter.disable()) {
            prefs.edit().remove("disable_bt_when_undock").apply();
            return false;
        }
        prefs.edit().putBoolean("disable_bt", true).apply();
        this.mPendingTurnOffStartId = startId;
        return true;
    }

    private void msgTypeUndockedTemporary(BluetoothDevice device, int state, int startId) {
        this.mServiceHandler.sendMessageDelayed(this.mServiceHandler.obtainMessage(444, state, startId, device), 1000);
    }

    private boolean msgTypeUndockedPermanent(BluetoothDevice device, int startId) {
        handleUndocked(device);
        if (device != null) {
            SharedPreferences prefs = getPrefs();
            if (prefs.getBoolean("disable_bt_when_undock", false)) {
                if (hasOtherConnectedDevices(device)) {
                    prefs.edit().remove("disable_bt_when_undock").apply();
                } else {
                    this.mServiceHandler.sendMessageDelayed(this.mServiceHandler.obtainMessage(555, 0, startId, null), 2000);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean msgTypeDocked(final BluetoothDevice device, final int state, final int startId) {
        this.mServiceHandler.removeMessages(444);
        this.mServiceHandler.removeMessages(555);
        getPrefs().edit().remove("disable_bt").apply();
        if (device != null) {
            if (!device.equals(this.mDevice)) {
                if (this.mDevice != null) {
                    handleUndocked(this.mDevice);
                }
                this.mDevice = device;
                this.mProfileManager.addServiceListener(this);
                if (this.mProfileManager.isManagerReady()) {
                    handleDocked(device, state, startId);
                    this.mProfileManager.removeServiceListener(this);
                } else {
                    BluetoothDevice d = device;
                    this.mRunnable = new Runnable() {
                        public void run() {
                            DockService.this.handleDocked(device, state, startId);
                        }
                    };
                    return true;
                }
            }
        } else if (Global.getInt(getContentResolver(), "dock_audio_media_enabled", -1) == -1 && state == 3) {
            handleDocked(null, state, startId);
            return true;
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    synchronized boolean hasOtherConnectedDevices(BluetoothDevice dock) {
        Collection<CachedBluetoothDevice> cachedDevices = this.mDeviceManager.getCachedDevicesCopy();
        Set<BluetoothDevice> btDevices = this.mLocalAdapter.getBondedDevices();
        if (!(btDevices == null || cachedDevices == null)) {
            if (!btDevices.isEmpty()) {
                for (CachedBluetoothDevice deviceUI : cachedDevices) {
                    BluetoothDevice btDevice = deviceUI.getDevice();
                    if (!btDevice.equals(dock) && btDevices.contains(btDevice) && deviceUI.isConnected()) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    private Message parseIntent(Intent intent) {
        int msgType;
        BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
        int state = intent.getIntExtra("android.intent.extra.DOCK_STATE", -1234);
        switch (state) {
            case 0:
                msgType = 333;
                break;
            case 1:
            case 2:
            case 4:
                if (device == null) {
                    Log.w("DockService", "device is null");
                    return null;
                }
                break;
            case 3:
                break;
            default:
                return null;
        }
        if (!"com.android.settings.bluetooth.action.DOCK_SHOW_UI".equals(intent.getAction())) {
            msgType = 222;
        } else if (device == null) {
            Log.w("DockService", "device is null");
            return null;
        } else {
            msgType = 111;
        }
        return this.mServiceHandler.obtainMessage(msgType, state, 0, device);
    }

    private void createDialog(BluetoothDevice device, int state, int startId) {
        if (this.mDialog != null) {
            this.mDialog.dismiss();
            this.mDialog = null;
        }
        this.mDevice = device;
        switch (state) {
            case 1:
            case 2:
            case 3:
            case 4:
                View view;
                startForeground(0, new Notification());
                Builder ab = new Builder(this);
                LayoutInflater inflater = (LayoutInflater) getSystemService("layout_inflater");
                this.mAudioMediaCheckbox = null;
                if (device != null) {
                    boolean checked;
                    boolean firstTime = !LocalBluetoothPreferences.hasDockAutoConnectSetting(this, device.getAddress());
                    CharSequence[] items = initBtSettings(device, state, firstTime);
                    ab.setTitle(getString(2131624863));
                    ab.setMultiChoiceItems(items, this.mCheckedItems, this.mMultiClickListener);
                    view = inflater.inflate(2130969049, null);
                    CheckBox rememberCheckbox = (CheckBox) view.findViewById(2131887074);
                    if (firstTime) {
                        checked = true;
                    } else {
                        checked = LocalBluetoothPreferences.getDockAutoConnectSetting(this, device.getAddress());
                    }
                    rememberCheckbox.setChecked(checked);
                    rememberCheckbox.setOnCheckedChangeListener(this.mCheckedChangeListener);
                } else {
                    ab.setTitle(getString(2131624863));
                    view = inflater.inflate(2130968756, null);
                    this.mAudioMediaCheckbox = (CheckBox) view.findViewById(2131886533);
                    this.mAudioMediaCheckbox.setChecked(Global.getInt(getContentResolver(), "dock_audio_media_enabled", 0) == 1);
                    this.mAudioMediaCheckbox.setOnCheckedChangeListener(this.mCheckedChangeListener);
                }
                float pixelScaleFactor = getResources().getDisplayMetrics().density;
                ab.setView(view, (int) (14.0f * pixelScaleFactor), 0, (int) (14.0f * pixelScaleFactor), 0);
                ab.setPositiveButton(getString(17039370), this.mClickListener);
                this.mStartIdAssociatedWithDialog = startId;
                this.mDialog = ab.create();
                this.mDialog.getWindow().setType(2009);
                this.mDialog.setOnDismissListener(this.mDismissListener);
                this.mDialog.show();
                return;
            default:
                return;
        }
    }

    private CharSequence[] initBtSettings(BluetoothDevice device, int state, boolean firstTime) {
        int numOfProfiles;
        switch (state) {
            case 1:
            case 3:
            case 4:
                numOfProfiles = 1;
                break;
            case 2:
                numOfProfiles = 2;
                break;
            default:
                return null;
        }
        this.mProfiles = new LocalBluetoothProfile[numOfProfiles];
        this.mCheckedItems = new boolean[numOfProfiles];
        CharSequence[] items = new CharSequence[numOfProfiles];
        switch (state) {
            case 1:
            case 3:
            case 4:
                items[0] = getString(2131624865);
                this.mProfiles[0] = this.mProfileManager.getA2dpProfile();
                if (!firstTime) {
                    this.mCheckedItems[0] = this.mProfiles[0].isPreferred(device);
                    break;
                }
                this.mCheckedItems[0] = false;
                break;
            case 2:
                items[0] = getString(2131624864);
                items[1] = getString(2131624865);
                this.mProfiles[0] = this.mProfileManager.getHeadsetProfile();
                this.mProfiles[1] = this.mProfileManager.getA2dpProfile();
                if (!firstTime) {
                    this.mCheckedItems[0] = this.mProfiles[0].isPreferred(device);
                    this.mCheckedItems[1] = this.mProfiles[1].isPreferred(device);
                    break;
                }
                this.mCheckedItems[0] = true;
                this.mCheckedItems[1] = true;
                break;
        }
        return items;
    }

    private void handleBtStateChange(Intent intent, int startId) {
        int btState = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", Integer.MIN_VALUE);
        synchronized (this) {
            if (btState == 12) {
                handleBluetoothStateOn(startId);
            } else if (btState == 13) {
                getPrefs().edit().remove("disable_bt_when_undock").apply();
                DockEventReceiver.finishStartingService(this, startId);
            } else if (btState == 10) {
                if (this.mPendingTurnOffStartId != -100) {
                    DockEventReceiver.finishStartingService(this, this.mPendingTurnOffStartId);
                    getPrefs().edit().remove("disable_bt").apply();
                    this.mPendingTurnOffStartId = -100;
                }
                if (this.mPendingDevice != null) {
                    this.mLocalAdapter.enable();
                    this.mPendingTurnOnStartId = startId;
                } else {
                    DockEventReceiver.finishStartingService(this, startId);
                }
            }
        }
    }

    private void handleBluetoothStateOn(int startId) {
        if (this.mPendingDevice != null) {
            if (this.mPendingDevice.equals(this.mDevice)) {
                applyBtSettings(this.mPendingDevice, this.mPendingStartId);
            }
            this.mPendingDevice = null;
            DockEventReceiver.finishStartingService(this, this.mPendingStartId);
        } else {
            SharedPreferences prefs = getPrefs();
            Intent i = registerReceiver(null, new IntentFilter("android.intent.action.DOCK_EVENT"));
            if (i != null) {
                if (i.getIntExtra("android.intent.extra.DOCK_STATE", 0) != 0) {
                    BluetoothDevice device = (BluetoothDevice) i.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                    if (device != null) {
                        connectIfEnabled(device);
                    }
                } else if (prefs.getBoolean("disable_bt", false) && this.mLocalAdapter.disable()) {
                    this.mPendingTurnOffStartId = startId;
                    prefs.edit().remove("disable_bt").apply();
                    return;
                }
            }
        }
        if (this.mPendingTurnOnStartId != -100) {
            DockEventReceiver.finishStartingService(this, this.mPendingTurnOnStartId);
            this.mPendingTurnOnStartId = -100;
        }
        DockEventReceiver.finishStartingService(this, startId);
    }

    private synchronized void handleUnexpectedDisconnect(BluetoothDevice disconnectedDevice, LocalBluetoothProfile profile, int startId) {
        if (disconnectedDevice != null) {
            Intent intent = registerReceiver(null, new IntentFilter("android.intent.action.DOCK_EVENT"));
            if (!(intent == null || intent.getIntExtra("android.intent.extra.DOCK_STATE", 0) == 0)) {
                BluetoothDevice dockedDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                if (dockedDevice != null && dockedDevice.equals(disconnectedDevice)) {
                    getCachedBluetoothDevice(dockedDevice).connectProfile(profile);
                }
            }
        }
        DockEventReceiver.finishStartingService(this, startId);
    }

    private synchronized void connectIfEnabled(BluetoothDevice device) {
        CachedBluetoothDevice cachedDevice = getCachedBluetoothDevice(device);
        for (LocalBluetoothProfile profile : cachedDevice.getConnectableProfiles()) {
            if (profile.getPreferred(device) == 1000) {
                cachedDevice.connect(false);
                return;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void applyBtSettings(BluetoothDevice device, int startId) {
        synchronized (this) {
            if (device != null) {
                if (this.mProfiles != null) {
                    if (!(this.mCheckedItems == null || this.mLocalAdapter == null)) {
                        for (boolean enable : this.mCheckedItems) {
                            if (enable) {
                                int btState = this.mLocalAdapter.getBluetoothState();
                                this.mLocalAdapter.enable();
                                if (btState != 12) {
                                    if (this.mPendingDevice == null || !this.mPendingDevice.equals(this.mDevice)) {
                                        this.mPendingDevice = device;
                                        this.mPendingStartId = startId;
                                        if (btState != 11) {
                                            getPrefs().edit().putBoolean("disable_bt_when_undock", true).apply();
                                        }
                                    } else {
                                        return;
                                    }
                                }
                            }
                        }
                        this.mPendingDevice = null;
                        boolean callConnect = false;
                        CachedBluetoothDevice cachedDevice = getCachedBluetoothDevice(device);
                        for (int i = 0; i < this.mProfiles.length; i++) {
                            LocalBluetoothProfile profile = this.mProfiles[i];
                            if (this.mCheckedItems[i]) {
                                callConnect = true;
                            } else if (!this.mCheckedItems[i] && profile.getConnectionStatus(cachedDevice.getDevice()) == 2) {
                                cachedDevice.disconnect(this.mProfiles[i]);
                            }
                            profile.setPreferred(device, this.mCheckedItems[i]);
                        }
                        if (callConnect) {
                            cachedDevice.connect(false);
                        }
                    }
                }
            }
        }
    }

    private synchronized void handleDocked(BluetoothDevice device, int state, int startId) {
        if (device != null) {
            if (LocalBluetoothPreferences.getDockAutoConnectSetting(this, device.getAddress())) {
                initBtSettings(device, state, false);
                applyBtSettings(this.mDevice, startId);
            }
        }
        createDialog(device, state, startId);
    }

    private synchronized void handleUndocked(BluetoothDevice device) {
        this.mRunnable = null;
        this.mProfileManager.removeServiceListener(this);
        if (this.mDialog != null) {
            this.mDialog.dismiss();
            this.mDialog = null;
        }
        this.mDevice = null;
        this.mPendingDevice = null;
        if (device != null) {
            getCachedBluetoothDevice(device).disconnect();
        }
    }

    private CachedBluetoothDevice getCachedBluetoothDevice(BluetoothDevice device) {
        CachedBluetoothDevice cachedDevice = this.mDeviceManager.findDevice(device);
        if (cachedDevice == null) {
            return this.mDeviceManager.addDevice(this.mLocalAdapter, this.mProfileManager, device);
        }
        return cachedDevice;
    }

    public synchronized void onServiceConnected() {
        if (this.mRunnable != null) {
            this.mRunnable.run();
            this.mRunnable = null;
            this.mProfileManager.removeServiceListener(this);
        }
    }

    public void onServiceDisconnected() {
    }
}
