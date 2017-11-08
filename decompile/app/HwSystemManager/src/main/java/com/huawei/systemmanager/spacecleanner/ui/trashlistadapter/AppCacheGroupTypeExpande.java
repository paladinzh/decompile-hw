package com.huawei.systemmanager.spacecleanner.ui.trashlistadapter;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.ui.SpaceState;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.TrashItemGroup;
import com.huawei.systemmanager.spacecleanner.ui.trashlistadapter.GroupType.GroupTypeFactory;

public class AppCacheGroupTypeExpande extends GroupType {
    public static final GroupTypeFactory sExpandeGroupFactory = new GroupTypeFactory() {
        GroupType createHolder(View view) {
            return new AppCacheGroupTypeExpande(view);
        }

        int getLayoutResId() {
            return R.layout.spaceclean_expand_group_item_twolines_arrow_checkbox;
        }

        public int getType() {
            return 1;
        }
    };
    private final CheckBox checkbox;
    private final ImageView indicator;
    private final TextView number;
    private final TextView totalSize;

    public AppCacheGroupTypeExpande(View view) {
        super(view);
        this.title = (TextView) view.findViewById(R.id.text1);
        this.number = (TextView) view.findViewById(R.id.number);
        this.indicator = (ImageView) view.findViewById(R.id.arrow);
        this.totalSize = (TextView) view.findViewById(R.id.text2);
        this.checkbox = (CheckBox) view.findViewById(R.id.list_item_checkbox);
    }

    public int getType() {
        return 1;
    }

    public void bindView(boolean isExpanded, TrashItemGroup itemGroup, SpaceState state, OnClickListener itemClicker, Boolean canShowProgress, OnClickListener checkClicker) {
        int i;
        this.title.setText(itemGroup.getName());
        int childCount = itemGroup.getSize();
        this.number.setText(GlobalContext.getContext().getResources().getQuantityString(R.plurals.spaceclean_items, childCount, new Object[]{Integer.valueOf(childCount)}));
        if (itemGroup.getTrashType() == 131072) {
            this.checkbox.setVisibility(8);
        }
        if (itemGroup.isNoTrash()) {
            this.totalSize.setVisibility(8);
            this.checkbox.setVisibility(8);
        } else {
            this.totalSize.setVisibility(0);
            if (itemGroup.isCleaned()) {
                this.checkbox.setVisibility(8);
                this.totalSize.setText(R.string.space_cache_item_cleaned);
            } else {
                this.totalSize.setText(FileUtil.getFileSize(itemGroup.getTrashSizeCleaned(false)));
                this.checkbox.setVisibility(0);
                this.checkbox.setOnClickListener(checkClicker);
                this.checkbox.setChecked(itemGroup.isChecked());
                this.checkbox.setTag(itemGroup);
            }
        }
        ImageView imageView = this.indicator;
        if (isExpanded) {
            i = R.drawable.spacecleaner_expander_arrow_close;
        } else {
            i = R.drawable.spacecleaner_expander_arrow_open;
        }
        imageView.setImageResource(i);
    }
}
