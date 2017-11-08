package com.huawei.systemmanager.rainbow.comm.meta;

public abstract class AbsConfigItem {
    private static final String ITEM_COL_SUFFIX = "_COL";
    private static final String ITEM_FEA_SUFFIX = "_FEA";

    public abstract int getCfgItemId();

    public String getColumnlName() {
        return getCfgItemName() + ITEM_COL_SUFFIX;
    }

    public String getShortFeatureName() {
        return String.valueOf(getCfgItemId());
    }

    public String getLongFeatureName() {
        return getCfgItemName() + ITEM_FEA_SUFFIX;
    }

    public String getCfgItemName() {
        return CloudMetaMgr.getItemName(getCfgItemId());
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        buf.append("id: ").append(getCfgItemId());
        buf.append(" colName: ").append(getColumnlName());
        buf.append(" feaName: ").append(getLongFeatureName());
        buf.append(" itemName: ").append(getCfgItemName());
        return buf.toString();
    }
}
