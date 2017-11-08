package com.android.contacts.calllog;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;
import android.provider.CallLog.Calls;
import android.text.TextUtils;
import com.android.common.io.MoreCloseables;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import com.google.common.collect.Maps;
import java.util.Map;

public class DefaultVoicemailNotifier {
    public static final boolean IS_FT_OPERATOR = SystemProperties.get("ro.config.hw_opta", "0").equals("109");
    private static DefaultVoicemailNotifier sInstance;
    private final Context mContext;
    private final NameLookupQuery mNameLookupQuery;
    private final NewCallsQuery mNewCallsQuery;
    private final NotificationManager mNotificationManager;
    private final PhoneNumberHelper mPhoneNumberHelper;

    public interface NameLookupQuery {
        String query(String str);
    }

    private static final class DefaultNameLookupQuery implements NameLookupQuery {
        private static final String[] PROJECTION = new String[]{"display_name", "lookup", "_id"};
        private final ContentResolver mContentResolver;

        private DefaultNameLookupQuery(ContentResolver contentResolver) {
            this.mContentResolver = contentResolver;
        }

        public String query(String number) {
            Cursor cursor = null;
            String nameLookup = null;
            try {
                cursor = this.mContentResolver.query(QueryUtil.getPhoneLookupUri(Uri.encode(number)), PROJECTION, null, null, null);
                if (cursor == null || !cursor.moveToFirst()) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return null;
                }
                nameLookup = cursor.getString(0);
                if (cursor != null) {
                    cursor.close();
                }
                return nameLookup;
            } catch (Exception e) {
                HwLog.d("DefaultVoicemailNotifier", "DefaultNameLookupQuery query e : " + e.getMessage());
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

    public interface NewCallsQuery {
        NewCall[] query();
    }

    private static final class DefaultNewCallsQuery implements NewCallsQuery {
        private static final String[] PROJECTION = new String[]{"_id", "number", "voicemail_uri", "presentation", "date", "formatted_number"};
        private final ContentResolver mContentResolver;

        private DefaultNewCallsQuery(ContentResolver contentResolver) {
            this.mContentResolver = contentResolver;
        }

        public NewCall[] query() {
            Cursor cursor = null;
            try {
                cursor = this.mContentResolver.query(QueryUtil.getCallsContentUri(), PROJECTION, String.format("%s = 1 AND %s = ? AND is_read = 0", new Object[]{"new", "type"}), new String[]{Integer.toString(4)}, "date DESC");
                if (cursor == null) {
                    return null;
                }
                if (cursor.getCount() > 0) {
                    NewCall[] newCalls = new NewCall[1];
                    if (cursor.moveToNext()) {
                        newCalls[0] = createNewCallsFromCursor(cursor);
                    }
                    if (cursor != null) {
                        MoreCloseables.closeQuietly(cursor);
                    }
                    return newCalls;
                }
                NewCall[] newCallArr = new NewCall[0];
                if (cursor != null) {
                    MoreCloseables.closeQuietly(cursor);
                }
                return newCallArr;
            } finally {
                if (cursor != null) {
                    MoreCloseables.closeQuietly(cursor);
                }
            }
        }

        private NewCall createNewCallsFromCursor(Cursor cursor) {
            String voicemailUriString = cursor.getString(2);
            return new NewCall(voicemailUriString == null ? null : Uri.parse(voicemailUriString), cursor.getString(1), cursor.getInt(3), cursor.getInt(0));
        }
    }

    private static final class NewCall {
        public final int callId;
        public final int numPresentation;
        public final String number;
        public final Uri voicemailUri;

        public NewCall(Uri voicemailUri, String number, int presentation, int callId) {
            this.voicemailUri = voicemailUri;
            this.number = number;
            this.numPresentation = presentation;
            this.callId = callId;
        }
    }

    public static synchronized DefaultVoicemailNotifier getInstance(Context context) {
        DefaultVoicemailNotifier defaultVoicemailNotifier;
        synchronized (DefaultVoicemailNotifier.class) {
            if (sInstance == null) {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
                ContentResolver contentResolver = context.getContentResolver();
                sInstance = new DefaultVoicemailNotifier(context, notificationManager, createNewCallsQuery(contentResolver), createNameLookupQuery(contentResolver), createPhoneNumberHelper(context));
            }
            defaultVoicemailNotifier = sInstance;
        }
        return defaultVoicemailNotifier;
    }

    private DefaultVoicemailNotifier(Context context, NotificationManager notificationManager, NewCallsQuery newCallsQuery, NameLookupQuery nameLookupQuery, PhoneNumberHelper phoneNumberHelper) {
        this.mContext = context;
        this.mNotificationManager = notificationManager;
        this.mNewCallsQuery = newCallsQuery;
        this.mNameLookupQuery = nameLookupQuery;
        this.mPhoneNumberHelper = phoneNumberHelper;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateNotification(Uri newCallUri) {
        NewCall[] newCalls = this.mNewCallsQuery.query();
        if (newCalls != null && newCalls.length != 0) {
            int i;
            Intent contentIntent;
            Resources resources = this.mContext.getResources();
            CharSequence callers = null;
            Map<String, String> names = Maps.newHashMap();
            NewCall callToNotify = null;
            for (NewCall newCall : newCalls) {
                if (((String) names.get(newCall.number)) == null) {
                    String name;
                    String nameLookup = this.mNameLookupQuery.query(newCall.number);
                    if (nameLookup == null) {
                        name = null;
                    } else {
                        name = nameLookup;
                    }
                    if (name == null) {
                        name = this.mPhoneNumberHelper.getDisplayNumber(newCall.number, newCall.numPresentation, "", "").toString();
                        if (TextUtils.isEmpty(name)) {
                            name = newCall.number;
                        }
                    }
                    names.put(newCall.number, name);
                    if (TextUtils.isEmpty(callers)) {
                        callers = name;
                    } else {
                        callers = resources.getString(R.string.notification_voicemail_callers_list, new Object[]{callers, name});
                    }
                }
                if (newCallUri != null) {
                }
                if (IS_FT_OPERATOR) {
                    if (!isUriEquals(newCallUri, newCall.voicemailUri)) {
                    }
                    callToNotify = newCall;
                }
            }
            if (newCallUri != null && callToNotify == null) {
                HwLog.e("DefaultVoicemailNotifier", "The new call could not be found in the call log: " + newCallUri);
            }
            String title = resources.getQuantityString(R.plurals.notification_voicemail_title, newCalls.length, new Object[]{Integer.valueOf(newCalls.length)});
            CommonUtilMethods.constructAndSendVvmSummaryNotification(this.mContext, title);
            Builder contentText = new Builder(this.mContext).setSmallIcon(CommonUtilMethods.getBitampIcon(this.mContext, R.drawable.ic_notification_voicemail)).setContentTitle(callers).setContentText(title);
            if (callToNotify != null) {
                i = -1;
            } else {
                i = 0;
            }
            Builder notificationBuilder = contentText.setDefaults(i).setDeleteIntent(createMarkNewVoicemailsAsOldIntent()).setGroup("group_key_contacts_vvm").setAppName(this.mContext.getString(R.string.voicemail)).setShowWhen(true).setGroupSummary(false).setAutoCancel(true);
            if (newCalls.length == 1) {
                contentIntent = new Intent("android.intent.action.VIEW");
                contentIntent.setData(newCalls[0].voicemailUri);
                contentIntent.putExtra("EXTRA_VOICEMAIL_CALL_ID", newCalls[0].callId);
                contentIntent.putExtra("EXTRA_FROM_NOTIFICATION", true);
            } else {
                contentIntent = new Intent("android.intent.action.VIEW", Calls.CONTENT_URI);
            }
            notificationBuilder.setContentIntent(PendingIntent.getActivity(this.mContext, 0, contentIntent, 134217728));
            if (callToNotify != null) {
                notificationBuilder.setTicker(resources.getString(R.string.notification_new_voicemail_ticker, new Object[]{names.get(callToNotify.number)}));
            }
            this.mNotificationManager.notify("DefaultVoicemailNotifier", newCalls[0].callId, notificationBuilder.build());
        }
    }

    private boolean isUriEquals(Uri newCallUri, Uri voicemailUri) {
        if (newCallUri == null || voicemailUri == null || newCallUri.getAuthority() == null || !newCallUri.getAuthority().equals(voicemailUri.getAuthority()) || newCallUri.getScheme() == null || !newCallUri.getScheme().equals(voicemailUri.getScheme()) || newCallUri.getPath() == null || !newCallUri.getPath().equals(voicemailUri.getPath())) {
            return false;
        }
        return true;
    }

    private PendingIntent createMarkNewVoicemailsAsOldIntent() {
        Intent intent = new Intent(this.mContext, CallLogNotificationsService.class);
        intent.setAction("com.android.contacts.calllog.ACTION_MARK_NEW_VOICEMAILS_AS_OLD");
        return PendingIntent.getService(this.mContext, 0, intent, 0);
    }

    public void clearAllNotification() {
        this.mNotificationManager.cancel("contact_summary_notification_tag", 101);
    }

    public static NewCallsQuery createNewCallsQuery(ContentResolver contentResolver) {
        return new DefaultNewCallsQuery(contentResolver);
    }

    public static NameLookupQuery createNameLookupQuery(ContentResolver contentResolver) {
        return new DefaultNameLookupQuery(contentResolver);
    }

    public static PhoneNumberHelper createPhoneNumberHelper(Context context) {
        return new PhoneNumberHelper(context.getResources());
    }
}
