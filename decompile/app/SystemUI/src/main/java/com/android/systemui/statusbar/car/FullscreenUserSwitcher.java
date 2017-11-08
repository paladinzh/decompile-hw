package com.android.systemui.statusbar.car;

import android.view.View;
import android.view.ViewStub;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.policy.UserSwitcherController;

public class FullscreenUserSwitcher {
    private View mContainer;
    private UserGridView mUserGridView = ((UserGridView) this.mContainer.findViewById(R.id.user_grid));
    private UserSwitcherController mUserSwitcherController;

    public FullscreenUserSwitcher(PhoneStatusBar statusBar, UserSwitcherController userSwitcherController, ViewStub containerStub) {
        this.mUserSwitcherController = userSwitcherController;
        this.mContainer = containerStub.inflate();
        this.mUserGridView.init(statusBar, this.mUserSwitcherController);
    }

    public void onUserSwitched(int newUserId) {
        this.mUserGridView.onUserSwitched(newUserId);
    }

    public void show() {
        this.mContainer.setVisibility(0);
    }

    public void hide() {
        this.mContainer.setVisibility(8);
    }
}
