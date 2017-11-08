package com.huawei.rcs.ui;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.mms.model.VcardModel;
import com.android.mms.model.VcardModel.VCardDetailNode;
import com.android.mms.util.VcardMessageHelper;
import com.google.android.gms.R;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.ResEx;
import java.util.List;

public class RcsVCardInfo {
    private boolean isUseDefaultImg = false;
    private Context mContext;
    private VcardMessageHelper mVCardMessageHelper;
    private VcardModel mVCardModel = null;
    private List<VCardDetailNode> mVcardDetailList = null;
    private Drawable mVcardDrawable = null;
    private String mVcardName = null;

    public RcsVCardInfo(Context context, Uri uri) throws MmsException {
        this.mContext = context;
        try {
            this.mVCardModel = new VcardModel(this.mContext, "text/x-vCard", uri);
            this.mVcardDetailList = this.mVCardModel.getVcardSelectedDetailList();
            this.mVcardName = this.mVCardModel.getName();
            this.mVcardDrawable = new BitmapDrawable(context.getResources(), this.mVCardModel.getBitmap());
            this.isUseDefaultImg = this.mVCardModel.isUseDefaultImg();
            if (this.isUseDefaultImg) {
                MLog.i("VCardParsingModel FileTrans: ", "VCardParsingModel  isUseDefaultImg ");
                this.mVcardDrawable = context.getResources().getDrawable(R.drawable.rcs_ic_contact_picture_holo_dark);
            }
            this.mVCardMessageHelper = new VcardMessageHelper(this.mContext, this.mVCardModel.getData(), this.mVCardModel.getVcardSelectedDetailList(), this.mVCardModel.getBitmap());
        } catch (Exception e) {
            MLog.e("VCardParsingModel FileTrans: ", Log.getStackTraceString(e));
            this.mVCardModel = null;
        }
    }

    public void showVcardDetail() {
        MLog.i("VCardParsingModel FileTrans: ", "showVcardDetail" + this.mVCardModel);
        if (this.mVCardModel.getBitmap() != null) {
            MLog.i("VCardParsingModel FileTrans: ", "mVCardModel.getBitmap() is null");
        }
        if (this.mVCardMessageHelper != null) {
            this.mVCardMessageHelper.viewVcardDetail();
        }
    }

    public Drawable getVCardDrawable() {
        return this.mVcardDrawable;
    }

    public VcardModel getVcardModel() {
        return this.mVCardModel;
    }

    public VcardMessageHelper getVcardMessageHelper() {
        return this.mVCardMessageHelper;
    }

    public void presentVcardThumbnail(Context context, View attactview, LinearLayout vattchNodesLayout, ImageView vcardView, boolean sendMessage) {
        String str = null;
        String strNum = null;
        List<VCardDetailNode> nodes = getVcardModel().getVcardDetailList();
        if (getVcardModel().getVcardSize() <= 1) {
            for (VCardDetailNode node : nodes) {
                if (str != null) {
                    strNum = node.getValue();
                    break;
                }
                str = node.getValue();
            }
        } else {
            String[] vCardNames = new String[nodes.size()];
            int i = 0;
            for (VCardDetailNode node2 : nodes) {
                int i2 = i + 1;
                vCardNames[i] = node2.getName();
                i = i2;
            }
            str = TextUtils.join(",", vCardNames);
        }
        setVcard(context, str, strNum, attactview, vattchNodesLayout, vcardView, sendMessage);
    }

    private void setVcard(Context context, String textSub1, String textSub2, View attactview, LinearLayout vattchNodesLayout, ImageView vcardView, boolean sendMessage) {
        String[] vcardDetails;
        vcardView.setImageResource(R.drawable.rcs_ic_contact_picture_holo_dark);
        if (textSub2 == null) {
            vcardDetails = new String[]{textSub1};
        } else {
            vcardDetails = getVcardMessageHelper().getVcardDetail();
        }
        setDetail(context, vcardDetails, attactview, vattchNodesLayout, sendMessage);
    }

    private void setDetail(Context context, String[] details, View attactview, LinearLayout vattchNodesLayout, boolean sendMessage) {
        int width = (int) context.getResources().getDimension(R.dimen.mms_attch_text_max_width);
        vattchNodesLayout.removeAllViews();
        vattchNodesLayout.requestLayout();
        int textColor = ResEx.self().getMsgItemTextColor(sendMessage);
        int i = 0;
        while (i < details.length && i != 5) {
            View child = vattchNodesLayout.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setMaxWidth(width);
                ((TextView) child).setText(details[i]);
                ((TextView) child).setTextColor(textColor);
            } else {
                TextView node = new TextView(this.mContext);
                node.setGravity(17);
                node.setSingleLine(true);
                node.setEllipsize(TruncateAt.END);
                node.setGravity(8388611);
                node.setTextColor(context.getResources().getColor(R.color.mms_main_text_color));
                if (i == 0) {
                    MLog.i("VCardParsingModel FileTrans: ", "setDetail name_width i = 0");
                    node.setTextSize(2, 15.0f);
                } else {
                    node.setTextSize(1, 13.0f);
                }
                node.setMaxWidth(width);
                node.setText(details[i]);
                node.setTextColor(textColor);
                vattchNodesLayout.addView(node, new LayoutParams(-2, -2));
            }
            i++;
        }
    }
}
