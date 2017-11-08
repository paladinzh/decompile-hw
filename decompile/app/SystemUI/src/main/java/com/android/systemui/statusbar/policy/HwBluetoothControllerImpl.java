package com.android.systemui.statusbar.policy;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.os.UserHandle;
import com.android.systemui.R;
import com.android.systemui.tint.TintManager;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import java.util.HashMap;
import java.util.List;

public class HwBluetoothControllerImpl extends BluetoothControllerImpl {
    private final int[] BLUETOOTH_EAR_IMAGE_BLACK = new int[]{R.drawable.stat_sys_data_bluetooth_battery1_black, R.drawable.stat_sys_data_bluetooth_battery2_black, R.drawable.stat_sys_data_bluetooth_battery3_black, R.drawable.stat_sys_data_bluetooth_battery4_black, R.drawable.stat_sys_data_bluetooth_battery5_black};
    private final int[] BLUETOOTH_EAR_IMAGE_WHITE = new int[]{R.drawable.stat_sys_data_bluetooth_battery1, R.drawable.stat_sys_data_bluetooth_battery2, R.drawable.stat_sys_data_bluetooth_battery3, R.drawable.stat_sys_data_bluetooth_battery4, R.drawable.stat_sys_data_bluetooth_battery5};
    private int mBluetoothBatteryIconId = 0;
    private BroadcastReceiver mBluetoothBatteryReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                HwLog.e("HwBluetoothControllerImpl", "mBluetoothBatteryReceiver::onReceive: intent or action is null!");
                return;
            }
            String action = intent.getAction();
            HwLog.i("HwBluetoothControllerImpl", "mBluetoothBatteryReceiver::onReceive:action=" + action);
            if ("android.bluetooth.headset.action.VENDOR_SPECIFIC_HEADSET_EVENT".equals(action)) {
                int level = intent.getIntExtra("huawei.android.bluetooth.TANSFER_BATTERY_LEVEL", -1);
                if (level > 0) {
                    HwBluetoothControllerImpl.this.updateBluetoothEarBattery(level);
                }
            } else if ("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED".equals(action)) {
                HwBluetoothControllerImpl.this.mBluetoothEarBattery = false;
                HwBluetoothControllerImpl.this.mHandler.sendEmptyMessage(2);
            }
        }
    };
    private boolean mBluetoothEarBattery = false;
    private HashMap<Integer, Integer> mBluetoothEarBlackImgMap = new HashMap();
    private boolean mBluetoothTransferEnable = false;
    private BroadcastReceiver mBluetoothTransferReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                HwLog.e("HwBluetoothControllerImpl", "mBluetoothTransferReceiver::onReceive: intent or action is null!");
                return;
            }
            String action = intent.getAction();
            HwLog.i("HwBluetoothControllerImpl", "mBluetoothBatteryReceiver::onReceive:action=" + action);
            if ("com.huawei.bluetooth.action.TRANSFER_PROGRESS".equals(action)) {
                int status = intent.getIntExtra("status", 0);
                if (status == 0) {
                    HwBluetoothControllerImpl.this.mBluetoothTransferEnable = false;
                } else {
                    HwBluetoothControllerImpl.this.mBluetoothTransferEnable = true;
                }
                HwBluetoothControllerImpl.this.mHandler.sendEmptyMessage(2);
                HwLog.i("HwBluetoothControllerImpl", "mBluetoothBatteryReceiver::status=" + status + ", mBluetoothTransferEnable=" + HwBluetoothControllerImpl.this.mBluetoothTransferEnable);
            }
        }
    };
    private Context mContext;
    private ServiceListener mServiceStateListener = new ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            HwLog.i("HwBluetoothControllerImpl", "onServiceConnected -> update bluetooth battery usage manually! profile=" + profile);
            if (profile == 1) {
                final BluetoothHeadset bluetoothHeadset = (BluetoothHeadset) proxy;
                SystemUIThread.runAsync(new SimpleAsyncTask() {
                    int level = -1;

                    public boolean runInThread() {
                        boolean z = true;
                        List<BluetoothDevice> deviceList = bluetoothHeadset.getConnectedDevices();
                        if (deviceList == null || deviceList.size() < 1) {
                            return false;
                        }
                        BluetoothDevice bluetoothHeadsetDevice = (BluetoothDevice) deviceList.get(0);
                        if (bluetoothHeadsetDevice != null) {
                            this.level = bluetoothHeadset.getBatteryUsageHint(bluetoothHeadsetDevice);
                        }
                        if (this.level <= 0) {
                            z = false;
                        }
                        return z;
                    }

                    public void runInUI() {
                        HwBluetoothControllerImpl.this.updateBluetoothEarBattery(this.level);
                    }
                });
            }
        }

        public void onServiceDisconnected(int profile) {
        }
    };

    public HwBluetoothControllerImpl(Context context, Looper bgLooper) {
        super(context, bgLooper);
        this.mContext = context;
        init();
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.bluetooth.headset.action.VENDOR_SPECIFIC_HEADSET_EVENT");
        filter.addAction("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED");
        filter.addCategory("android.bluetooth.headset.intent.category.companyid.85");
        filter.addCategory("android.bluetooth.headset.intent.category.companyid.76");
        this.mContext.registerReceiverAsUser(this.mBluetoothBatteryReceiver, UserHandle.ALL, filter, null, null);
        IntentFilter transferFilter = new IntentFilter();
        transferFilter.addAction("com.huawei.bluetooth.action.TRANSFER_PROGRESS");
        this.mContext.registerReceiverAsUser(this.mBluetoothTransferReceiver, UserHandle.ALL, transferFilter, null, null);
    }

    private void registerListener() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            adapter.getProfileProxy(this.mContext, this.mServiceStateListener, 1);
        }
    }

    private final void updateBluetoothEarBattery(int level) {
        if (level < 1 || level > this.BLUETOOTH_EAR_IMAGE_WHITE.length) {
            HwLog.e("HwBluetoothControllerImpl", "updateBluetoothEarBattery::level " + level + " is invalid!");
            return;
        }
        this.mBluetoothBatteryIconId = this.BLUETOOTH_EAR_IMAGE_WHITE[level - 1];
        this.mBluetoothEarBattery = true;
        HwLog.i("HwBluetoothControllerImpl", "updateBluetoothEarBattery:::setBluetoothBatteryEnable true!");
        this.mHandler.sendEmptyMessage(2);
    }

    public int getSuggestBluetoothIcon(int orginIconId) {
        int tmpIconId = orginIconId;
        if (this.mBluetoothEarBattery) {
            HwLog.i("HwBluetoothControllerImpl", "getSuggestBluetoothIcon::enable bluetoothEar battery.");
            tmpIconId = this.mBluetoothBatteryIconId;
        }
        if (TintManager.getInstance().isStatusBarBlack()) {
            tmpIconId = ((Integer) this.mBluetoothEarBlackImgMap.get(Integer.valueOf(tmpIconId))).intValue();
        }
        return Integer.valueOf(tmpIconId).intValue();
    }

    private void initBluetoothIcons() {
        this.mBluetoothEarBlackImgMap.put(Integer.valueOf(R.drawable.stat_sys_data_bluetooth), Integer.valueOf(R.drawable.stat_sys_data_bluetooth_black));
        this.mBluetoothEarBlackImgMap.put(Integer.valueOf(R.drawable.stat_sys_data_bluetooth_connected), Integer.valueOf(R.drawable.stat_sys_data_bluetooth_connected_black));
        for (int i = 0; i < this.BLUETOOTH_EAR_IMAGE_WHITE.length; i++) {
            this.mBluetoothEarBlackImgMap.put(Integer.valueOf(this.BLUETOOTH_EAR_IMAGE_WHITE[i]), Integer.valueOf(this.BLUETOOTH_EAR_IMAGE_BLACK[i]));
        }
    }

    private void init() {
        HwLog.i("HwBluetoothControllerImpl", "init");
        registerReceiver();
        registerListener();
        initBluetoothIcons();
    }

    public void setBluetoothBatteryEnable(boolean enable) {
        this.mBluetoothEarBattery = enable;
    }

    public boolean isBluetoothTransfering() {
        return this.mBluetoothTransferEnable;
    }
}
