package tmsdkobf;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import com.tencent.tmsecurelite.base.ITmsConnection;
import com.tencent.tmsecurelite.base.ITmsProvider.Stub;
import com.tencent.tmsecurelite.commom.ServiceManager;
import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.d;
import tmsdk.common.utils.l;

/* compiled from: Unknown */
public class mh {
    private static mh AM = null;
    private final int AH = 256;
    private String AI = null;
    private boolean AJ = false;
    private ITmsConnection AK;
    private boolean AL = false;
    private Stub AN = new Stub(this) {
        final /* synthetic */ mh AP;

        {
            this.AP = r1;
        }

        public int getVersion() throws RemoteException {
            return 1;
        }

        public int ipcCall(int i, Bundle bundle, Bundle bundle2) throws RemoteException {
            switch (i) {
                case 790529:
                    d.e("TMSLiteService--TMS", "EXIT");
                    this.AP.eR();
                    this.AP.eP();
                    break;
                case 790530:
                    d.e("TMSLiteService--TMS", "START_TOP_PKG_MONITOR");
                    if (fw.w().I().booleanValue()) {
                        if (!this.AP.eQ()) {
                            bundle2.putInt("ret", 1);
                            break;
                        }
                        bundle2.putInt("ret", 0);
                        break;
                    }
                    bundle2.putInt("ret", 2);
                    break;
                case 790531:
                    d.e("TMSLiteService--TMS", "STOP_TOP_PKG_MONITOR");
                    this.AP.eR();
                    break;
            }
            return 0;
        }
    };
    private a AO;
    private ServiceConnection mConnection = new ServiceConnection(this) {
        final /* synthetic */ mh AP;

        {
            this.AP = r1;
        }

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            this.AP.AL = false;
            this.AP.AK = (ITmsConnection) ServiceManager.getTmsConnection(iBinder);
            try {
                String packageName = TMSDKContext.getApplicaionContext().getPackageName();
                boolean checkVersion = this.AP.AK.checkVersion(3);
                boolean checkPermission = this.AP.AK.checkPermission(packageName, 12);
                if (!checkVersion || !checkPermission) {
                    d.e("TMSLiteService--TMS", "checkVersion or checkPermission failed!!");
                    this.AP.eP();
                } else if (this.AP.AK.setProvider(this.AP.AN) != 0) {
                }
            } catch (Throwable th) {
                this.AP.eP();
            }
        }

        public void onServiceDisconnected(ComponentName componentName) {
            this.AP.AL = false;
            this.AP.eR();
            this.AP.AK = null;
        }
    };
    Handler yO = new Handler(this, Looper.getMainLooper()) {
        final /* synthetic */ mh AP;

        public void handleMessage(Message message) {
            Object obj = null;
            if (message.what == 256) {
                String eM = mg.eM();
                if (l.dm(this.AP.AI) || !this.AP.AI.equals(eM)) {
                    this.AP.AI = eM;
                    obj = 1;
                }
                if (obj != null) {
                    try {
                        if (!this.AP.AJ) {
                            Bundle bundle = new Bundle();
                            Bundle bundle2 = new Bundle();
                            bundle.putString("t.pkg", eM);
                            this.AP.AK.sendTmsRequest(786434, bundle, bundle2);
                        }
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                }
                if (!this.AP.AJ) {
                    this.AP.yO.sendEmptyMessageDelayed(256, 500);
                }
            }
        }
    };

    /* compiled from: Unknown */
    public static class a extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            boolean booleanValue = fw.w().J().booleanValue();
            mg.a(booleanValue, "com.tencent.qqpimsecure", "OP_WRITE_SMS");
            int a = mg.a(booleanValue, "com.tencent.qqpimsecure", "OP_SYSTEM_ALERT_WINDOW");
            if (a == 1) {
                ma.bx(1320018);
            } else if (a == 2) {
                ma.bx(1320019);
            }
            mh.eN().eO();
        }
    }

    private mh() {
        register();
    }

    public static mh eN() {
        if (AM == null) {
            AM = new mh();
        }
        return AM;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized boolean eO() {
        Context applicaionContext = TMSDKContext.getApplicaionContext();
        if (!this.AL) {
            if (this.AK == null) {
                if (fw.w().I().booleanValue()) {
                    boolean bindService = applicaionContext.bindService(ServiceManager.getTmsIntent(12), this.mConnection, 1);
                    if (bindService) {
                        this.AL = true;
                    }
                } else {
                    d.e("TMSLiteService--TMS", "getTopActPerSwitch off");
                    return false;
                }
            }
        }
    }

    private synchronized void eP() {
        this.AL = false;
        if (this.AK != null) {
            TMSDKContext.getApplicaionContext().unbindService(this.mConnection);
            this.AK = null;
        }
    }

    private boolean eQ() {
        this.AJ = false;
        if (mg.eM() != null) {
            ma.bx(1320016);
            this.yO.sendEmptyMessageDelayed(256, 500);
            return true;
        }
        d.f("TMSLiteService--TMS", "No Permission");
        ma.bx(1320017);
        return false;
    }

    private void eR() {
        this.AJ = true;
        this.yO.removeMessages(256);
    }

    private void register() {
        if (this.AO == null) {
            this.AO = new a();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("tencent.bxx.ayy.czz");
            intentFilter.setPriority(Integer.MAX_VALUE);
            try {
                TMSDKContext.getApplicaionContext().registerReceiver(this.AO, intentFilter);
            } catch (Throwable th) {
            }
        }
    }
}
