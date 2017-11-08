package com.huawei.systemmanager.netassistant.netapp.ui;

import android.app.AlertDialog.Builder;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.android.collect.Lists;
import com.huawei.cust.HwCustUtils;
import com.huawei.netassistant.util.CommonMethodUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.GenericHandler;
import com.huawei.systemmanager.comm.component.GenericHandler.MessageHandler;
import com.huawei.systemmanager.comm.concurrent.HsmExecutor;
import com.huawei.systemmanager.comm.daulapp.DualAppDialog;
import com.huawei.systemmanager.comm.daulapp.DualAppDialogCallBack;
import com.huawei.systemmanager.comm.daulapp.DualAppUtil;
import com.huawei.systemmanager.comm.misc.Constant;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.CommonAdapter;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.netassistant.netapp.control.AppPermissionController;
import com.huawei.systemmanager.netassistant.netapp.control.NetAppPermissionExcutor;
import com.huawei.systemmanager.netassistant.netapp.datasource.NetAppManager;
import com.huawei.systemmanager.netassistant.netapp.datasource.NetAppManager.UidDetail;
import com.huawei.systemmanager.netassistant.traffic.appinfo.SpecialUid;
import com.huawei.systemmanager.spacecleanner.view.EnsureCheckBox;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

public class NetAppListFragment extends ListFragment implements OnClickListener, MessageHandler {
    public static final String EXTRA_APP_TYPE = "app_type";
    private static final int MSG_INIT_DATA = 201;
    private static final String TAG = NetAppListFragment.class.getSimpleName();
    private NetAppListAdapter adapter = null;
    private int listType;
    private DualAppDialog mDualAppDialog;
    private View mEmptyView;
    private GenericHandler mHandler;
    private HwCustNetAppListFragment mHwCustNetAppListFragment;
    private boolean mIsClickTaskRunning = false;
    private PackageManager mPackageManager;
    private ProgressBar mProgressBar = null;
    private CheckBox mobileCheckBoxAll;
    private CheckBox mobileCheckBoxAllChecked;
    private CheckBox mobileCheckBoxAllUnchecked;
    private CheckBox wifiCheckBoxAll;
    private CheckBox wifiCheckBoxAllChecked;
    private CheckBox wifiCheckBoxAllUnchecked;

    class BitmapWorkerTask extends AsyncTask<Integer, Void, Drawable> {
        private final WeakReference<ImageView> imageViewReference;
        private int uid = 0;

        public BitmapWorkerTask(ImageView imageView) {
            this.imageViewReference = new WeakReference(imageView);
        }

        protected Drawable doInBackground(Integer... params) {
            this.uid = params[0].intValue();
            if (this.uid == -4 || this.uid == -5 || this.uid == 1000) {
                return HsmPackageManager.getInstance().getDefaultIcon();
            }
            String[] pkgs = NetAppListFragment.this.mPackageManager.getPackagesForUid(this.uid);
            if (pkgs == null || pkgs.length <= 0) {
                return HsmPackageManager.getInstance().getDefaultIcon();
            }
            return HsmPackageManager.getInstance().getIcon(pkgs[0]);
        }

