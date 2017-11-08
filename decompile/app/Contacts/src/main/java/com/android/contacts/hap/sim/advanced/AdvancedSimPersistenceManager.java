package com.android.contacts.hap.sim.advanced;

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
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.huawei.android.provider.IccProviderUtilsEx;
import com.huawei.android.util.NoExtAPIException;
import java.util.ArrayList;
import java.util.List;

public class AdvancedSimPersistenceManager extends SimPersistanceManager {
    private String mAccountType = null;
    private SimConfig mConfig;
    private Context mContext;
    private int mMaxNameLength;
    private Uri mProviderUri;

    private static class AdvancedContactInfo {
        private String efid;
        private ArrayList<DataItem> emails = new ArrayList();
        private String id;
        private String index;
        private boolean isAnrEnable;
        private String name;
        private ArrayList<DataItem> phNumbers = new ArrayList();

        public void populateContactListForCopy(List<SimContact> contactList, IIccPhoneBookAdapter lIccPhoneBookAdapter, int maxNameLength) {
            int emailIndex = 0;
            int phoneIndex = 0;
            boolean isEmailAvailable = true;
            boolean isPhoneNumberAvailable = true;
            int emailSize = this.emails.size();
            int phoneSize = this.phNumbers.size();
            int maxNumLength = EmuiFeatureManager.getDefaultSimNumLength();
            while (true) {
                if (isEmailAvailable || isPhoneNumberAvailable) {
                    AdvancedSimContact simContact = new AdvancedSimContact();
                    String newName = this.name;
                    if (!TextUtils.isEmpty(newName)) {
                        int lEncodedLength = lIccPhoneBookAdapter.getAlphaEncodedLength(this.name);
                        if (lEncodedLength != -1 && lEncodedLength > maxNameLength) {
                            newName = CommonUtilMethods.getAlphaEncodeNameforSIM(lIccPhoneBookAdapter, this.name, maxNameLength);
                        }
                    }
                    simContact.name = newName;
                    if (emailSize > emailIndex) {
                        DataItem emailData = (DataItem) this.emails.get(emailIndex);
                        simContact.email = emailData.data;
                        simContact.email_type = emailData.type;
                        simContact.email_custom = emailData.label;
                        emailIndex++;
                    }
                    if (phoneSize > phoneIndex) {
                        DataItem numberData = (DataItem) this.phNumbers.get(phoneIndex);
                        if (TextUtils.isEmpty(numberData.data) || numberData.data.length() <= maxNumLength) {
                            simContact.number = numberData.data;
                        } else {
                            simContact.number = numberData.data.substring(0, maxNumLength);
                        }
                        simContact.number_type = numberData.type;
                        simContact.number_custom = numberData.label;
                        phoneIndex++;
                        if (phoneSize > phoneIndex && this.isAnrEnable) {
                            DataItem anrData = (DataItem) this.phNumbers.get(phoneIndex);
                            if (TextUtils.isEmpty(anrData.data) || anrData.data.length() <= maxNumLength) {
                                simContact.anr = anrData.data;
                            } else {
                                simContact.anr = anrData.data.substring(0, maxNumLength);
                            }
                            simContact.anr_type = anrData.type;
                            simContact.anr_custom = anrData.label;
                            phoneIndex++;
                        }
                    }
                    if (!(TextUtils.isEmpty(simContact.name) && TextUtils.isEmpty(simContact.number) && TextUtils.isEmpty(simContact.email))) {
                        contactList.add(simContact);
                    }
                    isEmailAvailable = emailSize > emailIndex;
                    if (phoneSize > phoneIndex) {
                        isPhoneNumberAvailable = true;
                    } else {
                        isPhoneNumberAvailable = false;
                    }
                } else {
                    return;
                }
            }
        }

