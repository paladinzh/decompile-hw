package com.huawei.gallery.actionbar.view;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.actionbar.ActionBarStateBase;

public class ActionCustomMode extends ActionBarStateBase implements OnClickListener {
    private ActionCustomModeDelegate mDelegate;
    private View mMainView = null;

    public interface ActionCustomModeDelegate {
        View getCustomView();
    }

    public int getMode() {
        return 5;
    }

    protected void showHeadView() {
        if (this.mMainView == null && this.mDelegate != null) {
            this.mMainView = this.mDelegate.getCustomView();
            this.mMainView.setLayoutParams(new LayoutParams(-1, -1));
            this.mContainerMgr.setHeadView(this.mMainView);
        }
    }

    public void onClick(View view) {
        if (view instanceof ActionItem) {
            this.mActivity.onActionItemClicked(((ActionItem) view).getAction());
        } else {
            GalleryLog.d("ActionCustomMode", "Click on a strange view, return : " + view);
        }
    }
}
