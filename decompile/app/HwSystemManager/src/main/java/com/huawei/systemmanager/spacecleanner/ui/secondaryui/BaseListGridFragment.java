package com.huawei.systemmanager.spacecleanner.ui.secondaryui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ListView;
import android.widget.TextView;
import com.common.imageloader.core.ImageLoader;
import com.common.imageloader.core.listener.PauseOnScrollListener;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Constant;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.ToastUtils;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.netassistant.utils.ViewUtils;
import com.huawei.systemmanager.spacecleanner.CommonHandler;
import com.huawei.systemmanager.spacecleanner.CommonHandler.MessageHandler;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.engine.base.CleanTask;
import com.huawei.systemmanager.spacecleanner.engine.base.ICleanListener;
import com.huawei.systemmanager.spacecleanner.engine.base.ICleanListener.SimpleListener;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.ListGrid.BaseListGridAdapter;
import com.huawei.systemmanager.spacecleanner.ui.ListGrid.ListGridListener.OnSizeChangeListener;
import com.huawei.systemmanager.spacecleanner.ui.StatisticalData;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.Convertor;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.view.SquareFrameLayout;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class BaseListGridFragment extends Fragment implements MessageHandler {
    private static final int MSG_CLEAN_FINISH = 2;
    private static final int MSG_CLEAN_START = 1;
    private static final String TAG = "BaseListGridFragment";
    public Activity mActivity = null;
    private BaseListGridAdapter mAdapter;
    private MenuItem mAllCheckMenuItem;
    private int mCheckedSize = 0;
    private ICleanListener mCleanListener = new SimpleListener() {
        public void onItemUpdate(Trash trash) {
            if (!trash.isCleaned()) {
                HwLog.e(BaseListGridFragment.TAG, "clean failed!");
                ToastUtils.toastShortMsg((int) R.string.space_clean_uninstall_fail_toast);
            }
        }

        public void onCleanEnd(boolean canceled, long cleanedSize) {
            HwLog.i(BaseListGridFragment.TAG, "onCleanEnd");
            BaseListGridFragment.this.mHandler.obtainMessage(2, Long.valueOf(cleanedSize)).sendToTarget();
        }
    };
    private MenuItem mCleanMenuItem;
    private View mContainer;
    private OnClickListener mDialogListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case -1:
                    CleanTask.startClean(Convertor.covertTrashItem(BaseListGridFragment.this.getCheckedList()), BaseListGridFragment.this.mCleanListener, BaseListGridFragment.this.mScanHandler);
                    return;
                default:
                    return;
            }
        }
    };
    private View mEmptyView;
    private final Handler mHandler = new CommonHandler(this);
    private boolean mIsAllChecked;
    private ListView mListView;
    private ProgressDialog mProgressDlg;
    private TrashScanHandler mScanHandler;
    public OnSizeChangeListener mSizeChangeListener = new OnSizeChangeListener() {
        public void onSizeChanged(long checkedSize, long allSize, boolean isAll, int checkedCount) {
            if (!(BaseListGridFragment.this.mCleanMenuItem == null || BaseListGridFragment.this.mAllCheckMenuItem == null)) {
                int i;
                boolean z;
                BaseListGridFragment.this.mCleanMenuItem.setTitle(GlobalContext.getString(R.string.common_delete));
                MenuItem -get0 = BaseListGridFragment.this.mAllCheckMenuItem;
                if (isAll) {
                    i = R.drawable.menu_check_pressed;
                } else {
                    i = R.drawable.menu_check_status;
                }
                -get0.setIcon(i);
                BaseListGridFragment.this.mAllCheckMenuItem.setTitle(isAll ? R.string.unselect_all : R.string.select_all);
                BaseListGridFragment.this.mAllCheckMenuItem.setChecked(isAll);
                BaseListGridFragment.this.mIsAllChecked = isAll;
                -get0 = BaseListGridFragment.this.mCleanMenuItem;
                if (checkedSize != 0) {
                    z = true;
                } else {
                    z = false;
                }
                -get0.setEnabled(z);
                BaseListGridFragment.this.mCheckedSize = checkedCount;
            }
            if (BaseListGridFragment.this.mSumTextView != null) {
                String str = FileUtil.getFileSize(checkedSize);
                BaseListGridFragment.this.mSumTextView.setText(GlobalContext.getString(R.string.phone_spcae_clean_tips_new_copy, str));
            }
        }
    };
    private List<ITrashItem> mSource = new ArrayList();
    private StatisticalData mStatisticalData;
    private TextView mSumTextView;
    public onCallTrashSetListener mTrashListener;

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
        this.mActivity = getActivity();
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mStatisticalData != null) {
            this.mStatisticalData.destroy();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_grid_fragment, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mListView = (ListView) view.findViewById(R.id.listview);
        this.mListView.setPadding(SquareFrameLayout.getRemainWidth() / 2, 0, SquareFrameLayout.getRemainWidth() / 2, 0);
        this.mSumTextView = (TextView) view.findViewById(R.id.tips);
        this.mContainer = view.findViewById(R.id.list_container);
        this.mEmptyView = view.findViewById(R.id.empty_view_stub);
        try {
            ViewUtil.initEmptyViewMargin(GlobalContext.getContext(), (ViewStub) this.mEmptyView);
        } catch (Exception e) {
            HwLog.i(TAG, "emptyview is not viewstub");
        }
        this.mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
        this.mListView.setTag(Constant.DISALBE_LISTVIEW_CHECKOBX_MULTI_SELECT);
        View footerView = this.mActivity.getLayoutInflater().inflate(R.layout.blank_footer_view, this.mListView, false);
        this.mListView.setFooterDividersEnabled(false);
        this.mListView.addFooterView(footerView, null, false);
        initData();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.cache_clean_menu, menu);
        this.mAllCheckMenuItem = menu.findItem(R.id.select_all);
        this.mCleanMenuItem = menu.findItem(R.id.cache_clean_menu_item);
        this.mCleanMenuItem.setTitle(GlobalContext.getString(R.string.common_delete));
        this.mCleanMenuItem.setEnabled(false);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cache_clean_menu_item:
                deleteFiles();
                break;
            case R.id.select_all:
                this.mIsAllChecked = !this.mIsAllChecked;
                updateSelectAll();
                if (this.mStatisticalData != null) {
                    this.mStatisticalData.sendSelectAllMsg(this.mIsAllChecked);
                    break;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateSelectAll() {
        if (this.mAdapter != null) {
            this.mAdapter.setAllItemChecked(this.mIsAllChecked);
        }
    }

    private void deleteFiles() {
        showDialogFragment();
    }

    private void showDialogFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        SecondaryDialogFragment newFragment = new SecondaryDialogFragment();
        Bundle bundle = new Bundle();
        if (this.mTrashListener != null) {
            bundle.putParcelable(SecondaryDialogFragment.ARG_SEC_DATA, this.mTrashListener.getDataHolder().param);
            bundle.putInt(SecondaryDialogFragment.ARG_SEC_SELECT, this.mCheckedSize);
            bundle.putBoolean(SecondaryDialogFragment.ARG_SEC_ALL_SELECT, this.mIsAllChecked);
        }
        newFragment.setArguments(bundle);
        newFragment.setOnClickListener(this.mDialogListener);
        newFragment.show(ft, "secondary_dialog");
    }

    public List<ITrashItem> getCheckedList() {
        if (this.mAdapter != null) {
            return this.mAdapter.getCheckedList();
        }
        return null;
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                HwLog.i(TAG, "clean start, show progress dialog");
                showProgressDialog();
                return;
            case 2:
                HwLog.i(TAG, "clean finished, update data");
                String str = FileUtil.getFileSize(Long.parseLong(msg.obj.toString()));
                updateAfterDelete();
                dismissProgressDialog();
                if (this.mStatisticalData != null) {
                    this.mStatisticalData.sendDeleteMsg(str);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void showProgressDialog() {
        if (this.mProgressDlg != null) {
            this.mProgressDlg.dismiss();
        }
        if (this.mActivity != null) {
            this.mProgressDlg = ProgressDialog.show(this.mActivity, null, GlobalContext.getString(R.string.space_common_msg_cleaning), true, false);
        }
    }

    private void dismissProgressDialog() {
        if (this.mProgressDlg != null && this.mProgressDlg.isShowing()) {
            this.mProgressDlg.dismiss();
            this.mProgressDlg = null;
        }
    }

    private void updateAfterDelete() {
        List<ITrashItem> list = new ArrayList();
        for (ITrashItem item : this.mSource) {
            item.refreshContent();
            if (!item.isCleaned()) {
                list.add(item);
            }
        }
        this.mSource.clear();
        this.mSource.addAll(list);
        if (this.mAdapter != null) {
            this.mAdapter.swapData(this.mSource);
        }
        checkToShowEmptyView();
    }

    private void checkToShowEmptyView() {
        if (this.mSource.isEmpty()) {
            HwLog.i(TAG, "checkToShowEmptyView , show  empty view");
            ViewUtils.setVisibility(this.mContainer, 8);
            ViewUtils.setVisibility(this.mEmptyView, 0);
            ViewUtils.setVisible(this.mAllCheckMenuItem, false);
            ViewUtils.setVisible(this.mCleanMenuItem, false);
        }
    }

    private void initData() {
        if (this.mTrashListener == null) {
            HwLog.i(TAG, "mTrashListener is null");
            checkToShowEmptyView();
            return;
        }
        DataHolder dataHolder = this.mTrashListener.getDataHolder();
        if (dataHolder == null || dataHolder.param == null) {
            HwLog.e(TAG, "initData, dataHolder is null");
            checkToShowEmptyView();
            return;
        }
        this.mScanHandler = dataHolder.trashHander;
        int trashType = dataHolder.param.getTrashType();
        int subType = dataHolder.param.getSubTrashType();
        int index = dataHolder.index;
        this.mStatisticalData = StatisticalData.newInstance(dataHolder.param, StatisticalData.FRAGMENT_TYPE_LIST_GRID);
        if (this.mStatisticalData != null) {
            this.mStatisticalData.setSubTrashType(subType);
        }
        this.mSource.addAll(createData(trashType, index, this.mScanHandler));
        this.mAdapter = createAdapter(trashType, subType);
        if (this.mAdapter != null) {
            this.mAdapter.swapData(this.mSource);
            this.mAdapter.setStatisticalData(this.mStatisticalData);
        }
        if (this.mListView != null) {
            this.mListView.setAdapter(this.mAdapter);
        }
    }

    protected List<ITrashItem> createData(int trsahType, int index, TrashScanHandler mScanHandler) {
        return null;
    }

    protected BaseListGridAdapter createAdapter(int trsahType, int subType) {
        return null;
    }
}
