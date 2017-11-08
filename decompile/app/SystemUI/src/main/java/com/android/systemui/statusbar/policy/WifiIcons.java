package com.android.systemui.statusbar.policy;

import com.android.systemui.R;

public class WifiIcons {
    public static final int[][] QS_WIFI_SIGNAL_STRENGTH = new int[][]{new int[]{R.drawable.ic_qs_wifi_0, R.drawable.ic_qs_wifi_1, R.drawable.ic_qs_wifi_2, R.drawable.ic_qs_wifi_3, R.drawable.ic_qs_wifi_4}, new int[]{R.drawable.ic_qs_wifi_full_0, R.drawable.ic_qs_wifi_full_1, R.drawable.ic_qs_wifi_full_2, R.drawable.ic_qs_wifi_full_3, R.drawable.ic_qs_wifi_full_4}};
    static final int WIFI_LEVEL_COUNT = WIFI_SIGNAL_STRENGTH[0].length;
    static final int[][] WIFI_SIGNAL_STRENGTH = new int[][]{new int[]{R.drawable.stat_sys_wifi_signal_0, R.drawable.stat_sys_wifi_signal_1, R.drawable.stat_sys_wifi_signal_2, R.drawable.stat_sys_wifi_signal_3, R.drawable.stat_sys_wifi_signal_4}, new int[]{R.drawable.stat_sys_wifi_signal_0_fully, R.drawable.stat_sys_wifi_signal_1_fully, R.drawable.stat_sys_wifi_signal_2_fully, R.drawable.stat_sys_wifi_signal_3_fully, R.drawable.stat_sys_wifi_signal_4_fully}};
}
