package com.android.contacts.group;

import android.animation.Animator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import com.android.contacts.ContactSplitUtils;
import com.android.contacts.activities.GroupDetailActivity;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.fragment.HwBaseFragment;
import com.android.contacts.group.GroupBrowseListAdapter.GroupListItemViewCache;
import com.android.contacts.hap.CommonConstants;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.utils.ActionBarTitle;
import com.android.contacts.hap.utils.ScreenUtils;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.HwLog;
import com.android.contacts.widget.AutoScrollListView;
import com.google.android.gms.R;

public class SmartGroupBrowseListFragment extends HwBaseFragment {
    private ActionBarTitle mActionBarTitle;
    private GroupBrowseListAdapter mAdapter;
    private Context mContext;
    private AutoScrollListView mListView;
    private View mProgressBar;
    private View mRootView;
    private final LoaderCallbacks<Cursor> mSmartGroupClassifyLoader = new LoaderCallbacks<Cursor>() {
        public CursorLoader onCreateLoader(int id, Bundle args) {
            if (CommonConstants.LOG_DEBUG) {
                HwLog.d("SmartGroupBrowseListFragment", "SmartGroupClassifyLoader: " + SmartGroupBrowseListFragment.this.mSmartGroupType + "#onCreateLoader.");
            }
            if ("smart_groups_company".equals(SmartGroupBrowseListFragment.this.mSmartGroupType)) {
                return new CursorLoader(SmartGroupBrowseListFragment.this.mContext, SmartGroupUtil.COMPANY_CLASSIFY_URI, null, null, null, null);
            }
            if ("smart_groups_location".equals(SmartGroupBrowseListFragment.this.mSmartGroupType)) {
                return new CursorLoader(SmartGroupBrowseListFragment.this.mContext, SmartGroupUtil.LOCATION_URI, null, null, null, null);
            }
            if ("smart_groups_last_contact_time".equals(SmartGroupBrowseListFragment.this.mSmartGroupType)) {
                return new SmartGroupLastTimeContactLoader(SmartGroupBrowseListFragment.this.mContext);
            }
            return null;
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (CommonConstants.LOG_DEBUG) {
                HwLog.d("SmartGroupBrowseListFragment", "SmartGroupClassifyLoader: " + SmartGroupBrowseListFragment.this.mSmartGroupType + "#onLoadFinished");
            }
            if (data != null) {
                SmartGroupBrowseListFragment.this.mAdapter.setCursor(data);
                if (SmartGroupBrowseListFragment.this.mProgressBar != null) {
                    SmartGroupBrowseListFragment.this.mProgressBar.setVisibility(8);
                }
                SmartGroupBrowseListFragment.this.refreshListViewDelayed();
            }
        }

        public void onLoaderReset(Loader<Cursor> loader) {
            if (CommonConstants.LOG_DEBUG) {
                HwLog.d("SmartGroupBrowseListFragment", "SmartGroupClassifyLoader: " + SmartGroupBrowseListFragment.this.mSmartGroupType + "#onLoaderReset.");
            }
        }
    };
    private String mSmartGroupType = null;

    public SmartGroupBrowseListFragment(String smartGroupType) {
        this.mSmartGroupType = smartGroupType;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (CommonUtilMethods.calcIfNeedSplitScreen() && savedInstanceState == null && (getActivity() instanceof PeopleActivity)) {
            savedInstanceState = CommonUtilMethods.getInstanceState();
        }
        if (!CommonUtilMethods.calcIfNeedSplitScreen()) {
            setHasOptionsMenu(true);
        }
        if (savedInstanceState != null) {
            this.mSmartGroupType = savedInstanceState.getString("smart_groups_type");
        } else if (this.mSmartGroupType == null) {
            this.mSmartGroupType = getActivity().getIntent().getStringExtra("smart_groups_type");
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("smart_groups_type", this.mSmartGroupType);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!CommonUtilMethods.calcIfNeedSplitScreen()) {
            configActionBarTitle();
        }
    }

