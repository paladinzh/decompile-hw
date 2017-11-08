package com.android.contacts.calllog;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.android.contacts.CallUtil;
import com.android.contacts.ContactsUtils;
import com.android.contacts.PhoneCallDetails;
import com.android.contacts.activities.ContactDetailActivity;
import com.android.contacts.activities.ContactEditorActivity;
import com.android.contacts.activities.DialtactsActivity;
import com.android.contacts.compatibility.CompatUtils;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.hap.CommonConstants;
import com.android.contacts.hap.CommonUtilMethods;
import java.util.ArrayList;

public abstract class IntentProvider {
    private String mName;

    public abstract Intent getIntent(Context context);

    public static IntentProvider getReturnCallIntentProvider(String number, int presentation, int aSubId, String normalizedNumber, long duration, String countryIso, String callLookupuri, String callName, int feature) {
        final String str = number;
        final int i = presentation;
        final int i2 = aSubId;
        final String str2 = callName;
        final String str3 = normalizedNumber;
        final String str4 = countryIso;
        final String str5 = callLookupuri;
        final long j = duration;
        final int i3 = feature;
        return new IntentProvider() {
            public Intent getIntent(Context context) {
                if (!ContactsUtils.isNumberDialable(str, i)) {
                    return null;
                }
                Intent callIntent = CallUtil.getCallIntent(str, i, i2);
                IntentProvider.addRoamingDataIntent(callIntent, str2, str3, str4, str5, j);
                callIntent.putExtra("EXTRA_FEATURE", i3);
                return callIntent;
            }
        };
    }

