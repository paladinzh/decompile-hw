package com.huawei.systemmanager.preventmode;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import com.huawei.systemmanager.optimize.base.Const;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.contacts.ContactsObject.ContactsUpdateMap;
import com.huawei.systemmanager.util.contacts.IContactsObserver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PreventModeContactObserver implements IContactsObserver {
    private static final String TAG = PreventModeContactObserver.class.getName();
    private static final int TAG_WHITELIST_CONTACTS = 0;
    private static final int UPDATE_ITEM_SIZE = 1;
    private Context mContext = null;
    private PreventConfig mPreventConfig = null;

    public PreventModeContactObserver(Context context) {
        this.mContext = context.getApplicationContext();
        this.mPreventConfig = new PreventConfig(this.mContext);
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
        List<ContactsUpdateMap> contactsList = new ArrayList();
        Cursor cursor = this.mPreventConfig.queryPreventWhiteListDB();
        if (cursor != null) {
            int numCol = cursor.getColumnIndex(Const.PREVENT_WHITE_LIST_NUMBER);
            int nameCol = cursor.getColumnIndex(Const.PREVENT_WHITE_LIST_NAME);
            Map<String, String> mapContacts = new HashMap();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                mapContacts.put(cursor.getString(numCol), cursor.getString(nameCol));
                cursor.moveToNext();
            }
            cursor.close();
            contactsList.add(new ContactsUpdateMap(0, mapContacts));
        }
        return contactsList;
    }

    private void updateContacts(List<ContactsUpdateMap> updateList) {
        if (1 == updateList.size()) {
            ArrayList<ContactInfo> contactsList = getUpdateContactsList(((ContactsUpdateMap) updateList.get(0)).getContatcsMap());
            if (contactsList.size() != 0 && updateWhiteList(contactsList) > 0) {
                sendUpdateNotification(contactsList);
            }
        }
    }

    private int updateWhiteList(ArrayList<ContactInfo> contactsList) {
        int nUpdateCount = this.mPreventConfig.updateWhiteList(contactsList);
        HwLog.d(TAG, "updateWhiteList = " + nUpdateCount);
        return nUpdateCount;
    }

    private ArrayList<ContactInfo> getUpdateContactsList(Map<String, String> contactsMap) {
        ArrayList<ContactInfo> updateList = new ArrayList();
        if (contactsMap.isEmpty()) {
            return updateList;
        }
        for (Entry<String, String> entry : contactsMap.entrySet()) {
            updateList.add(new ContactInfo((String) entry.getValue(), (String) entry.getKey()));
        }
        return updateList;
    }

    private void sendUpdateNotification(ArrayList<ContactInfo> contactsList) {
        Intent intent = new Intent();
        intent.setAction(PreventConst.ACTION_PREVENT_MODE_UPDATE_WHITELIST);
        intent.putExtra(PreventConst.PREVENT_UPDATE_WHITELIST_KEY, contactsList);
        this.mContext.sendBroadcast(intent, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
        HwLog.e(TAG, "sendUpdateNotification");
    }
}
