package com.huawei.systemmanager.util.phonematch;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;

public class PhoneMatchInfo {
    private boolean mIsExactMatch;
    private String mPhoneNumber;

    public PhoneMatchInfo(String phone, boolean isExactMatch) {
        this.mPhoneNumber = phone;
        this.mIsExactMatch = isExactMatch;
    }

    public void setPhoneNumber(String phone) {
        this.mPhoneNumber = phone;
    }

    public String getPhoneNumber() {
        return this.mPhoneNumber;
    }

    public void setIsExactMatch(boolean isExactMatch) {
        this.mIsExactMatch = isExactMatch;
    }

    public boolean isExactMatch() {
        return this.mIsExactMatch;
    }

    public boolean isValid() {
        return !TextUtils.isEmpty(this.mPhoneNumber);
    }

    public String getSqlSelectionStatement(String phoneColName) {
        return getSqlSelectionStatement(phoneColName, (String[]) null);
    }

    public String getSqlSelectionStatement(String phoneColName, String... otherColumns) {
        StringBuilder selection = new StringBuilder();
        if (TextUtils.isEmpty(phoneColName)) {
            return selection.toString();
        }
        selection.append(phoneColName);
        if (isExactMatch()) {
            selection.append(" = ?");
        } else {
            selection.append(" like ?");
        }
        if (otherColumns == null || otherColumns.length <= 0) {
            return selection.toString();
        }
        for (String column : otherColumns) {
            if (!TextUtils.isEmpty(column)) {
                selection.append(" AND ");
                selection.append(column);
                selection.append(" = ?");
            }
        }
        return selection.toString();
    }

    public String[] getSqlSelectionArgs() {
        if (isExactMatch()) {
            return new String[]{getPhoneNumber()};
        }
        return new String[]{"%" + getPhoneNumber()};
    }

    public String[] getSqlSelectionArgs(String... otherValues) {
        List<String> selectionArgs = new ArrayList();
        String phoneNumberValue = "";
        if (isExactMatch()) {
            phoneNumberValue = getPhoneNumber();
        } else {
            phoneNumberValue = "%" + getPhoneNumber();
        }
        selectionArgs.add(phoneNumberValue);
        for (String value : otherValues) {
            if (!TextUtils.isEmpty(value)) {
                selectionArgs.add(value);
            }
        }
        return (String[]) selectionArgs.toArray(new String[selectionArgs.size()]);
    }
}
