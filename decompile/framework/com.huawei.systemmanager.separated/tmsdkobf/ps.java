package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import java.util.Iterator;
import java.util.LinkedList;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class ps extends jj {
    private static ps IB;
    private static Object lock = new Object();
    private boolean Ao;
    private LinkedList<a> IA = new LinkedList();
    private State Iz = State.DISCONNECTED;

    /* compiled from: Unknown */
    public interface a {
        void cn();

        void co();
    }

    private ps() {
    }

    private void init(Context context) {
        u(context);
    }

    public static ps t(Context context) {
        if (IB == null) {
            synchronized (lock) {
                if (IB == null) {
                    if (context != null) {
                        IB = new ps();
                        IB.init(context);
                    } else {
                        return null;
                    }
                }
            }
        }
        return IB;
    }

    private synchronized void u(Context context) {
        if (!this.Ao) {
            try {
                NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
                if (activeNetworkInfo == null) {
                    this.Iz = State.DISCONNECTED;
                } else {
                    d.d("NetworkBroadcastReceiver", "network type:" + activeNetworkInfo.getType());
                    this.Iz = activeNetworkInfo.getState();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            intentFilter.setPriority(Integer.MAX_VALUE);
            try {
                context.registerReceiver(this, intentFilter);
                this.Ao = true;
            } catch (Throwable th) {
                d.c("NetworkBroadcastReceiver", th);
            }
        }
    }

    public void b(a aVar) {
        synchronized (this.IA) {
            this.IA.add(aVar);
        }
    }

    public void c(a aVar) {
        synchronized (this.IA) {
            this.IA.remove(aVar);
        }
    }

    public void doOnRecv(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        d.d("NetworkBroadcastReceiver", action);
        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
            State state = ((NetworkInfo) extras.getParcelable("networkInfo")).getState();
            if (state != State.CONNECTED) {
                if (state != State.DISCONNECTED) {
                    return;
                }
                if (this.Iz.compareTo(State.CONNECTED) == 0) {
                    jq.ct().b(new Runnable(this) {
                        final /* synthetic */ ps IC;

                        {
                            this.IC = r1;
                        }

                        public void run() {
                            synchronized (this.IC.IA) {
                                LinkedList linkedList = (LinkedList) this.IC.IA.clone();
                            }
                            if (linkedList != null) {
                                Iterator it = linkedList.iterator();
                                while (it.hasNext()) {
                                    ((a) it.next()).cn();
                                }
                            }
                        }
                    }, "monitor_toDisconnected");
                }
            } else if (this.Iz.compareTo(State.DISCONNECTED) == 0) {
                jq.ct().b(new Runnable(this) {
                    final /* synthetic */ ps IC;

                    {
                        this.IC = r1;
                    }

                    public void run() {
                        boolean z = false;
                        synchronized (this.IC.IA) {
                            LinkedList linkedList = (LinkedList) this.IC.IA.clone();
                        }
                        String str = "NetworkBroadcastReceiver";
                        StringBuilder append = new StringBuilder().append("copy != null ? ");
                        if (linkedList != null) {
                            z = true;
                        }
                        d.d(str, append.append(z).toString());
                        if (linkedList != null) {
                            d.d("NetworkBroadcastReceiver", "copy.size() : " + linkedList.size());
                            Iterator it = linkedList.iterator();
                            while (it.hasNext()) {
                                ((a) it.next()).co();
                            }
                        }
                    }
                }, "monitor_toConnected");
            }
            this.Iz = state;
        }
    }
}
