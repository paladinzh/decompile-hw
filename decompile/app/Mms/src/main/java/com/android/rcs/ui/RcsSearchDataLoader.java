package com.android.rcs.ui;

import android.net.Uri;
import com.android.mms.data.Contact;
import com.android.mms.data.Conversation;
import com.android.mms.ui.SearchDataLoader.Row;
import com.android.rcs.RcsCommonConfig;
import com.huawei.cspcommon.ex.AutoExtendArray;
import com.huawei.rcs.util.RCSConst;
import java.util.Locale;

public class RcsSearchDataLoader {
    private static final boolean mIsRcsOn = RcsCommonConfig.isRCSSwitchOn();

    public boolean isRcsSwitchOn() {
        return mIsRcsOn;
    }

    public Uri getNewUri(Uri uri, String searchString) {
        if (mIsRcsOn) {
            return RCSConst.RCS_URI_SEARCH.buildUpon().appendQueryParameter("pattern", searchString).build();
        }
        return uri;
    }

    public boolean isAddOtherRowContacts(int threadType) {
        boolean z = true;
        if (!mIsRcsOn) {
            return false;
        }
        if (!(threadType == 2 || threadType == 4)) {
            z = false;
        }
        return z;
    }

    public void addOtherRowContacts(AutoExtendArray<Row> list, Conversation conv, String number, String searchResult, String msgCountStr) {
        if (mIsRcsOn && conv != null && conv.getHwCust() != null) {
            switch (conv.getHwCust().getRcsThreadType()) {
                case 2:
                    list.add(new Row(conv.getThreadId(), number, msgCountStr, conv.getDate(), searchResult, searchResult, 301));
                    break;
                case 4:
                    number = ((Contact) conv.getRecipients().get(0)).getName();
                    long groupchatThreadID = conv.getHwCust().getGroupChatThreadId();
                    if (groupchatThreadID > 0) {
                        list.add(new Row(groupchatThreadID, number, msgCountStr, conv.getDate(), searchResult, searchResult, 302));
                        break;
                    }
                    break;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String getMatchStringForGroupChat(String groupName, String query) {
        if (mIsRcsOn && groupName != null && query != null && groupName.length() >= query.length() && groupName.toLowerCase(Locale.getDefault()).contains(query)) {
            return query;
        }
        return null;
    }
}
