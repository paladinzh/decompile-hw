package com.android.contacts.detail;

import android.app.Activity;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.StatusUpdates;
import android.telephony.PhoneNumberUtils;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.contacts.Collapser.Collapsible;
import com.android.contacts.ContactPresenceIconUtil;
import com.android.contacts.ContactsUtils;
import com.android.contacts.MoreContactUtils;
import com.android.contacts.TypePrecedence;
import com.android.contacts.activities.ContactDetailActivity;
import com.android.contacts.activities.ContactInfoFragment;
import com.android.contacts.calllog.IntentProvider;
import com.android.contacts.compatibility.NumberLocationCache;
import com.android.contacts.compatibility.NumberLocationLoader;
import com.android.contacts.detail.ContactDetailFragment.Listener;
import com.android.contacts.group.SmartGroupUtil;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.CommonUtilMethods.SelectCardCallback;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.rcs.RcsCLIRBroadCastHelper;
import com.android.contacts.hap.rcs.RcsContactsUtils;
import com.android.contacts.hap.rcs.detail.RcsContactDetailAdapter;
import com.android.contacts.hap.roaming.RoamingPhoneGatherUtils;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.hap.util.ContactStaticCache;
import com.android.contacts.hap.util.MultiUsersUtils;
import com.android.contacts.hap.utils.MessageUtils;
import com.android.contacts.hap.utils.VtLteUtils;
import com.android.contacts.model.Contact;
import com.android.contacts.model.RawContact;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountType.EditType;
import com.android.contacts.model.account.BaseAccountType;
import com.android.contacts.model.dataitem.DataItem;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.util.PhoneNumberFormatter;
import com.google.android.gms.R;
import com.google.common.collect.Iterables;
import com.huawei.cspcommon.performance.PLog;
import com.huawei.cspcommon.util.DialerHighlighter;
import com.huawei.cspcommon.util.ViewUtil;
import com.huawei.cust.HwCustUtils;
import com.huawei.numberlocation.NLUtils;
import java.util.ArrayList;

