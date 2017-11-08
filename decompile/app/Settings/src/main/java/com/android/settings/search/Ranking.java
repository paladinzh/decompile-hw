package com.android.settings.search;

public final class Ranking extends RankingHwBase {
    public static int getRankForClassName(String className) {
        Integer rank = (Integer) sRankMap.get(className);
        return rank != null ? rank.intValue() : 1024;
    }

    public static int getBaseRankForAuthority(String authority) {
        synchronized (sBaseRankMap) {
            Integer base = (Integer) sBaseRankMap.get(authority);
            if (base != null) {
                int intValue = base.intValue();
                return intValue;
            }
            sCurrentBaseRank++;
            sBaseRankMap.put(authority, Integer.valueOf(sCurrentBaseRank));
            intValue = sCurrentBaseRank;
            return intValue;
        }
    }
}
