package com.huawei.timekeeper;

public class Rule {
    private static final int CHANCE_ADDITION = 3;
    private static final int[] CHANCE_STAGE_HIGH = new int[]{5, 8, 11};
    private static final int[] CHANCE_STAGE_LOW = new int[]{5, 8, 11, 14};
    private static final long[] LOCKING_TIME_STAGE_HIGH = new long[]{60000, 3600000, 86400000};
    private static final long[] LOCKING_TIME_STAGE_LOW = new long[]{60000, 600000, 1800000, 3600000};
    private final int mChanceAddition;
    private final int[] mChanceStage;
    private final int mLevel;
    private final long[] mLockingTimeStage;

    Rule(int level) {
        this.mLevel = level;
        if (level == 0) {
            this.mChanceStage = CHANCE_STAGE_LOW;
            this.mLockingTimeStage = LOCKING_TIME_STAGE_LOW;
            this.mChanceAddition = 3;
            return;
        }
        this.mChanceStage = CHANCE_STAGE_HIGH;
        this.mLockingTimeStage = LOCKING_TIME_STAGE_HIGH;
        this.mChanceAddition = 3;
    }

    public Rule(int level, int[] chanceStage, long[] lockingTimeStage, int chanceAddition) {
        this.mLevel = level;
        this.mChanceStage = (int[]) chanceStage.clone();
        this.mLockingTimeStage = (long[]) lockingTimeStage.clone();
        this.mChanceAddition = chanceAddition;
    }

    int getLevel() {
        return this.mLevel;
    }

    int[] getChanceStage() {
        return this.mChanceStage;
    }

    long[] getLockingTimeStage() {
        return this.mLockingTimeStage;
    }

    int getChanceAddition() {
        return this.mChanceAddition;
    }

    static void verify(Rule rule) {
        int[] chanceStage = rule.getChanceStage();
        if (chanceStage == null || chanceStage.length == 0) {
            throw new IllegalArgumentException("chanceLevel is empty");
        }
        int temp = 0;
        int i = 0;
        while (i < chanceStage.length) {
            if (chanceStage[i] > temp) {
                temp = chanceStage[i];
                i++;
            } else {
                throw new IllegalArgumentException("chanceLevel value is illegal");
            }
        }
        long[] lockingTimeStage = rule.getLockingTimeStage();
        if (lockingTimeStage == null || lockingTimeStage.length == 0) {
            throw new IllegalArgumentException("lockingTimeLevel is empty");
        }
        for (long j : lockingTimeStage) {
            if (j <= 0) {
                throw new IllegalArgumentException("lockingTimeLevel value is illegal");
            }
        }
        if (rule.getChanceAddition() < 1) {
            throw new IllegalArgumentException("chanceAddition is illegal");
        }
    }
}
