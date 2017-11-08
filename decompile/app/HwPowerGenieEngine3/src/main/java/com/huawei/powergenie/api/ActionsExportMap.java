package com.huawei.powergenie.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public final class ActionsExportMap {
    private static final HashMap<Integer, Integer> mActionMap = new HashMap<Integer, Integer>() {
        {
            put(Integer.valueOf(208), Integer.valueOf(10000));
            put(Integer.valueOf(203), Integer.valueOf(10001));
            put(Integer.valueOf(204), Integer.valueOf(10002));
            put(Integer.valueOf(205), Integer.valueOf(10003));
            put(Integer.valueOf(206), Integer.valueOf(10004));
            put(Integer.valueOf(225), Integer.valueOf(10008));
            put(Integer.valueOf(228), Integer.valueOf(10009));
            put(Integer.valueOf(230), Integer.valueOf(10010));
            put(Integer.valueOf(233), Integer.valueOf(10011));
            put(Integer.valueOf(235), Integer.valueOf(10013));
            put(Integer.valueOf(210), Integer.valueOf(10005));
            put(Integer.valueOf(211), Integer.valueOf(10006));
            put(Integer.valueOf(246), Integer.valueOf(10015));
            put(Integer.valueOf(247), Integer.valueOf(10016));
            put(Integer.valueOf(221), Integer.valueOf(10007));
            put(Integer.valueOf(244), Integer.valueOf(10017));
            put(Integer.valueOf(506), Integer.valueOf(10018));
            put(Integer.valueOf(507), Integer.valueOf(10019));
            put(Integer.valueOf(510), Integer.valueOf(10020));
            put(Integer.valueOf(511), Integer.valueOf(10021));
            put(Integer.valueOf(350), Integer.valueOf(20001));
            put(Integer.valueOf(301), Integer.valueOf(20004));
        }
    };

    public static boolean isScenario(int exportActionId) {
        if (exportActionId < 10000 || exportActionId >= 20000) {
            return false;
        }
        return true;
    }

    public static boolean isChildScenario(int exportActionId) {
        switch (exportActionId) {
            case 10005:
            case 10006:
            case 10007:
            case 10015:
            case 10016:
            case 10017:
            case 10018:
            case 10019:
            case 10020:
            case 10021:
                return true;
            default:
                return false;
        }
    }

    public static boolean isValidExportActionID(int exportId) {
        for (Entry entry : mActionMap.entrySet()) {
            if (((Integer) entry.getValue()).intValue() == exportId) {
                return true;
            }
        }
        return false;
    }

    public static int getExportActionID(int innerActionId) {
        Integer exportActionId = (Integer) mActionMap.get(Integer.valueOf(innerActionId));
        if (exportActionId != null) {
            return exportActionId.intValue();
        }
        return -1;
    }

    public static int getPGActionID(int exportActionId) {
        for (Entry entry : mActionMap.entrySet()) {
            if (exportActionId == ((Integer) entry.getValue()).intValue()) {
                return ((Integer) entry.getKey()).intValue();
            }
        }
        return -1;
    }

    public static ArrayList<Integer> getAllActions() {
        ArrayList<Integer> allActions = new ArrayList();
        for (Entry entry : mActionMap.entrySet()) {
            allActions.add((Integer) entry.getKey());
        }
        return allActions;
    }
}
