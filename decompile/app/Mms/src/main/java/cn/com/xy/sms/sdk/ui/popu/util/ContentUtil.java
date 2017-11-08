package cn.com.xy.sms.sdk.ui.popu.util;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.bubbleview.DuoquBubbleViewCache;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseManager;
import cn.com.xy.sms.util.SdkCallBack;
import com.google.android.gms.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

public class ContentUtil {
    public static final String CHINESE = "[一-龥]";
    public static final String COLLAPSE = getResourceString(Constant.getContext(), R.string.duoqu_collapse);
    public static final String COR_GRAY = "#7F000000";
    public static final String COR_LIGHT_BLUE = "#D4EEFB";
    public static final int COR_RED = getResourceColor(Constant.getContext(), R.color.duoqu_red);
    public static final String COR_WHITE = "#ffffff";
    public static final String DUOQU_IS_SAFE_VERIFY_CODE_KEY = "isSecret";
    public static final String DUOQU_SMS_CONTENT_KEY = "view_sms_content";
    public static final String ELECTRIC = getResourceString(Constant.getContext(), R.string.duoqu_electric_txt);
    public static final String EXPAND = getResourceString(Constant.getContext(), R.string.duoqu_expand);
    public static final String EXPRESS_STATUS_DELIVERING = getResourceString(Constant.getContext(), R.string.duoqu_delivering);
    public static final String FIGHT_STATE_AHEAD_CANCEL = getResourceString(Constant.getContext(), R.string.duoqu_fight_state_ahead_cancel);
    public static final String FIGHT_STATE_ALTERNATE = getResourceString(Constant.getContext(), R.string.duoqu_fight_state_alternate);
    public static final String FIGHT_STATE_ALTERNATE_ARRIVAL = getResourceString(Constant.getContext(), R.string.duoqu_train_date_alternate_arrival);
    public static final String FIGHT_STATE_ALTERNATE_CANCEL = getResourceString(Constant.getContext(), R.string.duoqu_fight_state_alternate_cancel);
    public static final String FIGHT_STATE_ARRIVAL = getResourceString(Constant.getContext(), R.string.duoqu_train_date_arrival);
    public static final String FIGHT_STATE_CANCEL = getResourceString(Constant.getContext(), R.string.duoqu_fight_state_cancel);
    public static final String FIGHT_STATE_DELAYS = getResourceString(Constant.getContext(), R.string.duoqu_fight_state_delays);
    public static final String FIGHT_STATE_DEPARTURE = getResourceString(Constant.getContext(), R.string.duoqu_fight_state_departure);
    public static final String FIGHT_STATE_PLAN = getResourceString(Constant.getContext(), R.string.duoqu_fight_state_plan);
    public static final String FIGHT_STATE_PLAN_ALTERNATE = getResourceString(Constant.getContext(), R.string.duoqu_fight_state_alternate_departure);
    public static final String FIGHT_STATE_RETURN = getResourceString(Constant.getContext(), R.string.duoqu_fight_state_return);
    public static final String FIGHT_STATE_RETURN_ARRIVAL = getResourceString(Constant.getContext(), R.string.duoqu_train_date_return_arrival);
    public static final String FIGHT_STATE_RETURN_CANCEL = getResourceString(Constant.getContext(), R.string.duoqu_fight_state_return_cancel);
    public static final String FIGHT_STATE_RETURN_DEPARTURE = getResourceString(Constant.getContext(), R.string.duoqu_fight_state_return_departure);
    public static final float FONT_SIZE_EXTRA_HUGE = 1.30001f;
    public static final float FONT_SIZE_HUGE = 1.3f;
    public static final float FONT_SIZE_LARGE = 1.15f;
    public static final float FONT_SIZE_NORMAL = 1.0f;
    public static final float FONT_SIZE_SMALL = 0.85f;
    public static final int HORIZ_TABLE_TITLE_LINE_SPACING = getDimension(R.dimen.duoqu_title_Line_spacing);
    public static final int HORIZ_TABLE_TITLE_PADDING = getDimension(R.dimen.duoqu_title_padding);
    public static final String IS_SAFE_VERIFY_CODE = "1";
    public static final String LEFTBREAK = getResourceString(Constant.getContext(), R.string.duoqu_left_brackets);
    public static final String NO_DATA = getResourceString(Constant.getContext(), R.string.duoqu_double_line);
    public static final String NO_DATA_ARR_TIME = (getResourceString(Constant.getContext(), R.string.duoqu_double_line_arr_time) + "      ");
    public static final String NO_DATA_DEP_TIME = getResourceString(Constant.getContext(), R.string.duoqu_double_line_dep_time);
    public static final String NO_DATA_EN = getResourceString(Constant.getContext(), R.string.duoqu_double_line_en);
    public static final String RIGHTBREAK = getResourceString(Constant.getContext(), R.string.duoqu_right_brackets);
    public static final String TRAIN_DATE_FORMAT = getResourceString(Constant.getContext(), R.string.duoqu_train_date_format);
    public static final String TRAIN_Query_DATE_ONE = getResourceString(Constant.getContext(), R.string.duoqu_train_date_one);
    public static final String TRAIN_Query_DATE_THREE = getResourceString(Constant.getContext(), R.string.duoqu_train_date_three);
    public static final String TRAIN_Query_DATE_TWO = getResourceString(Constant.getContext(), R.string.duoqu_train_date_two);
    public static final String TRAIN_SELECT_SITES = getResourceString(Constant.getContext(), R.string.duoqu_select_destination);
    public static final String TRAIN_SUPPLEMENT_DATE = getResourceString(Constant.getContext(), R.string.duoqu_train_dupplement_date);
    public static final String TRAIN_SUPPLEMENT_NUMBER = getResourceString(Constant.getContext(), R.string.duoqu_train_time);
    public static final String WATER = getResourceString(Constant.getContext(), R.string.duoqu_water_txt);
    public static final String WEB_TRAIN_STATION_NEW = "WEB_TRAIN_STATION_NEW";
    private static int mBottomButtonTextSize = getDimension(R.dimen.duoqu_bottom_text_size_normal);
    private static int mContentTextColor = getResourceColor(Constant.getContext(), R.color.duoqu_black2);
    private static int mCurrentThemeMode = -1;
    private static int mDateTextSize = getDimension(R.dimen.duoqu_sp_data_text_normal);
    private static float mDisplayFontSize = FONT_SIZE_NORMAL;
    private static int mGeneralPartOneContentTextStyleID = R.style.duoqu_bubble_body_main_text_style_normal;
    private static int mGeneralPartOneTitleTextSize = getDimension(R.dimen.duoqu_bottom_text_size_normal);
    private static int mGeneralPartOneUnitTextStyleID = R.style.duoqu_bubble_body_unit_text_style_normal;
    private static int mHeadFlightStatusTextSize = getDimension(R.dimen.duoqu_title_state_text_size_normal);
    private static int mHeadStationSelectTextSize = getDimension(R.dimen.duoqu_bubble_title_text_size_normal);
    private static int mHeadSubTitleTextSize = getDimension(R.dimen.duoqu_table_title_text_size_normal);
    private static int mHeadTitleTextSize = getDimension(R.dimen.duoqu_bubble_title_text_size_normal);
    private static int mHorizonalTableContentTextSize = getDimension(R.dimen.duoqu_table_content_text_size_normal);
    private static int mHorizonalTableTitleTextSize = getDimension(R.dimen.duoqu_table_title_text_size_normal);
    private static int mLastThemeMode = -1;
    private static int mNoneTimeTextSize = getDimension(R.dimen.duoqu_none_time_normal);
    private static int mTableLineSpacing = getDimension(R.dimen.duoqu_table_line_spacing_nomal);
    private static int mTimeTextSize = getDimension(R.dimen.duoqu_time_normal);
    private static int mVerticalTableContentTextSize = getDimension(R.dimen.duoqu_table_content_text_size_normal);
    private static int mVerticalTableTitleTextSize = getDimension(R.dimen.duoqu_vertical_table_title_text_size_normal);
    public static final String refresh = getResourceString(Constant.getContext(), R.string.duoqu_str_refresh);
    public static final String web_statement = getResourceString(Constant.getContext(), R.string.duoqu_str_web_statement);

