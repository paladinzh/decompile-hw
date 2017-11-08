package com.huawei.systemmanager.optimize.process;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import com.common.imageloader.core.DisplayImageOptions;
import com.common.imageloader.core.DisplayImageOptions.Builder;
import com.common.imageloader.core.ImageLoader;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.widget.CommonAdapter;
import com.huawei.systemmanager.comm.widget.CommonSwitchController;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.power.comm.SpecialAPpList;
import com.huawei.systemmanager.power.util.SavingSettingUtil;
import com.huawei.systemmanager.shortcut.HeaderGridView;
import com.huawei.systemmanager.util.HSMConst;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class ProtectActivity extends HsmActivity implements IDataChangedListener {
    public static final String COMEFROM = "comefrom";
    private static final int MSG_PACKAGE_ADD = 4;
    private static final int MSG_PACKAGE_REMOVE = 5;
    private static final int MSG_START_LOAD_TASK = 3;
    public static final String POWER = "powerprotect";
    private static final String PROTECT_DIALOG_CANCLE = "0";
    private static final String PROTECT_DIALOG_ENABLE = "1";
    private static final String TAG = "ProtectActivity";
    public static final String TITLE = null;
    private static final int ZERO_NUM = 0;
    private static DisplayImageOptions options = new Builder().useApkDecoder(true).loadDrawableByPkgInfo(true).cacheInMemory(true).cacheOnDisk(false).showImageOnLoading(17301651).considerExifParams(true).resetViewBeforeLoading(true).build();
    private ProtectListAdapter mAdapter;
    private OnCheckedChangeListener mAllCheckClicker = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            String statParam = "";
            if (isChecked) {
                ProtectActivity.this.createDialog(ProtectActivity.this);
                if (!TextUtils.isEmpty(HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "1"))) {
                    HsmStat.statE((int) Events.E_PROTECTED_APP_SET_ALL, HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "1"));
                    return;
                }
                return;
            }
            ProtectActivity.this.doCancelAll();
            statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "0");
            ProtectActivity.this.onListDataChanged();
            if (!TextUtils.isEmpty(statParam)) {
                HsmStat.statE((int) Events.E_PROTECTED_APP_SET_ALL, statParam);
            }
        }
    };
    private View mEmptyView = null;
    private ViewStub mEmptyViewStub = null;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 3:
                    ProtectActivity.this.startLoadData();
                    return;
                case 4:
                    ProtectActivity.this.mShowList.add(msg.obj);
                    ProtectActivity.this.onListDataChanged();
                    return;
                case 5:
                    String pkgName = msg.obj;
                    Iterator<ProtectAppItem> it = ProtectActivity.this.mShowList.iterator();
                    while (it.hasNext()) {
                        if (Objects.equal(pkgName, ((ProtectAppItem) it.next()).getPackageName())) {
                            it.remove();
                            ProtectActivity.this.onListDataChanged();
                            ProtectActivity.this.checkEmptyView();
                            return;
                        }
                    }
                    ProtectActivity.this.onListDataChanged();
                    ProtectActivity.this.checkEmptyView();
                    return;
                default:
                    return;
            }
        }
    };
    private MyDataLoaderTask mLoadTask;
    private CommonSwitchController mMainSwitcher;
    private MenuItem mMenuSetting = null;
    private boolean mPowerSavingProtect = false;
    private ProgressBar mProgressbar = null;
    private HeaderGridView mProtectAppGridView = null;
    private LinearLayout mProtectAppLayout = null;
    private ProtectAppControl mProtectListControl;
    private final List<ProtectAppItem> mShowList = Lists.newArrayList();
    private View protectHeaderView = null;

    class MyDataLoaderTask extends AsyncTask<Void, Void, List<ProtectAppItem>> {
        private Context mCtx;

        public MyDataLoaderTask(Context ctx) {
            this.mCtx = ctx;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            ProtectActivity.this.mProgressbar.setVisibility(0);
        }

        protected void onCancelled() {
            super.onCancelled();
            ProtectActivity.this.mProgressbar.setVisibility(8);
        }

        protected List<ProtectAppItem> doInBackground(Void... params) {
            if (ProtectActivity.this.mProtectListControl == null) {
                ProtectActivity.this.mProtectListControl = ProtectAppControl.getInstance(this.mCtx);
            }
            if (isCancelled()) {
                HwLog.i(ProtectActivity.TAG, "MyDataLoaderTask doInBackground is canceled");
                return null;
            }
            List<ProtectAppItem> result = ProtectActivity.this.mProtectListControl.getProtectAppItems();
            for (ProtectAppItem item : result) {
                item.getIcon();
                item.getName();
            }
            Collections.sort(result, ProtectAppItem.PROTECT_APP_COMPARATOR);
            return result;
        }

        protected void onPostExecute(List<ProtectAppItem> result) {
            if (isCancelled()) {
                HwLog.i(ProtectActivity.TAG, "MyDataLoaderTask onPostExecute is canceled");
                return;
            }
            ProtectActivity.this.mShowList.clear();
            ProtectActivity.this.mShowList.addAll(result);
            HSMConst.doMultiply(ProtectActivity.this.getApplicationContext(), ProtectActivity.this.getResources().getConfiguration().orientation == 2, ProtectActivity.this.mProtectAppGridView);
            ProtectActivity.this.onListDataChanged();
            ProtectActivity.this.checkEmptyView();
        }
    }

    private class ProtectListAdapter extends CommonAdapter<ProtectAppItem> {
        private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ProtectAppItem item = (ProtectAppItem) buttonView.getTag();
                if (ProtectActivity.this.mProtectListControl == null) {
                    ProtectActivity.this.mProtectListControl = ProtectAppControl.getInstance(ProtectActivity.this);
                }
                if (item.isProtect()) {
                    if (ProtectActivity.this.mProtectListControl != null) {
                        ProtectActivity.this.mProtectListControl.setProtect(item, false);
                    }
                } else if (ProtectActivity.this.mProtectListControl != null) {
                    ProtectActivity.this.mProtectListControl.setProtect(item, true);
                }
                ProtectListAdapter.this.notifyDataSetChanged();
                ProtectActivity.this.updateCheckedNumber();
                buttonView.sendAccessibilityEvent(8);
                String[] strArr = new String[4];
                strArr[0] = HsmStatConst.PARAM_PKG;
                strArr[1] = item.getPackageName();
                strArr[2] = HsmStatConst.PARAM_OP;
                strArr[3] = item.isProtect() ? "1" : "0";
                HsmStat.statE((int) Events.E_PROTECTED_APP_SET, HsmStatConst.constructJsonParams(strArr));
            }
        };
        private int mTitleMaxWidth = getResources().getDimensionPixelSize(R.dimen.phone_optimize_protecte_item_title_maxwidth);
        private int mTitleMaxWidthInPowerCost = getResources().getDimensionPixelSize(R.dimen.phone_optimize_protecte_item_title_maxwidth2);

        public ProtectListAdapter(Context context) {
            super(context);
        }

        protected View newView(int position, ViewGroup parent, ProtectAppItem item) {
            View convertView = this.mInflater.inflate(R.layout.protect_list_item, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.mIcon = (ImageView) convertView.findViewById(R.id.white_app_item_icon);
            holder.mLabel = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_1);
            holder.mStatus = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_2);
            holder.mStatus.setSingleLine(false);
            holder.mSwitch = (Switch) convertView.findViewById(R.id.switcher);
            holder.mHighPower = (TextView) convertView.findViewById(R.id.high_power);
            convertView.setTag(holder);
            return convertView;
        }

        protected void bindView(int position, View view, ProtectAppItem item) {
            int subTextId;
            ViewHolder holder = (ViewHolder) view.getTag();
            String label = item.getName();
            ImageLoader.getInstance().displayImage(item.getPackageName(), holder.mIcon, ProtectActivity.options, null);
            holder.mLabel.setText(label);
            if (item.isHighPower()) {
                holder.mHighPower.setVisibility(0);
                holder.mHighPower.setText(R.string.app_high_power_cost);
                holder.mLabel.setMaxWidth(this.mTitleMaxWidthInPowerCost);
            } else {
                holder.mHighPower.setText("");
                holder.mLabel.setMaxWidth(this.mTitleMaxWidth);
            }
            holder.mSwitch.setOnCheckedChangeListener(null);
            holder.mSwitch.setChecked(!item.isProtect());
            holder.mSwitch.setTag(item);
            holder.mSwitch.setOnCheckedChangeListener(this.mOnCheckedChangeListener);
            int subTextColor = 0;
            Resources resources = getResources();
            int colorID = resources.getIdentifier("secondary_text_emui", "color", "androidhwext");
            if (colorID > 0) {
                subTextColor = resources.getColor(colorID);
            }
            if (item.isProtect()) {
                subTextId = R.string.power_locked_save_list_uncleand_item;
            } else if (SpecialAPpList.isSpecialApp(item.getPackageName())) {
                subTextId = R.string.power_lock_clean_app_not_protected_special;
                subTextColor = getResources().getColor(R.color.protect_item_red_color);
            } else {
                subTextId = R.string.power_locked_save_list_cleand_item;
            }
            if (colorID > 0) {
                holder.mStatus.setTextColor(subTextColor);
            }
            holder.mStatus.setText(subTextId);
        }
    }

    private static class SwitchItemClickListener implements OnItemClickListener {
        private SwitchItemClickListener() {
        }

        public void onItemClick(AdapterView<?> adapterView, View arg1, int arg2, long arg3) {
            Switch sw = (Switch) arg1.findViewById(R.id.switcher);
            if (sw != null) {
                sw.performClick();
            }
        }
    }

    private static class ViewHolder {
        TextView mHighPower;
        ImageView mIcon;
        TextView mLabel;
        TextView mStatus;
        Switch mSwitch;

        private ViewHolder() {
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.protect_app_activity);
        checkFrom();
        this.mProgressbar = (ProgressBar) findViewById(R.id.loading_progressbar);
        this.mProtectAppLayout = (LinearLayout) findViewById(R.id.protect_app_list_layout);
        this.mProtectAppGridView = (HeaderGridView) findViewById(R.id.progress_manager_white_gridview);
        View HeaderLayout = LayoutInflater.from(this).inflate(R.layout.protect_app_header_view, this.mProtectAppGridView, false);
        this.mMainSwitcher = new CommonSwitchController(HeaderLayout.findViewById(R.id.allcheck_container), (Switch) HeaderLayout.findViewById(R.id.main_switcher));
        this.mMainSwitcher.setOnCheckedChangeListener(this.mAllCheckClicker);
        this.mAdapter = new ProtectListAdapter(this);
        boolean isLand = 2 == getResources().getConfiguration().orientation;
        this.protectHeaderView = HeaderLayout.findViewById(R.id.protect_app_header_view);
        this.mProtectAppGridView.addHeaderView(this.protectHeaderView);
        this.mProtectAppGridView.setAdapter(this.mAdapter);
        HSMConst.doMultiply(getApplicationContext(), isLand, this.mProtectAppGridView);
        this.mProtectAppGridView.setOnItemClickListener(new SwitchItemClickListener());
        AbsProtectAppControl.registerListener(this);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.systemmanager_module_title_lockscreencleanup);
        setTitle(R.string.systemmanager_module_title_lockscreencleanup);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        HSMConst.doMultiply(getApplicationContext(), newConfig.orientation == 2, this.mProtectAppGridView);
    }

    protected void onResume() {
        super.onResume();
        startLoadData();
    }

    protected void initEmptyView() {
        if (this.mEmptyView == null) {
            this.mEmptyViewStub = (ViewStub) findViewById(R.id.no_protect_app_stub);
            if (this.mEmptyViewStub != null) {
                this.mEmptyViewStub.inflate();
                this.mEmptyView = findViewById(R.id.no_protect_app);
            }
        }
    }

    protected void onDestroy() {
        AbsProtectAppControl.unregisterListener(this);
        super.onDestroy();
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        changeMenuStatus();
        return super.onPrepareOptionsMenu(menu);
    }

    private void doSelectAll() {
        List<String> pkgList = new ArrayList();
        for (ProtectAppItem info : this.mShowList) {
            if (info.isProtect()) {
                pkgList.add(info.getPackageName());
                info.setProtect(false);
            }
        }
        if (this.mProtectListControl != null) {
            this.mProtectListControl.setNoProtect(pkgList);
        }
    }

    private void doCancelAll() {
        List<String> pkgList = new ArrayList();
        for (ProtectAppItem info : this.mShowList) {
            if (!info.isProtect()) {
                pkgList.add(info.getPackageName());
                info.setProtect(true);
            }
        }
        if (this.mProtectListControl != null) {
            this.mProtectListControl.setProtect(pkgList);
        }
    }

    private void changeMenuStatus() {
        if (this.mMenuSetting == null) {
            return;
        }
        if (this.mShowList == null || this.mShowList.isEmpty()) {
            this.mMenuSetting.setVisible(false);
        } else {
            this.mMenuSetting.setVisible(true);
        }
    }

    private void onListDataChanged() {
        notifyAdapter();
        changeMenuStatus();
        updateCheckedNumber();
    }

    private void updateCheckedNumber() {
        boolean z = false;
        int number = 0;
        for (ProtectAppItem item : this.mShowList) {
            if (item.isProtect()) {
                number++;
            }
        }
        if (this.mMainSwitcher != null) {
            CommonSwitchController commonSwitchController = this.mMainSwitcher;
            if (number == 0) {
                z = true;
            }
            commonSwitchController.updateCheckState(z);
        }
    }

    private void notifyAdapter() {
        if (this.mAdapter != null) {
            this.mAdapter.swapData(this.mShowList);
        }
    }

    private void checkEmptyView() {
        int i;
        this.mProgressbar.setVisibility(8);
        boolean isShowNoView = this.mShowList.size() == 0;
        LinearLayout linearLayout = this.mProtectAppLayout;
        if (isShowNoView) {
            i = 8;
        } else {
            i = 0;
        }
        linearLayout.setVisibility(i);
        if (isShowNoView) {
            initEmptyView();
            if (this.mEmptyView != null) {
                this.mEmptyView.setVisibility(0);
            }
        } else if (this.mEmptyView != null) {
            this.mEmptyView.setVisibility(8);
        }
    }

    public void onPackageAdded(String pkgName, boolean protect) {
        if (pkgName != null) {
            HashSet<String> powerCostSet = SavingSettingUtil.getRogueAppSet(this);
            HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfo(pkgName);
            if (info == null) {
                HwLog.i(TAG, "can not find pkg:" + pkgName);
                return;
            }
            this.mHandler.obtainMessage(4, new ProtectAppItem(info, protect, powerCostSet.contains(pkgName))).sendToTarget();
        }
    }

    public void onPackageRemoved(String pkgName) {
        if (pkgName != null) {
            this.mHandler.obtainMessage(5, pkgName).sendToTarget();
        }
    }

    private void checkFrom() {
        Intent intent = getIntent();
        if (intent != null) {
            Bundle data = intent.getExtras();
            Object from = null;
            if (data != null) {
                from = data.getString("comefrom");
            }
            if (POWER.equals(from)) {
                this.mPowerSavingProtect = true;
            }
        }
    }

    public void onProtectedAppRefresh() {
        this.mHandler.removeMessages(3);
        this.mHandler.sendEmptyMessage(3);
    }

    private synchronized void startLoadData() {
        HwLog.i(TAG, "startLoadData");
        if (this.mLoadTask != null) {
            this.mLoadTask.cancel(true);
        }
        this.mLoadTask = new MyDataLoaderTask(getApplicationContext());
        this.mLoadTask.execute(new Void[0]);
    }

    private void createDialog(Context context) {
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(context);
        dlgBuilder.setTitle(R.string.power_lock_clean_dialog_title);
        dlgBuilder.setMessage(R.string.power_lock_clean_dialog_message);
        dlgBuilder.setCancelable(false);
        dlgBuilder.setNegativeButton(R.string.cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ProtectActivity.this.updateCheckedNumber();
                String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "0");
                HsmStat.statE((int) Events.E_POWER_PROTECT_DIALOG_ENTER, statParam);
            }
        });
        dlgBuilder.setPositiveButton(R.string.power_lock_clean_dialog_selectall, new OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ProtectActivity.this.doSelectAll();
                String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "1");
                ProtectActivity.this.onListDataChanged();
                if (!TextUtils.isEmpty(statParam)) {
                    HsmStat.statE((int) Events.E_PROTECTED_APP_SET_ALL, statParam);
                }
                String statParam1 = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "1");
                HsmStat.statE((int) Events.E_POWER_PROTECT_DIALOG_ENTER, statParam1);
            }
        });
        AlertDialog alertDialog = dlgBuilder.create();
        alertDialog.show();
        alertDialog.getButton(-1).setTextColor(getResources().getColor(R.color.protect_item_red_color));
    }
}
