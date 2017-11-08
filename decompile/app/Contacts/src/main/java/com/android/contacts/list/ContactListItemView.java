package com.android.contacts.list;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView.SelectionBoundsAdjuster;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import com.android.contacts.ContactPresenceIconUtil;
import com.android.contacts.ContactStatusUtil;
import com.android.contacts.ContactsUtils;
import com.android.contacts.R$styleable;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.format.PrefixHighlighter;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.hwsearch.HwSearchCursor;
import com.android.contacts.hap.rcs.list.RcsContactListItemView;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.hap.util.MultiUsersUtils;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.hap.widget.ShadowView;
import com.android.contacts.util.AccountFilterUtil;
import com.android.contacts.util.Constants;
import com.android.contacts.util.ContactDisplayUtils;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SearchUtil;
import com.android.contacts.util.SearchUtil.MatchedLine;
import com.android.contacts.util.TextUtil;
import com.google.android.gms.R;
import com.google.android.gms.location.places.Place;
import com.google.common.collect.Lists;
import com.huawei.cspcommon.util.DialerHighlighter;
import com.huawei.cspcommon.util.HanziToPinyin.Token;
import com.huawei.cspcommon.util.PhoneItem;
import com.huawei.cspcommon.util.SearchMatch;
import com.huawei.cspcommon.util.SortUtils;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContactListItemView extends ViewGroup implements SelectionBoundsAdjuster, Checkable {
    public static final PhotoPosition DEFAULT_PHOTO_POSITION = PhotoPosition.LEFT;
    private static final Pattern SPLIT_PATTERN = Pattern.compile("([\\w-\\.]+)@((?:[\\w]+\\.)+)([a-zA-Z]{2,4})|[\\w]+");
    private static String mEllipsis = "…";
    private boolean isDisplayAlph = false;
    private boolean isOrgnizationSnippet;
    private int mAccIconWidth;
    Bitmap[] mAccIconsInfo = new Bitmap[0];
    private int mAccIndicatorGapFromEnd;
    private TextView mAccountFilterView;
    ImageView[] mAccountIcons;
    String[] mAccountTypeDescriptions = new String[0];
    private Drawable mActivatedBackgroundDrawable;
    private boolean mActivatedStateSupported;
    private int mAlphTextColor;
    private int mAlphTextSize;
    private int mAlphTextWidth;
    private TextView mAlphabetIndexView;
    private int mAlphalbetHeight;
    private Rect mBoundsWithoutHeader = new Rect();
    protected CheckBox mCheckBox;
    private final int mCheckBoxPadding;
    private int mCheckWidth;
    private int mCompanyTextViewHeight;
    private TextView mCompanyView;
    private int mContactListItemPaddingBottom;
    private int mContactListItemPaddingTop;
    protected int mContentPaddingLeft;
    private int mContentPaddingLeftForSimpleMode;
    protected int mContentPaddingRight;
    private String mCount;
    private int mCustomSearchHighlightColor;
    private final CharArrayBuffer mDataBuffer = new CharArrayBuffer(128);
    private TextView mDataView;
    private int mDataViewHeight;
    private final int mDataViewWidthWeight;
    private int mDefaultPhotoViewSize;
    private DialerHighlighter mDialerHighlighter;
    private ImageView mDragIcon;
    private int mDragIconHeight;
    private int mDragIconWidth;
    private int mDragListViewItemDoubleHeight;
    private int mDragListViewItemPaddingLeft;
    private boolean mFromFavFreEditFragment;
    private int mGapBetweenAccIcons;
    private final int mGapBetweenImageAndText;
    private final int mGapBetweenLabelAndData;
    private final int mGroupHeaderTextIndent;
    private final int mGroupHeaderTextIndentMirror;
    private int mHDividerPaddingEnd;
    private int mHDividerPaddingStart;
    private final int mHeaderBackgroundColor;
    private int mHeaderBackgroundHeight;
    private View mHeaderDivider;
    private Drawable mHeaderDividerDrable;
    private View mHeaderSectionView;
    private final int mHeaderTextColor;
    private final int mHeaderTextIndent;
    private boolean mHeaderTextLeftIndent = false;
    private boolean mHeaderTextNoLeftIndent = false;
    private final int mHeaderTextPaddingBottom;
    private final int mHeaderTextPaddingTop;
    private final int mHeaderTextSize;
    private TextView mHeaderTextView;
    private final int mHeaderUnderlineHeight;
    private boolean mHeaderVisible;
    private char[] mHighlightedPrefix;
    private Drawable mHorizontalDividerDrawable;
    private int mHorizontalDividerHeight;
    private boolean mHorizontalDividerVisible = true;
    protected boolean mIsChecked;
    private boolean mIsDisplayDragIcon = false;
    protected boolean mIsMirror;
    private boolean mIsSimAccount;
    private boolean mIsSimAccountIndDisplayEnabled = EmuiFeatureManager.isSimAccountIndicatorEnabled();
    private boolean mKeepHorizontalPaddingForPhotoView;
    private boolean mKeepVerticalPaddingForPhotoView;
    private int mLabelAndDataViewMaxHeight;
    private TextView mLabelView;
    private int mLabelViewHeight;
    private final int mLabelViewWidthWeight;
    private final int mLandCheckBoxPadding;
    private int mLeftPadding;
    private int mListItemDoubleLineHeight;
    private int mListItemSingleLineHeight;
    private int[] mMatchTypeArray = null;
    private boolean mMultiSearchContacts = false;
    private TextView mNameTextView;
    private int mNameTextViewHeight;
    private ImageView mOverlayView;
    private final CharArrayBuffer mPhoneticNameBuffer = new CharArrayBuffer(128);
    private TextView mPhoneticNameTextView;
    private int mPhoneticNameTextViewHeight;
    private PhotoPosition mPhotoPosition = DEFAULT_PHOTO_POSITION;
    private ImageView mPhotoView;
    private int mPhotoViewHeight;
    private int mPhotoViewWidth;
    private boolean mPhotoViewWidthAndHeightAreReady = false;
    private int mPreferredHeight;
    private PrefixHighlighter mPrefixHighligher;
    private ImageView mPresenceIcon;
    private final int mPresenceIconMargin;
    private final int mPresenceIconSize;
    private int mPrimaryTextColor;
    private int mPrimaryTextSize;
    private RcsContactListItemView mRcsCust = null;
    private int mRightPading;
    private int mSearchMatchType = 40;
    private int mSecondaryTextColor;
    private int mSecondaryTextSize;
    private String mSectionTitle;
    private int mSelectionBoundsMarginLeft;
    private int mSelectionBoundsMarginRight;
    private View mShadowView;
    private ImageView mSimAccountIndicator;
    private String mSimAccountIndicatorContentDescription;
    private Bitmap mSimAccountIndicatorInfo;
    private int mSnippetAndCompanyViewMaxHeight;
    private int mSnippetMatchPosition = 40;
    private int mSnippetMatchType = 40;
    private int mSnippetTextViewHeight;
    private TextView mSnippetView;
    private SpannableString mSpannable;
    private int mStatusTextViewHeight;
    private TextView mStatusView;
    private boolean mTextHighlighted;
    private final int mTextIndent;
    private CharSequence mUnknownNameText;
    private ImageView mWorkProfileIcon;

    public enum PhotoPosition {
        LEFT,
        RIGHT
    }

    public void setSimAccountIndicatorContentDescription(String simAccountIndicatorContentDescription) {
        this.mSimAccountIndicatorContentDescription = simAccountIndicatorContentDescription;
    }

    public ContactListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources lres = getResources();
        TypedArray a = getContext().obtainStyledAttributes(attrs, R$styleable.ContactListItemView);
        this.mActivatedBackgroundDrawable = a.getDrawable(2);
        this.mHorizontalDividerDrawable = a.getDrawable(5);
        this.mHeaderDividerDrable = a.getDrawable(5);
        this.mGapBetweenImageAndText = a.getDimensionPixelOffset(10, 0);
        this.mGapBetweenLabelAndData = a.getDimensionPixelOffset(11, 0);
        this.mCheckBoxPadding = a.getDimensionPixelOffset(13, 0);
        this.mLandCheckBoxPadding = a.getDimensionPixelOffset(14, 0);
        this.mPresenceIconMargin = a.getDimensionPixelOffset(16, 4);
        this.mPresenceIconSize = a.getDimensionPixelOffset(17, 16);
        this.mDefaultPhotoViewSize = a.getDimensionPixelOffset(20, 0);
        this.mHeaderTextIndent = a.getDimensionPixelOffset(23, 0);
        this.mHeaderTextPaddingBottom = lres.getDimensionPixelOffset(R.dimen.list_item_header_text_padding_bottom);
        this.mHeaderTextPaddingTop = lres.getDimensionPixelOffset(R.dimen.list_item_header_text_padding_top);
        this.mGroupHeaderTextIndent = a.getDimensionPixelOffset(24, 0);
        this.mGroupHeaderTextIndentMirror = Float.valueOf(getResources().getDimension(R.dimen.contact_header_view_mirror_indent)).intValue();
        this.mHeaderTextColor = lres.getColor(R.color.contact_section_title_text_color);
        this.mHeaderTextSize = a.getDimensionPixelSize(26, 13);
        this.mHeaderBackgroundHeight = a.getDimensionPixelSize(27, 32);
        this.mHeaderUnderlineHeight = Float.valueOf(getResources().getDimension(R.dimen.contact_list_divider_height)).intValue();
        this.mHeaderBackgroundColor = lres.getColor(R.color.contact_section_background_color);
        this.mTextIndent = a.getDimensionPixelOffset(31, 0);
        this.mDataViewWidthWeight = a.getInteger(33, 5);
        this.mLabelViewWidthWeight = a.getInteger(34, 3);
        this.mCustomSearchHighlightColor = lres.getColor(R.color.searchhint_people);
        this.mContactListItemPaddingTop = context.getResources().getDimensionPixelSize(R.dimen.contact_list_item_padding_top);
        this.mContactListItemPaddingBottom = context.getResources().getDimensionPixelSize(R.dimen.contact_list_item_padding_bottom);
        this.mListItemDoubleLineHeight = context.getResources().getDimensionPixelSize(R.dimen.list_item_double_line_height);
        this.mListItemSingleLineHeight = context.getResources().getDimensionPixelSize(R.dimen.list_item_single_line_height);
        this.mContentPaddingLeftForSimpleMode = context.getResources().getDimensionPixelSize(R.dimen.contact_list_item_content_padding_left_simple);
        this.mLeftPadding = a.getDimensionPixelOffset(18, 0);
        this.mRightPading = a.getDimensionPixelOffset(19, 0);
        setBackgroundColor(3);
        this.mIsMirror = CommonUtilMethods.isLayoutRTL();
        if (this.mIsMirror) {
            this.mPhotoPosition = PhotoPosition.RIGHT;
            this.mContentPaddingRight = this.mLeftPadding;
            this.mContentPaddingLeft = this.mRightPading;
        } else {
            this.mPhotoPosition = PhotoPosition.LEFT;
            this.mContentPaddingLeft = this.mLeftPadding;
            this.mContentPaddingRight = this.mRightPading;
        }
        int color = ImmersionUtils.getControlColor(lres);
        if (color != 0) {
            this.mPrefixHighligher = new PrefixHighlighter(color);
        } else {
            this.mPrefixHighligher = new PrefixHighlighter(lres.getColor(R.color.people_app_theme_color));
        }
        a.recycle();
        this.mSecondaryTextColor = lres.getColor(R.color.contact_list_item_sub_text_color);
        this.mPrimaryTextColor = lres.getColor(R.color.contact_list_item_text_color);
        this.mAlphTextColor = lres.getColor(R.color.contact_list_item_sub_text_color);
        this.mAlphTextSize = lres.getDimensionPixelSize(R.dimen.contact_list_item_alphtext_size);
        this.mAlphTextWidth = lres.getDimensionPixelSize(R.dimen.contact_list_item_alphtextheight_size);
        this.mDragIconWidth = lres.getDimensionPixelSize(R.dimen.drag_list_view_icon_width);
        this.mDragListViewItemDoubleHeight = lres.getDimensionPixelSize(R.dimen.drag_list_view_double_line_height);
        this.mDragListViewItemPaddingLeft = lres.getDimensionPixelSize(R.dimen.drag_list_view_icon_left_margin);
        this.mSecondaryTextSize = lres.getDimensionPixelSize(R.dimen.contact_list_item_second_line_text_size);
        this.mPrimaryTextSize = lres.getDimensionPixelSize(R.dimen.contact_list_item_first_line_text_size);
        this.mGapBetweenAccIcons = Float.valueOf(getResources().getDimension(R.dimen.ContactListItemView_gap_between_acc_icons)).intValue();
        this.mAccIndicatorGapFromEnd = Float.valueOf(getResources().getDimension(R.dimen.acc_indicator_gap_from_end)).intValue();
        this.mAccIconWidth = (int) getResources().getDimension(R.dimen.ContactListItemView_acc_icon_width);
        this.mHorizontalDividerHeight = Float.valueOf(getResources().getDimension(R.dimen.contact_list_divider_height)).intValue();
        if (this.mActivatedBackgroundDrawable != null) {
            this.mActivatedBackgroundDrawable.setCallback(this);
        }
        this.mAccountIcons = new ImageView[3];
        for (int i = 0; i < 3; i++) {
            this.mAccountIcons[i] = new ImageView(context);
            this.mAccountIcons[i].setScaleType(ScaleType.CENTER);
            addView(this.mAccountIcons[i]);
        }
        if (this.mIsSimAccountIndDisplayEnabled) {
            this.mSimAccountIndicator = new ImageView(context);
            this.mSimAccountIndicator.setScaleType(ScaleType.CENTER);
            addView(this.mSimAccountIndicator);
        }
        if (getContext() instanceof Activity) {
            this.mDialerHighlighter = new DialerHighlighter((Activity) getContext());
        }
        if (isLandScape()) {
            this.mCheckWidth = Float.valueOf(getResources().getDimension(R.dimen.contact_list_item_view_checkbox_width_land)).intValue();
        } else {
            this.mCheckWidth = Float.valueOf(getResources().getDimension(R.dimen.contact_list_item_view_checkbox_width)).intValue();
        }
        this.mOverlayView = new ImageView(context);
        this.mOverlayView.setScaleType(ScaleType.CENTER_CROP);
        this.mOverlayView.setImageResource(R.drawable.head_list);
        if (EmuiFeatureManager.isRcsFeatureEnable() && MultiUsersUtils.isSmsEnabledForCurrentUser(context)) {
            this.mRcsCust = new RcsContactListItemView(getContext());
        }
        this.mShadowView = new ShadowView(getContext(), attrs);
        this.mShadowView.setDuplicateParentStateEnabled(true);
        addView(this.mShadowView);
        this.mShadowView.setVisibility(8);
    }

    public void setFromFavFreEditFragment(boolean flag) {
        this.mFromFavFreEditFragment = flag;
    }

    public void initDragIcon() {
        if (this.mDragIcon == null) {
            this.mDragIcon = new ImageView(getContext());
            this.mDragIcon.setImageResource(R.drawable.ic_public_drag_handle);
            this.mDragIcon.setContentDescription(getContext().getString(R.string.content_description_drag_handle));
            addView(this.mDragIcon);
        }
    }

    public void showDragIcon(boolean isShow) {
        if (this.mDragIcon != null) {
            if (isShow) {
                this.mDragIcon.setVisibility(0);
            } else {
                this.mDragIcon.setVisibility(8);
            }
        }
        this.mIsDisplayDragIcon = isShow;
    }

    public ImageView getDragIcon() {
        return this.mDragIcon;
    }

    public void setUnknownNameText(CharSequence unknownNameText) {
        this.mUnknownNameText = unknownNameText;
    }

    public void setHeaderTextLeftIndent(boolean setPaddingLeft) {
        this.mHeaderTextLeftIndent = setPaddingLeft;
    }

    private int getpreferredHeight() {
        if (ContactDisplayUtils.isSimpleDisplayMode()) {
            if (this.mIsMirror) {
                if (isDisplayAlph()) {
                    this.mContentPaddingRight = this.mAlphTextWidth;
                } else if (this.mIsDisplayDragIcon) {
                    this.mContentPaddingRight = this.mDragIconWidth + (this.mDragListViewItemPaddingLeft * 2);
                } else {
                    this.mContentPaddingRight = this.mContentPaddingLeftForSimpleMode;
                }
            } else if (isDisplayAlph()) {
                this.mContentPaddingLeft = this.mAlphTextWidth;
            } else if (this.mIsDisplayDragIcon) {
                this.mContentPaddingLeft = this.mDragIconWidth + (this.mDragListViewItemPaddingLeft * 2);
            } else {
                this.mContentPaddingLeft = this.mContentPaddingLeftForSimpleMode;
            }
            this.mPreferredHeight = this.mListItemSingleLineHeight;
        } else {
            if (this.mIsMirror) {
                if (isDisplayAlph()) {
                    this.mContentPaddingRight = this.mAlphTextWidth;
                } else if (this.mIsDisplayDragIcon) {
                    this.mContentPaddingRight = this.mDragIconWidth + (this.mLeftPadding * 2);
                } else {
                    this.mContentPaddingRight = this.mLeftPadding;
                }
            } else if (isDisplayAlph()) {
                this.mContentPaddingLeft = this.mAlphTextWidth;
            } else if (this.mIsDisplayDragIcon) {
                this.mContentPaddingLeft = this.mDragIconWidth + (this.mLeftPadding * 2);
            } else {
                this.mContentPaddingLeft = this.mLeftPadding;
            }
            this.mPreferredHeight = this.mListItemDoubleLineHeight;
        }
        if (this.mHorizontalDividerVisible) {
            return this.mPreferredHeight + this.mHorizontalDividerHeight;
        }
        return this.mPreferredHeight;
    }

    private int setAccountIcons() {
        int iconNum = 0;
        if (this.mIsSimAccountIndDisplayEnabled && this.mSimAccountIndicator != null) {
            if (this.mSimAccountIndicatorInfo != null) {
                this.mSimAccountIndicator.setVisibility(0);
                this.mSimAccountIndicator.setImageBitmap(this.mSimAccountIndicatorInfo);
                if (this.mSimAccountIndicatorContentDescription != null) {
                    this.mSimAccountIndicator.setContentDescription(this.mSimAccountIndicatorContentDescription);
                }
                this.mSimAccountIndicator.measure(0, 0);
                iconNum = 1;
            } else {
                this.mSimAccountIndicator.setVisibility(8);
            }
        }
        int i;
        if (this.mIsSimAccount || this.mAccIconsInfo.length <= 0) {
            for (i = 0; i < 3; i++) {
                this.mAccountIcons[i].setVisibility(8);
            }
        } else {
            for (i = 0; i < this.mAccIconsInfo.length; i++) {
                if (this.mAccIconsInfo[i] != null) {
                    this.mAccountIcons[i].setVisibility(0);
                    this.mAccountIcons[i].setImageBitmap(this.mAccIconsInfo[i]);
                    this.mAccountIcons[i].setContentDescription(this.mAccountTypeDescriptions[i]);
                    this.mAccountIcons[i].measure(MeasureSpec.makeMeasureSpec(this.mAccIconWidth, 1073741824), MeasureSpec.makeMeasureSpec(this.mAccIconWidth, 1073741824));
                    iconNum++;
                } else {
                    this.mAccountIcons[i].setVisibility(8);
                }
            }
            for (i = this.mAccIconsInfo.length; i < 3; i++) {
                this.mAccountIcons[i].setVisibility(8);
            }
        }
        if (isVisible(this.mWorkProfileIcon)) {
            this.mWorkProfileIcon.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
        }
        return iconNum;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int effectiveWidth;
        int dataWidth;
        int labelWidth;
        int snippetWidth;
        int companyWidth;
        int specWidth = resolveSize(0, widthMeasureSpec);
        int preferredHeight = getpreferredHeight();
        this.mAlphalbetHeight = 0;
        this.mDragIconHeight = 0;
        this.mNameTextViewHeight = 0;
        this.mPhoneticNameTextViewHeight = 0;
        this.mLabelViewHeight = 0;
        this.mDataViewHeight = 0;
        this.mLabelAndDataViewMaxHeight = 0;
        this.mSnippetTextViewHeight = 0;
        this.mCompanyTextViewHeight = 0;
        this.mSnippetAndCompanyViewMaxHeight = 0;
        this.mStatusTextViewHeight = 0;
        int iconNum = setAccountIcons();
        ensurePhotoViewSize();
        int paddingLeft;
        int i;
        if (this.mPhotoViewWidth > 0 || this.mKeepHorizontalPaddingForPhotoView) {
            paddingLeft = (((((((specWidth - getPaddingLeft()) - getPaddingRight()) - this.mContentPaddingLeft) - this.mContentPaddingRight) - ((this.mAccIconWidth + this.mGapBetweenAccIcons) * iconNum)) - (this.mPhotoViewWidth + this.mGapBetweenImageAndText)) - (isVisible(this.mCheckBox) ? this.mCheckWidth : 0)) - (iconNum > 0 ? this.mAccIndicatorGapFromEnd : 0);
            if (isVisible(this.mCheckBox) && isLandScape()) {
                i = this.mLandCheckBoxPadding;
            } else {
                i = 0;
            }
            effectiveWidth = paddingLeft - i;
            if (this.mRcsCust != null) {
                if (isVisible(this.mRcsCust.getRCSView())) {
                    i = this.mRcsCust.getRcsViewWidth() + this.mGapBetweenAccIcons;
                } else {
                    i = 0;
                }
                effectiveWidth -= i;
            }
        } else {
            paddingLeft = ((((((specWidth - getPaddingLeft()) - getPaddingRight()) - this.mContentPaddingLeft) - this.mContentPaddingRight) - ((this.mAccIconWidth + this.mGapBetweenAccIcons) * iconNum)) - (isVisible(this.mCheckBox) ? this.mCheckWidth : 0)) - (iconNum > 0 ? this.mAccIndicatorGapFromEnd : 0);
            i = (isVisible(this.mCheckBox) && isLandScape()) ? this.mLandCheckBoxPadding : 0;
            effectiveWidth = paddingLeft - i;
            if (this.mRcsCust != null) {
                if (isVisible(this.mRcsCust.getRCSView())) {
                    i = this.mRcsCust.getRcsViewWidth() + this.mGapBetweenAccIcons;
                } else {
                    i = 0;
                }
                effectiveWidth -= i;
            }
        }
        if (isVisible(this.mWorkProfileIcon)) {
            effectiveWidth -= this.mWorkProfileIcon.getMeasuredWidth() + this.mGapBetweenImageAndText;
        }
        if (isVisible(this.mAlphabetIndexView)) {
            int mAlphabetIndexWidth = this.mAlphTextWidth;
            this.mAlphabetIndexView.measure(MeasureSpec.makeMeasureSpec(mAlphabetIndexWidth, 1073741824), MeasureSpec.makeMeasureSpec(mAlphabetIndexWidth, 1073741824));
            this.mAlphalbetHeight = this.mAlphabetIndexView.getMeasuredHeight();
        }
        if (isVisible(this.mNameTextView)) {
            int nameTextWidth = effectiveWidth;
            if (this.mPhotoPosition != PhotoPosition.LEFT) {
                nameTextWidth -= this.mTextIndent;
            }
            this.mNameTextView.measure(MeasureSpec.makeMeasureSpec(nameTextWidth, 1073741824), MeasureSpec.makeMeasureSpec(0, 0));
            this.mNameTextViewHeight = this.mNameTextView.getMeasuredHeight();
            adjustTextInView(this.mNameTextView);
        }
        if (isVisible(this.mPhoneticNameTextView)) {
            this.mPhoneticNameTextView.measure(MeasureSpec.makeMeasureSpec(effectiveWidth, 1073741824), MeasureSpec.makeMeasureSpec(0, 0));
            this.mPhoneticNameTextViewHeight = this.mPhoneticNameTextView.getMeasuredHeight();
        }
        if (isVisible(this.mDragIcon)) {
            this.mDragIcon.measure(MeasureSpec.makeMeasureSpec(this.mDragIconWidth, 1073741824), MeasureSpec.makeMeasureSpec(0, 0));
            this.mDragIconHeight = this.mDragIcon.getMeasuredHeight();
        }
        if (isVisible(this.mDataView)) {
            if (isVisible(this.mLabelView)) {
                int totalWidth = effectiveWidth - this.mGapBetweenLabelAndData;
                dataWidth = (this.mDataViewWidthWeight * totalWidth) / (this.mDataViewWidthWeight + this.mLabelViewWidthWeight);
                labelWidth = (this.mLabelViewWidthWeight * totalWidth) / (this.mDataViewWidthWeight + this.mLabelViewWidthWeight);
            } else {
                dataWidth = effectiveWidth;
                labelWidth = 0;
            }
        } else {
            dataWidth = 0;
            if (isVisible(this.mLabelView)) {
                labelWidth = effectiveWidth;
            } else {
                labelWidth = 0;
            }
        }
        if (isVisible(this.mDataView)) {
            this.mDataView.measure(MeasureSpec.makeMeasureSpec(dataWidth, 1073741824), MeasureSpec.makeMeasureSpec(0, 0));
            this.mDataViewHeight = this.mDataView.getMeasuredHeight();
        }
        if (isVisible(this.mLabelView)) {
            this.mLabelView.measure(MeasureSpec.makeMeasureSpec(labelWidth, this.mPhotoPosition == PhotoPosition.LEFT ? 1073741824 : Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(0, 0));
            this.mLabelViewHeight = this.mLabelView.getMeasuredHeight();
        }
        this.mLabelAndDataViewMaxHeight = Math.max(this.mLabelViewHeight, this.mDataViewHeight);
        if (isVisible(this.mSnippetView)) {
            if (isVisible(this.mCompanyView)) {
                totalWidth = effectiveWidth - this.mGapBetweenLabelAndData;
                if (this.mSnippetView.getText() != null) {
                    snippetWidth = TextUtil.getTextWidth(this.mSnippetView.getText().toString(), this.mSnippetView.getTextSize());
                } else {
                    snippetWidth = 0;
                }
                if (snippetWidth > totalWidth) {
                    snippetWidth = totalWidth;
                    companyWidth = 0;
                } else {
                    companyWidth = totalWidth - snippetWidth;
                }
            } else {
                snippetWidth = effectiveWidth;
                companyWidth = 0;
            }
        } else {
            snippetWidth = 0;
            if (isVisible(this.mCompanyView)) {
                companyWidth = effectiveWidth;
            } else {
                companyWidth = 0;
            }
        }
        if (isVisible(this.mSnippetView)) {
            this.mSnippetView.measure(MeasureSpec.makeMeasureSpec(snippetWidth, 1073741824), MeasureSpec.makeMeasureSpec(0, 0));
            this.mSnippetTextViewHeight = this.mSnippetView.getMeasuredHeight();
            adjustTextInView(this.mSnippetView);
        }
        if (isVisible(this.mCompanyView)) {
            this.mCompanyView.measure(MeasureSpec.makeMeasureSpec(companyWidth, this.mPhotoPosition == PhotoPosition.LEFT ? 1073741824 : Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(0, 0));
            this.mCompanyTextViewHeight = this.mCompanyView.getMeasuredHeight();
            adjustTextInView(this.mCompanyView);
        }
        this.mSnippetAndCompanyViewMaxHeight = Math.max(this.mSnippetTextViewHeight, this.mCompanyTextViewHeight);
        if (isVisible(this.mPresenceIcon)) {
            this.mPresenceIcon.measure(MeasureSpec.makeMeasureSpec(this.mPresenceIconSize, 1073741824), MeasureSpec.makeMeasureSpec(this.mPresenceIconSize, 1073741824));
            this.mStatusTextViewHeight = this.mPresenceIcon.getMeasuredHeight();
        }
        if (isVisible(this.mStatusView)) {
            int statusWidth;
            if (isVisible(this.mPresenceIcon)) {
                statusWidth = (effectiveWidth - this.mPresenceIcon.getMeasuredWidth()) - this.mPresenceIconMargin;
            } else {
                statusWidth = effectiveWidth;
            }
            this.mStatusView.measure(MeasureSpec.makeMeasureSpec(statusWidth, 1073741824), MeasureSpec.makeMeasureSpec(0, 0));
            this.mStatusTextViewHeight = Math.max(this.mStatusTextViewHeight, this.mStatusView.getMeasuredHeight());
        }
        if (isVisible(this.mWorkProfileIcon)) {
            this.mNameTextViewHeight = Math.max(this.mNameTextViewHeight, this.mWorkProfileIcon.getMeasuredHeight());
        }
        int height = (((this.mNameTextViewHeight + this.mPhoneticNameTextViewHeight) + this.mLabelAndDataViewMaxHeight) + this.mSnippetAndCompanyViewMaxHeight) + this.mStatusTextViewHeight;
        if (isVisible(this.mCheckBox)) {
            this.mCheckBox.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
        }
        if (ContactDisplayUtils.isSimpleDisplayMode()) {
            height = (this.mContactListItemPaddingBottom + height) + this.mContactListItemPaddingTop;
        } else {
            height = Math.max((this.mContactListItemPaddingBottom + height) + this.mContactListItemPaddingTop, (this.mPhotoViewHeight + this.mContactListItemPaddingBottom) + this.mContactListItemPaddingTop);
        }
        if (this.mHorizontalDividerVisible) {
            height += this.mHorizontalDividerHeight;
        }
        if (this.mFromFavFreEditFragment && ContactDisplayUtils.isSimpleDisplayMode()) {
            height = this.mDragListViewItemDoubleHeight;
        } else {
            height = Math.max(height, preferredHeight);
        }
        if (this.mHeaderVisible) {
            this.mHeaderSectionView.measure(MeasureSpec.makeMeasureSpec(specWidth, 1073741824), MeasureSpec.makeMeasureSpec(0, 0));
            this.mHeaderTextView.measure(MeasureSpec.makeMeasureSpec(specWidth, Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(0, 0));
            if (this.mAccountFilterView != null) {
                this.mAccountFilterView.measure(MeasureSpec.makeMeasureSpec((specWidth - this.mHeaderTextView.getMeasuredWidth()) - this.mContentPaddingRight, Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(0, 0));
            }
            this.mHeaderBackgroundHeight = Math.max(0, this.mHeaderTextView.getMeasuredHeight());
            height += this.mHeaderBackgroundHeight + this.mHeaderUnderlineHeight;
        }
        if (this.mShadowView != null) {
            if (this.mHeaderVisible) {
                this.mShadowView.measure(MeasureSpec.makeMeasureSpec(specWidth, 1073741824), MeasureSpec.makeMeasureSpec(height - getHeaderHeight(), 1073741824));
            } else {
                this.mShadowView.measure(MeasureSpec.makeMeasureSpec(specWidth, 1073741824), MeasureSpec.makeMeasureSpec(height, 1073741824));
            }
        }
        setMeasuredDimension(specWidth, height);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int alphTop;
        int accRightIndPos;
        int accLeftIndPos;
        int i;
        int height = bottom - top;
        int width = right - left;
        int topBound = 0;
        int bottomBound = height;
        int leftBound = getPaddingLeft();
        int rightBound = width - getPaddingRight();
        if (this.mHeaderVisible) {
            this.mHeaderSectionView.layout(leftBound, 0, rightBound, this.mHeaderBackgroundHeight);
            int headerWidth = this.mHeaderTextView.getMeasuredWidth();
            if (this.mIsMirror) {
                this.mHeaderTextView.layout(rightBound - headerWidth, 0, rightBound, this.mHeaderBackgroundHeight);
                if (this.mAccountFilterView != null) {
                    int accountRightBound = (this.mContentPaddingLeft + leftBound) + this.mAccountFilterView.getMeasuredWidth();
                    if (accountRightBound > rightBound - headerWidth) {
                        accountRightBound = rightBound - headerWidth;
                    }
                    this.mAccountFilterView.layout(this.mContentPaddingLeft + leftBound, 0, accountRightBound, this.mHeaderBackgroundHeight);
                }
            } else {
                this.mHeaderTextView.layout(leftBound, 0, leftBound + headerWidth, this.mHeaderBackgroundHeight);
                if (this.mAccountFilterView != null) {
                    int accountLeftBound = (rightBound - this.mContentPaddingRight) - this.mAccountFilterView.getMeasuredWidth();
                    if (accountLeftBound < leftBound + headerWidth) {
                        accountLeftBound = leftBound + headerWidth;
                    }
                    this.mAccountFilterView.layout(accountLeftBound, 0, rightBound - this.mContentPaddingRight, this.mHeaderBackgroundHeight);
                }
            }
            this.mHeaderDivider.layout(leftBound, this.mHeaderBackgroundHeight, rightBound, this.mHeaderBackgroundHeight + this.mHeaderUnderlineHeight);
            topBound = (this.mHeaderBackgroundHeight + this.mHeaderUnderlineHeight) + 0;
        }
        int dividerLeftPadding = this.mIsMirror ? this.mHDividerPaddingEnd : this.mHDividerPaddingStart;
        int dividerRightPadding = this.mIsMirror ? this.mHDividerPaddingStart : this.mHDividerPaddingEnd;
        if (this.mHorizontalDividerVisible) {
            this.mHorizontalDividerDrawable.setBounds(leftBound + dividerLeftPadding, height - this.mHorizontalDividerHeight, rightBound - dividerRightPadding, height);
            bottomBound = height - this.mHorizontalDividerHeight;
        }
        this.mBoundsWithoutHeader.set(0, topBound, width, bottomBound);
        if (this.mActivatedStateSupported && isActivated()) {
            this.mActivatedBackgroundDrawable.setBounds(this.mBoundsWithoutHeader);
        }
        View dragIcon = this.mDragIcon;
        if (this.mIsMirror) {
            if (dragIcon != null) {
                alphTop = topBound + (((bottomBound - topBound) - this.mDragIconHeight) / 2);
                dragIcon.layout((rightBound - this.mDragIconWidth) - this.mLeftPadding, alphTop, rightBound - this.mLeftPadding, this.mDragIconHeight + alphTop);
            }
        } else if (dragIcon != null) {
            alphTop = topBound + (((bottomBound - topBound) - this.mDragIconHeight) / 2);
            dragIcon.layout(this.mLeftPadding + leftBound, alphTop, (this.mDragIconWidth + leftBound) + this.mLeftPadding, this.mDragIconHeight + alphTop);
        }
        View alPhView = this.mAlphabetIndexView;
        if (this.mIsMirror) {
            if (alPhView != null) {
                alphTop = topBound + (((bottomBound - topBound) - this.mAlphalbetHeight) / 2);
                alPhView.layout(rightBound - this.mAlphTextWidth, alphTop, rightBound, this.mAlphalbetHeight + alphTop);
            }
        } else if (alPhView != null) {
            alphTop = topBound + (((bottomBound - topBound) - this.mAlphalbetHeight) / 2);
            alPhView.layout(leftBound, alphTop, this.mAlphTextWidth + leftBound, this.mAlphalbetHeight + alphTop);
        }
        leftBound += this.mContentPaddingLeft;
        rightBound -= this.mContentPaddingRight;
        View photoView = this.mPhotoView;
        int photoTop;
        if (this.mPhotoPosition == PhotoPosition.LEFT) {
            if (photoView != null) {
                photoTop = topBound + (((bottomBound - topBound) - this.mPhotoViewHeight) / 2);
                photoView.layout(leftBound, photoTop, this.mPhotoViewWidth + leftBound, this.mPhotoViewHeight + photoTop);
                this.mOverlayView.layout(leftBound, photoTop, this.mPhotoViewWidth + leftBound, this.mPhotoViewHeight + photoTop);
                leftBound += this.mPhotoViewWidth + this.mGapBetweenImageAndText;
            } else if (this.mKeepHorizontalPaddingForPhotoView) {
                leftBound += this.mPhotoViewWidth + this.mGapBetweenImageAndText;
            }
        } else if (photoView != null) {
            photoTop = topBound + (((bottomBound - topBound) - this.mPhotoViewHeight) / 2);
            photoView.layout(rightBound - this.mPhotoViewWidth, photoTop, rightBound, this.mPhotoViewHeight + photoTop);
            this.mOverlayView.layout(rightBound - this.mPhotoViewWidth, photoTop, rightBound, this.mPhotoViewHeight + photoTop);
            rightBound -= this.mPhotoViewWidth + this.mGapBetweenImageAndText;
        } else if (this.mKeepHorizontalPaddingForPhotoView) {
            rightBound -= this.mPhotoViewWidth + this.mGapBetweenImageAndText;
        }
        int textTopBound = ((bottomBound + topBound) - ((((this.mNameTextViewHeight + this.mPhoneticNameTextViewHeight) + this.mLabelAndDataViewMaxHeight) + this.mSnippetAndCompanyViewMaxHeight) + this.mStatusTextViewHeight)) / 2;
        int accIndicatorWidth = 0;
        if (this.mRcsCust != null && EmuiFeatureManager.isRcsFeatureEnable()) {
            int[] returnArray = this.mRcsCust.layoutRCSFeatureIcon(rightBound, leftBound, textTopBound, 0, this.mAccIndicatorGapFromEnd, isVisible(this.mCheckBox), isLandScape(), this.mCheckWidth, this.mNameTextViewHeight, this.mIsSimAccountIndDisplayEnabled, this.mSimAccountIndicator, this.mPhotoPosition, this.mGapBetweenAccIcons, getPaddingLeft());
            if (returnArray.length == 3) {
                rightBound = returnArray[0];
                leftBound = returnArray[1];
                accIndicatorWidth = returnArray[2];
            }
        } else if (this.mIsSimAccountIndDisplayEnabled && this.mSimAccountIndicator != null && this.mSimAccountIndicator.getVisibility() == 0) {
            accIndicatorWidth = this.mSimAccountIndicator.getMeasuredWidth();
            if (this.mPhotoPosition == PhotoPosition.LEFT) {
                accRightIndPos = rightBound - this.mAccIndicatorGapFromEnd;
                if (isVisible(this.mCheckBox)) {
                    accRightIndPos -= this.mCheckWidth;
                }
                accLeftIndPos = accRightIndPos - accIndicatorWidth;
                if (isLandScape()) {
                    if (isVisible(this.mCheckBox)) {
                        accLeftIndPos -= this.mCheckWidth;
                    }
                }
                if (!isVisible(this.mCheckBox)) {
                    rightBound -= this.mGapBetweenAccIcons + accIndicatorWidth;
                }
            } else {
                accLeftIndPos = leftBound + this.mAccIndicatorGapFromEnd;
                if (isVisible(this.mCheckBox)) {
                    accLeftIndPos += this.mCheckWidth;
                }
                accRightIndPos = accLeftIndPos + accIndicatorWidth;
                if (!isVisible(this.mCheckBox)) {
                    leftBound += this.mGapBetweenAccIcons + accIndicatorWidth;
                }
                if (isLandScape()) {
                    if (isVisible(this.mCheckBox)) {
                        accRightIndPos += this.mCheckWidth;
                    }
                }
            }
            this.mSimAccountIndicator.layout(accLeftIndPos, textTopBound, accRightIndPos, this.mNameTextViewHeight + textTopBound);
            this.mSimAccountIndicator.bringToFront();
        }
        if (this.mAccIconsInfo.length > 0) {
            int j = 0;
            for (i = 0; i < this.mAccIconsInfo.length; i++) {
                if (this.mAccountIcons[i].getVisibility() == 0) {
                    if (j == 0) {
                        if (this.mPhotoPosition == PhotoPosition.LEFT) {
                            rightBound -= this.mAccIndicatorGapFromEnd;
                        } else {
                            leftBound += this.mAccIndicatorGapFromEnd;
                        }
                    }
                    int accIconWidth = this.mAccountIcons[i].getMeasuredWidth();
                    if (this.mPhotoPosition == PhotoPosition.LEFT) {
                        accRightIndPos = rightBound;
                        accLeftIndPos = rightBound - accIconWidth;
                        rightBound -= this.mGapBetweenAccIcons + accIconWidth;
                    } else {
                        accLeftIndPos = leftBound;
                        accRightIndPos = leftBound + accIconWidth;
                        leftBound += this.mGapBetweenAccIcons + accIconWidth;
                    }
                    this.mAccountIcons[i].layout(accLeftIndPos, textTopBound, accRightIndPos, this.mNameTextViewHeight + textTopBound);
                    this.mAccountIcons[i].bringToFront();
                    j++;
                }
            }
        }
        int workProfileIconWidth = 0;
        if (isVisible(this.mWorkProfileIcon)) {
            workProfileIconWidth = this.mWorkProfileIcon.getMeasuredWidth();
            int distanceFromEnd = isVisible(this.mCheckBox) ? this.mCheckWidth + this.mAccIndicatorGapFromEnd : this.mAccIndicatorGapFromEnd;
            if (this.mPhotoPosition == PhotoPosition.LEFT) {
                this.mWorkProfileIcon.layout((rightBound - workProfileIconWidth) - distanceFromEnd, topBound, rightBound - distanceFromEnd, height - this.mHorizontalDividerHeight);
            } else {
                this.mWorkProfileIcon.layout(leftBound + distanceFromEnd, topBound, (leftBound + workProfileIconWidth) + distanceFromEnd, height - this.mHorizontalDividerHeight);
            }
        }
        if (isVisible(this.mNameTextView)) {
            if (this.mIsMirror) {
                if (isVisible(this.mCheckBox)) {
                    this.mNameTextView.layout((((this.mCheckWidth + leftBound) + accIndicatorWidth) + this.mGapBetweenImageAndText) + workProfileIconWidth, textTopBound, rightBound, this.mNameTextViewHeight + textTopBound);
                    textTopBound += this.mNameTextViewHeight;
                } else {
                    this.mNameTextView.layout((leftBound + accIndicatorWidth) + workProfileIconWidth, textTopBound, rightBound, this.mNameTextViewHeight + textTopBound);
                    textTopBound += this.mNameTextViewHeight;
                }
            } else {
                if (isVisible(this.mCheckBox)) {
                    this.mNameTextView.layout(leftBound, textTopBound, this.mNameTextView.getMeasuredWidth() + leftBound, this.mNameTextViewHeight + textTopBound);
                    textTopBound += this.mNameTextViewHeight;
                } else {
                    this.mNameTextView.layout(leftBound, textTopBound, (rightBound - accIndicatorWidth) - workProfileIconWidth, this.mNameTextViewHeight + textTopBound);
                    textTopBound += this.mNameTextViewHeight;
                }
            }
        }
        int iconWidth;
        if (this.mIsMirror) {
            int statusRightBound = rightBound;
            if (isVisible(this.mPresenceIcon)) {
                iconWidth = this.mPresenceIcon.getMeasuredWidth();
                this.mPresenceIcon.layout(rightBound - iconWidth, textTopBound, rightBound, this.mStatusTextViewHeight + textTopBound);
                statusRightBound -= this.mPresenceIconMargin + iconWidth;
            }
            if (isVisible(this.mStatusView)) {
                this.mStatusView.layout(leftBound, textTopBound, statusRightBound, this.mStatusTextViewHeight + textTopBound);
            }
        } else {
            int statusLeftBound = leftBound;
            if (isVisible(this.mPresenceIcon)) {
                iconWidth = this.mPresenceIcon.getMeasuredWidth();
                this.mPresenceIcon.layout(leftBound, textTopBound, leftBound + iconWidth, this.mStatusTextViewHeight + textTopBound);
                statusLeftBound += this.mPresenceIconMargin + iconWidth;
            }
            if (isVisible(this.mStatusView)) {
                this.mStatusView.layout(statusLeftBound, textTopBound, rightBound, this.mStatusTextViewHeight + textTopBound);
            }
        }
        if (!isVisible(this.mStatusView)) {
        }
        textTopBound += this.mStatusTextViewHeight;
        int dataLeftBound = leftBound;
        if (isVisible(this.mPhoneticNameTextView)) {
            this.mPhoneticNameTextView.layout(leftBound, textTopBound, rightBound, this.mPhoneticNameTextViewHeight + textTopBound);
            textTopBound += this.mPhoneticNameTextViewHeight;
        }
        if (isVisible(this.mLabelView)) {
            if (this.mPhotoPosition == PhotoPosition.LEFT) {
                this.mLabelView.layout(rightBound - this.mLabelView.getMeasuredWidth(), (this.mLabelAndDataViewMaxHeight + textTopBound) - this.mLabelViewHeight, rightBound, this.mLabelAndDataViewMaxHeight + textTopBound);
                rightBound -= this.mLabelView.getMeasuredWidth();
            } else {
                dataLeftBound = leftBound + this.mLabelView.getMeasuredWidth();
                this.mLabelView.layout(leftBound, (this.mLabelAndDataViewMaxHeight + textTopBound) - this.mLabelViewHeight, dataLeftBound, this.mLabelAndDataViewMaxHeight + textTopBound);
                dataLeftBound += this.mGapBetweenLabelAndData;
            }
        }
        if (isVisible(this.mDataView)) {
            this.mDataView.layout(dataLeftBound, (this.mLabelAndDataViewMaxHeight + textTopBound) - this.mDataViewHeight, rightBound, this.mLabelAndDataViewMaxHeight + textTopBound);
        }
        if (!isVisible(this.mLabelView)) {
        }
        textTopBound += this.mLabelAndDataViewMaxHeight;
        int lrightBound = rightBound;
        int lleftBound = leftBound;
        if (isVisible(this.mCheckBox)) {
            if (this.mIsMirror) {
                lleftBound = isLandScape() ? lleftBound + (this.mCheckWidth + this.mLandCheckBoxPadding) : lleftBound + this.mCheckWidth;
            } else {
                lrightBound = isLandScape() ? lrightBound - (this.mCheckWidth + this.mLandCheckBoxPadding) : lrightBound - this.mCheckWidth;
            }
        }
        if (this.mIsMirror) {
            lleftBound += isVisible(this.mWorkProfileIcon) ? this.mGapBetweenImageAndText + workProfileIconWidth : 0;
        } else {
            lrightBound -= isVisible(this.mWorkProfileIcon) ? this.mGapBetweenImageAndText + workProfileIconWidth : 0;
        }
        if (this.mIsMirror) {
            if (isVisible(this.mSnippetView)) {
                this.mSnippetView.layout(lrightBound - this.mSnippetView.getMeasuredWidth(), (this.mSnippetAndCompanyViewMaxHeight + textTopBound) - this.mSnippetTextViewHeight, lrightBound, this.mSnippetTextViewHeight + textTopBound);
                lrightBound = (lrightBound - this.mSnippetView.getMeasuredWidth()) - this.mGapBetweenLabelAndData;
            }
            if (isVisible(this.mCompanyView)) {
                this.mCompanyView.layout(lleftBound, (this.mSnippetAndCompanyViewMaxHeight + textTopBound) - this.mCompanyTextViewHeight, lrightBound, this.mCompanyTextViewHeight + textTopBound);
            }
        } else {
            if (isVisible(this.mCompanyView)) {
                this.mCompanyView.layout(lrightBound - this.mCompanyView.getMeasuredWidth(), (this.mSnippetAndCompanyViewMaxHeight + textTopBound) - this.mCompanyTextViewHeight, lrightBound, this.mCompanyTextViewHeight + textTopBound);
                lrightBound = (lrightBound - this.mCompanyView.getMeasuredWidth()) - this.mGapBetweenLabelAndData;
            }
            if (isVisible(this.mSnippetView)) {
                this.mSnippetView.layout(lleftBound, (this.mSnippetAndCompanyViewMaxHeight + textTopBound) - this.mSnippetTextViewHeight, lrightBound, this.mSnippetTextViewHeight + textTopBound);
            }
        }
        layoutRightSide(height, topBound, bottomBound, rightBound);
        if (this.mShadowView != null) {
            this.mShadowView.layout(left, topBound, right, bottomBound);
        }
        for (i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view != null) {
                if (!view.equals(this.mShadowView)) {
                    setViewPivot(view);
                }
            }
        }
    }

    private boolean isDisplayAlph() {
        return this.isDisplayAlph;
    }

    public void setIsDisplayAlph(boolean flag) {
        this.isDisplayAlph = flag;
    }

    public int getHeaderHeight() {
        if (!this.mHeaderVisible || this.mHeaderSectionView == null) {
            return 0;
        }
        return this.mHeaderSectionView.getMeasuredHeight();
    }

    protected int layoutRightSide(int height, int topBound, int bottomBound, int rightBound) {
        if (isVisible(this.mCheckBox)) {
            int CheckBoxWidth = this.mCheckBox.getMeasuredWidth();
            if (this.mIsMirror) {
                int leftBound = getPaddingLeft() + this.mContentPaddingLeft;
                if (this.mFromFavFreEditFragment) {
                    leftBound = getPaddingLeft() + this.mDragListViewItemPaddingLeft;
                }
                this.mCheckBox.layout(leftBound, topBound, leftBound + CheckBoxWidth, height - this.mHorizontalDividerHeight);
            } else {
                rightBound -= CheckBoxWidth;
                if (this.mFromFavFreEditFragment) {
                    rightBound += this.mContentPaddingRight - this.mDragListViewItemPaddingLeft;
                }
                this.mCheckBox.layout(rightBound, topBound, rightBound + CheckBoxWidth, height - this.mHorizontalDividerHeight);
            }
        }
        return rightBound;
    }

    public void adjustListItemSelectionBounds(Rect bounds) {
        bounds.top += this.mBoundsWithoutHeader.top;
        bounds.bottom = bounds.top + this.mBoundsWithoutHeader.height();
        bounds.left += this.mSelectionBoundsMarginLeft;
        bounds.right -= this.mSelectionBoundsMarginRight;
    }

    protected boolean isVisible(View view) {
        return view != null && view.getVisibility() == 0;
    }

    private void ensurePhotoViewSize() {
        if (!this.mPhotoViewWidthAndHeightAreReady) {
            int defaultPhotoViewSize = getDefaultPhotoViewSize();
            this.mPhotoViewHeight = defaultPhotoViewSize;
            this.mPhotoViewWidth = defaultPhotoViewSize;
            if (this.mPhotoView == null) {
                if (!this.mKeepHorizontalPaddingForPhotoView) {
                    this.mPhotoViewWidth = 0;
                }
                if (!this.mKeepVerticalPaddingForPhotoView) {
                    this.mPhotoViewHeight = 0;
                }
            }
            this.mPhotoViewWidthAndHeightAreReady = true;
        }
    }

    protected int getDefaultPhotoViewSize() {
        return this.mDefaultPhotoViewSize - (getContext().getResources().getDimensionPixelSize(R.dimen.default_detail_contact_photo_margin) * 2);
    }

    private LayoutParams getDefaultPhotoLayoutParams() {
        LayoutParams params = generateDefaultLayoutParams();
        params.width = getDefaultPhotoViewSize();
        params.height = params.width;
        return params;
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (this.mActivatedStateSupported) {
            this.mActivatedBackgroundDrawable.setState(getDrawableState());
        }
    }

    protected boolean verifyDrawable(Drawable who) {
        return who != this.mActivatedBackgroundDrawable ? super.verifyDrawable(who) : true;
    }

    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (this.mActivatedStateSupported) {
            this.mActivatedBackgroundDrawable.jumpToCurrentState();
        }
    }

    public void dispatchDraw(Canvas canvas) {
        if (this.mActivatedStateSupported && isActivated()) {
            this.mActivatedBackgroundDrawable.draw(canvas);
        }
        if (this.mHorizontalDividerVisible) {
            this.mHorizontalDividerDrawable.draw(canvas);
        }
        super.dispatchDraw(canvas);
    }

    public void setDividerVisible(boolean visible) {
        this.mHorizontalDividerVisible = visible;
    }

    public void setSectionHeader(String title) {
        setSectionHeaderAndPadding(title, false);
    }

    public void setSectionHeaderAndPadding(String title, boolean showHeaderDiver) {
        CharSequence charSequence = null;
        if (TextUtils.isEmpty(title)) {
            if (this.mHeaderSectionView != null) {
                this.mHeaderSectionView.setVisibility(8);
            }
            if (this.mHeaderTextView != null) {
                this.mHeaderTextView.setVisibility(8);
            }
            this.mSectionTitle = null;
            if (this.mHeaderDivider != null) {
                this.mHeaderDivider.setVisibility(8);
            }
            this.mHeaderVisible = false;
            return;
        }
        if (this.mHeaderTextView == null) {
            this.mHeaderSectionView = new View(getContext());
            this.mHeaderSectionView.setClickable(true);
            this.mHeaderSectionView.setBackgroundColor(this.mHeaderBackgroundColor);
            addView(this.mHeaderSectionView);
            this.mHeaderTextView = new TextView(getContext());
            this.mHeaderTextView.setTextColor(this.mHeaderTextColor);
            this.mHeaderTextView.setTypeface(Typeface.create("HwChinese-medium", 0));
            if (!this.mHeaderTextNoLeftIndent) {
                this.mHeaderTextView.setPaddingRelative(this.mHeaderTextIndent, this.mHeaderTextPaddingTop, 0, this.mHeaderTextPaddingBottom);
            }
            if (this.mHeaderTextLeftIndent) {
                if (this.mIsMirror) {
                    this.mHeaderTextView.setPaddingRelative(this.mGroupHeaderTextIndentMirror, this.mHeaderTextPaddingTop, 0, this.mHeaderTextPaddingBottom);
                } else {
                    this.mHeaderTextView.setPaddingRelative(this.mGroupHeaderTextIndent, this.mHeaderTextPaddingTop, 0, this.mHeaderTextPaddingBottom);
                }
                this.mHeaderTextLeftIndent = false;
            }
            this.mHeaderTextView.setSingleLine(true);
            this.mHeaderTextView.setTextSize(0, (float) this.mHeaderTextSize);
            this.mHeaderTextView.setGravity(80);
            addView(this.mHeaderTextView);
            if (this.mAccountFilterView != null) {
                this.mAccountFilterView.bringToFront();
            }
        }
        if (this.mHeaderDivider == null) {
            this.mHeaderDivider = new View(getContext());
            this.mHeaderDivider.setBackground(this.mHeaderDividerDrable);
            addView(this.mHeaderDivider);
        }
        this.mSectionTitle = title;
        String sectionTitleCount = this.mCount != null ? this.mSectionTitle + "  " + this.mCount : this.mSectionTitle;
        TextView textView = this.mHeaderTextView;
        if (sectionTitleCount != null) {
            charSequence = sectionTitleCount.toString().toUpperCase();
        }
        setMarqueeText(textView, charSequence);
        this.mHeaderSectionView.setContentDescription(sectionTitleCount);
        this.mHeaderTextView.setVisibility(0);
        this.mHeaderSectionView.setVisibility(0);
        if (showHeaderDiver) {
            this.mHeaderDivider.setVisibility(0);
        } else {
            this.mHeaderDivider.setVisibility(8);
        }
        this.mHeaderVisible = true;
        setPadding(0, 0, 0, 0);
    }

    public ImageView getPhotoView(long photoId) {
        if (this.mPhotoView == null) {
            this.mPhotoView = new ImageView(this.mContext);
            this.mPhotoView.setLayoutParams(getDefaultPhotoLayoutParams());
            this.mPhotoView.setScaleType(ScaleType.CENTER_CROP);
            this.mPhotoView.setBackground(null);
            addView(this.mPhotoView);
            this.mOverlayView.setLayoutParams(getDefaultPhotoLayoutParams());
            this.mOverlayView.setBackground(null);
            this.mPhotoViewWidthAndHeightAreReady = false;
        }
        if (photoId > 0) {
            addOverLayView();
        } else {
            removeOverLayView();
        }
        return this.mPhotoView;
    }

    public void removePhotoView() {
        removePhotoView(false, true);
    }

    public void removePhotoView(boolean keepHorizontalPadding, boolean keepVerticalPadding) {
        this.mPhotoViewWidthAndHeightAreReady = false;
        this.mKeepHorizontalPaddingForPhotoView = keepHorizontalPadding;
        this.mKeepVerticalPaddingForPhotoView = keepVerticalPadding;
        if (this.mPhotoView != null) {
            removeView(this.mPhotoView);
            this.mPhotoView = null;
        }
        if (this.mOverlayView != null) {
            removeView(this.mOverlayView);
        }
    }

    private void addOverLayView() {
        if (this.mOverlayView != null && this.mOverlayView.getParent() == null) {
            addView(this.mOverlayView);
        }
    }

    private void removeOverLayView() {
        if (this.mOverlayView != null && this.mOverlayView.getParent() != null) {
            removeView(this.mOverlayView);
        }
    }

    public void setHighlightedPrefix(char[] lowerCasePrefix) {
        if (lowerCasePrefix == null) {
            this.mHighlightedPrefix = null;
        } else if (this.mMultiSearchContacts) {
            this.mHighlightedPrefix = standardPrefix(lowerCasePrefix);
        } else {
            this.mHighlightedPrefix = (char[]) lowerCasePrefix.clone();
        }
    }

    public char[] standardPrefix(char[] prefix) {
        char blankSpace = HwCustPreloadContacts.EMPTY_STRING.charAt(0);
        int validStringNum = 0;
        if (prefix == null) {
            return new char[0];
        }
        StringBuffer queryString = new StringBuffer(String.valueOf(prefix).trim());
        int i = 1;
        while (i < queryString.length()) {
            if (blankSpace != queryString.charAt(i - 1)) {
                i++;
            } else {
                if (blankSpace == queryString.charAt(i)) {
                    queryString.deleteCharAt(i - 1);
                } else {
                    validStringNum++;
                    i++;
                }
                if (10 == validStringNum) {
                    int end = queryString.length();
                    int start = (i - 1) - 1;
                    if (end <= start || start < 0) {
                        HwLog.i("ContactListItemView", "standardPrefix try to delete chars OutOfBounds.");
                    } else {
                        queryString.delete(start, end);
                    }
                    return removeEmptySubstring(queryString.toString());
                }
            }
        }
        return removeEmptySubstring(queryString.toString());
    }

    private char[] removeEmptySubstring(String str) {
        if (TextUtils.isEmpty(str) || this.mMatchTypeArray == null) {
            return new char[0];
        }
        if (str.split(HwCustPreloadContacts.EMPTY_STRING).length == this.mMatchTypeArray.length) {
            return str.toCharArray();
        }
        StringBuffer sb = new StringBuffer(str);
        int start = 0;
        int strLength = sb.length();
        boolean isAllSpecialSymbol = true;
        int i = 0;
        while (i < strLength) {
            if (Character.isLetterOrDigit(sb.charAt(i))) {
                isAllSpecialSymbol = false;
            }
            if (HwCustPreloadContacts.EMPTY_STRING.charAt(0) == sb.charAt(i) || i == strLength - 1) {
                if (isAllSpecialSymbol) {
                    sb.delete(start, i + 1);
                    i = start;
                    strLength = sb.length();
                } else {
                    start = i + 1;
                    isAllSpecialSymbol = true;
                }
            }
            i++;
        }
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == HwCustPreloadContacts.EMPTY_STRING.charAt(0)) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString().toCharArray();
    }

    public TextView getNameTextView() {
        if (this.mNameTextView == null) {
            this.mNameTextView = new TextView(getContext());
            this.mNameTextView.setSingleLine(true);
            this.mNameTextView.setEllipsize(getTextEllipsis(0));
            this.mNameTextView.setTextSize(0, (float) this.mPrimaryTextSize);
            this.mNameTextView.setTextColor(this.mPrimaryTextColor);
            this.mNameTextView.setActivated(isActivated());
            this.mNameTextView.setGravity(16);
            if (Constants.isEXTRA_HUGE()) {
                this.mNameTextView.setTextSize(1, 28.0f);
            }
            addView(this.mNameTextView);
        }
        CommonUtilMethods.setNameViewDirection(this.mNameTextView);
        return this.mNameTextView;
    }

    public void setAlphaView(String str) {
        if (!TextUtils.isEmpty(str)) {
            if (this.mAlphabetIndexView == null) {
                this.mAlphabetIndexView = new TextView(getContext());
                this.mAlphabetIndexView.setTextSize(0, (float) this.mAlphTextSize);
                this.mAlphabetIndexView.setTextColor(this.mAlphTextColor);
                this.mAlphabetIndexView.setTypeface(Typeface.createFromFile("/system/fonts/Roboto-Regular.ttf"));
                this.mAlphabetIndexView.setWidth(this.mAlphalbetHeight);
                this.mAlphabetIndexView.setHeight(this.mAlphalbetHeight);
                this.mAlphabetIndexView.setGravity(17);
                addView(this.mAlphabetIndexView);
            }
            this.mAlphabetIndexView.setVisibility(0);
            this.mAlphabetIndexView.setText(str);
        } else if (this.mAlphabetIndexView != null) {
            this.mAlphabetIndexView.setVisibility(8);
        }
    }

    public void removeAlphaView() {
        if (this.mAlphabetIndexView != null) {
            this.mAlphabetIndexView.setVisibility(8);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Constants.updateFontSizeSettings(getContext());
    }

    public void showCheckBox() {
        getCheckBox();
        this.mCheckBox.setVisibility(0);
    }

    public void hideCheckbox() {
        getCheckBox();
        this.mCheckBox.setVisibility(8);
    }

    private void getCheckBox() {
        if (this.mCheckBox == null) {
            this.mCheckBox = new CheckBox(getContext());
            this.mCheckBox.setFocusable(false);
            this.mCheckBox.setClickable(false);
            addView(this.mCheckBox);
            if (this.mRcsCust != null) {
                this.mRcsCust.setCheckBox(this.mCheckBox);
            }
        }
        setCheckBoxPadding();
    }

    public RcsContactListItemView getRcsCust() {
        return this.mRcsCust;
    }

    private void setCheckBoxPadding() {
        if (isLandScape()) {
            this.mCheckBox.setPadding(this.mLandCheckBoxPadding, 0, this.mLandCheckBoxPadding, 0);
        } else {
            this.mCheckBox.setPadding(this.mCheckBoxPadding, 0, this.mCheckBoxPadding, 0);
        }
    }

    private boolean isLandScape() {
        if (getResources().getConfiguration().orientation == 2) {
            return true;
        }
        return false;
    }

    public void setPhoneticName(char[] text, int size) {
        if (text != null && size != 0) {
            getPhoneticNameTextView();
            setMarqueeText(this.mPhoneticNameTextView, text, size);
            this.mPhoneticNameTextView.setVisibility(0);
        } else if (this.mPhoneticNameTextView != null) {
            this.mPhoneticNameTextView.setVisibility(8);
        }
    }

    public TextView getPhoneticNameTextView() {
        if (this.mPhoneticNameTextView == null) {
            this.mPhoneticNameTextView = new TextView(getContext());
            this.mPhoneticNameTextView.setMaxLines(1);
            this.mPhoneticNameTextView.setEllipsize(getTextEllipsis(0));
            this.mPhoneticNameTextView.setActivated(isActivated());
            addView(this.mPhoneticNameTextView);
        }
        return this.mPhoneticNameTextView;
    }

    public void setLabel(CharSequence text) {
        if (!TextUtils.isEmpty(text)) {
            getLabelView();
            setMarqueeText(this.mLabelView, text);
            this.mLabelView.setVisibility(0);
        } else if (this.mLabelView != null) {
            this.mLabelView.setVisibility(8);
        }
    }

    public TextView getLabelView() {
        if (this.mLabelView == null) {
            this.mLabelView = new TextView(getContext());
            this.mLabelView.setMaxLines(1);
            this.mLabelView.setEllipsize(getTextEllipsis(0));
            if (this.mPhotoPosition == PhotoPosition.LEFT) {
                this.mLabelView.setAllCaps(true);
                this.mLabelView.setGravity(8388613);
            }
            this.mLabelView.setTextSize(0, (float) this.mSecondaryTextSize);
            this.mLabelView.setTextColor(this.mSecondaryTextColor);
            this.mLabelView.setActivated(isActivated());
            addView(this.mLabelView);
        }
        return this.mLabelView;
    }

    public void setData(char[] text, int size) {
        if (text != null && size != 0) {
            getDataView();
            setMarqueeText(this.mDataView, text, size);
            this.mDataView.setVisibility(0);
        } else if (this.mDataView != null) {
            this.mDataView.setVisibility(8);
        }
    }

    private void setMarqueeText(TextView textView, char[] text, int size) {
        if (getTextEllipsis(0) == TruncateAt.END) {
            setMarqueeText(textView, new String(text, 0, size));
        } else {
            textView.setText(text, 0, size);
        }
    }

    private void setMarqueeText(TextView textView, CharSequence text) {
        if (getTextEllipsis(0) == TruncateAt.END) {
            SpannableString spannable = new SpannableString(text);
            spannable.setSpan(TruncateAt.END, 0, spannable.length(), 33);
            textView.setText(spannable);
            return;
        }
        textView.setText(text);
    }

    public TextView getDataView() {
        if (this.mDataView == null) {
            this.mDataView = new TextView(getContext());
            this.mDataView.setSingleLine(true);
            this.mDataView.setEllipsize(getTextEllipsis(0));
            this.mDataView.setActivated(isActivated());
            this.mDataView.setTextSize(0, (float) this.mSecondaryTextSize);
            this.mDataView.setTextColor(this.mSecondaryTextColor);
            addView(this.mDataView);
        }
        return this.mDataView;
    }

    public void setSnippet(String text) {
        if (TextUtils.isEmpty(text)) {
            if (this.mSnippetView != null) {
                this.mSnippetView.setVisibility(8);
            }
            this.isOrgnizationSnippet = false;
            return;
        }
        if (EmuiFeatureManager.isChinaArea()) {
            text = ContactsUtils.getChinaFormatNumber(text);
        }
        this.mPrefixHighligher.setText(getSnippetView(), text, this.mHighlightedPrefix);
        this.mSnippetView.setVisibility(0);
    }

    public void setCompany(String company) {
        if (!TextUtils.isEmpty(company) && !this.isOrgnizationSnippet) {
            getCompanyView().setText(company);
            this.mCompanyView.setVisibility(0);
        } else if (this.mCompanyView != null) {
            this.mCompanyView.setVisibility(8);
        }
    }

    public void setSnippet(String label, String text) {
        if (TextUtils.isEmpty(text)) {
            if (this.mSnippetView != null) {
                this.mSnippetView.setVisibility(8);
            }
            this.isOrgnizationSnippet = false;
            return;
        }
        if (EmuiFeatureManager.isChinaArea()) {
            text = ContactsUtils.getChinaFormatNumber(text);
        }
        this.mPrefixHighligher.setText(getSnippetView(), Html.fromHtml("<b>" + label + "</b>") + " ‪" + text + "‬", this.mHighlightedPrefix);
        this.mSnippetView.setVisibility(0);
    }

    public void setSearchMatchType(Cursor cursor) {
        boolean isSearchCursor;
        if (cursor instanceof MultiCursor) {
            isSearchCursor = ((MultiCursor) cursor).getCurrentCursor() instanceof HwSearchCursor;
        } else {
            isSearchCursor = false;
        }
        if (isSearchCursor) {
            Cursor currentCursor = ((MultiCursor) cursor).getCurrentCursor();
            int columnIndex = currentCursor.getColumnIndex("search_result");
            if (columnIndex < 0) {
                this.mSearchMatchType = -1;
                this.mPrefixHighligher.setSearchMatchType(this.mSearchMatchType);
                return;
            }
            this.mMultiSearchContacts = currentCursor.getType(columnIndex) == 4;
            if (this.mMultiSearchContacts) {
                this.mMatchTypeArray = ((HwSearchCursor) currentCursor).getMatchInfoArray(4);
                if (this.mMatchTypeArray.length == 0) {
                    setDefaultType();
                    HwLog.i("ContactListItemView", "SEARCH_RESULT is null.");
                    return;
                }
                int[] matchPositionArray = ((HwSearchCursor) currentCursor).getMatchInfoArray(0);
                this.mSearchMatchType = -1;
                int i = 0;
                while (i < this.mMatchTypeArray.length) {
                    if (this.mMatchTypeArray[i] == 40 || this.mMatchTypeArray[i] == 32) {
                        this.mSearchMatchType = this.mMatchTypeArray[i];
                    } else if (this.mMatchTypeArray[i] < this.mSnippetMatchType) {
                        this.mSnippetMatchType = this.mMatchTypeArray[i];
                        this.mSnippetMatchPosition = matchPositionArray[i];
                    } else if (40 == this.mSnippetMatchType) {
                        this.mSnippetMatchType = this.mMatchTypeArray[i];
                        this.mSnippetMatchPosition = matchPositionArray[i];
                    }
                    i++;
                }
                this.mPrefixHighligher.setSearchMatchType(this.mSnippetMatchType);
            } else {
                this.mSearchMatchType = ((HwSearchCursor) ((MultiCursor) cursor).getCurrentCursor()).getMatchType();
                this.mPrefixHighligher.setSearchMatchType(this.mSearchMatchType);
            }
        }
    }

    public void setDefaultType() {
        this.mSearchMatchType = 40;
        this.mPrefixHighligher.setSearchMatchType(this.mSearchMatchType);
    }

    public TextView getCompanyView() {
        if (this.mCompanyView == null) {
            this.mCompanyView = new TextView(getContext());
            this.mCompanyView.setSingleLine(true);
            this.mCompanyView.setEllipsize(getTextEllipsis(0));
            this.mCompanyView.setTextSize(0, (float) this.mSecondaryTextSize);
            this.mCompanyView.setTextColor(this.mSecondaryTextColor);
            this.mCompanyView.setActivated(isActivated());
            this.mCompanyView.setGravity(16);
            addView(this.mCompanyView);
        }
        return this.mCompanyView;
    }

    public TextView getSnippetView() {
        if (this.mSnippetView == null) {
            this.mSnippetView = new TextView(getContext());
            this.mSnippetView.setSingleLine(true);
            this.mSnippetView.setEllipsize(getTextEllipsis(0));
            this.mSnippetView.setTextSize(0, (float) this.mSecondaryTextSize);
            this.mSnippetView.setTextColor(this.mSecondaryTextColor);
            this.mSnippetView.setActivated(isActivated());
            this.mSnippetView.setGravity(16);
            addView(this.mSnippetView);
        }
        return this.mSnippetView;
    }

    public TextView getStatusView() {
        if (this.mStatusView == null) {
            this.mStatusView = new TextView(getContext());
            this.mStatusView.setMaxLines(1);
            this.mStatusView.setEllipsize(getTextEllipsis(0));
            this.mStatusView.setTextSize(0, (float) this.mSecondaryTextSize);
            this.mStatusView.setTextColor(this.mSecondaryTextColor);
            this.mStatusView.setActivated(isActivated());
            addView(this.mStatusView);
        }
        return this.mStatusView;
    }

    public TextView getAccountFilterView() {
        if (this.mAccountFilterView == null) {
            this.mAccountFilterView = new TextView(getContext());
            this.mAccountFilterView.setSingleLine(true);
            this.mAccountFilterView.setTextColor(this.mHeaderTextColor);
            this.mAccountFilterView.setTextSize(0, (float) this.mHeaderTextSize);
            this.mAccountFilterView.setEllipsize(getTextEllipsis(1));
            this.mAccountFilterView.setTypeface(Typeface.create("HwChinese-medium", 0));
            this.mAccountFilterView.setPaddingRelative(0, this.mHeaderTextPaddingTop, 0, this.mHeaderTextPaddingBottom);
            this.mAccountFilterView.setGravity(8388693);
            addView(this.mAccountFilterView);
        }
        return this.mAccountFilterView;
    }

    public void setAccountFilterText(ContactListFilter filter) {
        getAccountFilterView();
        if (filter == null) {
            this.mAccountFilterView.setText(null);
        } else {
            AccountFilterUtil.updateAccountFilterTitleForPeople(this.mAccountFilterView, filter, false);
        }
    }

    public void setCountView(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            this.mCount = null;
            if (this.mHeaderTextView != null) {
                CharSequence charSequence;
                TextView textView = this.mHeaderTextView;
                if (this.mSectionTitle != null) {
                    charSequence = this.mSectionTitle;
                } else {
                    charSequence = "";
                }
                textView.setText(charSequence);
                return;
            }
            return;
        }
        try {
            this.mCount = NumberFormat.getInstance().format((long) Integer.parseInt(text.toString()));
        } catch (NumberFormatException e) {
            this.mCount = text.toString();
        }
        if (this.mHeaderTextView != null) {
            setMarqueeText(this.mHeaderTextView, (this.mSectionTitle != null ? this.mSectionTitle + "  " + this.mCount : this.mCount).toString().toUpperCase());
        }
    }

    public void setStatus(CharSequence text) {
        if (!TextUtils.isEmpty(text)) {
            getStatusView();
            setMarqueeText(this.mStatusView, text);
            this.mStatusView.setVisibility(0);
        } else if (this.mStatusView != null) {
            this.mStatusView.setVisibility(8);
        }
    }

    public void setPresence(Drawable icon) {
        if (icon != null) {
            if (this.mPresenceIcon == null) {
                this.mPresenceIcon = new ImageView(getContext());
                addView(this.mPresenceIcon);
            }
            this.mPresenceIcon.setImageDrawable(icon);
            this.mPresenceIcon.setScaleType(ScaleType.CENTER);
            this.mPresenceIcon.setVisibility(0);
        } else if (this.mPresenceIcon != null) {
            this.mPresenceIcon.setVisibility(8);
        }
    }

    public void setWorkProfileIconEnabled(boolean enabled) {
        int i = 0;
        if (this.mWorkProfileIcon != null) {
            ImageView imageView = this.mWorkProfileIcon;
            if (!enabled) {
                i = 8;
            }
            imageView.setVisibility(i);
        } else if (enabled) {
            this.mWorkProfileIcon = new ImageView(getContext());
            addView(this.mWorkProfileIcon);
            this.mWorkProfileIcon.setImageResource(R.drawable.ic_work_profile);
            this.mWorkProfileIcon.setScaleType(ScaleType.CENTER_INSIDE);
            this.mWorkProfileIcon.setVisibility(0);
        }
    }

    private TruncateAt getTextEllipsis(int textEllipsisFlag) {
        switch (textEllipsisFlag) {
            case 0:
                return TruncateAt.END;
            case 1:
                return TruncateAt.START;
            default:
                return TruncateAt.END;
        }
    }

    private void highlighter(PhoneItem item, TextView nameView, String prefix, String sortKey) {
        int mode;
        String name;
        ArrayList tokens = null;
        if (item.mName.equals(item.mSortKey)) {
            mode = 0;
            name = item.mName.toLowerCase();
        } else {
            Object[] objs;
            mode = 0;
            DialerHighlighter dialerHighlighter;
            if (Locale.getDefault().getCountry().equalsIgnoreCase("TW")) {
                dialerHighlighter = this.mDialerHighlighter;
                objs = DialerHighlighter.convertToZhuyin(item.mName);
                prefix = prefix.replace("_", "");
            } else {
                dialerHighlighter = this.mDialerHighlighter;
                objs = DialerHighlighter.convertToPinyin(item.mName);
            }
            if (objs != null && objs.length != 0) {
                ArrayList<Token> tokens2 = objs[0];
                name = objs[1];
                for (Token t : tokens2) {
                    if (t.type != 2) {
                        if (t.type == 3) {
                        }
                    }
                    mode = 1;
                }
            } else {
                return;
            }
        }
        if (this.mMultiSearchContacts) {
            String[] subPrefix = prefix.split(HwCustPreloadContacts.EMPTY_STRING);
            int hightlightTimes = Math.min(subPrefix.length, this.mMatchTypeArray.length);
            int i = 0;
            while (i < hightlightTimes) {
                if (40 == this.mMatchTypeArray[i] || 32 == this.mMatchTypeArray[i]) {
                    highlightMatchInitials(name, subPrefix[i], nameView, item.mType, item, mode, tokens);
                }
                i++;
            }
        } else {
            highlightMatchInitials(name, prefix, nameView, item.mType, item, mode, tokens);
        }
    }

    private void highlightMatchInitials(String aTarget, String input, TextView aNameView, int aType, PhoneItem item, int aMode, ArrayList<Token> aTokens) {
        if (input != null && input.length() != 0) {
            int[] startIndex;
            if (HwLog.HWDBG) {
                HwLog.d("ContactListItemView", "highlightMatchInitials() aMode  = " + aMode);
            }
            StringBuffer newMatchPinyin = new StringBuffer();
            String language = Locale.getDefault().getCountry();
            if ("JP".equalsIgnoreCase(language) || "KR".equalsIgnoreCase(language)) {
                startIndex = new int[0];
            } else {
                startIndex = SearchMatch.getMatchIndex(aTarget, input, item.mName, false, newMatchPinyin, getContext());
            }
            if (startIndex == null) {
                HwLog.d("ContactListItemView", "startIndex == null!");
                return;
            }
            int i;
            int begin;
            int end;
            if (newMatchPinyin.length() != 0) {
                aTarget = newMatchPinyin.toString();
            }
            int targetLength = aTarget.length();
            for (i = 0; i < startIndex.length / 2; i++) {
                begin = startIndex[i * 2];
                end = startIndex[(i * 2) + 1];
                if (begin >= 0 && end >= begin && end < targetLength) {
                    if (aMode == 0) {
                        if (item.equals(aNameView.getTag()) && end < this.mSpannable.length()) {
                            this.mSpannable.setSpan(new ForegroundColorSpan(this.mPrefixHighligher.getHighlightColor()), begin, end + 1, 33);
                            setMarqueeText(aNameView, this.mSpannable);
                            this.mTextHighlighted = true;
                        }
                    } else {
                        boolean isChineseOrEnglishchar;
                        if (SortUtils.isChinese(input.charAt(0)) || SortUtils.isZhuyin(input.charAt(0))) {
                            isChineseOrEnglishchar = true;
                        } else {
                            isChineseOrEnglishchar = SortUtils.isEnglish(input.charAt(0));
                        }
                        if (item.equals(aNameView.getTag()) && r16) {
                            HighlightChineseItem(begin, end, aNameView, aTarget, aTokens, item);
                        }
                    }
                }
            }
            if (startIndex.length == 0 || !this.mTextHighlighted) {
                int queryIndex = item.mName.toLowerCase().indexOf(input.toLowerCase());
                if (queryIndex > -1) {
                    int endIdx = queryIndex + input.length();
                    int maxEndIdx = this.mSpannable.toString().length();
                    if (endIdx > maxEndIdx) {
                        endIdx = maxEndIdx;
                    }
                    this.mSpannable.setSpan(new ForegroundColorSpan(this.mPrefixHighligher.getHighlightColor()), queryIndex, endIdx, 33);
                    setMarqueeText(aNameView, this.mSpannable);
                    this.mTextHighlighted = true;
                } else {
                    startIndex = SearchMatch.filterAndMatchName(item.mName, input.toLowerCase());
                    if (startIndex != null) {
                        for (i = 0; i < startIndex.length / 2; i++) {
                            begin = startIndex[i * 2];
                            end = startIndex[(i * 2) + 1];
                            if (begin >= 0 && end >= begin && end < item.mName.length()) {
                                if (item.equals(aNameView.getTag())) {
                                    this.mSpannable.setSpan(new ForegroundColorSpan(this.mPrefixHighligher.getHighlightColor()), begin, end + 1, 33);
                                    setMarqueeText(aNameView, this.mSpannable);
                                    this.mTextHighlighted = true;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void HighlightChineseItem(int aStart, int aEnd, TextView aNameView, String aKey, ArrayList<Token> aTokens, PhoneItem item) {
        Integer[] wordsIndexes = this.mDialerHighlighter.findIndexForWords(aKey, new StringBuffer());
        if (wordsIndexes != null) {
            int keyLength = aKey.length();
            String displayName = this.mSpannable.toString();
            int length = displayName.length();
            if (wordsIndexes.length <= length && wordsIndexes.length <= aTokens.size()) {
                int index = 0;
                int tempStart = 0;
                int charsToHighlight = (aEnd - aStart) + 1;
                int offset = 0;
                while (index < wordsIndexes.length) {
                    if (wordsIndexes[index].intValue() == aStart) {
                        tempStart = index + offset;
                        break;
                    }
                    if (((Token) aTokens.get(index)).type == 1 || ((Token) aTokens.get(index)).type == 4) {
                        offset += ((Token) aTokens.get(index)).source.length() - 1;
                    }
                    index++;
                    while (index + offset < length && (displayName.charAt(index + offset) == ' ' || displayName.charAt(index + offset) == '.')) {
                        offset++;
                    }
                }
                int tempEnd = tempStart;
                int i = tempStart;
                while (charsToHighlight > 0 && i < length && index < wordsIndexes.length) {
                    int tokenType = ((Token) aTokens.get(index)).type;
                    char c = displayName.charAt(i);
                    if (c == ' ' || c == '.') {
                        tempEnd++;
                    } else if (tokenType == 1) {
                        int sourceL = ((Token) aTokens.get(index)).source.length();
                        if (charsToHighlight > sourceL) {
                            charsToHighlight -= sourceL + 1;
                            tempEnd += sourceL;
                        } else {
                            tempEnd += charsToHighlight;
                            charsToHighlight = 0;
                        }
                        index++;
                        i += sourceL - 1;
                    } else if (tokenType == 2 || tokenType == 3) {
                        int intValue;
                        if (index < wordsIndexes.length - 1) {
                            intValue = wordsIndexes[index + 1].intValue() - wordsIndexes[index].intValue();
                        } else {
                            intValue = keyLength - wordsIndexes[index].intValue();
                        }
                        charsToHighlight -= intValue;
                        index++;
                        tempEnd++;
                    } else {
                        tempEnd++;
                    }
                    i++;
                }
                if (tempStart > -1 && tempEnd <= length && tempStart < tempEnd && item.equals(aNameView.getTag())) {
                    this.mSpannable.setSpan(new ForegroundColorSpan(this.mPrefixHighligher.getHighlightColor()), tempStart, tempEnd, 33);
                    setMarqueeText(aNameView, this.mSpannable);
                    this.mTextHighlighted = true;
                }
            }
        }
    }

    public void showDisplayName(Cursor cursor, int nameColumnIndex, int displayOrder) {
        showDisplayName(cursor, nameColumnIndex, displayOrder, false);
    }

    public void showDisplayName(final Cursor cursor, int nameColumnIndex, int displayOrder, final boolean aIsSCrolling) {
        CharSequence name = cursor.getString(nameColumnIndex);
        if (TextUtils.isEmpty(name)) {
            setMarqueeText(getNameTextView(), this.mUnknownNameText);
            return;
        }
        final String finalName = name.toString();
        if (aIsSCrolling && this.mHighlightedPrefix == null) {
            getNameTextView().setText(finalName);
            return;
        }
        if (getContext() instanceof Activity) {
            ((Activity) getContext()).runOnUiThread(new Runnable() {
                public void run() {
                    int sortKeyIndex;
                    if (aIsSCrolling) {
                        ContactListItemView.this.getNameTextView().setText(finalName);
                    } else {
                        ContactListItemView.this.setMarqueeText(ContactListItemView.this.getNameTextView(), finalName);
                    }
                    ContactListItemView.this.mSpannable = new SpannableString(finalName);
                    ContactListItemView.this.mTextHighlighted = false;
                    PhoneItem phoneItem = new PhoneItem(finalName, finalName, 2, null);
                    ContactListItemView.this.getNameTextView().setTag(phoneItem);
                    if (ContactDisplayUtils.getNameDisplayOrder() == 1) {
                        sortKeyIndex = cursor.getColumnIndex("sort_key");
                    } else {
                        sortKeyIndex = cursor.getColumnIndex("sort_key_alt");
                    }
                    if (sortKeyIndex != -1) {
                        phoneItem.mSortKey = cursor.getString(sortKeyIndex);
                    }
                    if (ContactListItemView.this.mHighlightedPrefix == null) {
                        return;
                    }
                    if (ContactListItemView.this.mSearchMatchType == 40 || ContactListItemView.this.mSearchMatchType == 32) {
                        ContactListItemView.this.highlighter(phoneItem, ContactListItemView.this.getNameTextView(), String.valueOf(ContactListItemView.this.mHighlightedPrefix), null);
                    }
                }
            });
        }
        if (this.mTextHighlighted && !this.mMultiSearchContacts) {
            this.mHighlightedPrefix = null;
        }
    }

    public void hideDisplayName() {
        if (this.mNameTextView != null) {
            removeView(this.mNameTextView);
            this.mNameTextView = null;
        }
    }

    public void showPhoneticName(Cursor cursor, int phoneticNameColumnIndex) {
        cursor.copyStringToBuffer(phoneticNameColumnIndex, this.mPhoneticNameBuffer);
        int phoneticNameSize = this.mPhoneticNameBuffer.sizeCopied;
        if (phoneticNameSize != 0) {
            setPhoneticName(this.mPhoneticNameBuffer.data, phoneticNameSize);
        } else {
            setPhoneticName(null, 0);
        }
    }

    public void showPresenceAndStatusMessage(Cursor cursor, int presenceColumnIndex, int contactStatusColumnIndex) {
        showPresenceAndStatusMessage(cursor, presenceColumnIndex, contactStatusColumnIndex, true);
    }

    public void showPresenceAndStatusMessage(Cursor cursor, int presenceColumnIndex, int contactStatusColumnIndex, boolean setStatus) {
        Drawable icon = null;
        int presence = 0;
        if (!cursor.isNull(presenceColumnIndex)) {
            presence = cursor.getInt(presenceColumnIndex);
            icon = ContactPresenceIconUtil.getPresenceIcon(getContext(), presence);
        }
        setPresence(icon);
        if (setStatus) {
            CharSequence statusMessage = null;
            if (!(contactStatusColumnIndex == 0 || cursor.isNull(contactStatusColumnIndex))) {
                statusMessage = cursor.getString(contactStatusColumnIndex);
            }
            if (statusMessage == null && presence != 0) {
                statusMessage = ContactStatusUtil.getStatusString(getContext(), presence);
            }
            setStatus(statusMessage);
        }
    }

    public void showSnippet(Cursor cursor) {
        String snippet = null;
        if (cursor.getColumnIndex("snippet") > -1) {
            snippet = cursor.getString(8);
        }
        Bundle extras = cursor.getExtras();
        if (extras != null && extras.getBoolean("deferred_snippeting")) {
            String query = extras.getString("deferred_snippeting_query");
            String displayName = null;
            int displayNameIndex = cursor.getColumnIndex("display_name");
            if (displayNameIndex >= 0) {
                displayName = cursor.getString(displayNameIndex);
            }
            snippet = updateSnippet(snippet, query, displayName);
        } else if (snippet != null) {
            int from = 0;
            int to = snippet.length();
            int start = snippet.indexOf(1);
            if (start == -1) {
                snippet = null;
            } else {
                int firstNl = snippet.lastIndexOf(10, start);
                if (firstNl != -1) {
                    from = firstNl + 1;
                }
                int end = snippet.lastIndexOf(1);
                if (end != -1) {
                    int lastNl = snippet.indexOf(10, end);
                    if (lastNl != -1) {
                        to = lastNl;
                    }
                }
                StringBuilder sb = new StringBuilder();
                for (int i = from; i < to; i++) {
                    char c = snippet.charAt(i);
                    if (c != '\u0001') {
                        sb.append(c);
                    }
                }
                snippet = sb.toString();
            }
        }
        if (QueryUtil.isSpecialLanguageForSearch() && snippet != null) {
            String company = cursor.getString(cursor.getColumnIndex("company"));
            this.isOrgnizationSnippet = company == null ? false : snippet.contains(company);
        }
        setSnippet(snippet);
    }

    private String updateSnippet(String snippet, String query, String displayName) {
        if (TextUtils.isEmpty(snippet) || TextUtils.isEmpty(query)) {
            return null;
        }
        query = SearchUtil.cleanStartAndEndOfSearchQuery(query.toLowerCase(Locale.getDefault()));
        if (!TextUtils.isEmpty(displayName)) {
            for (String nameToken : split(displayName.toLowerCase(Locale.getDefault()))) {
                if (nameToken.startsWith(query)) {
                    return null;
                }
            }
        }
        MatchedLine matched = SearchUtil.findMatchingLine(snippet, query);
        if (matched.line == null) {
            return null;
        }
        int lengthThreshold = getResources().getInteger(R.integer.snippet_length_before_tokenize);
        if (matched.line.length() > lengthThreshold) {
            return snippetize(matched.line, matched.startIndex, lengthThreshold);
        }
        return matched.line;
    }

    private String snippetize(String line, int matchIndex, int maxLength) {
        int index;
        int remainingLength = maxLength;
        int tempRemainingLength = maxLength;
        int endTokenIndex = matchIndex;
        for (index = matchIndex; index < line.length(); index++) {
            if (!Character.isLetterOrDigit(line.charAt(index))) {
                endTokenIndex = index;
                remainingLength = tempRemainingLength;
                break;
            }
            tempRemainingLength--;
        }
        tempRemainingLength = remainingLength;
        int startTokenIndex = matchIndex;
        for (index = matchIndex - 1; index > -1 && tempRemainingLength > 0; index--) {
            if (!Character.isLetterOrDigit(line.charAt(index))) {
                startTokenIndex = index;
                remainingLength = tempRemainingLength;
            }
            tempRemainingLength--;
        }
        tempRemainingLength = remainingLength;
        for (index = endTokenIndex; index < line.length() && tempRemainingLength > 0; index++) {
            if (!Character.isLetterOrDigit(line.charAt(index))) {
                endTokenIndex = index;
            }
            tempRemainingLength--;
        }
        StringBuilder sb = new StringBuilder();
        if (startTokenIndex > 0) {
            sb.append("...");
        }
        sb.append(line.substring(startTokenIndex, endTokenIndex));
        if (endTokenIndex < line.length()) {
            sb.append("...");
        }
        return sb.toString();
    }

    private static List<String> split(String content) {
        Matcher matcher = SPLIT_PATTERN.matcher(content);
        ArrayList<String> tokens = Lists.newArrayList();
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }

    public void showSnippetInfo(Cursor cursor) {
        if (!QueryUtil.isUseHwSearch()) {
            showSnippet(cursor);
        } else if (this.mMultiSearchContacts) {
            showSnippetInfoMultiHighlight(cursor);
        } else {
            boolean isSearchCursor;
            int matchType = -1;
            if (cursor instanceof MultiCursor) {
                isSearchCursor = ((MultiCursor) cursor).getCurrentCursor() instanceof HwSearchCursor;
            } else {
                isSearchCursor = false;
            }
            if (isSearchCursor) {
                matchType = ((HwSearchCursor) ((MultiCursor) cursor).getCurrentCursor()).getMatchType();
            }
            if (isSearchCursor && matchType != -1) {
                boolean z;
                String field = getMatchField(cursor, ((HwSearchCursor) ((MultiCursor) cursor).getCurrentCursor()).getMatchPosition(), matchType);
                String snippet = field;
                if (matchType == 48) {
                    snippet = field;
                }
                if (HwLog.HWDBG) {
                    HwLog.d("ContactListItemView", " showSnippet matchType=" + matchType);
                }
                if (matchType == 56) {
                    z = true;
                } else {
                    z = false;
                }
                this.isOrgnizationSnippet = z;
                setSnippet(snippet);
            }
        }
    }

    public void showSnippetInfoMultiHighlight(Cursor cursor) {
        boolean z;
        String field = getMatchField(cursor, this.mSnippetMatchPosition, this.mSnippetMatchType);
        String snippet = field;
        if (this.mSnippetMatchType == 56) {
            z = true;
        } else {
            z = false;
        }
        this.isOrgnizationSnippet = z;
        if (HwLog.HWDBG) {
            HwLog.d("ContactListItemView", " showSnippetInfoMultiHighlight matchType=" + this.mSnippetMatchType);
        }
        if (TextUtils.isEmpty(field)) {
            this.isOrgnizationSnippet = false;
        }
        setTextHighlighted(getSnippetView(), field, this.mMatchTypeArray, this.mSnippetMatchType);
        this.mSnippetMatchType = 40;
    }

    private void setTextHighlighted(TextView textView, String targetText, int[] matchTypeArray, int matchType) {
        if (TextUtils.isEmpty(targetText)) {
            textView.setVisibility(8);
            return;
        }
        if (EmuiFeatureManager.isChinaArea()) {
            targetText = ContactsUtils.getChinaFormatNumber(targetText);
        }
        CharSequence lHightlighedText = targetText;
        if (this.mHighlightedPrefix != null) {
            lHightlighedText = this.mPrefixHighligher.apply(targetText, String.valueOf(this.mHighlightedPrefix).split(HwCustPreloadContacts.EMPTY_STRING), matchTypeArray, matchType);
        }
        if (lHightlighedText != null) {
            textView.setText(lHightlighedText);
        }
        textView.setVisibility(0);
    }

    private boolean isHasHighlightedText(TextView textView) {
        if (textView == null) {
            return false;
        }
        CharSequence text = textView.getText();
        if (!TextUtils.isEmpty(text) && (text instanceof SpannedString)) {
            return true;
        }
        return false;
    }

    private void adjustTextInView(TextView textView) {
        if (isHasHighlightedText(textView)) {
            SpannedString fullText = (SpannedString) textView.getText();
            ForegroundColorSpan[] colorSpans = (ForegroundColorSpan[]) fullText.getSpans(0, fullText.length(), ForegroundColorSpan.class);
            if (colorSpans != null && colorSpans.length != 0) {
                int startPos = fullText.getSpanStart(colorSpans[0]);
                int endPos = fullText.getSpanEnd(colorSpans[0]);
                if (endPos != -1) {
                    String fullTextString = fullText.toString();
                    float textFieldWidth = (float) textView.getMeasuredWidth();
                    float textSize = textView.getTextSize();
                    Paint paint = new Paint();
                    paint.setTextSize(textSize);
                    float fullTextWidth = paint.measureText(fullTextString);
                    int bodyLength = fullTextString.length();
                    int start = startPos;
                    int end = endPos;
                    int offset = 0;
                    if (fullTextWidth > textFieldWidth) {
                        textFieldWidth -= paint.measureText(mEllipsis) * 2.0f;
                        while (true) {
                            offset++;
                            int newstart = Math.max(0, startPos - offset);
                            int newend = Math.min(bodyLength, endPos + offset);
                            if ((newstart == start && newend == end) || paint.measureText(fullTextString.substring(newstart, end)) > textFieldWidth) {
                                break;
                            }
                            start = newstart;
                            if (paint.measureText(fullTextString.substring(newstart, newend)) > textFieldWidth) {
                                break;
                            }
                            end = newend;
                        }
                        SpannableStringBuilder spannable = new SpannableStringBuilder();
                        spannable.append(start == 0 ? "" : mEllipsis);
                        spannable.append(fullText.subSequence(start, end));
                        spannable.append(end == bodyLength ? "" : mEllipsis);
                        textView.setText(spannable);
                    }
                }
            }
        }
    }

    public String getMatchField(Cursor cursor, int matchPosition, int matchType) {
        int index;
        String str = null;
        switch (matchType) {
            case Place.TYPE_HINDU_TEMPLE /*48*/:
                index = cursor.getColumnIndex("number_ori");
                break;
            case Place.TYPE_LIQUOR_STORE /*56*/:
                index = cursor.getColumnIndex("organization_ori");
                break;
            case Place.TYPE_LOCAL_GOVERNMENT_OFFICE /*57*/:
                index = cursor.getColumnIndex("nick_name_ori");
                break;
            case Place.TYPE_LOCKSMITH /*58*/:
                index = cursor.getColumnIndex("email_ori");
                break;
            case Place.TYPE_LODGING /*59*/:
                index = cursor.getColumnIndex("address_ori");
                break;
            case Place.TYPE_MOSQUE /*62*/:
                index = cursor.getColumnIndex("note_ori");
                break;
            case Place.TYPE_MOVIE_RENTAL /*63*/:
                index = cursor.getColumnIndex("im_ori");
                break;
            default:
                return null;
        }
        if (index >= 0 && matchPosition > 0) {
            try {
                String field = cursor.getString(index);
                if (TextUtils.isEmpty(field)) {
                    return null;
                }
                String[] sourceStrArray = HwSearchCursor.splitString(field);
                return HwSearchCursor.replaceString(matchPosition > sourceStrArray.length ? null : sourceStrArray[matchPosition - 1]);
            } catch (Exception e) {
                HwLog.e("ContactListItemView", "getMatchField:" + e.getMessage());
            }
        }
        if (index >= 0) {
            str = cursor.getString(index);
        }
        return str;
    }

    public void showCompany(Cursor cursor, int companyIndex, int titleIndex) {
        StringBuilder sb = new StringBuilder();
        boolean hasCompany = false;
        if (companyIndex > -1) {
            String company = cursor.getString(companyIndex);
            if (!TextUtils.isEmpty(company)) {
                sb.append(company);
                hasCompany = true;
            }
        }
        if (titleIndex > -1) {
            String title = cursor.getString(titleIndex);
            if (!TextUtils.isEmpty(title)) {
                if (hasCompany) {
                    sb.append(HwCustPreloadContacts.EMPTY_STRING);
                }
                sb.append(title);
            }
        }
        if (!this.mMultiSearchContacts || this.isOrgnizationSnippet) {
            setCompany(sb.toString());
        } else {
            setTextHighlighted(getCompanyView(), sb.toString(), this.mMatchTypeArray, 56);
        }
    }

    public void showData(Cursor cursor, int dataColumnIndex) {
        cursor.copyStringToBuffer(dataColumnIndex, this.mDataBuffer);
        setData(this.mDataBuffer.data, this.mDataBuffer.sizeCopied);
    }

    public void showPhoneNumber(CharSequence phone, CharSequence label) {
        if (TextUtils.isEmpty(phone)) {
            setSnippet(null);
        } else {
            setSnippet(label == null ? "" : label.toString(), phone.toString());
        }
    }

    public void setActivatedStateSupported(boolean flag) {
        this.mActivatedStateSupported = flag;
    }

    public void requestLayout() {
        forceLayout();
    }

    public void setAccountIcons(Bitmap[] aAccIconsInfo) {
        if (aAccIconsInfo == null) {
            this.mAccIconsInfo = new Bitmap[0];
        } else {
            this.mAccIconsInfo = (Bitmap[]) aAccIconsInfo.clone();
        }
    }

    public void setAccountIcons(Bitmap[] aAccIconsInfo, String[] accountTypeDescriptions) {
        if (aAccIconsInfo == null) {
            this.mAccIconsInfo = new Bitmap[0];
            this.mAccountTypeDescriptions = new String[0];
            return;
        }
        this.mAccIconsInfo = (Bitmap[]) aAccIconsInfo.clone();
        this.mAccountTypeDescriptions = (String[]) accountTypeDescriptions.clone();
    }

    public boolean isChecked() {
        if (HwLog.HWDBG) {
            HwLog.d("ContactListItemView", "isChecked() is called!");
        }
        return this.mIsChecked;
    }

    public void setChecked(boolean checked) {
        this.mIsChecked = checked;
        if (HwLog.HWDBG) {
            HwLog.d("ContactListItemView", "setChecked() is called!, checked: [" + checked + "]");
        }
        this.mCheckBox.setChecked(this.mIsChecked);
        setActivated(this.mIsChecked);
    }

    public void toggle() {
        if (HwLog.HWDBG) {
            HwLog.d("ContactListItemView", "toggle() is called!, mIsChecked : [" + this.mIsChecked + "]");
        }
        this.mIsChecked = !this.mIsChecked;
        setActivated(this.mIsChecked);
    }

    public void setSimAccountIndicatorInfo(Bitmap aBitmap) {
        this.mSimAccountIndicatorInfo = aBitmap;
    }

    public void setAccountTypeToSimAccount(boolean isSimAccount) {
        this.mIsSimAccount = isSimAccount;
    }

    public boolean isSimAccount() {
        return this.mIsSimAccount;
    }

    public void setHorizontalDividerPadding(int paddingStart, int paddingEnd) {
        this.mHDividerPaddingEnd = paddingEnd;
        this.mHDividerPaddingStart = paddingStart;
    }

    public void setScaleX(float scaleX) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (!(view == null || view.equals(this.mShadowView) || view.equals(this.mHeaderSectionView) || view.equals(this.mHeaderTextView) || view.equals(this.mAccountFilterView))) {
                view.setScaleX(scaleX);
            }
        }
    }

    public void setScaleY(float scaleY) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (!(view == null || view.equals(this.mShadowView) || view.equals(this.mHeaderSectionView) || view.equals(this.mHeaderTextView) || view.equals(this.mAccountFilterView))) {
                view.setScaleY(scaleY);
            }
        }
    }

    private void setViewPivot(View view) {
        if (view != null) {
            view.setPivotX((float) ((getMeasuredWidth() / 2) - view.getLeft()));
            view.setPivotY(getRotationY());
        }
    }
}
