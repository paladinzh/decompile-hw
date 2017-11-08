package com.huawei.systemmanager.spacecleanner.ui.secondaryui;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.spacecleanner.CommonHandler;
import com.huawei.systemmanager.spacecleanner.CommonHandler.MessageHandler;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.engine.base.CleanTask;
import com.huawei.systemmanager.spacecleanner.engine.base.ICleanListener;
import com.huawei.systemmanager.spacecleanner.engine.base.ICleanListener.SimpleListener;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.StatisticalData;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.Convertor;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.TrashItemGroup;
import com.huawei.systemmanager.spacecleanner.ui.dialog.CustomDetailDialog;
import com.huawei.systemmanager.spacecleanner.ui.dialog.TrashDetailDialog;
import com.huawei.systemmanager.spacecleanner.ui.trashlistadapter.AppTrashListAdapter;
import com.huawei.systemmanager.util.HwLog;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class ListCommonFragment extends Fragment implements MessageHandler {
    protected static final int MSG_CLEAN_FINISH = 2;
    protected static final int MSG_CLEAN_ITEM_FAIL = 4;
    protected static final int MSG_CLEAN_START = 1;
    protected static final int MSG_CLICK_CHILD_CHECK = 3;
    protected static final int MSG_CLICK_DELETE_BUTTON = 8;
    protected static final int MSG_CLICK_GROUP_CHECK = 5;
    protected static final int MSG_CLICK_SELECT_ALL_BUTTON = 7;
    protected static final int MSG_CLICK_TRASH_ITEM = 6;
    protected static final int OPERATION_REMOVE_CLEANED_GROUP = 3;
    protected static final int OPERATION_REMOVE_CLEANED_ITEM = 1;
    protected static final int OPERATION_SET_ITEM_CHECKED = 2;
    protected static final int OPERATION_SET_SUGGESTION_CHECKED = 4;
    private static final String TAG = "ListCommonFragment";
    private static OnLongClickListener mLongClicker = new OnLongClickListener() {
        public boolean onLongClick(View v) {
            return true;
        }
    };
    protected View contentView;
    protected SecondaryDialogFragment deleteFragment;
    protected AppTrashListAdapter mAdapter;
    protected boolean mAllChecked = false;
    private OnClickListener mCheckClicker = new OnClickListener() {
        public void onClick(View v) {
            ITrashItem item = (ITrashItem) v.getTag();
            if (item == null) {
                HwLog.i(ListCommonFragment.TAG, "onCheckedChanged, but trash in null!");
                return;
            }
            HwLog.i(ListCommonFragment.TAG, "user click trash checkbox");
            ListCommonFragment.this.mHandle.obtainMessage(3, item).sendToTarget();
        }
    };
    protected long mCheckSize = 0;
    private int mCheckedNum;
    private ICleanListener mCleanListener = new SimpleListener() {
        public void onItemUpdate(Trash trash) {
            if (!trash.isCleaned()) {
                HwLog.e(ListCommonFragment.TAG, "clean failed!");
                ListCommonFragment.this.mHandle.obtainMessage(4, trash).sendToTarget();
            }
        }

        public void onCleanEnd(boolean canceled, long cleanedSize) {
            HwLog.i(ListCommonFragment.TAG, "onCleanEnd");
            ListCommonFragment.this.mHandle.obtainMessage(2, Long.valueOf(cleanedSize)).sendToTarget();
        }
    };
    protected DialogFragment mDetailDialog;
    private DialogInterface.OnClickListener mDialogListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case -1:
                    if (ListCommonFragment.this.mTrashListener != null) {
                        ListCommonFragment.this.mIsCleanded = true;
                        ListCommonFragment.this.recordDeleteOp();
                        List<Trash> trashList = Convertor.covertTrashItem(ListCommonFragment.this.getCheckedList());
                        ListCommonFragment.this.mTrashListener.setCleanedOperation(true);
                        ListCommonFragment.this.mHandle.sendEmptyMessage(1);
                        if (ListCommonFragment.this.mStatisticalData != null) {
                            ListCommonFragment.this.mStatisticalData.sendDeleteMsg(FileUtil.getFileSize(ListCommonFragment.this.mCheckSize));
                        }
                        CleanTask.startClean(ListCommonFragment.this.getApplicationContext(), trashList, ListCommonFragment.this.mCleanListener, ListCommonFragment.this.mScanHandler);
                        break;
                    }
                    HwLog.i(ListCommonFragment.TAG, "mTrashListener is null");
                    return;
            }
        }
    };
    protected int mEmptyImageId;
    protected ViewStub mEmptyStub;
    protected int mEmptyTextId;
    protected View mEmptyView;
    private OnClickListener mGroupCheckClicker = new OnClickListener() {
        public void onClick(View v) {
            TrashItemGroup itemGroup = (TrashItemGroup) v.getTag();
            if (itemGroup == null) {
                HwLog.i(ListCommonFragment.TAG, "onCheckedChanged, but trash group in null!");
                return;
            }
            HwLog.i(ListCommonFragment.TAG, "user click trash group checkbox");
            ListCommonFragment.this.mHandle.obtainMessage(5, itemGroup).sendToTarget();
        }
    };
    protected final Handler mHandle = new CommonHandler(this);
    protected int mIndex;
    protected boolean mIsCleanded = false;
    private OnClickListener mItemClicker = new OnClickListener() {
        public void onClick(View v) {
            ITrashItem item = (ITrashItem) v.getTag(R.id.convertview_tag_item);
            if (item == null) {
                HwLog.i(ListCommonFragment.TAG, "item click, but trash item in null!");
                return;
            }
            if (ListCommonFragment.this.mStatisticalData != null) {
                ListCommonFragment.this.mStatisticalData.sendItemPreviewMsg();
            }
            ListCommonFragment.this.handlerTrashItemClick(item);
        }
    };
    protected int mListAppType;
    protected LinearLayout mListContainer;
    protected boolean mNeedChangeCkeckPoint = false;
    protected int mOperationTitleID = 0;
    protected ProgressDialog mProgressDlg;
    protected TrashScanHandler mScanHandler;
    private StatisticalData mStatisticalData;
    protected TextView mTipsView;
    protected TrashDetailDialogFragment mTrashDetailDialogFragment;
    protected onCallTrashSetListener mTrashListener;

    protected abstract void dimissShowingDialog();

    public abstract void initAdapter();

    public abstract void recordDeleteOp();

    protected abstract void updateMenu(long j, int i, int i2, boolean z);

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
        setHasOptionsMenu(true);
        HwLog.i(TAG, "onCreate");
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mStatisticalData != null) {
            this.mStatisticalData.destroy();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_app_cache_set, container, false);
        Activity ac = getActivity();
        ExpandableListView list = (ExpandableListView) view.findViewById(R.id.list_app_cache_set);
        View footerView = inflater.inflate(R.layout.blank_footer_view, list, false);
        list.setFooterDividersEnabled(false);
        list.addFooterView(footerView, null, false);
        this.mAdapter = new AppTrashListAdapter(ac, this.mCheckClicker, this.mItemClicker, mLongClicker, this.mGroupCheckClicker);
        this.mAdapter.setList(list);
        list.setOnChildClickListener(new OnChildClickListener() {
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                ITrashItem item = ListCommonFragment.this.mAdapter.getChild(groupPosition, childPosition);
                if (item == null) {
                    HwLog.i(ListCommonFragment.TAG, "OnClickListener, but trash in null!");
                    return false;
                }
                ListCommonFragment.this.mHandle.obtainMessage(6, item).sendToTarget();
                return false;
            }
        });
        this.contentView = view;
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mTipsView = (TextView) view.findViewById(R.id.tips);
        this.mListContainer = (LinearLayout) view.findViewById(R.id.list_container);
        try {
            this.mEmptyStub = (ViewStub) view.findViewById(R.id.empty_view_stub);
            ViewUtil.initEmptyViewMargin(GlobalContext.getContext(), this.mEmptyStub);
        } catch (Exception e) {
            HwLog.i(TAG, "emptyview is not viewstub");
        }
        initData();
    }

    public void onResume() {
        super.onResume();
        updateSelectState();
    }

    protected void updateSelectState() {
        if (this.mAdapter == null) {
            HwLog.e(TAG, "updateSelectState called, but adapter is null!");
            return;
        }
        List<TrashItemGroup> list = this.mAdapter.getData();
        itemOperation(list, 1, Boolean.valueOf(false));
        groupOperation(list, 3);
        int checkableNum = 0;
        int checkNum = 0;
        long checkedTotalSize = 0;
        long totalSize = 0;
        for (TrashItemGroup<ITrashItem> groupIt : list) {
            for (ITrashItem item : groupIt) {
                if (item.isCheckable()) {
                    checkableNum++;
                    if (item.isChecked()) {
                        checkNum++;
                        checkedTotalSize += item.getTrashSizeCleaned(false);
                    }
                    totalSize += item.getTrashSizeCleaned(false);
                }
            }
        }
        boolean z = checkNum == checkableNum && checkNum != 0;
        this.mAllChecked = z;
        this.mAdapter.notifyDataSetChanged();
        if (this.mTipsView != null) {
            this.mTipsView.setText(getApplicationContext().getString(R.string.phone_spcae_clean_tips_new_copy, new Object[]{Formatter.formatFileSize(getApplicationContext(), checkedTotalSize)}));
        }
        this.mCheckSize = checkedTotalSize;
        this.mCheckedNum = checkNum;
        updateMenu(checkedTotalSize, checkableNum, checkNum, this.mAllChecked);
        updateEmptyViewByData();
    }

    private void updateEmptyViewByData() {
        if (this.mAdapter.isEmpty()) {
            HwLog.i(TAG, "show emptyview");
            initEmptyView();
            setVisibility(this.mEmptyView, 0);
            ViewUtil.initEmptyViewMargin(GlobalContext.getContext(), this.mEmptyView);
            setVisibility(this.mListContainer, 8);
            return;
        }
        HwLog.i(TAG, "hide emptyview");
        setVisibility(this.mEmptyView, 8);
        setVisibility(this.mListContainer, 0);
    }

    private void initEmptyView() {
        if (this.mEmptyView == null && this.contentView != null) {
            if (this.mEmptyStub != null) {
                this.mEmptyStub.inflate();
            }
            this.mEmptyView = this.contentView.findViewById(R.id.empty_view);
            if (this.mEmptyView != null) {
                TextView textView = (TextView) this.mEmptyView.findViewById(R.id.empty_text);
                if (!(textView == null || this.mEmptyTextId == 0)) {
                    textView.setText(this.mEmptyTextId);
                }
                ImageView imageView = (ImageView) this.mEmptyView.findViewById(R.id.empty_image);
                if (!(imageView == null || this.mEmptyImageId == 0)) {
                    imageView.setImageResource(this.mEmptyImageId);
                }
            }
        }
    }

    private void setVisibility(View view, int visibleState) {
        if (view != null && visibleState != view.getVisibility()) {
            view.setVisibility(visibleState);
        }
    }

    private void clickAllSelect(boolean checked) {
        if (checked != this.mAllChecked) {
            if (this.mAdapter == null) {
                HwLog.i(TAG, "onAllSelectButtonClick adapte is null");
                return;
            }
            itemOperation(this.mAdapter.getData(), 2, Boolean.valueOf(checked));
            updateSelectState();
        }
    }

    private boolean isAllChecked() {
        return this.mAllChecked;
    }

    private boolean handlerTrashItemClick(ITrashItem item) {
        int action = item.doClickAction();
        if (action == 2) {
            this.mHandle.obtainMessage(3, item).sendToTarget();
            return true;
        } else if (action == 3) {
            openSecondary(item);
            return true;
        } else if (action == 5) {
            handleOpenDialog(item);
            return true;
        } else if (action != 6) {
            return false;
        } else {
            openCustomDialog(item.getPreMessage());
            return true;
        }
    }

    public void handleOpenDialog(ITrashItem item) {
        if (item != null) {
            if (512 == item.getTrashType() || 256 == item.getTrashType()) {
                showDetailDialogFragment(item);
            } else {
                openDialog(item);
            }
        }
    }

    private void openSecondary(ITrashItem item) {
        HwLog.d(TAG, "openSecondary start");
        Activity ac = getActivity();
        if (ac != null && item != null && this.mScanHandler != null) {
            int itemType = item.getTrashType();
            try {
                startActivityForResult(new Intent(ac, ListTrashSetActivity.class).putExtra("handler_id", this.mScanHandler.getId()).putExtra(SecondaryConstant.OPEN_SECONDARY_PARAM, item.getOpenSecondaryParam()), itemType);
            } catch (Exception e) {
                HwLog.e(TAG, "startActivityForResult error.itemType:" + itemType);
                e.printStackTrace();
            }
        }
    }

    private void deleteFiles() {
        if (!getCheckedList().isEmpty()) {
            if (!ensureActivityInResume()) {
                HwLog.i(TAG, "deleteFiles, activity in not resumed, dropped");
            } else if (getActivity() == null) {
                HwLog.i(TAG, "deleteFiles, activity is null, dropped");
            } else {
                showDialogFragment();
            }
        }
    }

    private void showDialogFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        dimissShowingDialog();
        if (this.mTrashListener == null) {
            HwLog.i(TAG, "mTrashListener is null");
            return;
        }
        this.deleteFragment = new SecondaryDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(SecondaryDialogFragment.ARG_SEC_DATA, this.mTrashListener.getDataHolder().param);
        bundle.putInt(SecondaryDialogFragment.ARG_SEC_SELECT, this.mCheckedNum);
        bundle.putBoolean(SecondaryDialogFragment.ARG_SEC_ALL_SELECT, this.mAllChecked);
        this.deleteFragment.setArguments(bundle);
        this.deleteFragment.setOnClickListener(this.mDialogListener);
        this.deleteFragment.show(ft, "secondary_dialog");
    }

    protected void showDetailDialogFragment(ITrashItem item) {
        String path = item.getTrashPath();
        int trashType = item.getTrashType();
        if (!TextUtils.isEmpty(path)) {
            dimissShowingDialog();
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

    public List<ITrashItem> getCheckedList() {
        List<ITrashItem> result = new LinkedList();
        if (this.mAdapter == null) {
            HwLog.i(TAG, "getCheckedList ,adapter is null");
            return result;
        }
        for (TrashItemGroup<ITrashItem> groupItem : this.mAdapter.getData()) {
            for (ITrashItem it : groupItem) {
                if (it.isChecked()) {
                    result.add(it);
                }
            }
        }
        return result;
    }

    public boolean ensureActivityInResume() {
        Activity activity = getActivity();
        if (activity == null || !activity.isResumed()) {
            return false;
        }
        return true;
    }

    public void handleMessage(Message msg) {
        boolean z = false;
        switch (msg.what) {
            case 1:
                HwLog.i(TAG, "clean start, show progress dialog");
                showProgressDialog();
                break;
            case 2:
                HwLog.i(TAG, "clean finished, update data");
                dismissProgressDialog();
                updateSelectState();
                break;
            case 3:
                msg.obj.toggle();
                updateSelectState();
                break;
            case 4:
                Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.space_clean_delete_fail_toast), 0).show();
                updateSelectState();
                break;
            case 5:
                msg.obj.toggle();
                updateSelectState();
                break;
            case 6:
                ITrashItem itemTrash = msg.obj;
                if (itemTrash != null) {
                    showDetailDialogFragment(itemTrash);
                    break;
                } else {
                    HwLog.e(TAG, "MSG_CLICK_ITEM_ICON trash is null!");
                    return;
                }
            case 7:
                boolean z2;
                boolean isAllChecked = isAllChecked();
                if (isAllChecked) {
                    z2 = false;
                } else {
                    z2 = true;
                }
                clickAllSelect(z2);
                if (this.mStatisticalData != null) {
                    StatisticalData statisticalData = this.mStatisticalData;
                    if (!isAllChecked) {
                        z = true;
                    }
                    statisticalData.sendSelectAllMsg(z);
                    break;
                }
                break;
            case 8:
                deleteFiles();
                break;
        }
    }

    private void itemOperation(List<TrashItemGroup> list, int operationId, Boolean checked) {
        for (TrashItemGroup groupItem : list) {
            operation(groupItem, operationId, checked);
        }
    }

    private void operation(TrashItemGroup groupItem, int operationId, Boolean checked) {
        Iterator<ITrashItem> it = groupItem.iterator();
        while (it.hasNext()) {
            ITrashItem item = (ITrashItem) it.next();
            if (1 == operationId) {
                item.refreshContent();
                if (item.isCleaned()) {
                    it.remove();
                }
            }
            if (2 == operationId) {
                item.setChecked(checked.booleanValue());
            }
            if (4 == operationId) {
                item.setChecked(item.isSuggestClean());
            }
        }
    }

    protected void itemOperation(List<TrashItemGroup> list, int Type, int operationId, Boolean checked) {
        for (TrashItemGroup groupItem : list) {
            if (groupItem.getTrashType() == Type) {
                operation(groupItem, operationId, checked);
            }
        }
    }

    private void groupOperation(List<TrashItemGroup> list, int operationId) {
        Iterator<TrashItemGroup> grouplist = list.iterator();
        while (grouplist.hasNext()) {
            TrashItemGroup groupItem = (TrashItemGroup) grouplist.next();
            if (3 == operationId && groupItem.getTrashCount() == 0) {
                grouplist.remove();
            }
        }
    }

    private void showProgressDialog() {
        if (this.mProgressDlg != null) {
            this.mProgressDlg.dismiss();
        }
        Activity ac = getActivity();
        if (ac != null) {
            this.mProgressDlg = ProgressDialog.show(ac, null, getApplicationContext().getString(R.string.space_common_msg_cleaning), true, false);
        }
    }

    private void dismissProgressDialog() {
        if (this.mProgressDlg != null) {
            this.mProgressDlg.dismiss();
            this.mProgressDlg = null;
        }
    }

    protected void initData() {
        Activity ac = getActivity();
        if (ac == null) {
            HwLog.i(TAG, "getActivity is null");
        } else if (this.mTrashListener == null) {
            HwLog.i(TAG, "mTrashListener is null");
        } else {
            DataHolder dataHolder = this.mTrashListener.getDataHolder();
            if (dataHolder == null) {
                HwLog.e(TAG, "initData, dataHolder is null");
                return;
            }
            this.mScanHandler = dataHolder.trashHander;
            this.mOperationTitleID = dataHolder.param.getOperationResId();
            this.mEmptyTextId = dataHolder.param.getEmptyTextID();
            this.mEmptyImageId = dataHolder.param.getEmptyIconID();
            this.mIndex = dataHolder.index;
            ac.setTitle(dataHolder.param.getTitleStr());
            this.mListAppType = dataHolder.param.getTrashType();
            initAdapter();
            this.mStatisticalData = StatisticalData.newInstance(dataHolder.param, StatisticalData.FRAGMENT_TYPE_EXPAND_LIST);
        }
    }

    protected void initTotalList(List<TrashItemGroup> mTotalList) {
        if (mTotalList != null) {
            itemOperation(mTotalList, 1, Boolean.valueOf(false));
            groupOperation(mTotalList, 3);
            itemOperation(mTotalList, 2, Boolean.valueOf(false));
            return;
        }
        HwLog.e(TAG, "mTotalList is null!");
    }

    protected Context getApplicationContext() {
        return GlobalContext.getContext();
    }

    protected void openDialog(ITrashItem item) {
        dimissShowingDialog();
        this.mDetailDialog = new TrashDetailDialog(item);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (!this.mDetailDialog.isVisible()) {
            this.mDetailDialog.show(ft, "normal trash_detail_dialog");
        }
    }

    private void openCustomDialog(String msg) {
        if (this.mDetailDialog == null || !this.mDetailDialog.isAdded()) {
            this.mDetailDialog = new CustomDetailDialog(msg);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (!this.mDetailDialog.isVisible()) {
                this.mDetailDialog.show(ft, "normal trash_detail_dialog");
            }
        }
    }
}
