package com.android.settings.dashboard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.appcompat.R$id;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.HwCustSplitUtils;
import com.android.settings.InstrumentedFragment;
import com.android.settings.ItemUseStat;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsExtUtils;
import com.android.settings.Utils;
import com.android.settings.search.Index;
import com.android.settingslib.drawer.SplitUtils;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.HashMap;

public class SearchResultsSummary extends InstrumentedFragment {
    private static char ELLIPSIS = '…';
    private LinearLayout mEmptyView;
    private HwCustSplitUtils mHwCustSplitUtils;
    private ViewGroup mLayoutResults;
    private ViewGroup mLayoutSuggestions;
    private String mQuery;
    private SearchResultsAdapter mResultsAdapter;
    private ListView mResultsListView;
    private SearchView mSearchView;
    private boolean mShowResults;
    private SuggestionsAdapter mSuggestionsAdapter;
    private ListView mSuggestionsListView;
    private UpdateSearchResultsTask mUpdateSearchResultsTask;
    private UpdateSuggestionsTask mUpdateSuggestionsTask;

    private static class SearchResult {
        public Context context;
        public String entries;
        public int iconResId;
        public boolean isLastItem = false;
        public boolean isSectionHeader = false;
        public String key;
        public String summaryOff;
        public String summaryOn;
        public String title;

        public SearchResult(String title) {
            this.title = title;
            this.isSectionHeader = true;
        }

        public SearchResult(Context context, String title, String summaryOn, String summaryOff, String entries, int iconResId, String key) {
            this.context = context;
            this.title = title;
            this.summaryOn = summaryOn;
            this.summaryOff = summaryOff;
            this.entries = entries;
            this.iconResId = iconResId;
            this.key = key;
        }
    }

    private class SearchResultsAdapter extends BaseAdapter {
        private int mColor;
        private Context mContext;
        private HashMap<String, Context> mContextMap = new HashMap();
        private Cursor mCursor;
        private ArrayList<SearchResult> mData = new ArrayList();
        private boolean mDataValid;
        private LayoutInflater mInflater;
        private SparseIntArray mPositionMap = new SparseIntArray();

        public SearchResultsAdapter(Context context) {
            this.mContext = context;
            this.mInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
            this.mDataValid = false;
            this.mColor = Utils.getControlColor(SearchResultsSummary.this.getActivity().getApplicationContext(), SearchResultsSummary.this.getActivity().getApplicationContext().getResources().getColor(2131427515));
        }

        public Cursor swapCursor(Cursor newCursor) {
            if (newCursor == this.mCursor) {
                return null;
            }
            Cursor oldCursor = this.mCursor;
            this.mCursor = newCursor;
            if (newCursor != null) {
                this.mDataValid = true;
                assembleData(newCursor);
                notifyDataSetChanged();
            } else {
                this.mDataValid = false;
                notifyDataSetInvalidated();
            }
            return oldCursor;
        }

        public int getItemViewType(int position) {
            SearchResult item = (SearchResult) getItem(position);
            if (item.isSectionHeader) {
                return 1;
            }
            if (item.isLastItem) {
                return 2;
            }
            return 0;
        }

        public int getViewTypeCount() {
            return 3;
        }

        public int getCursorPosition(int position) {
            return this.mPositionMap.get(position, -1);
        }

        private void assembleData(Cursor cursor) {
            String oldRank = "";
            int indexOfCursor = 0;
            int indexOfListView = 0;
            this.mData.clear();
            this.mPositionMap.clear();
            while (this.mDataValid && cursor.moveToNext()) {
                String rank = this.mCursor.getString(0);
                if (!oldRank.equals(rank)) {
                    oldRank = rank;
                    int resId = RankingHwBase.getTitleResId(Integer.parseInt(rank));
                    SearchResult sectionHeader = new SearchResult(resId != -1 ? this.mContext.getResources().getString(resId) : "");
                    if (this.mData.size() > 0) {
                        ((SearchResult) this.mData.get(this.mData.size() - 1)).isLastItem = true;
                    }
                    this.mData.add(sectionHeader);
                    indexOfListView++;
                }
                this.mPositionMap.put(indexOfListView, indexOfCursor);
                SearchResult item = makeSearchResult(cursor);
                if (item != null) {
                    this.mData.add(item);
                    indexOfListView++;
                }
                indexOfCursor++;
            }
        }

