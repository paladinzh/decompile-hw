package com.android.settings.notification;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.INotificationManager;
import android.app.INotificationManager.Stub;
import android.app.Notification;
import android.app.Notification.Action;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.NotificationListenerService.Ranking;
import android.service.notification.NotificationListenerService.RankingMap;
import android.service.notification.StatusBarNotification;
import android.support.v7.appcompat.R$id;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DateTimeView;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.CopyablePreference;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class NotificationStation extends SettingsPreferenceFragment {
    private static final String TAG = NotificationStation.class.getSimpleName();
    private Context mContext;
    private Handler mHandler;
    private final NotificationListenerService mListener = new NotificationListenerService() {
        public void onNotificationPosted(StatusBarNotification sbn, RankingMap ranking) {
            int i = 0;
            String str = "onNotificationPosted: %s, with update for %d";
            Object[] objArr = new Object[2];
            objArr[0] = sbn.getNotification();
            if (ranking != null) {
                i = ranking.getOrderedKeys().length;
            }
            objArr[1] = Integer.valueOf(i);
            NotificationStation.logd(str, objArr);
            NotificationStation.this.mRanking = ranking;
            NotificationStation.this.scheduleRefreshList();
        }

        public void onNotificationRemoved(StatusBarNotification notification, RankingMap ranking) {
            String str = "onNotificationRankingUpdate with update for %d";
            Object[] objArr = new Object[1];
            objArr[0] = Integer.valueOf(ranking == null ? 0 : ranking.getOrderedKeys().length);
            NotificationStation.logd(str, objArr);
            NotificationStation.this.mRanking = ranking;
            NotificationStation.this.scheduleRefreshList();
        }

        public void onNotificationRankingUpdate(RankingMap ranking) {
            String str = "onNotificationRankingUpdate with update for %d";
            Object[] objArr = new Object[1];
            objArr[0] = Integer.valueOf(ranking == null ? 0 : ranking.getOrderedKeys().length);
            NotificationStation.logd(str, objArr);
            NotificationStation.this.mRanking = ranking;
            NotificationStation.this.scheduleRefreshList();
        }

        public void onListenerConnected() {
            NotificationStation.this.mRanking = getCurrentRanking();
            String str = "onListenerConnected with update for %d";
            Object[] objArr = new Object[1];
            objArr[0] = Integer.valueOf(NotificationStation.this.mRanking == null ? 0 : NotificationStation.this.mRanking.getOrderedKeys().length);
            NotificationStation.logd(str, objArr);
            NotificationStation.this.scheduleRefreshList();
        }
    };
    private INotificationManager mNoMan;
    private final Comparator<HistoricalNotificationInfo> mNotificationSorter = new Comparator<HistoricalNotificationInfo>() {
        public int compare(HistoricalNotificationInfo lhs, HistoricalNotificationInfo rhs) {
            return (int) (rhs.timestamp - lhs.timestamp);
        }
    };
    private PackageManager mPm;
    private RankingMap mRanking;
    private Runnable mRefreshListRunnable = new Runnable() {
        public void run() {
            NotificationStation.this.refreshList();
        }
    };

    private static class HistoricalNotificationInfo {
        public boolean active;
        public CharSequence extra;
        public Drawable icon;
        public String pkg;
        public Drawable pkgicon;
        public CharSequence pkgname;
        public int priority;
        public long timestamp;
        public CharSequence title;
        public int user;

        private HistoricalNotificationInfo() {
        }
    }

    private static class HistoricalNotificationPreference extends CopyablePreference {
        private final HistoricalNotificationInfo mInfo;

        public HistoricalNotificationPreference(Context context, HistoricalNotificationInfo info) {
            super(context);
            setLayoutResource(2130968884);
            this.mInfo = info;
        }

        public void onBindViewHolder(PreferenceViewHolder row) {
            float f;
            super.onBindViewHolder(row);
            if (this.mInfo.icon != null) {
                ((ImageView) row.findViewById(2131886147)).setImageDrawable(this.mInfo.icon);
            }
            if (this.mInfo.pkgicon != null) {
                ((ImageView) row.findViewById(2131886829)).setImageDrawable(this.mInfo.pkgicon);
            }
            ((DateTimeView) row.findViewById(2131886830)).setTime(this.mInfo.timestamp);
            ((TextView) row.findViewById(R$id.title)).setText(this.mInfo.title);
            ((TextView) row.findViewById(2131886831)).setText(this.mInfo.pkgname);
            final TextView extra = (TextView) row.findViewById(2131886832);
            extra.setText(this.mInfo.extra);
            extra.setVisibility(8);
            row.itemView.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    int i = 0;
                    TextView textView = extra;
                    if (extra.getVisibility() == 0) {
                        i = 8;
                    }
                    textView.setVisibility(i);
                }
            });
            View view = row.itemView;
            if (this.mInfo.active) {
                f = 1.0f;
            } else {
                f = 0.5f;
            }
            view.setAlpha(f);
        }

        public CharSequence getCopyableText() {
            return new SpannableStringBuilder(this.mInfo.title).append(" [").append(new Date(this.mInfo.timestamp).toString()).append("]\n").append(this.mInfo.pkgname).append("\n").append(this.mInfo.extra);
        }

        public void performClick() {
        }
    }

    private void scheduleRefreshList() {
        if (this.mHandler != null) {
            this.mHandler.removeCallbacks(this.mRefreshListRunnable);
            this.mHandler.postDelayed(this.mRefreshListRunnable, 100);
        }
    }

    public void onAttach(Activity activity) {
        logd("onAttach(%s)", activity.getClass().getSimpleName());
        super.onAttach(activity);
        this.mHandler = new Handler(activity.getMainLooper());
        this.mContext = activity;
        this.mPm = this.mContext.getPackageManager();
        this.mNoMan = Stub.asInterface(ServiceManager.getService("notification"));
    }

    public void onDetach() {
        logd("onDetach()", new Object[0]);
        this.mHandler.removeCallbacks(this.mRefreshListRunnable);
        this.mHandler = null;
        super.onDetach();
    }

    public void onPause() {
        try {
            this.mListener.unregisterAsSystemService();
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot unregister listener", e);
        }
        super.onPause();
    }

    protected int getMetricsCategory() {
        return 75;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        logd("onActivityCreated(%s)", savedInstanceState);
        super.onActivityCreated(savedInstanceState);
        Utils.forceCustomPadding(getListView(), false);
    }

    public void onResume() {
        logd("onResume()", new Object[0]);
        super.onResume();
        try {
            this.mListener.registerAsSystemService(this.mContext, new ComponentName(this.mContext.getPackageName(), getClass().getCanonicalName()), ActivityManager.getCurrentUser());
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot register listener", e);
        }
        refreshList();
    }

    private void refreshList() {
        List<HistoricalNotificationInfo> infos = loadNotifications();
        if (infos != null) {
            logd("adding %d infos", Integer.valueOf(infos.size()));
            Collections.sort(infos, this.mNotificationSorter);
            if (getPreferenceScreen() == null) {
                setPreferenceScreen(getPreferenceManager().createPreferenceScreen(getContext()));
            }
            getPreferenceScreen().removeAll();
            for (int i = 0; i < N; i++) {
                getPreferenceScreen().addPreference(new HistoricalNotificationPreference(getPrefContext(), (HistoricalNotificationInfo) infos.get(i)));
            }
        }
    }

    private static void logd(String msg, Object... args) {
    }

    private static CharSequence bold(CharSequence cs) {
        if (cs.length() == 0) {
            return cs;
        }
        SpannableString ss = new SpannableString(cs);
        ss.setSpan(new StyleSpan(1), 0, cs.length(), 0);
        return ss;
    }

    private static String getTitleString(Notification n) {
        Object obj = null;
        if (n.extras != null) {
            obj = n.extras.getCharSequence("android.title");
            if (TextUtils.isEmpty(obj)) {
                obj = n.extras.getCharSequence("android.text");
            }
        }
        if (TextUtils.isEmpty(obj) && !TextUtils.isEmpty(n.tickerText)) {
            obj = n.tickerText;
        }
        return String.valueOf(obj);
    }

    private static String formatPendingIntent(PendingIntent pi) {
        StringBuilder sb = new StringBuilder();
        IntentSender is = pi.getIntentSender();
        sb.append("Intent(pkg=").append(is.getCreatorPackage());
        try {
            if (ActivityManagerNative.getDefault().isIntentSenderAnActivity(is.getTarget())) {
                sb.append(" (activity)");
            }
        } catch (RemoteException e) {
        }
        sb.append(")");
        return sb.toString();
    }

    private List<HistoricalNotificationInfo> loadNotifications() {
        int currentUserId = ActivityManager.getCurrentUser();
        try {
            StatusBarNotification[] active = this.mNoMan.getActiveNotifications(this.mContext.getPackageName());
            List<HistoricalNotificationInfo> list = new ArrayList(active.length + this.mNoMan.getHistoricalNotifications(this.mContext.getPackageName(), 50).length);
            Ranking rank = new Ranking();
            for (StatusBarNotification[] resultset : new StatusBarNotification[][]{active, dismissed}) {
                for (StatusBarNotification sbn : resultset) {
                    if (((sbn.getUserId() != currentUserId ? 1 : 0) & (sbn.getUserId() != -1 ? 1 : 0)) == 0) {
                        Notification n = sbn.getNotification();
                        HistoricalNotificationInfo info = new HistoricalNotificationInfo();
                        info.pkg = sbn.getPackageName();
                        info.user = sbn.getUserId();
                        info.icon = loadIconDrawable(info.pkg, info.user, n.icon);
                        info.pkgicon = loadPackageIconDrawable(info.pkg, info.user);
                        info.pkgname = loadPackageName(info.pkg);
                        info.title = getTitleString(n);
                        if (TextUtils.isEmpty(info.title)) {
                            info.title = getString(2131627182);
                        }
                        info.timestamp = sbn.getPostTime();
                        info.priority = n.priority;
                        info.active = resultset == active;
                        SpannableStringBuilder sb = new SpannableStringBuilder();
                        String delim = getString(2131627183);
                        sb.append(bold(getString(2131627184))).append(delim).append(info.pkg).append("\n").append(bold(getString(2131627185))).append(delim).append(sbn.getKey());
                        sb.append("\n").append(bold(getString(2131627201))).append(delim).append(n.getSmallIcon().toString());
                        if (sbn.isGroup()) {
                            sb.append("\n").append(bold(getString(2131627186))).append(delim).append(sbn.getGroupKey());
                            if (n.isGroupSummary()) {
                                sb.append(bold(getString(2131627187)));
                            }
                        }
                        sb.append("\n").append(bold(getString(2131627204))).append(delim);
                        if ((n.defaults & 1) != 0) {
                            sb.append(getString(2131627206));
                        } else if (n.sound != null) {
                            sb.append(n.sound.toString());
                        } else {
                            sb.append(getString(2131627207));
                        }
                        sb.append("\n").append(bold(getString(2131627205))).append(delim);
                        if ((n.defaults & 2) != 0) {
                            sb.append(getString(2131627206));
                        } else if (n.vibrate != null) {
                            for (int vi = 0; vi < n.vibrate.length; vi++) {
                                if (vi > 0) {
                                    sb.append(',');
                                }
                                sb.append(String.valueOf(n.vibrate[vi]));
                            }
                        } else {
                            sb.append(getString(2131627207));
                        }
                        sb.append("\n").append(bold(getString(2131627188))).append(delim).append(Notification.visibilityToString(n.visibility));
                        if (n.publicVersion != null) {
                            sb.append("\n").append(bold(getString(2131627189))).append(delim).append(getTitleString(n.publicVersion));
                        }
                        sb.append("\n").append(bold(getString(2131627190))).append(delim).append(Notification.priorityToString(n.priority));
                        if (resultset == active) {
                            if (this.mRanking != null) {
                                if (this.mRanking.getRanking(sbn.getKey(), rank)) {
                                    sb.append("\n").append(bold(getString(2131627191))).append(delim).append(Ranking.importanceToString(rank.getImportance()));
                                    if (rank.getImportanceExplanation() != null) {
                                        sb.append("\n").append(bold(getString(2131627192))).append(delim).append(rank.getImportanceExplanation());
                                    }
                                }
                            }
                            if (this.mRanking == null) {
                                sb.append("\n").append(bold(getString(2131627208)));
                            } else {
                                sb.append("\n").append(bold(getString(2131627209)));
                            }
                        }
                        if (n.contentIntent != null) {
                            sb.append("\n").append(bold(getString(2131627193))).append(delim).append(formatPendingIntent(n.contentIntent));
                        }
                        if (n.deleteIntent != null) {
                            sb.append("\n").append(bold(getString(2131627194))).append(delim).append(formatPendingIntent(n.deleteIntent));
                        }
                        if (n.fullScreenIntent != null) {
                            sb.append("\n").append(bold(getString(2131627195))).append(delim).append(formatPendingIntent(n.fullScreenIntent));
                        }
                        if (n.actions != null && n.actions.length > 0) {
                            sb.append("\n").append(bold(getString(2131627196)));
                            for (int ai = 0; ai < n.actions.length; ai++) {
                                Action action = n.actions[ai];
                                sb.append("\n  ").append(String.valueOf(ai)).append(' ').append(bold(getString(2131627197))).append(delim).append(action.title);
                                if (action.actionIntent != null) {
                                    sb.append("\n    ").append(bold(getString(2131627193))).append(delim).append(formatPendingIntent(action.actionIntent));
                                }
                                if (action.getRemoteInputs() != null) {
                                    sb.append("\n    ").append(bold(getString(2131627198))).append(delim).append(String.valueOf(action.getRemoteInputs().length));
                                }
                            }
                        }
                        if (n.contentView != null) {
                            sb.append("\n").append(bold(getString(2131627199))).append(delim).append(n.contentView.toString());
                        }
                        if (n.extras != null && n.extras.size() > 0) {
                            sb.append("\n").append(bold(getString(2131627200)));
                            for (String extraKey : n.extras.keySet()) {
                                String val = String.valueOf(n.extras.get(extraKey));
                                if (val.length() > 100) {
                                    val = val.substring(0, 100) + "...";
                                }
                                sb.append("\n  ").append(extraKey).append(delim).append(val);
                            }
                        }
                        Parcel p = Parcel.obtain();
                        n.writeToParcel(p, 0);
                        sb.append("\n").append(bold(getString(2131627202))).append(delim).append(String.valueOf(p.dataPosition())).append(' ').append(bold(getString(2131627203))).append(delim).append(String.valueOf(p.getBlobAshmemSize())).append("\n");
                        info.extra = sb;
                        logd("   [%d] %s: %s", Long.valueOf(info.timestamp), info.pkg, info.title);
                        list.add(info);
                    }
                }
            }
            return list;
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot load Notifications: ", e);
            return null;
        }
    }

    private Resources getResourcesForUserPackage(String pkg, int userId) {
        Resources r;
        if (pkg != null) {
            if (userId == -1) {
                userId = 0;
            }
            try {
                r = this.mPm.getResourcesForApplicationAsUser(pkg, userId);
            } catch (NameNotFoundException ex) {
                Log.e(TAG, "Icon package not found: " + pkg, ex);
                return null;
            }
        }
        r = this.mContext.getResources();
        return r;
    }

    private Drawable loadPackageIconDrawable(String pkg, int userId) {
        Drawable icon = null;
        try {
            icon = this.mPm.getApplicationIcon(pkg);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Cannot get application icon", e);
        }
        return icon;
    }

    private CharSequence loadPackageName(String pkg) {
        try {
            ApplicationInfo info = this.mPm.getApplicationInfo(pkg, 8192);
            if (info != null) {
                return this.mPm.getApplicationLabel(info);
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Cannot load package name", e);
        }
        return pkg;
    }

    private Drawable loadIconDrawable(String pkg, int userId, int resId) {
        Resources r = getResourcesForUserPackage(pkg, userId);
        if (resId == 0) {
            return null;
        }
        try {
            return r.getDrawable(resId, null);
        } catch (RuntimeException e) {
            Log.w(TAG, "Icon not found in " + (pkg != null ? Integer.valueOf(resId) : "<system>") + ": " + Integer.toHexString(resId), e);
            return null;
        }
    }
}
