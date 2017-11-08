package com.huawei.systemmanager.comm.grule.rules.xml;

import android.content.Context;
import com.google.android.collect.Sets;
import com.google.common.base.Strings;
import com.huawei.systemmanager.comm.grule.rules.IRule;
import com.huawei.systemmanager.comm.xml.XmlParserException;
import com.huawei.systemmanager.util.HwLog;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

abstract class XmlRuleBase implements IRule<String> {
    private static final String ATTR_NAME = "name";
    private static final String CACHE_KEY_DEFAULT = "default_cache_key";
    private static final String TAG = XmlRuleBase.class.getSimpleName();
    private static final String TAG_PACKAGE = "package";
    private static Set<String> cachedKeys = Sets.newHashSet();
    private static Map<String, List<String>> xmlMultimap = new HashMap();

    abstract String getDiskCustFilePath();

    abstract String getMatchingKey();

    abstract List<String> getPackageList(Context context, String str, String str2);

    XmlRuleBase() {
    }

    public boolean match(Context context, String pkgName) {
        if (Strings.isNullOrEmpty(pkgName)) {
            return false;
        }
        parseAndCacheList(context);
        return xmlContainPkg(pkgName);
    }

    private synchronized void parseAndCacheList(Context context) {
        if (!matchingKeyAlreadyCached()) {
            try {
                List<String> pkgList = getPackageList(context, "package", "name");
                HwLog.d(TAG, "parseAndCacheList " + getMatchingKey() + " :" + pkgList);
                savePackageList(pkgList);
            } catch (XmlParserException ex) {
                HwLog.e(TAG, "parseAndCacheList catch exception:" + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private boolean matchingKeyAlreadyCached() {
        return cachedKeys.contains(matchingKeyInner());
    }

    private void savePackageList(List<String> pkgList) {
        try {
            cachedKeys.add(matchingKeyInner());
            xmlMultimap.put(matchingKeyInner(), pkgList);
        } catch (ArrayIndexOutOfBoundsException e) {
            HwLog.e(TAG, "savePackageList failed, ArrayIndexOutOfBoundsException detected!");
        }
    }

    private boolean xmlContainPkg(String pkgName) {
        if (xmlMultimap == null || xmlMultimap.size() == 0 || xmlMultimap.get(matchingKeyInner()) == null) {
            return false;
        }
        return ((List) xmlMultimap.get(matchingKeyInner())).contains(pkgName);
    }

    private String matchingKeyInner() {
        if (getMatchingKey() == null) {
            return CACHE_KEY_DEFAULT;
        }
        return getMatchingKey();
    }
}
