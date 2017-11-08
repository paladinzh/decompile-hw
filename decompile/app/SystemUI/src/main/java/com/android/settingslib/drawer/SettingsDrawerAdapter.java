package com.android.settingslib.drawer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settingslib.R$drawable;
import com.android.settingslib.R$id;
import com.android.settingslib.R$layout;
import com.android.settingslib.R$string;
import com.android.settingslib.Utils;
import java.util.ArrayList;
import java.util.List;

public class SettingsDrawerAdapter extends BaseAdapter {
    private final SettingsDrawerActivity mActivity;
    private final ArrayList<Item> mItems = new ArrayList();

    private static class Item {
        public boolean hasDivider;
        public Icon icon;
        public CharSequence label;
        public Tile tile;

        private Item() {
            this.hasDivider = true;
        }
    }

    public SettingsDrawerAdapter(SettingsDrawerActivity activity) {
        this.mActivity = activity;
    }

    void updateCategories() {
        List<DashboardCategory> categories = this.mActivity.getDashboardCategories();
        this.mItems.clear();
        Item tile = new Item();
        tile.label = this.mActivity.getString(R$string.home);
        tile.icon = Icon.createWithResource(this.mActivity, R$drawable.ic_settings_home_screen);
        tile.hasDivider = false;
        this.mItems.add(tile);
        for (int i = 0; i < categories.size(); i++) {
            Item category = new Item();
            category.icon = null;
            DashboardCategory dashboardCategory = (DashboardCategory) categories.get(i);
            category.label = dashboardCategory.title;
            this.mItems.add(category);
            for (int j = 0; j < dashboardCategory.tiles.size(); j++) {
                if (!needIgnoreThisTile((Tile) dashboardCategory.tiles.get(j))) {
                    tile = new Item();
                    Tile dashboardTile = (Tile) dashboardCategory.tiles.get(j);
                    tile.label = dashboardTile.title;
                    tile.icon = dashboardTile.icon;
                    tile.tile = dashboardTile;
                    if (j == dashboardCategory.tiles.size() - 1) {
                        tile.hasDivider = false;
                    }
                    this.mItems.add(tile);
                }
            }
        }
        notifyDataSetChanged();
    }

    public Tile getTile(int position) {
        return this.mItems.get(position) != null ? ((Item) this.mItems.get(position)).tile : null;
    }

    public int getCount() {
        return this.mItems.size();
    }

    public Object getItem(int position) {
        return this.mItems.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public boolean isEnabled(int position) {
        return (this.mItems.get(position) == null || ((Item) this.mItems.get(position)).icon == null) ? false : true;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Item item = (Item) this.mItems.get(position);
        if (item == null) {
            if (convertView == null || convertView.getId() != R$id.spacer) {
                convertView = LayoutInflater.from(this.mActivity).inflate(R$layout.drawer_spacer, parent, false);
            }
            return convertView;
        }
        if (convertView != null && convertView.getId() == R$id.spacer) {
        }
        boolean isTile = item.icon != null;
        int layoutResId = R$layout.drawer_category;
        if (isTile) {
            if (item.hasDivider) {
                layoutResId = R$layout.drawer_item;
            } else {
                layoutResId = R$layout.drawer_item_without_divider;
            }
        }
        convertView = LayoutInflater.from(this.mActivity).inflate(layoutResId, parent, false);
        if (isTile) {
            ((ImageView) convertView.findViewById(16908294)).setImageIcon(item.icon);
        }
        ((TextView) convertView.findViewById(16908310)).setText(item.label);
        return convertView;
    }

    private boolean needIgnoreThisTile(Tile tile) {
        if (TileUtils.isAirplaneModeTile(tile) || TileUtils.isTelecom4GModeTile(tile) || TileUtils.isSplitSwitchTile(tile) || TileUtils.isNormalSwitchTile(tile)) {
            return true;
        }
        Intent intent = tile.intent;
        PackageManager pm = this.mActivity.getPackageManager();
        if (intent == null) {
            return false;
        }
        ComponentName cname = intent.resolveActivity(pm);
        if (cname == null) {
            return false;
        }
        String className = cname.getClassName();
        if (!className.equals("com.huawei.android.dsdscardmanager.HWCardManagerActivity")) {
            return className.equals("com.huawei.android.dsdscardmanager.HWCardManagerTabActivity") && !(Utils.isMultiSimEnabled() && Utils.isChinaTelecomArea() && !Utils.isWifiOnly(this.mActivity) && Utils.isOwnerUser());
        } else {
            if (!Utils.isMultiSimEnabled() || Utils.isChinaTelecomArea() || Utils.isWifiOnly(this.mActivity) || !Utils.isOwnerUser()) {
                return true;
            }
        }
    }
}
