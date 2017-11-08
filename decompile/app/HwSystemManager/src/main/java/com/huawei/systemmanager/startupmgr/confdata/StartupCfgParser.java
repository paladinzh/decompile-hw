package com.huawei.systemmanager.startupmgr.confdata;

import android.content.Context;
import com.google.android.collect.Maps;
import com.google.common.base.Function;
import com.huawei.systemmanager.comm.xml.XmlParsers;
import com.huawei.systemmanager.comm.xml.base.SimpleXmlRow;
import java.util.Map;

public class StartupCfgParser {
    private static final String ASSET_XML_ATTR_NAME = "name";
    private static final String ASSET_XML_ATTR_R = "r";
    private static final String ASSET_XML_ATTR_SP = "sp";
    private static final String ASSET_XML_TAG = "package";
    private static final int CFG_MASK_R = 1;
    private static final int CFG_MASK_SP = 2;
    private static final String COMPETITOR_APP_LIST_ASSET_FILE = "startupmgr/startup_default_cfg.xml";
    private Map<String, Integer> mLocalCfg = Maps.newHashMap();

    private static class MaskTransformFunction implements Function<SimpleXmlRow, Integer> {
        private MaskTransformFunction() {
        }

        public Integer apply(SimpleXmlRow simpleXmlRow) {
            if (simpleXmlRow == null) {
                return null;
            }
            int result = 0;
            if ("1".equals(simpleXmlRow.getAttrValue("r"))) {
                result = 1;
            }
            if ("1".equals(simpleXmlRow.getAttrValue("sp"))) {
                result |= 2;
            }
            return Integer.valueOf(result);
        }
    }

    StartupCfgParser(Context ctx) {
        loadXml(ctx);
    }

    boolean getRDefaultValue(String pkgName) {
        boolean z = false;
        Integer value = (Integer) this.mLocalCfg.get(pkgName);
        if (value == null) {
            return false;
        }
        if ((value.intValue() & 1) != 0) {
            z = true;
        }
        return z;
    }

    boolean getSPDefaultValue(String pkgName) {
        boolean z = false;
        Integer value = (Integer) this.mLocalCfg.get(pkgName);
        if (value == null) {
            return false;
        }
        if ((value.intValue() & 2) != 0) {
            z = true;
        }
        return z;
    }

    private void loadXml(Context ctx) {
        this.mLocalCfg = XmlParsers.assetAttrsToMap(ctx, COMPETITOR_APP_LIST_ASSET_FILE, XmlParsers.getTagAttrMatchPredicate3("package", "name", "r", "sp"), XmlParsers.getRowToAttrValueFunc("name"), new MaskTransformFunction());
    }
}
