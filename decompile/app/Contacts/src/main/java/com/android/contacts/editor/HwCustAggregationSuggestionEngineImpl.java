package com.android.contacts.editor;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.android.contacts.util.HwCustContactFeatureUtils;
import com.google.android.gms.actions.SearchIntents;

public class HwCustAggregationSuggestionEngineImpl extends HwCustAggregationSuggestionEngine {
    public void getDisplayNameForQuery(Uri uri, StringBuilder sb) {
        if (HwCustContactFeatureUtils.isJoinFeatureEnabled() && uri != null && sb != null) {
            try {
                String displayName = uri.getQueryParameter(SearchIntents.EXTRA_QUERY);
                if (!TextUtils.isEmpty(displayName) && displayName.contains(":")) {
                    sb.append(" AND display_name = '").append(displayName.substring(displayName.indexOf(58) + 1)).append("'");
                }
            } catch (UnsupportedOperationException e) {
                Log.i("AggregationSuggestionEngine", e + " Caught and Handled");
            } catch (NullPointerException e2) {
                Log.i("AggregationSuggestionEngine", e2 + " Caught and Handled");
            } catch (StringIndexOutOfBoundsException e3) {
                Log.i("AggregationSuggestionEngine", e3 + " Caught and Handled");
            }
        }
    }

    public void getDisplayNameForQuery(Uri uri, StringBuilder sb, String[] selectionArgs) {
        if (HwCustContactFeatureUtils.isJoinFeatureEnabled() && uri != null && sb != null) {
            try {
                String displayName = uri.getQueryParameter(SearchIntents.EXTRA_QUERY);
                if (!TextUtils.isEmpty(displayName) && displayName.contains(":")) {
                    displayName = displayName.substring(displayName.indexOf(58) + 1);
                    sb.append(" AND display_name = ?");
                    selectionArgs[0] = displayName;
                }
            } catch (UnsupportedOperationException e) {
                Log.i("AggregationSuggestionEngine", e + " Caught and Handled");
            } catch (NullPointerException e2) {
                Log.i("AggregationSuggestionEngine", e2 + " Caught and Handled");
            } catch (StringIndexOutOfBoundsException e3) {
                Log.i("AggregationSuggestionEngine", e3 + " Caught and Handled");
            }
        }
    }
}
