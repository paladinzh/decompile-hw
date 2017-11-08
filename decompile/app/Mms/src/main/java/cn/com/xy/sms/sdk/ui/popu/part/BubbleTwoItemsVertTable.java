package cn.com.xy.sms.sdk.ui.popu.part;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.widget.DuoquFieldShapeItem;
import com.google.android.gms.R;
import org.json.JSONArray;
import org.json.JSONObject;

public class BubbleTwoItemsVertTable extends UIPart {
    private static String COLLAPSE_STR = ContentUtil.COLLAPSE;
    private static int DEFAULT_SHOW_ITEMS = 0;
    private static String DEFAULT_SHOW_ITEMS_STR = "default_num_of_items";
    private static String EXPAND_STR = ContentUtil.EXPAND;
    public static final String KEY_EXPANDED = "expanded";
    private static int MAX_SHOW_ITEMS = 0;
    private static String MAX_SHOW_ITEMS_STR = "maximum_num_of_items";
    private static final String TABLE_KEY = "duoqu_table_data_vert";
    private DuoquFieldShapeItem mContentListView;
    private TextView mExpand;
    private ImageView mExpandImage;
    private LinearLayout mLinearLayout;
    private TextView mMoreNotice;

    public BubbleTwoItemsVertTable(Activity mContext, BusinessSmsMessage message, XyCallBack callback, int layoutId, ViewGroup root, int partId) {
        super(mContext, message, callback, layoutId, root, partId);
    }

