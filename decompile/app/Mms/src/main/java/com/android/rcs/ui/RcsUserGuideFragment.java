package com.android.rcs.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.rcs.commonInterface.IfMsgplus;
import com.huawei.rcs.ui.RcsUserGuide;
import com.huawei.rcs.utils.RcsProfile;

public class RcsUserGuideFragment extends HwBaseFragment {
    private static String SECNE_NUM = "keyNum";
    private static String SECNE_PREFERENCE_KEY = "preferenceKey";
    private String mPreferenKeyName;
    private View mRootView;
    private int mSceneNum;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getIntentInfo();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mRootView = inflater.inflate(R.layout.rcs_mms_user_guide, container, false);
        return this.mRootView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MLog.v("RcsUserGuideFragment", "onActivityCreated");
        initView();
    }

    public void onNewIntent(Intent intent) {
        setIntent(intent);
        getIntentInfo();
        initView();
    }

    private void getIntentInfo() {
        this.mSceneNum = getIntent().getIntExtra(SECNE_NUM, 0);
        this.mPreferenKeyName = getIntent().getStringExtra(SECNE_PREFERENCE_KEY);
    }

    private void initView() {
        View bottomView = this.mRootView.findViewById(R.id.rcs_guide_bottom);
        switch (this.mSceneNum) {
            case 1:
                showConversationListGuide();
                break;
            case 2:
                showSingleChatGuide();
                break;
            case 3:
                showGroupChatGuide();
                break;
            default:
                return;
        }
        if (bottomView != null) {
            bottomView.setContentDescription(getContext().getResources().getString(R.string.rcs_guide_page));
            bottomView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    RcsUserGuideFragment.this.setPrefenceToFalse();
                    RcsUserGuideFragment.this.finishSelf(false);
                }
            });
        }
    }

    public void onPause() {
        super.onPause();
        finishSelf(false);
    }

    private void showConversationListGuide() {
        View middleView = this.mRootView.findViewById(R.id.guide_conversation_list);
        View newMessageText = this.mRootView.findViewById(R.id.guide_new_message_text);
        View newGroupText = this.mRootView.findViewById(R.id.guide_new_group_text);
        if (!(middleView == null || newMessageText == null || newGroupText == null)) {
            middleView.setVisibility(0);
            newMessageText.setVisibility(0);
            newGroupText.setVisibility(0);
        }
        View newView = this.mRootView.findViewById(R.id.guide_conversation_list_new);
        Drawable newViewDrawable = getResources().getDrawable(R.drawable.rcs_guide_conversation_list_new);
        if (newView != null && newViewDrawable != null) {
            newView.setVisibility(0);
            int marginwidth = MessageUtils.getScreenWidth(getActivity()) - newViewDrawable.getIntrinsicWidth();
            if (marginwidth > 0) {
                LayoutParams newViewLayoutParam = (LayoutParams) newView.getLayoutParams();
                newViewLayoutParam.setMarginStart(marginwidth / 4);
                newView.setLayoutParams(newViewLayoutParam);
            }
        }
    }

    private void showSingleChatGuide() {
        View middleView = this.mRootView.findViewById(R.id.guide_single_chat);
        View chatModeSwitchText = this.mRootView.findViewById(R.id.guide_chat_mode_switch_text);
        View attachShareText = this.mRootView.findViewById(R.id.guide_attach_share_text);
        if (middleView != null && chatModeSwitchText != null && attachShareText != null) {
            middleView.setVisibility(0);
            chatModeSwitchText.setVisibility(0);
            attachShareText.setVisibility(0);
        }
    }

    private void showGroupChatGuide() {
        View middleView = this.mRootView.findViewById(R.id.rcs_guide_group_chat);
        View enterGroupDetailsText = this.mRootView.findViewById(R.id.guide_enter_group_details_text);
        if (middleView != null && enterGroupDetailsText != null) {
            middleView.setVisibility(0);
            enterGroupDetailsText.setVisibility(0);
        }
    }

    private void setPrefenceToFalse() {
        if (this.mPreferenKeyName != null) {
            Editor editor = getContext().getSharedPreferences("pref_key_im_settings", 0).edit();
            editor.putBoolean(this.mPreferenKeyName, false);
            editor.commit();
        }
        getActivity().setResult(-1);
    }

    public boolean onBackPressed() {
        setPrefenceToFalse();
        return false;
    }

    public static String getPreferenceKey(int sceneNum) {
        switch (sceneNum) {
            case 1:
                return "conversationListFirstIn";
            case 2:
                return "singleChatFirstIn";
            case 3:
                return "groupChatFirstIn";
            default:
                return null;
        }
    }

    public static void startUserGuide(Context context, int sceneNum) {
        IfMsgplus aMsgPlus = RcsProfile.getRcsService();
        boolean isUserGuideEnable = true;
        if (aMsgPlus != null) {
            try {
                isUserGuideEnable = !"0".equals(aMsgPlus.getXmlConfigValue(4));
            } catch (RemoteException e) {
                MLog.e("RcsUserGuideFragment", "getXmlConfigValue error");
            }
        }
        if (isUserGuideEnable) {
            String preferenceKey = getPreferenceKey(sceneNum);
            if (preferenceKey == null) {
                MLog.e("RcsUserGuideFragment", "Error sceneNum: " + sceneNum);
                return;
            } else if (context.getSharedPreferences("pref_key_im_settings", 0).getBoolean(preferenceKey, true)) {
                Intent guideIntent = new Intent();
                guideIntent.putExtra(SECNE_NUM, sceneNum);
                guideIntent.putExtra(SECNE_PREFERENCE_KEY, preferenceKey);
                guideIntent.setClass(context, RcsUserGuide.class);
                if (sceneNum == 2) {
                    ((Activity) context).startActivityForResult(guideIntent, 15001);
                } else {
                    context.startActivity(guideIntent);
                }
                return;
            } else {
                MLog.d("RcsUserGuideFragment", "Not first in this scene: " + sceneNum);
                return;
            }
        }
        MLog.d("RcsUserGuideFragment", "rcs user guide feature is not enabled!!!");
    }
}
