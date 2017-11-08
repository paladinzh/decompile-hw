package com.huawei.systemmanager.rainbow.comm.meta;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class CloudMetaMgr {
    private static final String TAG = CloudMetaMgr.class.getSimpleName();

    public static String getBusinessName(int businessId) {
        return idToName(CloudMetaConst.BUSINESS_NAMES, businessId);
    }

    public static String getBusinessViewKey(int businessId) {
        return idToName(CloudMetaConst.BUSINESS_VIEWKEYS, businessId);
    }

    public static String getItemName(int itemId) {
        return idToName(CloudMetaConst.ITEM_NAMES, itemId);
    }

    public static AbsBusiness getBusinessInstance(int businessId) {
        return (AbsBusiness) idToObj(CloudMetaConst.BUSINESS_INSTANCES, businessId);
    }

    public static List<AbsBusiness> getAllValidBusiness() {
        return Lists.newArrayList(Collections2.filter(Lists.newArrayList(CloudMetaConst.BUSINESS_INSTANCES), new Predicate<AbsBusiness>() {
            public boolean apply(AbsBusiness input) {
                return input != null;
            }
        }));
    }

    public static AbsConfigItem getItemInstance(int itemId) {
        return (AbsConfigItem) idToObj(CloudMetaConst.ITEM_INSTANCES, itemId);
    }

    public static int getItemId(String itemColName) {
        for (int i = 1; i < CloudMetaConst.ITEM_INSTANCES.length; i++) {
            AbsConfigItem item = getItemInstance(i);
            if (item != null && item.getColumnlName().equals(itemColName)) {
                return i;
            }
        }
        HwLog.e(TAG, "getItemId failed for colName: " + itemColName);
        return -1;
    }

    public static boolean validItemConfigType(int type) {
        if (type == 0 || 1 == type || 2 == type) {
            return true;
        }
        return false;
    }

    public static String getPIKey(int itemId) {
        return idToName(CloudMetaConst.PI_KEYS, itemId);
    }

    public static int getPIType(int itemType) {
        return CloudMetaConst.PI_TYPES[itemType];
    }

    private static String idToName(String[] nameArray, int id) {
        String value = (String) idToObj(nameArray, id);
        if (value != null && !CloudMetaConst.STRING_HOLDER.equals(value)) {
            return value;
        }
        throw new IndexOutOfBoundsException("idToString invalid id: " + id);
    }

    private static Object idToObj(Object[] objs, int id) {
        if (id > 0 && objs.length >= id) {
            return objs[id];
        }
        throw new IndexOutOfBoundsException("idToObj invalid id: " + id);
    }
}
