package com.huawei.systemmanager.comm.grule;

import android.content.Context;
import com.google.android.collect.Maps;
import com.google.common.base.Joiner;
import com.huawei.systemmanager.comm.grule.rules.IRule;
import com.huawei.systemmanager.comm.xml.XmlParserException;
import com.huawei.systemmanager.comm.xml.XmlParsers;
import com.huawei.systemmanager.util.HwLog;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GRuleFactory {
    private static final String ASSETS_GRULE_ALL_CONCRETE_RULES = "grule/all_concrete_rules.xml";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_PATH = "path";
    private static final String TAG = GRuleFactory.class.getSimpleName();
    private static final String TAG_PACKAGE = "Package";
    private static final String TAG_RULE = "Rule";
    private static Map<String, IRule<?>> ruleMap = Maps.newHashMap();

    public static IRule<?> findRule(Context context, String ruleKey) throws GRuleException {
        parseConcreteRule(context);
        if (ruleMap.containsKey(ruleKey)) {
            return (IRule) ruleMap.get(ruleKey);
        }
        throw new GRuleException("can't find rule for key:" + ruleKey);
    }

    private static synchronized void parseConcreteRule(Context context) {
        synchronized (GRuleFactory.class) {
            if (ruleMap.isEmpty()) {
                try {
                    NodeList packageList = XmlParsers.assetXmlRootElement(context, ASSETS_GRULE_ALL_CONCRETE_RULES).getElementsByTagName(TAG_PACKAGE);
                    for (int i = 0; i < packageList.getLength(); i++) {
                        Element element = (Element) packageList.item(i);
                        parsePackageNode(element.getAttribute("path"), element);
                    }
                } catch (XmlParserException ex) {
                    HwLog.e(TAG, "parseConcreteRule catch exception:" + ex.getMessage());
                }
            } else {
                return;
            }
        }
        return;
    }

    private static void parsePackageNode(String basePath, Element element) {
        NodeList ruleList = element.getElementsByTagName(TAG_RULE);
        for (int i = 0; i < ruleList.getLength(); i++) {
            cacheRule(Joiner.on(".").join(basePath, ((Element) ruleList.item(i)).getAttribute("name"), new Object[0]));
        }
    }

    private static void cacheRule(String clazzPath) {
        try {
            Class<?> clazz = Class.forName(clazzPath);
            ruleMap.put(clazz.getSimpleName(), (IRule) clazz.newInstance());
        } catch (ClassNotFoundException e) {
            HwLog.e(TAG, "cacheRule catch ClassNotFoundException:" + clazzPath);
        } catch (IllegalAccessException ex) {
            HwLog.e(TAG, "cacheRule catch IllegalAccessException:" + clazzPath);
            ex.printStackTrace();
        } catch (InstantiationException ex2) {
            HwLog.e(TAG, "cacheRule catch InstantiationException:" + clazzPath);
            ex2.printStackTrace();
        }
    }
}
