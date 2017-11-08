package com.huawei.systemmanager.rainbow.comm.meta;

import com.huawei.systemmanager.rainbow.comm.meta.business.AddViewBusiness;
import com.huawei.systemmanager.rainbow.comm.meta.business.BootStartupBusiness;
import com.huawei.systemmanager.rainbow.comm.meta.business.NetworkBusiness;
import com.huawei.systemmanager.rainbow.comm.meta.business.NotificationBusiness;
import com.huawei.systemmanager.rainbow.comm.meta.business.PermissionBusiness;
import com.huawei.systemmanager.rainbow.comm.meta.business.ReadAppListBusiness;
import com.huawei.systemmanager.rainbow.comm.meta.item.CommCfgItem;

public class CloudMetaConst {
    static final AbsBusiness[] BUSINESS_INSTANCES = new AbsBusiness[]{null, new NetworkBusiness(), new NotificationBusiness(), new ReadAppListBusiness(), new BootStartupBusiness(), new AddViewBusiness(), new PermissionBusiness()};
    static final String[] BUSINESS_NAMES = new String[]{STRING_HOLDER, "BN_NETWORK", "BN_NOTIFY", "BN_APPLIST", "BN_STARTUP", "BN_ADDVIEW", "BN_PERMISSION"};
    static final String[] BUSINESS_VIEWKEYS = new String[]{STRING_HOLDER, "BV_Network", "BV_Notification", "BV_AppList", "BV_Bootstartup", "BV_AddViewMgr", "BV_Permission"};
    public static final int INVALID_ID = -1;
    static final AbsConfigItem[] ITEM_INSTANCES = new AbsConfigItem[]{null, CommCfgItem.createItem(1), CommCfgItem.createItem(2), CommCfgItem.createItem(3), CommCfgItem.createItem(4), CommCfgItem.createItem(5), CommCfgItem.createItem(6), null, null, null, null, CommCfgItem.createItem(11), CommCfgItem.createItem(12), CommCfgItem.createItem(13), CommCfgItem.createItem(14), CommCfgItem.createItem(15), CommCfgItem.createItem(16), CommCfgItem.createItem(17), CommCfgItem.createItem(18), CommCfgItem.createItem(19), CommCfgItem.createItem(20), CommCfgItem.createItem(21), CommCfgItem.createItem(22), CommCfgItem.createItem(23), CommCfgItem.createItem(24), CommCfgItem.createItem(25), CommCfgItem.createItem(26), CommCfgItem.createItem(27), CommCfgItem.createItem(28), CommCfgItem.createItem(29), CommCfgItem.createItem(30), CommCfgItem.createItem(31), CommCfgItem.createItem(32), CommCfgItem.createItem(33), CommCfgItem.createItem(34), CommCfgItem.createItem(35), CommCfgItem.createItem(36), CommCfgItem.createItem(37), CommCfgItem.createItem(38)};
    static final String[] ITEM_NAMES = new String[]{STRING_HOLDER, "IN_WIFI_NETWORK", "IN_DATA_NETWORK", "IN_NOTIFICATION", "IN_READAPPLIST", "IN_BOOTSTARTUP", "IN_ADDVIEW", STRING_HOLDER, STRING_HOLDER, STRING_HOLDER, STRING_HOLDER, "IN_READ_CONTACT", "IN_READ_MESSAGE", "IN_READ_CALLLOG", "IN_READ_CALENDAR", "IN_READ_LOCATION", "IN_READ_PHONECODE", "IN_MODIFY_CONTACT", "IN_MODIFY_MESSAGE", "IN_MODIFY_CALLLOG", "IN_DELETE_CONTACT", "IN_DELETE_MESSAGE", "IN_DELETE_CALLLOG", "IN_MAKE_CALL", "IN_SEND_SMS", "IN_SEND_MMS", "IN_TAKE_PHOTO", "IN_SOUND_RECORDER", "IN_OPEN_WIFI", "IN_OPEN_DATA", "IN_OPEN_BT", "IN_EDIT_SHORTCUT", "IN_ACCESS_APPLIST", "IN_RMD", "IN_RHD", "ITEM_ID_MODIFY_CALENDAR", "ITEM_ID_DELETE_CALENDAR", "ITEM_ID_CALL_FORWARD", "ITEM_ID_ACCESS_BROWSER_RECORDS"};
    static final String[] PI_KEYS = new String[]{STRING_HOLDER, STRING_HOLDER, STRING_HOLDER, STRING_HOLDER, STRING_HOLDER, "BOOT_STARTUP", STRING_HOLDER, STRING_HOLDER, STRING_HOLDER, STRING_HOLDER, STRING_HOLDER, "READ_CONTACTS", "READ_SMS", "READ_CALL_LOG", "READ_CALENDAR", "READ_LOCATION", "READ_PHONEID", "WRITE_CONTACTS", "WRITE_SMS", "WRITE_CALL_LOG", "DELETE_CONTACTS", "DELETE_SMS", "DELETE_CALL_LOG", "MAKE_PHONE", "SEND_SMS", "SEND_MMS", "TAKE_PHOTO", "SOUND_RECORDER", "OPEN_WIFI", "OPEN_NETWORK", "OPEN_BT", "EDIT_SHORTCUT", "ACCESS_APPLIST", "RMD", "RHD", "MODIFY_CALENDAR", "DELETE_CALENDAR", "CALL_FORWARD", "ACCESS_BROWSER_RECORDS"};
    static final int[] PI_TYPES = new int[]{0, 2, 1};
    public static final String STRING_HOLDER = "STRING_HOLDER";

