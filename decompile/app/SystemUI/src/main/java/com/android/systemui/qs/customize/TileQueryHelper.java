package com.android.systemui.qs.customize;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.widget.Button;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.DrawableIcon;
import com.android.systemui.qs.QSTile.State;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.utils.UserSwitchUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TileQueryHelper {
    private final Context mContext;
    private TileStateListener mListener;
    private final ArrayList<String> mSpecs = new ArrayList();
    private final ArrayList<TileInfo> mTiles = new ArrayList();

    public interface TileStateListener {
        void onTilesChanged(List<TileInfo> list);
    }

    private class QueryTilesTask extends AsyncTask<Collection<QSTile<?>>, Void, Collection<TileInfo>> {
        private QueryTilesTask() {
        }

        protected Collection<TileInfo> doInBackground(Collection<QSTile<?>>... params) {
            List<TileInfo> tiles = new ArrayList();
            PackageManager pm = TileQueryHelper.this.mContext.getPackageManager();
            for (ResolveInfo info : pm.queryIntentServicesAsUser(new Intent("android.service.quicksettings.action.QS_TILE"), 0, UserSwitchUtils.getCurrentUser())) {
                ComponentName componentName = new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name);
                CharSequence appLabel = info.serviceInfo.applicationInfo.loadLabel(pm);
                String spec = CustomTile.toSpec(componentName);
                State state = getState(params[0], spec);
                if (state != null) {
                    TileQueryHelper.this.addTile(spec, appLabel, state, false);
                } else if (info.serviceInfo.icon != 0 || info.serviceInfo.applicationInfo.icon != 0) {
                    Drawable icon = info.serviceInfo.loadIcon(pm);
                    if ("android.permission.BIND_QUICK_SETTINGS_TILE".equals(info.serviceInfo.permission) && icon != null) {
                        icon.setTint(TileQueryHelper.this.mContext.getColor(R.color.qs_tile_tint_off));
                        CharSequence label = info.serviceInfo.loadLabel(pm);
                        TileQueryHelper.this.addTile(spec, icon, label != null ? label.toString() : "null", appLabel, TileQueryHelper.this.mContext);
                    }
                }
            }
            return tiles;
        }

        private State getState(Collection<QSTile<?>> tiles, String spec) {
            for (QSTile<?> tile : tiles) {
                if (spec.equals(tile.getTileSpec())) {
                    State state = tile.newTileState();
                    tile.getState().copyTo(state);
                    return state;
                }
            }
            return null;
        }

        protected void onPostExecute(Collection<TileInfo> result) {
            TileQueryHelper.this.mTiles.addAll(result);
            TileQueryHelper.this.mListener.onTilesChanged(TileQueryHelper.this.mTiles);
        }
    }

    public static class TileInfo {
        public CharSequence appLabel;
        public boolean isSystem;
        public String spec;
        public State state;
    }

    public TileQueryHelper(Context context, QSTileHost host) {
        this.mContext = context;
        addSystemTiles(host);
    }

    private void addSystemTiles(final QSTileHost host) {
        String possible;
        String custtile = System.getString(this.mContext.getContentResolver(), "cust_tile");
        if (TextUtils.isEmpty(custtile)) {
            possible = this.mContext.getString(R.string.quick_settings_tiles_all);
        } else {
            possible = custtile;
        }
        String[] possibleTiles = possible.split(",");
        Handler qsHandler = new Handler(host.getLooper());
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        for (final String spec : possibleTiles) {
            final QSTile<?> tile = host.createTile(spec);
            if (tile != null && tile.isAvailable()) {
                tile.setListening(this, true);
                tile.clearState();
                tile.refreshState();
                final QSTileHost qSTileHost = host;
                qsHandler.post(new Runnable() {
                    public void run() {
                        final State state = tile.newTileState();
                        tile.getState().copyTo(state);
                        state.label = TileQueryHelper.this.checkWiFiTileLabel(qSTileHost, tile.getTileLabel(), spec);
                        Handler handler = mainHandler;
                        final String str = spec;
                        final QSTile qSTile = tile;
                        handler.post(new Runnable() {
                            public void run() {
                                TileQueryHelper.this.addTile(str, null, state, true);
                                TileQueryHelper.this.mListener.onTilesChanged(TileQueryHelper.this.mTiles);
                                qSTile.setListening(this, false);
                            }
                        });
                    }
                });
            }
        }
        qsHandler.post(new Runnable() {
            public void run() {
                Handler handler = mainHandler;
                final QSTileHost qSTileHost = host;
                handler.post(new Runnable() {
                    public void run() {
                        new QueryTilesTask().execute(new Collection[]{qSTileHost.getAllTiles()});
                    }
                });
            }
        });
    }

    private CharSequence checkWiFiTileLabel(QSTileHost host, CharSequence label, String spec) {
        if (!"wifi".equals(spec)) {
            return label;
        }
        for (QSTile tile : host.getTiles()) {
            if (tile != null && "wifi".equals(tile.getTileSpec())) {
                return tile.getState().label;
            }
        }
        return label;
    }

    public void setListener(TileStateListener listener) {
        this.mListener = listener;
    }

    private void addTile(String spec, CharSequence appLabel, State state, boolean isSystem) {
        if (!this.mSpecs.contains(spec)) {
            TileInfo info = new TileInfo();
            info.state = state;
            State state2 = info.state;
            String name = Button.class.getName();
            info.state.expandedAccessibilityClassName = name;
            state2.minimalAccessibilityClassName = name;
            info.spec = spec;
            info.appLabel = appLabel;
            info.isSystem = isSystem;
            this.mTiles.add(info);
            this.mSpecs.add(spec);
        }
    }

    private void addTile(String spec, Drawable drawable, CharSequence label, CharSequence appLabel, Context context) {
        State state = new State();
        state.label = label;
        state.contentDescription = label;
        state.icon = new DrawableIcon(drawable);
        addTile(spec, appLabel, state, false);
    }
}
