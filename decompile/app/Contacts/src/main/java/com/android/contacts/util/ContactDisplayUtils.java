package com.android.contacts.util;

import com.android.contacts.hap.EmuiFeatureManager;
import com.google.android.gms.R;

public class ContactDisplayUtils {
    private static final String TAG = ContactDisplayUtils.class.getSimpleName();
    private static boolean mIsSimpleDisplayMode = false;
    private static int mNameDisplayOrder = 1;

    public static boolean isSimpleDisplayMode() {
        return EmuiFeatureManager.isSimpleDisplayMode();
    }

    public static boolean isCustomPhoneType(Integer type) {
        return type.intValue() == 0 || type.intValue() == 19;
    }

    public static int getNameDisplayOrder() {
        return mNameDisplayOrder;
    }

    public static void setNameDisplayOrder(int nameDisplayOrder) {
        mNameDisplayOrder = nameDisplayOrder;
    }

    public static int getPhoneLabelResourceId(Integer type) {
        if (type == null) {
            return R.string.call_other;
        }
        switch (type.intValue()) {
            case 1:
                return R.string.call_home;
            case 2:
                return R.string.call_mobile;
            case 3:
                return R.string.call_work;
            case 4:
                return R.string.call_fax_work;
            case 5:
                return R.string.call_fax_home;
            case 6:
                return R.string.call_pager;
            case 7:
                return R.string.call_other;
            case 8:
                return R.string.call_callback;
            case 9:
                return R.string.call_car;
            case 10:
                return R.string.call_company_main;
            case 11:
                return R.string.call_isdn;
            case 12:
                return R.string.call_main;
            case 13:
                return R.string.call_other_fax;
            case 14:
                return R.string.call_radio;
            case 15:
                return R.string.call_telex;
            case 16:
                return R.string.call_tty_tdd;
            case 17:
                return R.string.call_work_mobile;
            case 18:
                return R.string.call_work_pager;
            case 19:
                return R.string.call_assistant;
            case 20:
                return R.string.call_mms;
            default:
                return R.string.call_custom;
        }
    }

    public static int getSmsLabelResourceId(Integer type) {
        if (type == null) {
            return R.string.sms_other;
        }
        switch (type.intValue()) {
            case 1:
                return R.string.sms_home;
            case 2:
                return R.string.sms_mobile;
            case 3:
                return R.string.sms_work;
            case 4:
                return R.string.sms_fax_work;
            case 5:
                return R.string.sms_fax_home;
            case 6:
                return R.string.sms_pager;
            case 7:
                return R.string.sms_other;
            case 8:
                return R.string.sms_callback;
            case 9:
                return R.string.sms_car;
            case 10:
                return R.string.sms_company_main;
            case 11:
                return R.string.sms_isdn;
            case 12:
                return R.string.sms_main;
            case 13:
                return R.string.sms_other_fax;
            case 14:
                return R.string.sms_radio;
            case 15:
                return R.string.sms_telex;
            case 16:
                return R.string.sms_tty_tdd;
            case 17:
                return R.string.sms_work_mobile;
            case 18:
                return R.string.sms_work_pager;
            case 19:
                return R.string.sms_assistant;
            case 20:
                return R.string.sms_mms;
            default:
                return R.string.sms_custom;
        }
    }
}
