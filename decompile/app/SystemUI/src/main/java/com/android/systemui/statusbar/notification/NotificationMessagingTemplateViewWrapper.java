package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.view.View;
import com.android.internal.widget.MessagingLinearLayout;
import com.android.systemui.statusbar.ExpandableNotificationRow;

public class NotificationMessagingTemplateViewWrapper extends NotificationTemplateViewWrapper {
    private View mContractedMessage;

    protected NotificationMessagingTemplateViewWrapper(Context ctx, View view, ExpandableNotificationRow row) {
        super(ctx, view, row);
    }

    private void resolveViews() {
        this.mContractedMessage = null;
        View container = this.mView.findViewById(16909430);
        if ((container instanceof MessagingLinearLayout) && ((MessagingLinearLayout) container).getChildCount() > 0) {
            MessagingLinearLayout messagingContainer = (MessagingLinearLayout) container;
            View child = messagingContainer.getChildAt(0);
            if (child.getId() == messagingContainer.getContractedChildId()) {
                this.mContractedMessage = child;
            }
        }
    }

    public void notifyContentUpdated(StatusBarNotification notification) {
        resolveViews();
        super.notifyContentUpdated(notification);
    }

    protected void updateTransformedTypes() {
        super.updateTransformedTypes();
        if (this.mContractedMessage != null) {
            this.mTransformationHelper.addTransformedView(2, this.mContractedMessage);
        }
    }
}
