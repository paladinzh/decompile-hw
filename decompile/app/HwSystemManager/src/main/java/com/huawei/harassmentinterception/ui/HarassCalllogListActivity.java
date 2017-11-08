package com.huawei.harassmentinterception.ui;

import android.app.ActionBar;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.huawei.android.app.ActionBarEx;
import com.huawei.harassmentinterception.blackwhitelist.DataShareManager;
import com.huawei.harassmentinterception.common.CommonObject.CallInfo;
import com.huawei.harassmentinterception.common.CommonObject.ParcelableBlacklistItem;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.harassmentinterception.util.CommonObjectHelper;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.TimeUtil;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class HarassCalllogListActivity extends HsmActivity {
    private static final String TAG = HarassCalllogListActivity.class.getSimpleName();
    private MenuItem mAddBtn;
    private RelativeLayout mCallLogListLayout = null;
    private List<CallInfo> mDataList = new ArrayList();
    private CallLogListAdapter mDataListAdapter = null;
    private boolean mIsAllChecked;
    private Menu mMenu;
    private View mNoDataLayout = null;
    private TextView mOperatorView = null;
    private ProgressBar mProgressBar = null;
    private MenuItem mSelAllBtn;
    private TextView mSelectedCountView = null;
    private long mTodayStartTime = TimeUtil.getTodayStartTime();
    private ViewGroup mViewGroup = null;

    class CallLogListAdapter extends BaseAdapter {
        private List<CallInfo> mList = new ArrayList();

        CallLogListAdapter() {
        }

        public int getCount() {
            return this.mList.size();
        }

        public Object getItem(int i) {
            return null;
        }

        public long getItemId(int i) {
            return 0;
        }

        public void setData(List<CallInfo> dataList) {
            this.mList.clear();
            this.mList.addAll(dataList);
            notifyDataSetChanged();
        }

        public View getView(int position, View convertView, ViewGroup viewgroup) {
            if (convertView == null) {
                return createListViewWithTimeAxis(position, viewgroup);
            }
            fillViewHolderWithData((ViewHolder) convertView.getTag(), (CallInfo) this.mList.get(position), position);
            return convertView;
        }

        private View createListViewWithTimeAxis(int position, ViewGroup viewgroup) {
            View interView = HarassCalllogListActivity.this.getLayoutInflater().inflate(R.layout.interception_calllog_list_item, viewgroup, false);
            ViewHolder holder = new ViewHolder();
            holder._contactInfo = (TextView) interView.findViewById(R.id.calllog_contactInfo);
            holder._checkBox = (CheckBox) interView.findViewById(R.id.calllog_checkbox);
            holder._info = (TextView) interView.findViewById(R.id.calllog_location);
            holder._time = (TextView) interView.findViewById(R.id.time);
            fillViewHolderWithData(holder, (CallInfo) this.mList.get(position), position);
            interView.setClickable(true);
            interView.setTag(holder);
            interView.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    ((ViewHolder) view.getTag())._checkBox.toggle();
                }
            });
            interView.setTag(holder);
            return interView;
        }

        private void fillViewHolderWithData(ViewHolder holder, CallInfo callInfo, final int position) {
            holder._contactInfo.setText(callInfo.getContactInfo(HarassCalllogListActivity.this));
            String location = callInfo.getGeoLocation();
            if (TextUtils.isEmpty(location)) {
                holder._info.setText("");
                holder._info.setVisibility(8);
            } else {
                holder._info.setText(location);
                holder._info.setVisibility(0);
            }
            int index = position;
            holder._checkBox.setOnCheckedChangeListener(null);
            holder._checkBox.setChecked(callInfo.isSelected());
            holder._checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton compoundbutton, boolean isChecked) {
                    if (isChecked) {
                        ((CallInfo) CallLogListAdapter.this.mList.get(position)).setSelected(true);
                    } else {
                        ((CallInfo) CallLogListAdapter.this.mList.get(position)).setSelected(false);
                    }
                    HarassCalllogListActivity.this.updateSelectingStatus();
                }
            });
            holder._checkBox.setFocusable(false);
            String timeDes = "";
            long recordTime = callInfo.getDate();
            if (recordTime >= HarassCalllogListActivity.this.mTodayStartTime) {
                timeDes = DateUtils.formatDateTime(GlobalContext.getContext(), recordTime, 1);
            } else {
                timeDes = DateUtils.formatDateTime(GlobalContext.getContext(), recordTime, 16);
            }
            holder._time.setText(timeDes);
        }
    }

    private class DataLoadingTask extends AsyncTask<Void, Void, List<CallInfo>> {
        private DataLoadingTask() {
        }

        protected List<CallInfo> doInBackground(Void... params) {
            List<CallInfo> callInfoList = Collections.emptyList();
            try {
                HwLog.i(HarassCalllogListActivity.TAG, "DataLoadingTask-doInBackground:getActivity = ");
                callInfoList = DBAdapter.getInterceptedCalls(GlobalContext.getContext());
            } catch (Exception e) {
                HwLog.e(HarassCalllogListActivity.TAG, "DataLoadingTask-doInBackground: Exception", e);
            }
            return callInfoList;
        }

        protected void onPostExecute(List<CallInfo> callInfoList) {
            HarassCalllogListActivity.this.mDataList = callInfoList;
            HarassCalllogListActivity.this.refreshListView();
        }
    }

    private static class ViewHolder {
        CheckBox _checkBox;
        TextView _contactInfo;
        TextView _info;
        TextView _time;

        private ViewHolder() {
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interception_calllog_list);
        this.mViewGroup = (ViewGroup) ((RelativeLayout) findViewById(R.id.rl_calllog_list)).getParent();
        initActionBar();
        this.mProgressBar = (ProgressBar) findViewById(R.id.loading_progressbar);
        this.mNoDataLayout = findViewById(R.id.no_calllog_view);
        ViewUtil.initEmptyViewMargin(GlobalContext.getContext(), this.mNoDataLayout);
        ((TextView) findViewById(R.id.no_calllog_info)).setText(getResources().getString(R.string.harassmentInterception_no_callog_info));
        this.mCallLogListLayout = (RelativeLayout) findViewById(R.id.calllog_layout);
        ListView callLogListView = (ListView) findViewById(R.id.calllog_list_view);
        this.mDataListAdapter = new CallLogListAdapter();
        callLogListView.setAdapter(this.mDataListAdapter);
        this.mProgressBar.setVisibility(0);
        loadMessage();
        setTitle(R.string.ActionBar_DoNotDisturb_Unselect);
    }

    protected void onResume() {
        super.onResume();
    }

    private void loadMessage() {
        new DataLoadingTask().execute(new Void[0]);
    }

    private void updateSelectingStatus() {
        boolean z = true;
        if (this.mMenu != null) {
            if (this.mDataList.isEmpty()) {
                this.mAddBtn.setVisible(false);
                this.mSelAllBtn.setVisible(false);
                return;
            }
            this.mAddBtn.setVisible(true);
            this.mSelAllBtn.setVisible(true);
            int number = CommonObjectHelper.getCheckedItemNum(this.mDataList);
            this.mIsAllChecked = CommonObjectHelper.isAllItemChecked(this.mDataList, number);
            if (number > 0) {
                this.mOperatorView.setText(R.string.ActionBar_DoNotDisturb_Select);
                this.mSelectedCountView.setVisibility(0);
                this.mSelectedCountView.setText("" + Utility.getLocaleNumber(number));
                setTitle(getString(R.string.ActionBar_DoNotDisturb_Select) + Utility.getLocaleNumber(number));
                this.mAddBtn.setEnabled(true);
            } else {
                setTitle(R.string.ActionBar_DoNotDisturb_Unselect);
                this.mOperatorView.setText(R.string.ActionBar_DoNotDisturb_Unselect);
                this.mSelectedCountView.setVisibility(8);
                this.mAddBtn.setEnabled(false);
            }
            if (number == 0 || !this.mIsAllChecked) {
                this.mSelAllBtn.setIcon(R.drawable.menu_check_status);
                this.mSelAllBtn.setTitle(R.string.select_all);
                this.mSelAllBtn.setChecked(false);
            } else {
                this.mSelAllBtn.setIcon(R.drawable.menu_check_pressed);
                this.mSelAllBtn.setTitle(R.string.unselect_all);
                this.mSelAllBtn.setChecked(true);
            }
            MenuItem menuItem = this.mSelAllBtn;
            if (this.mDataList.size() == 0) {
                z = false;
            }
            menuItem.setEnabled(z);
        }
    }

    private void refreshListView() {
        if (Utility.isNullOrEmptyList(this.mDataList)) {
            this.mCallLogListLayout.setVisibility(8);
            this.mNoDataLayout.setVisibility(0);
        } else {
            this.mCallLogListLayout.setVisibility(0);
            this.mNoDataLayout.setVisibility(8);
        }
        this.mProgressBar.setVisibility(8);
        this.mDataListAdapter.setData(this.mDataList);
        updateSelectingStatus();
    }

    private void initActionBar() {
        View titleBarView = getLayoutInflater().inflate(R.layout.custom_actionbar_selecting, this.mViewGroup, false);
        this.mOperatorView = (TextView) titleBarView.findViewById(R.id.view_title);
        this.mOperatorView.setText(getResources().getString(R.string.ActionBar_DoNotDisturb_Unselect));
        this.mSelectedCountView = (TextView) titleBarView.findViewById(R.id.view_selected_count);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
        ActionBarEx.setCustomTitle(actionBar, titleBarView);
        ActionBarEx.setStartIcon(actionBar, true, null, new OnClickListener() {
            public void onClick(View arg0) {
                HarassCalllogListActivity.this.finish();
            }
        });
    }

    private void finishActivityWithResult() {
        filterRepeatNumber();
        if (this.mDataList.isEmpty()) {
            finish();
            return;
        }
        ArrayList<ParcelableBlacklistItem> selectedList = new ArrayList();
        for (CallInfo callInfo : this.mDataList) {
            ParcelableBlacklistItem item = new ParcelableBlacklistItem();
            item.setPhone(callInfo.getPhone());
            item.setName(callInfo.getName());
            selectedList.add(item);
        }
        Intent intent = new Intent();
        DataShareManager.getInstance().setWhitelistBuff(selectedList);
        setResult(-1, intent);
        finish();
    }

    private void filterRepeatNumber() {
        Set<String> numberSet = new HashSet();
        Iterator<CallInfo> iter = this.mDataList.iterator();
        while (iter.hasNext()) {
            CallInfo callLog = (CallInfo) iter.next();
            boolean isSelected = callLog.isSelected();
            String matchedNumber = callLog.getMatchedNumber();
            if (!isSelected || TextUtils.isEmpty(matchedNumber) || numberSet.contains(matchedNumber)) {
                iter.remove();
            }
            if (isSelected) {
                numberSet.add(matchedNumber);
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.interception_add_menu, menu);
        this.mMenu = menu;
        this.mAddBtn = menu.findItem(R.id.add_contacts);
        this.mSelAllBtn = menu.findItem(R.id.select_all_contacts);
        updateSelectingStatus();
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                break;
            case R.id.add_contacts:
                finishActivityWithResult();
                break;
            case R.id.select_all_contacts:
                selectAllContacts();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectAllContacts() {
        this.mIsAllChecked = !this.mIsAllChecked;
        CommonObjectHelper.doSelect(this.mDataList, this.mIsAllChecked);
        refreshListView();
    }
}
