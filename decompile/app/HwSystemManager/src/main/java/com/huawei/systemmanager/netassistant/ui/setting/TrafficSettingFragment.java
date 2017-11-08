package com.huawei.systemmanager.netassistant.ui.setting;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.widget.ListView;
import com.google.common.collect.Lists;
import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.netassistant.util.CommonConstantUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.component.SingleFragmentActivity;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.simcard.HsmSubsciptionManager;
import com.huawei.systemmanager.customize.CustomizeManager;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.netassistant.traffic.setting.mainpage.ui.FstPackageSetActivity;
import com.huawei.systemmanager.netassistant.traffic.statusspeed.NatSettingInfo;
import com.huawei.systemmanager.netassistant.ui.Item.CardItem;
import com.huawei.systemmanager.netassistant.ui.setting.AjustPackageFragment.AdjustPackageActivity;
import com.huawei.systemmanager.netassistant.ui.setting.OverLimitNotifyFragment.OverLimitNotifyActivity;
import com.huawei.systemmanager.netassistant.ui.setting.PackageSetFragment.PackageSetActivity;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class TrafficSettingFragment extends PreferenceFragment {
    private static final String KEY_USAGE_DISPLAY = "usage_display";
    private static final String PREFER_CATEGORY_KEY_OTHER = "traffic_other";
    private static final String PREFER_KEY_STATUS_BAR_SPEED = "traffic_speed";
    private static final String PREFER_KEY_UNLOCKSCREEN_SWITCH = "notify_unlockscreen";
    private static final String PREFER_SCREEN_KEY = "traffic_settings";
    private static final String TAG = "TrafficSettingFragment";
    private int mCardNum;
    private List<CardSettingPrefer> mCardSettingPrefers = Lists.newArrayList();
    protected PreferenceScreen mPreferScreen;

    private class CardSettingPrefer {
        private static final String PREFER_KEY_PACKAGE_ADJUST = "flow_package_adjust";
        private static final String PREFER_KEY_PACKAGE_ADJUST_DIVIDER_LINE = "flow_package_adjust_divider_line";
        private static final String PREFER_KEY_PACKAGE_NOTIFY = "flow_package_notify";
        private static final String PREFER_KEY_PACKAGE_SET = "flow_package_settings";
        private static final String PREFER_KEY_PACKAGE_SET_DIVIDER_LINE = "flow_package_settings_divider_line";
        private CardItem mCard;
        private boolean mHasSetPackage;
        private int mIndex;
        private Preference mPerferPackageSet;
        private Preference mPerferPackageSetLine = this.perferCate.findPreference(PREFER_KEY_PACKAGE_SET_DIVIDER_LINE);
        private Preference mPreferFlowAdjust;
        private Preference mPreferFlowAdjustLine = this.perferCate.findPreference(PREFER_KEY_PACKAGE_ADJUST_DIVIDER_LINE);
        private Preference mPreferOverLimit;
        private PreferenceGroup perferCate;

        public CardSettingPrefer(PreferenceGroup preferenceGroup) {
            this.perferCate = preferenceGroup;
            initPackageSetPrefer();
            initFlowAdjustPrefer();
            initOverLimitPrefer();
        }

        private void initPackageSetPrefer() {
            this.mPerferPackageSet = this.perferCate.findPreference(PREFER_KEY_PACKAGE_SET);
            this.mPerferPackageSet.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Activity ac = TrafficSettingFragment.this.getActivity();
                    if (ac == null || CardSettingPrefer.this.mCard == null) {
                        return true;
                    }
                    Intent intent;
                    if (CardSettingPrefer.this.mHasSetPackage) {
                        intent = new Intent(ac, PackageSetActivity.class);
                        intent.putExtra(CommonConstantUtil.KEY_NETASSISTANT_CARD_ITEM, CardSettingPrefer.this.mCard);
                        if (TrafficSettingFragment.this.mCardNum > 1) {
                            intent.putExtra(CommonConstantUtil.KEY_NETASSISTANT_CARD_INDEX, CardSettingPrefer.this.mIndex);
                        }
                        TrafficSettingFragment.this.startActivity(intent);
                    } else {
                        intent = new Intent(ac, FstPackageSetActivity.class);
                        intent.putExtra(CommonConstantUtil.KEY_NETASSISTANT_IMSI, CardSettingPrefer.this.mCard.getImsi());
                        TrafficSettingFragment.this.startActivity(intent);
                    }
                    return true;
                }
            });
        }

        private void initFlowAdjustPrefer() {
            this.mPreferFlowAdjust = this.perferCate.findPreference(PREFER_KEY_PACKAGE_ADJUST);
            this.mPreferFlowAdjust.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Activity ac = TrafficSettingFragment.this.getActivity();
                    if (ac == null || CardSettingPrefer.this.mCard == null) {
                        return true;
                    }
                    Intent intent = new Intent(ac, AdjustPackageActivity.class);
                    intent.putExtra(CommonConstantUtil.KEY_NETASSISTANT_CARD_ITEM, CardSettingPrefer.this.mCard);
                    if (TrafficSettingFragment.this.mCardNum > 1) {
                        intent.putExtra(CommonConstantUtil.KEY_NETASSISTANT_CARD_INDEX, CardSettingPrefer.this.mIndex);
                    }
                    TrafficSettingFragment.this.startActivity(intent);
                    return true;
                }
            });
        }

        private void initOverLimitPrefer() {
            this.mPreferOverLimit = this.perferCate.findPreference(PREFER_KEY_PACKAGE_NOTIFY);
            this.mPreferOverLimit.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Activity ac = TrafficSettingFragment.this.getActivity();
                    if (ac == null || CardSettingPrefer.this.mCard == null) {
                        return true;
                    }
                    Intent intent = new Intent(ac, OverLimitNotifyActivity.class);
                    intent.putExtra(CommonConstantUtil.KEY_NETASSISTANT_CARD_ITEM, CardSettingPrefer.this.mCard);
                    if (TrafficSettingFragment.this.mCardNum > 1) {
                        intent.putExtra(CommonConstantUtil.KEY_NETASSISTANT_CARD_INDEX, CardSettingPrefer.this.mIndex);
                    }
                    TrafficSettingFragment.this.startActivity(intent);
                    return true;
                }
            });
        }

        public void removeFromParent() {
            if (this.perferCate.getPreferenceCount() <= 0) {
                TrafficSettingFragment.this.mPreferScreen.removePreference(this.mPerferPackageSet);
                TrafficSettingFragment.this.mPreferScreen.removePreference(this.mPerferPackageSetLine);
                TrafficSettingFragment.this.mPreferScreen.removePreference(this.mPreferFlowAdjust);
                TrafficSettingFragment.this.mPreferScreen.removePreference(this.mPreferFlowAdjustLine);
                TrafficSettingFragment.this.mPreferScreen.removePreference(this.mPreferOverLimit);
                return;
            }
            TrafficSettingFragment.this.mPreferScreen.removePreference(this.perferCate);
        }

        public void setCardInfo(CardItem card, int index, boolean showTitle) {
            this.mCard = card;
            if (this.mCard == null) {
                removeFromParent();
                return;
            }
            this.mIndex = index + 1;
            String cardName = geOpName(index);
            if (index == 0) {
                this.perferCate.setTitle(String.format(TrafficSettingFragment.this.getContextEx().getString(R.string.harassment_cardtab1_info), new Object[]{cardName}));
            } else if (index == 1) {
                this.perferCate.setTitle(String.format(TrafficSettingFragment.this.getContextEx().getString(R.string.harassment_cardtab2_info), new Object[]{cardName}));
            }
            if (showTitle) {
                TrafficSettingFragment.this.mPreferScreen.addPreference(this.perferCate);
            } else {
                TrafficSettingFragment.this.mPreferScreen.removePreference(this.perferCate);
            }
            PreferenceGroup parent = showTitle ? this.perferCate : TrafficSettingFragment.this.mPreferScreen;
            boolean hasSetPackge = this.mCard.hasSetPackage();
            this.mHasSetPackage = hasSetPackge;
            parent.addPreference(this.mPerferPackageSet);
            if (hasSetPackge) {
                parent.addPreference(this.mPerferPackageSetLine);
                parent.addPreference(this.mPreferFlowAdjust);
                parent.addPreference(this.mPreferFlowAdjustLine);
                parent.addPreference(this.mPreferOverLimit);
            } else {
                parent.removePreference(this.mPerferPackageSetLine);
                parent.removePreference(this.mPreferFlowAdjust);
                parent.removePreference(this.mPreferFlowAdjustLine);
                parent.removePreference(this.mPreferOverLimit);
            }
        }

        private String geOpName(int subId) {
            String imsi = HsmSubsciptionManager.getImsi(subId);
            if (imsi == null) {
                return null;
            }
            return SimCardManager.getInstance().getOpName(imsi);
        }
    }

    public static class TrafficSettingActivity extends SingleFragmentActivity {
        protected Fragment buildFragment() {
            if (CustomizeManager.getInstance().isFeatureEnabled(30)) {
                return new TrafficSettingFragment();
            }
            return new GlobalTrafficSettingFragment();
        }

        public boolean isSupprotMultiUser() {
            return false;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreference();
        this.mPreferScreen = (PreferenceScreen) findPreference(PREFER_SCREEN_KEY);
        initPreference();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListView lv = (ListView) getActivity().findViewById(16908298);
        if (lv != null) {
            lv.setDivider(null);
        }
    }

    public void onResume() {
        super.onResume();
        refreshCardSettins();
        refreshScreenLockNotifyPrefer();
        refreshStatusBarSpeedPrefer();
        refreshTrafficDisplayPrefer();
    }

    protected void addPreference() {
        addPreferencesFromResource(R.xml.traffic_setting_main_preference);
    }

    protected void initPreference() {
        initCardSettings();
    }

    public void initCardSettings() {
        this.mCardSettingPrefers.add(new CardSettingPrefer((PreferenceGroup) findPreference("traffic_card_1")));
        this.mCardSettingPrefers.add(new CardSettingPrefer((PreferenceGroup) findPreference("traffic_card_2")));
    }

    public void refreshCardSettins() {
        List<CardItem> cardList = CardItem.getCardItems();
        if (HsmCollections.isEmpty(cardList)) {
            this.mCardNum = 0;
            for (CardSettingPrefer cardPrefer : this.mCardSettingPrefers) {
                CardSettingPrefer cardPrefer2;
                cardPrefer2.removeFromParent();
            }
            return;
        }
        this.mCardNum = cardList.size();
        int size;
        int i;
        if (this.mCardNum == 1) {
            this.mCardNum = 1;
            size = this.mCardSettingPrefers.size();
            for (i = 0; i < size; i++) {
                cardPrefer2 = (CardSettingPrefer) this.mCardSettingPrefers.get(i);
                if (i == 0) {
                    cardPrefer2.setCardInfo((CardItem) cardList.get(0), 0, false);
                } else {
                    cardPrefer2.removeFromParent();
                }
            }
            return;
        }
        if (this.mCardNum >= 2) {
            size = this.mCardSettingPrefers.size();
            for (i = 0; i < size; i++) {
                ((CardSettingPrefer) this.mCardSettingPrefers.get(i)).setCardInfo((CardItem) cardList.get(i), i, true);
            }
        }
    }

    protected void refreshScreenLockNotifyPrefer() {
        SwitchPreference pf = (SwitchPreference) findPreference(PREFER_KEY_UNLOCKSCREEN_SWITCH);
        pf.setChecked(NatSettingInfo.getUnlockScreenNotify(getContextEx()));
        pf.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                NatSettingInfo.setUnlockScreenNotify(TrafficSettingFragment.this.getContextEx(), ((Boolean) newValue).booleanValue());
                String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, String.valueOf(checked));
                HsmStat.statE(99, statParam);
                return true;
            }
        });
    }

    protected void refreshStatusBarSpeedPrefer() {
        SwitchPreference pf = (SwitchPreference) findPreference(PREFER_KEY_STATUS_BAR_SPEED);
        if (pf != null) {
            int speedSet = NatSettingInfo.getStatusBarSpeedSet(getActivity());
            switch (speedSet) {
                case -1:
                    ((PreferenceGroup) findPreference(PREFER_CATEGORY_KEY_OTHER)).removePreference(pf);
                    break;
                case 0:
                    pf.setTitle(NatSettingInfo.getStatusBarSpeedTitle());
                    pf.setSummary(NatSettingInfo.getStatusBarSpeedSummary());
                    pf.setChecked(false);
                    break;
                case 1:
                    pf.setTitle(NatSettingInfo.getStatusBarSpeedTitle());
                    pf.setSummary(NatSettingInfo.getStatusBarSpeedSummary());
                    pf.setChecked(true);
                    break;
                default:
                    HwLog.d(TAG, "other speed set code:" + speedSet);
                    break;
            }
            pf.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean isChecked = ((Boolean) newValue).booleanValue();
                    NatSettingInfo.setStatusBarSpeedEnable(isChecked, TrafficSettingFragment.this.getActivity());
                    String[] strArr = new String[2];
                    strArr[0] = HsmStatConst.PARAM_OP;
                    strArr[1] = isChecked ? "1" : "0";
                    HsmStat.statE(101, HsmStatConst.constructJsonParams(strArr));
                    return true;
                }
            });
        }
    }

    protected void refreshTrafficDisplayPrefer() {
        boolean z = true;
        SwitchPreference pf = (SwitchPreference) findPreference(KEY_USAGE_DISPLAY);
        if (pf != null) {
            if (NatSettingInfo.getTrafficDisplaySet(getActivity()) != 1) {
                z = false;
            }
            pf.setChecked(z);
            pf.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean isChecked = ((Boolean) newValue).booleanValue();
                    NatSettingInfo.setTrafficDisplayEnable(isChecked, TrafficSettingFragment.this.getActivity());
                    String[] strArr = new String[2];
                    strArr[0] = HsmStatConst.PARAM_VAL;
                    strArr[1] = isChecked ? "1" : "0";
                    HsmStat.statE((int) Events.E_NETASSISTANT_FLOW_SHOW, HsmStatConst.constructJsonParams(strArr));
                    return true;
                }
            });
        }
    }

    public Context getContextEx() {
        Context ctx = getContext();
        if (ctx != null) {
            return ctx;
        }
        return GlobalContext.getContext();
    }
}
