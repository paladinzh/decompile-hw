package com.android.contacts.dialpad;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.telephony.PhoneNumberUtils;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import com.amap.api.maps.model.WeightedLatLng;
import com.android.contacts.CallUtil;
import com.android.contacts.ContactDpiAdapter;
import com.android.contacts.calllog.CallLogFragment;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.encryptcall.EncryptCallUtils;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.sprint.calllog.HwCustDialpadCallIntercept;
import com.android.contacts.hap.utils.BackgroundGenricHandler;
import com.android.contacts.hap.utils.NanpNumberHelper;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.HwCustContactFeatureUtils;
import com.google.android.gms.R;
import com.huawei.android.provider.SettingsEx.Systemex;
import com.huawei.android.telephony.PhoneStateListenerEx;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.cspcommon.util.ViewUtil;
import java.util.regex.Pattern;

public class HwCustDialpadFragmentImpl extends HwCustDialpadFragment {
    private static final boolean CUST_EMERGENCY_NUMBER = SystemProperties.getBoolean("ro.config.hw_sdlEyNumber", false);
    private static final int HIDE_DELAY = 5000;
    private static final int HIDE_MARQUEE = 101;
    private static final String IS_ENCRYPT_SIM_ONE_RECOMMENDED = "is_encrypt_sim_one_recommended";
    private static final String IS_ENCRYPT_SIM_TWO_RECOMMENDED = "is_encrypt_sim_two_recommended";
    private static final int SHOW_MARQUEE = 100;
    private static final int SPEED_DIALER_SLOT1 = 0;
    private static final String TAG = "HwCustDialpadFragmentImpl";
    private static final int UPDATE_DIAL_BUTTON = 102;
    private static final int VIEW_PADDING = 0;
    private static final boolean mIsNeedToChangeTWEmergencyNum = SystemProperties.getBoolean("ro.config.hw_TW_emergencyNum", false);
    private static final boolean mIsRCMCertificate = SystemProperties.getBoolean("ro.config.hw_rcm_cert", false);
    private boolean isEncryptSimOneRecommended = false;
    private boolean isEncryptSimTwoRecommended = false;
    private LinearLayout mCardNameDial2;
    private LinearLayout mCardNameDial3;
    private LinearLayout mCardNameDialEncrypt2;
    private LinearLayout mCardNameDialEncrypt3;
    private Context mContext;
    private LinearLayout mDialButtonSwitcher3;
    private LinearLayout mDialButtonSwitcher4;
    private DialpadCallBack mDialerCallBack;
    private Handler mHeaderUpdaterHandler;
    private HwCustDialpadCallIntercept mHwCustDialpadCallIntercept;
    private CustPhoneStateListenerEx mPhoneState1;
    private CustPhoneStateListenerEx mPhoneState2;
    private ContentObserver mSettingsDBObserver;
    private ShowStateNameTask mShowStateNameTask;
    private ViewSwitcher mViewSwitcher;
    private ViewSwitcher mViewSwitcher2;
    private Uri mWifiCallSettingUri = Global.getUriFor("wfc_ims_enabled");
    private NanpNumberHelper nanpNumberHelper;

    private class CustPhoneStateListenerEx extends PhoneStateListenerEx {
        private boolean mIsInit;
        private ServiceState mState;

        public CustPhoneStateListenerEx(int subscription) {
            super(subscription);
            setSubscription(this, subscription);
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            this.mState = serviceState;
            this.mIsInit = true;
            Log.d(HwCustDialpadFragmentImpl.TAG, "serviceState:" + this.mState + " mSubscription:" + getSubscription(this));
            BackgroundGenricHandler.getInstance().post(new Runnable() {
                public void run() {
                    if (HwCustDialpadFragmentImpl.this.mParent != null) {
                        HwCustDialpadFragmentImpl.this.mParent.checkEmergencyEx();
                    }
                }
            });
        }

        public int getState() {
            if (this.mState != null) {
                return this.mState.getState();
            }
            return 3;
        }

        public boolean isEmergencyOnly() {
            if (this.mState != null) {
                return this.mState.isEmergencyOnly();
            }
            return false;
        }

        public boolean isNoService() {
            return getState() != 0;
        }

        public boolean isInit() {
            return this.mIsInit;
        }
    }

    public class ShowStateNameTask extends AsyncTask<Void, Void, Boolean> {
        TextView mShowDialpadLocation;
        String number;

        public ShowStateNameTask(TextView mShowDialpadLocation, String number) {
            this.mShowDialpadLocation = mShowDialpadLocation;
            this.number = number;
        }

        protected Boolean doInBackground(Void... params) {
            if (HwCustDialpadFragmentImpl.this.nanpNumberHelper == null) {
                HwCustDialpadFragmentImpl.this.nanpNumberHelper = NanpNumberHelper.getInstance(HwCustDialpadFragmentImpl.this.mContext);
            }
            this.number = PhoneNumberUtils.stripSeparators(this.number);
            if (this.number.length() == 3) {
                return Boolean.valueOf(true);
            }
            if (isCancelled()) {
                return Boolean.valueOf(false);
            }
            if (this.number.startsWith("+")) {
                this.number = this.number.substring(1);
            }
            char[] numArray = this.number.toCharArray();
            int pos = 0;
            int length = numArray.length;
            int i = 0;
            while (i < length && numArray[i] == '0') {
                pos++;
                i++;
            }
            if (isCancelled()) {
                return Boolean.valueOf(false);
            }
            this.number = this.number.substring(pos);
            int length2 = this.number.length();
            if (length2 == 10) {
                this.number = this.number.substring(0, 3);
                return Boolean.valueOf(true);
            } else if (length2 != 11 || !this.number.startsWith(CallInterceptDetails.BRANDED_STATE)) {
                return Boolean.valueOf(false);
            } else {
                this.number = this.number.substring(1, 4);
                return Boolean.valueOf(true);
            }
        }

        protected void onPostExecute(Boolean result) {
            if (!isCancelled()) {
                String nanpDisplayName = "";
                if (result.booleanValue()) {
                    nanpDisplayName = HwCustDialpadFragmentImpl.this.nanpNumberHelper.getNanpStateName(this.number);
                }
                if (TextUtils.isEmpty(nanpDisplayName)) {
                    this.mShowDialpadLocation.setText("");
                } else {
                    this.mShowDialpadLocation.setText(nanpDisplayName);
                }
            }
        }
    }