    public static void observerFontSize() {
        float currentFontSize = getFontScale();
        if (!isEqualTwoFloats(mDisplayFontSize, currentFontSize)) {
            mDisplayFontSize = currentFontSize;
            updateDimension();
            DuoquBubbleViewCache.clearFormatItemViewCacheMapList();
        }
    }

    public static void observerFontSize(float currentFontSize) {
        if (!isEqualTwoFloats(mDisplayFontSize, currentFontSize)) {
            mDisplayFontSize = currentFontSize;
            updateDimension();
            DuoquBubbleViewCache.clearFormatItemViewCacheMapList();
        }
    }

    public static void observerTheme() {
        if (mCurrentThemeMode != -1 && mCurrentThemeMode != mLastThemeMode) {
            mLastThemeMode = mCurrentThemeMode;
            if (Constant.getContext() != null) {
                mContentTextColor = Constant.getContext().getResources().getColor(R.color.duoqu_black2);
            }
            DuoquBubbleViewCache.clearFormatItemViewCacheMapList();
        }
    }

    public static String getLanguage() {
        if ("zh".equals(Locale.getDefault().getLanguage())) {
            return "zh";
        }
        return "en";
    }

    public static String getBtnName(JSONObject actionMap) {
        String btnName = (String) JsonUtil.getValueFromJsonObject(actionMap, "btn_name");
        if ("zh".equals(getLanguage())) {
            return btnName;
        }
        String egName = (String) JsonUtil.getValueFromJsonObject(actionMap, "egName");
        if (StringUtils.isNull(egName)) {
            return btnName;
        }
        return egName;
    }

