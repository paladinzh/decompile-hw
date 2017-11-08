package cn.com.xy.sms.sdk.ui.popu.util;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.widget.AdapterDataSource;
import cn.com.xy.sms.sdk.ui.popu.widget.DuoquDialogSelected;
import cn.com.xy.sms.sdk.ui.popu.widget.DuoquSourceAdapterDataSource;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.SdkCallBack;
import com.google.android.gms.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TravelDataUtil {
    private String mDataIndexKey = null;
    private String mInterfaceDataKeyStart = null;
    private String mQueryTimeKeyStart = null;
    private String mViewContentKeyStart = null;

    public String getDataIndexKey() {
        return this.mDataIndexKey;
    }

    public String getViewContentKeyStart() {
        return this.mViewContentKeyStart;
    }

    public String getInterfaceDataKeyStart() {
        return this.mInterfaceDataKeyStart;
    }

    public String getQueryTimeKeyStart() {
        return this.mQueryTimeKeyStart;
    }

    public TravelDataUtil(String dataIndexKey, String viewContentKeyStart, String interfaceDataKeyStart, String queryTimeKeyStart) {
        this.mDataIndexKey = dataIndexKey;
        this.mViewContentKeyStart = viewContentKeyStart;
        this.mInterfaceDataKeyStart = interfaceDataKeyStart;
        this.mQueryTimeKeyStart = queryTimeKeyStart;
    }

    public JSONObject getViewContentData(BusinessSmsMessage smsMessage) {
        if (ContentUtil.bubbleDataIsNull(smsMessage) || getViewContentDataArray(smsMessage) == null) {
            return null;
        }
        try {
            return (JSONObject) getViewContentDataArray(smsMessage).get(getDefaultSelectedIndex(smsMessage));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONArray getViewContentDataArray(BusinessSmsMessage smsMessage) {
        if (ContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optJSONArray(getViewContentKeyStart());
    }

    public String getViewContentKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, this.mViewContentKeyStart);
    }

    public String getInterfaceDataKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, this.mInterfaceDataKeyStart);
    }

    public long getQueryTime(BusinessSmsMessage smsMessage) {
        if (ContentUtil.bubbleDataIsNull(smsMessage)) {
            return 0;
        }
        return smsMessage.bubbleJsonObj.optLong(getQueryTimeKey(smsMessage));
    }

    public String getQueryTimeKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, this.mQueryTimeKeyStart);
    }

    public String getDataIndex(BusinessSmsMessage smsMessage) {
        if (ContentUtil.bubbleDataIsNull(smsMessage)) {
            return "0";
        }
        String dataIndex = smsMessage.bubbleJsonObj.optString(this.mDataIndexKey);
        if (StringUtils.isNull(dataIndex)) {
            dataIndex = "0";
        }
        return dataIndex;
    }

    public int getDefaultSelectedIndex(BusinessSmsMessage smsMessage) {
        int defaultSelectedIndex = 0;
        try {
            defaultSelectedIndex = Integer.parseInt(getDataIndex(smsMessage));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return defaultSelectedIndex;
    }

    public boolean hasInterfaceData(BusinessSmsMessage smsMessage) {
        if (ContentUtil.bubbleDataIsNull(smsMessage)) {
            return false;
        }
        return smsMessage.bubbleJsonObj.has(getInterfaceDataKey(smsMessage));
    }

    public static boolean hasValue(TextView textView, String noDataValue) {
        boolean z = false;
        if (textView == null) {
            return false;
        }
        String value = textView.getText().toString();
        if (!(StringUtils.isNull(value) || value.equals(noDataValue))) {
            z = true;
        }
        return z;
    }

    public static void setViewValue(String value, TextView textView, String lostValueShowText, ImageView lostValueShowImage) {
        int i;
        int i2 = 8;
        boolean timeIsNull = StringUtils.isNull(value);
        if (timeIsNull) {
            i = 0;
        } else {
            i = 8;
        }
        ContentUtil.setViewVisibility(lostValueShowImage, i);
        if (!timeIsNull) {
            i2 = 0;
        }
        ContentUtil.setViewVisibility(textView, i2);
        ContentUtil.setText(textView, value, lostValueShowText);
    }

    public static void setPopupDialogClickListener(Context ctx, AdapterDataSource adapterDataSource, DuoquDialogSelected dialogSelected, String dialogTitle, final SdkCallBack callBack, View... bindListenerViews) {
        if (ctx != null && dialogSelected != null && bindListenerViews != null && bindListenerViews.length != 0 && adapterDataSource != null && adapterDataSource.getDataSrouce() != null) {
            Resources res = ctx.getResources();
            ContentUtil.setOnClickListener(SelectListDialogUtil.showSelectListDialogClickListener(ctx, dialogTitle, res.getString(R.string.duoqu_confirm), res.getString(R.string.duoqu_cancel), adapterDataSource, dialogSelected, new SdkCallBack() {
                public void execute(Object... obj) {
                    ContentUtil.callBackExecute(callBack, obj);
                }
            }), bindListenerViews);
        }
    }

    public static void setPopupDialogClickListener(Context ctx, final BusinessSmsMessage smsMessage, String dialogTitle, AdapterDataSource adapterDataSource, final TravelDataUtil dataUtil, final DuoquDialogSelected dialogSelected, final SdkCallBack callBack, View... bindListenerViews) {
        if (ctx != null && smsMessage != null && adapterDataSource != null && adapterDataSource.getDataSrouce() != null && adapterDataSource.getDataSrouce().length() >= 2 && dataUtil != null && dialogSelected != null && bindListenerViews != null && bindListenerViews.length != 0) {
            dialogSelected.setSelectIndex(dataUtil.getDefaultSelectedIndex(smsMessage));
            setPopupDialogClickListener(ctx, adapterDataSource, dialogSelected, dialogTitle, new SdkCallBack() {
                public void execute(Object... obj) {
                    if (!queryFail(obj)) {
                        int selectedIndex = ((Integer) obj[1]).intValue();
                        if (dialogSelected.getSelectIndex() != selectedIndex) {
                            dialogSelected.setSelectIndex(selectedIndex);
                            ContentUtil.saveSelectedIndex(smsMessage, dataUtil.getDataIndexKey(), obj[0].optString(DuoquSourceAdapterDataSource.INDEX_KEY));
                            ContentUtil.callBackExecute(callBack, obj);
                        }
                    }
                }

                private boolean queryFail(Object... obj) {
                    if (obj == null || obj.length < 3 || !(obj[0] instanceof JSONObject) || !(obj[1] instanceof Integer) || obj[2] == null) {
                        return true;
                    }
                    return false;
                }
            }, bindListenerViews);
        }
    }

    public void resetBackgroundResource(ViewGroup root, BusinessSmsMessage smsMessage) {
        if (root != null && !ContentUtil.bubbleDataIsNull(smsMessage)) {
            int bgResourceId = R.drawable.duoqu_pop_bg_gray;
            if (smsMessage.bubbleJsonObj.has(this.mViewContentKeyStart + "1")) {
                bgResourceId = R.drawable.duoqu_pop_bg_double;
            }
            try {
                Object currentBgResourceId = root.getTag(R.id.tag_bg_resource);
                if (currentBgResourceId != null && ((Integer) currentBgResourceId).intValue() == bgResourceId) {
                    return;
                }
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
            root.setTag(R.id.tag_bg_resource, Integer.valueOf(bgResourceId));
            root.setBackgroundResource(bgResourceId);
        }
    }

    public static String getDataByKey(JSONObject viewContentData, String key, String... replaceToEmpty) {
        if (viewContentData == null || StringUtils.isNull(key)) {
            return null;
        }
        String value = viewContentData.optString(key);
        if (StringUtils.isNull(value)) {
            return null;
        }
        if (replaceToEmpty != null) {
            for (String replace : replaceToEmpty) {
                value = value.replace(replace, "");
            }
        }
        return value.trim();
    }

    public boolean dataBelongCurrentMsg(BusinessSmsMessage smsMessage, Object... obj) {
        boolean z = false;
        if (smsMessage == null || obj == null || obj.length < 1) {
            return false;
        }
        String callbackMsgId = obj[0].toString();
        String currentMsgId = String.valueOf(smsMessage.getSmsId());
        if (!StringUtils.isNull(currentMsgId)) {
            z = currentMsgId.equals(callbackMsgId);
        }
        return z;
    }

    public String getKey(BusinessSmsMessage smsMessage, String keyStart) {
        return keyStart + getDataIndex(smsMessage);
    }
}
