package com.android.settings.colormode;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.support.v7.preference.Preference.BaseSavedState;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.android.settings.CustomDialogPreference;
import com.android.settings.ItemUseStat;
import com.android.settings.Utils;

public class ColorModeSettingsPreference extends CustomDialogPreference {
    private OnItemClickListener mClickedHandler;
    private ListView mColorModeChoiceList;
    private Context mContext;
    private int mLastMode;
    private ContentResolver mResolver;
    private boolean mRestoredOldState;
    private int mSelectedMode;

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int modeValue;

        public SavedState(Parcel source) {
            super(source);
            this.modeValue = source.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.modeValue);
        }
    }

    public ColorModeSettingsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mRestoredOldState = false;
        this.mSelectedMode = 0;
        this.mLastMode = 0;
        this.mClickedHandler = new OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                if (-1 != position) {
                    ColorModeSettingsPreference.this.mSelectedMode = position;
                    System.putIntForUser(ColorModeSettingsPreference.this.mResolver, "color_mode_switch", ColorModeSettingsPreference.this.mSelectedMode, UserHandle.myUserId());
                }
            }
        };
        this.mContext = context;
        this.mResolver = this.mContext.getContentResolver();
        setColorModeSummary();
        setDialogLayoutResource(2130968928);
    }

    public ColorModeSettingsPreference(Context context) {
        this(context, null);
    }

    protected void onClick() {
        setPotrait();
        super.onClick();
        this.mRestoredOldState = false;
    }

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        setPotrait();
        String[] MODES = new String[]{this.mContext.getResources().getString(2131628462), this.mContext.getResources().getString(2131628463)};
        this.mColorModeChoiceList = (ListView) view.findViewById(2131886894);
        this.mColorModeChoiceList.setAdapter(new ArrayAdapter(this.mContext, 2130969135, MODES));
        this.mColorModeChoiceList.setOverScrollMode(2);
        this.mColorModeChoiceList.setOnItemClickListener(this.mClickedHandler);
        initDefaultDialogView();
        this.mRestoredOldState = false;
    }

    private void setPotrait() {
        if (!Utils.isTablet()) {
            Activity a = getParentActivity(this.mContext);
            if (!(a == null || a.getRequestedOrientation() == 1)) {
                a.setRequestedOrientation(1);
            }
        }
    }

    private void setRotatable() {
        if (!Utils.isTablet()) {
            Activity a = getParentActivity(this.mContext);
            if (a != null) {
                a.setRequestedOrientation(-1);
            }
        }
    }

    private void initDefaultDialogView() {
        this.mLastMode = System.getIntForUser(this.mResolver, "color_mode_switch", 0, UserHandle.myUserId());
        this.mColorModeChoiceList.setSelection(this.mLastMode);
        this.mColorModeChoiceList.setItemChecked(this.mLastMode, true);
    }

    private void setColorModeSummary() {
        if (System.getIntForUser(this.mResolver, "color_mode_switch", 0, UserHandle.myUserId()) == 0) {
            setSummary(this.mContext.getResources().getString(2131628462));
        } else {
            setSummary(this.mContext.getResources().getString(2131628463));
        }
    }

    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (!positiveResult) {
            restoreOldState();
        } else if (this.mLastMode != this.mSelectedMode) {
            ItemUseStat.getInstance().handleClick(this.mContext, 2, "select different mode");
        }
        setRotatable();
        setColorModeSummary();
    }

    private Activity getParentActivity(Context context) {
        if (context == null) {
            return null;
        }
        if (context instanceof Activity) {
            return (Activity) context;
        }
        if (context instanceof ContextWrapper) {
            return getParentActivity(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }

    private void restoreOldState() {
        if (!this.mRestoredOldState) {
            System.putIntForUser(this.mResolver, "color_mode_switch", this.mLastMode, UserHandle.myUserId());
            this.mRestoredOldState = true;
        }
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (getDialog() == null || !getDialog().isShowing()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.modeValue = this.mSelectedMode;
        restoreOldState();
        return myState;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        System.putIntForUser(this.mResolver, "color_mode_switch", myState.modeValue, UserHandle.myUserId());
    }
}
