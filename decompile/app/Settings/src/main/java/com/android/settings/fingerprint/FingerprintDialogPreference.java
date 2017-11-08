package com.android.settings.fingerprint;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.hardware.fingerprint.Fingerprint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.UserHandle;
import android.support.v7.preference.Preference.BaseSavedState;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import com.android.settings.CustomDialogPreference;
import com.android.settings.fingerprint.utils.BiometricManager;

public class FingerprintDialogPreference extends CustomDialogPreference {
    protected BiometricManager mBiometricManager;
    protected int mClickedDialogEntryIndex;
    protected Context mContext;
    protected Fingerprint mCurrentFinger;
    protected Dialog mDialog;
    protected FingerprintDialogListener mDialogListener;
    protected Entry[] mEntries;
    protected int mFingerNum;
    protected HighlightItemHandler mHandler;
    protected ListView mListView;
    protected CharSequence mNetherSummary;
    protected int mUserId = UserHandle.myUserId();
    protected int mValidStartPos = 0;
    protected String mValue;
    protected boolean mValueSet;

    public static class Entry {
        String key;
        String value;

        public Entry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String toString() {
            return this.key;
        }
    }

    private class HighlightItemHandler extends Handler {
        private HighlightItemHandler() {
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                FingerprintDialogPreference.this.lightItem(msg.arg1, false);
                if (FingerprintDialogPreference.this.mDialogListener != null) {
                    FingerprintDialogPreference.this.mDialogListener.onItemHighLightOff(FingerprintDialogPreference.this);
                }
            }
            super.handleMessage(msg);
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
        String value;

        public SavedState(Parcel source) {
            super(source);
            this.value = source.readString();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(this.value);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

    public FingerprintDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mBiometricManager = BiometricManager.open(context);
        this.mContext = context;
        setDialogLayoutResource(2130969130);
    }

    public CharSequence getNetherSummary() {
        return this.mNetherSummary;
    }

    public void setNetherSummary(CharSequence summary) {
        if (summary != null || this.mNetherSummary == null) {
            if (summary == null) {
                return;
            }
            if (summary.equals(this.mNetherSummary)) {
                return;
            }
        }
        this.mNetherSummary = summary;
        notifyChanged();
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

    public void clear() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(1);
        }
    }

    public int findIndexOfValue(String value) {
        if (!(value == null || this.mEntries == null)) {
            for (int idx = this.mEntries.length - 1; idx >= 0; idx--) {
                if (this.mEntries[idx].value.equals(value)) {
                    return idx;
                }
            }
        }
        return -1;
    }

    public void highLightItem(String fpIdStr) {
        this.mHandler = new HighlightItemHandler();
        int position = findIndexOfValue(fpIdStr);
        if (position >= this.mValidStartPos) {
            this.mListView.smoothScrollToPosition(position);
            lightItem(position, true);
            Message msg = new Message();
            msg.what = 1;
            msg.arg1 = position;
            this.mHandler.sendMessageDelayed(msg, 1000);
        }
    }

    protected void lightItem(int position, boolean isLight) {
        if (isLight) {
            this.mListView.getChildAt(position).setBackgroundColor(this.mContext.getResources().getColor(2131427503));
        } else {
            this.mListView.getChildAt(position).setBackgroundColor(17170445);
        }
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        TextView netherSummaryView = (TextView) view.findViewById(2131886914);
        if (netherSummaryView != null) {
            CharSequence summary = getNetherSummary();
            if (TextUtils.isEmpty(summary)) {
                netherSummaryView.setVisibility(8);
            } else {
                netherSummaryView.setText(summary);
                netherSummaryView.setVisibility(0);
                netherSummaryView.setMaxLines(4);
            }
        }
        super.onBindViewHolder(view);
    }

    protected Dialog onCreateDialog(Bundle savedInstanceState, Dialog dialog) {
        this.mDialog = dialog;
        if (this.mDialog != null) {
            Log.i("FingerprintDialogPreference", "onCreateDialog set dialog!");
        } else {
            Log.i("FingerprintDialogPreference", "onCreateDialog cannot get dialog!");
        }
        return dialog;
    }

    public void setDialogListener(FingerprintDialogListener dialogListener) {
        this.mDialogListener = dialogListener;
    }

    public boolean isDialogShowing() {
        return this.mDialog != null ? this.mDialog.isShowing() : false;
    }

    public void setUserId(int userId) {
        this.mUserId = userId;
    }

    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            defaultValue = getPersistedString(this.mValue);
        } else {
            String defaultValue2 = (String) defaultValue;
        }
        setValue(defaultValue);
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

    protected int getValueIndex() {
        return findIndexOfValue(this.mValue);
    }
}
