package com.android.contacts.model.account;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public abstract class AccountType {
    private static Comparator<DataKind> sWeightComparator = new Comparator<DataKind>() {
        public int compare(DataKind object1, DataKind object2) {
            return object1.weight - object2.weight;
        }
    };
    public String accountType = null;
    public String dataSet = null;
    public int iconRes;
    protected boolean mIsInitialized;
    private ArrayList<DataKind> mKinds = Lists.newArrayList();
    private HashMap<String, DataKind> mMimeKinds = Maps.newHashMap();
    public String resourcePackageName;
    public String syncAdapterPackageName;
    public int titleRes;

    public static class DefinitionException extends Exception {
        public DefinitionException(String message) {
            super(message);
        }

        public DefinitionException(String message, Exception inner) {
            super(message, inner);
        }
    }

    public static class DisplayLabelComparator implements Comparator<AccountType> {
        private final Collator mCollator = Collator.getInstance();
        private final Context mContext;

        public DisplayLabelComparator(Context context) {
            this.mContext = context;
        }

        private String getDisplayLabel(AccountType type) {
            CharSequence label = type.getDisplayLabel(this.mContext);
            return label == null ? "" : label.toString();
        }

        public int compare(AccountType lhs, AccountType rhs) {
            return this.mCollator.compare(getDisplayLabel(lhs), getDisplayLabel(rhs));
        }
    }

    public static final class EditField {
        public String column;
        public int inputType;
        public boolean longForm;
        public int minLines;
        public boolean optional;
        public boolean shortForm;
        public int titleRes;

        public EditField(String column, int titleRes) {
            this.column = column;
            this.titleRes = titleRes;
        }

        public EditField(String column, int titleRes, int inputType) {
            this(column, titleRes);
            this.inputType = inputType;
        }

        public EditField setOptional(boolean optional) {
            this.optional = optional;
            return this;
        }

        public EditField setShortForm(boolean shortForm) {
            this.shortForm = shortForm;
            return this;
        }

        public EditField setLongForm(boolean longForm) {
            this.longForm = longForm;
            return this;
        }

        public boolean isMultiLine() {
            return (this.inputType & 131072) != 0;
        }

        public String toString() {
            return getClass().getSimpleName() + ":" + " column=" + this.column + " titleRes=" + this.titleRes + " inputType=" + this.inputType + " minLines=" + this.minLines + " optional=" + this.optional + " shortForm=" + this.shortForm + " longForm=" + this.longForm;
        }
    }

    public static class EditType {
        public String customColumn;
        public String customTitle;
        public int labelRes;
        public int rawValue;
        public boolean secondary;
        public int specificMax = -1;

        public EditType(int rawValue, int labelRes) {
            this.rawValue = rawValue;
            this.labelRes = labelRes;
        }

        public EditType setSecondary(boolean secondary) {
            this.secondary = secondary;
            return this;
        }

        public EditType setSpecificMax(int specificMax) {
            this.specificMax = specificMax;
            return this;
        }

        public EditType setCustomColumn(String customColumn) {
            this.customColumn = customColumn;
            return this;
        }

        public EditType setCustomDialogTitle(String customTitle) {
            this.customTitle = customTitle;
            return this;
        }

        public boolean equals(Object object) {
            boolean z = false;
            if (!(object instanceof EditType)) {
                return false;
            }
            if (((EditType) object).rawValue == this.rawValue) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return this.rawValue;
        }

        public String toString() {
            return getClass().getSimpleName() + " rawValue=" + this.rawValue + " labelRes=" + this.labelRes + " secondary=" + this.secondary + " specificMax=" + this.specificMax + " customColumn=" + this.customColumn;
        }
    }

    public static class EventEditType extends EditType {
        private boolean mYearOptional;

        public EventEditType(int rawValue, int labelRes) {
            super(rawValue, labelRes);
        }

        public boolean isYearOptional() {
            return this.mYearOptional;
        }

        public EventEditType setYearOptional(boolean yearOptional) {
            this.mYearOptional = yearOptional;
            return this;
        }

        public String toString() {
            return super.toString() + " mYearOptional=" + this.mYearOptional;
        }

        public boolean equals(Object object) {
            return super.equals(object);
        }

        public int hashCode() {
            return super.hashCode();
        }
    }

    public interface StringInflater {
        CharSequence inflateUsing(Context context, ContentValues contentValues);
    }

    public abstract boolean areContactsWritable();

    public abstract boolean isGroupMembershipEditable();

    public final boolean isInitialized() {
        return this.mIsInitialized;
    }

    public boolean isExtension() {
        return false;
    }

    public String getEditContactActivityClassName() {
        return null;
    }

    public String getCreateContactActivityClassName() {
        return null;
    }

    public String getInviteContactActivityClassName() {
        return null;
    }

    public String getViewContactNotifyServiceClassName() {
        return null;
    }

    public String getViewContactNotifyServicePackageName() {
        return this.syncAdapterPackageName;
    }

    public String getViewGroupActivity() {
        return null;
    }

    public CharSequence getDisplayLabel(Context context) {
        if (this.titleRes != 0) {
            return getResourceText(context, this.syncAdapterPackageName, this.titleRes, this.accountType);
        }
        HwLog.e("AccountType", "resource not find. titleRes=" + this.titleRes);
        return this.accountType;
    }

    protected int getInviteContactActionResId() {
        return -1;
    }

    protected int getViewGroupLabelResId() {
        return -1;
    }

    public AccountTypeWithDataSet getAccountTypeAndDataSet() {
        return AccountTypeWithDataSet.get(this.accountType, this.dataSet);
    }

    public List<String> getExtensionPackageNames() {
        return new ArrayList();
    }

    public CharSequence getInviteContactActionLabel(Context context) {
        return getResourceText(context, this.syncAdapterPackageName, getInviteContactActionResId(), "");
    }

    public CharSequence getViewGroupLabel(Context context) {
        CharSequence customTitle = getResourceText(context, this.syncAdapterPackageName, getViewGroupLabelResId(), null);
        if (customTitle == null) {
            return context.getText(R.string.view_updates_from_group);
        }
        return customTitle;
    }

    @VisibleForTesting
    static CharSequence getResourceText(Context context, String packageName, int resId, String defaultValue) {
        if (resId != -1 && packageName != null) {
            try {
                return context.getPackageManager().getText(packageName, resId, null);
            } catch (NotFoundException e) {
                HwLog.e("AccountType", "resource not find." + resId);
                return defaultValue;
            }
        } else if (resId != -1) {
            return context.getText(resId);
        } else {
            return defaultValue;
        }
    }

    public Drawable getDisplayIcon(Context context) {
        if (this.titleRes != -1 && this.syncAdapterPackageName != null) {
            return context.getPackageManager().getDrawable(this.syncAdapterPackageName, this.iconRes, null);
        }
        if (this.titleRes != -1) {
            return context.getResources().getDrawable(this.iconRes);
        }
        return null;
    }

    public boolean isProfileEditable() {
        return false;
    }

    public ArrayList<DataKind> getSortedDataKinds() {
        Collections.sort(this.mKinds, sWeightComparator);
        return this.mKinds;
    }

    public DataKind getKindForMimetype(String mimeType) {
        return (DataKind) this.mMimeKinds.get(mimeType);
    }

    public DataKind addKind(DataKind kind) throws DefinitionException {
        if (kind.mimeType == null) {
            throw new DefinitionException("null is not a valid mime type");
        } else if (this.mMimeKinds.get(kind.mimeType) != null) {
            throw new DefinitionException("mime type '" + kind.mimeType + "' is already registered");
        } else {
            kind.resourcePackageName = this.resourcePackageName;
            this.mKinds.add(kind);
            this.mMimeKinds.put(kind.mimeType, kind);
            return kind;
        }
    }
}
