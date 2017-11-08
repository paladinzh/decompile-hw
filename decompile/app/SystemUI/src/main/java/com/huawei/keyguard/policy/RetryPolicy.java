package com.huawei.keyguard.policy;

import android.content.Context;
import android.util.SparseArray;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternUtils.RequestThrottledException;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.OsUtils;
import com.huawei.timekeeper.AbsTimeKeeper;
import com.huawei.timekeeper.TimeKeeper;
import com.huawei.timekeeper.TimeObserver;
import com.huawei.timekeeper.TimeTickInfo;
import java.util.ArrayList;

public abstract class RetryPolicy {
    private static final int[] KG_RETRY_COUNTER = new int[]{6, 9, 12, 15};
    private static final long[] KG_WAIT_TIME = new long[]{60000, 600000, 1800000, 3600000};
    public static final SparseArray<IRetryPolicy> sPolicyMap = new SparseArray();

    public static abstract class IRetryPolicy extends AbsTimeKeeper {
        public abstract void checkLockDeadline() throws RequestThrottledException;

        public abstract boolean isThinkAsFail(int i, int i2);

        public int getUserId() {
            return -10000;
        }

        public long getRemainingTime() {
            try {
                checkLockDeadline();
                return 0;
            } catch (RequestThrottledException ex) {
                return (long) ex.getTimeoutMs();
            }
        }

        public boolean hasUnexecuteError() {
            return false;
        }

        public void setDpmMaxFailed(int attempts) {
        }

        public void trigerLockout(long timeToLock) {
        }
    }

    public static abstract class RetryPolicyWrapper extends IRetryPolicy {
        protected AbsTimeKeeper mSrcPolicy;

        public RetryPolicyWrapper(AbsTimeKeeper standardKeeper) {
            this.mSrcPolicy = standardKeeper;
        }

        public int addErrorCount() {
            try {
                return this.mSrcPolicy.addErrorCount();
            } catch (IllegalStateException e) {
                HwLog.e("KG_Policy", "RetryPolicy addErrorCnt fail", e);
                return 0;
            }
        }

        public int getErrorCount() {
            return this.mSrcPolicy.getErrorCount();
        }

        public int getRemainingChance() {
            int remainng = this.mSrcPolicy.getRemainingChance();
            HwLog.d("KG_Policy", "getRemainingChance " + remainng);
            return remainng;
        }

        public TimeTickInfo getTimeTickInfo() {
            return this.mSrcPolicy.getTimeTickInfo();
        }

        public boolean isObserverRegistered(TimeObserver obj) {
            return this.mSrcPolicy.isObserverRegistered(obj);
        }

        public void registerObserver(TimeObserver obj) {
            if (!this.mSrcPolicy.isObserverRegistered(obj)) {
                this.mSrcPolicy.registerObserver(obj);
            }
        }

        public void unregisterObserver(TimeObserver observer) {
            if (this.mSrcPolicy.isObserverRegistered(observer)) {
                this.mSrcPolicy.unregisterObserver(observer);
            } else {
                HwLog.w("KG_Policy", "RetryPolicy unregisterObserver invalide observer");
            }
        }

        public void resetErrorCount(Context context) {
            this.mSrcPolicy.resetErrorCount(context);
        }

        public void unregisterAll() {
            this.mSrcPolicy.unregisterAll();
        }

        public void trigerLockout(long timeToLock) {
            HwLog.d("KG_Policy", "RetryPolicy trigerLockout with SrcPolicy");
            this.mSrcPolicy.trigerLockout(timeToLock);
        }
    }

    public static class PswdRetryPolicy extends RetryPolicyWrapper {
        private int mDpmMaxFails = 0;
        private int mFailAttempts = 0;
        private int mHash = 0;
        private int mLastVerifyLength = 0;
        private int mUserId = 0;

        public int getUserId() {
            return this.mUserId;
        }

        public PswdRetryPolicy(Context context, String policyName, int level, int userId) {
            super(TimeKeeper.getInstanceForUser(context, policyName + "_x" + OsUtils.getUserSN(userId), level, userId));
            this.mUserId = userId;
        }

        public void checkLockDeadline() throws RequestThrottledException {
            TimeTickInfo ti = this.mSrcPolicy.getTimeTickInfo();
            if (ti.getMillisUntilFinished() > 200 || ti.getHour() > 0 || ti.getMinute() > 0) {
                throw new RequestThrottledException((int) ti.getMillisUntilFinished());
            }
        }

        public boolean isThinkAsFail(int len, int hash) {
            boolean ret = (this.mHash == 0 || (len > 0 && hash == 0)) ? len > 0 && hash == 0 : this.mLastVerifyLength >= len && this.mLastVerifyLength >= 4;
            if (ret) {
                hash = 0;
            }
            this.mHash = hash;
            this.mLastVerifyLength = len;
            return ret;
        }

        public void resetErrorCount(Context context) {
            this.mFailAttempts = 0;
            this.mDpmMaxFails = 0;
            this.mLastVerifyLength = 0;
            this.mHash = 0;
            super.resetErrorCount(context);
        }

