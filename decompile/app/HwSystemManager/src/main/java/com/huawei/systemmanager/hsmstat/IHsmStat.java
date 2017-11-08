package com.huawei.systemmanager.hsmstat;

public interface IHsmStat {

    public static class SimpleHsmStat implements IHsmStat {
        public boolean eStat(String key, String value) {
            return false;
        }

        public boolean rStat() {
            return false;
        }

        public boolean isEnable() {
            return false;
        }

        public boolean setEnable(boolean enable) {
            return false;
        }

        public void activityStat(int action, String activityName, String params) {
        }
    }

    void activityStat(int i, String str, String str2);

    boolean eStat(String str, String str2);

    boolean isEnable();

    boolean rStat();

    boolean setEnable(boolean z);
}
