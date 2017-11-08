package com.android.settings.qstile;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.SystemProperties;
import android.service.quicksettings.TileService;
import com.android.settings.DevelopmentSettings.SystemPropPoker;

public class DevelopmentTiles {
    static final Class[] TILE_CLASSES = new Class[]{ShowLayout.class, GPUProfiling.class};

    public static class GPUProfiling extends TileService {
        public void onStartListening() {
            super.onStartListening();
            refresh();
        }

        public void refresh() {
            String value = SystemProperties.get("debug.hwui.profile");
            if (getQsTile() != null) {
                getQsTile().setState(value.equals("visual_bars") ? 2 : 1);
                getQsTile().updateTile();
            }
        }

        public void onClick() {
            if (getQsTile() != null) {
                SystemProperties.set("debug.hwui.profile", getQsTile().getState() == 1 ? "visual_bars" : "");
            }
            new SystemPropPoker().execute(new Void[0]);
            refresh();
        }
    }

    public static class ShowLayout extends TileService {
        public void onStartListening() {
            super.onStartListening();
            refresh();
        }

        public void refresh() {
            boolean enabled = SystemProperties.getBoolean("debug.layout", false);
            if (getQsTile() != null) {
                getQsTile().setState(enabled ? 2 : 1);
                getQsTile().updateTile();
            }
        }

        public void onClick() {
            if (getQsTile() != null) {
                SystemProperties.set("debug.layout", getQsTile().getState() == 1 ? "true" : "false");
            }
            new SystemPropPoker().execute(new Void[0]);
            refresh();
        }
    }

    public static void setTilesEnabled(Context context, boolean enable) {
        PackageManager pm = context.getPackageManager();
        for (Class cls : TILE_CLASSES) {
            int i;
            ComponentName componentName = new ComponentName(context, cls);
            if (enable) {
                i = 1;
            } else {
                i = 0;
            }
            pm.setComponentEnabledSetting(componentName, i, 1);
        }
    }
}