public class ContactDetailAdapter extends BaseAdapter {
    private HwCustContactDetailAdapter hwCustContactDetailAdapter = null;
    private ArrayList<ViewEntry> mAllEntries = new ArrayList();
    private Contact mContactData;
    private Context mContext;
    public DetailViewEntry mCurrentClickDetailViewEntry;
    private ContactDetailFragment mDetailFragment;
    private LayoutInflater mInflater;
    private final OnClickListener mPrimaryActionClickListener = new OnClickListener() {
        public void onClick(View view) {
            if (ContactDetailAdapter.this.mDetailFragment.getListener() != null && (view.getTag() instanceof DetailViewEntry)) {
                DetailViewEntry detailViewEntry = (DetailViewEntry) view.getTag();
                ContactDetailAdapter.this.mCurrentClickDetailViewEntry = detailViewEntry;
                if (detailViewEntry.mimetype != null && detailViewEntry.mimetype.equals("vnd.android.cursor.item/phone_v2")) {
                    Fragment fragment = ContactDetailAdapter.this.mDetailFragment.getFragment();
                    if (fragment != null && (fragment instanceof ContactInfoFragment)) {
                        ((ContactInfoFragment) fragment).setResetFlag(false);
                    }
                    ContactDetailAdapter.this.startChooseSubActivity(detailViewEntry);
                } else if (detailViewEntry.mimetype == null || !"vnd.android.huawei.cursor.item/ringtone".equals(detailViewEntry.mimetype)) {
                    if ("capability".equals(detailViewEntry.mimetype) && "sms".equals(detailViewEntry.mCustom_mimetype)) {
                        ContactDetailAdapter.this.sendSmsSilent(detailViewEntry.intent);
                    } else if ("capability".equals(detailViewEntry.mimetype) && ("weixin".equals(detailViewEntry.mCustom_mimetype) || "weibo".equals(detailViewEntry.mCustom_mimetype))) {
                        ContactDetailAdapter.this.startComponent(detailViewEntry.intent);
                    } else if ("vnd.android.cursor.item/contact_event".equals(detailViewEntry.mimetype)) {
                        ContactDetailAdapter.this.goToCalendar(detailViewEntry);
                    } else {
                        detailViewEntry.click(view, ContactDetailAdapter.this.mDetailFragment.getListener());
                    }
                } else if (ContactDetailAdapter.this.mContext.checkCallingOrSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != 0) {
                    if (ContactDetailAdapter.this.mContext instanceof ContactDetailActivity) {
                        ((ContactDetailActivity) ContactDetailAdapter.this.mContext).requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 2);
                    }
                } else {
                    ContactDetailAdapter.this.selectRingTone(ContactDetailAdapter.this.initRingToneUri(detailViewEntry));
                }
            }
        }
    };
    private RcsCLIRBroadCastHelper mRcsCLIRBroadCastHelper = null;
    private RcsContactDetailAdapter mRcsCust = null;
    private final OnClickListener mSecondaryActionClickListener = new OnClickListener() {
        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onClick(View view) {
            if (!(ContactDetailAdapter.this.mDetailFragment.getListener() == null || view == null || !(view.getTag() instanceof DetailViewEntry))) {
                Intent intent = ((DetailViewEntry) view.getTag()).secondaryIntent;
                if (intent != null) {
                    ContactDetailAdapter.this.mDetailFragment.getListener().onItemClicked(intent);
                }
            }
        }
    };
    private Uri mSelectRingtoneUri;
    private final OnClickListener mTertiaryActionClickListener = new OnClickListener() {
        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onClick(View view) {
            if (!(ContactDetailAdapter.this.mDetailFragment.getListener() == null || view == null || !(view.getTag() instanceof DetailViewEntry))) {
                Intent intent = ((DetailViewEntry) view.getTag()).tertiaryIntent;
                if (intent != null) {
                    if ("vnd.android.cursor.item/vnd.com.tencent.mm.chatting.voip.video".equals(intent.getType())) {
                        StatisticalHelper.report(1179);
                        if (CommonUtilMethods.isNetworkWifi(ContactDetailAdapter.this.mContext)) {
                            ContactDetailAdapter.this.mDetailFragment.getListener().onItemClicked(intent);
                        } else {
                            Toast.makeText(ContactDetailAdapter.this.mContext, R.string.contacts_detail_no_wifi_toast, 0).show();
                        }
                    } else {
                        ContactDetailAdapter.this.mDetailFragment.getListener().onItemClicked(intent);
                    }
                }
            }
        }
    };

    private static class AddConnectionViewCache {
        public final ImageView icon;
        public final TextView name;
        public final View primaryActionView;

        public AddConnectionViewCache(View view) {
            this.name = (TextView) view.findViewById(R.id.add_connection_label);
            this.icon = (ImageView) view.findViewById(R.id.add_connection_icon);
            this.primaryActionView = view.findViewById(R.id.primary_action_view);
        }
    }

    public static class ViewEntry {
        public long id = -1;
        public boolean isEnabled = false;
        public boolean isFromNoNameCall = false;
        private final int viewTypeForAdapter;

        ViewEntry(int viewType) {
            this.viewTypeForAdapter = viewType;
        }

        int getViewType() {
            return this.viewTypeForAdapter;
        }

        long getId() {
            return this.id;
        }

        boolean isEnabled() {
            return this.isEnabled;
        }

        public void click(View clickedView, Listener fragmentListener) {
        }

        public boolean isDetailViewEntry() {
            return this.viewTypeForAdapter == 0;
        }

        public boolean isKindTitleViewEntry() {
            return 2 == this.viewTypeForAdapter;
        }
    }

    public static class AddConnectionViewEntry extends ViewEntry {
        private final Drawable mIcon;
        private final CharSequence mLabel;
        private final OnClickListener mOnClickListener;

        public AddConnectionViewEntry(Context context, OnClickListener onClickListener) {
            super(4);
            this.mIcon = context.getResources().getDrawable(R.drawable.contacts_dialpad_sub_tab_selected_middle);
            this.mLabel = context.getString(R.string.add_connection_button);
            this.mOnClickListener = onClickListener;
            this.isEnabled = true;
        }

        public void click(View clickedView, Listener fragmentListener) {
            if (this.mOnClickListener != null) {
                this.mOnClickListener.onClick(clickedView);
            }
        }

        public Drawable getIcon() {
            return this.mIcon;
        }

        public CharSequence getLabel() {
            return this.mLabel;
        }
    }

    public static class DetailViewCache {
        public final View actionsViewContainer;
        public final TextView data;
        public ImageView mIcon;
        public ImageView mIconType;
        private boolean mIsVtLteOn = false;
        public final ImageView mPrimaryActionButton;
        public final ImageView mRcsCallAction;
        public final ImageView mVideoAction;
        public final View primaryActionView;
        public final TextView type;

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public DetailViewCache(ContactDetailFragment detailFragment, View view, int aMimeTypeInt, DetailViewEntry detailViewEntry, OnClickListener primaryActionClickListener, OnClickListener secondaryActionClickListener, OnClickListener tertiaryActionClickListener, RcsContactDetailAdapter rcsCustDetailAdapter) {
            this.actionsViewContainer = view.findViewById(R.id.actions_view_container);
            this.primaryActionView = view.findViewById(R.id.primary_action_view);
            this.data = (TextView) view.findViewById(R.id.data);
            this.mIconType = (ImageView) view.findViewById(R.id.icon_type);
            boolean isProfile = detailFragment.isProfile();
            ImageView videoAction = null;
            ImageView imageView = null;
            switch (aMimeTypeInt) {
                case 1:
                    if (detailViewEntry.mCustom_mimetype == null) {
                        if (!EmuiFeatureManager.isSystemSMSCapable() || isProfile) {
                            this.mPrimaryActionButton = null;
                            ImageView mPrimaryActionButtonTmp = (ImageView) view.findViewById(R.id.primary_action_button);
                            this.mIconType = (ImageView) view.findViewById(R.id.primary_action_call_button_image);
                            if (mPrimaryActionButtonTmp != null) {
                                mPrimaryActionButtonTmp.setVisibility(8);
                            }
                        } else {
                            this.mPrimaryActionButton = (ImageView) view.findViewById(R.id.primary_action_button);
                            this.mIconType = (ImageView) view.findViewById(R.id.primary_action_call_button_image);
                            ViewUtil.setStateListIcon(detailFragment.getContext(), this.mPrimaryActionButton, false);
                        }
                        if (isProfile && detailFragment.getContext() != null) {
                            this.actionsViewContainer.setPaddingRelative(this.actionsViewContainer.getPaddingStart(), this.actionsViewContainer.getPaddingTop(), detailFragment.getContext().getResources().getDimensionPixelSize(R.dimen.detail_item_action_button_width), this.actionsViewContainer.getPaddingBottom());
                        }
                        if (!EmuiFeatureManager.isSystemVoiceCapable() || isProfile) {
                            ImageView mVoiceActionButtonTmp = (ImageView) view.findViewById(R.id.primary_action_call_button_image);
                            if (mVoiceActionButtonTmp != null) {
                                mVoiceActionButtonTmp.setVisibility(8);
                            }
                        }
                        this.mIsVtLteOn = VtLteUtils.isVtLteOn(detailFragment.getContext());
                        if (!this.mIsVtLteOn || isProfile) {
                            videoAction = null;
                            ImageView videoActionTmp = (ImageView) view.findViewById(R.id.video_action);
                            if (videoActionTmp != null) {
                                videoActionTmp.setVisibility(8);
                            }
                        } else {
                            videoAction = (ImageView) view.findViewById(R.id.video_action);
                            ViewUtil.setStateListIcon(detailFragment.getContext(), videoAction, false);
                        }
                        if (rcsCustDetailAdapter != null) {
                            imageView = (ImageView) view.findViewById(R.id.rcs_call_action);
                            ViewUtil.setStateListIcon(detailFragment.getContext(), imageView, false);
                        }
                        this.type = (TextView) view.findViewById(R.id.type);
                        this.mIcon = null;
                        break;
                    }
                    break;
                case 17:
                    this.actionsViewContainer.setOnClickListener(primaryActionClickListener);
                    this.mPrimaryActionButton = (ImageView) view.findViewById(R.id.primary_action_button);
                    ViewUtil.setStateListIcon(detailFragment.getContext(), this.mPrimaryActionButton, false);
                    ViewUtil.setStateListIcon(detailFragment.getContext(), view.findViewById(R.id.primary_action_call_button), false);
                    this.mPrimaryActionButton.setOnClickListener(secondaryActionClickListener);
                    this.mIcon = (ImageView) view.findViewById(R.id.icon);
                    this.type = null;
                    videoAction = (ImageView) view.findViewById(R.id.video_action);
                    ViewUtil.setStateListIcon(detailFragment.getContext(), videoAction, false);
                    videoAction.setOnClickListener(tertiaryActionClickListener);
                    break;
                case 19:
                case 21:
                case 22:
                case 23:
                    this.actionsViewContainer.setOnClickListener(primaryActionClickListener);
                    this.mPrimaryActionButton = (ImageView) view.findViewById(R.id.primary_action_button);
                    if (aMimeTypeInt == 23) {
                        ImageView imageView2 = (ImageView) view.findViewById(R.id.primary_action_call_button);
                        if (imageView2 != null) {
                            imageView2.setImageResource(R.drawable.contacts_hwsns_profile_normal);
                        }
                    }
                    ViewUtil.setStateListIcon(detailFragment.getContext(), this.mPrimaryActionButton, false);
                    ViewUtil.setStateListIcon(detailFragment.getContext(), view.findViewById(R.id.primary_action_call_button), false);
                    this.mPrimaryActionButton.setOnClickListener(secondaryActionClickListener);
                    this.mIcon = (ImageView) view.findViewById(R.id.icon);
                    this.type = null;
                    if (aMimeTypeInt == 22) {
                        videoAction = (ImageView) view.findViewById(R.id.video_action);
                        ViewUtil.setStateListIcon(detailFragment.getContext(), videoAction, false);
                        videoAction.setOnClickListener(tertiaryActionClickListener);
                    }
                    if (aMimeTypeInt == 19) {
                        videoAction = (ImageView) view.findViewById(R.id.video_action);
                        if (videoAction != null) {
                            videoAction.setVisibility(8);
                        } else {
                            HwLog.e("DetailViewCache", "MIMETYPE_INT.QQ videoAction is null");
                        }
                        ImageView primaryActionCallButton = (ImageView) view.findViewById(R.id.primary_action_call_button);
                        if (primaryActionCallButton == null) {
                            HwLog.e("DetailViewCache", "MIMETYPE_INT.QQ primaryActionCallButton is null");
                            break;
                        } else {
                            primaryActionCallButton.setImageResource(R.drawable.contacts_call_normal);
                            break;
                        }
                    }
                    break;
                default:
                    this.mIcon = (ImageView) view.findViewById(R.id.icon);
                    this.mPrimaryActionButton = null;
                    this.type = (TextView) view.findViewById(R.id.type);
                    break;
            }
            this.mVideoAction = videoAction;
            this.mRcsCallAction = imageView;
        }
    }

    public static class DetailViewEntry extends ViewEntry implements Collapsible<DetailViewEntry>, Parcelable {
        public static final Creator<DetailViewEntry> CREATOR = new Creator<DetailViewEntry>() {
            public DetailViewEntry createFromParcel(Parcel aSource) {
                return new DetailViewEntry(aSource);
            }

            public DetailViewEntry[] newArray(int aSize) {
                return new DetailViewEntry[aSize];
            }
        };
        public int chatCapability;
        public int collapseCount;
        public String data;
        public ArrayList<Long> ids;
        public Intent intent;
        public boolean isFristEntry;
        public boolean isNullOriginalRoamingData;
        public boolean isPrimary;
        public boolean isYellowPage;
        public String kind;
        public String location;
        public String mAccountType;
        public String mCustom_mimetype;
        private boolean mIsInSubSection;
        public String mOriginalPhoneNum;
        public int mPosition;
        public String mPrimaryLabel;
        public String mSecondaryLabel;
        public String mTertiaryLabel;
        public int maxLines;
        public String mimetype;
        public String normalizedNumber;
        public int presence;
        public String roamingData;
        public RoamingPhoneGatherUtils roamingPhoneGatherUtils;
        public int secondaryActionDescription;
        public int secondaryActionIcon;
        public Intent secondaryIntent;
        public Intent tertiaryIntent;
        public int type;
        public String typeString;
        public Uri uri;

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("== DetailViewEntry ==\n");
            sb.append("  type: ").append(this.type).append("\n");
            sb.append("  kind: ").append(this.kind).append("\n");
            sb.append("  typeString: ").append(this.typeString).append("\n");
            sb.append("  data: ").append(this.data).append("\n");
            if (this.uri == null) {
                sb.append("  uri: (null)\n");
            } else {
                sb.append("  uri: ").append(this.uri.toString()).append("\n");
            }
            sb.append("  maxLines: ").append(this.maxLines).append("\n");
            sb.append("  mimetype: ").append(this.mimetype).append("\n");
            sb.append("  isPrimary: ").append(this.isPrimary ? "true" : "false").append("\n");
            sb.append("  secondaryActionIcon: ").append(this.secondaryActionIcon).append("\n");
            sb.append("  secondaryActionDescription: ").append(this.secondaryActionDescription).append("\n");
            if (this.intent == null) {
                sb.append("  intent: (null)\n");
            } else {
                sb.append("  intent: ").append(this.intent.toString()).append("\n");
            }
            if (this.secondaryIntent == null) {
                sb.append("  secondaryIntent: (null)\n");
            } else {
                sb.append("  secondaryIntent: ").append(this.secondaryIntent.toString()).append("\n");
            }
            if (this.tertiaryIntent == null) {
                sb.append("  tertiaryIntent: (null)\n");
            } else {
                sb.append("  tertiaryIntent : ").append(this.tertiaryIntent.toString()).append("\n");
            }
            sb.append("  ids: ").append(Iterables.toString(this.ids)).append("\n");
            sb.append("  collapseCount: ").append(this.collapseCount).append("\n");
            sb.append("  presence: ").append(this.presence).append("\n");
            sb.append("  chatCapability: ").append(this.chatCapability).append("\n");
            sb.append("  mIsInSubsection: ").append(this.mIsInSubSection ? "true" : "false").append("\n");
            sb.append(" mPrimaryLabel: ").append(this.mPrimaryLabel).append("\n");
            sb.append(" mSecondaryLabel: ").append(this.mSecondaryLabel).append("\n");
            sb.append(" mTertiaryLabel: ").append(this.mTertiaryLabel).append("\n");
            sb.append(" mOriginalPhoneNum: ").append(this.mOriginalPhoneNum).append("\n");
            sb.append(" mAccountType: ").append(this.mAccountType).append("\n");
            return sb.toString();
        }

        public RoamingPhoneGatherUtils getRoamingPhoneGatherUtils() {
            return this.roamingPhoneGatherUtils;
        }

        public void setRoamingPhoneGatherUtils(RoamingPhoneGatherUtils roamingPhoneGatherUtils) {
            this.roamingPhoneGatherUtils = roamingPhoneGatherUtils;
        }

        public void setNullOriginalRoamingData(boolean isNullOriginalRoamingData) {
            this.isNullOriginalRoamingData = isNullOriginalRoamingData;
        }

        public DetailViewEntry() {
            super(0);
            this.mPosition = 0;
            this.type = -1;
            this.location = null;
            this.isNullOriginalRoamingData = false;
            this.isYellowPage = false;
            this.maxLines = 1;
            this.isPrimary = false;
            this.secondaryActionIcon = -1;
            this.secondaryActionDescription = -1;
            this.secondaryIntent = null;
            this.tertiaryIntent = null;
            this.ids = new ArrayList();
            this.collapseCount = 0;
            this.presence = -1;
            this.chatCapability = 0;
            this.mIsInSubSection = false;
            this.isEnabled = true;
        }

        public DetailViewEntry(boolean aEnabled) {
            super(0);
            this.mPosition = 0;
            this.type = -1;
            this.location = null;
            this.isNullOriginalRoamingData = false;
            this.isYellowPage = false;
            this.maxLines = 1;
            this.isPrimary = false;
            this.secondaryActionIcon = -1;
            this.secondaryActionDescription = -1;
            this.secondaryIntent = null;
            this.tertiaryIntent = null;
            this.ids = new ArrayList();
            this.collapseCount = 0;
            this.presence = -1;
            this.chatCapability = 0;
            this.mIsInSubSection = false;
            this.isEnabled = aEnabled;
        }

        DetailViewEntry(Parcel aSource) {
            super(0);
            this.mPosition = 0;
            this.type = -1;
            this.location = null;
            this.isNullOriginalRoamingData = false;
            this.isYellowPage = false;
            this.maxLines = 1;
            this.isPrimary = false;
            this.secondaryActionIcon = -1;
            this.secondaryActionDescription = -1;
            this.secondaryIntent = null;
            this.tertiaryIntent = null;
            this.ids = new ArrayList();
            this.collapseCount = 0;
            this.presence = -1;
            this.chatCapability = 0;
            this.mIsInSubSection = false;
            this.mPosition = aSource.readInt();
            this.type = aSource.readInt();
            this.kind = aSource.readString();
            this.typeString = aSource.readString();
            this.data = aSource.readString();
            this.mimetype = aSource.readString();
            this.presence = aSource.readInt();
            this.uri = (Uri) aSource.readParcelable(null);
            this.mCustom_mimetype = aSource.readString();
            this.mPrimaryLabel = aSource.readString();
            this.mSecondaryLabel = aSource.readString();
            this.mTertiaryLabel = aSource.readString();
            try {
                this.mOriginalPhoneNum = aSource.readString();
            } catch (Exception e) {
                HwLog.e("DetailViewEntry", "read mOriginalPhoneNum fail");
            }
            try {
                this.mAccountType = aSource.readString();
            } catch (Exception e2) {
                HwLog.e("DetailViewEntry", "read mAccountType fail");
            }
        }

        public static DetailViewEntry fromValues(Context context, DataItem item, boolean isDirectoryEntry, long directoryId, DataKind dataKind) {
            DetailViewEntry entry = new DetailViewEntry();
            entry.id = item.getId();
            entry.uri = ContentUris.withAppendedId(Data.CONTENT_URI, entry.id);
            if (isDirectoryEntry) {
                entry.uri = entry.uri.buildUpon().appendQueryParameter("directory", String.valueOf(directoryId)).build();
            }
            entry.mimetype = item.getMimeType();
            entry.kind = dataKind.getKindString(context);
            entry.data = item.buildDataString(context, dataKind);
            if (item.hasKindTypeColumn(dataKind)) {
                entry.type = item.getKindTypeColumn(dataKind);
                entry.typeString = "";
                boolean hasFind = false;
                for (EditType type : dataKind.typeList) {
                    if (type.rawValue == entry.type) {
                        if (type.customColumn == null) {
                            entry.typeString = context.getString(type.labelRes);
                        } else {
                            entry.typeString = item.getContentValues().getAsString(type.customColumn);
                        }
                        hasFind = true;
                        if (!hasFind) {
                            addTypeStringForDefault(context, entry);
                        }
                    }
                }
                if (hasFind) {
                    addTypeStringForDefault(context, entry);
                }
            } else {
                addTypeStringForDefault(context, entry);
            }
            return entry;
        }

        private static void addTypeStringForDefault(Context context, DetailViewEntry entry) {
            if ("vnd.android.cursor.item/phone_v2".equals(entry.mimetype)) {
                entry.typeString = context.getString(BaseAccountType.getOtherEditType().labelRes);
            } else if ("vnd.android.cursor.item/email_v2".equals(entry.mimetype)) {
                entry.typeString = context.getString(BaseAccountType.getEmailOtherEditType().labelRes);
            } else if ("vnd.android.cursor.item/im".equals(entry.mimetype)) {
                entry.typeString = context.getString(BaseAccountType.getImDefaultEditType().labelRes);
            } else if ("vnd.android.cursor.item/postal-address_v2".equals(entry.mimetype)) {
                entry.typeString = context.getString(BaseAccountType.getStructuredPostalOtherEditType().labelRes);
            } else {
                entry.typeString = "";
            }
        }

        public void setPresence(int presence) {
            this.presence = presence;
        }

        public void setIsInSubSection(boolean isInSubSection) {
            this.mIsInSubSection = isInSubSection;
        }

        public boolean collapseWith(DetailViewEntry entry) {
            if (!shouldCollapseWith(entry)) {
                return false;
            }
            if (TypePrecedence.getTypePrecedence(this.mimetype, this.type) > TypePrecedence.getTypePrecedence(entry.mimetype, entry.type)) {
                this.type = entry.type;
                this.kind = entry.kind;
                this.typeString = entry.typeString;
            }
            this.maxLines = Math.max(this.maxLines, entry.maxLines);
            if (StatusUpdates.getPresencePrecedence(this.presence) < StatusUpdates.getPresencePrecedence(entry.presence)) {
                this.presence = entry.presence;
            }
            this.isPrimary = entry.isPrimary ? true : this.isPrimary;
            this.ids.add(Long.valueOf(entry.getId()));
            this.collapseCount++;
            return true;
        }

        public boolean shouldCollapseWith(DetailViewEntry entry) {
            if (entry != null && MoreContactUtils.shouldCollapse(this.mimetype, this.data, entry.mimetype, entry.data) && TextUtils.equals(this.mimetype, entry.mimetype) && ContactsUtils.areIntentActionEqual(this.intent, entry.intent) && ContactsUtils.areIntentActionEqual(this.secondaryIntent, entry.secondaryIntent)) {
                return true;
            }
            return false;
        }

        public void click(View clickedView, Listener fragmentListener) {
            if (fragmentListener != null && this.intent != null) {
                fragmentListener.onItemClicked(this.intent);
            }
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mPosition);
            dest.writeInt(this.type);
            dest.writeString(this.kind);
            dest.writeString(this.typeString);
            dest.writeString(this.data);
            dest.writeString(this.mimetype);
            dest.writeInt(this.presence);
            dest.writeParcelable(this.uri, flags);
            dest.writeString(this.mCustom_mimetype);
            dest.writeString(this.mPrimaryLabel);
            dest.writeString(this.mSecondaryLabel);
            dest.writeString(this.mTertiaryLabel);
            try {
                dest.writeString(this.mOriginalPhoneNum);
            } catch (Exception e) {
                HwLog.e("DetailViewEntry", "write mOriginalPhoneNum fail");
            }
            try {
                dest.writeString(this.mAccountType);
            } catch (Exception e2) {
                HwLog.e("DetailViewEntry", "write mAccountType fail");
            }
        }

        boolean isEspaceCallEntry() {
            if (TextUtils.isEmpty(this.mimetype) || !"vnd.android.cursor.item/phone_v2".equals(this.mimetype)) {
                return false;
            }
            return "ip_call".equals(this.mCustom_mimetype);
        }

        boolean isMessageEntry() {
            if (TextUtils.isEmpty(this.mimetype) || !"vnd.android.cursor.item/phone_v2".equals(this.mimetype)) {
                return false;
            }
            return "message".equals(this.mCustom_mimetype);
        }
    }

    private static class DialVideoCallListener implements OnClickListener, Runnable {
        private boolean mClicked = false;
        private ContactDetailFragment mDetailFragment;
        private Handler mHandler;
        private CharSequence mPhoneNum;

        public DialVideoCallListener(ContactDetailFragment detailFragment, CharSequence phoneNum, Handler h) {
            if (CommonUtilMethods.isIpCallEnabled()) {
                this.mPhoneNum = CommonUtilMethods.deleteIPHead(phoneNum.toString());
            } else {
                this.mPhoneNum = phoneNum;
            }
            this.mDetailFragment = detailFragment;
            this.mHandler = h;
        }

        public void onClick(View v) {
            if (this.mClicked || this.mDetailFragment == null || this.mDetailFragment.getListener() == null || v == null) {
                if (HwLog.HWDBG) {
                    HwLog.d("VideoCall", "Video call dost not lauch in onClick. mClicked = " + this.mClicked);
                }
                return;
            }
            if (this.mPhoneNum != null) {
                long contactId;
                String lNumber = PhoneNumberFormatter.parsePhoneNumber(this.mPhoneNum.toString());
                if (this.mDetailFragment.getContactData() == null) {
                    contactId = -1;
                } else {
                    contactId = this.mDetailFragment.getContactData().getId();
                }
                Fragment fragment = this.mDetailFragment.getFragment();
                if (fragment != null && (fragment instanceof ContactInfoFragment)) {
                    ((ContactInfoFragment) fragment).setResetFlag(false);
                }
                VtLteUtils.startVideoCall(contactId, lNumber, this.mDetailFragment.getActivity());
                StatisticalHelper.report(1162);
                this.mClicked = true;
                this.mHandler.postDelayed(this, 100);
            }
        }

        public void run() {
            this.mClicked = false;
        }
    }

    private static class NetworkTitleViewCache {
        public final ImageView icon;
        public final TextView name;

        public NetworkTitleViewCache(View view) {
            this.name = (TextView) view.findViewById(R.id.network_title);
            this.icon = (ImageView) view.findViewById(R.id.network_icon);
        }
    }

    protected static class NetworkTitleViewEntry extends ViewEntry {
        private final Drawable mIcon;
        private final CharSequence mLabel;

        public NetworkTitleViewEntry(Context context, AccountType type) {
            super(3);
            this.mIcon = type.getDisplayIcon(context);
            this.mLabel = type.getDisplayLabel(context);
            this.isEnabled = false;
        }

        public Drawable getIcon() {
            return this.mIcon;
        }

        public CharSequence getLabel() {
            return this.mLabel;
        }
    }

    public static class SendSmsListener implements OnClickListener {
        private Fragment mFragment;
        private CharSequence mPhoneNum;

        public SendSmsListener(Fragment fragment, CharSequence phoneNum) {
            if (CommonUtilMethods.isIpCallEnabled()) {
                this.mPhoneNum = CommonUtilMethods.deleteIPHead(phoneNum.toString());
            } else {
                this.mPhoneNum = phoneNum;
            }
            this.mFragment = fragment;
        }

        public void onClick(View v) {
            if (this.mPhoneNum != null) {
                ContactDetailAdapter.sendSms(this.mFragment, PhoneNumberFormatter.parsePhoneNumber(this.mPhoneNum.toString()));
            }
        }
    }

    private static class TextViewRunnalbe implements Runnable {
        private String mText;
        private TextView mTextView;

        public TextViewRunnalbe(TextView textView, String text) {
            this.mTextView = textView;
            this.mText = text;
        }

        public void run() {
            if (this.mTextView != null) {
                this.mTextView.setText(this.mText);
            }
        }
    }

    public RcsContactDetailAdapter getRcsContactDetailAdapter() {
        return this.mRcsCust;
    }

    public void setContactData(Contact contactData) {
        if (PLog.DEBUG) {
            PLog.d(10, "ContactDetailAdapter setContactData");
        }
        this.mContactData = contactData;
    }

    public void setInflater(LayoutInflater inflater) {
        this.mInflater = inflater;
    }

    public void setDetailFragment(ContactDetailFragment detailFragment) {
        this.mDetailFragment = detailFragment;
    }

    public void setAllEntries(ArrayList<ViewEntry> allEntries) {
        this.mAllEntries = allEntries;
    }

    public ContactDetailAdapter(Context context) {
        this.mContext = context;
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.hwCustContactDetailAdapter = (HwCustContactDetailAdapter) HwCustUtils.createObj(HwCustContactDetailAdapter.class, new Object[]{this.mContext});
        }
        if (MultiUsersUtils.isSmsEnabledForCurrentUser(context)) {
            if (HwLog.HWFLOW) {
                HwLog.i("ContactDetailAdapter", "init RcsContactDetailAdapter");
            }
            this.mRcsCust = new RcsContactDetailAdapter();
            this.mRcsCust.initForCustomizations(this.mContext);
        }
    }

    public void initRCSCapabityquest() {
        if (this.mRcsCust != null) {
            this.mRcsCust.initRCSCapabityquest();
        }
    }

    public void updateFTCapInAdapter(boolean fTcap, String phoneNumber) {
        if (this.mRcsCust != null && !this.mAllEntries.isEmpty()) {
            this.mRcsCust.updateFTCapInAdapter(fTcap, phoneNumber, this.mAllEntries, this);
        }
    }

    public void addPhoneNum(String number) {
        if (this.mRcsCust != null && number != null) {
            this.mRcsCust.addPhoneNum(number, this.mContext);
        }
    }

    public void updateCapMap(String oriNumber, boolean isOnlineEnable, boolean isPreCallSupportedEnable, String formatNumber) {
        if (this.mRcsCust != null && oriNumber != null && formatNumber != null) {
            this.mRcsCust.updateCapMap(oriNumber, isOnlineEnable, isPreCallSupportedEnable, formatNumber);
        }
    }

    public int getCount() {
        return this.mAllEntries.size();
    }

    public ViewEntry getItem(int position) {
        return (ViewEntry) this.mAllEntries.get(position);
    }

    public int getItemViewType(int position) {
        return ((ViewEntry) this.mAllEntries.get(position)).getViewType();
    }

    public int getViewTypeCount() {
        return 6;
    }

    public long getItemId(int position) {
        ViewEntry entry = (ViewEntry) this.mAllEntries.get(position);
        if (entry != null) {
            return entry.getId();
        }
        return -1;
    }

    public boolean areAllItemsEnabled() {
        return false;
    }

    public boolean isEnabled(int position) {
        return getItem(position).isEnabled();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (this.mAllEntries.size() == 0) {
            return null;
        }
        switch (getItemViewType(position)) {
            case 0:
                return getDetailEntryView(position, convertView, parent);
            case 3:
                return getNetworkTitleEntryView(position, convertView, parent);
            case 4:
                return getAddConnectionEntryView(position, convertView, parent);
            default:
                throw new IllegalStateException("Invalid view type ID " + getItemViewType(position));
        }
    }

    private View getNetworkTitleEntryView(int position, View convertView, ViewGroup parent) {
        ViewEntry viewEntry = getItem(position);
        if (!(viewEntry instanceof NetworkTitleViewEntry)) {
            return convertView;
        }
        View result;
        NetworkTitleViewCache viewCache;
        NetworkTitleViewEntry entry = (NetworkTitleViewEntry) viewEntry;
        if (convertView == null || !(convertView.getTag() instanceof NetworkTitleViewCache)) {
            if (this.mInflater == null) {
                this.mInflater = LayoutInflater.from(this.mContext);
            }
            result = this.mInflater.inflate(R.layout.contact_detail_network_title_entry_view, parent, false);
            viewCache = new NetworkTitleViewCache(result);
            result.setTag(viewCache);
        } else {
            result = convertView;
            viewCache = (NetworkTitleViewCache) convertView.getTag();
        }
        viewCache.name.setText(entry.getLabel());
        viewCache.icon.setImageDrawable(entry.getIcon());
        return result;
    }

    private View getAddConnectionEntryView(int position, View convertView, ViewGroup parent) {
        ViewEntry viewEntry = getItem(position);
        if (!(viewEntry instanceof AddConnectionViewEntry)) {
            return convertView;
        }
        View result;
        AddConnectionViewCache viewCache;
        AddConnectionViewEntry entry = (AddConnectionViewEntry) viewEntry;
        if (convertView == null || !(convertView.getTag() instanceof NetworkTitleViewCache)) {
            if (this.mInflater == null) {
                this.mInflater = LayoutInflater.from(this.mContext);
            }
            result = this.mInflater.inflate(R.layout.contact_detail_add_connection_entry_view, parent, false);
            viewCache = new AddConnectionViewCache(result);
            result.setTag(viewCache);
        } else {
            result = convertView;
            viewCache = (AddConnectionViewCache) convertView.getTag();
        }
        viewCache.name.setText(entry.getLabel());
        viewCache.icon.setImageDrawable(entry.getIcon());
        viewCache.primaryActionView.setOnClickListener(entry.mOnClickListener);
        return result;
    }

    private View getDetailEntryView(int position, View convertView, ViewGroup parent) {
        ViewEntry viewEntry = getItem(position);
        if (!(viewEntry instanceof DetailViewEntry)) {
            return convertView;
        }
        View view = convertView;
        DetailViewEntry entry = (DetailViewEntry) viewEntry;
        DetailViewCache detailViewCache = null;
        int mimeType = ContactStaticCache.getMimeTypeIntFromMap(entry.mimetype);
        int layoutId = ContactStaticCache.getContactDetailLayoutForItem(entry.mimetype, mimeType, entry.mCustom_mimetype);
        int intValue = (convertView == null || convertView.getTag(R.layout.detail_item_phone) == null) ? -1 : ((Integer) convertView.getTag(R.layout.detail_item_phone)).intValue();
        Integer layoutIdFromView = Integer.valueOf(intValue);
        if (this.hwCustContactDetailAdapter != null) {
            layoutId = this.hwCustContactDetailAdapter.getCustomLayoutIfNeeded(entry, layoutId);
        }
        int intLayoutIdFromView = layoutIdFromView.intValue();
        if (mimeType == 0 || layoutId != intLayoutIdFromView) {
            view = this.mDetailFragment.getViewInflator().getViewFromCache(layoutId);
            if (view == null) {
                if (this.mInflater == null) {
                    this.mInflater = LayoutInflater.from(this.mContext);
                }
                view = this.mInflater.inflate(layoutId, parent, false);
                view.setTag(R.layout.detail_item_phone, Integer.valueOf(layoutId));
            }
            detailViewCache = new DetailViewCache(this.mDetailFragment, view, mimeType, entry, this.mPrimaryActionClickListener, this.mSecondaryActionClickListener, this.mTertiaryActionClickListener, this.mRcsCust);
            view.setTag(detailViewCache);
        }
        if (mimeType == 1 && entry.mCustom_mimetype == null && viewCache == null && (view.getTag() instanceof DetailViewCache)) {
            detailViewCache = (DetailViewCache) view.getTag();
            if (VtLteUtils.isVtLteOn(this.mContext) != detailViewCache.mIsVtLteOn) {
                view = this.mDetailFragment.getViewInflator().getViewFromCache(layoutId);
                if (view == null) {
                    if (this.mInflater == null) {
                        this.mInflater = LayoutInflater.from(this.mContext);
                    }
                    view = this.mInflater.inflate(layoutId, parent, false);
                    view.setTag(R.layout.detail_item_phone, Integer.valueOf(layoutId));
                }
                detailViewCache = new DetailViewCache(this.mDetailFragment, view, mimeType, entry, this.mPrimaryActionClickListener, this.mSecondaryActionClickListener, this.mTertiaryActionClickListener, this.mRcsCust);
                view.setTag(detailViewCache);
            }
        }
        bindDetailView(position, view, entry);
        if (mimeType == 1 && entry.mCustom_mimetype == null && detailViewCache != null) {
            String lNumber = entry.mOriginalPhoneNum;
            if (TextUtils.isEmpty(lNumber)) {
                lNumber = entry.data;
            }
            if (detailViewCache.mPrimaryActionButton != null) {
                detailViewCache.mPrimaryActionButton.setOnClickListener(new SendSmsListener(this.mDetailFragment.getFragment(), lNumber));
            }
            if (detailViewCache.mVideoAction != null) {
                detailViewCache.mVideoAction.setOnClickListener(new DialVideoCallListener(this.mDetailFragment, lNumber, new Handler()));
                detailViewCache.mVideoAction.setEnabled(VtLteUtils.isLteServiceAbility());
            }
        }
        return view;
    }

    private Uri getContactUri() {
        return this.mContactData != null ? this.mContactData.getLookupUri() : null;
    }

    private void bindDetailView(int position, View view, DetailViewEntry entry) {
        if (position == 0) {
            PLog.d(0, "ContactDetailApater bindDetailView begin");
        }
        Resources resources = this.mContext.getResources();
        if (view.getTag() instanceof DetailViewCache) {
            DetailViewCache views = (DetailViewCache) view.getTag();
            boolean isPhoneItem = ContactStaticCache.isMimeTypeEqual(entry.mimetype, 1, "vnd.android.cursor.item/phone_v2");
            boolean isEmailITem = ContactStaticCache.isMimeTypeEqual(entry.mimetype, 2, "vnd.android.cursor.item/email_v2");
            if (this.mRcsCust != null && EmuiFeatureManager.isRcsFeatureEnable()) {
                this.mRcsCust.setRcsViewVisibility(isPhoneItem, view, entry, this.mContext);
                setRcsCallAction(entry, views, isPhoneItem);
            }
            boolean isVoiceMailNum = false;
            if (MultiUsersUtils.isCurrentUserOwner() && isPhoneItem) {
                isVoiceMailNum = PhoneNumberUtils.isVoiceMailNumber(entry.data);
            }
            if (!TextUtils.isEmpty(entry.typeString) || entry.isFromNoNameCall) {
                String lGeocode = "";
                String lTypeAndLocation = "";
                if (entry.isFromNoNameCall) {
                    lTypeAndLocation = entry.typeString;
                } else if (PhoneCapabilityTester.isGeoCodeFeatureEnabled(this.mContext) && isPhoneItem) {
                    String tmpNumber;
                    if (NLUtils.handleWithChinaPhoneOrFixNumberLogic(entry.data)) {
                        tmpNumber = getGeoNumber(entry);
                    } else {
                        tmpNumber = entry.data;
                    }
                    String number = DialerHighlighter.cleanNumber(tmpNumber, false);
                    lGeocode = NumberLocationCache.getLocation(number);
                    if (lGeocode == null) {
                        lGeocode = NumberLocationLoader.getAndUpdateGeoNumLocation(this.mContext, number);
                    }
                    if (EmuiFeatureManager.isChinaArea()) {
                        SmartGroupUtil.updateNumberLocation(this.mContext, entry.mOriginalPhoneNum, lGeocode, entry.location);
                    }
                    if ((this.hwCustContactDetailAdapter != null && this.hwCustContactDetailAdapter.isCustHideGeoInfo()) || isVoiceMailNum || entry.isYellowPage) {
                        lTypeAndLocation = entry.typeString;
                    } else if (!TextUtils.isEmpty(lGeocode)) {
                        lTypeAndLocation = entry.typeString + " - " + lGeocode;
                    } else if (EmuiFeatureManager.isHideUnknownGeo()) {
                        lTypeAndLocation = entry.typeString;
                    } else {
                        lTypeAndLocation = entry.typeString + " - " + this.mContext.getResources().getString(R.string.numberLocationUnknownLocation2);
                    }
                } else {
                    lTypeAndLocation = entry.typeString;
                }
                if (entry.isPrimary && (isPhoneItem || isEmailITem)) {
                    lTypeAndLocation = lTypeAndLocation + HwCustPreloadContacts.EMPTY_STRING + this.mContext.getResources().getString(R.string.contacts_default);
                }
                if (views.type != null) {
                    if (isVoiceMailNum) {
                        views.type.setText(entry.data);
                    } else {
                        views.type.setText(lTypeAndLocation);
                    }
                    views.type.setVisibility(0);
                }
                if (this.hwCustContactDetailAdapter != null && this.hwCustContactDetailAdapter.isHideGeoInfoOfNoNameCall(entry.isFromNoNameCall)) {
                    this.hwCustContactDetailAdapter.setVisibility(views.type, 8);
                }
            }
            String intentExtra = this.mDetailFragment.getActivity().getIntent().getStringExtra("phone");
            if (this.mContactData != null) {
                String accountType = ((RawContact) this.mContactData.getRawContacts().get(0)).getValues().getAsString("account_type");
                if (intentExtra != null && CommonUtilMethods.isSimAccount(accountType) && intentExtra.length() > 20) {
                    intentExtra = intentExtra.substring(0, 20);
                }
            }
            boolean isNameNotHandled = true;
            if (!(this.mDetailFragment.isHighligh() || intentExtra == null || !DialerHighlighter.cleanNumber(intentExtra, false).equals(DialerHighlighter.cleanNumber(entry.data, false)))) {
                String textString = entry.data;
                CharSequence spannableString = new SpannableString(textString);
                spannableString.setSpan(new ForegroundColorSpan(resources.getColor(R.color.searchhint_people)), 0, textString.length(), 0);
                views.data.setText(spannableString);
                isNameNotHandled = false;
                new Handler().postDelayed(new TextViewRunnalbe(views.data, textString), 1000);
                this.mDetailFragment.setHighligh(true);
            }
            if (isNameNotHandled) {
                if (isVoiceMailNum) {
                    views.data.setText(resources.getString(R.string.voicemail));
                } else {
                    views.data.setText(entry.data);
                }
            }
            if (ContactStaticCache.isMimeTypeEqual(entry.mimetype, 13, "vnd.android.cursor.item/note")) {
                setMaxLines(views.data, Integer.MAX_VALUE);
            } else if ("vnd.android.cursor.item/phone_v2".equals(entry.mimetype)) {
                setMaxLines(views.data, 5);
                if (views.data != null) {
                    views.data.setEllipsize(TruncateAt.END);
                }
            } else {
                setMaxLines(views.data, entry.maxLines);
            }
            if ("emergency".equals(entry.mimetype)) {
                setMaxLines(views.type, entry.maxLines);
            }
            if (!this.mDetailFragment.isProfile()) {
                isEmailITem = false;
            } else if (isPhoneItem) {
                isEmailITem = true;
            }
            if (!isEmailITem) {
                views.actionsViewContainer.setOnClickListener(this.mPrimaryActionClickListener);
            }
            if (SimFactoryManager.isDualSim() && isPhoneItem && entry.mCustom_mimetype == null && views.mPrimaryActionButton != null) {
                views.mPrimaryActionButton.setTag(entry);
                views.mPrimaryActionButton.setEnabled(true);
                views.mPrimaryActionButton.setVisibility(0);
            } else if (views.mPrimaryActionButton != null) {
                views.mPrimaryActionButton.setTag(entry);
            }
            if (views.mIsVtLteOn) {
                if (SimFactoryManager.isDualSim() && isPhoneItem && entry.mCustom_mimetype == null) {
                    if (views.mVideoAction != null) {
                        views.mVideoAction.setTag(entry);
                        views.mVideoAction.setEnabled(true);
                        if (!(this.mDetailFragment.ismIsFirstSimEnabled() || this.mDetailFragment.ismIsSecondSimEnabled())) {
                            views.mVideoAction.setEnabled(true);
                        }
                    }
                } else if (views.mVideoAction != null) {
                    views.mVideoAction.setTag(entry);
                }
            }
            if ((ContactStaticCache.isMimeTypeEqual(entry.mimetype, 17, "vnd.android.cursor.item/vnd.com.tencent.mm.chatting.voip.video") || ContactStaticCache.isMimeTypeEqual(entry.mimetype, 22, "skype")) && views.mVideoAction != null) {
                views.mVideoAction.setTag(entry);
            }
            boolean isProfile = this.mDetailFragment.isProfile();
            Drawable presenceIcon = ContactPresenceIconUtil.getPresenceIcon(this.mContext, entry.presence);
            if (presenceIcon != null) {
                views.data.setCompoundDrawablesWithIntrinsicBounds(presenceIcon, null, null, null);
            } else if (entry.isEspaceCallEntry()) {
                setVisiblityForEspaceCallEntry(views, entry);
            } else if ("vnd.android.cursor.item/phone_v2".equals(entry.mimetype)) {
                if (views.mIconType != null) {
                    views.mIconType.setVisibility(entry.isFristEntry ? 0 : 4);
                }
                if (this.mRcsCust != null) {
                    this.mRcsCust.setVisiblityForOtherEntry(views, entry, view, this.mDetailFragment);
                }
                if (this.hwCustContactDetailAdapter != null) {
                    this.hwCustContactDetailAdapter.setVisiblityForOtherEntry(views, entry, this.mDetailFragment);
                }
                if (entry.isFristEntry && SimFactoryManager.isBothSimEnabled()) {
                    TextView sim_card = (TextView) view.findViewById(R.id.sim_card_text);
                    boolean isFirstSimEnable = this.mDetailFragment.ismIsFirstSimEnabled();
                    boolean isSecondSimEnable = this.mDetailFragment.ismIsSecondSimEnabled();
                    if ((isFirstSimEnable || isSecondSimEnable) && !(isFirstSimEnable && isSecondSimEnable)) {
                        sim_card.setText(isFirstSimEnable ? R.string.detail_sim_card1_number : R.string.detail_sim_card2_number);
                    } else if (this.mDetailFragment.getDefaultSimcard() != -1 && isFirstSimEnable && isSecondSimEnable) {
                        sim_card.setText(String.valueOf(this.mDetailFragment.getDefaultSimcard() + 1));
                    } else if (this.mDetailFragment.getDefaultSimcard() == -1 && isFirstSimEnable && isSecondSimEnable) {
                        sim_card.setText("");
                    }
                }
            } else {
                setDetailIcon(views, entry, view, isProfile);
            }
            ActionsViewContainer actionsButtonContainer = views.actionsViewContainer;
            actionsButtonContainer.setTag(entry);
            actionsButtonContainer.setPosition(position);
            this.mDetailFragment.registerForContextMenu(actionsButtonContainer);
            View primaryActionView = views.primaryActionView;
            if (primaryActionView != null) {
                primaryActionView.setPadding(primaryActionView.getPaddingLeft(), this.mDetailFragment.getViewEntryDimensions().getPaddingTop(), primaryActionView.getPaddingRight(), this.mDetailFragment.getViewEntryDimensions().getPaddingBottom());
            }
            if (position == 0) {
                PLog.d(11, "ContactDetailApater bindDetailView end");
            }
        }
    }

    private void setRcsCallAction(DetailViewEntry entry, DetailViewCache views, boolean isPhoneItem) {
        if (isPhoneItem && entry.mCustom_mimetype == null) {
            final String number = PhoneNumberFormatter.formatNumber(this.mContext, entry.mOriginalPhoneNum);
            int dispalyState = this.mRcsCust.getDisplayState(this.mContactData, entry.data);
            if (views.mRcsCallAction != null) {
                this.mRcsCLIRBroadCastHelper = this.mDetailFragment.getRcsCLIRBroadCastHelper();
                views.mRcsCallAction.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        ContactDetailAdapter.this.setResetFlag(false);
                        StatisticalHelper.report(1220);
                        ContactDetailAdapter.this.startPreCallActivity(number);
                    }
                });
                if (dispalyState == 1) {
                    views.mRcsCallAction.setVisibility(0);
                    views.mRcsCallAction.setEnabled(true);
                } else if (dispalyState == 0) {
                    views.mRcsCallAction.setVisibility(0);
                    views.mRcsCallAction.setEnabled(false);
                } else {
                    views.mRcsCallAction.setVisibility(8);
                }
            }
        }
    }

    private void startPreCallActivity(String number) {
        if (this.mRcsCLIRBroadCastHelper == null || !this.mRcsCLIRBroadCastHelper.isCLIROpen()) {
            RcsContactsUtils.startPreCallActivity(this.mDetailFragment.getActivity(), number, getContactUri());
        } else {
            this.mRcsCLIRBroadCastHelper.showDialog(this.mDetailFragment.getContext());
        }
    }

    private void setDetailIcon(DetailViewCache views, DetailViewEntry entry, View view, boolean isProfile) {
        int i = 4;
        if (entry.mimetype != null && views != null && views.mIcon != null) {
            views.mIcon.setTag(entry);
            String str = entry.mimetype;
            if (str.equals("vnd.android.huawei.cursor.item/ringtone")) {
                views.mIconType.setImageResource(R.drawable.contacts_edit_icon_ringtone);
                views.type.setText(R.string.phone_ringtone_string);
                views.mIcon.setVisibility(0);
            } else if (str.equals("emergency")) {
                views.mIconType.setImageResource(R.drawable.contact_icon_emergency);
                views.type.setText(entry.kind);
                views.mIcon.setVisibility(0);
            } else if (str.equals("vnd.android.cursor.item/email_v2")) {
                views.mIconType.setImageResource(R.drawable.contact_icon_mail);
                views.mIconType.setVisibility(entry.isFristEntry ? 0 : 4);
                r0 = views.mIcon;
                if (!isProfile) {
                    i = 0;
                }
                r0.setVisibility(i);
            } else if (str.equals("vnd.android.cursor.item/note")) {
                views.mIconType.setImageResource(R.drawable.contact_icon_note);
                views.mIcon.setVisibility(4);
                views.type.setText(entry.kind);
            } else if (str.equals("vnd.android.cursor.item/im")) {
                views.mIconType.setImageResource(R.drawable.contacts_icon_aim);
                r0 = views.mIconType;
                if (entry.isFristEntry) {
                    i = 0;
                }
                r0.setVisibility(i);
                views.mIcon.setVisibility(0);
            } else if (str.equals("vnd.android.cursor.item/postal-address_v2")) {
                views.mIconType.setImageResource(R.drawable.contacts_icon_address);
                r0 = views.mIconType;
                if (entry.isFristEntry) {
                    i = 0;
                }
                r0.setVisibility(i);
                views.mIcon.setVisibility(0);
            } else if (str.equals("vnd.android.cursor.item/website")) {
                r0 = views.mIconType;
                if (entry.isFristEntry) {
                    i = 0;
                }
                r0.setVisibility(i);
                views.mIconType.setImageResource(R.drawable.contacts_edit_icon_website);
                views.mIcon.setVisibility(0);
                views.type.setText(entry.kind);
            } else if (str.equals("vnd.android.cursor.item/relation")) {
                views.mIconType.setImageResource(R.drawable.contacts_edit_icon_relation);
                r0 = views.mIconType;
                if (entry.isFristEntry) {
                    i = 0;
                }
                r0.setVisibility(i);
                views.mIcon.setVisibility(0);
            } else if (str.equals("vnd.android.cursor.item/contact_event")) {
                views.mIconType.setImageResource(R.drawable.contacts_edit_icon_date);
                views.mIconType.setVisibility(entry.isFristEntry ? 0 : 4);
                views.mIcon.setVisibility(0);
                if (entry.type == 3 || entry.type == 4) {
                    views.mIcon.setVisibility(0);
                } else {
                    views.mIcon.setVisibility(4);
                }
            } else if (str.equals("vnd.android.cursor.item/group_membership")) {
                views.mIconType.setImageResource(R.drawable.contacts_edit_icon_group);
                views.mIcon.setVisibility(4);
                views.type.setText(entry.kind);
            } else if (str.equals("vnd.android.cursor.item/nickname") || str.equals("#phoneticName")) {
                views.mIconType.setImageResource(R.drawable.contacts_edit_icon_nickname);
                views.mIcon.setVisibility(4);
                views.type.setText(entry.kind);
            } else if (str.equals("vnd.android.cursor.item/sip_address")) {
                views.mIconType.setImageResource(R.drawable.contacts_edit_icon_date);
                views.mIcon.setVisibility(0);
                views.type.setText(entry.kind);
            } else if (str.equals("wechat")) {
                views.mIcon.setImageResource(R.drawable.connection_icon_wechat);
            } else if (str.equals("whatsapp")) {
                views.mIcon.setImageResource(R.drawable.whatsapp_icon);
            } else if (str.equals("skype")) {
                views.mIcon.setImageResource(R.drawable.skypeapp_icon);
            } else if (str.equals("hwsns")) {
                views.mIcon.setImageResource(R.drawable.hwsns_icon);
            } else if (str.equals("qq")) {
                views.mIcon.setImageResource(R.drawable.connection_icon_qq);
                views.mIcon.setContentDescription(this.mContext.getString(R.string.content_description_qq));
                if (entry.secondaryIntent == null) {
                    views.mPrimaryActionButton.setVisibility(8);
                }
            } else if (str.equals("capability")) {
                views.mIconType.setImageResource(R.drawable.ic_contacts_common_details);
                r0 = views.mIconType;
                if (entry.isFristEntry) {
                    i = 0;
                }
                r0.setVisibility(i);
                views.mIcon.setVisibility(0);
                if (TextUtils.isEmpty(views.type.getText())) {
                    views.type.setVisibility(8);
                } else {
                    views.type.setVisibility(0);
                }
            }
        }
    }

    public String getGeoNumber(DetailViewEntry entry) {
        if (entry == null) {
            return null;
        }
        if (entry.normalizedNumber == null || entry.normalizedNumber.matches("^((\\+86)|(0086)|(86))?[4,8]00\\d{7}$")) {
            return entry.data;
        }
        return entry.normalizedNumber;
    }

    private void setVisiblityForEspaceCallEntry(DetailViewCache aViews, DetailViewEntry aEntry) {
        OnClickListener ipCallListener = new OnClickListener() {
            public void onClick(View v) {
                ContactDetailAdapter.this.mDetailFragment.getEspaceDialog();
            }
        };
        if (aEntry.isEnabled) {
            aViews.actionsViewContainer.setOnClickListener(ipCallListener);
        }
    }

    private void setMaxLines(TextView textView, int maxLines) {
        if (maxLines == 1) {
            textView.setSingleLine(true);
            textView.setEllipsize(TruncateAt.END);
        } else if (maxLines == Integer.MAX_VALUE) {
            textView.setSingleLine(false);
            textView.setEllipsize(null);
        } else {
            textView.setSingleLine(false);
            textView.setMaxLines(maxLines);
            textView.setEllipsize(null);
        }
    }

    private void startChooseSubActivity(DetailViewEntry detailViewEntry) {
        Intent chooseSubIntent = new Intent("com.android.contacts.action.CHOOSE_SUB", Uri.fromParts("tel", detailViewEntry.data, null));
        IntentProvider.addRoamingDataIntent(chooseSubIntent, this.mContactData == null ? null : this.mContactData.getDisplayName(), detailViewEntry.isNullOriginalRoamingData ? null : detailViewEntry.roamingData, null, null, 0);
        chooseSubIntent.putExtra("needlearn", detailViewEntry.isNullOriginalRoamingData);
        chooseSubIntent.setFlags(276856832);
        this.mContext.getApplicationContext().startActivity(chooseSubIntent);
        if (this.mDetailFragment.getActivity() != null) {
            this.mDetailFragment.getActivity().overridePendingTransition(0, 0);
        }
    }

    private Uri initRingToneUri(DetailViewEntry detailViewEntry) {
        if ("-1".equals(this.mDetailFragment.getmRingtoneString())) {
            return null;
        }
        if (TextUtils.isEmpty(this.mDetailFragment.getmRingtoneString())) {
            return CommonUtilMethods.initializeDefaultRingtone(this.mContext, detailViewEntry.mAccountType);
        }
        return Uri.parse(this.mDetailFragment.getmRingtoneString());
    }

    public void updateContactDetailUIOnRcsStatusChanged(boolean loginStatus) {
        if (this.mRcsCust != null) {
            this.mRcsCust.updateContactDetailUIOnRcsStatusChanged(this.mAllEntries, this, loginStatus);
        }
    }

    public void addOtherEntry(String entryType, ArrayList<ViewEntry> allEntries) {
        if (this.mRcsCust != null) {
            this.mRcsCust.addOtherEntry(this.mContext, allEntries, this.mContactData, entryType, this.mDetailFragment, this);
        }
    }

    public Uri getRingToneUri() {
        if (this.mCurrentClickDetailViewEntry == null) {
            return null;
        }
        return initRingToneUri(this.mCurrentClickDetailViewEntry);
    }

    public void onSaveInstanceState(Bundle outState) {
        if (outState != null && this.mSelectRingtoneUri != null) {
            outState.putParcelable("SelectRingtoneUri", this.mSelectRingtoneUri);
        }
    }

    public void selectRingTone(Uri aRingtonUri) {
        Intent lRingtoneSlectionIntent = CommonUtilMethods.getRingtoneIntent(this.mDetailFragment.getActivity(), aRingtonUri);
        try {
            Fragment frag = this.mDetailFragment.getFragment();
            if ((frag instanceof ContactInfoFragment) && ((ContactInfoFragment) frag).isNeedUpdateWindows()) {
                this.mDetailFragment.getActivity().setRequestedOrientation(1);
                this.mSelectRingtoneUri = aRingtonUri;
                ExceptionCapture.reportScene(84);
                StatisticalHelper.report(4047);
            }
            this.mSelectRingtoneUri = null;
            this.mDetailFragment.getFragment().startActivityForResult(lRingtoneSlectionIntent, 1000);
            ExceptionCapture.reportScene(84);
            StatisticalHelper.report(4047);
        } catch (ActivityNotFoundException e) {
            HwLog.e("AssignRingtone", "Activity not found." + e);
        }
    }

    private static void sendSms(Fragment fragment, String number) {
        if (fragment != null) {
            Activity activity = fragment.getActivity();
            if (activity == null || activity.checkSelfPermission("android.permission.READ_SMS") == 0) {
                Intent smsIntent = new Intent("android.intent.action.SENDTO", Uri.fromParts("smsto", number, null));
                smsIntent.setFlags(524288);
                if (activity != null) {
                    try {
                        activity.startActivity(smsIntent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(activity, R.string.quickcontact_missing_app_Toast, 0).show();
                    }
                }
                if ((fragment instanceof ContactInfoFragment) && ((ContactInfoFragment) fragment).getIntent().getBooleanExtra("INTENT_FROM_DIALER", false)) {
                    StatisticalHelper.report(((ContactInfoFragment) fragment).isIsNoNamedContact() ? 1110 : 1111);
                } else {
                    StatisticalHelper.report(1108);
                }
                ExceptionCapture.reportScene(85);
                return;
            }
            if (activity instanceof ContactDetailActivity) {
                ((ContactDetailActivity) activity).requestPermissions(new String[]{"android.permission.READ_SMS"}, 3);
            }
        }
    }

    private void sendSmsSilent(final Intent intent) {
        if (this.mContext.checkSelfPermission("android.permission.SEND_SMS") == 0) {
            final String number = intent.getStringExtra("number");
            int[] slotId = MessageUtils.getSlotIdOfOperator(this.mContext, number);
            int length = slotId.length;
            if (length == 0) {
                Toast.makeText(this.mContext, R.string.contact_send_message_failure, 1).show();
                return;
            }
            if (length == 1 && number != null) {
                sendSmsSilent(number, intent.getStringExtra("content"), slotId[0]);
            } else if (number != null) {
                CommonUtilMethods.showSelectCardDialog(this.mContext, this.mContext.getResources().getString(R.string.contact_menu_send_message), R.drawable.contact_message_send_card1, R.drawable.contact_message_send_card2, new SelectCardCallback() {
                    public void confirm(View v, int slotId) {
                        ContactDetailAdapter.this.sendSmsSilent(number, intent.getStringExtra("content"), slotId);
                    }

                    public void cancel(DialogInterface dialog) {
                    }
                }, number);
            }
        }
    }

    private void startComponent(Intent intent) {
        try {
            this.mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this.mContext, R.string.quickcontact_missing_app_Toast, 0).show();
        }
    }

    private void sendSmsSilent(String number, String content, int slotId) {
        int cardNum = 1;
        if (slotId == 0) {
            cardNum = 1;
        } else if (slotId == 1) {
            cardNum = 2;
        }
        String instruct = this.mDetailFragment.getRemainInstruct(cardNum);
        if (TextUtils.isEmpty(instruct)) {
            if (HwLog.HWFLOW) {
                HwLog.i("ContactDetailAdapter", "getRemainInstruct is null");
            }
            instruct = content;
        }
        ArrayList<String> list = new ArrayList();
        list.add(instruct);
        ArrayList<PendingIntent> intentList = new ArrayList();
        intentList.add(PendingIntent.getBroadcast(this.mContext, 0, new Intent("send_message_pending_action"), 1073741824));
        MessageUtils.sendMultipartTextMessage(number, null, list, intentList, null, SimFactoryManager.getSubscriptionIdBasedOnSlot(slotId));
    }

    private void goToCalendar(DetailViewEntry detailViewEntry) {
        if (detailViewEntry.type == 4 || detailViewEntry.type == 3) {
            try {
                this.mDetailFragment.startActivity(detailViewEntry.intent);
            } catch (ActivityNotFoundException e) {
                HwLog.e("ContactDetailAdapter", "ActivityNotFoundException : " + e);
                Toast.makeText(this.mContext, R.string.quickcontact_missing_app_Toast, 0).show();
            }
        }
    }

    private void setResetFlag(boolean flag) {
        Fragment fragment = this.mDetailFragment.getFragment();
        if (fragment != null && (fragment instanceof ContactInfoFragment)) {
            ((ContactInfoFragment) fragment).setResetFlag(flag);
        }
    }
}
