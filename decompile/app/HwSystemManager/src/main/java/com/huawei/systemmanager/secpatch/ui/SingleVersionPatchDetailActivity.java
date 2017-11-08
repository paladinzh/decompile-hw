package com.huawei.systemmanager.secpatch.ui;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.secpatch.adapter.SingleVersionDetailListAdapter;
import com.huawei.systemmanager.secpatch.common.ConstValues;
import com.huawei.systemmanager.secpatch.common.SecPatchItem;
import com.huawei.systemmanager.secpatch.db.DBAdapter;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SingleVersionPatchDetailActivity extends HsmActivity {
    private static final String TAG = "SingleVersionPatchDetailActivity";
    private ActionBar mActionBar;
    private Context mContext;
    private ListView mListView;
    private List<SecPatchItem> mSecPatchItemList = new ArrayList();
    private SingleVersionDetailListAdapter mSinVersionDetailListAdapter;
    private TextView mTitleDescription;
    private String mVersionName = null;

    private class AsynctaskGetDBPatch extends AsyncTask<Void, Void, List<SecPatchItem>> {
        private AsynctaskGetDBPatch() {
        }

        protected List<SecPatchItem> doInBackground(Void... params) {
            return DBAdapter.getSecPatchList(SingleVersionPatchDetailActivity.this.mContext);
        }

        protected void onPostExecute(List<SecPatchItem> secpatchlist) {
            super.onPostExecute(secpatchlist);
            SingleVersionPatchDetailActivity.this.initUIData(secpatchlist);
        }
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.singleversion_patchdetail_activity);
        getIntentValues();
        if (TextUtils.isEmpty(this.mVersionName)) {
            finish();
        }
        this.mContext = getApplicationContext();
        initActionBar();
        initView();
        new AsynctaskGetDBPatch().execute(new Void[0]);
        setListener();
    }

    private void initActionBar() {
        this.mActionBar = getActionBar();
        if (this.mActionBar != null) {
            this.mActionBar.setTitle(this.mVersionName);
        }
    }

    private void initView() {
        this.mListView = (ListView) findViewById(R.id.single_version_detail);
        this.mTitleDescription = (TextView) findViewById(R.id.single_version_title);
    }

    private void initUIData(List<SecPatchItem> secpatchlist) {
        if (!Utility.isNullOrEmptyList(secpatchlist)) {
            int size = secpatchlist.size();
            for (int i = 0; i < size; i++) {
                if (((SecPatchItem) secpatchlist.get(i)).isSameVersion(this.mVersionName)) {
                    this.mSecPatchItemList.add((SecPatchItem) secpatchlist.get(i));
                }
            }
            Collections.sort(this.mSecPatchItemList, new SecPatchItem());
            if (!Utility.isNullOrEmptyList(this.mSecPatchItemList)) {
                this.mTitleDescription.setText(getVersionTitle(this.mSecPatchItemList.size()));
            }
        }
        initListAdapter();
    }

    private void initListAdapter() {
        if (this.mSinVersionDetailListAdapter == null) {
            this.mSinVersionDetailListAdapter = new SingleVersionDetailListAdapter(this, this.mSecPatchItemList);
        } else {
            this.mSinVersionDetailListAdapter.setSingleVersionDetailInfo(this.mSecPatchItemList);
        }
        this.mListView.setAdapter(this.mSinVersionDetailListAdapter);
    }

    private void setListener() {
        this.mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View arg1, int position, long arg3) {
                SecPatchItem detailItem = (SecPatchItem) SingleVersionPatchDetailActivity.this.mSecPatchItemList.get(position);
                Intent intent = new Intent(SingleVersionPatchDetailActivity.this, SecurityPatchDetailActivity.class);
                intent.putExtra(ConstValues.INTENT_DETAIL_SID, detailItem.mSid);
                intent.putExtra(ConstValues.INTENT_DETAIL_OCID, detailItem.mOcid);
                intent.putExtra(ConstValues.INTENT_DETAIL_SRC, detailItem.mSrc);
                intent.putExtra(ConstValues.INTENT_DETAIL_CHN, detailItem.mDigest);
                intent.putExtra(ConstValues.INTENT_DETAIL_ENG, detailItem.mDigest_en);
                SingleVersionPatchDetailActivity.this.startActivity(intent);
            }
        });
    }

    private void getIntentValues() {
        Intent intent = getIntent();
        if (intent == null) {
            HwLog.e(TAG, "The intent is null");
        } else {
            this.mVersionName = intent.getStringExtra(ConstValues.INTENT_SINGLE_VERSION);
        }
    }

    private String getVersionTitle(int patchCount) {
        return this.mContext.getResources().getString(R.string.Security_Patch_Fixed_Description, new Object[]{this.mVersionName, Integer.valueOf(patchCount)});
    }
}
