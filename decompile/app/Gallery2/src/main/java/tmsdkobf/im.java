package tmsdkobf;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import tmsdk.common.TMSDKContext;
import tmsdk.common.TMServiceFactory;

/* compiled from: Unknown */
public final class im {
    private static volatile im sg;
    private Context mContext;
    private Handler mHandler;
    private Looper mLooper;
    private ArrayList<String> sh = new ArrayList();
    private ConcurrentHashMap<String, jl> si = new ConcurrentHashMap();
    private boolean sj;

    /* compiled from: Unknown */
    private static abstract class a implements ServiceConnection {
        protected ServiceInfo so;
        protected jl sp;

        public a(Context context, ServiceInfo serviceInfo) {
            this.so = serviceInfo;
        }
    }

    private im(Context context) {
        this.mContext = context;
        this.sj = bR();
        if (this.sj) {
            HandlerThread bF = jq.ct().bF(im.class.getName());
            bF.start();
            this.mLooper = bF.getLooper();
            this.mHandler = new Handler(this.mLooper);
        }
    }

    private boolean a(ServiceInfo serviceInfo) {
        return serviceInfo == null ? false : (jw.uF.equals(serviceInfo.packageName) || "com.tencent.qqphonebook".equals(serviceInfo.packageName)) ? !this.sh.contains(serviceInfo.packageName) && serviceInfo.permission != null && serviceInfo.permission.equals("com.tencent.tmsecure.permission.RECEIVE_SMS") && serviceInfo.exported : false;
    }

    private jl b(ServiceInfo serviceInfo) {
        jl jlVar = (jl) this.si.get(serviceInfo.packageName);
        if (jlVar != null) {
            return jlVar;
        }
        final Intent intent = new Intent();
        intent.setClassName(serviceInfo.packageName, serviceInfo.name);
        final Object obj = new Object();
        final a anonymousClass1 = new a(this, this.mContext, serviceInfo) {
            final /* synthetic */ im sl;

            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                this.sp = tmsdkobf.jl.a.a(iBinder);
                this.sl.si.put(this.so.packageName, this.sp);
                synchronized (obj) {
                    obj.notify();
                }
            }

            public void onServiceDisconnected(ComponentName componentName) {
                this.sl.si.remove(this.so.packageName);
                this.sp = null;
            }
        };
        this.mHandler.post(new Runnable(this) {
            final /* synthetic */ im sl;

            public void run() {
                this.sl.mContext.bindService(intent, anonymousClass1, 1);
            }
        });
        synchronized (obj) {
            try {
                obj.wait(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return anonymousClass1.sp;
    }

    public static im bO() {
        if (sg == null) {
            synchronized (im.class) {
                if (sg == null) {
                    sg = new im(TMSDKContext.getApplicaionContext());
                }
            }
        }
        return sg;
    }

    private boolean bR() {
        int i = 0;
        PackageInfo packageInfo = TMServiceFactory.getSystemInfoService().getPackageInfo(this.mContext.getPackageName(), 4100);
        if (packageInfo == null) {
            return false;
        }
        int i2;
        int i3;
        String[] strArr = packageInfo.requestedPermissions;
        if (strArr != null) {
            for (String equals : strArr) {
                if (equals.equals("com.tencent.tmsecure.permission.RECEIVE_SMS")) {
                    i2 = 1;
                    break;
                }
            }
        }
        i2 = 0;
        PermissionInfo[] permissionInfoArr = packageInfo.permissions;
        if (permissionInfoArr != null) {
            for (PermissionInfo permissionInfo : permissionInfoArr) {
                if (permissionInfo.name.equals("com.tencent.tmsecure.permission.RECEIVE_SMS")) {
                    i3 = 1;
                    break;
                }
            }
        }
        i3 = 0;
        if (packageInfo.services != null) {
            for (ServiceInfo serviceInfo : packageInfo.services) {
                String str = serviceInfo.permission;
                if (str != null && str.equals("com.tencent.tmsecure.permission.RECEIVE_SMS") && serviceInfo.exported) {
                    i = 1;
                    break;
                }
            }
        }
        return (i2 & i3) & i;
    }

    public ArrayList<jl> bP() {
        ArrayList<jl> arrayList = new ArrayList();
        if (this.sj) {
            List<ResolveInfo> queryIntentServices = TMServiceFactory.getSystemInfoService().queryIntentServices(new Intent("com.tencent.tmsecure.action.SMS_RECEIVED"), 0);
            ArrayList arrayList2 = new ArrayList();
            if (queryIntentServices != null) {
                for (ResolveInfo resolveInfo : queryIntentServices) {
                    ServiceInfo serviceInfo = resolveInfo.serviceInfo;
                    String str = serviceInfo.packageName;
                    if (!arrayList2.contains(str) && a(serviceInfo)) {
                        Object b = !str.equals(this.mContext.getPackageName()) ? b(serviceInfo) : jo.cp();
                        if (b != null) {
                            arrayList.add(b);
                        }
                    }
                }
            }
        }
        return arrayList;
    }

    public int bQ() {
        return 1;
    }

    public void bv(String str) {
        if (!this.sh.contains(str)) {
            this.sh.add(str);
        }
    }

    public void bw(String str) {
        if (this.sh.contains(str)) {
            this.sh.remove(str);
        }
    }
}
