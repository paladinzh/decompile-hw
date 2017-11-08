package com.android.contacts.editor;

import com.google.android.gms.R;
import com.google.common.collect.Maps;
import java.util.HashMap;

public class EditorUiUtils {
    private static final HashMap<String, Integer> mimetypeLayoutMap = Maps.newHashMap();

    static {
        mimetypeLayoutMap.put("#phoneticName", Integer.valueOf(R.layout.phonetic_name_editor_view));
        mimetypeLayoutMap.put("vnd.android.cursor.item/name", Integer.valueOf(R.layout.structured_name_editor_view));
        mimetypeLayoutMap.put("vnd.android.cursor.item/group_membership", Integer.valueOf(-1));
        mimetypeLayoutMap.put("vnd.android.cursor.item/photo", Integer.valueOf(-1));
        mimetypeLayoutMap.put("vnd.android.cursor.item/phone_v2", Integer.valueOf(R.layout.text_fields_editor_view_simple));
        mimetypeLayoutMap.put("vnd.android.cursor.item/email_v2", Integer.valueOf(R.layout.text_fields_editor_view_simple));
        mimetypeLayoutMap.put("vnd.android.cursor.item/contact_event", Integer.valueOf(R.layout.event_field_editor_view));
        mimetypeLayoutMap.put("vnd.android.cursor.item/organization", Integer.valueOf(R.layout.organisation_name_editor_view));
        mimetypeLayoutMap.put("vnd.android.huawei.cursor.item/ringtone", Integer.valueOf(R.layout.listitem_ringtone_without_header));
    }

    public static int getLayoutResourceId(String mimetype) {
        Integer id = (Integer) mimetypeLayoutMap.get(mimetype);
        if (id == null) {
            return R.layout.text_fields_editor_view;
        }
        return id.intValue();
    }
}