    public static void updateDimension() {
        if (isEqualTwoFloats(mDisplayFontSize, FONT_SIZE_SMALL)) {
            mHorizonalTableTitleTextSize = getDimension(R.dimen.duoqu_table_content_text_size_small);
            mHorizonalTableContentTextSize = getDimension(R.dimen.duoqu_table_content_text_size_small);
            mVerticalTableTitleTextSize = getDimension(R.dimen.duoqu_vertical_table_title_text_size_small);
            mVerticalTableContentTextSize = getDimension(R.dimen.duoqu_table_content_text_size_small);
            mBottomButtonTextSize = getDimension(R.dimen.duoqu_bottom_text_size_small);
            mGeneralPartOneTitleTextSize = getDimension(R.dimen.duoqu_bottom_text_size_small);
            mGeneralPartOneContentTextStyleID = R.style.duoqu_bubble_body_main_text_style_small;
            mGeneralPartOneUnitTextStyleID = R.style.duoqu_bubble_body_unit_text_style_small;
            mHeadTitleTextSize = getDimension(R.dimen.duoqu_bubble_title_text_size_small);
            mHeadSubTitleTextSize = getDimension(R.dimen.duoqu_head_subtitle_small);
            mHeadFlightStatusTextSize = getDimension(R.dimen.duoqu_title_state_text_size_small);
            mHeadStationSelectTextSize = getDimension(R.dimen.duoqu_bubble_title_text_size_small);
            mDateTextSize = getDimension(R.dimen.duoqu_sp_data_text_small);
            mTableLineSpacing = getDimension(R.dimen.duoqu_table_line_spacing_small);
        } else if (isEqualTwoFloats(mDisplayFontSize, FONT_SIZE_NORMAL)) {
            mHorizonalTableTitleTextSize = getDimension(R.dimen.duoqu_table_content_text_size_normal);
            mHorizonalTableContentTextSize = getDimension(R.dimen.duoqu_table_content_text_size_normal);
            mVerticalTableTitleTextSize = getDimension(R.dimen.duoqu_vertical_table_title_text_size_normal);
            mVerticalTableContentTextSize = getDimension(R.dimen.duoqu_table_content_text_size_normal);
            mBottomButtonTextSize = getDimension(R.dimen.duoqu_bottom_text_size_normal);
            mGeneralPartOneTitleTextSize = getDimension(R.dimen.duoqu_bottom_text_size_normal);
            mGeneralPartOneContentTextStyleID = R.style.duoqu_bubble_body_main_text_style_normal;
            mGeneralPartOneUnitTextStyleID = R.style.duoqu_bubble_body_unit_text_style_normal;
            mHeadTitleTextSize = getDimension(R.dimen.duoqu_bubble_title_text_size_normal);
            mHeadSubTitleTextSize = getDimension(R.dimen.duoqu_head_subtitle_normal);
            mHeadFlightStatusTextSize = getDimension(R.dimen.duoqu_title_state_text_size_normal);
            mHeadStationSelectTextSize = getDimension(R.dimen.duoqu_bubble_title_text_size_normal);
            mDateTextSize = getDimension(R.dimen.duoqu_sp_data_text_normal);
            mTableLineSpacing = getDimension(R.dimen.duoqu_table_line_spacing_nomal);
        } else {
            mHorizonalTableTitleTextSize = getDimension(R.dimen.duoqu_table_content_text_size_huge);
            mHorizonalTableContentTextSize = getDimension(R.dimen.duoqu_table_content_text_size_huge);
            mVerticalTableTitleTextSize = getDimension(R.dimen.duoqu_vertical_table_title_text_size_huge);
            mVerticalTableContentTextSize = getDimension(R.dimen.duoqu_table_content_text_size_huge);
            mBottomButtonTextSize = getDimension(R.dimen.duoqu_bottom_text_size_huge);
            mGeneralPartOneTitleTextSize = getDimension(R.dimen.duoqu_bottom_text_size_huge);
            mGeneralPartOneContentTextStyleID = R.style.duoqu_bubble_body_main_text_style_huge;
            mGeneralPartOneUnitTextStyleID = R.style.duoqu_bubble_body_unit_text_style_huge;
            mHeadTitleTextSize = getDimension(R.dimen.duoqu_bubble_title_text_size_huge);
            mHeadSubTitleTextSize = getDimension(R.dimen.duoqu_head_subtitle_huge);
            mHeadFlightStatusTextSize = getDimension(R.dimen.duoqu_title_state_text_size_huge);
            mHeadStationSelectTextSize = getDimension(R.dimen.duoqu_bubble_title_text_size_huge);
            mDateTextSize = getDimension(R.dimen.duoqu_sp_data_text_huge);
            mTableLineSpacing = getDimension(R.dimen.duoqu_table_line_spacing_huge);
        }
    }

