package com.android.mms.transaction;

import android.app.Notification;
import android.app.Notification.Builder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.SpannableStringBuilder;
import android.widget.RemoteViews;
import com.android.mms.data.Contact;
import com.android.mms.data.Conversation;
import com.android.mms.transaction.MessagingNotification.NotificationInfo;
import java.util.ArrayList;
import java.util.SortedSet;

public class HwCustMessagingNotification {
    public static final int RCS_TYPE_GROUP = 3;
    public static final int RCS_TYPE_IM = 2;
    public static final int RCS_TYPE_NONE = 0;
    public static final int RCS_TYPE_SMS = 1;
    private static final String TAG = "HwCustMessagingNotification";

    public interface IHwCustMessagingNotificationCallback {
        boolean bluetoothHeadset(Context context);

        NotificationInfo createNotifcationInfoFromUri(Context context, Uri uri);

        void enableHeadsup(Notification notification, boolean z);

        RemoteViews getBigView(Context context, Bitmap bitmap, String str, CharSequence charSequence);

        Bitmap getContactAvatar(Context context, Contact contact);

        Drawable getDefaultAvatar(Context context, Contact contact, Conversation conversation);

        RemoteViews getRelpyView(Context context, Bitmap bitmap, String str, CharSequence charSequence);

        void headsetTone(Context context);

        void initSoundAndVibrateSettings(Context context, String str, Builder builder, int i);

        boolean isMmsBannerEnabled(Context context);

        boolean isNotificationBlocked(Context context, long j);

        void sendMultyNotifications(Context context, boolean z, boolean z2, boolean z3, int i, SortedSet<NotificationInfo> sortedSet, Uri uri);

        void setNotificationIcon(Context context, Contact contact, Conversation conversation, Builder builder);

        void updateSingleNotifications(Context context, long j, Uri uri);
    }

    public void initExt(Context context) {
    }

    public boolean isRcsSwitchOn() {
        return false;
    }

    public void blockingUpdateNewMessageIndicatorExt(Context context, long newMsgThreadId, boolean isStatusMessage, Uri messageUri) {
    }

    public void judgeRcsGroupAndSetSubject(SpannableStringBuilder sb, ArrayList<NotificationInfo> arrayList, int position) {
    }

    public String getDescription(Context context, String oldDescription, int totalFailedCount) {
        return oldDescription;
    }

    public Intent getNewIntentWithoutDownload(Context context, Intent oldIntent, long threadId) {
        return oldIntent;
    }

    public void clearData() {
    }

    public int getAllFailedMsgCount(Context context, long[] threadIdResult) {
        return 0;
    }

    public void setCurrentlyDisplayedThreadId(long threadId, long RCSType) {
    }

    public void removeGlobalGroupId(String globalGroupId) {
    }

    public void deleteGroupInviteNotificationID(Context context, String notificationId) {
    }

    public boolean getEnableSmsDeliverToast() {
        return true;
    }

    public boolean setNotificationVibrate(Context context, Builder noti) {
        return false;
    }

    public boolean vibrate(Context context, Vibrator vibrator) {
        return false;
    }

    public String getGroupMessageSenderName(Context context, String address, String subject, int subId, long threadId) {
        return subject;
    }

    public boolean isRcsOnAndGroupChatType(int chatType) {
        return false;
    }

    public void setHwCustCallback(IHwCustMessagingNotificationCallback callback) {
    }

    public void updateNotificationsExt(Context context, long newMsgThreadId, Uri msgUri, boolean checked, int isIncomingMessageType, Bundle bundle) {
    }

    public void checkAndUpdateNotifications(Context context, long newMsgThreadId, boolean isBannerMode) {
    }

    public boolean enableSmsNotifyInSilentMode() {
        return true;
    }

    public void playInchatTone(Context aContext) {
    }

    public void updateDefaultReceiveTone(Context aContext) {
    }

    public boolean getDisableSmsDeliverToastByCard() {
        return false;
    }
}
