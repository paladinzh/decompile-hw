package com.huawei.harassmentinterception.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.huawei.harassmentinterception.common.CommonObject.KeywordsInfo;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KeywordsFragment extends Fragment {
    private static final String TAG = KeywordsFragment.class.getSimpleName();
    private KeywordsListAdapter mAdapter;
    private AlertDialog mAddKeywordsDlg = null;
    private Context mContext = null;
    private View mFragmentView;
    private LayoutInflater mInflater;
    private List<KeywordsInfo> mKeywordsList = new ArrayList();
    private ListView mKeywordsListView;
    private KeywordsLoadingTask mLoadingTask;
    private RelativeLayout mNoDataLayout;
    private ProgressBar mProgressBar;

    class KeywordsListAdapter extends BaseAdapter {
        KeywordsListAdapter() {
        }

        public int getCount() {
            return KeywordsFragment.this.mKeywordsList.size();
        }

        public Object getItem(int index) {
            return KeywordsFragment.this.mKeywordsList.get(index);
        }

        public long getItemId(int arg0) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup viewgroup) {
            ListItemViewHolder viewHolder;
            if (convertView == null) {
                convertView = KeywordsFragment.this.mInflater.inflate(R.layout.common_list_item_singleline_checkbox, null);
                viewHolder = new ListItemViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ListItemViewHolder) convertView.getTag();
            }
            if (position >= KeywordsFragment.this.mKeywordsList.size()) {
                setAppViewContent(viewHolder, "");
                HwLog.w(KeywordsFragment.TAG, "getView: Invalid keyword position: " + position);
            } else {
                KeywordsInfo keyword = (KeywordsInfo) KeywordsFragment.this.mKeywordsList.get(position);
                if (keyword == null) {
                    setAppViewContent(viewHolder, "");
                } else if (TextUtils.isEmpty(keyword.getValue())) {
                    setAppViewContent(viewHolder, "");
                    HwLog.w(KeywordsFragment.TAG, "getView: Invalid keyword at " + position);
                } else {
                    setAppViewContent(viewHolder, keyword.getValue());
                }
            }
            return convertView;
        }

        private void setAppViewContent(ListItemViewHolder holder, String keyword) {
            holder.txKeywords.setText(keyword);
        }
    }

    private class KeywordsLoadingTask extends AsyncTask<Void, Void, List<KeywordsInfo>> {
        protected List<KeywordsInfo> doInBackground(Void... voidParams) {
            try {
                List<KeywordsInfo> keywords = DBAdapter.getKeywordsList(KeywordsFragment.this.mContext);
                Collections.sort(keywords, KeywordsInfo.KEYWORD_ALP_COMPARATOR);
                return keywords;
            } catch (Exception e) {
                HwLog.e(KeywordsFragment.TAG, "KeywordsLoadingTask-doInBackground: Exception ", e);
                return null;
            }
        }

        protected void onPostExecute(List<KeywordsInfo> keywordsList) {
            KeywordsFragment.this.mKeywordsList.clear();
            if (!Utility.isNullOrEmptyList(keywordsList)) {
                KeywordsFragment.this.mKeywordsList.addAll(keywordsList);
            }
            KeywordsFragment.this.refreshListview();
            KeywordsFragment.this.mLoadingTask = null;
        }
    }

    static class ListItemViewHolder {
        TextView txKeywords = null;

        public ListItemViewHolder(View view) {
            this.txKeywords = (TextView) view.findViewById(ViewUtil.HWID_TEXT_1);
        }
    }

    static class TextWrapper implements TextWatcher {
        private Button mOkButton = null;

        public TextWrapper(Button btnOK) {
            this.mOkButton = btnOK;
        }

        public void onTextChanged(CharSequence s, int start, int end, int count) {
            if (s.toString().trim().length() == 0) {
                this.mOkButton.setEnabled(false);
            } else {
                this.mOkButton.setEnabled(true);
            }
        }

        public void afterTextChanged(Editable arg0) {
        }

        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mContext = getActivity();
        this.mInflater = inflater;
        this.mFragmentView = this.mInflater.inflate(R.layout.interception_fragment_keywords, container, false);
        initView();
        return this.mFragmentView;
    }

    private void initView() {
        this.mKeywordsListView = (ListView) this.mFragmentView.findViewById(R.id.keywords_list_view);
        this.mKeywordsListView.setItemsCanFocus(false);
        this.mProgressBar = (ProgressBar) this.mFragmentView.findViewById(R.id.loading_progressbar);
        this.mAdapter = new KeywordsListAdapter();
        this.mKeywordsListView.setAdapter(this.mAdapter);
        this.mKeywordsListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> adapterView, View arg1, int arg2, long arg3) {
                KeywordsFragment.this.editKeywordsList(arg2);
                return true;
            }
        });
    }

    private void editKeywordsList(int position) {
        Intent intent = new Intent(this.mContext, KeywordsEditActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("id", ((KeywordsInfo) this.mKeywordsList.get(position)).getId());
        bundle.putInt(ConstValues.GET_FIRST_ITEM_POSITION, getAdjustedPosition(position));
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private int getAdjustedPosition(int position) {
        int firstPosition = this.mKeywordsListView.getFirstVisiblePosition();
        if (position > (firstPosition + this.mKeywordsListView.getLastVisiblePosition()) / 2) {
            return firstPosition + 1;
        }
        return firstPosition;
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

    private void refreshListview() {
        showLoadingProcess(Boolean.valueOf(false));
        if (this.mKeywordsList.size() > 0) {
            this.mKeywordsListView.setVisibility(0);
            updateNoDataView(8);
        } else {
            this.mKeywordsListView.setVisibility(8);
            updateNoDataView(0);
        }
        this.mAdapter.notifyDataSetChanged();
    }

    private void updateNoDataView(int visibility) {
        if (visibility == 0) {
            if (this.mNoDataLayout == null) {
                ViewStub stub = (ViewStub) this.mFragmentView.findViewById(R.id.viewstub_no_keywords);
                if (stub != null) {
                    stub.inflate();
                }
                this.mNoDataLayout = (RelativeLayout) this.mFragmentView.findViewById(R.id.no_keywords_view);
                ViewUtil.initEmptyViewMargin(GlobalContext.getContext(), this.mNoDataLayout);
            }
            this.mNoDataLayout.setVisibility(0);
        } else if (this.mNoDataLayout != null) {
            this.mNoDataLayout.setVisibility(8);
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.interception_keywordsfragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_keywords:
                confirmAddKeywords();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmAddKeywords() {
        View layout = getLayoutInflater(null).inflate(R.layout.interception_create_keywords_dialog, null);
        final EditText editTextKeywords = (EditText) layout.findViewById(R.id.edit_keywords);
        Button btnCancel = (Button) layout.findViewById(R.id.btn_cancel);
        Button btnOK = (Button) layout.findViewById(R.id.btn_ok);
        Builder altDlg = new Builder(getActivity());
        altDlg.setView(layout);
        altDlg.setTitle(getResources().getString(R.string.harassmentInterception_add_keywords));
        editTextKeywords.addTextChangedListener(new TextWrapper(btnOK));
        this.mAddKeywordsDlg = altDlg.show();
        btnCancel.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                KeywordsFragment.this.dissmissAddKeywordsDlg();
            }
        });
        btnOK.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                String keywords = editTextKeywords.getText().toString();
                if (!TextUtils.isEmpty(keywords)) {
                    if (DBAdapter.addKeywords(KeywordsFragment.this.mContext, keywords.replace(ConstValues.SEPARATOR_KEYWORDS_CN, ConstValues.SEPARATOR_KEYWORDS_EN).split(ConstValues.SEPARATOR_KEYWORDS_EN)) > 0) {
                        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, String.valueOf(DBAdapter.addKeywords(KeywordsFragment.this.mContext, keywords.replace(ConstValues.SEPARATOR_KEYWORDS_CN, ConstValues.SEPARATOR_KEYWORDS_EN).split(ConstValues.SEPARATOR_KEYWORDS_EN))));
                        HsmStat.statE((int) Events.E_HARASSMENT_BLOCK_ADD_KEYWORDS, statParam);
                    }
                    KeywordsFragment.this.refurbishKeywordsList();
                    KeywordsFragment.this.dissmissAddKeywordsDlg();
                }
            }
        });
    }

    private void dissmissAddKeywordsDlg() {
        if (this.mAddKeywordsDlg != null) {
            this.mAddKeywordsDlg.dismiss();
            this.mAddKeywordsDlg = null;
        }
    }

    public void onDestroy() {
        dissmissAddKeywordsDlg();
        super.onDestroy();
    }
}
