package com.android.contacts.calllog;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;
import android.provider.ContactsContract.Contacts;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.android.contacts.ContactsUtils;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.provider.ContactsAppProvider;
import com.android.contacts.hap.utils.FixedPhoneNumberMatchUtils;
import com.android.contacts.util.UriUtils;
import com.huawei.cust.HwCustUtils;

public class ContactInfoHelper {
    private static final String[] PROJECTION_PREDEFINED_CALLLOG = new String[]{"ypid", "name", "number", "photo"};
    private static HwCustContactInfoHelper mHwCustContactInfoHelper;
    private final Context mContext;
    private final String mCurrentCountryIso;
    private final boolean mUseCallerInfo = SystemProperties.getBoolean("ro.config.hw_caller_info", true);

    public ContactInfoHelper(Context context, String currentCountryIso) {
        this.mContext = context;
        this.mCurrentCountryIso = currentCountryIso;
    }

    public ContactInfo lookupNumber(String number, String countryIso) {
        ContactInfo info;
        if (PhoneNumberUtils.isUriNumber(number)) {
            ContactInfo sipInfo = queryContactInfoForSipAddress(number);
            if (sipInfo == null || sipInfo == ContactInfo.EMPTY) {
                String username = number.substring(0, number.indexOf(64));
                if (PhoneNumberUtils.isGlobalPhoneNumber(username)) {
                    sipInfo = queryContactInfoForPhoneNumber(username, countryIso);
                }
            }
            info = sipInfo;
        } else {
            info = queryContactInfoForPhoneNumber(number, countryIso);
        }
        if (info == null) {
            return null;
        }
        if (info != ContactInfo.EMPTY) {
            return info;
        }
        ContactInfo updatedInfo = new ContactInfo();
        updatedInfo.number = number;
        updatedInfo.formattedNumber = formatPhoneNumber(number, null, countryIso);
        return updatedInfo;
    }

