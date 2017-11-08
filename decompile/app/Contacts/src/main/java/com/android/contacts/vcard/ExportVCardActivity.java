package com.android.contacts.vcard;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.provider.ContactsContract.RawContacts;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.android.contacts.activities.RequestImportVCardPermissionsActivity;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.sim.SimUtility;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.AsyncTaskExecutors;
import com.android.contacts.util.Constants;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HiCloudUtil;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.vcard.VCardService.MyBinder;
import com.google.android.gms.R;
import huawei.android.app.HwProgressDialog;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExportVCardActivity extends Activity implements ServiceConnection, OnClickListener, OnCancelListener {
    private static final boolean DEBUG = HwLog.HWDBG;
    private final IntentFilter homeFilter = new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS");
    private final BroadcastReceiver homePressReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("android.intent.action.CLOSE_SYSTEM_DIALOGS")) {
                String reason = intent.getStringExtra("reason");
                if (reason != null && reason.equals("homekey")) {
                    ExportVCardActivity.this.finish();
                }
            }
        }
    };
    private boolean isShowProgress = false;
    public CharSequence[] mAccountList;
    public List<AccountWithDataSet> mAccounts;
    private boolean mConnected;
    private int mCurrentDialogId;
    public Uri mDestinationUri;
    private String mErrorReason;
    private final Messenger mIncomingMessenger = new Messenger(new IncomingHandler());
    private boolean mIsDialogDismissed = true;
    private volatile boolean mProcessOngoing = true;
    private ExportPorcessDialog mProgressDialog;
    public ArrayList<AccountWithDataSet> mSelectedAccounts = new ArrayList();
    private VCardService mService;
    private String mTargetFileName;
    public ArrayList<AccountWithDataSet> mTempAccounts = new ArrayList();

    private class AccountDialog extends AsyncTask<Void, Void, Void> {
        private AccountDialog() {
        }

        protected Void doInBackground(Void... params) {
            AccountTypeManager accManager = AccountTypeManager.getInstance(ExportVCardActivity.this);
            CharSequence[] mTempAccountList = new CharSequence[ExportVCardActivity.this.mAccounts.size()];
            int index = 0;
            for (AccountWithDataSet acc : ExportVCardActivity.this.mAccounts) {
                if (acc != null) {
                    AccountType accType = accManager.getAccountType(acc.type, acc.dataSet);
                    if (accType == null || !accType.isExtension() || acc.hasData(ExportVCardActivity.this)) {
                        ExportVCardActivity.this.mTempAccounts.add(acc);
                        String accountType = acc.type;
                        boolean isDefaultAccount = CommonUtilMethods.isLocalDefaultAccount(accountType);
                        int hiCloudAccountState = HiCloudUtil.getHicloudAccountState(ExportVCardActivity.this);
                        int index2;
                        if ((!isDefaultAccount || hiCloudAccountState == 1) && !CommonUtilMethods.isSimAccount(accountType)) {
                            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
                            if (isDefaultAccount && hiCloudAccountState == 1) {
                                stringBuilder.append(CommonUtilMethods.getHiCloudAccountLogOnSyncStateDisplayString(ExportVCardActivity.this, HiCloudUtil.isHicloudSyncStateEnabled(ExportVCardActivity.this)));
                            } else {
                                stringBuilder.append(accManager.getAccountTypeForAccount(acc).getDisplayLabel(ExportVCardActivity.this));
                            }
                            stringBuilder.append('\n');
                            int indexToBeSpanned = stringBuilder.length();
                            if (isDefaultAccount && hiCloudAccountState == 1) {
                                stringBuilder.append(HiCloudUtil.getHiCloudAccountName());
                            } else {
                                stringBuilder.append(acc.name);
                            }
                            stringBuilder.setSpan(new RelativeSizeSpan(0.7f), indexToBeSpanned, stringBuilder.length(), 51);
                            stringBuilder.setSpan(new ForegroundColorSpan(ExportVCardActivity.this.getResources().getColor(R.color.shortcut_item_data_textcolor)), indexToBeSpanned, stringBuilder.length(), 51);
                            index2 = index + 1;
                            mTempAccountList[index] = stringBuilder;
                            index = index2;
                        } else {
                            index2 = index + 1;
                            mTempAccountList[index] = accManager.getAccountTypeForAccount(acc).getDisplayLabel(ExportVCardActivity.this).toString();
                            index = index2;
                        }
                    }
                }
            }
            ExportVCardActivity.this.mAccountList = new CharSequence[index];
            for (int i = 0; i < index; i++) {
                ExportVCardActivity.this.mAccountList[i] = mTempAccountList[i];
            }
            return null;
        }

        protected void onPostExecute(Void unused) {
            if (!ExportVCardActivity.this.isFinishing()) {
                ExportVCardActivity.this.showDialogById(R.id.account_list);
            }
        }
    }

    private class ContactsSelectedListener implements OnClickListener {
        private ContactsSelectedListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            long[] lContactIds = ExportVCardActivity.this.getContactIds();
            if (ExportVCardActivity.this.mSelectedAccounts.size() <= 0 || lContactIds == null || lContactIds.length <= 0) {
                if (lContactIds == null || lContactIds.length <= 0) {
                    Toast.makeText(ExportVCardActivity.this, ExportVCardActivity.this.getString(R.string.listTotalAllContactsZero), 0).show();
                }
                ExportVCardActivity.this.unbindAndFinish();
                ExportVCardActivity.this.finish();
                return;
            }
            if (PhoneCapabilityTester.isCMCCCustomer(ExportVCardActivity.this.getApplicationContext())) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.HAP_EXPORT_CONTACTS");
                intent.putParcelableArrayListExtra("accounts", ExportVCardActivity.this.mSelectedAccounts);
                ExportVCardActivity.this.startActivityForResult(intent, 1);
            } else {
                ExportRequest request = new ExportRequest(ExportVCardActivity.this.mDestinationUri);
                request.mIsSelectedContacts = true;
                request.selectedContactIds = lContactIds;
                synchronized (ExportVCardActivity.this) {
                    if (ExportVCardActivity.this.mService.isServiceRunning()) {
                        ExportVCardActivity.this.mService.setIncomingExportMessenger(null);
                        ExportVCardActivity.this.mService.setUpdateExportProgressDialog(false);
                        ExportVCardActivity.this.finish();
                    } else {
                        ExportVCardActivity.this.mService.setIncomingExportMessenger(ExportVCardActivity.this.mIncomingMessenger);
                        ExportVCardActivity.this.mService.setUpdateExportProgressDialog(true);
                    }
                    ExportVCardActivity.this.mService.handleExportRequest(request, new NotificationImportExportListener(ExportVCardActivity.this));
                }
            }
            StatisticalHelper.report(4015);
        }
    }

    private class ExportConfirmationListener implements OnClickListener {
        public ExportConfirmationListener(ExportVCardActivity this$0, String path) {
            this(Uri.parse("file://" + path));
        }

        public ExportConfirmationListener(Uri uri) {
            ExportVCardActivity.this.mDestinationUri = uri;
        }

        public void onClick(DialogInterface dialog, int which) {
            if (which == -1) {
                ExportVCardActivity.this.mIsDialogDismissed = false;
                if (ExportVCardActivity.DEBUG) {
                    HwLog.d("VCardExport", String.format("Try sending export request (uri: %s)", new Object[]{ExportVCardActivity.this.mDestinationUri}));
                }
                ExceptionCapture.reportScene(67);
                AccountTypeManager accManager = AccountTypeManager.getInstance(ExportVCardActivity.this);
                if (SimUtility.isSimReady(0) || SimUtility.isSimReady(1)) {
                    ExportVCardActivity.this.mAccounts = accManager.getAccounts(false);
                } else {
                    ExportVCardActivity.this.mAccounts = accManager.getAccountsExcludeSim(false);
                }
                if (ExportVCardActivity.this.mAccounts.size() > 1) {
                    new AccountDialog().executeOnExecutor(AsyncTaskExecutors.THREAD_POOL_EXECUTOR, new Void[0]);
                    return;
                }
                StatisticalHelper.report(4015);
                ExportRequest request = new ExportRequest(ExportVCardActivity.this.mDestinationUri);
                request.mIsSelectedContacts = true;
                request.selectedContactIds = ExportVCardActivity.this.getContactIds();
                synchronized (ExportVCardActivity.this) {
                    if (ExportVCardActivity.this.mService.isServiceRunning()) {
                        ExportVCardActivity.this.mService.setIncomingExportMessenger(null);
                        ExportVCardActivity.this.mService.setUpdateExportProgressDialog(false);
                        ExportVCardActivity.this.finish();
                    } else {
                        ExportVCardActivity.this.mService.setIncomingExportMessenger(ExportVCardActivity.this.mIncomingMessenger);
                        ExportVCardActivity.this.mService.setUpdateExportProgressDialog(true);
                    }
                    ExportVCardActivity.this.mService.handleExportRequest(request, new NotificationImportExportListener(ExportVCardActivity.this));
                }
            } else {
                ExportVCardActivity.this.unbindAndFinish();
                ExportVCardActivity.this.finish();
            }
        }
    }

    private class ExportPorcessDialog {
        private int mCurrent;
        private HwProgressDialog mExportProgressDialog;

        private ExportPorcessDialog() {
        }

        public void showExportProcessDialog(boolean show, int current, int total) {
            if (ExportVCardActivity.DEBUG) {
                HwLog.d("VCardExport", "showExportProcessDialog,show=" + show + ",current=" + current + ",tota=" + total);
            }
            this.mCurrent = current;
            if (show) {
                if (this.mExportProgressDialog == null) {
                    this.mExportProgressDialog = new HwProgressDialog(ExportVCardActivity.this);
                    this.mExportProgressDialog.setCancelable(false);
                    this.mExportProgressDialog.setMessage(ExportVCardActivity.this.getString(R.string.export_contacts_message));
                    this.mExportProgressDialog.setProgressStyle(1);
                    this.mExportProgressDialog.setMax(total);
                    this.mExportProgressDialog.setButton(-1, ExportVCardActivity.this.getString(R.string.hide), new OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (ExportVCardActivity.DEBUG) {
                                HwLog.d("VCardExport", "mExportProcessDialogButtonListener-BUTTON_POSITIVE");
                            }
                            synchronized (ExportVCardActivity.this) {
                                ExportVCardActivity.this.mService.setUpdateExportProgressDialog(false);
                            }
                            ExportVCardActivity.this.mProgressDialog.dismiss();
                            ExportVCardActivity.this.unbindAndFinish();
                            ExportVCardActivity.this.finish();
                        }
                    });
                    this.mExportProgressDialog.show();
                    View cancelView = this.mExportProgressDialog.getCancelButton();
                    if (cancelView != null) {
                        cancelView.setContentDescription(ExportVCardActivity.this.getString(R.string.stop));
                        cancelView.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                if (ExportVCardActivity.DEBUG) {
                                    HwLog.d("VCardExport", "mExportProcessDialogButtonListener-BUTTON_NEGATIVE");
                                }
                                ExportVCardActivity.this.showDialogById(R.id.dialog_cancel_confirmation);
                            }
                        });
                    }
                }
                this.mExportProgressDialog.setProgress(current);
                return;
            }
            if (this.mExportProgressDialog != null && this.mExportProgressDialog.isShowing()) {
                this.mExportProgressDialog.dismiss();
                this.mExportProgressDialog = null;
            }
            ExportVCardActivity.this.unbindAndFinish();
            ExportVCardActivity.this.finish();
        }

        public void showDefault() {
            if (this.mExportProgressDialog != null) {
                this.mExportProgressDialog.show();
                this.mExportProgressDialog.setProgress(this.mCurrent);
            }
        }

        public void dismiss() {
            if (this.mExportProgressDialog != null && this.mExportProgressDialog.isShowing()) {
                this.mExportProgressDialog.dismiss();
                this.mExportProgressDialog = null;
            }
        }
    }

    private class IncomingHandler extends Handler {
        private IncomingHandler() {
        }

        public void handleMessage(Message msg) {
            if (ExportVCardActivity.DEBUG) {
                HwLog.d("VCardExport", "IncomingHandler received message.");
            }
            if (!ExportVCardActivity.this.isFinishing()) {
                switch (msg.what) {
                    case 5:
                        if (msg.arg1 == 0) {
                            if (msg.obj != null) {
                                ExportVCardActivity.this.mTargetFileName = (String) msg.obj;
                                if (!TextUtils.isEmpty(ExportVCardActivity.this.mTargetFileName)) {
                                    if (ExportVCardActivity.DEBUG) {
                                        HwLog.d("VCardExport", String.format("Target file name is set (%s). Show confirmation dialog", new Object[]{ExportVCardActivity.this.mTargetFileName}));
                                    }
                                    ExportVCardActivity.this.showDialogById(R.id.dialog_export_confirmation);
                                    break;
                                }
                                HwLog.w("VCardExport", "Destination file name coming from vCard service is empty.");
                                ExportVCardActivity.this.mErrorReason = ExportVCardActivity.this.getString(R.string.fail_reason_unknown);
                                ExportVCardActivity.this.showDialogById(R.id.dialog_fail_to_export_with_reason);
                                break;
                            }
                            HwLog.w("VCardExport", "Message returned from vCard server doesn't contain valid path");
                            ExportVCardActivity.this.mErrorReason = ExportVCardActivity.this.getString(R.string.fail_reason_unknown);
                            ExportVCardActivity.this.showDialogById(R.id.dialog_fail_to_export_with_reason);
                            break;
                        }
                        HwLog.i("VCardExport", "Message returned from vCard server contains error code.");
                        if (msg.obj != null) {
                            ExportVCardActivity.this.mErrorReason = (String) msg.obj;
                        }
                        ExportVCardActivity.this.showDialogById(msg.arg1);
                        return;
                    case 6:
                        if (ExportVCardActivity.DEBUG) {
                            HwLog.d("VCardExport", "IncomingHandler-handleMessage:MSG_SHOW_EXPORT_PROGRESS_DIALOG:arg1:" + msg.arg1 + ",arg2:" + msg.arg2 + ",show:" + ((Boolean) msg.obj).booleanValue());
                        }
                        ExportVCardActivity.this.isShowProgress = ((Boolean) msg.obj).booleanValue();
                        ExportVCardActivity.this.mProgressDialog.showExportProcessDialog(((Boolean) msg.obj).booleanValue(), msg.arg1, msg.arg2);
                        break;
                    default:
                        HwLog.w("VCardExport", "Unknown message type: " + msg.what);
                        super.handleMessage(msg);
                        break;
                }
            }
        }
    }

    private class SelectAccountConfirmationListener implements OnMultiChoiceClickListener {
        private SelectAccountConfirmationListener() {
        }

        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
            if (which < ExportVCardActivity.this.mTempAccounts.size()) {
                if (isChecked) {
                    ExportVCardActivity.this.mSelectedAccounts.add((AccountWithDataSet) ExportVCardActivity.this.mTempAccounts.get(which));
                } else {
                    ExportVCardActivity.this.mSelectedAccounts.remove(ExportVCardActivity.this.mTempAccounts.get(which));
                }
            }
        }
    }

    private long[] getContactIds() {
        int i;
        String[] strArr;
        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList();
        long[] jArr = null;
        for (i = 0; i < this.mSelectedAccounts.size(); i++) {
            if (i > 0) {
                selection.append(" OR ");
            }
            selection.append("(");
            selection.append("account_type").append("=?");
            selectionArgs.add(((AccountWithDataSet) this.mSelectedAccounts.get(i)).type);
            selection.append(" AND ").append("account_name").append("=?");
            selectionArgs.add(((AccountWithDataSet) this.mSelectedAccounts.get(i)).name);
            if (((AccountWithDataSet) this.mSelectedAccounts.get(i)).dataSet != null) {
                selection.append(" AND ").append("data_set").append("=?");
                selectionArgs.add(((AccountWithDataSet) this.mSelectedAccounts.get(i)).dataSet);
            } else {
                selection.append(" AND ").append("data_set").append(" is null");
            }
            selection.append(")");
        }
        ContentResolver contentResolver = getContentResolver();
        Uri uri = RawContacts.CONTENT_URI;
        String[] strArr2 = new String[]{"contact_id"};
        String stringBuilder = selection.toString();
        if (selectionArgs.size() > 0) {
            strArr = (String[]) selectionArgs.toArray(new String[selectionArgs.size()]);
        } else {
            strArr = null;
        }
        Cursor lCoursor = contentResolver.query(uri, strArr2, stringBuilder, strArr, null);
        if (lCoursor != null) {
            try {
                if (lCoursor.moveToFirst()) {
                    jArr = new long[lCoursor.getCount()];
                    int i2 = 0;
                    while (true) {
                        i = i2 + 1;
                        jArr[i2] = lCoursor.getLong(lCoursor.getColumnIndex("contact_id"));
                        if (!lCoursor.moveToNext()) {
                            break;
                        }
                        i2 = i;
                    }
                }
                lCoursor.close();
            } catch (Throwable th) {
                lCoursor.close();
            }
        }
        return jArr;
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        registerReceiver(this.homePressReceiver, this.homeFilter, "android.permission.INJECT_EVENTS", null);
        if (RequestImportVCardPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        boolean canRead;
        if (ActivityManager.isUserAMonkey()) {
            finish();
        }
        if (bundle != null) {
            if (bundle.getBoolean("recreate")) {
                finish();
            }
            this.mTargetFileName = bundle.getString("file_path", null);
            this.mAccountList = bundle.getCharSequenceArray("account_list");
            this.mAccounts = bundle.getParcelableArrayList("string_account_list");
            this.mIsDialogDismissed = bundle.getBoolean("is_dialog_dismissed");
            this.mSelectedAccounts = bundle.getParcelableArrayList("selected_account");
            this.mTempAccounts = bundle.getParcelableArrayList("tmp_account_list");
        }
        File targetDirectory = CommonUtilMethods.getUseStroagePathsPriorSDCardDefault(getApplication());
        if (targetDirectory.exists() && targetDirectory.isDirectory()) {
            canRead = targetDirectory.canRead();
        } else {
            canRead = false;
        }
        if (!canRead && !targetDirectory.mkdirs()) {
            showDialogById(R.id.dialog_sdcard_not_found);
        } else if (getIntent().getExtras() == null) {
            HwLog.e("VCardExport", "start from an ilegal activity");
            finish();
        } else {
            String callingActivity = getIntent().getExtras().getString("CALLING_ACTIVITY");
            Intent intent = new Intent(this, VCardService.class);
            intent.putExtra("CALLING_ACTIVITY", callingActivity);
            if (startService(intent) == null) {
                HwLog.e("VCardExport", "Failed to start vCard service");
                this.mErrorReason = getString(R.string.fail_reason_unknown);
                showDialogById(R.id.dialog_fail_to_export_with_reason);
                return;
            }
            if (!bindService(intent, this, 1)) {
                HwLog.e("VCardExport", "Failed to connect to vCard service.");
                this.mErrorReason = getString(R.string.fail_reason_unknown);
                showDialogById(R.id.dialog_fail_to_export_with_reason);
            }
            this.mProgressDialog = new ExportPorcessDialog();
        }
    }

    public synchronized void onServiceConnected(ComponentName name, IBinder binder) {
        if (DEBUG) {
            HwLog.d("VCardExport", "connected to service, requesting a destination file name");
        }
        this.mConnected = true;
        this.mService = ((MyBinder) binder).getService();
        this.mService.setIncomingExportMessenger(this.mIncomingMessenger);
        if (TextUtils.isEmpty(this.mTargetFileName)) {
            this.mService.handleRequestAvailableExportDestination();
        } else if (this.mIsDialogDismissed) {
            showDialogById(R.id.dialog_export_confirmation);
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("file_path", this.mTargetFileName);
        outState.putCharSequenceArray("account_list", this.mAccountList);
        outState.putParcelableArrayList("string_account_list", (ArrayList) this.mAccounts);
        outState.putBoolean("is_dialog_dismissed", this.mIsDialogDismissed);
        outState.putParcelableArrayList("selected_account", this.mSelectedAccounts);
        outState.putParcelableArrayList("tmp_account_list", this.mTempAccounts);
        outState.putBoolean("recreate", this.isShowProgress);
    }

    public synchronized void onServiceDisconnected(ComponentName name) {
        if (DEBUG) {
            HwLog.d("VCardExport", "onServiceDisconnected()");
        }
        this.mService.setIncomingExportMessenger(null);
        this.mService = null;
        this.mConnected = false;
        if (this.mProcessOngoing) {
            HwLog.w("VCardExport", "Disconnected from service during the process ongoing.");
            this.mErrorReason = getString(R.string.fail_reason_unknown);
            showDialogById(R.id.dialog_fail_to_export_with_reason);
        }
    }

    protected Dialog onCreateDialog(int id, Bundle bundle) {
        Builder builder;
        View view;
        TextView content;
        switch (id) {
            case R.string.fail_reason_too_many_vcard:
                this.mProcessOngoing = false;
                builder = new Builder(this).setTitle(R.string.export_contacts_failed_title).setPositiveButton(R.string.contact_known_button_text, this);
                view = getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(getString(R.string.failed_reason_index, new Object[]{getString(R.string.fail_reason_too_many_vcard)}));
                builder.setView(view);
                return builder.create();
            case R.id.dialog_sdcard_not_found:
                this.mProcessOngoing = false;
                builder = new Builder(this).setTitle(R.string.no_sdcard_title).setIconAttribute(16843605).setPositiveButton(R.string.contact_known_button_text, this);
                view = getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(getString(R.string.failed_reason_index, new Object[]{getString(R.string.no_sdcard_message)}));
                builder.setView(view);
                return builder.create();
            case R.id.dialog_cancel_confirmation:
                String filename = this.mTargetFileName.substring(this.mTargetFileName.lastIndexOf(47) == -1 ? 0 : this.mTargetFileName.lastIndexOf(47) + 1);
                AlertDialog dialog = new Builder(this).setMessage(getString(R.string.cancel_export_confirmation_message, new Object[]{filename})).setPositiveButton(R.string.button_cancel_export_text, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        synchronized (ExportVCardActivity.this) {
                            ExportVCardActivity.this.mService.cancelCurrentWorking(2);
                        }
                        ExportVCardActivity.this.unbindAndFinish();
                        ExportVCardActivity.this.finish();
                    }
                }).setNegativeButton(R.string.button_continue_text, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ExportVCardActivity.this.mProgressDialog.showDefault();
                    }
                }).create();
                dialog.setMessageNotScrolling();
                return dialog;
            case R.id.dialog_export_confirmation:
                builder = new Builder(this).setTitle(R.string.confirm_export_title).setPositiveButton(R.string.export_label, new ExportConfirmationListener(this, this.mTargetFileName)).setNegativeButton(17039360, this).setOnCancelListener(this);
                view = getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                content = (TextView) view.findViewById(R.id.alert_dialog_content);
                content.setText(getString(R.string.confirm_export_message_ext, new Object[]{getDisplayPath()}));
                if (Constants.isFontSizeHugeorMore()) {
                    content.setTextSize(1, 20.0f);
                }
                builder.setView(view);
                return builder.create();
            case R.id.account_list:
                return new Builder(this).setTitle(R.string.dialog_title_export_from).setMultiChoiceItems(this.mAccountList, null, new SelectAccountConfirmationListener()).setPositiveButton(17039370, new ContactsSelectedListener()).setNegativeButton(17039360, this).setOnCancelListener(this).create();
            case R.id.dialog_fail_to_export_with_reason:
                String str;
                this.mProcessOngoing = false;
                builder = new Builder(this).setTitle(R.string.export_contacts_failed_title).setPositiveButton(R.string.contact_known_button_text, this).setOnCancelListener(this);
                view = getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                content = (TextView) view.findViewById(R.id.alert_dialog_content);
                Object[] objArr = new Object[1];
                if (this.mErrorReason != null) {
                    str = this.mErrorReason;
                } else {
                    str = getString(R.string.fail_reason_unknown);
                }
                objArr[0] = str;
                content.setText(getString(R.string.failed_reason_index, objArr));
                builder.setView(view);
                return builder.create();
            default:
                return super.onCreateDialog(id, bundle);
        }
    }

    private String getDisplayPath() {
        StringBuilder message = new StringBuilder("");
        String[] targetFilePath = this.mTargetFileName.split("/");
        if (targetFilePath == null || targetFilePath.length < 4) {
            return this.mTargetFileName;
        }
        if ("emulated".equalsIgnoreCase(targetFilePath[2])) {
            message.append(getString(R.string.internal_storage));
            message.append("/").append("‪").append(targetFilePath[targetFilePath.length - 1]).append("‬");
        } else {
            File sdCardFile = CommonUtilMethods.getExternalSDCardPath(getApplication());
            if (sdCardFile != null) {
                String[] targetFilePathSD = sdCardFile.toString().split("/");
                if (targetFilePathSD != null && targetFilePathSD.length > 2 && targetFilePath[2].contains(targetFilePathSD[2])) {
                    message.append(getString(R.string.sd_card));
                    message.append("/").append("‪").append(targetFilePath[targetFilePath.length - 1]).append("‬");
                }
            }
        }
        return message.toString();
    }

    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        View view;
        if (id == R.id.dialog_fail_to_export_with_reason) {
            view = getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
            ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(this.mErrorReason);
            ((AlertDialog) dialog).setView(view);
        } else if (id == R.id.dialog_export_confirmation) {
            view = getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
            ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(getString(R.string.confirm_export_message_ext, new Object[]{getDisplayPath()}));
            ((AlertDialog) dialog).setView(view);
        } else {
            super.onPrepareDialog(id, dialog, args);
        }
    }

    protected void onStop() {
        super.onStop();
    }

    public void onClick(DialogInterface dialog, int which) {
        if (DEBUG) {
            HwLog.d("VCardExport", "ExportVCardActivity#onClick() is called");
        }
        unbindAndFinish();
        finish();
    }

    public void onCancel(DialogInterface dialog) {
        if (DEBUG) {
            HwLog.d("VCardExport", "ExportVCardActivity#onCancel() is called");
        }
        this.mProcessOngoing = false;
        unbindAndFinish();
        finish();
    }

    public void unbindService(ServiceConnection conn) {
        this.mProcessOngoing = false;
        super.unbindService(conn);
    }

    private synchronized void unbindAndFinish() {
        if (this.mConnected) {
            unbindService(this);
            this.mConnected = false;
        }
        if (this.mService != null) {
            this.mService.setIncomingExportMessenger(null);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1) {
            HwLog.i("VCardExport", "Activity result received");
            long[] contactList = data.getLongArrayExtra("SelItemData_KeyValue");
            for (long contactId : contactList) {
                if (DEBUG) {
                    HwLog.d("VCardExport", "Contacts Selected = " + contactId);
                }
            }
            ExportRequest request = new ExportRequest(this.mDestinationUri);
            request.mIsSelectedContacts = true;
            request.selectedContactIds = contactList;
            synchronized (this) {
                if (this.mService.isServiceRunning()) {
                    this.mService.setIncomingExportMessenger(null);
                    this.mService.setUpdateExportProgressDialog(false);
                    finish();
                } else {
                    this.mService.setIncomingExportMessenger(this.mIncomingMessenger);
                    this.mService.setUpdateExportProgressDialog(true);
                }
                this.mService.handleExportRequest(request, new NotificationImportExportListener(this));
            }
            return;
        }
        unbindAndFinish();
        finish();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mProgressDialog != null) {
            this.mProgressDialog.dismiss();
        }
        synchronized (this) {
            if (this.mService != null) {
                this.mService.setUpdateExportProgressDialog(false);
            }
        }
        unbindAndFinish();
        if (this.homePressReceiver != null) {
            unregisterReceiver(this.homePressReceiver);
        }
    }

    protected void onPause() {
        super.onPause();
        if (this.mProgressDialog != null) {
            this.mProgressDialog.dismiss();
        }
    }

    protected void onResume() {
        super.onResume();
        if (this.mProgressDialog != null) {
            this.mProgressDialog.showDefault();
        }
    }

    protected void onRestart() {
        super.onRestart();
        if (this.mProgressDialog != null) {
            this.mProgressDialog.showDefault();
        }
    }

    private void showDialogById(int resId) {
        this.mCurrentDialogId = resId;
        showDialog(this.mCurrentDialogId);
    }

    public void finish() {
        removeDialog(this.mCurrentDialogId);
        super.finish();
        overridePendingTransition(0, 0);
    }
}
