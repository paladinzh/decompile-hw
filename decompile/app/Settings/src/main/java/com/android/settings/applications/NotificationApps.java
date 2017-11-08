package com.android.settings.applications;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.notification.NotificationBackend;

public class NotificationApps extends ManageApplications {
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mLoader;
        private final NotificationBackend mNotificationBackend;

        private SummaryProvider(Context context, SummaryLoader loader) {
            this.mContext = context;
            this.mLoader = loader;
            this.mNotificationBackend = new NotificationBackend();
        }

        public void setListening(boolean listening) {
            if (listening) {
                new AppCounter(this.mContext) {
                    protected void onCountComplete(int num) {
                        SummaryProvider.this.updateSummary(num);
                    }

                    protected boolean includeInCount(ApplicationInfo info) {
                        return SummaryProvider.this.mNotificationBackend.getNotificationsBanned(info.packageName, info.uid);
                    }
                }.execute(new Void[0]);
            }
        }

        private void updateSummary(int count) {
            if (count == 0) {
                this.mLoader.setSummary(this, this.mContext.getString(2131627093));
                return;
            }
            this.mLoader.setSummary(this, this.mContext.getResources().getQuantityString(2131689503, count, new Object[]{Integer.valueOf(count)}));
        }
    }
}
