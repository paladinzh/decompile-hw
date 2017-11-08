package android.support.v4.app;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.NotificationCompatBase.Action;
import android.widget.RemoteViews;
import java.util.ArrayList;

public class NotificationCompatApi21$Builder implements NotificationBuilderWithBuilderAccessor, NotificationBuilderWithActions {
    private Builder b;
    private RemoteViews mBigContentView;
    private RemoteViews mContentView;
    private Bundle mExtras;
    private RemoteViews mHeadsUpContentView;

    public NotificationCompatApi21$Builder(Context context, Notification n, CharSequence contentTitle, CharSequence contentText, CharSequence contentInfo, RemoteViews tickerView, int number, PendingIntent contentIntent, PendingIntent fullScreenIntent, Bitmap largeIcon, int progressMax, int progress, boolean progressIndeterminate, boolean showWhen, boolean useChronometer, int priority, CharSequence subText, boolean localOnly, String category, ArrayList<String> people, Bundle extras, int color, int visibility, Notification publicVersion, String groupKey, boolean groupSummary, String sortKey, RemoteViews contentView, RemoteViews bigContentView, RemoteViews headsUpContentView) {
        boolean z;
        Builder lights = new Builder(context).setWhen(n.when).setShowWhen(showWhen).setSmallIcon(n.icon, n.iconLevel).setContent(n.contentView).setTicker(n.tickerText, tickerView).setSound(n.sound, n.audioStreamType).setVibrate(n.vibrate).setLights(n.ledARGB, n.ledOnMS, n.ledOffMS);
        if ((n.flags & 2) != 0) {
            z = true;
        } else {
            z = false;
        }
        lights = lights.setOngoing(z);
        if ((n.flags & 8) != 0) {
            z = true;
        } else {
            z = false;
        }
        lights = lights.setOnlyAlertOnce(z);
        if ((n.flags & 16) != 0) {
            z = true;
        } else {
            z = false;
        }
        lights = lights.setAutoCancel(z).setDefaults(n.defaults).setContentTitle(contentTitle).setContentText(contentText).setSubText(subText).setContentInfo(contentInfo).setContentIntent(contentIntent).setDeleteIntent(n.deleteIntent);
        if ((n.flags & 128) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.b = lights.setFullScreenIntent(fullScreenIntent, z).setLargeIcon(largeIcon).setNumber(number).setUsesChronometer(useChronometer).setPriority(priority).setProgress(progressMax, progress, progressIndeterminate).setLocalOnly(localOnly).setGroup(groupKey).setGroupSummary(groupSummary).setSortKey(sortKey).setCategory(category).setColor(color).setVisibility(visibility).setPublicVersion(publicVersion);
        this.mExtras = new Bundle();
        if (extras != null) {
            this.mExtras.putAll(extras);
        }
        for (String person : people) {
            this.b.addPerson(person);
        }
        this.mContentView = contentView;
        this.mBigContentView = bigContentView;
        this.mHeadsUpContentView = headsUpContentView;
    }

    public void addAction(Action action) {
        NotificationCompatApi20.addAction(this.b, action);
    }

    public Builder getBuilder() {
        return this.b;
    }

    public Notification build() {
        this.b.setExtras(this.mExtras);
        Notification notification = this.b.build();
        if (this.mContentView != null) {
            notification.contentView = this.mContentView;
        }
        if (this.mBigContentView != null) {
            notification.bigContentView = this.mBigContentView;
        }
        if (this.mHeadsUpContentView != null) {
            notification.headsUpContentView = this.mHeadsUpContentView;
        }
        return notification;
    }
}
