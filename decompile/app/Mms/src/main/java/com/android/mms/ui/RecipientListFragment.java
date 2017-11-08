package com.android.mms.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.Contacts;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.android.messaging.util.OsUtil;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.util.HwCustEcidLookup;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.RcsRecipientListFragment;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.ui.EmuiMenu;
import com.huawei.mms.ui.HwListFragment;
import com.huawei.mms.ui.MmsEmuiActionBar;
import com.huawei.mms.util.AvatarCache;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.PrivacyModeReceiver;
import com.huawei.mms.util.PrivacyModeReceiver.ModeChangeListener;
import com.huawei.mms.util.PrivacyModeReceiver.PrivacyStateListener;
import java.util.Arrays;

public class RecipientListFragment extends HwListFragment implements OnClickListener {
    private static HwCustEcidLookup mHwCustEcidLookup = ((HwCustEcidLookup) HwCustUtils.createObj(HwCustEcidLookup.class, new Object[0]));
    ModeChangeListener localPrivacyMonitor = new ModeChangeListener() {
        public void onModeChange(Context context, boolean isInPrivacy) {
            if (isInPrivacy) {
                RecipientListFragment.this.updateContactAndFresh();
            } else {
                RecipientListFragment.this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        if (RecipientListFragment.this.mThreadId != -1 && PrivacyModeReceiver.isPrivacyThread(RecipientListFragment.this.getContext(), RecipientListFragment.this.mThreadId)) {
                            RecipientListFragment.this.finishSelf(false);
                        }
                    }
                }, 300);
            }
        }
    };
    private MmsEmuiActionBar mActionBar;
    private RecipientListAdapter mAdapter;
    String[] mAddresseses;
    private Handler mHandler = new Handler();
    private RcsRecipientListFragment mHwCustRecipientListFragment = null;
    MenuEx mMenuEx;
    private long mThreadId;
    private boolean mTooOftenOnUpdate = false;
    private Runnable mUpdateContactRunner = new Runnable() {
        public void run() {
            if (RecipientListFragment.this.getListView() != null) {
                RecipientListFragment.this.getListView().invalidateViews();
            }
        }
    };
    private ContentObserver sPresenceObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfUpdate) {
            RecipientListFragment.this.updateContactAndFresh();
        }
    };

    private class MenuEx extends EmuiMenu {
        public MenuEx() {
            super(null);
        }

        private MenuEx setOptionMenu(Menu menu) {
            this.mOptionMenu = menu;
            return this;
        }

        public boolean onPrepareOptionsMenu() {
            boolean isInLandscape = RecipientListFragment.this.isInLandscape();
            if (OsUtil.isAtLeastL() && OsUtil.isSecondaryUser()) {
                MLog.d("RecipientListFragment", "Settings not support in secondary user");
            } else {
                addMenuSetting(isInLandscape);
            }
            return true;
        }

        private boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case 16908332:
                    RecipientListFragment.this.finishSelf(false);
                    break;
                case 278925317:
                    RecipientListFragment.this.startActivity(new Intent(RecipientListFragment.this.getContext(), MessagingPreferenceActivity.class));
                    break;
            }
            return true;
        }
    }

    private static class RecipientListAdapter extends ArrayAdapter<Contact> {
        private AvatarCache mAvatarCache = AvatarCache.newCache(true);
        private final LayoutInflater mInflater;
        private final int mResourceId;

        public RecipientListAdapter(Context context, int resource, ContactList recipients) {
            super(context, resource, recipients);
            this.mResourceId = resource;
            this.mInflater = LayoutInflater.from(context);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = this.mInflater.inflate(this.mResourceId, null);
            if (view instanceof RecipientListItem) {
                RecipientListItem listItemView = (RecipientListItem) view;
                TextView nameView = (TextView) listItemView.findViewById(R.id.name);
                TextView numberView = (TextView) listItemView.findViewById(R.id.number);
                Contact contact = (Contact) getItem(position);
                String name = contact.getName();
                String number = contact.getNumber();
                if (name.equals(number)) {
                    nameView.setText(number);
                    numberView.setVisibility(8);
                } else {
                    nameView.setText(name);
                    numberView.setText(number);
                    numberView.setVisibility(0);
                }
                listItemView.updateAvatarIcon(number);
                nameView.setTextSize(MmsConfig.isExtraHugeEnabled(getContext().getResources().getConfiguration().fontScale) ? 21.5f : 15.0f);
                if (RecipientListFragment.mHwCustEcidLookup != null && RecipientListFragment.mHwCustEcidLookup.getNameIdFeatureEnable() && contact.getPersonId() == 0) {
                    String lName = RecipientListFragment.mHwCustEcidLookup.getEcidName(getContext().getContentResolver(), number, name);
                    nameView.setText(lName);
                    if (!lName.equals(number)) {
                        numberView.setText(number);
                        numberView.setVisibility(0);
                    }
                }
                return listItemView;
            }
            MLog.e("RecipientListFragment", "Unexpected bound view: " + view);
            return view;
        }
    }

    public void updateContactAndFresh() {
        this.mHandler.removeCallbacks(this.mUpdateContactRunner);
        this.mHandler.postDelayed(this.mUpdateContactRunner, 500);
        if (!this.mTooOftenOnUpdate) {
            this.mTooOftenOnUpdate = true;
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    if (!(RecipientListFragment.this.mAdapter == null || RecipientListFragment.this.mAdapter.mAvatarCache == null)) {
                        RecipientListFragment.this.mAdapter.mAvatarCache.clearCache();
                    }
                    ContactList<Contact> contacts = RecipientListFragment.this.getContactList();
                    if (contacts == null || contacts.size() < 1) {
                        RecipientListFragment.this.finishSelf(false);
                        return;
                    }
                    for (Contact c : contacts) {
                        c.reload();
                    }
                    Adapter adp = RecipientListFragment.this.getListView().getAdapter();
                    if (adp instanceof ArrayAdapter) {
                        ((ArrayAdapter) adp).notifyDataSetInvalidated();
                    }
                    RecipientListFragment.this.mTooOftenOnUpdate = false;
                }
            }, 300);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.recipient_list_screen, container, false);
        this.mActionBar = createEmuiActionBar(root);
        this.mActionBar.setStartIcon(true, (int) R.drawable.ic_public_back, (OnClickListener) this);
        return root;
    }

    public void onActivityCreated(Bundle icicle) {
        super.onActivityCreated(icicle);
        this.mMenuEx = new MenuEx();
        this.mMenuEx.setContext(getContext());
        if (RcsCommonConfig.isRCSSwitchOn() && this.mHwCustRecipientListFragment == null) {
            this.mHwCustRecipientListFragment = new RcsRecipientListFragment(getContext());
        }
        if (icicle != null) {
            this.mThreadId = icicle.getLong("thread_id", -1);
            this.mAddresseses = icicle.getStringArray("recipients");
        } else if (!(getIntent() == null || getIntent().getExtras() == null)) {
            this.mThreadId = getIntent().getLongExtra("thread_id", -1);
            if (getIntent().getExtras().getStringArray("recipients") != null) {
                this.mAddresseses = getIntent().getExtras().getStringArray("recipients");
            }
        }
        ContactList contacts = getContactList();
        if (contacts == null || contacts.size() == 0) {
            MLog.w("RecipientListFragment", "No contacts exists. Finishing... >> " + this.mThreadId);
            finishSelf(false);
            return;
        }
        this.mActionBar.setTitle(getResources().getString(R.string.check_recipient_list_activity), contacts.size());
        this.mAdapter = new RecipientListAdapter(getContext(), R.layout.recipient_list_item, contacts);
        getListView().setAdapter(this.mAdapter);
        getListView().setDivider(null);
        PrivacyStateListener.self().register(this.localPrivacyMonitor);
        getContext().getContentResolver().registerContentObserver(Contacts.CONTENT_URI, true, this.sPresenceObserver);
        getListView().setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                ContactList contactList = RecipientListFragment.this.getContactList();
                if (contactList == null) {
                    MLog.d("RecipientListFragment", "click contact item, but contact list is null, do nothing");
                    return;
                }
                Contact contact = (Contact) contactList.get(pos);
                if (contact == null) {
                    MLog.d("RecipientListFragment", "click contact item, but item in contact list is null, do nothing : " + pos);
                    return;
                }
                Intent intent;
                if (contact.existsInDatabase()) {
                    MLog.d("RecipientListFragment", "click exists contact item, go to contact details");
                    intent = new Intent("android.intent.action.VIEW", contact.getUri());
                    intent.setFlags(67108864);
                    RecipientListFragment.this.getActivity().startActivity(intent);
                } else if (Contact.isEmailAddress(contact.getNumber())) {
                    MLog.d("RecipientListFragment", "click not exists email contact item, do nothing");
                } else {
                    MLog.d("RecipientListFragment", "click not exists contact item, go to unknow contact details");
                    intent = new Intent();
                    intent.setClassName("com.android.contacts", "com.android.contacts.activities.ContactDetailActivity");
                    intent.putExtra("isFromRcsGroupChat", true);
                    intent.putExtra("nickName", contact.getName());
                    intent.putExtra("address", contact.getNumber());
                    intent.setFlags(67108864);
                    RecipientListFragment.this.getActivity().startActivity(intent);
                }
            }
        });
    }

    private ContactList getContactList() {
        if (this.mThreadId != -1) {
            return Conversation.get(getContext(), this.mThreadId, true).getRecipients();
        }
        if (this.mAddresseses == null) {
            Intent intent = getIntent();
            if (intent == null || intent.getExtras() == null || !intent.getExtras().containsKey("recipients")) {
                return null;
            }
            this.mAddresseses = intent.getStringArrayExtra("recipients");
        }
        if (this.mAddresseses == null || this.mAddresseses.length == 0) {
            return null;
        }
        return ContactList.getByNumbers(Arrays.asList(this.mAddresseses), false);
    }

    public void onDestroy() {
        PrivacyStateListener.self().unRegister(this.localPrivacyMonitor);
        if (this.mHandler != null) {
            this.mHandler.removeCallbacks(this.mUpdateContactRunner);
        }
        getContext().getContentResolver().unregisterContentObserver(this.sPresenceObserver);
        if (this.mMenuEx != null) {
            this.mMenuEx.clear();
        }
        super.onDestroy();
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putLong("thread_id", this.mThreadId);
        super.onSaveInstanceState(outState);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return this.mMenuEx.onOptionsItemSelected(item);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        this.mMenuEx.setOptionMenu(menu).onPrepareOptionsMenu();
    }

    public void onClick(View v) {
        getActivity().onBackPressed();
    }

    protected MmsEmuiActionBar createEmuiActionBar(View fragmentRootView) {
        return new MmsEmuiActionBar(getActivity(), fragmentRootView.findViewById(R.id.compose_message_top), null);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mActionBar.setActionBarHeight(HwMessageUtils.getSplitActionBarHeight(getContext()));
    }

    public boolean onBackPressed() {
        Activity activity = getActivity();
        if (!(activity instanceof ConversationList) || !((ConversationList) activity).isSplitState() || !((ConversationList) activity).isRightPaneOnTop()) {
            return false;
        }
        ((ConversationList) activity).backToListWhenSplit();
        return true;
    }
}