        public void populateContactListForCompare(List<SimContact> contactList) {
            AdvancedSimContact simContact = new AdvancedSimContact();
            simContact.id = this.id;
            simContact.efid = this.efid;
            simContact.index = this.index;
            simContact.name = this.name;
            for (DataItem dataItem : this.emails) {
                simContact.email = dataItem.data;
            }
            for (DataItem dataItem2 : this.phNumbers) {
                if (!TextUtils.isEmpty(simContact.number)) {
                    if (!TextUtils.isEmpty(simContact.anr)) {
                        break;
                    }
                    simContact.anr = dataItem2.data;
                } else {
                    simContact.number = dataItem2.data;
                }
            }
            contactList.add(simContact);
        }
    }

    private static class DataItem {
        public String data;
        public String label;
        public String type;
    }

    private class LoadADNRecordSize extends AsyncTask<Void, Void, Void> {
        private LoadADNRecordSize() {
        }

        protected Void doInBackground(Void... params) {
            IIccPhoneBookAdapter lIccPhoneBookAdapter;
            if (SimFactoryManager.isDualSim()) {
                lIccPhoneBookAdapter = new IIccPhoneBookAdapter(SimFactoryManager.getSlotIdBasedOnAccountType(AdvancedSimPersistenceManager.this.mAccountType));
            } else {
                lIccPhoneBookAdapter = new IIccPhoneBookAdapter();
            }
            try {
                int[] result = lIccPhoneBookAdapter.getAdnRecordsSize();
                if (result == null || result.length <= 0) {
                    AdvancedSimPersistenceManager.this.mMaxNameLength = 14;
                    return null;
                }
                AdvancedSimPersistenceManager.this.mMaxNameLength = result[0] - 14;
                return null;
            } catch (UnsupportedException e) {
                HwLog.e("AdvancedSimPersistenceManager", "getRecordsSize() is unsupported in initSimRecordsSize()");
            }
        }

        protected void onPostExecute(Void unused) {
        }
    }