        public boolean hasUnexecuteError() {
            boolean ret = this.mHash != 0 && this.mLastVerifyLength >= 4;
            this.mHash = 0;
            this.mLastVerifyLength = 0;
            if (ret) {
                HwLog.w("KG_Policy", "user hasUnexecuteError. " + OsUtils.getCurrentUser());
            }
            return ret;
        }

        public void setDpmMaxFailed(int attempts) {
            this.mDpmMaxFails = attempts;
        }

        public int getErrorCount() {
            if (this.mDpmMaxFails > 0) {
                return this.mFailAttempts;
            }
            return super.getErrorCount();
        }

        public int getRemainingChance() {
            if (this.mDpmMaxFails > 0) {
                return this.mDpmMaxFails - this.mFailAttempts;
            }
            return super.getRemainingChance();
        }

        public int addErrorCount() {
            if (this.mDpmMaxFails <= 0) {
                return super.addErrorCount();
            }
            this.mFailAttempts++;
            return this.mFailAttempts;
        }
    }

    public static final IRetryPolicy getDefaultPolicy(Context context) {
        return getRetryPolicy(context, 1);
    }

    public static final IRetryPolicy getFingerPolicy(Context context) {
        return getRetryPolicy(context, 10);
    }

    public static IRetryPolicy getRetryPolicy(Context context, int policyType) {
        if (policyType == 1) {
            return getRetryPolicy(context, policyType, OsUtils.getCurrentUser());
        }
        if (policyType == 10) {
            return getRetryPolicy(context, policyType, 0);
        }
        if (policyType != 12 && policyType != 11) {
            return null;
        }
        HwLog.w("KG_Policy", "POLICY_FINGER_BLACK POLICY_FINGER_WHITE is reserved");
        return getRetryPolicy(context, policyType, 0);
    }

    public static final IRetryPolicy getPswdRetryPolicy(Context context, int userId) {
        return getRetryPolicy(context, 1, userId);
    }

    public static final IRetryPolicy getRetryPolicy(Context context, int policyType, int userId) {
        IRetryPolicy policy;
        if (policyType > 255) {
            HwLog.w("KG_Policy", "Unsupport policyType " + policyType);
            policyType = 1;
        }
        int key = getKey(policyType, userId);
        synchronized (RetryPolicy.class) {
            policy = (IRetryPolicy) sPolicyMap.get(key);
        }
        if (policy == null) {
            policy = createRetryPolicy(context, policyType, userId);
            if (policy == null) {
                HwLog.d("KG_Policy", "Can't create policy for " + userId);
            } else {
                synchronized (RetryPolicy.class) {
                    sPolicyMap.put(key, policy);
                }
            }
        }
        return policy;
    }

    public static void checkAllUnexecuteError(Context context) {
        Throwable th;
        synchronized (RetryPolicy.class) {
            int idx = 0;
            LockPatternUtils lockPatternUtils = null;
            while (idx < sPolicyMap.size()) {
                LockPatternUtils lockPatternUtils2;
                try {
                    IRetryPolicy policy = (IRetryPolicy) sPolicyMap.valueAt(idx);
                    if ((policy instanceof PswdRetryPolicy) && policy.hasUnexecuteError()) {
                        policy.addErrorCount();
                        if (lockPatternUtils == null) {
                            lockPatternUtils2 = new LockPatternUtils(context);
                        } else {
                            lockPatternUtils2 = lockPatternUtils;
                        }
                        try {
                            lockPatternUtils2.reportFailedPasswordAttempt(policy.getUserId());
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    } else {
                        lockPatternUtils2 = lockPatternUtils;
                    }
                    idx++;
                    lockPatternUtils = lockPatternUtils2;
                } catch (Throwable th3) {
                    th = th3;
                    lockPatternUtils2 = lockPatternUtils;
                }
            }
            return;
        }
        throw th;
    }

    public static ArrayList<IRetryPolicy> getUserPolicies(int userId) {
        ArrayList<IRetryPolicy> retPolicies = new ArrayList();
        synchronized (RetryPolicy.class) {
            int len = sPolicyMap.size();
            for (int i = 0; i < len; i++) {
                int key = sPolicyMap.keyAt(i);
                if (((IRetryPolicy) sPolicyMap.valueAt(i)).getUserId() == userId) {
                    retPolicies.add((IRetryPolicy) sPolicyMap.valueAt(i));
                }
            }
        }
        return retPolicies;
    }

    private static int getKey(int policyType, int userId) {
        return (userId << 8) | (policyType & 255);
    }

    private static IRetryPolicy createRetryPolicy(Context context, int policyType, int userId) {
        switch (policyType) {
            case 1:
                return new PswdRetryPolicy(context, getPolicyName(policyType), 0, userId);
            case 10:
                return new FingerPrintPolicy(context, userId);
            case 11:
                return null;
            case 12:
                return null;
            default:
                return null;
        }
    }

    public static final String getPolicyName(int type) {
        switch (type) {
            case 1:
                return "kg_policy_pswd_pin_patten";
            case 11:
                return "kg_policy_pinger_screen_on";
            case 12:
                return "kg_policy_pinger_screen_off";
            default:
                return "kg_policy_unsupport";
        }
    }
}
