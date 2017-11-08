package com.android.contacts;

import android.content.Context;
import android.content.CursorLoader;
import android.net.Uri;
import android.provider.ContactsContract.Groups;

public final class GroupMetaDataLoader extends CursorLoader {
    private static final String[] COLUMNS = new String[]{"account_name", "account_type", "data_set", "_id", "title", "auto_add", "favorites", "group_is_read_only", "deleted", "sync4", "title_res", "res_package", "sync1"};
    private static final String GROUP_LIST_SORT_ORDER = ("account_type" + " , " + "account_name" + " , " + "data_set" + " , " + "title" + " COLLATE LOCALIZED ASC");

    public GroupMetaDataLoader(Context context, Uri groupUri) {
        super(context, ensureIsGroupUri(groupUri), COLUMNS, "account_type NOT NULL AND account_name NOT NULL AND deleted = 0", null, GROUP_LIST_SORT_ORDER);
    }

    public GroupMetaDataLoader(Context context, Uri groupUri, boolean excludePrivateGroup) {
        super(context, ensureIsGroupUri(groupUri), COLUMNS, "account_type NOT NULL AND account_name NOT NULL AND deleted = 0 AND (sync1!= 'Private contacts' OR sync1 IS NULL)", null, GROUP_LIST_SORT_ORDER);
    }

    public GroupMetaDataLoader(Context context, Uri groupUri, String selections, String[] SelectionArgs) {
        super(context, ensureIsGroupUri(groupUri), COLUMNS, selections, SelectionArgs, GROUP_LIST_SORT_ORDER);
    }

    private static Uri ensureIsGroupUri(Uri groupUri) {
        if (groupUri == null) {
            throw new IllegalArgumentException("Uri must not be null");
        } else if (groupUri.toString().startsWith(Groups.CONTENT_URI.toString())) {
            return groupUri;
        } else {
            throw new IllegalArgumentException("Invalid group Uri: " + groupUri);
        }
    }
}
