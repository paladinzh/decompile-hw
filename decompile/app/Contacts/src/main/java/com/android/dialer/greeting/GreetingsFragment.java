package com.android.dialer.greeting;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.android.contacts.activities.RequestPermissionsActivityBase;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.optimize.BackgroundCacheHdlr;
import com.android.contacts.util.ActionBarCustomTitle;
import com.android.contacts.util.HwLog;
import com.android.contacts.widget.ActionBarEx;
import com.android.dialer.greeting.presenter.PlaybackPresenter;
import com.android.dialer.greeting.ui.AudioRecordDialog;
import com.android.dialer.greeting.ui.ChoiceView;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.ViewUtil;
import java.util.ArrayList;
import java.util.List;

public class GreetingsFragment extends ListFragment implements LoaderCallbacks<Cursor> {
    public static final String TAG = GreetingsFragment.class.getSimpleName();
    private ActionBar mActionBar;
    private OnClickListener mActionBarListener = new OnClickListener() {
        public void onClick(View v) {
            int viewId = v.getId();
            if (viewId == 16908295) {
                GreetingsFragment.this.onStartIconClick();
            } else if (viewId == 16908296) {
                GreetingsFragment.this.onEndIconClick();
            }
        }
    };
    private GreetingsAdapter mAdapter;
    private Activity mContext;
    private MenuItem mDeleteMenu;
    private int mGreetingCount;
    private ChoiceView mHeader;
    private boolean mIsMultiMode;
    private ListView mListView;
    private OnItemClickListener mListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (GreetingsFragment.this.mIsMultiMode) {
                GreetingsFragment.this.refreshSelectedCount();
            } else {
                GreetingsFragment.this.onSingleClick(position);
            }
        }
    };
    private MenuItem mNewGreetingMenu;
    private String mPhoneAccountId;
    private PlaybackPresenter mPresenter;
    protected Drawable mSelectAllDrawable;
    private MenuItem mSelectAllMenu;
    protected Drawable mSelectNoneDrawable;
    private int mSelectedCount;
    private GreetingStatusDelta mStatusDelta;
    private ActionBarCustomTitle mTitle;

    private static class GreetingStatusQuery {
        private static final String[] PROJECTION = new String[]{"greeting_id"};

        private GreetingStatusQuery() {
        }
    }

    private class QueryActivedTask extends AsyncTask<Void, Void, Integer> {
        private QueryActivedTask() {
        }

        protected Integer doInBackground(Void... params) {
            if (GreetingsFragment.this.mContext == null) {
                return null;
            }
            String selection = "phone_account_id = ? AND deleted = ?";
            List<String> selectionArgs = new ArrayList();
            selectionArgs.add(GreetingsFragment.this.mPhoneAccountId);
            selectionArgs.add(String.valueOf(0));
            Cursor cursor = null;
            int activedId = 0;
            try {
                cursor = GreetingsFragment.this.mContext.getContentResolver().query(GreetingContract$GreetingStatus.buildSourceUri("com.android.phone"), GreetingStatusQuery.PROJECTION, selection, (String[]) selectionArgs.toArray(new String[selectionArgs.size()]), null);
                if (cursor != null && cursor.moveToFirst()) {
                    activedId = cursor.getInt(0);
                }
                if (cursor != null) {
                    cursor.close();
                }
                return Integer.valueOf(activedId);
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        protected void onPostExecute(Integer activedId) {
            super.onPostExecute(activedId);
            if (GreetingsFragment.this.isAdded()) {
                GreetingsFragment.this.mStatusDelta.setQueryItem((long) activedId.intValue());
                GreetingsFragment.this.refreshStatusDelta();
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getActivity();
        setHasOptionsMenu(true);
        this.mPhoneAccountId = this.mContext.getIntent().getStringExtra("phone_account_id");
        this.mAdapter = new GreetingsAdapter(getActivity(), null, false, this.mPhoneAccountId);
        getMenuDrawable();
        onRestoreInstance(savedInstanceState);
        this.mPresenter = PlaybackPresenter.getInstance(this.mContext);
    }

    private void onRestoreInstance(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.mIsMultiMode = savedInstanceState.getBoolean("key_is_multi_mode");
            this.mGreetingCount = savedInstanceState.getInt("key_greeting_count");
            this.mSelectedCount = savedInstanceState.getInt("key_selected_count");
            this.mAdapter.changeMode(this.mIsMultiMode);
            this.mStatusDelta = (GreetingStatusDelta) savedInstanceState.getParcelable("key_status_delta");
            return;
        }
        this.mStatusDelta = new GreetingStatusDelta();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("key_is_multi_mode", this.mIsMultiMode);
        outState.putInt("key_greeting_count", this.mGreetingCount);
        outState.putInt("key_selected_count", this.mSelectedCount);
        outState.putParcelable("key_status_delta", this.mStatusDelta);
    }

    private void getMenuDrawable() {
        this.mSelectAllDrawable = BackgroundCacheHdlr.getSelectAllDrawable(this.mContext);
        if (this.mSelectAllDrawable == null) {
            this.mSelectAllDrawable = ViewUtil.getSelectAllItemIcon(this.mContext);
        }
        this.mSelectNoneDrawable = BackgroundCacheHdlr.getSelectNoneDrawable(this.mContext);
        if (this.mSelectNoneDrawable == null) {
            this.mSelectNoneDrawable = ViewUtil.getSelectNoneItemIcon(this.mContext);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mActionBar = this.mContext.getActionBar();
        this.mTitle = new ActionBarCustomTitle(this.mContext, inflater);
        ActionBarEx.setStartIcon(this.mActionBar, true, null, this.mActionBarListener);
        ActionBarEx.setCustomTitle(this.mActionBar, this.mTitle.getTitleLayout());
        updateActionBarTitle();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void onStartIconClick() {
        if (this.mIsMultiMode) {
            this.mIsMultiMode = false;
            changeMode();
            return;
        }
        getActivity().finish();
    }

    private int getAdjPosition(int position) {
        return position - this.mListView.getHeaderViewsCount();
    }

    private void onEndIconClick() {
        if (this.mStatusDelta.isChanged()) {
            int position = getAdjPosition(this.mListView.getCheckedItemPosition());
            Uri greetingUri = null;
            if (position > -1) {
                greetingUri = this.mAdapter.getGreetingUri(position);
            }
            getActivity().startService(GreetingsSaveService.createActiveIntent(getActivity(), getGreetingStatusValues(greetingUri)));
        }
        getActivity().finish();
    }

    private ContentValues getGreetingStatusValues(Uri greetingUri) {
        long greeting_id;
        ContentValues values = new ContentValues();
        if (greetingUri != null) {
            greeting_id = ContentUris.parseId(greetingUri);
        } else {
            greeting_id = 0;
        }
        values.put("greeting_id", Long.valueOf(greeting_id));
        values.put("phone_account_id", this.mPhoneAccountId);
        values.put("dirty", Integer.valueOf(1));
        return values;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        int i;
        super.onViewCreated(view, savedInstanceState);
        this.mListView = getListView();
        ListView listView = this.mListView;
        if (this.mIsMultiMode) {
            i = 2;
        } else {
            i = 1;
        }
        listView.setChoiceMode(i);
        this.mListView.setOnItemClickListener(this.mListener);
        this.mHeader = new ChoiceView(this.mContext);
        configHeaderMode();
        this.mListView.addHeaderView(this.mHeader);
        configureListView();
        setListAdapter(this.mAdapter);
    }

    private void configureListView() {
        CommonUtilMethods.addFootEmptyViewPortrait(this.mListView, this.mContext);
        this.mListView.setFooterDividersEnabled(false);
        TypedValue v = new TypedValue();
        this.mContext.getTheme().resolveAttribute(16843284, v, true);
        this.mListView.setOverscrollFooter(this.mContext.getDrawable(v.resourceId));
    }

    private void configHeaderMode() {
        boolean z;
        boolean z2 = false;
        this.mHeader.changeMode(this.mIsMultiMode);
        ChoiceView choiceView = this.mHeader;
        if (this.mIsMultiMode) {
            z = false;
        } else {
            z = true;
        }
        choiceView.setEnabled(z);
        ChoiceView choiceView2 = this.mHeader;
        if (!this.mIsMultiMode) {
            z2 = true;
        }
        choiceView2.setTextViewEnabled(z2);
        this.mHeader.setClickable(this.mIsMultiMode);
        this.mHeader.setText(this.mContext.getString(R.string.default_ringtone));
    }

    private void onSingleClick(int position) {
        int ajustPos = getAdjPosition(position);
        if (ajustPos > -1) {
            Uri greetingUri = this.mAdapter.getGreetingUri(ajustPos);
            if (greetingUri != null) {
                this.mPresenter.resumePlayback(greetingUri);
                this.mStatusDelta.setSelectItem(this.mAdapter.getItemId(ajustPos));
                return;
            }
            return;
        }
        this.mPresenter.pausePresenter();
        this.mStatusDelta.setSelectItem((long) position);
    }

    public void onStart() {
        super.onStart();
        getLoaderManager().initLoader(0, new Bundle(), this);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(getActivity());
        this.mAdapter.configureLoader(loader, getActivity());
        return loader;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        this.mAdapter.changeCursor(data);
        this.mAdapter.notifyDataSetChanged();
        this.mGreetingCount = data == null ? 0 : data.getCount();
        if (this.mContext != null) {
            this.mContext.invalidateOptionsMenu();
        }
        if (!this.mIsMultiMode) {
            refreshActivedSelect();
        }
    }

    private void refreshActivedSelect() {
        new QueryActivedTask().execute(new Void[0]);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private boolean refreshStatusDelta() {
        long greetingId = this.mStatusDelta.getSelectItem();
        Log.d(TAG, "refreshStatusDelta greetingId : " + greetingId);
        if (greetingId == 0) {
            this.mListView.setItemChecked(0, true);
            return true;
        }
        int count = this.mListView.getCount();
        int header = this.mListView.getHeaderViewsCount();
        int ajustCount = (count - header) - this.mListView.getFooterViewsCount();
        for (int i = header; i < header + ajustCount; i++) {
            if (this.mAdapter.getItemId(getAdjPosition(i)) == greetingId) {
                this.mListView.setItemChecked(i, true);
                return true;
            }
        }
        return false;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.greeting_menu, menu);
        this.mNewGreetingMenu = menu.findItem(R.id.menu_new_greeting);
        this.mDeleteMenu = menu.findItem(R.id.menu_delete);
        this.mSelectAllMenu = menu.findItem(R.id.menu_action_selectall);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        boolean z = true;
        super.onPrepareOptionsMenu(menu);
        MenuItem menuItem;
        boolean z2;
        if (this.mIsMultiMode) {
            this.mNewGreetingMenu.setVisible(false);
            menuItem = this.mDeleteMenu;
            if (this.mSelectedCount > 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            menuItem.setEnabled(z2);
            this.mSelectAllMenu.setVisible(true);
            if (this.mGreetingCount != this.mSelectedCount) {
                z = false;
            }
            setSelectAllItemTitle(z);
        } else {
            this.mNewGreetingMenu.setVisible(true);
            menuItem = this.mDeleteMenu;
            if (this.mGreetingCount > 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            menuItem.setVisible(z2);
            this.mSelectAllMenu.setVisible(false);
            MenuItem menuItem2 = this.mNewGreetingMenu;
            if (this.mGreetingCount >= 10) {
                z = false;
            }
            menuItem2.setEnabled(z);
        }
        updateActionBarTitle();
    }

    private void updateActionBarTitle() {
        boolean z = false;
        if (this.mIsMultiMode) {
            this.mTitle.setCustomTitle(CommonUtilMethods.getMultiSelectionTitle(this.mContext, this.mSelectedCount), this.mSelectedCount);
        } else {
            this.mTitle.setCustomTitle(getString(R.string.menu_greeting), 0);
        }
        ActionBar actionBar = this.mActionBar;
        if (!this.mIsMultiMode) {
            z = true;
        }
        ActionBarEx.setEndIcon(actionBar, z, null, this.mActionBarListener);
    }

    private void setSelectAllItemTitle(boolean isAllSelected) {
        if (this.mSelectAllMenu != null) {
            if (isAllSelected) {
                this.mSelectAllMenu.setTitle(R.string.menu_select_none);
                this.mSelectAllMenu.setIcon(this.mSelectNoneDrawable);
            } else {
                this.mSelectAllMenu.setTitle(R.string.contact_menu_select_all);
                this.mSelectAllMenu.setIcon(this.mSelectAllDrawable);
            }
            this.mSelectAllMenu.setChecked(isAllSelected);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        boolean z = true;
        switch (item.getItemId()) {
            case R.id.menu_action_selectall:
                if (this.mGreetingCount != this.mSelectedCount) {
                    z = false;
                }
                doActionMenu(z);
                break;
            case R.id.menu_delete:
                if (!this.mIsMultiMode) {
                    this.mIsMultiMode = true;
                    changeMode();
                    this.mPresenter.pausePresenter();
                    break;
                }
                doDelete();
                break;
            case R.id.menu_new_greeting:
                this.mPresenter.pausePresenter();
                if (onCheckNewGreetingPermission()) {
                    showNewGreetingDialog();
                    break;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean onCheckNewGreetingPermission() {
        if (this.mContext.checkSelfPermission("android.permission.RECORD_AUDIO") == 0) {
            return true;
        }
        requestPermissions(new String[]{"android.permission.RECORD_AUDIO"}, 0);
        return false;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0 && permissions != null && permissions.length > 0) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != 0) {
                    try {
                        startActivity(RequestPermissionsActivityBase.createRequestPermissionIntent(permissions, getContext().getPackageName()));
                    } catch (Exception e) {
                        HwLog.e(TAG, "Activity not find!");
                    }
                    return;
                }
            }
            showNewGreetingDialog();
        }
    }

    private void showNewGreetingDialog() {
        AudioRecordDialog.show(this.mPhoneAccountId, getFragmentManager());
    }

    private void refreshSelectedCount() {
        int count = this.mListView.getCount();
        int header = this.mListView.getHeaderViewsCount();
        int ajustCount = (count - header) - this.mListView.getFooterViewsCount();
        this.mSelectedCount = 0;
        for (int i = header; i < header + ajustCount; i++) {
            if (this.mListView.isItemChecked(i)) {
                this.mSelectedCount++;
            }
        }
        getActivity().invalidateOptionsMenu();
    }

    public boolean onBackPressed() {
        if (!this.mIsMultiMode) {
            return true;
        }
        this.mIsMultiMode = false;
        changeMode();
        return false;
    }

    private void changeMode() {
        int i;
        this.mSelectedCount = 0;
        this.mListView.clearChoices();
        this.mAdapter.changeMode(this.mIsMultiMode);
        ListView listView = this.mListView;
        if (this.mIsMultiMode) {
            i = 2;
        } else {
            i = 1;
        }
        listView.setChoiceMode(i);
        if (!this.mIsMultiMode) {
            refreshStatusDelta();
        }
        configHeaderMode();
        this.mAdapter.notifyDataSetChanged();
        getActivity().invalidateOptionsMenu();
    }

    private void doDelete() {
        StringBuilder builder = new StringBuilder();
        int count = this.mListView.getCount();
        int header = this.mListView.getHeaderViewsCount();
        int ajustCount = (count - header) - this.mListView.getFooterViewsCount();
        for (int i = header; i < header + ajustCount; i++) {
            if (this.mListView.isItemChecked(i)) {
                long greetingId = this.mAdapter.getItemId(getAdjPosition(i));
                builder.append(greetingId).append(",");
                this.mStatusDelta.invalidateGreetingId(greetingId);
            }
        }
        if (builder.length() > 0) {
            builder.setLength(builder.length() - 1);
        }
        getActivity().startService(GreetingsSaveService.createDelteIntent(getActivity(), builder.toString()));
        this.mIsMultiMode = false;
        changeMode();
    }

    private void doActionMenu(boolean selected) {
        int i = 0;
        int count = this.mListView.getCount();
        int header = this.mListView.getHeaderViewsCount();
        int ajustCount = (count - header) - this.mListView.getFooterViewsCount();
        for (int i2 = header; i2 < header + ajustCount; i2++) {
            this.mListView.setItemChecked(i2, !selected);
        }
        if (!selected) {
            i = ajustCount;
        }
        this.mSelectedCount = i;
        getActivity().invalidateOptionsMenu();
    }

    public void onPause() {
        super.onPause();
        this.mPresenter.onPause();
    }

    public void onDestroy() {
        this.mPresenter.onDestroy();
        super.onDestroy();
    }
}
