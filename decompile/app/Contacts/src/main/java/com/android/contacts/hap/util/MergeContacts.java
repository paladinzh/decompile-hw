package com.android.contacts.hap.util;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorEntityIterator;
import android.content.Entity;
import android.content.Entity.NamedContentValues;
import android.content.EntityIterator;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.AggregationExceptions;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.TextUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

public class MergeContacts {
    static final List<Long> mJoinContacts = Lists.newArrayList();
    private static ProgressAsyncTask mMergeOperation;
    private static int mMergeType = 0;
    static ArrayList<ContentProviderOperation> mOperations = new ArrayList();
    static final StringBuilder mSb = new StringBuilder();
    static final ContentValues mValues = new ContentValues();
    private static List<MergeFinishNotification> sCallbacks = new CopyOnWriteArrayList();
    private static long stime = 0;

    public interface MergeFinishNotification {
        void onFinished();

        void onProgress(int i, int i2);
    }

    private static class EntityDeltaMod {
        private final HashMap<String, ArrayList<ValuesDeltaMod>> mEntries = Maps.newHashMap();
        private ValuesDeltaMod mValuesDelta;

        public String toString() {
            MergeContacts.mSb.setLength(0);
            if (this.mValuesDelta != null) {
                MergeContacts.mSb.append(this.mValuesDelta.getValues().toString());
            }
            if (this.mEntries != null) {
                for (ArrayList<ValuesDeltaMod> e : this.mEntries.values()) {
                    for (ValuesDeltaMod f : e) {
                        MergeContacts.mSb.append(f.toString());
                    }
                }
            }
            return MergeContacts.mSb.toString();
        }

        public static EntityDeltaMod fromEntity(Entity in) {
            EntityDeltaMod entity = new EntityDeltaMod();
            entity.mValuesDelta = ValuesDeltaMod.fromValues(in.getEntityValues());
            for (NamedContentValues namedValues : in.getSubValues()) {
                entity.addEntry(ValuesDeltaMod.fromValues(namedValues.values));
            }
            return entity;
        }

        public ValuesDeltaMod addEntry(ValuesDeltaMod entry) {
            getMimeEntries(entry.getDataMimetype()).add(entry);
            return entry;
        }

        private ArrayList<ValuesDeltaMod> getMimeEntries(String mimeType) {
            ArrayList<ValuesDeltaMod> mimeEntries = (ArrayList) this.mEntries.get(mimeType);
            if (mimeEntries != null) {
                return mimeEntries;
            }
            mimeEntries = Lists.newArrayList();
            this.mEntries.put(mimeType, mimeEntries);
            return mimeEntries;
        }

        public ValuesDeltaMod getValues() {
            return this.mValuesDelta;
        }

