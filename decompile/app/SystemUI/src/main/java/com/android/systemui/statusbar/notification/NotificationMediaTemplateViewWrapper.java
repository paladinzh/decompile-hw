package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.view.View;
import com.android.systemui.statusbar.ExpandableNotificationRow;

public class NotificationMediaTemplateViewWrapper extends NotificationTemplateViewWrapper {
    View mActions;

    protected NotificationMediaTemplateViewWrapper(Context ctx, View view, ExpandableNotificationRow row) {
        super(ctx, view, row);
    }

    private void resolveViews(StatusBarNotification notification) {
        this.mActions = this.mView.findViewById(16909232);
    }

    public void notifyContentUpdated(StatusBarNotification notification) {
        resolveViews(notification);
        super.notifyContentUpdated(notification);
    }

    protected void updateTransformedTypes() {
        super.updateTransformedTypes();
        if (this.mActions != null) {
            this.mTransformationHelper.addTransformedView(5, this.mActions);
        }
    }
}
