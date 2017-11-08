package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.view.View;
import com.android.internal.widget.ImageFloatingTextView;
import com.android.systemui.statusbar.ExpandableNotificationRow;

public class NotificationBigTextTemplateViewWrapper extends NotificationTemplateViewWrapper {
    private ImageFloatingTextView mBigtext;

    protected NotificationBigTextTemplateViewWrapper(Context ctx, View view, ExpandableNotificationRow row) {
        super(ctx, view, row);
    }

    private void resolveViews(StatusBarNotification notification) {
        this.mBigtext = (ImageFloatingTextView) this.mView.findViewById(16909234);
    }

    public void notifyContentUpdated(StatusBarNotification notification) {
        resolveViews(notification);
        super.notifyContentUpdated(notification);
    }

    protected void updateTransformedTypes() {
        super.updateTransformedTypes();
        if (this.mBigtext != null) {
            this.mTransformationHelper.addTransformedView(2, this.mBigtext);
        }
    }
}
