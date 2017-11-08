package com.android.contacts;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.View;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.util.PhoneCapabilityTester;

public class MoreContactUtils {
    private static final String WAIT_SYMBOL_AS_STRING = String.valueOf(';');

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static final boolean shouldCollapse(CharSequence mimetype1, CharSequence data1, CharSequence mimetype2, CharSequence data2) {
        if (!TextUtils.equals(mimetype1, mimetype2)) {
            return false;
        }
        if (TextUtils.equals(data1, data2)) {
            return true;
        }
        if (data1 == null || data2 == null || !TextUtils.equals("vnd.android.cursor.item/phone_v2", mimetype1)) {
            return false;
        }
        if (PhoneCapabilityTester.isEmobileCustomer()) {
            return CommonUtilMethods.normalizeNumber(String.valueOf(data1)).equals(CommonUtilMethods.normalizeNumber(String.valueOf(data2)));
        } else {
            return shouldCollapsePhoneNumbers(data1.toString(), data2.toString());
        }
    }

    private static final boolean shouldCollapsePhoneNumbers(String number1WithLetters, String number2WithLetters) {
        if (number1WithLetters.contains("#") != number2WithLetters.contains("#") || number1WithLetters.contains("*") != number2WithLetters.contains("*")) {
            return false;
        }
        String number1 = PhoneNumberUtils.convertKeypadLettersToDigits(number1WithLetters);
        String number2 = PhoneNumberUtils.convertKeypadLettersToDigits(number2WithLetters);
        int index1 = 0;
        int index2 = 0;
        while (true) {
            if (index1 >= number1.length() || PhoneNumberUtils.isNonSeparator(number1.charAt(index1))) {
                while (index2 < number2.length() && !PhoneNumberUtils.isNonSeparator(number2.charAt(index2))) {
                    index2++;
                }
                boolean number1End = index1 == number1.length();
                boolean number2End = index2 == number2.length();
                if (number1End) {
                    return number2End;
                }
                if (number2End || number1.charAt(index1) != number2.charAt(index2)) {
                    return false;
                }
                index1++;
                index2++;
            } else {
                index1++;
            }
        }
    }

    public static Rect getTargetRectFromView(Context context, View view) {
        float appScale = context.getResources().getCompatibilityInfo().applicationScale;
        int[] pos = new int[2];
        view.getLocationOnScreen(pos);
        Rect rect = new Rect();
        rect.left = Float.valueOf((((float) pos[0]) * appScale) + 0.5f).intValue();
        rect.top = Float.valueOf((((float) pos[1]) * appScale) + 0.5f).intValue();
        rect.right = Float.valueOf((((float) (pos[0] + view.getWidth())) * appScale) + 0.5f).intValue();
        rect.bottom = Float.valueOf((((float) (pos[1] + view.getHeight())) * appScale) + 0.5f).intValue();
        return rect;
    }

    public static Intent getInvitableIntent(AccountType accountType, Uri lookupUri) {
        String syncAdapterPackageName = accountType.syncAdapterPackageName;
        String className = accountType.getInviteContactActivityClassName();
        if (TextUtils.isEmpty(syncAdapterPackageName) || TextUtils.isEmpty(className)) {
            return null;
        }
        Intent intent = new Intent();
        intent.setClassName(syncAdapterPackageName, className);
        intent.setAction("com.android.contacts.action.INVITE_CONTACT");
        intent.setData(lookupUri);
        return intent;
    }
}
