package com.android.contacts.calllog;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.ImageView;
import com.android.contacts.HwCustPhoneCallDetailsViews;
import com.android.contacts.PhoneCallDetailsViews;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.encryptcall.EncryptCallUtils;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.utils.NanpNumberHelper;
import com.android.contacts.util.HwCustContactFeatureUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HwCustCallLogAdapterImpl extends HwCustCallLogAdapter {
    private static final String CONTACT_FIELD_SPLIT_CHAR = ":";
    private static final String CONTACT_SPLIT_CHAR = ";";
    protected static final int DEFAULT_CALL_FEATURES_VALUE = 0;
    private static final String FDN_INFORMATION_CONFIG = "fdn_info_show_in_calllog";
    private static final boolean IS_MULTISIM_ENABLED = TelephonyManager.getDefault().isMultiSimEnabled();
    private static final boolean IS_SUPPORT_LONG_VMNUM = SystemProperties.getBoolean("ro.config.hw_support_long_vmNum", false);
    private static final String NO_FDN_NUMBER_IN_SIM_CARD = "no_fdn_number_in_sim_card";
    private static final String NO_SDN_NUMBER_IN_SIM_CARD = "no_sdn_number_in_sim_card";
    private static final String PROPERTY_CUST_LONG_VMNUM = "gsm.hw.cust.longvmnum";
    public static final String SDN_INFORMATION_CONFIG = "sdn_info_show_in_calllog";
    public static final String SDN_INFORMATION_CONFIG_SIM1 = "sdn_info_on_SIM1";
    public static final String SDN_INFORMATION_CONFIG_SIM2 = "sdn_info_on_SIM2";
    private static List<String> mFdnNumbersList = new ArrayList();
    private static Map<String, String> mFdnNumbersMap = new HashMap(2);
    private static List<String> mSdn1NumbersList = new ArrayList();
    private static Map<String, String> mSdn1NumbersMap = new HashMap(2);
    private static List<String> mSdn2NumbersList = new ArrayList();
    private static Map<String, String> mSdn2NumbersMap = new HashMap(2);
    private static List<String> mSdnNumbersList = new ArrayList();
    private static Map<String, String> mSdnNumbersMap = new HashMap(2);
    private String mFdnInformationCfg = null;
    protected boolean mPriorityShowFdn = false;
    private String mSdn1InformationCfg = null;
    private String mSdn2InformationCfg = null;
    private String mSdnInformationCfg = null;
    protected boolean mShowFdnNameInCalllog = false;
    private boolean mShowSdnNameInCalllog = false;
    private boolean mshowSdnPriority = false;

    public HwCustCallLogAdapterImpl(Context context) {
        super(context);
    }

    public void getSdnNumbers() {
        this.mShowSdnNameInCalllog = "true".equals(System.getString(this.mContext.getContentResolver(), "hw_show_sdn_name_in_calllog"));
        this.mshowSdnPriority = "true".equals(System.getString(this.mContext.getContentResolver(), "show_sdn_priority"));
        this.mShowFdnNameInCalllog = "true".equals(System.getString(this.mContext.getContentResolver(), "hw_show_fdn_name_in_calllog"));
        this.mPriorityShowFdn = "true".equals(System.getString(this.mContext.getContentResolver(), "show_fdn_priority"));
        if (this.mShowFdnNameInCalllog) {
            addFdnNumbers();
        }
        if (this.mShowSdnNameInCalllog) {
            addSdnNumbers();
        }
    }

    public String setSdnName(String number, String name) {
        if (this.mShowSdnNameInCalllog && ((!PhoneNumberUtils.isVoiceMailNumber(number) || this.mshowSdnPriority) && mSdnNumbersList != null && mSdnNumbersMap.size() > 0)) {
            String sdnNameTemp = (String) mSdnNumbersMap.get(number);
            if (sdnNameTemp != null) {
                name = sdnNameTemp;
            }
        }
        if (!this.mShowFdnNameInCalllog) {
            return name;
        }
        if ((PhoneNumberUtils.isVoiceMailNumber(number) && !this.mPriorityShowFdn) || mFdnNumbersList == null || mFdnNumbersMap.size() <= 0) {
            return name;
        }
        String fdnNameTemp = (String) mFdnNumbersMap.get(number);
        if (fdnNameTemp != null) {
            return fdnNameTemp;
        }
        return name;
    }

    public void addSdnNumbers() {
        this.mSdnInformationCfg = System.getString(this.mContext.getContentResolver(), SDN_INFORMATION_CONFIG);
        addSdnNumbers41();
    }

    public void addSdnNumbers41() {
        if (!TextUtils.isEmpty(this.mSdnInformationCfg)) {
            if (!NO_SDN_NUMBER_IN_SIM_CARD.equals(this.mSdnInformationCfg)) {
                if (mSdnNumbersList != null && mSdnNumbersList.size() > 0) {
                    mSdnNumbersList.clear();
                }
                String[] sdnContacts = this.mSdnInformationCfg.split(CONTACT_SPLIT_CHAR);
                for (String split : sdnContacts) {
                    String[] contact = split.split(CONTACT_FIELD_SPLIT_CHAR);
                    if (2 == contact.length) {
                        mSdnNumbersMap.put(contact[1], contact[0]);
                        mSdnNumbersList.add(contact[1]);
                    }
                }
            } else {
                return;
            }
        }
        if (!(TextUtils.isEmpty(this.mSdn1InformationCfg) || NO_SDN_NUMBER_IN_SIM_CARD.equals(this.mSdn1InformationCfg))) {
            if (mSdn1NumbersList != null && mSdn1NumbersList.size() > 0) {
                mSdn1NumbersList.clear();
            }
            String[] sdn1Contacts = this.mSdn1InformationCfg.split(CONTACT_SPLIT_CHAR);
            for (String split2 : sdn1Contacts) {
                String[] contact1 = split2.split(CONTACT_FIELD_SPLIT_CHAR);
                if (2 == contact1.length) {
                    mSdn1NumbersMap.put(contact1[1], contact1[0]);
                    if (mSdn1NumbersList != null) {
                        mSdn1NumbersList.add(contact1[1]);
                    }
                }
            }
        }
        if (!TextUtils.isEmpty(this.mSdn2InformationCfg) && !NO_SDN_NUMBER_IN_SIM_CARD.equals(this.mSdn2InformationCfg)) {
            if (mSdn2NumbersList != null && mSdn2NumbersList.size() > 0) {
                mSdn2NumbersList.clear();
            }
            String[] sdn2Contacts = this.mSdn2InformationCfg.split(CONTACT_SPLIT_CHAR);
            for (String split22 : sdn2Contacts) {
                String[] contact2 = split22.split(CONTACT_FIELD_SPLIT_CHAR);
                if (2 == contact2.length) {
                    mSdn2NumbersMap.put(contact2[1], contact2[0]);
                    if (mSdn2NumbersList != null) {
                        mSdn2NumbersList.add(contact2[1]);
                    }
                }
            }
        }
    }

    public void addFdnNumbers() {
        this.mFdnInformationCfg = System.getString(this.mContext.getContentResolver(), FDN_INFORMATION_CONFIG);
        addFdnNumbers41();
    }

    public void addFdnNumbers41() {
        if (!TextUtils.isEmpty(this.mFdnInformationCfg) && !NO_FDN_NUMBER_IN_SIM_CARD.equals(this.mFdnInformationCfg)) {
            if (mFdnNumbersMap != null) {
                mFdnNumbersMap.clear();
            }
            if (mFdnNumbersList != null) {
                mFdnNumbersList.clear();
            }
            String[] fdnContacts = this.mFdnInformationCfg.split(CONTACT_SPLIT_CHAR);
            for (String split : fdnContacts) {
                String[] contact = split.split(CONTACT_FIELD_SPLIT_CHAR);
                if (2 == contact.length) {
                    mFdnNumbersMap.put(contact[1], contact[0]);
                    mFdnNumbersList.add(contact[1]);
                }
            }
        }
    }

    public void updateCallLogContactInfoCache() {
        if (this.mShowFdnNameInCalllog) {
            addFdnNumbers();
        }
    }

    public boolean isSdnNumber(String number) {
        boolean isSdnNumber = false;
        if (this.mShowSdnNameInCalllog && ((!PhoneNumberUtils.isVoiceMailNumber(number) || this.mshowSdnPriority) && mSdnNumbersList != null && mSdnNumbersMap.size() > 0 && ((String) mSdnNumbersMap.get(number)) != null)) {
            isSdnNumber = true;
        }
        if (!this.mShowFdnNameInCalllog) {
            return isSdnNumber;
        }
        if ((!PhoneNumberUtils.isVoiceMailNumber(number) || this.mPriorityShowFdn) && mFdnNumbersList != null && mFdnNumbersMap.size() > 0 && ((String) mFdnNumbersMap.get(number)) != null) {
            return true;
        }
        return isSdnNumber;
    }

    public long getCallFeaturesValue(Cursor c) {
        if (!HwCustContactFeatureUtils.isSupportCallFeatureIcon()) {
            return 0;
        }
        int calltypefeatureindex = c.getColumnIndex("features");
        if (calltypefeatureindex >= 0) {
            return c.getLong(calltypefeatureindex);
        }
        return 0;
    }

    public boolean isSupportEmergencyCallDisplay() {
        return HwCustContactFeatureUtils.isSupportADCnodeFeature();
    }

    public String getCustNumberLocationDisplay(String number, String defaultGeoCode) {
        if (!HwCustContactFeatureUtils.isSupportNanpStateNameDisplay() || this.mContext == null || number.length() < 3) {
            return defaultGeoCode;
        }
        number = PhoneNumberUtils.stripSeparators(number);
        String locationDisplay;
        if (number.length() == 3) {
            locationDisplay = NanpNumberHelper.getInstance(this.mContext).getNanpStateName(number);
            if (TextUtils.isEmpty(locationDisplay)) {
                return defaultGeoCode;
            }
            return locationDisplay;
        }
        if (number.startsWith("+")) {
            number = number.substring(1);
        }
        char[] numArray = number.toCharArray();
        int pos = 0;
        int length = numArray.length;
        int i = 0;
        while (i < length && numArray[i] == '0') {
            pos++;
            i++;
        }
        number = number.substring(pos);
        int length2 = number.length();
        if (length2 == 10) {
            number = number.substring(0, 3);
        } else if (length2 != 11 || !number.startsWith(CallInterceptDetails.BRANDED_STATE)) {
            return defaultGeoCode;
        } else {
            number = number.substring(1, 4);
        }
        locationDisplay = NanpNumberHelper.getInstance(this.mContext).getNanpStateName(number);
        if (TextUtils.isEmpty(locationDisplay)) {
            return defaultGeoCode;
        }
        return locationDisplay;
    }

    public boolean isCopyTextToEditText(boolean doubleSimCardEnabled) {
        if (!EncryptCallUtils.isEncryptCallEnable()) {
            return false;
        }
        if (SimFactoryManager.isExtremeSimplicityMode()) {
            return EncryptCallUtils.isEncryptCallCard(SimFactoryManager.getDefaultSimcard());
        }
        if (doubleSimCardEnabled) {
            return true;
        }
        if (CommonUtilMethods.getFirstSimEnabled()) {
            return EncryptCallUtils.isCallCard1Encrypt();
        }
        if (CommonUtilMethods.getSecondSimEnabled()) {
            return EncryptCallUtils.isCallCard2Encrypt();
        }
        return false;
    }

    public void updateEncryptCallView(PhoneCallDetailsViews phoneCallDetailsViews, Cursor c) {
        int i = 0;
        if (EncryptCallUtils.isEncryptCallEnable(this.mContext) && phoneCallDetailsViews != null) {
            int encryptCallColumnIndex = c.getColumnIndex("encrypt_call");
            if (encryptCallColumnIndex >= 0) {
                int encryptCall = c.getInt(encryptCallColumnIndex);
                HwCustPhoneCallDetailsViews viewCust = phoneCallDetailsViews.getHwCust();
                if (viewCust != null) {
                    ImageView imageView = viewCust.getEncryptCallView();
                    if (imageView != null) {
                        if (encryptCall != 1) {
                            i = 8;
                        }
                        imageView.setVisibility(i);
                    }
                }
            }
        }
    }

    public void updateCustSetting() {
        this.mShowSdnNameInCalllog = "true".equals(System.getString(this.mContext.getContentResolver(), "hw_show_sdn_name_in_calllog"));
        this.mshowSdnPriority = "true".equals(System.getString(this.mContext.getContentResolver(), "show_sdn_priority"));
        this.mShowFdnNameInCalllog = "true".equals(System.getString(this.mContext.getContentResolver(), "hw_show_fdn_name_in_calllog"));
        this.mPriorityShowFdn = "true".equals(System.getString(this.mContext.getContentResolver(), "show_fdn_priority"));
        this.mSdnInformationCfg = System.getString(this.mContext.getContentResolver(), SDN_INFORMATION_CONFIG);
        this.mSdn1InformationCfg = System.getString(this.mContext.getContentResolver(), SDN_INFORMATION_CONFIG_SIM1);
        this.mSdn2InformationCfg = System.getString(this.mContext.getContentResolver(), SDN_INFORMATION_CONFIG_SIM2);
        this.mFdnInformationCfg = System.getString(this.mContext.getContentResolver(), FDN_INFORMATION_CONFIG);
    }

    public void getSdnNumbers41() {
        if (this.mShowFdnNameInCalllog) {
            addFdnNumbers41();
        }
        if (this.mShowSdnNameInCalllog) {
            addSdnNumbers41();
        }
    }

    public String setSdnName41(String number, String name, boolean isVoiceMail) {
        if (this.mShowSdnNameInCalllog && ((!isVoiceMail || this.mshowSdnPriority) && mSdnNumbersList != null && mSdnNumbersMap.size() > 0)) {
            String sdnNameTemp = (String) mSdnNumbersMap.get(number);
            if (sdnNameTemp != null) {
                name = sdnNameTemp;
            }
        }
        if (!this.mShowFdnNameInCalllog) {
            return name;
        }
        if ((isVoiceMail && !this.mPriorityShowFdn) || mFdnNumbersList == null || mFdnNumbersMap.size() <= 0) {
            return name;
        }
        String fdnNameTemp = (String) mFdnNumbersMap.get(number);
        if (fdnNameTemp != null) {
            return fdnNameTemp;
        }
        return name;
    }

    public String setSdnName41(String number, String name, boolean isVoiceMail, int subId) {
        if (HwCustContactFeatureUtils.isSDNNameRequired(this.mContext, subId)) {
            if (subId == 0) {
                if (this.mShowSdnNameInCalllog && ((!isVoiceMail || this.mshowSdnPriority) && mSdn1NumbersList != null && mSdn1NumbersMap.size() > 0)) {
                    String sdn1NameTemp = (String) mSdn1NumbersMap.get(number);
                    if (sdn1NameTemp != null) {
                        name = sdn1NameTemp;
                    } else {
                        name = setSdnName41(number, name, isVoiceMail);
                    }
                }
            } else if (subId != 1) {
                name = setSdnName41(number, name, isVoiceMail);
            } else if (this.mShowSdnNameInCalllog && ((!isVoiceMail || this.mshowSdnPriority) && mSdn2NumbersList != null && mSdn2NumbersMap.size() > 0)) {
                String sdn2NameTemp = (String) mSdn2NumbersMap.get(number);
                if (sdn2NameTemp != null) {
                    name = sdn2NameTemp;
                } else {
                    name = setSdnName41(number, name, isVoiceMail);
                }
            }
        }
        if (!this.mShowFdnNameInCalllog) {
            return name;
        }
        if ((isVoiceMail && !this.mPriorityShowFdn) || mFdnNumbersList == null || mFdnNumbersMap.size() <= 0) {
            return name;
        }
        String fdnNameTemp = (String) mFdnNumbersMap.get(number);
        if (fdnNameTemp != null) {
            return fdnNameTemp;
        }
        return name;
    }

    public boolean isLongVoiceMailNumber(String number) {
        boolean z = false;
        if (!IS_SUPPORT_LONG_VMNUM) {
            return false;
        }
        String vmNumberSub2 = "";
        try {
            String vmNumber;
            if (IS_MULTISIM_ENABLED) {
                vmNumber = SystemProperties.get("gsm.hw.cust.longvmnum0", "");
                vmNumberSub2 = SystemProperties.get("gsm.hw.cust.longvmnum1", "");
            } else {
                vmNumber = SystemProperties.get(PROPERTY_CUST_LONG_VMNUM, "");
            }
            if (TextUtils.isEmpty(vmNumber) && TextUtils.isEmpty(vmNumberSub2)) {
                return false;
            }
            number = PhoneNumberUtils.extractNetworkPortionAlt(number);
            if (!TextUtils.isEmpty(number)) {
                z = !number.equals(vmNumber) ? number.equals(vmNumberSub2) : true;
            }
            return z;
        } catch (SecurityException e) {
            return false;
        }
    }

    public boolean isSupportCnap() {
        return HwCustContactFeatureUtils.isCNAPFeatureSupported(this.mContext);
    }

    public String getCustomContextMenuTitle(Context context, CallLogListItemViews views, String toUse) {
        if (context == null || views == null || views.secondaryActionViewLayout == null) {
            return toUse;
        }
        String name = toUse;
        if (HwCustContactFeatureUtils.isCNAPFeatureSupported(this.mContext)) {
            IntentProvider intentProvider = (IntentProvider) views.secondaryActionViewLayout.getTag();
            if (intentProvider != null) {
                Intent intent = intentProvider.getIntent(context);
                if (intent != null) {
                    name = intent.getStringExtra("contact_display_name");
                }
            }
        }
        if (TextUtils.isEmpty(name)) {
            name = toUse;
        }
        return name;
    }

    public boolean isNameUpdateRequried(ContactInfo updatedInfo, ContactInfo callLogInfo) {
        if (updatedInfo == null || callLogInfo == null) {
            return true;
        }
        boolean isNameUpdateRequired = true;
        if (HwCustContactFeatureUtils.isCNAPFeatureSupported(this.mContext) && !TextUtils.isEmpty(callLogInfo.name) && TextUtils.isEmpty(updatedInfo.name)) {
            isNameUpdateRequired = updatedInfo.lookupUri != null;
        }
        return isNameUpdateRequired;
    }
}
