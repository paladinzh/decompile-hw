package com.android.contacts.hap.roaming;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.sim.SimFactoryManager;
import java.util.ArrayList;

public class RoamingDialPadDirectlyManager {
    private static String mDirectlyData = "";

    public static String getDialpadRoamingNumber(Context context, String number, Intent intent, boolean isRoamingStatus, boolean isDoubleSimCard, RoamingDialPadDirectlyDataListener roamingDialPadDirectlyDataListener) {
        return getDialpadRoamingNumber(context, number, intent, isRoamingStatus, isDoubleSimCard, roamingDialPadDirectlyDataListener, SimFactoryManager.getSlotidBasedOnSubscription(SimFactoryManager.getDefaultSubscription()));
    }

    public static String getDialpadRoamingNumber(Context context, String number, Intent intent, boolean isRoamingStatus, boolean isDoubleSimCard, RoamingDialPadDirectlyDataListener roamingDialPadDirectlyDataListener, int aSimType) {
        String result = number;
        if (intent == null || context == null || TextUtils.isEmpty(number)) {
            return number;
        }
        if (number != null) {
            if (number.startsWith(IsPhoneNetworkRoamingUtils.DEFAULT_INTERNATIONAL_PHONE_PREFIX)) {
                return number;
            }
        }
        String normalizedNumber = intent.getStringExtra("EXTRA_NORMALIZED_NUMBER");
        long durationTime = intent.getLongExtra("EXTRA_DURATION", 0);
        String countryIso = intent.getStringExtra("EXTRA_CALL_LOG_COUNTRY_ISO");
        if (TextUtils.isEmpty(normalizedNumber)) {
            boolean callOutResult = false;
            if (!TextUtils.isEmpty(intent.getStringExtra("contact_display_name"))) {
                callOutResult = disposeContactsCallOutRoamingPhoneNumber(context, number, normalizedNumber, aSimType, isRoamingStatus, isDoubleSimCard, roamingDialPadDirectlyDataListener);
            } else if (!(durationTime <= 5 || TextUtils.isEmpty(countryIso) || countryIso.equalsIgnoreCase(IsPhoneNetworkRoamingUtils.getNetworkCountryIso()))) {
                result = disposeStrangerCallOutRoamingPhoneNumber(number, countryIso);
            }
            if (callOutResult) {
                return mDirectlyData;
            }
        } else if (IsPhoneNetworkRoamingUtils.isNumberMatchCurrentCountry(number, normalizedNumber, context)) {
            result = number;
        } else {
            String compResult = IsPhoneNetworkRoamingUtils.compEditPhoneNumer(number, normalizedNumber);
            if (!TextUtils.isEmpty(compResult)) {
                result = compResult;
                if (compResult.matches("^\\+(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}$") && disposeContactsCallOutRoamingPhoneNumber(context, number, normalizedNumber, aSimType, isRoamingStatus, isDoubleSimCard, roamingDialPadDirectlyDataListener)) {
                    return mDirectlyData;
                }
            } else if (!(durationTime <= 5 || TextUtils.isEmpty(countryIso) || countryIso.equalsIgnoreCase(IsPhoneNetworkRoamingUtils.getNetworkCountryIso()))) {
                result = disposeStrangerCallOutRoamingPhoneNumber(number, countryIso);
            }
        }
        return result;
    }

    private static boolean disposeContactsCallOutRoamingPhoneNumber(Context context, String data1, String data4, int aSimType, boolean isRoaming, boolean doubleSimCard, RoamingDialPadDirectlyDataListener roamingDialPadDirectlyDataListener) {
        if (!isRoaming) {
            return false;
        }
        RoamingPhoneGatherUtils roamingPhoneGatherUtils = IsPhoneNetworkRoamingUtils.getPhoneNumberRoamingPhoneGatherUtils(context, data1, data4);
        if (roamingPhoneGatherUtils == null) {
            return false;
        }
        ArrayList<RoamingPhoneItem> phoneList = roamingPhoneGatherUtils.productRoamingPhoneItem(context, aSimType, false, true);
        if (phoneList == null || phoneList.size() <= 1) {
            return false;
        }
        RoamingLearnCarrier roamingLearnCarrier = null;
        if (TextUtils.isEmpty(data4)) {
            roamingLearnCarrier = new RoamingLearnCarrier(data1, true);
        }
        if (doubleSimCard) {
            RoamingPhoneDisambiguationDialogFragment.show(((Activity) context).getFragmentManager(), phoneList, 258, roamingLearnCarrier, roamingDialPadDirectlyDataListener);
        } else {
            RoamingPhoneDisambiguationDialogFragment.show(((Activity) context).getFragmentManager(), phoneList, 256, roamingLearnCarrier, roamingDialPadDirectlyDataListener);
        }
        return true;
    }

    private static String disposeStrangerCallOutRoamingPhoneNumber(String number, String countryIso) {
        if (TextUtils.isEmpty(number) || TextUtils.isEmpty(countryIso)) {
            return number;
        }
        String result;
        String data4 = IsPhoneNetworkRoamingUtils.produectData4ByCountryISO(number, countryIso);
        if (TextUtils.isEmpty(data4)) {
            result = number;
        } else {
            result = data4;
        }
        return result;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String getRoamingDialNumber(Context context, String number, String normalizedNumber, RoamingDialPadDirectlyDataListener roamingDialPadDirectlyDataListener) {
        if (context == null || TextUtils.isEmpty(number) || !IsPhoneNetworkRoamingUtils.isPhoneNetworkRoamging() || !IsPhoneNetworkRoamingUtils.isRoamingDealWithNumber(context, number)) {
            return number;
        }
        String result;
        boolean secondSimEnabled = CommonUtilMethods.getFirstSimEnabled() ? CommonUtilMethods.getSecondSimEnabled() : false;
        if (TextUtils.isEmpty(normalizedNumber)) {
            if (disposeContactsCallOutRoamingPhoneNumber(context, number, normalizedNumber, -1, true, secondSimEnabled, roamingDialPadDirectlyDataListener)) {
                return mDirectlyData;
            }
            result = number;
        } else if (IsPhoneNetworkRoamingUtils.isNumberMatchCurrentCountry(number, normalizedNumber, context)) {
            result = number;
        } else {
            result = IsPhoneNetworkRoamingUtils.compEditPhoneNumer(number, normalizedNumber);
            if (result != null && result.matches("^\\+(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}$") && disposeContactsCallOutRoamingPhoneNumber(context, number, normalizedNumber, -1, true, secondSimEnabled, roamingDialPadDirectlyDataListener)) {
                return mDirectlyData;
            }
        }
        return result;
    }
}
