package com.huawei.systemmanager.spacecleanner.ui.photomanager;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.common.imageloader.core.ImageLoader;
import com.common.imageloader.core.listener.PauseOnScrollListener;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.netassistant.utils.ViewUtils;
import com.huawei.systemmanager.spacecleanner.CommonHandler;
import com.huawei.systemmanager.spacecleanner.CommonHandler.MessageHandler;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.engine.base.CleanTask;
import com.huawei.systemmanager.spacecleanner.engine.base.ICleanListener;
import com.huawei.systemmanager.spacecleanner.engine.base.ICleanListener.SimpleListener;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.TrashGroup;
import com.huawei.systemmanager.spacecleanner.ui.ListGrid.ListGridListener.OnSizeChangeListener;
import com.huawei.systemmanager.spacecleanner.ui.StatisticalData;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.Convertor;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.DataHolder;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.SecondaryDialogFragment;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.onCallTrashSetListener;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.FileTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.SimilarPhotoTrashItem;
import com.huawei.systemmanager.spacecleanner.view.GridViewWithHeaderAndFooter;
import com.huawei.systemmanager.spacecleanner.view.SquareFrameLayout;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BasePhotoGridFragment extends Fragment implements MessageHandler {
    private static final int CLEAN_FINISH_MSG = 2;
    private static final int CLEAN_START_MSG = 1;
    private static final int INIT_DATA_MSG = 3;
    private static final String TAG = "BasePhotoGridFragment";
    private Activity mActivity = null;
    private BasePhotoGridAdapter mAdapter;
    private MenuItem mCheckAllMenu;
    private int mCheckedCount;
    private ICleanListener mCleanCallBack = new SimpleListener() {
        public void onCleanEnd(boolean canceled, long cleanedSize) {
            HwLog.i(BasePhotoGridFragment.TAG, "onCleanEnd");
            BasePhotoGridFragment.this.mHandler.obtainMessage(2, Long.valueOf(cleanedSize)).sendToTarget();
        }
    };
    private ProgressDialog mDelProgressDialog;
    private MenuItem mDeleteMenu;
    private View mEmptyView;
    private GridViewWithHeaderAndFooter mGridView;
    private final Handler mHandler = new CommonHandler(this);
    private boolean mIsCheckedAll;
    private LinearLayout mLinearLayout;
    private TrashScanHandler mScanHandler;
    private StatisticalData mStatisticalData;
    private TextView mTipsView;
    private onCallTrashSetListener mTrashCallBack;
    private final List<ITrashItem> mTrashItems = new LinkedList();
    private int mTrashType;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HwLog.i(TAG, "onCreate");
        setHasOptionsMenu(true);
        this.mActivity = getActivity();
        this.mHandler.sendEmptyMessageDelayed(3, 300);
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mStatisticalData != null) {
            this.mStatisticalData.destroy();
        }
    }

    public void onAttach(Activity ac) {
        super.onAttach(ac);
        if (ac instanceof onCallTrashSetListener) {
            this.mTrashCallBack = (onCallTrashSetListener) ac;
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.base_photo_grid_layout, container, false);
        view.setPadding(SquareFrameLayout.getRemainWidth() / 2, 0, SquareFrameLayout.getRemainWidth() / 2, 0);
        this.mTipsView = (TextView) view.findViewById(R.id.photo_tips_base);
        this.mLinearLayout = (LinearLayout) view.findViewById(R.id.linearlayout_photo_base);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mGridView = (GridViewWithHeaderAndFooter) view.findViewById(R.id.grid_view);
        this.mGridView.setPadding(SquareFrameLayout.getRemainWidth() / 2, 0, SquareFrameLayout.getRemainWidth() / 2, 0);
        this.mGridView.addFooterView(this.mActivity.getLayoutInflater().inflate(R.layout.blank_footer_view, this.mGridView, false), null, false);
        this.mEmptyView = view.findViewById(R.id.empty_view);
        ViewUtil.initEmptyViewMargin(GlobalContext.getContext(), this.mEmptyView);
        this.mAdapter = new BasePhotoGridAdapter();
        initView();
        initEmptyView();
        initData();
        refreshView();
    }

    private void initView() {
        if (this.mGridView != null) {
            this.mGridView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
            this.mGridView.setAdapter(this.mAdapter);
            this.mGridView.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    ITrashItem item = (ITrashItem) BasePhotoGridFragment.this.mTrashItems.get(position);
                    if (item != null) {
                        PhotoViewerActivity.startPhotoViewer(BasePhotoGridFragment.this.getActivity(), item.getTrashPath());
                    }
                }
            });
        }
        if (this.mAdapter != null) {
            this.mAdapter.setSizeListener(new OnSizeChangeListener() {
                public void onSizeChanged(long checkedSize, long allSize, boolean isAll, int checkedCount) {
                    if (BasePhotoGridFragment.this.mDeleteMenu != null && BasePhotoGridFragment.this.mCheckAllMenu != null) {
                        boolean z;
                        BasePhotoGridFragment.this.mCheckAllMenu.setIcon(isAll ? R.drawable.menu_check_pressed : R.drawable.menu_check_status);
                        BasePhotoGridFragment.this.mCheckAllMenu.setTitle(isAll ? R.string.unselect_all : R.string.select_all);
                        BasePhotoGridFragment.this.mCheckAllMenu.setChecked(isAll);
                        BasePhotoGridFragment.this.mIsCheckedAll = isAll;
                        BasePhotoGridFragment.this.mTipsView.setText(GlobalContext.getString(R.string.phone_spcae_clean_tips_new_copy, Formatter.formatFileSize(BasePhotoGridFragment.this.getApplicationContext(), checkedSize)));
                        BasePhotoGridFragment.this.mDeleteMenu.setTitle(GlobalContext.getString(R.string.common_delete));
                        MenuItem -get2 = BasePhotoGridFragment.this.mDeleteMenu;
                        if (checkedSize != 0) {
                            z = true;
                        } else {
                            z = false;
                        }
                        -get2.setEnabled(z);
                        BasePhotoGridFragment.this.mCheckedCount = checkedCount;
                    }
                }
            });
        }
        if (this.mEmptyView == null) {
            HwLog.i(TAG, "initEmptyView, empty view is null");
        }
    }

    private void initEmptyView() {
        if (this.mTrashCallBack != null) {
            DataHolder holder = this.mTrashCallBack.getDataHolder();
            if (holder != null && holder.param != null && this.mEmptyView != null) {
                ViewUtils.setText((TextView) this.mEmptyView.findViewById(R.id.empty_text), holder.param.getEmptyTextID());
                ViewUtils.setImageResource((ImageView) this.mEmptyView.findViewById(R.id.empty_image), holder.param.getEmptyIconID());
            }
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.cache_clean_menu, menu);
        this.mCheckAllMenu = menu.findItem(R.id.select_all);
        this.mDeleteMenu = menu.findItem(R.id.cache_clean_menu_item);
        this.mDeleteMenu.setTitle(GlobalContext.getString(R.string.common_delete));
        this.mDeleteMenu.setEnabled(false);
    }

    protected Context getApplicationContext() {
        return GlobalContext.getContext();
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                showDeleteProgressDialog();
                return;
            case 2:
                HwLog.i(TAG, "clean finished, update data");
                String str = FileUtil.getFileSize(Long.parseLong(msg.obj.toString()));
                dismissDeleteProgressDialog();
                updateData();
                if (this.mStatisticalData != null) {
                    this.mStatisticalData.sendDeleteMsg(str);
                    return;
                }
                return;
            case 3:
                if (this.mAdapter != null) {
                    this.mAdapter.refreshData();
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void updateData() {
        List<ITrashItem> list = new ArrayList();
        for (ITrashItem item : this.mTrashItems) {
            item.refreshContent();
            if (!item.isCleaned()) {
                list.add(item);
            }
        }
        this.mTrashItems.clear();
        this.mTrashItems.addAll(list);
        if (this.mAdapter != null) {
            this.mAdapter.swapData(this.mTrashItems);
        }
        refreshView();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cache_clean_menu_item:
                deletePhoto();
                break;
            case R.id.select_all:
                this.mIsCheckedAll = !this.mIsCheckedAll;
                if (this.mCheckAllMenu != null) {
                    this.mCheckAllMenu.setChecked(this.mIsCheckedAll);
                }
                selectAll();
                if (this.mStatisticalData != null) {
                    this.mStatisticalData.sendSelectAllMsg(this.mIsCheckedAll);
                    break;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectAll() {
        if (this.mAdapter != null) {
            this.mAdapter.selectAll(this.mIsCheckedAll);
        }
    }

    private void deletePhoto() {
        OnClickListener dialogListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case -1:
                        BasePhotoGridFragment.this.mHandler.sendEmptyMessage(1);
                        CleanTask.startClean(Convertor.covertTrashItem(BasePhotoGridFragment.this.getCheckedList()), BasePhotoGridFragment.this.mCleanCallBack, BasePhotoGridFragment.this.mScanHandler);
                        return;
                    default:
                        return;
                }
            }
        };
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        SecondaryDialogFragment newFragment = new SecondaryDialogFragment();
        Bundle bundle = new Bundle();
        if (this.mTrashCallBack != null) {
            bundle.putParcelable(SecondaryDialogFragment.ARG_SEC_DATA, this.mTrashCallBack.getDataHolder().param);
        }
        bundle.putInt(SecondaryDialogFragment.ARG_SEC_SELECT, this.mCheckedCount);
        bundle.putBoolean(SecondaryDialogFragment.ARG_SEC_ALL_SELECT, this.mIsCheckedAll);
        newFragment.setArguments(bundle);
        newFragment.setOnClickListener(dialogListener);
        newFragment.show(ft, "secondary_dialog");
    }

    private List<ITrashItem> getCheckedList() {
        List<ITrashItem> list = new ArrayList();
        for (ITrashItem item : this.mTrashItems) {
            if (item.isChecked()) {
                list.add(item);
            }
        }
        return list;
    }

    private void showDeleteProgressDialog() {
        Activity ac = getActivity();
        if (ac != null && !ac.isFinishing() && !ac.isDestroyed()) {
            if (this.mDelProgressDialog != null && this.mDelProgressDialog.isShowing()) {
                this.mDelProgressDialog.dismiss();
            }
            HwLog.i(TAG, "clean start, show progress dialog");
            this.mDelProgressDialog = ProgressDialog.show(ac, null, GlobalContext.getString(R.string.space_common_msg_cleaning), true, false);
        }
    }

    private void dismissDeleteProgressDialog() {
        Activity ac = getActivity();
        if (ac != null && !ac.isFinishing() && !ac.isDestroyed() && this.mDelProgressDialog != null && this.mDelProgressDialog.isShowing()) {
            HwLog.i(TAG, "dismiss dialog");
            this.mDelProgressDialog.dismiss();
            this.mDelProgressDialog = null;
        }
    }

    private void initData() {
        if (this.mTrashCallBack == null) {
            HwLog.i(TAG, "mTrashListener is null");
            showEmptyView();
            return;
        }
        DataHolder dataHolder = this.mTrashCallBack.getDataHolder();
        if (dataHolder == null || dataHolder.param == null || dataHolder.trashHander == null) {
            HwLog.e(TAG, "initData, dataHolder is null");
            showEmptyView();
            return;
        }
        this.mScanHandler = dataHolder.trashHander;
        this.mTrashType = dataHolder.param.getTrashType();
        TrashTransFunc transFunc = getTransFunc(this.mTrashType);
        TrashGroup<Trash> group = this.mScanHandler.getTrashByType(this.mTrashType);
        if (group != null) {
            for (Trash trash : group) {
                if (!trash.isCleaned()) {
                    ITrashItem item = transFunc.apply(trash);
                    if (item != null) {
                        item.setChecked(trash.isSuggestClean());
                        this.mTrashItems.add(item);
                    }
                }
            }
        }
        if (this.mAdapter != null) {
            this.mAdapter.swapData(this.mTrashItems);
        }
        this.mStatisticalData = StatisticalData.newInstance(dataHolder.param, StatisticalData.FRAGMENT_TYPE_LIST_GRID);
    }

    private void refreshView() {
        if (this.mAdapter == null || this.mAdapter.isEmpty()) {
            showEmptyView();
        } else {
            showGridView();
        }
    }

    private void showEmptyView() {
        ViewUtils.setVisibility(this.mEmptyView, 0);
        ViewUtils.setVisibility(this.mGridView, 8);
        ViewUtils.setVisible(this.mCheckAllMenu, false);
        ViewUtils.setVisible(this.mDeleteMenu, false);
        ViewUtils.setVisibility(this.mLinearLayout, 8);
    }

    private void showGridView() {
        ViewUtils.setVisibility(this.mEmptyView, 8);
        ViewUtils.setVisibility(this.mGridView, 0);
    }

    private TrashTransFunc getTransFunc(int trashType) {
        switch (trashType) {
            case 4194304:
                return SimilarPhotoTrashItem.sTransFunc;
            default:
                return FileTrashItem.getTransFunc(trashType);
        }
    }
}
