package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothUuid;
import android.os.ParcelUuid;
import android.util.Log;

public final class BluetoothDeviceFilter {
    public static final Filter ALL_FILTER = new AllFilter();
    public static final Filter BONDED_DEVICE_FILTER = new BondedDeviceFilter();
    private static final Filter[] FILTERS = new Filter[]{ALL_FILTER, new AudioFilter(), new TransferFilter(), new PanuFilter(), new NapFilter()};
    public static final Filter UNBONDED_DEVICE_FILTER = new UnbondedDeviceFilter();

    public interface Filter {
        boolean matches(BluetoothDevice bluetoothDevice);
    }

    private static final class AllFilter implements Filter {
        private AllFilter() {
        }

        public boolean matches(BluetoothDevice device) {
            return true;
        }
    }

    private static abstract class ClassUuidFilter implements Filter {
        abstract boolean matches(ParcelUuid[] parcelUuidArr, BluetoothClass bluetoothClass);

        private ClassUuidFilter() {
        }

        public boolean matches(BluetoothDevice device) {
            return matches(device.getUuids(), device.getBluetoothClass());
        }
    }

    private static final class AudioFilter extends ClassUuidFilter {
        private AudioFilter() {
            super();
        }

        boolean matches(ParcelUuid[] uuids, BluetoothClass btClass) {
            if (uuids != null) {
                return BluetoothUuid.containsAnyUuid(uuids, A2dpProfile.SINK_UUIDS) || BluetoothUuid.containsAnyUuid(uuids, HeadsetProfile.UUIDS);
            } else {
                if (btClass != null && (btClass.doesClassMatch(1) || btClass.doesClassMatch(0))) {
                    return true;
                }
            }
        }
    }

    private static final class BondedDeviceFilter implements Filter {
        private BondedDeviceFilter() {
        }

        public boolean matches(BluetoothDevice device) {
            return device.getBondState() == 12;
        }
    }

    private static final class NapFilter extends ClassUuidFilter {
        private NapFilter() {
            super();
        }

        boolean matches(ParcelUuid[] uuids, BluetoothClass btClass) {
            if (uuids != null && BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.NAP)) {
                return true;
            }
            boolean doesClassMatch;
            if (btClass != null) {
                doesClassMatch = btClass.doesClassMatch(5);
            } else {
                doesClassMatch = false;
            }
            return doesClassMatch;
        }
    }

    private static final class PanuFilter extends ClassUuidFilter {
        private PanuFilter() {
            super();
        }

        boolean matches(ParcelUuid[] uuids, BluetoothClass btClass) {
            if (uuids != null && BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.PANU)) {
                return true;
            }
            boolean doesClassMatch;
            if (btClass != null) {
                doesClassMatch = btClass.doesClassMatch(4);
            } else {
                doesClassMatch = false;
            }
            return doesClassMatch;
        }
    }

    private static final class TransferFilter extends ClassUuidFilter {
        private TransferFilter() {
            super();
        }

        boolean matches(ParcelUuid[] uuids, BluetoothClass btClass) {
            if (uuids != null && BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.ObexObjectPush)) {
                return true;
            }
            boolean doesClassMatch;
            if (btClass != null) {
                doesClassMatch = btClass.doesClassMatch(2);
            } else {
                doesClassMatch = false;
            }
            return doesClassMatch;
        }
    }

    private static final class UnbondedDeviceFilter implements Filter {
        private UnbondedDeviceFilter() {
        }

        public boolean matches(BluetoothDevice device) {
            return device.getBondState() != 12;
        }
    }

    private BluetoothDeviceFilter() {
    }

    public static Filter getFilter(int filterType) {
        if (filterType >= 0 && filterType < FILTERS.length) {
            return FILTERS[filterType];
        }
        Log.w("BluetoothDeviceFilter", "Invalid filter type " + filterType + " for device picker");
        return ALL_FILTER;
    }
}
