package cn.com.xy.sms.sdk.ui.popu.part;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.google.android.gms.R;
import java.lang.reflect.Array;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;

@SuppressLint({"ResourceAsColor"})
public class BubbleGeneralOneBody extends UIPart {
    private static final int SPAN_MAX = 5;
    private static final int SPAN_TYPE_MAIN = 1;
    private static final int SPAN_TYPE_SPLIT = 2;
    private static final int SPAN_TYPE_SUB = 0;
    private static String mReNumber = "";
    private static HashMap<String, ViewHolder> sViewHolderCach = new HashMap(5);
    private TextView mContentTextView = null;
    private View mContentView = null;
    private String mMsgKey = null;
    private TextView mTitleTextView = null;
    private ViewHolder mVh = null;

    private static class ViewHolder {
        private String contentColor;
        private String msgKey;
        private String normalContent;
        private String normalTitle;
        private SpannableString spContent;
        private String titleColor;

        private ViewHolder() {
            this.normalTitle = "";
            this.normalContent = null;
            this.spContent = null;
            this.msgKey = "";
            this.titleColor = "";
            this.contentColor = "";
        }
    }

    public BubbleGeneralOneBody(Activity context, BusinessSmsMessage message, XyCallBack callback, int layoutId, ViewGroup root, int partId) {
        super(context, message, callback, layoutId, root, partId);
    }

    public void initUi() {
        this.mTitleTextView = (TextView) this.mView.findViewById(R.id.duoqu_bubble_generalone_title);
        this.mContentTextView = (TextView) this.mView.findViewById(R.id.duoqu_bubble_generalone_content);
        this.mContentView = this.mView.findViewById(R.id.duoqu_bubble_generalone_layout);
        this.mTitleTextView.setTextSize(0, (float) ContentUtil.getGeneralPartOneTitleTextSize());
    }

    public void setContent(BusinessSmsMessage message, boolean isRebind) throws Exception {
        this.mMessage = message;
        if (message == null) {
            this.mMsgKey = null;
            return;
        }
        String reNumber = (String) message.getValue("phoneNum");
        if (reNumber == null || !reNumber.equals(mReNumber)) {
            if (sViewHolderCach != null) {
                sViewHolderCach.clear();
            }
            this.mVh = null;
        }
        setReNumber(reNumber);
        this.mMsgKey = String.valueOf(message.smsId) + String.valueOf(message.msgTime);
        ViewHolder viewHolder = null;
        if (this.mVh == null || TextUtils.isEmpty(this.mVh.msgKey) || !this.mVh.msgKey.equals(this.mMsgKey) || !isRebind) {
            if (this.mMsgKey != null && sViewHolderCach != null && sViewHolderCach.containsKey(this.mMsgKey) && isRebind) {
                viewHolder = (ViewHolder) sViewHolderCach.get(this.mMsgKey);
            }
            if (viewHolder == null) {
                viewHolder = buildViewHold(message);
                if (!(viewHolder == null || sViewHolderCach == null)) {
                    sViewHolderCach.put(this.mMsgKey, viewHolder);
                }
            }
            this.mVh = viewHolder;
        } else {
            viewHolder = this.mVh;
        }
        buildTitleText();
        buildContentText();
        if (!isRebind) {
            setTextStyle();
        }
        updateNormalView(message, isRebind);
    }

    private ViewHolder buildViewHold(BusinessSmsMessage message) {
        ViewHolder vh = new ViewHolder();
        if (message == null) {
            return null;
        }
        JSONArray jSONArray;
        vh.normalTitle = (String) message.getValue("m_by_text_1");
        vh.normalContent = (String) message.getValue("m_by_text_2");
        vh.titleColor = (String) message.getValue("v_by_text_1");
        vh.contentColor = (String) message.getValue("v_by_text_2");
        try {
            jSONArray = (JSONArray) message.getValue("moneyList");
        } catch (Exception e) {
            jSONArray = null;
        }
        vh.spContent = constructContent(jSONArray, vh.titleColor, vh.contentColor);
        vh.msgKey = this.mMsgKey;
        return vh;
    }

    private void updateNormalView(BusinessSmsMessage message, boolean isRebind) {
        if (this.mContentTextView == null || this.mContentTextView.getText() == null || this.mContentTextView.getText().length() <= 0 || this.mTitleTextView == null || this.mTitleTextView.getText() == null || this.mTitleTextView.getText().length() <= 0) {
            if (this.mContentTextView != null) {
                this.mContentTextView.setVisibility(8);
            }
            if (this.mTitleTextView != null) {
                this.mTitleTextView.setVisibility(8);
            }
            if (this.mContentView != null) {
                this.mContentView.setVisibility(8);
                return;
            }
            return;
        }
        this.mContentTextView.setVisibility(0);
        this.mTitleTextView.setVisibility(0);
        if (this.mContentView != null) {
            this.mContentView.setVisibility(0);
        }
    }

    private void buildTitleText() {
        if (this.mVh != null && this.mTitleTextView != null) {
            if (StringUtils.isNull(this.mVh.normalTitle)) {
                this.mTitleTextView.setText("");
            } else {
                this.mTitleTextView.setText(this.mVh.normalTitle);
            }
        }
    }

