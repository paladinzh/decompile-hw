package cn.com.xy.sms.sdk.ui.popu.part;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.google.android.gms.R;

public class BubbleBodyFeature extends UIPart {
    private static final String VIEW_TYPE_MOVIE = "movie";
    private TextView mDoublevertContenteFourTextView;
    private TextView mDoublevertContenteThreeTextView;
    private TextView mDoublevertTitleFourTextView;
    private TextView mDoublevertTitleThreeTextView;
    private TextView mHorizContenteOneTextView;
    private TextView mHorizContenteTwoTextView;
    private TextView mHorizTitleOneTextView;
    private TextView mHorizTitleTwoTextView;
    private TextView mVertContenteFiveTextView;
    private TextView mVertContenteSixTextView;
    private TextView mVertTitleFiveTextView;
    private TextView mVertTitleSixTextView;

    public BubbleBodyFeature(Activity context, BusinessSmsMessage message, XyCallBack callback, int layoutId, ViewGroup root, int partId) {
        super(context, message, callback, layoutId, root, partId);
    }

    public void initUi() throws Exception {
        this.mHorizTitleOneTextView = (TextView) this.mView.findViewById(R.id.duoqu_horiz_title_one);
        this.mHorizContenteOneTextView = (TextView) this.mView.findViewById(R.id.duoqu_horiz_content_one);
        this.mHorizTitleTwoTextView = (TextView) this.mView.findViewById(R.id.duoqu_horiz_title_two);
        this.mHorizContenteTwoTextView = (TextView) this.mView.findViewById(R.id.duoqu_horiz_content_two);
        this.mDoublevertTitleThreeTextView = (TextView) this.mView.findViewById(R.id.duoqu_double_vert_title_three);
        this.mDoublevertContenteThreeTextView = (TextView) this.mView.findViewById(R.id.duoqu_double_vert_content_three);
        this.mDoublevertTitleFourTextView = (TextView) this.mView.findViewById(R.id.duoqu_double_vert_title_four);
        this.mDoublevertContenteFourTextView = (TextView) this.mView.findViewById(R.id.duoqu_double_vert_content_four);
        this.mVertTitleFiveTextView = (TextView) this.mView.findViewById(R.id.duoqu_vert_title_five);
        this.mVertContenteFiveTextView = (TextView) this.mView.findViewById(R.id.duoqu_vert_content_five);
        this.mVertTitleSixTextView = (TextView) this.mView.findViewById(R.id.duoqu_vert_title_six);
        this.mVertContenteSixTextView = (TextView) this.mView.findViewById(R.id.duoqu_vert_content_six);
        super.initUi();
    }

    public void setContent(BusinessSmsMessage message, boolean isRebind) throws Exception {
        String type = (String) message.getValue("view_type");
        ContentUtil.setText(this.mHorizTitleOneTextView, (String) message.getValue("view_horiz_title_one"), ContentUtil.NO_DATA);
        ContentUtil.setText(this.mHorizContenteOneTextView, (String) message.getValue("view_horiz_content_one"), ContentUtil.NO_DATA);
        ContentUtil.setText(this.mHorizTitleTwoTextView, (String) message.getValue("view_horiz_title_two"), ContentUtil.NO_DATA);
        ContentUtil.setText(this.mHorizContenteTwoTextView, (String) message.getValue("view_horiz_content_two"), ContentUtil.NO_DATA);
        ContentUtil.setText(this.mDoublevertTitleThreeTextView, (String) message.getValue("view_doublevert_title_three"), ContentUtil.NO_DATA);
        ContentUtil.setText(this.mDoublevertContenteThreeTextView, (String) message.getValue("view_doublevert_content_three"), ContentUtil.NO_DATA);
        ContentUtil.setText(this.mDoublevertTitleFourTextView, (String) message.getValue("view_doublevert_title_four"), ContentUtil.NO_DATA);
        ContentUtil.setText(this.mDoublevertContenteFourTextView, (String) message.getValue("view_doublevert_content_four"), ContentUtil.NO_DATA);
        ContentUtil.setText(this.mVertTitleFiveTextView, (String) message.getValue("view_vert_title_five"), ContentUtil.NO_DATA);
        ContentUtil.setText(this.mVertContenteFiveTextView, (String) message.getValue("view_vert_content_five"), ContentUtil.NO_DATA);
        if (type.equals(VIEW_TYPE_MOVIE)) {
            ContentUtil.setViewVisibility(this.mVertTitleSixTextView, 0);
            ContentUtil.setViewVisibility(this.mVertContenteSixTextView, 0);
            ContentUtil.setText(this.mVertTitleSixTextView, (String) message.getValue("view_vert_title_six"), ContentUtil.NO_DATA);
            ContentUtil.setText(this.mVertContenteSixTextView, (String) message.getValue("view_vert_content_six"), ContentUtil.NO_DATA);
            return;
        }
        ContentUtil.setViewVisibility(this.mVertTitleSixTextView, 8);
        ContentUtil.setViewVisibility(this.mVertContenteSixTextView, 8);
    }
}
