package com.huawei.systemmanager.spacecleanner.ui.commonitem;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class OpenSecondaryParam implements Parcelable {
    public static final int CHECK_STATE_FALSE = -1;
    public static final int CHECK_STATE_TRUE = 1;
    public static final int CHECK_STATE_UNKONW = 0;
    public static final Creator<OpenSecondaryParam> CREATOR = new Creator<OpenSecondaryParam>() {
        public OpenSecondaryParam createFromParcel(Parcel in) {
            OpenSecondaryParam param = new OpenSecondaryParam();
            Bundle bundle = in.readBundle();
            if (bundle != null) {
                param.data.putAll(bundle);
            }
            return param;
        }

        public OpenSecondaryParam[] newArray(int size) {
            return new OpenSecondaryParam[size];
        }
    };
    private static final String KEY_ALL_DIALOG_TITLE_ID = "mAllDialogTitle";
    private static final String KEY_CHECK_STATE = "mCheckState";
    private static final String KEY_DEEP_ITEM_TYPE = "deepItemType";
    private static final String KEY_DIALOG_CONTENT_ID = "mDialogContent";
    private static final String KEY_DIALOG_POSITIVE_BUTTON_ID = "mPositiveButton";
    private static final String KEY_DIALOG_TITLE_ID = "mDialogTitle";
    private static final String KEY_EMPTY_ICON_ID = "emptyIconID";
    private static final String KEY_EMPTY_TEXT_ID = "emptyTextID";
    private static final String KEY_OPERATION_ID = "operationResId";
    private static final String KEY_SCAN_TYPE = "scanType";
    private static final String KEY_SUB_TRASH_TYPE = "subTrashType";
    private static final String KEY_TITLE_STR = "titleStr";
    private static final String KEY_TRASH_TYPE = "trashType";
    private static final String KEY_UNIQUE_DESCRIPTION = "";
    private Bundle data = new Bundle();

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeBundle(this.data);
    }

    public void setDeepItemType(int deepItemType) {
        this.data.putInt(KEY_DEEP_ITEM_TYPE, deepItemType);
    }

    public int getDeepItemType() {
        return this.data.getInt(KEY_DEEP_ITEM_TYPE);
    }

    public int getScanType() {
        return this.data.getInt("scanType");
    }

    public void setScanType(int scanType) {
        this.data.putInt("scanType", scanType);
    }

    public int getTrashType() {
        return this.data.getInt(KEY_TRASH_TYPE);
    }

    public void setTrashType(int trashType) {
        this.data.putInt(KEY_TRASH_TYPE, trashType);
    }

    public int getSubTrashType() {
        return this.data.getInt(KEY_SUB_TRASH_TYPE);
    }

    public void setSubTrashType(int trashType) {
        this.data.putInt(KEY_SUB_TRASH_TYPE, trashType);
    }

    public String getUniqueDescription() {
        return this.data.getString("", "");
    }

    public void setUniqueDescription(String description) {
        this.data.putString("", description);
    }

    public int getOperationResId() {
        return this.data.getInt(KEY_OPERATION_ID);
    }

    public void setOperationResId(int operationResId) {
        this.data.putInt(KEY_OPERATION_ID, operationResId);
    }

    public int getEmptyIconID() {
        return this.data.getInt(KEY_EMPTY_ICON_ID);
    }

    public void setEmptyIconID(int emptyIconID) {
        this.data.putInt(KEY_EMPTY_ICON_ID, emptyIconID);
    }

    public int getEmptyTextID() {
        return this.data.getInt(KEY_EMPTY_TEXT_ID);
    }

    public void setEmptyTextID(int emptyTextID) {
        this.data.putInt(KEY_EMPTY_TEXT_ID, emptyTextID);
    }

    public String getTitleStr() {
        return this.data.getString(KEY_TITLE_STR);
    }

    public void setTitleStr(String titleStr) {
        this.data.putString(KEY_TITLE_STR, titleStr);
    }

    public int getCheckState() {
        return this.data.getInt(KEY_CHECK_STATE, 0);
    }

    public void setCheckState(int state) {
        this.data.putInt(KEY_CHECK_STATE, state);
    }

    public int getDialogTitleId() {
        return this.data.getInt(KEY_DIALOG_TITLE_ID);
    }

    public void setDialogTitleId(int titleId) {
        this.data.putInt(KEY_DIALOG_TITLE_ID, titleId);
    }

    public void setAllDialogTitleId(int titleId) {
        this.data.putInt(KEY_ALL_DIALOG_TITLE_ID, titleId);
    }

    public int getAllDialogTitleId() {
        return this.data.getInt(KEY_ALL_DIALOG_TITLE_ID);
    }

    public void setDialogPositiveButtonId(int titleId) {
        this.data.putInt(KEY_DIALOG_POSITIVE_BUTTON_ID, titleId);
    }

    public int gettDialogPositiveButtonId() {
        return this.data.getInt(KEY_DIALOG_POSITIVE_BUTTON_ID);
    }

    public int getDialogContentId() {
        return this.data.getInt(KEY_DIALOG_CONTENT_ID);
    }

    public void setDialogContentId(int contentId) {
        this.data.putInt(KEY_DIALOG_CONTENT_ID, contentId);
    }
}
