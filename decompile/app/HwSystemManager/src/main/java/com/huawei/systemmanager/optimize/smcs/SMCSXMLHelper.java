package com.huawei.systemmanager.optimize.smcs;

import android.content.Context;
import android.text.TextUtils;
import com.google.android.collect.Maps;
import com.huawei.systemmanager.comm.xml.XmlParsers;
import com.huawei.systemmanager.comm.xml.base.SimpleXmlRow;
import com.huawei.systemmanager.optimize.base.Const;
import com.huawei.systemmanager.util.HwLog;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class SMCSXMLHelper {
    public static final String ATTR_CHECK = "check";
    public static final String ATTR_CONTROLLED = "controlled";
    public static final String ATTR_KEY_TASK = "keytask";
    public static final String ATTR_NAME = "name";

    SMCSXMLHelper() {
    }

    public static Map<String, Map<String, String>> parseProtectTableDefaultValue(Context ctx) {
        Map<String, Map<String, String>> values = Maps.newHashMap();
        HwLog.i("SmartMemoryCleanService", Const.HSM_PROTECT_DEFAULT_VALUE_FILE_CUST + " exist");
        try {
            addXmlRowToMap(values, XmlParsers.diskSimpleXmlRows(Const.HSM_PROTECT_DEFAULT_VALUE_FILE_CUST));
            return values;
        } catch (Exception e) {
            HwLog.i("SmartMemoryCleanService", "parse " + Const.HSM_PROTECT_DEFAULT_VALUE_FILE_CUST + " failed!");
            e.printStackTrace();
            try {
                List<SimpleXmlRow> pkgWitheCust = XmlParsers.diskSimpleXmlRows(Const.HSM_PROTECT_FILE_OLD);
                for (SimpleXmlRow row : pkgWitheCust) {
                    if (row.getAttrValue(ATTR_CONTROLLED) == null) {
                        row.addAttribute(ATTR_CONTROLLED, "true");
                    }
                }
                addXmlRowToMap(values, pkgWitheCust);
            } catch (Exception e2) {
                HwLog.e("SmartMemoryCleanService", "parse /data/cust/xml/hw_powersaving_packagename_whitelist.xml failed!");
                e2.printStackTrace();
            }
            try {
                List<SimpleXmlRow> backWhitCust = XmlParsers.diskSimpleXmlRows(Const.HSM_PROTECT_DEFAULT_VALUE_FILE_OLD);
                for (SimpleXmlRow row2 : backWhitCust) {
                    if (row2.getAttrValue("check") == null) {
                        row2.addAttribute("check", "true");
                    }
                }
                addXmlRowToMap(values, backWhitCust);
            } catch (Exception e22) {
                HwLog.e("SmartMemoryCleanService", "parse /data/cust/xml/background_white_package_name.xml failed!");
                e22.printStackTrace();
            }
            return values;
        }
    }

    private static void addXmlRowToMap(Map<String, Map<String, String>> value, List<SimpleXmlRow> rows) {
        for (SimpleXmlRow row : rows) {
            String name = row.getAttrValue("name");
            if (!TextUtils.isEmpty(name)) {
                Map<String, String> attrs = (Map) value.get(name);
                if (attrs == null) {
                    value.put(name, new HashMap(row.getAttrMap()));
                } else {
                    attrs.putAll(row.getAttrMap());
                }
            }
        }
    }
}
