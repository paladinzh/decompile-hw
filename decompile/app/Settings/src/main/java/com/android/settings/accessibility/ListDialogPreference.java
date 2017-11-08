package com.android.settings.accessibility;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.v7.preference.Preference.BaseSavedState;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import com.android.settings.CustomDialogPreference;

public abstract class ListDialogPreference extends CustomDialogPreference {
    private CharSequence[] mEntryTitles;
    private int[] mEntryValues;
    private int mListItemLayout;
    private OnValueChangedListener mOnValueChangedListener;
    private int mValue;
    private int mValueIndex;
    private boolean mValueSet;

    public interface OnValueChangedListener {
        void onValueChanged(ListDialogPreference listDialogPreference, int i);
    }

    private class ListPreferenceAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        private ListPreferenceAdapter() {
        }

        public int getCount() {
            return ListDialogPreference.this.mEntryValues.length;
        }

        public Integer getItem(int position) {
            return Integer.valueOf(ListDialogPreference.this.mEntryValues[position]);
        }

        public long getItemId(int position) {
            return (long) ListDialogPreference.this.mEntryValues[position];
        }

        public boolean hasStableIds() {
            return true;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                if (this.mInflater == null) {
                    this.mInflater = LayoutInflater.from(parent.getContext());
                }
                convertView = this.mInflater.inflate(ListDialogPreference.this.mListItemLayout, parent, false);
            }
            ListDialogPreference.this.onBindListItem(convertView, position);
            return convertView;
        }
    }

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        public int value;

        public SavedState(Parcel source) {
            super(source);
            this.value = source.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.value);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

    protected abstract void onBindListItem(View view, int i);

    public ListDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnValueChangedListener(OnValueChangedListener listener) {
        this.mOnValueChangedListener = listener;
    }

    public void setListItemLayoutResource(int layoutResId) {
        this.mListItemLayout = layoutResId;
    }

    public void setValues(int[] values) {
        this.mEntryValues = values;
        if (this.mValueSet && this.mValueIndex == -1) {
            this.mValueIndex = getIndexForValue(this.mValue);
        }
    }

    public void setTitles(CharSequence[] titles) {
        this.mEntryTitles = titles;
    }

    protected CharSequence getTitleAt(int index) {
        if (this.mEntryTitles == null || this.mEntryTitles.length <= index) {
            return null;
        }
        return this.mEntryTitles[index];
    }

    protected int getValueAt(int index) {
        return this.mEntryValues[index];
    }

    public CharSequence getSummary() {
        if (this.mValueIndex >= 0) {
            return getTitleAt(this.mValueIndex);
        }
        return null;
    }

    protected void onPrepareDialogBuilder(Builder builder, OnClickListener listener) {
        super.onPrepareDialogBuilder(builder, listener);
        Context context = getContext();
        View picker = LayoutInflater.from(context).inflate(getDialogLayoutResource(), null);
        AbsListView list = (AbsListView) picker.findViewById(16908298);
        list.setAdapter(new ListPreferenceAdapter());
        list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                if (ListDialogPreference.this.callChangeListener(Integer.valueOf((int) id))) {
                    ListDialogPreference.this.setValue((int) id);
                }
                Dialog dialog = ListDialogPreference.this.getDialog();
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        int selectedPosition = getIndexForValue(this.mValue);
        if (selectedPosition != -1) {
            list.setSelection(selectedPosition);
        }
        builder.setView(picker);
        builder.setPositiveButton(null, null);
    }

    protected int getIndexForValue(int value) {
        int[] values = this.mEntryValues;
        if (values != null) {
            int count = values.length;
            for (int i = 0; i < count; i++) {
                if (values[i] == value) {
                    return i;
                }
            }
        }
        return -1;
    }

    public void setValue(int value) {
        boolean changed = this.mValue != value;
        if (changed || !this.mValueSet) {
            this.mValue = value;
            this.mValueIndex = getIndexForValue(value);
            this.mValueSet = true;
            persistInt(value);
            if (changed) {
                notifyDependencyChange(shouldDisableDependents());
                notifyChanged();
            }
            if (this.mOnValueChangedListener != null) {
                this.mOnValueChangedListener.onValueChanged(this, value);
            }
        }
    }

    public int getValue() {
        return this.mValue;
    }

    protected Object onGetDefaultValue(TypedArray a, int index) {
        return Integer.valueOf(a.getInt(index, 0));
    }

    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedInt(this.mValue) : ((Integer) defaultValue).intValue());
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.value = getValue();
        return myState;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setValue(myState.value);
    }
}
