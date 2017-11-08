package com.huawei.systemmanager.applock.datacenter.tbl;

public class AppLockTableViews {

    public interface BackupPrefView {
        public static final String VIEW_NAME = "applockpreference_backup_view";
    }

    public interface LockedAppView {
        public static final String VIEW_NAME = "applockstatus_locked_view";
    }

    public interface UnlockedAppView {
        public static final String VIEW_NAME = "applockstatus_unlocked_view";
    }

    public static String[] getTableViewSentences() {
        return new String[]{"CREATE VIEW IF NOT EXISTS applockstatus_locked_view as SELECT packageName FROM applockstatus WHERE lockStatus=1", "CREATE VIEW IF NOT EXISTS applockstatus_unlocked_view as SELECT packageName FROM applockstatus WHERE lockStatus=0", "CREATE VIEW IF NOT EXISTS applockpreference_backup_view as SELECT prefkey, prefvalue, prefbackup FROM applockpreference WHERE prefbackup=1"};
    }

    public static String[] getDropViewSentences() {
        return new String[]{"DROP VIEW applockstatus_locked_view", "DROP VIEW applockstatus_unlocked_view", "DROP VIEW applockpreference_backup_view"};
    }
}
