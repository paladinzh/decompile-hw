package cn.com.xy.sms.sdk.mms.ui.menu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.entity.PhoneSmsParseManager;
import cn.com.xy.sms.sdk.iccid.IccidLocationUtil;
import cn.com.xy.sms.sdk.ui.bubbleview.DuoquBubbleViewManager;
import cn.com.xy.sms.sdk.ui.menu.PopMenus;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.settings.SimCardUtil;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseBubbleManager;
import cn.com.xy.sms.util.ParseManager;
import cn.com.xy.sms.util.SdkCallBack;
import com.android.mms.data.Contact;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.SmartSmsBubbleManager;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.StatisticalHelper;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SmartSmsComposeManager implements ISmartSmsMenuManager {
    private static final String ACTION_DATA = "action_data";
    private static final String LOCATION_KEY = "code";
    private static final String LOCATION_VALUE_CN = "CN";
    private static final String SECONDMENU = "secondmenu";
    public static final short SMART_SMS_DUOQU_EVENT_HIDE_EDIT_LAYOUT = (short) 4;
    public static final short SMART_SMS_DUOQU_EVENT_HIDE_EDIT_LAYOUT_FIRST_IN = (short) 6;
    public static final short SMART_SMS_DUOQU_EVENT_SHOW_EDIT_LAYOUT = (short) 1;
    public static final short SMART_SMS_DUOQU_EVENT_SHOW_EDIT_LAYOUT_WITHOUT_ANIMATION = (short) 7;
    public static final short SMART_SMS_DUOQU_EVENT_SHOW_VIEW_MENU = (short) 5;
    public static final short SMART_SMS_SHOW_MENU_IN_CONVERSATION = (short) 2;
    public static final short SMART_SMS_SHOW_MENU_NEW_CONVERSATION = (short) 1;
    private final String TAG = "XIAOYUAN";
    private LinearLayout mButtonToEditMenu;
    private LinearLayout mButtonToSmartMenu;
    private Activity mCtx;
    private Map<String, String> mExtraMenuDataMap = new HashMap();
    private String mFormatNumber;
    private Handler mHandler = null;
    private ISmartSmsUIHolder mISmartSmsUIHolder;
    private boolean mIsInitMenuView = false;
    private boolean mIsLoad = false;
    private boolean mIsNotifyComposeMessage = true;
    private boolean mIsUsedTheme = false;
    private LayoutInflater mLayoutInflater = null;
    private String mMenuJsonData = null;
    private View mMenuRootLayout = null;
    private ViewStub mMenuRootStub = null;
    LinkedList<SmartSmsBubbleManager> mNeedRefreshBubbleItem = new LinkedList();
    private int mNumOperator;
    private String mNumber = null;
    private PopMenus mPopupWindowCustommenu;
    private int mSelectedSimIndex = 0;
    private LinearLayout mSmartMenuContent;
    private RelativeLayout mSmartMenuLayout;

    private static class BeforeInitBubbleRunnable implements Runnable {
        private JSONObject mObj = null;
        private String mPhoneNumber = null;
        private WeakReference<Activity> mReference = null;

        public BeforeInitBubbleRunnable(Activity ctx, String num, JSONObject obj) {
            this.mPhoneNumber = num;
            this.mReference = new WeakReference(ctx);
            this.mObj = obj;
        }

        public void run() {
            try {
                Activity activity = (Activity) this.mReference.get();
                if (!SmartSmsSdkUtil.activityIsFinish(activity)) {
                    DuoquBubbleViewManager.beforeInitBubbleView(activity, this.mPhoneNumber, this.mObj);
                    this.mReference = null;
                    this.mPhoneNumber = null;
                    this.mObj = null;
                }
            } catch (Throwable e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("BeforeInitBubbleRunnable run error: " + e.getMessage(), e);
            }
        }
    }

    public static class ComposeManagerHandler extends Handler {
        private WeakReference<SmartSmsComposeManager> mReference = null;

        public ComposeManagerHandler(SmartSmsComposeManager smartSmsComposeManager) {
            this.mReference = new WeakReference(smartSmsComposeManager);
        }

        public void handleMessage(Message msg) {
            try {
                SmartSmsComposeManager smartSmsComposeManager = (SmartSmsComposeManager) this.mReference.get();
                if (smartSmsComposeManager != null && !SmartSmsSdkUtil.activityIsFinish(smartSmsComposeManager.mCtx)) {
                    smartSmsComposeManager.bindMenuView(msg.getData().getString("JSON"), smartSmsComposeManager.mCtx);
                    super.handleMessage(msg);
                }
            } catch (Throwable e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("ComposeManagerHandler handleMessage error:" + e.getMessage(), e);
            }
        }

        public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
            try {
                SmartSmsComposeManager smartSmsComposeManager = (SmartSmsComposeManager) this.mReference.get();
                if (smartSmsComposeManager == null || SmartSmsSdkUtil.activityIsFinish(smartSmsComposeManager.mCtx)) {
                    return false;
                }
            } catch (Throwable e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("ComposeManagerHandler sendMessageAtTime error:" + e.getMessage(), e);
            }
            return super.sendMessageAtTime(msg, uptimeMillis);
        }
    }

    public View getMenuRootView() {
        return this.mMenuRootLayout;
    }

    public LinearLayout getButtonToSmartMenu() {
        return this.mButtonToSmartMenu;
    }

    public SmartSmsComposeManager(ISmartSmsUIHolder iSmartSmsUIHolder) {
        this.mISmartSmsUIHolder = iSmartSmsUIHolder;
        this.mCtx = iSmartSmsUIHolder.getActivityContext();
        this.mHandler = new ComposeManagerHandler(this);
    }

    public void queryMenu(ISmartSmsUIHolder iSmartSmsUIHolder, String recipientNumber, short conversationType) {
        if (StringUtils.isPhoneNumber(recipientNumber)) {
            this.mIsNotifyComposeMessage = false;
            this.mIsLoad = true;
            return;
        }
        ContentUtil.observerFontSize();
        ContentUtil.observerTheme();
        reloadContact(recipientNumber);
        if (recipientNumber == null) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsComposeManager queryMenu recipientNumber is null", null);
        } else if (this.mCtx != null) {
            SmartSmsSdkUtil.setBubbleActivityResumePhoneNum(iSmartSmsUIHolder.hashCode(), recipientNumber);
            if (this.mNumber == null || !this.mNumber.equals(recipientNumber)) {
                this.mNumber = recipientNumber;
                this.mIsLoad = false;
                queryMenu(recipientNumber, this.mCtx, iSmartSmsUIHolder, conversationType);
            }
        }
    }

    public boolean onSmartSmsEvent(short eventType) {
        switch (eventType) {
            case (short) 5:
                ContentUtil.setViewVisibility(this.mMenuRootLayout, 0);
                break;
        }
        return false;
    }

    public boolean getIsNotifyComposeMessage() {
        return this.mIsNotifyComposeMessage;
    }

    private void initSmartSmsMenu() {
        if (this.mISmartSmsUIHolder == null) {
            MLog.w("XIAOYUAN", SmartSmsComposeManager.class.getName() + "  initSmartSmsMenuManager iSmartSmsUIHolder is null");
        } else if (this.mCtx == null) {
            MLog.w("XIAOYUAN", SmartSmsComposeManager.class.getName() + "  initSmartSmsMenuManager iSmartSmsUIHolder.getActivityContext() is null");
        } else {
            this.mIsUsedTheme = ResEx.init(this.mCtx).isUseThemeBackground(this.mCtx);
            this.mLayoutInflater = (LayoutInflater) this.mCtx.getSystemService("layout_inflater");
            initBottomMenuView(this.mCtx, this.mISmartSmsUIHolder);
            this.mIsInitMenuView = true;
        }
    }

    private void initBottomMenuView(final Activity ctx, final ISmartSmsUIHolder iSmartSmsUIHolder) {
        if (this.mMenuRootLayout == null) {
            this.mMenuRootStub = (ViewStub) iSmartSmsUIHolder.findViewById(R.id.duoqu_menu_layout_stub);
            if (this.mMenuRootStub == null) {
                SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsMenuManager initBottomMenu menuRootStub is null.", null);
                return;
            }
            this.mMenuRootLayout = this.mMenuRootStub.inflate();
        }
        if (this.mMenuRootLayout == null) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsMenuManager initBottomMenu mMenuRootLayout is null.", null);
            return;
        }
        this.mButtonToSmartMenu = (LinearLayout) iSmartSmsUIHolder.findViewById(R.id.duoqu_button_menu);
        this.mSmartMenuLayout = (RelativeLayout) this.mMenuRootLayout.findViewById(R.id.layout_bottom_menu);
        this.mSmartMenuContent = (LinearLayout) this.mMenuRootLayout.findViewById(R.id.layout_menu);
        this.mButtonToEditMenu = (LinearLayout) this.mMenuRootLayout.findViewById(R.id.layout_exchange);
        this.mButtonToEditMenu.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SmartSmsComposeManager.this.toEditMenu(iSmartSmsUIHolder, true);
                StatisticalHelper.incrementReportCount(ctx, 2265);
            }
        });
        if (this.mIsUsedTheme) {
            this.mButtonToEditMenu.setBackgroundResource(R.color.duoqu_all_transparent);
        }
    }

    private boolean isSameOperators() {
        boolean isSameOperator = true;
        try {
            IccidLocationUtil.changeIccidAreaCode(false);
            HashMap<String, String[]> iccidMap = IccidLocationUtil.getIccidAreaCodeMap();
            if (iccidMap == null || iccidMap.isEmpty() || iccidMap.size() < 2) {
                return false;
            }
            Set<Entry<String, String[]>> setEntery = ((HashMap) iccidMap.clone()).entrySet();
            for (Entry<String, String[]> entry : setEntery) {
                try {
                    if (this.mNumOperator != Integer.parseInt(((String[]) entry.getValue())[2])) {
                        isSameOperator = false;
                        break;
                    }
                } catch (Exception e) {
                    SmartSmsSdkUtil.smartSdkExceptionLog("isShowSelectSimPop error: " + e.getMessage(), e);
                }
            }
            if (isSameOperator) {
                if (setEntery.size() >= 2) {
                    return true;
                }
            }
            return false;
        } catch (Exception e2) {
            SmartSmsSdkUtil.smartSdkExceptionLog("isSameOperators error: " + e2.getMessage(), e2);
            return false;
        }
    }

    private void queryMenu(String recipientNumber, Activity ctx, ISmartSmsUIHolder iSmartSmsUIHolder, short conversationType) {
        if (!this.mIsLoad && !TextUtils.isEmpty(recipientNumber)) {
            final String str = recipientNumber;
            final Activity activity = ctx;
            final ISmartSmsUIHolder iSmartSmsUIHolder2 = iSmartSmsUIHolder;
            final short s = conversationType;
            new Thread() {
                public void run() {
                    try {
                        Process.setThreadPriority(-4);
                        SmartSmsComposeManager.this.mFormatNumber = MessageUtils.parseMmsAddress(str, true);
                        SmartSmsComposeManager.this.mNumOperator = ParseManager.getOperatorNumByPubNum(SmartSmsComposeManager.this.mFormatNumber);
                        SmartSmsComposeManager.this.mSelectedSimIndex = SimCardUtil.getDefaultSimCardIndex();
                        if (SimCardUtil.isChangeSimCard()) {
                            DuoquUtils.getSdkDoAction().simChange();
                        } else if (SimCardUtil.needUpdateAllIccidInfo(SmartSmsComposeManager.this.mCtx)) {
                            SimCardUtil.loadLocation();
                        }
                        final boolean isSameOperators = SmartSmsComposeManager.this.isSameOperators();
                        final Activity activity = activity;
                        final ISmartSmsUIHolder iSmartSmsUIHolder = iSmartSmsUIHolder2;
                        final String str = str;
                        final short s = s;
                        SdkCallBack callBack = new SdkCallBack() {
                            public void execute(Object... dataArr) {
                                try {
                                    if (!SmartSmsSdkUtil.activityIsFinish(activity) && SmartSmsComposeManager.this.isValidData(dataArr)) {
                                        final String menuJson = dataArr[0].toString();
                                        if (SmartSmsComposeManager.this.mIsInitMenuView) {
                                            SmartSmsComposeManager.this.reShowMenuInHandle(menuJson);
                                        } else {
                                            Activity activity = activity;
                                            final Activity activity2 = activity;
                                            final ISmartSmsUIHolder iSmartSmsUIHolder = iSmartSmsUIHolder;
                                            final String str = str;
                                            final short s = s;
                                            activity.runOnUiThread(new Runnable() {
                                                public void run() {
                                                    SmartSmsComposeManager.this.bindMenu(activity2, iSmartSmsUIHolder, str, menuJson, 0, s);
                                                }
                                            });
                                            if (isSameOperators) {
                                                SmartSmsComposeManager.this.querySameOperatorsMenuData(menuJson);
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsComponseManager queryMenu callBack error: " + e.getMessage(), e);
                                }
                            }
                        };
                        if (isSameOperators) {
                            Map<String, String> extend = new HashMap();
                            extend.put(SmartSmsComposeManager.LOCATION_KEY, SmartSmsComposeManager.LOCATION_VALUE_CN);
                            ParseManager.queryMenuByPhoneNum(SmartSmsComposeManager.this.mCtx, SmartSmsComposeManager.this.mFormatNumber, 1, null, extend, callBack);
                            return;
                        }
                        ParseManager.queryMenuByPhoneNum(SmartSmsComposeManager.this.mCtx, SmartSmsComposeManager.this.mFormatNumber, 1, null, null, callBack);
                    } catch (Throwable e) {
                        SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsComponseManager queryDoubleSimMenuByPhoneNum error: " + e.getMessage(), e);
                    }
                }
            }.start();
            loadBubbleData(recipientNumber);
        }
    }

    public void setSmartMenuClickListener(final ISmartSmsUIHolder iSmartSmsUIHolder) {
        this.mButtonToSmartMenu.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SmartSmsComposeManager.this.toSmartMenu(iSmartSmsUIHolder, true);
            }
        });
    }

    private void bindMenu(Activity ctx, final ISmartSmsUIHolder iSmartSmsUIHolder, String recipientNumber, String result, long start, short conversationType) {
        String json = result;
        if (TextUtils.isEmpty(result)) {
            this.mIsLoad = true;
            return;
        }
        try {
            if (new JSONArray(result).length() != 3) {
                this.mIsLoad = true;
                return;
            }
            initSmartSmsMenu();
            if (this.mMenuRootLayout == null || this.mButtonToSmartMenu == null || this.mSmartMenuContent == null || this.mLayoutInflater == null) {
                SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsComposeManager queryMenu onPostExecute mMenuRootLayout is null", null);
                return;
            }
            bindMenuView(result, ctx);
            if (iSmartSmsUIHolder.editorHasContent()) {
                iSmartSmsUIHolder.onSmartSmsEvent((short) 7);
                this.mMenuRootLayout.setVisibility(8);
            } else if (iSmartSmsUIHolder.isIntentHasSmsBody() || conversationType == (short) 1) {
                iSmartSmsUIHolder.onSmartSmsEvent((short) 1);
                this.mMenuRootLayout.setVisibility(8);
            } else {
                iSmartSmsUIHolder.onSmartSmsEvent((short) 6);
                this.mMenuRootLayout.setVisibility(0);
            }
            this.mButtonToSmartMenu.setVisibility(0);
            this.mButtonToSmartMenu.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    SmartSmsComposeManager.this.toSmartMenu(iSmartSmsUIHolder, true);
                }
            });
            this.mIsLoad = true;
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsMenuManager queryMenu onPostExecute  error." + e.getMessage(), e);
            this.mIsLoad = false;
        }
    }

    public void toSmartMenu(ISmartSmsUIHolder iSmartSmsUIHolder, boolean isShowAnim) {
        iSmartSmsUIHolder.onSmartSmsEvent((short) 4);
        if (this.mMenuRootLayout != null) {
            if (isShowAnim) {
                this.mMenuRootLayout.setAnimation(AnimationUtils.loadAnimation(this.mCtx, R.anim.translate_fade_in));
            } else {
                this.mMenuRootLayout.setAnimation(null);
            }
            this.mMenuRootLayout.setVisibility(0);
        }
    }

    public void toEditMenu(ISmartSmsUIHolder iSmartSmsUIHolder, boolean isShowAnim) {
        if (this.mMenuRootLayout != null && this.mButtonToSmartMenu != null) {
            if (isShowAnim) {
                Animation showAnim = AnimationUtils.loadAnimation(this.mCtx, R.anim.translate_fade_in);
                this.mMenuRootLayout.setAnimation(AnimationUtils.loadAnimation(this.mCtx, R.anim.translate));
                this.mButtonToSmartMenu.setAnimation(showAnim);
                iSmartSmsUIHolder.onSmartSmsEvent((short) 1);
            } else {
                this.mMenuRootLayout.setAnimation(null);
                this.mButtonToSmartMenu.setAnimation(null);
                iSmartSmsUIHolder.onSmartSmsEvent((short) 7);
            }
            this.mMenuRootLayout.setVisibility(8);
            this.mButtonToSmartMenu.setVisibility(0);
        }
    }

    private void loadBubbleData(final String recipientNumber) {
        if (this.mCtx != null) {
            new Thread() {
                public void run() {
                    try {
                        SmartSmsComposeManager.this.mIsNotifyComposeMessage = ParseManager.isEnterpriseSms(SmartSmsComposeManager.this.mCtx, recipientNumber, null, null);
                        ParseBubbleManager.loadBubbleDataByPhoneNum(recipientNumber, true);
                        SmartSmsComposeManager.this.mHandler.postDelayed(new BeforeInitBubbleRunnable(SmartSmsComposeManager.this.mCtx, recipientNumber, PhoneSmsParseManager.findObjectByPhone(recipientNumber)), 2000);
                    } catch (Throwable e) {
                        SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsMenuManager bindMenuView onClick  error.", e);
                    }
                }
            }.start();
        }
    }

    private void bindMenuView(String json, Activity ctx) throws JSONException {
        if ((!StringUtils.isNull(json) && json.equals(this.mMenuJsonData)) || this.mMenuRootLayout == null) {
            return;
        }
        if (StringUtils.isNull(json) || this.mSmartMenuContent == null) {
            ContentUtil.setViewVisibility(this.mMenuRootLayout, 8);
            this.mMenuJsonData = null;
            return;
        }
        this.mMenuJsonData = json;
        JSONArray jsonCustomMenu = new JSONArray(json);
        if (jsonCustomMenu.length() == 3) {
            this.mSmartMenuContent.removeAllViews();
            if (this.mPopupWindowCustommenu != null) {
                this.mPopupWindowCustommenu.dismiss();
            }
            for (int i = 0; i < jsonCustomMenu.length(); i++) {
                final JSONObject ob = jsonCustomMenu.getJSONObject(i);
                final LinearLayout layout = (LinearLayout) this.mLayoutInflater.inflate(R.layout.duoqu_item_custommenu, null);
                layout.setLayoutParams(new LayoutParams(-1, -1, ContentUtil.FONT_SIZE_NORMAL));
                final String menuName = ob.getString("name");
                ((TextView) layout.findViewById(R.id.duoqu_custommenu_name)).setText(menuName);
                if (this.mIsUsedTheme) {
                    layout.setBackgroundResource(R.drawable.duoqu_item_menu_select);
                }
                final Activity activity = ctx;
                layout.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        Object obj = layout.getTag();
                        if (obj == null || SmartSmsComposeManager.this.mPopupWindowCustommenu == null || SmartSmsComposeManager.this.mPopupWindowCustommenu != ((PopMenus) obj)) {
                            final JSONObject jSONObject = ob;
                            final Activity activity = activity;
                            final LinearLayout linearLayout = layout;
                            final String str = menuName;
                            new XyCallBack() {
                                public void execute(Object... backData) {
                                    if (!SmartSmsComposeManager.this.isSameOperators()) {
                                        SmartSmsComposeManager.this.mSelectedSimIndex = SimCardUtil.getIndexbyOperator(SmartSmsComposeManager.this.mNumOperator, SmartSmsComposeManager.this.mSelectedSimIndex);
                                    }
                                    try {
                                        if (!jSONObject.has(SmartSmsComposeManager.SECONDMENU) || jSONObject.getJSONArray(SmartSmsComposeManager.SECONDMENU).length() <= 0) {
                                            Map<String, String> extend = new HashMap();
                                            extend.put("simIndex", String.valueOf(SmartSmsComposeManager.this.mSelectedSimIndex));
                                            ParseManager.doAction(activity, jSONObject.get(SmartSmsComposeManager.ACTION_DATA).toString(), extend);
                                            PopMenus.menuActionReport(SmartSmsComposeManager.this.mFormatNumber, str);
                                            return;
                                        }
                                        if (SmartSmsComposeManager.this.mPopupWindowCustommenu != null) {
                                            SmartSmsComposeManager.this.mPopupWindowCustommenu.dismiss();
                                        }
                                        SmartSmsComposeManager.this.mExtraMenuDataMap.put(IccidInfoManager.NUM, SmartSmsComposeManager.this.mFormatNumber);
                                        SmartSmsComposeManager.this.mPopupWindowCustommenu = new PopMenus(activity, jSONObject.getJSONArray(SmartSmsComposeManager.SECONDMENU), 0, 0, SmartSmsComposeManager.this.mSelectedSimIndex, SmartSmsComposeManager.this.mExtraMenuDataMap);
                                        SmartSmsComposeManager.this.mPopupWindowCustommenu.showAtLocation(linearLayout);
                                        linearLayout.setTag(SmartSmsComposeManager.this.mPopupWindowCustommenu);
                                    } catch (Throwable e) {
                                        SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsMenuManager bindMenuView onClick  error." + e.getMessage(), e);
                                    }
                                }
                            }.execute(new Object[0]);
                            return;
                        }
                        SmartSmsComposeManager.this.mPopupWindowCustommenu = null;
                    }
                });
                this.mSmartMenuContent.addView(layout);
            }
        } else {
            this.mMenuRootLayout.setVisibility(8);
        }
    }

    private boolean isValidData(Object... dataArr) {
        if (dataArr == null || dataArr.length == 0 || dataArr[0] == null || !dataArr[0].toString().contains(ACTION_DATA)) {
            return false;
        }
        return true;
    }

    private void reShowMenuInHandle(String json) {
        Message message = this.mHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString("JSON", json);
        message.setData(bundle);
        this.mHandler.sendMessage(message);
    }

    public void hideSmartsmsMenu() {
        ContentUtil.setViewVisibility(this.mSmartMenuLayout, 8);
    }

    @SuppressLint({"NewApi"})
    public void addNeedRefreshSmartBubbleItem(SmartSmsBubbleManager bubbleItem) {
        if (bubbleItem != null) {
            this.mNeedRefreshBubbleItem.offerLast(bubbleItem);
            if (this.mNeedRefreshBubbleItem.size() > 10) {
                this.mNeedRefreshBubbleItem.removeFirst();
            }
        }
    }

    public void notifyCheckRefresh(final BaseAdapter adpater) {
        new AsyncTask() {
            protected Boolean doInBackground(Object... arg0) {
                return Boolean.valueOf(SmartSmsComposeManager.this.isNeedRefreshSmartBubbleItem());
            }

            protected void onPostExecute(Object result) {
                if (adpater != null && result != null && ((Boolean) result).booleanValue()) {
                    adpater.notifyDataSetChanged();
                }
            }
        }.execute(new Object[0]);
    }

    private boolean isNeedRefreshSmartBubbleItem() {
        boolean res = false;
        int cnt = 0;
        while (!this.mNeedRefreshBubbleItem.isEmpty()) {
            if (!((SmartSmsBubbleManager) this.mNeedRefreshBubbleItem.removeLast()).isNeedRefresh()) {
                cnt++;
                if (cnt > 10) {
                    break;
                }
            }
            res = true;
            break;
        }
        this.mNeedRefreshBubbleItem.clear();
        return res;
    }

    public boolean isShowMenu() {
        if (this.mPopupWindowCustommenu == null) {
            return false;
        }
        boolean isShow = this.mPopupWindowCustommenu.isShow();
        if (isShow) {
            this.mPopupWindowCustommenu.dismiss();
        }
        return isShow;
    }

    public void reShowMenu() {
        if (this.mPopupWindowCustommenu != null && this.mPopupWindowCustommenu.isShow() && this.mButtonToEditMenu != null) {
            this.mButtonToEditMenu.postDelayed(new Runnable() {
                public void run() {
                    SmartSmsComposeManager.this.mPopupWindowCustommenu.dismiss();
                    SmartSmsComposeManager.this.mPopupWindowCustommenu.showPopupAccordingParentView();
                }
            }, 100);
        }
    }

    private void querySameOperatorsMenuData(final String json) {
        if (!StringUtils.isNull(json)) {
            try {
                ParseManager.queryAllSimCardTrafficAndChargeActionData(this.mCtx, this.mFormatNumber, new SdkCallBack() {
                    public void execute(Object... dataArr) {
                        if (dataArr != null && dataArr.length >= 3 && dataArr[2] != null) {
                            try {
                                SmartSmsComposeManager.this.mExtraMenuDataMap.put(dataArr[1].toString(), dataArr[2].toString());
                                if (SmartSmsComposeManager.this.mExtraMenuDataMap.size() > 1) {
                                    String menuData = ParseManager.addQueryTrafficAndChargeToMenuData(json, SmartSmsComposeManager.this.mExtraMenuDataMap);
                                    if (!StringUtils.isNull(menuData) && menuData.contains(SmartSmsComposeManager.ACTION_DATA)) {
                                        SmartSmsComposeManager.this.reShowMenuInHandle(menuData);
                                    }
                                }
                            } catch (Exception e) {
                                SmartSmsSdkUtil.smartSdkExceptionLog("querySameOperatorsMenuData error: " + e.getMessage(), e);
                            }
                        }
                    }
                });
            } catch (Throwable e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("querySameOperatorsMenuData error: " + e.getMessage(), e);
            }
        }
    }

    private void reloadContact(String recipientNumber) {
        if (!StringUtils.isNull(recipientNumber)) {
            Contact contact = Contact.get(recipientNumber, false);
            if (contact != null && StringUtils.isNull(contact.getXiaoyuanPhotoUri())) {
                contact.reload();
            }
        }
    }
}
