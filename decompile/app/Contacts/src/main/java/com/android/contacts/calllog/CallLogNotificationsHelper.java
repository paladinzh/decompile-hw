package com.android.contacts.calllog;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog.Calls;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import com.android.contacts.GeoUtil;
import com.google.android.gms.R;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CallLogNotificationsHelper {
    private static final Set<String> LEGACY_UNKNOWN_NUMBERS = Sets.newHashSet("-1", "-2", "-3");
    private static volatile CallLogNotificationsHelper sInstance;
    private final ContactInfoHelper mContactInfoHelper;
    private final Context mContext;
    private final String mCurrentCountryIso;
    private final NewCallsQuery mNewCallsQuery;

    public interface NewCallsQuery {
        @Nullable
        List<NewCall> query(int i);
    }

    private static final class DefaultNewCallsQuery implements NewCallsQuery {
        private static final String[] PROJECTION = new String[]{"_id", "number", "voicemail_uri", "presentation", "subscription_component_name", "subscription_id", "transcription", "countryiso", "date"};
        private final ContentResolver mContentResolver;
        private final Context mContext;

        private DefaultNewCallsQuery(Context context, ContentResolver contentResolver) {
            this.mContext = context;
            this.mContentResolver = contentResolver;
        }

        @Nullable
        public List<NewCall> query(int type) {
            Cursor cursor;
            Throwable th;
            Throwable th2;
            if (CallLogNotificationsHelper.hasPermission(this.mContext, "android.permission.READ_CALL_LOG")) {
                cursor = null;
                try {
                    cursor = this.mContentResolver.query(Calls.CONTENT_URI_WITH_VOICEMAIL, PROJECTION, String.format("%s = 1 AND %s = ?", new Object[]{"new", "type"}), new String[]{Integer.toString(type)}, "date DESC");
                    if (cursor == null) {
                        if (cursor != null) {
                            try {
                                cursor.close();
                            } catch (Throwable th3) {
                                th = th3;
                            }
                        }
                        th = null;
                        if (th == null) {
                            return null;
                        }
                        try {
                            throw th;
                        } catch (RuntimeException e) {
                            Log.w("CallLogNotifHelper", "Exception when querying Contacts Provider for calls lookup");
                            return null;
                        }
                    }
                    List<NewCall> newCalls = new ArrayList();
                    while (cursor.moveToNext()) {
                        newCalls.add(createNewCallsFromCursor(cursor));
                    }
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Throwable th4) {
                            th = th4;
                        }
                    }
                    th = null;
                    if (th == null) {
                        return newCalls;
                    }
                    throw th;
                } catch (Throwable th22) {
                    Throwable th5 = th22;
                    th22 = th;
                    th = th5;
                }
            } else {
                Log.w("CallLogNotifHelper", "No READ_CALL_LOG permission, returning null for calls lookup.");
                return null;
            }
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Throwable th6) {
                    if (th22 == null) {
                        th22 = th6;
                    } else if (th22 != th6) {
                        th22.addSuppressed(th6);
                    }
                }
            }
            if (th22 != null) {
                throw th22;
            } else {
                throw th;
            }
        }

        private NewCall createNewCallsFromCursor(Cursor cursor) {
            return new NewCall(cursor.getString(1), cursor.getInt(3), cursor.getString(7), cursor.getLong(8));
        }
    }

    public static final class NewCall {
        public final String countryIso;
        public final long dateMs;
        public final String number;
        public final int numberPresentation;

        public NewCall(String number, int numberPresentation, String countryIso, long dateMs) {
            this.number = number;
            this.numberPresentation = numberPresentation;
            this.countryIso = countryIso;
            this.dateMs = dateMs;
        }
    }

    public static CallLogNotificationsHelper getInstance(Context context) {
        if (sInstance == null) {
            ContentResolver contentResolver = context.getContentResolver();
            String countryIso = GeoUtil.getCurrentCountryIso(context);
            sInstance = new CallLogNotificationsHelper(context, createNewCallsQuery(context, contentResolver), new ContactInfoHelper(context, countryIso), countryIso);
        }
        return sInstance;
    }

    CallLogNotificationsHelper(Context context, NewCallsQuery newCallsQuery, ContactInfoHelper contactInfoHelper, String countryIso) {
        this.mContext = context;
        this.mNewCallsQuery = newCallsQuery;
        this.mContactInfoHelper = contactInfoHelper;
        this.mCurrentCountryIso = countryIso;
    }

    @Nullable
    public List<NewCall> getNewMissedCalls() {
        return this.mNewCallsQuery.query(3);
    }

    private CharSequence getDisplayName(Context context, CharSequence number, int presentation) {
        if (presentation == 3) {
            return context.getResources().getString(R.string.unknown);
        }
        if (presentation == 2) {
            return context.getResources().getString(R.string.private_num);
        }
        if (presentation == 4) {
            return context.getResources().getString(R.string.payphone);
        }
        if (isLegacyUnknownNumbers(number)) {
            return context.getResources().getString(R.string.unknown);
        }
        return "";
    }

    private boolean isLegacyUnknownNumbers(CharSequence number) {
        return number != null ? LEGACY_UNKNOWN_NUMBERS.contains(number.toString()) : false;
    }

    public ContactInfo getContactInfo(@Nullable String number, int numberPresentation, @Nullable String countryIso) {
        if (countryIso == null) {
            countryIso = this.mCurrentCountryIso;
        }
        number = Strings.nullToEmpty(number);
        ContactInfo contactInfo = new ContactInfo();
        contactInfo.number = number;
        contactInfo.formattedNumber = PhoneNumberUtils.formatNumber(number, countryIso);
        contactInfo.normalizedNumber = PhoneNumberUtils.formatNumberToE164(number, countryIso);
        contactInfo.name = getDisplayName(this.mContext, number, numberPresentation).toString();
        if (!TextUtils.isEmpty(contactInfo.name)) {
            return contactInfo;
        }
        ContactInfo cachedContactInfo = this.mContactInfoHelper.lookupNumber(number, countryIso);
        if (cachedContactInfo != null && !TextUtils.isEmpty(cachedContactInfo.name)) {
            return cachedContactInfo;
        }
        if (!TextUtils.isEmpty(contactInfo.formattedNumber)) {
            contactInfo.name = contactInfo.formattedNumber;
        } else if (TextUtils.isEmpty(number)) {
            contactInfo.name = this.mContext.getResources().getString(R.string.unknown);
        } else {
            contactInfo.name = number;
        }
        return contactInfo;
    }

    public static void removeMissedCallNotifications(Context context) {
        if (hasPermission(context, "android.permission.MODIFY_PHONE_STATE")) {
            try {
                TelecomManager tm = (TelecomManager) context.getSystemService("telecom");
                if (tm != null) {
                    tm.cancelMissedCallsNotification();
                }
            } catch (SecurityException e) {
                Log.w("CallLogNotifHelper", "TelecomManager.cancelMissedCalls called without permission.");
            }
        }
    }

    public static NewCallsQuery createNewCallsQuery(Context context, ContentResolver contentResolver) {
        return new DefaultNewCallsQuery(context.getApplicationContext(), contentResolver);
    }

    private static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == 0;
    }
}
