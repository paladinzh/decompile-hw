package com.huawei.systemmanager.spacecleanner.ui.secondaryui;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.component.SelectListFragment;
import com.huawei.systemmanager.comm.misc.Constant;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.CommonHandler;
import com.huawei.systemmanager.spacecleanner.CommonHandler.MessageHandler;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.engine.base.CleanTask;
import com.huawei.systemmanager.spacecleanner.engine.base.ICleanListener;
import com.huawei.systemmanager.spacecleanner.engine.base.ICleanListener.SimpleListener;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.TrashGroup;
import com.huawei.systemmanager.spacecleanner.statistics.SpaceStatsUtils;
import com.huawei.systemmanager.spacecleanner.ui.StatisticalData;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.Convertor;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.OpenSecondaryParam;
import com.huawei.systemmanager.spacecleanner.ui.dialog.TrashDetailDialog;
import com.huawei.systemmanager.spacecleanner.ui.spacemanager.item.WeChatDeepItem;
import com.huawei.systemmanager.spacecleanner.ui.trashlistadapter.BaseTrashListAdapter;
import com.huawei.systemmanager.spacecleanner.ui.trashlistadapter.NoIconTrashListAdapter;
import com.huawei.systemmanager.spacecleanner.utils.TrashUtils;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ListTrashSetFragment extends SelectListFragment<ITrashItem> implements MessageHandler, OnClickListener {
    private static final int MSG_CLEAN_FINISH = 2;
    private static final int MSG_CLEAN_ITEM_FAIED = 4;
    private static final int MSG_CLEAN_START = 1;
    private static final int MSG_CLICK_TRASH_ITEM = 3;
    public static final String TAG = "ListTrashSetFragment";
    public static final Set<Integer> mOpenDetailEnbaleTrash = Collections.unmodifiableSet(getOpenDetailEnableSet());
    private MenuItem mAllCheckMenuItem;
    private BaseTrashListAdapter mAppDataSetAdapter;
    private long mCheckSize = 0;
    private ICleanListener mCleanListener = new SimpleListener() {
        public void onItemUpdate(Trash trash) {
            if (!trash.isCleaned()) {
                HwLog.e(ListTrashSetFragment.TAG, "clean failed!");
                ListTrashSetFragment.this.mHandle.obtainMessage(4, trash).sendToTarget();
            }
        }

        public void onCleanEnd(boolean canceled, long cleanedSize) {
            HwLog.i(ListTrashSetFragment.TAG, "onCleanEnd");
            ListTrashSetFragment.this.mHandle.obtainMessage(2, Long.valueOf(cleanedSize)).sendToTarget();
        }
    };
    private MenuItem mCleanMenuItem;
    private TrashDetailDialog mDetailDialog;
    private DialogInterface.OnClickListener mDialogListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case -1:
                    if (ListTrashSetFragment.this.mTrashListener != null) {
                        List<Trash> trashList = Convertor.covertTrashItem(ListTrashSetFragment.this.getCheckedList());
                        ListTrashSetFragment.this.mTrashListener.setCleanedOperation(true);
                        ListTrashSetFragment.this.mHandle.sendEmptyMessage(1);
                        ListTrashSetActivity activity = (ListTrashSetActivity) ListTrashSetFragment.this.getActivity();
                        if (activity != null) {
                            SpaceStatsUtils.reportDeepCleanTrashSize(activity.fromDeepManagerEnterence(), ListTrashSetFragment.this.mCheckSize);
                        }
                        CleanTask.startClean(ListTrashSetFragment.this.getApplicationContext(), trashList, ListTrashSetFragment.this.mCleanListener, ListTrashSetFragment.this.mTrashListener.getTrashHandler());
                        break;
                    }
                    HwLog.i(ListTrashSetFragment.TAG, "mTrashListener is null");
                    return;
            }
        }
    };
    private final Handler mHandle = new CommonHandler(this);
    private ListView mListView;
    private OnClickListener mOnClickListener = new OnClickListener() {
        public void onClick(View v) {
            ITrashItem item = (ITrashItem) v.getTag();
            if (item != null) {
                item.toggle();
                ListTrashSetFragment.this.updateSelectState();
            }
        }
    };
    private int mOperationTitleID = 0;
    private SecondaryDialogFragment mSecondaryDialogFragment;
    private StatisticalData mStatisticalData;
    private TextView mTipsView;
    private ArrayList<ITrashItem> mTotalList = Lists.newArrayList();
    private TrashDetailDialogFragment mTrashDetailDialogFragment;
    private TrashScanHandler mTrashHander;
    private onCallTrashSetListener mTrashListener;

    private static Set<Integer> getOpenDetailEnableSet() {
        Map<Integer, Object> map = HsmCollections.newArrayMap();
        map.put(Integer.valueOf(512), map);
        map.put(Integer.valueOf(256), map);
        map.put(Integer.valueOf(1024), map);
        map.put(Integer.valueOf(16384), map);
        map.put(Integer.valueOf(65536), map);
        map.put(Integer.valueOf(8192), map);
        return map.keySet();
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof onCallTrashSetListener) {
            this.mTrashListener = (onCallTrashSetListener) activity;
        } else {
            HwLog.d(TAG, "activity not have interface! ");
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HwLog.i(TAG, "onCreate");
        setHasOptionsMenu(true);
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mStatisticalData != null) {
            this.mStatisticalData.destroy();
        }
    }

    public void onResume() {
        super.onResume();
        updateSelectState();
        updateData();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.trash_list_set, container, false);
        this.mListView = (ListView) view.findViewById(R.id.content_list);
        View footerView = inflater.inflate(R.layout.blank_footer_view, this.mListView, false);
        this.mListView.setFooterDividersEnabled(false);
        this.mListView.addFooterView(footerView, null, false);
        DataHolder dataHolder = this.mTrashListener.getDataHolder();
        if (dataHolder != null && dataHolder.param.getTrashType() == 1048576) {
            this.mAppDataSetAdapter = new NoIconTrashListAdapter(this.mOnClickListener, getActivity());
        }
        this.mAppDataSetAdapter = this.mAppDataSetAdapter == null ? new BaseTrashListAdapter(this.mOnClickListener, getActivity()) : this.mAppDataSetAdapter;
        setListAdapter(this.mAppDataSetAdapter);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mTipsView = (TextView) view.findViewById(R.id.tips);
        AdapterView lv = getListView();
        if (lv != null) {
            lv.setTag(Constant.DISALBE_LISTVIEW_CHECKOBX_MULTI_SELECT);
        }
        initView();
        initData();
    }

    private void initView() {
        Activity ac = getActivity();
        if (ac == null) {
            HwLog.i(TAG, "Activity not found");
            return;
        }
        DataHolder dataHolder = this.mTrashListener.getDataHolder();
        if (dataHolder == null) {
            HwLog.e(TAG, "initView get dataHolder is null!");
            return;
        }
        OpenSecondaryParam param = dataHolder.param;
        this.mOperationTitleID = param.getOperationResId();
        ac.setTitle(param.getTitleStr());
        setEmptyTextAndImage(param.getEmptyTextID(), param.getEmptyIconID());
    }

    private void deleteFiles() {
        List<ITrashItem> checkedList = getCheckedList();
        if (!checkedList.isEmpty()) {
            if (!ensureActivityInResmue()) {
                HwLog.i(TAG, "deleteFiles, activity in not resumed, dropped");
            } else if (getActivity() == null) {
                HwLog.i(TAG, "deleteFiles, activity is null, dropped");
            } else {
                showDialogFragment(checkedList);
            }
        }
    }

    private void showDialogFragment(List<ITrashItem> checkedList) {
        if (TrashUtils.containsCloneApp(getContext(), checkedList)) {
            this.mTrashListener.getDataHolder().param.setDialogContentId(R.plurals.space_clean_app_restore_message_for_dual_app);
        } else {
            this.mTrashListener.getDataHolder().param.setDialogContentId(R.plurals.space_clean_app_restore_message);
        }
        if (this.mSecondaryDialogFragment != null) {
            this.mSecondaryDialogFragment.dismiss();
        }
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        this.mSecondaryDialogFragment = new SecondaryDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(SecondaryDialogFragment.ARG_SEC_DATA, this.mTrashListener.getDataHolder().param);
        bundle.putInt(SecondaryDialogFragment.ARG_SEC_SELECT, getCheckNum());
        bundle.putBoolean(SecondaryDialogFragment.ARG_SEC_ALL_SELECT, isAllChecked());
        this.mSecondaryDialogFragment.setArguments(bundle);
        this.mSecondaryDialogFragment.setOnClickListener(this.mDialogListener);
        this.mSecondaryDialogFragment.show(ft, "secondary_dialog");
    }

    private void showDetailDialogFragment(String path, int trashType) {
        if (TextUtils.isEmpty(path)) {
            HwLog.e(TAG, "showDetailDialogFragment path is empty. trash type :" + trashType);
        } else if (this.mTrashDetailDialogFragment == null || !this.mTrashDetailDialogFragment.isAdded()) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            this.mTrashDetailDialogFragment = new TrashDetailDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putString(TrashDetailDialogFragment.ARG_TRASH_PATH, path);
            bundle.putInt(TrashDetailDialogFragment.ARG_TRASH_TYPE, trashType);
            this.mTrashDetailDialogFragment.setArguments(bundle);
            if (!this.mTrashDetailDialogFragment.isVisible()) {
                this.mTrashDetailDialogFragment.show(ft, "trash_detail_dialog");
            }
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.cache_clean_menu, menu);
        this.mAllCheckMenuItem = menu.findItem(R.id.select_all);
        this.mCleanMenuItem = menu.findItem(R.id.cache_clean_menu_item);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        updateSelectState();
        super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cache_clean_menu_item:
                deleteFiles();
                break;
            case R.id.select_all:
                boolean value = !isAllChecked();
                clickAllSelect(value);
                if (this.mStatisticalData != null) {
                    this.mStatisticalData.sendSelectAllMsg(value);
                    break;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void handleMessage(Message msg) {
        ITrashItem item;
        switch (msg.what) {
            case 1:
                HwLog.i(TAG, "clean start, show progress dialog");
                showProgressDialog();
                break;
            case 2:
                HwLog.i(TAG, "clean finished, update data");
                Iterator<ITrashItem> it = this.mTotalList.iterator();
                while (it.hasNext()) {
                    item = (ITrashItem) it.next();
                    item.refreshContent();
                    if (item.isCleaned()) {
                        it.remove();
                    }
                }
                long cleanedSize = Long.parseLong(msg.obj.toString());
                Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.spaceclean_clean_trash_total_size_message, new Object[]{Formatter.formatFileSize(getApplicationContext(), cleanedSize)}), 0).show();
                if (this.mStatisticalData != null) {
                    this.mStatisticalData.sendDeleteMsg(FileUtil.getFileSize(cleanedSize));
                }
                dismissProgressDialog();
                swapAdapterData(this.mTotalList);
                break;
            case 3:
                item = (ITrashItem) msg.obj;
                if (item != null) {
                    if (item.getTrashType() != 16384 && item.getTrashType() != 65536 && item.getTrashType() != 8192) {
                        showDetailDialogFragment(item.getTrashPath(), item.getTrashType());
                        break;
                    } else {
                        openDialog(item);
                        break;
                    }
                }
                HwLog.e(TAG, "MSG_CLICK_ITEM_ICON trash is null!");
                return;
                break;
            case 4:
                Toast.makeText(GlobalContext.getContext(), getStringEx(R.string.space_clean_uninstall_fail_toast), 0).show();
                break;
        }
    }

    private void openDialog(ITrashItem item) {
        if (this.mDetailDialog == null || !this.mDetailDialog.isAdded()) {
            this.mDetailDialog = new TrashDetailDialog(item);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (!this.mDetailDialog.isVisible()) {
                this.mDetailDialog.show(ft, "normal trash_detail_dialog");
            }
        }
    }

    protected void onCheckNumChanged(int allNum, int checkNum, boolean allChecked) {
        long totalSize = 0;
        long checkedTotalSize = 0;
        for (ITrashItem item : getAdapter().getData()) {
            totalSize += item.getTrashSize();
            if (item.isChecked()) {
                checkedTotalSize += item.getTrashSize();
            }
        }
        if (this.mTipsView != null) {
            this.mTipsView.setText(getStringEx(R.string.phone_spcae_clean_tips_new_copy, Formatter.formatFileSize(getApplicationContext(), checkedTotalSize)));
        }
        this.mCheckSize = checkedTotalSize;
        updateMenu(checkedTotalSize, allNum, checkNum, allChecked);
    }

    public void updateMenu(long checkedTotalSize, int allNum, int checkNum, boolean allChecked) {
        if (this.mAllCheckMenuItem != null && this.mCleanMenuItem != null) {
            updateMenuNew(this.mAllCheckMenuItem, this.mCleanMenuItem, allNum, checkNum, allChecked);
            if (this.mOperationTitleID == 0) {
                this.mCleanMenuItem.setTitle(GlobalContext.getString(R.string.common_delete));
            } else {
                this.mCleanMenuItem.setTitle(GlobalContext.getString(this.mOperationTitleID));
            }
        }
    }

    public void updateMenuNew(MenuItem mAllCheckMenuItem, MenuItem mCleanMenuItem, int totalNum, int checkNum, boolean allChecked) {
        boolean z = true;
        Activity ac = getActivity();
        if (ac == null) {
            HwLog.i(TAG, "Activity not found");
            return;
        }
        boolean newMenuState;
        boolean z2;
        boolean preMenuState = mAllCheckMenuItem.isVisible();
        if (totalNum > 0) {
            newMenuState = true;
        } else {
            newMenuState = false;
        }
        if ((preMenuState ^ newMenuState) != 0) {
            mAllCheckMenuItem.setVisible(newMenuState);
            mCleanMenuItem.setVisible(newMenuState);
            if (newMenuState) {
                ac.invalidateOptionsMenu();
            }
        }
        if (allChecked) {
            mAllCheckMenuItem.setIcon(R.drawable.menu_check_pressed);
            mAllCheckMenuItem.setTitle(R.string.unselect_all);
            mAllCheckMenuItem.setChecked(true);
        } else {
            mAllCheckMenuItem.setIcon(R.drawable.menu_check_status);
            mAllCheckMenuItem.setTitle(R.string.select_all);
            mAllCheckMenuItem.setChecked(false);
        }
        if (totalNum > 0) {
            z2 = true;
        } else {
            z2 = false;
        }
        mAllCheckMenuItem.setEnabled(z2);
        if (checkNum <= 0) {
            z = false;
        }
        mCleanMenuItem.setEnabled(z);
    }

    private void initData() {
        if (this.mTrashListener == null) {
            HwLog.i(TAG, "mTrashListener is null");
            return;
        }
        DataHolder dataHolder = this.mTrashListener.getDataHolder();
        if (dataHolder == null) {
            HwLog.e(TAG, "initData failed! get dataHolder is null!");
            swapAdapterData(this.mTotalList);
            return;
        }
        this.mTrashHander = dataHolder.trashHander;
        int trashType = dataHolder.param.getTrashType();
        int checkState = dataHolder.param.getCheckState();
        TrashTransFunc transFunc = SecondaryConstant.getTransFunc(trashType);
        List<Trash> datas = this.mTrashListener.initAndGetData();
        List<ITrashItem> result = Lists.newArrayList();
        for (Trash trash : datas) {
            if (!trash.isCleaned()) {
                ITrashItem item = transFunc.apply(trash);
                if (item != null) {
                    if (checkState == 1) {
                        item.setChecked(true);
                    } else if (checkState == -1) {
                        item.setChecked(false);
                    } else {
                        item.setChecked(trash.isSuggestClean());
                    }
                    result.add(item);
                }
            }
        }
        this.mTotalList.addAll(result);
        swapAdapterData(this.mTotalList);
        this.mStatisticalData = StatisticalData.newInstance(dataHolder.param, StatisticalData.FRAGMENT_TYPE_LIST);
    }

    protected void onListItemClick(AdapterView<?> l, View v, int position, long id) {
        ITrashItem item = (ITrashItem) getAdapter().getItem(position);
        if (item == null) {
            HwLog.i(TAG, "OnClickListener, but trash in null!");
            return;
        }
        if (this.mStatisticalData != null) {
            this.mStatisticalData.sendItemPreviewMsg();
        }
        if (1048576 == item.getTrashType()) {
            HwLog.i(TAG, "wechat click trash item");
            long handlerId = this.mTrashHander.getId();
            TrashGroup group = this.mTrashHander.getTrashByType(1048576);
            if (group == null) {
                HwLog.i(TAG, "onListItemClick, wechat type trash group is null");
                return;
            }
            int index = group.getTrashIndex(item.getTrash());
            HwLog.i(TAG, "index is:  " + index);
            startActivity(WeChatDeepItem.getTrashIntent(getActivity(), index, handlerId, item));
        } else if (mOpenDetailEnbaleTrash.contains(Integer.valueOf(item.getTrashType()))) {
            HwLog.i(TAG, "user click trash item");
            this.mHandle.obtainMessage(3, item).sendToTarget();
        } else {
            HwLog.i(TAG, "OnClickListener, use the super");
            super.onListItemClick(l, v, position, id);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cache_clean_menu_item:
                deleteFiles();
                return;
            case R.id.select_all:
                clickAllSelect(!isAllChecked());
                return;
            default:
                return;
        }
    }

    private void updateData() {
        Iterator<ITrashItem> it = this.mTotalList.iterator();
        while (it.hasNext()) {
            ITrashItem item = (ITrashItem) it.next();
            item.refreshContent();
            if (item.isCleaned() || item.getTrashSize() == 0) {
                it.remove();
            }
        }
        swapAdapterData(this.mTotalList);
    }
}