    private static class UpAnimation extends Animation {
        private static final int ANIMATION_DURATION = 500;
        private boolean isShow;
        private int mHeight;
        private TextView mTextView;
        private View mViewToAnimate;

        public UpAnimation(View v, TextView aTextView, boolean aflag, int aHeight) {
            this.mViewToAnimate = v;
            this.isShow = aflag;
            this.mHeight = aHeight;
            this.mTextView = aTextView;
            setDuration(500);
        }

        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            if (this.mViewToAnimate != null) {
                this.mViewToAnimate.setPadding(this.mViewToAnimate.getPaddingStart(), (int) ((this.isShow ? (double) interpolatedTime : WeightedLatLng.DEFAULT_INTENSITY - ((double) interpolatedTime)) * ((double) this.mHeight)), this.mViewToAnimate.getPaddingEnd(), this.mViewToAnimate.getPaddingBottom());
                if (this.isShow && WeightedLatLng.DEFAULT_INTENSITY == ((double) interpolatedTime)) {
                    this.mTextView.setText(R.string.wifi_call_only);
                }
            }
        }
    }

    public HwCustDialpadFragmentImpl(Context context, DialpadFragment parent) {
        super(context, parent);
        this.mContext = context;
        this.mHwCustDialpadCallIntercept = HwCustDialpadCallIntercept.getInstance(this.mContext);
        initialize();
    }

    public Boolean isDisableCustomService() {
        String mcc_mnc = TelephonyManager.getDefault().getSimOperator();
        boolean result = true;
        String teleConfigString = Systemex.getString(this.mContext.getContentResolver(), "telefonica_custom_service");
        if (!TextUtils.isEmpty(teleConfigString)) {
            String[] custValues = teleConfigString.trim().split(",");
            int size = custValues.length;
            int i = 0;
            while (i < size) {
                if (!TextUtils.isEmpty(mcc_mnc) && mcc_mnc.equals(custValues[i])) {
                    result = false;
                    break;
                }
                i++;
            }
        } else {
            result = false;
        }
        return Boolean.valueOf(result);
    }

    public String getPredefinedSpeedDialNumbersByMccmnc(String singlePair) {
        if (!TextUtils.isEmpty(singlePair)) {
            String myMcc_mnc = "";
            boolean isFirstSimEnabled = CommonUtilMethods.getFirstSimEnabled();
            String[] tempPair = singlePair.split(":");
            if (tempPair.length <= 1) {
                return singlePair;
            }
            if (isFirstSimEnabled) {
                myMcc_mnc = TelephonyManager.getDefault().getSimOperator(0);
            } else {
                myMcc_mnc = TelephonyManager.getDefault().getSimOperator();
            }
            for (String mccmnc : tempPair[1].split("\\|")) {
                if (mccmnc.equals(myMcc_mnc)) {
                    return tempPair[0];
                }
            }
        }
        return "";
    }

    public boolean disablePauseFunctionalityFromDialpad() {
        if (HwCustContactFeatureUtils.disablePauseFromDialpad()) {
            return true;
        }
        return false;
    }

    public void checkAndUpdatePhoneType(TextView aTextView, String phoneType, String lookUp) {
        if (aTextView == null) {
            return;
        }
        if (validPhoneType(phoneType, lookUp)) {
            aTextView.setVisibility(0);
            aTextView.setText(phoneType);
            return;
        }
        aTextView.setVisibility(8);
    }

    private boolean validPhoneType(String phoneType, String lookUp) {
        if (!HwCustContactFeatureUtils.isSupportPhoneType() || TextUtils.isEmpty(lookUp) || lookUp.contains("com.android.contacts.app") || TextUtils.isEmpty(phoneType)) {
            return false;
        }
        return true;
    }

    public Boolean isFilterText() {
        return Boolean.valueOf(SystemProperties.getBoolean("ro.config.contact_paste_filter", false));
    }

    public String getShowText(String text) {
        if (text == null) {
            return text;
        }
        String MOBILE_NUMBER_CUST = "[^0-9|#|*|+|-|(|)|,|/|N|.| |;]";
        String str = Pattern.compile("[^0-9|#|*|+|-|(|)|,|/|N|.| |;]").matcher(text).replaceAll("");
        if (TextUtils.isEmpty(str)) {
            str = null;
        }
        return str;
    }

    public void showEmergentView(TextView mEmergentDialText, String text) {
        int mEmergencyInSimLock = 0;
        try {
            mEmergencyInSimLock = Systemex.getInt(this.mContext.getContentResolver(), "hw_emergencyinsimlock", 0);
        } catch (NoExtAPIException e) {
            Log.v(TAG, "showEmergentView->NoExtAPIException!");
        }
        if (1 == mEmergencyInSimLock && mEmergentDialText != null && this.mContext.getResources().getString(R.string.no_service).equals(text)) {
            mEmergentDialText.setText(R.string.emergency_call_only);
        }
    }

    public void startListenPhoneState() {
        this.mPhoneState1 = new CustPhoneStateListenerEx(0);
        SimFactoryManager.listenPhoneState(this.mPhoneState1, 1);
        if (SimFactoryManager.isDualSim()) {
            this.mPhoneState2 = new CustPhoneStateListenerEx(1);
            SimFactoryManager.listenPhoneState(this.mPhoneState2, 1);
        }
    }

    public void stopListenPhoneState() {
        if (this.mPhoneState1 != null) {
            SimFactoryManager.listenPhoneState(this.mPhoneState1, 0);
        }
        if (this.mPhoneState2 != null) {
            SimFactoryManager.listenPhoneState(this.mPhoneState2, 0);
        }
    }

    public boolean isRCMCertificate() {
        return mIsRCMCertificate;
    }

    public String stringToShowForRCMCert(Resources res) {
        String stringToShown = "";
        if (this.mContext == null || res == null) {
            return "";
        }
        boolean isSub1Emergency;
        boolean isSub1NoService;
        if (SimFactoryManager.isDualSim()) {
            if (this.mPhoneState1 == null || !this.mPhoneState1.isInit()) {
                return "";
            }
            if (this.mPhoneState2 == null || !this.mPhoneState2.isInit()) {
                return "";
            }
            isSub1Emergency = this.mPhoneState1.isEmergencyOnly();
            boolean isSub2Emergency = this.mPhoneState2.isEmergencyOnly();
            isSub1NoService = this.mPhoneState1.isNoService();
            boolean isSub2NoService = this.mPhoneState2.isNoService();
            Log.d(TAG, "isSub1Emergency: " + isSub1Emergency + ", isSub2Emergency: " + isSub2Emergency + "; isSub1NoService: " + isSub1NoService + ", isSub2NoService: " + isSub2NoService);
            if (CommonUtilMethods.isAirplaneModeOn(this.mContext)) {
                stringToShown = res.getString(R.string.airplane_mode_on);
            } else if (isSub1Emergency && isSub2Emergency) {
                stringToShown = res.getString(R.string.emergency_call_only);
            } else if ((isSub1Emergency && isSub2NoService) || (isSub2Emergency && isSub1NoService)) {
                stringToShown = res.getString(R.string.emergency_call_only);
            } else if (isSub1NoService && isSub2NoService) {
                stringToShown = res.getString(R.string.no_service);
            }
        } else if (this.mPhoneState1 == null || !this.mPhoneState1.isInit()) {
            return "";
        } else {
            isSub1Emergency = this.mPhoneState1.isEmergencyOnly();
            isSub1NoService = 1 == this.mPhoneState1.getState();
            Log.d(TAG, "isSub1Emergency: " + isSub1Emergency + "; isSub1NoService: " + isSub1NoService);
            if (CommonUtilMethods.isAirplaneModeOn(this.mContext)) {
                stringToShown = res.getString(R.string.airplane_mode_on);
            } else if (isSub1Emergency) {
                stringToShown = res.getString(R.string.emergency_call_only);
            } else if (isSub1NoService) {
                stringToShown = res.getString(R.string.no_service);
            }
        }
        return stringToShown;
    }

    public boolean isSupportCallIntercept() {
        return HwCustContactFeatureUtils.isSupportCallInterceptFeature();
    }

    public boolean getCallInterceptIntent(String number) {
        if (this.mContext != null) {
            Intent callInterceptIntent = this.mHwCustDialpadCallIntercept.getCallInterceptIntent(number);
            if (callInterceptIntent != null) {
                Log.d(TAG, "callInterceptIntent is not null, launching the intent");
                try {
                    callInterceptIntent.setFlags(268435456);
                    this.mContext.startActivity(callInterceptIntent);
                } catch (ActivityNotFoundException e) {
                    Log.d(TAG, " Callintercept intent's detail either be wrong or not exist ");
                }
                return true;
            }
        }
        return false;
    }

    public String changeToTwEmergencyNum(String numer, int subId) {
        if (!mIsNeedToChangeTWEmergencyNum) {
            return numer;
        }
        int emrgencySlotId = CommonUtilMethods.getEmergencyNumberSimSlot(numer, SimFactoryManager.isDualSim());
        if (emrgencySlotId != -1) {
            subId = emrgencySlotId;
        }
        String num = PhoneNumberUtils.extractNetworkPortionAlt(numer);
        if (!TextUtils.isEmpty(num) && ("110".equals(num) || "119".equals(num))) {
            String mcc_mncnet = SystemProperties.get("ril.operator.numeric", "");
            String mcc_mncsim = TelephonyManager.getDefault().getSimOperator(subId);
            Object strMccMncnet = null;
            String strMcc = null;
            boolean isAirplaneModeOn = Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0;
            if (!TextUtils.isEmpty(mcc_mncnet)) {
                strMccMncnet = mcc_mncnet.substring(0, 3);
            }
            if (!TextUtils.isEmpty(mcc_mncsim)) {
                strMcc = mcc_mncsim.substring(0, 3);
            }
            if (!TextUtils.isEmpty(strMcc) && strMcc.equals("466") && isAirplaneModeOn) {
                return "112";
            }
            if (TextUtils.isEmpty(strMcc) && !TextUtils.isEmpty(r7) && r7.equals("466")) {
                return "112";
            }
        }
        return numer;
    }

    public void showStateName(TextView mShowDialpadLocation, String number) {
        if (HwCustContactFeatureUtils.isSupportNanpStateNameDisplay() && !TextUtils.isEmpty(number)) {
            if (this.mShowStateNameTask != null) {
                this.mShowStateNameTask.cancel(true);
            }
            this.mShowStateNameTask = new ShowStateNameTask(mShowDialpadLocation, number);
            this.mShowStateNameTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        }
    }

    public boolean isCustSdlEyNuber(String aNumber) {
        if (!CUST_EMERGENCY_NUMBER || !PhoneNumberUtils.isEmergencyNumber(aNumber)) {
            return false;
        }
        startSpeeddialEmergencyNumber(aNumber);
        return true;
    }

    public void startSpeeddialEmergencyNumber(String aNumber) {
        if (this.mParent != null) {
            this.mParent.startActivity(CallUtil.getCallIntent(aNumber, 0));
            StatisticalHelper.reportDialPortal(this.mParent.getActivity(), 4);
        }
    }

    public boolean predefinedHeaderNotNeeded() {
        return HwCustContactFeatureUtils.isSupportADCnodeFeature();
    }

    public boolean isSupportOtherEmergencyNetworkSignal() {
        return HwCustContactFeatureUtils.isSupportOtherEmergencyNetworkSignal();
    }

    public void repalceAdditionalButtonRowForEncryptCall(LayoutInflater inflater, ViewGroup addButtonIpCall, OnClickListener dialButtonEncrypt1Listener, OnClickListener dialButtonEncrypt2Listener) {
        if (EncryptCallUtils.isEncryptCallEnable()) {
            View dialpadAdditionalButtonsWithIpCall = addButtonIpCall.findViewById(R.id.dialpadAdditionalButtonsWithIpCall);
            View replaceView = inflater.inflate(R.layout.dialpad_additional_buttions_ip_encrypt, null);
            for (int index = 0; index < addButtonIpCall.getChildCount(); index++) {
                if (addButtonIpCall.getChildAt(index).equals(dialpadAdditionalButtonsWithIpCall)) {
                    LayoutParams lp = dialpadAdditionalButtonsWithIpCall.getLayoutParams();
                    addButtonIpCall.removeViewAt(index);
                    addButtonIpCall.addView(replaceView, index, lp);
                    break;
                }
            }
            this.mCardNameDial2 = (LinearLayout) addButtonIpCall.findViewById(R.id.nameDialButton2);
            this.mCardNameDial3 = (LinearLayout) addButtonIpCall.findViewById(R.id.nameDialButton3);
            this.mCardNameDialEncrypt2 = (LinearLayout) addButtonIpCall.findViewById(R.id.nameDialButtonEncrypt2);
            this.mCardNameDialEncrypt3 = (LinearLayout) addButtonIpCall.findViewById(R.id.nameDialButtonEncrypt3);
            this.mCardNameDialEncrypt2.setBackgroundResource(R.drawable.btn_call);
            this.mCardNameDialEncrypt3.setBackgroundResource(R.drawable.btn_call);
            this.mCardNameDialEncrypt2.setOnClickListener(dialButtonEncrypt1Listener);
            this.mCardNameDialEncrypt3.setOnClickListener(dialButtonEncrypt2Listener);
        }
    }

    public void hideEncryptCallButton() {
        if (EncryptCallUtils.isEncryptCallEnable()) {
            this.mCardNameDialEncrypt2.setVisibility(8);
            this.mCardNameDialEncrypt3.setVisibility(8);
        }
    }

    private void setEncryptCallBtnStatus(boolean isSim1Enabled, boolean isSim2Enabled, boolean isInLeftOrRight, boolean isLandscapt) {
        int i;
        int i2 = 8;
        this.mCardNameDialEncrypt2.setEnabled(isSim1Enabled);
        this.mCardNameDialEncrypt3.setEnabled(isSim2Enabled);
        this.mCardNameDialEncrypt2.setClickable(isSim1Enabled);
        this.mCardNameDialEncrypt3.setClickable(isSim2Enabled);
        boolean isNotShowEncrypt = (isSim1Enabled && isSim2Enabled && isInLeftOrRight) ? !isLandscapt : false;
        View findViewById = this.mCardNameDialEncrypt2.findViewById(R.id.button_text);
        if (isNotShowEncrypt) {
            i = 8;
        } else {
            i = 0;
        }
        findViewById.setVisibility(i);
        findViewById = this.mCardNameDialEncrypt3.findViewById(R.id.button_text);
        if (isNotShowEncrypt) {
            i = 8;
        } else {
            i = 0;
        }
        findViewById.setVisibility(i);
        boolean isNotShowBtnTxt = (isSim1Enabled && isSim2Enabled && isInLeftOrRight) ? !isLandscapt : false;
        findViewById = this.mCardNameDial2.findViewById(R.id.button_text);
        if (isNotShowBtnTxt) {
            i = 8;
        } else {
            i = 0;
        }
        findViewById.setVisibility(i);
        View findViewById2 = this.mCardNameDial3.findViewById(R.id.button_text);
        if (!isNotShowBtnTxt) {
            i2 = 0;
        }
        findViewById2.setVisibility(i2);
        setDialBtnTextAndImage(this.mCardNameDialEncrypt2, this.mContext.getString(R.string.encrypt_call), R.drawable.encrypt_dial_button_dual_sim_icon_1, false);
        setDialBtnTextAndImage(this.mCardNameDialEncrypt3, this.mContext.getString(R.string.encrypt_call), R.drawable.encrypt_dial_button_dual_sim_icon_2, false);
    }

    private void setDialBtnTextAndImage(LinearLayout cardNameDial, String cardName, int imageId, boolean isViewSwitcher) {
        if (cardNameDial != null) {
            TextView textView;
            ImageView imageView;
            if (isViewSwitcher) {
                textView = (TextView) cardNameDial.findViewById(R.id.button_text_switcher);
                imageView = (ImageView) cardNameDial.findViewById(R.id.button_image_switcher);
            } else {
                textView = (TextView) cardNameDial.findViewById(R.id.button_text);
                imageView = (ImageView) cardNameDial.findViewById(R.id.button_image);
            }
            textView.setText(cardName);
            imageView.setImageResource(imageId);
            ViewUtil.setStateListIcon(cardNameDial.getContext(), imageView);
        }
    }

    public void setButtonsLayoutForOneSimWithEncryptCall(boolean isSim1Enabled, boolean isSim2Enabled, LinearLayout mCardNameDial, int which, boolean isLandscapt, boolean isInLeftOrRightState) {
        if (EncryptCallUtils.isEncryptCallEnable()) {
            int btnHeight;
            int btnWidth;
            setEncryptCallBtnStatus(isSim1Enabled, isSim2Enabled, isInLeftOrRightState, isLandscapt);
            if (which == 0) {
                this.mCardNameDialEncrypt2.setVisibility(0);
                this.mCardNameDialEncrypt3.setVisibility(8);
            } else if (which == 1) {
                this.mCardNameDialEncrypt2.setVisibility(8);
                this.mCardNameDialEncrypt3.setVisibility(0);
            }
            this.mParent.setSearchBtnsLayout(true);
            setCardNameDialMarginEnd(getDimenPixelSize(R.dimen.dialpad_additional_buttions_one_marginend_encrypt));
            if (isLandscapt) {
                btnHeight = getDimenPixelSize(R.dimen.contact_dialpad_dial_button_height);
                btnWidth = getDimenPixelSize(R.dimen.contact_dialpad_dial_button_landscape_width_2sim);
            } else {
                if (isInLeftOrRightState) {
                    btnWidth = getDimenPixelSize(R.dimen.contact_dialpad_dial_button_single_width_2sim);
                } else {
                    btnWidth = getDimenPixelSize(R.dimen.contact_dialpad_dial_button_width_2sim);
                }
                btnHeight = getDimenPixelSize(R.dimen.contact_dialpad_dial_button_height);
            }
            if (which == 0) {
                adjustDialBtnSize(this.mCardNameDialEncrypt2, btnWidth, btnHeight, false);
            } else if (which == 1) {
                adjustDialBtnSize(this.mCardNameDialEncrypt3, btnWidth, btnHeight, false);
            }
            adjustDialBtnSize(mCardNameDial, btnWidth, btnHeight, false);
        }
    }

    private void adjustDialBtnSize(LinearLayout linearLayout, int width, int height, boolean isViewSwitcher) {
        if (linearLayout != null) {
            LayoutParams params = linearLayout.getLayoutParams();
            params.width = width;
            params.height = height;
            linearLayout.setLayoutParams(params);
            this.mParent.adjustNameViewWidth(this.mContext, linearLayout, width, isViewSwitcher);
        }
    }

    private void setCardNameDialMarginEnd(int marginEnd) {
        if (this.mCardNameDial2 != null) {
            LayoutParams lp = this.mCardNameDial2.getLayoutParams();
            if (lp instanceof LinearLayout.LayoutParams) {
                ((LinearLayout.LayoutParams) lp).setMarginEnd(marginEnd);
            }
        }
    }

    public void setButtonsLayoutForBothSimWithEncryptCall(boolean isSim1Enabled, boolean isSim2Enabled, LinearLayout mCardNameDial2, LinearLayout mCardNameDial3, boolean isLandscape, boolean isInLeftOrRightState) {
        if (EncryptCallUtils.isEncryptCallEnable()) {
            int btnHeight;
            int btnWidth;
            setEncryptCallBtnStatus(isSim1Enabled, isSim2Enabled, isInLeftOrRightState, isLandscape);
            int which = 0;
            if (EncryptCallUtils.isCallCard1Encrypt()) {
                which = 0;
                this.mCardNameDialEncrypt2.setVisibility(0);
                this.mCardNameDialEncrypt3.setVisibility(8);
            } else if (EncryptCallUtils.isCallCard2Encrypt()) {
                which = 1;
                this.mCardNameDialEncrypt3.setVisibility(0);
                this.mCardNameDialEncrypt2.setVisibility(8);
            }
            this.mParent.setSearchBtnsLayout(true, true);
            setCardNameDialMarginEnd(getDimenPixelSize(R.dimen.dialpad_additional_buttions_one_marginend_encrypt));
            if (isLandscape) {
                btnHeight = getDimenPixelSize(R.dimen.contact_dialpad_dial_button_height);
                btnWidth = getDimenPixelSize(R.dimen.contact_dialpad_dial_button_landscape_width_2sim_with_encrypt);
            } else {
                if (isInLeftOrRightState) {
                    btnWidth = getDimenPixelSize(R.dimen.contact_dialpad_dial_button_single_width_2sim_with_encrypt);
                } else {
                    btnWidth = getDimenPixelSize(R.dimen.contact_dialpad_dial_button_width_2sim_with_encrypt);
                }
                btnHeight = getDimenPixelSize(R.dimen.contact_dialpad_dial_button_height);
            }
            adjustDialBtnSize(mCardNameDial2, btnWidth, btnHeight, false);
            adjustDialBtnSize(mCardNameDial3, btnWidth, btnHeight, false);
            if (which == 0) {
                adjustDialBtnSize(this.mCardNameDialEncrypt2, btnWidth, btnHeight, false);
            } else if (which == 1) {
                adjustDialBtnSize(this.mCardNameDialEncrypt3, btnWidth, btnHeight, false);
            }
        }
    }

    private int getDimenPixelSize(int resourceID) {
        return this.mContext.getResources().getDimensionPixelSize(resourceID);
    }

    public int updateSearchBtnsPaddingStart(int paddingStart) {
        if (EncryptCallUtils.isEncryptCallEnable()) {
            return getDimenPixelSize(R.dimen.contact_dialpad_btn_search_padding_start_2sim_encrypt);
        }
        return paddingStart;
    }

    public int updateSearchBtnsPaddingEnd(int paddingEnd) {
        if (EncryptCallUtils.isEncryptCallEnable()) {
            return getDimenPixelSize(R.dimen.contact_dialpad_btn_search_padding_end_2sim_encrypt);
        }
        return paddingEnd;
    }

    public void initExtremeSimplicityMode(ViewSwitcher aViewSwitcher, LayoutInflater inflater, LinearLayout addDialBtnSwitcherLayout) {
        if (EncryptCallUtils.isEncryptCallEnable()) {
            LinearLayout.LayoutParams dialButtonParams = new LinearLayout.LayoutParams(getDimenPixelSize(R.dimen.contact_dialpad_dial_button_width), getDimenPixelSize(R.dimen.contact_dialpad_dial_button_height));
            this.mDialButtonSwitcher3 = (LinearLayout) inflater.inflate(R.layout.dialpad_additonal_dial_button_switcher, null);
            this.mDialButtonSwitcher4 = (LinearLayout) inflater.inflate(R.layout.dialpad_additonal_dial_button_switcher, null);
            this.mDialButtonSwitcher3.setPadding(0, 0, 0, 0);
            this.mDialButtonSwitcher4.setPadding(0, 0, 0, 0);
            this.mDialButtonSwitcher3.setLayoutParams(dialButtonParams);
            this.mDialButtonSwitcher4.setLayoutParams(dialButtonParams);
            LinearLayout.LayoutParams dialButtonSwitcher2 = new LinearLayout.LayoutParams(-2, -2);
            dialButtonSwitcher2.setMarginStart(getDimenPixelSize(R.dimen.dialpad_additional_buttions_one_marginend));
            this.mViewSwitcher2 = new DialpadViewSwitcher(this.mContext);
            this.mViewSwitcher2.setLayoutParams(dialButtonSwitcher2);
            this.mViewSwitcher2.addView(this.mDialButtonSwitcher3);
            this.mViewSwitcher2.addView(this.mDialButtonSwitcher4);
            this.mViewSwitcher2.setBackgroundResource(R.drawable.btn_call);
            this.mViewSwitcher2.setVisibility(8);
            this.mViewSwitcher = aViewSwitcher;
            addDialBtnSwitcherLayout.removeAllViews();
            addDialBtnSwitcherLayout.setOrientation(0);
            addDialBtnSwitcherLayout.addView(aViewSwitcher);
            addDialBtnSwitcherLayout.addView(this.mViewSwitcher2);
        }
    }

    public void setSwitcherBtnState(OnClickListener dialBtnEncryptListener, OnTouchListener switcherTouchListener, OnLongClickListener switcherLongClickListener, boolean isLandscape, boolean isInLeftOrRightState, boolean isSim1Enabled, boolean isSim2Enabled) {
        if (this.mViewSwitcher2 != null) {
            int extremeSimpleDefaultSimcard = SimFactoryManager.getDefaultSimcard();
            this.mViewSwitcher2.setEnabled(true);
            this.mViewSwitcher2.setLongClickable(true);
            this.mViewSwitcher2.setOnTouchListener(switcherTouchListener);
            this.mViewSwitcher2.setOnLongClickListener(switcherLongClickListener);
            if (extremeSimpleDefaultSimcard != this.mViewSwitcher.getDisplayedChild()) {
                this.mViewSwitcher2.setDisplayedChild(extremeSimpleDefaultSimcard);
            }
            this.mDialButtonSwitcher3.setEnabled(isSim1Enabled);
            this.mDialButtonSwitcher4.setEnabled(isSim2Enabled);
            setSwitcherBtnSlidingState(dialBtnEncryptListener, isLandscape, isInLeftOrRightState);
        }
    }

    public void setSwitcherBtnLayout(boolean isSim1Enabled, boolean isSim2Enabled, LinearLayout card1ButtonLayout, LinearLayout card2ButtonLayout, boolean isLandscape, boolean isInLeftOrRightState) {
        if (EncryptCallUtils.isEncryptCallEnable()) {
            setDialBtnTextAndImage(this.mDialButtonSwitcher3, this.mContext.getString(R.string.encrypt_call), R.drawable.encrypt_dial_button_dual_sim_icon_1, true);
            setDialBtnTextAndImage(this.mDialButtonSwitcher4, this.mContext.getString(R.string.encrypt_call), R.drawable.encrypt_dial_button_dual_sim_icon_2, true);
            this.mParent.setSearchBtnsLayout(true, true);
            int btnWidth = switcherBtnWidthCalculate(EncryptCallUtils.isEncryptCallCard(this.mViewSwitcher.getDisplayedChild()), isLandscape, isInLeftOrRightState);
            int btnHeight = getDimenPixelSize(R.dimen.contact_dialpad_dial_button_height);
            adjustDialBtnSize(card1ButtonLayout, btnWidth, btnHeight, true);
            adjustDialBtnSize(card2ButtonLayout, btnWidth, btnHeight, true);
            adjustDialBtnSize(this.mDialButtonSwitcher3, btnWidth, btnHeight, true);
            adjustDialBtnSize(this.mDialButtonSwitcher4, btnWidth, btnHeight, true);
        }
    }

    public void setViewSwitcherPressed() {
        if (this.mViewSwitcher2 != null) {
            this.mViewSwitcher2.setPressed(false);
        }
    }

    public void showSwitcherNext(Animation InAnimation, Animation OutAnimation, OnClickListener dialBtnEncryptListener, boolean isLandscape, boolean isInLeftOrRightState) {
        if (this.mViewSwitcher2 != null) {
            this.mViewSwitcher2.setInAnimation(InAnimation);
            this.mViewSwitcher2.setOutAnimation(OutAnimation);
            this.mViewSwitcher2.showNext();
            setSwitcherBtnSlidingState(dialBtnEncryptListener, isLandscape, isInLeftOrRightState);
        }
    }

    public void showSwitcherPrevious(Animation InAnimation, Animation OutAnimation, OnClickListener dialBtnEncryptListener, boolean isLandscape, boolean isInLeftOrRightState) {
        if (this.mViewSwitcher2 != null) {
            this.mViewSwitcher2.setInAnimation(InAnimation);
            this.mViewSwitcher2.setOutAnimation(OutAnimation);
            this.mViewSwitcher2.showPrevious();
            setSwitcherBtnSlidingState(dialBtnEncryptListener, isLandscape, isInLeftOrRightState);
        }
    }

    private void setSwitcherBtnSlidingState(OnClickListener dialBtnEncryptListener, boolean isLandscape, boolean isInLeftOrRightState) {
        boolean isTwoBtnShow = EncryptCallUtils.isEncryptCallCard(this.mViewSwitcher.getDisplayedChild());
        int btnWidth = switcherBtnWidthCalculate(isTwoBtnShow, isLandscape, isInLeftOrRightState);
        int btnHeight = getDimenPixelSize(R.dimen.contact_dialpad_dial_button_height);
        this.mViewSwitcher.setLayoutParams(new LinearLayout.LayoutParams(btnWidth, btnHeight));
        if (isTwoBtnShow) {
            LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(btnWidth, btnHeight);
            params2.setMarginStart(getDimenPixelSize(R.dimen.dialpad_additional_buttions_one_marginend));
            this.mViewSwitcher2.setLayoutParams(params2);
            this.mViewSwitcher2.setOnClickListener(dialBtnEncryptListener);
        }
        this.mViewSwitcher2.setVisibility(isTwoBtnShow ? 0 : 8);
        adjustDialBtnSize((LinearLayout) this.mViewSwitcher.getCurrentView(), btnWidth, btnHeight, true);
        if (isTwoBtnShow) {
            adjustDialBtnSize((LinearLayout) this.mViewSwitcher2.getCurrentView(), btnWidth, btnHeight, true);
        }
    }

    public void hideSwitchButton() {
        if (this.mViewSwitcher2 != null) {
            this.mViewSwitcher2.setVisibility(8);
        }
    }

    private int switcherBtnWidthCalculate(boolean showTwoBtn, boolean isLandscape, boolean isInLeftOrRightState) {
        if (isLandscape) {
            if (showTwoBtn) {
                return getDimenPixelSize(R.dimen.contact_dialpad_dial_button_landscape_width_2sim);
            }
            return getDimenPixelSize(R.dimen.contact_dialpad_dial_button_width_landscape);
        } else if (isInLeftOrRightState) {
            if (showTwoBtn) {
                return getDimenPixelSize(R.dimen.contact_dialpad_dial_button_single_width_2sim);
            }
            return ContactDpiAdapter.getNewPxDpi(R.dimen.contact_dialpad_dial_button_width, this.mContext);
        } else if (showTwoBtn) {
            return getDimenPixelSize(R.dimen.contact_dialpad_dial_button_width_2sim);
        } else {
            return ContactDpiAdapter.getNewPxDpi(R.dimen.contact_dialpad_dial_button_width, this.mContext);
        }
    }

    public void setEncryptBtnBgNormal() {
        if (EncryptCallUtils.isEncryptCallEnable()) {
            if (!(this.mCardNameDialEncrypt2 == null || this.mCardNameDialEncrypt3 == null)) {
                this.mCardNameDialEncrypt2.setBackgroundResource(R.drawable.btn_call);
                this.mCardNameDialEncrypt3.setBackgroundResource(R.drawable.btn_call);
            }
            if (SimFactoryManager.isExtremeSimplicityMode()) {
                if (!(this.mDialButtonSwitcher3 == null || this.mDialButtonSwitcher4 == null)) {
                    this.mDialButtonSwitcher3.setBackgroundResource(R.drawable.btn_call);
                    this.mDialButtonSwitcher4.setBackgroundResource(R.drawable.btn_call);
                }
                if (this.mViewSwitcher != null) {
                    this.mViewSwitcher.getCurrentView().setBackgroundResource(R.drawable.btn_call);
                }
            }
            this.isEncryptSimOneRecommended = false;
            this.isEncryptSimTwoRecommended = false;
        }
    }

    public void setEncryptBtnBgNormal(int slotId) {
        if (EncryptCallUtils.isEncryptCallEnable() && slotId != 2 && slotId != 3) {
            if (!(this.mCardNameDialEncrypt2 == null || this.mCardNameDialEncrypt3 == null)) {
                this.mCardNameDialEncrypt2.setBackgroundResource(R.drawable.btn_call);
                this.mCardNameDialEncrypt3.setBackgroundResource(R.drawable.btn_call);
            }
            if (SimFactoryManager.isExtremeSimplicityMode()) {
                if (!(this.mDialButtonSwitcher3 == null || this.mDialButtonSwitcher4 == null)) {
                    this.mDialButtonSwitcher3.setBackgroundResource(R.drawable.btn_call);
                    this.mDialButtonSwitcher4.setBackgroundResource(R.drawable.btn_call);
                }
                if (this.mViewSwitcher != null) {
                    this.mViewSwitcher.getCurrentView().setBackgroundResource(R.drawable.btn_call);
                }
            }
            this.isEncryptSimOneRecommended = false;
            this.isEncryptSimTwoRecommended = false;
        }
    }

    public void getEncryptButtonRecommended(Bundle savedState) {
        if (EncryptCallUtils.isEncryptCallEnable()) {
            this.isEncryptSimOneRecommended = savedState.getBoolean(IS_ENCRYPT_SIM_ONE_RECOMMENDED);
            this.isEncryptSimTwoRecommended = savedState.getBoolean(IS_ENCRYPT_SIM_TWO_RECOMMENDED);
        }
    }

    public void putEncryptButtonRecommended(Bundle outState) {
        if (EncryptCallUtils.isEncryptCallEnable()) {
            outState.putBoolean(IS_ENCRYPT_SIM_ONE_RECOMMENDED, this.isEncryptSimOneRecommended);
            outState.putBoolean(IS_ENCRYPT_SIM_TWO_RECOMMENDED, this.isEncryptSimTwoRecommended);
        }
    }

    public void setEncryptButtonBgByRecommended() {
        if (EncryptCallUtils.isEncryptCallEnable()) {
            if (this.isEncryptSimOneRecommended) {
                if (SimFactoryManager.isExtremeSimplicityMode()) {
                    this.mDialButtonSwitcher3.setBackgroundResource(R.drawable.rectangle);
                } else {
                    this.mCardNameDialEncrypt2.setBackgroundResource(R.drawable.rectangle);
                }
            } else if (!this.isEncryptSimTwoRecommended) {
                if (this.mDialButtonSwitcher3 != null) {
                    this.mDialButtonSwitcher3.setBackgroundResource(R.drawable.btn_call);
                    this.mDialButtonSwitcher4.setBackgroundResource(R.drawable.btn_call);
                }
                if (this.mCardNameDialEncrypt2 != null) {
                    this.mCardNameDialEncrypt2.setBackgroundResource(R.drawable.btn_call);
                    this.mCardNameDialEncrypt3.setBackgroundResource(R.drawable.btn_call);
                }
            } else if (SimFactoryManager.isExtremeSimplicityMode()) {
                this.mDialButtonSwitcher4.setBackgroundResource(R.drawable.rectangle);
            } else {
                this.mCardNameDialEncrypt3.setBackgroundResource(R.drawable.rectangle);
            }
        }
    }

    public boolean setEncryptButtonBackgroundChoosed(int resultid) {
        if (!EncryptCallUtils.isEncryptCallEnable()) {
            return false;
        }
        boolean flag = true;
        switch (resultid) {
            case 0:
            case 1:
                if (SimFactoryManager.isExtremeSimplicityMode()) {
                    this.mViewSwitcher.getCurrentView().setBackgroundResource(R.drawable.rectangle);
                    break;
                }
                break;
            case 2:
                if (!SimFactoryManager.isExtremeSimplicityMode()) {
                    this.mCardNameDialEncrypt2.setBackgroundResource(R.drawable.rectangle);
                    break;
                }
                this.mDialButtonSwitcher3.setBackgroundResource(R.drawable.rectangle);
                break;
            case 3:
                if (!SimFactoryManager.isExtremeSimplicityMode()) {
                    this.mCardNameDialEncrypt3.setBackgroundResource(R.drawable.rectangle);
                    break;
                }
                this.mDialButtonSwitcher4.setBackgroundResource(R.drawable.rectangle);
                break;
            default:
                flag = false;
                break;
        }
        return flag;
    }

    public boolean isEncryptBtnUpdateRecommend(String dialString) {
        if (PhoneNumberUtils.isEmergencyNumber(dialString)) {
            return false;
        }
        boolean isFirstSimEnabled = CommonUtilMethods.getFirstSimEnabled();
        boolean isSecondSimEnabled = CommonUtilMethods.getSecondSimEnabled();
        if (isFirstSimEnabled && isSecondSimEnabled) {
            return false;
        }
        if (isFirstSimEnabled) {
            return EncryptCallUtils.isCallCard1Encrypt();
        }
        if (isSecondSimEnabled) {
            return EncryptCallUtils.isCallCard2Encrypt();
        }
        return false;
    }

    private void initialize() {
        if (HwCustContactFeatureUtils.isVOWifiFeatureEnabled()) {
            if (this.mSettingsDBObserver == null) {
                this.mSettingsDBObserver = new ContentObserver(new Handler()) {
                    public void onChange(boolean selfChange, Uri uri) {
                        super.onChange(selfChange, uri);
                        if (HwCustDialpadFragmentImpl.this.mHeaderUpdaterHandler != null) {
                            HwCustDialpadFragmentImpl.this.mHeaderUpdaterHandler.sendEmptyMessage(102);
                        }
                    }
                };
            }
            if (this.mHeaderUpdaterHandler == null) {
                this.mHeaderUpdaterHandler = new Handler() {
                    public void handleMessage(Message msg) {
                        if (HwCustDialpadFragmentImpl.this.mParent == null || (HwCustDialpadFragmentImpl.this.mParent.isAdded() && !HwCustDialpadFragmentImpl.this.mParent.isRemoving())) {
                            switch (msg.what) {
                                case 100:
                                    HwCustDialpadFragmentImpl.this.updateHeaderTextUI(HwCustDialpadFragmentImpl.this.mParent, true);
                                    break;
                                case 101:
                                    HwCustDialpadFragmentImpl.this.updateHeaderTextUI(HwCustDialpadFragmentImpl.this.mParent, false);
                                    break;
                                case 102:
                                    if (HwCustDialpadFragmentImpl.this.mDialerCallBack != null) {
                                        HwCustDialpadFragmentImpl.this.mDialerCallBack.updateButtonStatesEx(false);
                                        break;
                                    }
                                    break;
                            }
                            return;
                        }
                        Log.d(HwCustDialpadFragmentImpl.TAG, "Dialer is not added or removed");
                    }
                };
            }
        }
    }

    public void setContentObserver(Context aContext) {
        if (HwCustContactFeatureUtils.isVOWifiFeatureEnabled() && aContext != null && this.mSettingsDBObserver != null) {
            aContext.getContentResolver().registerContentObserver(this.mWifiCallSettingUri, true, this.mSettingsDBObserver);
        }
    }

    public void removeContentObserver(Context aContext) {
        if (HwCustContactFeatureUtils.isVOWifiFeatureEnabled() && aContext != null && this.mSettingsDBObserver != null) {
            aContext.getContentResolver().unregisterContentObserver(this.mSettingsDBObserver);
        }
    }

    public void checkAndAddHeaderView(View aView, LayoutInflater aInflater) {
        if (HwCustContactFeatureUtils.isVOWifiFeatureEnabled() && aView != null && aInflater != null && this.mParent != null && this.mParent.isAdded()) {
            ViewGroup lRootView = (ViewGroup) aView.findViewById(R.id.frg_content);
            if (lRootView != null && lRootView.findViewById(R.id.header_container) == null) {
                View lHeaderView = aInflater.inflate(R.layout.dialpad_header, null, false);
                lRootView.addView(lHeaderView, 0);
                ((TextView) lHeaderView.findViewById(R.id.header_text)).setGravity(1);
            }
        }
    }

    public void checkAndShowMarqueeOnResume(Context aContext) {
        if (this.mHeaderUpdaterHandler != null && HwCustContactFeatureUtils.isWifiCallEnabled(aContext)) {
            if (this.mHeaderUpdaterHandler.hasMessages(100)) {
                this.mHeaderUpdaterHandler.removeMessages(100);
            }
            this.mHeaderUpdaterHandler.sendEmptyMessage(100);
            if (this.mHeaderUpdaterHandler.hasMessages(101)) {
                this.mHeaderUpdaterHandler.removeMessages(101);
            }
            this.mHeaderUpdaterHandler.sendEmptyMessageDelayed(101, 5000);
        }
    }

    public void removeMarqueeMessageOnPause() {
        if (this.mHeaderUpdaterHandler != null && this.mHeaderUpdaterHandler.hasMessages(101)) {
            this.mHeaderUpdaterHandler.removeMessages(101);
        }
    }

    private void updateHeaderTextUI(DialpadFragment aDialPadFragment, boolean aflag) {
        if (aDialPadFragment != null && aDialPadFragment.isAdded()) {
            CallLogFragment lCallLogFragment = aDialPadFragment.getCallLogFragment();
            if (lCallLogFragment != null && lCallLogFragment.isAdded()) {
                View lCallLogList = lCallLogFragment.getView();
                View lDialPadView = aDialPadFragment.getView();
                if (lDialPadView != null && lCallLogList != null) {
                    View lHeader = lDialPadView.findViewById(R.id.header_container);
                    if (lHeader != null) {
                        TextView lHeaderTextView = (TextView) lHeader.findViewById(R.id.header_text);
                        if (lHeaderTextView != null) {
                            if (aflag) {
                                lHeader.setVisibility(0);
                                lHeaderTextView.setSelected(true);
                            } else {
                                lHeader.setVisibility(8);
                                lHeaderTextView.setText("");
                            }
                            lCallLogList.startAnimation(new UpAnimation(lCallLogList, lHeaderTextView, aflag, lHeader.getMinimumHeight()));
                        }
                    }
                }
            }
        }
    }

    public boolean checkAndInitCall(Context aContext, String number) {
        return HwCustContactFeatureUtils.checkAndInitCall(aContext, number);
    }

    public void registerDialerCallBack(DialpadCallBack aDialerCallBack) {
        if (HwCustContactFeatureUtils.isVOWifiFeatureEnabled()) {
            this.mDialerCallBack = aDialerCallBack;
        }
    }

    public void unregisterDialerCallBack() {
        this.mDialerCallBack = null;
    }
}
