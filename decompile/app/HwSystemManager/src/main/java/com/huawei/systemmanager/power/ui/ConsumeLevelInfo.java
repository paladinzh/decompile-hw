package com.huawei.systemmanager.power.ui;

import android.graphics.drawable.Drawable;
import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatterySipper.DrainType;
import com.huawei.systemmanager.power.util.Conversion;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

class ConsumeLevelInfo {
    static final int ALIVE_INT_HIDE = -1;
    static final int ALIVE_INT_RUNNING = 0;
    static final int ALIVE_INT_STOPPED = 1;

    static class ConsumeCommInfo {
        int adjValue;
        double exactPercentage;
        Drawable icon;
        String labelName;
        double value;

        ConsumeCommInfo() {
        }

        public String toString() {
            return "ConsumeCommInfo lableName: " + this.labelName + ", value: " + this.value + ", exactP:" + this.exactPercentage + ", adjP: " + this.adjValue;
        }
    }

    static class ConsumeLevelHardwareInfo {
        ConsumeCommInfo commInfo = new ConsumeCommInfo();
        DrainType type;

        public static class Cmp implements Comparator<ConsumeLevelHardwareInfo> {
            public int compare(ConsumeLevelHardwareInfo arg0, ConsumeLevelHardwareInfo arg1) {
                return Conversion.invertedCompare(arg0.commInfo.value, arg1.commInfo.value);
            }
        }

        ConsumeLevelHardwareInfo() {
        }

        public String toString() {
            return "\nConsumeLevelHardwareInfo type: " + this.type + "{" + this.commInfo + "}";
        }
    }

    static class ConsumeLevelSoftwareInfo {
        int aliveStatus = -1;
        ConsumeCommInfo commInfo = new ConsumeCommInfo();
        boolean isShareUidApp;
        String pkgName;
        Map<String, Double> procPowerMap;
        BatterySipper sipper;
        int uid;

        public static class Cmp implements Comparator<ConsumeLevelSoftwareInfo>, Serializable {
            private static final long serialVersionUID = 157541977051712460L;

            public int compare(ConsumeLevelSoftwareInfo arg0, ConsumeLevelSoftwareInfo arg1) {
                return Conversion.invertedCompare(arg0.commInfo.value, arg1.commInfo.value);
            }
        }

        ConsumeLevelSoftwareInfo() {
        }

        public String toString() {
            return "\nConsumeLevelHardwareInfo uid: " + this.uid + ", pkgName: " + this.pkgName + ", isShareUid: " + this.isShareUidApp + ", runningStatus:" + this.aliveStatus + "{" + this.commInfo + "}";
        }
    }

    ConsumeLevelInfo() {
    }
}
