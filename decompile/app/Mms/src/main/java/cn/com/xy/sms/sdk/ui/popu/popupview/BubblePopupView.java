package cn.com.xy.sms.sdk.ui.popu.popupview;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.bubbleview.DuoquBubbleViewManager;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.google.android.gms.R;
import java.util.Map;

public class BubblePopupView extends BasePopupView implements IBubbleView {
    private static final int EXPIRE_TYPE = 1;
    private Integer mDuoquBubbleViewWidth = null;
    private ImageView mOverMark = null;

    public BubblePopupView(Context context) {
        super(context);
    }

    public BubblePopupView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initExtendParamData(boolean isRebind) {
        this.mDuoquBubbleViewWidth = (Integer) this.mBusinessSmsMessage.getExtendParamValue("duoqu_bubble_view_width");
        if (!isRebind) {
            Integer bgResId = (Integer) this.mBusinessSmsMessage.getExtendParamValue("duoqu_bg_resid");
            if (bgResId != null) {
                this.mView.setBackgroundResource(bgResId.intValue());
                return;
            }
            Drawable bg = (Drawable) this.mBusinessSmsMessage.getExtendParamValue("duoqu_bg_drawable");
            if (bg != null) {
                ViewUtil.setBackground(this.mView, bg);
            }
        }
    }

    public void initUIAfter() {
        initExtendParamData(false);
        Integer leftPadding = (Integer) this.mBusinessSmsMessage.getExtendParamValue("duoqu_leftPadding");
        Integer topPadding = (Integer) this.mBusinessSmsMessage.getExtendParamValue("duoqu_topPadding");
        setPadding(leftPadding == null ? getPaddingStart() : leftPadding.intValue(), topPadding == null ? getPaddingTop() : topPadding.intValue(), getPaddingEnd(), getPaddingBottom());
        setLayoutParam();
        updateMarkView();
        setBackground();
    }

    private void setBackground() {
        try {
            int resID;
            boolean hasButtomData = this.mBusinessSmsMessage.bubbleJsonObj.optBoolean("hasButtonData", false);
            String backgroud = "";
            if (this.mBusinessSmsMessage.getValue("m_special_layout") != null) {
                if (hasButtomData) {
                    resID = R.drawable.v_bg_2;
                } else {
                    resID = R.drawable.v_bg_4;
                }
            } else if (hasButtomData) {
                resID = R.drawable.v_bg_1;
            } else {
                resID = R.drawable.v_bg_3;
            }
            ThemeUtil.setViewBg(getContext(), this.mView, backgroud, resID);
        } catch (Exception e) {
            LogManager.e("XIAOYUAN", "BubblePopupView: setBackground : " + e.getMessage(), e);
        }
    }

    private void updateMarkView() {
        showExpireMarkView();
    }

    private void showExpireMarkView() {
        showExpireMarkView(0);
    }

    private void showExpireMarkView(long deadLine) {
        if (this.mBusinessSmsMessage == null || !isExpire(deadLine)) {
            setMarkViewVisible(false, 1);
            return;
        }
        if (this.mOverMark == null) {
            addMarkView();
        }
        setMarkViewVisible(true, 1);
    }

    private boolean isExpire(long deadLine) {
        long deadLineF;
        boolean z = false;
        if (deadLine > 0) {
            deadLineF = deadLine;
        } else {
            try {
                deadLineF = this.mBusinessSmsMessage.bubbleJsonObj.optLong("deadline");
            } catch (Exception e) {
                deadLineF = -1;
            }
        }
        if (deadLineF <= 0) {
            return false;
        }
        if (System.currentTimeMillis() > deadLineF) {
            z = true;
        }
        return z;
    }

