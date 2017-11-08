package com.huawei.systemmanager.util.contacts;

import java.util.Map;

public class ContactsObject {

    public static class ContactsUpdateMap {
        private Map<String, String> mContactsMap = null;
        private int mTag = 0;

        public ContactsUpdateMap(int tag, Map<String, String> contactsMap) {
            this.mTag = tag;
            this.mContactsMap = contactsMap;
        }

        public int getTag() {
            return this.mTag;
        }

        public void setTag(int tag) {
            this.mTag = tag;
        }

        public Map<String, String> getContatcsMap() {
            return this.mContactsMap;
        }

        public void setContactsMap(Map<String, String> contactsMap) {
            this.mContactsMap = contactsMap;
        }
    }

    public static class SysContactsObject {
        private String mName;
        private String mNumber;

        public SysContactsObject(String name, String number) {
            this.mName = name;
            this.mNumber = number;
        }

        public String getName() {
            return this.mName;
        }

        public String getNumber() {
            return this.mNumber;
        }
    }
}