        protected void onPostExecute(Drawable bitmap) {
            if (this.imageViewReference != null && bitmap != null) {
                ImageView imageView = (ImageView) this.imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageDrawable(bitmap);
                }
            }
        }
    }

    private class NetAppClickExcutor extends AsyncTask<AppPermissionController, Void, Void> {
        protected void onPreExecute() {
            NetAppListFragment.this.showProgressBar(true);
            NetAppListFragment.this.setAllCheckviewEnable(false);
            NetAppListFragment.this.mIsClickTaskRunning = true;
            super.onPreExecute();
        }

        protected Void doInBackground(AppPermissionController... params) {
            NetAppPermissionExcutor.execute(params);
            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            NetAppListFragment.this.adapter.notifyDataSetChanged();
            NetAppListFragment.this.showProgressBar(false);
            NetAppListFragment.this.setAllCheckviewEnable(true);
            NetAppListFragment.this.mIsClickTaskRunning = false;
        }
    }

    public class NetAppListAdapter extends CommonAdapter<UidDetail> {
        private static final int TYPE_NORMAL = 0;
        private static final int TYPE_SYSTEM_CORE = 1;
        private LayoutInflater mInflater = null;

        public NetAppListAdapter(Context context, LayoutInflater inflater) {
            super(context, inflater);
            this.mInflater = inflater;
        }

        public int getItemViewType(int position) {
            UidDetail appInfo = getItem(position);
            if (appInfo == null || !SpecialUid.isWhiteListUid(appInfo.getUid())) {
                return 0;
            }
            return 1;
        }

        public int getViewTypeCount() {
            return 2;
        }

        public UidDetail getItem(int position) {
            return (UidDetail) super.getItem(position);
        }

        protected View newView(int position, ViewGroup parent, UidDetail item) {
            ViewHolder vh = new ViewHolder();
            View view = this.mInflater.inflate(R.layout.net_app_list_item, null);
            vh.iconView = (ImageView) view.findViewById(R.id.icon);
            vh.labelTextView = (TextView) view.findViewById(ViewUtil.HWID_TEXT_1);
            vh.summaryTextView = (TextView) view.findViewById(ViewUtil.HWID_TEXT_2);
            vh.wifiCheckBox = (EnsureCheckBox) view.findViewById(R.id.wifi_checkbox);
            vh.mobileCheckBox = (EnsureCheckBox) view.findViewById(R.id.mobile_checkbox);
            vh.wifiCheckBox.setOnClickListener(NetAppListFragment.this);
            vh.mobileCheckBox.setOnClickListener(NetAppListFragment.this);
            View wifiView = view.findViewById(R.id.ll_wifi_checkbox);
            if (Utility.isWifiOnlyMode()) {
                view.findViewById(R.id.ll_mobile_checkbox).setVisibility(8);
            }
            if (NetAppListFragment.this.listType == 1) {
                wifiView.setVisibility(8);
            }
            view.setTag(vh);
            return view;
        }

        protected void bindView(int position, View view, UidDetail item) {
            ViewHolder vh = (ViewHolder) view.getTag();
            new BitmapWorkerTask(vh.iconView).execute(new Integer[]{Integer.valueOf(item.getUid())});
            vh.labelTextView.setText(item.getLabel());
            vh.wifiCheckBox.setTag(item);
            vh.mobileCheckBox.setTag(item);
            if (getItemViewType(position) == 1) {
                HwLog.i(NetAppListFragment.TAG, "this is system core uid");
            }
            if (item.isMultiPkg()) {
                vh.summaryTextView.setVisibility(0);
                vh.summaryTextView.setText(GlobalContext.getString(R.string.net_assistant_more_application));
            } else {
                vh.summaryTextView.setVisibility(8);
            }
            vh.wifiCheckBox.setChecked(item.isWifiAccess());
            vh.mobileCheckBox.setChecked(item.isMobileAccess());
        }
    }

    private class NetAppListTask extends AsyncTask<Integer, Void, List<UidDetail>> {
        public NetAppListTask(Context ctx) {
        }

        protected void onPreExecute() {
            super.onPreExecute();
            if (NetAppListFragment.this.listType == 0) {
                NetAppListFragment.this.showProgressBar(true);
            }
        }

        protected List<UidDetail> doInBackground(Integer... params) {
            SparseArray<UidDetail> sparseArray;
            int i;
            List<UidDetail> details = Lists.newArrayList();
            List<UidDetail> detailFilterList = Lists.newArrayList();
            if (params[0].intValue() == 0) {
                sparseArray = NetAppManager.getAllInstalledAppWithUid(true);
            } else if (params[0].intValue() != 1) {
                return details;
            } else {
                sparseArray = NetAppManager.getAllInstalledAppWithUid(false);
            }
            for (i = 0; i < sparseArray.size(); i++) {
                details.add((UidDetail) sparseArray.valueAt(i));
            }
            if (NetAppListFragment.this.mHwCustNetAppListFragment != null) {
                List<Integer> uidFilterList = NetAppListFragment.this.mHwCustNetAppListFragment.getFilterUidList();
                if (uidFilterList != null) {
                    for (i = 0; i < details.size(); i++) {
                        UidDetail detail = (UidDetail) details.get(i);
                        if (uidFilterList.contains(Integer.valueOf(detail.getUid()))) {
                            detailFilterList.add(detail);
                        }
                    }
                    if (detailFilterList.size() > 0) {
                        details.removeAll(detailFilterList);
                    }
                }
            }
            Collections.sort(details, UidDetail.UIDDETAIL_ALP_COMPARATOR);
            return details;
        }

        protected void onPostExecute(List<UidDetail> result) {
            super.onPostExecute(result);
            if (result == null || result.size() == 0) {
                NetAppListFragment.this.mEmptyView.setVisibility(0);
            } else {
                NetAppListFragment.this.mEmptyView.setVisibility(8);
            }
            NetAppListFragment.this.adapter.swapData(result);
            NetAppListFragment.this.syncAllMobileCheckBox(true);
            NetAppListFragment.this.syncAllWifiCheckBox(true);
            NetAppListFragment.this.showCheckBox();
            NetAppListFragment.this.showProgressBar(false);
        }
    }

    private static class ViewHolder {
        ImageView iconView;
        TextView labelTextView;
        EnsureCheckBox mobileCheckBox;
        TextView summaryTextView;
        EnsureCheckBox wifiCheckBox;

        private ViewHolder() {
        }
    }

    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.adapter = new NetAppListAdapter(getActivity(), getActivity().getLayoutInflater());
        setListAdapter(this.adapter);
        this.listType = getArguments().getInt("app_type");
        this.mHandler = new GenericHandler(this);
        this.mPackageManager = GlobalContext.getContext().getPackageManager();
        this.mHwCustNetAppListFragment = (HwCustNetAppListFragment) HwCustUtils.createObj(HwCustNetAppListFragment.class, new Object[]{GlobalContext.getContext()});
    }

    public void onResume() {
        super.onResume();
        this.mHandler.sendEmptyMessageDelayed(201, 300);
    }

    public void onPause() {
        super.onPause();
        if (this.mDualAppDialog != null) {
            this.mDualAppDialog.dismiss();
        }
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setTag(Constant.DISALBE_LISTVIEW_CHECKOBX_MULTI_SELECT);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.net_app_list_fragment, container, false);
        this.mProgressBar = (ProgressBar) v.findViewById(R.id.net_app_loading);
        this.wifiCheckBoxAllChecked = (CheckBox) v.findViewById(R.id.wifi_icon_checkbox_checked);
        this.wifiCheckBoxAllUnchecked = (CheckBox) v.findViewById(R.id.wifi_icon_checkbox_unchecked);
        this.mobileCheckBoxAllChecked = (CheckBox) v.findViewById(R.id.mobile_icon_checkbox_checked);
        this.mobileCheckBoxAllUnchecked = (CheckBox) v.findViewById(R.id.mobile_icon_checkbox_unchecked);
        RelativeLayout rlAllMobileCheckBox = (RelativeLayout) v.findViewById(R.id.rl_all_checkbox);
        rlAllMobileCheckBox.setBackgroundResource(getResources().getIdentifier(ViewUtil.EMUI_SELECTOR_BACKGROUND, null, null));
        this.mobileCheckBoxAll = this.mobileCheckBoxAllChecked;
        this.wifiCheckBoxAll = this.wifiCheckBoxAllChecked;
        View wifiHeadView = v.findViewById(R.id.ll_headview_wifi);
        if (Utility.isWifiOnlyMode()) {
            v.findViewById(R.id.ll_headview_mobile).setVisibility(8);
        }
        if (this.listType == 1) {
            wifiHeadView.setVisibility(8);
            rlAllMobileCheckBox.setOnClickListener(this);
            this.mobileCheckBoxAllUnchecked.setClickable(false);
            this.mobileCheckBoxAllUnchecked.setFocusable(false);
            this.mobileCheckBoxAllChecked.setClickable(false);
            this.mobileCheckBoxAllChecked.setFocusable(false);
        } else {
            this.wifiCheckBoxAll.setOnClickListener(this);
            this.wifiCheckBoxAllChecked.setOnClickListener(this);
            this.wifiCheckBoxAllUnchecked.setOnClickListener(this);
            this.mobileCheckBoxAll.setOnClickListener(this);
            this.mobileCheckBoxAllChecked.setOnClickListener(this);
            this.mobileCheckBoxAllUnchecked.setOnClickListener(this);
        }
        this.mEmptyView = v.findViewById(R.id.empty_view);
        TextView emptyText = (TextView) v.findViewById(R.id.empty_text);
        ((ImageView) v.findViewById(R.id.empty_image)).setBackgroundResource(R.drawable.ic_no_apps);
        emptyText.setText(getString(R.string.net_assistant_no_net_apps));
        return v;
    }

    public void onHandleMessage(Message msg) {
        switch (msg.what) {
            case 201:
                new NetAppListTask(getActivity()).execute(new Integer[]{Integer.valueOf(this.listType)});
                return;
            default:
                return;
        }
    }

    private void showProgressBar(boolean show) {
        if (this.mProgressBar != null) {
            this.mProgressBar.setVisibility(show ? 0 : 8);
        }
    }

    private void setAllCheckviewEnable(boolean enabled) {
        this.wifiCheckBoxAllChecked.setEnabled(enabled);
        this.wifiCheckBoxAllUnchecked.setEnabled(enabled);
        this.mobileCheckBoxAllChecked.setEnabled(enabled);
        this.mobileCheckBoxAllUnchecked.setEnabled(enabled);
    }

    private void showCheckBox() {
        this.wifiCheckBoxAll.setVisibility(0);
        this.mobileCheckBoxAll.setVisibility(0);
    }

    public View getView() {
        return super.getView();
    }

    public void onClick(View v) {
        if (this.mIsClickTaskRunning) {
            HwLog.w(TAG, " Progress is showing ,can't click the item isClickTaskRunning");
            return;
        }
        HsmStat.statE(Events.E_NETASSISTANT_NEP_APP_SWITCH_CHANGE);
        String statParam = "";
        CheckBox cb;
        switch (v.getId()) {
            case R.id.mobile_icon_checkbox_checked:
            case R.id.mobile_icon_checkbox_unchecked:
                doMobileAllCheckBoxChange((CheckBox) v);
                break;
            case R.id.wifi_icon_checkbox_checked:
            case R.id.wifi_icon_checkbox_unchecked:
                cb = (CheckBox) v;
                int count = this.adapter.getCount();
                List<AppPermissionController> wifiHolders = Lists.newArrayList();
                for (int i = 0; i < count; i++) {
                    UidDetail netAppInfo = this.adapter.getItem(i);
                    if (cb.isChecked() != netAppInfo.isWifiAccess()) {
                        netAppInfo.changeWifiAccess();
                        wifiHolders.add(new AppPermissionController(netAppInfo.isWifiAccess() ? 0 : 1, 1, netAppInfo.getUid(), this.listType));
                    }
                }
                new NetAppClickExcutor().executeOnExecutor(HsmExecutor.THREAD_POOL_EXECUTOR, (AppPermissionController[]) wifiHolders.toArray(new AppPermissionController[wifiHolders.size()]));
                break;
            case R.id.mobile_checkbox:
                cb = (CheckBox) v;
                UidDetail mobileInfo = (UidDetail) v.getTag();
                if (!cb.isChecked() || !isPackageCloned(mobileInfo)) {
                    changeMobilePermissionState(cb, mobileInfo);
                    break;
                } else {
                    showDialogToCheckMobilePermission(cb, mobileInfo);
                    break;
                }
                break;
            case R.id.wifi_checkbox:
                cb = (CheckBox) v;
                UidDetail wifiInfo = (UidDetail) v.getTag();
                if (!cb.isChecked() || !isPackageCloned(wifiInfo)) {
                    changeWifiPermissionState(wifiInfo);
                    break;
                } else {
                    showDialogToCheckWifiPermission(cb, wifiInfo);
                    break;
                }
            case R.id.rl_all_checkbox:
                if (this.mobileCheckBoxAll != null) {
                    cb = this.mobileCheckBoxAll;
                    if (cb.isChecked()) {
                        cb.setChecked(false);
                    } else {
                        cb.setChecked(true);
                    }
                    doMobileAllCheckBoxChange(cb);
                    break;
                }
                break;
        }
    }

    private void doMobileAllCheckBoxChange(CheckBox cb) {
        int mobiCount = this.adapter.getCount();
        List<AppPermissionController> mobiHolders = Lists.newArrayList();
        for (int i = 0; i < mobiCount; i++) {
            UidDetail netAppInfo = this.adapter.getItem(i);
            if (cb.isChecked() != netAppInfo.isMobileAccess()) {
                netAppInfo.changeMobileAccess();
                mobiHolders.add(new AppPermissionController(netAppInfo.isMobileAccess() ? 0 : 1, 0, netAppInfo.getUid(), this.listType));
            }
        }
        new NetAppClickExcutor().executeOnExecutor(HsmExecutor.THREAD_POOL_EXECUTOR, (AppPermissionController[]) mobiHolders.toArray(new AppPermissionController[mobiHolders.size()]));
    }

    private boolean isPackageCloned(UidDetail wifiInfo) {
        if (wifiInfo == null) {
            return false;
        }
        String packageName = CommonMethodUtil.getPackageNameByUid(wifiInfo.getUid());
        if (packageName == null) {
            return false;
        }
        return DualAppUtil.isPackageCloned(getContext(), packageName);
    }

    private void showDialogToCheckWifiPermission(CheckBox cb, final UidDetail wifiInfo) {
        this.mDualAppDialog = new DualAppDialog(getContext(), getContext().getString(R.string.wifi_check_dialog_title_for_dual_app), getContext().getString(R.string.wifi_check_dialog_description_for_dual_app), getContext().getString(R.string.notify_dialog_forbid_for_dual_app), getContext().getString(R.string.wifi_check_dialog_cancel_for_dual_app), new DualAppDialogCallBack() {
            public void onPositiveBtnClick() {
                NetAppListFragment.this.changeWifiPermissionState(wifiInfo);
            }

            public void onNegativeBtnClick() {
            }
        });
        this.mDualAppDialog.show();
    }

    private void changeWifiPermissionState(UidDetail wifiInfo) {
        int i;
        wifiInfo.changeWifiAccess();
        if (wifiInfo.isWifiAccess()) {
            i = 0;
        } else {
            i = 1;
        }
        AppPermissionController holder = new AppPermissionController(i, 1, wifiInfo.getUid(), this.listType);
        NetAppPermissionExcutor.execute(holder);
        syncAllWifiCheckBox(false);
        String[] strArr = new String[6];
        strArr[0] = HsmStatConst.PARAM_PKG;
        strArr[1] = wifiInfo.getLabel();
        strArr[2] = HsmStatConst.PARAM_KEY;
        strArr[3] = "w";
        strArr[4] = HsmStatConst.PARAM_VAL;
        strArr[5] = wifiInfo.isMobileAccess() ? "1" : "0";
        HsmStat.statE(103, HsmStatConst.constructJsonParams(strArr));
    }

    private void showDialogToCheckMobilePermission(final CheckBox cb, final UidDetail mobileInfo) {
        this.mDualAppDialog = new DualAppDialog(getContext(), getContext().getString(R.string.mobile_check_dialog_title_for_dual_app), getContext().getString(R.string.mobile_check_dialog_description_for_dual_app), getContext().getString(R.string.notify_dialog_forbid_for_dual_app), getContext().getString(R.string.mobile_check_dialog_cancel_for_dual_app), new DualAppDialogCallBack() {
            public void onPositiveBtnClick() {
                NetAppListFragment.this.changeMobilePermissionState(cb, mobileInfo);
            }

            public void onNegativeBtnClick() {
            }
        });
        this.mDualAppDialog.show();
    }

    private void changeMobilePermissionState(CheckBox cb, UidDetail mobileInfo) {
        if (SpecialUid.isWhiteListUid(mobileInfo.getUid()) && cb.isChecked()) {
            showWarnningDialog(mobileInfo);
            return;
        }
        int i;
        mobileInfo.changeMobileAccess();
        if (mobileInfo.isMobileAccess()) {
            i = 0;
        } else {
            i = 1;
        }
        AppPermissionController holder = new AppPermissionController(i, 0, mobileInfo.getUid(), this.listType);
        NetAppPermissionExcutor.execute(holder);
        syncAllMobileCheckBox(false);
        String[] strArr = new String[6];
        strArr[0] = HsmStatConst.PARAM_PKG;
        strArr[1] = mobileInfo.getLabel();
        strArr[2] = HsmStatConst.PARAM_KEY;
        strArr[3] = "m";
        strArr[4] = HsmStatConst.PARAM_VAL;
        strArr[5] = mobileInfo.isMobileAccess() ? "1" : "0";
        HsmStat.statE(103, HsmStatConst.constructJsonParams(strArr));
    }

    private void showWarnningDialog(final UidDetail mobileInfo) {
        Builder builder = new Builder(getActivity());
        builder.setTitle(R.string.net_assistant_net_app_network_dialog_title);
        builder.setMessage(SpecialUid.getWarningTextId(mobileInfo.getUid()));
        builder.setPositiveButton(R.string.power_cpu_wakeup_dialog_close, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                int i;
                String statParam = "";
                mobileInfo.changeMobileAccess();
                if (mobileInfo.isMobileAccess()) {
                    i = 0;
                } else {
                    i = 1;
                }
                AppPermissionController holder = new AppPermissionController(i, 0, mobileInfo.getUid(), NetAppListFragment.this.listType);
                NetAppPermissionExcutor.execute(holder);
                NetAppListFragment.this.syncAllMobileCheckBox(false);
                String[] strArr = new String[6];
                strArr[0] = HsmStatConst.PARAM_PKG;
                strArr[1] = mobileInfo.getLabel();
                strArr[2] = HsmStatConst.PARAM_KEY;
                strArr[3] = "m";
                strArr[4] = HsmStatConst.PARAM_VAL;
                strArr[5] = mobileInfo.isMobileAccess() ? "1" : "0";
                HsmStat.statE(103, HsmStatConst.constructJsonParams(strArr));
            }
        });
        builder.setNegativeButton(R.string.common_cancel, null);
        builder.show().getButton(-1).setTextColor(getResources().getColor(R.color.hsm_forbidden));
    }

    private void syncAllWifiCheckBox(boolean isFirstIn) {
        boolean allAccess = true;
        for (UidDetail netInfo : this.adapter.getData()) {
            if (!netInfo.isWifiAccess()) {
                allAccess = false;
                break;
            }
        }
        if (allAccess) {
            if (isFirstIn) {
                this.wifiCheckBoxAllUnchecked.setVisibility(8);
                this.wifiCheckBoxAll = this.wifiCheckBoxAllChecked;
            }
            this.wifiCheckBoxAll.setChecked(true);
        } else {
            if (isFirstIn) {
                this.wifiCheckBoxAllChecked.setVisibility(8);
                this.wifiCheckBoxAll = this.wifiCheckBoxAllUnchecked;
            }
            this.wifiCheckBoxAll.setChecked(false);
        }
        this.adapter.notifyDataSetChanged();
    }

    private void syncAllMobileCheckBox(boolean isFirstIn) {
        boolean allAccess = true;
        for (UidDetail netInfo : this.adapter.getData()) {
            if (!netInfo.isMobileAccess()) {
                allAccess = false;
                break;
            }
        }
        if (allAccess) {
            if (isFirstIn) {
                this.mobileCheckBoxAllUnchecked.setVisibility(8);
                this.mobileCheckBoxAll = this.mobileCheckBoxAllChecked;
            }
            this.mobileCheckBoxAll.setChecked(true);
        } else {
            if (isFirstIn) {
                this.mobileCheckBoxAllChecked.setVisibility(8);
                this.mobileCheckBoxAll = this.mobileCheckBoxAllUnchecked;
            }
            this.mobileCheckBoxAll.setChecked(false);
        }
        this.adapter.notifyDataSetChanged();
    }

    public void onDestroy() {
        super.onDestroy();
        this.mHandler.quiteLooper();
    }
}
