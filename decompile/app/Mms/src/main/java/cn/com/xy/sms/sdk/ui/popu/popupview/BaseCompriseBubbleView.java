package cn.com.xy.sms.sdk.ui.popu.popupview;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.RelativeLayout.LayoutParams;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleBottomTwo;
import cn.com.xy.sms.sdk.ui.popu.part.UIPart;
import cn.com.xy.sms.sdk.ui.popu.util.ViewManger;
import cn.com.xy.sms.sdk.ui.popu.widget.IViewAttr;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaseCompriseBubbleView {
    private static final int FIRST_PREV_ID = 100000;
    public List<UIPart> mBodyUIPartList = null;
    public BusinessSmsMessage mBusinessSmsMessage;
    public XyCallBack mCallback = null;
    public Activity mContext;
    public List<UIPart> mFootUIPartList = null;
    public List<UIPart> mHeadUIPartList = null;
    private Map<String, PartViewParam> mPartViewMap = null;
    public int mPopupContentPadding = 0;
    int mPrevId = FIRST_PREV_ID;
    public ViewGroup mRoot = null;

    public BaseCompriseBubbleView(Activity context, XyCallBack callback, BusinessSmsMessage message, ViewGroup root) {
        initData(context, callback, message, root);
        this.mPopupContentPadding = Math.round(Constant.getContext().getResources().getDimension(R.dimen.popup_content_padding));
    }

    public void addViews(ViewGroup root, BasePopupView bubbleView) throws Exception {
        if (root != null) {
            this.mRoot = root;
        }
        addUIPartToRoot(PartViewParam.HEAD, this.mHeadUIPartList, bubbleView);
        addUIPartToRoot(PartViewParam.BODY, this.mBodyUIPartList, bubbleView);
        addUIPartToRoot(PartViewParam.FOOT, this.mFootUIPartList, bubbleView);
    }

    public void initData(Activity context, XyCallBack callback, BusinessSmsMessage message, ViewGroup root) {
        this.mContext = context;
        this.mCallback = callback;
        this.mBusinessSmsMessage = message;
        this.mRoot = root;
        try {
            this.mPartViewMap = (Map) message.getValue("viewPartParam");
            if (this.mPartViewMap == null) {
                Log.e("duoqu_xiaoyuan", "BaseCompriseBubbleView.initData mPartViewMap is null.");
                return;
            }
            this.mHeadUIPartList = initUIPart(PartViewParam.HEAD, this.mPartViewMap);
            this.mBodyUIPartList = initUIPart(PartViewParam.BODY, this.mPartViewMap);
            this.mFootUIPartList = initUIPart(PartViewParam.FOOT, this.mPartViewMap);
        } catch (Exception e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("BaseCompriseBubbleView initData error:" + e.getMessage(), e);
        }
    }

    public List<UIPart> initUIPart(String type, Map<String, PartViewParam> partViewMap) throws Exception {
        PartViewParam param = (PartViewParam) partViewMap.get(type);
        if (param == null || param.mLayOutList == null) {
            return null;
        }
        int size = param.mLayOutList.size();
        List<UIPart> partList = new ArrayList();
        for (int i = 0; i < size; i++) {
            UIPart tempPart = ViewManger.getUIPartByPartId(this.mContext, this.mBusinessSmsMessage, this.mCallback, this.mRoot, ((Integer) param.mLayOutList.get(i)).intValue());
            if (tempPart != null) {
                partList.add(tempPart);
            }
        }
        return partList;
    }

    public void setViewLayoutParam(View view, PartViewParam viewParamRule, UIPart part) throws Exception {
        if (view != null) {
            int margin = 0;
            if (part != null) {
                Integer mgType = (Integer) part.getParam("MLR");
                if (mgType != null) {
                    margin = ViewManger.getInnerLayoutMargin(Constant.getContext(), mgType.intValue());
                }
                Integer h = (Integer) part.getParam(PartViewParam.HEAD);
                if (h != null) {
                    view.getLayoutParams().height = h.intValue();
                }
                Integer mtpo = (Integer) part.getParam("MTPO");
                if (mtpo != null && (view.getLayoutParams() instanceof MarginLayoutParams)) {
                    ((MarginLayoutParams) view.getLayoutParams()).topMargin = mtpo.intValue();
                }
                if (margin > 0 && (view.getLayoutParams() instanceof MarginLayoutParams)) {
                    MarginLayoutParams vMp = (MarginLayoutParams) view.getLayoutParams();
                    vMp.setMarginStart(margin);
                    vMp.setMarginEnd(margin);
                }
                if (part instanceof BubbleBottomTwo) {
                    ((BubbleBottomTwo) part).setLayoutParams();
                }
                if (view instanceof IViewAttr) {
                    int height = ViewManger.getDouquAttrDimen((IViewAttr) view, 0);
                    if (height > 0) {
                        view.getLayoutParams().height = height;
                    }
                }
            }
        }
    }

    public static LayoutParams getRelativeLayoutParam(LayoutParams lp, int verb, int viewId, int... verbs) {
        if (lp == null) {
            lp = new LayoutParams(-1, -2);
        }
        if (viewId > 0) {
            lp.addRule(verb, viewId);
        }
        if (verbs != null) {
            for (int v : verbs) {
                lp.addRule(v);
            }
        }
        return lp;
    }

    public static LayoutParams getRelativeLayoutParam2(LayoutParams lp, int viewId, int... verbs) {
        if (lp == null) {
            lp = new LayoutParams(-2, -2);
        }
        if (viewId > 0 && verbs != null) {
            for (int v : verbs) {
                lp.addRule(v, viewId);
            }
        }
        return lp;
    }

    public LayoutParams getGloabRelativeLayoutParams(View view, int prevId) {
        LayoutParams lp = null;
        ViewGroup.LayoutParams vlp = view.getLayoutParams();
        if (vlp != null && (vlp instanceof LayoutParams)) {
            lp = (LayoutParams) vlp;
        }
        if (prevId == FIRST_PREV_ID) {
            lp = getRelativeLayoutParam(lp, -1, -1, 10);
        } else {
            lp = getRelativeLayoutParam(lp, 3, prevId, new int[0]);
        }
        if (this.mBusinessSmsMessage.viewType == (byte) 0) {
            lp.addRule(14);
        } else {
            lp.addRule(18);
        }
        return lp;
    }

    private void addUIPartToRoot(String uiType, List<UIPart> uiPartList, BasePopupView bubbleView) throws Exception {
        if (uiPartList != null) {
            int size = uiPartList.size();
            if (size > 0) {
                PartViewParam viewParamRule = (PartViewParam) this.mPartViewMap.get(uiType);
                boolean isBodyUi = PartViewParam.BODY.equals(uiType);
                int newViewId = this.mPrevId + 1;
                int i = 0;
                while (i < size) {
                    UIPart uiPart = (UIPart) uiPartList.get(i);
                    uiPart.mBasePopupView = bubbleView;
                    if (uiPart.mView != null) {
                        uiPart.build();
                        LayoutParams lp = getGloabRelativeLayoutParams(uiPart.mView, this.mPrevId);
                        uiPart.mView.setId(newViewId);
                        this.mRoot.addView(uiPart.mView, lp);
                        setViewLayoutParam(uiPart.mView, viewParamRule, uiPart);
                        if (viewParamRule != null && i > 0) {
                            ViewManger.setLayoutMarginTop(Constant.getContext(), uiPart.mView.getLayoutParams(), viewParamRule.mUiPartMarginTopType);
                        }
                        if (isBodyUi) {
                            ViewManger.setBodyViewPadding(Constant.getContext(), uiPart.mView, uiPart.mView, viewParamRule, this.mPopupContentPadding);
                        }
                        this.mPrevId = newViewId;
                        newViewId++;
                    }
                    i++;
                }
            }
        }
    }

    private void destory(List<UIPart> UIPartList) {
        if (UIPartList != null) {
            for (UIPart part : UIPartList) {
                part.destroy();
            }
            UIPartList.clear();
        }
    }

    public void destory() {
        destory(this.mHeadUIPartList);
        destory(this.mBodyUIPartList);
        destory(this.mFootUIPartList);
        this.mPartViewMap = null;
        this.mBusinessSmsMessage = null;
        this.mContext = null;
        this.mCallback = null;
        this.mRoot = null;
        this.mHeadUIPartList = null;
        this.mBodyUIPartList = null;
        this.mFootUIPartList = null;
    }

    public void reBindData(Activity context, BusinessSmsMessage businessSmsMessage, boolean reBindData) throws Exception {
        this.mBusinessSmsMessage = businessSmsMessage;
        if (this.mHeadUIPartList != null) {
            for (UIPart part : this.mHeadUIPartList) {
                part.mContext = context;
                part.setContent(businessSmsMessage, reBindData);
            }
        }
        if (this.mBodyUIPartList != null) {
            for (UIPart part2 : this.mBodyUIPartList) {
                part2.mContext = context;
                part2.setContent(businessSmsMessage, reBindData);
            }
        }
        if (this.mFootUIPartList != null) {
            for (UIPart part22 : this.mFootUIPartList) {
                part22.mContext = context;
                part22.setContent(businessSmsMessage, reBindData);
            }
        }
    }
}
