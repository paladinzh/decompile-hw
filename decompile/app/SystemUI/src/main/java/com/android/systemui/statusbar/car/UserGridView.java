package com.android.systemui.statusbar.car;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.statusbar.UserUtil;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.statusbar.policy.UserSwitcherController.BaseUserAdapter;
import com.android.systemui.statusbar.policy.UserSwitcherController.UserRecord;

public class UserGridView extends GridView {
    private Adapter mAdapter;
    private int mPendingUserId = -10000;
    private PhoneStatusBar mStatusBar;
    private UserSwitcherController mUserSwitcherController;

    private final class Adapter extends BaseUserAdapter {
        public Adapter(UserSwitcherController controller) {
            super(controller);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = ((LayoutInflater) UserGridView.this.getContext().getSystemService("layout_inflater")).inflate(R.layout.car_fullscreen_user_pod, null);
            }
            UserRecord record = getItem(position);
            TextView nameView = (TextView) convertView.findViewById(R.id.user_name);
            if (record != null) {
                nameView.setText(getName(UserGridView.this.getContext(), record));
                convertView.setActivated(record.isCurrent);
            } else {
                nameView.setText("Unknown");
            }
            ImageView iconView = (ImageView) convertView.findViewById(R.id.user_avatar);
            if (record == null || record.picture == null) {
                iconView.setImageDrawable(getDrawable(UserGridView.this.getContext(), record));
            } else {
                iconView.setImageBitmap(record.picture);
            }
            return convertView;
        }
    }

    public UserGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(PhoneStatusBar statusBar, UserSwitcherController userSwitcherController) {
        this.mStatusBar = statusBar;
        this.mUserSwitcherController = userSwitcherController;
        this.mAdapter = new Adapter(this.mUserSwitcherController);
        setAdapter(this.mAdapter);
        setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                UserGridView.this.mPendingUserId = -10000;
                UserRecord record = UserGridView.this.mAdapter.getItem(position);
                if (record != null) {
                    if (record.isGuest || record.isAddUser) {
                        UserGridView.this.mUserSwitcherController.switchTo(record);
                        return;
                    }
                    if (record.isCurrent) {
                        UserGridView.this.showOfflineAuthUi();
                    } else {
                        UserGridView.this.mPendingUserId = record.info.id;
                        UserGridView.this.mUserSwitcherController.switchTo(record);
                    }
                }
            }
        });
        setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                UserRecord record = UserGridView.this.mAdapter.getItem(position);
                if (record == null || record.isAddUser) {
                    return false;
                }
                if (record.isGuest) {
                    if (record.isCurrent) {
                        UserGridView.this.mUserSwitcherController.switchTo(record);
                    }
                    return true;
                }
                UserUtil.deleteUserWithPrompt(UserGridView.this.getContext(), record.info.id, UserGridView.this.mUserSwitcherController);
                return true;
            }
        });
    }

    public void onUserSwitched(int newUserId) {
        if (this.mPendingUserId == newUserId) {
            post(new Runnable() {
                public void run() {
                    UserGridView.this.showOfflineAuthUi();
                }
            });
        }
        this.mPendingUserId = -10000;
    }

    private void showOfflineAuthUi() {
        this.mStatusBar.executeRunnableDismissingKeyguard(null, null, true, true, true);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (widthMode == 0) {
            setNumColumns(-1);
        } else {
            setNumColumns(Math.max(1, Math.min(getAdapter() == null ? 0 : getAdapter().getCount(), widthSize / Math.max(1, getRequestedColumnWidth()))));
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
