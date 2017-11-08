package com.android.server.mtm.condition;

import android.os.Bundle;

public class ConditionMatchorImp implements ConditionMatchor {
    private static final boolean DEBUG = false;
    private static final String TAG = "ConditionMatchorImp";

    public static ConditionMatchor getConditionMatchor(int conditionType) {
        switch (conditionType) {
            case 1000:
                return CombinedConditionMatchor.getInstance();
            case 1001:
            case 1002:
            case 1003:
            case 1004:
                return AppTypeConditionMatchor.getInstance();
            case 1005:
            case 1006:
            case 1007:
            case 1008:
                return GroupConditionMatchor.getInstance();
            case 1009:
            case ConditionMatchor.PACKAGENAMECONTAINS /*1012*/:
                return PackageConditionMatchor.getInstance();
            case ConditionMatchor.PROCESSNAME /*1010*/:
            case ConditionMatchor.PROCESSNAMECONTAINS /*1013*/:
                return ProcessConditionMatchor.getInstance();
            default:
                return null;
        }
    }

    public int conditionMatch(int conditiontype, Bundle args) {
        return 0;
    }
}
