package com.android.mms.transaction;

import android.app.ActivityManager;
import android.app.INotificationManager;
import android.app.INotificationManager.Stub;
import android.app.Notification;
import android.app.Notification.Action;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Audio.Media;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Inbox;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Sms.Intents;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.InboxStyle;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.SmsMessage;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.widget.RemoteViews;
import cn.com.xy.sms.sdk.SmartSmsPublicinfoUtil;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.android.messaging.util.OsUtil;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.data.Conversation;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.transaction.HwCustMessagingNotification.IHwCustMessagingNotificationCallback;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.ConversationList;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.SmartNewNotificationManager;
import com.android.mms.util.AddressUtils;
import com.android.mms.util.HwCustEcidLookup;
import com.android.mms.util.ItemLoadedCallback;
import com.android.mms.widget.MmsWidgetProvider;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.transaction.RcsMessagingNotification;
import com.android.rcs.transaction.RcsMessagingNotificationInfo;
import com.android.rcs.transaction.RcsNotificationReceiver;
import com.google.android.gms.R;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.MultimediaMessagePdu;
import com.google.android.mms.pdu.PduPersister;
import com.huawei.android.media.AudioManagerEx;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.crypto.CryptoMessageUtil;
import com.huawei.mms.service.NameMatchResult;
import com.huawei.mms.util.AvatarCache;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.NumberUtils;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.SmartArchiveSettingUtils;
import com.huawei.rcs.utils.RcsUtility;
import com.huawei.systemmanager.preventmode.HwPreventModeHelper;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class MessagingNotification {
    public static final NotificationInfoComparator INFO_COMPARATOR = new NotificationInfoComparator();
    private static final String[] MMS_STATUS_PROJECTION = new String[]{"thread_id", "date", "_id", "sub", "sub_cs", "sub_id"};
    private static final String[] MMS_THREAD_ID_PROJECTION = new String[]{"thread_id"};
    public static final Uri RCS_CONTENT_URI = Uri.parse("content://rcsim/chat");
    public static final Uri RCS_GROUP_CONTENT_URI = Uri.parse("content://rcsim/rcs_group_message");
    private static final String[] SMS_STATUS_PROJECTION = new String[]{"thread_id", "date", "address", "subject", "body", "sub_id", "_id"};
    private static final String[] SMS_THREAD_ID_PROJECTION = new String[]{"thread_id"};
    private static final Uri UNDELIVERED_URI = Uri.parse("content://mms-sms/undelivered");
    private static boolean bSupportWearable = true;
    private static HwCustMessagingNotificationHolder holder;
    private static CryptoMsgNotification mCryptoMsgNotification = new CryptoMsgNotification();
    private static HwCustEcidLookup mHwCustEcidLookup = ((HwCustEcidLookup) HwCustUtils.createObj(HwCustEcidLookup.class, new Object[0]));
    private static HwCustMessagingNotification mHwCustMsg = ((HwCustMessagingNotification) HwCustUtils.createObj(HwCustMessagingNotification.class, new Object[0]));
    private static ArrayList<Integer> mNotificationIdList = new ArrayList();
    private static HashMap<Integer, Long> mNotificationMap = new HashMap();
    private static RcsMessagingNotification mRcsMsg = new RcsMessagingNotification();
    private static long sCurrentlyDisplayedThreadId;
    private static final Object sCurrentlyDisplayedThreadLock = new Object();
    private static final Bundle sNotificationManagerCallExtral = new Bundle();
    private static final Uri sNotificationManagerCenterUri = Uri.parse("content://com.huawei.systemmanager.NotificationDBProvider");
    private static PduPersister sPduPersister;
    private static float sScreenDensity;
    private static boolean sShowListImage = false;
    private static Handler sToastHandler = HwBackgroundLoader.getUIHandler();

    public static class HwCustMessagingNotificationHolder implements IHwCustMessagingNotificationCallback {
        public boolean isNotificationBlocked(Context context, long newMsgThreadId) {
            return NewMessageUpdater.getInst().isNotificationBlocked(context, newMsgThreadId);
        }

        public boolean isMmsBannerEnabled(Context context) {
            return MessagingNotification.isMmsBannerEnabled(context);
        }

        public NotificationInfo createNotifcationInfoFromUri(Context context, Uri uri) {
            NewMessageUpdater.getInst();
            return NewMessageUpdater.createNotifcationInfoFromUri(context, uri);
        }

        public Bitmap getContactAvatar(Context context, Contact contact) {
            return MessagingNotification.getContactAvatar(context, contact);
        }

        public boolean bluetoothHeadset(Context context) {
            return MessagingNotification.bluetoothHeadset(context);
        }

        public void headsetTone(Context context) {
            MessagingNotification.headsetTone(context);
        }

        public void sendMultyNotifications(Context context, boolean isPreviewEnabled, boolean isNew, boolean isBannerMode, int uniqueThreadCount, SortedSet<NotificationInfo> notificationSet, Uri uri) {
            NewMessageUpdater.getInst().sendMultyNotifications(context, isPreviewEnabled, isNew, isBannerMode, true, uri, uniqueThreadCount, notificationSet);
        }

        public void updateSingleNotifications(Context context, long newMsgThreadId, Uri msgUri) {
            NewMessageUpdater.getInst().updateSingleNotifications(context, newMsgThreadId, msgUri, false);
        }

        public RemoteViews getRelpyView(Context context, Bitmap icon, String title, CharSequence content) {
            return MessagingNotification.getRelpyView(context, icon, title, content);
        }

        public RemoteViews getBigView(Context context, Bitmap icon, String title, CharSequence content) {
            return MessagingNotification.getBigView(context, icon, title, content);
        }

        public void initSoundAndVibrateSettings(Context context, String number, Builder builder, int subId) {
            MessagingNotification.initSoundAndVibrateSettings(context, number, builder, subId);
        }

        public void enableHeadsup(Notification noti, boolean enabled) {
            MessagingNotification.enableHeadsup(noti, enabled);
        }

        public void setNotificationIcon(Context context, Contact c, Conversation conv, Builder builder) {
            MessagingNotification.setNotificationIcon(context, c, conv, builder);
        }

        public Drawable getDefaultAvatar(Context context, Contact c, Conversation conv) {
            return MessagingNotification.getDefaultAvatar(context, c, conv);
        }
    }

    static final class MmsSmsDeliveryInfo {
        public CharSequence mTicker;
        public long mTimeMillis;

        public MmsSmsDeliveryInfo(CharSequence ticker, long timeMillis) {
            this.mTicker = ticker;
            this.mTimeMillis = timeMillis;
        }

        public void deliver() {
            MessagingNotification.sToastHandler.post(new Runnable() {
                public void run() {
                    ResEx.makeToast(MmsSmsDeliveryInfo.this.mTicker, (int) MmsSmsDeliveryInfo.this.mTimeMillis);
                }
            });
        }

        protected static final MmsSmsDeliveryInfo createFromUri(Context context, Uri messageUri) {
            Cursor cursor = SqliteWrapper.query(context, messageUri == null ? Sms.CONTENT_URI : messageUri, MessagingNotification.SMS_STATUS_PROJECTION, "(type = 2 AND status = 0)", null, "date");
            if (cursor == null) {
                return null;
            }
            try {
                if (!cursor.moveToLast()) {
                    return null;
                }
                MmsSmsDeliveryInfo mmsSmsDeliveryInfo = new MmsSmsDeliveryInfo(context.getString(R.string.delivery_toast_body, new Object[]{Contact.get(cursor.getString(2), false).getNameAndNumber()}), 3000);
                cursor.close();
                return mmsSmsDeliveryInfo;
            } finally {
                cursor.close();
            }
        }
    }

    private static class NewMessageUpdater {
        private static NewMessageUpdater mInst = new NewMessageUpdater();

        private NewMessageUpdater() {
        }

        private static NewMessageUpdater getInst() {
            return mInst;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private boolean isNotificationBlocked(Context context, long newMsgThreadId) {
            synchronized (MessagingNotification.sCurrentlyDisplayedThreadLock) {
                if (newMsgThreadId > 0) {
                    if (newMsgThreadId == MessagingNotification.sCurrentlyDisplayedThreadId) {
                        return true;
                    }
                }
            }
        }

        private void updateNotifications(Context context, long newMsgThreadId, Uri msgUri, boolean checked) {
            boolean isBannerEnabled = false;
            boolean userPresent = false;
            boolean isFirstMessage = false;
            if (isNotificationBlocked(context, newMsgThreadId)) {
                if (MessagingNotification.mHwCustMsg != null) {
                    MessagingNotification.mHwCustMsg.playInchatTone(context);
                }
                return;
            }
            if (MessagingNotification.mHwCustMsg != null) {
                MessagingNotification.mHwCustMsg.updateDefaultReceiveTone(context);
            }
            if (!checked || msgUri == null) {
                if (!NotificationReceiver.getInst().canSendNotification(context, msgUri)) {
                    if (msgUri != null && newMsgThreadId > 0) {
                        NotificationReceiver.getInst().cacheMessage(newMsgThreadId, msgUri);
                        long msgId = ContentUris.parseId(msgUri);
                        synchronized (MessagingNotification.class) {
                            MessagingNotification.mNotificationMap.put(Integer.valueOf((int) msgId), Long.valueOf(newMsgThreadId));
                        }
                    }
                    return;
                } else if (OsUtil.isOwnerLogin()) {
                    userPresent = NotificationReceiver.getInst().isUserPresent(context);
                    if (userPresent) {
                        isBannerEnabled = MessagingNotification.isMmsBannerEnabled(context);
                        if (isBannerEnabled && newMsgThreadId > 0 && msgUri != null) {
                            checked = true;
                        }
                    }
                }
            }
            if (checked) {
                if (NotificationReceiver.getInst().getCacheMessageSize() == 0) {
                    isFirstMessage = true;
                }
                updateSingleNotifications(context, newMsgThreadId, msgUri, isFirstMessage);
                return;
            }
            MLog.d("Mms_TX_NOTIFY", "UpdateMulty preview=" + true + "; banner=" + isBannerEnabled + "; present=" + userPresent + "; secondary=" + false);
            updateMultyNotifications(context, newMsgThreadId, true, false, false, false, msgUri);
        }

        private boolean updateSingleNotifications(Context context, long newMsgThreadId, Uri msgUri, boolean isFirstMessage) {
            NotificationInfo notificationInfo = createNotifcationInfoFromUri(context, msgUri);
            if (notificationInfo == null) {
                MLog.e("Mms_TX_NOTIFY", "Can't updateNotifications, createNotifcationInfoFromUri fail");
                return false;
            }
            Contact contact;
            String title = notificationInfo.getSenderName(context);
            Bitmap avatar = MessagingNotification.getContactAvatar(context, notificationInfo.mSender);
            boolean bAvatarStatus = false;
            if (MessagingNotification.mHwCustEcidLookup != null && MessagingNotification.mHwCustEcidLookup.getNameIdFeatureEnable() && notificationInfo.mSender.getPersonId() == 0) {
                title = MessagingNotification.mHwCustEcidLookup.getEcidName(context.getContentResolver(), notificationInfo.mSender.getNumber(), title);
                avatar = MessagingNotification.mHwCustEcidLookup.getEcidNotificationAvatar(context, notificationInfo.mSender.getNumber(), avatar);
                bAvatarStatus = avatar != null;
            }
            if (MessagingNotification.bluetoothHeadset(context)) {
                MessagingNotification.headsetTone(context);
            }
            CharSequence content = notificationInfo.formatMessage(context);
            int notificationId = NotificationReceiver.getInst().getNotificationId(context);
            PendingIntent clickPendingIntent = NotificationReceiver.getClickIntent(context, msgUri, newMsgThreadId, notificationInfo.mSubId, isFirstMessage, notificationId, notificationInfo, title);
            PendingIntent viewPendingIntent = NotificationReceiver.getContentIntent(context, msgUri, newMsgThreadId, notificationInfo.mClickIntent);
            PendingIntent deletPendingIntent = NotificationReceiver.getDeleteIntent(context, -1);
            Builder nBuilder = new Builder(context);
            SortedSet<NotificationInfo> treeSet = new TreeSet(MessagingNotification.INFO_COMPARATOR);
            Set<Long> hashSet = new HashSet(4);
            Conversation conv = Conversation.get(context, newMsgThreadId, true);
            MessagingNotification.addMmsNotificationInfos(context, hashSet, treeSet);
            MessagingNotification.addSmsNotificationInfos(context, hashSet, treeSet);
            if (CryptoMessageUtil.isMsgEncrypted(notificationInfo.mMessage)) {
                content = context.getResources().getQuantityString(R.plurals.encrypted_sms_notification_counts, 1, new Object[]{Integer.valueOf(1)});
            }
            MessagingNotification.initSoundAndVibrateSettings(context, notificationInfo.mSender.getNumber(), nBuilder, notificationInfo.mSubId);
            Action action = new Action.Builder(R.drawable.mms_ic_add_contact_dark, context.getString(R.string.quick_reply), clickPendingIntent).addRemoteInput(new RemoteInput.Builder("Quick_Reply").setLabel(context.getString(R.string.type_to_compose_text_enter_to_send)).build()).build();
            Bundle bundle = new Bundle();
            bundle.putInt("sub_id", notificationInfo.mSubId);
            bundle.putString("hw_notification_type", "hang_up");
            if (RcsCommonConfig.isRCSSwitchOn() && notificationInfo.mHwCustInnerInfo != null && (notificationInfo.mHwCustInnerInfo.isRcsChat() || notificationInfo.mHwCustInnerInfo.isGroupChat())) {
                bundle.putBoolean("hw_rcs", true);
            }
            Builder contentTitle = nBuilder.setWhen(notificationInfo.mTimeMillis).setCategory("msg").setPriority(6).setSmallIcon(R.drawable.stat_notify_sms).setContentIntent(viewPendingIntent).setDeleteIntent(deletPendingIntent).setExtras(bundle).setContentTitle(title);
            if (!bAvatarStatus) {
                contact = (conv.getRecipients() == null || conv.getRecipients().size() == 0) ? null : (Contact) conv.getRecipients().get(0);
                avatar = AvatarCache.drawableToBitmap(MessagingNotification.getDefaultAvatar(context, contact, conv));
            }
            contentTitle.setLargeIcon(avatar).setContentText(content);
            if (conv.getRecipients() == null || conv.getRecipients().size() == 0) {
                contact = null;
            } else {
                contact = (Contact) conv.getRecipients().get(0);
            }
            MessagingNotification.setNotificationIcon(context, contact, conv, nBuilder);
            MessagingNotification.enableHeadsUp(nBuilder, true);
            nBuilder.addAction(action);
            if (MmsConfig.getSupportSmartSmsFeature()) {
                Map<String, Object> smartResult = SmartNewNotificationManager.getSmartSmsResult(String.valueOf(notificationInfo.mMsgId), notificationInfo.getSenderName(context), notificationInfo.mMessage, false, null);
                if (smartResult != null) {
                    smartResult.put("threadId", conv.getThreadId() + "");
                }
                SmartNewNotificationManager.bindSmartNotifyView(context, nBuilder, smartResult, msgUri.getLastPathSegment(), notificationInfo.mSender.getNumber(), notificationInfo.mMessage, null);
            }
            ((NotificationManager) context.getSystemService("notification")).notifyAsUser(null, notificationId, nBuilder.build(), UserHandle.CURRENT);
            NotificationReceiver.getInst().markCurrentNotifyMessage(newMsgThreadId, msgUri, notificationInfo.mSubId);
            MLog.w("Mms_TX_NOTIFY", "UpdateSingleNotifications finish. " + (notificationInfo.mIsSms ? "Sms:" : "Mms:"));
            treeSet.clear();
            hashSet.clear();
            return true;
        }

        private void checkAndUpdateNotificationsForWap(Context context, long newMsgThreadId, boolean isBannerMode, boolean isSupportWatch, Uri uri) {
            if (!isNotificationBlocked(context, newMsgThreadId) && NotificationReceiver.getInst().canSendNotification(context, uri)) {
                updateMultyNotifications(context, newMsgThreadId, MessagingNotification.isMmsBannerEnabled(context), isBannerMode, isSupportWatch, true, uri);
            }
        }

        private void checkAndUpdateNotifications(Context context, long newMsgThreadId, boolean isBannerMode, boolean isSupportWatch, Uri uri) {
            if (!isNotificationBlocked(context, newMsgThreadId) && NotificationReceiver.getInst().canSendNotification(context, uri)) {
                updateMultyNotifications(context, newMsgThreadId, MessagingNotification.isMmsBannerEnabled(context), isBannerMode, isSupportWatch, false, uri);
            }
        }

        private void checkAndUpdateNotifications(Context context, long newMsgThreadId, boolean isBannerMode, boolean isSupportWatch, boolean isFirstMessage, Uri uri) {
            if (isFirstMessage && !isNotificationBlocked(context, newMsgThreadId)) {
                updateMultyNotifications(context, newMsgThreadId, MessagingNotification.isMmsBannerEnabled(context), isBannerMode, isSupportWatch, false, uri);
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void updateMultyNotifications(Context context, long newMsgThreadId, boolean preViewEnabled, boolean isBannerMode, boolean isSupportWatch, boolean isWap, Uri uri) {
            synchronized (MessagingNotification.sCurrentlyDisplayedThreadLock) {
                if (newMsgThreadId > 0) {
                    if (newMsgThreadId == MessagingNotification.sCurrentlyDisplayedThreadId) {
                        MLog.d("Mms_TX_NOTIFY", "updateMultyNotifications: Displayed ThreadId isnewMsgThreadId " + newMsgThreadId);
                    }
                }
            }
        }

        private Intent getMultipleIntent(Context context, SortedSet<NotificationInfo> notificationSet) {
            Intent mainActivityIntent = ComposeMessageActivity.createIntent(context, -1);
            mainActivityIntent.putExtra("new_message_type", "multi-thread");
            mainActivityIntent.putExtra("arg_number_type", MessagingNotification.getNumberType(context, notificationSet));
            mainActivityIntent.setFlags(872415232);
            mainActivityIntent.setType("vnd.android-dir/mms-sms");
            return mainActivityIntent;
        }

        private boolean sendMultySmartNotifications(Context context, boolean isNew, boolean isBannerMode, boolean supportWatch, Uri uri, int uniqueThreadCount, SortedSet<NotificationInfo> notificationSet) {
            boolean summaryVibranate = false;
            NotificationInfo mostRecentNotification = (NotificationInfo) notificationSet.first();
            int threadCount = uniqueThreadCount;
            if (supportWatch) {
                if (uri != null) {
                    long id = ContentUris.parseId(uri);
                    if (id > 0) {
                        for (NotificationInfo info : notificationSet) {
                            if (info.mMsgId == id) {
                                mostRecentNotification = info;
                                break;
                            }
                        }
                    }
                }
                threadCount = 1;
            }
            Map<String, Object> smartResult = SmartNewNotificationManager.getSmartSmsResult(String.valueOf(mostRecentNotification.mMsgId), mostRecentNotification.getSenderName(context), mostRecentNotification.mMessage, false, null);
            if (smartResult == null) {
                return false;
            }
            Uri msgUri;
            NotificationInfo notificationInfo = mostRecentNotification;
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            Conversation conv = Conversation.get(context, notificationInfo.mThreadId, true);
            Intent clickIntent = mostRecentNotification.mClickIntent;
            clickIntent.putExtra("fromNotification", true);
            PendingIntent pendingIntent = PendingIntent.getActivityAsUser(context, 0, clickIntent, 134217728, null, UserHandle.CURRENT);
            PendingIntent deleteIntent = NotificationReceiver.getDeleteIntent(context, (int) notificationInfo.mMsgId);
            if (notificationInfo.mIsSms) {
                msgUri = Sms.CONTENT_URI.buildUpon().appendPath(Long.toString(notificationInfo.mMsgId)).build();
            } else {
                msgUri = Mms.CONTENT_URI.buildUpon().appendPath(Long.toString(notificationInfo.mMsgId)).build();
            }
            NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.mms_ic_add_contact_dark, context.getString(R.string.quick_reply), NotificationReceiver.getClickIntent(context, msgUri, notificationInfo.mThreadId, notificationInfo.mSubId, false, (int) notificationInfo.mMsgId, notificationInfo, mostRecentNotification.mTitle)).addRemoteInput(new android.support.v4.app.RemoteInput.Builder("Quick_Reply").setLabel(context.getString(R.string.type_to_compose_text_enter_to_send)).build()).build();
            NotificationCompat.Builder group = builder.setWhen(mostRecentNotification.mTimeMillis).setSmallIcon(R.drawable.stat_notify_sms).setContentIntent(pendingIntent).setContentText(notificationInfo.mMessage).setContentTitle(notificationInfo.mSender.getNumber()).setDeleteIntent(deleteIntent).setAutoCancel(true).setGroup("group_key_mms");
            Contact contact = (conv.getRecipients() == null || conv.getRecipients().size() == 0) ? null : (Contact) conv.getRecipients().get(0);
            group.setLargeIcon(AvatarCache.drawableToBitmap(MessagingNotification.getDefaultAvatar(context, contact, conv))).setPriority(6).addAction(action);
            if (conv.getRecipients() == null || conv.getRecipients().size() == 0) {
                contact = null;
            } else {
                contact = (Contact) conv.getRecipients().get(0);
            }
            MessagingNotification.setNotificationIcon(context, contact, conv, builder);
            if (MessagingNotification.bSupportWearable) {
                builder.setGroup("group_key_mms");
            }
            if (isNew || isBannerMode) {
                if (MessagingNotification.bluetoothHeadset(context)) {
                    MessagingNotification.headsetTone(context);
                }
                summaryVibranate = true;
                MessagingNotification.initSoundAndVibrateSettings(context, ((NotificationInfo) notificationSet.first()).mSender.getNumber(), builder, ((NotificationInfo) notificationSet.first()).mSubId);
            }
            if (isNew) {
                isBannerMode = true;
            }
            MessagingNotification.enableHeadsUp(builder, isBannerMode);
            MessagingNotification.setDefaultLight(builder, ((NotificationInfo) notificationSet.first()).mSender.getNumber());
            smartResult.put("threadId", conv.getThreadId() + "");
            if (!SmartNewNotificationManager.bindDropSmartNotifyView(context, builder, smartResult, String.valueOf(mostRecentNotification.mMsgId), notificationInfo.getSenderName(context), notificationInfo.mMessage, null)) {
                return false;
            }
            Intent clickIntentSummary;
            Notification notification = builder.build();
            NotificationManagerCompat.from(context).notify((int) notificationInfo.mMsgId, notification);
            if (threadCount == 1) {
                clickIntentSummary = notificationInfo.mClickIntent;
            } else {
                clickIntentSummary = getMultipleIntent(context, notificationSet);
            }
            clickIntentSummary.putExtra("fromNotification", true);
            PendingIntent pendingIntentSummary = PendingIntent.getActivityAsUser(context, 0, clickIntentSummary, 134217728, null, UserHandle.CURRENT);
            if (summaryVibranate) {
                MessagingNotification.showSummaryNotification(notificationSet.size(), context, notificationInfo.mSender.getNumber(), notificationInfo.mMessage, pendingIntentSummary, deleteIntent, notificationInfo.mSender.getNumber(), notificationInfo.getTicker(context), notificationInfo.mSubId);
            } else {
                MessagingNotification.showSummaryNotification(notificationSet.size(), context, notificationInfo.mSender.getNumber(), notificationInfo.mMessage, pendingIntentSummary, deleteIntent, null, notificationInfo.getTicker(context), notificationInfo.mSubId);
            }
            MessagingNotification.addToNotiticationList(Integer.valueOf((int) notificationInfo.mMsgId));
            MessagingNotification.saveNotificationIdListToPreference(context);
            if (notification.largeIcon != null) {
                notification.largeIcon.recycle();
            }
            return true;
        }

        private boolean sendMultySmartNotificationsForGuest(Context context, boolean isNew, boolean isBannerMode, int uniqueThreadCount, SortedSet<NotificationInfo> notificationSet) {
            NotificationInfo mostRecentNotification = (NotificationInfo) notificationSet.first();
            Map<String, Object> smartResult = SmartNewNotificationManager.getSmartSmsResult(String.valueOf(mostRecentNotification.mMsgId), mostRecentNotification.getSenderName(context), mostRecentNotification.mMessage, false, null);
            if (smartResult == null) {
                return false;
            }
            NotificationInfo notificationInfo = mostRecentNotification;
            Conversation conv = Conversation.get(context, mostRecentNotification.mThreadId, true);
            Builder nBuilder = new Builder(context);
            Intent clickIntent = mostRecentNotification.mClickIntent;
            clickIntent.putExtra("fromNotification", true);
            Builder deleteIntent = nBuilder.setWhen(mostRecentNotification.mTimeMillis).setSmallIcon(R.drawable.stat_notify_sms).setContentIntent(PendingIntent.getActivityAsUser(context, 0, clickIntent, 134217728, null, UserHandle.CURRENT)).setContentText(mostRecentNotification.mMessage).setContentTitle(mostRecentNotification.mSender.getNumber()).setDeleteIntent(NotificationReceiver.getDeleteIntent(context));
            Contact contact = (conv.getRecipients() == null || conv.getRecipients().size() == 0) ? null : (Contact) conv.getRecipients().get(0);
            deleteIntent.setLargeIcon(AvatarCache.drawableToBitmap(MessagingNotification.getDefaultAvatar(context, contact, conv)));
            contact = (conv.getRecipients() == null || conv.getRecipients().size() == 0) ? null : (Contact) conv.getRecipients().get(0);
            MessagingNotification.setNotificationIcon(context, contact, conv, nBuilder);
            if (isNew || isBannerMode) {
                if (MessagingNotification.bluetoothHeadset(context)) {
                    MessagingNotification.headsetTone(context);
                }
                MessagingNotification.initSoundAndVibrateSettings(OsUtil.getContextOfCurrentUser(context), ((NotificationInfo) notificationSet.first()).mSender.getNumber(), nBuilder, ((NotificationInfo) notificationSet.first()).mSubId);
            }
            boolean -wrap4 = (isNew || isBannerMode) ? MessagingNotification.isMmsBannerEnabled(context) : false;
            MessagingNotification.enableHeadsUp(nBuilder, -wrap4);
            MessagingNotification.setDefaultLight(nBuilder, ((NotificationInfo) notificationSet.first()).mSender.getNumber());
            if (!SmartNewNotificationManager.bindSmartNotifyView(context, nBuilder, smartResult, String.valueOf(mostRecentNotification.mMsgId), mostRecentNotification.getSenderName(context), mostRecentNotification.mMessage, null)) {
                return false;
            }
            Notification notification = nBuilder.build();
            ((NotificationManager) context.getSystemService("notification")).notifyAsUser(null, 123, notification, new UserHandle(ActivityManager.getCurrentUser()));
            if (notification.largeIcon != null) {
                notification.largeIcon.recycle();
            }
            return true;
        }

        public NotificationInfo getMostRecentNotification(SortedSet<NotificationInfo> notificationSet, Uri uri, Boolean supportWatch) {
            NotificationInfo mostRecentNotification = (NotificationInfo) notificationSet.first();
            if (!supportWatch.booleanValue() || uri == null) {
                return mostRecentNotification;
            }
            long id = ContentUris.parseId(uri);
            if (id <= 0) {
                return mostRecentNotification;
            }
            for (NotificationInfo info : notificationSet) {
                if (info.mMsgId == id) {
                    return info;
                }
            }
            return mostRecentNotification;
        }

        public Intent getPendingIntent(NotificationInfo info) {
            return info.mClickIntent;
        }

        private void sendMultyNotifications(Context context, boolean isPreviewEnabled, boolean isNew, boolean isBannerMode, boolean supportWatch, Uri uri, int uniqueThreadCount, SortedSet<NotificationInfo> notificationSet) {
            Bundle bundle;
            Intent clickIntentSummary;
            Contact contact;
            int messageCount = notificationSet.size();
            boolean summaryVibranate = false;
            NotificationInfo mostRecentNotification = getMostRecentNotification(notificationSet, uri, Boolean.valueOf(supportWatch));
            MLog.d("Mms_TX_NOTIFY", "in sendMultyNotifications(),msgLength = " + (mostRecentNotification.mMessage != null ? mostRecentNotification.mMessage.length() : 0));
            long mostRecentNotificationThreadId = mostRecentNotification.mThreadId;
            int mostRecentNotiThreadUnreadMessageCount = 0;
            String title = "";
            String description = "";
            String tickle = "";
            Resources rs = context.getResources();
            if (!isPreviewEnabled) {
                title = rs.getQuantityString(R.plurals.recipient_count, uniqueThreadCount, new Object[]{Integer.valueOf(uniqueThreadCount)});
                description = context.getResources().getQuantityString(R.plurals.message_count_notification, messageCount, new Object[]{Integer.valueOf(messageCount)});
            } else if (uniqueThreadCount > 1) {
                title = rs.getQuantityString(R.plurals.message_count_notification, messageCount, new Object[]{Integer.valueOf(messageCount)});
                String firstName = HwMessageUtils.formatNumberString(mostRecentNotification.getSenderName(context));
                description = rs.getQuantityString(R.plurals.notification_people_detail, uniqueThreadCount, new Object[]{firstName, Integer.valueOf(uniqueThreadCount)});
            } else if (messageCount > 1) {
                title = mostRecentNotification.getSenderName(context);
                description = rs.getQuantityString(R.plurals.message_count_notification, messageCount, new Object[]{Integer.valueOf(messageCount)});
            } else {
                title = mostRecentNotification.getSenderName(context);
                description = mostRecentNotification.mMessage;
                if (mostRecentNotification.mHwCustInnerInfo != null && mostRecentNotification.mHwCustInnerInfo.isGroupChat()) {
                    description = mostRecentNotification.mHwCustInnerInfo.buildRcsMessage(mostRecentNotification.getSenderNumber(), mostRecentNotification.mMessage, mostRecentNotification.mThreadId);
                }
            }
            Bitmap bitmap = null;
            boolean bAvatarStatus = false;
            if (MessagingNotification.mHwCustEcidLookup != null && MessagingNotification.mHwCustEcidLookup.getNameIdFeatureEnable() && mostRecentNotification.mSender.getPersonId() == 0) {
                bitmap = MessagingNotification.mHwCustEcidLookup.getEcidNotificationAvatar(context, mostRecentNotification.getSenderNumber(), null);
                bAvatarStatus = bitmap != null;
            }
            long notificationId = mostRecentNotification.mMsgId;
            if (mostRecentNotification.mHwCustInnerInfo != null && mostRecentNotification.mHwCustInnerInfo.isGroupChat()) {
                notificationId = mostRecentNotification.mNotificationId;
                for (NotificationInfo notificationInfo : notificationSet) {
                    if (notificationInfo.mThreadId == mostRecentNotificationThreadId) {
                        mostRecentNotiThreadUnreadMessageCount++;
                    }
                }
                StringBuilder notificationDescription = new StringBuilder();
                notificationDescription.append("(").append(rs.getString(R.string.rcs_group_new_message_count, new Object[]{Integer.valueOf(mostRecentNotiThreadUnreadMessageCount)})).append(") ");
                notificationDescription.append(RcsUtility.getGroupContactShowName(mostRecentNotification.getSenderNumber(), mostRecentNotification.mThreadId));
                notificationDescription.append(":");
                notificationDescription.append(mostRecentNotification.mMessage);
                description = notificationDescription.toString();
            }
            description = MessagingNotification.mCryptoMsgNotification.updateNotificationContent(context, description, notificationSet);
            PendingIntent pendingIntent = null;
            Uri msgUri = uri;
            PendingIntent clickPendingIntent = null;
            if (mostRecentNotification.mIsSms) {
                msgUri = Sms.CONTENT_URI.buildUpon().appendPath(Long.toString(mostRecentNotification.mMsgId)).build();
            } else {
                msgUri = Mms.CONTENT_URI.buildUpon().appendPath(Long.toString(mostRecentNotification.mMsgId)).build();
            }
            Intent clickIntent;
            if (!RcsCommonConfig.isRCSSwitchOn()) {
                clickIntent = getPendingIntent(mostRecentNotification);
                clickIntent.putExtra("fromNotification", true);
                pendingIntent = PendingIntent.getActivityAsUser(context, 0, clickIntent, 134217728, null, UserHandle.CURRENT);
                MLog.d("Mms_TX_NOTIFY", "in sendMultyNotifications(), msgUri = " + msgUri + ", nId = " + ((int) mostRecentNotification.mMsgId));
                clickPendingIntent = NotificationReceiver.getClickIntent(context, msgUri, mostRecentNotification.mThreadId, mostRecentNotification.mSubId, false, (int) notificationId, mostRecentNotification, title);
            } else if (mostRecentNotification.mHwCustInnerInfo != null) {
                boolean isFirstMessage;
                if (mostRecentNotification.mHwCustInnerInfo.isRcsChat()) {
                    msgUri = MessagingNotification.RCS_CONTENT_URI.buildUpon().appendEncodedPath(Long.toString(mostRecentNotification.mMsgId)).build();
                    isFirstMessage = false;
                    if (NotificationReceiver.getInst().getCacheMessageSize() == 0) {
                        isFirstMessage = true;
                    }
                    clickPendingIntent = RcsNotificationReceiver.getClickIntent(context, msgUri, mostRecentNotification.mThreadId, mostRecentNotification.mSubId, "isChat", mostRecentNotification.getSenderNumber(), isFirstMessage, (int) notificationId, mostRecentNotification, false);
                    bundle = new Bundle();
                    bundle.putString(NumberInfo.TYPE_KEY, "isChat");
                    bundle.putString("address", mostRecentNotification.getSenderNumber());
                    pendingIntent = RcsNotificationReceiver.getContentIntent(context, msgUri, mostRecentNotification.mThreadId, bundle);
                } else if (mostRecentNotification.mHwCustInnerInfo.isGroupChat()) {
                    msgUri = MessagingNotification.RCS_GROUP_CONTENT_URI.buildUpon().appendEncodedPath(Long.toString(mostRecentNotification.mMsgId)).build();
                    String groupChatID = RcsMessagingNotification.getGroupId(context, mostRecentNotification.mThreadId)[0];
                    isFirstMessage = false;
                    if (NotificationReceiver.getInst().getCacheMessageSize() == 0) {
                        isFirstMessage = true;
                    }
                    clickPendingIntent = RcsNotificationReceiver.getClickIntent(context, msgUri, mostRecentNotification.mThreadId, mostRecentNotification.mSubId, "isGroup", groupChatID, isFirstMessage, (int) notificationId, mostRecentNotification, false);
                    bundle = new Bundle();
                    bundle.putString(NumberInfo.TYPE_KEY, "isGroup");
                    bundle.putString("groupId", groupChatID);
                    pendingIntent = RcsNotificationReceiver.getContentIntent(context, msgUri, mostRecentNotification.mThreadId, bundle);
                } else {
                    clickIntent = getPendingIntent(mostRecentNotification);
                    clickIntent.putExtra("fromNotification", true);
                    pendingIntent = PendingIntent.getActivityAsUser(context, 0, clickIntent, 134217728, null, UserHandle.CURRENT);
                    MLog.d("Mms_TX_NOTIFY", "in sendMultyNotifications(), msgUri = " + msgUri + ", nId = " + ((int) mostRecentNotification.mMsgId));
                    clickPendingIntent = NotificationReceiver.getClickIntent(context, msgUri, mostRecentNotification.mThreadId, mostRecentNotification.mSubId, false, (int) notificationId, mostRecentNotification, title);
                }
            }
            if (uniqueThreadCount == 1) {
                clickIntentSummary = mostRecentNotification.mClickIntent;
            } else {
                clickIntentSummary = getMultipleIntent(context, notificationSet);
            }
            clickIntentSummary.putExtra("fromNotification", true);
            PendingIntent pendingIntentSummary = PendingIntent.getActivityAsUser(context, 0, clickIntentSummary, 134217728, null, UserHandle.CURRENT);
            PendingIntent deleteIntent = NotificationReceiver.getDeleteIntent(context, (int) mostRecentNotification.mMsgId);
            Conversation conv = Conversation.get(context, mostRecentNotification.mThreadId, true);
            NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.mms_ic_add_contact_dark, context.getString(R.string.quick_reply), clickPendingIntent).addRemoteInput(new android.support.v4.app.RemoteInput.Builder("Quick_Reply").setLabel(context.getString(R.string.type_to_compose_text_enter_to_send)).build()).build();
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            NotificationCompat.Builder autoCancel = builder.setWhen(mostRecentNotification.mTimeMillis).setSmallIcon(R.drawable.stat_notify_sms).setContentIntent(pendingIntent).setContentTitle(mostRecentNotification.getSenderName(context)).setDeleteIntent(deleteIntent).setAutoCancel(true);
            if (!bAvatarStatus) {
                contact = (conv.getRecipients() == null || conv.getRecipients().size() == 0) ? null : (Contact) conv.getRecipients().get(0);
                bitmap = AvatarCache.drawableToBitmap(MessagingNotification.getDefaultAvatar(context, contact, conv));
            }
            autoCancel.setLargeIcon(bitmap).setPriority(6);
            contact = (conv.getRecipients() == null || conv.getRecipients().size() == 0) ? null : (Contact) conv.getRecipients().get(0);
            MessagingNotification.setNotificationIcon(context, contact, conv, builder);
            if (CryptoMessageUtil.isMsgEncrypted(mostRecentNotification.mMessage)) {
                builder.setContentText(context.getResources().getQuantityString(R.plurals.encrypted_sms_notification_counts, 1, new Object[]{Integer.valueOf(1)}));
            } else {
                builder.setContentText(mostRecentNotification.formatMessage(context));
            }
            if (mostRecentNotification.mHwCustInnerInfo != null && mostRecentNotification.mHwCustInnerInfo.isGroupChat()) {
                builder.setContentText(description);
            }
            if (MessagingNotification.bSupportWearable) {
                builder.setGroup("group_key_mms").addAction(action);
            }
            if (isNew || isBannerMode) {
                if (MessagingNotification.bluetoothHeadset(context)) {
                    MessagingNotification.headsetTone(context);
                }
                summaryVibranate = true;
                MessagingNotification.initSoundAndVibrateSettings(context, ((NotificationInfo) notificationSet.first()).mSender.getNumber(), builder, ((NotificationInfo) notificationSet.first()).mSubId);
            }
            MessagingNotification.setDefaultLight(builder, ((NotificationInfo) notificationSet.first()).mSender.getNumber());
            CharSequence ticker = "";
            if (isNew && !isBannerMode) {
                ticker = mostRecentNotification.getTicker(context);
                if (!TextUtils.isEmpty(ticker)) {
                    builder.setTicker(ticker);
                }
            }
            MLog.d("Mms_TX_NOTIFY", "updateNotification: update new for all message." + isBannerMode + isNew);
            NotificationManagerCompat nm = NotificationManagerCompat.from(context);
            boolean -wrap4 = (isNew || isBannerMode) ? MessagingNotification.isMmsBannerEnabled(context) : false;
            MessagingNotification.enableHeadsUp(builder, -wrap4);
            if (RcsCommonConfig.isRCSSwitchOn() && mostRecentNotification.mHwCustInnerInfo != null && (mostRecentNotification.mHwCustInnerInfo.isRcsChat() || mostRecentNotification.mHwCustInnerInfo.isGroupChat())) {
                bundle = new Bundle();
                bundle.putInt("sub_id", mostRecentNotification.mSubId);
                bundle.putBoolean("hw_rcs", true);
                builder.setExtras(bundle);
            }
            Notification notification = builder.build();
            if (RcsCommonConfig.isRCSSwitchOn()) {
                nm.notify((int) mostRecentNotification.getNotificationId(), notification);
                MessagingNotification.addToNotiticationList(Integer.valueOf((int) mostRecentNotification.getNotificationId()));
            } else {
                nm.notify((int) mostRecentNotification.mMsgId, notification);
                MessagingNotification.addToNotiticationList(Integer.valueOf((int) mostRecentNotification.mMsgId));
            }
            MessagingNotification.saveNotificationIdListToPreference(context);
            if (summaryVibranate) {
                MessagingNotification.showSummaryNotification(messageCount, context, title, description, pendingIntentSummary, deleteIntent, ((NotificationInfo) notificationSet.first()).mSender.getNumber(), ticker, mostRecentNotification.mSubId);
            } else {
                MessagingNotification.showSummaryNotification(messageCount, context, title, description, pendingIntentSummary, deleteIntent, null, ticker, mostRecentNotification.mSubId);
            }
            if (notification.largeIcon != null) {
                notification.largeIcon.recycle();
            }
        }

        private void sendMultyNotificationsForGuest(Context context, boolean isPreviewEnabled, boolean isNew, boolean isBannerMode, int uniqueThreadCount, SortedSet<NotificationInfo> notificationSet) {
            Intent clickIntent;
            int messageCount = notificationSet.size();
            NotificationInfo mostRecentNotification = (NotificationInfo) notificationSet.first();
            String title = "";
            String description = "";
            String tickle = "";
            Resources rs = context.getResources();
            if (messageCount > 1) {
                title = context.getString(R.string.notification_multiple_title);
                description = rs.getQuantityString(R.plurals.message_count_notification, messageCount, new Object[]{Integer.valueOf(messageCount)});
            } else {
                title = context.getString(R.string.notification_multiple_title);
                description = mostRecentNotification.mMessage;
                if (mostRecentNotification.mHwCustInnerInfo != null && mostRecentNotification.mHwCustInnerInfo.isGroupChat()) {
                    description = mostRecentNotification.mHwCustInnerInfo.buildRcsMessage(mostRecentNotification.getSenderNumber(), mostRecentNotification.mMessage, mostRecentNotification.mThreadId);
                }
            }
            description = MessagingNotification.mCryptoMsgNotification.updateNotificationContent(context, description, notificationSet);
            if (uniqueThreadCount == 1) {
                clickIntent = mostRecentNotification.mClickIntent;
            } else {
                clickIntent = getMultipleIntent(context, notificationSet);
            }
            clickIntent.putExtra("fromNotification", true);
            PendingIntent pendingIntent = PendingIntent.getActivityAsUser(context, 0, clickIntent, 134217728, null, UserHandle.CURRENT);
            PendingIntent deleteIntent = NotificationReceiver.getDeleteIntent(context);
            Conversation conv = Conversation.get(context, mostRecentNotification.mThreadId, true);
            Builder nBuilder = new Builder(context);
            Builder deleteIntent2 = nBuilder.setWhen(mostRecentNotification.mTimeMillis).setSmallIcon(R.drawable.stat_notify_sms).setContentIntent(pendingIntent).setContentText(description).setContentTitle(title).setDeleteIntent(deleteIntent);
            Contact contact = (conv.getRecipients() == null || conv.getRecipients().size() == 0) ? null : (Contact) conv.getRecipients().get(0);
            deleteIntent2.setLargeIcon(AvatarCache.drawableToBitmap(MessagingNotification.getDefaultAvatar(context, contact, conv)));
            if (conv.getRecipients() == null || conv.getRecipients().size() == 0) {
                contact = null;
            } else {
                contact = (Contact) conv.getRecipients().get(0);
            }
            MessagingNotification.setNotificationIcon(context, contact, conv, nBuilder);
            if (isNew || isBannerMode) {
                if (MessagingNotification.bluetoothHeadset(context)) {
                    MessagingNotification.headsetTone(context);
                }
                MessagingNotification.initSoundAndVibrateSettings(OsUtil.getContextOfCurrentUser(context), ((NotificationInfo) notificationSet.first()).mSender.getNumber(), nBuilder, ((NotificationInfo) notificationSet.first()).mSubId);
            }
            MessagingNotification.setDefaultLight(nBuilder, ((NotificationInfo) notificationSet.first()).mSender.getNumber());
            if (isNew && !isBannerMode) {
                CharSequence ticker = mostRecentNotification.getTicker(context);
                if (!TextUtils.isEmpty(ticker)) {
                    nBuilder.setTicker(ticker);
                }
            }
            MLog.d("Mms_TX_NOTIFY", "updateNotification: update new for all message." + isBannerMode + isNew);
            NotificationManager nm = (NotificationManager) context.getSystemService("notification");
            boolean -wrap4 = (isNew || isBannerMode) ? MessagingNotification.isMmsBannerEnabled(context) : false;
            MessagingNotification.enableHeadsUp(nBuilder, -wrap4);
            Notification notification = nBuilder.build();
            nm.cancelAsUser(null, 123, new UserHandle(ActivityManager.getCurrentUser()));
            nm.notifyAsUser(null, 123, notification, new UserHandle(ActivityManager.getCurrentUser()));
            if (notification.largeIcon != null) {
                notification.largeIcon.recycle();
            }
        }

        private static NotificationInfo createNotifcationInfoFromUri(Context context, Uri uri) {
            String authority = uri.getAuthority();
            boolean isMms = Mms.CONTENT_URI.getAuthority().equals(authority);
            boolean isSms = Sms.CONTENT_URI.getAuthority().equals(authority);
            if (isMms || isSms) {
                Cursor cursor;
                if (isMms) {
                    cursor = SqliteWrapper.query(context, uri, MessagingNotification.MMS_STATUS_PROJECTION, "(msg_box=1 AND seen=0 AND (m_type=130 OR m_type=132))", null, null);
                } else {
                    cursor = SqliteWrapper.query(context, uri, MessagingNotification.SMS_STATUS_PROJECTION, "(type = 1 AND seen = 0)", null, null);
                }
                if (cursor != null) {
                    try {
                        if (cursor.moveToFirst()) {
                            NotificationInfo -wrap0;
                            if (isMms) {
                                -wrap0 = NotificationInfo.createMmsNotifcationInfoFromCursor(context, cursor);
                            } else {
                                -wrap0 = NotificationInfo.createSmsNotifcationInfoFromCursor(context, cursor);
                            }
                            if (cursor != null) {
                                cursor.close();
                            }
                            return -wrap0;
                        }
                    } catch (Exception e) {
                        MLog.e("Mms_TX_NOTIFY", "add Notification info error", (Throwable) e);
                        if (cursor != null) {
                            cursor.close();
                        }
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            MLog.d("Mms_TX_NOTIFY", "createNotifcationInfoFromUri unsupport uri " + uri + " " + authority + " " + Mms.CONTENT_URI.getAuthority());
            return null;
        }
    }

    public static final class NotificationInfo {
        public final int mAttachmentType;
        public final Intent mClickIntent;
        public RcsMessagingNotificationInfo mHwCustInnerInfo = null;
        public final boolean mIsSms;
        public final String mMessage;
        public long mMsgId;
        private long mNotificationId;
        public final Contact mSender;
        public final int mSubId;
        public final String mSubject;
        public final long mThreadId;
        public CharSequence mTicker;
        public final long mTimeMillis;
        public String mTitle;

        public void setMsgId(long msgId) {
            this.mMsgId = msgId;
        }

        public NotificationInfo(boolean isSms, Intent clickIntent, String message, String subject, CharSequence ticker, long timeMillis, String title, Bitmap attachmentBitmap, Contact sender, int attachmentType, long threadId, int subId) {
            this.mIsSms = isSms;
            this.mClickIntent = clickIntent;
            this.mSubject = subject;
            this.mTicker = ticker;
            this.mTimeMillis = timeMillis;
            this.mTitle = title;
            this.mMessage = message;
            this.mSender = sender;
            this.mAttachmentType = attachmentType;
            this.mThreadId = threadId;
            this.mSubId = subId;
            this.mHwCustInnerInfo = new RcsMessagingNotificationInfo();
        }

        public NotificationInfo(boolean isSms, Intent clickIntent, String message, String subject, CharSequence ticker, long timeMillis, String title, Bitmap attachmentBitmap, Contact sender, int attachmentType, long threadId, int subId, int imChatType) {
            this.mIsSms = isSms;
            this.mClickIntent = clickIntent;
            this.mMessage = message;
            this.mSubject = subject;
            this.mTicker = ticker;
            this.mTimeMillis = timeMillis;
            this.mTitle = title;
            this.mSender = sender;
            this.mAttachmentType = attachmentType;
            this.mThreadId = threadId;
            this.mSubId = subId;
            this.mHwCustInnerInfo = new RcsMessagingNotificationInfo(imChatType);
            if (this.mHwCustInnerInfo.isGroupChat()) {
                this.mNotificationId = threadId;
            }
        }

        public long getTime() {
            return this.mTimeMillis;
        }

        public String getSenderName(Context context) {
            if (this.mHwCustInnerInfo != null && this.mHwCustInnerInfo.isGroupChat()) {
                MLog.i("Mms_TX_NOTIFY", "Get Sender From RCS");
                return this.mSubject;
            } else if (OsUtil.isInLoginUser()) {
                return MessagingNotification.getMessageSenderName(context, this.mTitle, this.mSender, this.mThreadId);
            } else {
                return context.getString(R.string.notification_multiple_title);
            }
        }

        public String getSenderNumber() {
            return this.mSender.getNumber();
        }

        public CharSequence getTicker(Context context) {
            if (this.mTicker == null) {
                this.mTicker = MessagingNotification.buildTickerMessage(context, getSenderName(context), this.mSubject, this.mMessage);
            }
            return this.mTicker;
        }

        private static final void conjuction(SpannableStringBuilder builder, CharSequence s0, CharSequence s1) {
            if (s0.length() > 0) {
                builder.append(s0);
                if (s1.length() > 0) {
                    builder.append(' ').append(s1);
                }
            } else if (s1.length() > 0) {
                builder.append(s1);
            }
        }

        public CharSequence formatMessage(Context context) {
            boolean hasMessage = false;
            SpannableStringBuilder strSpanBuilder = new SpannableStringBuilder();
            CharSequence attachmentTypeString = "";
            if (this.mAttachmentType > 0) {
                attachmentTypeString = MessagingNotification.getAttachmentTypeString(context, this.mAttachmentType);
            }
            String message = this.mMessage;
            if (message != null) {
                message = message.replaceAll("\\n\\s+", "\n");
            }
            boolean hasSubject = !TextUtils.isEmpty(this.mSubject);
            if (!TextUtils.isEmpty(message)) {
                hasMessage = true;
            }
            if (hasMessage && hasSubject) {
                strSpanBuilder.append(this.mSubject).append("\r\n");
                conjuction(strSpanBuilder, attachmentTypeString, message);
            } else if (hasMessage) {
                conjuction(strSpanBuilder, attachmentTypeString, message);
            } else if (hasSubject) {
                conjuction(strSpanBuilder, attachmentTypeString, this.mSubject);
            } else if (attachmentTypeString != null) {
                strSpanBuilder.append(attachmentTypeString);
            }
            return strSpanBuilder;
        }

        private static NotificationInfo createSmsNotifcationInfoFromCursor(Context context, Cursor cursor) {
            try {
                String address = cursor.getString(2);
                Contact contact = Contact.get(address, false);
                if (contact.getSendToVoicemail()) {
                    MLog.e("Mms_TX_NOTIFY", "don't notify Voicemail, skip this one");
                    return null;
                }
                NotificationInfo info = MessagingNotification.getNewMessageNotificationInfo(context, true, address, cursor.getString(4), null, cursor.getLong(0), cursor.getLong(1), null, contact, 0, cursor.getInt(5));
                info.setMsgId(cursor.getLong(6));
                return info;
            } catch (Exception e) {
                MLog.e("Mms_TX_NOTIFY", "MmsException createSmsNotifcationInfoFromCursor: ", (Throwable) e);
                return null;
            }
        }

        private static NotificationInfo createMmsNotifcationInfoFromCursor(Context context, Cursor cursor) {
            try {
                long msgId = cursor.getLong(2);
                Uri msgUri = Mms.CONTENT_URI.buildUpon().appendPath(Long.toString(msgId)).build();
                String address = AddressUtils.getFrom(context, msgUri);
                Contact contact = Contact.get(address, false);
                if (contact.getSendToVoicemail()) {
                    MLog.e("Mms_TX_NOTIFY", "don't notify Voicemail, skip this one");
                    return null;
                }
                String subject = MessageUtils.cleanseMmsSubject(context, MessagingNotification.getMmsSubject(cursor.getString(3), cursor.getInt(4)));
                long threadId = cursor.getLong(0);
                long timeMillis = cursor.getLong(1) * 1000;
                int subId = cursor.getInt(5);
                if (MLog.isLoggable("Mms_app", 2)) {
                    MLog.d("Mms_TX_NOTIFY", "addMmsNotificationInfos: count=" + cursor.getCount() + ", thread_id=" + threadId);
                }
                Bitmap bitmap = null;
                String messageBody = null;
                int attachmentType = 0;
                GenericPdu pdu = MessagingNotification.sPduPersister.load(msgUri);
                if (pdu != null && (pdu instanceof MultimediaMessagePdu)) {
                    SlideshowModel slideshow = SlideshowModel.createFromPduBody(context, ((MultimediaMessagePdu) pdu).getBody());
                    attachmentType = MessagingNotification.getAttachmentType(slideshow);
                    SlideModel firstSlide = slideshow.get(0);
                    if (firstSlide != null) {
                        if (MessagingNotification.sShowListImage && firstSlide.hasImage()) {
                            int maxDim = MessagingNotification.dp2Pixels(360);
                            bitmap = firstSlide.getImage().getBitmap(maxDim, maxDim);
                        }
                        if (firstSlide.hasText()) {
                            messageBody = firstSlide.getText().getText();
                        }
                    }
                }
                NotificationInfo info = MessagingNotification.getNewMessageNotificationInfo(context, false, address, messageBody, subject, threadId, timeMillis, bitmap, contact, attachmentType, subId);
                info.setMsgId(msgId);
                return info;
            } catch (MmsException e) {
                MLog.e("Mms_TX_NOTIFY", "MmsException: createMmsNotifcationInfoFromCursor", (Throwable) e);
                return null;
            } catch (Throwable e2) {
                MLog.e("Mms_TX_NOTIFY", "Exception: createMmsNotifcationInfoFromCursor: ", e2);
                return null;
            }
        }

        public long getNotificationId() {
            if (this.mNotificationId == 0) {
                return this.mMsgId;
            }
            return this.mNotificationId;
        }

        public void setNotificationId(long mNotificationId) {
            this.mNotificationId = mNotificationId;
        }
    }

    private static final class NotificationInfoComparator implements Comparator<NotificationInfo>, Serializable {
        private NotificationInfoComparator() {
        }

        public int compare(NotificationInfo info1, NotificationInfo info2) {
            return Long.signum(info2.getTime() - info1.getTime());
        }
    }

    static {
        sNotificationManagerCallExtral.putString("package_name", "com.android.mms");
    }

    private MessagingNotification() {
    }

    public static void init(Context context) {
        sScreenDensity = context.getResources().getDisplayMetrics().density;
        sPduPersister = PduPersister.getPduPersister(context);
        NotificationReceiver.getInst().registe(context);
        if (mRcsMsg != null) {
            mRcsMsg.initExt(context);
        }
        String notificationList = PreferenceManager.getDefaultSharedPreferences(context).getString("last_notification_id_list", "");
        if (!TextUtils.isEmpty(notificationList)) {
            String[] arrId = notificationList.split(",");
            try {
                ArrayList<Integer> notificationIdList = new ArrayList();
                for (String parseInt : arrId) {
                    notificationIdList.add(Integer.valueOf(Integer.parseInt(parseInt)));
                }
                mNotificationIdList.addAll(notificationIdList);
            } catch (Exception e) {
                MLog.e("Mms_TX_NOTIFY", "" + e.getMessage());
            }
        }
    }

    public static void saveNotificationIdListToPreference(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        ArrayList<Integer> notificationIdList = new ArrayList();
        notificationIdList.addAll(mNotificationIdList);
        if (notificationIdList.isEmpty()) {
            prefs.edit().putString("last_notification_id_list", "").apply();
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Integer id : notificationIdList) {
            sb.append(id).append(",");
        }
        sb.setLength(sb.length() - 1);
        prefs.edit().putString("last_notification_id_list", sb.toString()).apply();
    }

    public static void setCurrentlyDisplayedThreadId(long threadId) {
        synchronized (sCurrentlyDisplayedThreadLock) {
            sCurrentlyDisplayedThreadId = threadId;
            MLog.d("Mms_TX_NOTIFY", "setCurrentlyDisplayedThreadId: " + sCurrentlyDisplayedThreadId);
        }
        MmsPermReceiver.noticeCurrentThreadId(MmsApp.getApplication(), threadId);
    }

    public static void setCurrentlyDisplayedThreadIdWithoutNotice(long threadId) {
        synchronized (sCurrentlyDisplayedThreadLock) {
            sCurrentlyDisplayedThreadId = threadId;
            MLog.d("Mms_TX_NOTIFY", "setCurrentlyDisplayedThreadId: " + sCurrentlyDisplayedThreadId);
        }
    }

    public static final void addSmsNotificationInfos(Context context, Set<Long> threads, SortedSet<NotificationInfo> notificationSet) {
        Cursor cursor = null;
        cursor = SqliteWrapper.query(context, Sms.CONTENT_URI, SMS_STATUS_PROJECTION, "(type = 1 AND seen = 0)", null, "date desc");
        if (cursor == null) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }
        while (cursor.moveToNext()) {
            NotificationInfo info = NotificationInfo.createSmsNotifcationInfoFromCursor(context, cursor);
            if (info != null) {
                notificationSet.add(info);
                threads.add(Long.valueOf(info.mThreadId));
                synchronized (MessagingNotification.class) {
                    mNotificationMap.put(Integer.valueOf((int) info.mMsgId), Long.valueOf(info.mThreadId));
                    try {
                    } catch (Exception e) {
                        MLog.e("Mms_TX_NOTIFY", "add Notification info error", (Throwable) e);
                        if (cursor != null) {
                            cursor.close();
                        }
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    public static final void addMmsNotificationInfos(Context context, Set<Long> threads, SortedSet<NotificationInfo> notificationSet) {
        Cursor cursor = null;
        cursor = SqliteWrapper.query(context, Mms.CONTENT_URI, MMS_STATUS_PROJECTION, "(msg_box=1 AND seen=0 AND (m_type=130 OR m_type=132))", null, "date desc");
        if (cursor == null) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }
        while (cursor.moveToNext()) {
            NotificationInfo info = NotificationInfo.createMmsNotifcationInfoFromCursor(context, cursor);
            if (info != null) {
                notificationSet.add(info);
                threads.add(Long.valueOf(info.mThreadId));
                synchronized (MessagingNotification.class) {
                    mNotificationMap.put(Integer.valueOf((int) info.mMsgId), Long.valueOf(info.mThreadId));
                    try {
                    } catch (SQLiteException e) {
                        MLog.e("Mms_TX_NOTIFY", "MmsException loading uri: ", (Throwable) e);
                        if (cursor != null) {
                            cursor.close();
                        }
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    public static void blockingUpdateStatusMessage(Context context, boolean statusReportMessage, Uri messageUri) {
        if (!statusReportMessage || messageUri == null) {
            MLog.e("Mms_TX_NOTIFY", "blockingUpdateStatusMessage with error type");
        } else if (isMessageNotificationEnabled(context)) {
            MmsSmsDeliveryInfo delivery = MmsSmsDeliveryInfo.createFromUri(context, messageUri);
            if (delivery != null && (mHwCustMsg == null || mHwCustMsg.getEnableSmsDeliverToast())) {
                if (mHwCustMsg == null || !mHwCustMsg.getDisableSmsDeliverToastByCard()) {
                    delivery.deliver();
                }
            }
        }
    }

    public static void nonBlockingUpdateNewMessageIndicatorForWap(final Context context, final long newMsgThreadId, final boolean isBannerMode) {
        synchronized (sCurrentlyDisplayedThreadLock) {
            MLog.d("Mms_TX_NOTIFY", "nonBlockingUpdateNewMessageIndicatorForWap: newMsgThreadId: " + newMsgThreadId + " sCurrentlyDisplayedThreadId: " + sCurrentlyDisplayedThreadId);
        }
        ThreadEx.execute(new Runnable() {
            public void run() {
                MLog.w("Mms_TX_NOTIFY", "nonBlockingUpdateNewMessageIndicatorForWap: newMsgThreadId: %d " + newMsgThreadId);
                if (MessagingNotification.mRcsMsg == null || !MessagingNotification.mRcsMsg.isRcsSwitchOn()) {
                    NewMessageUpdater.getInst().checkAndUpdateNotificationsForWap(context, newMsgThreadId, isBannerMode, true, null);
                    return;
                }
                MLog.i("Mms_TX_NOTIFY", "Rcs checkAndUpdateNotifications.");
                MessagingNotification.mRcsMsg.checkAndUpdateNotifications(context, newMsgThreadId, isBannerMode, null);
            }

            public String toString() {
                return "MessagingNotification.nonBlockingUpdateNewMessageIndicatorForWap." + super.toString();
            }
        });
    }

    public static void nonBlockingUpdateNewMessageIndicator(final Context context, final long newMsgThreadId, final boolean isBannerMode) {
        synchronized (sCurrentlyDisplayedThreadLock) {
            MLog.d("Mms_TX_NOTIFY", "nonBlockingUpdateNewMessageIndicator: newMsgThreadId: " + newMsgThreadId + " sCurrentlyDisplayedThreadId: " + sCurrentlyDisplayedThreadId);
        }
        ThreadEx.execute(new Runnable() {
            public void run() {
                MLog.w("Mms_TX_NOTIFY", "blockingUpdateNewMessageIndicator: newMsgThreadId is : %d " + newMsgThreadId);
                if (MessagingNotification.mRcsMsg == null || !MessagingNotification.mRcsMsg.isRcsSwitchOn()) {
                    NewMessageUpdater.getInst().checkAndUpdateNotifications(context, newMsgThreadId, isBannerMode, true, null);
                    return;
                }
                MLog.i("Mms_TX_NOTIFY", "Rcs checkAndUpdateNotifications.");
                MessagingNotification.mRcsMsg.checkAndUpdateNotifications(context, newMsgThreadId, isBannerMode, null);
            }

            public String toString() {
                return "MessagingNotification.nonBlockingUpdateNewMessageIndicator." + super.toString();
            }
        });
    }

    public static void nonBlockingUpdateNewMessageIndicatorUri(Context context, long newMsgThreadId, boolean isBannerMode, boolean isFirstMessage, Uri uri) {
        synchronized (sCurrentlyDisplayedThreadLock) {
            MLog.d("Mms_TX_NOTIFY", "nonBlockingUpdateNewMessageIndicator: newMsgThreadId: " + newMsgThreadId + " sCurrentlyDisplayedThreadId: " + sCurrentlyDisplayedThreadId);
        }
        final long j = newMsgThreadId;
        final Context context2 = context;
        final boolean z = isBannerMode;
        final Uri uri2 = uri;
        final boolean z2 = isFirstMessage;
        ThreadEx.execute(new Runnable() {
            public void run() {
                MLog.w("Mms_TX_NOTIFY", "blockingUpdateNewMessageIndicator: newMsgThreadId: %d " + j);
                if (MessagingNotification.mRcsMsg != null && MessagingNotification.mRcsMsg.isRcsSwitchOn()) {
                    MLog.i("Mms_TX_NOTIFY", "Rcs checkAndUpdateNotifications.");
                    MessagingNotification.mRcsMsg.checkAndUpdateNotifications(context2, j, z, uri2);
                } else if (z2) {
                    NewMessageUpdater.getInst().checkAndUpdateNotifications(context2, j, z, true, uri2);
                } else {
                    NewMessageUpdater.getInst().checkAndUpdateNotifications(context2, j, z, true, true, uri2);
                }
            }

            public String toString() {
                return "MessagingNotification.nonBlockingUpdateNewMessageIndicator." + super.toString();
            }
        });
    }

    public static void blockingUpdateNewMessageIndicator(Context context, long newMsgThreadId, boolean isBannerMode) {
        MLog.e("Mms_TX_NOTIFY", "Eroor blockingUpdateNewMessageIndicator: newMsgThreadId: %d is not support Anymore", new Exception().fillInStackTrace());
    }

    public static void blockingUpdateNewMessageIndicator(Context context, long newMsgThreadId, Uri msgUri) {
        blockingUpdateNewSingleMessageIndicator(context, newMsgThreadId, msgUri, false);
    }

    static void blockingUpdateNewSingleMessageIndicator(Context context, long newMsgThreadId, Uri msgUri, boolean checked) {
        MLog.w("Mms_TX_NOTIFY", "blockingUpdateNewMessageIndicator with uri: newMsgThreadId: %d " + newMsgThreadId);
        if (mRcsMsg == null || !mRcsMsg.isRcsSwitchOn()) {
            NewMessageUpdater.getInst().updateNotifications(context, newMsgThreadId, msgUri, checked);
            return;
        }
        MLog.w("Mms_TX_NOTIFY", "updateNotifications in RCS " + holder);
        holder = getHolderInstance();
        mRcsMsg.setHwCustCallback(holder);
        mRcsMsg.updateNotificationsExt(context, newMsgThreadId, msgUri, checked, 1, null);
    }

    public static void blockingUpdateAllNotifications(Context context, long threadId) {
        nonBlockingUpdateNewMessageIndicator(context, threadId, false);
        nonBlockingUpdateSendFailedNotification(context);
        updateDownloadFailedNotification(context);
        MmsWidgetProvider.notifyDatasetChanged(context);
    }

    private static CharSequence getAttachmentTypeString(Context context, int attachmentType) {
        int id;
        switch (attachmentType) {
            case 1:
                id = R.string.attachment_picture;
                break;
            case 2:
                id = R.string.attachment_video;
                break;
            case 3:
                id = R.string.attachment_audio;
                break;
            case 4:
                id = R.string.attachment_slideshow;
                break;
            default:
                return "";
        }
        return "(" + context.getString(id) + ")";
    }

    private static int getAttachmentType(SlideshowModel slideshow) {
        int slideCount = slideshow.size();
        if (slideCount == 0) {
            return 0;
        }
        if (slideCount > 1) {
            return 4;
        }
        SlideModel slide = slideshow.get(0);
        if (slide.hasImage()) {
            return 1;
        }
        if (slide.hasVideo()) {
            return 2;
        }
        if (slide.hasAudio()) {
            return 3;
        }
        return 0;
    }

    private static final int dp2Pixels(int dip) {
        return (int) ((((float) dip) * sScreenDensity) + 0.5f);
    }

    private static final NotificationInfo getNewMessageNotificationInfo(Context context, boolean isSms, String address, String message, String subject, long threadId, long timeMillis, Bitmap attachmentBitmap, Contact contact, int attachmentType, int subId) {
        Intent clickIntent = ComposeMessageActivity.createIntent(context, threadId);
        clickIntent.setFlags(872415232);
        clickIntent.putExtra("received_flag", true);
        clickIntent.putExtra("fromNotification", true);
        clickIntent.putExtra("test_from_click", "this is a intent from click");
        setHwContactExtra(clickIntent, context, address, message, threadId);
        return new NotificationInfo(isSms, clickIntent, message, subject, null, timeMillis, address, attachmentBitmap, contact, attachmentType, threadId, subId);
    }

    private static void setHwContactExtra(Intent clickIntent, Context context, String address, String message, long threadId) {
        if (SmartArchiveSettingUtils.isHuaweiArchiveEnabled(context) && NumberUtils.isHwMessageNumber(address)) {
            if (TextUtils.isEmpty(Conversation.getFromNumberForHw(threadId))) {
                Conversation.updateHwSenderName(threadId, message);
            }
            String hwName = Conversation.getFromNumberForHw(threadId);
            clickIntent.putExtra("sender_for_huawei_message", hwName);
            NameMatchResult result = Contact.getNameMatchedContact(context, hwName);
            if (result != null) {
                clickIntent.putExtra("contact_id", result.contactId);
                clickIntent.putExtra("name", result.contactName);
            } else {
                clickIntent.putExtra("contact_id", 0);
            }
        }
    }

    public static void cancelAllNotification(Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService("notification");
        MLog.d("Mms_TX_NOTIFY", "cancelAllNotification");
        NotificationReceiver.getInst().clearNotifications(nm);
        nm.cancelAsUser(null, HwCustMessagingNotificationImpl.NOTIFICATION_CLASS_ZERO_ID, UserHandle.ALL);
        nm.cancelAsUser(null, 531, UserHandle.ALL);
        nm.cancelAsUser(null, 789, UserHandle.ALL);
        nm.cancelAsUser(null, 123, UserHandle.ALL);
        if (mNotificationIdList != null && mNotificationIdList.size() > 0) {
            while (true) {
                Integer notificationId = removeFromNotifiactionList(0);
                if (notificationId == null) {
                    break;
                }
                nm.cancelAsUser(null, notificationId.intValue(), UserHandle.ALL);
            }
            synchronized (MessagingNotification.class) {
                mNotificationMap.clear();
            }
        }
        saveNotificationIdListToPreference(context);
        nm.cancelAsUser(null, 1390, UserHandle.ALL);
    }

    public static void cancelNotification(Context context, int notificationId) {
        NotificationManager nm = (NotificationManager) context.getSystemService("notification");
        MLog.d("Mms_TX_NOTIFY", "cancelNotification");
        nm.cancelAsUser(null, notificationId, UserHandle.ALL);
        if (mRcsMsg != null && mRcsMsg.getReceiveTypeMap().size() > 0) {
            RcsMessagingNotification rcsMessagingNotification = mRcsMsg;
            RcsMessagingNotification.clearReceiveTypeMap();
        }
        if (123 != notificationId) {
            return;
        }
        if (OsUtil.isOwner()) {
            cancelNotificationOfOwner(context);
            return;
        }
        MLog.d("Mms_TX_NOTIFY", "cancelNotificationOfOwner action sent to owner now");
        context.sendBroadcastAsUser(MmsPermReceiver.getNotificationDeleteIntent(context), UserHandle.OWNER);
    }

    public static void cancelNotificationOfOwner(Context context) {
        MLog.d("Mms_TX_NOTIFY", "cancelNotificationOfOwner now");
        if (mNotificationIdList.isEmpty()) {
            MLog.d("Mms_TX_NOTIFY", "cancelNotificationOfOwner now, mNotificationIdList is empty");
            return;
        }
        NotificationManager nm = (NotificationManager) context.getSystemService("notification");
        ArrayList<Integer> notificationIdList = new ArrayList();
        notificationIdList.addAll(mNotificationIdList);
        for (int i = 0; i < notificationIdList.size(); i++) {
            if (notificationIdList.get(i) != null) {
                nm.cancelAsUser(null, ((Integer) notificationIdList.get(i)).intValue(), UserHandle.ALL);
            }
        }
        mNotificationIdList.clear();
        synchronized (MessagingNotification.class) {
            mNotificationMap.clear();
        }
        saveNotificationIdListToPreference(context);
    }

    private static void initSoundAndVibrateSettings(Context context, String number, NotificationCompat.Builder builder, int subId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (HwPreventModeHelper.isBlackListNumInPreventMode(number)) {
            builder.setVibrate(new long[]{0});
            MLog.w("Mms_TX_NOTIFY", "Keep silence for blackListNumber");
            return;
        }
        boolean vibrate;
        Uri ringtonUri = null;
        if (MmsApp.getDefaultTelephonyManager().getCallState() == 0) {
            String ringtoneStr;
            AudioManager audioManager = (AudioManager) context.getSystemService("audio");
            String vibrateKeyStr = "pref_key_vibrateWhen";
            String notificationRingtoneSpStr = "pref_key_ringtoneSp";
            if (MessageUtils.isMultiSimEnabled()) {
                if (subId == 0) {
                    vibrateKeyStr = "pref_key_vibrateWhen_sub0";
                    notificationRingtoneSpStr = "pref_key_ringtoneSp_sub0";
                } else {
                    vibrateKeyStr = "pref_key_vibrateWhen_sub1";
                    notificationRingtoneSpStr = "pref_key_ringtoneSp_sub1";
                }
            }
            if (audioManager.getRingerMode() == 2) {
                vibrate = sp.getBoolean(vibrateKeyStr, false);
                if (!OsUtil.isForgroundOwner()) {
                    ringtoneStr = HwMessageUtils.getSystemDefaultRingTone(context);
                } else if (MmsConfig.hasMmsRingtoneUri(context)) {
                    if (MessageUtils.isMultiSimEnabled()) {
                        ringtoneStr = HwMessageUtils.getRingtoneString(context, subId);
                    } else {
                        ringtoneStr = HwMessageUtils.getRingtoneString(context);
                    }
                } else if (MessageUtils.isMultiSimEnabled()) {
                    if (subId == 0) {
                        ringtoneStr = sp.getString("pref_key_ringtone_sub0", null);
                    } else {
                        ringtoneStr = sp.getString("pref_key_ringtone_sub1", null);
                    }
                    MmsConfig.setRingToneUriToDatabase(context, ringtoneStr, subId);
                } else {
                    ringtoneStr = sp.getString("pref_key_ringtone", null);
                    MmsConfig.setRingToneUriToDatabase(context, ringtoneStr);
                }
            } else if (audioManager.getRingerMode() == 1) {
                vibrate = sp.getBoolean(vibrateKeyStr, false);
                ringtoneStr = sp.getString(notificationRingtoneSpStr, null);
            } else {
                vibrate = false;
                ringtoneStr = sp.getString(notificationRingtoneSpStr, null);
            }
            ringtonUri = TextUtils.isEmpty(ringtoneStr) ? null : Uri.parse(ringtoneStr);
        } else if (MmsConfig.isRingWhentalk() && MmsApp.getDefaultTelephonyManager().getCallState() == 2) {
            ringtonUri = getMediaVolumeUri(context);
            vibrate = false;
        } else {
            vibrate = false;
        }
        StringBuilder logInfo = new StringBuilder("updateNotification: ");
        if (ringtonUri != null) {
            builder.setSound(ringtonUri);
            logInfo.append("Adding Sound, ringtonUri = ").append(ringtonUri);
        }
        if (vibrate) {
            builder.setVibrate(new long[]{100, 10, 100, 1000});
            logInfo.append(" vibrate ");
        } else {
            builder.setVibrate(new long[]{0});
        }
        MLog.d("Mms_TX_NOTIFY", logInfo.toString());
    }

    private static void initSoundAndVibrateSettings(Context context, String number, Builder builder, int subId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (HwPreventModeHelper.isBlackListNumInPreventMode(number)) {
            builder.setVibrate(new long[]{0});
            MLog.w("Mms_TX_NOTIFY", "Keep silence for blackListNumber");
            return;
        }
        boolean vibrate;
        Uri ringtonUri = null;
        if (MmsApp.getDefaultTelephonyManager().getCallState() == 0) {
            String ringtoneStr;
            AudioManager audioManager = (AudioManager) context.getSystemService("audio");
            String notificationRingtoneSpStr = "pref_key_ringtoneSp";
            String vibrateKeyStr = "pref_key_vibrateWhen";
            if (MessageUtils.isMultiSimEnabled()) {
                if (subId == 0) {
                    notificationRingtoneSpStr = "pref_key_ringtoneSp_sub0";
                    vibrateKeyStr = "pref_key_vibrateWhen_sub0";
                } else {
                    notificationRingtoneSpStr = "pref_key_ringtoneSp_sub1";
                    vibrateKeyStr = "pref_key_vibrateWhen_sub1";
                }
            }
            if (audioManager.getRingerMode() == 2) {
                vibrate = sp.getBoolean(vibrateKeyStr, false);
                if (!OsUtil.isForgroundOwner()) {
                    ringtoneStr = HwMessageUtils.getSystemDefaultRingTone(context);
                } else if (MmsConfig.hasMmsRingtoneUri(context)) {
                    if (MessageUtils.isMultiSimEnabled()) {
                        ringtoneStr = HwMessageUtils.getRingtoneString(context, subId);
                    } else {
                        ringtoneStr = HwMessageUtils.getRingtoneString(context);
                    }
                } else if (MessageUtils.isMultiSimEnabled()) {
                    if (subId != 0) {
                        ringtoneStr = sp.getString("pref_key_ringtone_sub1", null);
                    } else {
                        ringtoneStr = sp.getString("pref_key_ringtone_sub0", null);
                    }
                    MmsConfig.setRingToneUriToDatabase(context, ringtoneStr, subId);
                } else {
                    ringtoneStr = sp.getString("pref_key_ringtone", null);
                    MmsConfig.setRingToneUriToDatabase(context, ringtoneStr);
                }
            } else if (audioManager.getRingerMode() == 1) {
                ringtoneStr = sp.getString(notificationRingtoneSpStr, null);
                vibrate = sp.getBoolean(vibrateKeyStr, false);
            } else {
                ringtoneStr = sp.getString(notificationRingtoneSpStr, null);
                vibrate = false;
            }
            ringtonUri = TextUtils.isEmpty(ringtoneStr) ? null : Uri.parse(ringtoneStr);
        } else if (MmsConfig.isRingWhentalk() && MmsApp.getDefaultTelephonyManager().getCallState() == 2) {
            vibrate = false;
            ringtonUri = getMediaVolumeUri(context);
        } else {
            vibrate = false;
        }
        StringBuilder logInfo = new StringBuilder("updateNotification: ");
        if (ringtonUri != null) {
            builder.setSound(ringtonUri);
            logInfo.append("Adding Sound for guest,ringtonUri = ").append(ringtonUri);
        }
        if (vibrate) {
            builder.setVibrate(new long[]{100, 10, 100, 1000});
            logInfo.append(" vibrate ");
        } else {
            builder.setVibrate(new long[]{0});
        }
        MLog.d("Mms_TX_NOTIFY", logInfo.toString());
    }

    private static Bitmap getContactAvatar(Context context, Contact contact) {
        BitmapDrawable contactDrawable = (BitmapDrawable) contact.getAvatar(context, null);
        if (contactDrawable == null) {
            return null;
        }
        Bitmap srcBitmap = contactDrawable.getBitmap();
        if (srcBitmap == null) {
            return null;
        }
        Resources res = context.getResources();
        int idealIconHeight = res.getDimensionPixelSize(17104902);
        int idealIconWidth = res.getDimensionPixelSize(17104901);
        Bitmap avatar = srcBitmap;
        try {
            if (srcBitmap.getHeight() < idealIconHeight) {
                avatar = Bitmap.createScaledBitmap(srcBitmap, idealIconWidth, idealIconHeight, true);
            }
            if (avatar == srcBitmap) {
                avatar = ResEx.duplicateBitmap(srcBitmap, 255);
            }
            return avatar;
        } catch (Exception e) {
            MLog.e("Mms_TX_NOTIFY", e.getMessage());
            return null;
        } catch (OutOfMemoryError e2) {
            MLog.e("Mms_TX_NOTIFY", "updateNotification::get bitmap out of memmory: " + e2);
            return null;
        }
    }

    public static void enableHeadsup(Notification noti, boolean enabled) {
        Bundle extras = noti.extras;
        if (enabled) {
            extras.putInt("headsup", 1);
        } else {
            extras.putInt("headsup", 0);
        }
    }

    public static void enableHeadsUp(NotificationCompat.Builder builder, boolean enabled) {
        if (enabled) {
            builder.setPriority(1);
        } else {
            builder.setPriority(-1);
        }
    }

    public static void enableHeadsUp(Builder builder, boolean enabled) {
        if (enabled) {
            builder.setPriority(1);
        } else {
            builder.setPriority(-1);
        }
    }

    public static void setNotificationIcon(Context context, Contact c, Conversation conv, final Builder builder) {
        final Drawable defaultAvtar = getDefaultAvatar(context, c, conv);
        if (c == null) {
            builder.setLargeIcon(AvatarCache.drawableToBitmap(defaultAvtar));
            return;
        }
        boolean isNotificationSms = false;
        if (conv != null && conv.getPhoneType() == 2) {
            isNotificationSms = true;
        }
        setAvatarImage(context, c, defaultAvtar, new ItemLoadedCallback<Drawable>() {
            public void onItemLoaded(Drawable result, Throwable exception) {
                if (result != null) {
                    builder.setLargeIcon(AvatarCache.drawableToBitmap(result));
                } else {
                    builder.setLargeIcon(AvatarCache.drawableToBitmap(defaultAvtar));
                }
            }
        }, isNotificationSms);
    }

    public static Drawable getDefaultAvatar(Context context, Contact c, Conversation conv) {
        Drawable avatar = SmartSmsPublicinfoUtil.getDrawableFromCache(context, c);
        if (avatar != null) {
            return avatar;
        }
        Drawable defaultAvtar;
        int phoneType = -1;
        if (conv != null) {
            phoneType = conv.getPhoneType();
        }
        switch (phoneType) {
            case 1:
                defaultAvtar = ResEx.self().getAvtarDefault(c, -4);
                break;
            case 2:
                defaultAvtar = ResEx.self().getAvtarDefault(null, -2);
                break;
            case 3:
                defaultAvtar = ResEx.self().getAvtarDefault(c, -4);
                break;
            default:
                if (c == null || !c.isMe()) {
                    if (c != null && c.isXiaoyuanContact()) {
                        defaultAvtar = ResEx.self().getAvtarDefault(null, -2);
                        break;
                    }
                    defaultAvtar = ResEx.self().getAvtarDefault(c);
                    break;
                }
                defaultAvtar = ResEx.self().getAvtarDefault(c, -1);
                break;
                break;
        }
        return defaultAvtar;
    }

    public static void setNotificationIcon(Context context, Contact c, Conversation conv, final NotificationCompat.Builder builder) {
        final Drawable defaultAvtar = getDefaultAvatar(context, c, conv);
        if (c == null) {
            builder.setLargeIcon(AvatarCache.drawableToBitmap(defaultAvtar));
            return;
        }
        boolean isNotificationSms = false;
        if (conv != null && conv.getPhoneType() == 2) {
            isNotificationSms = true;
        }
        setAvatarImage(context, c, defaultAvtar, new ItemLoadedCallback<Drawable>() {
            public void onItemLoaded(Drawable result, Throwable exception) {
                if (result != null) {
                    builder.setLargeIcon(AvatarCache.drawableToBitmap(result));
                } else {
                    builder.setLargeIcon(AvatarCache.drawableToBitmap(defaultAvtar));
                }
            }
        }, isNotificationSms);
    }

    private static void setAvatarImage(Context context, Contact c, Drawable defaultAvtar, ItemLoadedCallback<Drawable> loadedCallback, boolean isNotificationSms) {
        if (c.isXyHwNumber()) {
            Drawable avatar = ResEx.self().getAvtarDefault(c, -4);
            if (!(avatar == null || loadedCallback == null)) {
                loadedCallback.onItemLoaded(avatar, null);
                return;
            }
        }
        c.parseAvatarImage(context, defaultAvtar, loadedCallback, false, true, isNotificationSms);
    }

    private static RemoteViews getRelpyView(Context context, Bitmap icon, String title, CharSequence content) {
        RemoteViews headsUpView = new RemoteViews(context.getPackageName(), R.layout.heads_up_view_reply);
        headsUpView.setImageViewBitmap(R.id.icon, icon);
        headsUpView.setTextViewText(R.id.title, title);
        headsUpView.setTextViewText(R.id.text, content);
        return headsUpView;
    }

    private static RemoteViews getBigView(Context context, Bitmap icon, String title, CharSequence content) {
        RemoteViews headsUpView = new RemoteViews(context.getPackageName(), R.layout.heads_up_view_big);
        headsUpView.setImageViewBitmap(R.id.icon, icon);
        headsUpView.setTextViewText(R.id.title, title);
        headsUpView.setTextViewText(R.id.text, content);
        return headsUpView;
    }

    private static int getNumberType(Context context, SortedSet<NotificationInfo> notificationSet) {
        int i = 2;
        if (notificationSet.size() == 0 || !SmartArchiveSettingUtils.isSmartArchiveEnabled(context)) {
            return 0;
        }
        int noticeCount = 0;
        int hwNoticeCount = 0;
        for (NotificationInfo info : notificationSet) {
            Conversation conv = Conversation.get(context, info.mThreadId, false);
            if (conv.getNumberType() == 2) {
                noticeCount++;
            } else if (conv.getNumberType() != 1) {
                return 0;
            } else {
                hwNoticeCount++;
            }
            if (noticeCount > 0 && hwNoticeCount > 0) {
                return 0;
            }
        }
        if (noticeCount <= 0) {
            i = 1;
        }
        return i;
    }

    private static void headsetTone(Context context) {
        Throwable th;
        ToneGenerator toneGenerator = null;
        AudioManager audioManager = (AudioManager) context.getSystemService("audio");
        try {
            ToneGenerator toneGenerator2 = new ToneGenerator(0, 80);
            try {
                audioManager.requestAudioFocus(null, AudioManagerEx.STREAM_FM, 2);
                toneGenerator2.startTone(22);
                Thread.sleep(500);
                toneGenerator2.stopTone();
                Thread.sleep(100);
                audioManager.abandonAudioFocus(null);
                if (toneGenerator2 != null) {
                    toneGenerator2.release();
                }
                toneGenerator = toneGenerator2;
            } catch (Exception e) {
                toneGenerator = toneGenerator2;
                if (toneGenerator != null) {
                    toneGenerator.release();
                }
            } catch (Throwable th2) {
                th = th2;
                toneGenerator = toneGenerator2;
                if (toneGenerator != null) {
                    toneGenerator.release();
                }
                throw th;
            }
        } catch (Exception e2) {
            if (toneGenerator != null) {
                toneGenerator.release();
            }
        } catch (Throwable th3) {
            th = th3;
            if (toneGenerator != null) {
                toneGenerator.release();
            }
            throw th;
        }
    }

    private static boolean bluetoothHeadset(Context context) {
        if (mHwCustMsg != null && !mHwCustMsg.enableSmsNotifyInSilentMode()) {
            return false;
        }
        AudioManager audioManager = (AudioManager) context.getSystemService("audio");
        return (audioManager.getRingerMode() == 1 || audioManager.getRingerMode() == 0) && (audioManager.isWiredHeadsetOn() || audioManager.isBluetoothA2dpOn());
    }

    private static final String getMessageSenderName(Context context, String address, Contact contact, long tId) {
        if (contact.existsInDatabase()) {
            return contact.getName();
        }
        if (SmartArchiveSettingUtils.isHuaweiArchiveEnabled(context) && NumberUtils.isHwMessageNumber(address)) {
            String hwName = Conversation.getFromNumberForHw(tId);
            if (!TextUtils.isEmpty(hwName)) {
                MLog.v("Mms_TX_NOTIFY", "Notification set huawei sender name");
                return hwName;
            }
        }
        return contact.getName();
    }

    private static String removeLineSeperator(String str) {
        return str == null ? "" : str.replace('\n', ' ').replace('\r', ' ');
    }

    public static CharSequence buildTickerMessage(Context context, String address, String subject, String body, int subId, long threadId) {
        return buildTickerMessage(context, getMessageSenderName(context, address, Contact.get(address, true), threadId), subject, body);
    }

    public static CharSequence buildTickerMessage(Context context, String displayAddress, String subject, String body) {
        int i;
        boolean hintAdded = false;
        body = mCryptoMsgNotification.buildTickerBodyForEncryptedSms(context, body);
        StringBuilder buf = new StringBuilder();
        if (!(TextUtils.isEmpty(body) && TextUtils.isEmpty(subject))) {
            buf.append('‪');
            buf.append(removeLineSeperator(displayAddress));
            buf.append('‬');
        }
        int offset = buf.length();
        if (!TextUtils.isEmpty(subject)) {
            if (null == null) {
                buf.append(':');
                hintAdded = true;
            }
            buf.append(' ');
            buf.append(removeLineSeperator(subject));
        }
        if (!TextUtils.isEmpty(body)) {
            if (!hintAdded) {
                buf.append(':');
                hintAdded = true;
            }
            buf.append(' ');
            buf.append(removeLineSeperator(body));
        }
        if (hintAdded) {
            i = 1;
        } else {
            i = 0;
        }
        offset += i;
        SpannableString spanText = new SpannableString(buf.toString());
        spanText.setSpan(new StyleSpan(1), 0, offset, 33);
        return spanText;
    }

    private static String getMmsSubject(String sub, int charset) {
        if (TextUtils.isEmpty(sub)) {
            return "";
        }
        return new EncodedStringValue(charset, PduPersister.getBytes(sub)).getString();
    }

    public static void notifyDownloadFailed(Context context, long threadId) {
        notifyFailed(context, true, threadId, false);
    }

    public static void notifySendFailed(Context context, boolean noisy) {
        notifyFailed(context, false, 0, noisy);
    }

    private static void notifyFailed(Context context, boolean isDownload, long threadId, boolean noisy) {
        if (isMessageNotificationEnabled(context)) {
            long[] jArr = new long[2];
            jArr = new long[]{0, 1};
            int totalFailedCount = getUndeliveredMessageCount(context, jArr);
            if (totalFailedCount != 0 || isDownload) {
                String title;
                String description;
                Intent failedIntent;
                boolean allFailedInSameThread = jArr[1] != 0 || (isDownload && threadId > 0);
                Builder builder = new Builder(context);
                enableHeadsUp(builder, false);
                Notification notification = builder.build();
                if (totalFailedCount <= 1 || isDownload) {
                    if (isDownload) {
                        title = context.getString(R.string.message_download_failed_title);
                    } else {
                        title = context.getString(R.string.message_send_failed_title);
                    }
                    if (isDownload) {
                        description = context.getString(R.string.message_download_failed_body);
                    } else {
                        description = context.getString(R.string.message_send_failed_body);
                    }
                } else {
                    description = context.getString(R.string.notification_failed_multiple, new Object[]{Integer.toString(totalFailedCount)});
                    if (mRcsMsg != null) {
                        description = mRcsMsg.getDescription(context, description, totalFailedCount);
                    }
                    title = context.getString(R.string.notification_failed_multiple_title);
                }
                if (allFailedInSameThread) {
                    failedIntent = new Intent(context, ComposeMessageActivity.class);
                    if (isDownload) {
                        failedIntent.putExtra("failed_download_flag", true);
                    } else {
                        threadId = jArr[0];
                        if (mRcsMsg != null) {
                            failedIntent = mRcsMsg.getNewIntentWithoutDownload(context, failedIntent, threadId);
                        }
                        failedIntent.putExtra("undelivered_flag", true);
                    }
                    if (mRcsMsg == null || !mRcsMsg.isRcsSwitchOn()) {
                        failedIntent.putExtra("thread_id", threadId);
                    }
                } else {
                    failedIntent = new Intent(context, ConversationList.class);
                    failedIntent.putExtra("undelivered_flag", true);
                }
                failedIntent.putExtra("fromNotification", true);
                PendingIntent pendingIntent = PendingIntent.getActivityAsUser(context, 0, failedIntent, 134217728, null, UserHandle.CURRENT);
                notification.icon = R.drawable.stat_notify_sms;
                if (notification.largeIcon == null) {
                    notification.largeIcon = AvatarCache.drawableToBitmap(getDefaultAvatar(context, null, null));
                }
                notification.tickerText = title;
                notification = setLatestEventInfo(context, notification, title, description, pendingIntent, false);
                if (noisy && MmsApp.getDefaultTelephonyManager().getCallState() == 0) {
                    if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_vibrate", false)) {
                        notification.defaults |= 2;
                    }
                    String ringtoneStr = HwMessageUtils.getRingtoneString(context);
                    notification.sound = TextUtils.isEmpty(ringtoneStr) ? null : Uri.parse(ringtoneStr);
                } else if (noisy && MmsConfig.isRingWhentalk() && MmsApp.getDefaultTelephonyManager().getCallState() == 2) {
                    notification.sound = getMediaVolumeUri(context);
                }
                NotificationManager notificationMgr = (NotificationManager) context.getSystemService("notification");
                if (isDownload) {
                    notificationMgr.notifyAsUser(null, 531, notification, UserHandle.CURRENT);
                } else {
                    notificationMgr.notifyAsUser(null, 789, notification, UserHandle.CURRENT);
                }
                if (mRcsMsg != null) {
                    mRcsMsg.clearData();
                }
                if (notification.largeIcon != null) {
                    notification.largeIcon.recycle();
                }
                return;
            }
            cancelNotification(context, 789);
        }
    }

    private static int getUndeliveredMessageCount(Context context, long[] threadIdResult) {
        Cursor cursor = null;
        int count = 0;
        if (mRcsMsg != null && mRcsMsg.isRcsSwitchOn()) {
            return mRcsMsg.getAllFailedMsgCount(context, threadIdResult);
        }
        try {
            cursor = SqliteWrapper.query(context, UNDELIVERED_URI, MMS_THREAD_ID_PROJECTION, "read=0", null, null);
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return 0;
            }
            count = cursor.getCount();
            if (threadIdResult != null && cursor.moveToFirst()) {
                threadIdResult[0] = cursor.getLong(0);
                if (threadIdResult.length >= 2) {
                    long firstId = threadIdResult[0];
                    while (cursor.moveToNext()) {
                        if (cursor.getLong(0) != firstId) {
                            firstId = 0;
                            break;
                        }
                    }
                    threadIdResult[1] = firstId;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return count;
        } catch (Exception e) {
            MLog.e("Mms_TX_NOTIFY", "getUndeliveredMessageCount occur exception: " + e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static void nonBlockingUpdateSendFailedNotification(final Context context) {
        MLog.d("Mms_TX_NOTIFY", "nonBlockingUpdateSendFailedNotification");
        ThreadEx.execute(new Runnable() {
            public void run() {
                MessagingNotification.notifyFailed(context, false, 0, false);
            }

            public String toString() {
                return "MessagingNotification.nonBlockingUpdateSendFailedNotification." + super.toString();
            }
        });
    }

    public static void updateSendFailedNotificationForThread(Context context, long threadId) {
        long[] msgThreadId = new long[]{0, 0};
        if (getUndeliveredMessageCount(context, msgThreadId) > 0 && msgThreadId[0] == threadId && msgThreadId[1] != 0) {
            cancelNotification(context, 789);
        }
    }

    private static int getDownloadFailedMessageCount(Context context) {
        Cursor c = SqliteWrapper.query(context, context.getContentResolver(), Inbox.CONTENT_URI, null, "m_type=" + String.valueOf(130) + " AND " + "st" + "=" + String.valueOf(135), null, null);
        if (c == null) {
            return 0;
        }
        int count = c.getCount();
        c.close();
        return count;
    }

    public static void updateDownloadFailedNotification(Context context) {
        if (getDownloadFailedMessageCount(context) < 1) {
            cancelNotification(context, 531);
        }
    }

    public static boolean isFailedToDeliver(Intent intent) {
        return intent != null ? intent.getBooleanExtra("undelivered_flag", false) : false;
    }

    public static boolean isFailedToDownload(Intent intent) {
        return intent != null ? intent.getBooleanExtra("failed_download_flag", false) : false;
    }

    public static long getSmsThreadId(Context context, Uri uri) {
        if (context == null || uri == null) {
            MLog.d("Mms_TX_NOTIFY", "Error in getSmsThreadId, context is null or uri is null");
            return -2;
        }
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(context, uri, SMS_THREAD_ID_PROJECTION, null, null, null);
            if (cursor == null) {
                MLog.d("Mms_TX_NOTIFY", "getSmsThreadId uri: " + uri + " NULL cursor! returning THREAD_NONE");
                return -2;
            } else if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("thread_id");
                if (columnIndex < 0) {
                    MLog.d("Mms_TX_NOTIFY", "getSmsThreadId uri: " + uri + " Couldn't read row 0, col -1! returning THREAD_NONE");
                    if (cursor != null) {
                        cursor.close();
                    }
                    return -2;
                }
                long threadId = cursor.getLong(columnIndex);
                MLog.d("Mms_TX_NOTIFY", "getSmsThreadId uri: " + uri + " returning threadId: " + threadId);
                if (cursor != null) {
                    cursor.close();
                }
                return threadId;
            } else {
                MLog.d("Mms_TX_NOTIFY", "getSmsThreadId uri: " + uri + " NULL cursor! returning THREAD_NONE");
                if (cursor != null) {
                    cursor.close();
                }
                return -2;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static long getThreadId(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(context, context.getContentResolver(), uri, MMS_THREAD_ID_PROJECTION, null, null, null);
            if (cursor == null) {
                MLog.d("Mms_TX_NOTIFY", "getThreadId uri: " + uri + " NULL cursor! returning THREAD_NONE");
                return -2;
            } else if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("thread_id");
                if (columnIndex < 0) {
                    MLog.d("Mms_TX_NOTIFY", "getThreadId uri: " + uri + " Couldn't read row 0, col -1! returning THREAD_NONE");
                    if (cursor != null) {
                        cursor.close();
                    }
                    return -2;
                }
                long threadId = cursor.getLong(columnIndex);
                MLog.d("Mms_TX_NOTIFY", "getThreadId uri: " + uri + " returning threadId: " + threadId);
                if (cursor != null) {
                    cursor.close();
                }
                return threadId;
            } else {
                if (cursor != null) {
                    cursor.close();
                }
                return -2;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static boolean isUriAvalible(Context context, String strUri) {
        Uri ringtoneUri = Uri.parse(strUri);
        boolean isFind = false;
        AssetFileDescriptor assetFileDescriptor = null;
        try {
            assetFileDescriptor = context.getContentResolver().openAssetFileDescriptor(ringtoneUri, "r");
            if (!(assetFileDescriptor == null || assetFileDescriptor.getLength() == -1)) {
                isFind = true;
            }
            if (assetFileDescriptor != null) {
                try {
                    assetFileDescriptor.close();
                } catch (IOException e) {
                }
            }
        } catch (Exception e2) {
            MLog.e("Mms_TX_NOTIFY", "Failed to open ringtone " + ringtoneUri);
            if (assetFileDescriptor != null) {
                try {
                    assetFileDescriptor.close();
                } catch (IOException e3) {
                }
            }
        } catch (Throwable th) {
            if (assetFileDescriptor != null) {
                try {
                    assetFileDescriptor.close();
                } catch (IOException e4) {
                }
            }
        }
        return isFind;
    }

    public static String getUriByPath(Context context, String path) {
        String rr;
        Uri ROOT_INTERNAL = Media.INTERNAL_CONTENT_URI;
        Uri ROOT_EXTERNAL = Media.EXTERNAL_CONTENT_URI;
        String CATALOG_SELECTION = "_data = ? ";
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(ROOT_INTERNAL, new String[]{"_id"}, "_data = ? ", new String[]{path}, null);
            if (cursor != null && cursor.moveToFirst()) {
                rr = Media.INTERNAL_CONTENT_URI.toString();
                if (cursor.getInt(0) > 0) {
                    rr = rr + "/" + cursor.getInt(0);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return rr;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException e) {
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        try {
            Uri uri = ROOT_EXTERNAL;
            cursor = context.getContentResolver().query(uri, new String[]{"_id"}, "_data = ? ", new String[]{path}, null);
            if (cursor != null && cursor.moveToFirst()) {
                rr = Media.EXTERNAL_CONTENT_URI.toString();
                if (cursor.getInt(0) > 0) {
                    rr = rr + "/" + cursor.getInt(0);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return rr;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException e2) {
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th2) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private static Uri getMediaVolumeUri(Context context) {
        return Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.arrived);
    }

    public static void sendClassZeroMessageNotification(Context context, Intent smsIntent) {
        SmsMessage[] msgs = Intents.getMessagesFromIntent(smsIntent);
        String sendNumber = msgs[0].getDisplayOriginatingAddress();
        String message = msgs[0].getDisplayMessageBody();
        String senderName = getMessageSenderName(context, sendNumber, Contact.get(sendNumber, true), 0);
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setFlags(536870912);
        PendingIntent pendingIntent = PendingIntent.getActivityAsUser(context, 0, intent, 134217728, null, UserHandle.CURRENT);
        Builder nBuilder = new Builder(context);
        nBuilder.setSmallIcon(R.drawable.stat_notify_sms).setContentIntent(pendingIntent).setContentText(message).setContentTitle(senderName).setAutoCancel(true).setLargeIcon(AvatarCache.drawableToBitmap(getDefaultAvatar(context, null, null)));
        if (!TextUtils.isEmpty(sendNumber)) {
            initSoundAndVibrateSettings(context, sendNumber, nBuilder, MessageUtils.getSubId(msgs[0]));
        }
        enableHeadsUp(nBuilder, false);
        ((NotificationManager) context.getSystemService("notification")).notifyAsUser(null, HwCustMessagingNotificationImpl.NOTIFICATION_CLASS_ZERO_ID, nBuilder.build(), UserHandle.CURRENT);
    }

    public static final Bitmap getSmsAppBitmap(Context context) {
        int size = context.getResources().getDimensionPixelSize(R.dimen.notification_large_icon_width);
        return ResEx.getBitMapFromDrawable(ResEx.getSmsAppIcon(context), size, size);
    }

    private static Notification setLatestEventInfo(Context context, Notification notification, CharSequence contentTitle, CharSequence contentText, PendingIntent contentIntent, boolean enableHeadsUp) {
        Builder builder = new Builder(context);
        builder.setWhen(notification.when);
        builder.setSmallIcon(notification.icon);
        builder.setPriority(notification.priority);
        builder.setTicker(notification.tickerText);
        builder.setNumber(notification.number);
        builder.setSound(notification.sound, notification.audioStreamType);
        builder.setDefaults(notification.defaults);
        builder.setVibrate(notification.vibrate);
        if (notification.largeIcon != null) {
            builder.setLargeIcon(notification.largeIcon);
        }
        if (contentTitle != null) {
            builder.setContentTitle(contentTitle);
        }
        if (contentText != null) {
            builder.setContentText(contentText);
        }
        builder.setContentIntent(contentIntent);
        enableHeadsUp(builder, enableHeadsUp);
        return builder.getNotification();
    }

    public static RcsMessagingNotification getRcsMessagingNotification() {
        return mRcsMsg;
    }

    public static void setRcsCurrentlyDisplayedThreadId(long threadId, long rcsType) {
        if (mRcsMsg != null) {
            mRcsMsg.setCurrentlyDisplayedThreadId(threadId, rcsType);
        }
    }

    public static Object getCurrentlyDisplayedThreadLock() {
        return sCurrentlyDisplayedThreadLock;
    }

    public static long getCurrentlyDisplayedThreadId() {
        long j;
        synchronized (sCurrentlyDisplayedThreadLock) {
            j = sCurrentlyDisplayedThreadId;
        }
        return j;
    }

    private static boolean isMmsBannerEnabled(Context context) {
        if (OsUtil.isForgroundOwner()) {
            return isMmsNotificationBannerEnabled(context);
        }
        return false;
    }

    public static boolean isMessageNotificationEnabled(Context context) {
        if (OsUtil.isAtLeastL() && OsUtil.isSmsDisabledForLoginUser(context)) {
            return false;
        }
        return true;
    }

    private static boolean isMmsNotificationBannerEnabled(Context context) {
        boolean ret = true;
        try {
            INotificationManager nm = Stub.asInterface(ServiceManager.getService("notification"));
            ret = true;
            MLog.d("Mms_TX_NOTIFY", "Notification banner enabled = " + true);
            return true;
        } catch (SecurityException e) {
            MLog.e("Mms_TX_NOTIFY", "Can't get NotificationBanner Permission denied", (Throwable) e);
            return ret;
        }
    }

    public static HwCustMessagingNotificationHolder getHolderInstance() {
        if (holder == null) {
            holder = new HwCustMessagingNotificationHolder();
        }
        return holder;
    }

    private static void showSummaryNotification(int messageCount, Context context, String title, String description, PendingIntent pendingIntent, PendingIntent deleteIntent, String number, CharSequence ticker, int subId) {
        if (messageCount > 1) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context).setContentTitle(title).setContentText(description).setSmallIcon(R.drawable.stat_notify_sms).setStyle(new InboxStyle().addLine(description).setBigContentTitle(title)).setGroup("group_key_mms").setGroupSummary(true).setAutoCancel(true).setContentIntent(pendingIntent).setDeleteIntent(deleteIntent).setPriority(6);
            if (!TextUtils.isEmpty(ticker)) {
                nBuilder.setTicker(ticker);
            }
            if (!TextUtils.isEmpty(number)) {
                initSoundAndVibrateSettings(context, number, nBuilder, subId);
            }
            enableHeadsUp(nBuilder, false);
            setDefaultLight(nBuilder, number);
            notificationManager.notify(123, nBuilder.build());
        }
    }

    public static void updateSummaryNotification(Context context) {
        if (getMsgGroupNotificationCount(context) == 1) {
            NotificationManagerCompat.from(context).cancel(123);
        }
    }

    public static int getMsgGroupNotificationCount(Context context) {
        int iNotificationCount = 0;
        for (StatusBarNotification notification : ((NotificationManager) context.getSystemService("notification")).getActiveNotifications()) {
            String groupkey = notification.getNotification().getGroup();
            if (groupkey != null && groupkey.equals("group_key_mms")) {
                iNotificationCount++;
            }
        }
        return iNotificationCount;
    }

    public static void addToNotiticationList(Integer notificationId) {
        mNotificationIdList.add(notificationId);
    }

    public static Integer removeFromNotifiactionList(int index) {
        if (index < 0 || mNotificationIdList.size() <= 0 || index >= mNotificationIdList.size()) {
            return null;
        }
        Integer nId = (Integer) mNotificationIdList.remove(index);
        removeFromNotificationMap(nId.intValue());
        return nId;
    }

    public static void removeFromNotifiactionList(Integer value) {
        if (mNotificationIdList.size() > 0) {
            mNotificationIdList.remove(value);
            removeFromNotificationMap(value.intValue());
        }
    }

    private static void setDefaultLight(Object nBuilder, String number) {
        if (HwPreventModeHelper.isBlackListNumInPreventMode(number)) {
            MLog.w("Mms_TX_NOTIFY", "Keep silence for blackListNumber");
            return;
        }
        if (nBuilder instanceof NotificationCompat.Builder) {
            ((NotificationCompat.Builder) nBuilder).setDefaults(4);
        } else if (nBuilder instanceof Builder) {
            ((Builder) nBuilder).setDefaults(4);
        }
    }

    public static void clearAllNotificationInThread(Context context, long threadId) {
        if (context == null) {
            context = MmsApp.getApplication().getApplicationContext();
            if (context == null) {
                return;
            }
        }
        NotificationManager nm = (NotificationManager) context.getSystemService("notification");
        synchronized (MessagingNotification.class) {
            ArrayList<Integer> removeList = new ArrayList();
            for (Entry<Integer, Long> entry : mNotificationMap.entrySet()) {
                if (((Long) entry.getValue()).longValue() == threadId) {
                    nm.cancel(((Integer) entry.getKey()).intValue());
                    removeList.add((Integer) entry.getKey());
                }
            }
            for (Integer notificationId : removeList) {
                removeFromNotificationMap(notificationId.intValue());
            }
            if (mNotificationMap.size() <= 0) {
                nm.cancel(123);
            }
        }
    }

    public static void removeFromNotificationMap(int notificationId) {
        synchronized (MessagingNotification.class) {
            mNotificationMap.remove(Integer.valueOf(notificationId));
        }
    }

    public static boolean isUnreadMsg(long mTId) {
        boolean containsValue;
        synchronized (MessagingNotification.class) {
            containsValue = mNotificationMap.containsValue(Long.valueOf(mTId));
        }
        return containsValue;
    }

    public static void addNotificationMap(Uri msgUri, long newMsgThreadId) {
        if (msgUri != null && mNotificationMap != null) {
            long msgId = ContentUris.parseId(msgUri);
            synchronized (MessagingNotification.class) {
                mNotificationMap.put(Integer.valueOf((int) msgId), Long.valueOf(newMsgThreadId));
            }
        }
    }
}
