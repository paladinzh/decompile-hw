package com.android.contacts;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.SparseArray;
import android.widget.TextView;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.calllog.CallLogAdapter;
import com.android.contacts.calllog.CallLogListItemViews;
import com.android.contacts.calllog.CallTypeHelper;
import com.android.contacts.calllog.PhoneNumberHelper;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.rcs.dialer.RcsPhoneCallDetailHelper;
import com.android.contacts.hap.service.NumberMarkInfo;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.util.Constants;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.TextUtil;
import com.google.android.gms.R;
import com.huawei.cust.HwCustUtils;
import huawei.android.widget.TimeAxisWidget;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PhoneCallDetailsHelper {
    private HashMap<String, CallData> detailsCache;
    private HwCustPhoneCallDetailsHelper hwCustPhoneCallDetailsHelper = null;
    private float mCallLogDurationTextSize;
    private float mCallLogFirstLineTextSize;
    private float mCallLogMissedTextSize;
    private int mCallLogThridLineWidth;
    private int mCallTypeFeatureIconViewWidth;
    private final CallTypeHelper mCallTypeHelper;
    private int mCardTypeViewWidth;
    private Context mContext;
    private DateFormat mDateFormatter = null;
    private int mDistanceBetweenItems;
    private SparseArray<Drawable> mDrawableArray = new SparseArray();
    private boolean mIsRingtimesEnabled;
    private int mItemMaxWidth;
    private int mOutgoingIconViewWidth;
    private Paint mPaint;
    private final PhoneNumberHelper mPhoneNumberHelper;
    private int mRoundSidesWidth;
    private boolean mSaved24HourFormat;
    private String mSavedDateFormat;
    private float mSecondLineTextSize;
    private String mStrUnkownLocation = null;
    private int mSvgIconWidth;
    private int mVoicemailIconViewWidth;
    private int mWidthBetweenSvgAndText;

    static class CallData {
        CallData() {
        }
    }

    public PhoneCallDetailsHelper(Context context, CallTypeHelper callTypeHelper, PhoneNumberHelper phoneNumberHelper) {
        this.mContext = context;
        this.mCallTypeHelper = callTypeHelper;
        this.mPhoneNumberHelper = phoneNumberHelper;
        this.detailsCache = new HashMap();
        resetTimeFormats();
        this.mIsRingtimesEnabled = EmuiFeatureManager.isRingTimesDisplayEnabled(this.mContext);
        this.mPaint = new Paint();
        this.mItemMaxWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.call_log_first_line_name_view_width);
        this.mDistanceBetweenItems = this.mContext.getResources().getDimensionPixelSize(R.dimen.call_log_second_line_item_distance);
        this.mRoundSidesWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_round_sides_width);
        this.mSecondLineTextSize = (float) this.mContext.getResources().getDimensionPixelSize(R.dimen.call_log_second_line_text_size);
        this.mOutgoingIconViewWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.call_log_first_line_outgoingicon_width);
        this.mVoicemailIconViewWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.call_log_first_line_voicemail_width);
        this.mCardTypeViewWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.call_log_second_line_cardtype_width);
        this.mDateFormatter = android.text.format.DateFormat.getDateFormat(context);
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.hwCustPhoneCallDetailsHelper = (HwCustPhoneCallDetailsHelper) HwCustUtils.createObj(HwCustPhoneCallDetailsHelper.class, new Object[]{context});
        }
        this.mCallLogFirstLineTextSize = this.mContext.getResources().getDimension(R.dimen.call_log_first_line_text_size);
        this.mCallTypeFeatureIconViewWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.call_log_second_line_calltype_width);
        this.mCallLogDurationTextSize = this.mContext.getResources().getDimension(R.dimen.contact_calllog_item_duration_font);
        this.mCallLogMissedTextSize = this.mContext.getResources().getDimension(R.dimen.call_log_first_line_text_size);
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            this.mCallLogThridLineWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.rcs_call_log_list_item_third_line_default_width_land);
            this.mSvgIconWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.rcs_call_log_list_item_svg_icon_width);
            this.mWidthBetweenSvgAndText = this.mContext.getResources().getDimensionPixelSize(R.dimen.rcs_call_log_list_item_padding_between_icon_and_text);
        }
    }

    public void updateCustSetting() {
        if (this.hwCustPhoneCallDetailsHelper != null) {
            this.hwCustPhoneCallDetailsHelper.updateCustSetting();
        }
    }

    private Drawable getDrawable(int resId) {
        Drawable ret = (Drawable) this.mDrawableArray.get(resId);
        if (ret != null) {
            return ret;
        }
        ret = this.mContext.getResources().getDrawable(resId);
        this.mDrawableArray.put(resId, ret);
        return ret;
    }

    public void setPhoneCallDetails(PhoneCallDetailsViews views, PhoneCallDetails details, boolean isHighlighted, String callId, boolean aFling, boolean aIsFromDialer) {
        int count = details.callTypes.length;
        int mCallsType = details.callTypes[0];
        if (CommonUtilMethods.isMissedType(mCallsType) || 2 == mCallsType || 1 == mCallsType) {
            int totalMissedCallCount = count;
            if (count <= 1) {
                views.missedCallCount.setVisibility(8);
            } else {
                views.missedCallCount.setText(this.mContext.getResources().getString(R.string.contacts_call_num_count, new Object[]{Integer.valueOf(count)}));
                views.missedCallCount.setContentDescription(String.format(this.mContext.getResources().getQuantityText(R.plurals.content_description_contacts_call_count, count).toString(), new Object[]{Integer.valueOf(count)}));
                if (CommonUtilMethods.isMissedType(mCallsType)) {
                    views.missedCallCount.setTextColor(this.mContext.getResources().getColor(R.color.call_log_missed_call_count));
                } else {
                    views.missedCallCount.setTextColor(this.mContext.getResources().getColor(R.color.call_log_primary_text_color));
                }
                views.missedCallCount.setVisibility(0);
                reSetTextSize(views.missedCallCount, this.mCallLogMissedTextSize);
            }
        } else {
            views.missedCallCount.setVisibility(8);
        }
        boolean mCallTypeIconVisibilty = true;
        if (1 == details.getCallsTypeFeatures()) {
            if (2 == mCallsType) {
                views.outgoingIcon.setImageDrawable(getDrawable(R.drawable.ic_video_call_out));
                views.outgoingIcon.setContentDescription(this.mContext.getString(R.string.content_description_outgoing_video_call));
            } else if (1 == mCallsType) {
                views.outgoingIcon.setImageDrawable(getDrawable(R.drawable.ic_video_call_incoming_normal));
                views.outgoingIcon.setContentDescription(this.mContext.getString(R.string.content_description_incoming_video_call));
            } else if (!CommonUtilMethods.isMissedType(mCallsType)) {
                mCallTypeIconVisibilty = false;
            } else if (3 == mCallsType) {
                views.outgoingIcon.setImageDrawable(getDrawable(R.drawable.ic_video_call_incoming_missed));
                views.outgoingIcon.setContentDescription(this.mContext.getString(R.string.content_description_missed_video_call));
            } else if (5 == mCallsType) {
                views.outgoingIcon.setImageDrawable(getDrawable(R.drawable.ic_video_call_incoming_reject));
                views.outgoingIcon.setContentDescription(this.mContext.getString(R.string.content_description_rejected_video_call));
            }
        } else if (2 == mCallsType) {
            views.outgoingIcon.setImageDrawable(getDrawable(R.drawable.ic_call_out));
            views.outgoingIcon.setContentDescription(this.mContext.getString(R.string.content_description_outgoing_call));
        } else if (1 == mCallsType) {
            views.outgoingIcon.setImageDrawable(getDrawable(R.drawable.ic_call_incoming_normal));
            views.outgoingIcon.setContentDescription(this.mContext.getString(R.string.content_description_incoming_call));
        } else if (!CommonUtilMethods.isMissedType(mCallsType)) {
            mCallTypeIconVisibilty = false;
        } else if (3 == mCallsType) {
            views.outgoingIcon.setImageDrawable(getDrawable(R.drawable.ic_video_call_incoming_missed));
            views.outgoingIcon.setContentDescription(this.mContext.getString(R.string.content_description_missed_call));
        } else if (5 == mCallsType) {
            views.outgoingIcon.setImageDrawable(getDrawable(R.drawable.ic_video_call_incoming_reject));
            views.outgoingIcon.setContentDescription(this.mContext.getString(R.string.content_description_rejected_call));
        }
        if (mCallTypeIconVisibilty) {
            views.outgoingIcon.setVisibility(0);
        } else {
            views.outgoingIcon.setVisibility(8);
        }
        if (4 == details.callTypes[0]) {
            views.voicemailIcon.setVisibility(0);
            if (isHighlighted) {
                views.voicemailIcon.setContentDescription(this.mContext.getString(R.string.content_description_unread_voicemail));
            }
        } else {
            views.voicemailIcon.setVisibility(8);
        }
        if (views.mWorkIcon != null) {
            views.mWorkIcon.setVisibility(details.contactUserType == 1 ? 0 : 8);
        }
        if (this.hwCustPhoneCallDetailsHelper != null) {
            this.hwCustPhoneCallDetailsHelper.checkCallTypeFeaturesVisibility(views, details);
        }
        if (((CallData) this.detailsCache.get(callId)) != null) {
            setTimeAxisWidget(views, details, aFling);
            if (this.mIsRingtimesEnabled && views.mRingTimes != null) {
                views.mRingTimes.setVisibility(8);
            }
        } else {
            this.detailsCache.put(callId, new CallData());
            setTimeAxisWidget(views, details, aFling);
        }
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            RcsPhoneCallDetailHelper.displayPreCallView(this.mContext, views, details);
        }
        displayNameAndNumber(views, details, aFling, aIsFromDialer, isHighlighted);
    }

    private void setTimeAxisWidget(PhoneCallDetailsViews views, PhoneCallDetails details, boolean aFling) {
        if (views.timeAxisWidget != null && EmuiVersion.isSupportEmui3()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(details.date);
            if (views.timeAxisWidget instanceof TimeAxisWidget) {
                TimeAxisWidget lTimeAxis = views.timeAxisWidget;
                lTimeAxis.setCalendar(calendar);
                if (isSameDateWithToday(details.date)) {
                    lTimeAxis.setMode(2);
                } else {
                    lTimeAxis.setMode(1);
                }
            }
        }
    }

    private boolean isLocationShown(PhoneCallDetails details, boolean isVoiceMailNumber) {
        if (TextUtils.isEmpty(details.geocode) || isVoiceMailNumber || details.mIsEmergencyNumber || ((this.hwCustPhoneCallDetailsHelper != null && this.hwCustPhoneCallDetailsHelper.isCustHideGeoInfo41()) || (EmuiFeatureManager.isHideUnknownGeo() && details.geocode.equals(getUnkownLocation())))) {
            return true;
        }
        return ContactsUtils.isUnknownNumber(details.getPresentation());
    }

    public void displayNameAndNumber(PhoneCallDetailsViews views, PhoneCallDetails details, boolean aFling, boolean aIsFromDialer, boolean isHighlighted) {
        CharSequence nameText;
        boolean lDisplayNameExists;
        boolean isVoiceMailNumber = details.isVoicemailNumber;
        Integer highlightColor = this.mCallTypeHelper.getHighlightedColor(details.callTypes[0]);
        CharSequence displayNumber = this.mPhoneNumberHelper.getDisplayNumber(details.number, details.getPresentation(), details.formattedNumber, details.postDialDigits, details.isVoicemailNumber);
        boolean lLocationShown = true;
        if (isLocationShown(details, isVoiceMailNumber)) {
            lLocationShown = false;
        }
        boolean lLocationViewExists = views.mLocationView != null;
        if (!TextUtils.isEmpty(details.name) && !details.isVoicemailNumber) {
            nameText = details.name;
            lDisplayNameExists = true;
        } else if (details.isVoicemailNumber) {
            nameText = displayNumber;
            lDisplayNameExists = true;
        } else if (details.numberMark == null || !details.numberMark.isBrandInfo() || TextUtils.isEmpty(details.numberMarkInfo)) {
            nameText = displayNumber;
            lDisplayNameExists = false;
        } else {
            Object nameText2 = details.numberMarkInfo;
            lDisplayNameExists = true;
        }
        if (highlightColor == null || (aIsFromDialer && details.contactUri != null)) {
            views.nameView.setTextColor(this.mContext.getResources().getColor(R.color.call_log_primary_text_color));
        } else if (details.callTypes[0] == 4) {
            int intValue;
            TextView textView = views.nameView;
            if (isHighlighted) {
                intValue = highlightColor.intValue();
            } else {
                intValue = this.mContext.getResources().getColor(R.color.call_log_primary_text_color);
            }
            textView.setTextColor(intValue);
        } else {
            views.nameView.setTextColor(highlightColor.intValue());
        }
        views.nameView.setText(nameText);
        if (Constants.isEXTRA_HUGE()) {
            views.nameView.setTextSize(1, 28.0f);
        } else {
            views.nameView.setTextSize(0, this.mCallLogFirstLineTextSize);
        }
        StringBuilder stringBuilder = new StringBuilder(200);
        stringBuilder = new StringBuilder(200);
        if (lDisplayNameExists) {
            if (lLocationShown && lLocationViewExists) {
                if (TextUtils.isEmpty(details.geocode) || details.geocode.equals(getUnkownLocation())) {
                    if (details.isVoicemailNumber) {
                        displayNumber = details.formattedNumber;
                    }
                    formatNumber(stringBuilder, displayNumber);
                    stringBuilder.append(stringBuilder);
                    views.mLocationView.setVisibility(8);
                    views.mLocationView.setText("");
                } else if (details.numberMark == null || !details.numberMark.isBrandInfo() || TextUtils.isEmpty(details.numberMarkInfo)) {
                    views.mLocationView.setVisibility(0);
                    views.mLocationView.setText(details.geocode);
                } else {
                    formatNumber(stringBuilder, displayNumber);
                    stringBuilder.append(stringBuilder);
                    views.mLocationView.setVisibility(8);
                    views.mLocationView.setText("");
                }
            } else if (lLocationViewExists) {
                views.mLocationView.setVisibility(8);
                if (!isShownNumberLocation(details)) {
                    if (details.isVoicemailNumber) {
                        displayNumber = details.formattedNumber;
                    }
                    formatNumber(stringBuilder, displayNumber);
                    stringBuilder.append(stringBuilder);
                }
            }
        } else if (!TextUtils.isEmpty(details.numberMarkInfo)) {
            stringBuilder.append(details.numberMarkInfo);
            stringBuilder.append(stringBuilder);
            if (lLocationViewExists) {
                views.mLocationView.setVisibility(8);
                views.mLocationView.setText("");
            }
        } else if (aIsFromDialer || views.mLocationView == null) {
            if (lLocationShown) {
                stringBuilder.append(details.geocode);
                stringBuilder.append(stringBuilder);
            } else {
                views.numberView.setText("");
            }
            if (lLocationViewExists) {
                views.mLocationView.setVisibility(8);
            }
        } else {
            views.mLocationView.setVisibility(0);
            if (lLocationShown) {
                views.mLocationView.setText(details.geocode);
                if (stringBuilder.length() > 0) {
                    stringBuilder.append("  ");
                }
                stringBuilder.append(details.geocode);
            } else if (details.getPresentation() != 2) {
                views.mLocationView.setVisibility(8);
            } else if (EmuiFeatureManager.isHideUnknownGeo() && (TextUtils.isEmpty(details.geocode) || details.geocode.equals(getUnkownLocation()))) {
                views.mLocationView.setVisibility(8);
            } else {
                views.mLocationView.setVisibility(0);
                views.mLocationView.setText(getUnkownLocation());
            }
        }
        String number = stringBuilder.toString().trim();
        adjustViewWidth(views, number);
        if (views.numberView.getMinWidth() > 0) {
            if (views.mLocationView != null && views.mLocationView.getVisibility() == 0) {
                views.mLocationView.setVisibility(8);
            }
            views.numberView.setText(stringBuilder.toString().trim());
        } else {
            views.numberView.setText(number);
        }
        reSetTextSize(views.mLocationView, this.mCallLogDurationTextSize);
        reSetTextSize(views.numberView, this.mCallLogDurationTextSize);
        if (views.mEspaceView != null) {
            views.mEspaceView.setVisibility(8);
            if (details.mCallsTypeFeatures == 32) {
                if (lLocationViewExists && lLocationShown) {
                    views.mLocationView.setVisibility(8);
                }
                setNumberView(details.numberMark, views, details);
                if (views.cardType != null) {
                    views.cardType.setVisibility(8);
                }
                views.mEspaceView.setVisibility(0);
            }
        }
    }

    private void setNumberView(NumberMarkInfo numberMark, PhoneCallDetailsViews views, PhoneCallDetails details) {
        if (numberMark != null && numberMark.getName() == null) {
            views.numberView.setText("");
        }
        if (numberMark == null) {
            views.numberView.setText("");
        }
        if (numberMark != null && numberMark.isBrandInfo() && !TextUtils.isEmpty(details.numberMarkInfo)) {
            views.numberView.setText("");
        }
    }

    private void formatNumber(StringBuilder stringBuilder, CharSequence charSequence) {
        if (stringBuilder != null && !TextUtils.isEmpty(charSequence)) {
            stringBuilder.append('‪');
            stringBuilder.append(charSequence);
            stringBuilder.append('‬');
        }
    }

    private String getUnkownLocation() {
        if (this.mStrUnkownLocation == null) {
            this.mStrUnkownLocation = this.mContext.getResources().getString(R.string.numberLocationUnknownLocation2);
        }
        return this.mStrUnkownLocation;
    }

    private boolean isShownNumberLocation(PhoneCallDetails details) {
        if (details == null) {
            return false;
        }
        if (EmuiFeatureManager.isProductCustFeatureEnable() && this.hwCustPhoneCallDetailsHelper != null && this.hwCustPhoneCallDetailsHelper.isShowVMNumInCalllog(details.isVoicemailNumber)) {
            return false;
        }
        return ContactsUtils.isUnknownNumber(details.getPresentation());
    }

    private void reSetTextSize(TextView textview, float size) {
        if (textview != null) {
            textview.setTextSize(0, size);
        }
    }

    private void adjustViewWidth(PhoneCallDetailsViews views, String strNumber) {
        int orientation = this.mContext.getResources().getConfiguration().orientation;
        int maxWidth = this.mItemMaxWidth;
        if ((this.mContext instanceof PeopleActivity) && 2 == orientation) {
            maxWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.call_log_first_line_name_view_land_width);
            if (EmuiFeatureManager.isRcsFeatureEnable() && views.mRcsPreCallView != null) {
                if (views.mRcsImportantIcon.getVisibility() == 0) {
                    views.mRcsSubject.setMaxWidth((this.mCallLogThridLineWidth - this.mSvgIconWidth) - this.mWidthBetweenSvgAndText);
                } else {
                    views.mRcsSubject.setMaxWidth(this.mCallLogThridLineWidth);
                }
            }
        }
        adjustNameViewWidth(views, maxWidth, this.mDistanceBetweenItems);
        adjustNumberViewWidth(views, maxWidth, this.mDistanceBetweenItems, strNumber);
    }

    private void adjustNameViewWidth(PhoneCallDetailsViews views, int maxWidth, int distanceBetweenItems) {
        if (views.missedCallCount == null || views.missedCallCount.getVisibility() != 0) {
            views.nameView.setWidth(maxWidth);
            return;
        }
        int nameViewWidth = TextUtil.getTextWidth(this.mPaint, views.nameView.getText().toString(), views.nameView.getTextSize());
        int sizeofNameViewToLeft = (maxWidth - distanceBetweenItems) - (this.mRoundSidesWidth + TextUtil.getTextWidth(this.mPaint, views.missedCallCount.getText().toString(), views.missedCallCount.getTextSize()));
        if (sizeofNameViewToLeft <= 0) {
            HwLog.e("PhoneCallDetailsHelper", "Invalid room!!! sizeofNameViewToLeft=" + sizeofNameViewToLeft);
        } else if (sizeofNameViewToLeft < nameViewWidth) {
            views.nameView.setMinWidth(0);
            views.nameView.setMaxWidth(sizeofNameViewToLeft);
        } else {
            views.nameView.setMinWidth(nameViewWidth);
            views.nameView.setMaxWidth(maxWidth);
        }
    }

    public static void adjustNameViewWidth(CallLogListItemViews views, int maxWidth, int distanceBetweenItems, int roundSidesWidth) {
        if (views != null) {
            TextView missedCallCount = (TextView) views.primaryActionView.findViewById(R.id.missed_call);
            TextView nameView = (TextView) views.primaryActionView.findViewById(R.id.name);
            if (nameView != null) {
                Paint paint = new Paint();
                if (missedCallCount == null || missedCallCount.getVisibility() != 0) {
                    nameView.setWidth(maxWidth);
                } else {
                    int nameViewWidth = TextUtil.getTextWidth(paint, nameView.getText().toString(), nameView.getTextSize());
                    int sizeofNameViewToLeft = (maxWidth - distanceBetweenItems) - (TextUtil.getTextWidth(paint, missedCallCount.getText().toString(), missedCallCount.getTextSize()) + roundSidesWidth);
                    if (sizeofNameViewToLeft > 0) {
                        if (sizeofNameViewToLeft < nameViewWidth) {
                            nameView.setMinWidth(0);
                            nameView.setMaxWidth(sizeofNameViewToLeft);
                        } else {
                            nameView.setMinWidth(nameViewWidth);
                            nameView.setMaxWidth(maxWidth);
                        }
                    }
                }
            }
        }
    }

    private void adjustNumberViewWidth(PhoneCallDetailsViews views, int maxWidth, int distanceBetweenItems, String strNumber) {
        int sizeOfOtherItemsExceptNumberView = 0;
        int numberViewWidth = TextUtil.getTextWidth(this.mPaint, strNumber, this.mSecondLineTextSize);
        int locationViewWidth = views.mLocationView != null ? TextUtil.getTextWidth(this.mPaint, views.mLocationView.getText().toString(), this.mSecondLineTextSize) : 0;
        int ringTimesViewWidth = views.mRingTimes != null ? TextUtil.getTextWidth(this.mPaint, views.mRingTimes.getText().toString(), this.mSecondLineTextSize) : 0;
        int numDividers = 0;
        if (views.mLocationView != null && views.mLocationView.getVisibility() == 0 && locationViewWidth > 0) {
            numDividers = 1;
            sizeOfOtherItemsExceptNumberView = locationViewWidth + 0;
        }
        if (views.mRingTimes != null && views.mRingTimes.getVisibility() == 0 && ringTimesViewWidth > 0) {
            numDividers++;
            sizeOfOtherItemsExceptNumberView += ringTimesViewWidth;
        }
        if (views.outgoingIcon != null && views.outgoingIcon.getVisibility() == 0) {
            numDividers++;
            sizeOfOtherItemsExceptNumberView += this.mOutgoingIconViewWidth;
        }
        if (views.hdcallIcon != null && views.hdcallIcon.getVisibility() == 0) {
            numDividers++;
            sizeOfOtherItemsExceptNumberView += this.mCallTypeFeatureIconViewWidth;
        }
        if (views.voicemailIcon != null && views.voicemailIcon.getVisibility() == 0) {
            numDividers++;
            sizeOfOtherItemsExceptNumberView += this.mVoicemailIconViewWidth;
        }
        if (views.cardType != null && views.cardType.getVisibility() == 0) {
            numDividers++;
            sizeOfOtherItemsExceptNumberView += this.mCardTypeViewWidth;
        }
        int sizeOfNumberViewToLeft = (maxWidth - sizeOfOtherItemsExceptNumberView) - (distanceBetweenItems * numDividers);
        if (sizeOfNumberViewToLeft <= 0) {
            HwLog.e("PhoneCallDetailsHelper", "Invalid room!!! sizeOfNumberViewToLeft=" + sizeOfNumberViewToLeft);
        } else if (sizeOfNumberViewToLeft < numberViewWidth) {
            views.numberView.setMinWidth(0);
            views.numberView.setMaxWidth(sizeOfNumberViewToLeft);
        } else {
            views.numberView.setMinWidth(numberViewWidth);
            views.numberView.setMaxWidth(maxWidth);
        }
    }

    public void setCallDetailsHeader(TextView nameView, PhoneCallDetails details, int aOrientation, ActionBar aActionBar) {
        CharSequence nameText;
        CharSequence nameTemp = null;
        if (CallLogAdapter.getCust() != null) {
            nameTemp = CallLogAdapter.getCust().setSdnName((String) details.number, (String) details.name);
        }
        if (!TextUtils.isEmpty(nameTemp)) {
            nameText = nameTemp;
            if (aActionBar != null) {
                aActionBar.setTitle(nameText);
            }
        } else if (TextUtils.isEmpty(details.name)) {
            String predefinedName = ContactsUtils.getEmergencyOrHotlineName(this.mContext, (String) details.number);
            if (!TextUtils.isEmpty(predefinedName) && details.contactUri == null) {
                Object nameText2 = predefinedName;
                if (aActionBar != null) {
                    aActionBar.setTitle(predefinedName);
                }
            } else if (ContactsUtils.displayEmergencyNumber(this.mContext) && CommonUtilMethods.isEmergencyNumber((String) details.number, SimFactoryManager.isDualSim())) {
                nameText = this.mContext.getResources().getString(R.string.emergency_number);
                if (aActionBar != null) {
                    aActionBar.setTitle(nameText);
                }
            } else {
                CharSequence displayNumber = this.mPhoneNumberHelper.getDisplayNumber(details.number, details.getPresentation(), "", details.postDialDigits);
                nameText = displayNumber;
                if (aActionBar != null) {
                    if (this.mPhoneNumberHelper.canPlaceCallsTo(details.number, details.getPresentation())) {
                        displayNumber = details.number;
                    }
                    aActionBar.setTitle(displayNumber);
                }
            }
        } else {
            nameText = details.name;
            if (aActionBar != null) {
                aActionBar.setTitle(details.name);
            }
        }
        nameView.setVisibility(0);
        nameView.requestLayout();
        nameView.setText(nameText);
    }

    public void saveTimeFormatFlag() {
        if (this.mContext != null) {
            String str;
            this.mSaved24HourFormat = android.text.format.DateFormat.is24HourFormat(this.mContext);
            this.mSavedDateFormat = System.getString(this.mContext.getContentResolver(), "date_format");
            if (this.mSavedDateFormat == null) {
                str = "null";
            } else {
                str = this.mSavedDateFormat;
            }
            this.mSavedDateFormat = str;
        }
    }

    public boolean restoreTimeFormatFlag() {
        if (this.mSavedDateFormat == null || this.mContext == null) {
            return true;
        }
        boolean is24HourFormat = android.text.format.DateFormat.is24HourFormat(this.mContext);
        boolean changed = is24HourFormat != this.mSaved24HourFormat;
        this.mSaved24HourFormat = is24HourFormat;
        if (changed) {
            return true;
        }
        String strDateFormat = System.getString(this.mContext.getContentResolver(), "date_format");
        if (strDateFormat == null) {
            strDateFormat = "null";
        }
        if (strDateFormat.compareTo(this.mSavedDateFormat) == 0) {
            changed = false;
        } else {
            changed = true;
        }
        return changed;
    }

    public void resetTimeFormats() {
        this.detailsCache.clear();
    }

    private boolean isSameDateWithToday(long time) {
        return this.mDateFormatter.format(Long.valueOf(time)).equals(this.mDateFormatter.format(Long.valueOf(System.currentTimeMillis())));
    }
}
