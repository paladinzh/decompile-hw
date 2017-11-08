package com.huawei.powergenie.api;

import java.util.ArrayList;

public interface IPowerStats {
    void iStats(int i, String str, int i2);

    void iStats(int i, String str, int i2, long j, long j2);

    void iStats(int i, String str, int i2, String str2);

    void iStats(int i, ArrayList<String> arrayList);
}
