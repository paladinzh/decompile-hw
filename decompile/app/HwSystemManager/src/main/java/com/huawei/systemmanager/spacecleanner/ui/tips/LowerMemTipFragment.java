package com.huawei.systemmanager.spacecleanner.ui.tips;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.Storage.StorageHelper;
import com.huawei.systemmanager.comm.component.IBackPressListener;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.SpaceManagerActivity;
import com.huawei.systemmanager.spacecleanner.statistics.SpaceStatsUtils;
import com.huawei.systemmanager.spacecleanner.view.LowerMemChartView;
import com.huawei.systemmanager.util.HwLog;
import java.text.NumberFormat;

public class LowerMemTipFragment extends Fragment implements IBackPressListener, OnClickListener {
    public static final long EXTERNAL_LARGGER_SPACE = 524288000;
    private static final int FUNCTION_DISABLE_PERCENT = 10;
    private static final int GO_BACK = 2;
    private static final int GO_SPACE_MANAGER = 1;
    private static final int SUGGEST_OPTIMIZE_PERCENT = 20;
    public static final String TAG = "LowerMemTipFragment";
    private static long mInternalTotalSize;
    private TextView mCurrentStorageTips;
    private boolean mFristOp = true;
    private long mInternalFreeSize = 0;
    private LowerMemChartView mLowerMemChart;
    private TextView mOptimizeTips;
    private TextView mPhoneFunctionTips;
    private Button mSpacemanagerBtn;
    private StorageHelper mStorageHelper;
    private onTipsListener mTipsListener;

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof onTipsListener) {
            this.mTipsListener = (onTipsListener) activity;
        } else {
            HwLog.d(TAG, "activity not have onTipsListener interface! ");
        }
    }

    public boolean onBackPressed() {
        HwLog.i(TAG, "onBackPressed");
        clickOpReport(2);
        return false;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SpaceStatsUtils.reportLowerMemTipsEntranceOp();
        return inflater.inflate(R.layout.spaceclean_lower_mem, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mLowerMemChart = (LowerMemChartView) view.findViewById(R.id.lower_mem_chart);
        this.mCurrentStorageTips = (TextView) view.findViewById(R.id.txt_current_storage_tips);
        this.mPhoneFunctionTips = (TextView) view.findViewById(R.id.txt_phone_function_tips);
        this.mOptimizeTips = (TextView) view.findViewById(R.id.txt_optimize_tips);
        this.mSpacemanagerBtn = (Button) view.findViewById(R.id.btn_spacemanager);
        this.mSpacemanagerBtn.setOnClickListener(this);
        initStorageState();
    }

    public void onResume() {
        super.onResume();
        this.mInternalFreeSize = this.mStorageHelper.getAvalibaleSize(0);
        int freePercent = getFreePercentInternal();
        SpaceStatsUtils.reportInternalFreePercent(freePercent);
        HwLog.i(TAG, "free percent is " + freePercent);
        if (checkPercentValidate(freePercent)) {
            this.mLowerMemChart.setPercentage(freePercent);
            double freeValue = ((double) freePercent) * 0.01d;
            NumberFormat.getPercentInstance().setMinimumFractionDigits(0);
            this.mCurrentStorageTips.setText(getApplicationContext().getResources().getString(R.string.spaceclean_lower_mem_tips, new Object[]{pnf.format(freeValue), FileUtil.getFileSize(this.mInternalFreeSize), FileUtil.getFileSize(mInternalTotalSize)}));
            if (freePercent <= 10) {
                this.mPhoneFunctionTips.setVisibility(0);
                this.mOptimizeTips.setVisibility(0);
                initOptimizeTips();
                return;
            } else if (freePercent < 20) {
                this.mOptimizeTips.setVisibility(0);
                this.mPhoneFunctionTips.setVisibility(8);
                initOptimizeTips();
                return;
            } else {
                this.mOptimizeTips.setVisibility(8);
                this.mPhoneFunctionTips.setVisibility(8);
                return;
            }
        }
        HwLog.e(TAG, "percent is not validate.do not show tips");
        this.mCurrentStorageTips.setVisibility(8);
        this.mOptimizeTips.setVisibility(8);
        this.mPhoneFunctionTips.setVisibility(8);
    }

    protected Context getApplicationContext() {
        return GlobalContext.getContext();
    }

    private void initOptimizeTips() {
        if (checkOptimizeSizeValidate(caculateOptimizeSizeSuggest())) {
            this.mOptimizeTips.setText(getApplicationContext().getResources().getString(R.string.spaceclean_txt_optimize_tips, new Object[]{FileUtil.getFileSize(optimizeSizeSuggest)}));
            return;
        }
        this.mOptimizeTips.setVisibility(8);
        HwLog.e(TAG, "Optimize Size is not validate.Do not show optimeze tips");
    }

    private boolean checkPercentValidate(int freePercent) {
        return freePercent >= 0 && freePercent <= 100;
    }

    private void initStorageState() {
        this.mStorageHelper = StorageHelper.getStorage();
        mInternalTotalSize = this.mStorageHelper.getTotalSize(0);
    }

    private int getFreePercentInternal() {
        if (this.mInternalFreeSize > mInternalTotalSize) {
            HwLog.e(TAG, "getFreePercentInternal free size is larger than total size.free:" + this.mInternalFreeSize + " total:" + mInternalTotalSize);
        }
        return Double.valueOf(((((double) this.mInternalFreeSize) * 1.0d) / ((double) mInternalTotalSize)) * 100.0d).intValue();
    }

    private long caculateOptimizeSizeSuggest() {
        return ((mInternalTotalSize * 20) / 100) - this.mInternalFreeSize;
    }

    private boolean checkOptimizeSizeValidate(long optimizeSizeSuggest) {
        return optimizeSizeSuggest >= 0 && optimizeSizeSuggest <= mInternalTotalSize;
    }

    private void openSpaceManager() {
        HwLog.i(TAG, "openSpaceManager!");
        SpaceStatsUtils.reportSpaceManagerEntranceFromLowMemTipsOp();
        Intent intent = new Intent();
        intent.setClass(getActivity(), SpaceManagerActivity.class);
        intent.putExtra(SpaceManagerActivity.KEY_CREATE_NEW_HANDLER_ID, true);
        intent.putExtra(SpaceManagerActivity.KEY_ONLY_SCAN_INTERNAL, true);
        startActivity(intent);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_spacemanager:
                HwLog.i(TAG, "SPACE_MANAGER");
                clickOpReport(1);
                openSpaceManager();
                this.mTipsListener.goToSpaceManager();
                return;
            default:
                return;
        }
    }

    private void clickOpReport(int type) {
        if (this.mFristOp) {
            SpaceStatsUtils.reportLowMemFirstOP(type);
        } else {
            SpaceStatsUtils.reportLowMemOP(type);
        }
        this.mFristOp = false;
    }
}
