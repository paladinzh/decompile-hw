package com.android.contacts;

import android.content.Context;
import android.content.CursorLoader;
import android.net.Uri;
import android.provider.ContactsContract.Data;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.preference.ContactsPreferences;
import java.util.ArrayList;
import java.util.List;

public final class GroupMemberLoader extends CursorLoader {
    private final long mGroupId;

    public static class GroupDetailQuery {
        private static final String[] PROJECTION = new String[]{"contact_id", "photo_uri", "lookup", "display_name", "contact_presence", "contact_status", "photo_id", "company"};
        private static final String[] PROJECTION_PRIVATE;

        static {
            if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
                PROJECTION_PRIVATE = new String[(PROJECTION.length + 1)];
                System.arraycopy(PROJECTION, 0, PROJECTION_PRIVATE, 0, PROJECTION.length);
                PROJECTION_PRIVATE[PROJECTION.length] = "is_private";
                return;
            }
            PROJECTION_PRIVATE = PROJECTION;
        }
    }

    public static class GroupEditorQuery {
        private static final String[] PROJECTION = new String[]{"contact_id", "raw_contact_id", "display_name", "photo_uri", "lookup", "photo_id"};
        private static final String[] PROJECTION_PRIVATE;

        static {
            if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
                PROJECTION_PRIVATE = new String[(PROJECTION.length + 1)];
                System.arraycopy(PROJECTION, 0, PROJECTION_PRIVATE, 0, PROJECTION.length);
                PROJECTION_PRIVATE[PROJECTION.length] = "is_private";
                return;
            }
            PROJECTION_PRIVATE = PROJECTION;
        }
    }

    public static GroupMemberLoader constructLoaderForGroupEditorQuery(Context context, long groupId) {
        String[] -get1;
        if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
            -get1 = GroupEditorQuery.PROJECTION_PRIVATE;
        } else {
            -get1 = GroupEditorQuery.PROJECTION;
        }
        return new GroupMemberLoader(context, groupId, -get1, true);
    }

    public static GroupMemberLoader constructLoaderForGroupDetailQuery(Context context, long groupId) {
        String[] -get1;
        if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
            -get1 = GroupDetailQuery.PROJECTION_PRIVATE;
        } else {
            -get1 = GroupDetailQuery.PROJECTION;
        }
        return new GroupMemberLoader(context, groupId, -get1, false);
    }

    private GroupMemberLoader(Context context, long groupId, String[] projection, boolean isGroupBy) {
        super(context);
        this.mGroupId = groupId;
        setUri(createUri(isGroupBy));
        setProjection(projection);
        setSelection(createSelection());
        setSelectionArgs(createSelectionArgs());
        if (new ContactsPreferences(context).getSortOrder() == 1) {
            setSortOrder("sort_key");
        } else {
            setSortOrder("sort_key_alt");
        }
    }

    private Uri createUri(boolean isGroupBy) {
        Uri uri = Data.CONTENT_URI;
        if (isGroupBy) {
            return uri.buildUpon().appendQueryParameter("group_by", "contact_id").build();
        }
        return uri;
    }

    private String createSelection() {
        StringBuilder selection = new StringBuilder();
        selection.append("mimetype").append(" = ? AND ").append("data1").append(" = ? AND ").append("raw_contact_id").append(" in (select ").append("_id").append(" from raw_contacts where ").append("deleted").append(" =0)");
        return selection.toString();
    }

    private String[] createSelectionArgs() {
        List<String> selectionArgs = new ArrayList();
        selectionArgs.add("vnd.android.cursor.item/group_membership");
        selectionArgs.add(String.valueOf(this.mGroupId));
        return (String[]) selectionArgs.toArray(new String[0]);
    }
}
