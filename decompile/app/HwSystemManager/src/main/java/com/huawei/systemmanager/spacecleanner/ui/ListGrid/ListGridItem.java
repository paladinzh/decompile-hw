package com.huawei.systemmanager.spacecleanner.ui.ListGrid;

import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;

public class ListGridItem {
    public static final int CONTENT_TYPE = 1;
    public static final int SUM_TYPE = 2;
    public static final int TITLE_TYPE = 0;

    public interface BaseListGridItem {
        String getDes();

        String getTitle();

        ITrashItem getTrashItem(int i);

        int getType();

        boolean isChecked();

        void setChecked(boolean z);
    }

    public static class SimpleListGridItem implements BaseListGridItem {
        public int getType() {
            return 0;
        }

        public String getTitle() {
            return null;
        }

        public String getDes() {
            return null;
        }

        public ITrashItem getTrashItem(int index) {
            return null;
        }

        public boolean isChecked() {
            return false;
        }

        public void setChecked(boolean checked) {
        }
    }
}
