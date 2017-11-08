package rcstelephony;

import android.net.Uri;
import android.provider.BaseColumns;

public final class RcsMessagingConstants$MmsSms implements BaseColumns {
    public static final Uri CONTENT_CONVERSATIONS_URI = Uri.parse("content://mms-sms/conversations");
    public static final Uri CONTENT_DRAFT_URI = Uri.parse("content://mms-sms/draft");
    public static final Uri CONTENT_FILTER_BYPHONE_URI = Uri.parse("content://mms-sms/messages/byphone");
    public static final Uri CONTENT_LOCKED_URI = Uri.parse("content://mms-sms/locked");
    public static final Uri CONTENT_UNDELIVERED_URI = Uri.parse("content://mms-sms/undelivered");
    public static final Uri CONTENT_URI = Uri.parse("content://mms-sms/");
    public static final Uri SEARCH_URI = Uri.parse("content://mms-sms/search");
}
