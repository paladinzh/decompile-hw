package cn.com.xy.sms.sdk.ui.menu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseManager;
import com.android.mms.MmsApp;
import com.google.android.gms.R;
import com.huawei.mms.util.StatisticalHelper;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PopMenus {
    private static final String SELECT_SIM_CARD = "selectSimCard";
    private static final String SIM_CARD = ContentUtil.getResourceString(Constant.getContext(), R.string.duoqu_sim_card);
    private static final int SIM_CARD_1_INDEX = 0;
    private static final int SIM_CARD_2_INDEX = 1;
    private View mContainerView;
    private Activity mContext;
    private Map<String, String> mExtraMenuDataMap = null;
    private int mHeight;
    private boolean mIsShow = false;
    private JSONArray mJsonArray;
    private LinearLayout mListView;
    private View mParentView = null;
    private PopupWindow mPopupWindow;
    private int mSimIndex = 0;
    private int mWidth;

    @SuppressLint({"ResourceAsColor"})
    public PopMenus(Activity context, JSONArray jsonArray, int width, int height, int simIndex, Map<String, String> extraMenuDataMap) {
        int i = -2;
        this.mContext = context;
        this.mJsonArray = jsonArray;
        this.mWidth = width;
        this.mHeight = height;
        this.mSimIndex = simIndex;
        this.mExtraMenuDataMap = extraMenuDataMap;
        this.mContainerView = LayoutInflater.from(context).inflate(R.layout.duoqu_popmenus, null);
        this.mContainerView.setLayoutParams(new LayoutParams(-2, -1, ContentUtil.FONT_SIZE_NORMAL));
        this.mListView = (LinearLayout) this.mContainerView.findViewById(R.id.layout_subcustommenu);
        try {
            setSubMenu();
        } catch (JSONException e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("PopMenus setSubMenu() error: " + e.getMessage(), e);
        }
        this.mListView.setFocusableInTouchMode(true);
        this.mListView.setFocusable(false);
        View view = this.mContainerView;
        int i2 = this.mWidth == 0 ? -2 : this.mWidth;
        if (this.mHeight != 0) {
            i = this.mHeight;
        }
        this.mPopupWindow = new PopupWindow(view, i2, i);
        this.mPopupWindow.setAnimationStyle(R.style.popwin_anim_style);
    }

    public void showAtLocation(View parent) {
        this.mParentView = parent;
        showPopupAccordingParentView();
    }

    public void showPopupAccordingParentView() {
        if (this.mParentView != null) {
            this.mPopupWindow.setBackgroundDrawable(new ColorDrawable());
            this.mContainerView.measure(0, 0);
            this.mPopupWindow.showAsDropDown(this.mParentView, (this.mParentView.getWidth() - this.mPopupWindow.getContentView().getMeasuredWidth()) / 2, (-this.mPopupWindow.getContentView().getMeasuredHeight()) - this.mParentView.getMeasuredHeight());
            this.mPopupWindow.setOutsideTouchable(true);
            this.mPopupWindow.setFocusable(false);
            this.mPopupWindow.update();
            this.mPopupWindow.setOnDismissListener(new OnDismissListener() {
                public void onDismiss() {
                    PopMenus.this.mIsShow = false;
                    if (PopMenus.this.mParentView != null) {
                        PopMenus.this.mParentView.postDelayed(new Runnable() {
                            public void run() {
                                PopMenus.this.mParentView.setTag(null);
                            }
                        }, 200);
                    }
                    PopMenus.this.mJsonArray = null;
                }
            });
            this.mIsShow = true;
        }
    }

    public void dismiss() {
        if (this.mPopupWindow != null) {
            this.mPopupWindow.dismiss();
        }
    }

    void setSubMenu() throws JSONException {
        this.mListView.removeAllViews();
        for (int i = 0; i < this.mJsonArray.length(); i++) {
            final JSONObject ob = this.mJsonArray.getJSONObject(i);
            LinearLayout layoutItem = (LinearLayout) ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.duoqu_pomenu_menuitem, null);
            this.mContainerView.setLayoutParams(new LayoutParams(-1, -1, ContentUtil.FONT_SIZE_NORMAL));
            layoutItem.setFocusable(true);
            TextView tv_funbtntitle = (TextView) layoutItem.findViewById(R.id.pop_item_textView);
            View pop_item_line = layoutItem.findViewById(R.id.pop_item_line);
            if (i + 1 == this.mJsonArray.length()) {
                pop_item_line.setVisibility(8);
            }
            final String buttonName = ob.getString("name");
            tv_funbtntitle.setText(buttonName);
            layoutItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    try {
                        if (ob.has(NumberInfo.TYPE_KEY) && PopMenus.SELECT_SIM_CARD.equalsIgnoreCase(ob.getString(NumberInfo.TYPE_KEY))) {
                            PopMenus.this.showSelectSimCardDialog(buttonName, ob.optString("actionType"));
                        } else {
                            PopMenus.this.doAction(ob);
                        }
                        PopMenus.this.dismiss();
                    } catch (Exception e) {
                        SmartSmsSdkUtil.smartSdkExceptionLog("setSubMenu() error: " + e.getMessage(), e);
                    }
                }
            });
            this.mListView.addView(layoutItem);
        }
        this.mListView.setVisibility(0);
    }

    private void showSelectSimCardDialog(String dialogTitle, final String actionType) {
        if (!StringUtils.isNull(actionType)) {
            Builder builder = new Builder(this.mContext);
            builder.setTitle(dialogTitle);
            builder.setItems(new String[]{getItemName(0), getItemName(1)}, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int index) {
                    try {
                        String actionData = (String) PopMenus.this.mExtraMenuDataMap.get(DuoquUtils.getSdkDoAction().getIccidBySimIndex(index));
                        if (!StringUtils.isNull(actionData)) {
                            JSONObject actionTypeJson = new JSONObject(actionData);
                            PopMenus.this.mSimIndex = index;
                            PopMenus.this.doAction(actionTypeJson.getJSONObject(actionType));
                        }
                    } catch (Exception e) {
                        SmartSmsSdkUtil.smartSdkExceptionLog("showSelectSimCardDialog error: " + e.getMessage(), e);
                    }
                }
            });
            builder.show();
        }
    }

    public boolean isShow() {
        return this.mIsShow;
    }

    private void doAction(JSONObject menuItemData) {
        try {
            Map<String, String> extend = new HashMap();
            extend.put("simIndex", String.valueOf(this.mSimIndex));
            ParseManager.doAction(this.mContext, menuItemData.getString("action_data"), extend);
            menuActionReport(getNum(), menuItemData.optString("name"));
        } catch (Exception e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("PopMenus doAction error: " + e.getMessage(), e);
        }
    }

    private static String getItemName(int simIndex) {
        String simCardName = SIM_CARD + (simIndex + 1);
        String phoneNumber = DuoquUtils.sdkAction.getPhoneNumberBySimIndex(simIndex);
        if (StringUtils.isNull(phoneNumber)) {
            return simCardName;
        }
        return simCardName + " (" + phoneNumber + ")";
    }

    private String getNum() {
        if (this.mExtraMenuDataMap == null || !this.mExtraMenuDataMap.containsKey(IccidInfoManager.NUM)) {
            return "";
        }
        return String.valueOf(this.mExtraMenuDataMap.get(IccidInfoManager.NUM));
    }

    public static void menuActionReport(String num, String menuName) {
        StatisticalHelper.reportEvent(MmsApp.getApplication(), 8001, num + "," + menuName);
    }
}
