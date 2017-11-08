package com.android.mms.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.LongSparseArray;
import com.android.mms.LogTag;
import com.android.mms.ui.MessageUtils;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.mms.util.CursorUtils;
import com.huawei.mms.util.HwBackgroundLoader;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class RecipientIdCache {
    private static boolean ENABLE_CONTACTS_FRESH = false;
    private static Uri sAllCanonical = Uri.parse("content://mms-sms/canonical-contact");
    private static RecipientIdCache sInstance;
    private static Uri sSingleCanonicalAddressUri = Uri.parse("content://mms-sms/canonical-address");
    @GuardedBy("this")
    private final LongSparseArray<Entry> mCache = new LongSparseArray();
    private final Context mContext;

    private static class Entry {
        public long cid;
        public Contact contact;
        public long id;
        public String name;
        public String number;

        public Entry(Contact contact) {
            this.id = contact.getRecipientId();
            this.number = contact.getNumber();
            String name = contact.getName();
            if (name == null) {
                name = "";
            }
            this.contact = Contact.get(contact.getPersonId(), this.id, this.number, name);
        }

        public Entry(long id, String number, long cid, String name) {
            this.id = id;
            this.number = number;
            if (name == null) {
                name = "";
            }
            this.cid = cid;
            this.name = name;
            this.contact = null;
        }

        public Contact getContact() {
            if (this.contact == null) {
                this.contact = Contact.get(this.cid, this.id, this.number, this.name);
            }
            return this.contact;
        }

        public void update(long id, String number, long cid, String name) {
            if (this.id == id && this.contact.getPersonId() != cid) {
                this.number = number;
                if (name == null) {
                    name = "";
                }
                this.cid = cid;
                this.name = name;
                this.contact = Contact.get(cid, id, number, name);
            }
        }
    }

    public static RecipientIdCache getInstance() {
        return sInstance;
    }

    public static void init(Context context) {
        sInstance = new RecipientIdCache(context);
    }

    RecipientIdCache(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public static void fill() {
        MLog.i("Mms_threadcache", "[RecipientIdCache] fill: begin");
        synchronized (sInstance) {
            Context context = sInstance.mContext;
        }
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(context, context.getContentResolver(), sAllCanonical, null, null, null, null);
            if (cursor == null) {
                MLog.w("Mms/cache", "null Cursor in fill()");
                if (cursor != null) {
                    cursor.close();
                }
            } else if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
            } else {
                LongSparseArray<Entry> cache;
                synchronized (sInstance) {
                    cache = sInstance.mCache;
                    cache.clear();
                    do {
                        long id = cursor.getLong(0);
                        String number = cursor.getString(1);
                        long cid = cursor.getLong(2);
                        String name = cursor.getString(3);
                        int posIdx = cache.indexOfKey(id);
                        if (posIdx >= 0) {
                            ((Entry) cache.valueAt(posIdx)).update(id, number, cid, name);
                        } else {
                            cache.put(id, new Entry(id, number, cid, name));
                        }
                    } while (cursor.moveToNext());
                    MLog.i("Mms_threadcache", "[RecipientIdCache] fill: finish. Loaded =" + sInstance.mCache.size());
                }
                if (cursor != null) {
                    cursor.close();
                }
                if (ENABLE_CONTACTS_FRESH) {
                    final List<Entry> cachedEntry = new ArrayList();
                    synchronized (sInstance) {
                        cache = sInstance.mCache;
                        int len = cache.size();
                        for (int index = 0; index < len; index++) {
                            cachedEntry.add((Entry) cache.valueAt(index));
                        }
                    }
                    HwBackgroundLoader.getInst().postTaskDelayed(new Runnable() {
                        public void run() {
                            for (Entry e : cachedEntry) {
                                e.getContact();
                            }
                            MLog.i("Mms_threadcache", "[RecipientIdCache] load entry: finish.Loaded >> " + cachedEntry.size());
                            HwBackgroundLoader.getInst().loadDataDelayed(2, 1000);
                        }
                    }, 2000);
                }
            }
        } catch (Exception e) {
            try {
                MLog.e("Mms/cache", "fill database query exception: " + e);
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isContactsDataMatch(String spaceIds, ContactList contacts) {
        if (contacts == null || contacts.size() <= 0 || spaceIds.contains(" ")) {
            return false;
        }
        try {
            long longId = Long.parseLong(spaceIds, 10);
            synchronized (sInstance) {
                Entry entry = (Entry) sInstance.mCache.get(longId);
                if (entry == null || contacts.size() <= 0 || entry.contact != contacts.get(0)) {
                } else {
                    return true;
                }
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static ContactList getSingleContact(String spaceSepId, boolean canBlock) {
        long longId = -1;
        try {
            longId = Long.parseLong(spaceSepId, 10);
        } catch (NumberFormatException e) {
            MLog.e("Mms/cache", "getSingleContact error for: " + spaceSepId);
        }
        ContactList list = new ContactList();
        if (longId == -1) {
            return list;
        }
        synchronized (sInstance) {
            if (sInstance.mCache.indexOfKey(longId) < 0) {
                fill();
                MLog.w("Mms/cache", "Cache hav't initialized !");
            }
            Entry e2 = (Entry) sInstance.mCache.get(longId);
            if (e2 == null) {
                MLog.e("Mms/cache", "Can't get contact for " + spaceSepId);
                return list;
            }
            Contact contact = e2.getContact();
            if (!canBlock) {
                contact.checkAndUpdateContact();
            }
            list.add(contact);
            return list;
        }
    }

    public static ContactList getContacts(String spaceSepIds, boolean canBlock) {
        int i;
        int size;
        boolean mayNotFreshed = true;
        ContactList contacts = new ContactList();
        try {
            synchronized (sInstance) {
                for (String parseLong : spaceSepIds.split(" ")) {
                    long longId = Long.parseLong(parseLong, 10);
                    if (mayNotFreshed && sInstance.mCache.indexOfKey(longId) < 0) {
                        fill();
                        mayNotFreshed = false;
                        MLog.w("Mms/cache", "Cache hav't initialized !");
                    }
                    Entry e = (Entry) sInstance.mCache.get(longId);
                    if (e == null) {
                        MLog.e("Mms/cache", "Can't get contact for " + longId);
                    } else {
                        contacts.add(e.getContact());
                    }
                }
            }
        } catch (NumberFormatException ex) {
            MLog.e("Mms/cache", "NumberFormatException:" + ex.toString());
        }
        if (!canBlock) {
            size = contacts.size();
            for (i = 0; i < size; i++) {
                ((Contact) contacts.get(i)).checkAndUpdateContact();
            }
        }
        return contacts;
    }

    public static void updateNumbers(long threadId, ContactList contacts) {
        for (Contact contact : contacts) {
            if (contact.isNumberModified()) {
                contact.setIsNumberModified(false);
                long recipientId = contact.getRecipientId();
                if (recipientId != 0) {
                    String number1 = contact.getNumber();
                    String formatedNumber = MessageUtils.parseMmsAddress(number1, true);
                    if (!TextUtils.isEmpty(formatedNumber)) {
                        number1 = formatedNumber;
                    }
                    boolean needsDbUpdate = false;
                    synchronized (sInstance) {
                        if (sInstance.mCache.indexOfKey(recipientId) < 0) {
                            MLog.w("Mms/cache", "recipientId is not in the cache");
                            sInstance.mCache.put(recipientId, new Entry(contact));
                            needsDbUpdate = true;
                        } else {
                            Entry entry = (Entry) sInstance.mCache.get(recipientId);
                            if (entry != null) {
                                if (MLog.isLoggable("Mms_app", 2)) {
                                    MLog.d("Mms/cache", "[RecipientIdCache] updateNumbers: recipientId=" + recipientId);
                                }
                                if (!number1.equalsIgnoreCase(entry.number)) {
                                    updateEntryNumber(recipientId, number1);
                                    needsDbUpdate = true;
                                }
                            }
                        }
                        if (needsDbUpdate) {
                            sInstance.updateCanonicalAddressInDb(recipientId, number1, contact);
                        }
                    }
                } else {
                    continue;
                }
            }
        }
    }

    private static void updateEntryNumber(long l, String number) {
        synchronized (sInstance) {
            Entry e = (Entry) sInstance.mCache.get(l);
        }
        if (e != null) {
            e.number = number;
            e.contact = Contact.get(number, false);
        }
    }

    private void updateCanonicalAddressInDb(long id, String number, Contact contact) {
        final ContentValues values = new ContentValues();
        values.put("address", number);
        values.put("CID", Long.valueOf(contact.getPersonId()));
        values.put("NAME", contact.getName());
        final StringBuilder buf = new StringBuilder("_id");
        buf.append('=').append(id);
        final Uri uri = ContentUris.withAppendedId(sSingleCanonicalAddressUri, id);
        HwBackgroundLoader.getInst().postTask(new Runnable() {
            public void run() {
                RecipientIdCache.this.mContext.getContentResolver().update(uri, values, buf.toString(), null);
            }
        });
    }

    public static void dump() {
        synchronized (sInstance) {
            MLog.d("Mms/cache", "*** Recipient ID cache dump ***");
            for (int idx = 0; idx < sInstance.mCache.size(); idx++) {
                MLog.d("Mms/cache", "dump:: " + sInstance.mCache.keyAt(idx));
            }
        }
    }

    public static void canonicalTableDump() {
        Context context;
        MLog.d("Mms/cache", "**** Dump of canoncial_addresses table ****");
        synchronized (sInstance) {
            context = sInstance.mContext;
        }
        Cursor c = SqliteWrapper.query(context, context.getContentResolver(), sAllCanonical, null, null, null, null);
        if (c == null) {
            MLog.w("Mms/cache", "null Cursor in content://mms-sms/canonical-addresses");
            return;
        }
        while (c.moveToNext()) {
            try {
                MLog.d("Mms/cache", "canonicalTableDump::id: " + c.getLong(0));
            } finally {
                c.close();
            }
        }
    }

    public static String getSingleAddressFromCanonicalAddressInDb(Context context, String recipientId) {
        Cursor c = SqliteWrapper.query(context, context.getContentResolver(), ContentUris.withAppendedId(sSingleCanonicalAddressUri, Long.parseLong(recipientId)), null, null, null, null);
        if (c == null) {
            LogTag.warn("Mms/cache", "null Cursor looking up recipient: " + recipientId);
            return null;
        }
        try {
            if (c.moveToFirst()) {
                String number = c.getString(0);
                return number;
            }
            c.close();
            return null;
        } finally {
            c.close();
        }
    }

    public void updateDbCache() {
        HwBackgroundLoader.getInst().postTaskDelayed(new Runnable() {
            public void run() {
                ArrayList<Long> dirtyRecipient;
                synchronized (RecipientIdCache.sInstance) {
                    dirtyRecipient = new ArrayList(RecipientIdCache.sInstance.mCache.size());
                }
                queryDirtyData(dirtyRecipient);
                MLog.d("Mms/cache", "Has " + dirtyRecipient.size() + " dirtyRecipients need update");
                ContentValues cv = new ContentValues();
                for (Long l : dirtyRecipient) {
                    synchronized (RecipientIdCache.sInstance) {
                        Entry v = (Entry) RecipientIdCache.sInstance.mCache.get(l.longValue());
                    }
                    if (v != null) {
                        Contact contact = v.getContact();
                        long cid = contact.getPersonId();
                        if (contact.getPersonId() != 0) {
                            cv.put("NAME", contact.getName());
                            cv.put("CID", Long.valueOf(cid));
                            MLog.v("Mms/cache", "RecipientCache Update for " + l);
                            SqliteWrapper.update(RecipientIdCache.this.mContext, RecipientIdCache.sAllCanonical, cv, "_id=" + l, null);
                        }
                    }
                }
                MLog.d("Mms/cache", "dirtyRecipiend update finish dirty size: " + dirtyRecipient.size());
            }

            private void queryDirtyData(ArrayList<Long> dirtyRecipient) {
                if (RecipientIdCache.sInstance != null) {
                    Cursor c = SqliteWrapper.query(RecipientIdCache.this.mContext, RecipientIdCache.this.mContext.getContentResolver(), RecipientIdCache.sAllCanonical, null, null, null, null);
                    if (c == null) {
                        MLog.w("Mms/cache", "null Cursor in fill()");
                        return;
                    }
                    c = CursorUtils.getFastCursor(c);
                    while (c.moveToNext()) {
                        try {
                            long id = c.getLong(0);
                            long cid = c.getLong(2);
                            String name = c.getString(3);
                            synchronized (RecipientIdCache.sInstance) {
                                Entry entry = (Entry) RecipientIdCache.sInstance.mCache.get(id);
                                if (entry != null) {
                                    Contact contact = entry.getContact();
                                    if (!(contact.getPersonId() == cid && contact.getName().equals(name))) {
                                        dirtyRecipient.add(Long.valueOf(id));
                                    }
                                }
                            }
                        } catch (Throwable th) {
                            c.close();
                        }
                    }
                    c.close();
                }
            }
        }, 10000);
    }

    public static Uri getCanonicalAddressUri() {
        return sAllCanonical;
    }
}
