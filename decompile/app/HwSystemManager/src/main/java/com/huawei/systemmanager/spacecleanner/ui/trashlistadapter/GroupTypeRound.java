package com.huawei.systemmanager.spacecleanner.ui.trashlistadapter;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.spacecleanner.ui.SpaceState;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.TrashItemGroup;
import com.huawei.systemmanager.spacecleanner.ui.trashlistadapter.GroupType.GroupTypeFactory;

public class GroupTypeRound extends GroupType {
    public static final GroupTypeFactory sRopundGroupFactory = new GroupTypeFactory() {
        GroupType createHolder(View view) {
            return new GroupTypeRound(view);
        }

        int getLayoutResId() {
            return R.layout.spaceclean_trashlist_group_item_rounding;
        }

        int getType() {
            return 0;
        }
    };
    private ViewStub containerStubView;
    private ImageView finishedIcon;
    private View groupView;
    private ProgressBar progressBar;

    public GroupTypeRound(View view) {
        super(view);
        this.containerStubView = (ViewStub) view.findViewById(R.id.end_icon_container_stub);
        this.groupView = view;
    }

    int getType() {
        return 0;
    }

    void bindView(boolean isExpanded, TrashItemGroup itemGroup, SpaceState state, OnClickListener itemClicker, Boolean canShowProgress, OnClickListener checkClicker) {
        this.title.setText(itemGroup.getName());
        if (canShowProgress.booleanValue()) {
            if (this.containerStubView != null) {
                this.containerStubView.inflate();
                this.containerStubView = null;
                this.finishedIcon = (ImageView) this.groupView.findViewById(R.id.imageview);
                this.progressBar = (ProgressBar) this.groupView.findViewById(R.id.progress_bar);
            }
            if (itemGroup.isScanFinished()) {
                setVisibility(this.finishedIcon, 0);
                setVisibility(this.progressBar, 4);
                return;
            }
            setVisibility(this.finishedIcon, 4);
            setVisibility(this.progressBar, 0);
        }
    }

    private void setVisibility(View view, int visibleState) {
        if (view != null && visibleState != view.getVisibility()) {
            view.setVisibility(visibleState);
        }
    }
}