    private com.android.contacts.calllog.ContactInfo lookupContactFromUri(android.net.Uri r15) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Unexpected register number in merge insn: ?: MERGE  (r12_4 android.database.Cursor) = (r12_3 android.database.Cursor), (r12_6 android.database.Cursor)
	at jadx.core.dex.visitors.ssa.EliminatePhiNodes.replaceMerge(EliminatePhiNodes.java:84)
	at jadx.core.dex.visitors.ssa.EliminatePhiNodes.replaceMergeInstructions(EliminatePhiNodes.java:68)
	at jadx.core.dex.visitors.ssa.EliminatePhiNodes.visit(EliminatePhiNodes.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r14 = this;
        r0 = r14.mContext;
        r0 = r0.getContentResolver();
        r2 = com.android.contacts.calllog.PhoneQuery.getPhoneLookupProjection(r15);
        r3 = 0;
        r4 = 0;
        r5 = 0;
        r1 = r15;
        r12 = r0.query(r1, r2, r3, r4, r5);
        if (r12 == 0) goto L_0x00e7;
    L_0x0014:
        r0 = r12.moveToFirst();	 Catch:{ all -> 0x00df }
        if (r0 == 0) goto L_0x00e4;	 Catch:{ all -> 0x00df }
    L_0x001a:
        r8 = new com.android.contacts.calllog.ContactInfo;	 Catch:{ all -> 0x00df }
        r8.<init>();	 Catch:{ all -> 0x00df }
        r0 = 0;	 Catch:{ all -> 0x00df }
        r6 = r12.getLong(r0);	 Catch:{ all -> 0x00df }
        r0 = 7;	 Catch:{ all -> 0x00df }
        r11 = r12.getString(r0);	 Catch:{ all -> 0x00df }
        r0 = android.provider.ContactsContract.Contacts.getLookupUri(r6, r11);	 Catch:{ all -> 0x00df }
        r8.lookupUri = r0;	 Catch:{ all -> 0x00df }
        r0 = 1;	 Catch:{ all -> 0x00df }
        r0 = r12.getString(r0);	 Catch:{ all -> 0x00df }
        r8.name = r0;	 Catch:{ all -> 0x00df }
        r0 = 2;	 Catch:{ all -> 0x00df }
        r0 = r12.getInt(r0);	 Catch:{ all -> 0x00df }
        r8.type = r0;	 Catch:{ all -> 0x00df }
        r0 = 3;	 Catch:{ all -> 0x00df }
        r0 = r12.getString(r0);	 Catch:{ all -> 0x00df }
        r8.label = r0;	 Catch:{ all -> 0x00df }
        r0 = 4;	 Catch:{ all -> 0x00df }
        r0 = r12.getString(r0);	 Catch:{ all -> 0x00df }
        r8.number = r0;	 Catch:{ all -> 0x00df }
        r0 = 5;	 Catch:{ all -> 0x00df }
        r0 = r12.getString(r0);	 Catch:{ all -> 0x00df }
        r8.normalizedNumber = r0;	 Catch:{ all -> 0x00df }
        r0 = 6;	 Catch:{ all -> 0x00df }
        r0 = r12.getLong(r0);	 Catch:{ all -> 0x00df }
        r8.photoId = r0;	 Catch:{ all -> 0x00df }
        r0 = 8;	 Catch:{ all -> 0x00df }
        r0 = r12.getString(r0);	 Catch:{ all -> 0x00df }
        r0 = com.android.contacts.util.UriUtils.parseUriOrNull(r0);	 Catch:{ all -> 0x00df }
        r8.photoUri = r0;	 Catch:{ all -> 0x00df }
        r0 = 0;	 Catch:{ all -> 0x00df }
        r8.formattedNumber = r0;	 Catch:{ all -> 0x00df }
        r0 = android.provider.ContactsContract.Contacts.CONTENT_URI;	 Catch:{ all -> 0x00df }
        r0 = android.content.ContentUris.withAppendedId(r0, r6);	 Catch:{ all -> 0x00df }
        r0 = r0.toString();	 Catch:{ all -> 0x00df }
        r8.mCachedLookUpUriString = r0;	 Catch:{ all -> 0x00df }
        r10 = 0;	 Catch:{ all -> 0x00df }
        r0 = com.android.contacts.hap.EmuiFeatureManager.isPrivacyFeatureEnabled();	 Catch:{ all -> 0x00df }
        if (r0 == 0) goto L_0x00c2;	 Catch:{ all -> 0x00df }
    L_0x007b:
        r0 = r14.mContext;	 Catch:{ all -> 0x00df }
        r0 = r0.getContentResolver();	 Catch:{ all -> 0x00df }
        r1 = android.provider.ContactsContract.Contacts.CONTENT_URI;	 Catch:{ all -> 0x00df }
        r2 = 1;	 Catch:{ all -> 0x00df }
        r2 = new java.lang.String[r2];	 Catch:{ all -> 0x00df }
        r3 = "is_private";	 Catch:{ all -> 0x00df }
        r4 = 0;	 Catch:{ all -> 0x00df }
        r2[r4] = r3;	 Catch:{ all -> 0x00df }
        r3 = "_id= ?";	 Catch:{ all -> 0x00df }
        r4 = 1;	 Catch:{ all -> 0x00df }
        r4 = new java.lang.String[r4];	 Catch:{ all -> 0x00df }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00df }
        r5.<init>();	 Catch:{ all -> 0x00df }
        r5 = r5.append(r6);	 Catch:{ all -> 0x00df }
        r13 = "";	 Catch:{ all -> 0x00df }
        r5 = r5.append(r13);	 Catch:{ all -> 0x00df }
        r5 = r5.toString();	 Catch:{ all -> 0x00df }
        r13 = 0;	 Catch:{ all -> 0x00df }
        r4[r13] = r5;	 Catch:{ all -> 0x00df }
        r5 = 0;	 Catch:{ all -> 0x00df }
        r9 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ all -> 0x00df }
        if (r9 == 0) goto L_0x00c2;
    L_0x00b0:
        r0 = r9.moveToFirst();	 Catch:{ all -> 0x00da }
        if (r0 == 0) goto L_0x00bf;	 Catch:{ all -> 0x00da }
    L_0x00b6:
        r0 = 0;	 Catch:{ all -> 0x00da }
        r0 = r9.getInt(r0);	 Catch:{ all -> 0x00da }
        r1 = 1;
        if (r0 != r1) goto L_0x00d8;
    L_0x00be:
        r10 = 1;
    L_0x00bf:
        r9.close();	 Catch:{ all -> 0x00df }
    L_0x00c2:
        r8.mIsPrivate = r10;	 Catch:{ all -> 0x00df }
        r0 = 0;	 Catch:{ all -> 0x00df }
        r0 = r12.getLong(r0);	 Catch:{ all -> 0x00df }
        r0 = java.lang.Long.valueOf(r0);	 Catch:{ all -> 0x00df }
        r1 = 0;	 Catch:{ all -> 0x00df }
        r0 = com.android.contacts.ContactsUtils.determineUserType(r1, r0);	 Catch:{ all -> 0x00df }
        r8.userType = r0;	 Catch:{ all -> 0x00df }
    L_0x00d4:
        r12.close();
    L_0x00d7:
        return r8;
    L_0x00d8:
        r10 = 0;
        goto L_0x00bf;
    L_0x00da:
        r0 = move-exception;
        r9.close();	 Catch:{ all -> 0x00df }
        throw r0;	 Catch:{ all -> 0x00df }
    L_0x00df:
        r0 = move-exception;
        r12.close();
        throw r0;
    L_0x00e4:
        r8 = com.android.contacts.calllog.ContactInfo.EMPTY;	 Catch:{ all -> 0x00df }
        goto L_0x00d4;
    L_0x00e7:
        r8 = 0;
        goto L_0x00d7;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.calllog.ContactInfoHelper.lookupContactFromUri(android.net.Uri):com.android.contacts.calllog.ContactInfo");
    }

    private ContactInfo lookupContactFromUriForPhone(Uri uri, String number, String countryIso) {
        int fixedIndex;
        ContactInfo info;
        Throwable th;
        String yellowNum;
        ContactInfo info2;
        Cursor phonesPredefinedCursor = null;
        Cursor phonesCursor = this.mContext.getContentResolver().query(uri, PhoneQuery.getPhoneLookupProjection(uri), null, null, null);
        if (phonesCursor != null) {
            try {
                if (phonesCursor.getCount() > 0) {
                    fixedIndex = CommonUtilMethods.getCallerInfoHW(phonesCursor, number, "number", countryIso);
                    if (fixedIndex <= -1) {
                        info = null;
                    } else if (phonesCursor.moveToPosition(fixedIndex)) {
                        info = new ContactInfo();
                        long contactId = phonesCursor.getLong(0);
                        info.lookupUri = Contacts.getLookupUri(contactId, phonesCursor.getString(7));
                        info.name = phonesCursor.getString(1);
                        info.type = phonesCursor.getInt(2);
                        info.label = phonesCursor.getString(3);
                        info.number = phonesCursor.getString(4);
                        info.normalizedNumber = phonesCursor.getString(5);
                        info.photoId = phonesCursor.getLong(6);
                        info.photoUri = UriUtils.parseUriOrNull(phonesCursor.getString(8));
                        info.formattedNumber = null;
                        info.mCachedLookUpUriString = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId).toString();
                        boolean lIsPrivateContact = false;
                        if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
                            Cursor lCursorForPrivate = this.mContext.getContentResolver().query(Contacts.CONTENT_URI, new String[]{"is_private"}, "_id= ?", new String[]{contactId + ""}, null);
                            if (lCursorForPrivate != null) {
                                try {
                                    if (lCursorForPrivate.moveToFirst()) {
                                        lIsPrivateContact = lCursorForPrivate.getInt(0) == 1;
                                    }
                                    lCursorForPrivate.close();
                                } catch (Throwable th2) {
                                    th = th2;
                                    if (phonesCursor != null) {
                                        phonesCursor.close();
                                    }
                                    if (phonesPredefinedCursor != null) {
                                        phonesPredefinedCursor.close();
                                    }
                                    throw th;
                                }
                            }
                        }
                        info.mIsPrivate = lIsPrivateContact;
                        info.userType = ContactsUtils.determineUserType(null, Long.valueOf(phonesCursor.getLong(0)));
                    }
                    if (info != null) {
                        yellowNum = number;
                        phonesPredefinedCursor = this.mContext.getContentResolver().query(ContactsAppProvider.YELLOW_PAGE_DATA_URI, PROJECTION_PREDEFINED_CALLLOG, "PHONE_NUMBERS_EQUAL(number,?)", new String[]{number}, null);
                        fixedIndex = -1;
                        if (phonesPredefinedCursor != null) {
                            fixedIndex = CommonUtilMethods.getCallerInfoHW(phonesPredefinedCursor, number, "number", countryIso);
                        }
                        if (fixedIndex == -1 || !phonesPredefinedCursor.moveToPosition(fixedIndex)) {
                            if (EmuiFeatureManager.isChinaArea()) {
                                yellowNum = ContactsUtils.removeDashesAndBlanks(number);
                                if (!TextUtils.isEmpty(yellowNum)) {
                                    if (yellowNum.matches("^0\\d{2,3}[1,9]\\d{4}$")) {
                                        yellowNum = FixedPhoneNumberMatchUtils.parseFixedPhoneNumber(this.mContext, yellowNum);
                                        if (phonesPredefinedCursor != null) {
                                            phonesPredefinedCursor.close();
                                        }
                                        phonesPredefinedCursor = this.mContext.getContentResolver().query(ContactsAppProvider.YELLOW_PAGE_DATA_URI, PROJECTION_PREDEFINED_CALLLOG, "PHONE_NUMBERS_EQUAL(number,?)", new String[]{yellowNum}, null);
                                        if (phonesPredefinedCursor != null) {
                                            fixedIndex = CommonUtilMethods.getCallerInfoHW(phonesPredefinedCursor, yellowNum, "number", countryIso);
                                        }
                                    }
                                }
                            }
                        }
                        if (fixedIndex == -1 && phonesPredefinedCursor.moveToPosition(fixedIndex)) {
                            info2 = new ContactInfo();
                            int id = phonesPredefinedCursor.getInt(0);
                            info2.name = phonesPredefinedCursor.getString(1);
                            info2.number = yellowNum;
                            info2.lookupUri = ContentUris.withAppendedId(ContactsAppProvider.YELLOW_PAGE_URI, (long) id);
                            if (TextUtils.isEmpty(phonesPredefinedCursor.getString(3))) {
                                info2.photoUri = null;
                            } else {
                                info2.photoUri = ContentUris.withAppendedId(ContactsAppProvider.YELLOW_PAGE_URI, (long) id);
                            }
                            info2.formattedNumber = null;
                        } else {
                            info2 = ContactInfo.EMPTY;
                        }
                    } else {
                        info2 = info;
                    }
                    if (phonesCursor != null) {
                        phonesCursor.close();
                    }
                    if (phonesPredefinedCursor != null) {
                        phonesPredefinedCursor.close();
                    }
                    return info2;
                }
            } catch (Throwable th3) {
                th = th3;
                if (phonesCursor != null) {
                    phonesCursor.close();
                }
                if (phonesPredefinedCursor != null) {
                    phonesPredefinedCursor.close();
                }
                throw th;
            }
        }
        info = null;
        if (info != null) {
            info2 = info;
        } else {
            yellowNum = number;
            phonesPredefinedCursor = this.mContext.getContentResolver().query(ContactsAppProvider.YELLOW_PAGE_DATA_URI, PROJECTION_PREDEFINED_CALLLOG, "PHONE_NUMBERS_EQUAL(number,?)", new String[]{number}, null);
            fixedIndex = -1;
            if (phonesPredefinedCursor != null) {
                fixedIndex = CommonUtilMethods.getCallerInfoHW(phonesPredefinedCursor, number, "number", countryIso);
            }
            if (EmuiFeatureManager.isChinaArea()) {
                yellowNum = ContactsUtils.removeDashesAndBlanks(number);
                if (TextUtils.isEmpty(yellowNum)) {
                    if (yellowNum.matches("^0\\d{2,3}[1,9]\\d{4}$")) {
                        yellowNum = FixedPhoneNumberMatchUtils.parseFixedPhoneNumber(this.mContext, yellowNum);
                        if (phonesPredefinedCursor != null) {
                            phonesPredefinedCursor.close();
                        }
                        phonesPredefinedCursor = this.mContext.getContentResolver().query(ContactsAppProvider.YELLOW_PAGE_DATA_URI, PROJECTION_PREDEFINED_CALLLOG, "PHONE_NUMBERS_EQUAL(number,?)", new String[]{yellowNum}, null);
                        if (phonesPredefinedCursor != null) {
                            fixedIndex = CommonUtilMethods.getCallerInfoHW(phonesPredefinedCursor, yellowNum, "number", countryIso);
                        }
                    }
                }
            }
            if (fixedIndex == -1) {
            }
            info2 = ContactInfo.EMPTY;
        }
        if (phonesCursor != null) {
            phonesCursor.close();
        }
        if (phonesPredefinedCursor != null) {
            phonesPredefinedCursor.close();
        }
        return info2;
    }

    private ContactInfo queryContactInfoForSipAddress(String sipAddress) {
        return lookupContactFromUri(QueryUtil.getPhoneLookupUri(Uri.encode(sipAddress), "sip", CallInterceptDetails.BRANDED_STATE));
    }

    private ContactInfo queryContactInfoForPhoneNumber(String number, String countryIso) {
        String contactNumber = number;
        String normalizedNumber = PhoneNumberUtils.normalizeNumber(number);
        if (TextUtils.isEmpty(normalizedNumber)) {
            return null;
        }
        int numberLen;
        if (normalizedNumber != null) {
            numberLen = normalizedNumber.length();
        } else {
            numberLen = 0;
        }
        if (numberLen > 7) {
            normalizedNumber = normalizedNumber.substring(numberLen - 7);
        }
        if (TextUtils.isEmpty(normalizedNumber)) {
            return null;
        }
        ContactInfo info;
        boolean isStartWithPlus = false;
        if (normalizedNumber.charAt(0) == '+' && normalizedNumber.length() > 1) {
            normalizedNumber = normalizedNumber.substring(1);
            isStartWithPlus = true;
        }
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            mHwCustContactInfoHelper = (HwCustContactInfoHelper) HwCustUtils.createObj(HwCustContactInfoHelper.class, new Object[0]);
        }
        if (mHwCustContactInfoHelper != null) {
            normalizedNumber = mHwCustContactInfoHelper.getNormalizedNumber(normalizedNumber);
        } else {
            normalizedNumber = Uri.encode(normalizedNumber, "#");
        }
        if (isStartWithPlus) {
            normalizedNumber = "+" + normalizedNumber;
        }
        Uri uri = QueryUtil.getPhoneLookupUri(normalizedNumber, "PHONE_NUMBER", number);
        if (this.mUseCallerInfo) {
            info = lookupContactFromUriForPhone(uri, number, countryIso);
        } else {
            info = lookupContactFromUri(uri);
        }
        if (!(info == null || info == ContactInfo.EMPTY)) {
            info.formattedNumber = formatPhoneNumber(number, null, countryIso);
        }
        return info;
    }

    private String formatPhoneNumber(String number, String normalizedNumber, String countryIso) {
        if (TextUtils.isEmpty(number)) {
            return "";
        }
        if (PhoneNumberUtils.isUriNumber(number)) {
            return number;
        }
        if (TextUtils.isEmpty(countryIso)) {
            countryIso = this.mCurrentCountryIso;
        }
        return PhoneNumberUtils.formatNumber(number, normalizedNumber, countryIso);
    }
}
