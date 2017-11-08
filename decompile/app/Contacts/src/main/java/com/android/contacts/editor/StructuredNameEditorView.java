package com.android.contacts.editor;

import android.content.ContentValues;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.ValuesDelta;
import com.android.contacts.model.dataitem.DataItem;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.model.dataitem.StructuredNameDataItem;
import com.android.contacts.util.ContactsThreadPool;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.MultiReadingUtils;
import com.android.contacts.util.NameConverter;
import com.huawei.cust.HwCustUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public class StructuredNameEditorView extends TextFieldsEditorView {
    private boolean mChanged;
    private HwCustStructuredNameEditorView mCust;
    private StructuredNameDataItem mSnapshot;

    private class BuildName implements Runnable {
        private BuildName() {
        }

        public void run() {
            if (HwLog.HWDBG) {
                HwLog.d("View", "execute BuildName runnable");
            }
            if (StructuredNameEditorView.this.hasShortAndLongForms()) {
                ArrayList<ValuesDelta> valuesList = StructuredNameEditorView.this.getValuesList();
                if (StructuredNameEditorView.this.areOptionalFieldsVisible()) {
                    if (valuesList == null || valuesList.isEmpty()) {
                        StructuredNameEditorView.this.rebuildFullName(StructuredNameEditorView.this.getValues());
                    } else {
                        StructuredNameEditorView.this.rebuildFullName(valuesList, StructuredNameEditorView.this.getValues());
                    }
                } else if (valuesList == null || valuesList.isEmpty()) {
                    StructuredNameEditorView.this.rebuildStructuredName(StructuredNameEditorView.this.getValues());
                } else {
                    StructuredNameEditorView.this.rebuildStructuredName(valuesList, StructuredNameEditorView.this.getValues());
                }
                StructuredNameEditorView.this.post(new Runnable() {
                    public void run() {
                        if (HwLog.HWDBG) {
                            HwLog.d("View", "execute notifyEditorListener");
                        }
                        StructuredNameEditorView.this.notifyEditorListener();
                    }
                });
            }
        }
    }

    private static class SavedState implements Parcelable {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        public boolean mChanged;
        public ContentValues mSnapshot;
        public Parcelable mSuperState;

        SavedState(Parcelable superState) {
            this.mSuperState = superState;
        }

        private SavedState(Parcel in) {
            boolean z = false;
            ClassLoader loader = getClass().getClassLoader();
            this.mSuperState = in.readParcelable(loader);
            if (in.readInt() != 0) {
                z = true;
            }
            this.mChanged = z;
            this.mSnapshot = (ContentValues) in.readParcelable(loader);
        }

        public void writeToParcel(Parcel out, int flags) {
            int i;
            out.writeParcelable(this.mSuperState, 0);
            if (this.mChanged) {
                i = 1;
            } else {
                i = 0;
            }
            out.writeInt(i);
            out.writeParcelable(this.mSnapshot, 0);
        }

        public int describeContents() {
            return 0;
        }
    }

    public StructuredNameEditorView(Context context) {
        super(context);
        this.mCust = (HwCustStructuredNameEditorView) HwCustUtils.createObj(HwCustStructuredNameEditorView.class, new Object[0]);
    }

    public StructuredNameEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mCust = (HwCustStructuredNameEditorView) HwCustUtils.createObj(HwCustStructuredNameEditorView.class, new Object[0]);
    }

    public StructuredNameEditorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mCust = (HwCustStructuredNameEditorView) HwCustUtils.createObj(HwCustStructuredNameEditorView.class, new Object[0]);
    }

    public void setValues(DataKind kind, ValuesDelta entry, RawContactDelta state, boolean readOnly, ViewIdGenerator vig) {
        super.setValues(kind, entry, state, readOnly, vig);
        if (this.mSnapshot == null) {
            DataItem item = DataItem.createFrom(new ContentValues(getValues().getCompleteValues()));
            if (item instanceof StructuredNameDataItem) {
                this.mSnapshot = (StructuredNameDataItem) item;
            }
            this.mChanged = entry.isInsert();
            return;
        }
        this.mChanged = false;
    }

    public void onFieldChanged(String column, String value, View view) {
        if (isFieldChanged(column, value)) {
            saveValue(column, value);
            if (this.mIccPhoneBookAdapter != null) {
                int lEncodedLength = this.mIccPhoneBookAdapter.getAlphaEncodedLength(value);
                if (value != null && lEncodedLength == -1) {
                    if (lEncodedLength < this.mSimMaxLimit) {
                    }
                }
                this.mHasShortAndLongForms = false;
            }
            this.mChanged = true;
            if (!EmuiFeatureManager.isChinaArea() && getRawContactEditorView().isPhoneticNameVisible() && !Locale.getDefault().getCountry().equalsIgnoreCase("JP") && (this.mCust == null || !this.mCust.disableSyncPhoneticName())) {
                String original = value;
                if (areOptionalFieldsVisible()) {
                    original = NameConverter.structuredNameToDisplayName(getContext(), NameConverter.valuesToStructuredNameMap(getValues()));
                }
                MultiReadingUtils.setLocale(Locale.getDefault());
                MultiReadingUtils utils = MultiReadingUtils.getInstance();
                try {
                    if (Locale.getDefault().getCountry().equalsIgnoreCase("TW")) {
                        utils.setUserFile(getContext().getAssets().open("multiZhuyin.txt"));
                    } else if (Locale.getDefault().getCountry().equalsIgnoreCase("CN")) {
                        utils.setUserFile(getContext().getAssets().open("multiPinyin.txt"));
                    }
                } catch (IOException e) {
                    HwLog.e("StructuredNameEditorView", "Can not find multiPinyin.txt");
                }
                String sortKey = utils.getReading(original);
                EditText firstEditField = getRawContactEditorView().getPhoneticNameEditor().getFirstEditField();
                if (firstEditField != null) {
                    firstEditField.setText(sortKey);
                }
                if (!(sortKey == null || sortKey.equals(""))) {
                    getState().getValues().put("sort_key", sortKey);
                }
            }
            ContactsThreadPool.getInstance().execute(new BuildName());
        }
    }

    protected void onOptionalFieldVisibilityChange() {
        if (hasShortAndLongForms()) {
            if (areOptionalFieldsVisible()) {
                switchFromFullNameToStructuredName();
            } else {
                switchFromStructuredNameToFullName();
            }
        }
        super.onOptionalFieldVisibilityChange();
    }

    private void switchFromFullNameToStructuredName() {
        ValuesDelta values = getValues();
        if (this.mChanged) {
            String displayName = values.getDisplayName();
            Map<String, String> structuredNameMap = NameConverter.displayNameToStructuredName(getContext(), displayName);
            if (!structuredNameMap.isEmpty()) {
                eraseFullName(values);
                for (Entry<String, String> entry : structuredNameMap.entrySet()) {
                    values.put((String) entry.getKey(), (String) entry.getValue());
                }
            }
            this.mSnapshot.getContentValues().clear();
            this.mSnapshot.getContentValues().putAll(values.getCompleteValues());
            this.mSnapshot.setDisplayName(displayName);
            return;
        }
        for (String field : NameConverter.getStructuredNameFields()) {
            values.put(field, this.mSnapshot.getContentValues().getAsString(field));
        }
    }

    private void switchFromStructuredNameToFullName() {
        ValuesDelta values = getValues();
        if (this.mChanged) {
            Map structuredNameMap = NameConverter.valuesToStructuredNameMap(values);
            String displayName = NameConverter.structuredNameToDisplayName(getContext(), structuredNameMap);
            if (!TextUtils.isEmpty(displayName)) {
                eraseStructuredName(values);
                values.put("data1", displayName);
            }
            this.mSnapshot.getContentValues().clear();
            this.mSnapshot.setDisplayName(values.getDisplayName());
            for (Entry<String, String> entry : structuredNameMap.entrySet()) {
                this.mSnapshot.getContentValues().put((String) entry.getKey(), (String) entry.getValue());
            }
            return;
        }
        values.setDisplayName(this.mSnapshot.getDisplayName());
    }

    private void eraseFullName(ValuesDelta values) {
        values.setDisplayName(null);
    }

    private void rebuildFullName(ValuesDelta values) {
        values.setDisplayName(NameConverter.getDisplayNameFromValuesDelta(getContext(), values));
    }

    private void rebuildFullName(ArrayList<ValuesDelta> valuesList, ValuesDelta values) {
        String displayName = NameConverter.getDisplayNameFromValuesDelta(getContext(), values);
        for (ValuesDelta entryValue : valuesList) {
            entryValue.setDisplayName(displayName);
        }
    }

    public void eraseStructuredName(ValuesDelta values) {
        for (String field : NameConverter.getStructuredNameFields()) {
            values.putNull(field);
        }
    }

    private void rebuildStructuredName(ValuesDelta values) {
        for (Entry<String, String> entry : NameConverter.displayNameToStructuredName(getContext(), values.getDisplayName()).entrySet()) {
            values.put((String) entry.getKey(), (String) entry.getValue());
        }
    }

    private void rebuildStructuredName(ArrayList<ValuesDelta> valuesList, ValuesDelta values) {
        for (Entry<String, String> entry : NameConverter.displayNameToStructuredName(getContext(), values.getDisplayName()).entrySet()) {
            for (ValuesDelta entryValue : valuesList) {
                entryValue.put((String) entry.getKey(), (String) entry.getValue());
            }
        }
    }

    protected Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        state.mChanged = this.mChanged;
        state.mSnapshot = this.mSnapshot.getContentValues();
        return state;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.mSuperState);
        this.mChanged = ss.mChanged;
        DataItem item = DataItem.createFrom(ss.mSnapshot);
        if (item instanceof StructuredNameDataItem) {
            this.mSnapshot = (StructuredNameDataItem) item;
        }
    }
}
