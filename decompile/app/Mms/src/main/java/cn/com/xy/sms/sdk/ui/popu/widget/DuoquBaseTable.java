package cn.com.xy.sms.sdk.ui.popu.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import java.util.ArrayList;
import java.util.List;

public abstract class DuoquBaseTable extends RelativeLayout {
    protected int mChildId = 0;
    protected List<DuoquHorizLFTableViewHolder> mViewHolderList = null;

    protected abstract void getHolder(int i, BusinessSmsMessage businessSmsMessage, int i2, String str, boolean z);

    protected abstract LayoutParams getLayoutParams(int i);

    protected abstract void initParams(Context context, AttributeSet attributeSet);

    public DuoquBaseTable(Context context, AttributeSet attrs) {
        super(context, attrs);
        initParams(context, attrs);
    }

    public void setContentList(BusinessSmsMessage message, int dataSize, String dataKey, boolean isRebind) {
        try {
            setVisibility(8);
            List<DuoquHorizLFTableViewHolder> holderList = this.mViewHolderList;
            int i;
            if (!isRebind || holderList == null) {
                this.mViewHolderList = new ArrayList();
                this.mChildId = 0;
                removeAllViews();
                for (i = 0; i < dataSize; i++) {
                    getHolder(i, message, dataSize, dataKey, false);
                }
                if (dataSize == 0) {
                    try {
                        setVisibility(8);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
                setVisibility(0);
                return;
            }
            int holderSize = holderList.size();
            DuoquHorizLFTableViewHolder tempHolder;
            if (holderSize - dataSize > 0) {
                for (i = 0; i < holderSize; i++) {
                    tempHolder = (DuoquHorizLFTableViewHolder) holderList.get(i);
                    if (i < dataSize) {
                        tempHolder.setVisibility(0);
                        tempHolder.setContent(i, message, dataKey, isRebind);
                    } else {
                        tempHolder.setVisibility(8);
                    }
                }
            } else {
                for (i = 0; i < dataSize; i++) {
                    if (i < holderSize) {
                        tempHolder = (DuoquHorizLFTableViewHolder) holderList.get(i);
                        tempHolder.setVisibility(0);
                        tempHolder.setContent(i, message, dataKey, isRebind);
                    } else {
                        getHolder(i, message, dataSize, dataKey, false);
                    }
                }
            }
            if (dataSize == 0) {
                try {
                    setVisibility(8);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            } else {
                setVisibility(0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            if (dataSize == 0) {
                try {
                    setVisibility(8);
                } catch (Exception e22) {
                    e22.printStackTrace();
                }
            } else {
                setVisibility(0);
            }
        } catch (Throwable th) {
            if (dataSize == 0) {
                try {
                    setVisibility(8);
                } catch (Exception e222) {
                    e222.printStackTrace();
                }
            } else {
                setVisibility(0);
            }
        }
    }
}
