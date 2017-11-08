package com.android.contacts.hap.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.contacts.hap.CommonUtilMethods;
import com.google.android.gms.R;
import com.huawei.android.immersion.ImmersionStyle;

public class ImmersionUtils {
    private static int IMMERSION_STYLE = -1;
    private static boolean isInit = false;
    private static int mDisPlayHwNoSplitLine = -999;
    private static boolean mIsGetPrimaryColor = false;
    private static boolean mIsReflectDisPlayHwNoSplitLine = false;
    private static int mPrimaryColor = -1;

    public static void initImmersionState() {
        mDisPlayHwNoSplitLine = getDisPlayHwNoSplitLine();
        isInit = true;
    }

    public static int getDisPlayHwNoSplitLine() {
        if (!mIsReflectDisPlayHwNoSplitLine) {
            try {
                mDisPlayHwNoSplitLine = Class.forName("android.app.ActionBar").getField("DISPLAY_HW_NO_SPLIT_LINE").getInt("DISPLAY_HW_NO_SPLIT_LINE");
            } catch (IllegalAccessException e) {
                mDisPlayHwNoSplitLine = -999;
            } catch (IllegalArgumentException e2) {
                mDisPlayHwNoSplitLine = -999;
            } catch (ClassNotFoundException e3) {
                mDisPlayHwNoSplitLine = -999;
            } catch (NoSuchFieldException e4) {
                mDisPlayHwNoSplitLine = -999;
            }
            mIsReflectDisPlayHwNoSplitLine = true;
        }
        return mDisPlayHwNoSplitLine;
    }

    public static int getPrimaryColor(Context context) {
        if (context == null) {
            return -999;
        }
        mPrimaryColor = ImmersionStyle.getPrimaryColor(context);
        return mPrimaryColor;
    }

    public static int getSuggestionForgroundColorStyle(int colorBackground) {
        if (-999 == colorBackground) {
            return -999;
        }
        return ImmersionStyle.getSuggestionForgroundColorStyle(colorBackground);
    }

    public static int getImmersionStyle(Context context) {
        if (context == null) {
            return -1;
        }
        return getSuggestionForgroundColorStyle(getPrimaryColor(context));
    }

    public static int getColorDark(Context context) {
        Resources res = context.getResources();
        int idColorDark = res.getIdentifier("action_bar_title_emui_dark", "color", "androidhwext");
        if (idColorDark > 0) {
            return res.getColor(idColorDark);
        }
        return -999;
    }

    public static int getColorLight(Context context) {
        Resources res = context.getResources();
        int idColorLight = res.getIdentifier("action_bar_title_emui", "color", "androidhwext");
        if (idColorLight > 0) {
            return res.getColor(idColorLight);
        }
        return -999;
    }

