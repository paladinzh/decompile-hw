package com.google.android.gms.wearable.internal;

import android.os.RemoteException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.NodeApi.GetConnectedNodesResult;
import com.google.android.gms.wearable.NodeApi.GetLocalNodeResult;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public final class zzba implements NodeApi {

    /* compiled from: Unknown */
    public static class zzb implements GetConnectedNodesResult {
        private final Status zzQA;
        private final List<Node> zzbaP;

        public zzb(Status status, List<Node> list) {
            this.zzQA = status;
            this.zzbaP = list;
        }

        public List<Node> getNodes() {
            return this.zzbaP;
        }

        public Status getStatus() {
            return this.zzQA;
        }
    }

    /* compiled from: Unknown */
    public static class zzc implements GetLocalNodeResult {
        private final Status zzQA;

        public Status getStatus() {
            return this.zzQA;
        }
    }

    public PendingResult<GetConnectedNodesResult> getConnectedNodes(GoogleApiClient client) {
        return client.zza(new zzh<GetConnectedNodesResult>(this, client) {
            final /* synthetic */ zzba zzbaM;

            protected void zza(zzbn zzbn) throws RemoteException {
                zzbn.zzp(this);
            }

            protected /* synthetic */ Result zzb(Status status) {
                return zzbv(status);
            }

            protected GetConnectedNodesResult zzbv(Status status) {
                return new zzb(status, new ArrayList());
            }
        });
    }
}
