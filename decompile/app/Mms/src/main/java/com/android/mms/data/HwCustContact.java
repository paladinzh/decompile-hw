package com.android.mms.data;

public class HwCustContact {
    public boolean isGroupID(String groupID) {
        return false;
    }

    public boolean isNotRegularPhoneNumber(boolean orignalValue, String number) {
        return orignalValue;
    }

    public String getContactName(Contact orginalContact, String name, String number) {
        return null;
    }

    public boolean needResetContactName(Contact orginalContact, String name, String number) {
        return false;
    }

    public void clearGroupNameCache() {
    }

    public boolean isPoundCharValid() {
        return false;
    }

    public String setMatchNumber(String normalizedNumber, String number) {
        return normalizedNumber;
    }
}
