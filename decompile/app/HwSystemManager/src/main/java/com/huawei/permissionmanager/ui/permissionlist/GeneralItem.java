package com.huawei.permissionmanager.ui.permissionlist;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.addviewmonitor.AddViewMonitorActivity;
import com.huawei.systemmanager.comm.component.ListItem;
import com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity;

public abstract class GeneralItem implements ListItem, ISearchKey {
    public static final String KEY_ADDVIEW = "addviewItem";
    public static final String KEY_AUTOSTARTUP = "AutoStartup";

    public static class AddViewItem extends GeneralItem {
        private int mCount;

        public AddViewItem(int count) {
            this.mCount = count;
        }

        public String getTitle(Context ctx) {
            return ctx.getString(R.string.DropzoneAppTitle);
        }

        public Intent getIntent(Context ctx) {
            return new Intent(ctx, AddViewMonitorActivity.class);
        }

        public String getKey() {
            return GeneralItem.KEY_ADDVIEW;
        }

        public String getDescription(Context ctx) {
            return ctx.getResources().getQuantityString(R.plurals.app_cnt_suffix, this.mCount, new Object[]{Integer.valueOf(this.mCount)});
        }
    }

    public static class AutoStartupItem extends GeneralItem {
        public String getTitle(Context ctx) {
            return ctx.getString(R.string.systemmanager_module_title_autolaunch);
        }

        public Intent getIntent(Context ctx) {
            return new Intent(ctx, StartupNormalAppListActivity.class);
        }

        public String getKey() {
            return GeneralItem.KEY_AUTOSTARTUP;
        }
    }

    public abstract Intent getIntent(Context context);
}