        private SearchResult makeSearchResult(Cursor cursor) {
            Context packageContext;
            String title = cursor.getString(1);
            String summaryOn = cursor.getString(2);
            String summaryOff = cursor.getString(3);
            String entries = cursor.getString(4);
            String iconResStr = cursor.getString(8);
            String className = cursor.getString(6);
            String packageName = cursor.getString(10);
            String key = cursor.getString(13);
            if (!TextUtils.isEmpty(className) || TextUtils.isEmpty(packageName)) {
                packageContext = this.mContext;
            } else {
                packageContext = (Context) this.mContextMap.get(packageName);
                if (packageContext == null) {
                    try {
                        packageContext = this.mContext.createPackageContext(packageName, 0);
                        this.mContextMap.put(packageName, packageContext);
                    } catch (NameNotFoundException e) {
                        Log.e("SearchResultsSummary", "Cannot create Context for package: " + packageName);
                        return null;
                    }
                }
            }
            return new SearchResult(packageContext, title, summaryOn, summaryOff, entries, TextUtils.isEmpty(iconResStr) ? 2130837691 : Integer.parseInt(iconResStr), key);
        }

        public int getCount() {
            if (!this.mDataValid || this.mCursor == null || this.mCursor.isClosed()) {
                return 0;
            }
            return this.mData.size();
        }

        public Object getItem(int position) {
            if (this.mDataValid) {
                return this.mData.get(position);
            }
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (this.mDataValid || convertView != null) {
                int cursorPosition = getCursorPosition(position);
                if (cursorPosition == -1 || this.mCursor.moveToPosition(cursorPosition)) {
                    View view;
                    int rowType = getItemViewType(position);
                    if (convertView != null) {
                        view = convertView;
                    } else if (rowType == 1) {
                        view = this.mInflater.inflate(2130969089, parent, false);
                    } else if (rowType == 2) {
                        view = this.mInflater.inflate(2130969088, parent, false);
                    } else {
                        view = this.mInflater.inflate(2130969087, parent, false);
                    }
                    TextView textTitle = (TextView) view.findViewById(R$id.title);
                    SearchResult result = (SearchResult) getItem(position);
                    if (result == null) {
                        return view;
                    }
                    textTitle.setText(result.title);
                    if (rowType == 1) {
                        return view;
                    }
                    ImageView imageView = (ImageView) view.findViewById(2131886147);
                    setSpanedTextView(textTitle);
                    updateArrowStatus(view);
                    TextView summary = (TextView) view.findViewById(2131886387);
                    summary.setVisibility(0);
                    if (!TextUtils.isEmpty(result.summaryOn)) {
                        summary.setText(result.summaryOn);
                    } else if (!TextUtils.isEmpty(result.summaryOff)) {
                        summary.setText(result.summaryOff);
                    } else if (TextUtils.isEmpty(result.entries)) {
                        summary.setText("");
                        summary.setVisibility(8);
                    } else {
                        summary.setText(result.entries);
                    }
                    setSpanedTextView(summary);
                    if (result.iconResId != 2130837691) {
                        try {
                            imageView.setImageDrawable(result.context.getDrawable(result.iconResId));
                        } catch (NotFoundException e) {
                            Log.e("SearchResultsSummary", "Cannot load Drawable for " + result.title);
                        }
                    } else {
                        imageView.setImageDrawable(null);
                        imageView.setBackgroundResource(2130837691);
                    }
                    return view;
                }
                throw new IllegalStateException("couldn't move cursor to position " + position);
            }
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }

        private void updateArrowStatus(View view) {
            View arrowSection = view.findViewById(2131887139);
            if (arrowSection == null) {
                return;
            }
            if (SplitUtils.reachSplitSize(SearchResultsSummary.this.getActivity())) {
                arrowSection.setVisibility(8);
            } else {
                arrowSection.setVisibility(0);
            }
        }

