package com.huawei.systemmanager.preventmode;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import com.huawei.systemmanager.comparator.AlpComparator;
import com.huawei.systemmanager.optimize.base.Const;
import com.huawei.systemmanager.preventmode.util.Utility;
import com.huawei.systemmanager.util.numberlocation.NumberLocationInfo;
import com.huawei.systemmanager.util.phonematch.PhoneMatch;

public class WhiteNameInfo {
    public static final AlpComparator<WhiteNameInfo> PREVENT_ALP_COMPARATOR = new AlpComparator<WhiteNameInfo>() {
        public String getStringKey(WhiteNameInfo t) {
            String name = t.mName;
            String num = t.mNumber;
            if (TextUtils.isEmpty(name)) {
                return num;
            }
            return t.mName;
        }
    };
    public boolean isChecked;
    public long mId;
    public NumberLocationInfo mLocation;
    public String mName;
    public String mNumber;
    public String mPhoneNumId;
    public String mShortNumer;

    public WhiteNameInfo(long id, String name, String number, NumberLocationInfo location, String phoneNumId, String shortNumer) {
        this.mId = id;
        this.mName = name;
        this.mNumber = number;
        this.mLocation = location;
        this.mPhoneNumId = phoneNumId;
        this.mShortNumer = shortNumer;
        this.isChecked = false;
    }

    public String getContactInfo(Context context) {
        String strContactInfo = "";
        if (!TextUtils.isEmpty(this.mName)) {
            strContactInfo = strContactInfo + this.mName + " ";
        }
        if (TextUtils.isEmpty(this.mNumber)) {
            return strContactInfo;
        }
        return strContactInfo + "‭" + this.mNumber + "‬";
    }

    public String getLocationInfoString() {
        if (this.mLocation == null) {
            return "";
        }
        return this.mLocation.getGeoLocation();
    }

    public void setLocationInfo(NumberLocationInfo location) {
        this.mLocation = location;
    }

    public boolean updateIfSameContacts(ContactInfo contact) {
        if (!this.mNumber.equals(contact.getmPhone())) {
            return false;
        }
        this.mName = contact.getmName();
        return true;
    }

    public void parseFrom(Cursor cursor) {
        this.mId = cursor.getLong(cursor.getColumnIndex("_id"));
        this.mPhoneNumId = cursor.getString(cursor.getColumnIndex(Const.PREVENT_WHITE_LIST_ID));
        this.mNumber = cursor.getString(cursor.getColumnIndex(Const.PREVENT_WHITE_LIST_NUMBER));
        this.mName = cursor.getString(cursor.getColumnIndex(Const.PREVENT_WHITE_LIST_NAME));
        this.mLocation = new NumberLocationInfo(cursor.getString(cursor.getColumnIndex("location")), cursor.getString(cursor.getColumnIndex(Const.PREVENT_WHITE_LIST_MOBILE)));
        this.isChecked = false;
    }

    public ContentValues getAsContentValue() {
        ContentValues values = new ContentValues();
        values.put(Const.PREVENT_WHITE_LIST_NUMBER, this.mNumber);
        values.put(Const.PREVENT_WHITE_LIST_SHORTNUMBER, PhoneMatch.getPhoneNumberMatchInfo(Utility.formatPhoneNumber(this.mShortNumer)).getPhoneNumber());
        values.put(Const.PREVENT_WHITE_LIST_NAME, this.mName);
        values.put(Const.PREVENT_WHITE_LIST_ID, this.mPhoneNumId);
        values.put("location", this.mLocation.getLocation());
        values.put(Const.PREVENT_WHITE_LIST_MOBILE, this.mLocation.getOperator());
        return values;
    }
}
