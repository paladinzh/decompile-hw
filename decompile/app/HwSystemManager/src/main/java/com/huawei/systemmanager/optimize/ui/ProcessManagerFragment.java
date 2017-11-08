package com.huawei.systemmanager.optimize.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.GenericHandler;
import com.huawei.systemmanager.comm.component.GenericHandler.MessageHandler;
import com.huawei.systemmanager.comm.component.SelectListFragment;
import com.huawei.systemmanager.comm.concurrent.HsmAsyncTask;
import com.huawei.systemmanager.comm.misc.Constant;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.slideview.SlidingUpPanelLayout;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.optimize.MemoryManager;
import com.huawei.systemmanager.optimize.MemoryManager.HsmMemoryInfo;
import com.huawei.systemmanager.optimize.ProcessManager;
import com.huawei.systemmanager.optimize.ProcessManagerSettingActivity;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;
import com.huawei.systemmanager.optimize.process.ProcessFilterPolicy;
import com.huawei.systemmanager.optimize.process.ProtectAppControl;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProcessManagerFragment extends SelectListFragment<ProcessAppItem> implements MessageHandler {
    private static final int CONTEXT_MENU_DETAIL = 6;
    private static final int CONTEXT_MENU_KILL_APP = 5;
    private static final int CONTEXT_MENU_NO_PROTECT = 4;
    private static final int CONTEXT_MENU_PROTECT = 3;
    private static final int MSG_UPDATE_MEMORY_USE = 2;
    private static final long READ_MEMORY_SLEEP_TIME = 500;
    private static final int REUEST_CODE_DETAIL = 5;
    private static final int SLOW_PERCENT = 75;
    public static final String TAG = "ProcessManagerFragment";
    private static final long UPDATE_MEMORY_USED_DELAY = 2000;
    private MenuItem mAllSelectItem;
    private ProcessManagerAdapter mAppListAdapter;
    private PmCircleViewController mCvController;
    private boolean mDataInitialed;
    private View mDragViewContainer;
    private final Handler mHandler = new GenericHandler(this);
    private String mJumpPkgName;
    private MenuItem mKillAppItem;
    private LayoutInflater mLayoutInflater;
    private TextView mMemoryView;
    private boolean mNeedLoadData = true;
    private TextView mProcessTitleView;
    private SlidingUpPanelLayout mSlidingUpPanelLayout;
    private TextView mSlowTipView;

    private class AppInfoLoadingTask extends AsyncTask<Void, Void, List<ProcessAppItem>> {
        private AppInfoLoadingTask() {
        }

        protected List<ProcessAppItem> doInBackground(Void... voidParams) {
            HwLog.i(ProcessManagerFragment.TAG, "AppInfoLoadingTask start");
            long start = SystemClock.elapsedRealtime();
            List<ProcessAppItem> list = ProcessFilterPolicy.getRunningApps(ProcessManagerFragment.this.getApplicationContext());
            for (ProcessAppItem item : list) {
                item.getIcon();
            }
            HwLog.i(ProcessManagerFragment.TAG, "AppInfoLoadingTask cost time:" + (SystemClock.elapsedRealtime() - start));
            return list;
        }

        protected void onPostExecute(List<ProcessAppItem> data) {
            ProcessManagerFragment.this.swapAdapterData(data);
            ProcessManagerFragment.this.animationDragViewShow();
            HwLog.i(ProcessManagerFragment.TAG, "AppInfoLoadingTask sucess");
        }
    }

    private class ClearAppTask extends HsmAsyncTask<List<ProcessAppItem>, Void, Long> {
        private int mAppCount;

        private ClearAppTask() {
        }

        protected Long doInBackground(List<ProcessAppItem>... params) {
            if (params == null || params.length <= 0) {
                return null;
            }
            List<ProcessAppItem> processApplist = params[0];
            this.mAppCount = processApplist.size();
            long memory = 0;
            ArrayList<String> pkgList = Lists.newArrayListWithCapacity(this.mAppCount);
            for (ProcessAppItem item : processApplist) {
                memory += item.getMemoryCost();
                pkgList.add(item.getPackageName());
            }
            ProcessManager.clearPackages(pkgList);
            return Long.valueOf(memory);
        }

        protected void onSuccess(Long aLong) {
            if (aLong == null) {
                HwLog.e(ProcessManagerFragment.TAG, "ClearAppTask result is null!");
            } else {
                ProcessManagerFragment.this.updateMemoryUsedInfo(true);
            }
        }
    }

    private class RefreshMemoryTask extends AsyncTask<Void, Void, HsmMemoryInfo> {
        private RefreshMemoryTask() {
        }

        protected HsmMemoryInfo doInBackground(Void... params) {
            HwLog.i(ProcessManagerFragment.TAG, "RefreshMemoryTask start");
            Context ctx = ProcessManagerFragment.this.getApplicationContext();
            long start = SystemClock.elapsedRealtime();
            HsmMemoryInfo info = MemoryManager.getMemoryInfo(ctx);
            long cost = SystemClock.elapsedRealtime() - start;
            HwLog.i(ProcessManagerFragment.TAG, "RefreshMemoryTask cost time:" + cost);
            long sleeptime = Math.max(500 - cost, 0);
            if (sleeptime > 0 && sleeptime <= 500) {
                try {
                    Thread.sleep(sleeptime);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                HwLog.i(ProcessManagerFragment.TAG, "RefreshMemoryTask sleep:" + sleeptime);
            }
            return info;
        }

        protected void onPostExecute(HsmMemoryInfo result) {
            ProcessManagerFragment.this.refreshMemoryUI(result);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.progress_manager_fragment, container, false);
        this.mLayoutInflater = inflater;
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mAppListAdapter = new ProcessManagerAdapter(getApplicationContext(), this.mLayoutInflater);
        setListAdapter(this.mAppListAdapter);
        getListView().setTag(Constant.DISALBE_LISTVIEW_CHECKOBX_MULTI_SELECT);
        this.mProcessTitleView = (TextView) view.findViewById(R.id.progress_manager_running_app_info);
        this.mMemoryView = (TextView) view.findViewById(R.id.memory_view);
        this.mSlowTipView = (TextView) view.findViewById(R.id.process_manager_slow_tips);
        this.mDragViewContainer = view.findViewById(R.id.dragview_container);
        this.mSlidingUpPanelLayout = (SlidingUpPanelLayout) view.findViewById(R.id.sliding_layout);
        setEmptyTextAndImage(R.string.Other_Accelerater_Tip03, R.drawable.ic_no_apps);
        this.mCvController = new PmCircleViewController(view);
        registerForContextMenu(getListView());
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.process_manager_menu, menu);
        this.mAllSelectItem = menu.findItem(R.id.menu_select_all);
        this.mKillAppItem = menu.findItem(R.id.menu_close);
        if (getAdapter() != null) {
            updateSelectState();
        }
    }

    public void onHandleMessage(Message msg) {
        switch (msg.what) {
            case 2:
                refreshMomeryAsync();
                return;
            default:
                return;
        }
    }

    private void loadData() {
        refreshMomeryAsync();
        refreshAppDataAsync();
        this.mDataInitialed = true;
    }

    private void refreshMomeryAsync() {
        HwLog.i(TAG, "refreshMomeryAsync() called");
        new RefreshMemoryTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    private void refreshMemoryUI(HsmMemoryInfo data) {
        if (data == null) {
            HwLog.w(TAG, "refreshMemoryUI data is null");
            return;
        }
        long used = data.getUsed();
        long total = data.getTotal();
        int usedPerecent = data.getUsedPercent();
        HwLog.i(TAG, "refreshMemoryUI called, usedPercent=" + usedPerecent + ",total size=" + total + ",used size=" + used);
        Context ctx = GlobalContext.getContext();
        String usedText = Formatter.formatFileSize(ctx, used);
        String totalText = Formatter.formatFileSize(ctx, total);
        if (this.mMemoryView != null) {
            this.mMemoryView.setText(getStringEx(R.string.common_precent_value, usedText, totalText));
        }
        if (this.mCvController != null) {
            this.mCvController.setDataWithAnima(usedPerecent);
        } else {
            HwLog.e(TAG, "mCvController is null !");
        }
        if (this.mSlowTipView != null) {
            int i;
            TextView textView = this.mSlowTipView;
            if (usedPerecent >= 75) {
                i = R.string.Other_Accelerater_Tip01;
            } else {
                i = R.string.health_right;
            }
            textView.setText(i);
        }
    }

    private void terminateSelectApp() {
        List<ProcessAppItem> checkedList = getCheckedList();
        doStat(checkedList);
        terminateApp(checkedList);
    }

    private void doStat(List<ProcessAppItem> checkedList) {
        if (!Utility.isNullOrEmptyList(checkedList)) {
            StringBuilder sb = new StringBuilder();
            for (ProcessAppItem item : checkedList) {
                sb.append(item.getPackageName());
                sb.append(" ");
            }
            String pkgs = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_PKGS, sb.toString());
            if (isAllChecked()) {
                HsmStat.statE(12, pkgs);
            } else {
                HsmStat.statE(13, pkgs);
            }
        }
    }

    private void terminateSingleApp(ProcessAppItem item) {
        terminateApp(Lists.newArrayList(item));
    }

    private void terminateApp(List<ProcessAppItem> deleteList) {
        ArrayList<ProcessAppItem> showingList = Lists.newArrayList(getData());
        showingList.removeAll(deleteList);
        swapAdapterData(showingList);
        new ClearAppTask().executeParallel(deleteList);
    }

    private void updateMemoryUsedInfo(boolean immediately) {
        if (immediately) {
            this.mHandler.removeMessages(2);
            this.mHandler.sendEmptyMessage(2);
            return;
        }
        this.mHandler.sendEmptyMessageDelayed(2, UPDATE_MEMORY_USED_DELAY);
    }

    private void refreshAppDataAsync() {
        HwLog.i(TAG, "refreshAppDataAsync() called");
        new AppInfoLoadingTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    protected void onAdapterDataChange() {
        int runningAppNumber = ((ProcessManagerAdapter) getAdapter()).getTotalTaskNum();
        String tips = getResourcesEx().getQuantityString(R.plurals.ListViewFirstLine_Accelerater_Tips01, runningAppNumber, new Object[]{Integer.valueOf(runningAppNumber)});
        if (this.mProcessTitleView != null) {
            this.mProcessTitleView.setText(tips);
        }
        updateSelectState();
    }

    public void onResume() {
        super.onResume();
        if (this.mNeedLoadData) {
            if (this.mSlidingUpPanelLayout != null && this.mSlidingUpPanelLayout.isExpanded()) {
                showLoadingView();
            }
            loadData();
            return;
        }
        HwLog.i(TAG, "onResume, need not load data");
        this.mNeedLoadData = true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_select_all:
                HwLog.i(TAG, "select all menu clicked!");
                clickAllSelect();
                break;
            case R.id.menu_close:
                HwLog.i(TAG, "close menu clicked!");
                terminateSelectApp();
                updateMemoryUsedInfo(false);
                break;
            case R.id.process_manager_setting_menu:
                HwLog.i(TAG, "setting menu clicked!");
                Activity ac = getActivity();
                if (ac != null) {
                    startActivity(new Intent(ac, ProcessManagerSettingActivity.class));
                    break;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onCheckNumChanged(int allNum, int checkedAbleNum, int checkNum, boolean allChecked) {
        if (this.mAllSelectItem == null || this.mKillAppItem == null) {
            HwLog.w(TAG, "update menu failed! MenuItem is null");
            return;
        }
        boolean z;
        if (allChecked) {
            this.mAllSelectItem.setIcon(R.drawable.menu_check_pressed);
            this.mAllSelectItem.setTitle(R.string.unselect_all);
            this.mAllSelectItem.setChecked(true);
        } else {
            this.mAllSelectItem.setIcon(R.drawable.menu_check_status);
            this.mAllSelectItem.setTitle(R.string.select_all);
            this.mAllSelectItem.setChecked(false);
        }
        MenuItem menuItem = this.mAllSelectItem;
        if (checkedAbleNum != 0) {
            z = true;
        } else {
            z = false;
        }
        menuItem.setEnabled(z);
        menuItem = this.mKillAppItem;
        if (checkNum == 0) {
            z = false;
        } else {
            z = true;
        }
        menuItem.setEnabled(z);
        this.mKillAppItem.setTitle(getStringEx(R.string.Button_Accelerater_Close, Integer.valueOf(checkNum)));
    }

    private void animationDragViewShow() {
        if (this.mDragViewContainer == null) {
            HwLog.e(TAG, "animationDragViewShow, mDragViewContainer is null");
        } else if (this.mDragViewContainer.getVisibility() != 0) {
            Animation anima = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.process_manager_dragview_up);
            anima.setAnimationListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    ProcessManagerFragment.this.mSlidingUpPanelLayout.setShowShadow(true);
                }
            });
            this.mDragViewContainer.startAnimation(anima);
            this.mDragViewContainer.setVisibility(0);
        }
    }

    protected void onListItemClick(AdapterView<?> adapterView, View v, int position, long id) {
        ProcessAppItem item = (ProcessAppItem) getAdapter().getItem(position);
        if (item.isKeyProcess()) {
            jumpToDetailPage(item);
            return;
        }
        item.toggle();
        updateSelectState();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 5) {
            HwLog.i(TAG, "back from detail page, need not reload data");
            if (this.mDataInitialed) {
                this.mNeedLoadData = false;
                if (TextUtils.isEmpty(this.mJumpPkgName)) {
                    HwLog.e(TAG, "onActivityResult , but mJumpPkgName is empty!");
                    return;
                }
                boolean alive = ProcessFilterPolicy.queryIfAppAlive(getApplicationContext(), this.mJumpPkgName);
                HwLog.i(TAG, "pkg:" + this.mJumpPkgName + ", alive:" + alive);
                if (!alive) {
                    List<ProcessAppItem> list = Lists.newArrayList(getData());
                    Iterator<ProcessAppItem> it = list.iterator();
                    while (it.hasNext()) {
                        if (this.mJumpPkgName.equals(((ProcessAppItem) it.next()).getPackageName())) {
                            it.remove();
                            HwLog.i(TAG, "remove pkg:" + this.mJumpPkgName);
                            break;
                        }
                    }
                    swapAdapterData(list);
                } else {
                    return;
                }
            }
            HwLog.w(TAG, "the data not be initialed, just return!");
        }
    }

    private void jumpToDetailPage(ProcessAppItem item) {
        String pkg = item.getPackageName();
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.fromParts("package", pkg, null));
        this.mJumpPkgName = pkg;
        HwLog.i(TAG, "begin to jump detail page, pkgName:" + pkg);
        try {
            startActivityForResult(intent, 5);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            HwLog.e(TAG, "ActivityNotFoundException error! pkg=" + pkg);
        } catch (Exception e2) {
            e2.printStackTrace();
            HwLog.e(TAG, "Exception error! pkg=" + pkg);
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        ProcessAppItem item = (ProcessAppItem) getAdapter().getItem(((AdapterContextMenuInfo) menuInfo).position);
        HwLog.i(TAG, item.getPackageName() + " is selected!");
        menu.setHeaderTitle(item.getName());
        if (item.isKeyProcess()) {
            menu.add(0, 6, 0, R.string.process_manager_view_details);
            return;
        }
        if (item.isProtect()) {
            menu.add(0, 4, 0, R.string.process_manager_set_not_protected);
        } else {
            menu.add(0, 3, 0, R.string.process_manager_set_protected);
        }
        menu.add(0, 5, 0, R.string.process_manager_close_this_application);
        menu.add(0, 6, 0, R.string.process_manager_view_details);
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
        if (menuInfo == null) {
            HwLog.i(TAG, "onContextItemSelected, menuInfo is null!");
            return true;
        }
        int menuItemId = item.getItemId();
        HwLog.i(TAG, "onContextItemSelected, menuItemId:" + menuItemId);
        ProcessAppItem appItem = (ProcessAppItem) getAdapter().getItem(menuInfo.position);
        switch (menuItemId) {
            case 3:
                getProtectAppControl().setProtect(appItem, true);
                appItem.setChecked(false);
                updateSelectState();
                break;
            case 4:
                getProtectAppControl().setProtect(appItem, false);
                appItem.setChecked(true);
                updateSelectState();
                break;
            case 5:
                terminateSingleApp(appItem);
                break;
            case 6:
                jumpToDetailPage(appItem);
                break;
        }
        return true;
    }

    private ProtectAppControl getProtectAppControl() {
        return ProtectAppControl.getInstance(getApplicationContext());
    }
}
