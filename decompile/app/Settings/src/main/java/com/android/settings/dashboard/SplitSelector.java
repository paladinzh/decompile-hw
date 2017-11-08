package com.android.settings.dashboard;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import com.android.settings.HwCustSplitUtils;
import com.android.settings.Utils;
import com.android.settingslib.drawer.SplitUtils;
import com.android.settingslib.drawer.Tile;
import com.huawei.cust.HwCustUtils;

public class SplitSelector {
    public static final int SELECTOR_COLOR = Color.parseColor("#1A007DFF");
    private static final ComponentName defaultMarked = new ComponentName("com.android.settings", "com.android.settings.Settings$WifiSettingsActivity");
    private Tile currentMarkedTile = null;
    private boolean isMarkView = true;
    private boolean isSplitMode = false;
    private Context mContext;
    private DashboardAdapter mDashboardAdapter = null;
    private HwCustSplitUtils mHwCustSplitUtils = null;
    private View markedView = null;

    public SplitSelector(DashboardAdapter dashboardAdapter, Context context) {
        this.mDashboardAdapter = dashboardAdapter;
        this.mContext = context;
        this.mHwCustSplitUtils = (HwCustSplitUtils) HwCustUtils.createObj(HwCustSplitUtils.class, new Object[]{(Activity) this.mContext});
        this.isSplitMode = this.mHwCustSplitUtils.reachSplitSize();
    }

    public void markClick(View v) {
        if (v != null && !isNotSurpportLand(((Tile) v.getTag()).intent)) {
            this.isMarkView = true;
            setSelectorColor(0);
            setmarkedView(v);
            setCurrentMarkedTile((Tile) v.getTag(), false);
            if (isSplitMode()) {
                setSelectorColor(SELECTOR_COLOR);
            }
        }
    }

    public void checkMarkedView(View view) {
        if (view != null) {
            if (this.currentMarkedTile == null) {
                setCurrentMarkedTile(this.mDashboardAdapter.getTile(defaultMarked), false);
            }
            if (isSplitMode()) {
                if (this.currentMarkedTile != null && this.isMarkView && !isNotSurpportLand(((Tile) view.getTag()).intent) && isTileSameByIntent(this.currentMarkedTile, (Tile) view.getTag())) {
                    setSelectorColor(0);
                    setmarkedView(view);
                    setSelectorColor(SELECTOR_COLOR);
                }
                return;
            }
            setSelectorColor(0);
        }
    }

    private boolean isTileSameByIntent(Tile t1, Tile t2) {
        if (t1 == null || t2 == null) {
            return false;
        }
        if (t1.intent.getComponent() != null && t2.intent.getComponent() != null) {
            return t1.intent.getComponent().toShortString().trim().equals(t2.intent.getComponent().toShortString().trim());
        }
        if (t1.intent.getAction() == null || t2.intent.getAction() == null) {
            return false;
        }
        return t1.intent.getAction().trim().equals(t2.intent.getAction().trim());
    }

    public void updatetCurrentTileByIntent(Intent intent) {
        if (intent != null) {
            Tile tile = this.mDashboardAdapter.getTile(intent.getComponent());
            if (tile != null) {
                setCurrentMarkedTile(tile, true);
            } else {
                this.isMarkView = intent.getBooleanExtra("isMarkViewEx", true);
                if (!this.isMarkView) {
                    setSelectorColor(0);
                }
            }
        }
    }

    private boolean isNotSurpportLand(Intent intent) {
        return SplitUtils.notSupportSplit(intent);
    }

    private void setCurrentMarkedTile(Tile tile, boolean isNotifyChanged) {
        if (!(tile == null || isTileSameByIntent(this.currentMarkedTile, tile) || isNotSurpportLand(tile.intent))) {
            this.currentMarkedTile = tile;
        }
        if (isNotifyChanged) {
            this.mDashboardAdapter.notifyChanged(this.currentMarkedTile);
        }
    }

    private void setmarkedView(View view) {
        if (view != null) {
            this.markedView = view;
        }
    }

    private void setSelectorColor(int color) {
        if (this.markedView != null) {
            if (!(color == 0 && Utils.getViewBackgroundColor(this.markedView) == SELECTOR_COLOR)) {
                if (color == SELECTOR_COLOR) {
                }
            }
            this.markedView.setBackgroundColor(color);
        }
    }

    private boolean isSplitMode() {
        return this.isSplitMode;
    }

    public void updateSplitMode() {
        boolean isSplitModeNow = this.mHwCustSplitUtils.reachSplitSize();
        if (isSplitModeNow != this.isSplitMode) {
            this.isSplitMode = isSplitModeNow;
            splitModeChange(isSplitModeNow);
        }
    }

    private void splitModeChange(boolean isSplitMode) {
        Tile tile = null;
        if (this.mHwCustSplitUtils.getCurrentSubIntent() != null) {
            tile = this.mDashboardAdapter.getTile(this.mHwCustSplitUtils.getCurrentSubIntent().getComponent());
        }
        setCurrentMarkedTile(tile, true);
    }
}
