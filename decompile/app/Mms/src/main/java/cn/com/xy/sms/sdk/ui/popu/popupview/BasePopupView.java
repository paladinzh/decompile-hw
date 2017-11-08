package cn.com.xy.sms.sdk.ui.popu.popupview;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import cn.com.xy.sms.sdk.ISmartSmsEvent;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleAirBody;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleTrainBody;
import cn.com.xy.sms.sdk.ui.popu.part.UIPart;
import cn.com.xy.sms.sdk.ui.popu.util.ViewManger;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import com.google.android.gms.R;
import java.util.Map;

public class BasePopupView extends RelativeLayout {
    public String groupValue;
    private BaseCompriseBubbleView mBaseCompriseView = null;
    public BusinessSmsMessage mBusinessSmsMessage;
    private ISmartSmsEvent mSmartSmsEvent = null;
    public ViewGroup mView = null;

    public BasePopupView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BasePopupView(Context context) {
        super(context);
    }

    public void init(Activity context, BusinessSmsMessage message, XyCallBack callback) throws Exception {
        this.mBusinessSmsMessage = message;
        initUIPartBefore(context, message);
        this.mBaseCompriseView = new BaseCompriseBubbleView(context, callback, message, this.mView);
        initView();
        initData(message);
    }

    public void initData(BusinessSmsMessage message) {
        this.mBusinessSmsMessage = message;
        initUIAfter();
    }

    public void initUIAfter() {
    }

    public void initView() throws Exception {
        this.mBaseCompriseView.addViews(this.mView, this);
    }

    public void initUIPartBefore(Activity mContext, BusinessSmsMessage businessSmsMessage) {
        this.mView = this;
        this.mView.setPaddingRelative(this.mView.getPaddingStart(), ViewManger.getIntDimen(mContext, R.dimen.base_margin_top), this.mView.getPaddingEnd(), this.mView.getPaddingBottom());
    }

    public void destroy() {
        ViewUtil.recycleViewBg(this.mView);
        this.mView = null;
        if (this.mBaseCompriseView != null) {
            this.mBaseCompriseView.destory();
        }
        this.mBaseCompriseView = null;
    }

    public void reBindData(Activity context, BusinessSmsMessage businessSmsMessage) throws Exception {
        this.mBusinessSmsMessage = businessSmsMessage;
        bindData(context, true);
    }

    public void bindData(Activity context, boolean isRebind) throws Exception {
        this.mBaseCompriseView.reBindData(context, this.mBusinessSmsMessage, isRebind);
    }

    public void changeData(Map<String, Object> param) {
        if (param != null) {
            Integer type = (Integer) param.get(NumberInfo.TYPE_KEY);
            if (type != null) {
                if (type.intValue() == 0) {
                    if (this.mBaseCompriseView.mBodyUIPartList != null) {
                        for (UIPart part : this.mBaseCompriseView.mBodyUIPartList) {
                            part.changeData(param);
                        }
                    }
                    return;
                }
                if (type.intValue() == 1) {
                    if (this.mBaseCompriseView.mHeadUIPartList != null) {
                        for (UIPart part2 : this.mBaseCompriseView.mHeadUIPartList) {
                            part2.changeData(param);
                        }
                    }
                    if (this.mBaseCompriseView.mBodyUIPartList != null) {
                        for (UIPart part22 : this.mBaseCompriseView.mBodyUIPartList) {
                            part22.changeData(param);
                        }
                    }
                    if (this.mBaseCompriseView.mFootUIPartList != null) {
                        for (UIPart part222 : this.mBaseCompriseView.mFootUIPartList) {
                            part222.changeData(param);
                        }
                    }
                }
                if (type.intValue() == 2 && this.mBaseCompriseView.mFootUIPartList != null) {
                    for (UIPart part2222 : this.mBaseCompriseView.mFootUIPartList) {
                        part2222.changeData(param);
                    }
                }
                if (type.intValue() == 3 && this.mBaseCompriseView.mHeadUIPartList != null) {
                    for (UIPart part22222 : this.mBaseCompriseView.mHeadUIPartList) {
                        part22222.changeData(param);
                    }
                }
                if (type.intValue() == 4) {
                    if (this.mBaseCompriseView.mHeadUIPartList != null) {
                        for (UIPart part222222 : this.mBaseCompriseView.mHeadUIPartList) {
                            part222222.changeData(param);
                        }
                    }
                    if (this.mBaseCompriseView.mBodyUIPartList != null) {
                        for (UIPart part2222222 : this.mBaseCompriseView.mBodyUIPartList) {
                            part2222222.changeData(param);
                        }
                    }
                }
            }
        }
    }

    public void runAnimation() {
        if (this.mBaseCompriseView.mBodyUIPartList != null) {
            for (UIPart part : this.mBaseCompriseView.mBodyUIPartList) {
                if ((part instanceof BubbleAirBody) || (part instanceof BubbleTrainBody)) {
                    part.runAnimation();
                }
            }
        }
    }

    public void setSmartSmsEvent(ISmartSmsEvent smartSmsEvent) {
        this.mSmartSmsEvent = smartSmsEvent;
    }

    public int callSmartSmsEvent(int eventType, Map extend) {
        if (this.mSmartSmsEvent != null) {
            return this.mSmartSmsEvent.onSmartSmsEvent(eventType, extend);
        }
        return 0;
    }
}
