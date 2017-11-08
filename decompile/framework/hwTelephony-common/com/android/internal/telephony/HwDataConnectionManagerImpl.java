package com.android.internal.telephony;

import android.content.Context;
import android.database.DatabaseUtils;
import android.net.IConnectivityManager;
import android.net.IConnectivityManager.Stub;
import android.net.TrafficStats;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.util.ArraySet;
import com.android.internal.telephony.dataconnection.AbstractDcTrackerBase;
import com.android.internal.telephony.dataconnection.AbstractDcTrackerBase.DcTrackerBaseReference;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.dataconnection.HwDcTrackerBaseReference;
import com.android.internal.telephony.intelligentdataswitch.IntelligentDataSwitch;
import java.util.HashMap;

public class HwDataConnectionManagerImpl implements HwDataConnectionManager {
    private static int CONNECTIVITY_SERVICE_NEED_SET_USER_DATA = 1100;
    private static HwDataConnectionManager mInstance = new HwDataConnectionManagerImpl();
    private static IntelligentDataSwitch mIntelligentDataSwitch = null;

    public DcTrackerBaseReference createHwDcTrackerBaseReference(AbstractDcTrackerBase dcTrackerBase) {
        return new HwDcTrackerBaseReference((DcTracker) dcTrackerBase);
    }

    public static HwDataConnectionManager getDefault() {
        return mInstance;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean needSetUserDataEnabled(boolean enabled) {
        boolean z = true;
        IConnectivityManager cm = Stub.asInterface(ServiceManager.getService("connectivity"));
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            IBinder connectivityServiceBinder = cm.asBinder();
            if (connectivityServiceBinder != null) {
                int i;
                data.writeInterfaceToken("android.net.IConnectivityManager");
                if (enabled) {
                    i = 1;
                } else {
                    i = 0;
                }
                data.writeInt(i);
                connectivityServiceBinder.transact(CONNECTIVITY_SERVICE_NEED_SET_USER_DATA + 1, data, reply, 0);
            }
            DatabaseUtils.readExceptionFromParcel(reply);
            int result = reply.readInt();
            Rlog.d("HwDataConnectionManager", "needSetUserDataEnabled result = " + result);
            if (result != 1) {
                z = false;
            }
            reply.recycle();
            data.recycle();
            return z;
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
            return true;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    public void createIntelligentDataSwitch(Context context) {
        if (SystemProperties.getBoolean("ro.hwpp.autodds", false)) {
            mIntelligentDataSwitch = new IntelligentDataSwitch(context);
            if (mIntelligentDataSwitch == null) {
                Rlog.e("HwDataConnectionManager", "mIntelligentDataSwitch start error");
            }
        }
    }

    public long getThisModemMobileTxPackets(HashMap<String, Integer> mIfacePhoneHashMap, int phoneId) {
        long total = 0;
        for (String iface : getThisModemMobileIfaces(mIfacePhoneHashMap, phoneId)) {
            total += TrafficStats.getTxPackets(iface);
        }
        return total;
    }

    public long getThisModemMobileRxPackets(HashMap<String, Integer> mIfacePhoneHashMap, int phoneId) {
        long total = 0;
        for (String iface : getThisModemMobileIfaces(mIfacePhoneHashMap, phoneId)) {
            total += TrafficStats.getRxPackets(iface);
        }
        return total;
    }

    private String[] getThisModemMobileIfaces(HashMap<String, Integer> mIfacePhoneHashMap, int phoneId) {
        ArraySet<String> mobileIfaces = new ArraySet();
        for (String iface : TrafficStats.getMobileIfaces()) {
            if (mIfacePhoneHashMap.get(iface) == null || ((Integer) mIfacePhoneHashMap.get(iface)).equals(Integer.valueOf(phoneId))) {
                mobileIfaces.add(iface);
            }
        }
        return (String[]) mobileIfaces.toArray(new String[mobileIfaces.size()]);
    }
}