    public static boolean isEqualTwoFloats(float f1, float f2) {
        return ((double) Math.abs(f1 - f2)) < 1.0E-5d;
    }

    public static int getDimension(int dimenId) {
        try {
            Context context = Constant.getContext();
            if (context == null) {
                return 0;
            }
            return (int) context.getResources().getDimension(dimenId);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("ContentUtil getDimension error: " + e.getMessage(), e);
            return 0;
        }
    }

    public static int getPxDimensionFromString(Context c, String string) {
        if (TextUtils.isEmpty(string) || c == null) {
            return -1;
        }
        string = string.trim().toLowerCase(Locale.ENGLISH);
        int length = string.length();
        if (length < 2) {
            return -1;
        }
        String unit = string.substring(length - 2);
        String value = string.substring(0, length - 2);
        int result = -1;
        if (!TextUtils.isEmpty(value)) {
            if ("px".equals(unit)) {
                try {
                    result = Integer.parseInt(value);
                } catch (Exception e) {
                    result = -1;
                }
            } else if ("dp".equals(unit)) {
                int intValue;
                try {
                    intValue = Integer.parseInt(value);
                } catch (Exception e2) {
                    intValue = -1;
                }
                if (intValue >= 0) {
                    result = (int) TypedValue.applyDimension(1, (float) intValue, c.getResources().getDisplayMetrics());
                }
            }
        }
        return result;
    }