        public static ArrayList<ContentProviderOperation> mergeRawContactsFromSameAccounts(List<EntityDeltaMod> list, Context context) {
            EntityDeltaMod diffEntity = new EntityDeltaMod();
            ContentValues diffValues = new ContentValues();
            boolean allDataMatch = true;
            int totalDiffCount = 0;
            ArrayList<ContentProviderOperation> buildInto = new ArrayList();
            int bestPhotoIndex = computeBestPhotoIndex(list);
            if (bestPhotoIndex > 0 && bestPhotoIndex < list.size()) {
                try {
                    EntityDeltaMod bestPhotoOne = (EntityDeltaMod) list.get(bestPhotoIndex);
                    list.set(bestPhotoIndex, (EntityDeltaMod) list.get(0));
                    list.set(0, bestPhotoOne);
                } catch (Throwable e) {
                    HwLog.e("EntityDeltaMod", e.getMessage(), e);
                }
            }
            int loopIndex = 0;
            for (EntityDeltaMod entries : list) {
                totalDiffCount += MergeContacts.mergeRawContactValues(diffValues, entries.getValues().getCompleteValues(), loopIndex);
                if (loopIndex != 0) {
                    Builder builder = ContentProviderOperation.newDelete(RawContacts.CONTENT_URI);
                    builder.withSelection("_id=?", new String[]{entries.mValuesDelta.getId() + ""});
                    buildInto.add(builder.build());
                    for (Entry<String, ArrayList<ValuesDeltaMod>> entry : entries.mEntries.entrySet()) {
                        if (!("vnd.android.cursor.item/name".equals(entry.getKey()) || "vnd.android.cursor.item/photo".equals(entry.getKey()))) {
                            for (ValuesDeltaMod value : (ArrayList) entry.getValue()) {
                                diffEntity.addEntry(ValuesDeltaMod.fromValues(new ContentValues(value.mData)));
                            }
                        }
                    }
                }
                loopIndex++;
            }
            EntityDeltaMod current = (EntityDeltaMod) list.get(0);
            for (Entry<String, ArrayList<ValuesDeltaMod>> mimeEntriesWithType : diffEntity.mEntries.entrySet()) {
                String mimeType = (String) mimeEntriesWithType.getKey();
                String[] columns = MergeContacts.getDataColumnsForMime(mimeType);
                boolean canHaveMore = MergeContacts.CanHaveMultipleValues(mimeType);
                for (ValuesDeltaMod diffEntryMimes : (ArrayList) mimeEntriesWithType.getValue()) {
                    boolean foundDataMatch = false;
                    if (!"vnd.android.cursor.item/name".equals(mimeType)) {
                        ArrayList<ValuesDeltaMod> currentData;
                        if (current.mEntries.get(mimeType) == null) {
                            currentData = null;
                        } else {
                            currentData = Lists.newArrayList((Iterable) current.mEntries.get(mimeType));
                        }
                        if (currentData != null) {
                            if (!canHaveMore) {
                                break;
                            }
                            for (ValuesDeltaMod currentEntry : currentData) {
                                boolean foundColumnsMatch = false;
                                if (columns == null) {
                                    foundDataMatch = true;
                                } else {
                                    for (String column : columns) {
                                        Object currentValue = MergeContacts.getNormalizedData(mimeType, column, currentEntry.mData.get(column));
                                        Object diffValue = MergeContacts.getNormalizedData(mimeType, column, diffEntryMimes.mData.get(column));
                                        if (diffValue == null && currentValue == null) {
                                            foundColumnsMatch = true;
                                        } else if (diffValue == null || currentValue == null || !diffValue.equals(currentValue)) {
                                            if (MergeContacts.isAllowedToCopyMultipleEntryWithSameType(mimeType, diffEntryMimes.mData, currentEntry.mData)) {
                                                foundColumnsMatch = false;
                                                break;
                                            }
                                        } else {
                                            foundColumnsMatch = true;
                                        }
                                    }
                                    if (foundColumnsMatch) {
                                        foundDataMatch = true;
                                        break;
                                    }
                                }
                            }
                            if (!foundDataMatch) {
                                allDataMatch = false;
                                current.addEntry(ValuesDeltaMod.fromValues(new ContentValues(diffEntryMimes.mData)));
                            }
                        } else {
                            if (((ArrayList) diffEntity.mEntries.get(mimeType)) != null) {
                                allDataMatch = false;
                            }
                            if (columns == null) {
                                break;
                            }
                            current.addEntry(ValuesDeltaMod.fromValues(new ContentValues(diffEntryMimes.mData)));
                        }
                    } else {
                        current.mEntries.remove(mimeType);
                        current.addEntry(ValuesDeltaMod.fromValues(new ContentValues(diffEntryMimes.mData)));
                    }
                }
            }
            if (totalDiffCount > 0) {
                allDataMatch = false;
            }
            if (allDataMatch) {
                return buildInto;
            }
            MergeContacts.mValues.clear();
            MergeContacts.mValues.putAll(current.mValuesDelta.getValues());
            MergeContacts.mValues.putAll(diffValues);
            current.mValuesDelta.getValues().putAll(MergeContacts.mValues);
            MergeContacts.mValues.remove("contact_id");
            MergeContacts.mValues.remove("_id");
            if (!allDataMatch) {
                builder = ContentProviderOperation.newUpdate(Contacts.CONTENT_URI);
                builder.withValues(MergeContacts.mValues);
                builder.withSelection("_id=?", new String[]{current.mValuesDelta.getContactId() + ""});
                buildInto.add(builder.build());
                for (Entry<String, ArrayList<ValuesDeltaMod>> mimeEntriesWithType2 : current.mEntries.entrySet()) {
                    for (ValuesDeltaMod data : (ArrayList) mimeEntriesWithType2.getValue()) {
                        if (current.mValuesDelta.getId().longValue() != data.getDataContactId()) {
                            if (data.getDataMimetype().equals("vnd.android.cursor.item/name")) {
                                builder = ContentProviderOperation.newDelete(Data.CONTENT_URI);
                                builder.withSelection("raw_contact_id=? AND mimetype= ?", new String[]{current.mValuesDelta.getId() + "", "vnd.android.cursor.item/name"});
                                buildInto.add(builder.build());
                            }
                            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                            MergeContacts.mValues.clear();
                            MergeContacts.mValues.putAll(data.getValues());
                            MergeContacts.mValues.remove("_id");
                            builder.withValues(MergeContacts.mValues);
                            builder.withValue("raw_contact_id", current.mValuesDelta.getId());
                            data.getValues().put("raw_contact_id", current.mValuesDelta.getId());
                            buildInto.add(builder.build());
                        }
                    }
                }
            }
            return buildInto;
        }

