package com.huawei.systemmanager.util.contacts;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;
import com.google.common.base.Objects;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.customize.AbroadUtils;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.contacts.ContactsObject.ContactsUpdateMap;
import com.huawei.systemmanager.util.phonematch.PhoneMatch;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ContactsObserverHelper {
    private static final long CONTACTS_CHANGED_HANDLE_DELAY = 10000;
    private static final int INDEX_DISPLAY_NAME = 1;
    private static final int INDEX_NUMBER = 0;
    private static final int MSG_CONTACTS_CHANGED = 1;
    private static final String PREF_KEY_FACTORYRESET = "FactoryResetFlag";
    private static final String TAG = "ContactsObserverHelper";
    private static ContactsObserverHelper sInstance = null;
    private Context mContext = null;
    private Handler mHandler = null;
    private ContentObserver mObserverEntrance = null;
    private List<IContactsObserver> mObserverList = new ArrayList();

    private class ContactsContentObserver extends ContentObserver {
        public ContactsContentObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            ContactsObserverHelper.this.sendContactsChangeMsg(true);
        }
    }

    private class ContactsUpdateThread extends Thread {
        public ContactsUpdateThread(String name) {
            super(name);
        }

        public void run() {
            if (Utility.isNullOrEmptyList(ContactsObserverHelper.this.mObserverList)) {
                HwLog.d(ContactsObserverHelper.TAG, "Empty observer list");
                return;
            }
            Map<IContactsObserver, List<ContactsUpdateMap>> observerContactsMap = new HashMap();
            for (IContactsObserver observer : ContactsObserverHelper.this.mObserverList) {
                try {
                    List<ContactsUpdateMap> contactsList = observer.onPrepareContactsChange();
                    if (!Utility.isNullOrEmptyList(contactsList)) {
                        observerContactsMap.put(observer, contactsList);
                    }
                } catch (Exception e) {
                    HwLog.e(ContactsObserverHelper.TAG, "Exception: ", e);
                }
            }
            if (observerContactsMap.isEmpty()) {
                HwLog.d(ContactsObserverHelper.TAG, "No observer needs update");
                return;
            }
            Map<String, MatchContact> sysContactsMap = ContactsObserverHelper.this.getSysContactsMap();
            if (sysContactsMap == null) {
                HwLog.w(ContactsObserverHelper.TAG, "Invalid system contacts ,skip");
                return;
            }
            ContactsObserverHelper.this.applyContactsUpdate(ContactsObserverHelper.this.getUpdateMap(observerContactsMap, sysContactsMap));
        }
    }

    public static class MatchContact {
        String contactName;
        String originNumber;
    }

    static class MyHandler extends Handler {
        WeakReference<ContactsObserverHelper> mObserver;

        MyHandler(ContactsObserverHelper observer) {
            this.mObserver = new WeakReference(observer);
        }

        public void handleMessage(Message msg) {
            ContactsObserverHelper observer = (ContactsObserverHelper) this.mObserver.get();
            if (observer == null) {
                HwLog.e(ContactsObserverHelper.TAG, "handleMessage: Reference to the observer is lost");
                return;
            }
            switch (msg.what) {
                case 1:
                    observer.onContactsChanged();
                    break;
            }
        }
    }

    public static synchronized ContactsObserverHelper getInstance(Context context) {
        ContactsObserverHelper contactsObserverHelper;
        synchronized (ContactsObserverHelper.class) {
            if (sInstance == null) {
                sInstance = new ContactsObserverHelper(context.getApplicationContext());
            }
            contactsObserverHelper = sInstance;
        }
        return contactsObserverHelper;
    }

    public void registerObserver(IContactsObserver observer) {
        if (observer == null) {
            HwLog.e(TAG, "registerObserver: Invalid observer");
            return;
        }
        synchronized (this.mObserverList) {
            registerObserverEntrance();
            this.mObserverList.add(observer);
        }
    }

    public void unregisterObserver(IContactsObserver observer) {
        if (observer == null) {
            HwLog.e(TAG, "unregisterObserver: Invalid observer");
            return;
        }
        synchronized (this.mObserverList) {
            this.mObserverList.remove(observer);
            if (Utility.isNullOrEmptyList(this.mObserverList)) {
                unregisterObserverEntrance();
            }
        }
    }

    private ContactsObserverHelper(Context context) {
        this.mContext = context;
        this.mHandler = new MyHandler(this);
    }

    private void registerObserverEntrance() {
        if (this.mObserverEntrance == null) {
            try {
                this.mObserverEntrance = new ContactsContentObserver();
                this.mContext.getContentResolver().registerContentObserver(ContactsContract.AUTHORITY_URI, true, this.mObserverEntrance);
                triggerContactsSyncOnStart();
                HwLog.d(TAG, "registerContactsObserver : Finished");
            } catch (Exception e) {
                this.mObserverEntrance = null;
                HwLog.e(TAG, "registerContactsObserver: Exception", e);
            }
        }
    }

    private void triggerContactsSyncOnStart() {
        if (isFirstStartOnFactoryReset()) {
            HwLog.i(TAG, "triggerContactsSyncOnStart:Skip on first start after factory reset");
            return;
        }
        HwLog.i(TAG, "triggerContactsSyncOnStart:trigger first contacts sync event");
        sendContactsChangeMsg(false);
    }

    private void sendContactsChangeMsg(boolean bDelaySend) {
        this.mHandler.removeMessages(1);
        Message msg = this.mHandler.obtainMessage();
        msg.what = 1;
        if (bDelaySend) {
            this.mHandler.sendMessageDelayed(msg, 10000);
        } else {
            this.mHandler.sendMessage(msg);
        }
    }

    private void unregisterObserverEntrance() {
        if (this.mObserverEntrance != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mObserverEntrance);
            this.mObserverEntrance = null;
            HwLog.d(TAG, "unregisterContactsObserver : Finished");
        }
    }

    private boolean isFirstStartOnFactoryReset() {
        SharedPreferences sp = this.mContext.getSharedPreferences(TAG, 4);
        boolean bState = sp.getBoolean(PREF_KEY_FACTORYRESET, true);
        if (bState) {
            Editor editor = sp.edit();
            editor.putBoolean(PREF_KEY_FACTORYRESET, false);
            editor.commit();
        }
        return bState;
    }

    private void onContactsChanged() {
        HwLog.d(TAG, "onContactsChanged: Start to handle contacts change message");
        new ContactsUpdateThread("Util_ContactsUpdateThread").start();
    }

    private void applyContactsUpdate(Map<IContactsObserver, List<ContactsUpdateMap>> updateMap) {
        HwLog.d(TAG, "applyContactsUpdate: Starts");
        for (Entry<IContactsObserver, List<ContactsUpdateMap>> entry : updateMap.entrySet()) {
            try {
                ((IContactsObserver) entry.getKey()).onContactsChange((List) entry.getValue());
            } catch (Exception e) {
                HwLog.e(TAG, "applyContactsUpdate: Exception", e);
            }
        }
        HwLog.i(TAG, "applyContactsUpdate: Ends");
    }

    private Map<IContactsObserver, List<ContactsUpdateMap>> getUpdateMap(Map<IContactsObserver, List<ContactsUpdateMap>> observerContactsMap, Map<String, MatchContact> sysContactsMap) {
        for (Entry<IContactsObserver, List<ContactsUpdateMap>> observerEntry : observerContactsMap.entrySet()) {
            for (ContactsUpdateMap updateItem : (List) observerEntry.getValue()) {
                Map<String, String> contactsMap = updateItem.getContatcsMap();
                Map<String, String> contactsUpdateMap = new HashMap();
                for (Entry<String, String> contactsEntry : contactsMap.entrySet()) {
                    String phone = (String) contactsEntry.getKey();
                    MatchContact matchContact = (MatchContact) sysContactsMap.get(PhoneMatch.getPhoneNumberMatchInfo(phone).getPhoneNumber());
                    if (isNeedUpdate(phone, (String) contactsEntry.getValue(), matchContact)) {
                        String sysContactName = matchContact.contactName;
                        if (TextUtils.isEmpty(sysContactName)) {
                            sysContactName = "";
                        }
                        contactsUpdateMap.put(phone, sysContactName);
                    }
                }
                updateItem.setContactsMap(contactsUpdateMap);
            }
        }
        return observerContactsMap;
    }

    private boolean isNeedUpdate(String localPhone, String localName, MatchContact matchContact) {
        if (matchContact == null) {
            return false;
        }
        String sysName = matchContact.contactName;
        if (TextUtils.isEmpty(localName) && TextUtils.isEmpty(sysName)) {
            return false;
        }
        if (!TextUtils.isEmpty(localName) && !TextUtils.isEmpty(sysName) && localName.equals(sysName)) {
            return false;
        }
        if (!TextUtils.isEmpty(localName) && TextUtils.isEmpty(sysName)) {
            return false;
        }
        if (PhoneMatch.isConfigured() || AbroadUtils.isAbroad()) {
            return true;
        }
        return Objects.equal(PhoneMatch.getDefalutChinaMatchPhoneNumber(localPhone), PhoneMatch.getDefalutChinaMatchPhoneNumber(matchContact.originNumber));
    }

    private Map<String, MatchContact> getSysContactsMap() {
        try {
            Cursor cursor = this.mContext.getContentResolver().query(Phone.CONTENT_URI, new String[]{"data1", "display_name"}, null, null, null);
            if (cursor == null) {
                HwLog.w(TAG, "getSysContactsMap: Fail to get system contatcs");
                return null;
            }
            Map<String, MatchContact> contactsMap = HsmCollections.newHashMap();
            if (cursor.getCount() <= 0) {
                cursor.close();
                return contactsMap;
            }
            while (cursor.moveToNext()) {
                try {
                    String number = cursor.getString(0);
                    String name = cursor.getString(1);
                    String matchNumber = PhoneMatch.getPhoneNumberMatchInfo(number).getPhoneNumber();
                    MatchContact matchContact = new MatchContact();
                    matchContact.originNumber = number;
                    matchContact.contactName = name;
                    contactsMap.put(matchNumber, matchContact);
                } catch (Exception e) {
                    HwLog.e(TAG, "getSysContactsMap: exception", e);
                    return null;
                } finally {
                    cursor.close();
                }
            }
            return contactsMap;
        } catch (Exception e2) {
            HwLog.e(TAG, "getSysContactsMap: Exception", e2);
            return null;
        }
    }
}
