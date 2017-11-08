package com.android.mms.ui.views;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.data.Contact.ContactsChangedListener;
import com.android.mms.data.ContactList;
import com.android.mms.ui.ConversationList;
import com.android.mms.ui.MatchedContactsListAdpter;
import com.android.mms.ui.MatchedContactsListAdpter.MatchedContactsChangedListener;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.views.CommonLisener.HideKeyboardTouchListener;
import com.android.mms.util.PhoneNumberFormatter;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.views.RcsComposeRecipientsView;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.cache.MmsMatchContact;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.ui.HwRecipientsEditor;
import com.huawei.mms.ui.HwRecipientsEditor.AppendWatcher;
import com.huawei.mms.ui.HwRecipientsEditor.ComposeActivityCallBack;
import com.huawei.mms.ui.HwRecipientsEditor.IHwRecipientEditorCallBack;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.HwRecipientUtils;
import com.huawei.mms.util.MatchedContactsHelper;
import com.huawei.mms.util.MccMncConfig;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.StatisticalHelper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ComposeRecipientsView implements OnClickListener, ContactsChangedListener {
    static final int CHANGE_SCROLLER_HEIGHT_DELAY_LONG = MmsConfig.getChangeScrollerHeightDelayLong();
    private static final int CHANGE_SCROLLER_HEIGHT_DELAY_SHORT = MmsConfig.getChangeScrollerHeightDelayLong();
    private static final String TAG = null;
    private ImageView mBackButton;
    private int mBeforeChangeRecipientCount;
    private RelativeLayout mCollapsedRecipientsView;
    private ComposeRecipientsViewCallBack mComposeRecipientsViewCallBack;
    RecipientsPick mContactPickWaiter;
    private Object mDialogSync = new Object();
    private IRecipientsHoler mHolder;
    private HwCustComposeRecipientsView mHwCust = ((HwCustComposeRecipientsView) HwCustUtils.createObj(HwCustComposeRecipientsView.class, new Object[0]));
    private AlertDialog mInvalidRecipentDialog = null;
    private boolean mIsContactsPicking = false;
    private boolean mIsFromLauncher = false;
    private int mLastRecipientCount;
    private Runnable mLoadRecipientsRunnable = new Runnable() {
        public void run() {
            if (ComposeRecipientsView.this.mMatchedContactsListAdpter != null) {
                ComposeRecipientsView.this.mMatchedContactsListAdpter.clearRecentContactsCache();
            }
            ComposeRecipientsView.this.startLoadRecipients();
        }
    };
    private MatchedContactsListAdpter mMatchedContactsListAdpter;
    private ListView mMatchedContactsListView;
    private RcsComposeRecipientsView mRcsComposeRecipientsView;
    OnItemClickListener mRecentContactItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View arg1, int position, long arg3) {
            MmsMatchContact reccentCon = ComposeRecipientsView.this.mMatchedContactsListAdpter.getItem(position);
            if (reccentCon != null) {
                ComposeRecipientsView.this.mRecipientsEditor.setAppendWatcher(new AppendWatcher() {
                    public void onAppendCompleted() {
                        ComposeRecipientsView.this.mRecipientsEditor.removeAppendWatcher();
                        ComposeRecipientsView.this.startLoadRecipients();
                    }
                });
                ComposeRecipientsView.this.mRecipientsEditor.addPopupRecipient(reccentCon);
                if (ComposeRecipientsView.this.mRecipientsEditor.getRecipientCount() == 0) {
                    if (ComposeRecipientsView.this.mHwCust == null || !ComposeRecipientsView.this.mHwCust.isRcsSwitchOn()) {
                        ((InputMethodManager) ComposeRecipientsView.this.mHolder.getFragment().getContext().getSystemService("input_method")).showSoftInput(ComposeRecipientsView.this.mRecipientsEditor, 1);
                    }
                    ComposeRecipientsView.this.mHolder.onRecipientsEditorFocusOut();
                }
            }
        }
    };
    private LinearLayout mRecipientEditorLayout = null;
    private View mRecipientGroupLayout = null;
    private int mRecipientGroupMaxHeight = 0;
    private ProgressDialog mRecipientPickerDialog;
    private ScrollView mRecipientRowsScroller;
    private HwRecipientsEditor mRecipientsEditor;
    private ImageView mRecipientsPicker;
    private final TextWatcher mRecipientsWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            ComposeRecipientsView.this.mBeforeChangeRecipientCount = ComposeRecipientsView.this.getRecipientCount();
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            boolean z = false;
            if (!ComposeRecipientsView.this.mIsContactsPicking) {
                ComposeRecipientsView.this.mSearchKey = MatchedContactsHelper.caculateSearchKey(s);
            }
            if (ComposeRecipientsView.this.mHwCust != null && ComposeRecipientsView.this.mHwCust.getIsTitleChangeWhenRecepientsChange()) {
                ComposeRecipientsView.this.mHwCust.checkUpdateTitle(ComposeRecipientsView.this.mRecipientsEditor, count, ComposeRecipientsView.this.mHolder);
            }
            IRecipientsHoler -get4 = ComposeRecipientsView.this.mHolder;
            if (start == 0) {
                z = true;
            }
            -get4.onRecipientTextChanged(z);
            if (ComposeRecipientsView.this.mRecipientsEditor.isFocused()) {
                ComposeRecipientsView.this.setScrollerHeightAndHint(ComposeRecipientsView.CHANGE_SCROLLER_HEIGHT_DELAY_LONG);
            }
        }

        public void afterTextChanged(Editable s) {
            if (ComposeRecipientsView.this.isVisible()) {
                ComposeRecipientsView.this.mHolder.afterRecipientTextChanged();
                if (!ComposeRecipientsView.this.mIsContactsPicking) {
                    ComposeRecipientsView.this.startLoadRecipients();
                    return;
                }
                return;
            }
            IllegalStateException e = new IllegalStateException("afterTextChanged called with invisible mRecipientsEditor");
            MLog.w(ComposeRecipientsView.TAG, "RecipientsWatcher: afterTextChanged called with invisible mRecipientsEditor");
            throw e;
        }
    };
    private LayoutParams mScrollerParams;
    private String mSearchKey = null;
    private ViewStub mTopPanelStub;
    private AlertDialog toomanyRecipientsDialog;

    public interface IRecipientsHoler {
        void afterRecipientTextChanged();

        void alertForSendMms();

        View findViewById(int i);

        int getBottomPanalMinHeight();

        HwBaseFragment getFragment();

        Handler getHandler();

        int getMultiSimModelLayoutHeight();

        int getParentHeight();

        ContactList getRecipients();

        Resources getResources();

        void hideKeyboard();

        boolean isEditOnly();

        boolean isInMultiWindowMode();

        boolean isKeyBoardOpen();

        boolean isMms();

        boolean isResumeFromStop();

        boolean isSmsEnabled();

        void onRecipientTextChanged(boolean z);

        void onRecipientsChanged();

        void onRecipientsEditorFocusIn();

        void onRecipientsEditorFocusOut();

        void setCryptoToastIsShow(boolean z);

        void showNewMessageTitle();

        void updateTitle(ContactList contactList);
    }

    public class ComposeRecipientsViewCallBack implements IHwRecipientEditorCallBack {
        public boolean isSendMms() {
            if (ComposeRecipientsView.this.mHolder != null) {
                return ComposeRecipientsView.this.mHolder.isMms();
            }
            return false;
        }
    }

    private class RecipientsPick {
        final Runnable mShowRecipientProgressRunner;

        private RecipientsPick() {
            this.mShowRecipientProgressRunner = new Runnable() {
                public void run() {
                    if (ComposeRecipientsView.this.mRecipientPickerDialog == null) {
                        HwBaseFragment fragment = ComposeRecipientsView.this.mHolder.getFragment();
                        if (fragment.isDetached()) {
                            MLog.v(ComposeRecipientsView.TAG, "processPickResult:: already finish when show pick_too_many_recipients");
                            return;
                        }
                        MLog.e(ComposeRecipientsView.TAG, "mShowRecipientProgressRunner");
                        ComposeRecipientsView.this.mRecipientPickerDialog = new ProgressDialog(fragment.getContext());
                        synchronized (ComposeRecipientsView.this.mRecipientPickerDialog) {
                            ComposeRecipientsView.this.mRecipientPickerDialog.setTitle(fragment.getContext().getText(R.string.pick_too_many_recipients));
                            ComposeRecipientsView.this.mRecipientPickerDialog.setMessage(fragment.getContext().getText(R.string.adding_recipients));
                            ComposeRecipientsView.this.mRecipientPickerDialog.setIndeterminate(true);
                            ComposeRecipientsView.this.mRecipientPickerDialog.setCancelable(false);
                            ComposeRecipientsView.this.mRecipientPickerDialog.show();
                        }
                    }
                }
            };
        }

        void showWaitDialog(int size) {
            if (size > 10) {
                ComposeRecipientsView.this.mHolder.getHandler().post(this.mShowRecipientProgressRunner);
            } else if (ComposeRecipientsView.this.mRcsComposeRecipientsView == null || !ComposeRecipientsView.this.mRcsComposeRecipientsView.isRcsSwitchOn()) {
                ComposeRecipientsView.this.mHolder.getHandler().postDelayed(this.mShowRecipientProgressRunner, 200);
            } else {
                ComposeRecipientsView.this.mRcsComposeRecipientsView.postRcsDelayed(ComposeRecipientsView.this.mHolder.getHandler(), this.mShowRecipientProgressRunner);
            }
        }

        void stopWaitDialog() {
            ComposeRecipientsView.this.mHolder.getHandler().removeCallbacks(this.mShowRecipientProgressRunner);
            synchronized (ComposeRecipientsView.this.mDialogSync) {
                if (ComposeRecipientsView.this.mRecipientPickerDialog != null) {
                    if (ComposeRecipientsView.this.mRecipientPickerDialog.isShowing()) {
                        ComposeRecipientsView.this.mRecipientPickerDialog.dismiss();
                    }
                    ComposeRecipientsView.this.mRecipientPickerDialog = null;
                }
            }
        }

        void alertInvalidRecipients(final List<String> invalideRecipients) {
            if (invalideRecipients.size() != 0) {
                ComposeRecipientsView.this.mHolder.getHandler().post(new Runnable() {
                    public void run() {
                        if (invalideRecipients.size() > 0) {
                            StringBuilder sb = new StringBuilder();
                            for (String title : invalideRecipients) {
                                sb.append(title).append(",");
                            }
                            sb.replace(sb.length() - 1, sb.length(), "");
                            ComposeRecipientsView.this.alertForInvalidRecipient(ComposeRecipientsView.this.getResources().getQuantityString(R.plurals.has_invalid_recipient, invalideRecipients.size(), new Object[]{sb.toString()}));
                        }
                    }
                });
            }
        }

        void addContact(final ContactList contactLists, final ContactList existContactLists) {
            ComposeRecipientsView.this.mHolder.getHandler().post(new Runnable() {
                public void run() {
                    if ("CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""))) {
                        contactLists.removeIPAndZeroPrefixForChina();
                    }
                    ComposeRecipientsView.this.mRecipientsEditor.addRecipients(contactLists, existContactLists);
                    ComposeRecipientsView.this.setScrollerHeightAndHint(0);
                    ((LinearLayout.LayoutParams) ComposeRecipientsView.this.mRecipientEditorLayout.getLayoutParams()).height = -2;
                    ComposeRecipientsView.this.mHolder.onRecipientsChanged();
                }
            });
        }
    }

    public ComposeRecipientsView(IRecipientsHoler mRecipientsHoler) {
        this.mHolder = mRecipientsHoler;
        if (RcsCommonConfig.isRCSSwitchOn() && this.mRcsComposeRecipientsView == null) {
            this.mRcsComposeRecipientsView = new RcsComposeRecipientsView();
        }
        this.mComposeRecipientsViewCallBack = new ComposeRecipientsViewCallBack();
    }

    private View findViewById(int id) {
        return this.mHolder.findViewById(id);
    }

    private HwBaseFragment getFragment() {
        return this.mHolder.getFragment();
    }

    private Resources getResources() {
        return this.mHolder.getResources();
    }

    public int getRecipientCount() {
        if (this.mRecipientsEditor != null) {
            int recipientCount = this.mRecipientsEditor.getRecipientCount();
            if (recipientCount != 0 || this.mRecipientsEditor.length() == 0) {
                return recipientCount;
            }
            return 1;
        } else if (this.mHolder.getRecipients() != null) {
            return this.mHolder.getRecipients().getNumbers().length;
        } else {
            return 0;
        }
    }

    public void requestFocus() {
        this.mRecipientsEditor.requestFocus();
    }

    public boolean containsEmail() {
        return this.mRecipientsEditor != null ? this.mRecipientsEditor.containsEmail() : false;
    }

    public boolean hasValidRecipient(boolean isMms) {
        return this.mRecipientsEditor.hasValidRecipient(isMms);
    }

    public boolean hasInvalidRecipient() {
        return this.mRecipientsEditor.hasInvalidRecipient(this.mHolder.isMms());
    }

    public boolean hasInvalidRecipient(boolean isMms) {
        return this.mRecipientsEditor.hasInvalidRecipient(isMms);
    }

    public boolean hasComplexInvalidRecipient() {
        return this.mRecipientsEditor.hasComplexInvalidRecipient();
    }

    public boolean showInvalidDestinationToast() {
        if (!isVisible()) {
            return false;
        }
        if (!this.mRecipientsEditor.hasInvalidRecipient(this.mHolder.isMms()) && !this.mRecipientsEditor.hasComplexInvalidRecipient()) {
            return false;
        }
        alertForInvalidRecipient(getResources().getQuantityString(R.plurals.has_invalid_recipient, this.mRecipientsEditor.getRecipientCount(), new Object[]{this.mRecipientsEditor.formatInvalidNumbers(isMms)}));
        return true;
    }

    public void clear() {
        if (this.mRecipientsEditor != null) {
            this.mRecipientsEditor.getRecipients().clear();
        }
    }

    public boolean isFocused() {
        return this.mRecipientsEditor != null ? this.mRecipientsEditor.isFocused() : false;
    }

    public boolean isInEdit() {
        return this.mRecipientsEditor != null && this.mRecipientsEditor.getVisibility() == 0;
    }

    public CharSequence getRecipientsEditorText() {
        return this.mRecipientsEditor == null ? null : this.mRecipientsEditor.getText().toString();
    }

    private int getRecipientEditorLayoutWidth() {
        if (this.mRecipientEditorLayout != null) {
            return this.mRecipientEditorLayout.getMeasuredWidth() - (((int) getResources().getDimension(R.dimen.mms_recipient_layout_padding)) * 2);
        }
        return 0;
    }

    public void initRecipientsEditor() {
        if (!isVisible()) {
            if (this.mTopPanelStub == null) {
                this.mTopPanelStub = (ViewStub) findViewById(R.id.recipients_subject_linear);
                this.mTopPanelStub.inflate();
            }
            this.mTopPanelStub.setVisibility(0);
            initRecentContactsListRefs();
            ContactList recipients = this.mHolder.getRecipients();
            if ("CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", "")) && recipients != null) {
                recipients.removeIPAndZeroPrefixForChina();
            }
            ViewStub stub = (ViewStub) findViewById(R.id.recipients_editor_stub);
            if (stub != null) {
                View stubView = stub.inflate();
                this.mRecipientsEditor = (HwRecipientsEditor) stubView.findViewById(R.id.recipients_editor);
                this.mRecipientsPicker = (ImageView) stubView.findViewById(R.id.recipients_picker);
                this.mBackButton = (ImageView) stubView.findViewById(R.id.bt_back);
                if (this.mHolder.isEditOnly()) {
                    this.mRecipientsEditor.setVisibility(0);
                }
                this.mCollapsedRecipientsView = (RelativeLayout) stubView.findViewById(R.id.collapsed_recipients_view);
            } else {
                this.mRecipientsEditor = (HwRecipientsEditor) findViewById(R.id.recipients_editor);
                this.mRecipientsEditor.setVisibility(0);
                this.mRecipientsPicker = (ImageView) findViewById(R.id.recipients_picker);
                this.mBackButton = (ImageView) findViewById(R.id.bt_back);
                this.mCollapsedRecipientsView = (RelativeLayout) findViewById(R.id.collapsed_recipients_view);
                if (this.mRecipientsPicker != null) {
                    this.mRecipientsPicker.setVisibility(0);
                } else {
                    return;
                }
            }
            this.mRecipientGroupLayout = findViewById(R.id.recipients_and_subject_layout);
            this.mRecipientsPicker.setImageDrawable(ResEx.self().getStateListDrawable(getFragment().getContext(), R.drawable.icon_top_bar_add_contact));
            this.mRecipientsEditor.setChipHeight(getResources().getDimensionPixelSize(R.dimen.chips_height));
            this.mRecipientsEditor.setHwRecipientEditorCallBack(this.mComposeRecipientsViewCallBack);
            this.mRecipientsEditor.setCollapseRecipientView(this.mCollapsedRecipientsView);
            this.mHolder.showNewMessageTitle();
            this.mRecipientsEditor.setDropDownWidth(MessageUtils.getScreenWidth(getFragment().getActivity()));
            boolean smsEnabled = this.mHolder.isSmsEnabled();
            this.mRecipientsPicker.setAlpha(MessageUtils.getImageDisplyAlpha(smsEnabled));
            this.mBackButton.setOnClickListener(this);
            if (smsEnabled) {
                this.mRecipientsPicker.setClickable(true);
                this.mRecipientsPicker.setOnClickListener(this);
            } else {
                this.mRecipientsPicker.setClickable(false);
            }
            this.mRecipientEditorLayout = (LinearLayout) findViewById(R.id.recipients_editor_layout);
            this.mRecipientRowsScroller = (ScrollView) findViewById(R.id.recipients_editor_scrollview);
            this.mRecipientEditorLayout.setVisibility(0);
            this.mRecipientRowsScroller.setVisibility(0);
            this.mRecipientsEditor.measure(0, 0);
            this.mRecipientGroupMaxHeight = ((this.mRecipientsEditor.getMeasuredHeight() - (((int) getResources().getDimension(R.dimen.recipient_vertcal_space)) * 2)) * 3) + (((int) getResources().getDimension(R.dimen.recipient_vertcal_space)) * 4);
            this.mScrollerParams = this.mRecipientRowsScroller.getLayoutParams();
            this.mRecipientEditorLayout.setBackgroundResource(R.drawable.message_new_edit_bg);
            MessageUtils.setPadding(getFragment().getContext(), this.mRecipientEditorLayout, -1, 0, -1, 0);
            this.mRecipientsEditor.removeComposeCallBack();
            this.mRecipientsEditor.setComposeCallBack(new ComposeActivityCallBack() {
                public MmsMatchContact getMatchContact() {
                    ArrayList conList = null;
                    if (ComposeRecipientsView.this.mMatchedContactsListAdpter != null) {
                        conList = ComposeRecipientsView.this.mMatchedContactsListAdpter.getData();
                    }
                    if (conList == null || conList.size() <= 0) {
                        return null;
                    }
                    return (MmsMatchContact) conList.get(0);
                }
            });
            this.mCollapsedRecipientsView.setOnClickListener(this);
            this.mRecipientsEditor.addTextChangedListener(this.mRecipientsWatcher);
            if (recipients != null) {
                this.mRecipientsEditor.addRecipients(recipients, null);
            }
            this.mRecipientsEditor.setOnSelectChipRunnable(new Runnable() {
                public void run() {
                    if (ComposeRecipientsView.this.mRecipientsEditor.getRecipientCount() == 1) {
                        ComposeRecipientsView.this.mHolder.onRecipientsEditorFocusOut();
                    }
                }
            });
            this.mRecipientsEditor.setOnFocusChangeListener(new OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    MLog.v(ComposeRecipientsView.TAG, "mRecipientsEditor:: onFocusChange......hasFocus::" + hasFocus);
                    if (hasFocus) {
                        ComposeRecipientsView.this.setScrollerHeightAndHint(ComposeRecipientsView.CHANGE_SCROLLER_HEIGHT_DELAY_SHORT);
                        if (TextUtils.isEmpty(ComposeRecipientsView.this.mRecipientsEditor.getText())) {
                            ComposeRecipientsView.this.mRecipientsEditor.setText("");
                        }
                        ComposeRecipientsView.this.mHolder.onRecipientsEditorFocusIn();
                        ComposeRecipientsView.this.mCollapsedRecipientsView.setVisibility(8);
                        ComposeRecipientsView.this.mRecipientRowsScroller.setVisibility(0);
                        ComposeRecipientsView.this.mRecipientsEditor.setSelection(ComposeRecipientsView.this.mRecipientsEditor.length());
                        ComposeRecipientsView.this.startLoadRecipients();
                        if (!HwMessageUtils.isSplitOn()) {
                            ComposeRecipientsView.this.showSoftInput(1);
                        }
                    } else {
                        ComposeRecipientsView.this.mRecipientsEditor.commitNumberChip();
                        ComposeRecipientsView.this.mHolder.onRecipientsEditorFocusOut();
                        if (ComposeRecipientsView.this.mRecipientsEditor.hasInvalidRecipient(ComposeRecipientsView.this.mHolder.isMms()) || ComposeRecipientsView.this.mRecipientsEditor.hasComplexInvalidRecipient()) {
                            ComposeRecipientsView.this.alertForInvalidRecipient(ComposeRecipientsView.this.getResources().getQuantityString(R.plurals.has_invalid_recipient, ComposeRecipientsView.this.mRecipientsEditor.getRecipientCount(), new Object[]{ComposeRecipientsView.this.mRecipientsEditor.formatInvalidNumbers(isMms)}));
                            return;
                        }
                        if (ComposeRecipientsView.this.mHwCust != null && ComposeRecipientsView.this.mHwCust.getIsTitleChangeWhenRecepientsChange()) {
                            ComposeRecipientsView.this.mHwCust.updateTitle(ComposeRecipientsView.this.mHolder, v);
                        }
                        ComposeRecipientsView.this.setScrollerHeightAndHint(ComposeRecipientsView.CHANGE_SCROLLER_HEIGHT_DELAY_SHORT);
                        ComposeRecipientsView.this.updateRecentContactList();
                        if (ComposeRecipientsView.this.mRecipientsEditor.isNeedCollapse()) {
                            ComposeRecipientsView.this.mRecipientRowsScroller.setVisibility(8);
                            ComposeRecipientsView.this.mCollapsedRecipientsView.setVisibility(0);
                            ComposeRecipientsView.this.mRecipientsEditor.updateCollapseTextView(ComposeRecipientsView.this.getRecipientEditorLayoutWidth());
                        }
                    }
                }
            });
            PhoneNumberFormatter.setPhoneNumberFormattingTextWatcher(this.mHolder.getFragment().getContext(), this.mRecipientsEditor);
            setScrollerHeightAndHint(CHANGE_SCROLLER_HEIGHT_DELAY_LONG);
            if (MmsConfig.isShowCheckEmailPoup()) {
                this.mHolder.alertForSendMms();
            }
        }
    }

    public void checkForTooManyRecipients() {
        int recipientLimit = MmsConfig.getRecipientLimit();
        if (recipientLimit != Integer.MAX_VALUE && recipientLimit > 0) {
            int recipientCount = getRecipientCount();
            boolean tooMany = recipientCount > recipientLimit;
            if (recipientCount != this.mLastRecipientCount) {
                this.mLastRecipientCount = recipientCount;
                if (tooMany && this.mBeforeChangeRecipientCount <= this.mLastRecipientCount) {
                    displayTooManyRecipientsDialog();
                }
            }
        }
    }

    public void displayTooManyRecipientsDialog() {
        int recipientCount = getRecipientCount();
        int recipientLimit = MmsConfig.getRecipientLimit();
        if (MmsConfig.getCustMmsConfig() != null) {
            recipientLimit = MmsConfig.getCustMmsConfig().getCustRecipientLimit(this.mHolder.isMms(), recipientLimit);
        }
        Context context = this.mHolder.getFragment().getContext();
        this.toomanyRecipientsDialog = new Builder(context).setTitle(R.string.pick_too_many_recipients).setIcon(17301543).setMessage(context.getResources().getQuantityString(R.plurals.too_many_recipients, recipientLimit, new Object[]{Integer.valueOf(recipientCount), Integer.valueOf(recipientLimit)})).setPositiveButton(17039370, null).create();
        this.toomanyRecipientsDialog.setCanceledOnTouchOutside(true);
        this.toomanyRecipientsDialog.show();
    }

    public void setScrollerHeightAndHint(int delay) {
        this.mHolder.getHandler().postDelayed(new Runnable() {
            public void run() {
                if (!ComposeRecipientsView.this.mHolder.getFragment().isDetached()) {
                    int viewHheight = Math.min(ComposeRecipientsView.this.mRecipientGroupMaxHeight, ComposeRecipientsView.this.mRecipientsEditor.getMeasuredHeight());
                    if (ComposeRecipientsView.this.mScrollerParams == null) {
                        ComposeRecipientsView.this.mScrollerParams = ComposeRecipientsView.this.mRecipientRowsScroller.getLayoutParams();
                    }
                    ComposeRecipientsView.this.mScrollerParams.height = viewHheight;
                    ComposeRecipientsView.this.mRecipientRowsScroller.requestLayout();
                }
            }
        }, (long) delay);
    }

    public void hideRecipientEditor() {
        if (!(this.mRecipientsEditor == null || this.mRecipientsPicker == null || this.mRecipientEditorLayout == null || this.mTopPanelStub == null)) {
            this.mRecipientsEditor.removeTextChangedListener(this.mRecipientsWatcher);
            this.mRecipientsEditor.setVisibility(8);
            this.mRecipientEditorLayout.setVisibility(8);
            this.mRecipientsPicker.setVisibility(8);
            this.mHolder.updateTitle(this.mRecipientsEditor.constructContactsFromInput(false));
            this.mTopPanelStub.setVisibility(8);
        }
        this.mBeforeChangeRecipientCount = 0;
    }

    public void showRecipientEditor() {
        if (this.mRecipientEditorLayout == null || this.mRecipientEditorLayout.getVisibility() == 8) {
            MLog.d(TAG, "ConversationUpdator initiliz RecipientEditor");
            hideRecipientEditor();
            initRecipientsEditor();
        }
    }

    public void setVisibility(boolean isShow) {
        if (this.mRecipientGroupLayout != null) {
            this.mRecipientGroupLayout.setVisibility(isShow ? 0 : 8);
        }
    }

    public boolean isVisible() {
        if (this.mRecipientEditorLayout == null || this.mRecipientEditorLayout.getVisibility() != 0) {
            return false;
        }
        return true;
    }

    public HwRecipientsEditor getRecipientsEditor() {
        return this.mRecipientsEditor;
    }

    public void setNumberFromIntent(Intent data) {
        ArrayList<Integer> dataIds = data.getIntegerArrayListExtra("SelItemData_KeyValue");
        ArrayList<Integer> callsIds = data.getIntegerArrayListExtra("SelItemCalls_KeyValue");
        if ((dataIds == null || dataIds.size() <= 0) && (callsIds == null || callsIds.size() <= 0)) {
            MLog.v(TAG, "setNumberFromIntent::get the number from intent sent by gallary");
            String[] extraStrings = data.getStringArrayExtra("android.intent.extra.PHONE_NUMBER");
            if (extraStrings != null && extraStrings.length != 0) {
                final StringBuffer contactString = new StringBuffer();
                for (String string : extraStrings) {
                    contactString.append(string + ";");
                }
                final Handler handler = new Handler();
                ThreadEx.execute(new Runnable() {
                    public String toString() {
                        return "ComoseMessageActivity.getNumberFromIntent." + super.toString();
                    }

                    public void run() {
                        final ContactList list = ContactList.getByNumbers(contactString.toString(), true, true);
                        if ("CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""))) {
                            list.removeIPAndZeroPrefixForChina();
                        }
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                if (!ComposeRecipientsView.this.mHolder.getFragment().isDetached()) {
                                    ComposeRecipientsView.this.mRecipientsEditor.addRecipients(list, null);
                                }
                            }
                        }, 300);
                    }
                });
                return;
            }
            return;
        }
        processLargeNumberRecipients(dataIds, callsIds);
    }

    private void initRecentContactsListRefs() {
        this.mMatchedContactsListView = (ListView) findViewById(R.id.match_contact_list);
        this.mMatchedContactsListView.setFastScrollEnabled(true);
        this.mMatchedContactsListAdpter = new MatchedContactsListAdpter(getFragment().getContext());
        this.mMatchedContactsListView.setAdapter(this.mMatchedContactsListAdpter);
        this.mMatchedContactsListView.setOnItemClickListener(this.mRecentContactItemClickListener);
        this.mMatchedContactsListView.setClickable(true);
        this.mMatchedContactsListView.setFocusable(false);
        this.mMatchedContactsListView.setFocusableInTouchMode(false);
        this.mMatchedContactsListView.setDescendantFocusability(393216);
        this.mMatchedContactsListAdpter.setListener(new MatchedContactsChangedListener() {
            public void onMatchedContactsChanged() {
                HwBackgroundLoader.getUIHandler().post(new Runnable() {
                    public void run() {
                        ComposeRecipientsView.this.updateRecentContactList();
                    }
                });
            }
        });
        this.mMatchedContactsListView.setOnTouchListener(new HideKeyboardTouchListener() {
            protected void hideKeyboard() {
                ComposeRecipientsView.this.mHolder.hideKeyboard();
            }
        });
    }

    public void onActivityDestroy() {
        if (this.mInvalidRecipentDialog != null) {
            this.mInvalidRecipentDialog.dismiss();
            this.mInvalidRecipentDialog = null;
        }
        synchronized (this.mDialogSync) {
            if (this.mRecipientPickerDialog != null) {
                if (this.mRecipientPickerDialog.isShowing()) {
                    this.mRecipientPickerDialog.dismiss();
                }
                this.mRecipientPickerDialog = null;
            }
        }
        if (this.mMatchedContactsListAdpter != null) {
            this.mMatchedContactsListAdpter.clearRecipientsList();
        }
    }

    public void onActivityConfigurationChanged(Configuration newConfig) {
        if (isInEdit()) {
            setScrollerHeightAndHint(CHANGE_SCROLLER_HEIGHT_DELAY_LONG);
        }
        HwBackgroundLoader.getUIHandler().postDelayed(new Runnable() {
            public void run() {
                if (!ComposeRecipientsView.this.mHolder.getFragment().isDetached()) {
                    ComposeRecipientsView.this.updateRecentContactList();
                    if (!(ComposeRecipientsView.this.mRecipientsEditor == null || ComposeRecipientsView.this.mRecipientsEditor.hasFocus())) {
                        ComposeRecipientsView.this.mRecipientsEditor.updateCollapseTextView(ComposeRecipientsView.this.getRecipientEditorLayoutWidth());
                    }
                }
            }
        }, 200);
    }

    public void onActivityStop() {
        if (this.mMatchedContactsListAdpter != null) {
            this.mMatchedContactsListAdpter.clearRecentContactsCache();
        }
        Contact.removeContactsChangedListener(this);
        synchronized (this.mDialogSync) {
            if (this.mRecipientPickerDialog != null) {
                if (this.mRecipientPickerDialog.isShowing()) {
                    this.mRecipientPickerDialog.dismiss();
                }
                this.mRecipientPickerDialog = null;
            }
        }
    }

    public void onActivityResume() {
        if (isInEdit()) {
            if (this.mCollapsedRecipientsView != null) {
                if (this.mCollapsedRecipientsView.getVisibility() == 0) {
                    this.mRecipientRowsScroller.setVisibility(8);
                    this.mCollapsedRecipientsView.setVisibility(0);
                    this.mHolder.onRecipientsEditorFocusOut();
                    HwBackgroundLoader.getUIHandler().postDelayed(new Runnable() {
                        public void run() {
                            if (!ComposeRecipientsView.this.mHolder.getFragment().isDetached()) {
                                ComposeRecipientsView.this.mRecipientsEditor.updateCollapseTextView(ComposeRecipientsView.this.getRecipientEditorLayoutWidth());
                            }
                        }
                    }, 200);
                } else {
                    this.mCollapsedRecipientsView.setVisibility(8);
                    this.mRecipientRowsScroller.setVisibility(0);
                }
            }
            if (this.mHolder.isResumeFromStop()) {
                synchronized (this.mDialogSync) {
                    if (!(this.mRecipientPickerDialog == null || this.mRecipientPickerDialog.isShowing())) {
                        this.mRecipientPickerDialog.show();
                    }
                }
            }
        }
    }

    public ContactList constructContactsFromInput(boolean blocking) {
        return this.mRecipientsEditor.constructContactsFromInput(blocking);
    }

    public void removeEmailAddress() {
        this.mRecipientsEditor.removeEmail();
    }

    public void showSoftInput(int state) {
        if (this.mRecipientsEditor != null) {
            ((InputMethodManager) this.mHolder.getFragment().getContext().getSystemService("input_method")).showSoftInput(this.mRecipientsEditor, state);
        }
    }

    public void onActivityStart() {
        Contact.addContactsChangedListener(this);
        startLoadRecipients();
    }

    public ContactList getRecipients() {
        return this.mRecipientsEditor == null ? null : this.mRecipientsEditor.getRecipients();
    }

    public List<String> getNumbers() {
        return this.mRecipientsEditor == null ? new ArrayList(0) : this.mRecipientsEditor.getNumbers();
    }

    public void commitNumberChip() {
        if (this.mRecipientsEditor != null) {
            this.mRecipientsEditor.commitNumberChip();
        }
    }

    public boolean hasFocus() {
        return (this.mRecipientsEditor == null || this.mRecipientsEditor.getVisibility() != 0) ? false : this.mRecipientsEditor.hasFocus();
    }

    private RecipientsPick getContactPicker() {
        if (this.mContactPickWaiter == null) {
            this.mContactPickWaiter = new RecipientsPick();
        }
        return this.mContactPickWaiter;
    }

    public void processPickResult(Intent data) {
        ArrayList<Integer> dataIds = data.getIntegerArrayListExtra("SelItemData_KeyValue");
        ArrayList<Integer> callsIds = data.getIntegerArrayListExtra("SelItemCalls_KeyValue");
        if ((dataIds != null && dataIds.size() != 0) || (callsIds != null && callsIds.size() != 0)) {
            StatisticalHelper.incrementReportCount(this.mHolder.getFragment().getContext(), 2017);
            processLargeNumberRecipients(dataIds, callsIds);
        }
    }

    private void processLargeNumberRecipients(ArrayList<Integer> dataIds, ArrayList<Integer> callsIds) {
        int i;
        int dataSize = dataIds != null ? dataIds.size() : 0;
        int callsSize = callsIds != null ? callsIds.size() : 0;
        final Parcelable[] uris = new Parcelable[(dataSize + callsSize)];
        if (dataSize > 0) {
            for (i = 0; i < dataSize; i++) {
                uris[i] = Uri.parse("content://com.android.contacts/data/" + ((Integer) dataIds.get(i)).toString());
            }
        }
        if (callsSize > 0) {
            for (i = 0; i < callsSize; i++) {
                uris[dataSize + i] = Uri.parse("content://call_log/calls/" + ((Integer) callsIds.get(i)).toString());
            }
        }
        if (this.mRecipientsEditor == null) {
            initRecipientsEditor();
        }
        if (this.mRecipientsEditor != null) {
            int recipientCount = uris.length + this.mRecipientsEditor.getRecipientCount();
            MLog.d(TAG, "Pick Contact add " + uris.length + " new, already exists " + this.mRecipientsEditor.getRecipientCount());
            int recipientLimit = MmsConfig.getRecipientLimit();
            if (recipientLimit == Integer.MAX_VALUE || recipientCount <= recipientLimit) {
                getContactPicker().showWaitDialog(dataSize + callsSize);
                this.mIsContactsPicking = true;
                ThreadEx.execute(new Runnable() {
                    List<String> mInvalideRecipients = new ArrayList();

                    public String toString() {
                        return "ComoseMessageActivity.processPickResult." + super.toString();
                    }

                    public void run() {
                        ContactList list = ContactList.blockingGetByUris(uris);
                        boolean isMms = ComposeRecipientsView.this.mHolder.isMms();
                        Iterator<Contact> iter = list.iterator();
                        while (iter.hasNext()) {
                            String orgNubmer = ((Contact) iter.next()).getNumber();
                            String number = MccMncConfig.getFilterNumberByMCCMNC(orgNubmer);
                            if (HwRecipientUtils.isInvalidRecipient(number, isMms) || (!Contact.isEmailAddress(number) && HwRecipientUtils.isComplexInvalidRecipient(number))) {
                                this.mInvalideRecipients.add(orgNubmer);
                                iter.remove();
                            }
                        }
                        ComposeRecipientsView.this.mRecipientsEditor.setAppendWatcher(new AppendWatcher() {
                            public void onAppendCompleted() {
                                if (!ComposeRecipientsView.this.mHolder.getFragment().isDetached()) {
                                    ComposeRecipientsView.this.mIsContactsPicking = false;
                                    ComposeRecipientsView.this.mSearchKey = MatchedContactsHelper.caculateSearchKey(ComposeRecipientsView.this.mRecipientsEditor.getText());
                                    ComposeRecipientsView.this.startLoadRecipients();
                                    ComposeRecipientsView.this.getContactPicker().stopWaitDialog();
                                    ComposeRecipientsView.this.getContactPicker().alertInvalidRecipients(AnonymousClass13.this.mInvalideRecipients);
                                    ComposeRecipientsView.this.mRecipientsEditor.removeAppendWatcher();
                                    if (ComposeRecipientsView.this.mRecipientsEditor.isNeedCollapse()) {
                                        ComposeRecipientsView.this.mRecipientRowsScroller.setVisibility(8);
                                        ComposeRecipientsView.this.mCollapsedRecipientsView.setVisibility(0);
                                        ComposeRecipientsView.this.mRecipientsEditor.updateCollapseTextView(ComposeRecipientsView.this.getRecipientEditorLayoutWidth());
                                        ComposeRecipientsView.this.mHolder.getHandler().post(new Runnable() {
                                            public void run() {
                                                ComposeRecipientsView.this.mHolder.onRecipientsEditorFocusOut();
                                            }
                                        });
                                    }
                                }
                            }
                        });
                        ComposeRecipientsView.this.getContactPicker().addContact(list, null);
                    }
                });
                return;
            }
            Context context = this.mHolder.getFragment().getContext();
            new Builder(context).setTitle(R.string.pick_too_many_recipients).setIcon(17301543).setMessage(context.getResources().getQuantityString(R.plurals.too_many_recipients, recipientLimit, new Object[]{Integer.valueOf(recipientCount), Integer.valueOf(recipientLimit)})).setPositiveButton(17039370, null).create().show();
        }
    }

    public void alertForInvalidRecipient() {
        alertForInvalidRecipient(getResources().getQuantityString(R.plurals.has_invalid_recipient, getRecipientCount(), new Object[]{this.mRecipientsEditor.formatInvalidNumbers(this.mHolder.isMms())}));
    }

    public void alertForInvalidRecipient(String title) {
        HwBaseFragment fragment = this.mHolder.getFragment();
        if (fragment.isDetached()) {
            MLog.v(TAG, "alertForInvalidRecipient:: already finish when show invalid_recipient_message");
            return;
        }
        if (this.mInvalidRecipentDialog == null) {
            this.mInvalidRecipentDialog = new Builder(fragment.getContext()).setIconAttribute(16843605).setCancelable(true).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (ComposeRecipientsView.this.isVisible()) {
                        ComposeRecipientsView.this.requestFocus();
                    }
                    if (ComposeRecipientsView.this.mInvalidRecipentDialog != null && ComposeRecipientsView.this.mInvalidRecipentDialog.isShowing()) {
                        ComposeRecipientsView.this.mInvalidRecipentDialog.dismiss();
                    }
                }
            }).setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    ComposeRecipientsView.this.requestFocus();
                    dialog.dismiss();
                }
            }).create();
        }
        if (!this.mInvalidRecipentDialog.isShowing()) {
            String message = getResources().getQuantityString(R.plurals.invalid_recipient_message, getRecipientCount());
            this.mInvalidRecipentDialog.setTitle(title);
            this.mInvalidRecipentDialog.setMessage(message);
            this.mInvalidRecipentDialog.show();
        }
    }

    public void filterInvalidRecipients(boolean isMms) {
        try {
            this.mRecipientsEditor.setText(this.mRecipientsEditor.filterInvalidRecipients(isMms));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getMatchedContactsCount() {
        if (this.mMatchedContactsListAdpter != null) {
            return this.mMatchedContactsListAdpter.getCount();
        }
        return 0;
    }

    public int getContactListHeight() {
        int contactsListCount = this.mMatchedContactsListAdpter.getCount();
        if (contactsListCount < 1) {
            return 0;
        }
        int recentContactListHeight;
        int meatureLayoutHeight = (contactsListCount * getResources().getDimensionPixelOffset(R.dimen.recipient_list_item_height)) + ((contactsListCount - 1) * this.mMatchedContactsListView.getDividerHeight());
        LinearLayout recipientLayout = (LinearLayout) findViewById(R.id.mms_recipients_editer_group);
        int layoutHeight = 0;
        if (recipientLayout != null) {
            layoutHeight = recipientLayout.getMeasuredHeight();
        }
        int composeGroupHeight = this.mHolder.getParentHeight();
        int paddingHeight = getResources().getDimensionPixelOffset(R.dimen.mms_recipient_layout_margin_top);
        if (!this.mHolder.isKeyBoardOpen() || this.mHolder.isInMultiWindowMode()) {
            recentContactListHeight = (((composeGroupHeight - layoutHeight) - (paddingHeight * 2)) - this.mHolder.getBottomPanalMinHeight()) - this.mHolder.getMultiSimModelLayoutHeight();
        } else {
            recentContactListHeight = (composeGroupHeight - layoutHeight) - (paddingHeight * 2);
        }
        if (meatureLayoutHeight <= recentContactListHeight) {
            recentContactListHeight = meatureLayoutHeight;
        }
        return recentContactListHeight;
    }

    private boolean isShowMatchedContactsList() {
        boolean z = false;
        if (this.mRecipientsEditor == null || !this.mRecipientsEditor.hasFocus()) {
            return false;
        }
        if (this.mMatchedContactsListAdpter.getCount() > 0) {
            z = true;
        }
        return z;
    }

    public void updateRecentContactList() {
        if (this.mRecipientsEditor != null && this.mRecipientsEditor.getVisibility() == 0) {
            if (isShowMatchedContactsList()) {
                this.mMatchedContactsListView.setVisibility(0);
                this.mMatchedContactsListView.measure(0, 0);
                LayoutParams layoutParams = this.mMatchedContactsListView.getLayoutParams();
                layoutParams.height = getContactListHeight();
                this.mMatchedContactsListView.setLayoutParams(layoutParams);
            } else {
                this.mMatchedContactsListView.setVisibility(8);
                this.mMatchedContactsListView.invalidate();
            }
        }
    }

    public void onKeyboardStateChanged(boolean isSmsEnabled, boolean isKeyboardOpen) {
        if (isSmsEnabled) {
            if (isKeyboardOpen) {
                if (this.mRecipientsEditor != null) {
                    this.mRecipientsEditor.setFocusableInTouchMode(true);
                }
            } else if (this.mRecipientsEditor != null) {
                this.mRecipientsEditor.setFocusable(false);
            }
        } else if (this.mRecipientsEditor != null) {
            this.mRecipientsEditor.setFocusableInTouchMode(false);
        }
    }

    private void launchMultiplePhonePicker() {
        HwBaseFragment fragment = this.mHolder.getFragment();
        MLog.v(TAG, "launchMultiplePhonePicker:: picking contact");
        if (!showInvalidDestinationToast()) {
            int count = getRecipientCount();
            if (count >= MmsConfig.getRecipientLimit()) {
                Toast.makeText(fragment.getContext(), fragment.getContext().getString(R.string.exceed_recipients_limit_Toast, new Object[]{Integer.valueOf(MmsConfig.getRecipientLimit())}), 1).show();
            } else {
                Intent contactIntent = new Intent();
                contactIntent.setAction("android.intent.action.PICK");
                contactIntent.setType("vnd.android.cursor.dir/phone_v2");
                contactIntent.putExtra("com.huawei.community.action.ADD_EMAIL", true);
                contactIntent.putExtra("com.huawei.community.action.MULTIPLE_PICK", true);
                contactIntent.putExtra("com.huawei.community.action.MAX_SELECT_COUNT", MmsConfig.getRecipientLimit() - count);
                contactIntent.putExtra("com.huawei.community.action.EXPECT_INTEGER_LIST", true);
                if (this.mRcsComposeRecipientsView != null) {
                    this.mRcsComposeRecipientsView.setContactIntent(contactIntent);
                }
                this.mHolder.setCryptoToastIsShow(true);
                fragment.startActivityForResult(contactIntent, 109);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void startLoadRecipients() {
        if (this.mRecipientsEditor != null && this.mRecipientsEditor.getVisibility() == 0 && this.mRecipientsEditor.isFocused()) {
            if (!TextUtils.isEmpty(this.mSearchKey)) {
                this.mMatchedContactsListAdpter.loadMatchContacts(this.mSearchKey);
            } else if (this.mRecipientsEditor.isNeedBlockRecentContactsLoading()) {
                this.mRecipientsEditor.setNeedBlockRecentContactsLoading(false);
            } else {
                this.mMatchedContactsListAdpter.loadRecentContacts(this.mRecipientsEditor.getNumbers());
            }
        }
    }

    public void onClick(View v) {
        if (v == this.mRecipientsPicker) {
            StatisticalHelper.incrementReportCount(this.mHolder.getFragment().getContext(), 2182);
            launchMultiplePhonePicker();
            return;
        }
        if (v == this.mCollapsedRecipientsView) {
            if (this.mRecipientsEditor.isUpdatedText()) {
                this.mRecipientRowsScroller.setVisibility(0);
                this.mCollapsedRecipientsView.setVisibility(8);
                this.mRecipientsEditor.requestFocus();
                this.mRecipientsEditor.setSelection(this.mRecipientsEditor.length());
            } else {
                return;
            }
        }
        if (v == this.mBackButton) {
            HwMessageUtils.hideKeyBoard(getFragment().getActivity());
            if (this.mIsFromLauncher) {
                Intent itt = new Intent(getFragment().getActivity(), ConversationList.class);
                itt.setAction("android.intent.action.MAIN");
                getFragment().getActivity().startActivity(itt);
                getFragment().getActivity().overridePendingTransition(R.anim.activity_from_launcher_enter, R.anim.activity_from_launcher_exit);
                getFragment().getActivity().finish();
            } else {
                getFragment().getActivity().onBackPressed();
            }
        }
    }

    public void onChanged() {
        HwBackgroundLoader.getUIHandler().removeCallbacks(this.mLoadRecipientsRunnable);
        HwBackgroundLoader.getUIHandler().postDelayed(this.mLoadRecipientsRunnable, 500);
    }

    public void setBackButtonState(boolean isVisible) {
        if (this.mBackButton != null) {
            this.mBackButton.setVisibility(isVisible ? 0 : 8);
        }
    }

    public void setIsFromLauncher(boolean isFromLauncher) {
        this.mIsFromLauncher = isFromLauncher;
    }

    public int getRecipientGroupLayoutHeight() {
        if (this.mRecipientGroupLayout != null) {
            return this.mRecipientGroupLayout.getMeasuredHeight();
        }
        return 0;
    }
}
