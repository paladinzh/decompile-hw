package com.huawei.harassmentinterception.db;

import android.content.Context;
import com.huawei.permissionmanager.db.DBHelper;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.xml.XmlParserException;
import com.huawei.systemmanager.comm.xml.XmlParsers;
import com.huawei.systemmanager.comm.xml.base.SimpleXmlRow;
import com.huawei.systemmanager.util.HwLog;
import java.util.Map;
import java.util.Set;

public class HarasInitConfiguration {
    private static final String INITCONFIG_FILE_PATH = "harassment/init_config.xml";
    public static final String KEY_SUGGEST_VALUE = "intelligent_call_rules_suggestvalue";
    private static final String TAG = "InitConfiguration";
    private Map<String, Integer> mValues = HsmCollections.newArrayMap();

    private HarasInitConfiguration() {
    }

    private void initXml(Context ctx) {
        try {
            for (SimpleXmlRow row : XmlParsers.assetSimpleXmlRows(ctx, INITCONFIG_FILE_PATH)) {
                this.mValues.put(row.getAttrValue("name"), Integer.valueOf(row.getAttrInteger(DBHelper.VALUE)));
            }
        } catch (XmlParserException e) {
            HwLog.e(TAG, "initXml failed", e);
        } catch (Exception e2) {
            HwLog.e(TAG, "initXml failed", e2);
        }
    }

    public int getValue(String name) {
        Integer value = (Integer) this.mValues.get(name);
        if (value != null) {
            return value.intValue();
        }
        HwLog.e(TAG, "count not found value by name:" + name);
        return 0;
    }

    public Set<String> getDefaultEnableIntellItems() {
        Set<String> result = HsmCollections.newArraySet();
        if (getValue("intelligent_call_rules_scam") == 3) {
            result.add("scam");
        }
        if (getValue("intelligent_call_rules_harassment") == 3) {
            result.add("harassment");
        }
        if (getValue("intelligent_call_rules_advertise") == 3) {
            result.add("advertise");
        }
        if (getValue("intelligent_call_rules_estate") == 3) {
            result.add("estate");
        }
        return result;
    }

    public static HarasInitConfiguration getInitConfiguration(Context ctx) {
        HarasInitConfiguration config = new HarasInitConfiguration();
        config.initXml(ctx);
        return config;
    }
}