    public static IntentProvider getCallDetailIntentProvider(Cursor callLogAdapterCursor, int position, long id, int groupSize, String formatNumber, String markInfo, String cacheUri, String name, boolean isSpecialNumber, String originMarkInfo) {
        final Cursor cursor = callLogAdapterCursor;
        final int i = position;
        final String str = cacheUri;
        final String str2 = formatNumber;
        final String str3 = markInfo;
        final String str4 = originMarkInfo;
        final String str5 = name;
        final int i2 = groupSize;
        final long j = id;
        final boolean z = isSpecialNumber;
        return new IntentProvider() {
            public Intent getIntent(Context context) {
                Cursor cursor = cursor;
                if (cursor == null || cursor.isClosed()) {
                    return null;
                }
                cursor.moveToPosition(i);
                if (CallLogQuery.isSectionHeader(cursor)) {
                    return null;
                }
                Intent intent = new Intent(context, ContactDetailActivity.class);
                String lookupUriString = cursor.getString(11);
                if (lookupUriString == null) {
                    lookupUriString = str;
                }
                if (lookupUriString != null) {
                    intent.setData(Uri.parse(lookupUriString));
                } else {
                    intent.putExtra("EXTRA_CALL_LOG_NONAME_CALL", true);
                }
                String number = cursor.getString(1);
                int presentation = cursor.getInt(17);
                if (number != null) {
                    intent.putExtra("EXTRA_CALL_LOG_NUMBER", number);
                }
                if (CompatUtils.isNCompatible()) {
                    intent.putExtra("EXTRA_CALL_LOG_POST_DIAL_DIGITS", cursor.getString(CallLogQuery.POST_DIAL_DIGITS));
                }
                String geo = cursor.getString(7);
                if (geo != null) {
                    intent.putExtra("EXTRA_CALL_LOG_GEO", geo);
                }
                String countryIso = cursor.getString(5);
                if (countryIso != null) {
                    intent.putExtra("EXTRA_CALL_LOG_COUNTRY_ISO", countryIso);
                }
                String formattedNum = str2;
                if (formattedNum != null) {
                    intent.putExtra("EXTRA_CALL_LOG_FORMATTED_NUM", formattedNum);
                } else {
                    formattedNum = cursor.getString(15);
                    if (formattedNum == null) {
                        formattedNum = ContactsUtils.formatPhoneNumber(number, null, ContactsUtils.getCurrentCountryIso(context), context);
                    }
                    intent.putExtra("EXTRA_CALL_LOG_FORMATTED_NUM", formattedNum);
                }
                String normalizedNumber = cursor.getString(13);
                if (normalizedNumber != null) {
                    intent.putExtra("EXTRA_NORMALIZED_NUMBER", normalizedNumber);
                }
                String duration = cursor.getString(3);
                if (duration != null) {
                    intent.putExtra("EXTRA_DURATION", duration);
                }
                intent.putExtra("EXTRA_CALL_LOG_PRESENTATION", presentation);
                if (!TextUtils.isEmpty(str3)) {
                    intent.putExtra("EXTRA_CALL_LOG_MARKINFO", str3);
                    intent.putExtra("EXTRA_CALL_LOG_ORIGIN_MARK_INFO", str4);
                }
                intent.putExtra("INTENT_FROM_DIALER", true);
                intent.putExtra("EXTRA_IGNORE_INCOMING_CALLLOG_IDS", true);
                intent.putExtra("contact_display_name", str5);
                if (i2 > 1) {
                    int index;
                    ArrayList<Long> lcalllogids = new ArrayList(i2);
                    for (index = 0; index < i2; index++) {
                        if (CommonConstants.IS_HW_CUSTOM_NUMBER_MATCHING_ENABLED) {
                            if (CommonUtilMethods.equalByNameOrNumber(str5, number, cursor.getString(8), cursor.getString(1))) {
                                if (CommonUtilMethods.compareNumsHw(number, countryIso, cursor.getString(1), cursor.getString(5))) {
                                    lcalllogids.add(Long.valueOf(cursor.getLong(0)));
                                }
                            }
                        } else {
                            lcalllogids.add(Long.valueOf(cursor.getLong(0)));
                        }
                        cursor.moveToNext();
                    }
                    index = 0;
                    long[] ids = new long[lcalllogids.size()];
                    for (Long id : lcalllogids) {
                        int index2 = index + 1;
                        ids[index] = id.longValue();
                        index = index2;
                    }
                    intent.putExtra("EXTRA_CALL_LOG_IDS", ids);
                } else {
                    intent.putExtra("EXTRA_CALL_LOG_IDS", new long[]{j});
                }
                intent.putExtra("contact_display_name_is_special_num", z);
                if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                    intent.putExtra("EXTRA_ID_SELECTED", j);
                }
                return intent;
            }
        };
    }

    public static IntentProvider getDialerCallIntentProvider(String number, String name, String normalized_numebr, String countryIso, String lookupuri, long duration, int callFeature) {
        if (number == null) {
            return null;
        }
        final String Dialnumber = PhoneNumberUtils.convertPreDial(number);
        final String str = name;
        final String str2 = normalized_numebr;
        final String str3 = countryIso;
        final String str4 = lookupuri;
        final long j = duration;
        final int i = callFeature;
        return new IntentProvider() {
            public Intent getIntent(Context context) {
                Intent intent = new Intent(QueryUtil.isSystemAppForContacts() ? "android.intent.action.CALL_PRIVILEGED" : "android.intent.action.CALL", Uri.fromParts("tel", Dialnumber, null));
                intent.setFlags(268435456);
                IntentProvider.addRoamingDataIntent(intent, str, str2, str3, str4, j);
                if (context instanceof DialtactsActivity) {
                    intent.putExtra("com.android.phone.CALL_ORIGIN", "com.android.contacts.activities.DialtactsActivity");
                }
                intent.putExtra("EXTRA_FEATURE", i);
                if (str4 != null) {
                    intent.putExtra("EXTRA_IS_YELLOWPAGE_URI", str4.contains("com.android.contacts.app"));
                }
                return intent;
            }
        };
    }

    public static void addRoamingDataIntent(Intent intent, String name, String normalized_numebr, String countryIso, String lookupuri, long duration) {
        if (intent != null) {
            intent.putExtra("EXTRA_NORMALIZED_NUMBER", normalized_numebr);
            intent.putExtra("EXTRA_DURATION", duration);
            intent.putExtra("EXTRA_CALL_LOG_COUNTRY_ISO", countryIso);
            intent.putExtra("contact_display_name", name);
            intent.putExtra("EXTRA_LOOKUP_URI", lookupuri);
        }
    }

    public static IntentProvider getContactDetailIntentProvider(final Uri lookupUri, final int hashcode, final boolean isEnterpriseContact) {
        return new IntentProvider() {
            public Intent getIntent(Context context) {
                Intent intent = new Intent("android.intent.action.VIEW", lookupUri);
                intent.putExtra("contact_select_hashcode", hashcode);
                intent.setComponent(new ComponentName(context, ContactDetailActivity.class));
                intent.setFlags(67108864);
                intent.putExtra("is_enterprise_contact", isEnterpriseContact);
                return intent;
            }
        };
    }

    public static IntentProvider getCallDetailIntentProvider(long id, PhoneCallDetails details, String numberMark, String originMarkInfo) {
        final PhoneCallDetails phoneCallDetails = details;
        final long j = id;
        final String str = numberMark;
        final String str2 = originMarkInfo;
        return new IntentProvider() {
            public Intent getIntent(Context context) {
                Intent intent = new Intent(context, ContactDetailActivity.class);
                intent.putExtra("EXTRA_CALL_LOG_NONAME_CALL", true);
                String number = String.valueOf(phoneCallDetails.number);
                int presentation = phoneCallDetails.getPresentation();
                String geo = phoneCallDetails.geocode;
                if (geo != null) {
                    intent.putExtra("EXTRA_CALL_LOG_GEO", geo);
                }
                String countryIso = phoneCallDetails.countryIso;
                if (countryIso != null) {
                    intent.putExtra("EXTRA_CALL_LOG_COUNTRY_ISO", countryIso);
                }
                String formattedNum = ContactsUtils.formatPhoneNumber(number, null, ContactsUtils.getCurrentCountryIso(context), context);
                if (formattedNum != null) {
                    intent.putExtra("EXTRA_CALL_LOG_FORMATTED_NUM", formattedNum);
                }
                intent.putExtra("EXTRA_CALL_LOG_PRESENTATION", presentation);
                intent.putExtra("EXTRA_IGNORE_INCOMING_CALLLOG_IDS", true);
                intent.putExtra("EXTRA_CALL_LOG_NUMBER", number);
                if (CompatUtils.isNCompatible()) {
                    intent.putExtra("EXTRA_CALL_LOG_POST_DIAL_DIGITS", phoneCallDetails.postDialDigits);
                }
                intent.putExtra("EXTRA_CALL_LOG_IDS", new long[]{j});
                intent.putExtra("INTENT_FROM_DIALER", true);
                if (!TextUtils.isEmpty(str)) {
                    intent.putExtra("EXTRA_CALL_LOG_MARKINFO", str);
                    intent.putExtra("EXTRA_CALL_LOG_ORIGIN_MARK_INFO", str2);
                }
                if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                    intent.putExtra("EXTRA_ID_SELECTED", j);
                }
                return intent;
            }
        };
    }

    public void setName(String name) {
        this.mName = name;
    }

    public static Intent getViewContactIntent(Context context, Uri uri) {
        Intent intent = new Intent("android.intent.action.VIEW", uri);
        intent.setComponent(new ComponentName(context, ContactDetailActivity.class));
        return intent;
    }

    public static Intent getEditorContactIntent(Context context, Uri uri) {
        Intent intent = new Intent("android.intent.action.EDIT", uri);
        intent.setComponent(new ComponentName(context, ContactEditorActivity.class));
        return intent;
    }
}
