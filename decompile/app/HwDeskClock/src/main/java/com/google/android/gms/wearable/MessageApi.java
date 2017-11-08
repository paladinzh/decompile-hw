package com.google.android.gms.wearable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;

/* compiled from: Unknown */
public interface MessageApi {

    /* compiled from: Unknown */
    public interface MessageListener {
        void onMessageReceived(MessageEvent messageEvent);
    }

    /* compiled from: Unknown */
    public interface SendMessageResult extends Result {
    }

    PendingResult<SendMessageResult> sendMessage(GoogleApiClient googleApiClient, String str, String str2, byte[] bArr);
}
