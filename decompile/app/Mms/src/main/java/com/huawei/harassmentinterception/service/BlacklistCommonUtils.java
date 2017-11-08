package com.huawei.harassmentinterception.service;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.widget.Toast;
import com.android.messaging.util.OsUtil;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.harassmentinterception.service.IHarassmentInterceptionService.Stub;

public class BlacklistCommonUtils {
    public static IHarassmentInterceptionService getBlacklistService() {
        return Stub.asInterface(ServiceManager.getService("com.huawei.harassmentinterception.service.HarassmentInterceptionService"));
    }

    public static void setNumberBlocked(String phoneNumber, boolean blocked) {
        setNumberBlocked(getBlacklistService(), phoneNumber, blocked);
    }

    public static void setNumberBlocked(IHarassmentInterceptionService aService, String phoneNumber, boolean blocked) {
        if (aService == null) {
            MLog.e("BlacklistCommonUtils", "add number to blacklist in empty service");
            return;
        }
        Bundle phoneData = new Bundle();
        if (blocked) {
            try {
                phoneData.putString("BLOCK_PHONENUMBER", phoneNumber);
                aService.addPhoneNumberBlockItem(phoneData, 0, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            phoneData.putString("BLOCK_PHONENUMBER", phoneNumber);
            aService.removePhoneNumberBlockItem(phoneData, 0, 0);
        }
    }

    public static void handleNumberBlockList(IHarassmentInterceptionService aService, String phoneNumber, int menuId) {
        MLog.d("BlacklistCommonUtils", "this method is Useless");
    }

    public static boolean isNumberBlocked(String phoneNumber) {
        return checkPhoneNumberFromBlockItem(getBlacklistService(), phoneNumber);
    }

    public static boolean isBlacklistFeatureEnable() {
        return ((OsUtil.isAtLeastL() ? OsUtil.isSecondaryUser() : false) || getBlacklistService() == null) ? false : true;
    }

    public static boolean checkPhoneNumberFromBlockItem(IHarassmentInterceptionService aService, String phoneNumber) {
        Bundle phoneData = new Bundle();
        int itemBlocked = 0;
        boolean isPhoneNumbersBlackListed = true;
        phoneData.putString("CHECK_PHONENUMBER", phoneNumber);
        if (aService == null) {
            isPhoneNumbersBlackListed = false;
            try {
                MLog.w("BlacklistCommonUtils", "service is null!!!");
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (IllegalStateException e2) {
                e2.printStackTrace();
            }
        } else {
            itemBlocked = aService.checkPhoneNumberFromBlockItem(phoneData, 0);
        }
        if (itemBlocked == 1 || itemBlocked == 3) {
            return isPhoneNumbersBlackListed;
        }
        return false;
    }

    public static void toastAddOrRemoveBlacklistInfo(Context context, boolean isAddToBlacklist) {
        int resId;
        if (isAddToBlacklist) {
            resId = R.string.add_to_blacklist_success_Toast;
        } else {
            resId = R.string.remove_from_blacklist_success_Toast;
        }
        Toast.makeText(context, resId, 0).show();
    }

    public static void comfirmAddContactToBlacklist(Context context, String phoneNumber) {
        comfirmAddContactToBlacklist(context, phoneNumber, null);
    }

    public static void comfirmAddContactToBlacklist(Context context, String phoneNumber, Runnable onComplete) {
        IHarassmentInterceptionService service = getBlacklistService();
        if (!isInWhiteList(service, phoneNumber) || removePhoneNumberFromWhiteList(service, phoneNumber) == 0) {
            setNumberBlocked(service, phoneNumber, true);
            toastAddOrRemoveBlacklistInfo(context, true);
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        Toast.makeText(context, R.string.mms_add_to_blacklist_failed_Toast, 0).show();
    }

    public static boolean judgeAddBlackEntryItem(ContactList contactList) {
        if (1 != contactList.size() || !isBlacklistFeatureEnable()) {
            return false;
        }
        return !Contact.isEmailAddress(((Contact) contactList.get(0)).getNumber());
    }

    public static boolean isInWhiteList(IHarassmentInterceptionService aService, String phoneNumber) {
        boolean z = false;
        if (aService == null) {
            MLog.e("BlacklistCommonUtils", "add number to blacklist in empty service");
            return false;
        }
        Bundle phoneData = new Bundle();
        try {
            phoneData.putString("CHECK_PHONENUMBER", phoneNumber);
            int result = aService.checkPhoneNumberFromWhiteItem(phoneData, 0);
            MLog.d("BlacklistCommonUtils", "isInWhiteList result" + result);
            if (result == 0) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            MLog.e("BlacklistCommonUtils", "isInWhiteList RemoteException");
            return false;
        }
    }

    public static int removePhoneNumberFromWhiteList(IHarassmentInterceptionService aService, String phoneNumber) {
        if (aService == null) {
            MLog.e("BlacklistCommonUtils", "add number to blacklist in empty service");
            return -1;
        }
        Bundle phoneData = new Bundle();
        try {
            phoneData.putString("BLOCK_PHONENUMBER", phoneNumber);
            int result = aService.removePhoneNumberFromWhiteItem(phoneData, 0, 0);
            MLog.d("BlacklistCommonUtils", "removePhoneNumberFromWhiteList");
            return result;
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
