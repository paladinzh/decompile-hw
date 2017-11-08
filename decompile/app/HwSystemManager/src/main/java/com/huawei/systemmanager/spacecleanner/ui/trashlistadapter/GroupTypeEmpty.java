package com.huawei.systemmanager.spacecleanner.ui.trashlistadapter;

import android.view.View;
import android.view.View.OnClickListener;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.spacecleanner.ui.SpaceState;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.TrashItemGroup;
import com.huawei.systemmanager.spacecleanner.ui.trashlistadapter.GroupType.GroupTypeFactory;

public class GroupTypeEmpty extends GroupType {
    public static final GroupTypeFactory sEmptyGroupFactory = new GroupTypeFactory() {
        int getLayoutResId() {
            return R.layout.spaceclean_trashlist_group_item_none;
        }

        int getType() {
            return 3;
        }

        GroupType createHolder(View view) {
            return new GroupTypeEmpty(view);
        }
    };

    public GroupTypeEmpty(View view) {
        super(view);
        view.findViewById(R.id.number).setVisibility(8);
        view.findViewById(R.id.total_Size).setVisibility(8);
    }

    int getType() {
        return 3;
    }

    void bindView(boolean isExpanded, TrashItemGroup itemGroup, SpaceState state, OnClickListener itemClicker, Boolean canShowProgress, OnClickListener checkClicker) {
        this.title.setText(itemGroup.getName());
    }
}
