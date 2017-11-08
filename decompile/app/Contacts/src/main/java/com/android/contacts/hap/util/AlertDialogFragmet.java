package com.android.contacts.hap.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.activities.ShowOrCreateActivity;
import com.android.contacts.detail.ContactDetailHelper;
import com.android.contacts.detail.ContactDetailHelper.ContactRoamingInfo;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;

public class AlertDialogFragmet extends DialogFragment {
    private static Uri mContactUri;
    private static HashMap<String, Uri> mNmberUri;
    private static List<String> mNormalizedNumberList;
    private static List<String> mNumberList;
    private Button aPositiveButton;
    public boolean isCancelButtonRequired = false;
    private Activity mActivity;
    public int mAlertDialogType = 0;
    private int mCancelButtonResId;
    private int mCheckMessageId;
    private ClickListener mClickListener = new ClickListener();
    public DialogClickListener mDelListener;
    public OnDialogOptionSelectListener mEarseContactsListener;
    private boolean mHasRecord = false;
    private int mIconId = -1;
    private boolean mIsCannotCopy;
    private boolean mIsContactsOrGrpsAllSel;
    private boolean mIsDetailDelCallLog = false;
    private boolean mIsMulDelContactsOrGrps;
    private boolean mIsShareProfile;
    private boolean mIsShortCut;
    private boolean mIsVolCalllogChecked = false;
    public OnDialogOptionSelectListener mListener;
    public String mMessage;
    public int mMessageId;
    private OnClickListener mOnClickListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            if (AlertDialogFragmet.this.mSetedProfileListener != null) {
                AlertDialogFragmet.this.mSetedProfileListener.onClick(dialog, which);
            } else {
                HwLog.e("AlertDialogFragmet", "mSetedProfileListener == null");
            }
        }
    };
    private int mPosition;
    private int mResArray;
    private CharSequence[] mResArrayMessages;
    private int mSelContactsOrGrpsCount;
    private BitSet mSelectItems;
    public OnClickListener mSetedProfileListener;
    public int mTitleResId;

    public interface OnDialogOptionSelectListener extends Parcelable {
        void onDialogOptionSelected(int i, Context context);
    }

    public interface DialogClickListener {
        void onClick(DialogInterface dialogInterface, int i, boolean z, int i2);
    }

    private class ClickListener implements OnClickListener {
        private ClickListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            if (AlertDialogFragmet.this.mIsDetailDelCallLog) {
                if (AlertDialogFragmet.this.mDelListener != null) {
                    AlertDialogFragmet.this.mDelListener.onClick(dialog, which, AlertDialogFragmet.this.mIsVolCalllogChecked, AlertDialogFragmet.this.mPosition);
                }
            } else if (2 == AlertDialogFragmet.this.mAlertDialogType) {
                if (AlertDialogFragmet.this.mEarseContactsListener != null) {
                    AlertDialogFragmet.this.mEarseContactsListener.onDialogOptionSelected(which, AlertDialogFragmet.this.getActivity());
                }
            } else if (AlertDialogFragmet.this.mListener != null) {
                AlertDialogFragmet.this.mListener.onDialogOptionSelected(which, AlertDialogFragmet.this.getActivity());
            } else if (AlertDialogFragmet.this.getTargetFragment() != null) {
                AlertDialogFragmet.this.getTargetFragment().onActivityResult(AlertDialogFragmet.this.getTargetRequestCode(), which, null);
            }
        }
    }

    private class MyShortcutAdapter extends BaseAdapter {

        private class ViewHolder {
            RadioButton shortcutRadioButton;
            TextView shotcutDataTextView;
            TextView shotcutTypeTextView;

            private ViewHolder() {
            }
        }

        private MyShortcutAdapter() {
        }

        public int getCount() {
            return AlertDialogFragmet.this.getShortCutCount();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            View result;
            ViewHolder viewHolder;
            if (convertView == null) {
                result = View.inflate(AlertDialogFragmet.this.mActivity, R.layout.shortcut_item, null);
                viewHolder = new ViewHolder();
                viewHolder.shotcutTypeTextView = (TextView) result.findViewById(R.id.shortcut_type);
                viewHolder.shotcutDataTextView = (TextView) result.findViewById(R.id.shortcut_data);
                viewHolder.shortcutRadioButton = (RadioButton) result.findViewById(R.id.shortcut_radioButton);
                result.setTag(viewHolder);
            } else {
                result = convertView;
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if (AlertDialogFragmet.mNumberList == null || AlertDialogFragmet.mNumberList.size() == 0) {
                HwLog.e("AlertDialogFragmet", "mNumberList is null ");
            } else if (AlertDialogFragmet.this.isDialPos(position, AlertDialogFragmet.mNumberList.size())) {
                viewHolder.shotcutDataTextView.setText((CharSequence) AlertDialogFragmet.mNumberList.get(position - 1));
                viewHolder.shotcutTypeTextView.setText(R.string.dial_number);
                viewHolder.shotcutDataTextView.setVisibility(0);
            } else if (AlertDialogFragmet.this.isMmsPos(position, AlertDialogFragmet.mNumberList.size())) {
                viewHolder.shotcutDataTextView.setText((CharSequence) AlertDialogFragmet.mNumberList.get(AlertDialogFragmet.this.adjustMmsPos(position - 1)));
                viewHolder.shotcutTypeTextView.setText(R.string.send_message);
                viewHolder.shotcutDataTextView.setVisibility(0);
            } else if (position == 0) {
                int marginSize = AlertDialogFragmet.this.getResources().getDimensionPixelOffset(R.dimen.list_padding_left);
                LayoutParams params = new LayoutParams(-1, -2);
                params.setMargins(marginSize, marginSize, marginSize, marginSize);
                viewHolder.shotcutTypeTextView.setLayoutParams(params);
                viewHolder.shotcutDataTextView.setVisibility(8);
                viewHolder.shotcutTypeTextView.setText(R.string.menu_viewContact);
            }
            viewHolder.shortcutRadioButton.setChecked(AlertDialogFragmet.this.mSelectItems.get(position));
            result.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    boolean select = !AlertDialogFragmet.this.mSelectItems.get(position);
                    AlertDialogFragmet.this.mSelectItems.clear();
                    AlertDialogFragmet.this.mSelectItems.set(position, select);
                    viewHolder.shortcutRadioButton.setChecked(select);
                    MyShortcutAdapter.this.notifyDataSetChanged();
                    if (AlertDialogFragmet.this.aPositiveButton != null) {
                        boolean z;
                        Button -get0 = AlertDialogFragmet.this.aPositiveButton;
                        if (AlertDialogFragmet.this.mSelectItems.isEmpty()) {
                            z = false;
                        } else {
                            z = true;
                        }
                        -get0.setEnabled(z);
                    }
                }
            });
            return result;
        }
    }

    private class ShortcutClickListener implements OnClickListener {
        private ShortcutClickListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            if (AlertDialogFragmet.this.mSelectItems.get(0)) {
                ContactDetailHelper.createLauncherShortcutWithContact(AlertDialogFragmet.this.mActivity, AlertDialogFragmet.mContactUri);
            }
            for (int i = 0; i < AlertDialogFragmet.this.getShortCutCount() - 1; i++) {
                if (AlertDialogFragmet.this.mSelectItems.get(i + 1)) {
                    String str;
                    String str2;
                    if (AlertDialogFragmet.this.isClickDialPos(i, AlertDialogFragmet.mNumberList.size())) {
                        if (AlertDialogFragmet.mNmberUri != null) {
                            ContactDetailHelper.createLauncherShortcutWithContact(AlertDialogFragmet.this.mActivity, (Uri) AlertDialogFragmet.mNmberUri.get(AlertDialogFragmet.mNumberList.get(i)), "com.android.contacts.action.CHOOSE_SUB_HUAWEI");
                        } else {
                            str = (String) AlertDialogFragmet.mNumberList.get(i);
                            if (AlertDialogFragmet.mNormalizedNumberList != null) {
                                str2 = (String) AlertDialogFragmet.mNormalizedNumberList.get(i);
                            } else {
                                str2 = null;
                            }
                            ContactDetailHelper.createLauncherShortcutWithContact(AlertDialogFragmet.this.mActivity, AlertDialogFragmet.mContactUri, "com.android.contacts.action.CHOOSE_SUB_HUAWEI", new ContactRoamingInfo(str, str2));
                        }
                    } else if (AlertDialogFragmet.mNmberUri != null) {
                        ContactDetailHelper.createLauncherShortcutWithContact(AlertDialogFragmet.this.mActivity, (Uri) AlertDialogFragmet.mNmberUri.get(AlertDialogFragmet.mNumberList.get(AlertDialogFragmet.this.adjustMmsPos(i))), "android.intent.action.SENDTO");
                    } else {
                        str = (String) AlertDialogFragmet.mNumberList.get(AlertDialogFragmet.this.adjustMmsPos(i));
                        if (AlertDialogFragmet.mNormalizedNumberList != null) {
                            str2 = (String) AlertDialogFragmet.mNormalizedNumberList.get(AlertDialogFragmet.this.adjustMmsPos(i));
                        } else {
                            str2 = null;
                        }
                        ContactDetailHelper.createLauncherShortcutWithContact(AlertDialogFragmet.this.mActivity, AlertDialogFragmet.mContactUri, "android.intent.action.SENDTO", new ContactRoamingInfo(str, str2));
                    }
                }
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.mAlertDialogType = savedInstanceState.getInt("alertDialog_type_key");
            HwLog.i("AlertDialogFragmet", "onCreate mAlertDialogType :" + this.mAlertDialogType);
        }
        super.onCreate(savedInstanceState);
    }

    public static void show(FragmentManager aFragmentManager, int aTitleResId, String aMessage, int aMessageId, boolean aIsCancelButtonRequired, OnDialogOptionSelectListener aListener, int aIconId) {
        show(aFragmentManager, aTitleResId, aMessage, aMessageId, aIsCancelButtonRequired, aListener, aIconId, 17039370, "AlertDialogFragmet");
    }

    public static void show(FragmentManager aFragmentManager, int aTitleResId, List<String> numberList, List<String> normalizedNumberList, Uri contactUri, HashMap<String, Uri> nmberUri) {
        mNumberList = numberList;
        mContactUri = contactUri;
        mNmberUri = nmberUri;
        mNormalizedNumberList = normalizedNumberList;
        show(aFragmentManager, aTitleResId, aTitleResId, true);
    }

    public static void show(FragmentManager aFragmentManager, int aTitleResId, List<String> numberList, Uri contactUri, HashMap<String, Uri> nmberUri) {
        mNumberList = numberList;
        mContactUri = contactUri;
        mNmberUri = nmberUri;
        show(aFragmentManager, aTitleResId, aTitleResId, true);
    }

    private static void show(FragmentManager aFragmentManager, int aTitleResId, int aMessageId, boolean b) {
        AlertDialogFragmet fragment = new AlertDialogFragmet();
        fragment.mTitleResId = aTitleResId;
        fragment.mMessageId = aMessageId;
        fragment.mCancelButtonResId = 17039370;
        fragment.mIsShortCut = true;
        CommonUtilMethods.showFragment(aFragmentManager, fragment, "AlertDialogFragmet");
    }

    public static void show(FragmentManager aFragmentManager, int aTitleResId, int resArray, Boolean isShareProfile, OnClickListener aListener, int alertDialogType) {
        AlertDialogFragmet fragment = new AlertDialogFragmet();
        fragment.mTitleResId = aTitleResId;
        fragment.mResArray = resArray;
        fragment.mIsShareProfile = isShareProfile.booleanValue();
        fragment.mSetedProfileListener = aListener;
        fragment.mAlertDialogType = alertDialogType;
        CommonUtilMethods.showFragment(aFragmentManager, fragment, "AlertDialogFragmet");
    }

    public static void show(FragmentManager aFragmentManager, int aTitleResId, int aMessageId, int aCancelResId, boolean aCannotCopy) {
        AlertDialogFragmet fragment = new AlertDialogFragmet();
        fragment.mTitleResId = aTitleResId;
        fragment.mMessageId = aMessageId;
        fragment.mCancelButtonResId = aCancelResId;
        fragment.mIsCannotCopy = aCannotCopy;
        fragment.isCancelButtonRequired = false;
        CommonUtilMethods.showFragment(aFragmentManager, fragment, "AlertDialogFragmet");
    }

    public static void show(FragmentManager aFragmentManager, int aTitleResId, boolean aAllSelected, int aSelectedCount, boolean aMulDelContactsOrGrps, String aMessage, int aMessageId, boolean aIsCancelButtonRequired, OnDialogOptionSelectListener aListener, int aIconId, int aCancelButtonResId) {
        AlertDialogFragmet fragment = new AlertDialogFragmet();
        fragment.mTitleResId = aTitleResId;
        fragment.mMessage = aMessage;
        fragment.mMessageId = aMessageId;
        fragment.isCancelButtonRequired = aIsCancelButtonRequired;
        fragment.mIconId = aIconId;
        fragment.mListener = aListener;
        fragment.mCancelButtonResId = aCancelButtonResId;
        fragment.mIsContactsOrGrpsAllSel = aAllSelected;
        fragment.mSelContactsOrGrpsCount = aSelectedCount;
        fragment.mIsMulDelContactsOrGrps = aMulDelContactsOrGrps;
        CommonUtilMethods.showFragment(aFragmentManager, fragment, "AlertDialogFragmet");
    }

    public static void show(FragmentManager aFragmentManager, int aTitleResId, boolean aAllSelected, int aSelectedCount, boolean aMulDelContactsOrGrps, String aMessage, int aMessageId, boolean aIsCancelButtonRequired, Fragment targetFragment, int aIconId, int aCancelButtonResId, int requestCode) {
        AlertDialogFragmet fragment = new AlertDialogFragmet();
        fragment.mTitleResId = aTitleResId;
        fragment.mMessage = aMessage;
        fragment.mMessageId = aMessageId;
        fragment.isCancelButtonRequired = aIsCancelButtonRequired;
        fragment.mIconId = aIconId;
        fragment.mCancelButtonResId = aCancelButtonResId;
        fragment.mIsContactsOrGrpsAllSel = aAllSelected;
        fragment.mSelContactsOrGrpsCount = aSelectedCount;
        fragment.mIsMulDelContactsOrGrps = aMulDelContactsOrGrps;
        fragment.setTargetFragment(targetFragment, requestCode);
        CommonUtilMethods.showFragment(aFragmentManager, fragment, "AlertDialogFragmet");
    }

    public static void show(FragmentManager aFragmentManager, int aTitleResId, String aMessage, int aMessageId, boolean aIsCancelButtonRequired, OnDialogOptionSelectListener aListener, int aIconId, int aCancelButtonResId) {
        show(aFragmentManager, aTitleResId, aMessage, aMessageId, aIsCancelButtonRequired, aListener, aIconId, aCancelButtonResId, "AlertDialogFragmet");
    }

    public static void show(FragmentManager aFragmentManager, int aTitleResId, String aMessage, int aMessageId, boolean aIsCancelButtonRequired, OnDialogOptionSelectListener aListener, int aIconId, int aCancelButtonResId, int alertDialogType) {
        show(aFragmentManager, aTitleResId, aMessage, aMessageId, aIsCancelButtonRequired, aListener, aIconId, aCancelButtonResId, "AlertDialogFragmet", alertDialogType);
    }

    public static void show(FragmentManager aFragmentManager, int aTitleResId, String aMessage, int aMessageId, boolean aIsCancelButtonRequired, OnDialogOptionSelectListener aListener, int aIconId, int aCancelButtonResId, String tag) {
        show(aFragmentManager, aTitleResId, aMessage, aMessageId, aIsCancelButtonRequired, aListener, aIconId, aCancelButtonResId, tag, 0);
    }

    public static void show(FragmentManager aFragmentManager, int aTitleResId, String aMessage, int aMessageId, boolean aIsCancelButtonRequired, OnDialogOptionSelectListener aListener, int aIconId, int aCancelButtonResId, String tag, int alertDialogType) {
        AlertDialogFragmet fragment = new AlertDialogFragmet();
        fragment.mTitleResId = aTitleResId;
        fragment.mMessage = aMessage;
        fragment.mMessageId = aMessageId;
        fragment.isCancelButtonRequired = aIsCancelButtonRequired;
        fragment.mIconId = aIconId;
        fragment.mAlertDialogType = alertDialogType;
        if (alertDialogType == 2) {
            fragment.mEarseContactsListener = aListener;
        } else {
            fragment.mListener = aListener;
        }
        if (aCancelButtonResId <= 0) {
            fragment.mCancelButtonResId = 17039370;
        } else {
            fragment.mCancelButtonResId = aCancelButtonResId;
        }
        CommonUtilMethods.showFragment(aFragmentManager, fragment, tag);
    }

    public static void show(FragmentManager aFragmentManager, int aTitleResId, String aMessage, int aMessageId, int aCheckMessage, boolean aIsDetailDelCallLog, DialogClickListener aListener, int aIconId, int aCancelButtonResId, boolean hasRecord, int position, int alertDialogType) {
        show(aFragmentManager, aTitleResId, aMessage, aMessageId, aCheckMessage, aIsDetailDelCallLog, aListener, aIconId, hasRecord, aCancelButtonResId, "AlertDialogFragmet", position, alertDialogType);
    }

    private static void show(FragmentManager aFragmentManager, int aTitleResId, String aMessage, int aMessageId, int aCheckMessage, boolean aIsDetailDelCallLog, DialogClickListener aListener, int aIconId, boolean hasRecord, int aCancelButtonResId, String tag, int position, int alertDialogType) {
        AlertDialogFragmet fragment = new AlertDialogFragmet();
        fragment.mTitleResId = aTitleResId;
        fragment.mMessage = aMessage;
        fragment.mMessageId = aMessageId;
        fragment.mIsDetailDelCallLog = aIsDetailDelCallLog;
        fragment.mDelListener = aListener;
        fragment.mIconId = aIconId;
        fragment.mHasRecord = hasRecord;
        fragment.mPosition = position;
        fragment.mCheckMessageId = aCheckMessage;
        fragment.mAlertDialogType = alertDialogType;
        if (aCancelButtonResId <= 0) {
            fragment.mCancelButtonResId = 17039370;
        } else {
            fragment.mCancelButtonResId = aCancelButtonResId;
        }
        CommonUtilMethods.showFragment(aFragmentManager, fragment, tag);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog dialog;
        if (savedInstanceState != null) {
            this.mTitleResId = savedInstanceState.getInt("title");
            this.mMessageId = savedInstanceState.getInt("message");
            try {
                this.mMessage = getString(this.mMessageId);
            } catch (NotFoundException e) {
                this.mMessage = "";
                HwLog.w("AlertDialogFragmet", "mMessageId:" + this.mMessageId);
            }
            this.isCancelButtonRequired = savedInstanceState.getBoolean("cancel_button");
            this.mIsDetailDelCallLog = savedInstanceState.getBoolean("detail_del_calllog");
            this.mHasRecord = savedInstanceState.getBoolean("has_record");
            this.mPosition = savedInstanceState.getInt("calllog_position");
            this.mCheckMessageId = savedInstanceState.getInt("check_message");
            this.mIsShortCut = savedInstanceState.getBoolean("is_shortcut");
            this.mSelectItems = (BitSet) savedInstanceState.getSerializable("selectitems");
            this.mIsShareProfile = savedInstanceState.getBoolean("is_share_profile");
            this.mResArray = savedInstanceState.getInt("resarray_id");
            this.mIsCannotCopy = savedInstanceState.getBoolean("is_cannotcopy");
            this.mIsContactsOrGrpsAllSel = savedInstanceState.getBoolean("is_contacts_grps_all_sel");
            this.mIsMulDelContactsOrGrps = savedInstanceState.getBoolean("is_mul_del_contacts_grps");
            this.mSelContactsOrGrpsCount = savedInstanceState.getInt("sel_contacts_grps_count");
            if (this.mListener == null) {
                this.mListener = (OnDialogOptionSelectListener) savedInstanceState.getParcelable("listener");
            }
            this.mIconId = savedInstanceState.getInt("icon");
            this.mCancelButtonResId = savedInstanceState.getInt("cancel_res_key");
            if (this.mEarseContactsListener == null) {
                this.mEarseContactsListener = (OnDialogOptionSelectListener) savedInstanceState.getParcelable("earse_contacts_key");
            }
            this.mAlertDialogType = savedInstanceState.getInt("alertDialog_type_key");
        }
        this.mActivity = getActivity();
        View view;
        TextView content;
        if (this.mIsDetailDelCallLog) {
            dialog = new Builder(this.mActivity).setPositiveButton(getResString(this.mCancelButtonResId), this.mClickListener).setNegativeButton(17039360, this.mClickListener).create();
            if (!isAdded()) {
                dialog.setMessage(this.mMessage);
            } else if (!this.mMessage.isEmpty() || this.mHasRecord) {
                view = View.inflate(this.mActivity, R.layout.detail_delete_record_dialog, null);
                final CheckBox checkbox = (CheckBox) view.findViewById(R.id.remindMeCheckbox);
                content = (TextView) view.findViewById(R.id.dialog_content);
                try {
                    checkbox.setText(getString(this.mCheckMessageId));
                } catch (NotFoundException e2) {
                    checkbox.setText("");
                    HwLog.w("AlertDialogFragmet", "mCheckMessageId:" + this.mCheckMessageId);
                }
                checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                        AlertDialogFragmet.this.mIsVolCalllogChecked = checkbox.isChecked();
                    }
                });
                if (this.mHasRecord) {
                    checkbox.setVisibility(0);
                } else {
                    checkbox.setVisibility(8);
                }
                content.setMovementMethod(new ScrollingMovementMethod());
                content.setText(this.mMessage);
                if (this.mMessage.isEmpty()) {
                    content.setVisibility(8);
                }
                dialog.setTitle(this.mTitleResId);
                dialog.setView(view);
            } else if (this.mMessage.isEmpty()) {
                dialog.setMessage(this.mActivity.getResources().getString(this.mTitleResId));
                dialog.setMessageNotScrolling();
            }
        } else if (this.mIsShortCut) {
            if (mNumberList == null || mNumberList.size() == 0) {
                return null;
            }
            AlertDialogFragmet alertDialogFragmet = this;
            builder = new Builder(this.mActivity).setTitle(this.mTitleResId).setNeutralButton(17039360, this.mClickListener).setPositiveButton(17039370, new ShortcutClickListener());
            view = this.mActivity.getLayoutInflater().inflate(R.layout.shortcut_list, null);
            if (isAdded()) {
                builder.setView(view);
            } else {
                builder.setMessage(this.mMessage);
            }
            if (this.mSelectItems == null) {
                this.mSelectItems = new BitSet();
                this.mSelectItems.set(0);
            }
            ListView shortcutList = (ListView) view.findViewById(R.id.shortcut_list);
            alertDialogFragmet = this;
            shortcutList.setAdapter(new MyShortcutAdapter());
            shortcutList.setFastScrollEnabled(true);
            dialog = builder.create();
        } else if (this.mIsShareProfile) {
            try {
                this.mResArrayMessages = getResources().getTextArray(this.mResArray);
            } catch (NotFoundException e3) {
                this.mResArrayMessages = null;
                HwLog.w("AlertDialogFragmet", "mResArray:" + this.mResArray);
            }
            dialog = new Builder(this.mActivity).setTitle(this.mTitleResId).setItems(this.mResArrayMessages, this.mOnClickListener).create();
        } else if (this.isCancelButtonRequired) {
            builder = new Builder(this.mActivity);
            String title = "";
            if (!this.mIsMulDelContactsOrGrps) {
                title = this.mActivity.getResources().getString(this.mTitleResId);
            } else if (this.mIsContactsOrGrpsAllSel) {
                title = this.mActivity.getResources().getString(this.mTitleResId);
            } else {
                title = this.mActivity.getResources().getQuantityString(this.mTitleResId, this.mSelContactsOrGrpsCount, new Object[]{Integer.valueOf(this.mSelContactsOrGrpsCount)});
            }
            builder.setPositiveButton(getResString(this.mCancelButtonResId), this.mClickListener).setNegativeButton(17039360, this.mClickListener);
            if (isAdded()) {
                if (this.mMessage.isEmpty()) {
                    builder.setMessage(title);
                } else {
                    view = this.mActivity.getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                    content = (TextView) view.findViewById(R.id.alert_dialog_content);
                    content.setMovementMethod(new ScrollingMovementMethod());
                    content.setText(this.mMessage);
                    builder.setView(view);
                    builder.setTitle(title);
                }
            } else if (this.mMessage.isEmpty()) {
                builder.setMessage(title);
            } else {
                builder.setMessage(this.mMessage);
                builder.setTitle(title);
            }
            dialog = builder.create();
            dialog.setMessageNotScrolling();
        } else {
            try {
                builder = new Builder(this.mActivity).setNeutralButton(getResString(this.mCancelButtonResId), this.mClickListener);
                if (this.mIsCannotCopy) {
                    builder.setMessage(this.mTitleResId);
                    dialog = builder.create();
                    dialog.setMessageNotScrolling();
                } else {
                    if (isAdded()) {
                        builder.setTitle(this.mTitleResId);
                        view = this.mActivity.getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                        content = (TextView) view.findViewById(R.id.alert_dialog_content);
                        content.setMovementMethod(new ScrollingMovementMethod());
                        content.setText(this.mMessage);
                        builder.setView(view);
                    } else {
                        builder.setTitle(this.mTitleResId);
                        builder.setMessage(this.mMessage);
                    }
                    dialog = builder.create();
                }
            } catch (NotFoundException e4) {
                return null;
            }
        }
        dialog.setOnShowListener(new OnShowListener() {
            public void onShow(DialogInterface adialog) {
                Activity activity = AlertDialogFragmet.this.getActivity();
                if (activity != null) {
                    AlertDialogFragmet.this.aPositiveButton = dialog.getButton(-1);
                    if (AlertDialogFragmet.this.aPositiveButton != null && (AlertDialogFragmet.this.aPositiveButton.getText().toString().equalsIgnoreCase(AlertDialogFragmet.this.getString(R.string.contact_menu_detail_reset_mark_erase_button)) || AlertDialogFragmet.this.aPositiveButton.getText().toString().equalsIgnoreCase(AlertDialogFragmet.this.getString(R.string.menu_deleteContact)) || AlertDialogFragmet.this.aPositiveButton.getText().toString().equalsIgnoreCase(AlertDialogFragmet.this.getString(R.string.remove_label)))) {
                        AlertDialogFragmet.this.aPositiveButton.setTextColor(activity.getResources().getColor(R.color.delete_text_color));
                    }
                    if (AlertDialogFragmet.this.mIsShortCut && AlertDialogFragmet.this.aPositiveButton != null) {
                        boolean z;
                        Button -get0 = AlertDialogFragmet.this.aPositiveButton;
                        if (AlertDialogFragmet.this.mSelectItems.isEmpty()) {
                            z = false;
                        } else {
                            z = true;
                        }
                        -get0.setEnabled(z);
                    }
                }
            }
        });
        if (this.mIconId > -1) {
            dialog.setIconAttribute(this.mIconId);
        }
        if (this.mActivity instanceof PeopleActivity) {
            ((PeopleActivity) this.mActivity).mGlobalDialogReference = dialog;
        }
        return dialog;
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (this.mActivity instanceof PeopleActivity) {
            ((PeopleActivity) this.mActivity).mGlobalDialogReference = null;
        }
        if (this.mActivity instanceof ShowOrCreateActivity) {
            this.mActivity.finish();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("cancel_button", this.isCancelButtonRequired);
        outState.putInt("title", this.mTitleResId);
        outState.putInt("message", this.mMessageId);
        outState.putParcelable("listener", this.mListener);
        outState.putInt("icon", this.mIconId);
        outState.putInt("cancel_res_key", this.mCancelButtonResId);
        outState.putBoolean("detail_del_calllog", this.mIsDetailDelCallLog);
        outState.putBoolean("has_record", this.mHasRecord);
        outState.putInt("calllog_position", this.mPosition);
        outState.putInt("check_message", this.mCheckMessageId);
        outState.putBoolean("is_shortcut", this.mIsShortCut);
        outState.putSerializable("selectitems", this.mSelectItems);
        outState.putBoolean("is_share_profile", this.mIsShareProfile);
        outState.putInt("resarray_id", this.mResArray);
        outState.putBoolean("is_cannotcopy", this.mIsCannotCopy);
        outState.putBoolean("is_mul_del_contacts_grps", this.mIsMulDelContactsOrGrps);
        outState.putBoolean("is_contacts_grps_all_sel", this.mIsContactsOrGrpsAllSel);
        outState.putInt("sel_contacts_grps_count", this.mSelContactsOrGrpsCount);
        outState.putParcelable("earse_contacts_key", this.mEarseContactsListener);
        outState.putInt("alertDialog_type_key", this.mAlertDialogType);
        super.onSaveInstanceState(outState);
    }

    private int getShortCutCount() {
        int size = mNumberList.size();
        int cnt = (size * 2) + 1;
        if (!EmuiFeatureManager.isSystemVoiceCapable()) {
            cnt -= size;
        }
        if (EmuiFeatureManager.isSystemSMSCapable()) {
            return cnt;
        }
        return cnt - size;
    }

    private int adjustMmsPos(int pos) {
        int numPos = pos - mNumberList.size();
        if (EmuiFeatureManager.isSystemVoiceCapable()) {
            return numPos;
        }
        return pos;
    }

    private boolean isClickDialPos(int position, int size) {
        boolean z = false;
        if (!EmuiFeatureManager.isSystemVoiceCapable()) {
            return false;
        }
        if (position < size) {
            z = true;
        }
        return z;
    }

    private boolean isDialPos(int position, int size) {
        boolean z = false;
        if (!EmuiFeatureManager.isSystemVoiceCapable()) {
            return false;
        }
        if (position != 0 && size >= position) {
            z = true;
        }
        return z;
    }

    private boolean isMmsPos(int position, int size) {
        boolean z = true;
        boolean z2 = false;
        boolean isVoice = EmuiFeatureManager.isSystemVoiceCapable();
        boolean isMms = EmuiFeatureManager.isSystemSMSCapable();
        if (isVoice && isMms) {
            if (size >= position) {
                z = false;
            }
            return z;
        } else if (!isMms || isVoice) {
            return false;
        } else {
            if (position != 0 && size >= position) {
                z2 = true;
            }
            return z2;
        }
    }

    private String getResString(int resid) {
        String lMsg = "";
        try {
            lMsg = getString(resid);
        } catch (NotFoundException e) {
            HwLog.w("AlertDialogFragmet", "resid:" + resid);
        }
        return lMsg;
    }
}