    public static void setTextViewOrEditViewImmersonColorLight(Context context, View view, boolean isNeedAlpha) {
        if (view != null && context != null && !CommonUtilMethods.isLargeThemeApplied(context.getResources())) {
            int color = -999;
            if (getImmersionStyle(context) == 0) {
                color = getColorLight(context);
            } else if (getImmersionStyle(context) == 1) {
                color = getColorDark(context);
            }
            if (color != -999) {
                if (isNeedAlpha) {
                    color = getColorWithAlpha(color, 128);
                }
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(color);
                } else if (view instanceof EditText) {
                    ((EditText) view).setTextColor(color);
                }
            }
        }
    }

    public static void setImmersionImageView(Context context, View menuItem, int viewId, int resId) {
        if (menuItem != null && context != null && !CommonUtilMethods.isLargeThemeApplied(context.getResources())) {
            ImageView imageView = (ImageView) menuItem.findViewById(viewId);
            if (imageView != null) {
                setImmersionImageViewDrawable(context, imageView, resId);
            }
        }
    }

    public static void setImmersionImageViewDrawable(Context context, ImageView imageView, int resId) {
        if (imageView != null && context != null) {
            imageView.setImageDrawable(context.getResources().getDrawable(resId));
        }
    }

    public static void setImmersionMenuItemIcon(Context context, MenuItem menuItem, int resId) {
        if (menuItem != null && context != null) {
            menuItem.setIcon(context.getResources().getDrawable(resId));
        }
    }

    public static int getUserDefinedColor(Context context, int colorType) {
        int color = -1;
        if (context == null || CommonUtilMethods.isLargeThemeApplied(context.getResources())) {
            return -1;
        }
        switch (colorType) {
            case 1:
                color = context.getResources().getColor(R.color.immersion_dawdler_name_color);
                break;
            case 16:
                color = context.getResources().getColor(R.color.immersion_dawdler_number_color);
                break;
            case 256:
                color = context.getResources().getColor(R.color.immersion_dialpad_tab_color);
                break;
        }
        return color;
    }

    public static int getImmersionImageID(Context context, int lightImageID, int drakImageID) {
        if (!replaceIconCondition(context)) {
            return drakImageID;
        }
        if (getImmersionStyle(context) != 0) {
            drakImageID = lightImageID;
        }
        return drakImageID;
    }

    public static void setImmersionMommonMenu(Context context, MenuItem menuItem) {
        if (menuItem != null && replaceIconCondition(context)) {
            switch (menuItem.getItemId()) {
                case R.id.menu_scan_card:
                    setImmersionMenuItemIcon(context, menuItem, getImmersionImageID(context, R.drawable.contacts_scan_light, R.drawable.contacts_scan));
                    break;
                case R.id.menu_delete_groups_action:
                case R.id.menu_action_delete:
                case R.id.menu_delete_group:
                    setImmersionMenuItemIcon(context, menuItem, getImmersionImageID(context, R.drawable.ic_trash_normal_light, R.drawable.ic_trash_normal));
                    break;
                case R.id.menu_action_merge:
                case R.id.menu_confirm:
                    setImmersionMenuItemIcon(context, menuItem, getImmersionImageID(context, R.drawable.ic_done_normal_light, R.drawable.ic_done_normal));
                    break;
                case R.id.favorites_menu_add_sharred_members:
                case R.id.menu_group_browser_newgroup:
                case R.id.menu_add_group_members:
                case R.id.menu_add_company_members:
                case R.id.menu_nomembers_add_group_members:
                    setImmersionMenuItemIcon(context, menuItem, getImmersionImageID(context, R.drawable.ic_new_contact_light, R.drawable.ic_new_contact));
                    break;
                case R.id.favorites_menu_edit_starred_member:
                    setImmersionMenuItemIcon(context, menuItem, getImmersionImageID(context, R.drawable.contacts_ic_edit_light, R.drawable.contacts_ic_edit));
                    break;
                case R.id.menu_send_group_message:
                    setImmersionMenuItemIcon(context, menuItem, getImmersionImageID(context, R.drawable.ic_portrait_message_light, R.drawable.ic_portrait_message));
                    break;
                case R.id.menu_send_group_mail:
                    setImmersionMenuItemIcon(context, menuItem, getImmersionImageID(context, R.drawable.ic_portrait_mail_light, R.drawable.ic_portrait_mail));
                    break;
                case R.id.overflow_menu_group_detail:
                    setImmersionMenuItemIcon(context, menuItem, getImmersionImageID(context, R.drawable.ic_menu_light, R.drawable.ic_menu));
                    break;
                case R.id.menu_rename_group:
                    setImmersionMenuItemIcon(context, menuItem, getImmersionImageID(context, R.drawable.ic_rename_light, R.drawable.ic_rename));
                    break;
            }
        }
    }

    public static boolean replaceIconCondition(Context context) {
        if (context == null || CommonUtilMethods.isLargeThemeApplied(context.getResources()) || context.getResources().getConfiguration().orientation == 1) {
            return false;
        }
        return true;
    }

    public static int getControlColor(Resources res) {
        if (res != null) {
            int colorfulId = res.getIdentifier("colorful_emui", "color", "androidhwext");
            if (colorfulId != 0) {
                return res.getColor(colorfulId);
            }
        }
        return 0;
    }

    public static int getColorWithAlpha(int color, int alpha) {
        return Color.argb(alpha, (16711680 & color) >> 16, (65280 & color) >> 8, color & 255);
    }
}
