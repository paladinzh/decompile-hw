package com.huawei.harassmentinterception.db;

import com.huawei.harassmentinterception.common.Tables;
import com.huawei.harassmentinterception.common.Tables.TbBlacklist;
import com.huawei.harassmentinterception.common.Tables.TbCalls;
import com.huawei.harassmentinterception.common.Tables.TbMessages;
import com.huawei.harassmentinterception.common.Tables.TbNumberLocation;
import java.util.Locale;

class SqlStatements {
    static final String SELECTION_RULES_KEY = "key=?";
    static final String SQL_ALTER_CLOUMN_GROUP_MESSAGE_NAME = "ALTER TABLE interception_messages ADD group_message_name Text;";
    static final String SQL_ALTER_CLOUMN_MESSAGE_ID = "ALTER TABLE interception_messages ADD message_id INTEGER DEFAULT -1;";
    static final String SQL_ALTER_CLOUMN_MESSAGE_TYPE = "ALTER TABLE interception_messages ADD message_type INTEGER DEFAULT 0;";
    static final String SQL_CREATE_BLACKLIST_TABLE = "CREATE TABLE IF NOT EXISTS interception_blacklist(_id INTEGER PRIMARY KEY AUTOINCREMENT,phone TEXT,name TEXT,interception_call_count int DEFAULT 0,interception_msg_count int DEFAULT 0,option int DEFAULT 3,type int DEFAULT 0);";
    static final String SQL_CREATE_BLACKLIST_VIEW = String.format(Locale.US, "CREATE VIEW IF NOT EXISTS %1$s AS SELECT A.%2$s ,A.%3$s, A.%4$s, A.%5$s, A.%6$s, A.%7$s, A.%8$s, B.%9$s, B.%10$s FROM %11$s A, %12$s B WHERE A.%13$s=B.%14$s", new Object[]{Tables.BLACKLIST_VIEW, "_id", "name", "phone", TbBlacklist.INTERCEPTED_CALL_COUNT, TbBlacklist.INTERCEPTED_MSG_COUNT, "option", "type", "location", TbNumberLocation.OPERATOR, Tables.BLACKLIST_TABLE, Tables.NUMBERLOCATION_TABLE, "phone", "phone"});
    static final String SQL_CREATE_CALLS_VIEW = String.format(Locale.US, "CREATE VIEW IF NOT EXISTS %1$s AS SELECT A.%2$s ,A.%3$s, A.%4$s, A.%5$s, A.%6$s, A.%7$s, A.%8$s, A.%9$s, B.%10$s ,B.%11$s FROM %12$s A, %13$s B WHERE A.%14$s=B.%15$s ORDER BY A.%16$s", new Object[]{Tables.CALLS_VIEW, "_id", "name", "phone", "date", "block_reason", TbCalls.BLOCK_TYPE, "sub_id", TbCalls.MARK_COUNT, "location", TbNumberLocation.OPERATOR, Tables.CALLS_TABLE, Tables.NUMBERLOCATION_TABLE, "phone", "phone", "date"});
    static final String SQL_CREATE_KEYWORDS_TABLE = "CREATE TABLE IF NOT EXISTS tbKeywordsTable(_id INTEGER PRIMARY KEY AUTOINCREMENT,keyword TEXT);";
    static final String SQL_CREATE_MESSAGES_VIEW = String.format(Locale.US, "CREATE VIEW IF NOT EXISTS %1$s AS SELECT A.%2$s, A.%3$s, A.%4$s, A.%5$s, A.%6$s, A.%7$s, A.%8$s, A.%9$s, A.%10$s,A.%11$s, A.%12$s, B.%13$s, B.%14$s FROM %15$s A, %16$s B WHERE A.%17$s=B.%18$s ORDER BY A.%19$s", new Object[]{Tables.MESSAGES_VIEW, "_id", "name", "phone", "size", "date", TbMessages.EXPDATE, "sub_id", TbMessages.BODY, TbMessages.PDU, "type", "block_reason", "location", TbNumberLocation.OPERATOR, "interception_messages", Tables.NUMBERLOCATION_TABLE, "phone", "phone", "date"});
    static final String SQL_CREATE_NUMBERLOCATION_INDEX = String.format("CREATE UNIQUE INDEX IF NOT EXISTS %1$s_IDX ON %2$s(%3$s)", new Object[]{"phone", Tables.NUMBERLOCATION_TABLE, "phone"});
    static final String SQL_CREATE_NUMBERLOCATION_TABLE = "CREATE TABLE IF NOT EXISTS tbNumberLocation(_id INTEGER PRIMARY KEY AUTOINCREMENT,phone TEXT,location TEXT,operator TEXT);";
    static final String SQL_CREATE_RULES_TABLE = "CREATE TABLE IF NOT EXISTS interception_rules(_id INTEGER PRIMARY KEY AUTOINCREMENT,name Text,state int DEFAULT 0);";
    static final String SQL_CREATE_WHITELIST_TABLE = "CREATE TABLE IF NOT EXISTS tbWhitelist(_id INTEGER PRIMARY KEY AUTOINCREMENT,phone TEXT,name TEXT, option int DEFAULT 3,type int DEFAULT 0);";
    static final String SQL_CREATE_WHITELIST_VIEW = String.format(Locale.US, "CREATE VIEW IF NOT EXISTS %1$s AS SELECT A.%2$s ,A.%3$s, A.%4$s, A.%5$s, A.%6$s, B.%7$s, B.%8$s FROM %9$s A, %10$s B WHERE A.%11$s=B.%12$s", new Object[]{Tables.WHITELIST_VIEW, "_id", "name", "phone", "option", "type", "location", TbNumberLocation.OPERATOR, Tables.WHITELIST_TABLE, Tables.NUMBERLOCATION_TABLE, "phone", "phone"});
    static final String SQL_DROP_BLACKLIST_TABLE = "DROP TABLE IF EXISTS interception_blacklist";
    static final String SQL_DROP_BLACKLIST_VIEW = "DROP VIEW IF EXISTS vBlacklist";
    static final String SQL_DROP_CALLS_TABLE = "DROP TABLE IF EXISTS interception_calls";
    static final String SQL_DROP_CALLS_VIEW = "DROP VIEW IF EXISTS vCalls";
    static final String SQL_DROP_KEYWORDS_TABLE = "DROP TABLE IF EXISTS tbKeywordsTable";
    static final String SQL_DROP_MESSAGES_TABLE = "DROP TABLE IF EXISTS interception_messages";
    static final String SQL_DROP_MESSAGES_VIEW = "DROP VIEW IF EXISTS vMessages";
    static final String SQL_DROP_NUMBERLOCATION_TABLE = "DROP TABLE IF EXISTS tbNumberLocation";
    static final String SQL_DROP_RULES_TABLE = "DROP TABLE IF EXISTS interception_rules";
    static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS ";
    static final String SQL_DROP_WHITELIST_TABLE = "DROP TABLE IF EXISTS tbWhitelist";
    static final String SQL_DROP_WHITELIST_VIEW = "DROP VIEW IF EXISTS vWhitelist";
    static final String SQL_GETCOUNT = "SELECT COUNT(*) FROM ";

    SqlStatements() {
    }
}
