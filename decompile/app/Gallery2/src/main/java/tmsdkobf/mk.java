package tmsdkobf;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import java.util.concurrent.ConcurrentHashMap;
import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class mk {
    private static mk AU = null;
    private static Object lock = new Object();
    ConcurrentHashMap<String, a> AT;
    private Context context;
    private Handler mHandler;

    /* compiled from: Unknown */
    class a extends jj {
        public Runnable AV = null;
        final /* synthetic */ mk AW;
        public String action = null;

        a(mk mkVar) {
            this.AW = mkVar;
        }

        public void doOnRecv(Context context, Intent intent) {
            d.e("AlarmerTask", "AlarmerTaskReceiver.onReceive()");
            String action = intent.getAction();
            if (action == null) {
                d.e("AlarmerTask", "AlarmerTaskReceiver.onReceive() null == action");
            } else if (this.action.equals(action) && this.AV != null) {
                this.AW.mHandler.post(this.AV);
                this.AW.cu(action);
            }
        }
    }

    private mk() {
        this.mHandler = null;
        this.context = TMSDKContext.getApplicaionContext();
        this.AT = new ConcurrentHashMap();
        this.mHandler = new Handler(this.context.getMainLooper());
    }

    public static mk eU() {
        if (AU == null) {
            synchronized (lock) {
                if (AU == null) {
                    AU = new mk();
                }
            }
        }
        return AU;
    }

    public void a(String str, long j, Runnable runnable) {
        d.d("AlarmerTask", "添加闹钟任务 : action : " + str + "  " + (j / 1000) + "s");
        try {
            BroadcastReceiver aVar = new a(this);
            this.context.registerReceiver(aVar, new IntentFilter(str));
            aVar.AV = runnable;
            aVar.action = str;
            PendingIntent broadcast = PendingIntent.getBroadcast(this.context, 0, new Intent(str), 0);
            AlarmManager alarmManager = (AlarmManager) this.context.getSystemService("alarm");
            this.AT.put(str, aVar);
            alarmManager.set(0, System.currentTimeMillis() + j, broadcast);
        } catch (Throwable th) {
            d.c("AlarmerTask", th);
        }
    }

    public void cu(String str) {
        d.d("AlarmerTask", "注销闹钟任务 : action : " + str);
        a aVar = (a) this.AT.remove(str);
        if (aVar != null) {
            pm.f(this.context, str);
            this.context.unregisterReceiver(aVar);
        }
    }
}
