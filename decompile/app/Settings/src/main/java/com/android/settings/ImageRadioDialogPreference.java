package com.android.settings;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.v7.preference.Preference.BaseSavedState;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ImageRadioDialogPreference extends CustomDialogPreference {
    private Context mContext;
    protected Map<String, Integer> mListAdapterResMap;
    protected ListView mListView;
    protected CharSequence mNetherSummary;
    protected String mValue;
    protected boolean mValueSet;

    public static class ImageRadioListAdapter extends ArrayAdapter<String> {
        private Integer[] mItemResoures;

        public ImageRadioListAdapter(Context context, int resource, Map<String, Integer> resMap) {
            super(context, resource);
            init(resMap);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (convertView == null) {
                view = LayoutInflater.from(getContext()).inflate(2130968843, parent, false);
            }
            ImageView image = (ImageView) view.findViewById(2131886748);
            if (!(image == null || this.mItemResoures == null)) {
                image.setImageResource(this.mItemResoures[position].intValue());
                image.setFocusable(false);
            }
            RadioButton radio = (RadioButton) view.findViewById(2131886749);
            if (radio != null) {
                radio.setFocusable(false);
            }
            return view;
        }

        private void init(Map<String, Integer> resMap) {
            if (resMap != null) {
                addAll(resMap.keySet());
                this.mItemResoures = (Integer[]) resMap.values().toArray(new Integer[resMap.values().size()]);
            }
        }
    }

    private class ItemClickListener implements OnItemClickListener {
        private ItemClickListener() {
        }

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String value = (String) parent.getItemAtPosition(position);
            if (ImageRadioDialogPreference.this.callChangeListener(value)) {
                ImageRadioDialogPreference.this.setValue(value);
            }
            Dialog dialog = ImageRadioDialogPreference.this.getDialog();
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    }

    public static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        Bundle resBundle;
        String value;

        public SavedState(Parcel source) {
            super(source);
            this.value = source.readString();
            this.resBundle = source.readBundle();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(this.value);
            dest.writeBundle(this.resBundle);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

    public ImageRadioDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        setDialogLayoutResource(2130968842);
    }

    public void setListAdapterResMap(Map<String, Integer> listAdapterResMap) {
        this.mListAdapterResMap = listAdapterResMap;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        updateView(view);
        super.onBindViewHolder(view);
    }

    public CharSequence getNetherSummary() {
        return this.mNetherSummary;
    }

    private void updateView(PreferenceViewHolder view) {
        TextView netherSummaryView = (TextView) view.findViewById(2131886914);
        if (netherSummaryView != null) {
            CharSequence summary = getNetherSummary();
            if (TextUtils.isEmpty(summary)) {
                netherSummaryView.setVisibility(8);
                return;
            }
            netherSummaryView.setText(summary);
            netherSummaryView.setVisibility(0);
        }
    }

    public void setValue(String value) {
        boolean changed;
        if (TextUtils.equals(this.mValue, value)) {
            changed = false;
        } else {
            changed = true;
        }
        if (changed || !this.mValueSet) {
            this.mValue = value;
            this.mValueSet = true;
            persistString(value);
            if (changed) {
                notifyChanged();
            }
        }
    }

    public String getValue() {
        return this.mValue;
    }

    protected Parcelable onSaveInstanceState() {
        SavedState myState = new SavedState(super.onSaveInstanceState());
        myState.value = getValue();
        myState.resBundle = getResBundle();
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
        this.mListAdapterResMap = restoreResMap(myState.resBundle);
    }

    protected int getValueIndex(ArrayAdapter<String> adapter) {
        if (adapter == null) {
            return -1;
        }
        int count = adapter.getCount();
        for (int idx = 0; idx < count; idx++) {
            if (TextUtils.equals((CharSequence) adapter.getItem(idx), this.mValue)) {
                return idx;
            }
        }
        return -1;
    }

    protected void onPrepareDialogBuilder(Builder builder, OnClickListener listener) {
        builder.setTitle(this.mContext.getString(2131628913));
        builder.setPositiveButton(null, null);
        builder.setNegativeButton(17039360, listener);
    }

    protected void onBindDialogView(View view) {
        this.mListView = (ListView) view.findViewById(16908298);
        ImageRadioListAdapter adapter = new ImageRadioListAdapter(this.mContext, 2130968843, this.mListAdapterResMap);
        this.mListView.setAdapter(adapter);
        this.mListView.setItemsCanFocus(true);
        this.mListView.setChoiceMode(1);
        this.mListView.setOnItemClickListener(new ItemClickListener());
        this.mListView.setScrollBarStyle(33554432);
        int pos = getValueIndex(adapter);
        if (pos >= 0) {
            this.mListView.setSelection(pos);
            this.mListView.setItemChecked(pos, true);
        }
    }

    private Bundle getResBundle() {
        Bundle bundle = new Bundle();
        if (this.mListAdapterResMap != null) {
            for (Entry<String, Integer> entry : this.mListAdapterResMap.entrySet()) {
                bundle.putInt((String) entry.getKey(), ((Integer) entry.getValue()).intValue());
            }
        }
        return bundle;
    }

    private Map<String, Integer> restoreResMap(Bundle bundle) {
        Map<String, Integer> resMap = new HashMap();
        for (String key : bundle.keySet()) {
            resMap.put(key, Integer.valueOf(bundle.getInt(key)));
        }
        return resMap;
    }
}
