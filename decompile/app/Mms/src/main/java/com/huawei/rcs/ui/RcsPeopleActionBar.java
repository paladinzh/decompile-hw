package com.huawei.rcs.ui;

import android.content.Context;
import android.net.Uri;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.RcsMmsConfig;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.EmuiMenuText;
import com.huawei.mms.ui.PeopleActionBar.PeopleActionBarAdapter;
import com.huawei.rcs.utils.RcsProfile;

public class RcsPeopleActionBar implements OnClickListener {
    private static final Uri IM_GROUPCHAT_URI = Uri.parse("content://rcsim/rcs_groups");
    private boolean isRcsOn = RcsCommonConfig.isRCSSwitchOn();
    private IHwCustPeopleActionBarCallback mCallback;
    private ImageView mComposingPenView;
    private LinearLayout mComposingView;
    private Context mContext;
    private PeopleActionBarAdapter mDataAdatper;
    private TextView mGroupMemberCountView;
    private View mHoldChat;
    private View mHoldGroupChat;
    private boolean mIsComposingStatus = false;
    private EmuiMenuText mMenuGroupChatDetail;
    private ImageView mNotDisturbView;
    private PeopleActionBarAdapterExt mRcsAdapterExt;

    public interface PeopleActionBarAdapterExt {
        void createGroupChat();

        boolean isRcsGroupChat();

        void showRcsGroupChatDetail();
    }

    public interface IHwCustPeopleActionBarCallback {
        void updateRcsMenu(boolean z);
    }

