package com.android.contacts.activities;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageView;
import com.android.contacts.ContactsActivity;
import com.android.contacts.ContactsUtils;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.list.JoinContactListFragment;
import com.android.contacts.list.OnContactPickerActionListener;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class JoinContactActivity extends ContactsActivity {
    private JoinContactListFragment mListFragment;
    private ImageView mSearchClearButton;
    private EditText mSearchView;
    private long mTargetContactId;

    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof JoinContactListFragment) {
            this.mListFragment = (JoinContactListFragment) fragment;
            setupActionListener();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
        }
        getWindow().setFlags(16777216, 16777216);
        setTheme(R.style.JoinContactActivityTheme);
        Intent intent = getIntent();
        this.mTargetContactId = intent.getLongExtra("com.android.contacts.action.CONTACT_ID", -1);
        if (this.mTargetContactId == -1) {
            HwLog.e("JoinContactActivity", "Intent " + intent.getAction() + " is missing required extra: " + "com.android.contacts.action.CONTACT_ID");
            setResult(0);
            finish();
            return;
        }
        setContentView(R.layout.join_contact_picker);
        setTitle(R.string.titleJoinContactDataWith);
        if (this.mListFragment == null) {
            this.mListFragment = new JoinContactListFragment();
            getFragmentManager().beginTransaction().replace(R.id.list_container, this.mListFragment).commitAllowingStateLoss();
        }
        if (!(EmuiFeatureManager.isProductCustFeatureEnable() && isJoinContactsRequired())) {
            prepareSearchViewAndActionBar();
        }
    }

    public boolean isJoinContactsRequired() {
        return SystemProperties.getBoolean("ro.config.hw_enable_join", false);
    }

    private void setupActionListener() {
        this.mListFragment.setTargetContactId(this.mTargetContactId);
        this.mListFragment.setOnContactPickerActionListener(new OnContactPickerActionListener() {
            public void onPickContactAction(Uri contactUri) {
                Intent intent = new Intent(null, contactUri);
                intent.putExtra("com.android.contacts.action.CONTACT_ID", JoinContactActivity.this.mTargetContactId);
                JoinContactActivity.this.setResult(-1, intent);
                JoinContactActivity.this.finish();
            }

            public void onShortcutIntentCreated(Intent intent) {
            }

            public void onCreateNewContactAction() {
            }

            public void onEditContactAction(Uri contactLookupUri) {
            }
        });
    }

    private void prepareSearchViewAndActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            View searchViewLayout = LayoutInflater.from(actionBar.getThemedContext()).inflate(R.layout.custom_action_bar_with_searchview, null);
            View mSearchLayout = searchViewLayout.findViewById(R.id.contactListsearchlayout);
            mSearchLayout.setVisibility(0);
            mSearchLayout.setBackgroundResource(R.drawable.textfield_activated_holo_light);
            this.mSearchView = (EditText) searchViewLayout.findViewById(R.id.search_view);
            ContactsUtils.configureSearchViewInputType(this.mSearchView);
            this.mSearchClearButton = (ImageView) searchViewLayout.findViewById(R.id.clearSearchResult);
            this.mSearchView.setHint(CommonUtilMethods.getSearchViewSpannableHint(this, getResources().getString(R.string.contact_hint_findContacts), this.mSearchView.getTextSize()));
            this.mSearchView.setCursorVisible(false);
            this.mSearchView.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    v.onTouchEvent(event);
                    JoinContactActivity.this.mSearchView.setCursorVisible(true);
                    return true;
                }
            });
            this.mSearchClearButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    JoinContactActivity.this.mListFragment.setSearchMode(false);
                    JoinContactActivity.this.mSearchView.setText(null);
                }
            });
            this.mSearchView.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String str = null;
                    if (s == null || TextUtils.isEmpty(s.toString().trim())) {
                        JoinContactListFragment -get0 = JoinContactActivity.this.mListFragment;
                        if (s != null) {
                            str = s.toString();
                        }
                        -get0.setQueryString(str, true);
                        JoinContactActivity.this.mSearchClearButton.setVisibility(8);
                        return;
                    }
                    JoinContactActivity.this.mListFragment.setQueryString(s.toString(), true);
                    JoinContactActivity.this.mSearchClearButton.setVisibility(0);
                }
            });
            actionBar.setCustomView(searchViewLayout, new LayoutParams(-1, -2));
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            this.mSearchView.clearFocus();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                setResult(0);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("targetContactId", this.mTargetContactId);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.mTargetContactId = savedInstanceState.getLong("targetContactId");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == -1) {
            this.mListFragment.onPickerResult(data);
        }
    }
}
