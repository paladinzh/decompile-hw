package com.huawei.android.airsharing.client;

import android.os.RemoteException;
import com.huawei.android.airsharing.api.IEventListener;
import com.huawei.android.airsharing.client.IAidlHwListener.Stub;

public class EventListenerAgent extends Stub {
    private IEventListener mListener;

    public EventListenerAgent(IEventListener listener) {
        this.mListener = listener;
    }

    public boolean onEvent(int eventId, String type) {
        return this.mListener.onEvent(eventId, type);
    }

    public int getId() throws RemoteException {
        return this.mListener.hashCode();
    }
}
