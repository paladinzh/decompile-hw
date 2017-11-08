package com.huawei.systemmanager.util.contacts;

import com.huawei.systemmanager.util.contacts.ContactsObject.ContactsUpdateMap;
import java.util.List;

public interface IContactsObserver {

    public static class DftObserver implements IContactsObserver {
        public List<ContactsUpdateMap> onPrepareContactsChange() {
            return null;
        }

        public void onContactsChange(List<ContactsUpdateMap> list) {
        }
    }

    void onContactsChange(List<ContactsUpdateMap> list);

    List<ContactsUpdateMap> onPrepareContactsChange();
}
