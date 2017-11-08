package com.huawei.systemmanager.comm.grule.scene.monitor;

import android.content.Context;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.huawei.systemmanager.comm.grule.GRuleException;
import com.huawei.systemmanager.comm.grule.rules.GPostRuleBase;
import com.huawei.systemmanager.util.HwLog;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonitorChecker {
    private static final String TAG = MonitorChecker.class.getSimpleName();
    private Map<String, List<GPostRuleBase<String>>> mMonitorRuleMap = new HashMap();
    private MonitorRuleParser mRuleParser;

    public boolean shouldMonitor(Context context, String scenarioKey, String pkgName) {
        boolean z;
        boolean z2 = true;
        if (context != null) {
            z = true;
        } else {
            z = false;
        }
        try {
            Preconditions.checkArgument(z, "context can't be null!");
            if (Strings.isNullOrEmpty(pkgName)) {
                z2 = false;
            }
            Preconditions.checkArgument(z2, "package name should be valid!");
            parseMonitorRuleFile(context);
            List<GPostRuleBase<String>> ruleList = getMonitorRuleList(scenarioKey);
            if (!ruleList.isEmpty()) {
                return shouldMonitorInner(context, ruleList, pkgName);
            }
        } catch (GRuleException ex) {
            HwLog.e(TAG, "shouldMonitor catch exception:" + ex.getMessage());
            ex.printStackTrace();
        } catch (IllegalArgumentException ex2) {
            HwLog.e(TAG, "shouldMonitor IllegalArgumentException:" + ex2.getMessage());
            ex2.printStackTrace();
        }
        return false;
    }

    private List<GPostRuleBase<String>> getMonitorRuleList(String scenarioKey) {
        if (this.mMonitorRuleMap.containsKey(scenarioKey)) {
            return (List) this.mMonitorRuleMap.get(scenarioKey);
        }
        return (List) this.mMonitorRuleMap.get(MonitorScenario.SCENARIO_COMMON);
    }

    private synchronized void parseMonitorRuleFile(Context context) {
        if (this.mRuleParser == null) {
            this.mRuleParser = new MonitorRuleParser();
            this.mRuleParser.parseRuleXml(context, this.mMonitorRuleMap);
        }
    }

    private <T> boolean shouldMonitorInner(Context context, List<GPostRuleBase<T>> ruleList, T pkgName) {
        for (GPostRuleBase<T> rule : ruleList) {
            rule.checkPostValid();
            int post = rule.getPost(rule.match(context, pkgName));
            if (2 != post) {
                if (post == 0) {
                    return false;
                }
                return true;
            }
        }
        throw new GRuleException("All rules' post are \"continue\", can't get match result!");
    }
}