    public void initUi() {
        this.mContentListView = (DuoquFieldShapeItem) this.mView.findViewById(R.id.duoqu_two_vert_list);
        this.mExpand = (TextView) this.mView.findViewById(R.id.duoqu_two_items_table_expand);
        this.mExpandImage = (ImageView) this.mView.findViewById(R.id.duoqu_two_items_table_expand_icon);
        this.mMoreNotice = (TextView) this.mView.findViewById(R.id.duoqu_two_items_table_more);
        this.mLinearLayout = (LinearLayout) this.mView.findViewById(R.id.duoqu_two_items_table_li_expand);
        this.mLinearLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                BubbleTwoItemsVertTable.this.setOnClickEvent();
            }
        });
        this.mExpand.setTextSize(0, (float) ContentUtil.getVerticalTableTitleTextSize());
    }

    public void setContent(BusinessSmsMessage message, boolean isRebind) throws Exception {
        this.mMessage = message;
        if (message != null) {
            try {
                int currentDataSize;
                boolean expanded;
                int lineSize;
                if (message.getValue(DEFAULT_SHOW_ITEMS_STR) == null || message.getValue(MAX_SHOW_ITEMS_STR) == null) {
                    setDefaultShowItems(2);
                    setMaxShowItems(6);
                    message.getTableDataSize(TABLE_KEY);
                    currentDataSize = getDataItemSize(message);
                    expanded = message.bubbleJsonObj.optBoolean(KEY_EXPANDED, false);
                    if (expanded) {
                        lineSize = getLineSize(message, DEFAULT_SHOW_ITEMS);
                    } else {
                        lineSize = getLineSize(message, MAX_SHOW_ITEMS);
                    }
                    this.mContentListView.setContentList(message, lineSize, TABLE_KEY, isRebind);
                    if (message.getValue("m_special_layout") != null) {
                        currentDataSize = DEFAULT_SHOW_ITEMS;
                    }
                    showOrHideMoreInfo(message, expanded, currentDataSize, MAX_SHOW_ITEMS, DEFAULT_SHOW_ITEMS);
                }
                setDefaultShowItems(Integer.parseInt(String.valueOf(message.getValue(DEFAULT_SHOW_ITEMS_STR))));
                setMaxShowItems(Integer.parseInt(String.valueOf(message.getValue(MAX_SHOW_ITEMS_STR))));
                message.getTableDataSize(TABLE_KEY);
                currentDataSize = getDataItemSize(message);
                expanded = message.bubbleJsonObj.optBoolean(KEY_EXPANDED, false);
                if (expanded) {
                    lineSize = getLineSize(message, DEFAULT_SHOW_ITEMS);
                } else {
                    lineSize = getLineSize(message, MAX_SHOW_ITEMS);
                }
                this.mContentListView.setContentList(message, lineSize, TABLE_KEY, isRebind);
                if (message.getValue("m_special_layout") != null) {
                    currentDataSize = DEFAULT_SHOW_ITEMS;
                }
                showOrHideMoreInfo(message, expanded, currentDataSize, MAX_SHOW_ITEMS, DEFAULT_SHOW_ITEMS);
            } catch (Throwable th) {
                setDefaultShowItems(2);
                setMaxShowItems(6);
            }
        }
    }

    public void showOrHideMoreInfo(BusinessSmsMessage message, boolean expand, int size, int maxShowItems, int defaultShowItems) {
        if (expand) {
            this.mLinearLayout.setVisibility(0);
            this.mExpand.setText(COLLAPSE_STR);
            this.mExpand.setVisibility(0);
            this.mExpandImage.setVisibility(0);
            this.mExpandImage.setBackgroundResource(R.drawable.cs_common_btn_default_pressed_emui);
            if (size > maxShowItems) {
                this.mMoreNotice.setVisibility(0);
                return;
            } else {
                this.mMoreNotice.setVisibility(8);
                return;
            }
        }
        this.mMoreNotice.setVisibility(8);
        if (size > defaultShowItems) {
            this.mLinearLayout.setVisibility(0);
            this.mExpand.setText(EXPAND_STR);
            this.mExpand.setVisibility(0);
            this.mExpandImage.setBackgroundResource(R.drawable.cs_icon_one);
            this.mExpandImage.setVisibility(0);
            return;
        }
        this.mLinearLayout.setVisibility(8);
        this.mExpand.setVisibility(8);
        this.mExpandImage.setVisibility(8);
        this.mMoreNotice.setVisibility(8);
    }

    public void setOnClickEvent() {
        boolean z = false;
        boolean expanded = this.mMessage.bubbleJsonObj.optBoolean(KEY_EXPANDED, false);
        try {
            JSONObject jSONObject = this.mMessage.bubbleJsonObj;
            String str = KEY_EXPANDED;
            if (!expanded) {
                z = true;
            }
            jSONObject.put(str, z);
            setContent(this.mMessage, true);
            if (this.mBasePopupView != null) {
                this.mBasePopupView.callSmartSmsEvent(1, null);
            }
        } catch (Exception e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(e.getMessage(), e);
        }
    }

    public int getDataItemSize(BusinessSmsMessage message) {
        JSONArray arry = message.bubbleJsonObj.optJSONArray(TABLE_KEY);
        int size = 0;
        if (arry != null && arry.length() > 0) {
            for (int i = 0; i < arry.length(); i++) {
                JSONObject object = arry.optJSONObject(i);
                if (object != null) {
                    if (object.has("t1")) {
                        size++;
                    }
                    if (object.has("t3")) {
                        size++;
                    }
                }
            }
        }
        return size;
    }

    public int getLineSize(BusinessSmsMessage message, int maxItemSize) {
        JSONArray arry = message.bubbleJsonObj.optJSONArray(TABLE_KEY);
        int size = 0;
        int line = -1;
        if (arry != null && arry.length() > 0) {
            for (int i = 0; i < arry.length(); i++) {
                JSONObject object = arry.optJSONObject(i);
                if (object != null) {
                    line = i;
                    if (object.has("t1")) {
                        size++;
                    }
                    if (object.has("t3")) {
                        size++;
                    }
                    if (size >= maxItemSize) {
                        break;
                    }
                }
            }
        }
        return line + 1;
    }

    public void destroy() {
        super.destroy();
    }

    public static void setDefaultShowItems(int defaultShowItems) {
        DEFAULT_SHOW_ITEMS = defaultShowItems;
    }

    public static void setMaxShowItems(int maxShowItems) {
        MAX_SHOW_ITEMS = maxShowItems;
    }
}
