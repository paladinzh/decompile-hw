package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.Intent;
import android.telephony.SubscriptionInfo;
import com.android.settingslib.net.DataUsageController;
import com.android.settingslib.wifi.AccessPoint;
import java.util.List;

public interface NetworkController {

    public interface SignalCallback {
        void setEthernetIndicators(IconState iconState);

        void setExtData(int i, int i2, boolean z, boolean z2, int... iArr);

        void setIsAirplaneMode(IconState iconState);

        void setMobileDataEnabled(boolean z);

        void setMobileDataIndicators(IconState iconState, IconState iconState2, int i, int i2, boolean z, boolean z2, String str, String str2, boolean z3, int i3);

        void setNoSims(boolean z);

        void setSubs(List<SubscriptionInfo> list);

        void setWifiIndicators(boolean z, IconState iconState, IconState iconState2, boolean z2, boolean z3, String str);

        void updateSubs(int i, int i2);
    }

    public interface EmergencyListener {
        void setEmergencyCallsOnly(boolean z);
    }

    public interface AccessPointController {

        public interface AccessPointCallback {
            void onAccessPointsChanged(List<AccessPoint> list);

            void onSettingsActivityTriggered(Intent intent);
        }

        void addAccessPointCallback(AccessPointCallback accessPointCallback);

        boolean canConfigWifi();

        boolean connect(AccessPoint accessPoint);

        int getIcon(AccessPoint accessPoint);

        void removeAccessPointCallback(AccessPointCallback accessPointCallback);

        void scanForAccessPoints();
    }

    public static class IconState {
        public final String contentDescription;
        public final int icon;
        public final boolean visible;

        public IconState(boolean visible, int icon, String contentDescription) {
            this.visible = visible;
            this.icon = icon;
            this.contentDescription = contentDescription;
        }

        public IconState(boolean visible, int icon, int contentDescription, Context context) {
            this(visible, icon, context.getString(contentDescription));
        }
    }

    void addEmergencyListener(EmergencyListener emergencyListener);

    void addSignalCallback(SignalCallback signalCallback);

    AccessPointController getAccessPointController();

    DataSaverController getDataSaverController();

    DataUsageController getMobileDataController();

    boolean hasVoiceCallingFeature();

    void removeEmergencyListener(EmergencyListener emergencyListener);

    void removeSignalCallback(SignalCallback signalCallback);

    void setWifiEnabled(boolean z);
}
