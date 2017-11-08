package com.android.systemui.statusbar;

public class StatusBarState {
    public static String toShortString(int x) {
        switch (x) {
            case 0:
                return "SHD";
            case 1:
                return "KGRD";
            case 2:
                return "SHD_LCK";
            case 3:
                return "FS_USRSW";
            default:
                return "bad_value_" + x;
        }
    }
}
