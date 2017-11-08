package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.systemui.HwSystemUIApplication;
import com.android.systemui.R;
import com.android.systemui.utils.NumberLocationPercent;
import com.android.systemui.utils.SystemUiUtil;
import java.util.Locale;

public class HybridGroupManager {
    private final Context mContext;
    private int mOverflowNumberColor;
    private ViewGroup mParent;

    public HybridGroupManager(Context ctx, ViewGroup parent) {
        this.mContext = ctx;
        this.mParent = parent;
    }

    private HybridNotificationView inflateHybridView() {
        HybridNotificationView hybrid = (HybridNotificationView) ((LayoutInflater) this.mContext.getSystemService(LayoutInflater.class)).inflate(R.layout.hybrid_notification, this.mParent, false);
        this.mParent.addView(hybrid);
        return hybrid;
    }

    private TextView inflateOverflowNumber() {
        TextView numberView = (TextView) ((LayoutInflater) this.mContext.getSystemService(LayoutInflater.class)).inflate(R.layout.hybrid_overflow_number, this.mParent, false);
        this.mParent.addView(numberView);
        updateOverFlowNumberColor(numberView);
        return numberView;
    }

    private void updateOverFlowNumberColor(TextView numberView) {
        numberView.setTextColor(this.mOverflowNumberColor);
    }

    public void setOverflowNumberColor(TextView numberView, int overflowNumberColor) {
        this.mOverflowNumberColor = overflowNumberColor;
        if (numberView != null) {
            updateOverFlowNumberColor(numberView);
        }
    }

    public HybridNotificationView bindFromNotification(HybridNotificationView reusableView, StatusBarNotification notification) {
        if (reusableView == null) {
            reusableView = inflateHybridView();
            reusableView.setVisible(false);
        }
        reusableView.bind(resolveTitle(notification), resolveText(notification));
        return reusableView;
    }

    private CharSequence resolveText(StatusBarNotification notification) {
        CharSequence contentText = notification.getNotification().extras.getCharSequence("android.text");
        if (contentText == null) {
            contentText = notification.getNotification().extras.getCharSequence("android.bigText");
        }
        if (TextUtils.isEmpty(contentText)) {
            return HwSystemUIApplication.getContext().getResources().getString(R.string.notification_default_content);
        }
        return contentText;
    }

    private CharSequence resolveTitle(StatusBarNotification notification) {
        CharSequence titleText = notification.getNotification().extras.getCharSequence("android.title");
        if (titleText == null) {
            titleText = notification.getNotification().extras.getCharSequence("android.title.big");
        }
        if (TextUtils.isEmpty(titleText)) {
            return SystemUiUtil.getAppName(notification);
        }
        return titleText;
    }

    public TextView bindOverflowNumber(TextView reusableView, int number) {
        if (reusableView == null) {
            reusableView = inflateOverflowNumber();
        }
        reusableView.setTextSize(0, (float) this.mContext.getResources().getDimensionPixelSize(17105002));
        Locale locale = this.mContext.getResources().getConfiguration().locale;
        String text = "+" + (locale == null ? NumberLocationPercent.getFormatnumberString(number) : NumberLocationPercent.getFormatnumberString(number, locale));
        if (!text.equals(reusableView.getText())) {
            reusableView.setText(text);
        }
        reusableView.setContentDescription(String.format(this.mContext.getResources().getQuantityString(R.plurals.notification_group_overflow_description, number), new Object[]{Integer.valueOf(number)}));
        return reusableView;
    }
}
