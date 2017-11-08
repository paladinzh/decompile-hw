package com.android.mms.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import android.view.ViewStub;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ISmartSmsEvent;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.mms.ui.menu.ISmartSmsListItemHolder;
import cn.com.xy.sms.sdk.mms.ui.menu.ISmartSmsUIHolder;
import cn.com.xy.sms.sdk.ui.anim.CardAnimUtil;
import cn.com.xy.sms.sdk.ui.bubbleview.DuoquBubbleViewManager;
import cn.com.xy.sms.sdk.ui.popu.popupview.BubblePopupView;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.simplebubbleview.DuoquSimpleBubbleViewManager;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.SdkCallBack;
import com.google.android.gms.R;
import com.huawei.mms.ui.SpandTextView;
import com.huawei.mms.util.StatisticalHelper;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressLint({"NewApi"})
public class SmartSmsBubbleManager extends AnimatorListenerAdapter implements SdkCallBack, OnClickListener, ISmartSmsEvent {
    private static final int SMS_ROOT_GROUP_MARGIN_END = ContentUtil.getDimension(R.dimen.duoqu_sms_root_group_margin_end);
    private static long mEnterTime = 0;
    private static boolean mRichAnimRuning = false;
    private static int mSmartSmsUiHolderCode = 0;
    private BubblePopupView mBubblePopupView;
    private JSONObject mCacheItemData = null;
    private ViewGroup mChangeBtnContainer = null;
    private Activity mCtx;
    private Handler mHandler = null;
    private View mMessageBlockSuper = null;
    private MessageItem mMessageItem = null;
    private View mMsgListItem = null;
    private TextView mOrgDateTextView;
    private ImageView mOrgLockedIndicatorView;
    private ImageView mOrgSubscriptionNetworkTypeView;
    private ViewGroup mRichBubbleLayoutParent;
    private TextView mRichInfoTime;
    private ViewGroup mRichItemGroup = null;
    private ImageView mRichLockedIndicatorView;
    private TextView mRichShowInfo;
    private ImageView mRichSubscriptionNetworkTypeView;
    private int mShowBubbleModel;
    private SdkCallBack mSimpleCallBack = null;
    private ViewGroup mSimpleItemGroup = null;
    private ISmartSmsUIHolder mSmartHolder = null;
    private ISmartSmsListItemHolder mSmartSmsUiHolder = null;
    private View mSmsRootGroup = null;
    private View mSmsViewSuperParent = null;
    private long mUseMsgId = -1;

    private static class BubbleHandler extends Handler {
        private WeakReference<Activity> mReference = null;

        public BubbleHandler(Activity activity) {
            if (activity != null) {
                this.mReference = new WeakReference(activity);
            }
        }

        public void handleMessage(Message msg) {
            try {
                if (SmartSmsSdkUtil.activityIsFinish((Activity) this.mReference.get())) {
                    return;
                }
            } catch (Throwable e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("BubbleHandler handleMessage error: " + e.getMessage(), e);
            }
            super.handleMessage(msg);
        }

