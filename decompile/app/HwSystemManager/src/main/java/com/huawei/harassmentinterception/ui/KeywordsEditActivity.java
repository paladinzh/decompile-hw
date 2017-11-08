package com.huawei.harassmentinterception.ui;

import android.app.ActionBar;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.huawei.android.app.ActionBarEx;
import com.huawei.harassmentinterception.common.CommonObject.KeywordsInfo;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KeywordsEditActivity extends HsmActivity {
    private static final String TAG = KeywordsFragment.class.getSimpleName();
    private KeywordsListAdapter mAdapter;
    private int mCheckedId = -1;
    private int mCheckerPosition = 0;
    private MenuItem mDelBtn;
    private boolean mFirstTimeCheck = true;
    private boolean mFirstTimeFresh = true;
    private boolean mIsAllChecked = false;
    private List<KeywordsInfo> mKeywordsList = new ArrayList();
    private ListView mKeywordsListView;
    private KeywordsLoadingTask mLoadingTask;
    private Menu mMenu;
    private RelativeLayout mNoDataLayout;
    private ProgressBar mProgressBar;
    private MenuItem mSelAllBtn;
    private TextView mSelectedCountView;
    private TextView mTitleView;

    class KeywordsListAdapter extends BaseAdapter {
        KeywordsListAdapter() {
        }

        public int getCount() {
            return KeywordsEditActivity.this.mKeywordsList.size();
        }

        public Object getItem(int index) {
            return KeywordsEditActivity.this.mKeywordsList.get(index);
        }

        public long getItemId(int arg0) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup viewgroup) {
            ListItemViewHolder viewHolder;
            KeywordsInfo keyword = (KeywordsInfo) KeywordsEditActivity.this.mKeywordsList.get(position);
            if (convertView == null) {
                convertView = KeywordsEditActivity.this.getLayoutInflater().inflate(R.layout.common_list_item_singleline_checkbox, null);
                viewHolder = new ListItemViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ListItemViewHolder) convertView.getTag();
            }
            setAppViewContent(viewHolder, keyword);
            return convertView;
        }

        private void setAppViewContent(ListItemViewHolder holder, KeywordsInfo keyword) {
            if (TextUtils.isEmpty(keyword.getValue())) {
                holder.txKeywords.setText("");
            } else {
                holder.txKeywords.setText(keyword.getValue());
            }
            holder.cbSelected.setChecked(keyword.isChecked());
        }
    }

    private class KeywordsLoadingTask extends AsyncTask<Void, Void, List<KeywordsInfo>> {
        protected List<KeywordsInfo> doInBackground(Void... voidParams) {
            try {
                List<KeywordsInfo> keywords = DBAdapter.getKeywordsList(KeywordsEditActivity.this.getApplicationContext());
                Collections.sort(keywords, KeywordsInfo.KEYWORD_ALP_COMPARATOR);
                for (KeywordsInfo keyword : keywords) {
                    if (KeywordsEditActivity.this.mFirstTimeCheck && keyword.getId() == KeywordsEditActivity.this.mCheckedId) {
                        boolean z;
                        keyword.setChecked(true);
                        KeywordsEditActivity keywordsEditActivity = KeywordsEditActivity.this;
                        if (KeywordsEditActivity.this.mFirstTimeCheck) {
                            z = false;
                        } else {
                            z = true;
                        }
                        keywordsEditActivity.mFirstTimeCheck = z;
                    }
                }
                return keywords;
            } catch (Exception e) {
                HwLog.e(KeywordsEditActivity.TAG, "KeywordsLoadingTask-doInBackground: Exception ", e);
                return null;
            }
        }

        protected void onPostExecute(List<KeywordsInfo> keywordsList) {
            KeywordsEditActivity.this.mKeywordsList.clear();
            if (KeywordsEditActivity.this.mFirstTimeFresh) {
                KeywordsEditActivity.this.mKeywordsListView.setSelection(KeywordsEditActivity.this.mCheckerPosition);
                KeywordsEditActivity.this.mFirstTimeFresh = !KeywordsEditActivity.this.mFirstTimeFresh;
            }
            if (!Utility.isNullOrEmptyList(keywordsList)) {
                KeywordsEditActivity.this.mKeywordsList.addAll(keywordsList);
            }
            KeywordsEditActivity.this.notifyKeywordsListAdapter();
        }
    }

    static class ListItemViewHolder {
        CheckBox cbSelected = null;
        TextView txKeywords = null;

        public ListItemViewHolder(View view) {
            this.txKeywords = (TextView) view.findViewById(ViewUtil.HWID_TEXT_1);
            this.cbSelected = (CheckBox) view.findViewById(R.id.single_line_checkbox);
            this.cbSelected.setVisibility(0);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null) {
                this.mCheckedId = intent.getIntExtra("id", -1);
                this.mCheckerPosition = intent.getIntExtra(ConstValues.GET_FIRST_ITEM_POSITION, 0);
            }
        }
        setContentView(R.layout.interception_fragment_keywords);
        getActionBar().setDisplayHomeAsUpEnabled(false);
        initActionBar();
        initView();
    }

    private void initActionBar() {
        View titleBarView = getLayoutInflater().inflate(R.layout.custom_actionbar_selecting, null);
        this.mTitleView = (TextView) titleBarView.findViewById(R.id.view_title);
        this.mTitleView.setText(getResources().getString(R.string.actionbar_unselected));
        this.mSelectedCountView = (TextView) titleBarView.findViewById(R.id.view_selected_count);
        ActionBar actionBar = getActionBar();
        ActionBarEx.setCustomTitle(actionBar, titleBarView);
        ActionBarEx.setStartIcon(actionBar, true, null, new OnClickListener() {
            public void onClick(View v) {
                KeywordsEditActivity.this.finish();
            }
        });
        ActionBarEx.setEndIcon(actionBar, false, null, null);
    }

    private void initView() {
        this.mKeywordsListView = (ListView) findViewById(R.id.keywords_list_view);
        this.mKeywordsListView.setItemsCanFocus(false);
        this.mProgressBar = (ProgressBar) findViewById(R.id.loading_progressbar);
        this.mAdapter = new KeywordsListAdapter();
        this.mKeywordsListView.setAdapter(this.mAdapter);
        this.mKeywordsListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ((KeywordsInfo) KeywordsEditActivity.this.mKeywordsList.get(position)).reverseCheckStatus();
                KeywordsEditActivity.this.refreshListview();
            }
        });
    }

    private void refreshListview() {
        if (this.mKeywordsList.size() > 0) {
            this.mKeywordsListView.setVisibility(0);
            updateNoDataView(8);
        } else {
            this.mKeywordsListView.setVisibility(8);
            updateNoDataView(0);
        }
        this.mAdapter.notifyDataSetChanged();
        updateMenuStatus();
    }

    private void updateNoDataView(int visibility) {
        if (visibility == 0) {
            if (this.mNoDataLayout == null) {
                ViewStub stub = (ViewStub) findViewById(R.id.viewstub_no_keywords);
                if (stub != null) {
                    stub.inflate();
                }
                this.mNoDataLayout = (RelativeLayout) findViewById(R.id.no_keywords_view);
            }
            this.mNoDataLayout.setVisibility(0);
        } else if (this.mNoDataLayout != null) {
            this.mNoDataLayout.setVisibility(8);
        }
    }

    public void onResume() {
        refurbishKeywordsList();
        super.onResume();
    }

    private void refurbishKeywordsList() {
        if (this.mLoadingTask == null) {
            showLoadingProcess(Boolean.valueOf(true));
            this.mLoadingTask = new KeywordsLoadingTask();
            this.mLoadingTask.execute(new Void[0]);
        }
    }

    private void showLoadingProcess(Boolean bShow) {
        if (this.mProgressBar != null) {
            int i;
            ProgressBar progressBar = this.mProgressBar;
            if (bShow.booleanValue()) {
                i = 0;
            } else {
                i = 8;
            }
            progressBar.setVisibility(i);
        }
    }

    private void notifyKeywordsListAdapter() {
        showLoadingProcess(Boolean.valueOf(false));
        this.mAdapter.notifyDataSetChanged();
        refreshListview();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.interception_edit_menu, menu);
        this.mMenu = menu;
        this.mDelBtn = menu.findItem(R.id.menu_delete);
        this.mSelAllBtn = menu.findItem(R.id.menu_select_all);
        updateMenuStatus();
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                break;
            case R.id.menu_delete:
                deleteSelected();
                break;
            case R.id.menu_select_all:
                selectAll();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteSelected() {
        List selectedKeywords = new ArrayList();
        for (KeywordsInfo keywordsInfo : this.mKeywordsList) {
            if (keywordsInfo.isChecked()) {
                selectedKeywords.add(keywordsInfo);
            }
        }
        if (!Utility.isNullOrEmptyList(selectedKeywords)) {
            if (DBAdapter.deleteKeywords(getApplicationContext(), selectedKeywords) > 0) {
                String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, String.valueOf(DBAdapter.deleteKeywords(getApplicationContext(), selectedKeywords)));
                HsmStat.statE((int) Events.E_HARASSMENT_BLOCK_DEL_KEYWORDS, statParam);
            }
            finish();
        }
    }

    private void selectAll() {
        boolean newCheckedStatus = !this.mIsAllChecked;
        for (KeywordsInfo keywordsInfo : this.mKeywordsList) {
            keywordsInfo.setChecked(newCheckedStatus);
        }
        refreshListview();
    }

    private void updateMenuStatus() {
        if (this.mMenu != null) {
            int nSelectedCount = 0;
            for (KeywordsInfo keywords : this.mKeywordsList) {
                if (keywords.isChecked()) {
                    nSelectedCount++;
                }
            }
            if (nSelectedCount <= 0) {
                this.mTitleView.setText(R.string.actionbar_unselected);
                this.mSelectedCountView.setVisibility(8);
                this.mDelBtn.setEnabled(false);
            } else {
                this.mTitleView.setText(R.string.actionbar_select_count);
                this.mSelectedCountView.setVisibility(0);
                this.mSelectedCountView.setText("" + Utility.getLocaleNumber(nSelectedCount));
                this.mDelBtn.setEnabled(true);
            }
            if (this.mKeywordsList.isEmpty()) {
                this.mSelAllBtn.setEnabled(false);
                this.mSelAllBtn.setTitle(R.string.select_all);
                this.mSelAllBtn.setChecked(false);
                return;
            }
            this.mIsAllChecked = nSelectedCount == this.mKeywordsList.size();
            this.mSelAllBtn.setEnabled(true);
            if (this.mIsAllChecked) {
                this.mSelAllBtn.setTitle(R.string.unselect_all);
                this.mSelAllBtn.setIcon(R.drawable.menu_check_pressed);
                this.mSelAllBtn.setChecked(true);
            } else {
                this.mSelAllBtn.setTitle(R.string.select_all);
                this.mSelAllBtn.setIcon(R.drawable.menu_check_status);
                this.mSelAllBtn.setChecked(false);
            }
        }
    }

    protected void onDestroy() {
        super.onDestroy();
    }
}