    public static void setTextColor(TextView textView, String textColor) {
        if (textView != null) {
            try {
                if (!StringUtils.isNull(textColor)) {
                    textView.setTextColor(ResourceCacheUtil.parseColor(textColor));
                }
            } catch (Exception e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("ContentUtil setTextColor error: " + e.getMessage(), e);
            }
        }
    }

    public static void setText(TextView textView, String value, String defaultValue) {
        if (textView != null) {
            if (StringUtils.isNull(value)) {
                textView.setText(defaultValue);
            } else {
                textView.setText(value.trim());
            }
        }
    }

    public static void setText(TextView textView, String value, String defaultValue, int valueSizeId, int defaultValueSizeId, int textColor, int defaultTextcolor) {
        if (textView != null) {
            if (StringUtils.isNull(value)) {
                textView.setText(defaultValue);
                textView.setTextSize(0, (float) defaultValueSizeId);
                textView.setTextColor(defaultTextcolor);
                return;
            }
            textView.setText(value.trim());
            textView.setTextSize(0, (float) valueSizeId);
            textView.setTextColor(textColor);
        }
    }

    public static void setViewVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    public static void setVisibilityAndAlpha(View[] views, int visibility, float alpha) {
        if (views != null && views.length != 0) {
            for (View visibilityAndAlpha : views) {
                setVisibilityAndAlpha(visibilityAndAlpha, visibility, alpha);
            }
        }
    }

    public static void setVisibilityAndAlpha(View view, int visibility, float alpha) {
        if (view != null) {
            view.setVisibility(visibility);
            view.setAlpha(alpha);
        }
    }

    public static String getResourceString(Context context, int id) {
        if (context == null) {
            return null;
        }
        try {
            return context.getResources().getString(id);
        } catch (Throwable th) {
            return null;
        }
    }

    public static int getResourceColor(Context context, int id) {
        if (context == null) {
            return Integer.MIN_VALUE;
        }
        try {
            return context.getResources().getColor(id);
        } catch (Throwable th) {
            return Integer.MIN_VALUE;
        }
    }

    public static int getStringLength(String value) {
        int valueLength = 0;
        if (value == null) {
            return 0;
        }
        for (int i = 0; i < value.length(); i++) {
            if (Character.valueOf(value.charAt(i)).toString().matches(CHINESE)) {
                valueLength += 2;
            } else {
                valueLength++;
            }
        }
        return valueLength;
    }

    public static int getBackgroundResId(String popupColor) {
        return R.drawable.duoqu_pop_bg_gray;
    }

    public static boolean isSmallEnabled() {
        return getFontScale() < FONT_SIZE_NORMAL;
    }

    public static boolean isHugeEnabled() {
        return getFontScale() > FONT_SIZE_NORMAL;
    }

    public static float getFontScale() {
        Context ctx = Constant.getContext();
        if (ctx == null) {
            return FONT_SIZE_NORMAL;
        }
        return ctx.getResources().getConfiguration().fontScale;
    }

    public static int getVerticalTableTitleTextSize() {
        return mVerticalTableTitleTextSize;
    }

    public static int getVerticalTableContentTextSize() {
        return mVerticalTableContentTextSize;
    }

    public static int getHorizonalTableTitleTextSize() {
        return mHorizonalTableTitleTextSize;
    }

