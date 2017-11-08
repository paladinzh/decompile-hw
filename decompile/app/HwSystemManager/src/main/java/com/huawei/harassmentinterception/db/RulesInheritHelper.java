package com.huawei.harassmentinterception.db;

import android.content.Context;
import com.huawei.harassmentinterception.strategy.StrategyConfigs.StrategyId;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.valueprefer.ValuePair;
import java.util.ArrayList;

public class RulesInheritHelper {
    private static final String TAG = "RulesInheritHelper";

    static void inheritRulesFrom8(Context ctx, int strategy) {
        boolean isBlockBlackListEnable = (StrategyId.BLOCK_BLACKLIST.getValue() & strategy) != 0;
        boolean isBlockStrangerEnable = (StrategyId.BLOCK_STRANGER.getValue() & strategy) != 0;
        boolean isBlockUnknowEnable = (StrategyId.BLOCK_UNKNOWN.getValue() & strategy) != 0;
        boolean isBlockAll = (StrategyId.BLOCK_ALL.getValue() & strategy) != 0;
        boolean isIntelligentEnable = (StrategyId.BLOCK_INTELLIGENT.getValue() & strategy) != 0;
        boolean isBlockKeywords = (StrategyId.BLOCK_KEYWORDS.getValue() & strategy) != 0;
        ArrayList<ValuePair> values = HsmCollections.newArrayList();
        values.add(RulesOps.createValuePair(RulesOps.KEY_BLOCK_BLACK_LIST_CALL, 1, isBlockBlackListEnable));
        values.add(RulesOps.createValuePair(RulesOps.KEY_BLOCK_UNKONW_CALL, 1, isBlockUnknowEnable));
        values.add(RulesOps.createValuePair(RulesOps.KEY_BLOCK_STRANGER_CALL, 1, isBlockStrangerEnable));
        values.add(RulesOps.createValuePair(RulesOps.KEY_BLOCK_ALL_CALL, 1, isBlockAll));
        values.add(RulesOps.createValuePair(RulesOps.KEY_INTELL_BLOCK_MSG, 1, isIntelligentEnable));
        values.add(RulesOps.createValuePair(RulesOps.KEY_BLOCK_STRANGER_MSG, 1, isBlockStrangerEnable));
        String str = RulesOps.KEY_BLOCK_BLACK_LIST_MSG;
        if (isBlockBlackListEnable) {
            isBlockKeywords = true;
        }
        values.add(RulesOps.createValuePair(str, 1, isBlockKeywords));
        RulesOps.setRules(ctx, values);
    }
}
