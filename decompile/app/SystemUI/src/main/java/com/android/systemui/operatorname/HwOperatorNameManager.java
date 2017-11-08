package com.android.systemui.operatorname;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.HwCustPhoneStatusBar;
import com.android.systemui.statusbar.policy.NetWorkUtils;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.Proguard;
import com.android.systemui.utils.SimCardMethod;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.UserSwitchUtils;
import com.huawei.cust.HwCustUtils;
import fyusion.vislib.BuildConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HwOperatorNameManager extends BroadcastReceiver {
    private static final boolean EE_IS_SHOW = SystemProperties.getBoolean("ro.config.hw_is_ee_show_n", false);
    private String AIR_PLANE_NAME;
    private boolean isBroadcastRegistered = false;
    private boolean isSettingDBRegistered = false;
    private Context mContext;
    HwCustPhoneStatusBar mCust = null;
    private boolean mIsAirplaneMode = false;
    private boolean mIsVoWifiEnable = false;
    private String mNetworkNameSeparator;
    private String mNoServiceName;
    private Callback mOperatorCallback;
    ArrayList<OperatorNameInfo> mOperatorNameInfoList = new ArrayList();
    private ContentObserver mOperatorShowObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            HwOperatorNameManager.this.setOperatorNameShowOrNotByUserSetting();
        }
    };
    private int mPhoneNum = 0;
    private TelephonyManager mTelephonyManager = null;
    private String mVoWifiName;
    private int mVoWifiSubId = 0;

    public interface Callback {
        void onVisibilityChanged(boolean z);

        void setMultiCardsName(String str, String str2);

        void setSingleCardName(String str);

        void updateLocale(String str);
    }

    public static class OperatorNameInfo {
        String operatorName;
        int state;
        int subscription;

        public String getOperatorName() {
            if (this.operatorName != null) {
                return this.operatorName;
            }
            return BuildConfig.FLAVOR;
        }

        public void setOperatorName(String operatorName) {
            this.operatorName = operatorName;
        }

        public int getSubscription() {
            return this.subscription;
        }

        public void setSubscription(int subscription) {
            this.subscription = subscription;
        }

        public int getState() {
            return this.state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public String toString() {
            return "subId:" + this.subscription + " ,operatorName:" + this.operatorName + " ,state:" + this.state;
        }
    }

    private void init() {
        boolean z;
        this.mNetworkNameSeparator = this.mContext.getString(R.string.status_bar_network_name_separator);
        this.mCust = (HwCustPhoneStatusBar) HwCustUtils.createObj(HwCustPhoneStatusBar.class, new Object[]{this.mContext});
        HwLog.i("HwOperatorNameManager", "init() mCust:" + (this.mCust == null ? "mCust is null" : "mCust is not null"));
        this.AIR_PLANE_NAME = this.mContext.getString(R.string.airplanemode_widget_name);
        if (Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1) {
            z = true;
        } else {
            z = false;
        }
        this.mIsAirplaneMode = z;
        this.mNoServiceName = this.mContext.getString(R.string.carrier_label_no_service);
    }

    private void registerBroadcast() {
        if (!this.isBroadcastRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SIM_STATE_CHANGED");
            filter.addAction("android.provider.Telephony.SPN_STRINGS_UPDATED");
            filter.addAction("android.intent.action.LOCALE_CHANGED");
            filter.addAction("android.intent.action.AIRPLANE_MODE");
            filter.addAction("android.intent.action.ACTION_DSDS_SUB1_OPERATOR_CHANGED");
            filter.addAction("android.intent.action.ACTION_DSDS_SUB2_OPERATOR_CHANGED");
            filter.addAction("android.provider.Telephony.SPN_STRINGS_UPDATED_VSIM");
            filter.addAction("android.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT");
            filter.addAction("android.intent.action.USER_SWITCHED");
            this.mContext.registerReceiver(this, filter);
            this.isBroadcastRegistered = true;
        }
    }

    private void unregisterBroadcast() {
        if (this.isBroadcastRegistered) {
            this.mContext.unregisterReceiver(this);
            this.isBroadcastRegistered = false;
        }
    }

    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            HwLog.i("HwOperatorNameManager", "onReceive() intent:" + Proguard.get(intent));
            if ("android.provider.Telephony.SPN_STRINGS_UPDATED".equals(action) || "android.intent.action.ACTION_DSDS_SUB1_OPERATOR_CHANGED".equals(action) || "android.intent.action.ACTION_DSDS_SUB2_OPERATOR_CHANGED".equals(action) || "android.provider.Telephony.SPN_STRINGS_UPDATED_VSIM".equals(action)) {
                updateOperatorNameByIntent(intent);
                updateParentView();
            } else if ("android.intent.action.LOCALE_CHANGED".equals(action)) {
                if (this.mContext != null) {
                    this.AIR_PLANE_NAME = this.mContext.getString(R.string.airplanemode_widget_name);
                    this.mNoServiceName = this.mContext.getString(R.string.carrier_label_no_service);
                }
                updateLanguageLocale(Locale.getDefault().getLanguage());
                updateParentView();
            } else if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
                boolean lastAirplaneMode = this.mIsAirplaneMode;
                this.mIsAirplaneMode = intent.getBooleanExtra("state", false);
                boolean modeChanged = lastAirplaneMode != this.mIsAirplaneMode;
                HwLog.i("HwOperatorNameManager", "onReceive() airplane mode changed:" + modeChanged + " mIsAirplaneMode:" + this.mIsAirplaneMode);
                if (this.mIsAirplaneMode) {
                    updateSingleCardName(this.AIR_PLANE_NAME);
                } else if (modeChanged) {
                    updateSingleCardName(BuildConfig.FLAVOR);
                }
            } else if ("android.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT".equals(action)) {
                updateOperateNameAndParentView();
            } else if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                updateSimState(intent);
            } else if ("android.intent.action.USER_SWITCHED".equals(action)) {
                setOperatorNameShowOrNotByUserSetting();
            }
        }
    }

    private void updateSimState(final Intent intent) {
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            public boolean runInThread() {
                int sub = intent.getIntExtra("subscription", 0);
                int simState = HwOperatorNameManager.this.mTelephonyManager.getSimState(sub);
                if (HwOperatorNameManager.this.isValidSub(sub)) {
                    ((OperatorNameInfo) HwOperatorNameManager.this.mOperatorNameInfoList.get(sub)).setState(simState);
                }
                return true;
            }

            public void runInUI() {
                HwOperatorNameManager.this.updateParentView();
            }
        });
    }

    private void updateOperateNameAndParentView() {
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            public boolean runInThread() {
                if (HwOperatorNameManager.this.mOperatorNameInfoList.get(0) != null) {
                    ((OperatorNameInfo) HwOperatorNameManager.this.mOperatorNameInfoList.get(0)).setState(HwOperatorNameManager.this.mTelephonyManager.getSimState(0));
                }
                if (HwOperatorNameManager.this.mOperatorNameInfoList.size() > 1 && HwOperatorNameManager.this.mOperatorNameInfoList.get(1) != null) {
                    ((OperatorNameInfo) HwOperatorNameManager.this.mOperatorNameInfoList.get(1)).setState(HwOperatorNameManager.this.mTelephonyManager.getSimState(1));
                }
                return true;
            }

            public void runInUI() {
                HwOperatorNameManager.this.updateParentView();
            }
        });
    }

    public void setOperatorNameShowOrNotByUserSetting() {
        new AsyncTask<Void, Void, Boolean>() {
            protected Boolean doInBackground(Void... params) {
                boolean showValue = HwOperatorNameManager.this.getShowValue();
                HwLog.i("HwOperatorNameManager", "showValue:" + showValue);
                return Boolean.valueOf(showValue);
            }

            protected void onPostExecute(Boolean isShowOperators) {
                HwOperatorNameManager.this.setViewVisibility(isShowOperators.booleanValue());
            }
        }.execute(new Void[0]);
    }

    public void register(Context context, Callback callback) {
        this.mContext = context;
        this.mOperatorCallback = callback;
        init();
        initOperateName();
    }

    private void initOperateName() {
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            public boolean runInThread() {
                HwOperatorNameManager.this.mTelephonyManager = TelephonyManager.from(HwOperatorNameManager.this.mContext);
                HwOperatorNameManager.this.mPhoneNum = HwOperatorNameManager.this.mTelephonyManager.getPhoneCount();
                if (SystemUiUtil.isSupportVSim()) {
                    HwOperatorNameManager hwOperatorNameManager = HwOperatorNameManager.this;
                    hwOperatorNameManager.mPhoneNum = hwOperatorNameManager.mPhoneNum + 1;
                }
                for (int i = 0; i < HwOperatorNameManager.this.mPhoneNum; i++) {
                    int state = HwOperatorNameManager.this.mTelephonyManager.getSimState(i);
                    OperatorNameInfo operatorNameInfo = new OperatorNameInfo();
                    operatorNameInfo.setSubscription(i);
                    operatorNameInfo.setState(state);
                    HwOperatorNameManager.this.mOperatorNameInfoList.add(operatorNameInfo);
                }
                if (!HwOperatorNameManager.this.isSettingDBRegistered) {
                    try {
                        HwOperatorNameManager.this.mContext.getContentResolver().registerContentObserver(System.getUriFor("hw_status_bar_operators"), true, HwOperatorNameManager.this.mOperatorShowObserver, -1);
                    } catch (IllegalArgumentException e) {
                        Log.e("HwOperatorNameManager", "uri or mOperatorObserver is null");
                    }
                    HwOperatorNameManager.this.isSettingDBRegistered = true;
                }
                HwOperatorNameManager.this.registerBroadcast();
                return true;
            }

            public void runInUI() {
                HwOperatorNameManager.this.setOperatorNameShowOrNotByUserSetting();
            }
        });
    }

    public void unregister(Context context, Callback callback) {
        this.mCust = null;
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            public boolean runInThread() {
                if (HwOperatorNameManager.this.isSettingDBRegistered && HwOperatorNameManager.this.mOperatorShowObserver != null) {
                    HwOperatorNameManager.this.mContext.getContentResolver().unregisterContentObserver(HwOperatorNameManager.this.mOperatorShowObserver);
                    HwOperatorNameManager.this.isSettingDBRegistered = false;
                }
                HwOperatorNameManager.this.unregisterBroadcast();
                return false;
            }
        });
    }

    private boolean getShowValue() {
        boolean isShowOperators = System.getIntForUser(this.mContext.getContentResolver(), "hw_status_bar_operators", 1, UserSwitchUtils.getCurrentUser()) == 1;
        HwLog.i("HwOperatorNameManager", "getShowValue() isShowOperators:" + isShowOperators);
        return isShowOperators;
    }

    private void updateOperatorNameByIntent(Intent intent) {
        boolean shouldUpdateVoWifi = true;
        String action = intent.getAction();
        if ("android.provider.Telephony.SPN_STRINGS_UPDATED".equals(action) || "android.intent.action.ACTION_DSDS_SUB1_OPERATOR_CHANGED".equals(action) || "android.intent.action.ACTION_DSDS_SUB2_OPERATOR_CHANGED".equals(action) || "android.provider.Telephony.SPN_STRINGS_UPDATED_VSIM".equals(action)) {
            int subscription = intent.getIntExtra("subscription", 0);
            boolean showSpn = intent.getBooleanExtra("showSpn", false);
            boolean showPlmn = intent.getBooleanExtra("showPlmn", false);
            boolean isVoWiFiEnable = intent.getBooleanExtra("showWifi", false);
            String voWiFiName = intent.getStringExtra("wifi");
            if (!isVoWiFiEnable && (isVoWiFiEnable == this.mIsVoWifiEnable || subscription != this.mVoWifiSubId)) {
                shouldUpdateVoWifi = false;
            }
            if (shouldUpdateVoWifi) {
                this.mIsVoWifiEnable = isVoWiFiEnable;
                this.mVoWifiName = voWiFiName;
                this.mVoWifiSubId = subscription;
            }
            String spn = intent.getStringExtra("spn");
            String plmn = intent.getStringExtra("plmn");
            HwLog.i("HwOperatorNameManager", "getOperatorNameByIntent() subscription:" + subscription + " showSpn:" + showSpn + " showPlmn:" + showPlmn + " spn:" + spn + " plmn:" + plmn + "mIsVoWiFiEnable=" + this.mIsVoWifiEnable + "mVoWiFiName=" + this.mVoWifiName + ";mVoWifiSubId=" + this.mVoWifiSubId);
            String finalNameToShow = BuildConfig.FLAVOR;
            String operatorName = getOperatorName(showSpn, spn, showPlmn, plmn);
            if (this.mCust != null) {
                this.mCust.setPlmnToSettings(operatorName, subscription);
            }
            HwLog.i("HwOperatorNameManager", "updateOperatorNameByIntent() operatorName:" + operatorName + " subscription:" + subscription);
            if (TextUtils.isEmpty(operatorName)) {
                operatorName = BuildConfig.FLAVOR;
            }
            if (isValidSub(subscription)) {
                ((OperatorNameInfo) this.mOperatorNameInfoList.get(subscription)).setOperatorName(operatorName);
            }
        }
    }

    private void updateParentView() {
        if (this.mIsAirplaneMode) {
            String airplaneStr = this.AIR_PLANE_NAME;
            if (this.mIsVoWifiEnable) {
                HwLog.i("HwOperatorNameManager", "updateParentView::airplane mode with vo wifi enable!");
                airplaneStr = this.mVoWifiName;
            }
            updateSingleCardName(airplaneStr);
        } else if (this.mPhoneNum == 0) {
            HwLog.e("HwOperatorNameManager", "mPhoneNum == 0, return!");
        } else if (1 == this.mPhoneNum) {
            OperatorNameInfo info = (OperatorNameInfo) this.mOperatorNameInfoList.get(0);
            if (info == null) {
                HwLog.e("HwOperatorNameManager", "mPhoneNum == 1 and info == null, return!");
                return;
            }
            updateSingleCardName(getCustString(info));
        } else {
            final OperatorNameInfo card1 = getOperatorNameInfo(0);
            final OperatorNameInfo card2 = getOperatorNameInfo(1);
            HwLog.i("HwOperatorNameManager", "updateParentView() card1:" + card1.toString() + " card2:" + card2.toString());
            SystemUIThread.runAsync(new SimpleAsyncTask() {
                public boolean runInThread() {
                    if (SystemUiUtil.isChinaTelecomArea()) {
                        HwLog.i("HwOperatorNameManager", "updateParentView() do not consider inactive CL phone, return ");
                        return true;
                    }
                    boolean card1Absent = SimCardMethod.isCardAbsent(HwOperatorNameManager.this.mTelephonyManager, 0);
                    HwLog.i("HwOperatorNameManager", "updateParentView(), card1Absent:" + card1Absent + " card2Absent:" + SimCardMethod.isCardAbsent(HwOperatorNameManager.this.mTelephonyManager, 1));
                    List<SubscriptionInfo> subInfos = SubscriptionManager.from(HwOperatorNameManager.this.mContext).getActiveSubscriptionInfoList();
                    if (subInfos != null && subInfos.size() == 2) {
                        boolean card1Inactive = SimCardMethod.isCardInactive(0);
                        boolean card2Inactive = SimCardMethod.isCardInactive(1);
                        HwLog.i("HwOperatorNameManager", "updateParentView(), card1Inactive:" + card1Inactive + " card2Inactive:" + card2Inactive);
                        if (card1Inactive && card2Inactive) {
                            if (card2.getSubscription() == 1 || card2.getSubscription() == 2) {
                                card2.setState(1);
                            }
                        } else if (!card1Inactive && !card2Inactive) {
                            card1.setState(5);
                            card2.setState(5);
                            return true;
                        } else if (SimCardMethod.isCardInactive(card1.getSubscription())) {
                            if (card1.getSubscription() != 2) {
                                card1.setState(1);
                                card2.setState(5);
                            }
                        } else if (SimCardMethod.isCardInactive(card2.getSubscription()) && card2.getSubscription() != 2) {
                            card1.setState(5);
                            card2.setState(1);
                        }
                    }
                    return true;
                }

                public void runInUI() {
                    int state1 = card1.getState();
                    int state2 = card2.getState();
                    HwLog.i("HwOperatorNameManager", "updateParentView() runInUI state1:" + state1 + " state2:" + state2);
                    if (HwOperatorNameManager.this.isShowCardOperatorName(0, state1, state2)) {
                        HwOperatorNameManager.this.updateSingleCardName(HwOperatorNameManager.this.getCustString(card1));
                    } else if (HwOperatorNameManager.this.isShowCardOperatorName(1, state1, state2)) {
                        HwOperatorNameManager.this.updateSingleCardName(HwOperatorNameManager.this.getCustString(card2));
                    } else if (HwOperatorNameManager.this.isShowCardOperatorName(-1, state1, state2)) {
                        if (TextUtils.isEmpty(card1.getOperatorName())) {
                            card1.setOperatorName(HwOperatorNameManager.this.mNoServiceName);
                        }
                        if (TextUtils.isEmpty(card2.getOperatorName())) {
                            card2.setOperatorName(HwOperatorNameManager.this.mNoServiceName);
                        }
                        HwOperatorNameManager.this.updateMultiCardsName(HwOperatorNameManager.this.getCustString(card1), HwOperatorNameManager.this.getCustString(card2));
                    } else {
                        String cardName = HwOperatorNameManager.this.getCustString(card1);
                        if (TextUtils.isEmpty(cardName)) {
                            cardName = HwOperatorNameManager.this.getCustString(card2);
                        }
                        HwOperatorNameManager.this.updateSingleCardName(cardName);
                    }
                }
            });
        }
    }

    private boolean isShowCardOperatorName(int showMode, int state1, int state2) {
        boolean result = false;
        switch (showMode) {
            case -1:
                if (!(state1 == 1 || state2 == 1)) {
                    result = true;
                    break;
                }
            case 0:
                if (state1 != 1 && state2 == 1) {
                    result = true;
                    break;
                }
            case 1:
                if (state1 == 1 && state2 != 1) {
                    result = true;
                    break;
                }
        }
        HwLog.i("HwOperatorNameManager", "isShowCardOperatorName showMode:" + showMode + " state1:" + state1 + " state2:" + state2 + " result:" + result);
        return result;
    }

    private OperatorNameInfo getOperatorNameInfo(int cardId) {
        OperatorNameInfo info = (OperatorNameInfo) this.mOperatorNameInfoList.get(cardId);
        int vSimdSubId = NetWorkUtils.getVSimSubId();
        if (getSimStateVSimFixup(1, vSimdSubId) != 5) {
            return info;
        }
        ((OperatorNameInfo) this.mOperatorNameInfoList.get(vSimdSubId)).setState(5);
        int sim1State = ((OperatorNameInfo) this.mOperatorNameInfoList.get(0)).getState();
        int sim2State = ((OperatorNameInfo) this.mOperatorNameInfoList.get(1)).getState();
        boolean card1Inactive = SimCardMethod.isCardInactive(0);
        boolean card2Inactive = SimCardMethod.isCardInactive(1);
        if (sim1State == 1 || sim2State == 1) {
            if (!(cardId == 0 && sim1State == 1)) {
                if (cardId != 1 || sim1State == 1 || sim2State != 1) {
                    return info;
                }
            }
            Log.d("HwOperatorNameManager", "Absent replace Vsim ");
            return (OperatorNameInfo) this.mOperatorNameInfoList.get(vSimdSubId);
        } else if (!card1Inactive && !card2Inactive) {
            return info;
        } else {
            if ((cardId != 0 || !card1Inactive) && (cardId != 1 || card1Inactive || !card2Inactive)) {
                return info;
            }
            Log.d("HwOperatorNameManager", "Inactive replace Vsim ");
            return (OperatorNameInfo) this.mOperatorNameInfoList.get(vSimdSubId);
        }
    }

    private boolean isValidSub(int sub) {
        if (sub < 0 || sub >= this.mOperatorNameInfoList.size()) {
            return false;
        }
        return true;
    }

    private String getOperatorName(boolean showSpn, String spn, boolean showPlmn, String plmn) {
        StringBuilder str = new StringBuilder();
        boolean something = false;
        if (showPlmn && !TextUtils.isEmpty(plmn)) {
            str.append(plmn);
            something = true;
        }
        if (showSpn && !TextUtils.isEmpty(spn)) {
            if (something) {
                str.append(this.mNetworkNameSeparator);
            }
            str.append(spn);
            something = true;
        }
        if (something) {
            String networkName = str.toString();
            HwLog.i("HwOperatorNameManager", "getOperatorName() networkName:" + networkName + " and return");
            return networkName;
        }
        HwLog.i("HwOperatorNameManager", "getOperatorName() return null");
        return null;
    }

    private String getCustString(OperatorNameInfo info) {
        if (info == null || this.mContext == null) {
            HwLog.i("HwOperatorNameManager", "getCustString return , info maybe null or mContext is null");
            return BuildConfig.FLAVOR;
        }
        HwLog.i("HwOperatorNameManager", "getCustString info.toString():" + info.toString());
        int subscription = info.getSubscription();
        if (this.mIsVoWifiEnable && subscription == this.mVoWifiSubId) {
            return this.mVoWifiName;
        }
        String name = info.getOperatorName();
        if (SystemUiUtil.isChinaTelecomArea()) {
            return getOperatorNameForTelecom(subscription, name);
        }
        if (SystemUiUtil.isChinaUnicomArea()) {
            return getOperatorNameForUnicom(name);
        }
        name = getOperatorNameForSkytone(subscription, name);
        if (this.mCust != null) {
            String custString = this.mCust.getCustomOperatorName(subscription, name);
            HwLog.i("HwOperatorNameManager", "getCustString custString:" + custString);
            return custString;
        }
        HwLog.i("HwOperatorNameManager", "getCustString return platform operator name:" + name);
        return name;
    }

    private String getOperatorNameForTelecom(int sub, String name) {
        String operatorName = name;
        int sim1State = ((OperatorNameInfo) this.mOperatorNameInfoList.get(0)).getState();
        int sim2State = 1;
        int vsimState = getSimStateVSimFixup(1, NetWorkUtils.getVSimSubId());
        OperatorNameInfo info2 = (OperatorNameInfo) this.mOperatorNameInfoList.get(1);
        if (info2 != null) {
            sim2State = info2.getState();
        }
        if (sim1State == 1 && sim2State == 1 && vsimState != 5) {
            return this.mContext.getString(R.string.carrier_label_no_sim_card);
        }
        if (SystemUiUtil.isMulityCard(this.mContext) && (sub == 0 || sub == 1)) {
            boolean isCardInactive = SimCardMethod.isCardInactive(sub);
            int simState = sub == 0 ? sim1State : sim2State;
            Log.d("HwOperatorNameManager", "getOperatorNameForTelecom sub:" + sub + " simState:" + simState + " isCardInactive:" + isCardInactive);
            if (simState == 1) {
                operatorName = this.mContext.getString(sub == 0 ? R.string.carrier_label_no_sim_card1 : R.string.carrier_label_no_sim_card2);
                if (vsimState == 5 && (sub == 0 || (sub == 1 && sim1State != 1))) {
                    operatorName = ((OperatorNameInfo) this.mOperatorNameInfoList.get(NetWorkUtils.getVSimSubId())).getOperatorName();
                }
            } else if (simState == 0 || simState == 6) {
                operatorName = this.mContext.getString(R.string.carrier_label_no_service);
                if (isCardInactive) {
                    operatorName = this.mContext.getString(sub == 0 ? R.string.carrier_label_not_enabled_card1 : R.string.carrier_label_not_enabled_card2);
                }
            } else if (simState == 2) {
                operatorName = this.mContext.getString(sub == 0 ? R.string.carrier_label_pin_locked_card1 : R.string.carrier_label_pin_locked_card2);
            } else if (simState == 3) {
                operatorName = this.mContext.getString(sub == 0 ? R.string.carrier_label_puk_locked_card1 : R.string.carrier_label_puk_locked_card2);
            } else if (simState == 5) {
                if (TextUtils.isEmpty(name) && !isCardInactive) {
                    operatorName = this.mContext.getString(R.string.carrier_label_no_service);
                } else if (isCardInactive) {
                    operatorName = this.mContext.getString(sub == 0 ? R.string.carrier_label_not_enabled_card1 : R.string.carrier_label_not_enabled_card2);
                }
            } else if (simState == 8) {
                operatorName = this.mContext.getString(R.string.card_invalid);
            } else if (simState == 7) {
                operatorName = this.mContext.getString(sub == 0 ? R.string.carrier_label_not_enabled_card1 : R.string.carrier_label_not_enabled_card2);
            } else {
                operatorName = this.mContext.getString(R.string.carrier_label_no_service);
            }
        }
        return operatorName;
    }

    private String getOperatorNameForUnicom(String name) {
        String operatorName = name;
        int sim1State = ((OperatorNameInfo) this.mOperatorNameInfoList.get(0)).getState();
        int sim2State = 1;
        OperatorNameInfo info2 = (OperatorNameInfo) this.mOperatorNameInfoList.get(1);
        if (info2 != null) {
            sim2State = info2.getState();
        }
        if (sim1State == 1 && sim2State == 1 && getSimStateVSimFixup(1, NetWorkUtils.getVSimSubId()) != 5) {
            return this.mContext.getString(R.string.carrier_label_no_sim_card);
        }
        return operatorName;
    }

    private String getOperatorNameForSkytone(int sub, String name) {
        String operatorName = name;
        if (this.mIsAirplaneMode || getSimStateVSimFixup(1, NetWorkUtils.getVSimSubId()) != 5) {
            return operatorName;
        }
        int sim1State = ((OperatorNameInfo) this.mOperatorNameInfoList.get(0)).getState();
        int sim2State = 1;
        OperatorNameInfo info2 = (OperatorNameInfo) this.mOperatorNameInfoList.get(1);
        if (info2 != null) {
            sim2State = info2.getState();
        }
        if (sim1State != 1 && sim2State != 1) {
            return operatorName;
        }
        if (!(sub == 0 && sim1State == 1)) {
            if (sub != 1 || sim1State == 1 || sim2State != 1) {
                return operatorName;
            }
        }
        operatorName = ((OperatorNameInfo) this.mOperatorNameInfoList.get(NetWorkUtils.getVSimSubId())).getOperatorName();
        HwLog.i("HwOperatorNameManager", "getOperatorNameForSkytone operatorName:" + operatorName);
        return operatorName;
    }

    private int getSimStateVSimFixup(int state, int sub) {
        int simState = state;
        if (SystemUiUtil.isSupportVSim() && state == 1 && isValidSub(sub) && NetWorkUtils.getVSimSubId() == sub && NetWorkUtils.getVSimCurCardType() == 2) {
            return 5;
        }
        return simState;
    }

    private void setViewVisibility(boolean visiable) {
        this.mOperatorCallback.onVisibilityChanged(visiable);
    }

    private void updateLanguageLocale(String locale) {
        this.mOperatorCallback.updateLocale(locale);
    }

    private void updateSingleCardName(String cardName) {
        this.mOperatorCallback.setSingleCardName(cardName);
    }

    private void updateMultiCardsName(String card1Name, String card2Name) {
        this.mOperatorCallback.setMultiCardsName(card1Name, card2Name);
    }
}
