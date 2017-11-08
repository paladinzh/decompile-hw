package com.android.settings;

import android.app.AlertDialog.Builder;
import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class AppListPreference extends CustomListPreference {
    private Drawable[] mEntryDrawables;
    protected final boolean mForWork;
    private boolean mShowItemNone = false;
    private CharSequence[] mSummaries;
    private int mSystemAppIndex = -1;
    protected final int mUserId;

    public class AppArrayAdapter extends ArrayAdapter<CharSequence> {
        private Drawable[] mImageDrawables = null;
        private int mSelectedIndex = 0;

        public AppArrayAdapter(Context context, int textViewResourceId, CharSequence[] objects, Drawable[] imageDrawables, int selectedIndex) {
            super(context, textViewResourceId, objects);
            this.mSelectedIndex = selectedIndex;
            this.mImageDrawables = imageDrawables;
        }

        public boolean isEnabled(int position) {
            return AppListPreference.this.mSummaries == null || AppListPreference.this.mSummaries[position] == null;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            boolean enabled = true;
            View view = LayoutInflater.from(getContext()).inflate(2130968634, parent, false);
            ((TextView) view.findViewById(16908310)).setText((CharSequence) getItem(position));
            if (position == this.mSelectedIndex && position == AppListPreference.this.mSystemAppIndex) {
                view.findViewById(2131886262).setVisibility(0);
            } else if (position == this.mSelectedIndex) {
                view.findViewById(2131886260).setVisibility(0);
            } else if (position == AppListPreference.this.mSystemAppIndex) {
                view.findViewById(2131886261).setVisibility(0);
            }
            ((ImageView) view.findViewById(16908294)).setImageDrawable(this.mImageDrawables[position]);
            if (!(AppListPreference.this.mSummaries == null || AppListPreference.this.mSummaries[position] == null)) {
                enabled = false;
            }
            view.setEnabled(enabled);
            if (!enabled) {
                TextView summary = (TextView) view.findViewById(16908304);
                summary.setText(AppListPreference.this.mSummaries[position]);
                summary.setVisibility(0);
            }
            return view;
        }
    }

    private static class SavedState implements Parcelable {
        public static Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source.readCharSequenceArray(), source.readCharSequence(), source.readCharSequenceArray(), source.readInt() != 0, source.readParcelable(getClass().getClassLoader()));
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        public final CharSequence[] entryValues;
        public final boolean showItemNone;
        public final CharSequence[] summaries;
        public final Parcelable superState;
        public final CharSequence value;

        public SavedState(CharSequence[] entryValues, CharSequence value, CharSequence[] summaries, boolean showItemNone, Parcelable superState) {
            this.entryValues = entryValues;
            this.value = value;
            this.showItemNone = showItemNone;
            this.superState = superState;
            this.summaries = summaries;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeCharSequenceArray(this.entryValues);
            dest.writeCharSequence(this.value);
            dest.writeInt(this.showItemNone ? 1 : 0);
            dest.writeParcelable(this.superState, flags);
            dest.writeCharSequenceArray(this.summaries);
        }
    }

    public AppListPreference(Context context, AttributeSet attrs, int defStyle, int defAttrs) {
        int myUserId;
        super(context, attrs, defStyle, defAttrs);
        this.mForWork = context.obtainStyledAttributes(attrs, R$styleable.WorkPreference, 0, 0).getBoolean(0, false);
        UserHandle managedProfile = Utils.getManagedProfile(UserManager.get(context));
        if (!this.mForWork || managedProfile == null) {
            myUserId = UserHandle.myUserId();
        } else {
            myUserId = managedProfile.getIdentifier();
        }
        this.mUserId = myUserId;
    }

    public AppListPreference(Context context, AttributeSet attrs) {
        int myUserId;
        super(context, attrs);
        this.mForWork = context.obtainStyledAttributes(attrs, R$styleable.WorkPreference, 0, 0).getBoolean(0, false);
        UserHandle managedProfile = Utils.getManagedProfile(UserManager.get(context));
        if (!this.mForWork || managedProfile == null) {
            myUserId = UserHandle.myUserId();
        } else {
            myUserId = managedProfile.getIdentifier();
        }
        this.mUserId = myUserId;
    }

    public void setShowItemNone(boolean showItemNone) {
        this.mShowItemNone = showItemNone;
    }

    public void setPackageNames(CharSequence[] packageNames, CharSequence defaultPackageName) {
        setPackageNames(packageNames, defaultPackageName, null);
    }

    public void setPackageNames(CharSequence[] packageNames, CharSequence defaultPackageName, CharSequence systemPackageName) {
        PackageManager pm = getContext().getPackageManager();
        int entryCount = packageNames.length + (this.mShowItemNone ? 1 : 0);
        List<CharSequence> applicationNames = new ArrayList(entryCount);
        List<CharSequence> validatedPackageNames = new ArrayList(entryCount);
        List<Drawable> entryDrawables = new ArrayList(entryCount);
        int selectedIndex = -1;
        this.mSystemAppIndex = -1;
        for (int i = 0; i < packageNames.length; i++) {
            try {
                ApplicationInfo appInfo = pm.getApplicationInfoAsUser(packageNames[i].toString(), 0, this.mUserId);
                applicationNames.add(appInfo.loadLabel(pm));
                validatedPackageNames.add(appInfo.packageName);
                entryDrawables.add(appInfo.loadIcon(pm));
                if (defaultPackageName != null && appInfo.packageName.contentEquals(defaultPackageName)) {
                    selectedIndex = i;
                }
                if (!(appInfo.packageName == null || systemPackageName == null || !appInfo.packageName.contentEquals(systemPackageName))) {
                    this.mSystemAppIndex = i;
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (this.mShowItemNone) {
            applicationNames.add(getContext().getResources().getText(2131627039));
            validatedPackageNames.add("");
            entryDrawables.add(getContext().getDrawable(2130838328));
        }
        setEntries((CharSequence[]) applicationNames.toArray(new CharSequence[applicationNames.size()]));
        setEntryValues((CharSequence[]) validatedPackageNames.toArray(new CharSequence[validatedPackageNames.size()]));
        this.mEntryDrawables = (Drawable[]) entryDrawables.toArray(new Drawable[entryDrawables.size()]);
        if (selectedIndex != -1) {
            setValueIndex(selectedIndex);
        } else {
            setValue(null);
        }
    }

    public void setComponentNames(ComponentName[] componentNames, ComponentName defaultCN, CharSequence[] summaries) {
        this.mSummaries = summaries;
        PackageManager pm = getContext().getPackageManager();
        int entryCount = componentNames.length + (this.mShowItemNone ? 1 : 0);
        List<CharSequence> applicationNames = new ArrayList(entryCount);
        List<CharSequence> validatedComponentNames = new ArrayList(entryCount);
        List<Drawable> entryDrawables = new ArrayList(entryCount);
        int selectedIndex = -1;
        int i = 0;
        while (i < componentNames.length) {
            try {
                ActivityInfo activityInfo = AppGlobals.getPackageManager().getActivityInfo(componentNames[i], 0, this.mUserId);
                if (activityInfo != null) {
                    applicationNames.add(activityInfo.loadLabel(pm));
                    validatedComponentNames.add(componentNames[i].flattenToString());
                    entryDrawables.add(activityInfo.loadIcon(pm));
                    if (defaultCN != null && componentNames[i].equals(defaultCN)) {
                        selectedIndex = i;
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            i++;
        }
        if (this.mShowItemNone) {
            applicationNames.add(getContext().getResources().getText(2131627039));
            validatedComponentNames.add("");
            entryDrawables.add(getContext().getDrawable(2130838328));
        }
        setEntries((CharSequence[]) applicationNames.toArray(new CharSequence[applicationNames.size()]));
        setEntryValues((CharSequence[]) validatedComponentNames.toArray(new CharSequence[validatedComponentNames.size()]));
        this.mEntryDrawables = (Drawable[]) entryDrawables.toArray(new Drawable[entryDrawables.size()]);
        if (selectedIndex != -1) {
            setValueIndex(selectedIndex);
        } else {
            setValue(null);
        }
    }

    protected ListAdapter createListAdapter() {
        String selectedValue = getValue();
        boolean selectedNone = selectedValue != null ? this.mShowItemNone ? selectedValue.contentEquals("") : false : true;
        return new AppArrayAdapter(getContext(), 2130968634, getEntries(), this.mEntryDrawables, selectedNone ? -1 : findIndexOfValue(selectedValue));
    }

    protected void onPrepareDialogBuilder(Builder builder, OnClickListener listener) {
        builder.setAdapter(createListAdapter(), listener);
    }

    protected Parcelable onSaveInstanceState() {
        return new SavedState(getEntryValues(), getValue(), this.mSummaries, this.mShowItemNone, super.onSaveInstanceState());
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            this.mShowItemNone = savedState.showItemNone;
            setPackageNames(savedState.entryValues, savedState.value);
            this.mSummaries = savedState.summaries;
            super.onRestoreInstanceState(savedState.superState);
            return;
        }
        super.onRestoreInstanceState(state);
    }
}
