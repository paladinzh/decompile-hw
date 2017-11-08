package cn.com.xy.sms.sdk.ui.popu.part;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.popupview.BasePopupView;
import cn.com.xy.sms.sdk.ui.popu.util.ViewManger;
import java.util.HashMap;
import java.util.Map;

public abstract class UIPart {
    public BasePopupView mBasePopupView;
    public XyCallBack mCallback;
    public Activity mContext;
    public HashMap<String, Object> mExtendParam = null;
    public BusinessSmsMessage mMessage;
    public View mView;

    public UIPart(Activity context, BusinessSmsMessage message, XyCallBack callback, int layoutId, ViewGroup root, int partId) {
        init(context, message, callback, layoutId, root);
    }

    void init(Activity context, BusinessSmsMessage message, XyCallBack callback, int layoutId, ViewGroup root) {
        this.mContext = context;
        this.mMessage = message;
        this.mCallback = callback;
        this.mView = ViewManger.createContextByLayoutId(this.mContext, layoutId, null);
    }

    public void build() throws Exception {
        initUi();
        initListener();
        if (this.mMessage.messageBody != null) {
            setContent(this.mMessage, false);
        }
    }

    public void executePopuCmd(byte popu_cmd) {
        if (this.mCallback != null) {
            this.mCallback.execute(Byte.valueOf(popu_cmd));
        }
    }

    public String getTitleNo() {
        return (String) this.mMessage.getValue("title_num");
    }

    public void destroy() {
        this.mView = null;
        this.mContext = null;
        this.mMessage = null;
        this.mCallback = null;
    }

    public void putParam(String key, Object val) {
        if (key != null && val != null) {
            if (this.mExtendParam == null) {
                this.mExtendParam = new HashMap();
            }
            this.mExtendParam.put(key, val);
        }
    }

    public Object getParam(String key) {
        if (this.mExtendParam != null) {
            return this.mExtendParam.get(key);
        }
        return null;
    }

    public void initUi() throws Exception {
    }

    public void initListener() throws Exception {
    }

    public void setContent(BusinessSmsMessage message, boolean isRebind) throws Exception {
    }

    public int getMsgCount() {
        return 0;
    }

    public int getCurrentIndex() {
        return 0;
    }

    public void changeData(Map<String, Object> map) {
    }

    public void runAnimation() {
    }
}
