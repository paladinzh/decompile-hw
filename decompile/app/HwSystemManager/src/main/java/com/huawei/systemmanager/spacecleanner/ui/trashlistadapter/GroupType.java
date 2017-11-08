package com.huawei.systemmanager.spacecleanner.ui.trashlistadapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.spacecleanner.ui.SpaceState;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.TrashItemGroup;

public abstract class GroupType {
    public static final int TYPE_EMPTY = 3;
    public static final int TYPE_EXPANDE = 1;
    public static final int TYPE_JUMP = 2;
    public static final int TYPE_ROUND = 0;
    protected TextView title;

    public static abstract class GroupTypeFactory {
        abstract GroupType createHolder(View view);

        abstract int getLayoutResId();

        abstract int getType();

        View newView(LayoutInflater inflater, ViewGroup parent) {
            View view = inflater.inflate(getLayoutResId(), parent, false);
            view.setTag(createHolder(view));
            return view;
        }
    }

    abstract void bindView(boolean z, TrashItemGroup trashItemGroup, SpaceState spaceState, OnClickListener onClickListener, Boolean bool, OnClickListener onClickListener2);

    abstract int getType();

    public GroupType(View view) {
        this.title = (TextView) view.findViewById(R.id.text1);
    }
}
