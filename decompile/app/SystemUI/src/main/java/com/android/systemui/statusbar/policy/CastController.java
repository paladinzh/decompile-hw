package com.android.systemui.statusbar.policy;

import java.util.Set;

public interface CastController {

    public interface Callback {
        void onCastDevicesChanged();
    }

    public static final class CastDevice {
        public String description;
        public String id;
        public String name;
        public int state = 0;
        public Object tag;
    }

    void addCallback(Callback callback);

    Set<CastDevice> getCastDevices();

    void removeCallback(Callback callback);

    void setCurrentUserId(int i);

    void setDiscovering(boolean z);

    void startCasting(CastDevice castDevice);

    void stopCasting(CastDevice castDevice);
}
