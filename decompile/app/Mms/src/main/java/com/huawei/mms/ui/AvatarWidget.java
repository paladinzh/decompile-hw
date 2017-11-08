package com.huawei.mms.ui;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.QuickContact;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import cn.com.xy.sms.sdk.SmartSmsPublicinfoUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.messaging.util.OsUtil;
import com.android.mms.MmsConfig;
import com.android.mms.R$styleable;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.data.Conversation.Cache;
import com.android.mms.directory.DirectoryQuery;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.ui.AvatarView;
import com.android.mms.ui.ConversationList;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.RecipientListActivity;
import com.android.mms.ui.RecipientListFragment;
import com.android.mms.util.ContactUtil;
import com.android.mms.util.HwCustEcidLookup;
import com.android.mms.util.ItemLoadedCallback;
import com.android.rcs.ui.RcsGroupChatConversationDetailFragment;
import com.google.android.gms.R;
import com.huawei.android.text.format.DateUtilsEx;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.util.AvatarCache;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.StatisticalHelper;
import com.huawei.rcs.ui.RcsGroupChatConversationDetailActivity;
import com.huawei.rcs.utils.RcseMmsExt;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class AvatarWidget extends LinearLayout {
    public static final Uri YELLOW_PAGE_URI = Uri.parse("content://com.android.contacts.app/yellow_page");
    private static HwCustEcidLookup mHwCustEcidLookup = ((HwCustEcidLookup) HwCustUtils.createObj(HwCustEcidLookup.class, new Object[0]));
    private static boolean mIsInvalidGroupStatus = false;
    public static final Uri sGroupUri = Uri.parse("content://rcsim/rcs_groups");
    private final int MINUTE_IN_ONE_HOUR;
    private Calendar cal;
    private DateFormat dMonthDay;
    private DateFormat dYearMonthDay;
    private int mAvatarWeight;
    private ContactList mContactList;
    private View mContentView;
    protected Context mContext;
    private String mDateFormat;
    private int mGroupAvatarReadyCount;
    private int mGroupAvatarStrokeColor;
    private int mGroupAvatarWhiteStrokeWidth;
    private LinearLayout mIconLayout;
    private boolean mIsRcsGroup;
    protected int mItemType;
    private int mMessageItemAvatarMarginMsgpop;
    private int mMessageItemAvatarMarginScreen;
    private int mMessageItemAvatarMarginSelect;
    private int mMessageItemAvatarWidthHeight;
    private boolean mNeedUpdate;
    private AvatarView mRecvAvatarView;
    private float mScale;
    private boolean mShowTextModeTime;
    protected Context mSplitContext;
    private String mTimeIs12Or24;
    private int mTimeMode;
    private ImageView mUnreadIcon;
    private LinearLayout mUnreadIconLayout;
    private LinearLayout mViewGroup;
    private boolean misFirst;

    public AvatarWidget(Context context) {
        this(context, null);
        init(context);
        initItemType(null);
    }

    public AvatarWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context);
        initItemType(attrs);
    }

    public AvatarWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mItemType = 0;
        this.mShowTextModeTime = true;
        this.cal = null;
        this.mTimeMode = 0;
        this.mScale = ContentUtil.FONT_SIZE_NORMAL;
        this.mNeedUpdate = true;
        this.mContactList = null;
        this.mIsRcsGroup = false;
        this.mGroupAvatarReadyCount = 0;
        this.MINUTE_IN_ONE_HOUR = 60;
        init(context);
        initItemType(attrs);
    }

    private void initItemType(AttributeSet attrs) {
        if (attrs == null) {
            this.mItemType = 0;
            return;
        }
        TypedArray a = this.mContext.obtainStyledAttributes(attrs, R$styleable.AvatarWidget);
        this.mItemType = a.getInt(0, 0);
        a.recycle();
    }

    private void init(Context context) {
        this.mContext = context;
        this.mScale = context.getResources().getDisplayMetrics().density;
        this.mDateFormat = System.getString(context.getContentResolver(), "date_format");
        this.mTimeIs12Or24 = System.getString(context.getContentResolver(), "time_12_24");
        this.misFirst = true;
        this.mNeedUpdate = true;
        this.mAvatarWeight = (int) context.getResources().getDimension(R.dimen.avatar_view_width_height_conversation_item);
        this.mMessageItemAvatarMarginScreen = (int) context.getResources().getDimension(R.dimen.avatar_view_message_list_item_margin_screen);
        this.mMessageItemAvatarMarginSelect = (int) context.getResources().getDimension(R.dimen.avatar_view_message_list_item_margin_select);
        this.mMessageItemAvatarMarginMsgpop = (int) context.getResources().getDimension(R.dimen.avatar_view_message_list_item_margin_msgpop);
        this.mMessageItemAvatarWidthHeight = (int) context.getResources().getDimension(R.dimen.avatar_view_width_height_message_item);
        this.mGroupAvatarWhiteStrokeWidth = (int) context.getResources().getDimension(R.dimen.avatar_view_group_stroke);
        this.mGroupAvatarStrokeColor = context.getResources().getColor(R.color.avater_group_stroke_color);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        initLayout();
        updateIconStyle();
    }

    private void updateIconStyle() {
        updateIconStyle(false);
    }

    public void updateIconStyle(boolean isInEditMode) {
        int i = 4;
        LayoutParams params = (LayoutParams) this.mRecvAvatarView.getLayoutParams();
        switch (this.mItemType) {
            case 1:
            case 3:
                int i2;
                boolean isSplitState = false;
                if ((this.mSplitContext instanceof ConversationList) && HwMessageUtils.isSplitOn()) {
                    isSplitState = ((ConversationList) this.mSplitContext).isSplitState();
                }
                AvatarView avatarView = this.mRecvAvatarView;
                if (isSplitState) {
                    i2 = 8;
                } else {
                    i2 = 0;
                }
                avatarView.setVisibility(i2);
                this.mUnreadIcon.setVisibility(4);
                break;
            case 4:
            case 7:
                this.mRecvAvatarView.setVisibility(0);
                params.gravity = 48;
                params.setMarginEnd(this.mMessageItemAvatarMarginMsgpop);
                params.setMarginStart(this.mMessageItemAvatarMarginScreen);
                params.width = this.mMessageItemAvatarWidthHeight;
                params.height = this.mMessageItemAvatarWidthHeight;
                this.mRecvAvatarView.setLayoutParams(params);
                this.mUnreadIcon.setVisibility(8);
                this.mUnreadIconLayout.setVisibility(OsUtil.IS_EMUI_LITE ? 4 : 8);
                break;
            case 5:
            case 10:
            case 11:
                this.mRecvAvatarView.setVisibility(0);
                this.mUnreadIcon.setVisibility(4);
                break;
            case 8:
                this.mRecvAvatarView.setVisibility(0);
                params.gravity = 48;
                params.setMarginEnd(isInEditMode ? this.mMessageItemAvatarMarginSelect : this.mMessageItemAvatarMarginScreen);
                params.setMarginStart(this.mMessageItemAvatarMarginMsgpop);
                params.width = this.mMessageItemAvatarWidthHeight;
                params.height = this.mMessageItemAvatarWidthHeight;
                this.mRecvAvatarView.setLayoutParams(params);
                this.mUnreadIcon.setVisibility(8);
                LinearLayout linearLayout = this.mUnreadIconLayout;
                if (!OsUtil.IS_EMUI_LITE) {
                    i = 8;
                }
                linearLayout.setVisibility(i);
                break;
            default:
                this.mRecvAvatarView.setVisibility(8);
                this.mUnreadIcon.setVisibility(8);
                break;
        }
        if (OsUtil.IS_EMUI_LITE) {
            this.mRecvAvatarView.setVisibility(8);
        }
    }

    public int getItemType() {
        return this.mItemType;
    }

    private void initLayout() {
        if (!isInEditMode()) {
            initRootView();
            initContentView();
            initIconLayout();
            LayoutParams iconLayoutParams = new LayoutParams(-2, -1);
            LayoutParams contentLayoutParams = new LayoutParams(-1, -2);
            contentLayoutParams.gravity = 17;
            switch (this.mItemType) {
                case 1:
                case 3:
                case 5:
                case 10:
                case 11:
                    addView(this.mIconLayout, 0, iconLayoutParams);
                    addView(this.mViewGroup, 1, contentLayoutParams);
                    return;
                default:
                    addView(this.mViewGroup, 0, contentLayoutParams);
                    return;
            }
        }
    }

    private void initContentView() {
        int resId = getContentResId();
        this.mContentView = findViewById(resId);
        if (this.mContentView == null) {
            Log.e("AvatarWidget", "setContentView in " + getClass().getSimpleName() + " empty res: " + resId);
            return;
        }
        removeView(this.mContentView);
        this.mViewGroup = new LinearLayout(this.mContext);
        this.mViewGroup.setOrientation(0);
        if (this.mViewGroup.getChildCount() > 0) {
            this.mViewGroup.removeAllViews();
        }
        this.mViewGroup.addView(this.mContentView);
    }

    private void initIconLayout() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        switch (this.mItemType) {
            case 4:
            case 7:
            case 8:
                if (this.mContentView != null) {
                    this.mIconLayout = (LinearLayout) this.mContentView.findViewById(R.id.avatar_layout);
                    break;
                }
            case 1:
                this.mIconLayout = (LinearLayout) inflater.inflate(R.layout.avatar_widget_layout_conversation, null);
                break;
            default:
                this.mIconLayout = (LinearLayout) inflater.inflate(R.layout.avatar_widget_layout, null);
                break;
        }
        if (this.mIconLayout != null) {
            this.mUnreadIconLayout = (LinearLayout) this.mIconLayout.findViewById(R.id.new_message_hint_layout);
            this.mUnreadIcon = (ImageView) this.mIconLayout.findViewById(R.id.new_message_hint);
            this.mRecvAvatarView = (AvatarView) this.mIconLayout.findViewById(R.id.recv_avatar);
            this.mRecvAvatarView.setImageDrawable(ResEx.self().getAvtarDefault(null));
            this.mRecvAvatarView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (AvatarWidget.this.mContactList == null || AvatarWidget.this.mContactList.size() == 0) {
                        Log.d("AvatarWidget", "click 'notification' or 'favorites in search' avatar, don't response");
                    } else if (AvatarWidget.this.mContactList.size() == 1 && DirectoryQuery.isEnterpriseContactId(((Contact) AvatarWidget.this.mContactList.get(0)).getPersonId()) && MmsConfig.isEnableAFW()) {
                        QuickContact.showQuickContact(AvatarWidget.this.mContext, new Rect(), ((Contact) AvatarWidget.this.mContactList.get(0)).getAfwUri(AvatarWidget.this.mContext), 3, null);
                    } else if (AvatarWidget.this.mItemType == 8 || (AvatarWidget.this.mItemType == 4 && ((Contact) AvatarWidget.this.mContactList.get(0)).isMe())) {
                        StatisticalHelper.incrementReportCount(AvatarWidget.this.mContext, 2219);
                        Log.d("AvatarWidget", "click my avatar, go to my infomation");
                        Uri profileLookupUri = ContactUtil.getProfileLookupUri(AvatarWidget.this.mContext);
                        AvatarWidget.this.mContactList.get(0);
                        if (Contact.isProfileUri(profileLookupUri)) {
                            ComponentName componentName = new ComponentName("com.android.contacts", "com.android.contacts.activities.ProfileSimpleCardActivity");
                            r0 = new Intent("com.huawei.android.intent.action.PROFILE_CONTACT");
                            r0.setFlags(67108864);
                            r0.setData(profileLookupUri);
                            r0.setComponent(componentName);
                            if (r0.resolveActivity(AvatarWidget.this.mContext.getPackageManager()) != null) {
                                AvatarWidget.this.mContext.startActivity(r0);
                            } else {
                                Log.d("AvatarWidget", "intent is null");
                            }
                        } else if (profileLookupUri != null) {
                            r0 = new Intent("android.intent.action.VIEW", profileLookupUri);
                            r0.setFlags(67108864);
                            AvatarWidget.this.mContext.startActivity(r0);
                        }
                    } else if (AvatarWidget.this.mContactList.size() == 1 && ((Contact) AvatarWidget.this.mContactList.get(0)).existsInDatabase()) {
                        StatisticalHelper.incrementReportCount(AvatarWidget.this.mContext, 2215);
                        Log.d("AvatarWidget", "click exists avatar, go to contact infomation");
                        SmartSmsPublicinfoUtil.reflashPublicInfo(AvatarWidget.this.mContext, ((Contact) AvatarWidget.this.mContactList.get(0)).getNumber());
                        r0 = new Intent("android.intent.action.VIEW", ((Contact) AvatarWidget.this.mContactList.get(0)).getUri());
                        r0.setFlags(67108864);
                        AvatarWidget.this.mContext.startActivity(r0);
                    } else if (AvatarWidget.this.mContactList.size() == 1) {
                        StatisticalHelper.incrementReportCount(AvatarWidget.this.mContext, 2216);
                        Log.d("AvatarWidget", "click not exists avatar, go to unknow/AFW contact infomation");
                        long contactId = ((Contact) AvatarWidget.this.mContactList.get(0)).getPersonId();
                        String str = null;
                        String number = ((Contact) AvatarWidget.this.mContactList.get(0)).getNumber();
                        if (MmsConfig.isEnableAFW()) {
                            Cursor cursor = null;
                            try {
                                cursor = queryEnterpriseContact(number);
                                if (cursor != null && cursor.moveToFirst()) {
                                    contactId = cursor.getLong(0);
                                    str = cursor.getString(5);
                                }
                                if (cursor != null) {
                                    cursor.close();
                                }
                            } catch (Exception e) {
                                Log.e("AvatarWidget", "queryDiretory::query the directoryId exception: " + e);
                                if (cursor != null) {
                                    cursor.close();
                                }
                            } catch (Throwable th) {
                                if (cursor != null) {
                                    cursor.close();
                                }
                            }
                        }
                        if (Contact.isEmailAddress(number)) {
                            if (DirectoryQuery.isEnterpriseContactId(contactId) && MmsConfig.isEnableAFW()) {
                                QuickContact.showQuickContact(AvatarWidget.this.mContext, new Rect(), Contacts.getLookupUri(contactId, str), 3, null);
                            } else {
                                Log.d("AvatarWidget", "click not exists email avatar, do nothing");
                            }
                        } else if (DirectoryQuery.isEnterpriseContactId(contactId) && MmsConfig.isEnableAFW()) {
                            QuickContact.showQuickContact(AvatarWidget.this.mContext, new Rect(), Contacts.getLookupUri(contactId, str), 3, null);
                        } else {
                            Contact contact = (Contact) AvatarWidget.this.mContactList.get(0);
                            if (contact.judgeYpContact(contact, number, AvatarWidget.this.mContext) && contact.isYpContact()) {
                                long ypid = contact.getYellowPageNumberUriId();
                                Uri ypUri = null;
                                if (ypid != -1) {
                                    ypUri = ContentUris.withAppendedId(AvatarWidget.YELLOW_PAGE_URI, ypid);
                                }
                                if (ypUri != null) {
                                    r0 = new Intent("android.intent.action.VIEW", ypUri);
                                    r0.setFlags(67108864);
                                    AvatarWidget.this.mContext.startActivity(r0);
                                    return;
                                }
                            }
                            Intent intent = new Intent();
                            intent.setClassName("com.android.contacts", "com.android.contacts.activities.ContactDetailActivity");
                            intent.putExtra("isFromRcsGroupChat", true);
                            intent.putExtra("nickName", ((Contact) AvatarWidget.this.mContactList.get(0)).getName());
                            intent.putExtra("address", ((Contact) AvatarWidget.this.mContactList.get(0)).getNumber());
                            intent.setFlags(67108864);
                            AvatarWidget.this.mContext.startActivity(intent);
                        }
                    } else if (AvatarWidget.this.mContactList.size() > 1 && !AvatarWidget.this.mIsRcsGroup) {
                        StatisticalHelper.incrementReportCount(AvatarWidget.this.mContext, 2217);
                        Log.d("AvatarWidget", "click group send message, go to detail");
                        r0 = new Intent(AvatarWidget.this.mContext, RecipientListActivity.class);
                        r0.putExtra("recipients", AvatarWidget.this.mContactList.getNumbers());
                        if (HwMessageUtils.isSplitOn()) {
                            Fragment recipientListFragment = new RecipientListFragment();
                            recipientListFragment.setIntent(r0);
                            if (((ConversationList) AvatarWidget.this.mSplitContext).isSplitState()) {
                                ((ConversationList) AvatarWidget.this.mSplitContext).changeRightAddToStack(recipientListFragment);
                            } else {
                                ((ConversationList) AvatarWidget.this.mSplitContext).openRightClearStack(recipientListFragment);
                            }
                        } else {
                            AvatarWidget.this.mContext.startActivity(r0);
                        }
                    } else if (AvatarWidget.this.mContactList.size() > 1 && AvatarWidget.this.mIsRcsGroup) {
                        StatisticalHelper.incrementReportCount(AvatarWidget.this.mContext, 2218);
                        Log.d("AvatarWidget", "click rcs group, go to detail");
                        Conversation conversation = Cache.get(AvatarWidget.this.mContactList);
                        if (conversation == null) {
                            Log.d("AvatarWidget", "click rcs group, but conversation is null");
                            return;
                        }
                        r0 = new Intent(AvatarWidget.this.getContext(), RcsGroupChatConversationDetailActivity.class);
                        r0.putExtra("bundle_group_id", conversation.getHwCust().getGroupId());
                        r0.putExtra("bundle_thread_id", conversation.getHwCust().getGroupChatThreadId());
                        r0.putExtra("bundle_rcs_thread_id", conversation.getThreadId());
                        AvatarWidget.this.isInvalidGroupStatus(conversation.getHwCust().getGroupId(), conversation.getThreadId());
                        if (!AvatarWidget.mIsInvalidGroupStatus) {
                            if (HwMessageUtils.isSplitOn()) {
                                HwBaseFragment fragment = new RcsGroupChatConversationDetailFragment();
                                fragment.setIntent(r0);
                                if (((ConversationList) AvatarWidget.this.mSplitContext).isSplitState()) {
                                    ((ConversationList) AvatarWidget.this.mSplitContext).changeRightAddToStack(fragment);
                                } else {
                                    ((ConversationList) AvatarWidget.this.mSplitContext).openRightClearStack(fragment);
                                }
                            } else {
                                AvatarWidget.this.mContext.startActivity(r0);
                            }
                        }
                    }
                }

                /* JADX WARNING: inconsistent code. */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                private long queryDiretory(Context context) {
                    Cursor directoryCursor = SqliteWrapper.query(context, DirectoryQuery.URI, DirectoryQuery.getProjection(), null, null, "_id");
                    long enterpriseDirectoryId = -1;
                    if (directoryCursor != null) {
                        long directoryId;
                        do {
                            try {
                                if (!directoryCursor.moveToNext()) {
                                    break;
                                }
                                directoryId = directoryCursor.getLong(0);
                            } catch (Exception e) {
                                Log.e("AvatarWidget", "queryDiretory::query the directoryId exception: " + e);
                            } catch (Throwable th) {
                                directoryCursor.close();
                            }
                        } while (!DirectoryQuery.isEnterpriseDirectoryId(directoryId));
                        enterpriseDirectoryId = directoryId;
                        directoryCursor.close();
                    }
                    return enterpriseDirectoryId;
                }

                private Cursor queryEnterpriseContact(String number) {
                    long enterpriseDirectoryId = queryDiretory(AvatarWidget.this.mContext);
                    if (enterpriseDirectoryId <= -1 || TextUtils.isEmpty(number)) {
                        return null;
                    }
                    Builder builder = Uri.withAppendedPath(Phone.ENTERPRISE_CONTENT_FILTER_URI, Uri.encode(number)).buildUpon();
                    builder.appendQueryParameter("search_type", "search_contacts_mms");
                    builder.appendQueryParameter("search_email", "true");
                    builder.appendQueryParameter("directory", String.valueOf(enterpriseDirectoryId));
                    return SqliteWrapper.query(AvatarWidget.this.mContext, builder.build(), DirectoryQuery.getEnterpriseProjection(), null, null, null);
                }
            });
        }
    }

    private void isInvalidGroupStatus(String groupId, long threadId) {
        if (TextUtils.isEmpty(groupId) && threadId == 0) {
            Log.d("AvatarWidget", "group is empty,thread id is 0");
            return;
        }
        Cursor cursor = null;
        if (groupId != null) {
            try {
                cursor = SqliteWrapper.query(getContext(), sGroupUri, null, "name = ?", new String[]{groupId}, null);
            } catch (RuntimeException e) {
                Log.e("AvatarWidget", "cursor unknowable error");
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            cursor = SqliteWrapper.query(getContext(), sGroupUri, null, "thread_id = ?", new String[]{String.valueOf(threadId)}, null);
        }
        if (cursor != null) {
            int status = cursor.getColumnIndexOrThrow("status");
            if (cursor.moveToFirst()) {
                mIsInvalidGroupStatus = RcseMmsExt.checkInvalidGroupStatus(cursor.getInt(status));
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    public boolean needSetBackground() {
        return true;
    }

    private void initRootView() {
        setLayoutParams(new LayoutParams(-1, -1));
        TypedValue outValue = new TypedValue();
        this.mContext.getTheme().resolveAttribute(16842829, outValue, true);
        setOrientation(0);
        this.mContext.getTheme().resolveAttribute(16843534, outValue, true);
        int resid = outValue.resourceId;
        if (needSetBackground()) {
            setBackgroundResource(resid);
        }
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(AvatarWidget.class.getName());
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(AvatarWidget.class.getName());
    }

    private boolean settingDateFormatChange() {
        String dateformat = System.getString(this.mContext.getContentResolver(), "date_format");
        if (this.mDateFormat == null && dateformat != null) {
            this.mDateFormat = dateformat;
            return true;
        } else if (this.mDateFormat == null || this.mDateFormat.equals(dateformat)) {
            return false;
        } else {
            this.mDateFormat = dateformat;
            return true;
        }
    }

    private boolean setting12Or24FormatChange() {
        String timeIs12Or24 = System.getString(this.mContext.getContentResolver(), "time_12_24");
        if (this.mTimeIs12Or24 == null && timeIs12Or24 != null) {
            this.mTimeIs12Or24 = timeIs12Or24;
            return true;
        } else if (this.mTimeIs12Or24 == null || this.mTimeIs12Or24.equals(timeIs12Or24)) {
            return false;
        } else {
            this.mTimeIs12Or24 = timeIs12Or24;
            return true;
        }
    }

    public void setCalendar(Calendar calendar) {
        if (settingDateFormatChange() || setting12Or24FormatChange()) {
            this.mNeedUpdate = true;
        } else {
            this.mNeedUpdate = false;
        }
        if (this.cal == null || !this.cal.equals(calendar) || this.mNeedUpdate) {
            this.cal = calendar;
        }
    }

    public void setTimeMode(Calendar calendar, boolean showWeek) {
        setCalendar(calendar);
        switch (this.mItemType) {
            case 1:
            case 3:
                this.mShowTextModeTime = false;
                this.mTimeMode = getConversationListTimeMode(calendar);
                return;
            case 4:
            case 6:
                this.mShowTextModeTime = true;
                this.mTimeMode = getFavoritesTimeMode(calendar);
                return;
            case 7:
            case 8:
            case 11:
                this.mShowTextModeTime = true;
                this.mTimeMode = getMessageListItemTimeMode(calendar, showWeek);
                return;
            case 9:
                this.mTimeMode = getNoticeListItemTimeMode(calendar, showWeek);
                return;
            default:
                this.mShowTextModeTime = true;
                this.mTimeMode = 3;
                return;
        }
    }

    private boolean isNeedFormatUpdate() {
        if (this.misFirst) {
            this.misFirst = false;
            return true;
        } else if (this.mNeedUpdate) {
            return true;
        } else {
            this.mNeedUpdate = true;
            return false;
        }
    }

    private DateFormat getYearMonthDayFormat(Context context, int timeStyle) {
        return getYearMonthDayFormatForSetting(Locale.getDefault(), this.mDateFormat, timeStyle);
    }

    private DateFormat getYearMonthDayFormatForSetting(Locale locale, String value, int timeStyle) {
        return new SimpleDateFormat(getYearMonthDay(locale, value, timeStyle), locale);
    }

    @SuppressLint({"NewApi"})
    private String getYearMonthDay(Locale locale, String value, int timeStyle) {
        String yearMonthDay = android.text.format.DateFormat.getBestDateTimePattern(locale, "yMd");
        if (timeStyle == 1) {
            yearMonthDay = android.text.format.DateFormat.getBestDateTimePattern(locale, "Md");
        }
        if (value == null) {
            return yearMonthDay;
        }
        int month = value.indexOf(77);
        int day = value.indexOf(100);
        if (month < 0 || day < 0) {
            return yearMonthDay;
        }
        String template = getStringForDate(yearMonthDay);
        switch (timeStyle) {
            case 0:
                if (month >= day) {
                    yearMonthDay = String.format(template, new Object[]{"dd", "MM", "yyyy"});
                    break;
                }
                yearMonthDay = String.format(template, new Object[]{"MM", "dd", "yyyy"});
                break;
            case 1:
                if (month >= day) {
                    yearMonthDay = String.format(template, new Object[]{"dd", "MM"});
                    break;
                }
                yearMonthDay = String.format(template, new Object[]{"MM", "dd"});
                break;
        }
        return yearMonthDay;
    }

    private String getStringForDate(String dateStr) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < dateStr.length()) {
            char c = dateStr.charAt(i);
            if (i == 0 || (i > 0 && c != dateStr.charAt(i - 1))) {
                if (c == 'M' || c == 'd' || c == 'y') {
                    sb.append("%s");
                } else {
                    sb.append(c);
                }
            }
            i++;
        }
        return sb.toString();
    }

    protected void setTime(long temestamp, int mode) {
    }

    protected int getConversationListTimeMode(Calendar cal) {
        Calendar now = MessageUtils.getCalendar();
        now.setTimeInMillis(System.currentTimeMillis());
        if (cal.get(1) == now.get(1) && cal.get(6) == now.get(6)) {
            if (cal.get(11) == now.get(11) && cal.get(12) == now.get(12)) {
                return 5;
            }
            if (cal.get(11) == now.get(11) || (cal.get(11) == now.get(11) - 1 && cal.get(12) > now.get(12))) {
                return 6;
            }
            return 2;
        } else if (cal.get(1) == now.get(1) && cal.get(6) == now.get(6) - 1) {
            return 7;
        } else {
            if (cal.get(1) == now.get(1)) {
                return 8;
            }
            return 1;
        }
    }

    protected int getMessageListItemTimeMode(Calendar cal, boolean showWeek) {
        Calendar now = MessageUtils.getCalendar();
        now.setTimeInMillis(System.currentTimeMillis());
        if (!showWeek) {
            if (cal.get(1) == now.get(1) && cal.get(6) == now.get(6)) {
                if (cal.get(11) == now.get(11) && cal.get(12) == now.get(12)) {
                    return 5;
                }
                if (cal.get(11) == now.get(11) || (cal.get(11) == now.get(11) - 1 && cal.get(12) > now.get(12))) {
                    return 6;
                }
            }
            return 2;
        } else if (cal.get(1) == now.get(1) && cal.get(6) == now.get(6)) {
            return 14;
        } else {
            if (cal.get(1) == now.get(1) && cal.get(6) == now.get(6) - 1) {
                return 13;
            }
            if (cal.get(1) == now.get(1)) {
                return 15;
            }
            return 12;
        }
    }

    protected int getNoticeListItemTimeMode(Calendar cal, boolean showWeek) {
        Calendar now = MessageUtils.getCalendar();
        now.setTimeInMillis(System.currentTimeMillis());
        if (!showWeek) {
            return 2;
        }
        if (cal.get(1) == now.get(1) && cal.get(6) == now.get(6)) {
            return 18;
        }
        if (cal.get(1) == now.get(1) && cal.get(6) == now.get(6) - 1) {
            return 17;
        }
        if (cal.get(1) == now.get(1)) {
            return 19;
        }
        return 16;
    }

    protected int getFavoritesTimeMode(Calendar cal) {
        Calendar now = MessageUtils.getCalendar();
        now.setTimeInMillis(System.currentTimeMillis());
        if (cal.get(1) == now.get(1) && cal.get(6) == now.get(6)) {
            return 9;
        }
        if (cal.get(1) == now.get(1) && cal.get(6) == now.get(6) - 1) {
            return 10;
        }
        if (cal.get(1) == now.get(1)) {
            return 11;
        }
        return 4;
    }

    protected int getContentResId() {
        return -1;
    }

    public void setScaleX(float scaleX) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).setScaleX(scaleX);
        }
    }

    public void setScaleY(float scaleY) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).setScaleY(scaleY);
        }
    }

    public void updateAvatarIcon(ContactList contactList, boolean isRcsGroupChat) {
        if (!OsUtil.IS_EMUI_LITE) {
            this.mContactList = contactList;
            this.mIsRcsGroup = isRcsGroupChat;
            if (contactList != null && contactList.size() == 1) {
                updateSingleAvatarView(contactList);
                updateSingleAvatarDescription(contactList);
            } else if (contactList != null && contactList.size() > 1) {
                if (!(this.mItemType == 3 && isRcsGroupChat)) {
                    this.mRecvAvatarView.setImageDrawable(ResEx.self().getAvtarDefault(null));
                }
                if (isRcsGroupChat) {
                    updateRcsGroupAvatarView(contactList);
                } else {
                    updateGroupAvatarView(contactList);
                }
                this.mRecvAvatarView.setContentDescription(getContext().getString(R.string.group_avatar_description));
            } else if (this.mItemType != 3 || !isRcsGroupChat) {
                this.mRecvAvatarView.setImageDrawable(ResEx.self().getAvtarDefault(null));
            }
        }
    }

    private void updateSingleAvatarDescription(ContactList contactList) {
        if (contactList.contains(Contact.getMe(false))) {
            this.mRecvAvatarView.setContentDescription(getContext().getString(R.string.my_avatar));
        } else {
            this.mRecvAvatarView.setContentDescription(getContext().getString(R.string.description_avatar_photo));
        }
    }

    public void updateAvatarIcon(ContactList contactList, boolean isRcsGroupChat, boolean isScrolling, Conversation con, int type) {
        if (!OsUtil.IS_EMUI_LITE) {
            this.mContactList = contactList;
            this.mIsRcsGroup = isRcsGroupChat;
            if (contactList != null && contactList.size() == 1) {
                Drawable drawable = null;
                switch (type) {
                    case 1:
                        drawable = ResEx.self().getHwNotificationDrawable(con.getThreadId());
                        break;
                    default:
                        if (!((Contact) contactList.get(0)).isXiaoyuanContact()) {
                            drawable = ResEx.self().getContactDrawable(con.getThreadId());
                            break;
                        }
                        break;
                }
                if (drawable != null) {
                    this.mRecvAvatarView.setImageDrawable(drawable);
                } else {
                    updateSingleAvatarView(contactList, con, isScrolling);
                }
                this.mRecvAvatarView.setContentDescription(getContext().getString(R.string.description_avatar_photo));
            } else if (contactList != null && contactList.size() > 1) {
                if (!(this.mItemType == 3 && isRcsGroupChat)) {
                    this.mRecvAvatarView.setImageDrawable(ResEx.self().getAvtarDefault(null));
                }
                if (isRcsGroupChat) {
                    updateRcsGroupAvatarView(contactList);
                } else {
                    updateGroupAvatarView(contactList);
                }
                this.mRecvAvatarView.setContentDescription(getContext().getString(R.string.group_avatar_description));
            } else if (!(this.mItemType == 3 && isRcsGroupChat)) {
                this.mRecvAvatarView.setImageDrawable(MessagingNotification.getDefaultAvatar(this.mContext, null, con));
            }
            this.mRecvAvatarView.setFocusable(true);
            this.mRecvAvatarView.setClickable(true);
        }
    }

    public void updateFavoritesInSearchAvatarIcon() {
        if (!OsUtil.IS_EMUI_LITE) {
            this.mContactList = null;
            this.mRecvAvatarView.setImageDrawable(ResEx.self().getAvtarDefault(null, -3));
        }
    }

    public void updateNotificationAvatarIcon() {
        if (!OsUtil.IS_EMUI_LITE) {
            this.mContactList = null;
            this.mRecvAvatarView.setImageDrawable(ResEx.self().getAvtarDefault(null, -2));
            this.mRecvAvatarView.setContentDescription(".");
            this.mRecvAvatarView.setFocusable(false);
            this.mRecvAvatarView.setClickable(false);
        }
    }

    public void updateHuaweiNotificationAvatarIcon() {
        if (!OsUtil.IS_EMUI_LITE) {
            this.mContactList = null;
            this.mRecvAvatarView.setImageDrawable(ResEx.self().getAvtarDefault(null, -4));
            this.mRecvAvatarView.setContentDescription(".");
            this.mRecvAvatarView.setFocusable(false);
            this.mRecvAvatarView.setClickable(false);
        }
    }

    public void updateAvatarIcon(String address) {
        ContactList contactList = new ContactList();
        contactList.add(Contact.get(address, false));
        updateAvatarIcon(contactList, false);
    }

    public void updateAvatarIcon(String address, String name, long contactId, String lookupKey) {
        ContactList contactList = new ContactList();
        contactList.add(Contact.get(contactId, 0, address, name, lookupKey));
        updateAvatarIcon(contactList, false);
    }

    public void updateMyAvatarIcon() {
        ContactList contactList = new ContactList();
        contactList.add(Contact.getMe(false));
        updateAvatarIcon(contactList, false);
    }

    private void updateRcsGroupAvatarView(ContactList contactList) {
        ContactList cl = new ContactList();
        cl.addAll(contactList.subList(1, contactList.size()));
        updateGroupAvatarView(cl);
    }

    private void updateGroupAvatarView(ContactList contactList) {
        if (!OsUtil.IS_EMUI_LITE) {
            final List<Drawable> drawableList = new ArrayList();
            final List<Boolean> isDefaultAvatarList = new ArrayList();
            final List<Integer> defaultAvatarColorList = new ArrayList();
            int listSize = contactList.size();
            final int groupAvatarSize = listSize >= 3 ? 3 : listSize;
            this.mGroupAvatarReadyCount = 0;
            for (int i = 0; i < groupAvatarSize; i++) {
                final Contact c = (Contact) contactList.get(i);
                final Drawable defaultAvtar = ResEx.self().getAvtarDefault(c);
                setAvatarImage(c, defaultAvtar, new ItemLoadedCallback<Drawable>() {
                    public void onItemLoaded(Drawable result, Throwable exception) {
                        AvatarWidget.this.setGroupAvatarDrawable(drawableList, isDefaultAvatarList, defaultAvatarColorList, groupAvatarSize, defaultAvtar, result, c);
                    }
                }, false);
            }
        }
    }

    private void setGroupAvatarDrawable(List<Drawable> drawableList, List<Boolean> isDefaultAvatarList, List<Integer> defaultAvatarColorList, int groupAvatarSize, Drawable defaultAvtar, Drawable result, Contact contact) {
        Object obj;
        if (result == null) {
            obj = defaultAvtar;
        } else {
            Drawable drawable = result;
        }
        drawableList.add(obj);
        isDefaultAvatarList.add(Boolean.valueOf(result == defaultAvtar));
        defaultAvatarColorList.add(Integer.valueOf(AvatarCache.pickColor(contact)));
        this.mGroupAvatarReadyCount++;
        if (this.mGroupAvatarReadyCount == groupAvatarSize) {
            this.mRecvAvatarView.setImageDrawable(buildTotalDrawable(drawableList, isDefaultAvatarList, defaultAvatarColorList));
        }
    }

    private Drawable buildTotalDrawable(List<Drawable> drawableList, List<Boolean> isDefaultAvatarList, List<Integer> defaultAvatarColorList) {
        return buildTotalDrawable(drawableList, isDefaultAvatarList, defaultAvatarColorList, false);
    }

    private Drawable buildTotalDrawable(List<Drawable> drawableList, List<Boolean> isDefaultAvatarList, List<Integer> defaultAvatarColorList, boolean buildDefaultAvatar) {
        List<RectF> targetRects = buildDefaultAvatar ? getDefaultAvatarRects() : getChildTargetRects(drawableList);
        Bitmap totalBitmap = Bitmap.createBitmap(this.mAvatarWeight, this.mAvatarWeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(totalBitmap);
        Paint paint = new Paint(1);
        int size = drawableList.size();
        for (int i = 0; i < size; i++) {
            RectF avatarDestOnGroup = (RectF) targetRects.get(i);
            if (avatarDestOnGroup != null) {
                Bitmap resourceBitmap = MessageUtils.drawable2Bitmap((Drawable) drawableList.get((size - i) - 1));
                RectF resourceRect = new RectF(0.0f, 0.0f, (float) resourceBitmap.getWidth(), (float) resourceBitmap.getHeight());
                Bitmap smallCircleBitmap = Bitmap.createBitmap(Math.round(avatarDestOnGroup.width()), Math.round(avatarDestOnGroup.height()), Config.ARGB_8888);
                RectF smallCircleRect = new RectF(0.0f, 0.0f, (float) smallCircleBitmap.getWidth(), (float) smallCircleBitmap.getHeight());
                drawBitmapWithCircleOnCanvas(resourceBitmap, new Canvas(smallCircleBitmap), resourceRect, smallCircleRect, new RectF(0.0f, 0.0f, (float) ((smallCircleBitmap.getWidth() - (this.mGroupAvatarWhiteStrokeWidth * 2)) + 1), (float) ((smallCircleBitmap.getHeight() - (this.mGroupAvatarWhiteStrokeWidth * 2)) + 1)), null, size, i, buildDefaultAvatar, ((Integer) defaultAvatarColorList.get((size - i) - 1)).intValue(), ((Boolean) isDefaultAvatarList.get((size - i) - 1)).booleanValue());
                Matrix matrix = new Matrix();
                matrix.setRectToRect(smallCircleRect, avatarDestOnGroup, ScaleToFit.FILL);
                canvas.drawBitmap(smallCircleBitmap, matrix, paint);
            }
        }
        return new BitmapDrawable(this.mContext.getResources(), totalBitmap);
    }

    public void drawBitmapWithCircleOnCanvas(Bitmap bitmap, Canvas canvas, RectF source, RectF dest, RectF destInner, Paint bitmapPaint, int listSize, int index, boolean buildDefaultAvatar, int avatarBgColor, boolean isDefaultAvatar) {
        Matrix matrix = new Matrix();
        destInner.left = (destInner.left + ((float) this.mGroupAvatarWhiteStrokeWidth)) - 0.5f;
        destInner.top = (destInner.top + ((float) this.mGroupAvatarWhiteStrokeWidth)) - 0.5f;
        destInner.right = (destInner.right + ((float) this.mGroupAvatarWhiteStrokeWidth)) - 0.5f;
        destInner.bottom = (destInner.bottom + ((float) this.mGroupAvatarWhiteStrokeWidth)) - 0.5f;
        matrix.setRectToRect(source, destInner, ScaleToFit.CENTER);
        if (bitmapPaint == null) {
            bitmapPaint = new Paint();
        }
        bitmapPaint.setAntiAlias(true);
        bitmapPaint.setFilterBitmap(true);
        canvas.drawBitmap(bitmap, matrix, bitmapPaint);
        if (isDefaultAvatar) {
            bitmapPaint.setStyle(Style.STROKE);
            bitmapPaint.setStrokeWidth((float) this.mGroupAvatarWhiteStrokeWidth);
            bitmapPaint.setColor(avatarBgColor);
            canvas.drawCircle(dest.centerX(), dest.centerY(), (dest.width() / 2.0f) - 3.0f, bitmapPaint);
        }
        if (!buildDefaultAvatar) {
            bitmapPaint.setStyle(Style.STROKE);
            bitmapPaint.setStrokeWidth((float) this.mGroupAvatarWhiteStrokeWidth);
            bitmapPaint.setColor(this.mGroupAvatarStrokeColor);
            canvas.drawCircle(dest.centerX(), dest.centerY(), (dest.width() / 2.0f) - 1.5f, bitmapPaint);
        }
    }

    private List<RectF> getChildTargetRects(List<Drawable> drawableList) {
        int groupSize = drawableList.size();
        float width = (float) this.mAvatarWeight;
        float height = (float) this.mAvatarWeight;
        float halfWidth = width / 2.0f;
        float halfHeight = height / 2.0f;
        RectF[] destArray = new RectF[groupSize];
        switch (groupSize) {
            case 1:
                destArray[0] = new RectF(0.0f, 0.0f, width, height);
                break;
            case 2:
                destArray[0] = new RectF((2.0f * halfWidth) / 3.0f, halfHeight / 3.0f, width, (halfHeight * 5.0f) / 3.0f);
                destArray[1] = new RectF(0.0f, halfHeight / 3.0f, (4.0f * halfWidth) / 3.0f, (halfHeight * 5.0f) / 3.0f);
                break;
            default:
                destArray[0] = new RectF((2.0f * halfWidth) / 3.0f, halfHeight / 3.0f, width, (halfHeight * 5.0f) / 3.0f);
                destArray[1] = new RectF(halfWidth / 3.0f, halfHeight / 3.0f, (halfWidth * 5.0f) / 3.0f, (halfHeight * 5.0f) / 3.0f);
                destArray[2] = new RectF(0.0f, halfHeight / 3.0f, (4.0f * halfWidth) / 3.0f, (halfHeight * 5.0f) / 3.0f);
                break;
        }
        return Arrays.asList(destArray);
    }

    private List<RectF> getDefaultAvatarRects() {
        float width = (float) this.mAvatarWeight;
        float height = (float) this.mAvatarWeight;
        return Arrays.asList(new RectF[]{new RectF(0.0f, 0.0f, width, height), new RectF(width / 5.0f, height / 5.0f, (width * 4.0f) / 5.0f, (4.0f * height) / 5.0f)});
    }

    public void updateSingleAvatarView(ContactList contactList) {
        if (!OsUtil.IS_EMUI_LITE) {
            updateSingleAvatarView(contactList, Cache.get(contactList), false);
        }
    }

    private void updateSingleAvatarView(ContactList contactList, final Conversation conv, boolean isScrolling) {
        Contact contact = null;
        if (contactList != null && contactList.size() > 0) {
            contact = (Contact) contactList.get(0);
        }
        if (conv != null && conv.getPhoneType() == 1) {
            this.mRecvAvatarView.setImageDrawable(ResEx.self().getAvtarDefault(contact, -4));
        } else if (contact == null || !contact.isXyHwNumber()) {
            if (isScrolling) {
                this.mRecvAvatarView.setImageDrawable(MessagingNotification.getDefaultAvatar(this.mContext, contact, conv));
            } else {
                final boolean isNotification = contact.isXiaoyuanContact();
                final Drawable defaultAvtar = MessagingNotification.getDefaultAvatar(this.mContext, contact, conv);
                setAvatarImage(contact, defaultAvtar, new ItemLoadedCallback<Drawable>() {
                    public void onItemLoaded(Drawable result, Throwable exception) {
                        if (result != null) {
                            AvatarWidget.this.mRecvAvatarView.setImageDrawable(result);
                            if (conv != null && !isNotification) {
                                ResEx.self().putContactDrawable(conv.getThreadId(), result);
                            }
                        } else if (defaultAvtar != null) {
                            AvatarWidget.this.mRecvAvatarView.setImageDrawable(defaultAvtar);
                        }
                    }
                }, isNotification);
            }
        } else {
            this.mRecvAvatarView.setImageDrawable(ResEx.self().getAvtarDefault(contact, -4));
        }
    }

    private void setAvatarImage(Contact c, Drawable defaultAvtar, ItemLoadedCallback<Drawable> loadedCallback, boolean isNotification) {
        if (c != null) {
            c.parseAvatarImage(this.mContext, defaultAvtar, loadedCallback, false, false, isNotification);
        }
        if (mHwCustEcidLookup != null && mHwCustEcidLookup.getNameIdFeatureEnable() && c.getPersonId() == 0) {
            AvatarCache.instance().setAvatar(this.mContext, c.isMe(), c.getPersonId(), false, mHwCustEcidLookup.getEcidDrawableIfExists(getContext(), c.getNumber(), defaultAvtar), (ItemLoadedCallback) loadedCallback, c);
        }
    }

    protected void updateUnreadIcon(boolean unreadMode) {
        this.mUnreadIcon.setVisibility(unreadMode ? 0 : 4);
    }

    public CharSequence buildTime(long date) {
        return buildTime(date, false);
    }

    public CharSequence buildTime(long date, boolean showWeek) {
        Calendar calendar = MessageUtils.getCalendar();
        calendar.setTimeInMillis(date);
        setTimeMode(calendar, showWeek);
        if (this.mShowTextModeTime) {
            return getTextFormatTime(date, calendar);
        }
        return getNumberFormatTime(date, calendar);
    }

    private String getNumberFormatTime(long date, Calendar calendar) {
        String time = null;
        if (isNeedFormatUpdate()) {
            this.dYearMonthDay = getYearMonthDayFormat(this.mContext, 0);
            this.dMonthDay = getYearMonthDayFormat(this.mContext, 1);
        }
        this.dYearMonthDay.setCalendar(calendar);
        String yearMonthDayStr = this.dYearMonthDay.format(calendar.getTime());
        this.dMonthDay.setCalendar(calendar);
        String monthDayStr = this.dMonthDay.format(calendar.getTime());
        String timeStr = DateUtilsEx.formatChinaDateRange(this.mContext, new Formatter(new StringBuilder(50), Locale.getDefault()), date, date, 1);
        int formatterFlag = -1;
        switch (this.mTimeMode) {
            case 1:
                time = yearMonthDayStr;
                break;
            case 2:
                time = timeStr;
                break;
            case 4:
                time = yearMonthDayStr + " " + timeStr;
                break;
            case 5:
                time = this.mContext.getResources().getString(R.string.time_format_just_now);
                break;
            case 6:
                time = getMinuteAgo(calendar);
                if (TextUtils.isEmpty(time)) {
                    time = timeStr;
                    break;
                }
                break;
            case 7:
                time = this.mContext.getResources().getString(R.string.time_format_yesterday);
                break;
            case 8:
                time = monthDayStr;
                break;
            case 9:
                time = this.mContext.getResources().getString(R.string.mms_today) + " " + timeStr;
                break;
            case 10:
                time = this.mContext.getResources().getString(R.string.time_format_yesterday) + " " + timeStr;
                break;
            case 11:
                time = monthDayStr + " " + timeStr;
                break;
            case 12:
                time = yearMonthDayStr;
                formatterFlag = 2;
                break;
            case 13:
                time = this.mContext.getResources().getString(R.string.time_format_yesterday);
                formatterFlag = 2;
                break;
            case 14:
                time = this.mContext.getResources().getString(R.string.mms_today);
                formatterFlag = 2;
                break;
            case 15:
                time = monthDayStr;
                formatterFlag = 2;
                break;
            default:
                formatterFlag = -1;
                break;
        }
        if (formatterFlag != -1 && time == null) {
            return DateUtilsEx.formatChinaDateRange(this.mContext, new Formatter(new StringBuilder(50), Locale.getDefault()), date, date, formatterFlag);
        } else if (formatterFlag == -1) {
            return time;
        } else {
            return time + " " + DateUtilsEx.formatChinaDateRange(this.mContext, new Formatter(new StringBuilder(50), Locale.getDefault()), date, date, formatterFlag);
        }
    }

    private String getTextFormatTime(long date, Calendar calendar) {
        String time = null;
        Formatter formatter = new Formatter(new StringBuilder(50), Locale.getDefault());
        int formatterFlag = -1;
        switch (this.mTimeMode) {
            case 1:
                formatterFlag = 4;
                break;
            case 2:
                formatterFlag = 1;
                break;
            case 4:
                formatterFlag = 21;
                break;
            case 5:
                time = this.mContext.getResources().getString(R.string.time_format_just_now);
                break;
            case 6:
                time = getMinuteAgo(calendar);
                if (TextUtils.isEmpty(time)) {
                    formatterFlag = 1;
                    break;
                }
                break;
            case 7:
                time = this.mContext.getResources().getString(R.string.time_format_yesterday);
                break;
            case 8:
                formatterFlag = 16;
                break;
            case 9:
                time = this.mContext.getResources().getString(R.string.mms_today);
                formatterFlag = 17;
                break;
            case 10:
                time = this.mContext.getResources().getString(R.string.time_format_yesterday);
                formatterFlag = 17;
                break;
            case 11:
                formatterFlag = 17;
                break;
            case 12:
                formatterFlag = 22;
                break;
            case 13:
                time = this.mContext.getResources().getString(R.string.time_format_yesterday);
                formatterFlag = 18;
                break;
            case 14:
                time = this.mContext.getResources().getString(R.string.mms_today);
                formatterFlag = 18;
                break;
            case 15:
                formatterFlag = 18;
                break;
            case 16:
                formatterFlag = 23;
                break;
            case 17:
                time = this.mContext.getResources().getString(R.string.time_format_yesterday);
                formatterFlag = 19;
                break;
            case 18:
                time = this.mContext.getResources().getString(R.string.mms_today);
                formatterFlag = 19;
                break;
            case 19:
                formatterFlag = 19;
                break;
            default:
                formatterFlag = -1;
                break;
        }
        if (formatterFlag != -1 && time == null) {
            return DateUtilsEx.formatChinaDateRange(this.mContext, formatter, date, date, formatterFlag);
        }
        if (formatterFlag == -1) {
            return time;
        }
        return DateUtilsEx.formatChinaDateRange(this.mContext, formatter, date, date, formatterFlag).replaceAll(DateUtilsEx.formatChinaDateRange(this.mContext, new Formatter(new StringBuilder(50), Locale.getDefault()), date, date, 16), time);
    }

    private String getMinuteAgo(Calendar calendar) {
        int timeAgo;
        Calendar now = MessageUtils.getCalendar();
        now.setTimeInMillis(System.currentTimeMillis());
        if (calendar.get(11) == now.get(11)) {
            timeAgo = now.get(12) - calendar.get(12);
        } else {
            timeAgo = (now.get(12) - calendar.get(12)) + 60;
        }
        if (timeAgo <= 0) {
            return null;
        }
        return this.mContext.getResources().getQuantityString(R.plurals.time_format_minute_ago, timeAgo, new Object[]{Integer.valueOf(timeAgo)});
    }
}
