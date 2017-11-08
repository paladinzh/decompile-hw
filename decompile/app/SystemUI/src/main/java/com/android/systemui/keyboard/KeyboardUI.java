package com.android.systemui.keyboard;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanFilter.Builder;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Configuration;
import android.hardware.input.InputManager;
import android.hardware.input.InputManager.OnTabletModeChangedListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Pair;
import android.widget.Toast;
import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.CachedBluetoothDeviceManager;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.bluetooth.LocalBluetoothProfileManager;
import com.android.settingslib.bluetooth.Utils;
import com.android.settingslib.bluetooth.Utils.ErrorListener;
import com.android.systemui.R;
import com.android.systemui.SystemUI;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class KeyboardUI extends SystemUI implements OnTabletModeChangedListener {
    private boolean mBootCompleted;
    private long mBootCompletedTime;
    private CachedBluetoothDeviceManager mCachedDeviceManager;
    protected volatile Context mContext;
    private BluetoothDialog mDialog;
    private boolean mEnabled;
    private volatile KeyboardHandler mHandler;
    private int mInTabletMode = -1;
    private String mKeyboardName;
    private LocalBluetoothAdapter mLocalBluetoothAdapter;
    private LocalBluetoothProfileManager mProfileManager;
    private int mScanAttempt = 0;
    private ScanCallback mScanCallback;
    private int mState;
    private volatile KeyboardUIHandler mUIHandler;

    private final class BluetoothCallbackHandler implements BluetoothCallback {
        private BluetoothCallbackHandler() {
        }

        public void onBluetoothStateChanged(int bluetoothState) {
            KeyboardUI.this.mHandler.obtainMessage(4, bluetoothState, 0).sendToTarget();
        }

        public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState) {
            KeyboardUI.this.mHandler.obtainMessage(5, bondState, 0, cachedDevice).sendToTarget();
        }

        public void onDeviceAdded(CachedBluetoothDevice cachedDevice) {
        }

        public void onDeviceDeleted(CachedBluetoothDevice cachedDevice) {
        }

        public void onScanningStateChanged(boolean started) {
        }

        public void onConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state) {
        }
    }

    private final class BluetoothDialogClickListener implements OnClickListener {
        private BluetoothDialogClickListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            KeyboardUI.this.mHandler.obtainMessage(3, -1 == which ? 1 : 0, 0).sendToTarget();
            KeyboardUI.this.mDialog = null;
        }
    }

    private final class BluetoothDialogDismissListener implements OnDismissListener {
        private BluetoothDialogDismissListener() {
        }

        public void onDismiss(DialogInterface dialog) {
            KeyboardUI.this.mDialog = null;
        }
    }

    private final class BluetoothErrorListener implements ErrorListener {
        private BluetoothErrorListener() {
        }

        public void onShowError(Context context, String name, int messageResId) {
            KeyboardUI.this.mHandler.obtainMessage(11, messageResId, 0, new Pair(context, name)).sendToTarget();
        }
    }

    private final class KeyboardHandler extends Handler {
        public KeyboardHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            boolean enable = true;
            switch (msg.what) {
                case 0:
                    KeyboardUI.this.init();
                    return;
                case 1:
                    KeyboardUI.this.onBootCompletedInternal();
                    return;
                case 2:
                    KeyboardUI.this.processKeyboardState();
                    return;
                case 3:
                    if (msg.arg1 != 1) {
                        enable = false;
                    }
                    if (enable) {
                        KeyboardUI.this.mLocalBluetoothAdapter.enable();
                        return;
                    } else {
                        KeyboardUI.this.mState = 8;
                        return;
                    }
                case 4:
                    KeyboardUI.this.onBluetoothStateChangedInternal(msg.arg1);
                    return;
                case 5:
                    KeyboardUI.this.onDeviceBondStateChangedInternal(msg.obj, msg.arg1);
                    return;
                case 6:
                    KeyboardUI.this.onDeviceAddedInternal(KeyboardUI.this.getCachedBluetoothDevice(msg.obj));
                    return;
                case 7:
                    KeyboardUI.this.onBleScanFailedInternal();
                    return;
                case 10:
                    KeyboardUI.this.bleAbortScanInternal(msg.arg1);
                    return;
                case 11:
                    Pair<Context, String> p = msg.obj;
                    KeyboardUI.this.onShowErrorInternal((Context) p.first, (String) p.second, msg.arg1);
                    return;
                default:
                    return;
            }
        }
    }

    private final class KeyboardScanCallback extends ScanCallback {
        private KeyboardScanCallback() {
        }

        private boolean isDeviceDiscoverable(ScanResult result) {
            if ((result.getScanRecord().getAdvertiseFlags() & 3) != 0) {
                return true;
            }
            return false;
        }

        public void onBatchScanResults(List<ScanResult> results) {
            Object bestDevice = null;
            int bestRssi = Integer.MIN_VALUE;
            for (ScanResult result : results) {
                if (isDeviceDiscoverable(result) && result.getRssi() > bestRssi) {
                    bestDevice = result.getDevice();
                    bestRssi = result.getRssi();
                }
            }
            if (bestDevice != null) {
                KeyboardUI.this.mHandler.obtainMessage(6, bestDevice).sendToTarget();
            }
        }

        public void onScanFailed(int errorCode) {
            KeyboardUI.this.mHandler.obtainMessage(7).sendToTarget();
        }

        public void onScanResult(int callbackType, ScanResult result) {
            if (isDeviceDiscoverable(result)) {
                KeyboardUI.this.mHandler.obtainMessage(6, result.getDevice()).sendToTarget();
            }
        }
    }

    private final class KeyboardUIHandler extends Handler {
        public KeyboardUIHandler() {
            super(Looper.getMainLooper(), null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 8:
                    if (KeyboardUI.this.mDialog == null) {
                        OnClickListener clickListener = new BluetoothDialogClickListener();
                        OnDismissListener dismissListener = new BluetoothDialogDismissListener();
                        KeyboardUI.this.mDialog = new BluetoothDialog(KeyboardUI.this.mContext);
                        KeyboardUI.this.mDialog.setTitle(R.string.enable_bluetooth_title);
                        KeyboardUI.this.mDialog.setMessage(R.string.enable_bluetooth_message);
                        KeyboardUI.this.mDialog.setPositiveButton(R.string.enable_bluetooth_confirmation_ok, clickListener);
                        KeyboardUI.this.mDialog.setNegativeButton(17039360, clickListener);
                        KeyboardUI.this.mDialog.setOnDismissListener(dismissListener);
                        KeyboardUI.this.mDialog.show();
                        return;
                    }
                    return;
                case 9:
                    if (KeyboardUI.this.mDialog != null) {
                        KeyboardUI.this.mDialog.dismiss();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public void start() {
        this.mContext = this.mContext;
        HandlerThread thread = new HandlerThread("Keyboard", 10);
        thread.start();
        this.mHandler = new KeyboardHandler(thread.getLooper());
        this.mHandler.sendEmptyMessage(0);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("KeyboardUI:");
        pw.println("  mEnabled=" + this.mEnabled);
        pw.println("  mBootCompleted=" + this.mEnabled);
        pw.println("  mBootCompletedTime=" + this.mBootCompletedTime);
        pw.println("  mKeyboardName=" + this.mKeyboardName);
        pw.println("  mInTabletMode=" + this.mInTabletMode);
        pw.println("  mState=" + stateToString(this.mState));
    }

    protected void onBootCompleted() {
        this.mHandler.sendEmptyMessage(1);
    }

    public void onTabletModeChanged(long whenNanos, boolean inTabletMode) {
        int i = 1;
        if (!inTabletMode || this.mInTabletMode == 1) {
            if (inTabletMode || this.mInTabletMode == 0) {
                return;
            }
        }
        if (!inTabletMode) {
            i = 0;
        }
        this.mInTabletMode = i;
        processKeyboardState();
    }

    private void init() {
        Context context = this.mContext;
        this.mKeyboardName = context.getString(17039468);
        if (!TextUtils.isEmpty(this.mKeyboardName)) {
            LocalBluetoothManager bluetoothManager = LocalBluetoothManager.getInstance(context, null);
            if (bluetoothManager != null) {
                this.mEnabled = true;
                this.mCachedDeviceManager = bluetoothManager.getCachedDeviceManager();
                this.mLocalBluetoothAdapter = bluetoothManager.getBluetoothAdapter();
                this.mProfileManager = bluetoothManager.getProfileManager();
                bluetoothManager.getEventManager().registerCallback(new BluetoothCallbackHandler());
                Utils.setErrorListener(new BluetoothErrorListener());
                InputManager im = (InputManager) context.getSystemService(InputManager.class);
                im.registerOnTabletModeChangedListener(this, this.mHandler);
                this.mInTabletMode = im.isInTabletMode();
                processKeyboardState();
                this.mUIHandler = new KeyboardUIHandler();
            }
        }
    }

    private void processKeyboardState() {
        this.mHandler.removeMessages(2);
        if (!this.mEnabled) {
            this.mState = -1;
        } else if (!this.mBootCompleted) {
            this.mState = 1;
        } else if (this.mInTabletMode != 0) {
            if (this.mState == 3) {
                stopScanning();
            } else if (this.mState == 4) {
                this.mUIHandler.sendEmptyMessage(9);
            }
            this.mState = 2;
        } else {
            int btState = this.mLocalBluetoothAdapter.getState();
            if ((btState == 11 || btState == 12) && this.mState == 4) {
                this.mUIHandler.sendEmptyMessage(9);
            }
            if (btState == 11) {
                this.mState = 4;
            } else if (btState != 12) {
                this.mState = 4;
                showBluetoothDialog();
            } else {
                CachedBluetoothDevice device = getPairedKeyboard();
                if (this.mState == 2 || this.mState == 4) {
                    if (device != null) {
                        this.mState = 6;
                        device.connect(false);
                        return;
                    }
                    this.mCachedDeviceManager.clearNonBondedDevices();
                }
                device = getDiscoveredKeyboard();
                if (device != null) {
                    this.mState = 5;
                    device.startPairing();
                } else {
                    this.mState = 3;
                    startScanning();
                }
            }
        }
    }

    public void onBootCompletedInternal() {
        this.mBootCompleted = true;
        this.mBootCompletedTime = SystemClock.uptimeMillis();
        if (this.mState == 1) {
            processKeyboardState();
        }
    }

    private void showBluetoothDialog() {
        if (isUserSetupComplete()) {
            long earliestDialogTime = this.mBootCompletedTime + 10000;
            if (earliestDialogTime < SystemClock.uptimeMillis()) {
                this.mUIHandler.sendEmptyMessage(8);
                return;
            } else {
                this.mHandler.sendEmptyMessageAtTime(2, earliestDialogTime);
                return;
            }
        }
        this.mLocalBluetoothAdapter.enable();
    }

    private boolean isUserSetupComplete() {
        if (Secure.getIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 0, -2) != 0) {
            return true;
        }
        return false;
    }

    private CachedBluetoothDevice getPairedKeyboard() {
        for (BluetoothDevice d : this.mLocalBluetoothAdapter.getBondedDevices()) {
            if (this.mKeyboardName.equals(d.getName())) {
                return getCachedBluetoothDevice(d);
            }
        }
        return null;
    }

    private CachedBluetoothDevice getDiscoveredKeyboard() {
        for (CachedBluetoothDevice d : this.mCachedDeviceManager.getCachedDevicesCopy()) {
            if (d.getName().equals(this.mKeyboardName)) {
                return d;
            }
        }
        return null;
    }

    private CachedBluetoothDevice getCachedBluetoothDevice(BluetoothDevice d) {
        CachedBluetoothDevice cachedDevice = this.mCachedDeviceManager.findDevice(d);
        if (cachedDevice == null) {
            return this.mCachedDeviceManager.addDevice(this.mLocalBluetoothAdapter, this.mProfileManager, d);
        }
        return cachedDevice;
    }

    private void startScanning() {
        BluetoothLeScanner scanner = this.mLocalBluetoothAdapter.getBluetoothLeScanner();
        ScanFilter filter = new Builder().setDeviceName(this.mKeyboardName).build();
        ScanSettings settings = new ScanSettings.Builder().setCallbackType(1).setNumOfMatches(1).setScanMode(2).setReportDelay(0).build();
        this.mScanCallback = new KeyboardScanCallback();
        scanner.startScan(Arrays.asList(new ScanFilter[]{filter}), settings, this.mScanCallback);
        KeyboardHandler keyboardHandler = this.mHandler;
        int i = this.mScanAttempt + 1;
        this.mScanAttempt = i;
        this.mHandler.sendMessageDelayed(keyboardHandler.obtainMessage(10, i, 0), 30000);
    }

    private void stopScanning() {
        if (this.mScanCallback != null) {
            BluetoothLeScanner scanner = this.mLocalBluetoothAdapter.getBluetoothLeScanner();
            if (scanner != null) {
                scanner.stopScan(this.mScanCallback);
            }
            this.mScanCallback = null;
        }
    }

    private void bleAbortScanInternal(int scanAttempt) {
        if (this.mState == 3 && scanAttempt == this.mScanAttempt) {
            stopScanning();
            this.mState = 9;
        }
    }

    private void onDeviceAddedInternal(CachedBluetoothDevice d) {
        if (this.mState == 3 && d.getName().equals(this.mKeyboardName)) {
            stopScanning();
            d.startPairing();
            this.mState = 5;
        }
    }

    private void onBluetoothStateChangedInternal(int bluetoothState) {
        if (bluetoothState == 12 && this.mState == 4) {
            processKeyboardState();
        }
    }

    private void onDeviceBondStateChangedInternal(CachedBluetoothDevice d, int bondState) {
        if (this.mState != 5 || !d.getName().equals(this.mKeyboardName)) {
            return;
        }
        if (bondState == 12) {
            this.mState = 6;
        } else if (bondState == 10) {
            this.mState = 7;
        }
    }

    private void onBleScanFailedInternal() {
        this.mScanCallback = null;
        if (this.mState == 3) {
            this.mState = 9;
        }
    }

    private void onShowErrorInternal(Context context, String name, int messageResId) {
        if ((this.mState == 5 || this.mState == 7) && this.mKeyboardName.equals(name)) {
            Toast.makeText(context, context.getString(messageResId, new Object[]{name}), 0).show();
        }
    }

    private static String stateToString(int state) {
        switch (state) {
            case -1:
                return "STATE_NOT_ENABLED";
            case 1:
                return "STATE_WAITING_FOR_BOOT_COMPLETED";
            case 2:
                return "STATE_WAITING_FOR_TABLET_MODE_EXIT";
            case 3:
                return "STATE_WAITING_FOR_DEVICE_DISCOVERY";
            case 4:
                return "STATE_WAITING_FOR_BLUETOOTH";
            case 5:
                return "STATE_PAIRING";
            case 6:
                return "STATE_PAIRED";
            case 7:
                return "STATE_PAIRING_FAILED";
            case 8:
                return "STATE_USER_CANCELLED";
            case 9:
                return "STATE_DEVICE_NOT_FOUND";
            default:
                return "STATE_UNKNOWN (" + state + ")";
        }
    }
}
