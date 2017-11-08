package com.android.settings.dashboard;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import com.android.settings.notification.ZenModeAutomationSettings;
import com.android.settingslib.drawer.DashboardCategory;
import com.android.settingslib.drawer.Tile;
import java.util.ArrayList;
import java.util.List;

public class SummaryLoader {
    private final Activity mActivity;
    private DashboardAdapter mAdapter;
    private final Handler mHandler = new Handler();
    private boolean mListening;
    private ArraySet<BroadcastReceiver> mReceivers = new ArraySet();
    private final ArrayMap<SummaryProvider, ComponentName> mSummaryMap = new ArrayMap();
    private final List<Tile> mTiles = new ArrayList();
    private final Worker mWorker;
    private boolean mWorkerListening;
    private final HandlerThread mWorkerThread = new HandlerThread("SummaryLoader", 10);
    private SummaryProvider mZenModeProvider = null;

    public interface SummaryProviderFactory {
        SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader);
    }

    public interface SummaryProvider {
        void setListening(boolean z);
    }

    private class Worker extends Handler {
        public Worker(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    SummaryLoader.this.makeProviderW(msg.obj);
                    return;
                case 2:
                    SummaryLoader.this.setListeningW(msg.arg1 != 0);
                    return;
                default:
                    return;
            }
        }
    }

    public SummaryLoader(Activity activity, List<DashboardCategory> categories) {
        this.mWorkerThread.start();
        this.mWorker = new Worker(this.mWorkerThread.getLooper());
        this.mActivity = activity;
        for (int i = 0; i < categories.size(); i++) {
            List<Tile> tiles = ((DashboardCategory) categories.get(i)).tiles;
            for (int j = 0; j < tiles.size(); j++) {
                this.mWorker.obtainMessage(1, (Tile) tiles.get(j)).sendToTarget();
            }
        }
    }

    public void release() {
        this.mWorkerThread.quitSafely();
        setListeningW(false);
    }

    public void setAdapter(DashboardAdapter adapter) {
        this.mAdapter = adapter;
    }

    public void setSummary(SummaryProvider provider, final CharSequence summary) {
        final ComponentName component = (ComponentName) this.mSummaryMap.get(provider);
        this.mHandler.post(new Runnable() {
            public void run() {
                Tile tile = SummaryLoader.this.mAdapter.getTile(component);
                if (tile != null) {
                    tile.summary = summary;
                    SummaryLoader.this.mAdapter.notifyChanged(tile);
                }
            }
        });
    }

    public void setListening(boolean listening) {
        if (this.mListening != listening) {
            this.mListening = listening;
            this.mWorker.removeMessages(2);
            unregisterReceivers();
            this.mWorker.obtainMessage(2, listening ? 1 : 0, 0).sendToTarget();
        }
    }

    public void unregisterReceivers() {
        for (int i = 0; i < this.mReceivers.size(); i++) {
            this.mActivity.unregisterReceiver((BroadcastReceiver) this.mReceivers.valueAt(i));
        }
        this.mReceivers.clear();
    }

    SummaryProvider getZenModeProvider() {
        return this.mZenModeProvider;
    }

    private SummaryProvider getSummaryProvider(Tile tile) {
        if (tile.intent.getComponent() == null || !this.mActivity.getPackageName().equals(tile.intent.getComponent().getPackageName())) {
            return null;
        }
        Bundle metaData = getMetaData(tile);
        if (metaData == null) {
            return null;
        }
        String clsName = metaData.getString("com.android.settings.FRAGMENT_CLASS");
        if (clsName == null) {
            if (!"com.android.settings.LauncherModeSettingsActivity".equals(tile.intent.getComponent().getClassName())) {
                return null;
            }
            clsName = "com.android.settings.LauncherModeSettingsActivity";
        }
        try {
            SummaryProvider p = ((SummaryProviderFactory) Class.forName(clsName).getField("SUMMARY_PROVIDER_FACTORY").get(null)).createSummaryProvider(this.mActivity, this);
            if (ZenModeAutomationSettings.class.getName().equals(clsName)) {
                this.mZenModeProvider = p;
            }
            return p;
        } catch (ClassNotFoundException e) {
            return null;
        } catch (NoSuchFieldException e2) {
            return null;
        } catch (ClassCastException e3) {
            return null;
        } catch (IllegalAccessException e4) {
            return null;
        }
    }

    private Bundle getMetaData(Tile tile) {
        return tile.metaData;
    }

    public void registerReceiver(final BroadcastReceiver receiver, final IntentFilter filter) {
        this.mActivity.runOnUiThread(new Runnable() {
            public void run() {
                if (SummaryLoader.this.mListening) {
                    SummaryLoader.this.mReceivers.add(receiver);
                    SummaryLoader.this.mActivity.registerReceiver(receiver, filter);
                }
            }
        });
    }

    private synchronized void setListeningW(boolean listening) {
        if (this.mWorkerListening != listening) {
            this.mWorkerListening = listening;
            for (SummaryProvider p : this.mSummaryMap.keySet()) {
                try {
                    p.setListening(listening);
                } catch (Exception e) {
                    Log.d("SummaryLoader", "Problem in setListening", e);
                }
            }
        }
    }

    private synchronized void makeProviderW(Tile tile) {
        SummaryProvider provider = getSummaryProvider(tile);
        if (provider != null) {
            this.mSummaryMap.put(provider, tile.intent.getComponent());
        }
    }
}