    public void setMenuMultiLine(EmuiMenuText mentFirst, EmuiMenuText menuSecond, EmuiMenuText menuThird, EmuiMenuText menuFouth) {
        if (RcsMmsConfig.getEnablePeopleActionBarMultiLine()) {
            if (mentFirst != null) {
                mentFirst.setSingleLine(false);
            }
            if (menuSecond != null) {
                menuSecond.setSingleLine(false);
            }
            if (menuThird != null) {
                menuThird.setSingleLine(false);
            }
            if (menuFouth != null) {
                menuFouth.setSingleLine(false);
            }
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_group_chat_detail:
                if (this.mRcsAdapterExt != null) {
                    this.mRcsAdapterExt.showRcsGroupChatDetail();
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void createGroupChat() {
        if (this.mRcsAdapterExt != null) {
            this.mRcsAdapterExt.createGroupChat();
        }
    }

    public void initRcsView(View view, Context context) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            MLog.i("RcsPeopleActionBar", "start to initRcsView");
            ViewStub stubGroupChat = (ViewStub) view.findViewById(R.id.menu_ext_stub);
            stubGroupChat.setLayoutResource(R.layout.rcs_menu_groupchat_detail_stub);
            this.mHoldGroupChat = (ViewGroup) stubGroupChat.inflate();
            this.mMenuGroupChatDetail = (EmuiMenuText) view.findViewById(R.id.menu_group_chat_detail);
            this.mMenuGroupChatDetail.setOnClickListener(this);
            this.mHoldGroupChat.setVisibility(8);
            this.mHoldChat = view.findViewById(R.id.custom_menus);
            this.mContext = context;
        }
    }

    private boolean isNeedToShowCreateGroupchat(String peerNumber) {
        boolean isRcsUser = false;
        boolean z = false;
        if (RcsProfile.getRcsService() != null) {
            try {
                isRcsUser = RcsProfile.getRcsService().isRcsUeser(peerNumber);
                z = RcsProfile.getRcsService().getLoginState();
                MLog.d("RcsPeopleActionBar", "isNeedToShowCreateGroupchat login status " + z + " rcs user " + isRcsUser);
            } catch (RemoteException e) {
                MLog.e("RcsPeopleActionBar", "getRcsService error " + e.toString());
            }
        }
        if (isRcsUser) {
            return z;
        }
        return false;
    }

    public boolean isRcsGroupChat() {
        if (!RcsCommonConfig.isRCSSwitchOn() || this.mRcsAdapterExt == null) {
            return false;
        }
        return this.mRcsAdapterExt.isRcsGroupChat();
    }

    public boolean changeToGroupChatMenu() {
        if (!RcsCommonConfig.isRCSSwitchOn()) {
            return false;
        }
        if (this.mRcsAdapterExt == null || !this.mRcsAdapterExt.isRcsGroupChat()) {
            this.mHoldGroupChat.setVisibility(8);
            this.mHoldChat.setVisibility(0);
            return false;
        }
        this.mHoldGroupChat.setVisibility(0);
        this.mHoldChat.setVisibility(8);
        this.mMenuGroupChatDetail.updateMenu(9, R.string.groupchat_detail, R.drawable.rcs_groupchat_detail_dark);
        return true;
    }

    public void updateRcsMenu() {
        if (RcsCommonConfig.isRCSSwitchOn() && this.mRcsAdapterExt != null) {
            String number = this.mDataAdatper.getNumber();
            if (number == null) {
                MLog.e("RcsPeopleActionBar", "updateRcsMenu number is null");
                return;
            }
            boolean isNeedToShowCreateGroupchat = isNeedToShowCreateGroupchat(number);
            MLog.i("RcsPeopleActionBar", "updateRcsMenu isNeedToShowCreateGroupchat: " + isNeedToShowCreateGroupchat);
            this.mCallback.updateRcsMenu(isNeedToShowCreateGroupchat);
        }
    }

    public void updateTitle(Context context, View view) {
        if (this.isRcsOn && view != null && context != null) {
            if ((context instanceof RcsGroupChatComposeMessageActivity) || (context instanceof ComposeMessageActivity)) {
                this.mGroupMemberCountView = (TextView) view.findViewById(R.id.group_member_count);
                this.mComposingView = (LinearLayout) view.findViewById(R.id.composing);
                this.mComposingPenView = (ImageView) view.findViewById(R.id.composing_pen);
                this.mNotDisturbView = (ImageView) view.findViewById(R.id.icon_groupchat_notify_silent);
                return;
            }
            MLog.d("RcsPeopleActionBar", "not groupchat and not signal chat return");
        }
    }

    public void updateGroupMenuHolderLayout(boolean isLandscape, boolean mIsExpanded) {
        if (RcsCommonConfig.isRCSSwitchOn() && this.mHoldGroupChat != null && this.mHoldChat != null) {
            MLog.d("RcsPeopleActionBar", "updateGroupMenuHolderLayout(isLandscape:" + isLandscape + ",mIsExpanded:" + mIsExpanded + ")");
            if (this.mRcsAdapterExt == null || !this.mRcsAdapterExt.isRcsGroupChat()) {
                this.mHoldGroupChat.setVisibility(8);
                this.mHoldChat.setVisibility(0);
            } else {
                LayoutParams params = (LayoutParams) this.mHoldGroupChat.getLayoutParams();
                if (isLandscape) {
                    params.width = (int) this.mHoldGroupChat.getResources().getDimension(R.dimen.mms_people_action_bar_max_width_landscape);
                    params.setMarginEnd((int) this.mHoldGroupChat.getResources().getDimension(R.dimen.mms_people_action_bar_marginend_landscape));
                    params.setMarginStart((int) this.mHoldGroupChat.getResources().getDimension(R.dimen.mms_people_action_bar_marginend_landscape));
                    params.topMargin = (int) this.mHoldGroupChat.getResources().getDimension(R.dimen.mms_people_action_bar_margintop_landscape);
                    params.bottomMargin = (int) this.mHoldGroupChat.getResources().getDimension(R.dimen.rcs_mms_people_action_bar_marginbottom_landscape);
                    params.addRule(14);
                    if (mIsExpanded) {
                        setGroupMenuAlpha(ContentUtil.FONT_SIZE_NORMAL);
                    } else {
                        setGroupMenuAlpha(0.0f);
                    }
                } else {
                    int mSrcDpi = SystemProperties.getInt("ro.sf.lcd_density", 0);
                    int mRealDpi = SystemProperties.getInt("persist.sys.dpi", mSrcDpi);
                    params.width = (int) this.mHoldGroupChat.getResources().getDimension(R.dimen.mms_people_action_bar_max_width_landscape);
                    float marginWidth = ((this.mHoldGroupChat.getResources().getDimension(R.dimen.mms_people_action_bar_max_width_landscape) * ((float) (mSrcDpi - mRealDpi))) / ((float) mRealDpi)) / 2.0f;
                    float scale = ((float) mSrcDpi) / ((float) mRealDpi);
                    params.setMarginStart((int) ((this.mHoldGroupChat.getResources().getDimension(R.dimen.rcs_mms_people_action_bar_marginendstart_cust) * scale) + marginWidth));
                    params.setMarginEnd((int) ((this.mHoldGroupChat.getResources().getDimension(R.dimen.rcs_mms_people_action_bar_marginendstart_cust) * scale) + marginWidth));
                    params.topMargin = (int) this.mHoldGroupChat.getResources().getDimension(R.dimen.action_bar_menu_padding_top);
                    params.bottomMargin = (int) this.mHoldGroupChat.getResources().getDimension(R.dimen.rcs_mms_people_action_bar_marginbottom_landscape);
                    this.mHoldGroupChat.setVisibility(0);
                }
                this.mHoldChat.setVisibility(8);
                this.mMenuGroupChatDetail.updateMenu(9, R.string.groupchat_detail, R.drawable.rcs_groupchat_detail_dark);
            }
        }
    }

    public void resetParaLayoutGroup(boolean mIsExpanded) {
        if (RcsCommonConfig.isRCSSwitchOn() && this.mHoldGroupChat != null) {
            if (mIsExpanded) {
                setGroupMenuAlpha(ContentUtil.FONT_SIZE_NORMAL);
            } else {
                setGroupMenuAlpha(0.0f);
            }
            MLog.d("RcsPeopleActionBar", "resetParaLayoutGroup mIsExpanded:" + mIsExpanded);
        }
    }

    public void setHwCustCallback(IHwCustPeopleActionBarCallback callback) {
        this.mCallback = callback;
    }

    public boolean isShowMenuHolder(boolean show) {
        if (!this.isRcsOn) {
            return show;
        }
        if (isRcsGroupChat()) {
            show = false;
        }
        return show;
    }

    public void setGroupMenuAlpha(float alpha) {
        if (RcsCommonConfig.isRCSSwitchOn() && this.mHoldGroupChat != null) {
            this.mHoldGroupChat.setAlpha(alpha);
        }
    }
}
