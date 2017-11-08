package com.android.mms.ui;

import android.net.Uri;
import com.android.mms.data.Conversation;
import com.android.mms.ui.SearchDataLoader.Row;
import com.huawei.cspcommon.ex.AutoExtendArray;

public class HwCustSearchDataLoader {
    public boolean isRcsSwitchOn() {
        return false;
    }

    public Uri getNewUri(Uri uri, String searchString) {
        return uri;
    }

    public boolean isAddOtherRowContacts(int threadType) {
        return false;
    }

    public void addOtherRowContacts(AutoExtendArray<Row> autoExtendArray, Conversation conv, String number, String searchResult, String msgCountStr) {
    }

    public String getMatchStringForGroupChat(String groupName, String query) {
        return null;
    }

    public void asyncSearchMessageInnerThread(String aSearchString, String aThreadId, SearchDataLoader aSearchDataLoader) {
    }
}
