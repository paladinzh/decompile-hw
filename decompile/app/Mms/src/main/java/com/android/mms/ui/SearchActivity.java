package com.android.mms.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import com.google.android.gms.actions.SearchIntents;
import com.huawei.mms.ui.HwListActivity;
import com.huawei.mms.ui.SearchMsgUtils;

public class SearchActivity extends HwListActivity {
    private SearchMsgUtils mSearchMsgUtils = null;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        String searchStringParameter = intent.getStringExtra(SearchIntents.EXTRA_QUERY);
        if (searchStringParameter == null) {
            searchStringParameter = intent.getStringExtra("user_query");
        }
        if (searchStringParameter == null) {
            searchStringParameter = intent.getStringExtra("intent_extra_data_key");
        }
        if (TextUtils.isEmpty(searchStringParameter)) {
            searchStringParameter = null;
        } else {
            searchStringParameter = searchStringParameter.trim();
        }
        String searchString = searchStringParameter;
        if (TextUtils.isEmpty(searchStringParameter)) {
            finish();
            return;
        }
        Uri u = intent.getData();
        if (this.mSearchMsgUtils == null) {
            this.mSearchMsgUtils = new SearchMsgUtils(this);
        }
        if (u != null && this.mSearchMsgUtils.gotoTargetActivity(u, searchString)) {
            finish();
        }
    }
}
