package com.huawei.harassmentinterception.receiver;

import android.content.Context;
import com.huawei.harassmentinterception.common.CommonObject.ContactInfo;
import com.huawei.harassmentinterception.common.CommonObject.WhitelistInfo;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.contacts.ContactsObject.ContactsUpdateMap;
import com.huawei.systemmanager.util.contacts.IContactsObserver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ContactsObserver implements IContactsObserver {
    private static final String TAG = "ContactsObserver";
    private static final int TAG_BLACKLIST_CONTACTS = 0;
    private static final int TAG_CALL_CONTACTS = 1;
    private static final int TAG_MSG_CONTACTS = 2;
    private static final int TAG_WHITELIST_CONTACTS = 3;
    private Context mContext = null;

    public ContactsObserver(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public List<ContactsUpdateMap> onPrepareContactsChange() {
        HwLog.d(TAG, "onPreContactsChange: Starts");
        return loadLocalContacts();
    }

    public void onContactsChange(List<ContactsUpdateMap> updateList) {
        HwLog.d(TAG, "onContactsChange: Starts");
        updateContacts(updateList);
    }

    private List<ContactsUpdateMap> loadLocalContacts() {
        Map<String, String> mapContacts;
        List<ContactsUpdateMap> contactsList = new ArrayList();
        List<ContactInfo> contactsInBlackList = DBAdapter.getContactInfoFromBlackList(this.mContext);
        if (!Utility.isNullOrEmptyList(contactsInBlackList)) {
            HwLog.d(TAG, "loadLocalContacts: contactsInBlackList = " + contactsInBlackList.size());
            mapContacts = new HashMap();
            for (ContactInfo contactInfo : contactsInBlackList) {
                mapContacts.put(contactInfo.getPhone(), contactInfo.getName());
            }
            contactsList.add(new ContactsUpdateMap(0, mapContacts));
        }
        List<ContactInfo> contactsInCall = DBAdapter.getContactInfoFromInterceptedCalls(this.mContext);
        if (!Utility.isNullOrEmptyList(contactsInCall)) {
            HwLog.d(TAG, "loadLocalContacts: contactsInCall = " + contactsInCall.size());
            mapContacts = new HashMap();
            for (ContactInfo contactInfo2 : contactsInCall) {
                mapContacts.put(contactInfo2.getPhone(), contactInfo2.getName());
            }
            contactsList.add(new ContactsUpdateMap(1, mapContacts));
        }
        List<ContactInfo> contactsInMsg = DBAdapter.getContactInfoFromInterceptedMsgs(this.mContext);
        if (!Utility.isNullOrEmptyList(contactsInMsg)) {
            HwLog.d(TAG, "loadLocalContacts: contactsInMsg = " + contactsInMsg.size());
            mapContacts = new HashMap();
            for (ContactInfo contactInfo22 : contactsInMsg) {
                mapContacts.put(contactInfo22.getPhone(), contactInfo22.getName());
            }
            contactsList.add(new ContactsUpdateMap(2, mapContacts));
        }
        List<WhitelistInfo> contactsInWhiteList = DBAdapter.getWhitelist(this.mContext);
        if (!Utility.isNullOrEmptyList(contactsInWhiteList)) {
            HwLog.d(TAG, "loadLocalContacts: contactsInWhiteList = " + contactsInWhiteList.size());
            mapContacts = new HashMap();
            for (WhitelistInfo contactInfo3 : contactsInWhiteList) {
                mapContacts.put(contactInfo3.getPhone(), contactInfo3.getName());
            }
            contactsList.add(new ContactsUpdateMap(3, mapContacts));
        }
        if (contactsList.isEmpty()) {
            HwLog.d(TAG, "loadLocalContacts: No contacts");
        }
        return contactsList;
    }

    private void updateContactsInBlacklist(Map<String, String> contactsMap) {
        List<ContactInfo> updateList = getUpdateContactsList(contactsMap);
        if (updateList.isEmpty()) {
            HwLog.d(TAG, "updateContactsInBlacklist: No contacts needs update");
            return;
        }
        HwLog.d(TAG, "updateContactsInBlacklist = " + DBAdapter.updateContactInfoInBlackList(this.mContext, updateList));
    }

    private void updateContactsInCall(Map<String, String> contactsMap) {
        List<ContactInfo> updateList = getUpdateContactsList(contactsMap);
        if (updateList.isEmpty()) {
            HwLog.d(TAG, "updateContactsInCall: No contacts needs update");
            return;
        }
        HwLog.d(TAG, "updateContactsInCall = " + DBAdapter.updateContactInfoInInterceptedCalls(this.mContext, updateList));
    }

    private void updateContactsInMsg(Map<String, String> contactsMap) {
        List<ContactInfo> updateList = getUpdateContactsList(contactsMap);
        if (updateList.isEmpty()) {
            HwLog.d(TAG, "updateContactsInMsg: No contacts needs update");
            return;
        }
        HwLog.d(TAG, "updateContactsInMsg = " + DBAdapter.updateContactInfoInInterceptedMsgs(this.mContext, updateList));
    }

    private void updateContactsInWhitelist(Map<String, String> contactsMap) {
        List<ContactInfo> updateList = getUpdateContactsList(contactsMap);
        if (updateList.isEmpty()) {
            HwLog.d(TAG, "updateContactsInWhitelist: No contacts needs update");
            return;
        }
        HwLog.d(TAG, "updateContactsInWhitelist = " + DBAdapter.updateContactInfoInWhitelist(this.mContext, updateList));
    }

    private List<ContactInfo> getUpdateContactsList(Map<String, String> contactsMap) {
        List<ContactInfo> updateList = new ArrayList();
        if (contactsMap.isEmpty()) {
            return updateList;
        }
        for (Entry<String, String> entry : contactsMap.entrySet()) {
            updateList.add(new ContactInfo((String) entry.getKey(), (String) entry.getValue()));
        }
        return updateList;
    }

    private void updateContacts(List<ContactsUpdateMap> updateList) {
        for (ContactsUpdateMap updateItem : updateList) {
            int nTag = updateItem.getTag();
            switch (nTag) {
                case 0:
                    updateContactsInBlacklist(updateItem.getContatcsMap());
                    break;
                case 1:
                    updateContactsInCall(updateItem.getContatcsMap());
                    break;
                case 2:
                    updateContactsInMsg(updateItem.getContatcsMap());
                    break;
                case 3:
                    updateContactsInWhitelist(updateItem.getContatcsMap());
                    break;
                default:
                    HwLog.w(TAG, "updateContacts: Invalid contacts tag " + nTag);
                    break;
            }
        }
    }
}
