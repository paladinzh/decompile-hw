package com.huawei.systemmanager.netassistant.traffic.notrafficapp;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import com.huawei.netassistant.util.CommonConstantUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Constant;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.netassistant.netapp.bean.NoTrafficAppInfo;
import com.huawei.systemmanager.netassistant.traffic.notrafficapp.adapter.NoTrafficAppAdapter;
import com.huawei.systemmanager.netassistant.utils.ViewUtils;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.content.HsmBroadcastReceiver;

public class NoTrafficAppFragment extends Fragment implements OnClickListener {
    private static final String TAG = "NoTrafficAppFragment";
    private NoTrafficAppAdapter mAdapter;
    private View mEmptyView;
    private CheckBox mHeadCheckBox;
    private View mHeadView;
    private String mImsi;
    private ListView mListView;
    private NoTrafficAppDbInfo mNoTrafficAppDbInfo;
    private ProgressBar mProgressBar;

    public class HelpAsyncTask extends AsyncTask<Void, Void, Void> {
        public static final int INIT_TASK = 1;
        public static final int SET_HEAD_TASK = 3;
        public static final int SET_ITEM_TASK = 2;
        private final int mAsyncTaskId;
        private final int mAsyncTaskPos;
        private final boolean mAsyncTaskResult;
        private ProgressDialog progressDialog;

        public HelpAsyncTask(NoTrafficAppFragment this$0, int taskId) {
            this(taskId, false, -1);
        }

        public HelpAsyncTask(NoTrafficAppFragment this$0, int taskId, boolean result) {
            this(taskId, result, -1);
        }

        public HelpAsyncTask(int taskId, boolean result, int pos) {
            this.mAsyncTaskId = taskId;
            this.mAsyncTaskResult = result;
            this.mAsyncTaskPos = pos;
        }

        protected void onPreExecute() {
            if (this.mAsyncTaskId == 1) {
                NoTrafficAppFragment.this.showLoadingView();
            } else if (this.mAsyncTaskId == 3) {
                showWaitingDialog();
            }
        }

        protected Void doInBackground(Void... params) {
            if (this.mAsyncTaskId == 1) {
                HwLog.i(NoTrafficAppFragment.TAG, "doInBackground, INIT_TASK");
                NoTrafficAppFragment.this.mNoTrafficAppDbInfo = new NoTrafficAppDbInfo(NoTrafficAppFragment.this.mImsi);
                NoTrafficAppFragment.this.mNoTrafficAppDbInfo.initAllData();
            } else if (this.mAsyncTaskId == 2) {
                HwLog.i(NoTrafficAppFragment.TAG, "doInBackground, SET_ITEM_TASK");
                setItem(this.mAsyncTaskPos, this.mAsyncTaskResult);
            } else if (this.mAsyncTaskId == 3) {
                HwLog.i(NoTrafficAppFragment.TAG, "doInBackground, SET_HEAD_TASK");
                setHead(this.mAsyncTaskResult);
            }
            HwLog.i(NoTrafficAppFragment.TAG, HsmBroadcastReceiver.METHOD_DO_IN_BACKGROUND);
            return null;
        }

        protected void onPostExecute(Void params) {
            if (this.mAsyncTaskId == 1) {
                NoTrafficAppFragment.this.mAdapter.swapData(NoTrafficAppFragment.this.mNoTrafficAppDbInfo.getAllUidList());
                HwLog.i(NoTrafficAppFragment.TAG, "onPostExecute, INIT_TASK");
                NoTrafficAppFragment.this.showContentView();
                checkHeadCheckBox();
            } else if (this.mAsyncTaskId == 2) {
                HwLog.i(NoTrafficAppFragment.TAG, "onPostExecute, SET_ITEM_TASK");
                checkHeadCheckBox();
            } else if (this.mAsyncTaskId == 3) {
                HwLog.i(NoTrafficAppFragment.TAG, "onPostExecute, SET_HEAD_TASK");
                NoTrafficAppFragment.this.mAdapter.notifyDataSetChanged();
                dismissWaitingDialog();
            }
        }

        private void showWaitingDialog() {
            Activity ac = NoTrafficAppFragment.this.getActivity();
            if (ac != null) {
                this.progressDialog = new ProgressDialog(ac);
                this.progressDialog.setMessage(NoTrafficAppFragment.this.getString(R.string.data_usage_restrict_background_wait));
                this.progressDialog.show();
            }
        }

        private void dismissWaitingDialog() {
            if (this.progressDialog != null && this.progressDialog.isShowing()) {
                this.progressDialog.dismiss();
            }
        }

