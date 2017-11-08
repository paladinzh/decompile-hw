package com.android.contacts.hap.yellowpage;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.amap.api.services.core.AMapException;
import com.android.contacts.activities.ActionBarAdapter.TabState;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import com.huawei.yellowpage.YpSdkMgr;

public class YellowPageFragment extends Fragment {
    private Context mContext = null;
    private boolean mIsAttached = false;
    private boolean mIsPlugIn = false;
    private LinearLayout mMorelayout = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getActivity();
        if (HwLog.HWFLOW) {
            HwLog.i("YellowPageFragment", "[YellowPageFragment] onCreate");
        }
        setRetainInstance(true);
        setHasOptionsMenu(false);
        if (!EmuiFeatureManager.getPTSDKStatus()) {
            YellowPageUtils.initPlug(this.mContext.getApplicationContext());
            EmuiFeatureManager.setPTSDKStatus(true);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        YpSdkMgr.getInstance().onConfigurationChanged(this.mContext.getApplicationContext());
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (HwLog.HWFLOW) {
            HwLog.i("YellowPageFragment", "[YellowPageFragment] onActivityCreated");
        }
        getPlugView();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (HwLog.HWFLOW) {
            HwLog.i("YellowPageFragment", "[YellowPageFragment] onCreateView");
        }
        ViewGroup mFragmentView = (ViewGroup) inflater.inflate(R.layout.yellow_fragment, container, false);
        this.mMorelayout = (LinearLayout) mFragmentView.findViewById(R.id.more_layout);
        return mFragmentView;
    }

    public void onStart() {
        super.onStart();
        YpSdkMgr.getInstance().onPlugStart(this.mContext);
    }

    private void getPlugView() {
        if (HwLog.HWFLOW) {
            HwLog.i("YellowPageFragment", "[YellowPageFragment] getPlugView");
        }
        long startTime = System.currentTimeMillis();
        View view = YpSdkMgr.getInstance().getView(this.mContext);
        if (HwLog.HWDBG) {
            HwLog.d("YellowPageFragment", "[YellowPageFragment] getView time:" + (System.currentTimeMillis() - startTime));
        }
        if (view == null) {
            this.mMorelayout.setVisibility(8);
            HwLog.w("YellowPageFragment", "[YellowPageFragment] getPlugView getView fail");
        } else if (view.getParent() == null) {
            this.mMorelayout.setVisibility(0);
            this.mMorelayout.addView(view);
            this.mMorelayout.setTag(view);
        }
    }

    public void onResume() {
        super.onResume();
        if (HwLog.HWFLOW) {
            HwLog.i("YellowPageFragment", "[YellowPageFragment] onResume");
        }
        YpSdkMgr.getInstance().onPlugResume(this.mContext);
        if (((PeopleActivity) this.mContext).getCurrentTab() == TabState.FAVOR_YELLOWPAGE) {
            if (HwLog.HWFLOW) {
                HwLog.i("YellowPageFragment", "[YellowPageFragment] onPlugIn");
            }
            if (!this.mIsPlugIn) {
                YpSdkMgr.getInstance().onPlugIn(getActivity());
            }
        }
    }

    public void onPause() {
        super.onPause();
        if (HwLog.HWFLOW) {
            HwLog.i("YellowPageFragment", "[YellowPageFragment] onPause");
        }
        YpSdkMgr.getInstance().onPlugPause(this.mContext);
        if (((PeopleActivity) this.mContext).getCurrentTab() == TabState.FAVOR_YELLOWPAGE) {
            YpSdkMgr.getInstance().onPlugOut(getActivity());
            if (HwLog.HWFLOW) {
                HwLog.i("YellowPageFragment", "[YellowPageFragment] onPlugOut");
            }
        }
    }

    public void onStop() {
        super.onStop();
        YpSdkMgr.getInstance().onPlugStop(this.mContext);
        if (HwLog.HWFLOW) {
            HwLog.i("YellowPageFragment", "[YellowPageFragment] onPlugStop");
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
        this.mMorelayout.removeAllViews();
        if (HwLog.HWFLOW) {
            HwLog.i("YellowPageFragment", "[YellowPageFragment] onDestroyView");
        }
    }

    public void onDestroy() {
        super.onDestroy();
        YpSdkMgr.getInstance().onPlugDestory(this.mContext);
        if (HwLog.HWFLOW) {
            HwLog.i("YellowPageFragment", "[YellowPageFragment] onDestroy");
        }
    }

    public void onPageSelected(int postion, boolean isRestore) {
        boolean isSelected = TabState.FAVOR_YELLOWPAGE == postion;
        if (HwLog.HWFLOW) {
            HwLog.i("YellowPageFragment", "[YellowPageFragment] onPageSelected postion=" + postion + ", isRestore=" + isRestore);
        }
        if (isSelected && !isRestore) {
            StatisticalHelper.report(AMapException.CODE_AMAP_ENGINE_RESPONSE_ERROR);
            if (this.mIsAttached) {
                YpSdkMgr.getInstance().onPlugIn(getActivity());
                this.mIsPlugIn = true;
            }
            YpSdkMgr.getInstance().onPageSelected(getActivity());
        }
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mIsAttached = true;
    }
}
