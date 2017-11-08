package com.android.contacts.hap.sprint.preload;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.SettingsEx.Systemex;
import android.util.Log;
import com.huawei.sprint.chameleon.provider.ChameleonContract;
import java.util.ArrayList;

public class HwCustPreloadContacts {
    public static final String EMPTY_STRING = " ";
    public static final long INVALID_ID = -1;
    public static final String NULL_STRING = "null";
    public static final String PRELOAD_CONTACT = "contacts_preloaded";
    public static final String SYSTEM_PRELOAD_CONTACT = "preload_contact_version";
    private static final String TAG = "PreloadContacts";
    private volatile boolean isRunning = false;
    private Context mContext;
    private LoadPreloadContacts mLoadPreloadContacts = new LoadPreloadContacts();
    private ArrayList<ContactData> mPreloadedContacts = new ArrayList();

    static class ContactData {
        private String mCustom = null;
        private String mFirstname = null;
        private boolean mIsDC;
        private String mNumber = null;
        private String mSecondname = null;
        private int mType;
        private String mURL = null;

        ContactData(String fname, String sname, String number, String type, String url) {
            boolean z = false;
            this.mFirstname = fname;
            this.mSecondname = sname;
            this.mNumber = number;
            this.mType = getType(type);
            if (this.mType == 0) {
                z = true;
            }
            this.mIsDC = z;
            this.mCustom = "DC";
            this.mURL = url;
        }

        public String getFirstName() {
            return this.mFirstname;
        }

        public String getSecondName() {
            return this.mSecondname;
        }

        public String getNumber() {
            return this.mNumber;
        }

        public boolean isDC() {
            return this.mIsDC;
        }

        public String getUrl() {
            return this.mURL;
        }

        public String getCustomName() {
            return this.mCustom;
        }

        public int getMobileType() {
            return this.mType;
        }

        private int getType(String type) {
            if ("Mobile".equalsIgnoreCase(type)) {
                return 2;
            }
            if ("Home".equalsIgnoreCase(type)) {
                return 1;
            }
            if ("Work".equalsIgnoreCase(type)) {
                return 3;
            }
            if ("Pager".equalsIgnoreCase(type)) {
                return 6;
            }
            if ("Fax".equalsIgnoreCase(type)) {
                return 5;
            }
            if ("DC".equalsIgnoreCase(type)) {
                return 0;
            }
            return 7;
        }
    }

    class LoadPreloadContacts extends AsyncTask<Void, Void, Void> {
        LoadPreloadContacts() {
        }

        protected Void doInBackground(Void... params) {
            HwCustPreloadContacts.this.isRunning = true;
            long dbVersion = getDBVersion();
            if (dbVersion != getPrefVersion()) {
                Log.w(HwCustPreloadContacts.TAG, " db is updated , reloading the data ...");
                HwCustPreloadContacts.deletePredefienedContactsIfPresent(HwCustPreloadContacts.this.mContext);
                readPreloadContactsFromChameleonDB();
                insertContactsToDB();
                saveToPref(dbVersion);
            }
            return null;
        }

        private void saveToPref(long dbVersion) {
            Systemex.putLong(HwCustPreloadContacts.this.mContext.getContentResolver(), HwCustPreloadContacts.SYSTEM_PRELOAD_CONTACT, dbVersion);
        }

        private void insertContactsToDB() {
            insertContactstoDBFromList();
        }

