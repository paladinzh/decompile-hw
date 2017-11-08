package cn.com.xy.sms.sdk.ui.popu.popupview;

import java.io.Serializable;
import java.util.ArrayList;

public class PartViewParam implements Serializable {
    public static final String BODY = "B";
    public static final String FOOT = "F";
    public static final String HEAD = "H";
    private static final long serialVersionUID = 1;
    public boolean mAddImageMark = false;
    public int mBodyHeightType = 0;
    public int mBodyMaxHeightType = 0;
    public ArrayList<Integer> mLayOutList = null;
    public boolean mNeedScroll = false;
    public int mPaddingBottomType = 0;
    public int mPaddingLeftType = 0;
    public int mPaddingRightType = 0;
    public int mPaddingTopType = 0;
    public int mUiPartMarginTopType = 0;
}
