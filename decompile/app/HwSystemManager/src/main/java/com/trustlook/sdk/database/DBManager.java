package com.trustlook.sdk.database;

import android.content.Context;

public class DBManager {
    private static DBManager a;
    private AppInfoDataSource b;

    public AppInfoDataSource getAppInfoDataSource() {
        return this.b;
    }

    public static DBManager getInstance(Context context) {
        if (a == null) {
            a = new DBManager(context);
        }
        return a;
    }

    private DBManager(Context context) {
        if (this.b == null) {
            this.b = new AppInfoDataSource(context);
        }
        this.b.open(context);
    }
}