    private void setMarkViewVisible(boolean visible, int type) {
        int visibleState = 8;
        if (visible) {
            visibleState = 0;
        }
        switch (type) {
            case 1:
                if (this.mOverMark != null && this.mOverMark.getVisibility() != visibleState) {
                    this.mOverMark.setVisibility(visibleState);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void addMarkView() {
        if (this.mBusinessSmsMessage != null) {
            Drawable dr = ViewUtil.getDrawable(getContext(), (String) this.mBusinessSmsMessage.getValue("v_by_mark_1"), false, false);
            if (dr != null) {
                ImageView mark = new ImageView(getContext());
                mark.setImageDrawable(dr);
                LayoutParams param = new LayoutParams(-2, -2);
                param.addRule(11);
                int paddingTpx = -1;
                int paddingRpx = -1;
                try {
                    String paddingTString = (String) this.mBusinessSmsMessage.getValue("v_by_mark_1_padding_u");
                    String paddingRString = (String) this.mBusinessSmsMessage.getValue("v_by_mark_1_padding_r");
                    if (!StringUtils.isNull(paddingTString)) {
                        paddingTpx = ContentUtil.getPxDimensionFromString(getContext(), paddingTString);
                    }
                    if (!StringUtils.isNull(paddingRString)) {
                        paddingRpx = ContentUtil.getPxDimensionFromString(getContext(), paddingRString);
                    }
                    if (paddingTpx < 0) {
                        paddingTpx = getResources().getDimensionPixelOffset(R.dimen.duoqu_part_overmark_padding_top);
                    }
                    if (paddingRpx < 0) {
                        paddingRpx = getResources().getDimensionPixelOffset(R.dimen.duoqu_part_overmark_padding_right);
                    }
                    mark.setPadding(0, paddingTpx, paddingRpx, 0);
                } catch (Exception e) {
                    LogManager.e("XIAOYUAN", "BubblePopupView addMarkView : " + e.getMessage(), e);
                }
                if (this.mView != null) {
                    this.mView.addView(mark, param);
                    this.mOverMark = mark;
                }
            }
        }
    }

    public void initUIPartBefore(Activity mContext, BusinessSmsMessage businessSmsMessage) {
        this.mView = this;
    }

    void setLayoutParam() {
        int width = -2;
        if (this.mDuoquBubbleViewWidth != null) {
            width = this.mDuoquBubbleViewWidth.intValue();
        }
        ViewGroup.LayoutParams lp = this.mView.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(width, -2);
        } else {
            lp.width = width;
        }
        this.mView.setId(DuoquBubbleViewManager.DUOQU_BUBBLE_VIEW_ID);
        this.mView.setLayoutParams(lp);
    }

    public void reBindData(Activity context, BusinessSmsMessage businessSmsMessage) throws Exception {
        if (this.mBusinessSmsMessage.messageBody == null) {
            Log.w("duoqu_xiaoyuan", "mBusinessSmsMessage.messageBody is null reBindData false.");
            initData(businessSmsMessage);
            bindData(context, false);
            updateMarkView();
            setBackground();
            return;
        }
        super.reBindData(context, businessSmsMessage);
        initExtendParamData(true);
        if (!(this.mDuoquBubbleViewWidth == null || this.mView.getLayoutParams().width == this.mDuoquBubbleViewWidth.intValue())) {
            ViewGroup.LayoutParams lp = this.mView.getLayoutParams();
            lp.width = this.mDuoquBubbleViewWidth.intValue();
            this.mView.setLayoutParams(lp);
        }
        updateMarkView();
        setBackground();
    }

    public void changeData(Map<String, Object> param) {
        super.changeData(param);
        if (param != null && param.containsKey("deadline")) {
            long dl;
            try {
                dl = ((Long) param.get("deadline")).longValue();
            } catch (Exception e) {
                dl = 0;
            }
            if (dl > 0) {
                showExpireMarkView(dl);
            }
            setBackground();
        }
    }

    public void addExtendView(View view, int place) throws Exception {
    }

    public void removeAllExtendView() throws Exception {
    }

    public void destroy() {
        super.destroy();
        ViewUtil.recycleImageView(this.mOverMark);
        this.mOverMark = null;
    }
}
