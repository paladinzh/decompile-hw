package com.huawei.systemmanager.rainbow.vaguerule;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VagueOrderUtil implements Comparator<String> {
    private Map<String, Integer> orderMap = new HashMap();

    public VagueOrderUtil(List<String> keyList) {
        if (keyList != null && !keyList.isEmpty()) {
            for (String rule : keyList) {
                int value = 0;
                if (rule.contains(VagueRegConst.REG_ONE_CHAR) && !rule.contains(VagueRegConst.PTAH_PREFIX)) {
                    value = 0;
                }
                if (rule.contains("*") && !rule.contains(VagueRegConst.PTAH_PREFIX)) {
                    value = 1;
                }
                if (rule.contains(VagueRegConst.PTAH_PREFIX) && !rule.contains("*")) {
                    value = 2;
                }
                if (rule.contains("*") && rule.contains(VagueRegConst.PTAH_PREFIX)) {
                    value = 3;
                }
                if (rule.equalsIgnoreCase(VagueRegConst.SYSTEM_FLAG)) {
                    value = 4;
                }
                if (rule.equalsIgnoreCase(VagueRegConst.REG_DEFAULT)) {
                    value = 5;
                }
                this.orderMap.put(rule, Integer.valueOf(value));
            }
        }
    }

    public int compare(String arg0, String arg1) {
        return ((Integer) this.orderMap.get(arg0)).compareTo((Integer) this.orderMap.get(arg1));
    }
}
