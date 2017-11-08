package com.huawei.systemmanager.spacecleanner.ui.commonitem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.Item.CheckItem;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;

public abstract class ITrashItem implements CheckItem {
    public static final int ACTION_DO_NOTHING = 0;
    public static final int ACTION_JUMP_TO_DEEP = 4;
    public static final int ACTION_JUMP_TO_SECOND = 3;
    public static final int ACTION_OPEN_CUSTOM_DIALOG = 6;
    public static final int ACTION_OPEN_DIALOG = 5;
    public static final int ACTION_TOGGLE_ITEM = 2;
    protected volatile boolean mChecked = false;
    private volatile boolean mCleaned = false;

    public abstract int getTrashCount();

    public abstract long getTrashSize();

    public abstract long getTrashSizeCleaned(boolean z);

    public abstract boolean isNoTrash();

    public abstract void refreshContent();

    public Trash getTrash() {
        return null;
    }

    public boolean isCheckable() {
        return true;
    }

    public void setChecked(boolean checked) {
        this.mChecked = checked;
    }

    public boolean isChecked() {
        return this.mChecked;
    }

    public void toggle() {
        setChecked(!isChecked());
    }

    public String getName() {
        return null;
    }

    public String getDescription(Context ctx) {
        return "";
    }

    public String getDesctiption2() {
        return "";
    }

    public String getPreMessage() {
        return "";
    }

    public String getSummary() {
        return "";
    }

    public Drawable getIcon(Context ctx) {
        return null;
    }

    public int getIconWidth(Context ctx) {
        return 0;
    }

    public int getIconHeight(Context ctx) {
        return 0;
    }

    public int getTrashType() {
        return 0;
    }

    public int getSubTrashType() {
        return -1;
    }

    public boolean isSuggestClean() {
        return false;
    }

    public boolean isCleaned() {
        return this.mCleaned;
    }

    protected void setCleaned() {
        this.mCleaned = true;
    }

    public String getTrashPath() {
        return "";
    }

    public boolean shouldLoadPic() {
        return false;
    }

    public boolean isUseIconAlways() {
        return false;
    }

    public OpenSecondaryParam getOpenSecondaryParam() {
        return buildCommonSecondaryParam();
    }

    public boolean hasSecondary() {
        return false;
    }

    public int doClickAction() {
        if (getTrashType() == 131072) {
            return 4;
        }
        if (isCleaned() || isNoTrash()) {
            return 0;
        }
        if (hasSecondary()) {
            return 3;
        }
        return 5;
    }

    protected OpenSecondaryParam buildCommonSecondaryParam() {
        int i;
        OpenSecondaryParam params = new OpenSecondaryParam();
        if (isSuggestClean()) {
            i = 50;
        } else {
            i = 100;
        }
        params.setScanType(i);
        params.setTrashType(getTrashType());
        params.setOperationResId(R.string.common_delete);
        params.setEmptyTextID(R.string.no_file_trash_tip);
        params.setEmptyIconID(R.drawable.ic_no_apps);
        params.setDialogTitleId(R.plurals.space_clean_any_data_delete_title);
        params.setAllDialogTitleId(R.string.space_clean_all_data_delete_title);
        params.setDialogContentId(R.plurals.space_clean_data_delete_message);
        params.setDialogPositiveButtonId(R.string.common_delete);
        if (isCheckable()) {
            params.setCheckState(isChecked() ? 1 : -1);
        }
        return params;
    }

    public Drawable getItemIcon() {
        return null;
    }

    public boolean hasIcon() {
        return true;
    }

    public short getYear() {
        return (short) -1;
    }

    public byte getMonth() {
        return (byte) -1;
    }
}
