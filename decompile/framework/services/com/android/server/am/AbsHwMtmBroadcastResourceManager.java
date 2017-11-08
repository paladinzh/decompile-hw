package com.android.server.am;

public interface AbsHwMtmBroadcastResourceManager {
    void boostCpuBySpecialBroadcast(BroadcastRecord broadcastRecord);

    boolean isBroadcastResourceManaged(BroadcastRecord broadcastRecord, BroadcastFilter broadcastFilter);

    void removeReceiverInDelayBroadcast(ReceiverList receiverList);
}
