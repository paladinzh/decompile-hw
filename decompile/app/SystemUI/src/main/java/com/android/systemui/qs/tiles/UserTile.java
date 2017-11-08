package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Pair;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.DetailAdapter;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.qs.QSTile.Icon;
import com.android.systemui.qs.QSTile.State;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserInfoController.OnUserInfoChangedListener;
import com.android.systemui.statusbar.policy.UserSwitcherController;

public class UserTile extends QSTile<State> implements OnUserInfoChangedListener {
    private Pair<String, Drawable> mLastUpdate;
    private final UserInfoController mUserInfoController;
    private final UserSwitcherController mUserSwitcherController;

    public UserTile(Host host) {
        super(host);
        this.mUserSwitcherController = host.getUserSwitcherController();
        this.mUserInfoController = host.getUserInfoController();
    }

    public State newTileState() {
        return new State();
    }

    public Intent getLongClickIntent() {
        return new Intent("android.settings.USER_SETTINGS");
    }

    protected void handleClick() {
        showDetail(true);
    }

    public DetailAdapter getDetailAdapter() {
        return this.mUserSwitcherController.userDetailAdapter;
    }

    public int getMetricsCategory() {
        return 260;
    }

    public void setListening(boolean listening) {
        if (listening) {
            this.mUserInfoController.addListener(this);
        } else {
            this.mUserInfoController.remListener(this);
        }
    }

    public CharSequence getTileLabel() {
        return getState().label;
    }

    protected void handleUpdateState(State state, Object arg) {
        final Pair<String, Drawable> p = arg != null ? (Pair) arg : this.mLastUpdate;
        if (p != null) {
            state.label = (CharSequence) p.first;
            state.contentDescription = (CharSequence) p.first;
            state.icon = new Icon() {
                public Drawable getDrawable(Context context) {
                    return (Drawable) p.second;
                }
            };
        }
    }

    public void onUserInfoChanged(String name, Drawable picture) {
        this.mLastUpdate = new Pair(name, picture);
        refreshState(this.mLastUpdate);
    }
}