    public static int getHorizonalTableContentTextSize() {
        return mHorizonalTableContentTextSize;
    }

    public static int getBottomButtonTextSize() {
        return mBottomButtonTextSize;
    }

    public static int getGeneralPartOneTitleTextSize() {
        return mGeneralPartOneTitleTextSize;
    }

    public static int getGeneralPartOneContentTextStyleID() {
        return mGeneralPartOneContentTextStyleID;
    }

    public static int getmGeneralPartOneUnitTextStyleID() {
        return mGeneralPartOneUnitTextStyleID;
    }

    public static int getHeadTitleTextSize() {
        return mHeadTitleTextSize;
    }

    public static int getHeadSubtitleTextSize() {
        return mHeadSubTitleTextSize;
    }

    public static int getFlightStatusTextSize() {
        return mHeadFlightStatusTextSize;
    }

    public static int getHeadStationSelectTextSize() {
        return mHeadStationSelectTextSize;
    }

    public static int getDateTextSize() {
        return mDateTextSize;
    }

    public static int getTimeTextSize() {
        return mTimeTextSize;
    }

    public static int getNoneTimeTextSize() {
        return mNoneTimeTextSize;
    }

    public static int getTableLineSpacing() {
        return mTableLineSpacing;
    }

    public static int getContentTextColor() {
        return mContentTextColor;
    }

    public static void setThemeMode(int themeMode) {
        mCurrentThemeMode = themeMode;
    }

    public static boolean isShowHint(BusinessSmsMessage message) {
        return message != null ? "1".equals((String) message.getValue(DUOQU_IS_SAFE_VERIFY_CODE_KEY)) : false;
    }

    public static Date stringToDate(String dateStr, String formatStr) {
        if (StringUtils.isNull(dateStr)) {
            return null;
        }
        Date date = null;
        try {
            date = new SimpleDateFormat(formatStr).parse(dateStr);
        } catch (Exception e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("ContentUtil stringToDate error: " + e.getMessage(), e);
        }
        return date;
    }

    public static String getFormatDate(Date date, SimpleDateFormat dateFormat) {
        String str = "";
        if (date != null) {
            try {
                str = dateFormat.format(date);
            } catch (Exception e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("ContentUtil getFormatDate error: " + e.getMessage(), e);
            }
        }
        return str;
    }

