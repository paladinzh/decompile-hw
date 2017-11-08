package cn.com.xy.sms.sdk.ui.popu.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.bubbleview.DuoquBubbleViewManager;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleAirBody;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleBodyBottom;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleBodyCallsMessage;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleBodyExpressStatus;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleBodyFeature;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleBottomTwo;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleGeneralOneBody;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleHorizTable;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleOriginSmsText;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleSimpleCallNumberBody;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleTitleHead;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleTrainBody;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleTwoItemsVertTable;
import cn.com.xy.sms.sdk.ui.popu.part.UIPart;
import cn.com.xy.sms.sdk.ui.popu.popupview.PartViewParam;
import cn.com.xy.sms.sdk.ui.popu.widget.IViewAttr;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewManger {
    public static final int ONE_SIDE_POPUPVIEW = 1;
    private static final int TYPE_MARGIN_11 = getIntDimen(Constant.getContext(), R.dimen.duoqu_type_margin_11);
    private static final int TYPE_PADDING_11 = getIntDimen(Constant.getContext(), R.dimen.duoqu_type_padding_11);
    private static final int TYPE_SPLIT_LR_MARGIN_111 = getIntDimen(Constant.getContext(), R.dimen.duoqu_type_split_lr_margin_111);
    private static final int TYPE_SPLIT_LR_MARGIN_112 = getIntDimen(Constant.getContext(), R.dimen.duoqu_type_split_lr_margin_112);
    private static final int TYPE_VIEW_HEIGHT_11 = getIntDimen(Constant.getContext(), R.dimen.duoqu_type_view_height_11);
    private static final Integer[] VIEW_PART_ID = new Integer[]{Integer.valueOf(101), Integer.valueOf(ViewPartId.PART_BODY_HORIZ_TABLE), Integer.valueOf(ViewPartId.PART_BODY_VERT_TABLE), Integer.valueOf(ViewPartId.PART_BODY_BOTTOM_PARTID), Integer.valueOf(ViewPartId.PART_BODY_CALLS_MESSAGE), Integer.valueOf(ViewPartId.PART_BODY_TRAIN_TICKET), Integer.valueOf(ViewPartId.PART_BODY_FEATURE), Integer.valueOf(ViewPartId.PART_BODY_AIR_TICKET), Integer.valueOf(ViewPartId.PART_BODY_TWOITEMVERT_TABLE), Integer.valueOf(ViewPartId.PART_BOTTOM_TWO_BUTTON), Integer.valueOf(ViewPartId.PART_BODY_HORIZ_LF_TABLE), Integer.valueOf(ViewPartId.PART_BODY_EXPRESS_STATUS), Integer.valueOf(ViewPartId.PART_BODY_GENENARALONE), Integer.valueOf(ViewPartId.PART_BODY_SIMPLE_CALL_NUMBER), Integer.valueOf(ViewPartId.PART_BODY_ORGSMSTEXT)};

    private static UIPart getHeadUIPartByPartId(Activity context, BusinessSmsMessage message, XyCallBack xyCallBack, ViewGroup root, int partId) throws Exception {
        switch (partId) {
            case 101:
                return new BubbleTitleHead(context, message, xyCallBack, R.layout.duoqu_bubble_title_head, root, partId);
            default:
                return null;
        }
    }

    private static UIPart getBodyUIPartByPartId(Activity context, BusinessSmsMessage message, XyCallBack xyCallBack, ViewGroup root, int partId) throws Exception {
        switch (partId) {
            case ViewPartId.PART_BODY_HORIZ_TABLE /*501*/:
                return new BubbleHorizTable(context, message, xyCallBack, R.layout.duoqu_horz_table, root, partId);
            case ViewPartId.PART_BODY_BOTTOM_PARTID /*504*/:
                return new BubbleBodyBottom(context, message, xyCallBack, R.layout.duoqu_bubble_body_bottom, root, partId);
            case ViewPartId.PART_BODY_CALLS_MESSAGE /*505*/:
                return new BubbleBodyCallsMessage(context, message, xyCallBack, R.layout.duoqu_bubble_body_callsmessage, root, partId);
            case ViewPartId.PART_BODY_TRAIN_TICKET /*506*/:
                return new BubbleTrainBody(context, message, xyCallBack, R.layout.duoqu_train_body, root, partId);
            case ViewPartId.PART_BODY_FEATURE /*507*/:
                return new BubbleBodyFeature(context, message, xyCallBack, R.layout.duoqu_bubble_body_feature, root, partId);
            case ViewPartId.PART_BODY_AIR_TICKET /*508*/:
                return new BubbleAirBody(context, message, xyCallBack, R.layout.duoqu_air_body, root, partId);
            case ViewPartId.PART_BODY_TWOITEMVERT_TABLE /*509*/:
                return new BubbleTwoItemsVertTable(context, message, xyCallBack, R.layout.duoqu_two_items_vertical_table, root, partId);
            case ViewPartId.PART_BODY_GENENARALONE /*511*/:
                return new BubbleGeneralOneBody(context, message, xyCallBack, R.layout.duoqu_bubble_body_generalone, root, partId);
            case ViewPartId.PART_BODY_SIMPLE_CALL_NUMBER /*512*/:
                return new BubbleSimpleCallNumberBody(context, message, xyCallBack, R.layout.duoqu_bubble_body_simple_call_number, root, partId);
            case ViewPartId.PART_BODY_ORGSMSTEXT /*513*/:
                return new BubbleOriginSmsText(context, message, xyCallBack, R.layout.duoqu_origin_sms_text, root, partId);
            case ViewPartId.PART_BODY_EXPRESS_STATUS /*514*/:
                return new BubbleBodyExpressStatus(context, message, xyCallBack, R.layout.duoqu_body_express_status, root, partId);
            default:
                return null;
        }
    }

    private static UIPart getFootUIPartByPartId(Activity context, BusinessSmsMessage message, XyCallBack xyCallBack, ViewGroup root, int partId) throws Exception {
        switch (partId) {
            case ViewPartId.PART_BOTTOM_TWO_BUTTON /*901*/:
                return new BubbleBottomTwo(context, message, xyCallBack, R.layout.duoqu_bubble_bottom_two, root, partId);
            default:
                return null;
        }
    }

    static boolean checkHasViewPartId(int partId) throws Exception {
        for (Integer i : VIEW_PART_ID) {
            if (i.intValue() == partId) {
                return true;
            }
        }
        throw new Exception("checkHasViewPartId partId: " + partId + " not Find.");
    }

    public static void setViewBg(Context context, View view, String relativePath, int resId, int width) throws Exception {
        setViewBg(context, view, relativePath, resId, width, false);
    }

    public static void setViewBg(Context context, View view, String relativePath, int resId, int width, boolean cache) throws Exception {
        LogManager.i("setViewBg", "relativePath=" + relativePath + "resId=" + resId);
        try {
            Drawable dw = ViewUtil.getDrawable(context, relativePath, false, cache);
            if (dw != null) {
                ViewUtil.setBackground(view, dw);
            } else if (resId != -1) {
                view.setBackgroundResource(resId);
                GradientDrawable myGrad = (GradientDrawable) view.getBackground();
                if (!StringUtils.isNull(relativePath)) {
                    myGrad.setColor(ResourceCacheUtil.parseColor(relativePath));
                    if (width > 0) {
                        myGrad.setStroke(width, ResourceCacheUtil.parseColor(relativePath));
                    } else {
                        myGrad.setColor(ResourceCacheUtil.parseColor(relativePath));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setViewBg(Context context, View view, String bgColor, String strokeColor, int resId, int width) throws Exception {
        if (!(context == null || view == null)) {
            try {
                if (!(StringUtils.isNull(bgColor) || StringUtils.isNull(strokeColor))) {
                    bgColor = bgColor.trim();
                    strokeColor = strokeColor.trim();
                    try {
                        view.setBackgroundResource(resId);
                        GradientDrawable myGrad = (GradientDrawable) view.getBackground();
                        if (!StringUtils.isNull(bgColor)) {
                            myGrad.setColor(ResourceCacheUtil.parseColor(bgColor));
                        }
                        if (!StringUtils.isNull(strokeColor)) {
                            myGrad.setStroke(width, ResourceCacheUtil.parseColor(strokeColor));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public static View createContextByLayoutId(Context packAgeCtx, int layoutId, ViewGroup root) {
        try {
            return ((LayoutInflater) packAgeCtx.getSystemService("layout_inflater")).inflate(layoutId, root);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isPopupAble(Map<String, Object> handervalueMap, String titleNo) {
        if (handervalueMap != null) {
            try {
                if (StringUtils.isNull(titleNo) || !handervalueMap.containsKey("View_viewid") || StringUtils.isNull((String) handervalueMap.get("View_viewid"))) {
                    return false;
                }
                try {
                    Map<String, PartViewParam> viewPartParamMap = parseViewPartParam((String) handervalueMap.get("View_fdes"));
                    if (!(viewPartParamMap == null || viewPartParamMap.isEmpty())) {
                        handervalueMap.put("viewPartParam", viewPartParamMap);
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            } catch (Exception e2) {
                e2.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public static int getIdentifier(String name, String defType) {
        return Constant.getContext().getResources().getIdentifier(name, defType, Constant.getContext().getPackageName());
    }

    public static int getIntDimen(Context ctx, int dimenId) {
        return Math.round(ctx.getResources().getDimension(dimenId));
    }

    public static int getDouquAttrDimen(IViewAttr iAttr, int duoquAttrId) {
        Object f = iAttr.obtainStyledAttributes((byte) 1, duoquAttrId);
        if (f != null) {
            return Math.round(((Float) f).floatValue());
        }
        return 0;
    }

    public static Object obtainStyledAttributes(TypedArray duoquAttr, byte styleType, int styleId) {
        Object obj = null;
        if (duoquAttr != null) {
            switch (styleType) {
                case (byte) 1:
                    obj = Float.valueOf(duoquAttr.getDimension(styleId, -1.0f));
                    break;
            }
            duoquAttr.recycle();
        }
        return obj;
    }

    public static ArrayList<Integer> getViewPartList(String orgNo) throws Exception {
        ArrayList<Integer> res = new ArrayList();
        int len = orgNo.length();
        int i = 0;
        while (i < len && i + 3 <= len) {
            int viewPartId = Integer.parseInt(orgNo.substring(i, i + 3));
            checkHasViewPartId(viewPartId);
            res.add(Integer.valueOf(viewPartId));
            i += 3;
        }
        return res;
    }

    private static void setPartViewParamRule(PartViewParam param, String paramStr) throws Exception {
        boolean z = true;
        if (paramStr != null) {
            int len = paramStr.length();
            if (len > 0) {
                boolean z2;
                if (Integer.parseInt(paramStr.substring(0, 1)) == 1) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                param.mNeedScroll = z2;
            }
            if (len > 1) {
                if (Integer.parseInt(paramStr.substring(1, 2)) != 1) {
                    z = false;
                }
                param.mAddImageMark = z;
            }
            if (len > 3) {
                param.mBodyHeightType = Integer.parseInt(paramStr.substring(2, 4));
            }
            if (len > 5) {
                param.mBodyMaxHeightType = Integer.parseInt(paramStr.substring(4, 6));
            }
            if (len > 7) {
                param.mPaddingLeftType = Integer.parseInt(paramStr.substring(6, 8));
            }
            if (len > 9) {
                param.mPaddingTopType = Integer.parseInt(paramStr.substring(8, 10));
            }
            if (len > 11) {
                param.mPaddingRightType = Integer.parseInt(paramStr.substring(10, 12));
            }
            if (len > 13) {
                param.mPaddingBottomType = Integer.parseInt(paramStr.substring(12, 14));
            }
            if (len > 15) {
                param.mUiPartMarginTopType = Integer.parseInt(paramStr.substring(14, 16));
            }
        }
    }

    public static Map<String, PartViewParam> parseViewPartParam(String uiPartParam) throws Exception {
        if (uiPartParam == null) {
            return null;
        }
        String[] attr = uiPartParam.split(";");
        Map<String, PartViewParam> res = new HashMap();
        for (String str : attr) {
            String str2;
            String tempStr;
            PartViewParam temp = new PartViewParam();
            int index = str2.indexOf(",");
            if (index > 0) {
                tempStr = str2.substring(0, index);
                str2 = str2.substring(index + 1);
            } else {
                tempStr = str2;
                str2 = null;
            }
            String typeKey = tempStr.substring(0, 1);
            if (PartViewParam.HEAD.equals(typeKey) || PartViewParam.FOOT.equals(typeKey) || PartViewParam.BODY.equals(typeKey)) {
                temp.mLayOutList = getViewPartList(tempStr.substring(1));
                res.put(typeKey, temp);
                setPartViewParamRule(temp, str2);
            }
        }
        return res;
    }

    public static View getDuoquImgMark(Context packAgeCtx) {
        return createContextByLayoutId(packAgeCtx, R.layout.duoqu_img_mark, null);
    }

    public static View getDuoquTimeMark(Context packAgeCtx) {
        return createContextByLayoutId(packAgeCtx, R.layout.duoqu_bottom_info, null);
    }

    public static UIPart getUIPartByPartId(Activity context, BusinessSmsMessage message, XyCallBack xyCallBack, ViewGroup root, int partId) throws Exception {
        if (partId < VTMCDataCache.MAXSIZE) {
            return getHeadUIPartByPartId(context, message, xyCallBack, root, partId);
        }
        if (partId < 900) {
            return getBodyUIPartByPartId(context, message, xyCallBack, root, partId);
        }
        return getFootUIPartByPartId(context, message, xyCallBack, root, partId);
    }

    public static ScrollView createScrollView(Context packAgeCtx, View root) {
        return (ScrollView) createContextByLayoutId(packAgeCtx, R.layout.duoqu_scroll_view, null);
    }

    public static ViewGroup createFrameViewGroup(Context packAgeCtx) {
        return (ViewGroup) createContextByLayoutId(packAgeCtx, R.layout.duoqu_frame_view, null);
    }

    public static RelativeLayout createRootView(Context packAgeCtx) {
        RelativeLayout rootView = new RelativeLayout(packAgeCtx);
        rootView.setLayoutParams(new LayoutParams(-1, -2));
        return rootView;
    }

    @SuppressLint({"NewApi"})
    public static int setBodyViewPadding(Context context, View view, View childView, PartViewParam viewParam, int addPadding) {
        if (view == null || viewParam == null) {
            return -1;
        }
        int leftPadding = getBodyViewPadding(context, viewParam.mPaddingLeftType);
        int topPadding = getBodyViewPadding(context, viewParam.mPaddingTopType);
        int rightPadding = getBodyViewPadding(context, viewParam.mPaddingRightType);
        int bottomPadding = getBodyViewPadding(context, viewParam.mPaddingBottomType);
        if (leftPadding == 0 && topPadding == 0 && rightPadding == 0) {
            if (bottomPadding != 0) {
            }
            return 1;
        }
        view.setPaddingRelative(leftPadding, topPadding, rightPadding, bottomPadding);
        return 1;
    }

    public static int getBodyViewPadding(Context context, int type) {
        switch (type) {
            case 11:
                return TYPE_PADDING_11;
            default:
                return 0;
        }
    }

    public static int setBodyLayoutHeight(Context context, LayoutParams lparam, int layoutHeightType, int sBodyPadding) {
        int h = -1;
        switch (layoutHeightType) {
            case 11:
                h = TYPE_VIEW_HEIGHT_11;
                break;
        }
        if (h != -1) {
            lparam.height = h;
        }
        return h;
    }

    public static int getInnerLayoutMargin(Context context, int marginType) {
        switch (marginType) {
            case 111:
                return TYPE_SPLIT_LR_MARGIN_111;
            case 112:
                return TYPE_SPLIT_LR_MARGIN_112;
            default:
                return 0;
        }
    }

    public static int setLayoutMarginTop(Context context, LayoutParams lparam, int marginTopType) {
        int marginTop = -1;
        if (lparam != null && (lparam instanceof MarginLayoutParams)) {
            MarginLayoutParams lp = (MarginLayoutParams) lparam;
            switch (marginTopType) {
                case 11:
                    marginTop = TYPE_MARGIN_11;
                    break;
            }
            if (marginTop != -1) {
                lp.setMargins(lp.leftMargin, marginTop, lp.rightMargin, lp.bottomMargin);
            }
        }
        return marginTop;
    }

    public static void setViewTreeObserver(final View view, final XyCallBack callBack) {
        try {
            view.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                @SuppressLint({"NewApi"})
                public void onGlobalLayout() {
                    try {
                        view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } catch (NoSuchMethodError e) {
                        try {
                            view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } catch (NoSuchMethodError e2) {
                            if (LogManager.debug) {
                                e2.printStackTrace();
                            }
                        } catch (Exception e3) {
                            if (LogManager.debug) {
                                e3.printStackTrace();
                            }
                        }
                    } catch (Exception e32) {
                        if (LogManager.debug) {
                            e32.printStackTrace();
                        }
                    }
                    callBack.execute(new Object[0]);
                }
            });
        } catch (Exception e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("setViewTreeObserver error:" + e.getMessage(), e);
        }
    }

    public static void setRippleDrawable(View view) {
    }

    public static boolean displayMarkImage(BusinessSmsMessage msg) {
        return true;
    }

    public static boolean displayTime(BusinessSmsMessage msg) {
        return false;
    }

    public static int indexOfChild(View view, ViewGroup apView) {
        if (view == null || apView == null) {
            Log.e("duoqu_xiaoyuan", "indexOfChild view == null || apView == null");
            return -1;
        }
        int childCount = apView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = apView.getChildAt(i);
            if (child == view) {
                return i;
            }
            View tempChild = child.findViewById(DuoquBubbleViewManager.DUOQU_BUBBLE_VIEW_ID);
            if (tempChild != null && tempChild == view) {
                return i;
            }
        }
        return -1;
    }
}
