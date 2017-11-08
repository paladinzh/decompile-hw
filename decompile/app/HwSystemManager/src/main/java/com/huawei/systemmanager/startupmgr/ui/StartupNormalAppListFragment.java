package com.huawei.systemmanager.startupmgr.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Switch;
import android.widget.TextView;
import com.google.android.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.daulapp.DualAppUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.CommonAdapter;
import com.huawei.systemmanager.comm.widget.CommonSwitchController;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.startupmgr.comm.AbsStartupInfo.Cmp;
import com.huawei.systemmanager.startupmgr.comm.NormalStartupInfo;
import com.huawei.systemmanager.startupmgr.comm.StartupBinderAccess;
import com.huawei.systemmanager.startupmgr.db.StartupDataMgrHelper;
import com.huawei.systemmanager.util.DimensionUtils;
import com.huawei.systemmanager.util.HSMConst;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import huawei.android.pfw.HwPFWStartupSetting;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class StartupNormalAppListFragment extends Fragment implements ISwitchChangeCallback {
    private static final int MSG_CODE_WRITE_NORMAL_INFO_LIST = 1;
    private static final int MSG_CODE_WRITE_SINGLE_NORMAL_INFO = 0;
    private static final String TAG = "StartupNormalAppListActivity";
    private boolean isMeasured = false;
    private NormalStartupInfoAdapter mAdapter = null;
    private CommonSwitchController mAllOpSwitch = null;
    private OnCheckedChangeListener mAllSwitchCheckedChangeListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int i;
            CharSequence string;
            StartupNormalAppListFragment.this.mConfirmDialog = ModifyConfirmDialog.createNewAllOpDialog(StartupNormalAppListFragment.this.getActivity(), StartupNormalAppListFragment.this.getSelf(), isChecked);
            ModifyConfirmDialog -get2 = StartupNormalAppListFragment.this.mConfirmDialog;
            if (isChecked) {
                i = R.string.startupmgr_all_allow_dialog_title;
            } else {
                i = R.string.startupmgr_all_forbid_dialog_title;
            }
            -get2.setTitle(i);
            -get2 = StartupNormalAppListFragment.this.mConfirmDialog;
            if (isChecked) {
                string = StartupNormalAppListFragment.this.getString(R.string.startupmgr_normal_all_allow_dialog_tip);
            } else {
                string = StartupNormalAppListFragment.this.getString(R.string.startupmgr_normal_all_forbid_dialog_tip);
            }
            -get2.setMessage(string);
            StartupNormalAppListFragment.this.mConfirmDialog.show();
        }
    };
    private TextView mAllowedCountView = null;
    private NormalListLoader mAsyncLoader = null;
    private int mAwakedAllowCount = 0;
    private ModifyConfirmDialog mConfirmDialog = null;
    private GridView mGridView = null;
    private Handler mHandler = null;
    private HandlerThread mHandlerThread = new HandlerThread(TAG);
    private int mHeight1;
    private int mHeight2;
    private View mJumpDivider = null;
    private RelativeLayout mJumpLayout = null;
    View mLayout = null;
    private View mNoStartupAppLayout = null;
    private int mNormalAllowCount = 0;
    private View mProgressBarLayout = null;
    private View mRecordDivider = null;
    private RelativeLayout mRecordLayout = null;
    private View mStartupAppView = null;
    private List<NormalStartupInfo> mStartupInfoList = Lists.newArrayList();

    private class NormalListLoader extends AsyncTask<Void, Void, List<NormalStartupInfo>> {
        private NormalListLoader() {
        }

        protected List<NormalStartupInfo> doInBackground(Void... params) {
            StartupNormalAppListFragment.this.resetNormalAllowCount();
            List<NormalStartupInfo> result = StartupDataMgrHelper.queryNormalStartupInfoList(GlobalContext.getContext());
            Iterator<NormalStartupInfo> iterator = result.iterator();
            while (iterator.hasNext()) {
                NormalStartupInfo tmp = (NormalStartupInfo) iterator.next();
                if (HsmPackageManager.getInstance().packageExists(tmp.getPackageName(), 0)) {
                    tmp.loadLabelAndIcon();
                    tmp.loadReadableActionList(GlobalContext.getContext());
                    if (tmp.getStatus()) {
                        StartupNormalAppListFragment.this.modifyNormalAllowCount(true);
                    }
                } else {
                    iterator.remove();
                }
            }
            Collections.sort(result, new Cmp());
            StartupNormalAppListFragment.this.mAwakedAllowCount = StartupDataMgrHelper.queryAwakedStartupCount(GlobalContext.getContext());
            return result;
        }

        protected void onPostExecute(List<NormalStartupInfo> result) {
            if (!isCancelled()) {
                StartupNormalAppListFragment.this.mStartupInfoList.clear();
                StartupNormalAppListFragment.this.mStartupInfoList.addAll(result);
                StartupNormalAppListFragment.this.updateJumpViews();
                StartupNormalAppListFragment.this.updateRecordViews();
                StartupNormalAppListFragment.this.updateTipViews();
                StartupNormalAppListFragment.this.updateAllOpSwitch();
                StartupNormalAppListFragment.this.mAdapter.swapData(StartupNormalAppListFragment.this.mStartupInfoList);
                HSMConst.doMultiply(GlobalContext.getContext(), GlobalContext.getContext().getResources().getConfiguration().orientation == 2, StartupNormalAppListFragment.this.mGridView);
                StartupNormalAppListFragment.this.showListLayout();
                StartupNormalAppListFragment.this.showProgressBar(false);
                StartupNormalAppListFragment.this.checkEmptyView();
            }
        }
    }

    private class NormalStartupInfoAdapter extends CommonAdapter<NormalStartupInfo> {
        public NormalStartupInfoAdapter(Context ctx) {
            super(ctx);
        }

        protected View newView(int position, ViewGroup parent, NormalStartupInfo item) {
            View convertView = this.mInflater.inflate(R.layout.common_list_item_twolines_image_switch, parent, false);
            CommonStartupViewHolder holder = new CommonStartupViewHolder();
            holder.mIcon = (ImageView) convertView.findViewById(R.id.image);
            holder.mTitle = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_1);
            holder.mDescription = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_2);
            holder.mSwitch = (Switch) convertView.findViewById(R.id.switcher);
            convertView.setTag(holder);
            return convertView;
        }

        protected void bindView(final int position, View view, final NormalStartupInfo item) {
            int startupDescriptionResId;
            CommonStartupViewHolder holder = (CommonStartupViewHolder) view.getTag();
            holder.mIcon.setImageDrawable(item.getIconDrawable());
            TextView textView = holder.mDescription;
            if (item.getStatus()) {
                startupDescriptionResId = item.startupDescriptionResId();
            } else {
                startupDescriptionResId = R.string.startupmgr_normal_forbid_item_description;
            }
            textView.setText(startupDescriptionResId);
            holder.mTitle.setText(item.getLabel());
            holder.mSwitch.setOnCheckedChangeListener(null);
            holder.mSwitch.setChecked(item.getStatus());
            holder.mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    boolean nStatus = !item.getStatus();
                    StartupNormalAppListFragment.this.mConfirmDialog = ModifyConfirmDialog.createNewItemOpDialog(StartupNormalAppListFragment.this.getActivity(), StartupNormalAppListFragment.this.getSelf(), position, nStatus);
                    StartupNormalAppListFragment.this.mConfirmDialog.setTitle(item.getLabel());
                    StartupNormalAppListFragment.this.mConfirmDialog.setMessage(StartupNormalAppListFragment.this.getNormalConfirmMsg(nStatus, item));
                    StartupNormalAppListFragment.this.mConfirmDialog.show();
                }
            });
        }
    }

    private class NormalWriteHandler extends Handler {
        public NormalWriteHandler(Looper looper) {
            super(looper);
        }

        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    StartupNormalAppListFragment.this.handleWriteSingleStatusInfoMsg((NormalStartupInfo) msg.obj);
                    return;
                case 1:
                    StartupNormalAppListFragment.this.handleWriteStatusInfoListMsg((List) msg.obj);
                    return;
                default:
                    return;
            }
        }
    }

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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (!CustomizeWrapper.isBootstartupEnabled()) {
            getActivity().finish();
        } else if (Utility.isOwnerUser()) {
            this.mHandlerThread.start();
            this.mHandler = new NormalWriteHandler(this.mHandlerThread.getLooper());
        } else {
            getActivity().finish();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mLayout = inflater.inflate(R.layout.startupmgr_normal_app_list, container, false);
        findViewAndInitSetting(this.mLayout);
        this.mLayout.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
            public boolean onPreDraw() {
                if (!StartupNormalAppListFragment.this.isMeasured) {
                    StartupNormalAppListFragment.this.mHeight1 = DimensionUtils.getAreaOne(StartupNormalAppListFragment.this.getActivity());
                    StartupNormalAppListFragment.this.mHeight2 = DimensionUtils.getAreaThree(StartupNormalAppListFragment.this.getActivity());
                    StartupNormalAppListFragment.this.isMeasured = true;
                    LayoutParams layoutParams = (LayoutParams) StartupNormalAppListFragment.this.mNoStartupAppLayout.getLayoutParams();
                    layoutParams.topMargin = ((int) (((double) StartupNormalAppListFragment.this.mHeight1) * 0.3d)) - (StartupNormalAppListFragment.this.mHeight1 - StartupNormalAppListFragment.this.mHeight2);
                    StartupNormalAppListFragment.this.mNoStartupAppLayout.setLayoutParams(layoutParams);
                }
                return true;
            }
        });
        return this.mLayout;
    }

    public void onPause() {
        super.onPause();
    }

    public void onResume() {
        super.onResume();
        showProgressBar(true);
        this.mAsyncLoader = new NormalListLoader();
        this.mAsyncLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
    }

    public void onDestroy() {
        this.mHandlerThread.quit();
        ModifyConfirmDialog.dismiss(getActivity());
        super.onDestroy();
    }

    public void allOpSwitchChanged(boolean isChecked) {
        HwLog.i(TAG, "allOpSwitchChanged " + isChecked);
        modifyAllCheckStatus(isChecked);
        updateTipViews();
        updateAllOpSwitch();
    }

    public void allOpSwitchChangeCancelled(boolean isChecked) {
        HwLog.i(TAG, "allOpSwitchChangeCancelled " + isChecked);
        updateAllOpSwitch();
    }

    public void itemSwitchChanged(int position, boolean isChecked) {
        modifyItemCheckStatus(position, isChecked);
        updateTipViews();
        updateAllOpSwitch();
    }

    public void itemSwitchChangeCancelled(int position, boolean isChecked) {
        HwLog.i(TAG, "itemSwitchChangeCancelled " + isChecked + ", position: " + position);
        this.mAdapter.notifyDataSetChanged();
    }

    private void findViewAndInitSetting(View view) {
        this.mProgressBarLayout = view.findViewById(R.id.startupmgr_main_app_loading);
        this.mJumpLayout = (RelativeLayout) view.findViewById(R.id.startupmgr_awaked_jump_layout);
        this.mJumpDivider = view.findViewById(R.id.startupmgr_awaked_jump_layout_divider);
        this.mRecordLayout = (RelativeLayout) view.findViewById(R.id.startupmgr_normal_record_layout);
        this.mRecordDivider = view.findViewById(R.id.startupmgr_normal_record_layout_divider);
        this.mAllowedCountView = (TextView) view.findViewById(R.id.autostart_allowed_count_tip);
        this.mStartupAppView = (LinearLayout) view.findViewById(R.id.startup_app_view);
        RelativeLayout allOpSwitchLayout = (RelativeLayout) view.findViewById(R.id.all_op_switch_layout);
        ((TextView) allOpSwitchLayout.findViewById(ViewUtil.HWID_TEXT_1)).setText(R.string.startupmgr_all_op);
        this.mAllOpSwitch = new CommonSwitchController(allOpSwitchLayout, (Switch) allOpSwitchLayout.findViewById(R.id.switcher), true);
        this.mJumpLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (StartupNormalAppListFragment.this.getActivity() != null) {
                    StartupNormalAppListFragment.this.startActivity(new Intent(StartupNormalAppListFragment.this.getActivity(), StartupAwakedAppListActivity.class));
                }
            }
        });
        this.mRecordLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (StartupNormalAppListFragment.this.getActivity() != null) {
                    StartupNormalAppListFragment.this.startActivity(new Intent(StartupNormalAppListFragment.this.getActivity(), StartupNormalRecordActivity.class));
                }
            }
        });
        this.mAdapter = new NormalStartupInfoAdapter(GlobalContext.getContext());
        this.mGridView = (GridView) view.findViewById(R.id.startupmgr_main_grid_view);
        this.mGridView.setAdapter(this.mAdapter);
        HSMConst.doMultiply(GlobalContext.getContext(), GlobalContext.getContext().getResources().getConfiguration().orientation == 2, this.mGridView);
        this.mGridView.setOnItemClickListener(new SwitchItemClickListener());
        this.mNoStartupAppLayout = view.findViewById(R.id.no_startup_app_layout);
    }

    private void checkEmptyView() {
        int i;
        int i2 = 0;
        boolean isShowNoView = this.mStartupInfoList.size() == 0;
        View view = this.mStartupAppView;
        if (isShowNoView) {
            i = 8;
        } else {
            i = 0;
        }
        view.setVisibility(i);
        View view2 = this.mNoStartupAppLayout;
        if (!isShowNoView) {
            i2 = 8;
        }
        view2.setVisibility(i2);
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        HSMConst.doMultiply(GlobalContext.getContext(), newConfig.orientation == 2, this.mGridView);
    }

    private StartupNormalAppListFragment getSelf() {
        return this;
    }

    private void updateJumpViews() {
        int i;
        int i2 = 8;
        RelativeLayout relativeLayout = this.mJumpLayout;
        if (this.mAwakedAllowCount == 0) {
            i = 8;
        } else {
            i = 0;
        }
        relativeLayout.setVisibility(i);
        View view = this.mJumpDivider;
        if (this.mAwakedAllowCount != 0) {
            i2 = 0;
        }
        view.setVisibility(i2);
    }

    private void updateRecordViews() {
        int i;
        int i2 = 8;
        RelativeLayout relativeLayout = this.mRecordLayout;
        if (this.mStartupInfoList.size() == 0) {
            i = 8;
        } else {
            i = 0;
        }
        relativeLayout.setVisibility(i);
        View view = this.mRecordDivider;
        if (this.mStartupInfoList.size() != 0) {
            i2 = 0;
        }
        view.setVisibility(i2);
    }

    private void updateTipViews() {
        this.mAllowedCountView.setText(GlobalContext.getContext().getResources().getQuantityString(R.plurals.startupmgr_allowed_count_tip, this.mNormalAllowCount, new Object[]{Integer.valueOf(this.mNormalAllowCount)}));
    }

    private void updateAllOpSwitch() {
        this.mAllOpSwitch.setOnCheckedChangeListener(null);
        this.mAllOpSwitch.updateCheckState(this.mNormalAllowCount == this.mStartupInfoList.size());
        this.mAllOpSwitch.setOnCheckedChangeListener(this.mAllSwitchCheckedChangeListener);
    }

    private void showProgressBar(boolean show) {
        if (this.mProgressBarLayout != null) {
            this.mProgressBarLayout.setVisibility(show ? 0 : 8);
        }
    }

    private void modifyNormalAllowCount(boolean isAdd) {
        if (isAdd) {
            this.mNormalAllowCount++;
        } else {
            this.mNormalAllowCount--;
        }
    }

    private void resetNormalAllowCount() {
        this.mNormalAllowCount = 0;
    }

    private void modifyAllCheckStatus(boolean isChecked) {
        for (NormalStartupInfo info : this.mStartupInfoList) {
            if (info.getStatus() != isChecked) {
                info.setStatus(isChecked);
                modifyNormalAllowCount(isChecked);
            }
        }
        sendWriteStatusInfoListMsg(this.mStartupInfoList);
        this.mAdapter.notifyDataSetChanged();
    }

    private void modifyItemCheckStatus(int position, boolean isChecked) {
        NormalStartupInfo info = (NormalStartupInfo) this.mStartupInfoList.get(position);
        if (info == null) {
            HwLog.e(TAG, "modifyItemCheckStatus can't find item of position: " + position);
            return;
        }
        HwLog.i(TAG, "modifyItemCheckStatus pkgName:" + info.getPackageName() + ", isChecked: " + isChecked + ", position:" + position);
        info.setStatus(isChecked);
        modifyNormalAllowCount(isChecked);
        sendWriteSingleStatusInfoMsg(info);
        this.mAdapter.notifyDataSetChanged();
    }

    private String getNormalConfirmMsg(boolean isChecked, NormalStartupInfo info) {
        return getNormalConfirmMsgWithActions(isChecked, info);
    }

    private String getNormalConfirmMsgWithActions(boolean isChecked, NormalStartupInfo info) {
        int resId;
        if (isChecked) {
            if (isPackageCloned(info)) {
                resId = R.plurals.startupmgr_normal_allow_msg_head_for_dual_app;
            } else {
                resId = R.plurals.startupmgr_normal_allow_msg_head;
            }
        } else if (isPackageCloned(info)) {
            resId = R.plurals.startupmgr_normal_forbid_msg_head_for_dual_app;
        } else {
            resId = R.plurals.startupmgr_normal_forbid_msg_head;
        }
        return GlobalContext.getContext().getResources().getQuantityString(resId, info.getReadableActionCount(), new Object[]{Integer.valueOf(info.getReadableActionCount())}) + info.getCombinedActionString(GlobalContext.getContext());
    }

    private boolean isPackageCloned(NormalStartupInfo info) {
        if (info == null) {
            return false;
        }
        return DualAppUtil.isPackageCloned(GlobalContext.getContext(), info.getPackageName());
    }

    private void showListLayout() {
        View listContainer = this.mLayout.findViewById(R.id.list_container);
        if (listContainer != null) {
            listContainer.setVisibility(0);
        }
    }

    private void sendWriteSingleStatusInfoMsg(NormalStartupInfo info) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(0, info));
    }

    private void sendWriteStatusInfoListMsg(List<NormalStartupInfo> infoList) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1, infoList));
    }

    private void handleWriteSingleStatusInfoMsg(NormalStartupInfo info) {
        info.setUserHasChanged();
        info.persistStatusData(GlobalContext.getContext(), true);
        String[] strArr = new String[4];
        strArr[0] = HsmStatConst.PARAM_OP;
        strArr[1] = info.getStatus() ? "1" : "0";
        strArr[2] = HsmStatConst.PARAM_PKG;
        strArr[3] = info.getPackageName();
        HsmStat.statE(1000, strArr);
    }

    private void handleWriteStatusInfoListMsg(List<NormalStartupInfo> infoList) {
        List<HwPFWStartupSetting> settingList = Lists.newArrayList();
        List<NormalStartupInfo> tmpInfoList = Lists.newArrayList();
        tmpInfoList.addAll(infoList);
        for (NormalStartupInfo info : tmpInfoList) {
            info.setUserHasChanged();
            info.persistStatusData(GlobalContext.getContext(), false);
            settingList.add(info.getPFWStartupSetting());
        }
        StartupBinderAccess.writeStartupSettingList(settingList, false);
        if (!tmpInfoList.isEmpty()) {
            String[] strArr = new String[2];
            strArr[0] = HsmStatConst.PARAM_OP;
            strArr[1] = ((NormalStartupInfo) infoList.get(0)).getStatus() ? "1" : "0";
            HsmStat.statE(1001, strArr);
        }
    }
}
