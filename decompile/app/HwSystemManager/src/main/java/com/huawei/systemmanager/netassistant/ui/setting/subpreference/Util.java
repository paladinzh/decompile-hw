package com.huawei.systemmanager.netassistant.ui.setting.subpreference;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.text.Editable;
import android.text.TextWatcher;
import com.huawei.netassistant.util.CommonConstantUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.netassistant.ui.Item.CardItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class Util {

    public static class SimpleTextWatcher implements TextWatcher {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
        }
    }

    public static void setCardToPreference(PreferenceGroup preferenceGroup, CardItem card) {
        if (preferenceGroup != null && card != null) {
            int count = preferenceGroup.getPreferenceCount();
            for (int i = 0; i < count; i++) {
                Preference perfer = preferenceGroup.getPreference(i);
                if (perfer instanceof PreferenceGroup) {
                    setCardToPreference((PreferenceGroup) perfer, card);
                    return;
                }
                if (perfer instanceof ICardPrefer) {
                    ((ICardPrefer) perfer).setCard(card);
                }
            }
        }
    }

    public static void refreshPreferenceShow(PreferenceGroup preferenceGroup) {
        if (preferenceGroup != null) {
            int count = preferenceGroup.getPreferenceCount();
            for (int i = 0; i < count; i++) {
                Preference perfer = preferenceGroup.getPreference(i);
                if (perfer instanceof PreferenceGroup) {
                    refreshPreferenceShow((PreferenceGroup) perfer);
                    return;
                }
                if (perfer instanceof ICardPrefer) {
                    ((ICardPrefer) perfer).refreshPreferShow();
                }
            }
        }
    }

    public static void initOverFlowArray(Context ctx, List<String> overFlowList) {
        overFlowList.clear();
        overFlowList.add(ctx.getString(R.string.sub_content_overflow_notify_mention));
        overFlowList.add(ctx.getString(R.string.sub_content_overflow_notify_disconnect));
    }

    public static String getOverFlowTypeString(int valueType, List<String> stringList) {
        if (valueType == -1) {
            valueType = 1;
        }
        if (stringList == null || stringList.isEmpty() || valueType < 1) {
            return null;
        }
        return (String) stringList.get(valueType - 1);
    }

    public static CardItem getCardFromActivityIntent(Activity ac, String tag) {
        if (ac == null) {
            HwLog.e(tag, "getCardFromActivity getActivity is null");
            return null;
        }
        Intent intent = ac.getIntent();
        if (intent == null) {
            HwLog.e(tag, "getCardFromActivity getIntent is null");
            return null;
        }
        CardItem item = (CardItem) intent.getParcelableExtra(CommonConstantUtil.KEY_NETASSISTANT_CARD_ITEM);
        if (item != null) {
            return item;
        }
        HwLog.e(tag, "getCardFromActivity getCarditem is null");
        return null;
    }
}
