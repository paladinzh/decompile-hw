package com.huawei.powergenie.modules.resgovernor;

import java.util.HashMap;
import java.util.Map.Entry;

class CpuFreqItem {
    public final int mActionId;
    public final String mPkgName;
    private final HashMap<Integer, Integer> mPolicyList = new HashMap();

    public CpuFreqItem(int actionId, String pkgName) {
        this.mActionId = actionId;
        this.mPkgName = pkgName;
    }

    public CpuFreqItem clone() {
        CpuFreqItem item = new CpuFreqItem(this.mActionId, this.mPkgName);
        item.addAllPolicy(this.mPolicyList);
        return item;
    }

    public void addPolicy(int policyType, int value) {
        synchronized (this.mPolicyList) {
            this.mPolicyList.put(Integer.valueOf(policyType), Integer.valueOf(value));
        }
    }

    private void addAllPolicy(HashMap<Integer, Integer> policyMap) {
        if (policyMap != null) {
            synchronized (this.mPolicyList) {
                this.mPolicyList.putAll(policyMap);
            }
        }
    }

    public int getPolicy(int policyType) {
        Integer value;
        synchronized (this.mPolicyList) {
            value = (Integer) this.mPolicyList.get(Integer.valueOf(policyType));
        }
        return value == null ? -1 : value.intValue();
    }

    public HashMap<Integer, Integer> getPolicyMap() {
        return this.mPolicyList;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("action id =").append(this.mActionId);
        for (Entry entry : this.mPolicyList.entrySet()) {
            builder.append(", policy type =").append(entry.getKey());
            builder.append(", policy value=").append(entry.getValue());
        }
        builder.append(", pkg name=").append(this.mPkgName);
        return builder.toString();
    }
}
