package com.android.mms.ui;

import android.net.Uri;
import android.text.TextUtils;
import com.android.mms.HwCustMmsConfigImpl;
import com.huawei.mms.util.HwMessageUtils;

public class HwCustSearchDataLoaderImpl extends HwCustSearchDataLoader {
    public static final String LOAD_TAG_MSG = "match:message";
    static final int QUERY_TOKEN_SEARCH_THREAD = 10002;

    public void asyncSearchMessageInnerThread(String aSearchString, String aThreadId, SearchDataLoader aSearchDataLoader) {
        if (!TextUtils.isEmpty(aSearchString) && HwCustMmsConfigImpl.getSupportSearchConversation() && aSearchDataLoader != null && aThreadId != null) {
            Uri uri = Uri.parse("content://mms-sms/search_thread").buildUpon().appendQueryParameter("pattern", HwMessageUtils.formatSqlString(aSearchString)).appendQueryParameter("threadId", aThreadId).build();
            if (aSearchDataLoader.hasMessages(QUERY_TOKEN_SEARCH_THREAD)) {
                aSearchDataLoader.removeMessages(QUERY_TOKEN_SEARCH_THREAD);
            }
            aSearchDataLoader.startQuery(QUERY_TOKEN_SEARCH_THREAD, LOAD_TAG_MSG, uri, null, null, null, null);
        }
    }
}
