package com.android.contacts.hap.sim.extended;

import android.content.ContentValues;
import android.content.Context;
import android.content.Entity;
import android.content.Entity.NamedContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import com.android.contacts.hap.CommonConstants;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.sim.IIccPhoneBookAdapter;
import com.android.contacts.hap.sim.SimConfig;
import com.android.contacts.hap.sim.SimContact;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.sim.SimPersistanceManager;
import com.android.contacts.hap.util.UnsupportedException;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.RawContactDeltaList;
import com.android.contacts.model.ValuesDelta;
import com.android.contacts.util.AsyncTaskExecutors;
import com.android.contacts.util.HwLog;
import com.huawei.android.provider.IccProviderUtilsEx;
import com.huawei.android.util.NoExtAPIException;
import java.util.ArrayList;
import java.util.List;

public class ExtendedSimPersistenceManager extends SimPersistanceManager {
    private String mAccountType = null;
    private SimConfig mConfig;
    private Context mContext;
    private int mMaxNameLength;
    private int mMaxNameLengthEntered = -1;
    private int mMaxNumberLenghtEntered = -1;
    private Uri mProviderUri;

    private static class ExtendedContactInfo {
        private String efid;
        private String id;
        private String index;
        private String name;
        private ArrayList<String> phNumbers = new ArrayList();

        public void populateList(List<SimContact> contactList, IIccPhoneBookAdapter lIccPhoneBookAdapter, int maxNameLength) {
            if (this.phNumbers.isEmpty() && TextUtils.isEmpty(this.name)) {
                ExtendedSimContact simContact = new ExtendedSimContact();
                simContact.efid = this.efid;
                simContact.index = this.index;
                simContact.id = this.id;
                contactList.add(simContact);
            }
            String newName;
            int lEncodedLength;
            if (this.phNumbers.size() != 0 || TextUtils.isEmpty(this.name)) {
                int maxNumLength = EmuiFeatureManager.getDefaultSimNumLength();
                for (String number : this.phNumbers) {
                    simContact = new ExtendedSimContact();
                    simContact.id = this.id;
                    newName = this.name;
                    if (!TextUtils.isEmpty(newName)) {
                        lEncodedLength = lIccPhoneBookAdapter.getAlphaEncodedLength(this.name);
                        if (lEncodedLength != -1 && lEncodedLength > maxNameLength) {
                            newName = CommonUtilMethods.getAlphaEncodeNameforSIM(lIccPhoneBookAdapter, this.name, maxNameLength);
                        }
                    }
                    simContact.name = newName;
                    if (TextUtils.isEmpty(number) || number.length() <= maxNumLength) {
                        simContact.number = number;
                    } else {
                        simContact.number = number.substring(0, maxNumLength);
                    }
                    simContact.efid = this.efid;
                    simContact.index = this.index;
                    contactList.add(simContact);
                }
                return;
            }
            simContact = new ExtendedSimContact();
            newName = this.name;
            if (!TextUtils.isEmpty(newName)) {
                lEncodedLength = lIccPhoneBookAdapter.getAlphaEncodedLength(this.name);
                if (lEncodedLength != -1 && lEncodedLength > maxNameLength) {
                    newName = CommonUtilMethods.getAlphaEncodeNameforSIM(lIccPhoneBookAdapter, this.name, maxNameLength);
                }
            }
            simContact.name = newName;
            simContact.efid = this.efid;
            simContact.index = this.index;
            simContact.id = this.id;
            contactList.add(simContact);
        }
    }

    private class LoadADNRecordSize extends AsyncTask<Void, Void, Void> {
        private LoadADNRecordSize() {
        }

        protected Void doInBackground(Void... params) {
            IIccPhoneBookAdapter lIccPhoneBookAdapter;
            if (SimFactoryManager.isDualSim()) {
                lIccPhoneBookAdapter = new IIccPhoneBookAdapter(SimFactoryManager.getSlotIdBasedOnAccountType(ExtendedSimPersistenceManager.this.mAccountType));
            } else {
                lIccPhoneBookAdapter = new IIccPhoneBookAdapter();
            }
            try {
                int[] result = lIccPhoneBookAdapter.getAdnRecordsSize();
                if (result == null || result.length <= 0) {
                    ExtendedSimPersistenceManager.this.mMaxNameLength = 14;
                    return null;
                }
                ExtendedSimPersistenceManager.this.mMaxNameLength = result[0] - 14;
                return null;
            } catch (UnsupportedException e) {
                HwLog.e("ExtendedSimPersistenceManager", "getRecordsSize() is unsupported in initSimRecordsSize()");
            }
        }

        protected void onPostExecute(Void unused) {
        }
    }

