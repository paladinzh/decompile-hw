package com.android.settings.datausage;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.format.Formatter;
import android.widget.ProgressBar;
import com.android.internal.util.Preconditions;
import com.android.settingslib.AppItem;
import com.android.settingslib.net.UidDetail;
import com.android.settingslib.net.UidDetailProvider;

public class AppDataUsagePreference extends Preference {
    private final AppItem mItem;
    private final int mPercent;

    private static class UidDetailTask extends AsyncTask<Void, Void, UidDetail> {
        private final AppItem mItem;
        private final UidDetailProvider mProvider;
        private final AppDataUsagePreference mTarget;

        private UidDetailTask(UidDetailProvider provider, AppItem item, AppDataUsagePreference target) {
            this.mProvider = (UidDetailProvider) Preconditions.checkNotNull(provider);
            this.mItem = (AppItem) Preconditions.checkNotNull(item);
            this.mTarget = (AppDataUsagePreference) Preconditions.checkNotNull(target);
        }

        public static void bindView(UidDetailProvider provider, AppItem item, AppDataUsagePreference target) {
            UidDetail cachedDetail = provider.getUidDetail(item.key, false);
            if (cachedDetail != null) {
                bindView(cachedDetail, target);
            } else {
                new UidDetailTask(provider, item, target).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
            }
        }

        private static void bindView(UidDetail detail, Preference target) {
            if (detail != null) {
                target.setIcon(detail.icon);
                target.setTitle(detail.label);
                return;
            }
            target.setIcon(null);
            target.setTitle(null);
        }

        protected void onPreExecute() {
            bindView(null, this.mTarget);
        }

        protected UidDetail doInBackground(Void... params) {
            return this.mProvider.getUidDetail(this.mItem.key, true);
        }

        protected void onPostExecute(UidDetail result) {
            bindView(result, this.mTarget);
        }
    }

    public AppDataUsagePreference(Context context, AppItem item, int percent, UidDetailProvider provider) {
        super(context);
        this.mItem = item;
        this.mPercent = percent;
        setLayoutResource(2130968728);
        setWidgetLayoutResource(2130969257);
        if (!item.restricted || item.total > 0) {
            setSummary(Formatter.formatFileSize(context, item.total));
        } else {
            setSummary(2131626306);
        }
        UidDetailTask.bindView(provider, item, this);
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        ProgressBar progress = (ProgressBar) holder.findViewById(16908301);
        if (!this.mItem.restricted || this.mItem.total > 0) {
            progress.setVisibility(0);
        } else {
            progress.setVisibility(8);
        }
        progress.setProgress(this.mPercent);
    }

    public AppItem getItem() {
        return this.mItem;
    }
}
