package com.android.contacts.hap.delete;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import com.android.contacts.hap.CommonConstants.DatabaseConstants;
import com.android.contacts.hap.sim.SimDatabaseHelper;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.util.HwLog;
import com.android.contacts.vcard.ProcessorBase;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import com.google.android.collect.Sets;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class DuplicateContactsProcessor extends ProcessorBase {
    private static HashSet<String> mIncludedMimetypes = Sets.newHashSet();
    private final AccountWithDataSet mAccount;
    private volatile boolean mCanceled;
    private volatile boolean mDone;
    private final int mJobId;
    private final DuplicateContactsListener mListener;
    private ContentResolver mResolver = this.mService.getContentResolver();
    private final DuplicateContactsService mService;

    private static class RawContactsAndMimetypeListObject {
        public HashMap<String, Integer> mimetypeMap = new HashMap();
        public long rawContactId;

        public RawContactsAndMimetypeListObject(long aRawContactId) {
            this.rawContactId = aRawContactId;
        }

        public boolean isMimetypeMapSame(HashMap<String, Integer> aMimeMap) {
            if (aMimeMap != null) {
                return this.mimetypeMap.equals(aMimeMap);
            }
            return false;
        }
    }

    static {
        mIncludedMimetypes.add("vnd.android.cursor.item/email_v2");
        mIncludedMimetypes.add("vnd.android.cursor.item/im");
        mIncludedMimetypes.add("vnd.android.cursor.item/nickname");
        mIncludedMimetypes.add("vnd.android.cursor.item/organization");
        mIncludedMimetypes.add("vnd.android.cursor.item/phone_v2");
        mIncludedMimetypes.add("vnd.android.cursor.item/photo");
        mIncludedMimetypes.add("vnd.android.cursor.item/sip_address");
        mIncludedMimetypes.add("vnd.android.cursor.item/name");
    }

    public DuplicateContactsProcessor(DuplicateContactsService service, DuplicateContactsListener listener, int jobId, AccountWithDataSet aAccount) {
        this.mService = service;
        this.mListener = listener;
        this.mJobId = jobId;
        this.mAccount = aAccount;
    }

    public int getType() {
        return 0;
    }

    public void run() {
        try {
            if (this.mListener != null && isCancelled()) {
                this.mListener.onDeleteDuplicateContactsCanceled("", this.mJobId);
            }
            deleteDuplicateContacts();
            synchronized (this) {
                this.mDone = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (this.mListener != null) {
                this.mListener.onDeleteDuplicateContactsFailed(this.mAccount.name, this.mJobId);
            }
            synchronized (this) {
                this.mDone = true;
            }
        } catch (Throwable th) {
            synchronized (this) {
                this.mDone = true;
            }
        }
    }

    private String getCommaSeparatedRawContactIds(ArrayList<Long> aRawContactIdList, StringBuilder builder) {
        if (aRawContactIdList.isEmpty()) {
            return "";
        }
        if (builder == null) {
            builder = new StringBuilder();
        }
        builder.setLength(0);
        for (Long rawContactId : aRawContactIdList) {
            builder.append(rawContactId.longValue());
            builder.append(",");
        }
        builder.setLength(builder.length() - 1);
        if (HwLog.HWFLOW) {
            HwLog.i("DuplicateContactsProcessor", "CommaSeperated raw contacts = " + builder.toString());
        }
        return builder.toString();
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        HwLog.d("DuplicateContactsProcessor", "DuplicateContactsProcessor received cancel request");
        if (this.mDone || this.mCanceled) {
            return false;
        }
        this.mCanceled = true;
        return true;
    }

    public synchronized void cancelAndNotified(boolean aNotified) {
        if (HwLog.HWDBG) {
            HwLog.d("DuplicateContactsProcessor", "received cancel request and notified");
        }
    }

    public boolean isCancelled() {
        return this.mCanceled;
    }

    public boolean isDone() {
        return this.mDone;
    }

    private void deleteDuplicateContacts() {
        String lAccountName;
        deleteDuplicateDataEntries();
        HashSet<Long> lDuplicateRawContactsSet = new HashSet();
        findDuplicateContacts(lDuplicateRawContactsSet);
        Iterator<Long> iter = lDuplicateRawContactsSet.iterator();
        ArrayList<Long> duplicateRawContacts = new ArrayList();
        while (iter.hasNext()) {
            duplicateRawContacts.add((Long) iter.next());
        }
        if ("com.android.huawei.phone".equals(this.mAccount.type)) {
            lAccountName = this.mService.getString(R.string.phoneLabelsGroup);
        } else {
            lAccountName = this.mAccount.name;
        }
        if (duplicateRawContacts.size() > 0) {
            if ("com.android.huawei.phone".equals(this.mAccount.type) && this.mService.getResources().getBoolean(R.bool.config_check_Russian_Grammar)) {
                lAccountName = this.mService.getString(R.string.phoneLabelsGroup_from);
            }
            int count = deleteRawContactsFromDatabase(duplicateRawContacts);
            if (this.mListener != null) {
                this.mListener.onDeleteDuplicateContactsFinished(lAccountName, count, this.mJobId);
                return;
            }
            return;
        }
        if ("com.android.huawei.phone".equals(this.mAccount.type) && this.mService.getResources().getBoolean(R.bool.config_check_Russian_Grammar)) {
            lAccountName = this.mService.getString(R.string.phoneLabelsGroup_in);
        }
        if (this.mListener != null) {
            this.mListener.onNoDuplicateContactsFound(lAccountName, this.mJobId);
        }
    }

    private int deleteRawContactsFromDatabase(ArrayList<Long> aDuplicateRawContacts) {
        Uri uri = RawContacts.CONTENT_URI;
        StringBuilder selectionBuilder = new StringBuilder();
        int index = 0;
        int count = 0;
        if ("com.android.huawei.sim".equals(this.mAccount.type)) {
            return SimDatabaseHelper.deleteSimContactsFromSimCard(this.mService.getApplicationContext(), aDuplicateRawContacts, this.mAccount.type);
        }
        if ("com.android.huawei.secondsim".equals(this.mAccount.type)) {
            return SimDatabaseHelper.deleteSimContactsFromSimCard(this.mService.getApplicationContext(), aDuplicateRawContacts, this.mAccount.type);
        }
        selectionBuilder.append("_id IN (");
        for (Long rawContact : aDuplicateRawContacts) {
            selectionBuilder.append(rawContact);
            selectionBuilder.append(",");
            index++;
            if (index == 200) {
                selectionBuilder.setLength(selectionBuilder.length() - 1);
                selectionBuilder.append(")");
                index = 0;
                count += this.mResolver.delete(uri, selectionBuilder.toString(), null);
                selectionBuilder.setLength(0);
                selectionBuilder.append("_id IN (");
            }
        }
        if (index > 0) {
            selectionBuilder.setLength(selectionBuilder.length() - 1);
            selectionBuilder.append(")");
            count += this.mResolver.delete(uri, selectionBuilder.toString(), null);
        }
        return count;
    }

    private void findDuplicateContacts(HashSet<Long> aDuplicateRawContacts) {
        ArrayList<ArrayList<RawContactsAndMimetypeListObject>> totalList = new ArrayList();
        fillRawContactsAndMimeTypesList(totalList);
        for (ArrayList<RawContactsAndMimetypeListObject> singleList : totalList) {
            ArrayList<Long> duplicateRawContactList = new ArrayList();
            if (singleList.size() > 1) {
                for (RawContactsAndMimetypeListObject obj : singleList) {
                    duplicateRawContactList.add(Long.valueOf(obj.rawContactId));
                }
                Set<Entry<String, Integer>> lEntrySet = ((RawContactsAndMimetypeListObject) singleList.get(0)).mimetypeMap.entrySet();
                HashMap<Long, ArrayList<Long>> duplicateContactMap = new HashMap();
                boolean toClearMap = true;
                if (!duplicateRawContactList.isEmpty()) {
                    duplicateContactMap.put((Long) duplicateRawContactList.get(0), duplicateRawContactList);
                    if (HwLog.HWDBG) {
                        HwLog.d("DuplicateContactsProcessor", " Finding duplicates");
                    }
                    for (Entry<String, Integer> entry : lEntrySet) {
                        String mimetype = (String) entry.getKey();
                        if (!"vnd.android.cursor.item/photo".equals(mimetype)) {
                            Integer count = (Integer) entry.getValue();
                            if (HwLog.HWDBG) {
                                HwLog.d("DuplicateContactsProcessor", " Current mimetype = " + mimetype);
                                HwLog.d("DuplicateContactsProcessor", " Mimetype count is = " + count);
                            }
                            findDuplicateValues(mimetype, count.intValue(), duplicateContactMap, duplicateRawContactList, toClearMap);
                            toClearMap = false;
                        }
                    }
                }
                for (Entry<Long, ArrayList<Long>> entry2 : duplicateContactMap.entrySet()) {
                    Long rawContactIdsFound = (Long) entry2.getKey();
                    if (HwLog.HWDBG) {
                        HwLog.d("DuplicateContactsProcessor", "Duplicate raw contact founds = " + rawContactIdsFound);
                    }
                    ArrayList<Long> singleDuplicateList = (ArrayList) entry2.getValue();
                    if (singleDuplicateList != null) {
                        for (Long rawContactId : singleDuplicateList) {
                            if (HwLog.HWDBG) {
                                HwLog.i("DuplicateContactsProcessor", "This raw contact is duplicated with = " + rawContactId);
                            }
                            aDuplicateRawContacts.add(rawContactId);
                        }
                    }
                }
            }
        }
    }

    private void fillRawContactsAndMimeTypesList(ArrayList<ArrayList<RawContactsAndMimetypeListObject>> totalList) {
        RawContactsAndMimetypeListObject object;
        Throwable th;
        RawContactsAndMimetypeListObject rawContactAndMimetypeListObj = null;
        Uri uri = DatabaseConstants.RAW_CONTACTS_MIMETYPE_COUNT_URI;
        String[] projection = new String[]{"raw_contact_id", "mimetype", "mimetype_count"};
        StringBuilder selectionBuilder = new StringBuilder();
        List<String> selectionArgs = new ArrayList();
        selectionBuilder.append("account_type=? AND account_name=?");
        selectionBuilder.append(" AND (");
        for (int i = 1; i <= 15; i++) {
            selectionBuilder.append("(data").append(i).append(" NOT NULL");
            selectionBuilder.append(" AND ");
            selectionBuilder.append(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH).append(i).append(" != '')");
            selectionBuilder.append(" OR ");
        }
        selectionBuilder.setLength(selectionBuilder.length() - 4);
        selectionBuilder.append(")");
        selectionArgs.add(this.mAccount.type);
        selectionArgs.add(this.mAccount.name);
        if (this.mAccount.dataSet != null) {
            selectionBuilder.append(" AND data_set=?");
            selectionArgs.add(this.mAccount.dataSet);
        } else {
            selectionBuilder.append(" AND data_set IS NULL");
        }
        Cursor lRawContactsAndMimetypeCursor = this.mResolver.query(uri, projection, selectionBuilder.toString(), (String[]) selectionArgs.toArray(new String[0]), "raw_contact_id,mimetype");
        long currentRawContact = -1;
        if (lRawContactsAndMimetypeCursor != null) {
            if (lRawContactsAndMimetypeCursor.moveToFirst()) {
                do {
                    RawContactsAndMimetypeListObject rawContactAndMimetypeListObj2 = rawContactAndMimetypeListObj;
                    try {
                        long rawContact = lRawContactsAndMimetypeCursor.getLong(0);
                        String mimetype = lRawContactsAndMimetypeCursor.getString(1);
                        int mimetypeCount = lRawContactsAndMimetypeCursor.getInt(2);
                        if (HwLog.HWDBG) {
                            HwLog.d("DuplicateContactsProcessor", "RawContact id = " + rawContact + " and mimetype = " + mimetype);
                        }
                        if (mIncludedMimetypes.contains(mimetype)) {
                            if (rawContact != currentRawContact) {
                                if (currentRawContact != -1) {
                                    boolean isAdded = false;
                                    for (ArrayList<RawContactsAndMimetypeListObject> singleList : totalList) {
                                        object = (RawContactsAndMimetypeListObject) singleList.get(0);
                                        if (rawContactAndMimetypeListObj2 != null) {
                                            if (object.isMimetypeMapSame(rawContactAndMimetypeListObj2.mimetypeMap)) {
                                                if (HwLog.HWDBG) {
                                                    HwLog.d("DuplicateContactsProcessor", "Found the raw contact " + object.rawContactId + " having mimetypes as  " + rawContactAndMimetypeListObj2.rawContactId);
                                                }
                                                singleList.add(rawContactAndMimetypeListObj2);
                                                isAdded = true;
                                            }
                                        }
                                    }
                                    if (!isAdded) {
                                        ArrayList<RawContactsAndMimetypeListObject> newSingleList = new ArrayList();
                                        newSingleList.add(rawContactAndMimetypeListObj2);
                                        totalList.add(newSingleList);
                                        if (rawContactAndMimetypeListObj2 != null && HwLog.HWDBG) {
                                            HwLog.d("DuplicateContactsProcessor", "Not Found any raw contact having mimetypes as  " + rawContactAndMimetypeListObj2.rawContactId);
                                        }
                                    }
                                }
                                currentRawContact = rawContact;
                                rawContactAndMimetypeListObj = new RawContactsAndMimetypeListObject(rawContact);
                            } else {
                                rawContactAndMimetypeListObj = rawContactAndMimetypeListObj2;
                            }
                            if (rawContactAndMimetypeListObj != null) {
                                try {
                                    rawContactAndMimetypeListObj.mimetypeMap.put(mimetype, Integer.valueOf(mimetypeCount));
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            }
                        } else {
                            rawContactAndMimetypeListObj = rawContactAndMimetypeListObj2;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        rawContactAndMimetypeListObj = rawContactAndMimetypeListObj2;
                    }
                } while (lRawContactsAndMimetypeCursor.moveToNext());
            }
            lRawContactsAndMimetypeCursor.close();
        }
        if (rawContactAndMimetypeListObj != null) {
            for (ArrayList<RawContactsAndMimetypeListObject> singleList2 : totalList) {
                object = (RawContactsAndMimetypeListObject) singleList2.get(0);
                if (object.isMimetypeMapSame(rawContactAndMimetypeListObj.mimetypeMap)) {
                    singleList2.add(rawContactAndMimetypeListObj);
                    if (HwLog.HWDBG) {
                        HwLog.d("DuplicateContactsProcessor", "Found the raw contact " + object.rawContactId + " having mimetypes as  " + rawContactAndMimetypeListObj.rawContactId);
                    }
                }
            }
            return;
        }
        return;
        lRawContactsAndMimetypeCursor.close();
        throw th;
    }

    private void findDuplicateValues(String aMimetype, int aCount, HashMap<Long, ArrayList<Long>> aDuplicateMap, ArrayList<Long> arrayList, boolean toClearMap) {
        if (HwLog.HWDBG) {
            HwLog.d("DuplicateContactsProcessor", "Size of the duplicate pairs = " + aDuplicateMap.size());
        }
        Set<Entry<Long, ArrayList<Long>>> lEntrySet = aDuplicateMap.entrySet();
        HashMap<Long, ArrayList<Long>> tempDuplicateMap = new HashMap();
        ArrayList<Long> tempContactList = new ArrayList();
        StringBuilder builder = new StringBuilder();
        for (Entry<Long, ArrayList<Long>> entry : lEntrySet) {
            Long rawContactId = (Long) entry.getKey();
            if (HwLog.HWDBG) {
                HwLog.d("DuplicateContactsProcessor", "Raw contact id for the mimetype iteration is = " + rawContactId);
            }
            ArrayList<Long> singleList = (ArrayList) entry.getValue();
            if (singleList != null) {
                if (!toClearMap) {
                    singleList.add(0, rawContactId);
                }
                tempContactList.addAll(singleList);
                singleList.clear();
                if (toClearMap) {
                    toClearMap = false;
                    aDuplicateMap.clear();
                }
                for (int i = 0; i < tempContactList.size(); i++) {
                    if (HwLog.HWDBG) {
                        HwLog.d("DuplicateContactsProcessor", "Current raw contact = " + String.valueOf(tempContactList.get(i)));
                    }
                    Cursor cursor = this.mResolver.query(DatabaseConstants.DUPLICATE_CONTACTS.buildUpon().appendQueryParameter("mimetype", aMimetype).appendQueryParameter("firstrawcontactid", String.valueOf(tempContactList.get(i))).appendQueryParameter("rawcontactids", getCommaSeparatedRawContactIds(tempContactList, builder)).build(), null, null, null, "firstRawContact, secondRawContact");
                    if (cursor != null) {
                        if (HwLog.HWDBG) {
                            HwLog.d("DuplicateContactsProcessor", "Cursor count = " + cursor.getCount());
                        }
                        try {
                            if (cursor.moveToFirst()) {
                                ArrayList<Long> singleDuplicateList;
                                long currentFirstRawContact = -1;
                                long currentSecondRawContact = -1;
                                int dupCount = 0;
                                do {
                                    long firstRawContactId;
                                    long secondRawContactId;
                                    long rc1 = cursor.getLong(0);
                                    long rc2 = cursor.getLong(1);
                                    if (rc1 < rc2) {
                                        firstRawContactId = rc1;
                                        secondRawContactId = rc2;
                                    } else {
                                        firstRawContactId = rc2;
                                        secondRawContactId = rc1;
                                    }
                                    if (!(currentFirstRawContact == firstRawContactId && (secondRawContactId == currentSecondRawContact || currentFirstRawContact == -1)) && dupCount == aCount) {
                                        singleDuplicateList = (ArrayList) tempDuplicateMap.get(Long.valueOf(currentFirstRawContact));
                                        if (singleDuplicateList == null) {
                                            singleDuplicateList = new ArrayList();
                                        }
                                        if (!singleDuplicateList.contains(Long.valueOf(currentSecondRawContact))) {
                                            singleDuplicateList.add(Long.valueOf(currentSecondRawContact));
                                        }
                                        tempDuplicateMap.put(Long.valueOf(currentFirstRawContact), singleDuplicateList);
                                        tempContactList.remove(Long.valueOf(currentSecondRawContact));
                                        dupCount = 0;
                                        if (HwLog.HWDBG) {
                                            HwLog.d("DuplicateContactsProcessor", " Raw contacts having same mimetype " + aMimetype + "  = " + currentFirstRawContact + " AND " + currentSecondRawContact);
                                        }
                                    }
                                    dupCount++;
                                    currentFirstRawContact = firstRawContactId;
                                    currentSecondRawContact = secondRawContactId;
                                } while (cursor.moveToNext());
                                if (currentFirstRawContact != -1 && dupCount == aCount) {
                                    singleDuplicateList = (ArrayList) tempDuplicateMap.get(Long.valueOf(currentFirstRawContact));
                                    if (singleDuplicateList == null) {
                                        singleDuplicateList = new ArrayList();
                                    }
                                    singleDuplicateList.add(Long.valueOf(currentSecondRawContact));
                                    tempDuplicateMap.put(Long.valueOf(currentFirstRawContact), singleDuplicateList);
                                    tempContactList.remove(Long.valueOf(currentSecondRawContact));
                                    if (HwLog.HWDBG) {
                                        HwLog.d("DuplicateContactsProcessor", " Raw contacts having same mimetype " + aMimetype + "  = " + currentFirstRawContact + " AND " + currentSecondRawContact);
                                    }
                                }
                            }
                            cursor.close();
                        } catch (Throwable th) {
                            cursor.close();
                        }
                    }
                }
                tempContactList.clear();
            }
        }
        aDuplicateMap.clear();
        aDuplicateMap.putAll(tempDuplicateMap);
        tempDuplicateMap.clear();
    }

    private void deleteDuplicateDataEntries() {
        StringBuilder selectionBuilder = new StringBuilder();
        selectionBuilder.append("_id NOT IN (");
        selectionBuilder.append("SELECT _id FROM data GROUP BY mimetype_id,");
        for (int i = 1; i <= 15; i++) {
            selectionBuilder.append(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH).append(i);
            selectionBuilder.append(",");
        }
        ArrayList<String> selectionArgs = new ArrayList();
        selectionBuilder.append("raw_contact_id)");
        selectionBuilder.append(" AND account_name=? AND account_type=? AND ");
        selectionArgs.add(this.mAccount.name);
        selectionArgs.add(this.mAccount.type);
        if (this.mAccount.dataSet == null) {
            selectionBuilder.append("data_set IS NULL");
        } else {
            selectionBuilder.append("data_set=?");
            selectionArgs.add(this.mAccount.dataSet);
        }
        this.mResolver.delete(Data.CONTENT_URI, selectionBuilder.toString(), (String[]) selectionArgs.toArray(new String[0]));
    }
}
