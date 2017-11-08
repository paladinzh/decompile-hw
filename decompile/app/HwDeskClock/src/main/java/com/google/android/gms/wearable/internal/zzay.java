package com.google.android.gms.wearable.internal;

import android.os.RemoteException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageApi.SendMessageResult;

/* compiled from: Unknown */
public final class zzay implements MessageApi {

    /* compiled from: Unknown */
    public static class zzb implements SendMessageResult {
        private final Status zzQA;
        private final int zzags;

        public zzb(Status status, int i) {
            this.zzQA = status;
            this.zzags = i;
        }

        public Status getStatus() {
            return this.zzQA;
        }
    }

    public PendingResult<SendMessageResult> sendMessage(GoogleApiClient client, String nodeId, String action, byte[] data) {
        final String str = nodeId;
        final String str2 = action;
        final byte[] bArr = data;
        return client.zza(new zzh<SendMessageResult>(this, client) {
            final /* synthetic */ zzay zzbaJ;

            protected void zza(zzbn zzbn) throws RemoteException {
                zzbn.zza((com.google.android.gms.common.api.zzc.zzb) this, str, str2, bArr);
            }

            protected /* synthetic */ Result zzb(Status status) {
                return zzbt(status);
            }

            protected SendMessageResult zzbt(Status status) {
                return new zzb(status, -1);
            }
        });
    }
}
