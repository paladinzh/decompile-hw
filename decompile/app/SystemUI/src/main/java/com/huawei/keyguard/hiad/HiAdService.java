package com.huawei.keyguard.hiad;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import com.huawei.keyguard.hiad.IHiAdService.Stub;
import com.huawei.openalliance.ad.inter.HiAdMagLock;
import com.huawei.openalliance.ad.inter.HiAdMagLock.Builder;

public class HiAdService extends Service {
    private HiAdMagLock hiAdMagLock = null;
    private final Stub mBinder = new Stub() {
        public int transferHiAdInfo(HiAdInfo hiadInfo) throws RemoteException {
            if (hiadInfo != null) {
                HiAdService.this.hiAdMagLock.updateMagLockInfo(HiAdService.this, hiadInfo.convertToAdInfo(), false);
                HiAdService.this.stopSelf();
                return 1;
            }
            HiAdService.this.stopSelf();
            return -1;
        }
    };

    public void onCreate() {
        super.onCreate();
        this.hiAdMagLock = new Builder().build();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return 2;
    }

    public IBinder onBind(Intent intent) {
        String[] callingApps = getApplicationContext().getPackageManager().getPackagesForUid(Binder.getCallingUid());
        boolean re = false;
        if (callingApps != null) {
            for (Object equals : callingApps) {
                if ("com.android.keyguard".equals(equals)) {
                    re = true;
                    break;
                }
            }
        }
        if (re) {
            return this.mBinder;
        }
        return null;
    }
}
