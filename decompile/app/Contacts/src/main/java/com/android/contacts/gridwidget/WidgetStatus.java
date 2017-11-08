package com.android.contacts.gridwidget;

public class WidgetStatus {
    private static WidgetStatus mWidgetStatus;
    private boolean addExtraItem = true;
    private boolean isDeleteMode;
    private boolean isEditMode;

    private WidgetStatus() {
    }

    public static WidgetStatus getWidgetStatus() {
        if (mWidgetStatus == null) {
            mWidgetStatus = new WidgetStatus();
        }
        return mWidgetStatus;
    }

    public boolean isDeleteMode() {
        return this.isDeleteMode;
    }

    public void setDeleteMode(boolean isDeleteMode) {
        this.isDeleteMode = isDeleteMode;
    }

    public boolean isEditMode() {
        return this.isEditMode;
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
    }
}