        private void insertContactstoDBFromList() {
            ArrayList<ContentProviderOperation> operation = new ArrayList();
            for (ContactData data : HwCustPreloadContacts.this.mPreloadedContacts) {
                int backReference = operation.size();
                Builder lBuilder = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI);
                lBuilder.withValue("account_name", "Phone");
                lBuilder.withValue("account_type", "com.android.huawei.phone");
                lBuilder.withValue("sync4", "PREDEFINED_HUAWEI_CONTACT");
                lBuilder.withValue("raw_contact_is_read_only", Boolean.valueOf(true));
                operation.add(lBuilder.build());
                lBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                lBuilder.withValue("mimetype", "vnd.android.cursor.item/name");
                boolean hasFirstName = data.getFirstName() != null ? !"null".equalsIgnoreCase(data.getFirstName()) : false;
                boolean hasSecondName = data.getSecondName() != null ? !"null".equalsIgnoreCase(data.getSecondName()) : false;
                StringBuffer fullName = new StringBuffer();
                if (hasFirstName) {
                    lBuilder.withValue("data2", data.getFirstName());
                    fullName.append(data.getFirstName());
                } else {
                    fullName.append(HwCustPreloadContacts.EMPTY_STRING);
                }
                if (hasFirstName && hasSecondName) {
                    fullName.append(HwCustPreloadContacts.EMPTY_STRING);
                }
                if (hasSecondName) {
                    lBuilder.withValue("data3", data.getSecondName());
                    fullName.append(data.getSecondName());
                } else {
                    fullName.append(HwCustPreloadContacts.EMPTY_STRING);
                }
                lBuilder.withValue("data1", fullName.toString());
                lBuilder.withValueBackReference("raw_contact_id", backReference);
                operation.add(lBuilder.build());
                boolean hasNumber = data.getNumber() != null ? !"null".equalsIgnoreCase(data.getNumber()) : false;
                if (hasNumber) {
                    lBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                    lBuilder.withValue("mimetype", "vnd.android.cursor.item/phone_v2");
                    lBuilder.withValue("data1", data.getNumber());
                    if (data.isDC()) {
                        lBuilder.withValue("data3", data.getCustomName());
                    }
                    lBuilder.withValue("data2", Integer.valueOf(data.getMobileType()));
                    lBuilder.withValueBackReference("raw_contact_id", backReference);
                    operation.add(lBuilder.build());
                }
                boolean hasUrl = data.getUrl() != null ? !"null".equalsIgnoreCase(data.getUrl()) : false;
                if (hasUrl) {
                    lBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                    lBuilder.withValueBackReference("raw_contact_id", backReference);
                    lBuilder.withValue("mimetype", "vnd.android.cursor.item/website");
                    lBuilder.withValue("data1", data.getUrl());
                    operation.add(lBuilder.build());
                }
            }
            try {
                HwCustPreloadContacts.this.mContext.getContentResolver().applyBatch("com.android.contacts", operation);
            } catch (OperationApplicationException e) {
                e.printStackTrace();
            } catch (RemoteException e2) {
                e2.printStackTrace();
            }
        }

        private void readPreloadContactsFromChameleonDB() {
            Cursor cursor = null;
            try {
                cursor = HwCustPreloadContacts.this.mContext.getContentResolver().query(ChameleonContract.CONTENT_URI_CONTACTS, new String[]{"value", "category"}, "category='contacts_preloaded'", null, null);
                if (cursor == null) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return;
                }
                if (cursor.moveToFirst()) {
                    do {
                        String value = cursor.getString(0);
                        if (!(value == null || value.isEmpty())) {
                            ContactData data = HwCustPreloadContacts.this.parseData(value);
                            if (data != null) {
                                HwCustPreloadContacts.this.mPreloadedContacts.add(data);
                            }
                        }
                    } while (cursor.moveToNext());
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (SQLiteException e) {
                Log.w(HwCustPreloadContacts.TAG, "=====Error is thrown-- Stop querying the db=====" + e);
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        private long getPrefVersion() {
            return Systemex.getLong(HwCustPreloadContacts.this.mContext.getContentResolver(), HwCustPreloadContacts.SYSTEM_PRELOAD_CONTACT, -1);
        }

        protected void onPostExecute(Void result) {
            HwCustPreloadContacts.this.isRunning = false;
            super.onPostExecute(result);
        }

        private long getDBVersion() {
            long dbVersion = -1;
            Cursor cursor = null;
            try {
                cursor = HwCustPreloadContacts.this.mContext.getContentResolver().query(ChameleonContract.CONTENT_URI_VERSION, new String[]{"category", "version"}, null, null, null);
                if (cursor == null) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return -1;
                }
                if (cursor.moveToFirst()) {
                    do {
                        if (HwCustPreloadContacts.PRELOAD_CONTACT.equals(cursor.getString(0))) {
                            dbVersion = cursor.getLong(1);
                        }
                    } while (cursor.moveToNext());
                }
                if (cursor != null) {
                    cursor.close();
                }
                return dbVersion;
            } catch (SQLiteException e) {
                Log.w(HwCustPreloadContacts.TAG, "=====Error is thrown-- Stop querying the db=====" + e);
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

    public void loadPreloadContactsIfNeeded(Context context) {
        this.mContext = context;
        if (!this.isRunning) {
            this.mLoadPreloadContacts.execute(new Void[0]);
        }
    }

    private ContactData parseData(String value) {
        String[] values = value.split(",");
        if (values.length != 5) {
            return null;
        }
        return new ContactData(values[0].trim(), values[1].trim(), values[2].trim(), values[3].trim(), values[4].trim());
    }

    public static int deletePredefienedContactsIfPresent(Context context) {
        if (context == null) {
            return 0;
        }
        return context.getContentResolver().delete(RawContacts.CONTENT_URI, "sync4 = 'PREDEFINED_HUAWEI_CONTACT' AND deleted = 0 ", null);
    }
}
