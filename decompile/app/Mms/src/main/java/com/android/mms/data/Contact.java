package com.android.mms.data;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Profile;
import android.provider.Telephony.Mms;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.LongSparseArray;
import cn.com.xy.sms.sdk.HarassNumberUtil;
import cn.com.xy.sms.sdk.SmartSmsPublicinfoUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.ui.MessageUtils;
import com.android.mms.util.HwCustEcidLookup;
import com.android.mms.util.ItemLoadedCallback;
import com.android.rcs.data.RcsContact;
import com.google.android.gms.R;
import com.google.android.gms.common.Scopes;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cspcommon.ex.ThreadEx.TaskStack;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.service.NameMatchResult;
import com.huawei.mms.ui.CspFragment;
import com.huawei.mms.util.AvatarCache;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwNumberMatchUtils;
import com.huawei.mms.util.NumberUtils;
import com.huawei.mms.util.SmartArchiveSettingUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

@SuppressLint({"NewApi", "NewApi", "NewApi", "NewApi", "NewApi", "NewApi", "NewApi", "NewApi"})
public class Contact implements Runnable {
    public static final boolean IS_CHINA_REGION = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    private static ChangeMoniter mChangeMonitor = new ChangeMoniter();
    private static RcsContact mHwCust = new RcsContact();
    private static HwCustContact mHwCustContact = ((HwCustContact) HwCustUtils.createObj(HwCustContact.class, new Object[0]));
    private static HwCustEcidLookup mHwCustEcidLookup = ((HwCustEcidLookup) HwCustUtils.createObj(HwCustEcidLookup.class, new Object[0]));
    private static ContactsCache sContactCache = null;
    private static Vector<ContactsChangedListener> sContactsChangedListeners = new Vector();
    private static ContentObserver sPresenceObserver;
    private static final String[] ypProjection = new String[]{"_ID", "photouri", "name", HarassNumberUtil.NUMBER, "ypid"};
    private static final Uri ypUri = Uri.parse("content://com.android.contacts.app/yellow_page_data");
    private boolean isUpdateNumber;
    private String mClassifyCode;
    private int mFlag;
    private boolean mIsXyHwNumber;
    private long mKey;
    private long mLoadXyPubInfoTime;
    private String mLookupKey;
    private String mName;
    private String mNumber;
    private String mNumberE164;
    private String mOriginNumber;
    private long mPersonId;
    private String mPurpose;
    private long mRecipientId;
    private String mXiaoyuanPhotoUri;
    private long mYpNumberUriId;
    private String mYpPhotoUri;

    public static class ChangeMoniter {
        private final HashSet<UpdateListener> mListeners = new HashSet();

        public synchronized void addListener(UpdateListener l) {
            this.mListeners.add(l);
        }

        public synchronized void removeListener(UpdateListener l) {
            this.mListeners.remove(l);
        }

        public synchronized void removeListeners() {
            this.mListeners.clear();
            MLog.v("Contact", "Contact-ChangeMoniter all listeners is removed");
        }

        public synchronized void noticeChanged(Contact c) {
            Iterator<UpdateListener> iter = this.mListeners.iterator();
            while (iter.hasNext()) {
                ((UpdateListener) iter.next()).onUpdate(c);
            }
        }
    }

    @SuppressLint({"NewApi", "NewApi"})
    private static class ContactsCache {
        private static String[] CALLER_ID_PROJECTION;
        private static final Uri CALLS_WITH_PRESENCE_URI = Calls.CONTENT_URI_WITH_VOICEMAIL;
        private static String COLLUMN_IS_PRIVACY;
        private static String[] EMAIL_PROJECTION;
        private static final Uri EMAIL_WITH_PRESENCE_URI = Data.CONTENT_URI;
        private static final Uri PHONES_WITH_PRESENCE_URI = Data.CONTENT_URI;
        private static String[] SELF_PROJECTION;
        private static boolean sSupportPriv;
        private HashMap<Long, Contact> mChangedContacts;
        private final LongSparseArray<Contact[]> mContactsHash;
        private final Context mContext;
        private final TaskStack mTaskQueue;

        static {
            COLLUMN_IS_PRIVACY = null;
            CALLER_ID_PROJECTION = null;
            SELF_PROJECTION = null;
            EMAIL_PROJECTION = null;
            sSupportPriv = false;
            if (MmsConfig.isSupportPrivacy()) {
                sSupportPriv = true;
                COLLUMN_IS_PRIVACY = "is_private";
                CALLER_ID_PROJECTION = new String[]{"data1", "display_name", "contact_id", "data4", "send_to_voicemail", COLLUMN_IS_PRIVACY};
                SELF_PROJECTION = new String[]{"display_name", COLLUMN_IS_PRIVACY};
                EMAIL_PROJECTION = new String[]{"data4", "contact_id", "display_name", "send_to_voicemail", COLLUMN_IS_PRIVACY};
            } else {
                sSupportPriv = false;
                CALLER_ID_PROJECTION = new String[]{"data1", "display_name", "contact_id", "data4", "send_to_voicemail"};
                SELF_PROJECTION = new String[]{"display_name"};
                EMAIL_PROJECTION = new String[]{"data4", "contact_id", "display_name", "send_to_voicemail"};
            }
        }

        private ContactsCache(Context context) {
            this.mTaskQueue = new TaskStack("Mms:ContactsCache");
            this.mChangedContacts = new HashMap();
            this.mContactsHash = new LongSparseArray();
            this.mContext = context.getApplicationContext();
        }

        void dump() {
            synchronized (this) {
                MLog.d("Contact", "**** Contact cache dump ****");
                for (int i = 0; i < this.mContactsHash.size(); i++) {
                    for (Contact c : (Contact[]) this.mContactsHash.valueAt(i)) {
                        MLog.d("Contact", this.mContactsHash.keyAt(i) + " ==> Contact cache dump " + c.getPersonId());
                    }
                }
            }
        }

        public void pushTask(Runnable r) {
            this.mTaskQueue.push(r, HwBackgroundLoader.getInst().isInUiThread());
        }

        public Contact getMe(boolean canBlock) {
            return get("Self_Item_Key", true, canBlock);
        }

