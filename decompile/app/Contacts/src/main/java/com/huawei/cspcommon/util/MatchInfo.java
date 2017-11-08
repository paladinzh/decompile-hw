package com.huawei.cspcommon.util;

import java.util.Arrays;

public class MatchInfo {
    int[] mMatchIndex;
    String mMatchString;

    public int[] getMatchIndex() {
        if (this.mMatchIndex == null || this.mMatchIndex.length == 0) {
            return new int[0];
        }
        return Arrays.copyOf(this.mMatchIndex, this.mMatchIndex.length);
    }
}
