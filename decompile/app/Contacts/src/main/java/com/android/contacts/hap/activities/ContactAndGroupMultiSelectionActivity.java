package com.android.contacts.hap.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import com.android.contacts.ContactsApplication;
import com.android.contacts.activities.RequestPermissionsActivity;
import com.android.contacts.hap.list.ContactDataMultiSelectFragment;
import com.android.contacts.hap.list.ContactDataMultiSelectFragmentEx;
import com.android.contacts.hap.list.FavoritesAndGroupsMultiSelectFragment;
import com.android.contacts.hap.list.FrequentContactMultiSelectFragment;
import com.android.contacts.hap.util.ActionBarCustom;
import com.android.contacts.hap.util.SelectedDataCache;
import com.android.contacts.list.ContactEntryListAdapter;
import com.android.contacts.list.ContactEntryListFragment;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import com.google.common.collect.Maps;
import huawei.android.widget.SubTabWidget;
import huawei.android.widget.SubTabWidget.SubTab;
import huawei.support.v13.app.SubTabFragmentPagerAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class ContactAndGroupMultiSelectionActivity extends ContactMultiSelectionActivity {
    private static int mCurrentMode = -1;
    private ActionBarCustom mCustActionBar;
    public HashMap<Uri, ArrayList<String>> mDataUriWithGroupsMap = new HashMap();
    private FrequentContactMultiSelectFragment mFrequentFragment;
    private FavoritesAndGroupsMultiSelectFragment mGroupSelectFragment;
    public HashMap<Uri, ArrayList<String>> mSelectedDataUriWithGroupsMap = new HashMap();
    public ArrayList<String> mSelectedGroupIdList = new ArrayList();
    SubTabFragmentPagerAdapter mSubTabFragmentPagerAdapter;
    private SubTabWidget mSubTabWidget;
    protected ViewPager mViewPager;

    public class SubTabAdapter extends SubTabFragmentPagerAdapter {
        public SubTabAdapter(Activity activity, ViewPager pager, SubTabWidget subTabWidget) {
            super(activity, pager, subTabWidget);
        }

        public void onSubTabSelected(SubTab subTab, FragmentTransaction ft) {
            super.onSubTabSelected(subTab, ft);
            fragmenSelected(subTab, ft);
        }

        public void onPageSelected(int position) {
            super.onPageSelected(position);
            fragmenSelected(position);
        }

        private void fragmenSelected(SubTab subTab, FragmentTransaction ft) {
            for (int i = 0; i < getCount() && ContactAndGroupMultiSelectionActivity.this.mSubTabWidget != null; i++) {
                if (subTab == ContactAndGroupMultiSelectionActivity.this.mSubTabWidget.getSubTabAt(i)) {
                    Fragment fragment = getItem(i);
                    if (fragment instanceof FrequentContactMultiSelectFragment) {
                        ((FrequentContactMultiSelectFragment) fragment).onSubTabSelected(subTab, ft);
                    } else if (fragment instanceof ContactDataMultiSelectFragmentEx) {
                        ((ContactDataMultiSelectFragmentEx) fragment).onSubTabSelected(subTab, ft);
                    } else if (fragment instanceof FavoritesAndGroupsMultiSelectFragment) {
                        ((FavoritesAndGroupsMultiSelectFragment) fragment).onSubTabSelected(subTab, ft);
                    }
                }
            }
        }

        private void fragmenSelected(int position) {
            SubTab subTab = ContactAndGroupMultiSelectionActivity.this.mSubTabWidget.getSubTabAt(position);
            Fragment fragment = getItem(position);
            if (fragment instanceof FrequentContactMultiSelectFragment) {
                ((FrequentContactMultiSelectFragment) fragment).onSubTabSelected(subTab, null);
            } else if (fragment instanceof ContactDataMultiSelectFragmentEx) {
                ((ContactDataMultiSelectFragmentEx) fragment).onSubTabSelected(subTab, null);
            } else if (fragment instanceof FavoritesAndGroupsMultiSelectFragment) {
                ((FavoritesAndGroupsMultiSelectFragment) fragment).onSubTabSelected(subTab, null);
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                onSearchRequested();
                return true;
            case R.id.menu_action_done:
                setResultForMessaging();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        getWindow().setFlags(16777216, 16777216);
        if (savedInstanceState != null) {
            this.mSelectedGroupIdList = savedInstanceState.getStringArrayList("key_selected_groups");
            this.mSelectedDataUriWithGroupsMap = (HashMap) savedInstanceState.getSerializable("key_selected_data_uri_group_map");
            this.mDataUriWithGroupsMap = (HashMap) savedInstanceState.getSerializable("key_data_uri_group_map");
        }
        if (!EmuiVersion.isSupportEmui()) {
            this.mCustActionBar = new ActionBarCustom(this, getActionBar());
        }
        setTheme(R.style.MultiSelectThemewithTab);
        this.mSubTabWidget = initializeSubTabs(this);
    }

    private SubTabWidget initializeSubTabs(Context context) {
        if (this.mViewPager == null) {
            return null;
        }
        SubTabWidget subTabWidget = (SubTabWidget) findViewById(R.id.subTab_layout);
        this.mSubTabFragmentPagerAdapter = new SubTabAdapter(this, this.mViewPager, subTabWidget);
        SubTab subTab = subTabWidget.newSubTab(getString(R.string.contacts_multi_select_tab_recently));
        subTab.setSubTabId(R.id.contact_multi_selection_tab_recent);
        this.mSubTabFragmentPagerAdapter.addSubTab(subTab, this.mFrequentFragment, null, true);
        subTab = subTabWidget.newSubTab(getString(R.string.contactsLabel));
        subTab.setSubTabId(R.id.contact_multi_selection_tab_contacts);
        this.mSubTabFragmentPagerAdapter.addSubTab(subTab, this.mMultiSelectFragment, null, false);
        subTab = subTabWidget.newSubTab(getString(R.string.contactsGroupsLabel));
        subTab.setSubTabId(R.id.contact_multi_selection_tab_groups);
        this.mSubTabFragmentPagerAdapter.addSubTab(subTab, this.mGroupSelectFragment, null, false);
        return subTabWidget;
    }

    protected boolean configureListFragment(Bundle args) {
        if (this.mFrequentFragment == null) {
            this.mFrequentFragment = new FrequentContactMultiSelectFragment();
        }
        if (this.mMultiSelectFragment == null) {
            this.mMultiSelectFragment = getFragmentToLoad();
        }
        if (this.mGroupSelectFragment == null) {
            this.mGroupSelectFragment = new FavoritesAndGroupsMultiSelectFragment();
        }
        this.mFrequentFragment.setContactsRequest(this.mRequest);
        this.mMultiSelectFragment.setContactsRequest(this.mRequest);
        this.mGroupSelectFragment.setContactsRequest(this.mRequest);
        if (!EmuiVersion.isSupportEmui()) {
            this.mFrequentFragment.setCustActionBar(this.mCustActionBar);
            if (this.mMultiSelectFragment instanceof ContactDataMultiSelectFragmentEx) {
                ((ContactDataMultiSelectFragmentEx) this.mMultiSelectFragment).setCustActionBar(this.mCustActionBar);
            }
            this.mGroupSelectFragment.setCustActionBar(this.mCustActionBar);
            invalidateOptionsMenu();
        }
        return true;
    }

    protected int getViewToUse() {
        return R.layout.simple_frame_layout_msg;
    }

    protected void addMultiFragmentPropertiesIfMultiTabbed() {
        this.mViewPager = (ViewPager) findViewById(R.id.pager);
        this.mViewPager.setOffscreenPageLimit(2);
        getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList("key_selected_groups", this.mSelectedGroupIdList);
        outState.putSerializable("key_selected_data_uri_group_map", this.mSelectedDataUriWithGroupsMap);
        outState.putSerializable("key_data_uri_group_map", this.mDataUriWithGroupsMap);
        super.onSaveInstanceState(outState);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    public ContactEntryListFragment<ContactEntryListAdapter> getFragmentToLoad() {
        return new ContactDataMultiSelectFragmentEx();
    }

    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof FrequentContactMultiSelectFragment) {
            this.mFrequentFragment = (FrequentContactMultiSelectFragment) fragment;
        } else if (fragment instanceof ContactDataMultiSelectFragment) {
            this.mMultiSelectFragment = (ContactDataMultiSelectFragment) fragment;
        } else if (fragment instanceof FavoritesAndGroupsMultiSelectFragment) {
            this.mGroupSelectFragment = (FavoritesAndGroupsMultiSelectFragment) fragment;
        }
    }

    public void setResultForMessaging() {
        Intent intent = new Intent();
        intent.putExtra("SelItemData_KeyValue", new ArrayList(this.mSelectedDataUris));
        setResult(-1, intent);
        finish();
    }

    public void initSelectedDataCache() {
        this.mGroupSelectFragment.setSelectedData(new SelectedDataCache() {
            public void removeSelectedUri(Uri uri) {
                if (ContactAndGroupMultiSelectionActivity.this.mSelectedDataUris.contains(uri)) {
                    ContactAndGroupMultiSelectionActivity.this.mSelectedDataUris.remove(uri);
                    ContactAndGroupMultiSelectionActivity.this.invalidateOptionsMenu();
                }
            }

            public Set<Uri> getSelectedDataUri() {
                return ContactAndGroupMultiSelectionActivity.this.mSelectedDataUris;
            }

            public void setSelectedUri(Uri uri) {
                if (!ContactAndGroupMultiSelectionActivity.this.mSelectedDataUris.contains(uri)) {
                    ContactAndGroupMultiSelectionActivity.this.mSelectedDataUris.add(uri);
                    ContactAndGroupMultiSelectionActivity.this.invalidateOptionsMenu();
                }
            }

            public int getMaxLimit() {
                return ContactAndGroupMultiSelectionActivity.this.mMaxLimit;
            }
        });
    }

    public boolean onSearchRequested() {
        return true;
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mSelectedGroupIdList = null;
        this.mDataUriWithGroupsMap = null;
        this.mSelectedDataUriWithGroupsMap = null;
    }

    public static boolean isPrivateModeChange() {
        if (HwLog.HWDBG) {
            HwLog.d("ContactMultiSelectionActivity", "mCurrentMode:" + mCurrentMode + "   private:" + ContactsApplication.isPrivateModeOn());
        }
        if (mCurrentMode == -1) {
            if (ContactsApplication.isPrivateModeOn()) {
                mCurrentMode = 1;
            } else {
                mCurrentMode = 0;
            }
            return false;
        }
        boolean result = false;
        if (ContactsApplication.isPrivateModeOn()) {
            if (mCurrentMode != 1) {
                mCurrentMode = 1;
                result = true;
            }
        } else if (mCurrentMode != 0) {
            mCurrentMode = 0;
            result = true;
        }
        return result;
    }

    public HashMap<Long, Long> getFrequentDataMap() {
        HashMap<Long, Long> tmp = null;
        if (this.mMultiSelectFragment instanceof ContactDataMultiSelectFragment) {
            tmp = ((ContactDataMultiSelectFragment) this.mMultiSelectFragment).getFrequentDataMap();
        }
        if (tmp == null) {
            return Maps.newHashMap();
        }
        return tmp;
    }
}
