package com.huawei.systemmanager.spacecleanner.ui.photomanager;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.SelectListFragment;
import com.huawei.systemmanager.comm.misc.Constant;
import com.huawei.systemmanager.spacecleanner.CommonHandler;
import com.huawei.systemmanager.spacecleanner.CommonHandler.MessageHandler;
import com.huawei.systemmanager.spacecleanner.engine.base.CleanTask;
import com.huawei.systemmanager.spacecleanner.engine.base.ICleanListener;
import com.huawei.systemmanager.spacecleanner.engine.base.ICleanListener.SimpleListener;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.statistics.SpaceStatsUtils;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.Convertor;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.OpenSecondaryParam;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.DataHolder;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.ListTrashSetActivity;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.SecondaryConstant;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.SecondaryDialogFragment;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.onCallTrashSetListener;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.FileTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.PhotoTrashItem;
import com.huawei.systemmanager.spacecleanner.view.GridViewWithHeaderAndFooter;
import com.huawei.systemmanager.spacecleanner.view.SquareFrameLayout;
import com.huawei.systemmanager.util.HwLog;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ListPhotoGridSetFragment extends SelectListFragment<ITrashItem> implements MessageHandler {
    private static final int MSG_CLEAN_FINISH = 2;
    private static final int MSG_CLEAN_START = 1;
    private static final int MSG_CLICK_PHOTO_CHILD_CHECK_BOX = 3;
    public static final String TAG = "ListPhotoGridSetFragment";
    private ActionBar actionBar;
    private View customView;
    private TextView mActionNum;
    private TextView mActionTitle;
    private Activity mActivity = null;
    private MenuItem mAllCheckMenuItem;
    private OnClickListener mCheckClicker = new OnClickListener() {
        public void onClick(View v) {
            ITrashItem item = (ITrashItem) v.getTag();
            if (item == null) {
                HwLog.i(ListPhotoGridSetFragment.TAG, "onCheckedChanged, but trash in null!");
                return;
            }
            HwLog.i(ListPhotoGridSetFragment.TAG, "user click trash checkbox");
            ListPhotoGridSetFragment.this.mHandle.obtainMessage(3, item).sendToTarget();
        }
    };
    private long mCheckSize = 0;
    private ICleanListener mCleanListener = new SimpleListener() {
        public void onCleanEnd(boolean canceled, long cleanedSize) {
            HwLog.i(ListPhotoGridSetFragment.TAG, "onCleanEnd");
            ListPhotoGridSetFragment.this.mHandle.obtainMessage(2, Long.valueOf(cleanedSize)).sendToTarget();
        }
    };
    private MenuItem mCleanMenuItem;
    private DialogInterface.OnClickListener mDialogListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case -1:
                    if (ListPhotoGridSetFragment.this.mTrashListener != null) {
                        List<ITrashItem> checkedList = ListPhotoGridSetFragment.this.getCheckedList();
                        List<Trash> trashList = Convertor.covertTrashItem(checkedList);
                        ListPhotoGridSetFragment.this.mHandle.sendEmptyMessage(1);
                        ListTrashSetActivity activity = (ListTrashSetActivity) ListPhotoGridSetFragment.this.getActivity();
                        if (activity != null) {
                            SpaceStatsUtils.reportDeepCleanTrashSize(activity.fromDeepManagerEnterence(), ListPhotoGridSetFragment.this.mCheckSize);
                        }
                        CleanTask.startClean(ListPhotoGridSetFragment.this.getApplicationContext(), trashList, ListPhotoGridSetFragment.this.mCleanListener, ListPhotoGridSetFragment.this.mTrashListener.getTrashHandler());
                        ListPhotoGridSetFragment.this.mTrashListener.setCleanedOperation(true);
                        ListPhotoGridSetFragment.this.mTotalList.removeAll(checkedList);
                        break;
                    }
                    HwLog.i(ListPhotoGridSetFragment.TAG, "Listener is null");
                    return;
            }
        }
    };
    private String mFolderPath;
    private GridViewWithHeaderAndFooter mGridView;
    private final Handler mHandle = new CommonHandler(this);
    private int mOperationTitleID = 0;
    private PhotoFolder mPhotoFolder;
    protected PhotoGridSetAdapter mPhotoGridSetAdapter;
    private onCallPhotoTrashSetListener mPhotoTrashListener;
    private TextView mTipsView;
    private ArrayList<ITrashItem> mTotalList = Lists.newArrayList();
    private onCallTrashSetListener mTrashListener;

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof onCallTrashSetListener) {
            this.mTrashListener = (onCallTrashSetListener) activity;
        } else {
            HwLog.d(TAG, "activity not have onCallTrashSetListener interface! ");
        }
        if (activity instanceof onCallPhotoTrashSetListener) {
            this.mPhotoTrashListener = (onCallPhotoTrashSetListener) activity;
        } else {
            HwLog.d(TAG, "activity not have onCallPhotoTrashSetListener interface! ");
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HwLog.i(TAG, "onCreate");
        setHasOptionsMenu(true);
        this.mActivity = getActivity();
    }

    public void initFolderPath(String path) {
        this.mFolderPath = path;
    }

    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
            this.mTotalList.clear();
            return;
        }
        initData();
        initActionBar();
    }

    private void initActionBar() {
        if (this.mTrashListener == null) {
            HwLog.i(TAG, "mTrashListener is null");
            return;
        }
        DataHolder dataHolder = this.mTrashListener.getDataHolder();
        if (dataHolder != null) {
            Activity ac = getActivity();
            if (ac == null) {
                HwLog.i(TAG, "getActivity is null");
                return;
            }
            ActionBar actionBar = ac.getActionBar();
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(dataHolder.param.getTitleStr());
            this.mActivity.setTitle(dataHolder.param.getTitleStr());
            actionBar.show();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initActionBar();
        View view = inflater.inflate(R.layout.fragment_grid_photo_set, container, false);
        this.mPhotoGridSetAdapter = new PhotoGridSetAdapter(getApplicationContext());
        this.mPhotoGridSetAdapter.setCheckBoxClickListener(this.mCheckClicker);
        setListAdapter(this.mPhotoGridSetAdapter);
        this.customView = (RelativeLayout) inflater.inflate(R.layout.actionmode_title, null, false);
        this.customView.setFocusableInTouchMode(true);
        this.mActionTitle = (TextView) this.customView.findViewById(R.id.txt_actionmode_title);
        this.mActionNum = (TextView) this.customView.findViewById(R.id.txt_actionmode_selected_num);
        this.mTipsView = (TextView) view.findViewById(R.id.photo_tips);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.mGridView = (GridViewWithHeaderAndFooter) view.findViewById(R.id.content_list);
        this.mGridView.addFooterView(this.mActivity.getLayoutInflater().inflate(R.layout.blank_footer_view, this.mGridView, false), null, false);
        super.onViewCreated(view, savedInstanceState);
        this.mGridView.setTag(Constant.DISALBE_LISTVIEW_CHECKOBX_MULTI_SELECT);
        initView();
        initData();
    }

    private void initView() {
        if (this.mTrashListener == null) {
            HwLog.i(TAG, "Listener is null");
            return;
        }
        DataHolder dataHolder = this.mTrashListener.getDataHolder();
        if (dataHolder == null) {
            HwLog.e(TAG, "initView get dataHolder is null!");
            return;
        }
        Activity ac = getActivity();
        if (ac == null) {
            HwLog.e(TAG, "getActivity is null!");
            return;
        }
        OpenSecondaryParam param = dataHolder.param;
        this.mOperationTitleID = param.getOperationResId();
        this.actionBar = ac.getActionBar();
        setEmptyTextAndImage(param.getEmptyTextID(), param.getEmptyIconID());
    }

    private void initData() {
        this.mTotalList.clear();
        if (this.mTrashListener == null || this.mPhotoTrashListener == null) {
            HwLog.i(TAG, "Listener is null");
            return;
        }
        for (PhotoFolder pf : this.mPhotoTrashListener.getPhotoFolders()) {
            if (this.mFolderPath.equals(pf.getFolderPath())) {
                this.mPhotoFolder = pf;
                break;
            }
        }
        if (this.mPhotoFolder == null) {
            HwLog.e(TAG, "PhotoFolder is null!");
            swapAdapterData(this.mTotalList);
            return;
        }
        DataHolder dataHolder = this.mTrashListener.getDataHolder();
        if (dataHolder == null) {
            HwLog.e(TAG, "initData failed! get dataHolder is null!");
            swapAdapterData(this.mTotalList);
            return;
        }
        int trashType = dataHolder.param.getTrashType();
        int checkState = dataHolder.param.getCheckState();
        TrashTransFunc transFunc = getTransFunc(trashType);
        List<ITrashItem> result = Lists.newArrayList();
        for (Trash trash : this.mPhotoFolder.getTrashs()) {
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
        initGridView();
        swapAdapterData(this.mTotalList);
    }

    private void initGridView() {
        if (this.mGridView != null) {
            int columnWith = SquareFrameLayout.getSquareWidth();
            this.mGridView.setPadding(SquareFrameLayout.getRemainWidth() / 2, 0, SquareFrameLayout.getRemainWidth() / 2, 0);
            this.mGridView.setNumColumns(4);
            this.mGridView.setColumnWidth(columnWith);
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.cache_clean_menu, menu);
        this.mAllCheckMenuItem = menu.findItem(R.id.select_all);
        this.mCleanMenuItem = menu.findItem(R.id.cache_clean_menu_item);
    }

    public void onDestroyView() {
        super.onDestroyView();
    }

    public void onPrepareOptionsMenu(Menu menu) {
        updateSelectState();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cache_clean_menu_item:
                deleteFiles();
                break;
            case R.id.select_all:
                clickAllSelect(!isAllChecked());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initGridView();
    }

    private void deleteFiles() {
        if (!getCheckedList().isEmpty()) {
            if (!ensureActivityInResmue()) {
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
        SecondaryDialogFragment newFragment = new SecondaryDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(SecondaryDialogFragment.ARG_SEC_DATA, this.mTrashListener.getDataHolder().param);
        bundle.putInt(SecondaryDialogFragment.ARG_SEC_SELECT, getCheckNum());
        bundle.putBoolean(SecondaryDialogFragment.ARG_SEC_ALL_SELECT, isAllChecked());
        newFragment.setArguments(bundle);
        newFragment.setOnClickListener(this.mDialogListener);
        newFragment.show(ft, "secondary_dialog");
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                HwLog.i(TAG, "clean start, show progress dialog");
                showProgressDialog();
                return;
            case 2:
                HwLog.i(TAG, "clean finished, update data");
                dismissProgressDialog();
                swapAdapterData(this.mTotalList);
                return;
            case 3:
                msg.obj.toggle();
                updateSelectState();
                return;
            default:
                return;
        }
    }

    protected void onCheckNumChanged(int allNum, int checkNum, boolean allChecked) {
        updateMenu(allNum, checkNum, allChecked);
        updateActionBarTitle(checkNum);
    }

    public void updateMenu(int allNum, int checkNum, boolean allChecked) {
        if (this.mAllCheckMenuItem != null && this.mCleanMenuItem != null) {
            updateMenu(this.mAllCheckMenuItem, this.mCleanMenuItem, allNum, checkNum, allChecked);
            this.mCleanMenuItem.setTitle(getApplicationContext().getString(R.string.common_delete));
            if (this.mOperationTitleID == 0) {
                this.mTipsView.setText(getApplicationContext().getString(R.string.phone_spcae_clean_tips_new_copy, new Object[]{Formatter.formatFileSize(getApplicationContext(), (long) checkNum)}));
            } else {
                this.mCheckSize = getCheckedSize();
                this.mTipsView.setText(getApplicationContext().getString(R.string.phone_spcae_clean_tips_new_copy, new Object[]{Formatter.formatFileSize(getApplicationContext(), this.mCheckSize)}));
            }
        }
    }

    private void updateActionBarTitle(int checkNum) {
        if (this.actionBar == null) {
            HwLog.i(TAG, "actionBar not found");
            return;
        }
        Activity ac = getActivity();
        if (checkNum <= 0) {
            this.mActionTitle.setText(getString(R.string.actionbar_unselected));
            if (ac != null) {
                ac.setTitle(getString(R.string.actionbar_unselected));
            }
            this.mActionNum.setVisibility(8);
        } else {
            this.mActionTitle.setText(getString(R.string.actionbar_select_count));
            if (ac != null) {
                ac.setTitle(getString(R.string.actionbar_select_count));
            }
            this.mActionNum.setText(String.valueOf(checkNum));
            this.mActionNum.setVisibility(0);
        }
    }

    private long getCheckedSize() {
        long checkedSize = 0;
        for (ITrashItem item : this.mTotalList) {
            if (item.isChecked()) {
                checkedSize += item.getTrashSize();
            }
        }
        return checkedSize;
    }

    protected void onListItemClick(AdapterView<?> adapterView, View v, int position, long id) {
        if (getActivity() != null) {
            ITrashItem item = (ITrashItem) getAdapter().getItem(position);
            if (item instanceof FileTrashItem) {
                String path = ((FileTrashItem) item).getTrashPath();
                if (new File(path).exists() && !TextUtils.isEmpty(path)) {
                    Intent requestIntent = new Intent();
                    requestIntent.setClass(getActivity(), PhotoViewerActivity.class);
                    requestIntent.putExtra(SecondaryConstant.PHOTO_TRASH_PATH_EXTRA, path);
                    try {
                        startActivity(requestIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private TrashTransFunc getTransFunc(int trashType) {
        switch (trashType) {
            case 128:
                return PhotoTrashItem.sTransFunc;
            default:
                return FileTrashItem.getTransFunc(trashType);
        }
    }
}
