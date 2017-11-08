package com.huawei.cspcommon.util;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.Data;

public class ContactQuery {
    public static final int ALTERNATIVE_LENGTH = FILTER_PROJECTION_ALTERNATIVE.length;
    private static final String[] CONTACT_BRIEF_INFO_PROJECTION = new String[]{"contact_id", "contact_last_updated_timestamp"};
    public static final String[] CONTACT_PROJECTION_ALTERNATIVE = new String[]{"_id", "display_name_alt", "contact_presence", "contact_status", "photo_id", "photo_thumb_uri", "lookup", "is_user_profile", "sort_key_alt", "name_raw_contact_id", "times_contacted", "company", "title", "starred", "is_user_profile", "starred_order"};
    public static final String[] CONTACT_PROJECTION_ALTERNATIVE_PRIVATE = new String[]{"_id", "display_name_alt", "contact_presence", "contact_status", "photo_id", "photo_thumb_uri", "lookup", "is_user_profile", "sort_key_alt", "name_raw_contact_id", "times_contacted", "company", "title", "is_private", "starred", "is_user_profile"};
    public static final String[] CONTACT_PROJECTION_PRIMARY = new String[]{"_id", "display_name", "contact_presence", "contact_status", "photo_id", "photo_thumb_uri", "lookup", "is_user_profile", "sort_key", "name_raw_contact_id", "times_contacted", "company", "title", "starred", "is_user_profile", "starred_order"};
    public static final String[] CONTACT_PROJECTION_PRIMARY_PRIVATE = new String[]{"_id", "display_name", "contact_presence", "contact_status", "photo_id", "photo_thumb_uri", "lookup", "is_user_profile", "sort_key", "name_raw_contact_id", "times_contacted", "company", "title", "is_private", "starred", "is_user_profile"};
    private static final String[] CONTACT_QUERYINFO_PROJECTION = new String[]{"_id", "contact_id", "mimetype", "data1", "data4", "display_name", "sort_key", "sort_key_alt", "times_contacted", "phonetic_name", "data2", "data3", "photo_id", "photo_thumb_uri", "lookup", "is_primary", "account_type", "account_name", "data_set", "company", "starred", "raw_contact_id", "name_raw_contact_id", "is_private", "has_phone_number", "send_to_voicemail", "contact_last_updated_timestamp", "display_name_alt", "is_camcard"};
    private static final String[] FILTER_PROJECTION_ALTERNATIVE = new String[]{"_id", "display_name_alt", "contact_presence", "contact_status", "photo_id", "photo_thumb_uri", "lookup", "is_user_profile", "snippet", "name_raw_contact_id", "sort_key_alt", "times_contacted", "company", "title", "starred", "is_user_profile"};
    public static final String[] FILTER_PROJECTION_ALTERNATIVE_PRIVATE = new String[]{"_id", "display_name_alt", "contact_presence", "contact_status", "photo_id", "photo_thumb_uri", "lookup", "is_user_profile", "snippet", "name_raw_contact_id", "sort_key_alt", "times_contacted", "company", "title", "is_private", "starred", "is_user_profile"};
    public static final String[] FILTER_PROJECTION_PRIMARY = new String[]{"_id", "display_name", "contact_presence", "contact_status", "photo_id", "photo_thumb_uri", "lookup", "is_user_profile", "snippet", "name_raw_contact_id", "sort_key", "times_contacted", "company", "title", "starred", "is_user_profile"};
    public static final String[] FILTER_PROJECTION_PRIMARY_PRIVATE = new String[]{"_id", "display_name", "contact_presence", "contact_status", "photo_id", "photo_thumb_uri", "lookup", "is_user_profile", "snippet", "name_raw_contact_id", "sort_key", "times_contacted", "company", "title", "is_private", "starred", "is_user_profile"};
    public static final String[] PROJECTION_RAW_CONTACT = new String[]{"contact_id", "display_name", "display_name_alt", "sort_key", "starred", "raw_contact_is_user_profile", "_id", "company"};
    public static final String[] PROJECTION_RAW_CONTACT_PRIVATE = new String[]{"contact_id", "display_name", "display_name_alt", "sort_key", "starred", "raw_contact_is_user_profile", "_id", "is_private", "company"};
    public static final String[] VOICE_SEARCH_PROJECTION = new String[]{"_id", "display_name", "contact_presence", "contact_status", "photo_id", "photo_thumb_uri", "lookup", "is_user_profile", "name_raw_contact_id", "sort_key", "times_contacted", "company", "starred"};

    public static Cursor queryContactInfo(Context c, String selection, String[] selectionArgs, String sortOrders) {
        return c.getContentResolver().query(Data.CONTENT_URI, CONTACT_QUERYINFO_PROJECTION, selection, selectionArgs, sortOrders);
    }

    public static String[] getProjectionFilterProjectionAlternative() {
        return (String[]) FILTER_PROJECTION_ALTERNATIVE.clone();
    }
}
