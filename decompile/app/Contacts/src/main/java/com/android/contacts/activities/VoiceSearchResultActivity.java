package com.android.contacts.activities;

import android.animation.Animator;
import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import com.android.contacts.ContactsActivity;
import com.android.contacts.calllog.IntentProvider;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.interactions.ContactDeletionInteraction;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.list.DefaultContactBrowseListFragment;
import com.android.contacts.list.OnContactBrowserActionListener;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import huawei.com.android.internal.widget.HwFragmentContainer;
import java.util.ArrayList;

public class VoiceSearchResultActivity extends ContactsActivity {
    private final int MAX_STRING_LIST_LENGTH = 5;
    private final String SEARCH_LIST = "searchStrings";
    private final int SUBACTIVITY_EDIT_CONTACT = 1;
    private DefaultContactBrowseListFragment mListFragment;

    private final class ContactBrowserActionListener implements OnContactBrowserActionListener {
        ContactBrowserActionListener() {
        }

        public void onSelectionChange() {
        }

        public void onViewContactAction(Uri contactLookupUri, boolean isEnterpriseContact) {
            Intent intent = IntentProvider.getViewContactIntent(VoiceSearchResultActivity.this, contactLookupUri);
            if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                VoiceSearchResultActivity.this.openContactInfo(contactLookupUri);
                return;
            }
            long contactId = CommonUtilMethods.getContactIdFromUri(contactLookupUri);
            intent.putExtra("EXTRA_URI_CONTACT_ID", contactId);
            intent.putExtra("EXTRA_LIST_TO_DETAIL_URI", contactLookupUri);
            intent.putExtra("EXTRA_CONTACT_ACCOUNT_TYPE", CommonUtilMethods.getAccountTypeFromUri(VoiceSearchResultActivity.this.getApplicationContext(), contactId));
            VoiceSearchResultActivity.this.startActivity(intent);
        }

        public void onEditContactAction(Uri contactLookupUri) {
            Intent intent = IntentProvider.getEditorContactIntent(VoiceSearchResultActivity.this, contactLookupUri);
            Bundle extras = VoiceSearchResultActivity.this.getIntent().getExtras();
            if (extras != null) {
                intent.putExtras(extras);
            }
            intent.putExtra("finishActivityOnSaveCompleted", true);
            VoiceSearchResultActivity.this.startActivityForResult(intent, 1);
        }

        public void onDeleteContactAction(Uri contactUri) {
            ContactDeletionInteraction.start(VoiceSearchResultActivity.this, contactUri, false, false);
        }

        public void onInvalidSelection() {
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        ArrayList<String> stringListForSearch;
        setTheme(R.style.PeopleTheme);
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            requestWindowFeature(9);
            getWindow().addFlags(67108864);
        }
        setContentView(R.layout.voice_search_result);
        this.mListFragment = (DefaultContactBrowseListFragment) getFragmentManager().findFragmentById(R.id.search_result_fragment);
        this.mListFragment.setSimpleShowMode();
        this.mListFragment.setQuickContactEnabled(true);
        this.mListFragment.setOnContactListActionListener(new ContactBrowserActionListener());
        Intent intent = getIntent();
        if (intent == null || intent.getStringArrayListExtra("searchStrings") == null) {
            stringListForSearch = new ArrayList(0);
            this.mListFragment.setFilter(ContactListFilter.createFilterWithType(-2));
        } else {
            ArrayList<String> stringListReturned = intent.getStringArrayListExtra("searchStrings");
            stringListForSearch = new ArrayList(stringListReturned.subList(0, Math.min(stringListReturned.size(), 5)));
            this.mListFragment.setFilter((ContactListFilter) intent.getParcelableExtra("contactListFilter"));
        }
        if (HwLog.HWDBG) {
            HwLog.d("VoiceSearchResultActivity", "onCreate, stringListForSearch length=" + stringListForSearch.size());
        }
        this.mListFragment.setQueryMultiStrings(stringListForSearch);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.search_result_title);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public Animator getAnimator(View view, int transit, boolean enter) {
        HwFragmentContainer fc = this.mListFragment.getFragmentContainer();
        if (fc == null || view == null) {
            return null;
        }
        return fc.getAnimator(view, transit, enter);
    }

    public void openContactInfo(Uri uri) {
        Intent intent = IntentProvider.getViewContactIntent(this, uri);
        ContactInfoFragment contactInfo = new ContactInfoFragment();
        contactInfo.setIntent(intent);
        this.mListFragment.openRightContainer(contactInfo);
    }

    public FragmentManager getFrameFragmentManager() {
        if (this.mListFragment != null) {
            return this.mListFragment.getChildFragmentManager();
        }
        return null;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == -1 && data != null) {
                    this.mListFragment.setSelectionRequired(true);
                    this.mListFragment.reloadDataAndSetSelectedUri(data.getData());
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void onBackPressed() {
        if (isBackPressed()) {
            super.onBackPressed();
        }
    }

    private boolean isBackPressed() {
        boolean ret = true;
        if (!(this.mListFragment == null || this.mListFragment.getFragmentContainer() == null)) {
            ret = this.mListFragment.getFragmentContainer().isBackPressed();
            if (!ret) {
                this.mListFragment.setNeedShowActionbarAnimate(true);
                this.mListFragment.showOrHideActionbar();
            }
        }
        return ret;
    }
}
