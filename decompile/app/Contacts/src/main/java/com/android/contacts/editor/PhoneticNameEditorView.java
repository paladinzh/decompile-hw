package com.android.contacts.editor;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.ValuesDelta;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.model.dataitem.StructuredNameDataItem;
import com.android.contacts.util.HwLog;
import java.util.ArrayList;
import java.util.Locale;

public class PhoneticNameEditorView extends TextFieldsEditorView {
    private ArrayList<ValuesDelta> mEntryList = new ArrayList();

    private static class PhoneticValuesDelta extends ValuesDelta {
        private String mPhoneticName;
        private ValuesDelta mValues;

        public PhoneticValuesDelta(ValuesDelta values) {
            this.mValues = values;
            buildPhoneticName();
        }

        public void put(String key, String value) {
            if (key.equals("#phoneticName")) {
                this.mPhoneticName = value;
                parsePhoneticName(value);
                return;
            }
            this.mValues.put(key, value);
            buildPhoneticName();
        }

        public String getAsString(String key) {
            if ("#phoneticName".equals(key)) {
                return this.mPhoneticName;
            }
            return this.mValues.getAsString(key);
        }

        private void parsePhoneticName(String value) {
            StructuredNameDataItem dataItem = PhoneticNameEditorView.parsePhoneticName(value, null);
            this.mValues.setPhoneticFamilyName(dataItem.getPhoneticFamilyName());
            this.mValues.setPhoneticMiddleName(dataItem.getPhoneticMiddleName());
            this.mValues.setPhoneticGivenName(dataItem.getPhoneticGivenName());
        }

        private void buildPhoneticName() {
            String family = this.mValues.getPhoneticFamilyName();
            String middle = this.mValues.getPhoneticMiddleName();
            String given = this.mValues.getPhoneticGivenName();
            if (Locale.getDefault().getCountry().equalsIgnoreCase("ru")) {
                this.mPhoneticName = PhoneticNameEditorView.buildPhoneticName(family, given, middle);
            } else {
                this.mPhoneticName = PhoneticNameEditorView.buildPhoneticName(family, middle, given);
            }
        }

        public Long getId() {
            return this.mValues.getId();
        }

        public boolean isVisible() {
            return this.mValues.isVisible();
        }

        public boolean equals(Object object) {
            return super.equals(object);
        }

        public int hashCode() {
            return super.hashCode();
        }
    }

    public static StructuredNameDataItem parsePhoneticName(String phoneticName, StructuredNameDataItem item) {
        String family = null;
        String middle = null;
        String given = null;
        if (!TextUtils.isEmpty(phoneticName)) {
            String[] strings = phoneticName.split(HwCustPreloadContacts.EMPTY_STRING, 3);
            switch (strings.length) {
                case 1:
                    family = strings[0];
                    break;
                case 2:
                    family = strings[0];
                    given = strings[1];
                    break;
                case 3:
                    family = strings[0];
                    middle = strings[1];
                    given = strings[2];
                    break;
                default:
                    HwLog.w("PhoneticNameEditorView", "Invalid String array length: " + strings.length);
                    break;
            }
        }
        if (item == null) {
            item = new StructuredNameDataItem();
        }
        item.setPhoneticFamilyName(family);
        item.setPhoneticMiddleName(middle);
        item.setPhoneticGivenName(given);
        return item;
    }

    public static String buildPhoneticName(String family, String middle, String given) {
        if (TextUtils.isEmpty(family) && TextUtils.isEmpty(middle) && TextUtils.isEmpty(given)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(family)) {
            sb.append(family.trim()).append(' ');
        }
        if (!TextUtils.isEmpty(middle)) {
            sb.append(middle.trim()).append(' ');
        }
        if (!TextUtils.isEmpty(given)) {
            sb.append(given.trim()).append(' ');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public static boolean isUnstructuredPhoneticNameColumn(String column) {
        return "#phoneticName".equals(column);
    }

    public PhoneticNameEditorView(Context context) {
        super(context);
    }

    public PhoneticNameEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhoneticNameEditorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setValues(DataKind kind, ValuesDelta entry, RawContactDelta state, boolean readOnly, ViewIdGenerator vig) {
        if (!(entry instanceof PhoneticValuesDelta)) {
            entry = new PhoneticValuesDelta(entry);
        }
        super.setValues(kind, entry, state, readOnly, vig);
    }

    public void onFieldChanged(String column, String value, View view) {
        if (isFieldChanged(column, value)) {
            if (hasShortAndLongForms()) {
                if ((!areOptionalFieldsVisible()) == isUnstructuredPhoneticNameColumn(column)) {
                    super.onFieldChanged(column, value, view);
                }
            } else {
                super.onFieldChanged(column, value, view);
            }
        }
    }

    public boolean hasData() {
        ValuesDelta entry = getEntry();
        String family = entry.getPhoneticFamilyName();
        String middle = entry.getPhoneticMiddleName();
        String given = entry.getPhoneticGivenName();
        if (TextUtils.isEmpty(family) && TextUtils.isEmpty(middle) && TextUtils.isEmpty(given)) {
            return false;
        }
        return true;
    }

    public ArrayList<ValuesDelta> getValuesList() {
        return this.mEntryList;
    }

    public void addEntry(ValuesDelta object) {
        super.addEntry(object);
        if (!(object instanceof PhoneticValuesDelta)) {
            object = new PhoneticValuesDelta(object);
        }
        this.mEntryList.add(object);
    }
}
