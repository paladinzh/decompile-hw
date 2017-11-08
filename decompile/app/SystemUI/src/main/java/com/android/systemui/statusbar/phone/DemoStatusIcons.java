package com.android.systemui.statusbar.phone;

import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.UserHandle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.DemoMode;
import com.android.systemui.R;
import com.android.systemui.statusbar.StatusBarIconView;

public class DemoStatusIcons extends LinearLayout implements DemoMode {
    private boolean mDemoMode;
    private final int mIconSize;
    private final LinearLayout mStatusIcons;

    public DemoStatusIcons(LinearLayout statusIcons, int iconSize) {
        super(statusIcons.getContext());
        this.mStatusIcons = statusIcons;
        this.mIconSize = iconSize;
        setLayoutParams(this.mStatusIcons.getLayoutParams());
        setOrientation(this.mStatusIcons.getOrientation());
        setGravity(16);
        ViewGroup p = (ViewGroup) this.mStatusIcons.getParent();
        p.addView(this, p.indexOfChild(this.mStatusIcons));
    }

    public void dispatchDemoCommand(String command, Bundle args) {
        if (!this.mDemoMode && command.equals("enter")) {
            this.mDemoMode = true;
            this.mStatusIcons.setVisibility(8);
            setVisibility(0);
        } else if (this.mDemoMode && command.equals("exit")) {
            this.mDemoMode = false;
            this.mStatusIcons.setVisibility(0);
            setVisibility(8);
        } else if (this.mDemoMode && command.equals("status")) {
            int iconId;
            String volume = args.getString("volume");
            if (volume != null) {
                if (volume.equals("vibrate")) {
                    iconId = R.drawable.stat_sys_ringer_vibrate;
                } else {
                    iconId = 0;
                }
                updateSlot("volume", null, iconId);
            }
            String zen = args.getString("zen");
            if (zen != null) {
                if (zen.equals("important")) {
                    iconId = R.drawable.stat_sys_zen_important;
                } else if (zen.equals("none")) {
                    iconId = R.drawable.stat_sys_zen_none;
                } else {
                    iconId = 0;
                }
                updateSlot("zen", null, iconId);
            }
            String bt = args.getString("bluetooth");
            if (bt != null) {
                if (bt.equals("disconnected")) {
                    iconId = R.drawable.stat_sys_data_bluetooth;
                } else if (bt.equals("connected")) {
                    iconId = R.drawable.stat_sys_data_bluetooth_connected;
                } else {
                    iconId = 0;
                }
                updateSlot("bluetooth", null, iconId);
            }
            String location = args.getString("location");
            if (location != null) {
                if (location.equals("show")) {
                    iconId = R.drawable.stat_sys_location;
                } else {
                    iconId = 0;
                }
                updateSlot("location", null, iconId);
            }
            String alarm = args.getString("alarm");
            if (alarm != null) {
                if (alarm.equals("show")) {
                    iconId = R.drawable.stat_sys_alarm;
                } else {
                    iconId = 0;
                }
                updateSlot("alarm_clock", null, iconId);
            }
            String tty = args.getString("tty");
            if (tty != null) {
                if (tty.equals("show")) {
                    iconId = R.drawable.stat_sys_tty_mode;
                } else {
                    iconId = 0;
                }
                updateSlot("tty", null, iconId);
            }
            String mute = args.getString("mute");
            if (mute != null) {
                if (mute.equals("show")) {
                    iconId = 17301622;
                } else {
                    iconId = 0;
                }
                updateSlot("mute", null, iconId);
            }
            String speakerphone = args.getString("speakerphone");
            if (speakerphone != null) {
                if (speakerphone.equals("show")) {
                    iconId = 17301639;
                } else {
                    iconId = 0;
                }
                updateSlot("speakerphone", null, iconId);
            }
            String cast = args.getString("cast");
            if (cast != null) {
                updateSlot("cast", null, cast.equals("show") ? R.drawable.stat_sys_cast : 0);
            }
            String hotspot = args.getString("hotspot");
            if (hotspot != null) {
                updateSlot("hotspot", null, hotspot.equals("show") ? R.drawable.stat_sys_hotspot : 0);
            }
        }
    }

    private void updateSlot(String slot, String iconPkg, int iconId) {
        if (this.mDemoMode) {
            StatusBarIconView v;
            StatusBarIcon icon;
            if (iconPkg == null) {
                iconPkg = this.mContext.getPackageName();
            }
            int removeIndex = -1;
            int i = 0;
            while (i < getChildCount()) {
                v = (StatusBarIconView) getChildAt(i);
                if (!slot.equals(v.getTag())) {
                    i++;
                } else if (iconId == 0) {
                    removeIndex = i;
                    if (iconId != 0) {
                        if (removeIndex != -1) {
                            removeViewAt(removeIndex);
                        }
                    }
                    icon = new StatusBarIcon(iconPkg, UserHandle.SYSTEM, iconId, 0, 0, "Demo");
                    v = new StatusBarIconView(getContext(), null, null);
                    v.setTag(slot);
                    v.set(icon);
                    addView(v, 0, new LayoutParams(this.mIconSize, this.mIconSize));
                    return;
                } else {
                    icon = v.getStatusBarIcon();
                    icon.icon = Icon.createWithResource(icon.icon.getResPackage(), iconId);
                    v.set(icon);
                    v.updateDrawable();
                    return;
                }
            }
            if (iconId != 0) {
                icon = new StatusBarIcon(iconPkg, UserHandle.SYSTEM, iconId, 0, 0, "Demo");
                v = new StatusBarIconView(getContext(), null, null);
                v.setTag(slot);
                v.set(icon);
                addView(v, 0, new LayoutParams(this.mIconSize, this.mIconSize));
                return;
            }
            if (removeIndex != -1) {
                removeViewAt(removeIndex);
            }
        }
    }
}
