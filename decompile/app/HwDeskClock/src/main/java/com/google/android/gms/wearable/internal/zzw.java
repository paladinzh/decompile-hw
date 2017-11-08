package com.google.android.gms.wearable.internal;

import android.os.ParcelFileDescriptor;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataApi.DataItemResult;
import com.google.android.gms.wearable.DataApi.DeleteDataItemsResult;
import com.google.android.gms.wearable.DataApi.GetFdForAssetResult;
import java.io.IOException;
import java.io.InputStream;

/* compiled from: Unknown */
public final class zzw implements DataApi {

    /* compiled from: Unknown */
    public static class zzb implements DataItemResult {
        private final Status zzQA;

        public Status getStatus() {
            return this.zzQA;
        }
    }

    /* compiled from: Unknown */
    public static class zzc implements DeleteDataItemsResult {
        private final Status zzQA;

        public Status getStatus() {
            return this.zzQA;
        }
    }

    /* compiled from: Unknown */
    public static class zzd implements GetFdForAssetResult {
        private volatile boolean mClosed;
        private final Status zzQA;
        private volatile InputStream zzaZZ;
        private volatile ParcelFileDescriptor zzbar;

        public Status getStatus() {
            return this.zzQA;
        }

        public void release() {
            if (this.zzbar == null) {
                return;
            }
            if (this.mClosed) {
                throw new IllegalStateException("releasing an already released result.");
            }
            try {
                if (this.zzaZZ == null) {
                    this.zzbar.close();
                } else {
                    this.zzaZZ.close();
                }
                this.mClosed = true;
                this.zzbar = null;
                this.zzaZZ = null;
            } catch (IOException e) {
            }
        }
    }
}
