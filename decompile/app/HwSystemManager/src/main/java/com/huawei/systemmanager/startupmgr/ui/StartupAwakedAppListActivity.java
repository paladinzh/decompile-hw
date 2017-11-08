package com.huawei.systemmanager.startupmgr.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.internal.view.SupportMenu;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Switch;
import android.widget.TextView;
import com.google.common.collect.Lists;
import com.huawei.permissionmanager.ui.AwakedAppNotifySwitcher;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.daulapp.DualAppUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.StringUtils;
import com.huawei.systemmanager.comm.widget.CommonAdapter;
import com.huawei.systemmanager.comm.widget.CommonSwitchController;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.startupmgr.comm.AbsStartupInfo.Cmp;
import com.huawei.systemmanager.startupmgr.comm.AwakedStartupInfo;
import com.huawei.systemmanager.startupmgr.comm.StartupBinderAccess;
import com.huawei.systemmanager.startupmgr.confdata.PaymentPkgChecker;
import com.huawei.systemmanager.startupmgr.confdata.StartupConfData;
import com.huawei.systemmanager.startupmgr.db.StartupDataMgrHelper;
import com.huawei.systemmanager.util.DimensionUtils;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import huawei.android.pfw.HwPFWStartupSetting;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class StartupAwakedAppListActivity extends HsmActivity implements ISwitchChangeCallback {
    private static final int MSG_CODE_WRITE_AWAKED_INFO_LIST = 1;
    private static final int MSG_CODE_WRITE_SINGLE_AWAKE_INFO = 0;
    private static final String TAG = "StartupAwakedAppListActivity";
    private AwakedStartupInfoAdapter mAdapter = null;
    private CommonSwitchController mAllOpSwitch = null;
    private OnCheckedChangeListener mAllSwitchCheckedChangeListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int i;
            CharSequence string;
            StartupAwakedAppListActivity.this.mConfirmDialog = ModifyConfirmDialog.createNewAllOpDialog(StartupAwakedAppListActivity.this.getSelf(), StartupAwakedAppListActivity.this.getSelf(), isChecked);
            ModifyConfirmDialog -get2 = StartupAwakedAppListActivity.this.mConfirmDialog;
            if (isChecked) {
                i = R.string.startupmgr_all_allow_dialog_title;
            } else {
                i = R.string.startupmgr_all_forbid_dialog_title;
            }
            -get2.setTitle(i);
            -get2 = StartupAwakedAppListActivity.this.mConfirmDialog;
            if (isChecked) {
                string = StartupAwakedAppListActivity.this.getString(R.string.startupmgr_awaked_all_allow_dialog_tip);
            } else {
                string = StartupAwakedAppListActivity.this.getString(R.string.startupmgr_awaked_all_forbid_dialog_tip);
            }
            -get2.setMessage(string);
            StartupAwakedAppListActivity.this.mConfirmDialog.show();
        }
    };
    private int mAllowCount = 0;
    private TextView mAllowedCountView = null;
    private Context mAppContext = null;
    private AwakedListLoader mAsyncLoader = null;
    private ModifyConfirmDialog mConfirmDialog = null;
    private Handler mHandler = null;
    private HandlerThread mHandlerThread = new HandlerThread(TAG);
    private int mHeight1;
    private int mHeight2;
    private ListView mListView = null;
    private View mNoStartupAppAwakeLayout = null;
    private AwakedAppNotifySwitcher mNotifySwitcher;
    private View mProgressBarLayout = null;
    private View mRecordDivider = null;
    private RelativeLayout mRecordLayout = null;
    private View mStartupAppView = null;
    private List<AwakedStartupInfo> mStartupInfoList = Lists.newArrayList();

    private class AwakedHandler extends Handler {
        public AwakedHandler(Looper looper) {
            super(looper);
        }

        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    StartupAwakedAppListActivity.this.handleWriteSingleStatusInfoMsg((AwakedStartupInfo) msg.obj);
                    return;
                case 1:
                    StartupAwakedAppListActivity.this.handleWriteStatusInfoListMsg((List) msg.obj);
                    return;
                default:
                    return;
            }
        }
    }

    private class AwakedListLoader extends AsyncTask<Void, Void, List<AwakedStartupInfo>> {
        private AwakedListLoader() {
        }

        protected List<AwakedStartupInfo> doInBackground(Void... params) {
            StartupAwakedAppListActivity.this.resetAllowCount();
            List<AwakedStartupInfo> result = StartupDataMgrHelper.queryAwakedStartupInfoList(StartupAwakedAppListActivity.this.mAppContext);
            Iterator<AwakedStartupInfo> iterator = result.iterator();
            while (iterator.hasNext()) {
                AwakedStartupInfo tmp = (AwakedStartupInfo) iterator.next();
                if (HsmPackageManager.getInstance().packageExists(tmp.getPackageName(), 0)) {
                    tmp.loadLabelAndIcon();
                    tmp.loadCallerLabel(StartupAwakedAppListActivity.this.mAppContext);
                    if (tmp.getStatus()) {
                        StartupAwakedAppListActivity.this.modifyAllowCount(true);
                    }
                    if (StartupConfData.isCompetitorPackage(StartupAwakedAppListActivity.this.mAppContext, tmp.getPackageName())) {
                        tmp.setPkgIsCompetitor();
                    }
                } else {
                    iterator.remove();
                }
            }
            Collections.sort(result, new Cmp());
            PaymentPkgChecker.getInstance().init(StartupAwakedAppListActivity.this.mAppContext);
            return result;
        }

        protected void onPostExecute(List<AwakedStartupInfo> result) {
            if (!isCancelled()) {
                StartupAwakedAppListActivity.this.mStartupInfoList.clear();
                StartupAwakedAppListActivity.this.mStartupInfoList.addAll(result);
                StartupAwakedAppListActivity.this.updateRecordViews();
                StartupAwakedAppListActivity.this.updateTipView();
                StartupAwakedAppListActivity.this.updateAllOpSwitch();
                StartupAwakedAppListActivity.this.mAdapter.swapData(StartupAwakedAppListActivity.this.mStartupInfoList);
                StartupAwakedAppListActivity.this.showProgressBar(false);
                StartupAwakedAppListActivity.this.checkEmptyView();
            }
        }
    }

    public class AwakedStartupInfoAdapter extends CommonAdapter<AwakedStartupInfo> {
        public AwakedStartupInfoAdapter(Context ctx) {
            super(ctx);
        }

        protected View newView(int position, ViewGroup parent, AwakedStartupInfo item) {
            View convertView = this.mInflater.inflate(R.layout.common_list_item_twolines_image_switch, parent, false);
            CommonStartupViewHolder holder = new CommonStartupViewHolder();
            holder.mIcon = (ImageView) convertView.findViewById(R.id.image);
            holder.mTitle = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_1);
            holder.mDescription = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_2);
            holder.mSwitch = (Switch) convertView.findViewById(R.id.switcher);
            convertView.setTag(holder);
            return convertView;
        }

        protected void bindView(final int position, View view, final AwakedStartupInfo item) {
            CommonStartupViewHolder holder = (CommonStartupViewHolder) view.getTag();
            holder.mIcon.setImageDrawable(item.getIconDrawable());
            if (!PaymentPkgChecker.isPaymentPkg(item.getPackageName())) {
                int i;
                TextView textView = holder.mDescription;
                if (item.getStatus()) {
                    i = R.string.startupmgr_awaked_allow_item_description;
                } else {
                    i = R.string.startupmgr_awaked_forbid_item_description;
                }
                textView.setText(i);
            } else if (StartupAwakedAppListActivity.this.isPackageCloned(item)) {
                holder.mDescription.setText(StartupAwakedAppListActivity.highlightRed(R.string.startupmgr_pay_warnning_des_for_dual_app));
            } else {
                holder.mDescription.setText(StartupAwakedAppListActivity.highlightRed(R.string.startupmgr_pay_warnning_des));
            }
            holder.mTitle.setText(item.getLabel());
            holder.mSwitch.setOnCheckedChangeListener(null);
            holder.mSwitch.setChecked(item.getStatus());
            holder.mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    boolean nStatus = !item.getStatus();
                    StartupAwakedAppListActivity.this.mConfirmDialog = ModifyConfirmDialog.createNewItemOpDialog(StartupAwakedAppListActivity.this.getSelf(), StartupAwakedAppListActivity.this.getSelf(), position, nStatus);
                    if (nStatus || !PaymentPkgChecker.isPaymentPkg(item.getPackageName())) {
                        StartupAwakedAppListActivity.this.mConfirmDialog.setTitle(item.getLabel());
                        StartupAwakedAppListActivity.this.mConfirmDialog.setMessage(StartupAwakedAppListActivity.this.getAwakedConfirmMsg(nStatus, item));
                    } else {
                        StartupAwakedAppListActivity.this.mConfirmDialog.setTitle((int) R.string.startupmgr_forbidden_awake);
                        if (StartupAwakedAppListActivity.this.isPackageCloned(item)) {
                            StartupAwakedAppListActivity.this.mConfirmDialog.setMessage(StartupAwakedAppListActivity.highlightRed(R.string.startupmgr_pay_warnning_ensure_for_dual_app));
                        } else {
                            StartupAwakedAppListActivity.this.mConfirmDialog.setMessage(StartupAwakedAppListActivity.highlightRed(R.string.startupmgr_pay_warnning_ensure));
                        }
                    }
                    StartupAwakedAppListActivity.this.mConfirmDialog.show();
                }
            });
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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mAppContext = getApplicationContext();
        this.mHandlerThread.start();
        this.mHandler = new AwakedHandler(this.mHandlerThread.getLooper());
        setContentView(R.layout.startupmgr_awaked_app_list);
        findViewAndInitSetting();
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
        showProgressBar(true);
        this.mAsyncLoader = new AwakedListLoader();
        this.mAsyncLoader.execute(new Void[0]);
        this.mNotifySwitcher.refreshState();
    }

    protected void onDestroy() {
        this.mHandlerThread.quit();
        ModifyConfirmDialog.dismiss(this);
        super.onDestroy();
    }

    public void allOpSwitchChanged(boolean isChecked) {
        HwLog.i(TAG, "allOpSwitchChanged " + isChecked);
        modifyAllCheckStatus(isChecked);
        updateTipView();
        updateAllOpSwitch();
    }

    public void allOpSwitchChangeCancelled(boolean isChecked) {
        HwLog.i(TAG, "allOpSwitchChangeCancelled " + isChecked);
        updateAllOpSwitch();
    }

    public void itemSwitchChanged(int position, boolean isChecked) {
        modifyItemCheckStatus(position, isChecked);
        updateTipView();
        updateAllOpSwitch();
    }

    public void itemSwitchChangeCancelled(int position, boolean isChecked) {
        HwLog.i(TAG, "itemSwitchChangeCancelled " + isChecked + ", position: " + position);
        this.mAdapter.notifyDataSetChanged();
    }

    private void findViewAndInitSetting() {
        this.mProgressBarLayout = findViewById(R.id.startupmgr_awaked_app_loading);
        this.mAllowedCountView = (TextView) findViewById(R.id.autostart_awaked_allowed_count_tip);
        RelativeLayout allOpSwitchLayout = (RelativeLayout) findViewById(R.id.all_op_switch_layout);
        ((TextView) allOpSwitchLayout.findViewById(ViewUtil.HWID_TEXT_1)).setText(R.string.startupmgr_all_op);
        this.mAllOpSwitch = new CommonSwitchController(allOpSwitchLayout, (Switch) allOpSwitchLayout.findViewById(R.id.switcher), true);
        this.mListView = (ListView) findViewById(R.id.startupmgr_awaked_list_view);
        this.mListView.setItemsCanFocus(false);
        this.mAdapter = new AwakedStartupInfoAdapter(this.mAppContext);
        this.mListView.setAdapter(this.mAdapter);
        this.mListView.setOnItemClickListener(new SwitchItemClickListener());
        this.mStartupAppView = (LinearLayout) findViewById(R.id.startup_app_awake_view);
        this.mNoStartupAppAwakeLayout = findViewById(R.id.no_startup_app_awake_layout);
        this.mRecordLayout = (RelativeLayout) findViewById(R.id.startupmgr_awaked_record_layout);
        this.mRecordDivider = findViewById(R.id.startupmgr_awaked_record_layout_divider);
        this.mRecordLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                StartupAwakedAppListActivity.this.startActivity(new Intent(StartupAwakedAppListActivity.this.mAppContext, StartupAwakedRecordActivity.class));
            }
        });
        this.mNotifySwitcher = new AwakedAppNotifySwitcher(findViewById(R.id.toast_switch_layout));
        this.mNotifySwitcher.init();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            this.mHeight1 = DimensionUtils.getAreaOne(this);
            this.mHeight2 = DimensionUtils.getAreaThree(this);
            LayoutParams layoutParams = (LayoutParams) this.mNoStartupAppAwakeLayout.getLayoutParams();
            layoutParams.topMargin = ((int) (((double) this.mHeight1) * 0.3d)) - (this.mHeight1 - this.mHeight2);
            this.mNoStartupAppAwakeLayout.setLayoutParams(layoutParams);
        }
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
        View view2 = this.mNoStartupAppAwakeLayout;
        if (!isShowNoView) {
            i2 = 8;
        }
        view2.setVisibility(i2);
        invalidateOptionsMenu();
    }

    private StartupAwakedAppListActivity getSelf() {
        return this;
    }

    private void updateTipView() {
        if (this.mAllowCount == 0) {
            this.mAllowedCountView.setText(R.string.startupmgr_awaked_count_tip_zero);
            return;
        }
        this.mAllowedCountView.setText(this.mAppContext.getResources().getQuantityString(R.plurals.startupmgr_awaked_count_tip_not_zero_content, this.mAllowCount, new Object[]{Integer.valueOf(this.mAllowCount), this.mAppContext.getResources().getString(R.string.device_name)}));
    }

    private void updateAllOpSwitch() {
        this.mAllOpSwitch.setOnCheckedChangeListener(null);
        this.mAllOpSwitch.updateCheckState(this.mAllowCount == this.mStartupInfoList.size());
        this.mAllOpSwitch.setOnCheckedChangeListener(this.mAllSwitchCheckedChangeListener);
    }

    private void showProgressBar(boolean show) {
        if (this.mProgressBarLayout != null) {
            this.mProgressBarLayout.setVisibility(show ? 0 : 8);
        }
    }

    private void modifyAllowCount(boolean isAdd) {
        if (isAdd) {
            this.mAllowCount++;
        } else {
            this.mAllowCount--;
        }
    }

    private void resetAllowCount() {
        this.mAllowCount = 0;
    }

    private void modifyAllCheckStatus(boolean isChecked) {
        for (AwakedStartupInfo info : this.mStartupInfoList) {
            if (info.getStatus() != isChecked) {
                info.setStatus(isChecked);
                modifyAllowCount(isChecked);
            }
        }
        sendWriteStatusInfoListMsg(this.mStartupInfoList);
        this.mAdapter.notifyDataSetChanged();
    }

    private void modifyItemCheckStatus(int position, boolean isChecked) {
        AwakedStartupInfo info = (AwakedStartupInfo) this.mStartupInfoList.get(position);
        if (info == null) {
            HwLog.e(TAG, "modifyItemCheckStatus can't find item of position: " + position);
            return;
        }
        HwLog.i(TAG, "itemSwitchChanged " + isChecked + ", position: " + position + ", pkg:" + info.getPackageName());
        info.setStatus(isChecked);
        modifyAllowCount(isChecked);
        sendWriteSingleStatusInfoMsg(info);
        this.mAdapter.notifyDataSetChanged();
    }

    private String getAwakedConfirmMsg(boolean isChecked, AwakedStartupInfo info) {
        int resId;
        if (info.isCompetitor()) {
            if (isChecked) {
                resId = R.plurals.startupmgr_awaked_allow_compitition_msg_content;
            } else {
                resId = R.plurals.startupmgr_awaked_forbid_compitition_msg_content;
            }
        } else if (!isChecked) {
            resId = R.plurals.startupmgr_awaked_forbid_msg_content;
        } else if (isPackageCloned(info)) {
            resId = R.plurals.startupmgr_awaked_allow_msg_head_for_dual_app_content;
        } else {
            resId = R.plurals.startupmgr_awaked_allow_msg_content;
        }
        return this.mAppContext.getResources().getQuantityString(resId, info.getCallerCount(), new Object[]{info.getLabel(), Integer.valueOf(info.getCallerCount()), this.mAppContext.getResources().getString(R.string.device_name)}) + info.getCombinedLabelString(this.mAppContext);
    }

    private boolean isPackageCloned(AwakedStartupInfo info) {
        if (info == null) {
            return false;
        }
        return DualAppUtil.isPackageCloned(this, info.getPackageName());
    }

    private void sendWriteSingleStatusInfoMsg(AwakedStartupInfo info) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(0, info));
    }

    private void sendWriteStatusInfoListMsg(List<AwakedStartupInfo> infoList) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1, infoList));
    }

    private void handleWriteSingleStatusInfoMsg(AwakedStartupInfo info) {
        info.setUserHasChanged();
        info.persistStatusData(this.mAppContext, true);
        String[] strArr = new String[4];
        strArr[0] = HsmStatConst.PARAM_OP;
        strArr[1] = info.getStatus() ? "1" : "0";
        strArr[2] = HsmStatConst.PARAM_PKG;
        strArr[3] = info.getPackageName();
        HsmStat.statE(1003, strArr);
    }

    private void handleWriteStatusInfoListMsg(List<AwakedStartupInfo> infoList) {
        List<HwPFWStartupSetting> settingList = Lists.newArrayList();
        for (AwakedStartupInfo info : infoList) {
            info.setUserHasChanged();
            info.persistStatusData(this.mAppContext, false);
            settingList.add(info.getPFWStartupSetting());
        }
        StartupBinderAccess.writeStartupSettingList(settingList, false);
        if (!infoList.isEmpty()) {
            String[] strArr = new String[2];
            strArr[0] = HsmStatConst.PARAM_OP;
            strArr[1] = ((AwakedStartupInfo) infoList.get(0)).getStatus() ? "1" : "0";
            HsmStat.statE(1004, strArr);
        }
    }

    public boolean isSupprotMultiUser() {
        return false;
    }

    private static final CharSequence highlightRed(int resId) {
        String text = GlobalContext.getString(resId);
        if (TextUtils.isEmpty(text)) {
            return "";
        }
        return StringUtils.highlight(text, 0, text.length(), SupportMenu.CATEGORY_MASK);
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
}
