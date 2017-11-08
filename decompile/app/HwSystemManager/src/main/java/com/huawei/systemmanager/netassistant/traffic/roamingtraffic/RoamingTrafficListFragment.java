package com.huawei.systemmanager.netassistant.traffic.roamingtraffic;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.huawei.cust.HwCustUtils;
import com.huawei.netassistant.util.CommonMethodUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.daulapp.DualAppDialog;
import com.huawei.systemmanager.comm.daulapp.DualAppDialogCallBack;
import com.huawei.systemmanager.comm.daulapp.DualAppUtil;
import com.huawei.systemmanager.comm.misc.Constant;
import com.huawei.systemmanager.comm.widget.CommonAdapter;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.netassistant.traffic.appdetail.AppDetailActivity;
import com.huawei.systemmanager.netassistant.traffic.appinfo.NetAppInfo;
import com.huawei.systemmanager.netassistant.traffic.appinfo.SpecialUid;
import com.huawei.systemmanager.netassistant.traffic.datasaver.DataSaverConstants;
import com.huawei.systemmanager.netassistant.utils.ViewUtils;
import com.huawei.systemmanager.netassistant.view.TrafficFragment;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class RoamingTrafficListFragment extends TrafficFragment implements OnClickListener, RoamingTrafficView {
    public static final String ARG_IS_REMOVABLE = "arg_removable_app";
    private static final String TAG = "RoamingTrafficListFragment";
    private boolean isFirstIn = true;
    private boolean isRemovable;
    RoamingTrafficListAdapter mAdapter;
    CheckBox mBackgroundHeadCheckBox;
    CheckBox mBackgroundHeadCheckBoxChecked;
    CheckBox mBackgroundHeadCheckBoxUnchecked;
    private DualAppDialog mDualAppDialog;
    ViewStub mEmptyView;
    View mHeadView;
    private HwCustRoamingTrafficListFragment mHwCustRoamingTrafficListFragment;
    ListView mListview;
    RoamingTrafficListPresenter mPresenter;
    private ProgressBar mProgressBar = null;
    CheckBox mRoamingHeadCheckBox;
    CheckBox mRoamingHeadCheckBoxChecked;
    CheckBox mRoamingHeadCheckBoxUnchecked;
    private int mWhiteListedUid;

    public class RoamingTrafficListAdapter extends CommonAdapter<RoamingAppInfo> {
        private static final int TYPE_DISABLE_CONTROL_APP = 1;
        private static final int TYPE_ENABLE_CONTROL_APP = 0;

        public RoamingTrafficListAdapter(Context context, LayoutInflater inflater) {
            super(context, inflater);
        }

        public int getItemViewType(int position) {
            if (SpecialUid.isWhiteListUid(((RoamingAppInfo) getItem(position)).appInfo.mUid)) {
                return 1;
            }
            return 0;
        }

        public int getViewTypeCount() {
            return 2;
        }

        protected View newView(int position, ViewGroup parent, RoamingAppInfo item) {
            View convertView = this.mInflater.inflate(R.layout.advanced_app_net_manager_item, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.iconView = (ImageView) convertView.findViewById(R.id.app_icon);
            viewHolder.nameTextView = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_1);
            viewHolder.summaryTextView = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_2);
            viewHolder.summaryTextView.setSingleLine(false);
            viewHolder.roamingCheckBox = (CheckBox) convertView.findViewById(R.id.roaming_checkbox);
            viewHolder.backgroundCheckBox = (CheckBox) convertView.findViewById(R.id.background_checkbox);
            viewHolder.roamingCheckBox.setOnClickListener(RoamingTrafficListFragment.this);
            viewHolder.backgroundCheckBox.setOnClickListener(RoamingTrafficListFragment.this);
            convertView.setTag(viewHolder);
            return convertView;
        }

        protected void bindView(int position, View view, RoamingAppInfo item) {
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            if (viewHolder.iconView != null && viewHolder.nameTextView != null && viewHolder.roamingCheckBox != null && viewHolder.summaryTextView != null && viewHolder.backgroundCheckBox != null) {
                viewHolder.iconView.setImageDrawable(item.getIcon());
                viewHolder.nameTextView.setText(item.getAppLabel());
                if (item.appInfo.isMultiPkg) {
                    viewHolder.summaryTextView.setVisibility(0);
                    viewHolder.summaryTextView.setText(R.string.net_assistant_more_application);
                } else {
                    viewHolder.summaryTextView.setVisibility(8);
                }
                if (1 == getItemViewType(position)) {
                    viewHolder.roamingCheckBox.setChecked(true);
                    viewHolder.roamingCheckBox.setEnabled(false);
                    viewHolder.backgroundCheckBox.setChecked(item.isBackgroundAccess());
                    viewHolder.backgroundCheckBox.setEnabled(false);
                } else {
                    viewHolder.backgroundCheckBox.setChecked(item.isBackgroundAccess());
                    viewHolder.backgroundCheckBox.setTag(item);
                    viewHolder.roamingCheckBox.setChecked(item.getNetAccess());
                    viewHolder.roamingCheckBox.setTag(item);
                }
            }
        }
    }

    public static class ViewHolder {
        CheckBox backgroundCheckBox;
        ImageView iconView;
        TextView nameTextView;
        CheckBox roamingCheckBox;
        TextView summaryTextView;
    }

    public static Fragment newInstance() {
        return new RoamingTrafficListFragment();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        this.mWhiteListedUid = bundle.getInt(DataSaverConstants.KEY_DATA_SAVER_WHITED_LIST_UID);
        this.isRemovable = bundle.getBoolean(ARG_IS_REMOVABLE);
        HwLog.d(TAG, "app removeable = " + this.isRemovable);
        this.mPresenter = new RoamingTrafficListPresenter(this, this.isRemovable);
        this.mAdapter = new RoamingTrafficListAdapter(getActivity(), getLayoutInflater(savedInstanceState));
    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.advanced_app_fragment, container, false);
        this.mListview = (ListView) view.findViewById(R.id.list);
        this.mEmptyView = (ViewStub) view.findViewById(R.id.empty_view);
        this.mHeadView = view.findViewById(R.id.head_view);
        this.mProgressBar = (ProgressBar) view.findViewById(R.id.advance_app_loading);
        View textAll = (TextView) view.findViewById(R.id.txt);
        this.mRoamingHeadCheckBoxChecked = (CheckBox) view.findViewById(R.id.roaming_head_checked);
        this.mRoamingHeadCheckBoxUnchecked = (CheckBox) view.findViewById(R.id.roaming_head_unchecked);
        this.mRoamingHeadCheckBox = this.mRoamingHeadCheckBoxChecked;
        this.mRoamingHeadCheckBox.setOnClickListener(this);
        this.mRoamingHeadCheckBoxChecked.setOnClickListener(this);
        this.mRoamingHeadCheckBoxUnchecked.setOnClickListener(this);
        this.mBackgroundHeadCheckBoxChecked = (CheckBox) view.findViewById(R.id.background_head_checked);
        this.mBackgroundHeadCheckBoxUnchecked = (CheckBox) view.findViewById(R.id.background_head_unchecked);
        this.mBackgroundHeadCheckBox = this.mBackgroundHeadCheckBoxChecked;
        this.mBackgroundHeadCheckBox.setOnClickListener(this);
        this.mBackgroundHeadCheckBoxChecked.setOnClickListener(this);
        this.mBackgroundHeadCheckBoxUnchecked.setOnClickListener(this);
        ViewUtils.setVisibility(this.mRoamingHeadCheckBoxUnchecked, 8);
        ViewUtils.setVisibility(this.mBackgroundHeadCheckBoxUnchecked, 8);
        this.mListview.setAdapter(this.mAdapter);
        if (!this.isRemovable) {
            ViewUtils.setVisibility(this.mRoamingHeadCheckBox, 8);
            ViewUtils.setVisibility(this.mBackgroundHeadCheckBox, 8);
            ViewUtils.setVisibility(this.mRoamingHeadCheckBoxChecked, 8);
            ViewUtils.setVisibility(this.mBackgroundHeadCheckBoxChecked, 8);
            ViewUtils.setVisibility(this.mRoamingHeadCheckBoxUnchecked, 8);
            ViewUtils.setVisibility(this.mBackgroundHeadCheckBoxUnchecked, 8);
            ViewUtils.setVisibility(textAll, 4);
            this.mHwCustRoamingTrafficListFragment = (HwCustRoamingTrafficListFragment) HwCustUtils.createObj(HwCustRoamingTrafficListFragment.class, new Object[0]);
            if (this.mHwCustRoamingTrafficListFragment != null) {
                this.mHwCustRoamingTrafficListFragment.systemAppAllSelect(this.mRoamingHeadCheckBox, this.mBackgroundHeadCheckBox);
            }
        }
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mListview.setTag(Constant.DISALBE_LISTVIEW_CHECKOBX_MULTI_SELECT);
        this.mListview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                if (RoamingTrafficListFragment.this.mAdapter != null && RoamingTrafficListFragment.this.getActivity() != null) {
                    RoamingAppInfo roamingAppInfo = (RoamingAppInfo) RoamingTrafficListFragment.this.mAdapter.getItem(pos);
                    if (roamingAppInfo != null) {
                        Intent intent = AppDetailActivity.getIntent(0, roamingAppInfo.getAppLabel(), roamingAppInfo.appInfo.mUid);
                        intent.setClass(RoamingTrafficListFragment.this.getActivity(), AppDetailActivity.class);
                        RoamingTrafficListFragment.this.startActivity(intent);
                    }
                }
            }
        });
    }

    public void onResume() {
        super.onResume();
        this.mPresenter.loadingData();
    }

    public void onPause() {
        super.onPause();
        if (this.mDualAppDialog != null) {
            this.mDualAppDialog.dismiss();
        }
    }

    protected ViewStub getEmptyView() {
        return this.mEmptyView;
    }

    private void showProgressBar(boolean show) {
        int i = 0;
        if (this.mProgressBar != null) {
            setAllCheckBoxEnable(!show);
            ProgressBar progressBar = this.mProgressBar;
            if (!show) {
                i = 8;
            }
            progressBar.setVisibility(i);
        }
    }

    private boolean isProgressBarShowing() {
        boolean z = false;
        if (this.mProgressBar == null) {
            return false;
        }
        if (this.mProgressBar.getVisibility() == 0) {
            z = true;
        }
        return z;
    }

    private void setAllCheckBoxEnable(boolean enabled) {
        this.mRoamingHeadCheckBoxChecked.setEnabled(enabled);
        this.mRoamingHeadCheckBoxUnchecked.setEnabled(enabled);
        this.mBackgroundHeadCheckBoxChecked.setEnabled(enabled);
        this.mBackgroundHeadCheckBoxUnchecked.setEnabled(enabled);
    }

    public void showLoadingDialog() {
        showProgressBar(true);
    }

    public void showTrafficList(List<RoamingAppInfo> appList) {
        if (appList == null || appList.size() == 0) {
            showEmptyView();
            return;
        }
        showListView();
        this.mAdapter.swapData(appList);
        toSelectPositionForDataSaver();
    }

    private void toSelectPositionForDataSaver() {
        if (this.mWhiteListedUid != -1) {
            int position = 0;
            for (int i = 0; i < this.mAdapter.getCount(); i++) {
                RoamingAppInfo info = (RoamingAppInfo) this.mAdapter.getItem(i);
                if (info != null) {
                    NetAppInfo netAppInfo = info.appInfo;
                    if (netAppInfo != null && netAppInfo.mUid == this.mWhiteListedUid) {
                        position = i;
                        break;
                    }
                }
            }
            this.mListview.setSelection(position);
        }
    }

    protected void showEmptyView() {
        ViewUtils.setVisibility(this.mHeadView, 8);
        ViewUtils.setVisibility(this.mListview, 8);
        super.showEmptyView();
    }

    private void showListView() {
        ViewUtils.setVisibility(this.mHeadView, 0);
        ViewUtils.setVisibility(this.mListview, 0);
        ViewUtils.setVisibility(this.mEmptyView, 8);
    }

    public void syncRoamingHeadCheckBox() {
        boolean allAccess = true;
        for (RoamingAppInfo info : this.mAdapter.getData()) {
            if (!info.isNetAccess) {
                allAccess = false;
                break;
            }
        }
        if (allAccess) {
            if (this.isFirstIn) {
                this.mRoamingHeadCheckBoxUnchecked.setVisibility(8);
                this.mRoamingHeadCheckBox = this.mRoamingHeadCheckBoxChecked;
            }
            this.mRoamingHeadCheckBox.setChecked(true);
        } else {
            if (this.isFirstIn) {
                this.mRoamingHeadCheckBoxChecked.setVisibility(8);
                this.mRoamingHeadCheckBox = this.mRoamingHeadCheckBoxUnchecked;
            }
            this.mRoamingHeadCheckBox.setChecked(false);
        }
        this.mAdapter.notifyDataSetChanged();
    }

    public void syncBackgroundHeadCheckBox() {
        boolean allAccess = true;
        for (RoamingAppInfo info : this.mAdapter.getData()) {
            if (!info.isBackgroundAccess()) {
                allAccess = false;
                break;
            }
        }
        if (allAccess) {
            if (this.isFirstIn) {
                this.mBackgroundHeadCheckBoxUnchecked.setVisibility(8);
                this.mBackgroundHeadCheckBox = this.mBackgroundHeadCheckBoxChecked;
            }
            this.mBackgroundHeadCheckBox.setChecked(true);
        } else {
            if (this.isFirstIn) {
                this.mBackgroundHeadCheckBoxChecked.setVisibility(8);
                this.mBackgroundHeadCheckBox = this.mBackgroundHeadCheckBoxUnchecked;
            }
            this.mBackgroundHeadCheckBox.setChecked(false);
        }
        this.mAdapter.notifyDataSetChanged();
    }

    public void dismissLoadingDialog() {
        showCheckBox();
        showProgressBar(false);
    }

    private void showCheckBox() {
        if (this.isRemovable) {
            ViewUtils.setVisibility(this.mRoamingHeadCheckBox, 0);
            ViewUtils.setVisibility(this.mBackgroundHeadCheckBox, 0);
            this.isFirstIn = false;
        }
    }

    public void onClick(View v) {
        boolean z = false;
        if (isProgressBarShowing()) {
            HwLog.w(TAG, "Progress is showing ,can't click the item");
            return;
        }
        HsmStat.statE(Events.E_NETASSISTANT_ROAMING_APP_SWITCH_CHANGE);
        CheckBox checkBox;
        List<RoamingAppInfo> list;
        RoamingAppInfo info;
        switch (v.getId()) {
            case R.id.background_head_checked:
            case R.id.background_head_unchecked:
                checkBox = (CheckBox) v;
                list = this.mAdapter.getData();
                RoamingTrafficListPresenter roamingTrafficListPresenter = this.mPresenter;
                if (!checkBox.isChecked()) {
                    z = true;
                }
                roamingTrafficListPresenter.setBackgroundListChecked(list, z);
                break;
            case R.id.roaming_head_checked:
            case R.id.roaming_head_unchecked:
                checkBox = (CheckBox) v;
                list = this.mAdapter.getData();
                RoamingAppInfo[] infos = (RoamingAppInfo[]) list.toArray(new RoamingAppInfo[list.size()]);
                if (checkBox.isChecked()) {
                    this.mPresenter.accessList(infos);
                    HwLog.i(TAG, "access all info");
                    for (RoamingAppInfo i : list) {
                        i.isNetAccess = true;
                    }
                } else {
                    this.mPresenter.denyList(infos);
                    HwLog.i(TAG, "deny all info");
                    for (RoamingAppInfo i2 : list) {
                        i2.isNetAccess = false;
                    }
                }
                this.mAdapter.notifyDataSetChanged();
                break;
            case R.id.background_checkbox:
                checkBox = (CheckBox) v;
                info = (RoamingAppInfo) v.getTag();
                if (!checkBox.isChecked() || !isPackageCloned(info)) {
                    this.mPresenter.setBackgroundChecked(info, checkBox.isChecked());
                    break;
                } else {
                    showDialogToCheckBackgroundPermission(checkBox, info);
                    break;
                }
            case R.id.roaming_checkbox:
                checkBox = (CheckBox) v;
                info = (RoamingAppInfo) v.getTag();
                if (!checkBox.isChecked() || !isPackageCloned(info)) {
                    changeRoamingPermissionState(checkBox, info);
                    break;
                } else {
                    showDialogToCheckRoamingPermission(checkBox, info);
                    break;
                }
        }
    }

    private boolean isPackageCloned(RoamingAppInfo info) {
        if (info == null || info.appInfo == null) {
            return false;
        }
        String packageName = CommonMethodUtil.getPackageNameByUid(info.appInfo.mUid);
        if (packageName == null) {
            return false;
        }
        return DualAppUtil.isPackageCloned(getContext(), packageName);
    }

    private void showDialogToCheckBackgroundPermission(final CheckBox mCheckBox, final RoamingAppInfo mInfo) {
        CheckBox checkBox = mCheckBox;
        RoamingAppInfo info = mInfo;
        this.mDualAppDialog = new DualAppDialog(getContext(), getContext().getString(R.string.background_check_dialog_title_for_dual_app), getContext().getString(R.string.background_check_dialog_description_for_dual_app), getContext().getString(R.string.notify_dialog_forbid_for_dual_app), getContext().getString(R.string.background_check_dialog_cancel_for_dual_app), new DualAppDialogCallBack() {
            public void onPositiveBtnClick() {
                RoamingTrafficListFragment.this.mPresenter.setBackgroundChecked(mInfo, mCheckBox.isChecked());
            }

            public void onNegativeBtnClick() {
            }
        });
        this.mDualAppDialog.show();
    }

    private void showDialogToCheckRoamingPermission(final CheckBox mCheckBox, final RoamingAppInfo mInfo) {
        CheckBox checkBox = mCheckBox;
        RoamingAppInfo info = mInfo;
        this.mDualAppDialog = new DualAppDialog(getContext(), getContext().getString(R.string.roaming_check_dialog_title_for_dual_app), getContext().getString(R.string.roaming_check_dialog_description_for_dual_app), getContext().getString(R.string.notify_dialog_forbid_for_dual_app), getContext().getString(R.string.roaming_check_dialog_cancel_for_dual_app), new DualAppDialogCallBack() {
            public void onPositiveBtnClick() {
                RoamingTrafficListFragment.this.changeRoamingPermissionState(mCheckBox, mInfo);
            }

            public void onNegativeBtnClick() {
            }
        });
        this.mDualAppDialog.show();
    }

    private void changeRoamingPermissionState(CheckBox checkBox, RoamingAppInfo info) {
        if (checkBox.isChecked()) {
            this.mPresenter.deny(info);
            HwLog.i(TAG, "deny one info, name = " + info.getAppLabel());
            return;
        }
        this.mPresenter.access(info);
        HwLog.i(TAG, "access one info, name = " + info.getAppLabel());
    }
}
