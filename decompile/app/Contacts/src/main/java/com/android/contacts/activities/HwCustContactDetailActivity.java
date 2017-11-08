package com.android.contacts.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import com.android.contacts.model.Contact;

public class HwCustContactDetailActivity {
    public HwCustContactDetailActivity(Context mContext) {
    }

    public boolean isCustHideGeoInfo() {
        return false;
    }

    public String getPersonToSendFile() {
        return null;
    }

    public void setPersonToSendFile(String personToSendFile) {
    }

    public boolean isFromRcsGroupChat(Intent intent) {
        return false;
    }

    public void initFromRcsGroupChat(Intent intent) {
    }

    public String getRcsGroupChatAddress(Intent intent) {
        return "";
    }

    public String getRcsGroupChatNickname(Intent intent) {
        return "";
    }

    public void custOnCreate() {
    }

    public boolean joinContactsRequired() {
        return false;
    }

    public void joinAggregate(Intent intent) {
    }

    public boolean supportReadOnly() {
        return false;
    }

    public boolean isReadOnlyContact(Uri contactUri) {
        return false;
    }

    public void checkAndAddIntentExtra(Intent aIntent, Contact aContactData, Context aContext) {
    }

    public void addCustomIntentExtrasForCnap(Intent receivedIntent, Intent targetIntent) {
    }

    public String getCnapNameExtraFromIntent(Intent receivedIntent, String defaultName) {
        return defaultName;
    }

    public void putNameExtraToIntent(Intent aIntent, Cursor cursor) {
    }
}
