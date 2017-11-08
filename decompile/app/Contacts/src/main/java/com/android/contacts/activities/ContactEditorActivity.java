package com.android.contacts.activities;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.contacts.ContactsActivity;
import com.android.contacts.ContactsUtils;
import com.android.contacts.ContactsUtils.PredefinedNumbers;
import com.android.contacts.GeoUtil;
import com.android.contacts.activities.ContactDetailActivity.TranslucentActivity;
import com.android.contacts.calllog.ContactInfo;
import com.android.contacts.calllog.ContactInfoHelper;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.editor.ContactEditorFragment;
import com.android.contacts.editor.ContactEditorFragment.CustAnimationListener;
import com.android.contacts.editor.ContactEditorFragment.Listener;
import com.android.contacts.editor.ContactEditorFragment.RingtoneListener;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.editor.RingtoneEditorView;
import com.android.contacts.hap.sim.SimFactory;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.DialogManager;
import com.android.contacts.util.DialogManager.DialogShowingViewActivity;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.UriUtils;
import com.android.contacts.widget.ActionBarEx;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import com.google.android.gms.R;
import com.google.android.gms.common.Scopes;
import com.huawei.cspcommon.performance.PLog;
import java.util.ArrayList;
import java.util.Map;

public class ContactEditorActivity extends ContactsActivity implements DialogShowingViewActivity, CustAnimationListener {
    private boolean isFirstShowAfterLaunched = true;
    private boolean isLaunchedByDetailActivity;
    private OnClickListener mActionBarListener = new OnClickListener() {
        public void onClick(View v) {
            int viewId = v.getId();
            if (viewId == 16908295) {
                if (!ContactEditorActivity.this.mActivityAtFront) {
                    return;
                }
                if (ContactEditorActivity.this.mNeedSave) {
                    ContactEditorActivity.this.mFragment.doSaveAction();
                    ContactEditorActivity.this.mNeedSave = false;
                    return;
                }
                ContactEditorActivity.this.mFragment.revert();
                if (ContactEditorActivity.this.mIsCamcard) {
                    StatisticalHelper.report(1158);
                }
            } else if (viewId != 16908296) {
            } else {
                if (ContactEditorActivity.this.mNeedSave) {
                    ContactEditorActivity.this.mFragment.doSaveAction();
                    ContactEditorActivity.this.mNeedSave = false;
                } else if (ContactEditorActivity.this.mFragment.hasPendingChanges()) {
                    ActionBar actionBar = ContactEditorActivity.this.getActionBar();
                    if (actionBar != null && ContactEditorActivity.this.getString(R.string.menu_newContact).equals(actionBar.getTitle())) {
                        PLog.d(12, "ContactEditorFragment new contact save click");
                    }
                    ContactEditorActivity.this.mFragment.doSaveAction();
                    if (ContactEditorActivity.this.mIsCamcard) {
                        StatisticalHelper.report(1157);
                    }
                } else {
                    ContactEditorActivity.this.mFragment.revert();
                }
            }
        }
    };
    private ActionMode mActionMode;
    private boolean mActivityAtFront = false;
    private Callback mCallback = new Callback() {
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.edit_contact_actionmode, menu);
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if (ContactEditorActivity.this.mReadOnly) {
                menu.findItem(R.id.menu_confirm).setVisible(false);
            }
            return true;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (R.id.menu_confirm == item.getItemId()) {
                if (ContactEditorActivity.this.mFragment.hasPendingChanges()) {
                    ContactEditorActivity.this.mFragment.doSaveAction();
                } else {
                    ContactEditorActivity.this.mFragment.revert();
                }
            } else if (ContactEditorActivity.this.mActivityAtFront) {
                ContactEditorActivity.this.mFragment.revert();
            }
            return true;
        }

        public void onDestroyActionMode(ActionMode mode) {
        }
    };
    private Messenger mCustAnimMessenger;
    private Dialog mCustomDialog = null;
    private DialogManager mDialogManager = new DialogManager(this);
    private boolean mFinishActivityOnSaveCompleted;
    private ContactEditorFragment mFragment;
    private final Listener mFragmentListener = new Listener() {
        public void onReverted() {
            ContactEditorActivity.this.finish();
        }

        public void onSaveFinished(Intent resultIntent) {
            if (HwLog.HWFLOW) {
                HwLog.i("ContactEditorActivity", "save contact finished");
            }
            boolean isFromAddExistContact = false;
            if (resultIntent != null) {
                resultIntent.putExtra("intent_key_is_from_dialpad", ContactEditorActivity.this.getIntent().getBooleanExtra("intent_key_is_from_dialpad", false));
                resultIntent.putExtra("intent_key_is_from_default", ContactEditorActivity.this.getIntent().getBooleanExtra("intent_key_is_from_default", false));
                resultIntent.putExtra("phone", ContactEditorActivity.this.getIntent().getStringExtra("phone"));
                if (ContactEditorActivity.this.getIntent().getBooleanExtra("intent_key_is_from_dialpad", false)) {
                    String number = ContactEditorActivity.this.getIntent().getStringExtra("phone");
                    if (number != null) {
                        ContactEditorActivity.this.updateCallLogCacheData(number);
                    }
                    if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                        resultIntent.setClass(ContactEditorActivity.this.getApplicationContext(), TranslucentActivity.class);
                    }
                }
            }
            if (resultIntent != null) {
                resultIntent.setPackage("com.android.contacts");
                isFromAddExistContact = resultIntent.getBooleanExtra("extra_add_exist_contact", false);
            }
            boolean createContact = ContactEditorActivity.this.getIntent().getBooleanExtra("isFromDetailActivityCreateContact", false);
            if (ContactEditorActivity.this.mFinishActivityOnSaveCompleted || r1) {
                ContactEditorActivity.this.setResult(resultIntent == null ? 0 : -1, resultIntent);
            } else if (resultIntent != null && createContact) {
                resultIntent.putExtra("isFromDetailActivityCreateContact", true);
                ContactEditorActivity.this.setResult(-1, resultIntent);
            } else if (resultIntent != null) {
                ContactEditorActivity.this.startActivity(resultIntent);
            }
            ContactEditorActivity.this.finish();
        }

        public void onContactSplit(Uri newLookupUri) {
            ContactEditorActivity.this.finish();
        }

        public void onContactNotFound() {
            ContactEditorActivity.this.finish();
        }

        public void onEditOtherContactRequested(Uri contactLookupUri, ArrayList<ContentValues> values) {
            Intent intent = new Intent("android.intent.action.EDIT", contactLookupUri);
            intent.setClass(ContactEditorActivity.this.getApplicationContext(), ContactEditorActivity.class);
            intent.setFlags(41943040);
            intent.putExtra("addToDefaultDirectory", "");
            if (!(values == null || values.size() == 0)) {
                intent.putParcelableArrayListExtra(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH, values);
            }
            intent.putExtra("extra_add_exist_contact", true);
            ContactEditorActivity.this.startActivity(intent);
            ContactEditorActivity.this.finish();
        }

        public void onCustomCreateContactActivityRequested(AccountWithDataSet account, Bundle intentExtras) {
            if (account != null) {
                AccountType accountType = AccountTypeManager.getInstance(ContactEditorActivity.this).getAccountType(account.type, account.dataSet);
                Intent intent = new Intent();
                intent.setClassName(accountType.syncAdapterPackageName, accountType.getCreateContactActivityClassName());
                intent.setAction("android.intent.action.INSERT");
                intent.setType("vnd.android.cursor.item/contact");
                if (intentExtras != null) {
                    intent.putExtras(intentExtras);
                }
                intent.putExtra("account_name", account.name);
                intent.putExtra("account_type", account.type);
                intent.putExtra("data_set", account.dataSet);
                intent.setFlags(41943040);
                ContactEditorActivity.this.startActivity(intent);
                ContactEditorActivity.this.finish();
            }
        }

        public void onCustomEditContactActivityRequested(AccountWithDataSet account, Uri rawContactUri, Bundle intentExtras, boolean redirect) {
            AccountType accountType = AccountTypeManager.getInstance(ContactEditorActivity.this).getAccountType(account.type, account.dataSet);
            Intent intent = new Intent();
            intent.setClassName(accountType.syncAdapterPackageName, accountType.getEditContactActivityClassName());
            intent.setAction("android.intent.action.EDIT");
            intent.setData(rawContactUri);
            if (intentExtras != null) {
                intent.putExtras(intentExtras);
            }
            if (redirect) {
                intent.setFlags(41943040);
                ContactEditorActivity.this.startActivity(intent);
                ContactEditorActivity.this.finish();
                return;
            }
            ContactEditorActivity.this.startActivity(intent);
        }
    };
    private boolean mIsAnimationOngoing = false;
    private boolean mIsCamcard;
    private boolean mIsFirstCreated = true;
    private boolean mNeedSave = false;
    private boolean mReadOnly = false;

    public void onCreate(Bundle savedState) {
        boolean z = false;
        super.onCreate(savedState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        PLog.d(0, "ContactEditorActivity onCreate");
        if (EmuiFeatureManager.isContactScreenPortrait() || CommonUtilMethods.isSimpleModeOn()) {
            setRequestedOrientation(1);
        }
        Intent intent = getIntent();
        String action = intent.getAction();
        this.mFinishActivityOnSaveCompleted = intent.getBooleanExtra("finishActivityOnSaveCompleted", false);
        this.mIsCamcard = intent.getBooleanExtra("key_from_camcard", false);
        if (this.mIsCamcard) {
            if (SimFactoryManager.isDualSim()) {
                intent.putExtra("EXCLUDE_SIM1", true);
                intent.putExtra("EXCLUDE_SIM2", true);
            } else {
                intent.putExtra("EXCLUDE_SIM", true);
            }
        }
        if ("joinCompleted".equals(action)) {
            finish();
        } else if ("saveCompleted".equals(action)) {
            finish();
        } else {
            setContentView(R.layout.contact_editor_activity);
            if (savedState != null) {
                this.mIsFirstCreated = savedState.getBoolean("first_created_flag");
            }
            this.mFragment = (ContactEditorFragment) getFragmentManager().findFragmentById(R.id.contact_editor_fragment);
            this.mFragment.setListener(this.mFragmentListener);
            Uri data = "android.intent.action.EDIT".equals(action) ? getIntent().getData() : null;
            Bundle bundle = getIntent().getExtras();
            this.mFragment.load(action, data, bundle);
            if (intent.getBooleanExtra("isFromDetailActivity", false)) {
                this.isLaunchedByDetailActivity = true;
            }
            if (intent.getBooleanExtra("PICK_CONTACT_PHOTO", false)) {
                if (SimFactoryManager.isDualSim()) {
                    this.mFragment.setExcludeBothSim(true);
                } else {
                    this.mFragment.setExcludeSim(true);
                }
            } else if (bundle == null || !bundle.containsKey(Scopes.EMAIL) || this.mIsCamcard) {
                if (SimFactoryManager.isDualSim()) {
                    if (intent.getBooleanExtra("EXCLUDE_SIM1", false)) {
                        this.mFragment.setExcludeSim1(true);
                    }
                    if (intent.getBooleanExtra("EXCLUDE_SIM2", false)) {
                        this.mFragment.setExcludeSim2(true);
                    }
                } else {
                    this.mFragment.setExcludeSim(intent.getBooleanExtra("EXCLUDE_SIM", false));
                }
            } else if (SimFactoryManager.isDualSim()) {
                SimFactory firstSimFactory = SimFactoryManager.getSimFactory("com.android.huawei.sim");
                SimFactory secondSimFactory = SimFactoryManager.getSimFactory("com.android.huawei.secondsim");
                if (firstSimFactory != null) {
                    this.mFragment.setExcludeSim1(!firstSimFactory.getSimConfig().isEmailEnabled());
                }
                if (secondSimFactory != null) {
                    ContactEditorFragment contactEditorFragment = this.mFragment;
                    if (!secondSimFactory.getSimConfig().isEmailEnabled()) {
                        z = true;
                    }
                    contactEditorFragment.setExcludeSim2(z);
                }
            } else {
                SimFactory simFactory = SimFactoryManager.getSimFactory(-1);
                if (simFactory != null) {
                    this.mFragment.setExcludeSim(!simFactory.getSimConfig().isEmailEnabled());
                }
            }
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (this.mFragment != null) {
            String action = intent.getAction();
            if ("android.intent.action.EDIT".equals(action)) {
                this.mFragment.setIntentExtras(intent.getExtras());
            } else if ("saveCompleted".equals(action)) {
                this.mFragment.onSaveCompleted(true, intent.getIntExtra("saveMode", 0), intent.getBooleanExtra("saveSucceeded", false), intent.getData(), intent.getLongExtra("listenerToken", 0));
                if (intent.getIntExtra("saveMode", 0) == 1) {
                    this.mNeedSave = true;
                }
            } else if ("joinCompleted".equals(action)) {
                this.mFragment.onJoinCompleted(intent.getData());
            }
        }
    }

    protected Dialog onCreateDialog(int id, Bundle args) {
        if (!DialogManager.isManagedId(id) || args == null) {
            HwLog.w("ContactEditorActivity", "Unknown dialog requested, id: " + id + ", args: " + args);
            return null;
        } else if (args.getInt("dialog_id") == 1) {
            this.mCustomDialog = this.mDialogManager.onCreateDialog(id, args);
            return this.mCustomDialog;
        } else {
            this.mCustomDialog = null;
            return this.mDialogManager.onCreateDialog(id, args);
        }
    }

    public void onBackPressed() {
        if (!this.mIsAnimationOngoing) {
            if (this.mNeedSave) {
                this.mFragment.doSaveAction();
            } else {
                this.mFragment.revert();
            }
            if (this.mIsCamcard) {
                StatisticalHelper.report(1158);
            }
        }
    }

    private void updateCallLogCacheData(String number) {
        String newNumber = ContactsUtils.removeDashesAndBlanks(number);
        ContentValues values = new ContentValues();
        String defaultCountryIso = GeoUtil.getCurrentCountryIso(this);
        ContactInfoHelper contactInfoHelper = new ContactInfoHelper(this, defaultCountryIso);
        Map<String, PredefinedNumbers> mSpecialNumbersMap = ContactsUtils.getPredefinedMap(this);
        if (mSpecialNumbersMap == null || !mSpecialNumbersMap.containsKey(newNumber)) {
            ContactInfo info = contactInfoHelper.lookupNumber(newNumber, defaultCountryIso);
            if (info != null) {
                values.put("name", info.name);
                values.put("numbertype", Integer.valueOf(info.type));
                values.put("numberlabel", info.label);
                values.put("lookup_uri", UriUtils.uriToString(info.lookupUri));
                values.put("matched_number", info.number);
                values.put("normalized_number", info.normalizedNumber);
                values.put("photo_id", Long.valueOf(info.photoId));
                values.put("formatted_number", info.formattedNumber);
                getContentResolver().update(QueryUtil.getCallsContentUri(), values, "number = ?", new String[]{newNumber});
            }
        }
    }

    public void onContactLoaded(boolean aIsReadOnly) {
        ActionBar actionBar = getActionBar();
        this.mReadOnly = aIsReadOnly;
        if (actionBar != null) {
            int color = ImmersionUtils.getDisPlayHwNoSplitLine();
            if (color != -999) {
                actionBar.setDisplayOptions(color);
            }
            if (aIsReadOnly) {
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setTitle(getString(R.string.read_only_contact));
                if (EmuiVersion.isSupportEmui()) {
                    ActionBarEx.setStartIcon(actionBar, true, null, this.mActionBarListener);
                    ActionBarEx.setEndIcon(actionBar, false, null, null);
                } else {
                    this.mActionMode = startActionMode(this.mCallback);
                }
            } else {
                if (EmuiVersion.isSupportEmui()) {
                    ActionBarEx.setStartIcon(actionBar, true, null, this.mActionBarListener);
                    ActionBarEx.setEndIcon(actionBar, true, null, this.mActionBarListener);
                } else {
                    this.mActionMode = startActionMode(this.mCallback);
                }
                actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.people_background));
            }
        }
    }

    public DialogManager getDialogManager() {
        return this.mDialogManager;
    }

    public void onPause() {
        super.onPause();
        if (this.mCustomDialog != null) {
            this.mCustomDialog.dismiss();
        }
        this.mActivityAtFront = false;
    }

    protected void onResume() {
        super.onResume();
        this.mActivityAtFront = true;
        this.mFragment.setActivityLeaveUserHint(false);
    }

    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (this.mFragment != null) {
            this.mFragment.setActivityLeaveUserHint(true);
        }
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus && this.mIsFirstCreated) {
            this.mIsFirstCreated = false;
            onActivityCreatedFirst();
        }
    }

    private void onActivityCreatedFirst() {
        this.mFragment.onActivityLaunchedFirst();
    }

    public void setActivityTransparent() {
        if (HwLog.HWFLOW) {
            HwLog.i("ContactEditorActivity", "setActivityTransparent");
        }
        getWindow().getDecorView().setAlpha(0.0f);
    }

    public void restoreActivityFromTransparent() {
        if (HwLog.HWFLOW) {
            HwLog.i("ContactEditorActivity", "restoreActivityFromTransparent");
        }
        View decorView = getWindow().getDecorView();
        decorView.setAlpha(1.0f);
        decorView.setBackgroundResource(R.drawable.people_background);
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("first_created_flag", this.mIsFirstCreated);
    }

    public void onBackToDetailAndFinish() {
        if (this.mCustAnimMessenger == null) {
            finish();
        } else if (this.mFragment != null) {
            this.mFragment.scrollToTopSmoothly();
        }
    }

    public void onStartDetailAndFinish() {
        finish();
    }

    public void onNotifyDetailResult(int resultCode, Intent data) {
        if (this.mCustAnimMessenger != null) {
            Message msg = Message.obtain(null, 4);
            msg.arg1 = resultCode;
            msg.obj = data;
            try {
                this.mCustAnimMessenger.send(msg);
            } catch (RemoteException e) {
                HwLog.e("ContactEditorActivity", "Faild to send MSG_EDITOR_RESULT to ContactDetailActivity");
            }
        }
    }

    public void finish() {
        super.finish();
        this.mCustAnimMessenger = null;
    }

    public void onDestroy() {
        super.onDestroy();
        this.mCustAnimMessenger = null;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (permissions != null && permissions.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (grantResults[i] != 0) {
                            try {
                                startActivity(RequestPermissionsActivityBase.createRequestPermissionIntent(permissions, getPackageName()));
                            } catch (Exception e) {
                                HwLog.e("ContactEditorActivity", "Activity not find!");
                            }
                            return;
                        }
                    }
                    if (!(this.mFragment == null || this.mFragment.getRawContactEditorView() == null)) {
                        RingtoneEditorView ringtoneEditor = this.mFragment.getRawContactEditorView().getRingtoneEditor();
                        if (ringtoneEditor != null) {
                            RingtoneListener ringtonListener = this.mFragment.getRingtoneListener(ringtoneEditor);
                            if (ringtonListener != null) {
                                ringtonListener.selectRingtone();
                                break;
                            }
                        }
                    }
                }
                break;
        }
    }
}
