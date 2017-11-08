package com.google.android.gms.wearable;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.data.DataHolder;
import com.google.android.gms.common.zzd;
import com.google.android.gms.wearable.CapabilityApi.CapabilityListener;
import com.google.android.gms.wearable.ChannelApi.ChannelListener;
import com.google.android.gms.wearable.DataApi.DataListener;
import com.google.android.gms.wearable.MessageApi.MessageListener;
import com.google.android.gms.wearable.NodeApi.NodeListener;
import com.google.android.gms.wearable.internal.AmsEntityUpdateParcelable;
import com.google.android.gms.wearable.internal.AncsNotificationParcelable;
import com.google.android.gms.wearable.internal.CapabilityInfoParcelable;
import com.google.android.gms.wearable.internal.ChannelEventParcelable;
import com.google.android.gms.wearable.internal.MessageEventParcelable;
import com.google.android.gms.wearable.internal.NodeParcelable;
import java.util.List;

/* compiled from: Unknown */
public abstract class WearableListenerService extends Service implements CapabilityListener, ChannelListener, DataListener, MessageListener, NodeListener, com.google.android.gms.wearable.NodeApi.zza {
    private boolean zzLA;
    private String zzOZ;
    private volatile int zzaZe = -1;
    private Handler zzaZf;
    private Object zzaZg = new Object();
    private IBinder zzacF;

    /* compiled from: Unknown */
    private class zza extends com.google.android.gms.wearable.internal.zzav.zza {
        boolean zzaZh = false;
        final /* synthetic */ WearableListenerService zzaZi;

        zza(WearableListenerService wearableListenerService) {
            this.zzaZi = wearableListenerService;
            this.zzaZh = wearableListenerService instanceof zzj;
        }

        public void onConnectedNodes(final List<NodeParcelable> connectedNodes) {
            if (Log.isLoggable("WearableLS", 3)) {
                Log.d("WearableLS", "onConnectedNodes: " + this.zzaZi.zzOZ + ": " + connectedNodes);
            }
            this.zzaZi.zzCu();
            synchronized (this.zzaZi.zzaZg) {
                if (this.zzaZi.zzLA) {
                    return;
                }
                this.zzaZi.zzaZf.post(new Runnable(this) {
                    final /* synthetic */ zza zzaZk;

                    public void run() {
                        this.zzaZk.zzaZi.onConnectedNodes(connectedNodes);
                    }
                });
            }
        }

        public void zza(final AmsEntityUpdateParcelable amsEntityUpdateParcelable) {
            if (Log.isLoggable("WearableLS", 3)) {
                Log.d("WearableLS", "onEntityUpdate: " + amsEntityUpdateParcelable);
            }
            if (this.zzaZh) {
                this.zzaZi.zzCu();
                final zzj zzj = (zzj) this.zzaZi;
                synchronized (this.zzaZi.zzaZg) {
                    if (this.zzaZi.zzLA) {
                        return;
                    }
                    this.zzaZi.zzaZf.post(new Runnable(this) {
                        final /* synthetic */ zza zzaZk;

                        public void run() {
                            zzj.zza(amsEntityUpdateParcelable);
                        }
                    });
                }
            }
        }

        public void zza(final AncsNotificationParcelable ancsNotificationParcelable) {
            if (Log.isLoggable("WearableLS", 3)) {
                Log.d("WearableLS", "onNotificationReceived: " + ancsNotificationParcelable);
            }
            if (this.zzaZh) {
                this.zzaZi.zzCu();
                final zzj zzj = (zzj) this.zzaZi;
                synchronized (this.zzaZi.zzaZg) {
                    if (this.zzaZi.zzLA) {
                        return;
                    }
                    this.zzaZi.zzaZf.post(new Runnable(this) {
                        final /* synthetic */ zza zzaZk;

                        public void run() {
                            zzj.zza(ancsNotificationParcelable);
                        }
                    });
                }
            }
        }

        public void zza(final CapabilityInfoParcelable capabilityInfoParcelable) {
            if (Log.isLoggable("WearableLS", 3)) {
                Log.d("WearableLS", "onConnectedCapabilityChanged: " + capabilityInfoParcelable);
            }
            this.zzaZi.zzCu();
            synchronized (this.zzaZi.zzaZg) {
                if (this.zzaZi.zzLA) {
                    return;
                }
                this.zzaZi.zzaZf.post(new Runnable(this) {
                    final /* synthetic */ zza zzaZk;

                    public void run() {
                        this.zzaZk.zzaZi.onCapabilityChanged(capabilityInfoParcelable);
                    }
                });
            }
        }

        public void zza(final ChannelEventParcelable channelEventParcelable) {
            if (Log.isLoggable("WearableLS", 3)) {
                Log.d("WearableLS", "onChannelEvent: " + channelEventParcelable);
            }
            this.zzaZi.zzCu();
            synchronized (this.zzaZi.zzaZg) {
                if (this.zzaZi.zzLA) {
                    return;
                }
                this.zzaZi.zzaZf.post(new Runnable(this) {
                    final /* synthetic */ zza zzaZk;

                    public void run() {
                        channelEventParcelable.zza(this.zzaZk.zzaZi);
                    }
                });
            }
        }

