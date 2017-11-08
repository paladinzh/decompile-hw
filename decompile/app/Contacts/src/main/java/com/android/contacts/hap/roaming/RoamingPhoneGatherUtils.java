package com.android.contacts.hap.roaming;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RoamingPhoneGatherUtils {
    HashMap<String, String> phoneCountry;
    List<String> phoneNumber;

    public void setPhoneNumber(List<String> phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPhoneCountry(HashMap<String, String> phoneCountry) {
        this.phoneCountry = phoneCountry;
    }

    public static boolean disposeSingleCardRoamingPhoneItem(Context context, RoamingPhoneGatherUtils roamingPhoneGatherUtils, Intent pIntent) {
        int num = roamingPhoneGatherUtils.getPhoneNum();
        if (num <= 1) {
            return false;
        }
        ArrayList<RoamingPhoneItem> list = roamingPhoneGatherUtils.productRoamingPhoneItem(context, -1, true, true);
        Bundle extras = new Bundle();
        String[] ssStrings = new String[num];
        for (int i = 0; i < ssStrings.length; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(((RoamingPhoneItem) list.get(i)).getPhoneNumber());
            sb.append(":");
            sb.append(((RoamingPhoneItem) list.get(i)).getCountry());
            ssStrings[i] = sb.toString();
        }
        extras.putInt("RoamingPhoneGatherSize", num);
        extras.putStringArray("RoamingPhoneGather", ssStrings);
        pIntent.putExtras(extras);
        return true;
    }

    public static boolean parseSingleCardRoamingPhoneItemData(Context aContext, Intent pIntent, int callType) {
        boolean result = false;
        if (pIntent != null && IsPhoneNetworkRoamingUtils.isPhoneNetworkRoamging()) {
            Bundle extras = pIntent.getExtras();
            if (extras != null) {
                if (extras.get("RoamingPhoneGatherSize") == null) {
                    return false;
                }
                if (((Integer) extras.get("RoamingPhoneGatherSize")).intValue() > 1) {
                    ArrayList<RoamingPhoneItem> phoneList = productRoamingPhoneItem(extras.getStringArray("RoamingPhoneGather"));
                    if (phoneList != null && phoneList.size() > 1) {
                        boolean mIsNullOrigNormalized = pIntent.getBooleanExtra(" pref_Original_Normalized_Number_Is_Null", false);
                        RoamingLearnCarrier roamingLearnCarrier = null;
                        if (mIsNullOrigNormalized) {
                            String number = pIntent.getStringExtra("pref_Original_Numbe");
                            if (number == null) {
                                return false;
                            }
                            roamingLearnCarrier = new RoamingLearnCarrier(number, mIsNullOrigNormalized);
                        }
                        sigleCardShowDisambiguationDialog(aContext, phoneList, callType, roamingLearnCarrier);
                        result = true;
                    }
                }
            }
        }
        return result;
    }

    public static ArrayList<RoamingPhoneItem> productRoamingPhoneItem(String[] ssStrings) {
        ArrayList<RoamingPhoneItem> list = null;
        if (ssStrings != null) {
            list = new ArrayList();
            for (String ss : ssStrings) {
                String[] roamingArrayStrings = ss.split(":");
                if (roamingArrayStrings.length == 2) {
                    RoamingPhoneItem phoneItem = new RoamingPhoneItem();
                    phoneItem.isFromDetail = true;
                    phoneItem.sendReport = true;
                    phoneItem.subScriptionId = -1;
                    phoneItem.phoneNumber = roamingArrayStrings[0];
                    phoneItem.country = roamingArrayStrings[1];
                    list.add(phoneItem);
                }
            }
        }
        return list;
    }

    public ArrayList<RoamingPhoneItem> productRoamingPhoneItem(Context context, int subscriptionId, boolean isFromDetail, boolean sendReport) {
        ArrayList<RoamingPhoneItem> list = null;
        if (!(this.phoneNumber == null || this.phoneNumber.isEmpty())) {
            int size = this.phoneNumber.size();
            String defaultString = null;
            if (this.phoneCountry == null) {
                defaultString = context.getString(R.string.roaming_dial_by_data1_number);
            }
            list = new ArrayList();
            for (int i = 0; i < size; i++) {
                String con;
                String num = (String) this.phoneNumber.get(i);
                if (this.phoneCountry != null) {
                    con = (String) this.phoneCountry.get(num);
                } else {
                    con = defaultString;
                }
                RoamingPhoneItem phoneItem = new RoamingPhoneItem();
                phoneItem.isFromDetail = isFromDetail;
                phoneItem.sendReport = sendReport;
                phoneItem.subScriptionId = subscriptionId;
                phoneItem.phoneNumber = num;
                phoneItem.country = con;
                list.add(phoneItem);
            }
        }
        return list;
    }

    public int getPhoneNum() {
        return this.phoneNumber != null ? this.phoneNumber.size() : 0;
    }

    public static void sigleCardShowDisambiguationDialog(Context context, ArrayList<RoamingPhoneItem> phoneList, int detailOrCallOut, RoamingLearnCarrier romaingLearn) {
        if (phoneList != null && !phoneList.isEmpty()) {
            RoamingPhoneDisambiguationDialogFragment.show(((Activity) context).getFragmentManager(), phoneList, detailOrCallOut, romaingLearn);
        }
    }

    public String toString() {
        return super.toString();
    }
}
