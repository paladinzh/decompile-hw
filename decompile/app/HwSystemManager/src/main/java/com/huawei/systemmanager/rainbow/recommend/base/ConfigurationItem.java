package com.huawei.systemmanager.rainbow.recommend.base;

import com.huawei.systemmanager.rainbow.comm.meta.CloudMetaConst;
import com.huawei.systemmanager.rainbow.comm.meta.CloudMetaMgr;

public class ConfigurationItem {
    public int mConfigItemId;
    public int mConfigType;

    public ConfigurationItem(int id, int type) {
        this.mConfigItemId = id;
        this.mConfigType = type;
    }

    public boolean valid() {
        try {
            if (!CloudMetaConst.STRING_HOLDER.equals(CloudMetaMgr.getItemName(this.mConfigItemId)) && CloudMetaMgr.validItemConfigType(this.mConfigType)) {
                return true;
            }
        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
        } catch (Exception ex2) {
            ex2.printStackTrace();
        }
        return false;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{ ");
        buf.append("itemId[").append(this.mConfigItemId).append("] ");
        buf.append("type[").append(this.mConfigType).append("] ");
        buf.append("} ");
        return buf.toString();
    }
}
