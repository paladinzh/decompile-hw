package com.android.contacts.hap.blacklist;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.Toast;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import com.huawei.harassmentinterception.service.IHarassmentInterceptionService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class BlacklistCommonUtils {
    public static boolean checkPhoneNumberFromBlockItem(IHarassmentInterceptionService aService, ArrayList<String> aPhoneNumberList) {
        return checkNumberForBlackListLocally(getBlockListNumberHashSet(aService), (ArrayList) aPhoneNumberList);
    }

    public static boolean checkPhoneNumberFromBlockItem(IHarassmentInterceptionService aService, String aPhoneNumber) {
        return checkNumberForBlackListLocally(getBlockListNumberHashSet(aService), aPhoneNumber);
    }

    public static void handleNumberBlockList(Context aContext, IHarassmentInterceptionService aService, String aPhoneNumber, String aContactName, int aType, boolean isNeedToast) {
        Bundle phoneData = new Bundle();
        int itemBlocked = 0;
        switch (aType) {
            case 0:
                phoneData.putString("BLOCK_PHONENUMBER", aPhoneNumber);
                phoneData.putString("BLOCK_CONTACTNAME", aContactName);
                if (aService != null) {
                    try {
                        if (isInWhiteList(aService, aPhoneNumber) && removePhoneNumberFromWhiteList(aService, aPhoneNumber) != 0) {
                            Toast.makeText(aContext, R.string.contacts_remove_from_white_list_failed, 0).show();
                        }
                        itemBlocked = aService.addPhoneNumberBlockItem(phoneData, 0, 0);
                        StatisticalHelper.report(5016);
                        ExceptionCapture.reportScene(95);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                if (HwLog.HWDBG) {
                    HwLog.d("BlacklistCommonUtils", "service is null!!!");
                }
                ExceptionCapture.captureBlacklistException("BlacklistCommonUtils->handleNumberBlockList ADD_TO_BLACKLIST service is null!", null);
                HwLog.v("BlacklistCommonUtils", "addPhoneNumberBlockItem -> [Add to BlackList] itemBlocked: " + itemBlocked);
                return;
            case 1:
                phoneData.putString("BLOCK_PHONENUMBER", aPhoneNumber);
                if (aService != null) {
                    try {
                        itemBlocked = aService.removePhoneNumberBlockItem(phoneData, 0, 0);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                        return;
                    }
                } else if (HwLog.HWDBG) {
                    HwLog.d("BlacklistCommonUtils", "service is null!!!");
                    ExceptionCapture.captureBlacklistException("BlacklistCommonUtils->handleNumberBlockList REMOVE_FROM_BLACKLIST service is null!", null);
                }
                HwLog.v("BlacklistCommonUtils", "setPhoneNumberBlockList -> [Remove from BlackList] itemBlocked: " + itemBlocked);
                return;
            default:
                return;
        }
    }

    public static void handleNumberBlockList(Context aContext, IHarassmentInterceptionService aService, ArrayList<String> aPhoneNumberList, String aContactName, int aType) {
        Bundle phoneData = new Bundle();
        int itemBlocked = -1;
        switch (aType) {
            case 0:
                if (HwLog.HWDBG) {
                    HwLog.v("BlacklistCommonUtils", "setPhoneNumberBlockList -> [Add To BlackList]");
                }
                if (aPhoneNumberList != null) {
                    ArrayList<String> tempArrayList = new ArrayList();
                    for (int i = 0; i < aPhoneNumberList.size(); i++) {
                        tempArrayList.add(aContactName);
                    }
                    phoneData.putStringArrayList("BLOCK_PHONENUMBER", aPhoneNumberList);
                    phoneData.putStringArrayList("BLOCK_CONTACTNAME", tempArrayList);
                    if (aService != null) {
                        try {
                            for (String number : aPhoneNumberList) {
                                if (isInWhiteList(aService, number) && removePhoneNumberFromWhiteList(aService, number) != 0) {
                                    Toast.makeText(aContext, R.string.contacts_remove_from_white_list_failed, 0).show();
                                }
                            }
                            itemBlocked = aService.setPhoneNumberBlockList(phoneData, 0, 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    } else if (HwLog.HWDBG) {
                        HwLog.d("BlacklistCommonUtils", "service is null!!!");
                    }
                    HwLog.v("BlacklistCommonUtils", "setPhoneNumberBlockList -> [Add To BlackList] itemBlocked: " + itemBlocked);
                    return;
                }
                return;
            case 1:
                if (HwLog.HWDBG) {
                    HwLog.v("BlacklistCommonUtils", "setPhoneNumberBlockList -> [Remove from BlackList]");
                }
                for (String phoneNumber : aPhoneNumberList) {
                    phoneData.putString("BLOCK_PHONENUMBER", phoneNumber);
                    if (aService != null) {
                        try {
                            itemBlocked = aService.removePhoneNumberBlockItem(phoneData, 0, 0);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    } else if (HwLog.HWDBG) {
                        HwLog.d("BlacklistCommonUtils", "service is null!!!");
                    }
                    HwLog.v("BlacklistCommonUtils", "setPhoneNumberBlockList -> [Remove from BlackList] itemBlocked: " + itemBlocked);
                }
                return;
            default:
                return;
        }
    }

    public static boolean checkNumberForBlackListLocally(Set<String> aBlackListedNumbersSet, String aPhoneNumber) {
        if (aBlackListedNumbersSet == null || aBlackListedNumbersSet.size() == 0 || aPhoneNumber == null) {
            return false;
        }
        if (aBlackListedNumbersSet.contains(CommonUtilMethods.trimNumberForMatching(aPhoneNumber))) {
            return true;
        }
        return false;
    }

    public static boolean checkNumberForBlackListLocally(Set<String> aBlackListedNumbersSet, ArrayList<String> aPhoneNumberList) {
        if (aBlackListedNumbersSet == null || aBlackListedNumbersSet.size() == 0 || aPhoneNumberList == null || aPhoneNumberList.size() == 0) {
            return false;
        }
        for (String number : aPhoneNumberList) {
            if (!aBlackListedNumbersSet.contains(CommonUtilMethods.trimNumberForMatching(number))) {
                return false;
            }
        }
        return true;
    }

    public static Set<String> getBlockListNumberHashSet(IHarassmentInterceptionService aService) {
        String[] lBlackListedNumbers = null;
        Set<String> lBlackListedNumbersSet = null;
        if (aService != null) {
            try {
                lBlackListedNumbers = aService.queryPhoneNumberBlockItem();
            } catch (RemoteException e) {
                HwLog.w("BlacklistCommonUtils", "Exception Occured in retriving blackListedNumbers");
                e.printStackTrace();
            }
        }
        if (lBlackListedNumbers != null && lBlackListedNumbers.length > 0) {
            lBlackListedNumbersSet = new HashSet();
            for (String number : lBlackListedNumbers) {
                lBlackListedNumbersSet.add(CommonUtilMethods.trimNumberForMatching(number));
            }
        }
        return lBlackListedNumbersSet;
    }

    public static boolean isInWhiteList(IHarassmentInterceptionService aService, String phoneNumber) {
        boolean z = false;
        if (aService == null) {
            HwLog.w("BlacklistCommonUtils", "add number to blacklist in empty service");
            ExceptionCapture.captureBlacklistException("BlacklistCommonUtils->isInWhiteList add number to blacklist in empty service", null);
            return false;
        }
        Bundle phoneData = new Bundle();
        try {
            phoneData.putString("CHECK_PHONENUMBER", phoneNumber);
            int result = aService.checkPhoneNumberFromWhiteItem(phoneData, 0);
            HwLog.d("BlacklistCommonUtils", "isInWhiteList result" + result);
            if (result == 0) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int removePhoneNumberFromWhiteList(IHarassmentInterceptionService aService, String phoneNumber) {
        if (aService == null) {
            HwLog.w("BlacklistCommonUtils", "add number to blacklist in empty service");
            return -1;
        }
        Bundle phoneData = new Bundle();
        try {
            phoneData.putString("BLOCK_PHONENUMBER", phoneNumber);
            int result = aService.removePhoneNumberFromWhiteItem(phoneData, 0, 0);
            HwLog.d("BlacklistCommonUtils", "removePhoneNumberFromWhiteList result" + result);
            return result;
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