        private void setSpanedTextView(TextView textView) {
            if (textView != null && SearchResultsSummary.this.mQuery != null) {
                Spannable spannable = (Spannable) textView.getText();
                ForegroundColorSpan[] removeTextSpan = (ForegroundColorSpan[]) spannable.getSpans(0, spannable.length(), ForegroundColorSpan.class);
                for (Object removeSpan : removeTextSpan) {
                    spannable.removeSpan(removeSpan);
                }
                String value = textView.getText().toString().toLowerCase();
                String query = SearchResultsSummary.this.mQuery.toLowerCase();
                if (TextUtils.isEmpty(query)) {
                    Log.e("SearchResultsSummary", "setSpanedTextView return as query is empty:" + query);
                    return;
                }
                int end = 0;
                int idx = 0;
                while (true) {
                    int start = value.indexOf(query, end);
                    if (start != -1) {
                        end = start + query.length();
                        int idx2 = idx + 1;
                        spannable.setSpan(SpanFactory.getColorSpan(idx, this.mColor), start, end, 18);
                        idx = idx2;
                    } else {
                        return;
                    }
                }
            }
        }
    }

    private static class SpanFactory {
        private static final SparseArray<ForegroundColorSpan> mSpans = new SparseArray();

        private SpanFactory() {
        }

        public static ForegroundColorSpan getColorSpan(int index, int color) {
            ForegroundColorSpan foregroundColorSpan = (ForegroundColorSpan) mSpans.get(index);
            foregroundColorSpan = new ForegroundColorSpan(color);
            mSpans.put(index, foregroundColorSpan);
            return foregroundColorSpan;
        }
    }

    private static class SuggestionItem {
        public String query;

        public SuggestionItem(String query) {
            this.query = query;
        }
    }

    private static class SuggestionsAdapter extends BaseAdapter {
        private Context mContext;
        private Cursor mCursor;
        private boolean mDataValid = false;
        private LayoutInflater mInflater;

        public SuggestionsAdapter(Context context) {
            this.mContext = context;
            this.mInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
            this.mDataValid = false;
        }

        public Cursor swapCursor(Cursor newCursor) {
            if (newCursor == this.mCursor) {
                return null;
            }
            Cursor oldCursor = this.mCursor;
            this.mCursor = newCursor;
            if (newCursor != null) {
                this.mDataValid = true;
                notifyDataSetChanged();
            } else {
                this.mDataValid = false;
                notifyDataSetInvalidated();
            }
            return oldCursor;
        }

        public int getCount() {
            if (!this.mDataValid || this.mCursor == null || this.mCursor.isClosed()) {
                return 0;
            }
            return this.mCursor.getCount();
        }

        public Object getItem(int position) {
            if (this.mDataValid && this.mCursor.moveToPosition(position)) {
                return new SuggestionItem(this.mCursor.getString(0));
            }
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (!this.mDataValid && convertView == null) {
                throw new IllegalStateException("this should only be called when the cursor is valid");
            } else if (this.mCursor.moveToPosition(position)) {
                View view;
                if (convertView == null) {
                    view = this.mInflater.inflate(2130969090, parent, false);
                } else {
                    view = convertView;
                }
                TextView query = (TextView) view.findViewById(R$id.title);
                SuggestionItem item = (SuggestionItem) getItem(position);
                if (item != null) {
                    query.setText(item.query);
                }
                return view;
            } else {
                throw new IllegalStateException("couldn't move cursor to position " + position);
            }
        }
    }

    private class UpdateSearchResultsTask extends AsyncTask<String, Void, Cursor> {
        private UpdateSearchResultsTask() {
        }

        protected Cursor doInBackground(String... params) {
            return Index.getInstance(SearchResultsSummary.this.getActivity()).search(params[0]);
        }

