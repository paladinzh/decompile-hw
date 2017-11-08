package com.google.android.gms.wearable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import java.util.List;

/* compiled from: Unknown */
public interface NodeApi {

    /* compiled from: Unknown */
    public interface NodeListener {
        void onPeerConnected(Node node);

        void onPeerDisconnected(Node node);
    }

    /* compiled from: Unknown */
    public interface zza {
        void onConnectedNodes(List<Node> list);
    }

    /* compiled from: Unknown */
    public interface GetConnectedNodesResult extends Result {
        List<Node> getNodes();
    }

    /* compiled from: Unknown */
    public interface GetLocalNodeResult extends Result {
    }

    PendingResult<GetConnectedNodesResult> getConnectedNodes(GoogleApiClient googleApiClient);
}
