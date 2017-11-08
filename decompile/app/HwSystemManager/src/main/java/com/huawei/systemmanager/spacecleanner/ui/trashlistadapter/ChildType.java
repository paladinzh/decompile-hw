package com.huawei.systemmanager.spacecleanner.ui.trashlistadapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;

public abstract class ChildType extends ViewType {
    public static final int TYPE_APK = 5;
    public static final int TYPE_CHECK = 0;
    public static final int TYPE_EMPTY = 1;
    public static final int TYPE_IMAGE = 4;
    public static final int TYPE_JUMP = 2;
    public static final int TYPE_VIDEO = 3;

    public static class ChildViewHolder {
        private final ChildType childType;

        public ChildViewHolder(ChildType type) {
            this.childType = type;
        }

        void bindView(boolean isLastChild, View convertView, ITrashItem trashItem) {
            if (this.childType != null) {
                this.childType.bindView(isLastChild, convertView, trashItem);
            }
        }
    }

    public abstract void bindView(boolean z, View view, ITrashItem iTrashItem);

    public abstract View newView(int i, int i2, boolean z, ViewGroup viewGroup, ITrashItem iTrashItem);

    public ChildType(LayoutInflater inflater) {
        super(inflater);
    }
}
