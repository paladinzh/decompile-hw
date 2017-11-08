package com.google.android.gms.wearable;

import com.google.android.gms.common.api.Releasable;
import com.google.android.gms.common.api.Result;

/* compiled from: Unknown */
public interface DataApi {

    /* compiled from: Unknown */
    public interface DataListener {
        void onDataChanged(DataEventBuffer dataEventBuffer);
    }

    /* compiled from: Unknown */
    public interface DataItemResult extends Result {
    }

    /* compiled from: Unknown */
    public interface DeleteDataItemsResult extends Result {
    }

    /* compiled from: Unknown */
    public interface GetFdForAssetResult extends Releasable, Result {
    }
}
