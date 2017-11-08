package com.huawei.systemmanager.adblock.ui.presenter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.adblock.comm.AdBlock;
import com.huawei.systemmanager.adblock.ui.model.AdModelImpl;
import com.huawei.systemmanager.adblock.ui.model.AdModelImpl.IDataListener;
import com.huawei.systemmanager.adblock.ui.model.IAdModel;
import com.huawei.systemmanager.adblock.ui.view.IAdView;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class AdPresenterImpl implements IAdPresenter, IDataListener {
    private static final String TAG = "AdBlock_AdPresenter";
    private List<AdBlock> mAdBlockList = Lists.newArrayList();
    private final IAdModel mAdModel;
    private final IAdView mAdView;
    private AdBlockInfoAdapter mAdapter = null;
    private OnCheckedChangeListener mAllSwitchCheckedChangeListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            HwLog.i(AdPresenterImpl.TAG, "onCheckedChanged all switch checked:" + isChecked);
            AdPresenterImpl.this.allOpSwitchChanged(isChecked);
        }
    };
    private final Context mAppContext;
    private int mCheckedCount;

    private static class SwitchItemClickListener implements OnItemClickListener {
        private SwitchItemClickListener() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Switch sw = (Switch) view.findViewById(R.id.switcher);
            if (sw != null) {
                sw.performClick();
            }
        }
    }

    public AdPresenterImpl(Context context, IAdView adVIew) {
        this.mAppContext = context;
        this.mAdView = adVIew;
        this.mAdModel = new AdModelImpl(context);
        this.mAdapter = new AdBlockInfoAdapter(this.mAppContext, this);
    }

    public void onCreate(Bundle savedInstanceState) {
        this.mAdView.initListView(this.mAdapter, new SwitchItemClickListener());
    }

    public void onResume() {
        this.mAdView.showProgressBar(true);
        this.mAdModel.loadAdBlocks(this);
    }

    public void onPause() {
        this.mAdModel.cancelLoad();
    }

    public void onDestroy() {
        this.mAdBlockList.clear();
    }

    public void onLoadCompleted(List<AdBlock> adBlocks, int checkedCount) {
        this.mCheckedCount = checkedCount;
        this.mAdBlockList.clear();
        this.mAdBlockList.addAll(adBlocks);
        updateViews();
        this.mAdapter.swapData(this.mAdBlockList);
        this.mAdView.showProgressBar(false);
    }

    private void updateViews() {
        boolean z;
        boolean z2 = true;
        int totalCount = this.mAdBlockList.size();
        this.mAdView.updateTipView(totalCount, this.mCheckedCount);
        IAdView iAdView = this.mAdView;
        if (totalCount > 0) {
            z = true;
        } else {
            z = false;
        }
        if (this.mCheckedCount != totalCount) {
            z2 = false;
        }
        iAdView.updateAllOpSwitch(z, z2, this.mAllSwitchCheckedChangeListener);
    }

    public void allOpSwitchChanged(boolean isChecked) {
        String[] strArr = new String[2];
        strArr[0] = HsmStatConst.PARAM_OP;
        strArr[1] = isChecked ? "1" : "0";
        HsmStat.statE((int) Events.E_VIRUS_ADVERTISE_SWITCH, HsmStatConst.constructJsonParams(strArr));
        this.mAdModel.enableAdBlock(isChecked, this.mAdBlockList);
        modifyCheckedCount(true, isChecked);
        updateViews();
        this.mAdapter.notifyDataSetChanged();
    }

    public void itemSwitchChanged(int position, boolean isChecked) {
        String pkgname = ((AdBlock) this.mAdBlockList.get(position)).getPkgName();
        String[] strArr = new String[4];
        strArr[0] = HsmStatConst.PARAM_PKG;
        strArr[1] = pkgname;
        strArr[2] = HsmStatConst.PARAM_OP;
        strArr[3] = isChecked ? "1" : "0";
        HsmStat.statE((int) Events.E_VIRUS_ADVERTISE_ITEM, HsmStatConst.constructJsonParams(strArr));
        this.mAdModel.enableAdBlock(isChecked, this.mAdBlockList.subList(position, position + 1));
        modifyCheckedCount(false, isChecked);
        updateViews();
        this.mAdapter.notifyDataSetChanged();
    }

    private void modifyCheckedCount(boolean updateAll, boolean isChecked) {
        if (updateAll) {
            this.mCheckedCount = isChecked ? this.mAdBlockList.size() : 0;
        } else if (isChecked) {
            this.mCheckedCount++;
        } else {
            this.mCheckedCount--;
        }
    }
}
