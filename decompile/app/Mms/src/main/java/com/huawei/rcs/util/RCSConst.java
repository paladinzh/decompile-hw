package com.huawei.rcs.util;

import android.net.Uri;

public class RCSConst {
    public static final Uri RCSEALIAS_URI = Uri.parse("content://com.huawei.rcse.provider/aliasinfo");
    public static final Uri RCSEUSERBLOCKED_URI = Uri.parse("content://com.huawei.rcse.provider/userblockedinfo");
    public static final Uri RCS_URI_CONVERSATIONS = Uri.parse("content://rcsim/conversations");
    public static final Uri RCS_URI_CONVERSATIONS_GROUP_ID = Uri.parse("content://rcsim/conversations_group_id");
    public static final Uri RCS_URI_DELETE_RCS_THREAD = Uri.parse("content://rcsim/delete_rcs_thread");
    public static final Uri RCS_URI_GET_SMS_THREAD = Uri.parse("content://rcsim/get_sms_thread");
    public static final Uri RCS_URI_SEARCH = Uri.parse("content://rcsim/search");
}
