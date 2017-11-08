package com.huawei.mms.util;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.CallerInfoHW;
import android.util.Log;
import cn.com.xy.sms.sdk.HarassNumberUtil;
import com.android.mms.data.Contact;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.mms.util.NumberUtils.AddrMatcher;

public class HwNumberMatchUtils {

    private static class ArrayCursor implements Cursor {
        private int mMaxSize;
        private final String[] mNumbers;
        private int mPosition = -1;

        public ArrayCursor(String[] data) {
            this.mNumbers = data;
            this.mMaxSize = data.length;
            while (this.mMaxSize > 0 && data[this.mMaxSize - 1] == null) {
                this.mMaxSize--;
                Log.w("Mms::HwNumberMatchUtils", "ArrayCursor has empty data " + this.mMaxSize);
            }
        }

        public int getColumnIndex(String columnName) {
            if ("data1".equalsIgnoreCase(columnName) || HarassNumberUtil.NUMBER.equalsIgnoreCase(columnName)) {
                return 0;
            }
            return -1;
        }

        public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
            int idx = getColumnIndex(columnName);
            if (idx >= 0) {
                return idx;
            }
            throw new IllegalArgumentException("HwNumberUtils-ArrayCurosr, getColumnIndexOrThrow for " + columnName);
        }

        public String getColumnName(int columnIndex) {
            return "data1";
        }

        public String[] getColumnNames() {
            return new String[]{"data1"};
        }

        public int getCount() {
            return this.mMaxSize;
        }

        public int getPosition() {
            return this.mPosition;
        }

        public boolean move(int offset) {
            return moveToPosition(this.mPosition + offset);
        }

        public boolean moveToFirst() {
            return moveToPosition(0);
        }

        public boolean moveToLast() {
            if (this.mNumbers == null || this.mMaxSize <= 0) {
                return false;
            }
            return moveToPosition(this.mNumbers.length - 1);
        }

        public boolean moveToNext() {
            return move(1);
        }

        public boolean moveToPosition(int position) {
            if (position < -1 || position >= this.mMaxSize) {
                return false;
            }
            this.mPosition = position;
            return true;
        }

        public boolean moveToPrevious() {
            return move(-1);
        }

        public String getString(int columnIndex) {
            if (this.mNumbers == null || columnIndex != 0) {
                return null;
            }
            return this.mNumbers[this.mPosition];
        }

        public boolean isAfterLast() {
            return this.mPosition == getCount();
        }

        public boolean isBeforeFirst() {
            return -1 == this.mPosition;
        }

        public boolean isFirst() {
            return this.mPosition == 0;
        }

        public boolean isLast() {
            return getCount() + -1 == this.mPosition;
        }

        public void setExtras(Bundle extras) {
        }

        public double getDouble(int columnIndex) {
            return 0.0d;
        }

        public Bundle getExtras() {
            return null;
        }

        public float getFloat(int columnIndex) {
            return 0.0f;
        }

        public int getInt(int columnIndex) {
            return 0;
        }

        public long getLong(int columnIndex) {
            return 0;
        }

        public short getShort(int columnIndex) {
            return (short) 0;
        }

        public int getType(int columnIndex) {
            return 0;
        }

        public boolean getWantsAllOnMoveCalls() {
            return false;
        }

        public boolean isNull(int columnIndex) {
            return false;
        }

        public void registerContentObserver(ContentObserver observer) {
        }

        public void registerDataSetObserver(DataSetObserver observer) {
        }

        @Deprecated
        public boolean requery() {
            return false;
        }

        public Bundle respond(Bundle bundle) {
            return null;
        }

        public Uri getNotificationUri() {
            return null;
        }

        public void setNotificationUri(ContentResolver resolver, Uri uri) {
        }

        public void unregisterContentObserver(ContentObserver resolver) {
        }

        public void unregisterDataSetObserver(DataSetObserver resolver) {
        }

        @Deprecated
        public void deactivate() {
        }

        public void close() {
        }

        public boolean isClosed() {
            return false;
        }

        public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
        }

        public byte[] getBlob(int columnIndex) {
            return new byte[0];
        }

        public int getColumnCount() {
            return 1;
        }
    }

    public static int getMatchedIndex(Cursor cursor, String compNum, String columnName) {
        try {
            return CallerInfoHW.getInstance().getCallerIndex(cursor, compNum, columnName);
        } catch (NoExtAPIException e) {
            Log.e("Mms::HwNumberMatchUtils", "getMatchedIndex NoExtAPIException: " + e.getMessage() + ", use mms application's number matched method isNumberMatch!");
            return -2;
        } catch (Exception e2) {
            Log.e("Mms::HwNumberMatchUtils", "getMatchedIndex exception: " + e2.getMessage());
            return -1;
        }
    }

    public static int getMatchedIndex(Cursor cursor, String compNum) {
        return getMatchedIndex(cursor, compNum, "data1");
    }

    public static Cursor getMatchedCursor(Cursor cursor, String compNum) {
        int index = getMatchedIndex(cursor, compNum);
        if (index >= 0) {
            if (cursor.moveToPosition(index)) {
                return cursor;
            }
        } else if (-2 == index) {
            return AddrMatcher.getPriorityMatchCursor(cursor, compNum, "address");
        }
        return null;
    }

    public static Contact getMatchedContact(Contact[] list, String compNum) {
        if (list == null || list.length == 0) {
            return null;
        }
        int index = getMatchedIndex(new ArrayCursor(getNumberList(list)), compNum);
        if (index >= 0) {
            return list[index];
        }
        if (-2 == index) {
            return AddrMatcher.getPriorityMatchContact(list, compNum);
        }
        return null;
    }

    public static String[] getNumberList(Contact[] list) {
        int size = list.length;
        String[] numbers = new String[size];
        int i = 0;
        int k = 0;
        while (i < size) {
            Contact c = list[i];
            if (list[i] == null) {
                break;
            }
            int k2;
            String number = c.getNumber();
            if (number == null || Contact.isEmailAddress(number)) {
                k2 = k + 1;
                numbers[k] = "";
                Log.e("Mms::HwNumberMatchUtils", "Notice this will cause index order change!!!");
            } else {
                k2 = k + 1;
                numbers[k] = number;
            }
            i++;
            k = k2;
        }
        while (k < size) {
            k2 = k + 1;
            numbers[k] = null;
            k = k2 + 1;
        }
        return numbers;
    }

    public static boolean isNumbersMatched(String number, String compNum) {
        boolean z = true;
        if (number == null || compNum == null) {
            return false;
        }
        int index = getMatchedIndex(new ArrayCursor(new String[]{number}), compNum);
        if (index >= 0) {
            return true;
        }
        if (-2 != index) {
            return false;
        }
        if (AddrMatcher.isNumberMatch(number, compNum) <= 0) {
            z = false;
        }
        return z;
    }
}
