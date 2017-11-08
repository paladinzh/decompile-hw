package com.android.server.mtm.condition;

import android.os.Bundle;
import android.util.Log;

public class CombinedConditionMatchor extends ConditionMatchorImp {
    private static boolean DEBUG = false;
    private static final String TAG = "CombinedConditionMatchor";
    private static CombinedConditionMatchor mConditionMatchor;

    private boolean checkSupportedCondition(int condtiontype) {
        switch (condtiontype) {
            case 1000:
                return false;
            case 1001:
            case 1002:
            case 1003:
            case 1004:
            case 1005:
            case 1006:
            case 1007:
            case 1008:
                return true;
            default:
                return false;
        }
    }

    public static synchronized CombinedConditionMatchor getInstance() {
        CombinedConditionMatchor combinedConditionMatchor;
        synchronized (CombinedConditionMatchor.class) {
            if (mConditionMatchor == null) {
                mConditionMatchor = new CombinedConditionMatchor();
                if (DEBUG) {
                    Log.d(TAG, "getInstance create new resource");
                }
            }
            combinedConditionMatchor = mConditionMatchor;
        }
        return combinedConditionMatchor;
    }

    public static void enableDebug() {
        DEBUG = true;
    }

    public static void disableDebug() {
        DEBUG = false;
    }

    public int conditionMatch(int conditiontype, Bundle args) {
        if (args == null) {
            if (DEBUG) {
                Log.d(TAG, "condition args is not exist ");
            }
            return 0;
        }
        int conditionattribute = args.getInt("conditionattribute", -1);
        String combinedcondition = args.getString("combinedcondition", null);
        if (DEBUG) {
            Log.d(TAG, "combinedcondition is:" + combinedcondition);
        }
        if (combinedcondition == null || combinedcondition.isEmpty()) {
            if (DEBUG) {
                Log.d(TAG, "combinedcondition null or empty: " + combinedcondition);
            }
            return 0;
        }
        String[] conditions = combinedcondition.split("\\|");
        if (conditions.length == 0) {
            if (DEBUG) {
                Log.d(TAG, "combinedcondition lengthe 0");
            }
            return 0;
        }
        int i = 0;
        while (i < conditions.length) {
            try {
                if (DEBUG) {
                    Log.d(TAG, "condition type is " + conditions[i]);
                }
                int mconditionType = Integer.parseInt(conditions[i]);
                if (checkSupportedCondition(mconditionType)) {
                    ConditionMatchor mConditionMatchor = ConditionMatchorImp.getConditionMatchor(mconditionType);
                    if (mConditionMatchor == null) {
                        if (DEBUG) {
                            Log.d(TAG, "combinedcondition type " + mconditionType + " do not exist ");
                        }
                        return 0;
                    }
                    int conditionmatch = mConditionMatchor.conditionMatch(mconditionType, args);
                    if (DEBUG) {
                        Log.d(TAG, "conditionmatch result is " + conditionmatch + " type is " + mconditionType);
                    }
                    if (conditionmatch == 0) {
                        if (DEBUG) {
                            Log.d(TAG, "combinedcondition do not match");
                        }
                        return 0;
                    }
                    i++;
                } else {
                    if (DEBUG) {
                        Log.d(TAG, "combinedcondition do not support");
                    }
                    return 0;
                }
            } catch (NumberFormatException e) {
                if (DEBUG) {
                    Log.d(TAG, "NumberFormatException");
                }
                return 0;
            }
        }
        if (2 == conditionattribute) {
            return 2;
        }
        return 1;
    }
}