        public Contact get(String number, boolean canBlock) {
            return get(number, false, canBlock);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void checkContact(Contact contact) {
            synchronized (contact) {
                if (!contact.isStale() || contact.isQueryPending()) {
                } else {
                    contact.setIsStale(false);
                    contact.setQueryPending(true);
                    pushTask(contact);
                }
            }
        }

        private Contact get(String number, boolean isMe, boolean canBlock) {
            if (TextUtils.isEmpty(number)) {
                number = "";
            }
            Contact contact = internalGet(number, isMe);
            Runnable r = null;
            synchronized (contact) {
                while (canBlock) {
                    if (contact.isQueryPending()) {
                        try {
                            contact.wait((long) (HwBackgroundLoader.getInst().isInUiThread() ? 800 : 8000));
                        } catch (InterruptedException e) {
                        }
                    }
                }
                if (Contact.mHwCustEcidLookup != null) {
                    Contact.mHwCustEcidLookup.setContactAsStale(contact, contact.mNumber);
                }
                if (contact.isStale() && !contact.isQueryPending()) {
                    contact.setIsStale(false);
                    Object r2 = contact;
                    contact.setQueryPending(true);
                }
            }
            if (r != null) {
                if (canBlock) {
                    r.run();
                } else {
                    pushTask(r);
                }
            }
            return contact;
        }

        public List<Contact> getContactInfoForPhoneUris(Parcelable[] uris) {
            if (uris.length == 0) {
                return null;
            }
            StringBuilder idsBuilderData = new StringBuilder();
            boolean hasContactData = false;
            StringBuilder idsBuilderCalls = new StringBuilder();
            boolean hasCallLogData = false;
            for (Parcelable p : uris) {
                Uri uri = (Uri) p;
                if (uri != null && "content".equals(uri.getScheme())) {
                    if (uri.toString().startsWith("content://com.android.contacts/data/")) {
                        if (hasContactData) {
                            idsBuilderData.append(',').append(uri.getLastPathSegment());
                        } else {
                            hasContactData = true;
                            idsBuilderData.append(uri.getLastPathSegment());
                        }
                    } else if (uri.toString().startsWith("content://call_log/calls/")) {
                        if (hasCallLogData) {
                            idsBuilderCalls.append(',').append(uri.getLastPathSegment());
                        } else {
                            hasCallLogData = true;
                            idsBuilderCalls.append(uri.getLastPathSegment());
                        }
                    }
                }
            }
            if (!hasContactData && !hasCallLogData) {
                return null;
            }
            List<Contact> entries = new ArrayList();
            if (hasContactData) {
                List<Contact> dataContacts = getContactsFromDataDB(idsBuilderData.toString());
                if (dataContacts != null && dataContacts.size() > 0) {
                    entries.addAll(dataContacts);
                }
            }
            if (hasCallLogData) {
                List<Contact> callsContacts = getContactsFromCallLogDB(idsBuilderCalls.toString());
                if (callsContacts != null && callsContacts.size() > 0) {
                    entries.addAll(callsContacts);
                }
            }
            return entries;
        }

        private List<Contact> getContactsFromDataDB(String whereCause) {
            Cursor cursor = null;
            if (whereCause.length() > 0) {
                try {
                    cursor = SqliteWrapper.query(this.mContext, PHONES_WITH_PRESENCE_URI, CALLER_ID_PROJECTION, "_id IN (" + whereCause + ")", null, null);
                } catch (Exception e) {
                    MLog.e("Contact", "getContactsFromDataDB occur exception when query contact!" + e);
                }
            }
            if (cursor == null) {
                return null;
            }
            List<Contact> dataContacts = new ArrayList();
            while (cursor.moveToNext()) {
                try {
                    String number = MessageUtils.parseMmsAddress(cursor.getString(0), true);
                    if (TextUtils.isEmpty(number)) {
                        number = cursor.getString(0);
                    }
                    Contact entry = new Contact(number, cursor.getString(1));
                    fillPhoneTypeContact(entry, cursor, true);
                    dataContacts.add(checkContactExists(entry));
                } finally {
                    cursor.close();
                }
            }
            return dataContacts;
        }

        private List<Contact> getContactsFromCallLogDB(String whereCause) {
            Cursor cursor = null;
            if (whereCause.length() > 0) {
                String finalWhereCause = "_id IN (" + whereCause + ")";
                try {
                    cursor = SqliteWrapper.query(this.mContext, CALLS_WITH_PRESENCE_URI, new String[]{HarassNumberUtil.NUMBER, "name"}, finalWhereCause, null, null);
                } catch (Exception e) {
                    MLog.e("Contact", "getContactsFromCallLogDB occur exception when query contact!" + e);
                }
            }
            if (cursor == null) {
                return null;
            }
            List<Contact> callsContacts = new ArrayList();
            while (cursor.moveToNext()) {
                try {
                    Contact entry;
                    String number = cursor.getString(0);
                    String name = cursor.getString(1);
                    if (TextUtils.isEmpty(name)) {
                        entry = new Contact(number);
                    } else {
                        entry = new Contact(number, name);
                    }
                    callsContacts.add(entry);
                } finally {
                    cursor.close();
                }
            }
            return callsContacts;
        }

        private Contact checkContactExists(Contact entry) {
            synchronized (this) {
                long key = entry.getKey();
                Contact[] clist = (Contact[]) this.mContactsHash.get(entry.getKey());
                if (clist == null) {
                    this.mContactsHash.put(key, new Contact[]{entry});
                    return entry;
                }
                Contact matchedContact = HwNumberMatchUtils.getMatchedContact(clist, entry.mNumber);
                if (matchedContact == null) {
                    this.mContactsHash.put(key, addNewEntry(clist, entry));
                    return entry;
                }
                synchronized (entry) {
                    entry.mNumber = NumberUtils.formatNumber(entry.mNumber, entry.mNumberE164);
                }
                updateContact(matchedContact, entry, false, true);
                return matchedContact;
            }
        }

        private boolean contactChanged(Contact orig, Contact newContactData) {
            if (orig.mPersonId != newContactData.mPersonId || orig.getSendToVoicemail() != newContactData.getSendToVoicemail()) {
                return true;
            }
            if (!Contact.emptyIfNull(orig.mName).equals(Contact.emptyIfNull(newContactData.mName))) {
                if (MLog.isLoggable("Mms_contact", 3)) {
                    MLog.d("Contact", "contactChanged:: name changed");
                }
                return true;
            } else if (orig.isPrivacyContact() != newContactData.isPrivacyContact()) {
                return true;
            } else {
                return false;
            }
        }

        private void updateContact(Contact c) {
            if (c != null) {
                updateContact(c, getContactInfo(c));
            }
        }

        private void updateContact(Contact src, Contact tag) {
            updateContact(src, tag, false);
        }

        private void updateContact(Contact src, Contact tag, boolean delayedFresh) {
            updateContact(src, tag, delayedFresh, false);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void updateContact(Contact src, Contact tag, boolean delayedFresh, boolean updateNumber) {
            boolean needNotify = false;
            synchronized (tag) {
                int flag = tag.mFlag;
            }
            synchronized (src) {
                if (!updateNumber) {
                    if (!contactChanged(src, tag)) {
                    }
                }
                if (src.isUpdateNumber) {
                    src.mNumber = Contact.isEmailAddress(tag.mNumber) ? tag.mNumber : PhoneNumberUtils.formatNumber(tag.mNumber, tag.mNumberE164, MmsApp.getApplication().getCurrentCountryIso());
                } else {
                    src.mNumber = tag.mNumber;
                }
                src.setIsUpdateNumber(false);
                src.mPersonId = tag.mPersonId;
                src.mNumberE164 = tag.mNumberE164;
                src.mName = tag.mName;
                src.mPurpose = tag.mPurpose;
                src.mClassifyCode = tag.mClassifyCode;
                src.mIsXyHwNumber = tag.mIsXyHwNumber;
                if (!TextUtils.isEmpty(tag.mOriginNumber)) {
                    src.mOriginNumber = tag.mOriginNumber;
                }
                src.mYpPhotoUri = tag.mYpPhotoUri;
                src.mFlag = flag;
                src.mXiaoyuanPhotoUri = tag.mXiaoyuanPhotoUri;
                if (TextUtils.isEmpty(src.mNumber)) {
                    MLog.d("Contact", "UPDATE Contact " + src.mPersonId + "with empty number");
                } else if (delayedFresh) {
                    markAsChanged(src);
                } else if (!TextUtils.isEmpty(src.mNumber)) {
                    needNotify = true;
                }
                src.setQueryPending(false);
                src.notifyAll();
            }
            if (needNotify) {
                Contact.getChangeMoniter().noticeChanged(src);
            }
        }

        private void markAsChanged(Contact c) {
            this.mChangedContacts.put(Long.valueOf(c.getKey()), c);
        }

        private Contact getContactInfo(Contact c) {
            if (c.isMe()) {
                return getContactInfoForSelf();
            }
            if (Contact.isEmailAddress(c.mNumber)) {
                return getContactInfoForEmailAddress(c.mNumber);
            }
            if (isAlphaNumber(c.mNumber)) {
                Contact contact = getContactInfoForEmailAddress(c.mNumber);
                if (contact.existsInDatabase()) {
                    return contact;
                }
                return getContactInfoForPhoneNumber(c.mNumber);
            } else if (Contact.mHwCust == null || !Contact.mHwCust.isGroupID(c.mNumber)) {
                return getContactInfoForPhoneNumber(PhoneNumberUtils.stripSeparators(c.mNumber));
            } else {
                return c;
            }
        }

        private boolean isAlphaNumber(String number) {
            boolean z = true;
            if (!PhoneNumberUtils.isWellFormedSmsAddress(number) || MessageUtils.isAlias(number)) {
                return true;
            }
            number = PhoneNumberUtils.extractNetworkPortion(number);
            if (TextUtils.isEmpty(number)) {
                return true;
            }
            if (number.length() >= 3) {
                z = false;
            }
            return z;
        }

        private Contact getContactInfoForPhoneNumber(String number) {
            Contact entry = new Contact("");
            entry.setNumber(number);
            entry.setOriginNumber(number);
            String normalizedNumber = PhoneNumberUtils.normalizeNumber(number);
            String minMatch = PhoneNumberUtils.toCallerIDMinMatch(normalizedNumber);
            if (!(TextUtils.isEmpty(normalizedNumber) || TextUtils.isEmpty(minMatch))) {
                Cursor cursor = null;
                try {
                    cursor = SqliteWrapper.query(this.mContext, PHONES_WITH_PRESENCE_URI, CALLER_ID_PROJECTION, " Data._ID IN  (SELECT DISTINCT lookup.data_id  FROM  (SELECT data_id, normalized_number, length(normalized_number) as len  FROM phone_lookup  WHERE min_match = ?) AS lookup )", new String[]{minMatch}, null);
                    if (cursor == null || cursor.getCount() == 0) {
                        fillContactByYp(entry, number);
                        if (Contact.mHwCustEcidLookup != null) {
                            Contact.mHwCustEcidLookup.setEcidContactName(this.mContext, normalizedNumber, entry);
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                        return entry;
                    }
                    String compNumber = normalizedNumber;
                    if (Contact.mHwCustContact != null && Contact.mHwCustContact.isPoundCharValid()) {
                        compNumber = Contact.mHwCustContact.setMatchNumber(normalizedNumber, number);
                    }
                    Cursor matchCursor = HwNumberMatchUtils.getMatchedCursor(cursor, compNumber);
                    if (matchCursor != null) {
                        fillPhoneTypeContact(entry, matchCursor, true);
                    } else {
                        fillContactByYp(entry, number);
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    MLog.e("Contact", "getContactInfoForPhoneNumber has exception. " + e);
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            return entry;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void freshAllContactsCache() {
            Throwable th;
            invalidate();
            synchronized (this) {
                try {
                    int size = this.mContactsHash.size();
                    ArrayList<Contact> contacts = new ArrayList((size >> 3) + size);
                    int i = 0;
                    while (i < size) {
                        try {
                            Contact[] alc = (Contact[]) this.mContactsHash.valueAt(i);
                            for (Object add : alc) {
                                contacts.add(add);
                            }
                            i++;
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        }

        @SuppressLint({"NewApi"})
        private Contact getContactInfoForSelf() {
            Contact entry = new Contact(true);
            if (MLog.isLoggable("Mms_contact", 3)) {
                Contact.log("getContactInfoForSelf");
            }
            Cursor cursor = SqliteWrapper.query(this.mContext, Profile.CONTENT_URI, SELF_PROJECTION, null, null, null);
            if (cursor == null) {
                MLog.w("Contact", "getContactInfoForSelf() returned NULL cursor! contact uri used " + Profile.CONTENT_URI);
                return entry;
            }
            try {
                if (cursor.moveToFirst()) {
                    fillSelfContact(entry, cursor);
                }
                cursor.close();
                return entry;
            } catch (Throwable th) {
                cursor.close();
            }
        }

        private void fillPhoneTypeContact(Contact contact, Cursor cursor, boolean loadAvatar) {
            boolean z = true;
            synchronized (contact) {
                boolean z2;
                contact.mName = cursor.getString(1);
                contact.mPersonId = cursor.getLong(2);
                contact.mNumberE164 = cursor.getString(3);
                if (cursor.getInt(4) == 1) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                contact.setSendToVoicemail(z2);
                if (!(sSupportPriv && cursor.getInt(5) == 1)) {
                    z = false;
                }
                contact.setIsPrivacyContact(z);
                contact.setIsYpContact(false);
            }
        }

        private void fillSelfContact(Contact contact, Cursor cursor) {
            boolean z = true;
            synchronized (contact) {
                contact.mName = cursor.getString(0);
                if (!(sSupportPriv && cursor.getInt(1) == 1)) {
                    z = false;
                }
                contact.setIsPrivacyContact(z);
                if (TextUtils.isEmpty(contact.mName)) {
                    contact.mName = this.mContext.getString(R.string.message_sender_from_self);
                }
                if (MLog.isLoggable("Mms_contact", 3)) {
                    Contact.log("fillSelfContact: name and number");
                }
                contact.setIsYpContact(false);
            }
        }

        private boolean fillContactByYp(Contact contact, String number) {
            if (Contact.loadXyPubInfo(this.mContext, number, contact)) {
                return false;
            }
            boolean result = false;
            if (TextUtils.isEmpty(number) || !Contact.IS_CHINA_REGION) {
                return false;
            }
            String tmpNumber = number;
            if (number.startsWith(StringUtils.MPLUG86)) {
                tmpNumber = number.substring(3);
            }
            String ypSelection = "number=?";
            Cursor cursor = null;
            try {
                cursor = SqliteWrapper.query(this.mContext, Contact.ypUri, Contact.ypProjection, "number=?", new String[]{tmpNumber}, null);
                if (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
                    synchronized (contact) {
                        contact.mPersonId = (long) cursor.getInt(0);
                        contact.mYpPhotoUri = cursor.getString(1);
                        contact.mName = cursor.getString(2);
                        contact.setIsYpContact(true);
                    }
                    result = true;
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                try {
                    MLog.e("Contact", "fillContactByYp has exception:  " + e);
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            return result;
        }

        private Contact getContactInfoForEmailAddress(String email) {
            Contact entry = new Contact(email);
            Cursor cursor = SqliteWrapper.query(this.mContext, this.mContext.getContentResolver(), EMAIL_WITH_PRESENCE_URI, EMAIL_PROJECTION, "UPPER(data1)=UPPER(?) AND mimetype='vnd.android.cursor.item/email_v2'", new String[]{email}, null);
            if (cursor != null) {
                boolean found;
                do {
                    try {
                        if (!cursor.moveToNext()) {
                            break;
                        }
                        found = false;
                        synchronized (entry) {
                            boolean z;
                            entry.mPersonId = cursor.getLong(1);
                            if (cursor.getInt(3) == 1) {
                                z = true;
                            } else {
                                z = false;
                            }
                            entry.setSendToVoicemail(z);
                            if (sSupportPriv && cursor.getInt(4) == 1) {
                                z = true;
                            } else {
                                z = false;
                            }
                            entry.setIsPrivacyContact(z);
                            String name = cursor.getString(0);
                            if (TextUtils.isEmpty(name)) {
                                name = cursor.getString(2);
                            }
                            if (!TextUtils.isEmpty(name)) {
                                entry.mName = name;
                                found = true;
                            }
                        }
                    } catch (Throwable th) {
                        cursor.close();
                    }
                } while (!found);
                cursor.close();
            }
            return entry;
        }

        private Contact internalGet(String numberOrEmail, boolean isMe) {
            long key;
            boolean isNotRegularPhoneNumber = !isMe ? Contact.isNotRegularPhoneNumber(numberOrEmail) : true;
            if (Contact.mHwCust != null) {
                isNotRegularPhoneNumber = !isNotRegularPhoneNumber ? Contact.mHwCust.isGroupID(numberOrEmail) : true;
            }
            if (isNotRegularPhoneNumber) {
                key = NumberUtils.emailKey(numberOrEmail);
            } else {
                key = NumberUtils.key(numberOrEmail);
            }
            synchronized (this) {
                Contact c;
                Contact[] candidates = (Contact[]) this.mContactsHash.get(key);
                if (candidates != null && candidates.length > 0) {
                    if (isNotRegularPhoneNumber) {
                        int size = candidates.length;
                        int i = 0;
                        while (i < size) {
                            c = candidates[i];
                            if (c == null) {
                                break;
                            } else if (numberOrEmail.equalsIgnoreCase(c.mNumber)) {
                                return c;
                            } else {
                                i++;
                            }
                        }
                    } else {
                        Contact matchedContact = HwNumberMatchUtils.getMatchedContact(candidates, numberOrEmail);
                        if (matchedContact != null) {
                            return matchedContact;
                        }
                    }
                }
                if (isMe) {
                    c = new Contact(true);
                } else {
                    c = new Contact(numberOrEmail);
                }
                if (isNotRegularPhoneNumber || key != 0) {
                    this.mContactsHash.put(key, addNewEntry(candidates, c));
                    return c;
                }
                c.setIsStale(false);
                return c;
            }
        }

        private static Contact[] removeEntry(Contact[] list, Contact c, boolean regularNumber) {
            if (list == null) {
                return new Contact[0];
            }
            int size = list.length;
            int i = 0;
            int k = 0;
            while (i < size && list[i] != null) {
                int i2;
                int k2;
                if (Contact.matched(list[i], c.mNumber, regularNumber)) {
                    i2 = i + 1;
                    k2 = k;
                } else {
                    k2 = k + 1;
                    i2 = i + 1;
                    list[k] = list[i];
                }
                i = i2;
                k = k2;
            }
            for (i2 = k; i2 < size; i2++) {
                list[i2] = null;
            }
            if (k == 0) {
                list = null;
            }
            return list;
        }

        private static Contact[] addNewEntry(Contact[] list, Contact c) {
            if (list == null) {
                return new Contact[]{c};
            }
            int size = list.length;
            for (int i = 0; i < size; i++) {
                if (list[i] == null) {
                    list[i] = c;
                    return list;
                }
            }
            Contact[] newList = (Contact[]) Arrays.copyOf(list, size + 1);
            newList[size] = c;
            return newList;
        }

        void invalidate() {
            synchronized (this) {
                int cacheSize = this.mContactsHash.size();
                for (int i = 0; i < cacheSize; i++) {
                    Contact[] alc = (Contact[]) this.mContactsHash.valueAt(i);
                    int size = alc == null ? 0 : alc.length;
                    for (int i2 = 0; i2 < size; i2++) {
                        Contact c = alc[i2];
                        if (c == null) {
                            break;
                        }
                        c.setIsStale(true);
                    }
                }
            }
        }

        public void invalidateNumber() {
            synchronized (this) {
                int cacheSize = this.mContactsHash.size();
                for (int i = 0; i < cacheSize; i++) {
                    Contact[] alc = (Contact[]) this.mContactsHash.valueAt(i);
                    int size = alc == null ? 0 : alc.length;
                    for (int i2 = 0; i2 < size; i2++) {
                        Contact c = alc[i2];
                        if (c == null) {
                            break;
                        }
                        c.setIsUpdateNumber(true);
                    }
                }
            }
        }

        private void remove(Contact contact) {
            boolean isNotRegularPhoneNumber;
            long key;
            String number = contact.getNumber();
            if (contact.isMe() || Contact.isEmailAddress(number)) {
                isNotRegularPhoneNumber = true;
            } else {
                isNotRegularPhoneNumber = MessageUtils.isAlias(number);
            }
            if (Contact.mHwCust != null) {
                isNotRegularPhoneNumber = !isNotRegularPhoneNumber ? Contact.mHwCust.isGroupID(number) : true;
            }
            if (isNotRegularPhoneNumber) {
                key = NumberUtils.emailKey(number);
            } else {
                key = NumberUtils.key(number);
            }
            synchronized (this) {
                Contact[] candidates = (Contact[]) this.mContactsHash.get(key);
                Contact[] newCandidates = removeEntry(candidates, contact, !isNotRegularPhoneNumber);
                if (newCandidates == null) {
                    this.mContactsHash.remove(key);
                } else if (newCandidates != candidates) {
                    this.mContactsHash.put(key, newCandidates);
                }
            }
        }

        public void clear() {
            this.mTaskQueue.clear();
            synchronized (this) {
                if (this.mContactsHash != null) {
                    this.mContactsHash.clear();
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        Contact get(long cid, long rid, String number, String name) {
            Throwable th;
            long key = Contact.isEmailAddress(number) ? NumberUtils.emailKey(number) : NumberUtils.key(number);
            if (Contact.mHwCust != null) {
                key = (Contact.isEmailAddress(number) || Contact.mHwCust.isGroupID(number)) ? NumberUtils.emailKey(number) : NumberUtils.key(number);
            }
            synchronized (this) {
                try {
                    if (this.mContactsHash.indexOfKey(key) >= 0) {
                        Contact contact = internalGet(number, false);
                        contact.setRecipientId(rid);
                        if (Contact.mHwCust != null && Contact.mHwCust.needResetContactName(contact, name, number)) {
                            contact.mName = name;
                        }
                    } else {
                        Contact contact2 = new Contact();
                        try {
                            contact2.setRecipientId(rid);
                            contact2.mPersonId = cid;
                            contact2.mName = name;
                            contact2.mNumber = NumberUtils.formatNumber(number, null);
                            contact2.setKey(key);
                            contact2.setIsStale(true);
                            this.mContactsHash.put(key, new Contact[]{contact2});
                            return contact2;
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        Contact getEnterpriseContact(long cid, long rid, String number, String name, String lookupKey) {
            Throwable th;
            long key = Contact.isEmailAddress(number) ? NumberUtils.emailKey(number) : NumberUtils.key(number);
            if (Contact.mHwCust != null) {
                key = (Contact.isEmailAddress(number) || Contact.mHwCust.isGroupID(number)) ? NumberUtils.emailKey(number) : NumberUtils.key(number);
            }
            synchronized (this) {
                try {
                    if (this.mContactsHash.indexOfKey(key) >= 0) {
                        Contact contact = internalGet(number, false);
                        contact.setRecipientId(rid);
                        if (cid != 0) {
                            contact.setContactId(cid);
                            contact.setLookupKey(lookupKey);
                        }
                        if (Contact.mHwCust != null && Contact.mHwCust.needResetContactName(contact, name, number)) {
                            contact.mName = name;
                        }
                    } else {
                        Contact contact2 = new Contact();
                        try {
                            contact2.setRecipientId(rid);
                            contact2.mPersonId = cid;
                            contact2.mName = name;
                            contact2.mNumber = NumberUtils.formatNumber(number, null);
                            contact2.setLookupKey(lookupKey);
                            contact2.setKey(key);
                            contact2.setIsStale(true);
                            this.mContactsHash.put(key, new Contact[]{contact2});
                            return contact2;
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        }
    }

    public interface ContactsChangedListener {
        void onChanged();
    }

    public interface UpdateListener {
        void onUpdate(Contact contact);
    }

    private static android.util.LongSparseArray<java.lang.String> getContactsIdByNameFromDB(android.content.Context r14, java.lang.String r15) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x00a8 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
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
        r0 = java.util.Locale.getDefault();
        r15 = r15.toLowerCase(r0);
        r15 = com.huawei.mms.util.HwMessageUtils.formatSqlString(r15);
        r7 = 0;
        r1 = android.provider.ContactsContract.Contacts.CONTENT_URI;	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r0 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r0.<init>();	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r2 = "LOWER(DISPLAY_NAME) like '";	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r0 = r0.append(r2);	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r0 = r0.append(r15);	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r2 = "%'";	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r0 = r0.append(r2);	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r3 = r0.toString();	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r2 = 0;	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r4 = 0;	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r5 = 0;	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r0 = r14;	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r7 = com.huawei.cspcommon.ex.SqliteWrapper.query(r0, r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        if (r7 == 0) goto L_0x003a;	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
    L_0x0034:
        r0 = r7.getCount();	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        if (r0 != 0) goto L_0x0063;	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
    L_0x003a:
        r1 = "Contact";	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r0 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r0.<init>();	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r2 = "get Contact by name fail.";	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r2 = r0.append(r2);	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        if (r7 != 0) goto L_0x005e;	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
    L_0x004b:
        r0 = -1;	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
    L_0x004c:
        r0 = r2.append(r0);	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r0 = r0.toString();	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        com.huawei.cspcommon.MLog.d(r1, r0);	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r0 = 0;
        if (r7 == 0) goto L_0x005d;
    L_0x005a:
        r7.close();
    L_0x005d:
        return r0;
    L_0x005e:
        r0 = r7.getCount();	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        goto L_0x004c;	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
    L_0x0063:
        r0 = "_id";	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r6 = r7.getColumnIndex(r0);	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r0 = "display_name";	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r12 = r7.getColumnIndex(r0);	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        if (r6 < 0) goto L_0x0075;
    L_0x0073:
        if (r12 >= 0) goto L_0x007c;
    L_0x0075:
        r0 = 0;
        if (r7 == 0) goto L_0x007b;
    L_0x0078:
        r7.close();
    L_0x007b:
        return r0;
    L_0x007c:
        r13 = new android.util.LongSparseArray;	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r13.<init>();	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
    L_0x0081:
        r0 = r7.moveToNext();	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        if (r0 == 0) goto L_0x00aa;	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
    L_0x0087:
        r10 = r7.getLong(r6);	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r8 = r7.getString(r12);	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r0 = 0;	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r0 = (r10 > r0 ? 1 : (r10 == r0 ? 0 : -1));	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        if (r0 <= 0) goto L_0x0081;	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
    L_0x0095:
        r13.put(r10, r8);	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        goto L_0x0081;
    L_0x0099:
        r9 = move-exception;
        r0 = "Contact";	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        r1 = "Error in getContactsIdByNameFromDB.";	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        com.huawei.cspcommon.MLog.d(r0, r1, r9);	 Catch:{ Exception -> 0x0099, all -> 0x00b0 }
        if (r7 == 0) goto L_0x00a8;
    L_0x00a5:
        r7.close();
    L_0x00a8:
        r0 = 0;
        return r0;
    L_0x00aa:
        if (r7 == 0) goto L_0x00af;
    L_0x00ac:
        r7.close();
    L_0x00af:
        return r13;
    L_0x00b0:
        r0 = move-exception;
        if (r7 == 0) goto L_0x00b6;
    L_0x00b3:
        r7.close();
    L_0x00b6:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.mms.data.Contact.getContactsIdByNameFromDB(android.content.Context, java.lang.String):android.util.LongSparseArray<java.lang.String>");
    }

    private static java.util.ArrayList<java.lang.String> getPhoneNnumbersFromDb(android.content.Context r11, long r12) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0057 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
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
        r10 = 0;
        r6 = 0;
        r1 = android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI;	 Catch:{ Exception -> 0x0048, all -> 0x005e }
        r0 = 1;	 Catch:{ Exception -> 0x0048, all -> 0x005e }
        r2 = new java.lang.String[r0];	 Catch:{ Exception -> 0x0048, all -> 0x005e }
        r0 = "data1";	 Catch:{ Exception -> 0x0048, all -> 0x005e }
        r3 = 0;	 Catch:{ Exception -> 0x0048, all -> 0x005e }
        r2[r3] = r0;	 Catch:{ Exception -> 0x0048, all -> 0x005e }
        r3 = "contact_id = ?";	 Catch:{ Exception -> 0x0048, all -> 0x005e }
        r0 = 1;	 Catch:{ Exception -> 0x0048, all -> 0x005e }
        r4 = new java.lang.String[r0];	 Catch:{ Exception -> 0x0048, all -> 0x005e }
        r0 = java.lang.String.valueOf(r12);	 Catch:{ Exception -> 0x0048, all -> 0x005e }
        r5 = 0;	 Catch:{ Exception -> 0x0048, all -> 0x005e }
        r4[r5] = r0;	 Catch:{ Exception -> 0x0048, all -> 0x005e }
        r5 = 0;	 Catch:{ Exception -> 0x0048, all -> 0x005e }
        r0 = r11;	 Catch:{ Exception -> 0x0048, all -> 0x005e }
        r6 = com.huawei.cspcommon.ex.SqliteWrapper.query(r0, r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0048, all -> 0x005e }
        if (r6 == 0) goto L_0x0028;	 Catch:{ Exception -> 0x0048, all -> 0x005e }
    L_0x0022:
        r0 = r6.getCount();	 Catch:{ Exception -> 0x0048, all -> 0x005e }
        if (r0 != 0) goto L_0x002e;
    L_0x0028:
        if (r6 == 0) goto L_0x002d;
    L_0x002a:
        r6.close();
    L_0x002d:
        return r10;
    L_0x002e:
        r9 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0048, all -> 0x005e }
        r9.<init>();	 Catch:{ Exception -> 0x0048, all -> 0x005e }
    L_0x0033:
        r0 = r6.moveToNext();	 Catch:{ Exception -> 0x0048, all -> 0x005e }
        if (r0 == 0) goto L_0x0058;	 Catch:{ Exception -> 0x0048, all -> 0x005e }
    L_0x0039:
        r0 = 0;	 Catch:{ Exception -> 0x0048, all -> 0x005e }
        r8 = r6.getString(r0);	 Catch:{ Exception -> 0x0048, all -> 0x005e }
        r0 = com.huawei.mms.util.HwRecipientUtils.isComplexInvalidRecipient(r8);	 Catch:{ Exception -> 0x0048, all -> 0x005e }
        if (r0 != 0) goto L_0x0033;	 Catch:{ Exception -> 0x0048, all -> 0x005e }
    L_0x0044:
        r9.add(r8);	 Catch:{ Exception -> 0x0048, all -> 0x005e }
        goto L_0x0033;
    L_0x0048:
        r7 = move-exception;
        r0 = "Contact";	 Catch:{ Exception -> 0x0048, all -> 0x005e }
        r1 = "Error in getPhoneNnumbersFromDb.";	 Catch:{ Exception -> 0x0048, all -> 0x005e }
        com.huawei.cspcommon.MLog.d(r0, r1, r7);	 Catch:{ Exception -> 0x0048, all -> 0x005e }
        if (r6 == 0) goto L_0x0057;
    L_0x0054:
        r6.close();
    L_0x0057:
        return r10;
    L_0x0058:
        if (r6 == 0) goto L_0x005d;
    L_0x005a:
        r6.close();
    L_0x005d:
        return r9;
    L_0x005e:
        r0 = move-exception;
        if (r6 == 0) goto L_0x0064;
    L_0x0061:
        r6.close();
    L_0x0064:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.mms.data.Contact.getPhoneNnumbersFromDb(android.content.Context, long):java.util.ArrayList<java.lang.String>");
    }

    private synchronized void setMask(boolean r1, int r2) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.mms.data.Contact.setMask(boolean, int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.mms.data.Contact.setMask(boolean, int):void");
    }

    public static ChangeMoniter getChangeMoniter() {
        return mChangeMonitor;
    }

    public void setIsUpdateNumber(boolean isUpdateNumber) {
        this.isUpdateNumber = isUpdateNumber;
    }

    public static RcsContact getHwCustContact() {
        return mHwCust;
    }

    public synchronized String getOnlyName() {
        if (TextUtils.isEmpty(this.mName)) {
            return "";
        }
        return this.mName;
    }

    public static void addContactsChangedListener(ContactsChangedListener listener) {
        synchronized (sContactsChangedListeners) {
            sContactsChangedListeners.add(listener);
        }
    }

    public static void removeContactsChangedListener(ContactsChangedListener listener) {
        synchronized (sContactsChangedListeners) {
            sContactsChangedListeners.remove(listener);
        }
    }

    public synchronized long getKey() {
        if (this.mKey != 0) {
            return this.mKey;
        } else if (TextUtils.isEmpty(this.mNumber)) {
            return 0;
        } else {
            if (isEmail()) {
                this.mKey = NumberUtils.emailKey(this.mNumber);
            }
            this.mKey = NumberUtils.key(this.mNumber);
            return this.mKey;
        }
    }

    private Contact() {
        this.mLoadXyPubInfoTime = 0;
        this.mIsXyHwNumber = false;
        this.mKey = 0;
        this.isUpdateNumber = false;
    }

    private Contact(String number, String name) {
        this.mLoadXyPubInfoTime = 0;
        this.mIsXyHwNumber = false;
        this.mKey = 0;
        this.isUpdateNumber = false;
        init(number, name);
    }

    private Contact(String number) {
        this.mLoadXyPubInfoTime = 0;
        this.mIsXyHwNumber = false;
        this.mKey = 0;
        this.isUpdateNumber = false;
        init(number, "");
    }

    private Contact(boolean isMe) {
        this.mLoadXyPubInfoTime = 0;
        this.mIsXyHwNumber = false;
        this.mKey = 0;
        this.isUpdateNumber = false;
        init("Self_Item_Key", "");
        setIsMe(isMe);
        setKey(0);
    }

    private synchronized void init(String number, String name) {
        setKey(isEmailAddress(number) ? NumberUtils.emailKey(number) : NumberUtils.key(number));
        this.mName = name;
        setNumber(number);
        setOriginNumber(number);
        this.mPersonId = 0;
        this.mFlag = 0;
        setIsStale(true);
    }

    public String toString() {
        String str = "{ number=%s, name=%s, person_id=%d, hash=%d }";
        Object[] objArr = new Object[4];
        objArr[0] = this.mNumber != null ? "****" : "null";
        objArr[1] = this.mName != null ? "****" : "null";
        objArr[2] = Long.valueOf(this.mPersonId);
        objArr[3] = Integer.valueOf(hashCode());
        return String.format(str, objArr);
    }

    public synchronized boolean isNumberModified() {
        boolean z = false;
        synchronized (this) {
            if ((this.mFlag & 1) != 0) {
                z = true;
            }
        }
        return z;
    }

    public synchronized boolean getSendToVoicemail() {
        boolean z = false;
        synchronized (this) {
            if ((this.mFlag & 16) != 0) {
                z = true;
            }
        }
        return z;
    }

    public void setSendToVoicemail(boolean pending) {
        setMask(pending, 16);
    }

    public void setIsNumberModified(boolean modified) {
        setMask(modified, 1);
    }

    public void setIsStale(boolean stale) {
        setMask(stale, 2);
    }

    public synchronized boolean isStale() {
        boolean z = false;
        synchronized (this) {
            if ((this.mFlag & 2) != 0) {
                z = true;
            }
        }
        return z;
    }

    public synchronized boolean isQueryPending() {
        boolean z = false;
        synchronized (this) {
            if ((this.mFlag & 4) != 0) {
                z = true;
            }
        }
        return z;
    }

    public void setQueryPending(boolean pending) {
        setMask(pending, 4);
    }

    public synchronized String getNumber() {
        return this.mNumber;
    }

    public synchronized long getPersonId() {
        return isYpContact() ? 0 : this.mPersonId;
    }

    public synchronized boolean isYpContact() {
        boolean z = false;
        synchronized (this) {
            if ((this.mFlag & 32) != 0) {
                z = true;
            }
        }
        return z;
    }

    public void setIsYpContact(boolean pending) {
        setMask(pending, 32);
    }

    public synchronized boolean isPrivacyContact() {
        boolean z = false;
        synchronized (this) {
            if ((this.mFlag & 64) != 0) {
                z = true;
            }
        }
        return z;
    }

    public void setIsPrivacyContact(boolean pending) {
        setMask(pending, 64);
    }

    public synchronized boolean isMe() {
        boolean z = false;
        synchronized (this) {
            if ((this.mFlag & 8) != 0) {
                z = true;
            }
        }
        return z;
    }

    public void setIsMe(boolean isMe) {
        setMask(isMe, 8);
    }

    public synchronized long getYpContactId() {
        return isYpContact() ? this.mPersonId : 0;
    }

    public synchronized String getYpPhotoUri() {
        return this.mYpPhotoUri;
    }

    public void reload() {
        setIsStale(true);
        sContactCache.get(getNumber(), false);
    }

    public static Contact get(String number, boolean canBlock) {
        return sContactCache.get(number, false, canBlock);
    }

    public static Contact getMe(boolean canBlock) {
        return sContactCache.getMe(canBlock);
    }

    public void removeFromCache() {
        sContactCache.remove(this);
    }

    public static List<Contact> getByPhoneUris(Parcelable[] uris) {
        return sContactCache.getContactInfoForPhoneUris(uris);
    }

    public static void invalidateCache() {
        if (MLog.isLoggable("Mms_app", 2)) {
            log("invalidateCache");
        }
        sContactCache.invalidate();
    }

    private static String emptyIfNull(String s) {
        return s != null ? s : "";
    }

    public static String formatNameAndNumber(String name, String number) {
        if (TextUtils.isEmpty(name) || name.equals(number)) {
            return "‭" + number + "‬";
        }
        return '‪' + name + '‬' + " ‪<" + number + ">‬";
    }

    public static String formatNumberAndName(String number, String name) {
        if (TextUtils.isEmpty(name) || name.equals(number)) {
            return "‭" + number + "‬";
        }
        return number + '‪' + " (" + name + ")";
    }

    public synchronized void setNumber(String number) {
        if (number.equals(" ")) {
            this.mNumber = number.replace(" ", "");
        } else {
            this.mNumber = NumberUtils.formatNumber(number, this.mNumberE164);
            setIsNumberModified(true);
        }
    }

    public synchronized String getOriginNumber() {
        return this.mOriginNumber;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setOriginNumber(String number) {
        if (!"".equals(number) && !"Self_Item_Key".equalsIgnoreCase(number)) {
            this.mOriginNumber = number;
        }
    }

    public synchronized String getName() {
        if (TextUtils.isEmpty(this.mName)) {
            addQueueForLoadXyPubInfo(this, true);
            return this.mNumber;
        }
        if (!existsInDatabase()) {
            addQueueForLoadXyPubInfo(this, false);
        }
        return this.mName;
    }

    public synchronized void setName(String name) {
        if (!TextUtils.isEmpty(name)) {
            this.mName = name;
        }
    }

    public synchronized String getNameAndNumber() {
        return formatNameAndNumber(this.mName, this.mNumber);
    }

    public synchronized long getRecipientId() {
        return this.mRecipientId;
    }

    public synchronized void setRecipientId(long id) {
        this.mRecipientId = id;
    }

    public synchronized void setContactId(long id) {
        this.mPersonId = id;
    }

    public synchronized void setLookupKey(String lookupKey) {
        this.mLookupKey = lookupKey;
    }

    public synchronized Uri getUri() {
        return ContentUris.withAppendedId(Contacts.CONTENT_URI, this.mPersonId);
    }

    public synchronized Uri getAfwUri(Context context) {
        return Contacts.getLookupUri(this.mPersonId, this.mLookupKey);
    }

    public synchronized boolean existsInDatabase() {
        boolean z = false;
        synchronized (this) {
            if (this.mPersonId > 0 && !isYpContact()) {
                z = true;
            }
        }
        return z;
    }

    public static boolean isProfileUri(Uri uri) {
        boolean z = false;
        if (uri == null || uri.toString() == null) {
            return false;
        }
        if (uri.toString().indexOf(Scopes.PROFILE) != -1) {
            z = true;
        }
        return z;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean isEmail() {
        boolean z = false;
        synchronized (this) {
            if (MessageUtils.isServerAddress(getNumber())) {
                return false;
            } else if (this.mKey > 2147483648L) {
                z = true;
            }
        }
    }

    public static boolean isEmailAddress(String address) {
        boolean z = false;
        if (address == null) {
            return false;
        }
        if (address.indexOf(64) != -1) {
            z = Mms.isEmailAddress(address);
        }
        return z;
    }

    public void checkAndUpdateContact() {
        sContactCache.checkContact(this);
    }

    @SuppressLint({"NewApi"})
    public synchronized Drawable getAvatar(Context context, Drawable defaultValue) {
        if (isYpContact()) {
            return AvatarCache.instance().loadAvatar(context, this.mPersonId, this.mYpPhotoUri, defaultValue, this);
        }
        return AvatarCache.instance().loadAvatar(context, isMe(), this.mPersonId, defaultValue, this);
    }

    public static synchronized void init(Context context) {
        synchronized (Contact.class) {
            if (sContactCache != null) {
                sContactCache.mTaskQueue.interrupt();
            }
            sContactCache = new ContactsCache(context);
            sContactCache.mTaskQueue.start(1);
            if (mHwCustEcidLookup != null) {
                mHwCustEcidLookup.init(context);
            }
        }
    }

    public static synchronized void registerForContactChange(Context context) {
        synchronized (Contact.class) {
            if (sPresenceObserver == null) {
                sPresenceObserver = new ContentObserver(new Handler()) {
                    public void onChange(boolean selfUpdate) {
                        if (MLog.isLoggable("Mms_app", 2)) {
                            Contact.log("presence changed, invalidate cache");
                        }
                        if (Contact.mHwCust != null) {
                            Contact.mHwCust.clearGroupNameCache();
                        }
                        Contact.invalidateCache();
                        AvatarCache.instance().clearCache();
                        CspFragment.setContactChangedWhenPause(true);
                        synchronized (Contact.sContactsChangedListeners) {
                            MLog.i("Contact", "contacts data changed, listener size=" + Contact.sContactsChangedListeners.size());
                            for (ContactsChangedListener listener : Contact.sContactsChangedListeners) {
                                listener.onChanged();
                            }
                        }
                    }
                };
            }
            context.getContentResolver().registerContentObserver(Contacts.CONTENT_URI, true, sPresenceObserver);
            getChangeMoniter().removeListeners();
        }
    }

    public static void freshCache() {
        sContactCache.freshAllContactsCache();
    }

    public static void freshCacheNumber() {
        sContactCache.invalidateNumber();
        freshCache();
    }

    public static void dump() {
        sContactCache.dump();
    }

    public static String getCallerIdSelection() {
        return " Data._ID IN  (SELECT DISTINCT lookup.data_id  FROM  (SELECT data_id, normalized_number, length(normalized_number) as len  FROM phone_lookup  WHERE min_match = ?) AS lookup )";
    }

    public static Uri getPhoneWithUri() {
        return ContactsCache.PHONES_WITH_PRESENCE_URI;
    }

    private static void log(String msg) {
        MLog.d("Contact", msg);
    }

    public static boolean isNotRegularPhoneNumber(String numberOrEmail) {
        if (isEmailAddress(numberOrEmail) || MessageUtils.isAlias(numberOrEmail)) {
            return true;
        }
        return NumberUtils.isMatchedSpecialNumber(numberOrEmail);
    }

    public static void clear(Context context) {
        sContactCache.clear();
        RecipientIdCache.fill();
    }

    public static Contact get(long cid, long rid, String number, String name) {
        return sContactCache.get(cid, rid, number, name);
    }

    public static Contact get(long cid, long rid, String number, String name, String lookup) {
        return sContactCache.getEnterpriseContact(cid, rid, number, name, lookup);
    }

    public static void addListener(UpdateListener l) {
        getChangeMoniter().addListener(l);
    }

    public static void removeListener(UpdateListener l) {
        getChangeMoniter().removeListener(l);
    }

    public synchronized void setKey(long key) {
        this.mKey = key;
    }

    public static LongSparseArray<String> getContactsIdByName(Context context, String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        return getContactsIdByNameFromDB(context, name);
    }

    public static ArrayList<String> getPhoneNnumbers(Context context, long id) {
        return getPhoneNnumbersFromDb(context, id);
    }

    public static void pushTask(Runnable r) {
        sContactCache.pushTask(r);
    }

    public static NameMatchResult getNameMatchedContact(Context context, String name) {
        return getNameMatchedContact(context, name, -1);
    }

    public static NameMatchResult getNameMatchedContact(Context context, String name, long priorId) {
        if (!SmartArchiveSettingUtils.isHuaweiArchiveEnabled(context) || TextUtils.isEmpty(name)) {
            return null;
        }
        name = name.toLowerCase(Locale.getDefault());
        LongSparseArray<String> matches = getContactsIdByName(context, name);
        int len = matches == null ? 0 : matches.size();
        if (len == 0) {
            MLog.d("Contact", "not find matched contact");
            return null;
        }
        NameMatchResult result = new NameMatchResult();
        result.contactId = -1;
        result.contactName = "";
        for (int i = 0; i < len; i++) {
            long contactId = matches.keyAt(i);
            String contactName = (String) matches.valueAt(i);
            int type = getMatchType(contactName, name);
            if (type != 0) {
                if (result.contactName == null || result.contactName.length() > contactName.length()) {
                    result.contactId = contactId;
                    result.contactName = contactName;
                }
                if (priorId != -1 || type != 9) {
                    if (priorId == contactId) {
                    }
                }
                MLog.d("Contact", "find matched contact for " + priorId);
                return result;
            }
        }
        if (result.contactId == -1) {
            result = null;
        }
        return result;
    }

    private static int getMatchType(String oName, String targ) {
        if (TextUtils.isEmpty(oName)) {
            return 0;
        }
        oName = oName.toLowerCase(Locale.getDefault());
        if (oName.equals(targ)) {
            return 9;
        }
        if (oName.startsWith(targ) && oName.charAt(targ.length()) == ' ') {
            return 3;
        }
        return 0;
    }

    public int getRelatedContactId(Context context, String mimeType) {
        int id = -1;
        Cursor cursor = null;
        try {
            Context context2 = context;
            cursor = SqliteWrapper.query(context2, ContactsCache.PHONES_WITH_PRESENCE_URI, new String[]{"_id"}, " contact_id=? and mimetype=? ", new String[]{String.valueOf(this.mPersonId), mimeType}, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
                id = cursor.getInt(cursor.getColumnIndex("_id"));
            }
            MLog.i("Mms_contact", "Query of " + mimeType + " return id:" + id);
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException e) {
            MLog.e("Contact", "query related contact info cause SQLiteException for:" + this.mPersonId, "SQLiteException:" + e.toString());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e2) {
            MLog.e("Contact", "query related contact info failed for:" + this.mPersonId, "Exception:" + e2.toString());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return id;
    }

    private static boolean matched(Contact tag, String number, boolean regularNumber) {
        if (regularNumber) {
            if (PhoneNumberUtils.compare(number, tag.mNumber)) {
                return true;
            }
        } else if (number.equalsIgnoreCase(tag.mNumber)) {
            return true;
        }
        return false;
    }

    public void run() {
        sContactCache.updateContact(this);
    }

    public synchronized boolean isXiaoyuanContact() {
        boolean z = false;
        synchronized (this) {
            if ((this.mFlag & 128) != 0) {
                z = true;
            }
        }
        return z;
    }

    public void setIsXiaoyuanContact(boolean pending) {
        setMask(pending, 128);
    }

    public synchronized String getXiaoyuanPhotoUri() {
        return this.mXiaoyuanPhotoUri;
    }

    public synchronized void setXiaoyuanPhotoUri(String xiaoyuanPhotoUri) {
        this.mXiaoyuanPhotoUri = xiaoyuanPhotoUri;
    }

    public void setPurpose(String purpose) {
        this.mPurpose = purpose;
    }

    public String getPurpose() {
        return this.mPurpose;
    }

    public String getClassifyCode() {
        return this.mClassifyCode;
    }

    public void setClassifyCode(String classifyCode) {
        this.mClassifyCode = classifyCode;
    }

    public void refreshContact(Contact src, Contact tag, boolean refresh) {
        sContactCache.updateContact(src, tag, false, refresh);
    }

    private static void addQueueForLoadXyPubInfo(final Contact contact, final boolean highLevel) {
        if (MmsConfig.getSupportSmartSmsFeature()) {
            HwBackgroundLoader.getInst().postTask(new Runnable() {
                public void run() {
                    SmartSmsPublicinfoUtil.addNeedLoadContact(contact, highLevel);
                }
            });
        }
    }

    private static boolean loadXyPubInfo(Context context, String number, Contact contact) {
        if (MmsConfig.getSupportSmartSmsFeature()) {
            try {
                contact.setLoadXyPubInfoTime(System.currentTimeMillis());
                return SmartSmsPublicinfoUtil.loadPublicInfoByPhoneNumber(context, number, contact);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public synchronized long getLoadXyPubInfoTime() {
        return this.mLoadXyPubInfoTime;
    }

    public synchronized void setLoadXyPubInfoTime(long loadTime) {
        this.mLoadXyPubInfoTime = loadTime;
    }

    public boolean isXyHwNumber() {
        return this.mIsXyHwNumber;
    }

    public void setIsXyHwNumber(boolean isXyHwNumber) {
        this.mIsXyHwNumber = isXyHwNumber;
    }

    public boolean judgeYpContact(Contact contact, String number, Context context) {
        boolean result = false;
        if (TextUtils.isEmpty(number) || !IS_CHINA_REGION) {
            return false;
        }
        String tmpNumber = number;
        if (number.startsWith(StringUtils.MPLUG86)) {
            tmpNumber = number.substring(3);
        }
        String ypSelection = "number=?";
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(context, ypUri, ypProjection, "number=?", new String[]{tmpNumber}, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
                synchronized (contact) {
                    contact.mPersonId = (long) cursor.getInt(0);
                    contact.mYpPhotoUri = cursor.getString(1);
                    contact.mName = cursor.getString(2);
                    contact.mYpNumberUriId = cursor.getLong(cursor.getColumnIndexOrThrow("ypid"));
                    contact.setIsYpContact(true);
                }
                result = true;
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            try {
                MLog.e("Contact", "judgeYpContact has exception:  " + e);
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return result;
    }

    public synchronized long getYellowPageNumberUriId() {
        return isYpContact() ? this.mYpNumberUriId : -1;
    }

    public void parseAvatarImage(Context context, Drawable defaultAvtar, ItemLoadedCallback<Drawable> loadedCallback, boolean isSquare, boolean isNotification, boolean isNotificationSms) {
        if (MmsConfig.getSupportSmartSmsFeature() && isNotificationSms) {
            SmartSmsPublicinfoUtil.setDrawableByLogoName(context, getXiaoyuanPhotoUri(), getClassifyCode(), isSquare, loadedCallback, SmartSmsPublicinfoUtil.getAvatartWidthHeight(context), SmartSmsPublicinfoUtil.getAvatartWidthHeight(context), isNotification);
        } else if (!isYpContact() || TextUtils.isEmpty(getYpPhotoUri())) {
            AvatarCache.instance().setAvatar(context, isMe(), getPersonId(), isSquare, defaultAvtar, (ItemLoadedCallback) loadedCallback, this);
        } else {
            AvatarCache.instance().setAvatar(context, getYpContactId(), getYpPhotoUri(), isSquare, defaultAvtar, (ItemLoadedCallback) loadedCallback, this);
        }
    }
}