    public ExtendedSimPersistenceManager(SimConfig config, Uri aProviderUri, String accountType) {
        this.mConfig = config;
        this.mProviderUri = aProviderUri;
        this.mAccountType = accountType;
    }

    protected void init(Context context) {
        this.mContext = context;
        this.mMaxNameLength = 14;
        new LoadADNRecordSize().executeOnExecutor(AsyncTaskExecutors.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    public Uri insert(SimContact aContact, boolean toCheckSpace) {
        if (HwLog.HWDBG) {
            HwLog.d("ExtendedSimPersistenceManager", "insert");
        }
        if (toCheckSpace && this.mConfig.getAvailableFreeSpace() <= 0) {
            return null;
        }
        if (aContact instanceof ExtendedSimContact) {
            ExtendedSimContact contact = (ExtendedSimContact) aContact;
            if (TextUtils.isEmpty(contact.name)) {
                contact.name = "";
            }
            if (TextUtils.isEmpty(contact.number)) {
                contact.number = "";
            } else {
                contact.number = CommonUtilMethods.extractNetworkPortion(contact.number);
            }
            ContentValues values = new ContentValues();
            values.put("tag", contact.name);
            values.put("number", contact.number);
            Uri uri = null;
            Cursor cursor = null;
            if (5 != SimFactoryManager.getSimState(SimFactoryManager.getSlotIdBasedOnAccountType(this.mAccountType))) {
                return null;
            }
            try {
                uri = this.mContext.getContentResolver().insert(this.mProviderUri, values);
                if (uri == null) {
                    HwLog.e("ExtendedSimPersistenceManager", "Unable to insert the contact in SIM");
                    return null;
                }
                if (HwLog.HWDBG) {
                    HwLog.d("ExtendedSimPersistenceManager", "Inserted Uri is : " + uri);
                }
                List<String> segments = uri.getPathSegments();
                if (segments != null) {
                    int size = segments.size();
                    contact.efid = (String) segments.get(size - 1);
                    contact.index = (String) segments.get(size - 2);
                }
                if (!isCrossCheckRequired(aContact)) {
                    return uri;
                }
                cursor = this.mContext.getContentResolver().query(this.mProviderUri, null, "efid = '" + contact.efid + "' AND " + "index" + " = '" + contact.index + "'", null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    SimContact lContact = getContact(cursor);
                    contact.name = lContact.name;
                    contact.number = lContact.number;
                    this.mMaxNameLengthEntered = contact.name.getBytes(CommonConstants.DEFAULT_CHARSET).length;
                    this.mMaxNumberLenghtEntered = contact.number.getBytes(CommonConstants.DEFAULT_CHARSET).length;
                }
                if (cursor != null) {
                    cursor.close();
                }
                return uri;
            } catch (SQLiteException e) {
                HwLog.e("ExtendedSimPersistenceManager", "Error Inserting Contacts to SIM, no is : ", e);
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            HwLog.e("ExtendedSimPersistenceManager", "aContact is not a instance of ExtendedSimContact!");
            return null;
        }
    }

    public int delete(SimContact aContact) {
        if (aContact instanceof ExtendedSimContact) {
            ExtendedSimContact simContact = (ExtendedSimContact) aContact;
            return delete(simContact.efid, simContact.index);
        }
        HwLog.e("ExtendedSimPersistenceManager", "aContact is not a instance of ExtendedSimContact!");
        return 0;
    }

    public SimContact getContact(Cursor aCursor) {
        ExtendedSimContact simContact = new ExtendedSimContact();
        try {
            simContact.name = aCursor.getString(((Integer) IccProviderUtilsEx.getIndexColumn().get("INDEX_NAME_COLUMN")).intValue());
            simContact.number = aCursor.getString(((Integer) IccProviderUtilsEx.getIndexColumn().get("INDEX_NUMBER_COLUMN")).intValue());
            simContact.efid = aCursor.getString(((Integer) IccProviderUtilsEx.getIndexColumn().get("INDEX_EFID_COLUMN")).intValue());
            simContact.index = aCursor.getString(((Integer) IccProviderUtilsEx.getIndexColumn().get("INDEX_SIM_INDEX_COLUMN")).intValue());
        } catch (NoExtAPIException e) {
            e.printStackTrace();
        }
        return simContact;
    }

    public void getContacts(Entity entity, List<SimContact> contactList) {
        if (contactList != null) {
            IIccPhoneBookAdapter lIccPhoneBookAdapter;
            ExtendedContactInfo contactInfo = new ExtendedContactInfo();
            ContentValues entityValues = entity.getEntityValues();
            if (CommonUtilMethods.isSimAccount(entityValues.getAsString("account_type"))) {
                String sync1 = entityValues.getAsString("sync1");
                if (sync1 != null) {
                    contactInfo.efid = sync1;
                }
                String sync2 = entityValues.getAsString("sync2");
                if (sync2 != null) {
                    contactInfo.index = sync2;
                }
            }
            contactInfo.id = entityValues.getAsString("_id");
            for (NamedContentValues ncValues : entity.getSubValues()) {
                ContentValues values = ncValues.values;
                if ("vnd.android.cursor.item/name".equals(values.getAsString("mimetype"))) {
                    contactInfo.name = values.getAsString("data1");
                }
                if ("vnd.android.cursor.item/phone_v2".equals(values.getAsString("mimetype"))) {
                    contactInfo.phNumbers.add(CommonUtilMethods.extractNetworkPortion(values.getAsString("data1")));
                }
            }
            if (SimFactoryManager.isDualSim()) {
                lIccPhoneBookAdapter = new IIccPhoneBookAdapter(SimFactoryManager.getSlotIdBasedOnAccountType(this.mAccountType));
            } else {
                lIccPhoneBookAdapter = new IIccPhoneBookAdapter();
            }
            contactInfo.populateList(contactList, lIccPhoneBookAdapter, this.mMaxNameLength);
        }
    }

    public void getSimContacts(Entity entity, List<SimContact> contactList) {
        getContacts(entity, contactList);
    }

    public Cursor queryAll() {
        try {
            return this.mContext.getContentResolver().query(this.mProviderUri, null, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String prepareWhereClause(SimContact aContact) {
        ExtendedSimContact contact = (ExtendedSimContact) aContact;
        StringBuilder builder = new StringBuilder();
        builder.append("tag='");
        builder.append(contact.name);
        builder.append("' AND number='");
        builder.append(contact.number);
        builder.append("'");
        if (!TextUtils.isEmpty(contact.efid)) {
            builder.append(" AND efid='");
            builder.append(contact.efid);
            builder.append("'");
        }
        if (!TextUtils.isEmpty(contact.index)) {
            builder.append(" AND index='");
            builder.append(contact.index);
            builder.append("'");
        }
        return builder.toString();
    }

    public int update(SimContact aOldContact, SimContact aNewContact) {
        if (HwLog.HWDBG) {
            HwLog.d("ExtendedSimPersistenceManager", "update");
        }
        if (aOldContact instanceof ExtendedSimContact) {
            ExtendedSimContact oldContact = (ExtendedSimContact) aOldContact;
            if (aNewContact instanceof ExtendedSimContact) {
                ExtendedSimContact newContact = (ExtendedSimContact) aNewContact;
                String whereClause = prepareWhereClause(oldContact);
                ContentValues simValues = new ContentValues();
                simValues.put("tag", oldContact.name);
                simValues.put("number", oldContact.number);
                simValues.put("newTag", newContact.name);
                simValues.put("newNumber", newContact.number);
                simValues.put("efid", newContact.efid);
                simValues.put("index", newContact.index);
                if (5 != SimFactoryManager.getSimState(SimFactoryManager.getSlotIdBasedOnAccountType(this.mAccountType))) {
                    return 0;
                }
                return this.mContext.getContentResolver().update(this.mProviderUri, simValues, whereClause, null);
            }
            HwLog.e("ExtendedSimPersistenceManager", "aNewContact is not a instance of ExtendedSimContact!");
            return 0;
        }
        HwLog.e("ExtendedSimPersistenceManager", "aOldContact is not a instance of ExtendedSimContact!");
        return 0;
    }

    public int save(RawContactDeltaList state) {
        if (HwLog.HWDBG) {
            HwLog.d("ExtendedSimPersistenceManager", "Save sim contact...!!");
        }
        int update = -1;
        if (5 != SimFactoryManager.getSimState(SimFactoryManager.getSlotIdBasedOnAccountType(this.mAccountType))) {
            return 0;
        }
        ExtendedSimContact lNewContact = new ExtendedSimContact();
        ValuesDelta values = null;
        ValuesDelta lNameDelta = null;
        ValuesDelta lNumberDelta = null;
        for (RawContactDelta delta : state) {
            ArrayList<ValuesDelta> nameEntries = delta.getMimeEntries("vnd.android.cursor.item/name");
            if (nameEntries != null) {
                lNameDelta = (ValuesDelta) nameEntries.get(0);
                if (!(lNameDelta == null || lNameDelta.isDelete())) {
                    lNewContact.name = lNameDelta.getAsString("data1");
                    if (!TextUtils.isEmpty(lNewContact.name)) {
                        lNameDelta.putNull("data2");
                        lNameDelta.putNull("data3");
                        lNameDelta.putNull("data4");
                        lNameDelta.putNull("data5");
                        lNameDelta.putNull("data6");
                        lNameDelta.putNull("data7");
                        lNameDelta.putNull("data8");
                        lNameDelta.putNull("data9");
                    }
                }
            }
            ArrayList<ValuesDelta> numberEntries = delta.getMimeEntries("vnd.android.cursor.item/phone_v2");
            if (numberEntries != null) {
                lNumberDelta = (ValuesDelta) numberEntries.get(0);
                if (!(lNumberDelta == null || lNumberDelta.isDelete())) {
                    lNewContact.number = CommonUtilMethods.extractNetworkPortion(lNumberDelta.getAsString("data1"));
                }
            }
            values = delta.getValues();
            lNewContact.efid = values.getAsString("sync1");
            lNewContact.index = values.getAsString("sync2");
        }
        if (lNewContact.efid == null || lNewContact.index == null) {
            update = insert(lNewContact, true) == null ? -1 : 1;
        } else {
            String whereClause = "efid = '" + lNewContact.efid + "' AND " + "index" + " = '" + lNewContact.index + "'";
            Cursor cursor = this.mContext.getContentResolver().query(this.mProviderUri, null, whereClause, null, null);
            if (cursor.moveToFirst()) {
                update = update(getContact(cursor), lNewContact);
                if (update > 0) {
                    cursor.close();
                    cursor = this.mContext.getContentResolver().query(this.mProviderUri, null, whereClause, null, null);
                    if (cursor.moveToFirst()) {
                        SimContact lContact = getContact(cursor);
                        lNewContact.name = lContact.name;
                        lNewContact.number = lContact.number;
                    }
                } else {
                    HwLog.i("ExtendedSimPersistenceManager", "Unable to update the contact in SIM");
                }
            }
            cursor.close();
        }
        if (values != null) {
            values.put("sync1", lNewContact.efid);
            values.put("sync2", lNewContact.index);
            values.put("sync3", lNewContact.hashCode());
            if (!(lNameDelta == null || TextUtils.isEmpty(lNewContact.name))) {
                lNameDelta.put("data1", lNewContact.name);
            }
            if (lNumberDelta != null) {
                if (TextUtils.isEmpty(lNewContact.number)) {
                    lNumberDelta.markDeleted();
                } else {
                    lNumberDelta.put("data1", lNewContact.number);
                }
            }
        }
        return update;
    }

    public int delete(String efid, String index) {
        if (TextUtils.isEmpty(efid) || TextUtils.isEmpty(index)) {
            HwLog.e("ExtendedSimPersistenceManager", "Efid or index is null therefore cannot delete this contact");
            return 0;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("efid='");
        builder.append(efid);
        builder.append("'");
        builder.append(" AND index='");
        builder.append(index);
        builder.append("'");
        if (5 != SimFactoryManager.getSimState(SimFactoryManager.getSlotIdBasedOnAccountType(this.mAccountType))) {
            if (HwLog.HWDBG) {
                HwLog.d("ExtendedSimPersistenceManager", "SIM state is not ready return zero contacts removed");
            }
            return 0;
        }
        if (HwLog.HWDBG) {
            HwLog.d("ExtendedSimPersistenceManager", "SIM state is ready remove cotnacts for  " + builder.toString());
        }
        int numOfContactsDeleted = this.mContext.getContentResolver().delete(this.mProviderUri, builder.toString(), null);
        if (HwLog.HWDBG) {
            HwLog.d("ExtendedSimPersistenceManager", "Provider uri " + this.mProviderUri + "Number of contacts deleted are " + numOfContactsDeleted);
        }
        return numOfContactsDeleted;
    }

    private boolean isCrossCheckRequired(SimContact aContact) {
        boolean isCrossCheckRequired = false;
        if (!TextUtils.isEmpty(aContact.name)) {
            isCrossCheckRequired = aContact.name.getBytes(CommonConstants.DEFAULT_CHARSET).length > this.mMaxNameLengthEntered;
        }
        if (TextUtils.isEmpty(aContact.number)) {
            return isCrossCheckRequired;
        }
        return isCrossCheckRequired || aContact.number.getBytes(CommonConstants.DEFAULT_CHARSET).length > this.mMaxNumberLenghtEntered;
    }

    public void performHealthCheck(String aAccountType) {
        String[] selectionArgs = new String[]{aAccountType};
        Uri uri = RawContacts.CONTENT_URI.buildUpon().appendQueryParameter("caller_is_syncadapter", "true").build();
        this.mContext.getContentResolver().delete(uri, "account_type =? AND (sync1 IS NULL OR sync2 IS NULL)", selectionArgs);
    }
}
