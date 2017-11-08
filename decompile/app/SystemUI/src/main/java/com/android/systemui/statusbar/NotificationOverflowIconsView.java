package com.android.systemui.statusbar;

import android.app.Notification;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.android.internal.util.NotificationColorUtil;
import com.android.systemui.R;
import com.android.systemui.statusbar.NotificationData.Entry;
import com.android.systemui.statusbar.phone.IconMerger;
import fyusion.vislib.BuildConfig;

public class NotificationOverflowIconsView extends IconMerger {
    private TextView mMoreText;
    private NotificationColorUtil mNotificationColorUtil;
    private int mTintColor;

    public NotificationOverflowIconsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        Context context = getContext();
        Resources res = getResources();
        this.mNotificationColorUtil = NotificationColorUtil.getInstance(getContext());
        this.mTintColor = context.getColor(R.color.keyguard_overflow_content_color);
        this.mIconSize = res.getDimensionPixelSize(R.dimen.keyguard_notification_icon_size);
    }

    public void setMoreText(TextView moreText) {
        this.mMoreText = moreText;
    }

    public void addNotification(Entry notification) {
        KeyguardStatusBarIconView v = new KeyguardStatusBarIconView(getContext(), BuildConfig.FLAVOR, notification.notification.getNotification());
        v.setScaleType(ScaleType.CENTER_INSIDE);
        v.setTag(notification.notification.getKey());
        addView(v, this.mIconSize, this.mIconSize);
        v.set(notification.icon.getStatusBarIcon());
        LayoutParams lp = (LayoutParams) v.getLayoutParams();
        lp.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.keyguard_notification_icon_margin));
        v.setLayoutParams(lp);
        applyColor(notification.notification.getNotification(), v);
        updateMoreText();
    }

    private void applyColor(Notification notification, StatusBarIconView view) {
        view.setColorFilter(this.mTintColor, Mode.SRC_ATOP);
    }

    private void updateMoreText() {
        this.mMoreText.setText(getResources().getString(R.string.keyguard_more_overflow_text, new Object[]{Integer.valueOf(getChildCount())}));
    }

    protected void reloadDimens() {
        Resources res = this.mContext.getResources();
        this.mIconSize = res.getDimensionPixelSize(R.dimen.keyguard_notification_icon_size);
        this.mIconHPadding = res.getDimensionPixelSize(R.dimen.keyguard_notification_icon_padding);
    }
}
