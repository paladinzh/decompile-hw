package android.bluetooth.le;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCallbackWrapper;
import android.bluetooth.BluetoothUuid;
import android.bluetooth.IBluetoothGatt;
import android.bluetooth.IBluetoothManager;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BluetoothLeAdvertiser {
    private static final int FLAGS_FIELD_BYTES = 3;
    private static final int MANUFACTURER_SPECIFIC_DATA_LENGTH = 2;
    private static final int MAX_ADVERTISING_DATA_BYTES = 31;
    private static final int OVERHEAD_BYTES_PER_FIELD = 2;
    private static final int SERVICE_DATA_UUID_LENGTH = 2;
    private static final String TAG = "BluetoothLeAdvertiser";
    private BluetoothAdapter mBluetoothAdapter;
    private final IBluetoothManager mBluetoothManager;
    private final Handler mHandler;
    private final Map<AdvertiseCallback, AdvertiseCallbackWrapper> mLeAdvertisers = new HashMap();

    private class AdvertiseCallbackWrapper extends BluetoothGattCallbackWrapper {
        private static final int LE_CALLBACK_TIMEOUT_MILLIS = 2000;
        private final AdvertiseCallback mAdvertiseCallback;
        private final AdvertiseData mAdvertisement;
        private final IBluetoothGatt mBluetoothGatt;
        private int mClientIf;
        private boolean mIsAdvertising = false;
        private final AdvertiseData mScanResponse;
        private final AdvertiseSettings mSettings;

        public AdvertiseCallbackWrapper(AdvertiseCallback advertiseCallback, AdvertiseData advertiseData, AdvertiseData scanResponse, AdvertiseSettings settings, IBluetoothGatt bluetoothGatt) {
            this.mAdvertiseCallback = advertiseCallback;
            this.mAdvertisement = advertiseData;
            this.mScanResponse = scanResponse;
            this.mSettings = settings;
            this.mBluetoothGatt = bluetoothGatt;
            this.mClientIf = 0;
        }

        public void startRegisteration() {
            synchronized (this) {
                if (this.mClientIf == -1) {
                    return;
                }
                try {
                    this.mBluetoothGatt.registerClient(new ParcelUuid(UUID.randomUUID()), this);
                    wait(2000);
                } catch (Exception e) {
                    Log.e(BluetoothLeAdvertiser.TAG, "Failed to start registeration", e);
                }
                if (this.mClientIf > 0 && this.mIsAdvertising) {
                    BluetoothLeAdvertiser.this.mLeAdvertisers.put(this.mAdvertiseCallback, this);
                } else if (this.mClientIf <= 0) {
                    Log.d(BluetoothLeAdvertiser.TAG, "mClientIf = " + this.mClientIf + ", mIsAdvertising = " + this.mIsAdvertising);
                    if (this.mClientIf == 0) {
                        this.mClientIf = -1;
                    }
                    BluetoothLeAdvertiser.this.postStartFailure(this.mAdvertiseCallback, 4);
                } else {
                    Log.d(BluetoothLeAdvertiser.TAG, "mClientIf = " + this.mClientIf + ", mIsAdvertising = " + this.mIsAdvertising);
                    try {
                        this.mBluetoothGatt.unregisterClient(this.mClientIf);
                        this.mClientIf = -1;
                    } catch (RemoteException e2) {
                        Log.e(BluetoothLeAdvertiser.TAG, "remote exception when unregistering", e2);
                    }
                }
            }
        }

        public void stopAdvertising() {
            synchronized (this) {
                try {
                    this.mBluetoothGatt.stopMultiAdvertising(this.mClientIf);
                    wait(2000);
                } catch (Exception e) {
                    Log.e(BluetoothLeAdvertiser.TAG, "Failed to stop advertising", e);
                }
                if (BluetoothLeAdvertiser.this.mLeAdvertisers.containsKey(this.mAdvertiseCallback)) {
                    BluetoothLeAdvertiser.this.mLeAdvertisers.remove(this.mAdvertiseCallback);
                }
            }
        }

        public void updateAdvertiseInterval(int interval) {
            synchronized (this) {
                try {
                    this.mBluetoothGatt.updateAdvertiseInterval(this.mClientIf, interval);
                } catch (RemoteException e) {
                    Log.e(BluetoothLeAdvertiser.TAG, "Failed to updateAdvertiseInterval");
                }
            }
        }

        public void updateAdvertiseData(AdvertiseData data, boolean isScanResponse) {
            synchronized (this) {
                try {
                    this.mBluetoothGatt.updateAdvertiseData(this.mClientIf, data, isScanResponse);
                } catch (RemoteException e) {
                    Log.e(BluetoothLeAdvertiser.TAG, "Failed to updateAdvertiseData");
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onClientRegistered(int status, int clientIf) {
            Log.d(BluetoothLeAdvertiser.TAG, "onClientRegistered() - status=" + status + " clientIf=" + clientIf);
            synchronized (this) {
                if (status == 0) {
                    try {
                        if (this.mClientIf == -1) {
                            this.mBluetoothGatt.unregisterClient(clientIf);
                        } else {
                            this.mClientIf = clientIf;
                            this.mBluetoothGatt.startMultiAdvertising(this.mClientIf, this.mAdvertisement, this.mScanResponse, this.mSettings);
                        }
                    } catch (RemoteException e) {
                        Log.e(BluetoothLeAdvertiser.TAG, "failed to start advertising", e);
                    }
                }
                this.mClientIf = -1;
                notifyAll();
            }
        }

        public void onMultiAdvertiseCallback(int status, boolean isStart, AdvertiseSettings settings) {
            synchronized (this) {
                if (!isStart) {
                    try {
                        this.mBluetoothGatt.unregisterClient(this.mClientIf);
                        this.mClientIf = -1;
                        this.mIsAdvertising = false;
                        BluetoothLeAdvertiser.this.mLeAdvertisers.remove(this.mAdvertiseCallback);
                        Log.d(BluetoothLeAdvertiser.TAG, "Unregister client");
                    } catch (RemoteException e) {
                        Log.e(BluetoothLeAdvertiser.TAG, "remote exception when unregistering", e);
                    }
                } else if (status == 0) {
                    this.mIsAdvertising = true;
                    BluetoothLeAdvertiser.this.postStartSuccess(this.mAdvertiseCallback, settings);
                    Log.d(BluetoothLeAdvertiser.TAG, "Start success");
                } else {
                    BluetoothLeAdvertiser.this.postStartFailure(this.mAdvertiseCallback, status);
                    Log.d(BluetoothLeAdvertiser.TAG, "Start failure");
                }
                notifyAll();
            }
        }
    }

    public BluetoothLeAdvertiser(IBluetoothManager bluetoothManager) {
        this.mBluetoothManager = bluetoothManager;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mHandler = new Handler(Looper.getMainLooper());
    }

    public void startAdvertising(AdvertiseSettings settings, AdvertiseData advertiseData, AdvertiseCallback callback) {
        startAdvertising(settings, advertiseData, null, callback);
    }

    public void startAdvertising(AdvertiseSettings settings, AdvertiseData advertiseData, AdvertiseData scanResponse, AdvertiseCallback callback) {
        Log.i(TAG, "startAdvertising is called");
        synchronized (this.mLeAdvertisers) {
            BluetoothLeUtils.checkAdapterStateOn(this.mBluetoothAdapter);
            if (callback == null) {
                throw new IllegalArgumentException("callback cannot be null");
            } else if (!this.mBluetoothAdapter.isMultipleAdvertisementSupported() && !this.mBluetoothAdapter.isPeripheralModeSupported()) {
                postStartFailure(callback, 5);
            } else if (totalBytes(advertiseData, settings.isConnectable()) > 31 || totalBytes(scanResponse, false) > 31) {
                postStartFailure(callback, 1);
            } else if (this.mLeAdvertisers.containsKey(callback)) {
                postStartFailure(callback, 3);
            } else {
                try {
                    new AdvertiseCallbackWrapper(callback, advertiseData, scanResponse, settings, this.mBluetoothManager.getBluetoothGatt()).startRegisteration();
                } catch (RemoteException e) {
                    Log.e(TAG, "Failed to get Bluetooth gatt - ", e);
                    postStartFailure(callback, 4);
                }
            }
        }
    }

    public void stopAdvertising(AdvertiseCallback callback) {
        Log.i(TAG, "stopAdvertising is called");
        synchronized (this.mLeAdvertisers) {
            if (callback == null) {
                throw new IllegalArgumentException("callback cannot be null");
            }
            AdvertiseCallbackWrapper wrapper = (AdvertiseCallbackWrapper) this.mLeAdvertisers.get(callback);
            if (wrapper == null) {
                Log.d(TAG, "wrap is null can not stop");
                return;
            }
            wrapper.stopAdvertising();
        }
    }

    public void updateAdvertiseInterval(AdvertiseCallback callback, int interval) {
        Log.d(TAG, "ruby in updateAdvertiseInterval");
        synchronized (this.mLeAdvertisers) {
            BluetoothLeUtils.checkAdapterStateOn(this.mBluetoothAdapter);
            if (callback == null) {
                throw new IllegalArgumentException("callback cannot be null");
            }
            AdvertiseCallbackWrapper wrapper = (AdvertiseCallbackWrapper) this.mLeAdvertisers.get(callback);
            if (wrapper == null) {
                Log.d(TAG, "wrap is null can not start");
                return;
            }
            Log.d(TAG, "ruby start real updateAdvertiseInterval");
            wrapper.updateAdvertiseInterval(interval);
        }
    }

    public void updateAdvertiseData(AdvertiseCallback callback, AdvertiseData data, boolean isScanResponse) {
        Log.d(TAG, "ruby in updateAdvertiseData isScanResponse:" + isScanResponse);
        synchronized (this.mLeAdvertisers) {
            BluetoothLeUtils.checkAdapterStateOn(this.mBluetoothAdapter);
            if (callback == null) {
                throw new IllegalArgumentException("callback cannot be null");
            }
            AdvertiseCallbackWrapper wrapper = (AdvertiseCallbackWrapper) this.mLeAdvertisers.get(callback);
            if (wrapper == null) {
                Log.d(TAG, "wrap is null can not start");
                return;
            }
            Log.d(TAG, "ruby start real updateAdvertiseData");
            wrapper.updateAdvertiseData(data, isScanResponse);
        }
    }

    public void cleanup() {
        this.mLeAdvertisers.clear();
    }

    private int totalBytes(AdvertiseData data, boolean isFlagsIncluded) {
        if (data == null) {
            return 0;
        }
        int size = isFlagsIncluded ? 3 : 0;
        if (data.getServiceUuids() != null) {
            int num16BitUuids = 0;
            int num32BitUuids = 0;
            int num128BitUuids = 0;
            for (ParcelUuid uuid : data.getServiceUuids()) {
                if (BluetoothUuid.is16BitUuid(uuid)) {
                    num16BitUuids++;
                } else if (BluetoothUuid.is32BitUuid(uuid)) {
                    num32BitUuids++;
                } else {
                    num128BitUuids++;
                }
            }
            if (num16BitUuids != 0) {
                size += (num16BitUuids * 2) + 2;
            }
            if (num32BitUuids != 0) {
                size += (num32BitUuids * 4) + 2;
            }
            if (num128BitUuids != 0) {
                size += (num128BitUuids * 16) + 2;
            }
        }
        for (ParcelUuid uuid2 : data.getServiceData().keySet()) {
            size += byteLength((byte[]) data.getServiceData().get(uuid2)) + 4;
        }
        for (int i = 0; i < data.getManufacturerSpecificData().size(); i++) {
            size += byteLength((byte[]) data.getManufacturerSpecificData().valueAt(i)) + 4;
        }
        if (data.getIncludeTxPowerLevel()) {
            size += 3;
        }
        if (data.getIncludeDeviceName() && this.mBluetoothAdapter.getName() != null) {
            size += this.mBluetoothAdapter.getName().length() + 2;
        }
        return size;
    }

    private int byteLength(byte[] array) {
        return array == null ? 0 : array.length;
    }

    private void postStartFailure(final AdvertiseCallback callback, final int error) {
        this.mHandler.post(new Runnable() {
            public void run() {
                callback.onStartFailure(error);
            }
        });
    }

    private void postStartSuccess(final AdvertiseCallback callback, final AdvertiseSettings settings) {
        this.mHandler.post(new Runnable() {
            public void run() {
                callback.onStartSuccess(settings);
            }
        });
    }
}
