package com.huawei.harassmentinterception.common;

import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.util.HwLog;
import java.util.Map;

public abstract class RuleStates {
    private static final String TAG = "RuleStates";
    private static final RuleStates mBothCloseStatus = new RuleStates() {
        int getState() {
            return 0;
        }

        boolean isCard1Open() {
            return false;
        }

        boolean isCard2Open() {
            return false;
        }
    };
    private static final RuleStates mBothOpenStatus = new RuleStates() {
        int getState() {
            return 3;
        }

        boolean isCard1Open() {
            return true;
        }

        boolean isCard2Open() {
            return true;
        }
    };
    private static RuleStates mCard1OpenStatus = new RuleStates() {
        int getState() {
            return 1;
        }

        boolean isCard1Open() {
            return true;
        }

        boolean isCard2Open() {
            return false;
        }
    };
    private static RuleStates mCard2OpenStatus = new RuleStates() {
        int getState() {
            return 2;
        }

        boolean isCard1Open() {
            return false;
        }

        boolean isCard2Open() {
            return true;
        }
    };
    private static final Map<Integer, RuleStates> mStatus = HsmCollections.newArrayMap();

    abstract int getState();

    abstract boolean isCard1Open();

    abstract boolean isCard2Open();

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    int doOp(int opCard, boolean on) {
        int i = 3;
        int i2 = 1;
        int i3 = 0;
        if (opCard == 0) {
            if (!on) {
                i = 0;
            }
            return i;
        }
        int curState = getState();
        boolean curCard1Open = isCard1Open();
        boolean curCard2Open = isCard2Open();
        if (opCard == 1 && on) {
            if (curCard1Open) {
                return curState;
            }
            if (!curCard2Open) {
                i = 1;
            }
            return i;
        } else if (opCard != 1 || on) {
            if (opCard == 2 && on) {
                if (curCard2Open) {
                    return curState;
                }
                if (!curCard1Open) {
                    i = 2;
                }
                return i;
            } else if (opCard != 2 || on || !curCard2Open) {
                return curState;
            } else {
                if (!curCard1Open) {
                    i2 = 0;
                }
                return i2;
            }
        } else if (!curCard1Open) {
            return curState;
        } else {
            if (curCard2Open) {
                i3 = 2;
            }
            return i3;
        }
    }

    static {
        mStatus.put(Integer.valueOf(mBothCloseStatus.getState()), mBothCloseStatus);
        mStatus.put(Integer.valueOf(mBothOpenStatus.getState()), mBothOpenStatus);
        mStatus.put(Integer.valueOf(mCard1OpenStatus.getState()), mCard1OpenStatus);
        mStatus.put(Integer.valueOf(mCard2OpenStatus.getState()), mCard2OpenStatus);
    }

    public static int doOp(int opCard, boolean on, int curState) {
        RuleStates ruleState = (RuleStates) mStatus.get(Integer.valueOf(curState));
        if (ruleState == null) {
            return curState;
        }
        return ruleState.doOp(opCard, on);
    }

    public static boolean isCard1Open(int state) {
        RuleStates states = (RuleStates) mStatus.get(Integer.valueOf(state));
        if (states != null) {
            return states.isCard1Open();
        }
        HwLog.e(TAG, "isCard1Open unknown state:" + state);
        return false;
    }

    public static boolean isCard2Open(int state) {
        RuleStates states = (RuleStates) mStatus.get(Integer.valueOf(state));
        if (states != null) {
            return states.isCard2Open();
        }
        HwLog.e(TAG, "isCard2Open unknown state:" + state);
        return false;
    }
}
