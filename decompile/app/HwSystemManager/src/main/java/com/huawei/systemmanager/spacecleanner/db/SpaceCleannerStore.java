package com.huawei.systemmanager.spacecleanner.db;

import android.net.Uri;

public class SpaceCleannerStore {
    public static final String AUTHORITY = "com.huawei.systemmanager.SpaceCleannerProvider";
    public static final String CONTENT_PREFIX = "content://com.huawei.systemmanager.SpaceCleannerProvider/";
    public static final int PROTECT_COLUMNS_ID = 0;
    public static final int PROTECT_COLUMNS_PATH = 2;
    public static final int PROTECT_COLUMNS_PKG = 1;
    public static final int TABLE_INDEX_HW_TRASH_INFO = 3;
    public static final int TABLE_INDEX_HW_TRASH_INFO_ID = 4;
    public static final int TABLE_INDEX_PROTECT_INFO = 1;
    public static final int TABLE_INDEX_PROTECT_INFO_ID = 2;
    public static final int TABLE_INDEX_RARELY_USED_APP = 5;
    public static final int TABLE_INDEX_RARELY_USED_APP_ID = 6;
    public static final String TABLE_NAME_HW_TRASH_INFO = "hwtrashinfo";
    public static final String TABLE_NAME_PROTECT_INFO = "protectfolder";
    public static final String TABLE_NAME_RARELY_USED_APP = "rarelyusedapp";
    public static final int TRASH_COLUMNS_ID = 0;
    public static final int TRASH_COLUMNS_PKG_NAME = 1;
    public static final int TRASH_COLUMNS_TRASH_KEEP_LATEST = 7;
    public static final int TRASH_COLUMNS_TRASH_KEEP_TIME = 6;
    public static final int TRASH_COLUMNS_TRASH_PATH = 2;
    public static final int TRASH_COLUMNS_TRASH_RECOMMENDED = 4;
    public static final int TRASH_COLUMNS_TRASH_RULE = 5;
    public static final int TRASH_COLUMNS_TRASH_TYPE = 3;
    public static final int TRASH_RECOMMENDED_NO = 2;
    public static final int TRASH_RECOMMENDED_YES = 1;

    public static final class ProtectTable {

        public interface Columns {
            public static final String PKG_NAME = "pkgname";
            public static final String PROTECT_PATH = "package_path";
            public static final String _ID = "id";
        }

        public static Uri getContentUri() {
            return Uri.parse("content://com.huawei.systemmanager.SpaceCleannerProvider/protectfolder");
        }
    }

    public static final class TrashInfoTable {

        public interface Columns {
            public static final String PKG_NAME = "pkgname";
            public static final String TRASH_KEEP_LATEST = "trash_keep_latest";
            public static final String TRASH_KEEP_TIME = "trash_keep_time";
            public static final String TRASH_PATH = "trash_path";
            public static final String TRASH_RECOMMENDED = "trash_recommended";
            public static final String TRASH_RULE = "trash_rule";
            public static final String TRASH_TYPE = "trash_type";
            public static final String _ID = "id";
        }

        public static Uri getContentUri() {
            return Uri.parse("content://com.huawei.systemmanager.SpaceCleannerProvider/hwtrashinfo");
        }
    }

    public static String[] getProtectPathColumns() {
        return new String[]{"id", "pkgname", Columns.PROTECT_PATH};
    }

    public static String[] getTrashInfoColumns() {
        return new String[]{"id", "pkgname", Columns.TRASH_PATH, Columns.TRASH_TYPE, Columns.TRASH_RECOMMENDED, Columns.TRASH_RULE, Columns.TRASH_KEEP_TIME, Columns.TRASH_KEEP_LATEST};
    }
}
