package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import com.android.systemui.HwSystemUIApplication;
import com.android.systemui.R;
import com.android.systemui.statusbar.HwExpandableNotificationRowHelper;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.NotificationData.Entry;
import com.android.systemui.statusbar.NotificationUserManager;
import com.android.systemui.statusbar.StatusBarIconView;
import java.util.ArrayList;

public class NotificationIconAreaController {
    private int mIconHPadding;
    private int mIconSize;
    private int mIconTint = -1;
    protected ImageView mMoreIcon;
    protected View mNotificationIconArea;
    protected IconMerger mNotificationIcons;
    private PhoneStatusBar mPhoneStatusBar;
    private final Rect mTintArea = new Rect();

    public NotificationIconAreaController(Context context, PhoneStatusBar phoneStatusBar) {
        this.mPhoneStatusBar = phoneStatusBar;
        initializeNotificationAreaViews(context);
    }

    protected View inflateIconArea(LayoutInflater inflater) {
        return inflater.inflate(R.layout.notification_icon_area, null);
    }

    protected void initializeNotificationAreaViews(Context context) {
        reloadDimens(context);
        this.mNotificationIconArea = inflateIconArea(LayoutInflater.from(context));
        this.mNotificationIcons = (IconMerger) this.mNotificationIconArea.findViewById(R.id.notificationIcons);
        this.mMoreIcon = (ImageView) this.mNotificationIconArea.findViewById(R.id.moreIcon);
        if (this.mMoreIcon != null) {
            this.mMoreIcon.setImageTintList(ColorStateList.valueOf(this.mIconTint));
        }
    }

    public void onDensityOrFontScaleChanged(Context context) {
        reloadDimens(context);
        LayoutParams params = generateIconLayoutParams();
        for (int i = 0; i < this.mNotificationIcons.getChildCount(); i++) {
            this.mNotificationIcons.getChildAt(i).setLayoutParams(params);
        }
    }

    @NonNull
    private LayoutParams generateIconLayoutParams() {
        LayoutParams params = new LayoutParams(this.mIconSize + (this.mIconHPadding * 2), this.mIconSize);
        params.gravity = 16;
        return params;
    }

    private void reloadDimens(Context context) {
        Resources res = context.getResources();
        this.mIconSize = res.getDimensionPixelSize(R.dimen.notification_icon_size);
        this.mIconHPadding = res.getDimensionPixelSize(R.dimen.notification_icon_padding);
    }

    public View getNotificationInnerAreaView() {
        return this.mNotificationIconArea;
    }

    public void setIconTint(int iconTint) {
        this.mIconTint = iconTint;
        if (this.mMoreIcon != null) {
            this.mMoreIcon.setImageTintList(ColorStateList.valueOf(this.mIconTint));
        }
        applyNotificationIconsTint();
    }

    protected boolean shouldShowNotification(Entry entry, NotificationData notificationData) {
        if (PhoneStatusBar.isTopLevelChild(entry) && entry.row.getVisibility() != 8) {
            return true;
        }
        return false;
    }

    public void updateNotificationIcons(NotificationData notificationData) {
        int i;
        LayoutParams params = generateIconLayoutParams();
        ArrayList<Entry> activeNotifications = notificationData.getActiveNotifications();
        int size = activeNotifications.size();
        ArrayList<StatusBarIconView> toShow = new ArrayList(size);
        for (i = 0; i < size; i++) {
            Entry ent = (Entry) activeNotifications.get(i);
            if (HwExpandableNotificationRowHelper.isVip(ent.notification)) {
                if (!HwExpandableNotificationRowHelper.isVipStatusBar(ent.notification)) {
                }
                if (shouldShowNotification(ent, notificationData) && !HwExpandableNotificationRowHelper.exists(toShow, ent.icon)) {
                    toShow.add(ent.icon);
                }
            } else {
                if (NotificationUserManager.getInstance(HwSystemUIApplication.getContext()).getNotificationState(ent.notification.getUserId(), ent.notification.getPackageName(), "1") == 0) {
                }
                toShow.add(ent.icon);
            }
        }
        ArrayList<View> toRemove = new ArrayList();
        for (i = 0; i < this.mNotificationIcons.getChildCount(); i++) {
            View child = this.mNotificationIcons.getChildAt(i);
            if (!toShow.contains(child)) {
                toRemove.add(child);
            }
        }
        int toRemoveCount = toRemove.size();
        for (i = 0; i < toRemoveCount; i++) {
            this.mNotificationIcons.removeView((View) toRemove.get(i));
        }
        for (i = 0; i < toShow.size(); i++) {
            View v = (View) toShow.get(i);
            if (v.getParent() == null) {
                this.mNotificationIcons.addView(v, i, params);
            }
        }
        int childCount = this.mNotificationIcons.getChildCount();
        for (i = 0; i < childCount; i++) {
            View actual = this.mNotificationIcons.getChildAt(i);
            actual.setVisibility(0);
            View expected = (StatusBarIconView) toShow.get(i);
            if (actual != expected) {
                this.mNotificationIcons.removeView(expected);
                this.mNotificationIcons.addView(expected, i);
            }
        }
        applyNotificationIconsTint();
    }

    protected void applyNotificationIconsTint() {
    }
}
