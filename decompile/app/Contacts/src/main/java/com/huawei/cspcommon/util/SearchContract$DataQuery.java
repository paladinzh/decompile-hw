package com.huawei.cspcommon.util;

public class SearchContract$DataQuery {
    public static final String[] CONTACT_SEARCH_PROJECTION = new String[]{"contact_id", "_id", "display_name", "display_name_alt", "photo_id", "photo_thumb_uri", "lookup", "NULL AS data1", "NULL AS data2", "NULL AS data3", "NULL AS mimetype", "0 AS is_primary", "company", "number_ori", "email_ori", "organization_ori", "nick_name_ori", "address_ori"};
    public static final String[] DATA_SEARCH_PROJECTION = new String[]{"contact_id", "data_id", "display_name", "display_name_alt", "photo_id", "photo_uri", "lookup", "data1", "data2", "data3", "NULL AS mimetype", "is_primary", "company"};
}