        protected void onPostExecute(Cursor cursor) {
            boolean z = true;
            if (!isCancelled()) {
                boolean z2;
                MetricsLogger.action(SearchResultsSummary.this.getContext(), 226, cursor.getCount());
                SearchResultsSummary.this.setResultsCursor(cursor);
                SearchResultsSummary searchResultsSummary = SearchResultsSummary.this;
                if (cursor.getCount() > 0) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                searchResultsSummary.setResultsVisibility(z2);
                SearchResultsSummary searchResultsSummary2 = SearchResultsSummary.this;
                if (cursor.getCount() != 0) {
                    z = false;
                }
                searchResultsSummary2.setEmptyViewVisible(z);
            } else if (cursor != null) {
                cursor.close();
            }
        }
    }

    private class UpdateSuggestionsTask extends AsyncTask<String, Void, Cursor> {
        private UpdateSuggestionsTask() {
        }

        protected Cursor doInBackground(String... params) {
            return Index.getInstance(SearchResultsSummary.this.getActivity()).getSuggestions(params[0]);
        }

        protected void onPostExecute(Cursor cursor) {
            boolean z = false;
            if (!isCancelled()) {
                SearchResultsSummary.this.setSuggestionsCursor(cursor);
                SearchResultsSummary searchResultsSummary = SearchResultsSummary.this;
                if (cursor.getCount() > 0) {
                    z = true;
                }
                searchResultsSummary.setSuggestionsVisibility(z);
            } else if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void setEmptyViewVisible(boolean visible) {
        if (this.mEmptyView != null) {
            this.mEmptyView.setVisibility(visible ? 0 : 8);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mResultsAdapter = new SearchResultsAdapter(getActivity());
        this.mSuggestionsAdapter = new SuggestionsAdapter(getActivity());
        if (savedInstanceState != null) {
            this.mShowResults = savedInstanceState.getBoolean(":settings:show_results");
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(":settings:show_results", this.mShowResults);
    }

    public void onStop() {
        super.onStop();
        if (!this.mHwCustSplitUtils.reachSplitSize()) {
            clearSuggestions();
            clearResults();
        }
    }

    public void onDestroy() {
        this.mResultsListView = null;
        this.mResultsAdapter = null;
        this.mUpdateSearchResultsTask = null;
        this.mSuggestionsListView = null;
        this.mSuggestionsAdapter = null;
        this.mUpdateSuggestionsTask = null;
        if (this.mSearchView != null) {
            this.mSearchView.setQuery("", false);
        }
        this.mSearchView = null;
        super.onDestroy();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mHwCustSplitUtils = (HwCustSplitUtils) HwCustUtils.createObj(HwCustSplitUtils.class, new Object[]{getActivity()});
        View view = inflater.inflate(2130969084, container, false);
        this.mLayoutSuggestions = (ViewGroup) view.findViewById(2131887134);
        this.mLayoutResults = (ViewGroup) view.findViewById(2131887137);
        this.mResultsListView = (ListView) view.findViewById(2131887138);
        this.mResultsListView.setDivider(null);
        this.mResultsListView.setDividerHeight(0);
        this.mResultsListView.setAdapter(this.mResultsAdapter);
        this.mResultsListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (SearchResultsSummary.this.mResultsAdapter.getItemViewType(position) != 1) {
                    position = SearchResultsSummary.this.mResultsAdapter.getCursorPosition(position);
                    Cursor cursor = SearchResultsSummary.this.mResultsAdapter.mCursor;
                    cursor.moveToPosition(position);
                    String className = cursor.getString(6);
                    String screenTitle = cursor.getString(7);
                    String action = cursor.getString(9);
                    String key = cursor.getString(13);
                    Context sa = (SettingsActivity) SearchResultsSummary.this.getActivity();
                    sa.needToClearFocusOfSearchView();
                    if (TextUtils.isEmpty(action)) {
                        Bundle args = new Bundle();
                        args.putString(":settings:fragment_args_key", key);
                        args.putBoolean("isMarkViewEx", false);
                        Utils.startWithFragment(sa, className, args, null, 0, -1, screenTitle);
                        ItemUseStat.getInstance().handleClick(sa, 2, "search_result_clicked", className);
                    } else {
                        Intent intent;
                        if ("com.android.settings.action.unknown".equals(action)) {
                            intent = new Intent();
                        } else {
                            intent = new Intent(action);
                            if ("com.huawei.hwid.ACTION_START_FOR_GOTO_ACCOUNTCENTER".equals(action)) {
                                intent.putExtra("START_FOR_GOTO_ACCOUNTCENTER", true);
                            }
                        }
                        intent.putExtra("extra_setting_key", key);
                        String targetPackage = cursor.getString(10);
                        String targetClass = cursor.getString(11);
                        if (!(TextUtils.isEmpty(targetPackage) || TextUtils.isEmpty(targetClass))) {
                            intent.setComponent(new ComponentName(targetPackage, targetClass));
                        }
                        if (Utils.hasIntentActivity(sa.getPackageManager(), intent)) {
                            intent.putExtra(":settings:fragment_args_key", key);
                            intent.putExtra("intent_from_settings", true);
                            sa.startActivity(intent);
                            SettingsExtUtils.setAnimationReflection(sa);
                        }
                        ItemUseStat.getInstance().handleClick(sa, 2, "search_result_clicked", targetClass);
                    }
                    SearchResultsSummary.this.saveQueryToDatabase();
                }
            }
        });
        this.mSuggestionsListView = (ListView) view.findViewById(2131887135);
        this.mSuggestionsListView.setAdapter(this.mSuggestionsAdapter);
        this.mSuggestionsListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                position--;
                if (position >= 0) {
                    Cursor cursor = SearchResultsSummary.this.mSuggestionsAdapter.mCursor;
                    cursor.moveToPosition(position);
                    SearchResultsSummary.this.mShowResults = true;
                    SearchResultsSummary.this.mQuery = cursor.getString(0);
                    SearchResultsSummary.this.mSearchView.setQuery(SearchResultsSummary.this.mQuery, false);
                    ItemUseStat.getInstance().handleClick(SearchResultsSummary.this.getActivity(), 2, "search_suggestion_clicked");
                }
            }
        });
        this.mSuggestionsListView.addHeaderView(LayoutInflater.from(getActivity()).inflate(2130969086, this.mSuggestionsListView, false), null, false);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateEmptyView();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateEmptyView();
    }

    private void updateEmptyView() {
        this.mEmptyView = (LinearLayout) getView().findViewById(2131886564);
        ImageView emptyIcon = (ImageView) getView().findViewById(2131886560);
        ((TextView) getView().findViewById(2131886561)).setText(2131627798);
        emptyIcon.setBackgroundResource(2130838636);
        Configuration configuration = getResources().getConfiguration();
        LinearLayout parent = (LinearLayout) emptyIcon.getParent();
        float density = getResources().getDisplayMetrics().density;
        if (2 == configuration.orientation) {
            parent.setPaddingRelative(parent.getPaddingStart(), (int) (68.0f * density), parent.getPaddingEnd(), 0);
        } else {
            parent.setPaddingRelative(parent.getPaddingStart(), (int) (120.0f * density), parent.getPaddingEnd(), 0);
        }
    }

    protected int getMetricsCategory() {
        return 34;
    }

    public void onResume() {
        super.onResume();
        if (!this.mShowResults) {
            showSomeSuggestions();
        }
    }

    public void setSearchView(SearchView searchView) {
        this.mSearchView = searchView;
        if (this.mSearchView != null) {
            EditText editText = (EditText) this.mSearchView.findViewById(16909303);
            if (Utils.isTablet()) {
                editText.setImeOptions(33554432);
            }
            editText.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (SearchResultsSummary.this.mHwCustSplitUtils.reachSplitSize()) {
                        SearchResultsSummary.this.mHwCustSplitUtils.finishAllSubActivities();
                    }
                }
            });
        }
    }

    private void setSuggestionsVisibility(boolean visible) {
        if (this.mLayoutSuggestions != null) {
            this.mLayoutSuggestions.setVisibility(visible ? 0 : 8);
        }
    }

    private void setResultsVisibility(boolean visible) {
        if (this.mLayoutResults != null) {
            this.mLayoutResults.setVisibility(visible ? 0 : 8);
        }
    }

    private void saveQueryToDatabase() {
        Index.getInstance(getActivity()).addSavedQuery(this.mQuery);
    }

    public boolean onQueryTextSubmit(String query) {
        this.mQuery = getFilteredQueryString(query);
        if (TextUtils.isEmpty(this.mQuery)) {
            return true;
        }
        this.mShowResults = true;
        setSuggestionsVisibility(false);
        updateSearchResults();
        saveQueryToDatabase();
        return false;
    }

    public boolean onQueryTextChange(String query) {
        this.mQuery = getFilteredQueryString(query);
        if (TextUtils.isEmpty(this.mQuery)) {
            this.mShowResults = false;
            setResultsVisibility(false);
            setEmptyViewVisible(false);
            updateSuggestions();
        } else {
            this.mShowResults = true;
            setSuggestionsVisibility(false);
            updateSearchResults();
        }
        if (this.mHwCustSplitUtils.reachSplitSize()) {
            this.mHwCustSplitUtils.finishAllSubActivities();
            if (this.mSearchView != null) {
                this.mSearchView.requestFocus();
            }
        }
        return true;
    }

    public void showSomeSuggestions() {
        setResultsVisibility(false);
        setEmptyViewVisible(false);
        this.mQuery = "";
        updateSuggestions();
    }

    private void clearSuggestions() {
        if (this.mUpdateSuggestionsTask != null) {
            this.mUpdateSuggestionsTask.cancel(false);
            this.mUpdateSuggestionsTask = null;
        }
        setSuggestionsCursor(null);
    }

    private void setSuggestionsCursor(Cursor cursor) {
        if (this.mSuggestionsAdapter != null) {
            Cursor oldCursor = this.mSuggestionsAdapter.swapCursor(cursor);
            if (oldCursor != null) {
                oldCursor.close();
            }
        }
    }

    private void clearResults() {
        if (this.mUpdateSearchResultsTask != null) {
            this.mUpdateSearchResultsTask.cancel(false);
            this.mUpdateSearchResultsTask = null;
        }
        setResultsCursor(null);
    }

    private void setResultsCursor(Cursor cursor) {
        if (this.mResultsAdapter != null) {
            Cursor oldCursor = this.mResultsAdapter.swapCursor(cursor);
            if (oldCursor != null) {
                oldCursor.close();
            }
        }
    }

    private String getFilteredQueryString(CharSequence query) {
        if (query == null) {
            return null;
        }
        StringBuilder filtered = new StringBuilder();
        for (int n = 0; n < query.length(); n++) {
            char c = query.charAt(n);
            if (Character.isLetterOrDigit(c) || Character.isSpaceChar(c)) {
                filtered.append(c);
            }
        }
        return filtered.toString();
    }

    private void clearAllTasks() {
        if (this.mUpdateSearchResultsTask != null) {
            this.mUpdateSearchResultsTask.cancel(false);
            this.mUpdateSearchResultsTask = null;
        }
        if (this.mUpdateSuggestionsTask != null) {
            this.mUpdateSuggestionsTask.cancel(false);
            this.mUpdateSuggestionsTask = null;
        }
    }

    private void updateSuggestions() {
        clearAllTasks();
        if (this.mQuery == null) {
            setSuggestionsCursor(null);
            return;
        }
        this.mUpdateSuggestionsTask = new UpdateSuggestionsTask();
        this.mUpdateSuggestionsTask.execute(new String[]{this.mQuery});
    }

    private void updateSearchResults() {
        clearAllTasks();
        if (TextUtils.isEmpty(this.mQuery)) {
            setResultsVisibility(false);
            setEmptyViewVisible(false);
            setResultsCursor(null);
            return;
        }
        this.mUpdateSearchResultsTask = new UpdateSearchResultsTask();
        this.mUpdateSearchResultsTask.execute(new String[]{this.mQuery});
    }
}
