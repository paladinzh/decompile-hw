package com.google.android.gms.wearable.internal;

import android.content.IntentFilter;
import com.google.android.gms.common.data.DataHolder;
import com.google.android.gms.wearable.CapabilityApi.CapabilityListener;
import com.google.android.gms.wearable.ChannelApi.ChannelListener;
import com.google.android.gms.wearable.DataApi.DataListener;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi.MessageListener;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.NodeApi.NodeListener;
import com.google.android.gms.wearable.internal.zzav.zza;
import com.google.android.gms.wearable.zzc;
import java.util.List;

/* compiled from: Unknown */
final class zzbo<T> extends zza {
    private final String zzaZI;
    private final IntentFilter[] zzbao;
    private com.google.android.gms.wearable.zza.zza zzbbj;
    private zzc.zza zzbbk;
    private DataListener zzbbl;
    private MessageListener zzbbm;
    private NodeListener zzbbn;
    private NodeApi.zza zzbbo;
    private ChannelListener zzbbp;
    private CapabilityListener zzbbq;
    private final String zzbbr;

    public void clear() {
        this.zzbbj = null;
        this.zzbbk = null;
        this.zzbbl = null;
        this.zzbbm = null;
        this.zzbbn = null;
        this.zzbbo = null;
        this.zzbbp = null;
        this.zzbbq = null;
    }

    public void onConnectedNodes(List<NodeParcelable> connectedNodes) {
        if (this.zzbbo != null) {
            this.zzbbo.onConnectedNodes(connectedNodes);
        }
    }

    public IntentFilter[] zzCL() {
        return this.zzbao;
    }

    public String zzCM() {
        return this.zzbbr;
    }

    public String zzCN() {
        return this.zzaZI;
    }

    public void zza(AmsEntityUpdateParcelable amsEntityUpdateParcelable) {
        if (this.zzbbj != null) {
            this.zzbbj.zza(amsEntityUpdateParcelable);
        }
    }

    public void zza(AncsNotificationParcelable ancsNotificationParcelable) {
        if (this.zzbbk != null) {
            this.zzbbk.zza(ancsNotificationParcelable);
        }
    }

    public void zza(CapabilityInfoParcelable capabilityInfoParcelable) {
        if (this.zzbbq != null) {
            this.zzbbq.onCapabilityChanged(capabilityInfoParcelable);
        }
    }

    public void zza(ChannelEventParcelable channelEventParcelable) {
        if (this.zzbbp != null) {
            channelEventParcelable.zza(this.zzbbp);
        }
    }

    public void zza(MessageEventParcelable messageEventParcelable) {
        if (this.zzbbm != null) {
            this.zzbbm.onMessageReceived(messageEventParcelable);
        }
    }

    public void zza(NodeParcelable nodeParcelable) {
        if (this.zzbbn != null) {
            this.zzbbn.onPeerConnected(nodeParcelable);
        }
    }

    public void zzad(DataHolder dataHolder) {
        if (this.zzbbl == null) {
            dataHolder.close();
            return;
        }
        try {
            this.zzbbl.onDataChanged(new DataEventBuffer(dataHolder));
        } finally {
            dataHolder.close();
        }
    }

    public void zzb(NodeParcelable nodeParcelable) {
        if (this.zzbbn != null) {
            this.zzbbn.onPeerDisconnected(nodeParcelable);
        }
    }
}
