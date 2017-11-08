package com.android.mms.ui;

import com.huawei.mms.util.HwCustSmartArchiveSettingUtilsImpl;
import java.util.regex.Pattern;

public class HwCustBaseConversationListFragmentImpl extends HwCustBaseConversationListFragment {
    public int getNumberTypeForServiceMessageArchival(String aRecipient, int aDefaultNumberType) {
        if (aRecipient == null) {
            return aDefaultNumberType;
        }
        if (aRecipient.length() != 9) {
            return 0;
        }
        for (String lStr : new String[]{"[A-Za-z]{2}-[\\dA-Za-z]{6}"}) {
            if (Pattern.compile(lStr).matcher(aRecipient).find()) {
                return 2;
            }
        }
        return 0;
    }

    public boolean isServiceMessageEnabled() {
        return HwCustSmartArchiveSettingUtilsImpl.SERVICE_MESSAGE_ARCHIVAL_ENABLED;
    }
}
