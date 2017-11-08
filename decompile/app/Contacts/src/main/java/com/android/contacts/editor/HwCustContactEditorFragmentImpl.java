package com.android.contacts.editor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.RawContacts;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.contacts.hap.CommonConstants;
import com.android.contacts.hap.HwCustCommonConstants;
import com.android.contacts.hap.numbermark.YellowPageContactUtil;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.RawContactDeltaList;
import com.android.contacts.model.ValuesDelta;
import com.android.contacts.util.HwCustContactFeatureUtils;
import com.google.android.gms.R;
import com.huawei.android.provider.SettingsEx.Systemex;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HwCustContactEditorFragmentImpl extends HwCustContactEditorFragment {
    private static final Uri DATA_URI = Uri.parse("content://com.android.contacts/data");
    private static final int DUPLICATE_NAME = 1;
    private static final int DUPLICATE_NUMBER = 2;
    private static final int NO_DUPLICATE = 0;
    private static final int PROGRESS_ALERT_DIALOG_HEIGHT = 180;
    private static final int PROGRESS_ALERT_DIALOG_WIDTH = 180;
    private static final Uri RAW_CONTACTS_URI = Uri.parse("content://com.android.contacts/raw_contacts");
    private static final int SPRINT_SPECIAL_NUMBER = 4;
    private static ContactEditorFragment contactEditorFragment;
    private static int mAllertMessage = R.string.str_same_number_exist;
    String lKey = "";
    private Activity mActivity;
    private boolean mIsValidationProgressVisible = false;
    private RawContactDeltaList mState;
    private Handler mValidateDuplicateHandler = new Handler();
    private ValidateDuplicateTask mValidateDuplicateTask;
    private AlertDialog mValidateProgressDialog = null;
    private boolean mValidateTaskCanStart = true;
    private Object mValidationDialogLock = new Object();

    public static class DuplicateContactDialog extends DialogFragment {
        static AlertDialog dialog;
        static int mSaveMode;

        public static void show(ContactEditorFragment fragment, int saveMode) {
            mSaveMode = saveMode;
            DuplicateContactDialog mDuplicateContactDialog = new DuplicateContactDialog();
            mDuplicateContactDialog.setTargetFragment(fragment, 0);
            mDuplicateContactDialog.show(fragment.getFragmentManager(), "duplicateContact");
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if (HwCustContactFeatureUtils.isSupportADCnodeFeature()) {
                return createSpecialCodeDialog();
            }
            dialog = new Builder(getActivity()).setIconAttribute(17301543).setTitle(R.string.editContactDescription).setMessage(getActivity().getString(HwCustContactEditorFragmentImpl.mAllertMessage)).setPositiveButton(17039370, new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    HwCustContactEditorFragmentImpl.contactEditorFragment.save(DuplicateContactDialog.mSaveMode);
                }
            }).setNegativeButton(17039360, null).create();
            return dialog;
        }

        public void onDestroy() {
            super.onDestroy();
            HwCustContactEditorFragmentImpl.contactEditorFragment = null;
        }

        public Dialog createSpecialCodeDialog() {
            Builder builder = new Builder(getActivity());
            builder.setIconAttribute(17301543);
            builder.setTitle(R.string.editContactDescription);
            builder.setMessage(getActivity().getString(R.string.str_special_number));
            builder.setPositiveButton(17039370, null);
            return builder.create();
        }
    }

    class ValidateDuplicateTask extends AsyncTask<Void, Void, Integer> implements OnCancelListener {
        int saveMode;

        public ValidateDuplicateTask(int aSaveMode) {
            this.saveMode = aSaveMode;
        }

        protected Integer doInBackground(Void... params) {
            return Integer.valueOf(validateDuplicate(HwCustContactEditorFragmentImpl.this.mState, HwCustContactEditorFragmentImpl.this.mContext.getContentResolver()));
        }

        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (CommonConstants.LOG_DEBUG) {
                Log.v("ContactEditorFragment", "ValidateDuplicateTask onPostExecute...");
            }
            if (HwCustContactEditorFragmentImpl.contactEditorFragment.isVisible() && result != null) {
                synchronized (HwCustContactEditorFragmentImpl.this.mValidationDialogLock) {
                    HwCustContactEditorFragmentImpl.this.mIsValidationProgressVisible = false;
                    if (!(HwCustContactEditorFragmentImpl.this.mValidateProgressDialog == null || !HwCustContactEditorFragmentImpl.this.mValidateProgressDialog.isShowing() || HwCustContactEditorFragmentImpl.this.mActivity == null || HwCustContactEditorFragmentImpl.this.mActivity.isFinishing())) {
                        Log.v("ContactEditorFragment", "progress dialog of validate duplicate contact got dismissed...");
                        HwCustContactEditorFragmentImpl.this.mValidateProgressDialog.dismiss();
                    }
                }
                int returnVal = result.intValue();
                if (HwCustContactFeatureUtils.isSupportADCnodeFeature()) {
                    if (returnVal == 4) {
                        HwCustContactEditorFragmentImpl.this.showDuplicateDialog(this.saveMode);
                    } else {
                        HwCustContactEditorFragmentImpl.contactEditorFragment.save(this.saveMode);
                    }
                    HwCustContactEditorFragmentImpl.this.mValidateTaskCanStart = true;
                    return;
                }
                if (returnVal == 1) {
                    Log.v("ContactEditorFragment", "notify user there is same contact name in database");
                    HwCustContactEditorFragmentImpl.mAllertMessage = R.string.str_same_name_exist;
                    HwCustContactEditorFragmentImpl.this.showDuplicateDialog(this.saveMode);
                } else if (returnVal == 2) {
                    Log.v("ContactEditorFragment", "notify user there is same contact number in database");
                    HwCustContactEditorFragmentImpl.mAllertMessage = R.string.str_same_number_exist;
                    HwCustContactEditorFragmentImpl.this.showDuplicateDialog(this.saveMode);
                } else if (returnVal == 3) {
                    Log.v("ContactEditorFragment", "notify user there is same contact name and number in database");
                    HwCustContactEditorFragmentImpl.mAllertMessage = R.string.str_same_name_exist;
                    HwCustContactEditorFragmentImpl.this.showDuplicateDialog(this.saveMode);
                } else {
                    HwCustContactEditorFragmentImpl.contactEditorFragment.save(this.saveMode);
                }
                HwCustContactEditorFragmentImpl.this.mValidateTaskCanStart = true;
            }
        }

        private int validateDuplicate(RawContactDeltaList state, ContentResolver resolver) {
            Log.v("ContactEditorFragment", "Validate whether the name and number of contact being saving exist in the contacts database");
            int retVal = 0;
            for (RawContactDelta delta : state) {
                ValuesDelta values = delta.getValues();
                if (values == null) {
                    return retVal;
                }
                String accountType = values.getAsString("account_type");
                String accountName = values.getAsString("account_name");
                if (values.isDelete()) {
                    Log.v("ContactEditorFragment", "The contact has been deleted, we needn't to validate it");
                } else {
                    boolean isContactInsert = values.isInsert();
                    ArrayList<ValuesDelta> numberList = delta.getMimeEntries("vnd.android.cursor.item/phone_v2");
                    boolean hasDuplicateNumber = false;
                    if (numberList != null) {
                        hasDuplicateNumber = validateDuplicateNumber(numberList, accountType, accountName, values, isContactInsert, resolver);
                    }
                    if (hasDuplicateNumber) {
                        retVal |= 2;
                    }
                    if (2 == retVal) {
                        Log.v("ContactEditorFragment", "The duplicate number has existed, we need not to validate duplicate name");
                        return retVal;
                    } else if (numberList != null && isSpecialSprintNumber(numberList)) {
                        return 4;
                    } else {
                        String lStrNameWhere = getNameWhere(delta.getMimeEntries("vnd.android.cursor.item/name"), accountType, accountName, values, isContactInsert);
                        String[] PROJECTION = new String[]{"display_name"};
                        if (!(TextUtils.isEmpty(lStrNameWhere) || isCancelled())) {
                            Cursor cName = resolver.query(HwCustContactEditorFragmentImpl.RAW_CONTACTS_URI, PROJECTION, lStrNameWhere, null, null);
                            if (cName != null) {
                                if (cName.getCount() > 0) {
                                    retVal |= 1;
                                }
                                cName.close();
                            }
                        }
                    }
                }
            }
            return retVal;
        }

        public boolean isSpecialSprintNumber(ArrayList<ValuesDelta> numberList) {
            if (!HwCustContactFeatureUtils.isSupportADCnodeFeature() || numberList.isEmpty()) {
                return false;
            }
            List<String> allNumber = new ArrayList();
            for (int i = 0; i < numberList.size(); i++) {
                String number = HwCustContactEditorFragmentImpl.this.getAsStringFromAfterEx("data1", (ValuesDelta) numberList.get(i));
                if (!(" = ''".equals(number) || " is null ".equals(number))) {
                    String originalNumber = ((ValuesDelta) numberList.get(i)).getAsString("data1");
                    if (!TextUtils.isEmpty(originalNumber)) {
                        allNumber.add(originalNumber.trim());
                    }
                }
            }
            if (allNumber.isEmpty()) {
                return false;
            }
            for (String number2 : allNumber) {
                boolean isADC;
                if (YellowPageContactUtil.queryYellowPageUriForNumber(HwCustContactEditorFragmentImpl.this.mActivity, number2, false) != null) {
                    isADC = true;
                    continue;
                } else {
                    isADC = false;
                    continue;
                }
                if (isADC) {
                    return true;
                }
            }
            return false;
        }

        public void onCancel(DialogInterface dialog) {
            HwCustContactEditorFragmentImpl.this.mValidateTaskCanStart = cancel(true);
            if (CommonConstants.LOG_DEBUG) {
                Log.v("ContactEditorFragment", "validateDuplicateTask cancelled..." + HwCustContactEditorFragmentImpl.this.mValidateTaskCanStart);
            }
            HwCustContactEditorFragmentImpl.this.mValidateProgressDialog.setOnCancelListener(null);
        }

        private String getNameWhere(ArrayList<ValuesDelta> names, String accountType, String accountName, ValuesDelta values, boolean isContactInsert) {
            Log.v("ContactEditorFragment", "Get the selection for querying view_raw_contacts table to get display_name field accroding name");
            if (names == null || names.size() == 0) {
                return null;
            }
            ValuesDelta firstName = (ValuesDelta) names.get(0);
            if (firstName == null) {
                return null;
            }
            String firstNameAfter;
            StringBuilder nameWhere = new StringBuilder();
            nameWhere.append("(");
            nameWhere.append("deleted");
            nameWhere.append(" != 1");
            if (!isContactInsert) {
                Log.v("ContactEditorFragment", "The contact is being updated");
                nameWhere.append(" AND ");
                nameWhere.append("_id != ").append(values.getId());
            }
            nameWhere.append(")");
            nameWhere.append(" AND ");
            nameWhere.append("(");
            nameWhere.append("_id IN ");
            nameWhere.append("(");
            nameWhere.append("SELECT raw_contact_id FROM data WHERE ");
            nameWhere.append("mimetype_id IN ");
            nameWhere.append("(");
            nameWhere.append("SELECT _id FROM mimetypes WHERE ");
            nameWhere.append("mimetype = 'vnd.android.cursor.item/name'");
            nameWhere.append(")");
            String prefixNameAfter = HwCustContactEditorFragmentImpl.this.getAsStringFromAfterEx("data4", firstName);
            if ("com.android.huawei.sim".equals(accountType)) {
                firstNameAfter = HwCustContactEditorFragmentImpl.this.getAsStringFromAfterEx("data1", firstName);
            } else {
                firstNameAfter = HwCustContactEditorFragmentImpl.this.getAsStringFromAfterEx("data2", firstName);
            }
            String middleNameAfter = HwCustContactEditorFragmentImpl.this.getAsStringFromAfterEx("data5", firstName);
            String lastNameAfter = HwCustContactEditorFragmentImpl.this.getAsStringFromAfterEx("data3", firstName);
            String sufixNameAfter = HwCustContactEditorFragmentImpl.this.getAsStringFromAfterEx("data6", firstName);
            nameWhere.append(" AND ");
            nameWhere.append(HwCustContactEditorFragmentImpl.this.formIndividualWhere("data4", prefixNameAfter));
            nameWhere.append(" AND ");
            nameWhere.append(HwCustContactEditorFragmentImpl.this.formIndividualWhere("data2", firstNameAfter));
            nameWhere.append(" AND ");
            nameWhere.append(HwCustContactEditorFragmentImpl.this.formIndividualWhere("data5", middleNameAfter));
            nameWhere.append(" AND ");
            nameWhere.append(HwCustContactEditorFragmentImpl.this.formIndividualWhere("data3", lastNameAfter));
            nameWhere.append(" AND ");
            nameWhere.append(HwCustContactEditorFragmentImpl.this.formIndividualWhere("data6", sufixNameAfter));
            nameWhere.append("))");
            return nameWhere.toString();
        }

        private boolean validateDuplicateNumber(ArrayList<ValuesDelta> numberList, String accountType, String accountName, ValuesDelta values, boolean isContactInsert, ContentResolver resolver) {
            Log.v("ContactEditorFragment", "Start to validate duplicate of number");
            List<String> allNumber = new ArrayList();
            for (int i = 0; i < numberList.size(); i++) {
                String number = HwCustContactEditorFragmentImpl.this.getAsStringFromAfterEx("data1", (ValuesDelta) numberList.get(i));
                if (!(" = ''".equals(number) || " is null ".equals(number))) {
                    allNumber.add(PhoneNumberUtils.stripSeparators(((ValuesDelta) numberList.get(i)).getAsString("data1")));
                }
            }
            if (allNumber.size() <= 0) {
                return false;
            }
            StringBuilder numberWhere = new StringBuilder();
            numberWhere.append("(");
            numberWhere.append("mimetype = 'vnd.android.cursor.item/phone_v2'");
            numberWhere.append(")");
            numberWhere.append(" AND ");
            numberWhere.append("(");
            numberWhere.append("raw_contact_id IN ");
            numberWhere.append("(");
            numberWhere.append(" SELECT _id FROM view_raw_contacts WHERE ");
            numberWhere.append("deleted");
            numberWhere.append(" != 1");
            if (!isContactInsert) {
                Log.v("ContactEditorFragment", "The contact is being updated");
                numberWhere.append(" AND ");
                numberWhere.append("_id != ").append(values.getId());
            }
            numberWhere.append("))");
            boolean hasDuplicateNumber = false;
            String lStrNumberWhere = numberWhere.toString();
            if (CommonConstants.LOG_DEBUG) {
                Log.d("ContactEditorFragment", "lStrNumberWhere = " + lStrNumberWhere);
            }
            String[] PROJECTION = new String[]{"data1"};
            if (!(TextUtils.isEmpty(lStrNumberWhere) || isCancelled())) {
                Cursor cNumber = resolver.query(HwCustContactEditorFragmentImpl.DATA_URI, PROJECTION, lStrNumberWhere, null, null);
                if (cNumber != null) {
                    if (cNumber.getCount() > 0 && cNumber.moveToFirst()) {
                        if (!matchNumber(cNumber, allNumber)) {
                            while (cNumber.moveToNext()) {
                                if (matchNumber(cNumber, allNumber)) {
                                    hasDuplicateNumber = true;
                                    break;
                                }
                            }
                        }
                        hasDuplicateNumber = true;
                    }
                    cNumber.close();
                }
            }
            return hasDuplicateNumber;
        }

        private boolean matchNumber(Cursor cursor, List<String> allNumber) {
            Log.v("ContactEditorFragment", "Start to match number of current contact being edited with number from cursor");
            String number = cursor.getString(cursor.getColumnIndex("data1"));
            if (!TextUtils.isEmpty(number)) {
                number = PhoneNumberUtils.stripSeparators(number);
                if (number != null) {
                    for (int i = 0; i < allNumber.size(); i++) {
                        if (number.equals(allNumber.get(i))) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    public HwCustContactEditorFragmentImpl(Context context) {
        super(context);
    }

    public boolean isSupportValidateDuplicate() {
        if ("true".equals(Systemex.getString(this.mContext.getContentResolver(), "validate_contacts_name_number_duplicate"))) {
            return true;
        }
        return HwCustContactFeatureUtils.isSupportADCnodeFeature();
    }

    private String formIndividualWhere(String field, String data) {
        StringBuilder where = new StringBuilder();
        where.append("(");
        where.append(field).append(data);
        if (" = ''".equals(data)) {
            where.append(" OR ");
            where.append(field).append(" is null ");
        }
        if (" is null ".equals(data)) {
            where.append(" OR ");
            where.append(field).append(" = ''");
        }
        where.append(")");
        return where.toString();
    }

    private void showDuplicateDialog(int saveMode) {
        DuplicateContactDialog.show(contactEditorFragment, saveMode);
    }

    public boolean doValidateDuplicate(int saveMode, RawContactDeltaList state, int mStatus, int mEditing, Activity activity, ContactEditorFragment editorFragment) {
        this.mActivity = activity;
        contactEditorFragment = editorFragment;
        synchronized (this.mValidationDialogLock) {
            this.mIsValidationProgressVisible = true;
            if (this.mValidateProgressDialog == null) {
                this.mValidateProgressDialog = new Builder(activity).create();
                this.mValidateProgressDialog.setView(new ProgressBar(activity));
            }
        }
        this.mState = state;
        if (!hasValidState(this.mState) || mStatus != mEditing) {
            return false;
        }
        if (saveMode != 0 || this.mState == null || 1 != this.mState.size()) {
            return contactEditorFragment.save(saveMode);
        }
        if (this.mValidateTaskCanStart) {
            this.mValidateDuplicateTask = new ValidateDuplicateTask(saveMode);
            if (this.mValidateDuplicateTask != null) {
                this.mValidateProgressDialog.setOnCancelListener(this.mValidateDuplicateTask);
                this.mValidateDuplicateTask.execute(new Void[0]);
                this.mValidateDuplicateHandler.postDelayed(new Runnable() {
                    public void run() {
                        synchronized (HwCustContactEditorFragmentImpl.this.mValidationDialogLock) {
                            if (HwCustContactEditorFragmentImpl.this.mIsValidationProgressVisible) {
                                HwCustContactEditorFragmentImpl.this.mValidateProgressDialog.show();
                                HwCustContactEditorFragmentImpl.this.mValidateProgressDialog.getWindow().setLayout(180, 180);
                            }
                        }
                    }
                }, 400);
            }
            this.mValidateTaskCanStart = false;
        }
        return true;
    }

    public void setValue() {
        if (this.mValidateProgressDialog != null && this.mValidateProgressDialog.isShowing()) {
            this.mValidateProgressDialog.dismiss();
            this.mValidateProgressDialog.setOnCancelListener(null);
        }
        if (this.mValidateDuplicateTask != null) {
            this.mValidateDuplicateTask.cancel(true);
        }
        this.mValidateTaskCanStart = true;
    }

    private boolean hasValidState(RawContactDeltaList mState) {
        return mState != null && mState.size() > 0;
    }

    public String getAsStringFromAfterEx(String key, ValuesDelta firstName) {
        if (firstName.getAsString(key) == null) {
            return " is null ";
        }
        return " = " + DatabaseUtils.sqlEscapeString(firstName.getAsString(key));
    }

    public void setNewContactFragment(ContactEditorFragment mContactEditorFragment) {
        contactEditorFragment = mContactEditorFragment;
    }

    public void customizeOnActivityResult(int requestCode, int resultCode, Intent data, Context context, FrameLayout mContent, long mRawContactIdRequestingRingtone) {
        if (HwCustContactFeatureUtils.isVibrationPatternRequired()) {
            switch (requestCode) {
                case 1003:
                    if (resultCode == -1) {
                        String SELECTED_PATTERN = "selected_pattern";
                        String VIBRATE_TYPE_KEY = "vibrate_key";
                        if (data != null) {
                            this.lKey = data.getStringExtra(VIBRATE_TYPE_KEY);
                            BaseRawContactEditorView requestingEditor = null;
                            for (int i = 0; i < mContent.getChildCount(); i++) {
                                View childView = mContent.getChildAt(i);
                                if (childView instanceof BaseRawContactEditorView) {
                                    requestingEditor = (BaseRawContactEditorView) childView;
                                }
                            }
                            if (requestingEditor != null) {
                                TextView vibrationtype = (TextView) requestingEditor.mVibration.findViewById(R.id.data);
                                if (!(vibrationtype == null || this.lKey == null)) {
                                    vibrationtype.setText(this.lKey.replace("_", HwCustPreloadContacts.EMPTY_STRING).toUpperCase(Locale.US));
                                    break;
                                }
                            }
                        }
                    }
                    break;
            }
        }
    }

    public void addSaveIntentExtras(Intent intent) {
        if (HwCustContactFeatureUtils.isVibrationPatternRequired()) {
            intent.putExtra(HwCustCommonConstants.VIBRATION_PATTERN_KEY, this.lKey);
        }
    }

    public boolean hasCustomFeatureChange(long rawContactId) {
        if (!HwCustContactFeatureUtils.isVibrationPatternRequired()) {
            return false;
        }
        String str = null;
        Cursor cursor = null;
        if (rawContactId > 0) {
            try {
                cursor = this.mContext.getContentResolver().query(RawContacts.CONTENT_URI, new String[]{"vibration_type"}, "_id=" + rawContactId, null, null);
                if (cursor.moveToFirst()) {
                    str = cursor.getString(0);
                }
                if (!(cursor == null || cursor.isClosed())) {
                    cursor.close();
                }
            } catch (SQLiteException e) {
                Log.e("ContactEditorFragment", "Execption during query");
                if (!(cursor == null || cursor.isClosed())) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (!(cursor == null || cursor.isClosed())) {
                    cursor.close();
                }
            }
        }
        if (str != null || TextUtils.isEmpty(this.lKey)) {
            return (str == null || str.equals(this.lKey)) ? false : true;
        } else {
            return true;
        }
    }

    public RawContactDelta getBestState(RawContactDeltaList stateList, RawContactDelta bestState, long nameRawContactId) {
        if (!HwCustContactFeatureUtils.isJoinFeatureEnabled() || stateList == null || stateList.isEmpty() || nameRawContactId == -1) {
            return bestState;
        }
        for (int index = 0; index < stateList.size(); index++) {
            RawContactDelta rawContactDelta = (RawContactDelta) stateList.get(index);
            if (rawContactDelta != null && rawContactDelta.getValues() != null && rawContactDelta.getRawContactId().longValue() == nameRawContactId) {
                return rawContactDelta;
            }
        }
        return bestState;
    }
}
