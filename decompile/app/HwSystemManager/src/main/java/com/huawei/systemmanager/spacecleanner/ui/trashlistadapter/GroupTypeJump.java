package com.huawei.systemmanager.spacecleanner.ui.trashlistadapter;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.netassistant.utils.ViewUtils;
import com.huawei.systemmanager.spacecleanner.ui.SpaceState;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.TrashItemGroup;
import com.huawei.systemmanager.spacecleanner.ui.trashlistadapter.GroupType.GroupTypeFactory;

public class GroupTypeJump extends GroupType {
    public static final GroupTypeFactory sJumpGroupFactory = new GroupTypeFactory() {
        GroupType createHolder(View view) {
            return new GroupTypeJump(view);
        }

        int getLayoutResId() {
            return R.layout.spaceclean_expand_group_item_singleline_arrow;
        }

        int getType() {
            return 2;
        }
    };
    private final ImageView indicator;
    private final TextView number;

    public GroupTypeJump(View view) {
        super(view);
        this.title = (TextView) view.findViewById(R.id.text1);
        this.number = (TextView) view.findViewById(R.id.number);
        this.indicator = (ImageView) view.findViewById(R.id.arrow);
        ViewUtils.setVisibility(this.number, 8);
    }

    void bindView(boolean isExpanded, TrashItemGroup itemGroup, SpaceState state, OnClickListener itemClicker, Boolean canShowProgress, OnClickListener checkClicker) {
        this.title.setText(itemGroup.getName());
        this.indicator.setImageResource(isExpanded ? R.drawable.spacecleaner_expander_arrow_close : R.drawable.spacecleaner_expander_arrow_open);
    }

    int getType() {
        return 2;
    }
}
