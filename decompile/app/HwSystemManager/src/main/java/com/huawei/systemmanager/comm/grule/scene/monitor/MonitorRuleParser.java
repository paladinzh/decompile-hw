package com.huawei.systemmanager.comm.grule.scene.monitor;

import android.content.Context;
import com.google.android.collect.Maps;
import com.google.common.base.Preconditions;
import com.huawei.systemmanager.comm.grule.GRuleException;
import com.huawei.systemmanager.comm.grule.GRuleFactory;
import com.huawei.systemmanager.comm.grule.rules.GPostRuleBase;
import com.huawei.systemmanager.comm.grule.rules.GPostRuleBase.PostStringKey;
import com.huawei.systemmanager.comm.grule.rules.GPostRuleBase.RuleAttrKey;
import com.huawei.systemmanager.comm.xml.XmlParserException;
import com.huawei.systemmanager.comm.xml.XmlParsers;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class MonitorRuleParser {
    private static String ASSET_GRULE_MONITOR_RULE = "grule/monitor_rule.xml";
    private static final String TAG = MonitorRuleParser.class.getSimpleName();
    private static String TAG_RULE = "Rule";
    private static String TAG_SCENARIO = "Scenario";
    private static Map<String, Integer> postMap = Maps.newHashMap();

    MonitorRuleParser() {
    }

    static {
        postMap.put("allow", Integer.valueOf(0));
        postMap.put(PostStringKey.VAL_POST_MONITOR, Integer.valueOf(1));
        postMap.put(PostStringKey.VAL_POST_CONTINUE, Integer.valueOf(2));
    }

    public void parseRuleXml(Context context, Map<String, List<GPostRuleBase<String>>> ruleMapResult) {
        try {
            Preconditions.checkArgument(ruleMapResult != null, "the result object can't be null");
            NodeList scenarioList = XmlParsers.assetXmlRootElement(context, ASSET_GRULE_MONITOR_RULE).getElementsByTagName(TAG_SCENARIO);
            for (int i = 0; i < scenarioList.getLength(); i++) {
                Element element = (Element) scenarioList.item(i);
                parseScenario(context, element.getAttribute("name"), element, ruleMapResult);
            }
        } catch (XmlParserException ex) {
            HwLog.e(TAG, "parseRuleXml catch XmlParserException!");
            ex.printStackTrace();
        } catch (IllegalArgumentException ex2) {
            HwLog.e(TAG, "parseRuleXml catch IllegalArgumentException:" + ex2.getMessage());
            ex2.printStackTrace();
        }
    }

    private void parseScenario(Context context, String name, Element element, Map<String, List<GPostRuleBase<String>>> ruleMapResult) {
        List<GPostRuleBase<String>> ruleMapList = new ArrayList();
        NodeList ruleList = element.getElementsByTagName(TAG_RULE);
        for (int i = 0; i < ruleList.getLength(); i++) {
            GPostRuleBase<String> rule = parseRule(context, (Element) ruleList.item(i));
            if (rule != null) {
                ruleMapList.add(rule);
            }
        }
        ruleMapResult.put(name, ruleMapList);
        HwLog.d(TAG, "parseRule result: " + ruleMapResult);
    }

    private GPostRuleBase<String> parseRule(Context context, Element element) {
        try {
            GPostRuleBase<String> ruleBase = new GPostRuleBase(GRuleFactory.findRule(context, element.getAttribute("name")));
            String matchPost = element.getAttribute(RuleAttrKey.ATTR_MATCH_POST);
            String mismatchPost = element.getAttribute(RuleAttrKey.ATTR_MISMATCH_POST);
            ruleBase.setMatchPost(getPostOp(matchPost));
            ruleBase.setMismatchPost(getPostOp(mismatchPost));
            return ruleBase;
        } catch (GRuleException ex) {
            HwLog.e(TAG, "parseRule catch GRuleException:" + ex.getMessage());
            HwLog.e(TAG, "parseRule failed, return null");
            return null;
        } catch (Exception ex2) {
            HwLog.e(TAG, "parseRule catch Exception:" + ex2.getMessage());
            HwLog.e(TAG, "parseRule failed, return null");
            return null;
        }
    }

    private int getPostOp(String key) {
        if (postMap.containsKey(key)) {
            return ((Integer) postMap.get(key)).intValue();
        }
        throw new GRuleException("Invalid post string value!");
    }
}