    public interface BusinessId {
        public static final int BUSINESS_ID_ADDVIEW = 5;
        public static final int BUSINESS_ID_APPLIST = 3;
        public static final int BUSINESS_ID_BOOTSTARTUP = 4;
        public static final int BUSINESS_ID_NETWORK = 1;
        public static final int BUSINESS_ID_NOTIFICATION = 2;
        public static final int BUSINESS_ID_PERMISSION = 6;
    }

    public interface ItemConfigType {
        public static final int ITEM_CONFIG_TYPE_ALLOW = 0;
        public static final int ITEM_CONFIG_TYPE_FORBID = 1;
        public static final int ITEM_CONFIG_TYPE_REMIND = 2;
    }

    public interface ItemId {
        public static final int ITEM_ALIAS_PERMISSION_BEGIN = 11;
        public static final int ITEM_ALIAS_PERMISSION_END = 38;
        public static final int ITEM_ID_ACCESS_APPLIST = 32;
        public static final int ITEM_ID_ACCESS_BROWSER_RECORDS = 38;
        public static final int ITEM_ID_ADD_VIEW = 6;
        public static final int ITEM_ID_BOOT_STARTUP = 5;
        public static final int ITEM_ID_CALL_FORWARD = 37;
        public static final int ITEM_ID_DATA_NETWORK = 2;
        public static final int ITEM_ID_DELETE_CALENDAR = 36;
        public static final int ITEM_ID_DELETE_CALLLOG = 22;
        public static final int ITEM_ID_DELETE_CONTACT = 20;
        public static final int ITEM_ID_DELETE_MESSAGE = 21;
        public static final int ITEM_ID_EDIT_SHORTCUT = 31;
        public static final int ITEM_ID_MAKE_CALL = 23;
        public static final int ITEM_ID_MODIFY_CALENDAR = 35;
        public static final int ITEM_ID_MODIFY_CALLLOG = 19;
        public static final int ITEM_ID_MODIFY_CONTACT = 17;
        public static final int ITEM_ID_MODIFY_MESSAGE = 18;
        public static final int ITEM_ID_NOTIFICATION = 3;
        public static final int ITEM_ID_OPEN_BT = 30;
        public static final int ITEM_ID_OPEN_DATA = 29;
        public static final int ITEM_ID_OPEN_WIFI = 28;
        public static final int ITEM_ID_READ_APPLIST = 4;
        public static final int ITEM_ID_READ_CALENDAR = 14;
        public static final int ITEM_ID_READ_CALLLOG = 13;
        public static final int ITEM_ID_READ_CONTACT = 11;
        public static final int ITEM_ID_READ_LOCATION = 15;
        public static final int ITEM_ID_READ_MESSAGE = 12;
        public static final int ITEM_ID_READ_PHONE_CODE = 16;
        public static final int ITEM_ID_RHD = 34;
        public static final int ITEM_ID_RMD = 33;
        public static final int ITEM_ID_SEND_MMS = 25;
        public static final int ITEM_ID_SEND_SMS = 24;
        public static final int ITEM_ID_SOUND_RECORDER = 27;
        public static final int ITEM_ID_TAKE_PHOTO = 26;
        public static final int ITEM_ID_WIFI_NETWORK = 1;
    }
}