        public void zza(final MessageEventParcelable messageEventParcelable) {
            if (Log.isLoggable("WearableLS", 3)) {
                Log.d("WearableLS", "onMessageReceived: " + messageEventParcelable);
            }
            this.zzaZi.zzCu();
            synchronized (this.zzaZi.zzaZg) {
                if (this.zzaZi.zzLA) {
                    return;
                }
                this.zzaZi.zzaZf.post(new Runnable(this) {
                    final /* synthetic */ zza zzaZk;

                    public void run() {
                        this.zzaZk.zzaZi.onMessageReceived(messageEventParcelable);
                    }
                });
            }
        }

        public void zza(final NodeParcelable nodeParcelable) {
            if (Log.isLoggable("WearableLS", 3)) {
                Log.d("WearableLS", "onPeerConnected: " + this.zzaZi.zzOZ + ": " + nodeParcelable);
            }
            this.zzaZi.zzCu();
            synchronized (this.zzaZi.zzaZg) {
                if (this.zzaZi.zzLA) {
                    return;
                }
                this.zzaZi.zzaZf.post(new Runnable(this) {
                    final /* synthetic */ zza zzaZk;

                    public void run() {
                        this.zzaZk.zzaZi.onPeerConnected(nodeParcelable);
                    }
                });
            }
        }

        public void zzad(final DataHolder dataHolder) {
            if (Log.isLoggable("WearableLS", 3)) {
                Log.d("WearableLS", "onDataItemChanged: " + this.zzaZi.zzOZ + ": " + dataHolder);
            }
            this.zzaZi.zzCu();
            synchronized (this.zzaZi.zzaZg) {
                if (this.zzaZi.zzLA) {
                    dataHolder.close();
                    return;
                }
                this.zzaZi.zzaZf.post(new Runnable(this) {
                    final /* synthetic */ zza zzaZk;

                    public void run() {
                        DataEventBuffer dataEventBuffer = new DataEventBuffer(dataHolder);
                        try {
                            this.zzaZk.zzaZi.onDataChanged(dataEventBuffer);
                        } finally {
                            dataEventBuffer.release();
                        }
                    }
                });
            }
        }

        public void zzb(final NodeParcelable nodeParcelable) {
            if (Log.isLoggable("WearableLS", 3)) {
                Log.d("WearableLS", "onPeerDisconnected: " + this.zzaZi.zzOZ + ": " + nodeParcelable);
            }
            this.zzaZi.zzCu();
            synchronized (this.zzaZi.zzaZg) {
                if (this.zzaZi.zzLA) {
                    return;
                }
                this.zzaZi.zzaZf.post(new Runnable(this) {
                    final /* synthetic */ zza zzaZk;

                    public void run() {
                        this.zzaZk.zzaZi.onPeerDisconnected(nodeParcelable);
                    }
                });
            }
        }
    }

    private void zzCu() throws SecurityException {
        int callingUid = Binder.getCallingUid();
        if (callingUid == this.zzaZe) {
            return;
        }
        if (GooglePlayServicesUtil.zza(this, callingUid, "com.google.android.wearable.app.cn")) {
            if (zzd.zzmY().zzb(getPackageManager(), "com.google.android.wearable.app.cn")) {
                this.zzaZe = callingUid;
                return;
            }
            throw new SecurityException("Caller is not Android Wear.");
        } else if (GooglePlayServicesUtil.zze(this, callingUid)) {
            this.zzaZe = callingUid;
        } else {
            throw new SecurityException("Caller is not GooglePlayServices");
        }
    }

    public final IBinder onBind(Intent intent) {
        return !"com.google.android.gms.wearable.BIND_LISTENER".equals(intent.getAction()) ? null : this.zzacF;
    }

    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
    }

    public void onChannelClosed(Channel channel, int closeReason, int appSpecificErrorCode) {
    }

    public void onChannelOpened(Channel channel) {
    }

    public void onConnectedNodes(List<Node> list) {
    }

    public void onCreate() {
        super.onCreate();
        if (Log.isLoggable("WearableLS", 3)) {
            Log.d("WearableLS", "onCreate: " + getPackageName());
        }
        this.zzOZ = getPackageName();
        HandlerThread handlerThread = new HandlerThread("WearableListenerService");
        handlerThread.start();
        this.zzaZf = new Handler(handlerThread.getLooper());
        this.zzacF = new zza(this);
    }

    public void onDataChanged(DataEventBuffer dataEvents) {
    }

    public void onDestroy() {
        synchronized (this.zzaZg) {
            this.zzLA = true;
            if (this.zzaZf != null) {
                this.zzaZf.getLooper().quit();
            } else {
                throw new IllegalStateException("onDestroy: mServiceHandler not set, did you override onCreate() but forget to call super.onCreate()?");
            }
        }
        super.onDestroy();
    }

    public void onInputClosed(Channel channel, int closeReason, int appSpecificErrorCode) {
    }

    public void onMessageReceived(MessageEvent messageEvent) {
    }

    public void onOutputClosed(Channel channel, int closeReason, int appSpecificErrorCode) {
    }

    public void onPeerConnected(Node peer) {
    }

    public void onPeerDisconnected(Node peer) {
    }
}
