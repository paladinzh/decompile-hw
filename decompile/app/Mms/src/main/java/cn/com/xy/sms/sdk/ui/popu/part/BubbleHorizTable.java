package cn.com.xy.sms.sdk.ui.popu.part;

import android.app.Activity;
import android.view.ViewGroup;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.ui.popu.widget.DuoquHorizItemTable;
import com.google.android.gms.R;
import java.util.Map;

public class BubbleHorizTable extends UIPart {
    private static final String TABLE_KEY = "duoqu_table_data_horiz";
    private DuoquHorizItemTable mContentListView;

    public BubbleHorizTable(Activity mContext, BusinessSmsMessage message, XyCallBack callback, int layoutId, ViewGroup root, int partId) {
        super(mContext, message, callback, layoutId, root, partId);
    }

    public void initUi() {
        this.mContentListView = (DuoquHorizItemTable) this.mView.findViewById(R.id.duoqu_horiz_list);
    }

    public void setContent(BusinessSmsMessage message, boolean isRebind) throws Exception {
        this.mMessage = message;
        if (message != null && this.mContentListView != null) {
            int size = message.getTableDataSize(TABLE_KEY);
            if (size > 0) {
                this.mView.setVisibility(0);
                this.mContentListView.setContentList(message, size, TABLE_KEY, isRebind);
            } else {
                this.mView.setVisibility(8);
            }
            ThemeUtil.setViewBg(Constant.getContext(), this.mView, (String) message.getValue("v_by_bg"), R.drawable.duoqu_rectangledrawble);
        }
    }

    public void changeData(Map<String, Object> param) {
        super.changeData(param);
        try {
            setContent(this.mMessage, true);
        } catch (Exception e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("BubbleHorizTable changeData error:" + e.getMessage(), e);
        }
    }
}