    private void configActionBarTitle() {
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null && this.mSmartGroupType != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if ("smart_groups_company".equals(this.mSmartGroupType)) {
                actionBar.setTitle(R.string.contacts_company);
            } else if ("smart_groups_location".equals(this.mSmartGroupType)) {
                actionBar.setTitle(R.string.contacts_location);
            } else if ("smart_groups_last_contact_time".equals(this.mSmartGroupType)) {
                actionBar.setTitle(R.string.contacts_last_contact_time);
            }
        }
    }

    private void configTitle() {
        if (this.mActionBarTitle != null && this.mSmartGroupType != null) {
            if ("smart_groups_company".equals(this.mSmartGroupType)) {
                this.mActionBarTitle.setTitle(getString(R.string.contacts_company));
            } else if ("smart_groups_location".equals(this.mSmartGroupType)) {
                this.mActionBarTitle.setTitle(getString(R.string.contacts_location));
            } else if ("smart_groups_last_contact_time".equals(this.mSmartGroupType)) {
                this.mActionBarTitle.setTitle(getString(R.string.contacts_last_contact_time));
            }
        }
    }

    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        return ContactSplitUtils.createSplitAnimator(transit, enter, nextAnim, this.mRootView, R.drawable.multiselection_background, getActivity());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mRootView = inflater.inflate(R.layout.smart_group_browse_list_fragment, null);
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            this.mActionBarTitle = new ActionBarTitle(this.mContext, this.mRootView.findViewById(R.id.edit_layout));
            this.mRootView.findViewById(R.id.edit_layout).setVisibility(0);
            configTitle();
            this.mActionBarTitle.setBackIcon(true, null, new OnClickListener() {
                public void onClick(View v) {
                    SmartGroupBrowseListFragment.this.getActivity().onBackPressed();
                }
            });
            if (getActivity() instanceof PeopleActivity) {
                PeopleActivity act = (PeopleActivity) getActivity();
                ScreenUtils.adjustPaddingTop(act, (ViewGroup) this.mRootView.findViewById(R.id.smart_group_browse_list_content), ContactSplitUtils.isSpiltTwoColumn(act, act.isInMultiWindowMode()));
            }
        } else {
            this.mRootView.findViewById(R.id.edit_layout).setVisibility(8);
        }
        this.mAdapter = new GroupBrowseListAdapter(this.mContext);
        this.mAdapter.setSmartGroupType(this.mSmartGroupType);
        this.mProgressBar = this.mRootView.findViewById(R.id.progressContainer);
        this.mProgressBar.setVisibility(0);
        this.mListView = (AutoScrollListView) this.mRootView.findViewById(R.id.list);
        this.mListView.setFastScrollEnabled(true);
        this.mListView.setItemsCanFocus(true);
        this.mAdapter.setShowBottomDivider(false);
        this.mListView.setAdapter(this.mAdapter);
        this.mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                HwLog.d("SmartGroupBrowseListFragment", "onCreateView position:" + position);
                GroupListItemViewCache groupListItem = (GroupListItemViewCache) view.getTag();
                if (groupListItem != null) {
                    Intent intent = new Intent(SmartGroupBrowseListFragment.this.getActivity(), GroupDetailActivity.class);
                    intent.putExtra("key_from_smart_group", true);
                    intent.setData(groupListItem.getUri());
                    if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                        Activity activity = SmartGroupBrowseListFragment.this.getActivity();
                        if ((activity instanceof PeopleActivity) && groupListItem.getUri() != null) {
                            ((PeopleActivity) activity).changeRightContainer(SmartGroupBrowseListFragment.this, new GroupDetailFragment(intent));
                        }
                    } else {
                        SmartGroupBrowseListFragment.this.startActivity(intent);
                    }
                }
                if ("smart_groups_last_contact_time".equals(SmartGroupBrowseListFragment.this.getActivity().getIntent().getStringExtra("smart_groups_type"))) {
                    switch (position) {
                        case 0:
                            StatisticalHelper.report(1194);
                            return;
                        case 1:
                            StatisticalHelper.report(1195);
                            return;
                        case 2:
                            StatisticalHelper.report(1196);
                            return;
                        case 3:
                            StatisticalHelper.report(1197);
                            return;
                        default:
                            return;
                    }
                }
            }
        });
        return this.mRootView;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }

    public void onDetach() {
        super.onDetach();
        this.mContext = null;
    }

    public void onStart() {
        boolean isSmartGroup;
        if ("smart_groups_company".equals(this.mSmartGroupType) || "smart_groups_location".equals(this.mSmartGroupType)) {
            isSmartGroup = true;
        } else {
            isSmartGroup = "smart_groups_last_contact_time".equals(this.mSmartGroupType);
        }
        if (isSmartGroup) {
            getLoaderManager().restartLoader(1, new Bundle(), this.mSmartGroupClassifyLoader);
        }
        super.onStart();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                getActivity().finish();
                return true;
            default:
                return false;
        }
    }

    private void refreshListViewDelayed() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (SmartGroupBrowseListFragment.this.mAdapter != null) {
                    SmartGroupBrowseListFragment.this.mAdapter.notifyDataSetChanged();
                }
            }
        }, 50);
    }
}
