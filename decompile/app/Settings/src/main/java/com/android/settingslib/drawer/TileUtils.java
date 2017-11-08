package com.android.settingslib.drawer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.graphics.drawable.VectorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.android.settingslib.R$color;
import com.android.settingslib.R$dimen;
import com.android.settingslib.R$drawable;
import com.android.settingslib.R$string;
import com.android.settingslib.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TileUtils {
    private static final Comparator<DashboardCategory> CATEGORY_COMPARATOR = new Comparator<DashboardCategory>() {
        public int compare(DashboardCategory lhs, DashboardCategory rhs) {
            return rhs.priority - lhs.priority;
        }
    };
    private static final Comparator<Tile> TILE_COMPARATOR = new Comparator<Tile>() {
        public int compare(Tile lhs, Tile rhs) {
            return rhs.priority - lhs.priority;
        }
    };

    public static List<DashboardCategory> getCategories(Context context, HashMap<Pair<String, String>, Tile> cache) {
        long startTime = System.currentTimeMillis();
        boolean setup = Global.getInt(context.getContentResolver(), "device_provisioned", 0) != 0;
        ArrayList<Tile> tiles = new ArrayList();
        for (UserHandle user : UserManager.get(context).getUserProfiles()) {
            if (user.getIdentifier() == ActivityManager.getCurrentUser()) {
                getTilesForAction(context, user, "com.android.settings.action.SETTINGS", cache, null, tiles, true);
                getTilesForAction(context, user, "com.android.settings.OPERATOR_APPLICATION_SETTING", cache, "com.android.settings.category.wireless", tiles, false);
                getTilesForAction(context, user, "com.android.settings.MANUFACTURER_APPLICATION_SETTING", cache, "com.android.settings.category.device", tiles, false);
            }
            if (setup) {
                getTilesForAction(context, user, "com.android.settings.action.EXTRA_SETTINGS", cache, null, tiles, false);
            }
        }
        addSpecialTile("airplane_mode_tile", 99, "com.android.settings.category.wireless", tiles, cache);
        PackageManager pm = context.getPackageManager();
        if (Utils.isChinaTelecomArea() && !Utils.isWifiOnly(context) && Utils.hasPackageInfo(pm, "com.android.phone")) {
            addSpecialTile("telecom4g_mode_tile", 6, "com.android.settings.category.wireless", tiles, cache);
        }
        HashMap<String, DashboardCategory> categoryMap = new HashMap();
        for (Tile tile : tiles) {
            DashboardCategory category = (DashboardCategory) categoryMap.get(tile.category);
            if (category == null) {
                category = createCategory(context, tile.category);
                if (category == null) {
                    Log.w("TileUtils", "Couldn't find category " + tile.category);
                } else {
                    categoryMap.put(category.key, category);
                }
            }
            category.addTile(tile);
        }
        ArrayList<DashboardCategory> categories = new ArrayList(categoryMap.values());
        for (DashboardCategory category2 : categories) {
            Collections.sort(category2.tiles, TILE_COMPARATOR);
        }
        Collections.sort(categories, CATEGORY_COMPARATOR);
        return categories;
    }

    private static DashboardCategory createCategory(Context context, String categoryKey) {
        DashboardCategory category = new DashboardCategory();
        category.key = categoryKey;
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> results = pm.queryIntentActivities(new Intent(categoryKey), 0);
        if (results.size() == 0) {
            return null;
        }
        for (ResolveInfo resolved : results) {
            if (resolved.system) {
                int i;
                category.title = resolved.activityInfo.loadLabel(pm);
                if ("com.android.settings".equals(resolved.activityInfo.applicationInfo.packageName)) {
                    i = resolved.priority;
                } else {
                    i = 0;
                }
                category.priority = i;
            }
        }
        return category;
    }

    private static void getTilesForAction(Context context, UserHandle user, String action, Map<Pair<String, String>, Tile> addedCache, String defaultCategory, ArrayList<Tile> outTiles, boolean requireSettings) {
        Intent intent = new Intent(action);
        if (requireSettings) {
            intent.setPackage("com.android.settings");
        }
        getTilesForIntent(context, user, intent, addedCache, defaultCategory, outTiles, requireSettings, true);
    }

    public static void getTilesForIntent(Context context, UserHandle user, Intent intent, Map<Pair<String, String>, Tile> addedCache, String defaultCategory, List<Tile> outTiles, boolean usePriority, boolean checkCategory) {
        PackageManager pm = context.getPackageManager();
        for (ResolveInfo resolved : pm.queryIntentActivitiesAsUser(intent, 128, user.getIdentifier())) {
            if (resolved.system) {
                ActivityInfo activityInfo = resolved.activityInfo;
                Bundle metaData = activityInfo.metaData;
                String categoryKey = defaultCategory;
                if (!checkCategory || ((metaData != null && metaData.containsKey("com.android.settings.category")) || defaultCategory != null)) {
                    categoryKey = metaData.getString("com.android.settings.category");
                    Pair<String, String> key = new Pair(activityInfo.packageName, activityInfo.name);
                    Tile tile = (Tile) addedCache.get(key);
                    if (tile == null) {
                        tile = new Tile();
                        tile.intent = new Intent().setClassName(activityInfo.packageName, activityInfo.name);
                        tile.category = categoryKey;
                        tile.priority = usePriority ? resolved.priority : 0;
                        if ("com.huawei.android.dsdscardmanager.HWCardManagerActivity".equals(activityInfo.name) || "com.huawei.android.dsdscardmanager.HWCardManagerTabActivity".equals(activityInfo.name)) {
                            tile.priority = 8;
                        }
                        if ("com.huawei.permissionmanager.ui.JumpActvity".equals(activityInfo.name)) {
                            tile.priority = 4;
                        }
                        tile.metaData = activityInfo.metaData;
                        if ("com.huawei.android.hwouc.ui.activities.MainEntranceActivity".equals(activityInfo.name)) {
                            metaData.putString("hw_meta_tile_type", "huawei_update_tile");
                        }
                        updateTileData(context, tile, activityInfo, activityInfo.applicationInfo, pm);
                        addedCache.put(key, tile);
                    }
                    if (!tile.userHandle.contains(user)) {
                        tile.userHandle.add(user);
                    }
                    if (!outTiles.contains(tile)) {
                        outTiles.add(tile);
                    }
                } else {
                    Log.w("TileUtils", "Found " + resolved.activityInfo.name + " for intent " + intent + " missing metadata " + (metaData == null ? "" : "com.android.settings.category"));
                }
            }
        }
    }

    private static boolean updateTileData(Context context, Tile tile, ActivityInfo activityInfo, ApplicationInfo applicationInfo, PackageManager pm) {
        if (!applicationInfo.isSystemApp()) {
            return false;
        }
        int icon = 0;
        CharSequence charSequence = null;
        CharSequence charSequence2 = null;
        Bundle tileMetaData = tile.metaData;
        try {
            Resources res = pm.getResourcesForApplication(applicationInfo.packageName);
            Bundle metaData = activityInfo.metaData;
            if (!(res == null || metaData == null)) {
                if (metaData.containsKey("com.android.settings.icon")) {
                    icon = metaData.getInt("com.android.settings.icon");
                }
                if (metaData.containsKey("com.android.settings.title")) {
                    if (metaData.get("com.android.settings.title") instanceof Integer) {
                        charSequence = res.getString(metaData.getInt("com.android.settings.title"));
                    } else {
                        charSequence = metaData.getString("com.android.settings.title");
                    }
                }
                if (metaData.containsKey("com.android.settings.summary")) {
                    if (metaData.get("com.android.settings.summary") instanceof Integer) {
                        charSequence2 = res.getString(metaData.getInt("com.android.settings.summary"));
                    } else {
                        charSequence2 = metaData.getString("com.android.settings.summary");
                    }
                }
                if (metaData.containsKey("com.android.settings.viewtype")) {
                    String viewType = metaData.getString("com.android.settings.viewtype");
                    if (tileMetaData == null) {
                        tileMetaData = new Bundle();
                    }
                    tileMetaData.putString("hw_meta_tile_type", viewType);
                }
            }
        } catch (NameNotFoundException e) {
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        if (TextUtils.isEmpty(charSequence)) {
            charSequence = activityInfo.loadLabel(pm).toString();
        }
        if (icon == 0) {
            icon = activityInfo.icon;
        }
        tile.icon = Icon.createWithResource(activityInfo.packageName, icon);
        if ("com.google.android.gms".equals(activityInfo.packageName)) {
            handleGmsTile(context, tile);
        } else if ("com.android.settings.Settings$AppCloneActivity".equals(activityInfo.name)) {
            Icon tmpIcon = getAppCloneIcon(context);
            if (tmpIcon != null) {
                tile.icon = tmpIcon;
            }
        }
        tile.title = charSequence;
        tile.summary = charSequence2;
        tile.intent = new Intent().setClassName(activityInfo.packageName, activityInfo.name);
        tile.metaData = tileMetaData;
        return true;
    }

    private static void handleGmsTile(Context context, Tile tile) {
        tile.icon = Icon.createWithResource(context, R$drawable.ic_settings_google);
        tile.category = "com.android.settings.category.accounts";
        tile.priority = -2;
    }

    public static Icon getAppCloneIcon(Context context) {
        if (context == null) {
            return null;
        }
        Drawable drawable = context.getResources().getDrawable(R$drawable.ic_settings_app_clone_global);
        if (!(drawable instanceof VectorDrawable)) {
            return null;
        }
        VectorDrawable vectorDrawable = (VectorDrawable) drawable;
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        int textOffset = context.getResources().getDimensionPixelOffset(R$dimen.app_clone_icon_text_offset);
        int testSize = context.getResources().getDimensionPixelOffset(R$dimen.app_clone_icon_text_size);
        int iconRadius = context.getResources().getDimensionPixelOffset(R$dimen.dashboard_tile_image_size) / 2;
        Paint paint = new Paint();
        paint.setStyle(Style.FILL);
        paint.setColor(context.getColor(R$color.app_clone_icon_text_color));
        paint.setTextSize((float) testSize);
        paint.setTextAlign(Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setAntiAlias(true);
        FontMetrics metrics = paint.getFontMetrics();
        canvas.drawText(context.getString(R$string.app_clone_count, new Object[]{Integer.valueOf(2)}), (float) (iconRadius - textOffset), ((((float) iconRadius) + ((metrics.descent - metrics.ascent) / 2.0f)) - metrics.bottom) + ((float) textOffset), paint);
        return Icon.createWithBitmap(bitmap);
    }

    private static Tile createSpecialTile(String category, String type, int priority) {
        Tile tile = new Tile();
        tile.category = category;
        Bundle metaData = new Bundle();
        metaData.putString("hw_meta_tile_type", type);
        tile.metaData = metaData;
        tile.priority = priority;
        tile.intent = new Intent();
        return tile;
    }

    public static boolean isSpecialTile(Tile tile) {
        String type = getMetaTileType(tile);
        if (type == null) {
            return false;
        }
        return type.equals("airplane_mode_tile") || type.equals("telecom4g_mode_tile");
    }

    public static boolean isAirplaneModeTile(Tile tile) {
        if ("airplane_mode_tile".equals(getMetaTileType(tile))) {
            return true;
        }
        return false;
    }

    public static boolean isTelecom4GModeTile(Tile tile) {
        if ("telecom4g_mode_tile".equals(getMetaTileType(tile))) {
            return true;
        }
        return false;
    }

    public static boolean isNormalSwitchTile(Tile tile) {
        if ("switch_tile".equals(getMetaTileType(tile))) {
            return true;
        }
        return false;
    }

    public static boolean isSplitSwitchTile(Tile tile) {
        if ("split_switch_tile".equals(getMetaTileType(tile))) {
            return true;
        }
        return false;
    }

    public static boolean isHuaweiUpdateTile(Tile tile) {
        if ("huawei_update_tile".equals(getMetaTileType(tile))) {
            return true;
        }
        return false;
    }

    public static String getMetaTileType(Tile tile) {
        if (tile == null) {
            return null;
        }
        Bundle b = tile.metaData;
        if (b == null) {
            return null;
        }
        return b.getString("hw_meta_tile_type");
    }

    private static void addSpecialTile(String key, int priority, String tileCategory, ArrayList<Tile> outTiles, HashMap<Pair<String, String>, Tile> tileCache) {
        Pair<String, String> tileKey = new Pair(key, key);
        Tile tile = (Tile) tileCache.get(tileKey);
        if (tile != null) {
            if (!outTiles.contains(tile)) {
                outTiles.add(tile);
            }
            return;
        }
        if (!key.equals("airplane_mode_tile")) {
            if (key.equals("telecom4g_mode_tile")) {
            }
        }
        tile = createSpecialTile(tileCategory, key, priority);
        tileCache.put(tileKey, tile);
        if (!outTiles.contains(tile)) {
            outTiles.add(tile);
        }
    }

    public static String getHwCloudStateInfo(Context context) {
        int resId = getHwCloudStateInfoResId(context);
        try {
            Resources res = context.getPackageManager().getResourcesForApplication("com.huawei.hidisk");
            if (res == null) {
                return null;
            }
            return res.getString(resId);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    private static int getHwCloudStateInfoResId(Context context) {
        Cursor cursor = context.getContentResolver().query(Uri.parse("content://com.huawei.android.hicloud.SwitchStatusProvider/hicloud"), new String[]{"switch_status"}, null, null, null);
        int resId = 0;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    resId = cursor.getInt(0);
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return resId;
    }
}
