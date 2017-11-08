package com.huawei.powergenie.core.device;

import android.util.Log;
import com.huawei.powergenie.core.StateAction;
import com.huawei.powergenie.integration.eventhub.MsgEvent;
import java.util.ArrayList;
import java.util.HashMap;

public final class StateActionFactory {
    private static final ArrayList<StateAction> mFreeStatePool = new ArrayList();
    private static final HashMap<Integer, Integer> mMsgIdActionIdMap = new HashMap<Integer, Integer>() {
        {
            put(Integer.valueOf(300), Integer.valueOf(300));
            put(Integer.valueOf(301), Integer.valueOf(301));
            put(Integer.valueOf(302), Integer.valueOf(302));
            put(Integer.valueOf(303), Integer.valueOf(303));
            put(Integer.valueOf(304), Integer.valueOf(304));
            put(Integer.valueOf(305), Integer.valueOf(305));
            put(Integer.valueOf(306), Integer.valueOf(306));
            put(Integer.valueOf(307), Integer.valueOf(307));
            put(Integer.valueOf(308), Integer.valueOf(308));
            put(Integer.valueOf(309), Integer.valueOf(309));
            put(Integer.valueOf(310), Integer.valueOf(310));
            put(Integer.valueOf(311), Integer.valueOf(311));
            put(Integer.valueOf(312), Integer.valueOf(312));
            put(Integer.valueOf(313), Integer.valueOf(313));
            put(Integer.valueOf(314), Integer.valueOf(314));
            put(Integer.valueOf(315), Integer.valueOf(315));
            put(Integer.valueOf(318), Integer.valueOf(318));
            put(Integer.valueOf(319), Integer.valueOf(319));
            put(Integer.valueOf(320), Integer.valueOf(320));
            put(Integer.valueOf(321), Integer.valueOf(321));
            put(Integer.valueOf(322), Integer.valueOf(322));
            put(Integer.valueOf(323), Integer.valueOf(323));
            put(Integer.valueOf(324), Integer.valueOf(324));
            put(Integer.valueOf(325), Integer.valueOf(325));
            put(Integer.valueOf(326), Integer.valueOf(326));
            put(Integer.valueOf(327), Integer.valueOf(327));
            put(Integer.valueOf(328), Integer.valueOf(328));
            put(Integer.valueOf(329), Integer.valueOf(329));
            put(Integer.valueOf(333), Integer.valueOf(333));
            put(Integer.valueOf(334), Integer.valueOf(334));
            put(Integer.valueOf(337), Integer.valueOf(337));
            put(Integer.valueOf(338), Integer.valueOf(338));
            put(Integer.valueOf(356), Integer.valueOf(356));
            put(Integer.valueOf(357), Integer.valueOf(357));
            put(Integer.valueOf(359), Integer.valueOf(359));
            put(Integer.valueOf(360), Integer.valueOf(360));
        }
    };

    protected static StateAction makeAction(MsgEvent event) {
        Integer actionId = (Integer) mMsgIdActionIdMap.get(Integer.valueOf(event.getEventId()));
        if (actionId == null) {
            Log.i("StateActionFactory", "not make action for msgId: " + event.getEventId());
            return null;
        }
        StateAction stAction = StateAction.obtain();
        if (stAction != null) {
            stAction.resetAs(actionId.intValue(), "message", 1, event.getTimeStamp(), event.getIntent());
        }
        return stAction;
    }
}