        private void setItem(int pos, boolean checked) {
            if (NoTrafficAppFragment.this.mNoTrafficAppDbInfo == null || NoTrafficAppFragment.this.mAdapter == null) {
                HwLog.i(NoTrafficAppFragment.TAG, "setItem, arg is wrong");
                return;
            }
            NoTrafficAppInfo info = NoTrafficAppFragment.this.mAdapter.getItem(pos);
            if (info == null) {
                HwLog.i(NoTrafficAppFragment.TAG, "setItem, info is wrong");
                return;
            }
            if (checked) {
                NoTrafficAppFragment.this.mNoTrafficAppDbInfo.save(info.getUid());
            } else {
                NoTrafficAppFragment.this.mNoTrafficAppDbInfo.clear(info.getUid());
            }
        }

        private void setHead(boolean checked) {
            if (NoTrafficAppFragment.this.mNoTrafficAppDbInfo == null) {
                HwLog.i(NoTrafficAppFragment.TAG, "setHead, arg is wrong");
                return;
            }
            if (checked) {
                NoTrafficAppFragment.this.mNoTrafficAppDbInfo.saveAll();
            } else {
                NoTrafficAppFragment.this.mNoTrafficAppDbInfo.clearAll();
            }
        }

        private void checkHeadCheckBox() {
            if (NoTrafficAppFragment.this.mAdapter != null) {
                ViewUtils.setChecked(NoTrafficAppFragment.this.mHeadCheckBox, NoTrafficAppFragment.this.mAdapter.isAllChecked());
            }
        }
    }

    public static NoTrafficAppFragment newInstance(Bundle bundle) {
        NoTrafficAppFragment fragment = new NoTrafficAppFragment();
        fragment.setArguments(new Bundle(bundle));
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        this.mImsi = getArguments().getString(CommonConstantUtil.KEY_NETASSISTANT_IMSI, "");
    }

    public void onResume() {
        super.onResume();
        new HelpAsyncTask(this, 1).execute(new Void[0]);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mAdapter = new NoTrafficAppAdapter(getActivity(), this);
        View view = inflater.inflate(R.layout.fragment_no_traffic_app, container, false);
        this.mHeadCheckBox = (CheckBox) view.findViewById(R.id.head_checkbox);
        this.mProgressBar = (ProgressBar) view.findViewById(R.id.loading_progressbar);
        this.mListView = (ListView) view.findViewById(R.id.list);
        this.mHeadView = view.findViewById(R.id.head_view);
        this.mHeadView.setBackgroundResource(getResources().getIdentifier(ViewUtil.EMUI_SELECTOR_BACKGROUND, null, null));
        this.mHeadView.setOnClickListener(this);
        this.mEmptyView = view.findViewById(R.id.empty_view);
        this.mHeadCheckBox.setClickable(false);
        this.mHeadCheckBox.setFocusable(false);
        this.mListView.setAdapter(this.mAdapter);
        this.mListView.setTag(Constant.DISALBE_LISTVIEW_CHECKOBX_MULTI_SELECT);
        showLoadingView();
        return view;
    }

    private void showLoadingView() {
        HwLog.i(TAG, "showLoadingView");
        ViewUtils.setVisibility(this.mHeadView, 4);
        ViewUtils.setVisibility(this.mProgressBar, 0);
        ViewUtils.setVisibility(this.mListView, 4);
        ViewUtils.setVisibility(this.mEmptyView, 8);
    }

    private void showContentView() {
        HwLog.i(TAG, "showContentView");
        ViewUtils.setVisibility(this.mProgressBar, 4);
        if (this.mAdapter == null || this.mAdapter.getCount() <= 0) {
            ViewUtils.setVisibility(this.mHeadView, 4);
            ViewUtils.setVisibility(this.mListView, 4);
            ViewUtils.setVisibility(this.mEmptyView, 0);
            return;
        }
        ViewUtils.setVisibility(this.mHeadView, 0);
        ViewUtils.setVisibility(this.mListView, 0);
        ViewUtils.setVisibility(this.mEmptyView, 8);
    }

    public void onClick(View v) {
        CheckBox checkBox;
        switch (v.getId()) {
            case R.id.head_view:
                if (this.mHeadCheckBox != null) {
                    checkBox = this.mHeadCheckBox;
                    if (this.mHeadCheckBox.isChecked()) {
                        this.mHeadCheckBox.setChecked(false);
                    } else {
                        this.mHeadCheckBox.setChecked(true);
                    }
                    new HelpAsyncTask(this, 3, checkBox.isChecked()).execute(new Void[0]);
                    return;
                }
                return;
            case R.id.checkbox:
                checkBox = (CheckBox) v;
                new HelpAsyncTask(2, checkBox.isChecked(), ((Integer) v.getTag()).intValue()).execute(new Void[0]);
                return;
            default:
                return;
        }
    }
}
