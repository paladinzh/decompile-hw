package com.huawei.systemmanager.comm.database;

public class IDatabaseConst {
    public static final long SQLITE_INVALID_ROW_ID = -1;

    public interface ColLimit {
        public static final String COL_LIMIT_NOT_NULL = "NOT NULL";
        public static final String COL_LIMIT_NULL = "NULL";
        public static final String COL_LIMIT_UNIQUE = "UNIQUE";
        public static final String COL_PRIMARY_LIMIT = "INTEGER PRIMARY KEY AUTOINCREMENT";
        public static final String COL_TEXT_PRIMARY_LIMIT = "PRIMARY KEY";
    }

    public interface ColType {
        public static final String COL_TYPE_INT = "INTEGER";
        public static final String COL_TYPE_TXT = "TEXT";
    }

    public interface SqlMarker {
        public static final String BLANK_SEPARATE = " ";
        public static final String COMMA_SEPARATE = ", ";
        public static final String DOT = ".";
        public static final String LEFT_PARENTHESES = " (";
        public static final String QUOTATION = "\"";
        public static final String RIGHT_PARENTHESES = " )";
        public static final String SQL_END = ";";
    }
}
