package com.huawei.harassmentinterception.common;

import android.provider.BaseColumns;

public class Tables {
    public static final String BLACKLIST_TABLE = "interception_blacklist";
    public static final String BLACKLIST_VIEW = "vBlacklist";
    public static final String CALLS_TABLE = "interception_calls";
    public static final String CALLS_VIEW = "vCalls";
    public static final String KEYWORDS_TABLE = "tbKeywordsTable";
    public static final String MESSAGES_TABLE = "interception_messages";
    public static final String MESSAGES_VIEW = "vMessages";
    public static final String NUMBERLOCATION_TABLE = "tbNumberLocation";
    public static final String PHONENUMBER_TABLE = "phoneNumberTable";
    public static final String RULES_TABLE = "interception_rules";
    public static final String WHITELIST_TABLE = "tbWhitelist";
    public static final String WHITELIST_VIEW = "vWhitelist";

    public static final class ExtRCSCloumns {
        public static final String GROUP_MESSAGE_NAME = "group_message_name";
        public static final String MESSAGE_ID = "message_id";
        public static final String MESSAGE_TYPE = "message_type";
    }

    public static class TbCommonColumns {
        public static final String ID = "_id";
        public static final String NAME = "name";
        public static final String OPTION = "option";
        public static final String PHONE = "phone";
        public static final String TYPE = "type";
    }

    public static final class TbBlacklist extends TbCommonColumns {
        public static final String INTERCEPTED_CALL_COUNT = "interception_call_count";
        public static final String INTERCEPTED_MSG_COUNT = "interception_msg_count";
        public static final String OPTION = "option";
        public static final String TYPE = "type";
    }

    public static class TbCalls extends TbCommonColumns {
        public static final String BLOCK_REASON = "block_reason";
        public static final String BLOCK_TYPE = "block_type";
        public static final String DATE = "date";
        public static final String MARK_COUNT = "mark_count";
        public static final String SUB_ID = "sub_id";
    }

    public static class TbInterceptionRules implements BaseColumns {
        public static final String KEY = "key";
        public static final String STATUS = "status";
        public static final String VALUE_1 = "value1";
        public static final String VALUE_2 = "value2";
    }

    public static final class TbKeywords {
        public static final String ID = "_id";
        public static final String KEYWORD = "keyword";
    }

    public static final class TbMessages extends TbCommonColumns {
        public static final String BLOCK_REASON = "block_reason";
        public static final String BODY = "body";
        public static final String DATE = "date";
        public static final String EXPDATE = "exp_date";
        public static final String MSGTYPE = "type";
        public static final String PDU = "pdu";
        public static final String SIZE = "size";
        public static final String SUBID = "sub_id";
    }

    public static final class TbNumberLocation {
        public static final String LOCATION = "location";
        public static final String OPERATOR = "operator";
        public static final String PHONE = "phone";
    }

    public static final class TbPermission {
        public static final String PHONE = "packageName";
    }

    public static final class TbRules {
        public static final String NAME = "name";
        public static final String STATE = "state";
    }

    public static final class TbWhitelist extends TbCommonColumns {
    }
}