    private void buildContentText() {
        if (this.mVh != null && this.mContentTextView != null) {
            if (this.mVh.spContent != null && this.mVh.spContent.length() > 0) {
                this.mContentTextView.setText(this.mVh.spContent);
            } else if (!TextUtils.isEmpty(this.mVh.normalContent)) {
                this.mContentTextView.setText(this.mVh.normalContent);
            }
        }
    }

    private void setTextStyle() {
        if (this.mVh != null) {
            if (!StringUtils.isNull(this.mVh.titleColor)) {
                ThemeUtil.setTextColor(this.mContext, this.mTitleTextView, this.mVh.titleColor, R.color.duoqu_theme_color_5010);
            }
            if (!StringUtils.isNull(this.mVh.contentColor) && (this.mVh.spContent == null || this.mVh.spContent.length() == 0)) {
                ThemeUtil.setTextColor(this.mContext, this.mContentTextView, this.mVh.contentColor, R.color.duoqu_theme_color_3010);
            }
        }
    }

    private SpannableString constructContent(JSONArray money, String titleColor, String contentColor) {
        if (money == null || money.length() == 0) {
            return null;
        }
        int i;
        StringBuilder sb = new StringBuilder();
        int[][] spanIndex = (int[][]) Array.newInstance(Integer.TYPE, new int[]{5, 3});
        int index = 0;
        int size = money.length();
        for (i = 0; i < size; i++) {
            String unit = "";
            String moneyType = "";
            String moneyCost = "";
            JSONObject jo = money.optJSONObject(i);
            if (jo != null) {
                int star;
                int end;
                moneyType = jo.optString("money_type");
                moneyCost = jo.optString("money");
                unit = jo.optString("unit");
                if (!StringUtils.isNull(moneyType)) {
                    star = sb.length();
                    sb.append(moneyType);
                    end = sb.length();
                    if (5 > index) {
                        spanIndex[index][0] = star;
                        spanIndex[index][1] = end;
                        spanIndex[index][2] = 1;
                        index++;
                    }
                }
                if (!StringUtils.isNull(moneyCost)) {
                    star = sb.length();
                    sb.append(moneyCost);
                    end = sb.length();
                    if (5 > index) {
                        spanIndex[index][0] = star;
                        spanIndex[index][1] = end;
                        spanIndex[index][2] = 1;
                        index++;
                    }
                }
                if (!StringUtils.isNull(unit)) {
                    star = sb.length();
                    sb.append(unit);
                    end = sb.length();
                    if (5 > index) {
                        spanIndex[index][0] = star;
                        spanIndex[index][1] = end;
                        spanIndex[index][2] = 0;
                        index++;
                    }
                }
                if (i + 1 < size) {
                    star = sb.length();
                    sb.append(this.mContext.getString(R.string.duoqu_bubble_money_split));
                    end = sb.length();
                    if (5 > index) {
                        spanIndex[index][0] = star;
                        spanIndex[index][1] = end;
                        spanIndex[index][2] = 2;
                        index++;
                    }
                }
            }
        }
        SpannableString sp = new SpannableString(sb);
        int mainTextColor = ThemeUtil.getColorId(ThemeUtil.getResIndex(contentColor));
        int titleTextColor = ThemeUtil.getColorId(ThemeUtil.getResIndex(titleColor));
        boolean hasMainColor = mainTextColor != -9999;
        boolean hasTitelColor = titleTextColor != -9999;
        i = 0;
        while (i < index && i < 5) {
            int styeId = 0;
            int textColor = -1;
            if (spanIndex[i][2] == 1) {
                if (hasMainColor) {
                    textColor = mainTextColor;
                } else {
                    textColor = R.color.duoqu_theme_color_3010;
                }
                styeId = ContentUtil.getGeneralPartOneContentTextStyleID();
            } else if (spanIndex[i][2] == 0) {
                if (hasTitelColor) {
                    textColor = titleTextColor;
                } else {
                    textColor = R.color.duoqu_theme_color_5010;
                }
                styeId = ContentUtil.getmGeneralPartOneUnitTextStyleID();
            } else if (spanIndex[i][2] == 2) {
                if (hasMainColor) {
                    textColor = mainTextColor;
                } else {
                    textColor = R.color.duoqu_theme_color_3010;
                }
                styeId = ContentUtil.getmGeneralPartOneUnitTextStyleID();
            }
            sp.setSpan(new TextAppearanceSpan(this.mContext, styeId), spanIndex[i][0], spanIndex[i][1], 33);
            if (textColor != -1) {
                sp.setSpan(new ForegroundColorSpan(this.mContext.getResources().getColor(textColor)), spanIndex[i][0], spanIndex[i][1], 33);
            }
            i++;
        }
        if (sp.length() > 0) {
            return sp;
        }
        return null;
    }

    public static void setReNumber(String reNumber) {
        mReNumber = reNumber;
    }

    public void destroy() {
        super.destroy();
        if (sViewHolderCach != null) {
            sViewHolderCach.clear();
        }
    }
}
