package com.huawei.systemmanager.startupmgr.confdata;

import android.content.Context;
import com.google.common.collect.Sets;
import com.huawei.systemmanager.comm.xml.XmlParsers;
import java.util.Set;

class CompetitorAppsParser {
    private static final String ASSET_XML_ATTR = "name";
    private static final String ASSET_XML_TAG = "package";
    private static final String COMPETITOR_APP_LIST_ASSET_FILE = "startupmgr/competitor_apps.xml";
    private Set<String> mCompetitorAppSet = Sets.newHashSet();

    CompetitorAppsParser(Context ctx) {
        this.mCompetitorAppSet.addAll(XmlParsers.xmlAttrValueList(ctx, null, COMPETITOR_APP_LIST_ASSET_FILE, XmlParsers.getTagAttrMatchPredicate("package", "name"), XmlParsers.getRowToAttrValueFunc("name")));
    }

    boolean isCompetitorApp(String pkgName) {
        return this.mCompetitorAppSet.contains(pkgName);
    }
}
