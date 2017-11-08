package cn.com.xy.sms.sdk.ui.popu.part;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.google.android.gms.R;

public class BubbleBodypost extends UIPart {
    private TextView mPostcode;
    private TextView mPostcodecon;
    private TextView mPostnumber;
    private TextView mPostnumbercon;
    private TextView mPoststate;
    private TextView mPoststatecon;
    private TextView mPosttell;
    private TextView mPosttellcon;

    public BubbleBodypost(Activity context, BusinessSmsMessage message, XyCallBack callback, int layoutId, ViewGroup root, int partId) {
        super(context, message, callback, layoutId, root, partId);
    }

    public void initUi() throws Exception {
        this.mPostnumber = (TextView) this.mView.findViewById(R.id.duoqu_post_numbercontent);
        this.mPostcode = (TextView) this.mView.findViewById(R.id.duoqu_post_codecontent);
        this.mPosttell = (TextView) this.mView.findViewById(R.id.duoqu_post_tellcontent);
        this.mPoststate = (TextView) this.mView.findViewById(R.id.duoqu_post_statecontent);
        this.mPostnumbercon = (TextView) this.mView.findViewById(R.id.duoqu_post_number);
        this.mPostcodecon = (TextView) this.mView.findViewById(R.id.duoqu_post_code);
        this.mPosttellcon = (TextView) this.mView.findViewById(R.id.duoqu_post_tell);
        this.mPoststatecon = (TextView) this.mView.findViewById(R.id.duoqu_post_state);
        super.initUi();
    }

    public void setContent(BusinessSmsMessage message, boolean isRebind) throws Exception {
        String Postcode = (String) message.getValue("view_post_code");
        String Posttell = (String) message.getValue("view_post_tell");
        String Poststate = (String) message.getValue("view_post_state");
        String Postcodelab = (String) message.getValue("view_post_codelab");
        String Posttelllab = (String) message.getValue("view_post_telllab");
        String Poststatelab = (String) message.getValue("view_post_statelab");
        setLableVisibility(this.mPostnumber, this.mPostnumbercon, (String) message.getValue("view_post_number"), (String) message.getValue("view_post_numberlab"));
        setLableVisibility(this.mPostcode, this.mPostcodecon, Postcode, Postcodelab);
        setLableVisibility(this.mPosttell, this.mPosttellcon, Posttell, Posttelllab);
        setLableVisibility(this.mPoststate, this.mPoststatecon, Poststate, Poststatelab);
        super.setContent(message, isRebind);
    }

    public void setLableVisibility(TextView t1, TextView t2, String value, String lable) {
        if (StringUtils.isNull(value)) {
            t1.setVisibility(8);
            t2.setVisibility(8);
            return;
        }
        t1.setVisibility(0);
        t2.setVisibility(0);
        ContentUtil.setText(t1, value, null);
        t2.setText(lable);
    }
}
