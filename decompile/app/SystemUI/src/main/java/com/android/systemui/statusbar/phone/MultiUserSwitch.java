package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.UserManager;
import android.provider.ContactsContract.Profile;
import android.provider.ContactsContract.QuickContact;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import com.android.systemui.R;
import com.android.systemui.observer.SystemUIObserver;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.statusbar.policy.UserSwitcherController.BaseUserAdapter;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.analyze.BDReporter;

public class MultiUserSwitch extends FrameLayout implements OnClickListener {
    private boolean mKeyguardMode;
    private KeyguardUserSwitcher mKeyguardUserSwitcher;
    private QSPanel mQsPanel;
    private final int[] mTmpInt2 = new int[2];
    private BaseUserAdapter mUserListener;
    final UserManager mUserManager = UserManager.get(getContext());
    private UserSwitcherController mUserSwitcherController;

    public MultiUserSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        setOnClickListener(this);
        refreshContentDescription();
    }

    public void setQsPanel(QSPanel qsPanel) {
        this.mQsPanel = qsPanel;
        setUserSwitcherController(qsPanel.getHost().getUserSwitcherController());
    }

    public boolean hasMultipleUsers() {
        boolean z = false;
        if (isStudentMode()) {
            return false;
        }
        if ((this.mUserSwitcherController != null && !this.mUserSwitcherController.needShowUserEntry()) || this.mUserListener == null) {
            return false;
        }
        if (this.mUserListener.getCount() != 0) {
            z = true;
        }
        return z;
    }

    public void setUserSwitcherController(UserSwitcherController userSwitcherController) {
        this.mUserSwitcherController = userSwitcherController;
        registerListener();
        refreshContentDescription();
    }

    public void setKeyguardUserSwitcher(KeyguardUserSwitcher keyguardUserSwitcher) {
        this.mKeyguardUserSwitcher = keyguardUserSwitcher;
    }

    public void setKeyguardMode(boolean keyguardShowing) {
        this.mKeyguardMode = keyguardShowing;
        registerListener();
    }

    private void registerListener() {
        if (this.mUserManager.isUserSwitcherEnabled() && this.mUserListener == null) {
            UserSwitcherController controller = this.mUserSwitcherController;
            if (controller != null) {
                this.mUserListener = new BaseUserAdapter(controller) {
                    public void notifyDataSetChanged() {
                        MultiUserSwitch.this.refreshContentDescription();
                    }

                    public View getView(int position, View convertView, ViewGroup parent) {
                        return null;
                    }
                };
                refreshContentDescription();
            }
        }
    }

    public void onClick(View v) {
        if (isStudentMode()) {
            HwLog.w("MultiUserSwitch", "disable switch user in student mode!");
            return;
        }
        BDReporter.c(this.mContext, 347);
        if (this.mUserManager.isUserSwitcherEnabled()) {
            if (this.mKeyguardMode) {
                if (this.mKeyguardUserSwitcher != null) {
                    this.mKeyguardUserSwitcher.show(true);
                }
            } else if (!(this.mQsPanel == null || this.mUserSwitcherController == null)) {
                View center = getChildCount() > 0 ? getChildAt(0) : this;
                center.getLocationInWindow(this.mTmpInt2);
                int[] iArr = this.mTmpInt2;
                iArr[0] = iArr[0] + (center.getWidth() / 2);
                iArr = this.mTmpInt2;
                iArr[1] = iArr[1] + (center.getHeight() / 2);
                this.mQsPanel.showDetailAdapter(true, this.mUserSwitcherController.userDetailAdapter, this.mTmpInt2);
            }
        } else if (this.mQsPanel != null) {
            this.mQsPanel.getHost().startActivityDismissingKeyguard(QuickContact.composeQuickContactsIntent(getContext(), v, Profile.CONTENT_URI, 3, null));
        }
    }

    public void setClickable(boolean clickable) {
        super.setClickable(clickable);
        refreshContentDescription();
    }

    private void refreshContentDescription() {
        CharSequence currentUser = null;
        if (this.mUserManager.isUserSwitcherEnabled() && this.mUserSwitcherController != null) {
            currentUser = this.mUserSwitcherController.getCurrentUserName(this.mContext);
        }
        CharSequence text = null;
        if (!TextUtils.isEmpty(currentUser)) {
            text = this.mContext.getString(R.string.accessibility_quick_settings_user, new Object[]{currentUser});
        }
        if (!TextUtils.equals(getContentDescription(), text)) {
            setContentDescription(text);
        }
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(Button.class.getName());
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(Button.class.getName());
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public boolean isStudentMode() {
        return ((Boolean) SystemUIObserver.get(13)).booleanValue();
    }
}
