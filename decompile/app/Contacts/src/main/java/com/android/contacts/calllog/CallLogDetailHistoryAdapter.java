package com.android.contacts.calllog;

import android.app.Activity;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.amap.api.services.core.AMapException;
import com.android.contacts.CallUtil;
import com.android.contacts.ContactDpiAdapter;
import com.android.contacts.ContactsUtils;
import com.android.contacts.PhoneCallDetails;
import com.android.contacts.activities.ContactInfoFragment;
import com.android.contacts.detail.ContactDetailAdapter.DetailViewEntry;
import com.android.contacts.detail.ContactDetailAdapter.SendSmsListener;
import com.android.contacts.detail.EspaceDialer;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.calllog.CallRecord;
import com.android.contacts.hap.calllog.CallRecord.CallRecordItem;
import com.android.contacts.hap.rcs.RcsCLIRBroadCastHelper;
import com.android.contacts.hap.rcs.RcsContactsUtils;
import com.android.contacts.hap.rcs.RcseProfile;
import com.android.contacts.hap.rcs.calllog.RcsCallLogDetailHistoryHelper;
import com.android.contacts.hap.rcs.calllog.RcsCallLogDetailHistoryHelper.HistoryCallback;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.util.BackgroundViewCacher;
import com.android.contacts.hap.util.ContactStaticCache;
import com.android.contacts.hap.util.MultiUsersUtils;
import com.android.contacts.hap.utils.VtLteUtils;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.AsyncTaskExecutors;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.Objects;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.util.PhoneNumberFormatter;
import com.android.contacts.util.SharePreferenceUtil;
import com.android.contacts.util.TextUtil;
import com.android.contacts.widget.AbstractExpandableViewAdapter;
import com.android.dialer.calllog.VoicemailDeleteDialog;
import com.android.dialer.greeting.presenter.PlaybackPresenter;
import com.android.dialer.util.OnVoicemailMenuDeletedListener;
import com.android.dialer.voicemail.VoicemailPlaybackPresenter;
import com.android.dialer.voicemail.VoicemailPlaybackPresenter.OnAutoCollapseListener;
import com.android.dialer.voicemail.VoicemailPlaybackPresenter.OnVoicemailDeletedListener;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.ViewUtil;
import com.huawei.cust.HwCustUtils;
import huawei.android.text.format.HwDateUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CallLogDetailHistoryAdapter extends BaseAdapter implements OnVoicemailDeletedListener, OnScrollListener, OnAutoCollapseListener, OnVoicemailMenuDeletedListener<Integer>, HistoryCallback {
    private boolean isLanguageForUrdu = false;
    public boolean isNeedShowDetailEntry = false;
    private boolean isShowImage = false;
    private View lastOpen = null;
    CallLogDetailFragment mCallLogDetailFragment;
    private final Context mContext;
    private int mCurrentlyExpandedPosition = -1;
    private long mCurrentlyExpandedRowId = -1;
    HwCustCallLogDetailHistoryAdapter mCustCallLogDetailHistoryAdapter;
    public int mDetailLabelLeft;
    private final OnClickListener mExpandCollapseListener = new OnClickListener() {
        public void onClick(View v) {
            CallLogDetailHolder viewHolder = (CallLogDetailHolder) v.getTag();
            if (viewHolder != null) {
                if (CallLogDetailHistoryAdapter.this.mVoicemailPlaybackPresenter != null) {
                    CallLogDetailHistoryAdapter.this.mVoicemailPlaybackPresenter.resetAll();
                }
                if (viewHolder.getAdapterPosition() == CallLogDetailHistoryAdapter.this.mCurrentlyExpandedPosition) {
                    viewHolder.showActionsAnimation(false);
                    CallLogDetailHistoryAdapter.this.mCurrentlyExpandedPosition = -1;
                    CallLogDetailHistoryAdapter.this.mCurrentlyExpandedRowId = -1;
                    CallLogDetailHistoryAdapter.this.lastOpen = null;
                } else {
                    CallLogDetailHistoryAdapter.this.expandViewHolderActions(viewHolder);
                }
            }
        }
    };
    private Fragment mFragment;
    private ArrayList<String> mHasRecordNumberList = new ArrayList();
    private boolean mIsRingTimesEnabled;
    private final LayoutInflater mLayoutInflater;
    public boolean mNeedShowEspaceEntry = false;
    View mNoNameDetailEntryView = null;
    private int mNoNamedMarginStart;
    private PhoneCallDetails[] mPhoneCallDetails;
    private final OnClickListener mPrimaryActionListener = new OnClickListener() {
        public void onClick(View v) {
            PhoneCallDetails details = CallLogDetailHistoryAdapter.this.getItem(((CallLogDetailHolder) v.getTag()).mPosition);
            if (details != null) {
                if (CallLogDetailHistoryAdapter.this.mFragment instanceof ContactInfoFragment) {
                    ((ContactInfoFragment) CallLogDetailHistoryAdapter.this.mFragment).setResetFlag(false);
                }
                if (details.getCallsTypeFeatures() == 32) {
                    EspaceDialer.dialVoIpCall(CallLogDetailHistoryAdapter.this.mContext, details.number.toString());
                    return;
                }
                if (VtLteUtils.isVtLteOn(CallLogDetailHistoryAdapter.this.mContext) && details.getCallsTypeFeatures() == 1) {
                    VtLteUtils.startVideoCall(details.number.toString(), CallLogDetailHistoryAdapter.this.mContext.getApplicationContext());
                } else {
                    Intent chooseSubIntent = new Intent("com.android.contacts.action.CHOOSE_SUB", Uri.fromParts("tel", details.number.toString(), null));
                    IntentProvider.addRoamingDataIntent(chooseSubIntent, details.name != null ? details.name.toString() : null, details.mNormalizedNumber, details.countryIso, details.contactUri != null ? details.contactUri.toString() : null, details.duration);
                    chooseSubIntent.setFlags(276856832);
                    if (!CallLogDetailHistoryAdapter.this.mCallLogDetailFragment.checkAndInitCall(details)) {
                        CallLogDetailHistoryAdapter.this.mContext.startActivity(chooseSubIntent);
                        if (!(CallLogDetailHistoryAdapter.this.mFragment == null || CallLogDetailHistoryAdapter.this.mFragment.getActivity() == null)) {
                            CallLogDetailHistoryAdapter.this.mFragment.getActivity().overridePendingTransition(0, 0);
                        }
                        StatisticalHelper.report(AMapException.CODE_AMAP_SERVICE_UNKNOWN_ERROR);
                    }
                }
            }
        }
    };
    public int mRcsCallActionState = -1;
    private RcsCallLogDetailHistoryHelper mRcsHelper;
    private BackgroundViewCacher mViewInflator = null;
    private final VoicemailPlaybackPresenter mVoicemailPlaybackPresenter;

    public static class DetailView implements OnCreateContextMenuListener, OnMenuItemClickListener {
        public View actionsViewContainer;
        public TextView data;
        public Activity mActivity;
        private RcsCLIRBroadCastHelper mCLIRBroadCastHelper;
        public Context mContext;
        public CallLogDetailFragment mDetailFragment;
        public DetailViewEntry mEntry;
        public Fragment mFragment;
        protected boolean mIsVtLteOn = false;
        public ImageView mPrimaryActionButton;
        private final OnClickListener mPrimaryActionClickListener = new OnClickListener() {
            public void onClick(View v) {
                if (v.getTag() instanceof DetailViewEntry) {
                    DetailViewEntry entry = (DetailViewEntry) v.getTag();
                    if (entry.intent != null) {
                        Intent chooseSubIntent = new Intent("com.android.contacts.action.CHOOSE_SUB", Uri.fromParts("tel", entry.data, null));
                        IntentProvider.addRoamingDataIntent(chooseSubIntent, entry.data, entry.roamingData, null, null, 0);
                        chooseSubIntent.setFlags(276856832);
                        DetailView.this.mActivity.getApplicationContext().startActivity(chooseSubIntent);
                        if (!(DetailView.this.mFragment == null || DetailView.this.mFragment.getActivity() == null)) {
                            DetailView.this.mFragment.getActivity().overridePendingTransition(0, 0);
                        }
                    }
                }
            }
        };
        public ImageView mRcsCallAction;
        public int mRcsCallButtonState = -1;
        public TextView mSimCard;
        public ImageView mVideoAction;
        private final OnClickListener mVideoActionClickListener = new OnClickListener() {
            public void onClick(View v) {
                if (v.getTag() instanceof DetailViewEntry) {
                    DetailViewEntry entry = (DetailViewEntry) v.getTag();
                    if (!TextUtils.isEmpty(entry.data)) {
                        if (DetailView.this.mFragment instanceof ContactInfoFragment) {
                            ((ContactInfoFragment) DetailView.this.mFragment).setResetFlag(false);
                        }
                        VtLteUtils.startVideoCall(entry.data, DetailView.this.mContext);
                    }
                }
            }
        };
        public TextView type;

        public DetailView(CallLogDetailFragment callLogDetailFragment, View view, int phone, DetailViewEntry entry) {
            ImageView videoAction;
            this.mFragment = callLogDetailFragment.getFragment();
            this.mActivity = callLogDetailFragment.getActivity();
            this.mContext = callLogDetailFragment.getActivity().getApplicationContext();
            this.mEntry = entry;
            this.actionsViewContainer = view.findViewById(R.id.actions_view_container);
            this.data = (TextView) view.findViewById(R.id.data);
            this.type = null;
            this.mVideoAction = null;
            this.mRcsCallAction = null;
            this.actionsViewContainer.setTag(entry);
            this.mDetailFragment = callLogDetailFragment;
            this.mSimCard = (TextView) view.findViewById(R.id.sim_card_text);
            if (EmuiFeatureManager.isSystemSMSCapable()) {
                this.mPrimaryActionButton = (ImageView) view.findViewById(R.id.primary_action_button);
                ViewUtil.setStateListIcon(this.mContext, this.mPrimaryActionButton, false);
                if (SimFactoryManager.isBothSimEnabled()) {
                    boolean isFirstSimEnable = callLogDetailFragment.ismIsFirstSimEnabled();
                    boolean isSecondSimEnable = callLogDetailFragment.ismIsSecondSimEnabled();
                    if ((isFirstSimEnable || isSecondSimEnable) && !(isFirstSimEnable && isSecondSimEnable)) {
                        this.mSimCard.setText(isFirstSimEnable ? R.string.detail_sim_card1_number : R.string.detail_sim_card2_number);
                    } else if (callLogDetailFragment.getDefaultSimcard() != -1 && isFirstSimEnable && isSecondSimEnable) {
                        this.mSimCard.setText(String.valueOf(callLogDetailFragment.getDefaultSimcard() + 1));
                    } else if (callLogDetailFragment.getDefaultSimcard() == -1 && isFirstSimEnable && isSecondSimEnable) {
                        this.mSimCard.setText("");
                    }
                }
            } else {
                this.mPrimaryActionButton = null;
                ImageView mPrimaryActionButtonTmp = (ImageView) view.findViewById(R.id.primary_action_button);
                if (mPrimaryActionButtonTmp != null) {
                    mPrimaryActionButtonTmp.setVisibility(8);
                }
            }
            if (!EmuiFeatureManager.isSystemVoiceCapable()) {
                ImageView mVoiceActionButtonTmp = (ImageView) view.findViewById(R.id.primary_action_call_button_image);
                if (mVoiceActionButtonTmp != null) {
                    mVoiceActionButtonTmp.setVisibility(8);
                }
            }
            this.mIsVtLteOn = VtLteUtils.isVtLteOn(this.mContext);
            if (this.mIsVtLteOn) {
                videoAction = (ImageView) view.findViewById(R.id.video_action);
                ViewUtil.setStateListIcon(this.mContext, videoAction, false);
            } else {
                videoAction = null;
                ImageView videoActionTmp = (ImageView) view.findViewById(R.id.video_action);
                if (videoActionTmp != null) {
                    videoActionTmp.setVisibility(8);
                }
            }
            if (EmuiFeatureManager.isRcsFeatureEnable()) {
                this.mRcsCallAction = (ImageView) view.findViewById(R.id.rcs_call_action);
                ViewUtil.setStateListIcon(this.mContext, this.mRcsCallAction, false);
                final String rcsNumber = entry.data;
                this.mCLIRBroadCastHelper = callLogDetailFragment.getRcsCLIRBroadCastHelper();
                this.mRcsCallAction.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        StatisticalHelper.report(1220);
                        DetailView.this.startPreCallActivity(rcsNumber);
                    }
                });
            }
            this.type = (TextView) view.findViewById(R.id.type);
            this.mVideoAction = videoAction;
            boolean isPhoneItem = ContactStaticCache.isMimeTypeEqual(entry.mimetype, 1, "vnd.android.cursor.item/phone_v2");
            boolean isVoiceMailNum = false;
            if (isPhoneItem) {
                isVoiceMailNum = PhoneNumberUtils.isVoiceMailNumber(entry.data);
            }
            if (this.type != null) {
                if (isVoiceMailNum) {
                    this.type.setText(entry.data);
                } else {
                    this.type.setText(entry.typeString);
                }
                this.type.setVisibility(0);
            }
            if (this.data != null) {
                if (!isVoiceMailNum) {
                    this.data.setText(entry.data);
                } else if (MultiUsersUtils.isCurrentUserGuest()) {
                    this.data.setText(entry.data);
                } else {
                    this.data.setText(this.mContext.getResources().getString(R.string.voicemail));
                }
            }
            setMaxLines(this.data, 5);
            if (this.data != null) {
                this.data.setEllipsize(TruncateAt.END);
            }
            if (this.mIsVtLteOn) {
                if (SimFactoryManager.isDualSim() && isPhoneItem && entry.mCustom_mimetype == null) {
                    if (this.mVideoAction != null) {
                        this.mVideoAction.setTag(entry);
                        this.mVideoAction.setEnabled(true);
                        if (!(callLogDetailFragment.ismIsFirstSimEnabled() || callLogDetailFragment.ismIsSecondSimEnabled())) {
                            this.mVideoAction.setEnabled(true);
                        }
                    }
                } else if (this.mVideoAction != null) {
                    this.mVideoAction.setTag(entry);
                }
            }
            String lNumber = entry.mOriginalPhoneNum;
            if (TextUtils.isEmpty(lNumber)) {
                lNumber = entry.data;
            }
            if (this.mPrimaryActionButton != null) {
                this.mPrimaryActionButton.setOnClickListener(new SendSmsListener(this.mFragment, lNumber));
            }
            this.actionsViewContainer.setOnCreateContextMenuListener(this);
            this.actionsViewContainer.setOnClickListener(this.mPrimaryActionClickListener);
            if (this.mVideoAction != null) {
                this.mVideoAction.setOnClickListener(this.mVideoActionClickListener);
            }
        }

        public void changeRcsCallActionState(int rcsCallActionState) {
            this.mRcsCallButtonState = rcsCallActionState;
            if (RcsContactsUtils.isBBVersion()) {
                this.mRcsCallButtonState = -1;
            }
            switch (this.mRcsCallButtonState) {
                case -1:
                    this.mRcsCallAction.setVisibility(8);
                    return;
                case 0:
                    this.mRcsCallAction.setVisibility(0);
                    this.mRcsCallAction.setEnabled(false);
                    return;
                case 1:
                    this.mRcsCallAction.setVisibility(0);
                    this.mRcsCallAction.setEnabled(true);
                    return;
                default:
                    this.mRcsCallAction.setVisibility(8);
                    return;
            }
        }

        private void startPreCallActivity(String rcsNumber) {
            if (this.mCLIRBroadCastHelper == null || !this.mCLIRBroadCastHelper.isCLIROpen()) {
                RcsContactsUtils.startPreCallActivity(this.mActivity, rcsNumber);
            } else {
                this.mCLIRBroadCastHelper.showDialog(this.mActivity);
            }
        }

        private void setMaxLines(TextView textView, int maxLines) {
            if (textView != null) {
                if (maxLines == 1) {
                    textView.setSingleLine(true);
                    textView.setEllipsize(TruncateAt.END);
                } else {
                    textView.setSingleLine(false);
                    textView.setMaxLines(maxLines);
                    textView.setEllipsize(null);
                }
            }
        }

        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            if (v.getTag() instanceof DetailViewEntry) {
                DetailViewEntry selectedEntry = (DetailViewEntry) v.getTag();
                menu.setHeaderTitle(selectedEntry.data);
                menu.add(0, 1000, 0, this.mContext.getString(R.string.copy_text)).setOnMenuItemClickListener(this);
                PhoneNumberHelper phoneNumberHelper = new PhoneNumberHelper(this.mContext.getResources());
                if ("vnd.android.cursor.item/phone_v2".equals(selectedEntry.mimetype) && EmuiFeatureManager.isSystemVoiceCapable() && !phoneNumberHelper.isSipNumber(selectedEntry.data)) {
                    menu.add(0, 1003, 0, this.mContext.getString(R.string.recentCalls_editBeforeCall)).setOnMenuItemClickListener(this);
                }
            }
        }

        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case 1000:
                    copyToClipboard();
                    return true;
                case 1003:
                    editBeforeCall();
                    return true;
                default:
                    return false;
            }
        }

        private void editBeforeCall() {
            CharSequence number = this.mEntry.data;
            if (ContactStaticCache.isMimeTypeEqual(this.mEntry.mimetype, 1, "vnd.android.cursor.item/phone_v2")) {
                number = PhoneNumberFormatter.parsePhoneNumber(number.toString());
            }
            Intent intent = new Intent("android.intent.action.DIAL", CallUtil.getCallUri(number.toString()));
            intent.setPackage("com.android.contacts");
            this.mActivity.startActivity(intent);
        }

        private void copyToClipboard() {
            CharSequence textToCopy = this.mEntry.data;
            if (!TextUtils.isEmpty(textToCopy)) {
                if (ContactStaticCache.isMimeTypeEqual(this.mEntry.mimetype, 1, "vnd.android.cursor.item/phone_v2")) {
                    textToCopy = PhoneNumberFormatter.parsePhoneNumber(textToCopy.toString());
                    if (!TextUtils.isEmpty(textToCopy)) {
                        textToCopy = ContactsUtils.removeDashesAndBlanks(textToCopy.toString());
                    }
                }
                ((ClipboardManager) this.mContext.getSystemService("clipboard")).setPrimaryClip(new ClipData(this.mEntry.typeString, new String[]{this.mEntry.mimetype}, new Item(textToCopy)));
            }
        }
    }

    private void expandViewHolderActions(CallLogDetailHolder viewHolder) {
        if (!(this.mCurrentlyExpandedPosition == -1 || this.lastOpen == null)) {
            AbstractExpandableViewAdapter.animateView(this.lastOpen, 1);
        }
        viewHolder.showActionsAnimation(true);
        this.mCurrentlyExpandedPosition = viewHolder.getAdapterPosition();
        this.mCurrentlyExpandedRowId = viewHolder.rowId;
        this.lastOpen = viewHolder.actionsView;
    }

    public void setIsShowImage(boolean isShow) {
        this.isShowImage = isShow;
    }

    public CallLogDetailHistoryAdapter(Fragment fragment, LayoutInflater layoutInflater, CallTypeHelper callTypeHelper, PhoneCallDetails[] phoneCallDetails, VoicemailPlaybackPresenter voicemailPlaybackPresenter, PlaybackPresenter presenter) {
        this.mFragment = fragment;
        this.mContext = fragment.getContext();
        this.mLayoutInflater = layoutInflater;
        this.mVoicemailPlaybackPresenter = voicemailPlaybackPresenter;
        if (this.mVoicemailPlaybackPresenter != null) {
            this.mVoicemailPlaybackPresenter.setOnVoicemailDeletedListener(this);
            this.mVoicemailPlaybackPresenter.setOnAutoCollapseListener(this);
        }
        String language = this.mContext.getResources().getConfiguration().locale.getLanguage();
        if (!TextUtils.isEmpty(language) && "ur".equals(language)) {
            this.isLanguageForUrdu = true;
        }
        if (fragment instanceof ContactInfoFragment) {
            this.isNeedShowDetailEntry = ((ContactInfoFragment) fragment).needShowDetailEntry();
            this.mNeedShowEspaceEntry = this.isNeedShowDetailEntry ? EspaceDialer.checkIsShowEspace(this.mContext) : false;
            this.mNoNamedMarginStart = this.mContext.getResources().getDimensionPixelSize(R.dimen.detail_calllog_nonamed_marginstart);
            this.mDetailLabelLeft = this.mContext.getResources().getDimensionPixelSize(R.dimen.detail_item_label_left_margin);
        }
        this.mPhoneCallDetails = new PhoneCallDetails[phoneCallDetails.length];
        System.arraycopy(phoneCallDetails, 0, this.mPhoneCallDetails, 0, phoneCallDetails.length);
        this.mIsRingTimesEnabled = EmuiFeatureManager.isRingTimesDisplayEnabled(this.mContext);
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.mCustCallLogDetailHistoryAdapter = (HwCustCallLogDetailHistoryAdapter) HwCustUtils.createObj(HwCustCallLogDetailHistoryAdapter.class, new Object[0]);
        }
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            this.mRcsHelper = new RcsCallLogDetailHistoryHelper(fragment, this, presenter);
        }
    }

    public void setPhoneCallDetails(PhoneCallDetails[] phoneCallDetails) {
        int i = 5;
        if (phoneCallDetails != null) {
            this.mCurrentlyExpandedPosition = -1;
            this.mPhoneCallDetails = new PhoneCallDetails[phoneCallDetails.length];
            System.arraycopy(phoneCallDetails, 0, this.mPhoneCallDetails, 0, phoneCallDetails.length);
            if (EmuiFeatureManager.isRcsFeatureEnable()) {
                RcsCallLogDetailHistoryHelper rcsCallLogDetailHistoryHelper = this.mRcsHelper;
                PhoneCallDetails[] phoneCallDetailsArr = this.mPhoneCallDetails;
                if (this.mPhoneCallDetails.length <= 5) {
                    i = this.mPhoneCallDetails.length - 1;
                }
                rcsCallLogDetailHistoryHelper.asyncLoadLocationAndMmsInCallDataCache(phoneCallDetailsArr, 0, i);
            }
            notifyDataSetChanged();
            if (HwLog.HWDBG) {
                HwLog.d("CallLogDetailHistoryAdapter", "setPhoneCallDetails");
            }
        }
    }

    public int getCount() {
        return getCallDetailCount();
    }

    public int getCallDetailCount() {
        return this.mPhoneCallDetails.length;
    }

    public PhoneCallDetails getItem(int position) {
        if (position >= 0 && position < getCallDetailCount()) {
            return this.mPhoneCallDetails[position];
        }
        HwLog.w("CallLogDetailHistoryAdapter", "getItem Array Index Out Of Bounds");
        return null;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public static boolean getDurationShowNotice(Context context) {
        if (context == null) {
            return false;
        }
        return SharePreferenceUtil.getDefaultSp_de(context).getBoolean("custom_made_duration_show_notice_id", true);
    }

    public static void setDurationShowNotice(Context context, boolean isNextShowNotice) {
        Editor editor = SharePreferenceUtil.getDefaultSp_de(context).edit();
        editor.putBoolean("custom_made_duration_show_notice_id", isNextShowNotice);
        editor.apply();
        editor.commit();
    }

    public View getNoNameDetailEntryView(ViewGroup parent) {
        if (this.mFragment instanceof ContactInfoFragment) {
            DetailViewEntry entry = ((ContactInfoFragment) this.mFragment).getNoNameDetailEntry();
            if (this.mNoNameDetailEntryView == null || ((this.mNoNameDetailEntryView.getTag() instanceof DetailView) && ((DetailView) this.mNoNameDetailEntryView.getTag()).mIsVtLteOn != VtLteUtils.isVtLteOn(this.mContext))) {
                this.mNoNameDetailEntryView = this.mLayoutInflater.inflate(R.layout.detail_item_phone, parent, false);
                this.mNoNameDetailEntryView.setTag(new DetailView(this.mCallLogDetailFragment, this.mNoNameDetailEntryView, 1, entry));
                try {
                    if (EmuiFeatureManager.isRcsFeatureEnable()) {
                        if (RcseProfile.getRcsService() == null || !RcseProfile.getRcsService().getLoginState()) {
                            this.mRcsCallActionState = 0;
                        } else {
                            this.mRcsCallActionState = -1;
                        }
                    }
                } catch (Exception e) {
                    HwLog.e("CallLogDetailHistoryAdapter", "failed to update rcs view");
                }
            }
        }
        if (this.mNoNameDetailEntryView.getTag() instanceof DetailView) {
            DetailView detailView = (DetailView) this.mNoNameDetailEntryView.getTag();
            if (detailView.mRcsCallButtonState != this.mRcsCallActionState) {
                detailView.changeRcsCallActionState(this.mRcsCallActionState);
            }
            if (detailView.mVideoAction != null && VtLteUtils.isVtLteOn(this.mContext)) {
                detailView.mVideoAction.setEnabled(VtLteUtils.isLteServiceAbility());
            }
        }
        return this.mNoNameDetailEntryView;
    }

    public View getNoNameEspaceEntryView(ViewGroup parent) {
        View view = this.mLayoutInflater.inflate(R.layout.detail_item_escape, parent, false);
        View containView = view.findViewById(R.id.actions_view_container);
        if (this.mFragment instanceof ContactInfoFragment) {
            final DetailViewEntry entry = ((ContactInfoFragment) this.mFragment).getNoNameDetailEntry();
            containView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    EspaceDialer.dialVoIpCall(CallLogDetailHistoryAdapter.this.mContext, entry.data);
                }
            });
        }
        return view;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View result;
        CallLogDetailHolder viewHolder;
        if (convertView != null && (convertView.getTag() instanceof DetailView)) {
            convertView = null;
        }
        if (convertView != null && convertView.getTag() == null) {
            convertView = null;
        }
        if (convertView == null) {
            result = this.mViewInflator.getViewFromCache(R.layout.call_detail_history_item);
            CallLogDetailHolder callLogDetailHolder = new CallLogDetailHolder(result, this.mContext, this.mVoicemailPlaybackPresenter);
            if (callLogDetailHolder.mEncryptCallLogDetailHistoryAdapterViewHolder != null) {
                callLogDetailHolder.mEncryptCallLogDetailHistoryAdapterViewHolder.initEncryptCallView(result);
            }
            if (this.isNeedShowDetailEntry) {
                LayoutParams params = (RelativeLayout.LayoutParams) callLogDetailHolder.dateView.getLayoutParams();
                params.setMarginStart(this.mNoNamedMarginStart);
                callLogDetailHolder.dateView.setLayoutParams(params);
                callLogDetailHolder.dateView.setSingleLine(true);
                callLogDetailHolder.dateView.setMaxWidth(ContactDpiAdapter.getNewPxDpi(R.dimen.stranger_number_history_call_log_date_item_max_length, this.mContext));
                callLogDetailHolder.dateView.setEllipsize(TruncateAt.END);
                callLogDetailHolder.isNeedShowDetailEntry = this.isNeedShowDetailEntry;
                LinearLayout.LayoutParams dividerParams = (LinearLayout.LayoutParams) callLogDetailHolder.divider.getLayoutParams();
                dividerParams.setMarginStart(this.mDetailLabelLeft);
                callLogDetailHolder.divider.setLayoutParams(dividerParams);
            }
            result.setTag(callLogDetailHolder);
        } else {
            result = convertView;
            viewHolder = (CallLogDetailHolder) convertView.getTag();
        }
        viewHolder.callRecordButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                CallLogDetailHistoryAdapter.this.mCallLogDetailFragment.handleItemClick(v);
            }
        });
        ImageView callRecord = viewHolder.callRecord;
        ImageView callRecordButton = viewHolder.callRecordButton;
        PhoneCallDetails details = this.mPhoneCallDetails[position];
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            this.mRcsHelper.updateViewHolderWithRcs(viewHolder, result, details);
        }
        callRecordButton.setTag(details);
        if (details == null) {
            return result;
        }
        setupCallRecord(callRecord, callRecordButton, details);
        CallTypeIconsView callTypeIconView = viewHolder.callTypeIconView;
        TextView callTypeTextView = viewHolder.callTypeTextView;
        TextView dateView = viewHolder.dateView;
        TextView callNumber = viewHolder.callNumber;
        if (this.mFragment instanceof ContactInfoFragment) {
            callNumber.setVisibility(((ContactInfoFragment) this.mFragment).isUnKnownNumberCall() ? 8 : 0);
        } else {
            callNumber.setVisibility(0);
        }
        callNumber.setText(details.formattedNumber);
        TextView durationView = viewHolder.durationView;
        TextView ringTimesView = viewHolder.ringTimesView;
        ImageView imageView = null;
        if (this.isShowImage) {
            imageView = (ImageView) result.findViewById(R.id.call_type_image);
            imageView.setVisibility(0);
            if (SimFactoryManager.getSimCombination() == 2) {
                if (details.subscriptionID == 0) {
                    imageView.setImageResource(R.drawable.stat_sys_sim1);
                    imageView.setContentDescription(this.mContext.getString(R.string.str_filter_sim1));
                } else if (details.subscriptionID == 1) {
                    imageView.setImageResource(R.drawable.stat_sys_sim2);
                    imageView.setContentDescription(this.mContext.getString(R.string.str_filter_sim2));
                } else if (details.subscriptionID == 2) {
                    imageView.setImageResource(R.drawable.fastscroll_familyname_normal);
                    imageView.setContentDescription(this.mContext.getString(R.string.content_description_card_type_g_roaming));
                }
            } else if (details.subscriptionID == 0) {
                imageView.setImageResource(R.drawable.stat_sys_sim1);
                imageView.setContentDescription(this.mContext.getString(R.string.str_filter_sim1));
            } else {
                imageView.setImageResource(R.drawable.stat_sys_sim2);
                imageView.setContentDescription(this.mContext.getString(R.string.str_filter_sim2));
            }
        }
        viewHolder.espacetext.setVisibility(8);
        if (details.getCallsTypeFeatures() == 32) {
            viewHolder.espacetext.setVisibility(0);
            if (imageView != null) {
                imageView.setVisibility(8);
            }
        }
        int callType = details.callTypes[0];
        callTypeIconView.clear();
        callTypeIconView.add(callType, details.getCallsTypeFeatures(), details.mReadState);
        viewHolder.mPosition = position;
        viewHolder.rowId = Long.parseLong(details.mId);
        viewHolder.voicemailUri = details.voiceMailNumber;
        viewHolder.callType = callType;
        viewHolder.number = details.number.toString();
        if (viewHolder.callType == 4) {
            viewHolder.actionView.setOnClickListener(this.mExpandCollapseListener);
            viewHolder.transcription = details.mTranscription;
        } else {
            viewHolder.actionView.setOnClickListener(this.mPrimaryActionListener);
        }
        viewHolder.actionView.setTag(viewHolder);
        if (ringTimesView != null) {
            if (!this.mIsRingTimesEnabled || !CommonUtilMethods.isMissedType(callType)) {
                ringTimesView.setVisibility(8);
            } else if (callType == 5) {
                ringTimesView.setText(this.mContext.getResources().getString(R.string.call_reject));
                ringTimesView.setVisibility(0);
            } else {
                ringTimesView.setText(String.format(this.mContext.getResources().getQuantityText(R.plurals.contacts_ring_times, details.mRingTimes).toString(), new Object[]{Integer.valueOf(details.mRingTimes)}));
                ringTimesView.setVisibility(0);
            }
        }
        if (CommonUtilMethods.isMissedType(callType)) {
            callTypeTextView.setText("");
            dateView.setTextColor(this.mContext.getResources().getColor(R.color.call_log_ring_times_text_color));
        } else if (callType == 2 && details.duration == 0) {
            dateView.setTextColor(this.mContext.getResources().getColor(R.color.contact_eidtor_item_name_color));
            callTypeTextView.setText(R.string.miss_outgoing);
        } else if (callType == 4) {
            callTypeTextView.setText("");
            if (details.mReadState == 0) {
                dateView.setTextColor(this.mContext.getResources().getColor(R.color.call_log_ring_times_text_color));
            } else {
                dateView.setTextColor(this.mContext.getResources().getColor(R.color.contact_eidtor_item_name_color));
            }
        } else {
            callTypeTextView.setText("");
            dateView.setTextColor(this.mContext.getResources().getColor(R.color.contact_eidtor_item_name_color));
        }
        if (viewHolder.mEncryptCallLogDetailHistoryAdapterViewHolder != null) {
            viewHolder.mEncryptCallLogDetailHistoryAdapterViewHolder.updateEncryptCallViewVisibility(details.getEncryptCallStatus());
        }
        setDateTextViewContext(dateView, details, this.mContext);
        if (CommonUtilMethods.isLayoutRTL() && this.isLanguageForUrdu) {
            dateView.setLayoutDirection(0);
        }
        if (CommonUtilMethods.isMissedType(callType) || (details.duration == 0 && callType != 1)) {
            durationView.setVisibility(4);
            durationView.setText("");
        } else {
            durationView.setVisibility(0);
            durationView.setText(formatDuration(details.duration));
        }
        if (this.mCustCallLogDetailHistoryAdapter != null) {
            this.mCustCallLogDetailHistoryAdapter.hideCallTypeAndDurationView(callTypeTextView, durationView);
        }
        adjustSecondViewWidth(viewHolder);
        if (this.mCurrentlyExpandedRowId == viewHolder.rowId) {
            this.mCurrentlyExpandedPosition = position;
            viewHolder.initActionView();
            this.lastOpen = viewHolder.actionsView;
        }
        viewHolder.showActions(this.mCurrentlyExpandedPosition == position);
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            this.mRcsHelper.getViewForRcs(viewHolder, details);
        }
        return result;
    }

    private void setupCallRecord(ImageView callRecord, ImageView callRecordButton, PhoneCallDetails details) {
        if (callRecord != null && details != null) {
            CallRecordItem[] items = details.mCallRecordItems;
            if (items == null || items.length <= 0) {
                callRecord.setVisibility(8);
                callRecordButton.setVisibility(8);
                return;
            }
            ViewUtil.setStateListIcon(callRecordButton.getContext(), callRecordButton, false);
            callRecord.setVisibility(0);
            callRecordButton.setVisibility(0);
        }
    }

    private String formatDuration(long elapsedSeconds) {
        long minutes = 0;
        long hours = 0;
        if (elapsedSeconds >= 60) {
            minutes = elapsedSeconds / 60;
            if (minutes >= 60) {
                hours = minutes / 60;
                minutes -= 60 * hours;
                elapsedSeconds -= (60 * hours) * 60;
            }
            elapsedSeconds -= 60 * minutes;
        }
        long seconds = elapsedSeconds;
        String min = this.mContext.getResources().getQuantityString(R.plurals.callDetailsDurationFormatHours_Minutes, (int) minutes, new Object[]{Long.valueOf(minutes)});
        String sec = this.mContext.getResources().getQuantityString(R.plurals.callDetailsDurationFormatHours_Seconds, (int) seconds, new Object[]{Long.valueOf(seconds)});
        if (hours >= 1) {
            String h = this.mContext.getResources().getQuantityString(R.plurals.callDetailsDurationFormatHours_Hours, (int) hours, new Object[]{Long.valueOf(hours)});
            return this.mContext.getString(R.string.callDetailsDurationFormat_Merge, new Object[]{h, min, sec});
        } else if (minutes < 1) {
            return this.mContext.getString(R.string.callDetailsDurationFormat_Merge, new Object[]{"", "", sec});
        } else {
            return this.mContext.getString(R.string.callDetailsDurationFormat_Merge, new Object[]{"", min, sec});
        }
    }

    public void clearCallLogs() {
        if (this.isNeedShowDetailEntry) {
            int size = 1;
            if (PhoneCapabilityTester.isCallDurationHid() && getDurationShowNotice(this.mContext)) {
                size = 2;
            }
            if (this.mNeedShowEspaceEntry) {
                size++;
            }
            this.mPhoneCallDetails = new PhoneCallDetails[size];
        } else {
            this.mPhoneCallDetails = new PhoneCallDetails[0];
        }
        notifyDataSetChanged();
    }

    public void removeSingleCallDetail(int deletedIndex) {
        if (-1 != deletedIndex) {
            ArrayList<PhoneCallDetails> lTempDetails = new ArrayList();
            for (int i = 0; i < this.mPhoneCallDetails.length; i++) {
                if (!Objects.equal(this.mPhoneCallDetails[i], getItem(deletedIndex))) {
                    lTempDetails.add(this.mPhoneCallDetails[i]);
                }
            }
            this.mPhoneCallDetails = new PhoneCallDetails[lTempDetails.size()];
            this.mPhoneCallDetails = (PhoneCallDetails[]) lTempDetails.toArray(this.mPhoneCallDetails);
            notifyDataSetChanged();
        }
    }

    public void setupCallRecords(final String number) {
        PhoneCallDetails[] tempPhoneCallDetails = new PhoneCallDetails[this.mPhoneCallDetails.length];
        System.arraycopy(this.mPhoneCallDetails, 0, tempPhoneCallDetails, 0, tempPhoneCallDetails.length);
        new AsyncTask<PhoneCallDetails[], Void, Void>() {
            protected Void doInBackground(PhoneCallDetails[]... params) {
                if (!(number == null || number.equals(""))) {
                    CallRecord cr = new CallRecord(CallLogDetailHistoryAdapter.this.mContext, number);
                    PhoneCallDetails[] ltempPhoneCallDetails = params[0];
                    if (CallLogDetailHistoryAdapter.this.mHasRecordNumberList.contains(number)) {
                        CallLogDetailHistoryAdapter.this.mHasRecordNumberList.remove(number);
                    }
                    for (PhoneCallDetails pcd : ltempPhoneCallDetails) {
                        if (pcd != null && (PhoneNumberUtils.compare(number, pcd.number.toString()) || "unknown".equals(number))) {
                            long begin = pcd.date;
                            pcd.mCallRecordItems = cr.getCallRecordItems(begin, begin + (pcd.duration * 1000), number);
                            if (!(pcd.mCallRecordItems == null || pcd.mCallRecordItems.length == 0 || CallLogDetailHistoryAdapter.this.mHasRecordNumberList.contains(number))) {
                                CallLogDetailHistoryAdapter.this.mHasRecordNumberList.add(number);
                            }
                        }
                    }
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                CallLogDetailHistoryAdapter.this.notifyDataSetChanged();
            }
        }.executeOnExecutor(AsyncTaskExecutors.SERIAL_EXECUTOR, new PhoneCallDetails[][]{tempPhoneCallDetails});
    }

    private void adjustSecondViewWidth(CallLogDetailHolder viewHolder) {
        int cardWidth;
        int max;
        int i = R.dimen.call_log_indent_margin_start;
        DisplayMetrics metrics = this.mContext.getResources().getDisplayMetrics();
        if (this.mContext.getResources().getConfiguration().orientation == 2) {
            cardWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_detail_width_landscape);
        } else if (EmuiFeatureManager.isRcsFeatureEnable()) {
            cardWidth = metrics.widthPixels;
        } else {
            cardWidth = (metrics.widthPixels - this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_detail_padding_start)) - this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_detail_padding_end);
        }
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            Resources resources = this.mContext.getResources();
            if (this.isNeedShowDetailEntry) {
                i = R.dimen.detail_calllog_nonamed_marginstart;
            }
            max = (cardWidth - resources.getDimensionPixelSize(i)) - this.mContext.getResources().getDimensionPixelSize(R.dimen.call_log_indent_margin_end);
        } else {
            max = (cardWidth - this.mContext.getResources().getDimensionPixelSize(R.dimen.call_log_indent_margin_start)) - this.mContext.getResources().getDimensionPixelSize(R.dimen.call_log_indent_margin_end);
        }
        adjustNumberViewWidth(viewHolder, max, this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_detail_calllog_item_senond_distance));
    }

    private void adjustNumberViewWidth(CallLogDetailHolder views, int maxWidth, int distanceBetweenItems) {
        int sizeOfOtherItemsExceptNumberView;
        int numDividers = 1;
        float textSize = (float) this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_detail_calllog_item_senond_font);
        int numberViewWidth = TextUtil.getTextWidth(views.callNumber.getText().toString(), textSize);
        int durationWidth = TextUtil.getTextWidth(views.durationView.getText().toString() + views.callTypeTextView.getText().toString(), textSize);
        int svgIconWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_svg_icon_width);
        int actionIconDistance = this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_rcs_call_detail_action_image_distance);
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            sizeOfOtherItemsExceptNumberView = (durationWidth + actionIconDistance) + 0;
        } else {
            sizeOfOtherItemsExceptNumberView = durationWidth + 0;
        }
        int ringTimesViewWidth = TextUtil.getTextWidth(views.ringTimesView.getText().toString(), textSize);
        int outgoingIconViewWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.call_log_second_line_outgoingicon_width);
        int recordIconViewWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.call_log_second_line_recordicon_width);
        int cardTypeViewWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.call_log_second_line_cardtype_width);
        if (views.ringTimesView.getVisibility() == 0 && ringTimesViewWidth > 0) {
            numDividers = 2;
            sizeOfOtherItemsExceptNumberView += ringTimesViewWidth;
        }
        if (views.callTypeIconView.getVisibility() == 0) {
            sizeOfOtherItemsExceptNumberView += outgoingIconViewWidth;
        }
        if (views.callRecord.getVisibility() == 0) {
            sizeOfOtherItemsExceptNumberView += recordIconViewWidth;
        }
        if (views.cardTypeImage.getVisibility() == 0) {
            numDividers++;
            sizeOfOtherItemsExceptNumberView += cardTypeViewWidth;
        }
        if (views.mEncryptCallLogDetailHistoryAdapterViewHolder != null) {
            sizeOfOtherItemsExceptNumberView = views.mEncryptCallLogDetailHistoryAdapterViewHolder.updateSizeOfOtherItemsExceptNumberView(this.mContext, sizeOfOtherItemsExceptNumberView);
        }
        sizeOfOtherItemsExceptNumberView += this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_detail_calllog_item_calltype_margin_start);
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            if (views.mRcsCallDetailImage != null && views.mRcsCallDetailImage.getVisibility() == 0) {
                sizeOfOtherItemsExceptNumberView = (sizeOfOtherItemsExceptNumberView + svgIconWidth) + actionIconDistance;
            }
            if (views.callRecordButton.getVisibility() == 0) {
                sizeOfOtherItemsExceptNumberView = (sizeOfOtherItemsExceptNumberView + svgIconWidth) + actionIconDistance;
            }
        }
        int sizeOfNumberViewToLeft = (maxWidth - sizeOfOtherItemsExceptNumberView) - (distanceBetweenItems * numDividers);
        if (sizeOfNumberViewToLeft <= 0) {
            HwLog.w("CallLogDetailHistoryAdapter", "Invalid room!!! sizeOfNumberViewToLeft=" + sizeOfNumberViewToLeft);
        } else if (sizeOfNumberViewToLeft < numberViewWidth) {
            views.callNumber.setMinWidth(0);
            views.callNumber.setMaxWidth(sizeOfNumberViewToLeft);
        } else {
            views.callNumber.setMinWidth(numberViewWidth);
            views.callNumber.setMaxWidth(maxWidth);
        }
    }

    public boolean hasRecord() {
        return !this.mHasRecordNumberList.isEmpty();
    }

    public void resetRecordList() {
        this.mHasRecordNumberList.clear();
    }

    public void setViewInflator(BackgroundViewCacher viewInflator) {
        this.mViewInflator = viewInflator;
    }

    public void setCallLogDetailFragment(CallLogDetailFragment calllogFragment) {
        this.mCallLogDetailFragment = calllogFragment;
    }

    public CallLogDetailFragment getCallLogDetailFragment() {
        return this.mCallLogDetailFragment;
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("expanded_position", this.mCurrentlyExpandedPosition);
        outState.putLong("expanded_row_id", this.mCurrentlyExpandedRowId);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.mCurrentlyExpandedPosition = savedInstanceState.getInt("expanded_position", -1);
            this.mCurrentlyExpandedRowId = savedInstanceState.getLong("expanded_row_id", -1);
        }
    }

    public void onVoicemailDeleted(Uri uri) {
        VoicemailDeleteDialog.show(this.mFragment.getFragmentManager(), uri);
    }

    public void onVoicemailDeletedInDatabase() {
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int header = ((ListView) view).getHeaderViewsCount();
        int firstVisiblePosition = view.getFirstVisiblePosition() - header;
        int lastVisiblePosition = view.getLastVisiblePosition() - header;
        if (this.mCurrentlyExpandedPosition != -1 && (this.mCurrentlyExpandedPosition < firstVisiblePosition || this.mCurrentlyExpandedPosition > lastVisiblePosition)) {
            HwLog.d("CallLogDetailHistoryAdapter", "slide out of screen , auto close");
            if (this.mVoicemailPlaybackPresenter != null) {
                this.mVoicemailPlaybackPresenter.pausePlayback();
            }
            this.mCurrentlyExpandedPosition = -1;
            this.mCurrentlyExpandedRowId = -1;
            this.lastOpen = null;
        }
        if (EmuiFeatureManager.isRcsFeatureEnable() && scrollState == 0) {
            this.mRcsHelper.asyncLoadLocationAndMmsInCallDataCache(this.mPhoneCallDetails, view.getFirstVisiblePosition(), view.getLastVisiblePosition());
        }
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    public void onAutoCollapse() {
        if (this.lastOpen != null) {
            AbstractExpandableViewAdapter.animateView(this.lastOpen, 1);
        }
        this.mCurrentlyExpandedPosition = -1;
        this.mCurrentlyExpandedRowId = -1;
        this.lastOpen = null;
    }

    public void onVoicemailMenuDeleted(Integer position) {
        if (this.mCurrentlyExpandedPosition != -1 && this.mVoicemailPlaybackPresenter != null && this.mCurrentlyExpandedPosition == position.intValue()) {
            HwLog.d("CallLogDetailHistoryAdapter", "onVoicemailMenuDeleted,pausePlayback");
            this.mVoicemailPlaybackPresenter.pausePlayback();
        }
    }

    private void setDateTextViewContext(TextView dateTextView, PhoneCallDetails details, Context context) {
        String callDateTime = "";
        boolean is24HourFormat = DateFormat.is24HourFormat(context);
        Calendar instance = Calendar.getInstance();
        int nowDate = instance.get(5);
        int nowMonth = instance.get(2);
        int nowYear = instance.get(1);
        Date date2 = new Date(details.date);
        int callDate = date2.getDate();
        int callMonth = date2.getMonth();
        if (nowYear != date2.getYear() + AMapException.CODE_AMAP_CLIENT_UNKNOWN_ERROR) {
            int previousYearFlag = getFormat(true);
            if (is24HourFormat || !CommonUtilMethods.isChineseLanguage()) {
                callDateTime = DateUtils.formatDateTime(context, details.date, previousYearFlag);
            } else {
                callDateTime = HwDateUtils.formatChinaDateTime(context, details.date, previousYearFlag);
            }
        } else if (nowDate - callDate == 0 && nowMonth == callMonth) {
            int flagsHourTime = com.android.contacts.util.DateUtils.getHourTimeFormat();
            if (is24HourFormat || !CommonUtilMethods.isChineseLanguage()) {
                callDateTime = DateUtils.formatDateTime(context, details.date, flagsHourTime);
            } else {
                callDateTime = HwDateUtils.formatChinaDateTime(context, details.date, flagsHourTime);
            }
        } else {
            int thisYearFlag = getFormat(false);
            if (is24HourFormat || !CommonUtilMethods.isChineseLanguage()) {
                callDateTime = DateUtils.formatDateTime(context, details.date, thisYearFlag);
            } else {
                callDateTime = HwDateUtils.formatChinaDateTime(context, details.date, thisYearFlag);
            }
        }
        dateTextView.setText(callDateTime);
    }

    public int getFormat(boolean showYear) {
        if (showYear) {
            return 68117;
        }
        return 68121;
    }

    public void setRcsCallActionState(int actionState) {
        this.mRcsCallActionState = actionState;
        if (this.mNoNameDetailEntryView != null) {
            ((DetailView) this.mNoNameDetailEntryView.getTag()).changeRcsCallActionState(this.mRcsCallActionState);
        }
    }

    public void release() {
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            this.mRcsHelper.release();
        }
    }

    public void clearLruCache() {
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            this.mRcsHelper.clearLruCache();
        }
    }

    public void asynLoadRcsCache() {
        int i = 5;
        if (this.mRcsHelper != null) {
            ListView callLogList = this.mCallLogDetailFragment.getCallLogList();
            if (callLogList != null) {
                this.mRcsHelper.asyncLoadLocationAndMmsInCallDataCache(this.mPhoneCallDetails, callLogList.getFirstVisiblePosition(), callLogList.getLastVisiblePosition());
                return;
            }
            RcsCallLogDetailHistoryHelper rcsCallLogDetailHistoryHelper = this.mRcsHelper;
            PhoneCallDetails[] phoneCallDetailsArr = this.mPhoneCallDetails;
            if (this.mPhoneCallDetails.length <= 5) {
                i = this.mPhoneCallDetails.length - 1;
            }
            rcsCallLogDetailHistoryHelper.asyncLoadLocationAndMmsInCallDataCache(phoneCallDetailsArr, 0, i);
        }
    }
}
