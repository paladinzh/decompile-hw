package com.huawei.systemmanager.util.contacts;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.contacts.ContactsObject.SysContactsObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ContactsHelper {
    public static final String KEY_CALLLOG_ID_LIST = "SelItemCalls_KeyValue";
    public static final String KEY_CONTACTS_ID_LIST = "SelItemData_KeyValue";
    private static final String TAG = "ContactsHelper";

    public static List<SysContactsObject> getSelectedContacts(Intent data, Context context, AtomicBoolean abortFlag) {
        List<SysContactsObject> selectedList = new ArrayList();
        ArrayList<Parcelable> contactsIdList = data.getParcelableArrayListExtra(KEY_CONTACTS_ID_LIST);
        if (Utility.isNullOrEmptyList(contactsIdList)) {
            HwLog.d(TAG, "getSelectedContacts: no contact is selected");
        } else {
            List<SysContactsObject> contactList = parseContactList(contactsIdList, context, abortFlag);
            if (!Utility.isNullOrEmptyList(contactList)) {
                selectedList.addAll(contactList);
                HwLog.d(TAG, "getSelectedContacts: Selected contacts count = " + contactList.size());
            }
        }
        ArrayList<Parcelable> callIdList = data.getParcelableArrayListExtra(KEY_CALLLOG_ID_LIST);
        if (Utility.isNullOrEmptyList(callIdList)) {
            HwLog.d(TAG, "getSelectedContacts: no call log is selected");
        } else {
            List<SysContactsObject> callList = parseCallLogList(callIdList, context, abortFlag);
            if (!Utility.isNullOrEmptyList(callList)) {
                selectedList.addAll(callList);
                HwLog.d(TAG, "getSelectedContacts: Selected call log count = " + callList.size());
            }
        }
        return selectedList;
    }

    public static List<SysContactsObject> parseContactList(ArrayList<Parcelable> parcelableList, Context context, AtomicBoolean abortFlag) {
        ArrayList<Integer> contactIdList = parseContactIdList(parcelableList);
        if (Utility.isNullOrEmptyList(contactIdList)) {
            HwLog.w(TAG, "parseContactList: get empty contact list");
            return null;
        }
        HashMap<String, SysContactsObject> contactsMap = getAllSysContacts(context);
        if (contactsMap == null || contactsMap.size() <= 0) {
            HwLog.i(TAG, "parseContactList: Fail to read contacts data");
            return null;
        }
        List<SysContactsObject> listContacts = new ArrayList();
        int contactCount = contactIdList.size();
        int i = 0;
        while (!abortFlag.get() && i < contactCount) {
            Integer id = (Integer) contactIdList.get(i);
            SysContactsObject contactsObj = (SysContactsObject) contactsMap.get(String.valueOf(id));
            if (contactsObj == null) {
                HwLog.w(TAG, "parseContactList: Fail to get contact by id, " + id);
            } else {
                listContacts.add(contactsObj);
            }
            i++;
        }
        return listContacts;
    }

    public static List<SysContactsObject> parseCallLogList(ArrayList<Parcelable> parcelableList, Context context, AtomicBoolean abortFlag) {
        ArrayList<Integer> callIdList = parseCallLogIdList(parcelableList);
        if (Utility.isNullOrEmptyList(callIdList)) {
            HwLog.w(TAG, "parseCallLogList: get empty call log list");
            return null;
        }
        HashMap<String, SysContactsObject> callMap = getCallLogs(context);
        if (callMap == null || callMap.size() <= 0) {
            HwLog.i(TAG, "parseCallLogList: Fail to read calllog list");
            return null;
        }
        List<SysContactsObject> listCalls = new ArrayList();
        int contactCount = callIdList.size();
        int i = 0;
        while (!abortFlag.get() && i < contactCount) {
            Integer id = (Integer) callIdList.get(i);
            SysContactsObject contactsObj = (SysContactsObject) callMap.get(String.valueOf(id));
            if (contactsObj == null) {
                HwLog.w(TAG, "parseCallLogList: Fail to get call by id, " + id);
            } else {
                listCalls.add(contactsObj);
            }
            i++;
        }
        return listCalls;
    }

    public static ArrayList<Integer> parseContactIdList(ArrayList<Parcelable> parcelableList) {
        if (Utility.isNullOrEmptyList(parcelableList)) {
            HwLog.w(TAG, "parseContactIdList: Invalid or empty list");
            return null;
        }
        Object obj = parcelableList.get(0);
        if (obj instanceof Integer) {
            return parseIntegerList(parcelableList);
        }
        if (obj instanceof Uri) {
            ArrayList<Integer> idList = new ArrayList();
            for (int i = 0; i < parcelableList.size(); i++) {
                String[] ss = ((Parcelable) parcelableList.get(i)).toString().split("/");
                idList.add(Integer.valueOf(Integer.parseInt(ss[ss.length - 1])));
            }
            return idList;
        }
        HwLog.w(TAG, "parseContactIdList: Invalid contacts list .Type = " + obj.getClass().toString());
        return null;
    }

    public static ArrayList<Integer> parseCallLogIdList(ArrayList<Parcelable> parcelableList) {
        if (Utility.isNullOrEmptyList(parcelableList)) {
            HwLog.w(TAG, "parseCallLogIdList: Invalid or empty list");
            return null;
        } else if (parcelableList.get(0) instanceof Integer) {
            return parseIntegerList(parcelableList);
        } else {
            HwLog.w(TAG, "parseCallLogIdList: Invalid list type");
            return null;
        }
    }

    private static ArrayList<Integer> parseIntegerList(ArrayList<Parcelable> parcelableList) {
        ArrayList<Integer> idList = new ArrayList();
        for (int i = 0; i < parcelableList.size(); i++) {
            idList.add((Integer) parcelableList.get(i));
        }
        return idList;
    }

    public static HashMap<String, SysContactsObject> getAllSysContacts(Context context) {
        Cursor contactCursor = context.getContentResolver().query(Phone.CONTENT_URI, new String[]{"_id", "data1", "display_name"}, null, null, null);
        if (Utility.isNullOrEmptyCursor(contactCursor, true)) {
            HwLog.w(TAG, "getAllSysContacts: Fail to get contacts");
            return null;
        }
        HwLog.d(TAG, "getAllSysContacts: Contacts count = " + contactCursor.getCount());
        HashMap<String, SysContactsObject> contactsMap = new HashMap();
        if (contactCursor.moveToNext()) {
            int nColIndexId = contactCursor.getColumnIndex("_id");
            int nColIndexName = contactCursor.getColumnIndex("display_name");
            int nColIndexNumber = contactCursor.getColumnIndex("data1");
            do {
                contactsMap.put(contactCursor.getString(nColIndexId), new SysContactsObject(contactCursor.getString(nColIndexName), contactCursor.getString(nColIndexNumber)));
            } while (contactCursor.moveToNext());
        }
        contactCursor.close();
        return contactsMap;
    }

    public static HashMap<String, SysContactsObject> getCallLogs(Context context) {
        Cursor callCursor = context.getContentResolver().query(Calls.CONTENT_URI, new String[]{"_id", "number", "name"}, null, null, null);
        if (Utility.isNullOrEmptyCursor(callCursor, true)) {
            HwLog.w(TAG, "getCallLogs: Fail to get call logs");
            return null;
        }
        HwLog.d(TAG, "getCallLogs: count = " + callCursor.getCount());
        HashMap<String, SysContactsObject> callsMap = new HashMap();
        if (callCursor.moveToNext()) {
            int nColIndexId = callCursor.getColumnIndex("_id");
            int nColIndexName = callCursor.getColumnIndex("name");
            int nColIndexNumber = callCursor.getColumnIndex("number");
            do {
                callsMap.put(callCursor.getString(nColIndexId), new SysContactsObject(callCursor.getString(nColIndexName), callCursor.getString(nColIndexNumber)));
            } while (callCursor.moveToNext());
        }
        callCursor.close();
        return callsMap;
    }
}
