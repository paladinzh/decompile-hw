package com.huawei.systemmanager.netassistant.ui.setting;

import android.app.Activity;
import android.content.Intent;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import com.huawei.cust.HwCustUtils;
import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.netassistant.util.CommonConstantUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.simcard.HsmSubsciptionManager;
import com.huawei.systemmanager.netassistant.ui.Item.CardItem;
import com.huawei.systemmanager.netassistant.ui.setting.OverLimitNotifyFragment.OverLimitNotifyActivity;
import com.huawei.systemmanager.netassistant.ui.setting.subpreference.IValueChangedListener;
import com.huawei.systemmanager.netassistant.ui.setting.subpreference.NotifyLimitPrefer;
import java.util.List;

public class GlobalTrafficSettingFragment extends TrafficSettingFragment {
    private static final String PREFER_KEY_PACKAGE_NOTIFY_LINE = "flow_package_notify_line";
    private GlobalCardSettingPrefer mCard1Prefer = null;
    private GlobalCardSettingPrefer mCard2Prefer = null;
    private HwCustGlobalTrafficSettingFragment mCust = ((HwCustGlobalTrafficSettingFragment) HwCustUtils.createObj(HwCustGlobalTrafficSettingFragment.class, new Object[]{this}));
    private Preference mPreferOverLimitNew;

    private class GlobalCardSettingPrefer {
        private static final String PREFER_KEY_CARD1 = "traffic_card_1";
        private static final String PREFER_KEY_CARD2 = "traffic_card_2";
        private static final String PREFER_KEY_PACKAGE_NOTIFY = "flow_package_notify";
        CardItem mCard;
        private final int mCardPreferIndex;
        private Preference mPreferOverLimit;
        private PreferenceCategory perferCate;
        private NotifyLimitPrefer totalSetting;

        public GlobalCardSettingPrefer(int index) {
            this.mCardPreferIndex = index;
            if (index == 0) {
                this.perferCate = (PreferenceCategory) GlobalTrafficSettingFragment.this.findPreference(PREFER_KEY_CARD1);
            } else if (index == 1) {
                this.perferCate = (PreferenceCategory) GlobalTrafficSettingFragment.this.findPreference(PREFER_KEY_CARD2);
            }
            if (this.perferCate != null) {
                String cardName = geOpName(this.mCardPreferIndex);
                if (this.mCardPreferIndex == 0) {
                    this.perferCate.setTitle(String.format(GlobalTrafficSettingFragment.this.getContextEx().getString(R.string.harassment_cardtab1_info), new Object[]{cardName}));
                } else if (this.mCardPreferIndex == 1) {
                    this.perferCate.setTitle(String.format(GlobalTrafficSettingFragment.this.getContextEx().getString(R.string.harassment_cardtab2_info), new Object[]{cardName}));
                }
                this.totalSetting = (NotifyLimitPrefer) this.perferCate.findPreference(NotifyLimitPrefer.TAG);
                this.totalSetting.setValueChangedListener(new IValueChangedListener() {
                    public void onValueChanged(Object newValue) {
                        GlobalTrafficSettingFragment.this.refreshCardSettins();
                    }
                });
                initOverLimitPrefer();
            }
        }

        private String geOpName(int subId) {
            String imsi = HsmSubsciptionManager.getImsi(subId);
            if (imsi == null) {
                return null;
            }
            return SimCardManager.getInstance().getOpName(imsi);
        }

        private void initOverLimitPrefer() {
            if (this.perferCate != null) {
                this.mPreferOverLimit = this.perferCate.findPreference(PREFER_KEY_PACKAGE_NOTIFY);
                this.mPreferOverLimit.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        Activity ac = GlobalTrafficSettingFragment.this.getActivity();
                        if (ac == null || GlobalCardSettingPrefer.this.mCard == null) {
                            return true;
                        }
                        Intent intent = new Intent(ac, OverLimitNotifyActivity.class);
                        intent.putExtra(CommonConstantUtil.KEY_NETASSISTANT_CARD_ITEM, GlobalCardSettingPrefer.this.mCard);
                        GlobalTrafficSettingFragment.this.startActivity(intent);
                        return true;
                    }
                });
            }
        }

        public void setCardInfo(CardItem card, boolean showTitle) {
            if (this.perferCate != null) {
                this.mCard = card;
                if (this.mCard == null) {
                    GlobalTrafficSettingFragment.this.mPreferScreen.removePreference(this.perferCate);
                    return;
                }
                GlobalTrafficSettingFragment.this.mPreferScreen.addPreference(this.perferCate);
                if (showTitle) {
                    GlobalTrafficSettingFragment.this.mPreferScreen.addPreference(this.perferCate);
                } else {
                    GlobalTrafficSettingFragment.this.mPreferScreen.removePreference(this.perferCate);
                }
                PreferenceGroup parent = showTitle ? this.perferCate : GlobalTrafficSettingFragment.this.mPreferScreen;
                boolean hasSetPackge = this.mCard.hasSetPackage();
                parent.addPreference(this.totalSetting);
                this.totalSetting.setCard(this.mCard);
                this.totalSetting.refreshPreferShow();
                if (hasSetPackge) {
                    if (GlobalTrafficSettingFragment.this.mCust != null) {
                        GlobalTrafficSettingFragment.this.mCust.addResetPreferenceToGroup(GlobalTrafficSettingFragment.this.getActivity(), parent, this.mCard.getImsi(), this.mCardPreferIndex);
                    }
                    parent.addPreference(this.mPreferOverLimit);
                    GlobalTrafficSettingFragment.this.addPreferenceLine(parent, this.perferCate);
                } else {
                    if (GlobalTrafficSettingFragment.this.mCust != null) {
                        GlobalTrafficSettingFragment.this.mCust.removeResetPreferenceFromGroup(parent, this.mCardPreferIndex);
                    }
                    parent.removePreference(this.mPreferOverLimit);
                }
            }
        }
    }

    protected void addPreference() {
        addPreferencesFromResource(R.xml.global_traffic_setting_main_preference);
    }

    protected void initPreference() {
        initCardSettings();
        refreshScreenLockNotifyPrefer();
        refreshStatusBarSpeedPrefer();
    }

    public void initCardSettings() {
        this.mCard1Prefer = new GlobalCardSettingPrefer(0);
        this.mCard2Prefer = new GlobalCardSettingPrefer(1);
    }

    public void refreshCardSettins() {
        List<CardItem> cardList = CardItem.getCardItems();
        if (HsmCollections.isEmpty(cardList)) {
            this.mCard1Prefer.setCardInfo(null, false);
            this.mCard2Prefer.setCardInfo(null, false);
        } else if (cardList.size() == 1) {
            this.mCard1Prefer.setCardInfo((CardItem) cardList.get(0), false);
            this.mCard2Prefer.setCardInfo(null, false);
        } else {
            if (cardList.size() >= 2) {
                this.mCard1Prefer.setCardInfo((CardItem) cardList.get(0), true);
                this.mCard2Prefer.setCardInfo((CardItem) cardList.get(1), true);
            }
        }
    }

    public void addPreferenceLine(PreferenceGroup parent, PreferenceCategory perferCate) {
        this.mPreferOverLimitNew = perferCate.findPreference(PREFER_KEY_PACKAGE_NOTIFY_LINE);
        if (getContext().getResources().getBoolean(R.bool.is_supprot_AGS_tvdpi)) {
            parent.addPreference(this.mPreferOverLimitNew);
        }
    }
}