    public AdvancedSimPersistenceManager(SimConfig config, Uri aProviderUri, String accountType) {
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
        if (toCheckSpace && this.mConfig.getAvailableFreeSpace() <= 0) {
            return null;
        }
        if (aContact instanceof AdvancedSimContact) {
            AdvancedSimContact contact = (AdvancedSimContact) aContact;
            if (HwLog.HWDBG) {
                HwLog.d("AdvancedSimPersistenceManager", "insert");
            }
            if (TextUtils.isEmpty(contact.name)) {
                contact.name = "";
            }
            if (TextUtils.isEmpty(contact.email) && this.mConfig.isEmailEnabled()) {
                contact.email = "";
            }
            if (TextUtils.isEmpty(contact.number)) {
                contact.number = "";
            } else {
                contact.number = CommonUtilMethods.extractNetworkPortion(contact.number);
            }
            if (TextUtils.isEmpty(contact.anr) || !this.mConfig.isANREnabled()) {
                contact.anr = "";
            } else {
                contact.anr = CommonUtilMethods.extractNetworkPortion(contact.anr);
            }
            ContentValues values = new ContentValues();
            values.put("tag", contact.name);
            values.put("number", contact.number);
            try {
                values.put((String) IccProviderUtilsEx.getSimAnr().get("SIM_ANR"), contact.anr);
            } catch (NoExtAPIException e) {
                e.printStackTrace();
            }
            values.put("emails", contact.email);
            if (HwLog.HWDBG) {
                HwLog.d("AdvancedSimPersistenceManager", "Inserting Values ");
            }
            if (5 != SimFactoryManager.getSimState(SimFactoryManager.getSlotIdBasedOnAccountType(this.mAccountType))) {
                return null;
            }
            Uri uri = null;
            Cursor cursor;
            try {
                uri = this.mContext.getContentResolver().insert(this.mProviderUri, values);
                if (uri == null) {
                    ExceptionCapture.captureSimContactSaveException("the returned uri is null when inserting SIM contact to SIM", null);
                    HwLog.e("AdvancedSimPersistenceManager", "Unable to insert the contact in SIM ");
                    return null;
                }
                if (HwLog.HWDBG) {
                    HwLog.d("AdvancedSimPersistenceManager", "Inserted Uri is ");
                }
                List<String> segments = uri.getPathSegments();
                if (segments != null) {
                    int size = segments.size();
                    contact.efid = (String) segments.get(size - 1);
                    contact.index = (String) segments.get(size - 2);
                }
                cursor = this.mContext.getContentResolver().query(this.mProviderUri, null, "efid = '" + contact.efid + "' AND " + "index" + " = '" + contact.index + "'", null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        AdvancedSimContact lContact = (AdvancedSimContact) getContact(cursor);
                        contact.name = lContact.name;
                        contact.number = lContact.number;
                        contact.email = lContact.email;
                        contact.anr = lContact.anr;
                    }
                    cursor.close();
                }
                return uri;
            } catch (SQLiteException e2) {
                ExceptionCapture.captureSimContactSaveException("inserting SIM contact to SIM error: " + e2, e2);
                HwLog.e("AdvancedSimPersistenceManager", "Error Inserting Contacts to SIM, ", e2);
            } catch (RuntimeException e3) {
                ExceptionCapture.captureSimContactSaveException("inserting SIM contact to SIM error: " + e3, e3);
            } catch (Throwable th) {
                cursor.close();
            }
        } else {
            HwLog.e("AdvancedSimPersistenceManager", "aContact is not instance of AdvancedSimContact!");
            return null;
        }
    }

    public int delete(SimContact aContact) {
        if (aContact instanceof AdvancedSimContact) {
            AdvancedSimContact simContact = (AdvancedSimContact) aContact;
            return delete(simContact.efid, simContact.index);
        }
        HwLog.e("AdvancedSimPersistenceManager", "contact is not instanceof AdvancedSimContact!");
        return 0;
    }

    public SimContact getContact(Cursor aCursor) {
        AdvancedSimContact simContact = new AdvancedSimContact();
        int coulmIndex = aCursor.getColumnIndex("name");
        if (-1 != coulmIndex) {
            simContact.name = aCursor.getString(coulmIndex);
        }
        coulmIndex = aCursor.getColumnIndex("number");
        if (-1 != coulmIndex) {
            simContact.number = aCursor.getString(coulmIndex);
        }
        coulmIndex = aCursor.getColumnIndex("emails");
        if (-1 != coulmIndex) {
            simContact.email = aCursor.getString(coulmIndex);
        }
        coulmIndex = aCursor.getColumnIndex("efid");
        if (-1 != coulmIndex) {
            simContact.efid = aCursor.getString(coulmIndex);
        }
        coulmIndex = aCursor.getColumnIndex("index");
        if (-1 != coulmIndex) {
            simContact.index = aCursor.getString(coulmIndex);
        }
        try {
            coulmIndex = aCursor.getColumnIndex((String) IccProviderUtilsEx.getSimAnr().get("SIM_ANR"));
            if (-1 != coulmIndex) {
                simContact.anr = aCursor.getString(coulmIndex);
            }
        } catch (NoExtAPIException e) {
            e.printStackTrace();
        }
        return simContact;
    }

    public void getContacts(Entity entity, List<SimContact> contactList) {
        if (contactList != null) {
            IIccPhoneBookAdapter lIccPhoneBookAdapter;
            AdvancedContactInfo lContact = new AdvancedContactInfo();
            lContact.isAnrEnable = this.mConfig.isANREnabled();
            for (NamedContentValues ncValues : entity.getSubValues()) {
                DataItem dateItem;
                ContentValues values = ncValues.values;
                if ("vnd.android.cursor.item/name".equals(values.getAsString("mimetype"))) {
                    lContact.name = values.getAsString("data1");
                }
                if ("vnd.android.cursor.item/phone_v2".equals(values.getAsString("mimetype"))) {
                    String number = values.getAsString("data1");
                    dateItem = new DataItem();
                    dateItem.data = CommonUtilMethods.extractNetworkPortion(number);
                    dateItem.type = values.getAsString("data2");
                    dateItem.label = values.getAsString("data3");
                    lContact.phNumbers.add(dateItem);
                }
                if ("vnd.android.cursor.item/email_v2".equals(values.getAsString("mimetype")) && this.mConfig.isEmailEnabled()) {
                    dateItem = new DataItem();
                    dateItem.data = values.getAsString("data1");
                    dateItem.type = values.getAsString("data2");
                    dateItem.label = values.getAsString("data3");
                    lContact.emails.add(dateItem);
                }
            }
            if (SimFactoryManager.isDualSim()) {
                lIccPhoneBookAdapter = new IIccPhoneBookAdapter(SimFactoryManager.getSlotIdBasedOnAccountType(this.mAccountType));
            } else {
                lIccPhoneBookAdapter = new IIccPhoneBookAdapter();
            }
            lContact.populateContactListForCopy(contactList, lIccPhoneBookAdapter, this.mMaxNameLength);
        }
    }

    public void getSimContacts(Entity entity, List<SimContact> contactList) {
        if (contactList != null) {
            AdvancedContactInfo lContact = new AdvancedContactInfo();
            ContentValues entityValues = entity.getEntityValues();
            if (CommonUtilMethods.isSimAccount(entityValues.getAsString("account_type"))) {
                String lId = entityValues.getAsString("_id");
                String lEfid = entityValues.getAsString("sync1");
                String lIndex = entityValues.getAsString("sync2");
                lContact.id = lId;
                lContact.efid = lEfid;
                lContact.index = lIndex;
            }
            for (NamedContentValues ncValues : entity.getSubValues()) {
                DataItem dateItem;
                ContentValues values = ncValues.values;
                if ("vnd.android.cursor.item/name".equals(values.getAsString("mimetype"))) {
                    lContact.name = values.getAsString("data1");
                }
                if ("vnd.android.cursor.item/phone_v2".equals(values.getAsString("mimetype"))) {
                    dateItem = new DataItem();
                    dateItem.data = values.getAsString("data1");
                    dateItem.type = values.getAsString("data2");
                    dateItem.label = values.getAsString("data3");
                    lContact.phNumbers.add(dateItem);
                }
                if ("vnd.android.cursor.item/email_v2".equals(values.getAsString("mimetype"))) {
                    dateItem = new DataItem();
                    dateItem.data = values.getAsString("data1");
                    dateItem.type = values.getAsString("data2");
                    dateItem.label = values.getAsString("data3");
                    lContact.emails.add(dateItem);
                }
            }
            lContact.populateContactListForCompare(contactList);
        }
    }

    public Cursor queryAll() {
        return this.mContext.getContentResolver().query(this.mProviderUri, null, null, null, null);
    }

    private String prepareWhereClause(SimContact aContact) {
        AdvancedSimContact contact = (AdvancedSimContact) aContact;
        StringBuilder builder = new StringBuilder();
        builder.append("tag='");
        builder.append(contact.name);
        builder.append("' AND number='");
        builder.append(contact.number);
        builder.append("'");
        if (!TextUtils.isEmpty(contact.email)) {
            builder.append(" AND emails='");
            builder.append(contact.email);
            builder.append("'");
        }
        if (!TextUtils.isEmpty(contact.anr)) {
            try {
                builder.append(" AND ").append((String) IccProviderUtilsEx.getSimAnr().get("SIM_ANR")).append("='");
            } catch (NoExtAPIException e) {
                e.printStackTrace();
            }
            builder.append(contact.anr);
            builder.append("'");
        }
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
        if (aOldContact instanceof AdvancedSimContact) {
            AdvancedSimContact oldContact = (AdvancedSimContact) aOldContact;
            if (aNewContact instanceof AdvancedSimContact) {
                AdvancedSimContact newContact = (AdvancedSimContact) aNewContact;
                if (HwLog.HWDBG) {
                    HwLog.d("AdvancedSimPersistenceManager", "update");
                }
                String whereClause = prepareWhereClause(oldContact);
                ContentValues simValues = new ContentValues();
                simValues.put("tag", oldContact.name);
                simValues.put("number", oldContact.number);
                try {
                    simValues.put((String) IccProviderUtilsEx.getSimAnr().get("SIM_ANR"), oldContact.anr);
                    simValues.put((String) IccProviderUtilsEx.getSimAnr().get("SIM_NEW_ANR"), newContact.anr);
                } catch (NoExtAPIException e) {
                    e.printStackTrace();
                }
                simValues.put("emails", oldContact.email);
                simValues.put("newEmails", newContact.email);
                simValues.put("newTag", newContact.name);
                simValues.put("newNumber", newContact.number);
                simValues.put("efid", newContact.efid);
                simValues.put("index", newContact.index);
                if (5 != SimFactoryManager.getSimState(SimFactoryManager.getSlotIdBasedOnAccountType(this.mAccountType))) {
                    return 0;
                }
                int ret = 0;
                try {
                    ret = this.mContext.getContentResolver().update(this.mProviderUri, simValues, whereClause, null);
                    if (ret <= 0) {
                        ExceptionCapture.captureSimContactSaveException("the returned value is " + ret + " when updating SIM contact to SIM", null);
                    }
                } catch (RuntimeException e2) {
                    ExceptionCapture.captureSimContactSaveException("updating SIM contact to SIM error: " + e2, e2);
                }
                return ret;
            }
            HwLog.e("AdvancedSimPersistenceManager", "aNewContact is not a instance of AdvancedSimContact!");
            return 0;
        }
        HwLog.e("AdvancedSimPersistenceManager", "aOldContact is not a instance of AdvancedSimContact!");
        return 0;
    }

    public int save(RawContactDeltaList state) {
        int update = -1;
        if (5 != SimFactoryManager.getSimState(SimFactoryManager.getSlotIdBasedOnAccountType(this.mAccountType))) {
            return 0;
        }
        AdvancedSimContact lNewContact = new AdvancedSimContact();
        ValuesDelta lNameDelta = null;
        ValuesDelta lEmailDelta = null;
        ValuesDelta lNumberDelta = null;
        ValuesDelta lAnrDelta = null;
        ValuesDelta values = null;
        for (RawContactDelta delta : state) {
            ArrayList<ValuesDelta> nameEntries = delta.getMimeEntries("vnd.android.cursor.item/name");
            if (nameEntries != null) {
                lNameDelta = (ValuesDelta) nameEntries.get(0);
                if (!(lNameDelta == null || lNameDelta.isDelete())) {
                    lNewContact.name = lNameDelta.getAsString("data1");
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
            ArrayList<ValuesDelta> numberEntries = delta.getMimeEntries("vnd.android.cursor.item/phone_v2");
            if (numberEntries != null) {
                int size = numberEntries.size();
                int flag = 0;
                if (size > 0) {
                    int index;
                    for (index = 0; index < size; index++) {
                        lNumberDelta = (ValuesDelta) numberEntries.get(index);
                        if (lNumberDelta != null && !lNumberDelta.isDelete()) {
                            flag = index;
                            lNewContact.number = CommonUtilMethods.extractNetworkPortion(lNumberDelta.getAsString("data1"));
                            break;
                        }
                    }
                    for (index = flag + 1; index < size; index++) {
                        lAnrDelta = (ValuesDelta) numberEntries.get(index);
                        if (lAnrDelta != null && !lAnrDelta.isDelete()) {
                            lNewContact.anr = CommonUtilMethods.extractNetworkPortion(lAnrDelta.getAsString("data1"));
                            break;
                        }
                    }
                }
            }
            ArrayList<ValuesDelta> emailEntries = delta.getMimeEntries("vnd.android.cursor.item/email_v2");
            if (!(emailEntries == null || emailEntries.isEmpty())) {
                lEmailDelta = (ValuesDelta) emailEntries.get(0);
                if (lEmailDelta.isDelete()) {
                    lNewContact.email = "";
                } else {
                    lNewContact.email = lEmailDelta.getAsString("data1");
                }
            }
            values = delta.getValues();
            lNewContact.efid = values.getAsString("sync1");
            lNewContact.index = values.getAsString("sync2");
        }
        if (lNewContact.efid != null && lNewContact.index != null) {
            Cursor cursor = this.mContext.getContentResolver().query(this.mProviderUri, null, "efid = '" + lNewContact.efid + "' AND " + "index" + " = '" + lNewContact.index + "'", null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        update = update(getContact(cursor), lNewContact);
                        cursor.close();
                        cursor = this.mContext.getContentResolver().query(this.mProviderUri, null, "efid='" + lNewContact.efid + "' AND " + "index" + "='" + lNewContact.index + "'", null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            lNewContact = (AdvancedSimContact) getContact(cursor);
                        }
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } else if (insert(lNewContact, true) == null) {
            HwLog.e("AdvancedSimPersistenceManager", "Unable to insert contact in SIM card");
            return -1;
        } else {
            Cursor crossCheckSimCursor = this.mContext.getContentResolver().query(this.mProviderUri, null, "efid='" + lNewContact.efid + "' AND " + "index" + "='" + lNewContact.index + "'", null, null);
            if (crossCheckSimCursor != null) {
                try {
                    if (crossCheckSimCursor.moveToFirst()) {
                        lNewContact = (AdvancedSimContact) getContact(crossCheckSimCursor);
                    }
                } catch (Exception e) {
                    if (crossCheckSimCursor != null) {
                        crossCheckSimCursor.close();
                    }
                } catch (Throwable th2) {
                    if (crossCheckSimCursor != null) {
                        crossCheckSimCursor.close();
                    }
                }
            }
            if (crossCheckSimCursor != null) {
                crossCheckSimCursor.close();
            }
            update = 1;
        }
        if (values != null) {
            ValuesDelta valuesDelta = values;
            valuesDelta.put("sync1", lNewContact.efid);
            valuesDelta = values;
            valuesDelta.put("sync2", lNewContact.index);
            values.put("sync3", lNewContact.hashCode());
        }
        if (!(lNameDelta == null || TextUtils.isEmpty(lNewContact.name))) {
            valuesDelta = lNameDelta;
            valuesDelta.put("data1", lNewContact.name);
        }
        if (!(lEmailDelta == null || TextUtils.isEmpty(lNewContact.email))) {
            valuesDelta = lEmailDelta;
            valuesDelta.put("data1", lNewContact.email);
        }
        if (lNumberDelta != null) {
            if (TextUtils.isEmpty(lNewContact.number)) {
                lNumberDelta.markDeleted();
            } else {
                valuesDelta = lNumberDelta;
                valuesDelta.put("data1", lNewContact.number);
            }
        }
        if (lAnrDelta != null) {
            if (TextUtils.isEmpty(lNewContact.anr)) {
                lAnrDelta.markDeleted();
            } else {
                valuesDelta = lAnrDelta;
                valuesDelta.put("data1", lNewContact.anr);
            }
        }
        return update;
    }

    public int delete(String efid, String index) {
        if (5 != SimFactoryManager.getSimState(SimFactoryManager.getSlotIdBasedOnAccountType(this.mAccountType))) {
            return 0;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("efid='");
        builder.append(efid);
        builder.append("'");
        builder.append(" AND index='");
        builder.append(index);
        builder.append("'");
        try {
            int numOfContactsDeleted = this.mContext.getContentResolver().delete(this.mProviderUri, builder.toString(), null);
            if (numOfContactsDeleted <= 0) {
                ExceptionCapture.captureSimContactDeleteException("the returned value is " + numOfContactsDeleted + " when deleting SIM contact from SIM", null);
            }
            return numOfContactsDeleted;
        } catch (IllegalArgumentException e) {
            HwLog.e("AdvancedSimPersistenceManager", "IllegalArgumentException");
            ExceptionCapture.captureSimContactDeleteException("deleting SIM contact from SIM error: " + e, e);
            return 0;
        } catch (RuntimeException e2) {
            ExceptionCapture.captureSimContactDeleteException("deleting SIM contact from SIM error: " + e2, e2);
            return 0;
        }
    }

    public void performHealthCheck(String aAccountType) {
        String[] selectionArgs = new String[]{aAccountType};
        Uri uri = RawContacts.CONTENT_URI.buildUpon().appendQueryParameter("caller_is_syncadapter", "true").build();
        this.mContext.getContentResolver().delete(uri, "account_type=? AND (sync1 IS NULL OR sync2 IS NULL)", selectionArgs);
    }
}