        private static int computeBestPhotoIndex(List<EntityDeltaMod> list) {
            int loopIndex = 0;
            int bestThumbNailIndex = -1;
            for (EntityDeltaMod entries : list) {
                for (Entry<String, ArrayList<ValuesDeltaMod>> entry : entries.mEntries.entrySet()) {
                    if ("vnd.android.cursor.item/photo".equals(entry.getKey())) {
                        for (ValuesDeltaMod value : (ArrayList) entry.getValue()) {
                            if (value.mData.containsKey("data15")) {
                                if (value.mData.containsKey("data14")) {
                                    return loopIndex;
                                }
                                if (bestThumbNailIndex == -1) {
                                    bestThumbNailIndex = loopIndex;
                                }
                            }
                        }
                        continue;
                    }
                }
                loopIndex++;
            }
            if (bestThumbNailIndex < 0) {
                bestThumbNailIndex = 0;
            }
            return bestThumbNailIndex;
        }
    }

    private static class EntityIteratorImpl extends CursorEntityIterator {
        private static final String[] DATA_KEYS = new String[]{"data1", "data2", "data3", "data4", "data5", "data6", "data7", "data8", "data9", "data10", "data11", "data12", "data13", "data14", "data15", "data_sync1", "data_sync2", "data_sync3", "data_sync4"};

        public EntityIteratorImpl(Cursor cursor) {
            super(cursor);
        }

