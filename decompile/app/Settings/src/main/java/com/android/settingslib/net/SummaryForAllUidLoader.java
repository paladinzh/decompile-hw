package com.android.settingslib.net;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.net.INetworkStatsSession;
import android.net.NetworkStats;
import android.net.NetworkTemplate;
import android.os.Bundle;
import android.os.RemoteException;

public class SummaryForAllUidLoader extends AsyncTaskLoader<NetworkStats> {
    private final Bundle mArgs;
    private final INetworkStatsSession mSession;

    public static Bundle buildArgs(NetworkTemplate template, long start, long end) {
        Bundle args = new Bundle();
        args.putParcelable("template", template);
        args.putLong("start", start);
        args.putLong("end", end);
        return args;
    }

    public SummaryForAllUidLoader(Context context, INetworkStatsSession session, Bundle args) {
        super(context);
        this.mSession = session;
        this.mArgs = args;
    }

    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    public NetworkStats loadInBackground() {
        try {
            return this.mSession.getSummaryForAllUid((NetworkTemplate) this.mArgs.getParcelable("template"), this.mArgs.getLong("start"), this.mArgs.getLong("end"), false);
        } catch (RemoteException e) {
            return null;
        }
    }

    protected void onStopLoading() {
        super.onStopLoading();
        cancelLoad();
    }

    protected void onReset() {
        super.onReset();
        cancelLoad();
    }
}