        public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
            try {
                if (SmartSmsSdkUtil.activityIsFinish((Activity) this.mReference.get())) {
                    return false;
                }
            } catch (Throwable e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("BubbleHandler sendMessageAtTime error: " + e.getMessage(), e);
            }
            return super.sendMessageAtTime(msg, uptimeMillis);
        }
    }

    private static void setSmartSmsUiHolderCode(int smartSmsUiHolderCode) {
        mSmartSmsUiHolderCode = smartSmsUiHolderCode;
    }

    private static void setEnterTime(long enterTime) {
        mEnterTime = enterTime;
    }

    public void onAnimationEnd(Animator animation) {
        setIsAnimRunning(false);
        if (this.mHandler != null) {
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    SmartSmsBubbleManager.this.showCompleteItemInListView();
                }
            }, 10);
        }
        if (!isShowingRich() && this.mSmartSmsUiHolder != null) {
            this.mSmartSmsUiHolder.bindItemAfter(true);
        }
    }

    private static void setIsAnimRunning(boolean isRunning) {
        mRichAnimRuning = isRunning;
    }

    private static boolean isAnimRunning() {
        return mRichAnimRuning;
    }

    public SmartSmsBubbleManager(ISmartSmsListItemHolder smartSmsUiHolder, View msgListItem) {
        this.mSmartSmsUiHolder = smartSmsUiHolder;
        this.mMsgListItem = msgListItem;
        if (this.mMsgListItem != null && smartSmsUiHolder != null) {
            this.mCtx = smartSmsUiHolder.getActivityContext();
            if (this.mCtx != null) {
                this.mHandler = new BubbleHandler(this.mCtx);
                if (this.mCtx instanceof ComposeMessageActivity) {
                    ComposeMessageFragment fragment = (ComposeMessageFragment) FragmentTag.getFragmentByTag(this.mCtx, "Mms_UI_CMF");
                    if (fragment != null) {
                        this.mSmartHolder = fragment;
                    }
                } else if (this.mCtx instanceof ConversationList) {
                    Fragment f = ((ConversationList) this.mCtx).getRightFragment();
                    if (f instanceof ComposeMessageFragment) {
                        this.mSmartHolder = (ISmartSmsUIHolder) f;
                    }
                }
                int hashCode = this.mCtx.hashCode();
                if (mSmartSmsUiHolderCode != hashCode) {
                    setSmartSmsUiHolderCode(hashCode);
                    setEnterTime(System.currentTimeMillis());
                }
                initView();
            }
        }
    }

    public void bindBubbleView(MessageItem mMessageItem, int showBubbleMode) {
        if (mMessageItem != null && this.mSmartSmsUiHolder != null) {
            if (this.mSmartSmsUiHolder.isScrollFing() && this.mSmartSmsUiHolder.getListView() != null && this.mSmartSmsUiHolder.getListView().getTag(R.id.tag_scroll_to_last_item) == null) {
                setScrollToLastItemTag(false);
            }
            this.mUseMsgId = -1;
            this.mMessageItem = mMessageItem;
            this.mShowBubbleModel = showBubbleMode;
            bindRichBubbleView(showBubbleMode);
            if (showBubbleMode == 1) {
                bindSimpleBubbleView();
            }
        }
    }

    private void bindRichBubbleView(int showBubbleModel) {
        if (this.mSmartSmsUiHolder == null || this.mMessageItem == null) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsBubbleManager bindRichBubbleView mSmartSmsUiHolder or mMessageItem is null", null);
        } else if (this.mRichBubbleLayoutParent == null || this.mChangeBtnContainer == null) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsBubbleManager bindRichBubbleView mRichItemGroup or mChangeBtnContainer is null", null);
        } else {
            this.mCacheItemData = SmartSmsSdkUtil.getBubbleDataFromCache(this.mMessageItem);
            if (this.mCacheItemData == null) {
                getRichBubbleData();
            } else if (getShowRichViewStatu() == 2) {
                bindRichView(false);
            } else {
                showDefaultListItem(false);
            }
            setChangeBtnViewVisibility();
        }
    }

    private void addRichItemDataToCache(MessageItem messageItem, JSONObject itemData) {
        String key = String.valueOf(messageItem.mMsgId) + String.valueOf(messageItem.mDate);
        if (itemData != null && !this.mSmartSmsUiHolder.isEditAble()) {
            if (!itemData.has("DISPLAY")) {
                setShowRichViewStatu(this.mShowBubbleModel);
            }
            SmartSmsSdkUtil.putBubbleDataToCache(key, itemData);
        }
    }

    public void hideBubbleView() {
        ContentUtil.setViewVisibility(this.mSimpleItemGroup, 8);
        showSimpleBubbleView();
    }

    private void showSimpleBubbleView() {
        ContentUtil.setViewVisibility(this.mMessageBlockSuper, 0);
        ContentUtil.setViewVisibility(this.mRichBubbleLayoutParent, 8);
        setBubbleBottomContent();
    }

    private void showRichBubbleView() {
        ContentUtil.setViewVisibility(this.mMessageBlockSuper, 8);
        ContentUtil.setViewVisibility(this.mRichBubbleLayoutParent, 0);
        setBubbleBottomContent();
    }

    private void setChangeBtnViewVisibility() {
        if (this.mChangeBtnContainer != null) {
            if (this.mSmartSmsUiHolder.isEditAble() || getShowRichViewStatu() == -1) {
                ContentUtil.setViewVisibility(this.mChangeBtnContainer, 8);
            } else if (isShowingRich()) {
                ContentUtil.setViewVisibility(this.mChangeBtnContainer, 8);
            } else {
                ContentUtil.setViewVisibility(this.mChangeBtnContainer, 0);
            }
        }
    }

    private void initView() {
        this.mSimpleItemGroup = (ViewGroup) this.mSmartSmsUiHolder.findViewById(R.id.duoqu_bubble_action_layout);
        if (this.mRichBubbleLayoutParent == null) {
            ViewStub mRichBubbleLayoutParentStub = (ViewStub) this.mSmartSmsUiHolder.findViewById(R.id.duoqu_rich_group_stub);
            if (mRichBubbleLayoutParentStub != null) {
                View mRichBubbleLayout = mRichBubbleLayoutParentStub.inflate();
                this.mRichBubbleLayoutParent = (ViewGroup) mRichBubbleLayout.findViewById(R.id.bubble_layout);
                this.mRichItemGroup = (ViewGroup) mRichBubbleLayout.findViewById(R.id.duoqu_rich_item_group);
                this.mRichInfoTime = (TextView) mRichBubbleLayout.findViewById(R.id.rich_info_time);
                this.mRichShowInfo = (TextView) mRichBubbleLayout.findViewById(R.id.btn_switch_text);
                this.mRichSubscriptionNetworkTypeView = (ImageView) mRichBubbleLayout.findViewById(R.id.rich_subscriptionnetworktype);
                this.mRichLockedIndicatorView = (ImageView) mRichBubbleLayout.findViewById(R.id.rich_locked_indicator);
            }
        }
        this.mOrgDateTextView = (TextView) this.mSmartSmsUiHolder.findViewById(R.id.date_view);
        this.mOrgSubscriptionNetworkTypeView = (ImageView) this.mSmartSmsUiHolder.findViewById(R.id.subscriptionnetworktype);
        this.mOrgLockedIndicatorView = (ImageView) this.mSmartSmsUiHolder.findViewById(R.id.locked_indicator);
        if (this.mChangeBtnContainer == null) {
            ViewStub mChangeBtnStub = (ViewStub) this.mSmartSmsUiHolder.findViewById(R.id.duoqu_right_change_btn_stub);
            if (mChangeBtnStub != null) {
                this.mChangeBtnContainer = (ViewGroup) mChangeBtnStub.inflate().findViewById(R.id.duoqu_right_change_btn_container);
            }
        }
        this.mSmsRootGroup = this.mSmartSmsUiHolder.findViewById(R.id.message_block);
        this.mMessageBlockSuper = this.mSmartSmsUiHolder.findViewById(R.id.message_block_super);
        this.mSmsViewSuperParent = this.mSmartSmsUiHolder.findViewById(R.id.mms_layout_view_super_parent);
        if ((this.mCtx instanceof ComposeMessageActivity) && ((ComposeMessageFragment) FragmentTag.getFragmentByTag(this.mCtx, "Mms_UI_CMF")).isPeeking()) {
            MarginLayoutParams lp = (MarginLayoutParams) this.mSmsRootGroup.getLayoutParams();
            lp.setMarginEnd(SMS_ROOT_GROUP_MARGIN_END);
            this.mSmsRootGroup.setLayoutParams(lp);
        }
        this.mSmartSmsUiHolder.setRichViewLongClick(this.mRichItemGroup);
        if (this.mChangeBtnContainer != null) {
            this.mChangeBtnContainer.setOnClickListener(this);
        }
        if (this.mRichShowInfo != null) {
            this.mRichShowInfo.setOnClickListener(this);
        }
    }

    private int getShowRichViewStatu() {
        try {
            if (this.mCacheItemData == null) {
                return -1;
            }
            if (this.mSmartSmsUiHolder.isEditAble()) {
                return 1;
            }
            if (this.mCacheItemData.has("DISPLAY")) {
                return this.mCacheItemData.getInt("DISPLAY");
            }
            return 2;
        } catch (JSONException e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsBubbleManager getShowRichViewStatu error : " + e.getMessage(), e);
            return 1;
        }
    }

    private void setShowRichViewStatu(int statu) {
        try {
            if (this.mCacheItemData != null && !this.mSmartSmsUiHolder.isEditAble()) {
                this.mCacheItemData.put("DISPLAY", statu);
            }
        } catch (JSONException e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsBubbleManager setShowRichViewStatu error : " + e.getMessage(), e);
        }
    }

    public boolean isOnlyShowSmsContent() {
        if (this.mCacheItemData != null) {
            String text = this.mCacheItemData.optString("View_fdes");
            if (!TextUtils.isEmpty(text) && text.contains("513")) {
                return true;
            }
        }
        return false;
    }

    private boolean bindRichView(boolean anim) {
        if (SmartSmsSdkUtil.activityIsFinish(this.mCtx)) {
            return false;
        }
        String msgIds = String.valueOf(this.mMessageItem.mMsgId);
        HashMap<String, Object> extendMap = this.mMessageItem.getExtendMap();
        this.mMessageItem.appendMapOfIsSecrect(extendMap);
        extendMap.put("isClickAble", Boolean.valueOf(!this.mSmartSmsUiHolder.isEditAble()));
        SpannableStringBuilder spannableStringBuilder = null;
        boolean onlyShowSmsContent = false;
        if (this.mCacheItemData != null) {
            onlyShowSmsContent = isOnlyShowSmsContent();
        }
        if (onlyShowSmsContent) {
            spannableStringBuilder = this.mSmartSmsUiHolder.getCachedLinkingMsg();
            extendMap.put("linkingMsg", spannableStringBuilder);
        }
        View richBubbleView = DuoquBubbleViewManager.getRichBubbleView(this.mCtx, this.mCacheItemData, msgIds, this.mMessageItem, this.mMsgListItem, this.mSmartSmsUiHolder.getListView(), extendMap, this);
        if (richBubbleView == null || this.mRichItemGroup == null) {
            this.mBubblePopupView = null;
            if (anim) {
                setIsAnimRunning(false);
            }
            setShowRichViewStatu(-1);
            noRichDataShowDefaultListItem();
        } else {
            this.mBubblePopupView = (BubblePopupView) richBubbleView;
            this.mRichItemGroup.removeAllViews();
            ViewParent parent = richBubbleView.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(richBubbleView);
            }
            this.mRichItemGroup.addView(richBubbleView);
            if (anim) {
                runSimpleAnimToRich();
            } else {
                showRichBubbleView();
            }
            setShowRichViewStatu(2);
            if (onlyShowSmsContent && r8 == null) {
                this.mSmartSmsUiHolder.bindItemAfter(false);
            }
        }
        return true;
    }

    private void getRichBubbleData() {
        String msgId = String.valueOf(this.mMessageItem.mMsgId);
        this.mRichItemGroup.setTag(msgId);
        DuoquBubbleViewManager.getRichBubbleData(this.mCtx, msgId, this.mMessageItem, (byte) 1, this.mMsgListItem, null, this.mRichItemGroup, this.mSmartSmsUiHolder.getListView(), this, isScrollFing());
    }

    public void execute(Object... obj) {
        if (!SmartSmsSdkUtil.activityIsFinish(this.mCtx)) {
            if (obj == null || obj.length == 0 || this.mSmartSmsUiHolder == null) {
                noRichDataShowDefaultListItem();
                return;
            }
            if (obj.length > 2) {
                String oldmsgid = obj[2];
                String orgMsgId = (String) this.mRichItemGroup.getTag();
                if (StringUtils.isNull(orgMsgId) || StringUtils.isNull(oldmsgid) || !orgMsgId.equals(oldmsgid)) {
                    return;
                }
            }
            switch (((Integer) obj[0]).intValue()) {
                case -4:
                    noRichDataShowDefaultListItem();
                    if (this.mSmartHolder != null) {
                        this.mUseMsgId = this.mMessageItem.mMsgId;
                        this.mSmartHolder.addNeedRefreshSmartBubbleItem(this);
                        break;
                    }
                    break;
                case -2:
                    noRichDataShowDefaultListItem();
                    break;
                case -1:
                    noRichDataShowDefaultListItem();
                    break;
                case 0:
                    this.mCacheItemData = (JSONObject) obj[1];
                    if (this.mShowBubbleModel == 2 && getShowRichViewStatu() == 2) {
                        bindRichView(false);
                    } else {
                        showDefaultListItem(false);
                    }
                    addRichItemDataToCache(this.mMessageItem, this.mCacheItemData);
                    break;
                case 1:
                    this.mCacheItemData = (JSONObject) obj[1];
                    addRichItemDataToCache(this.mMessageItem, this.mCacheItemData);
                    asyncBackBindView();
                    break;
            }
        }
    }

    private boolean isScrollFing() {
        if (this.mSmartSmsUiHolder == null || this.mSmartSmsUiHolder.getListView() == null) {
            return false;
        }
        return this.mSmartSmsUiHolder.isScrollFing();
    }

    private void runRichAnimToSimple() {
        ContentUtil.setViewVisibility(this.mChangeBtnContainer, 0);
        CardAnimUtil.viewChangeAnim(this.mRichBubbleLayoutParent, this.mMessageBlockSuper, null, this);
    }

    private void runSimpleAnimToRich() {
        setBubbleBottomContent();
        CardAnimUtil.viewChangeAnim(this.mMessageBlockSuper, this.mRichBubbleLayoutParent, this.mBubblePopupView, this);
    }

    private void showDefaultListItem(boolean needAnim) {
        if (this.mSmartSmsUiHolder != null) {
            this.mSmartSmsUiHolder.showDefaultListItem();
        }
        bindSimpleBubbleView();
        if (!needAnim) {
            ContentUtil.setViewVisibility(this.mMessageBlockSuper, 0);
            ContentUtil.setViewVisibility(this.mRichBubbleLayoutParent, 8);
            setBubbleBottomContent();
        }
    }

    private void noRichDataShowDefaultListItem() {
        this.mCacheItemData = null;
        setChangeBtnViewVisibility();
        showDefaultListItem(false);
    }

    private void bindSimpleBubbleView() {
        if (this.mSimpleItemGroup != null) {
            String msgIds = String.valueOf(this.mMessageItem.mMsgId);
            this.mSimpleItemGroup.setTag(msgIds);
            try {
                JSONArray btnData = ContentUtil.getActionJsonArray(this.mCacheItemData);
                if (btnData == null || btnData.length() <= 0) {
                    DuoquSimpleBubbleViewManager.getSimpleBubbleData(msgIds, this.mMessageItem, (byte) 1, this.mMessageItem.getExtendMap(), getSimpleBubbleDataCallBack(), isScrollFing());
                } else {
                    bindSimpleView(btnData, this.mMessageItem, this.mMsgListItem);
                }
            } catch (Exception e) {
                ContentUtil.setViewVisibility(this.mSimpleItemGroup, 8);
                SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsBubbleManager bindSimpleBubbleView error: " + e.getMessage(), e);
            }
        }
    }

    private SdkCallBack getSimpleBubbleDataCallBack() {
        if (this.mSimpleCallBack == null) {
            this.mSimpleCallBack = new SdkCallBack() {
                public void execute(Object... obj) {
                    if (obj != null && obj.length != 0 && SmartSmsBubbleManager.this.mSmartSmsUiHolder != null && !SmartSmsSdkUtil.activityIsFinish(SmartSmsBubbleManager.this.mCtx)) {
                        int statu = ((Integer) obj[0]).intValue();
                        if (obj.length > 2) {
                            String oldmsgid = obj[2];
                            String orgMsgId = (String) SmartSmsBubbleManager.this.mSimpleItemGroup.getTag();
                            if (StringUtils.isNull(orgMsgId) || StringUtils.isNull(oldmsgid) || !orgMsgId.equals(oldmsgid)) {
                                return;
                            }
                        }
                        switch (statu) {
                            case -4:
                            case -2:
                            case -1:
                                SmartSmsBubbleManager.this.bindSimpleView(null, SmartSmsBubbleManager.this.mMessageItem, SmartSmsBubbleManager.this.mMsgListItem);
                                break;
                            case 0:
                                SmartSmsBubbleManager.this.bindSimpleView((JSONArray) obj[1], SmartSmsBubbleManager.this.mMessageItem, SmartSmsBubbleManager.this.mMsgListItem);
                                break;
                            case 1:
                                SmartSmsBubbleManager.this.asyncBackBindSimpleView((JSONArray) obj[1]);
                                break;
                        }
                    }
                }
            };
        }
        return this.mSimpleCallBack;
    }

    private void asyncBackBindSimpleView(final JSONArray simpleBubbleData) {
        if (!this.mSmartSmsUiHolder.isScrollFing() && this.mCtx != null) {
            this.mCtx.runOnUiThread(new Runnable() {
                public void run() {
                    SmartSmsBubbleManager.this.bindSimpleView(simpleBubbleData, SmartSmsBubbleManager.this.mMessageItem, SmartSmsBubbleManager.this.mMsgListItem);
                    SmartSmsBubbleManager.this.showLastItemComplete();
                }
            });
        }
    }

    private void bindSimpleView(JSONArray btnData, MessageItem mMessageItem, View msgListItem) {
        boolean z = false;
        if (!SmartSmsSdkUtil.activityIsFinish(this.mCtx)) {
            View buttonView = null;
            if (btnData != null) {
                try {
                    HashMap<String, Object> extend = mMessageItem.getExtendMap();
                    String str = "isClickAble";
                    if (!this.mSmartSmsUiHolder.isEditAble()) {
                        z = true;
                    }
                    extend.put(str, Boolean.valueOf(z));
                    mMessageItem.appendSimpleViewExtendMap(extend);
                    extend.put("bubbleData", this.mCacheItemData);
                    buttonView = DuoquSimpleBubbleViewManager.getSimpleBubbleView(this.mCtx, btnData, this.mSimpleItemGroup, extend);
                } catch (Exception e) {
                    ContentUtil.setViewVisibility(this.mSimpleItemGroup, 8);
                }
            }
            if (buttonView == null || this.mSimpleItemGroup == null) {
                if (this.mSimpleItemGroup != null) {
                    ContentUtil.setViewVisibility(this.mSimpleItemGroup, 8);
                    this.mSmsRootGroup.setBackgroundResource(R.drawable.duoqu_no_button_bg);
                }
            }
            ContentUtil.setViewVisibility(this.mSimpleItemGroup, 0);
            this.mSmsRootGroup.setBackgroundResource(R.drawable.duoqu_has_botton_bg);
        }
    }

    private void showCompleteItemInListView() {
        try {
            if (this.mSmartSmsUiHolder == null || this.mRichItemGroup == null || this.mSmsRootGroup == null) {
                SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsBubbleManager showCompleteItemInListView mSmartSmsUiHolder is null", null);
                return;
            }
            ListView listView = this.mSmartSmsUiHolder.getListView();
            if (listView == null) {
                SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsBubbleManager showCompleteItemInListView listView is null", null);
                return;
            }
            int[] pos = new int[2];
            listView.getLocationInWindow(pos);
            int listBottomY = pos[1] + listView.getMeasuredHeight();
            this.mSmsViewSuperParent.getLocationInWindow(pos);
            int y = (this.mSmsViewSuperParent.getMeasuredHeight() + pos[1]) - listBottomY;
            if (y > 0) {
                listView.smoothScrollBy((listView.getPaddingBottom() + y) - 2, 0);
            }
            if (listView.getAdapter() instanceof BaseAdapter) {
                ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
            }
        } catch (Exception e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsBubbleManager showCompleteItemInListView error:" + e.getMessage(), e);
        }
    }

    public boolean isShowingRich() {
        boolean z = false;
        if (this.mRichBubbleLayoutParent == null) {
            return false;
        }
        if (this.mRichBubbleLayoutParent.getVisibility() == 0) {
            z = true;
        }
        return z;
    }

    public ViewGroup getRichBubbleLayoutParent() {
        return this.mRichBubbleLayoutParent;
    }

    public void onClick(View arg0) {
        if (this.mCacheItemData != null && !isAnimRunning()) {
            setIsAnimRunning(true);
            if (this.mRichBubbleLayoutParent.getVisibility() == 8) {
                StatisticalHelper.incrementReportCount(this.mCtx, 2171);
                bindRichView(true);
            } else {
                StatisticalHelper.incrementReportCount(this.mCtx, 2172);
                if (this.mSmartSmsUiHolder != null) {
                    this.mSmartSmsUiHolder.bindCommonItem();
                }
                bindSimpleBubbleView();
                runRichAnimToSimple();
                setShowRichViewStatu(1);
            }
        }
    }

    private void asyncBackBindView() {
        if (!this.mSmartSmsUiHolder.isScrollFing() && this.mCtx != null) {
            this.mCtx.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        String orgMsgId = (String) SmartSmsBubbleManager.this.mRichItemGroup.getTag();
                        if (orgMsgId != null && orgMsgId.equals(String.valueOf(SmartSmsBubbleManager.this.mMessageItem.mMsgId))) {
                            ListView listView = SmartSmsBubbleManager.this.mSmartSmsUiHolder.getListView();
                            if (listView != null) {
                                int beforeH;
                                int pos = listView.getLastVisiblePosition();
                                if (SmartSmsBubbleManager.this.mMessageBlockSuper.getVisibility() == 0) {
                                    beforeH = SmartSmsBubbleManager.this.mMessageBlockSuper.getMeasuredHeight();
                                } else {
                                    beforeH = SmartSmsBubbleManager.this.mRichItemGroup.getMeasuredHeight();
                                }
                                if (SmartSmsBubbleManager.this.mShowBubbleModel == 2 && SmartSmsBubbleManager.this.getShowRichViewStatu() == 2) {
                                    SmartSmsBubbleManager.this.bindRichView(false);
                                } else {
                                    SmartSmsBubbleManager.this.showDefaultListItem(false);
                                }
                                SmartSmsBubbleManager.this.setChangeBtnViewVisibility();
                                if (!SmartSmsBubbleManager.this.showLastItemComplete()) {
                                    ListAdapter listAdapter = listView.getAdapter();
                                    if (listAdapter != null) {
                                        if (pos != listAdapter.getCount() - 1) {
                                            int afterH;
                                            if (SmartSmsBubbleManager.this.mMessageBlockSuper.getVisibility() == 0) {
                                                afterH = SmartSmsBubbleManager.this.mMessageBlockSuper.getMeasuredHeight();
                                            } else {
                                                afterH = SmartSmsBubbleManager.this.mRichItemGroup.getMeasuredHeight();
                                            }
                                            int moveH = afterH - beforeH;
                                            if (moveH > 0) {
                                                listView.smoothScrollBy(moveH, 0);
                                            }
                                            SmartSmsBubbleManager.this.showCompleteItemInListView();
                                            return;
                                        }
                                        listView.setSelection(pos);
                                        SmartSmsBubbleManager.this.setLastItemComplete(listView);
                                    }
                                }
                            }
                        }
                    } catch (Throwable e) {
                        SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsBubbleManager run error:" + e.getMessage(), e);
                    }
                }
            });
        }
    }

    private boolean showLastItemComplete() {
        try {
            if (System.currentTimeMillis() - mEnterTime > 10000) {
                return false;
            }
            ListView listView = this.mSmartSmsUiHolder.getListView();
            if (listView == null) {
                return false;
            }
            Object scrollToLastItem = listView.getTag(R.id.tag_scroll_to_last_item);
            if (scrollToLastItem != null && !((Boolean) scrollToLastItem).booleanValue()) {
                return false;
            }
            listView.setSelection(listView.getBottom());
            return true;
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("showLastItemComplete error:", e);
            return false;
        }
    }

    private void setLastItemComplete(ListView listView) {
        View v = listView.getChildAt(listView.getChildCount() - 1);
        if (v instanceof ISmartSmsListItemHolder) {
            ISmartSmsListItemHolder listItem = (ISmartSmsListItemHolder) v;
            if (listItem.getSmartSmsBubble() != null) {
                listItem.getSmartSmsBubble().showCompleteItemInListView();
            }
        }
    }

    public boolean isNeedRefresh() {
        if (this.mMessageItem == null || this.mMessageItem.mMsgId != this.mUseMsgId) {
            return false;
        }
        this.mUseMsgId = -1;
        return true;
    }

    private void setBubbleBottomContent() {
        if (!(this.mOrgDateTextView == null || this.mRichInfoTime == null)) {
            this.mRichInfoTime.setText(this.mOrgDateTextView.getText());
        }
        cloneDisplayStatus(this.mRichSubscriptionNetworkTypeView, this.mOrgSubscriptionNetworkTypeView);
        cloneDisplayStatus(this.mRichLockedIndicatorView, this.mOrgLockedIndicatorView);
    }

    private static void cloneDisplayStatus(ImageView imageView, ImageView sourceImageView) {
        if (sourceImageView == null || sourceImageView.getVisibility() != 0) {
            ContentUtil.setViewVisibility(imageView, 8);
            return;
        }
        setImageDrawableBySourceImageView(imageView, sourceImageView);
        imageView.setContentDescription(sourceImageView.getContentDescription());
        ContentUtil.setViewVisibility(imageView, 0);
    }

    private static void setImageDrawableBySourceImageView(ImageView imageView, ImageView sourceImageView) {
        if (imageView != null && sourceImageView != null) {
            Drawable drawable = sourceImageView.getDrawable();
            if (drawable != null) {
                imageView.setImageDrawable(drawable);
            }
            drawable = sourceImageView.getBackground();
            if (drawable != null) {
                imageView.setBackground(drawable);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int onSmartSmsEvent(int eventType, Map extend) {
        switch (eventType) {
            case 1:
                try {
                    ListView listView = this.mSmartSmsUiHolder.getListView();
                    if (listView != null && (listView.getAdapter() instanceof BaseAdapter)) {
                        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                        break;
                    }
                } catch (Throwable e) {
                    SmartSmsSdkUtil.smartSdkExceptionLog("onSmartSmsEvent eventType:" + eventType + " error:" + e.getMessage(), e);
                    break;
                }
            case 2:
                if (extend != null) {
                    if (this.mMsgListItem != null && extend.containsKey("spandTextView")) {
                        ((SpandTextView) extend.get("spandTextView")).setSpandTouchMonitor((MessageListItem) this.mMsgListItem);
                        break;
                    }
                }
                break;
        }
        return 0;
    }

    private void setScrollToLastItemTag(boolean canScrollToLastItem) {
        if (this.mSmartSmsUiHolder != null) {
            ListView listView = this.mSmartSmsUiHolder.getListView();
            if (listView != null) {
                listView.setTag(R.id.tag_scroll_to_last_item, Boolean.valueOf(canScrollToLastItem));
            }
        }
    }
}
