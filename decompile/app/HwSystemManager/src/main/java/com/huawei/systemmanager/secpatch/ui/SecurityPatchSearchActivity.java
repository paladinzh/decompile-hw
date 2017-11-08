package com.huawei.systemmanager.secpatch.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.CursorHelper;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.secpatch.common.ConstValues;
import com.huawei.systemmanager.secpatch.common.SecDetailItem;
import com.huawei.systemmanager.secpatch.common.SecPatchSearchItem;
import com.huawei.systemmanager.secpatch.db.DBAdapter;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SecurityPatchSearchActivity extends HsmActivity {
    private static final String TAG = "SecurityPatchSearchActivity";
    private Context mContext;
    private Map<String, SecDetailItem> mDetailItemMap = new HashMap();
    private LinearLayout mEmptyLayout;
    private OnItemClickListener mListItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            TextView tv = (TextView) view.findViewById(R.id.child_version_num);
            HwLog.e(SecurityPatchSearchActivity.TAG, "The onItemClick text view is: " + tv.getText());
            Intent intent = new Intent(SecurityPatchSearchActivity.this, SecurityPatchDetailActivity.class);
            String ocid = (String) tv.getText();
            synchronized (SecurityPatchSearchActivity.this.mDetailItemMap) {
                SecDetailItem detailItem = (SecDetailItem) SecurityPatchSearchActivity.this.mDetailItemMap.get(ocid);
            }
            if (detailItem != null) {
                detailItem.fillIntentForDetailActivty(intent);
                SecurityPatchSearchActivity.this.startActivity(intent);
            }
        }
    };
    private ListView mListView;
    private Pattern mOcidFinalPartternAndroid = Pattern.compile("ANDROID-[0-9]{1,20}");
    private Pattern mOcidFinalPartternCVE = Pattern.compile("CVE-[0-9]{4}-[0-9]{4}");
    private Pattern mOcidFinalPartternHW = Pattern.compile("HWPSIRT-[0-9]{4}-[0-9]{4}");
    private List<String> mOcidList = new ArrayList();
    private Pattern mOcidParttern = Pattern.compile("[a-zA-Z]{0,7}[-]{0,1}[0-9]{0,20}");
    private Pattern mOcidPartternMore = Pattern.compile("[a-zA-Z]{0,3}[-]{0,1}[0-9]{0,4}[-]{0,1}[0-9]{0,4}");
    private OnQueryTextListener mQueryTextListener = new OnQueryTextListener() {
        public boolean onQueryTextSubmit(String queryText) {
            if (SecurityPatchSearchActivity.this.mSearchView != null) {
                InputMethodManager imm = (InputMethodManager) SecurityPatchSearchActivity.this.getSystemService("input_method");
                if (imm != null) {
                    imm.hideSoftInputFromWindow(SecurityPatchSearchActivity.this.mSearchView.getWindowToken(), 0);
                }
                SecurityPatchSearchActivity.this.mSearchView.clearFocus();
                if (SecurityPatchSearchActivity.this.isValidFinalInput(queryText) && SecurityPatchSearchActivity.this.isValidButNotFound(queryText)) {
                    SecurityPatchSearchActivity.this.mListView.setVisibility(8);
                    SecurityPatchSearchActivity.this.mSearchResult.setVisibility(8);
                    SecurityPatchSearchActivity.this.mEmptyLayout.setVisibility(0);
                }
            }
            return true;
        }

        public boolean onQueryTextChange(String newText) {
            SecurityPatchSearchActivity.this.swapAdapterData(newText);
            return true;
        }
    };
    private int mSearchCount = 0;
    private TextView mSearchResult;
    private SearchView mSearchView;

    private class AsynctaskGetDBPatch extends AsyncTask<Void, Void, Void> {
        private AsynctaskGetDBPatch() {
        }

        protected Void doInBackground(Void... params) {
            SecurityPatchSearchActivity.this.getListItems();
            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            SecurityPatchSearchActivity.this.swapAdapterData("");
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.security_search_main);
        this.mContext = getApplication();
        this.mListView = (ListView) findViewById(R.id.list_search);
        this.mListView.setOnItemClickListener(this.mListItemClickListener);
        this.mSearchResult = (TextView) findViewById(R.id.search_result_textview);
        this.mEmptyLayout = (LinearLayout) findViewById(R.id.empty_search);
        this.mEmptyLayout.setVisibility(8);
        new AsynctaskGetDBPatch().execute(new Void[0]);
        this.mSearchView = (SearchView) findViewById(R.id.search);
        this.mSearchView.setIconifiedByDefault(true);
        this.mSearchView.onActionViewExpanded();
        this.mSearchView.requestFocus();
        this.mSearchView.setQueryHint(getText(R.string.Security_Patch_Enter_Tip));
        this.mSearchView.setOnQueryTextListener(this.mQueryTextListener);
    }

    protected void onResume() {
        super.onResume();
    }

    private void getListItems() {
        Cursor cursor = DBAdapter.queryDBbyGivenCondition(this.mContext, null, null);
        if (CursorHelper.checkCursorValid(cursor)) {
            int sidIndex = cursor.getColumnIndex("sid");
            int ocidIndex = cursor.getColumnIndex("ocid");
            int fixVersionIndex = cursor.getColumnIndex(ConstValues.COL_FIXED_VERSION);
            int srcIndex = cursor.getColumnIndex("src");
            int desChnIndex = cursor.getColumnIndex("digest");
            int desEngIndex = cursor.getColumnIndex("digest_en");
            while (cursor.moveToNext()) {
                String sid = cursor.getString(sidIndex);
                String ocid = cursor.getString(ocidIndex);
                String fixVersion = cursor.getString(fixVersionIndex);
                String src = cursor.getString(srcIndex);
                String desChn = cursor.getString(desChnIndex);
                String desEng = cursor.getString(desEngIndex);
                synchronized (this.mOcidList) {
                    if (this.mOcidList.contains(ocid)) {
                    } else {
                        this.mOcidList.add(ocid);
                        SecDetailItem detailItem = new SecDetailItem(sid, ocid, src, desChn, desEng, fixVersion);
                        synchronized (this.mDetailItemMap) {
                            this.mDetailItemMap.put(ocid, detailItem);
                        }
                    }
                }
            }
            CursorHelper.closeCursor(cursor);
        }
    }

    private List<SecPatchSearchItem> getMatchedSidList(String queryText) {
        List<SecPatchSearchItem> matchedSidList = new ArrayList();
        List list;
        String desString;
        if (TextUtils.isEmpty(queryText)) {
            list = this.mOcidList;
            synchronized (list) {
                for (String currentOcid : this.mOcidList) {
                    desString = getCurrentDescription(currentOcid);
                    if (desString != null) {
                        matchedSidList.add(new SecPatchSearchItem(currentOcid, desString));
                    }
                }
            }
        } else {
            Matcher matcherPattern = this.mOcidParttern.matcher(queryText);
            Matcher matcherPatternMore = this.mOcidPartternMore.matcher(queryText);
            if (!matcherPattern.matches() && !matcherPatternMore.matches()) {
                return matchedSidList;
            }
            list = this.mOcidList;
            synchronized (list) {
                for (String currentOcid2 : this.mOcidList) {
                    if (currentOcid2.toUpperCase(Locale.US).contains(queryText.toUpperCase(Locale.US))) {
                        desString = getCurrentDescription(currentOcid2);
                        if (desString != null) {
                            matchedSidList.add(new SecPatchSearchItem(currentOcid2, desString));
                        }
                    }
                }
            }
        }
        return matchedSidList;
    }

    private String getCurrentDescription(String sid) {
        String result = null;
        synchronized (this.mDetailItemMap) {
            SecDetailItem detailItem = (SecDetailItem) this.mDetailItemMap.get(sid);
        }
        try {
            if (ConstValues.CHINA_COUNTRY_CODE.equals(Locale.getDefault().getLanguage())) {
                return detailItem.mDesc;
            }
            return detailItem.mDesc_en;
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
    }

    private void swapAdapterData(String textValue) {
        List<HashMap<String, String>> data = new ArrayList();
        List<SecPatchSearchItem> matchedSidList = getMatchedSidList(textValue);
        if (Utility.isNullOrEmptyList(matchedSidList)) {
            this.mListView.setVisibility(8);
            this.mSearchResult.setVisibility(8);
            this.mEmptyLayout.setVisibility(0);
            this.mSearchCount = 0;
            return;
        }
        this.mSearchCount = matchedSidList.size();
        for (SecPatchSearchItem item : matchedSidList) {
            HashMap<String, String> sidMap = new HashMap();
            sidMap.put("title", item.mSid);
            sidMap.put("desc", item.mDesc);
            data.add(sidMap);
        }
        this.mListView.setAdapter(new SimpleAdapter(this, data, R.layout.child_list, new String[]{"title", "desc"}, new int[]{R.id.child_version_num, R.id.child_patch_num}));
        this.mListView.setVisibility(0);
        this.mEmptyLayout.setVisibility(8);
        if (TextUtils.isEmpty(textValue)) {
            this.mSearchResult.setVisibility(8);
        } else {
            this.mSearchResult.setText(this.mContext.getResources().getString(R.string.Security_Patch_Exist_Match, new Object[]{Integer.valueOf(data.size())}));
            this.mSearchResult.setVisibility(0);
        }
    }

    private boolean isValidFinalInput(String patchOcid) {
        HwLog.d(TAG, "The queryText is: " + patchOcid);
        if (TextUtils.isEmpty(patchOcid)) {
            HwLog.e(TAG, "The queryText is empty!");
            return false;
        }
        String patchOcididUpper = patchOcid.toUpperCase(Locale.US);
        boolean matches = (this.mOcidFinalPartternAndroid.matcher(patchOcididUpper).matches() || this.mOcidFinalPartternCVE.matcher(patchOcididUpper).matches()) ? true : this.mOcidFinalPartternHW.matcher(patchOcididUpper).matches();
        return matches;
    }

    private boolean isValidButNotFound(String patchOcid) {
        boolean z;
        String patchOcididUpper = patchOcid.toUpperCase(Locale.US);
        synchronized (this.mOcidList) {
            z = !this.mOcidList.contains(patchOcididUpper);
        }
        return z;
    }
}
