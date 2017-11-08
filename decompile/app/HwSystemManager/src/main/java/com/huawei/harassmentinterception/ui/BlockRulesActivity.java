package com.huawei.harassmentinterception.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.db.RulesOps;
import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.simcard.HsmSubsciptionManager;
import com.huawei.systemmanager.comm.widget.CommonSwitchController;
import com.huawei.systemmanager.comm.widget.CustomViewPager;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;
import huawei.android.widget.SubTabWidget;
import huawei.android.widget.SubTabWidget.SubTab;
import huawei.support.v13.app.SubTabFragmentPagerAdapter;

public class BlockRulesActivity extends HsmActivity {
    private static final int CARD1_INDEX = 0;
    private static final int CARD2_INDEX = 1;
    private static final String TAG = "BlockRulesActivity";
    private SubCardFragment mCard1SetFrag;
    private SubCardFragment mCard2SetFrag;
    private CommonSwitchController mDualCardSwitcher;
    private View mDualcardDivider;
    private OnCheckedChangeListener mDualcardSwitchListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            HwLog.i(BlockRulesActivity.TAG, "user click mDualcardSwitchListener, isChecked:" + isChecked);
            RulesOps.setDualcardSet(BlockRulesActivity.this.getApplicationContext(), isChecked);
            BlockRulesActivity.this.updateDualcardState(isChecked);
            String[] strArr = new String[2];
            strArr[0] = HsmStatConst.PARAM_VAL;
            strArr[1] = isChecked ? String.valueOf("1") : String.valueOf("0");
            HsmStat.statE((int) Events.E_HARASSMENT_SET_DULACARD, strArr);
        }
    };
    private SubTabWidget mSubTabWidget;
    private boolean mSupportDualcard = true;
    private CustomViewPager mViewPager;

    static class DualcardSetAdapter extends SubTabFragmentPagerAdapter {
        public DualcardSetAdapter(Activity activity, ViewPager pager, SubTabWidget subTabWidget) {
            super(activity, pager, subTabWidget);
        }

        public void onPageSelected(int position) {
            super.onPageSelected(position);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mSupportDualcard = HsmSubsciptionManager.isSupportDualcard(getApplicationContext());
        HwLog.i(TAG, "phone support dualcard:" + this.mSupportDualcard);
        if (this.mSupportDualcard) {
            setContentView(R.layout.interception_block_rules);
            ViewGroup dualcardViewGroup = (ViewGroup) findViewById(R.id.duacardset);
            TextView dualCardTx = (TextView) dualcardViewGroup.findViewById(ViewUtil.HWID_TEXT_1);
            if (dualCardTx == null) {
                HwLog.e(TAG, "cannot find textview with id HWID_TEXT_1");
                dualCardTx = ViewUtil.findTextView(dualcardViewGroup, 1);
            }
            if (dualCardTx != null) {
                dualCardTx.setText(R.string.harassment_dualcardset_title);
                dualCardTx.setSingleLine(false);
            }
            this.mDualCardSwitcher = new CommonSwitchController(dualcardViewGroup, (Switch) dualcardViewGroup.findViewById(R.id.switcher), true);
            this.mDualCardSwitcher.setOnCheckedChangeListener(this.mDualcardSwitchListener);
            this.mDualcardDivider = findViewById(R.id.dualcard_divider);
            this.mViewPager = (CustomViewPager) findViewById(R.id.view_pager);
            SubTabWidget subTabWidget = (SubTabWidget) findViewById(R.id.subTab_layout);
            SubTabFragmentPagerAdapter subTabFragmentPagerAdapter = new SubTabFragmentPagerAdapter(this, this.mViewPager, subTabWidget);
            this.mCard1SetFrag = new SubCardFragment();
            this.mCard1SetFrag.setOpcard(1);
            String card1Name = geOpName(0);
            SubTab applicationSubTab = subTabWidget.newSubTab(getString(R.string.harassment_cardtab1_nocard));
            if (card1Name != null) {
                applicationSubTab = subTabWidget.newSubTab(String.format(getString(R.string.harassment_cardtab1_info), new Object[]{card1Name}));
                applicationSubTab.setSubTabId(R.id.systemmanager_harassment_cardtab1_info);
            }
            applicationSubTab.setSubTabId(R.id.systemmanager_harassment_cardtab1_nocard);
            subTabFragmentPagerAdapter.addSubTab(applicationSubTab, this.mCard1SetFrag, null, true);
            this.mCard2SetFrag = new SubCardFragment();
            this.mCard2SetFrag.setOpcard(2);
            String card2Name = geOpName(1);
            SubTab permissionSubTab = subTabWidget.newSubTab(getString(R.string.harassment_cardtab2_nocard));
            if (card2Name != null) {
                permissionSubTab = subTabWidget.newSubTab(String.format(getString(R.string.harassment_cardtab2_info), new Object[]{card2Name}));
                permissionSubTab.setSubTabId(R.id.systemmanager_harassment_cardtab2_info);
            }
            permissionSubTab.setSubTabId(R.id.systemmanager_harassment_cardtab2_nocard);
            subTabFragmentPagerAdapter.addSubTab(permissionSubTab, this.mCard2SetFrag, null, false);
            this.mSubTabWidget = subTabWidget;
            this.mViewPager.setCurrentItem(getSelectItemKey());
            return;
        }
        SubCardFragment frg = new SubCardFragment();
        frg.setOpcard(1);
        getFragmentManager().beginTransaction().replace(16908290, frg).commit();
    }

    private String geOpName(int subId) {
        String imsi = HsmSubsciptionManager.getImsi(subId);
        if (imsi == null) {
            return null;
        }
        return SimCardManager.getInstance().getOpName(imsi);
    }

    protected void onResume() {
        super.onResume();
        if (this.mSupportDualcard) {
            boolean dualCardSet = RulesOps.getDualcardSet(getApplication());
            this.mDualCardSwitcher.updateCheckState(dualCardSet);
            updateDualcardState(dualCardSet);
        }
    }

    private void updateDualcardState(boolean dualcardSet) {
        int i;
        int i2 = 8;
        SubTabWidget subTabWidget = this.mSubTabWidget;
        if (dualcardSet) {
            i = 0;
        } else {
            i = 8;
        }
        subTabWidget.setVisibility(i);
        this.mViewPager.setScrollEnable(dualcardSet);
        View view = this.mDualcardDivider;
        if (dualcardSet) {
            i2 = 0;
        }
        view.setVisibility(i2);
        if (!dualcardSet) {
            this.mViewPager.setCurrentItem(0, false);
        }
    }

    public int getSelectItemKey() {
        Intent intent = getIntent();
        return (intent != null && intent.getIntExtra(ConstValues.KEY_OP_CARD, 1) == 2) ? 1 : 0;
    }
}
