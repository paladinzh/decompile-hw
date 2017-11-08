package com.android.settings.applications;

import android.util.ArrayMap;

public final class AppPermissions {

    private static final class Permission {
    }

    private static final class PermissionGroup {
        private final ArrayMap<String, Permission> mPermissions = new ArrayMap();

        private PermissionGroup() {
        }
    }
}
