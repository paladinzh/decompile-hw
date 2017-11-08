package cn.com.xy.sms.sdk.ui.popu.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableString;
import android.text.TextUtils.TruncateAt;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.android.mms.R$styleable;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class DuoquHorizItemTable extends RelativeLayout {
    private int mChildId = 0;
    private int mContentSize = 0;
    protected Context mContext;
    private int mLineSpacing = 0;
    private int mMarginBottom = 0;
    private int mMarginLeft = 0;
    private int mMarginRight = 0;
    private int mMarginTop = 0;
    private String mSingleLine = null;
    private int mTextPaddingLeft = 0;
    protected List<ViewHolder> mViewHolderList = null;

    public class ViewHolder {
        public TextView contentView;

        public void setContent(int pos, BusinessSmsMessage message, String dataKey, boolean isReBind) {
            try {
                if (this.contentView != null) {
                    this.contentView.setFocusable(false);
                    JSONObject jsobj = (JSONObject) message.getTableData(pos, dataKey);
                    String leftText = (String) JsonUtil.getValFromJsonObject(jsobj, "t1");
                    String rightText = (String) JsonUtil.getValFromJsonObject(jsobj, "t2");
                    String leftColor = (String) message.getValue("v_by_text_l_1");
                    String rightColor = (String) message.getValue("v_by_text_r_1");
                    String leftTextColor = (String) JsonUtil.getValFromJsonObject(jsobj, "c1");
                    String rightTextColor = (String) JsonUtil.getValFromJsonObject(jsobj, "c2");
                    CharSequence msp = null;
                    if (!(StringUtils.isNull(leftText) || StringUtils.isNull(rightText))) {
                        msp = new SpannableString(leftText + rightText);
                        int lenMiddle = leftText.length();
                        int lenEnd = lenMiddle + rightText.length();
                        if (!StringUtils.isNull(leftTextColor)) {
                            msp.setSpan(new ForegroundColorSpan(ThemeUtil.getColorInteger(DuoquHorizItemTable.this.mContext, leftTextColor)), 0, lenMiddle, 33);
                        } else if (StringUtils.isNull(leftColor)) {
                            msp.setSpan(new ForegroundColorSpan(ThemeUtil.getColorInteger(DuoquHorizItemTable.this.mContext, "1000")), 0, lenMiddle, 33);
                        } else {
                            msp.setSpan(new ForegroundColorSpan(ThemeUtil.getColorInteger(DuoquHorizItemTable.this.mContext, leftColor)), 0, lenMiddle, 33);
                        }
                        if (!StringUtils.isNull(rightTextColor)) {
                            msp.setSpan(new ForegroundColorSpan(ThemeUtil.getColorInteger(DuoquHorizItemTable.this.mContext, rightTextColor)), lenMiddle, lenEnd, 33);
                        } else if (StringUtils.isNull(rightColor)) {
                            msp.setSpan(new ForegroundColorSpan(ThemeUtil.getColorInteger(DuoquHorizItemTable.this.mContext, "1000")), lenMiddle, lenEnd, 33);
                        } else {
                            msp.setSpan(new ForegroundColorSpan(ThemeUtil.getColorInteger(DuoquHorizItemTable.this.mContext, rightColor)), lenMiddle, lenEnd, 33);
                        }
                    }
                    this.contentView.setText(msp);
                    this.contentView.setIncludeFontPadding(false);
                    this.contentView.setLineSpacing(0.0f, 1.1f);
                    this.contentView.setTextSize(0, (float) ContentUtil.getHorizonalTableContentTextSize());
                }
            } catch (RuntimeException e) {
                LogManager.e("XIAOYUAN", "DuoquHorizItemTable setContent RuntimeException:" + e.getMessage(), e);
            } catch (Throwable e2) {
                LogManager.e("XIAOYUAN", "DuoquHorizItemTable setContent error:" + e2.getMessage(), e2);
            }
        }

        public void setVisibility(int visibility) {
            try {
                if (this.contentView != null) {
                    this.contentView.setVisibility(visibility);
                    if (this.contentView.getTag(R.id.tag_parent_layout) != null) {
                        ((RelativeLayout) this.contentView.getTag(R.id.tag_parent_layout)).setVisibility(visibility);
                    }
                }
            } catch (Exception ex) {
                LogManager.e("XIAOYUAN", ex.getMessage(), ex);
            }
        }
    }

    public DuoquHorizItemTable(Context context, AttributeSet attrs) {
        super(context, attrs);
        initParams(context, attrs);
        this.mContext = context;
    }

    protected void initParams(Context context, AttributeSet attrs) {
        TypedArray duoquTbAttr = context.obtainStyledAttributes(attrs, R$styleable.duoqu_table_attr);
        this.mContentSize = Math.round(duoquTbAttr.getDimension(1, 0.0f));
        this.mLineSpacing = Math.round(duoquTbAttr.getDimension(8, 0.0f));
        this.mSingleLine = duoquTbAttr.getString(7);
        this.mMarginTop = Math.round(duoquTbAttr.getDimension(9, 0.0f));
        this.mMarginLeft = Math.round(duoquTbAttr.getDimension(10, 0.0f));
        this.mMarginRight = Math.round(duoquTbAttr.getDimension(11, 0.0f));
        this.mMarginBottom = Math.round(duoquTbAttr.getDimension(12, 0.0f));
        this.mTextPaddingLeft = Math.round(duoquTbAttr.getDimension(14, 0.0f));
        duoquTbAttr.recycle();
        if (this.mMarginTop == 0 && this.mMarginLeft == 0 && this.mMarginBottom == 0) {
            if (this.mMarginRight == 0) {
                return;
            }
        }
        LayoutParams rp = new LayoutParams(-1, -2);
        rp.setMargins(this.mMarginLeft, this.mMarginTop, this.mMarginRight, this.mMarginBottom);
        setLayoutParams(rp);
    }

    protected void getHolder(int pos, BusinessSmsMessage message, int dataSize, String dataKey, boolean isRebind) {
        ViewHolder holder = new ViewHolder();
        holder.contentView = new TextView(getContext());
        TextView textView = holder.contentView;
        int i = this.mChildId + 1;
        this.mChildId = i;
        textView.setId(i);
        addView(holder.contentView, getLayoutParams(this.mChildId, 0, 0));
        holder.contentView.setTextSize(0, (float) this.mContentSize);
        if (this.mTextPaddingLeft > 0) {
            holder.contentView.setPadding(this.mTextPaddingLeft, 0, 0, 0);
        }
        if ("true".equals(this.mSingleLine)) {
            holder.contentView.setMaxLines(8);
            holder.contentView.setEllipsize(TruncateAt.valueOf("END"));
        }
        holder.setContent(pos, message, dataKey, isRebind);
        this.mViewHolderList.add(holder);
    }

    public LayoutParams getLayoutParams(int childId, int customLayoutWidth, int customLayoutHeight) {
        LayoutParams params;
        if (customLayoutWidth != 0 && customLayoutHeight != 0) {
            params = new LayoutParams(customLayoutWidth, customLayoutHeight);
        } else if (customLayoutWidth == 0 && customLayoutHeight != 0) {
            params = new LayoutParams(-2, customLayoutHeight);
        } else if (customLayoutWidth == 0 || customLayoutHeight != 0) {
            params = new LayoutParams(-1, -2);
        } else {
            params = new LayoutParams(customLayoutHeight, -2);
        }
        if (childId == 1) {
            params.addRule(10);
        }
        if (childId - 1 > 0) {
            params.addRule(3, childId - 1);
        }
        if (this.mLineSpacing > 0 && childId > 1) {
            params.setMargins(0, this.mLineSpacing, 0, 0);
        }
        return params;
    }

    protected LayoutParams getLayoutParams(int childId) {
        return null;
    }

    public static boolean belowTitle(String titleText, String contentText) {
        int titleLength = ContentUtil.getStringLength(titleText);
        return ((int) ((14.0d * Math.ceil(((double) titleLength) / 8.0d)) + ((double) titleLength))) > ContentUtil.getStringLength(contentText);
    }

    public void setContentList(BusinessSmsMessage message, int dataSize, String dataKey, boolean isRebind) {
        if (dataSize == 0) {
            try {
                setVisibility(8);
            } catch (Exception ex) {
                LogManager.e("XIAOYUAN", ex.getMessage(), ex);
            }
        } else {
            setVisibility(0);
            List<ViewHolder> holderList = this.mViewHolderList;
            int i;
            if (!isRebind || holderList == null) {
                this.mViewHolderList = new ArrayList();
                for (i = 0; i < dataSize; i++) {
                    getHolder(i, message, dataSize, dataKey, false);
                }
                return;
            }
            int holderSize = holderList.size();
            ViewHolder tempHolder;
            if (holderSize - dataSize > 0) {
                for (i = 0; i < holderSize; i++) {
                    tempHolder = (ViewHolder) holderList.get(i);
                    if (i < dataSize) {
                        tempHolder.setVisibility(0);
                        tempHolder.setContent(i, message, dataKey, isRebind);
                    } else {
                        tempHolder.setVisibility(8);
                    }
                }
            } else {
                for (i = 0; i < dataSize; i++) {
                    if (i < holderSize) {
                        tempHolder = (ViewHolder) holderList.get(i);
                        tempHolder.setVisibility(0);
                        tempHolder.setContent(i, message, dataKey, isRebind);
                    } else {
                        getHolder(i, message, dataSize, dataKey, false);
                    }
                }
            }
        }
    }
}