        public Entity getEntityAndIncrementCursor(Cursor cursor) {
            int columnRawContactId = cursor.getColumnIndexOrThrow("_id");
            long rawContactId = cursor.getLong(columnRawContactId);
            ContentValues cv = new ContentValues();
            DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "account_name");
            DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "account_type");
            DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "data_set");
            DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, "_id");
            DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, "contact_id");
            DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, "starred");
            DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, "last_time_contacted");
            DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, "times_contacted");
            DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "custom_ringtone");
            DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, "send_to_voicemail");
            DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, "is_private");
            Entity contact = new Entity(cv);
            while (rawContactId == cursor.getLong(columnRawContactId)) {
                cv = new ContentValues();
                cv.put("_id", Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("data_id"))));
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "res_package");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "mimetype");
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, "is_primary");
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, "is_super_primary");
                for (String key : DATA_KEYS) {
                    int columnIndex = cursor.getColumnIndexOrThrow(key);
                    switch (cursor.getType(columnIndex)) {
                        case 0:
                            break;
                        case 1:
                        case 2:
                        case 3:
                            cv.put(key, cursor.getString(columnIndex));
                            break;
                        case 4:
                            cv.put(key, cursor.getBlob(columnIndex));
                            break;
                        default:
                            throw new IllegalStateException("Invalid or unhandled data type");
                    }
                }
                cv.put("raw_contact_id", Long.valueOf(rawContactId));
                contact.addSubValue(Data.CONTENT_URI, cv);
                if (!cursor.moveToNext()) {
                    return contact;
                }
            }
            return contact;
        }
    }

    public static class ProgressAsyncTask extends AsyncTask<HashSet<ArrayList<Long>>, Integer, Void> {
        Context mContext;
        int mTotalContacts;

        public ProgressAsyncTask(Context context, int aTotalContacts) {
            this.mTotalContacts = aTotalContacts;
            this.mContext = context;
        }

        protected void onProgressUpdate(Integer... values) {
            for (MergeFinishNotification callback : MergeContacts.sCallbacks) {
                callback.onProgress(values[0].intValue(), this.mTotalContacts);
            }
        }

        protected Void doInBackground(HashSet<ArrayList<Long>>... params) {
            if (HwLog.HWDBG) {
                HwLog.d("MergeContacts", "mergeRawContacts do in background....");
                MergeContacts.stime = 0;
            }
            publishProgress(new Integer[]{Integer.valueOf(0)});
            int currentCount = 0;
            for (ArrayList<Long> mergeIds : params[0]) {
                List<Long> ids = Lists.newArrayList();
                for (Long longValue : mergeIds) {
                    ids.add(Long.valueOf(longValue.longValue()));
                }
                ArrayList<EntityDeltaMod> state = MergeContacts.mergeRawContacts(this.mContext, (List) ids);
                currentCount++;
                publishProgress(new Integer[]{Integer.valueOf(currentCount)});
                if (state != null) {
                    state.clear();
                }
            }
            try {
                if (MergeContacts.mOperations.size() > 0) {
                    ArrayList<ContentProviderOperation> lOperations = new ArrayList();
                    lOperations.addAll(MergeContacts.mOperations);
                    if (HwLog.HWDBG) {
                        long start = System.currentTimeMillis();
                        this.mContext.getApplicationContext().getContentResolver().applyBatch("com.android.contacts", lOperations);
                        MergeContacts.stime = MergeContacts.stime + (System.currentTimeMillis() - start);
                    } else {
                        this.mContext.getApplicationContext().getContentResolver().applyBatch("com.android.contacts", lOperations);
                    }
                    MergeContacts.mOperations.clear();
                }
            } catch (RemoteException e) {
                HwLog.w("MergeContacts", "RemoteException");
            } catch (OperationApplicationException e2) {
                HwLog.w("MergeContacts", "OperationApplicationException");
            }
            if (HwLog.HWDBG) {
                HwLog.d("MergeContacts", "complete merge, sending update fav. widget broadcast!!");
            }
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                public void run() {
                    CommonUtilMethods.updateFavoritesWidget(ProgressAsyncTask.this.mContext);
                }
            }, 300);
            if (HwLog.HWDBG) {
                HwLog.d("MergeContacts", "mergeRawContacts finished in background...., applybatch time is " + MergeContacts.stime + " ms");
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            for (MergeFinishNotification callback : MergeContacts.sCallbacks) {
                callback.onFinished();
            }
            MergeContacts.mMergeOperation = null;
            MergeContacts.setMergeType(0);
        }
    }

    private static class ValuesDeltaMod {
        private ContentValues mData;

        public String toString() {
            return this.mData.toString();
        }

        protected ValuesDeltaMod() {
        }

        public static ValuesDeltaMod fromValues(ContentValues data) {
            ValuesDeltaMod entry = new ValuesDeltaMod();
            entry.mData = data;
            return entry;
        }

        public ContentValues getValues() {
            return this.mData;
        }

        public String getDataMimetype() {
            return this.mData.getAsString("mimetype");
        }

        public long getDataContactId() {
            return this.mData.getAsLong("raw_contact_id").longValue();
        }

        public Account getAccountInfo() {
            String accountName = this.mData.getAsString("account_name");
            String accountType = this.mData.getAsString("account_type");
            if (TextUtils.isEmpty(accountName) || TextUtils.isEmpty(accountType)) {
                return null;
            }
            return new Account(accountName, accountType);
        }

        public Long getId() {
            return this.mData.getAsLong("_id");
        }

        public Long getContactId() {
            return this.mData.getAsLong("contact_id");
        }

        public ContentValues getCompleteValues() {
            ContentValues values = new ContentValues();
            if (this.mData != null) {
                values.putAll(this.mData);
            }
            return values;
        }
    }

    private static void createEntityList(Context context, List<Long> ids, ArrayList<EntityDeltaMod> state) {
        mSb.setLength(0);
        mSb.append("contact_id IN (");
        for (Long rawContact : ids) {
            mSb.append(rawContact).append(",");
        }
        mSb.setLength(mSb.length() - 1);
        mSb.append(")");
        Cursor cursor = context.getApplicationContext().getContentResolver().query(Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "raw_contact_entities_hw"), null, mSb.toString(), null, "account_name,account_type");
        if (cursor != null) {
            EntityIterator iterator = newEntityIterator(cursor);
            while (iterator.hasNext()) {
                try {
                    state.add(EntityDeltaMod.fromEntity((Entity) iterator.next()));
                } finally {
                    iterator.close();
                }
            }
        }
    }

    private static EntityIterator newEntityIterator(Cursor cursor) {
        return new EntityIteratorImpl(cursor);
    }

    public static void registerMergeCallback(MergeFinishNotification callback) {
        if (callback != null) {
            sCallbacks.add(callback);
        }
    }

    public static void unRegisterMergeCallback(MergeFinishNotification callback) {
        sCallbacks.remove(callback);
    }

    public static void mergeRawContacts(Context context, HashSet<ArrayList<Long>> in, int aTotalContacts, int aMerge) {
        if (HwLog.HWDBG) {
            HwLog.d("MergeContacts", "mergeRawContacts begin .... merge mode:" + aMerge);
        }
        if (mMergeOperation == null) {
            mMergeOperation = new ProgressAsyncTask(context, aTotalContacts);
            mMergeOperation.execute(new HashSet[]{new HashSet(in)});
            setMergeType(aMerge);
            return;
        }
        HwLog.e("MergeContacts", "Merge already in progress!!! Ideally this case should not occur");
    }

    private static void setMergeType(int aMergeType) {
        mMergeType = aMergeType;
    }

    public static int getMergeType() {
        return mMergeType;
    }

    public static ArrayList<EntityDeltaMod> mergeRawContacts(Context context, ArrayList<EntityDeltaMod> state) {
        if (state.size() < 2) {
            return null;
        }
        int startIndex = 0;
        boolean hasContactsToPurge = false;
        ArrayList<Integer> backreferences = new ArrayList(5);
        backreferences.add(Integer.valueOf(0));
        Account account = ((EntityDeltaMod) state.get(0)).mValuesDelta.getAccountInfo();
        int endIndex = 1;
        do {
            if (account == null || !account.equals(((EntityDeltaMod) state.get(endIndex)).mValuesDelta.getAccountInfo())) {
                if (hasContactsToPurge) {
                    hasContactsToPurge = false;
                    doSave(context, EntityDeltaMod.mergeRawContactsFromSameAccounts(state.subList(startIndex, endIndex), context));
                }
                account = ((EntityDeltaMod) state.get(endIndex)).mValuesDelta.getAccountInfo();
                startIndex = endIndex;
                backreferences.add(Integer.valueOf(endIndex));
            } else {
                hasContactsToPurge = true;
            }
            endIndex++;
        } while (endIndex < state.size());
        if (hasContactsToPurge && startIndex < state.size()) {
            doSave(context, EntityDeltaMod.mergeRawContactsFromSameAccounts(state.subList(startIndex, endIndex), context));
        }
        if (backreferences.size() > 1) {
            ArrayList<EntityDeltaMod> backRefs = Lists.newArrayList();
            for (int i = 0; i < backreferences.size(); i++) {
                if (HwLog.HWDBG) {
                    HwLog.v("MergeContacts", "the index of back reference :" + backreferences.get(i));
                }
                backRefs.add((EntityDeltaMod) state.get(((Integer) backreferences.get(i)).intValue()));
            }
            mJoinContacts.clear();
            boolean needJoin = false;
            long contactId = ((EntityDeltaMod) backRefs.get(0)).mValuesDelta.getContactId().longValue();
            for (int j = 1; j < backRefs.size(); j++) {
                if (((EntityDeltaMod) backRefs.get(j)).mValuesDelta.getContactId().longValue() != contactId) {
                    needJoin = true;
                    break;
                }
            }
            if (needJoin) {
                for (int k = 0; k < backRefs.size(); k++) {
                    mJoinContacts.add(((EntityDeltaMod) backRefs.get(k)).mValuesDelta.getId());
                }
            }
            joinContactsFromDifferentAccounts(context);
        }
        return state;
    }

    private static ArrayList<EntityDeltaMod> mergeRawContacts(Context context, List<Long> ids) {
        if (ids.size() == 0) {
            throw new IllegalArgumentException("length is zero");
        }
        ArrayList state = Lists.newArrayList();
        createEntityList(context, ids, state);
        return mergeRawContacts(context, state);
    }

    private static void doSave(Context context, ArrayList<ContentProviderOperation> operations) {
        mOperations.addAll(operations);
        doBatchOperations(context, mOperations);
    }

    private static void joinContactsFromDifferentAccounts(Context context) {
        for (int i = 0; i < mJoinContacts.size() - 1; i++) {
            Long id1 = (Long) mJoinContacts.get(i);
            for (int j = i + 1; j < mJoinContacts.size(); j++) {
                Long id2 = (Long) mJoinContacts.get(j);
                mValues.clear();
                mValues.put("type", Integer.valueOf(1));
                mValues.put("raw_contact_id1", id1);
                mValues.put("raw_contact_id2", id2);
                Builder builder = ContentProviderOperation.newUpdate(AggregationExceptions.CONTENT_URI);
                builder.withValues(mValues);
                mOperations.add(builder.build());
                doBatchOperations(context, mOperations);
            }
        }
    }

    private static void doBatchOperations(Context context, ArrayList<ContentProviderOperation> operations) {
        ContentResolver resolver = context.getApplicationContext().getContentResolver();
        ArrayList<ContentProviderOperation> lOperations = new ArrayList();
        int size = operations.size();
        if (size >= 100) {
            int startIndex = 0;
            int endIndex = 100;
            do {
                try {
                    lOperations.clear();
                    lOperations.addAll(operations.subList(startIndex, endIndex));
                    if (HwLog.HWDBG) {
                        long start = System.currentTimeMillis();
                        resolver.applyBatch("com.android.contacts", lOperations);
                        stime += System.currentTimeMillis() - start;
                    } else {
                        resolver.applyBatch("com.android.contacts", lOperations);
                    }
                    startIndex = endIndex;
                    endIndex += 100;
                } catch (RemoteException e) {
                    e.printStackTrace();
                    return;
                } catch (OperationApplicationException e2) {
                    e2.printStackTrace();
                    return;
                }
            } while (endIndex <= size);
            if (startIndex < size) {
                lOperations.clear();
                lOperations.addAll(operations.subList(startIndex, size));
                operations.clear();
                operations.addAll(lOperations);
                return;
            }
            operations.clear();
        }
    }

    private static int mergeRawContactValues(ContentValues diffValues, ContentValues currentValues, int loopIndex) {
        int diffCount = 0;
        String currentRingtone = currentValues.getAsString("custom_ringtone");
        String differentRingtone = diffValues.getAsString("custom_ringtone");
        if (loopIndex > 0 && !TextUtil.stringOrNullEquals(currentRingtone, differentRingtone)) {
            diffCount = 1;
        }
        if (differentRingtone == null && currentRingtone != null) {
            diffValues.put("custom_ringtone", currentRingtone);
        }
        String value = currentValues.getAsString("send_to_voicemail");
        int currentSendToVoicemail = value == null ? 0 : Integer.parseInt(value);
        value = diffValues.getAsString("send_to_voicemail");
        int diffSendToVoicemail = value == null ? 0 : Integer.parseInt(value);
        if (loopIndex > 0 && (currentSendToVoicemail > 0 || diffSendToVoicemail > 0)) {
            diffCount++;
        }
        diffValues.put("send_to_voicemail", Integer.valueOf(diffSendToVoicemail + currentSendToVoicemail));
        value = currentValues.getAsString("times_contacted");
        int currentTimesContacted = value == null ? 0 : Integer.parseInt(value);
        value = diffValues.getAsString("times_contacted");
        int diffTimesContacted = value == null ? 0 : Integer.parseInt(value);
        if (loopIndex > 0 && (currentTimesContacted > 0 || diffTimesContacted > 0)) {
            diffCount++;
        }
        diffValues.put("times_contacted", Integer.valueOf(currentTimesContacted + diffTimesContacted));
        value = currentValues.getAsString("starred");
        int currentStarred = value == null ? 0 : Integer.parseInt(value);
        value = diffValues.getAsString("starred");
        int diffstarred = value == null ? 0 : Integer.parseInt(value);
        if (loopIndex > 0 && currentStarred != diffstarred) {
            diffCount++;
        }
        diffValues.put("starred", Integer.valueOf(currentStarred | diffstarred));
        value = currentValues.getAsString("last_time_contacted");
        long currentLastTimeContacted = value == null ? 0 : Long.parseLong(value);
        value = diffValues.getAsString("last_time_contacted");
        long diffLastTimeContacted = value == null ? 0 : Long.parseLong(value);
        if (loopIndex > 0 && currentLastTimeContacted != diffLastTimeContacted) {
            diffCount++;
        }
        if (currentLastTimeContacted > diffLastTimeContacted) {
            diffValues.put("last_time_contacted", Long.valueOf(currentLastTimeContacted));
        }
        int privateMode = 0;
        int diffPrivateMode = 0;
        if (currentValues.containsKey("is_private")) {
            privateMode = currentValues.getAsInteger("is_private").intValue();
        }
        if (diffValues.containsKey("is_private")) {
            diffPrivateMode = diffValues.getAsInteger("is_private").intValue();
        }
        if (loopIndex > 0 && privateMode != diffPrivateMode) {
            diffCount++;
        }
        diffValues.put("is_private", Integer.valueOf(privateMode | diffPrivateMode));
        return diffCount;
    }

    private static String[] getDataColumnsForMime(String mimetype) {
        if ("vnd.android.cursor.item/contact_event".equals(mimetype) || "vnd.android.cursor.item/relation".equals(mimetype)) {
            return new String[]{"data1", "data2"};
        } else if ("vnd.android.cursor.item/im".equals(mimetype)) {
            return new String[]{"data1", "data5"};
        } else if ("vnd.android.cursor.item/phone_v2".equals(mimetype) || "vnd.android.cursor.item/email_v2".equals(mimetype) || "vnd.android.cursor.item/postal-address_v2".equals(mimetype) || "vnd.android.cursor.item/nickname".equals(mimetype) || "vnd.android.cursor.item/website".equals(mimetype) || "vnd.android.cursor.item/note".equals(mimetype) || "vnd.android.cursor.item/sip_address".equals(mimetype) || "vnd.android.cursor.item/name".equals(mimetype) || "vnd.android.cursor.item/group_membership".equals(mimetype) || "vnd.android.huawei.cursor.item/ringtone".equals(mimetype)) {
            return new String[]{"data1"};
        } else if (!"vnd.android.cursor.item/organization".equals(mimetype)) {
            return null;
        } else {
            return new String[]{"data1", "data4"};
        }
    }

    private static boolean CanHaveMultipleValues(String mimetype) {
        if ("vnd.android.cursor.item/phone_v2".equals(mimetype) || "vnd.android.cursor.item/email_v2".equals(mimetype) || "vnd.android.cursor.item/website".equals(mimetype) || "vnd.android.cursor.item/contact_event".equals(mimetype) || "vnd.android.cursor.item/relation".equals(mimetype) || "vnd.android.cursor.item/im".equals(mimetype) || "vnd.android.cursor.item/postal-address_v2".equals(mimetype) || "vnd.android.cursor.item/group_membership".equals(mimetype)) {
            return true;
        }
        return false;
    }

    private static Object getNormalizedData(String mimetype, String columnName, Object data) {
        if (mimetype.equals("vnd.android.cursor.item/phone_v2") && data != null && columnName.equals("data1")) {
            return PhoneNumberUtils.normalizeNumber(data.toString());
        }
        return data;
    }

    private static boolean isAllowedToCopyMultipleEntryWithSameType(String aMimeType, ContentValues aDiffType, ContentValues aCurrType) {
        boolean z = true;
        if (!ContactStaticCache.isMimeTypeEqual(aMimeType, 3, "vnd.android.cursor.item/contact_event")) {
            return true;
        }
        int diffType = aDiffType.getAsInteger("data2").intValue();
        int currType = aCurrType.getAsInteger("data2").intValue();
        if (!isBirthdayType(diffType, currType)) {
            return true;
        }
        if (diffType == currType) {
            z = false;
        }
        return z;
    }

    private static boolean isBirthdayType(int diffType, int currType) {
        boolean type1 = diffType == 3 || diffType == 4;
        boolean type2 = currType == 3 || currType == 4;
        return type1 ? type2 : false;
    }
}
