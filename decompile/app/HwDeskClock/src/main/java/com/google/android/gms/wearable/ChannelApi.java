package com.google.android.gms.wearable;

import com.google.android.gms.common.api.Result;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/* compiled from: Unknown */
public interface ChannelApi {

    /* compiled from: Unknown */
    public interface ChannelListener {
        void onChannelClosed(Channel channel, int i, int i2);

        void onChannelOpened(Channel channel);

        void onInputClosed(Channel channel, int i, int i2);

        void onOutputClosed(Channel channel, int i, int i2);
    }

    @Retention(RetentionPolicy.SOURCE)
    /* compiled from: Unknown */
    public @interface CloseReason {
    }

    /* compiled from: Unknown */
    public interface OpenChannelResult extends Result {
    }
}
