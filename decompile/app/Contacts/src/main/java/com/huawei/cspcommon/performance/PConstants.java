package com.huawei.cspcommon.performance;

import android.util.SparseArray;

public class PConstants {
    public static final SparseArray<String> sMappingJLogIds = new SparseArray<String>() {
        {
            put(9, "JLID_DEF_CONTACT_ITEM_CLICK");
            put(11, "JLID_CONTACT_DETAIL_BIND_VIEW");
            put(12, "JLID_NEW_CONTACT_SAVE_CLICK");
            put(13, "JLID_NEW_CONTACT_CLICK");
            put(14, "JLID_NEW_CONTACT_SELECT_ACCOUNT");
            put(15, "JLID_CONTACT_BIND_EDITOR_FOR_NEW");
            put(1003, "JLID_DIALPAD_AFTER_TEXT_CHANGE");
            put(17, "JLID_DIALPAD_ADAPTER_GET_VIEW");
            put(23, "JLID_CONTACT_MULTISELECT_ACTIVITY_ONCREATE");
            put(24, "JLID_CONTACT_MULTISELECT_BIND_VIEW");
            put(1001, "JLID_DIALPAD_ONTOUCH_NOT_FIRST_DOWN");
            put(1002, "JLID_EDIT_CONTACT_CLICK");
            put(1004, "JLID_EDIT_CONTACT_END");
        }
    };
    public static final SparseArray<Row> sMappingTable = new SparseArray<Row>() {
        {
            put(0, new Row("SCENE_STARTUP_DIALER_COLD", new int[]{1}, null, new int[]{2}, null, new int[]{5}, new int[]{8}));
            put(1, new Row("SCENE_STARTUP_DIALER_POWERON", new int[]{2}, new int[]{1}, null, null, new int[]{5}, new int[]{8}));
            put(2, new Row("SCENE_STARTUP_DIALER_HOT", new int[]{3}, new int[]{2}, null, null, new int[]{5}, new int[]{6}));
            put(3, new Row("SCENE_STARTUP_CONTACT_COLD", new int[]{1}, null, new int[]{2}, null, new int[]{4}, new int[]{7}));
            put(4, new Row("SCENE_STARTUP_CONTACT_POWERON", new int[]{2}, new int[]{1}, null, null, new int[]{4}, new int[]{7}));
            put(5, new Row("SCENE_STARTUP_CONTACT_HOT", new int[]{3}, new int[]{2}, null, null, new int[]{4}, new int[]{7}));
            put(6, new Row("SCENE_VIEW_CONTACT", new int[]{9}, null, null, new int[]{10}, null, new int[]{11}));
            put(7, new Row("SCENE_NEW_CONTACT", new int[]{13}, null, null, null, new int[]{14}, new int[]{15}));
            put(8, new Row("SCENE_NEW_CONTACT_SAVE", new int[]{12}, null, null, new int[]{10}, null, new int[]{11}));
            put(9, new Row("SCENE_SMARTSEACHE_DIALER", new int[]{1003}, null, null, null, null, new int[]{17}));
            put(10, new Row("SCENE_SMARTSEACHE_CONTACT", new int[]{18}, null, null, null, null, new int[]{19}));
            put(11, new Row("SCENE_SLIP_TAB", new int[]{21}, null, null, new int[]{22}, null, new int[]{7}));
            put(12, new Row("SCENE_STARTUP_CONTACTMULTISELECTION", new int[]{23}, null, null, null, null, new int[]{24}));
            put(13, new Row("SCENE_STARTUP_CONTACTANDGROUPMULTISELECTION", new int[]{23}, null, null, null, null, new int[]{20}));
        }
    };
}