    public static void showSafetyDialog(Activity activity) {
        new Builder(activity).setTitle(R.string.safety_sms_dialog_title).setMessage(R.string.safety_sms_dialog_content).setPositiveButton(R.string.confirm, new OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                dialog.dismiss();
            }
        }).setCancelable(true).create().show();
    }

    public static boolean isShowSmsContent(BusinessSmsMessage message) {
        return (message == null || message.bubbleJsonObj == null) ? false : message.bubbleJsonObj.has(DUOQU_SMS_CONTENT_KEY);
    }

    public static void saveSelectedIndex(BusinessSmsMessage message, String selecedIndexKey, String selectedIndexValue) {
        if (message != null) {
            try {
                message.bubbleJsonObj.put(selecedIndexKey, selectedIndexValue);
                updateMatchCache(message);
            } catch (Throwable e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("ContentUtil saveSelectedIndex error: " + e.getMessage(), e);
            }
        }
    }

    public static void callBackExecute(SdkCallBack callBack, Object... obj) {
        if (callBack != null) {
            callBack.execute(obj);
        }
    }

    public static void setClickableToFalse(View... views) {
        if (views != null && views.length != 0) {
            for (View view : views) {
                if (view != null) {
                    view.setClickable(false);
                }
            }
        }
    }

    public static void setOnClickListener(View.OnClickListener onClickListener, View... views) {
        if (views != null && views.length != 0) {
            for (View view : views) {
                if (view != null) {
                    view.setOnClickListener(onClickListener);
                }
            }
        }
    }

    public static boolean bubbleDataIsNull(BusinessSmsMessage smsMessage) {
        return smsMessage == null || smsMessage.bubbleJsonObj == null;
    }

    public static String getFightStateColor(String mState) {
        return getFightStateColor(mState, null);
    }

    public static String getFightStateColor(String mState, BusinessSmsMessage smsMessage) {
        if (StringUtils.isNull(mState)) {
            return null;
        }
        String str = null;
        String str2 = null;
        String str3 = null;
        if (smsMessage != null) {
            str = (String) smsMessage.getValue("v_hd_text_2");
            str2 = (String) smsMessage.getValue("v_hd_text_3");
            str3 = (String) smsMessage.getValue("v_hd_text_4");
        }
        if (FIGHT_STATE_PLAN.equals(mState) || FIGHT_STATE_DEPARTURE.equals(mState) || FIGHT_STATE_RETURN.equals(mState) || FIGHT_STATE_RETURN_DEPARTURE.equals(mState) || FIGHT_STATE_ALTERNATE.equals(mState) || FIGHT_STATE_PLAN_ALTERNATE.equals(mState)) {
            if (StringUtils.isNull(str)) {
                return "3010";
            }
            return str;
        } else if (FIGHT_STATE_DELAYS.equals(mState) || FIGHT_STATE_CANCEL.equals(mState) || FIGHT_STATE_ALTERNATE_CANCEL.equals(mState) || FIGHT_STATE_RETURN_CANCEL.equals(mState) || FIGHT_STATE_AHEAD_CANCEL.equals(mState)) {
            if (StringUtils.isNull(str)) {
                return "1010";
            }
            return str2;
        } else if (!FIGHT_STATE_ARRIVAL.equals(mState) && !FIGHT_STATE_RETURN_ARRIVAL.equals(mState) && !FIGHT_STATE_ALTERNATE_ARRIVAL.equals(mState)) {
            return "3010";
        } else {
            if (StringUtils.isNull(str3)) {
                return "5010";
            }
            return str3;
        }
    }

    public static void updateMatchCache(BusinessSmsMessage message) {
        updateMatchCache(message, null);
    }

    public static void updateMatchCache(BusinessSmsMessage message, JSONArray simpleBubbleData) {
        if (message != null) {
            updateMatchCache(String.valueOf(message.getValue("phoneNum")), message.getTitleNo(), String.valueOf(message.getSmsId()), message.bubbleJsonObj, simpleBubbleData, message.getMessageBody());
        }
    }

    public static void updateMatchCache(String phoneNumber, String titleNo, String msgId, JSONObject bubbleResult, JSONArray sessionReuslt, String messageBody) {
        JSONObject jSONObject = null;
        if (bubbleResult != null) {
            JSONObject newBubbleJsonObj;
            try {
                newBubbleJsonObj = new JSONObject(bubbleResult.toString());
            } catch (Throwable th) {
                e = th;
            }
            try {
                newBubbleJsonObj.remove("DISPLAY");
                jSONObject = newBubbleJsonObj;
            } catch (Throwable th2) {
                Throwable e;
                e = th2;
                SmartSmsSdkUtil.smartSdkExceptionLog("ContentUtil updateMatchCache error: " + e.getMessage(), e);
                return;
            }
        }
        ParseManager.updateMatchCacheManager(phoneNumber, titleNo, msgId, jSONObject, sessionReuslt, messageBody);
    }

    public static JSONArray getActionJsonArray(JSONObject itemData) {
        if (itemData == null) {
            return null;
        }
        try {
            Object objAction = itemData.get("ADACTION");
            if (objAction == null) {
                return null;
            }
            if (objAction instanceof JSONArray) {
                return (JSONArray) objAction;
            }
            JSONArray actionArr = JsonUtil.parseStrToJsonArray((String) objAction);
            itemData.put("ADACTION", actionArr);
            return actionArr;
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("ContentUtil getActionJsonArray error:" + e.getMessage(), e);
            return null;
        }
    }
}
