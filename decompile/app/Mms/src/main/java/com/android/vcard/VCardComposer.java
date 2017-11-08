package com.android.vcard;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContactsEntity;
import android.text.TextUtils;
import android.util.Log;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VCardComposer {
    private static final String[] sContactsProjection = new String[]{"_id"};
    private static final Map<Integer, String> sImMap = new HashMap();
    private final String mCharset;
    private final ContentResolver mContentResolver;
    private Uri mContentUriForRawContactsEntity;
    private Cursor mCursor;
    private boolean mCursorSuppliedFromOutside;
    private String mErrorReason;
    private boolean mFirstVCardEmittedInDoCoMoCase;
    private Map<Long, String> mGroupMap;
    private int mIdColumn;
    private boolean mInitDone;
    private final boolean mIsDoCoMo;
    private VCardPhoneNumberTranslationCallback mPhoneTranslationCallback;
    private RawContactEntitlesInfoCallback mRawContactEntitlesInfoCallback;
    private boolean mTerminateCalled;
    private final int mVCardType;

    public static class RawContactEntitlesInfo {
        public final long contactId;
        public final Uri rawContactEntitlesUri;
    }

    public interface RawContactEntitlesInfoCallback {
        RawContactEntitlesInfo getRawContactEntitlesInfo(long j);
    }

    static {
        sImMap.put(Integer.valueOf(0), "X-AIM");
        sImMap.put(Integer.valueOf(1), "X-MSN");
        sImMap.put(Integer.valueOf(2), "X-YAHOO");
        sImMap.put(Integer.valueOf(6), "X-ICQ");
        sImMap.put(Integer.valueOf(7), "X-JABBER");
        sImMap.put(Integer.valueOf(3), "X-SKYPE-USERNAME");
    }

    public VCardComposer(Context context, int vcardType, boolean careHandlerErrors) {
        this(context, vcardType, null, careHandlerErrors);
    }

    public VCardComposer(Context context, int vcardType, String charset, boolean careHandlerErrors) {
        this(context, context.getContentResolver(), vcardType, charset, careHandlerErrors);
    }

    public VCardComposer(Context context, ContentResolver resolver, int vcardType, String charset, boolean careHandlerErrors) {
        this.mErrorReason = "No error";
        this.mTerminateCalled = true;
        this.mGroupMap = new HashMap();
        this.mVCardType = vcardType;
        this.mContentResolver = resolver;
        this.mIsDoCoMo = VCardConfig.isDoCoMo(vcardType);
        if (TextUtils.isEmpty(charset)) {
            charset = "UTF-8";
        }
        boolean shouldAppendCharsetParam = (VCardConfig.isVersion30(vcardType) && "UTF-8".equalsIgnoreCase(charset)) ? false : true;
        if (this.mIsDoCoMo || shouldAppendCharsetParam) {
            if ("SHIFT_JIS".equalsIgnoreCase(charset)) {
                this.mCharset = charset;
            } else if (TextUtils.isEmpty(charset)) {
                this.mCharset = "SHIFT_JIS";
            } else {
                this.mCharset = charset;
            }
        } else if (TextUtils.isEmpty(charset)) {
            this.mCharset = "UTF-8";
        } else {
            this.mCharset = charset;
        }
        Log.d("VCardComposer", "Use the charset \"" + this.mCharset + "\"");
    }

    public boolean init(String selection, String[] selectionArgs) {
        return init(Contacts.CONTENT_URI, sContactsProjection, selection, selectionArgs, null, null);
    }

    public boolean init(Uri contentUri, String[] projection, String selection, String[] selectionArgs, String sortOrder, Uri contentUriForRawContactsEntity) {
        if (!"com.android.contacts".equals(contentUri.getAuthority())) {
            this.mErrorReason = "The Uri vCard composer received is not supported by the composer.";
            return false;
        } else if (initInterFirstPart(contentUriForRawContactsEntity) && initInterCursorCreationPart(contentUri, projection, selection, selectionArgs, sortOrder) && initInterMainPart()) {
            return initInterLastPart();
        } else {
            return false;
        }
    }

    private boolean initInterFirstPart(Uri contentUriForRawContactsEntity) {
        if (contentUriForRawContactsEntity == null) {
            contentUriForRawContactsEntity = RawContactsEntity.CONTENT_URI;
        }
        this.mContentUriForRawContactsEntity = contentUriForRawContactsEntity;
        if (!this.mInitDone) {
            return true;
        }
        Log.e("VCardComposer", "init() is already called");
        return false;
    }

    private boolean initInterCursorCreationPart(Uri contentUri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        this.mCursorSuppliedFromOutside = false;
        this.mCursor = this.mContentResolver.query(contentUri, projection, selection, selectionArgs, sortOrder);
        if (this.mCursor != null) {
            return true;
        }
        Log.e("VCardComposer", String.format("Cursor became null unexpectedly", new Object[0]));
        this.mErrorReason = "Failed to get database information";
        return false;
    }

    private boolean initInterMainPart() {
        boolean z = false;
        if (this.mCursor.getCount() == 0 || !this.mCursor.moveToFirst()) {
            closeCursorIfAppropriate();
            return false;
        }
        this.mIdColumn = this.mCursor.getColumnIndex("contact_id");
        if (this.mIdColumn < 0) {
            this.mIdColumn = this.mCursor.getColumnIndex("_id");
        }
        if (this.mIdColumn >= 0) {
            z = true;
        }
        return z;
    }

    private boolean initInterLastPart() {
        this.mInitDone = true;
        this.mTerminateCalled = false;
        return true;
    }

    public String createOneEntry() {
        return createOneEntry(null);
    }

    public String createOneEntry(Method getEntityIteratorMethod) {
        if (this.mIsDoCoMo && !this.mFirstVCardEmittedInDoCoMoCase) {
            this.mFirstVCardEmittedInDoCoMoCase = true;
        }
        String vcard = createOneEntryInternal(this.mCursor.getLong(this.mIdColumn), getEntityIteratorMethod);
        if (!this.mCursor.moveToNext()) {
            Log.e("VCardComposer", "Cursor#moveToNext() returned false");
        }
        return vcard;
    }

    private java.lang.String createOneEntryInternal(long r24, java.lang.reflect.Method r26) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxOverflowException: Regions stack size limit reached
	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:37)
	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:61)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r23 = this;
        r10 = new java.util.HashMap;
        r10.<init>();
        r16 = 0;
        r0 = r23;	 Catch:{ all -> 0x0081 }
        r3 = r0.mContentUriForRawContactsEntity;	 Catch:{ all -> 0x0081 }
        r0 = r23;	 Catch:{ all -> 0x0081 }
        r2 = r0.mRawContactEntitlesInfoCallback;	 Catch:{ all -> 0x0081 }
        if (r2 == 0) goto L_0x0025;	 Catch:{ all -> 0x0081 }
    L_0x0011:
        r0 = r23;	 Catch:{ all -> 0x0081 }
        r2 = r0.mRawContactEntitlesInfoCallback;	 Catch:{ all -> 0x0081 }
        r0 = r24;	 Catch:{ all -> 0x0081 }
        r20 = r2.getRawContactEntitlesInfo(r0);	 Catch:{ all -> 0x0081 }
        r0 = r20;	 Catch:{ all -> 0x0081 }
        r3 = r0.rawContactEntitlesUri;	 Catch:{ all -> 0x0081 }
        r0 = r20;	 Catch:{ all -> 0x0081 }
        r0 = r0.contactId;	 Catch:{ all -> 0x0081 }
        r24 = r0;	 Catch:{ all -> 0x0081 }
    L_0x0025:
        r21 = "contact_id=?";	 Catch:{ all -> 0x0081 }
        r2 = 1;	 Catch:{ all -> 0x0081 }
        r6 = new java.lang.String[r2];	 Catch:{ all -> 0x0081 }
        r2 = java.lang.String.valueOf(r24);	 Catch:{ all -> 0x0081 }
        r4 = 0;	 Catch:{ all -> 0x0081 }
        r6[r4] = r2;	 Catch:{ all -> 0x0081 }
        if (r26 == 0) goto L_0x00c8;
    L_0x0034:
        r2 = 5;
        r2 = new java.lang.Object[r2];	 Catch:{ IllegalArgumentException -> 0x00a8, IllegalAccessException -> 0x0088, InvocationTargetException -> 0x006e }
        r0 = r23;	 Catch:{ IllegalArgumentException -> 0x00a8, IllegalAccessException -> 0x0088, InvocationTargetException -> 0x006e }
        r4 = r0.mContentResolver;	 Catch:{ IllegalArgumentException -> 0x00a8, IllegalAccessException -> 0x0088, InvocationTargetException -> 0x006e }
        r5 = 0;	 Catch:{ IllegalArgumentException -> 0x00a8, IllegalAccessException -> 0x0088, InvocationTargetException -> 0x006e }
        r2[r5] = r4;	 Catch:{ IllegalArgumentException -> 0x00a8, IllegalAccessException -> 0x0088, InvocationTargetException -> 0x006e }
        r4 = 1;	 Catch:{ IllegalArgumentException -> 0x00a8, IllegalAccessException -> 0x0088, InvocationTargetException -> 0x006e }
        r2[r4] = r3;	 Catch:{ IllegalArgumentException -> 0x00a8, IllegalAccessException -> 0x0088, InvocationTargetException -> 0x006e }
        r4 = "contact_id=?";	 Catch:{ IllegalArgumentException -> 0x00a8, IllegalAccessException -> 0x0088, InvocationTargetException -> 0x006e }
        r5 = 2;	 Catch:{ IllegalArgumentException -> 0x00a8, IllegalAccessException -> 0x0088, InvocationTargetException -> 0x006e }
        r2[r5] = r4;	 Catch:{ IllegalArgumentException -> 0x00a8, IllegalAccessException -> 0x0088, InvocationTargetException -> 0x006e }
        r4 = 3;	 Catch:{ IllegalArgumentException -> 0x00a8, IllegalAccessException -> 0x0088, InvocationTargetException -> 0x006e }
        r2[r4] = r6;	 Catch:{ IllegalArgumentException -> 0x00a8, IllegalAccessException -> 0x0088, InvocationTargetException -> 0x006e }
        r4 = 0;	 Catch:{ IllegalArgumentException -> 0x00a8, IllegalAccessException -> 0x0088, InvocationTargetException -> 0x006e }
        r5 = 4;	 Catch:{ IllegalArgumentException -> 0x00a8, IllegalAccessException -> 0x0088, InvocationTargetException -> 0x006e }
        r2[r5] = r4;	 Catch:{ IllegalArgumentException -> 0x00a8, IllegalAccessException -> 0x0088, InvocationTargetException -> 0x006e }
        r4 = 0;	 Catch:{ IllegalArgumentException -> 0x00a8, IllegalAccessException -> 0x0088, InvocationTargetException -> 0x006e }
        r0 = r26;	 Catch:{ IllegalArgumentException -> 0x00a8, IllegalAccessException -> 0x0088, InvocationTargetException -> 0x006e }
        r2 = r0.invoke(r4, r2);	 Catch:{ IllegalArgumentException -> 0x00a8, IllegalAccessException -> 0x0088, InvocationTargetException -> 0x006e }
        r0 = r2;	 Catch:{ IllegalArgumentException -> 0x00a8, IllegalAccessException -> 0x0088, InvocationTargetException -> 0x006e }
        r0 = (android.content.EntityIterator) r0;	 Catch:{ IllegalArgumentException -> 0x00a8, IllegalAccessException -> 0x0088, InvocationTargetException -> 0x006e }
        r16 = r0;	 Catch:{ IllegalArgumentException -> 0x00a8, IllegalAccessException -> 0x0088, InvocationTargetException -> 0x006e }
    L_0x005a:
        if (r16 != 0) goto L_0x00ea;
    L_0x005c:
        r2 = "VCardComposer";	 Catch:{ all -> 0x0081 }
        r4 = "EntityIterator is null";	 Catch:{ all -> 0x0081 }
        android.util.Log.e(r2, r4);	 Catch:{ all -> 0x0081 }
        r2 = "";	 Catch:{ all -> 0x0081 }
        if (r16 == 0) goto L_0x006d;
    L_0x006a:
        r16.close();
    L_0x006d:
        return r2;
    L_0x006e:
        r14 = move-exception;
        r2 = "VCardComposer";	 Catch:{ all -> 0x0081 }
        r4 = "InvocationTargetException has been thrown: ";	 Catch:{ all -> 0x0081 }
        android.util.Log.e(r2, r4, r14);	 Catch:{ all -> 0x0081 }
        r2 = new java.lang.RuntimeException;	 Catch:{ all -> 0x0081 }
        r4 = "InvocationTargetException has been thrown";	 Catch:{ all -> 0x0081 }
        r2.<init>(r4);	 Catch:{ all -> 0x0081 }
        throw r2;	 Catch:{ all -> 0x0081 }
    L_0x0081:
        r2 = move-exception;
        if (r16 == 0) goto L_0x0087;
    L_0x0084:
        r16.close();
    L_0x0087:
        throw r2;
    L_0x0088:
        r12 = move-exception;
        r2 = "VCardComposer";	 Catch:{ all -> 0x0081 }
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0081 }
        r4.<init>();	 Catch:{ all -> 0x0081 }
        r5 = "IllegalAccessException has been thrown: ";	 Catch:{ all -> 0x0081 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0081 }
        r5 = r12.getMessage();	 Catch:{ all -> 0x0081 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0081 }
        r4 = r4.toString();	 Catch:{ all -> 0x0081 }
        android.util.Log.e(r2, r4);	 Catch:{ all -> 0x0081 }
        goto L_0x005a;	 Catch:{ all -> 0x0081 }
    L_0x00a8:
        r13 = move-exception;	 Catch:{ all -> 0x0081 }
        r2 = "VCardComposer";	 Catch:{ all -> 0x0081 }
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0081 }
        r4.<init>();	 Catch:{ all -> 0x0081 }
        r5 = "IllegalArgumentException has been thrown: ";	 Catch:{ all -> 0x0081 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0081 }
        r5 = r13.getMessage();	 Catch:{ all -> 0x0081 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0081 }
        r4 = r4.toString();	 Catch:{ all -> 0x0081 }
        android.util.Log.e(r2, r4);	 Catch:{ all -> 0x0081 }
        goto L_0x005a;	 Catch:{ all -> 0x0081 }
    L_0x00c8:
        r0 = r23;	 Catch:{ all -> 0x0081 }
        r2 = r0.mContentResolver;	 Catch:{ all -> 0x0081 }
        r5 = "contact_id=?";	 Catch:{ all -> 0x0081 }
        r4 = 0;	 Catch:{ all -> 0x0081 }
        r7 = 0;	 Catch:{ all -> 0x0081 }
        r11 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ all -> 0x0081 }
        if (r11 != 0) goto L_0x00e4;	 Catch:{ all -> 0x0081 }
    L_0x00d7:
        r2 = "VCardComposer";	 Catch:{ all -> 0x0081 }
        r4 = "query returns null cursor";	 Catch:{ all -> 0x0081 }
        android.util.Log.e(r2, r4);	 Catch:{ all -> 0x0081 }
        r2 = "";	 Catch:{ all -> 0x0081 }
        return r2;	 Catch:{ all -> 0x0081 }
    L_0x00e4:
        r16 = android.provider.ContactsContract.RawContacts.newEntityIterator(r11);	 Catch:{ all -> 0x0081 }
        goto L_0x005a;	 Catch:{ all -> 0x0081 }
    L_0x00ea:
        r2 = r16.hasNext();	 Catch:{ all -> 0x0081 }
        if (r2 != 0) goto L_0x0115;	 Catch:{ all -> 0x0081 }
    L_0x00f0:
        r2 = "VCardComposer";	 Catch:{ all -> 0x0081 }
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0081 }
        r4.<init>();	 Catch:{ all -> 0x0081 }
        r5 = "Data does not exist. contactId: ";	 Catch:{ all -> 0x0081 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0081 }
        r0 = r24;	 Catch:{ all -> 0x0081 }
        r4 = r4.append(r0);	 Catch:{ all -> 0x0081 }
        r4 = r4.toString();	 Catch:{ all -> 0x0081 }
        android.util.Log.w(r2, r4);	 Catch:{ all -> 0x0081 }
        r2 = "";	 Catch:{ all -> 0x0081 }
        if (r16 == 0) goto L_0x0114;
    L_0x0111:
        r16.close();
    L_0x0114:
        return r2;
    L_0x0115:
        r2 = r16.hasNext();	 Catch:{ all -> 0x0081 }
        if (r2 == 0) goto L_0x015a;	 Catch:{ all -> 0x0081 }
    L_0x011b:
        r15 = r16.next();	 Catch:{ all -> 0x0081 }
        r15 = (android.content.Entity) r15;	 Catch:{ all -> 0x0081 }
        r2 = r15.getSubValues();	 Catch:{ all -> 0x0081 }
        r19 = r2.iterator();	 Catch:{ all -> 0x0081 }
    L_0x0129:
        r2 = r19.hasNext();	 Catch:{ all -> 0x0081 }
        if (r2 == 0) goto L_0x0115;	 Catch:{ all -> 0x0081 }
    L_0x012f:
        r18 = r19.next();	 Catch:{ all -> 0x0081 }
        r18 = (android.content.Entity.NamedContentValues) r18;	 Catch:{ all -> 0x0081 }
        r0 = r18;	 Catch:{ all -> 0x0081 }
        r8 = r0.values;	 Catch:{ all -> 0x0081 }
        r2 = "mimetype";	 Catch:{ all -> 0x0081 }
        r17 = r8.getAsString(r2);	 Catch:{ all -> 0x0081 }
        if (r17 == 0) goto L_0x0129;	 Catch:{ all -> 0x0081 }
    L_0x0142:
        r0 = r17;	 Catch:{ all -> 0x0081 }
        r9 = r10.get(r0);	 Catch:{ all -> 0x0081 }
        r9 = (java.util.List) r9;	 Catch:{ all -> 0x0081 }
        if (r9 != 0) goto L_0x0156;	 Catch:{ all -> 0x0081 }
    L_0x014c:
        r9 = new java.util.ArrayList;	 Catch:{ all -> 0x0081 }
        r9.<init>();	 Catch:{ all -> 0x0081 }
        r0 = r17;	 Catch:{ all -> 0x0081 }
        r10.put(r0, r9);	 Catch:{ all -> 0x0081 }
    L_0x0156:
        r9.add(r8);	 Catch:{ all -> 0x0081 }
        goto L_0x0129;
    L_0x015a:
        if (r16 == 0) goto L_0x015f;
    L_0x015c:
        r16.close();
    L_0x015f:
        r0 = r23;
        r2 = r0.buildVCard(r10);
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.vcard.VCardComposer.createOneEntryInternal(long, java.lang.reflect.Method):java.lang.String");
    }

    public String buildVCard(Map<String, List<ContentValues>> contentValuesListMap) {
        if (contentValuesListMap == null) {
            Log.e("VCardComposer", "The given map is null. Ignore and return empty String");
            return "";
        }
        VCardBuilder builder = new VCardBuilder(this.mVCardType, this.mCharset);
        builder.appendNameProperties((List) contentValuesListMap.get("vnd.android.cursor.item/name")).appendNickNames((List) contentValuesListMap.get("vnd.android.cursor.item/nickname")).appendPhones((List) contentValuesListMap.get("vnd.android.cursor.item/phone_v2"), this.mPhoneTranslationCallback).appendEmails((List) contentValuesListMap.get("vnd.android.cursor.item/email_v2")).appendPostals((List) contentValuesListMap.get("vnd.android.cursor.item/postal-address_v2")).appendOrganizations((List) contentValuesListMap.get("vnd.android.cursor.item/organization")).appendWebsites((List) contentValuesListMap.get("vnd.android.cursor.item/website"));
        if ((this.mVCardType & 8388608) == 0) {
            builder.appendPhotos((List) contentValuesListMap.get("vnd.android.cursor.item/photo"));
        }
        builder.appendNotes((List) contentValuesListMap.get("vnd.android.cursor.item/note")).appendEvents((List) contentValuesListMap.get("vnd.android.cursor.item/contact_event")).appendIms((List) contentValuesListMap.get("vnd.android.cursor.item/im")).appendSipAddresses((List) contentValuesListMap.get("vnd.android.cursor.item/sip_address")).appendRelation((List) contentValuesListMap.get("vnd.android.cursor.item/relation")).appendGroupMemberships((List) contentValuesListMap.get("vnd.android.cursor.item/group_membership"), this.mContentResolver, this.mGroupMap);
        return builder.toString();
    }

    public void terminate() {
        closeCursorIfAppropriate();
        this.mTerminateCalled = true;
    }

    private void closeCursorIfAppropriate() {
        if (!this.mCursorSuppliedFromOutside && this.mCursor != null) {
            try {
                this.mCursor.close();
            } catch (SQLiteException e) {
                Log.e("VCardComposer", "SQLiteException on Cursor#close(): " + e.getMessage());
            }
            this.mCursor = null;
        }
    }

    protected void finalize() throws Throwable {
        try {
            if (!this.mTerminateCalled) {
                Log.e("VCardComposer", "finalized() is called before terminate() being called");
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public int getCount() {
        if (this.mCursor != null) {
            return this.mCursor.getCount();
        }
        Log.w("VCardComposer", "This object is not ready yet.");
        return 0;
    }

    public boolean isAfterLast() {
        if (this.mCursor != null) {
            return this.mCursor.isAfterLast();
        }
        Log.w("VCardComposer", "This object is not ready yet.");
        return false;
    }
}
