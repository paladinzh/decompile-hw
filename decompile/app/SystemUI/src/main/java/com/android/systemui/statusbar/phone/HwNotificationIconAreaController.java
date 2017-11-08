package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.HwSystemUIApplication;
import com.android.systemui.R;
import com.android.systemui.observer.ObserverItem.OnChangeListener;
import com.android.systemui.observer.SystemUIObserver;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.NotificationData.Entry;
import com.android.systemui.tint.TintManager;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.NumberLocationPercent;
import com.android.systemui.utils.SystemUiUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class HwNotificationIconAreaController extends NotificationIconAreaController implements Observer {
    private int mNotificationCount = 0;
    private int mNotificationIconShowType = 0;
    private TextView mNotificationMore = ((TextView) this.mNotificationIconArea.findViewById(R.id.hw_notification_more));
    private TextView mNotificationNumber = ((TextView) this.mNotificationIconArea.findViewById(R.id.hw_notification_number));
    private FrameLayout mNotificationNumberArea = ((FrameLayout) this.mNotificationIconArea.findViewById(R.id.hw_notification_number_area));
    private ImageView mNotificationNumberBg = ((ImageView) this.mNotificationIconArea.findViewById(R.id.hw_notification_number_bg));
    private OnChangeListener mOnChangeListener = new OnChangeListener() {
        public void onChange(Object value) {
            HwNotificationIconAreaController.this.mNotificationIconShowType = ((Integer) SystemUIObserver.get(11)).intValue();
            HwPhoneStatusBar.getInstance().updateNotifications();
            HwNotificationIconAreaController.this.updateNumberArea(true);
        }
    };

    public HwNotificationIconAreaController(Context context, PhoneStatusBar phoneStatusBar) {
        super(context, phoneStatusBar);
        this.mNotificationMore.setText("+");
        this.mNotificationIconShowType = ((Integer) SystemUIObserver.get(11)).intValue();
        SystemUIObserver.getObserver(11).addOnChangeListener(this.mOnChangeListener);
        TintManager.getInstance().addObserver(this);
    }

    protected void applyNotificationIconsTint() {
        if (!TintManager.getInstance().isUseTint()) {
            super.applyNotificationIconsTint();
        }
    }

    public void updateNotificationIcons(NotificationData notificationData) {
        super.updateNotificationIcons(notificationData);
        switch (this.mNotificationIconShowType) {
            case 0:
                showNotificationAll();
                return;
            case 1:
                showNotificationNumber(notificationData);
                return;
            case 2:
                showNotificationNone();
                return;
            default:
                return;
        }
    }

    public void showNotificationAll() {
        HwLog.i("HwNotificationIconAreaController", "showNotificationAll");
        this.mNotificationIconArea.setVisibility(0);
        this.mNotificationNumberArea.setVisibility(8);
        for (int i = 0; i < this.mNotificationIcons.getChildCount(); i++) {
            int i2;
            View childAt = this.mNotificationIcons.getChildAt(i);
            if (i < 5) {
                i2 = 0;
            } else {
                i2 = 8;
            }
            childAt.setVisibility(i2);
        }
        this.mNotificationIcons.setVisibility(0);
        if (this.mNotificationIcons.getChildCount() > 5) {
            this.mMoreIcon.setVisibility(0);
        } else {
            this.mMoreIcon.setVisibility(8);
        }
    }

    public void showNotificationNone() {
        HwLog.i("HwNotificationIconAreaController", "showNotificationNone");
        this.mNotificationIconArea.setVisibility(4);
        this.mNotificationIcons.setVisibility(8);
        this.mMoreIcon.setVisibility(8);
        this.mNotificationNumberArea.setVisibility(8);
    }

    public void showNotificationNumber(NotificationData notificationData) {
        this.mNotificationIconArea.setVisibility(0);
        this.mMoreIcon.setVisibility(8);
        ArrayList<Entry> activeNotifications = notificationData.getActiveNotifications();
        int size = activeNotifications.size();
        int showCount = size;
        HwLog.i("HwNotificationIconAreaController", "showNotificationNumber  Number of current real display notifications : " + size);
        Map<String, String> importantNotifications = new HashMap();
        for (int i = 0; i < size; i++) {
            Entry ent = (Entry) activeNotifications.get(i);
            if (shouldShowNotification(ent, notificationData)) {
                String key = ent.notification.getPackageName() + "." + ent.notification.getGroupKey();
                if (ent.icon == null || ent.icon.getParent() == this.mNotificationIcons) {
                    if (isImportantNotification(ent) || isVoiceMailImportantNotification(ent)) {
                        if (importantNotifications.containsKey(key)) {
                            this.mNotificationIcons.removeView(ent.icon);
                        } else {
                            importantNotifications.put(key, key);
                        }
                        showCount--;
                    } else {
                        this.mNotificationIcons.removeView(ent.icon);
                    }
                }
            } else {
                showCount--;
            }
        }
        this.mNotificationIcons.setVisibility(0);
        boolean countChanged = this.mNotificationCount != showCount;
        if (showCount <= 0) {
            showCount = 0;
        }
        this.mNotificationCount = showCount;
        updateNumberArea(countChanged);
    }

    private void updateNumberArea(boolean countChanged) {
        if (this.mNotificationIconShowType == 1) {
            if (this.mNotificationCount <= 0) {
                SystemUiUtil.setViewVisibility(this.mNotificationNumberArea, 8);
            } else if (countChanged) {
                this.mNotificationNumberBg.setImageResource(getNumberBg(this.mNotificationCount));
                this.mNotificationNumber.setText(getNotificationText(this.mNotificationCount));
                SystemUiUtil.setViewVisibility(this.mNotificationNumberArea, 0);
            }
        }
    }

    private String getNumberText(int showCount) {
        if (showCount > 9) {
            return String.valueOf(9) + "+";
        }
        return String.valueOf(showCount);
    }

    private String getNotificationText(int number) {
        int count = number < 9 ? number : 9;
        Locale locale = HwSystemUIApplication.getContext().getResources().getConfiguration().locale;
        String text = locale == null ? NumberLocationPercent.getFormatnumberString(count) : NumberLocationPercent.getFormatnumberString(count, locale);
        this.mNotificationMore.setVisibility(number > 9 ? 0 : 8);
        return text;
    }

    private int getNumberBg(int showCount) {
        String number = getNumberText(showCount);
        if (TintManager.getInstance().isStatusBarBlack()) {
            switch (number.length()) {
                case 1:
                    return R.drawable.status_bar_num_bg_bright_1;
                case 2:
                case 3:
                    return R.drawable.status_bar_num_bg_bright_2;
                default:
                    return R.drawable.status_bar_num_bg_bright_1;
            }
        }
        switch (number.length()) {
            case 1:
                return R.drawable.status_bar_num_bg_dark_1;
            case 2:
            case 3:
                return R.drawable.status_bar_num_bg_dark_2;
            default:
                return R.drawable.status_bar_num_bg_dark_1;
        }
    }

    protected boolean isImportantNotification(Entry entry) {
        if (entry.notification.getNotification().extras.getString("hw_important_flag", "false").equals("true")) {
            return true;
        }
        return false;
    }

    private boolean isVoiceMailImportantNotification(Entry entry) {
        if (entry.notification.getNotification().extras.getString("hw_important_voicemail_flag", "false").equals("true")) {
            return true;
        }
        return false;
    }

    public void update(Observable observable, Object o) {
        updateNumberArea(true);
    }
}
