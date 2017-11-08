package com.android.mms.ui;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.SparseIntArray;

public class HwCustMessageUtils {
    public SparseIntArray getDefault7bitsTableVenezuela(SparseIntArray charToGsmGreekSingle) {
        return charToGsmGreekSingle;
    }

    public boolean isReplaceCharUsingDefault7bitsTableVenezuela() {
        return false;
    }

    public boolean configRoamingNationalAsLocal() {
        return false;
    }

    public boolean isRoamingNationalP4(int subscription) {
        return false;
    }

    public String addMsgType(Context context, Cursor cursor) {
        return null;
    }

    public CharSequence formatMessage(String body) {
        return "";
    }

    public void selectFile(Context context, int requestCode) {
    }

    public void markOtherAsRead(ContentResolver cr, ContentValues values) {
    }

    public boolean isRcsSwitchOn() {
        return false;
    }

    public MessageItem getMsgItem(MessageListAdapter msgAdapter, Long itemId) {
        return null;
    }

    public Intent selectMediaByType(Intent innerIntent, String contentType) {
        return innerIntent;
    }

    public boolean isPoundChar(StringBuilder builder, char c) {
        return false;
    }

    public String getContactName(String addr) {
        return addr;
    }

    public boolean isAlwaysShowSmsOptimization(String mccmnc) {
        return false;
    }
}
