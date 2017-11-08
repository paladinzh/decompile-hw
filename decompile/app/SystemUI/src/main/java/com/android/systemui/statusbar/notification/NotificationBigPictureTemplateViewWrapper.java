package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.graphics.drawable.Icon;
import android.service.notification.StatusBarNotification;
import android.view.View;
import com.android.systemui.R;
import com.android.systemui.statusbar.ExpandableNotificationRow;

public class NotificationBigPictureTemplateViewWrapper extends NotificationTemplateViewWrapper {
    protected NotificationBigPictureTemplateViewWrapper(Context ctx, View view, ExpandableNotificationRow row) {
        super(ctx, view, row);
    }

    public void notifyContentUpdated(StatusBarNotification notification) {
        super.notifyContentUpdated(notification);
        updateImageTag(notification);
    }

    private void updateImageTag(StatusBarNotification notification) {
        Icon overRiddenIcon = (Icon) notification.getNotification().extras.getParcelable("android.largeIcon.big");
        if (overRiddenIcon != null) {
            this.mPicture.setTag(R.id.image_icon_tag, overRiddenIcon);
        }
    }
}
